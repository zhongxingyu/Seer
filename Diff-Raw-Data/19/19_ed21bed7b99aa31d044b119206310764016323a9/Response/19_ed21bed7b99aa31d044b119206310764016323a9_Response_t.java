 package org.inigma.lwrest.webapp;
 
 import java.util.Collection;
 import java.util.LinkedList;
 import java.util.Map;
 
 public class Response {
     class Error {
         private String code;
         private String message;
         private String parameter;
 
         public Error(String parameter, String code, String defaultMessage) {
             this.parameter = parameter;
             this.code = code;
             this.message = defaultMessage;
             if (this.message == null) {
                 this.message = code;
                 if (parameter != null) {
                     this.message += "." + parameter;
                 }
             }
         }
 
         public String getCode() {
             return code;
         }
 
         public String getMessage() {
             return message;
         }
 
         public String getParameter() {
             return parameter;
         }
     }
 
     private Object data;
     private Collection<Error> errors = new LinkedList<Error>();
    private boolean success = true;
 
     public Object getData() {
         return data;
     }
 
     public Collection<Error> getErrors() {
         return errors;
     }
 
     public boolean isSuccess() {
         return success;
     }
 
     public void reject(Exception e) {
         Throwable t = null;
         while (e.getCause() != null && e.getCause() != t) {
             t = e.getCause();
         }
         this.errors.add(new Error(null, t.getClass().getName(), t.getMessage()));
        this.success = false;
     }
 
     public void reject(String code) {
         this.errors.add(new Error(null, code, null));
        this.success = false;
     }
 
     public void reject(String code, String message) {
         this.errors.add(new Error(null, code, message));
        this.success = false;
     }
 
     public void rejectValue(String field, String code) {
         this.errors.add(new Error(field, code, null));
        this.success = false;
     }
 
     public void rejectValue(String field, String code, String message) {
         this.errors.add(new Error(null, code, message));
        this.success = false;
     }
 
     public void setData(Boolean data) {
         this.data = data;
     }
 
     public void setData(Collection<?> data) {
         this.data = data;
     }
 
     public void setData(Map<String, ?> data) {
         this.data = data;
     }
 
     public void setData(Number data) {
         this.data = data;
     }
 
     public void setData(ResponseTransformer data) {
         this.data = data;
     }
 
     public void setData(String data) {
         this.data = data;
     }
 
     public void setSuccess(boolean success) {
         this.success = success;
     }
 }
