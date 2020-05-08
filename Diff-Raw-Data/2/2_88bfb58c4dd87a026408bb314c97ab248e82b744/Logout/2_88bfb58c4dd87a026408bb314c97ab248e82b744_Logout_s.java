 /*
  * Created on Mar 30, 2006
  */
 package ecologylab.services.authentication.messages;
 
 import ecologylab.generic.ObjectRegistry;
 import ecologylab.services.authentication.Authenticatable;
 import ecologylab.services.authentication.AuthenticationListEntry;
 import ecologylab.services.authentication.registryobjects.AuthServerRegistryObjects;
 import ecologylab.services.messages.RequestMessage;
 import ecologylab.services.messages.ResponseMessage;
 import ecologylab.xml.xml_inherit;
 
 /**
  * A Logout message indicates that the connnected client no longer wants to be
  * connected.
  * 
  * @author Zach Toups (toupsz@gmail.com)
  */
 @xml_inherit public class Logout extends RequestMessage implements AuthMessages,
         AuthServerRegistryObjects
 {
    @xml_attribute protected AuthenticationListEntry entry = new AuthenticationListEntry("", "");
 
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
      * @override Attempts to log the user specified by entry from the system; if
      *           they are already logged in; if not, sends a failure response.
      */
     public ResponseMessage performService(ObjectRegistry objectRegistry)
     {
         Authenticatable server = (Authenticatable) objectRegistry.lookupObject(MAIN_AUTHENTICATABLE);
         
         if (server.logout(entry, this.getSender()))
         {
             return new LogoutStatusResponse(LOGOUT_SUCCESSFUL);
         }
         else
         {
             return new LogoutStatusResponse(LOGOUT_FAILED_IP_MISMATCH);
         }
     }
 
     /**
      * @return Returns the entry.
      */
     public AuthenticationListEntry getEntry()
     {
         return entry;
     }
 
 }
