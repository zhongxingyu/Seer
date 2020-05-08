 package image_comparer;
 import java.util.Random;
 import java.util.*;
 
 /*
  * Matches key points in two images to compare them.
  */
 public class KeypointComparer implements PixelArrayComparer
 {
 	public KeypointComparer()
 	{
 
 	}
 
 	/*
 	 * Picks random points in two images and compares them.
 	 */
 	public double compare(PixelArray a1, PixelArray a2) {
 		int width = a1.getWidth();
 		int height = a1.getHeight();
 		int n = (int)(0.01 * width * height);
 		int numMatched=0, currWidth, currHeight;
 		Random rand = new Random();
 		for (int i=0; i<n; i++) {
 			currWidth = rand.nextInt(width);
 			currHeight = rand.nextInt(height);
 			if (a1.getPixel(currWidth, currHeight)==
 					a2.getPixel(currWidth, currHeight))
 				numMatched++;
 		}
 		return (double)numMatched/n;
 	}
 
 	/*
 	 * Intelligently picks points in two images to compare.
 	 */
 	public double keypointcompare(PixelArray a1, PixelArray a2, int n) {
 		int width = a1.getWidth();
		int height = a1.getHeight();
 		//int dimension = width*height;
 		//ArrayList<Double> diffs = new ArrayList<Double>();
 		ArrayList<Integer> xcos = new ArrayList<Integer>();
 		ArrayList<Integer> ycos = new ArrayList<Integer>();
 		float rsum;
 		float bsum;
 		float gsum;
 		float diff;
 		int counter = 0;
		// finds interesting pixels
 		for (int i=1;++i<height;) {
 			for (int j=1;++j<width;) {
 				rsum = 0;
 				bsum = 0;
 				gsum = 0;
 				for (int k=-1;k<2;++k) {
 					for (int l=-1;l<2;++l) {
 						rsum += (float) a1.getRed(a1.getPixel(i+k, j+l));
 						bsum += (float) a1.getBlue(a1.getPixel(i+k, j+l));
 						gsum += (float) a1.getGreen(a1.getPixel(i+k, j+l));
 					}
 				}
 				diff = Math.abs(rsum-a1.getRed(a1.getPixel(i, j)))
 						+ Math.abs(bsum-a1.getBlue(a1.getPixel(i, j)))
 						+ Math.abs(gsum-a1.getGreen(a1.getPixel(i, j)));
 				if (diff > 0.2) {
 					xcos.add(i);
 					ycos.add(j);
 					++counter;
 				}
 			}
 		}
 		if (counter<20) {
 			return compare(a1, a2);
 		}
 		int matched = 0;
 		for (int i=0; i<counter; i++) {
 			if (a1.getPixel(xcos.get(i), ycos.get(i))==a2.getPixel(xcos.get(i), ycos.get(i)))
 				matched++;
 		}
 		return ((double) matched) / ((double) n);
 	}
}
