 package com.teambros.tbrpginspace;
 
 import com.badlogic.gdx.Game;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.GL10;
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.Texture.TextureFilter;
 import com.badlogic.gdx.graphics.g2d.Sprite;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 
 public class TBRPGInSpace extends Game {
 	private OrthographicCamera camera;
 	private SpriteBatch batch;
 	private Texture texture;
 	private Sprite sprite;
 	
 	@Override
 	public void create() {		
 		float w = Gdx.graphics.getWidth();
 		float h = Gdx.graphics.getHeight();
 		
 		camera = new OrthographicCamera(1, h/w);
 		batch = new SpriteBatch();
 		
 		texture = new Texture(Gdx.files.internal("data/logo.gif"));
 		texture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
 		
 		sprite = new Sprite(texture);
 		sprite.setSize(0.9f, 0.9f * sprite.getHeight() / sprite.getWidth());
 		sprite.setOrigin(sprite.getWidth()/2, sprite.getHeight()/2);
 		sprite.setPosition(-sprite.getWidth()/2, -sprite.getHeight()/2);
 	}
 
 	@Override
 	public void dispose() {
 		batch.dispose();
 		texture.dispose();
 	}
 
 	@Override
 	public void render() {		
 		Gdx.gl.glClearColor(0, 0, 0, 0);
 		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
 		
 		batch.setProjectionMatrix(camera.combined);
 		batch.begin();
 		sprite.draw(batch);
 		batch.end();
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
