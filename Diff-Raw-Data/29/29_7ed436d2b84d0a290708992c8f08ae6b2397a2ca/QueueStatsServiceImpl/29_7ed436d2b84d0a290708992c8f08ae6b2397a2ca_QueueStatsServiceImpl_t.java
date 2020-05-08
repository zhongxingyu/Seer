 /*
  * Copyright (C) 2013 The Zorba Open Source Project
  * 
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
  * in compliance with the License. You may obtain a copy of the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software distributed under the License
  * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
  * or implied. See the License for the specific language governing permissions and limitations under
  * the License.
  */
 package com.bbytes.zorba.messaging.rabbitmq.service;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 
 import com.bbytes.zorba.messaging.IQueueStatsService;
 import com.bbytes.zorba.messaging.rabbitmq.domain.Queue;
 
 /**
  * 
  * 
  * @author Thanneer
  * 
  * @version 0.0.1
  */
 @Service
 public class QueueStatsServiceImpl implements IQueueStatsService {
 
 	@Autowired
 	private IQueueServiceRest queueServiceRest;
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.bbytes.zorba.messaging.stats.IQueueStats#getQueueSize(java.lang.String)
 	 */
 	@Override
 	public long getQueueMessageSize(String queueName) {
 		Queue queue = queueServiceRest.getQueue(getVHost(), queueName);
 		if (queue != null)
 			return queue.getMessages();
 
 		return 0;
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.bbytes.zorba.messaging.stats.IQueueStats#getTotalQueueSize()
 	 */
 	@Override
 	public long getTotalQueueSize() {
 		List<Queue> queueList = queueServiceRest.getQueues();
 		if (queueList != null)
 			return queueServiceRest.getQueues().size();
 
 		return 0;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.bbytes.zorba.messaging.stats.IQueueStats#getPendingRequestSize()
 	 */
 	@Override
 	public long getPendingRequestSize() {
 		long totalRequestSize = 0;
 		List<Queue> queueList = queueServiceRest.getQueues();
 		if (queueList != null) {
 			for (Queue queue : queueList)
 				totalRequestSize = totalRequestSize + queue.getMessagesReady();
 		}
 
 		return totalRequestSize;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.bbytes.zorba.messaging.stats.IQueueStats#getQueueNames()
 	 */
 	@Override
 	public List<String> getQueueNames() {
 		List<String> queueNameList = new ArrayList<String>();
 		List<Queue> queueList = queueServiceRest.getQueues();
 		if (queueList != null) {
 			for (Queue queue : queueList)
 				queueNameList.add(queue.getName());
 		}
 
 		return queueNameList;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.bbytes.zorba.messaging.stats.IQueueStats#getQueueSystemType()
 	 */
 	@Override
 	public String getQueueSystemType() {
 		return "RabbitMQ";
 	}
 
 	/**
 	 * Returns default vhost for rabbitmq. Need to improve this method by taking the vhost thru
 	 * configuration
 	 * 
 	 * @return
 	 */
 	public String getVHost() {
		return "%2F";
 	}
 
 }
