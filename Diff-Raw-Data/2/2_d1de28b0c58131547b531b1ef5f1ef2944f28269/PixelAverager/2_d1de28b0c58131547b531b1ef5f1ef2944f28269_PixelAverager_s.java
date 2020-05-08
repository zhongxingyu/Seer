 package pixelaverager;
 
 
 import javax.imageio.ImageIO;
 import java.io.File;
 import java.io.IOException;
 import java.awt.image.BufferedImage;
 import java.awt.Color;
 
 /* A java command line applicaiton that takes in two bmp files of the 
  * same dimension and averages the pixels together.
 */
 
 public class PixelAverager {
 
   public static void main(String[] args) {
 		if(args.length != 3) {
 			System.err.println("Invalid command line argument. Requires exactly two file names");
 			System.exit(1);
 		}
 
 		String file1 = args[0];
 		String file2 = args[1];
                 String fileOut = args[2];
 
 		System.out.println("Reading in files...");
 
 		BufferedImage image1 = null;
 		BufferedImage image2 = null;
 
 		try {
 			File imagefile1 = new File(file1);
 			File imagefile2 = new File(file2);
 
 			image1 = ImageIO.read(imagefile1);
 			image2 = ImageIO.read(imagefile2);
 		} catch (IOException e) {
 			System.err.println("Error reading file.");
                         System.exit(1);
                 }
 		System.out.println("Successfully read in files");
 
 		BufferedImage avgImage = pAvg(image1, image2);
 		try {
 			ImageIO.write(avgImage, "jpg", new File(fileOut));
 		} catch (IOException er) {
 			System.out.println("Error writing file.");
                         System.exit(1);
 		}
 
 		System.out.println("Created " + fileOut);
 	}
 
 	public static BufferedImage pAvg(BufferedImage image1, BufferedImage image2) {
 		int height = image1.getHeight();
 		int width = image1.getWidth();
 
		BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
 
 		if (height != image2.getHeight() || width != image2.getWidth()) {
 			System.err.println("These files are different sizes. Try uploading files with the same dimensions.");
 			System.exit(1);
 		}
 
 		System.out.println("Averaging pixel values...");
 
 		//I feel like this method of averaging pixel values could be improved upon
 		for(int y=0; y<height; y++) {
 			for(int x=0; x<width; x++) {
 				int rgb1 = image1.getRGB(x, y);
 				int rgb2 = image2.getRGB(x, y);
 				//separate the individual components of rgb1
 				int alpha1  = (rgb1 >> 24) & 0xFF;
 				int red1 = (rgb1 >> 16) & 0xFF;
 				int green1 = (rgb1 >> 8) & 0xFF;
 				int blue1 =(rgb1) & 0xFF;
                                 //separate the individual components of rgb2
 				int alpha2  = (rgb2 >> 24) & 0xFF;
 				int red2 = (rgb2 >> 16) & 0xFF;
 				int green2 = (rgb2 >> 8) & 0xFF;
 				int blue2 =(rgb2) & 0xFF;
 				//avg the two sets
 				int red = (red1+red2)/2;
 				int green = (green1+green2)/2;
 				int blue = (blue1+blue2)/2;
 				int alpha = (alpha1+alpha2)/2;
                                 
                                 int rgb = (alpha << 24) | 
                                           (red << 16) | 
                                           (green << 8) |
                                           (blue);
 
 				result.setRGB(x, y, rgb);
 			}
 		}
 
 		System.out.println("Successfully averaged pixel values");
 
 		return result;
 	}
 }
