 package engine;
 
 import engine.character.Player;
 import engine.events.*;
 import engine.menu.DirList;
 import engine.network.*;
 import engine.utils.ConfigManager;
 import engine.utils.Logger;
 import engine.utils.Recorder;
 import engine.utils.Renderer;
 import engine.utils.Replay;
 import processing.core.*;
 
 /**
  * @author Charles Covar (covar1@gmail.com)
  * TODO: use the EventManager to better modularize the subsystems 
  */
 public class ProcessingSketch extends PApplet {
 	/**
 	 * Sets the screen size, locks the framerate and initializes values.
 	 */
 	public void setup() {
 		ConfigManager cm = ConfigManager.getInstance();
		size(Integer.parseInt(cm.getOption("width")),Integer.parseInt(cm.getOption("height")), P2D);  // screen size of 640x480 gives 40x30 tilemap
 		frameRate(30);
 		
 		this.eventManager = EventManager.getInstance();
 		
 		this.name = "" + System.currentTimeMillis();
 		
 		this.logger = new Logger();
 		this.recorder = new Recorder();
 		this.renderer = new Renderer(this);
 		this.renderer.setEventFlags(true, false);
 		this.replay = null;
 		
 		player = new Player();
 		player.setParam("name", name);
 		
 		this.server = new Server();
 	}
 	
 	/**
 	 * Checks to see if a key on the keyboard has been pressed
 	 * @param k char of the key we want to check
 	 * @return true if the key is pressed, else false
 	 */
 	public boolean checkKey(char k) { 
 		if (keys.length >= k) {
 			return keys[k];
 		}
 		return false;
 	}
 	
 	public void resetKeys() {
 		for(int i=0;i<this.keys.length;i++)
 			this.keys[i] = false;
 	}
 
 	/**
 	 * Event trigger. When a key is pressed the value of the key is set to true in our array
 	 */
 	public void keyPressed() {
 		keys[keyCode] = true;
 	}
 
 	/**
 	 * Event trigger. Sets the value of the key in our array to false.
 	 */
 	public void keyReleased() {		
 		if((keyCode == 't' || keyCode == 'T') && GameObject.getGameState() == GameObject.GAME_STATE_REPLAY)
 			this.replay.toggleSpeed();
 		if(keyCode == ' ') {
 			if(GameObject.getGameState() == GameObject.GAME_STATE_MENU && checkKey(' ')) {
 				this.game = new Game(DirList.getInstance().getSelected(), this.player);
 				this.currentLevel = game.nextLevel();
 				this.eventManager.sendEvent("gamestatechange", new EventData("",GameObject.GAME_STATE_LEVEL));
 				resetKeys();
 			} else if(GameObject.getGameState() == GameObject.GAME_STATE_END && checkKey(' ')) {
 				this.eventManager.sendEvent("gamestatechange", new EventData("",GameObject.GAME_STATE_MENU));
 				resetKeys();
 			}
 		}
 		if(keyCode == 's' || keyCode == 'S' && GameObject.getGameState() == GameObject.GAME_STATE_MENU) {
 			DirList.getInstance().nextMenuItem();
 		}
 		if(keyCode == 'w' || keyCode == 'W' && GameObject.getGameState() == GameObject.GAME_STATE_MENU) {
 			DirList.getInstance().previousMenuItem();
 		}
 		
 		if(keyCode == 'g' || keyCode == 'G' && GameObject.getGameState() == GameObject.GAME_STATE_LEVEL) {
 				currentLevel.addPlayer(new Player());
 		}
 		
 		keys[keyCode] = false;
 	}
 	
 	/**
 	 * Main game loop. Processing will call this method as often as the frame rate calls for it.
 	 */
 	public void draw() {	
 		GameObject.gameTime++;	// this should be the only write to this variable
 		this.eventManager.sendEvent("clear", null);
 				
 		switch (GameObject.getGameState()) {
 		case GameObject.GAME_STATE_MENU:
 			this.renderer.setEventFlags(true, false);
 			this.eventManager.setEventFlags(true, false);
 			DirList.getInstance().draw();			
 			break;
 		case GameObject.GAME_STATE_LEVEL:
 			this.renderer.setEventFlags(true, true);
 			this.eventManager.setEventFlags(true, true);
 			if(currentLevel == null) {
 				this.eventManager.sendEvent("gamestatechange", new EventData("",GameObject.GAME_STATE_END));
 				resetKeys();
 				break;
 			}
 			if(currentLevel.reachedVictory()) {
 				this.eventManager.sendEvent("gamestatechange", new EventData("",GameObject.GAME_STATE_REPLAY));
 				resetKeys();
 				break;
 			}
 			
 			if(checkKey('a') || checkKey('A')) {
 			    currentLevel.movePlayer(name,Level.LEFT);
 			}
 			if(checkKey('d') || checkKey('D')) {
 			    currentLevel.movePlayer(name,Level.RIGHT);
 			}
 			if(checkKey(' ')) currentLevel.movePlayer(name,Level.UP);
 			
 			currentLevel.update();
 			
 			this.eventManager.sendEvent("text", new EventData("LIVE",10,20));
 			
 			currentLevel.draw();
 			break;
 		case GameObject.GAME_STATE_REPLAY:
 			this.renderer.setEventFlags(true, false);
 			this.eventManager.setEventFlags(true, false);
 			if(this.replay == null)
 				this.replay = new Replay("replay" + this.recorder.gUId);
 			if(!this.replay.isDone()) {
 				// update and draw the replay
 				if(this.replay.getSpeed() == Replay.HALF) {
 					frameRate(15);
 					this.eventManager.sendEvent("text", new EventData("REPLAY 1/2x",10,20));
 				}else if (this.replay.getSpeed() == Replay.NORMAL) {
 					frameRate(30);
 					this.eventManager.sendEvent("text", new EventData("REPLAY 1x",10,20));
 				} else if (this.replay.getSpeed() == Replay.DOUBLE) {
 					frameRate(60);
 					this.eventManager.sendEvent("text", new EventData("REPLAY 2x",10,20));
 				}
 				this.replay.update();
 				this.replay.draw();
 			} else {
 				frameRate(30);
 				currentLevel = game.nextLevel();
 				if(currentLevel == null) {
 					this.eventManager.sendEvent("gamestatechange", new EventData("",GameObject.GAME_STATE_END));
 				}
 				else
 					this.eventManager.sendEvent("gamestatechange", new EventData("",GameObject.GAME_STATE_LEVEL));
 				resetKeys();
 			}
 			break;
 		case GameObject.GAME_STATE_END:
 			this.renderer.setEventFlags(true, false);
 			this.eventManager.setEventFlags(true, false);
 			this.eventManager.sendEvent("text", new EventData("YOU WIN",302,235));
 			break;
 		}
 
 		this.eventManager.process();
 	}
 	
 	/**
 	 * Main method of the engine. Creates a new Processing Applet and passes our ProcessingSketch class
 	 * @param args
 	 */
 	public static void main(String [] args) {
 		PApplet.main(new String[] { "engine.ProcessingSketch" });
 	}
 	
 	private Player player;
 	private Game game;
 	private Level currentLevel;
 	
 	private EventManager eventManager;
 	@SuppressWarnings("unused")
 	private Logger logger;
 	private Recorder recorder;
 	private Renderer renderer;
 	private Replay replay;
 
 	@SuppressWarnings("unused")
 	private Server server;
 	
 	private String name;
 	
 	private boolean[] keys = new boolean[526];
 	
 	private static final long serialVersionUID = -6142886135378656379L;
 }
