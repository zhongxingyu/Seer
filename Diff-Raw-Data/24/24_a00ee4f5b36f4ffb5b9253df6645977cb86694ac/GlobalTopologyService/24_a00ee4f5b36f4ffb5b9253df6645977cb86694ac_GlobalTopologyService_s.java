 package amp.topology.core;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Map;
 
import amp.bus.rabbit.topology.Exchange;
import amp.bus.rabbit.topology.RouteInfo;
import amp.bus.rabbit.topology.RoutingInfo;
import amp.bus.rabbit.topology.ITopologyService;
 import cmf.bus.EnvelopeHeaderConstants;
 
 
 
 public class GlobalTopologyService implements ITopologyService {
 
 	ITopologyRepository topologyRepository;
 	
 	public GlobalTopologyService(ITopologyRepository topologyRepository){
 	
 		this.topologyRepository = topologyRepository;
 	}
 
 	@Override
 	public RoutingInfo getRoutingInfo(Map<String, String> routingHints) {
 		
 		String topic = routingHints.get(EnvelopeHeaderConstants.MESSAGE_TOPIC);
 		String client = routingHints.get(EnvelopeHeaderConstants.MESSAGE_SENDER_IDENTITY);
 		
 		ArrayList<RouteInfo> routeInfos = new ArrayList<RouteInfo>();
 		
 		Collection<ExtendedRouteInfo> extendedRouteInfos = this.topologyRepository.find(topic, client);
 		
 		for (ExtendedRouteInfo extendedRouteInfo : extendedRouteInfos){
 			
 			Exchange producerExchange = 
 				this.topologyRepository.getExchange(
 					extendedRouteInfo.getProducerExchangeId())
 						.toExchange();
 			
 			ExtendedExchange consumerExtendedExchange =
 				this.topologyRepository.getExchange(
 					extendedRouteInfo.getConsumerExchangeId());
 			
 			Exchange consumerExchange = consumerExtendedExchange.toExchange();
 			
 			routeInfos.add(new RouteInfo(producerExchange, consumerExchange));
 		}
 		
 		return new RoutingInfo(routeInfos);
 	}
 
 	@Override
 	public void dispose() {}
 	
 }
