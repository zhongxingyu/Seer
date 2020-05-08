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
public abstract class DoubleMatrix2D extends AbstractMatrix2D
{

	/**
	 * Sets the matrix cell at coordinate <code>coordinates</code> to the specified value.
	 * 
	 * @param indexNode1
	 * @param annotation1
	 * @param value
	 *            the value to be filled into the specified cell.
	 * @throws IndexOutOfBoundsException
	 *             if there are not the valid number of coordinates or if a coordinate is out of
	 *             bounds.
	 */
	public void set(int indexNode1, int annotation1, double value)
	{
		if (indexNode1 < 0 || indexNode1 >= nodes || annotation1 < 0 || annotation1 >= annotations)
			throw new IndexOutOfBoundsException("Wrong coordinate");
		setQuick(indexNode1, annotation1, value);
	}

	/**
	 * Sets the matrix cell at the <code>coordinates</code> to the specified value.
	 * <p>
	 * Provided with invalid parameters this method may access illegal indexes without throwing any
	 * exception. <b>You should only use this method when you are absolutely sure that the
	 * coordinate is within bounds.</b> Preconditions unchecked.
	 * 
	 * @param indexNode1
	 * @param annotation1
	 * @param value
	 *            the value to be filled into the specified cell.
	 */
	public abstract void setQuick(int indexNode1, int annotation1, double value);

	/**
	 * Returns the matrix cell value at coordinate <code>coordinates</code>.
	 * 
	 * @param indexNode1
	 * @param annotation1
	 * @return the value of the specified cell.
	 * @throws IndexOutOfBoundsException
	 *             if there are not the valid number of coordinates or if a coordinate is out of
	 *             bounds.
	 */
	public double get(int indexNode1, int annotation1)
	{
		if (indexNode1 < 0 || indexNode1 >= nodes || annotation1 < 0 || annotation1 >= annotations)
			throw new IndexOutOfBoundsException("Wrong coordinate");

		return getQuick(indexNode1, annotation1);
	}

	/**
	 * Returns the matrix cell value at coordinate <code>coordinates</code>.
	 * <p>
	 * Provided with invalid parameters this method may access illegal indexes without throwing any
	 * exception. <b>You should only use this method when you are absolutely sure that the
	 * coordinate is within bounds.</b> Preconditions unchecked.
	 * 
	 * @param indexNode1
	 * @param annotation1
	 * @return the value of the specified cell.
	 */
	public abstract double getQuick(int indexNode1, int annotation1);

	public abstract double[] getElements();
}
