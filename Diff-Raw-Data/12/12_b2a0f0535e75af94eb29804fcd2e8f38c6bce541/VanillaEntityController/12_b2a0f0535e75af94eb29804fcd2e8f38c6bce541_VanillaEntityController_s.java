 /*
  * This file is part of Vanilla.
  *
  * Copyright (c) 2011-2012, VanillaDev <http://www.spout.org/>
  * Vanilla is licensed under the SpoutDev License Version 1.
  *
  * Vanilla is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * In addition, 180 days after any changes are published, you can use the
  * software, incorporating those changes, under the terms of the MIT license,
  * as described in the SpoutDev License Version 1.
  *
  * Vanilla is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License,
  * the MIT license and the SpoutDev License Version 1 along with this program.
  * If not, see <http://www.gnu.org/licenses/> for the GNU Lesser General Public
  * License and see <http://www.spout.org/SpoutDevLicenseV1.txt> for the full license,
  * including the MIT license.
  */
 package org.spout.vanilla.controller;
 
 import java.util.HashSet;
 import java.util.Random;
 import java.util.Set;
 import java.util.logging.Level;
 
 import org.spout.api.Source;
 import org.spout.api.Spout;
 import org.spout.api.collision.BoundingBox;
 import org.spout.api.collision.CollisionModel;
 import org.spout.api.collision.CollisionStrategy;
 import org.spout.api.entity.Entity;
 import org.spout.api.entity.component.controller.BasicController;
 import org.spout.api.entity.component.controller.PlayerController;
 import org.spout.api.event.entity.EntityHealthChangeEvent;
 import org.spout.api.geo.cuboid.Block;
 import org.spout.api.geo.discrete.Transform;
 import org.spout.api.inventory.ItemStack;
 import org.spout.api.math.MathHelper;
 import org.spout.api.math.Quaternion;
 import org.spout.api.math.Vector2;
 import org.spout.api.math.Vector3;
 
 import org.spout.vanilla.controller.object.moving.Item;
 import org.spout.vanilla.controller.source.DamageCause;
 import org.spout.vanilla.controller.source.HealthChangeReason;
 import org.spout.vanilla.data.VanillaData;
 import org.spout.vanilla.event.entity.EntityCombustEvent;
 import org.spout.vanilla.protocol.msg.entity.EntityAnimationMessage;
 import org.spout.vanilla.protocol.msg.entity.EntityStatusMessage;
 import org.spout.vanilla.util.VanillaNetworkUtil;
 
 import static org.spout.vanilla.util.VanillaNetworkUtil.broadcastPacket;
 
 /**
  * Controller that is the parent of all entity controllers.
  */
 public abstract class VanillaEntityController extends BasicController implements VanillaController {
 	private final VanillaControllerType type;
 	private final BoundingBox area = new BoundingBox(-0.3F, 0F, -0.3F, 0.3F, 0.8F, 0.3F);
 	private static Random rand = new Random();
 	// Protocol: last known updated client transform
 	private Transform lastClientTransform = new Transform();
 	// Controller flags
 	private boolean isFlammable = true;
 	private boolean hasDeathAnimation = false;
 	// Tick effects
 	private int fireTicks = 0;
 	private int positionTicks = 0;
 	private int velocityTicks = 0;
 	private int airTicks = 0;
 	// Velocity-related
 	private Vector3 velocity = Vector3.ZERO;
 	private Vector3 movementSpeed = Vector3.ZERO;
 	private Vector3 maxSpeed = new Vector3(0.4, 0.4, 0.4);
 	// Controller characteristics
 	private int health = 1;
 	private int maxHealth = 1;
 	private int deathTicks = -1;
 	// Damage
 	private Source lastDamage = DamageCause.UNKNOWN;
 	private VanillaEntityController lastDamager;
 
 	protected VanillaEntityController(VanillaControllerType type) {
 		super(type);
 		this.type = type;
 	}
 
 	@Override
 	public void onAttached() {
 		getParent().setCollision(new CollisionModel(area));
 		getParent().getCollision().setStrategy(CollisionStrategy.SOLID);
 		data().put(VanillaData.CONTROLLER_TYPE, getType().getMinecraftId());
 		this.lastClientTransform = getParent().getTransform();
 
 		// Load data
 		airTicks = data().get(VanillaData.AIR_TICKS);
 		fireTicks = data().get(VanillaData.FIRE_TICKS);
 		health = data().get(VanillaData.HEALTH);
 		isFlammable = data().get(VanillaData.FLAMMABLE);
 		maxHealth = data().get(VanillaData.MAX_HEALTH);
 		maxSpeed = data().get(VanillaData.MAX_SPEED, maxSpeed);
 		movementSpeed = data().get(VanillaData.MOVEMENT_SPEED, movementSpeed);
 		if (data().containsKey(VanillaData.VELOCITY)) {
 			velocity = data().get(VanillaData.VELOCITY);
 		}
 	}
 
 	@Override
 	public void onSave() {
 		// Load data
 		data().put(VanillaData.AIR_TICKS, airTicks);
 		data().put(VanillaData.FIRE_TICKS, fireTicks);
 		data().put(VanillaData.HEALTH, health);
 		data().put(VanillaData.FLAMMABLE, isFlammable);
 		data().put(VanillaData.MAX_HEALTH, maxHealth);
 		data().put(VanillaData.MAX_SPEED, maxSpeed);
 		data().put(VanillaData.MOVEMENT_SPEED, movementSpeed);
 		data().put(VanillaData.VELOCITY, velocity);
 	}
 
 	@Override
 	public void onTick(float dt) {
 		if (deathTicks > 0) {
 			deathTicks--;
 			if (deathTicks == 0) {
 				if (this instanceof PlayerController) {
 					deathTicks = 30;
 				} else {
 					getParent().kill();
 				}
 			}
 			return;
 		}
 		updateFireTicks();
 		updateAirTicks();
 
 		// Check controller health, send messages to the client based on current state.
 		if (health <= 0) {
 			if (!hasDeathAnimation()) {
 				getParent().kill();
 			} else {
 				VanillaNetworkUtil.broadcastPacket(new EntityStatusMessage(getParent().getId(), EntityStatusMessage.ENTITY_DEAD));
 				deathTicks = 30;
 			}
 
 			onDeath();
 		}
 
 		positionTicks++;
 		velocityTicks++;
 
 		super.onTick(dt);
 	}
 
 	@Override
 	public void onCollide(Block block) {
 		setVelocity(Vector3.ZERO);
 	}
 
 	@Override
 	public void onCollide(Entity entity) {
 		// push entities apart
 		// TODO: Ignore if this entity is a passenger?
 		Vector2 diff = entity.getPosition().subtract(this.getParent().getPosition()).toVector2();
 		float distance = diff.length();
 		if (distance > 0.1f) {
 			double factor = Math.min(1f / distance, 1f) / distance * 0.05;
 			diff = diff.multiply(factor);
 			setVelocity(getVelocity().add(diff.toVector3()));
 		}
 	}
 
 	@Override
 	public void onDeath() {
 		for (ItemStack drop : getDrops(lastDamage, lastDamager)) {
 			if (drop == null) {
 				continue;
 			}
 			Item item = new Item(drop, Vector3.ZERO);
 			getParent().getLastTransform().getPosition().getWorld().createAndSpawnEntity(getParent().getLastTransform().getPosition(), item);
 			// TODO: Drop experience
 		}
 	}
 
 	@Override
 	public VanillaControllerType getType() {
 		return type;
 	}
 
 	/**
 	 * Sets the last known transformation known by the clients<br>
 	 * This should only be called by the protocol classes
 	 * @param transform to set to
 	 */
 	public void setLastClientTransform(Transform transform) {
 		this.lastClientTransform = transform.copy();
 	}
 
 	/**
 	 * Gets the last known transformation updated to the clients
 	 * @return the last known transform by the clients
 	 */
 	public Transform getLastClientTransform() {
 		return this.lastClientTransform;
 	}
 
 	/**
 	 * Gets the bounding box for the entity that this controller supplies
 	 * @return the bounding box for the entity
 	 */
 	public BoundingBox getBounds() {
 		return this.area;
 	}
 
 	/**
 	 * Tests if a velocity update is needed for this entity<br>
 	 * This is called by the entity protocol
 	 * @return True if a velocity update is needed
 	 */
 	public boolean needsVelocityUpdate() {
 		return velocityTicks % 5 == 0;
 	}
 
 	/**
 	 * Tests if a position update is needed for this entity<br>
 	 * This is called by the entity protocol
 	 * @return True if a position update is needed
 	 */
 	public boolean needsPositionUpdate() {
 		return positionTicks % 30 == 0;
 	}
 
 	/**
 	 * Gets the current velocity of this controller
 	 * @return the velocity
 	 */
 	public Vector3 getVelocity() {
 		return velocity;
 	}
 
 	/**
 	 * Sets the current velocity for this controller
 	 * @param velocity to set to
 	 */
 	public void setVelocity(Vector3 velocity) {
 		if (velocity == null) {
 			if (Spout.debugMode()) {
 				Spout.getLogger().log(Level.SEVERE, "Velocity of " + this.toString() + " set to null!");
 				Spout.getLogger().log(Level.SEVERE, "Report this to http://issues.spout.org");
 			}
 			velocity = Vector3.ZERO;
 		}
 		this.velocity = velocity;
 	}
 
 	/**
 	 * Gets the speed of the controller during the prior movement. This will always be lower than the maximum speed.
 	 * @return the moved velocity
 	 */
 	public Vector3 getMovementSpeed() {
 		return movementSpeed;
 	}
 
 	/**
 	 * Gets the maximum speed this controller is allowed to move at once.
 	 * @return the maximum velocity
 	 */
 	public Vector3 getMaxSpeed() {
 		return maxSpeed;
 	}
 
 	/**
 	 * Sets the maximum speed this controller is allowed to move at once.
 	 * @param maxSpeed to set to
 	 */
 	public void setMaxSpeed(Vector3 maxSpeed) {
 		this.maxSpeed = maxSpeed;
 	}
 
 	/**
 	 * Get the drops that Vanilla controllers disperse into the world when un-attached (such as entity death). Children controllers should override this method for their own personal drops.
 	 * @param source Source of death
 	 * @param lastDamager Controller that killed this controller, can be null if death was caused by natural sources such as drowning or burning
 	 * @return the drops to disperse.
 	 */
 	public Set<ItemStack> getDrops(Source source, VanillaEntityController lastDamager) {
 		return new HashSet<ItemStack>();
 	}
 
 	/**
 	 * Checks to see if the controller is combustible.
 	 * @return true is combustible, false if not
 	 */
 	public boolean isFlammable() {
 		return isFlammable;
 	}
 
 	/**
 	 * Sets if the controller is combustible or not.
 	 * @param isFlammable flag representing combustible status.
 	 */
 	public void setFlammable(boolean isFlammable) {
 		this.isFlammable = isFlammable;
 	}
 
 	/**
 	 * Gets the amount of ticks the controller has been on fire.
 	 * @return amount of ticks
 	 */
 	public int getFireTicks() {
 		return fireTicks;
 	}
 
 	/**
 	 * Sets the amount of ticks the controller has been on fire.
 	 * @param fireTicks the new amount of ticks the controller has been on fire for.
 	 */
 	public void setFireTicks(int fireTicks) {
 		if (fireTicks > 0) {
 			EntityCombustEvent event = Spout.getEventManager().callEvent(new EntityCombustEvent(getParent(), fireTicks));
 			fireTicks = event.getDuration();
 		}
 		this.fireTicks = fireTicks;
 	}
 
 	private void updateFireTicks() {
 		if (fireTicks > 0) {
 			if (!isFlammable) {
 				fireTicks -= 4;
 				if (fireTicks < 0) {
 					fireTicks = 0;
 				}
 				return;
 			}
 
 			if (fireTicks % 20 == 0) {
 				damage(1, DamageCause.FIRE_CONTACT);
 			}
 
 			--fireTicks;
 		}
 	}
 
 	// =========================
 	// Controller helper methods
 	// =========================
 
 	/**
 	 * Damages this controller with the given {@link DamageCause}.
 	 * @param amount amount the controller will be damaged by, can be modified based on armor and enchantments
 	 */
 	public void damage(int amount) {
 		damage(amount, DamageCause.UNKNOWN);
 	}
 
 	/**
 	 * Damages this controller with the given {@link DamageCause}.
 	 * @param amount amount the controller will be damaged by, can be modified based on armor and enchantments
 	 * @param cause cause of this controller being damaged
 	 */
 	public void damage(int amount, DamageCause cause) {
 		damage(amount, cause, true);
 	}
 
 	/**
 	 * Damages this controller with the given {@link DamageCause}.
 	 * @param amount amount the controller will be damaged by, can be modified based on armor and enchantments
 	 * @param cause cause of this controller being damaged
 	 * @param sendHurtMessage whether to send the hurt packet to all players online
 	 */
 	public void damage(int amount, DamageCause cause, boolean sendHurtMessage) {
 		damage(amount, cause, null, sendHurtMessage);
 	}
 
 	/**
 	 * Damages this controller with the given {@link DamageCause} and damager.
 	 * @param amount amount the controller will be damaged by, can be modified based on armor and enchantments
 	 * @param cause cause of this controller being damaged
 	 * @param damager controller that damaged this controller
 	 * @param sendHurtMessage whether to send the hurt packet to all players online
 	 */
 	public void damage(int amount, DamageCause cause, VanillaEntityController damager, boolean sendHurtMessage) {
 		// TODO take potion effects into account
 		setHealth(getHealth() - amount, HealthChangeReason.DAMAGE);
 		lastDamager = damager;
 		if (sendHurtMessage) {
 			broadcastPacket(new EntityAnimationMessage(this.getParent().getId(), EntityAnimationMessage.ANIMATION_HURT), new EntityStatusMessage(this.getParent().getId(), EntityStatusMessage.ENTITY_HURT));
 		}
 	}
 
 	/**
 	 * Moves this controller.
 	 * @param vect the vector that is applied as the movement.
 	 */
 	public void move(Vector3 vect) {
 		getParent().translate(vect);
 	}
 
 	/**
 	 * Uses the current velocity and maximum speed to move this entity.
 	 */
 	public void move() {
 		movementSpeed = MathHelper.min(velocity, maxSpeed);
 		move(movementSpeed);
 	}
 
 	/**
 	 * Moves this controller
 	 * @param x x-axis to move the controller along
 	 * @param y y-axis to move the controller along
 	 * @param z z-axis to move the controller along
 	 */
 	public void move(float x, float y, float z) {
 		move(new Vector3(x, y, z));
 	}
 
 	/**
 	 * Rotates the controller
 	 * @param rot the quaternion that is applied as the rotation.
 	 */
 	public void rotate(Quaternion rot) {
 		getParent().rotate(rot);
 	}
 
 	/**
 	 * Rotates the controller
 	 * @param degrees the angle of which to do rotation.
 	 * @param x x-axis to rotate the controller along
 	 * @param y y-axis to rotate the controller along
 	 * @param z z-axis to rotate the controller along
 	 */
 	public void rotate(float degrees, float x, float y, float z) {
 		getParent().rotate(degrees, x, y, z);
 	}
 
 	/**
 	 * Sets the yaw angle for this controller
 	 * @param angle to set to
 	 */
 	public void yaw(float angle) {
 		getParent().yaw(angle);
 	}
 
 	/**
 	 * Sets the pitch angle for this controller
 	 * @param angle to set to
 	 */
 	public void pitch(float angle) {
 		getParent().pitch(angle);
 	}
 
 	/**
 	 * Rolls this controller along an angle.
 	 * @param angle the angle in-which to roll
 	 */
 	public void roll(float angle) {
 		getParent().roll(angle);
 	}
 
 	/**
 	 * Gets the position of this controller at the start of this tick
 	 * @return previous position
 	 */
 	public Vector3 getPreviousPosition() {
 		return getParent().getLastTransform().getPosition();
 	}
 
 	/**
 	 * Gets the rotation of this controller at the start of this tick
 	 * @return previous rotation
 	 */
 	public Quaternion getPreviousRotation() {
 		return getParent().getLastTransform().getRotation();
 	}
 
 	/**
 	 * If a child controller needs a random number for anything, they should call this method. This eliminates needless random objects created all the time.
 	 * @return random object.
 	 */
 	public Random getRandom() {
 		return rand;
 	}
 
 	/**
 	 * Gets the health of this controller (hitpoints)
 	 * @return the health value
 	 */
 	public int getHealth() {
 		return health;
 	}
 
 	/**
 	 * Gets the maximum health this controller can have
 	 * @return the maximum health
 	 */
 	public int getMaxHealth() {
 		return maxHealth;
 	}
 
 	/**
 	 * Sets the current health value for this controller
 	 * @param health hitpoints value to set to
 	 * @param source of the change
 	 */
 	public void setHealth(int health, Source source) {
 		EntityHealthChangeEvent event = new EntityHealthChangeEvent(getParent(), source, health-this.health);
 		Spout.getEngine().getEventManager().callEvent(event);
 		if (!event.isCancelled()) {
			if (health + event.getChange() > maxHealth) {
 				this.health = maxHealth;
 			} else {
				this.health = health + event.getChange();
 			}
 		}
 	}
 
 	/**
 	 * Sets the maximum health this controller can have
 	 * @param maxHealth to set to
 	 */
 	public void setMaxHealth(int maxHealth) {
 		this.maxHealth = maxHealth;
 	}
 
 	public boolean hasDeathAnimation() {
 		return hasDeathAnimation;
 	}
 
 	public void setDeathAnimation(boolean hasDeathAnimation) {
 		this.hasDeathAnimation = hasDeathAnimation;
 	}
 
 	public final int getAirTicks() {
 		return airTicks;
 	}
 
 	public final void setAirTicks(int ticks) {
 		this.airTicks = ticks;
 	}
 
 	public void updateAirTicks() {
 
 	}
 
 	public int getMaxAirTicks() {
 		return 300;
 	}
 }
