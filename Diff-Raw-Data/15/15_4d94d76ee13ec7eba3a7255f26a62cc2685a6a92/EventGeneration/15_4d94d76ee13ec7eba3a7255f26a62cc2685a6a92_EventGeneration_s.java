 package uk.frequency.glance.server.business.logic;
 
 import java.util.Date;
 import java.util.List;
 
 import org.hibernate.ObjectNotFoundException;
 import org.hibernate.Transaction;
 
 import uk.frequency.glance.server.business.remote.GoogleAPIs;
 import uk.frequency.glance.server.data_access.EventDAL;
 import uk.frequency.glance.server.data_access.TraceDAL;
 import uk.frequency.glance.server.data_access.UserDAL;
 import uk.frequency.glance.server.data_access.util.HibernateUtil;
 import uk.frequency.glance.server.model.component.Location;
 import uk.frequency.glance.server.model.component.Media;
 import uk.frequency.glance.server.model.component.Media.MediaType;
 import uk.frequency.glance.server.model.component.Position;
 import uk.frequency.glance.server.model.event.Event;
 import uk.frequency.glance.server.model.event.EventScore;
 import uk.frequency.glance.server.model.event.EventType;
 import uk.frequency.glance.server.model.event.StayEvent;
 import uk.frequency.glance.server.model.trace.PositionTrace;
 import uk.frequency.glance.server.model.trace.Trace;
 import uk.frequency.glance.server.model.user.EventGenerationInfo;
 import uk.frequency.glance.server.model.user.User;
 
 public class EventGeneration extends Thread {
 
 	Trace currentTrace;
 	EventGenerationInfo genInfo;
 
 	EventDAL eventDal;
 	TraceDAL traceDal;
 	UserDAL userDal;
 
 	private final int MIN_STAY_TIME = 10 * 60 * 1000; //in miliseconds
 	private final double MAX_STAY_RADIUS = Geometry.kmToDegrees(0.06); //in degrees
 
 	public EventGeneration(Trace currentTrace, EventDAL eventDal, TraceDAL traceDal, UserDAL userDal) {
 		this.currentTrace = currentTrace;
 		User user = userDal.findById(currentTrace.getUser().getId()); //unproxy from hibernate TODO: better way to do this?
 		this.genInfo = user.getEventGenerationInfo();
 		this.eventDal = eventDal;
 		this.traceDal = traceDal;
 		this.userDal = userDal;
 	}
 
 	@Override
 	public void run() {
 		Transaction tr = HibernateUtil.getSessionFactory().getCurrentSession().beginTransaction();
 		try{
 			if (currentTrace instanceof PositionTrace) {
 				handleTrace();
 			}
 			tr.commit();
 		}catch(RuntimeException e){
 			if(e instanceof ObjectNotFoundException){
 				//trace wasn't saved
 			}else{
 				e.printStackTrace();
 			}
             if (tr.isActive()) {
                 tr.rollback();
             }
 		}
 	}
 	
 	private void handleTrace(){
 		if (currentTrace instanceof PositionTrace) {
 			handlePositionTrace((PositionTrace) currentTrace);
 		} else if (currentTrace instanceof PositionTrace) {
 			//TODO handle ListenTrace
 		} else {
 			throw new AssertionError();
 		}
 	}
 
 	private void handlePositionTrace(PositionTrace currentTrace) {
 
 		User user = genInfo.getUser();
 		Event currentEvent = genInfo.getCurrentEvent();
 		
 		if (currentEvent == null) {
 			//create a new event
 			
 			//get all traces on a given time window before the current trace
 			Date timeWindowStart = TimeUtil.add(currentTrace.getTime(), -MIN_STAY_TIME);
 			List<PositionTrace> traces = traceDal.findAfter(user, timeWindowStart);
 			PositionTrace previous = traceDal.findRightBefore(user, timeWindowStart);
 			if(previous != null){
 				traces.add(0, previous);
 	
 				//find the bounding box of these traces
				double minLat = Double.MAX_VALUE;
				double minLng = Double.MAX_VALUE;
				double maxLat = Double.MIN_VALUE;
				double maxLng = Double.MIN_VALUE;
 				for (PositionTrace trace : traces) {
 					minLat = Math.min(minLat, trace.getPosition().getLat());
					maxLat = Math.min(maxLat, trace.getPosition().getLng());
					minLng = Math.min(minLng, trace.getPosition().getLat());
					maxLng = Math.min(maxLng, trace.getPosition().getLng());
 				}
 				
 				//check bounding box against the max radius
 				if (maxLat - minLat < 2 * MAX_STAY_RADIUS && maxLng - minLng < 2 * MAX_STAY_RADIUS) {
 					
 					//find the center of the bounding box
 					double avgLat = (minLat + maxLat) / 2;
 					double avgLng = (minLng + maxLng) / 2;
 					Position pos = new Position();
 					pos.setLat(avgLat);
 					pos.setLng(avgLng);
 					
 					//create a new event
 					Date start = previous.getTime();
 					Event newEvent = createStayEvent(user, pos, start, null);
 					genInfo.setCurrentEvent(newEvent);
 					eventDal.makePersistent(newEvent);
 					/*TEST*/System.out.println("EVENT CREATED");
 				}else{
 					//some traces are out of the radius (> MAX_STAY_RADIUS)
 				}
 			}else{
 				//trace history not long enough (< MIN_STAY_TIME)
 			}
 		} else {
 			//update current event 
 			
 			if (currentEvent instanceof StayEvent) {
 				StayEvent stay = ((StayEvent) currentEvent);
 				double distance = Geometry.distance(stay.getLocation().getPosition(), currentTrace.getPosition());
 				if(distance > 2*MAX_STAY_RADIUS){
 					//close current event
 					stay.setEndTime(currentTrace.getTime());
 					genInfo.setCurrentEvent(null);
 					eventDal.makePersistent(stay);
 					/*TEST*/System.out.println("EVENT CLOSED");
 				}
 				
 			} else {
 				// TODO handle other types of events
 			}
 		}
 
 		//update event generation data and persist
 		genInfo.setLastUsedTrace(currentTrace);
 		userDal.merge(genInfo); //TODO shouldn't need this. figure out why is hibernate throwing NonUniqueObjectException
 //		userDal.makePersistent(genInfo);
 		
 		// TODO notify client device
 	}
 
 	private Event createStayEvent(User user, Position pos, Date start, Date end) {
 		String imageUrl = GoogleAPIs.getStreetViewImageUrl(pos); // TODO get more from google places and also streetview with different headings, choose best image somehow
 		String name = GoogleAPIs.getLocationName(pos); // TODO get from google places
 		String address = GoogleAPIs.getLocationName(pos);
 
 		Location location = new Location();
 		location.setPosition(pos);
 		location.setAddress(address);
 		location.setName(name);
 		Media media = new Media();
 		media.setType(MediaType.IMAGE);
 		media.setUrl(imageUrl);
 		StayEvent event = new StayEvent();
 		event.setType(EventType.STAY);
 		event.setUser(user);
 		event.setStartTime(start);
 		event.setEndTime(end);
 		event.setLocation(location);
 		event.setMedia(media);
 		event.setScore(new EventScore());
 
 		return eventDal.makePersistent(event);
 	}
 
 }
