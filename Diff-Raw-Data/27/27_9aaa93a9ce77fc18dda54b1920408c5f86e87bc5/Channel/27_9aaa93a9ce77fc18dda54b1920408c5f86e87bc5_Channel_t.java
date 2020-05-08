 package model;
 
 import model.borderDetector.BorderDetector;
 import model.mask.Mask;
 import model.mask.MaskFactory;
 import model.mask.TwoMaskContainer;
 import org.jfree.data.Range;
 
import java.awt.*;
import java.util.*;
import java.util.List;

 public class Channel implements Cloneable {
 
 	static final int MIN_CHANNEL_COLOR = 0;
 	static final int MAX_CHANNEL_COLOR = 255;
 
 	private int width;
 	private int height;
 
 	// The matrix is represented by an array, and to get a pixel(x,y) make y *
 	// this.getWidth() + x
 	private double[] channel;
 
 	public Channel(int width, int height) {
 		if (width <= 0 || height <= 0) {
 			throw new IllegalArgumentException(
 					"Images must have at least 1x1 pixel size");
 		}
 
 		this.width = width;
 		this.height = height;
 		this.channel = new double[width * height];
 	}
 
 	/**
 	 * Indicates whether a coordinate is valid for a pixel
 	 * 
 	 * @param x
 	 * @param y
 	 * @return True if the pixel is valid
 	 */
 	public boolean validPixel(int x, int y) {
 		boolean validX = x >= 0 && x < this.getWidth();
 		boolean validY = y >= 0 && y < this.getHeight();
 		return validX && validY;
 	}
 
 	/**
 	 * Returns the Channel height
 	 * 
 	 * @return
 	 */
 	public int getHeight() {
 		return height;
 	}
 
 	/**
 	 * Returns the Channel width
 	 * 
 	 * @return
 	 */
 	public int getWidth() {
 		return width;
 	}
 
 	/**
 	 * Sets a pixel(x,y) in the channel
 	 * 
 	 * @param x
 	 * @param y
 	 * @param color
 	 */
 	public void setPixel(int x, int y, double color) {
 		if (!validPixel(x, y)) {
 			throw new IndexOutOfBoundsException();
 		}
 
 		channel[y * this.getWidth() + x] = color;
 	}
 
 	/**
 	 * Returns a pixel in the position x,y
 	 * 
 	 * @param x
 	 * @param y
 	 * @return
 	 */
 	public double getPixel(int x, int y) {
 		if (!validPixel(x, y)) {
 			throw new IndexOutOfBoundsException();
 		}
 
 		return channel[y * this.getWidth() + x];
 	}
 
 	/**
 	 * Truncates a pixel if it is out of vissibly color ranges
 	 * 
 	 * @param originalValue
 	 * @return
 	 */
 	int truncatePixel(double originalValue) {
 		if (originalValue > Channel.MAX_CHANNEL_COLOR) {
 			return Channel.MAX_CHANNEL_COLOR;
 		} else if (originalValue < Channel.MIN_CHANNEL_COLOR) {
 			return Channel.MIN_CHANNEL_COLOR;
 		}
 		return (int) originalValue;
 	}
 
 	public void add(Channel otherChannel) {
 		for (int x = 0; x < width; x++) {
 			for (int y = 0; y < height; y++) {
 				double color = this.getPixel(x, y)
 						+ otherChannel.getPixel(x, y);
 				this.setPixel(x, y, color);
 			}
 		}
 	}
 
 	public void substract(Channel otherChannel) {
 		for (int x = 0; x < width; x++) {
 			for (int y = 0; y < height; y++) {
 				double color = this.getPixel(x, y)
 						- otherChannel.getPixel(x, y);
 				color = Math.abs(color);
 				this.setPixel(x, y, color);
 			}
 		}
 	}
 
 	public void multiply(Channel otherChannel) {
 		for (int x = 0; x < width; x++) {
 			for (int y = 0; y < height; y++) {
 				double color = this.getPixel(x, y)
 						* otherChannel.getPixel(x, y);
 				this.setPixel(x, y, color);
 			}
 		}
 	}
 
 	public void multiply(double scalar) {
 		for (int i = 0; i < this.channel.length; i++) {
 			this.channel[i] *= scalar;
 		}
 	}
 
 	public void dynamicRangeCompression(double R) {
 		double L = MAX_CHANNEL_COLOR;
 		double c = (L - 1) / Math.log(1 + R);
 		for (int x = 0; x < width; x++) {
 			for (int y = 0; y < height; y++) {
 				double r = this.getPixel(x, y);
 				double color = (double) (c * Math.log(1 + r));
 				this.setPixel(x, y, color);
 			}
 		}
 	}
 
 	public void negative() {
 		for (int x = 0; x < width; x++) {
 			for (int y = 0; y < height; y++) {
 				double color = this.getPixel(x, y);
 				this.setPixel(x, y, MAX_CHANNEL_COLOR - color);
 			}
 		}
 	}
 
 	public void threshold(double value) {
 		double black = MIN_CHANNEL_COLOR;
 		double white = MAX_CHANNEL_COLOR;
 
 		for (int x = 0; x < width; x++) {
 			for (int y = 0; y < height; y++) {
 				double colorToApply = this.getPixel(x, y) > value ? white
 						: black;
 				this.setPixel(x, y, colorToApply);
 			}
 		}
 	}
 
 	/**
 	 * Ocurrences[j] = n_j totalPixels = n outGrayLevels[k] = s_k
 	 */
 	public void equalize() {
 		int[] ocurrences = this.getColorOccurrences();
 		int totalPixels = this.channel.length;
 		double[] outGrayLevels = new double[totalPixels];
 		double s_min = MAX_CHANNEL_COLOR;
 		double s_max = MIN_CHANNEL_COLOR;
 
 		for (int i = 0; i < outGrayLevels.length; i++) {
 			int grayLevel = (int) Math.floor(this.channel[i]);
 
 			double outGrayLevel = 0;
 			for (int k = 0; k < grayLevel; k++) {
 				outGrayLevel += ocurrences[k];
 			}
 
 			outGrayLevels[i] = outGrayLevel / totalPixels;
 			s_min = Math.min(s_min, outGrayLevels[i]);
 			s_max = Math.max(s_max, outGrayLevels[i]);
 
 		}
 		for (int i = 0; i < outGrayLevels.length; i++) {
 			double aux = 255 * (outGrayLevels[i] - s_min) / (s_max - s_min);
 			outGrayLevels[i] = Math.ceil(aux);
 		}
 
 		this.channel = outGrayLevels;
 	}
 
 	/**
 	 * Returns an array containing the amount of each gray level. The array is
 	 * ordered
 	 * 
 	 * @return
 	 */
 	private int[] getColorOccurrences() {
 		int[] dataset = new int[Image.GRAY_LEVEL_AMOUNT];
 
 		for (int i = 0; i < this.channel.length; i++) {
 			int grayLevel = (int) Math.floor(this.channel[i]);
 			dataset[grayLevel]++;
 		}
 
 		return dataset;
 	}
 
 	public void contrast(double r1, double r2, double y1, double y2) {
 		for (int x = 0; x < width; x++) {
 			for (int y = 0; y < height; y++) {
 				double r = this.getPixel(x, y);
 
 				double m = 0;
 				double b = 0;
 				if (r < r1) {
 					m = y1 / r1;
 					b = 0;
 				} else if (r > r2) {
 					m = (255 - y2) / (255 - r2);
 					b = y2 - m * r2;
 				} else {
 					m = (y2 - y1) / (r2 - r1);
 					b = y1 - m * r1;
 				}
 				double f = m * r + b;
 
 				this.setPixel(x, y, f);
 			}
 		}
 	}
 
 	public void applyMask(Mask mask) {
 		Channel newChannel = new Channel(this.width, this.height);
 		for (int x = 0; x < width; x++) {
 			for (int y = 0; y < height; y++) {
 				double newPixel = applyMask(x, y, mask);
 				newChannel.setPixel(x, y, newPixel);
 			}
 		}
 		this.channel = newChannel.channel;
 	}
 
 	private double applyMask(int x, int y, Mask mask) {
 		// boolean ignoreByX = x < mask.getWidth() / 2 || x > this.getWidth() -
 		// mask.getWidth() / 2;
 		// boolean ignoreByY = y < mask.getHeight() / 2 || y > this.getHeight()
 		// - mask.getHeight() / 2;
 		// if(ignoreByX || ignoreByY) {
 		// return this.getPixel(x, y);
 		// }
 
 		double newColor = 0;
 		for (int i = -mask.getWidth() / 2; i <= mask.getWidth() / 2; i++) {
 			for (int j = -mask.getHeight() / 2; j <= mask.getHeight() / 2; j++) {
 				if (this.validPixel(x + i, y + j)) {
 					double oldColor = 0;
 					try {
 						oldColor = this.getPixel(x + i, y + j);
 						newColor += oldColor * mask.getValue(i, j);
 					} catch (IndexOutOfBoundsException e) {
 						newColor += oldColor * mask.getValue(i, j);
 					}
 				}
 			}
 		}
 		return newColor;
 	}
 
 	public void applyMedianMask(Point point) {
 		Channel newChannel = new Channel(this.width, this.height);
 		for (int x = 0; x < width; x++) {
 			for (int y = 0; y < height; y++) {
 				double newPixel = applyMedianPixelMask(x, y, point);
 				newChannel.setPixel(x, y, newPixel);
 			}
 		}
 		this.channel = newChannel.channel;
 	}
 
 	private double applyMedianPixelMask(int x, int y, Point point) {
 		TreeSet<Double> pixelsAffected = new TreeSet<Double>();
 		for (int i = -point.x / 2; i <= point.x / 2; i++) {
 			for (int j = -point.y / 2; j <= point.y / 2; j++) {
 				if (this.validPixel(x + i, y + j)) {
 					double oldColor = this.getPixel(x + i, y + j);
 					pixelsAffected.add(oldColor);
 				}
 			}
 		}
 
 		double valueToReturn = 0;
 		int indexToReturn = pixelsAffected.size() / 2;
 		Iterator<Double> iterator = pixelsAffected.iterator();
 		for (int i = 0; iterator.hasNext(); i++) {
 			double each = iterator.next();
 			if (i == indexToReturn) {
 				valueToReturn = each;
 			}
 		}
 		return valueToReturn;
 	}
 
 	public void applyAnisotropicDiffusion(int iterations, BorderDetector bd) {
 		Channel auxiliarChannel = clone();
 
 		for (int t = 0; t < iterations; t++) {
 			auxiliarChannel = applyAnisotropicDiffusion(auxiliarChannel, bd);
 		}
 		this.channel = auxiliarChannel.channel;
 	}
 
 	private Channel applyAnisotropicDiffusion(Channel oldChannel,
 			BorderDetector bd) {
 		Channel modifiedChannel = new Channel(width, height);
 		for (int i = 0; i < width; i++) {
 			for (int j = 0; j < height; j++) {
 				double oldValueIJ = oldChannel.getPixel(i, j);
 
 				double DnIij = i > 0 ? oldChannel.getPixel(i - 1, j)
 						- oldValueIJ : 0;
 				double DsIij = i < width - 1 ? oldChannel.getPixel(i + 1, j)
 						- oldValueIJ : 0;
 				double DeIij = j < height - 1 ? oldChannel.getPixel(i, j + 1)
 						- oldValueIJ : 0;
 				double DoIij = j > 0 ? oldChannel.getPixel(i, j - 1)
 						- oldValueIJ : 0;
 
 				double Cnij = bd.g(DnIij);
 				double Csij = bd.g(DsIij);
 				double Ceij = bd.g(DeIij);
 				double Coij = bd.g(DoIij);
 
 				double DnIijCnij = DnIij * Cnij;
 				double DsIijCsij = DsIij * Csij;
 				double DeIijCeij = DeIij * Ceij;
 				double DoIijCoij = DoIij * Coij;
 
 				double lambda = 0.25;
 				double newValueIJ = oldValueIJ + lambda
 						* (DnIijCnij + DsIijCsij + DeIijCeij + DoIijCoij);
 				modifiedChannel.setPixel(i, j, newValueIJ);
 			}
 		}
 
 		return modifiedChannel;
 	}
 
 	@Override
 	public Channel clone() {
 		Channel newChannel = new Channel(width, height);
 
 		for (int i = 0; i < width; i++) {
 			for (int j = 0; j < height; j++) {
 				newChannel.setPixel(i, j, this.getPixel(i, j));
 			}
 		}
 		return newChannel;
 	}
 
 	public void synthesize(SynthesizationType st, Channel... chnls) {
 		if (st == SynthesizationType.MAX) {
 			synthesize(new SynthesizationFunction() {
 				@Override
 				public double synth(double... color) {
 					double max = color[0];
 					for (double d : color) {
 						max = Math.max(max, d);
 					}
 					return max;
 				}
 			}, chnls);
 			return;
 		}
 		if (st == SynthesizationType.MIN) {
 			synthesize(new SynthesizationFunction() {
 				@Override
 				public double synth(double... color) {
 					double min = color[0];
 					for (double d : color) {
 						min = Math.min(min, d);
 					}
 					return min;
 				}
 			}, chnls);
 			return;
 		}
 		if (st == SynthesizationType.AVG) {
 			synthesize(new SynthesizationFunction() {
 				@Override
 				public double synth(double... color) {
 					double sum = 0;
 					for (double d : color) {
 						sum += d;
 					}
 					return sum / 2;
 				}
 			}, chnls);
 			return;
 		}
 		if (st == SynthesizationType.ABS) {
 			synthesize(new SynthesizationFunction() {
 				@Override
 				public double synth(double... color) {
 					double sum = 0;
 					for (double d : color) {
 						sum += Math.pow(d, 2);
 					}
 					return Math.sqrt(sum);
 				}
 			}, chnls);
 			return;
 		}
 		throw new IllegalStateException();
 	}
 
 	private void synthesize(SynthesizationFunction fn, Channel... chnls) {
 		double[] result = new double[width * height];
 
 		// Iterates through all the pixels of the channel
 		// In every loop takes the i pixel from all the channels and apply the
 		// synth function
 		for (int i = 0; i < channel.length; i++) {
 			double[] colors = new double[chnls.length + 1];
 			colors[0] = this.channel[i];
 			for (int j = 1; j <= chnls.length; j++) {
 				colors[j] = chnls[j - 1].channel[i];
 			}
 			result[i] = fn.synth(colors);
 		}
 		this.channel = result;
 	}
 
 	public void localVarianceEvaluation(int variance) {
 		Channel chnl = new Channel(this.width, this.height);
 		for (int i = 1; i < this.channel.length; i++) {
 			double n1 = channel[i - 1];
 			double n2 = channel[i];
 			if (n1 > 0 && n2 < 0 && Math.abs(n1 - n2) > variance) {
 				chnl.channel[i] = MAX_CHANNEL_COLOR;
 			} else if (n1 < 0 && n2 > 0 && Math.abs(-n1 + n2) > variance) {
 				chnl.channel[i] = MAX_CHANNEL_COLOR;
 			} else {
 				chnl.channel[i] = MIN_CHANNEL_COLOR;
 			}
 		}
 		this.channel = chnl.channel;
 	}
 
 	public void zeroCross(double threshold) {
 
 		double[] hChannel = new double[channel.length];
 		double[] vChannel = new double[channel.length];
 
 		double last;
 		for (int x = 0; x < this.getWidth(); x++) {
 			for (int y = 0; y < this.getHeight(); y++) {
 				double past = 0;
 				double current = 0;
 
 				if (x + 1 < width) {
 					current = this.getPixel(x + 1, y);
 					last = past;
 					past = this.getPixel(x, y);
 
 					if (past == 0 && x > 0) {
 						past = last;
 					}
 
 					if (((current < 0 && past > 0) || (current > 0 && past < 0))
 							&& Math.abs(current - past) > threshold) {
 						vChannel[y * this.getWidth() + x] = MAX_CHANNEL_COLOR;
 					}
 				}
 			}
 		}
 		for (int x = 0; x < this.getWidth(); x++) {
 			for (int y = 0; y < this.getHeight(); y++) {
 				double past = 0;
 				double current = 0;
 				if (y + 1 < height) {
 					current = this.getPixel(x, y + 1);
 					last = past;
 					past = this.getPixel(x, y);
 
 					if (past == 0 && x > 0) {
 						past = last;
 					}
 
 					if (((current < 0 && past > 0) || (current > 0 && past < 0))
 							&& Math.abs(current - past) > threshold) {
 						vChannel[y * this.getWidth() + x] = MAX_CHANNEL_COLOR;
 					}
 				}
 			}
 		}
 		for (int x = 0; x < this.getWidth(); x++) {
 			for (int y = 0; y < this.getHeight(); y++) {
 				vChannel[y * this.getWidth() + x] += hChannel[y
 						* this.getWidth() + x];
 				if (vChannel[y * this.getWidth() + x] > 255) {
 					vChannel[y * this.getWidth() + x] = 255;
 				}
 			}
 		}
 
 		this.channel = vChannel;
 	}
 
 	public void globalThreshold() {
 		double globalThreshold = getGlobalThresholdValue();
 		threshold(globalThreshold);
 	}
 
 	private double getGlobalThresholdValue() {
 		double deltaT = 1;
 		// Step 1
 		double currentT = 128;
 		double previousT = 0;
 
 		// Step 5
 		int i = 0;
 		do {
 			previousT = currentT;
 			currentT = getAdjustedThreshold(currentT);
 			i++;
 		} while (Math.abs((currentT - previousT)) >= deltaT);
 		System.out.println("Iteraciones: " + i);
 		System.out.println("T: " + currentT);
 		return currentT;
 	}
 
 	/**
 	 * Calculates de new T.
 	 * 
 	 * @param previousThreshold
 	 * @return new threshold
 	 */
 	private double getAdjustedThreshold(double previousThreshold) {
 		double amountOfHigher = 0;
 		double amountOfLower = 0;
 
 		double sumOfHigher = 0;
 		double sumOfLower = 0;
 
 		// Step 3
 		for (int x = 0; x < width; x++) {
 			for (int y = 0; y < height; y++) {
 				double aPixel = this.getPixel(x, y);
 				if (aPixel >= previousThreshold) {
 					amountOfHigher += 1;
 					sumOfHigher += aPixel;
 				}
 				if (aPixel < previousThreshold) {
 					amountOfLower += 1;
 					sumOfLower += aPixel;
 				}
 			}
 		}
 		double n1 = amountOfHigher;
 		double n2 = amountOfLower;
 		double m1 = (1 / n1) * sumOfHigher;
 		double m2 = (1 / n2) * sumOfLower;
 
 		// Step 4
 		return 0.5 * (m1 + m2);
 	}
 
 	public void otsuThreshold() {
 		double maxSigma = 0;
 		int threshold = 0;
 		double[] probabilities = getProbabilitiesOfEachColorLevel();
 		for (int i = 0; i < MAX_CHANNEL_COLOR; i++) {
 			double aSigma = getSigma(i, probabilities);
 			if (aSigma > maxSigma) {
 				maxSigma = aSigma;
 				threshold = i;
 			}
 		}
 		threshold(threshold);
 	}
 
 	private double getSigma(int threshold, double[] probabilities) {
 		double w1 = 0;
 		double w2 = 0;
 		for (int i = 0; i < probabilities.length; i++) {
 			if (i <= threshold) {
 				w1 += probabilities[i];
 			} else {
 				w2 += probabilities[i];
 			}
 		}
 
 		if (w1 == 0 || w2 == 0) {
 			return 0;
 		}
 
 		double mu1 = 0;
 		double mu2 = 0;
 		for (int i = 0; i < probabilities.length; i++) {
 			if (i <= threshold) {
 				mu1 += i * probabilities[i] / w1;
 			} else {
 				mu2 += i * probabilities[i] / w2;
 			}
 		}
 
 		double mu_t = mu1 * w1 + mu2 * w2;
 		double sigma_B = w1 * Math.pow((mu1 - mu_t), 2) + w2
 				* Math.pow((mu2 - mu_t), 2);
 		return sigma_B;
 	}
 
 	private double[] getProbabilitiesOfEachColorLevel() {
 		double[] probabilities = new double[MAX_CHANNEL_COLOR + 1];
 
 		for (int x = 0; x < width; x++) {
 			for (int y = 0; y < height; y++) {
 				int aColorPixel = (int) this.getPixel(x, y);
 
 				probabilities[aColorPixel] += 1;
 			}
 		}
 		for (int i = 0; i < probabilities.length; i++) {
 			probabilities[i] /= (width * height);
 		}
 
 		return probabilities;
 	}
 
 	public void houghTransformForLines(double epsilon) {
 
 		double whiteColor = MAX_CHANNEL_COLOR;
 		double D = Math.max(width, height);
 		Range roRange = new Range(-Math.sqrt(2) * D, Math.sqrt(2) * D);
 		Range thetaRange = new Range(-90, 90);
 		int roSize = (int) (Math.abs(roRange.getLength()));
 		int thetaSize = (int) (Math.abs(thetaRange.getLength()));
 		int[][] A = new int[roSize][thetaSize];
 
 		// Step 3
 		for (int x = 0; x < width; x++) {
 			for (int y = 0; y < height; y++) {
 				double pixel = getPixel(x, y);
 				if (pixel == whiteColor) {
 
 					// Iterates theta (j) from 1 to m
 					for (int theta = 0; theta < thetaSize; theta++) {
 						double thetaValue = thetaRange.getLowerBound() + theta;
 						double thetaTerm = x
 								* Math.cos(thetaValue * Math.PI / 180) - y
 								* Math.sin(thetaValue * Math.PI / 180);
 
 						// Iterates ro (i) from 1 to n
 						for (int ro = 0; ro < roSize; ro++) {
 							double roValue = roRange.getLowerBound() + ro;
 							double total = roValue - thetaTerm;
 							// If verifies the normal equation of the line, add
 							// 1 to the acumulator
 							// Step 4
 							if (Math.abs(total) < epsilon) {
 								// The maximum values from this vector, gives
 								// the most voted positions.
 								A[ro][theta] += 1;
 							}
 						}
 					}
 				}
 			}
 		}
 
 		// Step 5
 		Set<BucketForLines> allBuckets = new HashSet<BucketForLines>();
 		for (int ro = 0; ro < roSize; ro++) {
 			for (int theta = 0; theta < thetaSize; theta++) {
 				BucketForLines newBucket = new BucketForLines(ro, theta,
 						A[ro][theta]);
 				allBuckets.add(newBucket);
 			}
 		}
 
 		// Generates a descending sorted list.
 		List<BucketForLines> allBucketsAsList = new ArrayList<BucketForLines>(
 				allBuckets);
 		Collections.sort(allBucketsAsList);
 
 		Channel newChannel = new Channel(width, height);
 		// Gets the max vote number
 		int maxVotes = allBucketsAsList.get(0).votes;
 		if (maxVotes > 1) {
 			for (BucketForLines b : allBucketsAsList) {
 
 				// Only for those with max votes
 				if (b.votes < maxVotes) {
 					break;
 				}
 
 				double roValue = roRange.getLowerBound() + b.ro;
 				double thetaValue = thetaRange.getLowerBound() + b.theta;
 
 				for (int x = 0; x < width; x++) {
 					for (int y = 0; y < height; y++) {
 						double thetaTerm = x
 								* Math.cos(thetaValue * Math.PI / 180) - y
 								* Math.sin(thetaValue * Math.PI / 180);
 						double total = roValue - thetaTerm;
 						// Step 6
 						if (Math.abs(total) < epsilon && validPixel(x, y)) {
 							newChannel.setPixel(x, y, whiteColor);
 						}
 					}
 				}
 
 			}
 		}
 		this.channel = newChannel.channel;
 	}
 
 	public void houghTransformForCircles(double epsilon) {
 
 		double whiteColor = MAX_CHANNEL_COLOR;
 		Range aRange = new Range(0, width);
 		Range bRange = new Range(0, height);
 
 		// TODO: Verificar si no es el min aca
 		double maxRad = Math.max(width, height);
 		Range rRange = new Range(epsilon, maxRad);
 
 		int aSize = (int) (Math.abs(aRange.getLength()));
 		int bSize = (int) (Math.abs(bRange.getLength()));
 		int rSize = (int) (Math.abs(rRange.getLength()));
 		int[][][] A = new int[aSize][bSize][rSize];
 
 		for (int x = 0; x < width; x++) {
 			for (int y = 0; y < height; y++) {
 				double pixel = getPixel(x, y);
 				if (pixel == whiteColor) {
 					for (int a = 0; a < aSize; a++) {
 						double aValue = aRange.getLowerBound() + a;
 						double aTerm = Math.pow(x - aValue, 2);
 						for (int b = 0; b < bSize; b++) {
 							double bValue = bRange.getLowerBound() + b;
 							double bTerm = Math.pow(y - bValue, 2);
 							for (int r = 0; r < rSize; r++) {
 								double rValue = rRange.getLowerBound() + r;
 								double rTerm = Math.pow(rValue, 2);
 								double total = rTerm - aTerm - bTerm;
 								if (Math.abs(total) < epsilon) {
 									A[a][b][r] += 1;
 								}
 							}
 						}
 					}
 				}
 			}
 		}
 
 		Set<BucketForCircles> allBuckets = new HashSet<BucketForCircles>();
 		for (int a = 0; a < aSize; a++) {
 			for (int b = 0; b < bSize; b++) {
 				for (int r = 0; r < rSize; r++) {
 					if (A[a][b][r] > 0) {
 						BucketForCircles newBucket = new BucketForCircles(a, b,
 								r, A[a][b][r]);
 						allBuckets.add(newBucket);
 					}
 				}
 			}
 		}
 
 		if (allBuckets.isEmpty())
 			return;
 
 		List<BucketForCircles> allBucketsAsList = new ArrayList<BucketForCircles>(
 				allBuckets);
 		Collections.sort(allBucketsAsList);
 
 		Channel newChannel = new Channel(width, height);
 		int maxHits = allBucketsAsList.get(0).votes;
 		if (maxHits > 2)
 			for (BucketForCircles b : allBucketsAsList) {
 				if (b.votes < maxHits) {
 					break;
 				}
 				double aValue = aRange.getLowerBound() + b.a;
 
 				double bValue = bRange.getLowerBound() + b.b;
 				double rValue = rRange.getLowerBound() + b.r;
 				for (int x = 0; x < width; x++) {
 					for (int y = 0; y < height; y++) {
 						double aTerm = Math.pow(x - aValue, 2);
 						double bTerm = Math.pow(y - bValue, 2);
 						double rTerm = Math.pow(rValue, 2);
 						double total = rTerm - aTerm - bTerm;
 						if (Math.abs(total) < 10 * epsilon && validPixel(x, y)) {
 							newChannel.setPixel(x, y, whiteColor);
 						}
 					}
 				}
 
 			}
 
 		this.channel = newChannel.channel;
 	}
 
 	public void applyCannyBorderDetection() {
 		List<Channel> channelList = new ArrayList<Channel>();
 		for (int maskSize = 3; maskSize <= 5; maskSize += 2) {
 			for (double sigma = 0.05; sigma <= 0.25; sigma += 0.05) {
 				Channel each = applyCannyBorderDetection(maskSize, sigma);
 				channelList.add(each);
 			}
 		}
 
 		Channel initialChannel = channelList.get(0);
 		Channel[] restOfChannels = channelList.subList(1, channelList.size())
 				.toArray(new Channel[channelList.size() - 1]);
 		initialChannel.synthesize(SynthesizationType.MAX, restOfChannels);
 		this.channel = initialChannel.channel;
 	}
 
 	private Channel applyCannyBorderDetection(int maskSize, double sigma) {
 		Channel channelToModify = clone();
 		channelToModify.applyMask(MaskFactory
 				.buildGaussianMask(maskSize, sigma));
 
 		// Step 2: Applies sobel masks.
 		TwoMaskContainer mc = MaskFactory.buildSobelMasks();
 		Channel G1 = channelToModify.clone();
 		G1.applyMask(mc.getDXMask());
 		Channel G2 = channelToModify.clone();
 		G2.applyMask(mc.getDYMask());
 
 		// Step 3: Get gradient angle to estimate the perpendicular direction to
 		// the border.
 		Channel direction = new Channel(width, height);
 		for (int x = 0; x < width; x++) {
 			for (int y = 0; y < height; y++) {
 				double pxG1 = G1.getPixel(x, y);
 				double pxG2 = G2.getPixel(x, y);
 				double anAngle = 0;
 				if (pxG2 != 0) {
 					anAngle = Math.atan(pxG1 / pxG2);
 				}
 				anAngle *= (180 / Math.PI);
 				direction.setPixel(x, y, anAngle);
 			}
 		}
 
 		G1.synthesize(SynthesizationType.ABS, G2);
 		channelToModify.channel = G1.channel;
 
        // TODO: Add parameters for threshold, sure we need to use a global threshold?
 		channelToModify.suppressNoMaxs(direction);
 		double globalThresholdValue = channelToModify.getGlobalThresholdValue();
 		channelToModify.thresholdWithHysteresis(globalThresholdValue,
 				globalThresholdValue + 30);
 		return channelToModify;
 	}
 
 	public void suppressNoMaxs() {
 		suppressNoMaxs(this);
 	}
 
 	private void suppressNoMaxs(Channel directionChannel) {
 		for (int x = 1; x < width - 1; x++) {
 			for (int y = 1; y < height - 1; y++) {
 				double pixel = getPixel(x, y);
 				if (pixel == MIN_CHANNEL_COLOR) {
 					continue;
 				}
 
 				double direction = directionChannel.getPixel(x, y);
 				double neighbor1 = 0;
 				double neighbor2 = 0;
 				// Get neighbours
 				if (direction >= -90 && direction < -45) {
 					neighbor1 = getPixel(x, y - 1);
 					neighbor2 = getPixel(x, y + 1);
 				} else if (direction >= -45 && direction < 0) {
 					neighbor1 = getPixel(x + 1, y - 1);
 					neighbor2 = getPixel(x - 1, y + 1);
 				} else if (direction >= 0 && direction < 45) {
 					neighbor1 = getPixel(x + 1, y);
 					neighbor2 = getPixel(x - 1, y);
 				} else if (direction >= 45 && direction <= 90) {
 					neighbor1 = getPixel(x + 1, y + 1);
 					neighbor2 = getPixel(x - 1, y - 1);
 				}
 
 				// If neighbours are greater than the pixels, erase them from
 				// borders.
 				if (neighbor1 > pixel || neighbor2 > pixel) {
 					setPixel(x, y, MIN_CHANNEL_COLOR);
 				}
 			}
 		}
 	}
 
 	public void thresholdWithHysteresis(double t1, double t2) {
 		double blackColor = MIN_CHANNEL_COLOR;
 		double whiteColor = MAX_CHANNEL_COLOR;
 
 		Channel thresholdedChannelOutsider = clone();
 		for (int x = 0; x < width; x++) {
 			for (int y = 0; y < height; y++) {
 				double pixel = this.getPixel(x, y);
 				double colorToApply = pixel;
 				if (pixel < t1) {
 					// Incorrect pixels
 					colorToApply = blackColor;
 				} else if (pixel > t2) {
 					// Correct pixels (Border pixels)
 					colorToApply = whiteColor;
 				}
 				thresholdedChannelOutsider.setPixel(x, y, colorToApply);
 			}
 		}
 
 		Channel thresholdedChannelInBetween = thresholdedChannelOutsider
 				.clone();
 		for (int x = 0; x < width; x++) {
 			for (int y = 0; y < height; y++) {
 				double pixel = this.getPixel(x, y);
 				if (pixel >= t1 && pixel <= t2) {
 					// Analyze if the pixel is neighbour of a border (a correct
 					// pixel)
 					boolean isBorderNeighbor1 = y > 0
 							&& thresholdedChannelOutsider.getPixel(x, y - 1) == whiteColor;
 					boolean isBorderNeighbor2 = x > 0
 							&& thresholdedChannelOutsider.getPixel(x - 1, y) == whiteColor;
 					boolean isBorderNeighbor3 = y < height - 1
 							&& thresholdedChannelOutsider.getPixel(x, y + 1) == whiteColor;
 					boolean isBorderNeighbor4 = x < width - 1
 							&& thresholdedChannelOutsider.getPixel(x + 1, y) == whiteColor;
 					if (isBorderNeighbor1 || isBorderNeighbor2
 							|| isBorderNeighbor3 || isBorderNeighbor4) {
 						thresholdedChannelInBetween.setPixel(x, y, whiteColor);
 					} else {
 						thresholdedChannelInBetween.setPixel(x, y, blackColor);
 					}
 				}
 			}
 		}
 
 		this.channel = thresholdedChannelInBetween.channel;
 	}
 
 	public void applySusanMask(boolean detectBorders, boolean detectCorners) {
 		double blackColor = MIN_CHANNEL_COLOR;
 		double whiteColor = MAX_CHANNEL_COLOR;
 
 		Mask mask = MaskFactory.buildSusanMask();
 		Channel newChannel = new Channel(this.width, this.height);
 		for (int x = 0; x < width; x++) {
 			for (int y = 0; y < height; y++) {
 				double newPixelValue = blackColor;
 				double s_ro = applySusanPixelMask(x, y, mask);
				if ((detectBorders && isBorder(s_ro)) || (detectCorners
						&& isCorner(s_ro))) {
 					newPixelValue = whiteColor;
 				}
 				newChannel.setPixel(x, y, newPixelValue);
 			}
 		}
 		this.channel = newChannel.channel;
 	}
 
 	// S_ro ~= 0.5
 	private boolean isBorder(double s_ro) {
 		double lowLimit = 0.5 - (0.75 - 0.5) / 2;
 		double highLimit = 0.5 + (0.75 - 0.5) / 2;
 
 		return s_ro > lowLimit && s_ro <= highLimit;
 	}
 
 	// S_ro ~= 0.75
 	private boolean isCorner(double s_ro) {
 		double lowLimit = 0.75 - (0.75 - 0.5) / 2;
 		double highLimit = 0.75 + (0.75 - 0.5) / 2;
 
 		return s_ro > lowLimit && s_ro <= highLimit;
 	}
 
 	private double applySusanPixelMask(int x, int y, Mask mask) {
 		boolean ignoreByX = x < mask.getWidth() / 2
 				|| x > this.getWidth() - mask.getWidth() / 2;
 		boolean ignoreByY = y < mask.getHeight() / 2
 				|| y > this.getHeight() - mask.getHeight() / 2;
 		if (ignoreByX || ignoreByY) {
			return 1;
 		}
 
 		// Step 2.
 		final int threshold = 27;
 		int n_ro = 0;
 		double ro = this.getPixel(x, y);
 		for (int i = -mask.getWidth() / 2; i <= mask.getWidth() / 2; i++) {
 			for (int j = -mask.getHeight() / 2; j <= mask.getHeight() / 2; j++) {
 				if (this.validPixel(x + i, y + j) && mask.getValue(i, j) == 1) {
 					double eachPixel = this.getPixel(x + i, y + j);
 					if (Math.abs(ro - eachPixel) < threshold) {
 						n_ro += 1;
 					}
 				}
 			}
 		}
 
 		final double N = 37.0;
 		double s_ro = 1 - n_ro / N;
 		return s_ro;
 	}
 }
