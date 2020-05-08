 package traffic;
 
 import java.util.Iterator;
 
 import com.google.inject.Inject;
 import com.google.inject.assistedinject.Assisted;
 
 class VehicleImpl implements Vehicle {
 
	//@Inject Logger logger = Logger.getAnonymousLogger();
 
 	private final Iterator<Cell> remainingItinerary;
 	private Cell location;
 	private final JourneyHistory history;
 
 	@Inject VehicleImpl(
 			@Assisted final Itinerary remainingItinerary,
 			final JourneyHistory history) {
 		this(remainingItinerary.iterator(), history);
 	}
 
 	VehicleImpl(
 			final Iterator<Cell> remainingItinerary,
 			final JourneyHistory history) {
 		this.remainingItinerary = remainingItinerary;
 		this.history = history;
 	}
 
 	@Override
 	public Cell location() {
 		return location;
 	}
 
 	@Override
 	public void step() {
 		final Cell cell = remainingItinerary.next();
 		cell.enter(this);
 		location = cell;
 		history.stepped();
 
		//logger.info(String.format("Vehicle entered %s", location));
 	}
 
 	@Override
 	public int journeyTime() {
 		return history.journeyTime();
 	}
 
 	@Override
 	public String toString() {
 		return String.format("Vehicle located at %s", location());
 		//return roadNetworkReflectionToString(this);
 	}
 }
