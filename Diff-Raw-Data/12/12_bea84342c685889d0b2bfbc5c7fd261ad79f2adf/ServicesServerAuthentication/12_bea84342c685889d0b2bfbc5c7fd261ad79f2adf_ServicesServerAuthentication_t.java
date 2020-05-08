 /*
  * Created on Mar 30, 2006
  */
 package ecologylab.services.authentication;
 
 import java.io.IOException;
 import java.net.BindException;
 import java.net.Socket;
 import java.util.HashMap;
 
 import ecologylab.generic.ObjectRegistry;
 import ecologylab.services.ServerToClientConnection;
 import ecologylab.services.ServicesServer;
 import ecologylab.xml.ElementState;
 import ecologylab.xml.NameSpace;
 import ecologylab.xml.XmlTranslationException;
 
 /**
  * ServiceServerAuthentication is used to handle servers that require
  * authentication. This means that the ServerToClientConnection will require
  * that the client log in before it will perform any other work.
  * 
  * It also means that the objectRegistry must contain "authenticationList" and
  * "authenticatedClients" entries. These are created by the constructors in this
  * class.
  * 
  * @author Zach Toups (toupsz@gmail.com)
  */
 public class ServicesServerAuthentication extends ServicesServer {
 
     /**
      * This is the actual way to create an instance of this.
      * 
      * @param portNumber
      * @param requestTranslationSpace
      * @param objectRegistry
      * @param authListFilename - a file name indicating the location of the authentication list; this should be an XML file of an AuthenticationList object.
      * @return  A server instance, or null if it was not possible to open a ServerSocket
      *          on the port on this machine.
      */
     public static ServicesServerAuthentication get(int portNumber,
                           NameSpace requestTranslationSpace, ObjectRegistry objectRegistry, String authListFilename)
     {
         ServicesServerAuthentication newServer    = null;
         try
         {
             AuthenticationList authList = (AuthenticationList) ElementState.translateFromXML(
                     authListFilename, NameSpace.get(
                             "authListNameSpace",
                             "ecologylab.services"));
             newServer   = new ServicesServerAuthentication(portNumber, requestTranslationSpace, objectRegistry, authList);
         } catch (IOException e)
         {
             println("ServicesServer ERROR: can't open ServerSocket on port " + portNumber);
             e.printStackTrace();
         } catch (XmlTranslationException e) {
             e.printStackTrace();
         }
         return newServer;
     }
 
     /**
      * This is the actual way to create an instance of this.
      * 
      * @param portNumber
      * @param requestTranslationSpace
      * @param objectRegistry
      * @param authList - the AuthorizationList object to be used to determine possible users.
      * @return  A server instance, or null if it was not possible to open a ServerSocket
      *          on the port on this machine.
      */
     public static ServicesServerAuthentication get(int portNumber,
                           NameSpace requestTranslationSpace, ObjectRegistry objectRegistry, AuthenticationList authList)
     {
         ServicesServerAuthentication newServer    = null;
         try
         {
             newServer   = new ServicesServerAuthentication(portNumber, requestTranslationSpace, objectRegistry, authList);
         } catch (IOException e)
         {
             println("ServicesServer ERROR: can't open ServerSocket on port " + portNumber);
             e.printStackTrace();
         } 
         
         return newServer;
     }
     
     /**
      * 
      * @param portNumber
      * @param requestTranslationSpace
      * @param objectRegistry
      * @param authList
      * @throws IOException
      * @throws BindException
      */
     private ServicesServerAuthentication(int portNumber,
             NameSpace requestTranslationSpace, ObjectRegistry objectRegistry,
            AuthenticationList authList) throws IOException, BindException 
    {
         super(portNumber, requestTranslationSpace, objectRegistry);
 
        requestTranslationSpace.addTranslation("ecologylab.services.authentication", "Login");
        requestTranslationSpace.addTranslation("ecologylab.services.authentication", "Logout");
         
         this.objectRegistry.registerObject("authenticationList", authList);
 
         this.objectRegistry.registerObject("authenticatedClients",
                 new HashMap());
     }
     
     /**
      * Create a ServerToClientConnectionAuthentication, the object that handles the connection
      * to each incoming client and requires that they pass a Login item that matches an entry in the authenticationList. To extend the functionality of the client, you
      * can override this method in your subclass of this, to return a subclass
      * of ServerToClientConnection.
      * 
      * @param incomingSocket
      * @return
      * @throws IOException
      */
     protected ServerToClientConnection getConnection(Socket incomingSocket)
            throws IOException 
    {
         return new ServerToClientConnectionAuthentication(incomingSocket, this);
     }
 }
