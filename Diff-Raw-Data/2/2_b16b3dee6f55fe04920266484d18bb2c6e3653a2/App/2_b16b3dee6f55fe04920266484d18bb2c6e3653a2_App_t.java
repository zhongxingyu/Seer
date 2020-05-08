 package com.timetravellingtreasurechest.vision;
 
 import static com.googlecode.javacv.cpp.opencv_core.*;
 import static com.googlecode.javacv.cpp.opencv_highgui.CV_LOAD_IMAGE_COLOR;
 import static com.googlecode.javacv.cpp.opencv_highgui.cvLoadImageM;
 
 import java.util.List;
 
 import com.googlecode.javacv.CanvasFrame;
 
 public class App {
 	public static void main(String[] args) {   	
 		FacialFeatures current;
 
         for (int i = 0; i < args.length; i++) {
         	
         	CvMat image = cvLoadImageM(args[i], CV_LOAD_IMAGE_COLOR);
         	if (image.size() == 0) {
     			System.out.println("error: cannot open " + image + "\n");
    			continue;
         	}
         	
         	current = new FacialFeatures(image);
         	
         	List<FacialFeature> features = current.getFeatures();
         	
         	for(FacialFeature f : features) 
         		putRects(image, f.boundingBox); // face rectangle
         	
         	System.out.println(args[i] + ": ");
 			System.out.println("\tforeheadSize: " + current.foreheadHeight);
 			System.out.println("\teyeSize: " + current.eyeSize);
 			System.out.println("\teyeSpace: " + current.eyeSpace);
 			System.out.println("\tnoseSize: " + current.noseSize);
 			System.out.println("\tnoseHeight: " + current.noseHeight);
 			System.out.println("\tnoseWidth: " + current.noseWidth);
 			System.out.println("\tmouthSize: " + current.mouthSize);
 			System.out.println("\tmouthHeight: " + current.mouthHeight);
 			System.out.println("\tmouthWidth: " + current.mouthWidth);
 			System.out.println();
         	
         	try {
         		CanvasFrame canvas = new CanvasFrame("Face with boxes");
 				canvas.showImage(image.asIplImage());				
 				canvas.waitKey();
 				canvas.dispose();
         	} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
         	 
         }
     }
 	
 	// draw rects on image relative to offsets given (for roi placement)
 	// only used for testing (if OUTPUT_IMAGE is set), not for final project
 	public static void putRects(CvMat image, CvRect rect) {
 		if (rect == null)
 			return;
 
 		cvRectangle(image, new CvPoint(rect.x(), rect.y()), new CvPoint(rect.x() + rect.width(), rect.y() + rect.height()), new CvScalar(0, 255, 0, 0), 1, 8, 0);
 	}
 }
