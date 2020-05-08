package org.triple_brain.module.repository.user.user;
 
 /**
  * Copyright Mozilla Public License 1.1
  */
 public class UserRepositoryException extends RuntimeException {
 
     public UserRepositoryException(String message) {
         super(message);
     }
 
     public UserRepositoryException(String message, Throwable cause) {
         super(message, cause);
     }
 
 }
