 import static java.lang.Math.min;
 import static java.lang.Math.*;
 
 import java.awt.Color;
 import java.awt.image.BufferedImage;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * A {@link CS440Image} processor that considers the {@link CS440Image image}
  * as a binary image and generates its image moments and using which computes
  * the desired object's centroid, orientation and length and breadth of a rectangle
  * with the same moments. This class assumes that all images are of 640 x 480 resolution.
  * 
  * @author Abhinay
  * 
  */
 public class ImageMomentsGenerator implements Sink<CS440Image>, Source<ImageMoments> {
 	
 	/**
 	 * The {@link Sink} subscribers to this
 	 * {@link ImageMomentsGenerator}.
 	 */
 	private List<Sink<ImageMoments>> subscribers = new ArrayList<Sink<ImageMoments>>(1);
 	
 	@Override
 	public void receive(CS440Image frame) {
 		momentsgenerator(frame);
 	}
 	
 	/**
      * 
      *Method that computes image moments and returns an 
      *{@link ImageMoments} class with the results.
 	 * @return 
 	 * @return 
      */
 	public void momentsgenerator(CS440Image frame){    /**
 	     * 
 	     * Variables to store the first and second order moments.
 	     */
 		double M00 = 0, M01 = 0, M10 = 0, M11 = 0, M20 = 0, M02 = 0;
 		
 		/**
 	     * 
 	     * Variables to store the centroid location.
 	     */
 		double x = 0, y = 0; 
 		
 		/**
 	     * 
 	     *Intermediate variables required to calculate image properties.
 	     */
 		double a = 0, b = 0, c = 0;
 		
 		/**
 	     * 
 	     *Variables to store the Length and Breadth of the rectangle with similar moments as those
 	     * of the {@link CS440Image image} being processed.
 	     */
 		double L1 = 0, L2 = 0;
 
 		/**
 	     * 
 	     *Variables to store the bounding box of the object in 
 	     *the {@link CS440Image image} being processed.
 	     */
 		double x1 = 0, y1 = 0, x2 = 0, y2 = 0;
 		
 		/**
 	     * 
 	     *Variable to store the orientation of the rectangle with similar moments as those
 	     * of the {@link CS440Image image} being processed.
 	     */
 		double theta;
 
 		BufferedImage image = frame.getRawImage();
 		for(int w = 0; w < image.getWidth(); w++) {
 			for (int h = 0; h < image.getHeight(); h++) {
 				Color pixelint = new Color (image.getRGB(w, h));
 				int intensity = min(min(pixelint.getRed(), 1)+ min(pixelint.getBlue(), 1)+ min(pixelint.getGreen(), 1), 1);
 				
 				if(intensity != 0) {
 					x1 = min(x1, w);
 					x2 = max(x2, w);
 					y1 = min(y1, h);
 					y2 = max(y2, h);
 				}
 				
 				M00 += intensity;
 				M10 += w * intensity;
 				M01 += h * intensity;
 				M11 += w * h * intensity;
 				M20 += w * w * intensity;
 				M02 += h * h * intensity;
 				
 			}
 		}	
 		
 
 
		double m00 = M00 == 0 ? 1.0D : M00;
 
		x = M10/m00;
		y = M01/m00;
 		
 		a = (M20/m00) - (x * x);
 		b = 2 * ((M11/m00) - x*y);
 		c = (M02/m00) - (y * y);
 		
 		theta = atan(b/(a-c)) / 2;
		L1 = Math.sqrt(6 * (a + c + Math.sqrt(b * b + Math.pow(a - c, 2))));
		L2 = Math.sqrt(6 * (a + c - Math.sqrt(b * b + Math.pow(a - c, 2))));
 		
 		ImageMoments moments = new ImageMoments();
 		moments.theta = theta;
 		moments.L1 = L1;
 		moments.L2 = L2;
 		moments.x  = x;
 		moments.y  = y;
 		moments.x1 = x1;
 		moments.x2 = x2;
 		moments.y1 = y1;
 		moments.y2 = y2;
 		moments.m00 = M00;
 		moments.m01 = M01;
 		moments.m02 = M02;
 		moments.m10 = M10;
 		moments.m11 = M11;
 		moments.m20 = M20;
 		
 		// notify subscribers
 		for (Sink<ImageMoments> subscriber : subscribers) {
 			subscriber.receive(moments);
 		}	
 		
 	}
 
 	@Override
 	public void subscribe(Sink<ImageMoments> sink) {
 		subscribers.add(sink);	
 	}
 }
