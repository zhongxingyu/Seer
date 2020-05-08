 package com.tacoid.pweek.screens;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.InputProcessor;
 import com.badlogic.gdx.graphics.GL20;
 import com.badlogic.gdx.graphics.GLCommon;
 import com.badlogic.gdx.graphics.g2d.BitmapFont;
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 import com.badlogic.gdx.scenes.scene2d.InputEvent;
 import com.badlogic.gdx.scenes.scene2d.InputListener;
 import com.badlogic.gdx.scenes.scene2d.Stage;
 import com.badlogic.gdx.scenes.scene2d.Touchable;
 import com.badlogic.gdx.scenes.scene2d.ui.Button;
 import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
 import com.tacoid.pweek.Controller;
 import com.tacoid.pweek.IGameService.Achievement;
 import com.tacoid.pweek.MusicPlayer;
 import com.tacoid.pweek.Pweek;
 import com.tacoid.pweek.Pweek.ScreenOrientation;
import com.tacoid.pweek.SoundPlayer;
 import com.tacoid.pweek.ScoreManager.GameType;
 import com.tacoid.pweek.SoundPlayer.SoundType;
 import com.tacoid.pweek.actors.BackgroundActor;
 import com.tacoid.pweek.actors.ControlerActor;
 import com.tacoid.pweek.actors.ExplosionActor;
 import com.tacoid.pweek.actors.GameOverActor;
 import com.tacoid.pweek.actors.GridActor;
 import com.tacoid.pweek.actors.MusicButtonActor;
 import com.tacoid.pweek.actors.NextPieceActor;
 import com.tacoid.pweek.actors.PauseMenu;
 import com.tacoid.pweek.actors.PortraitPanelActor;
 import com.tacoid.pweek.actors.ScoreActor;
 import com.tacoid.pweek.actors.SoundButtonActor;
 import com.tacoid.pweek.actors.StartActor;
 import com.tacoid.pweek.actors.GameOverActor.GameOverType;
 import com.tacoid.pweek.logic.GameLogic;
 import com.tacoid.pweek.logic.State;
 import com.tacoid.tracking.TrackingManager;
 
 public abstract class GameScreenPortrait implements GameScreen {
 	
 	protected static final int VIRTUAL_WIDTH = 768;
 	protected static final int VIRTUAL_HEIGHT = 1280;
 	
 	private int puyoSize = 64;
 
 	protected boolean end = false;
 	protected float elapsedTime;
 	protected PauseMenu pauseMenu;
 	protected PauseButton pauseButton;
 	protected Stage stage;
 	protected GameLogic gameLogic;
 	
 	private GridActor gridActor;
 	private InputProcessor controller;
 	private ControlerActor controllerActor;
 	protected boolean gamePaused;
 	protected GameOverActor gameOver;
 	private StartActor startActor;
 	private NextPieceActor nextPieceActor;
 	private ExplosionActor explosionActor;
 	private boolean started;
 	private int volumeStart;
 	
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
 
 	protected void addButton(Button button, int x, int y) {
 		stage.addActor(button);
 		button.setX(x);
 		button.setY(y);
 	}
 
 	public GameScreenPortrait() {
 		elapsedTime = 0;
 		gameLogic = new GameLogic(false);
 	}
 	
 	public int getPuyoSize() {
 		return puyoSize;
 	}
 	
 	protected void initGraphics() {
 		stage = new Stage(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(),
 				false);
 		
 		controller = new Controller(gameLogic, this, stage);
 
 		gridActor = new GridActor(Pweek.getInstance().atlasPuyo, gameLogic, Pweek.getInstance().manager.get("images/font_score.fnt", BitmapFont.class), 280, 325, 70, puyoSize);
 		explosionActor = new ExplosionActor(Pweek.getInstance().atlasPuyo, gameLogic, Pweek.getInstance().manager.get("images/font_score.fnt", BitmapFont.class), 280, 325, 70, puyoSize);
 		nextPieceActor = new NextPieceActor(Pweek.getInstance().atlasPuyo, gameLogic, 68, 920, puyoSize);
 		ScoreActor scoreActor = new ScoreActor(Pweek.getInstance().manager.get("images/font_score.fnt", BitmapFont.class), gameLogic, 500, 1245);
 		
 		TextureRegion pauseRegion = new TextureRegion(Pweek.getInstance().atlasBouttons.findRegion("pause_button"));
 		stage.addActor(new BackgroundActor(ScreenOrientation.PORTRAIT));
 		stage.addActor(new PortraitPanelActor(Pweek.getInstance().atlasPanelsPortrait));
 		stage.addActor(gridActor);
 		stage.addActor(nextPieceActor);
 		stage.addActor(scoreActor);
 		stage.addActor(explosionActor);
 
 		controllerActor = new ControlerActor(ScreenOrientation.PORTRAIT, gameLogic);
 		stage.addActor(controllerActor);
 		
 		pauseButton = new PauseButton(pauseRegion);
 		addButton(pauseButton,10,VIRTUAL_HEIGHT-10-pauseRegion.getRegionHeight());
 		addButton(MusicButtonActor.createMusicButton(Pweek.getInstance().atlasBouttons),VIRTUAL_WIDTH-90, VIRTUAL_HEIGHT-90);
 		addButton(SoundButtonActor.createSoundButton(Pweek.getInstance().atlasBouttons),VIRTUAL_WIDTH-180, VIRTUAL_HEIGHT-90);
 		
 		boolean show = false;
 		if (gameOver != null) {
 			show = gameOver.isVisible();
 		}
 		gameOver = new GameOverActor(Pweek.getInstance().atlasPlank, Pweek.getInstance().atlasPanelsPortrait, Pweek.getInstance().atlasBouttons, Pweek.getInstance().manager.get("images/font_score.fnt", BitmapFont.class), this, VIRTUAL_WIDTH/2, 3*VIRTUAL_HEIGHT/5);
 		stage.addActor(gameOver);
 		if (show) {
 			gameOver();
 		} else {
 			gameOver.hide();
 		}
 		
 		pauseMenu = new PauseMenu(Pweek.getInstance().atlasPlank, this, Pweek.getInstance().getHandler(), ScreenOrientation.PORTRAIT, !gamePaused);
 		stage.addActor(pauseMenu);
 	
 		show = true;
 		if (startActor != null) {
 			show = startActor.isVisible();
 		}
 		startActor = new StartActor(Pweek.getInstance().atlasPlank, this);
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
 		Pweek.getInstance().getHandler().setPortrait(true);
 		Pweek.getInstance().getHandler().showAds(true);
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
 		started = false;
 	}
 
 	private void gameOver() {
 		int volumeEnd;
 		if (!MusicPlayer.getInstance().isMuted()) {
 			volumeEnd = Pweek.getInstance().getHandler().getVolume();
 		} else {
 			volumeEnd = 0;
 		}
 		if (volumeEnd > 80 && volumeStart > 80 && elapsedTime > 600) {
 			Pweek.getInstance().getGameService().unlockAchievement(Achievement.DEAF);
 		}
 		controllerActor.setTouchable(Touchable.disabled);
 		pauseButton.setTouchable(Touchable.disabled);
 		gameOver.show(GameOverType.GAMEOVER);
 	}
 	
 	@Override
 	public void gamePause() {
 		gamePaused = true;
 		gameLogic.pause();
 		
 		gridActor.setVisible(false);
 		nextPieceActor.setVisible(false);
 		controllerActor.setTouchable(Touchable.disabled);
 		pauseButton.setTouchable(Touchable.disabled);
 	}
 
 	@Override
 	public void gameStart() {
 		if (!MusicPlayer.getInstance().isMuted()) {
 			this.volumeStart = Pweek.getInstance().getHandler().getVolume();
 		} else {
 			this.volumeStart = 0;
 		}
 		this.gameResume();
 		this.started = true;
 	}
 	
 	@Override
 	public boolean isGameStarted() {
 		return this.started;
 	}
 	
 	@Override
 	public void gameResume() {
 		gamePaused = false;
 		gameLogic.resume();
 		
 		gridActor.setVisible(true);
 		nextPieceActor.setVisible(true);
 		controllerActor.setTouchable(Touchable.enabled);
 		pauseButton.setTouchable(Touchable.enabled);
 	}
 	
 	public ScreenOrientation getOrientation() {
 		return ScreenOrientation.PORTRAIT;
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
 		Pweek.getInstance().setScreen(MainMenuScreen.getInstance());
 	}
 	
 	@Override
 	public boolean isGamePaused() {
 		return gamePaused;
 	}
 	
 	@Override
 	public void hidePause() {
 		pauseMenu.hide();
 	}
 }
