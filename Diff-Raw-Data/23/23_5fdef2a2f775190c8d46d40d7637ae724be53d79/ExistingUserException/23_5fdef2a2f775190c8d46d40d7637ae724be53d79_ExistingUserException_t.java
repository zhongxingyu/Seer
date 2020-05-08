package org.triple_brain.module.repository.user;
 
 /**
  * Copyright Mozilla Public License 1.1
  */
 public class ExistingUserException extends UserRepositoryException {
     public ExistingUserException(String userNameOrEmail) {
         super("A user already exist with username or email: " + userNameOrEmail);
     }
 }
