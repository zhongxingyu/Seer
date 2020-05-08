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
 
 package uk.ac.diamond.scisoft.analysis.dataset;
 
 import java.lang.reflect.Field;
 import java.util.Arrays;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.diamond.scisoft.analysis.io.ILazyLoader;
 import uk.ac.diamond.scisoft.analysis.io.IMetaData;
 import uk.ac.diamond.scisoft.analysis.monitor.IMonitor;
 
 /**
  * Class that implements lazy dataset interface
  */
 public class LazyDataset implements ILazyDataset {
 	
 	
 	private static final long serialVersionUID = -903717887381144620L;
 	transient protected static final Logger logger = LoggerFactory.getLogger(LazyDataset.class);
 
 	protected String     name;
 	protected int[]      shape;
 	private int[]        oShape; // original shape
 	protected long       size;   // number of items
 	protected ILazyLoader loader;
 	private int          dtype;
 	private int          oOffset; // original shape offset
 	private int          nOffset; // current shape offset
 	private IMetaData    metadata = null;
 	protected ILazyDataset lazyErrorDelegate;
 
 	/**
 	 * Create a lazy dataset
 	 * @param name
 	 * @param dtype dataset type
 	 * @param shape
 	 * @param loader
 	 */
 	public LazyDataset(String name, int dtype, int[] shape, ILazyLoader loader) {
 		this.name = name;
 		this.shape = shape;
 		oShape = shape;
 		this.loader = loader;
 		this.dtype = dtype;
 		try {
 			size = AbstractDataset.calcLongSize(shape);
 		} catch (IllegalArgumentException e) {
 			size = Long.MAX_VALUE; // this indicates that the entire dataset cannot be read in! 
 		}
 		oOffset = -1;
 		nOffset = -1;
 		for (int i = 0; i < oShape.length; i++) {
 			if (oShape[i] != 1) {
 				oOffset = nOffset = i;
 				break;
 			}
 		}
 	}
 
 	@Override
 	public LazyDataset clone() {
 		LazyDataset ret = new LazyDataset(new String(name), dtype, shape.clone(), loader);
 		ret.lazyErrorDelegate = lazyErrorDelegate;
 		return ret;
 	}
 
 	@Override
 	public Class<?> elementClass() {
 		return AbstractDataset.elementClass(dtype);
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj) {
 			return true;
 		}
 		if (obj == null) {
 			return false;
 		}
 		if (!getClass().equals(obj.getClass())) {
 			return false;
 		}
 
 		LazyDataset other = (LazyDataset) obj;
 		if (dtype != other.dtype) {
 			return false;
 		}
 		if (!Arrays.equals(shape, other.shape)) {
 			return false;
 		}
 		return true;
 	}
 
 	@Override
 	public int hashCode() {
 		int hash = dtype;
 		int rank = shape.length;
 		for (int i = 0; i < rank; i++) {
 			hash = hash*17 + shape[i];
 		}
 		return hash;
 	}
 
 	/**
 	 * @return type of dataset item
 	 */
 	public int getDtype() {
 		return dtype;
 	}
 
 	@Override
 	public String getName() {
 		return name;
 	}
 
 	@Override
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	@Override
 	public int getSize() {
 		return (int) size;
 	}
 
 	@Override
 	public int[] getShape() {
 		return shape.clone();
 	}
 
 	@Override
 	public int getRank() {
 		return shape.length;
 	}
 
 	@Override
 	public void setShape(int... shape) {
 		setShapeInternal(shape);
 		if (lazyErrorDelegate!=null) {
 			lazyErrorDelegate.setShape(shape);
 		}
 	}
 
 	private void setShapeInternal(int... shape) {
 		long nsize = AbstractDataset.calcLongSize(shape);
 		if (nsize != size) {
 			throw new IllegalArgumentException("Size of new shape is not equal to current size");
 		}
 		if (nsize == 1) {
 			this.shape = shape.clone();
 			return;
 		}
 
 		int da = -1; // length of first non-one dimension
 		if (oOffset >= 0) {
 			da = oShape[oOffset];
 		}
 		assert da >= 0;
 
 		int jstop = -1; // index of last non-one dimension + 1
 		for (int i = oShape.length - 1; i >= oOffset; i--) {
 			if (oShape[i] != 1) {
 				jstop = i + 1;
 				break;
 			}
 		}
 
 		int i = 0;
 		for (; i < shape.length; i++) {
 			if (shape[i] == da) {
 				break;
 			}
 		}
 
 		int off = i;
 		if (shape.length - off < jstop - oOffset) {
 			throw new IllegalArgumentException("New shape not allowed - can only increase rank by prepending or postpending ones to old shape");
 		}
 		int j = oOffset;
 		for (; i < shape.length && j < jstop; i++, j++) {
 			if (shape[i] != oShape[j]) {
 				throw new IllegalArgumentException("New shape not allowed - can only increase rank by prepending or postpending ones to old shape");
 			}
 		}
 		nOffset = off;
 		this.shape = shape.clone();
 	}
 
 	@Override
 	public ILazyDataset squeeze() {
 		return squeeze(false);
 	}
 
 	@Override
 	public ILazyDataset squeeze(boolean onlyFromEnd) {
 		setShapeInternal(AbstractDataset.squeezeShape(shape, onlyFromEnd));
 		if (lazyErrorDelegate!=null) lazyErrorDelegate = lazyErrorDelegate.squeeze(onlyFromEnd);
 		return this;
 	}
 
 	@Override
 	public String toString() {
 		StringBuilder out = new StringBuilder();
 
 		if (name != null && name.length() > 0) {
 			out.append("Lazy dataset '");
 			out.append(name);
 			out.append("' has shape [");
 		} else {
 			out.append("Lazy dataset shape is [");
 		}
 		int rank = shape == null ? 0 : shape.length;
 
 		if (rank > 0 && shape[0] >= 0) {
 			out.append(shape[0]);
 		}
 		for (int i = 1; i < rank; i++) {
 			out.append(", " + shape[i]);
 		}
 		out.append(']');
 
 		return out.toString();
 	}
 
 	@Override
 	public IDataset getSlice(Slice... slice) {
 		final int rank = shape.length;
 		if (slice == null || slice.length == 0) {
 			return getSlice((int[]) null, null, null);
 		}
 		final int[] start = new int[rank];
 		final int[] stop = new int[rank];
 		final int[] step = new int[rank];
 		Slice.convertFromSlice(slice, shape, start, stop, step);
 		return getSlice(start, stop, step);
 	}
 
 	@Override
 	public IDataset getSlice(int[] start, int[] stop, int[] step) {
 		try {
 			return getSlice(null, start, stop, step);
 		} catch (Exception e) {
 			// do nothing
 		}
 
 		return null;
 	}
 
 	@Override
 	public IDataset getSlice(IMonitor monitor, Slice... slice) throws Exception {
 		final int rank = shape.length;
 		final int[] start = new int[rank];
 		final int[] stop = new int[rank];
 		final int[] step = new int[rank];
 		Slice.convertFromSlice(slice, shape, start, stop, step);
 		return getSlice(monitor, start, stop, step);
 	}
 
 	@Override
 	public IDataset getSlice(IMonitor monitor, int[] start, int[] stop, int[] step) throws Exception {
 		if (!loader.isFileReadable())
 			return null; // TODO add interaction to use plot server to load dataset
 
 		int rank = shape.length;
 		int[] lstart;
 		int[] lstop;
 		int[] lstep;
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
 			lstop = getShape();
 		} else {
 			lstop = stop;
 		}
 
 		int[] nshape = AbstractDataset.checkSlice(shape, start, stop, lstart, lstop, lstep);
 		int[] nstart;
 		int[] nstop;
 		int[] nstep;
 
 		int r = oShape.length;
 		if (r != shape.length || oOffset != nOffset) {
 			nstart = new int[r];
 			nstop = new int[r];
 			nstep = new int[r];
 			int i = 0;
 			for (; i < oOffset; i++) {
 				nstart[i] = 0;
 				nstop[i] = 1;
 				nstep[i] = 1;
 			}
 			int j = nOffset;
 			for (; i < r && j < shape.length; i++, j++) {
 				nstart[i] = lstart[j];
 				nstop[i] = lstop[j];
 				nstep[i] = lstep[j];
 			}
 			for (; i < r; i++) {
 				nstart[i] = 0;
 				nstop[i] = 1;
 				nstep[i] = 1;
 			}
 		} else {
 			nstart = start;
 			nstop = stop;
 			nstep = step;
 		}
 
 		IDataset a;
 		try {
 			a = loader.getDataset(monitor, oShape, nstart, nstop, nstep);
 			a.setShape(nshape);
 		} catch (Exception e) {
 			// return a fake dataset to show that this has not worked, should not be used in general though.
 			logger.debug("Problem getting {}: {}", String.format("slice %s %s %s", Arrays.toString(start), Arrays.toString(stop),
 							Arrays.toString(step)), e);
 			a = new DoubleDataset(1);
 		}
 		a.setName(name);
 		
 		if (a instanceof IErrorDataset) {
 			IErrorDataset ea = (IErrorDataset)a;
 			if (lazyErrorDelegate!=null) {
 				IDataset lazySlice = lazyErrorDelegate.getSlice(monitor, start, stop, step);
 				ea.setError(lazySlice);
 			} else {
 				ea.clearError();
 			}
 		}
 		return a;
 	}
 
 	@Override
 	public ILazyDataset getSliceView(Slice... slice) {
 		final int rank = shape.length;
 		if (slice == null || slice.length == 0) {
 			return getSlice((int[]) null, null, null);
 		}
 		final int[] start = new int[rank];
 		final int[] stop = new int[rank];
 		final int[] step = new int[rank];
 		Slice.convertFromSlice(slice, shape, start, stop, step);
 		ILazyDataset sliceView = getSliceView(start, stop, step);
 		if (lazyErrorDelegate!=null) {
 			ILazyDataset errorView = lazyErrorDelegate.getSliceView(start, stop, step);
 			sliceView.setLazyErrors(errorView);
 		}
 		return sliceView;
 	}
 
 	@Override
 	public ILazyDataset getSliceView(int[] start, int[] stop, int[] step) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 	
 	@Override
 	public void setMetadata(IMetaData metadata) {
 		this.metadata = metadata;
 	}
 
 	@Override
 	public IMetaData getMetadata() {
 		return metadata;
 	}
 
 	@Override
 	public void setLazyErrors(ILazyDataset errors) {
 		if (this==errors) return;
 		if (errors==null) {
 			lazyErrorDelegate = null;
 			return;
 		}
 		if (errors.getRank()!=getRank()) throw new RuntimeException("Rank of errors not correct. Should be "+getRank());
 		if (!Arrays.equals(getShape(), errors.getShape())) {
 			throw new RuntimeException("Shape of errors not correct. Should be "+Arrays.toString(getShape()));
 		}
 		this.lazyErrorDelegate = errors;
 	}
 
 	@Override
 	public ILazyDataset getLazyErrors() {
 		return lazyErrorDelegate;
 	}
 
 
 	/**
 	 * Gets the maximum size of a slice of a dataset in a given dimension
	 * which should normally fit in memory. Note that is might be possible
 	 * to get more in memory, this is a conservative estimate and seems to
 	 * almost always work at the size returned; providing Xmx is less than
 	 * the physical memory.
 	 * 
 	 * To get more in memory increase -Xmx setting or use an expression
 	 * which calls a rolling function (like rmean) instead of slicing directly
 	 * to memory.
 	 * 
 	 * @param lazySet
 	 * @param dimension
 	 * @return maximum size of dimension that can be sliced.
 	 */
 	public static int getMaxSliceLength(ILazyDataset lazySet, int dimension) {
 		
 		final double size = getSize(lazySet.elementClass());
 		final double max  = Runtime.getRuntime().maxMemory();
 		
         // Firstly if the whole dataset it likely to fit in memory, then we
 		// allow it.
 		final double space = max/lazySet.getSize();
 		
 		// If we have room for this whole dataset, then fine
 		if (space>=size) return lazySet.getShape()[dimension];
 		
 		// Otherwize estimate what we can fit in, conservatively
 		// First get size of one slice, see it that fits, if not, still return 1.
 		double sizeOneSlice = 1; // in bytes eventually
 		for (int dim = 0; dim < lazySet.getRank(); dim++) {
 			if (dim == dimension) continue;
 			sizeOneSlice*=lazySet.getShape()[dim];
 		}
 		sizeOneSlice*=size;// in bytes now
 		double avail = max/sizeOneSlice;
 		if (avail<1) return 1;
 		
 		int maxAllowed = (int)Math.floor(avail);
         return maxAllowed;
 	}
 
 	/**
 	 * Size in bytes of 1 of given type
 	 * @param elementClass
 	 * @return size
 	 */
 	private static int getSize(Class<?> elementClass) {
 		// If Number will usually have the SIZE attribute
 		try {
 			Field size = elementClass.getField("SIZE");
 			if (size!=null) return size.getInt(null);// static
 		} catch (Throwable ne) {
 			// Ignored
 		}
 		return 64;
 	}
 
 }
