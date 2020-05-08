 
 package at.amarktl.cluster;
 
 import java.io.IOException;
 import java.net.InetSocketAddress;
 import java.nio.channels.SelectionKey;
 import java.nio.channels.Selector;
 import java.nio.channels.ServerSocketChannel;
 import java.nio.channels.SocketChannel;
 import java.nio.charset.Charset;
 import java.rmi.NotBoundException;
 import java.rmi.RemoteException;
 import java.rmi.registry.LocateRegistry;
 import java.rmi.registry.Registry;
 import java.rmi.server.UnicastRemoteObject;
 import java.util.Iterator;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.atomic.AtomicBoolean;
 
 import at.amarktl.http.HTTPRequest;
 import at.amarktl.http.HTTPResponse;
 import at.amarktl.http.HTTPSession;
 
 public class Server extends UnicastRemoteObject implements IServer {
 
   /**
    * Valid values / range for WEB_SRV_MASTER: TODO
    */
   private static final String RMI_IDENTIFIER = "web-srv-master";
 
   private class HTTPServer {
     private ExecutorService service = Executors.newSingleThreadExecutor();
 
     AtomicBoolean isRunning = new AtomicBoolean(true);
     Selector selector = null;
     ServerSocketChannel server = null;
 
     public HTTPServer(InetSocketAddress address) throws IOException {
       server = ServerSocketChannel.open();
       selector = Selector.open();
       server.socket().bind(address);
       server.configureBlocking(false);
       server.register(selector, SelectionKey.OP_ACCEPT);
     }
 
     public void shutdown() {
       isRunning.getAndSet(false);
 
       try {
         server.close();
         server = null;
       } catch (IOException e) {
         e.printStackTrace();
       }
 
       try {
         selector.close();
         selector = null;
       } catch (IOException e) {
         e.printStackTrace();
       }
 
       service.shutdownNow();
 
     }
 
     public void start() {
       if (isStarted.get()) {
         throw new IllegalStateException("Server Instance already started");
       }
 
       service.execute(new Runnable() {
         @Override
         public void run() {
           while (isRunning.get()) {
             int readyCount;
             try {
               readyCount = selector.selectNow();
               if (readyCount <= 0) {
                 continue;
               }
 
               Iterator<SelectionKey> i = selector.selectedKeys().iterator();
               while (i.hasNext()) {
                 SelectionKey key = i.next();
                 i.remove();
                 if (!key.isValid()) {
                   continue;
                 }
 
                 // get a new connection
                 if (key.isAcceptable()) {
                   // accept them
                   SocketChannel client = server.accept();
                   // non blocking please
                   client.configureBlocking(false);
                   // show out intentions
                   client.register(selector, SelectionKey.OP_READ);
                   // report an error immediatly if there are no cluster nodes
 
                   System.out.println("Accepted new Client [" + client.getRemoteAddress() + "]");
 
                   if (nodes.isEmpty()) {
                     HTTPSession session = (HTTPSession) key.attachment();
                     // create it if it doesnt exist
                     if (session == null) {
                       session = new HTTPSession(client);
                       key.attach(session);
                     }
                     try {
                       HTTPResponse error = new HTTPResponse();
                      IllegalStateException e = new IllegalStateException("No Cluster Nodes connected");
                      e.printStackTrace();
                      error.setContent(getSevereErrorPage(e));
                       session.sendResponse(error);
                       continue;
                     } finally {
                       session.close();
                     }
                   }
 
                 } else if (key.isReadable()) {
                   // get the client
                   SocketChannel client = (SocketChannel) key.channel();
 
                   // get the session
                   HTTPSession session = (HTTPSession) key.attachment();
                   // create it if it doesnt exist
                   if (session == null) {
                     session = new HTTPSession(client);
                     key.attach(session);
                   }
                   // get more data
                   session.readData();
                   // decode the message
                   String line;
                   while ((line = session.readLine()) != null) {
                     // check if we have got everything
                     if (line.isEmpty()) {
                       HTTPRequest request = new HTTPRequest(session.getContent());
                       if (!request.getMethod().equals("GET")) {
                         try {
                           HTTPResponse error = new HTTPResponse();
                          UnsupportedOperationException e = new UnsupportedOperationException("Unsupported Operation: " + request.getMethod());
                          e.printStackTrace();
                          error.setContent(getSevereErrorPage(e));
                           session.sendResponse(error);
                           continue;
                         } finally {
                           session.close();
                         }
                       }
                       handle(session, request);
                     }
                   }
 
                 }
               }
 
             } catch (IOException e) {
               shutdown();
             } finally {
               synchronized (lock) {
                 try {
                   lock.wait(timeout);
                 } catch (InterruptedException e) {
                   System.err.println("Interrpted while waiting due to [" + e.getMessage() + "]");
                 }
               }
             }
           }
         }
       });
     }
 
   }
 
   final Object lock = new Object();
   private static final long serialVersionUID = 1L;
   long timeout = 100;
   final ClusterNodeList nodes = new ClusterNodeList();
   private int portHTTP;
   private int portRMI;
   private final ExecutorService service;
   AtomicBoolean isStarted = new AtomicBoolean(false);
   AtomicBoolean isShutdownInProgress = new AtomicBoolean(false);
   private Registry registry;
   private HTTPServer httpServer;
 
   static final byte[] getSevereErrorPage(Exception e) {
     /** @formatter:off*/
     String error =
         "<html>" +
         "<header />" +
         "<body>" +
     		"<img alt=\"\" src=\"data:image/png;base64,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\" />"+
         "<br /><h1>Internal Severe Server Error</h1><br />" +
     		"<hr />" +
         "<i>" + e.getMessage() + "</i>"+
         "</body>"+
     		"</html>";
     /** @formatter:on */
 
     return error.getBytes(Charset.forName("UTF-8"));
   }
 
   public Server(String externalAddress, int portRMI, int portHTTP, int threadPoolSize) throws RemoteException {
     super();
     if (externalAddress == null) {
       throw new NullPointerException("'externalAddress' must not be null");
     }
     if (externalAddress.trim().length() == 0) {
       throw new IllegalArgumentException("'externalAddress' must not be empty");
     }
     if (portRMI <= 0) {
       throw new IllegalArgumentException("'portRMI' must not be less or equal than 0");
     }
     if (portHTTP <= 0) {
       throw new IllegalArgumentException("'portHTTP' must not be less or equal than 0");
     }
     if (threadPoolSize <= 0) {
       throw new IllegalArgumentException("'threadPoolSize' must not be less or equal than 0");
     }
 
     System.getProperties().setProperty("java.rmi.server.hostname", externalAddress);
 
     this.portRMI = portRMI;
     this.portHTTP = portHTTP;
     this.service = Executors.newFixedThreadPool(threadPoolSize);
   }
 
   /** {@inheritDoc} */
   @Override
   public boolean register(String host, int port) throws RemoteException {
     if (host == null) {
       throw new NullPointerException("'host' must not be null");
     }
     if (host.trim().length() == 0) {
       throw new IllegalArgumentException("'host' must not be empty");
     }
 
     if (port <= 0) {
       throw new IllegalArgumentException("'port' must not be less or equal than 0");
     }
 
     if (!isStarted.get()) {
       throw new IllegalStateException("Server Instance not started");
     }
 
     if (isShutdownInProgress.get()) {
       throw new IllegalStateException("Server Instance is performing Shutdown");
     }
 
     Registry node = LocateRegistry.getRegistry(host, port);
     IClusterNode nodeImpl = null;
 
     try {
       nodeImpl = (IClusterNode) node.lookup("cluster-node");
     } catch (NotBoundException e) {
       throw new RemoteException(e.getMessage(), e);
     }
 
     String address = host + ":" + port;
     if (nodeImpl == null) {
       Exception e = new NullPointerException("Failed to checkout Remote Cluster Node from [" + address + "]");
       throw new RemoteException(e.getMessage(), e);
     }
 
     String id = nodeImpl.getIdentifier() + "@" + address;
 
     System.out.println("Added Cluster Node [" + id + "]");
 
     nodes.add(nodeImpl);
 
     return true;
   }
 
   public void start() throws IOException {
 
     Runtime.getRuntime().addShutdownHook(new Thread(new ShutdownInstanceShutdownHook(this)));
 
     startRMIServer();
 
     startSocketServer();
 
     isStarted.getAndSet(true);
 
     System.out.println("Web Server Master is ready");
 
   }
 
   private class ShutdownInstanceShutdownHook implements Runnable {
 
     private final Server instance;
 
     public ShutdownInstanceShutdownHook(Server instance) {
       this.instance = instance;
     }
 
     /** {@inheritDoc} */
     @Override
     public void run() {
       instance.shutdown();
     }
 
   }
 
   void handle(HTTPSession session, HTTPRequest request) {
     if (session == null) {
       throw new NullPointerException("'session' must not be null");
     }
     if (request == null) {
       throw new NullPointerException("'request' must not be null");
     }
 
     if (nodes.isEmpty()) {
       try {
         HTTPResponse error = new HTTPResponse();
        IllegalStateException e = new IllegalStateException("No Cluster Nodes connected");
        e.printStackTrace();
        error.setContent(getSevereErrorPage(e));
         session.sendResponse(error);
         return;
       } finally {
         session.close();
       }
     }
 
     System.out.println("****************************************************************************************");
     System.out.println("Handling Request: " + request);
     System.out.println("****************************************************************************************");
 
     service.execute(new Handle(session, request, nodes.next()));
   }
 
   public void shutdown() {
     if (isShutdownInProgress.get()) {
       throw new IllegalStateException("Shutdown already in progress");
     }
 
     try {
       isShutdownInProgress.getAndSet(true);
       if (registry != null) {
         try {
           registry.unbind(RMI_IDENTIFIER);
           registry = null;
         } catch (RemoteException | NotBoundException e) {
           e.printStackTrace();
         }
       }
 
       if (httpServer != null) {
         httpServer.shutdown();
         httpServer = null;
       }
 
       isStarted.getAndSet(false);
     } finally {
       isShutdownInProgress.getAndSet(false);
     }
 
   }
 
   private class Handle implements Runnable {
 
     private IClusterNode node;
     private HTTPSession session;
     private HTTPRequest request;
 
     public Handle(HTTPSession session, HTTPRequest request, IClusterNode node) {
       if (session == null) {
         throw new NullPointerException("'session' must not be null");
       }
       if (request == null) {
         throw new NullPointerException("'request' must not be null");
       }
       if (node == null) {
         throw new NullPointerException("'node' must not be null");
       }
       this.session = session;
       this.request = request;
       this.node = node;
     }
 
     /** {@inheritDoc} */
     @Override
     public void run() {
       try {
         String uri = request.getLocation();
         System.out.println("Requested Location [" + uri + "]");
 
         if (uri.equals("/")) {
           uri = uri + "html/index.html";
         } else {
           uri = "/html" + uri;
         }
 
         byte[] b = node.loadFile(uri);
         HTTPResponse response = new HTTPResponse();
         response.setContent(b);
         session.sendResponse(response);
       } catch (Exception e) {
        e.printStackTrace();
         HTTPResponse error = new HTTPResponse();
         error.setContent(getSevereErrorPage(new IllegalStateException(e)));
         session.sendResponse(error);
       } finally {
         session.close();
       }
     }
 
   }
 
   private void startRMIServer() {
     try {
       registry = LocateRegistry.createRegistry(portRMI);
       registry.rebind(RMI_IDENTIFIER, this);
     } catch (Exception e) {
       e.printStackTrace();
       System.exit(-1);
     }
   }
 
   private void startSocketServer() {
     try {
       httpServer = new HTTPServer(new InetSocketAddress(portHTTP));
       httpServer.start();
     } catch (IOException e) {
       e.printStackTrace();
       System.exit(-1);
     }
   }
 }
 
 //---------------------------- Revision History ----------------------------
 //$Log$
 //
