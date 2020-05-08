 package com.crowdstore.models.users;
 
 import com.crowdstore.models.common.GenericIdentifiable;
 
 /**
  * @author fcamblor
  */
 public class UserIdentity extends GenericIdentifiable {
     private String displayName;
     private String email;
 
     public UserIdentity setDisplayName(String _displayName) {
         this.displayName = _displayName;
         return this;
     }
 
     public String getDisplayName() {
         return this.displayName;
     }
 
     public UserIdentity setEmail(String _email) {
         this.email = _email;
         return this;
     }
 
     public String getEmail() {
         return this.email;
     }
 }
