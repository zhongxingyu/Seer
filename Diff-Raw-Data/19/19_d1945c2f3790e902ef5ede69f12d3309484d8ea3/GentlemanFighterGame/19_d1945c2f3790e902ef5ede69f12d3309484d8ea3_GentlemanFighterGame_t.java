 package com.me.mygdxgame;
 
 import com.badlogic.gdx.ApplicationListener;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.GL10;
 import com.me.mygdxgame.gameplay.GameplayScreen;
 import com.me.mygdxgame.screens.GDXLogoScreen;
 import com.me.mygdxgame.screens.ScreenManager;
 
 public class GentlemanFighterGame implements ApplicationListener {
	
	public static final int TARGET_WIDTH = 1280, TARGET_HEIGHT = 720;
 
 	private ScreenManager screenManager = null;
 	
 	@Override
 	public void create() {		
 		this.screenManager = new ScreenManager();
 		
 		this.screenManager.addScreen(new GameplayScreen());
 		this.screenManager.addScreen(new GDXLogoScreen());
 	}
 
 	@Override
 	public void dispose() {
 		this.screenManager.dispose();
 	}
 
 	@Override
 	public void render() {
 		this.screenManager.update();
 		
 		Gdx.gl.glClearColor(1, 1, 1, 1);
 		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
 		
 		this.screenManager.render();
 	}
 
 	@Override
 	public void resize(int width, int height) {
 	}
 
 	@Override
 	public void pause() {
 	}
 
 	@Override
 	public void resume() {
 	}
 }
