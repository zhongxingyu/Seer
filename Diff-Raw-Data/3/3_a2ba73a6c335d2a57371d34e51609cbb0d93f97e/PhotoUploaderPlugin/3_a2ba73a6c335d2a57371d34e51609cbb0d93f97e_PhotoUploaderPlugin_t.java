 /**
  * 
  */
 package edu.ucla.cens.andwellness.mobile.plugin;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.graphics.Bitmap;
 import android.provider.MediaStore.Images.Media;
 import android.util.Log;
 
 import com.phonegap.api.Plugin;
 import com.phonegap.api.PluginResult;
 import com.phonegap.api.PluginResult.Status;
 
 import edu.ucla.cens.andwellness.mobile.AndWellnessApi;
 import edu.ucla.cens.andwellness.mobile.AndWellnessApi.Result;
 import edu.ucla.cens.andwellness.mobile.AndWellnessApi.ServerResponse;
 
 /**
  * @author mistralay
  *
  */
 public class PhotoUploaderPlugin extends Plugin {
 
 	/* (non-Javadoc)
 	 * @see com.phonegap.api.Plugin#execute(java.lang.String, org.json.JSONArray, java.lang.String)
 	 */
 	@Override
 	public PluginResult execute(String action, JSONArray data, String callbackId) {
 		PluginResult result = null;
 
 		Log.v("PLUGIN", action);
 		Log.v("PLUGIN", data.toString());
 		Log.v("PLUGIN", callbackId);
 		if (action.equals("upload")) { 
 			Log.v("PLUGIN", "upload action");
 
 			Log.v("PLUGIN", data.toString());
 			try { 
 				JSONObject dataObject = data.getJSONObject(0);
 				
 				String username = dataObject.getString("u"); 
 				String hashedPassword = dataObject.getString("p");
 				String client = dataObject.getString("ci");
 				String campaignName = dataObject.getString("c");
 				String uuid = dataObject.getString("i");
 				String imageUrl = dataObject.getString("url");
 //				File file = new File(imageUrl);
 				android.net.Uri androidImageUri = android.net.Uri.parse(imageUrl);
 				
 				// do resizing 
 				
 				Bitmap source;
 				Bitmap scaled = null;
 				try {
 					source = Media.getBitmap(this.ctx.getContentResolver(), androidImageUri);
 					if (source.getWidth() > source.getHeight()) {
 						scaled = Bitmap.createScaledBitmap(source, 800, 600, false);
 					} else {
 						scaled = Bitmap.createScaledBitmap(source, 600, 800, false);
 					}			
 				} catch (FileNotFoundException e1) {
 					// TODO Auto-generated catch block
 					e1.printStackTrace();
 				} catch (IOException e1) {
 					// TODO Auto-generated catch block
 					e1.printStackTrace();
 				}
 //				Bitmap source = BitmapFactory.decodeFile(imageUrl);
 				
 				
 				try {
 			       FileOutputStream out = new FileOutputStream("/sdcard/" + uuid + "temp.jpg");
 			       scaled.compress(Bitmap.CompressFormat.JPEG, 80, out);
 			       out.flush();
 			       out.close();
 				} catch (Exception e) {
 			       e.printStackTrace();
 				}
 				
 				
 //				Log.v("PLUGIN", username + " " + hashPassword); 
 				AndWellnessApi api = new AndWellnessApi(this.ctx);
 				ServerResponse response = api.mediaUpload(username, 
 													      hashedPassword, 
 													      client, 
 													      campaignName, 
 													      uuid, 
 													      new File("/sdcard/" + uuid + "temp.jpg"));
 				
 				new File("/sdcard/" + uuid + "temp.jpg").delete();
 				
 				JSONObject apiResult = new JSONObject();
 				if (response.getResult() == Result.SUCCESS) { 
 					apiResult.put("result", "success");
 				} else { 
 					apiResult.put("result", "failure");
 				}
 				result = new PluginResult(Status.OK, apiResult); 
 
 			} catch (JSONException jsonEx) {
 				result = new PluginResult(Status.JSON_EXCEPTION);
 			}
 		} else { 
 			result = new PluginResult(Status.INVALID_ACTION);
 		}
 				
 		return result;
 	}
 
 }
