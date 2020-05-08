 package com.jostrobin.battleships.server;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

 import com.jostrobin.battleships.common.data.AttackResult;
 import com.jostrobin.battleships.common.data.Ship;
 import com.jostrobin.battleships.common.network.Command;
 import com.jostrobin.battleships.server.client.Client;
 import com.jostrobin.battleships.server.game.Game;
 import com.jostrobin.battleships.server.util.IdGenerator;
 
 public class ServerManager
 {
     private static final Logger logger = LoggerFactory.getLogger(ServerManager.class);
 
     private List<Client> clients = new ArrayList<Client>();
 
     private IdGenerator gameIdGenerator = new IdGenerator();
 
     public void addClient(Client client)
     {
         logger.debug("New client registered: {}", client);
         clients.add(client);
 
         resendPlayerLists();
     }
 
     /**
      * Removes the client from the list of clients. Does not resend the
      * list of players.
      *
      * @param client
      */
     public void removeClient(Client client)
     {
         logger.debug("Player removed: {}", client);
         // TODO: What else do we need to do?
         // notify running games...
         clients.remove(client);
     }
 
     /**
      * The specified player has started a new game. Notify everyone.
      *
      * @param client
      */
     public void createGame(Client client, Command command)
     {
         Game game = new Game(gameIdGenerator.nextId(), command.getGameMode(), 0, command.getMaxPlayers(),
                 command.getFieldWidth(), command.getFieldLength());
         game.setOwner(client);
         game.addPlayer(client);
         client.setGame(game);
 
         client.initializeField(command.getFieldWidth(), command.getFieldLength());
 
         resendPlayerLists();
     }
 
     /**
      * The specified client wants to join the specified game.
      *
      * @param client
      * @param gameId
      */
     public synchronized void joinGame(Client client, Long gameId)
     {
         Game game = findGame(gameId);
         // clients can only join games of others
         if (game != null && !game.getOwner().equals(client))
         {
             boolean added = game.addPlayer(client);
             if (added && game.getCurrentPlayers() == game.getMaxPlayers())
             {
            	client.setGame(game);
                 // the game is full, we can start it
                 try
                 {
                     game.prepareGame();
                 }
                 catch (IOException e)
                 {
                     removeClient(client);
                 }
             }
         }
         resendPlayerLists();
     }
 
     private Game findGame(Long gameId)
     {
         Game game = null;
         for (Client c : clients)
         {
             if (c.getGame() != null && c.getGame().getId().equals(gameId))
             {
                 game = c.getGame();
             }
         }
         return game;
     }
 
     /**
      * Sends the lists of players to all the clients.
      */
     public void resendPlayerLists()
     {
         boolean resend = false;
         for (Client c : clients)
         {
             try
             {
                 c.sendAvailablePlayers(clients);
             }
             catch (IOException e)
             {
                 logger.info("Client communication aborted");
                 removeClient(c);
                 resend = true;
             }
         }
         if (resend)
         {
             resendPlayerLists();
         }
     }
 
     public void attack(Long clientId, int x, int y)
     {
         Client client = getClientById(clientId);
         if (client != null)
         {
             AttackResult result = client.attack(x, y);
             // notify the participants of this game about it
             for (Client toBeNotified : client.getGame().getPlayers())
             {
                 Ship ship = null;
                 if (result == AttackResult.SHIP_DESTROYED)
                 {
                     // we also need to transmit the ship which has been destroyed
                     ship = client.getShipAtPosition(x, y);
                 }
 
                 try
                 {
                     toBeNotified.sendAttackResult(clientId, x, y, result, ship);
                 }
                 catch (Exception e)
                 {
                     logger.info("Client communication aborted.");
                     removeClient(toBeNotified);
                     resendPlayerLists();
                 }
             }
         }
     }
 
     public String getServerStatus()
     {
         String status = "";
 
         List<Long> gameIds = new ArrayList<Long>();
         for (Client client : clients)
         {
             if (client.getGame() != null)
             {
                 if (!gameIds.contains(client.getGame().getId()))
                 {
                     gameIds.add(client.getGame().getId());
                 }
             }
         }
 
         status += "\nCurrently " + clients.size() + " clients are connected.\n";
         status += gameIds.size() + " games running in total.";
 
         return status;
     }
 
     public void updateGameState(Game game)
     {
         boolean ready = true;
         for (Client player : game.getPlayers())
         {
             ready &= player.isReady();
         }
         if (ready)
         {
             // TODO: notify clients
         }
     }
 
     /**
      * Returns the client with the specified id or null if it doesn't exist.
      *
      * @param id
      * @return
      */
     private Client getClientById(Long id)
     {
         for (Client client : clients)
         {
             if (client.getId().equals(id))
             {
                 return client;
             }
         }
         return null;
     }
 }
