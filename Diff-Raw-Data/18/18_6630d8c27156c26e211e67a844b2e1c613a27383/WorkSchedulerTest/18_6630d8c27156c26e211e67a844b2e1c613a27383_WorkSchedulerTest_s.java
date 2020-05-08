 package us.exultant.ahs.thread;
 
 import us.exultant.ahs.util.*;
 import us.exultant.ahs.log.*;
 import us.exultant.ahs.test.*;
 import java.util.*;
 import java.util.concurrent.*;
 
 public abstract class WorkSchedulerTest extends TestCase {
 	public WorkSchedulerTest() {
 		super(new Logger(Logger.LEVEL_DEBUG), true);
 	}
 	
 	public WorkSchedulerTest(Logger $log, boolean $enableConfirmation) {
 		super($log, $enableConfirmation);
 	}
 	
 	public List<Unit> getUnits() {
 		List<Unit> $tests = new ArrayList<Unit>();
 		$tests.add(new TestRunOnce());
 		$tests.add(new TestWtAlwaysReady());
 		$tests.add(new TestNbWt());
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
 		protected WorkScheduler $ws = makeScheduler();
 		
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
 		protected WorkScheduler $ws = makeScheduler();
 		
 		public Object call() {
 			Work[] $wt = new Work[8];
 			WorkFuture<?>[] $f = new WorkFuture<?>[8];
 			for (int $i = 0; $i < 8; $i++) $wt[$i] = new Work();
 			for (int $i = 0; $i < 8; $i++) $f[$i] = $ws.schedule($wt[$i], ScheduleParams.NOW);
 			
			X.chill(300);
			for (int $i = 0; $i < 8; $i++) X.sayet($f[$i].getState()+"");
 			
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
				X.sayet(x+"");
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
 	private class TestNbWt extends TestCase.Unit {
 		protected WorkScheduler $ws = makeScheduler();
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
 			}
 			catch (InterruptedException $e) { throw new AssertionFailed($e); }
 			catch (ExecutionException $e) { throw new AssertionFailed($e); }
 			
 			assertTrue($w1.isDone());
 			assertFalse($w1.isReady());
 			assertFalse($w2.isDone());
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
 				return (x <= 0);
 			}
 			public int getPriority() {
 				return 0;
 			}
 		}
 	}
 	
 	
 	
 	/** Same as {@link TestNbWt}, but with many more WorkTargets than threads. */
 	private class TestNbWtMany extends TestNbWt {
 		public Object call() {
 			// ...some super.call() crap, but i really need a futurepipe to do this sensibly.
 			return null;
 		}
 	}
 	
 	
 	
 	/**  */
 	private class TestScheduleFixedRate extends TestCase.Unit {
 		public Object call() {
 			breakIfFailed();
 			//TODO:AHS:THREAD
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
