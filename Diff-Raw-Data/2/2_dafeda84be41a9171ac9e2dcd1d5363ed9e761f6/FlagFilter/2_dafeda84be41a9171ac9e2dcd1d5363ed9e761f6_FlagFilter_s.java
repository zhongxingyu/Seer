 package org.gvlabs.image.utils.filter;
 
 import java.awt.image.BufferedImage;
 import java.util.Arrays;
 
 /**
  * Flag Filter
  * 
  * @author Thiago Galbiatti Vespa - <a
  *         href="mailto:thiago@thiagovespa.com.br">thiago@thiagovespa.com.br</a>
  * @version 0.7
  * 
  */
 public class FlagFilter implements ImageFilter {
 
 	private boolean xAxis;
 	private boolean yAxis;
 
 	/**
 	 * Default constructor.
 	 * The filter is applied under x and y axis
 	 */
 	public FlagFilter() {
 		this.xAxis = true;
 		this.yAxis = true;
 	}
 
 	/**
 	 * Constructor that defines what axis to aply the filter
 	 * 
 	 * @param x
 	 *            x (horizontal) axis
 	 * @param y
 	 *            y (vertical) axis
 	 */
 	public FlagFilter(boolean x, boolean y) {
 		this.xAxis = x;
 		this.yAxis = y;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.gvlabs.image.utils.filter.ImageFilter#applyTo(java.awt.image.BufferedImage)
 	 */
 	@Override
 	public BufferedImage applyTo(BufferedImage src) {
 
 		int w = src.getWidth();
 		int h = src.getHeight();
 
 		// TODO: Remove unnecessary matrix
 		int[][] resultMatrix = new int[w][h];
 
 		// TODO: Optimize: Maybe PixelGrabber
 		
 		// copy px to matrix
 		for (int i = 0; i < w; i++) {
 			for (int j = 0; j < h; j++) {
 				int value = src.getRGB(i, j);
 				resultMatrix[i][j] = value;
 			}
 		}
 
 		int[] transformY = new int[w];
 
 		if (this.yAxis) {
 
 			// cosine - horizontal
 			for (int i = 0; i < w; i++) {
 				// TODO: parameters
 				double x = (i / (double) (w - 1)) * 3 * Math.PI;
 				double valor = 0.27 * (((Math.cos(x) + 1) * ((double) (h - 1) / 2.0)));
 				transformY[i] = (int) valor;
 			}
 		}
 		MinMaxReturn increment = getNormValue(transformY);
 
 		int[] transformX = new int[h + increment.getDelta()];
 
 		int[][] transformMatrix = new int[(w)][h + increment.getDelta()];
 
 		for (int i = 0; i < w; i++) {
 			Arrays.fill(transformMatrix[i], 0xffffff);
 			for (int j = 0; j < h; j++) {
 				int desloc = (j - transformY[i]);
 				transformMatrix[i][increment.getSum() + desloc] = resultMatrix[i][j];
 			}
 		}
 		if (this.xAxis) {
 
 			// cosine - vertical
 			for (int i = 0; i < h + increment.getDelta(); i++) {
 				// TODO: parameters
 				double y = (i / (double) (h - 1)) * 1.5 * Math.PI + 16;
 				double valor = (((2 * Math.cos(y) + 1) * ((double) (w - 1) / 2.0)) * 0.07) + 7;
 				transformX[i] = (int) valor;
 			}
 		}
 		MinMaxReturn incrementX = getNormValue(transformX);
 
 		int[][] transform2Matrix = new int[(w + incrementX.getDelta())][h
 				+ increment.getDelta()];
 
 		for (int i = 0; i < transform2Matrix.length; i++) {
 			Arrays.fill(transform2Matrix[i], 0xffffff);
 		}
 
 		for (int j = h + increment.getDelta() - 1; j >= 0; j--) {
 
 			for (int i = w - 1; i >= 0; i--) {
 				int desloc = (i - transformX[j]);
 				transform2Matrix[incrementX.max + desloc][j] = transformMatrix[i][j];
 			}
 		}
 
 		int[] resultImage = new int[(w + incrementX.getDelta())
 				* (h + increment.getDelta())];
 
 		for (int i = 0; i < transform2Matrix.length; i++) {
 			for (int j = 0; j < transform2Matrix[0].length; j++) {
 				resultImage[j * (transform2Matrix.length) + i] = transform2Matrix[i][j];
 			}
 		}
 		BufferedImage dest = new BufferedImage(transform2Matrix.length,
 				transform2Matrix[0].length, BufferedImage.TYPE_INT_RGB);
 
 		dest.setRGB(0, 0, transform2Matrix.length, transform2Matrix[0].length,
 				resultImage, 0, transform2Matrix.length);
 
 		return dest;
 	}
 
	private class MinMaxReturn {
 		private int min;
 		private int max;
 
 		int getDelta() {
 			return max - min;
 		}
 
 		int getSum() {
 			return max + min;
 		}
 	}
 
 	private MinMaxReturn getNormValue(int[] numbers) {
 		MinMaxReturn ret = new MinMaxReturn();
 		ret.max = numbers[0];
 		ret.min = numbers[0];
 		for (int i = 1; i < numbers.length; i++) {
 			if (numbers[i] > ret.max) {
 				ret.max = numbers[i];
 			}
 			if (numbers[i] < ret.min) {
 				ret.min = numbers[i];
 			}
 		}
 
 		return ret;
 	}
 
 }
