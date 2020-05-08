 package org.qi4j.chronos.ui.wicket.authentication.concerns;
 
 import org.apache.wicket.authorization.strategies.role.Roles;
import static org.qi4j.chronos.model.SystemRole.ACCOUNT_ADMIN;
import static org.qi4j.chronos.model.SystemRole.ACCOUNT_DEVELOPER;
import static org.qi4j.chronos.model.SystemRole.CONTACT_PERSON;
import static org.qi4j.chronos.model.SystemRole.STAFF;
 import org.qi4j.chronos.ui.wicket.authentication.RoleHelperComposite;
 import org.qi4j.chronos.ui.wicket.bootstrap.ChronosSession;
import org.qi4j.composite.scope.ConcernFor;
 
 /**
  * @author edward.yakop@gmail.com
  */
public abstract class RoleHelperChornosSessionConcern
     implements RoleHelperComposite
 {
    @ConcernFor RoleHelperComposite ignored;

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
