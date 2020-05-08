 package cz.sortivo.sklikapi.exception;
 
import cz.sortivo.sklikapi.Response;
 import java.util.Map;
 
 /**
  * The exception is using if response status is different then 200
  * @author Jan Dufek
  */
 public class SKlikException extends Exception{
     
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
     
     
    
 }
