 package com.me.Roguish;
 
 import com.me.Roguish.Screens.*;
 
 import com.badlogic.gdx.Game;
 import com.badlogic.gdx.Screen;
 import com.badlogic.gdx.graphics.FPSLogger;
 
 public class Roguish extends Game {
 	boolean firstTimeCreate = true;
 	FPSLogger fps;
 	
 	@Override
 	public void create() {	
		setScreen(new GameScreen(this));
 		fps = new FPSLogger();
 	}
 
 	@Override
 	public void dispose() {
 		super.dispose();
 		getScreen().dispose();
 	}
 
 	@Override
 	public void render() {		
 		super.render();
 		fps.log();
 	}
	
	
 
 }
