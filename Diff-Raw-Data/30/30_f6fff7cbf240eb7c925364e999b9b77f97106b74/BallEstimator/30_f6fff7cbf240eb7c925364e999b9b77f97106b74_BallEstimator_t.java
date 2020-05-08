 package balle.world;
 
 import org.apache.log4j.Logger;
 
 
 public class BallEstimator {
 
 	private static final Logger LOG = Logger.getLogger(BallEstimator.class);
 
 	private DESP velocityPredictor;
 	private DESP positionPredictor;
 	
 	private Velocity velocity;
 
 	public BallEstimator() {
 		velocityPredictor = new DESP(0.7);
 		positionPredictor = new DESP(0.4);
 		
 		velocity = new Velocity(0, 0, 1000);
 	}
 	
 	/**
 	 * Get the estimated current position
 	 * 
 	 * @return The ball's current position
 	 */
 	public Coord getPosition() {
 		return estimatePosition(0);
 	}
 
 	/**
 	 * Get the ball's estimated future position
 	 * 
 	 * @param frames
 	 *            How many frames to predict
 	 * @return The estimated position
 	 */
 	public Coord estimatePosition(int frames) {
 		return positionPredictor.predict(frames);
 	}
 
 	/**
 	 * Get the ball's estimated current velocity
 	 * 
 	 * @return The current velocity of the ball
 	 */
 	public Velocity getVelocity() {
 		return velocity;
 	}
 
 	public void update(Coord pos, double dt) {
 
 		if (pos == null) {
 			pos = getPosition();
 		}
 
 		velocityPredictor.update(pos);
 		positionPredictor.update(pos);
 
		velocity = new Velocity(velocityPredictor.predict((int) (1000 / dt))
				.sub(pos), 1000);
 	}
 }
