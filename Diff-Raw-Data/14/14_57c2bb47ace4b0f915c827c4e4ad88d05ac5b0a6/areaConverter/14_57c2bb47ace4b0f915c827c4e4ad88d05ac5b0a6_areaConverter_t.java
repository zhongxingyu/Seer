 package org.goldclone.converter;
 
 public class areaConverter {
 
 	private static areaConverter instance = null;
 
 	protected areaConverter() {
 		// Exists only to defeat instantiation.
 	}
 
 	public static areaConverter getInstance() {
 		if (instance == null) {
 			instance = new areaConverter();
 		}
 		return instance;
 	}
 
 	// <item>Squaremillimeters</item>
 	// <item>Squarecentimeters</item>
 	// <item>Squarefeet</item>
 	// <item>Squareinches</item>
 	// <item>Squareyards</item>
 
 	public double convert(double input, int from, int to) {
 		double sqMMeter;
 
 		if (from == 0) {// Squaremillimeters
 			sqMMeter = input;
 		} else if (from == 1) { // Squarecentimeters
 			sqMMeter = input * 100;
 		} else if (from == 2) {// Squarefeet
 			sqMMeter = input * 92903;
 		} else if (from == 3) { // Squareinches
 			sqMMeter = input * 645.2;
 		} else { // Squareyards
 			sqMMeter = input * 836127;
 		}
 
 		
 		if (to == 0) {// Squaremillimeters
			return sqMMeter;
 		} else if (to == 1) { // Squarecentimeters
			return sqMMeter / 100;
 		} else if (to == 2) {// Squarefeet
			return sqMMeter / 92903;
 		} else if (to == 3) { // Squareinches
			return sqMMeter / 645.2;
 		} else { // Squareyards
			return sqMMeter / 836127;
 		}
 	}
 }
