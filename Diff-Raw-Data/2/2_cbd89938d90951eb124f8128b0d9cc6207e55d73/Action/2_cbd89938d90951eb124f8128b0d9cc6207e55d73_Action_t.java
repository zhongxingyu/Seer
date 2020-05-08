 /**
  * @file Action.java
  * 
  * Actions for the player to use
  * 
  * @author Grant Hays
  * @date 10/19/11
  * @version 2
  */
 
 import java.net.UnknownHostException;
 
 
 /**
  * @class Action
  * 
  * This class holds basic actions for the player to perform, such as ball searching and
  * intercepting, dashing to points, finding the ball and points and getting their coordinates.
  *
  */
 public class Action {
 	
 	/**
 	 * Default constructor
 	 */
 	public Action() {
 		
 	}
 	
 	/**
 	 * Constructor with parameters
 	 * 
 	 * @param mem The Memory containing all the parsed information from the server
 	 * @param rc The RoboClient that is the player's connection to the server
 	 * 
 	 * @pre Both a full memory and initialized RoboClient must be passed in to avoid any
 	 * errors
 	 * @post A new set of actions will be available for the player to call on
 	 */
 	public Action(Memory mem, RoboClient rc) {
 		this.mem = mem;
 		this.rc = rc;
 	}
 	
 	/**
 	 * This sets the Memory for the action to use. This is important as the 
 	 * Memory is constantly changing, and must be updated at every step.
 	 * @param mem The player's Memory
 	 * @pre The Memory should be the most up to date
 	 * @post The actions that require a Memory will be able to pull from it
 	 */
 	public void setMem(Memory mem) {
 		this.mem = mem;
 	}
 	
 	/**
 	 * This tells the player to turn and run to a point
 	 * 
 	 * @param go The Polar coordinates of the final position, with
 	 * the player's position as an origin
 	 * 
 	 * @pre The player must have a valid position on the field passed in
 	 * @post If the player is not facing the direction of the final position, s/he will
 	 * first turn toward it. If the player is approximately facing the position, s/he
 	 * will dash toward the direction of the position.
 	 */
 	public void gotoPoint(Polar go) {
 		
 		try {
 			if(go.t > 5.0 || go.t < -5.0) {
 				rc.turn(go.t * (1+(5*mem.getAmountOfSpeed())));
 				Thread.sleep(100);
 			}
 			
 			
			rc.dash(m.getPower(m.getPos(go), mem.getAmountOfSpeed(), mem.getDirectionOfSpeed(), mem.getEffort()));
 			Thread.sleep(100);
 			
 		} catch (UnknownHostException e) {
 			e.printStackTrace();
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		
 	}
 	
 	/**
 	 * A cartesian wrapper for the gotoPoint with Polar coordinate
 	 * 
 	 * @param p The Cartesian Pos of position to go to
 	 * 
 	 * @pre The player must have a valid position on the field passed in
 	 * 
 	 * @post First, the Pos will be converted to a Polar coordinateIf the player is 
 	 * not facing the direction of the final position, s/he will turn toward it. 
 	 * If the player is approximately facing the position, s/he will dash toward the 
 	 * direction of the position.
 	 */
 	public void gotoPoint(Pos p) {
 		gotoPoint(m.getPolar(p));
 	}
 	
 	/**
 	 * A method to get the turn moment, taking into account inertia of player, derived from the 
 	 * actual_angle equation in the RoboCup server manual's  section on Turn: 4.5.6
 	 * @param d
 	 * @param v
 	 * @return The optimal turn moment
 	 */
 	public double getTurn(double d, double v) {
 		return(d * (1 + 5*v));
 	}
 	
 	/**
 	 * A method to find the ball on the field. If it's not in view, the player turns
 	 * until he finds it. If the ball is too far, he dashes to get to it. If the ball
 	 * is within 20 distance, he intercepts the ball.
 	 * 
 	 * @throws UnknownHostException
 	 * @throws InterruptedException
 	 */
 	public void findBall() throws UnknownHostException, InterruptedException {
 		if(mem.isObjVisible("ball")) {
 			ObjBall ball = mem.getBall();
 			
 			if((ball.getDirChng() == 0)) {
 				gotoPoint(new Polar(ball.getDistance(), ball.getDirection()));
 			}
 			if(ball.getDirection() > 5.0 || ball.getDirection() < -5.0) {
 				rc.turn(ball.getDirection() * (1 + 5*mem.getAmountOfSpeed()));
 				Thread.sleep(100);
 			}
 			
 			if(ball.getDistance() < 20) {
 				interceptBall(ball);
 			}
 			else if (ball.getDistance() > 20) {
 				rc.turn(ball.getDirection() * (1 + 5*mem.getAmountOfSpeed()));
 				Thread.sleep(100);
 			}
 		}
 		else
 			System.out.println("No ball.");
 			rc.turn(45);
 			Thread.sleep(100);
 	}
 		
 	
 	/**
 	 * This method goes to the position that the ball will be in at time t+1 and kicks
 	 * it if it is withen 0.5 distance.
 	 * @param ball
 	 * @throws UnknownHostException
 	 * @throws InterruptedException
 	 * 
 	 * @pre A ball must be present and passed
 	 * @post The player (should) go to the point where the ball is and kick it
 	 */
 	private void interceptBall(ObjBall ball) throws UnknownHostException, InterruptedException {
 		
 		gotoPoint(m.getNextBallPoint(ball));
 		if(ball.getDistance() < 0.5) {
 			rc.kick(50, 0);
 			Thread.sleep(100);
 		}
 		
 	}
 
 
 
 	public MathHelp m = new MathHelp();
 	public Memory mem;
 	public RoboClient rc;
 	public Polar OppGoal;
 }
