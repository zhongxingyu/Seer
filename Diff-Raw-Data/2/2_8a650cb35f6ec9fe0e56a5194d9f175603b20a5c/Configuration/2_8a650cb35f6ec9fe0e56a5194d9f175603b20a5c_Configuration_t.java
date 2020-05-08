 package com.dunnkers.pathmaker;
 
 import java.awt.Dimension;
 
 import com.dunnkers.util.Resource;
 
 /**
  * 
  * @author Dunnkers
  */
 public class Configuration {
 
 	public static final double APPLICATION_VERSION_MAJOR = 2;
	public static final double APPLICATION_VERSION_MINOR = 0.02;
 	public static final double APPLICATION_VERSION = APPLICATION_VERSION_MAJOR
 			+ APPLICATION_VERSION_MINOR;
 	public static final String APPLICATION_TITLE = "DunkPathMaker";
 	public static final String APPLICATION_ALIAS = "Create your paths with ease!";
 
 	public static final String WINDOW_TITLE = APPLICATION_TITLE + " v"
 			+ APPLICATION_VERSION + " - " + APPLICATION_ALIAS;
 	public static final Dimension WINDOW_SIZE = new Dimension(750, 600);
 
 	public static final String LOCAL_MAP_PATH = "resources/runescape-map-07.jpg";
 
 	public static final Resource RESOURCE = new Resource(DunkPathMaker.class);
 	public static final int INITIAL_DRAG_SENSITIVITY = 5;
 	public static final int MAX_TILE_RADIUS = 15;
 
 	private Configuration() {
 	}
 }
