 package game;
 
 import java.awt.Dimension;
 import java.awt.Point;
 
 import Entities.Entity;
 
 import util.Util;
 import util.Vector2D;
 
 public class View {
 	public Dimension windowSize;
 	Game game;
 	int tileSize;
 	public double zoom = 1;
 	public Vector2D velocity = new Vector2D(0,0);
 	boolean autonomous = false;
 	Entity followedEntity;
 	
 	public Vector2D offset;
 	
 	public View(Dimension size, int tileSize, Game g) {
 		windowSize = size;
 		offset = new Vector2D(0,0);
 		this.tileSize = tileSize;
 		game = g;
 	}
 	
 	public Vector2D viewSize() {
 		return new Vector2D(windowSize.width/(tileSize*zoom), windowSize.height/(tileSize*zoom));
 	}
 	
 	public Vector2D viewToWorld(Point viewPos) {
		Vector2D v = new Vector2D(viewPos.x/((double)tileSize*zoom),
				(windowSize.height-viewPos.y)/((double)tileSize*zoom));
 		return v.subtract(offset);
 	}
 	
 	private void setOffset(Vector2D pOffset) {
 		double maxOffsX = -game.field.tilesX+(viewSize().x);
 		double maxOffsY = -game.field.tilesY+(viewSize().y);
 		
 		offset = pOffset.copy();
 		if(offset.x >= 0)
 			offset.x = 0;
 		if(offset.x <= maxOffsX)
 			offset.x = maxOffsX;
 		
 		if(offset.y >= 0)
 			offset.y = 0;
 		if(offset.y <= maxOffsY)
 			offset.y = maxOffsY;
 		
 	}
 	
 	public void setZoom(double z) {
 		zoom = Util.valueInBounds(0.2, z, 3);
 	}
 	
 	public void follow(Entity entity) {
 		autonomous = true;
 		followedEntity = entity;
 	}
 	
 	public void unfollow() {
 		autonomous = false;
 		followedEntity = null;
 	}	
 	
 	public void process(double dt) {
 		if(autonomous) {
 			setOffset(followedEntity.pos.negate().add(viewSize().scalar(0.5)));
 		}
 		else {
 			setOffset(offset.subtract(velocity.scalar(dt)));
 		}
 	}
 
 }
