 package com.aquasheep.Static.screens;
 
 import com.aquasheep.Static.StaticGame;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Screen;
 import com.badlogic.gdx.graphics.GL10;
 
 public abstract class AbstractScreen implements Screen {
 
 	private StaticGame game;
 	
 	public AbstractScreen(StaticGame game) {
 		this.game = game;
 	}
 	
 	@Override
 	public void show() {
		
 	}
 	
 	@Override
 	public void render(float delta) {
		Gdx.gl.glClearColor(1f,0.5f,1.0f,1f);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
 	}
 	
 
 	@Override
 	public void resize(int width, int height) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void hide() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void pause() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void resume() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void dispose() {
 		// TODO Auto-generated method stub
 		
 	}
 }
