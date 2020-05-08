 package com.quimian.setalyzer;
 
 import georegression.struct.point.Point2D_F64;
 import georegression.struct.point.Point2D_I32;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import boofcv.struct.image.FactoryImage;
 import boofcv.struct.image.ImageSInt32;
 import boofcv.struct.image.ImageUInt8;
 
 import android.graphics.Path;
 import android.graphics.Region;
 import android.util.Log;
 
 public class Segmenter {
 	public static ImageUInt8 test;
 	
 	private static final double ASPECT_RATIO_TOLERANCE = 0.80;    // aspect ratios within this percent of the target are card shaped
 	private	static final double TARGET_ASPECT_RATIO = 0.64;		 // aspect ratio of a set card
 	private static final double SLOPE_PARALLEL_TOLERANCE = 0.70;  // slopes within this percent are parallel
 	private static final int 	EDGE_SIZE_THRESHOLD = 100;
 	private static final int 	BLOB_SIZE_THRESHOLD = 10;
 	private static final double CARD_AREA_TOLERANCE = 0.80;
 
 	public static List<float[]> segment(ImageUInt8 image) {
 		ArrayList<float[]> thresholdRegionList = new ArrayList<float[]>();
 		ArrayList<float[]> cannyRegionList = new ArrayList<float[]>();
 
 		ImageUInt8 binary = thresholdImage(image);
 
 		// todo: We may want to dialate the image before labeling its blobs?
 		// Label blobs 
 		ImageSInt32 out = FactoryImage.create(ImageSInt32.class, image.getWidth(), image.getHeight());
 		int numBlobs = boofcv.alg.filter.binary.BinaryImageOps.labelBlobs4(binary, out);
 		Log.i("Setalyzer", "Blobs found:" + numBlobs);
 		
		List<Point2D_F64>[] points = quadsFromBlobs(out, numBlobs);
 		
 		// Generate quads for canny edges
 //		List<List<Point2D_F64>> cannyQuads = new ArrayList<List<Point2D_F64>>();
 //		for (int i = 0; i< cannyContours.size(); i++) {
 //			List<Point2D_I32> contour = cannyContours.get(i);
 //			if (contour.size() < cannyContourMinimumSize) {
 //				continue;
 //			}
 //			List<Point2D_F64> f64Contour = new ArrayList<Point2D_F64>();
 //			for (Point2D_I32 point: contour) {
 //				f64Contour.add(new Point2D_F64(point.x, point.y));
 //			}
 ////			Log.i("Setalyzer", f64Contour.toString());
 //			cannyQuads.add(boofcv.alg.feature.detect.quadblob.FindBoundingQuadrilateral.findCorners(f64Contour));
 //		}
 //		
 //		
 //		
		// Convert bounding quads into regions.
		for (List<Point2D_F64> quad : points) {
			if (quad == null) {
				continue;
			}
			thresholdRegionList.add(convertQuadToRegion(quad, image.getWidth(), image.getHeight()));
		}
 //		for (List<Point2D_F64> quad: cannyQuads) {
 //			if (quad == null) {
 //				continue;
 //			}
 //			cannyRegionList.add(convertQuadToRegion(quad, image.getWidth(), image.getHeight()));
 //		}
 		
 		
 		return thresholdRegionList;
 //		return cannyRegionList;
 	}
 	
 	// convert a blob image into quadrilaterals bounding each of the blobs
 	public static List<Point2D_F64>[] quadsFromBlobs(ImageSInt32 blobImage, int numBlobs) {
 		@SuppressWarnings("unchecked")
 		List<Point2D_F64>[] points = new ArrayList[numBlobs];
 		for (int i = 1; i < numBlobs; i++) {
 			//TODO: make fast
 			ArrayList<Point2D_F64> inClass = new ArrayList<Point2D_F64>();
 			for (int x = 0; x < blobImage.getWidth(); x++) {
 				for (int y = 0; y < blobImage.getHeight(); y++) {
 					if(blobImage.get(x, y) == i) {
 						inClass.add(new Point2D_F64(x, y));
 					}
 				}
 			}
 			// Transform blobs into quadrilaterals, ignoring blobs of very small size
 			if (inClass.size() > BLOB_SIZE_THRESHOLD) {
 				points[i] = boofcv.alg.feature.detect.quadblob.FindBoundingQuadrilateral.findCorners(inClass);
 			} else {
 				points[i] = null;
 			}
 		}
 		return points;
 	}
 	
 	public static ImageUInt8 thresholdImage(ImageUInt8 image) {
 		// Downsample and determine mean pixel value to use as threshold
 		// Do we want to do this over subimages to deal with different lighting?
 		Segmenter s = new Segmenter(image);
 		double threshold = s.getMean();
 		Log.i("Setalyzer", "THreshold value is " + threshold);
 		
 		// Blur
 		ImageUInt8 blurred = boofcv.alg.filter.blur.BlurImageOps.mean(image, null, 5, null);
 		// Threshold to generate binary image
 		ImageUInt8 binary = boofcv.alg.filter.binary.ThresholdImageOps.threshold(blurred, null, (int)threshold, false);
 		
 		
 		return binary;
 	}
 	
 	// Assumes that the points are ordered going around the quadrilateral
 	private static double areaOfQuad(List<Point2D_F64> quad) {
 		double area1 = boofcv.alg.feature.detect.quadblob.FindBoundingQuadrilateral.area(quad.get(0), quad.get(2), quad.get(1));
 		double area2 = boofcv.alg.feature.detect.quadblob.FindBoundingQuadrilateral.area(quad.get(0), quad.get(2), quad.get(3));
 		
 		return area1 + area2;
 	}
 	
 	private static float[] convertQuadToRegion(List<Point2D_F64> quad, int width, int height) {
 		if (quad == null)
 			return null;
 		float[] f = new float[2 * quad.size()];
 		for (int i = 0; i < quad.size(); i++) {
 			f[2*i] = (float)quad.get(i).x;
 			f[2*i + 1] = (float)quad.get(i).y;
 		}
 		return f;
 	}
 
 	private ImageUInt8 sample;
 	private double sum;
 
 	private Segmenter(ImageUInt8 image) {
 		int sampleWidth = 300;
 
 		double scale = (1.0 * image.getWidth()) / sampleWidth;
 		int sampleHeight = (int)(image.getHeight() * scale);
 		ImageUInt8 sample = new ImageUInt8(sampleWidth, sampleHeight);
 		for (int x = 0; x < sampleWidth; x++) {
 			for (int y = 0; y < sampleHeight; y++) {
 				int origX = (int)Math.floor(x*scale);
 				int origY = (int)Math.floor(y*scale);
 				if (image.isInBounds(origX, origY)) {
 					int value = image.get(origX, origY);
 					sum += value;
 					sample.set(x, y, value);
 				}
 			}
 		}
 		
 		this.sample = sample;
 	}
 	
 	private double getMean() {
 		return sum / (1.0 * sample.getWidth() * sample.getHeight());
 	}
 	
 	public static boolean topNSimilarSized(int[] areas, int n) {
 		int sum = 0;
 		int lastIndex = areas.length - 1;
 		for (int i=0; i<n; i++) {
 			sum += areas[lastIndex-i];
 		}
 		double average = (double)sum/(double)n;
 		
 		for (int i=0; i<n; i++) {
 			if (Math.min(areas[lastIndex-i], average) / Math.max(areas[lastIndex-i],  average) < CARD_AREA_TOLERANCE) {
 				return false;
 			}
 		}
 		return true;
 	}
 
 	public static int areaOfRegion(List<Point2D_I32> list) {
 		// turn it into a quad
 		List<Point2D_F64> f64Contour = new ArrayList<Point2D_F64>();
 		for (Point2D_I32 point: list) {
 			f64Contour.add(new Point2D_F64(point.x, point.y));
 		}
 		
 		List<Point2D_F64> quad;
 		try {
 			quad = boofcv.alg.feature.detect.quadblob.FindBoundingQuadrilateral.findCorners(f64Contour);
 		}
 		catch (Exception e) {
 			System.out.println("finding corners of bounding quadrilateral failed");
 			return -1;
 		}
 		
 		double triangleArea0 = boofcv.alg.feature.detect.quadblob.FindBoundingQuadrilateral.area(quad.get(0), quad.get(1), quad.get(2));
 		double triangleArea1 = boofcv.alg.feature.detect.quadblob.FindBoundingQuadrilateral.area(quad.get(0), quad.get(3), quad.get(2));
 		
 		return (int) Math.round(triangleArea1 + triangleArea0);
 		
 	}
 
 	public static boolean isCardShaped(List<Point2D_I32> contour) {
 		// turn it into a quad
 		List<Point2D_F64> f64Contour = new ArrayList<Point2D_F64>();
 		for (Point2D_I32 point: contour) {
 			f64Contour.add(new Point2D_F64(point.x, point.y));
 		}
 		
 		List<Point2D_F64> quad;
 		try {
 			quad = boofcv.alg.feature.detect.quadblob.FindBoundingQuadrilateral.findCorners(f64Contour);
 		}
 		catch (Exception e) {
 			System.out.println("finding corners of bounding quadrilateral failed");
 			return false;
 		}
 	
 		Double ratio = aspectRatio(quad);
 		if (ratio == null) {
 			return false;
 		}
 		if (Math.min(ratio, TARGET_ASPECT_RATIO) / Math.max(ratio, TARGET_ASPECT_RATIO) < ASPECT_RATIO_TOLERANCE ) {
 			return true;
 		}
 		return false;
 	}
 
 	private static Double aspectRatio(List<Point2D_F64> quad) {
 		Double d0 = euclidianDistance(quad.get(0), quad.get(1));
 		Double d1 = euclidianDistance(quad.get(0), quad.get(2));
 		Double d2 = euclidianDistance(quad.get(1), quad.get(3));
 		Double d3 = euclidianDistance(quad.get(3), quad.get(0));
 		
 		Double s0 = slope(quad.get(0), quad.get(1));
 		Double s1 = slope(quad.get(0), quad.get(2));
 		Double s2 = slope(quad.get(1), quad.get(3));
 		Double s3 = slope(quad.get(3), quad.get(0));
 		
 		// ensure that things are roughly parallel - slopes of opposing sides are similar
 		if (Math.min(s0,s2) / Math.max(s0, s2) > SLOPE_PARALLEL_TOLERANCE) {
 			return null;
 		}
 		if (Math.min(s1,s3) / Math.max(s1, s3) > SLOPE_PARALLEL_TOLERANCE) {
 			return null;
 		}
 		
 		// average the lengths of opposing sides
 		Double meanSide0 = (d0+d2)/2;
 		Double meanSide1 = (d1+d3)/2;
 		
 		// return the ratio of the (averaged) sides as the aspect ratio
 		return Math.max(meanSide0, meanSide1) / Math.min(meanSide0, meanSide1);
 	}
 
 	private static Double slope(Point2D_F64 p0, Point2D_F64 p1) {
 		Double rise = p0.y - p1.y;
 		Double run = p0.x - p1.x;
 		return rise/run;
 	}
 
 	private static double euclidianDistance(Point2D_F64 p0, Point2D_F64 p1) {
 		return Math.sqrt(Math.pow(p0.x - p1.x, 2) + Math.pow(p0.y - p1.y, 2));
 	}
 
 }
