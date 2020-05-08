 package com.haligali.PowerHangmanClient.Networking;
 
 import android.os.AsyncTask;
 import android.util.Log;
import com.haligali.PowerHangmanClient.Operations;
 import junit.framework.Assert;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Paul
  * Date: 10/8/13
  * Time: 8:47 PM
  * To change this template use File | Settings | File Templates.
  */
 public class LoginTask extends AsyncTask<String, Void, String>
 {
     // SERVER IP ADDRESS
     // TODO: there needs to be a better place for this
     // do a  ifconfig -a   on your mac to find the local address of your mac on WiFi or ethernet
    private static final String IPADDR = "192.168.1.240:8080";
 
     private static final int EXPECTED_SIGNIN_INPUT_LENGTH = 2;
 
     @Override
     public void onPreExecute()
     {
         // Do things.
     }
 
     @Override
     public String doInBackground(String... params)
     {
         // We only expect 'username' params[0] and 'password' params[1]
         Assert.assertEquals(params.length, EXPECTED_SIGNIN_INPUT_LENGTH);
 
         final StringBuilder serverResponse = new StringBuilder();
         final HttpClient httpClient = new DefaultHttpClient();
        final HttpGet loginRequest = new HttpGet("http://" + IPADDR + "/phm/login?nn=" + params[0] + "&pw=" + params[1]);
 
         try
         {
             final HttpResponse response = httpClient.execute(loginRequest);
             // TODO: maybe use response.getStatusLine() to check for successful request
             final BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
             String line;
             while ((line = reader.readLine()) != null)
             {
                 serverResponse.append(line);
             }
 
         } catch (IOException e)
         {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
 
         return serverResponse.toString();
     }
 
     @Override
     public void onPostExecute(final String resultFromServer)
     {
         JSONObject json = null;
         try
         {
             json = new JSONObject(resultFromServer);
             if (json.has("pid"))
             {
                 Log.d("LogInTag", "PID received: " + json.getString("pid"));
             }
             else
             {
                 Log.d("LogInTag", "pid was not found.");
             }
         } catch (JSONException e)
         {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
     }
 }
