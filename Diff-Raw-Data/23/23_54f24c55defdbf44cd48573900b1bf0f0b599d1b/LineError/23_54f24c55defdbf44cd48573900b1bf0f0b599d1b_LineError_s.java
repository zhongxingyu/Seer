 package de.thatsich.bachelor.opencv.error;
 
import java.util.Random;

 import org.opencv.core.Core;
 import org.opencv.core.Mat;
 import org.opencv.core.Point;
 import org.opencv.core.Scalar;
 
 import de.thatsich.core.opencv.error.AErrorGenerator;
 import de.thatsich.core.opencv.error.IErrorGenerator;
 
 public class LineError extends AErrorGenerator implements IErrorGenerator {
 
 	private final int MEAN_LENGTH = 100;
 	private final int STDDEV_LENTH = 50;
 	private final int MIN_LINE_THICKNESS = 1;
 	private final int MAX_LINE_THICKNESS = 5;
 	
 	@Override
 	public Mat generateError(Mat in) {
		final Random r = new Random();
		final int length = (int) Math.round(r.nextGaussian() * this.STDDEV_LENTH + this.MEAN_LENGTH);
		final int angle = (int) Math.round(r.nextInt() * 360) - 180;
		final int offset_x = length * (int) Math.round(Math.cos(angle));
		final int offset_y = length * (int) Math.round(Math.sin(angle));
 		
 		final int width = in.width();
 		final int height = in.height();
 		
 		final int leftWidth = width - offset_x;
 		final int leftHeight = height - offset_y;
 		
		final int x1 = r.nextInt() * leftWidth;
		final int y1 = r.nextInt() * leftHeight;
 		final int x2 = x1 + offset_x;
 		final int y2 = y1 + offset_y;
 		
		final Scalar color = new Scalar(r.nextInt() * 255);
		final int thickness = r.nextInt() * (this.MAX_LINE_THICKNESS - MIN_LINE_THICKNESS) + MIN_LINE_THICKNESS;
 		
 		Core.line(in, new Point(x1, y1), new Point(x2, y2), color, thickness);
 		// circle
 		// ellipse
 		// rectangle
 		// polylines
 		return in;
 	}
 
 }
