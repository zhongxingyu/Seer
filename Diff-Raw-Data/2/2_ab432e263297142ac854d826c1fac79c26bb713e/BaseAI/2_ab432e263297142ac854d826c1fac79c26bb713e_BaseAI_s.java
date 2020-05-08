 import com.sun.jna.Pointer;
 import java.awt.Point;
 
 /// \brief A basic AI interface.
 
 ///This class implements most the code an AI would need to interface with the lower-level game code.
 ///AIs should extend this class to get a lot of builer-plate code out of the way
 ///The provided AI class does just that.
 public abstract class BaseAI
 {
   static ShipType[] shipTypes;
   static Player[] players;
   static Ship[] ships;
   Pointer connection;
   static int iteration;
   boolean initialized;
 
   public BaseAI(Pointer c)
   {
     connection = c;
   }
     
   ///
   ///Make this your username, which should be provided.
   public abstract String username();
   ///
   ///Make this your password, which should be provided.
   public abstract String password();
   ///
   ///This is run on turn 1 before run
   public abstract void init();
   ///
   ///This is run every turn . Return true to end the turn, return false
   ///to request a status update from the server and then immediately rerun this function with the
   ///latest game status.
   public abstract boolean run();
 
   ///
   ///This is run on after your last turn.
   public abstract void end();
 
 
   public boolean startTurn()
   {
     iteration++;
     int count = 0;
     count = Client.INSTANCE.getShipTypeCount(connection);
     shipTypes = new ShipType[count];
     for(int i = 0; i < count; i++)
     {
       shipTypes[i] = new ShipType(Client.INSTANCE.getShipType(connection, i));
     }
     count = Client.INSTANCE.getPlayerCount(connection);
     players = new Player[count];
     for(int i = 0; i < count; i++)
     {
       players[i] = new Player(Client.INSTANCE.getPlayer(connection, i));
     }
     count = Client.INSTANCE.getShipCount(connection);
     ships = new Ship[count];
     for(int i = 0; i < count; i++)
     {
       ships[i] = new Ship(Client.INSTANCE.getShip(connection, i));
     }
 
     if(!initialized)
     {
       initialized = true;
       init();
     }
     return run();
   }
 
 
   ///How many turns it has been since the beginning of the game
   int turnNumber()
   {
     return Client.INSTANCE.getTurnNumber(connection);
   }
   ///Player Number; either 0 or 1
   int playerID()
   {
     return Client.INSTANCE.getPlayerID(connection);
   }
   ///What number game this is for the server
   int gameNumber()
   {
     return Client.INSTANCE.getGameNumber(connection);
   }
   ///The current round of the match
   int round()
   {
     return Client.INSTANCE.getRound(connection);
   }
   ///How many victories a player needs to win
   int victoriesNeeded()
   {
     return Client.INSTANCE.getVictoriesNeeded(connection);
   }
   ///The outer radius of the map.  Center of screen is (0,0), with +x right, +y up
   int mapRadius()
   {
     return Client.INSTANCE.getMapRadius(connection);
   }
   //TODO Document
   int distance(int fromX, int fromY, int toX, int toY)
   {
     return Client.INSTANCE.baseDistance(fromX, fromY, toX, toY);
   }
   //TODO Document
   Point pointOnLine(int fromX, int fromY, int toX, int toY, int travel)
   {
     int ret = Client.INSTANCE.basePointOnLine(fromX, fromY, toX, toY, travel);
    return new Point(ret / 1024 - 500, ret / 1024 - 500);
   }
 }
