 //---------------------------------------------------------------------------
 //  $Revision$ 
 //  $Date$
 //  $Author$
 //---------------------------------------------------------------------------
 package cytoscape.data;
 //---------------------------------------------------------------------------
 import java.util.*;
 import java.io.*;
 
 import giny.model.GraphPerspective;
 import giny.model.GraphPerspectiveChangeListener;
 import giny.model.GraphPerspectiveChangeEvent;
 import giny.model.Node;
 import giny.model.Edge;
 import giny.filter.Filter;
 //---------------------------------------------------------------------------
 /**
  * This class implements the ability to attach a flag to every node or
  * edge in a GraphPerspective. The flag can be either on or off. Methods are
  * provided for inspecting the current state of any graph object, for
  * setting the state, or getting the full set of currently flagged
  * nodes or edges. This functionality is often used to identify a set
  * of interesting nodes or edges in the graph.<P>
  *
  * A non-null GraphPerspective reference is required to construct an instance
  * of this class. This class will listen to the graph to respond to
  * the removal of graph objects. A currently flagged object that is
  * removed from the graph will lose its flag, even if it is later
  * added back to the graph.<P>
  *
  * When the state of a node or edge is changed, a event of type FlagEvent
  * is fired. When a group of nodes or edges are changed together in a single
  * operation, one event will be fired for the whole group (but separate
  * events for nodes and edges). Note: a listener should not be removed from
  * this object in response to the firing of an event, as this may cause
  * a ConcurrentModificationException.<P>
  *
  * WARNING: for performance reasons, the set of objects returned by the
  * getSelectedXX methods is the actual data object, not a copy. Users should
  * not directly modify these sets.<P>
  *
  * Performance note: the implementation is a HashSet of flagged objects,
  * so most methods are O(1). Operations on groups of nodes are O(N) where
  * N is either the number of flagged objects or the number of objects in
  * the graph, as applicable.<P>
  */
 public class FlagFilter implements Filter, GraphPerspectiveChangeListener {
     
     GraphPerspective graph;
     Set flaggedNodes = new HashSet();
     Set flaggedEdges = new HashSet();
     List listeners = new ArrayList();
     
     /**
      * Standard Constructor. The argument is the graph that this filter will
      * apply to; it cannot be null.
      *
      * @throws NullPointerException  if the argument is null.
      */
     public FlagFilter(GraphPerspective graph) {
         this.graph = graph;
         //this throws a NullPointerException if the graph is null
         graph.addGraphPerspectiveChangeListener(this);
     }
     
     /**
      * Returns the set of all flagged nodes in the referenced GraphPespective.<P>
      *
      * WARNING: the returned set is the actual data object, not a copy. Don't
      * directly modify this set.
      */
     public Set getFlaggedNodes() {return flaggedNodes;}
     /**
      * Returns the set of all flagged edges in the referenced GraphPespective.<P>
      *
      * WARNING: the returned set is the actual data object, not a copy. Don't
      * directly modify this set.
      */
     public Set getFlaggedEdges() {return flaggedEdges;}
     
     
     /**
      * Returns true if the argument is a flagged Node in the referenced
      * GraphPerspective, false otherwise.
      */
     public boolean isFlagged(Node node) {return flaggedNodes.contains(node);}
     /**
      * Returns true if the argument is a flagged Edge in the referenced
      * GraphPerspective, false otherwise.
      */
     public boolean isFlagged(Edge edge) {return flaggedEdges.contains(edge);}
     
     /**
      * Implementation of the Filter interface. Returns true if the argument
      * is a flagged Node or Edge in the referenced GraphPerspective, false otherwise.
      */
     public boolean passesFilter(Object o) {
         if (flaggedNodes.contains(o) || flaggedEdges.contains(o)) {
             return true;
         } else {
             return false;
         }
     }
     
     
     /**
      * If the first argument is a Node in the referenced GraphPerspective,
      * sets its flagged state to the value of the second argument. An event
      * will be fired iff the new state is different from the old state.
      *
      * @return  true if an actual change was made, false otherwise
      */
     public boolean setFlagged(Node node, boolean newState) {
         if (newState == true) {//set flag to on
             //don't flag the node if it's not in the graph
             if (!graph.containsNode(node)) {return false;}
             boolean setChanged = flaggedNodes.add(node);
             if (setChanged) {fireEvent(node, true);}
             return setChanged;
         } else {//set flag to off
             //a node can't be flagged unless it's in the graph
             boolean setChanged = flaggedNodes.remove(node);
             if (setChanged) {fireEvent(node, false);}
             return setChanged;
         }
     }
     
     /**
      * If the first argument is an Edge in the referenced GraphPerspective,
      * sets its flagged state to the value of the second argument. An event
      * will be fired iff the new state is different from the old state.
      *
      * @return  true if an actual change was made, false otherwise
      */
     public boolean setFlagged(Edge edge, boolean newState) {
         if (newState == true) {//set flag to on
             //don'tflagthe edge if it's not in the graph
             if (!graph.containsEdge(edge)) {return false;}
             boolean setChanged = flaggedEdges.add(edge);
             if (setChanged) {fireEvent(edge, true);}
             return setChanged;
         } else {//set flag to off
             //an edge can't be flagged unless it's in the graph
             boolean setChanged = flaggedEdges.remove(edge);
             if (setChanged) {fireEvent(edge, false);}
             return setChanged;
         }
     }
     
     /**
      * Sets the flagged state defined by the second argument for all Nodes
      * contained in the first argument, which should be a Collection of Node objects
      * contained in the referenced GraphPerspective. One event will be fired
      * for the full set of changes. This method does nothing if the first
      * argument is null.
      *
      * @return a Set containing the objects for which the flagged state changed
      * @throws ClassCastException  if the first argument contains objects other
      *                             than giny.model.Node objects
      */
     public Set setFlaggedNodes(Collection nodesToSet, boolean newState) {
         
       //System.out.println( "SettingFlaggedNodes" );
       
       Set returnSet = new HashSet();
         if (nodesToSet == null) {return returnSet;}
         if (newState == true) {
             for (Iterator i = nodesToSet.iterator(); i.hasNext(); ) {
                 Node node = (Node)i.next();
 
                 //System.out.println( "Flagging node"+node );
 
                 if (!graph.containsNode(node)) {continue;}
                 boolean setChanged = flaggedNodes.add(node);
                 if (setChanged) {returnSet.add(node);}
             }
             if (returnSet.size() > 0) {fireEvent(returnSet, true);}
         } else {
             for (Iterator i = nodesToSet.iterator(); i.hasNext(); ) {
                 Node node = (Node)i.next();
                 //System.out.println( "UNFlagging node"+node );
                 boolean setChanged = flaggedNodes.remove(node);
                 if (setChanged) {
                   //System.out.println( setChanged+" Set Changed: "+node);
                   returnSet.add(node);
                 }
             }
             if (returnSet.size() > 0) {fireEvent(returnSet, false);}
         }
         return returnSet;
     }
     
     /**
      * Sets the flagged state defined by the second argument for all Edges
      * contained in the first argument, which should be a Collection of Edge objects
      * contained in the referenced GraphPerspective. One event will be fired
      * for the full set of changes. This method does nothing if the first
      * argument is null.
      *
      * @return a Set containing the objects for which the flagged state changed
      * @throws ClassCastException  if the first argument contains objects other
      *                             than giny.model.Edge objects
      */
     public Set setFlaggedEdges(Collection edgesToSet, boolean newState) {
         Set returnSet = new HashSet();
         if (edgesToSet == null) {return returnSet;}
         if (newState == true) {
             for (Iterator i = edgesToSet.iterator(); i.hasNext(); ) {
                 Edge edge = (Edge)i.next();
                 if (!graph.containsEdge(edge)) {continue;}
                 boolean setChanged = flaggedEdges.add(edge);
                 if (setChanged) {returnSet.add(edge);}
             }
             if (returnSet.size() > 0) {fireEvent(returnSet, true);}
         } else {
             for (Iterator i = edgesToSet.iterator(); i.hasNext(); ) {
                 Edge edge = (Edge)i.next();
                 boolean setChanged = flaggedEdges.remove(edge);
                 if (setChanged) {returnSet.add(edge);}
             }
             if (returnSet.size() > 0) {fireEvent(returnSet, false);}
         }
         return returnSet;
     }
     
     /**
      * Sets the flagged state to true for all Nodes in the GraphPerspective.
      */
     public void flagAllNodes() {
         Set changes = new HashSet();
         for (Iterator i = graph.nodesIterator(); i.hasNext(); ) {
             Node node = (Node)i.next();
             boolean setChanged = flaggedNodes.add(node);
             if (setChanged) {changes.add(node);}
         }
         if (changes.size() > 0) {fireEvent(changes, true);}
     }
     
     /**
      * Sets the flagged state to true for all Edges in the GraphPerspective.
      */
     public void flagAllEdges() {
         Set changes = new HashSet();
         for (Iterator i = graph.edgesIterator(); i.hasNext(); ) {
             Edge edge = (Edge)i.next();
             boolean setChanged = flaggedEdges.add(edge);
             if (setChanged) {changes.add(edge);}
         }
         if (changes.size() > 0) {fireEvent(changes, true);}
     }
     
     /**
      * Sets the flagged state to false for all Nodes in the GraphPerspective.
      */
     public void unflagAllNodes() {
         if (flaggedNodes.size() == 0) {return;}
         Set changes = new HashSet(flaggedNodes);
         flaggedNodes.clear();
         fireEvent(changes, false);
     }
     
     /**
      * Sets the flagged state to false for all Edges in the GraphPerspective.
      */
     public void unflagAllEdges() {
         if (flaggedEdges.size() == 0) {return;}
         Set changes = new HashSet(flaggedEdges);
         flaggedEdges.clear();
         fireEvent(changes, false);
     }
     
     
     /**
      * Implementation of the GraphPerspectiveChangeListener interface. Responds
      * to the removal of nodes and edges by removing them from the set of
      * flagged graph objects if needed. Fires an event only if there was an
      * actual change in the current flagged set.
      */
     public void graphPerspectiveChanged(GraphPerspectiveChangeEvent event) {
         //careful: this event can represent both hidden nodes and hidden edges
         //if a hide node operation implicitly hid its incident edges
         Set nodeChanges = null; //only create the set if we need it
         if ( event.isNodesHiddenType() ) {//at least one node was hidden
             Node[] hiddenNodes = event.getHiddenNodes();
             for (int index=0; index<hiddenNodes.length; index++) {
                 Node node = hiddenNodes[index];
                 boolean setChanged = flaggedNodes.remove(node);
                 if (setChanged) {//the hidden node was actually flagged
                     if (nodeChanges == null) {nodeChanges = new HashSet();}
                     nodeChanges.add(node); //save change for the event we'll fire
                 }
             }
         }
         if (nodeChanges != null && nodeChanges.size() > 0) {
             fireEvent(nodeChanges, false);
         }
         Set edgeChanges = null; //only create the set if we need it
         if ( event.isEdgesHiddenType() ) {//at least one edge was hidden
             //GINY bug: sometimes we get an event that has valid edge indices
             //but the Edge array contains null objects
             //for now, get around this by converting indices to edges ourselves
            GraphPerspective source = (GraphPerspective)event.getSource();
             int[] indices = event.getHiddenEdgeIndices();
             for (int index = 0; index < indices.length; index++) {
                Edge edge = source.getRootGraph().getEdge(indices[index]);
                 boolean setChanged = flaggedEdges.remove(edge);
                 if (setChanged) {//the hidden edge was actually flagged
                     if (edgeChanges == null) {edgeChanges = new HashSet();}
                     edgeChanges.add(edge); //save change for the event we'll fire
                 }
             }
             /*this is the code that sometimes doesn't work
             Edge[] hiddenEdges = event.getHiddenEdges();
             for (int index=0; index<hiddenEdges.length; index++) {
                 Edge edge = hiddenEdges[index];
                 boolean setChanged = flaggedEdges.remove(edge);
                 if (setChanged) {
                     if (edgeChanges == null) {edgeChanges = new HashSet();}
                     edgeChanges.add(edge);
                 }
             }
             */
         }
         if (edgeChanges != null && edgeChanges.size() > 0) {
             fireEvent(edgeChanges, false);
         }
     }
     
     /**
      * If the argument is not already a listener to this object, it is added.
      * Does nothing if the argument is null.
      */
     public void addFlagEventListener(FlagEventListener listener) {
         if (listener != null) {listeners.add(listener);}
     }
     
     /**
      * If the argument is a listener to this object, removes it from the list
      * of listeners.
      */
     public void removeFlagEventListener(FlagEventListener listener) {
         listeners.remove(listener);
     }
     
     /**
      * Fires an event to all registered listeners that represents the operation
      * described by the arguments. The first argument should be the graph object
      * whose flagged state changed, or a Set of such objects. The second argument
      * identifies the change; true for setting a flag and false for removing it.
      * Creates a suitable event and passes it to all listeners.
      */
     protected void fireEvent(Object target, boolean selectOn) {
         //assert(target != null);//should never get called with null target
         FlagEvent event = new FlagEvent(this, target, selectOn);
         for (Iterator i = this.listeners.iterator(); i.hasNext(); ) {
             FlagEventListener listener = (FlagEventListener)i.next();
             listener.onFlagEvent(event);
         }
     }
     
             
 }
 
