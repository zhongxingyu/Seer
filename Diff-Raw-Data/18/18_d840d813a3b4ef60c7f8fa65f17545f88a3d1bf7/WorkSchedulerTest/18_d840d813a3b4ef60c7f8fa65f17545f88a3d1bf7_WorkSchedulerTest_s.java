 /*
  * Copyright 2010, 2011 Eric Myhre <http://exultant.us>
  * 
  * This file is part of AHSlib.
  *
  * AHSlib is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, version 3 of the License, or
  * (at the original copyright holder's option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package us.exultant.ahs.thread;
 
 import us.exultant.ahs.core.*;
 import us.exultant.ahs.util.*;
 import us.exultant.ahs.log.*;
 import us.exultant.ahs.test.*;
 import java.util.*;
 import java.util.concurrent.*;
 import java.util.concurrent.atomic.*;
 
 /**
  * A useful statement is this:
  * <tt>
 * i=0; while true; do i=$(math $i + 1); echo $i; java us.exultant.ahs.thread.WorkSchedulerFlexiblePriorityTest 2> lol; if [ "$?" -ne "0" ]; then break; fi; done
  * </tt>
  * 
  * @author hash
  * 
  */
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
 		$tests.add(new TestCompletionPreSubscribe());
 		$tests.add(new TestCompletionPostSubscribe());
 		$tests.add(new TestWtAlwaysReady());
 		$tests.add(new TestNonblockingLeaderFollower());
 		$tests.add(new TestScheduleSingleDelayMany());
 		$tests.add(new TestScheduleFixedRate());
 		$tests.add(new TestNonblockingManyWorkSingleSource());
 		$tests.add(new TestConcurrentFinish());
 		$tests.add(new TestPrioritizedDuo());
 		return $tests;
 	}
 	
 	private static final TestData	TD	= TestData.getFreshTestData();
 	
 	/** If this is a positive integer, we want that many threads.  A zero means that you can use your default (presumably threads=cores). */
 	protected abstract WorkScheduler makeScheduler(int $threads);
 	
 	/** Helper method &mdash I want a message straight to stdout every time I throw a major exception, because there's a dangerous tendancy for Runnable to eat those if there's a mistake somewhere. */
 	protected void blow(String $msg) {
 		X.saye("BLOW: "+$msg);
 		throw new AssertionFailed($msg);
 	}
 	
 	
 	/** One runnable wrapped to be a one-shot WorkTarget. */
 	private class TestRunOnce extends TestCase.Unit {
 		private WorkScheduler $ws = makeScheduler(0).start();
 		
 		public Object call() throws InterruptedException, ExecutionException {
 			Work $w = new Work();
 			Future<?> $f = $ws.schedule(new WorkTarget.RunnableWrapper($w, 0, true), ScheduleParams.NOW);
 			
 			$f.get();
 			
 			assertEquals(999, $w.x);
 			
 			breakCaseIfFailed();
 			return null;
 		}
 		private class Work implements Runnable {
 			public int x = 1000;
 			public void run() {
 				x--;
 			}
 		}
 	}
 	
 	
 	/** Tests for a single firing of a completion listener that's assigned to a one-shot WorkTarget before the scheduler is launched. */
 	private class TestCompletionPreSubscribe extends TestCase.Unit {
 		private WorkScheduler $ws = makeScheduler(0);
 		
 		public Object call() throws InterruptedException, ExecutionException {
 			final AtomicInteger $completionCalls = new AtomicInteger(0);
 			final Work $wt = new Work();
 			final WorkFuture<Void> $wf = $ws.schedule(new WorkTarget.RunnableWrapper($wt, 0, true), ScheduleParams.NOW);
 			
 			$wf.addCompletionListener(new Listener<WorkFuture<Void>>() {
 				public void hear(WorkFuture<Void> $lol) {
 					// demand an immediate response
 					$completionCalls.incrementAndGet();
 					try {
 						$wf.get(0, TimeUnit.NANOSECONDS);
 					}
 					catch (InterruptedException $e) { throw new AssertionFailed($e); }
 					catch (ExecutionException $e) { throw new AssertionFailed($e); }
 					catch (TimeoutException $e) { throw new AssertionFailed($e); }
 				}
 			});
 			$ws.start();
 			
 			$wf.get();
 			
 			X.chill(15);
 			assertEquals(1, $completionCalls.intValue());
 			
 			breakCaseIfFailed();
 			return null;
 		}
 		private class Work implements Runnable { public void run() {} }
 	}
 	
 	
 	/** Tests for a single firing of a completion listener that's assigned to a one-shot WorkTarget after it's been completed. */
 	private class TestCompletionPostSubscribe extends TestCase.Unit {
 		private WorkScheduler $ws = makeScheduler(0).start();
 		
 		public Object call() throws InterruptedException, ExecutionException {
 			final AtomicInteger $completionCalls = new AtomicInteger(0);
 			final Work $wt = new Work();
 			final WorkFuture<Void> $wf = $ws.schedule(new WorkTarget.RunnableWrapper($wt, 0, true), ScheduleParams.NOW);
 			
 			$wf.get();
 			
 			$wf.addCompletionListener(new Listener<WorkFuture<Void>>() {
 				public void hear(WorkFuture<Void> $lol) {
 					// demand an immediate response
 					$completionCalls.incrementAndGet();
 					try {
 						$wf.get(0, TimeUnit.NANOSECONDS);
 					}
 					catch (InterruptedException $e) { throw new AssertionFailed($e); }
 					catch (ExecutionException $e) { throw new AssertionFailed($e); }
 					catch (TimeoutException $e) { throw new AssertionFailed($e); }
 				}
 			});
 			
 			assertEquals(1, $completionCalls.intValue());
 			
 			breakCaseIfFailed();
 			return null;
 		}
 		private class Work implements Runnable { public void run() {} }
 	}
 	
 	
 	
 	/** Eight work targets, all always ready until they're done.
 	 *  Also performs completion signaling test. */
 	private class TestWtAlwaysReady extends TestCase.Unit {
 		private WorkScheduler $ws = makeScheduler(0).start();
 		
 		public Object call() throws InterruptedException, ExecutionException {
 			final AtomicInteger $completionCalls = new AtomicInteger(0);
 			Work[] $wt = new Work[8];
 			@SuppressWarnings("unchecked")	//srsly.
 			WorkFuture<Void>[] $f = new WorkFuture[8];
 			for (int $i = 0; $i < 8; $i++) $wt[$i] = new Work();
 			for (int $i = 0; $i < 8; $i++) $f[$i] = $ws.schedule($wt[$i], ScheduleParams.NOW);
 			for (int $i = 0; $i < 8; $i++) $f[$i].addCompletionListener(new Listener<WorkFuture<Void>>() {
 				public void hear(WorkFuture<Void> $lol) {
 					$completionCalls.incrementAndGet();
 				}
 			});
 			
 			for (int $i = 0; $i < 8; $i++) $f[$i].get();
 			
 			for (int $i = 0; $i < 8; $i++) assertEquals(0, $wt[$i].x);
 			
 			// so, funny thing about this next.  um, completion calls come AFTER that future.get guy is capable of returning.  not "much" after, of course, but still after.  so... yeah, this is kinda hard to test.  like, it may fail.  without the system being wrong.  i need a better test here.
 			X.chill(10);
 			assertEquals(8, $completionCalls.intValue());
 			
 			breakCaseIfFailed();
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
 	public class TestNonblockingLeaderFollower extends TestCase.Unit {
 		private WorkScheduler $ws = makeScheduler(0).start();
 		final int HIGH = 10000;
 		final int LOW = 100;
 		
 		public Object call() throws InterruptedException, ExecutionException {
 			WorkLeader $w1 = new WorkLeader();
 			WorkFollower $w2 = new WorkFollower();
 			$w2.$leader = $w1;
 			WorkFuture<Void> $f2 = $ws.schedule($w2, ScheduleParams.NOW);
 			$w1.$followerFuture = $f2;
 			WorkFuture<Void> $f1 = $ws.schedule($w1, ScheduleParams.NOW);
 			
 			$f1.get();
 			$f2.get();
 			
 			assertTrue($w1.isDone());
 			assertFalse($w1.isReady());
 			assertTrue($w2.isDone());
 			assertFalse($w2.isReady());
 			assertEquals(LOW, $w2.x);
 			assertEquals(LOW, $w1.x);
 			
 			breakCaseIfFailed();
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
 			public String toString() {
 				return "TestNonblockingLeaderFollower-WorkLeader";
 			}
 		}
 		public class WorkFollower implements WorkTarget<Void> {
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
 			public String toString() {
 				return "TestNonblockingLeaderFollower-WorkFollower";
 			}
 		}
 	}
 	
 	
 	// TestCancelWhileRunning
 	
 	// TestFinishWhileRunning	// I mean, come on.  you can make a much more direct test of this than what TestConcurrentFinish is doing, and you can (and should) do it without pipes.
 	
 	
 	
 	/**  */
 	private class TestScheduleSingleDelayMany extends TestCase.Unit {
 		private WorkScheduler $ws = makeScheduler(0);
 		public final int WTC = 8;
 		
 		public Object call() throws InterruptedException, ExecutionException {
 			WorkFuture<?>[] $wf = Arr.newInstance(WorkFuture.class, WTC);
 			$wf[3] = $ws.schedule(new WorkTarget.RunnableWrapper(new Work(), 03, true), ScheduleParams.makeDelayed(400));
 			$wf[4] = $ws.schedule(new WorkTarget.RunnableWrapper(new Work(), -9, true), ScheduleParams.makeDelayed(500));
 			$wf[5] = $ws.schedule(new WorkTarget.RunnableWrapper(new Work(), 07, true), ScheduleParams.makeDelayed(600));
 			$wf[0] = $ws.schedule(new WorkTarget.RunnableWrapper(new Work(), 00, true), ScheduleParams.makeDelayed(100));
 			$wf[1] = $ws.schedule(new WorkTarget.RunnableWrapper(new Work(), 40, true), ScheduleParams.makeDelayed(200));
 			$wf[2] = $ws.schedule(new WorkTarget.RunnableWrapper(new Work(), 17, true), ScheduleParams.makeDelayed(300));
 			$wf[6] = $ws.schedule(new WorkTarget.RunnableWrapper(new Work(), 30, true), ScheduleParams.makeDelayed(700));
 			$wf[7] = $ws.schedule(new WorkTarget.RunnableWrapper(new Work(), -6, true), ScheduleParams.makeDelayed(800));
 			$ws.start();
 			
 			for (int $i = 1; $i < WTC; $i++) {
 				$wf[$i-1].get();
 				$log.trace(this, "task with "+$i+"00ms delay finished");
 				assertFalse($wf[$i].isDone());
 			}
 			
 			breakCaseIfFailed();
 			return null;
 		}
 		private class Work implements Runnable {
 			public void run() {	// the run-once functionality is just provided by the RunnableWrapper class.
 				$log.trace(this, "task running");
 			}
 		}
 	}
 	
 	
 	
 	/** One task with a fixed delay is scheduled to run 10 times, and is checked by another thread (at fixed delay, awkwardly, but the resolution is low enough that it's kay). */
 	private class TestScheduleFixedRate extends TestCase.Unit {
 		private WorkScheduler $ws = makeScheduler(0).start();
 		
 		public Object call() throws InterruptedException, ExecutionException {
 			Work $wt = new Work();
 			WorkFuture<Integer> $wf = $ws.schedule($wt, ScheduleParams.makeFixedDelay(300, 100));
 			
 			X.chill(310);
 			assertEquals(9, $wt.x);
 			for (int $i = 8; $i >= 0; $i--) {
 				X.chill(100);
 				assertEquals($i, $wt.x);
 			}
 			assertEquals(0, $wf.get().intValue());
 			
 			breakCaseIfFailed();
 			return null;
 		}
 		private class Work implements WorkTarget<Integer> {
 			public int x = 10;
 			public synchronized Integer call() {
 				x--;
 				$log.trace(this, "reached count "+x);
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
 	 * This also tests (if indirectly) consistent results from WorkTarget that receive concurrent finishes (but I'd recommend running it numerous times if you want to feel confident of that.  And by numerous times, I mean many thousands of times.).
 	 */
 	private class TestNonblockingManyWorkSingleSource extends TestCase.Unit {
 		protected WorkScheduler $ws = makeScheduler(0).start();
 		public final int HIGH = 1000;
 		public final int WTC = 32;
 		
 		protected final Pipe<Integer> $pipe = new Pipe<Integer>();
 		protected final Work[] $wt = new Work[WTC];
 		@SuppressWarnings("unchecked")	// impossible to not suck in java.
 		protected final WorkFuture<Integer>[] $wf = Arr.newInstance(WorkFuture.class, WTC);
 		
 		public Object call() throws InterruptedException, ExecutionException {
 			feedPipe();
 
 			$log.trace(this, "creating work targets");
 			for (int $i = 0; $i < WTC; $i++)
 				$wt[$i] = new Work($i);
 			$log.trace(this, "scheduling work targets");
 			for (int $i = 0; $i < WTC; $i++)
 				$wf[$i] = $ws.schedule($wt[$i], ScheduleParams.NOW);
 			
 			configurePipe();
 			
 			$log.trace(this, "waiting for work future completion");
 			boolean $wonOnce = false;
 			for (int $i = 0; $i < WTC; $i++) {
 				Integer $ans = $wf[$i].get();
 				$log.debug(this, "final result of work target "+$i+": "+$ans);
 				if ($ans != null && $ans == HIGH) {
 					assertFalse("No more than one WorkTarget finished with the high value.", $wonOnce);
 					$wonOnce = true;
 				}
 			}
 			assertTrue("Exactly one WorkTarget finished with the high value.", $wonOnce);
 			
 			breakCaseIfFailed();
 			return null;
 		}
 		private class Work implements WorkTarget<Integer> {
 			public Work(int $name) { this.$name = $name; }
 			private final int $name;
 			public synchronized Integer call() {
 				Integer $move = $pipe.SRC.readNow();
 				$log.trace(this, "WT"+$name+" pulled "+$move);
 				return $move;
 			}
			public synchronized boolean isReady() {
				return !isDone();
 			}
			public synchronized boolean isDone() {
 				return $pipe.SRC.isClosed() && !$pipe.SRC.hasNext();
 			}
 			public int getPriority() {
 				return 0;
 			}
 			public String toString() {
 				return us.exultant.ahs.codec.eon.Eon.getKlass(this)+":[WT:"+$name+";];";
 			}
 		}
 		protected void feedPipe() {
 			$log.trace(this, "feed started");
 			for (int $i = 1; $i <= HIGH; $i++)
 				$pipe.SINK.write($i);
 			$log.trace(this, "feed complete");
 			$pipe.SINK.close();
 			$log.trace(this, "feed closed");
 		}
 		protected void configurePipe() {}
 	}
 	
 	
 	
 	/** Same as {@link TestNonblockingManyWorkSingleSource}, but the input pipe will be closed from the sink thread when the source is already empty (resulting in a (probably) concurrent finish for the WorkTargets). */
 	private class TestConcurrentFinish extends TestNonblockingManyWorkSingleSource {
 		protected void feedPipe() {
 			$ws.schedule(new WorkTarget.RunnableWrapper(new Runnable() { public void run() { TestConcurrentFinish.super.feedPipe(); } }), ScheduleParams.NOW);	// that was an incredibly satisfying line to write
 		}
 		
 		protected void configurePipe() {
 			// the earlier test didn't actually need to set the pipe listener because all the writes were done before any reading started, and so all of the work was always ready as long as it wasn't done.  now we're in an entirely different situation.
 			$pipe.SRC.setListener(new Listener<ReadHead<Integer>>() {
 				public void hear(ReadHead<Integer> $rh) {
 					for (WorkFuture<Integer> $x : $wf)
 						$ws.update($x);
 				}
 			});
 		}
 	}
 	
 	
 	
 	/** Test that when two tasks of different priority are scheduled, the higher priority goes first. */
 	private class TestPrioritizedDuo extends TestCase.Unit {
 		private WorkScheduler $ws = makeScheduler(1).start();
 		
 		public Object call() throws InterruptedException, ExecutionException {
 			WorkFuture<Void> $wf_high = $ws.schedule(new WorkTarget.RunnableWrapper(new Work(), 90000, true), ScheduleParams.NOW);
 			WorkFuture<Void> $wf_low = $ws.schedule(new WorkTarget.RunnableWrapper(new Work(), 10, true), ScheduleParams.NOW);
 			$ws.start();
 			
 			$wf_high.get();
 			X.chill(10);
 			assertFalse($wf_low.isDone());
 			$wf_low.get();
 			
 			return null;
 		}
 		
 		private class Work implements Runnable { public void run() { X.chill(100); } }
 	}
 	
 	
 	
 	/**  */
 	private class TestBasic extends TestCase.Unit {
 		public Object call() {
 			breakIfFailed();
 			return null;
 		}
 	}
 }
