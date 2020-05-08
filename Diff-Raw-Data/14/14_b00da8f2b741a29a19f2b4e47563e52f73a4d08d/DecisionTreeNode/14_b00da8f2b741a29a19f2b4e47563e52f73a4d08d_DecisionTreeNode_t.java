 package src;
 import java.util.ArrayList;
 
 public class DecisionTreeNode
 {
 	int id;
 	int decision;
 	int node;
 	int edge;
 	DecisionTreeNode parent;
 	ArrayList<DecisionTreeNode> children;
 	
 	public DecisionTreeNode(){
 		id = -1;
 		decision = -1;
 		node = -1;
 		edge = -1;
 		parent = null;
 		children = new ArrayList<DecisionTreeNode>();
 	}
 	
 	public DecisionTreeNode(int id, int node, int edge, int edgeChoice, int decision){
 		this.id = id;
 		this.decision = decision;
 		this.node = node;
 		this.edge = edge;
 		this.parent = null;
 		this.children = null;
 	}
 	
 	public void addNode(DecisionTreeNode child){
 		if(children == null){
 			children = new ArrayList<DecisionTreeNode>();
 		}
 		child.parent = this;
 		children.add(child);		
 	}
 	
 	public static String printTree(DecisionTreeNode tree, int depth){
 		
 		String answer = "";
 		String space = "";
 		for(int i = 0; i < depth; i++){
 			space += " ";
 		}
 		
 		if(tree.node != -1){
			answer += space+"node: " + tree.node+"| \n";
 			depth += 1;
 		}
 		else if(tree.edge != -1){
 			answer += space+"edge: " + tree.edge+"| \n";
 			depth += 1;
 		}
 		else if(tree.decision != -1){
 			answer += space+"decision: " + tree.decision+"| \n";
 			depth -= 2;
 		}
 		
 		if(tree.children == null){
 			return answer;
 		}
 		
 
 		
 		for(int i = 0; i < tree.children.size(); i++){
 			answer += printTree(tree.children.get(i),depth);
 		}
 		
 		return answer;
 	}
 	
 }
