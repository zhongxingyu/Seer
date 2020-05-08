 package Source;
 
 import java.util.ArrayList;
 
 public class Node {
 	ArrayList<Node> children;
 	RecursiveParserMiniRe.Symbol symbolType;
 	String name;
 	Variable data;
 	
 	public Node(String nodeName) {
 		this.children = new ArrayList<Node>();
 		this.name = nodeName;
 	}
 	
 	public void addChild(Node n) {
 		this.children.add(n);
 	}
 	
 	@Override
 	public String toString() {
		return "N<"+name+": "+this.children+">";
 	}
 
 	public void setData(Variable value) {
 		this.data = value;
 	}
 }
