 package se.diversify.jamit.resources;
 
 import org.apache.commons.lang3.StringUtils;
 import se.diversify.jamit.domain.User;
 import se.diversify.jamit.repository.UserDao;
 import se.diversify.jamit.util.EncryptionUtils;
 import se.diversify.jamit.util.JsonUtils;
 
 import javax.ws.rs.*;
 
 /**
  * REST service for registering new users
  */
 @Path("/register")
 public class RegisterService {
 
     private UserDao dao = UserDao.defaultDao();
 
     /**
      * Register a new user using the POST REST service. The request should include following arguments in
      * application/x-www-form-urlencoded format
      *
      * @param name      the user's name
      * @param email     the user's email address
      * @param phone     the user's phone no
      * @param role      the user's role
      * @param password1 the user's password
      * @param password2 the user's password entered again
      * @return the new created user, in JSOn format, if the registration went well, or a not ok message, also in JSON
      *         format, if anything went wrong
      */
     @POST
     @Consumes("application/x-www-form-urlencoded")
     @Produces("application/json")
    public String register(@FormParam("name") String name, @FormParam("email") String email,
                            @FormParam("phone") String phone, @FormParam("role") String role, @FormParam("password1") String password1,
                            @FormParam("password2") String password2) {
         String result = "";
        if (!isPasswordsOk(password1, password2) || isFieldsMissing(new String[]{name, email, phone, role, password1, password2})) {
             result = JsonUtils.notOk();
         } else {
             User user = dao.add(new User(-1, name, email, phone, role, EncryptionUtils.encrypt(password1)));
             result = JsonUtils.toJson(user);
         }
         return result;
     }
 
     private boolean isPasswordsOk(String password1, String password2) {
         boolean ok = true;
         ok &= StringUtils.isNotBlank(password1);
         ok &= StringUtils.isNotBlank(password2);
         ok &= password1.equals(password2);
 
         return ok;
     }

    private boolean isFieldsMissing(String[] fields) {
        for (String field : fields) {
            if (StringUtils.isBlank(field)) {
                return true;
            }
        }
        return false;
    }
 }
