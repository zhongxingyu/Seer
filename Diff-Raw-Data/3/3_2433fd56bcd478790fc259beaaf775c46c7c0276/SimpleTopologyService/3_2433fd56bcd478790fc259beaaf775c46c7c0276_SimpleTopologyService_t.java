 package amp.rabbit.topology;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Map;
 
 import amp.messaging.EnvelopeHeaderConstants;
 
 public class SimpleTopologyService implements ITopologyService {
 
 	protected static long QUEUE_NUMBER = 0;
 	
 	Exchange exchangePrototype = Exchange.builder()
 			.name("amq.direct").declare(false).build();
 	
 	Queue queuePrototype = Queue.builder()
 			.isAutoDelete(true).isDurable(false).isExclusive(true).declare(true).build();
 	
 	String clientProfile;
 	ArrayList<Broker> brokers = new ArrayList<Broker>();
 	
 	public SimpleTopologyService(){}
 	
     public SimpleTopologyService(String clientProfile, Broker... brokers) {
 
         this(clientProfile, Arrays.asList(brokers));
     }
     
     public SimpleTopologyService(String clientProfile, Collection<Broker> brokers) {
 
         this.clientProfile = clientProfile;
         this.setBrokers(brokers);
     }
 
     public void setExchangePrototype(Exchange exchangePrototype) {
 		this.exchangePrototype = exchangePrototype;
 	}
 
 	public void setQueuePrototype(Queue queuePrototype) {
 		this.queuePrototype = queuePrototype;
 	}
 
 	public void setClientProfile(String clientProfile) {
 		this.clientProfile = clientProfile;
 	}
 
 	public void setBrokers(Collection<Broker> brokers) {
 		this.brokers.addAll(brokers);
 	}
 
 	public Exchange getExchangePrototype(){
 		
 		return this.exchangePrototype;
 	}
 	
 	public Queue getQueuePrototype(){
 		
 		return this.queuePrototype;
 	}
 	
 	@Override
     public RoutingInfo getRoutingInfo(Map<String, String> headers) {
     		
         String topic = headers.get(EnvelopeHeaderConstants.MESSAGE_TOPIC);
 
         Exchange targetExchange = TopologyUtils.clone(exchangePrototype);
         
         Queue targetQueue = TopologyUtils.clone(queuePrototype);
         targetQueue.setName(buildIdentifiableQueueName(topic));
         
         ProducingRoute producingRoute = ProducingRoute.builder()
         		.exchange(targetExchange).brokers(brokers).routingKeys(topic).build();
         
         ConsumingRoute consumingRoute = ConsumingRoute.builder()
         		.exchange(targetExchange).queue(targetQueue).brokers(brokers).routingkeys(topic).build();
         
         return new RoutingInfo(Arrays.asList(producingRoute), 
         		Arrays.asList(consumingRoute));
     }
 
     public String buildIdentifiableQueueName(String topic){
     	
     		return String.format("%s#%03d#%s", clientProfile, ++QUEUE_NUMBER, topic);
     }
     
     @Override
    public void dispose() {}   
     
 }
