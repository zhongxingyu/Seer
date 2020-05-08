 package gamedev.objects;
 
 import gamedev.game.Direction;
 import gamedev.game.ResourcesManager;
 import gamedev.scenes.GameOverScene;
 
 import org.andengine.engine.handler.physics.PhysicsHandler;
 import org.andengine.extension.physics.box2d.PhysicsConnector;
 import org.andengine.extension.physics.box2d.PhysicsFactory;
 import org.andengine.util.math.MathUtils;
 
 import android.widget.Toast;
 
 import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
 
 public class Avatar extends AnimatedObject {
 
 	private final static float ENERGY_LOSS_RUNNING = 0.5f;
 	private final static float TIME_POISENED = 10; // in seconds
 	public final static long[] ANIMATION_DURATION = { 60, 60, 60, 60, 60, 60,
 			60, 60 };
 	public final static long[] RUNNING_WALKING_POISENED_DURATION = { 120, 120,
 			120, 120, 120, 120, 120, 120 };
 	public final static int FRAMES_PER_ANIMATION = ANIMATION_DURATION.length;
 	public final static int TILES_PER_LINE = 16;
 
 	protected float energy = 100;
 	protected Inventory inventory = new Inventory();
 	protected boolean poisened = false;
 	protected float timePoisened = 0;
 
 	public Avatar(float pX, float pY) {
 		super(pX, pY, ResourcesManager.getInstance().playerRegion);
 		this.resourcesManager.camera.setChaseEntity(this);
 		this.velocity = 4f;
 		this.factorRunning = 1.5f;
 	}
 
 	public Avatar() {
 		super(0, 0, ResourcesManager.getInstance().playerRegion);
 		this.resourcesManager.camera.setChaseEntity(this);
 		this.velocity = 4f;
 		this.factorRunning = 1.5f;
 	}
 
 	public void setState(GameState state, int direction) {
 
 		if (state == this.state
 				&& (direction == -1 || direction == this.direction)) {
 			return;
 		}

 		this.state = state;
 		if (direction != -1)
 			this.direction = direction;
 		int rowIndex = 0;
 		boolean loopAnimation = false;
 
 		switch (state) {
 		case IDLE:
 			this.body.setLinearVelocity(0, 0);
 			this.stopAnimation();
 			resourcesManager.walk.stop();
 			return;
 		case ATTACK:
 			rowIndex = 0;
 			this.body.setLinearVelocity(0, 0);
 			break;
 		case BEEN_HIT:
 			rowIndex = 4;
 			this.body.setLinearVelocity(0, 0);
 			playSound(resourcesManager.pain, 1, false);
 			break;
 		case RUNNING:
 			rowIndex = 8;
 			loopAnimation = true;
 			playSound(resourcesManager.walk, (isPoisened()) ? 0.6f : 0.8f, true);
 			break;
 		case TIPPING_OVER:
 			rowIndex = 12;
 			this.body.setLinearVelocity(0, 0);
 			break;
 		case WALKING:
 			rowIndex = 16;
 			loopAnimation = true;
 			playSound(resourcesManager.walk, (isPoisened()) ? 0.4f : 0.6f, true);
 			break;
 		default:
 			return;
 		}
 
 		long[] duration = (this.isPoisened()) ? RUNNING_WALKING_POISENED_DURATION
 				: ANIMATION_DURATION;
 
 		int startTile = rowIndex * TILES_PER_LINE + this.direction
 				* FRAMES_PER_ANIMATION;
 		this.animate(duration, startTile, startTile + FRAMES_PER_ANIMATION - 1,
 				loopAnimation);
 	}
 
 	public void attack(int damage) {
 		super.attack(damage);
 		this.resourcesManager.hud.setLife(this.life);
 	}
 
 	/**
 	 * Set the velocity direction. Speed is calculated based on state
 	 * 
 	 * @param pX
 	 * @param pY
 	 * @param state
 	 *            WALKING|RUNNING
 	 */
 	public void setVelocity(float pX, float pY, GameState state) {
 		// Compute direction
 		float degree = MathUtils.radToDeg((float) Math.atan2(pX, pY));
 		int direction = Direction.getDirectionFromDegree(degree);
 		// Check if enough energy, otherwise we reset to WALKIING
 		if (state == GameState.RUNNING && this.energy == 0)
 			state = GameState.WALKING;
 
 		// Check if we are poisened
 		float velocity = (this.poisened) ? this.velocity * 0.5f : this.velocity;
 		if (state == GameState.WALKING) {
 			this.body.setLinearVelocity(pX * velocity, pY * velocity);
 		} else {
 			this.body.setLinearVelocity(pX * velocity * this.factorRunning, pY
 					* velocity * this.factorRunning);
 			this.setEnergy(this.energy - ENERGY_LOSS_RUNNING);
 		}
 		this.setState(state, direction);
 	}
 
 	/**
 	 * Getters & Setters
 	 */
 
 	public void setLife(int life) {
 		super.setLife(life);
 		this.resourcesManager.hud.setLife(this.life);
 		if (this.life < 15) {
 			playSound(resourcesManager.heartbeat, 1f, true);
 		} else {
 			resourcesManager.heartbeat.stop();
 		}
 
 		// Game over if the avatar has no more life.
 		if (this.getLife() <= 0) {
 			resourcesManager.heartbeat.stop();
 			this.setState(GameState.TIPPING_OVER, -1);
 			GameOverScene gameOverScene = GameOverScene.getInstance();
 			gameOverScene.openGameOverScene();
 		}
 	}
 
 	public float getEnergy() {
 		return energy;
 	}
 
 	public void setEnergy(float energy) {
 		if (energy > 100)
 			this.energy = 100;
 		else if (energy < 0)
 			this.energy = 0;
 		else
 			this.energy = Math.max(energy, 0);
 		this.resourcesManager.hud.setEnergy(this.energy);
 	}
 
 	public void takeEnergy(float energy) {
 		this.setEnergy(this.energy - energy);
 	}
 
 	public Inventory getInventory() {
 		return this.inventory;
 	}
 
 	@Override
 	public void onManagedUpdate(float seconds) {
 		super.onManagedUpdate(seconds);
 		if (this.poisened) {
 			if (this.timePoisened > TIME_POISENED) {
 				this.setPoisened(false);
 				this.timePoisened = 0;
 			} else {
 				this.timePoisened += seconds;
 			}
 		}
 	}
 
 	@Override
 	protected void createPhysic() {
 		this.body = PhysicsFactory.createBoxBody(resourcesManager.physicsWorld,
 				this, BodyType.DynamicBody,
 				PhysicsFactory.createFixtureDef(0, 0, 0));
 		this.body.setUserData("Avatar");
 		this.physicsHandler = new PhysicsHandler(this);
 		this.registerUpdateHandler(this.physicsHandler);
 		this.resourcesManager.physicsWorld
 				.registerPhysicsConnector(new PhysicsConnector(this, this.body,
 						true, false) {
 					@Override
 					public void onUpdate(float pSecondsElapsed) {
 						super.onUpdate(pSecondsElapsed);
 						resourcesManager.camera.updateChaseEntity();
 					}
 				});
 	}
 
 	@Override
 	public void setInitialState() {
 		this.setState(GameState.IDLE, Direction.SOUTH);
 	}
 
 	public boolean isPoisened() {
 		return poisened;
 	}
 
 	public void setPoisened(boolean poisened) {
 		this.poisened = poisened;
 		if (!poisened) {
 			this.timePoisened = 0;
 			resourcesManager.heartbeat.stop();
 		}
 	}
 
 	public void poisen(int life, int energy) {
 		this.attack(life);
 		this.takeEnergy(energy);
 		this.poisened = true;
 		this.timePoisened = 0;
 		resourcesManager.activity.toastOnUIThread("Feeling dizzy...",
 				Toast.LENGTH_SHORT);
 		playSound(resourcesManager.heartbeat, 1f, true);
 	}
 
 }
