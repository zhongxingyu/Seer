 package uk.co.ipodling.newsblurb;
 /*
  * No, not the salt
  * */
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.List;
  
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.protocol.HTTP;
 import org.apache.http.util.EntityUtils;
 import org.json.JSONException;
 import org.json.JSONObject;
  
 import android.util.Log;
 
 public class ParseTheJSON {
 	 	static InputStream input = null;
 	    static JSONObject theObject = null;
 	    static String stringey = "";
 
 	public ParseTheJSON() {
 		// Nothing to see here, move along
 	}
 	
 	public JSONObject gettheJSONLogin(String user, String pass) {
 		 //make post request
         try {
             DefaultHttpClient httpClient = new DefaultHttpClient();
 	        String postURL = "http://www.newsblur.com/api/login";
             HttpPost post = new HttpPost(postURL);
             List<NameValuePair> params = new ArrayList<NameValuePair>();
             params.add(new BasicNameValuePair("username", user));
             params.add(new BasicNameValuePair("password", pass));
             UrlEncodedFormEntity ent = new UrlEncodedFormEntity(params,HTTP.UTF_8);
             post.setEntity(ent);
             HttpResponse resEntity = httpClient.execute(post);
             HttpEntity httpEntity = resEntity.getEntity();
            Log.i("RESPONSE",EntityUtils.toString(httpEntity));
            input = httpEntity.getContent();           
  
         } catch (UnsupportedEncodingException e) {
             e.printStackTrace();
         } catch (ClientProtocolException e) {
             e.printStackTrace();
         } catch (IOException e) {
             e.printStackTrace();
         }
  
         try {
             BufferedReader reader = new BufferedReader(new InputStreamReader(
                     input, "iso-8859-1"), 8);
             StringBuilder sb = new StringBuilder();
             String line = null;
             while ((line = reader.readLine()) != null) {
                 sb.append(line + "\n");
             }
             input.close();
             stringey = sb.toString();
         } catch (Exception e) {
             Log.e("Buffer Error", "Error converting: " + e.toString());
         }
  
         try {// makeJSON Object from input string (hopefully)
             theObject = new JSONObject(stringey);
         } catch (JSONException e) {
             Log.e("JSON Parser", "Error parsing: " + e.toString());
         }
  
         return theObject; // return JSON object
  
     }
 
 }
