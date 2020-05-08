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
 
 import java.lang.ref.WeakReference;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Queue;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentLinkedQueue;
 import java.util.concurrent.locks.LockSupport;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 /**
  * async queues that automatically consume the runnable
  * @author <a href="mailto:zhizhong.qiu@happyelements.com">kevin</a>
  */
 public class Asyncs {
 	private static final Log LOGGER = LogFactory.getLog(Asyncs.class);
 
 	private static final Map<WeakReference<Asyncs>, Boolean> MONITOR = new ConcurrentHashMap<WeakReference<Asyncs>, Boolean>();
 
 	static {
 		new Thread(new Runnable() {
 			@Override
 			public void run() {
 				while (true) {
 					try {
 						for (Entry<WeakReference<Asyncs>, Boolean> entry : Asyncs.MONITOR
 								.entrySet()) {
 							Asyncs asyncs = entry.getKey().get();
 							if (asyncs != null && asyncs.scheduler != null) {
 								asyncs.scheduler.run();
 							} else {
 								Asyncs.MONITOR.remove(entry.getKey());
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
 	private Runnable scheduler;
 
 	/**
 	 * constructor
 	 */
 	public Asyncs() {
 		// register this to monitor
 		Asyncs.MONITOR.put(new WeakReference<Asyncs>(this), true);
 		this.workqueue = new ConcurrentLinkedQueue<Runnable>();
 		this.scheduler = this.createScheduler();
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
 			private boolean pause_schedule = false;
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
 					this.last = current;
 					// still the last,start new async
 					Asyncs.LOGGER.info("seems async dead,fire new");
 					Asyncs.this.newAsync();
 					return;
 				}
 
 				// schedule is paused
 				if (this.pause_schedule) {
 					return;
 				}
 
 				try {
 					// place it to try catch block,avoid unexpected failure that
 					// may cause the scheduler die
 					this.pause_schedule = true;
 
 					// sample queue
 					int former_snapshtot = this.former_queue_size;
 					int first_sampe_queue_size = Asyncs.this.workqueue.size();
 					int second_sample_queue_size = Asyncs.this.workqueue.size();
 
 					this.former_queue_size = second_sample_queue_size;
 
 					// simple sample if it increasing
 					if (first_sampe_queue_size > former_snapshtot
							|| second_sample_queue_size > first_sampe_queue_size) {
 						Asyncs.LOGGER.info("former:" + former_snapshtot
 								+ " first:" + first_sampe_queue_size
 								+ " second:" + second_sample_queue_size);
 						// seems rather busy,start new
 						Asyncs.this.newAsync();
 					}
 				} catch (Exception e) {
 					Asyncs.LOGGER.error("unexpected exception in scheduler", e);
 				} finally {
 					this.pause_schedule = false;
 				}
 			}
 		};
 	}
 
 	/**
 	 * start new async servant
 	 */
 	private void newAsync() {
 		new Thread(new Runnable() {
 			@Override
 			public void run() {
 				long start = System.currentTimeMillis();
 				Asyncs.LOGGER.info("async servant start...");
 
 				Runnable runnable = null;
 				while ((runnable = Asyncs.this.workqueue.poll()) != null) {
 					try {
 						runnable.run();
 					} catch (Exception e) {
 						Asyncs.LOGGER.error(
 								"unexpected exception when handling work", e);
 					}
 				}
 
 				Asyncs.LOGGER.info("there are idle servants,quit. live for:"
 						+ (System.currentTimeMillis() - start));
 			}
 		}, "async-servant").start();
 	}
 }
