 package com.johnathanmsmith.mvc.web.error;
 
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

 /**
  * Date:   6/12/13 / 8:48 AM
  * Author: Johnathan Mark Smith
  * Email:  johnathansmith1969@gmail.com
  * <p/>
  * Comments:
  * <p/>
  * This is used to return a error to the client
  */
 
 public class ErrorHolder
 {
     public ErrorHolder()
     {
     }
 
    @JsonCreator
    public ErrorHolder(@JsonProperty("errorMessage") String errorMessage)
    {
        this.errorMessage = errorMessage;
    }

     @Override
     public String toString()
     {
         return "ErrorHolder{" +
                 "errorMessage='" + errorMessage + '\'' +
                 '}';
     }
 
     public String getErrorMessage()
     {
         return errorMessage;
     }
 
     public void setErrorMessage(String errorMessage)
     {
         this.errorMessage = errorMessage;
     }
 
     public String errorMessage;
 
     public ErrorHolder(String errorMessage)
     {
         this.errorMessage = errorMessage;
     }
 }
