 /*-
  * Copyright 2013 Diamond Light Source Ltd.
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
 
 
 /**
  * Class to run over a contiguous dataset using strides
  */
 public class StrideIterator extends SliceIterator {
 	private int[] stride;
 	private int[] delta;  // reset values
 	private int nstart;
 
 	public StrideIterator(final int[] shape) {
 		this(shape, null, 0);
 	}
 
 	public StrideIterator(final int isize, final int[] shape) {
 		this(isize, shape, null, 0);
 	}
 
 	public StrideIterator(final int[] shape, final int[] strides) {
 		this(shape, strides, 0);
 	}
 
 	public StrideIterator(final int[] shape, final int[] strides, final int offset) {
 		this(1, shape, strides, offset);
 	}
 
 	public StrideIterator(final int isize, final int[] shape, final int[] strides, final int offset) {
 		init(isize, shape, strides, offset);
 		reset();
 	}
 
 	public StrideIterator(final int isize, final int[] shape, final int[] start, final int[] stop, final int[] step) {
 		this(isize, shape, null, 0, start, stop, step);
 	}
 
 	public StrideIterator(final int isize, final int[] shape, final int[] oStrides, final int oOffset, final int[] start, final int[] stop, final int[] step) {
 		int rank = shape.length;
 		int[] strides = new int[rank];
 		int[] offset = new int[1];
 		int[] newShape = AbstractDataset.createStrides(isize, shape, oStrides, oOffset, start, stop, step, strides, offset);
 
 		init(isize, newShape, strides, offset[0]);
 		reset();
 	}
 
 	private void init(final int isize, final int[] shape, final int[] strides, final int offset) {
		this.isize = isize;
 		istep = isize;
 		this.shape = shape;
 		int rank = shape.length;
 		endrank = rank - 1;
 		start = new int[rank];
 		delta = new int[rank];
 		if (strides != null) {
 			stride = strides;
 			for (int j = endrank; j >= 0; j--) {
 				delta[j] = stride[j] * shape[j];
 			}
			if (endrank < 0) {
				imax = istep;
			} else {
				imax = 0; // use max delta
				for (int j = endrank; j >= 0; j--) {
					if (delta[j] > imax) {
						imax = delta[j];
					}
				}
			}
 		} else {
 			stride = new int[rank];
 			int s = isize;
 			for (int j = endrank; j >= 0; j--) {
 				stride[j] = s;
 				s *= shape[j];
 				delta[j] = s;
 			}
 			imax = s;
 		}
 		nstart = offset;
 		imax += nstart;
 	}
 
 	@Override
 	void calcGap() {
 		// do nothing
 	}
 
 	@Override
 	public int[] getShape() {
 		return shape;
 	}
 
 	@Override
 	public boolean hasNext() {
 		// now move on one position
 		int j = endrank;
 		for (; j >= 0; j--) {
 			pos[j]++;
 			index += stride[j];
 			if (pos[j] >= shape[j]) {
 				pos[j] = start[0];
 				index -= delta[j]; // reset this dimension
 			} else {
 				break;
 			}
 		}
 		if (j == -1) {
 			if (endrank >= 0) {
 				index = imax;
 				return false;
 			}
 			index += istep;
 		}
 
 		return index != imax;
 	}
 
 	@Override
 	public int[] getPos() {
 		return pos;
 	}
 
 	@Override
 	public void reset() {
 		pos = start.clone();
 		if (endrank >= 0) {
			pos[endrank] = -1;
 			index = nstart;
 			int j = endrank;
 			for (; j >= 0; j--) {
 				index += pos[j]*stride[j];
 			}
 		} else {
 			index = -istep;
 		}
 	}
 }
