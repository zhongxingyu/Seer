 package edu.mines.acmX.exhibit.stdlib.input_processing.tracking;
 
 import org.OpenNI.DepthGenerator;
 import org.OpenNI.Point3D;
 import org.OpenNI.StatusException;
 
 import edu.mines.acmX.exhibit.stdlib.graphics.Coordinate3D;
 
 public class HandTrackingUtilities {
 	
 	/**
 	 * Converts a Point3D provided by OpenNI to use a generic Coordinate3D.O
 	 * @param gen OpenNI DepthGenerator
 	 * @param point OpenNI Point3D for the given hand
 	 * @return a POJO Coordinate3D
 	 * 
 	 * @see {@link Coordinate3D}
 	 */
 	public static Coordinate3D convertOpenNIPoint(DepthGenerator gen, Point3D point) {
 		Point3D convertedPoint = null;
 		Coordinate3D ret = null;
 		try {
 			convertedPoint = gen.convertRealWorldToProjective(point);
 			ret = new Coordinate3D(convertedPoint.getX(),
 					convertedPoint.getY(),
 					convertedPoint.getZ());
 		} catch (StatusException e) {
 			e.printStackTrace();
 		}
 		
 		return ret;
 	}
 	
 	/**
 	 * Scales a given hand coordinate to fill the user frame, and zooms the sensitive area of
 	 * the hand device.
 	 * 
 	 * @param handX The unmodified hand position (float)
 	 * @param depthWidth The width of the frame used by the depth sensor (int)
 	 * @param frameWidth The width of the window to be converted to (int)
 	 * @param marginFraction The fraction of the screen to trim from the edges (float)
 	 * @return The scaled hand position (int)
 	 */
 	public static int getScaledHandX(float handX, int depthWidth, int frameWidth, float marginFraction) {
		//if no scaling is desired, 0 or any negative number will calculate a flat ratio of screen sizes
		if (marginFraction <= 0) return (int) (handX * (float) frameWidth/depthWidth);
		// if the margin is greater than 1/2 (would give negative remaining size), return flat ratio
		if (marginFraction > 0.5) return (int) (handX * (float) frameWidth/depthWidth);
 		//get margin size (fraction of total screen size)
		float marginX = depthWidth * marginFraction;
 		//find remainder of depth image to zoom into
 		float scaledDepthWidth = depthWidth - 2 * marginX;
 		//translate coordinate to new zoomed frame, and then perform screen size ratio
 		int newX = (int) ((handX - marginX) * frameWidth/scaledDepthWidth);
 		//if the calculated value is outside of the bounds of [0, frameWidth], return closest valid value
 		if (newX < 0) return 0;
 		if (newX > frameWidth) return frameWidth;
 		return newX;
 		
 	}
 	
 	/**
 	 * Scales a given hand coordinate to fill the user frame, and zooms the sensitive area of
 	 * the hand device.
 	 * 
 	 * @param handY The unmodified hand position (float)
 	 * @param depthHeight The height of the frame used by the depth sensor (int)
 	 * @param frameHeight The height of the window to be converted to (int)
 	 * @param marginFraction The fraction of the screen to trim from the edges (float)
 	 * @return The scaled hand position (int)
 	 */
 	public static int getScaledHandY(float handY, int depthHeight, int frameHeight, float marginFraction) {
		//if no scaling is desired, 0 or any negative number will calculate a flat ratio of screen sizes
		if (marginFraction <= 0) return (int) (handY * (float) frameHeight/depthHeight);
		// if the margin is greater than 1/2 (would give negative remaining size), return flat ratio
		if (marginFraction > 0.5) return (int) (handY * (float) frameHeight/depthHeight);
 		//get margin size (fraction of total screen size)
		float marginY = depthHeight * marginFraction;
 		//find remainder of depth image to zoom into
 		float scaledDepthHeight = depthHeight - 2 * marginY;
 		//translate coordinate to new zoomed frame, and then perform screen size ratio
 		int newY = (int) ((handY - marginY) * frameHeight/scaledDepthHeight);
 		//if the calculated value is outside of the bounds of [0, frameHeight], return closest valid value
 		if (newY < 0) return 0;
 		if (newY > frameHeight) return frameHeight;
 		return newY;
 	}
 }
