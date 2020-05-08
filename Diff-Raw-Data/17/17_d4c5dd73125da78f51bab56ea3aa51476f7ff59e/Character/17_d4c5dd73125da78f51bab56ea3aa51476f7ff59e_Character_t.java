 package characters;
 
 import java.awt.Graphics;
 
 import map.Tile;
 
 public class Character {
 	private final double speed = 3.5;
 	private boolean running = false;
 	private double currentSpeed = 3.5;
 
 	public static final int CHAR_WIDTH = 32;
 	public static final int CHAR_HEIGHT = 60;
 	public static final double hungerRate = 0.01;
 	private double x;
 	private double y;
 	private Hitbox feetHitbox;
 	private Hitbox bodyHitbox;
 	private int health;
 	private double hunger;
 	private double stamina;
 
 	private Sprite img;
 
 	private int spriteStage;
 	private int direction; // 0 = down, 1 = left, 2 = up, 3 = right
 	private boolean movingDown;
 	private boolean movingLeft;
 	private boolean movingUp;
 	private boolean movingRight;
 
 	public Character(int x, int y) {
 		this.x = x;
 		this.y = y;
 		health = 100;
 		hunger = 100;
 		stamina = 100;
 		feetHitbox = new Hitbox(x, y + 40, CHAR_WIDTH - 9, CHAR_HEIGHT - 40);
 		bodyHitbox = new Hitbox(x, y, CHAR_WIDTH, CHAR_HEIGHT);
 		img = new Sprite("Sprites/Character.png");
 		direction = 3;
 		spriteStage = 0;
 	}
 
 	public void move(Tile tiles[][]) {
 
 		if (running) {
 			currentSpeed = 2 * speed;
		}else{
		    currentSpeed = speed;
 		}
 
 		if (movingLeft) {
 			this.x -= currentSpeed;
 			direction = 1;
 			if (spriteStage >= 1 && spriteStage < 5)
 				spriteStage++;
 			else
 				spriteStage = 1;
 		}
 		if (movingUp) {
 			this.y -= currentSpeed;
 			direction = 2;
 			if (spriteStage >= 1 && spriteStage < 5)
 				spriteStage++;
 			else
 				spriteStage = 1;
 		}
 		if (movingRight) {
 			this.x += currentSpeed;
 			direction = 3;
 			if (spriteStage >= 1 && spriteStage < 5)
 				spriteStage++;
 			else
 				spriteStage = 1;
 		}
 		if (movingDown) {
 			this.y += currentSpeed;
 			direction = 0;
 			if (spriteStage >= 1 && spriteStage < 5)
 				spriteStage++;
 			else
 				spriteStage = 1;
 		}
 
 		if (!movingUp && !movingRight && !movingLeft && !movingDown)
 			spriteStage = 0;
 
 		feetHitbox.setX((int) this.x);
 		feetHitbox.setY((int) this.y + 40);
 
 		while (checkFeetCollision(tiles)) {
 			if (movingLeft)
 				this.x += speed;
 			feetHitbox.setX((int) this.x);
 			feetHitbox.setY((int) this.y + 40);
 			if (movingRight)
 				this.x -= speed;
 			feetHitbox.setX((int) this.x);
 			feetHitbox.setY((int) this.y + 40);
 			if (movingUp)
 				this.y += speed;
 			feetHitbox.setX((int) this.x);
 			feetHitbox.setY((int) this.y + 40);
 			if (movingDown)
 				this.y -= speed;
 			feetHitbox.setX((int) this.x);
 			feetHitbox.setY((int) this.y + 40);
 		}
 	}
 
 	public boolean checkFeetCollision(Tile[][] tiles) {
 		for (int h = 0; h < tiles.length; h++) {
 			for (int w = 0; w < tiles[0].length; w++) {
 				if (tiles[h][w].getHitbox() != null
 						&& getFeetHitbox().collisionCheck(
 								tiles[h][w].getHitbox())) {
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 
 	public int getX() {
 		return (int) x;
 	}
 
 	public int getY() {
 		return (int) y;
 	}
 
 	public int getHealth() {
 		return health;
 	}
 
 	public double getHunger() {
 		return hunger;
 	}
 
 	public double getStamina() {
 		return stamina;
 	}
 	
 	public boolean getRunning(){
 		return running;
 	}
 	
 	public void setRunning(boolean set){
 		running = set;
 	}
 
 	public void damageHealth(int damage) {
 		health = health - damage;
 		if (health > 100) {
 			health = 100;
 		}
 		if (health < 0) {
 			health = 0;
 		}
 	}
 
 	public void damageHunger(int damage) {
 		hunger = hunger - damage;
 		if (hunger < 0) {
 			hunger = 0;
 		}
 	}
 
 	public void naturalHunger() {
 		hunger = hunger - hungerRate;
 		if (stamina >= hunger) {
 			stamina = hunger;
 		}
 	}
 
 	public void damageStamina(double damage) {
 		stamina = stamina - damage;
 		if (stamina < 0) {
 			stamina = 0;
 		}
 	}
 
 	public String toString() {
 		return "X: " + x + " Y: " + y;
 	}
 
 	public void draw(Graphics g) {
 		img.draw(g, (int) x, (int) y, CHAR_WIDTH, CHAR_HEIGHT, spriteStage,
 				direction);
 	}
 
 	public Hitbox getFeetHitbox() {
 		return feetHitbox;
 	}
 
 	public void setMovingRight(boolean movingRight) {
 		this.movingRight = movingRight;
 	}
 
 	public void setMovingUp(boolean movingUp) {
 		this.movingUp = movingUp;
 	}
 
 	public void setMovingLeft(boolean movingLeft) {
 		this.movingLeft = movingLeft;
 	}
 
 	public void setMovingDown(boolean movingDown) {
 		this.movingDown = movingDown;
 	}
 
 }
