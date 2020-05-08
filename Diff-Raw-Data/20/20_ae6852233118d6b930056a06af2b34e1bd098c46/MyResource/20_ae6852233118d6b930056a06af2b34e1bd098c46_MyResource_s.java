 
 package JerseyRest;
 
 import javax.ws.rs.*;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentMap;
 
 /** Example resource class hosted at the URI path "/v2/user/someuser"
  */
 @Path("v2/user/{name}")
 public class MyResource {
     private static ConcurrentMap<String,String> users;
 
     static {
         users = new ConcurrentHashMap<String, String>();
     }
     /** Method processing HTTP GET requests, producing "text/plain" MIME media
      * type.
      * @return String that will be send back as a response of type "text/plain".
      */
     @GET
    @Produces("text/plain")
     public String getIt(@PathParam("name") String name) {
         if (users.containsKey(name)) {
             return "Hi there, " + name;
         } else {
            return "INvalid user!";
         }
     }
 
     @PUT
    @Produces("text/plain")
     public String createIt(@PathParam("name") String name) {
         String v = users.putIfAbsent(name,"");
        return v.equalsIgnoreCase("")? "user added successfully!": "User already exists";
     }
 
     @DELETE
    @Produces("text/plain")
     public String delete(@PathParam("name") String name) {
         boolean removed = users.remove(name, "");
         return removed? "User deleted": "User not found";
     }
 }
