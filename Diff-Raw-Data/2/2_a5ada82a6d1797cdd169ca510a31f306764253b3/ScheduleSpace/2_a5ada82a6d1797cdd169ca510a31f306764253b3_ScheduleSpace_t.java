 package local.radioschedulers;
 
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeMap;
 import java.util.AbstractMap.SimpleEntry;
 import java.util.Map.Entry;
 
 /**
  * A schedule space is a timeline that, for each time slot, defines a list of
  * JobCombinations that could be executed.
  * 
  * @author Johannes Buchner
  */
 public class ScheduleSpace implements Iterable<Entry<LSTTime, Set<JobCombination>>> {
 	public static final int LST_SLOTS_MINUTES = 15;
 	public static final int LST_SLOTS_PER_DAY = 60 * 24 / LST_SLOTS_MINUTES;
 
 	private Map<LSTTime, Set<JobCombination>> schedule = new TreeMap<LSTTime, Set<JobCombination>>();
 
 	private void createIfNeeded(LSTTime t) {
 		if (!schedule.containsKey(t)) {
 			schedule.put(t, new HashSet<JobCombination>());
 		}
 	}
 
 	public void clear(LSTTime t) {
 		schedule.remove(t);
 	}
 
 	public void add(LSTTime t, JobCombination jc) {
 		createIfNeeded(t);
 		schedule.get(t).add(jc);
 	}
 
 	public boolean isEmpty(LSTTime t) {
 		if (schedule.containsKey(t))
 			return schedule.get(t).isEmpty();
 		else
 			return true;
 	}
 
 	public Set<JobCombination> get(LSTTime t) {
 		createIfNeeded(t);
 		return schedule.get(t);
 	}
 
 	public LSTTime findLastEntry() {
 		return Collections.max(schedule.keySet());
 	}
 	
 	public Map<LSTTime, Set<JobCombination>> getSchedule() {
 		return schedule;
 	}
 	
 	public void setSchedule(Map<LSTTime, Set<JobCombination>> schedule) {
 		this.schedule = schedule;
 	}
 
 	@Override
 	public Iterator<Entry<LSTTime, Set<JobCombination>>> iterator() {
 		return new Iterator<Entry<LSTTime, Set<JobCombination>>>() {
 			LSTTime t = new LSTTime(0L, 0L);
 			LSTTime lastEntry = findLastEntry();
 
 			@Override
 			public boolean hasNext() {
				if (t.compareTo(lastEntry) <= 0)
 					return true;
 				else
 					return false;
 			}
 
 			@Override
 			public Entry<LSTTime, Set<JobCombination>> next() {
 				Set<JobCombination> jc = get(new LSTTime(t.day, t.minute));
 				Entry<LSTTime, Set<JobCombination>> entry = new SimpleEntry<LSTTime, Set<JobCombination>>(
 						new LSTTime(t.day, t.minute), jc);
 				t.minute += LST_SLOTS_MINUTES;
 
 				if (t.minute >= 24 * 60) {
 					t.day++;
 					t.minute = 0L;
 				}
 				return entry;
 			}
 
 			@Override
 			public void remove() {
 				throw new Error("Not implemented.");
 			}
 		};
 	}
 }
