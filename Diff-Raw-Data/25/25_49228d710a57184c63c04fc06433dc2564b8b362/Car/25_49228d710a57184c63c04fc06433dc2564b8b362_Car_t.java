 package car;
 
 import simulation.VehicleEvent;
 
 import common.GlobalConstants;
 
 import environment.CarWayPoint;
 import environment.ILane;
 import environment.IMovable;
 import environment.WayPointManager;
 
 /**
  * The first implemented vehicle
  * 
  */
 public class Car extends Vehicle {
 
 	long lastStep;
 
 	/**
 	 * The maximal acceleration of the car
 	 */
 	float maxAcceleration = 5;
 
 	/**
 	 * The maximal deceleration of the car
 	 */
 	float maxDeceleration = 8;
 
 	/**
 	 * Initialis1e the car's variables
 	 * 
 	 * @param lane
 	 *            The lane to put the car on
 	 */
 	public Car(ILane lane) {
 		super(lane);
 		this.speed = 0;
 		this.dimension = new VehicleDimension(1.8f * GlobalConstants
 				.getInstance().getScale(), 3.5f * GlobalConstants.getInstance()
 				.getScale());
 		lastStep = 0;
 	}
 
 	/**
 	 * Initialise the car's variables
 	 * 
 	 * @param lane
 	 *            The lane to put the car on
 	 * @param drivenLaneDistance
 	 *            The distance on the lane to put the car on
 	 */
 	public Car(ILane lane, float drivenLaneDistance)
 			throws IllegalArgumentException {
 		super(lane, drivenLaneDistance);
 		this.speed = 0f;
 
 		this.dimension = new VehicleDimension(1.8f * GlobalConstants
 				.getInstance().getScale(), 3.5f * GlobalConstants.getInstance()
 				.getScale());
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	protected void adjustSpeed(float timestep) throws CarCannotReverseException {
 		this.speed += this.acceleration * (timestep / 1000);
 		if (this.speed < 0) {
 			this.speed = 0;
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @param VehilceEvent
 	 */
 	@Override
 	public void handleEvent(VehicleEvent event) {
 		if (!freezed) {
 			if (lastStep == 0) {
 				lastStep = event.getTimeStamp();
 				accelerate(event.getTargetAcceleration());
 			} else {
 				lastStep = event.getTimeStamp();
 				accelerate(event.getTargetAcceleration());
 			}
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void createWayPoint() {
 		this.wayPoint = new CarWayPoint(this);
 
 		try {
 			WayPointManager.getInstance().add(this.wayPoint);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	protected float maxAcceleration() {
 		return this.maxAcceleration;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 
 	@Override
 	protected float maxDeceleration() {
 		return this.maxDeceleration;
 	}
 
 	@Override
 	public IMovable getWayPoint() {
 		return this.wayPoint;
 	}
 }
