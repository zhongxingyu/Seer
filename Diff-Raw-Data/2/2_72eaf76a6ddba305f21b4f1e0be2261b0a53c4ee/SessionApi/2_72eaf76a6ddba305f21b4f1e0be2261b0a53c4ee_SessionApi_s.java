 /*
  * This file is part of anycook. The new internet cookbook
  * Copyright (C) 2014 Jan Graßegger
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program. If not, see [http://www.gnu.org/licenses/].
  */
 
 package de.anycook.api;
 
 import com.fasterxml.jackson.annotation.JsonView;
 import de.anycook.api.util.MediaType;
 import de.anycook.db.mysql.DBMailProvider;
 import de.anycook.db.mysql.DBUser;
 import de.anycook.mailprovider.MailProvider;
 import de.anycook.session.LoginAttempt;
 import de.anycook.session.Session;
 import de.anycook.user.User;
 import de.anycook.user.views.Views;
 import org.apache.log4j.Logger;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.ws.rs.*;
 import javax.ws.rs.core.*;
 import javax.ws.rs.core.Response.ResponseBuilder;
 import java.io.IOException;
 import java.sql.SQLException;
 import java.util.Map;
 
 
 @Path("session")
 public class SessionApi {
 	
 	private final Logger logger;
     private final String cookieDomain;
 
 	
 	public SessionApi() {
 		logger = Logger.getLogger(getClass());
        cookieDomain = "."+de.anycook.conf.Configuration.getPropertyCookieDomain();
 	}
 	
 	@GET
 	@Produces(MediaType.APPLICATION_JSON)
     @JsonView(Views.PrivateUserView.class)
 	public User getSession(@Context HttpHeaders hh,
 			@Context HttpServletRequest request){
 		Session session = Session.init(request.getSession(true));
         try {
             session.checkLogin(hh.getCookies());
             return session.getUser();
         } catch (IOException e) {
             logger.error(e, e);
             throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
         }
 	}
 	
 	@POST
     @Consumes(MediaType.APPLICATION_JSON)
 	@Produces(MediaType.APPLICATION_JSON)
 	public Response login(@Context HttpServletRequest request,
 			Session.UserAuth auth){
 		
 		Session session = Session.init(request.getSession(true));
         LoginAttempt loginAttempt = null;
 
         try{
             int userId = User.getUserId(auth.username);
             if(!LoginAttempt.isLoginAllowed(userId)) {
                 logger.warn("too many login attempts for "+userId);
                 throw new WebApplicationException(Response.Status.FORBIDDEN);
             }
 
             loginAttempt = new LoginAttempt(userId, request.getRemoteAddr(), System.currentTimeMillis());
 
 
             session.login(userId, auth.password);
             loginAttempt.setSuccessful(true);
             User user = session.getUser();
             ResponseBuilder response = Response.ok(user);
 
             if(auth.stayLoggedIn){
                 logger.debug(String.format("stayLoggedIn"));
                 NewCookie cookie = new NewCookie("anycook", session.makePermanentCookieId(user.getId()), "/", cookieDomain,
                         "", 7 * 24 * 60 * 60, false);
                 response.cookie(cookie);
             }
 
             return response.build();
         }catch(User.LoginException|DBUser.UserNotFoundException e){
             throw new WebApplicationException(Response.Status.FORBIDDEN);
         } catch (IOException | SQLException e) {
             logger.error(e);
             throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
         } finally {
             if(loginAttempt != null) try {
                 loginAttempt.save();
             } catch (SQLException e) {
                 logger.error(e);
             }
         }
     }
 
     @POST
     @Path("facebook")
     @Consumes(MediaType.APPLICATION_JSON)
     @Produces(MediaType.APPLICATION_JSON)
     public void facebookLogin(@Context HttpServletRequest request, String signedRequest){
         Session session = Session.init(request.getSession(true));
         try {
             session.facebookLogin(signedRequest);
         } catch (IOException | SQLException e) {
             logger.error(e, e);
             throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
         } catch (User.LoginException|DBUser.UserNotFoundException e) {
             logger.warn(e);
             throw new WebApplicationException(Response.Status.FORBIDDEN);
         }
     }
 	
 	@DELETE
 	@Produces(MediaType.APPLICATION_JSON)
 	public Response logout(@Context HttpHeaders hh,
 			@Context HttpServletRequest request){
 		Session session = Session.init(request.getSession());
 		Map<String, Cookie> cookies = hh.getCookies();
         try {
             session.checkLogin(hh.getCookies());
         } catch (IOException e) {
             logger.error(e, e);
             throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
         }
 
         ResponseBuilder response = Response.ok();
 		if(cookies.containsKey("anycook")){
 			Cookie cookie = cookies.get("anycook");
             try {
                 session.deleteCookieID(cookie.getValue());
             } catch (SQLException e) {
                 logger.error(e);
                 throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
             }
             NewCookie newCookie = new NewCookie(cookie, "", -1, false);
 			response.cookie(newCookie);
 		}
 		session.logout();
 		return response.entity("true").build();
 	}
 	
 	@POST
 	@Path("activate")
 	@Produces(MediaType.APPLICATION_JSON)
 	public void activateAccount(@FormParam("activationkey") String activationKey){
         try {
             User.activateById(activationKey);
         } catch (SQLException e) {
             logger.error(e);
             throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
         } catch (DBUser.ActivationFailedException e) {
             logger.warn(e,e);
             throw new WebApplicationException(Response.Status.BAD_REQUEST);
         }
     }
 	
     //mail provider
 	@GET
 	@Path("mailprovider")
 	@Produces(MediaType.APPLICATION_JSON)
 	public MailProvider checkMailAnbieter(@QueryParam("domain") String domain){
 		if(domain == null) 
 			throw new WebApplicationException(401);
         try {
             return MailProvider.getMailanbieterfromDomain(domain);
         } catch (SQLException e) {
             logger.error(e);
             throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
         } catch (DBMailProvider.ProviderNotFoundException e) {
             logger.debug(e);
             throw new WebApplicationException(Response.Status.NO_CONTENT);
         }
 
 
 	}
 
     @POST
     @Path("resetPassword")
     @Consumes(MediaType.APPLICATION_JSON)
     public void resetPasswordRequest(String mail){
         try {
             User.createResetPasswordID(mail);
         } catch (SQLException | IOException e) {
             logger.error(e, e);
             throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
         } catch (DBUser.UserNotFoundException e) {
             logger.info(e);
             throw new WebApplicationException(Response.Status.BAD_REQUEST);
         }
     }
 
     @PUT
     @Path("resetPassword")
     @Consumes(MediaType.APPLICATION_JSON)
     public void resetPassword(PasswordReset passwordReset){
         try {
             User.resetPassword(passwordReset.id, passwordReset.newPassword);
         } catch (SQLException e) {
             logger.error(e, e);
             throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
         } catch (User.ResetPasswordException e) {
             logger.warn(e);
             throw new WebApplicationException(Response.Status.BAD_REQUEST);
         }
     }
 
     @GET
     @Path("id")
     @Produces(MediaType.TEXT_PLAIN)
     public String getSessionId(@Context HttpServletRequest request) {
         return request.getSession(true).getId();
     }
 
     public static class PasswordReset{
         public String id;
         public String newPassword;
     }
 }
