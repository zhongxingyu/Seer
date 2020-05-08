 package gamedev.objects;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 
 import gamedev.game.Direction;
 import gamedev.game.ResourcesManager;
 import org.andengine.engine.camera.BoundCamera;
 import org.andengine.engine.handler.physics.PhysicsHandler;
 import org.andengine.entity.sprite.AnimatedSprite;
 import org.andengine.extension.physics.box2d.PhysicsConnector;
 import org.andengine.extension.physics.box2d.PhysicsFactory;
 import org.andengine.extension.physics.box2d.PhysicsWorld;
 
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.physics.box2d.Body;
 import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
 
 public class Player extends AnimatedSprite {
 	
 	public final static long[] ANIMATION_DURATION = { 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50};
 	public final static int FRAMES_PER_ANIMATION = 11;
 	public final static int TILES_PER_LINE = 22;
 	
 	public Body body;
 	public PhysicsHandler physicsHandler;
 	
 	protected ResourcesManager resourcesManager;
 	protected PlayerState currentState = PlayerState.IDLE;
 	protected int direction = Direction.NORTH;
 	
 	protected float velocity = 4f;	
 	protected float factorRunning = 1.5f;
 	protected int life = 100;
 	protected int energy = 100;
 	
 	protected ArrayList<Dinosaur> attackers = new ArrayList<Dinosaur>();
 	
 	public enum PlayerState {
 		IDLE,
 		WALKING,
 		RUNNING,
 		BEEN_HIT,
 		ATTACK,
 		TIPPING_OVER,
 	}
 	
 	/**
 	 * Create the player
 	 * @param pX x-Position in the World
 	 * @param pY y-Position in the World
 	 */
 	public Player(float pX, float pY) {
 		super(pX, pY, ResourcesManager.getInstance().playerRegion, ResourcesManager.getInstance().vbom);
 		this.resourcesManager = ResourcesManager.getInstance();
 		this.resourcesManager.camera.setChaseEntity(this);
 		this.createAndConnectPhysics(this.resourcesManager.camera, this.resourcesManager.physicsWorld);
 		this.mScaleX = this.mScaleX * 2;
 		this.mScaleY = this.mScaleY * 2;
 	}
 	
 	public Player() {
 		super(0, 0, ResourcesManager.getInstance().playerRegion, ResourcesManager.getInstance().vbom);
 		this.resourcesManager = ResourcesManager.getInstance();
 		this.resourcesManager.camera.setChaseEntity(this);
 		this.createAndConnectPhysics(this.resourcesManager.camera, this.resourcesManager.physicsWorld);
 		this.mScaleX = this.mScaleX * 2;
 		this.mScaleY = this.mScaleY * 2;
 	}
 
 	/**
 	 * Display Player animation on current State and Direction
 	 * Also validate some states here, e.g. running is not possible if energy = 0
 	 * @param state new PlayerState
 	 */
 	public void setState(PlayerState state, int direction) {
		if (state != PlayerState.RUNNING && state != PlayerState.WALKING && state != PlayerState.IDLE) {
 			if (this.currentState == state && this.direction == direction) return;			
 		}
 		this.currentState = state;
 		this.direction = direction;
 		if (state == PlayerState.IDLE) {
 			this.body.setLinearVelocity(0, 0);
 			this.stopAnimation();
 			this.setEnergy(this.energy+2);
 			return;
 		}
 		if (!this.isAnimationRunning()) {
 			int rowIndex = 0;
 			if (state == PlayerState.ATTACK) rowIndex = 0;
 			if (state == PlayerState.BEEN_HIT) rowIndex = 4;
 			if (state == PlayerState.RUNNING) rowIndex = 8;
 			if (state == PlayerState.TIPPING_OVER) rowIndex = 12;
 			if (state == PlayerState.WALKING) rowIndex = 16;
 			boolean animate = (state == PlayerState.RUNNING || state == PlayerState.WALKING) ? false : true;
 			int startTile = rowIndex*TILES_PER_LINE + this.direction*FRAMES_PER_ANIMATION;
 			this.animate(ANIMATION_DURATION, startTile, startTile+FRAMES_PER_ANIMATION-1, animate);			
 		}
 	}
 	
 	/**
 	 * This method is called by the enemy when the player is attacked
 	 * @param damage
 	 * @param attacker TODO Maybe interesting to know, but not used ATM. Use superclass of our dinos/animals and not Object.
 	 */
 	public void underAttack(int damage, Dinosaur attacker) {
 		this.setLife(this.life - damage);
 		this.setState(PlayerState.BEEN_HIT, this.direction);
 		if (!this.attackers.contains(attacker)) {
 			this.attackers.add(attacker);			
 		}
 	}
 	
 	public boolean removeAttacker(Dinosaur attacker) {
 		return this.attackers.remove(attacker);
 	}
 	
 	public ArrayList<Dinosaur> getAttackers() {
 		return this.attackers;
 	}
 	
 	
 	/**
 	 * Set the velocity direction. Speed is calculated based on state
 	 * @param pX
 	 * @param pY
 	 * @param state WALKING|RUNNING
 	 */
 	public void setVelocity(float pX, float pY, PlayerState state, int direction) {
 		// Check if enough energy, otherwise we reset to WALKIING
 		if (state == PlayerState.RUNNING && this.energy == 0) state = PlayerState.WALKING;
 		if (state == PlayerState.WALKING) {
 			this.body.setLinearVelocity(pX * this.velocity, pY * this.velocity);
 			//this.setEnergy(this.energy+1);
 		} else {
 			this.body.setLinearVelocity(pX * this.velocity * this.factorRunning, pY * this.velocity * this.factorRunning);
 			this.setEnergy(this.energy-1); // TODO Move to constant / variable
 		}
 		this.setState(state, direction);
 	}
 	
 	@Override
     protected void onManagedUpdate(float pSecondsElapsed) {
 		super.onManagedUpdate(pSecondsElapsed);
             
 	}
 	
 	public void setDirection(int direction) {
 		this.direction = direction;
 	}
 	
 	public boolean isAlive() {
 		return this.life > 0;
 	}
 	
 	public Vector2 getPositionVector() {
 		return new Vector2(this.getX(), this.getY());
 	}
 	
 	public float getFactorRunning() {
 		return factorRunning;
 	}
 
 	public void setFactorRunning(float factorRunning) {
 		this.factorRunning = factorRunning;
 	}
 
 	public int getLife() {
 		return life;
 	}
 
 	public void setLife(int life) {
 		if (life > 100) life = 100;
 		this.life = Math.max(life, 0);
 		this.resourcesManager.hud.setLife(this.life);
 		// TODO Game over when life == 0
 	}
 
 	public int getEnergy() {
 		return energy;
 	}
 
 	public void setEnergy(int energy) {
 		if (energy > 100) energy = 100;
 		this.energy = Math.max(energy, 0);
 		this.resourcesManager.hud.setEnergy(this.energy);
 	}
 
 	protected void createAndConnectPhysics(final BoundCamera camera, PhysicsWorld physicsWorld) {
 		this.body = PhysicsFactory.createBoxBody(physicsWorld, this, BodyType.KinematicBody, PhysicsFactory.createFixtureDef(0, 0, 0));
 		this.body.setUserData("player");	
 		this.physicsHandler = new PhysicsHandler(this);
 		this.registerUpdateHandler(this.physicsHandler);
 		physicsWorld.registerPhysicsConnector(new PhysicsConnector(
 				this, this.body, true, false) {
 			@Override
 			public void onUpdate(float pSecondsElapsed) {
 				super.onUpdate(pSecondsElapsed);
 				camera.updateChaseEntity();
 			}
 		});		
 	}
 	
 }
