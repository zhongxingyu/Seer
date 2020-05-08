 package edu.chl.codenameg.model.entity;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import edu.chl.codenameg.model.CollisionEvent;
 import edu.chl.codenameg.model.Direction;
 import edu.chl.codenameg.model.Entity;
 import edu.chl.codenameg.model.Hitbox;
 import edu.chl.codenameg.model.Position;
 import edu.chl.codenameg.model.Vector2D;
 
 public class PlayerCharacter implements Entity {
 	private Hitbox hitbox;
 	private boolean gameWon;
 	private Position pt;
 	private Vector2D v2d;
 	private Vector2D addVector;
 	private Vector2D gravity;
 	private Vector2D acceleration;
 	private boolean colliding;
 	private boolean alive;
 	private Direction direction;
 	private boolean moving;
 	private boolean onGround;
 	private boolean jumping;
 	private boolean lifting;
 	private boolean justJumped;
 	private boolean crouching;
 	private LiftableBlock lb;
 	private List<CollisionEvent> collidingList;
 	private Hitbox hbCopy;
 	private Position startPos;
 
 	public PlayerCharacter() {
 		this(new Position(0, 0));
 	}
 
 	public PlayerCharacter(Position position) {
 		this.hitbox= new Hitbox(30, 52);
 		gameWon = false;
 		this.alive = true;
 		this.setPosition(position);
 		this.startPos = position;
 		this.v2d = new Vector2D(0, 0);
 		this.addVector = new Vector2D(0, 0);
 		this.direction = Direction.RIGHT;
 		this.collidingList = new ArrayList<CollisionEvent>();
 		this.gravity = new Vector2D(0,1);
 		this.acceleration = new Vector2D(0,0);
 	}
 
 	public void jump() {
 			this.jumping = true;
 	}
 	public void toggleCrouch(){
 			this.hbCopy=this.hitbox;
 			this.hitbox=new Hitbox(this.getHitbox().getWidth(),this.getHitbox().getHeight()-25);
 			this.pt=new Position(this.pt.getX(),this.pt.getY()+25);
 			this.crouching=true;
 	}
 	public void unToggleCrouch(){
 			this.pt=new Position(this.pt.getX(),this.pt.getY()-25);
 			this.hitbox =this.hbCopy;
 			this.crouching=false;
 	}
 	
 	public void Togglelift() {
 		//System.out.println("Lyfter");
 		this.lifting = true;
 	}
 
 	public void unToggleLift() {
 		//System.out.println("Slpper");
 		this.lifting=false;
 		if (lb!=null){
 			lb.drop(this);
 			lb = null;
 		}
 	}
 	
 	public boolean isLifting(){
 		return this.lifting;
 	}
 	public boolean isMoving(){
 		return this.moving;
 	}
 	public boolean isCrouching(){
 		return this.crouching;
 	}
 
 	public void stopJump() {
 		if(jumping)
 			this.justJumped = true;
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
 
 	public void collide(CollisionEvent evt) {
 		this.colliding = true;
 		if (this.getCollideTypes().contains(evt.getEntity().getType()) && (evt.getDirection() == Direction.BOTTOM)) {
 			this.onGround = true;
 			this.justJumped = false;
 		}
 		if (evt.getDirection()==Direction.TOP){
 			this.jumping=false;
 		}
 		if ((evt.getEntity()instanceof LiftableBlock)&&(evt.getDirection()== Direction.LEFT || evt.getDirection()== Direction.RIGHT)){
 			lb = (LiftableBlock)evt.getEntity();
 			if (lifting){
 			lb.lift(this);
 			}
 		}
 		if(evt.getEntity().getType() != this.getType() && this.getCollideTypes().contains(evt.getEntity().getType())) {
 			this.collidingList.add(evt);
 		}
 	}
 
 
 	private void checkCollisionDeath() {
 		
 		//TODO Check collision from both sides.
 		if (collidingList.size() > 0) {
 			int collideLeftCount = 0;
 			int collideRightCount = 0;
 			int collideTopCount = 0;
 			int collideBottomCount = 0;
 			
 			for(CollisionEvent evt : collidingList) {
 //				if(!(evt.getEntity() instanceof PlayerCharacter)){
 				if(!(evt.getEntity() instanceof LiftableBlock)){
 					
 				
 					switch(evt.getDirection()) {
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
 				}
 //				}
 			}
 			
 			if ((collideLeftCount > 0) && (collideRightCount > 0)) {
 				this.die();
 			} else if ((collideTopCount > 0 ) && (collideBottomCount > 0)) {
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
 
 	// getters & setters
 	public void setPosition(Position p) {
 		this.pt = p;
 	}
 	public Position getStartPosition(){
 		return new Position(this.startPos);
 	}
 	public Position getPosition() {
 		return new Position(this.pt);
 	}
 
 	public Hitbox getHitbox() {
 		return new Hitbox(hitbox);
 	}
 
 	public void setVector2D(Vector2D v2d) {
 		this.v2d = new Vector2D(v2d);
 	}
 
 	public void addVector2D(Vector2D v2d) {
 		this.addVector = new Vector2D(v2d);
 	}
 
 	public Vector2D getVector2D() {
 		return new Vector2D(this.v2d);
 	}
 
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
 	public boolean isJumping(){
 		return this.jumping;
 	}
 
 	@Override
 	public List<String> getCollideTypes() {
 		List<String> list = new ArrayList<String>();
 		list.add("Block");
 		list.add("MovableBlock");
 		list.add("PlayerCharacter");
 		list.add("LiftableBlock");
 		return list;
 	}
 
 	@Override
 	public String getType() {
 		return "PlayerCharacter";
 	}
 
 	public void update() {
 		this.update(10);
 	}
 	
 	public void update(int elapsedTime) {
 		this.checkCollisionDeath();
 		
 		this.v2d = new Vector2D(addVector);
 		this.addVector = new Vector2D(0, 0);
 
 		if (this.direction == Direction.RIGHT && this.moving) {
 			this.v2d.add(new Vector2D(2, 0));
 //			if(this.acceleration.getX()<0) {
 //				this.acceleration.setX(0);
 //			}
 			
 			this.acceleration.add(new Vector2D(0.1f,0));
 		} else if (this.direction == Direction.LEFT && this.moving) {
 			this.v2d.add(new Vector2D(-2, 0));
 //			if(this.acceleration.getX()>0) {
 //				this.acceleration.setX(0);
 //			}
 			this.acceleration.add(new Vector2D(-0.1f,0));
 		} else {
			if(Math.abs(this.acceleration.getX())<0.2) {
				this.acceleration.setX(0);
			}
			
			if(this.acceleration.getX() > 0) {
				this.acceleration.add(new Vector2D(-0.2f,0));
			} else if(this.acceleration.getX() < 0) {
				this.acceleration.add(new Vector2D(0.2f,0));
			}
 		}
 		this.v2d.add(acceleration);
 
 		if (jumping && !justJumped) {
 			this.v2d.add(new Vector2D(0,-5));
 		} else if(justJumped) { //TODO Not being able to jump if just dropped from height
 			this.v2d.add(new Vector2D(0,-2));
 		}
 
 
 		if (!onGround) {
 			this.gravity.add(new Vector2D(0,0.1f));
 		} else {
 			this.gravity = new Vector2D(0,0.98f);
 		}
 		this.v2d.add(this.gravity);
 		this.onGround = false;
 		this.colliding = false;
 	}
 
 	
 }
