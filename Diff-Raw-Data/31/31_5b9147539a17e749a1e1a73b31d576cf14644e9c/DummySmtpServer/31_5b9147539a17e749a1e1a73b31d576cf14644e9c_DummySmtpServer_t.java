 /* vim: set ts=2 et sw=2 cindent fo=qroca: */
 
 package com.globant.katari.tools;
 
 import java.net.ServerSocket;
 import java.util.Iterator;
 
 import org.springframework.beans.DirectFieldAccessor;
 
 import com.dumbster.smtp.SimpleSmtpServer;
 import com.dumbster.smtp.SmtpMessage;
 
/** Dummy smtp server used for testing.
 *
 * This server is based on dumbster, but adds the possibility to start in an OS
 * selected port.
 */
public final class DummySmtpServer implements Iterable<SmtpMessage> {
 
   /** The created SimpleSmtpServer.
    *
    * This is never null.
    */
   private SimpleSmtpServer server;

   /** Private constructor.
   *
    * @param smtpServer The dumbster smtp server wrapped by this class. It is
    * never null.
    */
   private DummySmtpServer(final SimpleSmtpServer smtpServer) {
     server = smtpServer;
   }
 
   /** Creates a dummy smtp server and starts it.
    *
    * @param port the port number that the server will listen to.
   *
    * @return the created dummy server, never null.
    */
   public static DummySmtpServer start(final int port) {
     return new DummySmtpServer(SimpleSmtpServer.start(port));
   }
 
   /** Stops the server.
    *
    * Server is shutdown after processing of the current request is complete.
    */
   public synchronized void stop() {
     server.stop();
   }
 
   /** Obtains the port number where the server bound to.
    *
    * This operation is specially useful when the server is asked to start in
    * port 0, where the operating system selects the port number.
    *
    * @return an integer with the port number.
    */
   public synchronized int getPortNumber() {
     DirectFieldAccessor accesor = new DirectFieldAccessor(server);
     ServerSocket socket;
     socket = (ServerSocket) accesor.getPropertyValue("serverSocket");
     return socket.getLocalPort();
   }

   /** Get email received by this instance since start up.
    *
   * @return an iterator for all the received emails.
    */
   @SuppressWarnings("unchecked")
   public synchronized Iterator<SmtpMessage> iterator() {
     return (Iterator<SmtpMessage>) server.getReceivedEmail();
   }
 
   /** Gets the number of messages received.
    *
    * @return size of received email list.
    */
   public synchronized int getReceivedEmailSize() {
     return server.getReceivedEmailSize();
   }
 }
 
