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
 
 import models.ServerNode;
 import models.Summary;
 import models.User;
 import models.Widget;
 import models.WidgetInstance;
 import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
 
 
 /**
  * Widget Admin controller.
  * 
  * @author Igor Goldenberg
  */
 public class WidgetAdmin extends Controller
 {
 	/**
 	 * Creates new account.
 	 * 
 	 * @param email
 	 * @param password
 	 * @return
 	 */
 	public static Result signUp( String email, String password, String firstname, String lastname )
 	{
 		try
 		{
 			User.Session session = User.newUser( firstname, lastname, email, password).getSession();
 			return RestUtils.resultAsJson( session );
 		}catch( ServerException ex )
 		{
 			return resultErrorAsJson(ex.getMessage());
 		}
 	}
 
     public static Result logout(){
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
             return badRequest(  views.html.common.linkExpired.render() );
         }
         // if p is valid lets reset the password
         String newPassword = StringUtils.substring( p, 0, 7 );
         user.encryptAndSetPassword( newPassword );
         user.save();
         return ok( views.html.widgets.admin.newPassword.render( newPassword ) );
     }
 
     public static Result postResetPassword( String email, String h ){
         if ( !StringUtils.isEmpty( h ) ){
             return badRequest(  ); // this is a bot.. lets block it.
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
         return ok( views.html.widgets.dashboard.account.render() );
     }
 
     public static Result getWidgetsPage(){
         return ok( views.html.widgets.dashboard.widgets.render() );
     }
     public static Result getSigninPage( String message ){
         return ok(views.html.widgets.admin.signin.render( message ));
     }
 
     public static Result getSignupPage(){
         return ok( views.html.widgets.admin.signup.render() );
     }
     public static Result getResetPasswordPage(){
         return ok( views.html.widgets.admin.resetPassword.render() );
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
 
         if ( StringUtils.getLevenshteinDistance( password, email.replaceAll( "@","" ).replaceAll( "\\.","" ) ) > 5 ){
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
 
     public static Result postChangePassword( String authToken, String oldPassword, String newPassword, String confirmPassword ){
         User user = User.validateAuthToken( authToken );
         if ( !user.comparePassword( oldPassword )){
             new HeaderMessage().setError( "Wrong Password" ).apply( response().getHeaders() );
             return internalServerError();
         }
 
         if ( !StringUtils.equals( newPassword, confirmPassword )){
             new HeaderMessage().setError( "Passwords do not match" ).apply( response().getHeaders() );
             response().getHeaders().put("message", "Passwords do not match");
             return internalServerError(  );
         }
 
         String passwordWeakReason = isPasswordStrongEnough( newPassword, user.getEmail() );
         if ( passwordWeakReason != null ){
                     new HeaderMessage().setError( passwordWeakReason ).apply( response().getHeaders() );
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
 
 
 	public static Result createNewWidget( String authToken,  String productName, String productVersion,
 										  String title, String youtubeVideoUrl, String providerURL,
 										  String recipeURL, String consolename, String consoleurl, String rootpath )
 	{
 		User user = User.validateAuthToken(authToken);
 		Widget widget = user.createNewWidget( productName, productVersion, title, youtubeVideoUrl, providerURL, recipeURL, consolename, consoleurl, rootpath );
 		
 		return resultAsJson(widget);
 	}
 
 	
 	public static Result getAllWidgets( String authToken )
 	{
 		User user = User.validateAuthToken(authToken);
 		List<Widget> list = null;
 
         if ( user.getSession().isAdmin() )   {
             list = Utils.workaround( Widget.find.all() );
         }
         else {
             list = Utils.workaround( user.getWidgets() );
         }
 
         return resultAsJson(list);
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
         return Widget.getWidgetByApiKey( user, apiKey );
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
 			int totalInstances = WidgetInstance.find.findRowCount();
 			int totalIdleServers = ServerNode.find.where().eq("busy", "false").findRowCount();
 			int totalBusyServers = ServerNode.find.where().eq("busy", "true").findRowCount();
 	
 			summary.addAttribute("Users", String.valueOf( totalUsers ));
 			summary.addAttribute("Widgets", String.valueOf( totalWidgets ));
 			summary.addAttribute("Instances", String.valueOf( totalInstances ));
 			summary.addAttribute("Idle Servers", String.valueOf( totalIdleServers ));
 			summary.addAttribute("Busy Servers", String.valueOf( totalBusyServers ));
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
        return ok(views.html.widgets.dashboard.previewWidget.render(widget, request().host()));
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
 }
