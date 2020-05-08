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
 import java.net.InetSocketAddress;
 import java.net.Socket;
 import java.util.concurrent.LinkedBlockingQueue;
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
     public static final int DEFAULT_PORT = 9001;
 
     /**
      * Amount of time to wait for a socket on connect
      */
     private static final int SOCKET_CONNECT_TIMEOUT = 500;
     
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
      * A concurrent queue of events to be sent
      */
     private LinkedBlockingQueue<NetworkEvent> eventQueue;
     
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
      * Thread that writes to the socket
      */
     private Thread writerThread;
     
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
         socket = new Socket();
         socket.connect(new InetSocketAddress(ip, DEFAULT_PORT), SOCKET_CONNECT_TIMEOUT);
         this.pool = pool;
 
         init();
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
         
         init();
     }
 
     private void init() throws IOException  {
         socket.setKeepAlive(true);
 
         eventQueue = new LinkedBlockingQueue<NetworkEvent>();
         out = new ObjectOutputStream(socket.getOutputStream());
         in = new ObjectInputStream(socket.getInputStream());        
 
         listenerThread = new Thread(new Runnable() {
             public void run() {
                 runConnectionListener();
             }
         });
         listenerThread.setDaemon(true);
         listenerThread.start();
 
         writerThread = new Thread(new Runnable() {
             public void run() {
                 runConnectionWriter();
             }
         });
         writerThread.setDaemon(true);
         writerThread.start();
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
             sendObject(new PingEvent(socket.getInetAddress()));
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
     public void sendEvent(NetworkEvent ev) {
         eventQueue.add(ev);
     }
     
     /**
      * send an object
      * @param obj the Object to be send
      * @throws IOException
      */
     private void sendObject(Object obj) throws IOException {
         out.writeObject(obj);
         out.flush();
     }
     
     private void remoteClosed() {
         pool.raiseEvent(new UserDisconnectedEvent(socket.getInetAddress()));
         // at this point, we should close our output (not our input, since it 
         // might still contain messages)
         try {
             socket.shutdownOutput();
         } catch (IOException ex) {
             LOGGER.log(Level.SEVERE, ex.toString());
         }
     }
     
     /**
      * Start listening for incoming events and send them to the EventPool
      */
     private void runConnectionListener() {
         try {
             while(!socket.isClosed()) {
                 try {
                     NetworkEvent ev = (NetworkEvent)in.readObject();
                     ev.setSource(socket.getInetAddress());
                     LOGGER.info(String.format("Received event %s from %s",
                             ev.getClass().getName(), ev.getSource()));
 
                     // downcast so we don't send it again
                     pool.raiseEvent((Event)ev);
                 } catch (ClassNotFoundException ex) {
                     // TODO handle error
                     LOGGER.log(Level.SEVERE, null, ex);
                 }
             }
         } catch (EOFException ex) {
            LOGGER.log(Level.INFO, ex.toString());
            // this happens when the socket on the remote end closes
            remoteClosed();
         } catch (IOException ex) {
             LOGGER.log(Level.SEVERE, null, ex);
         }
     }
 
     /**
      * Send events from the queue
      */
     private void runConnectionWriter() {
         try {
             while(!socket.isClosed()) {
                 try {
                     NetworkEvent ev = eventQueue.take();
                    LOGGER.info(String.format("Sending event %s to %s",
                            ev.getClass().getName(), ev.getTarget()));
                     sendObject(ev);
                 } catch (IOException ex) {
                     LOGGER.log(Level.SEVERE, null, ex);
                 }
             }
         } catch(InterruptedException ex) {
             // we should just stop then
             LOGGER.log(Level.INFO, ex.toString());
         }
     }
 }
