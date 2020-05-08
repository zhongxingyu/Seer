 package com.bengreenier.smashgrab.main;
 
 import java.util.ArrayList;
 
 import org.newdawn.slick.AppGameContainer;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.state.StateBasedGame;
 
 import com.bengreenier.slick.tiling.TileSystem;
 import com.bengreenier.slick.tiling.TileSystem.Tile;
 import com.bengreenier.smashgrab.states.Build;
 import com.bengreenier.smashgrab.states.GameOver;
 import com.bengreenier.smashgrab.states.MainMenu;
 import com.bengreenier.smashgrab.states.Paused;
 import com.bengreenier.smashgrab.states.Run;
 import com.bengreenier.smashgrab.towers.EndPoint;
 import com.bengreenier.smashgrab.util.TileUserData;
 
 import com.bengreenier.slick.util.GameObject;
 
 public class Main {
 
 	public static Main core;
 	public static void main(String[] args) throws Exception { core = new Main(); core.agc.start(); }
 	
 	public AppGameContainer agc;
 	public StateBasedGame sfg;
 	public TileSystem tileSystem;
 	
 	//store our static int id's here
 	public static class ID{
 		public static int PAUSED = 0;
 		public static int MAINMENU = 1;
 		public static int BUILD = 2;
 		public static int RUN = 3;
 		public static int GAMEOVER =4;
 	}
 	
 	public static final int TILESIZE = 50;
 	
 	private Main() throws Exception
 	{
 		tileSystem = new TileSystem(16, 10, new Tile(TILESIZE, TILESIZE,0));
 		
 		sfg = new StateBasedGame("SmashGrab"){
 
 			@Override
			public void initStatesList(GameContainer arg0) throws SlickException {
<<<<<<< HEAD
 				//addState(new Paused(ID.PAUSED));
 				//addState(new MainMenu(ID.MAINMENU));
				addState(new GameOver(ID.GAMEOVER));
=======
 				addState(new Paused(ID.PAUSED));
				addState(new MainMenu(ID.MAINMENU));
>>>>>>> fixed buttons and added background to main menu
 				addState(new Build(ID.BUILD));
 				addState(new Run(ID.RUN));
 				enterState(ID.PAUSED);
 				
 			}
 			
 		};
 
 		agc = new AppGameContainer(sfg);
 		agc.setDisplayMode(800, 600, false);
 		agc.setShowFPS(false);
 		agc.setVerbose(false);
 	}
 
 	public void promptShutdown()
 	{
 		//for now, just exit. but later, this might be configurable to display a popup first or something
 		agc.exit();
 	}
 }
