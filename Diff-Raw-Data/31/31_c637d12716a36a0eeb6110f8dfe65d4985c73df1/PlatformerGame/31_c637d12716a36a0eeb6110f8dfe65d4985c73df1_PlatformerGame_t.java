 package platformer.core;
 
 import platformer.core.model.Controllable;
 import platformer.core.model.Director;
 import platformer.core.model.GameState;
 import platformer.core.model.InputHandler;
 import platformer.core.model.Level;
 import platformer.core.model.Player;
 import platformer.core.model.ViewportRender;
 import platformer.core.model.camera.impl.DefaultCamera;
 import platformer.core.model.command.Command;
 import platformer.core.model.director.impl.DefaultDirector;
 import platformer.core.model.inputhandler.impl.DefaultInputHandler;
 import platformer.core.model.systems.Positionable;
 import platformer.core.model.viewportrender.impl.DefaultViewportRender;
 import platformer.core.renderer.RendererFactory;
 import platformer.core.renderer.impl.dummy.DummyRendererFactory;
 
 import com.badlogic.gdx.Application;
 import com.badlogic.gdx.ApplicationListener;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.assets.AssetManager;
 import com.badlogic.gdx.graphics.GL10;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.math.Matrix4;
 import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
 import com.badlogic.gdx.utils.Array;
 
 public class PlatformerGame implements ApplicationListener {
 	Texture texture;
 	SpriteBatch batch;
 	private Level level;
 	private InputHandler inputHandler;
 	private Director director;
 	private Player player;
 	private GameState gameState;
 	private ViewportRender renderer;
 	private DefaultCamera camera;
 	private Box2DDebugRenderer debugRenderer;
 
 	private final int LOG_LEVEL = Application.LOG_DEBUG;
 	private final int SCREEN_WIDTH = 1024;
 	private final int SCREEN_HEIGHT = 768;
 	private Controllable playerControlled;
 	private RendererFactory rendererFactory;
 	private AssetManager assetManager;
 	private Matrix4 debugMatrix;
 
 	@Override
 	public void create() {
 		assetManager = new AssetManager();
 		assetManager.load("../assets/grass.png", Texture.class);
 
 		Gdx.app.setLogLevel(LOG_LEVEL);
 
 		rendererFactory = new DummyRendererFactory();
 		
 		// Setup director;
 		director = new DefaultDirector();
 
 		// Setup camera
 		camera = new DefaultCamera();
 		camera.setToOrtho(false, 800, 600);
 		camera.setTarget((Positionable) director.getGameState().findGameObjectById("player"));
 
 		// Setup input handler;
 		inputHandler = new DefaultInputHandler(Gdx.input, camera);
 		Gdx.input.setInputProcessor(inputHandler);
 
 		// Setup Renderer
 		renderer = new DefaultViewportRender(rendererFactory, Gdx.graphics,
 				camera);
 
 		debugRenderer = new Box2DDebugRenderer();
 		debugRenderer.setDrawVelocities(true);
 	}
 
 	@Override
 	public void resize(int width, int height) {
 	}
 
 	@Override
 	public void render() {
 		if (!assetManager.update()) {
 			Gdx.app.debug("asset loading", "loading assets");
 		}
 		// Clear screen to black
 		Gdx.gl.glClearColor(0, 0, 0, 0);
 		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
 
 		Array<Command> inputState = inputHandler.readInput();
 		director.addCommand(inputState);
 		director.update();
 
 		GameState gameState = director.getGameState();
 
 		renderer.render(gameState.getRenderableObjects());
 		
 		//Setup debug camera matrix and render
 		float ratio = director.getPhysicsSystem().getRatio();
 		debugMatrix = new Matrix4(camera.combined);
 		debugMatrix.scale(1 / ratio, 1 / ratio, 1f);
		//debugRenderer.render(director.getPhysicsSystem().getWorld(), debugMatrix);
 		
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
