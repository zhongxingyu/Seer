 /**
  * @author Arthur
  * @version 1.3
  */
 package logic.entity;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.jbox2d.collision.shapes.PolygonShape;
 import org.jbox2d.common.Vec2;
 import org.jbox2d.dynamics.Body;
 import org.jbox2d.dynamics.BodyDef;
 import org.jbox2d.dynamics.BodyType;
 import org.jbox2d.dynamics.FixtureDef;
 import org.jbox2d.dynamics.joints.Joint;
 import org.jbox2d.dynamics.joints.RevoluteJointDef;
 import org.lwjgl.opengl.GL11;
 import org.lwjgl.util.Color;
 import org.lwjgl.util.vector.Vector2f;
 import org.newdawn.slick.Image;
 import render.RenderUtil;
 import util.MathUtil;
 import controller.ControlledObject;
 import logic.Level;
 import main.Game;
 
 final public class Ship extends GameObjectPhysicMoving implements
 		ControlledObject {
 	private float Ft = 300000f;
 	private boolean leftEngineActive = false;
 	private boolean rightEngineActive = false;
 	private boolean allEngineActive = false;
 	private boolean turnEnginesLeft = false;
 	private boolean turnEnginesRight = false;
 	private boolean weapon1Shot = false;
 	private boolean weapon2Shot = false;
 	private float width, height;
 	private float protection = 0.1f;
 	private List<ArsenalGameObject> arsenalList = new ArrayList<ArsenalGameObject>();
 	private Image image;
 	private final static float SPEED_PARTICLE_FROM_ENGINE = 200f;
 	private final static float SPEED_PARTICLE_KOOF_RANDOM = 20f;
 	private final static float ENGINE_TURN_VELOCITY = 10f;
 	private final static float ENGINE_FORCE = 1;
 	private Vector2f ENGINE_POSITION = new Vector2f(50f, 0f);
 	private Engine leftEngine, rightEngine;
 	private float engineRelativeAngle = 0;
 	private static final float MAX_ENGINE_ANGLE = 45/60f;
 
 	// private ArrayList<ShipComponent> shipComponents;
 	public Ship(Level level, float x, float y) {
 		// if(!loadParametres(..))
 		// throw...;
 		this.level = level;
 		position = new Vector2f(x, y);
 		init();
 	}
 
 	@Override
 	public void init() {
 		width = 100f;
 		height = 40f;
 		liveHealth = 10000;
 		BodyDef shipDef = new BodyDef();
 		shipDef.position.set(new Vec2(position.x / 30, position.y / 30));
 		shipDef.type = BodyType.DYNAMIC;
 		PolygonShape shipShape = new PolygonShape();
 		shipShape.setAsBox(width / 30 / 2, height / 30 / 2);
 		this.body = level.getWorld().createBody(shipDef);
 		this.body.m_userData = this;
 
 		FixtureDef shipFixture = new FixtureDef();
 		shipFixture.friction = 0.5f; // trenie
 		shipFixture.density = 0.5f; // plotnost'
 		shipFixture.restitution = 0.15f;
 		shipFixture.shape = shipShape;
 		body.createFixture(shipFixture);
 		body.setAngularDamping(3);
 
 		{
 			// adding weapons
 			SimpleWeapon weap1 = new SimpleWeapon(this, 20, 3, 1, 3, 4);
 			SimpleWeapon weap2 = new SimpleWeapon(this, 10, 5, 2, 15, 70);
 			arsenalList.add(weap1);
 			arsenalList.add(weap2);
 			level.getGameObjects().add(weap1);
 			level.getGameObjects().add(weap2);
 		}
 
 		{	
 			// adding engines
 			leftEngine = new Engine(level, new Vector2f(position.x
 					- ENGINE_POSITION.x, position.y - ENGINE_POSITION.y));
 			rightEngine = new Engine(level, new Vector2f(position.x
 					+ ENGINE_POSITION.x, position.y - ENGINE_POSITION.y));
 			addEngine(leftEngine);
 			addEngine(rightEngine);
 			level.getNotAddedGameObjects().add(leftEngine);
 			level.getNotAddedGameObjects().add(rightEngine);
 		}
 	}
 
 	private void addEngine(Engine engine) {
 		RevoluteJointDef jointDef = new RevoluteJointDef();
 		jointDef.initialize(body, engine.getBody(),
 				new Vec2(engine.getPosition().x / 30f,
 						engine.getPosition().y / 30f));
 		jointDef.collideConnected = false;
 //		jointDef.enableLimit = true;
 //		jointDef.lowerAngle = -MAX_ENGINE_ANGLE;
 //		jointDef.upperAngle = MAX_ENGINE_ANGLE;
 //		jointDef.referenceAngle = 0;
 		Joint joint = level.getWorld().createJoint(jointDef);
 		engine.setEngineJoint(joint);
 
 	}
 
 	@Override
 	public void update() {
 
 		if (weapon1Shot && !arsenalList.isEmpty()) {
 			arsenalList.get(0).setShootOn();
 		}
 		if (weapon2Shot && !arsenalList.isEmpty()) {
 			arsenalList.get(1).setShootOn();
 		}
 
 		// create mirage
 
 		clearFlags();
 		if (liveHealth < 0)
 			live = false;
 
 	}
 
 	// TODO add *dt
 	@Override
 	public void move() {
 		position = new Vector2f(body.getPosition().x * 30,
 				body.getPosition().y * 30);
 		angle = body.getAngle();
 
 		if (allEngineActive) {
 			leftEngine.enableForce();
 			rightEngine.enableForce();
 
 		}
 
 		if (rightEngineActive) {
 			rightEngine.enableForce();
 
 		}
 
 		if (leftEngineActive) {
 			leftEngine.enableForce();
 		}
 
 		if (turnEnginesLeft) {
 			// leftEngine.getBody().setAngularVelocity(ENGINE_TURN_VELOCITY);
 			// rightEngine.getBody().setAngularVelocity(ENGINE_TURN_VELOCITY);
 			engineRelativeAngle += 0.1;
 		}
 		if (turnEnginesRight) {
 			// leftEngine.getBody().setAngularVelocity(-ENGINE_TURN_VELOCITY);
 			// rightEngine.getBody().setAngularVelocity(-ENGINE_TURN_VELOCITY);
 			engineRelativeAngle -= 0.1;
 
 		}
 
 		if (engineRelativeAngle > MAX_ENGINE_ANGLE)
 			engineRelativeAngle = MAX_ENGINE_ANGLE;
 		else if (engineRelativeAngle < -MAX_ENGINE_ANGLE)
 			engineRelativeAngle = -MAX_ENGINE_ANGLE;
 		System.out.println(engineRelativeAngle * 60);
 		leftEngine.getBody().setTransform(leftEngine.getBody().getPosition(),
 				body.getAngle() + engineRelativeAngle);
 		rightEngine.getBody().setTransform(rightEngine.getBody().getPosition(),
 				body.getAngle() + engineRelativeAngle);
 
 	}
 
 	@Override
 	public void draw() {
 		GL11.glColor3ub(Color.WHITE.getRedByte(), Color.WHITE.getGreenByte(),
 				Color.WHITE.getBlueByte());
 
 		RenderUtil.drawLine(
 				new Vector2f(position.x
 						+ MathUtil.newXTurn(-width / 2, -height / 2, angle),
 						position.y
 								+ MathUtil.newYTurn(-width / 2, -height / 2,
 										angle)),
 				new Vector2f(position.x
 						+ MathUtil.newXTurn(width / 2, -height / 2, angle),
 						position.y
 								+ MathUtil.newYTurn(width / 2, -height / 2,
 										angle)), 3f, (Color) Color.GREY);
 
 		RenderUtil.drawLine(
 				new Vector2f(position.x
 						+ MathUtil.newXTurn(-width / 2, height / 2, angle),
 						position.y
 								+ MathUtil.newYTurn(-width / 2, height / 2,
 										angle)),
 				new Vector2f(position.x
 						+ MathUtil.newXTurn(width / 2, height / 2, angle),
 						position.y
 								+ MathUtil.newYTurn(width / 2, height / 2,
 										angle)), 3f, (Color) Color.GREY);
 
 		RenderUtil.drawLine(
 				new Vector2f(position.x
 						+ MathUtil.newXTurn(-width / 2, -height / 2, angle),
 						position.y
 								+ MathUtil.newYTurn(-width / 2, -height / 2,
 										angle)),
 				new Vector2f(position.x
 						+ MathUtil.newXTurn(-width / 2, height / 2, angle),
 						position.y
 								+ MathUtil.newYTurn(-width / 2, height / 2,
 										angle)), 3f, (Color) Color.GREY);
 
 		RenderUtil.drawLine(
 				new Vector2f(position.x
 						+ MathUtil.newXTurn(width / 2, -height / 2, angle),
 						position.y
 								+ MathUtil.newYTurn(width / 2, -height / 2,
 										angle)),
 				new Vector2f(position.x
 						+ MathUtil.newXTurn(width / 2, height / 2, angle),
 						position.y
 								+ MathUtil.newYTurn(width / 2, height / 2,
 										angle)), 3f, (Color) Color.GREY);
 	}
 
 	@Override
 	public void playSound() {
 	}
 
 	@Override
 	public void doAction(int code) {
 		// System.out.println("Ship.doAction()" + code);
 		switch (code) {
 		case ControlledObject.LEFT_ENGINE_ACTIVE: {
 			leftEngineActive = true;
 			break;
 		}
 		case ControlledObject.RIGHT_ENGINE_ACTIVE: {
 			rightEngineActive = true;
 			break;
 		}
 		case ControlledObject.All_ENGINE_ACTIVE: {
 			allEngineActive = true;
 			break;
 		}
 		case ControlledObject.FIRE_FIRST_WEAPON: {
 			weapon1Shot = true;
 			break;
 		}
 		case ControlledObject.FIRE_SECOND_WEAPON: {
 			weapon2Shot = true;
 			break;
 		}
 		case ControlledObject.TURN_ENGINES_LEFT: {
 			turnEnginesLeft = true;
 			break;
 		}
 		case ControlledObject.TURN_ENGINES_RIGHT: {
 			turnEnginesRight = true;
 			break;
 		}
 		}
 	}
 
 	@Override
 	public void clearFlags() {
 		allEngineActive = false;
 		leftEngineActive = false;
 		rightEngineActive = false;
 		weapon1Shot = false;
 		weapon2Shot = false;
 		turnEnginesLeft = false;
 		turnEnginesRight = false;
 	}
 
 	@Override
 	public void destroy() {
 		EmmiterEffects.drawBoom(position);
 		level.getWorld().destroyBody(body);
 	}
 
 	public void damage(float... impulses) {
 		float result = 0;
 		for (float f : impulses)
 			result += f;
 		liveHealth -= result * (1 - protection);
 	}
 }
