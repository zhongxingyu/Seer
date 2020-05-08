 package com.appspot.analyser;
 
 import java.io.IOException;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.GregorianCalendar;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.logging.Logger;
 
 import javax.jdo.PersistenceManager;
 
 import junit.framework.TestCase;
 
 import org.junit.Test;
 import org.junit.experimental.runners.Enclosed;
 import org.junit.runner.RunWith;
 
 import com.appspot.datastore.PMF;
 import com.appspot.datastore.SphereInfo;
 import com.appspot.datastore.SphereName;
 import com.appspot.datastore.UserProfile;
 import com.appspot.datastore.UserProfileStore;
 import com.appspot.iclifeplanning.events.Event;
 
 @RunWith(Enclosed.class)
 public class Analyser {
 
 	public static final double CONFIDENCE = 0.1;
 	private static final double ALTERNATIVE = 0.3;
 	static final int TRIES = 20;
 	private int maxDepth = 3;
 	private int maxSuggestions = 3;
 	private Map<SphereName, List<Proposal>> proposals;
 	private List<IEvent> events;
 	private static final Logger log = Logger.getLogger("EventStore");
 	
 	public Analyser() {
 		proposals = new HashMap<SphereName, List<Proposal>>();
 	}
 
 	public List<List<Suggestion>> getSuggestions(List<? extends IEvent> events, String currentUserId) throws IOException {
 		UserProfile profile = UserProfileStore.getUserProfile(currentUserId);
 		this.events = (List<IEvent>) events;
 		return convertToSuggestions(this.getSuggestions(currentUserId, profile.getSpherePreferences(), profile.isFullyOptimized()));
 	}
 	@SuppressWarnings("unchecked")
 	private List<List<CalendarStatus>> getSuggestions(String userID, Map<SphereName, Double> spherePreferences, boolean optimizeFull) throws IOException {
 		if (events.size() == 0)
 			return null;
 		LinkedList<List<CalendarStatus>> result = new LinkedList<List<CalendarStatus>>();
 		CalendarStatus start = checkGoals(events, spherePreferences);
 		if (isCloseEnough(start, optimizeFull))
 			return null;
 		removeStaticEvents(events);
 		List<CalendarStatus> statuses = Utilities.merge(generateEventStatuses(events, start), getProposalStatuses(start.getDeficitSpheres(optimizeFull), start, true) );
 		for (int i = 0; result.size() < maxSuggestions && i < statuses.size(); i++) {
 			LinkedList<CalendarStatus> list = new LinkedList<CalendarStatus>();			
 			CalendarStatus nextMin = statuses.get(i);
 			CalendarStatus nextStatus = nextMin;
 			// best event can't improve the status
 			if (nextMin.compareTo(start) >= 0)
 				break;
 			list.add(nextMin);
 			removeEvent(nextMin);
 			//check neighbours for possible alternatives
 			while((i+1) < statuses.size()){
 				CalendarStatus next = statuses.get(i+1);
 				if(next.compareTo(start) > 0 || next.getCoefficient() > nextMin.getCoefficient()*(1+Analyser.ALTERNATIVE))
 					break;
 				nextMin.addAlternative(next);
 				removeEvent(next);
 				//take freeSlots from first proposal
 				if(!nextStatus.containsProposal() && next.containsProposal())
 					nextStatus = next;
 				++i;
 			}		
 			List<CalendarStatus> rest = getSuggestions(nextStatus, optimizeFull, maxDepth);
 			if (rest != null)
 				list.addAll(rest);
 			result.add(list);
 			restoreEvents(nextMin);
 		}
 		return result;
 	}
 
 	private List<CalendarStatus> getSuggestions(CalendarStatus currentStatus, boolean optimizeFull, int depth)
 	throws IOException {
 		if (isCloseEnough(currentStatus, optimizeFull) || (events.size() == 0 && !haveAnyProposals()) || depth <= 0)
 			return null;
 		List<CalendarStatus> statuses = Utilities.merge(generateEventStatuses(events, currentStatus), 
 				                         getProposalStatuses(currentStatus.getDeficitSpheres(optimizeFull), currentStatus, false) );
 		LinkedList<CalendarStatus> list = new LinkedList<CalendarStatus>();
 		CalendarStatus minimum = statuses.get(0);
 		CalendarStatus nextStatus = minimum;
 		if (minimum.compareTo(currentStatus) >= 0)
 			return null;
 		list.add(minimum);
 		removeEvent(minimum);
 		//check for alternatives again - run proposals??
 		//check neighbours for possible alternatives - run loop which won't modify i (we want i sets of proposals) - proposals
 		int i  = 1;
 		while(i < statuses.size()){
 			CalendarStatus next = statuses.get(i);
 			if(next.compareTo(currentStatus) > 0 || next.getCoefficient() > minimum.getCoefficient()*(1+Analyser.ALTERNATIVE))
 				break;
 			minimum.addAlternative(next);
 			removeEvent(next);
 			//take freeSlots from first proposal
 			if(!nextStatus.containsProposal() && next.containsProposal())
 				nextStatus = next;
 			++i;			
 		}
 		List<CalendarStatus> rest = getSuggestions(nextStatus, optimizeFull, depth - 1);
 		if (rest != null)
 			list.addAll(rest);
 		restoreEvents(minimum);
 		return list;
 	}
 	
 	private void removeEvent(CalendarStatus status){
 		if(status.containsProposal())
 			proposals.get( ((Proposal) status.getEvent()).getMajorSphere()).remove(status.getEvent());
 		else
 			events.remove(status.getEvent());
 	}
 	
 	private void restoreEvents(CalendarStatus status){
 		if(status.containsProposal())
 			proposals.get( ((Proposal) status.getEvent()).getMajorSphere()).add((Proposal)status.getEvent());
 		else
 			events.add(status.getEvent());
 		if (status.hasAlternatives()) {
 			for (CalendarStatus alternative : status.getAlternatives()) {
 				if(alternative.containsProposal())
 					proposals.get( ((Proposal) alternative.getEvent()).getMajorSphere()).add((Proposal)alternative.getEvent());
 				else
 					events.add(alternative.getEvent());
 			}
 		}
 	}
 
 	private boolean haveAnyProposals(){
 		List<Proposal> next;
 		for(SphereName sphere : SphereName.values()){
 			next = proposals.get(sphere);
 			if(next != null && !next.isEmpty())
 				return true;
 		}
 		return false;
 	}
 	
 	
 	/* Find slots of free time in between events */
 	private List<BaseCalendarSlot> getFreeSlots(List<? extends IEvent> events) {
 		LinkedList<BaseCalendarSlot> ret = new LinkedList<BaseCalendarSlot>();
		Collections.sort(events);
 		System.out.println("-Free slots generation-");
 		Iterator<? extends IEvent> it = events.iterator();
 		IEvent beginning = it.next();
 		it.remove();
 		IEvent curr = beginning;
 		while (it.hasNext()) {
 			IEvent next = it.next();
 			if (curr.getEndDate().compareTo(next.getStartDate()) < 0) {
 				BaseCalendarSlot newSlot = new BaseCalendarSlot("Free Slot", null, curr.getEndDate(), next.getStartDate());
 				ret.add(newSlot);
 				Pair<Double, Double> durationInterval = curr.getDurationInterval();
 				durationInterval.setSecond(Math.min(durationInterval.getSecond(), curr.getDuration() + newSlot.getDuration()));
 				curr = next;
 			}
 			else {
 				if (curr.getEndDate().compareTo(next.getEndDate()) <= 0) {
 					Pair<Double, Double> durationInterval = curr.getDurationInterval();
 					durationInterval.setSecond(curr.getDuration());
 					curr = next;
 				}
 			}
 		}
		it.remove();		
 		return ret;
 	}
 
 	/* Test if we have reached the confidence interval for spheres */
 	private boolean isCloseEnough(CalendarStatus currentStatus, boolean optimizeFull) {
 		return currentStatus.getCoefficient() < Math.pow(Analyser.CONFIDENCE, 2) 
 			|| (!optimizeFull && currentStatus.isWithinConfidenceInterval());
 	}
 	
 	private List<CalendarStatus> getProposalStatuses(List<SphereName> spheres, CalendarStatus currentStatus, boolean truncateProposals){
 		List<CalendarStatus> result = new LinkedList<CalendarStatus>();
 		PersistenceManager pmf = PMF.get().getPersistenceManager();
 		List<Proposal> cache;
 		for(SphereName sphere : spheres){
 			cache = proposals.get(sphere);
 			if(cache == null){
 				cache = new LinkedList<Proposal>();
 				Collection<Proposal> res = (Collection<Proposal>) pmf.newQuery("select from " + Proposal.class.getName() + " where majorSphere =='" + sphere+ "'").execute();
 				for(Proposal p : res){
 					cache.add(p);
 				}
 				
 				proposals.put(sphere, cache);
 			}
 			CalendarStatus next;
 			Iterator<Proposal> it = cache.iterator();
 			while(it.hasNext()){
 				next = currentStatus.checkProposal(it.next());
 				if(next != null)
 					result.add(next);
 				else if(truncateProposals)
 					it.remove();
 			}
 			Collections.sort(result);
 		}
 		return result;
 	}
 	
 	/* Create calendar statuses for all events, order them in terms of 
 	 * how well they match to targets for spheres (the coefficient of accuracy) */
 	private List<CalendarStatus> generateEventStatuses(List<? extends IEvent> events, CalendarStatus currentStatus) {
 		List<CalendarStatus> result = new LinkedList<CalendarStatus>();
 		CalendarStatus newStatus;
 		for (IEvent event : events) {
 			newStatus = new CalendarStatus(event, currentStatus);
 			result.add(newStatus);
 		}
 		Collections.sort(result);
 		return result;
 	}
 
 	/* Convert from calendar statuses to suggestions which will be passed to front end */
 	private LinkedList<List<Suggestion>> convertToSuggestions(List<List<CalendarStatus>> statuses) {
 		LinkedList<List<Suggestion>> listSuggestions = new LinkedList<List<Suggestion>>();
 		Iterator<List<CalendarStatus>> it = statuses.iterator();
 		while (it.hasNext()) {
 			listSuggestions.add(convert(it.next()));
 		}
 		return listSuggestions;
 	}
 
 	public List<Suggestion> convert(List<CalendarStatus> statuses) {
 		List<Suggestion> suggestions = new LinkedList<Suggestion>();
 		Iterator<CalendarStatus> iterator = statuses.iterator();
 		while (iterator.hasNext()) {
 			CalendarStatus status = iterator.next();
 			IEvent event = status.getEvent();
 			Double additionalTime = status.getAdditionalEventTime();
 			Suggestion result;
 			if (event.getDuration() + additionalTime == 0) {
 				result = new DeleteSuggestion(event);
 			} else {
 				Calendar end = new GregorianCalendar();
 				end.setTimeInMillis(event.getEndDate().getTimeInMillis() + (long) (additionalTime * 60000));
 				result = new RescheduleSuggestion(event, event.getStartDate(), end);
 			}
 			if(status.hasAlternatives()) {
 				result.setAlternativeSuggetions(convert(status.getAlternatives()));
 			}
 			suggestions.add(result);
 		}
 		return suggestions;
 	}
 
 	/* Remove user events which cannot be rescheduled */
 	private void removeStaticEvents(List<? extends IEvent> events) {
 		Iterator<? extends IEvent> it = events.iterator();
 		while (it.hasNext()) {
 			IEvent current = it.next();
 			if (!current.canReschedule())
 				it.remove();
 		}
 	}
 
 	/* Work out overall sphere levels considering current events. 
 	 * Create calendar status for each event */
 	public CalendarStatus checkGoals(List<? extends IEvent> events, Map<SphereName, Double> choices) throws IOException {
 		List<BaseCalendarSlot> freeSlots = getFreeSlots(events);
 		Map<SphereName, Double> times = new HashMap<SphereName, Double>();
 		initializeTimes(times, choices.keySet());
 		Map<SphereName, Double> currentRatios = new HashMap<SphereName, Double>();
 		double sum = 0;
 		for (IEvent event : events) {
 			double durationInMins = event.getDuration();
			Map<SphereName, Double> sphereResults = event.getSpheres();
 			Set<SphereName> keys = sphereResults.keySet();
 			for (SphereName key : keys) {
 				double time = Math.round(sphereResults.get(key) * durationInMins);
 				times.put(key, times.get(key) + time);
 			}
 			sum += durationInMins;
 		}
 		for (SphereName key : times.keySet()) {
 			currentRatios.put(key, times.get(key) / sum);
 		}
 		Map<SphereName, SphereInfo> sphereResults = new HashMap<SphereName, SphereInfo>();
 		for (SphereName key : times.keySet()) {
 			SphereInfo info = new SphereInfo(currentRatios.get(key), choices.get(key), times.get(key));
 			sphereResults.put(key, info);
 		}
 		return new CalendarStatus(sum, sphereResults, freeSlots);
 	}
 
 //	private void printEvents(Collection<? extends ICalendarSlot> events) {
 //		for (ICalendarSlot event : events)
 //			System.out.println(event.getTitle() + "  " + printDate(event.getStartDate()) + "  " + printDate(event.getEndDate()));
 //	}
 //
 //	private String printDate(Calendar cal) {
 //		return cal.get(Calendar.DAY_OF_MONTH) + "/" + cal.get(Calendar.MONTH) + "/" + cal.get(Calendar.YEAR) + "  " + cal.get(Calendar.HOUR_OF_DAY)
 //		+ ":" + cal.get(Calendar.MINUTE) + ":" + cal.get(Calendar.SECOND);
 //	}
 
 	private static void initializeTimes(Map<SphereName, Double> times, Set<SphereName> keys) {
 		for (SphereName key : keys)
 			times.put(key, 0.0);
 	}
 
 	public static class AlgoTest extends TestCase {
 
 		private Analyser a = new Analyser();
 		private List<? extends IEvent> events = sampleEvents();
 
 		@Test 
 		public void testConvert() {
 			/* 
 			HashMap<SphereName, SphereInfo> m = generateSpheres(new double[]{0.7,0.3, 0.0, 0.0});
 			BaseCalendarSlot event = new BaseCalendarSlot(new GregorianCalendar(2000, 3, 3, 15, 00, 0), new GregorianCalendar(2000, 3, 3, 15, 30, 0));
 			CalendarStatus c1 = new CalendarStatus(10.0, m);
 			List<CalendarStatus> statuses = new LinkedList<CalendarStatus>();
 			statuses.add(c1);
 			List<Suggestion> suggestions = a.convert(statuses);
 			boolean a = suggestions.get(0) instanceof com.appspot.analyser.RescheduleSuggestion;
 			assertTrue("Calendar statuses not converted properly", a);
 			*/
 		}
 		
 		@Test 
 		public void testGetFreeSlots() {
 			
 		}
 		
 		@Test
 		public void testRemoveStaticEvents() {
 			
 		}
 		
 		@Test
 		public void testIsCloseEnough() {
 			
 		}
 		
 		@Test
 		public void testGetSortedStatuses() {
 			
 		}
 		
 		@Test
 		public void testCheckGoals() {
 			
 		}
 
 		/* Utils */
 		private HashMap<SphereName, SphereInfo> generateSpheres(double[] values){
 			SphereName[] names = SphereName.values();
 			HashMap<SphereName, SphereInfo> res = new HashMap<SphereName, SphereInfo>();
 			for(int i = 0; i < names.length; i++) {
 				SphereInfo si = new SphereInfo(values[i], values[i], 10.0);
 				res.put(names[i], si);
 			}
 			return res;
 		}
 
 		private HashMap<SphereName, Double> generateSpheres2(double[] values){
 			SphereName[] names = SphereName.values();
 			HashMap<SphereName, Double> res = new HashMap<SphereName, Double>();
 			for(int i = 0; i < names.length; i++){
 				res.put(names[i], values[i]);
 			}
 			return res;
 		}
 
 		private List<? extends IEvent> sampleEvents() {
 			Suggestion beginning = new RescheduleSuggestion("Begin", null, 
 					new GregorianCalendar(2000, 3, 3, 0, 0, 0),new GregorianCalendar(2000, 3, 3, 0, 0, 0) );
 			beginning.setDeurationInterval(0, 0);
 			Suggestion end = new RescheduleSuggestion("End", null, 
 					new GregorianCalendar(2000, 3, 3, 23, 59, 59),new GregorianCalendar(2000, 3, 3, 23, 59, 59) );
 			end.setDeurationInterval(0, 0);
 			Suggestion s = new RescheduleSuggestion("Big health small work", null, 
 					new GregorianCalendar(2000, 3, 3, 13, 0, 0),new GregorianCalendar(2000, 3, 3, 14, 45, 0) );
 			s.setSpheres(generateSpheres2(new double[]{0.7, 0.2, 0.0, 0.0}));
 			s.setDeurationInterval(30, 120);
 			s.setReschedule(true);
 			Suggestion s2 = new RescheduleSuggestion("small health", null, 
 					new GregorianCalendar(2000, 3, 3, 15, 00, 0),new GregorianCalendar(2000, 3, 3, 15, 30, 0) );
 			s2.setSpheres(generateSpheres2(new double[]{1.0, 0.0, 0.0, 0.0}));
 			s2.setDeurationInterval(0, 60);
 			s2.setReschedule(true);
 			Suggestion s3 = new RescheduleSuggestion("small work", null, 
 					new GregorianCalendar(2000, 3, 3, 16, 30, 0),new GregorianCalendar(2000, 3, 3, 16, 40, 0) );
 			s3.setSpheres(generateSpheres2(new double[]{0.0, 1.0, 0.0, 0.0}));
 			s3.setDeurationInterval(0, 20);
 			s3.setReschedule(true);
 			List<Suggestion> list = new LinkedList<Suggestion>();
 			list.add(s);
 			list.add(end);
 			list.add(beginning);
 			list.add(s2);
 			list.add(s3);
 			return list;
 		}
 	}
 
 	public static HashMap<SphereName, Double> analyseEvents(
 		    List<Event> events, Map<SphereName, Double> currentDesiredBalance) {
 		Map<SphereName, Double> times = new HashMap<SphereName, Double>();
 		for (SphereName key : currentDesiredBalance.keySet())
 			times.put(key, 0.0);
 		HashMap<SphereName, Double> result = new HashMap<SphereName, Double>();
 		int sum = 0;
 	
 		for (IEvent event : events) {
 			double durationInMins = event.getDuration();
 			Map<SphereName, Double> sphereResults = event.getSpheres();
 			Set<SphereName> keys = sphereResults.keySet();
 			for (SphereName key : keys) {
 				double time = Math.round(sphereResults.get(key) * durationInMins);
 				times.put(key, times.get(key) + time);
 				log.severe(key.name() + ": " + (times.get(key) + time));
 			}
 			sum += durationInMins;
 		}
 
 		for (SphereName key : times.keySet()) {
 			result.put(key, times.get(key) / sum);
 		}
 	
 		return result;
     }
 }
