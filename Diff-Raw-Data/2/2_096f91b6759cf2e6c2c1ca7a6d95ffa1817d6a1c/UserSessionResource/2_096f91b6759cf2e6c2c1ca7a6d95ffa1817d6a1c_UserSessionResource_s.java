 package org.triple_brain.service.resources;
 
 import org.codehaus.jettison.json.JSONException;
 import org.codehaus.jettison.json.JSONObject;
 import org.triple_brain.module.model.User;
 import org.triple_brain.module.model.json.UserJsonFields;
 import org.triple_brain.module.repository.user.NonExistingUserException;
 import org.triple_brain.module.repository.user.UserRepository;
 
 import javax.annotation.security.PermitAll;
 import javax.inject.Inject;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpSession;
 import javax.ws.rs.*;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 
 import static org.triple_brain.service.SecurityInterceptor.AUTHENTICATED_USER_KEY;
 import static org.triple_brain.service.SecurityInterceptor.AUTHENTICATION_ATTRIBUTE_KEY;
 import static org.triple_brain.service.resources.GraphManipulatorResourceUtils.isUserInSession;
 import static org.triple_brain.service.resources.GraphManipulatorResourceUtils.userFromSession;
 
 /*
 * Copyright Mozilla Public License 1.1
 */
 @Produces(MediaType.APPLICATION_JSON)
 @Consumes(MediaType.APPLICATION_JSON)
 @PermitAll
 public class UserSessionResource {
 
     @Inject
     UserRepository userRepository;
 
     @GET
     @Path("/")
     public Response get(
         @Context HttpServletRequest request
     ) {
         if(!isUserInSession(request.getSession())){
             throw new WebApplicationException(Response.Status.FORBIDDEN);
         }
         return Response.ok(
                 UserJsonFields.toJson(
                         userFromSession(request.getSession())
                 )
         ).build();
     }
 
     @POST
    @Produces(MediaType.TEXT_PLAIN)
     @Path("/")
     public Response authenticate(
             JSONObject loginInfo,
             @Context HttpServletRequest request
     ){
         try {
             User user = userRepository.findByEmail(
                     loginInfo.getString(UserJsonFields.EMAIL)
             );
             if (user.hasPassword(
                     loginInfo.getString(UserJsonFields.PASSWORD)
             )) {
                 authenticateUserInSession(user, request.getSession());
                 return Response.ok(
                         UserJsonFields.toJson(user)
                 ).build();
             }
         } catch (NonExistingUserException e) {
             return Response.status(401).build();
         } catch(JSONException e){
             throw new RuntimeException(e);
         }
         return Response.status(401).build();
     }
 
     @DELETE
     @Produces(MediaType.TEXT_PLAIN)
     @Path("/")
     public Response logout(@Context HttpServletRequest request){
         request.getSession().setAttribute(AUTHENTICATION_ATTRIBUTE_KEY, false);
         request.getSession().setAttribute(AUTHENTICATED_USER_KEY, null);
         return Response.ok().build();
     }
 
     public static void authenticateUserInSession(User user, HttpSession session){
         session.setAttribute(AUTHENTICATION_ATTRIBUTE_KEY, true);
         session.setAttribute(AUTHENTICATED_USER_KEY, user);
     }
 }
