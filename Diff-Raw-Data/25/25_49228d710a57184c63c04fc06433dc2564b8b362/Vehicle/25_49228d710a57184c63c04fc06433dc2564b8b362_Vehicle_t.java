 package car;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Queue;
 import java.util.concurrent.ArrayBlockingQueue;
 
 import simulation.VehicleEvent;
 
 import common.GlobalConstants;
 import common.IObservable;
 import common.IObserver;
 import common.IVector;
 import common.Vector;
 
 import driver.DriverView;
 import driver.IDriverView;
 import environment.ILane;
 import environment.IMovable;
 import environment.LaneLengthExceededException;
 import environment.WayPointManager;
 
 /**
  * The abstract vehicle
  * 
  * This class contains all the needed information and methods for a vehicle.
  * 
  */
 public abstract class Vehicle implements IVehicle, IObservable {
 
 	/**
 	 * List of Observers, that get notified when a change occurs
 	 */
 
 	private List<IObserver> observers;
 
 	protected boolean freezed;
 	
 	/**
 	 * This is more like a temporary hack
 	 */
 	// protected Animus animus;
 
 	/**
 	 * The way point that belongs to this vehicle
 	 */
 	protected IMovable wayPoint;
 
 	/**
 	 * The direction the vehicle is heading
 	 * 
 	 * This vector has always the length 1 (unit vector)
 	 */
 	protected IVector direction;
 
 	/**
 	 * The last direction
 	 */
 	protected IVector lastPosition;
 
 	/**
 	 * The current speed of the vehicle
 	 */
 	protected float speed = 0;
 
 	/**
 	 * The position in the simulation of the vehicle
 	 */
 	protected IVector position;
 
 	public final static int queueSize = 12;
 
 	/**
 	 * The vehicle's dimension
 	 */
 	protected VehicleDimension dimension;
 
 	/**
 	 * The driver's view
 	 * 
 	 * @TODO probably move this to another place
 	 */
 	protected DriverView driverView;
 
 	@Override
 	public VehicleDimension getDimension() {
 		return dimension;
 	}
 
 	@Override
 	public void setDimension(VehicleDimension dimension) {
 		this.dimension = dimension;
 	}
 
 	/**
 	 * The current acceleration
 	 * 
 	 * This acceleration is added to the speed and/or direction
 	 */
	protected float acceleration = 0;
 
 	/**
 	 * The lane the vehicle is on
 	 */
 	protected Queue<ILane> lanes = new ArrayBlockingQueue<ILane>(
 			Vehicle.queueSize);
 
 	/**
 	 * The driven distance on the lane
 	 */
 	protected float drivenLaneDistance = 0;
 
 	@Override
 	public float getSpeed() {
 		return this.speed;
 	}
 
 	/**
 	 * Construct
 	 * 
 	 * @param lane
 	 *            The lane the vehicle is on
 	 */
 	public Vehicle(ILane lane) throws IllegalArgumentException {
 		if (lane == null) {
 			throw new IllegalArgumentException();
 		}
 
 		this.construct(lane, 0);
 	}
 
 	/**
 	 * Construct
 	 * 
 	 * Create a car and position it on a lane
 	 * 
 	 * @param lane
 	 *            The lane the car is on
 	 * @param drivenLaneDistance
 	 *            The position on the lane
 	 */
 	public Vehicle(ILane lane, float drivenLaneDistance)
 			throws IllegalArgumentException {
 		if (lane == null) {
 			throw new IllegalArgumentException();
 		}
 
 		this.construct(lane, drivenLaneDistance);
 	}
 
 	/**
 	 * Initialize all needed variables
 	 * 
 	 * @param lane
 	 * @param drivenLaneDistance
 	 */
 	private void construct(ILane lane, float drivenLaneDistance) {
 		this.lanes.add(lane);
 		this.drivenLaneDistance = drivenLaneDistance;
 		try {
 			this.position = this.lanes.peek().getPositionOnLane(
 					this.drivenLaneDistance);
 		} catch (LaneLengthExceededException e) {
 			try {
 				this.position = this.lanes.peek().getPositionOnLane(0);
 			} catch (LaneLengthExceededException e1) {
 				e1.printStackTrace();
 			}
 		}
 		this.initializeDirection();
 		this.createWayPoint();
 
 		this.driverView = new DriverView(this.direction.normalize(),
 				this.position);
 	}
 
 	/**
 	 * Set the direction upon initialization
 	 */
 	private void initializeDirection() {
 		this.direction = this.lanes.peek().getFirstILaneSegment().getEndPoint()
 				.sub(this.lanes.peek().getFirstILaneSegment().getStartPoint());
 		this.lastPosition = this.lanes.peek().getFirstILaneSegment()
 				.getStartPoint();
 	}
 
 	/**
 	 * Calculate the real acceleration of the vehicle
 	 * 
 	 * @param acceleration
 	 * @return
 	 */
 	protected float calculateAcceleration(float acceleration) {
 		if (acceleration > 0) {
 			return this.maxAcceleration() * acceleration;
 		} else {
 			return this.maxDeceleration() * acceleration;
 		}
 	}
 
 	/**
 	 * Return the maximal acceleration
 	 * 
 	 * @return
 	 */
 	protected abstract float maxAcceleration();
 
 	/**
 	 * Return the maximal deceleration
 	 * 
 	 * @return
 	 */
 	protected abstract float maxDeceleration();
 
 	/**
 	 * Set the acceleration of the vehicle
 	 */
 	@Override
 	public void accelerate(float acceleration) {
 		this.acceleration = this.calculateAcceleration(acceleration);
 	}
 
 	@Override
 	public ILane getLane() {
 		return this.lanes.peek();
 	}
 
 	@Override
 	public IVector getPosition() {
 		return new Vector(new float[] { this.position.getComponent(0),
 				this.position.getComponent(1) });
 	}
 
 	@Override
 	public void updatePosition(float timestep) throws CarCannotReverseException {
 		if (!freezed) {
 			this.adjustSpeed(timestep);
 			this.drivenLaneDistance += (this.speed / 36) // this seems weird?
 					* GlobalConstants.getInstance().getScale();
 			try {
 				this.position = this.lanes.peek().getPositionOnLane(
 						this.drivenLaneDistance);
 			} catch (LaneLengthExceededException e) {
 				// change lane
 				this.drivenLaneDistance = this.drivenLaneDistance
 						- this.lanes.peek().getLength();
 				this.notify("laneChange");
 				try {
 					this.position = this.lanes.peek().getPositionOnLane(
 							this.drivenLaneDistance);
 				} catch (LaneLengthExceededException e1) {
 				} catch (NullPointerException e2) {
 					System.out.println("No further lane found!");
 					System.out.println("The car is " + this);
 					System.out.println("The car was on position "
 							+ this.position);
 					return;
 				}
 			} catch (NullPointerException e2) {
 				System.out.println("No further lane found!");
 				System.out.println("The car is " + this);
 				System.out.println("The car was on position " + this.position);
 				return;
 			}
 
 			this.driverView.setPosition(this.position);
 			this.updateDirection();
 			try {
 				WayPointManager.getInstance().move(this.wayPoint,
 						this.getPosition().getComponent(0),
 						this.getPosition().getComponent(1));
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	/**
 	 * Update the direction of the car
 	 * 
 	 * The difference vector of the old and new position is the direction the
 	 * car is looking
 	 */
 	public void updateDirection() {
 		if (this.position.compareTo(this.lastPosition) == 0) {
 			this.direction = this.lanes
 					.peek()
 					.getFirstILaneSegment()
 					.getEndPoint()
 					.sub(this.lanes.peek().getFirstILaneSegment()
 							.getStartPoint());
 		} else {
 			this.direction = this.position.sub(this.lastPosition);
 			this.lastPosition = this.position;
 		}
 
 		this.driverView.setDirection(this.direction);
 	}
 
 	/**
 	 * Return the driven lane distance
 	 * 
 	 * @return Driven lane distance
 	 */
 	@Override
 	public float getDrivenLaneDistance() {
 		return this.drivenLaneDistance;
 	}
 
 	/**
 	 * Adjust the speed
 	 * 
 	 * @throws CarCannotReverseException
 	 */
 	protected abstract void adjustSpeed(float timestep)
 			throws CarCannotReverseException;
 
 	/**
 	 * Handle an event
 	 * 
 	 * @see car.IVehicle#handleEvent(simulation.IEvent)
 	 */
 	@Override
 	public abstract void handleEvent(VehicleEvent event);
 
 	@Override
 	public void setLane(ILane lane) {
 		this.lanes.add(lane);
 	}
 
 	@Override
 	public void setLanes(List<ILane> lanes) {
 		this.lanes.addAll(lanes);
 	}
 
 	@Override
 	public Queue<ILane> getLanes() {
 		return this.lanes;
 	}
 
 	@Override
 	public IVector getDirection() {
 		return this.direction;
 	}
 
 	@Override
 	public IDriverView getDriverView() {
 		return this.driverView;
 	}
 
 	/*
 	 * @Override public void setAnimus(Animus animus) { this.animus = animus; }
 	 */
 	/**
 	 * Create a way point from this vehicle
 	 */
 	public abstract void createWayPoint();
 
 	public void register(IObserver obs) {
 		if (observers == null) {
 			observers = new ArrayList<IObserver>();
 		}
 		observers.add(obs);
 	}
 
 	public void remove(IObserver obs) {
 		observers.remove(obs);
 	}
 
 	public void notify(String msg) {
 		for (IObserver obs : observers) {
 			obs.update(msg);
 		}
 	}
 
 	public abstract IMovable getWayPoint();
 
 	public void freeze() {
 		freezed = true;
 		speed = 0;
 		acceleration = 0;
 	}
 	
 	public boolean isFreezed(){
 		return freezed;
 	}
 	
 	public void removeWaypoint(){
 		this.wayPoint = null;
 	}
 }
