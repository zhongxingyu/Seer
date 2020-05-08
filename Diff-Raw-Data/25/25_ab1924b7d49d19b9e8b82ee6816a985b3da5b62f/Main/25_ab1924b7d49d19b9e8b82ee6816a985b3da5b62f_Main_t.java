 package com.glowman.spaceunit;
 
 import com.badlogic.gdx.Game;
 import com.badlogic.gdx.Screen;
 import com.badlogic.gdx.graphics.FPSLogger;
 import com.glowman.spaceunit.Assets;
 import com.glowman.spaceunit.MainScreen;
 import com.glowman.spaceunit.game.core.FPSViewer;
 
 
 public class Main extends Game {
 	
 	//public static FPSLogger FPS;
 	
 	Screen mainScreen;
 	
 	@Override
 	public void create () {
 		Assets.load();
		FPSViewer.init(true);
 		
 		mainScreen = new MainScreen(this);
 		
 		setScreen(mainScreen);
 		
 		//FPS = new FPSLogger();
 		
 	}
 	
 	@Override
 	public void render() {
 		super.render();
 		//FPS.log();
 	}
 	
 	@Override
 	public void dispose () {
 		super.dispose();
 
 		getScreen().dispose();
 	}
 
 }
