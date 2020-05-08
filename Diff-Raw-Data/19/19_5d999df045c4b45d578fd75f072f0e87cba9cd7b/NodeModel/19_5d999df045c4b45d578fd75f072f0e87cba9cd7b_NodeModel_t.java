 package de.uni.stuttgart.informatik.ToureNPlaner.Data;
 
 
 import java.io.Serializable;
 import java.util.ArrayList;
 
 public class NodeModel implements Serializable {
 
 	private ArrayList<Node> nodeVector = new ArrayList<Node>();
 
     public static final String IDENTIFIER = "nodemodel";
 
 	public ArrayList<Node> getNodeVector() {
 		return nodeVector;
 	}
 
	public Node get(int i) {
 		return nodeVector.get(i);
 	}
 
 	public Integer size() {
 		return nodeVector.size();
 	}
 
 	public void addNodeToVector(Node node) {
 		nodeVector.add(node);
 	}
 
	public void remove(int pos) {
 		nodeVector.remove(pos);
 		}
 
	public void swapNodes(int pos1, int pos2) {
 		Node tempNode = nodeVector.get(pos1);
 		nodeVector.set(pos1, nodeVector.get(pos2));
 		nodeVector.set(pos2, tempNode);
 
 	}
 }
