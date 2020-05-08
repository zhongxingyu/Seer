 package net.techythomas.game.ResourceDefender.entities;
 
 import java.util.ArrayList;
 
 import net.techythomas.game.ResourceDefender.World;
 import net.techythomas.game.ResourceDefender.control.Keyboard;
 import net.techythomas.game.ResourceDefender.projectiles.Bullet;
 
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.geom.Rectangle;
 
 public class Player {
 	
 	private World world;
 	private Keyboard keyboard;
 	public static Rectangle rect;
 	private static float width;
 	private static float height;
 	public static float defaultGround;
 	public static float ground;
 	public static float x = 30;
 	public static float y = 30 ;
 	private Image image;
 	private float movementSpeed = 0.7f;
 	
 	public boolean allowMoveUp = true;
 	public boolean allowMoveDown = true;
 	public boolean allowMoveRight = true;
 	public boolean allowMoveLeft = true;
	public boolean cannotMoveDiagonalRight = false;
	public boolean cannotMoveDiagonalLeft = false;
 	public int FACING = 3;
 	
 	public static boolean isJumping  = false;
 	public boolean isColliding = false;
 	public boolean isOnGround = false;
 	public static boolean hasWeapon = false;
 	public static ArrayList bullets;
 	
 	private float gravity = 0.6f;
 	
 	public Player(Image image, float width, float height) throws SlickException {
 		this.image = image;
 		this.width = width;
 		this.height = height;
 		rect = new Rectangle(width, height, x, y);
 		bullets = new ArrayList();
 		keyboard = new Keyboard();
 		world = new World();
 		defaultGround = world.getHeight() - (height + 25);
 		ground = world.getHeight() - (height + 25);
 	}
 	
 	public static ArrayList getBullets() {
 		return bullets;
 	}
 	
 	public static void fire() throws SlickException {
 		Bullet bullet = new Bullet(rect.getX(), rect.getY());
 		bullets.add(bullet);
 	}
 	
 	public Rectangle getBounds() {
 		return new Rectangle(x, y, width, height);
 	}
 	
 	public void update(GameContainer container) throws SlickException {
 		Input input = container.getInput();
 		
 		if (y >= defaultGround) {
 			isOnGround = true;
 			
 		}
 		else {
 			isOnGround = false;
 		}
 		
 		if (hasWeapon) {
 			image = new Image("res/player_weapon.png");
 		}
 		
 		if (y < ground) {
 			//y += gravity;
 		}
 		
 		if (y >= ground || isColliding) {
 			isJumping = false;
 		}
 		
 		if ((input.isKeyDown(input.KEY_W) || input.isKeyDown(input.KEY_UP)) && (y > 20) && allowMoveUp) {
 			setY(y -= movementSpeed);
 			isJumping = true;
 			FACING = 0;
 		}
 		else if ((input.isKeyDown(input.KEY_S) || input.isKeyDown(input.KEY_DOWN)) && (y < ground) && allowMoveDown) {
 			setY(y += movementSpeed);
 			FACING = 1;
 		}
		if ((input.isKeyDown(input.KEY_A) || input.isKeyDown(input.KEY_LEFT)) && (x > 20) && allowMoveLeft && !cannotMoveDiagonalLeft) {
 			setX(x -= movementSpeed);
 			FACING = 2;
 		}
		else if ((input.isKeyDown(input.KEY_D) || input.isKeyDown(input.KEY_RIGHT)) && x < (container.getWidth() - (width + 20)) && allowMoveRight && !cannotMoveDiagonalRight) {
 			setX(x += movementSpeed);
 			FACING = 3;
 		}
 		if (input.isKeyPressed(input.KEY_SPACE)) {
 			if (hasWeapon) {
 				fire();
 			}
 			
 		}
 	}
 	
 	public static float getX() {
 		return x;
 	}
 	
 	public static float getMaxX() {
 		return rect.getMaxX();
 	}
 	
 	public static float getY() {
 		return y;
 	}
 	
 	public static float getWidth() {
 		return width;
 	}
 	
 	public static float getHeight() {
 		return height;
 	}
 	
 	public static void setX(float x) {
 		rect.setX(x);
 	}
 	
 	public static void setY(float y) {
 		rect.setY(y);
 	}
 	
 	public void render() throws SlickException {
 		if (FACING == 0) {
 			image = new Image("res/player_up.png");
 		}
 		else if (FACING == 1) {
 			image = new Image("res/player_down.png");
 		}
 		else if (FACING == 2) {
 			image = new Image("res/player_left.png");
 		}
 		else if (FACING == 3) {
 			image = new Image("res/player_right.png");
 		}
 		image.draw(x, y, width, height);
 	}
 	
 	
 
 }
