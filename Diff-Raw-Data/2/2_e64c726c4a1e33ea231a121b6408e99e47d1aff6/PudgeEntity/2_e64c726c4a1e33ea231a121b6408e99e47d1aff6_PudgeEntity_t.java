 package pudgewars.entities;
 
 import java.awt.Image;
 import java.awt.Shape;
 import java.awt.geom.AffineTransform;
 import java.awt.geom.Ellipse2D;
 import java.awt.image.BufferedImage;
 import java.util.List;
 import java.util.Random;
 
 import pudgewars.Game;
 import pudgewars.Window;
 import pudgewars.components.Stats;
 import pudgewars.entities.hooks.BurnerHookEntity;
 import pudgewars.entities.hooks.GrappleHookEntity;
 import pudgewars.entities.hooks.HookEntity;
 import pudgewars.entities.hooks.HookType;
 import pudgewars.entities.hooks.NormalHookEntity;
 import pudgewars.interfaces.BBOwner;
 import pudgewars.level.Tile;
 import pudgewars.particles.ParticleTypes;
 import pudgewars.particles.VelocityParticle;
 import pudgewars.render.ArcImage;
 import pudgewars.sfx.SoundEffect;
 import pudgewars.util.Animation;
 import pudgewars.util.CollisionBox;
 import pudgewars.util.ImageHandler;
 import pudgewars.util.Time;
 import pudgewars.util.Vector2;
 
 public class PudgeEntity extends HookableEntity implements LightSource {
 	public final static int CLICK_SIZE = 8;
 	public final static int MAX_LIFE = 20;
 
 	// Collision Data
 	public final static double COLLISION_WIDTH = 1;
 	public final static double COLLISION_HEIGHT = 1;
 
 	public final static double HOOK_COOLDOWN = 3;
 	public final static double GRAPPLEHOOK_COOLDOWN = 15;
 	public final static double BURNERHOOK_COOLDOWN = 8;
 
 	public final static double ATK_RANGE = 2;
 
 	public final static double RESPAWN_INTERVAL = 10;
 
 	public final static double MIN_HEART_SCALE = 1;
 	public final static double MAX_HEART_SCALE = 2.5;
 
 	public Stats stats;
 
 	// Whether or not you can control this Pudge
 	public boolean controllable = false;
 
 	// Hooking
 	public int activeHook;
 	public boolean isHooking;
 	public double hookCooldown;
 	public double burnerCooldown;
 	public double grappleCooldown;
 	// Attacking
 	public double attackInterval;
 
 	// Target
 	protected Image clicker;
 	protected PudgeEntity targetEnemy;
 	protected Vector2 target;
 	protected double targetRotation;
 
 	protected Vector2 hookTarget;
 	// protected boolean isGrapple;
 	protected int hookType;
 
 	// Rendering
 	protected Animation ani;
 	protected Image outline;
 	protected ArcImage life;
 	protected Image fullLife;
 	protected Image emptyLife;
 	protected Animation heart;
 
 	// Respawning
 	public double respawnInterval = RESPAWN_INTERVAL;
 
 	public PudgeEntity(Vector2 position, Team team, int clientID) {
 		super(position, new Vector2(COLLISION_WIDTH, COLLISION_HEIGHT));
 
 		this.ClientID = clientID;
 
 		this.team = team;
 
 		stats = new Stats(this);
 		stats.restoreDefaults();
 		// stats.subLife(8);
 
 		rigidbody.physicsSlide = true;
 
 		int index = clientID;
 		if (team == Team.leftTeam) index /= 2;
 		else index = (index - 1) / 2;
 		index = team == Team.leftTeam ? index : index + 4;
 		System.out.println("Index: " + index);
 
 		if (team == Team.leftTeam) outline = ImageHandler.get().getImage("outline_left");
 		else outline = ImageHandler.get().getImage("outline_right");
 
 		ani = Animation.makeAnimation("cowboys", 8, index, 32, 32, 0.05);
 		ani.startAnimation();
 
 		heart = Animation.makeAnimation("heart4", 8, 32, 32, 1 / 8.0);
 		heart.startAnimation();
 
 		clicker = ImageHandler.get().getImage("selector");
 		target = null;
 
 		transform.drawScale = new Vector2(2, 2);
 		fullLife = ImageHandler.get().getImage("life_full");
 		emptyLife = ImageHandler.get().getImage("life_empty");
 		life = new ArcImage((BufferedImage) emptyLife, (BufferedImage) fullLife);
 	}
 
 	public void update() {
 		// Heart Animation
 		heart.timeScale = -(MAX_HEART_SCALE - MIN_HEART_SCALE) * stats.lifePercentage() + MAX_HEART_SCALE;
 		heart.update();
 
 		if (rigidbody.isMoving()) {
 			// Update Animation
 			ani.update();
 
 			// Add Dust Particles
 			Random r = new Random();
 			if (r.nextInt(5) == 0) {
 				Vector2 posOffset = new Vector2(r.nextDouble() - 0.5, r.nextDouble() - 0.5);
 				posOffset.scale(0.5);
 				Vector2 velOffset = new Vector2(r.nextDouble() - 0.5, r.nextDouble() - 0.5);
 				velOffset.scale(0.5); // Randomness of Velocity
 				velOffset.add(rigidbody.velocity);
 				velOffset.scale(-0.2);
 				Game.entities.addParticle( //
 						new VelocityParticle("dust", 3, 3, 1, //
 								Vector2.add(posOffset, transform.position), //
 								velOffset, 0.35));
 			}
 		}
 
 		if (!canMove) target = null;
 
 		// Stats
 		if (controllable && Game.keyInput.buyMode.wasPressed()) {
 			stats.isOpen ^= true; // Cool way to NOT
 		}
 
 		if (controllable && canMove && !stats.isOpen) {
 			if (hookCooldown > 0) hookCooldown -= Time.getTickInterval();
 			if (hookCooldown < 0) hookCooldown = 0;
 			if (grappleCooldown > 0) grappleCooldown -= Time.getTickInterval();
 			if (grappleCooldown < 0) grappleCooldown = 0;
 			if (burnerCooldown > 0) burnerCooldown -= Time.getTickInterval();
 			if (burnerCooldown < 0) burnerCooldown = 0;
 
 			// Change Cursor
 			// if (Game.keyInput.specialHook.isDown) Game.cursor.setCursor("Special");
 			// else Game.cursor.setCursor("Default");
 
 			if (Game.keyInput.n1.wasPressed()) activeHook = HookType.NORMAL;
 			if (Game.keyInput.n2.wasPressed()) activeHook = HookType.BURNER;
 			if (Game.keyInput.n3.wasPressed()) activeHook = HookType.GRAPPLE;
 
 			// Hover
 			if (Game.keyInput.space.isDown) Game.focus = transform.position.clone();
 
 			Vector2 left = Game.mouseInput.left.wasPressed();
 			if (left != null) {
 				switch (activeHook) {
 					case HookType.NORMAL:
 						if (hookCooldown <= 0) {
 							if (setHook(Game.s.screenToWorldPoint(left), HookType.NORMAL)) hookCooldown = HOOK_COOLDOWN;
 							hookTarget = Game.s.screenToWorldPoint(left);
 							shouldSendNetworkData = true;
 						}
 						break;
 					case HookType.BURNER:
 						if (hookCooldown <= 0) {
 							if (setHook(Game.s.screenToWorldPoint(left), HookType.BURNER)) burnerCooldown = BURNERHOOK_COOLDOWN;
 							hookTarget = Game.s.screenToWorldPoint(left);
 							shouldSendNetworkData = true;
 						}
 						break;
 					case HookType.GRAPPLE:
 						if (grappleCooldown <= 0) {
 							if (setHook(Game.s.screenToWorldPoint(left), HookType.GRAPPLE)) grappleCooldown = GRAPPLEHOOK_COOLDOWN;
 							hookTarget = Game.s.screenToWorldPoint(left);
 							shouldSendNetworkData = true;
 						}
 						break;
 				}
 			}
 
 			Vector2 right = Game.mouseInput.right.wasPressed();
 			if (right != null) {
 				right = Game.s.screenToWorldPoint(right);
 
 				// See if right clicked on any player
 				clickedOnPlayer(right);
 
 				shouldSendNetworkData = true;
 			}
 
 			// Rotate the Clicker
 			if (target != null) targetRotation += -0.1;
 		}
 
 		// Target Movement
 		if (target != null) {
 			transform.rotateTowards(target, 0.1);
 
 			double dist = transform.position.distance(target);
 			if (dist < rigidbody.velocity.magnitude() * Time.getTickInterval()) {
 				rigidbody.velocity = new Vector2(0, 0);
 				target = null;
 
 				if (controllable) {
 					// SoundEffect.GALLOP.stop();
 					SoundEffect.NASAL.play();
 				}
 			} else {
 				// if(!SoundEffect.GALLOP.isPlaying() && controllable) SoundEffect.GALLOP.play();
 				rigidbody.setDirection(target);
 			}
 		}
 
 		// Attacking
 		if (attackInterval > 0) attackInterval -= Time.getTickInterval();
 		if (attackInterval < 0) attackInterval = 0;
 		if (targetEnemy != null) {
 			if (targetEnemy.transform.position.distance(transform.position) < ATK_RANGE) {
 				if (attackInterval == 0) {
 					attackInterval = 0.5;
 					Game.entities.addParticle(ParticleTypes.DIE, targetEnemy, null, 0.25);
 					if (targetEnemy.stats.subLife(4)) {
 						if (Game.isServer) {
 							stats.addExp(2);
 							stats.addKill();
 						}
 						targetEnemy = null;
 					}
 				}
 			}
 		}
 
 		rigidbody.updateVelocity();
 	}
 
 	public void respawnUpdate() {
 		if (respawning) {
 			if (respawnInterval < 0) {
 				this.stats.set_life(20);
 				String position = (team == Team.leftTeam) ? "4.0 " : "20.0 ";
 				position += 8 * (ClientID / 2) + 4;
 				transform.position.setNetString(position);
 				rigidbody.velocity.setNetString("0.0 0.0");
 				respawnInterval = RESPAWN_INTERVAL;
 				remove = false;
 				respawn = true;
 			}
 			respawnInterval -= Time.getTickInterval();
 		}
 	}
 
 	public void clickedOnPlayer(Vector2 right) {
 		List<CollisionBox> l = Game.entities.getEntityListCollisionBoxes(right);
 		targetEnemy = null;
 		for (CollisionBox b : l) {
 			if (b.owner instanceof PudgeEntity && b.owner != this) {
 				PudgeEntity p = (PudgeEntity) b.owner;
 				if (!this.isTeammate(p) && p.shouldRender) {
 					System.out.println("Clicked on: " + p.name);
 					targetEnemy = p;
 					break;
 				}
 			}
 		}
 
 		if (targetEnemy == null) target = right;
 		else target = targetEnemy.transform.position.clone();
 	}
 
 	public void render() {
 		if (!shouldRender) return;
 
 		AffineTransform a = transform.getAffineTransformation();
 
 		// Draw Pudge
 		Game.s.g.drawImage(outline, a, null);
 		Game.s.g.drawImage(ani.getImage(), a, null);
 
 		/*
 		 * LIFE DRAWING
 		 */
 
 		// Dimension Definitions!
 		Vector2 v = Game.s.worldToScreenPoint(transform.position);
 		v.y -= Game.TILE_SIZE / 2;
 		int lifebarWidth = fullLife.getWidth(null);
 		int lifebarHeight = fullLife.getHeight(null);
 		int lifebarActual = (int) (fullLife.getWidth(null) * stats.lifePercentage());
 
 		Game.s.g.drawImage(emptyLife, (int) v.x - lifebarWidth / 2, (int) v.y - lifebarHeight / 2, (int) v.x + lifebarWidth / 2, (int) v.y + lifebarHeight / 2, //
 				0, 0, lifebarWidth, lifebarHeight, null);
 		Game.s.g.drawImage(fullLife, (int) v.x - lifebarWidth / 2, (int) v.y - lifebarHeight / 2, (int) v.x - lifebarWidth / 2 + lifebarActual, (int) v.y + lifebarHeight / 2, //
 				0, 0, lifebarActual, lifebarHeight, null);
 	}
 
 	public void onGUI() {
 		if (controllable) {
 			// Draw Target Reticle
 			if (target != null) {
 				Vector2 targetLocation = Game.s.worldToScreenPoint(target);
 				AffineTransform a = new AffineTransform();
 				a.translate((int) (targetLocation.x - CLICK_SIZE / 2), (int) (targetLocation.y - CLICK_SIZE / 2));
 				a.rotate(targetRotation, CLICK_SIZE / 2, CLICK_SIZE / 2);
 				Game.s.g.drawImage(clicker, a, null);
 			}
 
 			Image i = heart.getImage();
 			Game.s.g.drawImage(i, //
 					Stats.LEFT_PADDING, Game.s.height - (Stats.BOT_PADDING + 32), //
 					32, 32, null);
 
 			// Draw Stats
 			stats.onGUI();
 		}
 	}
 
 	/*
 	 * Hooking
 	 */
 	public boolean setHook(Vector2 click, int shootingHook) {
 		if (!isHooking) {
 			Entity e = null;
 			switch (shootingHook) {
 				case HookType.NORMAL:
 					e = new NormalHookEntity(this, click);
 					hookType = HookType.NORMAL;
 					break;
 				case HookType.GRAPPLE:
 					e = new GrappleHookEntity(this, click);
 					hookType = HookType.GRAPPLE;
 					break;
 				case HookType.BURNER:
 					e = new BurnerHookEntity(this, click);
 					hookType = HookType.BURNER;
 					break;
 			}
 			Game.entities.entities.add(e);
 			isHooking = true;
 
 			// Play Hook Sound Effect
			if(controllable) SoundEffect.LASSO_RELEASE.play();
 			return true;
 		}
 		return false;
 	}
 
 	public void restoreDefaults() {
 		System.out.println("[PUDGE] Restore Defaults!");
 		super.restoreDefaults();
 		stats.restoreDefaults();
 	}
 
 	/*
 	 * Collisions
 	 */
 	public boolean shouldBlock(BBOwner b) {
 		if (b instanceof HookEntity) return true;
 		if (b instanceof PudgeEntity || b instanceof CowEntity) {
 			return canEntityCollide ? true : isHooking;
 		}
 		if (canTileCollide) {
 			if (b instanceof Tile) {
 				if (((Tile) b).isPudgeSolid()) return true;
 			}
 		}
 		return false;
 	}
 
 	public void collides(Entity e, double vx, double vy) {
 		super.collides(e, vx, vy);
 		if (e instanceof HookableEntity) {
 			HookableEntity p = (HookableEntity) e;
 			if (p.attachedHook != null) {
 				if (p.attachedHook.owner == this) {
 					p.attachedHook.detachHookableEntity();
 					p.rigidbody.velocity = Vector2.ZERO.clone();
 				}
 			}
 		}
 	}
 
 	public void kill() {
 		if (Game.isServer) {
 			System.out.println("Pudge was Killed");
 			respawning = true;
 			super.kill();
 		}
 	}
 
 	/*
 	 * Network
 	 */
 	public void sendNetworkData() {
 		if (controllable) {
 			super.sendNetworkData();
 		}
 	}
 
 	public String getNetworkString() {
 		String s = "PUDGE:";
 		s += ClientID + ":";
 		s += transform.position.getNetString();
 		s += ":" + rigidbody.velocity.getNetString() + ":";
 		s += (target == null) ? "null" : target.getNetString();
 		s += ":" + team + ":";
 		s += (hookTarget == null) ? "null" : hookTarget.getNetString();
 		s += ":" + hookType;
 		s += ":" + stats.getNetString();
 		s += ":" + name;
 		hookTarget = null;
 		hookType = 0;
 		return s;
 	}
 
 	public void setNetworkString(String s) {
 		wasUpdated = true;
 		String[] t = s.split(":");
 
 		if (!Game.isServer) {
 			transform.position.setNetString(t[2]);
 			rigidbody.velocity.setNetString(t[3]);
 		}
 
 		if (t[4].equals("null")) {
 			target = null;
 		} else {
 			target = new Vector2();
 			target.setNetString(t[4]);
 			clickedOnPlayer(target);
 		}
 
 		if (!t[6].equals("null")) {
 			String[] u = t[6].split(" ");
 			Vector2 hookTarget = new Vector2(Float.parseFloat(u[0]), Float.parseFloat(u[1]));
 
 			int ht = Integer.parseInt(t[7]);
 			setHook(hookTarget, ht);
 		}
 
 		this.stats.setNetString(t[8]);
 	}
 
 	/*
 	 * Light Source
 	 */
 	public Shape getLightShape() {
 		Vector2 v = Game.s.worldToScreenPoint(transform.position);
 		v.scale(1.0 / Window.LIGHTMAP_MULT);
 		double r = (4 * Game.TILE_SIZE) / Window.LIGHTMAP_MULT;
 		Shape circle = new Ellipse2D.Double(v.x - r, v.y - r, r * 2, r * 2);
 		return circle;
 	}
 }
