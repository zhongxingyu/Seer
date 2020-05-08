 package ddmp.projecttetra;
 
 import org.andengine.engine.Engine;
 import org.andengine.engine.handler.IUpdateHandler;
 import org.andengine.entity.sprite.Sprite;
 import org.andengine.extension.physics.box2d.PhysicsFactory;
 import org.andengine.extension.physics.box2d.PhysicsWorld;
 import org.andengine.opengl.texture.region.ITextureRegion;
 
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.physics.box2d.Body;
 import com.badlogic.gdx.physics.box2d.FixtureDef;
 import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
 
 /**
  * Spawns planets in the vicinity of the comet.
  */
 public class PlanetSpawner implements IUpdateHandler {
 	
 	private static final double PLANET_SPAWN_CHANCE = 0.01;
 	private static final FixtureDef PLANET_FIXTURE_DEF = PhysicsFactory.createFixtureDef(1, 0.5f, 0.5f);
 	private static final float PLANET_MIN_SIZE = 0.20f; //In percent of camera height
 	private static final float PLANET_MAX_SIZE = 0.35f;	//In percent of camera height
 	
 	private Engine engine;
 	private PhysicsWorld physicsWorld;
 	private PlanetManager planetManager;
 	private Comet comet;
 	private ITextureRegion planetTextureRegion;
 	
 	/* Vector that is reused over and over instead of creating a new
 	 * vector every time a spawn point is calculated. */
 	private Vector2 spawnPoint;
 	
 	public PlanetSpawner(Engine engine, PhysicsWorld physicsWorld, PlanetManager pManager, 
 										Comet comet, ITextureRegion planetTextureRegion) {
 		this.engine = engine;
 		this.physicsWorld = physicsWorld;
 		this.planetManager = pManager;
 		this.comet = comet;
 		this.planetTextureRegion = planetTextureRegion;
 		spawnPoint = new Vector2();
 	}
 	
 	@Override
 	public void onUpdate(float pSecondsElapsed) {
 		if(planetManager.canSpawn() && Math.random() < PLANET_SPAWN_CHANCE) {
 			/* Spawn planet */
			Vector2 spt = getSpawnPoint();
			
 			float scale = PLANET_MIN_SIZE + (PLANET_MAX_SIZE - PLANET_MIN_SIZE) * (float) Math.random();
 			float size = scale * engine.getCamera().getHeight();
 			Sprite planetSprite = new Sprite(spt.x, spt.y, size, size,
 									this.planetTextureRegion, this.engine.getVertexBufferObjectManager());
 			
 			Body planetBody = PhysicsFactory.createCircleBody(physicsWorld, 
 								planetSprite, BodyType.StaticBody, PLANET_FIXTURE_DEF);
 			
 			Planet planet = new Planet(planetSprite, planetBody, comet);
 			
 			this.engine.getScene().attachChild(planetSprite);
 			this.physicsWorld.registerPhysicsConnector(planet);
 			this.planetManager.addPlanet(planet);
 		}
 	}
 
 	@Override
 	public void reset() {
 		// Nothing to do here.
 	}
 	
	private Vector2 getSpawnPoint() {
 		double angle = Math.random() * 2 * Math.PI;
 		double spawnDistance = (engine.getCamera().getWidth() / 2) * (engine.getCamera().getWidth() / 2) +
 								(engine.getCamera().getHeight() / 2) * (engine.getCamera().getHeight() / 2);
		spawnDistance = Math.sqrt(spawnDistance);
 		
		float tmpX = (float) (comet.getShape().getX() + Math.cos(angle) * spawnDistance);
		float tmpY = (float) (comet.getShape().getY() + Math.sin(angle) * spawnDistance);
		spawnPoint.set(tmpX/2, tmpY - engine.getCamera().getHeight());
 		return spawnPoint;
 	}
 
 }
