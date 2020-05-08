 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 public class SimulatorScheduler {
 
 	private Map<Long, List<SchedulerRegistrator>> jobs;
 	private long actual_timestamp;
 	private static SimulatorScheduler instance = new SimulatorScheduler();
 
 	private SimulatorScheduler() {
 		jobs = new HashMap<Long, List<SchedulerRegistrator>>();
 		actual_timestamp = 0;
 	}
 
 	public static SimulatorScheduler getInstance() {
 		return instance;
 	}
 
 	public long getTime(){
 		return actual_timestamp;
 	}
 	
 	public void registerEvent(Long timestamp, SchedulerRegistrator listener) throws Exception {
 		if(timestamp == actual_timestamp)
 			throw new Exception("Impossible to register event in the same moment! time:" + timestamp + " listener:" + listener);
 		System.out.println("register event - " + listener.getDescription() );
 		if (timestamp < actual_timestamp)
 			throw new Exception("Invalid time" + timestamp + " for object " + listener + " actual_time" + actual_timestamp);
 
 		if (!jobs.containsKey(timestamp))
 			jobs.put(timestamp, new ArrayList<SchedulerRegistrator>());
 
		jobs.get(timestamp).add(listener);
 	}
 	
 	public void deleteEvent(Long timestamp, SimulatorObject listener){
//		System.out.println("deleting event - time: " + timestamp + " listener: " + listener);
 		List<SchedulerRegistrator> list = jobs.get(timestamp);
 		if(list != null)
 			list.remove(listener);
 	}
 
 	public void advanceTime() throws Exception {
 		SubwayMap.getInstance().render();
 		if(jobs.isEmpty())
 			return;
 		
 		while ( !jobs.containsKey(actual_timestamp)){
 //			System.out.println("------------------------------");
 //			System.out.println("Advancing time: " + actual_timestamp);
 //			System.out.println("------------------------------");
 			actual_timestamp++;
 		}
 		
 		for (SchedulerRegistrator obj : jobs.get(actual_timestamp)){
 			System.out.println("eventing - " + obj.getDescription());
 			obj.getObject().event(actual_timestamp);			
 		}
 
 		jobs.remove(actual_timestamp);
 		actual_timestamp++;
 //		System.out.println("------------------------------");
 //		System.out.println("Advancing time: " + actual_timestamp);
 //		System.out.println("------------------------------");
 	}
 }
