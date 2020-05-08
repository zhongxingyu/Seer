 package com.glowman.spaceunit.game;
 
 import android.util.Log;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Screen;
 import com.badlogic.gdx.Game;
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.GL10;
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.graphics.g2d.BitmapFont;
 import com.badlogic.gdx.graphics.g2d.Sprite;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.math.Vector2;
 import com.glowman.spaceunit.Assets;
 import com.glowman.spaceunit.core.FPSViewer;
 import com.glowman.spaceunit.game.balance.SpeedCollector;
 import com.glowman.spaceunit.game.mapObject.Ship;
 import com.glowman.spaceunit.game.strategy.GameStatus;
 import com.glowman.spaceunit.game.strategy.GameStrategyFactory;
 import com.glowman.spaceunit.game.strategy.IGameStrategy;
 
 
 public class GameScreen implements Screen {
 
 	private final Game _game;
 
 	private int _gameType;
 	private Ship _ship;
 	private IGameStrategy _gameStrategy;
 
	private final Sprite _bkg;
 	private final SpriteBatch _drawer;
 	private OrthographicCamera _camera;
 	private GameTouchListener _gameTouchListener;
 
 	private BitmapFont _font;
 
 	public GameScreen(Game game, OrthographicCamera camera)
 	{
 		_game = game;
 		_camera = camera;
 		_drawer = new SpriteBatch();
 		_drawer.setProjectionMatrix(_camera.combined);
 		_gameTouchListener = new GameTouchListener(_game, _camera);
		_bkg = new Sprite(Assets.bkg);
		_bkg.setSize(Assets.VIRTUAL_WIDTH, Assets.VIRTUAL_HEIGHT);
 	}
 	
 	public void play(int gameType) {
 		_gameType = gameType;
 		Log.d("hz", "game type : " + _gameType);
 		this.createShip();
 		_gameStrategy = GameStrategyFactory.createStrategy(_ship, _gameType);
 		_gameStrategy.startGame();
 		_gameTouchListener.init(_gameStrategy);
 		Gdx.input.setInputProcessor(_gameTouchListener);
 	}
 
 
 	@Override
 	public void render(float delta) {
 		this.clear();
 		_gameStrategy.tick(delta);
 
 		if (_gameStrategy.getGameStatus() == GameStatus.GAME_OVER) {
 			_gameTouchListener.gameOver();
 		}
 
 		_drawer.begin();
 
		_bkg.draw(_drawer, 0.3f);

 		Sprite[] objects = _gameStrategy.getDrawableObjects();
 		for (Sprite object : objects) {
 			object.draw(_drawer);
 		}
 
 		if (_gameStrategy.getGameStatus() == GameStatus.GAME_OVER) { this.drawGameOver(); }
 
 		FPSViewer.draw(_drawer);
 		_drawer.end();
 	}
 
 	@Override
 	public void resize(int width, int height) {}
 
 	@Override
 	public void show() {
 	}
 	@Override
 	public void hide() {
 		Gdx.input.setInputProcessor(null);
 		_gameStrategy.stopGame();
 		this.clear();
 		//TODO остановка/пауза всех просчетов, которые могут выполняться
 	}
 	@Override public void pause() {
 		_gameStrategy.pauseGame();
 	}
 	@Override public void resume() {
 		_gameStrategy.resumeGame();
 	}
 	@Override public void dispose() {
 		_gameStrategy.stopGame();
 	}
 
 	private void createShip() {
 		if (_ship != null) { Log.e("hz", "ship already exists!"); }
 
 		_ship = new Ship(new Sprite(Assets.ship), 10);
 		_ship.setGeneralSpeed(SpeedCollector.getHeroSpeed(_gameType));
 		_ship.setPosition(new Vector2(Assets.VIRTUAL_WIDTH / 2, Assets.VIRTUAL_HEIGHT / 2));
 	}
 
 	private void drawGameOver() {
 		if (_font == null) {
 			_font = new BitmapFont(Gdx.files.internal(Assets.gameFontPath), Assets.gameFontRegion, false);
 			_font.setColor(Color.RED);
 			_font.setScale(1f / Assets.pixelDensity);
 		}
 		BitmapFont.TextBounds bounds = _font.getBounds("Game Over");
 		_font.draw(_drawer, "Game Over", Assets.VIRTUAL_WIDTH/2, Assets.VIRTUAL_HEIGHT/2);
 	}
 
 	private void clear() {
 		Gdx.gl.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
 		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
 	}
 
 }
