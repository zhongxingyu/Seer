 package com.stresstest.runners;
 
 import static org.junit.Assert.assertEquals;
 
 import java.util.concurrent.atomic.AtomicInteger;
 
import org.junit.AfterClass;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 
 @RunWith(FrequentRunner.class)
 @RunInParallel(maxThreads = 50)
 @RunTimes(500)
 public class FrequentRunnerSingleRunTest {
 	
 	private static AtomicInteger onceCounter = new AtomicInteger(0);
 	private static AtomicInteger parallelCounter = new AtomicInteger(0);
 
 	@CheckAfter
	public static void afterClass() {
		try {
			Thread.sleep(100000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
 		assertEquals(onceCounter.get(), 1);
 		assertEquals(parallelCounter.get(), 500);
 	}
 
 	@SingleRun
 	@Test
 	public void increaseCounterOnceTest() {
 		onceCounter.incrementAndGet();
 	}
 	
 	@Test
 	public void increaseCounter() {
 		parallelCounter.incrementAndGet();
 	}
 }
