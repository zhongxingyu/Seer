 package com.github.joakimpersson.tda367.model.tiles.bombs;
 
 import java.util.List;
 import java.util.Timer;
 
 import com.github.joakimpersson.tda367.model.player.Player;
 import com.github.joakimpersson.tda367.model.tiles.Tile;
 import com.github.joakimpersson.tda367.model.utils.Position;
 
 /**
  * Definition of an area-bomb.
  * 
  * @author rekoil
  */
 public class AreaBomb extends Bomb {
 
 	/**
 	 * Creates an area-bomb, these have shorter range than regular bombs.
 	 * 
 	 * @param player
 	 *            The owner of the bomb.
 	 * @param timer
 	 *            The timer which will detonate the bomb.
 	 */
 	public AreaBomb(Player player, Timer timer) {
 		super(player, timer);
 		this.range--; // sets range one lower than usual due to the destructive
 						// nature of area bombs!
 	}
 
 	@Override
 	public List<Position> explode(Tile[][] map) {
 		int xPos = pos.getX();
 		int yPos = pos.getY();
 
 		for (int x = xPos - range; x <= xPos + range; x++) {
 			for (int y = yPos - range; y <= yPos + range; y++) {
 				Position firePos = new Position(x, y);
 				if (validPos(firePos) && tryBreak(map[x][y], power)) {
 					fireList.add(firePos);
 				}
 			}
 		}
 
 		this.player.decreaseBombsPlaced();
 		return fireList;
 	}
 
 }
