 /**
  *  Copyright 2013 Phantom Coder (Richard Tester)
  */
 package com.spectregames.concentration.libs;
 
 /**
  * 
  * Project: Concentration
  * 
  * Class: Reference
  * 
  * @author rtester
  *
  */
 public class Reference {
 	
 	public static final int WIDTH = 800;
 	public static final int HEIGHT = 640;
 	public static final int CENTER_X = WIDTH / 2;
 	public static final int CENTER_Y = HEIGHT / 2;
 	
 	public static final String TITLE = "Concentration";
 	
 	public static boolean isDevMode = true;
 	public static boolean isDebugMode = false;
 	public static boolean isPressed = false;
 	
 	// Directory structure for all images used in the game.
	public static final String BASE_SPRITE_DIR = "/sprites/";
 	public static final String GUI_SPRITES = BASE_SPRITE_DIR + "gui/";
 	public static final String MAINMENU = GUI_SPRITES + "main_menu/";
	public static final String SPLASH = GUI_SPRITES + "splash_screen/";
 	public static final String THEMES = BASE_SPRITE_DIR + "themes/";
 	public static final String CURSORS = BASE_SPRITE_DIR + "cursors/";
 	public static final String ICONS = BASE_SPRITE_DIR + "icons/";
 	public static final String FONTS = BASE_SPRITE_DIR + "fonts/";
 	
	
 	//Directory structure for all audio files used in the game.
 	public static final String BASE_AUDIO_DIR = "/audio/";
 	public static final String BGM = BASE_AUDIO_DIR + "bgm/";
 	public static final String SFX = BASE_AUDIO_DIR + "sfx/";
	
 }
