 package com.id3alsolutions.domain;
 
 import java.text.DecimalFormat;
 
 public class Measurement {
 	public static int INCHES_PER_FOOT = 12;
 	private Double valueInInches = 0.0;
 
 	public Measurement() {
 
 	}
 
 	public Measurement(double valueInInches) {
 		this.valueInInches = valueInInches;
 	}
 
 	public Measurement(int valueInFeet) {
 		this.valueInInches = new Double(valueInFeet / INCHES_PER_FOOT);
 	}
 
 	/**
 	 * @return the valueInInches
 	 */
 	public double getValueInInches() {
 		return valueInInches;
 	}
 
 	/**
 	 * @param valueInInches
 	 *            the valueInInches to set
 	 */
 	public void setValueInInches(double valueInInches) {
 		this.valueInInches = valueInInches;
 	}
 
 	public int getValueInFeet() {
		return valueInInches.intValue() * INCHES_PER_FOOT;
 	}
 
 	public Double getValueInInches(int precision) {
 		String format = "#.";
 		for (int decimals = 0; decimals < precision; decimals++)
 			format += "#";
 		DecimalFormat twoDForm = new DecimalFormat(format);
 		return Double.valueOf(twoDForm.format(valueInInches));
 	}
 }
