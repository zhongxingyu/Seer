 package rltut;
 
 import java.awt.Color;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Overarching world class. Contains the state of the world.
  * 
  * @author Jeremy Rist
  * 
  */
 public class World {
 	private Tile[][][] tiles;
 	private int width;
 	private int height;
 	private int depth;
 	private List<Creature> creatures;;
 	private Item[][][] items;
 
 	/**
 	 * @param tiles
 	 *            The array of tiles that makes up the basis for the dungeon.
 	 */
 	public World(Tile[][][] tiles) {
 		this.tiles = tiles;
 		this.width = tiles.length;
 		this.height = tiles[0].length;
 		this.depth = tiles[0][0].length;
 		this.creatures = new ArrayList<Creature>();
 		this.items = new Item[width][height][depth];
 	}
 
 	/**
 	 * Puts a creature into a random spot on a dungeon level.
 	 * 
 	 * @param creature
 	 *            Creature to place
 	 * @param z
 	 *            Level in which to place creature
 	 */
 	public void addAtEmptyLocation(Creature creature, int z) {
 		int x;
 		int y;
 
 		do {
 			x = (int) (Math.random() * width);
 			y = (int) (Math.random() * height);
 		} while (!tile(x, y, z).isGround() || creature(x, y, z) != null);
 
 		creature.x = x;
 		creature.y = y;
 		creature.z = z;
 		creatures.add(creature);
 	}
 
 	/**
 	 * Puts an item into a random spot on a dungeon level.
 	 * 
 	 * @param item
 	 *            Item to place
 	 * @param depth
 	 *            Level in which to place item
 	 */
 	public void addAtEmptyLocation(Item item, int depth) {
 		int x;
 		int y;
 
 		do {
 			x = (int) (Math.random() * width);
 			y = (int) (Math.random() * height);
 		} while (!tile(x, y, depth).isGround() || item(x, y, depth) != null);
 
 		items[x][y][depth] = item;
 	}
 
 	/**
 	 * puts an item at or around the given coordinate.
 	 * 
 	 * @param item
 	 *            Item to place
 	 * @param x
 	 *            coordinate
 	 * @param y
 	 *            coordinate
 	 * @param z
 	 *            coordinate
 	 */
	public void addAtEmptySpace(Item item, int x, int y, int z) {
 		/*
 		 * TODO: This routine will currently fail silently if there is no empty
 		 * space in which to place the item and the item will simply disappear
 		 * from the game. This should probably be fixed so the player is warned
 		 * and he can keep the item instead.
 		 */
 		if (item == null)
			return;
 
 		List<Point> points = new ArrayList<Point>();
 		List<Point> checked = new ArrayList<Point>();
 
 		points.add(new Point(x, y, z));
 
 		while (!points.isEmpty()) {
 			Point p = points.remove(0);
 			checked.add(p);
 
 			if (!tile(p.x, p.y, p.z).isGround())
 				continue;
 
 			if (items[p.x][p.y][p.z] == null) {
 				items[p.x][p.y][p.z] = item;
 				Creature c = this.creature(p.x, p.y, p.z);
 				if (c != null)
 					c.notify("A %s lands between your feet.", item.name());
				return;
 			} else {
 				List<Point> neighbors = p.neighbors8();
 				neighbors.removeAll(checked);
 				points.addAll(neighbors);
 			}
 		}
 	}
 
 	/**
 	 * Find the color that this tile should be. Whether that be an item, a
 	 * critter, or simply the color of the floor.
 	 * 
 	 * @param x
 	 * @param y
 	 * @param z
 	 * @return The Color to display for a specific dungeon Tile
 	 */
 	public Color color(int x, int y, int z) {
 		Creature creature = creature(x, y, z);
 
 		if (creature != null)
 			return creature.color();
 		if (item(x, y, z) != null)
 			return item(x, y, z).color();
 
 		return tile(x, y, z).color();
 	}
 
 	/**
 	 * Checks for a creature at the coordinates and returns it if possible.
 	 * 
 	 * @param x
 	 * @param y
 	 * @param z
 	 * @return the Creature at the given coordinates
 	 */
 	public Creature creature(int x, int y, int z) {
 		for (Creature c : creatures) {
 			if (c.x == x && c.y == y && c.z == z)
 				return c;
 		}
 		return null;
 	}
 
 	/**
 	 * @return depth of the dungeon
 	 */
 	public int depth() {
 		return depth;
 	}
 
 	/**
 	 * Check if the coordinates are diggable and turn the tile into a floor if
 	 * they are.
 	 * 
 	 * @param x
 	 * @param y
 	 * @param z
 	 */
 	public void dig(int x, int y, int z) {
 		if (tile(x, y, z).isDiggable())
 			tiles[x][y][z] = Tile.FLOOR;
 	}
 
 	/**
 	 * Checks what char should be displayed for the given coordinates; whether
 	 * item or critter or floor
 	 * 
 	 * @param x
 	 * @param y
 	 * @param z
 	 * @return the char for that position in the world
 	 */
 	public char glyph(int x, int y, int z) {
 		Creature creature = creature(x, y, z);
 		if (creature != null)
 			return creature.glyph();
 		if (item(x, y, z) != null)
 			return item(x, y, z).glyph();
 		return tile(x, y, z).glyph();
 	}
 
 	/**
 	 * @return height, or y dimension, of the dungeon
 	 */
 	public int height() {
 		return height;
 	}
 
 	/**
 	 * Looks for an item on the given coordinates and returns what it finds.
 	 * 
 	 * @param x
 	 * @param y
 	 * @param z
 	 * @return the item on the given Coordinates
 	 */
 	public Item item(int x, int y, int z) {
 		return items[x][y][z];
 	}
 
 	/**
 	 * removes a given creature from the world.
 	 * 
 	 * @param other
 	 *            The Creature to remove
 	 */
 	public void remove(Creature other) {
 		creatures.remove(other);
 	}
 
 	/**
 	 * Removes item from the given coordinates
 	 * 
 	 * @param x
 	 * @param y
 	 * @param z
 	 */
 	public void remove(int x, int y, int z) {
 		items[x][y][z] = null;
 	}
 
 	/**
 	 * @param x
 	 * @param y
 	 * @param z
 	 * @return the tile for the given coordinates
 	 */
 	public Tile tile(int x, int y, int z) {
 		if (x < 0 || x >= width || y < 0 || y >= height || z < 0 || z >= depth)
 			return Tile.BOUNDS;
 		else
 			return tiles[x][y][z];
 	}
 
 	/**
 	 * Update the world state. This means all the critters get a turn.
 	 */
 	public void update() {
 		List<Creature> toUpdate = new ArrayList<Creature>(creatures);
 		for (Creature creature : toUpdate) {
 			creature.update();
 		}
 	}
 
 	/**
 	 * @return the width, or x dimension, of the world
 	 */
 	public int width() {
 		return width;
 	}
 }
