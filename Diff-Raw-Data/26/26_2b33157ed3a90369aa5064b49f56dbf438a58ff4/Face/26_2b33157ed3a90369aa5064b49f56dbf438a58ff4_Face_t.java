 package nl.cs.uu.faceselector;
 
 import org.eclipse.swt.graphics.Rectangle;
 
 public class Face {
 
 	public Double rotation = null;
 	public Rectangle box = null;
 	public double width, height;
 
	public double computeError(Face other, double frameSize) {
 		if (other == null || other.box == null) {
 			return 1.0;
 		}
 		double error = 0.0;
		double diff = Math.sqrt(Math.pow(box.x - other.box.x, 2)
				+ Math.pow(box.y - other.box.y, 2));
		error += diff / frameSize;
		diff = Math.sqrt(Math.pow(box.width - other.box.width, 2)
				+ Math.pow(box.height - other.box.height, 2));
		error += diff / frameSize;
 		return error;
 	}
 
 }
