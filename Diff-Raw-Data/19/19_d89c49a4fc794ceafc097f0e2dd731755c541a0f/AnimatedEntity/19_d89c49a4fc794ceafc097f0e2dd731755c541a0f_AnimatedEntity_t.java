 package propra2012.gruppe33.graphics.rendering.scenegraph.entities;
 
 import propra2012.gruppe33.graphics.rendering.scenegraph.Entity;
 import propra2012.gruppe33.graphics.rendering.scenegraph.Vector2f;
 
 public class AnimatedEntity extends Entity {
 
 	// The acc of this entity
 	private Vector2f acceleration = Vector2f.zero();
 
 	// The velocity of this entity
 	private Vector2f velocity = Vector2f.zero();
 
 	// The angular velocity of this entity
 	private float angularVelocity = 0;
 
 	public AnimatedEntity(String id) {
 		super(id);
 	}
 
 	public Vector2f getAcceleration() {
 		return acceleration;
 	}
 
 	public void setAcceleration(Vector2f acceleration) {
 		if (acceleration != null) {
 			this.acceleration = acceleration;
 		}
 	}
 
 	public float getAngularVelocity() {
 		return angularVelocity;
 	}
 
 	public void setAngularVelocity(float angularVelocity) {
 		this.angularVelocity = angularVelocity;
 	}
 
 	public Vector2f getVelocity() {
 		return velocity;
 	}
 
 	public void setVelocity(Vector2f velocity) {
 		if (velocity != null) {
 			this.velocity = velocity;
 		}
 	}
 
 	@Override
 	protected void doUpdate(float tpf) {
 		super.doUpdate(tpf);
 
 		// Acc, Vel and Pos!
		getPosition().addLocal(
				velocity.addLocal(acceleration.scale(tpf)).scale(tpf));
 
 		// Rotation...
		setRotation(getRotation() + getAngularVelocity() * tpf);
 	}
 }
