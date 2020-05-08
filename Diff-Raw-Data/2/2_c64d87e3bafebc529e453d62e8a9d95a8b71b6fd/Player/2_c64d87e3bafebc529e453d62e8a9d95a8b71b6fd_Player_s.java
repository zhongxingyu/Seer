 package apra.trainGame;
 import java.util.ArrayDeque;
 import java.util.ArrayList;
 import java.awt.Color;
 import java.awt.Graphics;
 
 import apra.trainGame.pc.DrawSurface;
 
 public class Player
 {
 	/*
 	 * Physics constants
 	 */
     public static final float MIN_SPEED 			= .0025f / 16.0f;
     public static final float ACCEL 				= .00001f / (16.0f * 16.0f);
     public static final float COLLISION_WIDTH 		= .002f;
     public static final float ACCEL_WIDTH 			= .08f;
     public static final float DECEL_MODIFIER 		= 1.0f;
     public static final float SELF_ACCEL_MODIFIER 	= -1.0f; // -1.0f is default behavior
     public static final int MULTI_ACCEL 			= 1;
     
     /*
      * Drawing options
      */
     private static final boolean DRAW_HEAD_BOX = true;
     private static final boolean DRAW_TAIL_BOX = false;
 
     /*
      * Misc
      */
     public enum End { HEAD, TAIL }
     
     /*
      * Class fields
      */
     private ArrayDeque<Vector2D> segments;
     private Vector2D headPosition;
     private Vector2D direction;
     private float headSpeed;
     private float tailSpeed;
     private float headAcceleration;
     private float tailAcceleration;
     private boolean isDead;
     private int color;
     private boolean canTurn; // XXX this is a quick-fix, consider changing
     
     /**
      * Color only constructor for player.
      * 
      * @param color <code>Color</code> to use
      */
     public Player(int color)
     {
     	this(new Vector2D(0,0), VectorDirection.RIGHT, 0.0f, color);
     }
 
     /**
      * Full constructor for player.
      * 
      * @param position <code>Vector2D</code> position to use
      * @param direction <code>Vector2D</code> direction to use, see <code>VectorDirection</code> constants
      * @param length length to use
      * @param color <code>Color</code> to use
      */
     public Player(Vector2D position, Vector2D direction, float length, int color)
     {
     	this.headPosition = position;
     	this.direction = direction.copy();
     	this.headSpeed = MIN_SPEED;
     	this.tailSpeed = MIN_SPEED;
     	this.headAcceleration = 0.0f;
     	this.tailAcceleration = 0.0f;
     	this.isDead = false;
     	this.color = color;
     	
     	Vector2D segment = new Vector2D(direction);
     	segment.mult(length);
     	
     	segments = new ArrayDeque<Vector2D>();
     	segments.add(segment);
     }
     
     /**
      * Returns the color of the player.
      * 
      * @return <code>Color</code> of the train
      */
     public int getColor()
     {
     	return color;
     }
        
     /**
      * Draw the player to a <code>Graphics</code>.
      * 
      * @param ds <code>Graphics</code> to draw to
      */
     public void draw(DrawSurface ds)
     {
     	// draw the physical train
     	for (Rectangle r : getRectangles())
     	{
     		ds.setColor(color);
     		ds.fillRect(r);
     	}
 
     	// draw the head acceleration box
     	if (DRAW_HEAD_BOX)
 	    {
     		ds.setColor(color - 0xff000000 + 0x20000000);
     		Rectangle r = getEndBox(End.HEAD);
     		ds.fillRect(r);
 	    }
 	
     	// draw the tail acceleration box
     	if (DRAW_TAIL_BOX)
 	    {
     		ds.setColor(color - 0xff000000 + 0x10000000);
     		Rectangle r = getEndBox(End.TAIL);
     		ds.fillRect(r);
 	    }
     }
 
     /**
      * Initialize the player's state.
      * 
      * @param position <code>Vector2D</code> position to use
      * @param direction <code>Vector2D</code> direction to use, see VectorDirection constants
      * @param length length to use
      */
     public void init(Vector2D position, Vector2D direction, float length)
     {
     	// TODO find out if this is necessary
     	this.headPosition = position;
     	this.direction = direction.copy();
     	this.headSpeed = MIN_SPEED;
     	this.tailSpeed = MIN_SPEED;
     	this.headAcceleration = 0.0f;
     	this.tailAcceleration = 0.0f;
     	this.isDead = false;
     	
     	Vector2D segment = new Vector2D(direction);
     	segment = segment.mult(-1.0f*length);
     	
     	segments = new ArrayDeque<Vector2D>();
     	segments.add(segment);
     }
     
     /**
      * Set if the player is dead.
      * 
      * @param isDead <code>true</code> to set the train to dead
      */
     public void setDead(boolean isDead)
     {
     	this.isDead = isDead;
     }
       
     /**
      * Returns <code>true</code> if the player is dead.
      *
      * @return <code>true</code> if the player is dead
      */
     public boolean isDead()
     {
     	return isDead;
     }
  
     /**
      * Set the direction of the player.
      * 
      * @param direction <code>Vector2D</code> direction to use, see <code>VectorDirection</code> constants
      */
     public void setDirection(Vector2D direction)
     {
     	if (! (this.direction.equals(direction) || this.direction.mult(-1).equals(direction)))
     	{
     		if (canTurn) // XXX see canTurn
     		{
     			this.direction = direction.copy();
     			canTurn = false;
     		}
     	}
     }
 
     /**
      * Returns the position of the player.
      * 
      * @return position of the player
      */
     public Vector2D getPosition()
     {
     	return headPosition;
     }
     
     /**
      * Update the player based on the the elapsed time.
      * 
      * @param elapsed time in milliseconds since the last update
      * @param game reference to <code>TrainGame</code> the player belongs to
      */
     public void update(long elapsed, TrainGame game)
     {
     	updateAccelerations(game);
     	updatePositions(elapsed);
     	
    	canTurn = false; // XXX see canTurn
 
     	// check for player collisions
     	for (Player player : game.getPlayers())
     	{
     		if (player.checkCollisions(this) > 0)
     			isDead = true;
     	}
     	
     	// check for out of bounds
     	if (! game.inBounds(getRectangles().get(0)))
     		isDead = true;
     }
     
     /**
      * Do the physics updates for the game.
      * 
      * @param elapsed time since last update
      */
     private void updatePositions(long elapsed)
     {
     	// calculate new speed
     	headSpeed += headAcceleration * (float)elapsed;
 
     	if (headSpeed < MIN_SPEED)
     		headSpeed = MIN_SPEED;
     	
     	// calculate change in position and update head position
     	Vector2D deltaPos = (direction).mult(headSpeed * (float)elapsed);
     	headPosition = headPosition.add(deltaPos);
     	
     	// check if direction has changed => push new segment
     	if (!direction.equals(segments.getFirst().norm().mult(-1.0f)))
     	{
     		segments.addFirst(new Vector2D(0, 0));
     	}
     	
     	// update the first segment
     	Vector2D newVec = segments.removeFirst().add(deltaPos.mult(-1.0f));
     	segments.addFirst(newVec);
     	
     	// calculate new tail speed
     	tailSpeed += tailAcceleration * elapsed;
     	
     	if (tailSpeed < MIN_SPEED)
     		tailSpeed = MIN_SPEED;
     	
     	// calculate distance to be covered by the tail
     	float deltaLength = tailSpeed * elapsed;
     	
     	// shorten and remove tail segments as necessary
     	Vector2D segment;
     	while (deltaLength > 0)
     	{
     		segment = segments.removeLast();
     		
     		// segment is shorter => delete
     		if (segment.magnitude() <= deltaLength)
     		{
     			deltaLength -= segment.magnitude();
     			
     			// was the last segment
     			if (segments.isEmpty())
     			{
     				isDead = true;
     				break;
     			}
     		}
     		else	// segment is longer => shrink
     		{
     			float newLength = segment.magnitude() - deltaLength;
     			segments.addLast(segment.norm().mult(newLength));
     			
     			deltaLength = 0;
     		}
     	}
     }
     
     /**
      * Returns a square <code>Rectangle</code> centered on the specified end of the player.
      * 
      * @param end end of the player to get a box around
      * @return square <code>Rectangle</code> centered on the end
      */
     public Rectangle getEndBox(End end)
     {
     	int ppu = TrainGame.PIXELS_PER_UNIT;
 		int width = (int) (ACCEL_WIDTH * ppu);
 		int x = 0;
 		int y = 0;
     	
     	if (end == End.HEAD)
     	{    		
     		x = (int) (ppu * (headPosition.x - ACCEL_WIDTH / 2));
     		y = (int) (ppu * (headPosition.y - ACCEL_WIDTH / 2));
     	}
     	else
     	{
    		Vector2D tailPosition = getTailPosition();
         	x = (int) (ppu * (tailPosition.x - ACCEL_WIDTH / 2));
     		y = (int) (ppu * (tailPosition.y - ACCEL_WIDTH / 2));
     	}
     	
     	return new Rectangle(x, y, width, width);
     }
     
     /**
      * Update the accelerations to the appropriate values.
      * 
      * @param game reference to <code>TrainGame</code> the player belongs to
      */
     private void updateAccelerations(TrainGame game)
     {
     	// TODO combine these into a method somehow
     	
     	int accelCollisions = 0;
     	
 		Rectangle r = getEndBox(End.HEAD);
     	
     	for (Player player : game.getPlayers())
     	{
     		if (player != this)
     			accelCollisions += player.checkCollisions(r);
     	}
     	
     	if (accelCollisions > 0)
     	{
     		headAcceleration = ACCEL * Math.min(accelCollisions, MULTI_ACCEL); // TODO make this more versitile
     	}
     	else if (checkCollisions(r) > 0) // TODO figure out this + multi-accel
     	{
     		headAcceleration = ACCEL * SELF_ACCEL_MODIFIER;
     	}
     	else
     	{
     		headAcceleration = (float) (-1 * ACCEL * DECEL_MODIFIER);
     	}
     	
 		r = getEndBox(End.TAIL);
     	
 		accelCollisions = 0;
 		
     	for (Player player : game.getPlayers())
     	{
     		if (player != this)
     			accelCollisions += player.checkCollisions(r);
     	}
     	
     	if (accelCollisions > 0)
     	{
     		tailAcceleration = (float) ACCEL * Math.min(accelCollisions, MULTI_ACCEL); // TODO make this more versitile
     	}
     	else if (checkCollisions(r) > 0) // TODO figure out this + multi-accel
     	{
     		tailAcceleration = ACCEL * SELF_ACCEL_MODIFIER;
     	}
     	else
     	{
     		tailAcceleration = (float) (-1 * ACCEL * DECEL_MODIFIER);
     	}
     }
     
     /**
      * Check for collisions between the given <code>Rectangle</code> and the player.
      * 
      * @param r <code>Rectangle</code> to check for collisions with
      * @return number of segments collided with
      */
     public int checkCollisions(Rectangle r)
     {
     	return checkCollisions(r, 0);
     }
     
     /**
      * Check for collisions ignoring the first number of segments.
      * 
      * @param r <code>Rectangle</code> to check for collisions with
      * @param begin first segment to check with
      * @return number of segments collided with
      */
     private int checkCollisions(Rectangle r, int begin)
     {
     	int numCollisions = 0;
     	
     	ArrayList<Rectangle> rectangles = getRectangles();
     	
     	while (begin > 0 && !rectangles.isEmpty())
     	{
     		rectangles.remove(0);
     		begin--;
     	}
     	
     	for (Rectangle rect : rectangles)
     	{
     		if (r.intersects(rect))
     			numCollisions++;
     	}
     	
     	return numCollisions;
     }
     
     /**
      * Check for collisions with another player
      * 
      * @param player <code>Player</code> to check for collisions with
      * @return non-zero if there is a collision
      */
     public int checkCollisions(Player player)
     {
     	int begin = (player == this) ? 2 : 0;
     	
     	Rectangle r = player.getRectangles().get(0);
     	
     	return checkCollisions(r, begin);
     }
 
     /**
      * Returns the <code>Vector2D</code> position of the tail end of the player
      * 
      * @return <code>Vector2D</code> position of the tail
      */
     public Vector2D getTailPosition()
     {
     	Vector2D tailPosition = headPosition;
     	
     	for (Vector2D v : segments)
     		tailPosition = tailPosition.add(v);
     	
     	return tailPosition;
     }
     
     /**
      * Returns a list of rectangles which define the player.
      * 
      * @return list of rectangles which define the player
      */
     public ArrayList<Rectangle> getRectangles()
     {	
     	ArrayList<Rectangle> rectangles = new ArrayList<Rectangle>();
     	Vector2D pos = new Vector2D(headPosition);
     	
     	for (Vector2D segment : segments)
     	{
     		// find the dimensions of the rectangle
     		float width = Math.abs(segment.x) + (float) COLLISION_WIDTH;
     		float height = Math.abs(segment.y) + (float) COLLISION_WIDTH;
     		
     		// find the top-left corner of the rectangle
     		Vector2D bigOffset = new Vector2D(Math.min(segment.x, 0), Math.min(segment.y, 0));
     		Vector2D smallOffset = (new Vector2D(1,1)).mult((float) COLLISION_WIDTH / 2.0f * -1.0f);
     		Vector2D topLeft = pos.add(bigOffset).add(smallOffset);
     		
     		// TODO reconsider this
     		int ppu = TrainGame.PIXELS_PER_UNIT;
     		
     		Rectangle r = new Rectangle((int) (ppu * topLeft.x), (int) (ppu * topLeft.y),
     				(int) (ppu * width), (int) (ppu * height));
     		
     		rectangles.add(r);
 		pos = pos.add(segment);
     	}
     	
     	return rectangles;
     }
 }
