 package com.tzapps.common.utils;
 
 import java.util.Comparator;
 
 import android.graphics.Color;
 
 public class ColorUtils
 {
     /**
      * sRGB to XYZ conversion matrix
      */
     public static double[][] M = {{0.4124, 0.3576,  0.1805},
                                   {0.2126, 0.7152,  0.0722},
                                   {0.0193, 0.1192,  0.9505}};
     
     /**
      * XYZ to sRGB conversion matrix
      */
     public static double[][] Mi  = {{ 3.2406, -1.5372, -0.4986},
                                     {-0.9689,  1.8758,  0.0415},
                                     { 0.0557, -0.2040,  1.0570}};
     
     /**
      * reference white in XYZ coordinates
      */
     public static double[] D50 = {96.4212, 100.0, 82.5188};
     public static double[] D55 = {95.6797, 100.0, 92.1481};
     public static double[] D65 = {95.0429, 100.0, 108.8900};
     public static double[] D75 = {94.9722, 100.0, 122.6394};
     public static double[] whitePoint = D65;
     
     public static Comparator<Integer> colorSorter = new Comparator<Integer>(){
         @Override
         public int compare(Integer lColor, Integer rColor)
         {
             int[] leftHSL  = ColorUtils.colorToHSL(lColor);
             int[] rightHSL = ColorUtils.colorToHSL(rColor);
 
             if (leftHSL[2] != rightHSL[2])
             {
                 // sort color by its lightness first
                 // put the darker color at top
                 return leftHSL[2] - rightHSL[2];
             }
             else if (leftHSL[0] != rightHSL[0])
             {
                 // then its hue value
                 return leftHSL[0] - rightHSL[0];
             }
             else if (leftHSL[1] != rightHSL[1])
             {
                 // then its saturation
                 return leftHSL[1] - rightHSL[1];
             }
             else
             {
                 return 0;
             }
         }
     };
     
     /**
      *  Convert RGB Color to a HTML string (#RRGGBB).
      *
      *  @param color the RGB color
      *  @return the HTML string
      */
     public static String colorToHtml(int color)
     {
         return String.format("#%06X", (0xFFFFFF & color));
     }
     
     /**
      *  Convert RGB Color to a RGB values.
      *  
      *  <li>R (Red) is specified as degrees in the range 0 - 225. </li>
      *  <li>G (Green) is specified as a percentage in the range 0 - 255.</li>
      *  <li>B (Blue) is specified as a percentage in the range 0 - 255.</li>
      *
      *  @param color the RGB color
      *  @return the RGB values
      */
     public static int[] colorToRGB(int color)
     {
         int[] rgb = new int[3];
         
         rgb[0] = Color.red(color);
         rgb[1] = Color.green(color);
         rgb[2] = Color.blue(color);
         
         return rgb;
     }
     
     /**
      *  Convert RGB Color to a HSV values. 
      *  
      *  <li>H (Hue) is specified as degrees in the range 0 - 360.</li>
      *  <li>S (Saturation) is specified as a percentage in the range 1 - 100.</li>
      *  <li>V (Value) is specified as a percentage in the range 1 - 100.</li>
      *
      *  @param color the RGB color
      *  @return the HSV values
      */
     public static int[] colorToHSV(int color)
     {
         int[] hsv = new int[3];
         float[] hsv_float = new float[3];
         
         Color.colorToHSV(color, hsv_float);
         
         hsv[0] = (int)Math.round(hsv_float[0]);
         hsv[1] = (int)Math.round(hsv_float[1] * 100);
         hsv[2] = (int)Math.round(hsv_float[2] * 100);
         
         hsv[0] = (int)(hsv_float[0]);
         hsv[1] = (int)(hsv_float[1] * 100);
         hsv[2] = (int)(hsv_float[2] * 100);
         
         return hsv;
     }
     
     /**
      *  Convert RGB Color to a HSL values.
      *  
      *  <li>H (Hue) is specified as degrees in the range 0 - 360.</li>
      *  <li>S (Saturation) is specified as a percentage in the range 1 - 100.</li>
      *  <li>L (Lumanance) is specified as a percentage in the range 1 - 100.</li>
      *
      *  @param color the RGB color
      *  @return the HSL values
      */
     public static int[] colorToHSL(int color)
     {
         int[] hsl = new int[3];
         float[] hsl_float = hslFromRGB(color);
         
         hsl[0] = (int)Math.round(hsl_float[0]);
         hsl[1] = (int)Math.round(hsl_float[1]);
         hsl[2] = (int)Math.round(hsl_float[2]);
         
         return hsl;
     }
     
     /**
      *  Convert RGB Color to a Lab values.
      *  
      *  <br/>see wiki page http://en.wikipedia.org/wiki/Lab_color_space for Lab details
      *  
      *  <li>L (Lightness) is specified as a percentage in the range 0 - 100.</li>
      *  <li>a (color-opponent dimensions) is specified in the range -128 - 127.</li>
      *  <li>b (color-opponent dimensions) is specified in the range -128 - 127.</li>
      *
      *  @param color the RGB color
      *  @return the HSL values
      */
     public static int[] colorToLAB(int color)
     {
         int[] rgb = colorToRGB(color);
         int[] lab = new int[3];
         
         double[] lab_double = XYZtoLAB(RGBtoXYZ(rgb));
         
         lab[0] = (int)Math.round(lab_double[0]);
         lab[1] = (int)Math.round(lab_double[1]);
         lab[2] = (int)Math.round(lab_double[2]);
         
         return lab;
     }
     
     /**
      *  Convert RGB Color to a CMYK values.
      *  
      *  <li>C (Cyan) is specified as a percentage in the range 0 - 100.</li>
      *  <li>M (Magenta) is specified as a percentage in the range 0 - 100.</li>
      *  <li>Y (Yellow) is specified as a percentage in the range 0 - 100.</li>
      *  <li>K (Black) is specified as a percentage in the range 0 - 100.</li>
      *
      *  @param color the RGB color
      *  @return the CMYK values
      */
     public static int[] colorToCMYK(int color)
     {
         int[] rgb = colorToRGB(color);
         int[] cmyk = new int[4];
         
         rgb2cmyk(rgb[0], rgb[1], rgb[2], cmyk);
         
         return cmyk;
     }
     
     public static int hsvToColor(int[] hsv)
     {
         assert(hsv.length == 3);
         
         float[] hsv_float = new float[3];
         
         hsv_float[0] = hsv[0];
         hsv_float[1] = (float)hsv[1] / 100;
         hsv_float[2] = (float)hsv[2] / 100;
         
         return Color.HSVToColor(hsv_float);
     }
 
     public static int rgbToColor(int[] rgb)
     {
         assert(rgb.length == 3);
         
         return Color.rgb(rgb[0], rgb[1], rgb[2]);
     }
     
     public static int rgbToColor(int R, int G, int B)
     {
         return Color.rgb(R, G, B);
     }
     
     public static int hsvToColor(int h, int s, int v)
     {
         return Color.HSVToColor(new float[]{h,(float)s/100,(float)v/100});
     }
     
     /**
      *  Convert HSL values to a RGB Color with a default alpha value of 1.
      *  
      *  <li>H (Hue) is specified as degrees in the range 0 - 360.</li>
      *  <li>S (Saturation) is specified as a percentage in the range 0 - 100.</li>
      *  <li>L (Lumanance) is specified as a percentage in the range 0 - 100.</li>
      *
      *  @param hsl an array containing the 3 HSL values
      *  @return color int in rgb value
      */
     public static int hslToColor(int[] hsl)
     {
         return hslToRGB(hsl[0], hsl[1], hsl[2]);
     }
     
     /**
      *  Convert HSL values to a RGB Color with a default alpha value of 1.
      *  
      *  <li>H (Hue) is specified as degrees in the range 0 - 360.</li>
      *  <li>S (Saturation) is specified as a percentage in the range 0 - 100.</li>
      *  <li>L (Lumanance) is specified as a percentage in the range 0 - 100.</li>
      *
      *  @param h  Hue
      *  @param s  Saturation
      *  @param l  Lumanance
      *  @return color int in rgb value
      */
     public static int hslToColor(int h, int s, int l)
     {
         return hslToRGB(h,s,l);
     }
     
     public static int labToColor(int[] lab)
     {
         return labToColor(lab[0], lab[1], lab[2]);
     }
     
     private static int labToColor(int L, int a, int b)
     {
         int[] rgb = LABtoRGB(L, a, b);
         
         return Color.rgb(rgb[0], rgb[1], rgb[2]);
     }
     
     /**
      *  Convert a RGB Color to it corresponding HSL values.
      *
      *  @return an array containing the 3 HSL values.
      */
     public static float[] hslFromRGB(int color)
     {
         //  Get RGB values in the range 0 - 1
         float r = (float)Color.red(color) / 255.0f;
         float g = (float)Color.green(color) / 255.0f;
         float b = (float)Color.blue(color) / 255.0f;
 
         //  Minimum and Maximum RGB values are used in the HSL calculations
         float min = Math.min(r, Math.min(g, b));
         float max = Math.max(r, Math.max(g, b));
 
         //  Calculate the Hue
         float h = 0;
 
         if (max == min)
             h = 0;
         else if (max == r)
             h = ((60 * (g - b) / (max - min)) + 360) % 360;
         else if (max == g)
             h = (60 * (b - r) / (max - min)) + 120;
         else if (max == b)
             h = (60 * (r - g) / (max - min)) + 240;
 
         //  Calculate the Luminance
         float l = (max + min) / 2;
 
         //  Calculate the Saturation
         float s = 0;
 
         if (max == min)
             s = 0;
         else if (l <= .5f)
             s = (max - min) / (max + min);
         else
             s = (max - min) / (2 - max - min);
 
         return new float[] {h, s * 100, l * 100};
     }
     
     /**
      *  Convert HSL values to a RGB Color.
      *
      *  @param h Hue is specified as degrees in the range 0 - 360.
      *  @param s Saturation is specified as a percentage in the range 1 - 100.
      *  @param l Lumanance is specified as a percentage in the range 1 - 100.
      */
     private static int hslToRGB(int hInt, int sInt, int lInt)
     {
         float h, s, l;
         
         h = (float)hInt;
         s = (float)sInt;
         l = (float)lInt;
         
         if (s < 0.0f || s > 100.0f)
         {
             String message = "Color parameter outside of expected range - Saturation";
             throw new IllegalArgumentException( message );
         }
 
         if (l < 0.0f || l > 100.0f)
         {
             String message = "Color parameter outside of expected range - Luminance";
             throw new IllegalArgumentException( message );
         }
 
         //  Formula needs all values between 0 - 1.
 
         h = h % 360.0f;
         h /= 360f;
         s /= 100f;
         l /= 100f;
 
         float q = 0;
 
         if (l < 0.5)
             q = l * (1 + s);
         else
             q = (l + s) - (s * l);
 
         float p = 2 * l - q;
 
         int r = (int) (Math.max(0, HUEtoRGB(p, q, h + (1.0f / 3.0f))) * 255);
         int g = (int) (Math.max(0, HUEtoRGB(p, q, h)) * 255);
         int b = (int) (Math.max(0, HUEtoRGB(p, q, h - (1.0f / 3.0f))) * 255);
 
         return Color.rgb(r, g, b);
     }
     
     private static float HUEtoRGB(float p, float q, float h)
     {
         if (h < 0) h += 1;
 
         if (h > 1 ) h -= 1;
 
         if (6 * h < 1)
         {
             return p + ((q - p) * 6 * h);
         }
 
         if (2 * h < 1 )
         {
             return  q;
         }
 
         if (3 * h < 2)
         {
             return p + ( (q - p) * 6 * ((2.0f / 3.0f) - h) );
         }
 
         return p;
     }
     
     /**
      * Convert RGB to XYZ
      * 
      * Convert equation and source code is from ImageJ's plugin Color Space Converter
      * http://rsbweb.nih.gov/ij/plugins/download/Color_Space_Converter.java
      * 
      * @param R
      * @param G
      * @param B
      * @return XYZ in double array.
      */
     public static double[] RGBtoXYZ(int R, int G, int B) 
     {
       double[] result = new double[3];
 
       // convert 0..255 into 0..1
       double r = R / 255.0;
       double g = G / 255.0;
       double b = B / 255.0;
 
       // assume sRGB
       if (r <= 0.04045) 
       {
         r = r / 12.92;
       }
       else 
       {
         r = Math.pow(((r + 0.055) / 1.055), 2.4);
       }
       
       if (g <= 0.04045) 
       {
         g = g / 12.92;
       }
       else 
       {
         g = Math.pow(((g + 0.055) / 1.055), 2.4);
       }
       
       if (b <= 0.04045) 
       {
         b = b / 12.92;
       }
       else 
       {
         b = Math.pow(((b + 0.055) / 1.055), 2.4);
       }
 
       r *= 100.0;
       g *= 100.0;
       b *= 100.0;
 
       // [X Y Z] = [r g b][M]
       result[0] = (r * M[0][0]) + (g * M[0][1]) + (b * M[0][2]);
       result[1] = (r * M[1][0]) + (g * M[1][1]) + (b * M[1][2]);
       result[2] = (r * M[2][0]) + (g * M[2][1]) + (b * M[2][2]);
 
       return result;
     }
 
     /**
      * Convert RGB to XYZ
      * 
      * Convert equation and source code is from ImageJ's plugin Color Space Converter
      * http://rsbweb.nih.gov/ij/plugins/download/Color_Space_Converter.java
      * 
      * @param RGB
      * @return XYZ in double array.
      */
     private static double[] RGBtoXYZ(int[] RGB) 
     {
       return RGBtoXYZ(RGB[0], RGB[1], RGB[2]);
     }
 
     /**
      * Convert XYZ to LAB.
      * 
      * Convert equation and source code is from ImageJ's plugin Color Space Converter
      * http://rsbweb.nih.gov/ij/plugins/download/Color_Space_Converter.java
      * 
      * @param X
      * @param Y
      * @param Z
      * @return Lab values
      */
     private static double[] XYZtoLAB(double X, double Y, double Z) 
     {
         
         double x = X / whitePoint[0];
         double y = Y / whitePoint[1];
         double z = Z / whitePoint[2];
     
         if (x > 0.008856) 
         {
             x = Math.pow(x, 1.0 / 3.0);
         }
         else 
         {
             x = (7.787 * x) + (16.0 / 116.0);
         }
   
         if (y > 0.008856) 
         {
             y = Math.pow(y, 1.0 / 3.0);
         }
         else 
         {
             y = (7.787 * y) + (16.0 / 116.0);
         }
   
         if (z > 0.008856) 
         {
             z = Math.pow(z, 1.0 / 3.0);
         }
         else 
         {
             z = (7.787 * z) + (16.0 / 116.0);
         }
 
         double[] result = new double[3];
 
         result[0] = (116.0 * y) - 16.0;
         result[1] = 500.0 * (x - y);
         result[2] = 200.0 * (y - z);
 
         return result;
     }
 
     /**
      * Convert XYZ to LAB.
      * 
      * Convert equation and source code is from ImageJ's plugin Color Space Converter
      * http://rsbweb.nih.gov/ij/plugins/download/Color_Space_Converter.java
      * 
      * @param XYZ
      * @return Lab values
      */
     private static double[] XYZtoLAB(double[] xyz) 
     {
         return XYZtoLAB(xyz[0], xyz[1], xyz[2]);
     }
     
     /**
      * Convert LAB to RGB.
      * @param L
      * @param a
      * @param b
      * @return RGB values
      */
     private static int[] LABtoRGB(double L, double a, double b) 
     {
         return XYZtoRGB(LABtoXYZ(L, a, b));
     }
     
     /**
      * @param Lab
      * @return RGB values
      */
     private static int[] LABtoRGB(double[] Lab)
     {
         return XYZtoRGB(LABtoXYZ(Lab));
     }
     
     /**
      * Convert LAB to XYZ.
      *
      * Convert equation and source code is from ImageJ's plugin Color Space Converter
      * http://rsbweb.nih.gov/ij/plugins/download/Color_Space_Converter.java
      * 
      * @param L
      * @param a
      * @param b
      * @return XYZ values
      */
     private static double[] LABtoXYZ(double L, double a, double b) 
     {
         double[] result = new double[3];
 
         double y = (L + 16.0) / 116.0;
         double y3 = Math.pow(y, 3.0);
         double x = (a / 500.0) + y;
         double x3 = Math.pow(x, 3.0);
         double z = y - (b / 200.0);
         double z3 = Math.pow(z, 3.0);
 
         if (y3 > 0.008856) 
         {
             y = y3;
         }
         else 
         {
             y = (y - (16.0 / 116.0)) / 7.787;
         }
         
         if (x3 > 0.008856) 
         {
             x = x3;
         }
         else 
         {
             x = (x - (16.0 / 116.0)) / 7.787;
         }
         
         if (z3 > 0.008856) 
         {
             z = z3;
         }
         else 
         {
             z = (z - (16.0 / 116.0)) / 7.787;
         }
 
         result[0] = x * whitePoint[0];
         result[1] = y * whitePoint[1];
         result[2] = z * whitePoint[2];
 
         return result;
     }
     
     /**
      * Convert XYZ to RGB.
      *
      * Convert equation and source code is from ImageJ's plugin Color Space Converter
      * http://rsbweb.nih.gov/ij/plugins/download/Color_Space_Converter.java
      * 
      * @param X
      * @param Y
      * @param Z
      * @return RGB in int array.
      */
     private static int[] XYZtoRGB(double X, double Y, double Z) 
     {
         int[] result = new int[3];
 
         double x = X / 100.0;
         double y = Y / 100.0;
         double z = Z / 100.0;
 
         // [r g b] = [X Y Z][Mi]
         double r = (x * Mi[0][0]) + (y * Mi[0][1]) + (z * Mi[0][2]);
         double g = (x * Mi[1][0]) + (y * Mi[1][1]) + (z * Mi[1][2]);
         double b = (x * Mi[2][0]) + (y * Mi[2][1]) + (z * Mi[2][2]);
 
         // assume sRGB
         if (r > 0.0031308) 
         {
             r = ((1.055 * Math.pow(r, 1.0 / 2.4)) - 0.055);
         }
         else 
         {
             r = (r * 12.92);
         }
         
         if (g > 0.0031308) 
         {
             g = ((1.055 * Math.pow(g, 1.0 / 2.4)) - 0.055);
         }
         else 
         {
            g = (g * 12.92);
         }
         
         if (b > 0.0031308) 
         {
             b = ((1.055 * Math.pow(b, 1.0 / 2.4)) - 0.055);
         }
         else 
         {
             b = (b * 12.92);
         }
 
         r = (r < 0) ? 0 : r;
         g = (g < 0) ? 0 : g;
         b = (b < 0) ? 0 : b;
 
         // convert 0..1 into 0..255
         result[0] = (int) Math.round(r * 255);
         result[1] = (int) Math.round(g * 255);
         result[2] = (int) Math.round(b * 255);
         
         return result;
     }
 
     /**
      * Convert XYZ to RGB
      * @param XYZ in a double array.
      * @return RGB in int array.
      */
     private static int[] XYZtoRGB(double[] XYZ) 
     {
         return XYZtoRGB(XYZ[0], XYZ[1], XYZ[2]);
     }
     
     /**
      * Convert LAB to XYZ.
      * @param Lab
      * @return XYZ values
      */
     private static double[] LABtoXYZ(double[] Lab) 
     {
         return LABtoXYZ(Lab[0], Lab[1], Lab[2]);
     }
 
     private static void rgb2cmyk(int R, int G, int B, int []cmyk)
     {
         float C = 1 - (float)R / 255;
         float M = 1 - (float)G / 255;
         float Y = 1 - (float)B / 255;
         float K = 1;
         
         if (C < K)
             K = C;
         if (M < K)
             K = M;
         if (Y < K)
             K = Y;
         
        if (K == 1)
         {
             //pure black
             C = 0;
             M = 0;
             Y = 0;
         }
         else
         {
             C = (C - K) / (1 - K);
             M = (M - K) / (1 - K);
             Y = (Y - K) / (1 - K);
         }
         
         cmyk[0] = (int)Math.round(C * 100);
         cmyk[1] = (int)Math.round(M * 100);
         cmyk[2] = (int)Math.round(Y * 100);
         cmyk[3] = (int)Math.round(K * 100);
     }
 }
