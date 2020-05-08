 package validation.core;
 
 import java.util.Map;
 
 public class ValidationError {
     private String fieldName;
    private String message;
 
     public void describeTo(Map<String, String> report) {
         report.put("fieldName", fieldName);
        report.put("message", message);
     }
 
     public void setFieldName(String fieldName) {
         this.fieldName = fieldName;
     }
 
    public void setDescription(String message) {
        this.message = message;
     }
 
     @Override
     public String toString() {
         return "ValidationError{" +
                 "fieldName='" + fieldName + '\'' +
                ", message='" + message + '\'' +
                 '}';
     }
 }
