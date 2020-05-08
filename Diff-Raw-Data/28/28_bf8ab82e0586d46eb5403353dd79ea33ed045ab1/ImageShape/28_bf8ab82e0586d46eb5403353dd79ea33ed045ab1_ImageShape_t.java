 package riverObjects;
 
 import java.awt.Color;
 import java.awt.Polygon;
 
 /**
  *  Stores the polygon boundary and color of a 
  *  given object in a river image
  *  
  * @author mendesrodr
  *
  */
 public class ImageShape {
 	private Polygon polygon;
 	private Color color;
 	
 	public ImageShape(Polygon boundaries, Color c) {
 		polygon = boundaries;
 		color = c;
 	}
 	
 	public Polygon getPolygon() {
 		return polygon;
 	}
 	
 	public Color getColor() {
 		return color;
 	}
 	
 	/**
 	 * Gets the total perimeter for this
 	 * image shape.
 	 * 
 	 * @return perimeter
 	 */
 	public float getPerimeter() {
         int[] xpoints = polygon.xpoints;
         int[] ypoints = polygon.ypoints;
        int npoints = polygon.npoints;
         float totalLength = 0f;
         
         // Go through all the points in the image shape
        for (int i = 0; i < npoints - 1; i++) {
             // Add the length between the points
             totalLength += Math.sqrt( Math.pow( Math.abs(xpoints[i] - xpoints[i + 1]), 2 ) + 
                     Math.pow( Math.abs(ypoints[i] - ypoints[i + 1]), 2 ) );
         }
         
         // Now add the line between the first and the last points
        totalLength += Math.sqrt( Math.pow( Math.abs(xpoints[0] - xpoints[npoints - 1]), 2 ) + 
                Math.pow( Math.abs(ypoints[0] - ypoints[npoints - 1]), 2 ) );
         
         // Return perimeter
         return totalLength;
 	}
 }
