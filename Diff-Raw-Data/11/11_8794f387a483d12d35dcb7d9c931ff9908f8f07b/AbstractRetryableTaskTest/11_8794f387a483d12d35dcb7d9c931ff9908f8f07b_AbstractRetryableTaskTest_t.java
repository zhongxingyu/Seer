 package com.thebuzzmedia.common.concurrent;
 
 import static org.junit.Assert.assertNotNull;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
 
 import org.junit.Before;
 import org.junit.Test;
 
 public class AbstractRetryableTaskTest {
 	int numberOfTasks = 1000;
 	int taskID = 1;
 
 	ExecutorService executor = Executors.newCachedThreadPool();

 	@Before
 	public void reset() {
 		taskID = 1;
 	}
 
 	@Test
 	public void testAbstractRetryableTask() throws InterruptedException,
 			ExecutionException {
 		List<Future<Object>> resultList = new ArrayList<Future<Object>>();
 
 		long t = System.currentTimeMillis();
 		System.out.println("Beginning " + numberOfTasks + " tasks...");
 
 		// Start a number of tasks, adding the results to our list.
 		for (int i = 0; i < numberOfTasks; i++)
 			resultList.add(executor.submit(new TryingTask()));
 
 		// Now confirm non-null results from all tasks.
		for (int i = 0; i < numberOfTasks; i++) {
 			Future<Object> f = resultList.get(i);
 			assertNotNull(f.get());
 		}
 
 		System.out.println("Success, received results from " + numberOfTasks
 				+ " completed tasks in " + (System.currentTimeMillis() - t)
 				+ "ms");
 	}
 
 	class TryingTask extends AbstractRetryableTask<Object> {
 		private int id;
 		private int retry = 1;

 		public TryingTask() {
 			super(5, 10, 2);

 			id = taskID++;
 		}
 
 		@Override
 		protected Object callImpl() throws Exception {
 			if (retry++ < 5)
 				throw new RuntimeException("Fake exception to force retry!");
 			else {
 				System.out.println("\tTask " + id + " completed!");
 				return "";
 			}
 		}
 
 		@Override
 		protected boolean canRetry(Exception e, int currentRetryCount,
 				int maxRetryCount) {
 			return true;
 		}
 
 	}
 }
