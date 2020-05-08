 package propinquity;
 
 import controlP5.ControlEvent;
 
 import pbox2d.*;
 
 import java.io.File;
 
 import processing.core.*;
 import processing.xml.*;
 
 import propinquity.hardware.*;
 
 import java.util.*;
 
 import codeanticode.glgraphics.*;
 
 /**
  * This class is the processing sketch at the center of the Propinquity software. It contains the main draw loop, instatiates all primary objects and handles game state and game flow.
  *
  */
 public class Propinquity extends PApplet implements PlayerConstants, LevelConstants {
 
 	/** Unique serialization ID. */
 	static final long serialVersionUID = 6340518174717159418L;
 	public static final int FPS = 30;
 
 	static boolean useSimulator = true;
 
 	//General/Util
 	HeapDebug heapDebug;
 	public Logger logger;
 
 	GameState gameState;
 
 	Vector<UIElement> uiElements;
 
 	Sounds sounds;
 	Hud hud;
 
 	GLGraphicsOffScreen offscreen;
 	GLTextureFilter blur, thres;
 
 	int backgroundColor;
 	boolean blankScreen;
 
 	//Xbee/Hardware
 	HardwareSimulator simulator; //Testing 
 
 	XBeeBaseStation xbeeBaseStation;
 	XBeeManager xbeeManager;
 
 	HardwareInterface hardware;
 
 	//Player/Player List/PlayerSelect
 	Player[] players;
 	PlayerList playerList;
 	PlayerSelect playerSelect;
 
 	//Level/Level Select
 	Level level;
 	Level[] levels;
 	LevelSelect levelSelect;
 
 	//Box 2D
 	public float worldSize = 2f;
 	PBox2D box2d;
 	Fences fences;
 
 	public void setup() {
 		size(1024, 768, GLConstants.GLGRAPHICS);
 
 		noCursor();
 
 		frameRate(FPS);
 		imageMode(PConstants.CENTER);
 		textureMode(PConstants.NORMAL);
 		hint(PConstants.ENABLE_OPENGL_4X_SMOOTH);
 
 		//Initialize the OpenGL stuff
 		offscreen = new GLGraphicsOffScreen(this, 1024, 768);
 		blur = new GLTextureFilter(this, "shaders/Blur.xml");
 		thres = new GLTextureFilter(this, "shaders/Thres.xml");
 		thres.setParameterValue("bright_threshold", 0.153f);
 
 		//General/Util
 		heapDebug = new HeapDebug();
 		logger = new Logger(this);
 
 		sounds = new Sounds(this);
 		hud = new Hud(this);
 
 		backgroundColor = Color.black().toInt(this);
 
 		//Xbee/Hardware
 		simulator = new HardwareSimulator(this);
 		
 		xbeeBaseStation = new XBeeBaseStation();
 		if(!useSimulator) xbeeBaseStation.scanBlocking(); //FIXME: Use nonblocking and hold packets until an Xbee has been found ...
 		xbeeManager = new XBeeManager(this, xbeeBaseStation);
 
 		if(useSimulator) hardware = simulator;
 		else hardware = xbeeBaseStation;
 
 		//Player/Player List
 		if(MAX_PLAYERS < 2) {
 			System.err.println("Fatal Error: MAX_PLAYERS must be at least 2");
 			System.exit(1);
 		}
 
 		players = new Player[MAX_PLAYERS];
 
 		try {
 			for(int i = 0;i < MAX_PLAYERS;i++) {
 				Patch[] patches = new Patch[PATCH_ADDR[i].length];
 
 				for(int j = 0;j < PATCH_ADDR[i].length;j++) {
 					patches[j] = new Patch(PATCH_ADDR[i][j], hardware);
 					hardware.addPatch(patches[j]);
 				}
 
 				Glove glove = new Glove(GLOVE_ADDR[i], hardware);
 				hardware.addGlove(glove);
 
 				Color color = PLAYER_COLORS[i];
 
 				players[i] = new Player(this, sounds, null, color, patches, glove);
 			}
 		} catch(Exception e) {
 			System.err.println("Fatal Error: Malformed data structures for player patches/gloves/colors");
 			System.err.println(e.getMessage());
 			System.exit(1);
 		}
 
 		playerList = new PlayerList(this, "player.lst");
 		playerSelect = new PlayerSelect(this, hud, players);
 		
 		//Box 2D
 		box2d = new PBox2D(this, (float) height/worldSize);
 		box2d.createWorld(-worldSize/2f, -worldSize/2f, worldSize, worldSize);
 		box2d.setGravity(0.0f, 0.0f);
 
 		fences = new Fences(this, box2d);
 		
 		//Level/Level Select
 		Vector<Level> tmp_levels = new Vector<Level>();
 		File file = new File(dataPath(LEVEL_FOLDER));
 
 		if(file.isDirectory()) {
 			String names[] = file.list();
 
 			// if extension is specify, parse out the rest
 			for(String name : names) {
 				if(name.lastIndexOf(".xml") == name.length()-4) {
 					try {
 //                      ProxLevel level = new ProxLevel(this, hud, sounds, LEVEL_FOLDER+name, players);
 						AccelLevel level = new AccelLevel(this, hud, sounds, LEVEL_FOLDER+name, players);
 						level.useBackgroundColor(true);
 						tmp_levels.add(level);
 					} catch(XMLException e) {
 						System.err.println("Warning: Level not built for file \""+name+"\" because of the following XMLException");
 						System.err.println(e.getMessage());
 					} catch(NullPointerException e) {
 						System.err.println("Error Building Level, following execption was thrown");
 						System.err.println(e.getMessage());
 						System.exit(1);
 					}
 				}
 			}
 		}
 
 		//TODO: Disabled for now
 		// tmp_levels.add(new BopperLevel(this, hud, sounds, "Besouro.mp3", players));
                 //		tmp_levels.add(new HealthLevel(this, hud, sounds, "Leila Came Round And We Watched A Video.mp3", players));
 
 		levels = tmp_levels.toArray(new Level[0]);
 		if(levels.length == 0) {
 			System.err.println("Fatal Error: No valid levels were built ... quitting");
 			System.exit(1);
 		}
 
 		for(Level level : levels) { 
 		  hardware.addProxEventListener(level);
           hardware.addAccelEventListener(level);
 		}
 		
 		levelSelect = new LevelSelect(this, hud, levels);
 
 		//General + Util	
 		uiElements = new Vector<UIElement>();
 		uiElements.add(xbeeManager);
 		uiElements.add(playerList);
 		uiElements.add(playerSelect);
 		uiElements.add(levelSelect);
 		for(Level level : levels) uiElements.add(level);
 		
 		changeGameState(GameState.XBeeInit);
 	}
 
 	public GLGraphicsOffScreen getOffscreen() {
 		return offscreen;
 	}
 
 	public void draw() {
 		if(blankScreen) {
 			background(0);
 			return;
 		}
 
 		if(gameState == GameState.Play) {
 			if(keyPressed) {
 				if(key == 'a') {
 					players[0].suppressPoints(true);
 				} else if(key == 's') {
 					players[1].suppressPoints(true);
 				} else if(key == 'd') {
 					players[0].suppressPoints(true);
 					players[1].suppressPoints(true);
 				}
 			} else {
 				players[0].suppressPoints(false);
 				players[1].suppressPoints(false);
 			}
 		}
 
 		background(backgroundColor);
 
 		if(gameState != GameState.PlayerList) translate(0, 0, -42); //Hack so 
 
 		hud.update(hud.getAngle() + HALF_PI, TWO_PI/10000f, TWO_PI/2000f);
 
 		GLTexture tex = offscreen.getTexture();
 
 		if(gameState == GameState.Play) {
 			for(int i = 0;i < 5;i++) tex.filter(blur, tex);
 			tex.filter(thres, tex);
 			tex.filter(blur, tex);
 		}
 		image(tex, width/2, height/2, offscreen.width, offscreen.height);
 
 		offscreen.beginDraw();
 		offscreen.clear(0, 0);
 		offscreen.endDraw();
 
 		for(UIElement u: uiElements) u.draw();
 
 		if(gameState == GameState.Play) box2d.step();
 
 		pushMatrix();
 		translate(100, 100);
 		simulator.draw();
 		popMatrix();
 		
 		logger.recordFrame();
 	}
 
 	public void changeGameState(GameState newState) {
 		for(UIElement u: uiElements) u.hide();
 		noCursor();
 
 		switch(newState) {
 			case XBeeInit: {
 				xbeeManager.show();
 				break;
 			}
 
 			case PlayerList: {
 				playerList.reset();
 				playerList.show();
 				cursor(ARROW);
 				break;
 			}
 
 			case PlayerSelect: {
 				playerSelect.setPlayerNames(playerList.getPlayerNames());
 				playerSelect.reset();
 				playerSelect.show();
 				break;
 			}
 
 			case LevelSelect: {
 				levelSelect.reset();
 				levelSelect.show();
 				break;
 			}
 
 			case Play: {
 				blankScreen = false;
 				level = levelSelect.getSelectedLevel();
 				level.reset();
 				level.show();
 				break;
 			}
 		}
 
 		gameState = newState;
 	}
 
 	public void setBackgroundColor(Color backgroundColor) {
 		this.backgroundColor = backgroundColor.toInt(this);
 	}
 
 	public void controlEvent(ControlEvent event) {
 		switch(gameState) {
 			case XBeeInit: {
 				xbeeManager.controlEvent(event);
 				break;
 			}
 
 			case PlayerList: {
 				playerList.controlEvent(event);
 				break;
 			}
 		}
 	}
 
 	public void keyPressed() {
 		if(gameState != GameState.PlayerList) {
 			switch(key) {
 				case ENTER:
 				case ' ': {
 					if(blankScreen) {
 						blankScreen = false;
 						return;
 					}
 					break;
 				}
 
 				case 'n': {
 					if(simulator.isVisible()) simulator.hide();
 					else simulator.show();
 					break;
 				}
 
 				case 'h': {
 					if(heapDebug.isRunning()) heapDebug.stop();
 					else heapDebug.start();
 					break;
 				}
 
 				case 'b': {
 					if(gameState == GameState.PlayerSelect || gameState == GameState.LevelSelect) blankScreen = !blankScreen;
 					break;
 				}
 			}
 		}
 
 		switch(gameState) {
 			case XBeeInit: {
 				xbeeManager.keyPressed(key, keyCode);
 				break;
 			}
 
 			case PlayerList: {
 				playerList.keyPressed(key, keyCode);
 				break;
 			}
 
 			case PlayerSelect: {
 				playerSelect.keyPressed(key, keyCode);
 				break;
 			}
 
 			case LevelSelect: {
 				levelSelect.keyPressed(key, keyCode);
 				break;
 			}
 
 			case Play: {
 				level.keyPressed(key, keyCode);
 				break;
 			}
 		}
 
 	}
 
 	public void stop() {
 		logger.close();
 		for(Player player : players) player.reset();
 		for(Level level : levels) level.close();
 	}
 
 	static public void main(String args[]) {
 		if(args.length > 0 && args[0].equals("w")) {
 			PApplet.main(new String[] { "propinquity.Propinquity" });
 		} else if(args.length > 0 && args[0].equals("s")) {
 			useSimulator = true;
 			PApplet.main(new String[] { "propinquity.Propinquity" });
 		} else {
 			PApplet.main(new String[] { "--exclusive", "--present", "propinquity.Propinquity" });
 		}
 	}
 }
