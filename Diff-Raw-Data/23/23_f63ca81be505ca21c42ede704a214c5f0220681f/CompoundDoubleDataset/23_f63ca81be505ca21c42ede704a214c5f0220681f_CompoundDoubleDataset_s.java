 /*
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
 
 // GEN_COMMENT
 
 package uk.ac.diamond.scisoft.analysis.dataset;
 
 import java.util.Arrays;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  *
  */
 public class CompoundDoubleDataset extends AbstractCompoundDataset {
 	// pin UID to base class
 	private static final long serialVersionUID = AbstractDataset.serialVersionUID;
 
 	/**
 	 * Setup the logging facilities
 	 */
 	transient private static final Logger compoundLogger = LoggerFactory.getLogger(CompoundDoubleDataset.class);
 
 	protected double[] data; // subclass alias // PRIM_TYPE
 
 	@Override
 	protected void setData() {
 		data = (double[]) odata; // PRIM_TYPE
 	}
 
 	protected double[] createArray(final int size) { // PRIM_TYPE
 		double[] array = null; // PRIM_TYPE
 
 		try {
 			array = new double[isize * size]; // PRIM_TYPE
 		} catch (OutOfMemoryError e) {
 			compoundLogger.error("The size of the dataset ({}) that is being created is too large "
 					+ "and there is not enough memory to hold it.", size);
 			throw new OutOfMemoryError("The dimensions given are too large, and there is "
 					+ "not enough memory available in the Java Virtual Machine");
 		}
 		return array;
 	}
 
 	@Override
 	public int getDtype() {
 		return ARRAYFLOAT64; // DATA_TYPE
 	}
 
 	public CompoundDoubleDataset() {
 	}
 
 	public CompoundDoubleDataset(final int itemSize) {
 		isize = itemSize;
 	}
 
 	public CompoundDoubleDataset(final int itemSize, final int[] shape) {
 		isize = itemSize;
 		if (shape.length == 1) {
 			size = shape[0];
 			this.shape = shape.clone();
 			if (size > 0) {
 				odata = data = createArray(size);
 			} else if (size < 0) {
 				throw new IllegalArgumentException("Negative component in shape is not allowed");
 			}
 		} else {
 			size = calcSize(shape);
 			this.shape = shape.clone();
 
 			odata = data = createArray(size);
 		}
 	}
 
 	/**
 	 * Copy a dataset
 	 * 
 	 * @param dataset
 	 */
 	public CompoundDoubleDataset(final CompoundDoubleDataset dataset) {
 		this(dataset, false);
 	}
 
 	/**
 	 * Copy a dataset or just wrap in a new reference (for Jython sub-classing)
 	 * 
 	 * @param dataset
 	 * @param wrap
 	 */
 	public CompoundDoubleDataset(final CompoundDoubleDataset dataset, final boolean wrap) {
 		isize = dataset.isize;
 		size = dataset.size;
 
 		if (wrap) {
 			shape = dataset.shape;
 			dataSize = dataset.dataSize;
 			dataShape = dataset.dataShape;
 			name = dataset.name;
 			metadata = dataset.metadata;
 			if(dataset.metadataStructure != null)
 				metadataStructure = dataset.metadataStructure;
 			odata = data = dataset.data;
 
 			return;
 		}
 
 		shape = dataset.shape.clone();
 		name = new String(dataset.name);
 		metadata = copyMetadataMap(dataset.metadata);
 		if(dataset.metadataStructure != null)
 			metadataStructure = dataset.metadataStructure.clone();
 		
 		double[] gdata = dataset.data; // PRIM_TYPE
 
 		if (dataset.isContiguous()) {
 			odata = data = gdata.clone();
 		} else {
 			odata = data = createArray(size);
 
 			IndexIterator diter = dataset.getIterator();
 			IndexIterator iter = getIterator();
 			while (iter.hasNext() && diter.hasNext()) {
 				for (int i = 0; i < isize; i++) {
 					data[iter.index + i] = gdata[diter.index + i];
 				}
 			}
 		}
 
 		errorValue = dataset.errorValue;
 		errorData = dataset.errorData;
 		errorArray = dataset.errorArray;
 		errorCompoundData = dataset.errorCompoundData;
 	}
 
 	/**
 	 * Create a dataset using given dataset
 	 * 
 	 * @param dataset
 	 */
 	public CompoundDoubleDataset(final AbstractCompoundDataset dataset) {
 		isize = dataset.isize;
 		size = dataset.size;
 		shape = dataset.shape.clone();
 
 		odata = data = createArray(size);
 
 		IndexIterator iter = dataset.getIterator();
 
 		for (int j = 0; iter.hasNext();) {
 			for (int i = 0; i < isize; i++) {
 				data[j++] = dataset.getElementDoubleAbs(iter.index + i); // GET_ELEMENT_WITH_CAST
 			}
 		}
 
 		errorValue = dataset.errorValue;
 		errorData = dataset.errorData;
 		errorArray = dataset.errorArray;
 		errorCompoundData = dataset.errorCompoundData;
 		metadataStructure = dataset.metadataStructure;
 	}
 
 	/**
 	 * Create a dataset using given data (elements are grouped together)
 	 * 
 	 * @param itemSize
 	 * @param data
 	 * @param shape
 	 *            (can be null to create 1D dataset)
 	 */
 	public CompoundDoubleDataset(final int itemSize, final double[] data, int... shape) { // PRIM_TYPE
 		isize = itemSize;
 		if (shape == null || shape.length == 0) {
 			shape = new int[] { data.length / isize };
 		}
 		size = calcSize(shape);
 		if (size * isize != data.length) {
 			throw new IllegalArgumentException(String.format("Shape %s is not compatible with size of data array, %d",
 					Arrays.toString(shape), data.length / isize));
 		}
 		this.shape = shape.clone();
 
 		odata = this.data = data;
 	}
 
 	/**
 	 * Create a dataset using given datasets
 	 * 
 	 * @param datasets
 	 */
 	public CompoundDoubleDataset(final AbstractDataset... datasets) {
 		if (datasets.length <= 1) {
 			throw new IllegalArgumentException("Array of datasets must have length greater than one");
 		}
 
 		for (int i = 1; i < datasets.length; i++)
 			datasets[0].checkCompatibility(datasets[i]);
 
 		isize = datasets.length;
 		size = calcSize(datasets[0].shape);
 		shape = datasets[0].shape.clone();
 
 		odata = data = createArray(size);
 
 		IndexIterator[] iters = new IndexIterator[isize];
 		for (int i = 0; i < datasets.length; i++)
 			iters[i] = datasets[i].getIterator();
 
 		for (int j = 0; iters[0].hasNext();) {
 			data[j++] = datasets[0].getElementDoubleAbs(iters[0].index); // GET_ELEMENT_WITH_CAST
 			for (int i = 1; i < datasets.length; i++) {
 				iters[i].hasNext();
 				data[j++] = datasets[i].getElementDoubleAbs(iters[i].index); // GET_ELEMENT_WITH_CAST
 			}
 		}
 	}
 
 	/**
 	 * Cast a dataset to this compound type. If repeat is set, the first element of each item in the given dataset is
 	 * repeated across all elements of an item. Otherwise, each item comprises a truncated or zero-padded copy of
 	 * elements from the given dataset.
 	 * 
 	 * @param itemSize
 	 * @param repeat
 	 *            repeat first element
 	 * @param dataset
 	 */
 	public CompoundDoubleDataset(final int itemSize, final boolean repeat, final AbstractDataset dataset) {
 		isize = itemSize;
 		size = dataset.size;
 		shape = dataset.shape.clone();
 		name = new String(dataset.name);
 
 		odata = data = createArray(size);
 		final int os = dataset.getElementsPerItem();
 
 		IndexIterator iter = dataset.getIterator();
 		if (repeat) {
 			int i = 0;
 			while (iter.hasNext()) {
 				final double v = dataset.getElementDoubleAbs(iter.index); // PRIM_TYPE // GET_ELEMENT_WITH_CAST
 				for (int k = 0; k < isize; k++)
 					data[i++] = v;
 			}
 		} else {
 			final int kmax = Math.min(isize, os);
 			int i = 0;
 			while (iter.hasNext()) {
 				for (int k = 0; k < kmax; k++)
 					data[i + k] = dataset.getElementDoubleAbs(iter.index + k); // GET_ELEMENT_WITH_CAST
 				i += isize;
 			}
 		}
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (!super.equals(obj)) {
 			return false;
 		}
 
 		if (getRank() == 0) // already true for scalar dataset
 			return true;
 
 		CompoundDoubleDataset other = (CompoundDoubleDataset) obj;
 		IndexIterator it = getIterator();
 		while (it.hasNext()) {
 			for (int j = 0; j < isize; j++) {
 				if (data[it.index+j] != other.data[it.index+j])
 					return false;
 			}
 		}
 		return true;
 	}
 
 	/**
 	 * Create a dataset from an object which could be a PySequence, a Java array (of arrays...) or Number. Ragged
 	 * sequences or arrays are padded with zeros.
 	 * 
 	 * @param obj
 	 * @return dataset with contents given by input
 	 */
 	public static CompoundDoubleDataset createFromObject(final Object obj) {
 		DoubleDataset result = DoubleDataset.createFromObject(obj); // CLASS_TYPE
 		return (CompoundDoubleDataset) DatasetUtils.createCompoundDatasetFromLastAxis(result, true);
 	}
 
 	/**
 	 * @param stop
 	 * @return a new 1D dataset, filled with values determined by parameters
 	 */
 	public static CompoundDoubleDataset arange(final int itemSize, final double stop) {
 		return arange(itemSize, 0., stop, 1.);
 	}
 
 	/**
 	 * @param start
 	 * @param stop
 	 * @param step
 	 * @return a new 1D dataset, filled with values determined by parameters
 	 */
 	public static CompoundDoubleDataset arange(final int itemSize, final double start, final double stop,
 			final double step) {
 		int size = calcSteps(start, stop, step);
 		CompoundDoubleDataset result = new CompoundDoubleDataset(itemSize, new int[] { size });
 		for (int i = 0; i < size; i++) {
 			result.data[i * result.isize] = (start + i * step); // PRIM_TYPE // ADD_CAST
 		}
 		return result;
 	}
 
 	/**
 	 * @param shape
 	 * @return a dataset filled with ones
 	 */
 	public static CompoundDoubleDataset ones(final int itemSize, final int... shape) {
 		return new CompoundDoubleDataset(itemSize, shape).fill(1);
 	}
 
 	/**
 	 * @param obj
 	 * @return dataset filled with given object
 	 */
 	@Override
 	public CompoundDoubleDataset fill(final Object obj) {
 		IndexIterator iter = getIterator();
 		double[] vr = toDoubleArray(obj, isize); // PRIM_TYPE // CLASS_TYPE
 
 		while (iter.hasNext()) {
 			for (int i = 0; i < isize; i++)
 				data[iter.index + i] = vr[i]; // PRIM_TYPE
 		}
 
 		return this;
 	}
 
 	/**
 	 * This is a typed version of {@link #getBuffer()}
 	 * @return data buffer as linear array
 	 */
 	public double[] getData() { // PRIM_TYPE
 		return data;
 	}
 
 	@Override
 	public CompoundDoubleDataset getView() {
 		CompoundDoubleDataset view = new CompoundDoubleDataset(isize);
 		view.name = new String(name);
 		view.size = size;
 		view.dataSize = dataSize;
 		view.shape = shape.clone();
 		if (dataShape != null)
 			view.dataShape = dataShape.clone();
 		view.odata = view.data = data;
 		view.metadata = metadata;
 		view.metadataStructure = metadataStructure;
 		return view;
 	}
 
 	/**
 	 * Set values at absolute index in the internal array. This is an internal method with no checks so can be
 	 * dangerous. Use with care or ideally with an iterator.
 	 * 
 	 * @param index
 	 *            absolute index
 	 * @return values
 	 */
 	public double[] getAbs(final int index) { // PRIM_TYPE
 		double[] result = new double[isize]; // PRIM_TYPE
 		for (int i = 0; i < isize; i++)
 			result[i] = data[index + i];
 		return result;
 	}
 
 	@Override
 	public boolean getElementBooleanAbs(final int index) {
 		for (int i = 0; i < isize; i++) {
 			if (data[index + i] == 0) {
 				return false;
 			}
 		}
 		return true;
 	}
 
 	@Override
 	public double getElementDoubleAbs(final int index) {
 		return data[index];
 	}
 
 	@Override
 	public long getElementLongAbs(final int index) {
 		return (long) data[index]; // OMIT_CAST_INT
 	}
 
 	@Override
 	protected void setItemDirect(final int dindex, final int sindex, final Object src) {
 		double[] dsrc = (double[]) src; // PRIM_TYPE
 		for (int i = 0; i < isize; i++)
 			data[dindex + i] = dsrc[sindex + i];
 	}
 
 	/**
 	 * Set values at absolute index in the internal array. This is an internal method with no checks so can be
 	 * dangerous. Use with care or ideally with an iterator.
 	 * 
 	 * @param index
 	 *            absolute index
 	 * @param val
 	 *            new values
 	 */
 	public void setAbs(final int index, final double[] val) { // PRIM_TYPE
 		for (int i = 0; i < isize; i++)
 			data[index + i] = val[i];
 		setDirty();
 	}
 
 	/**
 	 * Set element value at absolute index in the internal array. This is an internal method with no checks so can be
 	 * dangerous. Use with care or ideally with an iterator.
 	 * 
 	 * @param index
 	 *            absolute index
 	 * @param val
 	 *            new value
 	 */
 	public void setAbs(final int index, final double val) { // PRIM_TYPE
 		data[index] = val;
 		setDirty();
 	}
 
 	@Override
 	public Object getObject(final int... pos) {
 		return getDoubleArray(pos); // CLASS_TYPE
 	}
 
 	/**
 	 * @param pos
 	 * @return item in given position
 	 */
 	public double[] getDoubleArray(final int... pos) { // CLASS_TYPE // PRIM_TYPE
 		int n = isize * get1DIndex(pos);
 		return (double[]) getObjectAbs(n); // PRIM_TYPE
 	}
 
 	@Override
 	public void getDoubleArray(final double[] darray, final int... pos) {
 		int n = isize * get1DIndex(pos);
 		for (int i = 0; i < isize; i++)
 			darray[i] = data[n + i];
 	}
 
 	@Override
 	public String getString(final int... pos) {
 		int n = isize * get1DIndex(pos);
 		return getStringAbs(n);
 	}
 
 	@Override
 	protected double getFirstValue(final int... pos) {
 		int n = isize * get1DIndex(pos);
 		return data[n];
 	}
 
 	@Override
 	public Object getObjectAbs(final int index) {
 		double[] result = new double[isize]; // PRIM_TYPE
 		for (int i = 0; i < isize; i++)
 			result[i] = data[index + i];
 		return result;
 	}
 
 	@Override
 	public String getStringAbs(final int index) {
 		StringBuilder s = new StringBuilder();
 		s.append('(');
 		s.append(String.format("%.8g", data[index])); // FORMAT_STRING
 		for (int i = 1; i < isize; i++) {
 			s.append(' ');
 			s.append(String.format("%.8g", data[index + i])); // FORMAT_STRING
 		}
 		s.append(')');
 		return s.toString();
 	}
 
 	@Override
 	public void setObjectAbs(final int index, final Object obj) {
 		double[] oa = toDoubleArray(obj, isize); // PRIM_TYPE // CLASS_TYPE
 		setAbs(index, oa);
 	}
 
 	@Override
 	public void set(final Object obj, int... pos) {
 		if (pos == null || pos.length == 0) {
 			pos = new int[shape.length];
 		}
 
 		setItem(toDoubleArray(obj, isize), pos); // CLASS_TYPE
 	}
 
 	/**
 	 * Set values at given position
 	 * 
 	 * @param d
 	 * @param pos
 	 */
 	public void setItem(final double[] d, final int... pos) { // PRIM_TYPE
 		try {
 			// is pos valid?
 			//   N calc new shape
 			//     is new shape valid in reserved space?
 			//       Y set new shape
 			//       N allocate new space
 			//         move data to new space
 			//   set value
 			if (!isPositionInShape(pos)) {
 				int[] nshape = shape.clone();
 
 				for (int i = 0; i < pos.length; i++)
 					if (pos[i] >= nshape[i])
 						nshape[i] = pos[i] + 1;
 
 				allocateArray(nshape);
 			}
 			if (d.length > isize) {
 				throw new IllegalArgumentException("Array is larger than compound");
 			}
 			setAbs(isize * get1DIndex(pos), d);
 		} catch (ArrayIndexOutOfBoundsException e) {
 			throw new ArrayIndexOutOfBoundsException("Index out of bounds - need to make dataset extendible");
 		}
 	}
 
 	private void allocateArray(final int... nshape) {
 		if (data == null) {
 			throw new IllegalStateException("Data buffer in dataset is null");
 		}
 
 		if (dataShape != null) {
 			// see if reserved space is sufficient
 			if (isShapeInDataShape(nshape)) {
 				shape = nshape;
 				size = calcSize(shape);
 				if (Arrays.equals(shape, dataShape)) {
 					dataShape = null; // no reserved space
 				}
 				return;
 			}
 		}
 
 		final IndexIterator iter = getIterator();
 
 		// not enough room so need to expand the allocated memory
 		if (dataShape == null)
 			dataShape = shape.clone();
 		expandDataShape(nshape);
 		dataSize = calcSize(dataShape);
 
 		final double[] ndata = createArray(dataSize); // PRIM_TYPE
 		final int[] oshape = shape;
 
 		// now this object has the new dimensions so specify them correctly
 		shape = nshape;
 		size = calcSize(nshape);
 
 		// make sure that all the data is set to NaN, minimum value or false
 		Arrays.fill(ndata, Double.NaN); // CLASS_TYPE // DEFAULT_VAL
 
 		// now copy the data back to the correct positions
 		final IndexIterator niter = getSliceIterator(null, oshape, null);
 
 		while (niter.hasNext() && iter.hasNext())
 			for (int j = 0; j < isize; j++) {
 				ndata[niter.index + j] = data[iter.index + j];
 			}
 
 		odata = data = ndata;
 
 		// if fully expanded then reset the reserved space dimensions
 		if (dataSize == size) {
 			dataShape = null;
 		}
 	}
 
 	@Override
 	public void resize(int... newShape) {
 		IndexIterator iter = getIterator();
 		int nsize = calcSize(newShape);
 		double[] ndata = createArray(nsize); // PRIM_TYPE
 
 		int i = 0;
 		while (iter.hasNext() && i < nsize) {
 			for (int j = 0; j < isize; j++) {
 				ndata[i++] = data[iter.index + j];
 			}
 		}
 
 		odata = data = ndata;
 		size = nsize;
 		shape = newShape;
 		dataShape = null;
 		dataSize = size;
 	}
 
 	@Override
 	public DoubleDataset real() { // CLASS_TYPE
 		DoubleDataset rdataset = new DoubleDataset(shape); // CLASS_TYPE
 		IndexIterator iter = getIterator();
 		IndexIterator riter = rdataset.getIterator();
 
 		double[] rdata = rdataset.data; // PRIM_TYPE
 		while (iter.hasNext() && riter.hasNext())
 			rdata[riter.index] = data[iter.index];
 
 		return rdataset;
 	}
 
 	@Override
 	public CompoundDoubleDataset getSlice(final int[] start, final int[] stop, final int[] step) {
 		return getSlice((SliceIterator) getSliceIterator(start, stop, step));
 	}
 
 	@Override
 	public CompoundDoubleDataset getSlice(final SliceIterator siter) {
 		CompoundDoubleDataset result = new CompoundDoubleDataset(isize, siter.getSliceShape());
 		double[] rdata = result.data; // PRIM_TYPE
 		IndexIterator riter = result.getIterator();
 
 		while (siter.hasNext() && riter.hasNext()) {
 			for (int i = 0; i < isize; i++)
 				rdata[riter.index + i] = data[siter.index + i];
 		}
 
 		result.setName(name + ".slice");
 		return result;
 	}
 
 	@Override
 	public AbstractDataset getElements(int element) {
 		final DoubleDataset elements = new DoubleDataset(shape); // CLASS_TYPE
 
 		copyElements(elements, element);
 		return elements;
 	}
 
 	@Override
 	public void copyElements(AbstractDataset destination, int element) {
 		if (element < 0)
 			element += isize;
 		if (element < 0 || element > isize) {
 			throw new IllegalArgumentException(String.format("Invalid choice of element: %d/%d", element, isize));
 		}
 		if (elementClass() != destination.elementClass()) {
 			throw new IllegalArgumentException("Element class of destination does not match this dataset");
 		}
 
 		final IndexIterator it = getIterator(element);
 		final double[] elements = ((DoubleDataset) destination).data; // CLASS_TYPE // PRIM_TYPE
 
 		int n = 0;
 		while (it.hasNext()) {
 			elements[n] = data[it.index];
 			n++;
 		}
 	}
 
 	@Override
 	public void setElements(AbstractDataset source, int element) {
 		if (element < 0)
 			element += isize;
 		if (element < 0 || element > isize) {
 			throw new IllegalArgumentException(String.format("Invalid choice of element: %d/%d", element, isize));
 		}
 		if (elementClass() != source.elementClass()) {
 			throw new IllegalArgumentException("Element class of destination does not match this dataset");
 		}
 
 		final IndexIterator it = getIterator(element);
 		final double[] elements = ((DoubleDataset) source).data; // CLASS_TYPE // PRIM_TYPE
 
 		if (source.isContiguous()) {
 			int n = 0;
 			while (it.hasNext()) {
 				data[it.index] = elements[n];
 				n++;
 			}
 		} else {
 			final IndexIterator sit = source.getIterator();
 			while (it.hasNext() && sit.hasNext()) {
 				data[it.index] = elements[sit.index];
 			}
 		}
 	}
 
 	@Override
 	public void fillDataset(AbstractDataset result, IndexIterator iter) {
 		IndexIterator riter = result.getIterator();
 
 		double[] rdata = ((CompoundDoubleDataset) result).data; // PRIM_TYPE
 
 		while (riter.hasNext() && iter.hasNext()) {
 			for (int i = 0; i < isize; i++)
 				rdata[riter.index + i] = data[iter.index + i];
 		}
 	}
 
 	@Override
 	public CompoundDoubleDataset setByBoolean(final Object o, BooleanDataset selection) {
 		if (o instanceof AbstractDataset) {
 			AbstractDataset ds = (AbstractDataset) o;
 			final int length = ((Number) selection.sum()).intValue();
 			if (length != ds.getSize()) {
 				throw new IllegalArgumentException(
 						"Number of true items in selection does not match number of items in dataset");
 			}
 
 			IndexIterator iter = ds.getIterator();
 			BooleanIterator biter = getBooleanIterator(selection);
 
 			if (ds instanceof AbstractCompoundDataset) {
 				if (isize != ds.getElementsPerItem()) {
 					throw new IllegalArgumentException("Input dataset is not compatible with slice");
 				}
 
 				while (biter.hasNext() && iter.hasNext()) {
 					for (int i = 0; i < isize; i++)
 						data[biter.index + i] = ds.getElementDoubleAbs(iter.index + i); // GET_ELEMENT_WITH_CAST
 				}
 			} else {
 				while (biter.hasNext() && iter.hasNext()) {
 					data[biter.index] = ds.getElementDoubleAbs(iter.index); // GET_ELEMENT_WITH_CAST
 					for (int i = 1; i < isize; i++)
 						data[biter.index + i] = 0;
 				}
 			}
 		} else {
 			try {
 				final double[] vr = toDoubleArray(o, isize); // PRIM_TYPE // CLASS_TYPE
 
 				final BooleanIterator biter = getBooleanIterator(selection);
 
 				while (biter.hasNext()) {
 					for (int i = 0; i < isize; i++)
 						data[biter.index + i] = vr[i];
 				}
 			} catch (IllegalArgumentException e) {
 				throw new IllegalArgumentException("Object for setting is not a dataset or number");
 			}
 		}
 		return this;
 	}
 
 	@Override
 	public CompoundDoubleDataset setByIndex(final Object o, IntegerDataset index) {
 		if (o instanceof AbstractDataset) {
 			AbstractDataset ds = (AbstractDataset) o;
 			if (index.getSize() != ds.getSize()) {
 				throw new IllegalArgumentException(
 						"Number of true items in selection does not match number of items in dataset");
 			}
 
 			IndexIterator oiter = ds.getIterator();
 			final IntegerIterator iter = new IntegerIterator(index, size, isize);
 
 			if (ds instanceof AbstractCompoundDataset) {
 				if (isize != ds.getElementsPerItem()) {
 					throw new IllegalArgumentException("Input dataset is not compatible with slice");
 				}
 
 				while (iter.hasNext() && oiter.hasNext()) {
 					for (int i = 0; i < isize; i++)
 						data[iter.index + i] = ds.getElementDoubleAbs(oiter.index + i); // GET_ELEMENT_WITH_CAST
 				}
 			} else {
 				while (iter.hasNext() && oiter.hasNext()) {
 					data[iter.index] = ds.getElementDoubleAbs(oiter.index); // GET_ELEMENT_WITH_CAST
 					for (int i = 1; i < isize; i++)
 						data[iter.index + i] = 0;
 				}
 			}
 		} else {
 			try {
 				final double[] vr = toDoubleArray(o, isize); // PRIM_TYPE // CLASS_TYPE
 
 				final IntegerIterator iter = new IntegerIterator(index, size, isize);
 
 				while (iter.hasNext()) {
 					for (int i = 0; i < isize; i++)
 						data[iter.index + i] = vr[i];
 				}
 			} catch (IllegalArgumentException e) {
 				throw new IllegalArgumentException("Object for setting is not a dataset or number");
 			}
 		}
 		return this;
 	}
 
 	@Override
 	public CompoundDoubleDataset setSlice(final Object o, final SliceIterator siter) {
 		if (o instanceof IDataset) {
 			final IDataset ds = (IDataset) o;
 			final int[] oshape = ds.getShape();
 
 			if (!areShapesCompatible(siter.getSliceShape(), oshape)) {
 				throw new IllegalArgumentException(String.format(
 						"Input dataset is not compatible with slice: %s cf %s", Arrays.toString(oshape),
 						Arrays.toString(siter.getSliceShape())));
 			}
 
 			if (ds instanceof AbstractDataset) {
 				final AbstractDataset ads = (AbstractDataset) ds;
 				IndexIterator oiter = ads.getIterator();
 
 				if (ds instanceof AbstractCompoundDataset) {
 					if (isize != ads.getElementsPerItem()) {
 						throw new IllegalArgumentException("Input dataset is not compatible with slice");
 					}
 
 					while (siter.hasNext() && oiter.hasNext()) {
 						for (int i = 0; i < isize; i++)
 							data[siter.index + i] = ads.getElementDoubleAbs(oiter.index + i); // GET_ELEMENT_WITH_CAST
 					}
 				} else {
 					while (siter.hasNext() && oiter.hasNext()) {
 						data[siter.index] = ads.getElementDoubleAbs(oiter.index); // GET_ELEMENT_WITH_CAST
 						for (int i = 1; i < isize; i++)
 							data[siter.index + i] = 0;
 					}
 				}
 			} else {
 				final IndexIterator oiter = new PositionIterator(oshape);
 				final int[] pos = oiter.getPos();
 
 				if (ds.getElementsPerItem() == 1) {
 					while (siter.hasNext() && oiter.hasNext()) {
 						data[siter.index] = ds.getDouble(pos); // PRIM_TYPE
 						for (int i = 1; i < isize; i++)
 							data[siter.index + i] = 0;
 					}
 				} else {
 					while (siter.hasNext() && oiter.hasNext()) {
 						final double[] val = toDoubleArray(ds.getObject(pos), isize); // PRIM_TYPE // CLASS_TYPE
 						for (int i = 0; i < isize; i++)
 							data[siter.index + i] = val[i];
 					}
 				}
 			}
 		} else {
 			try {
 				final double[] vr = toDoubleArray(o, isize); // PRIM_TYPE // CLASS_TYPE
 
 				while (siter.hasNext()) {
 					for (int i = 0; i < isize; i++)
 						data[siter.index + i] = vr[i];
 				}
 			} catch (IllegalArgumentException e) {
 				throw new IllegalArgumentException("Object for setting slice is not a dataset or number");
 			}
 		}
 		setDirty();
 		return this;
 	}
 
 	@Override
 	public void copyItemsFromAxes(final int[] pos, final boolean[] axes, final AbstractDataset dest) {
 		double[] ddata = (double[]) dest.odata; // PRIM_TYPE
 
 		if (dest.getElementsPerItem() != isize) {
 			throw new IllegalArgumentException(String.format(
 					"Destination dataset is incompatible as it has %d elements per item not %d",
 					dest.getElementsPerItem(), isize));
 		}
 
 		SliceIterator siter = getSliceIteratorFromAxes(pos, axes);
 		int[] sshape = squeezeShape(siter.getSliceShape(), false);
 
 		IndexIterator diter = dest.getSliceIterator(null, sshape, null);
 
 		if (ddata.length < calcSize(sshape)) {
 			throw new IllegalArgumentException("destination array is not large enough");
 		}
 
 		while (siter.hasNext() && diter.hasNext()) {
 			for (int i = 0; i < isize; i++)
 				ddata[diter.index + i] = data[siter.index + i];
 		}
 	}
 
 	@Override
 	public void setItemsOnAxes(final int[] pos, final boolean[] axes, final Object src) {
 		double[] sdata = (double[]) src; // PRIM_TYPE
 
 		SliceIterator siter = getSliceIteratorFromAxes(pos, axes);
 
 		if (sdata.length < calcSize(siter.getSliceShape())) {
 			throw new IllegalArgumentException("source array is not large enough");
 		}
 
 		for (int i = 0; siter.hasNext(); i++) {
 			for (int j = 0; j < isize; j++)
 				data[siter.index + j] = sdata[isize * i + j];
 		}
 
 		setDirty();
 	}
 
 	/**
 	 * @return true if dataset contains any NaNs
 	 */
 	@Override
 	public boolean containsNans() {
 		final IndexIterator iter = getIterator(); // REAL_ONLY
 		while (iter.hasNext()) { // REAL_ONLY
 			for (int i = 0; i < isize; i++) { // REAL_ONLY
 				if (Double.isNaN(data[iter.index + i])) // CLASS_TYPE // REAL_ONLY
 					return true; // REAL_ONLY
 			} // REAL_ONLY
 		} // REAL_ONLY
 		return false;
 	}
 
 	/**
 	 * @return true if dataset contains any Infs
 	 */
 	@Override
 	public boolean containsInfs() {
 		final IndexIterator iter = getIterator(); // REAL_ONLY
 		while (iter.hasNext()) { // REAL_ONLY
 			for (int i = 0; i < isize; i++) { // REAL_ONLY
 				if (Double.isInfinite(data[iter.index + i])) // CLASS_TYPE // REAL_ONLY
 					return true; // REAL_ONLY
 			} // REAL_ONLY
 		} // REAL_ONLY
 		return false;
 	}
 
 	@Override
 	public boolean containsInvalidNumbers() {
 		IndexIterator iter = getIterator(); // REAL_ONLY
 		while (iter.hasNext()) { // REAL_ONLY
 			for (int i = 0; i < isize; i++) { // REAL_ONLY
				double x = data[iter.index + i]; // REAL_ONLY
 				if (Double.isNaN(x) || Double.isInfinite(x)) // CLASS_TYPE // REAL_ONLY
 					return true; // REAL_ONLY
 			} // REAL_ONLY
 		} // REAL_ONLY
 		return false;
 	}
 
 	@Override
 	public CompoundDoubleDataset iadd(final Object b) {
 		if (b instanceof AbstractDataset) {
 			final AbstractDataset bds = (AbstractDataset) b;
 			checkCompatibility(bds);
 
 			final IndexIterator it1 = getIterator();
 			final IndexIterator it2 = bds.getIterator();
 			final int bis = bds.getElementsPerItem();
 
 			if (bis == 1) {
 				while (it1.hasNext() && it2.hasNext()) {
 					final double db = bds.getElementDoubleAbs(it2.index); // PRIM_TYPE // GET_ELEMENT_WITH_CAST
 					for (int i = 0; i < isize; i++)
 						data[it1.index + i] += db;
 				}
 			} else if (bis == isize) {
 				while (it1.hasNext() && it2.hasNext()) {
 					for (int i = 0; i < isize; i++)
 						data[it1.index + i] += bds.getElementDoubleAbs(it2.index + i); // GET_ELEMENT
 				}
 			} else {
 				throw new IllegalArgumentException(
 						"Argument does not have same number of elements per item or is not a non-compound dataset");
 			}
 		} else {
 			final double[] vr = toDoubleArray(b, isize); // PRIM_TYPE // CLASS_TYPE
 			final IndexIterator it1 = getIterator();
 
 			while (it1.hasNext()) {
 				for (int i = 0; i < isize; i++)
 					data[it1.index + i] += vr[i];
 			}
 		}
 		setDirty();
 		return this;
 	}
 
 	@Override
 	public CompoundDoubleDataset isubtract(final Object b) {
 		if (b instanceof AbstractDataset) {
 			final AbstractDataset bds = (AbstractDataset) b;
 			checkCompatibility(bds);
 
 			final IndexIterator it1 = getIterator();
 			final IndexIterator it2 = bds.getIterator();
 			final int bis = bds.getElementsPerItem();
 
 			if (bis == 1) {
 				while (it1.hasNext() && it2.hasNext()) {
 					final double db = bds.getElementDoubleAbs(it2.index); // PRIM_TYPE // GET_ELEMENT_WITH_CAST
 					for (int i = 0; i < isize; i++)
 						data[it1.index + i] -= db;
 				}
 			} else if (bis == isize) {
 				while (it1.hasNext() && it2.hasNext()) {
 					for (int i = 0; i < isize; i++)
 						data[it1.index + i] -= bds.getElementDoubleAbs(it2.index + i); // GET_ELEMENT
 				}
 			} else {
 				throw new IllegalArgumentException(
 						"Argument does not have same number of elements per item or is not a non-compound dataset");
 			}
 		} else {
 			final double[] vr = toDoubleArray(b, isize); // PRIM_TYPE // CLASS_TYPE
 			final IndexIterator it1 = getIterator();
 
 			while (it1.hasNext()) {
 				for (int i = 0; i < isize; i++)
 					data[it1.index + i] -= vr[i];
 			}
 		}
 		setDirty();
 		return this;
 	}
 
 	@Override
 	public CompoundDoubleDataset imultiply(final Object b) {
 		if (b instanceof AbstractDataset) {
 			final AbstractDataset bds = (AbstractDataset) b;
 			checkCompatibility(bds);
 
 			final IndexIterator it1 = getIterator();
 			final IndexIterator it2 = bds.getIterator();
 			final int bis = bds.getElementsPerItem();
 
 			if (bis == 1) {
 				while (it1.hasNext() && it2.hasNext()) {
 					final double db = bds.getElementDoubleAbs(it2.index); // PRIM_TYPE // GET_ELEMENT_WITH_CAST
 					for (int i = 0; i < isize; i++)
 						data[it1.index + i] *= db;
 				}
 			} else if (bis == isize) {
 				while (it1.hasNext() && it2.hasNext()) {
 					for (int i = 0; i < isize; i++)
 						data[it1.index + i] *= bds.getElementDoubleAbs(it2.index + i); // GET_ELEMENT
 				}
 			} else {
 				throw new IllegalArgumentException(
 						"Argument does not have same number of elements per item or is not a non-compound dataset");
 			}
 		} else {
 			final double[] vr = toDoubleArray(b, isize); // PRIM_TYPE // CLASS_TYPE
 			final IndexIterator it1 = getIterator();
 
 			while (it1.hasNext()) {
 				for (int i = 0; i < isize; i++)
 					data[it1.index + i] *= vr[i];
 			}
 		}
 		setDirty();
 		return this;
 	}
 
 	@Override
 	public CompoundDoubleDataset idivide(final Object b) {
 		if (b instanceof AbstractDataset) {
 			final AbstractDataset bds = (AbstractDataset) b;
 			checkCompatibility(bds);
 
 			final IndexIterator it1 = getIterator();
 			final IndexIterator it2 = bds.getIterator();
 			final int bis = bds.getElementsPerItem();
 
 			if (bis == 1) {
 				while (it1.hasNext() && it2.hasNext()) {
 					final double db = bds.getElementDoubleAbs(it2.index); // PRIM_TYPE // GET_ELEMENT_WITH_CAST
 					// if (db == 0) { // INT_ZEROTEST
 					// for (int i = 0; i < isize; i++) // INT_ZEROTEST
 					// 	data[it1.index + i] = 0; // INT_ZEROTEST
 					// } else { // INT_ZEROTEST
 					for (int i = 0; i < isize; i++)
 						data[it1.index + i] /= db;
 					// } // INT_ZEROTEST
 				}
 			} else if (bis == isize) {
 				while (it1.hasNext() && it2.hasNext()) {
 					for (int i = 0; i < isize; i++) {
 						data[it1.index + i] /= bds.getElementDoubleAbs(it2.index + i); // GET_ELEMENT // INT_EXCEPTION
 					}
 				}
 			} else {
 				throw new IllegalArgumentException(
 						"Argument does not have same number of elements per item or is not a non-compound dataset");
 			}
 		} else {
 			final double[] vr = toDoubleArray(b, isize);
 			final IndexIterator it1 = getIterator();
 
 			while (it1.hasNext()) {
 				for (int i = 0; i < isize; i++) {
 					// if (vr[i] == 0) { // INT_ZEROTEST
 					// 	data[it1.index + i] = 0; // INT_ZEROTEST
 					// } else { // INT_ZEROTEST
 					data[it1.index + i] /= vr[i];
 					// } // INT_ZEROTEST
 				}
 			}
 		}
 		setDirty();
 		return this;
 	}
 
 	@Override
 	public CompoundDoubleDataset ifloor() {
 		final IndexIterator it1 = getIterator(); // REAL_ONLY
 		// REAL_ONLY
 		while (it1.hasNext()) { // REAL_ONLY
 			for (int i = 0; i < isize; i++) // REAL_ONLY
 				data[it1.index + i] = Math.floor(data[it1.index] + i); // REAL_ONLY // ADD_CAST
 		} // REAL_ONLY
 		setDirty(); // REAL_ONLY
 		return this;
 	}
 
 	@Override
 	public CompoundDoubleDataset iremainder(final Object b) {
 		if (b instanceof AbstractDataset) {
 			final AbstractDataset bds = (AbstractDataset) b;
 			checkCompatibility(bds);
 
 			final IndexIterator it1 = getIterator();
 			final IndexIterator it2 = bds.getIterator();
 			final int bis = bds.getElementsPerItem();
 
 			if (bis == 1) {
 				while (it1.hasNext() && it2.hasNext()) {
 					final double db = bds.getElementDoubleAbs(it2.index); // PRIM_TYPE // GET_ELEMENT_WITH_CAST
 					// if (db == 0) { // INT_ZEROTEST
 					// for (int i = 0; i < isize; i++) // INT_ZEROTEST
 					// 	data[it1.index + i] = 0; // INT_ZEROTEST
 					// } else { // INT_ZEROTEST
 					for (int i = 0; i < isize; i++)
 						data[it1.index + i] %= db;
 					// } // INT_ZEROTEST
 				}
 			} else if (bis == isize) {
 				while (it1.hasNext() && it2.hasNext()) {
 					for (int i = 0; i < isize; i++) {
 						data[it1.index + i] %= bds.getElementDoubleAbs(it2.index + i); // GET_ELEMENT // INT_EXCEPTION
 					}
 				}
 			} else {
 				throw new IllegalArgumentException(
 						"Argument does not have same number of elements per item or is not a non-compound dataset");
 			}
 		} else {
 			final double[] vr = toDoubleArray(b, isize);
 			final IndexIterator it1 = getIterator();
 
 			while (it1.hasNext()) {
 				for (int i = 0; i < isize; i++) {
 					// if (vr[i] == 0) { // INT_ZEROTEST
 					// 	data[it1.index + i] = 0; // INT_ZEROTEST
 					// } else { // INT_ZEROTEST
 					data[it1.index + i] %= vr[i];
 					// } // INT_ZEROTEST
 				}
 			}
 		}
 		setDirty();
 		return this;
 	}
 
 	@Override
 	public CompoundDoubleDataset ipower(final Object b) {
 		if (b instanceof AbstractDataset) {
 			final AbstractDataset bds = (AbstractDataset) b;
 			checkCompatibility(bds);
 
 			final IndexIterator it1 = getIterator();
 			final IndexIterator it2 = bds.getIterator();
 			final int bis = bds.getElementsPerItem();
 
 			if (bis == 1) {
 				while (it1.hasNext() && it2.hasNext()) {
 					final double db = bds.getElementDoubleAbs(it2.index); // PRIM_TYPE // GET_ELEMENT_WITH_CAST
 					for (int i = 0; i < isize; i++) {
 						final double v = Math.pow(data[it1.index + i], db);
 						// if (Double.isInfinite(v) || Double.isNaN(v)) { // INT_ZEROTEST
 						// 	data[it1.index + i] = 0; // INT_ZEROTEST // CLASS_TYPE
 						// } else { // INT_ZEROTEST
 						data[it1.index + i] = v; // PRIM_TYPE_LONG // BOOLEAN_OMIT // ADD_CAST
 						// } // INT_ZEROTEST
 					}
 				}
 			} else if (bis == isize) {
 				while (it1.hasNext() && it2.hasNext()) {
 					for (int i = 0; i < isize; i++) {
 						final double v = Math.pow(data[it1.index + i], bds.getElementDoubleAbs(it2.index + i));
 						// if (Double.isInfinite(v) || Double.isNaN(v)) { // INT_ZEROTEST
 						// 	data[it1.index + i] = 0; // INT_ZEROTEST // CLASS_TYPE
 						// } else { // INT_ZEROTEST
 						data[it1.index + i] = v; // PRIM_TYPE_LONG // ADD_CAST
 						// } // INT_ZEROTEST
 					}
 				}
 			} else {
 				throw new IllegalArgumentException(
 						"Argument does not have same number of elements per item or is not a non-compound dataset");
 			}
 		} else {
 			final double[] vr = toDoubleArray(b, isize); // PRIM_TYPE // CLASS_TYPE
 			final IndexIterator it1 = getIterator();
 
 			while (it1.hasNext()) {
 				for (int i = 0; i < isize; i++) {
 					final double v = Math.pow(data[it1.index + i], vr[i]);
 					// if (Double.isInfinite(v) || Double.isNaN(v)) { // INT_ZEROTEST
 					// 	data[it1.index + i] = 0; // INT_ZEROTEST // CLASS_TYPE
 					// } else { // INT_ZEROTEST
 					data[it1.index + i] = v; // PRIM_TYPE_LONG // ADD_CAST
 					// } // INT_ZEROTEST
 				}
 			}
 		}
 		setDirty();
 		return this;
 	}
 
 	@Override
 	public double residual(final Object b) {
 		double sum = 0;
 		if (b instanceof AbstractDataset) {
 			final AbstractDataset bds = (AbstractDataset) b;
 			checkCompatibility(bds);
 
 			final IndexIterator it1 = getIterator();
 			final IndexIterator it2 = bds.getIterator();
 			final int bis = bds.getElementsPerItem();
 
 			if (bis == 1) {
 				double comp = 0;
 				while (it1.hasNext() && it2.hasNext()) {
 					final double db = bds.getElementDoubleAbs(it2.index);
 					for (int i = 0; i < isize; i++) {
 						final double diff = data[it1.index + i] - db;
 						final double err = diff * diff - comp;
 						final double temp = sum + err;
 						comp = (temp - sum) - err;
 						sum = temp;
 					}
 				}
 			} else if (bis == isize) {
 				double comp = 0;
 				while (it1.hasNext() && it2.hasNext()) {
 					for (int i = 0; i < isize; i++) {
 						final double diff = data[it1.index + i] - bds.getElementDoubleAbs(it2.index + i);
 						final double err = diff * diff - comp;
 						final double temp = sum + err;
 						comp = (temp - sum) - err;
 						sum = temp;
 					}
 				}
 			} else {
 				throw new IllegalArgumentException(
 						"Argument does not have same number of elements per item or is not a non-compound dataset");
 			}
 		} else {
 			final double[] vr = toDoubleArray(b, isize);
 			final IndexIterator it1 = getIterator();
 
 			double comp = 0;
 			while (it1.hasNext()) {
 				for (int i = 0; i < isize; i++) {
 					final double diff = data[it1.index + i] - vr[i];
 					final double err = diff * diff - comp;
 					final double temp = sum + err;
 					comp = (temp - sum) - err;
 					sum = temp;
 				}
 			}
 		}
 		return sum;
 	}
 }
