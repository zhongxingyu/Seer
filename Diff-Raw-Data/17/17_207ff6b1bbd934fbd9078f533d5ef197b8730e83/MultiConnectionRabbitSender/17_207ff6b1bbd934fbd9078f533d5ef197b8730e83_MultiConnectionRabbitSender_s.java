 package amp.rabbit.transport;
 
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import cmf.bus.Envelope;
 import cmf.bus.IDisposable;
 
 import com.rabbitmq.client.AMQP;
 import com.rabbitmq.client.Channel;
 
 import amp.rabbit.connection.IConnectionManager;
 import amp.rabbit.connection.IRabbitConnectionFactory;
 import amp.rabbit.topology.Exchange;
 import amp.rabbit.topology.ProducingRoute;
 import amp.rabbit.topology.RoutingInfo;
 
 
 /**
  * This abstracts the publishing of information to multiple channels & brokers,
  * managing the connections and fanout of the distribution.
  * 
  * @author jmccune
  */
 public class MultiConnectionRabbitSender implements IDisposable  {
 
 	private static final Logger LOG = LoggerFactory.getLogger(MultiConnectionRabbitSender.class);
 	
 	/** The factory that provides us our connections & channels */
 	private IRabbitConnectionFactory _connectionFactory;
 	
 	public MultiConnectionRabbitSender(IRabbitConnectionFactory factory) {
		
		
 	}
 	
 	public void send(RoutingInfo routing, Envelope envelope)  throws Exception {
 		LOG.debug("Enter Send");
 		
 		// for each exchange, send the envelope
 		for (ProducingRoute route : routing.getProducingRoutes()) {
 		
 			try {
 				
 				Collection<IConnectionManager> connectionManagers = _connectionFactory.getConnectionManagersFor(route);
 						
 				for (IConnectionManager mgr : connectionManagers) {
 					Channel channel 	 = mgr.createChannel();
 					sendDataOnChannel(channel,route, envelope);
 				}			
 			} catch (Exception e) {
 				LOG.error("Failed to send an envelope", e);
 				throw e;
 			}
 		}
 
 		LOG.debug("Leave Send");
 
 	}
 	
 	protected void sendDataOnChannel(Channel channel,ProducingRoute route,  Envelope envelope) {
 		
 		try { 
 			AMQP.BasicProperties props = new AMQP.BasicProperties.Builder().build();
 
 			//Convert Headers from String,String to String,Object Map.
 			Map<String, Object> headers = new HashMap<String, Object>();
 			for (Map.Entry<String, String> entry : envelope.getHeaders()
 					.entrySet()) {
 				headers.put(entry.getKey(), entry.getValue());
 			}
 
 			//Set the properties
 			props.setHeaders(headers);
 
 			Exchange ex = route.getExchange();
 			Collection<String> keys = route.getRoutingKeys();
 
			channel.exchangeDeclare(ex.getName(), ex.getExchangeType(),
					ex.isDurable(), ex.isAutoDelete(), ex.getArguments());
 
 			// For the channel, publish the topic...
 			for (String key : keys) {
 				channel.basicPublish(ex.getName(), key, props, envelope.getPayload());
 			}
 		} catch (Exception x) {
 			LOG.error("Error attempting to send envelope: ", x);
 		}
 	}
 
 	@Override
 	public void dispose() {
 		//Shoudl I dispose of hte factory...??
 		
 		try { _connectionFactory.dispose(); } catch(Exception x) {}
 	}
 }
