 package node;
 
 import java.util.TreeMap;
 import java.util.TreeSet;
 
 
 /**
  * Holds connections for a single node.
  * 
  * @author Nathan Zabriskie
  *
  */
 public class Connections {
 	private TreeMap<Integer,Node> downPointers = new TreeMap<Integer,Node>();
 	private TreeMap<Integer,Node> upPointers = new TreeMap<Integer,Node>();
 	private TreeMap<Integer,Node> lowerNeighbors = new TreeMap<Integer,Node>();
 	private TreeMap<Integer,Node> upperNeighbors = new TreeMap<Integer,Node>();
 	
 	private Node fold = Node.NULL_NODE;
 	private Node surrogateFold = Node.NULL_NODE;
 	private Node inverseSurrogateFold = Node.NULL_NODE;
 	
 	public Connections(){
 	}
 	
 	//--------------------
 	//  GETTERS AND SETTERS
 	//--------------------
 
 	public TreeMap<Integer,Node> getDownPointers() {
 		return downPointers;
 	}
 
 	public void setDownPointers(TreeMap<Integer,Node> downPointers) {
 		this.downPointers = downPointers;
 	}
 
 	public TreeMap<Integer,Node> getUpPointers() {
 		return upPointers;
 	}
 
 	public void setUpPointers(TreeMap<Integer,Node> upPointers) {
 		this.upPointers = upPointers;
 	}
 	
 	public TreeMap<Integer,Node> getLowerNeighbors() {
 		return lowerNeighbors;
 	}
 
 	public void setLowerNeighbors(TreeMap<Integer,Node> lowerNeighbors) {
 		this.lowerNeighbors = lowerNeighbors;
 	}
 
 	public TreeMap<Integer,Node> getUpperNeighbors() {
 		return upperNeighbors;
 	}
 
 	public void setUpperNeighbors(TreeMap<Integer,Node> upperNeighbors) {
 		this.upperNeighbors = upperNeighbors;
 	}
 
 	public Node getFold() {
 		return this.fold;
 	}
 
 	public void setFold(Node fold) {
 		this.fold = fold;
 	}
 
 	public Node getSurrogateFold() {
 		return surrogateFold;
 	}
 
 	public void setSurrogateFold(Node surrogateFold) {
 		this.surrogateFold = surrogateFold;
 	}
 
 	public Node getInverseSurrogateFold() {
 		return inverseSurrogateFold;
 	}
 
 	public void setInverseSurrogateFold(Node inverseSurrogateFold){
 		this.inverseSurrogateFold = inverseSurrogateFold;
 	}
 	
 	public Node getLargestUpPointer(){
 		if(upPointers.size() > 0){
 			return upPointers.get(upPointers.lastKey());
 		}
 		else{
 			return Node.NULL_NODE;
 		}
 	}
 	
 	public Node getBiggestNeighbor(){
 		if(upperNeighbors.size() > 0){
			return upperNeighbors.get(upperNeighbors.lastKey());
 		}
 		else{
 			return Node.NULL_NODE;
 		}
 	}
 	
 	//--------------------
 	//  A D D E R S
 	//--------------------		
 	public void addDownPointer(Node downPointer){
 		this.downPointers.put(downPointer.getWebIdValue(), downPointer);
 	}
 	
 	public void addLowerNeighbor(Node lowerNeighbor){
 		this.lowerNeighbors.put(lowerNeighbor.getWebIdValue(), lowerNeighbor);
 	}
 	
 	public void addUpperNeighbor(Node upperNeighbor){
 		this.upperNeighbors.put(upperNeighbor.getWebIdValue(), upperNeighbor);
 	}
 	
 	public void addUpPointer(Node upPointer){
 		this.upPointers.put(upPointer.getWebIdValue(), upPointer);
 	}
 	
 	//--------------------
 	//  R E M O V E R S
 	//--------------------	
 	public void removeDownPointer(Node downPointer){
 		this.downPointers.remove(downPointer.getWebIdValue());
 	}
 	
 	public void removeLowerNeighbor(Node lowerNeighbor){
 		this.lowerNeighbors.remove(lowerNeighbor.getWebIdValue());
 	}
 	
 	public void removeUpperNeighbor(Node upperNeighbor){
 		this.upperNeighbors.remove(upperNeighbor.getWebIdValue());
 	}
 	
 	public void removeUpPointer(Node upPointer){
 		this.upPointers.remove(upPointer.getWebIdValue());
 	}
 	
 	//--------------------
 	//  Q U E R I E S
 	//--------------------
 	
 	public boolean hasInverseSurrogateFold(){
 		if(inverseSurrogateFold == Node.NULL_NODE)
 			return false;
 		else
 			return true;
 	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result
 				+ ((downPointers == null) ? 0 : downPointers.hashCode());
 		result = prime * result + ((fold == null) ? 0 : fold.hashCode());
 		result = prime
 				* result
 				+ ((inverseSurrogateFold == null) ? 0 : inverseSurrogateFold
 						.hashCode());
 		//result = prime * result
 				//+ ((neighbors == null) ? 0 : neighbors.hashCode());
 		result = prime * result
 				+ ((surrogateFold == null) ? 0 : surrogateFold.hashCode());
 		result = prime * result
 				+ ((upPointers == null) ? 0 : upPointers.hashCode());
 		return result;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		Connections other = (Connections) obj;
 		if (downPointers == null) {
 			if (other.downPointers != null)
 				return false;
 		} else if (!downPointers.equals(other.downPointers))
 			return false;
 		if (fold == null) {
 			if (other.fold != null)
 				return false;
 		} else if (!fold.equals(other.fold))
 			return false;
 		if (inverseSurrogateFold == null) {
 			if (other.inverseSurrogateFold != null)
 				return false;
 		} else if (!inverseSurrogateFold.equals(other.inverseSurrogateFold))
 			return false;
 		/*if (neighbors == null) {
 			if (other.neighbors != null)
 				return false;
 		} else if (!neighbors.equals(other.neighbors))
 			return false;*/
 		if (surrogateFold == null) {
 			if (other.surrogateFold != null)
 				return false;
 		} else if (!surrogateFold.equals(other.surrogateFold))
 			return false;
 		if (upPointers == null) {
 			if (other.upPointers != null)
 				return false;
 		} else if (!upPointers.equals(other.upPointers))
 			return false;
 		return true;
 	}
 
 	public int getUpPointerCount() {
 		return upPointers.size();
 	}
 	
 	public int getDownPointerCount() {
 		return downPointers.size();
 	}
 
 	public Node getSmallestDownPointer() {
 		return downPointers.get(downPointers.firstKey());
 	}
 
 	public Node getSmallestChildlessNeighbor() {
 		Node result = Node.NULL_NODE;
 		for(Node ln: lowerNeighbors.values()){
 			if(ln.getConnections().getUpPointerCount() > 0){
 				return ln;
 			}
 		}
 		return result;
 	}
 }
