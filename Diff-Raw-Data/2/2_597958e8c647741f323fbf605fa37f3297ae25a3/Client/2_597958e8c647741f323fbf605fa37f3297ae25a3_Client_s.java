 package tools;
 
 import processing.core.PApplet;
 
 import engine.GameObject;
 import engine.Level;
 import engine.events.Event;
 import engine.events.EventData;
 import engine.events.EventManager;
 import engine.network.Connection;
 import engine.utils.Logger;
 import engine.utils.Renderer;
 
 /**
 * @author Covar
  *
  */
 public class Client extends PApplet {
 
 	/**
 	 * 
 	 */
 	public void setup() {
 		this.name = "" + System.currentTimeMillis();
 		
 		size(640,480, P2D);  // screen size of 640x480 gives 40x30 tilemap
 		frameRate(30);
 		this.eventManager = EventManager.getInstance();
 		
 		new Logger("out2.log");
 		new Renderer(this);
 		connection = new Connection("localhost", 10040, Connection.CLIENT);
 		this.connection.send(new Event("null",null));
 		this.connection.send(new Event("addplayer", new EventData(name)));
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
 		
 		keys[keyCode] = false;
 	}
 
 	public void draw() {
 		GameObject.gameTime++;
 		this.eventManager.sendEvent("clear", null);
 		
 		if(checkKey('a') || checkKey('A')) {
 		    this.eventManager.sendEvent("move", new EventData(name,Level.LEFT));
 		}
 		if(checkKey('d') || checkKey('D')) {
 		    this.eventManager.sendEvent("move", new EventData(name,Level.RIGHT));
 		}
 		if(checkKey(' ')) {
 			this.eventManager.sendEvent("move", new EventData(name,Level.UP));
 		}
 		
 		this.eventManager.process();
 	}
 	
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		PApplet.main(new String[] { "tools.Client" });
 
 	}
 	private static final long serialVersionUID = 100427647698574158L;
 	
 	private String name;
 	private EventManager eventManager;
 	private Connection connection;
 	private boolean[] keys = new boolean[526];
 
 }
