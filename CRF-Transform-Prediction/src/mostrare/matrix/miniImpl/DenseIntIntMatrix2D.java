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
public class DenseIntIntMatrix2D extends IntIntMatrix2D
{

	/**
	 * 
	 */
	private static final long	serialVersionUID	= -3976647376342306647L;

	protected int[]				elements;

	protected int[]				resetArr;

	public DenseIntIntMatrix2D(int nodes, int annotations)
	{
		this.setUp(nodes, annotations);
		elements = new int[nodes * annotations];
		resetArr = new int[nodes * annotations];
	}

	@Override
	public void setQuick(int indexNode1, int annotation1, int value)
	{
		elements[indexNode1 * rowStride + annotation1] = value;
	}

	@Override
	public int getQuick(int indexNode1, int annotation1)
	{
		return elements[indexNode1 * rowStride + annotation1];
	}

	@Override
	public void reset()
	{
		System.arraycopy(resetArr, 0, elements, 0, size());
	}
}
