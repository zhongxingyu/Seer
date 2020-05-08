 package amp.topology.client;
 
 import java.util.Map;
 import java.util.concurrent.TimeUnit;
 
 import amp.bus.rabbit.topology.ITopologyService;
 import amp.bus.rabbit.topology.RoutingInfo;
 
 import cmf.bus.EnvelopeHeaderConstants;
 
 import com.google.common.cache.Cache;
 import com.google.common.cache.CacheBuilder;
 
 /**
  * An implementation of ITopologyService that utilizes a central
  * configuration and management system for topology, called
  * the "Global Topology Service".  This implementation relies on 
  * an external source to provide routing info.  If the routing info
  * is found, it is cached for some interval.  If not, the Service will
  * default to a "fallback" configuration.
  * 
  * @author Richard Clayton (Berico Technologies)
  */
 public class GlobalTopologyService implements ITopologyService {
 
 	public static long CACHE_EXPIRY_TIME_IN_SECONDS = 1000;
 	
 	Cache<String, RoutingInfo> routingInfoCache;
 	
 	IRoutingInfoRetriever routingInfoRetriever;
 	
 	FallbackRoutingInfoProvider fallbackProvider = null;
 	
 	public GlobalTopologyService(IRoutingInfoRetriever routingInfoRetriever){
 		
 		this(routingInfoRetriever, CACHE_EXPIRY_TIME_IN_SECONDS);
 	}
 	
 	public GlobalTopologyService(
 		IRoutingInfoRetriever routingInfoRetriever, 
 		long cacheExpiryTime){
 		
 		this.routingInfoRetriever = routingInfoRetriever;
 		
 		this.routingInfoCache = 
 			CacheBuilder
 				.newBuilder()
 				.expireAfterWrite(cacheExpiryTime, TimeUnit.SECONDS)
 				.build();
 	}
 	
 	public GlobalTopologyService(
 		IRoutingInfoRetriever routingInfoRetriever, 
 		long cacheExpiryTime, 
 		FallbackRoutingInfoProvider fallbackProvider){
 		
 		this(routingInfoRetriever, cacheExpiryTime);
 		this.fallbackProvider = fallbackProvider;
 	}
 
 	@Override
 	public RoutingInfo getRoutingInfo(Map<String, String> routingHints) {
 		
 		String topic = routingHints.get(EnvelopeHeaderConstants.MESSAGE_TOPIC);
 		
 		RoutingInfo routingInfo = this.routingInfoCache.getIfPresent(topic);
 		
 		if (routingInfo == null){
 			
 			routingInfo = this.routingInfoRetriever.retrieveRoutingInfo(topic);
 			
			if (routingInfo == null && this.fallbackProvider != null) {
 				
 				routingInfo = this.fallbackProvider.getFallbackRoute(topic);
 			}
 			
 			// If the RoutingInfo is still null, end this program's life!
 			if (routingInfo == null){
 				
 				throw new RoutingInfoNotFoundException(topic);
 			}
 			
 			this.routingInfoCache.put(topic, routingInfo);
 		}
 		
 		return routingInfo;
 	}
 
 	@Override
 	public void dispose() {}
 }
