 package entities;
 
 import org.lwjgl.util.vector.Vector3f;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Image;
 
 import util.SpriteSheet;
 import base.World;
 
 public abstract class MovingEntity extends Entity {
 
 	private Vector3f velocity = new Vector3f(0, 0, 0);
 
 	public MovingEntity(SpriteSheet ss, float scale, int depth,
 			Vector3f position, Vector3f initalVelocity, World world) {
 		super(ss, true, depth, scale, position, world);
 
 		velocity = initalVelocity;
 	}
 
 	public MovingEntity(Image image, float scale, int depth, Vector3f position,
 			Vector3f initalVelocity, World world) {
 		super(image, true, depth, scale, position, world);
 
 		velocity = initalVelocity;
 	}
 
 	public Vector3f getVelocity() {
 		return new Vector3f(velocity);
 	}
 
 	protected float getVelocitySize() {
 		return velocity.length();
 	}
 
 	abstract protected Vector3f acceleration(int deltaT, GameContainer gc);
 
 	private final float lowestNonZeroSpeed = 0.27f;
 
 	@Override
 	public void update(int deltaT, GameContainer gc) {
 		super.update(deltaT, gc);
 
 		act(deltaT, gc);
 
 		Vector3f.add(velocity, acceleration(deltaT, gc), velocity);
 
 		Vector3f oldPosition = position;
 
 		Vector3f scaledVelocity = new Vector3f(velocity);
 
 		scaledVelocity.scale((float) deltaT / 33);
 
 		position = Vector3f.add(position, scaledVelocity, null);
 
		if (velocity.length() < lowestNonZeroSpeed && world.inWater(position)) {
 			position = oldPosition;
 			velocity = new Vector3f();
 			return;
 		}
 
 		float bounceDamping = 0.4f;
 
 		Vector3f boundry = world.hitBoundry(this);
 
 		if (boundry.lengthSquared() != 0) {
 
 			if (boundry.x > 0) {
 				velocity.x = -velocity.x;
 			} else if (boundry.z > 0) {
 				velocity.z = -velocity.z;
 			} else if (boundry.y > 0) {
 				velocity.y = -velocity.y;
 			}
 
 			velocity.scale(bounceDamping);
 			Vector3f.add(position, velocity, position);
 			return;
 
 		}
 	}
 
 	public void accelerate(Vector3f acceleration) {
 		Vector3f.add(velocity, acceleration, velocity);
 	}
 }
