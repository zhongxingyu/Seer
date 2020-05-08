 package org.racenet.racesow;
 
 import java.util.List;
 import java.util.Random;
 
 import org.racenet.framework.AndroidAudio;
 import org.racenet.framework.AndroidSound;
 import org.racenet.framework.AnimatedBlock;
 import org.racenet.framework.Camera2;
 import org.racenet.framework.CameraText;
 import org.racenet.framework.FifoPool;
 import org.racenet.framework.FifoPool.PoolObjectFactory;
 import org.racenet.framework.GLGame;
 import org.racenet.framework.GameObject;
 import org.racenet.framework.Polygon;
 import org.racenet.framework.TexturedBlock;
 import org.racenet.framework.TexturedShape;
 import org.racenet.framework.Vector2;
 import org.racenet.racesow.GameScreen.GameState;
 import org.racenet.racesow.threads.InternalScoresThread;
 
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.os.Handler;
 import android.os.Message;
 import android.util.Log;
 
 /**
  * Class which represents the player in the game.
  * Executes actions like shoot and jump and moves
  * the player througt the world.
  * 
  * @author al
  *
  */
 class Player extends AnimatedBlock {
 	
 	public final Vector2 velocity = new Vector2();
 	public final Vector2 accel = new Vector2();
 	
 	// animations
 	public static final short ANIM_RUN = 0;
 	public static final short ANIM_JUMP = 1;
 	public static final short ANIM_WALLJUMP = 2;
 	public static final short ANIM_BURN = 3;
 	public static final short ANIM_INVISIBLE = 4;
 	public static final short ANIM_ROCKET_RUN = 5;
 	public static final short ANIM_ROCKET_JUMP = 6;
 	public static final short ANIM_ROCKET_WALLJUMP = 7;
 	public static final short ANIM_PLASMA_RUN = 8;
 	public static final short ANIM_PLASMA_JUMP = 9;
 	public static final short ANIM_PLASMA_WALLJUMP = 10;
 	public static final short ANIM_DROWN = 11;
 	
 	// sounds
 	public static final short SOUND_JUMP1 = 0;
 	public static final short SOUND_JUMP2 = 1;
 	public static final short SOUND_WJ1 = 2;
 	public static final short SOUND_WJ2 = 3;
 	public static final short SOUND_DIE = 4;
 	public static final short SOUND_PICKUP = 5;
 	public static final short SOUND_ROCKET = 6;
 	public static final short SOUND_PLASMA = 7;
 	private AndroidSound sounds[] = new AndroidSound[8];
 	
 	// firerates
 	private static final float FIRERATE_ROCKET = 1.75f;
 	private static final float FIRERATE_PLASMA = 0.04f;
 	private float lastShot = 0;
 	
 	private boolean onFloor = false;
 	private float lastWallJumped = 0;
 	private float distanceOnJump = -1;
 	private boolean distanceRemembered = false;
 	public float virtualSpeed = 0;
 	private float startSpeed = 450;
 	private boolean enableAnimation = false;
 	private boolean isDead = false;
 	private float animDuration = 0;
 	private TexturedBlock attachedItem;
 	private Camera2 camera;
 	private Random rGen;
 	private float volume = 0.1f;
 	private String model = "male";
 	private Map map;
 	private FifoPool<TexturedBlock> plasmaPool;
 	private FifoPool<TexturedBlock> rocketPool;
 	private boolean soundEnabled;
 	private GameScreen gameScreen;
 	CameraText restartMessage;
 	CameraText timeMessage;
 	CameraText finishMessage;
 	CameraText recordMessage;
 	GameObject tutorialActive;
 	CameraText tutorialMessage1;
 	CameraText tutorialMessage2;
 	CameraText tutorialMessage3;
 	
 	private int frames = 0;
 	
 	/**
 	 * Constructor.
 	 * 
 	 * @param GLGame game
 	 * @param Map map
 	 * @param Camera2 camera
 	 * @param float x
 	 * @param float y
 	 * @param boolean soundEnabled
 	 */
 	public Player(final GLGame game, Map map, Camera2 camera, float x, float y, boolean soundEnabled) {
 		
 		// create the TexturedShape with static width and height
 		super(game, new Vector2(x,y), new Vector2(x + 3.4f, y), new Vector2(x + 3.4f, y + 6.5f), new Vector2(x, y + 6.5f));
 		
 		this.soundEnabled = soundEnabled;
 		this.rGen = new Random();
 		this.camera = camera;
 		this.map = map;
 		
 		// load the sounds
 		AndroidAudio audio = (AndroidAudio)game.getAudio();
 		this.sounds[SOUND_JUMP1] = (AndroidSound)audio.newSound("sounds/player/" + this.model + "/jump_1.ogg");
 		this.sounds[SOUND_JUMP2] = (AndroidSound)audio.newSound("sounds/player/" + this.model + "/jump_2.ogg");
 		this.sounds[SOUND_WJ1] = (AndroidSound)audio.newSound("sounds/player/" + this.model + "/wj_1.ogg");
 		this.sounds[SOUND_WJ2] = (AndroidSound)audio.newSound("sounds/player/" + this.model + "/wj_2.ogg");
 		this.sounds[SOUND_DIE] = (AndroidSound)audio.newSound("sounds/player/" + this.model + "/death.ogg");
 		this.sounds[SOUND_PICKUP] = (AndroidSound)audio.newSound("sounds/weapon_pickup.ogg");
 		this.sounds[SOUND_ROCKET] = (AndroidSound)audio.newSound("sounds/rocket_explosion.ogg");
 		this.sounds[SOUND_PLASMA] = (AndroidSound)audio.newSound("sounds/plasmagun.ogg");
 		
 		this.loadAnimations();
 		this.setupVertices();
 		
 		// create a first-in-first-out pool for plasma decals
 		this.plasmaPool = new FifoPool<TexturedBlock>(new PoolObjectFactory<TexturedBlock>() {
 			
 			public TexturedBlock createObject() {
 				
 				return new TexturedBlock(
 						game,
 						"decals/plasma_hit.png",
 						GameObject.FUNC_NONE,
 						-1,
 						-1,
 						new Vector2(-1, 0),
 						new Vector2(2, 0)
 						);
 			}
 		}, 10);
 		
 		// create a first-in-first-out pool for rocket decals
 		this.rocketPool = new FifoPool<TexturedBlock>(new PoolObjectFactory<TexturedBlock>() {
 	        	
             public TexturedBlock createObject() {
             	
             	return new TexturedBlock(
         				game,
         				"decals/rocket_hit.png",
         				GameObject.FUNC_NONE,
         				-1,
         				-1,
         				new Vector2(-3, 0),
         				new Vector2(5, 0)
         			);
             }
         }, 1);
 	}
 	
 	/**
 	 * Player requires to know the gameScreen
 	 * 
 	 * @param GameScreen gameScreen
 	 */
 	public void setGameScreen(GameScreen gameScreen) {
 		
 		this.gameScreen = gameScreen;
 	}
 	
 	/**
 	 * Load all defined animations
 	 */
 	public void loadAnimations() {
 		
 		String[][] animations = new String[12][];
 		
 		animations[ANIM_RUN] = new String[] {
 			"player/" + this.model + "/default.png"
 		};
 		
 		animations[ANIM_JUMP] = new String[] {
 			"player/" + this.model + "/jump_f1.png",
 			"player/" + this.model + "/jump_f2.png",
 			"player/" + this.model + "/jump_f1.png"
 		};
 		
 		animations[ANIM_ROCKET_RUN] = new String[] {
 			"player/" + this.model + "/rocket_run.png"
 		};
 		
 		animations[ANIM_ROCKET_JUMP] = new String[] {
 			"player/" + this.model + "/rocket_jump_f1.png",
 			"player/" + this.model + "/rocket_jump_f2.png",
 			"player/" + this.model + "/rocket_jump_f1.png"
 		};
 		
 		animations[ANIM_ROCKET_WALLJUMP] = new String[] {
 			"player/" + this.model + "/rocket_walljump_f1.png",
 			"player/" + this.model + "/rocket_walljump_f2.png",
 			"player/" + this.model + "/rocket_walljump_f1.png"
 		};
 		
 		animations[ANIM_PLASMA_RUN] = new String[] {
 			"player/" + this.model + "/plasma_run.png"
 		};
 		
 		animations[ANIM_PLASMA_JUMP] = new String[] {
 			"player/" + this.model + "/plasma_jump_f1.png",
 			"player/" + this.model + "/plasma_jump_f2.png",
 			"player/" + this.model + "/plasma_jump_f1.png"
 		};
 		
 		animations[ANIM_PLASMA_WALLJUMP] = new String[] {
 			"player/" + this.model + "/plasma_walljump_f1.png",
 			"player/" + this.model + "/plasma_walljump_f2.png",
 			"player/" + this.model + "/plasma_walljump_f1.png"
 		};
 		
 		animations[ANIM_WALLJUMP] = new String[] {
 			"player/" + this.model + "/walljump_f1.png",
 			"player/" + this.model + "/walljump_f2.png",
 			"player/" + this.model + "/walljump_f1.png"
 		};
 		
 		animations[ANIM_BURN] = new String[] {
 			"player/" + this.model + "/burn_f1.png",
 			"player/" + this.model + "/burn_f2.png",
 			"player/" + this.model + "/burn_f3.png",
 			"player/" + this.model + "/burn_f4.png"
 		};
 		
 		animations[ANIM_DROWN] = new String[] {
 			"player/" + this.model + "/drown_f1.png",
 			"player/" + this.model + "/drown_f2.png",
 			"player/" + this.model + "/drown_f3.png",
 			"player/" + this.model + "/drown_f4.png"
 		};
 		
 		animations[ANIM_INVISIBLE] = new String[] {
 			"player/" + this.model + "/invisible.png"
 		};
 	
 		this.setAnimations(animations);
 	}
 	
 	/**
 	 * Remove a tutorial message and continue the
 	 * game if the proper event is beeing passed
 	 *  
 	 * @param String event
 	 */
 	public void updateTutorial(String event) {
 		
 		if (this.tutorialActive != null) {
 			
 			if (!event.equals("reset") && !event.equals(this.tutorialActive.event)) return;
 			
 			this.tutorialActive = null;
 			((GameScreen)this.game.getCurrentScreen()).state = GameState.Running;
 			
 			if (this.tutorialMessage1 != null) {
 				
 				synchronized (this) {
 					
 					this.camera.removeHud(this.tutorialMessage1);
 					this.tutorialMessage1 = null;
 				}
 			}
 			
 			if (this.tutorialMessage2 != null) {
 				
 				synchronized (this) {
 					
 					this.camera.removeHud(this.tutorialMessage2);
 					this.tutorialMessage2 = null;
 				}
 			}
 			
 			if (this.tutorialMessage3 != null) {
 				
 				synchronized (this) {
 				
 					this.camera.removeHud(this.tutorialMessage3);
 					this.tutorialMessage3 = null;
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Execute the jump action
 	 * 
 	 * @param float jumpPressedTime
 	 */
 	public void jump(float jumpPressedTime) {
 		
 		if (this.isDead) return;
 		
 		// only for an initial jump action (not if the player holds jump)
 		if (jumpPressedTime == 0) {
 		
 			this.updateTutorial("jump");
 		}
 		
 		// remember the distance to the ground when falling and pressing jump
 		if (!this.distanceRemembered && this.velocity.y < 0) {
 			
 			TexturedShape ground = this.map.getGround(this);
 			if (ground != null) {
 				
 				this.distanceOnJump = Math.max(0.32f,
 						this.getPosition().y - (ground.getPosition().y + ground.getHeightAt(this.getPosition().x)));
 				this.distanceRemembered = true;
 			}
 		}
 		
 		// only jump when the player is on the floor
 		if (this.onFloor) {
 		
 			this.onFloor = false;
 			
 			if (this.soundEnabled) {
 				
 				int jumpSound = this.rGen.nextInt(SOUND_JUMP2 - SOUND_JUMP1 + 1) + SOUND_JUMP1;
 				this.sounds[jumpSound].play(this.volume);
 			}
 			
 			// reset to startspeed when no speed left
 			if (this.virtualSpeed == 0) {
 				
 				this.virtualSpeed = this.startSpeed;
 			}
 			
 			// give the player a speed boost according to
 			// the distance when initially pressed jump
 			if (this.distanceOnJump > 0) {
 				
 				float boost = (30000 / (this.virtualSpeed / 2) / this.distanceOnJump);
 				this.virtualSpeed += boost;
 			}
 			
 			this.velocity.add(0, 20);
 			this.distanceRemembered = false;
 			this.distanceOnJump = -1;
 			
 			// choose the proper jump animation
 			if (this.attachedItem != null) {
 				
 				switch (this.attachedItem.func) {
 				
 					case GameObject.ITEM_ROCKET:
 						this.activeAnimId = Player.ANIM_ROCKET_JUMP;
 						break;
 					
 					case GameObject.ITEM_PLASMA:
 						this.activeAnimId = Player.ANIM_PLASMA_JUMP;
 						break;
 						
 					default:
 						this.activeAnimId = Player.ANIM_JUMP;
 						break;
 				}
 				
 			} else {
 			
 				this.activeAnimId = Player.ANIM_JUMP;
 			}
 			
 			this.enableAnimation = true;
 			this.animDuration = 0.3f;
 		
 		// when in the air check for walls to perform a walljump
 		} else {
 			
 			// allow walljump only in certain intervals
 			if (jumpPressedTime == 0 && System.nanoTime() / 1000000000.0f > this.lastWallJumped + 1.5f) {
 				
 				List<GameObject> colliders = this.map.getPotentialWallColliders(this);
 				int length = colliders.size();
 				for (int i = 0; i < length; i++) {
 				
 					GameObject part = colliders.get(i);
 					CollisionInfo info = this.intersect(part);
 					if (info.collided) {
 						
 						if (this.soundEnabled) {
 						
 							int wjSound = this.rGen.nextInt(SOUND_WJ2 - SOUND_WJ1 + 1) + SOUND_WJ1;
 							this.sounds[wjSound].play(this.volume);
 						}
 						
 						// add some velocity after a walljump
 						this.velocity.set(this.velocity.x + 5, 17);
 						
 						// remember the walljump time for the interval to work
 						this.lastWallJumped = System.nanoTime() / 1000000000.0f;
 						
 						// choose the proper animation
 						if (this.attachedItem != null) {
 							
 							switch (this.attachedItem.func) {
 							
 								case GameObject.ITEM_ROCKET:
 									this.activeAnimId = Player.ANIM_ROCKET_WALLJUMP;
 									break;
 								
 								case GameObject.ITEM_PLASMA:
 									this.activeAnimId = Player.ANIM_PLASMA_WALLJUMP;
 									break;
 									
 								default:
 									this.activeAnimId = Player.ANIM_WALLJUMP;
 									break;
 							}
 							
 						} else {
 						
 							this.activeAnimId = Player.ANIM_WALLJUMP;
 						}
 						this.enableAnimation = true;
 						this.animDuration = 0.3f;
 					}
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Execute the shoot action
 	 * 
 	 * @param float shootPressedTime
 	 */
 	public void shoot(float shootPressedTime) {
 		
 		this.updateTutorial("shoot");
 		
 		// if there is no attached item or the
 		// player is dead we can not shoot
 		if (this.attachedItem == null || this.isDead) return;
 		
 		float currentTime = System.nanoTime() / 1000000000.0f;
 		switch (this.attachedItem.func) {
 		
 			// when shooting with the rocketlauncher
 			case GameObject.ITEM_ROCKET:
 				boolean hitWall = false;
 				if (currentTime >= this.lastShot + FIRERATE_ROCKET) {
 					
 					// prefer wall-rockets 
 					List<GameObject> colliders = this.map.getPotentialWallColliders(this);
 					int length = colliders.size();
 					for (int i = 0; i < length; i++) {
 					
 						GameObject part = colliders.get(i);
 						CollisionInfo info = this.intersect(part);
 						if (info.collided) {
 					
 							hitWall = true;
 							float impactX = this.getPosition().x;
 							float impactY = this.getPosition().y;
 							
 							// give the player some speed boost for wall-rockets
 							this.velocity.set(this.velocity.x, this.velocity.y < 0 ? 30 : this.velocity.y + 20);
 							this.virtualSpeed += 200;
 							
 							if (this.soundEnabled) {
 							
 								this.sounds[SOUND_ROCKET].play(this.volume * 1.5f);
 							}
 							
 							// show the rocket explosion
 							TexturedBlock decal = this.rocketPool.newObject();
 							decal.setPosition(new Vector2(impactX, impactY));
 							map.addDecal(decal, 0.25f);
 							
 							this.lastShot = currentTime;
 							break;
 						}
 					}
 					
 					// if we didn't hit a wall then hit the ground
 					if (!hitWall) {
 						
 						TexturedShape ground = map.getGround(this);
 						if (ground != null) {
 							
 							float impactY = ground.getPosition().y + ground.getHeightAt(this.getPosition().x) - 4;
 							float distance = this.getPosition().y - impactY;
 							
 							// only allow to hit blocks without functionality (ie. no lava)
 							if (ground.func == GameObject.FUNC_NONE) {
 								
 								this.addToPosition(0, 1);
 								this.velocity.add(0, 100 / distance);
 								this.onFloor = false;
 							}
 							
 							if (this.soundEnabled) {
 							
 								this.sounds[SOUND_ROCKET].play(this.volume * 1.5f);
 							}
 							
 							// show the rocket explosion
 							TexturedBlock decal = this.rocketPool.newObject();
 							decal.setPosition(new Vector2(this.getPosition().x, impactY));
 							map.addDecal(decal, 0.25f);
 						}
 						
 						this.lastShot = currentTime;
 					}
 				}
 				break;
 				
 			// when shooting with the plasma gun
 			case GameObject.ITEM_PLASMA:
 				if (currentTime >= this.lastShot + FIRERATE_PLASMA) {
 					
 					// plasma can only hit walls
 					List<GameObject> colliders = this.map.getPotentialWallColliders(this);
 					int length = colliders.size();
 					for (int i = 0; i < length; i++) {
 					
 						GameObject part = colliders.get(i);
 						CollisionInfo info = this.intersect(part);
 						if (info.collided) {
 					
 							float impactX = this.getPosition().x;
 							float impactY = this.getPosition().y + 1;
 							
 							// give the player some speed boost
 							this.velocity.add(0, 2.5f);
 							this.virtualSpeed += 15;
 							
 							if (this.soundEnabled) {
 							
 								this.sounds[SOUND_PLASMA].play(this.volume * 1.2f);
 							}
 							
 							// show the plasma impact
 							TexturedBlock decal = (TexturedBlock)this.plasmaPool.newObject();
 							decal.setPosition(new Vector2(impactX, impactY));
 							map.addDecal(decal, 0.25f);
 							
 							this.lastShot = currentTime;
 							break;
 						}
 					}
 					
 					this.lastShot = currentTime;
 				}
 				break;
 		}
 	}
 	
 	/**
 	 * Move the player in the map
 	 * 
 	 * @param Vector2 gravity
 	 * @param float deltaTime
 	 * @param boolean pressingJump
 	 */
 	public void move(Vector2 gravity, float deltaTime, boolean pressingJump) {
 		
 		 // workaround for initial loading
 		if (++frames < 3) return;
 		
 		// if enabled run a player animation
 		if (this.enableAnimation) {
 			
 			this.animTime += deltaTime;
 			if (this.animTime > this.animDuration) {
 				
 				this.enableAnimation = false;
 				this.animTime = 0;
 				
 				if (this.activeAnimId == Player.ANIM_BURN || this.activeAnimId == Player.ANIM_DROWN) {
 					
 					this.activeAnimId = Player.ANIM_INVISIBLE;
 				
 				// when the animation is over, choose
 				// the proper default animation
 				} else {
 				
 					if (this.attachedItem != null) {
 						
 						switch (this.attachedItem.func) {
 						
 							case GameObject.ITEM_ROCKET:
 								this.activeAnimId = Player.ANIM_ROCKET_RUN;
 								break;
 							
 							case GameObject.ITEM_PLASMA:
 								this.activeAnimId = Player.ANIM_PLASMA_RUN;
 								break;
 								
 							default:
 								this.activeAnimId = Player.ANIM_RUN;
 								break;
 						}
 						
 					} else {
 					
 						this.activeAnimId = Player.ANIM_RUN;
 					}
 				}
 			}
 		}
 		
 		if (this.isDead) return;
 		
 		// see if the player collides with a map-function
 		boolean stop = false;
 		List<GameObject> colliders = this.map.getPotentialFuncColliders(this);
 		int length = colliders.size();
 		for (int i = 0; i < length; i++) {
 		
 			GameObject part = colliders.get(i);
 			CollisionInfo info = this.intersect(part);
 			if (info.collided) {
 			
 				switch (part.func) {
 				
 					case GameObject.FUNC_START_TIMER:
 						this.map.startTimer();
 						break;
 						
 					case GameObject.FUNC_STOP_TIMER:
 						this.finishRace();
 						break;
 						
 					case GameObject.FUNC_TUTORIAL:
 						this.showTutorialMessage(part);
 						break;
 				}
 			}
 			
 			if (stop) break;
 		}
 		
 		// see if the player picks up an item (plasmagun, rocketlauncher)
 		length = this.map.items.size();
 		for (int i = 0; i < length; i++) {
 			
 			TexturedShape item = this.map.items.get(i);
 			float playerX = this.getPosition().x;
 			float itemX = item.getPosition().x;
 			if (playerX >= itemX && playerX <= itemX + item.width) {
 				
 				String texture = "";
 				switch (item.func) {
 				
 					case GameObject.ITEM_ROCKET:
 						texture = "items/rocket.png";
 						this.activeAnimId = ANIM_ROCKET_RUN;
 						break;
 					
 					case GameObject.ITEM_PLASMA:
 						texture = "items/plasma.png";
 						this.activeAnimId = ANIM_PLASMA_RUN;
 						break;
 				}
 				
 				// show a weapon icon in the HUD
 				TexturedBlock hudItem = new TexturedBlock(
 					this.game,
 					texture,
 					item.func,
 					-1,
 					-1,
 					new Vector2(-(this.camera.frustumWidth / 2) + 1, -(this.camera.frustumHeight / 2) + 1),
 					new Vector2(-(this.camera.frustumWidth / 2) + 10, -(this.camera.frustumHeight / 2) + 10)
 				);
 				
 				camera.addHud(hudItem);
 				this.map.items.remove(item);
 				this.map.pickedUpItems.add(item);
 				if (this.attachedItem != null) {
 					
 					synchronized (this) {
 						
 						this.camera.removeHud(this.attachedItem);
 						this.attachedItem.texture.dispose();
 					}
 				}
 				
 				this.lastShot = 0;
 				this.attachedItem = hudItem;
 				
 				if (this.soundEnabled) {
 				
 					this.sounds[SOUND_PICKUP].play(this.volume);
 				}
 				break;
 			}
 		}
 		
 		// when player is in the air
 		if (!this.onFloor) {
 			
 			// apply gravity
 			this.velocity.add(gravity.x * deltaTime, gravity.y * deltaTime);
 
 			// move the player
 			this.addToPosition(this.velocity.x * deltaTime, this.velocity.y * deltaTime);
 			
 			// check for collision with the ground
 			colliders = this.map.getPotentialGroundColliders(this);
 			length = colliders.size();
 			for (int i = 0; i < length; i++) {
 				
 				GameObject ground = colliders.get(i);
 				CollisionInfo info = this.intersect(ground);
 				
 				// check for functional collisions like water or lava
 				if (info.collided) {
 
 					switch (ground.func) {
 					
 						case GameObject.FUNC_LAVA:
 							this.activeAnimId = Player.ANIM_BURN;
 							this.enableAnimation = true;
 							this.animDuration = 0.4f;						
 							this.die();
 							return;
 							
 						case GameObject.FUNC_WATER:
 							this.activeAnimId = Player.ANIM_DROWN;
 							this.enableAnimation = true;
 							this.animDuration = 0.4f;						
 							this.die();
 							return;
 					}
 
 					// ground
 					if (info.type == Polygon.TOP) {
 					
 						this.setPosition(new Vector2(this.getPosition().x, this.getPosition().y + info.distance));
 						this.velocity.set(this.velocity.x, 0);
 						this.onFloor = true;
 					
 					// wall
 					} else if (info.type == Polygon.LEFT) {
 					
 						this.setPosition(new Vector2(this.getPosition().x - info.distance, this.getPosition().y));
 						this.velocity.set(0, this.velocity.y);
 						this.virtualSpeed = 0;
 					
 					// ramp up
 					} else if (info.type == Polygon.RAMPUP) {
 						
 						this.setPosition(new Vector2(this.getPosition().x, this.getPosition().y - info.distance));
 						if (pressingJump && this.virtualSpeed >= 900) {
 							
 							float m = (ground.vertices[2].y - ground.vertices[0].y) / (ground.vertices[2].x - ground.vertices[0].x);
 							this.velocity.set(this.velocity.x, this.velocity.x * m);
 							
 						} else {
 						
 							this.velocity.set(this.velocity.x, 0);
 							this.onFloor = true;
 						}
 						
 					// ramp down
 					} else if (info.type == Polygon.RAMPDOWN) {
 						
 						float m = (ground.vertices[0].y - ground.vertices[2].y) / (ground.vertices[0].x - ground.vertices[2].x);
 						
 						Log.d("DEBUG", "m " + String.valueOf(new Float(m)));
 						
 						this.velocity.set(this.velocity.x, 0);
 						this.virtualSpeed += (-m + 1) * (-m + 1) * (-m + 1) * 128;
 						this.onFloor = true;
 					}
 
 					break;
 				}
 			}
 		
 		// when on the ground...
 		} else {
 		
 			// ... lose some speed
 			if (this.virtualSpeed > 0) {
 				
 				this.virtualSpeed = Math.max(0, this.virtualSpeed - 10000 * deltaTime);
 			}
 		}
 		
 		// apply the velocity given by the virtual-speed
 		this.velocity.set(this.virtualSpeed / 23, this.velocity.y);
 	}
 	
 	/**
 	 * Let the player die
 	 */
 	public void die() {
 		
 		this.virtualSpeed = 0;
 		if (this.soundEnabled) {
 		
 			this.sounds[SOUND_DIE].play(this.volume);
 		}
 		
 		this.isDead = true;
 		
 		this.showRestartMessage();
 	}
 	
 	/**
 	 * Show a restart message
 	 */
 	public void showRestartMessage() {
 		
 		this.restartMessage = this.gameScreen.createCameraText(-30, -0);
 		this.restartMessage.text = "Press back to restart";
 		this.restartMessage.red = 1;
 		this.restartMessage.green = 0;
 		this.restartMessage.blue = 0;
 		this.restartMessage.scale = 0.15f;
 		this.restartMessage.space = 0.1f;
 		this.camera.addHud(this.restartMessage);
 	}
 	
 	/**
 	 * Show the current time
 	 */
 	public void showTimeMessage() {
 		
 		this.timeMessage = this.gameScreen.createCameraText(-27, 5);
 		this.timeMessage.text = "Your time: " + String.format("%.4f", this.map.getCurrentTime());
 		this.timeMessage.red = 0;
 		this.timeMessage.green = 0;
 		this.timeMessage.blue = 1;
 		this.timeMessage.scale = 0.15f;
 		this.timeMessage.space = 0.1f;
 		this.camera.addHud(this.timeMessage);
 	}
 	
 	/**
 	 * Show "new record" message
 	 */
 	public void showtRecordMessage() {
 		
 		this.recordMessage = this.gameScreen.createCameraText(-27, 10);
 		this.recordMessage.text = "New personal record!";
 		this.recordMessage.red = 0;
 		this.recordMessage.green = 1;
 		this.recordMessage.blue = 0;
 		this.recordMessage.scale = 0.15f;
 		this.recordMessage.space = 0.1f;
 		this.camera.addHud(this.recordMessage);
 	}
 	
 	/**
 	 * Show "race finished" message
 	 */
 	public void showtFinishMessage() {
 		
 		this.finishMessage= this.gameScreen.createCameraText(-20, 10);
 		this.finishMessage.text = "Race finished!";
 		this.finishMessage.red = 1;
 		this.finishMessage.green = 1;
 		this.finishMessage.blue = 0;
 		this.finishMessage.scale = 0.15f;
 		this.finishMessage.space = 0.1f;
 		this.camera.addHud(this.finishMessage);
 	}
 	
 	/**
 	 * Show a tutorial message
 	 * 
 	 * @param GameObject tutorial
 	 */
 	public void showTutorialMessage(GameObject tutorial) {
 		
 		if (tutorial.finished || this.tutorialActive != null) return;
 		
 		this.tutorialActive = tutorial;
 		tutorial.finished = true;
 		((GameScreen)this.game.getCurrentScreen()).state = GameState.Paused;
 		
 		this.tutorialMessage1= this.gameScreen.createCameraText(-32, 10);
 		this.tutorialMessage1.text = tutorial.info1;
 		this.tutorialMessage1.red = 0;
 		this.tutorialMessage1.green = 1;
 		this.tutorialMessage1.blue = 0;
 		this.tutorialMessage1.scale = 0.1f;
 		this.tutorialMessage1.space = 0.075f;
 		this.camera.addHud(this.tutorialMessage1);
 		
 		this.tutorialMessage2 = this.gameScreen.createCameraText(-32, 6);
 		this.tutorialMessage2.text = tutorial.info2;
 		this.tutorialMessage2.red = 0;
 		this.tutorialMessage2.green = 1;
 		this.tutorialMessage2.blue = 0;
 		this.tutorialMessage2.scale = 0.1f;
 		this.tutorialMessage2.space = 0.075f;
 		this.camera.addHud(this.tutorialMessage2);
 		
 		this.tutorialMessage3 = this.gameScreen.createCameraText(-32, 2);
 		this.tutorialMessage3.text = tutorial.info3;
 		this.tutorialMessage3.red = 0;
 		this.tutorialMessage3.green = 1;
 		this.tutorialMessage3.blue = 0;
 		this.tutorialMessage3.scale = 0.1f;
 		this.tutorialMessage3.space = 0.075f;
 		this.camera.addHud(this.tutorialMessage3);
 	}
 	
 	/**
 	 * Finish the race. Called when stopTimer is touched.
 	 */
 	public void finishRace() {
 		
 		if (!this.map.inRace()) return;
 		
 		this.map.stopTimer();
 		
 		SharedPreferences prefs = this.game.getSharedPreferences("racesow", Context.MODE_PRIVATE);
 		
 		// save the time to the local scores
 		InternalScoresThread t = new InternalScoresThread(
 			this.game.getApplicationContext(),
 			this.map.fileName,
 			prefs.getString("name", "player"),
 			this.map.getCurrentTime(),
 			new Handler() {
 		    	
 		    	@Override
 		        public void handleMessage(Message msg) {
 		    		
 		    		if (msg.getData().getBoolean("record")) {
 		    			
 		    			Player.this.showtRecordMessage();
 		    		
 		    		} else {
 		    			
 		    			Player.this.showtFinishMessage();
 		    		}
 		    		
 		    		Player.this.showTimeMessage();
 		    		Player.this.showRestartMessage();
 		    	}
 		});
 		
 		t.start();
 	}
 	
 	/**
 	 * Reset the player to the initial position
 	 * and also reset some variables and messages.
 	 * 
 	 * @param float x
 	 * @param float y
 	 */
 	public void reset(float x, float y) {
 				
 		this.updateTutorial("reset");
 		
 		this.isDead = false;
 		this.activeAnimId = ANIM_RUN;
 		this.virtualSpeed = 0;
 		this.setPosition(new Vector2(x, y));
 		this.velocity.set(0, 0);
 		
 		if (this.attachedItem != null) {
 			
 			synchronized (this) {
 				
 				this.camera.removeHud(this.attachedItem);
 				this.attachedItem.texture.dispose();
 				this.attachedItem = null;
 			}
 		}
 		
 		if (this.restartMessage != null) {
 			
 			synchronized (this) {
 			
 				this.camera.removeHud(this.restartMessage);
 				this.restartMessage.dispose();
 			}
 		}
 		
 		if (this.timeMessage != null) {
 			
 			synchronized (this) {
 				
 				this.camera.removeHud(this.timeMessage);
 				this.timeMessage.dispose();
 			}
 		}
 		
 		if (this.finishMessage != null) {
 			
 			synchronized (this) {
 				
 				this.camera.removeHud(this.finishMessage);
 				this.finishMessage.dispose();
 			}
 		}
 		
 		if (this.recordMessage != null) {
 			
 			synchronized (this) {
 				
 				this.camera.removeHud(this.recordMessage);
 				this.recordMessage.dispose();
 			}
 		}
 	}
 }
