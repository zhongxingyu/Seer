package mostrare.crf.tree;

import mostrare.tree.Node;

public class TriangleFeatureWithObservation implements Feature3Observation {

	/**
	 * annotations used for the annotation test
	 */

	private String                          parentNodeType, leftchildNodeType, rightchildNodeType;

	private int								parentAnnotationIndex, leftchildAnnotationIndex, rightchildAnnotationIndex;

	/**
	 * feature number
	 */
	private int								numFeature;

	public TriangleFeatureWithObservation (String typeofparent, String typeofleftchild, String typeofrightchild, 
			int parentAnnotationTest, int leftChildAnnotationTest,
			int rightChildAnnotationTest, 
			 int numFeature)
	{
		this.parentNodeType=typeofparent;
		this.leftchildNodeType=typeofleftchild;
		this.rightchildNodeType=typeofrightchild;
		this.parentAnnotationIndex = parentAnnotationTest;
		this.leftchildAnnotationIndex = leftChildAnnotationTest;
		this.rightchildAnnotationIndex= rightChildAnnotationTest;
        this.numFeature=numFeature;
	}
	
	@Override
	public String getParentNodeType()
	{
		return this.parentNodeType;
	}
	
	@Override
	public String getLeftchildNodeType()
	{
		return this.leftchildNodeType;
	}
	
	@Override
	public String getRightchildNodeType()
	{
		return this.rightchildNodeType;
	}
	
	@Override
	public int getParentAnnotationIndex()
	{
		return this.parentAnnotationIndex;
	}
	
	@Override
	public int getLeftchildAnnotationIndex()
	{
		return this.leftchildAnnotationIndex;
	}
	
	@Override
	public int getRightchildAnnotationIndex()
	{
		return this.rightchildAnnotationIndex;
	}
	
	@Override
	public boolean getObservationTestValueLeftChild(Node leftchildnode)
	{
		
		if(leftchildnode.getParentNode()==null || leftchildnode.getNextSibling()==null )
			return false;
		else {
			if(leftchildnode.getNodeType().equals(this.leftchildNodeType) && 
					leftchildnode.getParentNode().getNodeType().equals(this.parentNodeType)
					&& leftchildnode.getNextSibling().getNodeType().equals(this.rightchildNodeType))
				return true;
			else return false;
		}
	}
	
	@Override
    public boolean whetherNodeHasSameContent(Node leftchildnode, Node rightchildnode) {
		
		if(leftchildnode == null || rightchildnode == null )
			return false;
		
    	if(leftchildnode.getNameofNode().equals(rightchildnode.getNameofNode()))
			return true;
    	else {
    		if(leftchildnode.getNodeType().equals("TypeAccess")) {
    			if(isTypeAccessActualVar(leftchildnode.getNameofNode()) && isTypeAccessActualVar(rightchildnode.getNameofNode()))
					return true;
    		}
    		
    		if(leftchildnode.getNodeType().equals("Literal") && !leftchildnode.getTypeofReturn().trim().isEmpty() &&
    				!rightchildnode.getTypeofReturn().trim().isEmpty()) {
    			if(leftchildnode.getTypeofReturn().trim().toLowerCase().equals(rightchildnode.getTypeofReturn().trim().toLowerCase()) ||
    			   leftchildnode.getTypeofReturn().trim().toLowerCase().endsWith(rightchildnode.getTypeofReturn().trim().toLowerCase())||
    			   rightchildnode.getTypeofReturn().trim().toLowerCase().endsWith(leftchildnode.getTypeofReturn().trim().toLowerCase()))
            		return true;
    		}
    	}
		
		return false;
	}
    
    private boolean isTypeAccessActualVar(String name) {
		
		String[] splitname = name.split("\\.");
		if (splitname.length>1) {
			String simplename=splitname[splitname.length-1];
			if (simplename.toUpperCase().equals(simplename)) 
				return true;
		}		
		
		return false;
	}

	@Override
	public boolean getAnnotationTestValue(int parentAnnotationIndex, int leftchildAnnotationIndex,
			int rightchildAnnotationIndex)
	{
		return this.parentAnnotationIndex == parentAnnotationIndex &&
				this.leftchildAnnotationIndex == leftchildAnnotationIndex &&
				this.rightchildAnnotationIndex == rightchildAnnotationIndex;
	}

	@Override
	public double getValue(Node leftchild, int parentAnnotationIndex, int childAnnotationIndex,
			int siblingAnnotationIndex)
	{
		if(leftchild.getNextSibling() == null)
			return 0.0;
		
		return getAnnotationTestValue(parentAnnotationIndex, childAnnotationIndex,
				siblingAnnotationIndex) && isNodeObservableLeftChild(leftchild)
				&& whetherNodeHasSameContent(leftchild, leftchild.getNextSibling())? 1.0 : 0.0;
	}
	
	@Override
	public boolean isNodeObservableLeftChild(Node node)
	{
		Node[] nodes = getObservableNodesLeftChild(node.getTree().getIndex());
		for (Node observableNode : nodes)
			if (node == observableNode)
				return true;
		return false;
	}
	
	//
	@Override
	public int getAnnotationTestParentVar()
	{
		return parentAnnotationIndex;
	}

	@Override
	public int getAnnotationTestLeftChildVar()
	{
		return leftchildAnnotationIndex;
	}

	@Override
	public int getAnnotationTestRightChildVar()
	{
		return rightchildAnnotationIndex;
	}
	
	@Override
	public Node[] getObservableNodesLeftChild(int treeIndex)
	{
		return observableNodesLeftChild[treeIndex];
	}
	
	@Override
	public void setObservableNodesLeftChild(Node[] nodes, int treeIndex)
	{
		this.observableNodesLeftChild[treeIndex] = nodes;
	}
	
	@Override
	public void initObservableNodesLeftChild(int treesNumber)
	{
		this.observableNodesLeftChild = new Node[treesNumber][];
	}

	private Node[][]	observableNodesLeftChild;

	@Override
	public int getIndex()
	{
		return numFeature;
	}
}
