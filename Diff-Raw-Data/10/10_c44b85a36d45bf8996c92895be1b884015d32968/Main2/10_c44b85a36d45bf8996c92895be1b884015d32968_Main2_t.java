 package com.github.slamm.morrigna.desktop;
 
 import com.badlogic.gdx.Files.FileType;
 import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
 import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
 import com.reed.birdseye.BirdsEye;
 
 public class Main2 {
 	public static void main(String[] args) {
 		LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
 		cfg.title = "Lode Scape - 1.1.3 Beta";
 		cfg.useGL20 = true;
 		cfg.width = 960;
 		cfg.height = 540;
 		cfg.foregroundFPS = 60;
 		cfg.backgroundFPS = 60;
 		cfg.resizable = false;
 		cfg.addIcon("data/icon16x16.png", FileType.Internal);
 		cfg.addIcon("data/icon32x32.png", FileType.Internal);
 
		@SuppressWarnings("unused")
        LwjglApplication app = new LwjglApplication(new BirdsEye(), cfg);
 	}
 }
