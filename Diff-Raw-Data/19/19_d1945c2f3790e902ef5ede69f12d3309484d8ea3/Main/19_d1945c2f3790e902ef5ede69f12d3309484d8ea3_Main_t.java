 package com.me.mygdxgame;
 
 import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
 import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
 
 public class Main {
 	public static void main(String[] args) {
 		LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
 		cfg.title = "Gentleman-Fighter";
 		cfg.useGL20 = false;
		cfg.width = GentlemanFighterGame.TARGET_WIDTH;
		cfg.height = GentlemanFighterGame.TARGET_HEIGHT;
 		
 		new LwjglApplication(new GentlemanFighterGame(), cfg);
 	}
 }
