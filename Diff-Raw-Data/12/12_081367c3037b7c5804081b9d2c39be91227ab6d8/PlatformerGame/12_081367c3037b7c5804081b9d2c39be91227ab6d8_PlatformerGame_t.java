 package platformer.core;
 
 import java.util.Collection;
 
 import platformer.core.model.Controllable;
 import platformer.core.model.Director;
 import platformer.core.model.GameState;
 import platformer.core.model.InputHandler;
 import platformer.core.model.Level;
 import platformer.core.model.Player;
 import platformer.core.model.ViewportRender;
 import platformer.core.model.command.Command;
 import platformer.core.model.director.impl.DefaultDirector;
 import platformer.core.model.gameobject.impl.DummyCharacter;
 import platformer.core.model.gamestate.impl.GameStateImpl;
 import platformer.core.model.inputhandler.impl.DefaultInputHandler;
 import platformer.core.model.level.impl.DummyLevel;
 import platformer.core.model.viewportrender.impl.DefaultViewportRender;
 
 import com.badlogic.gdx.Application;
 import com.badlogic.gdx.ApplicationListener;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.GL10;
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 
 public class PlatformerGame implements ApplicationListener {
 	Texture texture;
 	SpriteBatch batch;
 	private Level level;
 	private InputHandler inputHandler;
 	private Director director;
 	private Player player;
 	private GameState gameState;
 	private ViewportRender renderer;
 	private OrthographicCamera camera;
 
 	private final int LOG_LEVEL = Application.LOG_DEBUG;
 	private final int SCREEN_WIDTH = 1024;
 	private final int SCREEN_HEIGHT = 768;
 	private Controllable playerControlled;
 
 	@Override
 	public void create() {
 		Gdx.app.setLogLevel(LOG_LEVEL);
 
 		// Create/load level;
 		level = new DummyLevel();
 		
 		// Setup input handler;
 		inputHandler = new DefaultInputHandler(Gdx.input);
 		
 		// Setup director;
 		director = new DefaultDirector();
 		
 		// Setup camera
 		camera = new OrthographicCamera();
 		camera.setToOrtho(false, 800, 480);
 		
 		// Setup Renderer
 		renderer = new DefaultViewportRender(Gdx.graphics, camera);
 	}
 
 	@Override
 	public void resize(int width, int height) {
 	}
 
 	@Override
 	public void render() {
 		//Clear screen to black
 		Gdx.gl.glClearColor(0, 0, 0, 0);
 		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
 		
 		Collection<Command> inputState = inputHandler.readInput();
 		director.addCommand(inputState);
		director.update();
 
 		GameState gameState = director.getGameState();
 		
 		renderer.render(gameState.getRenderableObjects());
 		gameState.cleanUp();
 	}
 
 	@Override
 	public void pause() {
 	}
 
 	@Override
 	public void resume() {
 	}
 
 	@Override
 	public void dispose() {
 	}
 }
