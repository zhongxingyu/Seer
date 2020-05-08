 package baseGame.Rendering;
 
 import java.awt.Color;
 import java.awt.Polygon;
 import java.awt.image.BufferedImage;
 import java.awt.image.DataBufferInt;
 import java.io.File;
 import java.io.IOException;
 import baseGame.*;
 import javax.imageio.ImageIO;
 
 public class RGBImage {
 	public BufferedImage getRgbBufferedImage() {
 		return rgbBufferedImage;
 	}
 
 	private BufferedImage rgbBufferedImage;
 	private int[] rgbPixels;
 
 	private int canvasWidth;
 	private int canvasHeight;
 
 	public int getWidth() {
 		return canvasWidth;
 	}
 
 	public int getHeight() {
 		return canvasHeight;
 	}
 
 	public RGBImage(int[] pixels, int width, int height) {
 		rgbPixels = pixels;
 		canvasHeight = height;
 		canvasWidth = width;
 
 	}
 
 	public RGBImage(BufferedImage img) {
 		setBufferedImage(img);
 	}
 
 	private void setBufferedImage(BufferedImage img) {
 		if (img.getType() != BufferedImage.TYPE_INT_RGB) {
 			rgbBufferedImage = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_RGB);
 
 			rgbBufferedImage.getGraphics().drawImage(img, 0, 0, img.getWidth(), img.getHeight(), Color.white, null);
 		} else {
 			rgbBufferedImage = img;
 		}
 		canvasWidth = img.getWidth();
 		canvasHeight = img.getHeight();
 		rgbPixels = ((DataBufferInt) rgbBufferedImage.getRaster().getDataBuffer()).getData();
 
 	}
 
 	public RGBImage(File file) {
 
 		BufferedImage img = null;
 
 		try {
 			img = ImageIO.read(file);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		setBufferedImage(img);
 	}
 
 	public void DrawRGBImage(int[] pixels, int x, int y, int width, int height) {
 
 		int realY = y * canvasWidth;
 
 		if (x >= 0 && y >= 0 && y <= canvasHeight && x <= canvasWidth) {
 
 			for (int i = 0; i < height; i++) {
 
 				for (int j = 0; j < width; j++) {
 					int adr = realY + x + i * canvasWidth + j;
 
 					if (adr < rgbPixels.length) {
 						rgbPixels[adr] = pixels[i * width + j];
 
 					} else {
 						break;
 					}
 				}
 			}
 		}
 	}
 
 	public void DrawRGBImage(int[] pixels, int mask, int x, int y, int width, int height) {
 
 		int realY = y * canvasWidth;
 
 		for (int i = 0; i < height; i++) {
 
 			for (int j = 0; j < width; j++) {
 				int adr = realY + x + i * canvasWidth + j;
 
 				if ((j + x) > canvasWidth | (j + x) < 0 || (y + i) < 0 || adr >= rgbPixels.length)
 					continue;
 
 				int pixelColor = pixels[i * width + j];
 				if (pixelColor != mask) {
 					rgbPixels[adr] = pixelColor;
 
 				}
 			}
 		}
 	}
 
 	// Midtpoint circle algorithm. Source:
 	// http://en.wikipedia.org/wiki/Midpoint_circle_algorithm
 	// Modded to fil a circle;
 
 	public void setPixel(int[] pixels, int width, int color, int x, int y) {
 
 		int height = pixels.length / width;
 
 		if (x >= 0 && y >= 0 && y <= height && x <= width) {
 			pixels[width * y + x] = color;
 		}
 	}
 
 	public RGBImage getSubImage(int x, int y, int width, int height) {
 		int[] pixels = new int[height * width];
 		RGBImage img = new RGBImage(pixels, width, height);
 
 		for (int i = 0; i < height; i++) {
 			for (int ii = 0; ii < width; ii++) {
 				img.setPixel(getPixel(x + ii, y + i), ii, i);
 			}
 		}
 		return img;
 	}
	public void replaceColors(int originalColor,int newColor){
		for(int i = 0;i<rgbPixels.length;i++){
			int color = rgbPixels[i];
			if(color == originalColor){
				rgbPixels[i] = newColor;
			}
		}
	}
 	public void setPixel(int color, int x, int y) {
 		if (x >= 0 && y >= 0 && y <= canvasHeight && x <= canvasWidth) {
 			rgbPixels[canvasWidth * y + x] = color;
 		}
 	}
 
 	public int getPixel(int x, int y) {
 		return rgbPixels[canvasWidth * y + x];
 	}
 
 	Polygon poly = new Polygon();
 
 	public int[] getPixels() {
 		return rgbPixels;
 	}
 }
