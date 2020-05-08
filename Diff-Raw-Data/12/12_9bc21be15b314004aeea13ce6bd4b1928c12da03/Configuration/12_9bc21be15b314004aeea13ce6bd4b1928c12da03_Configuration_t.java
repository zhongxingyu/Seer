 package com.example.digitallighterserver;
 
 import java.util.Arrays;
 import java.util.List;
 
 import com.example.lightdetector.ColorManager;
 import com.example.lightdetector.ColorMappingPair;
 
 public class Configuration {
 	// === MEDIA CHOICE =========================================
 	// media to be played in assets folder
 	static public String MEDIA_SOURCE = "5x4/czech-flag";
 	// number of columns
 	static public int TILES_X = 5;
 	// number of rows
 	static public int TILES_Y = 4;
 
 	// === DETECTION OF POSITION ================================
 	static public boolean USE_TREE_DETECTION = true;
 	// waiting time between sending a signal and taking a picture (ms)
 	static public int WAIT_TIME = 2000;
 
 	static public String SHUT_DOWN_COLOR = ColorManager
 			.getHexColor(ColorManager.BLACK);
 
 	// color for simple detection algorithm
 	static public String RARE_COLOR_SIMPLE = ColorManager
 			.getHexColor(ColorManager.WHITE);
 
 	// recovery tries for detection
	static public int RECOVERY_TRIES_SIMPLE = 1;
 
 	// rare colors for tree algorithm
 	static public List<ColorMappingPair> RARE_COLORS_TREE = Arrays.asList(
 			new ColorMappingPair(ColorManager.getHexColor(ColorManager.WHITE)),
 			new ColorMappingPair(ColorManager.getHexColor(ColorManager.BLUE)),
 			new ColorMappingPair(ColorManager
 					.getHexColor(ColorManager.DARK_RED))/*, new ColorMappingPair(
 					ColorManager.getHexColor(ColorManager.DARK_GREEN),
 					ColorManager.getHexColor(ColorManager.GREEN))*/);
 
 	// === MEDIA PLAYING ========================================
 	// how many frames in advance should be processed
	static public int NEXT_FRAMES = 1;
 	// images per second
 	static public int FRAME_RATE = 10;
 	// number of millisecond when
 	static public int SEND_COMMAND_BEFORE = 1000;
 	// wait before playing a media in ms
 	static public long WAIT_BEFORE_PLAYING = 5000;
 
 }
