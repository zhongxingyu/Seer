 package autograph;
 import java.util.ArrayList;
 import java.util.Random;
 import java.io.Serializable;
 
 import autograph.Edge.PairPosition;
 import autograph.exception.*;
 import autograph.undo.*;
 
 import javax.swing.event.UndoableEditEvent;
 import javax.swing.event.UndoableEditListener;
 import javax.swing.undo.AbstractUndoableEdit;
 import javax.swing.undo.CannotRedoException;
 import javax.swing.undo.CannotUndoException;
 import javax.swing.undo.UndoManager;
 /**
  * Graph contains the data and functionality for building graph objects to be represented by Autograph
  *
  * @author Keith Wentzel
  * @version 1.0
  */
 public class Graph implements Serializable {
 	private ArrayList<Node> vNodeList;
 	private ArrayList<Edge> vEdgeList;
 	private String vTitle;
 	public SelectedItems vSelectedItems;
 	public UndoManager vUndoManager;
 
 	public enum DeleteAction{
       BOTH,
       NODES,
       EDGES
    };
 	/**
 	 * ValidateGraph - ensures valid data is passed to the graph constructor
 	 *
 	 * @param title - title of the graph
 	 */
 	private void mValidateGraph(String title) throws CannotCreateGraphException{
 		if(title == null){
 			throw new CannotCreateGraphException("The graph must have a title.");
 		}
 	}
 
 	public Graph(String title){
 		try{
 			mValidateGraph(title);
 			vUndoManager = new UndoManager();
 			vNodeList = new ArrayList<Node>();
 			vEdgeList = new ArrayList<Edge>();
 			vSelectedItems = new SelectedItems();
 			if(title.isEmpty()){
 				vTitle = "New Graph";
 			}
 			else{
 				vTitle = title;
 			}
 		}
 		catch(CannotCreateGraphException e){
 			//TODO: This may need changed to an error file, etc
 			System.out.println("Error while trying to create graph: " + e.getError());
 		}
 	}
 
 	/**
 	 * GetNodeList - gets a list of nodes for the graph
 	 *
 	 * @return - node list for the graph
 	 * @see Node
 	 */
 	public ArrayList<Node> mGetNodeList(){
 		return vNodeList;
 	}
 
 	/**
 	 * GetEdgeList - gets an edge list for the graph
 	 *
 	 * @return a list of edges for the graph
 	 * @see Edge
 	 */
 	public ArrayList<Edge> mGetEdgeList(){
 		return vEdgeList;
 	}
 
 	/**
 	 * Returns title of Graph
 	 *
 	 * @return  The title of the graph
 	 */
 	public String mGetTitle(){
 		return vTitle;
 	}
 
 	/**
 	 * Sets title of Graph
 	 *
 	 * @param title - the new title of the graph
 	 */
 	public void mSetTitle(String title){
 		if (title !=null && !title.isEmpty()) {
 			vTitle = title;
 		}
 	}
 
 	/**
 	 * GetNodeById - returns the node with the selected Id
 	 *
 	 * @param id - id of the node 
 	 * @return a node with the selected id, or null if the node is not found
 	 * @see Node
 	 */
 	public Node mGetNodeById(String id){
 		//initialize to null. It will be up to the caller to make sure the value is not
 		//null when it is returned.
 		Node node = null;
 		try{
 			for(int i = 0; i < vNodeList.size(); i++){
 				if(vNodeList.get(i).mGetId().equals(id)){
 					node = vNodeList.get(i);
 				}
 			}
 		}
 		catch(Exception e){
 			//This will theoretically only happen if vNodeList.get(i) is null.
 			//TODO: This may need changed to an error file, etc
 			System.out.println("Error while trying to find node: " + e.getMessage());
 		}
 		return node;
 	}
 
 	/**
 	 * DeleteNodeById - removes a node with the selected id from the node list for the graph
 	 *
 	 * @param id - id of the Node
 	 * @see Node
 	 */
 	public void mDeleteNodeById(String id) throws CannotRemoveNodeException {
 		try{
 			for(int i = 0; i < vNodeList.size(); i++){
 				if(vNodeList.get(i).mGetId().equals(id)){
 					//remove will remove the node and shift left, so it will not create
 					//a null entry in the list.
 					vNodeList.remove(i);
 				}
 			}
 		}
 		catch(Exception e){
 			//this will theoretically only happen if vNodeList.get(i) is null
 			//TODO: This may need changed to an error file, etc
 			System.out.println("Error while trying to find node for deletion: " + e.getMessage());
 
 			throw new CannotRemoveNodeException("Could not find node for deletion!", e);
 		}
 	}
 
 	/**
 	 * GetEdgeById - returns an edge with the selected id in the graph
 	 *
 	 * @param id - id of the Edge
 	 * @return an edge with the selected id, or null if the node is not found
 	 * @see Edge
 	 */
 	public Edge mGetEdgeById(String id){
 		Edge edge = null;
 		try{
 			for(int i = 0; i < vEdgeList.size(); i++){
 				if(vEdgeList.get(i).mGetId().equals(id)){
 					edge = vEdgeList.get(i);
 				}
 			}
 		}
 		catch(Exception e){
 			//This will theoretically only happen if vNodeList.get(i) is null.
 			//TODO: This may need changed to an error file, etc
 			System.out.println("Error while trying to find edge: " + e.getMessage());
 		}
 		return edge;
 	}
 
 	/**
 	 * DeleteEdgeById - removes the edge with the selected id from the graph
 	 *
 	 * @param id of the edge
 	 * @see Edge
 	 */
 	public void mDeleteEdgeById(String id) throws CannotRemoveEdgeException {
 		try{
 			for(int i = 0; i < vEdgeList.size(); i++){
 				if(vEdgeList.get(i).mGetId().equals(id)){
 					//remove will remove the node and shift left, so it will not create
 					//a null entry in the list.
 					vEdgeList.remove(i);
 				}
 			}
 		}
 		catch(Exception e){
 			//this will theoretically only happen if vNodeList.get(i) is null
 			//TODO: This may need changed to an error file, etc
 			System.out.println("Error while trying to find edge for deletion: " + e.getMessage());
 
 			throw new CannotRemoveEdgeException("Could not find edge for deletion!", e);
 		}
 	}
 
 	/**
 	 * AddEdge - adds an edge to the edge list for the graph
 	 *
 	 * @param edge - edge to be added to the edge list
 	 * @see Edge
 	 * 
 	 */
 	public void mAddEdge(Edge edge) throws CannotAddEdgeException {
 		try {
 			vEdgeList.add(edge);
 			vUndoManager.addEdit( new AddEdgeEdit(edge, this.vEdgeList) );
 		}
 		catch (Exception e) {
 			throw new CannotAddEdgeException("Error while adding edge!", e);
 		}
 	}
 
 
 	/**
 	 * AddNode - adds a node to the node list
 	 * 
 	 * @param node
 	 * @throws CannotAddNodeException
 	 * @see Node
 	 * 
 	 */
 	public void mAddNode(Node node) throws CannotAddNodeException {
 		try {
 			vNodeList.add(node);
 			vUndoManager.addEdit( new AddNodeEdit(node, this.vNodeList) );
 		}
 		catch (Exception e) {
 			throw new CannotAddNodeException("Error while adding node!", e);
 		}
 	}
 
 
 	/**
 	 * Checks the edge list for a twin to edge and resets the twin variables if a twin 
 	 * is found.
 	 * @param edge
 	 * @return
 	 */
    public PairPosition mCheckForEdgeTwin(Edge edge){
       PairPosition position = PairPosition.UNPAIRED;
       
       for(int i = 0; i < vEdgeList.size(); i++){
         if(vEdgeList.get(i) != edge){
            if(vEdgeList.get(i).mGetEndNode() == edge.mGetEndNode() && vEdgeList.get(i).mGetStartNode() == edge.mGetStartNode()){
               position = PairPosition.SECOND;
               vEdgeList.get(i).mSetPairPosition(PairPosition.FIRST);
               vEdgeList.get(i).mSetTwin(edge);
               edge.mSetTwin(vEdgeList.get(i));
               break;
            }
            else if(vEdgeList.get(i).mGetEndNode() == edge.mGetStartNode() && vEdgeList.get(i).mGetStartNode() == edge.mGetEndNode()){
               position = PairPosition.SECOND;
               vEdgeList.get(i).mSetPairPosition(PairPosition.FIRST);
               vEdgeList.get(i).mSetTwin(edge);
               edge.mSetTwin(edge);
               break;
            }
         }
       }
       
       return position;
    }
 
    /**
     * Check if edgetToCheck already has two twins (we don't want three edges between the same nodes.)
     * @param startNode - the startNode for that edge
     * @param endNode - the end node for that edge.
     * @return - true if it has multiple twins, false otherwise.
     */
    public boolean mEdgeHasMultipleTwins(Node startNode, Node endNode) {
       Boolean hasMultipleTwins = false;
       
       for(int i = 0; i < vEdgeList.size(); i++){
          //Keep the edgeToCheck variable in case edgeToCheck has been added to vEdgeList at this point.
          if(vEdgeList.get(i).mGetEndNode() == endNode && vEdgeList.get(i).mGetStartNode() == startNode){
             if(vEdgeList.get(i).mGetPairPosition()!= PairPosition.UNPAIRED){
                hasMultipleTwins = true;
                break;
             }
          }
          if(vEdgeList.get(i).mGetEndNode() == startNode && vEdgeList.get(i).mGetStartNode() == endNode){
             if(vEdgeList.get(i).mGetPairPosition() != PairPosition.UNPAIRED){
                hasMultipleTwins = true;
                break;
             }
          }
       }
       
       return hasMultipleTwins;
    }
 
    public void mDeleteSelectedItems(DeleteAction deleteAction) {
    	ArrayList<Edge> undoableEdges = new ArrayList<Edge>();
    	ArrayList<Node> undoableNodes = new ArrayList<Node>();
    	switch(deleteAction) {
    		case BOTH:
    			undoableEdges = mDeleteSelectedEdges();
    			undoableNodes = mDeleteSelectedNodes(undoableEdges);
    			break;
 			case NODES:
 				undoableNodes = mDeleteSelectedNodes(undoableEdges);
 				break;
 			case EDGES:
 				undoableEdges = mDeleteSelectedEdges();
 				break;
    	}
    	vUndoManager.addEdit(new DeleteNodeEdgeEdit(undoableNodes, this.vNodeList, 
    															  undoableEdges, this.vEdgeList));
    }
 
    private ArrayList<Edge> mDeleteSelectedEdges() {
    	ArrayList<Edge> sEdges = this.vSelectedItems.mGetSelectedEdges();
    	ArrayList<Edge> undoableEdges = new ArrayList<Edge>();
    	for(int i = 0; i < sEdges.size(); i++) {
    		Edge e = sEdges.get(i);
    		Edge eTwin = e.mGetTwin();
    		if(eTwin != null) {
    			eTwin.mSetTwin(null);
    		}
    		undoableEdges.add(e);
    		this.vEdgeList.remove(this.vEdgeList.indexOf(e));
    		sEdges.remove(i);
 			i--;
    	}
    	return undoableEdges;
    }
 
    private ArrayList<Node> mDeleteSelectedNodes(ArrayList<Edge> undoableEdges) {
    	ArrayList<Node> sNodes = this.vSelectedItems.mGetSelectedNodes();
    	ArrayList<Edge> sEdges = this.vSelectedItems.mGetSelectedEdges();
    	ArrayList<Node> undoableNodes = new ArrayList<Node>();
   	vUndoManager.addEdit(new DeleteNodeEdit(sNodes, this.vNodeList));
    	for(int i = 0; i < sNodes.size(); i++) {
    		Node n = sNodes.get(i);
    		for (int j = 0; j < vEdgeList.size(); j++) {
    			Edge e = this.vEdgeList.get(j);
    			if ( (e.mGetStartNode() == n) || (e.mGetEndNode() == n) ) {
    				undoableEdges.add(e);
    				this.vEdgeList.remove(j);
    				j--;
    				int eIndex = sEdges.indexOf(e);
    				if (eIndex >= 0)
    				{
    					sEdges.remove(eIndex);
 					}
    			}
    		}
    		//System.out.println("Removed Node: " + this.vNodeList.indexOf(n));
    		undoableNodes.add(n);
    		this.vNodeList.remove(this.vNodeList.indexOf(n));
    		sNodes.remove(i);
    		i--;
    	}
    	return undoableNodes;
    }
 }
