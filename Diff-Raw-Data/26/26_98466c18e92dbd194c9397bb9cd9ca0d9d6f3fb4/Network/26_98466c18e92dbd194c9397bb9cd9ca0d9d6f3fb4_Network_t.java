 import java.util.ArrayList;
 
 public class Network{
 	ArrayList<Node> nodelist;
 	
 	public Network() {
 		nodelist = new ArrayList<Node>();
 	}
 	
	public Network(int a, int b) {
		// TODO: This contructor is incomplete, was added to solve errors
	}
	
 	public void addNode(Node n) {
 		nodelist.add(n);
 	}
 	
 	ArrayList<Node> getNodeList() {
 		return nodelist;
 	}

	public ArrayList<Node> getSensorList() {
		// TODO Auto-generated method stub
		return null;
	}
 	
 }
