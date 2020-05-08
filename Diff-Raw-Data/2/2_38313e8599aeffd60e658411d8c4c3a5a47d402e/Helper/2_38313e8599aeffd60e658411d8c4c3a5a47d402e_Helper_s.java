 package com.jpii.navalbattle.renderer;
 
 import java.awt.*;
 import java.awt.MultipleGradientPaint.CycleMethod;
 import java.awt.geom.*;
 import java.awt.image.*;
 import java.io.*;
 import java.util.*;
 
 import com.jpii.navalbattle.pavo.MessageBox;
 import com.jpii.navalbattle.util.FileUtils;
 
 import maximusvladimir.dagen.Rand;
 
 /**
  * The rendering helper. Consists of static methods.
  * @author MKirkby
  *
  */
 public class Helper {
 	public static BufferedImage GUI_OMNIMAP_BACKGROUND1;
 	public static BufferedImage GUI_OMNIMAP_BACKGROUND2;
 	public static BufferedImage GUI_OMNIMAP_ICON_WORLD;
 	public static BufferedImage GUI_WINDOW_ICON;
 	public static BufferedImage GUI_GLYPHS;
 	private static BufferedImage[] GUI_GLYPH_CHARS;
 	public static Font GUI_GAME_FONT;
 	public static Font GUI_MENU_TITLE_FONT;
 	private static Font loadFont(String name) {
 	    Font font = null;
 	    if (name == null) {
 	        return new Font("Fixedsys",0,11);
 	    }
 	    try {
 	        File file = new File(Helper.class.getResource("/com/jpii/navalbattle/res/" + name).toURI());
 	        font = Font.createFont(Font.TRUETYPE_FONT, file);
 	        GraphicsEnvironment ge = GraphicsEnvironment
 	                .getLocalGraphicsEnvironment();
 	        ge.registerFont(font);
 	    } catch (Exception ex) {
 	    	return new Font("Fixedsys",0,11);
 	    }
 	    return font;
 	}
 	public static void LoadStaticResources() {
 		loadFont("munro.ttf");
		GUI_GAME_FONT = new Font("Munro",0,16);
 		GUI_MENU_TITLE_FONT = new Font("Arial",0,56);
 		try {
 			GUI_OMNIMAP_BACKGROUND1 = 
 					FileUtils.getImage(("drawable-gui/gui_omnimap_background1.png"));
 			GUI_OMNIMAP_BACKGROUND2 = 
 					FileUtils.getImage(("drawable-gui/gui_omnimap_background2.png"));
 			GUI_WINDOW_ICON = FileUtils.getImage(("drawable-gui/gui_window_icon.png"));
 			GUI_GLYPHS= FileUtils.getImage(("font/glyphs.bmp"));
 			MessageBox.setMessageBoxErrorIcon(
 					FileUtils.getImageFromOtherPath("/com/jpii/navalbattle/pavo/res/msg_error.png"));
 			MessageBox.setMessageBoxWarnIcon(
 					FileUtils.getImageFromOtherPath("/com/jpii/navalbattle/pavo/res/msg_warn.png"));
 			MessageBox.setMessageBoxNotifyIcon(
 					FileUtils.getImageFromOtherPath("/com/jpii/navalbattle/pavo/res/msg_notify.png"));
 			MessageBox.setMessageBoxInfoIcon(
 					FileUtils.getImageFromOtherPath("/com/jpii/navalbattle/pavo/res/msg_info.png"));
 			
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		
 		
 		GUI_OMNIMAP_ICON_WORLD = new BufferedImage(20,20,BufferedImage.TYPE_INT_ARGB);
 		Graphics g = GUI_OMNIMAP_ICON_WORLD.getGraphics();
 		g.setColor(new Color(38, 65, 136));
         g.fillOval(0, 0, 19, 19);
         g.setColor(Color.black);
         g.drawOval(0, 0, 19, 19);
         g.setColor(new Color(90, 142, 81));
         int xt = 3;
         g.drawLine(4+xt, 1, 9+xt, 1);
         g.drawLine(2+xt, 2, 11+xt, 2);
         g.drawLine(1+xt, 3, 12+xt, 3);
         g.drawLine(1+xt, 4, 11+xt, 4);
         g.drawLine(1+xt, 5, 11+xt, 5);
         g.drawLine(2+xt, 6, 10+xt, 6);
         g.drawLine(3+xt, 7, 9+xt, 7);
         g.drawLine(3+xt, 8, 6+xt, 8);
         g.drawLine(3+xt, 9, 5+xt, 9);
         g.drawLine(3+xt, 10, 5+xt, 10);
         g.drawLine(2+xt, 11, 8+xt, 11);
         g.drawLine(1+xt, 12, 9+xt, 12);
         g.drawLine(1+xt, 13, 10+xt, 13);
         g.drawLine(0+xt, 14, 10+xt, 14);
         g.drawLine(1+xt, 15, 10+xt, 15);
         g.drawLine(1+xt, 16, 10+xt, 16);
         g.drawLine(2+xt, 17, 9+xt, 17);
         g.drawLine(4+xt, 18, 7+xt, 18);
         glyphParser();
 	}
 	private static void glyphParser() {
 		BufferedImage glyphs = GUI_GLYPHS;
 		int size = (glyphs.getWidth()/8)*(glyphs.getHeight()/11);
 		GUI_GLYPH_CHARS = new BufferedImage[size];
 		int c = 0;
 		for (int x = 0; x < (glyphs.getWidth()); x+=9) {
 			for (int y = 0; y < (glyphs.getHeight()); y+=12) {
 				if (x + 8 < glyphs.getWidth() && y + 11 < glyphs.getHeight()) {
 					GUI_GLYPH_CHARS[c] = glyphs.getSubimage(x,y,8,11);
 					BufferedImage copy = new BufferedImage(8,11,BufferedImage.TYPE_INT_ARGB);
 					Graphics g = copy.getGraphics();
 					for (int cx = 0; cx < GUI_GLYPH_CHARS[c].getWidth(); cx++) {
 						for (int cy = 0; cy < GUI_GLYPH_CHARS[c].getHeight(); cy++) {
 							if (new Color(GUI_GLYPH_CHARS[c].getRGB(cx,cy)).getBlue() > 20) {
 								g.setColor(Color.red);
 								g.drawLine(cx,cy,cx,cy);
 							}
 						}
 					}
 					c++;
 				}
 			}
 		}
 	}
 	public static void drawString(String txt, int x, int y, Graphics g) {
 		int x2 = 0;
 		for (int v = 0; v < txt.length(); v++) {
 			char c = txt.charAt(v);
 			drawChar(c,x2 + x, y, g);
 			x2 += 8;
 		}
 	}
 	public static Color randomColor(Random r) {
 		return new Color(r.nextInt(127) + 127, r.nextInt(127) + 127, r.nextInt(127) + 127);
 	}
 	public static void drawChar(char c, int x, int y, Graphics g) {
 		BufferedImage img = null;
 		switch (c) {
 			case ' ':
 				img = GUI_GLYPH_CHARS[0];
 			break;
 			case '\u0263':
 				img = GUI_GLYPH_CHARS[1];
 			break;
 			case '!':
 				img = GUI_GLYPH_CHARS[33];
 			break;
 			case '"':
 				img = GUI_GLYPH_CHARS[34];
 			break;
 			case '#':
 				img = GUI_GLYPH_CHARS[35];
 			break;
 			case '$':
 				img = GUI_GLYPH_CHARS[36];
 			break;
 			case '%':
 				img = GUI_GLYPH_CHARS[37];
 			break;
 			case '&':
 				img = GUI_GLYPH_CHARS[38];
 			break;
 			case '\'':
 				img = GUI_GLYPH_CHARS[39];
 			break;
 			case '(':
 				img = GUI_GLYPH_CHARS[40];
 			break;
 			case ')':
 				img = GUI_GLYPH_CHARS[41];
 			break;
 			case '*':
 				img = GUI_GLYPH_CHARS[42];
 			break;
 			case '+':
 				img = GUI_GLYPH_CHARS[43];
 			break;
 			case ',':
 				img = GUI_GLYPH_CHARS[44];
 			break;
 			case '-':
 				img = GUI_GLYPH_CHARS[45];
 			break;
 			case '.':
 				img = GUI_GLYPH_CHARS[46];
 			break;
 			case '/':
 				img = GUI_GLYPH_CHARS[47];
 			break;
 			case '0':
 				img = GUI_GLYPH_CHARS[48];
 			break;
 			case '1':
 				img = GUI_GLYPH_CHARS[49];
 			break;
 			case '2':
 				img = GUI_GLYPH_CHARS[50];
 			break;
 			case '3':
 				img = GUI_GLYPH_CHARS[51];
 			break;
 			case '4':
 				img = GUI_GLYPH_CHARS[52];
 			break;
 			case '5':
 				img = GUI_GLYPH_CHARS[53];
 			break;
 			case '6':
 				img = GUI_GLYPH_CHARS[54];
 			break;
 			case '7':
 				img = GUI_GLYPH_CHARS[55];
 			break;
 			case '8':
 				img = GUI_GLYPH_CHARS[56];
 			break;
 			case '9':
 				img = GUI_GLYPH_CHARS[57];
 			break;
 			case ':':
 				img = GUI_GLYPH_CHARS[58];
 			break;
 		}
 		g.drawImage(img,x,y,null);
 	}
 	/**
 	 * Creates a nifty looking inner shadow for the window.
 	 * @param width The width of the shadow.
 	 * @param height The height of the shadow.
 	 * @return The translucent shadow that was created.
 	 */
 	public static BufferedImage genInnerShadow(int width, int height) {
 		BufferedImage shadowOuter = new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB);
 		Graphics g = shadowOuter.getGraphics();
 		Graphics2D g2 = (Graphics2D)g;
 		Point2D center = new Point2D.Float(width/2,height/2);
         float radius = width;
         Point2D focus = new Point2D.Float(width/2, height/2);
         float[] dist = {0.0f, 1.0f};
         Color[] colors = {new Color(0,0,0,0), new Color(0,0,0,220)};
         RadialGradientPaint p = new RadialGradientPaint(center, radius, focus, dist, colors, CycleMethod.NO_CYCLE);
         g2.setPaint(p);
         g2.fillRect(0, 0, width,height);
         return shadowOuter;
 	}
 	/*public static BufferedImage genGrid(int width, int height, int spacing) {
 		BufferedImage grid = new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB);
 		Graphics g = grid.getGraphics();
 		Color def = new Color(60,60,60);
 		Color nwe = new Color(60,60,60,200);
 		int sub = height/spacing;
 		for (int gridy = 0; gridy < sub; gridy++) {
 			if (spacing <= 10 && (gridy % 4 == 0)) {
 				g.setColor(nwe);
 			}
 			else {
 				g.setColor(def);
 			}
 			g.drawLine(0, gridy*spacing, width, gridy*spacing);
 		}
 		sub = width/spacing;
 		for (int gridx = 0; gridx < sub; gridx++) {
 			if (spacing <= 10 && gridx % 4 == 0) {
 				g.setColor(nwe);
 			}
 			else {
 				g.setColor(def);
 			}
 			g.drawLine(gridx*spacing,0,gridx*spacing,height);
 		}
 		return grid;
 	}*/
 	
 	/**
 	 * Adjusts the double to fit it within 0.0 and 1.0.
 	 * @param d The double to adjust.
 	 * @return The adjusted double.
 	 */
 	public static double snap(double d) {
 		if (d > 1.0)
 			d = 1.0;
 		if (d < 0.0)
 			d = 0.0;
 		return (d);
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
 	    return new Color(r,g,b,a);
     }
 
     public static int ComputeTime() {
     	int le = RenderConstants.DAYNIGHT_LENGTH_IN_SECONDS;
     	int alph = 0;
     	double tofd = RenderConstants.CURRENT_TIME_OF_DAY;
     	int nightl = 5;
     	boolean changed = false;
     	if (tofd > 0 && tofd < le / nightl/2)
     	{
     		double t = tofd;
     		alph = (int)(t * RenderConstants.NIGHT_MAX_DARKNESS / (le / nightl/2));
     		if (alph < 0)
     			alph = 0;
     		if (alph > 255)
     			alph = 255;
     		if (!last.equals("Sunset"))
     		{
     			last = "Sunset";
     			changed = true;
     		}
     	}
     	else if (tofd > le / nightl/2 && tofd < le / nightl * 2)
     	{
     		alph = RenderConstants.NIGHT_MAX_DARKNESS;
     		if (!last.equals("Night"))
     		{
     			last = "Night";
     			changed = true;
     		}
     	}
     	else if (tofd > le / nightl * 2 && tofd < (le / nightl * 2) + (le / nightl / 2))
     	{
     		double t = ((le / nightl * 2) + (le / nightl / 2))- tofd;
     		alph = (int)(t * RenderConstants.NIGHT_MAX_DARKNESS / (le / nightl /2));
     		if (alph < 0)
     			alph = 0;
     		if (alph > 255)
     			alph = 255;
     		if (!last.equals("Sunrise"))
     		{
     			last = "Sunrise";
     			changed = true;
     		}
     	}
     	else {
     		if (!last.equals("Daytime"))
     		{
     			last = "Daytime";
     			changed = true;
     		}
     	}
     	if (changed)
     		Console.getInstance().printInfo("Time of Day-" + last);
     	return alph;
     }
     private static String last = "s";
     
     /**
      * Randomise a preset color, by the maximum differencial as the maximum absolute value, with the random provider.
      * @param orig The orginal color.
      * @param maxDiff The maximum differencial.
      * @param rand The random number provider.
      * @param includeAlpha Should alpha be included with the randomisation??? (Usually is false)
      * @return
      */
     public static Color randomise(Color orig, int maxDiff, Random rand, boolean includeAlpha) {
         int r = orig.getRed();
         int g = orig.getGreen();
         int b = orig.getBlue();
         int a = orig.getAlpha();
 
         r += rand.nextInt(maxDiff + maxDiff) - maxDiff;
         g += rand.nextInt(maxDiff + maxDiff) - maxDiff;
         b += rand.nextInt(maxDiff + maxDiff) - maxDiff;
         if (includeAlpha) a += rand.nextInt(maxDiff + maxDiff) - maxDiff;
 
         r = colorSnap(r);
         g = colorSnap(g);
         b = colorSnap(b);
         a = colorSnap(a);
 
         return new Color(r, g, b, a);
     }
     /**
      * Snaps a rgba value to a byte.
      * @param rgbaval The integer to snap.
      * @return Between 0 and 255.
      */
     public static int colorSnap(int rgbaval) {
         if (rgbaval > 255) rgbaval = 255;
         if (rgbaval < 0) rgbaval = 0;
         return rgbaval;
     }
     /**
      * Adjusts a preset color to a roughness value.
      * @param orig The original color
      * @param a The roughness value.
      * @param maxmin The maximum and minimum differencial.
      * @return A color that has been adjust to match the orginal and roughness.
      */
     public static Color adjust(Color orig, double a, int maxmin) {
         if (a > 1) a = 1;
         if (a < 0) a = 0;
 
         int r = orig.getRed();
         int g = orig.getGreen();
         int b = orig.getBlue();
 
         r = (int)(r - (a * maxmin));
         g = (int)(g - (a * maxmin));
         b = (int)(b - (a * maxmin));
 
         r = colorSnap(r);
         g = colorSnap(g);
         b = colorSnap(b);
         return new Color(r, g, b, orig.getAlpha());
     }
     public static Color adjust2(Color original, double value, int maximumrange) {
     	 if (value > 1) value = 1;
          if (value < 0) value = 0;
 
          int r = original.getRed();
          int g = original.getGreen();
          int b = original.getBlue();
          
          double nah = value * maximumrange;
          
          r += (int)nah;
          g += (int)nah;
          b += (int)nah;
          
          r = colorSnap(r);
          g = colorSnap(g);
          b = colorSnap(b);
          return new Color(r, g, b, original.getAlpha());
     }
 	public static Color randomise(Color orig, int maxDiff, Rand rand, boolean includeAlpha) {
 		int r = orig.getRed();
         int g = orig.getGreen();
         int b = orig.getBlue();
         int a = orig.getAlpha();
 
         r += rand.nextInt(maxDiff + maxDiff) - maxDiff;
         g += rand.nextInt(maxDiff + maxDiff) - maxDiff;
         b += rand.nextInt(maxDiff + maxDiff) - maxDiff;
         if (includeAlpha) a += rand.nextInt(maxDiff + maxDiff) - maxDiff;
 
         r = colorSnap(r);
         g = colorSnap(g);
         b = colorSnap(b);
         a = colorSnap(a);
 
         return new Color(r, g, b, a);
     }
 }
