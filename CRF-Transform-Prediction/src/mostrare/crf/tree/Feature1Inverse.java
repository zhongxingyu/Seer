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
 * Interface for a node feature.
 * 
 * @author missi
 */
public interface Feature1Inverse extends Serializable
{
	public int getCharacterIndex();
	
	public int getAnnotationIndex();
	
	public String getNodetype();
	/**
	 * Applies a test on the input observation (the node).
	 * 
	 * @param node
	 *            node
	 * @return <code>true</code> if the test is successful.
	 */
	public boolean getObservationTestValue(Node node);

	/**
	 * Applies a test on the annotation specified by <code>annotationIndex</code>.
	 * 
	 * @param annotationIndex
	 * @return <code>true</code> if the test is successful.
	 */
	public boolean getAnnotationTestValue(int annotationIndex);

	/**
	 * Computes the feature value that is returned by the method getValue.
	 * 
	 * @param node
	 *            node
	 * @param annotationIndex
	 *            annotation index
	 * @return the feature value when all the feature tests are successful.
	 */
//	public double computeValue(Node node, int annotationIndex, int indexofcharacter, boolean characterValue);

	/**
	 * Evaluates the value of the feature for the node with the annotation associated with
	 * <code>annotationIndex</code>.
	 * 
	 * @param node
	 *            node
	 * @param annotationIndex
	 *            annotation of the node
	 * @return the value of the feature.
	 */

	public double getValue(Node node, int annotationIndex);

	/**
	 * Returns the index of the annotation used in the annotation test.
	 * 
	 * @return the index of the annotation used in the annotation test.
	 */
	public int getAnnotationTestVar();

	/**
	 * Returns the nodes the observation tests are successful with.
	 * 
	 * @param treeIndex
	 *            index of the tree the returned nodes belong to
	 * @return the nodes the observation tests are successful with.
	 */
	public Node[] getObservableNodes(int treeIndex);

	/**
	 * Checks if the observation tests of the feature are successful with the provided
	 * <code>node</code>.
	 * 
	 * @param node
	 *            node
	 * @return <code>true</code> if the observation tests of the feature are successful with the
	 *         provided <code>node</code>.
	 */
	public boolean isNodeObservable(Node node);

	/**
	 * Saves the nodes the observation tests are successful with.
	 * 
	 * @param nodes
	 *            the set of nodes the observation tests are successful with
	 * @param treeIndex
	 *            index of the tree the nodes belong to
	 */
	public void setObservableNodes(Node[] nodes, int treeIndex);

	/**
	 * Initializes the data before setting the nodes the observation tests are successful with.
	 * 
	 * @param treesNumber
	 *            number of trees the feature will be used with
	 */
	public void initObservableNodes(int treesNumber);

	/**
	 * Returns the feature index.
	 * 
	 * @return the feature index.
	 */
	public int getIndex();
	
	public boolean getCharacterTestValue(Node node);

}