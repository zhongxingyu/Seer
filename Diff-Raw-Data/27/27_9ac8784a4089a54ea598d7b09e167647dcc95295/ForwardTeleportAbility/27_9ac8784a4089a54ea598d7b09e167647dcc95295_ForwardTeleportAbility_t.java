 package entities.players.abilities;
 
import map.TileProperty;
import utils.MapLoader;
 import entities.players.Player;
 
 public class ForwardTeleportAbility extends PlayerAbility {
 	
	private static float distance = 5f;
	
     public void use(Player p){
 		if (p.getDirection() == 1){
			if ("false".equals(MapLoader.getCurrentCell().getTile((int) p.getX() + (int) distance, (int) p.getY()).lookupProperty(TileProperty.BLOCKED))) p.moveX(distance);
 		} else {
			if ("false".equals(MapLoader.getCurrentCell().getTile((int) p.getX() - (int) distance, (int) p.getY()).lookupProperty(TileProperty.BLOCKED))) p.moveX(-distance);
 		}
 	}
 
 	@Override
 	public void stop_sounds() {
 		// TODO Auto-generated method stub
 		
 	}

	public static float getDistance() {
		return distance;
	}

	public static void setDistance(float distance) {
		ForwardTeleportAbility.distance = distance;
	}
 }
