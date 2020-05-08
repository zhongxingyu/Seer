 package net.stuffrepos.tactics16.util.image;
 
 import org.newdawn.slick.Color;
 
 /**
  *
  * @author Eduardo H. Bogoni <eduardobogoni@gmail.com>
  */
 public class ColorUtil {
 
     /**
      * Atalho para applyFactor(color, 0.5f).
      * @param color
      * @return
      */
     public static Color dark(Color color) {
         return applyFactor(color, 0.5f);
     }
 
     /**
      * Atalho para applyFactor(color, 1.5f).
      * @param color
      * @return
      */
     public static Color light(Color color) {
         return applyFactor(color, 1.5f);
     }
 
     public static Color grayScale(Color color) {
         int c = Math.max(color.getRed(), Math.max(color.getGreen(), color.getBlue()));
         return new Color(c, c, c);
     }
 
     public static Color getBetweenColor(Color beginColor, Color endColor, float factor) {
         return new Color(
                 limitColor((endColor.getRed() - beginColor.getRed()) * factor + beginColor.getRed()),
                 limitColor((endColor.getGreen() - beginColor.getGreen()) * factor + beginColor.getGreen()),
                 limitColor((endColor.getBlue() - beginColor.getBlue()) * factor + beginColor.getBlue()));
     }
 
     public static Color applyFactor(Color color, float factor) {
         if (color.getAlpha() > 0) {
             return new Color(
                     limitColor(color.getRed() * factor),
                     limitColor(color.getGreen() * factor),
                     limitColor(color.getBlue() * factor),
                     limitColor(color.getAlpha() * factor));
         } else {
            return new Color(0, 0, 0, 0);
         }
 
 
     }
 
     public static int getRgbBitmask(int rgba) {
         Color color = new Color(rgba);
 
         if (color.getAlpha() < 0x100 / 2) {
             return 0x00FFFFFF & rgba;
         } else {
             return 0xFF000000 | rgba;
         }
     }
 
     public static Color getColorBitmask(Color color) {
         return byRgba(getRgbBitmask(rgba(color)));
     }
 
     private static int limitColor(float color) {
         return limitColor((int) color);
     }
 
     private static int limitColor(int color) {
         color = Math.abs(color);
         if (color < 0) {
             return color;
         } else if (color > 0xFF) {
             return 0xFF;
         } else {
             return color;
         }
     }
 
     public static Color transparent(Color color, float alpha) {
         return new Color(color.getRed(), color.getGreen(), color.getBlue(), (int) (0xFF * alpha));
     }
 
     public static Color getBetweenColor(int min, int max, float factor) {
         return getBetweenColor(new Color(min), new Color(max), factor);
     }
 
     public static float getBetweenFactor(int playerColorMin, int playerColorMax, int originalRgb) {
         return getBetweenFactor(
                 new Color(playerColorMin),
                 new Color(playerColorMax),
                 new Color(originalRgb));
     }
 
     public static float getBetweenFactor(Color min, Color max, Color color) {
         //System.out.printf("Min: %s, Max: %s, Color: %s\n", min, max, color);
         return getColorComponentBetweenFactor(
                 sumColorComponents(min),
                 sumColorComponents(max),
                 sumColorComponents(color));
     }
 
     private static int sumColorComponents(Color color) {
         return color.getRed() + color.getGreen() + color.getBlue();
     }
 
     private static float getColorComponentBetweenFactor(int min, int max, int component) {
         //System.out.printf("Min: %d, Max: %d, Component: %s\n", min, max, component);
         return (float) (component - min) / (float) (max - min);
     }
 
     public static int compareColor(int rgb1, int rgb2) {
         return compareColor(new Color(rgb1), new Color(rgb2));
     }
 
     public static int compareColor(Color color1, Color color2) {
         return new Integer(sumColorComponents(color1)).compareTo(sumColorComponents(color2));
     }
 
     public static int rgba(Color color) {
         return (color.getAlpha() << 24)
                 + (color.getRed() << 16)
                 + (color.getGreen() << 8)
                 + color.getBlue();
     }
 
     public static int getAlpha(int rgba) {
         return (rgba & 0xFF000000) >> 24;
     }
 
     public static int getRed(int rgba) {
         return (rgba & 0x00FF0000) >> 16;
     }
 
     public static int getGreen(int rgba) {
         return (rgba & 0x0000FF00) >> 8;
     }
 
     public static int getBlue(int rgba) {
         return (rgba & 0x000000FF);
     }
 
     public static Color byRgba(int rgba) {
         return new Color(getRed(rgba), getGreen(rgba), getBlue(rgba), getAlpha(rgba));
     }
 
     public static Color opaque(Color c) {
         return new Color(c.getRed(), c.getGreen(), c.getBlue());
     }
 }
