 package de.thatsich.bachelor.opencv.error;
 
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
		final int length = (int) Math.round(Math.random() * this.STDDEV_LENTH + this.MEAN_LENGTH);
		final double radian = (Math.random() * Math.PI) + Math.PI / 2;
		
		final int offset_x = (int) Math.round(Math.cos(radian) * length);
		final int offset_y = (int) Math.round(Math.sin(radian) * length);
 		
 		final int width = in.width();
 		final int height = in.height();
 		
 		final int leftWidth = width - offset_x;
 		final int leftHeight = height - offset_y;
 		
		final int x1 = (int) Math.round(Math.random() * leftWidth);
		final int y1 = (int) Math.round(Math.random() * leftHeight);
 		final int x2 = x1 + offset_x;
 		final int y2 = y1 + offset_y;
 		
		final Scalar color = new Scalar((int) Math.round(Math.random() * 255));
		final int thickness = (int) Math.round(Math.random() * (this.MAX_LINE_THICKNESS - MIN_LINE_THICKNESS) + MIN_LINE_THICKNESS);
 		
 		Core.line(in, new Point(x1, y1), new Point(x2, y2), color, thickness);
 		// circle
 		// ellipse
 		// rectangle
 		// polylines
 		return in;
 	}
 
 }
