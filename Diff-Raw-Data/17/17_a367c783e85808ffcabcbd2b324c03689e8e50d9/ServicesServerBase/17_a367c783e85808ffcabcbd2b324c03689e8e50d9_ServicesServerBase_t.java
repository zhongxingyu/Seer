 /*
  * Created on May 3, 2006
  */
 package ecologylab.services;
 
 import java.io.IOException;
 import java.net.InetAddress;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.net.SocketAddress;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 
 import sun.misc.BASE64Encoder;
 import ecologylab.generic.Debug;
 import ecologylab.generic.ObjectRegistry;
 import ecologylab.generic.StartAndStoppable;
 import ecologylab.services.messages.RequestMessage;
 import ecologylab.services.messages.ResponseMessage;
 import ecologylab.xml.ElementState;
 import ecologylab.xml.TranslationSpace;
 import ecologylab.xml.XmlTranslationException;
 
 /**
  * A base set of fields and methods that are necessary for any server
  * implementation.
  * 
  * @author Zach Toups (toupsz@gmail.com)
  */
 public abstract class ServicesServerBase extends Debug implements Runnable,
         StartAndStoppable
 {
     protected int            portNumber;
 
     protected ServerSocket   serverSocket;
 
     protected boolean        finished;
 
     /**
      * Space that defines mappings between xml names, and Java class names, for
      * request messages.
      */
     protected TranslationSpace      requestTranslationSpace;
 
     /**
      * Provides a context for request processing.
      */
     protected ObjectRegistry objectRegistry;
 
     protected int            connectionCount = 0;
 
     private MessageDigest    digester;
     
     private long dispensedTokens;
 
     /**
      * Creates a Services Server Base. Sets internal variables, but does not
      * bind the port. Port binding is to be handled by sublcasses.
      * 
      * @param portNumber
      * @param requestTranslationSpace
      * @param objectRegistry
      *            Provides a context for request processing.
      * @throws IOException
      */
     protected ServicesServerBase(int portNumber,
             TranslationSpace requestTranslationSpace, ObjectRegistry objectRegistry)
             throws IOException, java.net.BindException
     {
         this.portNumber = portNumber;
         this.requestTranslationSpace = requestTranslationSpace;
         if (objectRegistry == null)
             objectRegistry = new ObjectRegistry();
         this.objectRegistry = objectRegistry;
 
         try
         {
             digester = MessageDigest.getInstance("SHA-256");
         } catch (NoSuchAlgorithmException e)
         {
             debug("This can only happen if the local implementation does not include the given hash algorithm.");
             e.printStackTrace();
         }
     }
 
     /**
      * Generates a unique identifier String for the given socket, based upon
      * actual ports used and ip addresses with a hash.
      * 
      * @param incomingSocket
      * @return
      */
     protected String generateSessionToken(Socket incomingSocket)
     {
         // clear digester
         digester.reset();
 
         // we make a string consisting of the following:
         // time of initial connection (when this method is called), server ip,
         // client ip, client actual port
         digester.update(String.valueOf(System.currentTimeMillis()).getBytes());
 //        digester.update(String.valueOf(System.nanoTime()).getBytes());
         digester.update(this.serverSocket.getInetAddress().toString()
                 .getBytes());
         digester.update(incomingSocket.getInetAddress().toString().getBytes());
         digester.update(String.valueOf(incomingSocket.getPort()).getBytes());
 
         digester.update(String.valueOf(this.dispensedTokens).getBytes());
         
         dispensedTokens++;
 
         // convert to normal characters and return as a String
         return new String((new BASE64Encoder()).encode(digester.digest()));
     }
 
     /**
      * Perform the service associated with a RequestMessage, by calling the
      * performService() method on that message.
      * 
      * @param requestMessage
      *            Message to perform.
      * @return Response to the message.
      */
     public ResponseMessage performService(RequestMessage requestMessage)
     {
        ResponseMessage temp = requestMessage.performService(objectRegistry);
        temp.setUid(requestMessage.getUid());
         
        return temp;
     }
 
     public RequestMessage translateXMLStringToRequestMessage(
             String messageString, boolean doRecursiveDescent)
             throws XmlTranslationException
     {
         RequestMessage requestMessage = (RequestMessage) ElementState
                 .translateFromXMLString(messageString, requestTranslationSpace,
                         doRecursiveDescent);
         return requestMessage;
     }
 
     /**
      * Remove the argument passed in from the set of connections we know about.
      * 
      * @param serverToClientConnection
      */
     protected void connectionTerminated()
     {
         connectionCount--;
         // When thread close by unexpected way (such as client just crashes),
         // this method will end the service gracefully.
         terminationAction();
     }
     
     /**
      * This defines the actions that server needs to perform 
      * when the client ends unexpected way. Detail implementations will be in subclasses.
      */
     protected void terminationAction()
     {
         
     }
     
     /**
      * Get the message passing context associated with this server.
      * 
      * @return
      */
     public ObjectRegistry getObjectRegistry()
     {
         return objectRegistry;
     }
 
     /**
      * @return Returns the requestTranslationSpace.
      */
     public TranslationSpace getRequestTranslationSpace()
     {
         return requestTranslationSpace;
     }
     
     
     public SocketAddress getAddress()
     {
         return this.serverSocket.getLocalSocketAddress();
     }
 }
