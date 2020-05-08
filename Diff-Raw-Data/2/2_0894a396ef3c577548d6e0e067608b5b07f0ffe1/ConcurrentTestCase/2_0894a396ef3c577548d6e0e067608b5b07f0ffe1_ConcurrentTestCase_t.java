 /*******************************************************************************
  * Copyright (c) 2012, 2013 Wind River Systems, Inc. and others. All rights reserved.
  * This program and the accompanying materials are made available under the terms
  * of the Eclipse Public License v1.0 which accompanies this distribution, and is
  * available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * Wind River Systems - initial API and implementation
  *******************************************************************************/
 package org.eclipse.tcf.te.tests.concurrent;
 
 import java.lang.reflect.Constructor;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.Callable;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Future;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.TimeoutException;
 import java.util.concurrent.atomic.AtomicInteger;
 
 import junit.framework.Test;
 import junit.framework.TestSuite;
 
 import org.eclipse.tcf.te.runtime.concurrent.Executors;
 import org.eclipse.tcf.te.runtime.concurrent.executors.AbstractDelegatingExecutorService;
 import org.eclipse.tcf.te.runtime.concurrent.interfaces.IExecutor;
 import org.eclipse.tcf.te.runtime.concurrent.interfaces.INestableExecutor;
 import org.eclipse.tcf.te.runtime.concurrent.interfaces.ISingleThreadedExecutor;
 import org.eclipse.tcf.te.runtime.concurrent.util.ExecutorsUtil;
 import org.eclipse.tcf.te.tests.CoreTestCase;
 
 /**
  * Concurrent test cases.
  */
 public class ConcurrentTestCase extends CoreTestCase {
 
 	/**
 	 * Provides a test suite to the caller which combines all single
 	 * test bundled within this category.
 	 *
 	 * @return Test suite containing all test for this test category.
 	 */
 	public static Test getTestSuite() {
 		TestSuite testSuite = new TestSuite("Test concurrent framework"); //$NON-NLS-1$
 
 			// add ourself to the test suite
 			testSuite.addTestSuite(ConcurrentTestCase.class);
 
 		return testSuite;
 	}
 
 	//***** BEGIN SECTION: Single test methods *****
 	//NOTE: All method which represents a single test case must
 	//      start with 'test'!
 
 	public void testPrivateOrInternalClasses() {
 		ExecutorsUtil executorsUtil = new ExecutorsUtil();
 		assertNotNull(executorsUtil);
 
 		try {
 			Constructor<?>[] constructors = Executors.class.getDeclaredConstructors();
 			for (Constructor<?> constructor : constructors) {
 				assertNotNull("Failed to get default constructor of Executors!", constructors); //$NON-NLS-1$
 				constructor.setAccessible(true);
 				Object instance = constructor.newInstance((Object[])null);
 				assertNotNull("Failed to invoke default constructor of Executors!", instance); //$NON-NLS-1$
 			}
 
 		} catch (Exception e) {}
 	}
 
 	public void testSingleThreadExecutorService() {
 		// Get the execution service instance
 		IExecutor executor = Executors.newExecutor("org.eclipse.tcf.te.runtime.concurrent.executors.singleThreaded"); //$NON-NLS-1$
 		assertNotNull("Failed to get executor instance!", executor); //$NON-NLS-1$
 		assertTrue("Executor not implementing ISingleThreadedExecutor", executor instanceof ISingleThreadedExecutor); //$NON-NLS-1$
 
 		final ISingleThreadedExecutor singleThreadedExecutor = (ISingleThreadedExecutor)executor;
 
 		// Within here, we have to be outside the execution thread
 		assertFalse("Is execution thread but should not!", singleThreadedExecutor.isExecutorThread()); //$NON-NLS-1$
 
 		// Create a runnable to be executed with the executor thread
 		final Boolean[] result = new Boolean[1];
 		result[0] = Boolean.FALSE;
 		Runnable runnable = new Runnable() {
 			@Override
             public void run() {
 				result[0] = Boolean.valueOf(singleThreadedExecutor.isExecutorThread());
 			}
 		};
 
 		// Execute
 		singleThreadedExecutor.execute(runnable);
 
 		// Give it a little bit time to run
 		AtomicInteger counter = new AtomicInteger();
 		while (Boolean.FALSE.equals(result[0]) && counter.getAndIncrement() < 20) {
 			try { Thread.sleep(100); } catch (InterruptedException e) { /* ignored on purpose */ }
 		}
 		assertTrue("Runnable not executed within the executor thread!", result[0].booleanValue()); //$NON-NLS-1$
 
 		// If the executor is implementing the ExecutorService interface, we can shutdown the executor
 		if (executor instanceof ExecutorService) {
 			ExecutorService service = (ExecutorService)executor;
 			// Shutdown the executor service
 			assertFalse("Executor service instance is marked as shutdowned, but should not!", service.isShutdown()); //$NON-NLS-1$
 			service.shutdown();
 			assertTrue("Executor service instance is not marked as shutdowned, but should!", service.isShutdown()); //$NON-NLS-1$
 		}
 
 		// Get the shared executor service instance
 		IExecutor sharedExecutor = Executors.getSharedExecutor("org.eclipse.tcf.te.runtime.concurrent.executors.singleThreaded"); //$NON-NLS-1$
 		assertNotNull("Failed to get shared executor instance!"); //$NON-NLS-1$
 		assertNotSame("Shared executor instance is same instance as the newly created executor!", executor, sharedExecutor); //$NON-NLS-1$
 
 		// If the shared executor service is not a nestable service, our tests are done here
 		if (!(sharedExecutor instanceof INestableExecutor)) return;
 
 		// Test the nestable executor functionality
 		final INestableExecutor nestedExecutor = (INestableExecutor)sharedExecutor;
 		result[0] = Boolean.FALSE;
 		final List<String> result2 = new ArrayList<String>();
 
 		// Single threaded and nested --> means maxDepth == 1
 		assertEquals("Single threaded nested executor has invalid maxDepth set!", 1, nestedExecutor.getMaxDepth()); //$NON-NLS-1$
 
 		Runnable runnable1 = new Runnable() {
 			@Override
             public void run() {
 				try { Thread.sleep(1000); } catch (InterruptedException e) {}
 				result2.add("1"); //$NON-NLS-1$
 				while (nestedExecutor.readAndExecute()) {}
 				result2.add("3"); //$NON-NLS-1$
 				result[0] = Boolean.TRUE;
 			}
 		};
 		Runnable runnable2 = new Runnable() {
 			@Override
             public void run() {
 				result2.add("2"); //$NON-NLS-1$
 			}
 		};
 
 		nestedExecutor.execute(runnable1);
 		nestedExecutor.execute(runnable2);
 
 		// Give it a little bit time to run
 		counter = new AtomicInteger();
		while (Boolean.FALSE.equals(result[0]) && counter.getAndIncrement() < 40) {
 			try { Thread.sleep(100); } catch (InterruptedException e) { /* ignored on purpose */ }
 		}
 		assertTrue("Runnable not executed within the executor thread!", result[0].booleanValue()); //$NON-NLS-1$
 
 		// The result list should contain "1", "2", "3" in this order.
 		assertEquals("Unexpected result list size!", 3, result2.size()); //$NON-NLS-1$
 		assertEquals("Unexpected result at position 1!", "1", result2.get(0)); //$NON-NLS-1$ //$NON-NLS-2$
 		assertEquals("Unexpected result at position 2!", "2", result2.get(1)); //$NON-NLS-1$ //$NON-NLS-2$
 		assertEquals("Unexpected result at position 3!", "3", result2.get(2)); //$NON-NLS-1$ //$NON-NLS-2$
 
 		// Get all shared executor service instances
 		IExecutor[] executors = Executors.getAllSharedExecutors();
 		assertTrue("Unexpected emply list returned!", executors.length > 0); //$NON-NLS-1$
 	}
 
 	class InternalTestAbstractDelegatingExecutorServiceImplementation extends AbstractDelegatingExecutorService {
 		final Map<String, Object> fResultMap;
 
 		/**
 		 * Constructor.
 		 *
 		 */
 		public InternalTestAbstractDelegatingExecutorServiceImplementation(Map<String, Object> resultMap) {
 			super();
 			assertNotNull("Invalid constructor parameter resultMap. Must not be null!", resultMap); //$NON-NLS-1$
 			fResultMap = resultMap;
 		}
 
 		/* (non-Javadoc)
 		 * @see org.eclipse.tcf.te.runtime.concurrent.executors.AbstractDelegatingExecutorService#createExecutorServiceDelegate()
 		 */
 		@Override
 		protected ExecutorService createExecutorServiceDelegate() {
 			return new ExecutorService() {
 
 				@Override
                 public void execute(Runnable command) {
 					fResultMap.put("ExecutorService.execute", Boolean.TRUE); //$NON-NLS-1$
 				}
 
 				@Override
                 public <T> Future<T> submit(Runnable task, T result) {
 					fResultMap.put("ExecutorService.submit1", Boolean.TRUE); //$NON-NLS-1$
 					return null;
 				}
 
 				@Override
                 public Future<?> submit(Runnable task) {
 					fResultMap.put("ExecutorService.submit2", Boolean.TRUE); //$NON-NLS-1$
 					return null;
 				}
 
 				@Override
                 public <T> Future<T> submit(Callable<T> task) {
 					fResultMap.put("ExecutorService.submit3", Boolean.TRUE); //$NON-NLS-1$
 					return null;
 				}
 
 				@Override
                 public List<Runnable> shutdownNow() {
 					fResultMap.put("ExecutorService.shutdownNow", Boolean.TRUE); //$NON-NLS-1$
 					return null;
 				}
 
 				@Override
                 public void shutdown() {
 					fResultMap.put("ExecutorService.shutdown", Boolean.TRUE); //$NON-NLS-1$
 				}
 
 				@Override
                 public boolean isTerminated() {
 					fResultMap.put("ExecutorService.isTerminated", Boolean.TRUE); //$NON-NLS-1$
 					return false;
 				}
 
 				@Override
                 public boolean isShutdown() {
 					fResultMap.put("ExecutorService.isShutdown", Boolean.TRUE); //$NON-NLS-1$
 					return false;
 				}
 
 				@Override
                 public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
 					fResultMap.put("ExecutorService.invokeAny1", Boolean.TRUE); //$NON-NLS-1$
 					return null;
 				}
 
 				@Override
                 public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
 					fResultMap.put("ExecutorService.invokeAny2", Boolean.TRUE); //$NON-NLS-1$
 					return null;
 				}
 
 				@Override
                 public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException {
 					fResultMap.put("ExecutorService.invokeAll1", Boolean.TRUE); //$NON-NLS-1$
 					return null;
 				}
 
 				@Override
                 public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
 					fResultMap.put("ExecutorService.invokeAll2", Boolean.TRUE); //$NON-NLS-1$
 					return null;
 				}
 
 				@Override
                 public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
 					fResultMap.put("ExecutorService.awaitTermination", Boolean.TRUE); //$NON-NLS-1$
 					return false;
 				}
 			};
 		}
 
 	}
 
 	public void testAbstractDelegatingExecutorService() {
 		// To test the AbstractDelegatingExecutorService, we create a special
 		// executor storing the signature of the invoked method into a map
 
 		// Create the result map
 		final Map<String, Object> result = new HashMap<String, Object>();
 
 		// Construct the test service
 		AbstractDelegatingExecutorService service = new InternalTestAbstractDelegatingExecutorServiceImplementation(result);
 		service.initializeExecutorServiceDelegate();
 		assertNotNull("Failed to instanciate and to initialize the test executor service implementation!", service); //$NON-NLS-1$
 
 		Runnable runnable = new Runnable() {
 			@Override
 			public void run() {
 			}
 		};
 
 		Callable<Object> callable = new Callable<Object>() {
 			@Override
 			public Object call() throws Exception {
 			    return null;
 			}
 		};
 
 		List<Callable<Object>> callables = new ArrayList<Callable<Object>>();
 		callables.add(callable);
 
 		// Invoke each method now
 		service.execute(runnable);
 		service.submit(callable);
 		service.submit(runnable);
 		service.submit(runnable, new Object());
 		service.shutdown();
 		service.shutdownNow();
 		service.isShutdown();
 		service.isTerminated();
 		try {
 			service.invokeAny(callables);
 			service.invokeAny(callables, 0, TimeUnit.MICROSECONDS);
 			service.invokeAll(callables);
 			service.invokeAll(callables, 0, TimeUnit.MICROSECONDS);
 			service.awaitTermination(0, TimeUnit.MICROSECONDS);
 		} catch (Exception e) {}
 
 		assertTrue(((Boolean)result.get("ExecutorService.execute")).booleanValue()); //$NON-NLS-1$
 		assertTrue(((Boolean)result.get("ExecutorService.submit1")).booleanValue()); //$NON-NLS-1$
 		assertTrue(((Boolean)result.get("ExecutorService.submit2")).booleanValue()); //$NON-NLS-1$
 		assertTrue(((Boolean)result.get("ExecutorService.submit3")).booleanValue()); //$NON-NLS-1$
 		assertTrue(((Boolean)result.get("ExecutorService.shutdown")).booleanValue()); //$NON-NLS-1$
 		assertTrue(((Boolean)result.get("ExecutorService.shutdownNow")).booleanValue()); //$NON-NLS-1$
 		assertTrue(((Boolean)result.get("ExecutorService.isShutdown")).booleanValue()); //$NON-NLS-1$
 		assertTrue(((Boolean)result.get("ExecutorService.isTerminated")).booleanValue()); //$NON-NLS-1$
 		assertTrue(((Boolean)result.get("ExecutorService.invokeAny1")).booleanValue()); //$NON-NLS-1$
 		assertTrue(((Boolean)result.get("ExecutorService.invokeAny2")).booleanValue()); //$NON-NLS-1$
 		assertTrue(((Boolean)result.get("ExecutorService.invokeAll1")).booleanValue()); //$NON-NLS-1$
 		assertTrue(((Boolean)result.get("ExecutorService.invokeAll2")).booleanValue()); //$NON-NLS-1$
 		assertTrue(((Boolean)result.get("ExecutorService.awaitTermination")).booleanValue()); //$NON-NLS-1$
 	}
 
 	//***** END SECTION: Single test methods *****
 }
