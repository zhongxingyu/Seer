 package model;
 
 import java.awt.Point;
 import java.util.Iterator;
 import java.util.TreeSet;
 
 import model.mask.Mask;
 
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
 			double aux = 255*(outGrayLevels[i] - s_min) / (s_max - s_min);
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
 		for( int x = 0 ; x < width ; x++ ){
 			for( int y = 0 ; y < height ; y++){
 				double r = this.getPixel(x, y);
 				
 				double m = 0;
 				double b = 0;
 				if(r < r1) {
 					m = y1 / r1;
 					b = 0;
 				} else if(r > r2) {
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
 	
 	public void applyMask(Mask mask){
 		Channel newChannel = new Channel(this.width, this.height);
 		for( int x = 0 ; x < width ; x++ ){
 			for( int y = 0 ; y < height ; y++){
 				double newPixel = applyMask(x, y, mask);
 				newChannel.setPixel(x, y, newPixel);
 			}
 		}
 		this.channel = newChannel.channel;
 	}
 	
 	private double applyMask(int x, int y, Mask mask) {
//		boolean ignoreByX = x < mask.getWidth() / 2 || x > this.getWidth() - mask.getWidth() / 2;
//		boolean ignoreByY = y < mask.getHeight() / 2 || y > this.getHeight() - mask.getHeight() / 2;
//		if(ignoreByX || ignoreByY) {
//			return this.getPixel(x, y);
//		}
 		
 		double newColor = 0;
		double previousColor = 255;
 		for(int i = - mask.getWidth() / 2 ; i <= mask.getWidth() / 2; i++) {
 			for(int j = - mask.getHeight() / 2; j <= mask.getHeight() / 2; j++) {
//				if(this.validPixel(x + i, y + j)) {
				double oldColor = previousColor;
				try {
					oldColor = this.getPixel(x + i, y + j);
					previousColor = oldColor;
					newColor += oldColor * mask.getValue(i, j);
				} catch (IndexOutOfBoundsException e) {
 					newColor += oldColor * mask.getValue(i, j);
 				}
//				}
 			}
 		}
 		return newColor;
 	}
 	
 	public void applyMedianMask(Point point) {
 		Channel newChannel = new Channel(this.width, this.height);
 		for( int x = 0 ; x < width ; x++ ){
 			for( int y = 0 ; y < height ; y++){
 				double newPixel = applyMedianPixelMask(x, y, point);
 				newChannel.setPixel(x, y, newPixel);
 			}
 		}
 		this.channel = newChannel.channel;
 	}
 	
 	private double applyMedianPixelMask(int x, int y, Point point) {
 		TreeSet<Double> pixelsAffected = new TreeSet<Double>(); 
 		for(int i = - point.x / 2 ; i <= point.x / 2; i++) {
 			for(int j = - point.y / 2; j <= point.y / 2; j++) {
 				if(this.validPixel(x + i, y + j)) {
 					double oldColor = this.getPixel(x + i, y + j); 
 					pixelsAffected.add(oldColor);
 				}
 			}
 		}
 		
 		double valueToReturn = 0;
 		int indexToReturn = pixelsAffected.size() / 2;
 		Iterator<Double> iterator = pixelsAffected.iterator();
 		for(int i = 0; iterator.hasNext(); i++) {
 			double each = iterator.next();
 			if(i == indexToReturn) {
 				valueToReturn = each;
 			}
 		}
 		return valueToReturn;
 	}
 }
