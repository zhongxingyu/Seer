 package events;
 
 import priority.Priority;
 import routes.DistributionCentre;
 import routes.Firm;
 
 public class DiscontinueTransportEvent extends Event {
 	
 	private Firm firm;
 	private Priority priority;
 	private DistributionCentre origin;
 	private DistributionCentre destination;
 	
 	
	/**
	 * 
	 * @param firm
	 * @param priority
	 * @param origin
	 * @param destination
	 */
 	public DiscontinueTransportEvent(Firm firm, Priority priority,
 			DistributionCentre origin, DistributionCentre destination) {
 		super();
 		this.firm = firm;
 		this.priority = priority;
 		this.origin = origin;
 		this.destination = destination;
 	}
 	
 	
 	public Firm getFirm() {
 		return firm;
 	}
 	public Priority getPriority() {
 		return priority;
 	}
 	public DistributionCentre getOrigin() {
 		return origin;
 	}
 	public DistributionCentre getDestination() {
 		return destination;
 	}
 
 }
