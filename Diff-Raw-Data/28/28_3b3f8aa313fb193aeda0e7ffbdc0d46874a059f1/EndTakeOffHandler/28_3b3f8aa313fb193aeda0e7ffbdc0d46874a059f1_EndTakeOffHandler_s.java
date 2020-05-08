 package simulation.eventHandlers;
 
 import simulation.definition.EventScheduler;
 import simulation.definition.TransactionalEventHandler;
 import simulation.model.Aircraft;
 import simulation.model.Airport;
 import simulation.model.Event;
 import simulation.model.RollBackVariables;
 
 public class EndTakeOffHandler implements TransactionalEventHandler {
 
 	@Override
 	public void process(Event e, EventScheduler scheduler) {
 		final Aircraft ac = e.getAirCraft();
 		final Airport ap = e.getAirPort();
 		ac.setState(Aircraft.ON_FLIGHT);
 		ac.setLastX(ap.getX2());
 		ac.setLastY(ap.getY2());
		long lastTime = ac.getLastTime();
 		ac.setLastTime(e.getTimeStamp());
 		ap.setRunWayFree(true);
 		ap.unsubscribeAircraft(ac);
 		long duration = (long) (ap.getDistanceTo(ac.getDestination()) / Aircraft.MAX_SPEED);
 
 		Event e1 = new Event(Event.LEAVE_AIRSPACE, e.getTimeStamp() + duration / 2, ac.getOrigin(), ac);
		e1.setRollBackVariable(new RollBackVariables(lastTime));
 		scheduler.scheduleEvent(e1);
 		Event e2 = new Event(Event.PROCESS_QUEUES, e.getTimeStamp(), ap, null);
 		scheduler.scheduleEvent(e2);
 		Event e3 = new Event(Event.ADD_TO_ANIMATION, e.getTimeStamp(), null, ac);
 		scheduler.scheduleEvent(e3);
 	}
 
 	@Override
 	public void rollback(Event e, EventScheduler scheduler) {
 		final Aircraft ac = e.getAirCraft();
 		final Airport ap = e.getAirPort();
 		ac.setState(Aircraft.TAKING_OFF);
 		ac.setLastX(ap.getX1());
 		ac.setLastY(ap.getY1());
		// TODO last time?
 		ap.setRunWayFree(false);
 		ap.subscribeAircraft(ac);
 
 		long duration = (long) (ap.getDistanceTo(ac.getDestination()) / Aircraft.MAX_SPEED);
 
 		Event e1 = new Event(Event.LEAVE_AIRSPACE, e.getTimeStamp() + duration / 2, ac.getOrigin(), ac);
 		e1.setAntiMessage(true);
 		scheduler.scheduleEvent(e1);
 		Event e2 = new Event(Event.PROCESS_QUEUES, e.getTimeStamp(), ap, null);
 		e2.setAntiMessage(true);
 		scheduler.scheduleEvent(e2);
 		Event e3 = new Event(Event.ADD_TO_ANIMATION, e.getTimeStamp(), null, ac);
 		e3.setAntiMessage(true);
 		scheduler.scheduleEvent(e3);
 
 	}
 
 }
