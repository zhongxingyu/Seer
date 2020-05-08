 package mpi;
 
 import air.SimWorld;
 
 public class MpiEvent {
 	
 	public static void createEventFromMessage(MpiMessage msg){
 		// it's always a END_TAKEOFF event
 		
 		// process only events, not null messages
 		if(!msg.isEvent()){
 			return;
 		}		
 		
 		String aircraftName = msg.getAircraft();
 		String fromAirportName = msg.getFromAirport();
 		String toAirportName = msg.getToAirport();
 		
 		// simworld instance
 		SimWorld world = SimWorld.getInstance();
 		
 		// find airports
 		//Airport fromAp = world.getAirport();
 		//Airport toAp = world.get
 		
 		// create new aircraft
 		/*Aircraft ac = new Aircraft(aircraftName, ap);		
 		world.addAircraft(ac);
 		
 		// add arrival event to the airport
 		long duration = (long) (ap.getDistanceTo(ac.destination)/ac.maxSpeed);
 		Event e1 = new Event(Event.ARRIVAL,ac.getDestination(),e.getTimeStamp()+duration,ac.getDestination(),ac); // to do!
 		sched.scheduleEvent(e1);*/
 		
 		
 		
 		
 	}
 
 	/**
 	 * Return some default lookahead to constantly send null msgs
 	 */
 	public static long getDefaultLookAhead() {
		return 10;
 	}
 }
