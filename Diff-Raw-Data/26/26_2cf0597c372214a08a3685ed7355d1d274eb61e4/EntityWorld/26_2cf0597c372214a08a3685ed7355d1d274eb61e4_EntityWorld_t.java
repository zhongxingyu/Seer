 package com.punchline.javalib.entities;
 
import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Rectangle;
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.physics.box2d.World;
 import com.badlogic.gdx.utils.Disposable;
 import com.punchline.javalib.entities.systems.DebugRenderSystem;
 import com.punchline.javalib.entities.systems.RenderSystem;
 
 public abstract class EntityWorld implements Disposable {
 
 	private final float TIME_STEP = 1.0f / 60.0f;
 	private final int VELOCITY_ITERATIONS = 6;
 	private final int POSITION_ITERATIONS = 2;
 	
 	/**
 	 * This world's {@link EntityManager}.
 	 */
 	protected EntityManager entities;
 	
 	/**
 	 * This world's {@link SystemManager}.
 	 */
 	protected SystemManager systems;
 	
 	/**
 	 * This world's {@link RenderSystem}.
 	 */
 	protected RenderSystem renderSystem;
 	
 	/**
 	 * This world's {@link DebugRenderSystem}.
 	 */
 	protected DebugRenderSystem debugView;
 	
 	/**
 	 * This world's Box2D {@link com.badlogic.gdx.physics.box2d.World World}
 	 */
 	protected World physicsWorld;
 	
 	/**
 	 * This world's {@link com.badlogic.gdx.graphics.Camera Camera}.
 	 */
 	protected Camera camera;
 	
 	/**
 	 * Template map.
 	 */
 	private Map<String, EntityTemplate> templates;
 	
 	/**
 	 * Group template map.
 	 */
 	private Map<String, EntityGroupTemplate> groupTemplates;
 	
 	/**
 	 * Instantiates the EntityWorld's {@link EntityManager}, {@link SystemManager}, and template map.
 	 * @param camera The camera that will be used for rendering this world.
 	 * @param gravity The gravity vector2.
 	 * @param doSleeping Whether the world allows sleeping.
 	 */
 	public EntityWorld(Camera camera, Vector2 gravity, boolean doSleeping) {
 		entities = new EntityManager();
 		
 		systems = new SystemManager();
 		
 		templates = new HashMap<String, EntityTemplate>();
		groupTemplates = new HashMap<String, EntityGroupTemplate>();
 		
 		this.camera = camera;
 		positionCamera();
 		
 		physicsWorld = new World(gravity, doSleeping);
 		
 		buildSystems();
 		buildTemplates();
 		buildEntities();
 	}
 	
 	/**
 	 * Disposes of all EntitySystems, and the physics world.
 	 */
 	@Override
 	public void dispose() {
 		
 		systems.dispose();
 		physicsWorld.dispose();
 		
 	}
 	
 	/**
 	 * Runs all system processing.
 	 */
 	public void process() {
 		
 		systems.process(
 				entities.getNewEntities(), 
 				entities.getChangedEntities(), 
 				entities.getRemovedEntities(), Gdx.graphics.getDeltaTime());
 		
 		entities.process();
 		
 		physicsWorld.step(TIME_STEP, VELOCITY_ITERATIONS, POSITION_ITERATIONS);
 		
 	}
 	
 	/**
	 * @return This world's boundaries.
	 */
	public abstract Rectangle getBounds();
	
	/**
 	 * @return This world's Box2D {@link com.badlogic.gdx.physics.box2d.World World}
 	 */
 	public World getPhysicsWorld() {
 		return physicsWorld;
 	}
 	
 	/**
 	 * @return This world's {@link com.badlogic.gdx.graphics.Camera Camera}.
 	 */
 	public Camera getCamera() {
 		return camera;
 	}
 	
 	/**
 	 * Creates an {@link Entity} using the {@link EntityTemplate} associated with the given tag.
 	 * @param template The tag of the template.
 	 * @param args Arguments for creating the {@link Entity}.
 	 * @return The created entity.
 	 */
 	public Entity createEntity(String template, Object... args) {
 		Entity e = templates.get(template).buildEntity(this, args);
 		entities.add(e);
 		return e;
 	}
 	
 	/**
 	 * Creates a group of Entities using the {@link EntityGroupTemplate} associated with the given tag.
 	 * @param template The tag of the template to use.
 	 * @param args Arguments for creating the entity group.
 	 * @return The group of entities.
 	 */
	public ArrayList<Entity> createEntityGroup(String template, Object... args) {
		ArrayList<Entity> group = groupTemplates.get(template).buildEntities(this, args);
 		
 		for (Entity e : group) {
 			entities.add(e); //Add the group to the world.
 		}
 		
 		return group;
 	}
 	
 	/**
 	 * Adds an EntityTemplate to the template map.
 	 * @param templateKey The template's key.
 	 * @param template The template.
 	 */
 	public void addTemplate(String templateKey, EntityTemplate template) {
 		templates.put(templateKey, template);
 	}
 	
 	/**
 	 * Adds an EntityGroupTemplate to the group template map.
 	 * @param templateKey The template's key.
 	 * @param template The template.
 	 */
 	public void addGroupTemplate(String templateKey, EntityGroupTemplate template) {
 		groupTemplates.put(templateKey, template);
 	}
 	
 	/**
 	 * Positions the camera.
 	 */
 	protected void positionCamera() { }
 	
 	/**
 	 * Adds necessary systems to the world. Called by the constructor.
 	 */
 	protected void buildSystems() {
 		
 		renderSystem = (RenderSystem)systems.addSystem(new RenderSystem(camera));
 		
 		debugView = (DebugRenderSystem)systems.addSystem(new DebugRenderSystem(getPhysicsWorld(), camera, systems));
 		
 	}
 	
 	/**
 	 * Adds necessary templates to the world. Called by the constructor.
 	 */
 	protected void buildTemplates() { }
 	
 	/**
 	 * Adds necessary entities to the world. Called by the constructor.
 	 */
 	protected void buildEntities() { }
 	
 }
