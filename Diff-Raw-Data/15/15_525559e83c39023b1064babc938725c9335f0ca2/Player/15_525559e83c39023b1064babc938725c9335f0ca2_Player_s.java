 package org.gaem.bodies;
 
 import org.gaem.ObjectManager;
 import org.jsfml.graphics.Color;
 import org.jsfml.graphics.RenderStates;
 import org.jsfml.graphics.RenderTarget;
 import org.jsfml.graphics.Texture;
 import org.jsfml.system.Vector2f;
 import org.omg.CORBA.TRANSACTION_MODE;
 
 public class Player extends TexturedBody {
 	//FIXME СРАНЫЙ БАРДАК
 	
 	private Vector2f a = new Vector2f(0, 5);
 	
 	private boolean isRunning;
 	private boolean isLocked;
 	
 	public Player(Vector2f position, Vector2f size, Texture texture) {
 		super(position, size, texture);
 	}
 	
 	public Player(Vector2f position, Texture texture) {
 		super(position, new Vector2f(0, 0));
 		collidable = true;
 		setTexture(texture);
 		fitCanvasToTexture();
 	}
 	
 	public void lock() {
 		isLocked = true;
 		setOutlineColour(Color.WHITE);
 		System.err.println("* Locked *");
 		System.out.println("v: " + v);
 		System.out.println("Collision: " + objectManager.getCollision(this));
 		System.out.println("On ground: " + isOnGround);
 	}
 	
 	public void unlock() {
 		isLocked = false;
 		setOutlineColour(Color.BLACK);
 		System.err.println("* Unlocked *");
 		System.out.println("pos: " + position);
 		System.out.println("spd: " + v);
 	}
 	
 	public void setObjectManager(ObjectManager objects) {
 		this.objectManager = objects;
 	}
 	
 	public void speedUp(float vx, float vy) {
 		v = new Vector2f(vx + v.x, vy + v.y);
 		isRunning = true;
 	}
 	
 	public void run(float speed) {
 		v = new Vector2f(speed, v.y);
 		isRunning = true;
 	}
 	
 	@Override
 	public void update(float dt) {
 		fancyBeamUpdate(dt);
 		if (isLocked) {
 			return;
 		}
 		
 		v = Vector2f.add(v, a);
 		move(v.x * dt, v.y * dt);
 		
 		if (isOnGround) {
 			if (v.x != 0) {
				if (v.x > 1) {
					v = new Vector2f(v.x/2, v.y);
 				} else {
 					v = new Vector2f(0, v.y);
 				}
 			}
 		}
 		
 		shift(0, 1);
 		if (objectManager.checkCollision(this)) {
 			isOnGround = true;
 		} else {
 			isOnGround = false;
 		}
 		shift(0, -1);
		/*if (Math.abs(v.x) < 1) {
			v = new Vector2f(0, v.y);
		}
		
		if (Math.abs(v.y) < 1) {
			v = new Vector2f(v.x, 0);
		}*/
 	}
 	
 	@Override
 	public void move(float dx, float dy) {	
 		super.move(dx, dy);
 		sprite.setPosition(position.x, position.y);
 	}
 	
 	@Override
 	public void draw(RenderTarget target, RenderStates states) {
 		drawBoundingBox(target, states);
 	}
 	
 	public void jump() {
 		if (isOnGround) {
			v = new Vector2f(v.x, v.y - 440);
 			isOnGround = false;
 		}
 	}
 }
