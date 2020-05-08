 package com.GauchoSpace.Players;
 
 import java.util.Collection;
 import java.util.Iterator;
 
 import org.newdawn.slick.Color;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.geom.Vector2f;
 import org.newdawn.slick.state.StateBasedGame;
 
 import com.GauchoSpace.GameField;
 import com.GauchoSpace.IBullet;
 import com.GauchoSpace.ICharacter;
 
 public class TestPlayer implements ICharacter {
 	private GameField field;
 	private Image sprite;
 	
 	private boolean invincible;
 	private boolean slow;
 	
 	private Vector2f pos;
 	private int radius;
 	
 	private int speed_fast;
 	private int speed_slow;
 	
 	private int ticksToNextShot;
 	
 	private Vector2f deathPosition;
 	private int ticksToRevival;
 	
 	public TestPlayer(GameField field) throws SlickException {
 		this.field = field;
 		sprite = new Image("res/playership.png");
 		invincible = false;
 		slow = false;
 		pos = new Vector2f();
 		radius = 3;
 		speed_fast = 5;
 		speed_slow = 2;
 		ticksToNextShot = 0;
 	}
 	
 	@Override
 	public Vector2f getPos() {
 		return pos.copy();
 	}
 	
 	@Override
 	public void setPos(Vector2f pos) {
 		this.pos = pos;
 	}
 	
 	@Override
 	public int getRadius() {
 		return radius;
 	}
 	
 	@Override
 	public int getHealth() {
 		return 1;
 	}
 	@Override
 	public boolean getInvincibility() {
 		return invincible;
 	}
 	
 	@Override
 	public void tookDamage(int damage) {
 		deathPosition = pos.copy();
		ticksToRevival = 180;
 		invincible = true;
 	}
 	
 	@Override
 	public void render(GameContainer gc, StateBasedGame game, Graphics graphics) {
		if (ticksToRevival > 120) {
			int radius = (180 - ticksToRevival) * 20;
 			graphics.setColor(Color.white);
 			graphics.drawArc(deathPosition.x - radius, deathPosition.y - radius, radius * 2, radius * 2, 0, 360);
 			return;
 		}
 		
 		int x = (int)pos.x;
 		int y = (int)pos.y;
 		
 		sprite.drawCentered(x, y);
 		
 		if (slow) {
 			// TODO let's just make this an image
 			graphics.setColor(Color.red);
 			graphics.fillArc(x - radius - 1, y - radius - 1, radius * 2 + 2, radius * 2 + 2, 0, 360);
 			graphics.setColor(Color.white);
 			graphics.fillArc(x - radius, y - radius, radius * 2, radius * 2, 0, 360);
 		}
 	}
 	
 	@Override
 	public void update(GameContainer gc, StateBasedGame game, int delta) {
 		// handle death
 		if (ticksToRevival > 0) {
 			ticksToRevival--;
 			
 			if (ticksToRevival > 0){
 				if ((ticksToRevival % 8) > 4) sprite.setAlpha(.4f);
 				else sprite.setAlpha(1f);
 			}
			if (ticksToRevival > 120) {
				int radius = (180 - ticksToRevival) * 20;
 				Iterator<IBullet> i = field.getEnemyBullets().iterator();
 				while (i.hasNext()) {
 					IBullet bullet = i.next();
 					if (deathPosition.distanceSquared(bullet.getPos()) <= radius*radius) {
 						i.remove();
 					}
 				}
 				return;
			} else if (ticksToRevival > 60) {
				pos = new Vector2f(field.getWidth() / 2, field.getHeight() - (120 - ticksToRevival) * 3);
 				return;
 			} else if (ticksToRevival == 0) {
 				invincible = false;
 			}
 		}
 		
 		// check keys
 		Input input = gc.getInput();
 		
 		// slow toggle
 		slow = input.isKeyDown(Input.KEY_LSHIFT);
 		int speed = (slow ? speed_slow : speed_fast);
 		
 		// movement
 		Vector2f vel = new Vector2f(0, 0);
 		if (input.isKeyDown(Input.KEY_LEFT)) vel.x -= 1;
 		if (input.isKeyDown(Input.KEY_RIGHT)) vel.x += 1;
 		if (input.isKeyDown(Input.KEY_UP)) vel.y -= 1;
 		if (input.isKeyDown(Input.KEY_DOWN)) vel.y += 1;
 		pos.add(vel.normalise().scale(speed));
 		
 		// shooting
 		if (ticksToNextShot > 0) ticksToNextShot--;
 		if (ticksToNextShot <= 0 && input.isKeyDown(Input.KEY_Z)) {
 			if (slow) {
 				field.addPlayerBullet(new PlayerBullet1(field, pos.x, pos.y, 10, 10, 5, Color.orange));
 				ticksToNextShot = 10;
 			} else {
 				field.addPlayerBullet(new PlayerBullet1(field, pos.x - 8, pos.y, 15, 5, 1, Color.yellow));
 				field.addPlayerBullet(new PlayerBullet1(field, pos.x + 8, pos.y, 15, 5, 1, Color.yellow));
 				ticksToNextShot = 5;
 			}
 		}
 		
 		// clamps
 		pos.x = Math.min(Math.max(pos.x, 12), field.getWidth() - 12);
 		pos.y = Math.min(Math.max(pos.y, 20), field.getHeight() - 20);
 	}
 
 	@Override
 	public boolean isDeletable() {
 		return false;
 	}
 
 	@Override
 	public void destroy() {
 	}
 }
 
 class PlayerBullet1 implements IBullet {
 	private GameField field;
 	private Vector2f pos;
 	private float speed;
 	private float width;
 	private int damage;
 	private Color color;
 	private boolean deletable;
 	
 	public PlayerBullet1(GameField field, float x, float y, float speed, float width, int damage, Color color) {
 		this.field = field;
 		this.pos = new Vector2f(x, y);
 		this.speed = speed;
 		this.width = width;
 		this.damage = damage;
 		this.color = color;
 	}
 	
 	@Override
 	public int getDamage() {
 		return damage;
 	}
 	
 	@Override
 	public boolean isDeletable() {
 		return deletable;
 	}
 
 	@Override
 	public boolean isColliding(ICharacter character) {
 		float totalRadius = width + character.getRadius();
 		boolean colliding = character.getPos().distanceSquared(pos) <= totalRadius * totalRadius;
 		if (colliding) deletable = true;
 		return colliding;
 	}
 
 	@Override
 	public void render(GameContainer gc, StateBasedGame game, Graphics graphics) {
 		graphics.setColor(color);
 		graphics.fillRoundRect((int)(pos.x - 0.5 * width), (int)(pos.y - 2.5 * width), width, width * 5, (int)width);
 	}
 
 	@Override
 	public void update(GameContainer gc, StateBasedGame game, int delta) {
 		pos.add(new Vector2f(0, -speed));
 		if (pos.y + width * 5 < 0) deletable = true;
 	}
 
 	@Override
 	public Vector2f getPos() {
 		return pos.copy();
 	}
 
 	@Override
 	public void onCollision(ICharacter target) {
 		deletable = true;
 	}
 
 	@Override
 	public void destroy() {
 	}
 }
