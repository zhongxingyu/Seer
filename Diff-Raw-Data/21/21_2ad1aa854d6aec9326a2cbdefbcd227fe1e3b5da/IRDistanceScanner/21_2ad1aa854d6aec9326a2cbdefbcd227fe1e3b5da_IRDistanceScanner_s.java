 package raisa.simulator;
 
 import raisa.domain.WorldModel;
 
 public class IRDistanceScanner implements DistanceScanner {
 
 	@Override
 	public float scanDistance(WorldModel worldModel, RobotState roverState, float heading) {
		// TODO this points to wrong direction
		return worldModel.traceRay(roverState.getPosition(), (float)Math.toRadians(roverState.getHeading() + heading));
 	}
 
 }
