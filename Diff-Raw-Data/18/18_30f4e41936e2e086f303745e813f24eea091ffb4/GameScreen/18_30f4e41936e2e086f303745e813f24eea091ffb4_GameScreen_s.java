 package com.me.Roguish.Screens;
 
 import com.me.Roguish.Roguish;
 import com.me.Roguish.Model.Level;
 import com.me.Roguish.View.LevelRenderer;
 import com.me.Roguish.Controller.LevelController;
 
 import com.badlogic.gdx.InputProcessor;
 import com.badlogic.gdx.Screen;
 import com.badlogic.gdx.Application.ApplicationType;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Input.Keys;
 import com.badlogic.gdx.graphics.GL10;
 
public class GameScreen implements Screen, InputProcessor {
 	
 	private Level level;
 	private LevelRenderer renderer;
 	private LevelController controller;
 
 	private int width, height;

 	
 	@Override
 	public void show() {
 		level = new Level();
 		renderer = new LevelRenderer(level, false);
 		controller = new LevelController(level);
 		Gdx.input.setInputProcessor(this);
 	}
 
 	@Override
 	public void hide() {
 		Gdx.input.setInputProcessor(null);
 
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
 		Gdx.input.setInputProcessor(null);
 
 	}
 	
 	
 	@Override
 	public boolean keyDown(int keycode) {
 		if (keycode == Keys.LEFT)
 			controller.leftPressed();
 		if (keycode == Keys.RIGHT)
 			controller.rightPressed();
 		if (keycode == Keys.UP)
 			controller.upPressed();
 		if (keycode == Keys.DOWN)
 			controller.downPressed();
 		return true;
 	}
 
 	@Override
 	public boolean keyUp(int keycode) {
 		if (keycode == Keys.LEFT)
 			controller.leftReleased();
 		if (keycode == Keys.RIGHT)
 			controller.rightReleased();
 		if (keycode == Keys.UP)
 			controller.upReleased();
 		if (keycode == Keys.DOWN)
 			controller.downReleased();
 		return true;
 	}
 
 	@Override
 	public boolean keyTyped(char character) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public boolean touchDragged(int screenX, int screenY, int pointer) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public boolean mouseMoved(int screenX, int screenY) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public boolean scrolled(int amount) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public void render(float delta) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void resize(int width, int height) {
 		renderer.setSize(width, height);
 		this.width = width;
 		this.height = height;
 
 	}
 
 
 
 }
