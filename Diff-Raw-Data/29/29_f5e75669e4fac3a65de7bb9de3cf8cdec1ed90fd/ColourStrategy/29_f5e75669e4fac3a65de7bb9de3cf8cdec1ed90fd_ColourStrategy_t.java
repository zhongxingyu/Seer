 package com.ns.app;
 
 import java.awt.Color;
 import java.io.File;
 
 /**
  * Generates a suitable colour for a file. This is used to allow different types
  * of files to have different colours based on their type, location etc.
  */
 public interface ColourStrategy {
 
 	/**
 	 * Generates a suitable colour for a file. This is used to allow different
 	 * types of files to have different colours based on their type, location
 	 * etc.
 	 * 
 	 * @param file
 	 *            the file that will be coloured
 	 * @param isShaded
 	 *            true if the file requires shading.
	 * @return the color of the file.
 	 */
 	public Color getColour(File file, boolean isShaded);
 }
