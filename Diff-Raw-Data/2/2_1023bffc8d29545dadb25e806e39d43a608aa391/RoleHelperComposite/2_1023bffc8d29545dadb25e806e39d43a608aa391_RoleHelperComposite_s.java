 package org.qi4j.chronos.ui.wicket.authentication;
 
 import org.qi4j.composite.Composite;
 import org.qi4j.composite.Mixins;
import org.qi4j.library.framework.properties.PropertiesMixin;
 
 /**
  * @author edward.yakop@gmail.com
  * @since 0.1.0
  */
 @Mixins( PropertiesMixin.class )
 public interface RoleHelperComposite extends Composite
 {
     /**
      * @return A {@code boolean} indicator whether the user has an admin role.
      * @since 0.1.0
      */
     boolean isAdmin();
 
     /**
      * @return A {@code boolean} indicator whether the user has a staff role.
      * @since 0.1.0
      */
     boolean isStaff();
 
     /**
      * @return A {@code boolean} indicator whether the user has a contact person role.
      * @since 0.1.0
      */
     boolean isContactPerson();
 
     /**
      * @return A {@code boolean} indicator whether the user has an account admin role.
      * @since 0.1.0
      */
     boolean isAccountAdmin();
 
     /**
      * @return A {@code boolean} indicator whether the user has an account developer role.
      * @since 0.1.0
      */
     boolean isAccountDeveloper();
 }
