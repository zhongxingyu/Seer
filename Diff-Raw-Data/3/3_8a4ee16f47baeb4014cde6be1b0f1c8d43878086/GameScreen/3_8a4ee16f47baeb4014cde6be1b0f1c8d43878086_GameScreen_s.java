 package com.linkmongrel.games.coonalicious;
 
 import com.badlogic.gdx.Application;
 import com.badlogic.gdx.Game;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Input.Keys;
 import com.badlogic.gdx.Screen;
 import com.badlogic.gdx.graphics.GL10;
 import com.badlogic.gdx.graphics.GLCommon;
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.math.Rectangle;
 import com.badlogic.gdx.math.Vector3;
 import com.linkmongrel.games.coonalicious.World.WorldListener;
 
 public class GameScreen implements Screen {
 	static final int GAME_READY = 0;
 	static final int GAME_RUNNING = 1;
 	static final int GAME_PAUSED = 2;
 	static final int GAME_LEVEL_END = 3;
 	static final int GAME_OVER = 4;
 
 	Game game;
 	ScoreloopInterface scoreloop;
 
 	int state;
 	OrthographicCamera guiCam;
 	Vector3 touchPoint;
 	SpriteBatch batcher;
 	World world;
 	WorldListener worldListener;
 	WorldRenderer renderer;
 	Rectangle pauseBounds;
 	Rectangle resumeBounds;
 	Rectangle quitBounds;
 	int lastScore;
 	int time;
 	String scoreString;
 	private String timeString;
 	private boolean gameOver = true;
 
 	public GameScreen(Game game, ScoreloopInterface scoreloop) {
 		this.game = game;
 		this.scoreloop = scoreloop;
 
 		state = GAME_READY;
 		guiCam = new OrthographicCamera(Gdx.graphics.getHeight(),
 				Gdx.graphics.getWidth());
 		guiCam.position.set(Gdx.graphics.getHeight() / 2,
 				Gdx.graphics.getWidth() / 2, 0);
 		touchPoint = new Vector3();
 		batcher = new SpriteBatch();
 		worldListener = new WorldListener() {
 			@Override
 			public void jump() {
 				Assets.playSound(Assets.jumpSound);
 				Gdx.input.vibrate(70);
 			}
 
 			@Override
 			public void highJump() {
 				Assets.playSound(Assets.highJumpSound);
 			}
 
 			@Override
 			public void eat() {
 				Assets.playSound(Assets.hitSound);
 			}
 		};
 		world = new World(worldListener);
 		renderer = new WorldRenderer(batcher, world);
 		pauseBounds = new Rectangle(Gdx.graphics.getHeight() - 64,
 				Gdx.graphics.getWidth() - 64, 64, 64);
 		resumeBounds = new Rectangle((Gdx.graphics.getHeight() / 2)
 				- (Gdx.graphics.getHeight() / 6),
 				((Gdx.graphics.getWidth() / 4) * 2.5f),
 				(Gdx.graphics.getHeight() / 3),
 				(Gdx.graphics.getHeight() / 3) * 0.8f);
 		quitBounds = new Rectangle((Gdx.graphics.getHeight() / 2)
 				- (Gdx.graphics.getHeight() / 6),
 				((Gdx.graphics.getWidth() / 4) * 1.5f),
 				(Gdx.graphics.getHeight() / 3),
 				(Gdx.graphics.getHeight() / 3) * 0.8f);
 		lastScore = 0;
 		time = 0;
 		timeString = "0:00";
 		scoreString = "0";
 	}
 
 	public void update(float deltaTime) {
 		if (deltaTime > 0.1f)
 			deltaTime = 0.1f;
 
 		switch (state) {
 		case GAME_READY:
 			gameOver = true;
 			updateReady();
 			break;
 		case GAME_RUNNING:
 			updateRunning(deltaTime);
 			break;
 		case GAME_PAUSED:
 			updatePaused();
 			break;
 		case GAME_LEVEL_END:
 			updateLevelEnd();
 			break;
 		case GAME_OVER:
 			if (gameOver)
 				scoreloop.OpenScoreloopLeaderboard(time);
 			updateGameOver();
 			gameOver = false;
 			break;
 		}
 	}
 
 	private void updateReady() {
 		if (Gdx.input.justTouched()) {
 			state = GAME_RUNNING;
 		}
 	}
 
 	private void updateRunning(float deltaTime) {
 		if (Gdx.input.justTouched()) {
 			guiCam.unproject(touchPoint.set(Gdx.input.getX(), Gdx.input.getY(),
 					0));
 
 			if (OverlapTester.pointInRectangle(pauseBounds, touchPoint.x,
 					touchPoint.y)) {
 				Assets.playSound(Assets.clickSound);
 				state = GAME_PAUSED;
 				return;
 			}
 		}
 
 		if (Gdx.app.getType() == Application.ApplicationType.Android) {
 			world.update(deltaTime, -Gdx.input.getAccelerometerY());
 			if (Gdx.input.justTouched()) {
 				world.jump();
 			}
 		} else {
 			float accel = 0;
 			if (Gdx.input.isKeyPressed(Keys.DPAD_LEFT))
 				accel = 5f;
 			if (Gdx.input.isKeyPressed(Keys.DPAD_RIGHT))
 				accel = -5f;
 			if (Gdx.input.isKeyPressed(Keys.DPAD_UP))
 				world.rikki.jump();
 			world.update(deltaTime, accel);
 		}
 		if (world.score != lastScore) {
 			lastScore = world.score;
 			scoreString = "" + lastScore;
 		}
 		if (world.clock != time) {
 			time = world.clock;
 			int minutes = (time / 60);
 			int seconds = (time % 60);
 			String secondsString;
 			if (seconds < 10)
 				secondsString = "0" + seconds;
 			else
 				secondsString = "" + seconds;
 			timeString = "" + minutes + ":" + secondsString;
 		}
 		if (world.state == World.WORLD_STATE_NEXT_LEVEL) {
 			state = GAME_LEVEL_END;
 		}
 		if (world.state == World.WORLD_STATE_GAME_OVER) {
 			state = GAME_OVER;
 		}
 	}
 
 	private void updatePaused() {
 		if (Gdx.input.justTouched()) {
 			guiCam.unproject(touchPoint.set(Gdx.input.getX(), Gdx.input.getY(),
 					0));
 
 			if (OverlapTester.pointInRectangle(resumeBounds, touchPoint.x,
 					touchPoint.y)) {
 				Assets.playSound(Assets.clickSound);
 				state = GAME_RUNNING;
 				return;
 			}
 
 			if (OverlapTester.pointInRectangle(quitBounds, touchPoint.x,
 					touchPoint.y)) {
 				Assets.playSound(Assets.clickSound);
 				game.setScreen(new MainMenuScreen(game, scoreloop));
 				return;
 			}
 		}
 	}
 
 	private void updateLevelEnd() {
 		if (Gdx.input.justTouched()) {
 			world = new World(worldListener);
 			renderer = new WorldRenderer(batcher, world);
 			world.score = lastScore;
 			state = GAME_READY;
 		}
 	}
 
 	private void updateGameOver() {
 		if (Gdx.input.justTouched()) {
 			game.setScreen(new MainMenuScreen(game, scoreloop));
 		}
 	}
 
 	public void draw(float deltaTime) {
 		GLCommon gl = Gdx.gl;
 		gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
 
 		renderer.render();
 
 		guiCam.update();
 		batcher.setProjectionMatrix(guiCam.combined);
 		batcher.enableBlending();
 		batcher.begin();
 		switch (state) {
 		case GAME_READY:
 			presentReady();
 			break;
 		case GAME_RUNNING:
 			presentRunning();
 			break;
 		case GAME_PAUSED:
 			presentPaused();
 			break;
 		case GAME_OVER:
 			presentGameOver();
 			break;
 		}
 		batcher.end();
 	}
 
 	private void presentReady() {
 		batcher.draw(Assets.ready, (Gdx.graphics.getHeight() / 2)
 				- (Gdx.graphics.getHeight() / 3),
 				((Gdx.graphics.getWidth() / 2)),
 				(Gdx.graphics.getHeight() / 1.5f),
 				(Gdx.graphics.getHeight() / 2) * 0.8f);
 	}
 
 	private void presentRunning() {
 		Assets.font.draw(batcher, timeString, Gdx.graphics.getHeight() - 50,
 				Gdx.graphics.getWidth() - 20);
 		Assets.font.draw(batcher, scoreString, 8, Gdx.graphics.getWidth() - 20);
 	}
 
 	private void presentPaused() {
 		batcher.draw(Assets.resume, (Gdx.graphics.getHeight() / 2)
 				- (Gdx.graphics.getHeight() / 6),
 				((Gdx.graphics.getWidth() / 4) * 2.5f),
 				(Gdx.graphics.getHeight() / 3),
 				(Gdx.graphics.getHeight() / 3) * 0.8f);
 		batcher.draw(Assets.quit, (Gdx.graphics.getHeight() / 2)
 				- (Gdx.graphics.getHeight() / 6),
 				((Gdx.graphics.getWidth() / 4) * 1.5f),
 				(Gdx.graphics.getHeight() / 3),
 				(Gdx.graphics.getHeight() / 3) * 0.8f);
 
 		// Assets.font.draw(batcher, scoreString, 16,
 		// (Gdx.graphics.getWidth() / 2) - 20);
 	}
 
 	private void presentGameOver() {
 		// batcher.draw(Assets.gameOver, 160 - 160 / 2, 240 - 96 / 2, 160, 96);
 		// float scoreWidth = Assets.font.getBounds(time+"").width;
 		float gameOver = Assets.font.getBounds("GAME OVER").width;
 		Assets.font.draw(batcher, "GAME OVER", (Gdx.graphics.getHeight() / 2)
 				- gameOver / 2, Gdx.graphics.getWidth() - 20);
 		// Assets.font.draw(batcher, scoreString, 160 - scoreWidth / 2, 480 -
 		// 20);
 	}
 
 	@Override
 	public void render(float delta) {
 		update(delta);
 		draw(delta);
 	}
 
 	@Override
 	public void resize(int width, int height) {
 	}
 
 	@Override
 	public void show() {
 	}
 
 	@Override
 	public void hide() {
 	}
 
 	@Override
 	public void pause() {
 		if (state == GAME_RUNNING)
 			state = GAME_PAUSED;
 	}
 
 	@Override
 	public void resume() {
 	}
 
 	@Override
 	public void dispose() {
 	}
 }
