 /*
  * Copyright (c) 2013 KloudTek Ltd
  */
 
 package com.kloudtek.systyrant;
 
 import com.kloudtek.systyrant.exception.STRuntimeException;
 
 public interface Action extends Comparable<Action> {
     /**
      * Executes the action
      *
      * @param context  Context
      * @param resource Resource being executed
      * @throws STRuntimeException If an error occurs during execution
      */
     void execute(STContext context, Resource resource) throws STRuntimeException;
 
     /**
      * Checks if execution is required.
      *
      * @param context
      * @param resource
      * @return True if execution is required.
      */
     boolean checkExecutionRequired(STContext context, Resource resource) throws STRuntimeException;
 
     /**
      * Checks if this action supports running on the resource
      *
      * @param context  Context
      * @param resource Resource
      * @return True if the action supports running on the resource and it's environment.
      * @throws STRuntimeException If an error occurs
      * @see #getAlternative()
      */
     boolean supports(STContext context, Resource resource) throws STRuntimeException;
 
     int getOrder();
 
     Type getType();
 
     /**
      * Return the alternative id.
      * If any action on a resource has a non-null alternative value, this means that it will expect at least of the actions
      * of that id to be supported (meaning that {@link #supports(com.kloudtek.systyrant.STContext, Resource)} has returned
      * true).
      *
      * @return Alternative id or null if not an alternative
      * @see #supports(com.kloudtek.systyrant.STContext, Resource)
      */
     String getAlternative();
 
     public enum Type {
        INIT, PREPARE, EXECUTE, SYNC, POSTCHILDREN_EXECUTE, POSTCHILDREN_SYNC, CLEANUP
     }
 }
