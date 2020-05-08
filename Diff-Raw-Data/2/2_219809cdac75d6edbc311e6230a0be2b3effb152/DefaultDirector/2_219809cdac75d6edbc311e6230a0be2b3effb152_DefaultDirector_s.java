 package platformer.core.model.director.impl;
 
 import platformer.core.model.Director;
 import platformer.core.model.GameState;
 import platformer.core.model.Level;
 import platformer.core.model.camera.impl.DefaultCamera;
 import platformer.core.model.command.Command;
 import platformer.core.model.gameobject.impl.ExampleGameObject;
 import platformer.core.model.gamestate.impl.GameStateImpl;
 import platformer.core.model.inputhandler.impl.DefaultInputHandler;
 import platformer.core.model.level.impl.DummyLevel;
 import platformer.core.model.systems.Positionable;
 import platformer.core.model.systems.impl.physics.PhysicsSystem;
 import platformer.core.model.systems.impl.physics.bodies.RegularPlayer;
 import platformer.core.model.viewportrender.impl.DefaultViewportRender;
 import platformer.core.renderer.RendererFactory;
 import platformer.core.renderer.impl.asset.AssetRendererFactory;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.assets.AssetManager;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.math.Matrix4;
 import com.badlogic.gdx.math.Rectangle;
 import com.badlogic.gdx.math.Vector3;
 import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
 import com.badlogic.gdx.utils.Array;
 
 public class DefaultDirector implements Director {
 	private GameState gameState;
 	private Array<Command> commandList = new Array<Command>();
 	private PhysicsSystem physicsSystem;
 	private RendererFactory rendererFactory;
 	private AssetManager assetManager;
 	private DefaultCamera camera;
 	private DefaultInputHandler inputHandler;
 	private DefaultViewportRender renderer;
 	private Box2DDebugRenderer debugRenderer;
 	private Matrix4 debugMatrix;
 
 	public DefaultDirector() {
 		initializeGame();
 	}
 
 	private void initializeGame() {
 		physicsSystem = new PhysicsSystem();
 		gameState = new GameStateImpl();
 		Level level = new DummyLevel();
 		camera = new DefaultCamera();
 		debugRenderer = new Box2DDebugRenderer();
 		assetManager = new AssetManager();
 		
 		assetManager.load("assets/grass_single.png", Texture.class);
 		assetManager.load("assets/stickman.png", Texture.class);
 		rendererFactory = new AssetRendererFactory(assetManager);// = new
 																	// DummyRendererFactory();
 		camera.setToOrtho(false, 800, 600);
 		inputHandler = new DefaultInputHandler(Gdx.input, camera);
 		Gdx.input.setInputProcessor(inputHandler);
 
 		physicsSystem.initialize(level.getGravity());
 		level.initialize(physicsSystem); // REFACTOR
 		gameState.initialize(level);
 
 		renderer = new DefaultViewportRender(rendererFactory, Gdx.graphics,
 				camera);
 		debugRenderer.setDrawVelocities(true);
 
 		//Add player and follow
 		Vector3 testPosition = new Vector3(100, 100, 0);
 		Rectangle testBound = new Rectangle(0, 0, 64, 64);
 		ExampleGameObject object = new ExampleGameObject(testPosition,
 				testBound, physicsSystem.createBody(
 						RegularPlayer.class.getName(), testPosition, testBound));
 
 		gameState.addGameObject(object);
 		camera.setTarget((Positionable) getGameState().findGameObjectById(
 				"player"));
 
 	}
 
 	public void update() {
 		if (!assetManager.update()) {
 			Gdx.app.debug("asset loading", "loading assets");
 			return;
 		}
 		
 		// execute and wipe
 		applyCommands();
 		gameState.update();
 		physicsSystem.update(gameState.getSimulatableObjects());		
 		renderer.render(gameState.getRenderableObjects());
 		debugRender();
 
 		gameState.cleanUp();
 	}
 
 	private void debugRender() {
 		float ratio = physicsSystem.getRatio();
 		debugMatrix = new Matrix4(camera.combined);
 		debugMatrix.scale(1 / ratio, 1 / ratio, 1f);
 		debugRenderer.render(getPhysicsSystem().getWorld(), debugMatrix);
 	}
 
 	private void applyCommands() {
 		addCommand(inputHandler.readInput());
 		
 		Object object;
 
 		// apply commands to target objects
 		for (Command command : commandList) {
 			if ("gamestate".equals(command.getTargetUID())) {
 				object = gameState;
 			} else {
 				object = gameState.findGameObjectById(command.getTargetUID());
 			}
 			command.execute(object);
 		}
 		
 		commandList.clear();
 	}
 
 	@Override
 	public void addCommand(Command command) {
 		commandList.add(command);
 	}
 
 	@Override
 	public void addCommand(Array<Command> commands) {
 		commandList.addAll(commands);
 	}
 
 	@Override
 	public GameState getGameState() {
 		return gameState;
 	}
 
 	public PhysicsSystem getPhysicsSystem() {
 		return physicsSystem;
 	}
 
 }
