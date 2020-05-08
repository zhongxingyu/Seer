 /*
  * Adventron - Stan Schwertly
  * 
  * Player.java
  * Represents the player on the map and his functions
  */
 
 import java.awt.Color;
 import java.awt.Point;
 import java.awt.Rectangle;
 
 public class Player
 {	
 	private static final int SPEED = 5;
 
 	//directions
 	public static final Point UP = new Point(0,-SPEED);
 	public static final Point DOWN =  new Point(0,SPEED);
 	public static final Point LEFT = new Point(-SPEED,0);
 	public static final Point RIGHT =  new Point(SPEED,0);
 	public static final Point STILL = new Point(0,0);
 	
 	public static final Color COLOR = Color.green;
 	
	public static final int HEIGHT = 10;
	public static final int WIDTH= 10;
 	
 	private Point position;
 	private Point direction;
 	private Point facing;
 	private int quadrant;
 	private Rectangle box;
 	
 	private boolean hit;
 	private int health;
 	private int score;
 	private int room;
 	private Map map;
 	
 	public Player()
 	{	
 		// 100, 100 is a good place to start on the first map. May change.
 		position = new Point(100, 100);
 		direction = STILL;
 		
 		// Facing determines the bullet's direction. Defaults to UP
 		facing = UP;
 		box = new Rectangle(position.x, position.y, WIDTH, HEIGHT);
 		quadrant = Map.TOP_LEFT;
 
 		// fighting variables
 		hit = false;
 		health = 3;
 		
 		score = 0;
 		room = 0;
 		setMap(new Map());
 	}
 	
 	public Point getPosition()
 	{
 		return position;
 	}
 	
 	public void changePosition(Point direction)
 	{
 		position.x += direction.x;
 		position.y += direction.y;
 		box.setRect(position.x, position.y, WIDTH, HEIGHT);
 	}
 	
 	public Point getDirection()
 	{
 		return direction;
 	}
 	
 	public void setDirection(Point d)
 	{
 		direction = d;
 	}
 	
 	/**
 	 * @return the facing
 	 */
 	public Point getFacing() {
 		return facing;
 	}
 
 	/**
 	 * @param facing the facing to set
 	 */
 	public void setFacing(Point facing) {
 		this.facing = facing;
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
 
 	/**
 	 * @return the health
 	 */
 	public int getHealth() {
 		return health;
 	}
 
 	/**
 	 * @param health the health to set
 	 */
 	public void setHealth(int health) {
 		this.health = health;
 	}
 
 	/**
 	 * @return the score
 	 */
 	public int getScore() {
 		return score;
 	}
 
 	/**
 	 * @param score the score to set
 	 */
 	public void setScore(int score) {
 		this.score = score;
 	}
 	
 	public void addToScore(int add) {
 		this.score += add;
 	}
 
 	/**
 	 * @return the room
 	 */
 	public int getRoom() {
 		return room;
 	}
 
 	/**
 	 * @param room the room to set
 	 */
 	public void setRoom(int room) {
 		this.room = room;
 	}
 
 	public void setMap(Map map) {
 		this.map = map;
 	}
 
 	public Map getMap() {
 		return map;
 	}
 
 	public boolean move()
 	{
 		// Don't let him move out of bounds.
 		/*if ( (position.x + WIDTH + direction.x > Map.WIDTH) || 
 				(position.x + direction.x < 0) ||
 				(position.y + HEIGHT + direction.y > Map.HEIGHT) || 
 				(position.y + direction.y < 0))
 			return;*/
 		if (position.x + WIDTH + direction.x > Map.WIDTH)
 		{
 			position.x=0;
 			room--;
 			return false;
 		}
 		else if (position.x + direction.x < 0)
 		{
 			room++;
 			position.x=Map.WIDTH-WIDTH;
 			return false;
 		}
 		else if (position.y + HEIGHT + direction.y > Map.HEIGHT)
 		{
 			room-=3;
 			position.y=20;
 			return false;
 		}
 		else if (position.y + direction.y < 20)
 		{
 			room+=3;
 			position.y=Map.HEIGHT-HEIGHT;
 			return false;
 		}
 		
 		Rectangle newPosition = new Rectangle(position.x + direction.x,
 				position.y + direction.y,
 				WIDTH, HEIGHT);
 		
 		// Don't move if he's going to hit a wall
 		for (int i=0; i<map.getWalls(quadrant).size(); i++)
 		{
 			if (newPosition.intersects(map.getWalls(quadrant).get(i)))
 				return true;
 		}
 		
 		// finally, move and figure out where he is
 		changePosition(direction);
 		determineQuadrant();
 		return true;
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
