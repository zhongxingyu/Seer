 package org.berkelium.java.impl;
 
 import java.util.Queue;
 import java.util.concurrent.BrokenBarrierException;
 import java.util.concurrent.ConcurrentLinkedQueue;
 import java.util.concurrent.CyclicBarrier;
 import java.util.concurrent.atomic.AtomicBoolean;
 import java.util.concurrent.atomic.AtomicReference;
 
 import org.berkelium.java.api.Berkelium;
 import org.berkelium.java.api.Window;
 
 public class MultiThreadBerkelium extends Berkelium {
 	private final CyclicBarrier initDoneBarrier = new CyclicBarrier(2);
 	private final Thread thread;
 	private final Queue<Runnable> queue = new ConcurrentLinkedQueue<Runnable>();
 	private RuntimeException initRuntimeException;
 	private Berkelium berkelium;
 	private final AtomicBoolean isShutdown = new AtomicBoolean(false);
 
 	private final Thread shutdown = new Thread("Berkelium Shutdown Thread") {
		public void run() {
 			try {
 				Thread.sleep(1000);
 				isShutdown.set(true);
 				while (thread.isAlive()) {
 					// TODO(drieks)
 					// InterruptedException in not correctly handled...
 					// see isShutdown quick fix
 					thread.interrupt();
 					thread.join(100);
 				}
 			} catch (InterruptedException e) {
 			}
 		};
 	};
 
 	private final Runnable threadRunnable = new Runnable() {
 		public void run() {
 			try {
 				initThread();
 			} catch (RuntimeException re) {
 				initRuntimeException = re;
 			} finally {
 				try {
 					initDoneBarrier.await();
 				} catch (InterruptedException e) {
 					// TODO
 				} catch (Throwable t) {
 					System.err.println(t);
 					throw new RuntimeException(t);
 				}
 			}
 			try {
 				while (!thread.isInterrupted()) {
 					if (!updateThread()) {
 						break;
 					}
 				}
 			} catch (Throwable t) {
 				System.err.println(t);
 				throw new RuntimeException(t);
 			} finally {
 				shutdownThread();
 			}
 		}
 	};
 
 	private final Runnable updater = new Runnable() {
 		public void run() {
 			try {
 				berkelium.update();
 			} catch (RuntimeException re) {
 				handleRuntimeException(re);
 			}
 			try {
 				Thread.sleep(100);
 			} catch (InterruptedException e) {
 				// TODO
 			}
 		}
 	};
 
 	public MultiThreadBerkelium() {
 		thread = new Thread(threadRunnable, "Berkelium Thread");
 		thread.start();
 		try {
 			initDoneBarrier.await();
 		} catch (InterruptedException e) {
 			// TODO
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		if (initRuntimeException != null)
 			throw initRuntimeException;
 	}
 
 	private void initThread() {
 		berkelium = new SingleThreadBerkelium();
 		Runtime.getRuntime().addShutdownHook(shutdown);
 	}
 
 	private boolean updateThread() {
 		execute(updater);
 
 		while (!queue.isEmpty()) {
 			try {
 				queue.remove().run();
 			} catch (Throwable ignore) {
 				System.err.println(ignore);
 				// TODO ignore...
 			}
 		}
 
 		if (isShutdown.get() && queue.isEmpty())
 			return false;
 
 		return true;
 	}
 
 	private void shutdownThread() {
 		try {
 			Thread.sleep(500);
 		} catch (InterruptedException e) {
 			// TODO
 		}
 		berkelium.destroy();
 	}
 
 	public void handleRuntimeException(RuntimeException re) {
 		// TODO delegate
 		re.printStackTrace();
 	}
 
 	public void execute(Runnable command) {
 		queue.add(command);
 	}
 
 	public void executeAndWait(final Runnable job) {
 		if (Thread.currentThread().equals(thread)) {
 			job.run();
 			return;
 		}
 
 		final CyclicBarrier barrier = new CyclicBarrier(2);
 		final AtomicReference<RuntimeException> ex = new AtomicReference<RuntimeException>();
 
 		execute(new Runnable() {
 			public void run() {
 				try {
 					try {
 						job.run();
 					} catch (RuntimeException t) {
 						ex.set(t);
 					}
 				} finally {
 					try {
 						barrier.await();
 					} catch (InterruptedException e) {
 						e.printStackTrace();
 					} catch (BrokenBarrierException e) {
 						e.printStackTrace();
 					}
 				}
 			}
 		});
 
 		try {
 			barrier.await();
 		} catch (InterruptedException e) {
 			// TODO
 			e.printStackTrace();
 		} catch (BrokenBarrierException e) {
 			e.printStackTrace();
 		}
 		RuntimeException re = ex.get();
 		if (re != null)
 			throw re;
 	}
 
 	public Window createWindow() {
 		final AtomicReference<Window> ret = new AtomicReference<Window>();
 		executeAndWait(new Runnable() {
 			public void run() {
 				ret.set(new WindowImpl(MultiThreadBerkelium.this));
 			}
 		});
 		return ret.get();
 	}
 
 	public void assertNotBerkeliumThread() {
 		berkelium.assertNotBerkeliumThread();
 	}
 
 	public void assertIsBerkeliumThread() {
 		berkelium.assertIsBerkeliumThread();
 	}
 
 	public void sync(Window win) {
 		berkelium.sync(win);
 	}
 
 	public void update() {
 		// not implemented
 		// update is called in berkelium thread
 	}
 
 	public void destroy() {
		// not implemented
		// runtime is destroyed in berkelium thread
 	}
 }
