 package fr.odai.zerozeroduck.model;
 
 import com.badlogic.gdx.math.Vector2;
 import fr.odai.zerozeroduck.controller.MainController.Keys;
 
 public class Trap {
 	public enum State {
 		RELOADING, // Not available yet, waiting for RELOAD_TIME seconds
 		HURTING, // Hurting enemies
 		READY, // Ready to be enabled
 		DISABLED // Disabled
 	}
 	
 	public static final float SIZE = 0.5f; // half a unit
 	public static final float RELOAD_TIME = 2;
 	public static final float HURTING_TIME = 2;
 	
 	Vector2 position = new Vector2();
 	State state;
 	int damage;
 	Keys associatedKey;
 	
 	float range;
 
 	private float stateTime;
 
 	public float getRange() {
 		return range;
 	}
 
 	public void setRange(float range) {
 		this.range = range;
 	}
 
 	public Vector2 getPosition() {
 		return position;
 	}
 
 	public void setPosition(Vector2 position) {
 		this.position = position;
 	}
 
 	public State getState() {
 		return state;
 	}
 
 	public void setState(State state) {
 		this.state = state;
 		stateTime = 0;
 	}
 	
 	public Keys getAssociatedKey() {
 		return associatedKey;
 	}
 
 	public void setAssociatedKey(Keys associatedKey) {
 		this.associatedKey = associatedKey;
 	}
 
 	public Trap(float range, Vector2 position, int damage) {
 		super();
 		this.range = range;
 		this.position = position;
 		this.damage = damage;
 	}
 
 	public void activate() {
 		if(state == State.READY)
 			setState(State.HURTING);
 	}
 	
 	public void update(float delta) {
 		stateTime += delta;
 		
 		if(state == State.HURTING && stateTime > HURTING_TIME)
 			setState(State.RELOADING);
 		
 		if(state == State.RELOADING && stateTime > RELOAD_TIME)
 			setState(State.READY);
 	}
 }
