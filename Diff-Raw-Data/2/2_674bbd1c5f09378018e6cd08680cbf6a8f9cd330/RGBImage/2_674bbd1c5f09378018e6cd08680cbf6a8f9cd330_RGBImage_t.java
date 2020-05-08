 /**
  * @author raskanskyz
  * 
  */
 public class RGBImage {
 
 	private RGBColor[][] pixels;
 
 	/**
 	 * This is the default constructor. It creates a black image with a height =
 	 * 'rows' and width = 'cols'
 	 * 
 	 * @param rows
 	 *            Sets the number of rows in the 2D array.
 	 * @param cols
 	 *            Sets the number of columns in the 2D array.
 	 */
 	public RGBImage(int rows, int cols) {
 		pixels = setArrayBlack(new RGBColor[rows][cols]);
 	}// CTOR1
 
 	/**
 	 * This constructor creates a new RGBImage and initializes it with 'pixels'
 	 * array.
 	 * 
 	 * @param pixels
 	 * 
 	 */
 	public RGBImage(RGBColor[][] pixels) {
 		setPixelArray(pixels);
 	}// CTOR2
 
 	/**
 	 * This constructor creates a new RGBImage identical to 'other'.
 	 * 
 	 * @param other
 	 * 
 	 */
 	public RGBImage(RGBImage other) {
 		this.pixels = copyArray(other.toRGBColorArray());
 	}// CTOR3
 
 	/**
 	 * 
 	 * @return The height of this array.
 	 * 
 	 */
 	public int getHeight() {
 		return pixels.length;
 	}// getHeight
 
 	/**
 	 * @return The width of this array.
 	 */
 	public int getWidth() {
 		return pixels[0].length;
 	}// getWidth
 
 	/**
 	 * This method returns the pixel located at [row][col].
 	 * 
 	 * @param row
 	 *            The row in which the pixel is located at.
 	 * @param col
 	 *            The column in which the pixel is located at.
 	 * @return The pixel located at [height][width].
 	 */
 	public RGBColor getPixel(int row, int col) {
 		if (row > getHeight() || col > getWidth()) {
 			return new RGBColor();
 		} else {
 			return new RGBColor(pixels[row][col]);
 		}
 
 	}// getPixel
 
 	/**
 	 * This method sets 'pixel' in the location [row][column].
 	 * 
 	 * @param row
 	 *            The row in which 'pixel' is to be set at.
 	 * @param col
 	 *            The column in which 'pixel' is to be set at.
 	 * @param pixel
 	 *            The pixel to set at [row][column].
 	 */
 	public void setPixel(int row, int col, RGBColor pixel) {
 		if (row > getHeight() || col > getWidth()) {
 			return;
 		}// if
 		else {
 			pixels[row][col] = new RGBColor(pixel);
 		}// else
 
 	}// setPixel
 
 	/**
 	 * This method check whether two RGBImages are identical.
 	 * 
 	 * @param other
 	 *            The RGBImage to be compared with.
 	 * @return True if images are the same and False if they are not
 	 */
 	public boolean equals(RGBImage other) {
 
 		if (!(getHeight() == other.getHeight())
 				|| !(getWidth() == other.getWidth())) {
 			return false;
 		}// if
 		for (int i = 0; i < getHeight(); i++) {
 
 			for (int j = 0; j < getWidth(); j++) {
 
				if (!pixels[i][j].equals(other.getPixel(i, j))) {
 					return false;
 				}// inner if
 
 			}// inner loop
 
 		}// outer loop
 		return true;
 	}// equals
 
 	/**
 	 * Flips the image on a horizontal axis.
 	 */
 	public void flipHorizontal() {
 
 		RGBColor[][] flippedImage = new RGBColor[getHeight()][getWidth()];
 
 		for (int i = 0; i < flippedImage.length; i++) {
 
 			flippedImage[i] = pixels[getHeight() - i - 1];
 
 		}// outer loop
 
 		setPixelArray(flippedImage);
 	}// flipHorizontal
 
 	/**
 	 * Flips the image on it's vertical axis.
 	 */
 	public void flipVertical() {
 
 		RGBColor[][] flippedImage = copyArray(toRGBColorArray());
 
 		for (int i = 0; i < flippedImage.length; i++) {
 
 			for (int j = 0; j < flippedImage[0].length; j++) {
 				flippedImage[i][j] = new RGBColor(pixels[i][getWidth() - 1 - j]);
 			}// inner loop
 
 		}// outer loop
 
 		setPixelArray(flippedImage);
 	}// flipVertical
 
 	/**
 	 * Inverts each pixel in the Array.
 	 */
 	public void invertColors() {
 		for (int i = 0; i < pixels.length; i++) {
 			for (int j = 0; j < pixels[0].length; j++) {
 				pixels[i][j].invert();
 			}// inner loop
 		}// outer loop
 	}
 
 	/**
 	 * This method copies an array.
 	 * 
 	 * @return A copy of this RGBImage array.
 	 */
 	public RGBColor[][] toRGBColorArray() {
 		return copyArray(pixels);
 	}// toRGBColorArray
 
 	/**
 	 * Rotates the image 90 degrees to the right.
 	 */
 	public void rotateClockwise() {
 
 		RGBColor[][] rotatedArray = new RGBColor[getWidth()][getHeight()];
 
 		for (int i = 0; i < rotatedArray.length; i++) {
 
 			for (int j = 0; j < rotatedArray[0].length; j++) {
 
 				rotatedArray[i][j] = toRGBColorArray()[getHeight() - 1 - j][i];
 
 			}// inner loop
 
 		}// outer loop
 		setPixelArray(rotatedArray);
 	}// rotateClockwise
 
 	/**
 	 * Rotates the image 90 degrees to the left.
 	 */
 	public void rotateCounterClockwise() {
 		rotateClockwise();
 		rotateClockwise();
 		rotateClockwise();
 	}// rotateCounterClockwise
 
 	/**
 	 * This method shifts the 2D array to the right if offset > 0 and to the
 	 * left if offset < 0.
 	 * 
 	 * @param offset
 	 *            The number of columns to shift the image.
 	 */
 	public void shiftCol(int offset) {
 		if (offset == getWidth() || offset == -getWidth()) {
 			setPixelArray(setArrayBlack(pixels));
 		}// if equal to columns
 
 		else if (offset > getWidth() - 1 || offset < -getWidth() + 1
 				|| offset == 0) {
 			return;
 		}// if out of bounds
 
 		else {
 
 			RGBColor[][] blackSheet = setArrayBlack(pixels);
 			if (offset > 0) {
 				for (int i = 0; i < blackSheet.length; i++) {
 					for (int j = 0; j < blackSheet[0].length; j++) {
 						if (!(j < offset)) {
 							blackSheet[i][j] = new RGBColor(
 									toRGBColorArray()[i][j - offset]);
 						}// if
 
 					}// inner loop
 				}// outer loop
 
 				setPixelArray(blackSheet);
 			}// if > 0
 
 			else if (offset < 0) {
 				flipVertical();
 				offset = -offset;
 				for (int i = 0; i < blackSheet.length; i++) {
 					for (int j = 0; j < blackSheet[0].length; j++) {
 						if (!(j < offset)) {
 							blackSheet[i][j] = toRGBColorArray()[i][j - offset];
 						}// if
 					}// inner loop
 				}// outer loop
 				setPixelArray(blackSheet);
 				flipVertical();
 			}// else if
 		}// else
 
 	}// shiftCol
 
 	/**
 	 * This method shifts the 2D array upwards if offset < 0 and downwards if
 	 * offset > 0.
 	 * 
 	 * @param offset
 	 *            The number of rows to shift the image.
 	 */
 	public void shiftRow(int offset) {
 		if (offset == getWidth() || offset == -getWidth()) {
 			setPixelArray(setArrayBlack(pixels));
 		}// if
 		else {
 			if (offset > 0) {
 
 			}// if offset positive
 			else if (offset < 0) {
 				rotateClockwise();
 				shiftCol(-offset);
 				rotateCounterClockwise();
 			}// of offset negative
 		}// else
 	}// shiftRow
 
 	/**
 	 * 
 	 * @return A grayscale representation of the image.
 	 */
 	public double[][] toGrayscaleArray() {
 		double[][] grayScaledArray = new double[getHeight()][getWidth()];
 		for (int i = 0; i < pixels.length; i++) {
 			for (int j = 0; j < pixels[0].length; j++) {
 
 				grayScaledArray[i][j] = pixels[i][j].convertToGrayscale();
 			}
 		}
 		return grayScaledArray;
 	}// toGrayscaleArray
 
 	/**
 	 * Returns the 2D array as A String for visualization.
 	 */
 	public String toString() {
 		String output = "";
 		for (int i = 0; i < pixels.length; i++) {
 			for (int j = 0; j < pixels[0].length; j++) {
 				if (j != pixels[0].length - 1) {
 					output += pixels[i][j].toString() + " ";
 				} else {
 					output += pixels[i][j].toString() + "\n";
 
 				}
 			}// inner loop
 
 		}// outer loop
 		return output;
 	}// toString
 
 	/**
 	 * This method copies an a given array.
 	 * 
 	 * @param pixels
 	 *            The array to copy.
 	 * @return A copy of 'pixels'.
 	 */
 	private RGBColor[][] copyArray(RGBColor[][] pixels) {
 		RGBColor[][] copiedArray = new RGBColor[pixels.length][pixels[0].length];
 		for (int i = 0; i < pixels.length; i++) {
 			for (int j = 0; j < pixels[0].length; j++) {
 
 				copiedArray[i][j] = new RGBColor(pixels[i][j]);
 
 			}// inner loop
 		}// outer loop
 		return copiedArray;
 	}// copyArray
 
 	/**
 	 * This method sets 'pixelArray' as this RGBImage array.
 	 * 
 	 * @param pixelArray
 	 *            the array to be set to this RGBImage.
 	 */
 	private void setPixelArray(RGBColor[][] pixelArray) {
 		this.pixels = copyArray(pixelArray);
 	}// getRGBImage
 
 	/**
 	 * This method converts a give Array into a black Array with the same
 	 * dimensions as 'sourceArray'.
 	 * 
 	 * @param sourceArray
 	 *            The array to be set to black.
 	 * @return A black image.
 	 */
 	private RGBColor[][] setArrayBlack(RGBColor[][] sourceArray) {
 		RGBColor[][] blackArray = new RGBColor[sourceArray.length][sourceArray[0].length];
 		for (int i = 0; i < blackArray.length; i++) {
 			for (int j = 0; j < blackArray[0].length; j++) {
 				blackArray[i][j] = new RGBColor();
 
 			}// inner loop
 		}// outer loop
 		return blackArray;
 	}// setArrayBlack
 
 }// class
