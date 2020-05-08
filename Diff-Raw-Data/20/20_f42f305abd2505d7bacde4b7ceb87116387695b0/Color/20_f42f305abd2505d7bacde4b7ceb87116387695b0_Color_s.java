 /*
  *  Copyright 2012 Axel Winkler, Daniel Dunér
  * 
  *  This file is part of Daxplore Presenter.
  *
  *  Daxplore Presenter is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU Lesser General Public License as published by
  *  the Free Software Foundation, either version 2.1 of the License, or
  *  (at your option) any later version.
  *
  *  Daxplore Presenter is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU Lesser General Public License for more details.
  *
  *  You should have received a copy of the GNU Lesser General Public License
  *  along with Daxplore Presenter.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.daxplore.presenter.shared;
 
 /**
  * A class that represents a color.
  * 
  *  <p>Supports RGB, HSV and HSL color formats.</p>
  *  
  *  <p>Suitable for use in GWT applications and in standard Java.</p>
  */
 public class Color {
 
 	/**
	 * An enum represeting different color models.
 	 */
 	public enum Model {
 		RGB, HSV, HSL
 	}
 
 	private int r, g, b;
 	private double hslh, hsls, hsll;
 	private double hsvh, hsvs, hsvv;
 
 	/**
 	 * Instantiates a new color from RGB values.
 	 * 
 	 * @param r
 	 *            the red part of the RGB representation
 	 * @param g
 	 *            the green part of the RGB representation
 	 * @param b
 	 *            the blue part of the RGB representation
 	 */
 	public Color(int r, int g, int b) {
 		this.r = r;
 		this.g = g;
 		this.b = b;
 		calcHSL();
 		calcHSV();
 	}
 
 	/**
 	 * Instantiates a new color from HSL or HSV values.
 	 * 
 	 * @param h
 	 *            the hue
 	 * @param s
 	 *            the saturation
 	 * @param lightnessOrValue
 	 *            the lightness (if HSL) or value (if HSV) 
 	 * @param model
 	 *            the color model used to interpret the lightnessOrValue argument
 	 */
 	public Color(double h, double s, double lightnessOrValue, Model model) {
 		double r = 0, g = 0, b = 0;
 		
 		h = h%1;
 		
 		if (s < 0) {
 			s = 0;
 		} else if (s > 1) {
 			s = 1.0;
 		}
 		if (lightnessOrValue < 0) {
 			lightnessOrValue = 0.0;
 		} else if (lightnessOrValue > 1) {
 			lightnessOrValue = 1.0;
 		}
 		
 		switch (model) {
 		case HSL:
 			hslh = h;
 			hsls = s;
 			hsll = lightnessOrValue;
 
 			// Code based on math from http://en.wikipedia.org/wiki/HSL_and_HSV#From_HSL
 			double C = (1 - Math.abs(2 * lightnessOrValue - 1)) * s;
 			double hprim = 360 * h / 60;
 			double X = C * (1 - Math.abs(hprim % 2 - 1));
 			if (hprim < 1) {
 				r = C;
 				g = X;
 				b = 0;
 			} else if (hprim < 2) {
 				r = X;
 				g = C;
 				b = 0;
 			} else if (hprim < 3) {
 				r = 0;
 				g = C;
 				b = X;
 			} else if (hprim < 4) {
 				r = 0;
 				g = X;
 				b = C;
 			} else if (hprim < 5) {
 				r = X;
 				g = 0;
 				b = C;
 			} else if (hprim < 6) {
 				r = C;
 				g = 0;
 				b = X;
 			}
 			double m = lightnessOrValue - 0.5 * C;
 			r += m;
 			g += m;
 			b += m;
 
 			this.r = (int) Math.round(r * 255);
 			this.g = (int) Math.round(g * 255);
 			this.b = (int) Math.round(b * 255);
 			break;
 		case HSV:
 			hsvh = h;
 			hsvs = s;
 			hsvv = lightnessOrValue;
 			int i = (int) Math.round(Math.floor(h * 6));
 			double f = h * 6 - i;
 			double p = lightnessOrValue * (1 - s);
 			double q = lightnessOrValue * (1 - f * s);
 			double t = lightnessOrValue * (1 - (1 - f) * s);
 
 			switch (i % 6) {
 			case 0:
 				r = lightnessOrValue;
 				g = t;
 				b = p;
 				break;
 			case 1:
 				r = q;
 				g = lightnessOrValue;
 				b = p;
 				break;
 			case 2:
 				r = p;
 				g = lightnessOrValue;
 				b = t;
 				break;
 			case 3:
 				r = p;
 				g = q;
 				b = lightnessOrValue;
 				break;
 			case 4:
 				r = t;
 				g = p;
 				b = lightnessOrValue;
 				break;
 			case 5:
 				r = lightnessOrValue;
 				g = p;
 				b = q;
 				break;
 			}
 			this.r = (int) Math.round(r * 255);
 			this.g = (int) Math.round(g * 255);
 			this.b = (int) Math.round(b * 255);
 			break;
 		}
 	}
 
 	/**
 	 * Conveniance method for converting a HSL color to a RGB representation.
 	 * 
 	 * @param h
 	 *            the hue part of a HSL representation
 	 * @param s
 	 *            the saturation part of a HSL representation
 	 * @param l
 	 *            the lightness part of a HSL representation
 	 * @return the three RGB values as an int[]
 	 */
 	public static int[] hslToRgb(double h, double s, double l) {
 		Color temp = new Color(h, s, l, Model.HSL);
 		int[] out = { temp.getRed(), temp.getGreen(), temp.getBlue() };
 		return out;
 	}
 
 	/**
	 * Conveniance method for converting a RGB color to a HSV representation.
 	 * 
 	 * @param r
 	 *            the red part of the RGB representation
 	 * @param g
 	 *            the green part of the RGB representation
 	 * @param b
 	 *            the blue part of the RGB representation
 	 * @return the three HSV values as a double[]
 	 */
 	public static double[] rgbToHsv(int r, int g, int b) {
 		Color temp = new Color(r, g, b);
 		double[] out = { temp.getHSVHue(), temp.getHSVSaturation(), temp.getHSVValue() };
 		return out;
 	}
 
 	/**
	 * Conveniance method for converting a RGB color to a HSL representation.
 	 * 
 	 * @param r
 	 *            the red part of the RGB representation
 	 * @param g
 	 *            the green part of the RGB representation
 	 * @param b
 	 *            the blue part of the RGB representation
 	 * @return the three HSL values as a double[]
 	 */
 	public static double[] rgbToHsl(int r, int g, int b) {
 		Color temp = new Color(r, g, b);
 		double[] out = { temp.getHSLHue(), temp.getHSLSaturation(), temp.getHSLLightness() };
 		return out;
 	}
 
 	/**
 	 * Get the red value if the color is represented as RGB.
 	 * 
 	 * @return the red
 	 */
 	public int getRed() {
 		return r;
 	}
 
 	/**
 	 * Get the green value if the color is represented as RGB.
 	 * 
 	 * @return the green
 	 */
 	public int getGreen() {
 		return g;
 	}
 
 	/**
 	 * Get the blue value if the color is represented as RGB.
 	 * 
 	 * @return the blue
 	 */
 	public int getBlue() {
 		return b;
 	}
 
 	/**
 	 * Get the blue value if the color is represented as HSV.
 	 * 
 	 * @return the HSV hue
 	 */
 	public double getHSVHue() {
 		return hsvh;
 	}
 
 	/**
 	 * Get the saturation if the color is represented as HSV.
 	 * 
 	 * @return the HSV saturation
 	 */
 	public double getHSVSaturation() {
 		return hsvs;
 	}
 
 	/**
 	 * Get the value if the color is represented as HSV.
 	 * 
 	 * @return the HSV value
 	 */
 	public double getHSVValue() {
 		return hsvv;
 	}
 
 	/**
 	 * Get the hue if the color is represented as HSL.
 	 * 
 	 * @return the HSL hue
 	 */
 	public double getHSLHue() {
 		return hslh;
 	}
 
 	/**
 	 * Get the saturation if the color is represented as HSL.
 	 * 
 	 * @return the HSL saturation
 	 */
 	public double getHSLSaturation() {
 		return hsls;
 	}
 
 	/**
 	 * Get the lightness if the color is represented as HSL.
 	 * 
 	 * @return the HSL lightness
 	 */
 	public double getHSLLightness() {
 		return hsll;
 	}
 
 	/**
 	 * Get a hex RGB representation of the color, useful in CSS.
 	 * 
 	 * @return the hex color representation
 	 */
 	public String getHexValue() {
 		return "#" + pad(Integer.toHexString(r)) + pad(Integer.toHexString(g)) + pad(Integer.toHexString(b));
 	}
 	
 	/**
	 * Conveniance method for converting a HSV color to a hex representation,
 	 * useful in CSS.
 	 * 
 	 * @param h
 	 *            the hue part of a HSV representation
 	 * @param s
 	 *            the saturation part of a HSV representation
 	 * @param v
	 *            the valye part of a HSV representation
 	 * @return the string
 	 */
 	public static String hsvToHex(double h, double s, double v) {
 		return new Color(h, s, v, Model.HSV).getHexValue();
 	}
 
 	/**
	 * Conveniance method for converting a HSL color to a hex representation,
 	 * useful in CSS.
 	 * 
 	 * @param h
 	 *            the hue part of a HSL representation
 	 * @param s
 	 *            the saturation part of a HSL representation
 	 * @param l
 	 *            the lightness part of a HSL representation
 	 * @return the string
 	 */
 	public static String hslToHex(double h, double s, double l) {
 		return new Color(h, s, l, Model.HSL).getHexValue();
 	}
 	
 
 	private String pad(String in) {
 		if (in.length() == 0) {
 			return "00";
 		}
 		if (in.length() == 1) {
 			return "0" + in;
 		}
 		return in;
 	}
 
 	/**
	 * An human-readable representation of the color as RGB.
 	 */
 	@Override
 	public String toString() {
 		return "red=" + r + ", green=" + g + ", blue=" + b;
 	}
 
 	private void calcHSL() {
 		double nr = r / 255, ng = g / 255, nb = b / 255;
 		double max = Math.max(nr, Math.max(ng, nb));
 		double min = Math.min(nr, Math.min(ng, nb));
 		double h, s, l = (max + min) / 2;
 
 		if (max == min) {
 			h = s = 0; // achromatic
 		} else {
 			double d = max - min;
 			s = l > 0.5 ? d / (2 - max - min) : d / (max + min);
 			if (nr > ng && nr > nb) {
 				h = (ng - nb) / d + (ng < nb ? 6 : 0); // max(r,g,b) = r
 			} else if (ng > nb) {
 				h = (nb - nr) / d + 2; // max(r,g,b) = g
 			} else {
 				h = (nr - ng) / d + 4; // max(r,g,b) = b
 			}
 			h = h / 6;
 		}
 		hslh = h;
 		hsls = s;
 		hsll = l;
 	}
 
 	private void calcHSV() {
 		double nr = r / 255, ng = g / 255, nb = b / 255;
 		double max = Math.max(nr, Math.max(ng, nb));
 		double min = Math.min(nr, Math.min(ng, nb));
 		double h = max, s = max, v = max;
 
 		double d = max - min;
 		s = max == 0 ? 0 : d / max;
 
 		if (max == min) {
 			h = 0; // achromatic
 		} else {
 			if (max == nr) {
 				h = (ng - nb) / d + (ng < nb ? 6 : 0);
 			} else if (max == ng) {
 				h = (nb - nr) / d + 2;
 			} else if (max == nb) {
 				h = (nr - ng) / d + 4;
 			}
 			h /= 6;
 		}
 		hsvh = h;
 		hsvs = s;
 		hsvv = v;
 	}
 }
