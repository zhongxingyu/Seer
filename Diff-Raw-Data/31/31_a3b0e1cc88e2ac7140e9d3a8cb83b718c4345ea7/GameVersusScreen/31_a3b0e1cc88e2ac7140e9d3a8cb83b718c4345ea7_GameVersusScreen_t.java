 package com.tacoid.puyopuyo.screens;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.InputProcessor;
 import com.badlogic.gdx.graphics.GL20;
 import com.badlogic.gdx.graphics.GLCommon;
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 import com.badlogic.gdx.scenes.scene2d.Stage;
 import com.badlogic.gdx.scenes.scene2d.ui.Button;
 import com.tacoid.puyopuyo.Controller;
 import com.tacoid.puyopuyo.PuyoPuyo;
 import com.tacoid.puyopuyo.SoundPlayer;
 import com.tacoid.puyopuyo.PuyoPuyo.ScreenOrientation;
 import com.tacoid.puyopuyo.ScoreManager.GameType;
 import com.tacoid.puyopuyo.SoundPlayer.SoundType;
 import com.tacoid.puyopuyo.actors.BackgroundActor;
 import com.tacoid.puyopuyo.actors.ControlerActor;
 import com.tacoid.puyopuyo.actors.GameOverActor;
 import com.tacoid.puyopuyo.actors.GridActor;
 import com.tacoid.puyopuyo.actors.LandscapePanelActor;
 import com.tacoid.puyopuyo.actors.MusicButtonActor;
 import com.tacoid.puyopuyo.actors.NextPieceActor;
 import com.tacoid.puyopuyo.actors.PauseMenu;
 import com.tacoid.puyopuyo.actors.ScoreActor;
 import com.tacoid.puyopuyo.actors.SoundButtonActor;
 import com.tacoid.puyopuyo.actors.StartActor;
 import com.tacoid.puyopuyo.actors.GameOverActor.GameOverType;
 import com.tacoid.puyopuyo.logic.GameLogic;
 import com.tacoid.puyopuyo.logic.IA;
 import com.tacoid.puyopuyo.logic.IAEasy;
 import com.tacoid.puyopuyo.logic.IAHard;
 import com.tacoid.puyopuyo.logic.State;
 
 public class GameVersusScreen implements GameScreen {
 	private static final int VIRTUAL_WIDTH = 1280;
 	private static final int VIRTUAL_HEIGHT = 768;
 	private static GameVersusScreen instance = null;
 	private Stage stage;
 	private GameLogic gameLogic;
 	private GameLogic gameLogicIA;
 	private GridActor gridActor;
 	private GridActor gridActorIA;
 	private NextPieceActor nextPieceActor;
 	private NextPieceActor nextPieceActorIA;
 	protected float elapsedTime;
 
 	private IA ia;
 	private InputProcessor controller;
 	private ControlerActor controllerActor;
 	private PauseMenu pauseMenu;
 	private PauseButton pauseButton;
 	private StartActor startActor;
 	
 	private GameOverActor gameOver;
 	private boolean gamePaused;
 	
 	private int level;
 	
 	private void addButton(Button button, int x, int y) {
 		stage.addActor(button);
 		button.x = x;
 		button.y = y;
 	}
 	
 	public class PauseButton extends Button {
 
 		public PauseButton(TextureRegion region) {
 			super(region);
 		}
 		
 		public boolean touchDown(float x, float y, int pointer) {
 			SoundPlayer.getInstance().playSound(SoundType.TOUCH_MENU, 0.5f, true);
 			pauseMenu.show();
 			return true;
 		}
 
 	}
 
 	private GameVersusScreen() {
 		gameLogic = new GameLogic();
 		gameLogicIA = new GameLogic();
 		gameLogic.setOpponent(gameLogicIA);
 		gameLogicIA.setOpponent(gameLogic);
 	}
 	
 	public void setLevel(int level) {
 		this.level = level;
 		if (level == 0) {
 			gameLogic.setInitialSpeed(0.5f);
 			gameLogicIA.setInitialSpeed(0.5f);
 			gameLogic.setNColors(3);
 			gameLogicIA.setNColors(4);
 			ia = new IAEasy(gameLogicIA);
 		} else if (level == 1) {
 			gameLogic.setInitialSpeed(0.5f);
 			gameLogicIA.setInitialSpeed(0.5f);
 			gameLogic.setNColors(4);
 			gameLogicIA.setNColors(4);
 			ia = new IAHard(gameLogicIA);
 		} else if (level == 2) {
 			gameLogic.setInitialSpeed(0.3f);
 			gameLogicIA.setInitialSpeed(0.3f);
 			gameLogic.setNColors(4);
 			gameLogicIA.setNColors(4);
 			gameLogicIA.setCheatMode(true);
 			ia = new IAHard(gameLogicIA);
 		} else if (level == 3) {
 			gameLogic.setInitialSpeed(0.15f);
 			gameLogicIA.setInitialSpeed(0.3f);
 			gameLogic.setNColors(5);
 			gameLogicIA.setNColors(5);
 			gameLogicIA.setCheatMode(true);
 			ia = new IAHard(gameLogicIA);
 		}
 	}
 	public int getLevel() {
 		return level;
 	}
 	
 	protected void initGraphics() {
 		stage = new Stage(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(),
 				false);
 		controller = new Controller(gameLogic, stage);
 		gameOver = null;
 
 		gridActor = new GridActor(gameLogic, 296, 26, 54, 48);
 		nextPieceActor = new NextPieceActor(gameLogic, 95, 500, 48);
 		ScoreActor scoreActor = new ScoreActor(gameLogic, 550, 743);
 
 		gridActorIA = new GridActor(gameLogicIA, 650, 26, 54, 48);
 		nextPieceActorIA = new NextPieceActor(gameLogicIA, 1066, 500, 48);
 		ScoreActor scoreActorIA = new ScoreActor(gameLogicIA, 830, 743);
 		
 		TextureRegion pauseRegion = new TextureRegion(PuyoPuyo.getInstance().atlasBouttons.findRegion("pause_button"));
 
 		stage.addActor(new BackgroundActor(ScreenOrientation.LANDSCAPE));
 		stage.addActor(new LandscapePanelActor());
 		
 		stage.addActor(gridActor);
 		stage.addActor(nextPieceActor);
 		stage.addActor(scoreActor);
 		stage.addActor(gridActorIA);
 		stage.addActor(nextPieceActorIA);
 		stage.addActor(scoreActorIA);
 
 		controllerActor = new ControlerActor(ScreenOrientation.LANDSCAPE, gameLogic);
 		stage.addActor(controllerActor);
 		
 		pauseButton = new PauseButton(pauseRegion);
 		addButton(pauseButton,10,VIRTUAL_HEIGHT-10-pauseRegion.getRegionHeight());
 		addButton(MusicButtonActor.createMusicButton(),VIRTUAL_WIDTH-64, VIRTUAL_HEIGHT-64);
 		addButton(SoundButtonActor.createSoundButton(),VIRTUAL_WIDTH-2*64-10, VIRTUAL_HEIGHT-64);
 		
 		pauseMenu = new PauseMenu(this, ScreenOrientation.LANDSCAPE);
 		stage.addActor(pauseMenu);
 		
 		gameOver = new GameOverActor(this, VIRTUAL_WIDTH/2, 3*VIRTUAL_HEIGHT/4);
 		stage.addActor(gameOver);
 		
 		startActor = new StartActor(this);
 		startActor.show();
 		stage.addActor(startActor);
 
 	}
 
 	public static GameVersusScreen getInstance() {
 		if (instance == null) {
 			instance = new GameVersusScreen();
 			instance.initGraphics();
 		}
 		return instance;
 	}
 	
 	public static void freeInstance() {
 		instance = null;
 	}
 	
 	@Override
 	public void dispose() {
 		Gdx.app.log("Versus", "paused");
 	}
 
 	@Override
 	public void hide() {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void pause() {
 		if (!gamePaused && gameLogic.getState() != State.LOST) {
 			pauseMenu.show();
 		}
 	}
 
 	@Override
 	public void render(float delta) {
 		GLCommon gl = Gdx.gl;
 
 		if (gameLogic.getState() != State.LOST
 				&& gameLogicIA.getState() != State.LOST) {
 			if (!gamePaused) {
 				this.elapsedTime += delta;
 			}
 			
 			// Update model
 			gameLogic.update(delta);
 			gameLogicIA.update(delta);
 			ia.update(delta);
 			if (gameLogic.getState() == State.LOST
 					|| gameLogicIA.getState() == State.LOST) {
				gameOver();
 			}
 		}
 
 		// clear previous frame
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
 		resize(0, 0);
 		show();
 	}
 
 	@Override
 	public void show() {
 		Gdx.input.setInputProcessor(controller);
 	}
 
 	@Override
 	public void init() {
 		elapsedTime = 0;
 		gameLogic.init();
 		gameLogicIA.init();
 		gameOver.hide();
 		startActor.show();
 	}
 
	private void gameOver() {
		if(gameLogic.getState() == State.LOST){
			gameOver.show(GameOverType.LOSE);
		} else {
			gameOver.show(GameOverType.WIN);
		}
		controllerActor.touchable = false;
		pauseButton.touchable = false;
	}
	
 	@Override
 	public void gamePause() {
 		gamePaused = true;
 		gameLogic.pause();
 		gameLogicIA.pause();
 		gridActor.visible = false;
 		gridActorIA.visible = false;
 		nextPieceActor.visible = false;
 		nextPieceActorIA.visible = false;
 		controllerActor.touchable = false;
 		pauseButton.touchable = false;
 	}
 
 	@Override
 	public void gameResume() {
 		gamePaused = false;
 		gameLogic.resume();
 		gameLogicIA.resume();
 		gridActor.visible = true;
 		gridActorIA.visible = true;
 		nextPieceActor.visible = true;
 		nextPieceActorIA.visible = true;
 		controllerActor.touchable = true;
 		pauseButton.touchable = true;
 	}
 
 	@Override
 	public ScreenOrientation getOrientation() {
 		return ScreenOrientation.LANDSCAPE;
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
 	public GameType getGameType() {
 		return GameType.VERSUS_IA;
 	}
 
 	@Override
 	public int getScore() {
 		if (gameLogic.getState() == State.LOST) {
 			return 0;
 		} else {
 			return gameLogic.getScore() + getTimeBonus();
 		}
 	}
 	
 	public int getTimeBonus() {
 		return Math.max(0, 25000 - (int)(elapsedTime * 50));
 	}
 
 }
