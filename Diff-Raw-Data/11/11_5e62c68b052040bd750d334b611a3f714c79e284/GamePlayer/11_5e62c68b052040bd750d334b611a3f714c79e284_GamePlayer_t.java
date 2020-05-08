 package de.futjikato.mrwhiz.game;
 
 import java.util.List;
 
 import org.newdawn.slick.Animation;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.Renderable;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.SpriteSheet;
 
 import de.futjikato.mrwhiz.App;
 import de.futjikato.mrwhiz.Util;
 import de.futjikato.mrwhiz.game.events.CallbackEvent;
 import de.futjikato.mrwhiz.game.inventory.Inventory;
 import de.futjikato.mrwhiz.game.inventory.Tool;
 
 public class GamePlayer extends GamePhysicalObject {
 
 	private GameUi gui;
 
 	private static final int PLAYER_WIDTH = 30;
 	private static final int PLAYER_HEIGHT = 149;
 
 	private float spawnX;
 	private float spawnY;
 	private int blocksize;
 
 	private static SpriteSheet PLAYER_SPRITE;
 
 	private boolean longJump = false;
 	private boolean jumpKeyPressed = true;
 
 	private static final float BASE_SPEED = 0.5f;
 	private float speed;
 	private long lastMovment;
 
 	private static final int START_HEALTH = 100;
 	private int health;
 	private boolean alive = true;
 	private int lives;
 	private boolean invincible = false;
 
 	private static Animation ANIMATION_WALK_LEFT;
 	private static Animation ANIMATION_WALK_RIGHT;
 
 	private Renderable activeRenderable;
 
 	private int score;
 
 	private Inventory inventory;
 
 	static {
 		// deactivate image loading in unit test
 		if (!App.getInstance().isUnittest()) {
 			try {
 				PLAYER_SPRITE = new SpriteSheet("resources/images/player_game.png", 80, 149, 1);
 			} catch (SlickException e) {
 				e.printStackTrace();
 			}
 
 			try {
 				ANIMATION_WALK_LEFT = new Animation(new Image[] { PLAYER_SPRITE.getSprite(0, 2), PLAYER_SPRITE.getSprite(0, 3), PLAYER_SPRITE.getSprite(0, 4), PLAYER_SPRITE.getSprite(0, 5), PLAYER_SPRITE.getSprite(0, 6) }, 180, true);
 				ANIMATION_WALK_RIGHT = new Animation(new Image[] { PLAYER_SPRITE.getSprite(0, 1) }, 150, true);
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	public GamePlayer(float spawnx, float spawny, int blocksize) {
 		this.spawnX = spawnx;
 		this.spawnY = spawny;
 		this.setX(spawnx);
 		this.setY(spawny);
 		this.blocksize = blocksize;
 
 		this.setGrip(1.2f);
 		this.setMaxYVal(1.5f);
 
 		this.speed = BASE_SPEED;
 		this.health = START_HEALTH;
 		this.lives = 5;
 
 		inventory = new Inventory(this);
 
 		// deactivate image loading in unit test
 		if (!App.getInstance().isUnittest()) {
 			this.activeRenderable = PLAYER_SPRITE.getSprite(0, 0);
 		}
 	}
 
 	public float getSpeed() {
 		return speed;
 	}
 
 	public void setBlockSpeed(float speed) {
 		this.speed = BASE_SPEED + speed;
 	}
 
 	public void damage(int dmg) {
 
 		if (this.invincible) {
 			return;
 		}
 
 		this.invincible = true;
 		CallbackEvent reinvincible = new CallbackEvent(new Runnable() {
 			@Override
 			public void run() {
 				GamePlayer.this.invincible = false;
 			}
 		}, 1);
 		GameTimeTrigger.getInstance().addEvent(reinvincible);
 
 		this.health -= dmg;
 
 		if (this.health <= 0) {
 			this.die();
 		}
 	}
 
 	private void die() {
 		if (this.alive) {
 			this.alive = false;
 
 			// decrement lives and check for game over
 			if (--this.lives <= 0) {
 				this.gui.handleGameOver();
 			} else {
 				// add callback event to trigger respawn after 5 seconds
 				CallbackEvent respawn = new CallbackEvent(new Runnable() {
 					@Override
 					public void run() {
 						GamePlayer.this.respawn();
 					}
 				}, 5);
 				GameTimeTrigger.getInstance().addEvent(respawn);
 			}
 		}
 	}
 
 	public void respawn() {
 		this.setX(this.spawnX);
 		this.setY(this.spawnY);
 		this.alive = true;
 		this.health = START_HEALTH;
 	}
 
 	public void render(float vpx, float vpy) {
 		activeRenderable.draw(this.getX() - vpx - (GamePlayer.PLAYER_WIDTH / 2), this.getY() - vpy - GamePlayer.PLAYER_HEIGHT);
 	}
 
 	public void handleInput(long delta, Input input) {
 		if (!this.alive)
 			return;
 
 		// get new y position
 		this.calcNewPos(this.blocksize, delta);
 
 		if (input.isKeyDown(Input.KEY_D)) {
 			this.setXvel(this.speed);
 			activeRenderable = ANIMATION_WALK_RIGHT;
 		} else {
 			if (!input.isKeyDown(Input.KEY_A)) {
 				this.activeRenderable = PLAYER_SPRITE.getSprite(0, 1);
 			}
 		}
 
 		if (input.isKeyDown(Input.KEY_A)) {
 			this.setXvel(-this.speed);
 			activeRenderable = ANIMATION_WALK_LEFT;
 		} else {
 			if (!input.isKeyDown(Input.KEY_D)) {
 				this.activeRenderable = PLAYER_SPRITE.getSprite(0, 2);
 			}
 		}
 
 		// TODO implement active tool
 		// TODO only use active tool
 		if (input.isKeyDown(Input.KEY_E)) {
 			List<Tool> tools = inventory.getInventory();
 			for ( Tool cTool : tools ) {
 				cTool.use(this);
 			}
 		}
 
 		if (input.isKeyDown(Input.KEY_SPACE)) {
 			this.jump();
 		} else {
 			this.jumpKeyPressed = false;
 		}
 
 		if (this.getXvel() == 0 && this.getYVel() == 0) {
 			if (this.lastMovment < (Util.getTime() - 2000)) {
 				this.activeRenderable = PLAYER_SPRITE.getSprite(0, 0);
 			}
 		} else {
 			this.lastMovment = Util.getTime();
 		}
 	}
 
 	public void jump() {
 		if (this.getYVel() == 0) {
 			this.setYVel(-1.2f);
 			this.longJump = false;
 			this.jumpKeyPressed = true;
 		} else if (this.getYVel() > -0.7 && this.getYVel() < -0.5 && this.jumpKeyPressed && !this.longJump) {
 			this.setYVel(-0.9f);
 			this.longJump = true;
 		}
 	}
 
 	public int getHeight() {
 		return PLAYER_HEIGHT;
 	}
 
 	public int getWidth() {
 		return PLAYER_WIDTH;
 	}
 
 	@Override
 	protected void hitBlock(Block block, int type) {
 
 		if (!this.alive)
 			return;
 
 		if (block instanceof Item) {
 			Item item = (Item) block;
 			score += item.getScore();
 
 			Tool itemTool = item.getTool();
 			if (itemTool != null) {
 				inventory.addItem(itemTool);
				item.setDoRender(false);
 			}
 		}
 
 		block.triggerPlayerTouch();
 
 		int dmg = block.getDamage();
 		if (dmg > 0) {
 			this.damage(dmg);
 		}
 
 		float speed = block.getSpeed();
 		this.setBlockSpeed(speed);
 	}
 
 	public int getScore() {
 		return this.score;
 	}
 
 	public int getLives() {
 		return this.lives;
 	}
 
 	public int getHealth() {
 		return this.health;
 	}
 
 	public Inventory getInventory() {
 		return inventory;
 	}
 
 	public void setGui(GameUi gui) {
 		this.gui = gui;
 	}
 }
