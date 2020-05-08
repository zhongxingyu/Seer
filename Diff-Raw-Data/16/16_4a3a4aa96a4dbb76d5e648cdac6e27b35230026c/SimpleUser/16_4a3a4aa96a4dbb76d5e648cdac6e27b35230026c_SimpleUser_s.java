 package org.jboss.picketlink.idm.model;
 
 /**
  * A simple User implementation
  */
 public class SimpleUser extends AbstractIdentityType implements User
 {
     private String id;
     private String firstName;
     private String lastName;
     private String email;
     
     public SimpleUser(String id)
     {
         this.id = id;
     }
     
     public String getId()
     {
         return id;
     }
 
     public String getFirstName()
     {
         return firstName;
     }
 
     public void setFirstName(String firstName)
     {
         this.firstName = firstName;
     }
 
     public String getLastName()
     {
         return lastName;
     }
 
     public void setLastName(String lastName)
     {
         this.lastName = lastName;
     }
 
     public String getFullName()
     {
         return String.format("%s %s", firstName, lastName);
     }
 
     public String getEmail()
     {
         return email;
     }
 
     public void setEmail(String email)
     {
         this.email = email;
     }
 
     public String getKey() 
     {
        return String.format("%d%d", KEY_PREFIX, id);
     }
 }
