 package fr.odai.zerozeroduck.model;
 
 
 import com.badlogic.gdx.math.Rectangle;
 import com.badlogic.gdx.math.Vector2;
 
 public class Patate {
 
 	public enum State {
		IDLE, WALKING, JUMPING, DYING
 	}
 	
 	static final float SPEED = 2f;	// unit per second
 	static final float JUMP_VELOCITY = 1f;
 	public static final float SIZE = 0.5f; // half a unit
 
 	float       stateTime = 0;
 	Vector2 	position = new Vector2();
	Vector2     velocity = new Vector2();
 	Rectangle 	bounds = new Rectangle();
 	State		state = State.WALKING;
 	boolean		facingLeft = true;
 
 	public Patate(Vector2 position) {
 		this.position = position;
 		this.bounds.height = SIZE;
 		this.bounds.width = SIZE;
 	}
 
 	public Rectangle getBounds() {
 		return bounds;
 	}
 
 	public Vector2 getPosition() {
 		return position;
 	}
 	
 	public State getState() {
 		return state;
 	}
 
 	public void setState(State state) {
 		this.state = state;
 	}
 
 	public boolean isFacingLeft() {
 		return facingLeft;
 	}
 
 	public void setFacingLeft(boolean facingLeft) {
 		this.facingLeft = facingLeft;
 	}

	public void walkForward(float delta) {
		position.x += delta * SPEED;
	}
 	
 	public void update(float delta) {
 		stateTime += delta;
		position.add(velocity.tmp().mul(delta)); 
 	}
 }
