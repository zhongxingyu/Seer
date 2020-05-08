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
 * Interface for an edge feature.
 * 
 * @author missi
 */
public interface Feature2 extends Serializable
{
	public String getParentNodeType();
	
	public String getChildNodeType();
	
	public int getParentAnnotationIndex();

	public int getChildAnnotationIndex();
	
	/**
	 * Applies a test on the input observation (the node).
	 * 
	 * @param node
	 *            node
	 * @return <code>true</code> if the test is successful.
	 */
	public boolean getObservationTestValueChild(Node childnode);
	
	/**
	 * Applies a test on the annotations specified by <code>parentAnnotationIndex</code> and by
	 * <code>childAnnotationIndex</code>.
	 * 
	 * @param parentAnnotationIndex
	 *            annotation for the parent node
	 * @param childAnnotationIndex
	 *            annotation for the child node
	 * @return <code>true</code> if the test is successful.
	 */
	public boolean getAnnotationTestValue(int parentAnnotationIndex, int childAnnotationIndex);

	/**
	 * Computes the feature value that is returned by the method getValue.
	 * 
	 * @param node
	 *            node
	 * @param parentAnnotationIndex
	 *            index of the annotation for the parent node of the clique
	 * @param childAnnotationIndex
	 *            index of the annotation for the child node of the clique
	 * @return the feature value when all the feature tests are successful.
	 */
//	public double computeValue(Node childnode, int parentAnnotationIndex, int childAnnotationIndex);
	/**
	 * Evaluates the value of the feature for the clique identified by the node . The node is in
	 * fact the child node of the clique. So the annotation of the parent node is specified by
	 * <code>parentAnnotationIndex</code> and the annotation of the child node is associated with
	 * <code>childAnnotationIndex</code>.
	 * 
	 * @param node
	 *            node
	 * @param parentAnnotationIndex
	 *            annotation of the parent node
	 * @param childAnnotationIndex
	 *            annotation of the child node
	 * @return the value of the feature.
	 */
	public double getValue(Node childnode, int parentAnnotationIndex, int childAnnotationIndex);

	/**
	 * Returns the index of the annotation used in the annotation test for the parent node.
	 * 
	 * @return the index of the annotation used in the annotation test for the parent node.
	 */
	public int getAnnotationTestParentVar();

	/**
	 * Returns the index of the annotation used in the annotation test for the child node.
	 * 
	 * @return the index of the annotation used in the annotation test for the child node.
	 */
	public int getAnnotationTestChildVar();

	/**
	 * Returns the nodes the observation tests are successful with.
	 * 
	 * @param treeIndex
	 *            index of the tree the returned nodes belong to
	 * @return the nodes the observation tests are successful with.
	 */
	public Node[] getObservableNodesChild(int treeIndex);


	/**
	 * Checks if the observation tests of the feature are successful with the provided
	 * <code>node</code>.
	 * 
	 * @param node
	 *            node
	 * @return <code>true</code> if the observation tests of the feature are successful with the
	 *         provided <code>node</code>.
	 */
	public boolean isNodeObservableChild(Node node);
	/**
	 * Saves the nodes the observation tests are successful with.
	 * 
	 * @param nodes
	 *            the set of nodes the observation tests are successful with
	 * @param treeIndex
	 *            index of the tree the nodes belong to
	 */
	public void setObservableNodesChild (Node[] nodes, int treeIndex);


	/**
	 * Initializes the data before setting the nodes the observation tests are successful with.
	 * 
	 * @param treesNumber
	 *            number of trees the feature will be used with
	 */
	public void initObservableNodesChild(int treesNumber);

	/**
	 * Returns the feature index.
	 * 
	 * @return the feature index.
	 */
	public int getIndex();
}
