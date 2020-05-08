 package com.haligali.PowerHangmanClient.Networking;
 
 import android.os.AsyncTask;
 import android.util.Log;
 import com.haligali.PowerHangmanClient.Networking.ServerResponseCodes.LogInCode;
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
     private static final int EXPECTED_SIGNIN_INPUT_LENGTH = 2;
 
     private OnLoginResponse _callbackMethods;
 
     @Override
     public void onPreExecute()
     {
         //
     }
 
     @Override
     public String doInBackground(String... params)
     {
         // We only expect 'username' params[0] and 'password' params[1]
         Assert.assertEquals(params.length, EXPECTED_SIGNIN_INPUT_LENGTH);
 
         final String uriLogin = Operations.loginURI(params[0], params[1]);
         final StringBuilder serverResponse = new StringBuilder();
         final HttpClient httpClient = new DefaultHttpClient();
         final HttpGet loginRequest = new HttpGet(uriLogin);
 
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
         String resp_PlayerId = "";
        LogInCode returnCode = LogInCode.LOG_IN_DENIED;
 
         try
         {
             json = new JSONObject(resultFromServer);
             if (json.has("pid"))
             {
                 resp_PlayerId = json.getString("pid");
                returnCode = LogInCode.LOG_IN_SUCCESSFUL;
                 Log.d("LogInTag", "PID received: " + resp_PlayerId);
             }
             else
             {
                 Log.d("LogInTag", "pid was not found.");
             }
         } catch (JSONException e)
         {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
 
         // Send the response to the calling activity
        _callbackMethods.ReceiveLoginResponse(returnCode, resp_PlayerId);
     }
 }
