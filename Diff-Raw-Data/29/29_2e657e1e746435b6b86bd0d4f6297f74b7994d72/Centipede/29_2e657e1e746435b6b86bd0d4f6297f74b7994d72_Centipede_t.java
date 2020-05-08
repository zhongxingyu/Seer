 package game;
 import gui.CentipedeSprite;
 import gui.EntitySprite;
 
 import java.awt.Point;
 import game.Rectangle;
 import java.awt.geom.Point2D;
 
 /**
  * A class which represents a Centipede segment. Centipedes are essentially
  * a list of linked nodes.
  *
  */
 public class Centipede implements Entity {
 	
 	private boolean 			m_isHead;
 	
 	/**
 	 * True if a centipede is moving leftward (if it is moving down, it will
 	 * need to turn right)
 	 */
 	private boolean				m_movingLeftward;
 	private Board				m_board;
 	private Point				m_location;
 	private EntitySprite		m_sprite;
 	private Rectangle			m_boundingBox;
 	private Direction			m_direction;
 	private Centipede			m_nextSegment;
 	
 	/**
 	 * The factor by which the square size will be divided to provide movement
 	 * rate.
 	 */
 	private int					m_speedFactor;
 	
 	/**
 	 * A decrement counter which when equal to 0 will signal the need for the
 	 * speed factor to increase.
 	 */
 	private int					m_speedCount;
 	
 	/**
	 * An increment counter which when equal to SQUARE_SIZE will signal the
	 * need for the centipede to change to moving left or right.
	 */
	private int					m_vertAmount;
	
	/**
 	 * The default decrement counter value.
 	 */
 	private final int			DEFAULT_SPEED_COUNT = 1000;
 	
 	private final int			SQUARE_SIZE = 50;
 	
 	/**
 	 * Construction of a Centipede requires a factory method in order to
 	 * create all segments and link them.
 	 * @param isHead	true if this segment is the head
 	 * @param board		the Board on which this centipede resides
 	 * @param loc		the location of this segment
 	 * @param dir		the direction in which this Centipede will travel
 	 * @param nextSeg	the next Centipede segment: null if this is the tail
 	 */
 	private Centipede(	boolean isHead,
 						Board board,
 						Point loc,
 						Direction dir,
 						Centipede nextSeg	) {
 		this.m_isHead = isHead;
 		this.m_movingLeftward = true;
 		this.m_board = board;
 		this.m_location = loc;
 		this.m_direction = dir;
 		this.m_nextSegment = nextSeg;
 		this.m_speedFactor = SQUARE_SIZE;
 		this.m_speedCount = DEFAULT_SPEED_COUNT;
		this.m_vertAmount = 0;
 		
 		// Set by the factory method
 		this.m_sprite = null;
 		this.m_boundingBox = null;
 	}
 	
 	public boolean isHead(){
 		return this.m_isHead;
 	}
 	
 	public Direction getDirection(){
 		return Direction.valueOf(this.m_direction.toString());
 	}
 	
 	public void move() {
 		if (--this.m_speedCount == 0) {
 			if (this.m_speedFactor != SQUARE_SIZE) {
 				++this.m_speedFactor;
 			}
 			this.m_speedCount = DEFAULT_SPEED_COUNT;
 		}
 		int moveAmount = SQUARE_SIZE / this.m_speedFactor;
 		switch(this.m_direction) {
 			case LEFT:
 				this.m_board.move(this.m_location.x - moveAmount,
 									this.m_location.y, this);
 				break;
 			case RIGHT:
 				this.m_board.move(this.m_location.x + moveAmount,
 									this.m_location.y, this);
 				break;
 			case DOWN:
				boolean vertChangeBiggerThanSquare = 
							((this.m_vertAmount + moveAmount) > SQUARE_SIZE);
				moveAmount = (vertChangeBiggerThanSquare) ?
									SQUARE_SIZE-this.m_vertAmount : moveAmount;
				this.m_vertAmount = (vertChangeBiggerThanSquare) ?
									0 : this.m_vertAmount + moveAmount;
 				this.m_board.move(this.m_location.x,
 									this.m_location.y + moveAmount, this);
 				break;
 			case UP:
				vertChangeBiggerThanSquare = 
							((this.m_vertAmount + moveAmount) > SQUARE_SIZE);
				moveAmount = (vertChangeBiggerThanSquare) ?
									SQUARE_SIZE-this.m_vertAmount : moveAmount;
				this.m_vertAmount = (vertChangeBiggerThanSquare) ?
									0 : this.m_vertAmount + moveAmount;
 				this.m_board.move(this.m_location.x,
 									this.m_location.y - moveAmount, this);
 				break;
 		}
 		// need to change direction after moving down a box size
 		
 	}
 
 	public void collidesWith(Entity entity) {
 		
 		if( entity.getType() == EntityTypes.BULLET )
 		{
 			die();
 		}
 		else if ( entity.getType() == EntityTypes.MUSHROOM ) 
 		{
 			if(this.m_direction == Direction.DOWN) {
 				if (this.m_movingLeftward) {
 					this.m_direction = Direction.RIGHT;
 				} else {
 					this.m_direction = Direction.LEFT;
 				}
 			} else {
 				this.m_direction = Direction.DOWN;
 			}
 		}
 		
 	}
 
 	/**
 	 * Kill this segment. If there is a next segment, it becomes a new head.
 	 * If the segment killed is not a head, there will now be two Centipedes.
 	 */
 	public void die() {
 		if (this.m_nextSegment != null) {
 			
 		}
 		// Problem: how does the previous segment know it has lost its next?
 		// Will we use a doubly linked list?
 	}
 	
 	public EntitySprite getSprite() {
 		// Sprites are immutable
 		return this.m_sprite;
 	}
 
 	public int[] getLocation() {
 		return new int[]{this.m_boundingBox.getX(), this.m_boundingBox.getY()};
 	}
 
 	public Rectangle getBoundingBox() {
 		// Rectangles are mutable
 		return (this.m_boundingBox);
 	}
 	
 	public EntityTypes getType() {
 		return EntityTypes.CENTIPEDE;
 	}
 	
 	public Centipede makeCentipede(	int length,
 									Board board,
 									Point headLocation,
 									Direction initialDirection	) {
 		Centipede head = null;
 		Centipede curr = null;
 		for (int i=length-1; i>1; --i) {	// Build from tail up
 			// assuming we are building head at left, tail to right
 			Point segmentLocation = new Point(headLocation.x + SQUARE_SIZE * i,
 												headLocation.y);
 			curr = new Centipede(false, board, segmentLocation,
 									initialDirection, curr);
 			curr.m_sprite = new CentipedeSprite(curr);
 			curr.recalcBoundingBox();
 		}
 		head = new Centipede(true, board, headLocation, initialDirection, curr);
 		head.m_sprite = new CentipedeSprite(head);
 		head.recalcBoundingBox();
 		return head;
 	}
 	
 	private void recalcBoundingBox() {
 		int x = m_location.x - SQUARE_SIZE / 2;
 		int y = m_location.y - SQUARE_SIZE / 2;
 		m_boundingBox =  Rectangle.fromUpperLeft( x, y, SQUARE_SIZE, SQUARE_SIZE );
 	}
 
	@Override
	public void updateLocation(int x, int y) {
		this.m_location.x = x;
		this.m_location.y = y;
	}

 }
