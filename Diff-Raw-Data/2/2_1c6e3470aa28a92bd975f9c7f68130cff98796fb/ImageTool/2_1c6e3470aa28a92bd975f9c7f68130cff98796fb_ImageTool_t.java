 package coggame;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Rectangle;
 import java.awt.Toolkit;
 import java.awt.Image;
 import java.awt.image.BufferedImage;
 import java.awt.image.PixelGrabber;
 import java.awt.image.RenderedImage;
 import java.io.File;
 import java.io.IOException;
 import java.util.List;
 import java.util.ArrayList;
 import javax.imageio.ImageIO;
 
 /**
 * The ImageTool is a collection of utility
 * routines for dealing with Images.
 *
 * @author John Earnest
 **/
 public class ImageTool {
 	
 	/**
 	* Unpack an image into a 2D grid of AWT Color
 	* values for easy manipulation and inspection.
 	*
 	* @param i the source image
 	**/
 	public static Color[][] getPixels(Image i) {
 		final int w = i.getWidth(null);
 		final int h = i.getHeight(null);
 		final int a[] = new int[w * h];
 		final PixelGrabber pg = new PixelGrabber(i,0,0,w,h,a,0,w);
 		try { pg.grabPixels(); }
 		catch(InterruptedException ie) { ie.printStackTrace(); }
 
 		final Color[][] ret = new Color[w][h];
 		for(int x = 0; x < w; x++) {
 			for(int y = 0; y < h; y++) {
 				int c = a[x + (w * y)];
 				ret[x][y] = new Color(	(c >> 24) & 0xFF,
 										(c >> 16) & 0xFF,
 										(c >>  8) & 0xFF,
 										(c >>  0) & 0xFF );
 			}
 		}
 		return ret;
 	}
 
 	/**
 	* Scan an image and compile a table
 	* of all the colors used.
 	*
 	* @param i the source image
 	**/
 	public static Color[] getColors(Image i) {
 		final List<Color> ret = new ArrayList<Color>();
 		for(Color[] a : getPixels(i)) {
 			for(Color b : a) {
 				if (!ret.contains(b)) { ret.add(b); }
 			}
 		}
 		return (Color[]) ret.toArray();
 	}
 
 	/**
 	* Replace every instance of a color in the
 	* first table with the corresponding color
 	* in the second table. Any colors not present
 	* in the first table will be unchanged.
 	*
 	* @param i the source image
 	* @param find the colors to scan for
 	* @param replace the resplacement colors
 	**/
 	public static Image recolor(Image i, Color[] find, Color[] replace) {
 		int w = i.getWidth(null);
 		int h = i.getHeight(null);
 		int a[] = new int[w * h];
 		int c[] = new int[find.length];
 		Image ret = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
 		Graphics g = ret.getGraphics();
 		PixelGrabber pg = new PixelGrabber(i,0,0,w,h,a,0,w);
 		try {pg.grabPixels();}
 		catch(InterruptedException ie){ie.printStackTrace();}
 		for(int z=0;z<find.length;z++) {
 			c[z] = find[z].getRGB();
 		}
 		g.drawImage(i,0,0,null);
 		for(int y = 0; y < h; y++) {
 			for(int x = 0; x < w; x++) {
 				for(int z = 0; z < c.length; z++) {
 					if (a[x + (y * w)] == c[z]) {
 						g.setColor(replace[z]);
 						g.drawLine(x, y, x, y);
 						break;
 					}
 				}
 			}
 		}
 		return ret;
 	}
 
 	/**
 	* Returns true if the supplied image has
 	* any pixels that are not completely opaque.
 	*
 	* @param i the source image
 	**/
 	public static boolean hasTransparency(Image i) {
 		return hasTransparency(i, i.getWidth(null), i.getHeight(null), 1);
 	}
 
 	private static int[] getTile(Image i, int tileWidth, int tileHeight, int tile) {
 		final int w = i.getWidth(null);
 		final int ret[] = new int[tileWidth * tileHeight];
 		final int tx = ((tile - 1) % (w / tileWidth)) * tileWidth;
 		final int ty = ((tile - 1) / (w / tileWidth)) * tileHeight;
		final PixelGrabber pg = new PixelGrabber(i,tx,ty,tileWidth,tileHeight,ret,0,tileWidth);
 		try { pg.grabPixels(); }
 		catch(InterruptedException ie) { ie.printStackTrace(); }
 		return ret;
 	}
 
 	/**
 	* Returns true if a given 1-indexed tile within
 	* the supplied image has any pixels that are not
 	* completely opaque.
 	*
 	* @param i the source image
 	* @param tileWidth the width of a tile in pixels
 	* @param tileHeight the height of a tile in pixels
 	* @param tile the 1-based index of the tile to examine
 	**/
 	public static boolean hasTransparency(Image i, int tileWidth, int tileHeight, int tile) {
 		final int[] a = getTile(i, tileWidth, tileHeight, tile);
 		for(int x : a) {
 			if ((x & 0xFF000000) != 0xFF000000) { return true; }
 		}
 		return false;
 	}
 
 	/**
 	* Return a Rectangle with a position relative to
 	* the upper-left corner of an image whose edges align with
 	* the boundaries of non-transparent pixels within the image.
 	*
 	* @param i the source image
 	**/
 	public static Rectangle tightBound(Image i) {
 		return tightBound(i, i.getWidth(null), i.getHeight(null), 1);
 	}
 
 	/**
 	* Return a Rectangle with a position relative to
 	* the upper-left corner of a tile whose edges align with
 	* the boundaries of non-transparent pixels within the tile.
 	* This is mainly intended for producing collision boxes for
 	* animated sprites, in which the "solid" region of an object
 	* may vary from frame to frame.
 	*
 	* @param i the source image
 	* @param tileWidth the width of a tile in pixels
 	* @param tileHeight the height of a tile in pixels
 	* @param tile the 1-based index of the tile to examine
 	**/
 	public static Rectangle tightBound(Image i, int tileWidth, int tileHeight, int tile) {
 		final int[] a = getTile(i, tileWidth, tileHeight, tile);
 		int x = tileWidth;
 		int y = tileHeight;
 		int w = 0;
 		int h = 0;
 		for(int z = 0; z < a.length; z++) {
 			if ((a[z] & 0xFF000000) != 0xFF000000) { continue; }
 
 			int px = z % tileWidth;
 			int py = z / tileWidth;
 			if (px < x)		{ x = px; }
 			if (py < y)		{ y = py; }
 			if (px - x > w) { w = px - x; }
 			if (py - y > h) { h = py - y; }
 		}
 		return new Rectangle(x, y, w, h);
 	}
 
 	/**
 	* A convenience method for loading image files
 	* from this application's JAR. Blocks until the
 	* image is fully loaded.
 	*
 	* @param filename the filename of the image resource
 	**/
 	public static Image loadImage(String filename) {
 		Toolkit toolkit = Toolkit.getDefaultToolkit();
 		ClassLoader loader = ImageTool.class.getClassLoader();
 		Image ret = toolkit.getImage(loader.getResource(filename));
 		while (ret.getWidth(null) < 0) {
 			try {Thread.sleep(10);}
 			catch(InterruptedException ie) {}
 		}
 		return ret;
 	}
 
 	/**
 	* A convenience method for saving images to
 	* the local filesystem. File extensions are inferred
 	* from the filename (.gif, .png, .jpg, .bmp), and
 	* .png is used as a default if the extension is
 	* unrecognized or absent.
 	* Returns false if the image did not save successfully.
 	**/
 	public static boolean saveImage(RenderedImage image, String filename) {
 		String extension;
 		if		(filename.toUpperCase().endsWith(".GIF")) { extension = "gif"; }
 		else if	(filename.toUpperCase().endsWith(".JPG")) { extension = "jpg"; }
 		else if	(filename.toUpperCase().endsWith(".JPEG")) { extension = "jpeg"; }
 		else if (filename.toUpperCase().endsWith(".BMP")) { extension = "bmp"; }
 		else if (filename.toUpperCase().endsWith(".PNG")) { extension = "png"; }
 		else	{ extension = "png"; filename += "." + extension; }
 
 		try {
 			ImageIO.write(image, extension, new File(filename));
 		}
 		catch(IOException ioe) { return false; }
 		return true;
 	}
 }
