 #set( $symbol_pound = '#' )
 #set( $symbol_dollar = '$' )
 #set( $symbol_escape = '\' )
 /* vim: set ts=2 et sw=2 cindent fo=qroca: */
 
 package ${package}.web.testsupport;
 
 import org.apache.commons.lang.Validate;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import com.globant.katari.hibernate.coreuser.domain.Role;
 import com.globant.katari.hibernate.coreuser.domain.RoleRepository;
 import com.globant.katari.user.domain.User;
 import com.globant.katari.user.domain.UserRepository;
 import com.globant.katari.user.domain.UserFilter;
 
 /** Utility class to help in the setup of test cases that need to interact with
  * the database.
  */
 public final class DataHelper {
 
   /** A logger.
    */
   private static Log log = LogFactory.getLog(DataHelper.class);
 
   /** A private constructor so no instances are created.
    */
   private DataHelper() {
   }
 
   /** Removes the extra users created by tests.
    *
    * It removes users with names that do not start with 'base-'. It also keeps
    * the administrator.
    *
    * @param repository The user repository. It cannot be null.
    */
   public static void removeExtraUsers(final UserRepository repository) {
     Validate.notNull(repository, "The user repository cannot be null");
     //  Removes the unneeded users.
     for (User user : repository.getUsers(new UserFilter())) {
       log.debug("Found user " + user.getName());
       boolean canDelete = !user.getName().equals("admin");
       canDelete = canDelete & !user.getName().startsWith("base-");
       if (canDelete) {
         repository.remove(user);
       }
     }
   }
 
   /** Removes the extra roles created by tests.
    *
    * It removes roles with names that do not start with 'base-'. It also keeps
    * the administrator role.
    *
    * @param repository The user repository. It cannot be null.
    */
   public static void removeExtraRoles(final RoleRepository repository) {
     Validate.notNull(repository, "The user repository cannot be null");
     //  Removes the unneeded users.
     for (Role role : repository.getRoles()) {
       log.debug("Found role " + role.getName());
       boolean canDelete = !role.getName().equals("ADMINISTRATOR");
       canDelete = canDelete & !role.getName().startsWith("base-");
       if (canDelete) {
         repository.remove(role);
       }
     }
   }
 }
 
