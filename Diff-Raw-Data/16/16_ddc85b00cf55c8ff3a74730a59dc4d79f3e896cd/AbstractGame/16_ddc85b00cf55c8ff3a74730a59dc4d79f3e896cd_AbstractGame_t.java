 import java.awt.Graphics;
 
//each game should extend AbstractGame 
 public abstract class AbstractGame 
 {
	//update ALL game objects' (Player, AI, etc) here
 	public abstract void update();
 	
	//draw ALL game stuff here
 	public abstract void draw( Graphics g );
 }
