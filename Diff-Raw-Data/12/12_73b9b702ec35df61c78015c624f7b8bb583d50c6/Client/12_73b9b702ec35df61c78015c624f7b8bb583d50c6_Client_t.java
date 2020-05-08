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
   int fishSpeciesSpawn(Pointer object, int x, int y);
   int fishMove(Pointer object, int x, int y);
   int fishPickUp(Pointer object, int x, int y, int weight);
   int fishDrop(Pointer object, int x, int y, int weight);
   int fishAttack(Pointer object, int x, int y);
   int playerTalk(Pointer object, String message);
 
     //accessors
   int getInitialFood(Pointer connection);
   int getSharedLowerBound(Pointer connection);
   int getSharedUpperBound(Pointer connection);
   int getSpawnFoodPerTurn(Pointer connection);
   int getTurnNumber(Pointer connection);
   int getPlayerID(Pointer connection);
   int getGameNumber(Pointer connection);
   int getTurnsTillSpawn(Pointer connection);
   int getMaxReefHealth(Pointer connection);
   int getTrashDamage(Pointer connection);
   int getMapWidth(Pointer connection);
   int getMapHeight(Pointer connection);
   int getTrashAmount(Pointer connection);
   int getCoveX(Pointer connection);
   int getCoveY(Pointer connection);
 
   Pointer getMappable(Pointer connection, int num);
   int getMappableCount(Pointer connection);
   Pointer getFishSpecies(Pointer connection, int num);
   int getFishSpeciesCount(Pointer connection);
   Pointer getTile(Pointer connection, int num);
   int getTileCount(Pointer connection);
   Pointer getFish(Pointer connection, int num);
   int getFishCount(Pointer connection);
   Pointer getPlayer(Pointer connection, int num);
   int getPlayerCount(Pointer connection);
 
 
     //getters
   int mappableGetId(Pointer ptr);
   int mappableGetX(Pointer ptr);
   int mappableGetY(Pointer ptr);
 
   int fishSpeciesGetId(Pointer ptr);
   String fishSpeciesGetSpecies(Pointer ptr);
   int fishSpeciesGetCost(Pointer ptr);
   int fishSpeciesGetMaxHealth(Pointer ptr);
   int fishSpeciesGetMaxMovement(Pointer ptr);
   int fishSpeciesGetCarryCap(Pointer ptr);
   int fishSpeciesGetAttackPower(Pointer ptr);
   int fishSpeciesGetRange(Pointer ptr);
   int fishSpeciesGetMaxAttacks(Pointer ptr);
   int fishSpeciesGetTurnsTillAvailalbe(Pointer ptr);
   int fishSpeciesGetTurnsTillUnavailable(Pointer ptr);
 
   int tileGetId(Pointer ptr);
   int tileGetX(Pointer ptr);
   int tileGetY(Pointer ptr);
   int tileGetTrashAmount(Pointer ptr);
   int tileGetOwner(Pointer ptr);
   int tileGetIsCove(Pointer ptr);
 
   int fishGetId(Pointer ptr);
   int fishGetX(Pointer ptr);
   int fishGetY(Pointer ptr);
   int fishGetOwner(Pointer ptr);
   int fishGetMaxHealth(Pointer ptr);
   int fishGetCurrentHealth(Pointer ptr);
   int fishGetMaxMovement(Pointer ptr);
   int fishGetMovementLeft(Pointer ptr);
   int fishGetCarryCap(Pointer ptr);
   int fishGetCarryingWeight(Pointer ptr);
   int fishGetAttackPower(Pointer ptr);
   int fishGetIsVisible(Pointer ptr);
   int fishGetMaxAttacks(Pointer ptr);
   int fishGetAttacksLeft(Pointer ptr);
   int fishGetRange(Pointer ptr);
   String fishGetSpecies(Pointer ptr);
 
   int playerGetId(Pointer ptr);
   String playerGetPlayerName(Pointer ptr);
   float playerGetTime(Pointer ptr);
   int playerGetCurrentReefHealth(Pointer ptr);
   int playerGetSpawnFood(Pointer ptr);
 
 
     //properties
 
 }
