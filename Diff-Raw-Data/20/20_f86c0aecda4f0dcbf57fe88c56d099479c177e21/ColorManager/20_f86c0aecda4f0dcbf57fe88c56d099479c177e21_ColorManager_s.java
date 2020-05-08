 package com.example.lightdetector;
 
 import java.util.HashMap;
 
 import org.opencv.core.Scalar;
 
 import android.graphics.Color;
 
 public class ColorManager {
 	public static double[] BLACK    = { 0.0, 0.0, 0.0, 255.0 };
 	public static double[] RED      = { 255.0, 0.0, 0.0, 255.0 };
 	public static double[] GREEN    = { 0.0, 255.0, 0.0, 255.0 };
 	public static double[] BLUE     = { 0.0, 0.0, 255.0, 255.0 };
 	public static double[] ORANGE   = { 255.0, 165.0, 0.0, 255.0 };
 	public static double[] WHITE    = { 255.0, 255.0, 255.0, 255.0 };
 	public static double[] MAGENTA  = { 202.0, 31.0, 123.0, 255.0 };
 	public static double[] DARK_RED  = { 102.0, 0.0, 0.0, 255.0 };
	public static double[] DARK_GREEN  = { 0.0, 0.0, 102.0, 255.0 };
 
 
 	public static Scalar getCvColor(double[] color) {
 		return new Scalar(color[0], color[1], color[2], color[3]);
 	}
 	
 	public static Scalar getCvColor(String hexa) {
 		return getCvColor(getColor(hexa));
 	}
 
 	public static String getHexColor(double[] color) {
 		String f = "#" + String.format("%02X", (int) color[0]);
 		String s = "" + String.format("%02X", (int) color[1]);
 		String t = "" + String.format("%02X", (int) color[2]);
 
 		return f + s + t;
 	}
 	
 	public static String getHexColor(int hexa) {
 		return getHexColor(getColor(hexa));
 	}
 	
 	public static double[] getColor(String hexa) {
 		double[] color = new double[4];
 		color[0] = Integer.parseInt(hexa.substring(1, 3), 16);
 		color[1] = Integer.parseInt(hexa.substring(3, 5), 16);
 		color[2] = Integer.parseInt(hexa.substring(5, 7), 16);
 		color[3] = 255.0;
 		return color;
 	}
 	
 	public static double[] getColor(int hexa) { // hexa int returned from Bitmap
 		double[] colorArray = { Color.red(hexa), Color.green(hexa), Color.blue(hexa) };
 		return colorArray;
 	}
 	
 	
 }
