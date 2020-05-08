 
 /**
  * Connections.java
  * 
  * @author Huy, Matthew
  */
 
 package hypeerweb;
 
 import java.util.HashSet;
 import java.util.NoSuchElementException;
 import java.util.Set;
 import java.util.SortedSet;
 import java.util.TreeSet;
 
 /**
  * This class is contained in a node, and makes it easier to access information
  * about it's owners connections.
  * 
  * <pre>
  * <b>Domain:</b>
  *      neighbors                  : Set<Node>
  *      surrogateNeighbors         : Set<Node>
  *      inversesurrogateNeighbors  : Set<Node>
  *      fold                       : Node
  *      surrogateFold              : Node
  *      inverseSurrogateFold       : Node
  *      
  * @author Trevor Bentley
  */
 public class Connections
 {
     public enum Action
     {   
         ADD_NEIGHBOR
         {
             public void notify(final Node actionNode, final Node node1, final Node node2)
             {
                 actionNode.addNeighbor(node1);
             }
         },
         REMOVE_NEIGHBOR
         {
             public void notify(final Node actionNode, final Node node1, final Node node2)
             {
                 actionNode.removeNeighbor(node1);
                 
                 //if(neighbor != parent) - if it's the parent it will say it's itself 
                 // and won't let it all the neighbors
                 actionNode.addDownPointer(node2);
                 node2.addUpPointer(actionNode);
             }
         },
         REPLACE_NEIGHBOR
         {
             public void notify(final Node actionNode, final Node node1, final Node node2)
             {
                 actionNode.removeNeighbor(node1);
                 actionNode.addNeighbor(node2);
             }
         },
         REMOVE_SURR_NEIGHBOR
         {
             public void notify(final Node actionNode, final Node node1, final Node node2)
             {
                 actionNode.removeDownPointer(node1);
             }
         },
         REPLACE_SURR_NEIGHBOR
         {
             public void notify(final Node actionNode, final Node node1, final Node node2)
             {
                 actionNode.removeDownPointer(node1);
                 actionNode.addDownPointer(node2);
             }
         },
         ADD_INV_SURR_NEIGHBOR
         {
             public void notify(final Node actionNode, final Node node1, final Node node2)
             {
                 actionNode.addUpPointer(node1);
             }
         },
         REMOVE_INV_SURR_NEIGHBOR
         {
             public void notify(final Node actionNode, final Node node1, final Node node2)
             {
                 actionNode.removeUpPointer(node1);
             }
         },
         REPLACE_INV_SURR_NEIGHBOR
         {
             public void notify(final Node actionNode, final Node node1, final Node node2)
             {
                 actionNode.removeUpPointer(node1);
                 actionNode.addUpPointer(node2);
             }
         };
         
         /**
          * @param actionNode - node that does the action
          * @param node1 - node to be added or removed (removed in replace methods)
          * @param node2 - node that should be added (Only used in replace methods)
          */
         public abstract void notify(final Node actionNode, final Node node1, final Node node2);
     }
     
     /**
      * Node's neighbors
      */
     private SortedSet<Node> neighbors;
     
     /**
      * Node's surrogate neighbors
      */
     private Set<Node> surrogateNeighbors;
     
     /**
      * Node's inverse surrogate neighbors
      */
     private Set<Node> inverseSurrogateNeighbors;
     
     /**
      * Node's fold
      */
     private Node fold;
     
     /**
      * Node's surrogate fold
      */
     private Node surrogateFold;
     
     /**
      * Node's inverse surrogate fold
      */
     private Node inverseSurrogateFold;
     
     /**
      * The default constructor.
      * 
      * @pre <i>None</i>
      * @post neighbors, surrogateNeighbors, and inverseSurrogateNeighbors sets are instantiated
      * fold, surrogateFold, and inverseSurrogateFold set to NULL_NODE singleton
      */
     public Connections() 
     {
         neighbors = new TreeSet<Node>();
         surrogateNeighbors = new TreeSet<Node>();
         inverseSurrogateNeighbors = new TreeSet<Node>();
         fold = Node.NULL_NODE;
         surrogateFold = Node.NULL_NODE;
         inverseSurrogateFold = Node.NULL_NODE;
     }
     
     /**
      * Set Fold
      * @obvious
      * @param node 
      */
     public void setFold(final Node node) 
     {
         // if node WebId is fold of this.WebId
         fold = node;
     }
 
     /**
      * Set surrogate fold
      * @obvious
      * @param node 
      */
     public void setSurrogateFold(final Node node) 
     {
         // if node WebId is surrogate fold of this.WebId
         surrogateFold = node;
     }
 
     /**
      * Set inverse surrogate fold
      * @obvious
      * @param node 
      */
     public void setInverseSurrogateFold(final Node node) 
     {
         inverseSurrogateFold = node;
     }
     
     /**
      * Add node to set of neighbors
      * @obvious
      * @param node 
      */
     public void addNeighbor(final Node node) 
     {
         // if WebIds are neighbors
         neighbors.add(node);
         
     }
 
     /**
      * Remove node from set of neighbors
      * @obvious
      * @param node 
      */
     public void removeNeighbor(final Node node) 
     {
         if(node != null) neighbors.remove(node);
     }
 
     /**
      * Add node to set of surrogate neighbors
      * @obvious
      * @param node 
      */
     public void addSurrogateNeighbor(final Node node) 
     {
         // if node WebId is surrogate neighbor of
         surrogateNeighbors.add(node);
     }
 
     /** 
      * Remove node from set of surrogate neighbors
      * @obvious
      * @param node 
      */
     public void removeSurrogateNeighbor(final Node node) 
     {
         if(node != null) surrogateNeighbors.remove(node);
     }
     
     /**
      * Add node to set of inverse surrogate neighbors
      * @obvious
      * @param node 
      */
     public void addInverseSurrogateNeighbor(final Node node)
     {
         // if this.WebId is surrogate neighbor of node WebId 
         inverseSurrogateNeighbors.add(node);
     }
 
     /**
      * Remove node from set of inverse surrogate neighbors
      * @obvious
      * @param node 
      */
     public void removeInverseSurrogateNeighbor(final Node node) 
     {
         if(node != null) inverseSurrogateNeighbors.remove(node);
     }
     
     /**
      * Get fold ID
      * @return fold id
      */
     public int getFoldId()
     {
         return fold == Node.NULL_NODE ? -1 : fold.getWebId();
     }
     
     /**
      * Get surrogate fold ID
      * @return surrogate fold id
      */
     public int getSurrogateFoldId()
     {
         return surrogateFold == Node.NULL_NODE ? -1 : surrogateFold.getWebId();
     }
     
     /**
      * Get inverse surrogate fold ID
      * @return inverse surrogate fold ID
      */
     public int getInverseSurrogateFoldId()
     {
         return inverseSurrogateFold == Node.NULL_NODE ? -1 : inverseSurrogateFold.getWebId();
     }
     
     /**
      * Get neighbor's IDs
      * @return set of neighbor's IDs
      */
     public HashSet<Integer> getNeighborsIds()
     {
         final HashSet<Integer> neighborsIds = new HashSet<Integer>();
         for (Node neighbor : neighbors)
         {
             neighborsIds.add(neighbor.getWebId());
         }
         
         return neighborsIds;
     }
     
     /**
      * Get surrogate neighbor's IDs
      * @return set of surrogate neighbor's IDs
      */
     public HashSet<Integer> getSurrogateNeighborsIds()
     {
         final HashSet<Integer> neighborsIds = new HashSet<Integer>();
         for (Node neighbor : surrogateNeighbors)
         {
             neighborsIds.add(neighbor.getWebId());
         }
         return neighborsIds;
     }
     
     /**
      * Get inverse surrogate neighbor's IDs
      * @return set of inverse surrogate neighbor's IDs
      */
     public HashSet<Integer> getInverseSurrogateNeighborsIds()
     {
         
         final HashSet<Integer> neighborsIds = new HashSet<Integer>();
         for (Node neighbor : inverseSurrogateNeighbors)
         {
             neighborsIds.add(neighbor.getWebId());
         }
         return neighborsIds;
     }
     
     /**
      * Get all neighbors
      * @return set of all neighbors
      */
     public Set<Node> getNeighbors()
     {
         return neighbors;
     }
     
     /**
      * Get all surrogate neighbors
      * @return set of all surrogate neighbors
      */
     public Set<Node> getSurrogateNeighbors()
     {
         return surrogateNeighbors;
     }
     
     /**
      * Get all inverse surrogate neighbors
      * @return set of all inverse surrogate neighbors
      */
     public Set<Node> getInverseSurrogateNeighbors()
     {
         return inverseSurrogateNeighbors;
     }
     
     /**
      * Get fold
      * @return fold
      */
     public Node getFold() 
     {
         return fold;
     }
 
     /**
      * Get surrogate fold
      * @return surrogate fold
      */
     public Node getSurrogateFold() 
     {
         return surrogateFold;
     }
 
     /**
      * Get inverse surrogate fold
      * @return inverse surrogate fold
      */
     public Node getInverseSurrogateFold() 
     {
         return inverseSurrogateFold;
     }
     
     /**
      * Returns a sorted set of all neighbors
      * 
      * @pre neighbors set exists
      * @post owner is added to end of neighbors set
      * @param owner 
      * @return sorted set of neighbors to the owner
      */
     private static SortedSet<Node> getLargerNeighbors(final Node owner)
     {
         return new TreeSet<Node>(owner.connections.neighbors.tailSet(owner));
     }
 
     /**
      * Returns the largest neighbor or NULL NODE if no such neighbor exists.
      * 
      * @pre neighbors set exists
      * @post largest neighbor is found and returned
      * @return largest neighbor or NULL NODE if no such neighbor exists.
      */
     public Node getHighestNeighbor()
     {
         return neighbors.size() > 0 ? neighbors.last() : Node.NULL_NODE;
     }
     
     /**
      * Returns the smallest neighbor or NULL NODE if no such neighbor exists.
      * 
      * @pre neighbors set exists
      * @post smallest neighbor is found and returned
      * @return smallest neighbor or NULL NODE if no such neighbor exists.
      */
     public Node getLowestNeighbor()
     {
         return neighbors.size() > 0 ? neighbors.first() : Node.NULL_NODE;
     }
 
     /**
      * Returns the largest surrogate neighbor
      * 
      * @pre surrogateNeighbors set exists
      * @post largest surrogate neighbor is found and returned
      * @return largest surrogate neighbor
      */
     public Node getHighestSurrogateNeighbor()
     {
        SortedSet<Node> sorted = new TreeSet<Node>(surrogateNeighbors);
        return sorted.last();
     }
 
     /**
      * Returns the smallest neighbor who doesn't have a child
      * 
      * @pre lists of neighbors exists
      * @post 
      * Option 1) Smallest neighbor without a child is found and returned 
      * Option 2) The smallest neighbor is returned
      * Option 3) NULL_NODE is returned if no neighbors exist
      * @return 
      * Option 1) Smallest neighbor without a child
      * Option 2) Smallest neighbor
      * Option 3) NULL_NODE 
      */
     public Node getLowestNeighborWithoutChild()
     {
         try
         {
             final int size = Math.max(neighbors.size(), neighbors.first().getNeighborsIds().size());
             for(Node neighbor: neighbors) if(neighbor.getHeight() < size) return neighbor;
             return neighbors.first();
         }
         catch(final NoSuchElementException e)
         {
             return Node.NULL_NODE;
         }
     }
     
     /**
      * Returns a Connections object will all new connections of child Node
      * 
      * @param parent 
      * @pre method is called by Parent Node
      * @post childConnections = 
      *      - neighbors = parent's inverse surrogate neighbors and neighbor
      *      - surrogate neighbors = parent's neighbors that are bigger than it
      *      - fold = parent's fold or inverse surrogate fold
      * @return child's connections
      */
     public Connections getChildConnections(final Node parent)
     {
        final Connections childConnections = new Connections();
        //Neighbors
        childConnections.neighbors = new TreeSet<Node>(inverseSurrogateNeighbors);
        childConnections.addNeighbor(parent);
        
        childConnections.surrogateNeighbors = getLargerNeighbors(parent);
        
        // Fold
        if (inverseSurrogateFold == Node.NULL_NODE) childConnections.fold = fold;
        else childConnections.fold = inverseSurrogateFold;
         
        return childConnections;
 	}
     
     /** Parent Notify */
     /**
      * Notify all of parent Node's inverse surrogate neighbors that it is no longer their 
      * surrogate neighbor. 
      * 
      * Clears all of parent's inverse surrogate neighbors 
      * 
      * Sets parent's fold
      * 
      * @param selfNode 
      * 
      * @pre parent Node calls parentNotify
      * @post all inverse surrogate neighbors are notified that parentNode is no longer their 
      * surrogate neighbor
      * 
      */
     public void parentNotify(final Node selfNode)
     {
         // Remove all inverse surrogate neighbors / surrogate neighbors connections of parent
         iterateInverseSurrogateNeighbors(selfNode,Node.NULL_NODE,Action.REMOVE_SURR_NEIGHBOR);
         inverseSurrogateNeighbors.clear();
         
         // Change parent Fold
         if(inverseSurrogateFold == Node.NULL_NODE)
         {
             surrogateFold = fold;
             fold = Node.NULL_NODE;
         }
         else inverseSurrogateFold = Node.NULL_NODE;
     }
     
     /** 
      * Child node notifies all of it's new connections how it is now
      * connected to them:
      *      - Fold
      *      - Neighbors
      *      - Surrogate Neighbors 
      *      
      * @param childNode 
      * @param parentNode 
      * 
      * @pre childNode has all of its Connections.
      * @post all Connections of childNode are notified of new Connection between childNode
      * and Node.
      */
     public void childNotify(final Node childNode, final Node parentNode)
 	{
         // Notify fold
         if(fold.connections.fold != Node.NULL_NODE)
         {
             fold.setInverseSurrogateFold(fold.connections.fold);
             fold.setFold(childNode);
         }
         else
         {
             fold.setFold(childNode);
             fold.setSurrogateFold(Node.NULL_NODE);
         }
         
         fold.setFold(childNode);
         
         // Notify other nodes of new connection
         // Add childNode as a new neighbor
         iterateNeighbors(childNode, Node.NULL_NODE, Action.ADD_NEIGHBOR);
         
         // Remove parentNode as a surrogate neighbor
         iterateNeighbors(parentNode, Node.NULL_NODE, Action.REMOVE_SURR_NEIGHBOR);
         
         // Add child as an inverse surrogate neighbor to all surrogate neighbors
         iterateSurrogateNeighbors(childNode, Node.NULL_NODE, Action.ADD_INV_SURR_NEIGHBOR);
 	}
     
     /**
      * Disconnect Deletion Point from HypeerWeb - Called from the deletionPoint node
      * Notifies all connections of Deletion Point to remove Deletion Point as one of their connections
      * 
      * @param deletionPoint - node to disconnect
      * @pre deletionPoint is a valid node in the HypeerWeb, deletionPoint is actually the deletion point
      * @post deletionPoint will be disconnected from the HyPeer web
      */
     public static void disconnect(final Node deletionPoint)
     { 
         final Node parent = getParent(deletionPoint);
                        
         // Remove me as a neighbor, set my parent as a surrogate neighbor and you as an inverse surrogate neighbor to parent
         deletionPoint.connections.iterateNeighbors(deletionPoint, parent, Action.REMOVE_NEIGHBOR);
         // Remove me as an inverse surrogate neighbor
         deletionPoint.connections.iterateSurrogateNeighbors(deletionPoint, Node.NULL_NODE, Action.REMOVE_INV_SURR_NEIGHBOR);
         
         if(deletionPoint.getFoldId() == parent.getWebId()) parent.connections.setFold(parent);
         else if (parent.connections.getSurrogateFold() == Node.NULL_NODE)
         {
             deletionPoint.connections.getFold().setSurrogateFold(parent);
             deletionPoint.connections.getFold().setFold(Node.NULL_NODE);
             parent.setInverseSurrogateFold(deletionPoint.connections.getFold());
         }
         else
         {
             deletionPoint.connections.getFold().setFold(parent);
             deletionPoint.connections.getFold().setInverseSurrogateFold(Node.NULL_NODE);
             parent.setFold(parent.connections.getSurrogateFold());
             parent.setSurrogateFold(Node.NULL_NODE);
         }
         
         // if deletion node is Cap Node set parent node to be the Cap Node
         if(deletionPoint.state == Node.State.CAP) parent.setState(Node.State.CAP);
         else if(deletionPoint.getFoldId() < parent.getWebId()) parent.setState(Node.State.DOWN);
     }
     
     /**
      * Replace selfNode with deletionPoint in the HypeerWeb
      * - deletionPoint node gets all information from selfNode 
      * 
      * @param selfNode - node to be replaced
      * @param deletionPoint - node to be replaced with
      */
     //called on a node to be deleted
     public static void replace(final Node selfNode, final Node deletionPoint)
     {
         // Give deletion Point all the selfNode's connections
         deletionPoint.connections = selfNode.connections;
         deletionPoint.setWebId(new WebId(selfNode.getWebId()));
         deletionPoint.state = selfNode.state;
         
         // Replace selfNode with deletionPoint node in all connections
         selfNode.connections.iterateNeighbors(selfNode, deletionPoint, Action.REPLACE_NEIGHBOR);
         selfNode.connections.iterateSurrogateNeighbors(selfNode, deletionPoint, Action.REPLACE_INV_SURR_NEIGHBOR);
         selfNode.connections.iterateInverseSurrogateNeighbors(selfNode, deletionPoint, Action.REPLACE_SURR_NEIGHBOR);
         
         // Replace as Fold
         if(deletionPoint.getFold() != Node.NULL_NODE)
             deletionPoint.getFold().setFold(deletionPoint);
         // Replace as Inverse Surrogate Fold
         if(deletionPoint.connections.getSurrogateFold() != Node.NULL_NODE)
             deletionPoint.connections.getSurrogateFold().setInverseSurrogateFold(deletionPoint);
         // Replace as Surrogate Fold of Inverse Surrogate Fold
         if(deletionPoint.connections.getInverseSurrogateFold() != Node.NULL_NODE)
             deletionPoint.connections.getInverseSurrogateFold().setSurrogateFold(deletionPoint);
         
     }
 
     /**
      *  Find parent node
      * @param node - child node
      * @return parent node
      */
     private static Node getParent(final Node node)
     {
         final Node parent = node.getLowestNeighbor();
         return parent == Node.NULL_NODE ? node : parent;
     }
     
     /**
      * Iterate through all neighbors and perform the desired action
      * 
      * @param node1 - Node to be inserted or removed
      * @param node2 - NULL_NODE or node that will replace node1 
      * @param action - what action to perform
      */
     public void iterateNeighbors(final Node node1, final Node node2, final Action action)
     {
         for (Node neighbor : neighbors)
             action.notify(neighbor,node1,node2);
     }
     
     /** 
      * Iterate through all the surrogate neighbors and perform the desired action
      * 
      * @param node1 - Node to be inserted or removed
      * @param node2 - NULL_NODE or node that will replace node1 
      * @param action - what action to perform
      */
     public void iterateSurrogateNeighbors(final Node node1, final Node node2, final Action action)
     {
         for (Node surrogateNeighbor : surrogateNeighbors) action.notify(surrogateNeighbor,node1,node2);
     }
     
     /** 
      * Iterate through all the inverse surrogate neighbors and perform the desired action
      * 
      * @param node1 - Node to be inserted or removed
      * @param node2 - NULL_NODE or node that will replace node1 
      * @param action - what action to perform
      */
     public void iterateInverseSurrogateNeighbors(final Node node1, final Node node2, final Action action)
     {
         for (Node inverseSurrogateNeighbor : inverseSurrogateNeighbors) action.notify(inverseSurrogateNeighbor, node1, node2);
     }
 }
 
