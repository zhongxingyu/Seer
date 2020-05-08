 package com.glowman.spaceunit;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Input;
 import com.badlogic.gdx.Screen;
 import com.badlogic.gdx.Game;
 import com.badlogic.gdx.graphics.Camera;
 import com.badlogic.gdx.graphics.GL10;
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.graphics.g2d.Sprite;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.math.Vector3;
 
 import android.util.Log;
 
 import com.glowman.spaceunit.game.behavior.AsteroidsBehavior;
 import com.glowman.spaceunit.game.core.Button;
 import com.glowman.spaceunit.game.core.CameraHelper;
 import com.glowman.spaceunit.game.core.CameraHelper.ViewportMode;
 
 
 public class MainScreen implements Screen {
 	
 	float MENU_WIDTH = 320f;
 	float MENU_HEIGHT = 480f;
 	
 	private Game _game;
 
 	SpriteBatch _spriteBatch;
 	
 	private OrthographicCamera _camera;
 	private Button _playBtnRun;
 	private Button _playBtnShoot;
 	private Sprite _bkg;
 	private Vector3 _touchPoint;
 	
 	private AsteroidsBehavior _behavior;
 	private float _frustX;
 	private float _frustY;
 
 	public MainScreen(Game game) {
 		_game = game;
 		
 		_camera = CameraHelper.createCamera2(ViewportMode.STRETCH_TO_SCREEN, Assets.VIRTUAL_WIDTH, Assets.VIRTUAL_HEIGHT, Assets.pixelDensity);
 		_spriteBatch = new SpriteBatch();
 		_spriteBatch.setProjectionMatrix(_camera.combined);
 		_touchPoint = new Vector3();
 		
 		_bkg = new Sprite(Assets.bkg2);
 		_playBtnRun = new Button(Assets.getPlayRunRegion(1), Assets.getPlayRunRegion(2));
 		_playBtnShoot = new Button(Assets.getPlayShootRegion(1), Assets.getPlayShootRegion(2));
 		
 		//_behavior = new AsteroidsBehavior(15, _spriteBatch);
 
 		
 		//_playBtnRun.setX(Assets.VIRTUAL_WIDTH/2 - Assets.playBtnWidth - 10/Assets.pixelDensity);
 		//_playBtnRun.setY((Assets.VIRTUAL_HEIGHT - Assets.playBtnHeight)/2);
 	}
 
 	@Override
 	public void resize(int width, int height) {
 		//MENU_WIDTH = (float) width;
 		//MENU_HEIGHT = (float) height;
 		/*
 		_frustX = (float)width/MENU_WIDTH;
 		_frustY = (float)height/MENU_HEIGHT;
 		
 		_camera.position.set(MENU_WIDTH / 2f, MENU_WIDTH / 2f, 0);
 		_camera.update();
 		
 		Log.d("RESIZE", "REsize: " + MENU_WIDTH + " / " + MENU_HEIGHT);
 		
 
 		//_playBtnRun.getView().setSize(width, height)
 		_playBtnRun.setX((MENU_WIDTH/2 - _playBtnRun.getView().getWidth() - 10)*_frustX);
 		_playBtnRun.setY(((MENU_HEIGHT - _playBtnRun.getView().getHeight())/2)*_frustY);
 		
 		_playBtnShoot.setScale(0.75f*_frustY);
 		_playBtnShoot.setX((MENU_WIDTH/2 + 10)*_frustX);
 		_playBtnShoot.setY(((MENU_HEIGHT - _playBtnShoot.getView().getHeight())/2)*_frustY);
 		
 		
 		//TODO this shit cuz wrong picture
 		//_bkg.setRotation(90);
 		_bkg.setSize(MENU_WIDTH*_frustX, MENU_HEIGHT*_frustY);
 		*/
 	}
 
 	@Override
 	public void hide() {
 		this.clear();
 		Gdx.input.setInputProcessor(null);
 	}
 
 	@Override
 	public void show() {
 		//Gdx.input.setInputProcessor(new MenuTouchListener(_game, _playBtnRun, _playBtnShoot));
 		
 		_bkg.setSize(Assets.VIRTUAL_WIDTH, Assets.VIRTUAL_HEIGHT);
 		
 		float scale = 0.5f;
 		_playBtnRun.setSize(Assets.playBtnWidth*scale, Assets.playBtnHeight*scale);
		_playBtnRun.setScale(0.5f);
 		_playBtnShoot.setSize(Assets.playBtnWidth, Assets.playBtnHeight);
 	}
 
 	@Override
 	public void render(float deltaTime) {
 		this.clear();
 		_spriteBatch.begin();
 		
 		_bkg.draw(_spriteBatch);
 		//_behavior.tick(deltaTime);
 		_playBtnRun.draw(_spriteBatch);
 		//_playBtnShoot.draw(_spriteBatch);
 		
 		_spriteBatch.end();
 		
 	}
 
 
 	@Override
 	public void pause() {
 		//To change body of implemented methods use File | Settings | File Templates.
 	}
 
 	@Override
 	public void resume() {
 	}
 
 	@Override
 	public void dispose() {
 		Log.d("hz", "dispose!!");
 		this.clear();
 	}
 
 	private void clear() {
 		Gdx.gl.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
 		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
 	}
 
 
 }
