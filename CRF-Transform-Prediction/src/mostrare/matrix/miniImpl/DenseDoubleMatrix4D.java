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
public class DenseDoubleMatrix4D extends DoubleMatrix4D
{

	/**
	 * 
	 */
	private static final long	serialVersionUID	= 6863032729864334694L;
	/**
	 * elements of the matrix.
	 */
	protected double[]			elements;

	protected DenseDoubleMatrix4D()
	{
		
	}
	
	/**
	 * Constructs a matrix with the given dimensions using memory as specified. All entries are
	 * initially <code>0</code>.
	 * 
	 * @param nodes
	 * @param annotations
	 * @throws IllegalArgumentException
	 */
	public DenseDoubleMatrix4D(int nodes, int annotations)
	{
		setUpQuick(nodes, annotations);
		elements = new double[nodes * annotations * annotations * annotations];
	}

	@Override
	public void setQuick(int indexNode1, int annotation1, int annotation2, int annotation3,
			double value)
	{
		elements[indexNode1 * firstNodeStride + annotation1 * firstAnnotationStride + annotation2
				* secondAnnotationStride + annotation3] = value;
	}

	@Override
	public double getQuick(int indexNode1, int annotation1, int annotation2, int annotation3)
	{
		return elements[indexNode1 * firstNodeStride + annotation1 * firstAnnotationStride
				+ annotation2 * secondAnnotationStride + annotation3];
	}

	@Override
	public double[] getElements()
	{
		return elements;
	}

}
