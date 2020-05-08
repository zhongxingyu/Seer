 /*
  * Copyright (c) 2012, someone All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  * 
  * 1.Redistributions of source code must retain the above copyright notice, this
  * list of conditions and the following disclaimer. 2.Redistributions in binary
  * form must reproduce the above copyright notice, this list of conditions and
  * the following disclaimer in the documentation and/or other materials provided
  * with the distribution. 3.Neither the name of the Happyelements Ltd. nor the
  * names of its contributors may be used to endorse or promote products derived
  * from this software without specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
  * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
  * POSSIBILITY OF SUCH DAMAGE.
  */
 package com.github.zero.async;
 
 import java.util.Comparator;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.NavigableSet;
 import java.util.Queue;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentLinkedQueue;
 import java.util.concurrent.ConcurrentSkipListSet;
 import java.util.concurrent.atomic.AtomicInteger;
 import java.util.concurrent.locks.LockSupport;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 /**
  * async queues that automatically consume the runnable
  * 
  * @author <a href="mailto:zhizhong.qiu@happyelements.com">kevin</a>
  */
 public class Asyncs {
 	private static final Log LOGGER = LogFactory.getLog(Asyncs.class);
 
 	/**
 	 * the servant that handle the tasks
 	 * 
 	 * @author <a href="mailto:zhizhong.qiu@happyelements.com">kevin</a>
 	 */
 	protected static class Servant extends Thread {
 		protected Asyncs master;
 
 		/**
 		 * the master asyncs
 		 * 
 		 * @param asyncs
 		 *            the asyncs
 		 */
 		public Servant(Asyncs asyncs) {
 			super("async-servant");
 			this.master = asyncs;
 
 			// start to work,unregister from notifier
 			Asyncs.NOTIFIERS.remove(asyncs);
 		}
 
 		/**
 		 * {@inheritDoc}
 		 * 
 		 * @see java.lang.Thread#run()
 		 */
 		@Override
 		public void run() {
 			long start = System.nanoTime();
 			Asyncs.LOGGER.debug("async servant start...");
 
 			for (;;) {
 				Runnable runnable = this.master.workqueue.poll();
 				if (runnable != null) {
 					try {
 						runnable.run();
 					} catch (Exception e) {
 						Asyncs.LOGGER.error(
 								"unexpected exception when handling work", e);
 					}
 				} else {
 					long now = System.nanoTime();
 					synchronized (Asyncs.COMMON_WAIT_POINT) {
 						try {
 							this.master.waiting_count.incrementAndGet();
 							Asyncs.COMMON_WAIT_POINT.wait(Asyncs.WAIT_TIME);
 						} catch (InterruptedException e) {
 							// silence it
 						} finally {
 							this.master.waiting_count.decrementAndGet();
 						}
 					}
 
 					// wake up by scheduler
 					long sleep = System.nanoTime() - now;
					if (sleep <= Asyncs.WAIT_TIME * 1000000) {
 						// try if a notifier in queue
 						if ((this.master = Asyncs.NOTIFIERS.pollFirst()) != null) {
 							// switch master
 							continue;
 						}
 
 						Asyncs.LOGGER
 								.warn("wake up by someother but got no master,it may be a bug,kill this servarnt");
 					}
 
 					// time out totally
 					Asyncs.LOGGER
 							.debug("there are idle servants,quit. live for:"
 									+ (System.nanoTime() - start)
 									+ " last sleep:" + sleep);
 					break;
 				}
 			}
 
 			// cut reference
 			this.master = null;
 		}
 
 		/**
 		 * {@inheritDoc}
 		 * 
 		 * @see java.lang.Thread#start()
 		 */
 		@Override
 		public void start() {
 			this.setDaemon(true);
 			super.start();
 		}
 	}
 
 	/**
 	 * servant error handler
 	 * 
 	 * @author <a href="mailto:zhizhong.qiu@happyelements.com">kevin</a>
 	 */
 	public static interface ErrorHandler {
 		public void errorThrow(Throwable throwable);
 	}
 
 	private static final ErrorHandler ERROR_HANDLER = new ErrorHandler() {
 		@Override
 		public void errorThrow(Throwable throwable) {
 		}
 	};
 
 	private static final Map<Asyncs, Boolean> MONITOR = new ConcurrentHashMap<Asyncs, Boolean>();
 
 	private static final Object COMMON_WAIT_POINT = new Object();
 
 	private static final int WAIT_TIME = 10000;
 
 	private static final NavigableSet<Asyncs> NOTIFIERS;
 
 	private static final long SCHEDULER_RATE = 1000000000L;
 
 	static {
 		// mark those asyncs of that need wake up new servants
 		NOTIFIERS = new ConcurrentSkipListSet<Asyncs>(new Comparator<Asyncs>() {
 			@Override
 			public int compare(Asyncs o1, Asyncs o2) {
 				return o1.hashCode() - o2.hashCode();
 			}
 		});
 
 		// start monitor
 		new Thread(new Runnable() {
 			@Override
 			public void run() {
 				for (;;) {
 					try {
 						for (Entry<Asyncs, Boolean> entry : Asyncs.MONITOR.entrySet()) {
 							Asyncs async = entry.getKey();
 							if (async != null && async.scheduler != null) {
 								async.scheduler.run();
 							}
 						}
 
 						// fix rate schedule
 						LockSupport.parkNanos(Asyncs.SCHEDULER_RATE);
 					} catch (Throwable e) {
 						Asyncs.LOGGER.error(
 								"unexpected exception in async monitoer", e);
 					}
 				}
 			}
 		}, "async-monitor") {
 			@Override
 			public void start() {
 				this.setDaemon(true);
 				super.start();
 			}
 		}.start();
 	}
 
 	private final ErrorHandler error_listner;
 	private final Queue<Runnable> workqueue;
 	private final AtomicInteger waiting_count;
 	private Runnable scheduler;
 
 	/**
 	 * constructor
 	 */
 	public Asyncs() {
 		this(null);
 	}
 
 	/**
 	 * constructor with a exception listner
 	 * 
 	 * @param listener
 	 *            the listener
 	 */
 	public Asyncs(final ErrorHandler listener) {
 		this.error_listner = Asyncs.ERROR_HANDLER;
 
 		this.workqueue = new ConcurrentLinkedQueue<Runnable>();
 
 		this.scheduler = this.createScheduler();
 		this.waiting_count = new AtomicInteger(0);
 
 		// attach to scheduler
 		this.attach();
 	}
 
 	/**
 	 * queue new runnable
 	 * 
 	 * @param runnable
 	 */
 	public void offer(final Runnable runnable) {
 		if (runnable != null) {
 			this.workqueue.offer(runnable);
 		}
 	}
 
 	/**
 	 * attach to scheduler
 	 */
 	public void attach() {
 		// register this to monitor
 		Asyncs.MONITOR.put(this, Boolean.TRUE);
 	}
 
 	/**
 	 * detach from scheduler
 	 */
 	public void detach() {
 		Asyncs.NOTIFIERS.remove(this);
 		Asyncs.MONITOR.remove(this);
 	}
 
 	/**
 	 * create scheduler
 	 */
 	private Runnable createScheduler() {
 		return new Runnable() {
 
 			private int last = 0;
 			private int average = 0;
 
 			@Override
 			public void run() {
 				int current_size = Asyncs.this.workqueue.size();
 				int consume = last - current_size;
 
 				// queue size increasing
 				if (consume < 0 || (consume > 0 && current_size > average)) {
 					Asyncs.this.newAsync();
 				}
 
 				Asyncs.LOGGER.debug("last size:" + last + " current size:"
 						+ current_size);
 
 				// update last
 				this.last = current_size;
 				this.average = (this.average + current_size) / 2 + 1;
 			}
 		};
 	}
 
 	/**
 	 * start new async servant
 	 * 
 	 * @param force_create
 	 *            force create new one
 	 */
 	private void newAsync() {
 		if (this.waiting_count.get() > 0) {
 			// flag it earlier so the a time out servant
 			// got a chance to alive
 			Asyncs.NOTIFIERS.add(this);
 
 			// flag this master
 			synchronized (Asyncs.COMMON_WAIT_POINT) {
 				Asyncs.LOGGER.debug("try wake up one");
 				Asyncs.COMMON_WAIT_POINT.notify();
 			}
 
 			// not yet sure there is a live servant working for this.
 			// leave this to be re-check by main scheduler
 			return;
 		}
 
 		// no waiting servant , start new one
 		new Servant(this) {
 			@Override
 			public void run() {
 				try {
 					super.run();
 				} catch (Throwable e) {
 					Asyncs.LOGGER.error("unexpected exception", e);
 					Asyncs.this.error_listner.errorThrow(e);
 					throw new RuntimeException(e);
 				}
 			}
 		}.start();
 	}
 }
