 package com.github.webtictactoe.webtictactoe.game;
 
 import com.github.webtictactoe.tictactoe.core.GameSession;
 import com.github.webtictactoe.tictactoe.core.ILobby;
 import com.github.webtictactoe.tictactoe.core.Player;
 import com.github.webtictactoe.webtictactoe.lobby.Lobby;
 import java.util.HashMap;
 import java.util.UUID;
 import javax.ws.rs.Consumes;
 import javax.ws.rs.CookieParam;
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
 
 /**
  * This is the resource that takes care of actually finding and playing a game of tictactoe.
  * @author pigmassacre
  */
 @Path("/game")
 @AtmosphereService(broadcaster = JerseyBroadcaster.class)
 public class GameResource {
     
     private static ILobby lobby = Lobby.INSTANCE.getLobby();
     
     // We use this to map UUID's to gameSessions, so we know what gameSession to handle depending on the UUID given
     // in a request.
     private static HashMap<String, GameSession> gameSessionMap = new HashMap<String, GameSession>();
     
     // We know the name of the player of the request by the use of cookies!
     private @CookieParam(value = "name") String name;
     
     /**
      * Suspends a client to the broadcaster with id 'id'.
      * @param id
      * @return 
      */
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
             if (gameSession != null) {
                 // Generate a new UUID.
                 UUID uuid = UUID.randomUUID();
 
                 // Map the new gamesession to the new uuid.
                 gameSessionMap.put(uuid.toString(), gameSession);
                 
                 System.out.println("Found game for " + gameSession.getPlayerOne() + " and " + gameSession.getPlayerTwo());
 
                 // Broadcast the UUID to the suspended requests that match both players names.
                BroadcasterFactory.getDefault().lookup(gameSession.getPlayerOne().getName()).broadcast(new UUIDMessage(uuid.toString(), size));
                BroadcasterFactory.getDefault().lookup(gameSession.getPlayerTwo().getName()).broadcast(new UUIDMessage(uuid.toString(), size));
                 
                 // Simply return an OK response, the UUID is broadcast to both relevant players above.
                 System.out.println("Game found, returning statuscode 200.");
                 return Response.ok().build();
             }
         }
         // Player matching that name not found, something terribly wrong has occured!
         System.out.println("Game not found, returning statuscode 400.");
         return Response.status(400).build();
     }
     
     /**
      * This is where player movements are taken into consideration. 
      * Broadcasts the state of the game to all clients suspended to the Broadcaster id.
      * 
      * Returns http status code 200 if the request is OK (i.e. the playername matches
      * the player whos turn it is, and the x and y-pos match an empty state of the board).
      * 
      * Else returns http status code 400.
      * 
      * @param id the Broadcaster to be used to broadcast the state of the game
      * @param gameMessage a JSON object marshaled into a GameMessage object that contains
      * the data with which to make a move with
      * @return a Response object
      */
     @POST
     @Consumes("application/json")
     @Produces("application/json")
     @Path("/{id}")
     public Response broadcastGamestate(@PathParam(value = "id") Broadcaster id, GameMessage gameMessage) {
         GameSession gameSession = gameSessionMap.get(id.getID());
         
         System.out.println("For game with UUID " + id.getID() + ", got a gameMessage: " + gameMessage);
         
         if (gameSession == null) {
             System.out.println("This UUID does not match any gameSession.");
             return Response.status(400).build();
         }
         
         if (gameSession.getWinner() != null) {
             System.out.println("A winner has already been decided.");
             return Response.status(400).build();
         }
         
         // We get the first player matching the name (there should only ever be one anyway).
         for (Player player : lobby.getPlayerRegistry().getByName(name)) {
             // Take the first matching player.
             Player givenPlayer = player;
             System.out.println("Mark of " + givenPlayer.getName() + " is: " + gameSession.getMarkForPlayer(givenPlayer));
             // Try to make a move to the given position with the given player.
             Boolean successfulMove = gameSession.move(gameMessage.xPos, gameMessage.yPos, givenPlayer);
             
             // If the move is successful, we convert the new gameboard to a response-friendly format.
             if (successfulMove) {
                 // Broadcast the name of the active player, the state of the board and, if available, the name of the winner.
                 if (gameSession.getWinner() == null) {
                     id.broadcast(new GameResponse(gameSession.getActivePlayer().getName(), gameSession.getBoard(), "Undecided"));
                 } else {
                     // A player has won, so we broadcast a gameresponse to all active connections and then remove the gamesession and id from the
                     // gameSessionMap.
                     System.out.println(gameSession.getWinner().getName() + " has won! Shutting down this gameSession and broadcaster.");
                     id.broadcast(new GameResponse(gameSession.getActivePlayer().getName(), gameSession.getBoard(), gameSession.getWinner().getName()));
                     gameSessionMap.remove(id.getID());
                 }
                 
                 // The move was successful, so we return an OK response.
                 System.out.println("Game move was accepted, returning statuscode 200.");
                 return Response.ok().build();
             }
         }
         
         // Move wasn't successful or player wasn't found.
         System.out.println("Game move was not accepted, returning statuscode 400.");
         return Response.status(400).build();
     }
     
 }
