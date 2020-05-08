 /*
  * Created on Mar 30, 2006
  */
 package ecologylab.services.authentication.messages;
 
 import java.util.HashMap;
 
 import ecologylab.generic.ObjectRegistry;
 import ecologylab.services.authentication.AuthenticationListEntry;
 import ecologylab.services.authentication.registryobjects.AuthServerRegistryObjects;
 import ecologylab.services.messages.ErrorResponse;
 import ecologylab.services.messages.OkResponse;
 import ecologylab.services.messages.RequestMessage;
 import ecologylab.services.messages.ResponseMessage;
 
 /**
  * A Logout message indicates that the connnected client no longer wants to be
  * connected.
  * 
  * @author Zach Toups (toupsz@gmail.com)
  */
 public class Logout extends RequestMessage implements AuthMessages,
         AuthServerRegistryObjects
 {
 
     public AuthenticationListEntry entry = new AuthenticationListEntry("", "");
 
     /**
      * Should not normally be used; only for XML translations.
      */
     public Logout()
     {
         super();
     }
 
     /**
      * Creates a new Logout object using the given AuthenticationListEntry
      * object, indicating the user that should be logged out of the server.
      * 
      * @param entry -
      *            the entry to use for this Logout object.
      */
     public Logout(AuthenticationListEntry entry)
     {
         super();
         this.entry = entry;
     }
 
     /**
      * @override
      * Attempts to log the user specified by entry from the system; if they are
      * already logged in; if not, sends a failure response.
      */
     public ResponseMessage performService(ObjectRegistry objectRegistry)
     {
         HashMap authedClients = (HashMap) objectRegistry
                 .lookupObject(AUTHENTICATED_CLIENTS_BY_USERNAME);
         ResponseMessage responseMessage;
 
         if ((authedClients != null)
                 && authedClients.containsKey(entry.getUsername()))
         {
             responseMessage = OkResponse.get();
 
             authedClients.remove(entry.getUsername());
         } else
            responseMessage = new ErrorResponse(LOGOUT_FAILED_NOT_LOGGEDIN);
 
         return responseMessage;
     }
 
 }
