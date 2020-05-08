 package com.github.joakimpersson.tda367;
 
 import java.io.File;
 
 import org.lwjgl.LWJGLUtil;
 import org.newdawn.slick.AppGameContainer;
 import org.newdawn.slick.SlickException;
 
 import com.github.joakimpersson.tda367.controller.PyromaniacsGame;
 import com.github.joakimpersson.tda367.gui.guiutils.GUIUtils;
 
 /**
  * 
  * @author joakimpersson
  * 
  */
 public class Main {
 
 	public static void main(String[] args) {
 		// Get the game size
 		int gameWidth = GUIUtils.getGameWidth();
 		int gameHeight = GUIUtils.getGameHeight();
 
 		/*
 		 * Dynamically uses the correct native files for lwjgl
 		 */
 		System.setProperty("org.lwjgl.librarypath",
 				new File(new File(new File(System.getProperty("user.dir"),
 						"lib"), "native"), LWJGLUtil.getPlatformName())
 						.getAbsolutePath());
 		System.setProperty("net.java.games.input.librarypath",
 				System.getProperty("org.lwjgl.librarypath"));
 		try {
 			AppGameContainer application = new AppGameContainer(
 					new PyromaniacsGame("Pyromaniacs"));
 
 			application.setDisplayMode(gameWidth, gameHeight, false);
 			// make sure that we are using the players screen
			application.setTargetFrameRate(60);
 
 			// remove the fps meter
 			application.setShowFPS(false);
 
 			// launch the game
 			application.start();
 		} catch (SlickException e) {
 			e.printStackTrace();
 		}
 	}
 
 }
