 //
 // typica - A client library for Amazon Web Services
 // Copyright (C) 2007 Xerox Corporation
 // 
 // Licensed under the Apache License, Version 2.0 (the "License");
 // you may not use this file except in compliance with the License.
 // You may obtain a copy of the License at
 //
 //     http://www.apache.org/licenses/LICENSE-2.0
 //
 // Unless required by applicable law or agreed to in writing, software
 // distributed under the License is distributed on an "AS IS" BASIS,
 // WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 // See the License for the specific language governing permissions and
 // limitations under the License.
 //
 
 package com.xerox.amazonws.sdb;
 
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import com.xerox.amazonws.common.AWSError;
 
 /**
  * This class handles threaded attribute fetching from SimpleDB.
  *
  * @author D. Kavanagh
  * @author developer@dotech.com
  */
 class AttrWorker implements Runnable {
     private static Log logger = LogFactory.getLog(AttrWorker.class);
 
 	private Item item;
 	private Counter running;
 	private Map<String, List<ItemAttribute>> results;
 	private ItemListener listener;
 
 	public AttrWorker(Item item, Counter running, Map<String, List<ItemAttribute>> results, ItemListener listener) {
 		this.item = item;
 		this.running = running;
 		this.results = results;
 		this.listener = listener;
 	}
 
 	public void run() {
 		boolean done = false;
 		while (!done) {
 			try {
 				if (results != null) {
 					results.put(item.getIdentifier(), item.getAttributes());
 				}
 				else if (listener != null) {
 					listener.itemAvailable(item.getIdentifier(), item.getAttributes());
 				}
 				done = true;
 			} catch (SDBException sdbex) {
 				AWSError err = sdbex.getErrors().get(0);
 				if (err.getCode().equals("NoSuchDomain")) {
 //					throw sdbex;
 				}
 			}
 			finally {
 				synchronized (running) {
 					running.decrement();
 				}
 			}
 		}
 	}
 }
