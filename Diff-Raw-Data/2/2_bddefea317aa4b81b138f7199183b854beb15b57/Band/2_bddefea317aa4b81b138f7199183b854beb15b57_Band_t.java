 package assignment1;
 
 import java.math.BigDecimal;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 
 /**
  * Class that contains all the information belonging to the band. Members,
  * tracks and events can be added and removed. There are validity checks for the
  * dates and to avoid duplicate elements.
  * 
  * A history is saved in order to enable queries about the bands' past. There
  * are Getters for Events, Tracks and Members which return all Objects at either
  * a given key date or within a range. A Billing-Method has also been
 * implemented which calculates the bands revenue within a given time frame.
  * 
  * 
  * @author OOP Gruppe 187
  * 
  */
 public class Band {
 
 	// global band information
 	private String name;
 	private String genre;
 
 	// contain the current information
 	private ArrayList<Event> events;
 	private ArrayList<Member> members;
 	private ArrayList<Track> tracks;
 
 	// contain the "join dates"
 	private HashMap<Member, ArrayList<Date>> memberDates;
 	private HashMap<Track, ArrayList<Date>> trackDates;
 
 	// contain the "leave dates"
 	private HashMap<Member, ArrayList<Date>> previousMembers;
 	private HashMap<Track, ArrayList<Date>> previousTracks;
 
 	/**
 	 * Constructor which requires two arguments
 	 * 
 	 * @param name
 	 *            the name of the band
 	 * @param genre
 	 *            the genre of the band
 	 */
 	public Band(String name, String genre) {
 		super();
 		this.name = name;
 		this.genre = genre;
 
 		events = new ArrayList<Event>();
 		members = new ArrayList<Member>();
 		tracks = new ArrayList<Track>();
 
 		memberDates = new HashMap<Member, ArrayList<Date>>();
 		trackDates = new HashMap<Track, ArrayList<Date>>();
 
 		previousMembers = new HashMap<Member, ArrayList<Date>>();
 		previousTracks = new HashMap<Track, ArrayList<Date>>();
 	}
 
 	/**
 	 * 
 	 * @return ArrayList of the current members
 	 */
 	public ArrayList<Member> getMembers() {
 		return this.members;
 	}
 
 	/**
 	 * 
 	 * @return ArrayList of all events
 	 */
 	public ArrayList<Event> getEvents() {
 		return this.events;
 	}
 
 	/**
 	 * 
 	 * @return ArrayList of the current tracks
 	 */
 	public ArrayList<Track> getTracks() {
 		return this.tracks;
 	}
 
 	/**
 	 * 
 	 * @return fancy string for debugging purposes
 	 */
 	public String toString() {
 		String ret = "";
 		ret += "Band name: " + this.name;
 		ret += "\nGenre: " + this.genre;
 
 		ret += "\n\nMembers:\n";
 		for (Member m : members) {
 			ret += m.toString();
 			ret += '\n';
 		}
 
 		ret += "\nEvents:\n";
 		for (Event e : events) {
 			ret += e.toString();
 			ret += '\n';
 		}
 
 		ret += "\nTracks:\n";
 		for (Track t : tracks) {
 			ret += t.toString();
 			ret += '\n';
 		}
 
 		return ret;
 
 	}
 
 	/**
 	 * Adds a new Track to the bands repertoire
 	 * 
 	 * @param t
 	 *            the track itself
 	 * @param d
 	 *            the date the track was added
 	 * @throws InvalidDateException
 	 *             thrown if the track has already been added AND removed before
 	 *             AND the removal date is PRIOR to the new add date
 	 * @throws InvalidBandObjectException
 	 *             thrown if the track already exists
 	 */
 	public void addTrack(Track t, Date d) throws InvalidDateException,
 			InvalidBandObjectException {
 		if (!tracks.contains(t)) {
 			if (trackDates.containsKey(t)) {
 				// the track has already been added in the past - we need to add
 				// a new date
 				ArrayList<Date> history = previousTracks.get(t);
 				Date removeDate = history.get(history.size() - 1);
 				if (removeDate.after(d)) {
 					throw new InvalidDateException(
 							"new date prior to last remove date");
 				} else {
 					trackDates.get(t).add(d);
 					tracks.add(t);
 				}
 			} else {
 				ArrayList<Date> newHistory = new ArrayList<Date>();
 				newHistory.add(d);
 				trackDates.put(t, newHistory);
 				tracks.add(t);
 			}
 		} else {
 			throw new InvalidBandObjectException("track already exists");
 		}
 
 	}
 
 	/**
 	 * Removes a track from the bands repertoire
 	 * 
 	 * @param t
 	 *            the track to be removed
 	 * @param d
 	 *            the date the track was removed
 	 * @throws InvalidDateException
 	 *             thrown if the removal date is prior to the add date
 	 * @throws InvalidBandObjectException
 	 *             thrown if the track doesnt exist
 	 */
 	public void removeTrack(Track t, Date d) throws InvalidDateException,
 			InvalidBandObjectException {
 		ArrayList<Date> history = trackDates.get(t);
 		Date joinDate = history.get(history.size() - 1);
 
 		if (!tracks.contains(t)) {
 			throw new InvalidBandObjectException("track doesnt exist");
 		} else if (joinDate.after(d)) {
 			throw new InvalidDateException("new date prior to last add date");
 		} else {
 			tracks.remove(t);
 			if (previousTracks.containsKey(t)) {
 				// we need to add a new date to the history
 				previousTracks.get(t).add(d);
 			} else {
 				ArrayList<Date> newHistory = new ArrayList<Date>();
 				newHistory.add(d);
 				previousTracks.put(t, newHistory);
 			}
 		}
 	}
 
 	/**
 	 * Adds an events to the bands event-log
 	 * 
 	 * @param e
 	 *            event to be added
 	 * @throws InvalidBandObjectException
 	 *             thrown if the event already exists
 	 */
 	public void addEvent(Event e) throws InvalidBandObjectException {
 		if (!events.contains(e)) {
 			events.add(e);
 		} else {
 			throw new InvalidBandObjectException("event already exists");
 		}
 
 	}
 
 	/**
 	 * removes an event from the event-log
 	 * 
 	 * @param e
 	 *            event to be removed
 	 * @throws InvalidBandObjectException
 	 *             thrown if the event doesnt exist
 	 */
 	public void removeEvent(Event e) throws InvalidBandObjectException {
 		if (!events.contains(e)) {
 			throw new InvalidBandObjectException("event doesnt exist");
 		} else {
 			events.remove(e);
 		}
 	}
 
 	/**
 	 * Adds a member to the bands lineup
 	 * 
 	 * @param m
 	 *            member to be added
 	 * @param d
 	 *            date the member (re)-joined the band
 	 * @throws InvalidDateException
 	 *             thrown if the member has already joined AND left the band
 	 *             once AND the "leave-date" is after the new join-date
 	 * @throws InvalidBandObjectException
 	 *             thrown if the member already exists
 	 */
 	public void addMember(Member m, Date d) throws InvalidDateException,
 			InvalidBandObjectException {
 		if (!members.contains(m)) {
 			if (memberDates.containsKey(m)) {
 				// the member has already been part of the band once before
 				// date
 				ArrayList<Date> history = previousMembers.get(m);
 				Date leaveDate = history.get(history.size() - 1);
 				if (leaveDate.after(d)) {
 					throw new InvalidDateException(
 							"new date prior to last remove date");
 				} else {
 					members.add(m);
 					memberDates.get(m).add(d);
 				}
 			} else {
 				ArrayList<Date> newHistory = new ArrayList<Date>();
 				newHistory.add(d);
 				memberDates.put(m, newHistory);
 				members.add(m);
 			}
 		} else {
 			throw new InvalidBandObjectException("member already exists");
 		}
 	}
 
 	/**
 	 * removes a member from the band
 	 * 
 	 * @param m
 	 *            the member to be removed
 	 * @param d
 	 *            the date the member left the band
 	 * @throws InvalidDateException
 	 *             thrown if the "leave-date" of the member is prior to the last
 	 *             join date
 	 * @throws InvalidBandObjectException
 	 *             thrown if the member doesnt exist
 	 */
 	public void removeMember(Member m, Date d) throws InvalidDateException,
 			InvalidBandObjectException {
 		ArrayList<Date> history = memberDates.get(m);
 		Date joinDate = history.get(history.size() - 1);
 		if (!members.contains(m)) {
 			throw new InvalidBandObjectException("member doesnt exist");
 		} else if (joinDate.after(d)) {
 			throw new InvalidDateException("new date prior to last add date");
 		} else {
 			members.remove(m);
 			if (previousMembers.containsKey(m)) {
 				// the member has leave once before
 				previousMembers.get(m).add(d);
 			} else {
 				ArrayList<Date> newHistory = new ArrayList<Date>();
 				newHistory.add(d);
 				previousMembers.put(m, newHistory);
 			}
 		}
 	}
 
 	/**
 	 * returns all events within a given time period
 	 * 
 	 * @param d1
 	 *            from-date
 	 * @param d2
 	 *            to-date
 	 * @param types
 	 *            the types of events that should be returned
 	 * @return an ArrayList of all events within the given time period
 	 */
 	public ArrayList<Event> getEvents(Date d1, Date d2,
 			ArrayList<Class<? extends Event>> types)
 			throws InvalidDateException {
 		ArrayList<Event> ret = new ArrayList<Event>();
 		if (d1.after(d2)) {
 			throw new InvalidDateException("from-date AFTER to-date");
 		} else {
 			for (Event e : events) {
 				if (types.contains(e.getClass())) {
 					if (e.getTime().after(d1) && e.getTime().before(d2)) {
 						ret.add(e);
 					}
 				}
 			}
 		}
 		return ret;
 	}
 
 	/**
 	 * return the revenue of the band within a given time period
 	 * 
 	 * @param d1
 	 *            from-date
 	 * @param d2
 	 *            to-date
 	 * @return the sum of the costs of all events within the given time period
 	 */
 	public BigDecimal getBilling(Date d1, Date d2,
 			ArrayList<Class<? extends Event>> types)
 			throws InvalidDateException {
 		BigDecimal ret = new BigDecimal(0.0);
 		if (d1.after(d2)) {
 			throw new InvalidDateException("from-date AFTER to-date");
 		} else {
 			for (Event e : events) {
 				if (types.contains(e.getClass())) {
 					if (e.getTime().after(d1) && e.getTime().before(d2)) {
 						ret = ret.add(e.getFinances());
 					}
 				}
 			}
 		}
 		return ret;
 	}
 
 	/**
 	 * return the lineup of the band at a given date
 	 * 
 	 * @param d
 	 *            key date
 	 * @return an ArrayList of all members that were part of the band on the
 	 *         given day
 	 */
 	public ArrayList<Member> getMembers(Date d) {
 		ArrayList<Member> ret = new ArrayList<Member>();
 
 		for (Member m : memberDates.keySet()) {
 			Date lastValidDate = null;
 			for (Date joinDate : memberDates.get(m)) {
 				if (joinDate.before(d)) {
 					lastValidDate = joinDate;
 				}
 			}
 
 			// if he left the group, get the "leave-date" after the
 			// lastValidDate
 			if (lastValidDate != null && previousMembers.containsKey(m)) {
 				for (Date leaveDate : previousMembers.get(m)) {
 					if (leaveDate.before(d) && leaveDate.after(lastValidDate)) {
 						lastValidDate = null;
 					}
 				}
 			}
 
 			if (lastValidDate != null) {
 				ret.add(m);
 			}
 
 		}
 		return ret;
 	}
 
 	/**
 	 * return the tracks of the band at a given date
 	 * 
 	 * @param d
 	 *            key date
 	 * @return an ArrayList of all the tracks that the band was performing at
 	 *         the given date
 	 */
 	public ArrayList<Track> getTracks(Date d) {
 		ArrayList<Track> ret = new ArrayList<Track>();
 
 		for (Track t : trackDates.keySet()) {
 			Date lastValidDate = null;
 			for (Date addDate : trackDates.get(t)) {
 				if (addDate.before(d)) {
 					lastValidDate = addDate;
 				}
 			}
 
 			if (lastValidDate != null && previousTracks.containsKey(t)) {
 				for (Date removeDate : previousTracks.get(t)) {
 					if (removeDate.before(d) && removeDate.after(lastValidDate)) {
 						lastValidDate = null;
 					}
 				}
 			}
 
 			if (lastValidDate != null) {
 				ret.add(t);
 			}
 		}
 
 		return ret;
 	}
 
 }
