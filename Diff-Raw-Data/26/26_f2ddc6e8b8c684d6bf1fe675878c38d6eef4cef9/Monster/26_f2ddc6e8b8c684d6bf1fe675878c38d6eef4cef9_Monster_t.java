 /*
  * Adventron - Stan Schwertly
  * 
  * Monster.java
  * Represents the monsters on the map and their functions
  */
 
 import java.awt.Color;
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.util.ArrayList;
 
 public class Monster
 {	
 	public static int HEIGHT = 10;
 	public static int WIDTH = 8;
 	public static Color COLOR = Color.yellow;
 	
 	private Point position;
 	private Point direction;
 	private Rectangle box;
 	private int quadrant;
 	
 	private boolean hit;
 	private boolean hitByPlayer;
 	
	// Maybe phase out/replace with default constructor?
 	public Monster()
 	{
 		// check for walls first
 		position = new Point((int)Math.round(Math.random()*700), (int)Math.round(Math.random()*100));
 		direction = new Point(1, 1);
 		box = new Rectangle(position.x, position.y, WIDTH, HEIGHT);
 		determineQuadrant();
 		
 		hit = false;
 		hitByPlayer = false;
 	}
 	
 	public Monster(Point p)
 	{
 		position = p;
 		direction = new Point(1, 1);
 		box = new Rectangle(position.x, position.y, WIDTH, HEIGHT);
 		determineQuadrant();
 		
 		hit = false;
 		hitByPlayer = false;
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
 		direction=d;
 	}
 	
 	/**
 	 * @return the quadrant
 	 */
 	public int getQuadrant() {
 		return quadrant;
 	}
 
 	/**
 	 * @param quadrant the quadrant to set
 	 */
 	public void setQuadrant(int quadrant) {
 		this.quadrant = quadrant;
 	}
 
 	/**
 	 * @return the box
 	 */
 	public Rectangle getBox() {
 		return box;
 	}
 
 	/**
 	 * @param box the box to set
 	 */
 	public void setBox(Rectangle box) {
 		this.box = box;
 	}
 
 	/**
 	 * @return the hit
 	 */
 	public boolean isHit() {
 		return hit;
 	}
 
 	/**
 	 * @param hit the hit to set
 	 */
 	public void setHit(boolean hit) {
 		this.hit = hit;
 	}
 
 	public void setHitByPlayer(boolean hitByPlayer) {
 		this.hitByPlayer = hitByPlayer;
 	}
 
 	public boolean isHitByPlayer() {
 		return hitByPlayer;
 	}
 
 	public void changePosition(ArrayList <Rectangle>walls, ArrayList <Bullet> bullets)
 	{
 		// Temporary. Makes the monster move random.
 		if (Math.random() < .5) direction.y *= -1;
 		
 		// keep him on the screen
 		if ( (position.x + direction.x > Map.WIDTH) || (position.x + direction.x < 0))
 			direction.x *= -1;
 		else if ( (position.y + direction.y > Map.HEIGHT) || (position.y + direction.y < 20))
 			direction.y *= -1;
 		
 		Rectangle temp = new Rectangle(
 				position.x + direction.x, 
 				position.y + direction.y,
 				HEIGHT, WIDTH); 
 		
 		// If he hits a wall, bounce off
 		for (int i=0; i<walls.size(); i++)
 		{
 			if (temp.intersects(walls.get(i)))
 			{
 				direction.setLocation(direction.x * -1, direction.y * -1);
 				break;
 			}
 		}
 		
 		// finally: move, update the box and figure out where he is
 		position.x += direction.x;
 		position.y += direction.y;
 		box.setRect(position.x, position.y, WIDTH, HEIGHT);
 		determineQuadrant();		
 		
 		// temporary. shoot down once in a while
 		if (Math.random() < .1)
 			bullets.add(new Bullet(position.x, position.y + HEIGHT + 1, Player.DOWN));
 	}
 
 	private void determineQuadrant()
 	{
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
 }
