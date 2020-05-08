 package gamedev.objects;
 
 import gamedev.game.Direction;
 import gamedev.game.ResourcesManager;
 
 import java.util.ArrayList;
 
 import org.andengine.engine.handler.physics.PhysicsHandler;
 import org.andengine.entity.sprite.AnimatedSprite;
 import org.andengine.extension.physics.box2d.PhysicsConnector;
 import org.andengine.extension.physics.box2d.PhysicsFactory;
 import org.andengine.util.math.MathUtils;
 
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.physics.box2d.Body;
 import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
 
 public class Player extends AnimatedSprite {
 
 	public final static long[] ANIMATION_DURATION = { 50, 50, 50, 50, 50, 50,
			50, 50 };
 	public final static int FRAMES_PER_ANIMATION = 8;
 	public final static int TILES_PER_LINE = 16;
 
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
 		IDLE, WALKING, RUNNING, BEEN_HIT, ATTACK, TIPPING_OVER,
 	}
 
 	/**
 	 * Create the player
 	 * 
 	 * @param pX
 	 *            x-Position in the World
 	 * @param pY
 	 *            y-Position in the World
 	 */
 	public Player(float pX, float pY) {
 		super(pX, pY, ResourcesManager.getInstance().playerRegion,
 				ResourcesManager.getInstance().vbom);
 		this.resourcesManager = ResourcesManager.getInstance();
 		this.resourcesManager.camera.setChaseEntity(this);
 		this.createAndConnectPhysics();
 	}
 
 	public Player() {
 		super(0, 0, ResourcesManager.getInstance().playerRegion,
 				ResourcesManager.getInstance().vbom);
 		this.resourcesManager = ResourcesManager.getInstance();
 		this.resourcesManager.camera.setChaseEntity(this);
 		this.createAndConnectPhysics();
 		// Scale it up. Texture has scale 0.48.
 		// this.mScaleX = this.mScaleX * 1.2f;
 		// this.mScaleY = this.mScaleY * 1.2f;
 	}
 
 	/**
 	 * Display Player animation on current State and Direction Also validate
 	 * some states here, e.g. running is not possible if energy = 0
 	 * 
 	 * @param state
 	 *            new PlayerState
 	 * @param direction
 	 *            the direction of the animation. Pass "-1" if you don't need to
 	 *            compute/change the direction
 	 */
 	public void setState(PlayerState state, int direction) {
 
 		boolean stateHasChanged = false;
 		if (this.currentState != state && !this.isAnimationRunning()) {
 			this.currentState = state;
 			stateHasChanged = true;
 		}
 
 		// Stop animation if state is idle.
 		if (state == PlayerState.IDLE) {
 			this.body.setLinearVelocity(0, 0);
 			this.stopAnimation();
 			this.setEnergy(this.energy + 1);
 			return;
 		}
 
 		// Check if direction has changed.
 		boolean directionHasChanged = false;
 		if (direction > -1 && this.direction != direction) {
 			this.direction = direction;
 			directionHasChanged = true;
 		}
 
 		// Change animation if no animation is running or if the direction has
 		// changed.
 		if (!this.isAnimationRunning() || directionHasChanged
 				|| stateHasChanged) {
 			int rowIndex = 0;
 			if (state == PlayerState.ATTACK)
 				rowIndex = 0;
 			if (state == PlayerState.BEEN_HIT)
 				rowIndex = 4;
 			if (state == PlayerState.RUNNING)
 				rowIndex = 8;
 			if (state == PlayerState.TIPPING_OVER)
 				rowIndex = 12;
 			if (state == PlayerState.WALKING)
 				rowIndex = 16;
 
 			// Do not loop animation since it will be looped automatically when
 			// the animation is over.
 			boolean loopAnimation = false;
 
 			int startTile = rowIndex * TILES_PER_LINE + this.direction
 					* FRAMES_PER_ANIMATION;
 			this.animate(ANIMATION_DURATION, startTile, startTile
 					+ FRAMES_PER_ANIMATION - 1, loopAnimation);
 		}
 	}
 
 	public PlayerState getState() {
 		return this.currentState;
 	}
 
 	/**
 	 * This method is called by the enemy when the player is attacked
 	 * 
 	 * @param damage
 	 * @param attacker
 	 *            TODO Maybe interesting to know, but not used ATM. Use
 	 *            superclass of our dinos/animals and not Object.
 	 */
 	public void underAttack(int damage, Dinosaur attacker) {
 		this.setLife(this.life - damage);
 		this.setState(PlayerState.BEEN_HIT, this.direction);
 		if (!this.attackers.contains(attacker)) {
 			this.attackers.add(attacker);
 		}
 
 		// TODO: Remove.
 		System.out.println(attacker);
 		System.out.println("PlayerPos: " + this.body.getPosition());
 	}
 
 	public boolean removeAttacker(Dinosaur attacker) {
 		return this.attackers.remove(attacker);
 	}
 
 	public ArrayList<Dinosaur> getAttackers() {
 		return this.attackers;
 	}
 
 	/**
 	 * Set the velocity direction. Speed is calculated based on state
 	 * 
 	 * @param pX
 	 * @param pY
 	 * @param state
 	 *            WALKING|RUNNING
 	 */
 	public void setVelocity(float pX, float pY, PlayerState state) {
 		// Compute direction
 		float degree = MathUtils.radToDeg((float) Math.atan2(pX, pY));
 		int direction = Direction.getDirectionFromDegree(degree);
 		// Check if enough energy, otherwise we reset to WALKIING
 		if (state == PlayerState.RUNNING && this.energy == 0)
 			state = PlayerState.WALKING;
 		if (state == PlayerState.WALKING) {
 			this.body.setLinearVelocity(pX * this.velocity, pY * this.velocity);
 		} else {
 			this.body.setLinearVelocity(
 					pX * this.velocity * this.factorRunning, pY * this.velocity
 							* this.factorRunning);
 			this.setEnergy(this.energy - 1); // TODO Move to constant / variable
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
 		if (life > 100)
 			life = 100;
 		this.life = Math.max(life, 0);
 		this.resourcesManager.hud.setLife(this.life);
 		// TODO Game over when life == 0
 	}
 
 	public int getEnergy() {
 		return energy;
 	}
 
 	public void setEnergy(int energy) {
 		if (energy > 100)
 			energy = 100;
 		this.energy = Math.max(energy, 0);
 		this.resourcesManager.hud.setEnergy(this.energy);
 	}
 
 	protected void createAndConnectPhysics() {
 		this.body = PhysicsFactory.createBoxBody(resourcesManager.physicsWorld,
 				this, BodyType.DynamicBody,
 				PhysicsFactory.createFixtureDef(0, 0, 0));
 		this.body.setUserData("Player");
 		this.physicsHandler = new PhysicsHandler(this);
 		this.registerUpdateHandler(this.physicsHandler);
 
 		resourcesManager.physicsWorld
 				.registerPhysicsConnector(new PhysicsConnector(this, this.body,
 						true, false) {
 					@Override
 					public void onUpdate(float pSecondsElapsed) {
 						super.onUpdate(pSecondsElapsed);
 						resourcesManager.camera.updateChaseEntity();
 					}
 				});
 	}
 
 	public int getDirection() {
 		return this.direction;
 	}
 
 }
