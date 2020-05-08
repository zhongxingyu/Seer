 package Entity;
 
 import java.util.ArrayList;
 import java.util.List;
 
import org.newdawn.slick.Color;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.geom.Rectangle;
 import org.newdawn.slick.state.StateBasedGame;
 
 import Main.GameObject;
 import Main.Play;
 
 
 public class Entity extends GameObject {
 	
 	protected float speed;
 	
 	protected boolean isWalking;
 	
 	protected Rectangle rightR;
 	protected Rectangle leftR;
 	protected Rectangle botR;
 	protected Rectangle topR;
 	
 	protected boolean blockedRight;
 	protected boolean blockedLeft;
 	protected boolean blockedUp;
 	protected boolean blockedDown;
 	
 	public Entity(float x, float y) {
 		super(x, y);
 		height = 32;
 		width = 32;
 		speed = 1;
 	}
 
 	@Override
 	public void init(GameContainer container, StateBasedGame game) {
 		this.setHitBox(x, y, width, height);
 	}
 
 	@Override
 	public void render(GameContainer container, StateBasedGame game, Graphics g) {
 		g.drawImage(sprite, x, y);
 	}
 
 	@Override
 	public void update(GameContainer container, StateBasedGame game, int delta) {
 		this.setHitBox(x, y, width, height);
 		handleBlockCollisions();
 	}
 	
 	public void handleBlockCollisions() {
 		blockedRight = false;
 		blockedLeft = false;
 		blockedUp = false;
 		blockedDown = false;
 
 		for(Rectangle rec : Play.levelHandler.getTileHitBoxes()) {
 			if(this.hitBox.intersects(rec)) {
 				topR = new Rectangle(rec.getMinX() + 1, rec.getMinY() - 16, 30, 15);
 				leftR = new Rectangle(rec.getMinX() - 16, rec.getMinY() + 1, 15, 30);
 				rightR = new Rectangle(rec.getMaxX() + 1, rec.getMinY() + 1, 15, 30);
 				botR = new Rectangle(rec.getMinX() + 1, rec.getMaxY() + 1, 30, 15);
 				
 				if(this.hitBox.intersects(leftR)) {
 					this.blockedRight = true;
 				}
 				
 				if(this.hitBox.intersects(rightR)) {
 					this.blockedLeft = true;
 				}
 				
 				if(this.hitBox.intersects(topR)) {
 					this.blockedDown = true;
 				}
 				
 				if(this.hitBox.intersects(botR)) {
 					this.blockedUp = true;
 				}
 			}
 		}
 	}
 	
 	public void move(float xa, float ya) {
 		if (xa != 0 && ya != 0) {
 			move(xa, 0);
 			move(0, ya);
 			return;
 		}
 		
 		this.x += xa * speed;
 		this.y += ya * speed;
 	}
 }
