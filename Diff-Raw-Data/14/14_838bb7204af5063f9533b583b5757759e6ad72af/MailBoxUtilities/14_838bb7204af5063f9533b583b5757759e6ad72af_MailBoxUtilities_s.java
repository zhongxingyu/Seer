 package com.example.strangers.utilities;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.StatusLine;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 
 import android.content.Context;
 import android.util.Base64;
 
 import com.example.strangers.R;
 
 public class MailBoxUtilities {
 
 	public static Integer addMailBox(Context context, String currentUserLogin, String currentUserPassword, 
 												String host, String port, String login, String password, String description) {
 		
 		Integer status = null;
 		
 		String baseUrl = context.getString(R.string.service_base_url);
 		String registrationService = context.getString(R.string.new_account);
 		
 		HttpClient httpclient = new DefaultHttpClient();
         HttpPost httppost = new HttpPost(baseUrl+registrationService);
         
         //Add http basic auth
         String authParams = currentUserLogin+":"+currentUserPassword;
         httppost.setHeader("Authorization", "Basic "+Base64.encodeToString(authParams.getBytes(), Base64.NO_WRAP));
                 
         try {
             // Add your data
             List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
             nameValuePairs.add(new BasicNameValuePair("account[host]", host));
            //nameValuePairs.add(new BasicNameValuePair("port", port));
             nameValuePairs.add(new BasicNameValuePair("account[username]", login));
             nameValuePairs.add(new BasicNameValuePair("account[password]", password));
             //nameValuePairs.add(new BasicNameValuePair("description", description));
             httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
 
             // Execute HTTP Post Request
             HttpResponse response = httpclient.execute(httppost);
                      
             StatusLine statusLine = response.getStatusLine();
             status = statusLine.getStatusCode();
             
         } catch (ClientProtocolException e) {
 			//TODO complete exception
         } catch (IOException e) {
 			//TODO complete exception
         } catch (SecurityException e) {
         	
         }
         
         return status;
 	}
 }
