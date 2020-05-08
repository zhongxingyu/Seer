 package com.glowman.spaceunit.game;
 
 import java.util.ArrayList;
 
 import android.util.Log;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Screen;
 import com.badlogic.gdx.Game;
 import com.badlogic.gdx.assets.loaders.SoundLoader;
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.GL10;
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.graphics.g2d.BitmapFont;
 import com.badlogic.gdx.graphics.g2d.Sprite;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.scenes.scene2d.ui.Skin;
 import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
 import com.glowman.spaceunit.Assets;
 import com.glowman.spaceunit.SoundPlayer;
 import com.glowman.spaceunit.achievements.AchievementsConrol;
 import com.glowman.spaceunit.core.Button;
 import com.glowman.spaceunit.core.FPSViewer;
 import com.glowman.spaceunit.core.TextButton;
 import com.glowman.spaceunit.data.GooglePlayData;
 import com.glowman.spaceunit.game.balance.SpeedCollector;
 import com.glowman.spaceunit.game.mapObject.Ship;
 import com.glowman.spaceunit.game.mapObject.enemy.behaviour.core.TouchPad;
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
 	private TouchPad _touchPad;
 	private TextButton _returnToMenuBtn;
 
 	private float _interfaceAlpha;
 
 	public GameScreen(Game game, OrthographicCamera camera)
 	{
 		_game = game;
 		_camera = camera;
 		_drawer = new SpriteBatch();
 		_drawer.setProjectionMatrix(_camera.combined);
 		_bkg = new Sprite(Assets.bkg);
 		_bkg.setSize(Assets.FULL_VIRTUAL_WIDTH, Assets.FULL_VIRTUAL_HEIGHT);
 		_bkg.setPosition(Assets.FULL_X_OFFSET, Assets.FULL_Y_OFFSET);
 
 		this.createInterface();
 
 		_gameTouchListener = new GameTouchListener(_game, _pauseButton, _touchPad, _returnToMenuBtn);
 	}
 	
 	public void play(int gameType) {
 		_gameType = gameType;
 		Log.d("hz", "game type : " + _gameType);
 		this.createShip();
 		_gameTouchListener.setShip(_ship);
 		_gameStrategy = GameStrategyFactory.createStrategy(_ship, _gameType);
 		_gameStrategy.startGame();
 		_gameTouchListener.init(_gameStrategy);
 		_scoreView.setScoreType(Score.getScoreTypeByGameType(_gameType));
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
 
 		_interfaceAlpha = _gameStrategy.isPaused() ? 1 : Assets.GAME_INTERFACE_ALPHA;
 		float bkgAlpha = _gameStrategy.isPaused() ? 1 : Assets.GAME_BKG_ALPHA;
 		_bkg.draw(_drawer, bkgAlpha);
 
 		ArrayList<Sprite> objects = _gameStrategy.getDrawableObjects();
 		for (Sprite object : objects) {
 			object.draw(_drawer);
 		}
 
 		if (_gameStrategy.getGameStatus() == GameStatus.GAME_OVER) { this.drawGameOver(); }
 		this.drawScore();
 		_pauseButton.draw(_drawer, _interfaceAlpha);
 
 		if (_gameStrategy.isPaused()) _returnToMenuBtn.draw(_drawer);
 
 		//_touchPad.act(delta);
 		_touchPad.draw(_drawer, _interfaceAlpha);
 
 		FPSViewer.draw(_drawer);
 		_drawer.end();
 	}
 
 	@Override
 	public void resize(int width, int height) {}
 
 	@Override
 	public void show() {
 		SoundPlayer.playMusic(Assets.backGameSound, .5f);
 	}
 	@Override
 	public void hide() {
 		Gdx.input.setInputProcessor(null);
 		_gameStrategy.stopGame();
 		this.clear();
 		SoundPlayer.stopMusic(Assets.backGameSound);
 	}
 	@Override public void pause() {
 		_pauseButton.setClickedMode();
 		_gameStrategy.pauseGame();
 	}
 	@Override public void resume() {
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
 			AchievementsConrol.checkUnlockAchievement(_gameType, (long)score.score);
 			GooglePlayData.gamesClient.submitScore(
 					GooglePlayData.getLeaderboardID(_gameType), (long)score.score);
 		}
 		String text = "GAME OVER\n" + score.type + " : " + score.getPrintableScore();
 		BitmapFont.TextBounds bounds = _font.getMultiLineBounds(text);
 
 		_font.drawMultiLine(_drawer, text,
 				Assets.VIRTUAL_WIDTH/2 - bounds.width/2, (Assets.VIRTUAL_HEIGHT - bounds.height)/2,
 				Assets.VIRTUAL_WIDTH, BitmapFont.HAlignment.LEFT);}
 
 	private void drawScore() {
 		Score score = _gameStrategy.getScore();
 		_scoreView.setScore(score.getPrintableScore());
 		_scoreView.draw(_drawer, _interfaceAlpha);
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
 		this.createTouchPad();
 
 		//score panel
 		_scoreView = new ScoreView();
 		_scoreView.setPosition(Assets.VIRTUAL_WIDTH / 2 - _scoreView.getWidth() / 2,
 				Assets.VIRTUAL_HEIGHT - _scoreView.getHeight());
 
 		//pause button
 		_pauseButton = new Button(Assets.getPauseBtn(1), Assets.getPauseBtn(2));
 		_pauseButton.setPosition(Assets.VIRTUAL_WIDTH - _pauseButton.getWidth() - 15,
 								 Assets.VIRTUAL_HEIGHT - _pauseButton.getHeight() - 15);
 
 		//return to menu button
 		_returnToMenuBtn = new TextButton(Assets.getSimpleBtnRegion(1), Assets.getSimpleBtnRegion(2), "Quit");
 		_returnToMenuBtn.setPosition(Assets.VIRTUAL_WIDTH/2 - _returnToMenuBtn.getWidth()/2,
 									 Assets.VIRTUAL_HEIGHT/2 - _returnToMenuBtn.getHeight()/2);
 
 	}
 
 	private void createTouchPad() {
 		Skin skin = new Skin();
 		skin.add("background", Assets.joyPadBkg);
 		skin.add("knob", Assets.joyPad);
 
 		TouchPad.TouchpadStyle style = new TouchPad.TouchpadStyle(new TextureRegionDrawable(Assets.joyPadBkg),
 																  new TextureRegionDrawable(Assets.joyPad));
 		_touchPad = new TouchPad(10, style);
 		_touchPad.setPosition(20, 20);
 	}
 
 }
