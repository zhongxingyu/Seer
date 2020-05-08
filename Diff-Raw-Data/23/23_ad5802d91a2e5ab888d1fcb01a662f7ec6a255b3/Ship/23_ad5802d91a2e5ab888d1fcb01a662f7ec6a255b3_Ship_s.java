 package net.carmgate.morph.model.entities;
 
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import net.carmgate.morph.model.Model;
 import net.carmgate.morph.model.common.Vect3D;
 import net.carmgate.morph.model.entities.orders.Order;
 import net.carmgate.morph.model.entities.orders.TakeDamageOrder;
 import net.carmgate.morph.ui.rendering.RenderingHints;
 import net.carmgate.morph.ui.rendering.RenderingSteps;
 
 import org.lwjgl.opengl.GL11;
 import org.newdawn.slick.opengl.Texture;
 import org.newdawn.slick.opengl.TextureLoader;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 @EntityHints(entityType = EntityType.SHIP)
 @RenderingHints(renderingStep = RenderingSteps.SHIP)
 public class Ship extends Entity {
 
 	public class Combat {
 		private final Logger LOGGER = LoggerFactory.getLogger(Combat.class);
 
 		/** rate of fire (nb/ms). */
 		private static final float rateOfFire = 0.001f;
 
 		protected Ship target;
 		protected long timeOfLastAction;
 
 		protected void doCombat() {
 			// TODO The damage amount taken from the target take into account the target's speed, distance and size.
 			// TODO The damage sent to the target should take into account current morphs' xp, level and type.
 			// TODO This should also be updated to cope with the improbable possibility that the refresh rate is insufficient to handle
 			// the orders one by one. (currentTs - timeOfLastAction / rateOfFire > 2)
 			if (timeOfLastAction == 0 || (Model.getModel().getCurrentTS() - timeOfLastAction) * rateOfFire > 1) {
 				target.fireOrder(new TakeDamageOrder(1));
 				timeOfLastAction += 1 / rateOfFire;
 			}
 		}
 
 		public void setTarget(Ship target) {
 			LOGGER.debug(target.toString());
 			this.target = target;
 			timeOfLastAction = Model.getModel().getCurrentTS();
 		}
 	}
 
 	public class Movement {
 		protected Vect3D target;
 		protected Vect3D desiredVelocity = new Vect3D();
 		protected Vect3D steeringDirection = new Vect3D();
 		protected Vect3D steeringForce = new Vect3D();
 
 		protected Movement() {
 		}
 
 		protected void arrive() {
 			// LOGGER.debug("Moving " + this);
 
 			// new calculations (from http://www.red3d.com/cwr/steer/gdc99/) for arrival
 			// target_offset = target - position
 			Vect3D targetOffset = new Vect3D(target).substract(pos);
 			Vect3D normalizedTargetOffset = new Vect3D(targetOffset).normalize(1);
 			// distance = length (target_offset)
 			float distance = targetOffset.modulus();
 
 			// Optimal slowing distance when cruising at MAX_SPEED before entering the slowing radius
			float slowingDistance = 0.00001f + (float) (Math.pow(speed.modulus(), 2) / (2 * MAX_FORCE / mass));
 			LOGGER.debug("distance: " + distance + ", slowingDistance: " + slowingDistance);
 
 			// TEST
 			// Lorsqu'on entre dans le slowing radius, on doit avoir speed et desired speed alignées.
 			if (distance > slowingDistance) {
 				distance = distance - slowingDistance;
 			}
 
 			// ramped_speed = max_speed * (distance / slowing_distance)
 			float rampedSpeed = (float) Math.sqrt(2 * MAX_FORCE / mass * distance);// MAX_SPEED;
 			// clipped_speed = minimum (ramped_speed, max_speed)
 			float clippedSpeed = Math.min(rampedSpeed, MAX_SPEED);
 			// desired_velocity = (clipped_speed / distance) * target_offset
 			desiredVelocity = new Vect3D(targetOffset).mult(clippedSpeed / distance);
 			// steering = desired_velocity - velocity
 			steeringDirection = new Vect3D(desiredVelocity).substract(speed);
 			// steering_force = truncate (steering_direction, max_force)
 			Vect3D bigSteeringForce = new Vect3D(steeringDirection).normalize(MAX_FORCE / mass);
 			Vect3D smallSteeringForce = new Vect3D(steeringDirection).truncate(MAX_FORCE / mass);
 			// if (smallSteeringForce.modulus() > MAX_FORCE / 500) {
 			steeringForce = new Vect3D(bigSteeringForce);
 			// } else {
 			// steeringForce = new Vect3D(smallSteeringForce);
 			// }
 			// acceleration = steering_force / mass
			accel = new Vect3D(steeringDirection);
 			// velocity = truncate (velocity + acceleration, max_speed)
 			speed.add(new Vect3D(accel).mult(secondsSinceLastUpdate)).truncate(MAX_SPEED);
 			// position = position + velocity
 			pos.add(new Vect3D(speed).mult(secondsSinceLastUpdate));
 
 			// rotate properly
 			float newHeading = new Vect3D(0, 1, 0).angleWith(steeringForce);
 			// heading = newHeading;
 			float angleDiff = (newHeading - heading + 360) % 360;
 			if (angleDiff < MAX_ANGLE_SPEED * secondsSinceLastUpdate) {
 				heading = newHeading;
 			} else if (angleDiff < 180) {
 				heading += MAX_ANGLE_SPEED * Math.max(1, angleDiff / 180) * secondsSinceLastUpdate;
 			} else if (angleDiff >= 360 - MAX_ANGLE_SPEED * secondsSinceLastUpdate) {
 				heading = newHeading;
 			} else {
 				heading -= MAX_ANGLE_SPEED * Math.max(1, angleDiff / 180) * secondsSinceLastUpdate;
 			}
 
 			// stop condition
 			if (new Vect3D(target).substract(pos).modulus() < 10 && speed.modulus() < 10) {
 				target = null;
 				accel.nullify();
 				speed.nullify();
 				desiredVelocity.nullify();
 				steeringDirection.nullify();
 				steeringForce.nullify();
 			}
 
 			// TODO remove this once the particle engine is used somewhere else
 			// TODO Find something to draw to show the engine is active.
 			if (steeringForce.modulus() > MAX_FORCE * 0.02) {
 				Model.getModel().getParticleEngine().addParticle(new Vect3D(pos), new Vect3D().substract(new Vect3D(steeringForce).mult(1.5f)), 3);
 			}
 
 		}
 		public void setTarget(Vect3D target) {
 			this.target = target;
 		}
 
 	}
 
 	public final Movement movement = new Movement();
 	public final Combat combat = new Combat();
 
 	private static final Logger LOGGER = LoggerFactory.getLogger(Ship.class);
 	// Management of the ship's ids.
 	private static Integer nextId = 1;
 
 	private final int id;
 
 	/** The texture under the morph image. */
 	private static Texture baseTexture;
 
 	/** The ship max speed. */
 	// TODO All these values should depend on the ship's fitting.
 	private static final float MAX_SPEED = 1000;
 	private static final float MAX_FORCE = 3000f;
 	private static final float MAX_ANGLE_SPEED = 200f;
 	private final List<Morph> morphs = new ArrayList<>();
 
 	/** The ship position in the world. */
 	protected final Vect3D pos;
 
 	protected final Vect3D speed;
 
 	/** The ship orientation in the world. */
 	protected float heading;
 
 	private final List<Order> orderList = new ArrayList<>();
 
 	private float mass = 10;
 
 	/** Timestamp of last time the ship's position was calculated. */
 	// TODO We should move this in a class that can handle this behavior for any Updatable
 	private long lastUpdateTS;
 
 	private boolean selected;
 
 	protected Vect3D accel;
 
 	protected float secondsSinceLastUpdate;
 
 	public Ship() {
 		this(0, 0, 0, 0, 10);
 	}
 
 	public Ship(float x, float y, float z, float heading, float mass) {
 		synchronized (nextId) {
 			id = nextId++;
 		}
 
 		pos = new Vect3D(x, y, z);
 		speed = new Vect3D(0, 0, 0);
 		accel = new Vect3D();
 		// posAccel = new Vect3D(0, 0, 0);
 		this.heading = heading;
 		this.mass = mass;
 		// rotSpeed = 0;
 
 		// Init lastUpdateTS
 		lastUpdateTS = Model.getModel().getCurrentTS();
 	}
 
 	public void fireOrder(Order order) {
 		orderList.add(order);
 	}
 
 	public List<Morph> getMorphs() {
 		return morphs;
 	}
 
 	@Override
 	public int getSelectionId() {
 		return id;
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
 
 	}
 
 	@Override
 	public void render(int glMode) {
 
 		GL11.glTranslatef(pos.x, pos.y, pos.z);
 		GL11.glRotatef(heading, 0, 0, 1);
 		float massScale = mass / 10;
 		GL11.glScalef(massScale, massScale, 0);
 
 		// Render for show
 		if (Model.getModel().isDebugMode()) {
 			// TODO replace this with some more proper mass rendering
 			float energyPercent = mass / 10;
 			if (energyPercent <= 0) {
 				GL11.glColor3f(0.1f, 0.1f, 0.1f);
 			} else {
 				GL11.glColor3f(1f - energyPercent, energyPercent, 0);
 			}
 		} else if (selected) {
 			GL11.glColor3f(1f, 1f, 1f);
 		} else {
 			GL11.glColor3f(0.5f, 0.5f, 0.5f);
 		}
 		GL11.glEnable(GL11.GL_TEXTURE_2D);
 		baseTexture.bind();
 		GL11.glBegin(GL11.GL_QUADS);
 		GL11.glTexCoord2f(0, 0);
 		GL11.glVertex2f(-baseTexture.getTextureWidth() / 2, -baseTexture.getTextureWidth() / 2);
 		GL11.glTexCoord2f(1, 0);
 		GL11.glVertex2f(baseTexture.getTextureWidth() / 2, -baseTexture.getTextureWidth() / 2);
 		GL11.glTexCoord2f(1, 1);
 		GL11.glVertex2f(baseTexture.getTextureWidth() / 2, baseTexture.getTextureHeight() / 2);
 		GL11.glTexCoord2f(0, 1);
 		GL11.glVertex2f(-baseTexture.getTextureWidth() / 2, baseTexture.getTextureHeight() / 2);
 		GL11.glEnd();
 
 		GL11.glScalef(1 / massScale, 1 / massScale, 0);
 		GL11.glRotatef(-heading, 0, 0, 1);
 
 		if (Model.getModel().isDebugMode()) {
 			speed.render(glMode, 1);
 			GL11.glColor3f(1, 0, 0);
 			movement.desiredVelocity.render(glMode, 1);
 			GL11.glTranslated(movement.desiredVelocity.x, movement.desiredVelocity.y, 0);
 			GL11.glColor3f(0, 0, 1);
 			movement.steeringForce.render(glMode, 1);
 			GL11.glTranslated(-movement.desiredVelocity.x, -movement.desiredVelocity.y, 0);
 		}
 
 		GL11.glTranslatef(-pos.x, -pos.y, -pos.z);
 
 		if (movement.target != null) {
 			// Add new particle
 
 			if (selected) {
 				// Show target
 				GL11.glTranslatef(movement.target.x, movement.target.y, 0);
 
 				GL11.glDisable(GL11.GL_TEXTURE_2D);
 				GL11.glBegin(GL11.GL_QUADS);
 				GL11.glVertex2f(-16, -16);
 				GL11.glVertex2f(16, -16);
 				GL11.glVertex2f(16, 16);
 				GL11.glVertex2f(-16, 16);
 				GL11.glEnd();
 
 				GL11.glTranslatef(-movement.target.x, -movement.target.y, 0);
 			}
 		}
 
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
 
 		// if no movement needed, no update needed
 		if (movement.target != null) {
 			movement.arrive();
 		}
 
 		if (combat.target != null) {
 			combat.doCombat();
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
