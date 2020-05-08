 import java.awt.FlowLayout;
 import java.awt.Image;
 import java.awt.image.BufferedImage;
 import java.awt.image.WritableRaster;
 import java.io.File;
 import java.io.IOException;
 
 import javax.imageio.ImageIO;
 import javax.swing.ImageIcon;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 
 
 public class IntegralImage {
 
 	/**
 	 * The integral image source image
 	 */
 	public double[] integralImage;
 	/**
 	 * The integral image of the squared source image
 	 */
 	public double[] squareIntegralImage;
 	public int width;
 	public int height;
 
 	/**
 	 * For testing.
 	 * 
 	 * @param args
 	 */
 	public static void main(String [ ] args) {
 		File file = new File("D:\\Dropbox\\BIK\\pro\\TrainingImages\\FACES\\face00001.bmp");
 		IntegralImage img = new IntegralImage(file);
 		img.drawIntegralImage(10);
 	}
 
 	/**
 	 * Creates an <code>IntegralImage</code> from the specified file.
 	 * 
 	 * @param fileName name of image to use
 	 */
 	IntegralImage(File file) {		
 		try {
 			BufferedImage srcImage = ImageIO.read(file);
 
 			width  = srcImage.getWidth();
 			height = srcImage.getHeight();
 
 			// Convert to a grayscale image.
 			// TODO: variable weighting of RGB channels.
 			BufferedImage grayImage = new BufferedImage(width, height,  
 					BufferedImage.TYPE_BYTE_GRAY);
 			grayImage.getGraphics().drawImage(srcImage, 0, 0, null);
 
 			integralImage 	    = new double [width*height];
 			squareIntegralImage = new double [width*height];
 
 			// Extract a double array from the BufferedImage object.
 			integralImage = grayImage.getData().getPixels(0, 0, width, height, integralImage);
 
 			// Build up an array containing the squared values of the original
 			// grayscale image.
 			for(int i = 0; i<width*height;i++) {
 				squareIntegralImage[i] = Math.pow(integralImage[i],2);
 			}
 
 			// Integrate both images.
 			integrateImage(integralImage);
 			integrateImage(squareIntegralImage);
 
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Takes a width coordinate <code>x</code> and a height 
 	 * coordinate <code>y</code> and returns a linear index <code>i</code>.
 	 * 
 	 * @param x width coordinate
 	 * @param y height coordinate
 	 * @return i linear index
 	 */
 	public int coord(int x, int y) {
 		return x + width*y;
 	}
 	
 	public double xy(int x, int y) {
 		return integralImage[coord(x,y)];
 	}
 	
 	public double xyS(int x, int y) {
 		return squareIntegralImage[coord(x,y)];
 	}
 
 	/**
 	 * Calculates the integral image by taking the cumulative
 	 * sum in both directions.
 	 * 
 	 * @param img image array to integrate
 	 */
 	private void integrateImage(double [] img) {
 		
 		// Sum in the x-direction
 		for(int y = 0; y<height;y++) {
 			double rs = 0;
 			for(int x = 0; x<width;x++) {
 				rs += img[coord(x,y)];
 				img[coord(x,y)] = rs;
 			}
 		}
 
 		// Sum in the y-direction
 		for (int x = 0; x<width;x++){
 			double rs = 0;
 			for (int y = 0; y<height;y++) {
 				rs += img[coord(x,y)];
 				img[coord(x,y)] = rs;
 			}
 		}
 	}
 
 	/**
 	 * Returns a normalized integral image, ie the integral image 
 	 * that would have been generated if the original image had been
 	 * normalized.
 	 * 
 	 * @return normalized image
 	 */
 	private double[] getNormalizedImg() {
 		double [] normImage = new double [width*height];
 		
 		// We are using
 		//
 		// std^2 = mean(x)^2 + mean(x^2)
 		//
 		// to normalize the image patch.
 		double mean = integralImage[width*height-1]/(width*height);
 		double meanSqr = squareIntegralImage[width*height-1]/(width*height);
		double std = Math.sqrt(meanSqr-Math.pow(mean,2));
 
 		for(int y = 0; y<height;y++) {
 			for(int x = 0; x<width;x++) {
 				normImage[coord(x,y)] = integralImage[coord(x,y)]-(x+1)*(y+1)*mean;
 				normImage[coord(x,y)] *= 1/std;
 			}
 		}
 
 		return normImage;
 	}
 
 	/**
 	 * Draws the normalized integral image, scaled to [0-255].
 	 *
 	 * @param scaleFactor scale the displayed image
 	 */
 	public void drawIntegralImage(double scaleFactor) {
 		// We want to display the normalized integral image.
 		double [] img = getNormalizedImg();
 		showImg(img, width, height, scaleFactor);
 	}
 	
 	static JFrame showImg(double[] img, int width, int height, double scaleFactor) {
 		// Find the smallest and largest values.
 				double min = Double.MAX_VALUE;
 				double max = Double.MIN_VALUE;
 				for(int i = 0; i<width*height;i++) {
 					if (img[i]>max) {
 						max = img[i];
 					}
 					else if (img[i]<min) {
 						min = img[i];
 					}
 				}
 
 				// Scale the image so that all values are in [0-255]
 				for(int i = 0; i<width*height;i++) {
 					img[i] = (img[i] - min)/(max - min)*255;
 				}
 
 				// We will write our double array to buffImg, so that 
 				// we can display it.
 				BufferedImage buffImg = new BufferedImage(width, height, 
 						BufferedImage.TYPE_BYTE_GRAY);
 
 				// We make a writable raster, write our image array to it
 				// and add the raster to buffImg.
 				WritableRaster raster = (WritableRaster) buffImg.getData();
 				raster.setPixels(0, 0, width, height, img);
 				buffImg.setData(raster);
 
 				JFrame frame = new JFrame();
 				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
 				frame.getContentPane().setLayout(new FlowLayout());
 				frame.getContentPane().add(
 						new JLabel(new ImageIcon(
 								buffImg.getScaledInstance(
 										(int)(width*scaleFactor), (int)(height*scaleFactor), Image.SCALE_SMOOTH))));
 				frame.pack();
 				frame.setVisible(true);
 				
 				return frame;
 	}
 
 }
