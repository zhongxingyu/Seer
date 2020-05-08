 package com.photohack.bythepeople;
 
 import android.content.Context;
 import android.content.Intent;
 import android.content.pm.PackageManager;
 import android.content.pm.ResolveInfo;
 import android.util.Log;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.math.BigInteger;
 import java.security.SecureRandom;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import com.amazonaws.auth.BasicAWSCredentials;
 import com.amazonaws.services.s3.AmazonS3Client;
 import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
 import com.amazonaws.services.s3.model.PutObjectRequest;
 import com.amazonaws.services.s3.model.ObjectMetadata;
 
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.NameValuePair;
 import org.apache.http.message.BasicHeader;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.protocol.HTTP;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 public final class Utils {
 
   private static final String MY_ACCESS_KEY_ID = "AKIAIGE3LGCGLXS3XOAA";
  private static final String MY_SECRET_KEY = "6rzaO7NtGx2pBYwBswsBpfrT0es64c8ncew74qI";
   private static final String AWS_INCIDENT_UPLOAD_URL =
       "http://ec2-54-224-126-216.compute-1.amazonaws.com:8080/servicearchitecture/rest/incident/new";
   private static final String MY_BUCKET = "bythepublic";  // This bucket was manually created using AWS console.
   private static SecureRandom random = new SecureRandom();
 
   public static boolean isIntentAvailable(Context context, String action) {
     final PackageManager packageManager = context.getPackageManager();
     final Intent intent = new Intent(action);
     List<ResolveInfo> list = packageManager.queryIntentActivities(intent,
         PackageManager.MATCH_DEFAULT_ONLY);
     return list.size() > 0;
   }
 
   /**
     @return URL of the uploaded video in case of success and null otherwise.
     */
   public static String uploadVideo(String path) {
     // Reference: https://developer.amazon.com/post/Tx1E7YWEXPSDRNO/How-To-Use-the-Amazon-SDK-for-Android-to-Upload-Photos-to-Amazon-S3.html
     String videoName = new BigInteger(130, random).toString(32);
     AmazonS3Client s3Client = new AmazonS3Client(new BasicAWSCredentials(MY_ACCESS_KEY_ID, MY_SECRET_KEY));
     s3Client.putObject(MY_BUCKET, videoName, new File(path));
     GeneratePresignedUrlRequest urlRequest = new GeneratePresignedUrlRequest(MY_BUCKET, videoName);
     // Added 100 years worth of milliseconds to the current time.
     urlRequest.setExpiration(new Date(System.currentTimeMillis() + 100 * 365 * 24 * 3600 * 1000L));
     try {
       URL url = s3Client.generatePresignedUrl(urlRequest);
       return url.toURI().toString();
     } catch (URISyntaxException e) {
       e.printStackTrace();
       return null;
     }
   }
 
   public static boolean uploadReport(IncidentData incidentData) {
     /*
        A sample curl command which works.
        curl -H "Content-Type: application/json" -X POST -d '{"url": "google.com", "longitude": "1", "latitude": "2", "description": "testing"}' http://ec2-54-224-126-216.compute-1.amazonaws.com:8080/servicearchitecture/rest/incident/new
      */
     HttpClient httpClient = new DefaultHttpClient();
     HttpPost httppost = new HttpPost(AWS_INCIDENT_UPLOAD_URL);
     JSONObject json = new JSONObject();
     try {
       json.put("url", incidentData.getVideoUri());
       json.put("longitude", incidentData.getLongitude());
       json.put("latitude", "" + incidentData.getLatitude());
       json.put("description", incidentData.getNote());
       StringEntity se = new StringEntity(json.toString());  
       se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
       httppost.setEntity(se);
       HttpResponse response = httpClient.execute(httppost);
       /*Checking response */
       if(response==null){
         return false;
       }
       int statusCode = response.getStatusLine().getStatusCode();
       if (statusCode == 200) {
         return true;
       } else {
         Log.e(LaunchActivity.TAG, "Response code for incident upload is " + statusCode);
         return false;
       }
     } catch (ClientProtocolException e) {
       e.printStackTrace();
       return false;
     } catch (IOException e) {
       e.printStackTrace();
       return false;
     } catch (JSONException e) {
       e.printStackTrace();
       return false;
     }
   }
 }
