 package com.eagerlogic.cubee.client.styles;
 
 /**
  *
  * @author dipacs
  */
 public final class Color {
 	
 	public static final Color BLACK = new Color(0xff000000);
 	public static final Color WHITE = new Color(0xffffffff);
 	public static final Color TRANSPARENT = new Color(0x00000000);
 	
 	public static Color getArgbColor(int argb) {
 		return new Color(argb);
 	}
 	
 	public static Color getArgbColor(int alpha, int red, int green, int blue) {
 		alpha = fixComponent(alpha);
 		red = fixComponent(red);
 		green = fixComponent(green);
 		blue = fixComponent(blue);
 		
 		return getArgbColor(
 				alpha << 24
 				| red << 16
 				| green << 8
 				| blue
 				);
 	}
 	
 	public static Color getRgbColor(int rgb) {
 		return getArgbColor(rgb | 0xff000000);
 	}
 	
 	public static Color getRgbColor(int red, int green, int blue) {
 		return getArgbColor(255, red, green, blue);
 	} 
 	
 	private static int fixComponent(int component) {
 		if (component < 0) {
 			return 0;
 		}
 		
 		if (component > 255) {
 			return 255;
 		}
 		
 		return component;
 	}
 	
 	public static Color fadeColors(Color startColor, Color endColor, double fadePosition) {
 		return Color.getArgbColor(
 				mixComponent(startColor.getAlpha(), endColor.getAlpha(), fadePosition), 
 				mixComponent(startColor.getRed(), endColor.getRed(), fadePosition), 
 				mixComponent(startColor.getGreen(), endColor.getGreen(), fadePosition), 
 				mixComponent(startColor.getBlue(), endColor.getBlue(), fadePosition)
 				);
 	}
 	
 	private static int mixComponent(int startValue, int endValue, double pos) {
 		int res = (int) (startValue + ((endValue - startValue) * pos));
 		res = fixComponent(res);
 		return res;
 	}
 	
 	
 	
 	
 	private final int argb;
 	
 	
 	
 	
 	public Color(int argb) {
 		this.argb = argb;
 	}
 
 	
 	
 	
 	public int getArgb() {
 		return argb;
 	}
 	
 	public final int getAlpha() {
 		return (argb >>> 24) & 0xff;
 	}
 	
 	public final int getRed() {
 		return (argb >>> 16) & 0xff;
 	}
 	
 	public final int getGreen() {
 		return (argb >>> 8) & 0xff;
 	}
 	
 	public final int getBlue() {
 		return argb & 0xff;
 	}
 	
 	public final Color fade(Color fadeColor, double fadePosition) {
 		return Color.fadeColors(this, fadeColor, fadePosition);
 	}
 	
 	public String toCSS() {
		return "rgba(" + getRed() + ", " + getGreen() + ", " + getBlue() + ", " + (getAlpha() / 255.0);
 	}
 
 }
