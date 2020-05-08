 /*-
  * Copyright 2011 Diamond Light Source Ltd.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *   http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package uk.ac.diamond.scisoft.analysis.dataset;
 
 import java.io.Serializable;
 import java.lang.reflect.Array;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.math.complex.Complex;
 import org.apache.commons.math.stat.descriptive.StorelessUnivariateStatistic;
 import org.apache.commons.math.stat.descriptive.SummaryStatistics;
 import org.apache.commons.math.stat.descriptive.moment.Variance;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.diamond.scisoft.analysis.io.IMetaData;
 import uk.ac.diamond.scisoft.analysis.monitor.IMonitor;
 
 /**
  * Generic container class for data 
  * <p/>
  * Each subclass has an array of primitive types, elements of this array are grouped or
  * compounded to make items 
  * <p/>
  * Data items can be boolean, integer, float, complex float, vector float, etc
  */
 public abstract class AbstractDataset implements ADataset {
 
 	/**
 	 * Boolean
 	 */
 	public static final int BOOL = ADataset.BOOL;
 
 	/**
 	 * Signed 8-bit integer
 	 */
 	public static final int INT8 = ADataset.INT8;
 
 	/**
 	 * Signed 16-bit integer
 	 */
 	public static final int INT16 = ADataset.INT16;
 
 	/**
 	 * Signed 32-bit integer
 	 */
 	public static final int INT32 = ADataset.INT32;
 	/**
 	 * Integer (same as signed 32-bit integer)
 	 */
 	public static final int INT = ADataset.INT;
 
 	/**
 	 * Signed 64-bit integer
 	 */
 	public static final int INT64 = ADataset.INT64;
 
 	/**
 	 * 32-bit floating point
 	 */
 	public static final int FLOAT32 = ADataset.FLOAT32;
 
 	/**
 	 * 64-bit floating point
 	 */
 	public static final int FLOAT64 = ADataset.FLOAT64;
 
 	/**
 	 * Floating point (same as 64-bit floating point)
 	 */
 	public static final int FLOAT = ADataset.FLOAT;
 
 	/**
 	 * 64-bit complex floating point (real and imaginary parts are 32-bit floats)
 	 */
 	public static final int COMPLEX64 = ADataset.COMPLEX64;
 
 	/**
 	 * 128-bit complex floating point (real and imaginary parts are 64-bit floats)
 	 */
 	public static final int COMPLEX128 = ADataset.COMPLEX128;
 
 	/**
 	 * Complex floating point (same as 64-bit floating point)
 	 */
 	public static final int COMPLEX = ADataset.COMPLEX;
 
 	/**
 	 * String
 	 */
 	public static final int STRING = ADataset.STRING;
 
 	/**
 	 * Object
 	 */
 	public static final int OBJECT = ADataset.OBJECT;
 
 	/**
 	 * Array of signed 8-bit integers
 	 */
 	public static final int ARRAYINT8 = ADataset.ARRAYINT8;
 
 	/**
 	 * Array of signed 16-bit integers
 	 */
 	public static final int ARRAYINT16 = ADataset.ARRAYINT16;
 
 	/**
 	 * Array of three signed 16-bit integers for RGB values
 	 */
 	public static final int RGB = ADataset.RGB;
 
 	/**
 	 * Array of signed 32-bit integers
 	 */
 	public static final int ARRAYINT32 = ADataset.ARRAYINT32;
 
 	/**
 	 * Array of signed 64-bit integers
 	 */
 	public static final int ARRAYINT64 = ADataset.ARRAYINT64;
 
 	/**
 	 * Array of 32-bit floating points
 	 */
 	public static final int ARRAYFLOAT32 = ADataset.ARRAYFLOAT32;
 
 	/**
 	 * Array of 64-bit floating points
 	 */
 	public static final int ARRAYFLOAT64 = ADataset.ARRAYFLOAT64;
 
 	/**
 	 * Update this when there are any serious changes to API
 	 */
 	protected static final long serialVersionUID = -6891075135217265625L;
 
 	/**
 	 * Setup the logging facilities
 	 */
 	protected static final Logger abstractLogger = LoggerFactory.getLogger(AbstractDataset.class);
 
 	private static boolean isDTypeElemental(int dtype) {
 		return (dtype <= COMPLEX128 || dtype == RGB);
 	}
 
 	/**
 	 * Limit to strings output via the toString() method
 	 */
 	private static final int MAX_STRING_LENGTH = 120;
 
 	/**
 	 * Limit to number of sub-blocks output via the toString() method
 	 */
 	private static final int MAX_SUBBLOCKS = 6;
 
 	/**
 	 * The shape or dimensions of the dataset
 	 */
 	protected int[] shape;
 	protected int size; // number of items
 
 	/**
 	 * The data itself, held in a 1D array, but the object will wrap it to appear as possessing as many dimensions as
 	 * wanted
 	 */
 	protected Serializable odata = null;
 
 	/**
 	 * Set aliased data as base data
 	 */
 	abstract protected void setData();
 
 	protected String name = "";
 
 	/**
 	 * These members hold cached values. If their values are null, then recalculate, otherwise just use the values
 	 */
 	transient protected HashMap<String, Object> storedValues = null;
 
 	/**
 	 * Constructor required for serialisation.
 	 */
 	public AbstractDataset() {
 	}
 
 	/**
 	 * This is a <b>synchronized</b> version of the clone method
 	 * 
 	 * @return a copy of dataset
 	 */
 	@Override
 	public synchronized AbstractDataset synchronizedCopy() {
 		return clone();
 	}
 
 	/**
 	 * @return true if dataset has same shape and data values
 	 */
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj) {
 			return true;
 		}
 		if (obj == null) {
 			return false;
 		}
 		if (!getClass().equals(obj.getClass())) {
 			if (getRank() == 0) // for zero-rank datasets
 				return obj.equals(getObject());
 			return false;
 		}
 
 		AbstractDataset other = (AbstractDataset) obj;
 		if (getElementsPerItem() != other.getElementsPerItem())
 			return false;
 		if (!Arrays.equals(shape, other.shape)) {
 			return false;
 		}
 		return true;
 	}
 
 	@Override
 	public int hashCode() {
 		return getHash();
 	}
 
 	@Override
 	public AbstractDataset clone() {
 		AbstractDataset c = null;
 		try {
 			// copy across the data
 			switch (getDtype()) {
 			case BOOL:
 				c = new BooleanDataset((BooleanDataset) this);
 				break;
 			case INT8:
 				c = new ByteDataset((ByteDataset) this);
 				break;
 			case INT16:
 				c = new ShortDataset((ShortDataset) this);
 				break;
 			case INT32:
 				c = new IntegerDataset((IntegerDataset) this);
 				break;
 			case INT64:
 				c = new LongDataset((LongDataset) this);
 				break;
 			case ARRAYINT8:
 				c = new CompoundByteDataset((CompoundByteDataset) this);
 				break;
 			case ARRAYINT16:
 				c = new CompoundShortDataset((CompoundShortDataset) this);
 				break;
 			case ARRAYINT32:
 				c = new CompoundIntegerDataset((CompoundIntegerDataset) this);
 				break;
 			case ARRAYINT64:
 				c = new CompoundLongDataset((CompoundLongDataset) this);
 				break;
 			case FLOAT32:
 				c = new FloatDataset((FloatDataset) this);
 				break;
 			case FLOAT64:
 				c = new DoubleDataset((DoubleDataset) this);
 				break;
 			case ARRAYFLOAT32:
 				c = new CompoundFloatDataset((CompoundFloatDataset) this);
 				break;
 			case ARRAYFLOAT64:
 				c = new CompoundDoubleDataset((CompoundDoubleDataset) this);
 				break;
 			case COMPLEX64:
 				c = new ComplexFloatDataset((ComplexFloatDataset) this);
 				break;
 			case COMPLEX128:
 				c = new ComplexDoubleDataset((ComplexDoubleDataset) this);
 				break;
 			default:
 				abstractLogger.error("Dataset of unknown type!");
 				break;
 			}
 		} catch (OutOfMemoryError e) {
 			throw new OutOfMemoryError("Not enough memory available to create dataset");
 		}
 
 		return c;
 	}
 
 	/**
 	 * Cast a dataset
 	 * 
 	 * @param dtype
 	 *            dataset type
 	 * @return a converted dataset
 	 */
 	@Override
 	public AbstractDataset cast(final int dtype) {
 		if (getDtype() == dtype) {
 			return this;
 		}
 		return DatasetUtils.cast(this, dtype);
 	}
 
 	/**
 	 * Cast a dataset
 	 * 
 	 * @param repeat
 	 * @param dtype
 	 *            dataset type
 	 * @param isize
 	 *            item size
 	 * @return a converted dataset
 	 */
 	@Override
 	public AbstractDataset cast(final boolean repeat, final int dtype, final int isize) {
 		if (getDtype() == dtype && getElementsPerItem() == isize) {
 			return this;
 		}
 		return DatasetUtils.cast(this, repeat, dtype, isize);
 	}
 
 	/**
 	 * @return whole view of dataset (i.e. data buffer is shared)
 	 */
 	@Override
 	public abstract AbstractDataset getView();
 
 	/**
 	 * Copy fields from original to view
 	 * @param orig
 	 * @param view
 	 * @param clone if true, then clone everything but bulk data
 	 * @param cloneMetadata if true, clone metadata
 	 */
 	protected static void copyToView(AbstractDataset orig, AbstractDataset view, boolean clone, boolean cloneMetadata) {
 		view.name = orig.name;
 		view.size = orig.size;
 		view.odata = orig.odata;
 
 		Serializable error = orig.errorData;
 		if (error != null && error instanceof ADataset)
 			view.errorData = ((ADataset) error).getView();
 		else
 			view.errorData = error;
 
 		if (clone) {
 			view.shape = orig.shape.clone();
 			copyStoredValues(orig, view, false);
 		} else {
 			view.shape = orig.shape;
 		}
 
 		IMetaData metadata = orig.metadataStructure;
 		if (cloneMetadata) {
 			if (metadata != null)
 				view.metadataStructure = metadata.clone();
 		} else {
 			view.metadataStructure = metadata;
 		}
 		if (orig.getDtype() != view.getDtype() && view.storedValues != null) {
 			view.storedValues.remove(STORE_SHAPELESS_HASH);
 			view.storedValues.remove(STORE_HASH);
 		}
 	}
 
 	/**
 	 * Generate an index dataset for current dataset
 	 * 
 	 * @return an index dataset
 	 */
 	@Override
 	public IntegerDataset getIndices() {
 		final IntegerDataset ret = DatasetUtils.indices(shape);
 		if (getName() != null) {
 			ret.setName("Indices of " + getName());
 		}
 		return ret;
 	}
 
 	/**
 	 * Permute copy of dataset's axes so that given order is old order:
 	 * 
 	 * <pre>
 	 *  axisPerm = (p(0), p(1),...) => newdata(n(0), n(1),...) = olddata(o(0), o(1), ...)
 	 *  such that n(i) = o(p(i)) for all i
 	 * </pre>
 	 * 
 	 * I.e. for a 3D dataset (1,0,2) implies the new dataset has its 1st dimension running along the old dataset's 2nd
 	 * dimension and the new 2nd is the old 1st. The 3rd dimension is left unchanged.
 	 * 
 	 * @param axes
 	 *            if zero length then axes order reversed
 	 * @return remapped copy of data
 	 */
 	@Override
 	public AbstractDataset transpose(int... axes) {
 		return DatasetUtils.transpose(this, axes);
 	}
 
 	/**
 	 * Swap two axes in dataset
 	 * 
 	 * @param axis1
 	 * @param axis2
 	 * @return swapped dataset
 	 */
 	@Override
 	public AbstractDataset swapaxes(int axis1, int axis2) {
 		return DatasetUtils.swapAxes(this, axis1, axis2);
 	}
 
 	/**
 	 * Flatten shape
 	 * 
 	 * @return a flattened dataset which is a view if dataset is contiguous otherwise is a copy
 	 */
 	@Override
 	public AbstractDataset flatten() {
 		AbstractDataset result = getView();
 		result.shape = new int[] { result.size };
 		return result;
 	}
 
 	/**
 	 * Calculate total number of items in given shape
 	 * @param shape
 	 * @return size
 	 */
 	public static int calcSize(final int[] shape) {
 		int size = 1;
 		double dsize = 1.0;
 
 		if (shape.length == 1) {
 			if (shape[0] == 0) {
 				return 0;
 			}
 
 			size *= shape[0];
 			dsize *= shape[0];
 		} else {
 			for (int i = 0; i < shape.length; i++) {
 				// make sure the indexes isn't zero or negative
 				if (shape[i] == 0) {
 					return 0;
 				} else if (shape[i] < 0) {
 					throw new IllegalArgumentException("The " + i + "-th is " + shape[i]
 							+ " which is an illegal argument as it is negative");
 				}
 
 				size *= shape[i];
 				dsize *= shape[i];
 			}
 		}
 
 		// check to see if the size is larger than an integer, i.e. we can't allocate it
 		if (dsize > Integer.MAX_VALUE) {
 			throw new IllegalArgumentException("Size of the dataset is too large to allocate");
 		}
 		return size;
 	}
 
 	/**
 	 * Find dataset type that best fits given types The best type takes into account complex and array datasets
 	 * 
 	 * @param atype
 	 *            first dataset type
 	 * @param btype
 	 *            second dataset type
 	 * @return best dataset type
 	 */
 	public static int getBestDType(final int atype, final int btype) {
 		int besttype;
 
 		int a = atype >= ARRAYINT8 ? atype / 100 : atype;
 		int b = btype >= ARRAYINT8 ? btype / 100 : btype;
 
 		besttype = a > b ? a : b;
 
 		if (atype >= ARRAYINT8 || btype >= ARRAYINT8) {
 			if (besttype >= COMPLEX64) {
 				throw new IllegalArgumentException("Complex type cannot be promoted to compound type");
 			}
 			besttype *= 100;
 		}
 
 		return besttype;
 	}
 
 	/**
 	 * The largest dataset type suitable for a summation of around a few thousand items without changing from the "kind"
 	 * of dataset
 	 * 
 	 * @param otype
 	 * @return largest dataset type available for given dataset type
 	 */
 	public static int getLargestDType(final int otype) {
 		switch (otype) {
 		case BOOL:
 		case INT8:
 		case INT16:
 			return INT32;
 		case INT32:
 		case INT64:
 			return INT64;
 		case FLOAT32:
 		case FLOAT64:
 			return FLOAT64;
 		case COMPLEX64:
 		case COMPLEX128:
 			return COMPLEX128;
 		case ARRAYINT8:
 		case ARRAYINT16:
 			return ARRAYINT32;
 		case ARRAYINT32:
 		case ARRAYINT64:
 			return ARRAYINT64;
 		case ARRAYFLOAT32:
 		case ARRAYFLOAT64:
 			return ARRAYFLOAT64;
 		}
 		throw new IllegalArgumentException("Unsupported dataset type");
 	}
 
 	/**
 	 * Find floating point dataset type that best fits given types The best type takes into account complex and array
 	 * datasets
 	 * 
 	 * @param otype
 	 *            old dataset type
 	 * @return best dataset type
 	 */
 	public static int getBestFloatDType(final int otype) {
 		int btype;
 		switch (otype) {
 		case BOOL:
 		case INT8:
 		case INT16:
 		case ARRAYINT8:
 		case ARRAYINT16:
 		case FLOAT32:
 		case ARRAYFLOAT32:
 		case COMPLEX64:
 			btype = FLOAT32; // demote, if necessary
 			break;
 		case INT32:
 		case INT64:
 		case ARRAYINT32:
 		case ARRAYINT64:
 		case FLOAT64:
 		case ARRAYFLOAT64:
 		case COMPLEX128:
 			btype = FLOAT64; // promote, if necessary
 			break;
 		default:
 			btype = otype; // for array datasets, preserve type
 			break;
 		}
 
 		return btype;
 	}
 
 	/**
 	 * Find floating point dataset type that best fits given class The best type takes into account complex and array
 	 * datasets
 	 * 
 	 * @param cls
 	 *            of an item or element
 	 * @return best dataset type
 	 */
 	public static int getBestFloatDType(Class<? extends Object> cls) {
 		return getBestFloatDType(getDTypeFromClass(cls));
 	}
 
 	transient private static final Map<Class<?>, Integer> dtypeMap = createDTypeMap();
 
 	private static Map<Class<?>, Integer> createDTypeMap() {
 		Map<Class<?>, Integer> result = new HashMap<Class<?>, Integer>();
 		result.put(Boolean.class, BOOL);
 		result.put(Byte.class, INT8);
 		result.put(Short.class, INT16);
 		result.put(Integer.class, INT32);
 		result.put(Long.class, INT64);
 		result.put(Float.class, FLOAT32);
 		result.put(Double.class, FLOAT64);
 		result.put(Complex.class, COMPLEX128);
 		result.put(String.class, STRING);
 		result.put(Object.class, OBJECT);
 		return result;
 	}
 
 	/**
 	 * Get dataset type from a class
 	 * 
 	 * @param cls
 	 * @return dataset type
 	 */
 	public static int getDTypeFromClass(Class<? extends Object> cls) {
 		Integer dtype = dtypeMap.get(cls);
 		if (dtype == null) {
 			throw new IllegalArgumentException("Class of object not supported");
 		}
 		return dtype;
 	}
 
 	/**
 	 * Get dataset type from an object. The following are supported: Java Number objects, Apache common math Complex
 	 * objects, Java arrays and lists
 	 * 
 	 * @param obj
 	 * @return dataset type
 	 */
 	public static int getDTypeFromObject(Object obj) {
 		int dtype = BOOL;
 	
 		if (obj == null) {
 			return dtype;
 		}
 
 		if (obj instanceof List<?>) {
 			List<?> jl = (List<?>) obj;
 			int l = jl.size();
 			for (int i = 0; i < l; i++) {
 				int ldtype = getDTypeFromObject(jl.get(i));
 				if (ldtype > dtype) {
 					dtype = ldtype;
 				}
 			}
 		} else if (obj.getClass().isArray()) {
 			int l = Array.getLength(obj);
 			for (int i = 0; i < l; i++) {
 				Object lo = Array.get(obj, i);
 				int ldtype = getDTypeFromObject(lo);
 				if (ldtype > dtype) {
 					dtype = ldtype;
 				}
 			}
 		} else {
 			dtype = getDTypeFromClass(obj.getClass());
 		}
 		return dtype;
 	}
 
 	/**
 	 * Get dataset type from given dataset
 	 * @param d
 	 * @return dataset type
 	 */
 	public static int getDType(ILazyDataset d) {
 		if (d instanceof ADataset)
 			return ((ADataset) d).getDtype();
 		return getDTypeFromClass(d.elementClass());
 	}
 
 	/**
 	 * get shape from object (array or list supported)
 	 * @param obj
 	 * @return shape
 	 */
 	protected static int[] getShapeFromObject(final Object obj) {
 		ArrayList<Integer> lshape = new ArrayList<Integer>();
 
 		getShapeFromObj(lshape, obj, 0);
 		if (obj != null && lshape.size() == 0) {
 			return new int[0]; // cope with a single item
 		}
 		final int rank = lshape.size();
 		final int[] shape = new int[rank];
 		for (int i = 0; i < rank; i++) {
 			shape[i] = lshape.get(i);
 		}
 
 		return shape;
 	}
 
 	private static void getShapeFromObj(final ArrayList<Integer> ldims, Object obj, int depth) {
 		if (obj == null)
 			return;
 
 		if (obj instanceof List<?>) {
 			List<?> jl = (List<?>) obj;
 			int l = jl.size();
 			updateShape(ldims, depth, l);
 			for (int i = 0; i < l; i++) {
 				Object lo = jl.get(i);
 				getShapeFromObj(ldims, lo, depth + 1);
 			}
 		} else if (obj.getClass().isArray()) {
 			final int l = Array.getLength(obj);
 			updateShape(ldims, depth, l);
 			for (int i = 0; i < l; i++) {
 				Object lo = Array.get(obj, i);
 				getShapeFromObj(ldims, lo, depth + 1);
 			}
 		} else {
 			return; // not an array of any type
 		}
 	}
 
 	private static void updateShape(final ArrayList<Integer> ldims, final int depth, final int l) {
 		if (depth >= ldims.size()) {
 			ldims.add(l);
 		} else if (l > ldims.get(depth)) {
 			ldims.set(depth, l);
 		}
 	}
 
 	/**
 	 * Fill dataset from object at depth dimension
 	 * @param obj
 	 * @param depth
 	 * @param pos position
 	 */
 	@Override
 	public void fillData(Object obj, final int depth, final int[] pos) {
 		if (obj == null) {
 			int dtype = getDtype();
 			if (dtype == FLOAT32)
 				set(Float.NaN, pos);
 			else if (dtype == FLOAT64)
 				set(Double.NaN, pos);
 			return;
 		}
 
 		if (obj instanceof List<?>) {
 			List<?> jl = (List<?>) obj;
 			int l = jl.size();
 			for (int i = 0; i < l; i++) {
 				Object lo = jl.get(i);
 				fillData(lo, depth + 1, pos);
 				pos[depth]++;
 			}
 			pos[depth] = 0;
 		} else if (obj.getClass().isArray()) {
 			int l = Array.getLength(obj);
 			for (int i = 0; i < l; i++) {
 				Object lo = Array.get(obj, i);
 				fillData(lo, depth + 1, pos);
 				pos[depth]++;
 			}
 			pos[depth] = 0;
 		} else {
 			set(obj, pos);
 		}
 	}
 
 	protected static boolean toBoolean(final Object b) {
 		if (b instanceof Number) {
 			return ((Number) b).longValue() != 0;
 		} else if (b instanceof Boolean) {
 			return ((Boolean) b).booleanValue();
 		} else if (b instanceof Complex) {
 			return ((Complex) b).getReal() != 0;
 		} else {
 			throw new IllegalArgumentException("Argument is of unsupported class");
 		}
 	}
 
 	protected static long toLong(final Object b) {
 		if (b instanceof Number) {
 			double t = ((Number) b).doubleValue();
 			if (Double.isNaN(t) || Double.isInfinite(t)) {
 				return 0;
 			}
 			return ((Number) b).longValue();
 		} else if (b instanceof Boolean) {
 			return ((Boolean) b).booleanValue() ? 1 : 0;
 		} else if (b instanceof Complex) {
 			return (long) ((Complex) b).getReal();
 		} else {
 			throw new IllegalArgumentException("Argument is of unsupported class");
 		}
 	}
 
 	protected static double toReal(final Object b) {
 		if (b instanceof Number) {
 			return ((Number) b).doubleValue();
 		} else if (b instanceof Boolean) {
 			return ((Boolean) b).booleanValue() ? 1 : 0;
 		} else if (b instanceof Complex) {
 			return ((Complex) b).getReal();
 		} else {
 			throw new IllegalArgumentException("Argument is of unsupported class");
 		}
 	}
 
 	protected static double toImag(final Object b) {
 		if (b instanceof Number) {
 			return 0;
 		} else if (b instanceof Boolean) {
 			return 0;
 		} else if (b instanceof Complex) {
 			return ((Complex) b).getImaginary();
 		} else {
 			throw new IllegalArgumentException("Argument is not a number");
 		}
 	}
 
 	/**
 	 * @param withPosition
 	 *            set true if position is needed
 	 * @return an IndexIterator tailored for this dataset
 	 */
 	@Override
 	public IndexIterator getIterator(final boolean withPosition) {
 		return (withPosition) ? new ContiguousIteratorWithPosition(shape, size) : new ContiguousIterator(size);
 	}
 
 	/**
 	 * @return an IndexIterator tailored for this dataset
 	 */
 	@Override
 	public IndexIterator getIterator() {
 		return getIterator(false);
 	}
 
 	/**
 	 * @param axes
 	 * @return a PositionIterator that misses out axes
 	 */
 	@Override
 	public PositionIterator getPositionIterator(final int... axes) {
 		return new PositionIterator(shape, axes);
 	}
 
 	/**
 	 * @param start
 	 *            specifies the starting indexes
 	 * @param stop
 	 *            specifies the stopping indexes (nb, these are <b>not</b> included in the slice)
 	 * @param step
 	 *            specifies the steps in the slice
 	 * @return an slice iterator that operates like an IndexIterator
 	 */
 	@Override
 	public IndexIterator getSliceIterator(final int[] start, final int[] stop, final int[] step) {
 		int rank = shape.length;
 
 		int[] lstart, lstop, lstep;
 
 		if (step == null) {
 			lstep = new int[rank];
 			Arrays.fill(lstep, 1);
 		} else {
 			lstep = step;
 		}
 
 		if (start == null) {
 			lstart = new int[rank];
 		} else {
 			lstart = start;
 		}
 
 		if (stop == null) {
 			lstop = new int[rank];
 		} else {
 			lstop = stop;
 		}
 
 		int[] newShape;
 		if (rank > 1 || (rank > 0 && shape[0] > 0)) {
 			newShape = checkSlice(start, stop, lstart, lstop, lstep);
 		} else {
 			newShape = new int[rank];
 		}
 
 		return new SliceIterator(shape, size, lstart, lstep, newShape);
 	}
 
 	/**
 	 * Get a slice iterator that is defined by a starting position and a set of axes to include
 	 * 
 	 * @param pos
 	 * @param axes
 	 *            to include
 	 * @return slice iterator
 	 */
 	@Override
 	public SliceIterator getSliceIteratorFromAxes(final int[] pos, boolean[] axes) {
 		int rank = shape.length;
 		int[] start;
 		int[] stop = new int[rank];
 		int[] step = new int[rank];
 
 		if (pos == null) {
 			start = new int[rank];
 		} else if (pos.length == rank) {
 			start = pos.clone();
 		} else {
 			throw new IllegalArgumentException("pos array length is not equal to rank of dataset");
 		}
 		if (axes == null) {
 			axes = new boolean[rank];
 			Arrays.fill(axes, true);
 		} else if (axes.length != rank) {
 			throw new IllegalArgumentException("axes array length is not equal to rank of dataset");
 		}
 
 		for (int i = 0; i < rank; i++) {
 			if (axes[i]) {
 				stop[i] = shape[i];
 			} else {
 				stop[i] = start[i] + 1;
 			}
 			step[i] = 1;
 		}
 		return (SliceIterator) getSliceIterator(start, stop, step);
 	}
 
 	/**
 	 * Check slice and alter parameters if necessary
 	 * 
 	 * @param oldShape
 	 * @param start
 	 *            can be null
 	 * @param stop
 	 *            can be null
 	 * @param lstart
 	 *            can be a reference to start if that is not null
 	 * @param lstop
 	 *            can be a reference to stop if that is not null
 	 * @param lstep
 	 * @returns newShape
 	 */
 	public static int[] checkSlice(final int[] oldShape, final int[] start, final int[] stop, final int[] lstart,
 			final int[] lstop, final int[] lstep) {
 		// number of steps, or new shape, taken in each dimension is
 		// shape = (stop - start + step - 1) / step if step > 0
 		// (stop - start + step + 1) / step if step < 0
 		//
 		// thus the final index in each dimension is
 		// start + (shape-1)*step
 
 		int rank = oldShape.length;
 
 		if (lstart.length != rank || lstop.length != rank || lstep.length != rank) {
 			throw new IllegalArgumentException("No of indexes does not match data dimensions: you passed it start="
 					+ lstart.length + ", stop=" + lstop.length + ", step=" + lstep.length + ", and it needs " + rank);
 		}
 
 		final int[] newShape = new int[rank];
 
 		// sanitise input
 		for (int i = 0; i < rank; i++) {
 			if (lstep[i] == 0) {
 				throw new IllegalArgumentException("The step array is not allowed any zero entries: " + i
 						+ "-th entry is zero");
 			}
 			if (start != null) {
 				if (start[i] < 0) {
 					start[i] += oldShape[i];
 				}
 				if (start[i] < 0) {
 					start[i] = lstep[i] > 0 ? 0 : -1;
 				}
 				if (start[i] > oldShape[i]) {
 					start[i] = lstep[i] > 0 ? oldShape[i] : oldShape[i] - 1;
 				}
 			} else {
 				lstart[i] = lstep[i] > 0 ? 0 : oldShape[i] - 1;
 			}
 
 			if (stop != null) {
 				if (stop[i] < 0) {
 					if (lstep[i] > 0)
 						stop[i] += oldShape[i];
 				}
 				if (stop[i] < 0) {
 					stop[i] = -1;
 				}
 				if (stop[i] > oldShape[i]) {
 					stop[i] = oldShape[i];
 				}
 			} else {
 				lstop[i] = lstep[i] > 0 ? oldShape[i] : -1;
 			}
 			if (lstart[i] == lstop[i]) {
 				throw new IllegalArgumentException("Same indices in start and stop");
 			}
 			if ((lstep[i] > 0) != (lstart[i] < lstop[i])) {
 				throw new IllegalArgumentException("Start=" + lstart[i] + " and stop=" + lstop[i]
 						+ " indices are incompatible with step=" + lstep[i]);
 			}
 			if (lstep[i] > 0) {
 				newShape[i] = (lstop[i] - lstart[i] - 1) / lstep[i] + 1;
 			} else {
 				newShape[i] = (lstop[i] - lstart[i] + 1) / lstep[i] + 1;
 			}
 
 		}
 		return newShape;
 	}
 
 	protected int[] checkSlice(int[] start, int[] stop, int[] lstart, int[] lstop, int[] lstep) {
 		return checkSlice(shape, start, stop, lstart, lstop, lstep);
 	}
 
 	/**
 	 * This is modelled after the NumPy array slice
 	 * @param obj
 	 *            specifies the object used to set the specified slice
 	 * @param start
 	 *            specifies the starting indexes
 	 * @param stop
 	 *            specifies the stopping indexes (nb, these are <b>not</b> included in the slice)
 	 * @param step
 	 *            specifies the steps in the slice
 	 * 
 	 * @return The dataset with the sliced set to object
 	 */
 	@Override
 	public AbstractDataset setSlice(final Object obj, final int[] start, final int[] stop, final int[] step) {
 		return setSlice(obj, getSliceIterator(start, stop, step));
 	}
 
 	/**
 	 * @param obj
 	 *            specifies the object used to set the specified slice
 	 * @param iterator
 	 *            specifies the slice iterator
 	 * 
 	 * @return The dataset with the sliced set to object
 	 */
 	@Override
 	abstract public AbstractDataset setSlice(final Object obj, final IndexIterator iterator);
 
 	/**
 	 * Get an iterator that visits every item in this dataset where the corresponding item in choice dataset is true
 	 * 
 	 * @param choice
 	 * @return an iterator of dataset that visits items chosen by given choice dataset
 	 */
 	@Override
 	public BooleanIterator getBooleanIterator(ADataset choice) {
 		return getBooleanIterator(choice, true);
 	}
 
 	/**
 	 * Get an iterator that visits every item in this dataset where the corresponding item in choice dataset
 	 * is given by value
 	 * 
 	 * @param choice
 	 * @param value
 	 * @return an iterator of dataset that visits items chosen by given choice dataset
 	 */
 	@Override
 	public BooleanIterator getBooleanIterator(ADataset choice, boolean value) {
 		return new BooleanIterator(getIterator(), choice, value);
 	}
 
 	/**
 	 * This is modelled after the NumPy get item with a condition specified by a boolean dataset
 	 * 
 	 * @param selection
 	 *            a boolean dataset of same shape to use for selecting items
 	 * @return The new selected dataset
 	 */
 	@Override
 	public AbstractDataset getByBoolean(ADataset selection) {
 		checkCompatibility(selection);
 
 		final int length = ((Number) selection.sum()).intValue();
 		final int is = getElementsPerItem();
 		AbstractDataset r = zeros(is, new int[] { length }, getDtype());
 		BooleanIterator biter = getBooleanIterator(selection);
 
 		int i = 0;
 		while (biter.hasNext()) {
 			r.setObjectAbs(i, getObjectAbs(biter.index));
 			i += is;
 		}
 		return r;
 	}
 
 	/**
 	 * This is modelled after the NumPy set item with a condition specified by a boolean dataset
 	 * @param obj
 	 *            specifies the object used to set the selected items
 	 * @param selection
 	 *            a boolean dataset of same shape to use for selecting items
 	 * 
 	 * @return The dataset with modified content
 	 */
 	@Override
 	abstract public AbstractDataset setByBoolean(final Object obj, ADataset selection);
 
 	/**
 	 * This is modelled after the NumPy get item with an index dataset
 	 * 
 	 * @param index
 	 *            an integer dataset
 	 * @return The new selected dataset by indices
 	 */
 	@Override
 	public AbstractDataset getByIndex(IntegerDataset index) {
 		final int is = getElementsPerItem();
 		final AbstractDataset r = zeros(is, index.getShape(), getDtype());
 		final IntegerIterator iter = new IntegerIterator(index, size, is);
 
 		int i = 0;
 		while (iter.hasNext()) {
 			r.setObjectAbs(i, getObjectAbs(iter.index));
 			i += is;
 		}
 		return r;
 	}
 
 	/**
 	 * This is modelled after the NumPy get item with an array of indexing objects
 	 * @param index
 	 *            an array of integer dataset, boolean dataset, slices or null entries (same as full slices)
 	 * @return The new selected dataset by index
 	 */
 	@Override
 	public AbstractDataset getByIndexes(final Object... index) {
 		final IntegersIterator iter = new IntegersIterator(shape, index);
 		final int is = getElementsPerItem();
 		final AbstractDataset r = zeros(is, iter.getShape(), getDtype());
 
 		final int[] pos = iter.getPos();
 		int i = 0;
 		while (iter.hasNext()) {
 			r.setObjectAbs(i, getObject(pos));
 			i += is;
 		}
 		return r;
 	}
 
 	/**
 	 * This is modelled after the NumPy set item with an index dataset
 	 * @param obj
 	 *            specifies the object used to set the selected items
 	 * @param index
 	 *            an integer dataset
 	 * 
 	 * @return The dataset with modified content
 	 */
 	@Override
 	abstract public AbstractDataset setByIndex(final Object obj, final ADataset index);
 
 	/**
 	 * This is modelled after the NumPy set item with an array of indexing objects
 	 * @param obj
 	 *            specifies the object used to set the selected items
 	 * @param index
 	 *            an array of integer dataset, boolean dataset, slices or null entries (same as full slices)
 	 * 
 	 * @return The dataset with modified content
 	 */
 	@Override
 	abstract public AbstractDataset setByIndexes(final Object obj, final Object... index);
 
 	/**
 	 * @param dtype
 	 * @return class of constituent element
 	 */
 	public static Class<?> elementClass(final int dtype) {
 		switch (dtype) {
 		case BOOL:
 			return Boolean.class;
 		case INT8:
 		case ARRAYINT8:
 			return Byte.class;
 		case INT16:
 		case ARRAYINT16:
 		case RGB:
 			return Short.class;
 		case INT32:
 		case ARRAYINT32:
 			return Integer.class;
 		case INT64:
 		case ARRAYINT64:
 			return Long.class;
 		case FLOAT32:
 		case ARRAYFLOAT32:
 			return Float.class;
 		case FLOAT64:
 		case ARRAYFLOAT64:
 			return Double.class;
 		case COMPLEX64:
 			return Float.class;
 		case COMPLEX128:
 			return Double.class;
 		}
 		return Object.class;
 	}
 
 	@Override
 	public Class<?> elementClass() {
 		return elementClass(getDtype());
 	}
 
 	/**
 	 * @return true if dataset has elements which are floating point values
 	 */
 	@Override
 	public boolean hasFloatingPointElements() {
 		Class<?> cls = elementClass();
 		return cls == Float.class || cls == Double.class;
 	}
 
 	@Override
 	public int getElementsPerItem() {
 		return getElementsPerItem(getDtype());
 	}
 
 	@Override
 	public int getItemsize() {
 		return getItemsize(getDtype(), getElementsPerItem());
 	}
 
 	/**
 	 * @param dtype
 	 * @return number of elements per item
 	 */
 	public static int getElementsPerItem(final int dtype) {
 		switch (dtype) {
 		case ARRAYINT8:
 		case ARRAYINT16:
 		case ARRAYINT32:
 		case ARRAYINT64:
 		case ARRAYFLOAT32:
 		case ARRAYFLOAT64:
 			throw new UnsupportedOperationException("Multi-element type unsupported");
 		case COMPLEX64:
 		case COMPLEX128:
 			return 2;
 		}
 		return 1;
 	}
 
 	/**
 	 * @param dtype
 	 * @return length of single item in bytes
 	 */
 	public static int getItemsize(final int dtype) {
 		return getItemsize(dtype, getElementsPerItem(dtype));
 	}
 
 	/**
 	 * @param dtype
 	 * @param isize
 	 *            number of elements in an item
 	 * @return length of single item in bytes
 	 */
 	public static int getItemsize(final int dtype, final int isize) {
 		int size;
 
 		switch (dtype) {
 		case BOOL:
 			size = 1; // How is this defined?
 			break;
 		case INT8:
 		case ARRAYINT8:
 			size = Byte.SIZE / 8;
 			break;
 		case INT16:
 		case ARRAYINT16:
 		case RGB:
 			size = Short.SIZE / 8;
 			break;
 		case INT32:
 		case ARRAYINT32:
 			size = Integer.SIZE / 8;
 			break;
 		case INT64:
 		case ARRAYINT64:
 			size = Long.SIZE / 8;
 			break;
 		case FLOAT32:
 		case ARRAYFLOAT32:
 		case COMPLEX64:
 			size = Float.SIZE / 8;
 			break;
 		case FLOAT64:
 		case ARRAYFLOAT64:
 		case COMPLEX128:
 			size = Double.SIZE / 8;
 			break;
 		default:
 			size = 0;
 			break;
 		}
 
 		return size * isize;
 	}
 
 	/**
 	 * @return name
 	 */
 	@Override
 	public String getName() {
 		return name;
 	}
 
 	/**
 	 * @param name
 	 */
 	@Override
 	public void setName(final String name) {
 		this.name = name;
 	}
 
 	/**
 	 * @return number of data items in dataset
 	 */
 	@Override
 	public int getSize() {
 		if (odata == null) {
 			throw new NullPointerException("The data object inside the dataset has not been allocated, "
 					+ "this suggests a failed or absent construction of the dataset");
 		}
 		return size;
 	}
 
 	@Override
 	public int[] getShape() {
 		// make a copy of the dimensions data, and put that out
 		if (shape == null) {
 			abstractLogger.warn("Shape is null!!!");
 			return new int[] {};
 		}
 		return shape.clone();
 	}
 
 	@Override
 	public int getRank() {
 		return shape.length;
 	}
 
 	/**
 	 * @return number of bytes used (does not include reserved space)
 	 */
 	@Override
 	public int getNbytes() {
 		return getSize() * getItemsize();
 	}
 
 	/**
 	 * @param shape
 	 */
 	@Override
 	public void setShape(final int... shape) {
 		int size = calcSize(shape);
 		if (size != this.size) {
 			throw new IllegalArgumentException("New shape (" + Arrays.toString(shape)
 					+ ") is not compatible with old shape (" + Arrays.toString(this.shape) + ")");
 		}
 
 		this.shape = shape.clone();
 		if (errorData != null && errorData instanceof ADataset) {
 			((ADataset) errorData).setShape(shape);
 		}
 
 		if (storedValues != null)
 			filterStoredValues(storedValues); // as it is dependent on shape
 	}
 
 	@Override
 	public int[] getShapeRef() {
 		return shape;
 	}
 
 	/**
 	 * @return the buffer that backs the dataset
 	 */
 	@Override
 	public Serializable getBuffer() {
 		return odata;
 	}
 
 	/**
 	 * @param slice
 	 * @return a view of a slice
 	 */
 	@Override
 	public AbstractDataset getSliceView(Slice... slice) {
 		final int rank = shape.length;
 		final int[] start = new int[rank];
 		final int[] stop = new int[rank];
 		final int[] step = new int[rank];
 
 		Slice.convertFromSlice(slice, shape, start, stop, step);
 		return getSliceView(start, stop, step);
 	}
 
 	/**
 	 * @param start
 	 * @param stop
 	 * @param step
 	 * @return a view of a slice
 	 */
 	@Override
 	public AbstractDataset getSliceView(final int[] start, final int[] stop, final int[] step) {
 		return null; // TODO
 	}
 
 	/**
 	 * Function that uses the knowledge of the dataset to calculate the index in the data array that corresponds to the
 	 * n-dimensional position given by the int array. The input values <b>must</b> be inside the arrays, this should be
 	 * ok as this function is mainly in code which will be run inside the get and set functions
 	 * 
 	 * @param n
 	 *            the integer array specifying the n-D position
 	 * @return the index on the data array corresponding to that location
 	 */
 	protected int get1DIndex(final int... n) {
 		final int imax = n.length;
 		final int rank = shape.length;
 		if (imax == 0) {
 			if (rank == 0)
 				return 0;
 			if (rank == 1 && shape[0] <= 1) {
 				return 0;
 			}
 			throw new IllegalArgumentException("One or more index parameters must be supplied");
 		} else if (imax > rank) {
 			throw new IllegalArgumentException("No of index parameters is different to the shape of data: " + imax
 					+ " given " + rank + " required");
 		}
 
 		int index = 0;
 		int i = 0;
 		for (; i < imax; i++) {
 			final int ni = n[i];
 			final int si = shape[i];
 			if (ni < -si || ni >= si) {
 				throw new ArrayIndexOutOfBoundsException("Index (" + ni + ") out of range [-" + si + "," + si
 						+ ") in dimension " + i);
 			}
 			index = index * si + ni;
 			if (ni < 0) {
 				index += si;
 			}
 		}
 		for (; i < rank; i++) {
 			index *= shape[i];
 		}
 
 		return index;
 	}
 
 	/**
 	 * The n-D position in the dataset of the given index in the data array
 	 * 
 	 * @param n
 	 *            The index in the array
 	 * @return the corresponding [a,b,...,n] position in the dataset
 	 */
 	@Override
 	public int[] getNDPosition(final int n) {
 		if (n >= size) {
 			throw new IllegalArgumentException("Index provided " + n
 					+ "is larger then the size of the containing array");
 		}
 
 		if (shape.length == 1) {
 			return new int[] { n };
 		}
 
 		int r = shape.length;
 		int[] output = new int[r];
 
 		int inValue = n;
 		for (r--; r > 0; r--) {
 			output[r] = inValue % shape[r];
 			inValue /= shape[r];
 		}
 		output[0] = inValue;
 
 		return output;
 	}
 
 	/**
 	 * Check that axis is in range [-rank,rank)
 	 * 
 	 * @param axis
 	 * @return sanitized axis in range [0, rank)
 	 */
 	@Override
 	public int checkAxis(int axis) {
 		int rank = shape.length;
 		if (axis < 0) {
 			axis += rank;
 		}
 
 		if (axis < 0 || axis >= rank) {
 			throw new IndexOutOfBoundsException("Axis " + axis + " given is out of range [0, " + rank + ")");
 		}
 		return axis;
 	}
 
 	/**
 	 * Create a string representing the slice taken from given shape
 	 * @param shape
 	 * @param start
 	 * @param stop
 	 * @param step
 	 * @return string representation
 	 */
 	protected static String createSliceString(final int[] shape, final int[] start, final int[] stop, final int[] step) {
 		final int rank = shape.length;
		if (rank == 0) {
			return "()";
		}
 		StringBuilder s = new StringBuilder();
 		for (int i = 0; i < rank; i++) {
 			int l = shape[i];
 			int d = step == null ? 1 : step[i];
 			int b = start == null ? (d > 0 ? 0 : l - 1) : start[i];
 			int e = stop == null ? (d > 0 ? l : -1) : stop[i];
 
 			Slice.appendSliceToString(s, l, b, e, d);
 			s.append(',');
 		}
 
 		return s.substring(0, s.length()-1);
 	}
 
 	protected static final char BLOCK_OPEN = '[';
 	protected static final char BLOCK_CLOSE = ']';
 
 	@Override
 	public String toString() {
 		return toString(false);
 	}
 
 	/**
 	 * @param showData
 	 * @return string representation
 	 */
 	@Override
 	public String toString(boolean showData) {
 		final int rank = shape == null ? 0 : shape.length;
 		final StringBuilder out = new StringBuilder();
 
 		if (!showData) {
 			if (name != null && name.length() > 0) {
 				out.append("Dataset '");
 				out.append(name);
 				out.append("' has shape ");
 			} else {
 				out.append("Dataset shape is ");
 			}
 
 			out.append(BLOCK_OPEN);
 			if (rank > 0 && shape[0] > 0) {
 				out.append(shape[0]);
 			}
 			for (int i = 1; i < rank; i++) {
 				out.append(", " + shape[i]);
 			}
 			out.append(BLOCK_CLOSE);
 			return out.toString();
 		}
 
 		if (size == 0) {
 			return out.toString();
 		}
 
 		if (rank > 0) {
 			int[] pos = new int[rank];
 			final StringBuilder lead = new StringBuilder();
 			printBlocks(out, lead, 0, pos);
 		} else {
 			out.append(getString());
 		}
 		return out.toString();
 	}
 
 	private final static String SEPARATOR = ",";
 	private final static String SPACING = " ";
 	private final static String ELLIPSES = "...";
 	private final static String NEWLINE = "\n";
 
 	/**
 	 * Make a line of output for last dimension of dataset
 	 * 
 	 * @param start
 	 * @return line
 	 */
 	private StringBuilder makeLine(final int end, final int... start) {
 		StringBuilder line = new StringBuilder();
 		final int[] pos;
 		if (end >= start.length) {
 			pos = Arrays.copyOf(start, end + 1);
 		} else {
 			pos = start;
 		}
 		pos[end] = 0;
 		line.append(BLOCK_OPEN);
 		line.append(getString(pos));
 
 		final int length = shape[end];
 
 		// trim elements printed if length exceed estimate of maximum elements
 		int excess = length - MAX_STRING_LENGTH / 3; // space + number + separator
 		if (excess > 0) {
 			int index = (length - excess) / 2;
 			for (int y = 1; y < index; y++) {
 				line.append(SEPARATOR + SPACING);
 				pos[end] = y;
 				line.append(getString(pos));
 			}
 			index = (length + excess) / 2;
 			for (int y = index; y < length; y++) {
 				line.append(SEPARATOR + SPACING);
 				pos[end] = y;
 				line.append(getString(pos));
 			}
 		} else {
 			for (int y = 1; y < length; y++) {
 				line.append(SEPARATOR + SPACING);
 				pos[end] = y;
 				line.append(getString(pos));
 			}
 		}
 		line.append(BLOCK_CLOSE);
 
 		// trim string down to limit
 		excess = line.length() - MAX_STRING_LENGTH - ELLIPSES.length() - 1;
 		if (excess > 0) {
 			int index = line.substring(0, (line.length() - excess) / 2).lastIndexOf(SEPARATOR) + 2;
 			StringBuilder out = new StringBuilder(line.subSequence(0, index));
 			out.append(ELLIPSES + SEPARATOR);
 			index = line.substring((line.length() + excess) / 2).indexOf(SEPARATOR) + (line.length() + excess) / 2 + 1;
 			out.append(line.subSequence(index, line.length()));
 			return out;
 		}
 
 		return line;
 	}
 
 	/**
 	 * recursive method to print blocks
 	 */
 	private void printBlocks(final StringBuilder out, final StringBuilder lead, final int level, final int[] pos) {
 		if (out.length() > 0) {
 			char last = out.charAt(out.length() - 1);
 			if (last != BLOCK_OPEN) {
 				out.append(lead);
 			}
 		}
 		final int end = getRank() - 1;
 		if (level != end) {
 			out.append(BLOCK_OPEN);
 			int length = shape[level];
 
 			// first sub-block
 			pos[level] = 0;
 			StringBuilder newlead = new StringBuilder(lead);
 			newlead.append(SPACING);
 			printBlocks(out, newlead, level + 1, pos);
 			if (length < 2) { // escape
 				out.append(BLOCK_CLOSE);
 				return;
 			}
 
 			out.append(SEPARATOR + NEWLINE);
 			for (int i = level + 1; i < end; i++) {
 				out.append(NEWLINE);
 			}
 
 			// middle sub-blocks
 			if (length < MAX_SUBBLOCKS) {
 				for (int x = 1; x < length - 1; x++) {
 					pos[level] = x;
 					printBlocks(out, newlead, level + 1, pos);
 					if (end <= level + 1) {
 						out.append(SEPARATOR + NEWLINE);
 					} else {
 						out.append(SEPARATOR + NEWLINE + NEWLINE);
 					}
 				}
 			} else {
 				final int excess = length - MAX_SUBBLOCKS;
 				int xmax = (length - excess) / 2;
 				for (int x = 1; x < xmax; x++) {
 					pos[level] = x;
 					printBlocks(out, newlead, level + 1, pos);
 					if (end <= level + 1) {
 						out.append(SEPARATOR + NEWLINE);
 					} else {
 						out.append(SEPARATOR + NEWLINE + NEWLINE);
 					}
 				}
 				out.append(newlead);
 				out.append(ELLIPSES + SEPARATOR + NEWLINE);
 				xmax = (length + excess) / 2;
 				for (int x = xmax; x < length - 1; x++) {
 					pos[level] = x;
 					printBlocks(out, newlead, level + 1, pos);
 					if (end <= level + 1) {
 						out.append(SEPARATOR + NEWLINE);
 					} else {
 						out.append(SEPARATOR + NEWLINE + NEWLINE);
 					}
 				}
 			}
 
 			// last sub-block
 			pos[level] = length - 1;
 			printBlocks(out, newlead, level + 1, pos);
 			out.append(BLOCK_CLOSE);
 		} else {
 			out.append(makeLine(end, pos));
 		}
 	}
 
 	/**
 	 * This function allows anything that dirties the dataset to set stored values to null so that the other functions
 	 * can work correctly.
 	 */
 	@Override
 	public void setDirty() {
 		storedValues = null;
 	}
 
 	/**
 	 * Check the position given against the shape to make sure it is valid and sanitise it
 	 * 
 	 * @param pos
 	 * @return boolean
 	 */
 	protected boolean isPositionInShape(final int... pos) {
 		int pmax = pos.length;
 
 		// check the dimensionality of the request
 		if (pmax > shape.length) {
 			throw new IllegalArgumentException(String.format(
 					"Dimensionalities of requested position, %d, and dataset, %d, are incompatible", pos.length,
 					shape.length));
 		}
 
 		// if it's the right size or less, check to see if it's within bounds
 		for (int i = 0; i < pmax; i++) {
 			final int si = shape[i];
 			if (pos[i] < 0) {
 				pos[i] += si;
 			}
 			if (pos[i] < 0) {
 				throw new ArrayIndexOutOfBoundsException("Index (" + pos[i] + ") out of range [-" + si + "," + si
 						+ ") in dimension " + i);
 			}
 			if (pos[i] >= si)
 				return false;
 		}
 
 		return true;
 	}
 
 	/**
 	 * Remove dimensions of 1 in shape of a dataset
 	 */
 	@Override
 	public AbstractDataset squeeze() {
 		return squeeze(false);
 	}
 
 	/**
 	 * Remove dimensions of 1 in shape of a dataset - from both ends only, if true
 	 * 
 	 * @param onlyFromEnds
 	 */
 	@Override
 	public AbstractDataset squeeze(boolean onlyFromEnds) {
 		shape = squeezeShape(shape, onlyFromEnds);
 		return this;
 	}
 
 	/**
 	 * Remove dimensions of 1 in given shape - from both ends only, if true
 	 * 
 	 * @param oshape
 	 * @param onlyFromEnds
 	 * @return newly squeezed shape
 	 */
 	public static int[] squeezeShape(final int[] oshape, boolean onlyFromEnds) {
 		int unitDims = 0;
 		int rank = oshape.length;
 		int start = 0;
 
 		if (onlyFromEnds) {
 			int i = rank - 1;
 			for (; i >= 0; i--) {
 				if (oshape[i] == 1) {
 					unitDims++;
 				} else {
 					break;
 				}
 			}
 			for (int j = 0; j <= i; j++) {
 				if (oshape[j] == 1) {
 					unitDims++;
 				} else {
 					start = j;
 					break;
 				}
 			}
 		} else {
 			for (int i = 0; i < rank; i++) {
 				if (oshape[i] == 1) {
 					unitDims++;
 				}
 			}
 		}
 
 		if (unitDims == 0) {
 			return oshape;
 		}
 
 		int[] newDims = new int[rank - unitDims];
 		if (unitDims == rank)
 			return newDims; // zero-rank dataset
 
 		if (onlyFromEnds) {
 			rank = newDims.length;
 			for (int i = 0; i < rank; i++) {
 				newDims[i] = oshape[i+start];
 			}
 		} else {
 			int j = 0;
 			for (int i = 0; i < rank; i++) {
 				if (oshape[i] > 1) {
 					newDims[j++] = oshape[i];
 					if (j >= newDims.length)
 						break;
 				}
 			}
 		}
 
 		return newDims;
 	}
 
 	/**
 	 * Check if shapes are compatible, ignoring axes of length 1
 	 * 
 	 * @param ashape
 	 * @param bshape
 	 * @return true if they are compatible
 	 */
 	protected static boolean areShapesCompatible(final int[] ashape, final int[] bshape) {
 
 		List<Integer> alist = new ArrayList<Integer>();
 
 		for (int a : ashape) {
 			if (a > 1) alist.add(a);
 		}
 
 		final int imax = alist.size();
 		int i = 0;
 		for (int b : bshape) {
 			if (b == 1)
 				continue;
 			if (i >= imax || b != alist.get(i++))
 				return false;
 		}
 
 		return i == imax;
 	}
 
 	/**
 	 * Check if shapes are compatible but skip axis
 	 * 
 	 * @param ashape
 	 * @param bshape
 	 * @param axis
 	 * @return true if they are compatible
 	 */
 	public static boolean areShapesCompatible(final int[] ashape, final int[] bshape, final int axis) {
 		if (ashape.length != bshape.length) {
 			return false;
 		}
 
 		final int rank = ashape.length;
 		for (int i = 0; i < rank; i++) {
 			if (i != axis && ashape[i] != bshape[i]) {
 				return false;
 			}
 		}
 		return true;
 	}
 
 	/**
 	 * This function takes a dataset and checks its shape against the current dataset. If they are both of the same
 	 * size, then this returns true otherwise it returns false.
 	 * 
 	 * @param g
 	 *            The dataset to be compared
 	 * @return true if shapes are compatible
 	 */
 	@Override
 	public boolean isCompatibleWith(final ILazyDataset g) {
 		return areShapesCompatible(shape, g.getShape());
 	}
 
 	/**
 	 * This function takes a dataset and checks its shape against the current dataset. If they are both of the same
 	 * size, then this returns with no error, if there is a problem, then an error is thrown.
 	 * 
 	 * @param g
 	 *            The dataset to be compared
 	 * @throws IllegalArgumentException
 	 *             This will be thrown if there is a problem with the compatibility
 	 */
 	@Override
 	public void checkCompatibility(final ILazyDataset g) throws IllegalArgumentException {
 		checkCompatibility(this, g);
 	}
 
 	/**
 	 * This function takes a dataset and checks its shape against another dataset. If they are both of the same size,
 	 * then this returns with no error, if there is a problem, then an error is thrown.
 	 * 
 	 * @param g
 	 *            The first dataset to be compared
 	 * @param h
 	 *            The second dataset to be compared
 	 * @throws IllegalArgumentException
 	 *             This will be thrown if there is a problem with the compatibility
 	 */
 	public static void checkCompatibility(final ILazyDataset g, final ILazyDataset h) throws IllegalArgumentException {
 		if (!areShapesCompatible(g.getShape(), h.getShape())) {
 			throw new IllegalArgumentException("Shapes do not match");
 		}
 	}
 
 	/**
 	 * Returns dataset with new shape but old data <b>Warning</b> only works for un-expanded datasets! Copy the dataset
 	 * first
 	 * 
 	 * @param shape
 	 *            new shape
 	 */
 	@Override
 	public AbstractDataset reshape(final int... shape) {
 		AbstractDataset a = getView();
 		a.setShape(shape);
 		return a;
 	}
 
 	/**
 	 * Create a dataset from object (automatically detect dataset type)
 	 * 
 	 * @param obj
 	 *            can be a PySequence, Java array or Number
 	 * @return dataset
 	 */
 	public static AbstractDataset array(final Object obj) {
 		if (obj instanceof AbstractDataset)
 			return (AbstractDataset) obj;
 		if (obj instanceof ILazyDataset)
 			return DatasetUtils.convertToAbstractDataset((ILazyDataset) obj);
 
 		final int dtype = getDTypeFromObject(obj);
 		return array(obj, dtype);
 	}
 
 	/**
 	 * Create a dataset from object (automatically detect dataset type)
 	 * 
 	 * @param obj
 	 *            can be a PySequence, Java array or Number
 	 * @param isUnsigned
 	 *            if true, interpret integer values as unsigned by increasing element bit width
 	 * @return dataset
 	 */
 	public static AbstractDataset array(final Object obj, boolean isUnsigned) {
 		AbstractDataset a = array(obj);
 		if (isUnsigned) {
 			switch (a.getDtype()) {
 			case AbstractDataset.INT32:
 				a = new LongDataset(a);
 				DatasetUtils.unwrapUnsigned(a, 32);
 				break;
 			case AbstractDataset.INT16:
 				a = new IntegerDataset(a);
 				DatasetUtils.unwrapUnsigned(a, 16);
 				break;
 			case AbstractDataset.INT8:
 				a = new ShortDataset(a);
 				DatasetUtils.unwrapUnsigned(a, 8);
 				break;
 			case AbstractDataset.ARRAYINT32:
 				a = new CompoundLongDataset(a);
 				DatasetUtils.unwrapUnsigned(a, 32);
 				break;
 			case AbstractDataset.ARRAYINT16:
 				a = new CompoundIntegerDataset(a);
 				DatasetUtils.unwrapUnsigned(a, 16);
 				break;
 			case AbstractDataset.ARRAYINT8:
 				a = new CompoundShortDataset(a);
 				DatasetUtils.unwrapUnsigned(a, 8);
 				break;
 			}
 
 		}
 		return a;
 	}
 
 	/**
 	 * Create a dataset from object
 	 * 
 	 * @param obj
 	 *            can be a PySequence, Java array or Number
 	 * @param dtype
 	 * @return dataset
 	 */
 	public static AbstractDataset array(final Object obj, final int dtype) {
 		if (obj instanceof AbstractDataset)
 			return DatasetUtils.cast((AbstractDataset) obj, dtype);
 
 		if (obj instanceof ILazyDataset)
 			return DatasetUtils.cast(DatasetUtils.convertToAbstractDataset((ILazyDataset) obj), dtype);
 
 		switch (dtype) {
 		case BOOL:
 			return BooleanDataset.createFromObject(obj);
 		case INT8:
 			return ByteDataset.createFromObject(obj);
 		case INT16:
 			return ShortDataset.createFromObject(obj);
 		case INT32:
 			return IntegerDataset.createFromObject(obj);
 		case INT64:
 			return LongDataset.createFromObject(obj);
 		case ARRAYINT8:
 			return CompoundByteDataset.createFromObject(obj);
 		case ARRAYINT16:
 			return CompoundShortDataset.createFromObject(obj);
 		case ARRAYINT32:
 			return CompoundIntegerDataset.createFromObject(obj);
 		case ARRAYINT64:
 			return CompoundLongDataset.createFromObject(obj);
 		case FLOAT32:
 			return FloatDataset.createFromObject(obj);
 		case FLOAT64:
 			return DoubleDataset.createFromObject(obj);
 		case ARRAYFLOAT32:
 			return CompoundFloatDataset.createFromObject(obj);
 		case ARRAYFLOAT64:
 			return CompoundDoubleDataset.createFromObject(obj);
 		case COMPLEX64:
 			return ComplexFloatDataset.createFromObject(obj);
 		case COMPLEX128:
 			return ComplexDoubleDataset.createFromObject(obj);
 		case STRING:
 			return StringDataset.createFromObject(obj);
 		default:
 			return null;
 		}
 	}
 
 	/**
 	 * Create dataset of appropriate type from list
 	 * 
 	 * @param objectList
 	 * @return dataset filled with values from list
 	 */
 	public static AbstractDataset createFromList(List<?> objectList) {
 		if (objectList == null || objectList.size() == 0) {
 			throw new IllegalArgumentException("No list or zero-length list given");
 		}
 		Object obj = objectList.get(0);
 		if (obj instanceof Number || obj instanceof Complex) {
 			int dtype = getDTypeFromClass(obj.getClass());
 			int len = objectList.size();
 			AbstractDataset result = zeros(new int[] { len }, dtype);
 
 			int i = 0;
 			for (Object object : objectList) {
 				result.setObjectAbs(i++, object);
 			}
 			return result;
 		}
 		throw new IllegalArgumentException("Class of list element not supported");
 	}
 
 	/**
 	 * @param shape
 	 * @param dtype
 	 * @return a new dataset of given shape and type, filled with zeros
 	 */
 	public static AbstractDataset zeros(final int[] shape, final int dtype) {
 		switch (dtype) {
 		case BOOL:
 			return new BooleanDataset(shape);
 		case INT8:
 		case ARRAYINT8:
 			return new ByteDataset(shape);
 		case INT16:
 		case ARRAYINT16:
 			return new ShortDataset(shape);
 		case RGB:
 			return new RGBDataset(shape);
 		case INT32:
 		case ARRAYINT32:
 			return new IntegerDataset(shape);
 		case INT64:
 		case ARRAYINT64:
 			return new LongDataset(shape);
 		case FLOAT32:
 		case ARRAYFLOAT32:
 			return new FloatDataset(shape);
 		case FLOAT64:
 		case ARRAYFLOAT64:
 			return new DoubleDataset(shape);
 		case COMPLEX64:
 			return new ComplexFloatDataset(shape);
 		case COMPLEX128:
 			return new ComplexDoubleDataset(shape);
 		}
 		throw new IllegalArgumentException("dtype not known or unsupported");
 	}
 
 	/**
 	 * @param itemSize
 	 *            if equal to 1, then non-compound dataset is returned
 	 * @param shape
 	 * @param dtype
 	 * @return a new dataset of given item size, shape and type, filled with zeros
 	 */
 	public static AbstractDataset zeros(final int itemSize, final int[] shape, final int dtype) {
 		if (itemSize == 1) {
 			return zeros(shape, dtype);
 		}
 		switch (dtype) {
 		case INT8:
 		case ARRAYINT8:
 			return new CompoundByteDataset(itemSize, shape);
 		case INT16:
 		case ARRAYINT16:
 			return new CompoundShortDataset(itemSize, shape);
 		case RGB:
 			if (itemSize != 3) {
 				throw new IllegalArgumentException("Number of elements not compatible with RGB type");
 			}
 			return new RGBDataset(shape);
 		case INT32:
 		case ARRAYINT32:
 			return new CompoundIntegerDataset(itemSize, shape);
 		case INT64:
 		case ARRAYINT64:
 			return new CompoundLongDataset(itemSize, shape);
 		case FLOAT32:
 		case ARRAYFLOAT32:
 			return new CompoundFloatDataset(itemSize, shape);
 		case FLOAT64:
 		case ARRAYFLOAT64:
 			return new CompoundDoubleDataset(itemSize, shape);
 		case COMPLEX64:
 			if (itemSize != 2) {
 				throw new IllegalArgumentException("Number of elements not compatible with complex type");
 			}
 			return new ComplexFloatDataset(shape);
 		case COMPLEX128:
 			if (itemSize != 2) {
 				throw new IllegalArgumentException("Number of elements not compatible with complex type");
 			}
 			return new ComplexDoubleDataset(shape);
 		}
 		throw new IllegalArgumentException("dtype not a known compound type");
 	}
 
 	/**
 	 * @param dataset
 	 * @return a new dataset of same shape and type as input dataset, filled with zeros
 	 */
 	public static AbstractDataset zeros(final ADataset dataset) {
 		return zeros(dataset, dataset.getDtype());
 	}
 
 	/**
 	 * Create a new dataset of same shape as input dataset, filled with zeros. If dtype is not
 	 * explicitly compound then an elemental dataset is created 
 	 * @param dataset
 	 * @param dtype
 	 * @return a new dataset
 	 */
 	public static AbstractDataset zeros(final ADataset dataset, final int dtype) {
 		final int[] shape = dataset.getShapeRef();
 		final int isize = isDTypeElemental(dtype) ? 1 :dataset.getElementsPerItem();
 
 		return zeros(isize, shape, dtype);
 	}
 
 	/**
 	 * @param dataset
 	 * @return a new dataset of same shape and type as input dataset, filled with ones
 	 */
 	public static AbstractDataset ones(final ADataset dataset) {
 		return ones(dataset, dataset.getDtype());
 	}
 
 	/**
 	 * Create a new dataset of same shape as input dataset, filled with ones. If dtype is not
 	 * explicitly compound then an elemental dataset is created
 	 * @param dataset
 	 * @param dtype
 	 * @return a new dataset
 	 */
 	public static AbstractDataset ones(final ADataset dataset, final int dtype) {
 		final int[] shape = dataset.getShapeRef();
 		final int isize = isDTypeElemental(dtype) ? 1 :dataset.getElementsPerItem();
 
 		return ones(isize, shape, dtype);
 	}
 
 	/**
 	 * @param shape
 	 * @param dtype
 	 * @return a new dataset of given shape and type, filled with ones
 	 */
 	public static AbstractDataset ones(final int[] shape, final int dtype) {
 		switch (dtype) {
 		case BOOL:
 			return BooleanDataset.ones(shape);
 		case INT8:
 			return ByteDataset.ones(shape);
 		case INT16:
 			return ShortDataset.ones(shape);
 		case RGB:
 			return new RGBDataset(shape).fill(1);
 		case INT32:
 			return IntegerDataset.ones(shape);
 		case INT64:
 			return LongDataset.ones(shape);
 		case FLOAT32:
 			return FloatDataset.ones(shape);
 		case FLOAT64:
 			return DoubleDataset.ones(shape);
 		case COMPLEX64:
 			return ComplexFloatDataset.ones(shape);
 		case COMPLEX128:
 			return ComplexDoubleDataset.ones(shape);
 		}
 		throw new IllegalArgumentException("dtype not known");
 	}
 
 	/**
 	 * @param itemSize
 	 *            if equal to 1, then non-compound dataset is returned
 	 * @param shape
 	 * @param dtype
 	 * @return a new dataset of given item size, shape and type, filled with ones
 	 */
 	public static AbstractDataset ones(final int itemSize, final int[] shape, final int dtype) {
 		if (itemSize == 1) {
 			return ones(shape, dtype);
 		}
 		switch (dtype) {
 		case INT8:
 		case ARRAYINT8:
 			return CompoundByteDataset.ones(itemSize, shape);
 		case INT16:
 		case ARRAYINT16:
 			return CompoundShortDataset.ones(itemSize, shape);
 		case RGB:
 			if (itemSize != 3) {
 				throw new IllegalArgumentException("Number of elements not compatible with RGB type");
 			}
 			return new RGBDataset(shape).fill(1);
 		case INT32:
 		case ARRAYINT32:
 			return CompoundIntegerDataset.ones(itemSize, shape);
 		case INT64:
 		case ARRAYINT64:
 			return CompoundLongDataset.ones(itemSize, shape);
 		case FLOAT32:
 		case ARRAYFLOAT32:
 			return CompoundFloatDataset.ones(itemSize, shape);
 		case FLOAT64:
 		case ARRAYFLOAT64:
 			return CompoundDoubleDataset.ones(itemSize, shape);
 		case COMPLEX64:
 			if (itemSize != 2) {
 				throw new IllegalArgumentException("Number of elements not compatible with complex type");
 			}
 			return ComplexFloatDataset.ones(shape);
 		case COMPLEX128:
 			if (itemSize != 2) {
 				throw new IllegalArgumentException("Number of elements not compatible with complex type");
 			}
 			return ComplexDoubleDataset.ones(shape);
 		}
 		throw new IllegalArgumentException("dtype not a known compound type");
 	}
 
 	/**
 	 * @param stop
 	 * @param dtype
 	 * @return a new dataset of given shape and type, filled with values determined by parameters
 	 */
 	public static AbstractDataset arange(final double stop, final int dtype) {
 		return arange(0, stop, 1, dtype);
 	}
 
 	/**
 	 * @param start
 	 * @param stop
 	 * @param step
 	 * @return number of steps to take
 	 */
 	public static int calcSteps(final double start, final double stop, final double step) {
 		if (step > 0) {
 			return (int) Math.ceil((stop - start) / step);
 		}
 		return (int) Math.ceil((stop - start) / step);
 	}
 
 	/**
 	 * @param start
 	 * @param stop
 	 * @param step
 	 * @param dtype
 	 * @return a new 1D dataset of given type, filled with values determined by parameters
 	 */
 	public static AbstractDataset arange(final double start, final double stop, final double step, final int dtype) {
 		if ((step > 0) != (start <= stop)) {
 			return null;
 		}
 
 		switch (dtype) {
 		case BOOL:
 			break;
 		case INT8:
 			return ByteDataset.arange(start, stop, step);
 		case INT16:
 			return ShortDataset.arange(start, stop, step);
 		case INT32:
 			return IntegerDataset.arange(start, stop, step);
 		case INT64:
 			return LongDataset.arange(start, stop, step);
 		case FLOAT32:
 			return FloatDataset.arange(start, stop, step);
 		case FLOAT64:
 			return DoubleDataset.arange(start, stop, step);
 		case COMPLEX64:
 			return ComplexFloatDataset.arange(start, stop, step);
 		case COMPLEX128:
 			return ComplexDoubleDataset.arange(start, stop, step);
 		}
 		throw new IllegalArgumentException("dtype not known");
 	}
 
 	/**
 	 * @return true if dataset is complex
 	 */
 	@Override
 	public boolean isComplex() {
 		int type = getDtype();
 		return type == COMPLEX64 || type == COMPLEX128;
 	}
 
 	/**
 	 * @return real part of dataset as new dataset
 	 */
 	@Override
 	public AbstractDataset real() {
 		return this;
 	}
 
 	/**
 	 * In-place sort of dataset
 	 * 
 	 * @param axis
 	 *            to sort along
 	 * @return sorted dataset
 	 */
 	@Override
 	public AbstractDataset sort(Integer axis) {
 		int dtype = getDtype();
 		if (dtype == BOOL || dtype == COMPLEX64 || dtype == COMPLEX128 || getElementsPerItem() != 1) {
 			throw new UnsupportedOperationException("Cannot sort dataset");
 		}
 		if (axis == null) {
 			switch (dtype) {
 			case INT8:
 				Arrays.sort((byte[]) odata);
 				break;
 			case INT16:
 				Arrays.sort((short[]) odata);
 				break;
 			case INT32:
 				Arrays.sort((int[]) odata);
 				break;
 			case INT64:
 				Arrays.sort((long[]) odata);
 				break;
 			case FLOAT32:
 				Arrays.sort((float[]) odata);
 				break;
 			case FLOAT64:
 				Arrays.sort((double[]) odata);
 				break;
 			}
 		} else {
 			axis = checkAxis(axis);
 
 			AbstractDataset ads = zeros(new int[] { shape[axis] }, dtype);
 			Serializable adata = ads.getBuffer();
 
 			PositionIterator pi = getPositionIterator(axis);
 			int[] pos = pi.getPos();
 			boolean[] hit = pi.getOmit();
 			while (pi.hasNext()) {
 				copyItemsFromAxes(pos, hit, ads);
 				switch (dtype) {
 				case INT8:
 					Arrays.sort((byte[]) adata);
 					break;
 				case INT16:
 					Arrays.sort((short[]) adata);
 					break;
 				case INT32:
 					Arrays.sort((int[]) adata);
 					break;
 				case INT64:
 					Arrays.sort((long[]) adata);
 					break;
 				case FLOAT32:
 					Arrays.sort((float[]) adata);
 					break;
 				case FLOAT64:
 					Arrays.sort((double[]) adata);
 					break;
 				}
 				setItemsOnAxes(pos, hit, ads.getBuffer());
 			}
 		}
 		return this;
 	}
 
 	@Override
 	public AbstractDataset getSlice(final int[] start, final int[] stop, final int[] step) {
 		return getSlice((SliceIterator) getSliceIterator(start, stop, step));
 	}
 
 	@Override
 	abstract public AbstractDataset getSlice(final SliceIterator iterator);
 
 	@Override
 	public AbstractDataset getSlice(Slice... slice) {
 		final int rank = shape.length;
 		final int[] start = new int[rank];
 		final int[] stop = new int[rank];
 		final int[] step = new int[rank];
 
 		Slice.convertFromSlice(slice, shape, start, stop, step);
 
 		AbstractDataset s = getSlice(start, stop, step);
 		if (Arrays.equals(shape, s.shape)) {
 			s.setName(name);
 		} else {
 			s.setName(name + '[' + Slice.createString(slice) + ']');
 		}
 		return s;
 	}
 
 	@Override
 	public AbstractDataset getSlice(IMonitor monitor, Slice... slice) {
 		return getSlice(slice);
 	}
 
 	@Override
 	public AbstractDataset getSlice(IMonitor monitor, int[] start, int[] stop, int[] step) {
 		return getSlice(start, stop, step);
 	}
 
 	/**
 	 * 
 	 * @param object
 	 * @param slice
 	 */
 	@Override
 	public void setSlice(Object object, Slice... slice) {
 		final int rank = shape.length;
 		final int[] start = new int[rank];
 		final int[] stop = new int[rank];
 		final int[] step = new int[rank];
 
 		Slice.convertFromSlice(slice, shape, start, stop, step);
 
 		setSlice(object, start, stop, step);
 	}
 
 	/**
 	 * Test if all items are true
 	 */
 	@Override
 	public boolean all() {
 		final IndexIterator iter = getIterator();
 		while (iter.hasNext()) {
 			if (!getElementBooleanAbs(iter.index)) {
 				return false;
 			}
 		}
 		return true;
 	}
 
 	/**
 	 * @param axis
 	 * @return dataset where items are true if all items along axis are true
 	 */
 	@Override
 	public BooleanDataset all(final int axis) {
 		int rank = getRank();
 
 		int[] oshape = getShape();
 		int alen = oshape[axis];
 		oshape[axis] = 1;
 
 		int[] nshape = squeezeShape(oshape, false);
 		BooleanDataset all = new BooleanDataset(nshape);
 
 		IndexIterator qiter = all.getIterator(true);
 		int[] qpos = qiter.getPos();
 		int[] spos = oshape;
 
 		while (qiter.hasNext()) {
 			int i = 0;
 			for (; i < axis; i++) {
 				spos[i] = qpos[i];
 			}
 			spos[i++] = 0;
 			for (; i < rank; i++) {
 				spos[i] = qpos[i - 1];
 			}
 
 			boolean result = true;
 			for (int j = 0; j < alen; j++) {
 				spos[axis] = j;
 				if (getDouble(spos) == 0) {
 					result = false;
 					break;
 				}
 			}
 			all.set(result, qpos);
 		}
 		return all;
 	}
 
 	/**
 	 * Test if any items are true
 	 */
 	@Override
 	public boolean any() {
 		final IndexIterator iter = getIterator();
 		while (iter.hasNext()) {
 			if (getElementBooleanAbs(iter.index)) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * @param axis
 	 * @return dataset where items are true if any items along axis are true
 	 */
 	@Override
 	public BooleanDataset any(final int axis) {
 		int rank = getRank();
 
 		int[] oshape = getShape();
 		int alen = oshape[axis];
 		oshape[axis] = 1;
 
 		int[] nshape = squeezeShape(oshape, false);
 		BooleanDataset all = new BooleanDataset(nshape);
 
 		IndexIterator qiter = all.getIterator(true);
 		int[] qpos = qiter.getPos();
 		int[] spos = oshape;
 
 		while (qiter.hasNext()) {
 			int i = 0;
 			for (; i < axis; i++) {
 				spos[i] = qpos[i];
 			}
 			spos[i++] = 0;
 			for (; i < rank; i++) {
 				spos[i] = qpos[i - 1];
 			}
 
 			boolean result = false;
 			for (int j = 0; j < alen; j++) {
 				spos[axis] = j;
 				if (getDouble(spos) != 0) {
 					result = true;
 					break;
 				}
 			}
 			all.set(result, qpos);
 		}
 		return all;
 	}
 
 	/**
 	 * In-place addition with object o
 	 * 
 	 * @param o
 	 * @return sum dataset
 	 */
 	@Override
 	abstract public AbstractDataset iadd(final Object o);
 
 	/**
 	 * In-place subtraction with object o
 	 * 
 	 * @param o
 	 * @return difference dataset
 	 */
 	@Override
 	abstract public AbstractDataset isubtract(final Object o);
 
 	/**
 	 * In-place multiplication with object o
 	 * 
 	 * @param o
 	 * @return product dataset
 	 */
 	@Override
 	abstract public AbstractDataset imultiply(final Object o);
 
 	/**
 	 * In-place division with object o
 	 * 
 	 * @param o
 	 * @return dividend dataset
 	 */
 	@Override
 	abstract public AbstractDataset idivide(final Object o);
 
 	/**
 	 * In-place floor division with object o
 	 * 
 	 * @param o
 	 * @return dividend dataset
 	 */
 	@Override
 	public AbstractDataset ifloorDivide(final Object o) {
 		return idivide(o).ifloor();
 	}
 
 	/**
 	 * In-place remainder
 	 * 
 	 * @return remaindered dataset
 	 */
 	@Override
 	abstract public AbstractDataset iremainder(final Object o);
 
 	/**
 	 * In-place floor
 	 * 
 	 * @return floored dataset
 	 */
 	@Override
 	abstract public AbstractDataset ifloor();
 
 	/**
 	 * In-place raise to power of object o
 	 * 
 	 * @param o
 	 * @return raised dataset
 	 */
 	@Override
 	abstract public AbstractDataset ipower(final Object o);
 
 	/**
 	 * Calculate residual of dataset with object o
 	 * See {@link #residual(Object o, boolean ignoreNaNs)} with ignoreNaNs = false
 	 * 
 	 * @param o
 	 * @return sum of the squares of the differences
 	 */
 	@Override
 	public double residual(final Object o) {
 		return residual(o, false);
 	}
 
 	/**
 	 * Calculate residual of dataset with object o
 	 * 
 	 * @param o
 	 * @param ignoreNaNs if true, skip NaNs
 	 * @return sum of the squares of the differences
 	 */
 	@Override
 	abstract public double residual(final Object o, boolean ignoreNaNs);
 
 	public static final String STORE_HASH = "hash";
 	protected static final String STORE_SHAPELESS_HASH = "shapelessHash";
 	public static final String STORE_MAX = "max";
 	public static final String STORE_MIN = "min";
 	protected static final String STORE_MAX_POS = "maxPos";
 	protected static final String STORE_MIN_POS = "minPos";
 	protected static final String STORE_STATS = "stats";
 	protected static final String STORE_SUM = "sum";
 	protected static final String STORE_MEAN = "mean";
 	protected static final String STORE_VAR = "var";
 	private static final String STORE_POS_MAX = "+max";
 	private static final String STORE_POS_MIN = "+min";
 	private static final String STORE_COUNT = "count";
 	private static final String STORE_INDEX = "Index";
 
 
 	/**
 	 * Get value from store
 	 * 
 	 * @param key
 	 * @return value
 	 */
 	public Object getStoredValue(String key) {
 		if (storedValues == null) {
 			return null;
 		}
 
 		return storedValues.get(key);
 	}
 
 	/**
 	 * Set value in store
 	 * <p>
 	 * This is a <b>private method</b>: do not use!
 	 * 
 	 * @param key
 	 * @param obj
 	 */
 	public void setStoredValue(String key, Object obj) {
 		if (storedValues == null) {
 			storedValues = new HashMap<String, Object>();
 		}
 
 		storedValues.put(key, obj);
 	}
 
 	protected static String storeName(boolean ignoreNaNs, String name) {
 		return storeName(ignoreNaNs, false, name);
 	}
 
 	protected static String storeName(boolean ignoreNaNs, boolean ignoreInfs, String name) {
 		return (ignoreInfs ? "inf" : "") + (ignoreNaNs ? "nan" : "") +  name;
 	}
 
 	/**
 	 * Copy stored values from original to derived dataset
 	 * @param orig
 	 * @param derived
 	 * @param shapeChanged
 	 */
 	protected static void copyStoredValues(IDataset orig, AbstractDataset derived, boolean shapeChanged) {
 		if (orig instanceof AbstractDataset && ((AbstractDataset) orig).storedValues != null) {
 			derived.storedValues = new HashMap<String, Object>(((AbstractDataset) orig).storedValues);
 			if (shapeChanged) {
 				filterStoredValues(derived.storedValues);
 			}
 		}
 	}
 
 	private static void filterStoredValues(Map<String, Object> map) {
 		map.remove(STORE_HASH);
 		List<String> keys = new ArrayList<String>();
 		for (String n : map.keySet()) {
 			if (n.contains("-")) { // remove anything which is axis-specific
 				keys.add(n);
 			}
 		}
 		for (String n : keys) {
 			map.remove(n);
 		}
 	}
 
 	/**
 	 * Calculate minimum and maximum for a dataset
 	 * @param ignoreNaNs if true, ignore NaNs
 	 * @param ignoreInfs if true, ignore infinities
 	 */
 	protected void calculateMaxMin(final boolean ignoreNaNs, final boolean ignoreInfs) {
 		IndexIterator iter = getIterator();
 		double amax = Double.NEGATIVE_INFINITY;
 		double amin = Double.POSITIVE_INFINITY;
 		double pmax = Double.MIN_VALUE;
 		double pmin = Double.POSITIVE_INFINITY;
 		double hash = 0;
 		boolean hasNaNs = false;
 
 		while (iter.hasNext()) {
 			final double val = getElementDoubleAbs(iter.index);
 			if (Double.isNaN(val)) {
 				hash = (hash * 19) % Integer.MAX_VALUE;
 				if (ignoreNaNs)
 					continue;
 				hasNaNs = true;
 			} else if (Double.isInfinite(val)) {
 				hash = (hash * 19) % Integer.MAX_VALUE;
 				if (ignoreInfs)
 					continue;
 			} else {
 				hash = (hash * 19 + val) % Integer.MAX_VALUE;
 			}
 
 			if (val > amax) {
 				amax = val;
 			}
 			if (val < amin) {
 				amin = val;
 			}
 			if (val > 0) {
 				if (val < pmin) {
 					pmin = val;
 				}
 				if (val > pmax) {
 					pmax = val;
 				}
 			}
 		}
 
 		int ihash = ((int) hash) * 19 + getDtype() * 17 + getElementsPerItem();
 		setStoredValue(storeName(ignoreNaNs, ignoreInfs, STORE_SHAPELESS_HASH), ihash);
 		storedValues.put(storeName(ignoreNaNs, ignoreInfs, STORE_MAX), hasNaNs ? Double.NaN : fromDoubleToNumber(amax));
 		storedValues.put(storeName(ignoreNaNs, ignoreInfs, STORE_MIN), hasNaNs ? Double.NaN : fromDoubleToNumber(amin));
 		storedValues.put(storeName(ignoreNaNs, ignoreInfs, STORE_POS_MAX), hasNaNs ? Double.NaN : fromDoubleToNumber(pmax));
 		storedValues.put(storeName(ignoreNaNs, ignoreInfs, STORE_POS_MIN), hasNaNs ? Double.NaN : fromDoubleToNumber(pmin));
 	}
 
 	/**
 	 * Calculate summary statistics for a dataset
 	 * @param ignoreNaNs if true, ignore NaNs
 	 * @param ignoreInfs if true, ignore infinities
 	 * @param name
 	 */
 	protected void calculateSummaryStats(final boolean ignoreNaNs, final boolean ignoreInfs, final String name) {
 		final IndexIterator iter = getIterator();
 		final SummaryStatistics stats = new SummaryStatistics();
 
 		if (storedValues == null || !storedValues.containsKey(STORE_HASH)) {
 			boolean hasNaNs = false;
 			double hash = 0;
 			double pmax = Double.MIN_VALUE;
 			double pmin = Double.POSITIVE_INFINITY;
 
 			while (iter.hasNext()) {
 				final double val = getElementDoubleAbs(iter.index);
 				if (Double.isNaN(val)) {
 					hash = (hash * 19) % Integer.MAX_VALUE;
 					if (ignoreNaNs)
 						continue;
 					hasNaNs = true;
 				} else if (Double.isInfinite(val)) {
 					hash = (hash * 19) % Integer.MAX_VALUE;
 					if (ignoreInfs)
 						continue;
 				} else {
 					hash = (hash * 19 + val) % Integer.MAX_VALUE;
 				}
 				if (val > 0) {
 					if (val < pmin) {
 						pmin = val;
 					}
 					if (val > pmax) {
 						pmax = val;
 					}
 				}
 				stats.addValue(val);
 			}
 
 			int ihash = ((int) hash) * 19 + getDtype() * 17 + getElementsPerItem();
 			setStoredValue(storeName(ignoreNaNs, ignoreInfs, STORE_SHAPELESS_HASH), ihash);
 			storedValues.put(storeName(ignoreNaNs, ignoreInfs, STORE_MAX), hasNaNs ? Double.NaN : fromDoubleToNumber(stats.getMax()));
 			storedValues.put(storeName(ignoreNaNs, ignoreInfs, STORE_MIN), hasNaNs ? Double.NaN : fromDoubleToNumber(stats.getMin()));
 			storedValues.put(storeName(ignoreNaNs, ignoreInfs, STORE_POS_MAX), hasNaNs ? Double.NaN : fromDoubleToNumber(pmax));
 			storedValues.put(storeName(ignoreNaNs, ignoreInfs, STORE_POS_MIN), hasNaNs ? Double.NaN : fromDoubleToNumber(pmin));
 			storedValues.put(name, stats);
 		} else {
 			while (iter.hasNext()) {
 				final double val = getElementDoubleAbs(iter.index);
 				if (ignoreNaNs && Double.isNaN(val)) {
 					continue;
 				}
 				if (ignoreInfs && Double.isInfinite(val)) {
 					continue;
 				}
 
 				stats.addValue(val);
 			}
 
 			storedValues.put(name, stats);
 		}
 	}
 
 	/**
 	 * Calculate summary statistics for a dataset along an axis
 	 * @param ignoreNaNs if true, ignore NaNs
 	 * @param ignoreInfs if true, ignore infinities
 	 * @param axis
 	 */
 	protected void calculateSummaryStats(final boolean ignoreNaNs, final boolean ignoreInfs, final int axis) {
 		int rank = getRank();
 
 		int[] oshape = getShape();
 		int alen = oshape[axis];
 		oshape[axis] = 1;
 
 		int[] nshape = new int[rank - 1];
 		for (int i = 0; i < axis; i++) {
 			nshape[i] = oshape[i];
 		}
 		for (int i = axis + 1; i < rank; i++) {
 			nshape[i - 1] = oshape[i];
 		}
 
 		final int dtype = getDtype();
 		IntegerDataset count = new IntegerDataset(nshape);
 		AbstractDataset max = zeros(nshape, dtype);
 		AbstractDataset min = zeros(nshape, dtype);
 		IntegerDataset maxIndex = new IntegerDataset(nshape);
 		IntegerDataset minIndex = new IntegerDataset(nshape);
 		AbstractDataset sum = zeros(nshape, getLargestDType(dtype));
 		DoubleDataset mean = new DoubleDataset(nshape);
 		DoubleDataset var = new DoubleDataset(nshape);
 
 		IndexIterator qiter = max.getIterator(true);
 		int[] qpos = qiter.getPos();
 		int[] spos = oshape.clone();
 
 		while (qiter.hasNext()) {
 			int i = 0;
 			for (; i < axis; i++) {
 				spos[i] = qpos[i];
 			}
 			spos[i++] = 0;
 			for (; i < rank; i++) {
 				spos[i] = qpos[i - 1];
 			}
 
 			final SummaryStatistics stats = new SummaryStatistics();
 			double amax = Double.NEGATIVE_INFINITY;
 			double amin = Double.POSITIVE_INFINITY;
 			boolean hasNaNs = false;
 			if (ignoreNaNs) {
 				for (int j = 0; j < alen; j++) {
 					spos[axis] = j;
 					final double val = getDouble(spos);
 
 					if (Double.isNaN(val)) {
 						hasNaNs = true;
 						continue;
 					} else if (ignoreInfs && Double.isInfinite(val)) {
 						continue;
 					}
 
 					if (val > amax) {
 						amax = val;
 					}
 					if (val < amin) {
 						amin = val;
 					}
 
 					stats.addValue(val);
 				}
 			} else {
 				for (int j = 0; j < alen; j++) {
 					spos[axis] = j;
 					final double val = getDouble(spos);
 
 					if (hasNaNs) {
 						if (!Double.isNaN(val))
 							stats.addValue(0);
 						continue;
 					}
 
 					if (Double.isNaN(val)) {
 						amax = Double.NaN;
 						amin = Double.NaN;
 						hasNaNs = true;
 					} else if (ignoreInfs && Double.isInfinite(val)) {
 						continue;
 					} else {
 						if (val > amax) {
 							amax = val;
 						}
 						if (val < amin) {
 							amin = val;
 						}
 					}
 					stats.addValue(val);
 				}
 			}
 
 			count.setAbs(qiter.index, (int) stats.getN());
 
 			max.setObjectAbs(qiter.index, amax);
 			min.setObjectAbs(qiter.index, amin);
 			boolean fmax = false;
 			boolean fmin = false;
 			if (hasNaNs) {
 				if (ignoreNaNs) {
 					for (int j = 0; j < alen; j++) {
 						spos[axis] = j;
 						final double val = getDouble(spos);
 						if (Double.isNaN(val))
 							continue;
 
 						if (!fmax && val == amax) {
 							maxIndex.setAbs(qiter.index, j);
 							fmax = true;
 							if (fmin)
 								break;
 						}
 						if (!fmin && val == amin) {
 							minIndex.setAbs(qiter.index, j);
 							fmin = true;
 							if (fmax)
 								break;
 						}
 					}
 				} else {
 					for (int j = 0; j < alen; j++) {
 						spos[axis] = j;
 						final double val = getDouble(spos);
 						if (Double.isNaN(val)) {
 							maxIndex.setAbs(qiter.index, j);
 							minIndex.setAbs(qiter.index, j);
 							break;
 						}
 					}
 				}
 			} else {
 				for (int j = 0; j < alen; j++) {
 					spos[axis] = j;
 					final double val = getDouble(spos);
 					if (!fmax && val == amax) {
 						maxIndex.setAbs(qiter.index, j);
 						fmax = true;
 						if (fmin)
 							break;
 					}
 					if (!fmin && val == amin) {
 						minIndex.setAbs(qiter.index, j);
 						fmin = true;
 						if (fmax)
 							break;
 					}
 				}
 			}
 			sum.setObjectAbs(qiter.index, stats.getSum());
 			mean.setAbs(qiter.index, stats.getMean());
 			var.setAbs(qiter.index, stats.getVariance());
 		}
 		setStoredValue(storeName(ignoreNaNs, ignoreInfs, STORE_COUNT + "-" + axis), count);
 		storedValues.put(storeName(ignoreNaNs, ignoreInfs, STORE_MAX + "-" + axis), max);
 		storedValues.put(storeName(ignoreNaNs, ignoreInfs, STORE_MIN + "-" + axis), min);
 		storedValues.put(storeName(ignoreNaNs, ignoreInfs, STORE_SUM + "-" + axis), sum);
 		storedValues.put(storeName(ignoreNaNs, ignoreInfs, STORE_MEAN + "-" + axis), mean);
 		storedValues.put(storeName(ignoreNaNs, ignoreInfs, STORE_VAR + "-" + axis), var);
 		storedValues.put(storeName(ignoreNaNs, ignoreInfs, STORE_MAX + STORE_INDEX + "-" + axis), maxIndex);
 		storedValues.put(storeName(ignoreNaNs, ignoreInfs, STORE_MIN + STORE_INDEX + "-" + axis), minIndex);
 	}
 
 	private Number fromDoubleToNumber(double x) {
 		switch (getDtype()) {
 		case BOOL:
 		case INT32:
 			return Integer.valueOf((int) (long) x);
 		case INT8:
 			return Byte.valueOf((byte) (long) x);
 		case INT16:
 			return Short.valueOf((short) (long) x);
 		case INT64:
 			return Long.valueOf((long) x);
 		case FLOAT32:
 			return Float.valueOf((float) x);
 		case FLOAT64:
 			return Double.valueOf(x);
 		}
 		return null;
 	}
 
 	// return biggest native primitive if integer (should test for 64bit?)
 	private static Number fromDoubleToBiggestNumber(double x, int dtype) {
 		switch (dtype) {
 		case BOOL:
 		case INT8:
 		case INT16:
 		case INT32:
 			return Integer.valueOf((int) (long) x);
 		case INT64:
 			return Long.valueOf((long) x);
 		case FLOAT32:
 			return Float.valueOf((float) x);
 		case FLOAT64:
 			return Double.valueOf(x);
 		}
 		return null;
 	}
 
 	private SummaryStatistics getStatistics(boolean ignoreNaNs) {
 		boolean ignoreInfs = false; // TODO
 		if (!hasFloatingPointElements()) {
 			ignoreNaNs = false;
 		}
 
 		String n = storeName(ignoreNaNs, ignoreInfs, STORE_STATS);
 		SummaryStatistics stats = (SummaryStatistics) getStoredValue(n);
 		if (stats == null) {
 			calculateSummaryStats(ignoreNaNs, ignoreInfs, n);
 			stats = (SummaryStatistics) getStoredValue(n);
 		}
 
 		return stats;
 	}
 
 	@Override
 	public int[] maxPos() {
 		return maxPos(false);
 	}
 
 	@Override
 	public int[] minPos() {
 		return minPos(false);
 	}
 
 	private int getHash() {
 		Object value = getStoredValue(STORE_HASH);
 		if (value == null) {
 			value = getStoredValue(STORE_SHAPELESS_HASH);
 			if (value == null) {
 				calculateMaxMin(false, false);
 				value = getStoredValue(STORE_SHAPELESS_HASH);
 			}
 
 			int ihash = (Integer) value;
 			int rank = shape.length;
 			for (int i = 0; i < rank; i++) {
 				ihash = ihash * 17 + shape[i];
 			}
 			storedValues.put(STORE_HASH, ihash);
 			return ihash;
 		}
 
 		return (Integer) value;
 	}
 
 	protected Object getMaxMin(boolean ignoreNaNs, boolean ignoreInfs, String key) {
 		if (!hasFloatingPointElements()) {
 			ignoreNaNs = false;
 			ignoreInfs = false;
 		}
 
 		key = storeName(ignoreNaNs, ignoreInfs , key);
 		Object value = getStoredValue(key);
 		if (value == null) {
 			calculateMaxMin(ignoreNaNs, ignoreInfs);
 			value = getStoredValue(key);
 		}
 
 		return value;
 	}
 
 	private Object getStatistics(boolean ignoreNaNs, int axis, String stat) {
 		if (!hasFloatingPointElements())
 			ignoreNaNs = false;
 
 		boolean ignoreInfs = false; // TODO
 		stat = storeName(ignoreNaNs, ignoreInfs , stat);
 		axis = checkAxis(axis);
 		Object obj = getStoredValue(stat);
 
 		if (obj == null) {
 			calculateSummaryStats(ignoreNaNs, ignoreInfs, axis);
 			obj = getStoredValue(stat);
 		}
 
 		return obj;
 	}
 
 	/**
 	 * See {@link #max(boolean ignoreNaNs)} with ignoreNaNs = false
 	 */
 	@Override
 	public Number max() {
 		return max(false);
 	}
 
 	/**
 	 * @param ignoreInvalids if true, ignore NaNs and infinities
 	 * @return maximum
 	 */
 	@Override
 	public Number max(boolean ignoreInvalids) {
 		return (Number) getMaxMin(ignoreInvalids, ignoreInvalids, STORE_MAX);
 	}
 
 	/**
 	 * @param ignoreNaNs if true, ignore NaNs
 	 * @param ignoreInfs if true, ignore infinities
 	 * @return minimum
 	 */
 	@Override
 	public Number max(boolean ignoreNaNs, boolean ignoreInfs) {
 		return (Number) getMaxMin(ignoreNaNs, ignoreInfs, STORE_MAX);
 	}
 
 	/**
 	 * @param ignoreInvalids if true, ignore NaNs and infinities
 	 * @return minimum positive value (or infinity if there are no positive values)
 	 */
 	@Override
 	public Number positiveMax(boolean ignoreInvalids) {
 		return (Number) getMaxMin(ignoreInvalids, ignoreInvalids, STORE_POS_MAX);
 	}
 
 	/**
 	 * @param ignoreNaNs if true, ignore NaNs
 	 * @param ignoreInfs if true, ignore infinities
 	 * @return maximum positive value (or Double.MIN_VALUE=2^-1024 if there are no positive values)
 	 */
 	@Override
 	public Number positiveMax(boolean ignoreNaNs, boolean ignoreInfs) {
 		return (Number) getMaxMin(ignoreNaNs, ignoreInfs, STORE_POS_MAX);
 	}
 
 	/**
 	 * See {@link #max(boolean ignoreNaNs, int axis)} with ignoreNaNs = false
 	 * @param axis
 	 * @return maxima along axis in dataset
 	 */
 	@Override
 	public AbstractDataset max(int axis) {
 		return max(false, axis);
 	}
 
 	/**
 	 * @param ignoreNaNs if true, ignore NaNs
 	 * @param axis
 	 * @return maxima along axis in dataset
 	 */
 	@Override
 	public AbstractDataset max(boolean ignoreNaNs, int axis) {
 		return (AbstractDataset) getStatistics(ignoreNaNs, axis, STORE_MAX + "-" + axis);
 	}
 
 	/**
 	 * See {@link #min(boolean ignoreNaNs)} with ignoreNaNs = false
 	 */
 	@Override
 	public Number min() {
 		return min(false);
 	}
 
 	/**
 	 * @param ignoreInvalids if true, ignore NaNs and infinities
 	 * @return minimum
 	 */
 	@Override
 	public Number min(boolean ignoreInvalids) {
 		return (Number) getMaxMin(ignoreInvalids, ignoreInvalids, STORE_MIN);
 	}
 
 	/**
 	 * @param ignoreNaNs if true, ignore NaNs
 	 * @param ignoreInfs if true, ignore infinities
 	 * @return minimum
 	 */
 	@Override
 	public Number min(boolean ignoreNaNs, boolean ignoreInfs) {
 		return (Number) getMaxMin(ignoreNaNs, ignoreInfs, STORE_MIN);
 	}
 
 	/**
 	 * @param ignoreInvalids if true, ignore NaNs and infinities
 	 * @return minimum positive value (or infinity if there are no positive values)
 	 */
 	@Override
 	public Number positiveMin(boolean ignoreInvalids) {
 		return (Number) getMaxMin(ignoreInvalids, ignoreInvalids, STORE_POS_MIN);
 	}
 
 	/**
 	 * @param ignoreNaNs if true, ignore NaNs
 	 * @param ignoreInfs if true, ignore infinities
 	 * @return minimum positive value (or infinity if there are no positive values)
 	 */
 	@Override
 	public Number positiveMin(boolean ignoreNaNs, boolean ignoreInfs) {
 		return (Number) getMaxMin(ignoreNaNs, ignoreInfs, STORE_POS_MIN);
 	}
 
 	/**
 	 * See {@link #min(boolean ignoreNaNs, int axis)} with ignoreNaNs = false
 	 * @param axis
 	 * @return minima along axis in dataset
 	 */
 	@Override
 	public AbstractDataset min(int axis) {
 		return min(false, axis);
 	}
 
 	/**
 	 * @param ignoreNaNs if true, ignore NaNs
 	 * @param axis
 	 * @return minima along axis in dataset
 	 */
 	@Override
 	public AbstractDataset min(boolean ignoreNaNs, int axis) {
 		return (AbstractDataset) getStatistics(ignoreNaNs, axis, STORE_MIN + "-" + axis);
 	}
 
 	/**
 	 * Find absolute index of maximum value.
 	 * See {@link #argMax(boolean ignoreNaNs)} with ignoreNaNs = false
 	 * 
 	 * @return absolute index
 	 */
 	@Override
 	public int argMax() {
 		return argMax(false);
 	}
 
 	/**
 	 * Find absolute index of maximum value
 	 * 
 	 * @param ignoreInvalids if true, ignore NaNs and infinities
 	 * @return absolute index
 	 */
 	@Override
 	public int argMax(boolean ignoreInvalids) {
 		return get1DIndex(maxPos(ignoreInvalids));
 	}
 
 	/**
 	 * Find indices of maximum values along given axis.
 	 * See {@link #argMax(boolean ignoreNaNs, int axis)} with ignoreNaNs = false
 	 * 
 	 * @param axis
 	 * @return index dataset
 	 */
 	@Override
 	public IntegerDataset argMax(int axis) {
 		return argMax(false, axis);
 	}
 
 	/**
 	 * Find indices of maximum values along given axis
 	 * 
 	 * @param ignoreNaNs if true, ignore NaNs
 	 * @param axis
 	 * @return index dataset
 	 */
 	@Override
 	public IntegerDataset argMax(boolean ignoreNaNs, int axis) {
 		return (IntegerDataset) getStatistics(ignoreNaNs, axis, STORE_MAX + STORE_INDEX + "-" + axis);
 	}
 
 	/**
 	 * Find absolute index of minimum value.
 	 * See {@link #argMin(boolean ignoreNaNs)} with ignoreNaNs = false
 	 * 
 	 * @return absolute index
 	 */
 	@Override
 	public int argMin() {
 		return argMin(false);
 	}
 
 	/**
 	 * Find absolute index of minimum value
 	 * 
 	 * @param ignoreInvalids if true, ignore NaNs and infinities
 	 * @return absolute index
 	 */
 	@Override
 	public int argMin(boolean ignoreInvalids) {
 		return get1DIndex(minPos(ignoreInvalids));
 	}
 
 	/**
 	 * Find indices of minimum values along given axis.
 	 * See {@link #argMin(boolean ignoreNaNs, int axis)} with ignoreNaNs = false
 	 * 
 	 * @param axis
 	 * @return index dataset
 	 */
 	@Override
 	public IntegerDataset argMin(int axis) {
 		return argMin(false, axis);
 	}
 
 	/**
 	 * Find indices of minimum values along given axis
 	 * 
 	 * @param ignoreNaNs if true, ignore NaNs
 	 * @param axis
 	 * @return index dataset
 	 */
 	@Override
 	public IntegerDataset argMin(boolean ignoreNaNs, int axis) {
 		return (IntegerDataset) getStatistics(ignoreNaNs, axis, STORE_MIN + STORE_INDEX + "-" + axis);
 	}
 
 	/**
 	 * @return peak-to-peak value, the difference of maximum and minimum of dataset
 	 */
 	@Override
 	public Number peakToPeak() {
 		return fromDoubleToNumber(max().doubleValue() - min().doubleValue());
 	}
 
 	/**
 	 * @param axis
 	 * @return peak-to-peak dataset, the difference of maxima and minima of dataset along axis
 	 */
 	@Override
 	public AbstractDataset peakToPeak(int axis) {
 		return Maths.subtract(max(axis), min(axis));
 	}
 
 	/**
 	 * See {@link #count(boolean ignoreNaNs)} with ignoreNaNs = false
 	 * @return number of items in dataset
 	 */
 	@Override
 	public long count() {
 		return count(false);
 	}
 
 	/**
 	 * @param ignoreNaNs if true, ignore NaNs (treat as zeros)
 	 * @return number of items in dataset
 	 */
 	@Override
 	public long count(boolean ignoreNaNs) {
 		return getStatistics(ignoreNaNs).getN();
 	}
 
 	/**
 	 * See {@link #count(boolean ignoreNaNs, int axis)} with ignoreNaNs = false
 	 * @param axis
 	 * @return number of items along axis in dataset
 	 */
 	@Override
 	public AbstractDataset count(int axis) {
 		return count(false, axis);
 	}
 
 	/**
 	 * @param ignoreNaNs if true, ignore NaNs (treat as zeros)
 	 * @param axis
 	 * @return number of items along axis in dataset
 	 */
 	@Override
 	public AbstractDataset count(boolean ignoreNaNs, int axis) {
 		return (AbstractDataset) getStatistics(ignoreNaNs, axis, STORE_COUNT + "-" + axis);
 	}
 
 	/**
 	 * See {@link #sum(boolean ignoreNaNs)} with ignoreNaNs = false
 	 * @return sum over all items in dataset as a Double, array of doubles or a complex number
 	 */
 	@Override
 	public Object sum() {
 		return sum(false);
 	}
 
 	/**
 	 * @param ignoreNaNs if true, ignore NaNs (treat as zeros)
 	 * @return sum over all items in dataset as a Double, array of doubles or a complex number
 	 */
 	@Override
 	public Object sum(boolean ignoreNaNs) {
 		return getStatistics(ignoreNaNs).getSum();
 	}
 
 	/**
 	 * See {@link #sum(boolean ignoreNaNs, int axis)} with ignoreNaNs = false
 	 * @param axis
 	 * @return sum along axis in dataset
 	 */
 	@Override
 	public AbstractDataset sum(int axis) {
 		return sum(false, axis);
 	}
 
 	/**
 	 * @param ignoreNaNs if true, ignore NaNs (treat as zeros)
 	 * @param axis
 	 * @return sum along axis in dataset
 	 */
 	@Override
 	public AbstractDataset sum(boolean ignoreNaNs, int axis) {
 		return (AbstractDataset) getStatistics(ignoreNaNs, axis, STORE_SUM + "-" + axis);
 	}
 
 	/**
 	 * @return sum over all items in dataset as appropriate to dataset type
 	 * (integers for boolean, byte, short and integer; longs for long; floats for float; doubles for double)
 	 */
 	@Override
 	public Object typedSum() {
 		return typedSum(getDtype());
 	}
 
 	/**
 	 * @param dtype
 	 * @return sum over all items in dataset as appropriate to given dataset type
 	 */
 	@Override
 	public Object typedSum(int dtype) {
 		return fromDoubleToBiggestNumber(getStatistics(false).getSum(), dtype);
 	}
 
 	/**
 	 * @param dtype
 	 * @param axis
 	 * @return sum along axis in dataset
 	 */
 	@Override
 	public AbstractDataset typedSum(int dtype, int axis) {
 		return DatasetUtils.cast(sum(axis), dtype);
 	}
 
 	/**
 	 * @return product over all items in dataset
 	 */
 	@Override
 	public Object product() {
 		return Stats.product(this);
 	}
 
 	/**
 	 * @param axis
 	 * @return product along axis in dataset
 	 */
 	@Override
 	public AbstractDataset product(int axis) {
 		return Stats.product(this, axis);
 	}
 
 	/**
 	 * @param dtype
 	 * @return product over all items in dataset
 	 */
 	@Override
 	public Object typedProduct(int dtype) {
 		return Stats.typedProduct(this, dtype);
 	}
 
 	/**
 	 * @param dtype
 	 * @param axis
 	 * @return product along axis in dataset
 	 */
 	@Override
 	public AbstractDataset typedProduct(int dtype, int axis) {
 		return Stats.typedProduct(this, dtype, axis);
 	}
 
 	/**
 	 * See {@link #mean(boolean ignoreNaNs)} with ignoreNaNs = false
 	 * @return mean of all items in dataset as a double, array of doubles or a complex number
 	 */
 	@Override
 	public Object mean() {
 		return mean(false);
 	}
 
 	/**
 	 * @param ignoreNaNs if true, skip NaNs
 	 * @return mean of all items in dataset as a double, array of doubles or a complex number
 	 */
 	@Override
 	public Object mean(boolean ignoreNaNs) {
 		return getStatistics(ignoreNaNs).getMean();
 	}
 
 	/**
 	 * See {@link #mean(boolean ignoreNaNs, int axis)} with ignoreNaNs = false
 	 * @param axis
 	 * @return mean along axis in dataset
 	 */
 	@Override
 	public AbstractDataset mean(int axis) {
 		return mean(false, axis);
 	}
 
 	/**
 	 * @param ignoreNaNs if true, skip NaNs
 	 * @param axis
 	 * @return mean along axis in dataset
 	 */
 	@Override
 	public AbstractDataset mean(boolean ignoreNaNs, int axis) {
 		return (AbstractDataset) getStatistics(ignoreNaNs, axis, STORE_MEAN + "-" + axis);
 	}
 
 	/**
 	 * @return sample variance of whole dataset
 	 * @see #variance(boolean)
 	 */
 	@Override
 	public Number variance() {
 		return variance(false);
 	}
 
 	/**
 	 * The sample variance can be calculated in two ways: if the dataset is considered as the entire population then the
 	 * sample variance is simply the second central moment:
 	 * 
 	 * <pre>
 	 *    sum((x_i - m)^2)/N
 	 * where {x_i} are set of N population values and m is the mean
 	 *    m = sum(x_i)/N
 	 * </pre>
 	 * 
 	 * Otherwise, if the dataset is a set of samples (with replacement) from the population then
 	 * 
 	 * <pre>
 	 *    sum((x_i - m)^2)/(N-1)
 	 * where {x_i} are set of N sample values and m is the unbiased estimate of the mean
 	 *    m = sum(x_i)/N
 	 * </pre>
 	 * 
 	 * Note that the second definition is also the unbiased estimator of population variance.
 	 * 
 	 * @param isDatasetWholePopulation
 	 * @return sample variance
 	 */
 	@Override
 	public Number variance(boolean isDatasetWholePopulation) {
 		SummaryStatistics stats = getStatistics(false);
 
 		if (isDatasetWholePopulation) {
 			StorelessUnivariateStatistic oldVar = stats.getVarianceImpl();
 			stats.setVarianceImpl(new Variance(false));
 			Number var = stats.getVariance();
 			stats.setVarianceImpl(oldVar);
 			return var;
 		}
 		return stats.getVariance();
 	}
 
 	/**
 	 * @param axis
 	 * @return sample variance along axis in dataset
 	 * @see #variance(boolean)
 	 */
 	@Override
 	public AbstractDataset variance(int axis) {
 		return (AbstractDataset) getStatistics(false, axis, STORE_VAR + "-" + axis);
 	}
 
 	/**
 	 * Standard deviation is square root of the variance
 	 * 
 	 * @return sample standard deviation of all items in dataset
 	 * @see #variance()
 	 */
 	@Override
 	public Number stdDeviation() {
 		return Math.sqrt(variance().doubleValue());
 	}
 
 	/**
 	 * Standard deviation is square root of the variance
 	 * 
 	 * @param isDatasetWholePopulation
 	 * @return sample standard deviation of all items in dataset
 	 * @see #variance(boolean)
 	 */
 	@Override
 	public Number stdDeviation(boolean isDatasetWholePopulation) {
 		return Math.sqrt(variance(isDatasetWholePopulation).doubleValue());
 	}
 
 	/**
 	 * @param axis
 	 * @return standard deviation along axis in dataset
 	 */
 	@Override
 	public AbstractDataset stdDeviation(int axis) {
 		final AbstractDataset v = (AbstractDataset) getStatistics(false, axis, STORE_VAR + "-" + axis);
 		return Maths.sqrt(v);
 	}
 
 	/**
 	 * @return root mean square
 	 */
 	@Override
 	public Number rootMeanSquare() {
 		final SummaryStatistics stats = getStatistics(false);
 		final double mean = stats.getMean();
 		return Math.sqrt(stats.getVariance() + mean * mean);
 	}
 
 	/**
 	 * @param axis
 	 * @return root mean square along axis in dataset
 	 */
 	@Override
 	public AbstractDataset rootMeanSquare(int axis) {
 		AbstractDataset v = (AbstractDataset) getStatistics(false, axis, STORE_VAR + "-" + axis);
 		AbstractDataset m = (AbstractDataset) getStatistics(false, axis, STORE_MEAN + "-" + axis);
 		AbstractDataset result = Maths.power(m, 2);
 		return Maths.sqrt(result.iadd(v));
 	}
 
 	/**
 	 * @see DatasetUtils#put(AbstractDataset, int[], Object[])
 	 */
 	@Override
 	public AbstractDataset put(final int[] indices, Object[] values) {
 		return DatasetUtils.put(this, indices, values);
 	}
 
 	/**
 	 * @see DatasetUtils#take(AbstractDataset, int[], Integer)
 	 */
 	@Override
 	public AbstractDataset take(final int[] indices, final Integer axis) {
 		return DatasetUtils.take(this, indices, axis);
 	}
 
 	/**
 	 * Set item from compatible dataset in a direct and speedy way
 	 * 
 	 * @param dindex
 	 * @param sindex
 	 * @param src
 	 *            is the source data buffer
 	 */
 	protected abstract void setItemDirect(final int dindex, final int sindex, final Object src);
 
 	/*
 	 * Note that all error values are stored internally already squared to 
 	 * ease calculation time on error propagation.
 	 * 
 	 * It must be null, a Double, a double array, DoubleDataset or CompoundDoubleDataset
 	 */
 	protected Serializable errorData = 0;
 
 	/**
 	 * Set error for all points in the dataset
 	 * @param error can be a Number, a Complex, an array, a List or a dataset
 	 */
 	@Override
 	public void setError(Serializable error) {
 		final int is = getElementsPerItem();
 		double[] e = null;
 		if (is > 1) {
 			e = AbstractCompoundDataset.toDoubleArray(error, is);
 			if (e != null) {
 				if (e == error) {
 					e = e.clone();
 				}
 				for (int i = 0; i < is; i++) {
 					double x = e[i];
 					e[i] = x*x;
 				}
 				errorData = e;
 			} else if (error instanceof IDataset) {
 				AbstractDataset x = DatasetUtils.convertToAbstractDataset((IDataset) error);
 				if (!isCompatibleWith(x)) {
 					throw new IllegalArgumentException("Error dataset is incompatible with this dataset");
 				}
 				if (x instanceof AbstractCompoundDataset) {
 					int isize = x.getElementsPerItem();
 					if (isize != is && isize != 1) {
 						throw new IllegalArgumentException("Error dataset has incompatible number of elements with this dataset");
 					}
 					x = x.cast(ARRAYFLOAT64);
 				} else {
 					x = x.cast(FLOAT64);
 				}
 				errorData = Maths.square(x);
 			} else {
 				throw new IllegalArgumentException("Type of error could not be handled");
 			}
 		} else if (error instanceof Number) {
 			double x = ((Number) error).doubleValue();
 			errorData = x*x;
 		} else if (error instanceof IDataset) {
 			AbstractDataset x = DatasetUtils.convertToAbstractDataset((IDataset) error);
 			if (!isCompatibleWith(x)) {
 				throw new IllegalArgumentException("Error dataset is incompatible with this dataset");
 			}
 			if (x instanceof AbstractCompoundDataset) {
 				if (x.getElementsPerItem() != 1) {
 					throw new IllegalArgumentException("Error dataset has incompatible number of elements with this dataset");
 				}
 				x = x.cast(ARRAYFLOAT64);
 			} else {
 				x = x.cast(FLOAT64);
 			}
 			errorData = Maths.square(x);
 		} else {
 			throw new IllegalArgumentException("Type of error could not be handled");
 		}
 	}
 
 	/**
 	 * In the case where errorData is defined this is faster than
 	 * calling getError() to determine if there is error data. For instance
 	 * in plotting it is required to know for every point if there is an error.
 	 * 
 	 * @return if there is an error.
 	 */
 	@Override
 	public boolean hasErrors() {
 		if (errorData != null) {
 			if (errorData instanceof Number) {
 				return ((Number) errorData).intValue() != 0;
 			}
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Get the error array from the dataset, or creates an error array if all 
 	 * values are the same
 	 * @return the dataset which contains the error information (can be null)
 	 */
 	@Override
 	public AbstractDataset getError() {
 		if (errorData == null) {
 			return null;
 		}
 
 		if (errorData instanceof AbstractDataset) {
 			return Maths.sqrt((AbstractDataset) errorData);
 		} else if (errorData instanceof ILazyDataset) {
 			errorData = DatasetUtils.convertToAbstractDataset((ILazyDataset) errorData);
 			return Maths.sqrt((AbstractDataset) errorData);
 		}
 
 		DoubleDataset errors = new DoubleDataset(shape);
 		errors.fill(Math.sqrt(toReal(errorData)));
 		return errors;
 	}
 
 	/**
 	 * Get the error value for a single point in the dataset
 	 * @param pos of the point to be referenced 
 	 * @return the value of the error at this point
 	 */
 	@Override
 	public double getError(int... pos) {
 		if (errorData == null) {
 			return 0;
 		}
 		if (errorData instanceof IDataset) {
 			return Math.sqrt(((IDataset) errorData).getDouble(pos));
 		}
 		return Math.sqrt(toReal(errorData));
 	}
 
 	@Override
 	public double[] getErrorArray(int... pos) {
 		if (errorData == null) {
 			return null;
 		}
 		return new double[] {getError(pos)};
 	}
 
 	@Override
 	public Serializable getErrorBuffer() {
 		return errorData;
 	}
 
 	protected IMetaData metadataStructure = null;
 
 	@Override
 	public void setMetadata(IMetaData metadata) {
 		metadataStructure = metadata;
 	}
 
 	@Override
 	public IMetaData getMetadata() {
 		return metadataStructure;
 	}
 }
