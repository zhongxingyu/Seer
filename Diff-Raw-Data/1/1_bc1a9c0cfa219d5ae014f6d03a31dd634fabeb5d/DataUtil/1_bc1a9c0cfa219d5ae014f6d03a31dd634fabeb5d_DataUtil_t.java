 package chordest.util;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.co.labbookpages.WavFile;
 import uk.co.labbookpages.WavFileException;
 
 import chordest.transform.ScaleInfo;
 import chordest.util.metric.EuclideanMetric;
 import chordest.wave.Buffer;
 
 
 public class DataUtil {
 
 	private static final Logger LOG = LoggerFactory.getLogger(DataUtil.class);
 
 	/**
 	 * Fits all the array values to [0, 1] range.
 	 * @param data 2D array
 	 */
 	public static void scaleTo01(double[][] data) {
 		LOG.debug("Performing scaling to [0, 1] ...");
 		double max = Double.MIN_VALUE;
 		double min = Double.MAX_VALUE;
 		for (double[] array : data) {
 			for (double value : array) {
 				if (value > max) { max = value; }
 				if (value < min) { min = value; }
 			}
 		}
 		double d = max - min;
 		for (double[] array : data) {
 			for (int i = 0; i < array.length; i++) {
 				array[i] = (array[i] - min) / d;
 			}
 		}
 	}
 
 	/**
 	 * Fits each column separately to [0, 1] range.
 	 * @param data 2D array
 	 */
 	public static void scaleEachTo01(double[][] data) {
 		for (double[] array : data) {
 			scaleTo01(array);
 		}
 	}
 
 	/**
 	 * Fits all the array values to [0, 1] range.
 	 * @param data 1D array
 	 */
 	public static void scaleTo01(double[] data) {
 		double max = Double.MIN_VALUE;
 		double min = Double.MAX_VALUE;
 		for (double value : data) {
 			if (value > max) { max = value; }
 			if (value < min) { min = value; }
 		}
 		double d = max - min;
 		for (int i = 0; i < data.length; i++) {
 			data[i] = (data[i] - min) / d;
 		}
 	}
 
 	/**
 	 * Sorts the List of Buffers by buffer timestamp
 	 * @param buffers
 	 */
 	public static void sortByTimestamp(List<Buffer> buffers) {
 		Collections.sort(buffers, new Comparator<Buffer>() {
 			public int compare(Buffer o1, Buffer o2) {
 				double t1 = o1.getTimeStamp();
 				double t2 = o2.getTimeStamp();
 				if (Math.abs(t2 - t1) < 1) {
 					return t1 < t2 ? -1 : 1;
 				}
 				return (int) (t1 - t2);
 		} });
 	}
 
 	/**
 	 * Performs horizontal smoothing of the array: replaces each value with
 	 * an arithmetic mean of the value and its <code>window/2</code> left and
 	 * its <code>window/2</code> right successive neighbors.
 	 * @param data Array of columns
 	 * @param window Size of smoothing window
 	 * @return
 	 */
 	public static double[][] smoothHorizontally(final double[][] data, final int window) {
 		if (data == null) {
 			throw new NullPointerException("data is null");
 		}
 		if (window < 0 || window > data.length) {
 			throw new IllegalArgumentException("bad window size: " + window);
 		}
 		LOG.debug("Performing horizontal smooth with window size: " + window);
 		double[][] result = new double[data.length][];
 		int halfWindow = window / 2;
 		for (int col = 0; col < halfWindow; col++) {
 			result[col] = data[col];
 		}
 		for (int col = halfWindow; col < data.length - halfWindow - 1; col++) {
 			result[col] = new double[data[col].length];
 			for (int row = 0; row < data[col].length; row++) {
 				double sum = 0;
 				for (int i = -halfWindow; i <= halfWindow; i++) {
 					sum += data[col + i][row];
 				}
 				result[col][row] = sum / (2 * halfWindow + 1);
 			}
 		}
 		for (int col = data.length - halfWindow - 1; col < data.length; col++) {
 			result[col] = data[col];
 		}
 		return result;
 	}
 
 	/**
 	 * Performs horizontal smoothing of the array: replaces each value with
 	 * the median of the value and its <code>window/2</code> left and
 	 * its <code>window/2</code> right successive neighbors.
 	 * @param data Array of columns
 	 * @param window Size of smoothing window
 	 * @return
 	 */
 	public static double[][] smoothHorizontallyMedian(final double[][] data, final int window) {
 		if (data == null) {
 			throw new NullPointerException("data is null");
 		}
 		if (window < 0) {
 			throw new IllegalArgumentException("Window size must be >= 0 but was: " + window);
 		} else if (window > data.length) {
 			LOG.warn("Window is too large: " + window + ", only " + data.length + " values are available. Skip smoothing.");
 			return data;
 		}
 		LOG.debug("Performing horizontal smooth with window size: " + window);
 		int cols = data.length;
 		double[][] result = new double[cols][];
 		int halfWindow = window / 2;
 		for (int col = 0; col < halfWindow; col++) {
 			result[col] = data[col];
 			result[cols - col - 1] = data[cols - col - 1];
 		}
 
 		for (int col = halfWindow; col < cols - halfWindow; col++) {
 			result[col] = new double[data[col].length];
 			for (int row = 0; row < data[col].length; row++) {
 				double[] array = new double[2 * halfWindow + 1];
 				for (int i = -halfWindow; i <= halfWindow; i++) {
 					array[i + halfWindow] = data[col + i][row];
 				}
 				result[col][row] = findKthSmallest(array, 0, array.length, halfWindow + 1);
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * A variant of Prewitt operator for horizontal edge detection. Tries to
 	 * detect the lines of width <= 3 points. 
 	 * Following matrix is used:
 	 * <table>
 	 * <tr><td>-1</td> <td>-1</td> <td>-1</td></tr>
 	 * <tr><td>-1</td> <td>-1</td> <td>-1</td></tr>
 	 * <tr><td>-1</td> <td>-1</td> <td>-1</td></tr>
 	 * <tr><td>2</td> <td>2</td> <td>2</td></tr>
 	 * <tr><td>2</td> <td>2</td> <td>2</td></tr>
 	 * <tr><td>2</td> <td>2</td> <td>2</td></tr>
 	 * <tr><td>-1</td> <td>-1</td> <td>-1<td/><tr/>
 	 * <tr><td>-1</td> <td>-1</td> <td>-1<td/><tr/>
 	 * <tr><td>-1</td> <td>-1</td> <td>-1<td/><tr/>
 	 * </table>
 	 * 
 	 * @param data
 	 * @return
 	 */
 	public static double[][] filterHorizontal3(final double[][] data) {
 		if (data == null) {
 			throw new NullPointerException("data is null");
 		}
 		LOG.debug("Performing horizontal lines recognition...");
 		double[][] result = new double[data.length][];
 		result[0] = data[0];
 		result[1] = data[1];
 		result[2] = data[2];
 		result[3] = data[3];
 		for (int col = 1; col < data.length - 1; col++) {
 			result[col] = new double[data[col].length];
 			for (int row = 4; row < data[col].length - 4; row++) {
 				double y_4 = data[col-1][row-4] + data[col][row-4] + data[col+1][row-4];
 				double y_3 = data[col-1][row-3] + data[col][row-3] + data[col+1][row-3];
 				double y_2 = data[col-1][row-2] + data[col][row-2] + data[col+1][row-2];
 				double y_1 = data[col-1][row-1] + data[col][row-1] + data[col+1][row-1];
 				double y = data[col-1][row] + data[col][row] + data[col+1][row];
 				double y1 = data[col-1][row+1] + data[col][row+1] + data[col+1][row+1];
 				double y2 = data[col-1][row+2] + data[col][row+2] + data[col+1][row+2];
 				double y3 = data[col-1][row+3] + data[col][row+3] + data[col+1][row+3];
 				double y4 = data[col-1][row+4] + data[col][row+4] + data[col+1][row+4];
 				double dy = -y_4 - y_3 - y_2 + 2*y_1 + 2*y + 2*y1 - y2 - y3 - y4;
 				if (dy > 0) {
 					result[col][row] = data[col][row];
 				} else {
 					result[col][row] = 0;
 				}
 			}
 		}
 		result[data.length - 4] = data[data.length - 4];
 		result[data.length - 3] = data[data.length - 3];
 		result[data.length - 2] = data[data.length - 2];
 		result[data.length - 1] = data[data.length - 1];
 		return result;
 	}
 
 	/**
 	 * Removes the horizontal segments of non-zero values from 2D array
 	 * which have length less or equal to <code>tooShort</code> value.
 	 * @param data
 	 * @param tooShort Threshold value for the segment length
 	 * @return
 	 */
 	public static double[][] removeShortLines(final double[][] data, final int tooShort) {
 		if (data == null) {
 			throw new NullPointerException("data is null");
 		}
 		LOG.debug("Removing short lines...");
 		double[][] result = new double[data.length][];
 		double limit = 1e-5;
 		result[0] = data[0];
 		for (int col = 1; col < data.length; col++) {
 			result[col] = new double[data[col].length];
 			for (int row = 0; row < data[col].length; row++) {
 				double v = data[col][row];
 				if (v > limit && result[col-1][row] > limit) {
 					result[col][row] = v;
 				} else if (v > limit) {
 					int i = col;
 					boolean ok = true;
 					int maxI = Math.min(data.length, col + tooShort);
 					while (i < maxI && ok) {
 						ok = data[i][row] > limit;
 						i++;
 					}
 				    result[col][row] = ok ? v : 0;
 				} else {
 					result[col][row] = 0;
 				}
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * Collapses the spectrum bin that occupies multiple octaves into a
 	 * single octave by simple summing the values that belong to the same
 	 * pitch class over all octaves.
 	 * @param cqtSpectrum One spectrum bin
 	 * @param notesInOctave Number of spectral components per one octave
 	 * @return
 	 */
 	public static double[] toSingleOctave(final double[] cqtSpectrum, final int notesInOctave) {
 		if (cqtSpectrum == null || cqtSpectrum.length == 0) {
 			throw new NullPointerException("cqtSpectrum is null or empty");
 		}
 		if (notesInOctave <= 0 || (cqtSpectrum.length % notesInOctave) != 0) {
 			throw new IllegalArgumentException("Incorrect value of notesInOctave: " + notesInOctave);
 		}
 		if (cqtSpectrum.length == notesInOctave) {
 			return Arrays.copyOf(cqtSpectrum, cqtSpectrum.length);
 		} else {
 			double [] result = new double[notesInOctave];
 			for (int index = 0; index < cqtSpectrum.length; index++) {
 				double value = cqtSpectrum[index];
 				int noteNumber = index % notesInOctave;
 				result[noteNumber] += value;
 			}
 			return result;
 		}
 	}
 
 	/**
 	 * For the array of spectrum vectors returns the array of PCP vectors
 	 * obtained by applying {@link DataUtil.toPitchClassProfiles(double[])}
 	 * to each spectrum vector.
 	 * @param cqtSpectrum the spectrum
 	 * @param notesInOctave Number of spectral components per one octave
 	 * @return array of PCP vectors
 	 */
 	public static double[][] toSingleOctave(final double[][] cqtSpectrum, final int notesInOctave) {
 		if (cqtSpectrum == null) {
 			throw new NullPointerException("data is null");
 		}
 		if (notesInOctave <= 0) {
 			throw new IllegalArgumentException("Incorrect value of notesInOctave: " + notesInOctave);
 		}
 		LOG.debug("Converting to Pitch Class Profiles ...");
 		double[][] result = new double[cqtSpectrum.length][];
 		for (int i = 0; i < cqtSpectrum.length; i++) {
 			result[i] = DataUtil.toSingleOctave(cqtSpectrum[i], notesInOctave);
 		}
 		return result;
 	}
 
 	/**
 	 * Reduces a given spectrum bin having 12*n components per octave to a
 	 * spectrum bin having 12 components per octave. 
 	 * @param data 12*n*octaves-dimensional vector - spectrum bin
 	 * @param octaves Number of octaves in the given bin
 	 * @return
 	 */
 	public static double[] reduce(final double[] data, int octaves) {
 		if (data.length % 12 != 0) {
 			throw new IllegalArgumentException("There must be 12 * N components. " 
 					+ data.length + " is not multiple of 12");
 		}
 		int resultComponents = 12 * octaves;
 		int subnotes = data.length / resultComponents;
 		if (subnotes > 1) {
 			// Reduce pcp array size to 12 if we have more than 12 "notes" in octave
 			// by summing the values of the "subnotes" nearest to each note with weights
 			double [] result = new double[resultComponents];
 			int totalNotes = data.length;
 			int delta = subnotes / 2;
 			if ((subnotes & 1) == 0) { // if subnotesCount is even
 				delta--; // to exclude the subnotes that are in the "middle" between 2 real notes
 			}
 			for (int i = 0; i < resultComponents; i++) {
 				double temp = 0;
 				for (int j = -delta; j <= delta; j++) {
 					// account neighbors with lower weights
 					temp += data[(totalNotes + i * subnotes + j) % totalNotes] * Math.pow(0.6, Math.abs(j));
 				}
 				result[i] = temp;
 			}
 			return result;
 		} else {
 			return Arrays.copyOf(data, data.length);
 		}
 	}
 
 	/**
 	 * Reduces the given spectrum having 12*n components per octave to a
 	 * spectrum having 12 components per octave. 
 	 * @param data array of 12*n*octaves-dimensional vectors - spectrum
 	 * @param octaves Number of octaves in the given spectrum
 	 * @return
 	 */
 	public static double[][] reduce(final double[][] data, int octaves) {
 		if (data == null) {
 			throw new NullPointerException("data is null");
 		}
 		LOG.debug("Reducing to 12 notes per octave ...");
 		double[][] result = new double[data.length][];
 		for (int i = 0; i < data.length; i++) {
 			result[i] = reduce(data[i], octaves);
 		}
 		return result;
 	}
 
 	/**
 	 * Folds the full-sized PCP vector (containing 12 * N components, where
 	 * N is the count of "sub-notes" for each pitch class) into the
 	 * "standard-sized" 12-dimensional vector by combining the components
 	 * nearest to "base" components (corresponding to frequencies equal to
 	 * real note frequencies) into a single component.
 	 * @param pcp 12*N-dimensional vector
 	 * @return 12-dimensional vector
 	 */
 	public static double[] reduceTo12Notes(final double[] pcp) {
 		if (pcp.length % 12 != 0) {
 			throw new IllegalArgumentException("There must be 12 * N subnotes. " 
 					+ pcp.length + " is not multiple of 12");
 		}
 		int subnotesCount = pcp.length / 12;
 		if (subnotesCount > 1) {
 			// Reduce pcp array size to 12 if we have more than 12 "notes" in octave
 			// by summing the values of the "subnotes" nearest to each note with weights
 			double [] pcp12 = new double[12];
 			int totalNotes = pcp.length;
 			int delta = subnotesCount / 2 - 1; // TODO
 			if ((subnotesCount & 1) == 0) { // if subnotesCount is even
 				delta--; // to exclude the subnotes that are in the "middle" between 2 real notes
 			}
			delta = Math.max(delta, 0);
 			for (int i = 0; i < 12; i++) {
 				double temp = 0;
 				for (int j = -delta; j <= delta; j++) {
 					// account neighbors with lower weights
 					temp += pcp[(totalNotes + i * subnotesCount + j) % totalNotes];// * Math.pow(0.6, Math.abs(j));
 				}
 				pcp12[i] = temp;
 			}
 			return pcp12;
 		} else {
 			return Arrays.copyOf(pcp, pcp.length);
 		}
 	}
 
 	/**
 	 * For the array of PCP vectors returns the array of 12-dimensional
 	 * vectors obtained by applying {@link DataUtil.reduceTo12Notes(double[])}
 	 * to each PCP vector
 	 * @param data array of PCP vectors
 	 * @return array of 12-dimensional vectors
 	 */
 	public static double[][] reduceTo12Notes(final double[][] data) {
 		if (data == null) {
 			throw new NullPointerException("data is null");
 		}
 		LOG.debug("Reducing to 12-dimensional vectors ...");
 		double[][] result = new double[data.length][];
 		for (int i = 0; i < data.length; i++) {
 			result[i] = reduceTo12Notes(data[i]);
 		}
 		return result;
 	}
 
 	/**
 	 * Divides the input array sequentially into a set of regions, each
 	 * consisting of <code>step</code> successive columns. Last region will
 	 * consist of not much than <code>step</code> successive columns. Each
 	 * column in resulting array is an arithmetic mean of the columns from
 	 * the corresponding region.
 	 * @param data An array of columns
 	 * @param step Region size
 	 * @return An array of columns
 	 */
 	public static double[][] shrink(final double[][] data, final int step) {
 		if (data == null) {
 			throw new NullPointerException("data is null");
 		}
 		LOG.debug("Shrinking the spectrum ...");
 		int resultLength = data.length / step;
 		if (data.length % step != 0) {
 			resultLength++;
 		}
 		double[][] result = new double[resultLength][];
 		for (int i = 0; i < data.length; i += step) {
 			int left = i;
 //			int right = Math.min(i + step / 2, data.length);
 			double[] temp = data[left];
 //			for (int col = left + 1; col < right; col++) {
 //				double c = (right - col) * 1.0 / (right - left);
 //				temp = add(temp, multiply(data[col], c));
 //			}
 //			temp = multiply(temp, 1.0 / (right - left));
 			result[i / step] = temp;
 		}
 		return result;
 	}
 
 	/**
 	 * For each pair of successive elements in an array inserts intermediate 
 	 * values evenly between them. The resulting array has length times * L - 1
 	 * where L is the length of the array. 
 	 * @param array
 	 * @param times
 	 * @return
 	 */
 	public static double[] makeMoreFrequent(final double[] array, final int times) {
 		if (array == null) {
 			throw new NullPointerException("beatTimes is null");
 		}
 		LOG.debug("Creating the more frequent beat sequence ...");
 		int lastIndex = array.length - 1;
 		double[] result = new double[times * lastIndex + 1];
 		for (int i = 0; i < lastIndex; i++) {
 			double value = array[i];
 			double d = (array[i + 1] - array[i]) / times;
 			for (int j = 0; j < times; j++) {
 				result[times * i + j] = value;
 				value += d;
 			}
 		}
 		result[times * lastIndex] = array[lastIndex];
 		return result;
 	}
 
 	public static double[][] toLogSpectrum(final double[][] data) {
 		if (data == null) {
 			throw new NullPointerException("data is null");
 		}
 		LOG.debug("Calculating the log spectrum ...");
 		double[][] result = new double[data.length][];
 		for (int i = 0; i < data.length; i++) {
 			result[i] = new double[data[i].length];
 			for (int j = 0; j < data[i].length; j++) {
 				result[i][j] = Math.log10(1 + data[i][j] * 1000);
 			}
 		}
 		return result;
 	}
 
 	public static double[][] whitenSpectrum(final double[][] data, final int octaveSize) {
 		if (data == null) {
 			throw new NullPointerException("data is null");
 		}
 		LOG.debug("Performing spectrum whitening ...");
 		double[][] result = new double[data.length][];
 		for (int i = 0; i < data.length; i++) {
 			result[i] = new double[data[i].length];
 			for (int j = 0; j < data[i].length; j++) {
 				int left = Math.max(0, j - octaveSize);
 				int right = Math.min(data[i].length - 1, j + octaveSize);
 				double sum = 0;
 				for (int k = left; k <= right; k++) {
 					sum += data[i][k];
 				}
 				double mean = sum / (right - left + 1);
 				double std = 0;
 				for (int k = left; k <= right; k++) {
 					std += (data[i][k] - mean) * (data[i][k] - mean);
 				}
 				std = Math.sqrt(std / (right - left));
 				result[i][j] = Math.max((data[i][j] - mean) / std, 0);
 			}
 		}
 		return result;
 	}
 
 	public static double[] getFirstOctave(final double[] cqtSpectrum, final ScaleInfo scaleInfo) {
 		return Arrays.copyOf(cqtSpectrum, scaleInfo.notesInOctave);
 	}
 
 	public static double[][] getSelfSimilarity(final double[][] spectrum) {
 		if (spectrum == null) {
 			throw new NullPointerException("spectrum is null");
 		}
 		LOG.debug("Calculating the self-similarity matrix ...");
 		int size = spectrum.length;
 		double[][] result = new double[size][];
 		for (int i = 0; i < size; i++) {
 			result[i] = new double[size];
 		}
 		EuclideanMetric metric = new EuclideanMetric();
 		for (int i = 0; i < size; i++) {
 			double[] v1 = spectrum[i];
 			for (int j = i; j < size; j++) {
 				double[] v2 = spectrum[j];
 				double d = metric.distance(v1, v2);
 				result[i][j] = d;
 				result[j][i] = d;
 			}
 		}
 		DataUtil.scaleTo01(result);
 		return result;
 	}
 
 	public static double[][] removeDissimilar(final double[][] selfSim, final double theta) {
 		if (selfSim == null) {
 			throw new NullPointerException("self similarity matrix is null");
 		}
 		LOG.debug("Removing similarity information for dissimilar elements ...");
 		int size = selfSim.length;
 		int preserved = (int) (size * theta);
 		double[][] result = new double[size][];
 		for (int i = 0; i < size; i++) {
 			result[i] = new double[size];
 		}
 		for (int i = 0; i < size; i++) {
 			double eps = findKthSmallest(selfSim[i], 0, size, preserved);
 //			double eps = theta;
 			for (int j = 0; j < size; j++) {
 				double value = selfSim[i][j] < eps ? selfSim[i][j] : 1;
 				result[i][j] = value;
 //				result[j][i] = value;
 			}
 		}
 		
 		// if i similar to j, but j is not similar to j then they are dissimilar
 		for (int i = 0; i < size; i++) {
 			for (int j = i + 1; j < size; j++) {
 				if (result[i][j] == 1 && result[j][i] < 1) {
 					result[j][i] = 1;
 				} else if (result[i][j] < 1 && result[j][i] == 1) {
 					result[i][j] = 1;
 				}
 			}
 		}
 		return result;
 	}
 
 	public static double[][] smoothWithSelfSimilarity(final double[][] spectrum, final double[][] selfSimilarity) {
 		if (spectrum == null) {
 			throw new NullPointerException("spectrum is null");
 		}
 		if (selfSimilarity == null) {
 			throw new NullPointerException("Self-similarity matrix is null");
 		}
 		LOG.debug("Smoothing the spectrum with diagonal self-similarity matrix ...");
 		int size = spectrum.length;
 		double[][] result = new double[size][];
 		for (int i = 0; i < size; i++) {
 			double[] temp = new double[spectrum[i].length];
 			double sumOfWeights = 0;
 			for (int j = 0; j < size; j++) {
 				double weight = 1 - selfSimilarity[i][j];
 //				weight = weight * weight * weight;
 				temp = add(temp, multiply(spectrum[j], weight));
 				sumOfWeights += weight;
 			}
 			result[i] = multiply(temp, 1.0 / sumOfWeights);
 		}
 		scaleTo01(result);
 		return result;
 	}
 
 	public static double[] add(final double[] a1, final double[] a2) {
 		if (a1.length != a2.length) {
 			throw new IllegalArgumentException("Arrays have different lengths");
 		}
 		double[] result = new double[a1.length];
 		for (int i = 0; i < a1.length; i++) {
 			result[i] = a1[i] + a2[i];
 		}
 		return result;
 	}
 
 	public static double[] multiply(final double[] a, final double c) {
 		double[] result = new double[a.length];
 		if (c != 0) {
 			for (int i = 0; i < a.length; i++) {
 				result[i] = a[i] * c;
 			}
 		}
 		return result;
 	}
 
 	public static double[] sumVectors(final double[][] pcps, final int from, final int to) {
 		if (pcps == null) {
 			throw new NullPointerException("pcps is null");
 		}
 		if (from < 0 || to < 0 || from > to || to > pcps.length) {
 			throw new IllegalArgumentException("from = " + from + ", to = " + to);
 		}
 		double[] result = new double[12];
 		for (int i = from; i < to; i++) {
 			result = DataUtil.add(result, pcps[i]);
 		}
 		return result;
 	}
 
 	/**
 	 * Modification of QSort
 	 * @param a - array
 	 * @param start - index of 1st element in the array (inclusive)
 	 * @param end - index of last element in the array (exclusive)
 	 * @param k - the order of value
 	 * @return k-th smallest element in the given array
 	 */
 	private static double findKthSmallest(final double[] a, final int start, final int end, int k) {
 		int size = end - start;
 		double[] left = new double[size], right = new double[size];
 		int l = 0, r = 0;
 		double d = a[start + size / 2];
 		for (int i = start; i < end; i++) {
 			double v = a[i];
 			if (v < d) { left[l++] = v; }
 			else if (v > d) { right[r++] = v; }
 		}
 		if (k <= l && l > 0) {
 			return findKthSmallest(left, 0, l, k);
 		} else if (k > size - r && r > 0) {
 			return findKthSmallest(right, 0, r,  k - (size - r));
 		} else {
 			return d;
 		}
 	}
 
 	public static double[] generateDefaultBeats(final String wavFilePath) {
 		LOG.warn("Error occured during BeatRoot processing, generating a dummy sequence of beats");
 		WavFile wavFile = null;
 		try {
 			wavFile = WavFile.openWavFile(new File(wavFilePath));
 			int samplingRate = (int) wavFile.getSampleRate();
 			int frames = (int) wavFile.getNumFrames();
 			double totalSeconds = frames * 1.0 / samplingRate;
 			int length = (int) (Math.floor(totalSeconds))* 2;
 			double[] result = new double[length];
 			for (int i = 0; i < length; i++) {
 				result[i] = 0.5 * i;
 			}
 			return result;
 		} catch (WavFileException e) {
 			LOG.error("Error when reading wave file to generate default beat sequence", e);
 		} catch (IOException e) {
 			LOG.error("Error when reading wave file to generate default beat sequence", e);
 		} finally {
 			if (wavFile != null) {
 				try {
 					wavFile.close();
 				} catch (IOException e) {
 					LOG.error("Error when closing wave file after generation of default beat sequence", e);
 				}
 			}
 		}
 		return new double[] { 0 };
 	}
 
 }
