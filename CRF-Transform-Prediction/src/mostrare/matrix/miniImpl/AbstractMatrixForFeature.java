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
package mostrare.matrix.miniImpl;

import cern.colt.matrix.impl.AbstractMatrix;

/**
 * @author missi
 */
public abstract class AbstractMatrixForFeature extends AbstractMatrix
{

	protected int	numElemClique;

	protected int	nodes;

	protected int	annotations;

	public int getNbNodes()
	{
		return nodes;
	}

	public int getNbAnnotations()
	{
		return annotations;
	}

	public static int size(int numElemClique, int nodes, int annotations)
	{
		return (int) (nodes * StrictMath.pow(annotations, numElemClique));
	}

	@Override
	/**
	 * Returns the number of cells which is the product of the elements of <i>maxElemBySlice</i>.
	 * 
	 * @returns number of cells
	 */
	public int size()
	{
		return size(numElemClique, nodes, annotations);
	}

	/**
	 * Sets a matrix relative to a clique of <code>numElemClique</code> nodes and according to
	 * <code>maxNodes</code> nodes and <code>maxAnnotation</code> annotations.
	 * 
	 * @param maxNodes
	 *            number of nodes.
	 * @param maxAnnotations
	 *            number of annotations.
	 * @throws IllegalArgumentException
	 *             if <code>size > Integer.MAX_VALUE</code>.
	 * @throws IllegalArgumentException
	 *             if <code>numElemClique<=0 or nodes<=0 or annotations<=0</code>.
	 */
	protected abstract void setUp(int maxNodes, int maxAnnotations);

	/**
	 * Sets up a matrix with the given dimensions and the given strides.
	 * 
	 * @param numElemClique
	 * @param maxNodes
	 * @param maxAnnotations
	 * @strides
	 */
	protected void setUp(int numElemClique, int maxNodes, int maxAnnotations, int... strides)
	{
		this.numElemClique = numElemClique;
		this.nodes = maxNodes;
		this.annotations = maxAnnotations;
	}

	/**
	 * Sets a matrix with the given dimensions. Preconditions unchecked.
	 * 
	 * @param maxNodes
	 *            number of nodes.
	 * @param maxAnnotations
	 *            number of annotations.
	 */
	protected abstract void setUpQuick(int maxNodes, int maxAnnotations);

}
