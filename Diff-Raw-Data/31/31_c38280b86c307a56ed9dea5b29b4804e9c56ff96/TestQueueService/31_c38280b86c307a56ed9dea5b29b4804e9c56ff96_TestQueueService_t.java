 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Properties;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 //import com.xerox.amazonws.sqs.Grant;
 import com.xerox.amazonws.sqs.Message;
 import com.xerox.amazonws.sqs.MessageQueue;
 import com.xerox.amazonws.sqs.QueueService;
 import com.xerox.amazonws.sqs.SQSException;
 
 public class TestQueueService {
     private static Log logger = LogFactory.getLog(TestQueueService.class);
 
 	public static void main(String [] args) throws Exception {
 		Properties props = new Properties();
		props.load(TestQueueService.class.getClassLoader().getResourceAsStream("aws.properties"));
 
		QueueService qs = new QueueService(props.getProperty("aws.accessId"), props.getProperty("aws.secretKey"));
 /*
 	*/
 		List<MessageQueue> queues = qs.listMessageQueues(null);
 		for (MessageQueue queue : queues) {
 			logger.info("Queue : "+queue.getUrl().toString());
 			// delete queues that contain a certain phrase
 			if (queue.getUrl().toString().indexOf("delete")>-1) {
 				try {
 					queue.deleteQueue();
 				} catch (SQSException ex) {
 					ex.printStackTrace();
 				}
 			}
 		}
 		for (int i=0; i<args.length; i++) {
 			MessageQueue mq = qs.getOrCreateMessageQueue(args[i]);
 /* test send/receive
 */
 			ArrayList<String> msgids = new ArrayList<String>();
 			for (int j=0; j<5; j++) {
 				String msgid = mq.sendMessage("Testing 1, 2, 3");
 				msgids.add(msgid);
 				logger.info("send message, id = "+msgid);
 			}
 			for (int j=0; j<5; j++) {
 				Message msg = mq.receiveMessage();
 				if (msg == null) { continue; }
 				msgids.remove(msg.getMessageId());
 				logger.info("Message "+(j+1)+" = "+msg.getMessageBody());
 				mq.deleteMessage(msg);
 			}
 			logger.info("messages not received : "+msgids.size());
 /* test grants
 			logger.info("Grants for "+mq.getUrl());
 			Grant [] grants = mq.listGrants(null, null);
 			for (Grant g : grants) {
 				logger.info("grant : "+g.getGrantee()+" perm : "+g.getPermission());
 			}
 			logger.info("Adding Grant");
 			mq.addGrantByEmailAddress("dak@directthought.com", "ReceiveMessage");
 			logger.info("Grants for "+mq.getUrl());
 			grants = mq.listGrants(null, null);
 			for (Grant g : grants) {
 				logger.info("grant : "+g.getGrantee()+" perm : "+g.getPermission());
 			}
 			logger.info("Removing Grant");
 			mq.removeGrantByEmailAddress("xrxs33@gmail.com", "ReceiveMessage");
 			logger.info("Grants for "+mq.getUrl());
 			grants = mq.listGrants(null, null);
 			for (Grant g : grants) {
 				logger.info("grant : "+g.getGrantee()+" perm : "+g.getPermission());
 			}
 */
 		}
 		/* test forced deletion
 		*/
 		MessageQueue mq = qs.getOrCreateMessageQueue("deleteTest-12345");
 		for (int j=0; j<10; j++) {	// throw 10 messages in the queue
 			mq.sendMessage("Testing 1, 2, 3");
 		}
 		logger.info("visibility timeout = "+mq.getVisibilityTimeout());
 		logger.info("approximate queue size = "+mq.getApproximateNumberOfMessages());
 		try {
 			mq.deleteQueue();
 			logger.error("Queue deletion was allowed, even with messages and force=false !!");
 		} catch (SQSException ex) {
 			logger.info("queue can't be deleted (this is exptected)");
 		}
 		try {
 			mq.deleteQueue(true);
 			logger.info("queue deleted with force=true (this is exptected)");
 		} catch (SQSException ex) {
 			logger.error("Queue deletion failed (this is not exptected) !!");
 		}
 	}
 }
