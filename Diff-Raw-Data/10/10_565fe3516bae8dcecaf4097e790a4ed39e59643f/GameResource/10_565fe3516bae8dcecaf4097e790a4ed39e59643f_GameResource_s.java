 package com.github.webtictactoe.webtictactoe;
 
import com.github.webtictactoe.tictactoe.core.Game;
 import com.github.webtictactoe.tictactoe.core.GameSession;
 import com.github.webtictactoe.tictactoe.core.ILobby;
 import com.github.webtictactoe.tictactoe.core.Player;
 import java.util.HashMap;
import java.util.UUID;
import javax.ws.rs.Consumes;
 import javax.ws.rs.CookieParam;
 import org.atmosphere.annotation.Broadcast;
 import org.atmosphere.annotation.Suspend;
 import org.atmosphere.config.service.AtmosphereService;
 import org.atmosphere.jersey.JerseyBroadcaster;
 
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.Response;
 
@Path("/")
 @AtmosphereService(broadcaster = JerseyBroadcaster.class)
 public class GameResource {
     
     private static ILobby lobby = Lobby.INSTANCE.getLobby();
     private static HashMap<UUID, GameSession> gameSessionMap = new HashMap<UUID, GameSession>();
     private @CookieParam(value = "name") String name;
     
     @GET
     @Suspend(contentType = "application/json")
     @Path("/game/{id}")
     public String suspend(@PathParam(value = "id") UUID id) {
         if (gameSessionMap.containsKey(id)) {
             
         }
         System.out.println("Suspending response for " + name);
         return "";
     }
     
     /**
      * Finds an opponent, creates a game and a gamesession, maps that gamesession
      * to a new UUID and then finally returns that UUID.
      * 
      * @param size
      * @return a UUID that can be used to communicate with the matching gamesession.
      */
     @POST
     @Produces("application/json")
     @Path("/findgame/{size}")
     public Response findGame(@PathParam(value = "size") Integer size) {
         Player givenPlayer;
         
         // We get the first player matching the name (there should only ever be one anyway).
         for (Player player : lobby.getPlayerRegistry().getByName(name)) {
             // Take the first matching player.
             givenPlayer = player;
             
             // Find a game.
             GameSession gameSession = lobby.findGame(givenPlayer, size);
             
             // Generate a new UUID.
             UUID uuid = UUID.randomUUID();
             
             // Map the new gamesession to the new uuid.
             gameSessionMap.put(uuid, gameSession);
             
             // Return a response that contains the uuid.
             return Response.ok().entity(uuid).build();
         }
         
         // Player matching that name not found, something terribly wrong has occured!
         return Response.status(400).build();
     }
     
     @POST
     @Broadcast(writeEntity = false)
     @Consumes("application/json")
     @Produces("application/json")
     @Path("/game/{id}/move")
     public GameResponse broadcastGamestate(@PathParam(value = "id") String id, GameMessage gameMessage) {
         return new GameResponse();
     }
     
 }
