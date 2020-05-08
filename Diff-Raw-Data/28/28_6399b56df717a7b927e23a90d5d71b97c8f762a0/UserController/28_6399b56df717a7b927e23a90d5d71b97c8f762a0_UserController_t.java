 package org.walkingarchive.backend.controller;
 
 import javax.servlet.ServletContext;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.ws.rs.Consumes;
 import javax.ws.rs.GET;
 import javax.ws.rs.PUT;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.Response.Status;
 import javax.ws.rs.core.UriInfo;
 
 import org.codehaus.jettison.json.JSONException;
 import org.codehaus.jettison.json.JSONObject;
 import org.walkingarchive.backend.model.security.SecurityDAO;
 import org.walkingarchive.backend.model.security.User;
 
 @Path("/user/")
 public class UserController {
     @Context
     protected ServletContext context;
     @Context
     protected HttpServletRequest request;
     @Context
     protected HttpServletResponse response;
     @Context
     protected UriInfo uriInfo;
 
     @GET
     public Response getAll() {
         return Response.status(Status.FORBIDDEN).build();
     }
 
     @GET
     @Path("id/{userId}")
     @Produces(MediaType.APPLICATION_JSON)
     public Response getUserById(@PathParam("userId") String userId) throws JSONException {
         //TODO - validate input
         Response result;
         User user = SecurityDAO.getInstance().getUserById(Integer.parseInt(userId));
         if(user != null) {
            result = Response.ok(user, MediaType.APPLICATION_JSON).build();
         }
         else {
             result = Response.status(Status.NOT_FOUND).build();
         }
         
         return result;
     }
 
     @GET
     @Path("name/{name}")
     @Produces(MediaType.APPLICATION_JSON)
     public Response getUserByName(@PathParam("name") String name) throws JSONException {
         //TODO - validate input
         Response result;
         User user = SecurityDAO.getInstance().getUserByName(name);
         
         if(user != null) {
            result = Response.ok(user, MediaType.APPLICATION_JSON).build();
         }
         else {
             result = Response.status(Status.NOT_FOUND).build();
         }
         
         return result;
     }
     
     @GET
     @Path("email/{email}")
     @Produces(MediaType.APPLICATION_JSON)
     public JSONObject getUserByEmail(@PathParam("email") String email) throws JSONException {
         //TODO - validate input
         throw new RuntimeException("getUserByEmail is unimplemented.");
     }
     
     @PUT
     @Path("add")
     @Consumes(MediaType.APPLICATION_JSON)
     public Response addUser(String json) throws Exception {
         JSONObject jsonObject = new JSONObject(json);
         User user = new User(jsonObject.get("name").toString(), 
                 jsonObject.get("password").toString(), 
                 jsonObject.get("email").toString());
         
         user = SecurityDAO.getInstance().createUser(user);
         
         return Response.ok(user, MediaType.APPLICATION_JSON).build();
     }
 }
