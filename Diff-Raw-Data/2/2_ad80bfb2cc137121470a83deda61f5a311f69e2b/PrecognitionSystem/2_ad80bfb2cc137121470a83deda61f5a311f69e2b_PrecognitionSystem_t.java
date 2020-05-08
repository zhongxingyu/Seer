 package se.exuvo.planets.systems;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
 import java.util.concurrent.ThreadFactory;
 
 import se.exuvo.planets.components.Acceleration;
 import se.exuvo.planets.components.Mass;
 import se.exuvo.planets.components.Position;
 import se.exuvo.planets.components.Velocity;
 import se.exuvo.planets.systems.InputSystem.PlanetSelectionChanged;
 import se.exuvo.planets.utils.VectorD2;
 import se.exuvo.settings.Settings;
 
 import com.artemis.Aspect;
 import com.artemis.ComponentMapper;
 import com.artemis.Entity;
 import com.artemis.EntitySystem;
 import com.artemis.World;
 import com.artemis.annotations.Mapper;
 import com.artemis.utils.Bag;
 import com.artemis.utils.ImmutableBag;
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
 import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
 import com.badlogic.gdx.math.Vector2;
 
 public class PrecognitionSystem extends EntitySystem implements PlanetSelectionChanged {
 
 	@Mapper ComponentMapper<Position> pm;
 	@Mapper ComponentMapper<Acceleration> am;
 	@Mapper ComponentMapper<Mass> mm;
 	@Mapper ComponentMapper<Velocity> vm;
 
 	ComponentMapper<Position> fpm;
 	ComponentMapper<Acceleration> fam;
 	ComponentMapper<Mass> fmm;
 	ComponentMapper<Velocity> fvm;
 
 	private Bag<Entity> inserted = new Bag<Entity>(), removed = new Bag<Entity>();
 	private Map<Entity, Entity> toFuture = new HashMap<Entity, Entity>();
 
 	private OrthographicCamera camera;
 	private ShapeRenderer render;
 
 	// must be multiple of 2 for drawing to work correctly
 	private static int forwardComputationSteps = Settings.getInt("PrecognitionSteps");
 	private VectorD2[][] futureSteps = new VectorD2[Settings.getInt("PrecognitionMaxVisualPlanets")][forwardComputationSteps];
 	private Bag<Entity> selectedPlanets, selectedFutures;
 
 	private World futureWorld;
 	private ExecutorService executor = Executors.newSingleThreadExecutor(new ThreadFactory() {
 		@Override
 		public Thread newThread(Runnable arg0) {
 			Thread t = new Thread(arg0);
 			t.setPriority(Thread.MIN_PRIORITY);
 			return t;
 		}
 	});
 	private Future<?> task;
 
 	public PrecognitionSystem(OrthographicCamera camera) {
 		super(Aspect.getAspectForAll(Mass.class, Acceleration.class, Position.class, Velocity.class));
 		this.camera = camera;
 	}
 
 	@Override
 	protected void initialize() {
 		render = new ShapeRenderer();
 
 		futureWorld = new World();
 
 		futureWorld.setSystem(new InputSystem(null), true);
 		futureWorld.setSystem(new GravitationSystem());
 		futureWorld.setSystem(new VelocitySystem());
 		futureWorld.setSystem(new PositionSystem());
 //		futureWorld.setSystem(new CollisionSystem());
 
 		futureWorld.initialize();
 		futureWorld.setDelta(Settings.getFloat("PhysicsStep"));
 
 		world.getSystem(InputSystem.class).addListener(this);
 
 		selectedPlanets = new Bag<Entity>();
 		selectedFutures = new Bag<Entity>();
 
 		for (int i = 0; i < futureSteps.length; i++) {
 			for (int j = 0; j < futureSteps[i].length; j++) {
 				futureSteps[i][j] = new VectorD2();
 			}
 		}
 
 		pm = world.getMapper(Position.class);
 		am = world.getMapper(Acceleration.class);
 		mm = world.getMapper(Mass.class);
 		vm = world.getMapper(Velocity.class);
 
 		fpm = futureWorld.getMapper(Position.class);
 		fam = futureWorld.getMapper(Acceleration.class);
 		fmm = futureWorld.getMapper(Mass.class);
 		fvm = futureWorld.getMapper(Velocity.class);
 	}
 
 	@Override
 	protected void begin() {
 		render.setProjectionMatrix(camera.combined);
 		render.begin(ShapeType.Line);
 	}
 
 	@Override
 	protected void processEntities(ImmutableBag<Entity> entities) {
 		if (!selectedPlanets.isEmpty()) {
 			render.setColor(Color.WHITE);
 			for (int i = 0; i < futureSteps.length && i < selectedFutures.size(); i++) {
 				for (int j = 0; j < futureSteps[i].length; j += 2) {
 					// TODO avoid possible concurrent read write
 					Vector2 p1 = futureSteps[i][j].toVector2();
 					Vector2 p2 = futureSteps[i][j + 1].toVector2();
 
 					render.line(p1.x, p1.y, p2.x, p2.y); // draw between dots
 				}
 			}
 
 			if (task == null || task.isDone()) {
 				refreshFuture(entities);
 			}
 		} else {
 			if (task != null) {
 				stopTask();
 				clearWorld();
 				task = null;
 			}
 		}
 	}
 
 	@Override
 	protected void end() {
 		render.end();
 	}
 
 	private void clearWorld() {
 		// Remove old entities
 		for (int i = 0; i < removed.size(); i++) {
 			Entity e = removed.get(i);
 			futureWorld.deleteEntity(toFuture.get(e));
 			toFuture.remove(e);
 		}
 		removed.clear();
 	}
 
 	private void copyWorld(ImmutableBag<Entity> entities) {
		inserted.removeAll(removed);//If something added has already been removed
		
 		// Add new entities
 		for (int i = 0; i < inserted.size(); i++) {
 			Entity e = inserted.get(i);
 			if(!e.isActive()){
 				throw new RuntimeException("wut?");
 			}
 			if(!e.isEnabled()){
 				throw new RuntimeException("wut2?");
 			}
 
 			Position p = pm.get(e);
 			Mass m = mm.get(e);
 			Velocity v = vm.get(e);
 			Acceleration a = am.get(e);
 
 			// Copy entity
 			Entity eCopy = futureWorld.createEntity();
 			eCopy.addComponent(p.clone());
 			eCopy.addComponent(m.clone());
 			eCopy.addComponent(v.clone());
 			eCopy.addComponent(a.clone());
 
 			eCopy.addToWorld();
 
 			toFuture.put(e, eCopy);
 		}
 		inserted.clear();
 
 		// Copy component values
 		for (Map.Entry<Entity, Entity> key : toFuture.entrySet()) {
 			Entity e = key.getKey();
 
 			Position p = pm.get(e);
 			Mass m = mm.get(e);
 			Velocity v = vm.get(e);
 			Acceleration a = am.get(e);
 
 			Entity eCopy = key.getValue();
 			fpm.get(eCopy).vec.set(p.vec);
 			fmm.get(eCopy).mass = m.mass;
 			fvm.get(eCopy).vec.set(v.vec);
 			fam.get(eCopy).vec.set(a.vec);
 		}
 
 		selectedFutures.clear();
 		for (Entity e : selectedPlanets) {
 			selectedFutures.add(toFuture.get(e));
 		}
 	}
 
 	private void stopTask() {
 		if (task != null && !task.isDone()) {
 			task.cancel(true);
 			// TODO wait until actually done
 			try {
 				Thread.sleep(100);
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	private void startTask() {
 		task = executor.submit(new Runnable() {
 			@Override
 			public void run() {
 				try {
 //					long start = System.currentTimeMillis();
 					for (int i = 0; i < forwardComputationSteps; i++) {
 						if (Thread.interrupted()) {
 							System.out.println("Precognition Interrupted");
 							break;
 						}
 						futureWorld.process();
 
 						for (int j = 0; j < selectedFutures.size() && j < futureSteps.length; j++) {
 							futureSteps[j][i].set(fpm.get(selectedFutures.get(j)).vec);
 						}
 					}
 //					start = System.currentTimeMillis() - start;
 //					System.out.println(start);
 				} catch (Throwable t) {
 					t.printStackTrace();
 				} finally {}
 			}
 		});
 	}
 
 	private void refreshFuture(ImmutableBag<Entity> entities) {
 		if (task != null) {
 			stopTask();
 			clearWorld();
 			task = null;
 		}
 
 		copyWorld(entities);
 		startTask();
 	}
 
 	@Override
 	public void planetSelectionChanged(Bag<Entity> planets) {
 		selectedPlanets = planets;
 		stopTask();
 	}
 
 	@Override
 	protected boolean checkProcessing() {
 		return true;
 	}
 
 	@Override
 	protected void inserted(Entity e) {
 		inserted.add(e);
 	};
 
 	@Override
 	protected void removed(Entity e) {
 		removed.add(e);
 	};
 
 }
