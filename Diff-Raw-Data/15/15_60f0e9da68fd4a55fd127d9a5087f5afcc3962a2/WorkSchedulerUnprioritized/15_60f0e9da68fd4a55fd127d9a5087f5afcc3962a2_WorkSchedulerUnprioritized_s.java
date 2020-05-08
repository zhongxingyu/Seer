 package us.exultant.ahs.thread;
 
 import java.util.*;
 import java.util.concurrent.*;
 
 public class WorkSchedulerUnprioritized implements WorkScheduler {
 	public WorkSchedulerUnprioritized() {
 		this.$waiting = new HashSet<WorkTarget<?>>();
 		this.$fuck = new HashMap<Object,WorkTarget<?>>();
 		this.$seshat = new Seshat();
 	}
 	
 	private final Set<WorkTarget<?>>		$waiting;
 	private final Map<Object,WorkTarget<?>>		$fuck;
 	private final ScheduledThreadPoolExecutor	$seshat;
 	
 	public <$V> Future<$V> schedule(WorkTarget<$V> $wt) {
 		if ($wt.isDone()) return null;
 		synchronized ($seshat) {
 			$waiting.add($wt);
			return doUpdate($wt);
 		}
 	}
 	
 	public <$V> Future<$V> schedule(WorkTarget<$V> $wt, long $delay, TimeUnit $unit) {
 		if ($wt.isDone()) return null;
 		return $seshat.schedule($wt, $delay, $unit);
 	}
 	
 	public <$V> Future<$V> scheduleAtFixedRate(WorkTarget<$V> $wt, long $initialDelay, long $delay, TimeUnit $unit) {
 		if ($wt.isDone()) return null;
		return $seshat.scheduleAtFixedRate(new FutureTask<$V>($wt), $initialDelay, $delay, $unit);
 	}
 	
 	public <$V> Future<$V> scheduleWithFixedDelay(WorkTarget<$V> $wt, long $initialDelay, long $delay, TimeUnit $unit) {
 		if ($wt.isDone()) return null;
		return $seshat.scheduleWithFixedDelay(new FutureTask<$V>($wt), $initialDelay, $delay, $unit);
 	}
 	
 	public <$V> void update(WorkTarget<$V> $wt) {
 		doUpdate($wt);
 	}
 	
 	
 	
 	private <$V> Future<$V> doUpdate(WorkTarget<$V> $wt) {
 		synchronized ($seshat) {
 			// if the WorkTarget<$V> isn't ready, we don't care.
 			//	even if we'd previously put it in the priority queue but it's not ready now, we leave it there because that datastructure isn't great at arbitrary removes, and we're fine waiting for it to bubble out and be dismissed eventually.
			if (!$wt.isReady()) return null;
 			
 			if ($waiting.remove($wt)) {
 				Future<$V> $fu = $seshat.submit($wt);
 				remember($wt,$fu);
 				return $fu;
 			}
 			return null;
 		}
 	}
 	
 	private void remember(WorkTarget<?> $wt, Future<?> $key) {
 		$fuck.put($key, $wt);
 	}
 	private WorkTarget<?> remember(Runnable $r) {
 		return $fuck.get($r);
 	}
 	
 	
 	
 	private class Seshat extends ScheduledThreadPoolExecutor {
 		public Seshat() {
 			super(Math.min(4, Runtime.getRuntime().availableProcessors()));
 		}
 		public Seshat(int $corePoolSize) {
 			super($corePoolSize);
 		}
 		
 		protected void afterExecute(Runnable r, Throwable t) {
 			super.afterExecute(r, t);
 			// this Runnable r turns out to be ScheduledThreadPoolExecutor.ScheduledFutureTask<V> extends FutureTask<V> implements RunnableScheduledFuture<V>... which is too much of a dick to give us any exposure back to what we submitted, making it a serious pain in the ass to implement anything useful in this method without resorting to an utterly ridiculous hashmap.
 			synchronized ($seshat) {
 				WorkTarget<?> $wt = $fuck.get(r);
 				// wt can be null if it's a clock-oriented task, because we don't deal with rescheduling those ourselves in this implementation (the STPE keeps that for us).
 				if ($wt != null) WorkSchedulerUnprioritized.this.schedule($wt);
 			}
 		}
 	}
 }
