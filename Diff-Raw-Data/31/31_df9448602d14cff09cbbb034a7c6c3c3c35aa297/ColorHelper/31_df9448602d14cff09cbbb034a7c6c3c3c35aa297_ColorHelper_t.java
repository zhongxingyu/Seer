 /*
  * Jaffas and more!
  * author: monnef
  */
 
 package monnef.core.utils;
 
 public class ColorHelper {
     public static int getInt(IntColor color) {
         return getInt(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
     }
 
     public static int getInt(int red, int green, int blue) {
        return getInt(red, green, blue, 255);
     }
 
     public static int getInt(int red, int green, int blue, int alpha) {
         red = clampByte(red);
         green = clampByte(green);
         blue = clampByte(blue);
         alpha = clampByte(alpha);
         return (alpha << 24) + (red << 16) + (green << 8) + blue;
     }
 
     public static int clampByte(int input) {
         if (input < 0) return 0;
         if (input > 255) return 255;
         return input;
     }
 
     public static IntColor getColor(int color) {
         int alpha = (color >> 24) & 0xFF;
         int red = (color >> 16) & 0xFF;
         int green = (color >> 8) & 0xFF;
         int blue = color & 0xFF;
         return new IntColor(red, green, blue, alpha);
     }
 
     public static int addBrightness(int inputColor, int amount) {
         IntColor c = getColor(inputColor);
         c.setRed(c.getRed() + amount);
         c.setGreen(c.getGreen() + amount);
         c.setBlue(c.getBlue() + amount);
         return getInt(c);
     }
 
     public static int addContrast(int inputColor, float modifier) {
         IntColor c = getColor(inputColor);
         c.setRed(Math.round(c.getRed() * modifier));
         c.setGreen(Math.round(c.getGreen() * modifier));
         c.setBlue(Math.round(c.getBlue() * modifier));
         return getInt(c);
     }
 
     public static class IntColor {
         public static final IntColor BLACK = new IntColor(0, 0, 0);
 
         private int red;
         private int green;
         private int blue;
         private int alpha;
         private boolean dirty = true;
         private int cachedIntValue;
 
         public IntColor() {
         }
 
         public IntColor(int red, int green, int blue) {
             this(red, green, blue, (short) 255);
         }
 
         public IntColor(int red, int green, int blue, int alpha) {
             this.setRed(red);
             this.setGreen(green);
             this.setBlue(blue);
             this.setAlpha(alpha);
         }
 
         public float getFloatRed() {
             return toFloat(getRed());
         }
 
         public float getFloatGreen() {
             return toFloat(getGreen());
         }
 
         public float getFloatBlue() {
             return toFloat(getBlue());
         }
 
         public float getFloatAlpha() {
             return toFloat(getAlpha());
         }
 
         private float toFloat(int value) {
             return value / 255f;
         }
 
         public int toInt() {
             if (dirty) {
                 cachedIntValue = ColorHelper.getInt(this);
             }
             return cachedIntValue;
         }
 
         public int getRed() {
             return red;
         }
 
         public void setRed(int red) {
             dirty = true;
             this.red = red;
         }
 
         public int getGreen() {
             return green;
         }
 
         public void setGreen(int green) {
             dirty = true;
             this.green = green;
         }
 
         public int getBlue() {
             return blue;
         }
 
         public void setBlue(int blue) {
             dirty = true;
             this.blue = blue;
         }
 
         public int getAlpha() {
             return alpha;
         }
 
         public void setAlpha(int alpha) {
             dirty = true;
             this.alpha = alpha;
         }
     }
 }
