 package com.jpixlib.image;
 
 /**
  * A container for 2D pixel data. The class includes some useful functions to
  * write color/image data.
  * 
  * @author Denis Zhidkikh
  * @version 1.0
  * @since 26.10.2013
  * 
  */
 public class Bitmap {
 	protected int width, height;
 
 	/**
 	 * Raw pixel data.
 	 */
 	public int[] pixels;
 
 	/**
 	 * Initializes an empty bitmap.
 	 * 
 	 * @param width
 	 *            Width of the bitmap.
 	 * @param height
 	 *            Height of the bitmap.
 	 */
 	public Bitmap(int width, int height) {
 		this.width = width;
 		this.height = height;
 		pixels = new int[width * height];
 	}
 
 	/**
 	 * Fills the whole bitmap with a single color thus clearing the bitmap.
 	 * 
 	 * @param color
 	 *            Color to fill with.
 	 */
 	public void clear(int color) {
 		for (int i = 0; i < pixels.length; i++)
 			pixels[i] = color;
 	}
 
 	/**
 	 * Fills an area of the bitmap with a single color.
 	 * 
 	 * @param color
 	 *            Color to fill with.
 	 * @param x
 	 *            Position where to start the filling (X coordinate).
 	 * @param y
 	 *            Position where to start the filling (Y coordinate).
 	 * @param w
 	 *            Width of the area to fill.
 	 * @param h
 	 *            Height of the area to fill.
 	 */
 	public void fill(int color, int x, int y, int w, int h) {
 		int x0 = x < 0 ? 0 : x;
 		int x1 = x0 + w;
 		int y0 = y < 0 ? 0 : y;
 		int y1 = y0 + h;
 		if (y1 >= height)
 			y1 = height - 1;
 		if (x1 >= width)
 			x1 = width - 1;
 
 		for (int yp = y0; yp <= y1; yp++)
 			for (int xp = x0; xp <= x1; xp++)
 				pixels[xp + yp * width] = color;
 	}
 
 	/**
 	 * Combines (blits) given bitmap with this one. </br></br> <b>Note:</b> The
 	 * position can be negative or exceed the size of this bitmap.
 	 * 
 	 * @param b
 	 *            Bitmap to blit with.
 	 * @param x
 	 *            Position on this bitmap where to begin blitting.
 	 * @param y
 	 *            Position on this bitmap where to begin blitting.
 	 * 
 	 */
 	public void blit(Bitmap b, int x, int y) {
 		int x0 = x < 0 ? 0 : x;
		int x1 = x0 + b.width;
 		int y0 = y < 0 ? 0 : y;
		int y1 = y0 + b.height;
 
 		if (y1 > height)
 			y1 = height;
 		if (x1 > width)
 			x1 = width;
 
 		int w = x1 - x0;
 		int h = y1 - y0;
 
 		if (w <= 0 || h <= 0)
 			return;
 
 		for (int yp = y0; yp < y1; yp++) {
			int tp = yp * width;
 			int bp = (x0 - x) + (yp - y) * b.width;

			for (int xp = x0; xp < x1; xp++) {
				pixels[xp + tp] = pixels[xp + bp];
 			}
 		}
 	}
 
 	/**
 	 * Combines (blits) a part of a bitmap with another one.
 	 * 
 	 * @param b
 	 *            Bitmap to blit with.
 	 * @param x
 	 *            Where to blit to (X coordinate).
 	 * @param y
 	 *            Where to blit to (Y coordinate).
 	 * @param xb
 	 *            Where to blit from (X coordinate).
 	 * @param yb
 	 *            Where to blit from (Y coordinate).
 	 * @param w
 	 *            Width of the area to blit.
 	 * @param h
 	 *            Height of the area to blit.
 	 */
 	public void blit(Bitmap b, int x, int y, int xb, int yb, int w, int h) {
 		int x0 = x < 0 ? 0 : x;
		int x1 = x0 + w;
 		int y0 = y < 0 ? 0 : y;
		int y1 = y0 + h;
 
 		if (y1 > height)
 			y1 = height;
 		if (x1 > width)
 			x1 = width;
 
 		int ww = x1 - x0;
 		int hh = y1 - y0;
 
 		if (ww <= 0 || hh <= 0)
 			return;
 
 		for (int yp = y0; yp < y1; yp++) {
			int tp = yp * width;
 			int sp = (x0 - x + xb) + (yp - y + yb) * b.width;

			for (int xp = x0; xp < x1; xp++) {
				pixels[xp + tp] = b.pixels[xp + sp];
 			}
 		}
 	}
 
 	/**
 	 * Gets the width of this bitmap.
 	 * 
 	 * @return Width of this bitmap.
 	 */
 	public int getWidth() {
 		return width;
 	}
 
 	/**
 	 * Gets the width of this bitmap.
 	 * 
 	 * @return Width of this bitmap.
 	 */
 	public int getHeight() {
 		return height;
 	}
 
 	@Override
 	public String toString() {
 		StringBuffer sb = new StringBuffer("[BITMAP] ");
 		sb.append(width).append("x").append(height);
 		sb.append(" Hash: ").append(Integer.toHexString(hashCode()));
 		return sb.toString();
 	}
 }
