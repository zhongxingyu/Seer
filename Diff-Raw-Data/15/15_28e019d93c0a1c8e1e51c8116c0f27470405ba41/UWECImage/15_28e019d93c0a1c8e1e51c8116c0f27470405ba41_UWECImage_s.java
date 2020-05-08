 package mosaicAssignment1;
 
 import javax.imageio.ImageIO;
 import java.awt.*;
 import java.awt.image.*;
 import java.io.*;
 import java.util.ArrayList;
 
 public class UWECImage {
 	private BufferedImage im;
 	private ArrayList<BufferedImage> imList;
 
 	// Make an image from the given filename
 	public UWECImage(String filename) {
 		File f = new File(filename);
 
 		try {
 			this.im = ImageIO.read(f);
 
 		} catch (IOException e) {
 			e.printStackTrace();
 			System.exit(1);
 		}
 	}
 
 	// Make a blank image given the size
 	public UWECImage(int x, int y) {
		this.imList = new ArrayList<BufferedImage>();
 		this.im = new BufferedImage(x, y, BufferedImage.TYPE_INT_RGB);
 
 		// Create a graphics context for the new BufferedImage
 		Graphics2D g2 = this.im.createGraphics();
 
 		// Fill in the image with black
 		g2.setColor(Color.black);
 		g2.fillRect(0, 0, x, y);
 	}
 
 	public int getWidth() {
 		return im.getWidth();
 	}
 
 	public int getHeight() {
 		return im.getHeight();
 	}
 
 	public int getRed(int x, int y) {
 		int value = 0;
 
 		if ((x >= 0) && (y >= 0) && (x < im.getWidth()) && (y < im.getHeight())) {
 			value = ((im.getRGB(x, y) >> 16) & 255);
 		}
 
 		return value;
 	}
 
 	public int getGreen(int x, int y) {
 		int value = 0;
 
 		if ((x >= 0) && (y >= 0) && (x < im.getWidth()) && (y < im.getHeight())) {
 			value = ((im.getRGB(x, y) >> 8) & 255);
 		}
 
 		return value;
 	}
 
 	public int getBlue(int x, int y) {
 		int value = 0;
 
 		if ((x >= 0) && (y >= 0) && (x < im.getWidth()) && (y < im.getHeight())) {
 			value = ((im.getRGB(x, y) >> 0) & 255);
 		}
 
 		return value;
 	}
 
 	public void setRGB(int x, int y, int red, int green, int blue) {
 		int pixel = (red << 16) + (green << 8) + (blue);
 
 		im.setRGB(x, y, pixel);
 	}
 
 	public void draw(Graphics g) {
 		g.drawImage(im, 0, 0, null);
 	}
 
 	public void write(String filename) {
 		if (filename.endsWith(".png")) {
 			writePNG(filename);
 		} else {
 			System.out.println("Filetype not supported");
 		}
 	}
 
 	private void writePNG(String filename) {
 
 		File f = new File(filename);
 		try {
 			// Write out the image to the file as a png
 			ImageIO.write(this.im, "png", f);
 		} catch (IOException e) {
 			e.printStackTrace();
 			System.exit(1);
 		}
 	}
 	/**
 	 * Mutates the size of the image you call this method on
 	 * to the new size determined by the parameters
 	 * After this, deposits the new image into an ArrayList instance variable
 	 * 
 	 * @param newWidth
 	 * @param newHeight
 	 */
 	public void scaleImage(int newWidth, int newHeight){
 		//create a new empty buffered image with the newW and H
 		BufferedImage newIm = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
 		
 		//scale factor between the old and new image sizes
 		double scaleX = im.getWidth() / newIm.getWidth();
 		double scaleY = im.getHeight() / newIm.getHeight();
 		int rgb = 0;
 		
 		//double loop to run through the new image to move pixel colours
 		for(int i = 0; i< newIm.getWidth(); i++){
 			for(int j = 0; j< newIm.getHeight(); j++){
 				//sets rgb to the colour at the scaled location on the original picture
 				rgb = im.getRGB((int)(i * scaleX), (int)(j * scaleY));
 				//puts the colour into the new image
 				newIm.setRGB(i, j, rgb);
 			}
 		}
 		//if this arraylist doesn't work, remember to remove the instance variable and initialization in 
 		//the 2nd UWECImage constuctor, otherwise, hooray
 		
 		//adds the new image into an instance variable arraylist
 		imList.add(newIm);
 		
 	}
 	
 
 }
