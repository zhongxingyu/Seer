 package com.gemserk.games.archervsworld.gamestates;
 
 import java.io.InputStream;
 import java.text.MessageFormat;
 import java.util.ArrayList;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 import com.artemis.Entity;
 import com.artemis.World;
 import com.artemis.utils.ImmutableBag;
 import com.badlogic.gdx.Application.ApplicationType;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Input;
 import com.badlogic.gdx.Input.Keys;
 import com.badlogic.gdx.Input.Peripheral;
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.GL10;
 import com.badlogic.gdx.graphics.g2d.BitmapFont;
 import com.badlogic.gdx.graphics.g2d.Sprite;
 import com.badlogic.gdx.math.Circle;
 import com.badlogic.gdx.math.MathUtils;
 import com.badlogic.gdx.math.Rectangle;
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
 import com.gemserk.animation4j.transitions.sync.Synchronizers;
 import com.gemserk.commons.artemis.WorldWrapper;
 import com.gemserk.commons.artemis.components.SpatialImpl;
 import com.gemserk.commons.artemis.components.TimerComponent;
 import com.gemserk.commons.artemis.entities.EntityFactory;
 import com.gemserk.commons.artemis.systems.AliveAreaSystem;
 import com.gemserk.commons.artemis.systems.HierarchySystem;
 import com.gemserk.commons.artemis.systems.HitDetectionSystem;
 import com.gemserk.commons.artemis.systems.MovementSystem;
 import com.gemserk.commons.artemis.systems.PhysicsSystem;
 import com.gemserk.commons.artemis.systems.PointerUpdateSystem;
 import com.gemserk.commons.artemis.systems.RenderLayer;
 import com.gemserk.commons.artemis.systems.SpriteRendererSystem;
 import com.gemserk.commons.artemis.systems.SpriteUpdateSystem;
 import com.gemserk.commons.artemis.systems.TextRendererSystem;
 import com.gemserk.commons.artemis.systems.TimerSystem;
 import com.gemserk.commons.artemis.triggers.AbstractTrigger;
 import com.gemserk.commons.gdx.GameStateImpl;
 import com.gemserk.commons.gdx.box2d.Box2DCustomDebugRenderer;
 import com.gemserk.commons.gdx.camera.Camera;
 import com.gemserk.commons.gdx.camera.CameraRestrictedImpl;
 import com.gemserk.commons.gdx.camera.Libgdx2dCamera;
 import com.gemserk.commons.gdx.camera.Libgdx2dCameraTransformImpl;
 import com.gemserk.commons.gdx.controllers.CameraController;
 import com.gemserk.commons.gdx.graphics.ImmediateModeRendererUtils;
 import com.gemserk.commons.gdx.input.LibgdxPointer;
 import com.gemserk.commons.gdx.resources.LibgdxResourceBuilder;
 import com.gemserk.commons.svg.inkscape.DocumentParser;
 import com.gemserk.commons.svg.inkscape.SvgInkscapeImage;
 import com.gemserk.commons.svg.inkscape.SvgInkscapePath;
 import com.gemserk.componentsengine.input.ButtonMonitor;
 import com.gemserk.componentsengine.input.LibgdxButtonMonitor;
 import com.gemserk.componentsengine.input.LibgdxInputMappingBuilder;
 import com.gemserk.componentsengine.input.MonitorUpdater;
 import com.gemserk.games.archervsworld.LayerProcessor;
 import com.gemserk.games.archervsworld.LibgdxGame;
 import com.gemserk.games.archervsworld.artemis.entities.ArcherVsWorldEntityFactory;
 import com.gemserk.games.archervsworld.artemis.entities.Groups;
 import com.gemserk.games.archervsworld.artemis.systems.CorrectArrowDirectionSystem;
 import com.gemserk.games.archervsworld.artemis.systems.UpdateBowSystem;
 import com.gemserk.games.archervsworld.artemis.systems.UpdateChargingArrowSystem;
 import com.gemserk.games.archervsworld.artemis.systems.WalkingDeadSystem;
 import com.gemserk.games.archervsworld.controllers.BowController;
 import com.gemserk.games.archervsworld.controllers.BowData;
 import com.gemserk.games.archervsworld.controllers.BowDirectionAreaControllerHudImpl;
 import com.gemserk.games.archervsworld.controllers.BowDirectionControllerHudImpl;
 import com.gemserk.games.archervsworld.controllers.BowPowerControllerButonMonitorImpl;
 import com.gemserk.games.archervsworld.controllers.BowPowerHudControllerImpl;
 import com.gemserk.games.archervsworld.controllers.CameraControllerLibgdxPointerImpl;
 import com.gemserk.games.archervsworld.controllers.CameraMovementControllerImpl;
 import com.gemserk.games.archervsworld.controllers.CameraZoomControllerImpl;
 import com.gemserk.games.archervsworld.controllers.MultitouchCameraControllerImpl;
 import com.gemserk.resources.Resource;
 import com.gemserk.resources.ResourceManager;
 import com.gemserk.resources.ResourceManagerImpl;
 
 public class PlayGameState extends GameStateImpl {
 
 	private World world;
 
 	private com.badlogic.gdx.physics.box2d.World physicsWorld;
 
 	private int viewportWidth;
 
 	private int viewportHeight;
 
 	private Box2DCustomDebugRenderer box2dDebugRenderer;
 
 	private ArcherVsWorldEntityFactory archerVsWorldEntityFactory;
 
 	private ResourceManager<String> resourceManager;
 
 	private EntityFactory entityFactory;
 
 	private ButtonMonitor restartButtonMonitor = new LibgdxButtonMonitor(Input.Keys.R);
 
 	private MonitorUpdaterImpl monitorUpdater;
 
 	private Libgdx2dCamera myCamera;
 
 	private WorldWrapper worldWrapper;
 
 	private CameraController moveCameraController;
 
 	private BowData realBowController;
 
 	private BowController bowDirectionController;
 
 	private BowController bowPowerController;
 
 	private Rectangle powerButtonArea;
 
 	private Circle directionButtonArea;
 
 	private CameraController cameraZoomController;
 
 	private GameData gameData = new GameData();
 
 	private final LibgdxGame game;
 
 	static class MonitorUpdaterImpl implements MonitorUpdater {
 
 		ArrayList<ButtonMonitor> buttonMonitors = new ArrayList<ButtonMonitor>();
 
 		@Override
 		public void update() {
 			for (int i = 0; i < buttonMonitors.size(); i++) {
 				ButtonMonitor buttonMonitor = buttonMonitors.get(i);
 				buttonMonitor.update();
 			}
 		}
 
 		public void add(ButtonMonitor buttonMonitor) {
 			buttonMonitors.add(buttonMonitor);
 		}
 
 	}
 
 	public PlayGameState(LibgdxGame game, GameData gameData) {
 		this.game = game;
 		this.gameData = gameData;
 		entityFactory = new EntityFactory();
 		archerVsWorldEntityFactory = new ArcherVsWorldEntityFactory();
 	}
 
 	@Override
 	public void init() {
 		if (gameData.gameOver)
 			restart();
 		Gdx.input.setCatchBackKey(true);
 	}
 
 	@Override
 	public void pause() {
 		Gdx.input.setCatchBackKey(false);
 	}
 
 	protected void restart() {
 
 		System.out.println("restarting game!!");
 
 		gameData.zombiesCount = 10;
 		gameData.zombiesKilled = 0;
 		gameData.zombiesSpawned = 0;
 
 		gameData.gameOver = false;
 
 		resourceManager = new ResourceManagerImpl<String>();
 
 		viewportWidth = Gdx.graphics.getWidth();
 		viewportHeight = Gdx.graphics.getHeight();
 
 		loadResources();
 
 		myCamera = new Libgdx2dCameraTransformImpl();
 		myCamera.center(viewportWidth / 2, viewportHeight / 2);
 
 		ArrayList<RenderLayer> renderLayers = new ArrayList<RenderLayer>();
 
 		// background layer
 		renderLayers.add(new RenderLayer(-1000, -5, new Libgdx2dCameraTransformImpl()));
 		// world layer
 		renderLayers.add(new RenderLayer(-5, 10, myCamera));
 		// hud layer
 		renderLayers.add(new RenderLayer(10, 1000, new Libgdx2dCameraTransformImpl()));
 
 		SpriteRendererSystem spriteRenderSystem = new SpriteRendererSystem(renderLayers);
 
 		Vector2 gravity = new Vector2(0f, -10f);
 		PhysicsSystem physicsSystem = new PhysicsSystem(new com.badlogic.gdx.physics.box2d.World(gravity, true));
 
 		box2dDebugRenderer = new Box2DCustomDebugRenderer((Libgdx2dCameraTransformImpl) myCamera, physicsSystem.getPhysicsWorld());
 
 		LibgdxPointer pointer0 = new LibgdxPointer(0, myCamera);
 		LibgdxPointer pointer1 = new LibgdxPointer(1, myCamera);
 		LibgdxPointer pointer2 = new LibgdxPointer(0);
 		LibgdxPointer pointer3 = new LibgdxPointer(1);
 
 		ArrayList<LibgdxPointer> pointers = new ArrayList<LibgdxPointer>();
 
 		pointers.add(pointer0);
 		pointers.add(pointer1);
 		pointers.add(pointer2);
 		pointers.add(pointer3);
 
 		PointerUpdateSystem pointerUpdateSystem = new PointerUpdateSystem(pointers);
 
 		// Vector2 cameraPosition = new Vector2(viewportWidth * 0.5f * 0.025f, viewportHeight * 0.5f * 0.025f);
 		// Camera camera = new CameraImpl(cameraPosition.x, cameraPosition.y, 40f, 0f);
 		Camera camera = new CameraRestrictedImpl(0, 0, 32f, 0f, viewportWidth, viewportHeight, new Rectangle(2f, 0f, 25f, 15f));
 		camera.setPosition(viewportWidth * 0.5f * 0.025f, viewportHeight * 0.5f * 0.025f);
 
 		Rectangle controllerArea = new Rectangle(140, 0, viewportWidth - 140, viewportHeight);
 
 		// cameraController = new CameraControllerLibgdxPointerImpl(camera, pointer2, controllerArea);
 
 		if (Gdx.input.isPeripheralAvailable(Peripheral.MultitouchScreen)) {
 			moveCameraController = new MultitouchCameraControllerImpl(camera, pointer2, pointer3, controllerArea);
 		} else if (Gdx.app.getType() == ApplicationType.Android) {
 			moveCameraController = new CameraControllerLibgdxPointerImpl(camera, pointer2, controllerArea);
 		} else {
 			moveCameraController = new CameraMovementControllerImpl(camera, //
 					LibgdxInputMappingBuilder.pointerXCoordinateMonitor(Gdx.input, 0), //
 					LibgdxInputMappingBuilder.pointerYCoordinateMonitor(Gdx.input, 0), //
 					LibgdxInputMappingBuilder.rightMouseButtonMonitor(Gdx.input));
 			cameraZoomController = new CameraZoomControllerImpl(camera, LibgdxInputMappingBuilder.keyButtonMonitor(Gdx.input, Keys.DPAD_UP), LibgdxInputMappingBuilder.keyButtonMonitor(Gdx.input, Keys.DPAD_DOWN));
 		}
 
 		// controllers.add(cameraController);
 
 		realBowController = new BowData();
 
 		bowDirectionController = null;
 		bowPowerController = null;
 
 		directionButtonArea = new Circle(70f, 70f, 60f);
 		powerButtonArea = new Rectangle(Gdx.graphics.getWidth() - 100, 20, 80, 80);
 
 		if (Gdx.app.getType() != ApplicationType.Android) {
			// bowDirectionController = new BowDirectionControllerHudImpl(realBowController, pointer2, new Vector2(directionButtonArea.x, directionButtonArea.y));
			bowDirectionController = new BowDirectionControllerHudImpl(realBowController, pointer0, new Vector2(5, 5));
 			bowPowerController = new BowPowerControllerButonMonitorImpl(realBowController, LibgdxInputMappingBuilder.leftMouseButtonMonitor(Gdx.input));
 		} else {
 			bowDirectionController = new BowDirectionAreaControllerHudImpl(realBowController, pointer2, //
 					new Vector2(directionButtonArea.x, directionButtonArea.y), directionButtonArea.radius);
 			bowPowerController = new BowPowerHudControllerImpl(realBowController, //
 					LibgdxInputMappingBuilder.pointerDownButtonMonitor(Gdx.input, 1), //
 					LibgdxInputMappingBuilder.pointerXCoordinateMonitor(Gdx.input, 1), //
 					LibgdxInputMappingBuilder.pointerYCoordinateMonitor(Gdx.input, 1), //
 					powerButtonArea);
 		}
 
 		UpdateBowSystem updateBowSystem = new UpdateBowSystem();
 
 		WalkingDeadSystem walkingDeadSystem = new WalkingDeadSystem();
 
 		world = new World();
 
 		worldWrapper = new WorldWrapper(world);
 
 		worldWrapper.addRenderSystem(new SpriteUpdateSystem());
 		worldWrapper.addRenderSystem(spriteRenderSystem);
 		worldWrapper.addRenderSystem(new TextRendererSystem());
 
 		worldWrapper.addUpdateSystem(physicsSystem);
 		worldWrapper.addUpdateSystem(new HitDetectionSystem());
 		worldWrapper.addUpdateSystem(new CorrectArrowDirectionSystem());
 		worldWrapper.addUpdateSystem(pointerUpdateSystem);
 		worldWrapper.addUpdateSystem(walkingDeadSystem);
 		worldWrapper.addUpdateSystem(new MovementSystem());
 		worldWrapper.addUpdateSystem(updateBowSystem);
 		worldWrapper.addUpdateSystem(new UpdateChargingArrowSystem());
 		worldWrapper.addUpdateSystem(new HierarchySystem());
 		worldWrapper.addUpdateSystem(new AliveAreaSystem());
 		worldWrapper.addUpdateSystem(new TimerSystem());
 		// worldWrapper.addUpdateSystem(new DebugInformationSystem());
 
 		worldWrapper.init();
 
 		entityFactory.setWorld(world);
 
 		Resource<BitmapFont> fontResource = resourceManager.get("Font");
 
 		entityFactory.fpsEntity( //
 				new Vector2(0.5f, 0.5f), //
 				fontResource.get(), //
 				new SpatialImpl(10f, Gdx.graphics.getHeight() - 20, 1f, 1f, 0f));
 
 		physicsWorld = physicsSystem.getPhysicsWorld();
 
 		archerVsWorldEntityFactory.setWorld(world);
 		archerVsWorldEntityFactory.setPhysicsWorld(physicsWorld);
 		archerVsWorldEntityFactory.setResourceManager(resourceManager);
 
 		// I NEED AN EDITOR FOR ALL THIS STUFF!!
 		// I HAVE NOW AN EDITOR FOR ALL THIS STUFF!!!
 
 		archerVsWorldEntityFactory.createBackground(viewportWidth, viewportHeight);
 
 		// archerVsWorldEntityFactory.createWalkingDead(15f, 1.25f + 2f, 0.5f, 2f, -3.4f, 0f, 10f);
 
 		int time = MathUtils.random(4000, 6000);
 		archerVsWorldEntityFactory.createZombiesSpawner(time, new AbstractTrigger() {
 			@Override
 			protected boolean handle(Entity e) {
 				TimerComponent timerComponent = e.getComponent(TimerComponent.class);
 				timerComponent.setCurrentTime(MathUtils.random(3000, 5000));
 
 				archerVsWorldEntityFactory.createWalkingDead(28, 1f + 2f, 0.5f, 2f, -2.4f, 0f, 2f);
 				Gdx.app.log("Archer Vs Zombies", "New Zombie spawned");
 
 				// gameData.zombiesKilled++;
 				gameData.zombiesSpawned++;
 
 				if (gameData.zombiesSpawned == gameData.zombiesCount)
 					world.deleteEntity(e);
 
 				return false;
 			}
 		});
 
 		Vector2 direction = new Vector2(-1, 0);
 
 		Rectangle spawnArea = new Rectangle(10, 8, 25, 7);
 		Rectangle limitArea = new Rectangle(-15, 0, 45, 20);
 
 		float minSpeed = 0.1f;
 		float maxSpeed = 0.7f;
 
 		archerVsWorldEntityFactory.createCloudsSpawner(spawnArea, limitArea, direction, minSpeed, maxSpeed, 5000, 15000);
 
 		monitorUpdater = new MonitorUpdaterImpl();
 		monitorUpdater.add(restartButtonMonitor);
 
 		InputStream svg = Gdx.files.internal("data/scenes/scene01.svg").read();
 		Document document = new DocumentParser().parse(svg);
 
 		new LayerProcessor("World") {
 			protected void handleImageObject(SvgInkscapeImage svgImage, Element element, float x, float y, float width, float height, float sx, float sy, float angle) {
 				// create stuff..
 
 				if (element.hasAttribute("start")) {
 					String item = element.getAttribute("start");
 
 					if ("archer".equalsIgnoreCase(item))
 						archerVsWorldEntityFactory.createArcher(x, y, realBowController);
 					else if ("base".equalsIgnoreCase(item))
 						archerVsWorldEntityFactory.createEnemiesSensor(x, y, width, height, new AbstractTrigger() {
 							@Override
 							protected boolean handle(Entity e) {
 								gameData.gameOver = true;
 								world.deleteEntity(e);
 								return true;
 							}
 						});
 
 					return;
 				}
 
 				Sprite sprite = resourceManager.getResourceValue(svgImage.getLabel());
 				if (sprite == null)
 					return;
 				int layer = 2;
 				if (element.hasAttribute("layer"))
 					layer = Integer.parseInt(element.getAttribute("layer"));
 
 				sprite.setScale(sx, sy);
 
 				archerVsWorldEntityFactory.createStaticSprite(x, y, width, height, angle, sprite, layer, Color.WHITE, 0.5f, 0.5f);
 			};
 		}.processWorld(document);
 
 		new LayerProcessor("Physics") {
 			@Override
 			protected void handlePathObject(SvgInkscapePath svgPath, Element element, Vector2[] vertices) {
 				String label = svgPath.getLabel();
 
 				if ("StaticBody".equals(label))
 					archerVsWorldEntityFactory.createBody(new Vector2(), vertices, BodyType.StaticBody);
 
 				if ("DynamicBody".equals(label))
 					archerVsWorldEntityFactory.createBody(new Vector2(), vertices, BodyType.DynamicBody);
 			}
 		}.processWorld(document);
 
 		world.loopStart();
 	}
 
 	@Override
 	public void render(int delta) {
 		Gdx.graphics.getGL10().glClear(GL10.GL_COLOR_BUFFER_BIT);
 
 		Camera camera = moveCameraController.getCamera();
 		Vector2 cameraPosition = new Vector2(camera.getX(), camera.getY());
 
 		myCamera.zoom(camera.getZoom());
 		myCamera.move(cameraPosition.x, cameraPosition.y);
 		myCamera.rotate(camera.getAngle());
 
 		worldWrapper.render();
 
 		ImmediateModeRendererUtils.drawSolidCircle(directionButtonArea, realBowController.getAngle(), Color.WHITE);
 
 		if (realBowController.isCharging())
 			ImmediateModeRendererUtils.drawSolidCircle(Gdx.graphics.getWidth() - 70f, 70f, realBowController.getPower() * 2f, Color.WHITE);
 
 		if (Gdx.app.getType() == ApplicationType.Android) {
 			ImmediateModeRendererUtils.drawRectangle(powerButtonArea, Color.GREEN);
 		}
 
 		if (Gdx.input.isKeyPressed(Input.Keys.D)) {
 			box2dDebugRenderer.render();
 			Libgdx2dCameraTransformImpl myCamera2 = (Libgdx2dCameraTransformImpl) myCamera;
 			myCamera2.push();
 			ImmediateModeRendererUtils.drawHorizontalAxis(0, Color.RED);
 			ImmediateModeRendererUtils.drawVerticalAxis(0f, Color.RED);
 			myCamera2.pop();
 		}
 	}
 
 	@Override
 	public void update(int delta) {
 		Synchronizers.synchronize(delta);
 		monitorUpdater.update();
 
 		// check controllers in order, to avoid handle both camera and bow controllerS?
 
 		bowDirectionController.update(delta);
 		bowPowerController.update(delta);
 
 		if (!bowDirectionController.wasHandled() && !bowPowerController.wasHandled()) {
 			moveCameraController.update(delta);
 			if (cameraZoomController != null)
 				cameraZoomController.update(delta);
 		}
 
 		worldWrapper.update(delta);
 
 		// check world status to decide to finish the game or not?
 		
 		if (gameData.zombiesSpawned == gameData.zombiesCount) {
 			ImmutableBag<Entity> zombies = world.getGroupManager().getEntities(Groups.Enemy);
 			if (zombies.size() == 0) 
 				gameData.gameOver = true;
 		}
 
 		if (gameData.gameOver) 
 			game.transition(game.scoreScreen, true);
 
 		if (restartButtonMonitor.isReleased() || Gdx.input.isKeyPressed(Keys.BACK)) {
 			// restart();
 			gameData.gameOver = true;
 			game.transition(game.scoreScreen, true);
 		}
 		
 		if (Gdx.input.isKeyPressed(Keys.T)) {
 			Camera camera = moveCameraController.getCamera();
 			System.out.println(MessageFormat.format("x,y=({0}, {1}) zoom={2}", camera.getX(), camera.getY(), camera.getZoom()));
 		}
 	}
 
 	protected void loadResources() {
 
 		new LibgdxResourceBuilder(resourceManager) {
 			{
 				setCacheWhenLoad(true);
 
 				texture("BackgroundTexture", "data/images/background-512x512.jpg", false);
 				sprite("Background", "BackgroundTexture");
 
 				texture("Rock", "data/images/rock-512x512.png");
 				texture("Bow", "data/images/bow-512x512.png");
 				texture("Arrow", "data/images/arrow-512x512.png");
 
 				textureAtlas("MapTiles", "data/images/MapTilesPack");
 
 				spriteAtlas("Tile01", "MapTiles");
 				spriteAtlas("Tile02", "MapTiles");
 				spriteAtlas("Tile03", "MapTiles");
 				spriteAtlas("Tile04", "MapTiles");
 				spriteAtlas("Tile05", "MapTiles");
 				spriteAtlas("Tile06", "MapTiles");
 				spriteAtlas("Tile07", "MapTiles");
 				spriteAtlas("Tile08", "MapTiles");
 				spriteAtlas("Tile09", "MapTiles");
 
 				spriteAtlas("TileWall01", "MapTiles");
 				spriteAtlas("TileWall02", "MapTiles");
 				spriteAtlas("TileWall03", "MapTiles");
 				spriteAtlas("TileWall04", "MapTiles");
 				spriteAtlas("TileWall05", "MapTiles");
 				spriteAtlas("TileWall06", "MapTiles");
 				spriteAtlas("TileWall07", "MapTiles");
 				spriteAtlas("TileWall08", "MapTiles");
 				spriteAtlas("TileWall09", "MapTiles");
 
 				// WallTile01
 
 				texture("BuildingSpritesheet", "data/images/building-spritesheet.png");
 
 				sprite("WindowTile01", "BuildingSpritesheet", 0, 0, 64, 64);
 
 				texture("CloudsSpritesheet", "data/images/clouds-spritesheet.png", false);
 
 				sprite("Cloud01", "CloudsSpritesheet", 0, 0, 512, 128);
 				sprite("Cloud02", "CloudsSpritesheet", 0, 128, 512, 128);
 
 				sound("HitFleshSound", "data/sounds/hit-flesh.ogg");
 				sound("HitGroundSound", "data/sounds/hit-ground.ogg");
 				sound("BowSound", "data/sounds/bow.ogg");
 
 				font("Font", "data/fonts/font.png", "data/fonts/font.fnt");
 			}
 
 		};
 
 	}
 
 	@Override
 	public void dispose() {
 		// dispose resources!!
 		resourceManager.unloadAll();
 	}
 
 }

