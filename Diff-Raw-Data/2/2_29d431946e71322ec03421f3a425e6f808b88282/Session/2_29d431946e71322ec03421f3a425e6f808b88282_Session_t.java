 package com.trendmicro.mist.session;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.IOException;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.net.SocketTimeoutException;
 import java.util.Collection;
 import java.util.HashMap;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import com.trendmicro.mist.Client;
 import com.trendmicro.mist.MistException;
 import com.trendmicro.mist.proto.GateTalk;
 import com.trendmicro.mist.util.Exchange;
 import com.trendmicro.mist.util.GOCUtils;
 import com.trendmicro.spn.common.util.Utils;
 
 public abstract class Session extends Thread {
     public static final String MIST_MESSAGE_TTL = "MIST_TTL";
     protected static Log logger = LogFactory.getLog(Session.class);
     private ServerSocket localServer;
 
     protected Socket socket;
     protected BufferedInputStream socketInput;
     protected BufferedOutputStream socketOutput;
     protected boolean detachNow = false;
 
     protected HashMap<Exchange, Client> allClients = new HashMap<Exchange, Client>();
 
     private GOCUtils gocClient = null;
     protected boolean determinedConnection = false;
 
     protected int sessionId;
     protected GateTalk.Session sessionConfig;
     protected boolean isReady = false;
 
     /**
      * Helper function to accept an incoming connection from the client. The
      * localServer is used one time only and will not accept more then one
      * connection.
      * 
      * @return True - If the connection and the socket IO streams has been set
      *         up correctly<br>
      *         False - Fail to accept the connection or session is detached
      */
     protected boolean acceptConnection() {
         socket = null;
         socketInput = null;
         socketOutput = null;
         try {
             for(;;) {
                 try {
                     socket = localServer.accept();
                     localServer.close();
                     localServer = null;
                     break;
                 }
                 catch(SocketTimeoutException e) {
                     if(detachNow) {
                         cleanupSockets();
                         return false;
                     }
                 }
             }
             socket.setTcpNoDelay(true);
             socketInput = new BufferedInputStream(socket.getInputStream());
             socketOutput = new BufferedOutputStream(socket.getOutputStream());
             return true;
         }
         catch(IOException e) {
             cleanupSockets();
             return false;
         }
     }
 
     /**
      * Helper function to clean up the sockets
      */
     private void cleanupSockets() {
         if(localServer != null) {
             try {
                 localServer.close();
             }
             catch(Exception e) {
             }
         }
         if(socketOutput != null) {
             try {
                 socketOutput.close();
             }
             catch(Exception e) {
             }
         }
         if(socketInput != null) {
             try {
                 socketInput.close();
             }
             catch(Exception e) {
             }
         }
         if(socket != null) {
             try {
                 socket.close();
             }
             catch(Exception e) {
             }
         }
     }
 
     private void checkRole(GateTalk.Request.Role role) throws MistException {
         if(this instanceof ProducerSession) {
             if(role == GateTalk.Request.Role.SOURCE)
                 throw new MistException(MistException.INCOMPATIBLE_TYPE_SINK);
         }
         else {
             if(role == GateTalk.Request.Role.SINK)
                 throw new MistException(MistException.INCOMPATIBLE_TYPE_SOURCE);
         }
     }
 
     public Session(int sessId, GateTalk.Session sessConfig) throws MistException {
         this.sessionId = sessId;
         this.sessionConfig = sessConfig;
     }
 
     public int getCommPort() {
         if(localServer == null)
             return -1;
         else
             return localServer.getLocalPort();
     }
 
     @Override
     public abstract void run();
 
     public abstract void addClientIfAttached(Client c);
 
     /**
      * All clients under this session will be opened
      * 
      * @param isResume
      */
     public void open(boolean isResume) {
         for(Client c : allClients.values()) {
             try {
                 c.openClient(determinedConnection, isResume, false);
             }
             catch(Exception e) {
                 logger.error(e.getMessage());
             }
         }
     }
 
     /**
      * All clients under this session will be opened
      * 
      * @param isPause
      */
     public void close(boolean isPause) {
         for(Client c : allClients.values())
             c.closeClient(isPause, false);
         getGocClient().close();
     }
 
     /**
      * Create a server socket and starts a serving thread
      * 
      * @param role
      *            Whether it is a SOURCE or a SINK
      * @throws MistException
      */
     public synchronized void attach(GateTalk.Request.Role role) throws MistException {
         checkRole(role);
 
         if(isAttached())
             throw new MistException(MistException.ALREADY_ATTACHED);
 
         try {
             localServer = new ServerSocket();
             localServer.setReuseAddress(true);
             localServer.setSoTimeout(1000);
             localServer.bind(null);
         }
         catch(IOException e) {
             logger.error(e.getMessage());
             try {
                 localServer.close();
             }
             catch(Exception e2) {
             }
             throw new MistException(e.getMessage());
         }
         detachNow = false;
         gocClient = null;
         isReady = false;
         open(false);
         start();
     }
 
     protected abstract void detach();
 
    public void detach(GateTalk.Request.Role role) throws MistException {
         checkRole(role);
 
         detachNow = true;
         detach();
         close(false);
         cleanupSockets();
         try {
             join();
         }
         catch(InterruptedException e) {
         }
     }
 
     /**
      * Add a producer / consumer client to the session
      * 
      * @param client
      *            The client to be added
      * @throws MistException
      */
     public Client addClient(GateTalk.Client clientConfig) throws MistException {
         // A producer client cannot be added to a source session and vice versa
         checkRole(clientConfig.getType() == GateTalk.Client.Type.CONSUMER ? GateTalk.Request.Role.SOURCE: GateTalk.Request.Role.SINK);
 
         synchronized(allClients) {
             // Check if the client is already mounted, if not, add to the map
             if(allClients.containsKey(new Exchange(clientConfig.getChannel().getName())))
                 throw new MistException(MistException.ALREADY_MOUNTED);
 
             Client c = new Client(clientConfig, sessionConfig);
             c.openClient(determinedConnection, false, false);
             allClients.put(c.getExchange(), c);
             if(isAttached())
                 addClientIfAttached(c);
 
             return c;
         }
     }
 
     public void removeClient(GateTalk.Client clientConfig) throws MistException {
         checkRole(clientConfig.getType() == GateTalk.Client.Type.CONSUMER ? GateTalk.Request.Role.SOURCE: GateTalk.Request.Role.SINK);
 
         synchronized(allClients) {
             if(allClients.isEmpty())
                 throw new MistException(MistException.EMPTY_SESSION);
 
             Exchange exchange = new Exchange((clientConfig.getChannel().getType() == GateTalk.Channel.Type.QUEUE ? "queue": "topic") + ":" + clientConfig.getChannel().getName());
             Client c = findClient(exchange);
             if(c == null)
                 throw new MistException(MistException.exchangeNotFound(exchange.toString()));
             c.closeClient(false, false);
             allClients.remove(exchange);
         }
     }
 
     public void migrateClient(Exchange exchange) {
         logger.info("migrating " + exchange + "; session " + sessionId);
         try {
             GateTalk.Client clientConfig = findClient(exchange).getConfig();
             removeClient(findClient(exchange).getConfig());
             addClient(clientConfig);
         }
         catch(Exception e) {
             logger.error(Utils.convertStackTrace(e));
         }
     }
 
     public Client findClient(Exchange exchange) {
         return allClients.get(exchange);
     }
 
     public GOCUtils getGocClient() {
         if(gocClient == null)
             gocClient = new GOCUtils();
         return gocClient;
     }
 
     public boolean isReady() {
         return isReady;
     }
 
     public abstract boolean isAttached();
 
     public Collection<Client> getClientList() {
         return allClients.values();
     }
 }
