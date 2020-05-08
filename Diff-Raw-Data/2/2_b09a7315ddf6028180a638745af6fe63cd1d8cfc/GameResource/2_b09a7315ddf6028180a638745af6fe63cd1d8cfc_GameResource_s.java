 package predictem;
 
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 
 import org.atmosphere.annotation.Broadcast;
 import org.atmosphere.annotation.Suspend;
 import org.atmosphere.cpr.Broadcaster;
 import org.atmosphere.jersey.Broadcastable;
 
 
 @Path("/game/{id}")
 public class GameResource {
 	private @PathParam("id") Broadcaster game;
 	
 	@GET @Suspend(listeners={GameEventListener.class})
 	public Broadcastable subscribe() {
 		return new Broadcastable(game);
 	}
 	
 	@POST @Broadcast
 	public Broadcastable publish(String json) {
		return new Broadcastable(json + "\n", game);
     }
 }
