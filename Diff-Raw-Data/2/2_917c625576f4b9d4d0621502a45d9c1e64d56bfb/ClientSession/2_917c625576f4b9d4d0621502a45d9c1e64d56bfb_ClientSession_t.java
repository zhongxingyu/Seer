 package se.bricole.xss.server;
 
 import java.net.*;
 import java.io.*;
 import java.util.Enumeration;
 import java.util.Vector;
 import java.util.Properties;
 import java.util.Hashtable;
 import java.util.Set;
 import java.util.HashSet;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.w3c.dom.Document;
 
 /**
  * <p>
  * Each ClientConnection is associated with exactly one client, and is therefore the primary
  * interface for all client I/O and communication.
  * </p>
  * <p>The fact that this is a subclass of <code>Thread</code> is an
  * implementation detail side effect and should change in the future,
  * so developers should rely on the Thread API being there. </p>
  */
 public class ClientSession extends Thread implements XMLClient {
 
     public final static String vcId = "$Id$";
 
     static final boolean broadcastUnknownXML = false;
 
     private boolean receiveBroadcasts = false;
 
     Socket socket = null;
     ServerSocket serverSocket = null;
     Server server = null;
     ClientProxy proxy = null;
     AsynchSender asynchSender = null;
 
     private long lastSend = System.currentTimeMillis();
     private long lastReceive = 0;
 
     int proxyId = -1, poolSlot = -1;
 
     boolean keepAlive = true;
     boolean active = false;
 
     boolean hasClosed = false;
 
     OutputStream output;
     InputStream input;
 
     CommandParser parser = null;
 
     Properties clientProperties;
     Map clientObjects = new HashMap();
 
     Set authenticatedDomains = new HashSet();
     boolean isAuthenticated = false;
 
     public boolean isAuthenticated() {
 	return isAuthenticated;
     }
 
     public boolean isAuthenticatedTo(String domain) {
 	synchronized (authenticatedDomains) {
 	    return authenticatedDomains.contains(domain);
 	}
     }
 
     protected void authenticatedTo(String domain) {
 	synchronized (authenticatedDomains) {
 	    authenticatedDomains.add(domain);
 	    checkIsAuthenticated();
 	}
     }
 
     protected void deAuthenticatedTo(String domain) {
 	synchronized (authenticatedDomains) {
 	    authenticatedDomains.remove(domain);
 	    checkIsAuthenticated();
 	}
     }
 
     private void checkIsAuthenticated() {
 	isAuthenticated = authenticatedDomains.size() > 0;
     }
 
     protected int getPoolSlot() {
 	return poolSlot;
     }
 
     protected ClientSession(ServerSocket serverSocket, Server server) {
 	this.serverSocket = serverSocket;
 	this.server = server;
     }
 	
     /**
      * Returns the boolean value of the "receiveBroadcasts" property.
      * If this property is set to "false", this client will not
      * receive broadcasts sent with
      * ClientProxy.broadcast(ClientConnection, String).
      */
     public boolean getReceiveBroadcasts() {
 	return receiveBroadcasts;
     }
 
     /**
      * Sets the boolean value of the "receiveBroadcasts" property for
      * this client.  If this property is set to "false", this client
      * will not receive broadcasts sent with
      * ClientProxy.broadcast(ClientConnection, String).
      */
     public void setReceiveBroadcasts(boolean b) {
 	receiveBroadcasts = b;
     }
 
     /**
      * Similar to getSharedObject()/setSharedObject() in the
      * ClientProxy class, arbitrary object references can be stored in
      * a ClientConnection class, with the difference that the only
      * valid key is a String object.
      */
     public void putObject(String key, Object o) {
 	synchronized (clientObjects) {
 	    clientObjects.put(key, o);
 	}
     }
 
     /**
      * Similar to getSharedObject()/setSharedObject() in the
      * ClientProxy class, arbitrary object references can be stored in
      * a ClientConnection class, with the difference that the only
      * valid key is a String object.
      */
     public Object getObject(String key) {
 	synchronized (clientObjects) {
 	    return clientObjects.get(key);
 	}
     }
 
     public Object removeObject(String key) {
 	synchronized (clientObjects) {
 	    return clientObjects.remove(key);
 	}
     }
 
     /**
      * Along with the object reference storage of
      * getObject()/setObject(), The ClientConnection class provides
      * the methods getProperty() and setProperty() to let a user store
      * String objects with String keys ( la java.util.Properties).
      */
     public void setProperty(String key, String value) {
 	clientProperties.setProperty(key, value);
     }
 
     /**
      *
      * Returns the ClientProxy object to which this ClientConnection
      * is associated.
      *
      */
     public ClientProxy getProxy() {
 	return proxy;
     }
 
     /**
      *
      * Along with the object reference storage of
      * getObject()/setObject(), The ClientConnection class provides
      * the methods getProperty() and setProperty() to let a user store
      * String objects with String keys ( la java.util.Properties).
      *
      */
     public String getProperty(String key) {
 	return clientProperties.getProperty(key);
     }
 
     /**
      * Returns an integer unique for this server instance representing
      * this ClientConnection.
      */
     public int getId() {
 	return proxyId;
     }
 
     private void setId(int id) {
 	proxyId = id;
     }
 
     protected boolean hasClosed() {
 	return hasClosed;
     }
 
     public void sendAsynch(Document doc)
     throws IOException {
 	sendAsynch(XMLUtil.documentToString(doc));
     }
 
     public void sendAsynch(String s) 
     throws IOException {
 	if (asynchSender != null) {
 	    asynchSender.enqueue(s);
 	} else {
 	    send(s);
 	}
     }
 
 
     public void send(Document doc) throws IOException {
 	send(XMLUtil.documentToString(doc));
     }
 
     /**
      *
      * Sends a NULL-terminated bunch of octets to the client. The
      * octets sent are acquired from the String object using
      * String.getBytes(). There is room for improvement here.
      *
      */
     public void send(String s) throws IOException {
 	if (socket == null || input == null || output == null) {
 	    throw new IOException("Some I/O object is null (socket: " + socket + 
 				  ", input: " + input + ", output: " + output + ")");
 	}
 	if (Server.debug) Server.debug(this, "Sending: " + s);
 	output.write((s + "\000").getBytes("UTF-8"));
 	lastSend = System.currentTimeMillis();
     }
 
     /**
      * Closes the TCP socket associated with this ClientConnection
      */
     public void close() throws IOException {
 	if (socket != null) socket.close();
 	hasClosed = true;
 	Server.status(toString() + ": disconnecting client.");
     }
 
     /**
      * Generally used when something goes really bad. Closes socket.
      */
     protected void carefulCleanup() {
 	if (proxy != null) {
 	    proxy.remove(this);
 	}
 
 	if (socket != null) {
 	    try {
 		socket.close();
 	    } catch (IOException ioe) {
 		Server.debug(this, "carefulCleanup(): I/O Exception: " + ioe.getMessage());
 	    }
 	}
 	proxyId = -1;
 	proxy = null;
 		
 
     }
 
     /**
      * Returns the peer Internet address of this client's socket.
      */
     public InetAddress getInetAddress() {
 	return socket.getInetAddress();
     }
 
     /**
      * Returns an identifying string.
      */
     public String toString() {
 	return "[CS<" + 
 	    Integer.toHexString(System.identityHashCode(this)) + "/" +
 	    proxyId + ">]";
 	// return "[CS id:"
 // 	    + proxyId
 // 	    + " proxy:"
 // 	    + (proxy != null ? proxy.getId() : -1)
 // 	    + " pooled:"
 // 	    + poolSlot
 // 	    + "]"; 
     }
 
     /**
      * Sets whether this is a keep-alive thread or not.
      * If false, the thread will terminate as soon as it has served it's first
      * (or next, if set during execution) client.
      */
     public void setKeepAlive(boolean keepAlive) {
 	this.keepAlive = keepAlive;
     }
         
     /**
      * This is the main loop, which reads NULL-terminated (\0) strings and calls the XML parser (which in turn
      * may call a matching XML module).
      */
     public void run() {
 	if (server.config.getBooleanProperty("EnableAsynchSend")) {
 	    //server.debug(this, "Enabling asynchronous sending...");
 	    asynchSender = new AsynchSender(this);
 	    asynchSender.start();
 	} else {
 	    //server.debug(this, "Disabling asynchronous sending...");
 	}
 
 	do {
 	    try {
 		proxyId = -1;
 		proxy = null;
 		delayedFinishInProgress = false;
 				
 		Server.debug(this, "waiting for connection");
 		setName("IdleClientThread-" + poolSlot);
 		active = false;
 		hasClosed = false;
 		try {
 		    socket = serverSocket.accept();
 		} catch (SocketException se) {
 		    Server.debug(this, se.getMessage());
 		    keepAlive = false;
 		    continue;
 		}
 		Server.status(toString() + ": connect: " +
 			      socket.getInetAddress().getHostAddress());
 
 		active = true;
 		setName("ActiveClientThread-" + poolSlot);
 		setup(socket, server.findClientProxy());
 
 // 		Server.status("[" + proxyId + "] Connect from: " +
 // 			      socket.getInetAddress().getHostAddress());
 		while(blockingSocketRead(socket));
 
 	    } catch (IOException ioe) {
 		Server.debug(this, 
 			     "I/O error: " + ioe.getClass().toString() + ": " + ioe.getMessage()); 
 	    } catch (NoProxyAvailableException nae) {
 		Server.warn("refused connection; proxy count limit reached");
 	    } catch (Throwable t1) {
 		Server.warn(toString() + ": Unhandled exception: " + t1.toString());
 		t1.printStackTrace();
 	    }
 
 	    /**
 	     * cleanup.
 	     */
 	    if (proxy != null) proxy.remove(this);
             
 	    Enumeration e = sessionEventListeners.elements();
 	    while (e.hasMoreElements()) {
  		SessionEventListener l = (SessionEventListener) e.nextElement();
 		if (Server.debug) {
 		    Server.debug("calling " + l + ".clientStop(" + this + ")");
 		}
 		l.clientStop(this);
 	    }
 	    //socket = null;
 
 	} while (keepAlive);
 	if (asynchSender != null) asynchSender.shutdown();
 	active = false;
     }
 
     public boolean isActive() {
 	return active;
     }
 
 
 
     /**
      * Dereferences and closes this session.
      */
     public void finish() {
 	keepAlive = false;
 
 	if (proxy != null)
 	    proxy.remove(this);
 	if (socket != null) {
 	    try {
 		socket.close();
 	    } catch (IOException e) {
 
 	    }
 	    socket = null;
 	}
 
 	//if (asynchSender != null) asynchSender.shutdown();
 
 // 	if (poolSlot == -1) { // huh?
 // 	    keepAlive = false;
 // 	}
 
     }
 
     public void resetProxy() {
 	proxy = null;
 	proxyId = -1;
     }
 
     /**
      *
      */
     protected boolean socketIsNull() {
 	return socket == null;
     }
 
     /**
      * Returns the number of milliseconds elapsed since this client sent any data.
      */
     public long getIdleTimeMillis() {
 	return System.currentTimeMillis() - lastReceive;
     }
     protected void setup(Socket s, ClientProxy p) throws IOException {
 	this.socket = s;
 	this.proxy = p;
 
 	lastReceive = System.currentTimeMillis();
 
 	setId(Server.getGUID());
 	proxy.add(this);
 	output = socket.getOutputStream();
 	input = socket.getInputStream();
 
 	Iterator i = proxy.getConfiguration().ioFilters.iterator();
 	while (i.hasNext()) {
 	    IOFilterModule filter = (IOFilterModule) i.next();
 	    Server.debug("Attaching I/O filter " + filter);
 	    input = filter.getInputFilter(input);
 	    output = filter.getOutputFilter(output);
 	}
 
 	clientProperties = new Properties();
 
 	try {
 	    parser = (CommandParser) new XMLCommandParser(proxy, this);
 	} catch (ParserException pe) {
 	    Server.warn(this.toString() + " failed to initialize parser");
 	}
 
 	if (asynchSender != null) asynchSender.reset();
 
 	synchronized (sessionEventListeners) {
 	    Iterator si = sessionEventListeners.iterator();
 	    while (si.hasNext()) {
 		((SessionEventListener) si.next()).clientStart(this);
 	    }
 	}
     }
 
     Vector sessionEventListeners = new Vector(1);	
 
     /**
      * Adds a ConnectionEventReceiver to the list of listeners which
      * will receive a notification when something happens to this
      * object.
      */
     public void addSessionEventListener(SessionEventListener c) {
 	synchronized (sessionEventListeners) {
 	    if (!sessionEventListeners.contains(c)) {
 		sessionEventListeners.add(c);
 	    }
 	}
     }
 
     public void removeSessionEventListener(SessionEventListener c) {
 	synchronized (sessionEventListeners) {
 	    sessionEventListeners.remove(c);
 	}
     }
 
     boolean delayedFinishInProgress = false;
 
     /**
      * Ask this session to finish soon.
      *
      * Creation date: (2001-06-11 05:35:18)
      */
     public void delayedFinish() {
 	delayedFinishInProgress = true;
     }
 
     public boolean isInDelayedFinish() {
 	return delayedFinishInProgress;
     }
 
     private boolean blockingSocketRead(Socket s)
     throws IOException {	    
 	StringBuffer buff = new StringBuffer();
 	int b = -2;
 	lastReceive = System.currentTimeMillis();
 	b = input.read();
 	while (b != -1 && b != 0) {
 	    buff.append((char) b);
 	    b = input.read();
 	    lastReceive = System.currentTimeMillis();
 	}
 	if (b == -1) {
 	    //keepAlive = false;
 	    Server.status(toString() + ": connection closed by foreign host");
 	    return false;
 	}
 	boolean broadcast = true;
 
 	Server.debug(this, "Got XML: " + buff.toString());
 	if (parser != null) {
 	    try {
 		broadcast = parser.parse(buff);
 	    } catch (ParserException pe) {
 		Server.warn(this.toString() + ": parse error: " + pe.getMessage());
 	    }
 	}
 	if (broadcast && broadcastUnknownXML) {
 	    proxy.broadcast(this, buff.toString());
 	}
 
 	if (proxy != null)
 	    proxy.incrementCommandCount(1);
 		
 		
 	return true;
 
     }
 	
     /**
      * Creation date: (2001-06-11 19:46:31)
      * @author Rasmus Sten
      * @param id int
      */
     public void setPoolSlot(int id) {
 	poolSlot = id;	
     }
 	
     protected void setServerSocket(ServerSocket s) {
 	this.serverSocket = s;
     }
     private long lastClient = System.currentTimeMillis();
 
     static class AsynchSender extends Thread {
 	ClientSession session;
 	LinkedList queue = new LinkedList();
 	boolean shutdown = false;
 
 	public AsynchSender(ClientSession session) {
 	    this.session = session;
 	    setDaemon(false);
 	    setName("AsynchSender[" + session.getId() + "]");
 	}
 
 	public void reset() {
 	    setName("AsynchSender[" + session.getId() + "]");
 	    synchronized (queue) {
 		queue.clear();
 	    }
 	}
 
 	public void shutdown() {
 	    shutdown = true;
 	    interrupt();
 	    try {
 		join();
 	    } catch (InterruptedException ex1) {
 		Server.debug("AsynchSender.shutdown(): " + ex1.toString());
 	    }
 	}
 	public void enqueue(String s) {
 	    synchronized (queue) {
 		queue.add(s);
 		queue.notify();
 	    }
 	}
 
 	public void enqueue(List l) {
 	    synchronized (queue) {
 		synchronized (l) {
 		    queue.addAll(l);
 		}
 		queue.notify();
 	    }
 	}
 
 	public void run() {
 	    Exception exception = null;
 	    try {
 		while (true) {
 		    synchronized (queue) {
 			while (queue.size() == 0) {
 			    queue.wait();
 			}
 		    }
 
 		    while (!queue.isEmpty()) {
 			String nextString;
 			synchronized (queue) {
 			    nextString = (String) queue.removeFirst();
 			}
 			
 			try {
			    session.send(nextString);
 			} catch (IOException ex1) {
 			    
 			    if (!session.hasClosed()) {
 				
 				try {
 				    session.close();
 				} catch (IOException ex2) {}
 				
 				Server.warn("I/O error in AsynchSender for " + session + 
 					    ": " + ex1.toString());
 				if (Server.debug) ex1.printStackTrace();
 			    }
 			    if (Server.debug && queue.size() > 0) {
 				Server.warn("AsynchSender for " + session + " discarding " + 
 					    queue.size() + " messages.");
 			    }
 			    queue.clear();
 			}
 		    }
 		}
 	    } catch (InterruptedException ex2) {
 		if (!shutdown) exception = ex2;
 	    }
 
 	    if (exception != null) {
 		Server.warn(exception.toString());
 		exception.printStackTrace();
 		Server.warn("Exception in asynch-sender thread for " + session + " - quitting...");
 	    }
 	}
     }
 
 }
