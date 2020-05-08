 package net.bbm485.main;
 
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.UriInfo;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Consumes;
 import javax.ws.rs.PUT;
 import javax.ws.rs.Path;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.Produces;
 
 import net.bbm485.db.DBManager;
 import net.bbm485.db.User;
 import org.codehaus.jettison.json.JSONObject;
 import com.google.gson.*;
 import net.bbm485.exceptions.UserNotFoundException;
 import org.codehaus.jettison.json.JSONException;
 
 @Path("users")
 public class UserManager {
     @Context
     private UriInfo context;
     private final static String dbName = "restful_db";
     private final static String collectionName = "users";
     private DBManager db;
     private Gson gson;
     public UserManager() {
         db = new DBManager(dbName, collectionName);
         gson = new GsonBuilder().serializeNulls().disableHtmlEscaping().create();
     }
 
     @GET
     @Produces("text/plain")
     public String getText() {
         //TODO return proper representation object
         return "bok";
     }
 
     @POST
     @Produces("application/json; charset=UTF-8")
     @Consumes("application/json; charset=UTF-8")
     public String createUser(String content) {
         User newUser = null;
         try {
             JSONObject userObj = new JSONObject(content).getJSONObject("user");
             newUser = (User) gson.fromJson(userObj.toString(), User.class);
             newUser.checkData();
             db.createUser(newUser);
             return getSuccessfulCreateUserMsg();
         }
         catch (Exception e) {
             return e.getMessage();
         }
     }
     
     @GET
     @Produces("application/json; charset=UTF-8")
     @Consumes("application/json; charset=UTF-8")
     @Path("/{userId}")
     public String showUser(@PathParam("userId") String  userId) {
         JSONObject result = new JSONObject();
         try {
             User user = db.getUser(userId);
             // TODO : make 200 final static
             result.put("meta", (new JSONObject()).put("code", 200));
             result.put("data", user.toJson());
            return result.toString();
         }
         catch (UserNotFoundException e) {
             return e.getMessage();
         }
         catch (JSONException e) {
             return e.getMessage();
         }
     }
     
     
     
     private String getSuccessfulCreateUserMsg() {
         JSONObject msg = new JSONObject();
         try {
             msg.put("meta", (new JSONObject()).put("code", 200));
             msg.put("data", (new JSONObject()).put("message", "You successfully created a user."));
         }
         catch (JSONException e) {
         }
         return msg.toString();
     }
 }
