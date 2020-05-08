 
package Model

 import java.util.Collections;
 
 /**
  * The Schedule object.
  * This object contains information about our schedule.
  * 
  * @author Barnabbas
  */
 public class Schedule {
 	
 	private final Set<Task> tasks;
 	private final List<Task> taskSchedule;
 	
 	/**
 	 * Constructs a new Schedule.<br>
 	 * The schedule will consists of the Tasks in {@code schedule} such that 
 	 * {@code getTask(i) == schedule.get(i)}.
 	 * When there is no Task at a certain moment, then {@code null} has to be placed there.
 	 * The length of {@code schedule} indicates the LCM for this Schedule.
 	 * 
 	 * @param schedule the schedule for the Tasks.
 	 */
 	Schedule(List<Task> schedule){
 		// using unmodifiables to guarantee immutability.
		taskSchedule = Collections.unmodifiableList(new ArrayList(schedule));
		tasks = new Collections.unmodifiableSet(new HashSet<Task>(schedule));
 	}
 	
 	/**
 	 * Gets the tasks of this Schedule.
 	 * @return a List containing the tasks of this Schedule.
 	 */
 	public Set<Task> getTasks(){
 		return tasks;
 	}
 	
 	/**
 	 * The task that runs immediatly after the given time.
 	 * Returns {@code null} when no Task will run at that moment. <br>
 	 * {@code time} must be smaller then the LCM of this Schedule.
 	 * 
 	 * 
 	 * @param time the timestamp of the moment you want the task of.
 	 * @return the Task that runs immediatly after the given timestamp or 
 	 * {@code null} if there is no such Task.
 	 * 
 	 * @throws IllegalArgumentException if {@code time} is larger then the LCM
 	 */
 	public Task getTaskAt(int time) throws IllegalArgumentException{
 		if (time >= taskSchedule.size()){
 			throw new IllegalArgumentException("The given time is not within lcm, time is " + time);
 		}
 		
 		return taskSchedule.get(time);
 	}
 	
 	/**
 	 * The length of a cycle of this Schedule.<br>
 	 * This is the least common multiplier of the periods of the Tasks.
 	 * @return Length of a cycle of this Schedule.
 	 */
 	public int getLcm(){
 		// this assumes that the given taskSchedule is valid
 		return taskSchedule.size();
 	}
 
 }
