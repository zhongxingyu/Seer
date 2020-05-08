 /*
 SLIMPlugin for combined spectral-lifetime image analysis.
 
 Copyright (c) 2010-2013, UW-Madison LOCI
 All rights reserved.
 
 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:
     * Redistributions of source code must retain the above copyright
       notice, this list of conditions and the following disclaimer.
     * Redistributions in binary form must reproduce the above copyright
       notice, this list of conditions and the following disclaimer in the
       documentation and/or other materials provided with the distribution.
     * Neither the name of the UW-Madison LOCI nor the
       names of its contributors may be used to endorse or promote products
       derived from this software without specific prior written permission.
 
 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 POSSIBILITY OF SUCH DAMAGE.
 */
 
 package loci.slim2.decay;
 
 import imagej.data.Dataset;
 import java.util.LinkedHashMap;
 import java.util.Map;
 import net.imglib2.RandomAccess;
 import net.imglib2.meta.AxisType;
 import net.imglib2.type.numeric.RealType;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.display.ARGBScreenImage;
import net.imglib2.display.CompositeXYProjector;
import net.imglib2.display.RealLUTConverter;
import net.imglib2.exception.IncompatibleTypeException;
import net.imglib2.img.ImgPlus;
import net.imglib2.io.ImgIOException;
import net.imglib2.io.ImgIOUtils;
import net.imglib2.io.ImgOpener;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;
 
 /**
  * Wraps a {@link Dataset} that has lifetime information.
  * 
  * @author Aivar Grislis
  */
 public class LifetimeDatasetWrapper {
 	private static final String LIFETIME = "Lifetime";
 	private static final int IMPOSSIBLE_INDEX = -1;
 	private static final int MAX_BIN_WIDTH = 21;
 	private final Dataset dataset;
 	private final RandomAccess<? extends RealType<?>> randomAccess;
 	private final DecayCache cache;
 	private int lifetimeDimension;
 	private final AxisType[] externalAxes;
 	private final long[] internalDimensions;
 	private final long[] externalDimensions;
 	private final int bins;
 	private final int factor;
 	private double inc;
 
 	/**
 	 * Constructor.
 	 * 
 	 * @param dataset 
 	 */
 	public LifetimeDatasetWrapper(Dataset dataset) {
 		this.dataset = dataset;
 		randomAccess = dataset.getImgPlus().randomAccess();
 		cache = new DecayCache(this, MAX_BIN_WIDTH, 1000);
 		
 		// find lifetime axis
 		lifetimeDimension = IMPOSSIBLE_INDEX;
 		AxisType[] axes = dataset.getAxes();
 		externalAxes = new AxisType[axes.length - 1];
 		int i = 0;
 		for (int j = 0; j < axes.length; ++j) {
 			if (LIFETIME.equals(axes[j].getLabel())) {
 				lifetimeDimension = j;
 			}
 			else {
 				externalAxes[i++] = axes[j];
 			}
 		}
 		assert IMPOSSIBLE_INDEX != lifetimeDimension;
  
 		//TODO ARG this could be done with hyperslice cursor to find bin 0 of
 		// decay, another cursor that is limited to cruise the lifetime dim
 		// that gets transformed (or origined) into place.
 		/**
 		MixedTransformView<? extends RealType<?>> r = null;
 		//MixedTransformView<?> r = null;
 		//r = Views.<?>hyperSlice(randomAccess, lifetimeDimension, 0);
 		Object rr = Views.hyperSlice(randomAccess, lifetimeDimension, 0L);**/
 		
      // RandomAccessibleInterval< T > view =
      //     Views.interval( randomAccess, new long[] { 0, 0,1 }, new long[]{ randomAccess.dimension(0), randomAccess.dimension(1),1 } );		
 
 		// get internal (with lifetime) and external (no lifetime) dimensions
 		internalDimensions = dataset.getDims();
 		externalDimensions = new long[internalDimensions.length - 1];
 		i = 0;
 		for (int j = 0; j < internalDimensions.length; ++j) {
 			if (j != lifetimeDimension) {
 				externalDimensions[i++] = internalDimensions[j];
 			}
 		}
 		
 		bins = (int) internalDimensions[lifetimeDimension];
 		
 		factor = 1; //TODO how to get this from metadata?
 		inc = 1.0; //TODO this too is in meta data
 	}
 
 	/**
 	 * Return wrapped {@link Dataset}.
 	 * 
 	 * @return 
 	 */
 	public Dataset getDataset() {
 		return dataset;
 	}
 
 	/**
 	 * Returns external dimensions (i.e. w/o lifetime dimension).
 	 * 
 	 * @return 
 	 */
 	public long[] getDims() {
 		return externalDimensions;
 	}
 
 	/**
 	 * Returns external axis types (i.e. w/o lifetime dimension).
 	 * @return 
 	 */
 	public AxisType[] getAxes() {
 		return externalAxes;
 	}
 
 	/**
 	 * Returns number of lifetime bins.
 	 * 
 	 * @return 
 	 */
 	public int getBins() {
 		return bins;
 	}
 
 	/**
 	 * Gets bin width as time.
 	 * 
 	 * @return 
 	 */
 	public double getTimeIncrement() {
 		return inc;
 	}
 
 	/**
 	 * Gets decay histogram at given location.
 	 * 
 	 * @param x
 	 * @param y
 	 * @param tail rest of position
 	 * @return 
 	 */
 	public double[] getDecay(long x, long y, long[] tail) {
 		long[] position = new long[tail.length + 2];
 		int i = 0;
 		position[i++] = x;
 		position[i++] = y;
 		for (long t : tail) {
 			position[i++] = t;
 		}
 		return getDecay(position);
 	}
 
 	/**
 	 * Gets decay histogram at given location.
 	 * 
 	 * @param position
 	 * @return 
 	 */
 	public double[] getDecay(long[] position) {
 		return getDecayFromInternalPosition(internalPosition(position));
 	}
 
 	/**
 	 * Bins up the decay from surrounding pixels.
 	 * 
 	 * @param binSize 0=no binning, 1=3x3, 2=5x5, etc.
 	 * @param position
 	 * @return 
 	 */
 	public double[] getBinnedDecay(int binSize, long[] position) {
 		if (0 == binSize) {
 			return getDecay(position);
 		}
 		//TODO ARG experimental
 		//DecayCache cache = new DecayCache(this, 1000);
 		double[] summedDecay = new double[bins];
 		for (int i = 0; i < bins; ++i) {
 			summedDecay[i] = 0.0;
 		}
 		final long xAnchor = position[0];
 		final long yAnchor = position[1];
 		double[] decay;
 		for (long x = xAnchor - binSize; x < xAnchor + binSize; ++x) {
 			for (long y = yAnchor - binSize; y < yAnchor + binSize; ++y) {
 				if (x >= 0 && x < getDims()[0]
 						&& y >= 0 && y < getDims()[1]) {
 					position[0] = x;
 					position[1] = y;
 					decay = cache.getDecay(position);
 					for (int i = 0; i < bins; ++i) {
 						summedDecay[i] += decay[i];
 					}
 				}
 			}
 		}
 		return summedDecay;
 	}
 
 	/**
 	 * Gets summed decay histogram at given location, with binning.
 	 * 
 	 * @param position
 	 * @param bins for binning
 	 * @return 
 	 */
 	public int getSummedDecay(long[] position, int bins) {
 		double[] decay = getBinnedDecay(bins, position); //getDecay(position);
 		long sum = 0;
 		for (double d : decay) {
 			sum += d;
 		}
 		// avoid overflow; squash highlights
 		if (sum > Integer.MAX_VALUE) {
 			sum = Integer.MAX_VALUE;
 		}
 		return (int) sum;
 	}
 
 	/**
 	 * Expands external position (without lifetime dimension) to internal
 	 * position (with lifetime dimension).
 	 * 
 	 * @param position
 	 * @return 
 	 */
 	private long[] internalPosition(long[] position) {
 		long[] internalPosition = new long[position.length + 1];
 		for (int i = 0; i < lifetimeDimension; ++i) {
 			internalPosition[i] = position[i];
 		}
 		for (int i = lifetimeDimension + 1; i < internalPosition.length; ++i) {
 			internalPosition[i] = position[i - 1];
 		}
 		return internalPosition;
 	}
 
 	/**
 	 * Gets the decay histogram at given location.
 	 * 
 	 * @param position
 	 * @return 
 	 */
 	private double[] getDecayFromInternalPosition(long[] position) {
 		double[] decay = new double[bins];
 		for (int i = 0; i < bins; ++i) {
 			position[lifetimeDimension] = i;
 			randomAccess.setPosition(position);
 			decay[i] = randomAccess.get().getRealDouble() / factor;
 		}
 		return decay;
 	}
 
 	/**
 	 * Inner cache class.
 	 * 
 	 * Binning is very slow without caching.  However this cache only works well
 	 * if you are accessing pixels sequentially.
 	 * 
 	 * Thanks to Tino at:
 	 * http://www.java-blog.com/creating-simple-cache-java-linkedhashmap-anonymous-class
 	 */
 	private class DecayCache {
 		private final int cacheSize;
 		private final LifetimeDatasetWrapper wrapper;
 		private final long xDim;
 		private final LinkedHashMap<Long, double[]> cache;
 
 		/**
 		 * Constructor.
 		 * 
 		 * @param wrapper source of decays
 		 * @param size e.g. a 3x3 binning would be size 3
 		 * @param xDim dimension of x axis
 		 */
 		public DecayCache(LifetimeDatasetWrapper wrapper, int size, long xDim) {
 			this.wrapper = wrapper;
 			this.cacheSize = size * size;
 			this.xDim = xDim;
 			this.cache = new LinkedHashMap<Long, double[]>(cacheSize, 0.75f, true) {
 				@Override
 				protected boolean removeEldestEntry(Map.Entry<Long, double[]> eldest) {
 					// Remove the eldest entry if the size of the cache exceeds the
 					// maximum size
 					return size() > cacheSize;
 				}
 			};
 		}
 
 		/**
 		 * Clears the cache; should be called for each new plane.
 		 */
 		public void clear() {
 			cache.clear();
 		}
 
 		/**
 		 * Gets the decay at a given position.
 		 * 
 		 * @param position
 		 * @return 
 		 */
 		public double[] getDecay(long[] position) {
 			double[] decay = null;
 			final long x = position[0];
 			final long y = position[1];
 			final long key = x * xDim + y;
 			
 			decay = cache.get(key);
 			
 			if (null == decay) {
 				decay = wrapper.getDecay(position);
 				cache.put(key, decay);
 			}
 			return decay;
 		}
 	}
 }
