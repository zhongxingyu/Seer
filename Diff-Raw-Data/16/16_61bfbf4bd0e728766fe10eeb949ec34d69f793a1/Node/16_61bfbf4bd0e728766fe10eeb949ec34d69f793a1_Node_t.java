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
		String s = "N<"+name+":";
		if (this.data != null)
			s += "data="+this.data+":";
		if (this.children.size() > 0)
			s += "children="+this.children;
		return s + ">";
 	}
 
 	public void setData(Variable value) {
 		this.data = value;
 	}
 }
