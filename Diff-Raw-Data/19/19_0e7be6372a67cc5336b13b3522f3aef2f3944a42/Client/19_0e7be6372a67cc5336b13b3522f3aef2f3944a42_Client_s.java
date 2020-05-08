 import com.sun.jna.Library;
 import com.sun.jna.Pointer;
 import com.sun.jna.Native;
 
 public interface Client extends Library {
   Client INSTANCE = (Client)Native.loadLibrary("client", Client.class);
   Pointer createConnection();
   boolean serverConnect(Pointer connection, String host, String port);
 
   boolean serverLogin(Pointer connection, String username, String password);
   int createGame(Pointer connection);
   int joinGame(Pointer connection, int id, String playerType);
 
   void endTurn(Pointer connection);
   void getStatus(Pointer connection);
 
   int networkLoop(Pointer connection);
 
 
     //commands
   int shipTypeWarpIn(Pointer object, int x, int y);
   int playerTalk(Pointer object, String message);
   int shipMove(Pointer object, int x, int y);
   int shipSelfDestruct(Pointer object);
   int shipAttack(Pointer object, Pointer target);
 
     //accessors
   int getTurnNumber(Pointer connection);
   int getPlayerID(Pointer connection);
   int getGameNumber(Pointer connection);
   int getRound(Pointer connection);
   int getVictoriesNeeded(Pointer connection);
  int getInnerMapRadius(Pointer connection);
  int getOuterMapRadius(Pointer connection);
 
   Pointer getShipType(Pointer connection, int num);
   int getShipTypeCount(Pointer connection);
   Pointer getPlayer(Pointer connection, int num);
   int getPlayerCount(Pointer connection);
   Pointer getShip(Pointer connection, int num);
   int getShipCount(Pointer connection);
 
 
     //getters
   int shipTypeGetId(Pointer ptr);
   String shipTypeGetType(Pointer ptr);
   int shipTypeGetCost(Pointer ptr);
 
   int playerGetId(Pointer ptr);
   String playerGetPlayerName(Pointer ptr);
   float playerGetTime(Pointer ptr);
   int playerGetVictories(Pointer ptr);
   int playerGetEnergy(Pointer ptr);
 
   int shipGetId(Pointer ptr);
   int shipGetOwner(Pointer ptr);
   int shipGetX(Pointer ptr);
   int shipGetY(Pointer ptr);
   int shipGetRadius(Pointer ptr);
   String shipGetType(Pointer ptr);
   int shipGetAttacksLeft(Pointer ptr);
   int shipGetMovementLeft(Pointer ptr);
   int shipGetMaxMovement(Pointer ptr);
   int shipGetMaxAttacks(Pointer ptr);
   int shipGetDamage(Pointer ptr);
   int shipGetRange(Pointer ptr);
   int shipGetHealth(Pointer ptr);
   int shipGetMaxHealth(Pointer ptr);
   int shipGetSelfDestructDamage(Pointer ptr);
 
 
     //properties
 
 }
