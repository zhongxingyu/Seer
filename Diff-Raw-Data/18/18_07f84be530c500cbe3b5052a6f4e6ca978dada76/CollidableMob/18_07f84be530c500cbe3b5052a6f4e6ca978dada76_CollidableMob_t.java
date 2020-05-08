 package caskman.polygonsim.model.entities;
 
 import java.awt.Graphics;
 import java.util.ArrayList;
 import java.util.List;
 
 import caskman.polygonsim.model.Collidable;
 import caskman.polygonsim.model.Collisions;
 import caskman.polygonsim.model.GameContext;
 import caskman.polygonsim.model.GameModel;
 import caskman.polygonsim.model.Vector;
 
 public abstract class CollidableMob extends Mob implements Collidable {
 
 	protected Vector collisionPosition;
 	
 	public CollidableMob(GameModel m) {
 		super(m);
 	}
 
 	@Override
 	public void updateMob(GameContext g) {
 		checkCollisions(g);
 		update(g);
 	}
 	
 	private void checkCollisions(GameContext g) {
 		List<Collidable> possibleCollisions = g.quadTree.retrieve(new ArrayList<Collidable>(),this);
 		float percent;
 		
 		for (Collidable c : possibleCollisions) {
 			if ((percent = Collisions.detectCollision(this,c)) != -1F) {
 				resolveCollision(g,c,percent);
 			}
 		}
 	}
 	
 	protected abstract void resolveCollision(GameContext g,Collidable c,float percent);
 	
 	@Override
 	protected abstract void update(GameContext g);
 	
 	@Override
 	protected abstract void draw(Graphics g, float interpol);
 
 	@Override
 	public Vector getVelocity() {
 		return velocity;
 	}
 
 }
