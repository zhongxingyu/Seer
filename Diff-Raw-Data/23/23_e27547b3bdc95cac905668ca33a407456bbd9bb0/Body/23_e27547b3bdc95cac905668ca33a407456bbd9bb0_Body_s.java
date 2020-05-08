 package com.punchline.javalib.entities.components.physical;
 
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.physics.box2d.BodyDef;
 import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
 import com.badlogic.gdx.physics.box2d.FixtureDef;
 import com.badlogic.gdx.physics.box2d.Shape;
 import com.punchline.javalib.entities.Entity;
 import com.punchline.javalib.entities.EntityWorld;
import com.punchline.javalib.entities.PhysicsWorld;
 import com.punchline.javalib.entities.components.ComponentManager;
 
 /**
  * Component wrapper for a Box2D {@link com.badlogic.gdx.physics.box2d.Body
  * Body}.
  * 
  * @author Natman64
  * 
  */
 public class Body implements Transform, Velocity {
 
 	// region Fields/Initialization
 
 	private EntityWorld entityWorld;
 	private com.badlogic.gdx.physics.box2d.Body body;
 
 	/**
 	 * Constructs a Body without using BodyDef or FixtureDef.
 	 * 
 	 * @param world
 	 *            The EntityWorld.
 	 * @param e
 	 *            The entity that contains this Body.
 	 * @param bodyType
 	 *            The type of this Body.
 	 * @param shape
 	 *            The body's shape.
 	 * @param position
 	 *            The body's initial position.
 	 */
 	public Body(EntityWorld world, Entity e, BodyType bodyType, Shape shape,
 			Vector2 position) {
 		entityWorld = world;
 
 		BodyDef bodyDef = new BodyDef();
 		bodyDef.type = bodyType;
 		bodyDef.position.set(position);
 
 		body = world.getBox2DWorld().createBody(bodyDef);
 		body.setUserData(e);
 
 		FixtureDef fixtureDef = new FixtureDef();
 		fixtureDef.shape = shape;
 
 		body.createFixture(fixtureDef);
 	}
 
 	/**
 	 * Creates a Body without any attached fixtures.
 	 * 
 	 * @param world
 	 *            The EntityWorld.
 	 * @param e
 	 *            The entity that contains this Body.
 	 * @param bodyType
 	 *            The type of this Body.
 	 * @param position
 	 *            The body's initial position.
 	 */
 	public Body(EntityWorld world, Entity e, BodyType bodyType, Vector2 position) {
 		entityWorld = world;
 
 		BodyDef bodyDef = new BodyDef();
 		bodyDef.type = bodyType;
 		bodyDef.position.set(position);
 
 		body = world.getBox2DWorld().createBody(bodyDef);
 		body.setUserData(e);
 	}
 
 	/**
 	 * Constructs a body component.
 	 * 
 	 * @param world
 	 *            The EntityWorld.
 	 * @param e
 	 *            The entity that will own this body.
 	 * @param bodyDef
 	 *            The definition of the body to be created.
 	 * @param fixtureDef
 	 *            The definition of the body's fixture.
 	 */
 	public Body(EntityWorld world, Entity e, BodyDef bodyDef,
 			FixtureDef fixtureDef) {
 		this(world, e, bodyDef);
 		body.createFixture(fixtureDef);
 	}
 
 	/**
 	 * Constructs a body component without a fixture definition.
 	 * 
 	 * @param world
 	 *            The EntityWorld.
 	 * @param e
 	 *            The entity that will own this body.
 	 * @param bodyDef
 	 *            The definition of the body to be created.
 	 */
 	public Body(EntityWorld world, Entity e, BodyDef bd) {
 		entityWorld = world;
 		body = world.getBox2DWorld().createBody(bd);
 		body.setUserData(e);
 	}
 
 	// endregion
 
 	// region Accessors
 
 	/**
 	 * @return The Box2D body.
 	 */
 	public com.badlogic.gdx.physics.box2d.Body getBody() {
 		return body;
 	}
 
 	// endregion
 
 	// region Transform Implementation
 
 	@Override
 	public Vector2 getPosition() {
		Vector2 pos = body.getPosition().cpy();
		
//		PhysicsWorld world = entityWorld.getPhysicsWorld();
//		
//		if (world.getElapsedRatio() > 0) {
//			Vector2 delta = getLinearVelocity().cpy().scl(world.getElapsedRatio() / (1 / PhysicsWorld.TIME_STEP));
//			pos.add(delta);
//		}
		
		return pos;
 	}
 
 	@Override
 	public void setPosition(Vector2 position) {
 		body.setTransform(position, body.getAngle());
 	}
 
 	@Override
 	public float getRotation() {
 		return body.getAngle();
 	}
 
 	@Override
 	public void setRotation(float rotation) {
 		body.setTransform(getPosition(), rotation);
 	}
 
 	@Override
 	public Vector2 getOrigin() {
 		return body.getWorldCenter();
 	}
 
 	// endregion
 
 	// region Velocity Implementation
 
 	@Override
 	public Vector2 getLinearVelocity() {
 		return body.getLinearVelocity();
 	}
 
 	@Override
 	public void setLinearVelocity(Vector2 linearVelocity) {
 		body.setLinearVelocity(linearVelocity);
 	}
 
 	@Override
 	public float getAngularVelocity() {
 		return body.getAngularVelocity();
 	}
 
 	@Override
 	public void setAngularVelocity(float angularVelocity) {
 		body.setAngularVelocity(angularVelocity);
 	}
 
 	// endregion
 
 	// region Events
 
 	@Override
 	public void onAdd(ComponentManager container) {
 
 	}
 
 	@Override
 	public void onRemove(ComponentManager container) {
 		entityWorld.getBox2DWorld().destroyBody(body);
 	}
 
 	// endregion
 
 }
