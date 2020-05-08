 package org.synyx.minos.core.domain;
 
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.Set;
 
 import javax.persistence.Column;
import javax.persistence.ElementCollection;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 
 import org.synyx.hades.domain.auditing.AbstractAuditable;
 import org.synyx.minos.util.Assert;
 
 
 /**
  * Domain class for a role.
  * 
  * @author Oliver Gierke
  */
 @Entity
 public class Role extends AbstractAuditable<User, Long> {
 
     private static final long serialVersionUID = 71349340935123L;
 
     private static final String PREFIX = "ROLE_";
 
     public static final String ADMIN_NAME = "ADMIN";
     public static final String USER_NAME = "USER";
 
     private static final Collection<String> SYSTEM_ROLES =
             Arrays.asList(ADMIN_NAME, USER_NAME);
 
     @Column(nullable = false, unique = true)
     private String name;
 
    @ElementCollection(fetch = FetchType.EAGER)
     private Set<String> permissions;
 
 
     /**
      * Empty constructor.
      */
     public Role() {
 
         this.permissions = new HashSet<String>();
     }
 
 
     /**
      * Constructor setting a name instantly.
      * 
      * @param name
      */
     public Role(String name) {
 
         this();
 
         Assert.hasText(name, "Name must not be empty!");
         setName(name);
     }
 
 
     /**
      * Returns the {@link Role} name.
      * 
      * @return the name
      */
     public String getName() {
 
         return name;
     }
 
 
     /**
      * Sets the {@link Role}s name. Will prevent empty {@link String}s and
      * {@literal null} values and turn the name into uppercase letters.
      * 
      * @param name the name to set
      */
     public void setName(String name) {
 
         Assert.hasText(name, "Name must not be empty!");
         this.name = name.toUpperCase();
     }
 
 
     /**
      * Returns all permissions assigned to that {@link Role}.
      * 
      * @return
      */
     public Set<String> getPermissions() {
 
         return Collections.unmodifiableSet(permissions);
     }
 
 
     /**
      * Sets the permissions tied to that {@link Role}.
      * 
      * @param permissions
      */
     public void setPermissions(Set<String> permissions) {
 
         this.permissions = permissions;
     }
 
 
     /**
      * Adds the given permissions to the {@link Role}.
      * 
      * @param permission
      * @return
      */
     public Role add(String... permission) {
 
         this.permissions.addAll(Arrays.asList(permission));
         return this;
     }
 
 
     /**
      * Removes all given permissions from that {@link Role}.
      * 
      * @param permission
      * @return
      */
     public Role remove(String... permission) {
 
         this.permissions.removeAll(Arrays.asList(permission));
         return this;
     }
 
 
     /*
      * (non-Javadoc)
      * 
      * @see java.lang.Object#toString()
      */
     @Override
     public String toString() {
 
         return PREFIX + getName();
     }
 
 
     /**
      * Returns whether the role is a system role. This will have further
      * implications like not being allowed to be deleted a.s.o.
      * 
      * @return
      */
     public boolean isSystemRole() {
 
         return SYSTEM_ROLES.contains(this.getName());
     }
 
 
     /**
      * Extracts the actual name from the given {@link Role} name.
      * 
      * @param prefixedName
      * @return
      */
     public static String stripPrefix(String prefixedName) {
 
         if (null == prefixedName || !prefixedName.startsWith(PREFIX)) {
             return prefixedName;
         }
 
         return prefixedName.substring(PREFIX.length());
     }
 }
