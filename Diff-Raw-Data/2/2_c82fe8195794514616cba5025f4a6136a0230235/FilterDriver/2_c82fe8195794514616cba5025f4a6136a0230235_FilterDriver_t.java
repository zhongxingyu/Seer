 package net.coobird.paint.driver;
 
 import java.awt.image.BufferedImage;
 
 import net.coobird.paint.filter.ImageFilter;
 import net.coobird.paint.filter.ImageFilterThreadingWrapper;
 import net.coobird.paint.filter.RepeatableMatrixFilter;
 
 public class FilterDriver
 {
 	private static long perform(ImageFilter filter, int width, int height)
 	{
 		long startTime = System.currentTimeMillis();
 		
 		BufferedImage img = new BufferedImage(
 				width,
 				height,
 				BufferedImage.TYPE_INT_ARGB
 		);
 		
		filter.processImage(img);
 		
 		long timePast = System.currentTimeMillis() - startTime;
 		
 		return timePast;
 	}
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args)
 	{
 		final int SIZE = 1000;
 		
 		long time1 = 0;
 		long time2 = 0;
 		
 		ImageFilter filter = new RepeatableMatrixFilter(
 				3,
 				3,
 				10,
 				new float[] {
 						0.0f, 0.0f, 0.0f,
 						0.0f, 1.0f, 0.0f,
 						0.0f, 0.0f, 0.0f,
 				}
 		);
 		
 		time1 = perform(filter, SIZE, SIZE);
 		System.out.println("Time elapsed: " + time1 + " ms");
 		time2 = perform(new ImageFilterThreadingWrapper(filter), SIZE, SIZE);
 		System.out.println("Time elapsed: " + time2 + " ms");
 		
 		System.out.println("Decrease of " + (100d - ((double)time2 / (double)time1 * 100)) + "%");
 
 		filter = new RepeatableMatrixFilter(
 				5,
 				5,
 				10,
 				new float[] {
 						0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
 						0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
 						0.0f, 0.0f, 1.0f, 0.0f, 0.0f,
 						0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
 						0.0f, 0.0f, 0.0f, 0.0f, 0.0f
 				}
 		);
 		
 		time1 = perform(filter, SIZE, SIZE);
 		System.out.println("Time elapsed: " + time1 + " ms");
 		time2 = perform(new ImageFilterThreadingWrapper(filter), SIZE, SIZE);
 		System.out.println("Time elapsed: " + time2 + " ms");
 		
 		System.out.println("Decrease of " + (100d - ((double)time2 / (double)time1 * 100)) + "%");
 		
 		//System.gc();
 		//System.exit(0);
 	}
 }
