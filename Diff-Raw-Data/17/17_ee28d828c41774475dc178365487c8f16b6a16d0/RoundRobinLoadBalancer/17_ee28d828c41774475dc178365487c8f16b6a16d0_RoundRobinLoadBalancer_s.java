 package org.imirsel.nema.flowservice;
 
 import java.util.logging.Logger;
 
 import net.jcip.annotations.ThreadSafe;
 
 
 /**
  * A {@link MeandreLoadBalancer} implementation that uses a round robin
  * algorithm to deal out servers. Implemented as a circular doubly linked list.
  *
  * @author shirk
  * @since 0.4.0
  */
 @ThreadSafe
 public class RoundRobinLoadBalancer implements MeandreLoadBalancer {
 
    private static final Logger logger = 
 	   Logger.getLogger(RoundRobinLoadBalancer.class.getName());
 	
    //~ Instance fields ---------------------------------------------------------
 
    /** Pointer to the current node the load balancer is on. */
    Node curr = null;
 
    /** Number of servers the load balancer is managing. */
    int size = 0;
 
    //~ Constructors ------------------------------------------------------------
 
    /**
     * Creates a new {@link RoundRobinLoadBalancer} instance.
     */
    public RoundRobinLoadBalancer() {
    }
 
    //~ Methods -----------------------------------------------------------------
 
 
    /**
     * @see MeandreLoadBalancer#addServer(MeandreServerProxy)
     */
    public synchronized void addServer(MeandreServerProxy server) {
 	   // do not allow duplicate servers
 	  if(findNodeWithServer(server)!=null) {
 		  return;
 	  }
       Node node = new Node(server);
       if (size == 0) {
          node.setNext(node);
          node.setPrev(node);
          curr = node;
       } else {
          node.setNext(curr.getNext());
          curr.getNext()
          .setPrev(node);
          node.setPrev(curr);
          curr.setNext(node);
       }
       logger.fine("Added server " + server + " to load balancer");
       size++;
    }
 
    /**
     * @see MeandreLoadBalancer#removeServer(MeandreServerProxy)
     */
   public synchronized void removeServer(MeandreServerProxy server)
         throws IllegalStateException {
       if (size == 0) {
          return;
       }
 
       Node match = findNodeWithServer(server);
 
       if (match == null) {
          return;
       }
 
       if (server.getNumJobsRunning() != 0) {
          logger.warning("Server " + server.toString() + " will be removed " +
          		"from the load balancer, but it is still running jobs.");
       }
 
       if (size == 1) {
          curr = null;
       } else {
          if (curr == match) {
             curr = curr.getNext();
          }
          match.getNext().setPrev(match.getPrev());
          match.getPrev().setNext(match.getNext());
       }
       logger.fine("Removed server " + server + " from load balancer");
       size--;
    }
 
    /**
     * Finds the list {@link Node} that contains the specified
     * {@link MeandreServerProxy}.
     *
     * @param server The {@link MeandreServerProxy} to search for in the list
     * {@link Node}s.
     * @return List {@link Node} that contains the specified
     * {@link MeandreServerProxy}.
     */
    private Node findNodeWithServer(MeandreServerProxy server) {
       if (size == 0) {
          return null;
       }
 
       Node headNode = curr;
       Node iterNode = curr;
 
       do {
          if (iterNode.getServer().equals(server)) {
             return iterNode;
          }
          iterNode = iterNode.getNext();
 
       } while (!iterNode.equals(headNode));
 
       return null;
    }
 
    /**
     * @see MeandreLoadBalancer#nextAvailableServer()
     */
    @Override
    public synchronized MeandreServerProxy nextAvailableServer() {
       if (size == 0) {
          return null;
       }
 
       assert curr != null : "Current node should not be null";
 
       logger.fine("Finding the next available server.");
       
       Node headNode = curr.getNext();
       Node iterNode = headNode;
 
       do {
         if (!iterNode.getServer().isBusy()) {
             curr = iterNode;
        	MeandreServerProxy server = iterNode.getServer();
        	logger.fine("Next available server: " + server);
             return server;
          }
          iterNode = iterNode.getNext();

       } while (!iterNode.equals(headNode));
 
       return null;
    }
 
    /**
     * @see MeandreLoadBalancer#nextAvailableServer()
     */
    @Override
    public synchronized boolean hasAvailableServer() {
       if (size == 0) {
          return false;
       }
 
       assert curr != null : "Current node should not be null";
 
       Node headNode = curr.getNext();
       Node iterNode = headNode;
 
       do {
         if (!iterNode.getServer().isBusy()) {
             return true;
          }
          iterNode = iterNode.getNext();
 
       } while (!iterNode.equals(headNode));
 
       return false;
    }
    
    /**
     * @see MeandreLoadBalancer#size()
     */
    public synchronized int size() {
 	   return size;
    }
    
    //~ Inner Classes -----------------------------------------------------------
 
    /**
     * Doubly linked list node.
     */
    private class Node {
       Node next = null;
       Node prev = null;
       MeandreServerProxy server = null;
 
       public Node(MeandreServerProxy server) { this.server = server; }
 
       public void setNext(Node next) { this.next = next; }
 
       public Node getNext() { return next; }
 
       public void setPrev(Node prev) { this.prev = prev; }
 
       public Node getPrev() { return prev; }
 
       public MeandreServerProxy getServer() { return server; }
 
       @Override
       public int hashCode() {
          final int prime = 31;
          int result = 1;
          result = (prime * result) + ((server == null) ? 0 : server.hashCode());
          return result;
       }
 
       @Override
       public boolean equals(Object obj) {
          if (this == obj) {
             return true;
          }
          if (obj == null) {
             return false;
          }
          if (getClass() != obj.getClass()) {
             return false;
          }
 
          final Node other = (Node) obj;
          if (server == null) {
             if (other.server != null) {
                return false;
             }
          } else if (!server.equals(other.server)) {
             return false;
          }
          return true;
       }
 
    }
 
 }
