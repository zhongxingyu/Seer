 package de.hsa.otma.android.player;
 
 import android.util.Log;
 import de.hsa.otma.android.map.*;
 
 /**
  * Class handling player movement as well as win conditions.
  */
 public class PlayerService {
     private static final String TAG = PlayerService.class.getName();
     public static final PlayerService INSTANCE = new PlayerService();
 
     private HumanPlayer humanPlayer;
     private static final Board BOARD = Board.INSTANCE;
 
     private PlayerService() {
         // TODO change on final version
         humanPlayer = new HumanPlayer(new Coordinate(1, 1), "human");
 //        humanPlayer = new HumanPlayer(BOARD.getRandomCoordinateOnBoard(), "human");
     }
 
     public BoardElement move(Direction direction) {
         BoardElement currentMapItem = getCurrentMapItem();
         BoardElement newMapItem;
 
         if(currentMapItem instanceof Door){
             newMapItem = ((Door)currentMapItem).getOrigin();
        } else {
             newMapItem = currentMapItem.getElementFor(direction);
         }
 
         Log.d(TAG, "current position = " + currentMapItem);
 
         if (newMapItem == null) return null;
 
         Log.i(TAG, "moving to " + newMapItem);
         humanPlayer.moveTo(newMapItem.getCoordinate());
 
         return newMapItem;
     }
 
     public void foundHint(Hint hint) {
         humanPlayer.found(hint);
         Log.i(TAG, "found hint '" + hint.getTitle()  +"'");
     }
 
     public void foundNPC(NPCPlayer player) {
         humanPlayer.found(player);
         Log.i(TAG, "found npc '" + player.getName() +"'");
     }
 
     public boolean hasChallengeCompleted() {
 
         // TODO code to predict challenge completion
         return false;
     }
 
     public BoardElement getCurrentMapItem() {
         Coordinate currentCoordinate = humanPlayer.getCoordinate();
         return BOARD.getElementFor(currentCoordinate);
     }
 }
