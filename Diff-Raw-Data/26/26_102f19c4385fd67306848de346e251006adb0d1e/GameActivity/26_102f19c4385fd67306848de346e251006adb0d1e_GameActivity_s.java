 package pl.fzymek.android.cycloneeye.game.screens;
 
 import static android.opengl.GLES10.*;
 
 import java.util.List;
 
 import javax.microedition.khronos.egl.EGLConfig;
 import javax.microedition.khronos.opengles.GL10;
 
 import pl.fzymek.android.cycloneeye.database.providers.HighscoresProvider;
 import pl.fzymek.android.cycloneeye.game.cyclone.Assets;
 import pl.fzymek.android.cycloneeye.game.cyclone.Cyclone;
 import pl.fzymek.android.cycloneeye.game.cyclone.Settings;
 import pl.fzymek.android.cycloneeye.game.cyclone.World;
 import pl.fzymek.android.cycloneeye.game.cyclone.WorldRenderer;
 import pl.fzymek.android.cycloneeye.game.engine.Input.TouchEvent;
 import pl.fzymek.android.cycloneeye.game.engine.Screen;
 import pl.fzymek.android.cycloneeye.game.engine.gl.Camera2D;
 import pl.fzymek.android.cycloneeye.game.engine.gl.FPSCounter;
 import pl.fzymek.android.cycloneeye.game.engine.gl.SpriteBatcher;
 import pl.fzymek.android.cycloneeye.game.engine.impl.CEGame;
 import pl.fzymek.android.cycloneeye.game.engine.impl.CEScreen;
 import pl.fzymek.android.cycloneeye.game.engine.math.Rectangle;
 import pl.fzymek.android.cycloneeye.game.engine.math.Vector2;
 import android.annotation.TargetApi;
 import android.content.ContentResolver;
 import android.content.ContentValues;
 import android.content.Context;
 import android.os.Build;
 import android.os.Vibrator;
 import android.util.Log;
 import android.view.Menu;
 
 public class GameActivity extends CEGame {
 
 	private boolean firstTimeLoading = true;
 
 	@TargetApi(Build.VERSION_CODES.ECLAIR)
 	public static class GameScreen extends CEScreen {
 
 		static final int GAME_READY = 0;
 		static final int GAME_RUNNING = 1;
 		static final int GAME_PAUSED = 2;
 		static final int GAME_LEVEL_END = 3;
 		static final int GAME_OVER = 4;
 
		static final int GUI_WIDTH = 480;
		static final int GUI_HEIGHT = 854;
 		final String TAG = GameScreen.class.getSimpleName();
 
 		static int state;
 		Camera2D guiCamera;
 		Vector2 touchPoint;
 		SpriteBatcher batcher;
 		World world;
 		World.WorldEventsListener listener;
 		WorldRenderer renderer;
 		Rectangle resumeBounds;
 		Rectangle pauseBounds;
 		Rectangle quitBounds;
 		FPSCounter fpsCounter;
 
 		public GameScreen(CEGame game) {
 			super(game);
 			state = GAME_READY;
 			guiCamera = new Camera2D(glGraphics, GUI_WIDTH, GUI_HEIGHT);
 			touchPoint = new Vector2();
 			batcher = new SpriteBatcher(glGraphics, 500);
 			fpsCounter = new FPSCounter();
 
 			Log.d(TAG, "Creating GameScreen, camera, batcher state created");
 
 			listener = new World.WorldEventsListener() {
 
 				final Vibrator vibr = (Vibrator) GameScreen.this.game
 						.getSystemService(Context.VIBRATOR_SERVICE);
 				final String tag = "WorldEventsListener";
 
 				@Override
 				public void target() {
 					Log.d(tag, "target hit");
 					if (Settings.soundsEnabled) {
 						Assets.explosionSound.play(1.0f);
 					}
 				}
 
 				@Override
 				public void powerUp() {
 					Log.d(tag, "power up hit");
 					if (Settings.soundsEnabled) {
 						Assets.collectSound.play(1.0f);
 					}
 				}
 
 				@Override
 				public void obstacle() {
 					Log.d(tag, "obstacle hit");
 					if (Settings.vibrationsEnabled) {
 						vibr.vibrate(50);
 					}
 				}
 
 				@Override
 				public void gameOver() {
 					Log.d(tag, "game over");
 
 					if (Settings.soundsEnabled) {
 						Assets.gameOverSound.play(1.0f);
 					}
 
 					if (Settings.vibrationsEnabled) {
 						vibr.vibrate(120);
 					}
 				}
 
 				@Override
 				public void levelEnd() {
 
 					if (Settings.soundsEnabled) {
 						Assets.levelUpSound.play(1.0f);
 					}
 
 					if (Settings.vibrationsEnabled) {
 						vibr.vibrate(new long[] { 0, 50, 50, 50, 50, 50 }, -1);
 					}
 				}
 			};
 
 			world = new World(listener);
 			renderer = new WorldRenderer(glGraphics, batcher, world);
 
 			Log.d(TAG, "World created");
 		}
 
 		@Override
 		public void update(float deltaTime) {
 
 			if (deltaTime > 0.1f) {
 				deltaTime = 0.1f;
 			}
 
 			switch (state) {
 				case GAME_READY:
 					goToReady();
 					break;
 				case GAME_RUNNING:
 					goToRunning(deltaTime);
 					break;
 				case GAME_PAUSED:
 					goToPaused();
 					break;
 				case GAME_LEVEL_END:
 					goToLevelEnd();
 					break;
 				case GAME_OVER:
 					goToGameOver();
 					break;
 
 				default:
 					break;
 			}
 
 		}
 
 		private void goToGameOver() {
 
 			final List<TouchEvent> touchEvents = game.getInput()
 					.getTouchEvents();
 			final int len = touchEvents.size();
 			for (int i = 0; i < len; i++) {
 				final TouchEvent event = touchEvents.get(i);
 				if (event.type != TouchEvent.TOUCH_UP)
 					continue;
 
 				game.finish();
 				return;
 			}
 
 		}
 
 		private void goToLevelEnd() {
 
 			final List<TouchEvent> touchEvents = game.getInput()
 					.getTouchEvents();
 			final int len = touchEvents.size();
 			for (int i = 0; i < len; i++) {
 				final TouchEvent event = touchEvents.get(i);
 				if (event.type != TouchEvent.TOUCH_UP) {
 					continue;
 				}
 				int lastScore = world.score;
 				world = new World(listener);
 				renderer = new WorldRenderer(glGraphics, batcher, world);
 				world.score = lastScore;
 				state = GAME_READY;
 				return;
 			}
 		}
 
 		private void goToPaused() {
 			final List<TouchEvent> touchEvents = game.getInput()
 					.getTouchEvents();
 			final int len = touchEvents.size();
 			for (int i = 0; i < len; i++) {
 				final TouchEvent event = touchEvents.get(i);
 				if (event.type != TouchEvent.TOUCH_UP) {
 					continue;
 				}
 				state = GAME_RUNNING;
 				return;
 			}
 
 		}
 
 		private void goToRunning(float deltaTime) {
 			// Log.d(TAG, "RUNNING, updating world");
 			world.update(deltaTime, game.getInput().getAccelX());
 
 			if (world.state == World.WORLD_STATE_NEXT_LEVEL) {
 				state = GAME_LEVEL_END;
 			}
 
 			if (world.state == World.WORLD_STATE_GAME_OVER) {
 				state = GAME_OVER;
 
 				final ContentResolver resolver = game.getContentResolver();
 				final ContentValues values = new ContentValues();
 				values.put(HighscoresProvider.TableMetadata.PLAYER,
 						Settings.name);
 				values.put(HighscoresProvider.TableMetadata.SCORE, world.score);
 
 				resolver.insert(HighscoresProvider.TableMetadata.CONTENT_URI,
 						values);
 			}
 		}
 
 		private void goToReady() {
 			final List<TouchEvent> touchEvents = game.getInput()
 					.getTouchEvents();
 			final int len = touchEvents.size();
 			for (int i = 0; i < len; i++) {
 				final TouchEvent event = touchEvents.get(i);
 				if (event.type != TouchEvent.TOUCH_UP) {
 					continue;
 				}
 				state = GAME_RUNNING;
 				return;
 			}
 		}
 
 		@Override
 		public void present(float deltaTime) {
 
 			final GL10 gl = glGraphics.getGl();
 			gl.glClear(GL_COLOR_BUFFER_BIT);
 			gl.glEnable(GL_TEXTURE_2D);
 
 			// Log.d(TAG, "Rendering with renderer");
 			renderer.render();
 			//
 			guiCamera.setViewportAndMatrices();
 			// Log.d(TAG, "camera viewport set");
 
 			gl.glEnable(GL_BLEND);
 			gl.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
 			// batcher.beginBatch(Assets.text);
 
 			switch (state) {
 
 				case GAME_RUNNING:
 					gl.glLoadIdentity();
 					Assets.font.PrintAt(gl, "Score: " + world.score, 0,
 							GUI_HEIGHT - Assets.font.GetTextHeight());
 					final String lifes = "Lifes: "
 							+ (Cyclone.MAX_OBSTACLES_HIT - world.cyclone.numberOfObstaclesHit);
 
 					Assets.font.PrintAt(gl, lifes,
 							GUI_WIDTH - Assets.font.GetTextLength(lifes),
 							GUI_HEIGHT - Assets.font.GetTextHeight());
 					break;
 				case GAME_READY:
 					gl.glLoadIdentity();
 					final String msg = "Tap to start";
 					Assets.font.PrintAt(gl, msg,
 							(int) ((int) (GUI_WIDTH / 2) - Assets.font
 									.GetTextLength(msg) / 2),
 							(int) (GUI_HEIGHT / 2));
 					break;
 				case GAME_PAUSED:
 					gl.glLoadIdentity();
 					final String msg1 = "PAUSED";
 					final String msg2 = "Tap to resume";
 					Assets.font.PrintAt(gl, msg1, (int) (GUI_WIDTH / 2)
 							- Assets.font.GetTextLength(msg1) / 2,
 							(int) (GUI_HEIGHT / 2));
 					Assets.font.PrintAt(gl, msg2, (int) (GUI_WIDTH / 2)
 							- Assets.font.GetTextLength(msg2) / 2, 0);
 					break;
 				case GAME_LEVEL_END:
 					gl.glLoadIdentity();
 					final String msg11 = "Level Finished";
 					final String msg22 = "Tap to continue";
 					Assets.font.PrintAt(gl, msg11, (int) (GUI_WIDTH / 2)
 							- Assets.font.GetTextLength(msg11) / 2,
 							(int) (GUI_HEIGHT / 2));
 					Assets.font.PrintAt(gl, msg22, (int) (GUI_WIDTH / 2)
 							- Assets.font.GetTextLength(msg22) / 2, 0);
 					break;
 				case GAME_OVER:
 					gl.glLoadIdentity();
 					final String msg111 = "GAME OVER";
 					final String score = "Your score: " + world.score;
 					final String msg222 = "Tap to quit";
 					Assets.font.PrintAt(gl, msg111, (int) (GUI_WIDTH / 2)
 							- Assets.font.GetTextLength(msg111) / 2,
 							(int) (GUI_HEIGHT / 2));
 					Assets.font.PrintAt(gl, score, (int) (GUI_WIDTH / 2)
 							- Assets.font.GetTextLength(score) / 2,
 							(int) (GUI_HEIGHT / 3));
 					Assets.font.PrintAt(gl, msg222, (int) (GUI_WIDTH / 2)
 							- Assets.font.GetTextLength(msg222) / 2, 0);
 
 				default:
 					break;
 			}
 			// batcher.endBatch();
 
 			fpsCounter.LogFPS();
 
 		}
 
 		@Override
 		public void pause() {
 			if (state == GAME_READY) {
 				state = GAME_PAUSED;
 			}
 		}
 
 		@Override
 		public void resume() {
 			// TODO Auto-generated method stub
 
 		}
 
 		@Override
 		public void dispose() {
 			// TODO Auto-generated method stub
 
 		}
 
 	}
 
 	@Override
 	public Screen getStartScreen() {
 		Log.d("GameActivity", "Fetching game screen");
 		return new GameScreen(this);
 	}
 
 	@Override
 	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
 		super.onSurfaceCreated(gl, config);
 
 		if (firstTimeLoading) {
 			Log.d("GameActivity", "Loading assets");
 			Assets.load(this);
 			firstTimeLoading = false;
 		} else {
 			Log.d("GameActivity", "Reloading assets");
 			Assets.reload(this);
 		}
 
 	}
 
 	@Override
 	public boolean onPrepareOptionsMenu(Menu menu) {
 
 		Log.d("CEGame", "Menu pressed");
 		GameScreen.state = GameScreen.GAME_PAUSED;
 		return super.onPrepareOptionsMenu(menu);
 	}
 
 }
