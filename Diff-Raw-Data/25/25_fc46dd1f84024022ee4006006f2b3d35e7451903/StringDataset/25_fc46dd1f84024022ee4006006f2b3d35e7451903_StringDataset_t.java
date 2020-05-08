 /*-
  * Copyright © 2011 Diamond Light Source Ltd.
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
 
 /**
  * Extend dataset for objects
  */
 public class StringDataset extends StringDatasetBase {
 	// pin UID to base class
 	private static final long serialVersionUID = AbstractDataset.serialVersionUID;
 
 	/**
 	 * Setup the logging facilities
 	 */
 //	transient private static final Logger logger = LoggerFactory.getLogger(ObjectDataset.class);
 
 	public StringDataset() {
 		super();
 	}
 
 	/**
 	 * @param shape
 	 */
 	public StringDataset(final int... shape) {
 		super(shape);
 	}
 
 	/**
 	 * Create a dataset using given data
 	 * 
 	 * @param data
 	 * @param shape
 	 *            (can be null to create 1D dataset)
 	 */
 	public StringDataset(final String[] data, int... shape) {
 		super(data, shape);
 	}
 
 	/**
 	 * Copy a dataset
 	 * 
 	 * @param dataset
 	 */
 	public StringDataset(final StringDataset dataset) {
 		this(dataset, false);
 	}
 
 	/**
 	 * Copy a dataset or just wrap in a new reference (for Jython sub-classing)
 	 * 
 	 * @param dataset
 	 * @param wrap
 	 */
 	public StringDataset(final StringDataset dataset, final boolean wrap) {
 		super(dataset, wrap);
 	}
 
 	/**
 	 * Cast a dataset to this class type
 	 * 
 	 * @param dataset
 	 */
 	public StringDataset(final AbstractDataset dataset) {
 		super(dataset);
 	}
 
	@Override
	public StringDataset getView() {
		return new StringDataset(this, true);
	}

 	/**
 	 * Create a dataset from an object which could be a PySequence, a Java array (of arrays...)
 	 * or Number. Ragged sequences or arrays are padded with zeros.
 	 * 
 	 * @param obj
 	 * @return dataset with contents given by input
 	 */
 	public static StringDataset createFromObject(final Object obj) {
 		StringDatasetBase result = StringDatasetBase.createFromObject(obj);
 		return new StringDataset(result.data, result.shape);
 	}
 
 	/**
 	 * @param shape
 	 * @return a dataset filled with ones
 	 */
 	public static StringDataset ones(final int... shape) {
 		throw new UnsupportedOperationException("Unsupported method for class");
 	}
 
 	@Override
 	public boolean getElementBooleanAbs(int index) {
 		throw new UnsupportedOperationException("Unsupported method for class");
 	}
 
 	@Override
 	public double getElementDoubleAbs(int index) {
 		throw new UnsupportedOperationException("Unsupported method for class");
 	}
 
 	@Override
 	public long getElementLongAbs(int index) {
 		throw new UnsupportedOperationException("Unsupported method for class");
 	}
 
 	@Override
 	public double getDouble(int... pos) {
 		throw new UnsupportedOperationException("Unsupported method for class");
 	}
 
 	@Override
 	public long getLong(int... pos) {
 		throw new UnsupportedOperationException("Unsupported method for class");
 	}
 
 	@Override
 	public float getFloat(int... pos) {
 		throw new UnsupportedOperationException("Unsupported method for class");
 	}
 
 	@Override
 	public int getInt(int... pos) {
 		throw new UnsupportedOperationException("Unsupported method for class");
 	}
 
 	@Override
 	public short getShort(int... pos) {
 		throw new UnsupportedOperationException("Unsupported method for class");
 	}
 
 	@Override
 	public byte getByte(int... pos) {
 		throw new UnsupportedOperationException("Unsupported method for class");
 	}
 
 	@Override
 	public boolean getBoolean(int... pos) {
 		throw new UnsupportedOperationException("Unsupported method for class");
 	}
 
 	@Override
 	public int[] minPos() {
 		throw new UnsupportedOperationException("Unsupported method for class");
 	}
 
 	@Override
 	public int[] maxPos() {
 		throw new UnsupportedOperationException("Unsupported method for class");
 	}
 
 	@Override
 	public boolean containsInfs() {
 		return false;
 	}
 
 	@Override
 	public boolean containsNans() {
 		return false;
 	}
 
 	@Override
 	public StringDataset iadd(Object o) {
 		throw new UnsupportedOperationException("Unsupported method for class");
 	}
 
 	@Override
 	public StringDataset isubtract(Object o) {
 		throw new UnsupportedOperationException("Unsupported method for class");
 	}
 
 	@Override
 	public StringDataset imultiply(Object o) {
 		throw new UnsupportedOperationException("Unsupported method for class");
 	}
 
 	@Override
 	public StringDataset idivide(Object o) {
 		throw new UnsupportedOperationException("Unsupported method for class");
 	}
 
 	@Override
 	public StringDataset iremainder(Object o) {
 		throw new UnsupportedOperationException("Unsupported method for class");
 	}
 
 	@Override
 	public StringDataset ifloor() {
 		throw new UnsupportedOperationException("Unsupported method for class");
 	}
 
 	@Override
 	public StringDataset ipower(Object o) {
 		throw new UnsupportedOperationException("Unsupported method for class");
 	}
 
 	@Override
 	public double residual(Object o) {
 		throw new UnsupportedOperationException("Unsupported method for class");
 	}
 }
