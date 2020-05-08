 package de.redlion.civilwar.units;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.g3d.StillModelNode;
 import com.badlogic.gdx.math.MathUtils;
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.math.collision.BoundingBox;
 
 public class Soldier {
 
 	public int id;
 	public float angle = MathUtils.random(-90, 90);
 	public int angleSpin = 1;
 
 	private float shotCooldownTime = 6f;
 	private float shotCapacity = 5f;
 	private float shotReloadRate = 1f;
 
 	private float shots = shotCapacity;
 	private float cooldown = 0;
 	
 	protected float turnSpeed = 100.0f;
 	protected float accel = 1.0f;
 	protected float hitPoints = 0;
 
 	protected float maxHitPoints = 0;
 
 	private float delta = 0.0f;
 
 	public float aliveTime = 0.0f;
 
 	public Vector2 position = new Vector2();
 	public Vector2 velocity = new Vector2();
 	public Vector2 facing = new Vector2();
 	public float height = 0;
	public float targetHeight = 0;
	public int heightIterator = 0;
 	
 	public DefaultAI ai = new DefaultAI(this);
 	
 	public boolean alive = true;
 	
 	//render stuff
 	public StillModelNode instance;	
 	public BoundingBox instanceBB;
 
 	public Soldier(int id, Vector2 position, Vector2 facing) {
 		this.id = id;
 
 		this.position.set(position);
 		this.facing.set(facing);
 		this.facing.rotate(angle);
 		
 		instanceBB = new BoundingBox();		
 		instance = new StillModelNode();
 	}
 	
 	public void update(float delta) {
 		
 		this.delta = delta;
 		
 		ai.update();
 		
 		cooldown = Math.max(0, cooldown - delta*50f);
 		shots = Math.min(shots + (shotReloadRate * delta), shotCapacity);
 
 		velocity.mul( (float) Math.pow(0.97f, delta * 30.f));
 		position.add(velocity.x * delta, velocity.y * delta);
 		
 		//death check
 		if(!alive){
 			if(angle < 90 && angle > -90) {
 				angle = angle + delta * 100.f * angleSpin;
 			}
 			return;
 		}
 		
 		angle = angle + delta * 20.f * angleSpin;
 		if(angle > 15) {
 			angleSpin = -1;
 		}
 		if(angle < -15) {
 			angleSpin = 1;
 		}		
 	}
 	
 	public void turn(float direction) {
 		delta = Math.min(0.06f, Gdx.graphics.getDeltaTime());
 		
 		facing.rotate(direction * turnSpeed * delta).nor();
 
 	}
 
 	public void thrust() {
 		delta = Math.min(0.06f, Gdx.graphics.getDeltaTime());
 		
 		velocity.add(facing.x * accel * delta, facing.y * accel * delta);
 	}
 	
 	public void thrust(float amount) {
 		delta = Math.min(0.06f, Gdx.graphics.getDeltaTime());
 		
 		velocity.add(facing.x * accel * delta, facing.y * accel * amount * delta);		
 	}
 
 	public void goTowardsOrAway(Vector2 targetPos, boolean forceThrust, boolean isAway) {
 		Vector2 target_direction = targetPos.tmp().sub(position);
 		if (isAway) {
 			target_direction.mul(-1);
 		}
 
 		if (facing.crs(target_direction) > 0) {
 			turn(1);
 		} else {
 			turn(-1);
 		}
 
 		if (forceThrust || facing.dot(target_direction) > 0) {
 			thrust();
 		}
 	}
 	
 	// automatically thrusts and turns according to the target
 	public void goTowards(Vector2 targetPos, boolean forceThrust) {
 		goTowardsOrAway(targetPos, forceThrust, false);
 	}
 
 	public void goAway(Vector2 targetPos, boolean forceThrust) {
 		goTowardsOrAway(targetPos, forceThrust, true);
 	}
 	
 	public boolean isEmpty() {
 		return shots < 1;
 	}
 
 	public boolean isReloaded() {
 		return shots == shotCapacity;
 	}
 
 	public boolean isCooledDown() {
 		return cooldown == 0;
 	}
 
 	public boolean isReadyToShoot() {
 		return isCooledDown() && !isEmpty();
 	}
 
 	public void shoot() {
 		if (cooldown == 0 && shots >= 1) {
 			shots -= 1;
 			cooldown = shotCooldownTime;
 		}
 	}
 	
 	public void hit() {
 		alive = false;
 	}
 
 }
