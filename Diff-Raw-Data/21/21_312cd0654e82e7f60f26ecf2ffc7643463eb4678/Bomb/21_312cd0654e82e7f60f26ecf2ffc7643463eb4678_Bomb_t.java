 package com.github.joakimpersson.tda367.model.tiles.bombs;
 
 import java.awt.Dimension;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Timer;
 
 import static com.github.joakimpersson.tda367.model.constants.Attribute.BombPower;
 import static com.github.joakimpersson.tda367.model.constants.Attribute.BombRange;
 
 import com.github.joakimpersson.tda367.model.constants.Direction;
 import com.github.joakimpersson.tda367.model.constants.Parameters;
 import com.github.joakimpersson.tda367.model.player.Player;
 import com.github.joakimpersson.tda367.model.tiles.Tile;
 import com.github.joakimpersson.tda367.model.tiles.walkable.Floor;
 import com.github.joakimpersson.tda367.model.utils.Position;
 
 /**
  * This class defines a Bomb in the bomberman-like game.
  * 
  * @author rekoil
  * @modified Viktor Anderling
 * 
  */
 public abstract class Bomb implements Tile {
 
 	protected final int toughness, power;
 	protected int range; // TODO maybe do this differently so that it can also
 							// be final
 	protected final Timer timer;
 	protected final Player player;
 	protected final Position pos;
 	protected Map<Position, Direction> directedFireList = new HashMap<Position, Direction>();
 
 	/**
 	 * Creates an abstract Bomb.
 	 * 
 	 * @param player
 	 *            The Player who the Bomb belongs to.
 	 * @param timer
 	 *            The timer which will detonate the bomb.
 	 */
 	public Bomb(Player player, Timer timer) {
 		this.pos = player.getTilePosition();
 		this.player = player;
 		this.toughness = 1; // TODO perhaps define this in Parameters?
 		this.range = player.getAttribute(BombRange);
 		this.power = player.getAttribute(BombPower);
 		this.timer = timer;
 		this.player.increaseBombsPlaced();
 	}
 
 	/**
 	 * Determines if a tile can be destroyed by this bomb or not.
 	 * 
 	 * @param tile
 	 *            The tile which is to be tested.
 	 * @param power
 	 *            The current power of the flame.
 	 * @return Whether the tile can be destroyed or not.
 	 */
 	protected boolean tryBreak(Tile tile, int power) {
 		return power >= tile.getToughness();
 	}
 
 	/**
 	 * Tests if a position is able to contain fire or not.
 	 * 
 	 * @param firePos
 	 *            Position to test.
 	 * @return Whether a Position is able to contain fire or not.
 	 */
 	protected boolean validPos(Position firePos) {
 		Dimension d = Parameters.INSTANCE.getMapSize();
 		return firePos.getX() >= 0 
 				&& firePos.getX() < d.width 
 				&& firePos.getY() >= 0
 				&& firePos.getY() < d.height;
 	}
 
 	/**
 	 * Returns the owner of the bomb.
 	 * 
 	 * @return The owner of the bomb.
 	 */
 	public Player getPlayer() {
 		return player;
 	}
 
 	/**
 	 * Generates a list of fires caused by the bomb.
 	 * 
 	 * @param map
 	 *            The map which the fires will be placed on.
 	 * @return A list of fires.
 	 */
 	public abstract Map<Position, Direction> explode(Tile[][] map);
 
 	@Override
 	public int getToughness() {
 		return toughness;
 	}
 
 	@Override
 	public Tile onFire() {
 		this.timer.cancel();
 		this.player.decreaseBombsPlaced();
 		return new Floor();
 	}
 
 	@Override
 	public boolean isWalkable() {
 		return false;
 	}
 }
 
 //
 // int tryBreak(Position pos, int power, Tile tile) {
 // if (pos.getX()<0 || pos.getY()<0) {
 // return -1;
 // }
 // int toughness = tile.getToughness();
 // if (power >= toughness)
 // return toughness;
 // else
 // return -1;
 // }
