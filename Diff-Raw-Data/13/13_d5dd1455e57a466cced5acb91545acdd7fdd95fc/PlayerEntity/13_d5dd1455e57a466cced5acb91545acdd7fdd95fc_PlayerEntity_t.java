 package org.vergeman.sulfonicavenger;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 
 import org.lwjgl.Sys;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.KeyListener;
 
 public class PlayerEntity extends Entity implements KeyListener {
 	boolean alive, can_collide;
 	boolean game_over;
 	int lives = 3;
 	int spawn_x, spawn_y;
 
 	protected int move_speed = 300;
 
 	protected ShotEntity[] shots;
 	protected int shot_index = 0;
 	long last_shot;
 	long SHOT_INTERVAL = 115;
 	int PAUSE_SPEED = 1000;
 	int pause_counter = 0;
 
 	ArrayList<Animator> animators;
 	AssetManager assetManager;
 
 	Gamepad gp;
 	
 	protected PlayerEntity(GameContainer container, AssetManager assetManager,
 			Sprite sprite, int x, int y, ArrayList<Animator> animators) {
 		super(sprite, x, y);
 		this.spawn_x = x;
 		this.spawn_y = y;
 
 		this.alive = true;
 		this.can_collide = true;
 		this.game_over = false;
 		this.pause_counter = 0;
 
 		setVerticalMovement(0);
 		setHorizontalMovement(0);
 		resize(container.getWidth(), container.getHeight(), 1.0f, 1.0f);
 		this.assetManager = assetManager;
 		this.animators = animators;
 		
 		this.gp = new Gamepad(container);
 	}
 
 	public void setShots(ShotEntity[] shots) {
 		this.shots = shots;
 	}
 
 	public void move(int delta, ArrayList<MoleculeEntity> molecules) {
 		if (gp.detect()) {
 			if (gp.isControllerLeft()) {
 				setHorizontalMovement(-move_speed);
 			}
 			if (gp.isControllerRight()) {
 				setHorizontalMovement(move_speed);
 			}
 			if (gp.isControllerDown()) {
 				setVerticalMovement(move_speed);
 			}
 			if (gp.isControllerUp()) {
 				setVerticalMovement(-move_speed);
 			}
 			if (gp.isControllerNone()) {
 				setHorizontalMovement(0);
 				setVerticalMovement(0);
 			}
 			if (alive && gp.isButtonPressed()) {
 				shoot();
 			}
 		}
 		
 		if ((dx < 0) && (x < this.BOUNDS_LEFT)) {
 			dx = 0;
 		}
 		if ((dx > 0) && (x > this.BOUNDS_RIGHT)) {
 			dx = 0;
 		}
 		if ((dy < 0) && (y < this.BOUNDS_TOP)) {
 			dy = 0;
 		}
 		if ((dy > 0) && (y > this.BOUNDS_BOTTOM)) {
 			dy = 0;
 		}
 
 		
 
 		if (alive) {
 
 			super.move(delta);
 
 			if (!calculateValidMove(molecules)) {
 				super.unmove(delta);
 			}
 		}
 
 		if (pause_counter >= 0) {
 			pause_counter -= delta;
 		}
 
 		if (!alive && pause_counter < 0) {
 			reinitialize(molecules); // respawn, set to alive (moveable) but not
 							// collidable
 			pause_counter = PAUSE_SPEED + 500; // repause to give user time to
 												// orient
 		} else if (alive && pause_counter <= 0) {
 			can_collide = true;
 		}
 
 	}
 
 	public boolean calculateValidMove(ArrayList<MoleculeEntity> molecules) {
 		for (Iterator<MoleculeEntity> i = molecules.iterator(); i.hasNext();) {
 			MoleculeEntity m = i.next();
 			if (this.collidesWith(m)) {
 				return false;
 			}
 		}
 		return true;
 	}
 
 	@Override
 	// resolution relative
 	public void reposition(int x, int y, float scale_rel) {
 		// bounds
 		this.updateBounds(0, x - this.sprite.getWidth(), 0,
 				y - this.sprite.getHeight());
 
 		// placement - TODO: fucked up needs wrk
 		//this.x = Math.min(Math.abs(this.x * scale_rel), this.BOUNDS_RIGHT);
 		//this.y = Math.min(Math.abs(this.y * scale_rel), this.BOUNDS_BOTTOM);
 
 	}
 
 	public boolean isAlive() {
 		return alive;
 	}
 	public boolean CanCollide() {
 		return can_collide;
 	}
 	
 	public boolean isGameOver() {
 		return game_over;
 	}
 
 	public int paused() {
 		return pause_counter;
 	}
 
 	@Override
 	public void draw() {
 		
 		if (pause_counter <= 0) {
 			super.draw();
 		} 
 		else if (!isAlive()) {
 				//assetManager.getImage("explosion").setAlpha( 5 * pause_counter / PAUSE_SPEED);
 				//assetManager.getImage("explosion").draw(this.x, this.y);
 			}
 		else {
 			/*flash*/
 			switch ((int) (pause_counter / (PAUSE_SPEED / 4))) {
 
 			case 5:
 				break;
 			case 3:
 				break;
 			case 1:
 				break;
 			default:
 				super.draw();
 			}
 		}
 	}
 
 	@Override
 	public void collidedWith(Entity other) {
 		// TODO Auto-generated method stub
 		if (other instanceof CentiBallEntity || other instanceof NH3Entity) {
 
 			if (alive && can_collide) {
 				alive = false;
 				can_collide = false;
 				pause_counter = PAUSE_SPEED;
 				--lives;
 				assetManager.getSound("hit").play();
 			
 				animators.add(new Animator(assetManager.getSpriteSheet("player_explosion"), this.x, this.y,
 						0,0,2,2, true, 150, true));
 				
 				if (lives <= 0) {
 					game_over = true;
 				}
 			}
 		}
 	}
 
 	public void reinitialize(ArrayList<MoleculeEntity> molecules) {
 		//if we spawned into a collision
 		this.x = spawn_x;
 		this.y = spawn_y;
 		this.alive = true;
 
 		for (Iterator<MoleculeEntity> i = molecules.iterator(); i.hasNext();) {
 			MoleculeEntity m = i.next();
 			if (m.collidesWith(this)) {
 				i.remove();
 			}
 		}
 	}
 
 	public void shoot() {
 		// if we are allowed to shoot (in between time)
 		if (!game_over && alive && (Sys.getTime() - last_shot > SHOT_INTERVAL)) {
 			if (!shots[shot_index].isDisplay()) {
				shots[shot_index].reinitalize(this, x, y);
 				shot_index++;
 				shot_index = shot_index % shots.length;
 				last_shot = Sys.getTime();
 				assetManager.getSound("shoot").play();
 			}
 		}
 	}
 
 		
 	@Override
 	public void keyPressed(int key, char c) {
 		switch (key) {
 
 		case Input.KEY_LEFT:
 			setHorizontalMovement(-move_speed);
 			break;
 		case Input.KEY_RIGHT:
 			setHorizontalMovement(move_speed);
 			break;
 		case Input.KEY_SPACE:
 			shoot();
 			break;
 		case Input.KEY_UP:
 			setVerticalMovement(-move_speed);
 			break;
 		case Input.KEY_DOWN:
 			setVerticalMovement(move_speed);
 			break;
 		default:
 			break;
 
 		}
 	}
 
 	@Override
 	public void keyReleased(int key, char c) {
 		// TODO Auto-generated method stub
 		if (key == Input.KEY_LEFT || key == Input.KEY_RIGHT) {
 			setHorizontalMovement(0);
 		}
 		if (key == Input.KEY_UP || key == Input.KEY_DOWN) {
 			setVerticalMovement(0);
 		}
 
 	}
 
 	@Override
 	public void inputEnded() {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void inputStarted() {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public boolean isAcceptingInput() {
 		// TODO Auto-generated method stub
 		return true;
 	}
 
 	@Override
 	public void setInput(Input arg0) {
 		// TODO Auto-generated method stub
 	}
 
 
 }
