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
 package com.happyelements.zero.async;
 
 import java.lang.ref.SoftReference;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Queue;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentLinkedQueue;
 import java.util.concurrent.atomic.AtomicInteger;
 import java.util.concurrent.locks.LockSupport;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 /**
  * async queues that automatically consume the runnable
  * @author <a href="mailto:zhizhong.qiu@happyelements.com">kevin</a>
  */
 public class Asyncs {
 	private static final Log LOGGER = LogFactory.getLog(Asyncs.class);
 
 	private static final Map<SoftReference<Asyncs>, Boolean> MONITOR = new ConcurrentHashMap<SoftReference<Asyncs>, Boolean>();
 
 	private static final Object COMMON_WAIT_POINT = new Object();
 	
 	private static final int WAIT_TIME = 10000;
 
 	static {
 		new Thread(new Runnable() {
 			@Override
 			public void run() {
 				for (;;) {
 					try {
 						for (Entry<SoftReference<Asyncs>, Boolean> entry : Asyncs.MONITOR
 								.entrySet()) {
 							Asyncs asyncs = entry.getKey().get();
 							if (asyncs != null && asyncs.scheduler != null) {
 								asyncs.scheduler.run();
 							} else {
 								Asyncs.MONITOR.remove(asyncs);
 							}
 						}
 
 						// fix rate schedule
 						LockSupport.parkNanos(1000000000);
 					} catch (Exception e) {
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
 
 	private final Queue<Runnable> workqueue;
 	private final AtomicInteger active_count;
 	private final AtomicInteger waiting_count;
 	private int estimate_work_queue_size;
 	private Runnable scheduler;
 
 	/**
 	 * constructor
 	 */
 	public Asyncs() {
 		this.workqueue = new ConcurrentLinkedQueue<Runnable>() {
 			private static final long serialVersionUID = -2916720295058032067L;
 
 			@Override
 			public boolean offer(Runnable runnable) {
 				if (runnable != null) {
 					Asyncs.this.estimate_work_queue_size++;
 				}
 				return super.offer(runnable);
 			}
 
 			@Override
 			public Runnable poll() {
 				Runnable runnable = super.poll();
 				if (runnable != null) {
 					Asyncs.this.estimate_work_queue_size--;
 				}
 				return runnable;
 			}
 		};
 
 		this.scheduler = this.createScheduler();
 		this.active_count = new AtomicInteger(0);
 		this.waiting_count = new AtomicInteger(0);
 		this.estimate_work_queue_size = 0;
 
 		// register this to monitor
 		Asyncs.MONITOR.put(new SoftReference<Asyncs>(this), Boolean.TRUE);
 	}
 
 	/**
 	 * queue new runnable
 	 * @param runnable
 	 */
 	public void offer(final Runnable runnable) {
 		if (runnable != null) {
 			this.workqueue.offer(runnable);
 		}
 	}
 
 	/**
 	 * create scheduler
 	 */
 	private Runnable createScheduler() {
 		return new Runnable() {
 			private Runnable last = null;
 			private int former_queue_size = 0;
 
 			@Override
 			public void run() {
 				Runnable current = Asyncs.this.workqueue.peek();
 				Runnable snapshot = this.last;
 
 				// update last
 				this.last = current;
 
 				// even if schedule is paused, start new to prevent dead
 				if (current != null && current == snapshot) {
 					// still the last,start new async
 					Asyncs.LOGGER.info("seems async dead,fire new");
 					Asyncs.this.newAsync();
 					return;
 				}
 
 				// sample queue
 				int former_snapshot = this.former_queue_size;
 				int first_sampe_queue_size = Asyncs.this.estimate_work_queue_size;
 
 				// update estimate
 				this.former_queue_size = (former_snapshot + first_sampe_queue_size) / 2 + 1;
 
 				if (first_sampe_queue_size <= former_snapshot) {
 					return;
 				}
 
 				Asyncs.LOGGER.debug("snapshot:" + former_snapshot + " first:"
 						+ first_sampe_queue_size);
 				Asyncs.this.newAsync();
 			}
 		};
 	}
 
 	/**
 	 * start new async servant
 	 */
 	private void newAsync() {
 		if (this.waiting_count.get() > 0) {
 			Asyncs.LOGGER.debug("try wake up one");
 			synchronized (Asyncs.COMMON_WAIT_POINT) {
 				Asyncs.COMMON_WAIT_POINT.notify();
 			}
 			return;
 		}
 
 		this.active_count.incrementAndGet();
 		new Thread(new Runnable() {
 			@Override
 			public void run() {
 				long start = System.currentTimeMillis();
 				Asyncs.LOGGER.debug("async servant start...");
 
 				for (;;) {
 					Runnable runnable = Asyncs.this.workqueue.poll();
 					if (runnable != null) {
 						try {
 							runnable.run();
 						} catch (Exception e) {
 							Asyncs.LOGGER.error(
 									"unexpected exception when handling work",
 									e);
 						}
 					} else {
 						long now = System.currentTimeMillis();
 						synchronized (Asyncs.COMMON_WAIT_POINT) {
 							try {
 								Asyncs.this.waiting_count.incrementAndGet();
								Asyncs.this.waiting_count.wait(Asyncs.WAIT_TIME);
 							} catch (InterruptedException e) {
 								// silence it
 							} finally {
 								Asyncs.this.waiting_count.decrementAndGet();
 							}
 						}
 
 						// timeout
 						if (System.currentTimeMillis() - now >= Asyncs.WAIT_TIME) {
 							Asyncs.this.active_count.decrementAndGet();
 							Asyncs.LOGGER
 									.debug("there are idle servants,quit. live for:"
 											+ (System.currentTimeMillis() - start));
 							break;
 						} else {
 							Asyncs.LOGGER.debug("wake up by schedule");
 						}
 					}
 				}
 			}
 		}, "async-servant").start();
 	}
 }
