 package siver.river;
 
 import static java.lang.Math.PI;
 import java.awt.geom.Point2D;
 
 import repast.simphony.context.Context;
 import repast.simphony.space.continuous.ContinuousSpace;
 import siver.context.LaneContext;
 import siver.river.lane.Lane;
 import siver.river.lane.Lane.CompletedLaneException;
 import siver.river.lane.Lane.UnstartedLaneException;
 
 /**
  * Factory for making complete River objects.
  * 
  * @author henryaddison
  *
  */
 public class RiverFactory {
 	/**
 	 * Creates a simple, made-up river with a couple of corners.
 	 * 
 	 * @return completed River object based on the test coordinates
 	 */
 	public static River Test(Context<Object> context, ContinuousSpace<Object> space) {
 		LaneContext lane_context = new LaneContext();
 		context.addSubContext(lane_context);
 		
 		Lane up = new Lane(lane_context, "Upstream Lane");
 		Lane middle = new Lane(lane_context, "Middle Lane");
 		Lane down = new Lane(lane_context, "Downstream Lane");
 		
		up.start(new Point2D.Double(10, 50));
 		context.add(up);
		space.moveTo(up, 10, 50);
 		
		middle.start(new Point2D.Double(10, 30));
 		context.add(middle);
		space.moveTo(middle, 10, 30);
 		
		down.start(new Point2D.Double(10, 10));
 		context.add(down);
		space.moveTo(up, 10, 10);
 		
 		try {
 			for(int i = 1; i <= 40; i++) {
 				up.extend(0);
 				down.extend(0);
 				middle.extend(0);
 			}
 			
 			for(double theta = 0; theta < PI/2.0; theta += PI/20.0) {
 				up.extend(theta);
 			}
 			for(double theta = 0; theta < PI/2.0; theta += (PI/2.0)/(10+PI/2.0)) {
 				middle.extend(theta);
 			}
 			for(double theta = 0; theta < PI/2.0; theta += (PI/2.0)/(10+PI)) {
 				down.extend(theta);
 			}
 			
 			up.extend(PI/2.0);
 			up.extend(PI/2.0);
 			middle.extend(PI/2.0);
 			middle.extend(PI/2.0);
 			down.extend(PI/2.0);
 			
 			for(int i = 1; i<= 5; i++) {
 				up.extend(PI/2.0);
 				middle.extend(PI/2.0);
 				down.extend(PI/2.0);
 			}
 			
 			for(double theta = PI/2.0; theta > 0 ; theta -= PI/21.0) {
 				down.extend(theta);
 			}
 			for(double theta = PI/2.0; theta > 0; theta -= (PI/2.0)/(10+PI/2.0)) {
 				middle.extend(theta);
 			}
 			for(double theta = PI/2.0; theta > 0; theta -= (PI/2.0)/(10+PI)) {
 				up.extend(theta);
 			}
 			
 			for(int i = 1; i <= 20; i++) {
 				up.extend(0);
 				down.extend(0);
 				middle.extend(0);
 			}
 			
 		} catch (UnstartedLaneException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (CompletedLaneException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		River river = new River(up, middle, down);
 		
 		context.add(river);
 		space.moveTo(river, 0,0);
 		
 		return river;
 	}
 }
