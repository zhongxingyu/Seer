 package com.me.mygdxgame;
 
 import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
 import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
 import com.badlogic.gdx.tools.imagepacker.TexturePacker2;
 import com.badlogic.gdx.tools.imagepacker.TexturePacker2.Settings;
 
 public class Main {
 	public static void main(String[] args) {
 		LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
 		cfg.title = "my-gdx-game";
 		cfg.useGL20 = false;
 		
 		
 		cfg.width = GameConstants.screenWidth;
 		cfg.height = GameConstants.screenHeight;
 		
 		Settings settings = new Settings();
 		settings.maxWidth = 512;
 		settings.maxHeight = 512;
 		
 		// This part regenerates texture atlases each time the desktop project is ran.
 		
 		// Enter path to android asset folder on your computer, or remove this to compile if you want
 		// to compile and no new images have been added.
		String path = "D:\\github\\AdaptiveGame\\LibGDXScene2d\\my-gdx-game-android\\assets\\Images";
		//String path = "C:\\Users\\Robin\\github\\AdaptiveGame\\LibGDXScene2d\\my-gdx-game-android\\assets\\Images";
 		
 		
 		// Make copies in android project
         TexturePacker2.process(settings, path + "\\Enemies", "../my-gdx-game-android/assets/Images", "enemies"); // settings, source path, destination path, texture atlas file name
         TexturePacker2.process(settings, path + "\\MiscSmall", "../my-gdx-game-android/assets/Images", "misc");
         TexturePacker2.process(settings, path + "\\Towers", "../my-gdx-game-android/assets/Images", "towers");
         TexturePacker2.process(settings, path + "\\MapTiles", "../my-gdx-game-android/assets/Images", "mapTiles");
         
         // Make copies in desktop project
         TexturePacker2.process(settings, path + "\\Enemies", "../my-gdx-game-desktop/bin/Images", "enemies");
         TexturePacker2.process(settings, path + "\\MiscSmall", "../my-gdx-game-desktop/bin/Images", "misc");
         TexturePacker2.process(settings, path + "\\Towers", "../my-gdx-game-desktop/bin/Images", "towers");
         TexturePacker2.process(settings, path + "\\MapTiles", "../my-gdx-game-desktop/bin/Images", "mapTiles");
 		// ----------------------------------------------
         
         
 		new LwjglApplication(new MyGdxGame(), cfg);
 	}
 }
