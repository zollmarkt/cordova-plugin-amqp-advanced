package org.amqp.notification;

import java.util.ArrayList;
import java.util.List;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import org.amqp.notification.PushNotification;
import org.amqp.notification.PushManager;

public class Push extends CordovaPlugin {
	private static CallbackContext clbContext;
	private PushManager manager;
	public static boolean inPause;
	private static String notificationEventListener;
	private static CordovaWebView cordovaWebView;
	private static List<PushNotification> cachedNnotifications = new ArrayList<PushNotification>();

	public static final String TAG = "Push";

	public static final String ACTION_INITIALIZE = "initialize";


	@Override
	public boolean execute(String action, JSONArray args, CallbackContext callbackContext) {
		try {
			clbContext = callbackContext;
			notificationEventListener = args.getJSONObject(0).getString("notificationListener");

                        //read the configuration
                        JSONObject configJson = args.getJSONObject(0).getJSONObject("configuration");
                        
                        Config.init(configJson, cordova.getActivity().getApplicationContext());
                        
                        cordovaWebView = this.webView;
                        Log.e("INIT","INIT");
			this.manager = new PushManager(cordova.getActivity(),this);
        // ############# INIT HERE #############
        if (ACTION_INITIALIZE.equals(action)) {
				// Check if there is cached notifications
				if (!cachedNnotifications.isEmpty()) {
					for (PushNotification notification : cachedNnotifications) {
						Log.d(Push.TAG, "Proceed push: " + notification.toString());
						proceedNotification(notification);
					}
					cachedNnotifications.clear();
				}

				clbContext.success();
				return true;
			}

                } catch (Exception e) {
			Log.e("<<<<<<EXCEPTION >>>>>>>: " , e.getMessage());
			clbContext.error(e.getMessage());
			return false;
		}
                return false;

	}


	public static boolean isActive() {

		if (cordovaWebView != null) {
			return true;
		}
		return false;
	}

	@Override
	public void onPause(boolean multitasking) {
		super.onPause(multitasking);
		Push.inPause = true;
	}

	@Override
	public void onResume(boolean multitasking) {
		super.onResume(multitasking);
		Push.inPause = false;
	}


	public static void sendJavascript(String js) {
		if (null != cordovaWebView) {
			Log.d("js", "JS" + js);
			//cordovaWebView.sendJavascript(js);
			Log.e("CordovaWebView", "CordovaWebView status: " + (cordovaWebView != null));

      cordovaWebView.loadUrl("javascript:"+js);
			//Toast.makeText(cordova.getActivity().getApplicationContext(), "Message: " + message, Toast.LENGTH_LONG).show();

                        
		}
	}


	public static void proceedNotification(PushNotification extras) {
		if (null != extras) {
				if (null != cordovaWebView) {
						try {
								// Verificăm dacă extras este un JSON valid
								String message = extras.toString(); // String-ul mesajului
								String js;

								if (isJsonValid(message)) {
										// Dacă este JSON, îl trimitem direct
										js = "window.push.listenerCallback(\"BEEP\", " + message + ")";
								} else {
										// Dacă este string simplu, îl tratăm ca atare
										js = "window.push.listenerCallback(\"BEEP\", \"" + message.replace("\"", "\\\"") + "\")";
								}

								Log.d("Push", "Sending JavaScript event: " + js);
								cordovaWebView.sendJavascript(js);
						} catch (Exception e) {
								Log.e("Push", "Error while sending notification", e);
						}
				}
		}
	}

	//Check if is valid JSON
	private static boolean isJsonValid(String json) {
			try {
					new JSONObject(json);
					return true;  
			} catch (JSONException ex) {
					return false; 
			}
	}



	@Override
	public void onDestroy() {
		super.onDestroy();
		cordovaWebView = null;
	}

	@Override
	public boolean onOverrideUrlLoading(String url) {
		return false;
	}
}
