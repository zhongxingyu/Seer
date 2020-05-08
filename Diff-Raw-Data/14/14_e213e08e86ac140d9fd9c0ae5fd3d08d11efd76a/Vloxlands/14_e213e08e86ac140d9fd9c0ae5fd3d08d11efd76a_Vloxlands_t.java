 package com.vloxlands;
 
 import java.awt.Dimension;
 import java.awt.Toolkit;
 import java.io.File;
 import java.nio.ByteBuffer;
 
 import javax.swing.UIManager;
 
 import org.lwjgl.LWJGLException;
 import org.lwjgl.opengl.Display;
 import org.lwjgl.opengl.DisplayMode;
 
 import com.vloxlands.game.Game;
 import com.vloxlands.scene.Scene_Logo;
 import com.vloxlands.settings.CFG;
 import com.vloxlands.util.Assistant;
 
 import de.dakror.universion.UniVersion;
 
 public class Vloxlands
 {
 	public static void main(String[] args)
 	{
 		CFG.INTERNET = false;// Assistant.isInternetReachable();
 		
 		try
 		{
 			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
 		}
 		catch (Exception e1)
 		{
 			e1.printStackTrace();
 		}
 		
 		// -- UniVersion & Reporter initialization -- //
 		UniVersion.offline = !CFG.INTERNET;
 		UniVersion.init(Vloxlands.class, CFG.VERSION, CFG.PHASE);
 		
 		if (!CFG.DEBUG)
 		{
 			// -- Deactivated while in development stage -- //
 			// Reporter.init(new File(FileManager.dir, "Logs"));
 		}
 		
 		System.setProperty("org.lwjgl.librarypath", new File("natives").getAbsolutePath());
 		
 		// Direction.get90DegreeDiretions(Direction.UP);
 		
 		
 		try
 		{
 			if (CFG.FULLSCREEN) enterFullscreen();
 			else leaveFullscreen();
 			Display.setIcon(new ByteBuffer[] { Assistant.loadImage(Vloxlands.class.getResourceAsStream("/graphics/logo/logo16.png")), Assistant.loadImage(Vloxlands.class.getResourceAsStream("/graphics/logo/logo32.png")) });
 			Display.setTitle("Vloxlands");
 			// Display.setInitialBackground(0.5f, 0.8f, 0.85f);
 			Display.create();
 			Game.initGLSettings();
 			
 			Game.initGame();
 			// Game.currentGame.setScene(new Scene_Mainmenu());
 			Game.currentGame.setScene(new Scene_Logo());
 			
 			while (!Display.isCloseRequested())
 				Game.currentGame.gameLoop();
 			
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 		}
 		Display.destroy();
 	}
 	
 	public static void enterFullscreen() throws LWJGLException
 	{
 		Display.setDisplayModeAndFullscreen(Display.getDesktopDisplayMode());
 		Display.setVSyncEnabled(true);
 	}
 	
 	public static void leaveFullscreen() throws LWJGLException
 	{
 		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
 		
 		Display.setDisplayMode(new DisplayMode(1280/* d.width - 300 */, /* (int) (d.height - 300 * (d.height / (float) d.width)) */720));
 		Display.setResizable(true);
 		Display.setFullscreen(false);
 	}
 }
