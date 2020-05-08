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
      * 
      * Variables to store the first and second order moments.
      */
 	private int M00 = 0, M01 = 0, M10 = 0, M11 = 0, M20 = 0, M02 = 0;
 	
 	/**
      * 
      * Variables to store the centroid location.
      */
 	private int x = 0, y = 0; 
 	
 	/**
      * 
      *Intermediate variables required to calculate image properties.
      */
 	private double a = 0, b = 0, c = 0;
 	
 	/**
      * 
      *Variables to store the Length and Breadth of the rectangle with similar moments as those
      * of the {@link CS440Image image} being processed.
      */
 	private int L1 = 0, L2 = 0;
 
 	/**
      * 
      *Variables to store the bounding box of the object in 
      *the {@link CS440Image image} being processed.
      */
 	private int x1 = 0, y1 = 0, x2 = 0, y2 = 0;
 	
 	/**
 	 * The {@link Sink} subscribers to this
 	 * {@link ImageMomentsGenerator}.
 	 */
 	private List<Sink<ImageMoments>> subscribers = new ArrayList<Sink<ImageMoments>>(1);
 	
 	/**
      * 
      *Variable to store the orientation of the rectangle with similar moments as those
      * of the {@link CS440Image image} being processed.
      */
 	private double theta;
 	
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
 	public void momentsgenerator(CS440Image frame){
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
 		
 
 
 		if(M00 == 0) 
 			M00 = 1;  
 
 		x = M10/M00;
 		y = M01/M00;
 		
 		a = (M20/M00) - (x * x);
 		b = 2 * ((M11/M00) - x*y);
 		c = (M02/M00) - (y * y);
 		
 		theta = atan(b/(a-c)) / 2;
 		L1 = (int)Math.floor(Math.sqrt(6 * (a + c + Math.sqrt(b * b + Math.pow(a - c, 2)))));
 		L2 = (int)Math.floor(Math.sqrt(6 * (a + c - Math.sqrt(b * b + Math.pow(a - c, 2)))));
 		
 		ImageMoments moments = new ImageMoments();
 		moments.theta = this.theta;
 		moments.L1 = this.L1;
 		moments.L2 = this.L2;
 		moments.x  = this.x;
 		moments.y  = this.y;
 		moments.x1 = this.x1;
 		moments.x2 = this.x2;
 		moments.y1 = this.y1;
 		moments.y2 = this.y2;
		moments.M = new int[] {M00, M10, M01, M11, M20, M02};
 		
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
