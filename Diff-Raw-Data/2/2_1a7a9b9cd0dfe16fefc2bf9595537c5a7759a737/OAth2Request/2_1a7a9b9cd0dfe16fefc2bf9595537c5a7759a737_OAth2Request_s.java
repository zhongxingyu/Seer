 package com.artum.shootmaniacenter.utilities.oauth2;
 
 import android.app.Activity;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.webkit.WebView;
 import android.webkit.WebViewClient;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 import com.artum.shootmaniacenter.R;
 import com.artum.shootmaniacenter.global.Variables;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import java.util.Calendar;
 import java.util.HashMap;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * Created by artum on 07/07/13.
  */
 public class OAth2Request extends Activity {
 
     TextView alert;
     ImageView connectButton;
     WebView browser;
     String code = "";
     Boolean refresh;
 
     String api_username = "artum|appAccount";
     String api_secret = "app14185";
     String redirect_uri = "http://localhost:8080/ShootmaniaCenter";
 
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.oauth2_request);
         refresh = getIntent().getBooleanExtra("refresh", false);
 
 
 
             alert = (TextView)findViewById(R.id.alert_text);
             connectButton = (ImageView)findViewById(R.id.connect_button);
             browser = (WebView)findViewById(R.id.auth_browser);
             browser.setWebViewClient(new DontRedirect());
             browser.getSettings().setBuiltInZoomControls(true);
             browser.getSettings().setJavaScriptEnabled(true);
 
 
         if(!refresh)
         {
             connectButton.setOnClickListener(new View.OnClickListener() {
                 @Override
                 public void onClick(View view) {
                     RunOAth2();
                 }
             });
         }
         else
         {
             connectButton.setVisibility(View.INVISIBLE);
             alert.setVisibility(View.INVISIBLE);
             new RefreshToken().execute("");
         }
     }
 
     private void RunOAth2()
     {
         alert.setVisibility(View.INVISIBLE);
         connectButton.setVisibility(View.INVISIBLE);
         browser.setVisibility(View.VISIBLE);
 
         browser.loadUrl("https://ws.maniaplanet.com/oauth2/authorize/?client_id=" + api_username + "&redirect_uri=" + redirect_uri + "&scope=buddies teams dedicated titles offline&response_type=code");
     }
 
     private class DontRedirect extends WebViewClient {
         @Override
         public boolean shouldOverrideUrlLoading(WebView view, String url) {
             HashMap results = new HashMap();
                 Pattern pattern = Pattern.compile("[?&]([^&]+)=([^&]+)");
                 Matcher matcher = pattern.matcher(url);
                 while(matcher.find())
                 {
                     results.put(matcher.group(1), matcher.group(2));
                 }
                 if(code == "" && results.get("code") != null)
                 {
                     view.setVisibility(View.INVISIBLE);
                     code = (String)results.get("code");
                     alert.setVisibility(View.VISIBLE);
                     alert.setText("Getting token from Nadeo Servers...");
                     new GetToken().execute("");
                 }
                 else
                 {
                     view.loadUrl(url);
                 }
             return false;
         }
     }
 
     private class GetToken extends AsyncTask<String, Void, String>
     {
         @Override
         protected String doInBackground(String... strings) {
             return TokenManager.getTokenFromParams("https://ws.maniaplanet.com/oauth2/token/", "client_id="+ api_username +"&client_secret=" + api_secret +"&redirect_uri="+ redirect_uri + "&grant_type=authorization_code&code=" + code, false);
         }
 
         @Override
         protected void onPostExecute(String s) {
             try {
                 JSONObject object = new JSONObject(s);
                 if(object.getString("access_token") != null)
                 {
                     Variables.oauth2_token = object.getString("access_token");
                     Variables.oauth2_refresh_token = object.getString("refresh_token");
                     Variables.oauth2_username = object.getString("login");
                     Variables.oauth2_token_expires = Calendar.getInstance().getTime().getTime() + (object.getLong("expires_in") * 1000);
                     Variables.oauth2_token_expires_in = object.getLong("expires_in");
                     Variables.ForceSaveTokenData(OAth2Request.this);
                     finishTask();
                 }
                 else
                 {
                     alert.setText(object.getString("error"));
                 }
 
             } catch (JSONException e) {
 
                 alert.setText("Error parsing Response:\n" + s);
             }
         }
     }
 
     private class RefreshToken extends AsyncTask<String, Void, String>
     {
         @Override
         protected String doInBackground(String... strings) {
             return TokenManager.getTokenFromParams("https://ws.maniaplanet.com/oauth2/token/", "client_id=" + api_username + "&client_secret=" + api_secret + "grant_type=refresh_token&refresh_token=" + Variables.oauth2_refresh_token, true);
         }
 
         @Override
         protected void onPostExecute(String s) {
        Variables.oauth2_token_expires = Calendar.getInstance().getTime().getTime() + Variables.oauth2_token_expires_in;
         Variables.ForceSaveTokenData(OAth2Request.this);
         finishTask();
         }
     }
 
     private void finishTask()
     {
 
     }
 
 }
