 package server.model;
 
 import java.util.Collection;
 import java.util.List;
 
 import server.model.map.PositionType;
 import server.network.GsonExclusionStrategy.noGson;
 
 public class Bot extends Entity {
 
 	@noGson
 	protected float lastDX = 0, lastDY = 0;
 	private float lastX, lastY;
 
 	public Bot(int id, float x, float y, String name) {
 		super(id, x, y);
 		this.lastX = x;
 		this.lastY = y;
 		this.type = "bot";
		this.collisionResolving = false;
 		this.speed *= 0.75;
 	}
 
 	@Override
 	public void brain(Game game) {
 		// chase a player
 		this.lastX = this.getX();
 		this.lastY = this.getY();
 		Entity other = closestPlayer(game);
 		float dX = deltaX(other);
 		float dY = deltaY(other);
 		float distance = euclideanLength(dX, dY);
 		float factor = this.speed / distance;
 		List<Entity> overlapping = moveOnMap(game, factor * dX, factor * dY);
 		for (Entity entity : overlapping) {
 			entity.hitByBullet(game, new Bullet(this, 0, 0, 0, 0));
 		}
 
 		this.updateLookingDirection(this.getX(), this.getY());
 
 		lastDX = dX * factor;
 		lastDY = dY * factor;
 	}
 
 	private Entity closestPlayer(Game game) {
 		Collection<Player> players = game.getPlayers().values();
 		Entity closest = this; // just a fallback if none around
 		for (Player p : players) {
 			if (this.equals(closest))
 				closest = p;
 			else if (!Bot.class.isAssignableFrom(p.getClass())
 					&& this.distanceTo(p) < this.distanceTo(closest))
 				closest = p;
 		}
 		return closest;
 	}
 
 	@Override
 	public void hitByBullet(Game game, Bullet bullet) {
 		if (!bullet.getOwner().equals(this)) {
 			this.deathCount++;
 			bullet.getOwner().incrementKillCount();
 			float xy[] = game.getMap().getFirstTileXY(PositionType.BotStart);
 			this.setX(xy[0]);
 			this.setY(xy[1]);
 		}
 	}
 
 	private void updateLookingDirection(float xNew, float yNew) {
 		float dirXnew = xNew - this.lastX;
 		float dirYnew = yNew - this.lastY;
 		// calculate normalized (unit length) looking direction
 		if (dirXnew == dirYnew) {
 			if (dirXnew == 0)
 				return;
 			else { // exact diagonal movement -> set both
 				dirXnew /= Math.abs(dirXnew);
 				dirYnew /= Math.abs(dirYnew);
 			}
 		} else if (Math.abs(dirXnew) > Math.abs(dirYnew)) {
 			dirXnew /= Math.abs(dirXnew);
 			dirYnew = 0;
 		} else {
 			dirYnew /= Math.abs(dirYnew);
 			dirXnew = 0;
 		}
 		this.dirX = Math.round(dirXnew);
 		this.dirY = Math.round(dirYnew);
 	}
 }
