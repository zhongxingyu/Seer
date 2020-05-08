 package nl.tue.fingerpaint.client.model.drawingtool;
 
 import nl.tue.fingerpaint.shared.utils.Colour;
 
 import com.google.gwt.canvas.dom.client.CanvasPixelArray;
 import com.google.gwt.canvas.dom.client.ImageData;
 
 /**
  * A {@code CircleDrawingTool} is a {@link DrawingTool} with a circular shape.
  * Users can thus use this drawing tool to draw circles on the canvas.
  * 
  * @author Group Fingerpaint
  */
 public class CircleDrawingTool extends DrawingTool {
 
 	// --Constructors-------------------------------------------
 	/**
 	 * Constructs a circle drawing tool with radius r
 	 * 
 	 * @param r
 	 *            The radius of the drawing tool
 	 */
 	public CircleDrawingTool(int r) {
 		super(r);
 	}
 
 	// --Public methods for general use--------------------------
 	/**
 	 * Returns an ImageData object representing the circle drawing tool.
 	 * 
 	 * @param img
 	 *            The ImageData object to draw the drawing tool on
 	 * @param colour
 	 *            The colour to create the drawing tool with
 	 */
 	@Override
 	public ImageData getTool(ImageData img, Colour colour) {
 		int width = img.getWidth();
		int radius = (width - 1) / 2;
 		int x = radius;
 		int y = radius;
 		CanvasPixelArray data = img.getData();
 		int col = colour.getRed();
 
 		int i = 0, j = radius;
 		int index;
 		
 		while (i <= j) {
 			for (int w = x - j; w <= x + j; w++) {
 				index = ((y + i) * width + w) * 4;
 				fillPixel(data, index, col, 255);
 			}
 			
 			for (int w = x - j; w <= x + j; w++) {
 				index = ((y - i) * width + w) * 4;
 				fillPixel(data, index, col, 255);
 			}
 			
 			for (int w = x - i; w <= x + i; w++) {
 				index = ((y + j) * width + w) * 4;
 				fillPixel(data, index, col, 255);
 			}
 			
 			for (int w = x - i; w <= x + i; w++) {
 				index = ((y - j) * width + w) * 4;
 				fillPixel(data, index, col, 255);
 			}
 
 			i++;
 			j = (int) (Math.sqrt(radius * radius - i * i) + 0.5);
 		}
 
 		return img;
 	}
 
 }
