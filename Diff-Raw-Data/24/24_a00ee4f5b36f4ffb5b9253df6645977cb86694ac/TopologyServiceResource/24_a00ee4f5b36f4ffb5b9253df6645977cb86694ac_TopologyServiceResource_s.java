 package amp.topology.resources;
 
 import java.util.HashMap;
 
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.MediaType;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.security.core.userdetails.UserDetails;
 
 
 
import amp.bus.rabbit.topology.ITopologyService;
import amp.bus.rabbit.topology.RoutingInfo;
 
 import cmf.bus.EnvelopeHeaderConstants;
 
 import com.yammer.dropwizard.auth.Auth;
 import com.yammer.metrics.annotation.Timed;
 
 @Path("/topology")
 @Produces(MediaType.APPLICATION_JSON)
 public class TopologyServiceResource {
 
 	private static final Logger logger = LoggerFactory.getLogger(TopologyServiceResource.class);
 	
 	private ITopologyService topologyService;
 	
 	public TopologyServiceResource(ITopologyService topologyService){
 		
 		this.topologyService = topologyService;
 	}
 	
 	@GET
 	@Timed
 	@Path("/get-routing-info/{topic}")
 	public RoutingInfo getRoutingInfo(@Auth UserDetails client, @PathParam("topic") String topic){
 		
 		HashMap<String, String> routingHints = new HashMap<String, String>();
 		
 		logger.info("Getting Routing Info for client '{}' for topic '{}'.", client.getUsername(), topic);
 		
 		routingHints.put(EnvelopeHeaderConstants.MESSAGE_SENDER_IDENTITY, client.getUsername());
 		
 		routingHints.put(EnvelopeHeaderConstants.MESSAGE_TOPIC, topic);
 		
 		return this.topologyService.getRoutingInfo(routingHints);
 	}
 }
