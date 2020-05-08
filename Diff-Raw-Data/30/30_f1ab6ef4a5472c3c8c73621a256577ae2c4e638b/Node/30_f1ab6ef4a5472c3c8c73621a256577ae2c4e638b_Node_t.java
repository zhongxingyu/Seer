 package tree;
 public class Node{
 	private double value;
 	private String attribute;
 	private int level;
 	public Node leftChild;
 	public Node rightChild;
 
 	public Node()
 	{
 		value=0.0;
 		attribute="";
 		level=0;
 		leftChild=null;
 		rightChild=null;
 	}
 
 	public double getValue()
 	{
 		return value;
 	}
 	public void setValue(float val)
 	{
 		value=val;
 	}
 	public String getAttribute()
 	{
 		return attribute;
 	}
 	public void setAttribute(String attr)
 	{
 		attribute=attr;
 	}
 	public int getLevel()
 	{
 		return level;
 	}
 	public void setLevel(int l)
 	{
 		level = l;
 	}
	public void print()
	{
		//inorder traversal
		
		int i;
		for(i=0;i<level;i++)
			System.out.print("| ");
		System.out.println("Attribute: " + attribute + "    Value: " + value);
		if(leftChild==null)
			return;
		leftChild.print();
		if(rightChild==null)
			return;
		rightChild.print();
	}
 }
