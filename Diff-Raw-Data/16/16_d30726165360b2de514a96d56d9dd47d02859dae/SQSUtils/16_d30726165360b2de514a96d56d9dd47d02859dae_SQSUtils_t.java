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
 
 package com.xerox.amazonws.sqs;
 
 import java.util.List;
 
 import org.apache.log4j.Logger;
 
 /**
  * This class provides helper methods to interact with the Amazon Simple Queue Service.
  *
  * @author D. Kavanagh
  * @author developer@dotech.com
  */
 public class SQSUtils {
     private static final Logger logger = Logger.getLogger(SQSUtils.class.getName());
     
 	/**
 	 * Returns a message queue for a given name, credentials. Uses the default SQS host.
 	 *
 	 * @param queueName name of the queue to connect to
 	 * @param accessKey AWS access id
 	 * @param secretKey AWS secret key
 	 * @return object representing the message queue
 	 */
 	public static MessageQueue connectToQueue(String queueName, String accessKey, String secretKey) 
 			throws SQSException {
 		return connectToQueue(null, queueName, accessKey, secretKey);
 	}
 
 	/**
 	 * Returns a message queue for a given name, credentials.
 	 *
 	 * @param serverName name of the host to use
 	 * @param queueName name of the queue to connect to
 	 * @param accessKey AWS access id
 	 * @param secretKey AWS secret key
 	 * @return object representing the message queue
 	 */
 	public static MessageQueue connectToQueue(String serverName, String queueName,
                                               String accessKey, String secretKey) 
 			throws SQSException {
 		// Create the service object
 		QueueService service = getQueueService(accessKey, secretKey, serverName);
 		 
 		// Retrieve the message queue object (by name).
 		return getMessageQueue(service, queueName);
 	}
 
 	/**
 	 * Create a QueueService object for a given URL.
 	 *
 	 * @param accessKey AWS access id
 	 * @param secretKey AWS secret key
 	 * @param serverName name of the host to use
 	 * @return object representing the queue service
 	 */
 	public static QueueService getQueueService(String accessKey, String secretKey, String serverName) 
 			throws SQSException {
 		QueueService service = null;
 		if (serverName != null) {
 			service = new QueueService(accessKey, secretKey, true, serverName);
 		}
 		else {
 			service = new QueueService(accessKey, secretKey);
 		}
 		if (service.getUrl() != null) {
			logger.debug( "Service: " + service.getUrl().toString() );
 		} else {
 			logger.error( "Service: null url!" );
 		}
 		return service;
 	}
 	
 	
 	/**
 	 * Looks for a queue by name: if found, return a MessageQuueue object for it.
 	 * Else, return null.
 	 *
 	 * @param service the queue service we're using
 	 * @param msgQueueName the name of the message queue to find, or create
 	 * @return object representing the message queue
 	 */
 	public static MessageQueue getMessageQueue(QueueService service, String msgQueueName)
 			throws SQSException {
 		MessageQueue msgQueue = null;
 		MessageQueue msgQueueFound = null;
		logger.debug("looking for queue : "+msgQueueName);
 		List<MessageQueue> msgQueuesFound = service.listMessageQueues( null );
 		for ( MessageQueue mq : msgQueuesFound ) {
			logger.debug("found : "+mq.getUrl());
 			if ( mq.getUrl().toString().endsWith( msgQueueName ) ) {
 				msgQueueFound = mq;
 			}
 		}
 		if (msgQueueFound == null) {
			logger.debug("Message queue couldn't be listed, going to create it.");
 			msgQueue = service.getOrCreateMessageQueue(msgQueueName.substring(msgQueueName.lastIndexOf("/")+1));
 		} else if (msgQueue == null) {
 			msgQueue = msgQueueFound;
 		}
 		if (msgQueue == null) {
 			logger.error( "Couldn't find message queue " + msgQueueName);
 		} else {
 			logger.info( "Using message queue resource at " + msgQueue.getUrl() ); 
 		}
 		return msgQueue;
 	}
 }
