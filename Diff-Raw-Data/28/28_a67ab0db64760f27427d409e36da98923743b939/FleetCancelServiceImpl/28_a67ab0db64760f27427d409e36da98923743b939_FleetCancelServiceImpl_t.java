 package de.abbaddie.wot.fleet.data.fleet;
 
 import org.joda.time.DateTime;
 import org.joda.time.Duration;
import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 
import de.abbaddie.wot.data.event.EditableEvent;
import de.abbaddie.wot.data.event.EventRepository;
 
 @Service
 public class FleetCancelServiceImpl implements FleetCancelService {
	@Autowired
	protected EventRepository eventRepo;
	
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
 		
		EditableEvent returnEvent = eventRepo.toEditable(fleet.getEvents().get(DefaultEventTypes.RETURN));
 		final DateTime newReturnTime = time.plus(done);
 		
		returnEvent.setTime(newReturnTime);
		
 		fleet.setEvent(DefaultEventTypes.IMPACT, null);
 	}
 }
