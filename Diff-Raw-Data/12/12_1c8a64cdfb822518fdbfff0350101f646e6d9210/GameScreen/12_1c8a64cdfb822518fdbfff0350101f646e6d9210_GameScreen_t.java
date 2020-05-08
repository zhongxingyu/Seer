 package com.mark.games.fallingblocks;
 
 import java.util.List;
 
 import javax.microedition.khronos.opengles.GL10;
 
 import com.mark.games.fallingblocks.framework.Game;
 import com.mark.games.fallingblocks.framework.Input.TouchEvent;
 import com.mark.games.fallingblocks.framework.Screen;
 import com.mark.games.fallingblocks.framework.collision.OverlapTester;
 import com.mark.games.fallingblocks.framework.collision.SpatialHashGrid;
 import com.mark.games.fallingblocks.framework.gl.Camera2D;
 import com.mark.games.fallingblocks.framework.gl.FPSCounter;
 import com.mark.games.fallingblocks.framework.gl.SpriteBatcher;
 import com.mark.games.fallingblocks.framework.impl.GLGame;
 import com.mark.games.fallingblocks.framework.impl.GLGraphics;
 import com.mark.games.fallingblocks.framework.math.Rectangle;
 import com.mark.games.fallingblocks.framework.math.Vector2;
 
 public class GameScreen extends Screen {
 	GLGraphics glGraphics;
 
 	Vector2 touchPos;
 
 	static Camera2D camera;
 
 	FPSCounter fpsCounter;
 	int fps;
 
 	Vector2 touchPosition = new Vector2();
 	SpriteBatcher batcher;
 	GameWorld world;
 	GameRenderer renderer;
 	
 	Rectangle pauseButton;
 
 	public GameScreen(Game game) {
 		super(game);
 		glGraphics = ((GLGame) game).getGLGraphics();
 		
 		batcher = new SpriteBatcher(glGraphics, 100);
 		world = new GameWorld();
 		renderer = new GameRenderer(glGraphics, batcher, world);
 		camera = new Camera2D(glGraphics, renderer.camera.position.x, renderer.camera.position.y);
 		fpsCounter = new FPSCounter();
 		touchPos = new Vector2();
 	}
 
 	@Override
 	public void update(float deltaTime) {
 		updateState(deltaTime);
 	}
 	
 	public void updateState(float deltaTime) {
 		switch(world.state){ 
 		case GameWorld.GAME_GAMEOVER:
 			updateGameOver();
 			break;
 		case GameWorld.GAME_PAUSED:
 			updatePaused();
 			break;
 		case GameWorld.GAME_RUNNING:
 			updateRunning(deltaTime);
 			break;
 		}
 	}
 
 	public void updateGameOver() {
 		game.setScreen(new LossScreen(game));
         Settings.addScore(world.score);
         Settings.save(game.getFileIO());
 	}
 	
 	public void updatePaused() {
 		pauseButton = new Rectangle(renderer.FRUSTUM_WIDTH - 60, camera.position.y + renderer.FRUSTUM_HEIGHT/2-40, 60, 40);
 		
 		List<TouchEvent> touchEvents = game.getInput().getTouchEvents();
 		game.getInput().getKeyEvents();
 		
 		int len = touchEvents.size();
 		for (int i = 0; i < len; i++) {
 			TouchEvent event = touchEvents.get(i);
 			touchPosition.set(event.x, event.y);
 			renderer.camera.touchToWorld(touchPosition);
 			if (event.type == TouchEvent.TOUCH_DOWN) {
 				if(OverlapTester.pointInRectangle(pauseButton, touchPosition)){
 					world.state = GameWorld.GAME_RUNNING;	
 				}
 			}
 		}
 		fps = fpsCounter.getFps();
 	}
 	
 	public void updateRunning(float deltaTime) {
 		world.update(deltaTime,game.getInput().getAccelX());
 		world.checkCollisions(deltaTime);
 		
 		pauseButton = new Rectangle(renderer.FRUSTUM_WIDTH - 60, camera.position.y + renderer.FRUSTUM_HEIGHT/2-40, 60, 40);
 		
 		List<TouchEvent> touchEvents = game.getInput().getTouchEvents();
 		game.getInput().getKeyEvents();
 		
 		int len = touchEvents.size();
 		for (int i = 0; i < len; i++) {
 			TouchEvent event = touchEvents.get(i);
 			touchPosition.set(event.x, event.y);
 			renderer.camera.touchToWorld(touchPosition);
 			if (event.type == TouchEvent.TOUCH_DOWN) {
 				world.character.jump();
 			}
 			if (event.type == TouchEvent.TOUCH_UP) {
 				if(OverlapTester.pointInRectangle(pauseButton, touchPosition)){
 					world.state = GameWorld.GAME_PAUSED;					
 				}	
 			}
 		}
 		fps = fpsCounter.getFps();
 	}
 	
 	@Override
 	public void present(float deltaTime) {
 		GL10 gl = glGraphics.getGL();
 	
 		gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
 		gl.glClearColor(1, 1, 1, 1);
 		
 		gl.glEnable(GL10.GL_TEXTURE_2D);
 		
 		renderer.render();
 		camera.position.y = renderer.camera.position.y;
 		
 		gl.glEnable(GL10.GL_BLEND);
 		gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
 		
 		drawGUI();
 		
 		Assets.gameScreenText.begin(0f, 0f, 0f, 1.0f);
 		
 		if (Settings.showFps) {
 			Assets.gameScreenText.draw("FPS: " + Integer.toString(fps), 10, camera.position.y + 215);
 			Assets.gameScreenText.draw("Score:" + Integer.toString(world.score), 10, camera.position.y + 195);
 		} else
 			Assets.gameScreenText.draw("Score:" + Integer.toString(world.score), 10, camera.position.y + 215);
 		Assets.gameScreenText.end();
 	}
 
 	public void drawGUI() {
 		batcher.beginBatch(Assets.icons);
 		batcher.drawSprite(300, camera.position.y + renderer.FRUSTUM_HEIGHT / 2 - 15, 10, 15, Assets.pauseButton);
 		batcher.endBatch();
 		
 		//replace the gltext with an image maybe?
 		displayPause();
 	}
 	
 	public void displayPause() {
 		if(world.state == GameWorld.GAME_PAUSED) {
 			Assets.gameScreenText.begin(0.0f, 0.0f, 0.0f, 1.0f);
 			Assets.gameScreenText.draw("PAUSED", 140, camera.position.y);
 			Assets.gameScreenText.end();
 		}
 	}

 	
 	@Override
 	public void resume() {
 		Settings.load(((GLGame)game).getFileIO(), ".falling");
 	}
 
 	@Override
 	public void pause() {
 
 	}
 
 	@Override
 	public void dispose() {
 
 	}
 
 }
