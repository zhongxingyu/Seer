 package main;
 
 import java.awt.Rectangle;
 
 import org.lwjgl.input.Keyboard;
 
 /**
  * Entity super class for all entities within the game.<p>
  *
  * Movement, collision detection, texture binding...
  * 
  */
 public abstract class Entity {
 
 	private int x; // x coordinate of entity
 	private int y; // y coordinate of entity
 
 	private int w; // width of entity
 	private int h; // height of entity
 	
 	private int xv; // horizontal velocity of entity
 	private int yv; // vertical velocity of entity
 	
 	private int hp, maxHP; // The health of the entity (health points)
 	private int pp, maxPP; // The power of the entity (power points)
 	
 	/* Allows for continuous movement instead of spamming buttons.
 	 * Increase movementInterval for slower movement through the game.
 	 * Decrease movementInterval for quicker movement through the game.
 	 * 
 	 * This will obviously need to be balanced or redesigned completely.
 	 */
 	private long lastMove = 0;
 	private long movementInterval = 100;
 	
 	/* For collision detection, experimental and unimplemented */
 	private Rectangle o1 = new Rectangle();
 	private Rectangle o2 = new Rectangle();
 	private boolean drawThis = true;
 
 	/**
 	 * Entity class constructor. Creates an entity for the game.<p>
 	 * 
 	 * Note: This is hard coded at the moment rather than being how it
 	 * should be. 
 	 * 
 	 * @param x x coordinate for the entity
 	 * @param y y coordinate for the entity
 	 */
 	public Entity(int x, int y) {
 
 		this.x = x;
 		this.y = y;
 		
		this.w = Game.gridSize-1;
		this.h = Game.gridSize-1;
 
 		this.xv = Game.gridSize; // horizontal velocity (x direction)
 		this.yv = Game.gridSize; // vertical velocity (y direction)
 	}
 	
 	/**
 	 * Entity class constructor. Creates an entity for the game.<p>
 	 * 
 	 * Note: This is hard coded at the moment rather than being how it
 	 * should be. 
 	 * 
 	 * @param x x coordinate for the entity
 	 * @param y y coordinate for the entity
 	 * @param w width of the entity
 	 * @param h height of the entity
 	 */
 	public Entity(int x, int y,  int w, int h) {
 
 		this.x = x;
 		this.y = y;
 		
 		this.w = w;
 		this.h = h;
 
 		this.xv = w; // horizontal velocity (x direction)
 		this.yv = h; // vertical velocity (y direction)
 	}
 
 	/**
 	 * Method to move the entity through the game.<p>
 	 * 
 	 * This holds the actual code to move one place up or down. Collision
 	 * detection will probably be done in the subclasses.
 	 * 
 	 * @param keycode Keyboard keyevent for the movement buttons
 	 */
 	public void move(int keycode) {
 
 		switch(keycode) {
 
 			case Keyboard.KEY_W:
 				this.y -= yv;
 				break;
 			case Keyboard.KEY_A:
 				this.x -= xv;
 				break;
 			case Keyboard.KEY_S:
 				this.y += yv;
 				break;
 			case Keyboard.KEY_D:
 				this.x += xv;
 				break;
 		}
 	}
 	
 	/**
 	 * Tests if we are able to move. Basically, has enough time passed
 	 * for us to be able to move forward? If yes, reset our timings and
 	 * move. Otherwise return control.
 	 * 
 	 * @param keycode Keyboard keyevent for the movement buttons
 	 */
 	public void tryToMove(int keycode) {
 
 		if (System.currentTimeMillis() - lastMove < movementInterval) {
 			return;
 		}
 
 		lastMove = System.currentTimeMillis();
 		move(keycode);
 	}
 
 	/**
 	 * Draw our entity to the screen.
 	 * 
 	 */
 	public abstract void draw();
 	
 	public void updateHealth(int x) {
		if (hp+x >= maxHP) {
			hp = maxHP;
		} else if (hp+x <= 0) {
			hp = 0;
			// TODO: Notify death method
		} else {
			hp += x;
		}
 	}
 	public void updatePower(int x) {
		if (pp+x >= maxPP) {
			pp = maxPP;
		} else if (pp+x <= 0) {
			pp = 0;
		} else {
			pp += x;
		}
 	}
 	
 	/**
 	 * Collision detection method between two entities.<p>
 	 * Based on the intersection of two rectangles (java.awt.Rectangle)
 	 * 
 	 * @param entity Instance of Entity class to check for collisions.
 	 * @return True if the two entities are intersecting.
 	 */
 	public boolean collision(Entity entity) {
 		o1.setBounds(this.x, this.y, this.w, this.h);
 		o2.setBounds(	entity.getX(), 
 				entity.getY(), 
 				entity.getW(), 
 				entity.getH());
 
 		return o1.intersects(o2);
 	}
 
 	/**
 	 * Essentially a method to remove an entity from the game by no 
 	 * longer drawing it to the screen
 	 * 
 	 */
 	public void destroy() {
 		
 		drawThis = false;
 	}
 	
 	/**
 	 * Abstract method for what to do if two entities collide.
 	 * Must be implemented by all subclasses.
 	 * 
 	 */
 	public abstract void collided(Entity x);
 
 	/* Getters and setters for required variables from here on out. */
 	
 	public int getX() {
 		return x;
 	}
 
 	public void setX(int x) {
 		this.x = x;
 	}
 
 	public int getY() {
 		return y;
 	}
 
 	public void setY(int y) {
 		this.y = y;
 	}
 
 	public int getW() {
 		return w;
 	}
 
 	public void setW(int w) {
 		this.w = w;
 	}
 
 	public int getH() {
 		return h;
 	}
 
 	public void setH(int h) {
 		this.h = h;
 	}
 	
 	public int getYv() {
 		return yv;
 	}
 	
 	public void setYv(int yv) {
 		this.yv = yv;
 	}
 
 	public int getXv() {
 		return xv;
 	}
 
 	public void setXv(int xv) {
 		this.xv = xv;
 	}
 
 	public int getHp() {
 		return hp;
 	}
 
 	public void setHp(int hp) {
 		this.hp = hp;
 	}
 
 	public int getPp() {
 		return pp;
 	}
 
 	public void setPp(int pp) {
 		this.pp = pp;
 	}
 
 	public int getMaxHP() {
 		return maxHP;
 	}
 
 	public void setMaxHP(int maxHP) {
 		this.maxHP = maxHP;
 	}
 
 	public int getMaxPP() {
 		return maxPP;
 	}
 
 	public void setMaxPP(int maxPP) {
 		this.maxPP = maxPP;
 	}
 
 	public boolean isDrawThis() {
 		return drawThis;
 	}
 
 	public void setDrawThis(boolean drawThis) {
 		this.drawThis = drawThis;
 	}
 	
 }
