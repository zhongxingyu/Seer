 package com.devcamp.cadejo.screens;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.InputProcessor;
 import com.badlogic.gdx.Screen;
 import com.badlogic.gdx.graphics.GL10;
 import com.devcamp.cadejo.MyGame;
 import com.devcamp.cadejo.ScoreManager;
 import com.devcamp.cadejo.actors.Character.State;
 import com.devcamp.cadejo.actors.CharacterController;
 import com.devcamp.cadejo.world.World;
 import com.devcamp.cadejo.world.WorldRenderer;
 
 public class MainGameScreen implements Screen, InputProcessor{
 
 	private World world;
 	private WorldRenderer renderer;
 	private CharacterController controller;
 	private ScoreManager scoreManager;
	
 	private int width, height;
 	
 	private MyGame g;
 
 	public MainGameScreen(MyGame g){
 		this.g = g;
 	}
 
 	@Override
 	public void render(float delta) {
		if(!world.getMainCharacter().getState().equals(State.COLLISION)){
 			Gdx.gl.glClearColor(0f, 0f, 0f, 1);
 			Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
 			world.update(delta);
 			controller.update(delta);
 			renderer.render();
 		}
		else{
 			scoreManager.stopGame();
 			g.showScore(scoreManager.getScore());
 		}
 		
 	}
 
 	@Override
 	public void resize(int width, int height) {
 		renderer.setSize(width, height);
 		this.width = width;
 		this.height = height;
 		
 	}
 
 	@Override
 	public void show() {
		world = new World();
 		scoreManager = new ScoreManager();
 		renderer = new WorldRenderer(world, true, scoreManager);
 		controller = new CharacterController(world);
 		scoreManager.startGame();
 		Gdx.input.setInputProcessor(this);
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
 
 	@Override
 	public boolean keyDown(int keycode) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public boolean keyUp(int keycode) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public boolean keyTyped(char character) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
 		controller.touchDown();
 		return true;
 	}
 
 	@Override
 	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
 		controller.touchUp();
 		return true;
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
 
 }
