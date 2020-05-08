package mostrare.crf.tree;
/*
 * Copyright (C) 2006-2007 MOSTRARE INRIA Project
 * 
 * This file is part of XCRF, an implementation of CRFs for trees (http://treecrf.gforge.inria.fr)
 * 
 * XCRF is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * XCRF is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with XCRF; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

import mostrare.tree.Node;

public class TriangleFeature implements Feature3
{
	/**
	 * annotations used for the annotation test
	 */

	private String                          parentNodeType, leftchildNodeType, rightchildNodeType;

	private int								parentAnnotationIndex, leftchildAnnotationIndex, rightchildAnnotationIndex;

	/**
	 * feature number
	 */
	private int								numFeature;

	public TriangleFeature(String typeofparent, String typeofleftchild, String typeofrightchild, 
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
		return getAnnotationTestValue(parentAnnotationIndex, childAnnotationIndex,
				siblingAnnotationIndex) && isNodeObservableLeftChild(leftchild)? 1.0 : 0.0;
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