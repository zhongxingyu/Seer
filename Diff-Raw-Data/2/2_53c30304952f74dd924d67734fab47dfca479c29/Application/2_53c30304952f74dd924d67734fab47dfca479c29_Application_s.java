 package controllers;
 
 import com.google.common.base.Preconditions;
 import com.google.gson.Gson;
 import fi.foyt.foursquare.api.FoursquareApi;
 import fi.foyt.foursquare.api.FoursquareApiException;
 import models.foursquare.Checkin;
 import models.foursquare.FoursquareResponse;
 import models.siena.ConnectedUser;
 import play.libs.WS;
 import play.mvc.Controller;
 import util.FoursquareConfiguration;
 import util.URLHelper;
 
 import java.io.UnsupportedEncodingException;
 import java.net.URLEncoder;
 
 public class Application extends Controller {
 
     public static void index() {
         render();
     }
 
     public static void connected() {
         render();
     }
 
     /**
      * First part of the foursquare authentification & authorization
      */
     public static void authenticate(){
 
         FoursquareApi foursquareApi = new FoursquareApi(FoursquareConfiguration.getInstance().getClientId(), FoursquareConfiguration.getInstance().getClientSecret(), FoursquareConfiguration.getInstance().getRedirectUrl());
         redirect(foursquareApi.getAuthenticationUrl());
 
     }
 
     /**
      * Handle the foursquare's callback
       * @param code Authentication code to confirm
      */
     public static void handleCallback(String code) {
 
         Preconditions.checkNotNull(code, "Code must not be null", "code");
         Preconditions.checkArgument(code.trim().length() > 0, "Parameter '%s' must not be empty", "code");
 
         FoursquareApi foursquareApi = new FoursquareApi(FoursquareConfiguration.getInstance().getClientId(), FoursquareConfiguration.getInstance().getClientSecret(), FoursquareConfiguration.getInstance().getRedirectUrl());
 
         try {
 
             foursquareApi.authenticateCode(code);
             String oauthToken = foursquareApi.getOAuthToken();
 
             String replyUrl = URLHelper.buildUserSelfUrl(oauthToken);
 
             //get the current user through an active user call to foursquare
             String replyAnswer = WS.url(replyUrl).get().getString();
 
             FoursquareResponse foursquareResponse = new Gson().fromJson(replyAnswer, FoursquareResponse.class);
 
             ConnectedUser user = ConnectedUser.findByUserId(foursquareResponse.response.user.id);
 
             if(user == null ){
                 user = new ConnectedUser(foursquareResponse.response.user.id, oauthToken);
                 user.save();
             }else{
                 user.token = oauthToken; //replace the token for the given user
                 user.update();
             }
             
             // Cache will be used soon
 
 
             connected();
 
         } catch (FoursquareApiException e) {
             e.printStackTrace();
             // TODO: Error handling
         }
     }
 
 
     public static void psh(String checkin, String secret){
 
         play.Logger.info("Receiving a push from Foursquare");
 
         Checkin pushed = new Gson().fromJson(checkin, Checkin.class);
 
         //this is to catch a test push for the foursquare developer website
         //remove it when you have tested it.
         if(pushed.user.id == 1){
             play.Logger.info("It is a test push from foursquare");
             return;
         }
 
         //This is what will be in the "Connected Apps" section of the checkin. Up
         String reply = "Here is your reply";
 
         // Starting the database part -----------------------------------
         // Cache will be used soon
         ConnectedUser user = ConnectedUser.findByUserId(pushed.user.id);
 
         //Of course you need to encode your reply
         try {
             reply = URLEncoder.encode(reply, "UTF-8");
         } catch (UnsupportedEncodingException e) {
             //TODO: error handling but for a simple encoding it should be easy
         }
 
        String replyUrl = URLHelper.buildReplyUrl(push.id, reply, user.token);
 
         //send the reply to the user
         WS.url(replyUrl).post().getString();
 
         return;
 
     }
 
 }
