 /*
  * File: Account.java
  * Name: Peter Graham
  * Class: CS 461
  * Project 1
  * Date: April 17
  */
 package controllers;
 
 import models.UserModel;
 import play.i18n.Messages;
 import play.libs.Crypto;
 import utilities.AllowGuest;
 import utilities.Constants;
 import utilities.DenyUser;
 import utilities.ValidationException;
 
 /**
  * Controller for handling of user login/logout/registration and user settings
  * operations.
  *
  * @author Peter Graham
  */
// allow guest for logout since it doesn't make sense to prompt login for this
@AllowGuest({"login","logout", "register", "onLoginSubmit", "onRegisterSubmit"})
 @DenyUser({"login", "register", "onLoginSubmit", "onRegisterSubmit"})
 public class Account extends BaseController {
 
     /**
      * Render the login form.
      */
     public static void login() {
         // save this original URL to redirect to it on successful login
         flash.keep(Constants.ORIGINAL_URL);
         render();
     }
 
     /**
      * Render the registration form.
      */
     public static void register() {
         render();
     }
 
     /**
      * Render the settings form.
      */
     public static void settings() {
         render();
     }
 
     /**
      * Validate login information and login user if valid.
      *
      * @param email the submitted email address
      * @param password the submitted password
      * @param remember true if "Remember me" cookie is checked
      */
     public static void onLoginSubmit(String email, String password,
             boolean remember) {
         try {
             getUserModel().validateLogin(email, password);
             if(remember) {
                 // create a "Remember me" cookie
                 response.setCookie(Constants.REMEMBER_ME,
                         Crypto.sign(email) + "-" + email, "30d");
             }
             // log user in and redirect them to the original URL
             UserModel user = getUserModel().findByEmail(email);
             session.put(Constants.SESSION_KEY, user.getId());
             redirect(getOriginalUrl());
         }
         catch(ValidationException e) {
             // store error to show next and redirect back to login form
             flash.error(e.getMessage());
             params.flash();
             flash.keep(Constants.ORIGINAL_URL);
             login();
         }
     }
 
     /**
      * Validate registration information and create an account if valid.
      *
      * @param email the submitted email address
      * @param password the submitted password
      */
     public static void onRegisterSubmit(String email, String password) {
         try {
             // create user, log them in, and redirect to topics
             getUserModel().createUser(email, password);
             UserModel user = getUserModel().findByEmail(email);
             session.put(Constants.SESSION_KEY, user.getId());
             Topic.defaultFilters();
         }
         catch(ValidationException e) {
             // store error to show next and redirect back to registration form
             flash.error(e.getMessage());
             params.flash();
             register();
         }
     }
 
     /**
      * Modify user's settings.
      *
      * @param email String representing the user's email
      * @param oldpass String representing the user's old password
      * @param newpass String representing the user's new password
      */
     public static void onSettingsSubmit(String email, String oldpass,
             String newpass) {
         try {
             // modify settings and display a success message
             getUser().modifySettings(email, oldpass, newpass);
             flash.success(Messages.get("action.saved"));
         }
         catch(ValidationException e) {
             flash.error(e.getMessage());
         }
         settings();
     }
 
     /**
      * Logout and return to homepage. Delete "Remember me" cookie if it exists.
      */
     public static void logout() {
         session.clear();
         response.removeCookie(Constants.REMEMBER_ME);
         Topic.defaultFilters();
     }
 }
