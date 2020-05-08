 package modules;
 
 import java.awt.Color;
 import java.awt.Graphics2D;
 import java.awt.Polygon;
 import java.awt.image.BufferedImage;
 import java.util.List;
 
 import riverObjects.ImageShape;
 
 /**
  * Module that performs boundary rendering of
  * shapes within an image.
  */
 public class BoundaryRenderer implements Module {
 	private BufferedImage img;
 	private List<ImageShape> shapes;
 	private Color outlineColor;
 	
 	public BoundaryRenderer(BufferedImage img) {
 		this.img = img;
 	}
 	
 	/**
 	 * Sets the color for outline rendering
 	 * @param c
 	 */
 	public void setOutlineColor(Color c) {
 		outlineColor = c;
 	}
 	
 	/**
 	 * Set the image shapes to be outlined
 	 * on the picture
 	 * @param shapes
 	 */
 	public void setImageShapes(List<ImageShape> shapes) {
 		this.shapes = shapes;
 	}
 
 	@Override
 	/**
 	 * Returns the original image with the
 	 * user-defined boundaries drawn on.
 	 */
 	public BufferedImage getImage() {
 		// Return null if user hasn't specified outlined color/shapes
 		if (outlineColor == null || shapes == null)
 			return null;
 		
 		// Retrieve the image's Graphics2D
 		Graphics2D g2 = img.createGraphics();  
 		// Set Graphics2D color
 		g2.setColor(outlineColor);
 		// Iterate through all image shapes
 		for (ImageShape shape : shapes){
 			// For each shape, retrieve polygon (one of its fields)
 			Polygon p = shape.getPolygon();
 			// Get all the points of polygon (they will be split into an xPoints and a yPoints array)
 			 int[] xPoints = p.xpoints;
 			 int[] yPoints = p.ypoints;
			 int npoints = p.npoints;
 			// Iterate through the points
			 for(int i = 0; i < npoints-1; i++){
 				// for each point, look at the next one
 				// Draw a line from x1, y1 to x2, y2
 				 g2.drawLine(xPoints[i], yPoints[i], xPoints[i+1], yPoints[i+1]);
 			 }
 			// Once you exit iteration, draw the line between the first and the last points
			g2.drawLine(xPoints[npoints-1], yPoints[npoints-1], xPoints[0], yPoints[0]); 
 		}
 		// Dispose of Graphics2D object
 		g2.dispose();
 		// Return the buffered image
 		return img;		
 	}
 
 }
