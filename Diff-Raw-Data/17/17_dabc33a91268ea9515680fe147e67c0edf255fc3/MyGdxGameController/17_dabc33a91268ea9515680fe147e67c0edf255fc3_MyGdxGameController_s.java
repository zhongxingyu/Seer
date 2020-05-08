 package com.dat255_group3.controller;
 
 import com.badlogic.gdx.Game;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.GL10;
 import com.dat255_group3.model.MyGdxGame;
 
 
 public class MyGdxGameController extends Game {
 	private MyGdxGame myGdxGame;
 	private InGameController inGameController;
 	private PlayerController playerController;
 	
 	@Override
 	public void create() {
 		//create other the sceenes and the player and the gameModel
 		this.myGdxGame = new MyGdxGame();
 		this.playerController = new PlayerController(this);
 		
 		
 		//go to the first screen
 		setScreen(new InGameController(this));
 		
 		
	}

	@Override
	public void dispose() {
 		
 	}
 
 	@Override
	public void render() {	
		//show a red screen
		Gdx.gl.glClearColor(1, 0, 0, 1);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		
		if (Gdx.input.justTouched()) {
			setScreen(new InGameController(this));
		}
		
 		
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
