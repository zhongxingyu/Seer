 package com.lollapaloozer.auth.client;
 
 import android.accounts.Account;
 import android.accounts.AccountManager;
 import android.accounts.AuthenticatorDescription;
 import android.content.Intent;
 
 import com.facebook.android.Facebook;
 import com.ratethisfest.shared.Constants;
 
 public class AuthDemoModel {
 
   private GoogleAuthProvider _authProviderGoogle;
   private FacebookAuthProvider _authProviderFacebook;
   private AuthChooseAccountActivity _activity;
 
   private String _verifiedAccountName = null;
   private TwitterAuthProvider _authProviderTwitter;
 
   public AuthDemoModel(AuthChooseAccountActivity activity) {
     _activity = activity;
     _authProviderGoogle = new GoogleAuthProvider(_activity);
     _authProviderFacebook = new FacebookAuthProvider(_activity);
     _authProviderTwitter = new TwitterAuthProvider(_activity);
   }
 
   public boolean isLoggedIn() {
     int numLogins = 0;
     if (_authProviderGoogle.isLoggedIn()) {
       numLogins++;
     }
 
     if (_authProviderFacebook.isLoggedIn()) {
       numLogins++;
     }
 
     if (_authProviderTwitter.isLoggedIn()) {
       numLogins++;
     }
 
     if (numLogins > 1) {
       System.out.println("WARNING: Multiple concurrent login types detected");
     }
 
     if (numLogins > 0) {
       return true;
     } else {
       return false;
     }
   }
 
   public AuthProvider getCurrentAuthProvider() {
     if (_authProviderGoogle.isLoggedIn()) {
       return _authProviderGoogle;
     }
 
     if (_authProviderFacebook.isLoggedIn()) {
       return _authProviderFacebook;
     }
 
     if (_authProviderTwitter.isLoggedIn()) {
       return _authProviderTwitter;
     }
 
     return null;
   }
 
   public int getCurrentAuthProviderType() {
     if (_authProviderGoogle.isLoggedIn()) {
       return Constants.LOGIN_TYPE_GOOGLE;
     }
 
     if (_authProviderFacebook.isLoggedIn()) {
       return Constants.LOGIN_TYPE_FACEBOOK;
     }
 
     if (_authProviderTwitter.isLoggedIn()) {
       return Constants.LOGIN_TYPE_TWITTER;
     }
 
     return 0;
 
   }
 
   public void checkAccounts() {
     AccountManager aMgr = AccountManager.get(_activity);
 
     System.out.println("Warning: The following requires permission GET_ACCOUNTS");
     for (Account a : aMgr.getAccounts()) {
       System.out.println(a.name + " " + a.type + " " + a.toString());
     }
 
     System.out.println("Installed Authenticators: ");
     for (AuthenticatorDescription d : aMgr.getAuthenticatorTypes()) {
       System.out.print(d.type);
       System.out.print("/ ");
     }
     System.out.println();
   }
 
   public Facebook getFacebookObject() {
     return _authProviderFacebook.getFacebookObject();
   }
 
   public void invalidateTokens() {
     _authProviderGoogle.logout();
     _authProviderFacebook.logout();
     _authProviderTwitter.logout();
   }
 
   public void loginToGoogle() {
     _authProviderGoogle.login();
   }
 
   public void loginToFacebook() {
     _authProviderFacebook.login();
   }
 
   public void loginToTwitter() {
     _authProviderTwitter.login();
   }
 
   public void twitterAuthCallback(int requestCode, int resultCode, Intent data) {
     _authProviderTwitter.requestTokenCallback(requestCode, resultCode, data);
   }
 
 }
