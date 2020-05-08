 package game;
 
 import java.awt.Color;
 import java.awt.Graphics2D;
 import java.awt.Rectangle;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.image.BufferedImage;
 import java.util.ArrayList;
 import java.util.List;
 
 public class Player implements GameObject, KeyListener{
 
 	BufferedImage sprite;
 	public float x;
 	public float y;
 	public float yspeed = 0;
 	private Rectangle hitbox;
 	private boolean inAir = false;
 	private int maxJumpTime = 12;
 	private int jumpTime = maxJumpTime;
 	private float gravity = 6;
 	private float jumpSpeed = 20;
 	private boolean holdingButton = false;
 	
 	private int jumpButton = java.awt.event.KeyEvent.VK_SPACE;
 	
 	SpriteSheet spriteSheet;
 	
 	public Player(){
 		spriteSheet = new SpriteSheet();
 		sprite = spriteSheet.getSprite(0, 0);
 		x = 0;
 		y = 240;
 		hitbox = new Rectangle((int)x,(int)y-sprite.getHeight(),
 				sprite.getWidth(),sprite.getHeight());
 	}
 	
 	public void setPosition(float x, float y) {
 		this.x = x;
 		this.y = y;
 	}
 	
 	public Rectangle getHitbox() {
 		return hitbox;
 	}
 	
 	@Override
 	public void update(float tpf) {
 		if (inAir) {
 			if (holdingButton) {
 				jumpTime -= tpf;
 				yspeed = -jumpSpeed;
 				if (jumpTime <= 0) {
 					holdingButton = false;
 				}
 				
 			}
 			else {
 				yspeed += gravity*tpf;
 			}
 			y += yspeed*tpf;
 			if (y > 240) {
 				y = 240;
 				inAir = false;
 				yspeed = 0;
 			}
 		}
 		//update collision box
		hitbox.y = (int)y+sprite.getHeight();
 		
 	}
 	@Override
 	public void render(Graphics2D g) {
 		g.drawImage(sprite, (int)x, (int)y-sprite.getHeight(), null);
 	}
 	@Override
 	public void keyPressed(KeyEvent event) {
 		if (!inAir && event.getKeyCode() == jumpButton) {
 			yspeed = -jumpSpeed;
 			inAir = true;
 			holdingButton = true;
 			jumpTime = maxJumpTime;
 		}
 	}
 	@Override
 	public void keyReleased(KeyEvent event) {
 		if (event.getKeyCode() == jumpButton) {
 			holdingButton = false;
 			
 		}
 	}
 	@Override
 	public void keyTyped(KeyEvent arg0) {
 		//Not needed
 	}	
 }
