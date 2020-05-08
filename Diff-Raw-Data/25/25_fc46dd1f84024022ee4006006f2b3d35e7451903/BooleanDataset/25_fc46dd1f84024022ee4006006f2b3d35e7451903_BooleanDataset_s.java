 /*-
  * Copyright © 2010 Diamond Light Source Ltd.
  *
  * This file is part of GDA.
  *
  * GDA is free software: you can redistribute it and/or modify it under the
  * terms of the GNU General Public License version 3 as published by the Free
  * Software Foundation.
  *
  * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  *
  * You should have received a copy of the GNU General Public License along
  * with GDA. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package uk.ac.diamond.scisoft.analysis.dataset;
 
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 
 /**
  * Extend boolean base dataset for boolean values
  */
 public class BooleanDataset extends BooleanDatasetBase {
 	// pin UID to base class
 	private static final long serialVersionUID = AbstractDataset.serialVersionUID;
 
 	/**
 	 * Setup the logging facilities
 	 */
 	transient private static final Logger logger = LoggerFactory.getLogger(BooleanDataset.class);
 
 	public BooleanDataset() {
 		super();
 	}
 
 	/**
 	 * @param shape
 	 */
 	public BooleanDataset(final int... shape) {
 		super(shape);
 	}
 
 	/**
 	 * Create a dataset using given data
 	 * 
 	 * @param data
 	 * @param shape (can be null to create 1D dataset)
 	 */
 	public BooleanDataset(final boolean[] data, int... shape) {
 		super(data, shape);
 	}
 
 	/**
 	 * Copy a dataset
 	 * 
 	 * @param dataset
 	 */
 	public BooleanDataset(final BooleanDataset dataset) {
 		this(dataset, false);
 	}
 
 	/**
 	 * Copy a dataset or just wrap in a new reference (for Jython sub-classing)
 	 * 
 	 * @param dataset
 	 * @param wrap
 	 */
 	public BooleanDataset(final BooleanDataset dataset, final boolean wrap) {
 		super(dataset, wrap);
 	}
 
 	/**
 	 * Cast a dataset to this class type
 	 * 
 	 * @param dataset
 	 */
 	public BooleanDataset(final AbstractDataset dataset) {
 		super(dataset);
 	}
 
 	/**
 	 * Create a dataset from an object which could be a PySequence, a Java array (of arrays...)
 	 * or Number. Ragged sequences or arrays are padded with zeros.
 	 * 
 	 * @param obj
 	 * @return dataset with contents given by input
 	 */
 	public static BooleanDataset createFromObject(final Object obj) {
 		BooleanDatasetBase result = BooleanDatasetBase.createFromObject(obj);
 		return new BooleanDataset(result.data, result.shape);
 	}
 
 	/**
 	 * @param shape
 	 * @return a dataset filled with ones
 	 */
 	public static BooleanDataset ones(final int... shape) {
 		BooleanDatasetBase result = BooleanDatasetBase.ones(shape);
 		return new BooleanDataset(result.data, result.shape);
 	}
 
 	@Override
 	public boolean getElementBooleanAbs(final int index) {
 		return data[index];
 	}
 
 	@Override
 	public double getElementDoubleAbs(final int index) {
 		return data[index] ? 1 : 0;
 	}
 
 	@Override
 	public long getElementLongAbs(final int index) {
 		return data[index] ? 1 : 0;
 	}
 
 	@Override
 	public double getDouble(final int... pos) {
 		return getInt(pos);
 	}
 
 	@Override
 	public float getFloat(final int... pos) {
 		return getInt(pos);
 	}
 
 	@Override
 	public long getLong(final int... pos) {
 		return getInt(pos);
 	}
 
 	@Override
 	public int getInt(final int... pos) {
 		return get(pos) ? 1 : 0;
 	}
 
 	@Override
 	public short getShort(final int... pos) {
 		return (short) getInt(pos);
 	}
 
 	@Override
 	public byte getByte(final int... pos) {
 		return (byte) getInt(pos);
 	}
 
 	@Override
 	public boolean getBoolean(final int... pos) {
 		return get(pos);
 	}
 
 	@Override
 	public BooleanDataset getSlice(final int[] start, final int[] stop, final int[] step) {
 		BooleanDatasetBase result = super.getSlice(start, stop, step);
 
 		return new BooleanDataset(result.data, result.shape);
 	}
 
 	/**
 	 * OR
 	 */
 	@Override
 	public BooleanDataset iadd(final Object b) {
 		if (b instanceof AbstractDataset) {
 			AbstractDataset bds = (AbstractDataset) b;
 			checkCompatibility(bds);
 
 			IndexIterator it1 = getIterator();
 			IndexIterator it2 = bds.getIterator();
 
 			while (it1.hasNext() && it2.hasNext()) {
 				data[it1.index] |= bds.getElementBooleanAbs(it2.index);
 			}
 		} else {
 			boolean v = toBoolean(b);
 			IndexIterator it1 = getIterator();
 
 			while (it1.hasNext()) {
 				data[it1.index] |= v;
 			}
 		}
 		setDirty();
 		return this;
 	}
 
 	/**
 	 * XOR
 	 */
 	@Override
 	public BooleanDataset isubtract(final Object b) {
 		if (b instanceof AbstractDataset) {
 			AbstractDataset bds = (AbstractDataset) b;
 			checkCompatibility(bds);
 
 			IndexIterator it1 = getIterator();
 			IndexIterator it2 = bds.getIterator();
 
 			while (it1.hasNext() && it2.hasNext()) {
 				data[it1.index] ^= bds.getElementBooleanAbs(it2.index);
 			}
 		} else {
 			boolean v = toBoolean(b);
 			IndexIterator it1 = getIterator();
 
 			if (!(b instanceof Number)) {
 				logger.error("Argument is of unsupported class");
 				throw new IllegalArgumentException("Argument is of unsupported class");
 			}
 
 			while (it1.hasNext()) {
 				data[it1.index] ^= v;
 			}
 		}
 		setDirty();
 		return this;
 	}
 
 	/**
 	 * AND
 	 */
 	@Override
 	public BooleanDataset imultiply(final Object b) {
 		if (b instanceof AbstractDataset) {
 			AbstractDataset bds = (AbstractDataset) b;
 			checkCompatibility(bds);
 
 			IndexIterator it1 = getIterator();
 			IndexIterator it2 = bds.getIterator();
 
 			while (it1.hasNext() && it2.hasNext()) {
 				data[it1.index] &= bds.getElementBooleanAbs(it2.index);
 			}
 		} else {
 			boolean v = toBoolean(b);
 			IndexIterator it1 = getIterator();
 
 			if (!(b instanceof Number)) {
 				logger.error("Argument is of unsupported class");
 				throw new IllegalArgumentException("Argument is of unsupported class");
 			}
 
 			while (it1.hasNext()) {
 				data[it1.index] &= v;
 			}
 		}
 		setDirty();
 		return this;
 	}
 
 	@Override
 	public BooleanDataset idivide(final Object b) {
 		if (b instanceof AbstractDataset) {
 			AbstractDataset bds = (AbstractDataset) b;
 			checkCompatibility(bds);
 
 			IndexIterator it1 = getIterator();
 			IndexIterator it2 = bds.getIterator();
 
 			while (it1.hasNext() && it2.hasNext()) {
 				data[it1.index] &= bds.getElementBooleanAbs(it2.index);
 			}
 		} else {
 			boolean v = toBoolean(b);
 			IndexIterator it1 = getIterator();
 
 			if (!(b instanceof Number)) {
 				logger.error("Argument is of unsupported class");
 				throw new IllegalArgumentException("Argument is of unsupported class");
 			}
 
 			while (it1.hasNext()) {
 				data[it1.index] &= v;
 			}
 		}
 		setDirty();
 		return this;
 	}
 
 	@Override
 	public BooleanDataset iremainder(final Object b) {
 		logger.error("Unsupported method for class");
 		throw new UnsupportedOperationException("Unsupported method for class");
 	}
 
 	@Override
 	public BooleanDataset ipower(final Object b) {
 		logger.error("Unsupported method for class");
 		throw new UnsupportedOperationException("Unsupported method for class");
 	}
 
 	@Override
 	public double residual(final Object b) {
 		double sum = 0;
 		if (b instanceof AbstractDataset) {
 			AbstractDataset bds = (AbstractDataset) b;
 			checkCompatibility(bds);
 
 			IndexIterator it1 = getIterator();
 			IndexIterator it2 = bds.getIterator();
 
 			while (it1.hasNext() && it2.hasNext()) {
 				if (data[it1.index] ^ bds.getElementBooleanAbs(it2.index))
 					sum++;
 			}
 		} else {
 			boolean v = toBoolean(b);
 			IndexIterator it1 = getIterator();
 
 			while (it1.hasNext()) {
 				if (data[it1.index] ^ v)
 					sum++;
 			}
 		}
 		return sum;
 	}
 }
