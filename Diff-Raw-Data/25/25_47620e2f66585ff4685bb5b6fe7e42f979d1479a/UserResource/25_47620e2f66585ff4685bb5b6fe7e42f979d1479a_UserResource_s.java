 package com.tendril.cleanweb.server.resource;
 
 import com.tendril.cleanweb.client.TendrilClient;
 import com.tendril.cleanweb.domain.User;
 import com.tendril.cleanweb.domain.tendril.TendrilLocation;
 import com.tendril.cleanweb.domain.tendril.TendrilUser;
 
 import javax.inject.Inject;
 import javax.ws.rs.GET;
 import javax.ws.rs.HeaderParam;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 
 /**
  */
 @Path("/user")
 @Produces("application/json")
 public class UserResource {
 
     private TendrilClient client;
 
     @Inject
     public void UserResource(TendrilClient client) {
         this.client = client;
     }
 
     @GET
     public User getUser(@HeaderParam("custom-auth") String auth) throws Exception {
         String authValues[] = auth.split(":");
         String username = authValues[0];
         String password = authValues[1];
 
         TendrilUser tendrilUser = client.getUser(username, password);
         TendrilLocation tendrilLocation = client.getLocation(username, password);
 
        return new User(tendrilUser.getFirstName(), tendrilUser.getLastName(), tendrilUser.getUsername(), tendrilLocation.getPostalCode());
     }
 
     @GET
     @Path("basic")
     public String foo(@HeaderParam("custom-auth") String customAuth) {
         return customAuth;
     }
 
 
     @GET
     @Path("/test")
     public String getUserTest() {
         return "test";
     }
 }
