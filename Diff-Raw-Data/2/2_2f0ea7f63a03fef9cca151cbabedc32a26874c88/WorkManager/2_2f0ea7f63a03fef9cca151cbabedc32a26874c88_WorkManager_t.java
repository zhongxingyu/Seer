 package us.exultant.ahs.thread;
 
 import us.exultant.ahs.core.*;
 import java.util.concurrent.*;
 
 public class WorkManager {
 	public static Future<Void> scheduleOnePerCore(Factory<WorkTarget<?>> $wtf) {
 		return scheduleOnePerCore($wtf, getDefaultScheduler());
 	}
 	public static Future<Void> scheduleOnePerCore(final Factory<WorkTarget<?>> $wtf, final WorkScheduler $ws) {
 		final int $n = Runtime.getRuntime().availableProcessors();
 		WorkFuture<?>[] $fa = new WorkFuture<?>[$n]; 
 		for (int $i = 0; $i < $n; $i++)
 			$fa[$i] = $ws.schedule($wtf.make(), ScheduleParams.NOW);	// i assume it wouldn't often make sense to schedule the same task at the same time on multiple cores if it's clock-based
 		return null;	//FIXME:AHS:THREAD: return an aggregate future
 	}
 	
 	
 	
 	// factory methods for wrappers can also go in this class.
 	
 	
 	
 	
 	
 	//PLAN:AHS:THREAD: i'm really not sure what i'm going to do about defaults for this stuff.
 	//	core problem 1: not every program might want the same kind of WorkScheduler.  And while they can start their own, then we start spawning more threads than cores.  Not the end of the world (especially if the entire application ignores the default one, since then those threads are never run anyway), but less than ideal.
 	//	core problem 2: some programs might want to tweak performance settings -- changing the balance of thread allocation between priority bins, for example.  And those changes being global?  Urgh.
 	/**
 	 * <p>
 	 * Gets a "default" WorkScheduler that is a singleton to the VM.
 	 * </p>
 	 * 
 	 * <p>
 	 * This method is performs lazy instantiation, is thread-safe, and the returned
 	 * WorkScheduler is already started with its own complement of worker threads when
 	 * returned.
 	 * </p>
 	 * 
 	 * @return the single default WorkScheduler for this VM.
 	 */
 	public static WorkScheduler getDefaultScheduler() {
 		return SingletonHolder.INSTANCE;
 	}
 
 	private static class SingletonHolder {
		public static final WorkScheduler INSTANCE = new WorkSchedulerFlexiblePriority(Math.max(4, Runtime.getRuntime().availableProcessors()));
 	}
 }
