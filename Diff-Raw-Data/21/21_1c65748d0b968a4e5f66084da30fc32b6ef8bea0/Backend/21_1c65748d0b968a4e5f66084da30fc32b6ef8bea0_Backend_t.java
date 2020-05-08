 package com.github.mahmoudhossam.height;
 
 public class Backend {
 
 	private static final double CMS_IN_A_FOOT = 30.48;
	private static final double CMS_IN_AN_INCH = 2.54;
 
 	public static double getCentimeters(double feet, double inches) {
 		return (feet * CMS_IN_A_FOOT) + (inches * CMS_IN_AN_INCH);
 	}
 
 	public static int[] getFeetAndInches(double centimeters) {
 		int feet = (int) (centimeters / CMS_IN_A_FOOT);
 		int inches = (int) Math.round((centimeters % CMS_IN_A_FOOT)
				/ CMS_IN_AN_INCH);
 		return new int[] { feet, inches };
 	}
 
 }
