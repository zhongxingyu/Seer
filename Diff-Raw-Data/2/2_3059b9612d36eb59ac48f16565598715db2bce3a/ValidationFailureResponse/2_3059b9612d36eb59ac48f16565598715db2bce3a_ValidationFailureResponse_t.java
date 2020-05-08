 package org.inigma.shared.webapp;
 
 import java.util.ArrayList;
 import java.util.Collection;
 
public class ValidationFailureResponse {
     private static class FieldErrorResponse extends ObjectErrorResponse {
         private String field;
 
         public FieldErrorResponse(String field, String code, String message) {
             super(code, message);
             this.field = field;
         }
 
         public String getField() {
             return field;
         }
     }
 
     private static class ObjectErrorResponse {
         private String code;
         private String message;
 
         public ObjectErrorResponse(String code, String message) {
             this.code = code;
             this.message = message;
         }
 
         public String getCode() {
             return code;
         }
 
         public String getMessage() {
             return message;
         }
     }
 
     private Collection<ObjectErrorResponse> errors;
 
     public ValidationFailureResponse() {
         this.errors = new ArrayList<ObjectErrorResponse>();
     }
 
     public Collection<ObjectErrorResponse> getErrors() {
         return errors;
     }
 
     public void reject(String code, String message) {
         errors.add(new ObjectErrorResponse(code, message));
     }
 
     public void reject(String field, String code, String message) {
         errors.add(new FieldErrorResponse(field, code, message));
     }
 }
