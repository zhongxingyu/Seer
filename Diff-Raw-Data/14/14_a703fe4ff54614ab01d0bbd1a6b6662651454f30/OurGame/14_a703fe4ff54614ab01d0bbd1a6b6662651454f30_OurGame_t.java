 package com.teamcoffee.game;
 
 import com.badlogic.gdx.Game;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.GL10;
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.Sprite;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 
 public class OurGame extends Game {
 	private OrthographicCamera camera;
 	private SpriteBatch batch;
 	private Texture texture;
 	private Sprite sprite;
 	
 	@Override
 	public void create() {		
 		Texture.setEnforcePotImages(false);
 		setScreen(new GameScreen(this));
 	}
 
 	@Override
 	public void dispose() {
		//batch.dispose();
		//texture.dispose();
 	}
 }
