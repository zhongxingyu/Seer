 package vitro;
 
 /*
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.RenderingHints;
 */
 import java.awt.*;
 import java.awt.geom.*;
 import java.awt.Font;
 import java.awt.font.*;
 
 /**
 * A collection of useful utility routines for
 * drawing 2D graphics.
 *
 * @author John Earnest
 * @author Jason Hiebel
 **/
 public class Drawing {
 	
 	/**
 	* Get the width of a String in pixels given
 	* the current drawing context.
 	*
 	* @param g the target graphics context.
 	* @param s the String to consider.
 	* @return the width of the String in pixels.
 	**/
 	public static int stringWidth(Graphics g, String s) {
 		Font font = g.getFont();
 		return (int)font.getStringBounds(s, g.getFontMetrics().getFontRenderContext()).getWidth();
 	}
 
 	/**
 	* Get the height of a String in pixels given
 	* the current drawing context.
 	*
 	* @param g the target graphics context.
 	* @param s the String to consider.
 	* @return the height of the String in pixels.
 	**/
 	public static int stringHeight(Graphics g, String s) {
 
 		//Font font = g.getFont();
 		//TextLayout layout = new TextLayout(s, font, g.getFontMetrics().getFontRenderContext());
 		//Rectangle2D bounds = layout.getBounds();
 		//this.base = Math.round(layout.getDescent()) + 1;
 
 		Font font = g.getFont();
 		return (int)font.getStringBounds(s, g.getFontMetrics().getFontRenderContext()).getHeight();
 	}
 
 	/**
 	* Draw a String centered vertically and horizontal
 	* at a given position onscreen.
 	*
 	* @param g the target graphics surface.
 	* @param s the String to draw.
 	* @param x the x coordinate of the centerpoint.
 	* @param y the y coordinate of the centerpoint.
 	**/
 	public static void drawStringCentered(Graphics g, String s, int x, int y) {
 		if(s.length() == 0) { return; }
 		Font font = g.getFont();
 		TextLayout layout = new TextLayout(s, font, g.getFontMetrics().getFontRenderContext());
 		Rectangle2D bounds = layout.getBounds();
 		g.drawString(
 			s,
 			x-(int)((bounds.getX() + bounds.getWidth())  / 2),
 			y+(int)(bounds.getHeight() / 2)
 		);
 	}
 
 	/**
 	* Draw a filled circle with a given fill and outline color.
 	*
 	* @param g the target graphics surface.
 	* @param x the x coordinate of the circle's centerpoint.
 	* @param y the y coordinate of the circle's centerpoint.
 	* @param radius the circle's radius, in pixels.
 	* @param outline the color of the circle's outline.
 	* @param fill the color with which to fill the circle.
 	**/
 	public static void drawCircleCentered(Graphics g, int x, int y, int radius, Color outline, Color fill) {
 		g.setColor(fill);
 		g.fillOval(x-radius, y-radius, 2*radius, 2*radius);
 		g.setColor(outline);
 		g.drawOval(x-radius, y-radius, 2*radius, 2*radius);
 	}
 
 	/**
 	* Draw a filled rounded rectangle with a given fill and outline color.
 	*
 	* @param g the target graphics surface.
 	* @param x the x coordinate of the rectangle's top left corner.
 	* @param y the y coordinate of the rectangle's top left corner.
 	* @param width the width of the rectangle, in pixels.
 	* @param height the height of the rectangle, in pixels.
 	* @param radius the corner radius, in pixels.
 	* @param outline the color of the rectangle's outline.
 	* @param fill the color with which to fill the rectangle.
 	**/
 	public static void drawRoundRect(Graphics g, int x, int y, int width, int height, int radius, Color outline, Color fill) {
 		g.setColor(fill);
 		g.fillRoundRect(x, y, width, height, radius, radius);
 		g.setColor(outline);
 		g.drawRoundRect(x, y, width, height, radius, radius);
 	}
 	
 	/**
 	* Draw a '3D' bezeled rectangle.
 	*
 	* @param g the target graphics surface.
 	* @param bound a rectangle representing the size and position of the bezeled rectangle.
 	* @param thickness the thickness of the bezel, in pixels.
 	* @param ulBezel the color for the upper and left edges of the bezel.
 	* @param drBezel the color for the down and right edges of the bezel.
 	* @param main the primary fill for the rectangle.
 	**/
 	public static void drawBezelRect(Graphics g, Rectangle bound, int thickness, Color ulBezel, Color drBezel, Color main) {
 		g.setColor(main);
 		((Graphics2D)g).fill(bound);
 		g.setColor(ulBezel);
 		for(int t = 0; t < thickness; t++) {
 			g.drawLine(bound.x + t, bound.y    , bound.x                   + t, bound.y + bound.height - t);
 			g.drawLine(bound.x    , bound.y + t, bound.x + bound.width - 1 - t, bound.y                + t);
 		}
 		g.setColor(drBezel);
 		for(int t = 0; t < thickness; t++) {
 			g.drawLine(bound.x + bound.width - t, bound.y + bound.height    , bound.x + bound.width - t, bound.y                + t);
 			g.drawLine(bound.x + bound.width    , bound.y + bound.height - t, bound.x               + t, bound.y + bound.height - t);
 		}
 	}
 	
 	/**
 	* Draw a '3D' bezeled rectangle.
 	*
 	* @param g the target graphics surface.
 	* @param x the x coordinate of the rectangle's top left corner.
 	* @param y the y coordinate of the rectangle's top left corner.
 	* @param width the width of the rectangle, in pixels.
 	* @param height the height of the rectangle, in pixels.
 	* @param thickness the thickness of the bezel, in pixels.
 	* @param ulBezel the color for the upper and left edges of the bezel.
 	* @param drBezel the color for the down and right edges of the bezel.
	* @param main the primary fill for the rectangle.
 	**/
 	public static void drawBezelRect(Graphics g, int x, int y, int width, int height, int thickness, Color ulBezel, Color drBezel, Color fill) {
 		drawBezelRect(g, new Rectangle(x, y, width, height), thickness, ulBezel, drBezel, fill);
 	}
 
 	/**
 	* Configure a Graphics surface for drawing raster graphics-
 	* preference speed and pixel-wise interpolation.
 	*
 	* @param g the target graphics context.
 	**/
 	public static void configureRaster(Graphics g) {
 		if (g instanceof Graphics2D) {
 			Graphics2D g2 = (Graphics2D)g;
 			g2.setRenderingHint( RenderingHints.KEY_RENDERING,     RenderingHints.VALUE_RENDER_SPEED);
 			g2.setRenderingHint( RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
 		}
 	}
 
 	/**
 	* Configure a Graphics surface for drawing vector graphics-
 	* antialias and preference rendering quality.
 	*
 	* @param g the target graphics context.
 	**/
 	public static void configureVector(Graphics g) {
 		if (g instanceof Graphics2D) {
 			Graphics2D g2 = (Graphics2D)g;
 			g2.setRenderingHint( RenderingHints.KEY_ANTIALIASING,  RenderingHints.VALUE_ANTIALIAS_ON);
 		}
 	}
 }
