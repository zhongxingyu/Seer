 package com.gemserk.games.taken;
 
 import java.util.ArrayList;
 
 import javax.vecmath.Vector2f;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 import com.artemis.Entity;
 import com.artemis.World;
 import com.badlogic.gdx.Application.ApplicationType;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Input.Keys;
 import com.badlogic.gdx.Input.Peripheral;
 import com.badlogic.gdx.audio.Sound;
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.GL10;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.BitmapFont;
 import com.badlogic.gdx.graphics.g2d.Sprite;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.math.MathUtils;
 import com.badlogic.gdx.math.Rectangle;
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.physics.box2d.Body;
 import com.gemserk.animation4j.gdx.Animation;
 import com.gemserk.animation4j.interpolator.function.InterpolationFunctions;
 import com.gemserk.animation4j.transitions.Transitions;
 import com.gemserk.animation4j.transitions.sync.Synchronizers;
 import com.gemserk.commons.artemis.WorldWrapper;
 import com.gemserk.commons.artemis.components.MovementComponent;
 import com.gemserk.commons.artemis.components.SpatialComponent;
 import com.gemserk.commons.artemis.components.SpriteComponent;
 import com.gemserk.commons.artemis.entities.EntityTemplate;
 import com.gemserk.commons.artemis.systems.MovementSystem;
 import com.gemserk.commons.artemis.systems.RenderLayer;
 import com.gemserk.commons.artemis.systems.SpriteRendererSystem;
 import com.gemserk.commons.artemis.systems.SpriteUpdateSystem;
 import com.gemserk.commons.gdx.ScreenAdapter;
 import com.gemserk.commons.gdx.box2d.Box2DCustomDebugRenderer;
 import com.gemserk.commons.gdx.box2d.Box2dUtils;
 import com.gemserk.commons.gdx.camera.Camera;
 import com.gemserk.commons.gdx.camera.Libgdx2dCamera;
 import com.gemserk.commons.gdx.camera.Libgdx2dCameraTransformImpl;
 import com.gemserk.commons.gdx.controllers.Controller;
 import com.gemserk.commons.gdx.graphics.ImmediateModeRendererUtils;
 import com.gemserk.commons.gdx.input.LibgdxPointer;
 import com.gemserk.commons.gdx.resources.LibgdxResourceBuilder;
 import com.gemserk.commons.svg.inkscape.SvgDocument;
 import com.gemserk.commons.svg.inkscape.SvgDocumentHandler;
 import com.gemserk.commons.svg.inkscape.SvgInkscapeGroup;
 import com.gemserk.commons.svg.inkscape.SvgInkscapeGroupHandler;
 import com.gemserk.commons.svg.inkscape.SvgInkscapeImage;
 import com.gemserk.commons.svg.inkscape.SvgInkscapePath;
 import com.gemserk.commons.svg.inkscape.SvgInkscapePathHandler;
 import com.gemserk.commons.svg.inkscape.SvgParser;
 import com.gemserk.componentsengine.input.InputDevicesMonitorImpl;
 import com.gemserk.componentsengine.input.LibgdxButtonMonitor;
 import com.gemserk.componentsengine.input.LibgdxInputMappingBuilder;
 import com.gemserk.componentsengine.properties.PropertyBuilder;
 import com.gemserk.componentsengine.utils.Container;
 import com.gemserk.games.taken.PowerUp.Type;
 import com.gemserk.games.taken.controllers.AreaTouchJumpController;
 import com.gemserk.games.taken.controllers.AreaTouchMovementController;
 import com.gemserk.games.taken.controllers.ButtonMonitorJumpController;
 import com.gemserk.games.taken.controllers.ButtonMonitorMovementController;
 import com.gemserk.games.taken.controllers.DragJumpController;
 import com.gemserk.games.taken.controllers.JumpController;
 import com.gemserk.games.taken.controllers.MovementController;
 import com.gemserk.games.taken.controllers.TouchMovementController;
 import com.gemserk.resources.Resource;
 import com.gemserk.resources.ResourceManager;
 import com.gemserk.resources.ResourceManagerImpl;
 
 public class GameScreen extends ScreenAdapter {
 
 	private LibgdxGame game;
 
 	private WorldWrapper worldWrapper;
 
 	private InputDevicesMonitorImpl<String> inputDevicesMonitor;
 
 	private Libgdx2dCameraTransformImpl camera = new Libgdx2dCameraTransformImpl();
 
 	private Libgdx2dCamera backgroundLayerCamera = new Libgdx2dCameraTransformImpl();
 
 	private Libgdx2dCamera hudLayerCamera = new Libgdx2dCameraTransformImpl();
 
 	private Box2DCustomDebugRenderer box2dCustomDebugRenderer;
 
 	private com.badlogic.gdx.physics.box2d.World physicsWorld;
 
 	private SvgDocument svgDocument;
 
 	private PhysicsObjectsFactory physicsObjectsFactory;
 
 	private int viewportWidth;
 
 	private int viewportHeight;
 
 	private Camera cameraData;
 
 	private ResourceManager<String> resourceManager;
 
 	private World world;
 
 	private ArrayList<Controller> controllers;
 
 	private Entity mainCharacter;
 
 	private boolean gameOver = true;
 
 	private float score;
 
 	private SpriteBatch spriteBatch;
 
 	private final Color laserEndColor = new Color(1f, 1f, 1f, 0f);
 
 	private final Color laserStartColor = new Color(1f, 1f, 1f, 1f);
 	
 	public void setGameOver(boolean gameOver) {
 		this.gameOver = gameOver;
 	}
 
 	public GameScreen(LibgdxGame game) {
 		this.game = game;
 
 		viewportWidth = Gdx.graphics.getWidth();
 		viewportHeight = Gdx.graphics.getHeight();
 
 		camera.center(viewportWidth / 2, viewportHeight / 2);
 		camera.zoom(40f);
 
 		float zoom = 40f;
 		float invZoom = 1 / zoom;
 
 		Vector2 cameraPosition = new Vector2(viewportWidth * 0.5f * invZoom, viewportHeight * 0.5f * invZoom);
 		cameraData = new Camera(cameraPosition.x, cameraPosition.y, zoom, 0f);
 
 		inputDevicesMonitor = new InputDevicesMonitorImpl<String>();
 
 		resourceManager = new ResourceManagerImpl<String>();
 
 		new LibgdxInputMappingBuilder<String>(inputDevicesMonitor, Gdx.input) {
 			{
 				monitorKey("debug", Keys.D);
 				monitorKey("score", Keys.P);
 
 				if (Gdx.app.getType() == ApplicationType.Android)
 					monitorKey("menu", Keys.MENU);
 				else
 					monitorKey("menu", Keys.ESCAPE);
 
 				if (Gdx.app.getType() == ApplicationType.Android)
 					monitorKey("back", Keys.BACK);
 
 				// BACK and MENU keys should show a PAUSE
 
 			}
 		};
 
 	}
 
 	void restartGame() {
 
		Gdx.input.setCatchBackKey(true);

 		Gdx.app.log("Taken", "Reloading the level");
 
 		controllers = new ArrayList<Controller>();
 
 		spriteBatch = new SpriteBatch();
 
 		// create the scene...
 		physicsWorld = new com.badlogic.gdx.physics.box2d.World(new Vector2(0, -10f), false);
 
 		physicsObjectsFactory = new PhysicsObjectsFactory(physicsWorld);
 
 		box2dCustomDebugRenderer = new Box2DCustomDebugRenderer(camera, physicsWorld);
 
 		world = new World();
 
 		worldWrapper = new WorldWrapper(world);
 
 		ArrayList<RenderLayer> renderLayers = new ArrayList<RenderLayer>();
 
 		// background layer
 		renderLayers.add(new RenderLayer(-1000, -100, backgroundLayerCamera));
 		// world layer
 		renderLayers.add(new RenderLayer(-100, 100, camera));
 		// hud layer
 		renderLayers.add(new RenderLayer(100, 1000, hudLayerCamera));
 
 		worldWrapper.addUpdateSystem(new CharacterControllerSystem());
 		worldWrapper.addUpdateSystem(new JumpSystem());
 
 		worldWrapper.addUpdateSystem(new PhysicsSystem(physicsWorld));
 		worldWrapper.addUpdateSystem(new FollowCharacterBehaviorSystem());
 		worldWrapper.addUpdateSystem(new WeaponSystem(this, resourceManager));
 		worldWrapper.addUpdateSystem(new MovementSystem());
 		worldWrapper.addUpdateSystem(new BulletSystem());
 		worldWrapper.addUpdateSystem(new HealthVialSystem(resourceManager));
 
 		worldWrapper.addUpdateSystem(new GrabSystem(resourceManager));
 		worldWrapper.addUpdateSystem(new PowerUpSystem());
 
 		worldWrapper.addUpdateSystem(new AnimationSystem());
 		worldWrapper.addUpdateSystem(new CorrectSpriteDirectionSystem());
 
 		worldWrapper.addUpdateSystem(new BloodOverlaySystem());
 		worldWrapper.addUpdateSystem(new HitDetectionSystem(resourceManager));
 		worldWrapper.addUpdateSystem(new CameraFollowSystem());
 		worldWrapper.addUpdateSystem(new SpriteUpdateSystem());
 		worldWrapper.addRenderSystem(new SpriteRendererSystem(renderLayers));
 		worldWrapper.addUpdateSystem(new TimerSystem());
 
 		worldWrapper.addUpdateSystem(new SpawnerSystem());
 
 		worldWrapper.init();
 
 		// load scene!!
 
 		loadResources();
 
 		createBackground();
 
 		loadWorld();
 
 		loadPhysicObjects();
 
 		createCharacterBloodOverlay();
 
 		createEnemyRobotSpawner();
 
 		createHealthVialSpawner();
 
 		createPowerUpSpawner();
 
 		score = 0;
 
 		// Music backgroundMusic = resourceManager.getResourceValue("BackgroundMusic");
 		// backgroundMusic.play();
 		// backgroundMusic.setLooping(true);
 
 	}
 
 	void loadWorld() {
 		Document document = resourceManager.getResourceValue("Scene01");
 		new WorldLoader("World") {
 
 			protected void handleCharacterStartPoint(float x, float y) {
 				createMainCharacter(x, y);
 			};
 
 			protected void handleRobotStartPoint(float x, float y) {
 				createRobo(x, y);
 			}
 
 			protected void handleStaticObject(SvgInkscapeImage svgImage, float width, float height, float angle, float sx, float sy, float x, float y) {
 				Resource<Texture> tileResource = resourceManager.get(svgImage.getLabel());
 				if (tileResource == null)
 					return;
 				Texture texture = tileResource.get();
 				Sprite sprite = new Sprite(texture);
 				sprite.setScale(sx, sy);
 				createStaticSprite(sprite, x, y, width, height, angle, 0, 0.5f, 0.5f, Color.WHITE);
 			}
 
 		}.processWorld(document);
 
 		// new WorldLoader("Water") {
 		//
 		// protected void handleStaticObject(SvgInkscapeImage svgImage, float width, float height, float angle, float sx, float sy, float x, float y) {
 		// Resource<Texture> tileResource = resourceManager.get(svgImage.getLabel());
 		// if (tileResource == null)
 		// return;
 		// Texture texture = tileResource.get();
 		// Sprite sprite = new Sprite(texture);
 		// sprite.setScale(sx, sy);
 		// Entity entity = createStaticSprite(sprite, x, y, width, height, angle, 10, 0.5f, 0.5f, Color.WHITE);
 		// entity.addComponent(new MovementComponent(new Vector2(0.3f,0f), 0f));
 		// entity.refresh();
 		// }
 		//
 		// }.loadWorld(Gdx.files.internal("data/scenes/scene01.svg").read());
 
 	}
 
 	void loadPhysicObjects() {
 		Document document = resourceManager.getResourceValue("Scene01");
 		SvgParser svgParser = new SvgParser();
 		svgParser.addHandler(new SvgDocumentHandler() {
 			@Override
 			protected void handle(SvgParser svgParser, SvgDocument svgDocument, Element element) {
 				GameScreen.this.svgDocument = svgDocument;
 			}
 		});
 		svgParser.addHandler(new SvgInkscapeGroupHandler() {
 			@Override
 			protected void handle(SvgParser svgParser, SvgInkscapeGroup svgInkscapeGroup, Element element) {
 				if (svgInkscapeGroup.getGroupMode().equals("layer") && !svgInkscapeGroup.getLabel().equalsIgnoreCase("Physics")) {
 					svgParser.processChildren(false);
 					return;
 				}
 			}
 		});
 		svgParser.addHandler(new SvgInkscapePathHandler() {
 			@Override
 			protected void handle(SvgParser svgParser, SvgInkscapePath svgPath, Element element) {
 
 				// String collisionMaskValue = element.getAttribute("collisionMask");
 
 				Vector2f[] points = svgPath.getPoints();
 				Vector2[] vertices = new Vector2[points.length];
 
 				for (int i = 0; i < points.length; i++) {
 					Vector2f point = points[i];
 					vertices[i] = new Vector2(point.x, svgDocument.getHeight() - point.y);
 					// System.out.println(vertices[i]);
 				}
 
 				physicsObjectsFactory.createGround(new Vector2(), vertices);
 
 			}
 		});
 		svgParser.parse(document);
 	}
 
 	void createBackground() {
 		Resource<Texture> background = resourceManager.get("Background");
 		createStaticSprite(new Sprite(background.get()), 0f, 0f, 512, 512, 0f, -103, 0f, 0f, Color.WHITE);
 		createStaticSprite(new Sprite(background.get()), 512, 0f, 512, 512, 0f, -103, 0f, 0f, Color.WHITE);
 	}
 
 	Entity createStaticSprite(Sprite sprite, float x, float y, float width, float height, float angle, int layer, float centerx, float centery, Color color) {
 		Entity entity = world.createEntity();
 		entity.addComponent(new SpatialComponent(new Vector2(x, y), new Vector2(width, height), angle));
 		entity.addComponent(new SpriteComponent(sprite, layer, new Vector2(centerx, centery), new Color(color)));
 		entity.refresh();
 		return entity;
 	}
 
 	void createMainCharacter(float x, float y) {
 		Resource<Animation> walkingAnimationResource = resourceManager.get("Human_Walking");
 		Resource<Animation> idleAnimationResource = resourceManager.get("Human_Idle");
 		Resource<Animation> jumpAnimationResource = resourceManager.get("Human_Jump");
 		Resource<Animation> fallAnimationResource = resourceManager.get("Human_Fall");
 
 		Resource<Sound> jumpSound = resourceManager.get("JumpSound");
 
 		Sprite sprite = new Sprite(idleAnimationResource.get().getFrame(0));
 
 		float size = 1f;
 
 		float width = 0.15f;
 		float height = 1f;
 
 		Vector2[] bodyShape = Box2dUtils.createRectangle(width, height);
 		Body body = physicsObjectsFactory.createPolygonBody(x, y, bodyShape, true, 0.1f, 1f, 0f, 0.15f);
 
 		mainCharacter = world.createEntity();
 
 		mainCharacter.setTag("MainCharacter");
 
 		body.setUserData(mainCharacter);
 
 		PhysicsComponent physicsComponent = new PhysicsComponent(body);
 		physicsComponent.setVertices(bodyShape);
 
 		mainCharacter.setGroup("Player");
 
 		mainCharacter.addComponent(physicsComponent);
 		mainCharacter.addComponent(new SpatialComponent( //
 				new Box2dPositionProperty(body), //
 				PropertyBuilder.vector2(size, size), //
 				new Box2dAngleProperty(body)));
 		// PropertyBuilder.property(ValueBuilder.floatValue(0f))));
 		// entity.addComponent(new SpatialComponent(new Vector2(0, 0), new Vector2(viewportWidth, viewportWidth), 0f));
 		mainCharacter.addComponent(new SpriteComponent(sprite, 1, new Vector2(0.5f, 0.5f), new Color(Color.WHITE)));
 
 		Animation[] spriteSheets = new Animation[] { walkingAnimationResource.get(), idleAnimationResource.get(), jumpAnimationResource.get(), fallAnimationResource.get(), };
 
 		mainCharacter.addComponent(new AnimationComponent(spriteSheets));
 		mainCharacter.addComponent(new CameraFollowComponent(cameraData));
 		mainCharacter.addComponent(new HealthComponent(new Container(50f, 50f)));
 
 		MovementController movementController = null;
 		JumpController jumpController = new ButtonMonitorJumpController(new LibgdxButtonMonitor(Keys.DPAD_UP));
 
 		if (Gdx.app.getType() == ApplicationType.Desktop) {
 			movementController = new ButtonMonitorMovementController(new LibgdxButtonMonitor(Keys.DPAD_LEFT), new LibgdxButtonMonitor(Keys.DPAD_RIGHT));
 			jumpController = new ButtonMonitorJumpController(new LibgdxButtonMonitor(Keys.DPAD_UP));
 			// characterController = new AreaTouchCharacterController(new Rectangle(0, 0, 100, 100), new Rectangle(100, 0, 100, 100));
 			// jumpController = new AreaTouchJumpController(new Rectangle(Gdx.graphics.getWidth() - 100, 0, 100, 100));
 		} else if (Gdx.input.isPeripheralAvailable(Peripheral.MultitouchScreen)) {
 			movementController = new AreaTouchMovementController(new Rectangle(0, 0, 100, 100), new Rectangle(100, 0, 100, 100));
 			jumpController = new AreaTouchJumpController(new Rectangle(Gdx.graphics.getWidth() - 100, 0, 100, 100));
 			// characterController = new TouchCharacterController(new LibgdxPointer(0));
 			// jumpController = new DragJumpController(new LibgdxPointer(0));
 			// add a tap controller for jump, based on a screen area..
 
 		} else {
 			movementController = new TouchMovementController(new LibgdxPointer(0));
 			jumpController = new DragJumpController(new LibgdxPointer(0));
 		}
 
 		mainCharacter.addComponent(new CharacterControllerComponent(movementController, jumpController));
 
 		controllers.add(movementController);
 		controllers.add(jumpController);
 
 		mainCharacter.addComponent(new JumpComponent(12f, jumpSound.get()));
 
 		mainCharacter.refresh();
 	}
 
 	void createCharacterBloodOverlay() {
 		Resource<Animation> frontBloodAnimationResource = resourceManager.get("FrontBloodOverlay");
 		Resource<Animation> sideBloodAnimationResource = resourceManager.get("SideBloodOverlay");
 
 		Sprite sprite = new Sprite(frontBloodAnimationResource.get().getFrame(0));
 
 		float size = 1f;
 
 		Entity e = world.createEntity();
 
 		e.addComponent(new SpatialComponent(new Vector2(0, 0), new Vector2(size, size), 0));
 		e.addComponent(new SpriteComponent(sprite, 2, new Vector2(0.5f, 0.5f), new Color(Color.WHITE)));
 
 		Animation[] spriteSheets = new Animation[] { frontBloodAnimationResource.get(), sideBloodAnimationResource.get() };
 
 		e.addComponent(new BloodOverlayComponent(mainCharacter, spriteSheets));
 
 		e.refresh();
 	}
 
 	void createEnemyRobotSpawner() {
 		Entity entity = world.createEntity();
 
 		entity.addComponent(new SpawnerComponent(4500, new EntityTemplate() {
 			@Override
 			public Entity build() {
 				// TODO Auto-generated function stub
 
 				// limit robot spawner!!
 
 				SpatialComponent spatialComponent = mainCharacter.getComponent(SpatialComponent.class);
 				Vector2 position = spatialComponent.getPosition();
 
 				float x = position.x + MathUtils.random(-5, 5);
 				float y = position.y + MathUtils.random(-5, 5);
 
 				createEnemy(x, y);
 
 				return null;
 			}
 		}));
 
 		entity.refresh();
 	}
 
 	void createHealthVialSpawner() {
 		Entity entity = world.createEntity();
 
 		entity.addComponent(new SpawnerComponent(10000, new EntityTemplate() {
 			@Override
 			public Entity build() {
 				// TODO Auto-generated function stub
 
 				SpatialComponent spatialComponent = mainCharacter.getComponent(SpatialComponent.class);
 				Vector2 position = spatialComponent.getPosition();
 
 				float x = position.x + MathUtils.random(-10, 10);
 				float y = 0f + MathUtils.random(0f, 2.5f);
 
 				createHealthVial(x, y, 15000, 25f);
 
 				Gdx.app.log("Taken", "Health vial spawned at (" + x + ", " + y + ")");
 
 				return null;
 			}
 		}));
 
 		entity.refresh();
 	}
 
 	void createPowerUpSpawner() {
 		Entity entity = world.createEntity();
 
 		entity.addComponent(new SpawnerComponent(15000, new EntityTemplate() {
 			@Override
 			public Entity build() {
 				// TODO Auto-generated function stub
 
 				SpatialComponent spatialComponent = mainCharacter.getComponent(SpatialComponent.class);
 				Vector2 position = spatialComponent.getPosition();
 
 				float x = position.x + MathUtils.random(-10, 10);
 				float y = 0f + MathUtils.random(1f, 3f);
 
 				if (MathUtils.randomBoolean()) {
 					createPowerUp(x, y, 25000, new PowerUp(Type.MovementSpeedModifier, 2f, 25000));
 					Gdx.app.log("Taken", "Movement Speed Power Up spawned at (" + x + ", " + y + ")");
 				} else {
 					createPowerUp(x, y, 25000, new PowerUp(Type.WeaponSpeedModifier, 3f, 25000));
 					Gdx.app.log("Taken", "Weapon Speed Power Up spawned at (" + x + ", " + y + ")");
 				}
 
 				return null;
 			}
 		}));
 
 		entity.refresh();
 	}
 
 	void createRobo(float x, float y) {
 		Resource<Animation> enemyAnimationResource = resourceManager.get("Robo");
 		Sprite sprite = enemyAnimationResource.get().getFrame(0);
 
 		float size = 1f;
 
 		Entity entity = world.createEntity();
 
 		entity.setTag("Robo");
 
 		entity.addComponent(new SpatialComponent(new Vector2(x, y), new Vector2(size, size), 0f));
 		entity.addComponent(new MovementComponent(new Vector2(), 0f));
 		entity.addComponent(new SpriteComponent(sprite, 2, new Vector2(0.5f, 0.5f), new Color(Color.WHITE)));
 		entity.addComponent(new FollowCharacterComponent(new Vector2(x, y), 0f));
 
 		entity.addComponent(new WeaponComponent(500, 6f, 2.5f, "Player", "Enemy", 10f));
 
 		Animation[] spriteSheets = new Animation[] { enemyAnimationResource.get(), };
 
 		entity.addComponent(new AnimationComponent(spriteSheets));
 		entity.addComponent(new PowerUpComponent());
 
 		entity.refresh();
 	}
 
 	void createEnemy(float x, float y) {
 		Resource<Animation> enemyAnimationResource = resourceManager.get("Enemy");
 		Sprite sprite = enemyAnimationResource.get().getFrame(0);
 
 		float size = 1f;
 
 		Entity entity = world.createEntity();
 
 		entity.setGroup("Enemy");
 
 		entity.addComponent(new SpatialComponent(new Vector2(x, y), new Vector2(size, size), 0f));
 		entity.addComponent(new MovementComponent(new Vector2(), 0f));
 		entity.addComponent(new SpriteComponent(sprite, 2, new Vector2(0.5f, 0.5f), new Color(Color.WHITE)));
 		entity.addComponent(new FollowCharacterComponent(new Vector2(x, y), 0f));
 		entity.addComponent(new WeaponComponent(900, 5.5f, 7f, "Enemy", "Player", 5f));
 
 		entity.addComponent(new HealthComponent(new Container(20f, 20f)));
 
 		Animation[] spriteSheets = new Animation[] { enemyAnimationResource.get(), };
 
 		entity.addComponent(new AnimationComponent(spriteSheets));
 
 		entity.refresh();
 	}
 
 	void createLaser(float x, float y, int time, float dx, float dy, float damage, String ownerGroup, String targetGroup) {
 		Resource<Animation> laserAnimationResource;
 
 		if (ownerGroup.equals("Player"))
 			laserAnimationResource = resourceManager.get("FriendlyLaser");
 		else
 			laserAnimationResource = resourceManager.get("EnemyLaser");
 
 		Sprite sprite = laserAnimationResource.get().getFrame(0);
 
 		float size = 1f;
 
 		Entity entity = world.createEntity();
 
 		entity.setGroup(ownerGroup);
 
 		Color color = new Color();
 
 		Synchronizers.transition(color, Transitions.transitionBuilder(laserStartColor).end(laserEndColor).time(time)//
 				.functions(InterpolationFunctions.easeOut(), InterpolationFunctions.easeOut(), InterpolationFunctions.easeOut(), InterpolationFunctions.easeOut()) //
 				.build());
 
 		entity.addComponent(new SpatialComponent(new Vector2(x, y), new Vector2(size, size), 0f));
 		entity.addComponent(new MovementComponent(new Vector2(dx, dy), 0f));
 		entity.addComponent(new SpriteComponent(sprite, 2, new Vector2(0.5f, 0.5f), color));
 		entity.addComponent(new TimerComponent(time));
 
 		entity.addComponent(new BulletComponent());
 
 		entity.addComponent(new HitComponent(targetGroup, damage));
 
 		entity.addComponent(new HealthComponent(new Container(2f, 2f)));
 
 		Animation[] spriteSheets = new Animation[] { laserAnimationResource.get(), };
 
 		entity.addComponent(new AnimationComponent(spriteSheets));
 
 		entity.refresh();
 	}
 
 	void createHealthVial(float x, float y, int aliveTime, float health) {
 		Resource<Animation> healthVialAnimationResource = resourceManager.get("HealthVial");
 
 		Sprite sprite = healthVialAnimationResource.get().getFrame(0);
 
 		float size = 1f;
 
 		Entity entity = world.createEntity();
 
 		Color color = new Color();
 
 		int spawnTime = 1000;
 
 		Synchronizers.transition(color, Transitions.transitionBuilder(laserEndColor).end(laserStartColor).time(spawnTime)//
 				.functions(InterpolationFunctions.easeOut(), InterpolationFunctions.easeOut(), InterpolationFunctions.easeOut(), InterpolationFunctions.easeOut()) //
 				.build());
 
 		entity.addComponent(new SpatialComponent(new Vector2(x, y), new Vector2(size, size), 0f));
 		entity.addComponent(new SpriteComponent(sprite, -1, new Vector2(0.5f, 0.5f), color));
 		entity.addComponent(new TimerComponent(aliveTime));
 
 		entity.addComponent(new HealthComponent(new Container(health, health)));
 
 		Animation[] spriteSheets = new Animation[] { healthVialAnimationResource.get(), };
 
 		entity.addComponent(new AnimationComponent(spriteSheets));
 		entity.addComponent(new HealthVialComponent());
 
 		entity.refresh();
 	}
 
 	void createPowerUp(float x, float y, int aliveTime, PowerUp powerUp) {
 
 		Resource<Animation> animation = resourceManager.get("Powerup01");
 
 		if (powerUp.getType() == Type.MovementSpeedModifier)
 			animation = resourceManager.get("Powerup02");
 
 		Sprite sprite = animation.get().getFrame(0);
 
 		float size = 1f;
 
 		Entity entity = world.createEntity();
 
 		Color color = new Color();
 
 		int spawnTime = 1000;
 
 		Synchronizers.transition(color, Transitions.transitionBuilder(laserEndColor).end(laserStartColor).time(spawnTime)//
 				.functions(InterpolationFunctions.easeOut(), InterpolationFunctions.easeOut(), InterpolationFunctions.easeOut(), InterpolationFunctions.easeOut()) //
 				.build());
 
 		entity.addComponent(new SpatialComponent(new Vector2(x, y), new Vector2(size, size), 0f));
 		entity.addComponent(new SpriteComponent(sprite, -1, new Vector2(0.5f, 0.5f), color));
 		entity.addComponent(new TimerComponent(aliveTime));
 
 		Animation[] spriteSheets = new Animation[] { animation.get(), };
 
 		entity.addComponent(new AnimationComponent(spriteSheets));
 
 		entity.addComponent(new GrabComponent());
 		entity.addComponent(new PowerUpComponent(powerUp));
 
 		entity.refresh();
 	}
 
 	@Override
 	public void internalRender(float delta) {
 
 		if (spriteBatch == null)
 			return;
 		
 		Gdx.graphics.getGL10().glClear(GL10.GL_COLOR_BUFFER_BIT);
 
 		camera.zoom(cameraData.getZoom() * 2f);
 		camera.move(cameraData.getX(), cameraData.getY());
 		camera.rotate(cameraData.getAngle());
 
 		worldWrapper.render();
 
 		Resource<BitmapFont> font = resourceManager.get("Font");
 		BitmapFont bitmapFont = font.get();
 
 		spriteBatch.begin();
 		String scoreLabel = "score: " + (int) score;
 		spriteBatch.setColor(Color.WHITE);
 		bitmapFont.setScale(1f);
 		bitmapFont.draw(spriteBatch, scoreLabel, 10, Gdx.graphics.getHeight() - 10);
 		spriteBatch.end();
 
 		if (inputDevicesMonitor.getButton("debug").isHolded())
 			box2dCustomDebugRenderer.render();
 
 		if (Gdx.input.isPeripheralAvailable(Peripheral.MultitouchScreen)) {
 			ImmediateModeRendererUtils.drawRectangle(0, 0, 100, 100, Color.WHITE);
 			ImmediateModeRendererUtils.drawRectangle(100, 0, 200, 100, Color.WHITE);
 			ImmediateModeRendererUtils.drawRectangle(Gdx.graphics.getWidth() - 100, 0, Gdx.graphics.getWidth(), 100, Color.WHITE);
 		}
 	}
 
 	@Override
 	public void internalUpdate(float delta) {
 		int deltaInMs = (int) (delta * 1000f);
 
 		worldWrapper.update(deltaInMs);
 
 		Synchronizers.synchronize();
 		inputDevicesMonitor.update();
 
 		// if (inputDevicesMonitor.getButton("score").isPressed()) {
 		// game.setScreen(game.scoreScreen, true);
 		// gameOver = true;
 		// return;
 		// }
 
 		for (int i = 0; i < controllers.size(); i++) {
 			Controller controller = controllers.get(i);
 			controller.update(deltaInMs);
 		}
 
 		if (inputDevicesMonitor.getButton("menu").isReleased() || inputDevicesMonitor.getButton("back").isReleased()) {
 			game.setScreen(game.pauseScreen, false);
 			return;
 		}
 
 		score += 100 * delta;
 
 		HealthComponent healthComponent = mainCharacter.getComponent(HealthComponent.class);
 		SpatialComponent spatialComponent = mainCharacter.getComponent(SpatialComponent.class);
 
 		if (healthComponent.getHealth().isEmpty() || (spatialComponent.getPosition().y < -5)) {
 			// set score based on something...!!
 			game.scoreScreen.setScore((int) score);
 			game.setScreen(game.scoreScreen, true);
 			gameOver = true;
 			return;
 		}
 	}
 
 	@Override
 	public void resize(int width, int height) {
 
 	}
 
 	@Override
 	public void show() {

 		// if game over then ->
 		if (gameOver) {
 			restartGame();
 			gameOver = false;
 		}
 
 	}
 
 	protected void loadResources() {
 
 		new LibgdxResourceBuilder(resourceManager) {
 			{
 				setCacheWhenLoad(true);
 
 				texture("Background", "data/images/background-512x512.jpg");
 
 				// add method for sprites (with or without cache)
 
 				texture("Tile01", "data/images/tile01.png");
 				texture("Tile02", "data/images/tile02.png");
 				texture("Tile03", "data/images/tile03.png");
 				texture("Tile04", "data/images/tile04.png");
 				texture("Tile05", "data/images/tile05.png");
 				texture("Tile06", "data/images/tile06.png");
 				texture("Tile07", "data/images/tile07.png");
 				texture("Tile08", "data/images/tile08.png");
 				texture("Tile09", "data/images/tile09.png");
 
 				texture("Tile10", "data/images/tile10.png");
 				texture("Tile11", "data/images/tile11.png");
 				texture("Tile12", "data/images/tile12.png");
 				texture("Tile13", "data/images/tile13.png");
 				texture("Tile14", "data/images/tile14.png");
 
 				texture("CharactersSpriteSheet", "data/images/spritesheet.png", false);
 
 				animation("Human_Walking", "CharactersSpriteSheet", 0, 32, 32, 32, 4, true, 100, 250, 100, 250);
 				animation("Human_Idle", "CharactersSpriteSheet", 0, 0, 32, 32, 2, true, 1000, 50);
 				animation("Human_Jump", "CharactersSpriteSheet", 0, 64, 32, 32, 1, false, 100);
 				animation("Human_Fall", "CharactersSpriteSheet", 0, 96, 32, 32, 2, true, 400, 200);
 				animation("Robo", "CharactersSpriteSheet", 4 * 32, 4 * 32, 32, 32, 1, false, 0);
 				animation("EnemyLaser", "CharactersSpriteSheet", 64, 0, 32, 32, 3, false, 150);
 				animation("FriendlyLaser", "CharactersSpriteSheet", 64, 64, 32, 32, 3, false, 150);
 				animation("FrontBloodOverlay", "CharactersSpriteSheet", 0, 4 * 32, 32, 32, 3, false, 0);
 				animation("SideBloodOverlay", "CharactersSpriteSheet", 0, 5 * 32, 32, 32, 3, false, 0);
 				animation("HealthVial", "CharactersSpriteSheet", 4 * 32, 1 * 32, 32, 32, 4, true, 750);
 				animation("Powerup01", "CharactersSpriteSheet", 5 * 32, 2 * 32, 32, 32, 2, true, 750);
 				animation("Powerup02", "CharactersSpriteSheet", 5 * 32, 3 * 32, 32, 32, 2, true, 750);
 
 				// animation("Enemy", "CharactersSpriteSheet", 0 * 32, 6 * 32, 32, 32, 3, true, 800, 150, 150);
 				resource("Enemy", animation2("CharactersSpriteSheet") //
 						.frame(0, 6 * 32, 32, 32, 800) //
 						.frame(1 * 32, 6 * 32, 32, 32, 150) //
 						.frame(2 * 32, 6 * 32, 32, 32, 150) //
 						.frame(1 * 32, 6 * 32, 32, 32, 150) //
 						.loop(true));
 
 				sound("JumpSound", "data/sounds/jump.ogg");
 				sound("FriendlyLaserSound", "data/sounds/laser.ogg");
 				sound("EnemyLaserSound", "data/sounds/enemy_laser.ogg");
 				sound("Explosion", "data/sounds/explosion.ogg");
 
 				sound("HealthVialSound", "data/sounds/healthvial.ogg");
 
 				font("Font", "data/fonts/font.png", "data/fonts/font.fnt");
 
 				resource("Scene01", xmlDocument("data/scenes/scene01.svg")//
 						.cached());
 
 			}
 
 		};
 
 	}
 
 	@Override
 	public void dispose() {
 		resourceManager.unloadAll();
 		spriteBatch.dispose();
 		physicsWorld.dispose();
 		spriteBatch = null;
 	}
 
 }
