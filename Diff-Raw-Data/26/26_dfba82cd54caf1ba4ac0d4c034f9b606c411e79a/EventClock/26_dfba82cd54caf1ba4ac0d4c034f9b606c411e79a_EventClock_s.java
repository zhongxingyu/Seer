 /*
  * Copyright 2014 Eediom Inc.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.araqne.logdb.cep;
 
 import java.text.SimpleDateFormat;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Date;
 import java.util.List;
 import java.util.PriorityQueue;
 
 public class EventClock {
 	private final TimeoutComparator timeoutComparator = new TimeoutComparator();
 	private final ExpireComparator expireComparator = new ExpireComparator();
 	private final EventContextStorage storage;
 	private final String host;
 	private final PriorityQueue<EventContext> timeoutQueue;
 	private final PriorityQueue<EventContext> expireQueue;
 
 	private volatile long lastTime;
 
 	public EventClock(EventContextStorage storage, String host, long lastTime, int initialCapacity) {
 		this.storage = storage;
 		this.host = host;
 		this.lastTime = lastTime;
 		this.timeoutQueue = new PriorityQueue<EventContext>(initialCapacity, timeoutComparator);
 		this.expireQueue = new PriorityQueue<EventContext>(initialCapacity, expireComparator);
 	}
 
 	public String getHost() {
 		return host;
 	}
 
 	public Date getTime() {
 		return new Date(lastTime);
 	}
 
 	public List<EventContext> getTimeoutContexts() {
 		List<EventContext> l = Arrays.asList(timeoutQueue.toArray(new EventContext[0]));
 		Collections.sort(l, timeoutComparator);
 		return l;
 	}
 
 	public List<EventContext> getExpireContexts() {
 		List<EventContext> l = Arrays.asList(expireQueue.toArray(new EventContext[0]));
 		Collections.sort(l, expireComparator);
 		return l;
 	}
 
 	public int getTimeoutQueueLength() {
 		return timeoutQueue.size();
 	}
 
 	public int getExpireQueueLength() {
 		return expireQueue.size();
 	}
 
 	public void setTime(long now, boolean force) {
 		if (force || now > lastTime)
 			lastTime = now;
 
 		evictContext(lastTime);
 	}
 
 	public void add(EventContext ctx) {
 		synchronized (expireQueue) {
 			if (ctx.getExpireTime() != 0)
 				expireQueue.add(ctx);
 		}
 
 		synchronized (timeoutQueue) {
 			if (ctx.getTimeoutTime() != 0)
 				timeoutQueue.add(ctx);
 		}
 	}
 
 	public void updateTimeout(EventContext ctx) {
 		synchronized (timeoutQueue) {
 			// reorder
 			timeoutQueue.remove(ctx);
 			timeoutQueue.add(ctx);
 		}
 	}
 
 	public void remove(EventContext ctx) {
 		synchronized (expireQueue) {
 			expireQueue.remove(ctx);
 		}
 
 		synchronized (timeoutQueue) {
 			timeoutQueue.remove(ctx);
 		}
 	}
 
 	private void evictContext(long now) {
 		synchronized (expireQueue) {
 			while (true) {
 				EventContext ctx = expireQueue.peek();
 				if (ctx == null)
 					break;
 
 				if (ctx.getExpireTime() <= now) {
 					expireQueue.poll();
					storage.removeContext(ctx.getKey(), EventCause.EXPIRE);
 				} else
 					break;
 			}
 		}
 
 		synchronized (timeoutQueue) {
 			while (true) {
 				EventContext ctx = timeoutQueue.peek();
 				if (ctx == null)
 					break;
 
 				if (ctx.getTimeoutTime() <= now) {
 					timeoutQueue.poll();
					storage.removeContext(ctx.getKey(), EventCause.TIMEOUT);
 				} else
 					break;
 			}
 		}
 	}
 
 	@Override
 	public String toString() {
 		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
 		return host + " (timeout: " + timeoutQueue.size() + ", expire: " + expireQueue.size() + ") => "
 				+ df.format(new Date(lastTime));
 	}
 
 	private static class TimeoutComparator implements Comparator<EventContext> {
 		@Override
 		public int compare(EventContext o1, EventContext o2) {
 			return (int) (o1.getTimeoutTime() - o2.getTimeoutTime());
 		}
 	}
 
 	private static class ExpireComparator implements Comparator<EventContext> {
 		@Override
 		public int compare(EventContext o1, EventContext o2) {
 			return (int) (o1.getExpireTime() - o2.getExpireTime());
 		}
 	}
 }
