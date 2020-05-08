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
 import java.util.List;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.diamond.scisoft.analysis.dataset.function.Centroid;
 
 /**
  * Utilities for manipulating datasets
  */
 public class DatasetUtils {
 
 	/**
 	 * Setup the logging facilities
 	 */
 	transient protected static final Logger utilsLogger = LoggerFactory.getLogger(DatasetUtils.class);
 
 	/**
 	 * Append copy of dataset with another dataset along n-th axis
 	 * 
 	 * @param a
 	 * @param b
 	 * @param axis
 	 *            number of axis (negative number counts from last)
 	 * @return appended dataset
 	 */
 	public static AbstractDataset append(IDataset a, IDataset b, int axis) {
 		final int[] shape = a.getShape();
 		final int rank = shape.length;
 		final int[] othdims = b.getShape();
 		if (rank != othdims.length) {
 			throw new IllegalArgumentException("Incompatible number of dimensions");
 		}
 		if (axis >= rank) {
 			throw new IllegalArgumentException("Axis specified exceeds array dimensions");
 		} else if (axis > -rank) {
 			if (axis < 0)
 				axis += rank;
 		} else {
 			throw new IllegalArgumentException("Axis specified is less than " + (-rank));
 		}
 
 		for (int i = 0; i < rank; i++) {
 			if (i != axis && shape[i] != othdims[i]) {
 				throw new IllegalArgumentException("Incompatible dimensions");
 			}
 		}
 		final int[] newdims = new int[rank];
 		for (int i = 0; i < rank; i++) {
 			newdims[i] = shape[i];
 		}
 		newdims[axis] += othdims[axis];
 		final int ot = AbstractDataset.getDType(b);
 		final int dt = AbstractDataset.getDType(a);
 		AbstractDataset ds = AbstractDataset.zeros(a.getElementsPerItem(), newdims, dt > ot ? dt : ot);
 		for (int l = 0, lmax = ds.getSize(); l < lmax; l++) {
 			int[] n = ds.getNDPosition(l);
 			boolean isold = true;
 			for (int m = 0; m < newdims.length; m++) {
 				if (n[m] >= shape[m]) { // check which array is loop passing through
 					isold = false;
 					n[m] -= shape[m];
 					break;
 				}
 			}
 			if (isold) {
 				ds.setObjectAbs(l, a.getObject(n));
 			} else {
 				ds.setObjectAbs(l, b.getObject(n));
 			}
 		}
 
 		return ds;
 	}
 
 	/**
 	 * Changes specific items of dataset by replacing them with other array
 	 * @param a
 	 * @param indices
 	 * @param values
 	 * @return changed dataset
 	 */
 	public static AbstractDataset put(final AbstractDataset a, final int[] indices, Object[] values) {
 		int ilen = indices.length;
 		int vlen = values.length;
 		for (int i = 0, v= 0; i < ilen; i++, v++) {
 			if (v >= vlen) v -= vlen;
 
 			a.setObjectAbs(indices[i], values[v]);
 		}
 		return a;
 	}
 
 	/**
 	 * Take items from dataset along an axis
 	 * @param indices
 	 * @param axis if null, then use flattened view
 	 * @return a sub-array
 	 */
 	public static AbstractDataset take(final AbstractDataset a, final int[] indices, Integer axis) {
 		if (indices == null || indices.length == 0) {
 			utilsLogger.error("No indices given");
 			throw new IllegalArgumentException("No indices given");
 		}
 		int[] ashape = a.getShape();
 		final int rank = ashape.length;
 		final int at = a.getDtype();
 		final int ilen = indices.length;
 		final int is = a.getElementsPerItem();
 
 		AbstractDataset result;
 		if (axis == null) {
 			ashape = new int[1];
 			ashape[0] = ilen;
 			result = AbstractDataset.zeros(is, ashape, at);
 			for (int i = 0; i < ilen; i++) {
 				result.setItemDirect(i, indices[i], a);
 			}
 		} else {
 			axis = a.checkAxis(axis);
 			ashape[axis] = ilen;
 			result = AbstractDataset.zeros(is, ashape, at);
 
 			int[] dpos = new int[rank];
 			int[] spos = new int[rank];
 			boolean[] axes = new boolean[rank];
 			Arrays.fill(axes, true);
 			axes[axis] = false;
 			Serializable src = a.getBuffer();
 			for (int i = 0; i < ilen; i++) {
 				spos[axis] = indices[i];
 				dpos[axis] = i;
 				SliceIterator siter = a.getSliceIteratorFromAxes(spos, axes);
 				SliceIterator diter = result.getSliceIteratorFromAxes(dpos, axes);
 
 				while (siter.hasNext() && diter.hasNext()) {
 					result.setItemDirect(diter.index, siter.index, src);
 				}
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * Construct a dataset that contains the original dataset repeated the number
 	 * of times in each axis given by corresponding entries in the reps array
 	 *
 	 * @param a
 	 * @param reps 
 	 * @return tiled dataset
 	 */
 	public static AbstractDataset tile(final IDataset a, int... reps) {
 		int[] shape = a.getShape();
 		int rank = shape.length;
 		final int rlen = reps.length;
 
 		// expand shape
 		if (rank < rlen) {
 			int[] newShape = new int[rlen];
 			int extraRank = rlen - rank;
 			for (int i = 0; i < extraRank; i++) {
 				newShape[i] = 1;
 			}
 			for (int i = 0; i < rank; i++) {
 				newShape[i+extraRank] = shape[i];
 			}
 
 			shape = newShape;
 			rank = rlen;
 		} else if (rank > rlen) {
 			int[] newReps = new int[rank];
 			int extraRank = rank - rlen;
 			for (int i = 0; i < extraRank; i++) {
 				newReps[i] = 1;
 			}
 			for (int i = 0; i < rlen; i++) {
 				newReps[i+extraRank] = reps[i];
 			}
 			reps = newReps;
 		}
 
 		// calculate new shape
 		int[] newShape = new int[rank];
 		for (int i = 0; i < rank; i++) {
 			newShape[i] = shape[i]*reps[i];
 		}
 
 		AbstractDataset tdata = AbstractDataset.zeros(a.getElementsPerItem(), newShape, AbstractDataset.getDType(a));
 
 		// decide which way to put slices
 		boolean manyColumns;
 		if (rank == 1)
 			manyColumns = true;
 		else
 			manyColumns = shape[rank-1] > 64;
 
 		if (manyColumns) {
 			// generate each start point and put a slice in
 			IndexIterator iter = tdata.getSliceIterator(null, null, shape);
 			SliceIterator siter = (SliceIterator) tdata.getSliceIterator(null, shape, null); 
 			final int[] pos = iter.getPos();
 			while (iter.hasNext()) {
 				siter.setStart(pos);
 				tdata.setSlice(a, siter);
 			}
 
 		} else {
 			// for each value, set slice given by repeats
 			final int[] skip = new int[rank];
 			for (int i = 0; i < rank; i++) {
 				if (reps[i] == 1) {
 					skip[i] = newShape[i];
 				} else {
 					skip[i] = shape[i];
 				}
 			}
 			
 			AbstractDataset aa = convertToAbstractDataset(a);
 			IndexIterator ita = aa.getIterator(true);
 			final int[] pos = ita.getPos();
 
 			final int[] sstart = new int[rank];
 			final int extra = rank - pos.length;
 			for (int i = 0; i < extra; i++) {
 				sstart[i] = 0;
 			}
 			SliceIterator siter = (SliceIterator) tdata.getSliceIterator(sstart, null, skip);
 			while (ita.hasNext()) {
 				for (int i = 0; i < pos.length; i++) {
 					sstart[i + extra] = pos[i];
 				}
 				siter.setStart(sstart);
 				tdata.setSlice(aa.getObjectAbs(ita.index), siter);
 			}
 		}
 
 		return tdata;
 	}
 
 	/**
 	 * Permute copy of dataset's axes so that given order is old order:
 	 * <pre>
 	 *  axisPerm = (p(0), p(1),...) => newdata(n(0), n(1),...) = olddata(o(0), o(1), ...)
 	 *  such that n(i) = o(p(i)) for all i
 	 * </pre>
 	 * I.e. for a 3D dataset (1,0,2) implies the new dataset has its 1st dimension
 	 * running along the old dataset's 2nd dimension and the new 2nd is the old 1st.
 	 * The 3rd dimension is left unchanged.
 	 * 
 	 * @param a
 	 * @param axes if null or zero length then axes order reversed
 	 * @return remapped copy of data
 	 */
 	public static AbstractDataset transpose(final IDataset a, int... axes) {
 		int[] shape = a.getShape();
 		int rank = shape.length;
 		if (rank == 1) {
 			return convertToAbstractDataset(a);
 		}
 
 		if (axes == null || axes.length == 0) {
 			axes = new int[rank];
 			for (int i = 0; i < rank; i++) {
 				axes[i] = rank-1-i;
 			}
 		}
 
 		int i;
 
 		if (axes.length != rank) {
 			utilsLogger.error("axis permutation has length {} that does not match dataset's rank {}", axes.length, rank);
 			throw new IllegalArgumentException("axis permutation does not match shape of dataset");
 		}
 
 		// check all permutation values are within bounds
 		for (int d : axes) {
 			if (d < 0 || d >= rank) {
 				utilsLogger.error("axis permutation contains element {} outside rank of dataset", d);
 				throw new IllegalArgumentException("axis permutation contains element outside rank of dataset");
 			}
 		}
 
 		// check for a valid permutation (is this an unnecessary restriction?)
 		int[] perm = axes.clone();
 		Arrays.sort(perm);
 		for (i = 0; i < rank; i++) {
 			if (perm[i] != i) {
 				utilsLogger.error("axis permutation is not valid: it does not contain complete set of axes");
 				throw new IllegalArgumentException("axis permutation does not contain complete set of axes");	
 			}
 		}
 
 		// check for an identity permutation
 		for (i = 0; i < rank; i++) {
 			if (axes[i] != i)
 				break;
 		}
 		if (i == rank) {
 			return convertToAbstractDataset(a.clone());
 		}
 
 		int[] nshape = new int[rank];
 		for (i = 0; i < rank; i++) {
 			nshape[i] = shape[axes[i]];
 		}
 
 		final AbstractDataset ta = AbstractDataset.zeros(a.getElementsPerItem(), nshape, AbstractDataset.getDType(a));
 
 		// generate each start point and put a slice in
 		int[] npos = new int[rank];
 		int[] opos = new int[rank];
 		for (i = 0; i < rank; i++) {
 			npos[i] = 0;
 			opos[i] = 0;
 		}
 		while (true) {
 			ta.set(a.getObject(opos), npos);
 
 			// now move on one position
 			int j = rank-1;
 			for (; j >= 0; j--) {
 				npos[j]++;
 				final int ax = axes[j];
 				opos[ax]++;
 				if (npos[j] >= nshape[j]) {
 					npos[j] = 0;
 					opos[ax] = 0;
 				} else {
 					break;
 				}
 			}
 			if (j == -1)
 				break;
 		}
 
 		AbstractDataset.copyStoredValues(a, ta, true);
 		return ta;
 	}
 
 	/**
 	 * Swap two axes in dataset
 	 * @param a
 	 * @param axis1
 	 * @param axis2
 	 * @return swapped dataset
 	 */
 	public static AbstractDataset swapAxes(final IDataset a, int axis1, int axis2) {
 		int[] shape = a.getShape();
 		int rank = shape.length;
 		if (axis1 < 0)
 			axis1 += rank;
 		if (axis2 < 0)
 			axis2 += rank;
 
 		if (axis1 < 0 || axis2 < 0 || axis1 >= rank || axis2 >= rank) {
 			utilsLogger.error("Axis value invalid - out of range");
 			throw new IllegalArgumentException("Axis value invalid - out of range");
 		}
 
 		if (rank == 1 || axis1 == axis2) {
 			return convertToAbstractDataset(a);
 		}
 
 		int[] axes = new int[rank];
 		for (int i = 0; i < rank; i++) {
 			axes[i] = i;
 		}		
 
 		axes[axis1] = axis2;
 		axes[axis2] = axis1;
 		return transpose(a, axes);
 	}
 
 	/**
 	 * @param a
 	 * @param axis to sort along
 	 * @return dataset sorted along axis
 	 */
 	public static AbstractDataset sort(final AbstractDataset a, final Integer axis) {
 		AbstractDataset s = a.clone();
 		return s.sort(axis);
 	}
 
 	/**
 	 * Concatenate the set of datasets along given axis
 	 * @param as
 	 * @param axis
 	 * @return concatenated dataset
 	 */
 	public static AbstractDataset concatenate(final IDataset[] as, final int axis) {
 		if (as == null || as.length == 0) {
 			utilsLogger.error("No datasets given");
 			throw new IllegalArgumentException("No datasets given");
 		}
 		IDataset a = as[0];
 		if (as.length == 1) {
 			return convertToAbstractDataset(a.clone());
 		}
 		int[] ashape = a.getShape();
 		int at = AbstractDataset.getDType(a);
 		int anum = as.length;
 		int isize = a.getElementsPerItem();
 
 		int i = 1;
 		for (; i < anum; i++) {
 			if (at != AbstractDataset.getDType(as[i])) {
 				utilsLogger.error("Datasets are not of same type");
 				break;
 			}
 			if (!AbstractDataset.areShapesCompatible(ashape, as[i].getShape(), axis)) {
 				utilsLogger.error("Datasets' shapes are not equal");
 				break;
 			}
 			final int is = as[i].getElementsPerItem();
 			if (isize < is)
 				isize = is;
 		}
 		if (i < anum) {
 			utilsLogger.error("Dataset are not compatible");
 			throw new IllegalArgumentException("Datasets are not compatible");
 		}
 
 		for (i = 1; i < anum; i++) {
 			ashape[axis] += as[i].getShape()[axis];
 		}
 
 		AbstractDataset result = AbstractDataset.zeros(isize, ashape, at);
 
 		int[] start = new int[ashape.length];
 		int[] stop = ashape;
 		stop[axis] = 0;
 		for (i = 0; i < anum; i++) {
 			IDataset b = as[i];
 			int[] bshape = b.getShape();
 			stop[axis] += bshape[axis];
 			result.setSlice(b, start, stop, null);
 			start[axis] += bshape[axis];
 		}
 
 		return result;
 	}
 
 	/**
 	 * Split a dataset into equal sections along given axis
 	 * @param a
 	 * @param sections
 	 * @param axis
 	 * @param checkEqual makes sure the division is into equal parts
 	 * @return list of split datasets
 	 */
 	public static List<AbstractDataset> split(final AbstractDataset a, int sections, final int axis, final boolean checkEqual) {
 		int[] ashape = a.shape;
 		int rank = ashape.length;
 		if (axis > rank) {
 			utilsLogger.error("Axis exceeds rank of dataset");
 			throw new IllegalArgumentException("Axis exceeds rank of dataset");
 		}
 		int imax = ashape[axis];
 		if (checkEqual && (imax%sections) != 0) {
 			utilsLogger.error("Number of sections does not divide axis into equal parts");
 			throw new IllegalArgumentException("Number of sections does not divide axis into equal parts");
 		}
 		int n = (imax + sections - 1) / sections;
 		int[] indices = new int[sections-1];
 		for (int i = 1; i < sections; i++)
 			indices[i-1] = n*i;
 		return split(a, indices, axis);
 	}
 
 	/**
 	 * Split a dataset into parts along given axis
 	 * @param a
 	 * @param indices
 	 * @param axis
 	 * @return list of split datasets
 	 */
 	public static List<AbstractDataset> split(final AbstractDataset a, int[] indices, final int axis) {
 		final int[] ashape = a.shape;
 		final int rank = ashape.length;
 		if (axis > rank) {
 			utilsLogger.error("Axis exceeds rank of dataset");
 			throw new IllegalArgumentException("Axis exceeds rank of dataset");
 		}
 		final int imax = ashape[axis];
 
 		final List<AbstractDataset> result = new ArrayList<AbstractDataset>();
 
 		final int[] nshape = ashape.clone();
 		final int is = a.getElementsPerItem();
 
 		int oind = 0;
 		final int[] start = new int[rank];
 		final int[] stop = new int[rank];
 		final int[] step = new int[rank];
 		for (int i = 0; i < rank; i++) {
 			start[i] = 0;
 			stop[i] = ashape[i];
 			step[i] = 1;
 		}
 		for (int ind : indices) {
 			if (ind > imax) {
 				result.add(AbstractDataset.zeros(is, new int[] {0}, a.getDtype()));
 			} else {
 				nshape[axis] = ind - oind;
 				start[axis] = oind;
 				stop[axis] = ind;
 				AbstractDataset n = AbstractDataset.zeros(is, nshape, a.getDtype());
 				IndexIterator iter = a.getSliceIterator(start, stop, step);
 
 				a.fillDataset(n, iter);
 				result.add(n);
 				oind = ind;
 			}
 		}
 
 		if (imax > oind) {
 			nshape[axis] = imax - oind;
 			start[axis] = oind;
 			stop[axis] = imax;
 			AbstractDataset n = AbstractDataset.zeros(is, nshape, a.getDtype());
 			IndexIterator iter = a.getSliceIterator(start, stop, step);
 
 			a.fillDataset(n, iter);
 			result.add(n);
 		}
 
 		return result;
 	}
 
 	/**
 	 * Constructs a dataset which has its elements along an axis replicated from
 	 * the original dataset by the number of times given in the repeats array.
 	 * 
 	 * By default, axis=-1 implies using a flattened version of the input dataset 
 	 *
 	 * @param a
 	 * @param repeats 
 	 * @param axis
 	 * @return dataset
 	 */
 	public static AbstractDataset repeat(AbstractDataset a, int[] repeats, int axis) {
 		Serializable buf = a.getBuffer();
 		int[] shape = a.getShape();
 		int rank = shape.length;
 		final int is = a.getElementsPerItem();
 
 		if (axis >= rank) {
 			utilsLogger.warn("Axis value is out of bounds");
 			throw new IllegalArgumentException("Axis value is out of bounds");
 		}
 
 		int alen;
 		if (axis < 0) {
 			alen = a.size;
 			axis = 0;
 			rank = 1;
 			shape[0] = alen;
 		} else {
 			alen = shape[axis];
 		}
 		int rlen = repeats.length;
 		if (rlen != 1 && rlen != alen) {
 			utilsLogger.warn("Repeats array should have length of 1 or match chosen axis");
 			throw new IllegalArgumentException("Repeats array should have length of 1 or match chosen axis");
 		}
 
 		for (int i = 0; i < rlen; i++) {
 			if (repeats[i] < 0) {
 				utilsLogger.warn("Negative repeat value is not allowed");
 				throw new IllegalArgumentException("Negative repeat value is not allowed");
 			}
 		}
 
 		int[] newShape = new int[rank];
 		for (int i = 0; i < rank; i ++)
 			newShape[i] = shape[i];
 
 		// do single repeat separately
 		if (repeats.length == 1) {
 			newShape[axis] *= repeats[0];
 		} else {
 			int nlen = 0;
 			for (int i = 0; i < alen; i++) {
 				nlen += repeats[i];
 			}
 			newShape[axis] = nlen;
 		}
 
 		AbstractDataset rdata = AbstractDataset.zeros(is, newShape, a.getDtype());
 		Serializable nbuf = rdata.getBuffer();
 
 		int csize = is; // chunk size
 		for (int i = axis+1; i < rank; i++) {
 			csize *= newShape[i];
 		}
 		int nout = 1;
 		for (int i = 0; i < axis; i++) {
 			nout *= newShape[i];
 		}
 
 		int oi = 0;
 		int ni = 0;
 		if (rlen == 1) { // do single repeat separately
 			for (int i = 0; i < nout; i++) {
 				for (int j = 0; j < shape[axis]; j++) {
 					for (int k = 0; k < repeats[0]; k++) {
 						System.arraycopy(buf, oi, nbuf, ni, csize);
 						ni += csize;
 					}
 					oi += csize;
 				}
 			}
 		} else {
 			for (int i = 0; i < nout; i++) {
 				for (int j = 0; j < shape[axis]; j++) {
 					for (int k = 0; k < repeats[j]; k++) {
 						System.arraycopy(buf, oi, nbuf, ni, csize);
 						ni += csize;
 					}
 					oi += csize;
 				}
 			}
 		}
 
 		return rdata;
 	}
 
 	/**
 	 * Cast a dataset
 	 * 
 	 * @param a
 	 *            The dataset to be cast.
 	 * @param dtype dataset type
 	 */
 	public static AbstractDataset cast(final AbstractDataset a, final int dtype) {
 		if (a.getDtype() == dtype) {
 			return a;
 		}
 
 		AbstractDataset c = null;
 
 		try {
 			// copy across the data
 			switch (dtype) {
 			case AbstractDataset.BOOL:
 				c = new BooleanDataset(a);
 				break;
 			case AbstractDataset.INT8:
 				if (a instanceof AbstractCompoundDataset)
 					c = new CompoundByteDataset(a);
 				else
 					c = new ByteDataset(a);
 				break;
 			case AbstractDataset.INT16:
 				if (a instanceof AbstractCompoundDataset)
 					c = new CompoundShortDataset(a);
 				else
 					c = new ShortDataset(a);
 				break;
 			case AbstractDataset.INT32:
 				if (a instanceof AbstractCompoundDataset)
 					c = new CompoundIntegerDataset(a);
 				else
 					c = new IntegerDataset(a);
 				break;
 			case AbstractDataset.INT64:
 				if (a instanceof AbstractCompoundDataset)
 					c = new CompoundLongDataset(a);
 				else
 					c = new LongDataset(a);
 				break;
 			case AbstractDataset.ARRAYINT8:
 				if (a instanceof AbstractCompoundDataset)
 					c = new CompoundByteDataset((AbstractCompoundDataset) a);
 				else
 					c = new CompoundByteDataset(a);
 				break;
 			case AbstractDataset.ARRAYINT16:
 				if (a instanceof AbstractCompoundDataset)
 					c = new CompoundShortDataset((AbstractCompoundDataset) a);
 				else
 					c = new CompoundShortDataset(a);
 				break;
 			case AbstractDataset.ARRAYINT32:
 				if (a instanceof AbstractCompoundDataset)
 					c = new CompoundIntegerDataset((AbstractCompoundDataset) a);
 				else
 					c = new CompoundIntegerDataset(a);
 				break;
 			case AbstractDataset.ARRAYINT64:
 				if (a instanceof AbstractCompoundDataset)
 					c = new CompoundLongDataset((AbstractCompoundDataset) a);
 				else
 					c = new CompoundLongDataset(a);
 				break;
 			case AbstractDataset.FLOAT32:
 				c = new FloatDataset(a);
 				break;
 			case AbstractDataset.FLOAT64:
 				c = new DoubleDataset(a);
 				break;
 			case AbstractDataset.ARRAYFLOAT32:
 				if (a instanceof AbstractCompoundDataset)
 					c = new CompoundFloatDataset((AbstractCompoundDataset) a);
 				else
 					c = new CompoundFloatDataset(a);
 				break;
 			case AbstractDataset.ARRAYFLOAT64:
 				if (a instanceof AbstractCompoundDataset)
 					c = new CompoundDoubleDataset((AbstractCompoundDataset) a);
 				else
 					c = new CompoundDoubleDataset(a);
 				break;
 			case AbstractDataset.COMPLEX64:
 				c = new ComplexFloatDataset(a);
 				break;
 			case AbstractDataset.COMPLEX128:
 				c = new ComplexDoubleDataset(a);
 				break;
 			default:
 				utilsLogger.error("Dataset of unknown type!");
 				break;
 			}
 		} catch (OutOfMemoryError e) {
 			utilsLogger.error("Not enough memory available to create dataset");
 			throw new OutOfMemoryError("Not enough memory available to create dataset");
 		}
 
 		return c;
 	}
 
 	/**
 	 * Cast a dataset
 	 * 
 	 * @param a
 	 *            The dataset to be cast.
 	 * @param repeat repeat elements over item
 	 * @param dtype dataset type
 	 * @param isize item size
 	 */
 	public static AbstractDataset cast(final AbstractDataset a, final boolean repeat, final int dtype, final int isize) {
 		if (a.getDtype() == dtype && a.getElementsPerItem() == isize) {
 			return a;
 		}
 		if (isize <= 0) {
 			utilsLogger.error("Item size is invalid (>0)");
 			throw new IllegalArgumentException("Item size is invalid (>0)");
 		}
 		if (isize > 1 && dtype <= AbstractDataset.FLOAT64) {
 			utilsLogger.error("Item size is inconsistent with dataset type");
 			throw new IllegalArgumentException("Item size is inconsistent with dataset type");
 		}
 
 		AbstractDataset c = null;
 
 		try {
 			// copy across the data
 			switch (dtype) {
 			case AbstractDataset.BOOL:
 				c = new BooleanDataset(a);
 				break;
 			case AbstractDataset.INT8:
 				c = new ByteDataset(a);
 				break;
 			case AbstractDataset.INT16:
 				c = new ShortDataset(a);
 				break;
 			case AbstractDataset.INT32:
 				c = new IntegerDataset(a);
 				break;
 			case AbstractDataset.INT64:
 				c = new LongDataset(a);
 				break;
 			case AbstractDataset.ARRAYINT8:
 				c = new CompoundByteDataset(isize, repeat, a);
 				break;
 			case AbstractDataset.ARRAYINT16:
 				c = new CompoundShortDataset(isize, repeat, a);
 				break;
 			case AbstractDataset.ARRAYINT32:
 				c = new CompoundIntegerDataset(isize, repeat, a);
 				break;
 			case AbstractDataset.ARRAYINT64:
 				c = new CompoundLongDataset(isize, repeat, a);
 				break;
 			case AbstractDataset.FLOAT32:
 				c = new FloatDataset(a);
 				break;
 			case AbstractDataset.FLOAT64:
 				c = new DoubleDataset(a);
 				break;
 			case AbstractDataset.ARRAYFLOAT32:
 				c = new CompoundFloatDataset(isize, repeat, a);
 				break;
 			case AbstractDataset.ARRAYFLOAT64:
 				c = new CompoundDoubleDataset(isize, repeat, a);
 				break;
 			case AbstractDataset.COMPLEX64:
 				c = new ComplexFloatDataset(a);
 				break;
 			case AbstractDataset.COMPLEX128:
 				c = new ComplexDoubleDataset(a);
 				break;
 			default:
 				utilsLogger.error("Dataset of unknown type!");
 				break;
 			}
 		} catch (OutOfMemoryError e) {
 			utilsLogger.error("Not enough memory available to create dataset");
 			throw new OutOfMemoryError("Not enough memory available to create dataset");
 		}
 
 		return c;
 	}
 
 	/**
 	 * Cast array of datasets to a compound dataset
 	 * 
 	 * @param a
 	 *            The datasets to be cast.
 	 */
 	public static AbstractCompoundDataset cast(final AbstractDataset[] a, final int dtype) {
 		AbstractCompoundDataset c = null;
 
 		switch (dtype) {
 		case AbstractDataset.INT8:
 		case AbstractDataset.ARRAYINT8:
 			c = new CompoundByteDataset(a);
 			break;
 		case AbstractDataset.INT16:
 		case AbstractDataset.ARRAYINT16:
 			c = new CompoundShortDataset(a);
 			break;
 		case AbstractDataset.INT32:
 		case AbstractDataset.ARRAYINT32:
 			c = new CompoundIntegerDataset(a);
 			break;
 		case AbstractDataset.INT64:
 		case AbstractDataset.ARRAYINT64:
 			c = new CompoundLongDataset(a);
 			break;
 		case AbstractDataset.FLOAT32:
 		case AbstractDataset.ARRAYFLOAT32:
 			c = new CompoundFloatDataset(a);
 			break;
 		case AbstractDataset.FLOAT64:
 		case AbstractDataset.ARRAYFLOAT64:
 			c = new CompoundDoubleDataset(a);
 			break;
 		case AbstractDataset.COMPLEX64:
 			if (a.length != 2) {
 				throw new IllegalArgumentException("Need two datasets for complex dataset type");
 			}
 			c = new ComplexFloatDataset(a[0], a[1]);
 			break;
 		case AbstractDataset.COMPLEX128:
 			if (a.length != 2) {
 				throw new IllegalArgumentException("Need two datasets for complex dataset type");
 			}
 			c = new ComplexDoubleDataset(a[0], a[1]);
 			break;
 		default:
 			utilsLogger.error("Dataset of unsupported type!");
 			break;
 		}
 
 		return c;
 	}
 
 	/**
 	 * Unwrap dataset elements so that all elements are unsigned
 	 * @param a dataset
 	 * @param bitWidth width of original primitive in bits
 	 */
 	public static void unwrapUnsigned(AbstractDataset a, final int bitWidth) {
 		final int dtype = a.getDtype();
 		final double dv = 1L << bitWidth;
 		final int isize = a.getElementsPerItem();
 		IndexIterator it = a.getIterator();
 
 		switch (dtype) {
 		case AbstractDataset.BOOL:
 			break;
 		case AbstractDataset.INT8:
 			break;
 		case AbstractDataset.INT16:
 			ShortDataset sds = (ShortDataset) a;
 			final short soffset = (short) dv;
 			while (it.hasNext()) {
 				final short x = sds.getAbs(it.index);
 				if (x < 0)
 					sds.setAbs(it.index, (short) (x + soffset));
 			}
 			break;
 		case AbstractDataset.INT32:
 			IntegerDataset ids = (IntegerDataset) a;
 			final int ioffset = (int) dv;
 			while (it.hasNext()) {
 				final int x = ids.getAbs(it.index);
 				if (x < 0)
 					ids.setAbs(it.index, x + ioffset);
 			}
 			break;
 		case AbstractDataset.INT64:
 			LongDataset lds = (LongDataset) a;
 			final long loffset = (long) dv;
 			while (it.hasNext()) {
 				final long x = lds.getAbs(it.index);
 				if (x < 0)
 					lds.setAbs(it.index, x + loffset);
 			}
 			break;
 		case AbstractDataset.FLOAT32:
 			FloatDataset fds = (FloatDataset) a;
 			final float foffset = (float) dv;
 			while (it.hasNext()) {
 				final float x = fds.getAbs(it.index);
 				if (x < 0)
 					fds.setAbs(it.index, x + foffset);
 			}
 			break;
 		case AbstractDataset.FLOAT64:
 			DoubleDataset dds = (DoubleDataset) a;
 			final double doffset = dv;
 			while (it.hasNext()) {
 				final double x = dds.getAbs(it.index);
 				if (x < 0)
 					dds.setAbs(it.index, x + doffset);
 			}
 			break;
 		case AbstractDataset.ARRAYINT8:
 			break;
 		case AbstractDataset.ARRAYINT16:
 			CompoundShortDataset csds = (CompoundShortDataset) a;
 			final short csoffset = (short) dv;
 			final short[] csa = new short[isize];
 			while (it.hasNext()) {
 				csds.getAbs(it.index, csa);
 				boolean dirty = false;
 				for (int i = 0; i < isize; i++) {
 					short x = csa[i];
 					if (x < 0) {
 						csa[i] = (short) (x + csoffset);
 						dirty = true;
 					}
 				}
 				if (dirty)
 					csds.setAbs(it.index, csa);
 			}
 			break;
 		case AbstractDataset.ARRAYINT32:
 			CompoundIntegerDataset cids = (CompoundIntegerDataset) a;
 			final int cioffset = (int) dv;
 			final int[] cia = new int[isize];
 			while (it.hasNext()) {
 				cids.getAbs(it.index, cia);
 				boolean dirty = false;
 				for (int i = 0; i < isize; i++) {
 					int x = cia[i];
 					if (x < 0) {
 						cia[i] = x + cioffset;
 						dirty = true;
 					}
 				}
 				if (dirty)
 					cids.setAbs(it.index, cia);
 			}
 			break;
 		case AbstractDataset.ARRAYINT64:
 			CompoundLongDataset clds = (CompoundLongDataset) a;
 			final long cloffset = (long) dv;
 			final long[] cla = new long[isize];
 			while (it.hasNext()) {
 				clds.getAbs(it.index, cla);
 				boolean dirty = false;
 				for (int i = 0; i < isize; i++) {
 					long x = cla[i];
 					if (x < 0) {
 						cla[i] = x + cloffset;
 						dirty = true;
 					}
 				}
 				if (dirty)
 					clds.setAbs(it.index, cla);
 			}
 			break;
 		default:
 			utilsLogger.error("Dataset of unsupported type for this method");
 			break;
 		}
 	}
 
 	/**
 	 * Create a 1D dataset of linearly spaced values in closed interval
 	 * 
 	 * @param start
 	 * @param stop
 	 * @param length number of points
 	 * @param dtype
 	 * @return dataset with linearly spaced values
 	 */
 	public static AbstractDataset linSpace(final double start, final double stop, final int length, final int dtype) {
 		if (length < 1) {
 			utilsLogger.error("Length is less than one");
 			throw new IllegalArgumentException("Length is less than one");
 		} else if (length == 1) {
 			return AbstractDataset.array(start, dtype);
 		} else {
 			AbstractDataset ds = AbstractDataset.zeros(new int[] {length}, dtype);
 			double step = (stop - start) / (length - 1);
 			double value;
 
 			for (int i = 0; i < length; i++) {
 				value = start + i * step;
 				ds.setObjectAbs(i, value);
 			}
 			return ds;
 		}
 	}
 
 	/**
 	 * Create a 1D dataset of logarithmically spaced values in closed interval. The base value is used to
 	 * determine the factor between values: factor = base ** step, where step is the interval between linearly
 	 * spaced sequence of points
 	 * 
 	 * @param start
 	 * @param stop
 	 * @param length number of points
 	 * @param base
 	 * @param dtype
 	 * @return dataset with logarithmically spaced values
 	 */
 	public static AbstractDataset logSpace(final double start, final double stop, final int length, final double base, final int dtype) {
 		if (length < 1) {
 			utilsLogger.error("Length is less than one");
 			throw new IllegalArgumentException("Length is less than one");
 		} else if (length == 1) {
 			return AbstractDataset.array(Math.pow(base, start), dtype);
 		} else {
 			AbstractDataset ds = AbstractDataset.zeros(new int[] {length}, dtype);
 			double step = (stop - start) / (length - 1);
 			double value;
 
 			for (int i = 0; i < length; i++) {
 				value = start + i * step;
 				ds.setObjectAbs(i, Math.pow(base, value));
 			}
 			return ds;
 		}
 	}
 
 	/**
 	 * @param rows
 	 * @param cols
 	 * @param offset
 	 * @param dtype
 	 * @return a new 2d dataset of given shape and type, filled with ones on the (offset) diagonal
 	 */
 	public static AbstractDataset eye(final int rows, final int cols, final int offset, final int dtype) {
 		int[] shape = new int[] {rows, cols};
 		AbstractDataset a = AbstractDataset.zeros(shape, dtype);
 
 		int[] pos = new int[] {0, offset};
 		while (pos[1] < 0) {
 			pos[0]++;
 			pos[1]++;
 		}
 		while (pos[0] < rows && pos[1] < cols) {
 			a.set(1, pos);
 			pos[0]++;
 			pos[1]++;
 		}
 		return a;
 	}
 
 	/**
 	 * Create a (off-)diagonal matrix from items in dataset
 	 * @param a
 	 * @param offset
 	 * @return diagonal matrix
 	 */
 	public static AbstractDataset diag(final AbstractDataset a, final int offset) {
 		final int dtype = a.getDtype();
 		final int rank = a.getRank();
 		final int is = a.getElementsPerItem();
 
 		if (rank == 0 || rank > 2) {
 			utilsLogger.error("Rank of dataset should be one or two");
 			throw new IllegalArgumentException("Rank of dataset should be one or two");
 		}
 
 		AbstractDataset result;
 		if (rank == 1) {
 			int side = a.shape[0] + Math.abs(offset);
 			int[] pos = new int[] {side, side};
 			result = AbstractDataset.zeros(is, pos, dtype);
 			pos[0] = 0;
 			pos[1] = offset;
 			while (pos[1] < 0) {
 				pos[0]++;
 				pos[1]++;
 			}
 			int i = 0;
 			while (pos[0] < side && pos[1] < side) {
 				result.set(a.getObject(i++), pos);
 				pos[0]++;
 				pos[1]++;
 			}
 		} else {
 			int side = offset >= 0 ? Math.min(a.shape[0], a.shape[1]-offset) : Math.min(a.shape[0]+offset, a.shape[1]);
 			if (side < 0)
 				side = 0;
 			result = AbstractDataset.zeros(is, new int[] {side}, dtype);
 
 			if (side > 0) {
 				int[] pos = new int[] { 0, offset };
 
 				while (pos[1] < 0) {
 					pos[0]++;
 					pos[1]++;
 				}
 				int i = 0;
 				while (pos[0] < a.shape[0] && pos[1] < a.shape[1]) {
 					result.set(a.getObject(pos), i++);
 					pos[0]++;
 					pos[1]++;
 				}
 			}
 		}
 
 		return result;
 	}
 
 	/**
 	 * Convert (if necessary) a dataset obeying the interface to our implementation. This will strip Jython methods
 	 * from subclasses of AbstractDataset
 	 * @param lazydata can be null
 	 * @return Converted dataset or null
 	 */
 	public static AbstractDataset convertToAbstractDataset(ILazyDataset lazydata) {
 		if (lazydata == null) 
 			return null;
 
 		if (lazydata instanceof AbstractDataset) {
 			AbstractDataset adata = (AbstractDataset) lazydata;
 			return adata.getView();
 		}
 
 		int dtype;
 		if (lazydata instanceof ADataset) {
 			dtype = ((ADataset) lazydata).getDtype();
 		} else {
 			dtype = AbstractDataset.getDType(lazydata);
 		}
 
 		IDataset data;
 		if (lazydata instanceof IDataset) {
 			data = (IDataset)lazydata;
 		} else {
 			throw new IllegalArgumentException("This is a lazy dataset and should not be fully loaded");
 		}
 
 		final int isize = data.getElementsPerItem();
 		if (isize <= 0) {
 			throw new IllegalArgumentException("Datasets with " + isize + " elements per item not supported");
 		}
 
 		final AbstractDataset result = AbstractDataset.zeros(isize, data.getShape(), dtype);
 		result.setName(data.getName());
 
 		final IndexIterator it = result.getIterator(true);
 		final int[] pos = it.getPos();
 		switch (dtype) {
 		case AbstractDataset.BOOL:
 			while (it.hasNext()) {
 				result.setObjectAbs(it.index, data.getBoolean(pos));
 			}
 			break;
 		case AbstractDataset.INT8:
 			while (it.hasNext()) {
 				result.setObjectAbs(it.index, data.getByte(pos));
 			}
 			break;
 		case AbstractDataset.INT16:
 			while (it.hasNext()) {
 				result.setObjectAbs(it.index, data.getShort(pos));
 			}
 			break;
 		case AbstractDataset.INT32:
 			while (it.hasNext()) {
 				result.setObjectAbs(it.index, data.getInt(pos));
 			}
 			break;
 		case AbstractDataset.INT64:
 			while (it.hasNext()) {
 				result.setObjectAbs(it.index, data.getLong(pos));
 			}
 			break;
 		case AbstractDataset.FLOAT32:
 			while (it.hasNext()) {
 				result.setObjectAbs(it.index, data.getFloat(pos));
 			}
 			break;
 		case AbstractDataset.FLOAT64:
 			while (it.hasNext()) {
 				result.setObjectAbs(it.index, data.getDouble(pos));
 			}
 			break;
 		default:
 			while (it.hasNext()) {
 				result.setObjectAbs(it.index, data.getObject(pos));
 			}
 			break;
 		}
 
 		if (lazydata instanceof ADataset) {
			result.setError(((ADataset) lazydata).getErrorBuffer());
 		}
 		return result;
 	}
 
 	/**
 	 * Create a compound dataset by using last axis as elements of an item
 	 * @param a
 	 * @param shareData if true, then share data
 	 * @return compound dataset
 	 */
 	public static AbstractCompoundDataset createCompoundDatasetFromLastAxis(final AbstractDataset a, final boolean shareData) {
 		if (a instanceof AbstractCompoundDataset) {
 			utilsLogger.error("Need a single-element dataset");
 			throw new IllegalArgumentException("Need a single-element dataset");
 		}
 
 		Serializable buffer = shareData ? a.getBuffer() : a.clone().getBuffer();
 
 		final AbstractCompoundDataset result;
 		final int[] shape = a.getShape();
 		final int rank = shape.length - 1;
 
 		final int is = rank < 0 ? 1 : shape[rank];
 
 		switch (a.getDtype()) {
 		case AbstractDataset.INT8:
 			result = new CompoundByteDataset(is);
 			break;
 		case AbstractDataset.INT16:
 			result = new CompoundShortDataset(is);
 			break;
 		case AbstractDataset.INT32:
 			result = new CompoundIntegerDataset(is);
 			break;
 		case AbstractDataset.INT64:
 			result = new CompoundLongDataset(is);
 			break;
 		case AbstractDataset.FLOAT32:
 			result = new CompoundFloatDataset(is);
 			break;
 		case AbstractDataset.FLOAT64:
 			result = new CompoundDoubleDataset(is);
 			break;
 		default:
 			utilsLogger.error("Dataset type not supported for this operation");
 			throw new UnsupportedOperationException("Dataset type not supported");
 		}
 
 		result.shape = rank > 0 ? Arrays.copyOf(shape, rank) : (rank < 0 ? new int[] {} : new int[] {1});
 		result.size = AbstractDataset.calcSize(result.shape);
 		result.odata = buffer;
 		result.setName(a.getName());
 		result.setData();
 		return result;
 	}
 
 	/**
 	 * Create a dataset from a compound dataset by using elements of an item as last axis
 	 * @param a
 	 * @param shareData if true, then share data
 	 * @return compound dataset
 	 */
 	public static AbstractDataset createDatasetFromCompoundDataset(final AbstractCompoundDataset a, final boolean shareData) {
 		Serializable buffer = shareData ? a.getBuffer() : a.clone().getBuffer();
 
 		AbstractDataset result;
 		switch (a.getDtype()) {
 		case AbstractDataset.ARRAYINT8:
 			result = new ByteDataset();
 			break;
 		case AbstractDataset.ARRAYINT16:
 			result = new ShortDataset();
 			break;
 		case AbstractDataset.ARRAYINT32:
 			result = new IntegerDataset();
 			break;
 		case AbstractDataset.ARRAYINT64:
 			result = new LongDataset();
 			break;
 		case AbstractDataset.ARRAYFLOAT32:
 			result = new FloatDataset();
 			break;
 		case AbstractDataset.ARRAYFLOAT64:
 			result = new DoubleDataset();
 			break;
 		default:
 			utilsLogger.error("Dataset type not supported for this operation");
 			throw new UnsupportedOperationException("Dataset type not supported");
 		}
 
 		final int[] shape = a.getShape();
 		final int rank = shape.length + 1;
 		final int[] nshape = Arrays.copyOf(shape, rank);
 		final int is = a.getElementsPerItem();
 		nshape[rank-1] = is;
 
 		result.shape = nshape;
 		result.size = AbstractDataset.calcSize(nshape);
 		result.odata = buffer;
 		result.setName(a.getName());
 		result.setData();
 		return result;
 	}
 
 	/**
 	 * Create a copy that has been coerced to an appropriate dataset type
 	 * depending on the input object's class
 	 *
 	 * @param a
 	 * @param obj
 	 * @return coerced copy of dataset
 	 */
 	public static AbstractDataset coerce(AbstractDataset a, Object obj) {
 		final int dt = a.getDtype();
 		final int ot = AbstractDataset.getDTypeFromClass(obj.getClass());
 
 		return cast(a.clone(), AbstractDataset.getBestDType(dt, ot));
 	}
 
 	/**
 	 * @param a
 	 * @param b
 	 * @return element-wise maximum of given datasets
 	 */
 	public static AbstractDataset maximum(final AbstractDataset a, final Object b) {
 		AbstractDataset result;
 
 		if (b instanceof AbstractDataset) {
 			AbstractDataset bds = (AbstractDataset) b;
 			a.checkCompatibility(bds);
 
 			final int at = a.getDtype();
 			final int bt = bds.getDtype();
 			final int rt;
 
 			AbstractDataset d1, d2;
 			if (bt > at) {
 				rt = bt;
 				d1 = bds;
 				d2 = a;
 			} else {
 				rt = at;
 				d1 = a;
 				d2 = bds;
 			}
 
 			IndexIterator it1 = d1.getIterator();
 			IndexIterator it2 = d2.getIterator();
 
 			result = AbstractDataset.zeros(d1);
 
 			switch (rt) {
 			case AbstractDataset.BOOL:
 				boolean[] bdata = ((BooleanDataset) result).getData();
 
 				for (int i = 0; it1.hasNext() && it2.hasNext();) {
 					bdata[i++] = d1.getElementBooleanAbs(it1.index) || d2.getElementBooleanAbs(it2.index);
 				}
 				break;
 			case AbstractDataset.INT8:
 				byte[] i8data = ((ByteDataset) result).getData();
 				long i1;
 				long i2;
 
 				for (int i = 0; it1.hasNext() && it2.hasNext();) {
 					i1 = d1.getElementLongAbs(it1.index);
 					i2 = d2.getElementLongAbs(it2.index);
 					i8data[i++] = (byte) (i1 > i2 ? i1 : i2);
 				}
 				break;
 			case AbstractDataset.INT16:
 				short[] i16data = ((ShortDataset) result).getData();
 
 				for (int i = 0; it1.hasNext() && it2.hasNext();) {
 					i1 = d1.getElementLongAbs(it1.index);
 					i2 = d2.getElementLongAbs(it2.index);
 					i16data[i++] = (short) (i1 > i2 ? i1 : i2);
 				}
 				break;
 			case AbstractDataset.INT32:
 				int[] i32data = ((IntegerDataset) result).getData();
 
 				for (int i = 0; it1.hasNext() && it2.hasNext();) {
 					i1 = d1.getElementLongAbs(it1.index);
 					i2 = d2.getElementLongAbs(it2.index);
 					i32data[i++] = (int) (i1 > i2 ? i1 : i2);
 				}
 				break;
 			case AbstractDataset.INT64:
 				long[] i64data = ((LongDataset) result).getData();
 
 				for (int i = 0; it1.hasNext() && it2.hasNext();) {
 					i1 = d1.getElementLongAbs(it1.index);
 					i2 = d2.getElementLongAbs(it2.index);
 					i64data[i++] = i1 > i2 ? i1 : i2;
 				}
 				break;
 			case AbstractDataset.FLOAT32:
 				float[] f32data = ((FloatDataset) result).getData();
 				double r1,
 				r2;
 
 				for (int i = 0; it1.hasNext() && it2.hasNext();) {
 					r1 = d1.getElementDoubleAbs(it1.index);
 					r2 = d2.getElementDoubleAbs(it2.index);
 					f32data[i++] = (float) (r1 > r2 ? r1 : r2);
 				}
 				break;
 			case AbstractDataset.FLOAT64:
 				double[] f64data = ((DoubleDataset) result).getData();
 
 				for (int i = 0; it1.hasNext() && it2.hasNext();) {
 					r1 = d1.getElementDoubleAbs(it1.index);
 					r2 = d2.getElementDoubleAbs(it2.index);
 					f64data[i++] = r1 > r2 ? r1 : r2;
 				}
 				break;
 			default:
 				throw new IllegalArgumentException("Multiple-element datasets not supported");
 			}
 		} else {
 			final int dt = AbstractDataset.getBestDType(a.getDtype(), AbstractDataset.getDTypeFromClass(b.getClass()));
 			result = AbstractDataset.zeros(a.shape, dt);
 			final IndexIterator it1 = a.getIterator();
 			long i2;
 			double r2;
 
 			switch (dt) {
 			case AbstractDataset.BOOL:
 				boolean b2 = AbstractDataset.toBoolean(b);
 				boolean[] bdata = ((BooleanDataset) result).getData();
 
 				for (int i = 0; it1.hasNext();) {
 					bdata[i++] = a.getElementBooleanAbs(it1.index) || b2;
 				}
 				break;
 			case AbstractDataset.INT8:
 				byte[] i8data = ((ByteDataset) result).getData();
 				long i1;
 				i2 = AbstractDataset.toLong(b);
 				for (int i = 0; it1.hasNext();) {
 					i1 = a.getElementLongAbs(it1.index);
 					i8data[i++] = (byte) (i1 > i2 ? i1 : i2);
 				}
 				break;
 			case AbstractDataset.INT16:
 				short[] i16data = ((ShortDataset) result).getData();
 
 				i2 = AbstractDataset.toLong(b);
 				for (int i = 0; it1.hasNext();) {
 					i1 = a.getElementLongAbs(it1.index);
 					i16data[i++] = (short) (i1 > i2 ? i1 : i2);
 				}
 				break;
 			case AbstractDataset.INT32:
 				int[] i32data = ((IntegerDataset) result).getData();
 
 				i2 = AbstractDataset.toLong(b);
 				for (int i = 0; it1.hasNext();) {
 					i1 = a.getElementLongAbs(it1.index);
 					i32data[i++] = (int) (i1 > i2 ? i1 : i2);
 				}
 				break;
 			case AbstractDataset.INT64:
 				long[] i64data = ((LongDataset) result).getData();
 
 				i2 = AbstractDataset.toLong(b);
 				for (int i = 0; it1.hasNext();) {
 					i1 = a.getElementLongAbs(it1.index);
 					i64data[i++] = i1 > i2 ? i1 : i2;
 				}
 				break;
 			case AbstractDataset.FLOAT32:
 				float[] f32data = ((FloatDataset) result).getData();
 				double r1;
 
 				r2 = AbstractDataset.toReal(b);
 				for (int i = 0; it1.hasNext();) {
 					r1 = a.getElementDoubleAbs(it1.index);
 					f32data[i++] = (float) (r1 > r2 ? r1 : r2);
 				}
 				break;
 			case AbstractDataset.FLOAT64:
 				double[] f64data = ((DoubleDataset) result).getData();
 
 				r2 = AbstractDataset.toReal(b);
 				for (int i = 0; it1.hasNext();) {
 					r1 = a.getElementDoubleAbs(it1.index);
 					f64data[i++] = r1 > r2 ? r1 : r2;
 				}
 				break;
 			default:
 				throw new IllegalArgumentException("Multiple-element datasets not supported");
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * @param a
 	 * @param b
 	 * @return element-wise minimum of given datasets
 	 */
 	public static AbstractDataset minimum(final AbstractDataset a, final Object b) {
 		AbstractDataset result;
 
 		if (b instanceof AbstractDataset) {
 			AbstractDataset bds = (AbstractDataset) b;
 			a.checkCompatibility(bds);
 
 			final int at = a.getDtype();
 			final int bt = bds.getDtype();
 			final int rt;
 
 			AbstractDataset d1, d2;
 			if (bt > at) {
 				rt = bt;
 				d1 = bds;
 				d2 = a;
 			} else {
 				rt = at;
 				d1 = a;
 				d2 = bds;
 			}
 
 			IndexIterator it1 = d1.getIterator();
 			IndexIterator it2 = d2.getIterator();
 
 			result = AbstractDataset.zeros(d1);
 
 			switch (rt) {
 			case AbstractDataset.BOOL:
 				boolean[] bdata = ((BooleanDataset) result).getData();
 
 				for (int i = 0; it1.hasNext() && it2.hasNext();) {
 					bdata[i++] = d1.getElementBooleanAbs(it1.index) && d2.getElementBooleanAbs(it2.index);
 				}
 				break;
 			case AbstractDataset.INT8:
 				byte[] i8data = ((ByteDataset) result).getData();
 				long i1;
 				long i2;
 
 				for (int i = 0; it1.hasNext() && it2.hasNext();) {
 					i1 = d1.getElementLongAbs(it1.index);
 					i2 = d2.getElementLongAbs(it2.index);
 					i8data[i++] = (byte) (i1 < i2 ? i1 : i2);
 				}
 				break;
 			case AbstractDataset.INT16:
 				short[] i16data = ((ShortDataset) result).getData();
 
 				for (int i = 0; it1.hasNext() && it2.hasNext();) {
 					i1 = d1.getElementLongAbs(it1.index);
 					i2 = d2.getElementLongAbs(it2.index);
 					i16data[i++] = (short) (i1 < i2 ? i1 : i2);
 				}
 				break;
 			case AbstractDataset.INT32:
 				int[] i32data = ((IntegerDataset) result).getData();
 
 				for (int i = 0; it1.hasNext() && it2.hasNext();) {
 					i1 = d1.getElementLongAbs(it1.index);
 					i2 = d2.getElementLongAbs(it2.index);
 					i32data[i++] = (int) (i1 < i2 ? i1 : i2);
 				}
 				break;
 			case AbstractDataset.INT64:
 				long[] i64data = ((LongDataset) result).getData();
 
 				for (int i = 0; it1.hasNext() && it2.hasNext();) {
 					i1 = d1.getElementLongAbs(it1.index);
 					i2 = d2.getElementLongAbs(it2.index);
 					i64data[i++] = i1 < i2 ? i1 : i2;
 				}
 				break;
 			case AbstractDataset.FLOAT32:
 				float[] f32data = ((FloatDataset) result).getData();
 				double r1,
 				r2;
 
 				for (int i = 0; it1.hasNext() && it2.hasNext();) {
 					r1 = d1.getElementDoubleAbs(it1.index);
 					r2 = d2.getElementDoubleAbs(it2.index);
 					f32data[i++] = (float) (r1 < r2 ? r1 : r2);
 				}
 				break;
 			case AbstractDataset.FLOAT64:
 				double[] f64data = ((DoubleDataset) result).getData();
 
 				for (int i = 0; it1.hasNext() && it2.hasNext();) {
 					r1 = d1.getElementDoubleAbs(it1.index);
 					r2 = d2.getElementDoubleAbs(it2.index);
 					f64data[i++] = r1 < r2 ? r1 : r2;
 				}
 				break;
 			default:
 				throw new IllegalArgumentException("Multiple-element datasets not supported");
 			}
 		} else {
 			final int dt = AbstractDataset.getBestDType(a.getDtype(), AbstractDataset.getDTypeFromClass(b.getClass()));
 			result = AbstractDataset.zeros(a.shape, dt);
 			final IndexIterator it1 = a.getIterator();
 			long i2;
 			double r2;
 
 			switch (dt) {
 			case AbstractDataset.BOOL:
 				boolean b2 = AbstractDataset.toBoolean(b);
 				boolean[] bdata = ((BooleanDataset) result).getData();
 
 				for (int i = 0; it1.hasNext();) {
 					bdata[i++] = a.getElementBooleanAbs(it1.index) && b2;
 				}
 				break;
 			case AbstractDataset.INT8:
 				byte[] i8data = ((ByteDataset) result).getData();
 				long i1;
 				i2 = AbstractDataset.toLong(b);
 				for (int i = 0; it1.hasNext();) {
 					i1 = a.getElementLongAbs(it1.index);
 					i8data[i++] = (byte) (i1 < i2 ? i1 : i2);
 				}
 				break;
 			case AbstractDataset.INT16:
 				short[] i16data = ((ShortDataset) result).getData();
 
 				i2 = AbstractDataset.toLong(b);
 				for (int i = 0; it1.hasNext();) {
 					i1 = a.getElementLongAbs(it1.index);
 					i16data[i++] = (short) (i1 < i2 ? i1 : i2);
 				}
 				break;
 			case AbstractDataset.INT32:
 				int[] i32data = ((IntegerDataset) result).getData();
 
 				i2 = AbstractDataset.toLong(b);
 				for (int i = 0; it1.hasNext();) {
 					i1 = a.getElementLongAbs(it1.index);
 					i32data[i++] = (int) (i1 < i2 ? i1 : i2);
 				}
 				break;
 			case AbstractDataset.INT64:
 				long[] i64data = ((LongDataset) result).getData();
 
 				i2 = AbstractDataset.toLong(b);
 				for (int i = 0; it1.hasNext();) {
 					i1 = a.getElementLongAbs(it1.index);
 					i64data[i++] = i1 < i2 ? i1 : i2;
 				}
 				break;
 			case AbstractDataset.FLOAT32:
 				float[] f32data = ((FloatDataset) result).getData();
 				double r1;
 
 				r2 = AbstractDataset.toReal(b);
 				for (int i = 0; it1.hasNext();) {
 					r1 = a.getElementDoubleAbs(it1.index);
 					f32data[i++] = (float) (r1 < r2 ? r1 : r2);
 				}
 				break;
 			case AbstractDataset.FLOAT64:
 				double[] f64data = ((DoubleDataset) result).getData();
 
 				r2 = AbstractDataset.toReal(b);
 				for (int i = 0; it1.hasNext();) {
 					r1 = a.getElementDoubleAbs(it1.index);
 					f64data[i++] = r1 < r2 ? r1 : r2;
 				}
 				break;
 			default:
 				throw new IllegalArgumentException("Multiple-element datasets not supported");
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * Function that returns a normalised dataset which is bounded between 0 and 1
 	 * @param a dataset
 	 * @return normalised dataset
 	 */
 	public static AbstractDataset norm(AbstractDataset a) {
 		double amin = a.min().doubleValue();
 		double aptp = a.max().doubleValue() - amin;
 		AbstractDataset temp = Maths.subtract(a, amin);
 		temp.idivide(aptp);
 		return temp;
 	}
 
 	/**
 	 * Function that returns a normalised compound dataset which is bounded between 0 and 1. There
 	 * are (at least) two ways to normalise a compound dataset: per element - extrema for each element
 	 * in a compound item is used, i.e. many min/max pairs; over all elements - extrema for all elements
 	 * is used, i.e. one min/max pair.
 	 * @param a dataset
 	 * @param overAllElements if true, then normalise over all elements in each item
 	 * @return normalised dataset
 	 */
 	public static AbstractCompoundDataset norm(AbstractCompoundDataset a, boolean overAllElements) {
 		double[] amin = a.minItem();
 		double[] amax = a.maxItem();
 		final int is = a.isize;
 		AbstractDataset result;
 
 		if (overAllElements) {
 			Arrays.sort(amin);
 			Arrays.sort(amax);
 			double aptp = amax[0] - amin[0];
 			
 			result = Maths.subtract(a, amin[0]);
 			result.idivide(aptp);
 		} else {
 			double[] aptp = new double[is];
 			for (int j = 0; j < is; j++) {
 				aptp[j] = amax[j] - amin[j];
 			}
 
 			result = Maths.subtract(a, amin);
 			result.idivide(aptp);
 		}
 		return (AbstractCompoundDataset) result;
 	}
 
 	/**
 	 * Function that returns a normalised dataset which is bounded between 0 and 1
 	 * and has been distributed on a log10 scale
 	 * @param a dataset
 	 * @return normalised dataset
 	 */
 	public static AbstractDataset lognorm(AbstractDataset a) {
 		double amin = a.min().doubleValue();
 		double aptp = Math.log10(a.max().doubleValue() - amin + 1.);
 		AbstractDataset temp = Maths.subtract(a, amin - 1.);
 		temp = Maths.log10(temp);
 		temp = Maths.divide(temp, aptp);
 		return temp;
 	}
 
 	/**
 	 * Function that returns a normalised dataset which is bounded between 0 and 1
 	 * and has been distributed on a natural log scale
 	 * @param a dataset
 	 * @return normalised dataset
 	 */
 	public static AbstractDataset lnnorm(AbstractDataset a) {
 		double amin = a.min().doubleValue();
 		double aptp = Math.log(a.max().doubleValue() - amin + 1.);
 		AbstractDataset temp = Maths.subtract(a, amin - 1.);
 		temp = Maths.log(temp);
 		temp = Maths.divide(temp, aptp);
 		return temp;
 	}
 
 	/**
 	 * Construct a list of datasets where each represents a coordinate varying over the hypergrid
 	 * formed by the input list of axes
 	 * 
 	 * @param axes an array of 1D datasets representing axes
 	 * @return a list of coordinate datasets
 	 */
 	public static List<AbstractDataset> meshGrid(final AbstractDataset... axes) {
 		List<AbstractDataset> result = new ArrayList<AbstractDataset>();
 		int rank = axes.length;
 
 		if (rank < 2) {
 			utilsLogger.error("Two or more axes datasets are required");
 			throw new IllegalArgumentException("Two or more axes datasets are required");
 		}
 
 		int[] nshape = new int[rank];
 
 		for (int i = 0; i < rank; i++) {
 			AbstractDataset axis = axes[i];
 			if (axis.getRank() != 1) {
 				utilsLogger.error("Given axis is not 1D");
 				throw new IllegalArgumentException("Given axis is not 1D");
 			}
 			nshape[i] = axis.size;
 		}
 
 		for (int i = 0; i < rank; i++) {
 			AbstractDataset axis = axes[i];
 			AbstractDataset coord = AbstractDataset.zeros(nshape, axis.getDtype());
 			result.add(coord);
 
 			final int alen = axis.size;
 			for (int j = 0; j < alen; j++) {
 				final Object obj = axis.getObjectAbs(j);
 				PositionIterator pi = coord.getPositionIterator(i);
 				final int[] pos = pi.getPos();
 
 				pos[i] = j;
 				while (pi.hasNext()) {
 					coord.set(obj, pos);
 				}
 			}
 		}
 
 		return result;
 	}
 
 	/**
 	 * Generate an index dataset for given dataset where sub-datasets contain index values
 	 *
 	 * @return an index dataset
 	 */
 	public static IntegerDataset indices(int... shape) {
 		// now create another dataset to plot against
 		final int rank = shape.length;
 		int[] nshape = new int[rank+1];
 		nshape[0] = rank;
 		for (int i = 0; i < rank; i++) {
 			nshape[i+1] = shape[i];
 		}
 
 		IntegerDataset index = new IntegerDataset(nshape);
 
 		if (rank == 1) {
 			final int alen = shape[0];
 			int[] pos = new int[2];
 			for (int j = 0; j < alen; j++) {
 				pos[1] = j;
 				index.set(j, pos);
 			}
 		} else {
 			for (int i = 1; i <= rank; i++) {
 				final int alen = nshape[i];
 				for (int j = 0; j < alen; j++) {
 					PositionIterator pi = index.getPositionIterator(0, i);
 					final int[] pos = pi.getPos();
 
 					pos[0] = i-1;
 					pos[i] = j;
 					while (pi.hasNext()) {
 						index.set(j, pos);
 					}
 				}
 			}
 		}
 		return index;
 	}
 
 	/**
 	 * Get the centroid value of a dataset, this function works out the centroid in every direction
 	 * 
 	 * @param a
 	 *            the dataset to be analysed
 	 * @param bases the optional array of base coordinates to use as weights.
 	 * This defaults to the mid-point of indices
 	 * @return a double array containing the centroid for each dimension
 	 */
 	public static double[] centroid(AbstractDataset a, AbstractDataset... bases) {
 		List<Double> d = new Centroid(bases).value(a);
 		double[] dc = new double[d.size()];
 		for (int i = 0; i < dc.length; i++)
 			dc[i] = d.get(i);
 
 		return dc;
 	}
 
 	/**
 	 * Find linearly-interpolated crossing points where the given dataset crosses the given value
 	 * 
 	 * @param d
 	 * @param value
 	 * @return list of interpolated indices
 	 */
 	public static List<Double> crossings(AbstractDataset d, double value) {
 		if (d.getRank() != 1) {
 			utilsLogger.error("Only 1d datasets supported");
 			throw new UnsupportedOperationException("Only 1d datasets supported");
 		}
 		List<Double> results = new ArrayList<Double>();
 
 		// run through all pairs of points on the line and see if value lies within
 		double y1, y2;
 		y2 = d.getElementDoubleAbs(0);
 		for (int i = 1, imax = d.getSize(); i < imax; i++) {
 			y1 = y2;
 			y2 = d.getElementDoubleAbs(i);
 			// check if value lies within pair [y1, y2]
 			if ((y1 <= value && y2 > value) || (y1 > value && y2 <= value)) {
 				final double f = (value - y2)/(y2 - y1); // negative distance from right to left
 				results.add(i + f);
 			}
 		}
 
 		return results;
 	}
 
 	/**
 	 * Find x values of all the crossing points of the dataset with the given y value
 	 * 
 	 * @param xAxis
 	 *            Dataset of the X axis that needs to be looked at
 	 * @param yAxis
 	 *            Dataset of the Y axis that needs to be looked at
 	 * @param yValue
 	 *            The y value the X values are required for
 	 * @return An list of doubles containing all the X coordinates of where the line crosses
 	 */
 	public static List<Double> crossings(AbstractDataset xAxis, AbstractDataset yAxis, double yValue) {
 		List<Double> results = new ArrayList<Double>();
 
 		List<Double> indices = crossings(yAxis, yValue);
 
 		for (double xi : indices) {
 			results.add(Maths.getLinear(xAxis, xi));
 		}
 		return results;
 	}
 
 	/**
 	 * Function that uses the crossings function but prunes the result, so that multiple crossings within a
 	 * certain proportion of the overall range of the x values
 	 * 
 	 * @param xAxis
 	 *            Dataset of the X axis
 	 * @param yAxis
 	 *            Dataset of the Y axis
 	 * @param yValue
 	 *            The y value the x values are required for
 	 * @param xRangeProportion
 	 *            The proportion of the overall x spread used to prune result
 	 * @return A list containing all the unique crossing points
 	 */
 	public static List<Double> crossings(AbstractDataset xAxis, AbstractDataset yAxis, double yValue, double xRangeProportion) {
 		// get the values found
 		List<Double> vals = crossings(xAxis, yAxis, yValue);
 
 		// use the proportion to calculate the error spacing
 		double error = xRangeProportion * xAxis.peakToPeak().doubleValue();
 
 		int i = 0;
 		// now go through and check for groups of three crossings which are all
 		// within the boundaries
 		while (i < vals.size() - 3) {
 			double v1 = Math.abs(vals.get(i) - vals.get(i + 2));
 			if (v1 < error) {
 				// these 3 points should be treated as one
 				// make the first point equal to the average of them all
 				vals.set(i + 2, ((vals.get(i) + vals.get(i + 1) + vals.get(i + 2)) / 3.0));
 				// remove the other offending points
 				vals.remove(i);
 				vals.remove(i);
 			} else {
 				i++;
 			}
 		}
 
 		// once the thinning process has been completed, return the pruned list
 		return vals;
 	}
 
 	// recursive function
 	private static void setRow(Object row, AbstractDataset a, int... pos) {
 		final int l = Array.getLength(row);
 		final int rank = pos.length;
 		final int[] npos = Arrays.copyOf(pos, rank+1);
 		Object r;
 		if (rank+1 < a.getRank()) {
 			for (int i = 0; i < l; i++) {
 				npos[rank] = i;
 				r = Array.get(row, i);
 				setRow(r, a, npos);
 			}
 		} else {
 			for (int i = 0; i < l; i++) {
 				npos[rank] = i;
 				r = a.getObject(npos);
 				Array.set(row, i, r);
 			}
 		}
 	}
 
 	/**
 	 * Create Java array (of arrays) from dataset
 	 * @param a dataset
 	 * @return Java array (of arrays...)
 	 */
 	public static Object createJavaArray(AbstractDataset a) {
 		if (a.getElementsPerItem() > 1) {
 			a = createDatasetFromCompoundDataset((AbstractCompoundDataset) a, true);
 		}
 		Object matrix;
 
 		switch (a.getDtype()) {
 		case AbstractDataset.BOOL:
 			matrix = Array.newInstance(boolean.class, a.getShape());
 			break;
 		case AbstractDataset.INT8:
 			matrix = Array.newInstance(byte.class, a.getShape());
 			break;
 		case AbstractDataset.INT16:
 			matrix = Array.newInstance(short.class, a.getShape());
 			break;
 		case AbstractDataset.INT32:
 			matrix = Array.newInstance(int.class, a.getShape());
 			break;
 		case AbstractDataset.INT64:
 			matrix = Array.newInstance(long.class, a.getShape());
 			break;
 		case AbstractDataset.FLOAT32:
 			matrix = Array.newInstance(float.class, a.getShape());
 			break;
 		case AbstractDataset.FLOAT64:
 			matrix = Array.newInstance(double.class, a.getShape());
 			break;
 		default:
 			utilsLogger.error("Dataset type not supported");
 			throw new IllegalArgumentException("Dataset type not supported");
 		}
 
 		// populate matrix
 		setRow(matrix, a);
 		return matrix;
 	}
 	
 	/**
 	 * Removes NaNs and infinities from floating point datasets.
 	 * All other dataset types are ignored.
 	 * 
 	 * @param a dataset
 	 * @param value replacement value
 	 */
 	public static void removeNansAndInfinities(AbstractDataset a, final Number value) {
 		if (a instanceof DoubleDataset) {
 			final double dvalue = AbstractDataset.toReal(value);
 			final DoubleDataset set = (DoubleDataset) a;
 			final IndexIterator it = set.getIterator();
 			final double[] data = set.getData();
 			while (it.hasNext()) {
 				double x = data[it.index];
 				if (Double.isNaN(x) || Double.isInfinite(x))
 					data[it.index] = dvalue;
 			}
 		} else if (a instanceof FloatDataset) {
 			final float fvalue = (float) AbstractDataset.toReal(value);
 			final FloatDataset set = (FloatDataset) a;
 			final IndexIterator it = set.getIterator();
 			final float[] data = set.getData();
 			while (it.hasNext()) {
 				float x = data[it.index];
 				if (Float.isNaN(x) || Float.isInfinite(x))
 					data[it.index] = fvalue;
 			}
 		} else if (a instanceof CompoundDoubleDataset) {
 			final double dvalue = AbstractDataset.toReal(value);
 			final CompoundDoubleDataset set = (CompoundDoubleDataset) a;
 			final int is = set.getElementsPerItem();
 			final IndexIterator it = set.getIterator();
 			final double[] data = set.getData();
 			while (it.hasNext()) {
 				for (int j = 0; j < is; j++) {
 					double x = data[it.index + j];
 					if (Double.isNaN(x) || Double.isInfinite(x))
 						data[it.index + j] = dvalue;
 				}
 			}
 		} else if (a instanceof CompoundFloatDataset) {
 			final float fvalue = (float) AbstractDataset.toReal(value);
 			final CompoundFloatDataset set = (CompoundFloatDataset) a;
 			final int is = set.getElementsPerItem();
 			final IndexIterator it = set.getIterator();
 			final float[] data = set.getData();
 			while (it.hasNext()) {
 				for (int j = 0; j < is; j++) {
 					float x = data[it.index + j];
 					if (Float.isNaN(x) || Float.isInfinite(x))
 						data[it.index + j] = fvalue;
 				}
 			}
 		}
 	}
 
 	/**
 	 * Make floating point datasets contain only finite values. Infinities and NaNs are replaced
 	 * by +/- MAX_VALUE and 0, respectively.
 	 * All other dataset types are ignored.
 	 * 
 	 * @param a dataset
 	 */
 	public static void makeFinite(AbstractDataset a) {
 		if (a instanceof DoubleDataset) {
 			final DoubleDataset set = (DoubleDataset) a;
 			final IndexIterator it = set.getIterator();
 			final double[] data = set.getData();
 			while (it.hasNext()) {
 				final double x = data[it.index];
 				if (Double.isNaN(x))
 					data[it.index] = 0;
 				else if (Double.isInfinite(x))
 					data[it.index] = x > 0 ? Double.MAX_VALUE : -Double.MAX_VALUE;
 			}
 		} else if (a instanceof FloatDataset) {
 			final FloatDataset set = (FloatDataset) a;
 			final IndexIterator it = set.getIterator();
 			final float[] data = set.getData();
 			while (it.hasNext()) {
 				final float x = data[it.index];
 				if (Float.isNaN(x))
 					data[it.index] = 0;
 				else if (Float.isInfinite(x))
 					data[it.index] = x > 0 ? Float.MAX_VALUE : -Float.MAX_VALUE;
 			}
 		} else if (a instanceof CompoundDoubleDataset) {
 			final CompoundDoubleDataset set = (CompoundDoubleDataset) a;
 			final int is = set.getElementsPerItem();
 			final IndexIterator it = set.getIterator();
 			final double[] data = set.getData();
 			while (it.hasNext()) {
 				for (int j = 0; j < is; j++) {
 					final double x = data[it.index + j];
 					if (Double.isNaN(x))
 						data[it.index + j] = 0;
 					else if (Double.isInfinite(x))
 						data[it.index + j] = x > 0 ? Double.MAX_VALUE : -Double.MAX_VALUE;
 				}
 			}
 		} else if (a instanceof CompoundFloatDataset) {
 			final CompoundFloatDataset set = (CompoundFloatDataset) a;
 			final int is = set.getElementsPerItem();
 			final IndexIterator it = set.getIterator();
 			final float[] data = set.getData();
 			while (it.hasNext()) {
 				for (int j = 0; j < is; j++) {
 					final float x = data[it.index + j];
 					if (Float.isNaN(x))
 						data[it.index + j] = 0;
 					else if (Float.isInfinite(x))
 						data[it.index + j] = x > 0 ? Float.MAX_VALUE : -Float.MAX_VALUE;
 				}
 			}
 		}
 	}
 
 	/**
 	 * Find absolute index of first value in dataset that is equal to given number
 	 * @param a
 	 * @param n
 	 * @return absolute index (if greater than a.getSize() then no value found)
 	 */
 	public static int findIndexEqualTo(final AbstractDataset a, final double n) {
 		IndexIterator iter = a.getIterator();
 		while (iter.hasNext()) {
 			if (a.getElementDoubleAbs(iter.index) == n)
 				break;
 		}
 
 		return iter.index;
 	}
 
 	/**
 	 * Find absolute index of first value in dataset that is greater than given number
 	 * @param a
 	 * @param n
 	 * @return absolute index (if greater than a.getSize() then no value found)
 	 */
 	public static int findIndexGreaterThan(final AbstractDataset a, final double n) {
 		IndexIterator iter = a.getIterator();
 		while (iter.hasNext()) {
 			if (a.getElementDoubleAbs(iter.index) > n)
 				break;
 		}
 
 		return iter.index;
 	}
 
 	/**
 	 * Find absolute index of first value in dataset that is greater than or equal to given number
 	 * @param a
 	 * @param n
 	 * @return absolute index (if greater than a.getSize() then no value found)
 	 */
 	public static int findIndexGreaterThanOrEqualTo(final AbstractDataset a, final double n) {
 		IndexIterator iter = a.getIterator();
 		while (iter.hasNext()) {
 			if (a.getElementDoubleAbs(iter.index) >= n)
 				break;
 		}
 
 		return iter.index;
 	}
 
 	/**
 	 * Find absolute index of first value in dataset that is less than given number
 	 * @param a
 	 * @param n
 	 * @return absolute index (if greater than a.getSize() then no value found)
 	 */
 	public static int findIndexLessThan(final AbstractDataset a, final double n) {
 		IndexIterator iter = a.getIterator();
 		while (iter.hasNext()) {
 			if (a.getElementDoubleAbs(iter.index) < n)
 				break;
 		}
 
 		return iter.index;
 	}
 
 	/**
 	 * Find absolute index of first value in dataset that is less than or equal to given number
 	 * @param a
 	 * @param n
 	 * @return absolute index (if greater than a.getSize() then no value found)
 	 */
 	public static int findIndexLessThanOrEqualTo(final AbstractDataset a, final double n) {
 		IndexIterator iter = a.getIterator();
 		while (iter.hasNext()) {
 			if (a.getElementDoubleAbs(iter.index) <= n)
 				break;
 		}
 
 		return iter.index;
 	}
 
 	/**
 	 * Roll items over given axis by given amount
 	 * @param a
 	 * @param shift
 	 * @param axis if null, then roll flattened dataset
 	 * @return rolled dataset
 	 */
 	public static AbstractDataset roll(final AbstractDataset a, final int shift, final Integer axis) {
 		AbstractDataset r = AbstractDataset.zeros(a);
 		int is = a.getElementsPerItem();
 		if (axis == null) {
 			IndexIterator it = a.getIterator();
 			int s = r.getSize();
 			int i = shift % s;
 			if (i < 0)
 				i += s;
 			while (it.hasNext()) {
 				r.setObjectAbs(i, a.getObjectAbs(it.index));
 				i += is;
 				if (i >= s) {
 					i %= s;
 				}
 			}
 		} else {
 			PositionIterator pi = a.getPositionIterator(axis);
 			int s = a.shape[axis];
 			AbstractDataset u = AbstractDataset.zeros(is, new int[] {s}, a.getDtype());
 			AbstractDataset v = AbstractDataset.zeros(u);
 			int[] pos = pi.getPos();
 			boolean[] hit = pi.getOmit();
 			while (pi.hasNext()) {
 				a.copyItemsFromAxes(pos, hit, u);
 				int i = shift % s;
 				if (i < 0)
 					i += s;
 				for (int j = 0; j < s; j++) {
 					v.setObjectAbs(i, u.getObjectAbs(j*is));
 					i += is;
 					if (i >= s) {
 						i %= s;
 					}
 				}
 				r.setItemsOnAxes(pos, hit, v.getBuffer());
 			}
 		}
 		return r;
 	}
 
 	/**
 	 * Roll the specified axis backwards until it lies in given position
 	 * @param a
 	 * @param axis The rolled axis (index in shape array). Other axes are left unchanged in relative positions 
 	 * @param start The position with it right of the destination of the rolled axis
 	 * @return dataset with rolled axis
 	 */
 	public static AbstractDataset rollAxis(final AbstractDataset a, int axis, int start) {
 		int r = a.getRank();
 		if (axis < 0)
 			axis += r;
 		if (axis < 0 || axis >= r) {
 			throw new IllegalArgumentException("Axis is out of range: it should be >= 0 and < " + r);
 		}
 		if (start < 0)
 			start += r;
 		if (start < 0 || start > r) {
 			throw new IllegalArgumentException("Start is out of range: it should be >= 0 and <= " + r);
 		}
 		if (axis < start)
 			start--;
 
 		if (axis == start)
 			return a;
 
 		ArrayList<Integer> axes = new ArrayList<Integer>();
 		for (int i = 0; i < r; i++) {
 			if (i != axis) {
 				axes.add(i);
 			}
 		}
 		axes.add(start, axis);
 		int[] aa = new int[r];
 		for (int i = 0; i < r; i++) {
 			aa[i] = axes.get(i);
 		}
 		return a.transpose(aa);
 	}
 }
