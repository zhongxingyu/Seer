 package com.dropoutdesign.ddf.config;
 
 import java.util.regex.*;
 import java.awt.Rectangle;
 
 /**
  * This is a structure meant to be converted to and from XML using XStream.
  */
 
 public class ModuleConfig {
 	
 	public String address;
 	public String origin;
 	public String size;
 	
 	private Rectangle bounds;
 	
 	public ModuleConfig() {
 		address = "";
 		origin = "";
 		size = "";
 	}
 	
 	public String getAddress() {
 		return address;
 	}
 	
 	public Rectangle getBounds() {
 		if (bounds == null) {
			Pattern point = Pattern.compile(".*(\\d+),(\\d+).*");
 			Matcher m = point.matcher(origin);
 			if (!m.matches())
 				throw new IllegalArgumentException("Invalid origin: " + origin);
 			int ox = getNumber(m.group(1));
 			int oy = getNumber(m.group(2));
 			
 			m = point.matcher(size);
 			if (!m.matches())
 				throw new IllegalArgumentException("Invalid size: " + size);
 			int sx = getNumber(m.group(1));
 			int sy = getNumber(m.group(2));
 			
 			bounds = new Rectangle(ox, oy, sx, sy);
 		}
 		return bounds;
 	}
 	
 	private static int getNumber(String numString) {
 		try {
 			return Integer.parseInt(numString);
 		} catch (NumberFormatException e) {
 			throw new IllegalArgumentException("\"" + numString 
 				+ "\" is not a number.");
 		}	
 	}
 }
