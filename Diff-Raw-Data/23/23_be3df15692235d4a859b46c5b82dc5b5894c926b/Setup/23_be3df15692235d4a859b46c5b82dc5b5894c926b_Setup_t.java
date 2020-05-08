 package com.cnia.angelworks.example;
 
 import it.randomtower.engine.ResourceManager;
 
 import java.io.IOException;
 
 import org.newdawn.slick.AppGameContainer;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.state.StateBasedGame;
 import org.newdawn.slick.util.Log;
 
 public class Setup extends StateBasedGame {
 
 	private static boolean resourcesInited;
 	
 	public Setup(String name) {
 		super(name);
 		// TODO Auto-generated constructor stub
 	}
 	
	/*
	 * the initStatesList method is used to hold all your worlds together and has you put in a reference number for each world
	 * so you can change through them accordingly.
	 */
	@Override
	public void initStatesList(GameContainer gc) throws SlickException {
		initResources();
		addState(new Level(0, gc));

	}
	
 	public static void initResources() throws SlickException {
 		  if (resourcesInited)
 		   return;
 		  try {
 		   ResourceManager.loadResources("res/resources.xml");
 		  } catch (IOException e) {
 		   Log.error("failed to load resource file 'res/resources.xml': "
 		     + e.getMessage());
 		   throw new SlickException("Resource loading failed!");
 		  }
 		 
 		  resourcesInited = true;
 		 }
 	
 	/*
 	 * Main method creates an instance of the AppGameContainer to help set up your game's parameters
 	 */
 	public static void main(String[] args) throws SlickException  {
 		try {
 			AppGameContainer agc = new AppGameContainer(
 					new Setup("Example"));
 			agc.setDisplayMode(640, 400, false);
 			agc.setTargetFrameRate(60);
 			agc.setVSync(true);
 			//agc.setFullscreen(true);
 			agc.setShowFPS(false);
 			agc.start();
 		} catch (SlickException e) {
 			e.printStackTrace();
 		}
 	}
 
 }
