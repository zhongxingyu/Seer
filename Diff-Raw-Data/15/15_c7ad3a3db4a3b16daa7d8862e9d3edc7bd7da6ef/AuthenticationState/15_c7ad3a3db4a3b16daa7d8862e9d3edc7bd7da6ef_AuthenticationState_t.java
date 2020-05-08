 package com.tabbie.android.radar.remote;
 
 import android.app.Activity;
 import android.content.SharedPreferences;
 import android.os.Handler;
 import android.util.Pair;
 
 import com.facebook.android.Facebook;
 import com.tabbie.android.radar.core.BasicCallback;
 import com.tabbie.android.radar.core.facebook.FacebookAuthenticator;
 import com.tabbie.android.radar.core.facebook.FacebookUserRemoteResource;
 
 /**
  *  AuthenticationState.java
  * 
  *  Created on: October 21, 2012
  *      Author: Valeri Karpov
  * 
  *  High level interface for handling our authentication state, linking
  *  together all of our "auth" things
  * 
  */
 
 public class AuthenticationState {
   private FacebookAuthenticator facebookAuthenticator;
   private FacebookUserRemoteResource facebookUserRemoteResource;
   private TonightLifeAuthenticator tonightLifeAuthenticator;
   
   private int authCount;
   
   public AuthenticationState() {
     authCount = 0;
   }
   
   public void init(Facebook facebook, SharedPreferences preferences) {
     this.facebookAuthenticator = new FacebookAuthenticator(facebook, preferences);
     this.facebookUserRemoteResource = new FacebookUserRemoteResource(preferences);
     this.tonightLifeAuthenticator = new TonightLifeAuthenticator(preferences);
     
     // Load all stored states so we can tell if we're actually "logged in" or not
     this.facebookAuthenticator.loadStoredState();
     this.facebookUserRemoteResource.loadStoredState();
     this.tonightLifeAuthenticator.loadStoredState();
   }
   
   public String getFacebookAccessToken() {
     return this.facebookAuthenticator.getFacebookAccessToken();
   }
   
   public String getFacebookShortName() {
     return this.facebookUserRemoteResource.getFacebookName();
   }
   
   public String getTonightLifeToken() {
     return this.tonightLifeAuthenticator.getTonightLifeToken();
   }
   
   public String getGCMKey() {
     return this.tonightLifeAuthenticator.getGCMKey();
   }
   
   /* Daisy chain together all of our authentications, with extra callbacks for the UI
    */
   public void doFullLoginChain( final Activity parent,
                                 final Handler serverCallHandler,
                                 final BasicCallback<String> facebookAuthCallback,
                                 final BasicCallback<String> facebookUserCallback,
                                 final BasicCallback<Pair<String, String> > tonightLifeAuthCallback) {
     
     authCount = 0;
     facebookAuthenticator.authenticate(parent, new BasicCallback<String>() {
       @Override
       public void onDone(String response) {
         ++authCount;
         facebookAuthCallback.onDone(response);
         facebookUserRemoteResource.load(serverCallHandler, response, new BasicCallback<String>() {
           @Override
           public void onDone(String response) {
             ++authCount;
             facebookUserCallback.onDone(response);
             tonightLifeAuthenticator.authenticate(serverCallHandler,
                 facebookAuthenticator.getFacebookAccessToken(), new BasicCallback<Pair<String,String>>() {
               @Override
               public void onDone(Pair<String, String> response) {
                 ++authCount;
                 tonightLifeAuthCallback.onDone(response);
               }
 
               @Override
               public void onFail(String reason) {
                 tonightLifeAuthCallback.onFail(reason);
               }
             });
           }
 
           @Override
           public void onFail(String reason) {
             facebookUserCallback.onFail(reason);
           }
         });
       }
 
       @Override
       public void onFail(String reason) {
         facebookAuthCallback.onFail(reason);
       }
     });
   }
   
   public boolean isAuthenticated() {
    return facebookAuthenticator.isValidSession()
        && facebookUserRemoteResource.isValid() && tonightLifeAuthenticator.isValidSession();
   }
   
 }
