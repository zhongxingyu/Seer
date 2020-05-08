 package parser;
 
 import java.util.ArrayList;
 import java.util.Stack;
 
 public class StackToTree {	
 	private ArrayList<Tree<String>> trees;
 	private ArrayList<String> operands;	
 	
 	public StackToTree(ArrayList<Stack<String>> stacks, ArrayList<String> ids) {		
 		
 		trees = new ArrayList<Tree<String>>();
 		operands = new ArrayList<String>();
 		
 		operands.add("|");
 		operands.add("*");
 		operands.add("+");
 		operands.add("-");
 		operands.add("^");
 		operands.add(".");
 		operands.add("(");
 		operands.add(")");
 		operands.add("[");
 		operands.add("]");
 		
 		for(int i=0; i<ids.size(); i++){
 			trees.add(makeTree(ids.get(i), stacks.get(i)));			
 		}		
 	}
 	
 	private Tree<String> makeTree(String id, Stack<String> s) {		
 		
 		if(s.isEmpty())
 			return null;
 		
 		Tree<String> result = new Tree<String>(id);		
 
 		String str = s.pop();
		System.out.println(str);
 			
 		if(operands.contains(str)){
 			Node<String> nd = new Node<String>(str, result.getRoot());
 			result.addChild(nd);
 			makeTreeHelper(nd, s);
 				
 		}else {
 			result.getRoot().addChild(new Node<String>(str, result.getRoot()));
 		}				
 		
 		return result;
 	}
 	
 	private Node<String> makeTreeHelper(Node<String> parent, Stack<String> s) {
 		
 		if(s.isEmpty())
 			return null;
 		
 		String str = s.pop();
 		System.out.println(str);
 			
 		if(operands.contains(str)){
 			Node<String> nd = new Node<String>(str, parent);
 			parent.addChild(nd);
 			makeTreeHelper(nd, s);
 		
 		}else {
 			parent.addChild(new Node<String>(str, parent));
 		}				
 				
 		return parent;			
 	}
 		
 	public ArrayList<Tree<String>> getTrees(){
 		return trees;
 	}
 	
 	public void printTree(Node<String> head) {
 		if(head.hasChild()) {
 			for(Node<String> child: head.getChildren()) {
 				printTree(child);
 			}
 		}else {
 			System.out.println("Parent: " + head.getParent().getData() + " / Current Node: " + head.getData());			
 		}		
 	}
 	
 }
