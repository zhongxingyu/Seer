 /**
  * 
  */
 package com.github.podd.restlet;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.openrdf.model.URI;
 import org.openrdf.model.impl.ValueFactoryImpl;
 import org.restlet.security.Role;
 
 import com.github.ansell.restletutils.RestletUtilRole;
 
 /**
  * The Roles available for PODD users.
  * 
  * @author Peter Ansell p_ansell@yahoo.com
  * 
  *         Copied from http://github.com/ansell/restlet-utils
  */
 public enum PoddRoles implements RestletUtilRole
 {
     ADMIN("Administrator", "A repository administrator of the PODD System",
             "http://purl.org/podd/oas/roles/administrator"),
     
     AUTHENTICATED("Authenticated User", "A user of the PODD System", "http://purl.org/podd/oas/roles/authenticated"),
     
     PROJECT_MEMBER("Project member", "A user who is a member of a particular project",
             "http://purl.org/podd/oas/roles/project_member"),
     
     PROJECT_OBSERVER("Project observer", "A user who is an observer of a particular project",
            "http://purl.org/podd/oas/roles/project_member"),
     
     PROJECT_ADMIN("Project Administrator", "A user who is an administrator of a particular project",
             "http://purl.org/podd/oas/roles/project_administrator"),
 
     ROLE_A("Role A", "A generic role", "http://purl.org/podd/oas/roles/role_a"),
     
     ;
     
     public static RestletUtilRole getRoleByName(final String name)
     {
         for(final RestletUtilRole nextRole : PoddRoles.values())
         {
             if(nextRole.getName().equals(name))
             {
                 return nextRole;
             }
         }
         
         return null;
     }
     
     public static RestletUtilRole getRoleByUri(final URI nextUri)
     {
         for(final RestletUtilRole nextRole : PoddRoles.values())
         {
             if(nextRole.getURI().equals(nextUri))
             {
                 return nextRole;
             }
         }
         
         return null;
     }
     
     public static List<Role> getRoles()
     {
         final List<Role> result = new ArrayList<Role>(PoddRoles.values().length);
         
         for(final RestletUtilRole nextRole : PoddRoles.values())
         {
             // WARNING: After Restlet-2.1RC5 Roles will only be considered equal if they are the
             // same java object, so this must not create a new Role each time
             result.add(nextRole.getRole());
         }
         
         return result;
     }
     
     private Role role;
     
     private URI uri;
     
     /**
      * Constructor
      * 
      * @param roleName
      * @param description
      * @param uriString
      */
     PoddRoles(final String roleName, final String description, final String uriString)
     {
         this.role = new Role(roleName, description);
         this.uri = ValueFactoryImpl.getInstance().createURI(uriString);
     }
     
     /**
      * @return the description
      */
     @Override
     public String getDescription()
     {
         return this.role.getDescription();
     }
     
     /**
      * @return the name
      */
     @Override
     public String getName()
     {
         return this.role.getName();
     }
     
     @Override
     public Role getRole()
     {
         return this.role;
     }
     
     @Override
     public URI getURI()
     {
         return this.uri;
     }
     
 }
