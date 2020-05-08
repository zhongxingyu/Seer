 package com.tabbie.android.radar.core.facebook;
 
 /**
  *  FacebookUserRemoteResource.java
  * 
  *  Created on: September 15, 2012
  *      Author: Valeri Karpov
  * 
  *  Keeps track of basic Facebook user data using shared preferences, allowing
  *  us to avoid having to load it every time.
  */
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.content.SharedPreferences;
 import android.os.Handler;
 import android.os.Message;
 
 import com.tabbie.android.radar.core.BasicCallback;
 import com.tabbie.android.radar.enums.MessageType;
 import com.tabbie.android.radar.http.ServerRequest;
 import com.tabbie.android.radar.http.ServerResponse;
 
 public class FacebookUserRemoteResource implements Handler.Callback {
   private final SharedPreferences preferences;
   
   private String facebookName = "";
   private long expires = 0;
   
   private boolean loadStarted = false;
   private BasicCallback<String> fbNameCallback = null;
   
   public FacebookUserRemoteResource(SharedPreferences preferences) {
     this.preferences = preferences;
   }
   
   public String getFacebookName() {
     return facebookName;
   }
   
   public void loadStoredState() {
     facebookName = preferences.getString("fb_name", "");
     expires = preferences.getLong("fb_name_expires", 0);
   }
   
   public boolean load(Handler handler, String fbToken, BasicCallback<String> fbNameCallback) {
     if (loadStarted) {
       return false;
     }
     loadStarted = true;
     this.fbNameCallback = fbNameCallback;
     
     loadStoredState();
     
    if (System.currentTimeMillis() < expires) {
       fbNameCallback.onDone(facebookName);
     } else {
     	ServerRequest req = new ServerRequest(MessageType.FACEBOOK_LOGIN, new Handler(this), fbToken);
       final Message message = Message.obtain();
       message.obj = req;
       handler.sendMessage(message);
     }
     return true;
   }
   
   @Override
   public boolean handleMessage(Message msg) {
     final ServerResponse resp = (ServerResponse) msg.obj;
     if (fbNameCallback == null) {
       return false;
     }
     if (resp.responseTo != MessageType.FACEBOOK_LOGIN) {
       fbNameCallback.onFail("Invalid message response!");
       return false;
     }
     
     final JSONObject json = resp.parseJsonContent();
     if (json.has("id")) {
       try {
         facebookName = json.getString("first_name") + " "
             + json.getString("last_name").substring(0, 1) + ".";
         
         expires = System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000);
         final SharedPreferences.Editor editor = preferences.edit();
         editor.putString("fb_name", facebookName);
         editor.putLong("fb_name_expires", expires);
         editor.commit();
         
         fbNameCallback.onDone(facebookName);
       } catch (final JSONException e) {
         fbNameCallback.onFail(e.toString());
         return false;
       }
       return true;
     } else {
       fbNameCallback.onFail("Facebook Log-in JSON does not have an ID!");
       return false;
     }
   }
 }
