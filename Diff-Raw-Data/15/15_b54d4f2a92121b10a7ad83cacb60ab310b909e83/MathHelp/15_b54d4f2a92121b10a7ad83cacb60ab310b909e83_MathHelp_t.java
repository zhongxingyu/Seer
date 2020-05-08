 /**
  * @file MathHelp.java
  * 
  * This has functions of the math I need for calculations.
  * 
  * @author granthays
  * @date 10/09/11
  * @version 1
  *
  */
 
 
 
 public class MathHelp {
 
 	/**
 	 * Polar to Cartesian converter
 	 * 
 	 * @param r the length of the Polar arm
 	 * @param t the angle, in degrees, of the arm from the x-axis
 	 * @return A new Cartesian Pos converted from the r and t of a Polar vector
 	 */
 	public Pos getPos(double r, double t) {
 		
 		double x = r * Math.cos(Math.toRadians(t));
 		double y = r * Math.sin(Math.toRadians(t));
 		
 		return(new Pos(x, y));
 		
 	}
 	
 	/**
 	 * Polar to Cartesian wrapper
 	 * 
 	 * This allows you to pass a whole polar in, instead of extracting it's r
 	 * and t variables and passing them in
 	 * 
 	 * @param p The polar coordinates you want to convert
 	 * @return A new Pos with the Cartesian version of your Polar vector
 	 */
 	public Pos getPos(Polar p) {
 		return(getPos(p.r, p.t));
 	}
 	
 	/**
 	 * Cartesian to polar converter
 	 * 
 	 * @param x the x coordinate of the Cartesian vector
 	 * @param y the y coordinate of the Cartesian vector
 	 * @return A new Polar vector converted from the Cartesian vector
 	 */
 	public Polar getPolar(double x, double y) {
 		
 		double r = Math.sqrt(x*x + y*y);
 		double t = Math.toDegrees(Math.atan(y/x));
 		
 		return(new Polar(r, t));
 	}
 	
 	/**
 	 * Cartesian to polar wrapper
 	 * 
 	 * This is just a wrapper, so you can pass in a Pos
 	 * instead of extracting it's x and y and passing them in.
 	 * 
 	 * @param p the Cartesian vector
 	 * @return A new Polar vector converted from the Cartesian vector
 	 */
 	public Polar getPolar(Pos p) {
 		return(getPolar(p.x, p.y));
 	}
 	
 	/**
 	 * Vector Addition
 	 * 
 	 * @param p1 first position
 	 * @param p2 second position
 	 * @return New position with the sum of the two arguments
 	 */
 	public Pos vAdd(Pos p1, Pos p2) {
 		return(new Pos((p1.x + p2.x), (p1.y + p2.y)));
 	}
 	
 	
 	/**
 	 * Vector Subtraction
 	 * 
 	 * @param p2 final position
 	 * @param p1 initial position
 	 * @return new Pos with the difference between p2 and p1
 	 */
 	public Pos vSub(Pos p2, Pos p1) {
 		return(new Pos((p2.x - p1.x), (p2.y - p1.y)));
 	}
 	
 	/**
 	 * Multiply vector by scalar
 	 * @param p the vector
 	 * @param n the scalar
 	 * @return A Pos vector multiplied by a scalar value
 	 */
 	public Pos vMul(Pos p, double n) {
 		return(new Pos((p.x*n), (p.y*n)));
 	}
 	
 	/**
 	 * Divide vector by scalar
 	 * @param p the vector
 	 * @param n the scalar
 	 * @return A Pos vector divided by a scalar value
 	 */
 	public Pos vDiv(Pos p, double n) {
 		return(new Pos((p.x/n), (p.y/n)));
 	}
 	
 	/**
 	 * Magnitude
 	 * Calculates the Magnitude of a vector, same as r in a Polar vector
 	 * @param p the Pos of the vector
 	 * @return A double containing the magnitude of the vector
 	 */
 	public double mag(Pos p) {
 		return(Math.sqrt(p.x*p.x + p.y*p.y));
 	}
 
 	/**
 	 * A normalizer
 	 * @param p the vector to find the normal of
 	 * @return a Pos of the unit vector of p
 	 */
 	public Pos norm(Pos p) {
 		return(norm(mag(p), p));
 	}
 	
 
 	/**
 	 * A normalizer
 	 * @param dist the magnitude of the vector
 	 * @param a the vector to be normalized
 	 * @return a Pos of the unit vector of p
 	 */
 	public Pos norm(double dist, Pos a) {
 		return(new Pos((a.x/dist),(a.y/dist)));
 	}
 	
 	/**
 	 * The Effective Dash Power
 	 * 
 	 * @param effort From the stamina in the SenseMemory
 	 * @param power The Power of the dash
 	 * @return the product of effort x power x dash_power_rate (0.006)
 	 */
 	public double edp(double effort, double stamina) {
 		return(Math.min(100, stamina) * effort * .006 * 100);
 	}
 	
 	/**
 	 * A calculator for power needed to get to a position on the field. This is derived from
 	 * the Movement Model equations in the Server Manual: section 4.4
 	 * 
 	 * @param p the position to go to
 	 * @param vel_r the magnitude of the player's velocity
 	 * @param vel_t the direction of the player's velocity
 	 * @return The power needed to accelerate the player to the desired location
 	 */
 	public double getDashPower(Pos p, double vel_r, double vel_t, double effort, double stamina) {
 		
 		if(mag(p) > 20)
 			return(50);
 		else {
 			Pos v = getPos(vel_r, vel_t);
 			Pos a = vSub(p, v);
 			double power = mag(a) / (0.06 * effort);
 			return(Math.min(edp(effort, stamina), power));
 		}
 	}
 	
 	
 	
 	/**
 	 * A method to find the ball's next point given it's velocity and position relative
 	 * to player.
 	 * 
 	 * @param ball
 	 * @return A Polar coordinate with the theoretical position of the ball at time t+1
 	 */
 	public Polar getNextBallPoint(ObjBall ball) {
 		
 		Pos pb = getPos(new Polar(ball.getDistance(), ball.getDirection()));
 		Pos pb2 = getPos(new Polar(ball.getDistChng(), ball.getDirChng()));
 		
 		return(getPolar(vAdd(pb, pb2)));
 		
 	}
 	
 	
 	/**
 	 * A method to find an opponent's next point given his velocity and position relative
 	 * to the player. 
 	 * @param opponent An ObjPlayer object representing the opponent to track
 	 * @return A Polar coordinate with the predicted position of the opponent at time t+1
 	 */
 	public Polar getNextPlayerPoint(ObjPlayer player) {
 		Pos po = getPos(new Polar(player.getDistance(), player.getDirection()));
 		Pos po2 = getPos(new Polar(player.getDistChng(), player.getDirChng()));
 		
 		return (getPolar(vAdd(po,po2)));
 	}
 	
 	/**
 	 * Calculates the power needed to kick the ball to a specified place on the field, using
 	 * the equation from the manual
 	 * 
 	 * @param p A polar coordinate to kick the ball to
 	 * @param vel_r The magnitude of the player's velocity
 	 * @param vel_t the direction of the player's velocity
 	 * @param ball_r the distance of the ball to the player
 	 * @param ball_t the direction of the ball to the player
 	 * 
 	 * @return power of kick
 	 */
 	public double getKickPower(Polar p, double vel_r, double vel_t, double ball_r, double ball_t) {
 		double r = 1/0.94;
 		double n = (Math.log(((p.r*(r-1))/0.05)+1)/Math.log(r));
		//System.out.println("n: " + n);
 		double s = 0.05/Math.pow(0.94, n);
		//System.out.println("s: " + s);
 		double ep = s/(0.035 * (1 - ( 0.25 * ((Math.abs(ball_t-vel_t)/180) + (ball_r/0.7)))));
		//System.out.println("ep: " + ep);
 		return(Math.min(ep, 100));
 	}
 	
 	/**
 	 * A wrapper of the getKickPower with a Pos instead of Polar
 	 * 
 	 * @param p A polar coordinate to kick the ball to
 	 * @param vel_r The magnitude of the player's velocity
 	 * @param vel_t the direction of the player's velocity
 	 * @param ball_r the distance of the ball to the player
 	 * @param ball_t the direction of the ball to the player
 	 * 
 	 * @return power of kick
 	 */
 	public double getKickPower(Pos p, double vel_r, double vel_t, double ball_r, double ball_t) {
 		return(getKickPower(getPolar(p), vel_r, vel_t, ball_r, ball_t));
 	}
 	
 }
