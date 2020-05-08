 import java.util.ArrayList;
 
 
 public class Node 
 {
	private int weight;
 	private String string;
 	private Node leftNode;
 	private Node rightNode;
 	
 	public Node(String s)
 	{
 		string = s;
		weight = string.length();
 	}
 	
 	
 	public void addChild(Node n)
 	{
 		
 	}
 	public String getString()
 	{
 		return string;
 	}
 	
 	//To calculate the weight, get the weight of the node's left child and the weight of all
 	//the nodes in it's right subtree
 	
	//If the node is a leaf then return the length of the
	public int getWeight(Node n)
 	{
 		if(n.getRight() == null)
 		{
			return n.string.length();
 		}
		return n.string.length() + getWeight(n.getRight());
 	}
 	
 	public Node getLeft()
 	{
 		return leftNode;
 	}
 	public Node getRight()
 	{
 		return rightNode;
 	}
 }
