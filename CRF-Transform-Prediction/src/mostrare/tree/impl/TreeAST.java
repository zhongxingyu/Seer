package mostrare.tree.impl;

import java.util.ArrayList;

import mostrare.tree.Node;
import mostrare.tree.Tree;

public class TreeAST implements Tree {

	protected NodeAST root;

	protected ArrayList<Node>	allnodes;
	
	protected String filename;
	
	protected int numberofTransform;

	public TreeAST(NodeAST rootnode)
	{
		this.root=rootnode;
		init();
	}
	
	public TreeAST()
	{
		init();
	}

	private void init()
	{
		allnodes = new ArrayList<Node>();
	}
	
	public void setRoot(NodeAST rootnode)
	{
		this.root=rootnode;
	}

	public Node getRoot()
	{
		return root;
	}

	@Override
	public void addNode(Node node)
	{
		allnodes.add(node);
	}

	@Override
	public Node getNode(int index)
	{
		return allnodes.get(index);
	}

	@Override
	public int getNodesNumber()
	{
		return allnodes.size();
	}

	@Override
	public boolean annotate(int[] annotations)
	{
		for (int index = 0; index < annotations.length; index += 1)
			getNode(index).setAnnotation(annotations[index]);
		return true;
	}

	@Override
	public void indexesNodes()
	{
		
	}

	@Override
	public Node[] getNodes()
	{
		return allnodes.toArray(new Node[allnodes.size()]);
	}

	private int	index;

	@Override
	public int getIndex()
	{
		return index;
	}

	@Override
	public void setIndex(int index)
	{
		this.index = index;
	}
	
	@Override
	public void setFileNmae(String name) {
		this.filename=name;
	}
	
	@Override
	public String getFileName() {
		return this.filename;
	}
	
	@Override
	public void setNumberofTransform(int transformNumber) {
		this.numberofTransform = transformNumber;
	}
	
	@Override
	public int getNumberofTransform() {
		return this.numberofTransform;
	}
}

