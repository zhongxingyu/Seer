 package burrito.test;
 
 import java.util.Arrays;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import junit.framework.Assert;
 
 import org.junit.Test;
 
 import com.google.appengine.api.taskqueue.QueueFactory;
 import com.google.appengine.api.taskqueue.dev.LocalTaskQueue;
 import com.google.appengine.api.taskqueue.dev.QueueStateInfo;
 import com.google.appengine.tools.development.testing.LocalTaskQueueTestConfig;
 
 import burrito.BurritoRouter;
 import burrito.services.FeedsSubscription;
 import burrito.services.FeedsSubscriptionMessage;
 
 
 public class BroadcastTest extends TestBase {
 
 	
 	@Test
 	public void testPush() {
 		FeedsSubscription sub1 = new FeedsSubscription();
 		sub1.setChannelId("foo");
 		sub1.setClientId("foo_client");
 		sub1.setFeedIds(Arrays.asList("feed-x"));
 		sub1.setCreated(new Date());
 		sub1.insert();
 		
 		FeedsSubscription sub2 = new FeedsSubscription();
 		sub2.setChannelId("bar");
 		sub2.setClientId("bar_client");
 		sub2.setFeedIds(Arrays.asList("feed-x"));
 		sub2.setCreated(new Date());
 		sub2.insert();
 		
 		FeedsSubscription sub3 = new FeedsSubscription(); //a poll subscription
 		sub3.setClientId("poll_client");
 		sub3.setFeedIds(Arrays.asList("feed-x"));
 		sub3.setCreated(new Date());
 		sub3.insert();
 		
 		FeedsSubscription sub4 = new FeedsSubscription(); //also a poll subscription, but for a different feed
 		sub4.setClientId("poll_client");
 		sub4.setFeedIds(Arrays.asList("feed-bb"));
 		sub4.setCreated(new Date());
 		sub4.insert();
 		
 		
 		Map<String, String[]> params = new HashMap<String, String[]>();
 		params.put("message", new String[]{"test-message"});
 		params.put("secret", new String[]{"foo"});
 		@SuppressWarnings("unchecked")
 		Map<String, String> result = (Map<String, String>) TestUtils.runController("/burrito/feeds/feed-x/broadcast", params, BurritoRouter.class);
 		Assert.assertEquals("ok", result.get("status"));	
 		
 		
 		//broadcast another message
 		TestUtils.runController("/burrito/feeds/feed-x/broadcast", params, BurritoRouter.class);
 		
 		List<FeedsSubscriptionMessage> delayeds = FeedsSubscriptionMessage.fetchBySubscriptionId(sub3.getId());
 		Assert.assertEquals(2, delayeds.size());
 		Assert.assertEquals("feed-x", delayeds.get(0).getFeedId());
 		
 		
 		delayeds = FeedsSubscriptionMessage.fetchBySubscriptionId(sub4.getId());
 		Assert.assertEquals(0, delayeds.size());
 		
 		
 		
 	}
 	
 	@Test
 	public void testPushAsync() {
 		//Run an async broadcast
 		
 		FeedsSubscription sub1 = new FeedsSubscription();
 		sub1.setChannelId("foo");
 		sub1.setClientId("foo_client");
 		sub1.setFeedIds(Arrays.asList("feed-x"));
 		sub1.setCreated(new Date());
 		sub1.insert();
 		
 		FeedsSubscription sub2 = new FeedsSubscription();
 		sub2.setChannelId("bar");
 		sub2.setClientId("bar_client");
 		sub2.setFeedIds(Arrays.asList("feed-x"));
 		sub2.setCreated(new Date());
 		sub2.insert();
 		
 		
 		
 		Map<String, String[]> params = new HashMap<String, String[]>();
 		params.put("message", new String[]{"test-message"});
 		params.put("secret", new String[]{"foo"});
 		@SuppressWarnings("unchecked")
 		Map<String, String> result = (Map<String, String>) TestUtils.runController("/burrito/feeds/feed-x/broadcast/async", params, BurritoRouter.class);
 		Assert.assertEquals("ok", result.get("status"));	
 		
 		LocalTaskQueue ltq = LocalTaskQueueTestConfig.getLocalTaskQueue();
		QueueStateInfo qsi = ltq.getQueueStateInfo().get(QueueFactory.getQueue("burrito-broadcast").getQueueName());
         Assert.assertEquals(1, qsi.getTaskInfo().size());        
 	}
 	
 	
 	
 }
