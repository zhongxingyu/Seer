 package j3chess;
 
 import java.util.EnumMap;
 import java.util.EnumSet;
 import java.util.Map;
 
 /**
  * an artificial player.
  */
 public class ArtificialController extends PlayerController {
     /**
      * @brief Create an instance of a ArtificialController for a given player.
      * @param player The player which owns the PlayerController.
      */
     public ArtificialController(Player player) {
         super(player);
     }
 
     /** @brief defines the importance of each piece type */
     private Map<PieceType, Integer> mPieceValue =
             new EnumMap<PieceType, Integer>(PieceType.class);
 
     /**
      * @brief initializes the artificial controller with some context
      */
     private void initialize() {
         initializeValue(10);
     }
 
     /**
      * @brief initializes the value of each piece type
      * @param value basic value of a piece to calculate relative
      *        values with it
      */
     private void initializeValue(final int value) {
         for (PieceType pieceType : EnumSet.allOf(PieceType.class)) {
             Integer result;
             switch (pieceType) {
             case King:
                 result = value * value * value;
                 break;
             case Queen:
                 result = value * value;
                 break;
             case Rook:
                result = Math.round(value + (value * value / (float) 2));
                 break;
             case Bishop:
                result = Math.round(2 * value + (value / (float) 2));
                 break;
             case Knight:
                result = Math.round(2 * value + (value / (float) 2));
                 break;
             case Pawn:
                 result = value;
                 break;
             default:
                 result = 0;
                 break;
             }
             mPieceValue.put(pieceType, result);
         }
 
         String eol = System.getProperty("line.separator");
        StringBuffer logStringBuffer = new StringBuffer();
         for (PieceType pieceType : EnumSet.allOf(PieceType.class)) {
            logStringBuffer.append("Value of " + pieceType.toString() + ": "
                   + mPieceValue.get(pieceType).toString() + eol);
         }
        J3ChessApp.getLogger().debug(logStringBuffer.toString());
     }
 
 }
