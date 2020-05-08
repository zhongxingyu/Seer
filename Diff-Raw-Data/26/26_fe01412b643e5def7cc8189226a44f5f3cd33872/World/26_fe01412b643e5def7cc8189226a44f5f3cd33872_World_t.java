 package Game;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.image.BufferedImage;
 import java.util.LinkedList;
 
 public class World 
 {
 	private float worldx;
 	private float worldy;
 	private float oldworldx;
 	private float oldworldy;
 	private Player player;
 	private Tile[][] tiles;
 	private LinkedList<Bullet> bullets;
 	static int level = 0;
 	static int width;
 	static int height;
 	public World()
 	{
 		player = new Player(Main.width/2 - 150, Main.height/2 - 150);
 		
 		loadNextLevel();
 		
 		bullets = new LinkedList<Bullet>();
 	}
 	public void loadNextLevel()
 	{
 		BufferedImage map = Imageloader.loadImage("Map" + level);
 		
 		width = map.getWidth();
 		height = map.getHeight();
 		
 		tiles = new Tile[width][height];
 		
 		for(int x = 0; x < width; x++)
 		{
 			for(int y = 0; y < height; y++)
 			{
 				Color c = new Color(map.getRGB(x, y));
 				if(c.getRed() == 0 && c.getGreen() == 255 && c.getBlue() == 0) tiles[x][y] = new Tile(x, y, 0);
 				if(c.getRed() == 0 && c.getGreen() == 0 && c.getBlue() == 255) tiles[x][y] = new Tile(x, y, 1);
 				if(c.getRed() == 255 && c.getGreen() == 255 && c.getBlue() == 0) tiles[x][y] = new Tile(x, y, 2);
 				if(c.getRed() == 0 && c.getGreen() == 0 && c.getBlue() == 0) tiles[x][y] = new Tile(x, y, 3);
 			}
 		}
 	}
 	public void update(float tslf)
 	{
 		oldworldx = worldx;
 		oldworldy = worldy;
 		if(player.getXpos() == Main.width/2 - 15)worldx += player.getXSpeed() * tslf;
 		if(player.getYpos() == Main.height/2 -15)worldy += player.getYSpeed() * tslf;
 		
 		boolean onwallx = false;
 		boolean onwally = false;
 		if(worldx < 0)
 		{
 			worldx = 0;
 			onwallx = true;
 		}
 		if(worldy < 0)
 		{
 			worldy = 0;
 			onwally = true;
 		}
 		int mapwidth = width * Texture.tilesize;
 		int mapheight = height * Texture.tilesize;
 		if(worldx > mapwidth - Main.width)
 		{
 			worldx = mapwidth - Main.width;
 			onwallx = true;
 		}
 		if(worldy > mapheight - Main.height)
 		{
 			worldy = mapheight - Main.height;
 			onwally = true;
 		}
 		
 		if((player.getXpos() < Main.width/2 - Player.size/2 && worldx == 0) || (player.getXpos() > Main.width/2 - Player.size/2 && worldx == mapwidth - Main.width)) onwallx = true;
 		else player.setXpos(Main.width/2 -Player.size/2);
 		
 		if((player.getYpos() < Main.height/2 - Player.size/2 && worldy == 0)|| (player.getYpos() > Main.height/2 - Player.size/2 && worldy == mapheight - Main.height)) onwally = true;
 		else player.setYpos(Main.height/2 - Player.size/2);
 		
 		
 		player.update(tslf, onwallx, onwally);
 		
 		if(Keyboard.getButton() == 1 && player.shoot())
 		{
 			bullets.add(new Bullet(worldx + player.getXpos() + Player.size/2, worldy + player.getYpos() + Player.size/2, 
 					(float) (Math.cos(player.getRadianDegrees()) * Bullet.MAXSPEED), 
					(float) (Math.sin(player.getRadianDegrees()) * Bullet.MAXSPEED) ));
 		}
 		for(int i = 0; i < bullets.size(); i++)
 		{
 			if(bullets.get(i).update(tslf, tiles)) bullets.remove(i);
 		}
 		
 //		for(int x = 0; x < 2; x++)
 //		{
 //			for(int y = 0; y < 2; y++)
 //			{
 //				int playerposx = (int) ((worldx + player.getXpos() + x * Player.size) / Texture.tilesize);
 //				int playerposy = (int) ((worldy + player.getYpos() + y * Player.size) / Texture.tilesize);
 //				if(tiles[playerposx][playerposy].getLookID() == 3)
 //				{
 //					player.setToOld();
 //					setToOld();
 //				}
 //				if(tiles[playerposx][playerposy].getLookID() == 1)
 //				{
 //					player.setToOld();
 //					setToOld();
 //				}
 //			}
 //		}
 		
 		for(int x = 0; x < 2; x++)
 		{
 			for(int y = 0; y < 2; y++)
 			{
 				int playerposx = (int) ((worldx + player.getXpos() + x * Player.size) / Texture.tilesize);
 				int playerposy = (int) ((worldy + player.getYpos() + y * Player.size) / Texture.tilesize);
 				if(tiles[playerposx][playerposy].getLookID() == 3 && Collision.circleToRect(player.getXpos() + worldx + Player.size/2, player.getYpos() + worldy + Player.size/2, Player.size/2, playerposx * Texture.tilesize, playerposy * Texture.tilesize, Texture.tilesize, Texture.tilesize))
 				{
 					player.setToOld();
 					setToOld();
 				}
 				if(tiles[playerposx][playerposy].getLookID() == 1 && Collision.circleToRect(player.getXpos() + worldx + Player.size/2, player.getYpos() + worldy + Player.size/2, Player.size/2, playerposx * Texture.tilesize, playerposy * Texture.tilesize, Texture.tilesize, Texture.tilesize))
 				{
 					player.setToOld();
 					setToOld();
 				}
 			}
 		}
 	}
 	public void setToOld()
 	{
 		worldx = oldworldx;
 		worldy = oldworldy;
 	}
 	public void draw(Graphics g)
 	{	
 		int minx = (int) (worldx / Texture.tilesize);
 		int maxx = (int) ((worldx + Main.width) / Texture.tilesize) + 1;
 		int miny = (int) (worldy / Texture.tilesize);
 		int maxy = (int) ((worldy + Main.height) / Texture.tilesize) + 1;
 		if(maxx > width) maxx = width;
 		if(maxy > height) maxy = height;
 		for(int x = minx; x < maxx; x++)
 		{
 			for (int y = miny; y < maxy; y++) 
 			{
 				tiles[x][y].draw(g, (int) worldx * -1, (int) worldy * -1);
 			}
 		}
 		for(int i = 0; i < bullets.size(); i++)
 		{
			bullets.get(i).draw(g, (int) -worldx, (int) -worldy);
 		}	
 		player.draw(g);
 	}
 }
