package org.triple_brain.module.repository.user.user;
 
 /**
  * Copyright Mozilla Public License 1.1
  */
 public class NonExistingUserException extends UserRepositoryException {
     public NonExistingUserException(String identifier) {
         super("User not found: " + identifier);
     }
 }
