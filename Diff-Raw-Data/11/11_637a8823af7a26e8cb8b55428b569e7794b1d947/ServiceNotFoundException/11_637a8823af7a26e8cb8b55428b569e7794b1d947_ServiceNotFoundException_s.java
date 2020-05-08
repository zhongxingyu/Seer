 package com.undeadscythes.authenticmd.exception;
 
import org.apache.commons.lang3.*;
 
 /**
  * Thrown when a {@link com.undeadscythes.authenticmd.service.Service} cannot be found matching a user's input.
  *
  * @author UndeadScythes
  */
 public class ServiceNotFoundException extends Exception {
     private static final long serialVersionUID = 1L;
 
     /**
      * Provide the command and arguments that caused this exception.
      */
     public ServiceNotFoundException(final String[] args) {
         super("Cannot find service for command " + StringUtils.join(args, " ") + ".");
     }
 }
