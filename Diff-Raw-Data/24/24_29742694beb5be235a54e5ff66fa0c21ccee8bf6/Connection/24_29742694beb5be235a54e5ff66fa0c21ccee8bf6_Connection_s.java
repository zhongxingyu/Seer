 package ch9k.network;
 
 import ch9k.eventpool.Event;
 import ch9k.eventpool.EventPool;
 import ch9k.eventpool.NetworkEvent;
 import ch9k.network.events.PingEvent;
 import ch9k.network.events.UserDisconnectedEvent;
 import java.io.EOFException;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.net.InetAddress;
 import java.net.Socket;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * 
  * @author nudded
  */
 public class Connection {
     /**
      * The default port used to create connections
      * chosen because it's the smallest prime number
      * OVER 9000
      */
     public static int DEFAULT_PORT = 9001;
     
     /**
      * The socket used to write to the other side
      */
     private Socket socket;
 
     /**
      * I'm a lumberjack, and I'm okay.
      * I sleep all night and I work all day.
      *
      * Oh wait, that's a different kind of logging.
      */
     private static final Logger LOGGER =
             Logger.getLogger(Connection.class.getName());
     
     /**
      * Streams used to transfer Objects
      */
     private ObjectOutputStream out;
     private ObjectInputStream in;
 
     /**
      * Thread that listens to the socket
      */
     private Thread listenerThread;
     
     /**
      * The EventPool to send events to
      */
      private EventPool pool;
 
     /**
      * Constructor
      * @param ip
      * @param pool
      * @throws IOException
      */
     public Connection(InetAddress ip, EventPool pool) throws IOException {
         this(new Socket(ip,DEFAULT_PORT), pool);
     }
 
     /**
      * constructor used by ConnectionManager, constructs a Connection
      * out of a connected socket.
      * @param socket The socket that connected
      * @param pool The EventPool this connection will use
      * @throws IOException
      */
     public Connection(Socket socket, EventPool pool) throws IOException {
         this.socket = socket;
         this.pool = pool;
         
         socket.setKeepAlive(true);
 
         out = new ObjectOutputStream(socket.getOutputStream());
         in = new ObjectInputStream(socket.getInputStream());        
 
         listenerThread = new Thread(new Runnable() {
             public void run() {
                 runConnection();
             }
         });
         listenerThread.start();
     }
     
     /**
      * close the socket
      */
     public void close() {
         LOGGER.info("Closing connection to " + socket.getInetAddress());
         try {
             socket.close();
         } catch(IOException e) {
             // TODO handle error?
             System.out.println(e);
         }
     }
     
     /**
      * sends a PingEvent to the target
      * @return false if pinging the target gives an exception
      */
     public boolean hasConnection() {
         try {
             sendEvent(new PingEvent(socket.getInetAddress()));
         } catch (IOException e) {
             return false;
         }
         return true;
     }
     
     /**
      * send a NetworkEvent
      * @param ev The event to be send
      * @throws IOException
      */
     public void sendEvent(NetworkEvent ev) throws IOException {
         sendObject(ev);
     }
     
     /**
      * send an object
      * @param obj the Object to be send
      * @throws IOException
      */
     public void sendObject(Object obj) throws IOException {
         out.writeObject(obj);
     }
     
     /**
      * Start listening for incoming events and send them to the EventPool
      */
     private void runConnection() {
         try {
             while(!socket.isClosed()) {
                 try {
                     NetworkEvent ev = (NetworkEvent)in.readObject();
                     ev.setSource(socket.getInetAddress());
                     LOGGER.info(String.format("Received event %s from %s",
                             ev.getClass().getName(), ev.getSource()));
 
                     // downcast so we don't send it again
                     pool.raiseEvent((Event)ev);
                 } catch (ClassNotFoundException e) {
                     // TODO handle error
                     System.err.println(e);
                 }
             }
         } catch (EOFException e) {
            // This happens when the socket on the other side closes
            LOGGER.log(Level.INFO, e.toString());
            pool.raiseEvent(new UserDisconnectedEvent(socket.getInetAddress()));
         } catch (IOException e) {
             LOGGER.log(Level.WARNING, e.toString());
         }
     }
 }
