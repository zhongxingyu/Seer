 package trivia;
 
 import java.awt.Graphics;
 import trivia.Trivia.GameModes;
 
 /**
  * @author Cody Swendrowski
  *
  */
 public class GameEngine {
 	
 	private GameModes mode;
 	private Actors actors;
 	private int timer = 0;
 	private boolean debugMode;
 	
 	public GameEngine(Actors actors, Boolean debug)
 	{
 		mode = GameModes.mainMenu;
 		this.actors = actors;
 		debugMode = debug;
 	}
 	
 	public void run() {
 
 		timer++;
 		
 		if (mode == GameModes.mainMenu)
 		{
 			//mainMenu.run();
 		}
 		else if (mode == GameModes.play)
 		{
 			//play.run();
 		}
 		else if (mode == GameModes.pause)
 		{
 			//pause.run();
 		}
 		else
 		{
			log("Unknown game mode.");
 		}
 		
 	}
 
 	public void paint(Graphics g) {
 		
 		if (mode == GameModes.mainMenu)
 		{
 			//mainMenu.paint();
 		}
 		else if (mode == GameModes.play)
 		{
 			//play.paint();
 		}
 		else if (mode == GameModes.pause)
 		{
 			//pause.paint();
 		}
 		else
 		{
			log("Unknown game mode.");
 		}
 	}
 	
 	public void setMode(GameModes newMode)
 	{
 		mode = newMode;
 	}
	
	/**
	 * Debug tool.
	 * Used to print a String if Debug mode is enabled.
	 * @param s String to print.
	 */
	private void log(String s) {
		if (debugMode)
		{
			System.out.println(s);
		}
	}
 }
