 package de.atlassoft.model;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 //TODO: Klasse testen
 /**
  * Represents the statistic of one single train ride, i.e. the work-off of one
  * specific {@link Schedule} object.
  * 
  * @author Alexander Balogh
  * 
  */
 public class TrainRideStatistic {
 
 	/**
 	 * Contains all stations in the order they were approached.
 	 */
 	private List<Node> stations;
 
 	/**
 	 * Contains the delay for each approached station in seconds after the
 	 * planned arrival time. If the train arrived earlier than planned delay
 	 * should be a negative number.
 	 */
 	private Map<Node, Integer> delay;
 
 	/**
 	 * The {@link ScheduleScheme} of the train, this statistic belongs to.
 	 */
 	private ScheduleScheme schedule;
 
 
 
 	/**
 	 * Create a new instance of this class. If scheduleScheme is null, an
 	 * {@link IllegalArgumentException} is thrown.
 	 * 
 	 * @param scheduleScheme
 	 *            {@link ScheduleScheme} of the train, this statistic belongs to
 	 */
 	public TrainRideStatistic(ScheduleScheme scheduleScheme) {
 		if (scheduleScheme == null) {
 			throw new IllegalArgumentException(
 					"scheduleScheme must not be null");
 		}
 
 		stations = new ArrayList<Node>();
 		delay = new HashMap<Node, Integer>();
 	}
 
 
 
 	/**
 	 * Adds a new stop to this statistic where station is the approached station
 	 * and delay is the delay that belongs to station in seconds after the
 	 * planned arrival time. If the train arrived earlier than planned delay
 	 * should be a negative number. If station is null, an
 	 * {@link IllegalArgumentException} is thrown.
 	 * 
 	 * @param station
 	 *            Station to be added
 	 * @param delay
 	 *            Delay when approached station
 	 */
 	public void addStop(Node station, int delay) {
 		if (station == null) {
 			throw new IllegalArgumentException("station must not be null");
 		}
 
 		stations.add(station);
 		this.delay.put(station, delay);
 	}
 
 	/**
 	 * Return a list containing all stations which were approached. List may be
 	 * empty but not null.
 	 * 
 	 * @return list of stations
 	 */
 	public List<Node> getStations() {
 		return stations;
 	}
 
 	/**
 	 * Returns the delay that belongs to the given stations in seconds after the
 	 * planned arrival time. If the train arrived earlier than planned the
 	 * returned value will be a negative number. If station wasn't approached,
 	 * an {@link IllegalArgumentException} is thrown.
 	 * 
 	 * @param station
 	 * @return the delay in seconds
 	 */
 	public int getDelay(Node station) {
 		Integer del = delay.get(station);
 		if (del == null) {
 			throw new IllegalArgumentException("station wasn't visited");
 		} else {
 			return del;
 		}
 	}
 
 	/**
 	 * Returns the {@link ScheduleScheme} of the train, this statistic
 	 * belongs to.
 	 * 
 	 * @return the schedule scheme
 	 */
 	public ScheduleScheme getScheduleScheme() {
 		return schedule;
 	}
 }
