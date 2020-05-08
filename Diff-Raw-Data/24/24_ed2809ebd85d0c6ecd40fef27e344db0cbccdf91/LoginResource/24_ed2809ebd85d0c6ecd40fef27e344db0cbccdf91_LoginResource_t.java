 package se.diversify.jamit.resources;
 
 import se.diversify.jamit.domain.User;
 import se.diversify.jamit.repository.UserDao;
 import se.diversify.jamit.util.EncryptionUtils;
 import se.diversify.jamit.util.JsonUtils;
 
 import javax.ws.rs.*;
 
 /**
  * REST service definitions of path /login
  */
@Path("/login/")
 public class LoginResource {
 
     private UserDao dao = UserDao.defaultDao();
 
     /**
      * Login using the POST REST service. The request should include following arguments in
      * application/x-www-form-urlencoded format.
      *
      * @param email    the user's email address
      * @param password the user's password
      * @return the user, represented in JSON format, if the login went ok, or a not ok message, also in JSON format, if anything went wrong.
      */
     @POST
     @Consumes("application/x-www-form-urlencoded")
     @Produces("application/json")
     public String login(@FormParam("email") String email, @FormParam("password") String password) {
         String result = "";
         if (EncryptionUtils.isPasswordOk(email, password)) {
             User user = dao.getByEmail(email);
             result = JsonUtils.toJson(user);
         } else {
             result = JsonUtils.notOk();
         }
         return result;
     }
 }
