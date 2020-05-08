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
     
    public Object[] getErrors() {
        return (Object[]) response.get("errors");
     }

    @SuppressWarnings("unchecked")
     public String getErrorsToString() {
         String errorsStr = "";
        Object[] errors = getErrors();
         if (errors != null) {
            for (Object error : errors) {
                for (Entry<String, Object> errorEntry : ((Map<String, Object>)error).entrySet()) {
                    errorsStr += errorEntry.getKey() + ": " + errorEntry.getValue() + " - "; 
                }
             }
         }
         
        return errorsStr.substring(0, errorsStr.length() - 2);
     }
 }
