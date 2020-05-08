 package node;
 
 
 import static utilities.BitManipulation.calculateChildWebId;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.TreeMap;
 
 import utilities.BitManipulation;
 
 /**
  * @author Joseph
  */
 public class Node implements NodeInterface, Comparable<Node>{
 	
 	private WebId webId = null;
 	private int height = -1;
 	private Connections connections;
 	private NodeState state = NodeState.STANDARD;
 	
 	public static final Node NULL_NODE = new Node(){
 		@Override public void addDownPointer(Node downPointer){ return; }
 		@Override public void addNeighbor(Node neighbor){ return; }
 		@Override public void addUpPointer(Node upPointer){ return; }
 		@Override public void removeDownPointer(Node downPointer){ return; }
 		@Override public void removeNeighbor(Node neighbor){ return; }
 		@Override public void removeUpPointer(Node upPointer){ return; }
 		@Override public void setWebId(WebId webId){ return; }
 		@Override public void setFold(Node fold){ return; }
 		@Override public void setSurrogateFold(Node surrogateFold){ return; }
 		@Override public void setInverseSurrogateFold(Node inverseSurrogateFold){ return; }
 		@Override public void addChild(Node child){
 			child.setWebId(new WebId(0));
 			child.setConnections(new Connections());
 			child.setFold(child);
 			child.setState(NodeState.CAP);
 		}
 		@Override public Node findNode(int webId){
 			return this;
 		}
 	};
 	
 	private Node(){
 		webId = WebId.NULL_WEB_ID;
 		connections = new Connections();
 		connections.setFold(this);
 		connections.setSurrogateFold(this);
 		connections.setInverseSurrogateFold(this);
 	}
 
 	public Node(int id){
 		assert(id >= 0);
 		webId = new WebId(id);
 		connections = new Connections();
 		this.setFold(this);
 		this.height = webId.getHeight();
 		if(id == 0){
 			setState(NodeState.CAP);
 		}
 	}
 	
 	public Node(int id, int height){
 		assert(id >= 0 && height >= 0);
 		webId = new WebId(id, height);
 		connections = new Connections();
 		this.setFold(this);
 		this.height = webId.getHeight();
 	}
 	
 	/**
 	 * Constructs a SimplifiedNodeDomain object from the attributes of this Node.
 	 * @return The created SimplifiedNodeDomain object.
 	 */
 	public SimplifiedNodeDomain constructSimplifiedNodeDomain(){
 		HashSet<Integer> neighborIds = new HashSet<Integer>();
 		for(Node n : getNeighbors().values()) {
 			neighborIds.add(n.getWebIdValue());
 		}
 		
 		HashSet<Integer> upIds = new HashSet<Integer>();
 		for(Node n : getUpPointers().values()){
 			upIds.add(n.getWebIdValue());
 		}
 		
 		HashSet<Integer> downIds = new HashSet<Integer>();
 		for(Node n : getDownPointers().values()){
 			downIds.add(n.getWebIdValue());
 		}
 		
 		return new SimplifiedNodeDomain(webId.getValue(), this.getHeight(), neighborIds, upIds, downIds, 
 									    getFold().getWebIdValue(), getSurrogateFold().getWebIdValue(), 
 									    getInverseSurrogateFold().getWebIdValue(), state.STATE_ID);
 	}
 	
 	/**
 	 * Adds the node to the HyPeerWeb.
 	 * @param newNode The new Node to add to the HyPeerWeb.
 	 * @pre newNode is not null AND newNode is not NULL_NODE
 	 * @post newNode is properly connected to the HyPeerWeb
 	 */
 	public void addToHyPeerWeb(Node newNode){
 		assert (newNode != null && newNode != NULL_NODE);
 		Node insertionPoint = findInsertionPoint();
 		//System.out.println("this: "+this.getWebIdValue() + " " + this.getHeight());
 		int childWebId = calculateChildWebId(insertionPoint.getWebIdValue(), insertionPoint.getHeight());
 		//System.out.println(""+childWebId);
 		newNode.setWebId(new WebId(childWebId));
 		insertionPoint.addChild(newNode);
 	}
 	
 	/**
 	 * Removes the node from the HyPeerWeb.
 	 * @param deleteNode The node that is to be deleted.
 	 * @pre The node is in the web and is not null or NULL_NODE
 	 * @post The node was removed from the web and replaced with the last node in the web.
 	 */
 	public void removeFromHyPeerWeb(Node deleteNode) {
 		Node deletionPoint = findDeletionPoint();
 		deletionPoint.disconnect();
 		deleteNode.replaceNode(deletionPoint);
 	}
 
 	/**
 	 * Removes the node from the HyPeerWeb.
 	 * @param replacementNode is the node that will be taking this nodes place
 	 * @pre The node is in the web and is not null or NULL_NODE
 	 * @post All pointers to this node will now be pointing to replacementNode
 	 */
 	public void replaceNode(Node replacementNode) {
		this.connections.replaceNode(this, replacementNode);
		replacementNode.setWebId(this.webId);
 	}
 	
 	/**
 	 * Finds the insertion point.
 	 * @return The insertion point.
 	 */
 	public Node findInsertionPoint(){
 		return findLowerBoundUpperBoundPair().getUpperBound();
 	}
 
 	/**
 	 * Finds the deletion point.
 	 * @return The deletion point.
 	 */
 	public Node findDeletionPoint(){
 		return findLowerBoundUpperBoundPair().getLowerBound();
 	}
 	
 	/**
 	 * Using the greedy algorithm and the squeeze algorithm, finds both
 	 * the insertion point and deletion point.
 	 * @return The Pair that contains the lower bound (deletion point) and the upper bound
 	 * (insertion point).
 	 */
 	public Pair<Node> findLowerBoundUpperBoundPair(){
 		Node largest = this.findLargest();
 		Pair<Node> lowerUpperPair = new Pair<Node>(largest, Node.NULL_NODE);
 		lowerUpperPair = largest.getState().squeeze(lowerUpperPair);
 		return lowerUpperPair;
 	}
 	
 	public void disconnect() {
 		int parentId = BitManipulation.calculateParentWebId(this.getWebIdValue(), this.getHeight());
 		Node parent = connections.getLowerNeighbors().get(parentId);
 		parent.setHeight(parent.getHeight() - 1);
 		parent.getUpperNeighbors().remove(this.getWebIdValue());
 		
 		connections.getLowerNeighbors().remove(parent.getWebIdValue());
 		for(Node lowerNeighbor : connections.getLowerNeighbors().values()){
 			lowerNeighbor.removeNeighbor(this);
 			lowerNeighbor.addDownPointer(parent);
 			NodeState.setNodeState(lowerNeighbor);
 			
 			parent.addUpPointer(lowerNeighbor);
 		}
 		
 		for(Node upPointToMe : connections.getDownPointers().values()){
 			upPointToMe.removeUpPointer(this);
 			NodeState.setNodeState(upPointToMe);
 		}
 		
 		Node fold = this.getFold();
 		if(fold.equals(parent)){
 			parent.setFold(parent);
 			parent.setInverseSurrogateFold(NULL_NODE);
 			parent.setSurrogateFold(NULL_NODE);
 		} else if(parent.getConnections().hasSurrogateFold()){
 			parent.setFold(fold);
 			parent.setInverseSurrogateFold(NULL_NODE);
 			parent.setSurrogateFold(NULL_NODE);
 			fold.setFold(parent);
 			fold.setInverseSurrogateFold(NULL_NODE);
 		} else {
 			fold.setFold(NULL_NODE);
 			fold.setSurrogateFold(parent);
 			parent.setInverseSurrogateFold(fold);
 		}
 		
 		NodeState.setNodeState(parent);
 		NodeState.setNodeState(fold);
 	}
 	
 	/**
 	 * This is a greedy algorithm and only guarantees finding the largest node if the
 	 * HyPeerWeb is also a HyperCube; otherwise this algorithm only guarantees finding an edge node.
 	 * @return
 	 * @pre This node is connected to the HyPeerWeb.
 	 * @post Either the cap node (if it exists); otherwise an edge node.
 	 */
 	public Node findLargest(){
 		Node largest = this;
 		if(this.getFold().getWebIdValue() > largest.getWebIdValue()){
 			largest = this.getFold();
 		}
 		if(this.getInverseSurrogateFold().getWebIdValue() > largest.getWebIdValue()){
 			largest = this.getInverseSurrogateFold();
 		}
 		if(this.getSurrogateFold().getWebIdValue() > largest.getWebIdValue()){
 			largest = this.getSurrogateFold();
 		}
 		if(this.connections.getBiggestNeighbor().getWebIdValue() > largest.getWebIdValue()){
 			largest = this.connections.getBiggestNeighbor();
 		}
 		if(this.connections.getLargestUpPointer().getWebIdValue() > largest.getWebIdValue()){
 			largest = this.connections.getLargestUpPointer();
 		}
 		
 		if(largest == this){
 			return largest;
 		}
 		else{
 			return largest.findLargest();
 		}
 	}
 	
 	/**
 	 * Finds the node with the given webId.
 	 * @param webId The webId of the node to be found.
 	 * @pre The node with the given webId exists and is connected to the HyPeerWeb.
 	 * @post result = Node with given webId
 	 */
 	public Node findNode(int webId){
 		if (webId == this.webId.getValue()) {
 			return this;
 		}
 		
 		Node closest = this.connections.getNextClosestNeighbor(this.webId.getValue(), webId);
 		return closest.findNode(webId);
 	}
 	
 	/**
 	 * Adds the given node to the HyPeerWeb.
 	 * @param child The child node of this node.
 	 * @pre The child node has the appropriate webId (webId = N + 1) and the child node is a
 	 * child of this node.
 	 * @post The child node is connected to the HyPeerWeb, all connections are set, 
 	 * this node's height is correct.
 	 */
 	public void addChild(Node child){
 		this.height++;
 		
 		Connections childConnections = new Connections();
 		child.setConnections(childConnections);
 		
 		//set child node's lower neighbors
 		TreeMap<Integer,Node> upPointers = connections.getUpPointers();
 		childConnections.setLowerNeighbors(upPointers);
 		connections.setUpPointers(new TreeMap<Integer,Node>());
 		//notify child node's lower neighbors
 		Iterator<Node> childsLowerNeighbors = upPointers.values().iterator();
 		while(childsLowerNeighbors.hasNext()){
 			Node lowerNeighbor = childsLowerNeighbors.next();
 			lowerNeighbor.removeDownPointer(this);
 			lowerNeighbor.addNeighbor(child);
 		}
 		
 		//set child node's surrogate neighbors
 		Iterator<Node> childsSurrogateNeighbors = connections.getUpperNeighbors().values().iterator();
 		while(childsSurrogateNeighbors.hasNext()){
 			Node surrogateNeighbor = childsSurrogateNeighbors.next();
 			child.addDownPointer(surrogateNeighbor);
 			surrogateNeighbor.addUpPointer(child);
 		}
 		
 		//set all of the folds
 		Node childsNewFold = null;
 		if(connections.hasInverseSurrogateFold()){
 			childsNewFold = this.getInverseSurrogateFold();
 			child.setFold(childsNewFold);
 			childsNewFold.setFold(child);
 			childsNewFold.setSurrogateFold(NULL_NODE);
 			this.setInverseSurrogateFold(NULL_NODE);
 		}
 		else{
 			childsNewFold = this.getFold();
 			child.setFold(childsNewFold);
 			childsNewFold.setFold(child);
 			if(this.getWebIdValue() == 0 && this.getHeight() == 1){
 				this.setFold(child);
 			}
 			else{
 				this.setSurrogateFold(childsNewFold);
 				this.setFold(Node.NULL_NODE);
 				childsNewFold.setInverseSurrogateFold(this);
 			}
 		}
 		
 		//add child's first neighbor
 		child.addNeighbor(this);
 		//add child as neighbor last
 		this.addNeighbor(child);
 		
 		NodeState.setNodeState(child);
 		NodeState.setNodeState(this);
 	}
 	
 	//------------------
 	//  A D D E R S
 	//------------------
 	public void addDownPointer(Node downPointer){
 		connections.addDownPointer(downPointer);
 		if(connections.getDownPointerCount() > 0){
 			NodeState.setNodeState(this);
 		}
 	}
 	
 	public void addNeighbor(Node neighbor){
 		if(neighbor.getWebIdValue() > this.getWebIdValue()){
 			connections.addUpperNeighbor(neighbor);
 		}
 		else{
 			connections.addLowerNeighbor(neighbor);
 		}
 	}
 	
 	public void addUpPointer(Node upPointer){
 		connections.addUpPointer(upPointer);
 		if(connections.getUpPointerCount() > 0){
 			NodeState.setNodeState(this);
 		}
 	}
 	
 	//--------------------
 	//  R E M O V E R S
 	//--------------------	
 	public void removeDownPointer(Node downPointer){
 		connections.removeDownPointer(downPointer);
 		if(connections.getDownPointerCount() == 0){
 			NodeState.setNodeState(this);
 		}
 	}
 	
 	public void removeNeighbor(Node neighbor){
 		if(neighbor.getWebIdValue() > this.getWebIdValue()){
 			connections.removeUpperNeighbor(neighbor);
 		}
 		else{
 			connections.removeLowerNeighbor(neighbor);
 		}
 	}
 	
 	public void removeUpPointer(Node upPointer){
 		connections.removeUpPointer(upPointer);
 		if(connections.getUpPointerCount() == 0){
 			NodeState.setNodeState(this);
 		}
 	}
 	
 	public void setState(NodeState state){
 		this.state = state;
 	}
 	
 	//------------------
 	//  G E T T E R S
 	//------------------
 	public WebId getWebId(){
 		return webId;
 	}
 	
 	public int getHeight(){
 		return height;
 	}
 	
 	public int getWebIdValue(){
 		return webId.getValue();
 	}
 	
 	public int getWebIdHeight(){
 		return webId.getHeight();
 	}
 	
 	public TreeMap<Integer,Node> getDownPointers(){
 		return connections.getDownPointers();
 	}
 	
 	public TreeMap<Integer,Node> getUpPointers(){
 		return connections.getUpPointers();
 	}
 	
 	public TreeMap<Integer,Node> getNeighbors(){
 		TreeMap<Integer,Node> result = new TreeMap<Integer,Node>();
 		result.putAll(connections.getLowerNeighbors());
 		result.putAll(connections.getUpperNeighbors());
 		return result;
 	}
 	
 	public Node getFold(){
 		return connections.getFold();
 	}
 	
 	public Node getSurrogateFold(){
 		return connections.getSurrogateFold();
 	}
 	
 	public Node getInverseSurrogateFold(){
 		return connections.getInverseSurrogateFold();
 	}
 	
 	public NodeState getState(){
 		return state;
 	}
 	
 	public Connections getConnections(){
 		return connections;
 	}
 	
 	public Map<Integer, Node> getUpperNeighbors(){
 		return connections.getUpperNeighbors();
 	}
 	
 	//------------------
 	//  S E T T E R S
 	//------------------
 	public void setFold(Node fold){
 		if(fold == null) {
 			connections.setFold(Node.NULL_NODE);
 		}
 		else {
 			connections.setFold(fold);
 		}
 		
 		NodeState.setNodeState(this);
 	}
 	
 	public void setHeight(int height){
 		this.height = height;
 	}
 	
 	public void setInverseSurrogateFold(Node inverseSurrogateFold){
 		if(inverseSurrogateFold == null) {
 			connections.setInverseSurrogateFold(Node.NULL_NODE);
 		}
 		else {
 			connections.setInverseSurrogateFold(inverseSurrogateFold);
 		}
 	}
 	
 	public void setSurrogateFold(Node surrogateFold){
 		if(surrogateFold == null) {
 			connections.setSurrogateFold(Node.NULL_NODE);
 		}
 		else {
 			connections.setSurrogateFold(surrogateFold);
 		}
 	}
 	
 	public void setWebId(WebId newWebId){
 		if(webId == null) {
 			this.webId = WebId.NULL_WEB_ID;
 		}
 		else {
 			this.webId = newWebId;
 		}
 		this.height = this.getWebIdHeight();
 		
 		NodeState.setNodeState(this);
 	}
 	
 	/**
 	 * Sets this nodes connections. Should only need to be called by the {@code addChild} method.
 	 * @param connections This nodes new connections.
 	 * @pre connections is not null
 	 * @post this.connections = connections
 	 */
 	private void setConnections(Connections connections){
 		assert connections != null;
 		this.connections = connections;
 	}
 	
 	@Override
 	public String toString(){
 		return "Node [webId=" + webId.getValue() + "]";
 	}
 
 	@Override
 	public int hashCode(){
 		return webId.hashCode();
 	}
 
 	@Override
 	public boolean equals(Object obj){
 		
 		if (this == obj) {
 			return true;
 		}
 		
 		if (obj == null) {
 			return false;
 		}
 		
 		if (getClass() != obj.getClass()) {
 			return false;
 		}
 		
 		Node other = (Node) obj;
 
 		return this.webId == other.webId;
 	}
 
 	@Override
 	public int compareTo(Node other) {
 		if(getWebIdValue() > other.getWebIdValue()){
 			return 1;
 		} else if(getWebIdValue() < other.getWebIdValue()){
 			return -1;
 		} else {
 			return 0;
 		}
 	}
 
 	/**
 	 * Duplicate method. Defined in specs though.
 	 */
 	@Override
 	public void changeFold(Node newFold) {
 		assert (getConnections().hasFold());
 		this.setFold(newFold);
 	}
 
 	@Override
 	public void addConnection(Node aNode) {
 	}
 
 	@Override
 	public void removeConnection(Node aNode) {
 	}
 
 	@Override
 	public void replaceConnection(Node aNode, Node replacementNode) {
 	}
 }
