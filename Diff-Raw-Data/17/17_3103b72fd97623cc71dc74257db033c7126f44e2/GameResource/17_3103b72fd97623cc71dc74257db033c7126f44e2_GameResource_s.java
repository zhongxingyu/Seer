 package com.github.webtictactoe.webtictactoe;
 
 import com.github.webtictactoe.tictactoe.core.Game.Mark;
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
 import org.atmosphere.cpr.Broadcaster;
 import org.atmosphere.cpr.BroadcasterFactory;
 import org.atmosphere.jersey.Broadcastable;
 
 @Path("/game")
 @AtmosphereService(broadcaster = JerseyBroadcaster.class)
 public class GameResource {
     
     private static ILobby lobby = Lobby.INSTANCE.getLobby();
     private static HashMap<String, GameSession> gameSessionMap = new HashMap<String, GameSession>();
     // We know the name of the player of the request by the use of cookies!
     private @CookieParam(value = "name") String name;
     
     @GET
     @Suspend(contentType = "application/json")
     @Path("/{id}")
     public Broadcastable suspend(@PathParam(value = "id") Broadcaster id) {
         if (gameSessionMap.containsKey(id.getID())) {
             System.out.println("Given UUID matches a gamesession, request is OK");
         } else {
             System.out.println("Given UUID does NOT match a gamesession!");
         }
         
         System.out.println("Anyway, suspending response for " + name);
         return new Broadcastable("", id);
     }
     
     /**
      * Takes the first player that matches the name that is in the given cookie,
      * and calls the findGame method in the Lobby class in the model to create
      * a GameSession with that player and another player.
      * 
      * A random UUID is then generated, which is mapped to the new GameSession.
      * This UUID is then broadcast to /player/{name}, where {name} is the name
      * of either player.
      * 
      * Make sure to subscribe to /player/{name} in order to get the UUID which
      * can then be used to communicate with the new GameSession.
      * 
      * @param size
      * @return a UUID that can be used to communicate with the matching gamesession.
      */
     @POST
     @Path("/findgame/{size}")
     public Response findGame(@PathParam(value = "size") Integer size) {
         System.out.println("Trying to find a game for " + name + " of size " + size);
         // We get the first player matching the name (there should only ever be one anyway).
         for (Player player : lobby.getPlayerRegistry().getByName(name)) {
             // Take the first matching player.
             Player givenPlayer = player;
             
             // Find a game.
             GameSession gameSession = lobby.findGame(givenPlayer, size);
             System.out.println("mark of " + givenPlayer.getName() + ": " + gameSession.getMarkForPlayer(givenPlayer));
             if (gameSession != null) {
                 // Generate a new UUID.
                 UUID uuid = UUID.randomUUID();
 
                 // Map the new gamesession to the new uuid.
                 gameSessionMap.put(uuid.toString(), gameSession);
 
                 // Broadcast the UUID to the suspended requests that match both players names.
                 BroadcasterFactory.getDefault().lookup(gameSession.getPlayerOne().getName()).broadcast(new UUIDMessage(uuid.toString(), size));
                 BroadcasterFactory.getDefault().lookup(gameSession.getPlayerTwo().getName()).broadcast(new UUIDMessage(uuid.toString(), size));
                 
                 // Simply return an OK response, the UUID is broadcast to both relevant players above.
                 return Response.ok().build();
             }
         }
         // Player matching that name not found, something terribly wrong has occured!
         return Response.status(400).build();
     }
     
     @POST
     @Broadcast(writeEntity = false)
     @Consumes("application/json")
     @Produces("application/json")
     @Path("/{id}")
    public Response broadcastGamestate(@PathParam(value = "id") UUID id, GameMessage gameMessage) {
        GameSession gameSession = gameSessionMap.get(id.toString());
         
         System.out.println("Got a gameMessage: " + gameMessage);
         
         // We get the first player matching the name (there should only ever be one anyway).
         for (Player player : lobby.getPlayerRegistry().getByName(name)) {
             // Take the first matching player.
             Player givenPlayer = player;
             System.out.println("mark of " + givenPlayer.getName() + ": " + gameSession.getMarkForPlayer(givenPlayer));
             // Try to make a move to the given position with the given player.
             Boolean successfulMove = gameSession.move(gameMessage.xPos, gameMessage.yPos, givenPlayer);
             
             // If the move is successful, we convert the new gameboard to a response-friendly format.
             if (successfulMove) {
                 // The move was successful, so we return an OK response together with the state of the board, and the name
                 // of the player who played last. We also return true if the game was won, or false if it was not.
                 // If the game was won, the given name will match to the winning player.
                return Response
                        .ok()
                        .entity(new GameResponse(givenPlayer.getName(), gameSession.getBoard(), gameSession.gameWon()))
                        .build();
             }
         }
         
        // Something went wrong, so we simply return the 400 status code.
        return Response
                .status(400)
                .build();
     }
     
 }
