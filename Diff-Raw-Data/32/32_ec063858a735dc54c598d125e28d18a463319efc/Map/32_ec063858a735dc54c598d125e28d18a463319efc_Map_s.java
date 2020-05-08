 /*
  * Map.java
  * 
  * Part of the GameSDK starterkit made for the seminar at "The Gathering 2012 - At the end of the universe"
  * 
  * This source code is provided AS-IS and without any warranty. Use at own risk.
  * You can use and modify this source any way you want, but please send me an E-mail to petterroea@skymiastudios.com
  * if you make a game from it - I would like to keep a list of all games, as i am curious. You may also use this for games
  * that cost money. So it is basically public domain, except that i own the rights to it, but give you the rights to use
  * it for the purposes above. You cant sell this kit. You can sell a game based on it, but not the source code to this kit.
  * Also, this big comment must stay in all code files that originated from the starterkit, no matter how much they are
  * modified.
  * 
  * If you want support for this SDK, feel free to contact me. If you are at "The Gathering 2012", you can
  * look for me in the creative lounge(Where i sit). If not, feel free to send me an E-Mail to the above
  * adress. If you need normal java help, ask a mentor.
  * 
  */
 
 package net.petterroea.starterkit;
 
 import java.awt.Graphics;
 import java.awt.image.BufferedImage;
 import java.util.LinkedList;
 /**
  * This class is used to contain a array of Tiles
  * @author petterroea
  *
  */
 public class Map {
 	/**
 	 * The entities
 	 */
 	LinkedList<Entity> entities;
 	/**
 	 * The array of tiles
 	 */
 	Tile[][] tiles;
 	/**
 	 * Width or Height of the tile array
 	 */
 	public int w, h;
 	/**
 	 * Controls the offset of the map drawn
 	 */
 	public int xoff, yoff;
 	/**
 	 * Width and height of a tile
 	 */
 	public int tilew, tileh;
 	/**
 	 * Constructs a map from a image containing level data
 	 * @param src The image with the level data
 	 * @param tilew Width of one tile
 	 * @param tileh Height of one tile
 	 */
 	public Map(BufferedImage src, int tilew, int tileh)
 	{
 		this.w = src.getWidth();
 		this.h = src.getHeight();
 		this.tileh = tileh;
 		this.tilew = tilew;
 		tiles = new Tile[w][h];
 		for(int x = 0; x < w; x++)
 		{
 			for(int y = 0; y < h; y++)
 			{
 				tiles[x][y] = LevelReader.getTile(src.getRGB(x, y), x, y);
 				tiles[x][y].gridx = x;
 				tiles[x][y].gridy = y;
 			}
 		}
 	}
 	/**
 	 * Returns the tile at the specific coordinates
 	 * @param x X coordinate
 	 * @param y Y coordinate
 	 * @return The tile at the x and y coordinate
 	 */
 	public Tile getTileAt(int x, int y)
 	{
 		return tiles[x][y];
 	}
 	/**
 	 * Updates ALL the entities! (Position)
 	 * @param delta Time since last update in millisessions
 	 */
 	public void updateEntities(int delta)
 	{
 		for(int i = 0; i < entities.size(); i++)
 		{
 			entities.get(i).update(delta, this, i);
 		}
 	}
 	/**
 	 * Draws the map to the screen
 	 * @param g The graphics object
 	 */
 	public void draw(Graphics g)
 	{
 		//Modify this if you want to use a different drawing method
 		for(int x = 0; x < tilew; x++)
 		{
 			for(int y = 0; y < tileh; y++)
 			{
 				if(tiles[x][y] != null)
 				{
 					g.drawImage(tiles[x][y].getTile(this), (x * tilew) + xoff, (y * tileh) + yoff, null);
 				}
 			}
 		}
 		for(int i = 0; i < entities.size(); i++)
 		{
 			entities.get(i).draw(g, xoff, yoff);
 		}
 	}
 }
