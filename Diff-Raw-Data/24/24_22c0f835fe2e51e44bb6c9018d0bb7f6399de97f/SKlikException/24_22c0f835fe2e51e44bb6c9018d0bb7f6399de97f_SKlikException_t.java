 package cz.sortivo.sklikapi.exception;
 
 import java.util.Map;
import java.util.Map.Entry;
 
 /**
  * The exception is using if response status is different then 200
  * @author Jan Dufek
  */
 public class SKlikException extends Exception{
     
    private static final long serialVersionUID = 1L;

     private final int status;
     private final Map<String, Object> response;
  
     public SKlikException(String message, int status, Map<String, Object> response) {
         super(message);
         this.status = status;
         this.response = response;
     }
 
     public int getStatus() {
         return status;
     }
 
     public Map<String, Object> getResponse() {
         return response;
     }
     
    @SuppressWarnings("unchecked")
    public Map<String, Object> getErrors() {
        return (Map<String, Object>) response.get("errors");
    }
     
    public String getErrorsToString() {
        String errorsStr = "";
        Map<String, Object> errors = getErrors();
        if (errors != null) {
            for (Entry<String, Object> error : errors.entrySet()) {
                errorsStr += error.getKey() + ": " + error.getValue() + " - "; 
            }
        }
        
        return errorsStr;
    }
 }
