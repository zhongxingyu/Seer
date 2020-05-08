 package com.dgrid.test.helpers;
 
 import com.dgrid.helpers.SQSHelper;
 import com.dgrid.test.BaseTestCase;
 import com.xerox.amazonws.sqs2.Message;
 
 public class SQSHelperTestCase extends BaseTestCase {
 
 	public void testSQSHelper() throws Exception {
 		String queueName = String.format(
 				"1234567890-abcdefghijklm-test-queue-%1$d", System
 						.currentTimeMillis());
 		String msgBody = "Hello, world";
 		SQSHelper sqs = (SQSHelper) super.getBean(SQSHelper.NAME);
 		// create a queue?
 		sqs.getMessageQueue(queueName);
 		String msgid = sqs.send(queueName, msgBody);
 		assertNotNull(msgid);
 
 		// sleep for a bit
		Thread.sleep(1000);
 
 		// check queue size
 		int queueSize = sqs.getQueueSize(queueName);
 		assertEquals(queueSize, 1);
 
 		// receive
 		Message msg = sqs.receive(queueName);
 		try {
 			assertEquals(msg.getMessageBody(), msgBody);
 		} finally {
 			// delete the message
 			sqs.delete(queueName, msg);
 			// delete the queue
 			sqs.deleteMessageQueue(queueName);
 		}
 	}
 }
