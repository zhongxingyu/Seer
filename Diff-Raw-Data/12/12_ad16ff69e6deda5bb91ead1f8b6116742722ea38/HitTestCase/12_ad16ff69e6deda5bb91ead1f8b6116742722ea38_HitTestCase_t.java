 /**
  * Licensed under the MIT license: http://www.opensource.org/licenses/mit-license.php
  */
 package sim.monitor.test;
 
 import junit.framework.TestCase;
 import sim.monitor.Monitor;
 import sim.monitor.MonitorBuilder;
 import sim.monitor.processing.HitProcessor;
 import sim.monitor.publishers.Aggregate;
 import sim.monitor.subscribers.MockSubscriber;
 
 /**
  * @author val
  *
  */
 public class HitTestCase extends TestCase {
 
 	public HitTestCase() {
 		super("Hit tests");
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see junit.framework.TestCase#setUp()
 	 */
 	@Override
 	protected void setUp() throws Exception {
 		// ClassLoader.getSystemClassLoader().getResourceAsStream(name)
 		super.setUp();
 	}
 
 	public void testCounterHit() {
 		Monitor monitor = MonitorBuilder
 				.inContainer("com.test.hits:type=Counter")
 				.addStatistic(Aggregate.Count).publishRawValues(false)
 				.build("Counter", "Counts the hits on this monitor");
 
 		for (int i = 0; i < 10; i++) {
 			monitor.hit();
 		}
 
 		checkHitThreadsDone();
 
 		assertNotNull(MockSubscriber.instance);
 		assertNotNull(MockSubscriber.instance.value);
 		assertEquals(Long.class, MockSubscriber.instance.value.getClass());
 		assertEquals(10, ((Long) MockSubscriber.instance.value).longValue());
 	}
 
 	private void checkHitThreadsDone() {
 		sleep(100);
 		String failMessage = "Was waiting for all threads to finish in a reasonable amount of time. Failing task because of overtime!";
 		int waitingTurns = 0;
 		while (true) {
 			if (!HitProcessor.instance().allThreadsWaiting()) {
 				if (waitingTurns == 10) {
 					fail(failMessage);
 				}
 				if (!sleep(100)) {
 					break;
 				}
 				waitingTurns++;
			}
			sleep(100);
			// check again after a ilttle sleep that threads are still
			// waiting (we make sure that all hits were processed)
			if (HitProcessor.instance().allThreadsWaiting()) {
				break;
 			}
 		}
 	}
 
 	private boolean sleep(long amount) {
 		try {
 			Thread.sleep(amount);
 		} catch (InterruptedException e) {
 			return false;
 		}
 		return true;
 	}
 }
