 import play.Application;
 import play.GlobalSettings;
 import play.Logger;
 
 import com.beligum.core.models.User;
 import com.beligum.core.repositories.UserRepository;
 import com.beligum.core.utils.security.UserRoles;
 
 
 public class Global extends GlobalSettings
 {
     // -----CONSTANTS-----
 
     // -----VARIABLES-----
 
     // -----CONSTRUCTORS-----
 
     // -----PUBLIC FUNCTIONS-----
     @Override
     public void onStart(Application app)
     {
 	Logger.info("Cms has started");
 	if (play.Play.application().configuration().getBoolean("com.beligum.cms.demo")) {
 	    //Create dem user
 	    Logger.info("Creating demo user");
 	    User user = UserRepository.findByEmail("demo");
 	    if (user == null) {
 		user = new User();
 		user.setEmail("demo");
 		user.setFirstName("demo");
 		user.setLastName("account");
 		user.setNewPassword("demo");
 		user.setRoleLevel(UserRoles.ADMIN_ROLE.getLevel());
 		UserRepository.save(user);
 	    }
 	}
 	
 
     }
 
 
     // -----PROTECTED FUNCTIONS-----
 
     // -----PRIVATE FUNCTIONS-----
 }
