 package org.flying.lions;
 
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import org.apache.cordova.api.PluginResult;
 import org.apache.cordova.api.PluginResult.Status;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.telephony.SmsMessage;
 import android.util.Log;
 
 import com.phonegap.api.Plugin;
 
 
 
 public class SMSReceiverPlugin extends Plugin {
 	/** Logger-Tag. */
     public static final String TAG = "SMSReceiverPlugin";
 
     /** Constant for action REGISTER */
     public static final String REGISTER="register";
 
     /** Constant for action UNREGISTER */
 	public static final String UNREGISTER="unregister";
 
 	/** Currently active plugin instance */
 	public static Plugin currentPluginInstance;
 
 	/** save Callbackfunction-Name for later use */
 	private static String callbackFunction;
 
 	@Override
     public PluginResult execute(final String action, final JSONArray data, final String callbackId)
     {
 		Log.v(TAG + ":execute", "action=" + action);
 
 		PluginResult result = null;
 
 		if (REGISTER.equals(action))
 		{
 			Log.v(TAG + ":execute", "data=" + data.toString());
 
 			try
 			{
 				JSONObject json = data.getJSONObject(0);
 				callbackFunction = (String) json.get("callback");
 				currentPluginInstance = this;
 				result = new PluginResult(Status.OK);
 			}
 			catch (JSONException e)
 			{
 				Log.e(TAG, "Got JSON Exception " + e.getMessage());
 				result = new PluginResult(Status.JSON_EXCEPTION);
 			}
 		}
 		else if (UNREGISTER.equals(action))
 		{
 			currentPluginInstance = null;
 			result = new PluginResult(Status.OK);
 		}
 		else
 		{
 			Log.e(TAG, "Invalid action : " + action);
 			result = new PluginResult(Status.INVALID_ACTION);
 		}
 
 		return result;
 	}
 
 	/**
 	 * Static function to send a SMS to JS.
 	 * @param json
 	 */
 	public static void sendMessage(final SmsMessage msg)
 	{
			Log.d(TAG, "sendMessage Called");
 			// build JSON message
 			JSONObject json = new JSONObject();
 			try
 			{
 				
 				json.put("origin", msg.getOriginatingAddress());
 				json.put("body", msg.getMessageBody());
 				json.put("id", msg.getTimestampMillis());
 				
 				
 				Date dateObj = new Date(msg.getTimestampMillis());
 				DateFormat df = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
 				String timeDate = df.format(dateObj);
 				//Log.v(TAG + ":sendJavascript", timeDate);
 				
 				json.put("time", timeDate);
 			}
 			catch (JSONException e)
 			{
 		 	   	Log.e(TAG + ":sendMessage", "JSON exception");
 			}
 			catch (Exception ex) {
 				Log.e(TAG + ":sendMessage", "Decoder error");
 	        }
 
 			// When the Activity is not loaded, the currentPluginInstance is null
 			
 			if (currentPluginInstance != null)
 			{
 				// build code to call function
 				String code =  "javascript:" + callbackFunction + "(" + json.toString() + ");";
 				
 		 	   	Log.v(TAG + ":sendJavascript", code);
 	
 		 	   	// execute code
 		 	   	currentPluginInstance.sendJavascript(code);
 			}
 		
 	}
 
 }
