 package edu.chl.codenameg.model.entity;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import edu.chl.codenameg.model.CollisionEvent;
 import edu.chl.codenameg.model.Direction;
 import edu.chl.codenameg.model.Hitbox;
 import edu.chl.codenameg.model.Position;
 import edu.chl.codenameg.model.Vector2D;
 
 /**
  * A MovableBlock that is liftable by a PlayerCharacter
  * Is affected by gravity and friction
  */
 public class LiftableBlock extends MovableBlock{
 	private PlayerCharacter pc;
 	private List<String> collideList;
 	private boolean colliding;
 	private boolean onGround;
 
 	public LiftableBlock(Position ps){
 		super(ps);
 		init();
 	}
 	
 	public LiftableBlock(Position ps, Hitbox hb){
 		super(ps, hb);
 		init();
 	}
 	
 	public LiftableBlock() {
 		super();
 		init();
 	}
 	
 	private void init() {
 		this.collideList 	= new ArrayList<String>();
 		this.colliding 		= false;
 		this.onGround 		= false;
 		addCompleteCollideList();
 	}
 	
 	private void addCompleteCollideList() {
 		this.collideList.add("MovableBlock");
 		this.collideList.add("MovingBlock");
 		this.collideList.add("Block");
 		this.collideList.add("PlayerCharacter");
 	}
 	
 	private void removeAllInCollideList() {
 		for (int i = 0; this.collideList.size() > i; i++) {
 			this.collideList.remove(0);
 		}
 	}
 	
 	@Override
 	public String getType() {
 		return "LiftableBlock";
 	}
 	
 	@Override
 	public List<String> getCollideTypes() {
 		List<String> list = new ArrayList<String>(this.collideList);
 		return list;
 	}
 	
 	/**
 	 * Handles collision with this block
 	 */
 	@Override
 	public void collide(CollisionEvent evt) {
 		this.colliding = true;
 		if (this.getCollideTypes().contains(evt.getEntity().getType())
 				&& (evt.getDirection().equals(Direction.BOTTOM))) {
 			this.onGround = true;
 		}
 		if (this.getCollideTypes().contains(evt.getEntity().getType())) {
 			if (evt.getDirection().equals(Direction.RIGHT)
 					|| evt.getDirection().equals(Direction.LEFT)) {
 				this.setVector2D(new Vector2D(0, 0));
 			}
 		}
 	}
 	
 	@Override
 	public boolean isColliding() {
 		return this.colliding;
 	}
 	
 	@Override
 	public void update() {
 		this.update(10);
 	}
 	
 	/**
 	 * Removes gravity and friction when held by a player and adds it when it's released
 	 */
 	@Override
 	public void update(int elapsedTime) {
 		if (pc != null){
 			if (this.pc.getDirection()==Direction.RIGHT){
 				this.setPosition(new Position(pc.getPosition().getX(), 
 						pc.getPosition().getY() - this.getHitbox().getHeight() - 5));
 			}else{
 				this.setPosition(new Position(pc.getPosition().getX() - 
 							(pc.getHitbox().getWidth() - this.getHitbox().getWidth()), 
 						pc.getPosition().getY() - this.getHitbox().getHeight() - 5));		
 			}
 		}else{
 			this.addVector2D(new Vector2D(0, 0.1f));
 		}
 
 		if (this.onGround) {
 			if (this.getVector2D().getX() < 0) {
 				this.addVector2D(new Vector2D(0.1f, 0));
 				
 			} else if (this.getVector2D().getX() > 0) {
 				this.addVector2D(new Vector2D(-0.1f, 0));
 			}
 		}
 		
 		this.colliding = false;
 		this.onGround = false;
 	}
 	
 	/**
 	 * Sets this block to be lifted
 	 * @param a PlayerCharacter
 	 */
 	public void lift(PlayerCharacter pc) {
 		this.pc = pc;
 		removeAllInCollideList();
 	}
 
 	/**
 	 * Drops this block from the player holding it
 	 * Also adds throwing speed
 	 */
 	public void drop() {
		this.pc = null;
 		addCompleteCollideList();
 		float temp = (pc.getDirection() == Direction.LEFT ? -3f : 3f);
 		this.setVector2D(new Vector2D(temp, -2f));
 	}
 }
