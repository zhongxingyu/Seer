 package org.qi4j.chronos.ui.wicket.authentication.concerns;
 
 import org.apache.wicket.authorization.strategies.role.Roles;
import static org.qi4j.chronos.model.SystemRole.*;
 import org.qi4j.chronos.ui.wicket.authentication.RoleHelperComposite;
 import org.qi4j.chronos.ui.wicket.bootstrap.ChronosSession;
import org.qi4j.composite.ConcernOf;
 
 /**
  * @author edward.yakop@gmail.com
  */
public abstract class RoleHelperChornosSessionConcern extends ConcernOf<RoleHelperComposite>
     implements RoleHelperComposite
 {
     public final boolean isAdmin()
     {
         Roles roles = getCurrentUserRoles();
         return roles.hasRole( ACCOUNT_ADMIN );
     }
 
     public final boolean isStaff()
     {
         Roles roles = getCurrentUserRoles();
         return roles.hasRole( STAFF );
     }
 
     public final boolean isContactPerson()
     {
         Roles roles = getCurrentUserRoles();
         return roles.hasRole( CONTACT_PERSON );
     }
 
     public final boolean isAccountAdmin()
     {
         Roles roles = getCurrentUserRoles();
         return roles.hasRole( ACCOUNT_ADMIN );
     }
 
     public final boolean isAccountDeveloper()
     {
         Roles roles = getCurrentUserRoles();
         return roles.hasRole( ACCOUNT_DEVELOPER );
     }
 
     private Roles getCurrentUserRoles()
     {
         ChronosSession session = ChronosSession.get();
         return session.getRoles();
     }
 }
