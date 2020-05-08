 package org.codehaus.xfire.jaxws;
 
 public class EchoFault
     extends Exception
 {
     private String someMessage = "message";
 
     public String getSomeMessage()
     {
         return someMessage;
     }
 
     public void setSomeMessage(String someMessage)
     {
         this.someMessage = someMessage;
     }
     
 }
