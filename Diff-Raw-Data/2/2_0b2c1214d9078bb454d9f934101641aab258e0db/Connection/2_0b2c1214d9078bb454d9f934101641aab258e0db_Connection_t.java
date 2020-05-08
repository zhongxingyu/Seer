 package no.fictive.irclib.model.network;
 
 import no.fictive.irclib.control.IRCBufferedWriter;
 import no.fictive.irclib.control.IRCEventHandler;
 import no.fictive.irclib.control.MessageQueue;
 import no.fictive.irclib.model.user.Profile;
 import org.apache.log4j.Logger;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.net.InetAddress;
 import java.net.Socket;
 import java.net.UnknownHostException;
 import java.util.Random;
 
 /**
  * @author Espen Jacobsson
  *         This class keeps a connection to an IRC server.
  */
 public class Connection implements Runnable {
 
     private IRCEventHandler eventHandler;
     private Network network;
     private ConnectionValidator connectionValidator;
 
     private Socket socket;
     private BufferedReader reader;
     private MessageQueue messageQueue;
     private Logger logger = Logger.getLogger(Connection.class);
 
     private boolean running = false;
     private boolean triedAltInfo = false;
 
     private String hostname;
     private int port;
     private Profile profile;
     private InetAddress bindToHostname;
 
 
     /**
      * Creates a new connection.
      *
      * @param hostname            Hostname to connect to.
      * @param port                Port to connect on.
      * @param profile             {@link Profile} to associate with this connection.
      * @param network             {@link Network} to associate with this connection.
      * @param networkEventHandler {@link NetworkEventHandler} to create a link with an {@link IRCEventHandler}.
      */
     public Connection(String hostname, int port, Profile profile, Network network, NetworkEventHandler networkEventHandler) {
         this.hostname = hostname;
         this.port = port;
         this.profile = profile;
         this.network = network;
         this.eventHandler = new IRCEventHandler(network, profile, networkEventHandler);
     }
 
     public Connection(String hostname, int port, InetAddress bindToHostname, Profile profile, Network network, NetworkEventHandler networkEventHandler) {
         this.hostname = hostname;
         this.port = port;
         this.bindToHostname = bindToHostname;
         this.profile = profile;
         this.network = network;
         this.eventHandler = new IRCEventHandler(network, profile, networkEventHandler);
     }
 
     /**
      * Initiates a connection to the network.
      *
      * @throws UnknownHostException
      * @throws IOException
      */
     public void connect() throws UnknownHostException, IOException {
         network.setState(State.CONNECTING);
         socket = new Socket(hostname, port);
         if (bindToHostname != null)
             socket = new Socket(hostname, port, bindToHostname, 0);
 
         reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
         messageQueue = new MessageQueue(new IRCBufferedWriter(new OutputStreamWriter(socket.getOutputStream()), network));
         running = true;
         Random random = new Random();
         Thread thread = new Thread(this);
         thread.setName(network.getServerAlias() + random.nextInt());
         thread.start();
         connectionValidator = new ConnectionValidator(network);
         sendUserInfo();
     }
 
 
     /**
      * Sends the initial information to the server based on the information in the {@link Profile}.
      */
     private void sendUserInfo() {
         messageQueue.writeline("CAP LS");
         messageQueue.writeline("NICK " + profile.getNickname());
         messageQueue.writeline("USER " + profile.getNickname() + " 0 * :" + profile.getRealname());
     }
 
     /**
      * Sends initial info the the server, using the alternative nickname if there is one.
      */
     public void sendAlternativeUserInfo() {
         if (!profile.getAlternativeNickname().isEmpty()) {
             if (triedAltInfo) {
                 sendAlternativeRandomUserInfo();
                 //logger.error(String.format("All nicknames are in use. Terminating connection for %s.", network.getServerAlias()));
                 //disconnect();
             }
             else {
                 triedAltInfo = true;
                 messageQueue.writeline("CAP LS");
                 messageQueue.writeline("NICK " + profile.getAlternativeNickname());
                 messageQueue.writeline("USER " + profile.getAlternativeNickname() + " 0 * :" + profile.getRealname());
             }
         }
         else {
             sendAlternativeRandomUserInfo();
             //logger.error(String.format("Nickname in use, and no alternative nickname given. Terminating connection for %s.", network.getServerAlias()));
             //disconnect();
         }
     }
 
     private void sendAlternativeRandomUserInfo() {
 
         Random random = new Random();
         String randomNick = profile.getNickname() + random.nextInt(2000);
 
         messageQueue.writeline("CAP LS");
         messageQueue.writeline("NICK " + randomNick);
         messageQueue.writeline("USER " + randomNick + " 0 * :" + profile.getRealname());
     }
 
 
     /**
      * Disconnects from the network the good way, sending a QUIT-message.
      */
     public synchronized void disconnect() {
         if (profile.getQuitMessage() == null)
             messageQueue.writeline("QUIT");
         else
             messageQueue.writeline("QUIT :" + profile.getQuitMessage());
 
         try {
             this.wait(5000);
             stop();
         } catch (InterruptedException e) {
             e.printStackTrace();
         }
     }
 
     /**
      * @see Runnable#run()
      */
     public void run() {
         while (running) {
             String line = null;
             try {
                 line = new String(reader.readLine().getBytes(), "UTF-8");
                 network.gotResponse();
                 eventHandler.handleEvent(line);
             } catch (IOException e) {
                 logger.error("Connection broken.", e);
                 close();
             } catch (NullPointerException e) {

             }
         }
         close();
     }
 
     private void close() {
         try {
             if (socket != null && !socket.isClosed()) {
                 socket.close();
             }
             if (reader != null) {
                 reader.close();
             }
         } catch (IOException e) {
             logger.error("I/O error", e.getCause());
         }
         running = false;
         connectionValidator.stop();
     }
 
 
     /**
      * Stops this thread.
      */
     protected void stop() {
         close();
     }
 
 
     /**
      * Stops the connectionvalidator from running in the background.
      */
     protected void stopConnectionValidation() {
         connectionValidator.stop();
     }
 
 
     /**
      * Writes a line to the server.
      *
      * @param line The line to write.
      */
     public void writeline(String line) {
         if(line == null) {
             logger.warn("Got a line containing nothing (null). Returning.");
             return;
         }
 
         if(line.length() > 508) {
             logger.warn("Message is too long (508 is max). Max characters to an IRCd is 512 characters. 4 characters are reserved for \r\n which will be placed automatically.");
             return;
         }
 
         if(line.contains("\r\n") || line.contains("\n") || line.contains("\r")) {
             logger.warn("Message contains illegal new line characters (\\n, \\r, or both). Not printing.");
 			return;
 		}
         messageQueue.writeline(line);
     }
 }
