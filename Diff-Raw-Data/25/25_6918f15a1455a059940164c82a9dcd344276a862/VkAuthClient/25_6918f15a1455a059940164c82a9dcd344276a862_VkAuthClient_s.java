 package com.expelabs.social.webclient;
 
 import android.graphics.Bitmap;
 import android.webkit.WebView;
 import android.webkit.WebViewClient;
 import com.expelabs.tips.app.DailyTipsApp;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Вадим
  * Date: 24.05.13
  * Time: 15:11
  * To change this template use File | Settings | File Templates.
  */
 public class VkAuthClient extends WebViewClient implements AuthClientInterface {
 
     private String clientId;
     private String scope;
     private AuthListener listener;
 
     public VkAuthClient(String clientId, String scope, AuthListener listener) {
         this.listener = listener;
         this.clientId = clientId;
         this.scope = scope;
     }
 
     @Override
     public String loginUrl() {
         return (VK_AUTH_URI + "\n" +
                " client_id=" + clientId  + "&\n" +
                " scope=" + scope + "&\n" +
                " redirect_uri=" + VK_REDIRECT_URI + "&\n" +
                " display=" + VK_DISPLAY + "& \n" +
                " response_type=" + VK_RESPONSE_TYPE);
     }
 
     static final String VK_AUTH_URI="https://oauth.vk.com/authorize?";
   public static final String VK_REDIRECT_URI="http://api.vkontakte.ru/blank.html";
     static final String VK_DISPLAY="touch";
     static final String VK_RESPONSE_TYPE="token";
 
     @Override
     public boolean shouldOverrideUrlLoading(WebView view, String url) {
         if (url.startsWith(VK_REDIRECT_URI) & (!url.contains("error"))) {
             listener.onSuccess(url);
             return true;
         } else if (url.contains("error")) {
             listener.onError();
             return false;
         } else {
             view.loadUrl(url);
             return true;
         }
     }
 
     @Override
     public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
         super.onReceivedError(view, errorCode, description, failingUrl);
         listener.onError();
     }
 
     @Override
     public void onPageStarted(WebView view, String url, Bitmap favicon) {
         super.onPageStarted(view, url, favicon);
         if (url.contains("error")) {
             listener.onError();
             return;
         } else if (url.contains("access_token")) {
             listener.onSuccess(url);
             return;
         }
     }
 
 }
