 package de.krakel.darkbeam.lib;
 
 public final class FColors {
 	public static final String PREFIX_GRAY = "\u00a77";
 	public static final String PREFIX_WHITE = "\u00a7f";
 	public static final String PREFIX_YELLOW = "\u00a7e";
 	public static final String PURE_WHITE = "ffffff";
 
 	private FColors() {
 	}
 
 	public static String get( String name, boolean withColor) {
 		if (withColor) {
 			return PREFIX_YELLOW + name + PREFIX_WHITE;
 		}
		return String.valueOf( name);
 	}
 }
