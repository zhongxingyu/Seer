 package com.mobeelizer.java.api;
 
 import java.util.List;
 import java.util.Set;
 
 /**
  * Holder for database operations errors.
  * 
  * @since 1.0
  */
 public interface MobeelizerErrors {
 
     /**
      * Return the list of global errors.
      * 
      * @return errors
      * @since 1.4
      */
     List<MobeelizerError> getGlobalErrors();
 
     /**
      * Check if field is valid.
      * 
      * @param field
      *            field
      * @return true if valid
      * @since 1.4
      */
     boolean isFieldValid(final String field);
 
     /**
      * Return the list of errors for given field.
      * 
      * @param field
      *            field
      * @return errors
      * @since 1.4
      */
     List<MobeelizerError> getFieldErrors(final String field);
 
     /**
      * Return the set of all invalid fields.
      * 
      * @return errors
      * @since 1.4
      */
     Set<String> getInvalidFields();
 }
