 package com.zenbox;
 
 import org.opencv.core.Core;
 import org.opencv.core.CvType;
 import org.opencv.core.Mat;
 import org.opencv.core.MatOfPoint2f;
 import org.opencv.core.Point;
 import org.opencv.core.Scalar;
 import org.opencv.imgproc.Imgproc;
 
 import android.util.Log;
 
 public class ZoneProcessor {
 	
 	private static final int GRID_WIDTH = 4;
 	private static final int GRID_HEIGHT = 4;
 	private static final int GRID_AREA = GRID_WIDTH * GRID_HEIGHT;
 	private static final String TAG = "ZoneProcessor";
 	
 	// Buffers for the number of features in each zone, as well
 	// as the overall HSV of the zone.
 	public Float[] mNumFeatures;
 	public Float[] mHue;
 	public Float[] mSat;
 	public Float[] mVal;
 	
 	// The cells for each frame.  One frame fills a cell.
 	private Mat[] mCells;
 	
 	// Intermediate matrix for image manipulation.
 	private Mat mHSV;
 	
 	public ZoneProcessor(int height, int width) {
 		mNumFeatures = new Float[GRID_AREA];
 		mHue = new Float[GRID_AREA];
 		mSat = new Float[GRID_AREA];
 		mVal = new Float[GRID_AREA];
 		mCells = new Mat[GRID_AREA];
 		
 
 		mHSV = new Mat(height, width, CvType.CV_8UC3);
 		
 		// Chop up img into submats.
 		for (int i = 0; i < GRID_HEIGHT; ++i) {
 			for (int j = 0; j < GRID_WIDTH; ++j) {
 				int k = i * GRID_WIDTH + j;
 				mCells[k] = mHSV.submat(i * mHSV.rows() / GRID_HEIGHT,
 						(i + 1) * mHSV.rows() / GRID_HEIGHT,
 						j * mHSV.cols() / GRID_WIDTH,
 						(j + 1) * mHSV.cols() / GRID_WIDTH);
 			}
 		}
 	}
 	
 	/**
 	 * Processes the zones of the image and stores the average Hue, Saturation, and Values within the
 	 * internal buffers.
 	 * @param img
 	 * @return The image (for now) with numbers drawn on each cell.
 	 */
 	public synchronized Mat processZones(Mat img) {
 		Imgproc.cvtColor(img, mHSV, Imgproc.COLOR_RGB2HSV_FULL);
 		// Process avg HSV of each submat.
 		for (int i = 0; i < GRID_AREA; ++i) {
 			double[] hsv = Core.mean(mCells[i]).val;
 			Log.e("ZoneProcessor", "Hue: " + Double.toString(hsv[0]));
 			mHue[i] = (float) hsv[0];
 			mSat[i] = (float) hsv[1];
 			mVal[i] = (float) hsv[2];
 		}
         return img;
 	}
 	
 	public void setNumFeatures(Mat img, MatOfPoint2f features) {
 		// (little hacky), get the total width and height of each one of the columns.
 		int width = mCells[0].width();
 		int height = mCells[0].height();
 		
 		// Zero out array first.
 		for (int i = 0; i < mNumFeatures.length; ++i) {
 			mNumFeatures[i] = 0f;
 		}
 		
		// Set the values for each square appropriately.  This is also a bit of a hack,
		// as all of the points accrued through the image have been downsampled;  hence the
		// *2 at the end of the x/y values.
 		Point[] points = features.toArray();
 		for (int i = 0; i < points.length; ++i) {
 			final Point p = points[i];
			int x = (int) Math.floor((p.x * 2) / width);
			int y = (int) Math.floor((p.y * 2) / height);
			int k = y * GRID_WIDTH + x;
 			++mNumFeatures[k];
 		}
 		
 		// Super slow, but we need to debug!
 		Imgproc.cvtColor(mHSV, mHSV, Imgproc.COLOR_HSV2RGB_FULL);
 		for (int i = 0; i < mNumFeatures.length; ++i) {
 			Core.putText(mCells[i],
 					String.format("%2.0f", mNumFeatures[i]),
 					new Point(10, 40),
                     3/* CV_FONT_HERSHEY_COMPLEX */,
                     1,
                     new Scalar(255, 0, 255, 255),
                     2);
 		}
 		mHSV.copyTo(img);
 		Imgproc.cvtColor(mHSV, mHSV, Imgproc.COLOR_RGB2HSV_FULL);
 	}
 }
