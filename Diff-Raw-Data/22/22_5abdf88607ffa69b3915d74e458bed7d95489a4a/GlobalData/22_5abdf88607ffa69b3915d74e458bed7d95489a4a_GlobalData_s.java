 package org.ssg.Cambridge;
 
 import java.io.File;
 import java.io.IOException;
 
 import net.java.games.input.Controller;
 import net.java.games.input.ControllerEnvironment;
 import net.java.games.input.ControllerEvent;
 import net.java.games.input.ControllerListener;
 
 import org.ini4j.Ini;
 import org.ini4j.InvalidFileFormatException;
 import org.newdawn.slick.AngelCodeFont;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.SlickException;
 
 import paulscode.sound.SoundSystem;
 
 public class GlobalData {
 	final static String RESDIR = "res/";
 	public final String userconfigdir = "user_config.cfg";
 	public final String gameconfigdir = "config.cfg";
 	public final int GAMEMODES = 5;
 	public final int GAMEPLAYSTATE = 10;
 	public final int MENUMAINSTATE = 11;
 	public final int MENUOPTIONSSTATE = 12;
 	public final int MENUPLAYERSETUPSTATE = 13;
 	public final int MENUGAMESETUPSTATE = 14;
 	public final int MENUTEAMSETUPSTATE = 15;
 	public final int MENUPAUSESTATE = 16;
 	public final int GAMEOVERSTATE = 17;
 	private int screenHeight;
 	private int screenWidth;
 	private boolean fullscreen;
 	private int masterSound;
 	private int effectSound;
 	private int ambientSound;
 	private boolean playerIdDisplay;
 	private SoundSystem mySoundSystem;
 	private Ini userConfig, gameConfig;
 	private Ini.Section displayConfig, soundConfig, gameplayConfig;
 	private int[][] resolutionList;
 	private int resolutionIndex;
 	
 	private ControllerEnvironment controllerEnv;
 	private CambridgeController[] controllers;
 	private CambridgePlayerAnchor[] anchors;
 	
 	public final float DPAD_UP = 0.25f;
 	public final float DPAD_DOWN = 0.75f;
 	public final float DPAD_LEFT = 1.0f;
 	public final float DPAD_RIGHT = 0.5f;
 	
 	// Gamemode variables
 	private int gamemode, timeLimit, scoreLimit;
 	private int[] timeLimits, scoreLimits;
 	private boolean actionCam;
 
 	private boolean loaded;
 	
 	public GlobalData() {
 		userConfig = null;
 		gameConfig = null;
 		displayConfig = null;
 		soundConfig = null;
 		gameplayConfig = null;
 		loaded = false;
 		this.screenHeight = 800;
 		this.screenWidth = 600;
 		this.fullscreen = false;
 		this.masterSound = 10;
 		this.effectSound = 10;
 		this.ambientSound = 10;
 		this.playerIdDisplay = true;
 		this.controllerEnv = ControllerEnvironment.getDefaultEnvironment();
 		
 		this.gamemode = 0;
 		this.actionCam = true;
 		
 		this.timeLimits = new int[] {
 			-1,
 			30,
 			60,
 			90,
 			120,
 			180,
 			240,
 			300,
 			600,
 			900
 		};
 		
 		this.scoreLimits = new int[] {
 			-1,
 			1,
 			5,
 			10,
 			15,
 			20,
 			25,
 			50
 		};
 		
 		this.resolutionList = new int[][] {
 				{ 640, 480 },
 				{ 800, 600 },
 				{ 960, 720 },
 				{ 1024, 768 },
 				{ 1280, 720 },
 				{ 1280, 960 },
 				{ 1366, 768 },
 				{ 1400, 1050 },
 				{ 1440, 1080 },
 				{ 1600, 900 },
 				{ 1600, 1200 },
 				{ 1920, 1080 },
 				{ 1920, 1440 },
 				{ 2560, 1440 },
 				{ 2560, 1920 }
 		};
 		
 		resolutionIndex = -1;
 		
 		this.timeLimit = 3;
 		this.scoreLimit = 3;
 		
 		controllers = new CambridgeController[4];
 		anchors = new CambridgePlayerAnchor[] {
 				new CambridgePlayerAnchor(0),
 				new CambridgePlayerAnchor(1),
 				new CambridgePlayerAnchor(2),
 				new CambridgePlayerAnchor(3)
 		};
 		
 		initializeControllers();
 		loadConfig();
 		getControllers();
 	}
 	
 	public void loadConfig() {
 		try {
 			userConfig = new Ini(new File(RESDIR + userconfigdir));
 			gameConfig = new Ini(new File(RESDIR + gameconfigdir));
 			displayConfig = userConfig.get("DISPLAY");
 			setScreenWidth(displayConfig.get("SCREENWIDTH", int.class));
 			setScreenHeight(displayConfig.get("SCREENHEIGHT", int.class));
 			for (int i = 0; i < resolutionList.length; i++) {
 				if (screenWidth == resolutionList[i][0] && screenHeight == resolutionList[i][1]) {
 					resolutionIndex = i;
 				}
 			}
 			setFullscreen(displayConfig.get("FULLSCREEN", boolean.class));
 			
 			soundConfig = userConfig.get("AUDIO");
 			setMasterSound(soundConfig.get("MASTER", int.class));
 			setEffectSound(soundConfig.get("EFFECTS", int.class));
 			setAmbientSound(soundConfig.get("AMBIENT", int.class));
 			
 			gameplayConfig = userConfig.get("GAMEPLAY");
 			setPlayerIdDisplay((gameplayConfig.get("PLAYERID", boolean.class)));
 			
 			loaded = true;
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			System.out.println("Config Load Failed!");
 			e.printStackTrace();
 		}
 	}
 	
 	// DOES NOT WORK RIGHT NOW. Looking into JInput and source code, but things aren't promising.
 	private class CambridgeControllerListener implements ControllerListener {
 		@Override
 		public void controllerRemoved(ControllerEvent ev) {
 			removeControllers();
 			System.out.println("Controller Removed!" + ev);
 		}
 		
 		@Override
 		public void controllerAdded(ControllerEvent ev) {
 			getControllers();
 			System.out.println("Controller Added!" + ev);
 		}
 	}
 	
 	// Method to initialize controllers and attach controller added and removed listeners.
 	public void initializeControllers() {
 		for (int i = 0; i < controllers.length; i++) {
 			controllers[i] = new CambridgeController();
 		}
 	}
 	
 	public void getControllers() {
 		controllerEnv = ControllerEnvironment.getDefaultEnvironment();
 		for (Controller c : controllerEnv.getControllers()) {
 //			System.out.println(c.getName() + " " + c.getType());
 			if (c.getType() == Controller.Type.GAMEPAD || c.getType() == Controller.Type.STICK) {
 				if (!controllers[0].exists() || controllers[0].poll() == false) {
 					controllers[0] = new CambridgeController(c);
 //					controllers.addControllerListener(c1.);
 					System.out.println("First Controller Found: " + controllers[0].getName());
 				} else if (!controllers[1].exists() || controllers[1].poll() == false) {
 					controllers[1] = new CambridgeController(c);
 					System.out.println("Second Controller Found: " + controllers[1].getName());
 				} else if (!controllers[2].exists() || controllers[2].poll() == false) {
 					controllers[2] = new CambridgeController(c);
 					System.out.println("Third Controller Found: " + controllers[2].getName());
 				} else if (!controllers[3].exists() || controllers[3].poll() == false) {
 					controllers[3] = new CambridgeController(c);
 					System.out.println("Fourth Controller Found: " + controllers[3].getName());
 				} //end if-else block
 			} //end if statement
 		} //end for each loop
 	}
 	
 	public void removeControllers() {
 		for (CambridgeController cTemp: controllers) {
 			if (cTemp != null && cTemp.poll() == false) {
 				cTemp = new CambridgeController();
 			}
 		}
 	}
 
 	public CambridgeController[] getC() {
 		return controllers;
 	}
 
 //	public void setC(Controller[] controllers) {
 //		this.controllers = controllers;
 //	}
 
 	public void setControllerEnv(ControllerEnvironment controllerEnv) {
 		this.controllerEnv = controllerEnv;
 	}
 	
 	public ControllerEnvironment getControllerEnv() {
 		return this.controllerEnv;
 	}
 	
 	public CambridgePlayerAnchor[] playerAnchors() {
 		return anchors;
 	}
 	
 	public CambridgePlayerAnchor getAnchor(int n) {
 		return anchors[n];
 	}
 
 	public int screenHeight() {
 		return screenHeight;
 	}
 
 	public void setScreenHeight(int screenHeight) {
 		this.screenHeight = screenHeight;
 	}
 
 	public int screenWidth() {
 		return screenWidth;
 	}
 
 	public void setScreenWidth(int screenWidth) {
 		this.screenWidth = screenWidth;
 	}
 
 	public boolean fullscreen() {
 		return fullscreen;
 	}
 
 	public void setFullscreen(boolean fullscreen) {
 		this.fullscreen = fullscreen;
 	}
 	
 	public int[] getResolution() {
 		return resolutionList[resolutionIndex];
 	}
 	
 	public int resolutionIndex() {
 		return resolutionIndex;
 	}
 	
 	public void setResolutionIndex(int n) {
 		resolutionIndex = n;
 	}
 	
 	public int resolutions() {
 		return resolutionList.length;
 	}
 
 	public int masterSound() {
 		return masterSound;
 	}
 
 	public void setMasterSound(int masterSound) {
 		this.masterSound = masterSound;
 	}
 
 	public int effectSound() {
 		return effectSound;
 	}
 
 	public void setEffectSound(int effectSound) {
 		this.effectSound = effectSound;
 	}
 
 	public int ambientSound() {
 		return ambientSound;
 	}
 
 	public void setAmbientSound(int ambientSound) {
 		this.ambientSound = ambientSound;
 	}
 
 	public SoundSystem mySoundSystem() {
 		return mySoundSystem;
 	}
 
 	public void setMySoundSystem(SoundSystem mySoundSystem) {
 		this.mySoundSystem = mySoundSystem;
 	}
 
 	public boolean playerIdDisplay() {
 		return playerIdDisplay;
 	}
 
 	public void setPlayerIdDisplay(boolean playerIdDisplay) {
 		this.playerIdDisplay = playerIdDisplay;
 	}
 	
 	public Ini.Section displayConfig() {
 		return displayConfig;
 	}
 	
 	public void setDisplayConfig(Ini.Section displayConfig) {
 		this.displayConfig = displayConfig;
 	}
 	
 	public Ini.Section soundConfig() {
 		return soundConfig;
 	}
 	
 	public void setSoundConfig(Ini.Section soundConfig) {
 		this.soundConfig = soundConfig;
 	}
 	
 	public Ini.Section gameplayConfig() {
 		return gameplayConfig;
 	}
 	
 	public void setGameplayConfig(Ini.Section gameplayConfig) {
 		this.gameplayConfig = gameplayConfig;
 	}
 	public Ini userConfig() {
 		return userConfig;
 	}
 	public void setUserConfig(Ini userConfig) {
 		this.userConfig = userConfig;
 	}
 	public Ini gameConfig() {
 		return gameConfig;
 	}
 	public void setGameConfig(Ini gameConfig) {
 		this.gameConfig = gameConfig;
 	}
 	
 	public int gameType() {
 		return gamemode;
 	}
 	
 	public void setGameType(int n) {
 		gamemode = n;
 	}
 	
 	public int timeLimit() {
 		return timeLimits[timeLimit];
 	}
 	
 	public void setTimeLimit(int n) {
 		timeLimit += n;
 		if (timeLimit < 0) {
 			timeLimit = timeLimits.length + timeLimit;
 		} else if (timeLimit >= timeLimits.length) {
 			timeLimit %= timeLimits.length;
 		}
 	}
 	
 	public int scoreLimit() {
 		return scoreLimits[scoreLimit];
 	}
 	
 	public void setScoreLimit(int n) {
 		scoreLimit += n;
 		if (scoreLimit < 0) {
 			scoreLimit = scoreLimits.length + scoreLimit;
 		} else if (scoreLimit >= scoreLimits.length) {
 			scoreLimit %= scoreLimits.length;
 		}
 	}
 	
 	public boolean actionCam() {
 		return actionCam;
 	}
 	
 	public void setActionCam(boolean b) {
 		actionCam = b;
 	}
 }
