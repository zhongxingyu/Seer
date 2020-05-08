 package com.detourgames.raw;
 
 import com.badlogic.gdx.ApplicationListener;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.GL20;
 
 public class Game implements ApplicationListener {
 
 	GameLoop mGameLoop;
 	GameManager mGameManager = GameManager.getGameManager();
      
      SpriteFactory mSpriteFactory;
      SpriteSheet mSpriteSheet;
 	
 	@Override
 	public void create() {
 		mGameLoop = new GameLoop();
 		mGameManager.loadLevel(1);
 		
 	}
 
 	@Override
 	public void dispose() {
 	}
 	
 	@Override
 	public void render() {
 		
 		mGameLoop.tick();
 		// TODO either allow this to tick my game loop or
 		// create my game loop as a thread and start it in resume().
 		// The latter would require passing the OpenGL context to another
 		// thread. That topic is covered by LibGdx here:
 		// http://code.google.com/p/libgdx/wiki/ApplicationThreading
 	}
 
 	@Override
 	public void resize(int width, int height) {
		mGameManager.createCamera(width, height);
 		
 		Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
 		Gdx.gl.glEnable(GL20.GL_BLEND);
 		Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
 		Gdx.gl.glClearDepthf(1.0f);
 		Gdx.gl.glDepthFunc(GL20.GL_LEQUAL);
 		Gdx.gl.glDepthMask(true);
 		Gdx.gl.glDisable(GL20.GL_DITHER);
 		//Gdx.gl.glEnable(GL20.GL_CULL_FACE);
 		Gdx.gl.glCullFace(GL20.GL_BACK);
 		
 		Gdx.gl.glTexParameterf(GL20.GL_TEXTURE_2D, GL20.GL_TEXTURE_MIN_FILTER, GL20.GL_LINEAR);
 		Gdx.gl.glTexParameterf(GL20.GL_TEXTURE_2D, GL20.GL_TEXTURE_MAG_FILTER, GL20.GL_LINEAR);
 		Gdx.gl.glTexParameterf(GL20.GL_TEXTURE_2D, GL20.GL_TEXTURE_WRAP_S, GL20.GL_CLAMP_TO_EDGE);
 		Gdx.gl.glTexParameterf(GL20.GL_TEXTURE_2D, GL20.GL_TEXTURE_WRAP_T, GL20.GL_CLAMP_TO_EDGE);
 	}
 
 	@Override
 	public void pause() {
 	}
 
 	@Override
 	public void resume() {
 	}
 }
