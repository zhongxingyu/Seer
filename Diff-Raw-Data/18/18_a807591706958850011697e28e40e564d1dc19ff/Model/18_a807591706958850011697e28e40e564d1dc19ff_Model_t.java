 package bouncingballs;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.badlogic.gdx.math.Vector2;
 
 public class Model {
 
 	private List<Ball> balls;
 
 	/** Constructs a model with initial 3 balls. */
 	public Model() {
 		balls = new ArrayList<Ball>();
 		
 		//add a big, heavy ball with initial 0 velocity
 		Ball ball = new Ball(0, 0, 100, 70);
 		balls.add(ball);
 		
 		//add a smaller, lighter ball with a high initial velocity
 		ball = new Ball(250, 120, 50, 20);
 		ball.setVelocity(new Vector2(-500, 10));
 		balls.add(ball);
 		
 		//add a big, heavy ball with a high initial velocity
 		ball = new Ball(-250, 120, 100, 70);
 		ball.setVelocity(new Vector2(-500, 10));
 		balls.add(ball);
 		
 		// add a smaller, lighter ball with a medium velocity
 		ball = new Ball(-200, -200, 50, 20);
 		ball.setVelocity(new Vector2(-200, 10));
 		balls.add(ball);
 
 		// add a smaller, lighter ball with a medium velocity
 		ball = new Ball(-300, 0, 25, 100);
 		ball.setVelocity(new Vector2(50, 200));
 		balls.add(ball);
 
 		// add a smaller, lighter ball with a medium velocity
 		ball = new Ball(-130, 120, 10, 5);
 		ball.setVelocity(new Vector2(30, 170));
 		balls.add(ball);
 	}
 
 	/** Game loop */
 	public void update(float delta) {
 		// Update all balls positions, according to gravity and velocity
 		for (Ball ball : balls) {
 			ball.getVelocity().y -= Constants.GRAVITY * delta;
 			ball.getPosition().x += ball.getVelocity().x * delta;
 			ball.getPosition().y += ball.getVelocity().y * delta;
 		}
 
 		// Solve all collisions between balls
 		for (int i = 0; i < balls.size(); ++i) {
 			Ball ball1 = balls.get(i);
 			// Check if the current ball collides with any other balls
 			for (int j = i + 1; j < balls.size(); ++j) {
 				Ball ball2 = balls.get(j);
 				if (ball1.collidesWith(ball2)) {
 					// http://www.cse.chalmers.se/edu/year/2010/course/DAT026/CourseMaterial/lecture5.txt
 					/*
 					 * 1) calculate the degree of the plane
 					 * 2) convert the balls velocity to the new plane
 					 * 3) handle collision (only x-velocity)
 					 * 4) convert the new velocity back to the normal plane system
 					 * 5) update balls position and velocity
 					 */
 
 					// 1) calculate the degree of the plane
 					Vector2 position1 = ball1.getPosition();
 					Vector2 position2 = ball2.getPosition();
 					// rotation = how much the new x axis is rotated from the normal x axis
 					double rotation = Math.atan((position1.y - position2.y) / (position1.x - position2.x));
 					if (position2.x > position1.x) {
 						rotation = Math.PI + rotation;
 					}
 					
 					// 2) convert the balls velocity to the new plane
 					/* 
 					 * alfa = 90 - rotation		(the x-vectors angle towards the new plane)
 					 * beta = -rotation			(the y-vectors angle towards the new plane)
 					 * newX = oldY*cos(alfa)+oldX*cos(beta)		(the new planes x-vector)
 					 * newY = oldY*sin(alfa)+oldX*sin(beta)		(the new planes y-vector)
 					 */
 
 					Vector2 oldVelocity1 = ball1.getVelocity();
 					Vector2 oldVelocity2 = ball2.getVelocity();
 					Vector2 newVelocity1 = getVectorNewPlane(oldVelocity1, rotation);
 					Vector2 newVelocity2 = getVectorNewPlane(oldVelocity2, rotation);
 
 					float weight1 = ball1.getWeight();
 					float weight2 = ball2.getWeight();
 
 
 					// 3) handle collision (only x-velocity)
 					handleCollision(newVelocity1, newVelocity2, weight1, weight2);
 
 					// 4) convert the new velocity back to the normal plane system
 					Vector2 tmp = getVectorNewPlane(newVelocity1, -rotation);
 					oldVelocity1.x = tmp.x;
 					oldVelocity1.y = tmp.y;
 					tmp = getVectorNewPlane(newVelocity2, -rotation);
 					oldVelocity2.x = tmp.x;
 					oldVelocity2.y = tmp.y;
 
 					// 5) update balls position
 					float collision = (ball1.getRadius() + ball2.getRadius() - ball1.getPosition().dst(ball2.getPosition()) + 0.0001f) / 2;
 					ball1.getPosition().add(collision * (float)Math.cos(rotation), collision * (float)Math.sin(rotation));
 					ball2.getPosition().add(-collision * (float)Math.cos(rotation), -collision * (float)Math.sin(rotation));
 					
 					// optional: add some energy loss to the balls
 					reduceVelocity(ball1);
 					reduceVelocity(ball2);
 				}
 			}
 		}
 		// Make sure no ball is leaving the field
 		for (Ball ball : balls) {
 			float radius = ball.getRadius();
 			// Right border
 			if (ball.getPosition().x + radius > Constants.WIDTH / 2) {
 				ball.getPosition().x = Constants.WIDTH / 2 - radius;
 				ball.getVelocity().x = -ball.getVelocity().x;
 				reduceVelocity(ball);
 			}
 			// Left border
 			if (ball.getPosition().x - radius < -Constants.WIDTH / 2) {
 				ball.getPosition().x = -Constants.WIDTH / 2 + radius;
 				ball.getVelocity().x = -ball.getVelocity().x;
 				reduceVelocity(ball);
 			}
 			// Top border
 			if (ball.getPosition().y + radius > Constants.HEIGHT / 2) {
 				ball.getPosition().y = Constants.HEIGHT / 2 - radius;
 				ball.getVelocity().y = -ball.getVelocity().y;
 				reduceVelocity(ball);
 			}
 			// Bottom border
 			if (ball.getPosition().y - radius < -Constants.HEIGHT / 2) {
 				ball.getPosition().y = -Constants.HEIGHT / 2 + radius;
 				ball.getVelocity().y = -ball.getVelocity().y;
 				reduceVelocity(ball);
 			}
 		}
 	}
 	
 	/**
 	 * Reduces the velocity of a <code>Ball</code> with a certain percentage.
 	 * This method can be called to simulate energy loss at collisions.
 	 * @param ball The ball to reduce the velocity of.
 	 */
 	public void reduceVelocity(Ball ball) {
 		ball.getVelocity().mul(0.98f);
 	}
 
 	/**
 	 * Converts a velocity vector to a new plane, which is rotated <code>rotation</code>
 	 * radians from the velocity vectors initial plane.
 	 * @param velocity The vector to convert to the new plane.
 	 * @param rotation The new planes relative rotation to the former plane.
 	 * @return A velocity vector that represents the given parameter velocity in
 	 * a rotated plane.
 	 */
 	public Vector2 getVectorNewPlane(Vector2 velocity, double rotation) {
 		//alfa = the x-vectors angle towards the new plane
 		//beta = the y-vectors angle towards the new plane
 		double alfa = Math.PI/2 - rotation;
 		double beta = - rotation;
 		
 		//the x- and y-vectors for the new plane
 		Vector2 newVelocity = new Vector2();
 		newVelocity.x = (float) (velocity.y * Math.cos(alfa) + velocity.x * Math.cos(beta));
 		newVelocity.y = (float) (velocity.y * Math.sin(alfa) + velocity.x * Math.sin(beta));
 		return newVelocity;
 	}
 
 
 	public void handleCollision(Vector2 v1, Vector2 v2, float weight1, float weight2) {
 		/*
 		 * v1 = ( (m1 - m2)*u1 + 2*m2*u2 ) / (m1 + m2)
 		 * v2 = ( (m2 - m1)*u2 + 2*m1*u1 ) / (m1 + m2)
 		 */
 		float momentum = v1.x*weight1 + v2.x*weight2;
 		float kinetic = v1.x*v1.x*weight1/2 + v2.x*v2.x*weight2/2;
 		Vector2 tmp = new Vector2();
 		tmp.x = ((weight1 - weight2)*v1.x + 2*weight2*v2.x) / (weight1 + weight2);
 		v2.x = ((weight2 - weight1)*v2.x + 2*weight1*v1.x) / (weight1 + weight2);
 		v1.x = tmp.x;
 		System.out.println("Momentum: " + momentum + " vs " + (v1.x*weight1 + v2.x*weight2));
 		System.out.println("Kinetic: " + kinetic + " vs " + (v1.x*v1.x*weight1/2 + v2.x*v2.x*weight2/2));
 	}
 
 	public List<Ball> getBalls() {
 		return balls;
 	}
 	
 	/**
 	 * Checks if a click occured on a ball, and if it did, gives the ball
 	 * some extra velocity.
 	 * @param x the x-coord of the click.
 	 * @param y the y-coord of the click.
 	 */
	public void clickRegistered(float x, float y) {
 		for (Ball ball : balls) {
 			//check if click occured on a ball
 			if (ball.getPosition().dst(x, y) < ball.getRadius()) {
 				int speedMultiplier = 10;
 				// add speed towards the click. The longer the click from the balls
 				// center, the larger the gain.
 				float incX = (x - ball.getPosition().x) * speedMultiplier;
 				float incY = (y - ball.getPosition().y) * speedMultiplier;
 				ball.getVelocity().add(incX, incY);
 				return; //can't click on more than one ball
 			}
 		}
 	}
 }
