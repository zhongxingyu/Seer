 package jbookreader.rendering.swing;
 
 import java.awt.Graphics2D;
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
 
 		Rectangle2D bounds = font.getFont().getStringBounds(string, driver.getFontRC());
 //		System.out.println(bounds);
		height = JGraphicDriver.pixelToDimension((float) bounds.getHeight());
		depth = JGraphicDriver.pixelToDimension((float) (bounds.getHeight() + bounds.getY()));
 		width = JGraphicDriver.pixelToDimension((float) bounds.getWidth());
 	}
 
 	public void draw(Position position) {
 		Graphics2D graphics = driver.getPaperGraphics();
 		if (!graphics.getFont().equals(font.getFont())) {
 			graphics.setFont(font.getFont());
 		}
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
