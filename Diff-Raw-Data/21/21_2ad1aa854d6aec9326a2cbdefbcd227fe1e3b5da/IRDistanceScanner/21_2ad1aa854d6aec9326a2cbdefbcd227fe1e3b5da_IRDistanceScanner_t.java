 package raisa.simulator;
 
 import raisa.domain.WorldModel;
 
 public class IRDistanceScanner implements DistanceScanner {
 
 	@Override
 	public float scanDistance(WorldModel worldModel, RobotState roverState, float heading) {
		float angle = 360 - roverState.getHeading() + heading;
		float rad = (float)Math.toRadians(angle);
		return worldModel.traceRay(roverState.getPosition(), rad);
 	}
 
 }
