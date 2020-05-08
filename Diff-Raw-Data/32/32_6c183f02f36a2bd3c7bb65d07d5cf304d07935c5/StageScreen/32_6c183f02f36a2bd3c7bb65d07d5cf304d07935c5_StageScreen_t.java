 package com.vyorkin.engine.screens;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.Camera;
 import com.badlogic.gdx.scenes.scene2d.Stage;
 import com.vyorkin.engine.E;
 
 public abstract class StageScreen extends GameScreen {
 	protected Stage stage;
 	
 	protected StageScreen() {
 		this(E.settings.width, E.settings.height);
 	}
 	
 	protected StageScreen(int width, int height) {
 		this.stage = new Stage(width, height, true);
 	}
 	
 	@Override
 	public Camera getCamera() {
 		return stage.getCamera();
 	}
 	
 	@Override
 	public void show() {
 		super.show();
		stage.setViewport(E.settings.width, E.settings.height, true);
 		Gdx.input.setInputProcessor(stage);
 	}
 	
 	@Override
 	public void dispose() {
 		super.dispose();
 		stage.dispose();
 	}
 	
 	@Override
 	protected void update(float delta) {
 		stage.act(delta);
 	}
 	
 	@Override
 	protected void draw(float delta) {
 		stage.draw();
 	}
 }
