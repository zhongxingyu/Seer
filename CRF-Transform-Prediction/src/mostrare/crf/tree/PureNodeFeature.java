package mostrare.crf.tree;

import mostrare.tree.Node;

public class PureNodeFeature implements Feature1Pure {
	
	private String nodetype;
	/**
	 * annotation index used in the observation test
	 */
	private int						annotationIndex;

	/**
	 * feature number
	 */
	private int							numFeature;

	public PureNodeFeature (String nodetype, int annotationIndex, int numFeature)
	{
		this.nodetype = nodetype;
		this.annotationIndex = annotationIndex;
		this.numFeature=numFeature;
	}
	
	@Override
	public int getAnnotationIndex()
	{	
		return this.annotationIndex;
	} 

	@Override
	public String getNodetype()
	{	
		return this.nodetype;
	}
	
	@Override
	public boolean getObservationTestValue(Node node)
	{	
		return node.getNodeType().equals(this.nodetype);
//				|| (this.nodetype.equals("RootLogical") 
//				&& !node.getLogicalExpressionIdentity().isEmpty()) || 
//				(this.nodetype.equals("ExpUnderStudy") && !node.getExpressionIdentity().isEmpty());
	}

	@Override
	public boolean getAnnotationTestValue(int annotationIndex)
	{
		return this.annotationIndex == annotationIndex;
	}

	@Override
	public double getValue(Node node, int annotationIndex)
	{
		return getAnnotationTestValue(annotationIndex) && isNodeObservable(node) ? 1.0 : 0.0;
	}

	@Override
	public boolean isNodeObservable(Node node)
	{
		Node[] nodes = getObservableNodes(node.getTree().getIndex());
		for (Node observableNode : nodes)
			if (node == observableNode)
				return true;
		return false;
	}

	@Override
	public int getAnnotationTestVar()
	{
		return annotationIndex;
	}

	@Override
	public Node[] getObservableNodes(int treeIndex)
	{
		return observableNodes[treeIndex];
	}

	@Override
	public void setObservableNodes(Node[] nodes, int treeIndex)
	{
		this.observableNodes[treeIndex] = nodes;
	}

	@Override
	public void initObservableNodes(int treesNumber)
	{
		this.observableNodes = new Node[treesNumber][];
	}

	private Node[][] observableNodes;

	@Override
	public int getIndex()
	{
		return numFeature;
	}
}
