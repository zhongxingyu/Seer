 package edu.chl.codenameg.model.entity;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.newdawn.slick.geom.Rectangle;
 
 import edu.chl.codenameg.model.CollisionEvent;
 import edu.chl.codenameg.model.Direction;
 import edu.chl.codenameg.model.Entity;
 import edu.chl.codenameg.model.Hitbox;
 import edu.chl.codenameg.model.Position;
 import edu.chl.codenameg.model.Vector2D;
 import edu.chl.codenameg.model.World;
 
 // Needs cleaning of code and to finish TODOs
 public class PlayerCharacter implements Entity {
 	private Position pt;
 	private Position startPos;
 	private Vector2D v2d;
 	private Vector2D addVector;
 	private Vector2D gravity;
 	private Vector2D acceleration;
 	private boolean colliding;
 	private boolean alive;
 	private boolean moving;
 	private boolean onGround;
 	private boolean jumping;
 	private boolean lifting;
 	private boolean justJumped;
 	private boolean crouching;
 	private boolean waitingToUncrouch;
 	private boolean gameWon;
 	private boolean inWater;
 	private Direction direction;
 	private LiftableBlock lb;
 	private List<CollisionEvent> collidingList;
 	private Hitbox hitbox;
 	private Hitbox hbCopy;
 	private World world;
 	private List<String> collideList;
 	private float speedFactor;
 
 	// TODO Clean this commented code
 	// public PlayerCharacter() {
 	// this(new Position(0, 0));
 	//
 	// }
 	//
 	// public PlayerCharacter(Position position) {
 	// this(position);
 	// this.world = world;
 	// }
 	public PlayerCharacter(World world) {
 		this(new Position(0, 0), world);
 	}
 
 	public PlayerCharacter(Position position, World world) {
 		this.world = world;
 		this.collideList = new ArrayList<String>();
 		this.collideList.add("Block");
 		this.collideList.add("MovableBlock");
		this.collideList.add("MovingBlock");
 		this.collideList.add("PlayerCharacter");
 		this.collideList.add("LiftableBlock");
 		this.collideList.add("FallingBlock");
 		this.collideList.add("MovingWall");
 		this.collideList.add("Camera");
 		this.gameWon = false;
 		this.alive = true;
 		this.setPosition(position);
 		this.startPos = position;
 		this.v2d = new Vector2D(0, 0);
 		this.addVector = new Vector2D(0, 0);
 		this.direction = Direction.RIGHT;
 		this.collidingList = new ArrayList<CollisionEvent>();
 		this.gravity = new Vector2D(0, 1);
 		this.acceleration = new Vector2D(0, 0);
 		this.inWater = false;
 		this.speedFactor = 1.0f;
 		this.hitbox = new Hitbox(31, 46);
 	}
 
 	public void jump() {
 		this.jumping = true;
 	}
 
 	public void toggleCrouch() {
 		if (!crouching) {
 			this.hbCopy = this.hitbox;
 			this.hitbox = new Hitbox(this.getHitbox().getWidth(), this
 					.getHitbox().getHeight() - 25);
 			this.pt = new Position(this.pt.getX(), this.pt.getY() + 25);
 			this.crouching = true;
 		}
 	}
 
 	public void unToggleCrouch() {
 		if (crouching) {
 			this.waitingToUncrouch = false;
 			Rectangle searchRectangle = new Rectangle(
 					this.getPosition().getX(), this.getPosition().getY()
 							+ this.getHitbox().getHeight()-this.hbCopy.getHeight(),
 					this.hbCopy.getWidth(), this.hbCopy.getHeight());
 			for (Entity e : world.getEntitiesAt(searchRectangle)) {
 				if (this.getCollideTypes().contains(e.getType())&& !e.equals(this)) {
 					this.waitingToUncrouch = true;
 				}
 			}
 
 			if (!waitingToUncrouch) {
 				this.pt = new Position(this.pt.getX(), this.pt.getY() - 25);
 				this.hitbox = this.hbCopy;
 				this.crouching = false;
 				this.waitingToUncrouch = false;
 			}
 		}
 	}
 
 	public void toggleLift() {
 		Rectangle searchRectangle;
 		int factor = crouching ? 2 : 1;
 
 		if (this.direction == Direction.RIGHT) {
 			searchRectangle = new Rectangle(this.getPosition().getX()
 					+ this.hitbox.getWidth(), this.getPosition().getY(), 25f,
 					this.getHitbox().getHeight() * factor);
 
 		} else {
 			searchRectangle = new Rectangle(this.getPosition().getX()
 					- this.hitbox.getWidth(), this.getPosition().getY(), 25f,
 					this.getHitbox().getHeight() * factor);
 		}
 		List<Entity> entitiesList = this.world.getEntitiesAt(searchRectangle);
 
 		for (Entity entity : entitiesList) {
 			if (entity.getType().equals("LiftableBlock")) {
 				this.collideList.remove("LiftableBlock");
 				this.lb = ((LiftableBlock) entity);
 				this.lb.lift(this);
 				this.lifting = true;
 				break;
 			}
 		}
 	}
 
 	public void unToggleLift() {
 		if (!this.collideList.contains("LiftableBlock")) {
 			this.collideList.add("LiftableBlock");
 		}
 		this.lifting = false;
 
 		if (lb != null) {
 			lb.drop();
 			lb = null;
 		}
 	}
 
 	public boolean isLifting() {
 		return this.lifting;
 	}
 
 	public boolean isMoving() {
 		return this.moving;
 	}
 
 	public boolean isCrouching() {
 		return this.crouching;
 	}
 
 	public void stopJump() {
 		if (jumping) {
 			this.justJumped = true;
 		}
 		this.jumping = false;
 	}
 
 	public void move() {
 		this.moving = true;
 	}
 
 	public void move(Direction d) {
 		this.setDirection(d);
 		this.move();
 	}
 
 	public void setDirection(Direction d) {
 		this.direction = d;
 	}
 
 	public void stopMove() {
 		this.moving = false;
 	}
 
 	public Direction getDirection() {
 		Direction temp = this.direction;
 
 		return temp;
 	}
 
 	@Override
 	public void collide(CollisionEvent evt) {
 		this.colliding = true;
 
 		if (this.getCollideTypes().contains(evt.getEntity().getType())
 				&& (evt.getDirection().equals(Direction.BOTTOM))) {
 			this.onGround = true;
 			this.justJumped = false;
 		}
 		if (evt.getDirection().equals(Direction.TOP)
 				&& this.getCollideTypes().contains(evt.getEntity().getType())) {
 			this.jumping = false;
 		}
 		if (evt.getDirection() == Direction.TOP
 				&& this.getCollideTypes().contains(evt.getEntity().getType())
 				&& !(evt.getEntity().getType().equals("Water"))) {
 			this.jumping = false;
 		}
 		if (evt.getEntity().getType().equals("Water")) {
 			this.inWater = true;
 		}
 		if (!evt.getEntity().getType().equals(this.getType())
 				&& this.getCollideTypes().contains(evt.getEntity().getType())) {
 			this.collidingList.add(evt);
 		}
 		if (this.getCollideTypes().contains(evt.getEntity().getType())) {
 			if (evt.getDirection().equals(Direction.RIGHT)
 					|| evt.getDirection().equals(Direction.LEFT)) {
 				this.acceleration.setX(0);
 			} else if (evt.getDirection().equals(Direction.TOP)
 					|| evt.getDirection().equals(Direction.BOTTOM)) {
 				this.acceleration.setY(0);
 			}
 		}
 	}
 
 	private void checkCollisionDeath() {
 
 		if (collidingList.size() > 0) {
 			int collideLeftCount = 0;
 			int collideRightCount = 0;
 			int collideTopCount = 0;
 			int collideBottomCount = 0;
 
 			for (CollisionEvent evt : collidingList) {
 
 //				if (!(evt.getEntity().getType().equals("LiftableBlock")) ) {
 
 					switch (evt.getDirection()) {
 					case LEFT:
 						collideLeftCount++;
 						break;
 					case RIGHT:
 						collideRightCount++;
 						break;
 					case TOP:
 						collideTopCount++;
 						break;
 					case BOTTOM:
 						collideBottomCount++;
 						break;
 					}
 //				}
 //				 }
 			}
 
 			if ((collideLeftCount > 0) && (collideRightCount > 0)) {
 				this.die();
 			} else if ((collideTopCount > 0) && (collideBottomCount > 0)) {
 				this.die();
 			}
 			this.collidingList.clear();
 		}
 	}
 
 	public void die() {
 		this.alive = false;
 	}
 
 	public void winGame() {
 		this.gameWon = true;
 	}
 
 	// Getters and setters
 	@Override
 	public void setPosition(Position p) {
 		this.pt = p;
 	}
 
 	public Position getStartPosition() {
 		return new Position(this.startPos);
 	}
 
 	public void setStartPosition(Position p) {
 		this.startPos = p;
 	}
 
 	@Override
 	public Position getPosition() {
 		return new Position(this.pt);
 	}
 
 	@Override
 	public Hitbox getHitbox() {
 		return new Hitbox(hitbox);
 	}
 
 	public void setVector2D(Vector2D v2d) {
 		this.v2d = new Vector2D(v2d);
 	}
 
 	public void addVector2D(Vector2D v2d) {
 		this.addVector = new Vector2D(v2d);
 	}
 
 	@Override
 	public Vector2D getVector2D() {
 		return new Vector2D(this.v2d);
 	} // End of getters and setters
 
 	// Boolean methods
 	@Override
 	public boolean isColliding() {
 		boolean temp = this.colliding;
 		return temp;
 	}
 
 	public boolean hasWonGame() {
 		boolean temp = this.gameWon;
 		return temp;
 	}
 
 	public boolean isAlive() {
 		boolean temp = this.alive;
 		return temp;
 	}
 
 	public boolean isOnGround() {
 		return this.onGround;
 	}
 
 	public boolean isInWater() {
 		return this.inWater;
 	}
 
 	public boolean isJumping() {
 		return this.jumping || this.justJumped;
 	} // End of boolean methods
 
 	@Override
 	public List<String> getCollideTypes() {
 		List<String> list = new ArrayList<String>(this.collideList);
 		return list;
 	}
 
 	@Override
 	public String getType() {
 		return "PlayerCharacter";
 	}
 
 	public void update() {
 		this.update(10);
 	}
 
 	@Override
 	public void update(int elapsedTime) {
 		this.checkCollisionDeath();
 
 		this.v2d = new Vector2D(addVector);
 		this.addVector = new Vector2D(0, 0);
 		if (this.inWater) {
 			this.speedFactor = 0.5f;
 		}
 
 		if (this.waitingToUncrouch) {
 			this.unToggleCrouch();
 			System.out.println("hejhopp");
 		}
 
 		if (this.direction.equals(Direction.RIGHT) && this.moving) {
 			this.v2d.add(new Vector2D(2.8f, 0));
 			// TODO Clean this code
 			// if(this.acceleration.getX()<0) {
 			// this.acceleration.setX(0);
 			// }
 			if (this.onGround
 					&& (!this.jumping || this.acceleration.getX() < 0)) {
 				this.acceleration.add(new Vector2D(0.15f, 0));
 			}
 		} else if (this.direction.equals(Direction.LEFT) && this.moving) {
 			this.v2d.add(new Vector2D(-2.8f, 0));
 			// TODO Clean this code
 			// if(this.acceleration.getX()>0) {
 			// this.acceleration.setX(0);
 			// }
 			if (this.onGround
 					&& (!this.jumping || this.acceleration.getX() > 0)) {
 				this.acceleration.add(new Vector2D(-0.15f, 0));
 			}
 		}
 		if (Math.abs(this.acceleration.getX()) < 0.1) {
 			this.acceleration.setX(0);
 		}
 
 		if (this.acceleration.getX() > 0) {
 			this.acceleration.add(new Vector2D(-0.1f, 0));
 		} else if (this.acceleration.getX() < 0) {
 			this.acceleration.add(new Vector2D(0.1f, 0));
 		}
 
 		this.v2d.add(acceleration);
 
 		if (jumping && !justJumped) {
 			this.v2d.add(new Vector2D(0, -5));
 		} else if (justJumped) { // TODO Not being able to jump if just dropped
 									// from height
 			this.v2d.add(new Vector2D(0, -2f));
 		}
 
 		if (!onGround) {
 			this.gravity.add(new Vector2D(0, 0.1f));
 		} else {
 			this.gravity = new Vector2D(0, 0.98f);
 		}
 		this.v2d.add(this.gravity);
 		this.v2d = new Vector2D(this.v2d.getX() * this.speedFactor,
 				this.v2d.getY());
 		this.onGround = false;
 		this.colliding = false;
 		this.inWater = false;
 		this.speedFactor = 1.0f;
 	}
 }
