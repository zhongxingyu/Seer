 package com.fightthetoast.superstore;
 
 import org.isjaki.GameBase;
 
 import com.fightthetoast.superstore.screen.Screen;
 
 public class Game extends GameBase{
 	
 
 	public static final int DEFAULT_WIDTH = 800, DEFAULT_HEIGHT = 600;
 	public static final String GAME_TITLE = "Superstore Tycoon";
 	
 	protected Screen currentScreen = null;
 	
 	
 	public Game(String name, int width, int height) {
 		super(name, width, height);
 		start();
 	}
 
 	
 	public static void main(String[] args)
 	{
 		new Game(GAME_TITLE, DEFAULT_WIDTH, DEFAULT_HEIGHT);
 	}
 	
 	@Override
 	protected void init()
 	{
 		
 	}
 	
 	
 	@Override
 	protected void render()
 	{
		currentScreen.render();
 	}
 	
 	
 	protected void setScreen(Screen screen)
 	{
 		currentScreen.onScreenDestroy(screen);
 		screen.onScreenCreate(currentScreen);
 		currentScreen = screen;
 
 	}
 
 	
 	
 	
 	
 	
 }
