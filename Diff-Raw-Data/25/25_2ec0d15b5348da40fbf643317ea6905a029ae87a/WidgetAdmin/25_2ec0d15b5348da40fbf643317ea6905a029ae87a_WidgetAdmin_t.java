 /*******************************************************************************
  * Copyright (c) 2011 GigaSpaces Technologies Ltd. All rights reserved
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *       http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *******************************************************************************/
 package controllers;
 
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.regex.Pattern;
 
import beans.ServerNodesPoolStats;
 import data.validation.GsConstraints;
 import models.ServerNode;
 import models.Summary;
 import models.User;
 import models.Widget;
 import models.WidgetInstance;
 import org.apache.commons.lang.math.NumberUtils;
 import org.apache.commons.lang3.StringUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import play.data.validation.Constraints;
 import play.libs.Json;
 import play.mvc.Controller;
 import play.mvc.Http;
 import play.mvc.Result;
 import server.ApplicationContext;
 import server.HeaderMessage;
 import server.exceptions.ServerException;
 import utils.CollectionUtils;
 import utils.Utils;
 import utils.RestUtils;
 
 import static utils.RestUtils.*;
 
 import views.html.common.linkExpired;
 import views.html.widgets.*;
 import views.html.widgets.admin.*;
 import views.html.widgets.dashboard.*;
 
 
 /**
  * Widget Admin controller.
  * 
  * @author Igor Goldenberg
  */
 public class WidgetAdmin extends Controller
 {
 
     private static Logger logger = LoggerFactory.getLogger( WidgetAdmin.class );
 
     public static Result getWidget( String apiKey ){
         Widget widgetItem = Widget.getWidget(apiKey);
         if ( widgetItem == null || !widgetItem.isEnabled()){
             return ok();
         }
         return ok(widget.render(ApplicationContext.get().conf().mixpanelApiKey, widgetItem));
     }
 	/*
 	 * Creates new account.
 	 */
 	public static Result signUp( String email, String passwordConfirmation, String password, String firstname, String lastname )
 	{
         try {
             Constraints.EmailValidator ev =  new Constraints.EmailValidator();
             if ( StringUtils.isEmpty( email ) || !ev.isValid( email ) ){
                 new HeaderMessage().setError( "Email is incorrect" ).apply( response().getHeaders() );
                 return internalServerError(  );
             }
             if ( !validatePassword( password, passwordConfirmation, email ) ) {
                 return internalServerError();
             }
 
             User.Session session = User.newUser( firstname, lastname, email, password ).getSession();
             return RestUtils.resultAsJson( session );
         } catch ( ServerException ex ) {
             return resultErrorAsJson( ex.getMessage() );
         }
     }
 
     public static Result logout(){
         session().clear();
         response().discardCookies( "authToken" );
         return redirect( routes.WidgetAdmin.index() );
     }
 
     public static Result index()
     {
         // lets assume that if we have "authToken" we are already logged in
         // and we can redirect to widgets.html
         Http.Cookie authToken = request().cookies().get( "authToken" );
         if ( authToken != null && User.validateAuthToken( authToken.value(), true ) != null ) {
             return redirect( routes.WidgetAdmin.getWidgetsPage() );
         }
         else{
             return redirect( routes.WidgetAdmin.getSigninPage( null ) );
         }
     }
 
     /**
      *
      * this method will reset the user's password.
      * the parameters are cryptic on purpose.
      *
      * @param p - the hmac
      * @param pi - the user id
      * @return -
      */
     public static Result resetPasswordAction( String p, Long pi ){
         User user = User.findById( pi );
         // validate p
         if ( !ApplicationContext.get().getHmac().compare( p, user.getEmail(),  user.getId(), user.getPassword()  )){
             return badRequest(  linkExpired.render() );
         }
         // if p is valid lets reset the password
         String newPasswordStr = StringUtils.substring( p, 0, 7 );
         user.encryptAndSetPassword( newPasswordStr );
         user.save();
         return ok( newPassword.render( newPasswordStr ) );
     }
 
     public static Result postResetPassword( String email, String h ){
         logger.info( "user {} requested password reset", email );
         if ( !StringUtils.isEmpty( h ) ){
             return badRequest(  ); // this is a bot.. lets block it.
         }
 
         if ( StringUtils.isEmpty( email ) || !(new Constraints.EmailValidator().isValid( email )) ){
             new HeaderMessage().setError( "Invalid email" ).apply( response().getHeaders() );
             return badRequest(  );
         }
 
         User user = User.find.where(  ).eq( "email",email ).findUnique();
         if ( user == null ){
             return ok(  ); // do not notify if user does not exist. this is a security breach..
             // simply reply that an email was sent to the address.
         }
 
         ApplicationContext.get().getMailSender().resetPasswordMail( user );
         return ok(  );
     }
 
 
     public static Result getAccountPage(){
         return ok( account.render() );
     }
 
     public static Result getWidgetsPage(){
         return ok( widgets.render() );
     }
     public static Result getSigninPage( String message ){
         return ok( signin.render( message ));
     }
 
     public static Result getSignupPage(){
         return ok( signup.render() );
     }
     public static Result getResetPasswordPage(){
         return ok( resetPassword.render() );
     }
 
 
     public static Result checkPasswordStrength( String password, String email ){
         if ( !StringUtils.isEmpty( email  ) && new Constraints.EmailValidator().isValid( email )){
             String result = isPasswordStrongEnough( password, email );
             if ( result != null ){
                 new HeaderMessage().setError( result ).apply( response().getHeaders() );
                 return internalServerError(  );
             }
             return ok(  );
         }
         return ok(  );
     }
 
     private static String isPasswordStrongEnough( String password, String email ){
         if ( StringUtils.length( password ) < 8 ){
             return "Password is too short";
         }
         if ( !Pattern.matches( "(?=^.{8,}$)((?=.*\\d)|(?=.*\\W+))(?![.\\n])(?=.*[A-Z])(?=.*[a-z]).*$", password ) && !StringUtils.containsIgnoreCase( email, password ) ){
             return "Password must match requirements";
         }
 
         Set<String> strSet = new HashSet<String>(  );
         for ( String s : password.split( "" ) ) {
             if ( StringUtils.length( s ) > 0){
                 strSet.add( s.toLowerCase( ) );
             }
         }
 
         if ( CollectionUtils.size( strSet ) < 3 ){
             return "Too many repeating letters";
         }
 
         if ( StringUtils.getLevenshteinDistance( password, email.split( "@" )[0] ) < 5 || StringUtils.getLevenshteinDistance( password, email.split( "@" )[1] ) < 5 ){
             return "Password similar to email";
         }
 
         return null;
     }
 
     public static Result getPasswordMatch( String authToken, String password ){
         User user = User.validateAuthToken( authToken );
         String passwordWeakReason = isPasswordStrongEnough( password, user.getEmail() );
         if ( passwordWeakReason == null ){
             return ok( );
         }
         return ok( passwordWeakReason );
     }
 
     /**
      *
      * @param newPassword - the password user chose
      * @param confirmPassword - the confirmed password
      * @param email - user's email. used for checking similarity to password. passwords that are similar to email are considered weak.
      * @return true iff password is considered strong enough according to our policy.
      */
     private static boolean validatePassword( String newPassword, String confirmPassword, String email )
     {
         if ( !StringUtils.equals( newPassword, confirmPassword ) ) {
             new HeaderMessage().setError( "Passwords do not match" ).apply( response().getHeaders() );
             return false;
         }
 
         String passwordWeakReason = isPasswordStrongEnough( newPassword, email );
         if ( passwordWeakReason != null ) {
             new HeaderMessage().setError( passwordWeakReason ).apply( response().getHeaders() );
             return false;
         }
         return true;
     }
     public static Result postChangePassword( String authToken, String oldPassword, String newPassword, String confirmPassword ){
         User user = User.validateAuthToken( authToken );
         if ( !user.comparePassword( oldPassword )){
             new HeaderMessage().setError( "Wrong Password" ).apply( response().getHeaders() );
             return internalServerError();
         }
 
         if ( !validatePassword( newPassword, confirmPassword, user.getEmail() ) ){
             return internalServerError(  );
         }
 
 
         user.encryptAndSetPassword( newPassword );
         user.save();
         new HeaderMessage().setSuccess( "Password Changed Successfully" ).apply( response().getHeaders() );
         return ok(  );
     }
 
 	/**
 	 * Login with existing account.
 	 * 
 	 * @param email
 	 * @param password
 	 * @return
 	 */
 	public static Result signIn( String email, String password )
 	{
 		try
 		{
 			User.Session session = User.authenticate(email, password);
 
 			return resultAsJson(session);
 		}catch( ServerException ex )
 		{
 			return resultErrorAsJson(ex.getMessage());
 		}
 	}
 
 	
 	public static Result getAllUsers( String authToken )
 	{
 		User.validateAuthToken(authToken);   // TODO : remove these validations and use "action interceptor"
                                             // there's no official documentation for interceptors. see code sample at : http://stackoverflow.com/questions/9629250/how-to-avoid-passing-parameters-everywhere-in-play2
 		List<User> users = User.getAllUsers();
 
 		return resultAsJson(users);
 	}
 
 
 	public static Result createNewWidget( String widgetId, String authToken,  String productName, String productVersion,
 										  String title, String youtubeVideoUrl, String providerURL,
 										  String recipeURL, String consolename, String consoleurl, String rootpath )
 	{
         User user = User.validateAuthToken(authToken);
         Widget widget = null;
         if ( !NumberUtils.isNumber( widgetId ) ){
 		    widget = user.createNewWidget( productName, productVersion, title, youtubeVideoUrl, providerURL, recipeURL, consolename, consoleurl, rootpath );
         }else{
             Long widgetIdLong = Long.parseLong( widgetId );
             widget = Widget.findByUserAndId( user, widgetIdLong );
             if ( widget == null ){
                 new HeaderMessage().setError( "User is not allowed to edit this widget" ).apply( response().getHeaders() );
                 return badRequest(  );
             }
             widget.setProductName( productName );
             widget.setProductVersion( productVersion );
             widget.setTitle( title );
             widget.setYoutubeVideoUrl( youtubeVideoUrl );
             widget.setProviderURL( providerURL );
             widget.setRecipeURL( recipeURL );
             widget.setConsoleName( consolename );
             widget.setConsoleURL( consoleurl );
             widget.setRecipeRootPath( rootpath );
             widget.save(  );
         }
 
         logger.info( "edited widget : " + widget.toString() );
         return ok( Json.toJson(widget) );
 //		return resultAsJson(widget);
 	}
 
 	
 	public static Result getAllWidgets( String authToken )
 	{
 		User user = User.validateAuthToken(authToken);
 		List<Widget> list = null;
 
         if ( user.getSession().isAdmin() )   {
             list = Widget.find.all(); // Utils.workaround( Widget.find.all() );
 //            list = Utils.workaround( Widget.find.all() );
         }
         else {
             list = user.getWidgets();
         }
 
         return ok( Json.toJson(list) );
 	}
 	
 	
 	public static Result getAllServers()
 	{
 		List<ServerNode> list = ServerNode.find.all();
 
 		return resultAsJson(list);
 	}
 
 	
 	public static Result shutdownInstance( String authToken, String instanceId )
 	{
 		User.validateAuthToken(authToken);
 		ApplicationContext.get().getWidgetServer().undeploy(instanceId); // todo : link to user somehow
 		return ok(OK_STATUS).as("application/json");
 	}
 
 
 	public static Result disableWidget( String authToken, String apiKey )
 	{
         return enableDisableWidget( authToken, apiKey, false );
 	}
 
     private static Result enableDisableWidget( String authToken, String apiKey, boolean enabled )
     {
         getWidgetSafely( authToken, apiKey ).setEnabled( enabled );
         return ok(OK_STATUS).as("application/json");
     }
 
 
     public static Result enableWidget( String authToken, String apiKey )
 	{
         return enableDisableWidget( authToken, apiKey, true );
 	}
 
     private static Widget getWidgetSafely( String authToken, String apiKey )
     {
 
         User user = User.validateAuthToken(authToken);
         if ( user.isAdmin() ){
              return Widget.getWidget( apiKey );
         }else{
             return Widget.getWidgetByApiKey( user, apiKey );
         }
     }
 
 
     public static Result summary( String authToken )
 	{
 		User user = User.validateAuthToken(authToken);
 
 		Summary summary = new Summary();
 
 		// only for admin users, we return summary information
 		if ( user.isAdmin() )
 		{		
 			int totalUsers = User.find.findRowCount();
 			int totalWidgets = Widget.find.findRowCount();


            // find only widget instances deployed on my cloud.
            int localInstances = WidgetInstance.find.where().eq( "serverNode.remote", false ).findRowCount();



            summary.addAttribute( "Users", String.valueOf( totalUsers ) );
 			summary.addAttribute("Widgets", String.valueOf( totalWidgets ));
			summary.addAttribute("Instances", String.valueOf( localInstances ));


            ServerNodesPoolStats stats = ApplicationContext.get().getServerPool().getStats();
            summary.addAttribute("Idle Servers", String.valueOf( stats.nonBusyServers ));
			summary.addAttribute("Busy Servers", String.valueOf( stats.busyServers ));
 		}
 
 
 		return resultAsJson(summary);
 	}
 
     public static Result deleteWidget( String authToken, String apiKey ){
         Widget widget = getWidgetSafely( authToken, apiKey );
         widget.delete(  );
         return ok( );
     }
 
     public static Result previewWidget( String apiKey ){
         String authToken = request().cookies().get("authToken").value();
         Widget widget = getWidgetSafely( authToken, apiKey );
         return ok( previewWidget.render(widget, request().host()));
     }
 	
 	public static Result regenerateWidgetApiKey( String authToken, String apiKey )
 	{
         User user = User.validateAuthToken( authToken );
         Widget widget = Widget.regenerateApiKey(user, apiKey);
 		return resultAsJson(widget);
 	}
 
 	public static Result headers()
 	{
     	Http.Request req = Http.Context.current().request();
     	
     	StringBuilder sb = new StringBuilder("HEADERS:");
     	sb.append( "\nRemote address: " ).append( req.remoteAddress() );
     	
     	Map<String, String[]> headerMap = req.headers();
     	for (String headerKey : headerMap.keySet()) 
     	{
     	    for( String s : headerMap.get(headerKey) )
     	    	sb.append( "\n" ).append( headerKey ).append( "=" ).append( s );
     	}
 
     	return ok(sb.toString());
 	}
 
     public static Result postRequireLogin( String authToken, Long widgetId, boolean requireLogin,  String loginVerificationUrl, String webServiceKey ){
 
         User user = User.validateAuthToken( authToken );
         Widget widget = Widget.findByUserAndId( user, widgetId );
         if ( widget == null ){
             new HeaderMessage().setError(" User is not allowed to edit this widget ").apply(response().getHeaders());
             return badRequest();
         }
         GsConstraints.UrlValidator validator = new GsConstraints.UrlValidator();
         if ( !validator.isValid(loginVerificationUrl) ){
             new HeaderMessage().addFormError("loginVerificationUrl", "invalid value").apply(response().getHeaders());
             return badRequest();
         }
 
         widget.setRequireLogin( requireLogin );
         widget.setLoginVerificationUrl( loginVerificationUrl );
         widget.setWebServiceKey( webServiceKey );
         widget.save();
         return ok();
     }
 }
