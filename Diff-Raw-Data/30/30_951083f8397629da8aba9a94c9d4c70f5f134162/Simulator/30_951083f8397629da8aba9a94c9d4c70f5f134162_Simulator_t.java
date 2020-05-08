 package simulation;
 
 import car.Vehicle;
 
 import common.GlobalConstants;
 
 import environment.Gaia;
import environment.IWayPoint;
import environment.VehicleWayPoint;
 import environment.WayPointManager;
 
 public class Simulator implements IEventTarget<CrashEvent> {
 
 	protected long startTime;
 	protected long lastIteration;
 
 	
 	/**
 	 * singleton instance
 	 */
 	private static Simulator instance;
 
 	/**
 	 * 
 	 * method to access the singleton instance
 	 * 
 	 * @return singleton instance
 	 */
 	public synchronized static Simulator getInstance() {
 		if (instance == null) {
 			instance = new Simulator();
 		}
 		return instance;
 	}
 	
 	private Simulator(){
 
 	}
 
 	public void startSimulation(long timestamp, double factor) {
 		this.startTime = timestamp;
 		GlobalConstants.getInstance().setInitialTimestamp(timestamp);
 		this.lastIteration = 0;
 		GlobalConstants.getInstance().setTimeFactor(factor);
 	}
 
 	public synchronized void update(long timestamp) throws Exception {
 		float difference = (timestamp - (this.startTime + this.lastIteration)); // Math.round((double)(timestamp-this.startTime)/GlobalConstants.getInstance().getTimeFactor());
 
 		this.lastIteration += difference;
 		Boolean goOn = true;
 		Gaia.getInstance().updateVehicles(difference);
 		while (goOn) {
 			IEvent actualEvent = EventQueue.getInstance().getNextEvent(
 					this.lastIteration);
 			if (actualEvent != null) {
 				actualEvent.handleEvent();
 			} else {
 				goOn = false;
 			}
 		}
 	}
 	
 	public synchronized void handleEvent(CrashEvent event){
 		System.out.println("a crash occured");
 		for (Vehicle vehicle:event.getVehicles()){
 			vehicle.freeze();
 			try{
 				WayPointManager.getInstance().remove(vehicle.getWayPoint());
			}catch (Exception ex){
				System.out.println(ex);
			}
 		}
 	}
 
 }
