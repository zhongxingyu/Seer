 package com.js.mobile;
 
 import android.util.Base64;
 import android.util.Log;
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 import org.json.JSONArray;
 import org.json.JSONObject;
 
 import java.io.BufferedReader;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Created by IntelliJ IDEA.
  * User: JohnGyselinck
  * Date: 1/17/12
  * Time: 10:00 PM
  * To change this template use File | Settings | File Templates.
  */
 public class DataHandler {
     private static String baseUrl = "http://10.0.2.2/mobilechallenger/";
     
     public static boolean doLogin(String username, String password){
         String result = null;
         HttpPost request = null;
 
         try{
             String url = baseUrl + "loginService.php";
             request = new HttpPost(url);
 //            request.setURI(new URI(url));
             List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
             nameValuePairs.add(new BasicNameValuePair("user", username));
             nameValuePairs.add(new BasicNameValuePair("pass", password));
             request.setEntity(new UrlEncodedFormEntity(nameValuePairs));
         } catch (Exception ex){
             ex.printStackTrace();
         }
 
         result = doPostRequest(request);
 
         if(result.equalsIgnoreCase("TRUE")){
             return true;
         } else {
             return false;
         }
 
     }
 
     public static boolean doRegistration(String username, String password, String fname, String lname, String email){
         String result = null;
         HttpPost request = null;
 
         try{
             String url = baseUrl + "registerUser.php";
             request = new HttpPost(url);
             request.setURI(new URI(url));
             List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
             nameValuePairs.add(new BasicNameValuePair("user", username));
             nameValuePairs.add(new BasicNameValuePair("pass", password));
             nameValuePairs.add(new BasicNameValuePair("email", email));
             nameValuePairs.add(new BasicNameValuePair("fname", fname));
            nameValuePairs.add(new BasicNameValuePair("pass", lname));
             request.setEntity(new UrlEncodedFormEntity(nameValuePairs));
             result = doPostRequest(request);
             return true;
         } catch (Exception ex){
             ex.printStackTrace();
             return false;
         }
     }
     
     private static String doPostRequest(HttpPost request){
         StringBuilder sb = new StringBuilder();
         try{
             HttpClient client = new DefaultHttpClient();
 
             HttpResponse response = client.execute(request);
             HttpEntity entity = response.getEntity();
             InputStream is = entity.getContent();
 
             //convert response to string
             BufferedReader reader = new BufferedReader(new InputStreamReader(is, "iso-8859-1"));
 
             String line = null;
             while((line = reader.readLine()) != null){
                 sb.append(line + "\n");
             }
             is.close();
         } catch (Exception e){
             e.printStackTrace();
         }
 
         return (sb.toString()).replace("\n", "");
     }
 }
