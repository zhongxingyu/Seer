 /*-
  * Copyright © 2009 Diamond Light Source Ltd.
  *
  * This file is part of GDA.
  *
  * GDA is free software: you can redistribute it and/or modify it under the
  * terms of the GNU General Public License version 3 as published by the Free
  * Software Foundation.
  *
  * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  *
  * You should have received a copy of the GNU General Public License along
  * with GDA. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package gda.scan;
 
 import gda.data.scan.datawriter.DataWriter;
 import gda.device.DeviceException;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Vector;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.Callable;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
 import java.util.concurrent.LinkedBlockingQueue;
 import java.util.concurrent.RejectedExecutionException;
 import java.util.concurrent.ThreadFactory;
 import java.util.concurrent.ThreadPoolExecutor;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.atomic.AtomicInteger;
 
 import org.python.core.PyException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * An implementation of {@link ScanDataPointPipeline} that computes ScanDataPoints and broadcasts them using internally
  * managed threads.
  */
 public class MultithreadedScanDataPointPipeline implements ScanDataPointPipeline {
 
 	public class ScannableSpecificExecutorService{
 
 		private ExecutorService service;
 		Map<String, ExecutorService> namedES = new HashMap<String,ExecutorService>();
 		private ThreadFactory threadFactory;
 
 		public ScannableSpecificExecutorService(int positionCallableThreadPoolSize, ThreadFactory threadFactory) {
 			this.threadFactory = threadFactory;
 			service = Executors.newFixedThreadPool(positionCallableThreadPoolSize, threadFactory);
 			
 		}
 
 		
 		public void shutdown() {
 			service.shutdown();
 			for(Entry<String, ExecutorService> es : namedES.entrySet())
 				es.getValue().shutdown();
 		}
 
 		public void shutdownNow() {
 			service.shutdownNow();
 
 			for(Entry<String, ExecutorService> es : namedES.entrySet())
 				es.getValue().shutdownNow();
 		}
 
 		public boolean isShutdown() {
 			boolean shutdown = service.isShutdown();
 			for(Entry<String, ExecutorService> es : namedES.entrySet())
 				shutdown &= es.getValue().isShutdown();
 			return shutdown;
 		}
 
 		public boolean isTerminated() {
 			boolean terminated = service.isTerminated();
 			for(Entry<String, ExecutorService> es : namedES.entrySet())
 				terminated &= es.getValue().isTerminated();
 			return terminated;
 			
 		}
 
 		public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
 			boolean awaitTermination = service.awaitTermination(timeout, unit);
 			for(Entry<String, ExecutorService> es : namedES.entrySet())
 				awaitTermination &= es.getValue().awaitTermination(timeout,unit);
 			return awaitTermination;
 			
 		}
 
 		public <T> Future<T> submit(Callable<T> task) {
 			ExecutorService es = service;
 			if( task instanceof NamedQueueTask){
 				NamedQueueTask task2 = (NamedQueueTask)task;
 				String name = task2.getExecutorServiceName();
 				es = namedES.get(name);
 				if( es == null){
 					es = Executors.newFixedThreadPool(task2.getThreadPoolSize(), threadFactory);
 					namedES.put(name, es);
 				}
 			}  
 			return es.submit(task);
 		}
 
 	}
 
 	private static final Logger logger = LoggerFactory.getLogger(MultithreadedScanDataPointPipeline.class);
 
 	private Throwable exception; // Guarded by 'this'
 
 	/**
 	 * Pool used to compute individual Scannable's positions from their position Callables.
 	 */
 	private ScannableSpecificExecutorService positionCallableService;
 
 	/**
 	 * Single threaded, fixed length service used to populate and broadcast ScanDataPoints.
 	 */
 	private ThreadPoolExecutor broadcasterQueue;
 
 	private ScanDataPointPublisher broadcaster;
 
 	/**
 	 * Creates a new MultithreadedScanDataPointPipeline and starts it up to accept points.
 	 * 
 	 * @param broadcaster
 	 * @param positionCallableThreadPoolSize
 	 *            the number of threads used to process Callables
 	 * @param scanDataPointPipelineLength
 	 *            the number of points allowed in the Pipeline concurrently.
 	 */
 	public MultithreadedScanDataPointPipeline(ScanDataPointPublisher broadcaster, int positionCallableThreadPoolSize,
 			int scanDataPointPipelineLength, String scanName) {
 
 		this.broadcaster = broadcaster;
 		
 		if (scanDataPointPipelineLength == 0) {
 			logger.warn("A zero length pipeline was requested but this would be unable to accept ScanDataPoints. A pipeline of length one hase been created instead");
 			scanDataPointPipelineLength = 1;
 		}
 		
 		NamedThreadFactory threadFactory = new NamedThreadFactory(
 				" scan-" + scanName + "-MSDPP.positionCallableService-%d of " + positionCallableThreadPoolSize);
 		if (positionCallableThreadPoolSize > 0) {
 			positionCallableService = new ScannableSpecificExecutorService(positionCallableThreadPoolSize, threadFactory);
 		} // else leave it null.
 		createScannablePopulatorAndBroadcasterQueueAndThread(scanName);
 	}
 
 	/**
 	 * Uses a ThreadPoolExecutor with a custom queue designed to block rather than throw a RejectedExecutionException if
 	 * the thread is busy and queue is full. The total number of points in the Pipeline is the number of points in the
 	 * workQueue plus the one being worked on in the single thread.
 	 */
 	private void createScannablePopulatorAndBroadcasterQueueAndThread(String scanName) {
 		BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<Runnable>();
 		broadcasterQueue = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, workQueue,  
 				new NamedThreadFactory(" scan-" + scanName + "-MSDPP.broadcaster"));
 	}
 
 	/**
 	 * Computes ScanDataPoints and broadcasts them using internally managed threads.
 	 */
 	@Override
 	public void put(IScanDataPoint point) throws DeviceException, Exception {
 		logger.debug("'{}': added to pipeline. Points already waiting in queue: {}", point.toString(),
 				broadcasterQueue.getQueue().size());
 		throwException();
 		if (positionCallableService != null) {
 			// If this has not been created we need not look for Callables
 			convertPositionCallablesToFutures(point);
 		}
 		try {
 			broadcasterQueue.execute(new ScanDataPointPopulatorAndPublisher(getBroadcaster(),
 					point, this));
 		} catch (RejectedExecutionException e) {
 			if (broadcasterQueue.isShutdown()) {
 				throw new DeviceException(
 						"Could not add new point to MultithreadedScanDataPointPipeline as it is shutdown.", e);
 			}
 			throw e;
 		}
 		logger.debug("'{}' added to executor", point.toString());
 	}
 
 	private void convertPositionCallablesToFutures(IScanDataPoint point)  {
 		convertDevices(point.getPositions());
 		convertDevices(point.getDetectorData());
 	}
 	
 	private void convertDevices(Vector<Object> positions) throws RejectedExecutionException {
 		for (int i = 0; i < positions.size(); i++) {
 			Object possiblyCallable = positions.get(i);
 			
 			if (possiblyCallable instanceof Callable<?>) {
 				Future<?> future = positionCallableService.submit((Callable<?>) possiblyCallable);
 				positions.set(i, future);
 			}
 		}
 	}
 	
 	/**
 	 * If an exception has been caught then throw it.
 	 * 
 	 * @throws DeviceException
 	 */
 	private void throwException() throws DeviceException {
 		Throwable e = getException();
 		if (e != null) {
 			throw wrappedException(e);
 		}
 	}
 
 	protected static DeviceException wrappedException(Throwable e) {
 		String message;
 		if (e instanceof NullPointerException) {
 			message = "NullPointerException";
 		} else {
 			message = (e instanceof PyException) ? e.toString() : e.getMessage();
 		}
 		return new DeviceException("Unable to publish scan data point because: " + e.getClass().getSimpleName() + " : ", e);
 	}
 
 	synchronized Throwable getException() {
 		return exception;
 	}
 
 	/**
 	 * Called by a DataPointPopulatorAndBroadcaster runnable to indicate thet there was a problem computing a position
 	 * form a scan data point. This will store the exception and shutdownNow.
 	 * 
 	 * @param e
 	 */
 	synchronized void setExceptionAndShutdownNow(Throwable e) {
 		exception = e;
 		String message = (e instanceof PyException) ? e.toString() : e.getMessage();
 		logger.info("Storing an Exception caught computing a point (and then shutting down pipeline): " + message);
 		try {
 			shutdownNow();
 		} catch (Exception e1) {
 			logger.warn("While handling exception from thread, caught another Exception shutting down pipeline: " + e1.getMessage(), e1);
 		}
 
 	}
 
 	@Override
 	public void shutdownNow() throws DeviceException, InterruptedException {
 		logger.info("Shutting down MultithreadedScanDataPointPipeline NOW.");
 		int numberOfDumpedPoints = shutdownNowAndGetNumberOfDumpedPoints();
 		if (numberOfDumpedPoints > 0) {
 			logger.warn("The MultithreadedScanDataPointPipeline was shutdown with " + numberOfDumpedPoints
 					+ " points still to record and broadcast.");
 		}
 		logger.info("Awaiting positionCallableService shutdown");
 		positionCallableService.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
 		logger.info("Awaiting broadcasterQueue shutdown");
 		broadcasterQueue.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
 		
 	}
 
 	private int shutdownNowAndGetNumberOfDumpedPoints() {
 		if (positionCallableService != null) {
 			positionCallableService.shutdownNow();  // This depends on the tasks being cancelable.
 		}
 		List<Runnable> remainingPointsInQueue = broadcasterQueue.shutdownNow();
 		try {
 			/**
 			 * This call clears the interrupted flag.
 			 * This is important so that the normal completeCollection() and 
 			 * other ordinary cleanup code can run and do file and terminal 
 			 * I/O which would fail (by design) in interrupted state. 
 			 */
 			boolean interrupted = Thread.interrupted();
 			getBroadcaster().shutdown();
 		} catch (Exception e) {
 			// TODO: Why are we absorbing this?
 		}
 		return remainingPointsInQueue.size();
 
 	}
 
 	/**
 	 * Politely shutdown the pipeline. Blocks until processing is complete. Calls shutdownNow if there is any problem or
 	 * if interrupted.
 	 * 
 	 * @param timeoutMillis
 	 * @throws DeviceException
 	 * @throws InterruptedException
 	 */
 	@Override
 	public void shutdown(long timeoutMillis) throws DeviceException, InterruptedException {
 		try {
 			// 1. Shutdown the populate-and-broadcast Executor
 			if (positionCallableService != null) {
 				positionCallableService.shutdown();
 				boolean shutdownOkay = positionCallableService.awaitTermination(timeoutMillis, TimeUnit.MILLISECONDS);
 				if (!shutdownOkay) {
 					int numberOfDumpedPoints = shutdownNowAndGetNumberOfDumpedPoints();
 					throw new DeviceException("positionCallableExecutor did not shutdown politely before " + timeoutMillis
 							+ "ms timeout. The Pipeline has been stopped and " + numberOfDumpedPoints + " points dumped.");
 				}
 			}
 
 			// 2. Shutdown the now empty position callable executor
 			broadcasterQueue.shutdown();
 			boolean shutdownOkay = broadcasterQueue.awaitTermination(1200000, TimeUnit.MILLISECONDS);
 			// (timeout is to broadcast last point only as its callables will have all returned.)
 			if (!shutdownOkay) {
 				int numberOfDumpedPoints = shutdownNowAndGetNumberOfDumpedPoints();
 				throw new DeviceException(
 						"scannablePopulatorAndBroadcasterExecutor did not shutdown politely before 2 min timeout.  The Pipeline has been stopped and "
 								+ numberOfDumpedPoints + " points dumped.");
 			}
 
 			// 3. Shutdown the Broadcaster (DataWriter)
 			try {
 				getBroadcaster().shutdown();
 			} catch (Exception e) {
 				throw new DeviceException("problem shutting down broadcaster (datawriter).", e);
 			}
 
 		} catch (InterruptedException e) {
 
 			int numberOfDumpedPoints = shutdownNowAndGetNumberOfDumpedPoints();
 
 			throw new DeviceException(
 					"Interupted while shutting down MultithreadedScanDataPointPipeline. The Pipeline has been stopped and "
 							+ numberOfDumpedPoints + " points dumped.");
 		}
 		// If everything stopped normally then throw exception from Runnable if there were any.
 		throwException();
 	}
 
 	@Override
 	public DataWriter getDataWriter() {
 		return getBroadcaster().getDataWriter();
 	}
 
 	protected ScanDataPointPublisher getBroadcaster() {
 		return broadcaster;
 	}
 
 	private class NamedThreadFactory implements ThreadFactory {
 
 		private final ThreadFactory defaultThreadFactory;
 
 		final AtomicInteger threadNumber = new AtomicInteger(1);
 
 		private final String format;
 
 		public NamedThreadFactory(String format) {
 			this.format = format;
 			defaultThreadFactory = Executors.defaultThreadFactory();
 		}
 
 		@Override
 		public Thread newThread(Runnable r) {
 			Thread newThread = defaultThreadFactory.newThread(r);
 			newThread.setName(newThread.getName() + String.format(format, +threadNumber.getAndIncrement()));
 			return newThread;
 		}
 
 	}
 }
