 import java.net.DatagramSocket;
 import java.net.SocketException;
 import java.net.UnknownHostException;
 
 /** @file Goalie.java
 * Class file for Goalie class
 * @author Joel Tanzi
 * @date 11 October 2011
 * @version 1.0 
 */
 
 /** @class Goalie
 * The Goalie class inherits from the Player class.  The Goalie is a specialized
 * type of Player that may catch the ball under certain conditions and defends the goal
 * from the opposing team. 
 */
 public class Goalie extends Player {
 
 	/**
 	 * 
 	 */
 	public Goalie() {
 		super();
 		// TODO Auto-generated constructor stub
 	}
 
 	 /**
 	 * Initializes the Player with the RoboCup server as a goalie.
 	 * @pre A RoboCup server is available.
 	 * @post The Player has been initialized to the correct team as a goalie.
 	 */
 	public void initGoalie() throws SocketException, UnknownHostException {
 
 		rc.dsock = new DatagramSocket();
 		rc.initGoalie(getParser(), getMem());		
 	}
 
 	 /**
 	 * Causes the Goalie to catch the ball.
 	 * @pre Playmode is play-on, ball is within goalkeeper zone and in the catchable area.
 	 * @post The Goalie has caught the ball.
 	 */
 	public void catchball(double d) throws UnknownHostException{
 		rc.catchball(d);
 	}
 
 
 	/**
 	 * Gets absolute position of Goalie on field
 	 * @post Pos with x and y coordinates of Goalie on field
 	 * @return Pos coordinates of position
 	 */
 	public Pos getPosition() {
 		
 		ObjFlag flag = getMem().getClosestPenaltyFlag();
 		ObjGoal goal = getMem().getOwnGoal();
 		
 		
 		if(flag != null) {
 			
 			Pos flagCoord = getMem().getFlagPos(flag.getFlagName());
 			Pos toFlag = mh.getPos(flag.getDistance(), getMem().getDirectionOfSpeed() + flag.getDirection());
 			Pos self = mh.vSub(flagCoord, toFlag);
 			/*
 			System.out.println("****************************************");
 			System.out.println("Penalty Flag (" + flag.getFlagName() + "): (" + flagCoord.x + ", " + flagCoord.y + ")");
 			System.out.println("Flag Polar: (" + flag.getDistance() + ", " + flag.getDirection() + ")");
 			System.out.println("DirectionOfSpeed: " + getMem().getDirectionOfSpeed());
 			System.out.println("My Position: (" + self.x + ", " + self.y + ")");
 			*/
 			
 			return(self);
 			
 		}
 		else if(goal != null) {
 			
 			Pos goalCoord = getMem().getOwnGoalPos();
 			Pos toGoal = mh.getPos(goal.getDistance(), getMem().getDirectionOfSpeed() + goal.getDirection());
 			Pos self = mh.vSub(goalCoord, toGoal);
 			/*
 			System.out.println("****************************************");
 			System.out.println("Goal (g" + goal.getSide() + "): (" + goalCoord.x + ", " + goalCoord.y + ")");
 			System.out.println("Goal Polar: (" + goal.getDistance() + ", " + goal.getDirection() + ")");
 			System.out.println("DirectionOfSpeed: " + getMem().getDirectionOfSpeed());
 			System.out.println("My Position: (" + self.x + ", " + self.y + ")");
 			*/
 
 			return(self);
 		}
 		else {
 			flag = getMem().getClosestBoundary();
 			
 			Pos flagCoord = getMem().getFlagPos(flag.getFlagName());
 			Pos toFlag = mh.getPos(flag.getDistance(), getMem().getDirectionOfSpeed() + flag.getDirection());
 			Pos self = mh.vSub(flagCoord, toFlag);
 			/*
 			System.out.println("****************************************");
 			System.out.println("Boundary Flag (" + flag.getFlagName() + "): (" + flagCoord.x + ", " + flagCoord.y + ")");
 			System.out.println("Flag Polar: (" + flag.getDistance() + ", " + flag.getDirection() + ")");
 			System.out.println("DirectionOfSpeed: " + getMem().getDirectionOfSpeed());
 			System.out.println("My Position: (" + self.x + ", " + self.y + ")");
 			*/
 			return(self);
 			
 		}
 		
 	}
 	
 	/**
 	 * Turns goalie toward ball
 	 * 
 	 * @post The goalie will turn in the direction of the ball
 	 */
 	public void followBall() {
 
 
 		try {
 			if(!getMem().isObjVisible("ball")) {
 				getRoboClient().turn(45);
 			}
 			if(getMem().isObjVisible("ball")) {
 				ObjBall ball = getMem().getBall();
 
 				if((ball.getDirection() > 5.0) || (ball.getDirection() < -5.0)) {
 					getRoboClient().turn(ball.getDirection() * (1 + (5 * getMem().getAmountOfSpeed())));
 				}
 
 				if(ballInGoalzone(ball)) {
 
 					defendGoal(ball);
 				}
 			}
 
 		} catch (UnknownHostException e) {
 			System.out.println("Error in Goalie.followBall()");
 			e.printStackTrace();
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	/**
 	 * A method to determine whether the ball is in the penalty box
 	 * 
 	 * @param ball the ObjBall to follow
 	 * @pre this must be called with an ObjBall
 	 * @post true if ball is in penalty box, false if it's not
 	 * @return boolean
 	 */
 	public boolean ballInGoalzone(ObjBall ball) {
 		
 		if(ball == null)
 			return false;
 		
 		
 		Pos ballPos = mh.getPos(ball.getDistance(), getMem().getDirectionOfSpeed());
 		ballPos = mh.vAdd(getPosition(), ballPos);
 		/*
 		System.out.println("Ball polar: (" + ball.getDistance() + ", " + ball.getDirection() + ")");
 		System.out.println("Ball position: (" + ballPos.x + ", " + ballPos.y + ")");
 		System.out.println("****************************************");
 		System.out.println("");
 		System.out.println("");
 		*/
 		
 		if((ballPos.x <= -36) && ((-20.16 <= ballPos.y) && (ballPos.y <= 20.16)))
 			return true;
 		else
 			return false;
 		
 			
 	}
 	
 	/**
 	 * Returns true or false depending on whether the ball is within the catchable range
 	 * of the goalie.
 	 * @pre The ball is visible to the goalie
 	 * @post The ball is determined to catchable or not.
 	 * @return boolean True if catchable, false if not.
 	 */
 	public boolean catchable() {
 		
 		boolean catchable = false;
 		
 		//Test for visibility
 		if (getMem().isObjVisible("ball")) {
 			
 			//Test for moment range
 			if (getMem().getBall().getDirection()> -180 && getMem().getBall().getDirection() < 180) {
 				
 				//Test for catchable distance
 				if (getMem().getBall().getDistance() < 2.0) {
 					catchable = true;
 				}
 			}
 		}
 		
 		return catchable;
 	}
 	
 
 
 	/**
 	 * Causes the goalie to act to intercept the ball as it approaches the goal.
 	 * @param ObjBall representing the ball in play.
 	 * @throws UnknownHostException 
 	 * @throws InterruptedException 
 	 * @pre The ball has entered the goal zone.
 	 * @post The ball has been caught by the goalie, or the goalie has missed the ball.
 	 */
 	public void defendGoal(ObjBall ball) throws UnknownHostException, InterruptedException {				
 
 		//Move to catchable range of ball
 		getAction().gotoPoint(mh.getNextBallPoint(ball));
 
 		//If ball is in catchable area, catch it
 		if (catchable()){
 			catchball(getMem().getBall().getDirection());
 			Thread.sleep(100);
 			kickBallOutOfBounds();
 		}

 	}
 
 	/**
 	 * Returns the closest player to the goalie as an ObjPlayer object from the memory
 	 * of the current worldstate.
 	 * @pre Players are in sight of the goalie.
 	 * @post The closest player to the goalie has been determined.
 	 * @return ObjPlayer
 	 */
 	public ObjPlayer closestPlayer() {
 		ObjPlayer closestPlayer = new ObjPlayer();
 		double distance = 0;
 
 		//Loop through arraylist of ObjPlayers
 		for (int i = 0; i < getMem().getPlayers().size(); ++i) {
 
 			if (!getMem().getPlayers().isEmpty()) {  
 				if (distance == 0) {
 					distance = getMem().getPlayers().get(i).getDistance();
 				}
 				else {
 
 					//Test if this player is closer than the previous one
 					if (distance > getMem().getPlayers().get(i).getDistance()) {
 						distance = getMem().getPlayers().get(i).getDistance();
 						closestPlayer = getMem().getPlayers().get(i);
 					}
 				}
 			}
 		}		
 		return closestPlayer;
 	}
 
 	public void kickBallOutOfBounds() {
 		try {
 			//System.out.println("Flag name: " + getMem().getClosestBoundary().getFlagName());
			if (!(getMem().getClosestBoundary().getFlagName() == "flb10")) {
				kick(100,getMem().getClosestBoundary().getDirection());
 			}else {
 				turn(-110);
 				Thread.sleep(100);
 				dash(50);
 				Thread.sleep(100);
 				kick(100,getMem().getClosestBoundary().getDirection());
 			}
 		} catch (UnknownHostException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	public boolean ballTurn = false;
 	public MathHelp mh = new MathHelp();
 
 
 } //end class
 
