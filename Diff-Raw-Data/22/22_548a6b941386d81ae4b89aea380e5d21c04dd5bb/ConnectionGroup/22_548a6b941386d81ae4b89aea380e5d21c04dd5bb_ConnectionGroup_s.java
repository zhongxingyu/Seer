 
 package xella.net;
 
 import java.io.*;
 import java.nio.*;
 import java.nio.channels.*;
 import java.util.*;
 
 /**
  * Class that represents a group of connections
  */
 
 class ConnectionGroup {
 
     private Selector selector;
     private Set connections;
     private int minSize;
     private int maxSize;
 
     public ConnectionGroup(int minSize, int maxSize)
 	throws IOException 
     {
 	this.minSize = minSize;
 	this.maxSize = maxSize;
 	this.connections = new HashSet();
 	this.selector = Selector.open();
     }
     
     public synchronized void addConnection(GnutellaConnection connection) 
 	throws IOException 
     {
 	if (connection.getChannel().isOpen()) {
 	    connections.add(connection);
 	    SelectionKey key = connection.getChannel().register(selector, 
 								connection.getChannel().validOps()); 
 	    key.attach(connection);	
 	}
     }
     
     public synchronized void removeConnection(GnutellaConnection connection) 
     {
 	connections.remove(connection);
 	SelectionKey key = connection.getChannel().keyFor(selector);
 	if (key != null) {
 	    key.cancel();
 	}
     }
 
     /**
      * Block until one or more connections are ready for read/write or connect operations
      * @return the connections ready
      */
 
     public Collection waitForReadyConnections() 
 	throws IOException
     {
 	selector.select();
 	Collection toReturn = new ArrayList();
	Iterator selectedKeys = selector.selectedKeys().iterator();
	while (selectedKeys.hasNext()) {
	    SelectionKey key = (SelectionKey) selectedKeys.next();
 	    toReturn.add(key.attachment());
 	}	
 	return toReturn;
     }
 
     /**
      * @return the connections ready for i/o operations (non-blocking method)
      */
 
     public Collection getReadyConnections() 
 	throws IOException
     {
 	Collection toReturn = new ArrayList();
 	selector.selectNow();
	Iterator selectedKeys = selector.selectedKeys().iterator();
	while (selectedKeys.hasNext()) {
	    SelectionKey key = (SelectionKey) selectedKeys.next();
 	    GnutellaConnection connection = (GnutellaConnection) key.attachment();
 	    toReturn.add(connection);
 	    
 	    String str = "Connection " + connection.getConnectionNumber() + " is ready for ";
 	    if (key.isAcceptable()) {
 		str += "ACCEPT ";
 	    }
 	    if (key.isConnectable()) {
 		str += "CONNECT ";
 	    }
 	    if (key.isReadable()) {
 		str += "READ ";
 	    }
 	    if (key.isWritable()) {
 		str += "WRITE ";
 	    }
 	    if (!key.isValid()) {
 		str += "(invalid key) ";
 	    }
 	    System.out.println(str);
 
	}	
 	return toReturn;
     }
 
     public Iterator iterator() {
 	return new ArrayList(connections).iterator();
     }
 
     public synchronized boolean isSmallerThanMinSize() {
 	return connections.size() < minSize;
     }
 
     public synchronized boolean hasReachedMaxSize() {
 	return connections.size() >= maxSize;
     }
 
     public synchronized int size() {
 	return connections.size();
     }
 }
