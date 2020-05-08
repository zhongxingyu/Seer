 import java.util.ArrayList;
 
 public class Network{
 	ArrayList<Node> nodelist;
 	
 	public Network() {
 		nodelist = new ArrayList<Node>();
 	}
 	
 	public void addNode(Node n) {
 		nodelist.add(n);
 	}
 	
 	ArrayList<Node> getNodeList() {
 		return nodelist;
 	}
 	
 }
