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
public class FlexDenseDoubleMatrix4D extends DenseDoubleMatrix4D
{

	/**
	 * 
	 */
	private static final long	serialVersionUID	= 6863032729864334694L;

	private int					lastIndexSet;

	protected FlexDenseDoubleMatrix4D()
	{
		
	}
	
	public FlexDenseDoubleMatrix4D(int nodes, int annotations)
	{
		super(nodes, annotations);
		lastIndexSet = -1;
	}

	public void setNewSize(int nodesNumber, int annotationsNumber)
	{
		lastIndexSet = -1;
		int newSize = nodesNumber * annotationsNumber * annotationsNumber * annotationsNumber;
		if (this.size() < newSize)
		{
			elements = new double[0];
			elements = new double[newSize];
		}
		else
			for (int i = 0; i < newSize; i += 1)
				elements[i] = 0.0;
		setUpQuick(nodesNumber, annotationsNumber);
	}

	@Override
	public void setQuick(int indexNode1, int annotation1, int annotation2, int annotation3,
			double value)
	{
		lastIndexSet = indexNode1 * firstNodeStride + annotation1 * firstAnnotationStride
				+ annotation2 * secondAnnotationStride + annotation3;
		elements[lastIndexSet] = value;
	}

	public void setQuickNext(double value)
	{
		lastIndexSet += 1;
		elements[lastIndexSet] = value;
	}
}
