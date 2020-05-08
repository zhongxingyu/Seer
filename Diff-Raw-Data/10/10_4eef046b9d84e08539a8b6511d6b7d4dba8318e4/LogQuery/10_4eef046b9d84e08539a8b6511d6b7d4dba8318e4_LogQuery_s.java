 /*
  * Copyright 2013 Eediom Inc.
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
 package org.araqne.logdb.client;
 
 import java.util.concurrent.CopyOnWriteArrayList;
 
 public class LogQuery {
 	private int id;
 	private String queryString;
 	private String status;
 	private long loadedCount;
 	private CopyOnWriteArrayList<WaitingCondition> waitingConditions;
 
 	public LogQuery(int id, String queryString) {
 		this.id = id;
 		this.queryString = queryString;
 		this.status = "Stopped";
 		this.loadedCount = 0;
 		this.waitingConditions = new CopyOnWriteArrayList<WaitingCondition>();
 	}
 
 	public long getLoadedCount() {
 		return loadedCount;
 	}
 
 	public void waitUntil(Long count) {
 		WaitingCondition cond = new WaitingCondition(count);
 		try {
 			waitingConditions.add(cond);
 			synchronized (cond.signal) {
 				try {
					cond.signal.wait();
 				} catch (InterruptedException e) {
 				}
 			}
 		} finally {
 			waitingConditions.remove(cond);
 		}
 	}
 
 	public void updateCount(long count) {
 		loadedCount = count;
 
 		for (WaitingCondition cond : waitingConditions) {
 			if (cond.threshold != null && cond.threshold <= loadedCount) {
 				synchronized (cond.signal) {
 					cond.signal.notifyAll();
 				}
 			}
 		}
 	}
 
 	public void updateStatus(String status) {
 		this.status = status;
 		if (status.equals("Ended")) {
 			for (WaitingCondition cond : waitingConditions) {
 				synchronized (cond.signal) {
 					cond.signal.notifyAll();
 				}
 			}
 		}
 	}
 
 	private class WaitingCondition {
 		private Long threshold;
 		private Object signal = new Object();
 
 		public WaitingCondition(Long threshold) {
 			this.threshold = threshold;
 		}
 	}
 
 	@Override
 	public String toString() {
 		return "id=" + id + ", query=" + queryString + ", status=" + status + ", loaded=" + loadedCount;
 	}
 
 }
