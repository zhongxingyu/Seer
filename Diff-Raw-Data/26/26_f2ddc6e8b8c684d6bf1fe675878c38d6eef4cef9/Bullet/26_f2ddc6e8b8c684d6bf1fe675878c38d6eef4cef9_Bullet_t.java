 /*
  * Adventron - Stan Schwertly
  * 
  * Bullet.java
  * Represents the bullets shot by both players and monsters
  */
 
 import java.awt.Color;
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.util.ArrayList;
 
 public class Bullet
 {
 	private static final int SPEED = 2;
 	
 	// directions
 	public static final Point UP = new Point(0,-SPEED);
 	public static final Point DOWN =  new Point(0,SPEED);
 	public static final Point LEFT = new Point(-SPEED,0);
 	public static final Point RIGHT =  new Point(SPEED,0);
 	
 	public static final int WIDTH = 3;
 	public static final int HEIGHT = 2;
 	
 	public static final Color COLOR = Color.cyan;
 	
 	public static Map map;
 	public static ArrayList<Rectangle> walls[];
 	
 	private Point position;
 	private Point direction;
 	private int quadrant;
 	private boolean isPlayerBullet;
 	
 	public Bullet()
 	{
 		position = new Point();
 		direction = new Point();
 		determineQuadrant();
 		isPlayerBullet = false;
 	}
 	
 	public Bullet(int x, int y)
 	{
 		position = new Point(x,y);
 		direction = UP;
 		determineQuadrant();
 		isPlayerBullet = false;
 	}
 	
 	public Bullet(int x, int y, Point d)
 	{
 		position = new Point(x,y);
 		direction = d;
 		determineQuadrant();
 		isPlayerBullet = false;
 	}
 	
 	public Bullet(int x, int y, Point d, boolean ipb)
 	{
 		position = new Point(x,y);
 		direction = d;
 		determineQuadrant();
 		isPlayerBullet = ipb;
 	}
 	
 	public Point getPosition()
 	{
 		return position;
 	}
 	
 	public Point getDirection()
 	{
 		return direction;
 	}
 	
 	public void setDirection(Point d)
 	{
 		direction = d;
 	}
 	
 	public int getQuadrant()
 	{
 		return quadrant;
 	}
 	
 	public void changePosition(Player player, ArrayList <Monster> monsters)
 	{
 		Rectangle newPosition = new Rectangle(
 				position.x + direction.x, 
 				position.y + direction.y,
 				WIDTH, HEIGHT); 
 	
 		// If I'm hitting a player, tell him and then get set to die
 		if (newPosition.intersects(player.getBox()))
 		{
 			player.setHit(true);
 			quadrant = Map.OUT_OF_BOUNDS;
 			return;
 		}
 		
 		// if I hit a monster, tell the monster, then die
 		for (int i=0; i<monsters.size(); i++)
 		{
 			if (newPosition.intersects(monsters.get(i).getBox()))
 			{
 				monsters.get(i).setHit(true);
 				
 				if (isPlayerBullet)
 					monsters.get(i).setHitByPlayer(true);
 				
 				quadrant = Map.OUT_OF_BOUNDS;
 				return;
 			}
 		}
 		
 		// if I hit a wall, set my quadrant to die
 		for (int i=0; i<walls[quadrant].size(); i++)
 		{
 			if (newPosition.intersects(walls[quadrant].get(i)))
 			{
 				quadrant = Map.OUT_OF_BOUNDS;
 				return;
 			}
 		}
 		
 		// finally: move and figure out where I am
 		position.x += direction.x;
 		position.y += direction.y;
 		determineQuadrant();
 	}
 	
 	private void determineQuadrant()
 	{
 		//Is the bullet on the map?
 		if (    (position.x > Map.WIDTH) ||
 				(position.x < 0) ||
 				(position.y > Map.HEIGHT) ||
				(position.y < 20) )
 		{
 			quadrant = Map.OUT_OF_BOUNDS;
 			return;
 		}
 		
 		if (position.x < Map.HALF_WIDTH)
 		{
 			if (position.y < Map.HALF_HEIGHT)
 				quadrant = Map.TOP_LEFT;
 			else
 				quadrant = Map.BOTTOM_LEFT;
 		}
 		else if (position.y < Map.HALF_HEIGHT)
 			quadrant = Map.TOP_RIGHT;
 		else quadrant = Map.BOTTOM_RIGHT;
 	}
 	
 	public static void setMap(Map m)
 	{
 		map = m;
 		for (int i=0; i<4; i++)
 		{
 			walls[i] = map.getWalls(i);
 		}
 	}
 	
 	public static Map getMap()
 	{
 		return map;
 	}
 	
 	public void setPlayerBullet(boolean isPlayerBullet) {
 		this.isPlayerBullet = isPlayerBullet;
 	}
 
 	public boolean isPlayerBullet() {
 		return isPlayerBullet;
 	}
 
 	public static void initWalls()
 	{
 		walls = new ArrayList[4];
 		for (int i=0; i<4; i++)
 		{
 			walls[i] = new ArrayList<Rectangle>();
 		}
 	}
 }
