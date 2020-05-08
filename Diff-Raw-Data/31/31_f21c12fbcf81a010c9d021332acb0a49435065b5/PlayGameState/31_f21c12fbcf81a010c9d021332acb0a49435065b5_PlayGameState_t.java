 package com.gemserk.games.newtod.gamestates;
 
 import com.artemis.Entity;
 import com.artemis.utils.ImmutableBag;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Input.Keys;
 import com.badlogic.gdx.InputAdapter;
 import com.badlogic.gdx.InputMultiplexer;
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.GL10;
 import com.badlogic.gdx.graphics.g2d.BitmapFont;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.physics.box2d.World;
 import com.badlogic.gdx.scenes.scene2d.Stage;
 import com.badlogic.gdx.scenes.scene2d.ui.Button;
 import com.badlogic.gdx.scenes.scene2d.ui.Button.ButtonStyle;
 import com.badlogic.gdx.scenes.scene2d.ui.Label;
 import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
 import com.badlogic.gdx.utils.Array;
 import com.gemserk.animation4j.transitions.sync.Synchronizers;
 import com.gemserk.commons.artemis.EntityBuilder;
 import com.gemserk.commons.artemis.WorldWrapper;
 import com.gemserk.commons.artemis.render.RenderLayers;
 import com.gemserk.commons.artemis.systems.ContainerSystem;
 import com.gemserk.commons.artemis.systems.OwnerSystem;
 import com.gemserk.commons.artemis.systems.PhysicsSystem;
 import com.gemserk.commons.artemis.systems.RenderLayerSpriteBatchImpl;
 import com.gemserk.commons.artemis.systems.RenderableSystem;
 import com.gemserk.commons.artemis.systems.ScriptSystem;
 import com.gemserk.commons.artemis.systems.SpriteUpdateSystem;
 import com.gemserk.commons.artemis.systems.TagSystem;
 import com.gemserk.commons.artemis.templates.EntityFactoryImpl;
 import com.gemserk.commons.gdx.GameStateImpl;
 import com.gemserk.commons.gdx.GlobalTime;
 import com.gemserk.commons.gdx.box2d.BodyBuilder;
 import com.gemserk.commons.gdx.box2d.Box2DCustomDebugRenderer;
 import com.gemserk.commons.gdx.box2d.JointBuilder;
 import com.gemserk.commons.gdx.camera.Libgdx2dCamera;
 import com.gemserk.commons.gdx.camera.Libgdx2dCameraTransformImpl;
 import com.gemserk.commons.gdx.graphics.ImmediateModeRendererUtils;
 import com.gemserk.commons.gdx.graphics.Mesh2dBuilder;
 import com.gemserk.commons.reflection.Injector;
 import com.gemserk.commons.reflection.InjectorImpl;
 import com.gemserk.componentsengine.input.InputDevicesMonitorImpl;
 import com.gemserk.componentsengine.input.LibgdxInputMappingBuilder;
 import com.gemserk.componentsengine.utils.ParametersWrapper;
 import com.gemserk.games.newtod.Game;
 import com.gemserk.games.newtod.Layers;
 import com.gemserk.games.newtod.Tags;
 import com.gemserk.games.newtod.path.Path;
 import com.gemserk.games.newtod.path.Segment;
 import com.gemserk.games.newtod.systems.components.CreepDataComponent;
 import com.gemserk.games.newtod.templates.EntityTemplates;
 import com.gemserk.games.newtod.templates.EntityTemplatesHelper;
 import com.gemserk.resources.ResourceManager;
 
 public class PlayGameState extends GameStateImpl {
 
 	private final Game game;
 	private ResourceManager<String> resourceManager;
 	private SpriteBatch spriteBatch;
 	private World physicsWorld;
 	private JointBuilder jointBuilder;
 	private Libgdx2dCameraTransformImpl worldCamera;
 	private Libgdx2dCameraTransformImpl guiCamera;
 	private RenderLayers renderLayers;
 	private com.artemis.World world;
 	private EntityFactoryImpl entityFactory;
 	private WorldWrapper worldWrapper;
 	private EntityBuilder entityBuilder;
 	private Box2DCustomDebugRenderer box2dCustomDebugRenderer;
 	private com.gemserk.games.newtod.templates.EntityTemplatesHelper entityTemplatesHelper;
 	private InputDevicesMonitorImpl<String> inputDevicesMonitor;
 	private ShapeRenderer shapeRenderer;
 
 	public PlayGameState(Game game) {
 		this.game = game;
 	}
 
 	public void setResourceManager(ResourceManager<String> resourceManager) {
 		this.resourceManager = resourceManager;
 	}
 
 	@Override
 	public void init() {
 		super.init();
 
 		int width = Gdx.graphics.getWidth();
 		int height = Gdx.graphics.getHeight();
 
 		float centerX = width / 2;
 		float centerY = height / 2;
 		spriteBatch = new SpriteBatch();
 
 		physicsWorld = new World(new Vector2(0, 0), false);
 
 		jointBuilder = new JointBuilder(physicsWorld);
 
 		worldCamera = new Libgdx2dCameraTransformImpl();
 		worldCamera.center(centerX, centerY);
 		float zoom = 100;
 		worldCamera.zoom(zoom);
 		worldCamera.move(centerX / zoom, centerY / zoom);
 
 		guiCamera = new Libgdx2dCameraTransformImpl();
 
 		Libgdx2dCamera backgroundLayerCamera = new Libgdx2dCameraTransformImpl();
 
 		renderLayers = new RenderLayers();
 
 		renderLayers.add(Layers.Background, new RenderLayerSpriteBatchImpl(-10000, -500, backgroundLayerCamera, spriteBatch));
 		renderLayers.add(Layers.World, new RenderLayerSpriteBatchImpl(-500, 100, worldCamera));
 
 		world = new com.artemis.World();
 		entityFactory = new EntityFactoryImpl(world);
 		worldWrapper = new WorldWrapper(world);
 
 		worldWrapper.addUpdateSystem(new PhysicsSystem(physicsWorld));
 		worldWrapper.addUpdateSystem(new ScriptSystem());
 		worldWrapper.addUpdateSystem(new TagSystem());
 		worldWrapper.addUpdateSystem(new ContainerSystem());
 		worldWrapper.addUpdateSystem(new OwnerSystem());
 
 		worldWrapper.addRenderSystem(new SpriteUpdateSystem());
 		worldWrapper.addRenderSystem(new RenderableSystem(renderLayers));
 
 		worldWrapper.init();
 
 		entityBuilder = new EntityBuilder(world);
 
 		box2dCustomDebugRenderer = new Box2DCustomDebugRenderer((Libgdx2dCameraTransformImpl) worldCamera, physicsWorld);
 		entityTemplatesHelper = new EntityTemplatesHelper(physicsWorld, world, resourceManager, entityBuilder, entityFactory);
 
 		Injector injector = new InjectorImpl() {
 			{
 				bind("physicsWorld", physicsWorld);
 				bind("world", world);
 				bind("resourceManager", resourceManager);
 				bind("entityBuilder", new EntityBuilder(world));
 				bind("entityFactory", new EntityFactoryImpl(world));
 				bind("bodyBuilder", new BodyBuilder(physicsWorld));
 				bind("jointBuilder", new JointBuilder(physicsWorld));
 			}
 		};
 		
 		
 		
 		entityTemplates = new EntityTemplates(injector);
 		
 		inputDevicesMonitor = new InputDevicesMonitorImpl<String>();
 		new LibgdxInputMappingBuilder<String>(inputDevicesMonitor, Gdx.input) {
 			{
 				monitorKeys("pause", Keys.BACK, Keys.ESCAPE);
 				monitorKey("dump", Keys.D);
 			}
 		};
 
 		shapeRenderer = new ShapeRenderer();
 		immediateModeRendererUtils = new ImmediateModeRendererUtils();
 
 		loadLevel();
 	}
 
 	Path path;
 	private ImmediateModeRendererUtils immediateModeRendererUtils;
 
 	private void loadLevel() {
 
 		path = new Path(//
 				new Segment(new Vector2(-1.00f, 0.10f), new Vector2(3.00f, 0.10f)),//
 				new Segment(new Vector2(3.00f, 0.10f), new Vector2(3.00f, 3.00f))//
 		);
 
 		
 		for (int i = 0; i < 10; i++) {
			entityFactory.instantiate(entityTemplates.creepTemplate, new ParametersWrapper().put("path", path).put("speed", 0.50f).put("startDistanceInPath", 0.10f*i).put("hitpoints", 10));			
 		}
 		
 		entityFactory.instantiate(entityTemplates.towerTemplate, new ParametersWrapper().put("position", new Vector2(1.00f,1.00f)));
 
 		stage = new Stage(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), false);
 
 		Label label = new Label("TEST", new LabelStyle((BitmapFont) resourceManager.getResourceValue("FpsFont"), Color.RED));
 
 		Button button = new Button(label, new ButtonStyle()) {
 			@Override
 			public void touchDragged(float x, float y, int pointer) {
 				this.x += x;
 				this.y += y;
 				System.out.println(String.format("STAGE: x: %f - y:%f - pointer: %d", x, y, pointer));
 			}
 		};
 		button.setText("Button");
 		button.x = 100;
 		button.y = 100;
 
 		button.width = 100;
 		button.height = 100;
 		stage.addActor(button);
 
 		System.out.println(button.width);
 
 		InputMultiplexer inputMultiplexer = new InputMultiplexer();
 
 		inputMultiplexer.addProcessor(stage);
 		inputMultiplexer.addProcessor(new InputAdapter() {
 			@Override
 			public boolean touchDragged(int x, int y, int pointer) {
 				System.out.println(String.format("LOGGER: x: %d - y:%d - pointer: %d", x, y, pointer));
 				return false;
 			}
 		});
 
 		Gdx.input.setInputProcessor(inputMultiplexer);
 
 	}
 
 	@Override
 	public void render() {
 		Gdx.graphics.getGL10().glClear(GL10.GL_COLOR_BUFFER_BIT);
 
 		worldWrapper.render();
 
 		Array<Segment> segments = path.getSegments();
 		Segment[] segmentsArray = segments.items;
 		ImmediateModeRendererUtils.getProjectionMatrix().set(worldCamera.getCombinedMatrix());
 
 		for (int i = 0; i < segments.size; i++) {
 			Segment segment = segmentsArray[i];
 			ImmediateModeRendererUtils.drawLine(segment.getStart(), segment.getEnd(), Color.RED);
 		}
 
 		stage.draw();
 
 //		if (Game.isShowBox2dDebug())
 			box2dCustomDebugRenderer.render();
 	}
 
 	Vector2 pathEnd = new Vector2();
 	private Stage stage;
 	private EntityTemplates entityTemplates;
 
 	@Override
 	public void update() {
 
 		stage.act(GlobalTime.getDelta());
 
 		inputDevicesMonitor.update();
 		Synchronizers.synchronize(getDelta());
 		worldWrapper.update(getDeltaInMs());
 		if(inputDevicesMonitor.getButton("dump").isReleased()){
 			ImmutableBag<Entity> creeps = world.getGroupManager().getEntities(Tags.CREEPS);
 			for (int i = 0; i < creeps.size(); i++) {
 				Entity creep = creeps.get(i);
 				CreepDataComponent creepDataComponent = creep.getComponent(CreepDataComponent.class);
 				System.out.println(creep.getId() + " - " + creepDataComponent.hitpoints);
 			}
 		}
 	}
 
 	@Override
 	public void dispose() {
 		worldWrapper.dispose();
 		spriteBatch.dispose();
 	}
 }
