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

/**
 * @author missi
 */
public abstract class AbstractMatrix4D extends AbstractMatrixForFeature
{
	protected int	firstNodeStride, firstAnnotationStride, secondAnnotationStride,
			thirdAnnotationStride;

	@Override
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
	protected void setUp(int maxNodes, int maxAnnotations)
	{
		int numElemClique = 3;

		if (maxNodes <= 0 || maxAnnotations <= 0)
			throw new IllegalArgumentException("negative size");
		if (size(numElemClique, maxNodes, maxAnnotations) > Integer.MAX_VALUE)
			throw new IllegalArgumentException("matrix too large");

		setUpQuick(maxNodes, maxAnnotations);
	}

	@Override
	/**
	 * Sets up a matrix with the given dimensions and the given strides.
	 * 
	 * @param numElemClique
	 * @param maxNodes
	 * @param maxAnnotations
	 * @param firstNodeStride
	 * @param firstAnnotationStride
	 * @param secondAnnotationStride
	 * @param thirdAnnotationStride
	 */
	protected void setUp(int numElemClique, int maxNodes, int maxAnnotations, int... strides)
	{
		super.setUp(numElemClique, maxNodes, maxAnnotations, strides);
		this.firstNodeStride = strides[0];
		this.firstAnnotationStride = strides[1];
		this.secondAnnotationStride = strides[2];
		this.thirdAnnotationStride = strides[3];
	}

	@Override
	/**
	 * Sets a matrix with the given dimensions. Preconditions unchecked.
	 * 
	 * @param maxNodes
	 * @param maxAnnotations
	 */
	protected void setUpQuick(int maxNodes, int maxAnnotations)
	{
		int thirdAnnotationStride = 1;
		int secondAnnotationStride = maxAnnotations;
		int firstAnnotationStride = maxAnnotations * secondAnnotationStride;
		int firstNodeStride = firstAnnotationStride * maxAnnotations;
		setUp(3, maxNodes, maxAnnotations, firstNodeStride, firstAnnotationStride,
				secondAnnotationStride, thirdAnnotationStride);
	}

}
