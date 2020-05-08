 package com.gmail.at.sabre.alissa.numberplace.capture;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.opencv.core.Core;
 import org.opencv.core.CvType;
 import org.opencv.core.Mat;
 import org.opencv.core.MatOfPoint;
 import org.opencv.core.MatOfPoint2f;
 import org.opencv.core.Point;
 import org.opencv.core.Rect;
 import org.opencv.core.Scalar;
 import org.opencv.core.Size;
 import org.opencv.imgproc.Imgproc;
 
 import com.gmail.at.sabre.alissa.ocr.Ocr;
 
 public class ImageProcessing {
 	public static boolean recognize(Ocr ocr, Mat source, Mat result, byte[][] puzzle) {
 		// A default image used in case of an error.
 		// this is a small, random image (consisting of uninitialized native memory.)
 		result.create(32, 32, CvType.CV_8UC1);
 		
 		// Prepare a clean gray scale image of the source for processing.
 		// The source must be in RGBA color format.
 		final Mat gray = new Mat();
 		makeGrayImage(source, gray);
 		
 		// Recognize blobs.
 		final List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
 		final Mat hierarchy = new Mat();
 		findContours(gray, contours, hierarchy);
 		
 		// Find an outer contour of a blob that is most likely 
 		// of the puzzle frame.
 		MatOfPoint frame_contour = chooseFrameContour(gray.size(), contours, hierarchy);
 		if (frame_contour == null) return false;
 		
 		contours.clear();
 		hierarchy.release();
 
 		// Fit the frame contour to a quadrangle
 		final Point[] frame_quad = QuadrangleFitter.fit(frame_contour);
 
 		// Assuming the fit quadrangle is a good estimation of the
 		// out most border of the puzzle board, and it was a right
 		// square on its original surface, perform a reverse
 		// perspective adjustment to the original gray scale image
 		// to get the right gray image of the puzzle.
 		// Because the quadrangle is only an estimate and the 
 		// puzzle may extends beyond it, we prepare an image
 		// of 11x11 virtual cells and map the puzzle image into
 		// the central 9x9.  We set arbitrarily the size (in pixels)
 		// of the unit virtual cell.
 		final int UNIT = 80;
 		final Mat right = new Mat();
 		adjustPerspective(gray, right, frame_quad, UNIT);
 		
 		// Recognize fixed digits.
 		recognizeDigits(right, UNIT, ocr, puzzle);
 		
 		{
 			final Mat tmp = new Mat();
 			Imgproc.cvtColor(right, tmp, Imgproc.COLOR_GRAY2RGBA);
 			Scalar color = new Scalar(255, 0, 0, 255);
 			for (int i = UNIT; i <= UNIT * 10; i += UNIT) {
 				Core.line(tmp, new Point(i, UNIT), new Point(i, UNIT * 10), color);
 				Core.line(tmp, new Point(UNIT, i), new Point(UNIT * 10, i), color);
 			}
 			tmp.copyTo(result);
 			tmp.release();
 		}
 		
 		gray.release();
 		right.release();
 		
 		return true;
 	}
 	
 	/***
 	 * Make a gray scale image from an RGBA image, denoising.
 	 * @param src An RGBA image of type CV_8UC4. 
 	 * @param dst A gray scale image of type CV_8UC1.
 	 */
 	private static void makeGrayImage(Mat src, Mat dst) {
 		final Mat tmp = new Mat();
 		Imgproc.GaussianBlur(src, tmp, new Size(5, 5), 0f, 0f);
 		Imgproc.cvtColor(tmp, dst, Imgproc.COLOR_RGBA2GRAY);
 		tmp.release();
 	}
 	
 	/***
 	 * Perform a blob analysis on the source gray scale image and
 	 * return blob contours information.  This is a wrapper for
 	 * OpenCV Imgproc.findContours().
 	 * @param src A gray scale image for analysis.  This image is not modified.
 	 * @param contours List of contours to be filled upon return.
 	 * @param hierarchy List of hierarchy info to be filled upon return.
 	 */
 	private static void findContours(Mat src, List<MatOfPoint> contours, Mat hierarchy) {
 		// We use adaptive threasholding, because the lighting is often not
 		// uniform on the puzzle picture. (For example, upper right is very
 		// bright and right half is dark.)  The block size is a key to get 
 		// a good binary image.  We use a block size proportional to the
 		// image size, that is expected to be similar size relative to the 
 		// puzzle frame.  The actual constant 16 is chosen by experiments.
 		// (along with the offset constant 2.)
 		final int blockSize = (Math.min(src.width(), src.height()) / 16) | 1; // make the blocksize odd so that a block has a center pixel.
 		final Mat tmp = new Mat();
 		Imgproc.adaptiveThreshold(src, tmp, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY_INV, blockSize, 2);
 		
 		// Run analysis.
 		contours.clear();
 		Imgproc.findContours(tmp, contours, hierarchy, Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_SIMPLE);
 		tmp.release();
 	}
 	
 	/***
 	 * Find a blob that is most likely the number place puzzle frame,
 	 * and return its outer contour.
 	 * @param image_size The size of the image where the contours are on.
 	 * @param contours The list of contours to choose from.
 	 * @param hierarchy The hierarchy info for the contours.
 	 * @return The outer contour of the most likely puzzle frame.
 	 */
 	private static MatOfPoint chooseFrameContour(Size image_size, List<MatOfPoint> contours, Mat hierarchy) {
 		// Find a blob that is most likely the number place puzzle frame.
 		// We use two criteria:
 		// (1) the blob has at least nine and at most 81 holes, and
 		// (2) the blob covers the center of the image.
 		// 
 		// A number place puzzle is shown in a 9x9 (81) bordered cells.
 		// if all the borders are fully recognized as a blob, it has 81
 		// holes.  However, the cells are divided into nine 3x3 blocks,
 		// with major borders divide the blocks and minor borders divide
 		// cells in a block.  Minor borders are thin and in a lighter
 		// color than major borders and/or digits, so parts of minor
 		// borders often disappear or are broken in the binary image.
 		// If all minor borders were ignored, only major borders remain,
 		// and the puzzle frame blob would have nine holes.  That's why
 		// our criteria (1) is "9 to 81".
 		// 
 		// The second criterion is ad hoc.  Since this app is dedicated
 		// for recognizing the number place puzzle, when shooting,
 		// users are expected to _center_  the primary subject, the puzzle.
 		// When a user shoots a page of a puzzle magazine, for example, the
 		// page may show two or more number place puzzles, and the picture
 		// will very likely contain parts of other puzzles.  The
 		// "covers center" criterion effectively eliminates them.
 		// 
 		List<Integer> candidates = new ArrayList<Integer>();
 		int[] hinfo = new int[4];
 		Point center = new Point(image_size.width / 2f, image_size.height / 2f);
 		for (int i = 0; i < contours.size(); i++) {
 			hierarchy.get(0, i, hinfo);
 			
 			if (hinfo[3] >= 0) {
 				// this is an internal contour (a contour of a hole) of a blob.
 				// just skip it.
 				continue;
 			}
 			
 			// count how many holes this blob has.
 			int holes = 0;
 			for (int p = hinfo[2]; p >= 0; p = hinfo[0]) {
 				++holes;
 				hierarchy.get(0, p, hinfo);
 			}
 			
 			// with too few or too many holes, reject it.
 			if (holes < 9 || holes > 81) {
 				continue;
 			}
 			
 			// see if it covers the center of the original image.
 			// I want to use Imgproc.pointPolygonTest(contours.get(i), center, false),
 			// but it's hard to do so because of the data type discrepancy...
 			// Use of boundingRect is a cheap alternative.
 			if (Imgproc.boundingRect(contours.get(i)).contains(center)) {
 				candidates.add(i);
 			}
 		}
 
 		// If we have no candidates, give up.
 		if (candidates.size() == 0) return null;
 		
 		// If we have two or more candidates, take the first one, praying!
 		// (We should add more criteria, or should we run the quad-fit for 
 		// remaining candidates and evaluate the fitness.  FIXME!)
 		return contours.get(candidates.get(0));
 	}
 	
 	private static void adjustPerspective(Mat src, Mat dst, Point[] quad, int unit) {
 		Point[] goal = new Point[] {
 				new Point(unit,      unit),
 				new Point(unit * 10, unit),
 				new Point(unit * 10, unit * 10),
 				new Point(unit,      unit * 10)
 		};
 		Mat matrix = Imgproc.getPerspectiveTransform(new MatOfPoint2f(quad), new MatOfPoint2f(goal));
 		Size size = new Size(unit * 11, unit * 11);
 		dst.create(size, src.type());
 		Imgproc.warpPerspective(src, dst, matrix, size, Imgproc.INTER_LINEAR, Imgproc.BORDER_REPLICATE, new Scalar(0,0,0,0));
 		matrix.release();
 	}
 	
 	private static void recognizeDigits(Mat src, int unit, Ocr ocr, byte[][] puzzle) {
 		final Mat tmp = new Mat(unit * 2, unit * 2, CvType.CV_8UC1);
 		for (int y = 0; y < 9; y++) {
 			for (int x = 0; x < 9; x++) {
 				final Rect window = new Rect(x * unit + unit / 2, y * unit + unit / 2, unit * 2, unit * 2);
 				final Mat srcWindow = src.submat(window);
 				Imgproc.threshold(srcWindow, tmp, 0, 255, Imgproc.THRESH_BINARY_INV | Imgproc.THRESH_OTSU);
 				
 				final List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
 				final Mat hierarchy = new Mat();
 				Imgproc.findContours(tmp, contours, hierarchy, Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_SIMPLE);
 				
 				double maxArea = 0;
 				Rect maxRect = null;
 				final int[] hinfo = new int[4];
 				for (int i = 0; i < contours.size(); i++) {
 					hierarchy.get(0, i, hinfo);
 					if (hinfo[3] >= 0) continue;
 					
 					final MatOfPoint points = contours.get(i);
 					final Rect rect = Imgproc.boundingRect(points);
 					points.release();
 					
 					if (rect.width >= unit || rect.height >= unit) continue;
					if (rect.width < unit / 3 || rect.height < unit / 3) continue;
 					
 					final int midX = rect.x + rect.width / 2;
 					final int midY = rect.y + rect.height / 2;
 					if (midX <= unit / 2 || midX >= unit + unit / 2 ||
 						midY <= unit / 2 || midY >= unit + unit / 2) continue;
 					
 					double area = rect.area();
 					if (area > maxArea) {
 						maxArea = area;
 						maxRect = rect;
 					}
 				}
 				
 				contours.clear();
 				hierarchy.release();
 				
 				if (maxRect != null) {
 					Imgproc.threshold(srcWindow, tmp, 0, 255, Imgproc.THRESH_BINARY_INV | Imgproc.THRESH_OTSU);
 					final Mat tmpChar = tmp.submat(maxRect);
 					puzzle[y][x] = Byte.parseByte(ocr.recognize(tmpChar));
 					tmpChar.release();
 				}
 				
 				srcWindow.release();
 			}
 		}
 		tmp.release();
 	}
 
 
 }
