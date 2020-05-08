 package com.vloxlands;
 
 import java.nio.ByteBuffer;
 
 import org.lwjgl.LWJGLException;
 import org.lwjgl.opengl.Display;
 import org.lwjgl.opengl.DisplayMode;
 
 import com.vloxlands.game.Game;
 import com.vloxlands.game.world.Map;
 import com.vloxlands.scene.Scene_Mainmenu;
 import com.vloxlands.settings.CFG;
 import com.vloxlands.util.Assistant;
 import com.vloxlands.util.MapAssistant;
 
 import de.dakror.universion.UniVersion;
 
 public class Vloxlands
 {
 	public static void main(String[] args)
 	{
 		CFG.INTERNET = Assistant.isInternetReachable();
 
 		Map map = MapAssistant.loadMap("testMap");
 		MapAssistant.saveMap(map, "testMap2");
 		
 		// -- UniVersion & Reporter initialization -- //
 		UniVersion.offline = !CFG.INTERNET;
 		UniVersion.init(Vloxlands.class, CFG.VERSION, CFG.PHASE);
 
 		if (!CFG.DEBUG)
 		{
 			// -- Deactivated while in development stage -- //
 			// Reporter.init(new File(FileManager.dir, "Logs"));
 		}
 
 		Game.initGame();
 		try
 		{
 			Display.setDisplayMode(Display.getDesktopDisplayMode());
 			Display.setDisplayMode(new DisplayMode(640, 480));
 			Display.setIcon(new ByteBuffer[] { Assistant.loadImage(Vloxlands.class.getResource("/img/logo16.png")), Assistant.loadImage(Vloxlands.class.getResource("/img/logo32.png")) });
 			Display.setTitle("Vloxlands");
 			Display.create();
 			Game.initGLSettings();
 			Game.currentGame.setScene(new Scene_Mainmenu());
 
 			while (!Display.isCloseRequested())
 				Game.currentGame.gameLoop();
 
 		}
 		catch (LWJGLException e)
 		{
 			e.printStackTrace();
 		}
 		Display.destroy();
 	}
 }
