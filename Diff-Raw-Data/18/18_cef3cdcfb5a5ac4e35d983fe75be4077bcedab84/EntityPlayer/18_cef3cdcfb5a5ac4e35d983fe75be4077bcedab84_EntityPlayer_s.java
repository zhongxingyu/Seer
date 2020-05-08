 package me.Gugino.adventure.Entities;
 
 import org.newdawn.slick.Animation;
 import org.newdawn.slick.Color;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.SpriteSheet;
 
 public class EntityPlayer extends Entity {
 
 	private static final int DOWN = 1;
 	private static final int LEFT = 2;
 	private static final int RIGHT = 3;
 	private static final int UP = 4;
 
 	protected static int width = 48;
 	protected static int height = 96;
 
 	protected float speed = 2f;
 
 	protected float dx = 0, dy = 0;
 	protected int tx = 0, ty = 0;
 
 	boolean moving = false;
 
 	public int direction = DOWN;
 
 	public Animation[] anims = new Animation[5];
 
 	public EntityPlayer(int x, int y) {
 		super(x, y);
 		Init();
 	}
 
 	@Override
 	public void Init() {
 		System.out.println("Player spawned!");
 
 		setUpAnims();
 	}
 
 	private void setUpAnims() {
 		SpriteSheet ss;
 		try {
 			ss = new SpriteSheet("res/images/strips/male_walk_down.png", 48, 96);
 			anims[DOWN] = new Animation(ss, 150);
 
			ss = new SpriteSheet("res/images/strips/male_walk_down.png", 48, 96);
 			anims[LEFT] = new Animation(ss, 150);
 
			ss = new SpriteSheet("res/images/strips/male_walk_down.png", 48, 96);
 			anims[RIGHT] = new Animation(ss, 150);
 
 			ss = new SpriteSheet("res/images/strips/male_walk_up.png", 48, 96);
 			anims[UP] = new Animation(ss, 150);
 
 			for (int i = 1; i < anims.length; i++) {
 				anims[i].setPingPong(true);
 				anims[i].stop();
 				anims[i].setCurrentFrame(1);
 			}
 		} catch (SlickException e) {
 			e.printStackTrace();
 		}
 
 	}
 
 	public void update(int delta, Input input) {
 		move(delta, input);
 	}
 
 	private void move(int delta, Input input) {
 
 		if (!moving) {
 
 			tx = (int) x;
 			ty = (int) y;
 
 			if (input.isKeyDown(Input.KEY_W)) {
 				dy = -speed;
 				ty = (int) (y - 48);
 				direction = UP;
 				moving = true;
 				anims[direction].start();
 			} else if (input.isKeyDown(Input.KEY_S)) {
 				dy = speed;
 				ty = (int) (y + 48);
 				direction = DOWN;
 				moving = true;
 				anims[direction].start();
			}

			if (input.isKeyDown(Input.KEY_A)) {
 				dx = -speed;
 				tx = (int) (x - 48);
 				direction = LEFT;
 				moving = true;
 				anims[direction].start();
 			} else if (input.isKeyDown(Input.KEY_D)) {
 				dx = speed;
 				tx = (int) (x + 48);
 				direction = RIGHT;
 				moving = true;
 				anims[direction].start();
 			}
 		} else { // Clean this shit up later
 			x += dx;
 			y += dy;
 
 			if (dy > 0 && y > ty) {
 				if (input.isKeyDown(Input.KEY_S)) {
 					ty += 48;
 				} else {
 					y = ty;
 					dy = 0;
 					moving = false;
 					anims[direction].stop();
 					anims[direction].setCurrentFrame(1);
 				}
 
 			} else if (dy < 0 && y < ty) {
 				if (input.isKeyDown(Input.KEY_W)) {
 					ty -= 48;
 				} else {
 					y = ty;
 					dy = 0;
 					moving = false;
 					anims[direction].stop();
 					anims[direction].setCurrentFrame(1);
 				}
 			}
 
 			if (dx > 0 && x > tx) {
 				if (input.isKeyDown(Input.KEY_D)) {
 					tx += 48;
 				} else {
 					x = tx;
 					dx = 0;
 					moving = false;
 					anims[direction].stop();
 					anims[direction].setCurrentFrame(1);
 				}
 			} else if (dx < 0 && x < tx) {
 				if (input.isKeyDown(Input.KEY_A)) {
 					tx -= 48;
 				} else {
 					x = tx;
 					dx = 0;
 					moving = false;
 					anims[direction].stop();
 					anims[direction].setCurrentFrame(1);
 				}
 			}
 		}
 
 	}
 
 	public void render(Graphics g) {
 		// g.setColor(Color.white);
 		// g.fillRect(x + 1, y + 1, width - 2, height - 2);
 
 		anims[direction].draw(x, y);
 	}
 
 }
