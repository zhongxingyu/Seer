 package de.abbaddie.wot.fleet.data.fleet;
 
 import org.joda.time.DateTime;
 import org.joda.time.Duration;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 
import de.abbaddie.wot.data.event.Event;
import de.abbaddie.wot.data.event.ForwardingEvent;
 
 @Service
 public class FleetCancelServiceImpl implements FleetCancelService {
 	@Override
 	public boolean isCancellable(EditableFleet fleet) {
 		return(fleet.getEvents().get(DefaultEventTypes.IMPACT) != null);
 	}
 	
 	@Transactional
 	@Override
 	public void cancel(EditableFleet fleet, DateTime time) {
 		if(!isCancellable(fleet)) {
 			throw new IllegalArgumentException();
 		}
 		
 		DateTime impactTime = fleet.getEvents().get(DefaultEventTypes.IMPACT).getTime();
 		DateTime returnTime = fleet.getEvents().get(DefaultEventTypes.RETURN).getTime();
 		
 		Duration flight = new Duration(impactTime, returnTime);
 		DateTime startTime = impactTime.minus(flight);
 		
 		Duration done = new Duration(startTime, time);
 		
		final Event oldReturnEvent = fleet.getEvents().get(DefaultEventTypes.RETURN);
 		final DateTime newReturnTime = time.plus(done);
 		
 		fleet.setEvent(DefaultEventTypes.IMPACT, null);
		fleet.setEvent(DefaultEventTypes.RETURN, new ForwardingEvent() {
			@Override
			protected Event delegate() {
				return oldReturnEvent;
			}
			
			@Override
			public DateTime getTime() {
				return newReturnTime;
			}
		});
 	}
 }
