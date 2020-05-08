 package com.tacoid.pweekmini.screens;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.InputProcessor;
 import com.badlogic.gdx.graphics.GL20;
 import com.badlogic.gdx.graphics.GLCommon;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.BitmapFont;
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 import com.badlogic.gdx.scenes.scene2d.InputEvent;
 import com.badlogic.gdx.scenes.scene2d.InputListener;
 import com.badlogic.gdx.scenes.scene2d.Stage;
 import com.badlogic.gdx.scenes.scene2d.Touchable;
 import com.badlogic.gdx.scenes.scene2d.ui.Button;
 import com.badlogic.gdx.scenes.scene2d.ui.Image;
 import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
 import com.tacoid.pweek.Controller;
 import com.tacoid.pweekmini.PweekMini;
 import com.tacoid.pweek.SoundPlayer;
 import com.tacoid.pweek.Pweek.ScreenOrientation;
 import com.tacoid.pweek.ScoreManager.GameType;
 import com.tacoid.pweek.SoundPlayer.SoundType;
 import com.tacoid.pweekmini.actors.GameOverActor;
 import com.tacoid.pweek.actors.GridActor;
 import com.tacoid.pweekmini.actors.HighScoreActor;
 import com.tacoid.pweek.actors.MusicButtonActor;
 import com.tacoid.pweek.actors.NextPieceActor;
 import com.tacoid.pweekmini.actors.PauseMenu;
 import com.tacoid.pweekmini.actors.PortraitPanelActor;
 import com.tacoid.pweek.actors.ScoreActor;
 import com.tacoid.pweek.actors.SoundButtonActor;
 import com.tacoid.pweek.actors.StartActor;
 import com.tacoid.pweekmini.actors.GameOverActor.GameOverType;
 import com.tacoid.pweek.logic.GameLogic;
 import com.tacoid.pweek.logic.State;
 import com.tacoid.pweek.screens.GameScreen;
 import com.tacoid.tracking.TrackingManager;
 
 public abstract class GameScreenPortrait implements GameScreen {
 	
 	private static final int VIRTUAL_WIDTH = 480;
 	private static final int VIRTUAL_HEIGHT = 800;
 	
 	private int puyoSize = 48;
 
 	protected boolean end = false;
 	protected float elapsedTime;
 	protected PauseMenu pauseMenu;
 	protected PauseButton pauseButton;
 	protected Stage stage;
 	protected GameLogic gameLogic;
 	
 	private GridActor gridActor;
 	private InputProcessor controller;
 	protected boolean gamePaused;
 	private GameOverActor gameOver;
 	private StartActor startActor;
 	private NextPieceActor nextPieceActor;
 	
 	protected class PauseButton extends Button {
 
 		public PauseButton(TextureRegion region) {
 			super(new TextureRegionDrawable(region));
 			
 			addListener(new InputListener() {
 				@Override
 				public boolean touchDown(InputEvent event, float x, float y,
 						int pointer, int button) {
 					SoundPlayer.getInstance().playSound(SoundType.TOUCH_MENU, 0.5f, true);
 					pauseMenu.show();
 					return true;
 				}
 			});
 		}
 	}
 
 	private void addButton(Button button, int x, int y) {
 		stage.addActor(button);
 		button.setX(x);
 		button.setY(y);
 	}
 
 	public GameScreenPortrait() {
 		elapsedTime = 0;
 		gameLogic = new GameLogic();
 	}
 	
 	public int getPuyoSize() {
 		return puyoSize;
 	}
 	
 	protected void initGraphics() {
 		stage = new Stage(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(),
 				false);
 		
 		controller = new Controller(gameLogic, this, stage);
 
 		gridActor = new GridActor(PweekMini.getInstance().atlasPuyo, gameLogic, PweekMini.getInstance().manager.get("images/font_score.fnt", BitmapFont.class), 148, 130, 50, puyoSize);
 		nextPieceActor = new NextPieceActor(PweekMini.getInstance().atlasPuyo, gameLogic, 21, 590, puyoSize);
 		ScoreActor scoreActor = new ScoreActor(PweekMini.getInstance().manager.get("images/font_score.fnt", BitmapFont.class), gameLogic, 300, 786);
 		HighScoreActor highScoreActor = new HighScoreActor(PweekMini.getInstance().manager.get("images/font_score.fnt", BitmapFont.class), this, 76, 440);
 		
 		TextureRegion pauseRegion = new TextureRegion(PweekMini.getInstance().atlasBouttons.findRegion("pause_button"));
 
 		stage.addActor(new Image(new TextureRegion(PweekMini.getInstance().manager.get("images/background.png", Texture.class), 480, 800)));
 		stage.addActor(new PortraitPanelActor(PweekMini.getInstance().atlasPanelsPortrait));
 		stage.addActor(gridActor);
 		stage.addActor(nextPieceActor);
 		stage.addActor(scoreActor);
 		stage.addActor(highScoreActor);
 		
 		pauseButton = new PauseButton(pauseRegion);
 		addButton(pauseButton,10,VIRTUAL_HEIGHT-10-pauseRegion.getRegionHeight());
 		addButton(MusicButtonActor.createMusicButton(PweekMini.getInstance().atlasBouttons),VIRTUAL_WIDTH-64, VIRTUAL_HEIGHT-64);
 		addButton(SoundButtonActor.createSoundButton(PweekMini.getInstance().atlasBouttons),VIRTUAL_WIDTH-2*64-10, VIRTUAL_HEIGHT-64);
 		
 		boolean show = false;
 		if (gameOver != null) {
 			show = gameOver.isVisible();
 		}
 		gameOver = new GameOverActor(PweekMini.getInstance().atlasPlank, PweekMini.getInstance().atlasPanelsPortrait, PweekMini.getInstance().manager.get("images/font_score.fnt", BitmapFont.class), this, VIRTUAL_WIDTH/2, 2*VIRTUAL_HEIGHT/3);
 		stage.addActor(gameOver);
 		if (show) {
 			gameOver();
 		} else {
 			gameOver.hide();
 		}
 		
 		pauseMenu = new PauseMenu(PweekMini.getInstance().atlasPlank, this, PweekMini.getInstance().getHandler(), ScreenOrientation.PORTRAIT, !gamePaused);
 		stage.addActor(pauseMenu);
 	
 		show = true;
 		if (startActor != null) {
 			show = startActor.isVisible();
 		}
 		startActor = new StartActor(PweekMini.getInstance().atlasPlank, this);
 		stage.addActor(startActor);
 		if (show) {
 			startActor.show();
 		} else {
 			startActor.setVisible(false);
 			startActor.setTouchable(Touchable.disabled);
 			if (gamePaused) {
 				pauseMenu.show();
 			}
 		}
 	}
 
 	@Override
 	public void dispose() {
 		Gdx.app.log("Portrait", "paused");
 	}
 
 	@Override
 	public void hide() {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void pause() {
 		if (!gamePaused && !gameEnded()) {
 			pauseMenu.show();
 		}
 	}
 
 	protected boolean gameEnded() {
 		return gameLogic.getState() == State.LOST;
 	}
 	
 	@Override
 	public void render(float delta) {
 		if (!gameEnded()) {
 			if (!gamePaused) {
 				this.elapsedTime += delta;
 			}
 			// Update model
 			gameLogic.update(delta);
 			if(gameEnded()) {
 				gameOver();
 			}
 		} else {
 			//Gdx.input.setInputProcessor(null);
 			end = true;
 			
 			// TODO
 		}
 		
 		draw(delta);
 	}
 	
 	private void draw(float delta) {
 		GLCommon gl = Gdx.gl;
 		
 		gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
 
 		stage.act(delta);
 		stage.draw();
 	}
 
 	@Override
 	public void resize(int arg0, int arg1) {
 		stage.setViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, false);
 		stage.getCamera().position.set(VIRTUAL_WIDTH / 2, VIRTUAL_HEIGHT / 2, 0);
 	}
 
 	@Override
 	public void resume() {
 		show();
 	}
 
 	@Override
 	public void show() {
 		PweekMini.getInstance().getHandler().showAds(!startActor.isVisible() && (gamePaused || gameEnded()));
 		resize(0, 0);
 		Gdx.input.setInputProcessor(controller);
 	}
 
 	public float getElapsedTime() {
 		return elapsedTime;
 	}
 
 	@Override
 	public void init() {
 		gameLogic.init();
 		gameOver.hide();
 		startActor.show();
 		elapsedTime = 0;
 	}
 
 	private void gameOver() {
 		pauseButton.setTouchable(Touchable.disabled);
 		gameOver.show(GameOverType.GAMEOVER);
 	}
 	
 	@Override
 	public void gamePause() {
 		gamePaused = true;
 		gameLogic.pause();
 		
 		gridActor.setVisible(false);
 		nextPieceActor.setVisible(false);
 		pauseButton.setTouchable(Touchable.disabled);
 	}
 
 	@Override
 	public void gameResume() {
 		gamePaused = false;
 		gameLogic.resume();
 		
 		gridActor.setVisible(true);
 		nextPieceActor.setVisible(true);
 		pauseButton.setTouchable(Touchable.enabled);
 	}
 	
 	public ScreenOrientation getOrientation() {
 		return ScreenOrientation.LANDSCAPE;
 		// Ceci est un hack : en gros, en landscape les acteurs ne font pas de rotation.
 	}	
 	
 	@Override
 	public float getHeight() {
 		return VIRTUAL_HEIGHT;
 	}
 
 	@Override
 	public float getWidth() {
 		return VIRTUAL_WIDTH;
 	}
 	
 	@Override
 	public int getScore() {
 		return gameLogic.getScore();
 	}
 	
 	@Override
 	public void quit() {
 		pauseMenu.hide();
 		GameType type = getGameType();
 		TrackingManager.getTracker().trackEvent("UI", "button_click",type.toString() + " quit before end", null);
 		PweekMini.getInstance().setScreen(MainMenuScreen.getInstance());
 	}
 }
