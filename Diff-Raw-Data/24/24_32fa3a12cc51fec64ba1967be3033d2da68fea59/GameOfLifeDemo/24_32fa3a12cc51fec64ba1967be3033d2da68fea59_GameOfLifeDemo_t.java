 package gameOfLife;
 
 import graphics.Color;
 import java.util.Random;
 
 import math.Point2;
 import math.Vector2;
 import misc.IEventListener;
 
 import GUI.ButtonEventArgs;
 import ability.AbilityBuilder;
 import ability.AbilitySystem;
 
 import com.google.inject.Guice;
 import com.google.inject.Injector;
 
 import components.BehaviourComp;
 import components.RenderingComp;
 import components.TextRenderingComp;
 import components.TransformationComp;
 
 import content.ContentManager;
 import debugsystems.DebugAttackRenderSystem;
 import debugsystems.DebugSpatialRenderSystem;
 import demos.Demo;
 import demos.WorldBuilder;
 
 import entityFramework.*;
 import entitySystems.AnimationSystem;
 import entitySystems.CameraControlSystem;
 import entitySystems.ExistanceSystem;
 import entitySystems.HUDRenderingSystem;
 import entitySystems.InputSystem;
 import entitySystems.PhysicsSystem;
 import entitySystems.RegenSystem;
 import entitySystems.RenderingSystem;
 import entitySystems.ScriptCollisionSystem;
 import entitySystems.ScriptMouseSystem;
 import entitySystems.ScriptSystem;
 import entitySystems.TextRenderingSystem;
 import gameMap.Scene;
 import graphics.SpriteBatch;
 
 import scripting.ButtonBehavior;
 import tests.EntityModule;
 import utils.Camera2D;
 import utils.GameTime;
 import utils.Rectangle;
 
 public class GameOfLifeDemo extends Demo {
 
	private static Rectangle screenDim = new Rectangle(0,0,1366,768);
	private static Rectangle worldBounds = new Rectangle(0, 0, 1366 * 5, 768 * 5);
 	private static final int blockSize = 64;
 	private static final String groupName = "GameOL";
 	private static String buttonArcheype = "StandardButton.archetype";
 	private static String cellArchetype = "GameOfLifeCell.archetype";
 	
 	private IEntityWorld world;
 	private SpriteBatch spriteBatch;
 	private Camera2D camera;
 	private GameOfLifeLogicSystem golSystem;
 	
 	public static void main(String[] args) {
 		new GameOfLifeDemo().start();
 	}
 	
 	public GameOfLifeDemo() {
 		super(screenDim,60);
 	}
 	
 	@Override
 	protected void initialize() {
 		this.camera = new Camera2D(worldBounds, screenDim);
 		this.camera.setZoom(new Vector2(0.2f, 0.2f));
 		this.camera.setPosition(Vector2.Zero);
 		this.spriteBatch = new SpriteBatch(screenDim);
 		
 		
 		
 		world = buildGameOfLifeWorld(camera, spriteBatch, true);
 		world.initialize();
 		
 		
 	
 		//Primitive GUI!
 		
 		IEntity button = world.getEntityManager().createEntity(ContentManager.loadArchetype(buttonArcheype));
 		TransformationComp comp = button.getComponent(TransformationComp.class);
 		comp.setPosition(this.worldBounds.Width - 250,250);
 		comp.setScale(5f,5f);
 				
 		button.getComponent(RenderingComp.class).setColor(Color.Pink);
 		button.getComponent(TextRenderingComp.class).setText("Seed!");
 		
 		final Random random = new Random();
 		ButtonBehavior behaviour = (ButtonBehavior)button.getComponent(BehaviourComp.class).getBehavior();
 		behaviour.addEventListener(new IEventListener<ButtonEventArgs>() {
 			@Override
 			public void onEvent(Object sender, ButtonEventArgs e) {
 			  golSystem.seedNewLife(random.nextLong());
 				
 			}
 		});
 	}
 	
 	
 	public  IEntityWorld buildGameOfLifeWorld(Camera2D camera, SpriteBatch spriteBatch, boolean debugging) {
 		IEntityWorld world = Guice.createInjector(new EntityModule()).getInstance(IEntityWorld.class);
 		IEntitySystemManager manager = world.getSystemManager();
 		
 		this.golSystem = new GameOfLifeLogicSystem(world,
 												   blockSize, 
 												   new Point2((worldBounds.Width - 500)/ blockSize, worldBounds.Height / blockSize ), 
 												   cellArchetype, 
 												   groupName);
 		
 		manager.addLogicEntitySystem(new ScriptSystem(world));
 		manager.addLogicEntitySystem(new ScriptMouseSystem(world, camera));
 		
 		
 		//Logic systems!
 		manager.addLogicEntitySystem(golSystem);
 		
 		//Rendering systems!
 		manager.addRenderEntitySystem(new RenderingSystem(world, spriteBatch));
 		manager.addRenderEntitySystem(new TextRenderingSystem(world, spriteBatch));
 		
 		
 		return world;
 	}
 	
 	@Override
 	public void update(GameTime time) {
 		this.world.update(time);
 		this.world.getEntityManager().destoryKilledEntities();
 	}
 
 	@Override
 	public void render(GameTime time) {
 		this.spriteBatch.clearScreen(Color.Black);
 		this.spriteBatch.begin(null, camera.getTransformation());
 		this.world.render();		
 		this.spriteBatch.end();
 	}
 }
