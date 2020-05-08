 package jbookreader.rendering.swing;
 
 import java.awt.Graphics2D;
import java.awt.font.LineMetrics;
 import java.awt.geom.Rectangle2D;
 
 import jbookreader.rendering.IDrawable;
 import jbookreader.rendering.Position;
 
 final class SimpleSwingString implements IDrawable {
 
 	private final JGraphicDriver driver;
 	private final String string;
 	private final AWTFontAdapter font;
 	private int height;
 	private int depth;
 	private int width;
 
 	SimpleSwingString(JGraphicDriver driver, String string, AWTFontAdapter font) {
 		this.driver = driver;
 		this.string = string;
 		this.font = font;
 
		LineMetrics metrics = font.getFont().getLineMetrics(string, driver.getFontRC());
 		Rectangle2D bounds = font.getFont().getStringBounds(string, driver.getFontRC());
 //		System.out.println(bounds);
		height = JGraphicDriver.pixelToDimension(metrics.getAscent());
		depth = JGraphicDriver.pixelToDimension(metrics.getDescent());
		// XXX: There are some problems if we use bounds for height/depth:
		//     if the first char is some type of long dash \u2013, then we get incorrect
		//     measurements (the base line is shifted)
//		height = JGraphicDriver.pixelToDimension((float) bounds.getHeight());
//		depth = JGraphicDriver.pixelToDimension((float) (bounds.getHeight() + bounds.getY()));
 		width = JGraphicDriver.pixelToDimension((float) bounds.getWidth());
 	}
 
 	public void draw(Position position) {
 		Graphics2D graphics = driver.getPaperGraphics();
 		if (!graphics.getFont().equals(font.getFont())) {
 			graphics.setFont(font.getFont());
 		}
//		System.out.println(width + " x " + height + " + " + depth);
 		graphics.drawString(string,
 				(int) JGraphicDriver.dimensionToPixel(this.driver.horizontalPosition),
 				(int) JGraphicDriver.dimensionToPixel(this.driver.verticalPosition + height));
 
 		this.driver.horizontalPosition += width;
 	}
 
 	public int getDepth() {
 		return depth;
 	}
 
 	public int getHeight() {
 		return height;
 	}
 
 	public int getWidth(Position position) {
 		return width;
 	}
 
 }
