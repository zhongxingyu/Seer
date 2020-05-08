 package at.ppmrob.examples;
 
 import static com.googlecode.javacv.cpp.opencv_core.*;
 import static com.googlecode.javacv.cpp.opencv_imgproc.*;
 
 import java.util.Vector;
 
 import com.googlecode.javacpp.FloatPointer;
 import com.googlecode.javacpp.Pointer;
 import com.googlecode.javacv.cpp.opencv_core.IplImage;
 
 
 public class FeatureDetection {
 
 	private int height2 = 240; //240 144
 	private int width2 = 320; //320 176
 	
 	public FeatureDetection ()
 	{
 
 	}// FeatureDetection
 	
 	
 	public void initWidthAndHeight(int imgWidth, int imgHeight){
 //		this.imgWidth=imgHeight;
 //		this.imgHeight = imgWidth;
 	}
 	
 	
 	
 	/**
 	 * 
 	 * @param im2
 	 * @return
 	 */
 	public synchronized Vector<MyCircle> detectCircles(IplImage im, int width, int height)
 	{
 		
 //		IplImage img = IplImage.createFrom(im);
 		
 		Vector<MyCircle> detectedCircles = new Vector<MyCircle>();
 		
 		IplImage grayImgCircles = IplImage.create(width, height, IPL_DEPTH_8U, 1);
 
 		cvCvtColor(im, grayImgCircles, CV_BGR2GRAY);
 		
 		cvSmooth(grayImgCircles, grayImgCircles, CV_GAUSSIAN, 3);
 
 //		cvCircle(im, cvPoint(153, 130), 5, cvScalar(0, 255, 0, 0), 
 //    			-1,// line thickness 
 //    			8,// line type
 //    			0);// shift);		
 
 //	    cvCvtColor(im, im, CV_BGR2GRAY);
 
 	    CvMemStorage storage = CvMemStorage.create();
 
 	    CvSeq circles = cvHoughCircles(grayImgCircles, storage, CV_HOUGH_GRADIENT, 1, 7, 100, 70, 0, 0);
 
	    storage.release();
 	    
 	    System.out.println("circles detected: " + circles.total());
         for (int i = 0; i < circles.total(); i++) {
 
           FloatPointer fPoint = new FloatPointer(cvGetSeqElem(circles, i));
           
           int x= (int)fPoint.get(0);
           int y= (int)fPoint.get(1);
           int radius = (int)fPoint.get(2);
  
           detectedCircles.add(new MyCircle(new CvPoint(x,y), radius));
 //            if (y > 100 && y<350 && x >150 && x <224) {
 //                cvCircle(im, new CvPoint(x,y), 3, cvScalar(0, 255, 0, 0)/*CV_RGB(100, 0, 0)*/, -1, 8, 0);
 //                cvCircle(im, new CvPoint(x,y), radius, cvScalar(0, 255, 0, 0)/*CV_RGB(100, 0, 0)*/, 1, 8, 0);
         }
 
 		return detectedCircles;
 	}
 	
 	
 	
 	
 	
 	
 	/**
 	 * 
 	 * @param img
 	 * @param detectedCircles
 	 * @return
 	 */
 	public synchronized IplImage drawCircles(IplImage img, Vector<MyCircle> detectedCircles){
 		
 		for(int i=0; i<detectedCircles.size(); i++){
 		  cvCircle(img, detectedCircles.get(i).center, 3, CvScalar.GREEN/*CV_RGB(100, 0, 0)*/, -1, 8, 0);
           cvCircle(img, detectedCircles.get(i).center,  detectedCircles.get(i).radius, CvScalar.GREEN/*CV_RGB(100, 0, 0)*/, 1, 8, 0);
 		}
 		return img;
 	}
 
 	
 	
 	
 	
 	
 	/**
 	 * 
 	 * @param im
 	 * @return
 	 */
 	public synchronized Vector<MyLine> detectLines(IplImage im, int width, int height)
 	{
 		Vector<MyLine> detectedLines = new Vector<MyLine>();
     
 //	    cvSmooth(im, im, CV_BLUR, 3);//Smooth image (test)
 
 		IplImage grayImgLines= IplImage.create(width, height, IPL_DEPTH_8U, 1);
 
 		cvCvtColor(im, grayImgLines, CV_BGR2GRAY);
 
 	    CvMemStorage storage2 = CvMemStorage.create();
 
 	    IplImage imCanny = IplImage.create(width, height, IPL_DEPTH_8U, 1);
 //	    IplImage grayImgCanny = IplImage.create(320, 240, IPL_DEPTH_8U, 1);
 	
 	    cvCanny(grayImgLines, imCanny, 50, 200, 3);
 //	    cvCvtColor(imCanny, grayImgCanny, CV_BGR2GRAY);
 
 	    CvSeq lines = cvHoughLines2( imCanny, storage2, CV_HOUGH_PROBABILISTIC, 1, 3.1415926/180, 50, 80, 10 );
 	    System.out.println("lines detected: "+lines.total());
 	    for(int i = 0; i < lines.total(); i++ )
 	    {
 	    	Pointer line = cvGetSeqElem(lines, i);
 	    	detectedLines.add(new MyLine(new CvPoint(line).position(0), new CvPoint(line).position(1)));
 	    	MyLine myLine = new MyLine(new CvPoint(line).position(0), new CvPoint(line).position(1));
 	    	System.out.println("x1:"+myLine.point1.x()+"y1:"+myLine.point1.y()+"__x2:"+myLine.point2.x()+"y2:"+myLine.point2.y());
 //	    	cvLine(im, new CvPoint(line).position(0), 
 //	        		new CvPoint(line).position(1), CvScalar.BLUE, 2, CV_AA, 0);
 	    }
 
	    storage2.release();
 	 return detectedLines;
 	}
 	
 	
 	
 	/**
 	 * 
 	 * @param im
 	 * @return
 	 */
 	public synchronized IplImage getCanny(IplImage im, int width, int height)
 	{
 		
     
 //	    cvSmooth(im, im, CV_BLUR, 3);//Smooth image (test)
 
 		IplImage grayImgLines= IplImage.create(width, height, IPL_DEPTH_8U, 1);
 
 		cvCvtColor(im, grayImgLines, CV_BGR2GRAY);
 
 	    IplImage imCanny = IplImage.create(width, height, IPL_DEPTH_8U, 1);
 //	    IplImage grayImgCanny = IplImage.create(320, 240, IPL_DEPTH_8U, 1);
 	
 	    cvCanny(grayImgLines, imCanny, 50, 200, 3);
 //	    cvCvtColor(imCanny, grayImgCanny, CV_BGR2GRAY);
 	    return grayImgLines;
 	}
 	
 	
 	/**
 	 * 
 	 * @param img
 	 * @param detectedlines
 	 * @return
 	 */
 	public synchronized IplImage drawLines(IplImage img, Vector<MyLine> detectedlines){
 		 
 		for(int i = 0; i < detectedlines.size(); i++ )
 		    {
 		    	cvLine(img, detectedlines.get(i).point1, 
 		        		detectedlines.get(i).point2, CvScalar.RED, 1, CV_AA, 0);
 		    }
 		return img;
 	}
 }
