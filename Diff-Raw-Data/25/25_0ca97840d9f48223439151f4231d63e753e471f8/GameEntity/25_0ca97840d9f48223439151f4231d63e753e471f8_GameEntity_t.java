 /*
  * FlatCraft - GameEntity.java
  * 
  * The base entity class that all entities inherit from.
  * 
  */
 
 package no.ntnu.stud.flatcraft.entities;
 
 import java.util.ArrayList;
 
 import no.ntnu.stud.flatcraft.GameWorld;
 import no.ntnu.stud.flatcraft.Main;
 import no.ntnu.stud.flatcraft.quadtree.Block;
 import no.ntnu.stud.flatcraft.quadtree.Node;
 
 import org.newdawn.fizzy.Body;
 import org.newdawn.fizzy.CollisionEvent;
 import org.newdawn.slick.Color;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.geom.Rectangle;
 import org.newdawn.slick.state.StateBasedGame;
 
 public class GameEntity {
 
 	Body body;
 	public boolean grounded = false;
 	protected boolean moving = false;
 	private boolean falling = false;
 	protected boolean facingRight = true;
 	public Rectangle physrect; // physical bounding box of the entity
 	GameWorld gameworld; // handle back to the parent GameWorld.
 	private int offGroundTimer;
 	private boolean jumped;
 	boolean swimming = false;
 	boolean touchingAcid;
 
 	public void init(GameWorld gw, float _x, float _y, float _width,
 			float _height, float _mass) {
 
 //		body = new Body(new org.newdawn.fizzy.Rectangle(_width, _height), _mass);
 		body = new Body(new org.newdawn.fizzy.Rectangle(_width,_height), _x+_width*0.5f, _y+_height*0.5f, false);
 		gw.world.add(body);
 		body.setUserData(this);
 //		body.setRestitution(0);
 //		body.setDensity(0);
 //		body.setFriction(Main.mu);
 		body.setRestitution(0f);
 		body.setFriction(0.5f);
 		body.setDamping(0.8f);
 //		body.setMaxVelocity(40*Main.GU, 80*Main.GU);
 //		body.setRotatable(false);
 		body.setPosition(_x + _width * 0.5f, _y + _height * 0.5f);
 
 		gameworld = gw;
 		physrect = new Rectangle(_x, _y, _width, _height);
 	}
 
 	// reset() - Called when the GameWorld resets. May destroy the object if
 	// needed.
 	public void reset() {
 
 	}
 	
 	public void applyForce(float x, float y) {
 		body.applyForce(x, y);
 		moving = true;
 		if (y < 0) {
 			jumped = true;
 		}
 		
 		if (x > 0) {
 			facingRight = true;
 		}
 		if (x < 0) {
 			facingRight = false;
 		}
 	}
 
 	public void render(Graphics g) {
 		if (Main.DEBUG) {
 			g.pushTransform();
 
 			g.translate(-gameworld.viewport.getX()*Main.GULOL, -gameworld.viewport.getY()*Main.GULOL);
 			g.scale(Main.GULOL, Main.GULOL);
 //			g.scale(Main.GULOL, Main.GULOL);
 //			g.translate(-gameworld.viewport.getX(), -gameworld.viewport.getY());
 			g.setColor(Color.white);
 			g.draw(physrect);
 			g.fillRect(physrect.getX(), physrect.getY(), physrect.getWidth(), physrect.getHeight());
 //			g.fillRect(0, 0, 100, 100);
 			g.popTransform();
 		}
 	}
 
 	// update(GameContainer, StateBasedGame, int) - Updates the entity one tick.
 	public void update(GameContainer container, StateBasedGame game, int delta) {
 		setFlags();
 		if(body.getXVelocity() > 50){
 			body.setVelocity(50, body.getYVelocity());
 		}
 		if(body.getXVelocity() > 50){
 			body.setVelocity(body.getYVelocity(),50);
 		}
 //		if(grounded && body.getYVelocity() < 0){
 //			body.setVelocity(body.getXVelocity(), -100);
 //		}
 		physrect.setLocation(
 				body.getX()-physrect.getWidth()*0.5f, body
 						.getY()-physrect.getHeight()*0.5f);
 		if(swimming){
 			body.setVelocity(body.getXVelocity()*0.9f, body.getYVelocity()*0.5f);
 		}
 	}
 	
 	protected void setFlags() {
 		if (gameworld.world == null) {
 			return;
 		}
 		Node centernode = gameworld.terrain.getLeaf(physrect.getMinX()+physrect.getWidth()*0.5f, physrect.getMinY()+physrect.getHeight()*0.5f);
 		Node botnode1 = gameworld.terrain.getLeaf(physrect.getMinX()+1, physrect.getMaxY()+1);
 		Node botnode2 = gameworld.terrain.getLeaf(physrect.getMaxX(), physrect.getMaxY()+1);	
 		
 		if(botnode1 != null && botnode1.type != Block.EMPTY && botnode1.type != Block.WATER && botnode1.type != Block.ACID && botnode1.type != Block.START){
 			grounded = true;
 		}
		else if(botnode2 != null && botnode2.type != Block.EMPTY && botnode2.type != Block.WATER && botnode2.type != Block.ACID && botnode2.type != Block.START){
 			grounded = true;
 		}else grounded = false;
 		if(botnode1 != null && botnode1.type == Block.WATER){
 			swimming = true;
 		}
 		else if(botnode1 != null && botnode1.type == Block.WATER){
 			swimming = true;
 		}else swimming = false;
 		
 		if(Math.abs(body.getXVelocity()) > 0 || Math.abs(body.getYVelocity())>0)
 			moving = true;
 		else
 			moving = false;
 				
 		if(centernode !=null && centernode.type == Block.ACID ){
 			touchingAcid = true;
 		}
 	}
 }
 
