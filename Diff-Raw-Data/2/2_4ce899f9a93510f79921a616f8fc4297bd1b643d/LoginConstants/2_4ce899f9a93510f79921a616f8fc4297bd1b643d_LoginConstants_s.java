 package org.aksw.verilinks.games.peaInvasion.shared;
 
 public class LoginConstants {
 
 //Google authoriastion
   public static final String GOOGLE_AUTH_URL = "https://accounts.google.com/o/oauth2/auth";
 
   // This app's personal client ID assigned by the Google APIs Console
   // (http://code.google.com/apis/console).
   // Key for deployment
  public static final String GOOGLE_CLIENT_ID = "23285137063-2i3e7dk79kueejush34uhcqcor0i20mg.apps.googleusercontent.com";
   //Offline Test on localhost use this Facebook Key
 //  public static final String GOOGLE_CLIENT_ID = "23285137063.apps.googleusercontent.com";
   
   
   // The auth scope being requested. This scope will allow the application to
   // identify who the authenticated user is.
   public static final String PROFILE_SCOPE = "https://www.googleapis.com/auth/userinfo.profile";
 
   // Facebook
   public static final String FACEBOOK_AUTH_URL = "https://www.facebook.com/dialog/oauth";
 
   // This app's personal client ID assigned by the Facebook Developer App
   // (http://www.facebook.com/developers).
   // Key for deployment
   public static final String FACEBOOK_CLIENT_ID = "183676705103647";
   // Offline Test on localhost use this Facebook Key
 //  public static final String FACEBOOK_CLIENT_ID = "495198197169715";
   
   // All available scopes are listed here:
   // http://developers.facebook.com/docs/authentication/permissions/
   // This scope allows the app to access the user's email address.
   public static final String FACEBOOK_EMAIL_SCOPE = "email";
 
   // This scope allows the app to access the user's birthday.
   public static final String FACEBOOK_BIRTHDAY_SCOPE = "user_birthday";
 
   //This scope allows the app to access the user's birthday.
   public static final String FACEBOOK_FRIENDLIST_SCOPE = "read_friendlists";
 
 	
 	
 }
