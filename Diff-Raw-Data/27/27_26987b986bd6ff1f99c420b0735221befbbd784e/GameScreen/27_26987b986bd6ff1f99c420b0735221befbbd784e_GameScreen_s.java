 package com.me.mycoolgame.screens;
 
 import com.badlogic.gdx.Application.ApplicationType;
 import com.badlogic.gdx.Game;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.InputMultiplexer;
 import com.badlogic.gdx.InputProcessor;
 import com.badlogic.gdx.Screen;
 import com.badlogic.gdx.Input.Keys;
 import com.badlogic.gdx.graphics.GL10;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.scenes.scene2d.Stage;
 import com.badlogic.gdx.scenes.scene2d.ui.Skin;
 import com.badlogic.gdx.scenes.scene2d.ui.Touchpad;
 import com.me.mycoolgame.controller.PlayerController;
 import com.me.mycoolgame.model.World;
 import com.me.mycoolgame.view.WorldRenderer;
 
 public class GameScreen implements Screen, InputProcessor {
 
 	private Game game;
 	
 	private SpriteBatch spriteBatch;
 	private World world;
 	private WorldRenderer renderer;
 	private PlayerController controller;
 	
 	// The stage is our HUD
 	private Stage stage;
 	private Touchpad touchpad;
 	
 	public GameScreen(Game game) {
 		this.game = game;
 		spriteBatch = new SpriteBatch();
 	}
 	
 	@Override
 	public boolean keyDown(int keycode) {
 		if (keycode == Keys.W) {
 			controller.upPressed();
 		}
 
 		if (keycode == Keys.D) {
 			controller.rightPressed();
 		}
 
 		if (keycode == Keys.S) {
 			controller.downPressed();
 		}
 		
 		if (keycode == Keys.A) {
 			controller.leftPressed();
 		}
 		
 		return true;
 	}
 
 	@Override
 	public boolean keyUp(int keycode) {
 		if (keycode == Keys.W) {
 			controller.upReleased();
 		}
 		
 		if (keycode == Keys.D) {
 			controller.rightReleased();
 		}
 		
 		if (keycode == Keys.S) {
 			controller.downReleased();
 		}
 		
 		if (keycode == Keys.A) {
 			controller.leftReleased();
 		}
 		
 		return true;
 	}
 
 	@Override
 	public boolean keyTyped(char character) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
 		return false;
 	}
 
 	@Override
 	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
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
 		Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 1);
 		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
 
 		if (Gdx.app.getType() == ApplicationType.Android || Gdx.app.getType() == ApplicationType.iOS) {
 			if (touchpad.isTouched()) {
 				controller.touchpadPressed(touchpad);
 			} else {
 				controller.touchpadReleased();
 			}
 		}
 		
 		controller.update(delta);
 		renderer.render();
 		
 		stage.act();
 		stage.draw();
 	}
 
 	@Override
 	public void resize(int width, int height) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void show() {
 		world = new World();
 		renderer = new WorldRenderer(world, spriteBatch);
 		controller = new PlayerController(world);
 		stage = new Stage();
 		
 		// Only render the touchpad if they're running on mobile
 		if (Gdx.app.getType() == ApplicationType.Android || Gdx.app.getType() == ApplicationType.iOS) {
 			
 			Skin skin = new Skin();
 			skin.add("touchpadBase", new Texture("images/touchpad_base.png"));
 			skin.add("touchpadKnob", new Texture("images/touchpad_knob.png"));
 			
 			touchpad = new Touchpad(10f, new Touchpad.TouchpadStyle(skin.getDrawable("touchpadBase"), skin.getDrawable("touchpadKnob")));
 			touchpad.setBounds(25, 25, 300, 300);
 			
 			stage.addActor(touchpad);
 		}
 
 		InputMultiplexer multiplexer = new InputMultiplexer(stage, this);
 		Gdx.input.setInputProcessor(multiplexer);
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
 		spriteBatch.dispose();
 	}
 
 }
