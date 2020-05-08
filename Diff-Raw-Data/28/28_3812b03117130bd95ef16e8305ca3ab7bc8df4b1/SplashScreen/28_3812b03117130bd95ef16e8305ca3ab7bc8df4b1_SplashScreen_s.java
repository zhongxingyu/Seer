 package com.gamelab.mmi;
 
 import java.awt.List;
 import java.sql.BatchUpdateException;
 import java.util.Random;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.InputProcessor;
 import com.badlogic.gdx.Screen;
 import com.badlogic.gdx.graphics.GL10;
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.graphics.Pixmap;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.Texture.TextureFilter;
 import com.badlogic.gdx.graphics.g2d.Sprite;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 import com.badlogic.gdx.math.Intersector;
 import com.badlogic.gdx.math.Vector2;
 
 public class SplashScreen implements Screen {
 	
 	
 	private Mmi game;
 	private SpriteBatch batch=new SpriteBatch();
 	private Texture texture;
 	private Sprite sprite;
 	private Button[] buttons=new Button[1];
		
 	
 	public SplashScreen( Mmi game,String file) {	
 		this.game=game;
 		float w = Gdx.graphics.getWidth();
 		float h = Gdx.graphics.getHeight();
 		
 		
 		
 		texture = new Texture(Gdx.files.internal(file));
 		texture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
 		
 		TextureRegion region = new TextureRegion(texture, 0, 0, 544, 338);
 		
 		sprite = new Sprite(region);
 		sprite.setSize(0.9f, 0.9f * sprite.getHeight() / sprite.getWidth());
 		sprite.setOrigin(sprite.getWidth()/2, sprite.getHeight()/2);
 		sprite.setPosition(-sprite.getWidth()/2, -sprite.getHeight()/2);
 		
 		Gdx.input.setInputProcessor(new InputProcessor() {
 			
 			@Override
 			public boolean touchUp(int screenX, int screenY, int pointer, int button) {
 				callback();
 				return false;
 			}
 			
 			@Override
 			public boolean touchDragged(int screenX, int screenY, int pointer) {
 				// TODO Auto-generated method stub
 				return false;
 			}
 			
 			@Override
 			public boolean touchDown(int screenX, int screenY, int pointer, int button) {
 				// TODO Auto-generated method stub
 				return false;
 			}
 			
 			@Override
 			public boolean scrolled(int amount) {
 				// TODO Auto-generated method stub
 				return false;
 			}
 			
 			@Override
 			public boolean mouseMoved(int screenX, int screenY) {
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
 			public boolean keyDown(int keycode) {
 				// TODO Auto-generated method stub
 				return false;
 			}
 		});
 		
 		
 	}
 	
 	@Override
 	public void render(float delta) {
 		update(delta);
 		Gdx.gl.glClearColor(0, 0, 0, 1);
 		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
 		
 
 		batch.begin();
 
 		
 		batch.draw(sprite,Gdx.graphics.getWidth()/2-227,Gdx.graphics.getHeight()/2-338/2);
 		batch.end();
 		
 		
 		
 	}
 	
 	private void callback(){
 		game.showMenu();
 	}
 	
 	public void update(float delta){
 		
 	}
 
 	@Override
 	public void resize(int width, int height) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void show() {
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
 		batch.dispose();
 		texture.dispose();
 		
 		
 	}
 
 }
