 package pl.shockah.easyslick;
 
 import java.util.ArrayList;
 
 import org.newdawn.slick.Color;
 import org.newdawn.slick.Font;
 import org.newdawn.slick.UnicodeFont;
 import org.newdawn.slick.font.effects.ColorEffect;
 import org.newdawn.slick.geom.Vector2f;
 
 import pl.shockah.Helper;
 
 public class Fonts {
 	public static final int
 		TopLeft = 0, TopCenter = 1, TopRight = 2,
 		MiddleLeft = 3, MiddleCenter = 4, MiddleRight = 5,
 		BottomLeft = 6, BottomCenter = 7, BottomRight = 8;
 	private static int fontAlign = TopLeft;
 	
 	public static final UnicodeFont
 		standard12 = newUnicodeFontFile("fontExpresswayFree.ttf",12,false,false),
 		standard16 = newUnicodeFontFile("fontExpresswayFree.ttf",16,false,false),
 		standard24 = newUnicodeFontFile("fontExpresswayFree.ttf",24,false,false),
 		standard36 = newUnicodeFontFile("fontExpresswayFree.ttf",36,false,false);
 	
 	public static UnicodeFont newUnicodeFontFile(String fontPath, int size, boolean bold, boolean italic) {
 		try {
 			UnicodeFont font = new UnicodeFont(fontPath,size,bold,italic);
 			return prepareUnicodeFont(font);
 		} catch (Exception e) {App.getApp().handle(e);}
 		return null;
 	}
 	public static UnicodeFont newUnicodeFontSystem(String fontName, int size, boolean bold, boolean italic) {
 		int style = java.awt.Font.PLAIN;
 		if (bold) style += java.awt.Font.BOLD;
 		if (italic) style += java.awt.Font.ITALIC;
 		
 		UnicodeFont font = new UnicodeFont(new java.awt.Font(fontName,style,size),size,bold,italic);
 		return prepareUnicodeFont(font);
 	}
 	
 	@SuppressWarnings("unchecked") private static UnicodeFont prepareUnicodeFont(UnicodeFont font) {
 		try {
 			font.getEffects().add(new ColorEffect(java.awt.Color.white));
 			font.addAsciiGlyphs();
 			font.loadGlyphs();
 		} catch (Exception e) {App.getApp().handle(e);}
 		return font;
 	}
 	
 	public static Vector2f drawStringShadow(GraphicsHelper gh, String text, float x, float y) {
 		drawString(gh,text,x-1,y-1);
 		drawString(gh,text,x+1,y-1);
 		drawString(gh,text,x-1,y+1);
 		return drawString(gh,text,x+1,y+1).add(new Vector2f(-1,-1));
 	}
 	public static Vector2f drawStringShadowed(GraphicsHelper gh, String text, float x, float y, Color shadowColor) {
 		Color color = gh.g().getColor();
 		gh.g().setColor(shadowColor);
 		drawStringShadow(gh,text,x,y);
 		gh.g().setColor(color);
 		return drawString(gh,text,x,y);
 	}
 	
 	public static void resetFontAlign() {fontAlign = TopLeft;}
 	public static void setFontAlign(int fontAlign) {Fonts.fontAlign = fontAlign;}
 	public static int getFontAlign() {return fontAlign;}
 	public static Vector2f drawString(GraphicsHelper gh, String text, float x, float y) {
 		text = text.replace("\t","    ");
 		String[] lines = text.split("\\n");
 		int[] widths = new int[lines.length];
 		float w = 0, h = 0, hh = gh.g().getFont().getHeight("y");
 		for (int i = 0; i < lines.length; i++) {
 			widths[i] = gh.g().getFont().getWidth(lines[i]);
 			if (w < widths[i]) w = widths[i];
 			h += hh;
 		}
 		
 		float _x, _y = y;
 		if (Helper.equalsOR(fontAlign,BottomLeft,BottomCenter,BottomRight)) _y -= h;
 		if (Helper.equalsOR(fontAlign,MiddleLeft,MiddleCenter,MiddleRight)) _y -= h/2f;
 		
		Float minX = null, minY = _y;
 		
 		for (int i = 0; i < lines.length; i++) {
 			_x = x;
 			if (Helper.equalsOR(fontAlign,TopRight,MiddleRight,BottomRight)) _x -= widths[i];
 			if (Helper.equalsOR(fontAlign,TopCenter,MiddleCenter,BottomCenter)) _x -= widths[i]/2f;
 			
 			gh.g().drawString(lines[i],Math.round(_x),Math.round(_y));
 			_y += hh;
			
			if (minX == null || minX > _x) minX = _x;
 		}
 		
		return new Vector2f(minX,minY);
 	}
 	
 	public static Vector2f getActualStringXY(GraphicsHelper gh, String text, float x, float y) {
 		return getActualStringXY(gh.g().getFont(),text,x,y);
 	}
 	public static Vector2f getActualStringXY(Font font, String text, float x, float y) {
 		text = text.replace("\t","    ");
 		String[] lines = text.split("\\n");
 		int[] widths = new int[lines.length];
 		float w = 0, h = 0, hh = font.getHeight("y");
 		for (int i = 0; i < lines.length; i++) {
 			widths[i] = font.getWidth(lines[i]);
 			if (w < widths[i]) w = widths[i];
 			h += hh;
 		}
 		
		float _x, _y = y;
 		if (Helper.equalsOR(fontAlign,BottomLeft,BottomCenter,BottomRight)) _y -= h;
 		if (Helper.equalsOR(fontAlign,MiddleLeft,MiddleCenter,MiddleRight)) _y -= h/2f;
 		
		Float minX = null, minY = _y;
		
		for (int i = 0; i < lines.length; i++) {
			_x = x;
			if (Helper.equalsOR(fontAlign,TopRight,MiddleRight,BottomRight)) _x -= widths[i];
			if (Helper.equalsOR(fontAlign,TopCenter,MiddleCenter,BottomCenter)) _x -= widths[i]/2f;
			_y += hh;
			
			if (minX == null || minX > _x) minX = _x;
		}
 		
		return new Vector2f(minX,minY);
 	}
 	
 	public static float getFontWidthSubtract(Font font, int fontAlign, String text) {
 		float x = 0, w = font.getWidth(text);
 		if (Helper.equalsOR(fontAlign,TopRight,MiddleRight,BottomRight)) x += w;
 		if (Helper.equalsOR(fontAlign,TopCenter,MiddleCenter,BottomCenter)) x += w/2f;
 		return x;
 	}
 	public static float getFontHeightSubtract(Font font, int fontAlign, String text) {
 		float y = 0, h = font.getHeight(text);
 		if (Helper.equalsOR(fontAlign,BottomLeft,BottomCenter,BottomRight)) y += h;
 		if (Helper.equalsOR(fontAlign,MiddleLeft,MiddleCenter,MiddleRight)) y += h/2f;
 		return y;
 	}
 	
 	public static String getStringWordwrap(GraphicsHelper gh, String text, float maxWidth) {
 		return getStringWordwrap(gh.g().getFont(),text,maxWidth);
 	}
 	public static String getStringWordwrap(Font font, String text, float maxWidth) {
 		ArrayList<String> retl = new ArrayList<String>();
 		String[] lines = text.split("\\n");
 		
 		for (int i = 0; i < lines.length; i++) {
 			String[] words = lines[i].split(" ");
 			for (int i2 = 0; i2 < words.length; i2++) words[i2] = words[i2].replace("\t","    ");
 			StringBuffer sb = new StringBuffer(words[0]);
 			
 			for (int i2 = 1; i2 < words.length; i2++) {
 				if (font.getWidth(sb.toString()+" "+words[i2]) > maxWidth) {
 					retl.add(sb.toString());
 					sb = new StringBuffer(words[i2]);
 				} else sb.append(" "+words[i2]);
 			}
 			retl.add(sb.toString());
 		}
 		
 		StringBuffer sb = new StringBuffer();
 		for (int i = 0; i < retl.size(); i++) {
 			if (i != 0) sb.append("\n");
 			sb.append(retl.get(i));
 		}
 		return sb.toString();
 	}
 }
