 package com.chasemaster.util;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.servlet.ServletContext;
 
 import org.apache.log4j.Logger;
 
 import static com.chasemaster.util.GameConst.*;
 
 import com.chasemaster.exception.NoMovementException;
 import com.chasemaster.exception.NoObjectInContextException;
 import com.mgs.chess.core.ChessAnaliserImpl;
 import com.mgs.chess.core.ChessBoard;
 import com.mgs.chess.core.ChessReader;
 import com.mgs.chess.core.ChessReaderImpl;
 import com.mgs.chess.core.Color;
 import com.mgs.chess.core.Location;
 import com.mgs.chess.core.Piece;
 import com.mgs.chess.core.PieceOnLocation;
 import com.mgs.chess.core.movement.Movement;
 
 public class GameHelper {
   private static final Logger LOGGER = Logger.getLogger(GameHelper.class);
 
   private ServletContext context;
   private ChessReader chessReader;
   private ChessAnaliserImpl chessAnaliser;
 
   public GameHelper() {
     super();
 
     chessReader = new ChessReaderImpl();
     chessAnaliser = new ChessAnaliserImpl(chessReader);
   }
 
   public GameHelper(ServletContext context) {
     this();
     this.context = context;
   }
 
   public void changeTurn() {
     Boolean turn = (Boolean) context.getAttribute(TURN_WHITE);
     if (turn == null) {
       throw new NoObjectInContextException(TURN_WHITE);
     }
     context.setAttribute(TURN_WHITE, !turn);
   }
 
   public Boolean isTurnWhite() {
     Boolean turn = (Boolean) context.getAttribute(TURN_WHITE);
     if (turn == null) {
       throw new NoObjectInContextException(TURN_WHITE);
     }
     return turn;
   }
 
   public ChessBoard prepareBoard() {
     ChessBoard board = new ChessBoard().addPiece(Piece.WHITE_ROOK, Location.A1).addPiece(Piece.WHITE_KNIGHT, Location.B1).addPiece(
         Piece.WHITE_BISHOP, Location.C1).addPiece(Piece.WHITE_KING, Location.D1).addPiece(Piece.WHITE_QUEEN, Location.E1).addPiece(
         Piece.WHITE_BISHOP, Location.F1).addPiece(Piece.WHITE_KNIGHT, Location.G1).addPiece(Piece.WHITE_ROOK, Location.H1).addPiece(Piece.WHITE_PAWN,
         Location.A2).addPiece(Piece.WHITE_PAWN, Location.B2).addPiece(Piece.WHITE_PAWN, Location.C2).addPiece(Piece.WHITE_PAWN, Location.D2)
         .addPiece(Piece.WHITE_PAWN, Location.E2).addPiece(Piece.WHITE_PAWN, Location.F2).addPiece(Piece.WHITE_PAWN, Location.G2).addPiece(
             Piece.WHITE_PAWN, Location.H2).addPiece(Piece.BLACK_PAWN, Location.A7).addPiece(Piece.BLACK_PAWN, Location.B7).addPiece(Piece.BLACK_PAWN,
             Location.C7).addPiece(Piece.BLACK_PAWN, Location.D7).addPiece(Piece.BLACK_PAWN, Location.E7).addPiece(Piece.BLACK_PAWN, Location.F7)
         .addPiece(Piece.BLACK_PAWN, Location.G7).addPiece(Piece.BLACK_PAWN, Location.H7).addPiece(Piece.BLACK_ROOK, Location.A8).addPiece(
             Piece.BLACK_KNIGHT, Location.B8).addPiece(Piece.BLACK_BISHOP, Location.C8).addPiece(Piece.BLACK_KING, Location.D8).addPiece(
             Piece.BLACK_QUEEN, Location.E8).addPiece(Piece.BLACK_BISHOP, Location.F8).addPiece(Piece.BLACK_KNIGHT, Location.G8).addPiece(
             Piece.BLACK_ROOK, Location.H8);
 
     return board;
   }
 
   public ChessBoard getBoard() {
     return (ChessBoard) context.getAttribute(CHESSBOARD);
   }
 
   public Piece getPiece(Location location) {
     return getBoard().getPieceOnLocation(location).getPiece();
   }
 
   /*
    * Takes all pieces on the board and populates a map with their images
    */
   public Map<String, String> getBoardImages() {
     Map<String, String> boardImages = new HashMap<String, String>();
 
     ChessBoard board = getBoard();
     if (board != null) {
       List<PieceOnLocation> pieces = board.getPieces(Color.WHITE);
       for (PieceOnLocation piece : pieces) {
         LOGGER.debug("Piece on board: " + piece.getLocation().toString() + ", " + piece.getPiece() + ", " + piece.getPiece().getImageName());
         boardImages.put(piece.getLocation().toString(), piece.getPiece().getImageName());
       }
 
       pieces = board.getPieces(Color.BLACK);
       for (PieceOnLocation piece : pieces) {
         LOGGER.debug("Piece on board: \'" + piece.getLocation().toString() + "\', " + piece.getPiece() + ", " + piece.getPiece().getImageName());
         boardImages.put(piece.getLocation().toString(), piece.getPiece().getImageName());
       }
     }
 
     return boardImages;
   }
 
   public boolean isMovementValid(Location locationFrom, Location locationTo) {
     ChessBoard board = getBoard();
 
     LOGGER.debug("found reachable locations for location " + locationFrom + ": " + board.getPieceOnLocation(locationFrom));
 
     List<Location> reachablePositions = chessAnaliser.findReachableLocations(board.getPieceOnLocation(locationFrom), board, null);
     for (Location reachablePosition : reachablePositions) {
       LOGGER.debug("-> " + reachablePosition.getCoordinateX() + "," + reachablePosition.getCoordinateY() + " ("
           + Location.forCoordinates(reachablePosition.getCoordinateX(), reachablePosition.getCoordinateY()) + ")");
       if (locationTo.equals(reachablePosition)) {
         return true;
       }
     }
 
     return false;
   }
 
   /*
    * Returns winning movements, determining in the way: 1) field which contains the biggest number of movements 2) in
    * case of 2 or more fields with the same number of movements, movements with the shortest total perform time are
    * winning
    */
   public void findWinningMovements(List<Movement> winningMovements, List<Movement> losingMovements, Map<String, Movement> playerMovementPairs,
       Map<String, Movement> failedPlayerMovementPairs) throws NoMovementException {
     /*
      * All failed movements are automatically losing movements
      */
     for (Map.Entry<String, Movement> entry : failedPlayerMovementPairs.entrySet()) {
       losingMovements.add(entry.getValue());
     }
 
    LOGGER.debug("--------> Retrieved (correctMovements, failedMovements): " + playerMovementPairs.size() + ", " + failedPlayerMovementPairs.size());
 
     /*
      * Converts player-movement map to list of movements grouped by FROM-TO fields combination.
      * 
      * NOTE: Only TO is not enough because one TO field can be target by movements from different FROM fields, therefore
      * to identify distinguish identical movements both FROM and TO fields must be used in combination.
      */
 
     Map<String, List<Movement>> movementsByLocation = new HashMap<String, List<Movement>>();
 
     for (Map.Entry<String, Movement> entry : playerMovementPairs.entrySet()) {
       LOGGER.debug("From playerMovementPairs: " + entry.getKey() + ", " + entry.getValue());
 
       String from = entry.getValue().getFrom().toString();
       String to = entry.getValue().getTo().toString();
       String fromto = from + to;
 
       List<Movement> movementsFromTo = movementsByLocation.get(fromto);
       if (movementsFromTo == null) {
         movementsFromTo = new ArrayList<Movement>();
       }
       movementsFromTo.add(entry.getValue());
       movementsByLocation.put(fromto, movementsFromTo);
     }
 
     // 1) go over all from-to fields combinations and check number of belonging movements
     int maxNumOfMovementsPerField = Integer.MIN_VALUE;
     List<String> fromtoFields = null;
     for (Map.Entry<String, List<Movement>> entry : movementsByLocation.entrySet()) {
       LOGGER.debug("Grouped movements by locations: " + entry.getKey() + ", numOfMoves: " + entry.getValue().size());
 
       // found more movements - reset list
       if (entry.getValue().size() > maxNumOfMovementsPerField) {
         LOGGER.debug("Found more identical movements " + entry.getKey() + "; before: " + maxNumOfMovementsPerField + ", now: "
             + entry.getValue().size());
         maxNumOfMovementsPerField = entry.getValue().size();
         fromtoFields = new ArrayList<String>();
         fromtoFields.add(entry.getKey());
         // found equal number of movements - add location to existing list
       } else if (entry.getValue().size() == maxNumOfMovementsPerField) {
         LOGGER.debug("Found equal number of identical movements: " + entry.getKey());
         fromtoFields.add(entry.getKey());
       }
     }
 
     // no winning movements (all coming are failed) - other side wins
     if (fromtoFields == null) {
       LOGGER.debug("None from-to field determined");
       throw new NoMovementException("None from-to field determined");
     }
 
     // if there is only one fields combination (i.e. one identical movement by all players),
     // belonging movements are winning
     if (fromtoFields.size() == 1) {
       LOGGER.debug("There is 1 winning fields combination: " + fromtoFields.get(0) + " with " + movementsByLocation.get(fromtoFields.get(0))
           + " movements");
       for (Movement movement : movementsByLocation.get(fromtoFields.get(0))) {
         winningMovements.add(movement);
       }
       // otherwise shortest total time of movements per fields must be counted
     } else {
       LOGGER.debug("There are " + fromtoFields.size() + " winning fields combinations: " + fromtoFields);
 
       long totalDuration = Long.MAX_VALUE;
       List<Movement> tempWinningMovements = null;
       for (String fromto : fromtoFields) {
         tempWinningMovements = movementsByLocation.get(fromto);
         long totalDurationPerGroup = 0;
 
         for (Movement tempWinningMovement : tempWinningMovements) {
           LOGGER.debug("Measuring duration: " + tempWinningMovement.getDuration());
           totalDurationPerGroup += tempWinningMovement.getDuration();
         }
 
        // if current total time better than previous, current movements are temporarily winning
         if (totalDurationPerGroup < totalDuration) {
           // move current list into losing movements
           for (Movement winningMovement : winningMovements) {
             losingMovements.add(winningMovement);
           }
 
           // make new winning list
           winningMovements.clear();
           for (Movement tempWinningMovement : tempWinningMovements) {
             winningMovements.add(tempWinningMovement);
           }
 
          // set total time to currently best one
           totalDuration = totalDurationPerGroup;
          // otherwise current movements are losing ones
        } else {
          for (Movement tempWinningMovement : tempWinningMovements) {
            losingMovements.add(tempWinningMovement);
          }          
         }
         LOGGER.debug("Chosen winning fields combination: " + winningMovements.get(0).getFrom() + winningMovements.get(0).getTo());
       }
     }
   }
 }
