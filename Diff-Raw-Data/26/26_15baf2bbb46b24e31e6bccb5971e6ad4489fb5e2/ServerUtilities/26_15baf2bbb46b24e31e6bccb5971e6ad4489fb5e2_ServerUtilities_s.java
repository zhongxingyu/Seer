 package com.trueffelscout.tsadmin.gcm.utilities;
 
 import static com.trueffelscout.tsadmin.gcm.utilities.CommonUtilities.SERVER_UNREGISTER;
 import static com.trueffelscout.tsadmin.gcm.utilities.CommonUtilities.SERVER_URL;
 import static com.trueffelscout.tsadmin.gcm.utilities.CommonUtilities.TAG;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.content.Context;
 import android.util.Log;
 import android.widget.Toast;
 
 import com.google.android.gcm.GCMRegistrar;
 import com.trueffelscout.tsadmin.R;
 
 public class ServerUtilities{
 	
 	public static void register(Context context,String regId, String user, String email){
 	    String params = "user="+user+"&email="+email+"&device="+regId;
 	    String result = post(SERVER_URL, params);
 	    if(result!=null){
 	    	try {
				JSONObject json = new JSONObject(result);
				result = json.getString("registerResult");
 		    	if(result.equalsIgnoreCase("Success")||result.equalsIgnoreCase("ok")){
 		    		GCMRegistrar.setRegisteredOnServer(context, true);
 		    	}else{
 		    		new AlertDialogManager().showAlertDialog(context, "Error", "register could not be done", false);
 		    	}
	    	} catch (JSONException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 	    }
 	}
 		
 	public static void unregister(final Context context, final String regId, String user) {
         Log.i(TAG, "unregistering device (regId = " + regId + ")");
         String serverUrl = SERVER_UNREGISTER;
         String params = "device="+regId+"&user="+user;
         post(serverUrl, params);
         GCMRegistrar.setRegisteredOnServer(context, false);
         String message = context.getString(R.string.server_unregister);
         Toast.makeText(context, message, Toast.LENGTH_LONG);
         //CommonUtilities.displayMessage(context, message);
     }
 	
 	public static String post(String urlStr, String params){
 		String response=null;
 		URL url;
         try {
             url = new URL(urlStr+"?"+params);
        
 	        HttpURLConnection conn;
 			conn = (HttpURLConnection)url.openConnection();
 			conn.setDoOutput(true);
             conn.setRequestMethod("GET");
             conn.setRequestProperty("Accept-Charset", "ISO-8859-1");
             conn.connect();
             InputStream in = conn.getInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(in));
             response = reader.readLine();
             System.out.println(response);
 
             int status = conn.getResponseCode();
             if (status != 200) {
               throw new IOException("Post failed with error code " + status);
             }
             conn.disconnect();
         } catch (MalformedURLException e) {
         	e.printStackTrace();
   		} catch (IOException e) {
 			e.printStackTrace();
   		}
         return response;
 	}
 
 }
