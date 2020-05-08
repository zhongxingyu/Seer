 package gamedev.objects;
 
 import gamedev.ai.MoveStrategy;
 import gamedev.game.Direction;
 import gamedev.game.GameActivity;
 import gamedev.game.GameActivity.GameMode;
 import gamedev.game.ResourcesManager;
 
 import org.andengine.audio.sound.Sound;
 import org.andengine.engine.handler.physics.PhysicsHandler;
 import org.andengine.entity.sprite.AnimatedSprite;
 import org.andengine.extension.physics.box2d.PhysicsConnector;
 import org.andengine.extension.physics.box2d.PhysicsFactory;
 import org.andengine.extension.physics.box2d.util.Vector2Pool;
 import org.andengine.opengl.texture.region.ITiledTextureRegion;
 
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.physics.box2d.Body;
 import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
 
 public abstract class AnimatedObject extends AnimatedSprite {
 
 	protected Body body;
 	protected PhysicsHandler physicsHandler;
 	protected ResourcesManager resourcesManager;
 
 	/**
 	 * Different states. Not each animated object can implement all of these...
 	 */
 	public enum GameState {
 		IDLE, WALKING, RUNNING, ROARING, LOOKING, PAUSED, BEEN_HIT, TIPPING_OVER, ATTACK, CHASE_PLAYER, DEAD,
 	}
 
 	/** Current direction */
 	protected int direction;
 
 	/** Velocity */
 	protected float velocity;
 
 	/** Factor multiplied to velocity when RUNNING */
 	protected float factorRunning = 1.0f;
 
 	/** Current state */
 	protected GameState state;
 
 	/** Life */
 	protected int life = 100;
 
 	/** A move strategy */
 	protected MoveStrategy moveStrategy;
 
 	/** Duration of the current animation/state */
 	protected float animationTime;
 
 	/** The time alive... just a counter */
 	protected float timeAlive = 0;
 
 	public AnimatedObject(float x, float y, ITiledTextureRegion textureRegion) {
 		super(x, y, textureRegion, ResourcesManager.getInstance().vbom);
 		this.resourcesManager = ResourcesManager.getInstance();
 		this.direction = Direction.getRandomDirection();
 		this.createPhysic();
 		this.setInitialState();
 	}
 
 	/**
 	 * Set the initial state for an object
 	 */
 	abstract public void setInitialState();
 
 	/**
 	 * Set state for this object. Each object must set animation/time depending
 	 * on states and handle additional logic in this method
 	 * 
 	 * @param state
 	 * @param direction
 	 *            New direction. Set -1 if you don't need to change the
 	 *            direction
 	 */
 	abstract public void setState(GameState state, int direction);
 
 	/**
 	 * Connect objects physic to the world
 	 */
 	protected void createPhysic() {
 		this.body = PhysicsFactory.createBoxBody(
 				this.resourcesManager.physicsWorld, this,
 				BodyType.KinematicBody,
 				PhysicsFactory.createFixtureDef(0, 0, 0, true));
 		this.resourcesManager.physicsWorld
 				.registerPhysicsConnector(new PhysicsConnector(this, this.body,
 						true, false));
 	}
 
 	/**
 	 * Quit if object has state DEAD Each subclass should call this parent
 	 * method first, then implement its own logic
 	 */
 	@Override
 	public void onManagedUpdate(float pSecondsElapsed) {
 		super.onManagedUpdate(pSecondsElapsed);
 		this.timeAlive += pSecondsElapsed;
 		this.onCustomUpdate(pSecondsElapsed);
 	}
 
 	public boolean onCustomUpdate(float pSecondsElapsed) {
 		if (this.state == GameState.DEAD)
 			return false;
 		if (GameActivity.mode == GameMode.FIGHTING
 				|| GameActivity.mode == GameMode.POPUP) {
 			this.body.setLinearVelocity(0, 0);
 			this.setState(GameState.IDLE, -1);
 			return false;
 		}
 		return true;
 	}
 
 	/**
 	 * Attack object
 	 * 
 	 * @param damage
 	 *            Amount of life taken
 	 */
 	public void attack(int damage) {
		if (this.state == GameState.DEAD) return;
 		int life = this.life - damage;
 		this.setLife(life);
 		if (this.life <= 0) {
 			this.life = 0;
 			this.setState(GameState.DEAD, -1);
 		} else {
 			this.setState(GameState.BEEN_HIT, -1);
 		}
 	}
 
 	/**
 	 * Move this object to a specific position (display correct animation and
 	 * direction) This method should not be called directly - instead set a
 	 * (new) MoveStragey to the object!
 	 * 
 	 * @param pos
 	 * @param state
 	 */
 	public void moveTo(Vector2 pos, GameState state) {
 		Vector2 bodyPos = this.body.getPosition();
 		int direction = Direction.getDirectionFromVectors(bodyPos, pos);
 		Vector2 v = Vector2Pool.obtain(pos.x - bodyPos.x, pos.y - bodyPos.y);
 		v.nor();
 		if (state == GameState.WALKING) {
 			this.body.setLinearVelocity(v.x * this.velocity, v.y
 					* this.velocity);
 		} else {
 			this.body.setLinearVelocity(v.x * this.velocity
 					* this.factorRunning, v.y * this.velocity
 					* this.factorRunning);
 		}
 		Vector2Pool.recycle(v);
 		this.setState(state, direction);
 	}
 
 	public void moveTo(float x, float y, GameState state) {
 		this.moveTo(new Vector2(x, y), state);
 	}
 
 	public boolean isAlive() {
 		return this.life > 0;
 	}
 
 	/**
 	 * Getters & Setters
 	 */
 
 	public void setLife(int life) {
 		if (life > 100) {
 			life = 100;
 		}
 		this.life = Math.max(life, 0);
 	}
 
 	public int getLife() {
 		return this.life;
 	}
 
 	public int getDirection() {
 		return direction;
 	}
 
 	public void setDirection(int direction) {
 		this.direction = direction;
 	}
 
 	public float getVelocity() {
 		return velocity;
 	}
 
 	public void setVelocity(float velocity) {
 		this.velocity = velocity;
 	}
 
 	public float getFactorRunning() {
 		return factorRunning;
 	}
 
 	public void setFactorRunning(float factorRunning) {
 		this.factorRunning = factorRunning;
 	}
 
 	public Body getBody() {
 		return body;
 	}
 
 	public GameState getState() {
 		return state;
 	}
 
 	public MoveStrategy getMoveStrategy() {
 		return moveStrategy;
 	}
 
 	public void setMoveStrategy(MoveStrategy moveStrategy) {
 		this.moveStrategy = moveStrategy;
 	}
 
 	public float getTimeAlive() {
 		return timeAlive;
 	}
 
 	public void setTimeAlive(float timeAlive) {
 		this.timeAlive = timeAlive;
 	}
 	
 	public void playSound(Sound sound, float rate, boolean loop) {
 		sound.stop();
 		sound.setRate(rate);
 		sound.setLooping(loop);
 		sound.play();
 	}
 
 
 }
