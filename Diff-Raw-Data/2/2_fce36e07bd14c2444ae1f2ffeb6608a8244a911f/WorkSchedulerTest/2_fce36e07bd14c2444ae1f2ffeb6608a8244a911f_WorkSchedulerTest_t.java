 package us.exultant.ahs.thread;
 
 import us.exultant.ahs.util.*;
 import us.exultant.ahs.log.*;
 import us.exultant.ahs.test.*;
 import java.util.*;
 import java.util.concurrent.*;
 
 public abstract class WorkSchedulerTest extends TestCase {
 	public WorkSchedulerTest() {
 		super(new Logger(Logger.LEVEL_TRACE), true);
 	}
 	
 	public WorkSchedulerTest(Logger $log, boolean $enableConfirmation) {
 		super($log, $enableConfirmation);
 	}
 	
 	public List<Unit> getUnits() {
 		List<Unit> $tests = new ArrayList<Unit>();
 		$tests.add(new TestRunOnce());
 		$tests.add(new TestWtAlwaysReady());
 		$tests.add(new TestNonblockingLeaderFollower());
 		$tests.add(new TestScheduleSingleDelayMany());
 		$tests.add(new TestScheduleFixedRate());
 		$tests.add(new TestNonblockingManyWorkSingleSource());
 		return $tests;
 	}
 	
 	private static final TestData	TD	= TestData.getFreshTestData();
 	
 	protected abstract WorkScheduler makeScheduler();
 	
 	/** Helper method &mdash I want a message straight to stdout every time I throw a major exception, because there's a dangerous tendancy for Runnable to eat those if there's a mistake somewhere. */
 	protected void blow(String $msg) {
 		X.saye("BLOW: "+$msg);
 		throw new AssertionFailed($msg);
 	}
 	
 	
 	/** One runnable wrapped to be a one-shot WorkTarget. */
 	private class TestRunOnce extends TestCase.Unit {
 		private WorkScheduler $ws = makeScheduler();
 		
 		public Object call() {
 			Work $w = new Work();
 			Future<?> $f = $ws.schedule(new WorkTarget.RunnableWrapper($w, 0, true), ScheduleParams.NOW);
 			
 			try {
 				$f.get();
 			}
 			catch (InterruptedException $e) { throw new AssertionFailed($e); }
 			catch (ExecutionException $e) { throw new AssertionFailed($e); }
 			
 			assertEquals(999, $w.x);
 			return null;
 		}
 		private class Work implements Runnable {
 			public int x = 1000;
 			public void run() {
 				x--;
 			}
 		}
 	}
 	
 	
 	
 	/** Eight work targets, all always ready until they're done. */
 	private class TestWtAlwaysReady extends TestCase.Unit {
 		private WorkScheduler $ws = makeScheduler();
 		
 		public Object call() {
 			Work[] $wt = new Work[8];
 			WorkFuture<?>[] $f = new WorkFuture<?>[8];
 			for (int $i = 0; $i < 8; $i++) $wt[$i] = new Work();
 			for (int $i = 0; $i < 8; $i++) $f[$i] = $ws.schedule($wt[$i], ScheduleParams.NOW);
 			
 			//X.chill(300);
 			//for (int $i = 0; $i < 8; $i++) X.sayet($f[$i].getState()+"");
 			
 			try {
 				for (int $i = 0; $i < 8; $i++) $f[$i].get();
 			}
 			catch (InterruptedException $e) { throw new AssertionFailed($e); }
 			catch (ExecutionException $e) { throw new AssertionFailed($e); }
 			
 			for (int $i = 0; $i < 8; $i++) assertEquals(0, $wt[$i].x);
 			return null;
 		}
 		private class Work implements WorkTarget<Void> {
 			public int x = 1000;
 			public synchronized Void call() {
 				x--;
 				return null;
 			}
 			public synchronized boolean isReady() {
 				return !isDone();
 			}
 			public synchronized boolean isDone() {
 				return (x <= 0);
 			}
 			public int getPriority() {
 				return 0;
 			}
 		}
 	}
 	
 	
 	
 	/** Test two work targets, once of which must always follow the other (in other words, one is always ready, but the other changes readiness based on the progress of the first). */
 	private class TestNonblockingLeaderFollower extends TestCase.Unit {
 		private WorkScheduler $ws = makeScheduler();
 		final int HIGH = 10000;
 		final int LOW = 100;
 		
 		public Object call() {
 			WorkLeader $w1 = new WorkLeader();
 			WorkFollower $w2 = new WorkFollower();
 			$w2.$leader = $w1;
 			WorkFuture<Void> $f2 = $ws.schedule($w2, ScheduleParams.NOW);
 			$w1.$followerFuture = $f2;
 			WorkFuture<Void> $f1 = $ws.schedule($w1, ScheduleParams.NOW);
 			
 			try {
 				$f1.get();
 				$f2.get();
 			}
 			catch (InterruptedException $e) { throw new AssertionFailed($e); }
 			catch (ExecutionException $e) { throw new AssertionFailed($e); }
 			
 			assertTrue($w1.isDone());
 			assertFalse($w1.isReady());
 			assertTrue($w2.isDone());
 			assertFalse($w2.isReady());
 			assertEquals(LOW, $w2.x);
 			assertEquals(LOW, $w1.x);
 			return null;
 		}
 		private class WorkLeader implements WorkTarget<Void> {
 			public volatile WorkFuture<?> $followerFuture;
 			public volatile int x = HIGH;
 			public synchronized Void call() {
 				x--;
 				$ws.update($followerFuture);
 				return null;
 			}
 			public synchronized boolean isReady() {
 				return !isDone();
 			}
 			public synchronized boolean isDone() {
 				return (x <= LOW);
 			}
 			public int getPriority() {
 				return 0;
 			}
 		}
 		private class WorkFollower implements WorkTarget<Void> {
 			public volatile WorkLeader $leader;
 			public volatile int x = HIGH;
 			public synchronized Void call() {
 				if (!isReady()) blow("");	// not normal semantics for ready, obviously, but true for this test, since it should only be possible to flip to unready by running and one should never be scheduled for multiple runs without a check of readiness having already occurred between each run.
 				x--;
 				return null;
 			}
 			public synchronized boolean isReady() {
 				return (x > $leader.x);
 			}
 			public synchronized boolean isDone() {
 				return (x <= LOW);
 			}
 			public int getPriority() {
 				return 0;
 			}
 		}
 	}
 	
 	
 	
 	/**  */
 	private class TestScheduleSingleDelayMany extends TestCase.Unit {
 		private WorkScheduler $ws = makeScheduler();
 		public final int WTC = 8;
 		
 		public Object call() {
 			WorkFuture<?>[] $wf = Arr.newInstance(WorkFuture.class, WTC);
 			$wf[3] = $ws.schedule(new WorkTarget.RunnableWrapper(new Work(), 03, true), ScheduleParams.makeDelayed(400));
 			$wf[4] = $ws.schedule(new WorkTarget.RunnableWrapper(new Work(), -9, true), ScheduleParams.makeDelayed(500));
 			$wf[5] = $ws.schedule(new WorkTarget.RunnableWrapper(new Work(), 07, true), ScheduleParams.makeDelayed(600));
 			$wf[0] = $ws.schedule(new WorkTarget.RunnableWrapper(new Work(), 00, true), ScheduleParams.makeDelayed(100));
 			$wf[1] = $ws.schedule(new WorkTarget.RunnableWrapper(new Work(), 40, true), ScheduleParams.makeDelayed(200));
 			$wf[2] = $ws.schedule(new WorkTarget.RunnableWrapper(new Work(), 17, true), ScheduleParams.makeDelayed(300));
 			$wf[6] = $ws.schedule(new WorkTarget.RunnableWrapper(new Work(), 30, true), ScheduleParams.makeDelayed(700));
 			$wf[7] = $ws.schedule(new WorkTarget.RunnableWrapper(new Work(), -6, true), ScheduleParams.makeDelayed(800));
 			
 			try {
 				for (int $i = 1; $i < WTC; $i++) {
 					$wf[$i-1].get();
 					$log.trace("task with "+$i+"00ms delay finished");
 					assertFalse($wf[$i].isDone());
 				}
 			}
 			catch (InterruptedException $e) { throw new AssertionFailed($e); }
 			catch (ExecutionException $e) { throw new AssertionFailed($e); }
 			
 			return null;
 		}
 		private class Work implements Runnable {
 			public void run() {	// the run-once functionality is just provided by the RunnableWrapper class.
 				$log.trace("task running");
 			}
 		}
 	}
 	
 	
 	
 	/** One task with a fixed delay is scheduled to run 10 times, and is checked by another thread (at fixed delay, awkwardly, but the resolution is low enough that it's kay). */
 	private class TestScheduleFixedRate extends TestCase.Unit {
 		private WorkScheduler $ws = makeScheduler();
 		
 		public Object call() {
 			Work $wt = new Work();
 			WorkFuture<Integer> $wf = $ws.schedule($wt, ScheduleParams.makeFixedDelay(300, 100));
 			
 			X.chill(310);
 			assertEquals(9, $wt.x);
 			for (int $i = 8; $i >= 0; $i--) {
 				X.chill(100);
 				assertEquals($i, $wt.x);
 			}
 			
 			try {
 				assertEquals(0, $wf.get().intValue());
 			}
 			catch (InterruptedException $e) { throw new AssertionFailed($e); }
 			catch (ExecutionException $e) { throw new AssertionFailed($e); }
 			
 			return null;
 		}
 		private class Work implements WorkTarget<Integer> {
 			public int x = 10;
 			public synchronized Integer call() {
 				x--;
 				$log.trace("reached count "+x);
 				return x;
 			}
 			public synchronized boolean isReady() {
 				return !isDone();
 			}
 			public synchronized boolean isDone() {
 				return (x <= 0);
 			}
 			public int getPriority() {
 				return 0;
 			}
 		}
 	}
 	
 	
 	
 	/** Similar to {@link TestNonblockingLeaderFollower}, but with many more WorkTargets than threads, and all drawing from the same input pipe (which will have been closed before the draining WorkTargets start).
 	 * 
 	 * This is a very major test, since it deals with WorkTarget who have to notice their doneness and finish concurrently instead of at the completion of a normal run (probably).
 	 * 
 	 * This also tests (if indirectly) consistent results from WorkTarget that receive concurrent finishes (but I'd recommend running it numerous times if you want to feel confident of that).
 	 */
 	private class TestNonblockingManyWorkSingleSource extends TestCase.Unit {
 		private WorkScheduler $ws = makeScheduler();
 		public final int HIGH = 10000;
 		public final int WTC = 32;
 		
 		private final Pipe<Integer> $pipe = new Pipe<Integer>();
 		protected final Work[] $wt = new Work[WTC];
 		@SuppressWarnings("unchecked")	// impossible to not suck in java.
 		protected final WorkFuture<Integer>[] $wf = Arr.newInstance(WorkFuture.class, WTC);
 		
 		public Object call() {
 			feedPipe();
 
 			$log.trace("creating work targets");
 			for (int $i = 0; $i < WTC; $i++)
 				$wt[$i] = new Work();
 			$log.trace("scheduling work targets");
 			for (int $i = 0; $i < WTC; $i++)
 				$wf[$i] = $ws.schedule($wt[$i], ScheduleParams.NOW);
 			
 			$log.trace("waiting for work future completion");
 			try {
 				boolean $wonOnce = false;
 				for (int $i = 0; $i < WTC; $i++) {
 					int $ans = $wf[$i].get();
 					if ($ans == HIGH)
 						if ($wonOnce)
 							throw new AssertionFailed("More than one WorkTarget finished with the high value.");
 						else $wonOnce = true;
 				}
 				if (!$wonOnce) throw new AssertionFailed("No WorkTarget finished with the high value.");
 			}
 			catch (InterruptedException $e) { throw new AssertionFailed($e); }
 			catch (ExecutionException $e) { throw new AssertionFailed($e); }
 			
 			return null;
 		}
 		private class Work implements WorkTarget<Integer> {
 			public synchronized Integer call() {
 				Integer $move = $pipe.SRC.readNow();
 				$log.trace("pulled "+$move);
 				return $move;
 			}
 			public synchronized boolean isReady() {
 				return !isDone();
 			}
 			public synchronized boolean isDone() {
 				return !$pipe.SRC.hasNext();
 			}
 			public int getPriority() {
 				return 0;
 			}
 		}
 		protected void feedPipe() {
 			$log.trace("feed started");
			for (int $i = 1; $i <= HIGH; $i++)
 				$pipe.SINK.write($i);
 			$log.trace("feed complete");
 			$pipe.SINK.close();
 			$log.trace("feed closed");
 		}
 	}
 	
 	
 	
 	/** Same as {@link TestNonblockingManyWorkSingleSource}, but the input pipe will be closed from the sink thread when the source is already empty (resulting in a (probably) concurrent finish for the WorkTargets). */
 	private class TestConcurrentFinish extends TestNonblockingManyWorkSingleSource {
 		public Object call() {
 			// the earlier test didn't actually need to set the pipe listener because all the writes were done before any reading started, and so all of the work was always ready as long as it wasn't done.  now we're in an entirely different situation.
 //			$pipe.SRC.setListener(new Listener<ReadHead<Integer>>() {
 //				public void hear(ReadHead<Integer> $rh) {
 //					
 //				}
 //			});
 			
 			breakIfFailed();
 			return null;
 		}
 		
 		protected void feedPipe() {
 			
 		}
 	}
 	
 	
 	
 	/**  */
 	private class TestPrioritizedDuo extends TestCase.Unit {
 		public Object call() {
 			breakIfFailed();
 			return null;
 		}
 	}
 	
 	
 	
 	/**  */
 	private class TestBasic extends TestCase.Unit {
 		public Object call() {
 			breakIfFailed();
 			return null;
 		}
 	}
 }
