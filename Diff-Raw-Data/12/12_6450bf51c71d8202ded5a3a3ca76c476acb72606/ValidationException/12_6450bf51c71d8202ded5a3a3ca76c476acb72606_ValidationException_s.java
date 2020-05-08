 package com.amee.base.validation;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
public class ValidationException extends Throwable {
 
     private ValidationHelper validationHelper;
 
     public ValidationException() {
         super();
     }
 
     public ValidationException(ValidationHelper validationHelper) {
         this();
         setValidationHelper(validationHelper);
     }
 
     public ValidationHelper getValidationHelper() {
         return validationHelper;
     }
 
     public void setValidationHelper(ValidationHelper validationHelper) {
         this.validationHelper = validationHelper;
     }
 
     public JSONObject getJSONObject() {
         try {
             JSONObject o = new JSONObject();
             o.put("validationResult", getValidationHelper().getValidationResult().getJSONObject());
             o.put("status", "INVALID");
             return o;
         } catch (JSONException e) {
             throw new RuntimeException("Caught JSONException: " + e.getMessage(), e);
         }
     }
 }
