 package cryptocast.client;
 
 import java.io.File;
 import java.io.IOException;
 import java.math.BigInteger;
 import java.net.InetSocketAddress;
 import java.net.Socket;
 import cryptocast.comm.StreamMessageInChannel;
 import cryptocast.crypto.BroadcastEncryptionClient;
 import cryptocast.crypto.SchnorrGroup;
 import cryptocast.crypto.naorpinkas.NPKey;
 import cryptocast.crypto.naorpinkas.SchnorrNPClient;
 import cryptocast.util.SerializationUtils;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * This class does StreamViewerActivity's work to ensure that the activity is displayed
  * correctly.
  * @author Christoph
  *
  */
 public class SocketConnector implements Runnable {
     private Socket sock;
     private InetSocketAddress connectAddr;
     private File keyFile;
     private RawStreamMediaPlayer player;
     private StreamViewerActivity streamViewerActivity;
     private static final Logger log = LoggerFactory
             .getLogger(SocketConnector.class);
     
     /**
      * Creates a SocketConnector with the given socket.
      * @param socket the socket to connect
      * @param connectAddr 
      * @param player 
      * @param streamViewerActivity 
      */
     public SocketConnector(Socket socket, InetSocketAddress connectAddr, File keyFile, 
             RawStreamMediaPlayer player, StreamViewerActivity streamViewerActivity) {
         this.sock = socket;
         this.connectAddr = connectAddr;
         this.keyFile = keyFile;
         this.player = player;
         this.streamViewerActivity = streamViewerActivity;
         streamViewerActivity.setStatusText("Initializing..."); 
     }
 
     @Override
     public void run(){
         connectToStream();
     }
     
     /**
      * Stops the connector.
      */
     public void stop() {
         try {
             sock.close();
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
     
     private void connectToStream() {
         log.debug("Connecting to {}", connectAddr);
         streamViewerActivity.setStatusText("Connecting to server...");
 
         sock = new Socket();
 
         try {
             sock.connect(connectAddr, 5000);
         } catch (Exception e) {
             log.error("Could not connect to target server", e);
             streamViewerActivity.createErrorPopup("Could not connect to server!");
             return;
         }
         log.debug("Connected to {}", connectAddr);
         streamViewerActivity.setStatusText("Connected to server...");
 
         receiveData();
     }
 
     private void receiveData() {
         NPKey<BigInteger, SchnorrGroup> key;
         try {
             key = SerializationUtils.readFromFile(keyFile);
         } catch (Exception e) {
             log.error("Could not load key from file: ", e);
             streamViewerActivity.createErrorPopup("Invalid key file!");
             streamViewerActivity.app.getServerHistory().invalidateKeyFile(connectAddr);
            
             return;
         }
 
         try {
             BroadcastEncryptionClient in =
                     new BroadcastEncryptionClient(
                             new StreamMessageInChannel(sock.getInputStream()), 
                             new SchnorrNPClient(key));
             log.debug("Waiting for first byte");
             streamViewerActivity.setStatusText("Waiting for first byte...");
             in.read();
             log.debug("Starting media player");
             streamViewerActivity.setStatusText("Starting media player...");
             player.setRawDataSource(in, "audio/mpeg");
             player.prepare();
         } catch (Exception e) {
             log.error("Error while playing stream", e);
             streamViewerActivity.createErrorPopup("Error while playing stream!");
             return;
         }
     }
 }
