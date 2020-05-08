 package com.gamelab.mmi;
 
 import java.awt.List;
 import java.sql.BatchUpdateException;
 import java.sql.Timestamp;
 import java.util.Random;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.InputProcessor;
 import com.badlogic.gdx.Screen;
 import com.badlogic.gdx.graphics.GL10;
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.graphics.Pixmap;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.Texture.TextureFilter;
 import com.badlogic.gdx.graphics.g2d.Animation;
 import com.badlogic.gdx.graphics.g2d.Sprite;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 import com.badlogic.gdx.math.Intersector;
 import com.badlogic.gdx.math.Vector2;
 
 public class TutorialScrenn implements Screen {
 
 	private Mmi game;
 	private Texture walkSheet;
 	private SpriteBatch sb;
 	private Animation animation;
 	private TextureRegion[] walkframes;
 	private TextureRegion currentFrame;
 	private float animationTime;
 
 	private int frameWidth;
 	private int frameHeight;
 	private float frameDuration = 0.02f;
 	private Screen nextScreen;
 	float onScreen = 0;
 
 	public TutorialScrenn(Mmi game, String file, int frames, Screen nextScreen) {
 		this.game = game;
 		this.nextScreen = nextScreen;
 		float w = Gdx.graphics.getWidth();
 		float h = Gdx.graphics.getHeight();
 		walkSheet = new Texture(Gdx.files.internal(file));
 		walkSheet.setFilter(TextureFilter.Linear, TextureFilter.Linear);
 
 		frameWidth = walkSheet.getWidth() / frames;
 		frameHeight = walkSheet.getHeight();
 
 		TextureRegion[][] tmp = TextureRegion.split(walkSheet, frameWidth,
 				frameHeight);
 		this.walkframes = new TextureRegion[frames];
 		int index = 0;
 		for (int i = 0; i < 1; i++) {
 			for (int j = 0; j < frames; j++) {
 				walkframes[index++] = tmp[i][j];
 			}
 		}
 		animation = new Animation(frameDuration, walkframes);
 		this.sb = new SpriteBatch();
 		this.animationTime = 0;
 
 	}
 
 	@Override
 	public void render(float delta) {
 		update(delta);
 		float w = Gdx.graphics.getWidth();
 		float h = Gdx.graphics.getHeight();
 		Gdx.gl.glClearColor(0, 0, 0, 1);
 		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
 		currentFrame = animation.getKeyFrame(animationTime, true);
 		sb.begin();
 		sb.draw(currentFrame, 212, 0, 200, 300, frameHeight, frameWidth, 1, 1,
 				90, true);
 		sb.end();
 
 	}
 
 	private void callback() {
 
 		if (1 < onScreen) {
 			game.setScreen(nextScreen);
 			if(nextScreen instanceof GameScreen) {
				((GameScreen)nextScreen).omfg();
 			}
 		}
 	}
 
 	public void update(float delta) {
 		onScreen += delta;
 	}
 
 	@Override
 	public void resize(int width, int height) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void show() {
 		Gdx.input.setInputProcessor(new InputProcessor() {
 
 			@Override
 			public boolean touchUp(int screenX, int screenY, int pointer,
 					int button) {
 				callback();
 				return false;
 			}
 
 			@Override
 			public boolean touchDragged(int screenX, int screenY, int pointer) {
 				// TODO Auto-generated method stub
 				return false;
 			}
 
 			@Override
 			public boolean touchDown(int screenX, int screenY, int pointer,
 					int button) {
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
 		walkSheet.dispose();
 		sb.dispose();
 
 	}
 
 }
