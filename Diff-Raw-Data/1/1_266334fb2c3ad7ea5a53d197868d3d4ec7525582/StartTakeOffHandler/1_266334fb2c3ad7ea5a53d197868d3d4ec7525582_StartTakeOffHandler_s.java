 package simulation.eventHandlers;
 
 import simulation.definition.EventScheduler;
 import simulation.definition.TransactionalEventHandler;
 import simulation.model.Aircraft;
 import simulation.model.Airport;
 import simulation.model.Event;
 import simulation.model.RollBackVariables;
 
 public class StartTakeOffHandler implements TransactionalEventHandler {
 
 	static private final String KEY_LAST_TIME = "LAST_TIME"; 
 	static private final String KEY_TIMESTAMP = "TIMESTAMP"; 
 	
 	@Override
 	public void process(Event e, EventScheduler scheduler) {
 		final Aircraft ac = e.getAirCraft();
 		final Airport ap = e.getAirPort();
 		
 		ac.setState(Aircraft.TAKING_OFF);
 		ac.setLastTime(e.getTimeStamp());
 		RollBackVariables rv = new RollBackVariables(StartTakeOffHandler.KEY_LAST_TIME, e.getTimeStamp());
 		// we assume a constant acceleration maxAcceleration
 		long takeOffDuration = Aircraft.MAX_SPEED / Aircraft.MAX_ACCEL;
 		// distance for the accelerating part:
 		double dist = Aircraft.MAX_ACCEL * takeOffDuration * takeOffDuration
 				/ 2.0;
 		// the remaining part
 		double remainingDist = ap.getRunwayLength() - dist;
 		// the remaining distance of the runway we have constant speed
 		if (remainingDist > 0) {
 			takeOffDuration += remainingDist / Aircraft.MAX_SPEED;
 		} else {
 			throw new RuntimeException("runway too short!!");
 		}
 		// schedule next event
 		// to do!
 		long eventTimeStamp = e.getTimeStamp()
 				+ takeOffDuration;
 		Event eNew = new Event(Event.END_TAKE_OFF, eventTimeStamp, ap, ac); 
 		rv.setValue(StartTakeOffHandler.KEY_TIMESTAMP, eventTimeStamp);
 		scheduler.scheduleEvent(eNew);
 	}
 
 	@Override
 	public void rollback(Event e, EventScheduler scheduler) {
 		Aircraft ac = e.getAirCraft();
 		Airport ap = e.getAirPort();
 		
 		//rollback airplane state
 		ac.setState(Event.READY_FOR_DEPARTURE);
 		ac.setLastTime(e.getRollBackVariable().getLongValue(StartTakeOffHandler.KEY_LAST_TIME));
 		
 		//remove event from event List
 		Event endTakeOffEvent = new Event(
 				Event.END_TAKE_OFF,
 				e.getRollBackVariable().getLongValue(StartTakeOffHandler.KEY_TIMESTAMP),
 				ap,
 				ac
 		);
 		
 		endTakeOffEvent.setAntiMessage(true);
 		scheduler.scheduleEvent(endTakeOffEvent);
 	}
 
 }
