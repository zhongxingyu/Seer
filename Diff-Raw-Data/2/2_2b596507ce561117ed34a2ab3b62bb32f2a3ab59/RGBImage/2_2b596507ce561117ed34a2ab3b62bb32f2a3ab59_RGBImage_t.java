 public class RGBImage {
 
 	private RGBColor[][] pixels;
 	private int height;
 	private int width;
 
 	// ------------------------------------------------------------------------------
 	/* OKAY */public RGBImage(int rows, int cols) {
 		setHeight(rows);
 		setWidth(cols);
 		pixels = new RGBColor[rows][cols];
 
 		for (int i = 0; i < rows; i++) {
 			for (int j = 0; j < cols; j++) {
 				pixels[i][j] = new RGBColor();
 			}// inner loop
 		}// outer loop
 	}// CTOR1
 
 	/* OKAY */public RGBImage(RGBColor[][] pixels) {
 		setHeight(pixels.length);
 		setWidth(pixels[0].length);
 		setPixelArray(pixels);
 	}// CTOR2
 
 	/* OKAY */public RGBImage(RGBImage other) {
 		RGBImage image = new RGBImage(other.getPixelArray());
 		setHeight(image.getHeight());
 		setWidth(image.getWidth());
 	}// CTOR3
 		// -----------------------------------------------------------------------------
 
 	/******************** METHODS BELOW *******************************/
 	/* OKAY */public int getHeight() {
 		return height;
 	}// getHeight
 
 	public void setHeight(int height) {
 		this.height = height;
 	}// setHeight
 
 	/* OKAY */public int getWidth() {
 		return width;
 	}// getWidth
 
 	public void setWidth(int width) {
 		this.width = width;
 	}// setWidth
 
 	/* OKAY */public RGBColor getPixel(int row, int col) {
 		if (row > getHeight() || col > getWidth()) {
 			return new RGBColor();
 		} else {
 			return pixels[row][col];
 		}
 
 	}// getPixel
 
 	/* OKAY */public void setPixel(int row, int col, RGBColor pixel) {
 		if (row > getHeight() || col > getWidth()) {
 			return;
 		}// if
 		else {
 			pixels[row][col] = new RGBColor(pixel);
 		}// else
 
 	}// setPixel
 
 	/* OKAY */public boolean equals(RGBImage other) {
 
 		if (!(getHeight() == other.getHeight())
 				|| !(getWidth() == other.getWidth())) {
 			return false;
 		}// if
 		for (int i = 0; i < getHeight(); i++) {
 
 			for (int j = 0; j < getWidth(); j++) {
 
 				if (!pixels[i][j].equals(other.getPixelArray()[i][j])) {
 					return false;
 				}// inner if
 
 			}// inner loop
 
 		}// outer loop
 		return true;
 	}// equals
 
 	/* OKAY */public RGBColor[][] getPixelArray() {
 		return pixels;
 	}// getPixelArray
 
 	/* OKAY */public void setPixelArray(RGBColor[][] pixelArray) {
 		this.pixels = copyArray(pixelArray);
 	}// getRGBImage
 
 	/* OKAY */public void flipHorizontal() {
 		RGBColor[][] beforeFlip = getPixelArray();
 		RGBColor[][] flippedImage = new RGBColor[getHeight()][getWidth()];
 		int heightCounter = getHeight() - 1;
 		for (int i = 0; i < flippedImage.length; i++) {
 
 			for (int j = 0; j < flippedImage[0].length; j++) {
 
 				flippedImage[i][j] = beforeFlip[heightCounter][j];
 
			} 
 			heightCounter--;
 		}
 
 		setPixelArray(flippedImage);
 	}// flipHorizontal
 
 	/* OKAY */public void flipVertical() {
 		RGBColor[][] flippedImage = new RGBColor[getHeight()][getWidth()];
 
 		int widthCounter = getWidth();
 
 		for (int i = 0; i < flippedImage.length; i++) {
 			for (int j = 0; j < flippedImage[0].length; j++) {
 
 				flippedImage[i][j] = pixels[i][widthCounter];
 			}// inner loop
 			widthCounter -= 1;
 		}// outer loop
 
 	}// flipVertical
 
 	/* OKAY */public void invertColors() {
 		for (int i = 0; i < pixels.length; i++) {
 			for (int j = 0; j < pixels[0].length; j++) {
 				pixels[i][j].invert();
 			}// inner loop
 		}// outer loop
 	}
 
 	public void rotateClockwise() {
 
 		RGBColor[][] rotatedArray = new RGBColor[getWidth()][getHeight()];
 
 		for (int i = 0; i < rotatedArray.length; i++) {
 			int marker = getHeight();
 			for (int j = 0; j < rotatedArray[0].length; j++) {
 
 				rotatedArray[i][j] = getPixelArray()[marker][j];
 				marker--;
 			}// inner loop
 
 		}// outer loop
 
 	}// rotateClockwise
 
 	private RGBColor[][] copyArray(RGBColor[][] pixels) {
 		RGBColor[][] copiedArray = new RGBColor[pixels.length][pixels[0].length];
 		for (int i = 0; i < pixels.length; i++) {
 			for (int j = 0; j < pixels[0].length; j++) {
 
 				copiedArray[i][j] = pixels[i][j];
 
 			}// inner loop
 		}// outer loop
 		return copiedArray;
 	}// copyArray
 
 }// class
