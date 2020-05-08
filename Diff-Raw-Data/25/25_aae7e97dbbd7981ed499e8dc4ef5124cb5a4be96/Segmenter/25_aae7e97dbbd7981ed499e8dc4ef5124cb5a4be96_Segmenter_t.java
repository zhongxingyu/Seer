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
 	
 	private final static int cannyContourMinimumSize = 20; 
 
 	public static List<Region> segment(ImageUInt8 image) {
 		ArrayList<Region> thresholdRegionList = new ArrayList<Region>();
 		ArrayList<Region> cannyRegionList = new ArrayList<Region>();
 
 		// Downsample and determine mean pixel value to use as threshold
 		// Do we want to do this over subimages to deal with different lighting?
 		Segmenter s = new Segmenter(image);
 		double threshold = s.getMean();
 		Log.i("Setalyzer", "THreshold value is " + threshold);
 		// Blur
 		ImageUInt8 blurred = boofcv.alg.filter.blur.BlurImageOps.mean(image, null, 5, null);
 		// Threshold to generate binary image
 		ImageUInt8 binary = boofcv.alg.filter.binary.ThresholdImageOps.threshold(blurred, null, (int)threshold, false);
 		
 		// Canny to get contours
 		List<List<Point2D_I32>> cannyContours = LineDetector.cannyEdgeDetect(blurred);
 		
 		// We may want to dialate the image before labeling its blobs?
		// Label blobs 
 		ImageSInt32 out = FactoryImage.create(ImageSInt32.class, image.getWidth(), image.getHeight());
 		int numBlobs = boofcv.alg.filter.binary.BinaryImageOps.labelBlobs4(binary, out);
 		Log.i("Setalyzer", "Blobs found:" + numBlobs);
 		//Segmenter.test = out;
 		//for (int i = 0; i < binary.data.length; i++) {
 		//	if (binary.data[i] == 1) {
 		//		binary.data[i] = (byte) 0xff;
 		//	}
 		//}
 		//Segmenter.test = binary;
 
 		
 		@SuppressWarnings("unchecked")
 		List<Point2D_F64>[] points = new ArrayList[numBlobs];
 		for (int i = 1; i < numBlobs; i++) {
 			//TODO: make fast
 			ArrayList<Point2D_F64> inClass = new ArrayList<Point2D_F64>();
 			for (int x = 0; x < out.getWidth(); x++) {
 				for (int y = 0; y < out.getHeight(); y++) {
 					if(out.get(x, y) == i) {
 						inClass.add(new Point2D_F64(x, y));
 					}
 				}
 			}
 			// Transform blobs into quadrilaterals, ignoring blobs of very small size
 			if (inClass.size() > 10) {
 				points[i] = boofcv.alg.feature.detect.quadblob.FindBoundingQuadrilateral.findCorners(inClass);
 			} else {
 				points[i] = null;
 			}
 		}
 		
 		// Generate quads for canny edges
 		List<List<Point2D_F64>> cannyQuads = new ArrayList<List<Point2D_F64>>();
 		for (int i = 0; i< cannyContours.size(); i++) {
 			List<Point2D_I32> contour = cannyContours.get(i);
 			if (contour.size() < cannyContourMinimumSize) {
 				continue;
 			}
 			List<Point2D_F64> f64Contour = new ArrayList<Point2D_F64>();
 			for (Point2D_I32 point: contour) {
 				f64Contour.add(new Point2D_F64(point.x, point.y));
 			}
 			Log.i("Setalyzer", f64Contour.toString());
 			cannyQuads.add(boofcv.alg.feature.detect.quadblob.FindBoundingQuadrilateral.findCorners(f64Contour));
 		}
 		
 		
 		
 		// Convert bounding quads into regions.
 		for (List<Point2D_F64> quad : points) {
 			if (quad == null) {
 				continue;
 			}
 			thresholdRegionList.add(convertQuadToRegion(quad, image.getWidth(), image.getHeight()));
 		}
 		for (List<Point2D_F64> quad: cannyQuads) {
 			if (quad == null) {
 				continue;
 			}
 			cannyRegionList.add(convertQuadToRegion(quad, image.getWidth(), image.getHeight()));
 		}
 		
 		
 		return thresholdRegionList;
 //		return cannyRegionList;
 	}
 	
	// Assumes that the points are ordered going around the quadrilateral
	private static double areaOfQuad(List<Point2D_F64> quad) {
		double area1 = boofcv.alg.feature.detect.quadblob.FindBoundingQuadrilateral.area(quad.get(0), quad.get(2), quad.get(1));
		double area2 = boofcv.alg.feature.detect.quadblob.FindBoundingQuadrilateral.area(quad.get(0), quad.get(2), quad.get(3));
		
		return area1 + area2;
	}
	
 	private static Region convertQuadToRegion(List<Point2D_F64> quad, int width, int height) {
 		if (quad == null)
 			return null;
 		Region r = new Region();
 		Path p = null;
 		for (Point2D_F64 po : quad) {
 			if (p == null) {
 				p = new Path();
 				p.moveTo((float)po.x, (float)po.y);
 			} else
 				p.lineTo((float)po.x, (float)po.y);
 		}
 		p.close();
 		r.setPath(p, new Region(0, 0, width, height));
 		return r;
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
 }
