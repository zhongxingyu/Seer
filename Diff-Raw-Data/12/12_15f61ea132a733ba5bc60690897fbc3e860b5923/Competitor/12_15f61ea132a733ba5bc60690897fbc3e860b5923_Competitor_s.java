 package members;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * @author Philip & Andrée
  * 
  */
 public class Competitor implements Comparable<Competitor> {
 
 	private int index;
 	private List<Time> startTimes;
 	private List<Time> lapMoments;
 	private List<Time> finishTimes;
 	private String name;
 	
 	public List<Lap> getLaps() {
 		List<Lap> laps = new ArrayList<Lap>();
 		if (startTimes.isEmpty() || finishTimes.isEmpty())
 			return laps;
 		
 		if (lapMoments.isEmpty()) {
 			laps.add(new Lap(startTimes.get(0),finishTimes.get(0)));
 		} else {
 			for (int i = 0; i < lapMoments.size(); i++) {
 				if (i == 0) 
 					laps.add(new Lap(startTimes.get(0), lapMoments.get(0)));
 				else
 					laps.add(new Lap(lapMoments.get(i - 1), lapMoments.get(i)));
 			}
 			
 			laps.add(new Lap(lapMoments.get(lapMoments.size() - 1), finishTimes.get(0)));
 		}
 		
 		return laps;
 	}
 	
 	/**
 	 * @param index		index of the competitor
 	 */
 	public Competitor(int index) {
 		name= "";
 		this.index = index;
 		startTimes = new ArrayList<Time>();
 		finishTimes = new ArrayList<Time>();
 	}
 
 	/**
 	 * Adds start time to list of start times.
 	 * 
 	 * @param t		the start time to add
 	 */
 	public void addStartTime(Time t) {
 		startTimes.add(t);
 	}
 
 	/**
 	 * Return lists of start times.
 	 * 
 	 * @return the list of start times
 	 */
 	public List<Time> getStartTimes() {
 		return startTimes;
 	}
 	
 	public List<Time> getLapMoments() {
 		return lapMoments;
 	}
 	
 	public void addLapMoment(Time t) {
 		lapMoments.add(t);
 	}
 
 	/**
 	 * Adds finish time to list of finish times.
 	 * 
 	 * @param t		the finish time to add
 	 */
 	public void addFinishTime(Time t) {
 		finishTimes.add(t);
 	}
 	
 	/**
 	 * Adds a name to the competitor
 	 * @param name
 	 */
 	public void addName(String name) {
 		this.name=name;
 	}
 
 	/**
 	 * Return the list of finish times.
 	 * 
 	 * @return the list of finish times
 	 */
 	public List<Time> getFinishTimes() {
 		return finishTimes;
 	}
 
 	/**
 	 * Return the index.
 	 * 
 	 * @return	the index
 	 */
 	public int getIndex() {
 		return index;
 	}
 	
 	public String getName() {
 		return name;
 	}
 	
 	public String toString() {
 		throw new UnsupportedOperationException("Use CompetitorPrinter plz.");
 	}
 	
 	/**
 	 * Compares the total time of this competitor with the competitor o
 	 * @param o		The competitor to compare with.
 	 * @return 		1 if this competitors total time is less than o's
 	 * 				0 if the total times are equal
 	 * 				-1 if this competitors total time is larger than o's
 	 */
 	@Override
 	public int compareTo(Competitor comp) {
 		if(comp.getStartTimes().isEmpty() || comp.getFinishTimes().isEmpty()) {
 			return 1;
 		} else if (startTimes.isEmpty() || finishTimes.isEmpty()) {
 			return -1;
 		}
 		
 		Time totalTime = startTimes.get(0).difference(finishTimes.get(0));
 		Time totalTime2 = comp.getStartTimes().get(0).difference(comp.getFinishTimes().get(0));
 		
 		return totalTime.compareTo(totalTime2);
 	}
 
 }
