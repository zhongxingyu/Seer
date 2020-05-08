 /**
  * 
  * Class: SVGColorScheme
  * Description: Parsing <color> in SVG documents and convert to standard RGB Color scheme readable by Java
  * 
  * @author: Cheow Yeong Chi
  * @date: 15/03/2013
  * 
  */
 
 package rocks6205.svg.adt;
 
 import java.awt.Color;
 import java.util.regex.*;
 
 public class SVGColorScheme extends Color {
 
 	private static final long serialVersionUID = 5839823140108294927L;
 
 	/*
 	 * COLOR DATA STRUCTURE FORMAT
 	 */
 
 	//	Standard color format
 	//	e.g. RGB(120,83,22)
 	//	e.g. RGB(70%,62%,3%)
 	public static final String RGB_COLOR = "rgb\\((" + SVGPrimitive._8_BIT_UINT + ","
 			+ SVGPrimitive._8_BIT_UINT + "," + SVGPrimitive._8_BIT_UINT + "|" + SVGPrimitive.PERCENTAGE_UNUM
 			+ "," + SVGPrimitive.PERCENTAGE_UNUM + "," + SVGPrimitive.PERCENTAGE_UNUM + ")\\)";
 
 	//	Hexadecimal format
 	// 	e.g. #123abc , #fff
 	public static final String HEXA_COLOR = "#" + "(" + SVGPrimitive._4_BIT_HEX_UINT + "{3}){1,2}";
 
 	/*
 	 * CONSTRUCTOR
 	 */
 	public SVGColorScheme(int red, int green, int blue) {
 		super(red, green, blue);
 	}
 
 	/*
 	 * METHODS
 	 */
 	/**
 	 * Parsing color attribute to convert to Java-compatible color type where matches formats.
 	 * 
 	 * @param colorAttributeString	Attribute string that is read directly from SVG file
 	 * @return Color in RGB format which Java reads
 	 */
 	public static SVGColorScheme parse(String colorAttributeString){
 		colorAttributeString = colorAttributeString.trim();
 		SVGColorScheme color = getColorFromKeyword(colorAttributeString);
 		if (color != null) {
 			return color;
 		}
 
 		Matcher hexMatcher = Pattern.compile(HEXA_COLOR,Pattern.CASE_INSENSITIVE).matcher(colorAttributeString);
 		if (hexMatcher.matches()) {
 			return decodeHex(colorAttributeString);
 		}
 
 		Matcher rgbMatcher = Pattern.compile(RGB_COLOR,Pattern.CASE_INSENSITIVE).matcher(colorAttributeString);
 		if (rgbMatcher.matches()) {
 			int[] rgb = new int[3];
 			String string = rgbMatcher.group(1).trim();
 			double multiplier = 1;
 
 			if (string.contains("%")) {
 				string = string.replace("%", "");
 				multiplier = 2.55;
 			}
 
 			String[] parts = string.split("\\s*,\\s*");
 
 			for (int u = 0; u < 3; u++) {
 				rgb[u] = (int) (Integer.parseInt(parts[u]) * multiplier);
 				rgb[u] = normaliseRGB(rgb[u]);
 			}
 
 			return new SVGColorScheme(rgb[0], rgb[1], rgb[2]);
 		}
 
 		return null;
 	}
 
 	private static int normaliseRGB(int val) {
 		if(val>255) return 255;
 		if(val<0) return 0;
 		return val;
 	}
 
 	/**
 	 * Converts color keywords into RGB Format which Java reads.
 	 * 
 	 * @param colorAttributeString	Attribute string that is read directly from SVG file
 	 * @return Color in RGB format which Java reads
 	 */
 	public static SVGColorScheme getColorFromKeyword(
 			String colorAttributeString) {
 		colorAttributeString = colorAttributeString.toUpperCase();
 		for (SVGColorKeyword keyword : SVGColorKeyword.values()) {
 			if (keyword.name().equals(colorAttributeString)) {
 				return keyword.getSVGColorScheme();
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Decode hexadecimal color format
 	 * 
 	 * @param colorAttributeString	Attribute string that is read directly from SVG file
 	 * @return Color in RGB format which Java reads
 	 */
 	private static SVGColorScheme decodeHex(String hexColorString) {
 		try {
 			Color color = Color.decode(extendHex(hexColorString));
 			return new SVGColorScheme(color.getRed(), color.getGreen(),
 					color.getBlue());
 		} catch (NumberFormatException nfe) {
 			nfe.printStackTrace();
 			return null;
 		}
 
 	}
 	/**
 	 * Convert hexadecimal strings that are in 3 digit format into 6-digit format for Color
 	 * class to decode
 	 * 
 	 * @param hexColorString	Short hexadecimal string
 	 * @return	Extended hexadecimal string 
 	 */
 	private static String extendHex(String hexColorString) {		
 		Matcher shortHexaStringMatcher = Pattern.compile(SVGPrimitive._4_BIT_HEX_UINT + "{3}").matcher(
 				hexColorString.substring(1));
 		if (shortHexaStringMatcher.matches()) {
 			String shortHex = shortHexaStringMatcher.group(), prefix = "#";
 			char c;
 
 			for (int i = 0; i < 3; i++) {
 				c = shortHex.charAt(i);
 				prefix += "" + c + c;
 			}
 
 			return prefix;
 		}
 		return hexColorString;
 	}
 }
