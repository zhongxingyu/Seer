 package utils;
 
 import hopfield.HopfieldNet;
 
 import java.awt.Color;
 import java.awt.Graphics2D;
 import java.awt.Image;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 
 import javax.imageio.ImageIO;
 
 public class ImageUtils {
 	
 	/**
 	 * Loads an RGB image stored in the given file. 
 	 * Returns an INT array containing the black and white
 	 * pixels for that image
 	 * 
 	 * @path: Path of the image
 	 */ 
 	public static int[] loadBlackAndWhiteImage(String path) {
 		try {
 			BufferedImage image = ImageIO.read(new File(path));
 			image = getBlackAndWhite(image);
 			int w = image.getWidth();
 			int h = image.getHeight();
 			int[] rgb = new int[w * h];
 			image.getRGB(0, 0, w, h, rgb, 0, w);
 			return rgb;
 		} catch (IOException e) {
 			System.err.println(e.getMessage());
 			return null;
 		}
 	}
 	
 	/**
 	 * Sets an RGB image to HopField states valid array. 
 	 * Sets an int[] to a STATE_POSITIVE or STATE_NEGATIVE
 	 * depending on the color of the pixel.
 	 */ 
 	public static void convertBlackAndWhiteImageToState(int[] image) {
 		for (int i = 0; i < image.length; i++) {
 			if (image[i] == Color.BLACK.getRGB()) {
 				image[i] = HopfieldNet.STATE_POSITIVE;				
 			} else if (image[i] == Color.WHITE.getRGB()) {
 				image[i] = HopfieldNet.STATE_NEGATIVE;
 			} else {
 				throw new IllegalArgumentException("Invalid color: " + image[i]);
 			}
 		}
 	}
 	
 	private static BufferedImage getBlackAndWhite(BufferedImage originalImage) {
 		BufferedImage blackAndWhiteImage = new BufferedImage(
 			originalImage.getWidth(null), 
 			originalImage.getHeight(null), 
 			BufferedImage.TYPE_BYTE_BINARY);
 		Graphics2D g = (Graphics2D) blackAndWhiteImage.getGraphics();
 		g.drawImage(originalImage, 0, 0, null);
 		g.dispose();
 		return blackAndWhiteImage;
 	}
 	
 	/**
 	 * Function to create an image given a RGB pixel array
 	 * 
 	 * @param pixels array containing color to create the image
 	 * @param width	image width
 	 * @param height image height
 	 * @return the image that has been created from the pixel array.
 	 */
 	public static Image getImageFromArray(int[] pixels, int width, int height) {
         BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
         image.setRGB(0, 0, width, height, pixels, 0, width);
         return image;
     }
 
 	public static void convertStateToBlackAndWhiteRGB(int[] array) {
 		for (int i = 0; i < array.length; i++) {
 			if (array[i] == HopfieldNet.STATE_POSITIVE) {
 				array[i] = Color.BLACK.getRGB();
 			} else if (array[i] == HopfieldNet.STATE_NEGATIVE) {
 				array[i] = Color.WHITE.getRGB();
 			} else {
 				throw new IllegalArgumentException("Invalid state: " + array[i]);
 			}
 		}
 	}
 }
