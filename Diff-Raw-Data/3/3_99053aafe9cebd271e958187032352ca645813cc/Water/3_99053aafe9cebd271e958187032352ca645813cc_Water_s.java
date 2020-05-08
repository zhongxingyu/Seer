 package edu.chl.codenameg.model.entity;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import edu.chl.codenameg.model.CollisionEvent;
 import edu.chl.codenameg.model.Entity;
 import edu.chl.codenameg.model.Hitbox;
 import edu.chl.codenameg.model.Position;
 import edu.chl.codenameg.model.Vector2D;
 
 
 public class Water implements Entity{
 	private Hitbox hb;
 	private Vector2D v2d;
 	private boolean colliding;
 	private Position ps;
 	
 	public Water(){
 		this(new Position(0,0));
 	}
 	
 	public Water(Position position){
 		this(position,new Hitbox(32,32));
 	}
 	public Water(Position position, Hitbox hitbox){
 		this.setHitbox(hitbox);
 		this.setPosition(position);
 		this.colliding = false;
 		this.v2d = new Vector2D(0,0);
 	}
 	public Hitbox getHitbox() {
 			return new Hitbox(hb);
 	}
 	public void setHitbox(Hitbox hb) {
 		this.hb = new Hitbox(hb);
 	}
 
 	public Vector2D getVector2D() {
 		return new Vector2D(this.v2d);
 	}
 
 	public boolean isColliding() {
 		return colliding;
 	}
 
 	public void collide(CollisionEvent evt) {
 		this.colliding = true;
 		
 	}
 	
 
 	public Position getPosition() {
 		return new Position(this.ps);
 	}
 
 	public void update(int elapsedTime) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void setPosition(Position p) {
 		this.ps=p;
 		
 	}
 
	@Override
 	public void setVector2D(Vector2D v2d) {
 		this.v2d=v2d;
 	}
 
 	@Override
 	public List<String> getCollideTypes() {
 		// TODO Auto-generated method stub
 		return new ArrayList<String>();
 	}
 
 	@Override
 	public String getType() {
 		return "Water";
 	}
 
	@Override
 	public void addVector2D(Vector2D v2d) {
 		this.v2d.add(v2d);
 	}
 }
