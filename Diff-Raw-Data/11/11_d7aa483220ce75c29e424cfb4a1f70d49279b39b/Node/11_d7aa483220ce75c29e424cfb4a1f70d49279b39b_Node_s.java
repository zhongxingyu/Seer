 
 /** 
  * Surrogate neighbors are called DownPointers.
  * Inverse surrogate neighbors are called UpPointers.
  * Refer to: http://students.cs.byu.edu/~cs340ta/spring2012/projects/hypeerwebdesc/ 
  * 
  * Node.java
  * @author Trevor Bentley, Brian Davis, Matthew
  * 
  * */
 
 package dbPhase.hypeerweb;
 
 import java.util.HashSet;
 import java.util.Iterator;
 
 public class Node
 {
     /**
      * This represents the state of the node in the cap node finding algorithm.
     * The findCapNode method is called to produce the correct behavoir at each step.
      * @author Matthew, Brian, Trevor
      *
      */
     public enum State
     {   
         CAP
         {
             @Override
             public Node findCapNode(Node n)
             {
                 return n;
             }
             
             public State getInitialStateofChild()
             {
                 return CAP;
             }
             public State getNextState()
             {
                 return STANDARD;
             }
         },
         DOWN
         {
             @Override
             public Node findCapNode(Node n)
             {
                 return n.getHighestSurrogateNeighbor();
             }
             
             public State getInitialStateofChild()
             {
                 return DOWN;
             }
             
             public State getNextState()
             {
                 return STANDARD;
             }
         },
         STANDARD
         {
             @Override
             public Node findCapNode(Node n)
             {
                 return n.getHighestNeighbor();//Or highest fold?
             }
             
             public State getInitialStateofChild()
             {
                 return DOWN;
             }
             
             public State getNextState()
             {
                 return STANDARD;
             }
         };
         
         public abstract Node findCapNode(Node n);
         public abstract State getInitialStateofChild();
         public abstract State getNextState();
     }
     
     /**
     * state represents this node's position (and next action) in the cap node locating algorithm
      */
     protected State state;
     
     /**
      * This node's webId
      */
     protected WebId webid;
     
     /**
      * All of this nodes relations to other nodes (neighbors, folds, surrogates, etc.)
      */
     protected Connections connections;
     
 	public static final Node NULL_NODE = null;
 	
 	/**
 	 * Constructor for a new node.
 	 * It's webId is given by the parameter.
 	 * @param i
 	 */
 	public Node(final int i) //Wait, how would we even know what it's webId is supposed to be?
 	{
 		webid = new WebId(i);
 	}
 	
 	/**
 	 * @obvious
 	 * @return
 	 */
 	public Node getHighestNeighbor() {
         return connections.getHighestNeighbor();
     }
 	
 	/**
 	 * @obvious
 	 * @return
 	 */
     public Node getHighestSurrogateNeighbor() {
         return connections.getHighestSurrogateNeighbor();
     }
     
     /**
     * Returns fully initailized SimplifiedNodeDomain needed for testing
      * 
     * @pre this node is initailized
      * @post no changes to this node
      * 
      * @return SimplifiedNodeDomain representing this node
      */
     public SimplifiedNodeDomain constructSimplifiedNodeDomain() 
 	{
 	    final HashSet<Integer> intNeighbors = new HashSet<Integer>();
 	    final HashSet<Integer> intSurrogateNeighbors = new HashSet<Integer>();;
 	    final HashSet<Integer> intInverseSurrogateNeighbors = new HashSet<Integer>();;
 	    int tempFold = -1;
 	    int tempSurrogateFold = -1;
 	    int tempInverseSurrogateFold = -1;
 	    
 	    
 	    //convert HashSets to integer hash sets using iteration
 	    Iterator<Node> iter = connections.getNeighbors().iterator();
 	    while(iter.hasNext())
 	    {
 	        final Node temp = iter.next();
 	        intNeighbors.add(temp.webid.getValue());
 	    }
 	    
 	    iter = connections.getSurrogateNeighbors().iterator();
 	    while(iter.hasNext())
         {
             final Node temp = iter.next();
             intSurrogateNeighbors.add(temp.webid.getValue());
         }
 	    
 	    iter = connections.getInverseSurrogateNeighbors().iterator();
         while(iter.hasNext())
         {
             final Node temp = iter.next();
             intInverseSurrogateNeighbors.add(temp.webid.getValue());
         }
         
         Node fold = connections.getFold();
         Node surrogateFold = connections.getSurrogateFold();
         Node inverseSurrogateFold = connections.getInverseSurrogateFold();
         
         if(fold != NULL_NODE) tempFold = fold.webid.getValue();
         if(surrogateFold != NULL_NODE) tempSurrogateFold = surrogateFold.webid.getValue();
         if(inverseSurrogateFold != NULL_NODE) tempInverseSurrogateFold = inverseSurrogateFold.webid.getValue();
 	    
 	    final SimplifiedNodeDomain simpleNode = new SimplifiedNodeDomain( webid.getValue(),
                                                         		        webid.getHeight(),
                                                                         intNeighbors,
                                                                         intInverseSurrogateNeighbors, 
                                                                         intSurrogateNeighbors,
                                                                         tempFold, 
                                                                         tempSurrogateFold,
                                                                         tempInverseSurrogateFold);
 		return simpleNode;
 	}
 	
 	/**
 	 * @obvious
 	 * @param webId
 	 */
 	public void setWebId(final WebId webId) 
 	{
 		webid = webId;
 	}
 	
 	/**
 	 * @obvious
 	 * @param state
 	 */
 	public void setState(State state)
 	{
 	    this.state = state;
 	}
 	
 	/**
 	 * @obvious
 	 * @param node
 	 */
 	public void setFold(final Node node) 
 	{
 	    // if node WebId is fold of this.WebId
 	    connections.setFold(node);
 	}
 	
 	/**
 	 * @obvious
 	 * @param node
 	 */
 	public void setSurrogateFold(final Node node) 
 	{
 	    // if node WebId is surrogate fold of this.WebId
 		connections.setSurrogateFold(node);
 	}
 	
 	/**
 	 * @obvious
 	 * @param node
 	 */
 	public void setInverseSurrogateFold(final Node node) 
 	{
 	    connections.setInverseSurrogateFold(node);
 	}
 	
 	/**
 	 * @obvious
 	 * @param node
 	 */
 	public void addNeighbor(final Node node) 
 	{
 	    // if WebIds are neighbors
 	    connections.addNeighbor(node);
 	}
 	
 	/**
 	 * @obvious
 	 * @param node
 	 */
 	public void removeNeighbor(final Node node) 
 	{
 	    connections.removeNeighbor(node);
 	}
 	
 	/**
 	 * @obvious
 	 * @return int representation of the node's web id
 	 */
 	public int getWebId()
 	{
 	    return webid.getValue();
 	}
 	
 	/**
 	 * @obvious
 	 * @return
 	 */
 	public int getHeight()
 	{
 	    return webid.getHeight();
 	}
 	
 	
 	/**
      * @obvious
      * @param node0
      */
     public void addUpPointer(final Node node0)
     {
         connections.addInverseSurrogateNeighbor(node0);
     }
     
     /**
      * @obvious
      * @param node
      */
     public void removeUpPointer(final Node node)
     {
         connections.removeInverseSurrogateNeighbor(node);
     }
     
     /**
      * @obvious
      * @param node
      */
     public void addDownPointer(final Node node)
     {
         connections.addSurrogateNeighbor(node);
     }
     
     /**
      * @obvious
      * @param node
      */
     public void removeDownPointer(final Node node)
     {
         connections.removeSurrogateNeighbor(node);
     }
     
     /**
      * @obvious
      * @param node
      */
     public HashSet<Integer> getNeighborsIds()
     {
         return connections.getNeighborsIds();
     }
     
     /**
      * @obvious
      * @return copy of surrogate neighbor set
      */
     public HashSet<Integer> getSurNeighborsIds()
     {
         return connections.getSurrogateNeighborsIds();
     }
     
     /**
      * @obvious
      * @return copy of inverse surrogate neighbor set
      */
     public HashSet<Integer> getInvSurNeighborsIds()
     {
         return connections.getInverseSurrogateNeighborsIds();
     }
     
     
     /**
      * insertSelf
      * This method is called on a new node that hasn't been put into the HyPeerWeb yet.
      * 
      * @pre start node is part of a valid HyPeerWeb. This node is not part of the HyPeerWeb.
      * @post This node will be part of the HyPeerWeb and all connections will be modified to match the project constraints
      * @param startNode
      */
     public void insertSelf(Node startNode)
     {
         Node parent = findInsertionPoint(startNode);
         webid = new WebId((int) (parent.webid.getValue() + 
                 Math.pow(2, parent.getNeighborsIds().size())));
         
         // Give child its' connections.
         connections = Connections.extractChildConnections(parent);
         connections.addNeighbor(parent);
         
         // Update states
         setState(parent.state.getInitialStateofChild());
         parent.setState(parent.state.getNextState());
         
         // Child Notify
         connections.childNotify(this);
         
         // Parent Notify
         parent.connections.parentNotify(parent);
     }
 
     private Node findInsertionPoint(Node startNode) {
         Node currentNode = startNode.state.findCapNode(startNode);
         //This loop controls the stepping of the algorithm finding the cap node
         while(currentNode != startNode){
             startNode = currentNode;
             currentNode = currentNode.state.findCapNode(currentNode);
         }//The cap node is now found (currentNode).
         
         while (currentNode.getLowestNeighborWithoutChild() != Node.NULL_NODE)
         {
             currentNode = currentNode.getLowestNeighborWithoutChild();
         }
         return currentNode;
     }
     
     public Node getLowestNeighborWithoutChild() {
         return connections.getLowestNeighborWithoutChild();
     }
     
     //Do we still want this?
     public int getChildsWebId()
     {
         int bit = 1 << (getHeight()-1);
         return webid.getValue() | bit;
         
     }
     
     public int getFoldId()
     {
         return connections.getFoldId() ;
     }
     
     public int getSurrogateFoldId()
     {
         return connections.getSurrogateFoldId();
     }
     
     public int getInverseSurrogateFoldId()
     {
         return connections.getInverseSurrogateFoldId();
     }
     
 
 }
