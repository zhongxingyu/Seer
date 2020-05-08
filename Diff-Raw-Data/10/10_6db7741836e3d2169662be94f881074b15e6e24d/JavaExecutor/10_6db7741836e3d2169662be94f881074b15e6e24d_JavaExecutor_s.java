 package one.utils.jre.concurrent;
 
 import java.util.concurrent.CountDownLatch;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.TimeUnit;
 
 import one.utils.concurrent.OneExecutor;
 
 public class JavaExecutor implements OneExecutor {
 	private final ExecutorService executor;
 
 	Thread lastThread;
 
 	@Override
 	public Object execute(final Runnable runnable) {
 		final CountDownLatch latch = new CountDownLatch(2);
 
 		executor.execute(new Runnable() {
 
 			@Override
 			public void run() {
 				lastThread = Thread.currentThread();
 				latch.countDown();
 				runnable.run();
 			}
 		});
 
 		latch.countDown();
 
 		if (lastThread == null) {
 			try {
 				latch.await(5000, TimeUnit.MILLISECONDS);
 			} catch (final InterruptedException e) {
 				throw new RuntimeException(
 						"Cannot determine handle of thread to be executed.");
 			}
 		}
 
 		return lastThread;
 	}
 
 	@Override
 	public void shutdown(final WhenExecutorShutDown callback) {
 
 		executor.shutdown();
 
 		Thread t = new Thread() {
 
 			@Override
 			public void run() {
 				try {
 					executor.awaitTermination(10000, TimeUnit.MILLISECONDS);
 					callback.thenDo();
 				} catch (final Throwable t) {
 					callback.onFailure(t);
 				}
 
 			}
 
 		};
 		t.start();
 		t = null;
 
 	}
 
 	public JavaExecutor(final ExecutorService executor) {
 		super();
 		this.executor = executor;
 	}
 
 	@Override
 	public Object getCurrentThread() {
 
 		return Thread.currentThread();
 	}
 
 }
