 package org.on.puz.photobombsquad;
 
import org.opencv.core.Mat;
 
public final class MatAverager {
	public static native int average(Mat out,Mat[] mats);
 }
