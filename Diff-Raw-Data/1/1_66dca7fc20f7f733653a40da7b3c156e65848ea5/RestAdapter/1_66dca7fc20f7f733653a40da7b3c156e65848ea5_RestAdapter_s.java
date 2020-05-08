 package com.profbingo.android.webdata;
 
 import java.io.IOException;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.http.NameValuePair;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.ResponseHandler;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.BasicResponseHandler;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.content.res.Resources;
 import android.net.Uri;
 import android.net.Uri.Builder;
 import android.util.Log;
 
 import com.profbingo.android.R;
 import com.profbingo.android.model.Category;
 import com.profbingo.android.model.GameBoard;
 import com.profbingo.android.model.Professor;
import com.profbingo.android.model.School;
 
 public class RestAdapter implements WebDataAdapter {
 
     public static final String FAILURE = "FAILURE";
     public static final String ERROR = "ERROR";
 
     private static final String TAG = "ProfBingo.RestAdapter";
 
     private Resources mResources;
     private String mAuthCode;
 
     public RestAdapter(Resources resources) {
         mResources = resources;
     }
 
     private String hashSHA1(String str) {
         MessageDigest digest;
 
         try {
             digest = java.security.MessageDigest.getInstance("SHA-1");
         } catch (NoSuchAlgorithmException e) {
             Log.e(TAG, "Error hashing credentials: " + e.getMessage());
             return ERROR;
         }
 
         digest.update(str.getBytes());
         byte messageDigest[] = digest.digest();
 
         StringBuffer hexString = new StringBuffer();
 
         for (int i = 0; i < messageDigest.length; i++) {
             String h = Integer.toHexString(0xFF & messageDigest[i]);
             while (h.length() < 2)
                 h = "0" + h;
             hexString.append(h);
         }
         return hexString.toString();
     }
 
     private JSONObject postJSONData(Map<String, Object> data, String route) {
         String protocol = mResources.getString(R.string.url_protocol);
         String domain = mResources.getString(R.string.url_domain);
 
         Uri.Builder builder = new Builder().scheme(protocol).authority(domain).path(route);
 
         Log.d(TAG, "Posting to URL: " + builder.build().toString());
         Log.d(TAG, "Data: " + new JSONObject(data).toString());
 
         // Create a new HttpClient and Post Header
         HttpClient httpclient = new DefaultHttpClient();
         HttpPost post = new HttpPost(builder.build().toString());
 
         try {
             String jsonString = new JSONObject(data).toString();
 
             // Build Post Object
             List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
             nameValuePairs.add(new BasicNameValuePair("data", jsonString));
             post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
 
             // Execute HTTP Post Request and return
             ResponseHandler<String> responseHandler = new BasicResponseHandler();
             String responseBody = httpclient.execute(post, responseHandler);
             Log.d(TAG, "Response: " + new JSONObject(responseBody).getJSONObject("data"));
             return new JSONObject(responseBody).getJSONObject("data");
 
         } catch (ClientProtocolException e) {
             Log.e(TAG, "Error in JSON: " + e.getMessage());
             return new JSONObject();
         } catch (IOException e) {
             Log.e(TAG, "Error in JSON: " + e.getMessage());
             return new JSONObject();
         } catch (JSONException e) {
             Log.e(TAG, "Error in JSON: " + e.getMessage());
             return new JSONObject();
         }
     }
 
     private boolean isAuthValid(JSONObject json) {
         String resultParam = mResources.getString(R.string.json_result_result);
         String success = mResources.getString(R.string.json_success);
         try {
             return json.getString(resultParam).equals(success);
         } catch (JSONException e) {
             Log.e(TAG, "Error parsing JSON data: " + e.getMessage());
             return false;
         }
     }
 
     public String login(String email, String password) {
         String hashed = hashSHA1(password + email);
         if (hashed == ERROR) return ERROR;
 
         Map<String, Object> postData = new HashMap<String, Object>();
 
         postData.put(mResources.getString(R.string.json_param_email), email);
         postData.put(mResources.getString(R.string.json_param_password), hashed);
 
         JSONObject result = postJSONData(postData, mResources.getString(R.string.url_route_login));
 
         if (!isAuthValid(result)) {
             Log.i(TAG, "Bad login for " + email);
             return FAILURE;
         }
 
         try {
             mAuthCode = result.getString(mResources.getString(R.string.json_result_authcode));
             return mAuthCode;
         } catch (JSONException e) {
             Log.d(TAG, "Error parsing JSON data: " + e.getMessage());
             return ERROR;
         }
     }
 
     public boolean login(String authCode) {
         mAuthCode = authCode;
         return isLoggedIn();
     }
     
     public boolean register(String email, String firstName, String lastName, String password) {
         throw new UnsupportedOperationException("Not implemented");
     }
 
     public boolean logout() {
         throw new UnsupportedOperationException("Not implemented");
     }
 
     public boolean isLoggedIn() {
         Map<String, Object> postData = new HashMap<String, Object>();
 
         postData.put(mResources.getString(R.string.json_param_authcode), mAuthCode);
 
         JSONObject result = postJSONData(postData, mResources.getString(R.string.url_route_status));
         boolean res = isAuthValid(result);
         Log.d(TAG, "Login status: " + (res ? "logged in" : "not logged in"));
         return isAuthValid(result);
     }
 
     public List<Category> getCategories() {
         throw new UnsupportedOperationException("Not implemented");
     }
 
     public List<Professor> getProfessors() {
         List<Professor> result = new ArrayList<Professor>();
         
         result.add(new Professor(1, "Sriram Mohan"));
         result.add(new Professor(2, "Curt Clifton"));
         result.add(new Professor(3, "Matt Boutell"));
         result.add(new Professor(4, "Steve Chenoweth"));
         result.add(new Professor(5, "Claude Anderson"));
         
         return result;
     }
 
     public GameBoard getNewBoard(Professor professor) {
         throw new UnsupportedOperationException("Not implemented");
     }
 
 
 
 }
