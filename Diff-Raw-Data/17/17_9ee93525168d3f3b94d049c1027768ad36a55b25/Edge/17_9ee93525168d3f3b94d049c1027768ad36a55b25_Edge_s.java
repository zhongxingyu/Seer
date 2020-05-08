 
public class Edge implements Comparable {
 	private Node node1;
 	private Node node2;
 	
 	private Double strength;
 	
 	public Edge(Node u, Node v, Double strength) {
 		this.node1 = u;
 		this.node2 = v;
 		this.strength = strength;
 	}
 	
 	public Node getNode1() {
 		return node1;
 	}
 	
 	public Node getNode2() {
 		return node2;
 	}
 	
 	public Double getStrength() {
 		return this.strength;
 	}
 
 	public int compareTo(Object obj) {
 		if(obj == null) {
 			return -1;
 		}
 		if(obj instanceof Edge) {
 			Edge that = (Edge)obj;
 			
 			int result = this.node1.compareTo(that.node1);
 			
 			if(result == 0) {
 				return this.node2.compareTo(that.node2);
 			}
 		}
 		return -1;
 	}
 }
