 /*
  * Copyright (C) 2012 JPII and contributors
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.jpii.navalbattle;
 
 import javax.swing.UIManager.*;
 import javax.swing.*;
 
 import com.jpii.gamekit.GameKit;
 import com.jpii.gamekit.debug.Debugger;
 import com.jpii.gamekit.exception.InvalidApiLevelException;
 import com.jpii.gamekit.localization.LocalizationManager;
 import com.jpii.navalbattle.data.*;
 import com.jpii.navalbattle.debug.*;
 import com.jpii.navalbattle.io.*;
 import com.jpii.navalbattle.renderer.*;
 
 import com.roketgamer.RoketGamer;
 
 public class NavalBattle {
 
 	private static RoketGamer roketGamer;
 	private static Debugger debugInstance;
 	private static GameState gameState;
 	private static WindowHandler windowHandler;
 	private static LocalizationManager localizationManager;
 	private static BroadcastService broadcastService;
 	
 	/**
 	 * <code>NavalBattle</code> main method. Ran on launch.
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		try {
 			GameKit.checkVersion(Constants.GAMEKIT_MIN_API_LEVEL, Constants.GAMEKIT_MAX_API_LEVEL);
 		} catch (InvalidApiLevelException e) {
 			e.printStackTrace();
 		}
 		
 		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
 		    public void run() {
 		    	onShutdown();
 		    }
 		}));
 		
 		Helper.LoadStaticResources();
 		setDefaultLookAndFeel();
 		debugInstance = new Debugger("NavalBattle");
 		debugInstance.registerCommands(Commands.COMMANDS);
 		
 		NavalBattleIO.run();
 		
 		if(Constants.DEBUG_MODE)
 			debugInstance.showDebugWindow();
 		
 		gameState = new GameState();
 		roketGamer = new RoketGamer();
 		
 		getDebugWindow().printInfo("NavalBattle " + Constants.NAVALBATTLE_VERSION + " initialized");
 		getDebugWindow().printInfo("Successfully loaded GameKit " + GameKit.getVersion() + " (API " + GameKit.getApiLevel() + ")");
 		
		windowHandler = new WindowHandler(492,340);;
 		localizationManager = new LocalizationManager(NavalBattle.class, "/com/jpii/navalbattle/res/strings");
 		
 		getDebugWindow().printInfo("Locale set to " + localizationManager.getLocale());
 		getDebugWindow().printInfo("Loaded " + localizationManager.getDefaultStrings().size() + " default strings.");
 		getDebugWindow().printInfo("Loaded " + localizationManager.getCurrentStrings().size() + " current strings.");
 		
 		broadcastService = new BroadcastService();
 		
 		MaxTests.run();
 	}
 	
 	public static void onShutdown() {
 		try {
 		windowHandler.disposeAll();
 		System.out.println("Game is closing.");
 		try {
 			Thread.sleep(250);
 		} catch (Throwable t) { } 
 		} catch (Throwable t) {
 			
 		}
 	}
 	
 	/**
 	 * Returns current instance of RoketGamer.
 	 * @return roketGamer
 	 */
 	public static RoketGamer getRoketGamer() {
 		return roketGamer;
 	}
 	
 	/**
 	 * Returns current instance of DebugWindow.
 	 * @return debugWindow
 	 */
 	public static Debugger getDebugWindow() {
 		return debugInstance;
 	}
 	
 	/**
 	 * Returns current instance of GameState.
 	 * @return gameState
 	 */
 	public static GameState getGameState() {
 		return gameState;
 	}
 	
 	/**
 	 * Returns current instance of WindowHandler. Used to switch Windows.
 	 * @return windowHandler
 	 */
 	public static WindowHandler getWindowHandler() {
 		return windowHandler;
 	}
 	
 	/**
 	 * Returns current instance of LocalizationManager. Used to handle localization.
 	 * @return windowHandler
 	 */
 	public static LocalizationManager getLocalizationManager() {
 		return localizationManager;
 	}
 	
 	/**
 	 * Returns current instance of BroadcastService. Used to handle updates and announcements.
 	 * @return
 	 */
 	public static BroadcastService getBroadcastService() {
 		return broadcastService;
 	}
 	
 	/**
 	 * Attempt to set <code>DefaultLookAndFeel</code> to Nimbus and
 	 * alert the users if the process fails.
 	 */
 	private static void setDefaultLookAndFeel(){
 		try {
 		    for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
 		        if ("Nimbus".equals(info.getName())) {
 		            UIManager.setLookAndFeel(info.getClassName());
 		            break;
 		        }
 		    }
 		} catch (UnsupportedLookAndFeelException e) {
 			NavalBattle.getDebugWindow().printError("NimbusLookAndFeel was unable to be loaded, unsuported");	
 		} catch (ClassNotFoundException e) {
 			NavalBattle.getDebugWindow().printError("NimbusLookAndFeel was unable to be loaded, class not found");	
 		} catch (InstantiationException e) {
 			NavalBattle.getDebugWindow().printError("NimbusLookAndFeel was unable to be loaded, instantiation");	
 		} catch (IllegalAccessException e) {
 			NavalBattle.getDebugWindow().printError("NimbusLookAndFeel was unable to be loaded, illegalaccess");	
 		} catch (Exception e) {
 			NavalBattle.getDebugWindow().printError("NimbusLookAndFeel has encountered an error, " + e.getMessage());	
 		} catch (Error e) {
 			NavalBattle.getDebugWindow().printError("NimbusLookAndFeel has encountered an error, " + e.getMessage());	
 		} catch (Throwable thr) {
 			NavalBattle.getDebugWindow().printError("NimbusLookAndFeel has encountered an error, " + thr.getMessage());
 		}
 	}
 }
