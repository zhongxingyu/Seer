 /*
  * Copyright (C) 2012 JPII and contributors
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.jpii.navalbattle.pavo;
 
 import java.awt.AlphaComposite;
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Point;
 import java.awt.RadialGradientPaint;
 import java.awt.RenderingHints;
 import java.awt.MultipleGradientPaint.CycleMethod;
 import java.awt.geom.Point2D;
 import java.awt.image.BufferedImage;
 
 import maximusvladimir.dagen.Rand;
 
 import com.jpii.navalbattle.pavo.grid.Entity;
 import com.jpii.navalbattle.pavo.grid.Location;
 import com.jpii.navalbattle.pavo.io.PavoImage;
 
 public class PavoHelper {
 	private static SystemSpeed calcs;
 	/**
 	 * Creates a shadow for a buffer,
 	 * to give a nice 3D effect.
 	 * @param width The width to make it.
 	 * @param height The height to make it.
 	 * @return A image with the shadow.
 	 */
 	public static PavoImage createInnerShadow(int width, int height) {
 		PavoImage b = new PavoImage(width,height,BufferedImage.TYPE_INT_ARGB);
 		Graphics2D g2 = (Graphics2D)b.getGraphics();
 		Point2D center = new Point2D.Float(width/2, height/2);
         float radius = width;
         Point2D focus = new Point2D.Float(width/2, height/2);
         float[] dist = {0.0f,0.3f, 1.0f};
         Color[] colors = {new Color(0,0,0,0),new Color(0,0,0,0), new Color(0,0,0,255)};
         RadialGradientPaint p = new RadialGradientPaint(center, radius, focus, dist, colors, CycleMethod.NO_CYCLE);
         g2.setPaint(p);
         g2.fillRect(0, 0,width, height);
         return b;
 	}
 	/**
 	 * Sleeps the current thread to give the
 	 * CPU time to work on other threads.
 	 */
 	public static void threadSleep() {
 		try {
 			Thread.sleep(0);
 		}
 		catch (Throwable t) {
 			
 		}
 	}
 	
 	/**
 	 * Changes the alpha of a given color.
 	 * @param c The color to modify.
 	 * @param alpha The alpha to set it as.
 	 * @return The changed color. Could be null.
 	 */
 	public static Color changeAlpha(Color c, int alpha) {
 		if (c == null)
 			return null;
 		return new Color(c.getRed(),c.getGreen(),c.getBlue(),alpha);
 	}
 	
 	/**
 	 * Gets a short representation of a particular
 	 * color.
 	 * @param c The color to make it from.
 	 * @return
 	 */
 	public static short getByteFromColor(Color c) {
 		int rgb = c.getRGB();
 		rgb = (int)(rgb / 4);
 		if (rgb > 32767)
 			rgb = rgb - (32767*2);
 		return (short)rgb;
 	}
 	
 	//public static Point convertLocation
 	
 	private static Rand rand3 = new Rand();
 	
 	/**
 	 * Gets a color representation of a particular
 	 * short.
 	 * @param c The short to make it from.
 	 * @return
 	 */
 	public static Color getColorFromByte(short c) {
 		int hd = (int)c;
 		if (hd < 0)
 			hd = hd + (32767*2);
 		hd = (int)(hd * 4);
 		Color c2 = new Color(hd);
 		int r  = c2.getRed();
 		if (r >= 1)
 			r = r *(rand3.nextInt(4,18));
 		else
 			r = 255 - (rand3.nextInt(8, 32));
 		if (r > 255)
 			r =255;
 		if (r < 0)
 			r =0;
 		return new Color(r,c2.getGreen(),c2.getBlue());
 	}
 	
 	/**
 	 * Attempts to calculate a speed for the current
 	 * operating system for statistical and performance
 	 * reasons.
 	 * @return The SystemSpeed that is most closely matched
 	 * to the current operating system.
 	 */
 	public static SystemSpeed getCalculatedSystemSpeed() {
 		if (calcs == null) {
 			long a = System.nanoTime();
 			a = System.nanoTime();
 			a = System.nanoTime();
 			a = System.nanoTime();
 			a = System.nanoTime();
 			a = System.nanoTime() - a;
 			if (a < 2000000)
 				calcs = SystemSpeed.VERYFAST;
 			else if (a < 2500000)
 				calcs = SystemSpeed.CHEETAH;
 			else if (a < 3100000)
 				calcs = SystemSpeed.HARE;
 			else if (a < 3500000)
 				calcs = SystemSpeed.CREEPER;
 			else
 				calcs = SystemSpeed.TURTLE;
 			if (calcs == SystemSpeed.TURTLE) {
 				a = System.nanoTime();
 				a = System.nanoTime();
 				a = System.nanoTime();
 				a = System.nanoTime();
 				a = System.nanoTime() - a;
 				System.out.println("Speed time:"+a);
 				if (a < 2000000)
 					calcs = SystemSpeed.VERYFAST;
 				else if (a < 2500000)
 					calcs = SystemSpeed.CHEETAH;
 				else if (a < 3100000)
 					calcs = SystemSpeed.HARE;
 				else if (a < 3500000)
 					calcs = SystemSpeed.CREEPER;
 				else
 					calcs = SystemSpeed.TURTLE;
 			}
 		}
 		return calcs;
 	}
 	
 	/**
 	 * Determines if a chunk is visible in a world,
 	 * based on the viewing conditions.
 	 * @param w The world to use.
 	 * @param c The chunk to detect.
 	 * @return
 	 */
 	public static boolean isChunkVisibleOnScreen(World w, Chunk c) {
 		if (w == null || c == null)
 			return false;
		else
			return true;
 		/*
 		 * DO NOT DELETE THIS CODE. THIS IS A TEMPORARLY FIX, AND MAX WILL BE MAD IF
 		 * THE CODE DISAPPEARS!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
 		 * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
 		 * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
 		 * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
 		 * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
 		 * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
 		 * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
 		 * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
 		 * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
 		 * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
 		 * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
 		 * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
 		 * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
 		 * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
 		 * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
		int sx = Math.abs(w.getScreenX());
		int sy = Math.abs(w.getScreenY());
 		int px = c.getX() * 100;
 		int py = c.getZ() * 100;
 		if (px-sx+100 >= 0 && py-sy+100 >= 0 && px-sx <= Game.Settings.currentWidth && py-sy <= Game.Settings.currentHeight){
 			return true;
 		}
 		else if (sx > 0 && px == 1 && py-sy+100 >= 0 && py-sy <= Game.Settings.currentHeight)
 			return true;
 		else if (sy > 0 && py == 1 && px-sx+100 >= 0 && px-sx <= Game.Settings.currentWidth)
 			return true;
 		else
 			return false;
		*/
 	}
 	
 	public static Point convertLocationToScreen(World w, Location l) {
 		if (w == null || l == null)
 			return null;
 		int sx = Math.abs(w.getScreenX());
 		int sy = Math.abs(w.getScreenY());
 		int px = l.getCol() * 50;
 		int py = l.getRow() * 50;
 		return new Point(px-sx,py-sy);
 	}
 	
 	/**
 	 * Converts a location in the grid to a
 	 * chunk location.
 	 * @param w The world to use.
 	 * @param l The location to convert.
 	 * @return The converted location to a chunk
 	 * could be null.
 	 */
 	public static Chunk convertGridLocationToChunk(World w, Location l) {
 		if (l == null || w == null)
 			return null;
 		int x = l.getCol()/2;
 		int y = l.getRow()/2;
 		if (x < 0 || y < 0 || x > PavoHelper.getGameWidth(w.getWorldSize()) || y > PavoHelper.getGameHeight(w.getWorldSize()))
 			return null;
 		return w.getChunk(x, y);
 	}
 	
 	/**
 	 * Gets the width of the world.
 	 * @param ws The world size to use.
 	 * @return
 	 */
 	public static int getGameWidth(WorldSize ws) {
 		if (ws == WorldSize.WORLD_HUGE)
 			return 86;
 		else if (ws == WorldSize.WORLD_LARGE)
 			return 64;
 		else if (ws == WorldSize.WORLD_MEDIUM)
 			return 32;
 		else if (ws == WorldSize.WORLD_SMALL)
 			return 16;
 		else if (ws == WorldSize.WORLD_OF_NORTH_KOREA)
 			return 12;
 		else 
 			return 14;
 	}
 	
 	/** Gets the height of the world.
 	 * @param ws The world size to use.
 	 * @return
 	 */
 	public static int getGameHeight(WorldSize ws) {
 		if (ws == WorldSize.WORLD_HUGE)
 			return 86;
 		else if (ws == WorldSize.WORLD_LARGE)
 			return 64;
 		else if (ws == WorldSize.WORLD_MEDIUM)
 			return 32;
 		else if (ws == WorldSize.WORLD_SMALL)
 			return 16;
 		else if (ws == WorldSize.WORLD_OF_NORTH_KOREA)
 			return 12;
 		else 
 			return 14;
 	}
 	
 	/**
 	 * Converts a world space coordinate to
 	 * world coordinate.
 	 * @param w The world to use.
 	 * @param wp The world space to convert.
 	 * @deprecated
 	 * @return
 	 */
 	public static Point convertWorldToWorldSpace(World w, Point wp) {
 		return wp;
 	}
 	
 	/**
 	 * Converts a location in the world into a 
 	 * coordinate on the screen.
 	 * @param w The world to use.
 	 * @param wsp The point to convert.
 	 * @return
 	 */
 	public static Point convertWorldSpaceToScreen(World w, Point wsp) {
 		return new Point(wsp.x - w.getScreenX(), wsp.y - w.getScreenY());
 	}
 	
 	/**
 	 * Converts a location on the screen into a
 	 * coordinate in the world.
 	 * @param w The world to use.
 	 * @param wsp The screen coordinate to convert.
 	 * @return
 	 */
 	public static Point convertScreenToWorldSpace(World w, Point wsp) {
 		return new Point(wsp.x + w.getScreenX(), wsp.y + w.getScreenY());
 	}
 	
 	/**
 	 * Converts a world space coordinate into a
 	 * grid location.
 	 * @param p The point to convert.
 	 * @return
 	 */
 	public static Location convertWorldSpaceToGridLocation(Point p) {
 		return new Location(p.y/50,p.x/50);
 	}
 	
 	/**
 	 * Generates a leaf color.
 	 * @return
 	 */
 	public static Color generateNewLeafColor() {
 		byte pointer = (byte)Game.Settings.rand.nextInt(0, 5);
 		switch (pointer) {
 			case 0:
 				return Game.Settings.rand.nextColor(100,120,29,15);
 			case 1:
 				return Game.Settings.rand.nextColor(50,60,15,15);
 			case 2:
 				return Game.Settings.rand.nextColor(135,185,98,15);
 		}
 		return Game.Settings.rand.nextColor(60,75,70,15);
 	}
 	
 	/**
 	 * Modulates a leaf color to match
 	 * random values.
 	 * @param base The base leaf color.
 	 * @return
 	 */
 	public static Color generateLeafMod(Color base) {
 		Color c = Game.Settings.rand.nextColor(base.getRed(),base.getGreen(),base.getBlue(),8);
 		return new Color(c.getRed(),c.getGreen(),c.getBlue(),125);
 	}
 	
 	/**
 	 * Linear interpolation.
 	 * @param num0
 	 * @param num1
 	 * @param amount
 	 * @return
 	 */
 	public static double Lerp(int num0, int num1, double amount)
     {
     	return num0 + (amount*(num1-num0));
     }
 	
 	/**
 	 * Linear interpolation.
 	 * @param color0
 	 * @param color1
 	 * @param amount
 	 * @return
 	 */
     public static Color Lerp(Color color0, Color color1, double amount)
     {
 	    int r = (int)Lerp(color0.getRed(), color1.getRed(), amount);
 	    int g = (int)Lerp(color0.getGreen(), color1.getGreen(), amount);
 	    int b = (int)Lerp(color0.getBlue(), color1.getBlue(), amount);
 	    int a = (int)Lerp(color0.getAlpha(), color1.getAlpha(), amount);
 	    if (r > 255)
 	    	r = 255;
 	    if (r < 0)
 	    	r = 0;
 	    if (g > 255)
 	    	g = 255;
 	    if (g < 0)
 	    	g = 0;
 	    if (b > 255)
 	    	b = 255;
 	    if (b < 0)
 	    	b = 0;
 	    if (a > 255)
 	    	a = 255;
 	    if (a < 0)
 	    	a = 0;
 	    return new Color(r,g,b,a);
     }
     
     /**
      * Determines if an entity is visible in the
      * given world.
      * @param w The world to use.
      * @param ent The entity to find.
      * @return
      * @deprecated Not used anymore.
      */
 	public static boolean isEntityVisibleOnScreen(World w, Entity ent) {
 		if (w == null || ent == null || ent.getLocation() == null)
 			return false;
 		int sx = w.getScreenX();
 		int sy = w.getScreenY();
 		int px = ent.getLocation().getCol() * 50;
 		int py = ent.getLocation().getRow() * 50;
 		if (px >= sx && py >= sy && px <= sx + Game.Settings.currentWidth && py <= sy + Game.Settings.currentHeight)
 			return true;
 		else
 			return false;
 		
 	}
 	
 	/**
 	 * Creates a graphics object for the given
 	 * image.
 	 * 
 	 * Note: Perhaps the most used method in Pavo.
 	 * @param b The image to get the object for.
 	 * @return
 	 */
 	public static Graphics2D createGraphics(PavoImage b) {
 		if (b == null)
 			return null;
 		
 		Graphics2D g = (Graphics2D) b.getGraphics();
 		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
 		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
 		return g;
 	}
 	
 	/**
 	 * Creates a graphics object for the given
 	 * image.
 	 * 
 	 * Note: Perhaps the most used method in Pavo.
 	 * @param b The image to get the object for.
 	 * @return
 	 */
 	public static Graphics2D createGraphics(BufferedImage b) {
 		if (b == null)
 			return null;
 		
 		Graphics2D g = (Graphics2D) b.getGraphics();
 		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
 		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
 		return g;
 	}
 	
 	/**
 	 * Gets the default "spacer" pixel.
 	 */
 	public static final BufferedImage OneByOnePixel = create1x1Pixel();
 	
 	/**
 	 * Gets a "spacer" pixel for use.
 	 * @return
 	 */
 	public static BufferedImage create1x1Pixel() {
 		BufferedImage b = new BufferedImage(1,1,BufferedImage.TYPE_INT_RGB);
 		Graphics g = b.getGraphics();
 		g.setColor(Color.white);
 		g.fillRect(0,0,2,2);
 		g.dispose();
 		return b;
 	}
 	
 	/**
 	 * Changes the opacity of a given image.
 	 * @param src The source image.
 	 * @param opacity The opacity to change to.
 	 * @return
 	 */
 	public static BufferedImage imgUtilAdjustImageTransparency(BufferedImage src, float opacity) {
 		if (src == null)
 			return null;
 		AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity);
 		BufferedImage cpy = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_ARGB);
 		Graphics2D g = (Graphics2D)cpy.getGraphics();
 	    g.setComposite(ac);
 	    g.drawImage(src,0,0,null);
 	    g.dispose();
 	    return cpy;
 	}
 	
 	/**
 	 * Quickly copies an image.
 	 * @param src The source image.
 	 * @return The replicated image.
 	 */
 	public static BufferedImage imgUtilFastCopy(BufferedImage src) {
 		if (src == null)
 			return null;
 		BufferedImage b = new BufferedImage(src.getWidth(), src.getHeight(), src.getType());
 		b.setData(src.getRaster());
 		return b;
 	}
 	
 	/**
 	 * Creates an outline of an image,
 	 * with the default clipping rectangle.
 	 * @param src The source image.
 	 * @param c The color to outline the image
 	 * in.
 	 * @return
 	 */
 	public static BufferedImage imgUtilOutline(BufferedImage src, Color c) {
 		if (src == null)
 			return null;
 		BufferedImage b = new BufferedImage(src.getWidth(), src.getHeight(), src.getType());
 		Graphics2D g = (Graphics2D)b.getGraphics();
 		g.setColor(c);
 		g.drawRect(1,1,src.getWidth()-1, src.getHeight()-1);
 		g.drawImage(src,0,0,null);
 		g.dispose();
 		return b;
 	}
 	
 	/**
 	 * Quickly crops an image from its source.
 	 * @param src The source image.
 	 * @param x The x coordinate to start at.
 	 * @param y The y coordinate to start at.
 	 * @param width The width to clip.
 	 * @param height The height to clip.
 	 * @return
 	 */
 	public static BufferedImage imgUtilFastCrop(BufferedImage src, int x, int y, int width, int height) {
 		if (src == null)
 			return null;
 		if (x == 0 && y == 0 && width == src.getWidth() && height == src.getHeight())
 			return imgUtilFastCopy(src);
 		else {
 			BufferedImage b = new BufferedImage(width, height, src.getType());
 			Graphics g = b.getGraphics();
 			g.drawImage(src,-x,-y,null);
 			g.dispose();
 			return b;
 		}
 	}
 	
 	/**
 	 * Shrinks an image to fit into memory more
 	 * Effectively.
 	 * @param src The source image.
 	 * @return
 	 */
 	public static BufferedImage imgUtilMinimizeNoAlpha(BufferedImage src) {
 		if (src == null)
 			return null;
 		BufferedImage b = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
 		Graphics2D g = (Graphics2D)b.getGraphics();
 		g.drawImage(src,0,0,null);
 		g.dispose();
 		return b;
 	}
 }
