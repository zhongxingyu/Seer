 package se.chalmers.kangaroo.model.creatures;
 
 import java.awt.Polygon;
 
 import se.chalmers.kangaroo.model.utils.Direction;
 import se.chalmers.kangaroo.model.utils.Position;
 import se.chalmers.kangaroo.utils.Waiter;
 
 /**
  * The creature is a sumo wrestler that can cause a radius of three tiles ground
  * to be dangerous for the Kangaroo to be in when he is stamping on the ground.
  * He is not killable and the only way to get further is to flee when he is
  * jumping.
  * 
  * @author pavlov
  * 
  */
 public class SumoCreature implements Creature {
 	private final static int id = 115;
 	private Position pos;
 	private boolean isStomping = false;
 	private boolean isJumping = false;
 	private boolean isShaking = false;
 	private double verticalSpeed;
 	private Direction direction;
 
 
 	public SumoCreature(Position pos) {
 
 		this.pos = pos;
 		direction = Direction.DIRECTION_WEST;
 	}
 
 	@Override
 	public boolean isKillable() {
 		return false;
 	}
 
 	/**
 	 * If he is ground stomping, the polygon expands with three tiles radius
 	 * where Kangaroo is vulnerable.
 	 */
 	@Override
 	public Polygon getPolygon() {
 		if (isShaking == false) {
 			int polyX[] = { pos.getX() + 58, pos.getX() + 58, pos.getX() + 64,
 					pos.getX() + 64, pos.getX() + 0, pos.getX() + 0,
 					pos.getX() + 20, pos.getX() + 20 };
 			int polyY[] = { pos.getY() + 0, pos.getY() + 22, pos.getY() + 22,
 					pos.getY() + 64, pos.getY() + 64, pos.getY() + 22,
 					pos.getY() + 22, pos.getY() + 0 };
 			return new Polygon(polyX, polyY, 8);
 		} else {
 			int polyX[] = { pos.getX() + 58, pos.getX() + 58, pos.getX() + 64,
 					pos.getX() + 64, pos.getX() + 256, pos.getX() + 256,
 					pos.getX() - 96, pos.getX() - 96, pos.getX() + 0,
 					pos.getX() + 20, pos.getX() + 20 };
 			int polyY[] = { pos.getY() + 0, pos.getY() + 22, pos.getY() + 22,
 					pos.getY() + 63, pos.getY() + 63, pos.getY() + 64,
 					pos.getY() + 64, pos.getY() + 63, pos.getY() + 22,
 					pos.getY() + 22, pos.getY() + 0 };
 			return new Polygon(polyX, polyY, 8);
 		}
 	}
 
 	/**
 	 * The sumo is stamping the ground and making three tiles in radius being
 	 * vulnerable.
 	 */
 	private void groundStomp() {
 		isStomping = true;
 		new Thread() {
 			@Override
 			public void run() {
 				try {
 					sleep(2460);
 					isShaking = true;
 					isStomping = false;
 					sleep(2000);
 					isShaking = false;
 				} catch (InterruptedException ie) {
 
 				}
 			}
 		}.start();
 		changeDirection();
 	}
 	
 	public boolean isStomping(){
 		return isStomping;
 	}
 	
 	public boolean isJumping(){
 		return isJumping;
 	}
 
 	/**
 	 * Sumo is jumping so the Kangaroo has the chance to proceed further.
 	 */
 	private void jump() {
 		isJumping = true;
 		new Thread() {
 			@Override
 			public void start() {
 				try {
 					sleep(20);
 					verticalSpeed = -3;
					while (verticalSpeed != 3) {
 						pos = new Position(pos.getX(),
 								(int) (pos.getY() + verticalSpeed));
 						verticalSpeed += 0.01;
 					}
 				} catch (InterruptedException ie) {
 
 				}
 			}
 		}.start();
 
 	}
 
 	@Override
 	public void updateCreature() {
 		if ((Math.random() * 600) <= 599 && isJumping == false
 				&& isStomping == false) {
 			jump();
 		} else if (Math.random() * 600 <= 541 && isJumping == false
 				&& isStomping == false) {
 			groundStomp();
 		}
 	}
 
 	@Override
 	public void move() {
 	}
 
 	@Override
 	public int getId() {
 		return id;
 	}
 
 	@Override
 	public Position getPosition() {
 		return pos;
 	}
 
 	public Direction getDirection() {
 		return this.direction;
 	}
 
 	@Override
 	public void changeDirection() {
 		if(direction == Direction.DIRECTION_WEST){
 			direction = Direction.DIRECTION_EAST;
 		}else{
 			direction = Direction.DIRECTION_WEST;
 		}
 	}
 
 }
