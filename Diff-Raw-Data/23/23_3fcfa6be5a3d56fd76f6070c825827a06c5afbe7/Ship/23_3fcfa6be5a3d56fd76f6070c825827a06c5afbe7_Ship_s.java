 package net.carmgate.morph.model.entities;
 
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import net.carmgate.morph.model.Model;
 import net.carmgate.morph.model.common.Vect3D;
 import net.carmgate.morph.model.entities.common.Entity;
 import net.carmgate.morph.model.entities.common.EntityHints;
 import net.carmgate.morph.model.entities.common.EntityType;
 import net.carmgate.morph.model.entities.orders.Order;
 import net.carmgate.morph.model.entities.orders.TakeDamageOrder;
 import net.carmgate.morph.model.player.Player;
 import net.carmgate.morph.model.player.Player.PlayerType;
 import net.carmgate.morph.ui.rendering.RenderingHints;
 import net.carmgate.morph.ui.rendering.RenderingSteps;
 
 import org.lwjgl.opengl.GL11;
 import org.newdawn.slick.opengl.Texture;
 import org.newdawn.slick.opengl.TextureImpl;
 import org.newdawn.slick.opengl.TextureLoader;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 @EntityHints(entityType = EntityType.SHIP)
 @RenderingHints(renderingStep = RenderingSteps.SHIP)
 public class Ship extends Entity {
 
 	public static class Arrive extends Movement {
 
 		protected Vect3D arriveTarget;
 
 		protected final Vect3D desiredVelocity = new Vect3D();
 		private float slowingDistance;
 		private final Vect3D speedOpposition = new Vect3D();
 		private final Vect3D targetOffset = new Vect3D();
 		private final Vect3D normalizedTargetOffset = new Vect3D();
 
 		protected Arrive(Ship ship) {
 			super(ship);
 		}
 
 		@Override
 		protected void clearMovementVariables() {
 			super.clearMovementVariables();
 			arriveTarget = null;
 			desiredVelocity.nullify();
 			steeringForce.nullify();
 			slowingDistance = 0;
 			speedOpposition.nullify();
 			targetOffset.nullify();
 			normalizedTargetOffset.nullify();
 		}
 
 		@Override
 		protected void run(float secondsSinceLastUpdate) {
 
 			// Get some ship variables (must be final)
 			final float mass = ship.getMass();
 
 			targetOffset.copy(arriveTarget).substract(pos).mult(0.9f);
 
 			normalizedTargetOffset.copy(targetOffset).normalize(1);
 			speedOpposition.copy(normalizedTargetOffset).rotate(90).mult(speed.prodVectOnZ(normalizedTargetOffset));
 
 			float cosSpeedToTO = 1;
 			if (speed.modulus() != 0) {
 				cosSpeedToTO = Math.abs(new Vect3D(speed).normalize(1).prodScal(normalizedTargetOffset));
 			}
 
 			// distance = length (target_offset)
 			float distance = targetOffset.modulus();
 
 			// Optimal slowing distance when cruising at MAX_SPEED before entering the slowing radius
 			// Optimal slowing distance is computed for debugging purposes only
 			slowingDistance = 0.00001f + (float) (Math.pow(speed.modulus(), 2) / (2 * MAX_FORCE / mass * cosSpeedToTO));
 
 			// Ramped speed is the optimal target speed modulus
 			float rampedSpeed = (float) Math.sqrt(2 * MAX_FORCE / mass * distance);
 			// clipped_speed clips the speed to max speed
 			float clippedSpeed = Math.min(rampedSpeed, MAX_SPEED);
 			// desired_velocity would be the optimal speed vector if we had unlimited thrust
 			desiredVelocity.copy(targetOffset).add(speedOpposition).mult(clippedSpeed / distance);
 
 			// steering_force is the force we will apply
 			for (int i = steeringForces.length - 1; i > 0; i--) {
 				steeringForces[i].copy(steeringForces[i - 1]);
 			}
 			steeringForces[0].copy(desiredVelocity).substract(speed);
 			float factor = 1.35f;
 			float sdmin = slowingDistance / factor;
 			float sdmax = slowingDistance;
 			float overdrive = 1.0f + speed.modulus() / MAX_SPEED;
 			if (distance > sdmax) {
 				steeringForces[0].truncate(MAX_FORCE / mass);
 			} else if (distance > sdmin) {
 				float stModulus = steeringForces[0].modulus();
 				steeringForces[0].normalize((distance - sdmin) / (sdmax - sdmin) * stModulus + (sdmax - distance)
 						/ (sdmax - sdmin) * MAX_FORCE / mass * overdrive);
 			} else {
 				steeringForces[0].normalize(MAX_FORCE / mass * overdrive);
 			}
 
 			steeringForce.nullify();
 			for (Vect3D steeringForce2 : steeringForces) {
 				steeringForce.add(new Vect3D(steeringForce2).mult(1f / steeringForces.length));
 			}
 
 			// steeringForce.nullify();
 			// for (Vect3D steeringForce2 : steeringForces) {
 			// steeringForce.add(steeringForce2);
 			// }
 			// steeringForce.mult(1f / steeringForces.length);
 
 			// the following code helps to have a nice effect if the eulierian integration is not precise enough
 			// if (slowingDistance > distance + 10) {
 			// steeringForce.mult(1.5f);
 			// }
 
 			applySteeringForce(secondsSinceLastUpdate);
 			rotateProperly(secondsSinceLastUpdate);
 
 			// stop condition
 			if (new Vect3D(arriveTarget).substract(pos).modulus() < 10 && speed.modulus() < 10) {
 				clearMovementVariables();
 			}
 
 			addTrail();
 		}
 
 		public void setArriveTarget(Ship targetShip) {
 			arriveTarget = targetShip.pos;
 		}
 
 		public void setArriveTarget(Vect3D target) {
 			arriveTarget = target;
 		}
 
 	}
 
 	public static class Combat {
 		private final Logger LOGGER = LoggerFactory.getLogger(Combat.class);
 
 		/** rate of fire (nb/ms). */
 		private static final float rateOfFire = 0.001f;
 
 		protected Ship target;
 		protected long timeOfLastAction;
 
 		protected void run(float secondsSinceLastUpdate) {
 			// TODO The damage amount taken from the target take into account the target's speed, distance and size.
 			// TODO The damage sent to the target should take into account current morphs' xp, level and type.
 			// TODO This should also be updated to cope with the improbable possibility that the refresh rate is insufficient to handle
 			// the orders one by one. (currentTs - timeOfLastAction / rateOfFire > 2)
 			if (timeOfLastAction == 0 || (Model.getModel().getCurrentTS() - timeOfLastAction) * rateOfFire > 1) {
 				target.fireOrder(new TakeDamageOrder(0.1f));
 				timeOfLastAction += 1 / rateOfFire;
 			}
 		}
 
 		public void setTarget(Ship target) {
 			LOGGER.debug(target.toString());
 			this.target = target;
 			timeOfLastAction = Model.getModel().getCurrentTS();
 		}
 	}
 
 	// TODO make several movement classes to implement the different behaviors instead of mixing them.
 	public static abstract class Movement {
 		protected final Vect3D[] steeringForces = new Vect3D[3];
 		protected final Vect3D steeringForce = new Vect3D();
 		protected final Vect3D pos;
 		protected final Vect3D accel;
 		protected final Vect3D speed;
 		protected final Ship ship;
 
 		protected Movement(Ship ship) {
 			pos = ship.getPos();
 			accel = ship.getAccel();
 			speed = ship.getSpeed();
 			this.ship = ship;
 
 			for (int i = 0; i < steeringForces.length; i++) {
 				steeringForces[i] = new Vect3D();
 			}
 		}
 
 		protected void addTrail() {
 			if (steeringForce.modulus() > MAX_FORCE * 0.002) {
 				Model.getModel()
 						.getParticleEngine()
 						.addParticle(new Vect3D(pos), new Vect3D().substract(new Vect3D(steeringForce).mult(2)), 1f, 1f / 32,
 								steeringForce.modulus() / (MAX_FORCE / ship.getMass()) * 0.2f, steeringForce.modulus() / (MAX_FORCE / ship.getMass()) * 1f);
 			}
 		}
 
 		protected void applySteeringForce(float secondsSinceLastUpdate) {
 			// acceleration = steering_force / mass
 			accel.copy(steeringForce);
 			// velocity = truncate (velocity + acceleration, max_speed)
 			speed.add(new Vect3D(accel).mult(secondsSinceLastUpdate)).truncate(MAX_SPEED);
 			// position = position + velocity
 			pos.add(new Vect3D(speed).mult(secondsSinceLastUpdate));
 		}
 
 		protected void clearMovementVariables() {
 			accel.nullify();
 			speed.nullify();
 		}
 
 		protected void rotateProperly(float secondsSinceLastUpdate) {
 			// Get some ship variables
 			final float heading = ship.getHeading();
 			final float mass = ship.getMass();
 
 			// rotate properly
 			float newHeading = new Vect3D(0, 1, 0).angleWith(steeringForce);
 			// heading = newHeading;
 			float angleDiff = (newHeading - heading + 360) % 360;
 			float maxAngleSpeed = MAX_ANGLE_SPEED_PER_MASS_UNIT / mass;
 			if (angleDiff < maxAngleSpeed * secondsSinceLastUpdate) {
 				ship.setHeading(newHeading);
 			} else if (angleDiff < 180) {
 				ship.setHeading(heading + maxAngleSpeed * Math.max(1, angleDiff / 180) * secondsSinceLastUpdate);
 			} else if (angleDiff >= 360 - maxAngleSpeed * secondsSinceLastUpdate) {
 				ship.setHeading(newHeading);
 			} else {
 				ship.setHeading(heading - maxAngleSpeed * Math.max(1, angleDiff / 180) * secondsSinceLastUpdate);
 			}
 		}
 
 		protected abstract void run(float secondsSinceLastUpdate);
 	}
 
 	public static class Wander extends Movement {
 
 		private float wanderFocusDistance;
 		private float wanderRadius;
 
 		private final Vect3D wanderTarget = new Vect3D();
 
 		public Wander(Ship ship) {
 			super(ship);
 		}
 
 		public float getWanderFocusDistance() {
 			return wanderFocusDistance;
 		}
 
 		@Override
 		protected void run(float secondsSinceLastUpdate) {
 			if (wanderRadius == 0) {
 				clearMovementVariables();
 				return;
 			}
 
 			// Update target within the given constraints
 			Vect3D wanderFocus = new Vect3D(pos).add(new Vect3D(0, 1, 0).rotate(ship.getHeading()).normalize(wanderFocusDistance + ship.getMass()));
 
 			// Determine a target at acceptable distance from the wander focus point
 			wanderTarget.x += Math.random() * 0.25f - 0.125f;
 			wanderTarget.y += Math.random() * 0.25f - 0.125f;
 			if (new Vect3D(wanderFocus).add(wanderTarget).distance(wanderFocus) > wanderRadius) {
 				wanderTarget.copy(Vect3D.NULL);
 			}
 
 			steeringForce.copy(new Vect3D(wanderFocus).substract(pos).add(wanderTarget)).truncate(MAX_FORCE / ship.getMass());
 
 			applySteeringForce(secondsSinceLastUpdate);
 			rotateProperly(secondsSinceLastUpdate);
 			addTrail();
 		}
 
 		public void setWanderFocusDistance(float wanderFocusDistance) {
 			this.wanderFocusDistance = wanderFocusDistance;
 		}
 
 		public void setWanderRadius(float wanderRadius) {
 			this.wanderRadius = wanderRadius;
 		}
 
 	}
 
 	public final Arrive arrive;
 	public final Wander wander;
 	public final Combat combat;
 
 	private static final int nbSegments = 200;
 	private static final double deltaAngle = (float) (2 * Math.PI / nbSegments);
 	private static final float cos = (float) Math.cos(deltaAngle);
 	private static final float sin = (float) Math.sin(deltaAngle);
 
 	private static final Logger LOGGER = LoggerFactory.getLogger(Ship.class);
 	// Management of the ship's ids.
 	private static Integer nextId = 1;
 
 	private final int id;
 
 	/** The texture under the morph image. */
 	private static Texture baseTexture;
 	private static Texture zoomedOutTexture;
 
 	/** The ship max speed. */
 	// IMPROVE All these values should depend on the ship's fitting.
 	private static final float MAX_SPEED = 1000;
 	private static final float MAX_FORCE = 3000f;
 	private static final float MAX_ANGLE_SPEED_PER_MASS_UNIT = 3600f;
 	private final List<Morph> morphs = new ArrayList<>();
 
 	/** The ship position in the world. */
 	protected final Vect3D pos = new Vect3D();
 
 	protected final Vect3D speed = new Vect3D();
 
 	/** The ship orientation in the world. */
 	protected float heading;
 
 	private final List<Order> orderList = new ArrayList<>();
 
 	private float mass = 10;
 
 	/** Timestamp of last time the ship's position was calculated. */
 	// IMPROVE We should move this in a class that can handle this behavior for any Updatable
 	private long lastUpdateTS;
 
 	private boolean selected;
 
 	protected final Vect3D accel = new Vect3D();
 
 	protected float secondsSinceLastUpdate;
 	private final Player player;
 
 	/***
 	 * Creates a new ship with position (0, 0, 0), mass = 10 assigned to player "self".
 	 */
 	public Ship() {
 		this(0, 0, 0, 0, 10, Model.getModel().getSelf());
 	}
 
 	public Ship(float x, float y, float z, float heading, float mass, Player player) {
 		this.player = player;
 		Model.getModel().getPlayers().add(player);
 
 		synchronized (nextId) {
 			id = nextId++;
 		}
 
 		pos.copy(x, y, z);
 		// speed.nullify();
 		// accel = new Vect3D();
 		// posAccel = new Vect3D(0, 0, 0);
 		this.heading = heading;
 		this.mass = mass;
 		// rotSpeed = 0;
 
 		// Init lastUpdateTS
 		lastUpdateTS = Model.getModel().getCurrentTS();
 
 		// init behaviors
 		// steering
 		arrive = new Arrive(this);
 		wander = new Wander(this);
 		// other
 		combat = new Combat();
 	}
 
 	public void fireOrder(Order order) {
 		orderList.add(order);
 	}
 
 	public Vect3D getAccel() {
 		return accel;
 	}
 
 	public float getHeading() {
 		return heading;
 	}
 
 	@Override
 	public int getId() {
 		return id;
 	}
 
 	public float getMass() {
 		return mass;
 	}
 
 	public List<Morph> getMorphs() {
 		return morphs;
 	}
 
 	public Vect3D getPos() {
 		return pos;
 	}
 
 	public Vect3D getSpeed() {
 		return speed;
 	}
 
 	/** List of ships IAs. */
 	// private final List<IA> iaList = new ArrayList<IA>();
 
 	@Override
 	public void initRenderer() {
 		// load texture from PNG file if needed
 		if (baseTexture == null) {
 			try (FileInputStream fileInputStream = new FileInputStream(ClassLoader.getSystemResource("spaceship.png").getPath())) {
 				baseTexture = TextureLoader.getTexture("PNG", fileInputStream);
 			} catch (IOException e) {
 				LOGGER.error("Exception raised while loading texture", e);
 			}
 		}
 
 		if (zoomedOutTexture == null) {
 			try (FileInputStream fileInputStream = new FileInputStream(ClassLoader.getSystemResource("spaceshipZoomedOut.png").getPath())) {
 				zoomedOutTexture = TextureLoader.getTexture("PNG", fileInputStream);
 			} catch (IOException e) {
 				LOGGER.error("Exception raised while loading texture", e);
 			}
 		}
 	}
 
 	private void processAI() {
 		// TODO Outsource this AI to allow several kinds of AIs
 		// Very simple AI : wander and attack
 
 	}
 
 	@Override
 	public void render(int glMode) {
 
 		GL11.glTranslatef(pos.x, pos.y, pos.z);
 		GL11.glRotatef(heading, 0, 0, 1);
 		float massScale = mass / 10;
 
 		// Render selection circle around the ship
 		boolean maxZoom = 64f * massScale * Model.getModel().getViewport().getZoomFactor() > 15;
 		if (selected) {
 			// render limit of effect zone
 			TextureImpl.bindNone();
 			float tInt = 0; // temporary data holder
 			float tExt = 0; // temporary data holder
 			float xInt;
 			float xExt;
 			if (maxZoom) {
 				xInt = 64 * massScale - 15; // radius
 				xExt = 64 * massScale - 15 + 6 / Model.getModel().getViewport().getZoomFactor(); // radius
 			} else {
 				xInt = 15f / Model.getModel().getViewport().getZoomFactor(); // radius
 				xExt = 21f / Model.getModel().getViewport().getZoomFactor(); // radius
 			}
 			float xIntBackup = xInt; // radius
 			float xExtBackup = xExt; // radius
 			float yInt = 0;
 			float yExt = 0;
 			float yIntBackup = 0;
 			float yExtBackup = 0;
 			float alphaMax = 1f;
 			for (int i = 0; i < nbSegments; i++) {
 
 				tInt = xInt;
 				tExt = xExt;
 				xInt = cos * xInt - sin * yInt;
 				xExt = cos * xExt - sin * yExt;
 				yInt = sin * tInt + cos * yInt;
 				yExt = sin * tExt + cos * yExt;
 
 				GL11.glBegin(GL11.GL_QUADS);
 				GL11.glColor4f(0, 0.7f, 0, 0);
 				GL11.glVertex2f(xInt, yInt);
 				GL11.glColor4f(0, 0.7f, 0, 0);
 				GL11.glVertex2f(xIntBackup, yIntBackup);
 				GL11.glColor4f(0, 0.7f, 0, alphaMax);
 				GL11.glVertex2f((xExtBackup + xIntBackup) / 2, (yExtBackup + yIntBackup) / 2);
 				GL11.glColor4f(0, 0.7f, 0, alphaMax);
 				GL11.glVertex2f((xExt + xInt) / 2, (yExt + yInt) / 2);
 				GL11.glColor4f(0, 0.7f, 0, alphaMax);
 				GL11.glVertex2f((xExtBackup + xIntBackup) / 2, (yExtBackup + yIntBackup) / 2);
 				GL11.glColor4f(0, 0.7f, 0, alphaMax);
 				GL11.glVertex2f((xExt + xInt) / 2, (yExt + yInt) / 2);
 				GL11.glColor4f(0, 0.7f, 0, 0);
 				GL11.glVertex2f(xExt, yExt);
 				GL11.glColor4f(0, 0.7f, 0, 0);
 				GL11.glVertex2f(xExtBackup, yExtBackup);
 				GL11.glEnd();
 
 				xIntBackup = xInt;
 				xExtBackup = xExt;
 				yIntBackup = yInt;
 				yExtBackup = yExt;
 			}
 		}
 
 		// Render for show
 		if (Model.getModel().isDebugMode()) {
 			// TODO replace this with some more proper mass rendering
 			float energyPercent = mass / 10;
 			if (energyPercent <= 0) {
 				GL11.glColor3f(0.1f, 0.1f, 0.1f);
 			} else {
 				GL11.glColor3f(1f - energyPercent, energyPercent, 0);
 			}
 		} else {
 			GL11.glColor3f(1f, 1f, 1f);
 		}
 		if (maxZoom) {
 			GL11.glScalef(massScale, massScale, 0);
 			baseTexture.bind();
 			GL11.glBegin(GL11.GL_QUADS);
 			GL11.glTexCoord2f(0, 0);
 			GL11.glVertex2f(-64, 64);
 			GL11.glTexCoord2f(1, 0);
 			GL11.glVertex2f(64, 64);
 			GL11.glTexCoord2f(1, 1);
 			GL11.glVertex2f(64, -64);
 			GL11.glTexCoord2f(0, 1);
 			GL11.glVertex2f(-64, -64);
 			GL11.glEnd();
 			GL11.glScalef(1 / massScale, 1 / massScale, 0);
 		} else {
 			float adjustedSize = 15 / Model.getModel().getViewport().getZoomFactor();
 			zoomedOutTexture.bind();
 			GL11.glBegin(GL11.GL_QUADS);
 			GL11.glTexCoord2f(0, 0);
 			GL11.glVertex2f(-adjustedSize, adjustedSize);
 			GL11.glTexCoord2f(1, 0);
 			GL11.glVertex2f(adjustedSize, adjustedSize);
 			GL11.glTexCoord2f(1, 1);
 			GL11.glVertex2f(adjustedSize, -adjustedSize);
 			GL11.glTexCoord2f(0, 1);
 			GL11.glVertex2f(-adjustedSize, -adjustedSize);
 			GL11.glEnd();
 		}
 
 		GL11.glRotatef(-heading, 0, 0, 1);
 
 		if (Model.getModel().isDebugMode()) {
 			speed.render(glMode, 1);
 			GL11.glColor3f(1, 0, 0);
 			arrive.desiredVelocity.render(glMode, 1);
 			GL11.glTranslated(arrive.desiredVelocity.x, arrive.desiredVelocity.y, 0);
 			GL11.glColor3f(0, 0, 1);
 			arrive.steeringForce.render(glMode, 1);
 			GL11.glTranslated(-arrive.desiredVelocity.x, -arrive.desiredVelocity.y, 0);
 			GL11.glColor3f(0, 1, 0);
 			arrive.speedOpposition.render(glMode, 1);
 		}
 
 		GL11.glTranslatef(-pos.x, -pos.y, -pos.z);
 
 		if (arrive.arriveTarget != null && selected && Model.getModel().isDebugMode()) {
 			// Show target
 			GL11.glTranslatef(arrive.arriveTarget.x, arrive.arriveTarget.y, 0);
 
 			TextureImpl.bindNone();
 			GL11.glBegin(GL11.GL_QUADS);
 			GL11.glVertex2f(-16, -16);
 			GL11.glVertex2f(16, -16);
 			GL11.glVertex2f(16, 16);
 			GL11.glVertex2f(-16, 16);
 			GL11.glEnd();
 
 			// render limit of effect zone
 			GL11.glBegin(GL11.GL_LINES);
 			float t = 0; // temporary data holder
 			float x = arrive.slowingDistance; // radius
 			float y = 0;
 			for (int i = 0; i < nbSegments; i++) {
 				GL11.glColor4d(1, 1, 1, 0.15);
 				GL11.glVertex2d(x, y);
 
 				t = x;
 				x = cos * x - sin * y;
 				y = sin * t + cos * y;
 
 				GL11.glVertex2d(x, y);
 			}
 			GL11.glEnd();
 			GL11.glTranslatef(-arrive.arriveTarget.x, -arrive.arriveTarget.y, 0);
 		}
 
 	}
 
 	public void setHeading(float heading) {
 		this.heading = heading;
 	}
 
 	public void setMass(float mass) {
 		this.mass = mass;
 	}
 
 	/**
 	 * Get the neighbours of the provided Morph.
 	 * Works in 2D only for now.
 	 * @param morph
 	 * @return
 	 */
 	// public List<Morph> getNeighbours(Morph morph) {
 	// List<Morph> neighbours = new ArrayList<Morph>();
 	// // 1 2
 	// // 3 4
 	// // 5 6
 	// neighbours.add(getShipMorph((int) morph.shipGridPos.x -1, (int) morph.shipGridPos.y + 1, (int) morph.shipGridPos.z));
 	// neighbours.add(getShipMorph((int) morph.shipGridPos.x, (int) morph.shipGridPos.y + 1, (int) morph.shipGridPos.z));
 	// neighbours.add(getShipMorph((int) morph.shipGridPos.x - 1, (int) morph.shipGridPos.y, (int) morph.shipGridPos.z));
 	// neighbours.add(getShipMorph((int) morph.shipGridPos.x + 1, (int) morph.shipGridPos.y, (int) morph.shipGridPos.z));
 	// neighbours.add(getShipMorph((int) morph.shipGridPos.x, (int) morph.shipGridPos.y - 1, (int) morph.shipGridPos.z));
 	// neighbours.add(getShipMorph((int) morph.shipGridPos.x + 1, (int) morph.shipGridPos.y - 1, (int) morph.shipGridPos.z));
 	// return neighbours;
 	// }
 
 	// public List<Morph> getSelectedMorphList() {
 	// return selectedMorphList;
 	// }
 
 	// public Morph getShipMorph(int x, int y, int z) {
 	// for (Morph m : morphList) {
 	// if (m.shipGridPos.x == x && m.shipGridPos.y == y && m.shipGridPos.z == z) {
 	// return m;
 	// }
 	// }
 	//
 	// return null;
 	// }
 
 	@Override
 	public void setSelected(boolean selected) {
 		this.selected = selected;
 	}
 
 	/**
 	 * Looks for the {@link Morph} at the specified position in the ship.
 	 * If there is no morph at the given position, it returns null.
 	 * @param pos the position in the ship
 	 * @return null if there is no morph at the specified location.
 	 */
 	// public Morph getShipMorph(Vect3D pos) {
 	// return getShipMorph((int) pos.x, (int) pos.y, (int) pos.z);
 	// }
 	//
 	// public void removeActiveMorph(Morph morph) {
 	// activeMorphList.remove(morph);
 	// }
 	//
 	// public void setSelectedMorph(int index) {
 	// selectedMorphList.clear();
 	// if (index >= 0 && index < morphList.size()) {
 	// selectedMorphList.add(morphList.get(index));
 	// }
 	// }
 	//
 	// public boolean toggleActiveMorph(Morph morph) {
 	// if (activeMorphList.contains(morph)) {
 	// activeMorphList.remove(morph);
 	// return false;
 	// }
 	//
 	// activeMorphList.add(morph);
 	// return true;
 	// }
 	//
 	// public void toggleSelectedMorph(int index) {
 	// Morph selectedMorph = morphList.get(index);
 	// if (selectedMorphList.contains(selectedMorph)) {
 	// selectedMorphList.remove(selectedMorph);
 	// } else {
 	// selectedMorphList.add(selectedMorph);
 	// }
 	// }
 
 	@Override
 	public String toString() {
 		return "ship:" + pos.toString();
 	}
 
 	@Override
 	public void update() {
		secondsSinceLastUpdate = ((float) Model.getModel().getCurrentTS() - lastUpdateTS) / 1000;
 		lastUpdateTS = Model.getModel().getCurrentTS();
 		if (secondsSinceLastUpdate == 0f) {
 			return;
 		}
 
 		// handle AI assignements if appropriate
 		if (player.getPlayerType() == PlayerType.AI) {
 			processAI();
 		}
 
 		// if no movement needed, no update needed
 		if (arrive.arriveTarget != null) {
 			arrive.run(secondsSinceLastUpdate);
 		}
 		if (wander.wanderFocusDistance != 0) {
 			wander.run(secondsSinceLastUpdate);
 		}
 
 		if (combat.target != null) {
 			combat.run(secondsSinceLastUpdate);
 		}
 
 		// TODO This should be improved to handle order in a generic fashion
 		for (Order order : orderList) {
 			if (order instanceof TakeDamageOrder) {
 				// This is not multiplied by lastUpdateTS because the timing is handled by the sender of the event.
 				// TODO Introduce the previous rule in the Order contract
 				mass -= ((TakeDamageOrder) order).getDamageAmount();
 				LOGGER.debug("Mass at " + mass + " for " + this);
 			}
 		}
 		orderList.clear();
 
 		// If the mass of the current ship is null or below, remove it
 		if (mass <= 0) {
 			Model.getModel().removeEntity(this);
 		}
 
 		// applyForces();
 
 		// logger.debug(posAccel.modulus());
 
 		// posSpeed.x += posAccel.x * secondsSinceLastUpdate;
 		// posSpeed.y += posAccel.y * secondsSinceLastUpdate;
 		// posSpeed.z += posAccel.z * secondsSinceLastUpdate;
 
 		// The drag factor is reduced to take into account the fact that we update the position since last TS and not from a full second ago.
 		// float reducedDragFactor = 1 - (1 - dragFactor) * secondsSinceLastUpdate;
 		// posSpeed.x = Math.abs(posSpeed.x * reducedDragFactor) > MIN_SPEED ? posSpeed.x * reducedDragFactor : 0;
 		// posSpeed.y = Math.abs(posSpeed.y * reducedDragFactor) > MIN_SPEED ? posSpeed.y * reducedDragFactor : 0;
 		// posSpeed.z = Math.abs(posSpeed.z * reducedDragFactor) > MIN_SPEED ? posSpeed.z * reducedDragFactor : 0;
 		//
 		// pos.x += posSpeed.x * secondsSinceLastUpdate;
 		// pos.y += posSpeed.y * secondsSinceLastUpdate;
 		// pos.z += posSpeed.z * secondsSinceLastUpdate;
 
 		// rotSpeed += rotAccel * secondsSinceLastUpdate;
 
 		// rotSpeed = Math.abs(rotSpeed * reducedDragFactor) > MIN_SPEED ? rotSpeed * reducedDragFactor : 0;
 		//
 		// rot = (rot + rotSpeed * secondsSinceLastUpdate) % 360;
 
 		// updateMorphs();
 	}
 	// private void updateMorphs() {
 	// for (Morph m : morphList) {
 	// // Position of the morph in the referential centered on the ship (the central one has coords (0, 0).
 	// // m.getPosInShip().x = m.shipGridPos.x * World.GRID_SIZE + m.shipGridPos.y * World.GRID_SIZE / 2;
 	// // m.getPosInShip().y = (float) (m.shipGridPos.y * World.GRID_SIZE * Math.sqrt(3)/2);
 	//
 	// // Adding the rotation around the center of the ship
 	// // m.getPosInShip().rotate(rot);
 	//
 	// // Adding the position of the ship's inertia center
 	// // m.getPosInShip().add(pos);
 	//
 	// // Disabling if necessary
 	// if (m.mass < m.disableMass && !m.disabled) {
 	// logger.debug("Disabling morph");
 	// m.disabled = true;
 	// }
 	//
 	// // Reenable the morph if possible
 	// if (m.mass >= m.reenableMass && m.disabled && m.energy > 0) {
 	// m.disabled = false;
 	// }
 	//
 	// // Regaining mass if disabled
 	// if (m.disabled && m.mass < m.maxMass) {
 	// m.mass += 0.1;
 	// }
 	// }
 	// }
 }
