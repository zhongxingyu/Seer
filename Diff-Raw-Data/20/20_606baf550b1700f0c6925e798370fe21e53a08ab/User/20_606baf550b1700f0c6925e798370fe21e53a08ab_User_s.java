 package com.crowdstore.models.users;
 
 import com.crowdstore.models.common.IdentifiableContainer;
 
 import java.io.Serializable;
 
 /**
  * @author fcamblor
  */
 public class User implements Serializable, IdentifiableContainer<UserIdentity> {
     private UserIdentity identity;
 
     public User() {
         this(null);
     }
 
    public User(UserIdentity userIdentity) {
         this.identity = identity;
     }
 
     @Override
     public UserIdentity getIdentity() {
         return identity;
     }
 }
