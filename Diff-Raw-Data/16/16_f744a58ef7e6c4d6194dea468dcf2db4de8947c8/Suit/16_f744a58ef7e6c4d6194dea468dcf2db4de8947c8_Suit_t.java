 /**
 * Enumerated Suit specifying standard 
  * card suits (Club, Diamond, Heart, Spade)
  */
 package com.swm.blackjack;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.net.URL;
 
 import javax.swing.ImageIcon;
 
 /**
  * @author smartin
  */
 enum Suit implements Comparable<Suit>
 {
 	CLUB {
 		void drawSymbol(Graphics graphics, int x, int y)
 		{
 			if (clubIcon != null) {
 				clubIcon.paintIcon(null,graphics,x+12, y+4);
 			}
 			else {
 				int x1[] = {x+18, x+13, x+23};
 				int y1[] = {y+18, y+25, y+25};
 			
 				graphics.setColor(Color.BLACK);
 				graphics.fillOval( x+14, y+5, 9, 9);
 				graphics.fillOval( x+11, y+11, 9, 9);
 				graphics.fillOval( x+17, y+11, 9, 9);
 				graphics.fillPolygon( x1, y1, 3);
 			}
 		}
 	},
 	DIAMOND {
 		void drawSymbol(Graphics graphics, int x, int y)
 		{
 			if (diamondIcon != null) {
 				diamondIcon.paintIcon(null,graphics,x+12, y+4);
 			}
 			else {
 				int x1[] = {x+18, x+11, x+18, x+25};
 				int y1[] = {y+3, y+13, y+23, y+13};
 			
 				graphics.setColor(Color.RED);
 				graphics.fillPolygon( x1, y1, 4);
 			}
 		}
 	},
 	HEART {
 		void drawSymbol(Graphics graphics, int x, int y)
 		{
 			if (heartIcon != null) {
 				heartIcon.paintIcon(null,graphics,x+12, y+4);
 			}
 			else {
 				int x1[] = {x+11, x+18, x+25};
 				int y1[] = {y+13, y+23, y+13};
 			
 				graphics.setColor(Color.RED);
 				graphics.fillOval( x+11, y+6, 9, 9);
 				graphics.fillOval( x+17, y+6, 9, 9);
 				graphics.fillPolygon( x1, y1, 3);
 			}
 		}
 	},
 	SPADE {
 		void drawSymbol(Graphics graphics, int x, int y)
 		{
 			if (spadeIcon != null) {
 				spadeIcon.paintIcon(null,graphics,x+12, y+4);
 			}
 			else {
 				int x1[] = {x+18, x+11, x+25};
 				int y1[] = {y+3, y+13, y+13};
 				int x2[] = {x+18, x+11, x+25};
 				int y2[] = {y+16, y+26, y+26};
 				
 				graphics.setColor(Color.BLACK);
 				graphics.fillOval( x+11, y+11, 9, 9);
 				graphics.fillOval( x+17, y+11, 9, 9);
 				graphics.fillPolygon( x1, y1, 3);
 				graphics.fillPolygon( x2, y2, 3);
 			}
 		}
 	};
 	
 	private static ImageIcon clubIcon = null;
 	private static ImageIcon diamondIcon = null;
 	private static ImageIcon heartIcon = null;
 	private static ImageIcon spadeIcon = null;
 	
 	/**
 	 * Load our gif images for use in drawing each suit.
 	 */
 	static {
 		URL imgURL = BlackJack.class.getResource("Club.gif");
 		if (imgURL != null) {
 			clubIcon = new ImageIcon(imgURL);
 		}
 		imgURL = BlackJack.class.getResource("Diamond.gif");
 		if (imgURL != null) {
 			diamondIcon = new ImageIcon(imgURL);
 		}
 		imgURL = BlackJack.class.getResource("Heart.gif");
 		if (imgURL != null) {
 			heartIcon = new ImageIcon(imgURL);
 		}
 		imgURL = BlackJack.class.getResource("Spade.gif");
 		if (imgURL != null) {
 			spadeIcon = new ImageIcon(imgURL);
 		}
 	}
 	
 	/**
 	 * Compute the Suit from the ordinal (0-3)
 	 * @param ordinal the Suit ordinal
 	 * @return the Suit corresponding to the given ordinal
 	 */
 	public static Suit aSuit(int ordinal)
 	{
 		return Suit.values()[ordinal];
 	}
 	
 	/**
 	 * Draw the Suit Symbol into the graphics element Each suit must override
 	 * this with their own method to draw their symbol
 	 * @param graphics the Graphics containing object to draw into
 	 * @param xoffset the horizontal offset to draw at
 	 * @param yoffset the vertical offset to draw at
 	 */
 	abstract void drawSymbol(Graphics graphics, int xoffset, int yoffset);
 }
