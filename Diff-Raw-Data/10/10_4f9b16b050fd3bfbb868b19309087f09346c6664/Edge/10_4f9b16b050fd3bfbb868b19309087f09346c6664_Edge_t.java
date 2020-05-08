 package test;
 
 import unionfind.Node;
 
 /**
  * 
  * @author Anderson Queiroz <contato(at)andersonq(dot)eti(dot)br
  *
  */
 public class Edge {
 
 	Node u;
 	Node v;
 	int cost;
 	
	public Edge(Node u, Node v, int cost)
	{
		this.u = u;
		this.v = v;
		this.cost = cost;
	}
	
 	public Node getU() {
 		return u;
 	}
 	public void setU(Node u) {
 		this.u = u;
 	}
 	public Node getV() {
 		return v;
 	}
 	public void setV(Node v) {
 		this.v = v;
 	}
 	public int getCost() {
 		return cost;
 	}
 	public void setCost(int cost) {
 		this.cost = cost;
 	}
 	
 	public String toString()
 	{
		return String.format("Edge " + u.toString() + "-" + v.toString() + " cost: " + cost);
 	}
 }
