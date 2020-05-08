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

public class EdgeFeature implements Feature2
{
	/**
	 * annotations used for the annotation test
	 */
	private String                      parentNodeType, childNodeType;
	private int				            parentAnnotationIndex, childAnnotationIndex;

	/**
	 * feature index
	 */
	private int							numFeature; 

	public EdgeFeature(String typeofparent, String typeofchild, int parentAnnotationTest, int childAnnotationTest,
			 int numFeature)
	{
		this.parentNodeType=typeofparent;
		this.childNodeType=typeofchild;
		this.parentAnnotationIndex = parentAnnotationTest;
		this.childAnnotationIndex = childAnnotationTest;
		this.numFeature=numFeature;
	}
	
	@Override
	public String getParentNodeType()
	{
		return this.parentNodeType;
	}
	
	@Override
	public String getChildNodeType()
	{
		return this.childNodeType;
	}
	
	@Override
	public int getParentAnnotationIndex()
	{
		return this.parentAnnotationIndex;
	}
	
	@Override
	public int getChildAnnotationIndex()
	{
		return this.childAnnotationIndex;
	}
	
	@Override
	public boolean getObservationTestValueChild(Node childnode)
	{
		if(childnode.getParentNode()==null)
			return false;
		else {
		   if (childnode.getNodeType().equals(this.childNodeType) 
				&& childnode.getParentNode().getNodeType().equals(this.parentNodeType))
			  return true;
		   else return false;
		}
	}

	@Override
	public boolean getAnnotationTestValue(int parentAnnotationIndex, int childAnnotationIndex)
	{
		return (this.parentAnnotationIndex == parentAnnotationIndex && this.childAnnotationIndex == childAnnotationIndex);
	}

	@Override
	public double getValue(Node childnode, int parentAnnotationIndex, int childAnnotationIndex)
	{
		return getAnnotationTestValue(parentAnnotationIndex, childAnnotationIndex) &&
				 isNodeObservableChild(childnode) ?
						1.0 : 0.0;
	}
	
	@Override
	public boolean isNodeObservableChild(Node node)
	{
		Node[] nodes = getObservableNodesChild(node.getTree().getIndex());
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
	public int getAnnotationTestChildVar()
	{
		return childAnnotationIndex;
	}
	
	@Override
	public Node[] getObservableNodesChild(int treeIndex)
	{
		return observableNodesChild[treeIndex];
	}
	
	@Override
	public void setObservableNodesChild (Node[] nodes, int treeIndex)
	{
		this.observableNodesChild[treeIndex] = nodes;
	}
	
	@Override
	public void initObservableNodesChild(int treesNumber)
	{
		this.observableNodesChild = new Node[treesNumber][];
	}
	
	private Node[][]	observableNodesChild;

	@Override
	public int getIndex()
	{
		return numFeature;
	}
}

