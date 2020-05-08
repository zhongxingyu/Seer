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
package mostrare.crf.tree;

import java.io.Serializable;

import mostrare.tree.Node;

/**
 * Interface for a triangle feature.
 * 
 * @author missi
 */
public interface Feature3 extends Serializable
{
	public String getParentNodeType();
	
	public String getLeftchildNodeType();
	
	public String getRightchildNodeType();
	
	public int getParentAnnotationIndex();
	
	public int getLeftchildAnnotationIndex();
	
	public int getRightchildAnnotationIndex();
	
	/**
	 * Applies a test on the input observation (the node).
	 * 
	 * @param node
	 *            node
	 * @return true if the test is successful.
	 */
	public boolean getObservationTestValueLeftChild(Node leftchildnode);

	/**
	 * Applies a test on the annotations specified by <code>parentAnnotationIndex</code>, by
	 * <code>childAnnotationIndex</code> and by <code>siblingAnnotationIndex</code>.
	 * 
	 * @param parentAnnotationIndex
	 *            annotation for the parent node
	 * @param childAnnotationIndex
	 *            annotation for the left child node
	 * @param siblingAnnotationIndex
	 *            annotation for the right child node
	 * @return true if the test is successful.
	 */
	public boolean getAnnotationTestValue(int parentAnnotationIndex, int leftchildAnnotationIndex,
			int rightchildAnnotationIndex);

	/**
	 * Computes the feature value that is returned by the method getValue.
	 * 
	 * @param node
	 *            node
	 * @param parentAnnotationIndex
	 *            annotation for the parent node
	 * @param childAnnotationIndex
	 *            annotation for the left child node
	 * @param siblingAnnotationIndex
	 *            annotation for the right child node
	 * @return the feature value when all the feature tests are successful
	 */
	
//	public double computeValue(Node parentnode, Node leftchild, Node rightchild, int parentAnnotationIndex, int leftchildAnnotationIndex,
//			int rightchildAnnotationIndex);
	/**
	 * Evaluates the value of the feature for the clique identified by the node. The node is in fact
	 * the child node of the clique. So the annotation of the parent node is specified by
	 * <code>parentAnnotationIndex</code>, the annotation of the child node is associated with
	 * <code>childAnnotationIndex</code> and the annotation of the second child node is given by
	 * <code>siblingAnnotationIndex</code>.
	 * 
	 * @param node
	 *            node
	 * @param parentAnnotationIndex
	 *            annotation of the parent node
	 * @param childAnnotationIndex
	 *            annotation of the child node
	 * @param siblingAnnotationIndex
	 *            annotation of the second child node
	 * @return the value of the feature.
	 */
	
	public double getValue(Node leftchild, int parentAnnotationIndex, int childAnnotationIndex,
			int siblingAnnotationIndex);

	/**
	 * Returns the index of the annotations used in the annotation test for the parent node.
	 * 
	 * @return the index of the annotations used in the annotation test for the parent node.
	 */
	public int getAnnotationTestParentVar();

	/**
	 * Returns the index of the annotation used in the annotation test for the left child node.
	 * 
	 * @return the index of the annotation used in the annotation test for the left child node.
	 */
	public int getAnnotationTestLeftChildVar();

	/**
	 * Returns the index of the annotation used in the annotation test for the right child node.
	 * 
	 * @return the index of the annotation used in the annotation test for the right child node.
	 */
	public int getAnnotationTestRightChildVar();

	/**
	 * Returns the nodes the observation tests are successful with.
	 * 
	 * @param treeIndex
	 *            index of the tree the returned nodes belong to
	 * @return the nodes the observation tests are successful with.
	 */
	public Node[] getObservableNodesLeftChild(int treeIndex);

	/**
	 * Checks if the observation tests of the feature are successful with the provided
	 * <code>node</code>.
	 * 
	 * @param node
	 *            node
	 * @return <code>true</code> if the observation tests of the feature are successful with the
	 *         provided <code>node</code>.
	 */
	public boolean isNodeObservableLeftChild(Node node);

	/**
	 * Saves the nodes the observation tests are successful with.
	 * 
	 * @param nodes
	 *            the set of nodes the observation tests are successful with
	 * @param treeIndex
	 *            index of the tree the nodes belong to
	 */
	public void setObservableNodesLeftChild(Node[] nodes, int treeIndex);


	/**
	 * Initializes the data before setting the nodes the observation tests are successful with.
	 * 
	 * @param treesNumber
	 *            number of trees the feature will be used with
	 */
	public void initObservableNodesLeftChild(int treesNumber);

	/**
	 * Returns the feature index.
	 * 
	 * @return the feature index.
	 */
	public int getIndex();
}
