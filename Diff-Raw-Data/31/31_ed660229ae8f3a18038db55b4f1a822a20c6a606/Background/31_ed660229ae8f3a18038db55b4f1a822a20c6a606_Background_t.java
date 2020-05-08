 package edu.rit.se.sse.rapdevx.gui;
 
 import java.awt.Color;
 import java.awt.Graphics2D;
 import java.awt.Rectangle;
 import java.awt.geom.Rectangle2D;
 import java.awt.image.BufferedImage;
 
 import java.io.File;
 import java.io.IOException;
 
 import javax.imageio.ImageIO;
 
 public class Background {
 
 	private static int GRID_SIZE = 64;
 
 	private int x, y, width, height;
 	private BufferedImage background;
 
 	public Background(int x, int y, int width, int height) {
 		this.x = x;
 		this.y = y;
 		this.width = width;
 		this.height = height;
 
 		try {
 			background = ImageIO.read(new File("assets/background.png"));
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public void update() {
 	}
 
 	public void draw(Graphics2D gPen, Rectangle2D bounds) {
 		int xOffset = (int) bounds.getX();
 		int yOffset = (int) bounds.getY();
 
 		drawImage(gPen, xOffset, yOffset);
 		drawGrid(gPen, xOffset, yOffset);
 	}
 
 	public void drawImage(Graphics2D gPen, int xOffset, int yOffset) {
		
		// Steve figured it out with Kristen :D AT NIGHT!!! :D
		// Now we can enjoy Steve's magnificent background :D
 
		int x1 = xOffset;
 		int y1 = yOffset;
 		
		int backgroundWidth = background.getWidth();
		int backgroundHeight = background.getHeight();
		
		if ( xOffset > 0 ) {
			x1 =  xOffset - ( backgroundWidth * (int)( xOffset / backgroundWidth + 1 ) );
		}
		if ( yOffset > 0 ) {
			y1 = yOffset - ( backgroundHeight * (int)( yOffset / backgroundHeight +1 ) );
		}
		
		int adjustedY1 = y1;
		
 		while (x1 < width) {
 			while (y1 < height) {
 				gPen.drawImage(background, x1, y1,
						backgroundWidth,
						backgroundHeight, null);
				y1 += backgroundHeight;
 			}
			x1 += backgroundWidth;
			y1 = adjustedY1;
 		}
 	}
 
 	/**
 	 * Draws the grid
 	 * 
 	 * @param gPen
 	 *              the graphics2D Pen
 	 */
 	public void drawGrid(Graphics2D gPen, int xOffset, int yOffset) {
 		int x1 = 32 + xOffset;
 		int y1 = 32 + yOffset;
 		if (xOffset > 0) {
 			x1 = 32 - xOffset;
 		}
 		if (yOffset > 0) {
 			y1 = 32 - yOffset;
 		}
 
 		gPen.setColor(getColor(x1));
 		while (x1 < width) {
 			gPen.fill(new Rectangle(x1, 0, 4, height));
 			x1 += GRID_SIZE;
 			gPen.setColor(getColor(x1));
 		}
 
 		gPen.setColor(getColor(y1));
 		while (y1 < height) {
 			gPen.fill(new Rectangle(0, y1, width, 4));
 			y1 += GRID_SIZE;
 			gPen.setColor(getColor(y1));
 		}
 	}
 
 	/**
 	 * Swaps the color from gray to other gray
 	 * 
 	 * @param gPen
 	 *              the graphics2D Pen
 	 */
 	public void swapColor(Graphics2D gPen) {
 		Color color1 = new Color(18, 18, 19);
 		Color color2 = new Color(26, 24, 29);
 		if (gPen.getColor().equals(color1)) {
 			gPen.setColor(color2);
 		} else {
 			gPen.setColor(color1);
 		}
 	}
 
 	/**
 	 * Swaps the color from gray to other gray
 	 * 
 	 * @param gPen
 	 *              the graphics2D Pen
 	 */
 	public Color getColor(int coordinate) {
 		Color color1 = new Color(18, 18, 19);
 		Color color2 = new Color(26, 24, 29);
 
 		if ((coordinate / GRID_SIZE) % 2 == 0) {
 			return color1;
 		} else {
 			return color2;
 		}
 	}
 }
