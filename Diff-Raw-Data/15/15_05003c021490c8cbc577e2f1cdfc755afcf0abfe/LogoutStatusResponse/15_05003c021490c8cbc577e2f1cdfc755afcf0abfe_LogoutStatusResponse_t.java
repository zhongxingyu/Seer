 /*
  * Created on May 12, 2006
  */
 package ecologylab.services.authentication.messages;
 
 import ecologylab.generic.BooleanSlot;
 import ecologylab.generic.ObjectRegistry;
 import ecologylab.services.authentication.AuthConstants;
 import ecologylab.services.authentication.registryobjects.AuthClientRegistryObjects;
 import ecologylab.services.messages.ResponseMessage;
 
 public class LogoutStatusResponse extends ResponseMessage implements AuthMessages, AuthClientRegistryObjects
 {
     public String responseMessage = new String();
     
     public LogoutStatusResponse()
     {
         super();
    }
    
    public LogoutStatusResponse(String responseMessage)
    {
        this.responseMessage = responseMessage;
     }
 
     public boolean isOK()
     {
         return LOGOUT_SUCCESSFUL.equals(responseMessage);
     }
 
     public void processResponse(ObjectRegistry objectRegistry)
     {
         ((BooleanSlot)objectRegistry.lookupObject(LOGIN_STATUS)).value = false;
        
         objectRegistry.modifyObject(LOGIN_STATUS_STRING, responseMessage);
     }
 
 }
