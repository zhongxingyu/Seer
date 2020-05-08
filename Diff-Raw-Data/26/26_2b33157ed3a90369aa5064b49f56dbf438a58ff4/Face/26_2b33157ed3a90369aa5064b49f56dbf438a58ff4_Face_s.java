 package nl.cs.uu.faceselector;
 
 import org.eclipse.swt.graphics.Rectangle;
 
 public class Face {
 
 	public Double rotation = null;
 	public Rectangle box = null;
 	public double width, height;
 
	public double computeError(Face other, int frameSize) {
 		if (other == null || other.box == null) {
 			return 1.0;
 		}
 		double error = 0.0;
		int diff = Math.abs(box.x - other.box.x)
				+ Math.abs(box.y - other.box.y);
		error += diff / (double) frameSize;
		diff = Math.abs(box.width - other.box.width)
				+ Math.abs(box.height - other.box.height);
		error += diff / (double) frameSize;
 		return error;
 	}
 
 }
