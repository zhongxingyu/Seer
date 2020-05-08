 package rltut;
 
 import java.awt.Color;
 
 import asciiPanel.AsciiPanel;
 
 /**
  * Class for a dungeon tile.
  * 
  * @author Jeremy Rist
  * 
  */
 public enum Tile {
 	/**
 	 * Floor tile
 	 */
 	FLOOR((char) 250, AsciiPanel.yellow, "A dirt and rock cave floor."),
 	/**
 	 * Wall Tile
 	 */
 	WALL((char) 177, AsciiPanel.yellow, "A dirt and rock cave wall."),
 	/**
 	 * Out of bounds
 	 */
 	BOUNDS('x', AsciiPanel.brightBlack, "The abyss beyond that which is known."),
 	/**
 	 * Downward Staircase
 	 */
 	STAIRS_DOWN('>', AsciiPanel.white, "A stone staircase leading down."),
 	/**
 	 * Upward Staircase
 	 */
 	STAIRS_UP('<', AsciiPanel.white, "A stone staircase leading up."),
 	/**
 	 * Unknown Tile
 	 */
 	UNKNOWN(' ', AsciiPanel.white, "Your guess is as good as mine.");
 
 	private char glyph;
 
 	private Color color;
 	
 	private String details;
 
 	/**
 	 * Create a new tile
 	 * 
 	 * @param glyph
 	 *            char to display
 	 * @param color
 	 *            color for the tile
 	 */
 	Tile(char glyph, Color color, String details) {
 		this.glyph = glyph;
 		this.color = color;
 		this.details = details;
 	}
 
 	/**
 	 * @return Color of the tile
 	 */
 	public Color color() {
 		return color;
 	}
 
 	/**
 	 * @return The tile's char
 	 */
 	public char glyph() {
 		return glyph;
 	}
 
 	/**
 	 * @return whether the player can dig in the tile
 	 */
 	public boolean isDiggable() {
 		return this == Tile.WALL;
 	}
 
 	/**
 	 * @return whether things can be placed on the tile
 	 */
 	public boolean isGround() {
 		return this != WALL && this != BOUNDS;
 	}
 	
 	
 	public String details() {
 		return details;
 	}
	}
	 */
 }
