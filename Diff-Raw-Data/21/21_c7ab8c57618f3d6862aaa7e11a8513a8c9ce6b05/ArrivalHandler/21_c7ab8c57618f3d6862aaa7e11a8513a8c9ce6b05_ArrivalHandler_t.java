 package simulation.eventHandlers;
 
 import simulation.definition.EventScheduler;
 import simulation.definition.TransactionalEventHandler;
 import simulation.model.Aircraft;
 import simulation.model.Airport;
 import simulation.model.Event;
 import simulation.model.RollBackVariables;
 
 public class ArrivalHandler implements TransactionalEventHandler {
 
 	private static final String KEY_LAST_TIME = "LAST_TIME";
 	
 	@Override
 	public void process(Event e, EventScheduler scheduler) {
 		final Aircraft ac = e.getAirCraft();
 		final Airport ap = e.getAirPort();
 		ap.subscribeAircraft(ac);
 		ac.setState(Aircraft.ON_HOLDING_LOOP);
 		ac.setCurrentAirPort(ap);
 		ac.setLastX(ap.getX2());
 		ac.setLastY(ap.getY2());
 		long lastTime = ac.getLastTime(); // for a rollback
 		ac.setLastTime(e.getTimeStamp());
 		ap.addToHoldingQueue(ac);
 		Event eNew = new Event(Event.PROCESS_QUEUES, e.getTimeStamp(), ap, ac);
 		scheduler.scheduleEvent(eNew);
 		
 		// store the time for a possible rollback
		RollBackVariables vars = new RollBackVariables(KEY_LAST_TIME, lastTime);
 		e.setRollBackVariable(vars);
 	}
 
 	@Override
 	public void rollback(Event e, EventScheduler scheduler) {
 		final Aircraft ac = e.getAirCraft();
 		final Airport ap = e.getAirPort();
 		ap.unsubscribeAircraft(ac);
 		ac.setState(Aircraft.ON_FLIGHT);
 		ac.setCurrentAirPort(ac.getOrigin());
 		ac.setLastTime(e.getRollBackVariable().getLongValue(KEY_LAST_TIME));
 		ap.removeFromHoldingQueue(ac);
		Event eNew = new Event(Event.PROCESS_QUEUES, e.getTimeStamp(), ap, ac);
 		eNew.setAntiMessage(true);
 		scheduler.scheduleEvent(eNew);
 	}
 
 }
