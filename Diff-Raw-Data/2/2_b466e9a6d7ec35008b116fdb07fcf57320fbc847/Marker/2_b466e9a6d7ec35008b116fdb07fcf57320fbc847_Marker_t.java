 package uibk.autonom.ps.navigation;
 
 import java.util.NavigableMap;
 
 import org.opencv.core.Mat;
 import org.opencv.core.MatOfPoint;
 import org.opencv.core.Point;
 import org.opencv.core.Scalar;
 
 import android.annotation.TargetApi;
 import android.os.Build;
 
 import uibk.autonom.ps.colordetector.ColorDetector;
 
 @TargetApi(Build.VERSION_CODES.GINGERBREAD)
 public class Marker {
 	private Scalar color;
 	/**
 	 * Point corresponding to virtual system
 	 * 
 	 * 			100
 	 * -------------------------------------|
 	 * |1               2                  3|
 	 * |                                    | 100
 	 * |6               5                  4|
 	 * -------------------------------------
 	 */
 	private Point position;
 	
 	public Point curImgPosition;
	public double curImgSize = 0;
 	
 	public Marker(Scalar color, Point position){
 		this.color = color;
 		this.position = position;
 	}
 	
 	public Scalar getColor(){
 		return color;
 	}
 
 	public Point getPosition() {
 		return position;
 	}
 		
 	public void calculateImgPosition(Mat curImgFrame){
 		ColorDetector colorDetector = new ColorDetector();
 		colorDetector.setHsvColor(getColor());
 		colorDetector.detect(curImgFrame);
 		
 		NavigableMap<Double, MatOfPoint> result = colorDetector.getMaxContourSizes(1);
 
 		if(result.size() > 1){
 			java.util.Map.Entry<Double, MatOfPoint> entry = result.pollFirstEntry();
 			curImgSize = entry.getKey();
 			curImgPosition = colorDetector.getCenterPoint(entry.getValue());
 		}
 	}
 	
 }
