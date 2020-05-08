 package server.model;
 
 import java.util.List;
 
 import server.util.Util;
 
 public class Turret extends Entity {
 
 	protected float range;
 	protected int shootTimer;
	protected int shootSpeed = 45;
 	/**
 	 * Distance the turret can shoot.
 	 */
 	protected float reach = 100;
 	
 	public Turret(float x, float y) {
 		super(x, y);
 		this.type = "turret";
 		this.speed = 0;
 		this.shootTimer = 0;
 		this.range = 500;
 	}
 
 	@Override
 	public void brain(Game game) {
 		shootTimer++;
 		Entity target = getClosestPlayer(game.getPlayersList());
 		if(target == null)
 			return;
 		if(shootTimer > shootSpeed)
 			this.shootAt(game, target);
 	}
 
 	private void shootAt(Game game, Entity target) {
 		float deltaX = target.getX() - this.getX();
 		float deltaY = target.getY() - this.getY();
 		float abs = (float) Util.euclidian(deltaX, deltaY, 0, 0);
 		deltaX/=abs; deltaY/=abs;
 		
 		Bullet bullet = new TurretBullet(this, this.getX()+this.width/2, this.getY()+this.getHeight()/2, deltaX, deltaY, this.range);
 		game.addEntity(bullet);
 	}
 	/**
 	 * 
 	 * @param players all players the turret can shoot at.
 	 * @return the closest player IN RANGE
 	 */
 	protected Entity getClosestPlayer(List<Player> players){
 		if (players.isEmpty())
 			return null;
 		Entity e = players.get(0);
 		players.remove(0);
 		float delta = (float) Util.euclidian(this, e);
 		for(Entity entity : players){
 			float newDelta = (float) Util.euclidian(this, entity); 
 			if(newDelta  < delta){
 				e = entity;
 				delta = newDelta;
 			}
 		}
 		if(delta <= reach)
 			return e;
 		else
 			return null;
 	}
 
 	@Override
 	public void hitByBullet(Game game, Bullet bullet) {
 		// Probably nothing...
 	}
 }
