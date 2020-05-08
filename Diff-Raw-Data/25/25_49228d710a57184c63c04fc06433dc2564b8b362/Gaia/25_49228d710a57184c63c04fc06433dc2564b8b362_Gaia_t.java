 package environment;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import simulation.DriverEvent;
 import simulation.EventQueue;
 import simulation.Simulator;
 import simulation.builder.IXMLWorldBuilder;
 import simulation.builder.XMLWorldBuilder;
 import car.CarCannotReverseException;
 import car.IVehicle;
 import car.VehicleFactory;
 
 import common.GlobalConstants;
 
 import driver.Character;
 import driver.Driver;
 import driver.IDriver;
 import driver.Physics;
 
 public class Gaia implements IGaia {
 	
 	/**
 	 * private singleton instance
 	 */
 	
 	private static Gaia instance;
 	
 	/**
 	 * private Gaia constructor
 	 * @throws Exception
 	 */
 	
 	private Gaia() throws Exception {
 		Gaia.vehicles = new ArrayList<IVehicle>();
 		Gaia.waypoints = new ArrayList<IWayPoint>();
 		this.rebuildWorld();
 	}
 	
 	/**
 	 * singleton get instance method
 	 * @return
 	 * @throws Exception
 	 */
 	
 	public synchronized static Gaia getInstance() 
 			throws Exception {
 		if (Gaia.instance == null) {
 			Gaia.instance = new Gaia();
 		}
 		
 		return Gaia.instance;
 	}
 	
 	/**
 	 * destroy the singleton instance
 	 */
 	@Override
 	public void destroy() {
 		Gaia.instance = null;
 	}
 	
 	/**
 	 * Junctions
 	 * @see environment.IGaia#getJunctions()
 	 */
 	private static List<IJunction> junctions;
 	
 	@Override
 	public List<IJunction> getJunctions() {
 		return Gaia.junctions;
 	}
 
 	/**
 	 * Roads
 	 * @see environment.IGaia#getRoads()
 	 */
 	private static List<IRoad> roads;
 	
 	@Override
 	public List<IRoad> getRoads() {
 		return Gaia.roads;
 	}
 	
 	/**
 	 * Vehicle
 	 * @see environment.IGaia#setVehicle(car.IVehicle)
 	 */
 	private static List<IVehicle> vehicles;
 	
 	/**
 	 * add another vehicle
 	 */
 	
 	@Override
 	public void setVehicle(IVehicle vehicle) {
 		Gaia.vehicles.add(vehicle);
 	}
 
 	/**
 	 * get the vehicles  from the gaia
 	 */
 	
 	@Override
 	public List<IVehicle> getVehicles() {
 		return Gaia.vehicles;
 	}
 	
 	/*
 	 * The way points
 	 */
 	private static List<IWayPoint> waypoints;
 	
 	/**
 	 * get a list of all the way points
 	*/ 
 	
 	@Override
 	public List<IWayPoint> getWayPoints() {
 		return Gaia.waypoints;
 	}
 	
 	/**
 	 * add another way point 
 	 * @param waypoint
 	 */
 	
 	public void setWayPoint(IWayPoint waypoint) {
 		Gaia.waypoints.add(waypoint);
 	}
 	
 	/**
 	 * add a list of waypoints
 	 * @param waypoints
 	 */
 	
 	public void setWayPoints(List<IWayPoint> waypoints) {
 		Gaia.waypoints.addAll(waypoints);
 	}
 
 	/**
 	 * The world builder call to action
 	 */
 	public void rebuildWorld() throws Exception {
 		XMLWorldBuilder.reset();
 		WayPointManager.destroy();
 		IXMLWorldBuilder world = XMLWorldBuilder.getInstance(); 
 			
 		XMLWorldBuilder.getInstance().generate(GlobalConstants.getInstance().getStreetXMLSchema());
 		
 		Gaia.roads = XMLWorldBuilder.getInstance().getRoads();
 		Gaia.junctions = XMLWorldBuilder.getInstance().getJunctions();
 		Gaia.waypoints = XMLWorldBuilder.getInstance().getAllWayPoints();
 		
 		GlobalConstants.getInstance().setWorldBoundaries(
 				world.getWorldBoundaries());
 		WayPointManager.getInstance().setWayPoints();
 		
 		GlobalConstants.getInstance().setScale(world.getScale());
 		this.setWayPoints(WayPointManager.getInstance().getWayPoints());
 	}
 
 	/**
 	 * get a list of all traffic carriers
 	 */
 	
 	@Override
 	public List<ITrafficCarrier> getTrafficCarriers() {
 		List<ITrafficCarrier> everything = new ArrayList<ITrafficCarrier>();
 		everything.addAll(Gaia.roads);
 		everything.addAll(Gaia.junctions);
 		return everything;
 	}
 	
 	/**
 	 * Add a road user to the simulation
 	 * 
 	 * Create a new car and a driver with the given properties
 	 * and put the whole thing into action
 	 * @param vehicleType The type of the vehicle
 	 * @param lane The lane to put the vehicle on
 	 * @param character The character properties of the driver
 	 * @param physics The physics of the driver
 	 * @throws Exception
 	 */
 	@Override
 	public IVehicle addRoadUser(
 			VehicleFactory.VehicleType vehicleType, 
 			ILane lane, 
 			Character character, 
 			Physics physics) throws Exception {
 		
 		IVehicle vehicle = VehicleFactory.createVehicle(vehicleType, lane);
 		this.createDriver(character, physics, vehicle);
 		
 		return vehicle;
 	}
 	
 	/**
 	 * Add a road user to the simulation
 	 * 
 	 * Create a new car and a driver with the given properties
 	 * and put the whole thing into action
 	 * @param vehicleType The type of the vehicle
 	 * @param drivenLaneDistance The distance on the lane the vehicle has already driven
 	 * @param lane The lane to put the vehicle on
 	 * @param character The character properties of the driver
 	 * @param physics The physics of the driver
 	 * @throws Exception
 	 */
 	@Override
 	public IVehicle addRoadUser(
 			VehicleFactory.VehicleType vehicleType, 
 			float drivenLaneDistance,
 			ILane lane, 
 			Character character, 
 			Physics physics) throws Exception {
 		IVehicle vehicle = VehicleFactory.createVehicle(vehicleType, lane, drivenLaneDistance);
 		
 		this.createDriver(character, physics, vehicle);
 		
 		return vehicle;
 	}
 	
 	/**
 	 * Helper function to create the driver 
 	 * 
 	 * @see Simulator#addRoadUser(car.VehicleFactory.VehicleType, ILane, Character, Physics)
 	 * @param character
 	 * @param physics
 	 * @param vehicle
 	 */
 	private void createDriver(Character character, Physics physics,
 			IVehicle vehicle) {
 		IDriver driver =  new Driver(vehicle, character, physics);
 
 		EventQueue.getInstance().addEvent(
 			new DriverEvent(
 				1,
 				driver
 			).setMonitoring(true)
 		);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * @throws CarCannotReverseException 
 	 */
 	@Override
 	public void updateVehicles(float timestep) throws CarCannotReverseException {
		
 		for (IVehicle vehicle: this.getVehicles()) {
 			vehicle.updatePosition(timestep);
 		}
 	}
 }
