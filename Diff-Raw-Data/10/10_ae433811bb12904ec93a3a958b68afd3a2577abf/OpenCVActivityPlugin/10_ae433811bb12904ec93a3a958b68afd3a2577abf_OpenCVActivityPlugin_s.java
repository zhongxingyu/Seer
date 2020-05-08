 package co.mwater.opencvactivity;
 
 import org.apache.cordova.api.CallbackContext;
 import org.apache.cordova.api.CordovaPlugin;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.content.Intent;
 
 /*
  * Call using action "launch" with parameters processId, processParams, title.
  */
 public class OpenCVActivityPlugin extends CordovaPlugin {
 	private static int REQUEST_OPENCV_ACTIVITY = 1234;
 	CallbackContext currentCallbackContext;
 
 	@Override
 	public boolean execute(String action, JSONArray args,
 			CallbackContext callbackContext) throws JSONException {
 		if ("launch".equals(action)) {
 			// Launch OpenCV
 			currentCallbackContext = callbackContext;
 			Intent intent = new Intent(cordova.getActivity(), OpenCVActivity.class);
 			intent.putExtra("processId", args.getString(0));
 			intent.putExtra("title", args.getString(2));
 			
 			JSONArray paramsJSON = args.getJSONArray(1);
 			String[] params = new String[paramsJSON.length()];
 			for (int i=0;i<params.length;i++)
 				params[i]=paramsJSON.getString(i);
 			intent.putExtra("processParams", params);
 			cordova.startActivityForResult(this, intent, REQUEST_OPENCV_ACTIVITY);
 			return true;
 		}
 		return false;
 	}
 
 	@Override
 	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
 		if (requestCode == REQUEST_OPENCV_ACTIVITY) {
 			// Call Javascript with results
 			try {
				JSONObject res = new JSONObject(intent.getStringExtra("result"));
				currentCallbackContext.success(res);
 			} catch (JSONException e) {
 				currentCallbackContext.error(e.getLocalizedMessage());
 			}
 		}
 	}
 }
