 package amp.gel.service;
 
 import java.util.HashMap;
 import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
 
 import cmf.bus.Envelope;
 import cmf.bus.EnvelopeHeaderConstants;
 import cmf.bus.IEnvelopeFilterPredicate;
 import cmf.bus.IRegistration;
 
 /**
  * Counts the # of envelopes that have been received with a specific topic.
  * 
  */
 public class TopicCounter implements IRegistration {
 
 	private String topic;
 
 	public TopicCounter(String topic) {
 		super();
 		this.topic = topic;
 	}
 
	private AtomicLong count = new AtomicLong();
 
 	public long getCount() {
		return count.longValue();
 	}
 
 	public IEnvelopeFilterPredicate getFilterPredicate() {
 		return null;
 	}
 
 	public Map<String, String> getRegistrationInfo() {
 		Map<String, String> info = new HashMap<String, String>();
 		info.put(EnvelopeHeaderConstants.MESSAGE_TOPIC, topic);
 		return info;
 	}
 
 	public Object handle(Envelope envelope) throws Exception {
		count.incrementAndGet();
 		return true;
 	}
 
 	public Object handleFailed(Envelope envelope, Exception ex)
 			throws Exception {
 		return ex;
 	}
 
 }
