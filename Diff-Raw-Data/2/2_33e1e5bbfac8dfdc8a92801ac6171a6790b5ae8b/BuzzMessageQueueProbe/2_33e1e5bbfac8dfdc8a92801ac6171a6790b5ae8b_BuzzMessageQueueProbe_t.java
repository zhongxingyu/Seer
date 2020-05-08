 package org.atlasapi.tracking;
 
 import org.atlasapi.tracking.twitter.QueueingTweetProcessor;
 
 import com.metabroadcast.common.webapp.health.HealthProbe;
 import com.metabroadcast.common.webapp.health.ProbeResult;
 
 public class BuzzMessageQueueProbe implements HealthProbe  {
 
 	private final QueueingTweetProcessor queue;
 
 	public BuzzMessageQueueProbe(QueueingTweetProcessor queue) {
 		this.queue = queue;
 	}
 	
 	@Override
 	public ProbeResult probe() {
 		ProbeResult result = new ProbeResult(title());
 		result.addInfo("queue size", queue.queueSize() + " messages");
		result.addInfo("active threads", queue.activeThreads() + " threads");
 		return result;
 	}
 
 	@Override
 	public String title() {
 		return "Tweet queue";
 	}
 }
