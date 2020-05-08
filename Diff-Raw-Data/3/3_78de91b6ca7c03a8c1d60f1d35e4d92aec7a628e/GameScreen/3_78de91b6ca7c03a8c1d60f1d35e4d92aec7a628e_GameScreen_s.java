 package com.glowman.spaceunit.game;
 
 import java.util.ArrayList;
 
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
 import com.glowman.spaceunit.core.Button;
 import com.glowman.spaceunit.core.FPSViewer;
 import com.glowman.spaceunit.core.TextButton;
 import com.glowman.spaceunit.data.GooglePlayData;
 import com.glowman.spaceunit.game.balance.SpeedCollector;
 import com.glowman.spaceunit.game.mapObject.Ship;
 import com.glowman.spaceunit.game.score.Score;
 import com.glowman.spaceunit.game.score.ScoreView;
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
 	private ScoreView _scoreView;
 	private Button _pauseButton;
 
 	public GameScreen(Game game, OrthographicCamera camera)
 	{
 		_game = game;
 		_camera = camera;
 		_drawer = new SpriteBatch();
 		_drawer.setProjectionMatrix(_camera.combined);
 		_bkg = new Sprite(Assets.bkg);
 		_bkg.setSize(Assets.VIRTUAL_WIDTH, Assets.VIRTUAL_HEIGHT);
 
 		this.createInterface();
 
 		_gameTouchListener = new GameTouchListener(_game, _camera, _pauseButton);
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
 		if (!_gameStrategy.isPaused()) {
 			_gameStrategy.tick(delta);
 		}
 
 		if (_gameStrategy.getGameStatus() == GameStatus.GAME_OVER) {
 			_gameTouchListener.gameOver();
 		}
 
 		_drawer.begin();
 
 		float bkgAlpha = _gameStrategy.isPaused() ? 1 : 0.3f;
 		_bkg.draw(_drawer, bkgAlpha);
 
 		ArrayList<Sprite> objects = _gameStrategy.getDrawableObjects();
 		for (Sprite object : objects) {
 			object.draw(_drawer);
 		}
 
 		if (_gameStrategy.getGameStatus() == GameStatus.GAME_OVER) { this.drawGameOver(); }
 		this.drawScore();
 		_pauseButton.draw(_drawer);
 
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
 		this.createFont();
 		Score score = _gameStrategy.getScore();
 		if (GooglePlayData.gameHelper.isSignedIn()) {
 			//TODO create AchievementControl for tracking update achievements 
 			GooglePlayData.gamesClient.submitScore(
 					GooglePlayData.getLeaderboardID(_gameType), (long)score.score);
 		}
 		BitmapFont.TextBounds bounds = _font.getBounds("Game \n Over. " + score.type + " : " + score.getPrintableScore());
 		_font.draw(_drawer, "Game Over. " + score.type + " : " + score.getPrintableScore(),
 					Assets.VIRTUAL_WIDTH/4f, Assets.VIRTUAL_HEIGHT/2f);
 	}
 
 	private void drawScore() {
 		Score score = _gameStrategy.getScore();
 		_scoreView.updateScore(score.getPrintableScore());
 		_scoreView.draw(_drawer);
 	}
 
 	private void createFont() {
 		if (_font == null) {
 			_font = new BitmapFont(Gdx.files.internal(Assets.gameFontPath), Assets.gameFontRegion, false);
 			_font.setColor(Color.RED);
 			_font.setScale(1f);
 		}
 	}
 
 	private void clear() {
 		Gdx.gl.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
 		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
 	}
 
 	private void createInterface() {
 		//score panel
 		_scoreView = new ScoreView();
 		_scoreView.setPosition(Assets.VIRTUAL_WIDTH / 2 - _scoreView.getWidth() / 2,
 				Assets.VIRTUAL_HEIGHT - _scoreView.getHeight());
 
 		//pause button
 		_pauseButton = new Button(Assets.getPauseBtn(1), Assets.getPauseBtn(2));
 		_pauseButton.setPosition(Assets.VIRTUAL_WIDTH - _pauseButton.getWidth() - 15,
 								 Assets.VIRTUAL_HEIGHT - _pauseButton.getHeight() - 15);
 	}
 
 }
