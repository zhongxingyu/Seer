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
 import java.util.Map.Entry;
 import java.util.NavigableSet;
 import java.util.Queue;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentLinkedQueue;
 import java.util.concurrent.ConcurrentMap;
 import java.util.concurrent.ConcurrentSkipListSet;
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
 	 * servant error handler
 	 * 
 	 * @author <a href="mailto:zhizhong.qiu@happyelements.com">kevin</a>
 	 */
 	public static interface ErrorHandler {
 		public void errorThrow(Throwable throwable);
 	}
 
 	/**
 	 * the servant that handle the tasks
 	 * 
 	 * @author <a href="mailto:zhizhong.qiu@happyelements.com">kevin</a>
 	 */
 	protected static class Servant extends Thread {
 		/**
 		 * the servant state
 		 * 
 		 * @author <a href="mailto:zhizhong.qiu@happyelements.com">kevin</a>
 		 */
 		public static enum State {
 			INITIALIZE(-1), //
 			RUNNING(0), //
 			TO_WAIT(1); //
 
 			private final int value;
 
 			/**
 			 * constructor
 			 * 
 			 * @param value
 			 *            internal value
 			 */
 			State(int value) {
 				this.value = value;
 			}
 
 			/**
 			 * the value
 			 * 
 			 * @return the value
 			 */
 			public int value() {
 				return this.value;
 			}
 		}
 
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
 
 			// register this servant
 			Asyncs.WAITING_SERVANTS.add(this);
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
 					Asyncs origin = this.master;
 					// try steal from other
 					if ((this.master = Asyncs.NOTIFIERS.pollFirst()) != null) {
 						continue;
 					} else {
 						// reset
 						this.master = origin;
 					}
 
 					long now = System.nanoTime();
 
 					// put into waiting list
					Asyncs.WAITING_SERVANTS.add(this);
 
 					// park
 					LockSupport.parkNanos(Asyncs.WAIT_TIME * 1000000L);
 
 					// remove from waiting list
					Asyncs.WAITING_SERVANTS.remove(this);
 
 					// wake up by scheduler
 					long sleep = System.nanoTime() - now;
 					if (sleep <= Asyncs.WAIT_TIME * 1000000L) {
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
 
 			// remove from servants
 			Asyncs.WAITING_SERVANTS.remove(this);
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
 	 * the default error handler
 	 */
 	private static final ErrorHandler ERROR_HANDLER = new ErrorHandler() {
 
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public void errorThrow(Throwable throwable) {
 		}
 	};
 
 	private static final ConcurrentMap<Asyncs, Boolean> MONITOR = new ConcurrentHashMap<Asyncs, Boolean>();
 
 	private static final ConcurrentSkipListSet<Servant> WAITING_SERVANTS = new ConcurrentSkipListSet<Asyncs.Servant>(
 			new Comparator<Servant>() {
 				@Override
 				public int compare(Servant o1, Servant o2) {
 					return (int) (o1.getId() - o2.getId());
 				}
 			});
 
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
 
			private Runnable last_work;

 			@Override
 			public void run() {
 				int current_size = Asyncs.this.workqueue.size();
 				int consume = this.last - current_size;
 
				if (consume == 0) {
					Runnable work = Asyncs.this.workqueue.peek();
					if (work == this.last_work) {
						Asyncs.this.newAsync();
					} else {
						this.last_work = work;
					}
				} else if (consume < 0
						|| (consume > 0 && current_size > this.average)) {
					// queue size increasing
 					Asyncs.this.newAsync();
 				}
 
 				Asyncs.LOGGER.debug("last size:" + this.last + " current size:"
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
		Servant servant = Asyncs.WAITING_SERVANTS.pollFirst();
 		if (servant != null) {
 			// flag it earlier so the a time out servant
 			// got a chance to alive
 			Asyncs.NOTIFIERS.add(this);
 
 			// wake up
 			LockSupport.unpark(servant);
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
