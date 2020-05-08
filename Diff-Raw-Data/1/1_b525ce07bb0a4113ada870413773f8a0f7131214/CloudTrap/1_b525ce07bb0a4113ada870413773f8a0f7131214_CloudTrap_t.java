 package server.model.entities;
 
 import java.util.List;
 
 import server.model.Game;
 import server.model.entities.moving.Bullet;
import server.model.entities.moving.Player;
 import server.util.Util;
 
 public class CloudTrap extends Entity {
 
 	public CloudTrap(float x, float y) {
 		super(x, y);
 		this.setDeadlyForPlayer(false);
 	}
 
 	@Override
 	public void brain(Game game) {
 		List<Entity> entities = Util.getEntitiesOverlapping(game.getPlayersList(), this);
 		for (Entity entity : entities)
 			game.cloudPenaltyFor((Player) entity);
 	}
 
 	@Override
 	public void hitByBullet(Game game, Bullet bullet) {
 		
 	}
 
 }
