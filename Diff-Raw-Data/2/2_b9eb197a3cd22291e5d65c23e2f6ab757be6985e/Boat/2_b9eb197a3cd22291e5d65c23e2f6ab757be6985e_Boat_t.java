 package siver.boat;
 
 
 import java.awt.geom.Point2D;
 
 import repast.simphony.context.Context;
 import repast.simphony.engine.environment.RunEnvironment;
 import repast.simphony.engine.schedule.ScheduledMethod;
 import repast.simphony.space.SpatialMath;
 import repast.simphony.space.continuous.ContinuousSpace;
 import repast.simphony.space.continuous.NdPoint;
 import siver.context.SiverContextCreator;
 import siver.cox.Cox;
 import siver.experiments.BoatRecord;
 import siver.river.River;
 import siver.river.lane.Lane;
 import siver.river.lane.LaneEdge;
 import siver.river.lane.LaneNode;
 
 /** 
  * BoatAgent is a dumb agent, at each step it will carry on moving in the direction it is facing and at the speed it was set to
  * The CoxAgent will make decisions based on the boat's location to alter these speed and angle properties
  * 
  * @author henryaddison
  *
  */
 public class Boat {
 	
 	private static final int MAX_GEAR  = 10;
 	
 	//the river the boat is on and the cox in the boat
 	private River river;
 	private Cox cox;
 	
 	//the current gear and orientation
 	private double orientation;
 	private int gear;
 	private double gearMultiplier;
 	
 	//distance that can be travelled this tick
 	private double tick_distance_remaining;
 	private double total_distance_covered;
 	
 	//keep a reference of the space the boat is in for easier movement
 	private ContinuousSpace<Object> space;
 	private Context<Object> context;
 	
 	//to keep track of statistics about boat
 	private BoatRecord record; 
 	
 	public Boat(River river, Context<Object> context, ContinuousSpace<Object> space, double gearMult) {
 		this.river = river;
 		this.space = space;
 		this.context = context;
 		this.gearMultiplier = gearMult;
 	}
 	
 	public void launch(Cox cox, Point2D.Double pt) {
 		//initially the boat points straight up and is going at speed 10
 		this.orientation = 0;
 		this.gear = 0;
 		this.cox = cox;
 		space.moveTo(this, pt.getX(), pt.getY());
 		this.total_distance_covered = 0;
 	}
 	
 	public void launchComplete(Integer launch_schedule_id) {
 		this.record = new BoatRecord(launch_schedule_id, SiverContextCreator.getTickCount(), cox.desired_gear(), gearMultiplier, cox.brain_type());
 	}
 	
 	public void land() {
 		context.remove(this);
 		if(this.record != null) {
 			this.record.landed(SiverContextCreator.getTickCount());
 		}
 	}
 	
 	//MOVEMENT
 	@ScheduledMethod(start = 1, interval = 1, priority=10)
 	public void run() {
 		tick_distance_remaining = getSpeed();
 		moveBoat();
 	}
 	
 	private void moveBoat() {
 		BoatNavigation location = cox.getNavigator();
 		double distance_till_next_node = location.getTillEdgeEnd();
 		if(tick_distance_remaining >= distance_till_next_node) {
 			location.moveToEdgeEnd();
 			total_distance_covered += distance_till_next_node;
 			record.moved(distance_till_next_node, getGear());
 			tick_distance_remaining = tick_distance_remaining - distance_till_next_node;
 			
 			LaneNode steer_from = location.getDestinationNode();
 			Lane lane = steer_from.getLane();
 			LaneEdge next_edge = lane.getNextEdge(steer_from, location.headingUpstream());
 			
 			location.updateEdge(next_edge);
			moveBoat();
 		} else {
 			location.moveAlongEdge(tick_distance_remaining);
 			total_distance_covered += tick_distance_remaining;
 			record.moved(tick_distance_remaining, getGear());
 			tick_distance_remaining = 0;
 		}
 	}
 	
 	public void move(double dist) {
 		move(dist, orientation);
 	}
 	
 	public void move(double dist, double heading) {
 		space.moveByVector(this, dist, heading, 0);
 	}
 	
 	public void moveTo(NdPoint pt) {
 		space.moveTo(this, pt.toDoubleArray(null));
 	}
 	
 	public void steerToward(Point2D.Double pt) {
 		NdPoint otherPoint = new NdPoint(pt.getX(), pt.getY());
 		double angle = SpatialMath.calcAngleFor2DMovement(space, getLocation(), otherPoint);
 		setAngle(angle);
 	}
 	
 	//GETTERS AND SETTERS
 	public double getAngle() {
 		return orientation;
 	}
 	
 	public void setAngle(double angle) {
 		this.orientation = angle;
 	}
 	
 	public int getGear() {
 		return gear;
 	}
 	
 	public void setGear(int newValue) {
 		if(newValue < 0) {
 			gear = 0;
 		} else if(newValue > MAX_GEAR) {
 			gear = 10;
 		} else {
 			gear = newValue;
 		}
 		
 	}
 	
 	public void shiftUp() {
 		setGear(gear+1);
 	}
 	
 	public void shiftDown() {
 		setGear(gear-1);
 	}
 	
 	public double getSpeed() {
 		return gear*gearMultiplier;
 	}
 	
 	public NdPoint getLocation() {
 		return space.getLocation(this);
 	}
 	
 	public ContinuousSpace<Object> getSpace() {
 		return space;
 	}
 	
 	public Context<Object> getContext() {
 		return context;
 	}
 	
 	public River getRiver() {
 		return river;
 	}
 	
 	public void deadStop() {
 		setGear(0);
 		tick_distance_remaining = 0;
 	}
 	
 	public double getTickDistanceRemaining() {
 		return tick_distance_remaining;
 	}
 	
 	public void setTickDistanceRemaining(double value) {
 		tick_distance_remaining = value;
 	}
 	
 	public double total_distance_covered() {
 		return total_distance_covered;
 	}
 	
 }
