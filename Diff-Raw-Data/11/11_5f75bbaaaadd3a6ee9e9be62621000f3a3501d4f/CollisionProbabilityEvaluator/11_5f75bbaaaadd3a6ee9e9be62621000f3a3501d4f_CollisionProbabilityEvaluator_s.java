 package br.ufrj.jfirn.intelligent.evaluation;
 
 import java.util.Arrays;
 
 import org.apache.commons.math3.util.FastMath;
 
 import br.ufrj.jfirn.common.Point;
 import br.ufrj.jfirn.intelligent.Collision;
 import br.ufrj.jfirn.intelligent.MobileObstacleStatistics;
 import br.ufrj.jfirn.intelligent.Thoughts;
 import br.ufrj.jfirn.intelligent.Trajectory;
 
 public class CollisionProbabilityEvaluator implements Evaluator {
 
 	@Override
 	public void evaluate(Thoughts thoughts, Instruction instruction, ChainOfEvaluations chain) {
 		//The trajectory of the extremities of the IntelligentRobot, considering its movement direction
 		final Trajectory[] myTrajectory;
 		{
 			final Point points[] = RobotExtremePointsCalculator
 					.pointsFromVerticalAxis(thoughts.myPosition(), thoughts.myDirection());
 			myTrajectory = new Trajectory[] {
 				new Trajectory(thoughts.myDirection(), points[0]),
 				new Trajectory(thoughts.myDirection(), points[1]),
 			};
 		}
 
 		for (Collision collision : thoughts.allColisions()) {
 			final MobileObstacleStatistics stats =
 				thoughts.obstacleStatistics(collision.withObjectId);
 
 			final Trajectory[] trajectories = Trajectory.fromStatistics(stats);
 
 			//Here, intersections are points in the X, Y plane. The next step is convert them
 			//to the Angle, Speed planes because we chose to work with the probability
 			//distributions of the robot angle and the robot speed.
 			final Point[] intersections = new Point[] {
 				myTrajectory[0].intersect(trajectories[0]),
 				myTrajectory[1].intersect(trajectories[0]),
 				myTrajectory[0].intersect(trajectories[1]),
 				myTrajectory[1].intersect(trajectories[1])
 			};
 
 			collision.area = Arrays.copyOf(intersections, intersections.length);
 
 			final Point moPosition = stats.lastKnownPosition();
 
			//Convert from XY coordinates to a polar coordinates around the mobile obstacle.
 			//Normalize the input, since BGD works only with 0 mean and 1 variance: (value - mean) / sqrt(var)
 			for(int i = 0; i < intersections.length; i++) {
 				final Point intersection = intersections[i];
 
 				double direction = moPosition.directionTo(intersection); //first, calculate the direction...
 				if (stats.directionVariance() != 0d) {//...then normalize it.
 					direction = (direction - stats.directionMean()) / FastMath.sqrt(stats.directionVariance());
 				} else {
 					direction = moPosition.directionTo(intersection) - stats.directionMean();
 				}
 
 				double speed = moPosition.distanceTo(intersection) / collision.time; //first, calculate the speed...
 				if (stats.speedVariance() != 0d) { //...then normalize it.
 					speed = (speed - stats.speedMean()) / FastMath.sqrt(stats.speedVariance());
 				} else {
 					speed = speed - stats.speedMean();
 				}
 
 				intersections[i] = new Point(direction, speed);
 			}
 
 			//calculate the collision probability
 			collision.probability =
 				BGD.cdfOfConvexQuadrilaterals(
 					intersections[0],
 					intersections[1],
 					intersections[2],
 					intersections[3],
 					stats.speedDirectionCorrelation());
 		}
 
 		chain.nextEvaluator(thoughts, instruction, chain);
 	}
 
 }
