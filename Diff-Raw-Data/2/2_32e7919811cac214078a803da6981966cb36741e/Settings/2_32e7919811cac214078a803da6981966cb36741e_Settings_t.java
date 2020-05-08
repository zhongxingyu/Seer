 package model;
 
 import org.lwjgl.input.Keyboard;
 
 public class Settings {
 	
 	private static Settings settings;
 	
 	public static final String resources = System.getProperty("user.dir") + "/res/";
 	
 	// Represents the respective key's position in the controlKeys-array
 	public static final int MOVE_LEFT = 0;
 	public static final int MOVE_RIGHT = 1;
 	public static final int MOVE_UP = 2;
 	public static final int MOVE_DOWN = 3;
 	
 	private int windowWidth;
 	private int windowHeight;
 	
 	private int walkButton;
 	private int actionButton;
 	private boolean mouseWalk = false;
 	
 	private int[] controlKeys;
 		
 	public Settings() {
 		windowWidth = 1280;
 		windowHeight = 800;
 		
 		walkButton = 0;
 		actionButton = 1;
 		controlKeys = new int[4];
 		setControlKeys(new int[] {Keyboard.KEY_A, Keyboard.KEY_D, Keyboard.KEY_W, Keyboard.KEY_S});
 	}
 	
 	public synchronized static Settings getSettings() {
 		if(settings == null) {
 			settings = new Settings();
 		}
 		
 		return settings;
 	}
 	
 	public int getWindowWidth() {
 		return windowWidth;
 	}
 	
 	public int getWindowHeight() {
 		return windowHeight;
 	}
 	
 	public int getControlKey(int key) {
 		return controlKeys[key];
 	}
 	
 	public int getWalkButton() {
 		return walkButton;
 	}
 	
 	public int getActionButton() {
 		return actionButton;
 	}
 	
 	public boolean getMouseWalk() {
 		return mouseWalk;
 	}
 	
 	public int getNumberOfKeys() {
 		return controlKeys.length;
 	}
 	
 	public void setWindowWidth(int width) {
 		windowWidth = width;
 	}
 	
 	public void setWindowHeight(int height) {
 		windowHeight = height;
 	}
 	
 	public void setControlKey(int key, int index) {
 		controlKeys[index] = key;
 	}
 	
 	public void setControlKeys(int[] controlKeys) {
 		for(int i = 0; i < controlKeys.length; i++) {
 			this.controlKeys[i] = controlKeys[i];
 		}
 	}
 	
 	public void setMouseButtons() {
 		if(walkButton == 0) {
 			walkButton = 1;
 			actionButton = 0;
 		} else {
 			walkButton = 0;
 			actionButton = 1;
 		}
 	}
 	
 	public void setMouseWalk(boolean mouseWalk) {
 		this.mouseWalk = mouseWalk;
 	}
 	
 }
