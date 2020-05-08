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
package mostrare.tree;

import java.util.Map;

import mostrare.coltAdaptation.list.NodeArrayList;
import mostrare.crf.tree.CRF;
import mostrare.crf.tree.impl.CRFWithConstraintNode;

/**
 * A Node can be ordered or not and can be an internal node or a leaf.
 * 
 * @author missi
 */
public interface Node
{
	
	public void init();

	/**
	 * Returns the index of the annotation associated with the leaf.
	 * 
	 * @return the index of the annotation
	 */
	public int getAnnotation();

	
	/**
	 * Provides an annotation to the leaf.
	 * 
	 * @param annotation
	 *            the index of the annotation
	 */
	public void setAnnotation(int annotation);

	/**
	 * Returns the index of the node.
	 * 
	 * @return the index
	 */
	public abstract int getIndex();

	/**
	 * Sets the index of the node.
	 * 
	 * @param index
	 *            an index
	 */
	public abstract void setIndex(int index);

	/**
	 * Returns the parent of the node.
	 * 
	 * @return the parent
	 */
	public abstract Node getParentNode();

	/**
	 * Sets the parent of the node.
	 * 
	 * @param parent
	 *            a node
	 */
	public abstract void setParentNode(Node parent);

	/**
	 * Return the number of ordered children of the node.
	 * 
	 * @return the number of ordered children
	 */
	public abstract int getOrderedNodesNumber();
	
	public abstract void setOrderedNodesNumber(int number);

	/**
	 * Returns the <code>index</code>-th of the node.
	 * 
	 * @param index
	 * @return the <code>index</code>-th of the node.
	 */
	public Node getOrderedNodeAt(int index);

	/**
	 * Returns the tree to which the node belongs.
	 * 
	 * @return the tree
	 */
	public abstract Tree getTree();

	/**
	 * Sets the tree to which the node belongs.
	 * 
	 * @param tree
	 */
	public abstract Node getNextSibling();

	/**
	 * Sets the node <code>nextSibling</code> as the next sibling of the node.
	 * 
	 * @param nextSibling
	 *            a node
	 */
	public abstract void setNextSibling(Node nextSibling);

	/**
	 * Returns the previous sibling of the node.
	 * 
	 * @return the previous sibling or <code>null</code> if the node has no next sibling.
	 */
	public abstract Node getPreviousSibling();

	/**
	 * Sets the node <code>previousSibling</code> as the previous sibling of the node.
	 * 
	 * @param previousSibling
	 *            a node
	 */
	public abstract void setPreviousSibling(Node previousSibling);

	/**
	 * Returns the position of the node in comparison with its siblings.
	 * 
	 * @return the position of the node
	 */
	public abstract int getPosition();

	/**
	 * Sets the position of the node in comparison with its siblings.
	 * 
	 * @param position
	 */
	public abstract void setPosition(int position);
	
	/**
	 * Returns the first child of the node.
	 * 
	 * @return the first child
	 */
	public abstract Node getFirstChild();

	/**
	 * Sets the first child of the node.
	 * 
	 * @param firstChild
	 *            a node
	 */
	public abstract void setFirstChild(Node firstChild);
	
	public abstract void setTree(Tree tree);
	
	public abstract String getNodeLabel();
	
	public abstract void setNodeLabel(String label);
	
	public abstract String getNodeType();
	
	public abstract void setNodeType(String typeinfo);
	
	public abstract void addNode(Node child);
	
	public abstract int getCharacterIndex();
	
    public abstract void setCharacterIndex(int indexofcharacter);
	
	public abstract boolean getCharacterValue(int indexofcharacter);
	
	public abstract void setExpressionIdentity(String expID);
	
	public abstract String getExpressionIdentity();
	
	public abstract void setLogicalExpressionIdentity(String logexpID);
	
	public abstract String getLogicalExpressionIdentity();
	
	public abstract void setBinaryOperatorIdentity(String binoperatorID);
	
	public abstract String getBinaryOperatorIdentity();
	
	public abstract void setNameofNode (String NodeName);
	
	public abstract String getNameofNode();
	
	public abstract void setTypeofReturn(String returntype);
	
	public abstract String getTypeofReturn();
	
//	public abstract boolean checkNodeCharacter(String Character);

//	public abstract boolean isAnnotationAllowedForNode(int annotationindex);
//	
//	public abstract boolean isNodeAnnotable(int annotationindex);
	
//	public abstract int getCharacterIndex();
//	
//	public abstract boolean getCharacterValue();

//	public abstract boolean isAnnotationAllowedForEdgeChild(int parentAnnotationindex,
//			int childAnnotationindex);
//	
//	public abstract boolean isTriangleAnnotable(int parentAnnotationindex,
//			int leftChildAnnotationindex, int rightChildAnnotationindex);
	
	public abstract NodeArrayList getOrderedChildrenNodes();

	public abstract void setOrderedorderedChildrenNodes(NodeArrayList ChildrenNodes);
	
	public abstract CRF getStudiedCRF();

	public abstract void setStudiedCRF(CRFWithConstraintNode CRFtostudy);
	
	public Map<Integer,Boolean> getCharacterValueMap();

	public void setCharacterValueMap(Map<Integer,Boolean> studiedmap);
}
