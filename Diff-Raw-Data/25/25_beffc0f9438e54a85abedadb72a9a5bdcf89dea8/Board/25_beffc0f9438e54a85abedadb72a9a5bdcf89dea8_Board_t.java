 package datamodels;
 
 import ai.Rating;
 import constants.PieceColor;
 import constants.PieceType;
 import ui.MainClass;
 
 import java.awt.*;
 import java.util.ArrayDeque;
 
 /**
  * Created by
  * User: jakub
  * Date: 24.05.13
  */
 public class Board {
 
     private Piece[][] board = new Piece[8][8];
     private Point whiteKing;
     private Point blackKing;
 
     private boolean whiteKingMoved = false;
     private boolean leftWhiteRookMoved = false;
     private boolean rightWhiteRookMoved = false;
 
     private boolean blackKingMoved = false;
     private boolean leftBlackRookMoved = false;
     private boolean rightBlackRookMoved = false;
 
     private boolean blackCastling = false;
     private boolean whiteCastling = false;
 
     private boolean turn = false;
 
     public boolean equals(Board otherBoard){
         for(int x = 0; x < 8; x++){
             for(int y = 0; y < 8; y++){
                 if(board[x][y] != null){
                     if(!board[x][y].equals2(otherBoard.board[x][y])){
                         System.out.println("Pieces not equal");
                         System.out.println("board1 " + board[x][y]);
                         System.out.println("board2 " + otherBoard.board[x][y]);
                         return false;
                     }
 
                 }
             }
         }
 
         return true;
     }
 
     /**
      * Default constructor. Initializes the board with standard pieces set on it.
      */
     public Board(){
 
         /** Clear the board */
         clearBoard();
         /** Putting al the pieces in a standard way */
         setDefaultPieces();
 
     }
 
     public Board(Board source) {
         clearBoard();
 
         Piece[][] sourceBoard = source.board;
 
         for(int i=0; i<sourceBoard.length; i++){
             for(int j=0; j<sourceBoard[i].length; j++){
                 if(sourceBoard[i][j] != null){
                     board[i][j] = new Piece(sourceBoard[i][j]);
                 }
                 else
                     board[i][j] = null;
             }
         }
 
         whiteKing = new Point(source.whiteKing);
         blackKing = new Point(source.blackKing);
 
         whiteKingMoved = source.whiteKingMoved;
         leftWhiteRookMoved = source.leftWhiteRookMoved;
         rightWhiteRookMoved = source.rightWhiteRookMoved;
         blackKingMoved = source.blackKingMoved;
         leftBlackRookMoved = source.leftBlackRookMoved;
         rightBlackRookMoved = source.rightBlackRookMoved;
 
         blackCastling = source.blackCastling;
         whiteCastling = source.whiteCastling;
 
         turn = source.turn;
     }
 
     public ArrayDeque<Move> getAllMovesForCurrentPlayer(){
         ArrayDeque<Move> moves = new ArrayDeque<Move>();
 
 //        if(turn){
 //            for(Piece piece : blackPieces){
 //                moves.addAll(getMovesForField(piece.getX(), piece.getY()));
 //            }
 //        } else {
 //            for(Piece piece : whitePieces){
 //                moves.addAll(getMovesForField(piece.getX(), piece.getY()));
 //            }
 //        }
 
         for(int x = 0; x < 8; x++){
             for(int y = 0; y < 8; y++){
                 if(hasPiece(x, y)){
                     Piece piece = getPiece(x, y);
 
                     if(piece.getColor() == turn){
                         moves.addAll(getMovesForField(x, y));
                     }
                 }
             }
         }
 
         return moves;
     }
 
     public ArrayDeque<Move> getMovesForField(int x, int y){
         ArrayDeque<Move> moves = new ArrayDeque<Move>();
 
         if(hasPiece(x, y)){
 
             Piece pieceToCheck = getPiece(x, y);
 
             switch (pieceToCheck.getType()) {
 
                 case PAWN:
                     moves.addAll(generateMovesForPawn(x, y, pieceToCheck));
                     break;
                 case ROOK:
                     moves.addAll(generateMovesForRook(x, y, pieceToCheck));
                     break;
                 case KNIGHT:
                     moves.addAll(generateMovesForKnight(x, y, pieceToCheck));
                     break;
                 case BISHOP:
                     moves.addAll(generateMovesForBishop(x, y, pieceToCheck));
                     break;
                 case QUEEN:
                     moves.addAll(generateMovesForRook(x, y, pieceToCheck));
                     moves.addAll(generateMovesForBishop(x, y, pieceToCheck));
                     break;
                 case KING:
                     moves.addAll(generateMovesForKing(x, y, pieceToCheck));
                     break;
             }
         }
 
         return moves;
     }
 
     public ArrayDeque<Move> generateMovesForKing(int x, int y, Piece pieceToCheck){
         ArrayDeque<Move> moves = new ArrayDeque<Move>();
 
         /** Original FROM position */
         Point from = new Point(x, y);
 
         for(int i=x-2; i<x+3; i++){
 
             Point to = new Point(i, y);
             Move moveToCheck = new Move(from, to);
             if(checkIfMoveIsLegal(moveToCheck))
                 moves.add(moveToCheck);
 
         }
 
 
         for(int i=x-1; i<x+2; i++){
             for(int j=y-1; j<y+2; j++){
                 Point to = new Point(i, j);
                 Move moveToCheck = new Move(from, to);
                 if(checkIfMoveIsLegal(moveToCheck))
                     moves.add(moveToCheck);
             }
         }
 
 
 
         return moves;
     }
 
     public ArrayDeque<Move> generateMovesForBishop(int x, int y, Piece pieceToCheck){
         ArrayDeque<Move> moves = new ArrayDeque<Move>();
 
         /** Original FROM position */
         Point from = new Point(x, y);
 
         /** Right down */
         for(int i=x+1, j=y+1;i<8 && j<8;i++, j++){
             Point to = new Point(i, j);
             Move moveToCheck = new Move(from, to);
             if(checkIfMoveIsLegal(moveToCheck))
                 moves.add(moveToCheck);
 
             if(hasPiece(i, j))
                 break;
         }
 
         /** Right up */
         for(int i=x+1, j=y-1;i<8 && j>-1;i++, j--){
             Point to = new Point(i, j);
             Move moveToCheck = new Move(from, to);
             if(checkIfMoveIsLegal(moveToCheck))
                 moves.add(moveToCheck);
 
             if(hasPiece(i, j))
                 break;
         }
 
         /** Left down */
         for(int i=x-1, j=y+1;i>-1 && j<8; i--, j++){
             Point to = new Point(i, j);
             Move moveToCheck = new Move(from, to);
             if(checkIfMoveIsLegal(moveToCheck))
                 moves.add(moveToCheck);
 
             if(hasPiece(i, j))
                 break;
         }
 
         /** Left up */
         for(int i=x-1, j=y-1;i>-1 && j>-1; i--, j--){
             Point to = new Point(i, j);
             Move moveToCheck = new Move(from, to);
             if(checkIfMoveIsLegal(moveToCheck))
                 moves.add(moveToCheck);
 
             if(hasPiece(i, j))
                 break;
         }
 
 
         return moves;
     }
 
     public ArrayDeque<Move> generateMovesForKnight(int x, int y, Piece pieceToCheck){
         ArrayDeque<Move> moves = new ArrayDeque<Move>();
 
         /** Original FROM position */
         Point from = new Point(x, y);
 
         /** Hard coded positions */
         Point to = new Point(x+1, y-2);
 
         Move moveToCheck = new Move(from, to);
         if(checkIfMoveIsLegal(moveToCheck))
             moves.add(moveToCheck);
 
         to = new Point(x-1, y-2);
         moveToCheck = new Move(from, to);
         if(checkIfMoveIsLegal(moveToCheck))
             moves.add(moveToCheck);
 
         to = new Point(x+2, y+1);
         moveToCheck = new Move(from, to);
         if(checkIfMoveIsLegal(moveToCheck))
             moves.add(moveToCheck);
 
         to = new Point(x+2, y-1);
         moveToCheck = new Move(from, to);
         if(checkIfMoveIsLegal(moveToCheck))
             moves.add(moveToCheck);
 
         to = new Point(x+1, y+2);
         moveToCheck = new Move(from, to);
         if(checkIfMoveIsLegal(moveToCheck))
             moves.add(moveToCheck);
 
         to = new Point(x-1, y+2);
         moveToCheck = new Move(from, to);
         if(checkIfMoveIsLegal(moveToCheck))
             moves.add(moveToCheck);
 
         to = new Point(x-2, y+1);
         moveToCheck = new Move(from, to);
         if(checkIfMoveIsLegal(moveToCheck))
             moves.add(moveToCheck);
 
         to = new Point(x-2, y-1);
         moveToCheck = new Move(from, to);
         if(checkIfMoveIsLegal(moveToCheck))
             moves.add(moveToCheck);
 
         return moves;
     }
 
     public ArrayDeque<Move> generateMovesForRook(int x, int y, Piece pieceToCheck){
         ArrayDeque<Move> moves = new ArrayDeque<Move>();
 
         /** Original FROM position */
         Point from = new Point(x, y);
 
         /** Check up */
         for(int j=y-1; j>-1; j--){
             Point to = new Point(x, j);
 
             Move moveToCheck = new Move(from, to);
             if(checkIfMoveIsLegal(moveToCheck))
                 moves.add(moveToCheck);
 
             if(hasPiece(x, j))
                 break;
         }
 
         /** Check down */
         for(int j=y+1; j<8; j++){
             Point to = new Point(x, j);
 
             Move moveToCheck = new Move(from, to);
             if(checkIfMoveIsLegal(moveToCheck))
                 moves.add(moveToCheck);
 
             if(hasPiece(x, j))
                 break;
         }
 
         /** Check right */
         for(int i=x+1; i<8; i++){
             Point to = new Point(i, y);
 
             Move moveToCheck = new Move(from, to);
             if(checkIfMoveIsLegal(moveToCheck))
                 moves.add(moveToCheck);
 
             if(hasPiece(i, y))
                 break;
         }
 
         /** Check left */
         for(int i=x-1; i>-1; i--){
             Point to = new Point(i, y);
 
             Move moveToCheck = new Move(from, to);
             if(checkIfMoveIsLegal(moveToCheck))
                 moves.add(moveToCheck);
 
             if(hasPiece(i, y))
                 break;
         }
 
         return moves;
     }
 
     public ArrayDeque<Move> generateMovesForPawn(int x, int y, Piece pieceToCheck){
         ArrayDeque<Move> moves = new ArrayDeque<Move>();
 
         /** Original FROM position */
         Point from = new Point(x, y);
 
         /** Black pawn */
         if(pieceToCheck.getColor()){
             for(int i = x-1; i < x+2; i++){
                 Point to = new Point(i, y+1);
 
                 Move moveToCheck = new Move(from, to);
                 if(checkIfMoveIsLegal(moveToCheck)){
                     moves.add(moveToCheck);
                 }
             }
 
             Point to = new Point(x, y+2);
             Move moveToCheck = new Move(from, to);
             if(!hasPiece(x, y+2) && checkIfMoveIsLegal(moveToCheck)){
                 moves.add(moveToCheck);
             }
         }
         /** White pawn */
         else {
 
             for(int i = x-1; i<x+2; i++){
                 Point to = new Point(i, y-1);
 
                 Move moveToCheck = new Move(from, to);
                 if(checkIfMoveIsLegal(moveToCheck)){
                     moves.add(moveToCheck);
                 }
             }
 
             Point to = new Point(x, y-2);
             Move moveToCheck = new Move(from, to);
             if(!hasPiece(x, y+2) && checkIfMoveIsLegal(moveToCheck)){
                 moves.add(moveToCheck);
             }
         }
 
         return moves;
     }
 
     public boolean checkIfMoveIsLegal(Move move){
         boolean result = true;
 
         Point from = move.getFrom();
         Piece pieceFrom = getPiece(from);
 
         Point to = move.getTo();
 
         if(to.getX()<0 || to.getX() >7 || to.getY() < 0 || to.getY() > 7)
             return false;
 
         /** If the move is on a piece with the same color it is illegal. */
         if(hasPiece(to)){
             if(pieceFrom.getColor() == getPiece(to).getColor())
                 return false;
         }
 
         /**
          * Check if the king is safe after the move.
          */
 
         boolean colorOfPiece = pieceFrom.getColor();
 
         if( pieceFrom.getType() == PieceType.KING && !checkMoveForKing(move, pieceFrom)){
             return false;
         }
 
         makeMove(move, true);
         boolean kingIsSafe = isKingSafe(colorOfPiece);
         unmakeMove(move);
 
 
         if(!kingIsSafe){
             return false;
         }
 
         /** Check various constraints for specific piece type */
         switch (pieceFrom.getType()) {
 
             case PAWN:
                 result = checkMoveForPawn(move, pieceFrom);
                 break;
             case ROOK:
                 result = checkMoveForRook(move, pieceFrom);
                 break;
             case KNIGHT:
                 result = checkMoveForKnight(move, pieceFrom);
                 break;
             case BISHOP:
                 result = checkMoveForBishop(move, pieceFrom);
                 break;
             case QUEEN:
                 result = checkMoveForQueen(move, pieceFrom);
                 break;
             case KING:
                 result = checkMoveForKing(move, pieceFrom);
                 break;
         }
 
         return result;
     }
 
     private boolean checkMoveForQueen(Move moveToChceck, Piece pieceFrom) {
         boolean result = false;
 
 
         result = checkMoveForRook(moveToChceck, pieceFrom);
 
         if(!result)
             result = checkMoveForBishop(moveToChceck, pieceFrom);
 
 
         return result;
     }
 
     private boolean checkMoveForBishop(Move moveToChceck, Piece pieceFrom) {
         boolean result = false;
 
 
         Point from = moveToChceck.getFrom();
         Point to = moveToChceck.getTo();
 
         int oldX = from.x, oldY = from.y;
         int newX = to.x, newY = to.y;
 
 
         /** Has to be diagonal. */
         if(Math.abs(oldX-newX) == Math.abs(oldY-newY)){
             /** Went up */
             if(oldY > newY){
                 /** Went right */
                 if(oldX < newX){
                     for(int x=oldX+1, y=oldY-1;x<newX+1 && y>newY-1;x++,y--){
                         if(hasPiece(x, y) && x!=newX && y!=newY){
                             return false;
                         }
                     }
                     return true;
                 }
                 /** Went left */
                 else {
                     for(int x=oldX-1, y=oldY-1;x>newX-1 && y>newY-1;x--, y--){
                         if(hasPiece(x, y) && x!=newX && y!=newY){
                             return false;
                         }
                     }
                     return true;
                 }
             }
             /** Went down */
             else {
                 /** Went right */
                 if(oldX < newX){
                     for(int x=oldX+1, y=oldY+1;x<newX+1 && y<newY+1;x++, y++){
                         if(hasPiece(x, y) && x!=newX && y!=newY){
                             return false;
                         }
                     }
                     return true;
                 }
                 /** Went left */
                 else {
                     for(int x=oldX-1, y=oldY+1;x>newX-1 && y<newY+1;x--, y++){
                         if(hasPiece(x, y) && x!=newX && y!=newY){
                             return false;
                         }
                     }
                     return true;
                 }
             }
         }
 
 
         return result;
     }
 
     private boolean checkMoveForKnight(Move moveToChceck, Piece pieceFrom) {
         boolean result = false;
 
 
         Point from = moveToChceck.getFrom();
         Point to = moveToChceck.getTo();
 
         int oldX = from.x, oldY = from.y;
         int newX = to.x, newY = to.y;
 
         int distance = moveToChceck.getDistance();
 
         switch(distance){
             case 2:
                 if(Math.abs(oldX-newX) == 1 && Math.abs(oldY - newY) == 2)
                     return true;
                 if(Math.abs(oldX-newX) == 2 && Math.abs(oldY-newY) == 1)
                     return true;
                 break;
             default:
                 return false;
         }
 
         return result;
     }
 
     private boolean checkMoveForRook(Move moveToChceck, Piece pieceFrom) {
         boolean result = false;
 
         Point from = moveToChceck.getFrom();
         Point to = moveToChceck.getTo();
 
         int oldX = from.x, oldY = from.y;
         int newX = to.x, newY = to.y;
 
         /** Same row */
         if(oldY == newY){
             /**  Went right */
             if(oldX < newX){
                 for(int x = oldX+1; x<newX+1; x++){
                     if(hasPiece(x, oldY) && x!=newX){
                         return false;
                     }
 
                 }
                 return true;
             }
             /** Went left */
             else {
                 for(int x = oldX-1; x>newX-1; x--){
                     if(hasPiece(x, oldY) && x!=newX){
                         return false;
                     }
                 }
                 return true;
             }
         }
         /** Same column */
         else if (oldX == newX) {
             /** Went up */
             if(newY < oldY){
                 for(int y=oldY-1; y>newY-1; y--){
                     if(hasPiece(oldX, y) && y!=newY){
                         return false;
                     }
                 }
                 return true;
             }
             /** Went down */
             else {
                 for(int y=oldY+1; y<newY+1; y++){
                     if(hasPiece(oldX, y) && y!=newY){
                         return false;
                     }
                 }
                 return true;
             }
         }
 
         return result;
     }
 
     private boolean checkMoveForKing(Move moveToChceck, Piece pieceFrom) {
         boolean result = false;
 
         Point from = moveToChceck.getFrom();
         Point to = moveToChceck.getTo();
 
         int oldX = from.x, oldY = from.y;
         int newX = to.x, newY = to.y;
 
         int distance = (int) from.distance(to);
 
 
 //        System.out.println("White moved: " + whiteKingMoved + " black moved: " + blackKingMoved);
         if(pieceFrom.getColor()){
             if(blackKingMoved && distance == 2)
                 return false;
         } else{
             if(whiteKingMoved && distance == 2)
                 return false;
         }
 
         if(pieceFrom.getColor()){
             switch(distance){
                 case 1:
                     return true;
                 case 2:
                     if(!blackCastling){
                         /** Same row ! */
                         if(from.y == to.y){
                             /** Went right */
                             if(from.x < to.x){
                                 if(!rightBlackRookMoved){
                                     if(!hasPiece(oldX+1, oldY) && !hasPiece(oldX+2, oldY)){
                                         return true;
                                     }
                                 }
                             }
                             /** Went left */
                             else{
                                 if(!leftBlackRookMoved){
                                     if(!hasPiece(oldX-1, oldY) && !hasPiece(oldX-2, oldY)){
                                         return true;
                                     }
                                 }
                             }
                         }
                     }
                     break;
                 default:
                     return false;
             }
         } else {
             switch(distance){
                 case 1:
                     return true;
                 case 2:
                     /** same row ! */
                     if(oldY == newY){
                         /** Went right */
                         if(oldX < newX){
                             if(!rightWhiteRookMoved){
                                 if(!hasPiece(oldX+1, oldY) && !hasPiece(oldX+2, oldY)){
                                     return true;
                                 }
                             }
                         }
                         /** Went left */
                         else {
                             if(!leftWhiteRookMoved){
                                 if(!hasPiece(oldX-1, oldY) && !hasPiece(oldX-2, oldY)){
                                     return true;
                                 }
                             }
                         }
                     }
                     break;
                 default:
                     return false;
             }
         }
 
         return result;
     }
 
     private boolean checkMoveForPawn(Move moveToCheck, Piece pieceFrom) {
         boolean result = false;
 
         Point from = moveToCheck.getFrom();
         Point to = moveToCheck.getTo();
 
         int oldX = from.x, oldY = from.y;
         int newX = to.x, newY = to.y;
 
         int distance = (int) from.distance(to);
 
         /**
          * Case that the color of the pawn is black.
          */
         if(pieceFrom.getColor()){
             switch(distance){
                 case 2:
                     /** Starting position */
                     if(oldY == 1){
                         /** Same column */
                         if(oldX == newX){
                             /** Nothing in front of him */
                             if(!hasPiece(oldX, oldY+1)){
                                 /** Goes forward */
                                 if(newY > oldY){
                                     /** Nothing is in new spot */
                                     if(!hasPiece(newX, newY))
                                             return true;
                                 }
                             }
                         }
                     }
                     break;
                 case 1:
                     /** Goes forward */
                     if(newY > oldY){
                         /** Same column ? */
                         if(oldX == newX){
                             if(!hasPiece(newX, newY)){
                                 return true;
                             }
                         }
                         /** Neighbouring column */
                         else {
                            if(hasPiece(newX, newY))
                                return true;
                         }
                     }
                     break;
                 default:
                     return false;
             }
         }
         /**
          * Case that the color of the pawn is white.
          */
         else {
             switch(distance){
                 case 2:
                     /** Starting position */
                     if(oldY == 6){
                         /** Same column */
                         if(oldX == newX){
                             /** Nothing in front of him */
                             if(!hasPiece(oldX, oldY-1)){
                                 /** Goes forward */
                                 if(newY < oldY){
                                     /** Nothing is in the new spot */
                                     if(!hasPiece(newX, newY))
                                         return true;
                                 }
                             }
                         }
                     }
                     break;
                 case 1:
                     /** Goes forward */
                     if(newY < oldY){
                         /** Same column ? */
                         if(oldX == newX){
                             if(!hasPiece(newX, newY)){
                                 return true;
                             }
                         }
                         /** Neighbouring column */
                         else {
                             if(hasPiece(newX, newY))
                                 return true;
                         }
                     }
                     break;
                 default:
                     return false;
             }
         }
 
         return result;
     }
 
 
     public void unmakeMove(Move moveToUnmake){
 
         Point from = moveToUnmake.getFrom();
         Point to = moveToUnmake.getTo();
 
         Piece pieceTaken = moveToUnmake.getPieceTaken();
 
         Piece pieceMoved = getPiece(to);
 
 
         pieceMoved.setLocation(from.x, from.y);
         board[from.x][from.y] = pieceMoved;
         board[to.x][to.y] = pieceTaken;
 
 
         if(moveToUnmake.tookPiece()){
             pieceTaken.setLocation(to.x, to.y);
         }
 
         if(moveToUnmake.changedPawnIntoQueen){
             pieceMoved.setType(PieceType.PAWN);
         }
 
         Piece rookPiece = null;
 
         switch(moveToUnmake.getCastlingType()){
             case 1:
                 rookPiece = board[3][0];
                 rookPiece.setLocation(0, 0);
                 board[0][0] = rookPiece;
                 board[3][0] = null;
                 break;
             case 2:
                 rookPiece = board[5][0];
                 rookPiece.setLocation(7, 0);
                 board[7][0] = rookPiece;
                 board[5][0] = null;
                 break;
             case 3:
                 rookPiece = board[3][7];
                 rookPiece.setLocation(0, 7);
                 board[0][7] = rookPiece;
                 board[3][7] = null;
                 break;
             case 4:
                 rookPiece = board[5][7];
                 rookPiece.setLocation(7, 7);
                 board[7][7] = rookPiece;
                 board[5][7] = null;
                 break;
         }
 
        if(pieceMoved.getType() == PieceType.KING){
            if(pieceMoved.getColor()){
                blackKing = new Point(pieceMoved.getX(), pieceMoved.getY());
            } else {
                whiteKing = new Point(pieceMoved.getX(), pieceMoved.getY());
            }
        }

         whiteCastling = moveToUnmake.whiteCastling;
         blackCastling = moveToUnmake.blackCastling;
         rightBlackRookMoved = moveToUnmake.rightBlackRookMoved;
         leftBlackRookMoved = moveToUnmake.leftBlackRookMoved;
         rightWhiteRookMoved = moveToUnmake.rightWhiteRookMoved;
         leftWhiteRookMoved = moveToUnmake.leftWhiteRookMoved;
         blackKingMoved = moveToUnmake.blackKingMoved;
         whiteKingMoved = moveToUnmake.whiteKingMoved;
 
         turn = !turn;
 
     }
 
 //    private ArrayDeque<Move> movesMade = new ArrayDeque<Move>();
 
     public void makeMove(Move moveToMake, boolean toCheck) {
         /** Clone this board */
 //        savedBoards.add(new Board(this));
 
 
         Point from = moveToMake.getFrom();
         Point to = moveToMake.getTo();
 
         Piece pieceToMove = board[from.x][from.y];
 
         if(hasPiece(to.x, to.y)){
             Piece pieceToTake = getPiece(to);
 
             moveToMake.setPieceTaken(pieceToTake);
         }
 
         board[to.x][to.y] = pieceToMove;
         board[from.x][from.y] = null;
 
 
         switch (pieceToMove.getType()) {
             case PAWN:
                 /** Is black */
                 if(pieceToMove.getColor()){
                     if(to.getY() == 7){
                         pieceToMove.setType(PieceType.QUEEN);
                         moveToMake.changedPawnIntoQueen = true;
                     }
                 }
                 /** Is white */
                 else {
                     if(to.getY() == 0) {
                         pieceToMove.setType(PieceType.QUEEN);
                         moveToMake.changedPawnIntoQueen = true;
                     }
                 }
                 break;
             case ROOK:
                 if(pieceToMove.getColor()) {
                     if(from.x == 0 && from.y == 0)
                         leftBlackRookMoved = true;
                     else if(from.x == 7 && from.y == 0)
                         rightBlackRookMoved = true;
                 } else {
                     if(from.x == 0 && from.y == 7)
                         leftWhiteRookMoved = true;
                     else if(from.x == 7 && from.y == 7)
                         rightWhiteRookMoved = true;
                 }
                 break;
             case KNIGHT:
                 break;
             case BISHOP:
                 break;
             case QUEEN:
                 break;
             case KING:
                 if(pieceToMove.getColor()){
                     blackKing = new Point(to.x, to.y);
                     blackKingMoved = true;
 
                 }
                 else{
                     whiteKing =  new Point(to.x, to.y);
                     whiteKingMoved = true;
 
                 }
 
                 if(moveToMake.getDistance() == 2){
                     if(pieceToMove.getColor()){
 
                         if(!blackCastling){
                             blackCastling = true;
                             castleKing(pieceToMove.getColor(), moveToMake);
                         }
                     } else
                         if(!whiteCastling){
                             whiteCastling = true;
                             castleKing(pieceToMove.getColor(), moveToMake);
                         }
 
                 }
                 break;
         }
 
         pieceToMove.setLocation(to.x, to.y);
 
         /** Change turn */
         turn = !turn;
 
         moveToMake.whiteCastling = whiteCastling;
         moveToMake.blackCastling = blackCastling;
         moveToMake.rightBlackRookMoved = rightBlackRookMoved;
         moveToMake.leftBlackRookMoved = leftBlackRookMoved;
         moveToMake.rightWhiteRookMoved = rightWhiteRookMoved;
         moveToMake.leftWhiteRookMoved = leftWhiteRookMoved;
         moveToMake.blackKingMoved = blackKingMoved;
         moveToMake.whiteKingMoved = whiteKingMoved;
 
 //        if(toCheck)
 //            movesMade.add(moveToMake);
 
         if(!toCheck){
             MainClass.moveMade(this);
         }
     }
 
     public boolean getTurn(){
         return turn;
     }
 
     private void castleKing(boolean color, Move moveToMake) {
         if(color){
             /** Black castling */
             /** On the right */
             if(blackKing.getX() == 6){
                 Piece rookPiece = board[7][0];
 
                 board[5][0] = rookPiece;
                 board[7][0] = null;
                 rookPiece.setLocation(5, 0);
 
                 moveToMake.setCastlingType(2);
             } else if(blackKing.getX() == 2){
                 Piece rookPiece = board[0][0];
 
                 board[3][0] = rookPiece;
                 board[0][0] = null;
                 rookPiece.setLocation(3, 0);
 
                 moveToMake.setCastlingType(1);
             }
         } else {
             /** White castling */
             /** On the right */
             if(whiteKing.getX() == 6){
                 Piece rookPiece = board[7][7];
 
                 board[5][7] = rookPiece;
                 board[7][7] = null;
                 rookPiece.setLocation(5, 7);
                 moveToMake.setCastlingType(4);
             } else if(whiteKing.getX() == 2) {
                 Piece rookPiece = board[0][7];
 
                 board[3][7] = rookPiece;
                 board[0][7] = null;
                 rookPiece.setLocation(3, 7);
 
                 moveToMake.setCastlingType(3);
             }
         }
     }
 
     /**
      * Tells us if the board has a piece on the specified spot.
      * @param x x coordinate.
      * @param y y coordinate.
      * @return true if there is a piece on the specified spot.
      */
     public boolean hasPiece(int x, int y){
         if(x>7 || x<0 || y>7 || y<0)
             return false;
 
         return board[x][y] != null;
     }
 
     public boolean hasPiece(Point p) {
         if(p.x>7 || p.x<0 || p.y>7 || p.y<0)
             return false;
 
         return board[p.x][p.y] != null;
     }
 
     /**
      * Returns the piece at the specified spot.
      * @param x x coordinate.
      * @param y y coordinate.
      * @return Null if there is no piece.
      */
     public Piece getPiece(int x, int y) {
         return board[x][y];
     }
 
     public Piece getPiece(Point p) {
         return board[p.x][p.y];
     }
 
     /**
      * Sets a specific piece on a specific square.
      * @param x x coordinate of the square.
      * @param y y coordinate of the square.
      * @param piece piece to be placed on that spot.
      */
     public void setSquare(int x, int y, Piece piece) {
         board[x][y] = piece;
     }
 
     /**
      * Clears the board from all pieces.
      */
     private void clearBoard() {
         /** Filling the board with nulls */
         for(int i=0; i<board.length; i++){
             for(int j=0; j<board[i].length; j++){
                 board[i][j] = null;
             }
         }
     }
 
     /**
      * Sets the board with default chess pieces setting.
      */
     private void setDefaultPieces() {
 
 
 
         /** Adding blacks */
         for(int i=0; i<board.length; i++){
             Piece pieceToSet = new Piece(i, 1, PieceColor.BLACK, PieceType.PAWN);
             setSquare(i, 1, pieceToSet);
         }
 
         setSquare(4, 0, new Piece(4, 0, PieceColor.BLACK, PieceType.KING));
         setSquare(0, 0, new Piece(0, 0, PieceColor.BLACK, PieceType.ROOK));
         setSquare(1, 0, new Piece(1, 0, PieceColor.BLACK, PieceType.KNIGHT));
         setSquare(2, 0, new Piece(2, 0, PieceColor.BLACK, PieceType.BISHOP));
         setSquare(3, 0, new Piece(3, 0, PieceColor.BLACK, PieceType.QUEEN));
         setSquare(5, 0, new Piece(5, 0, PieceColor.BLACK, PieceType.BISHOP));
         setSquare(6, 0, new Piece(6, 0, PieceColor.BLACK, PieceType.KNIGHT));
         setSquare(7, 0, new Piece(7, 0, PieceColor.BLACK, PieceType.ROOK));
 
         /** Adding whites */
         for(int i=0; i<board.length; i++){
             Piece pieceToSet = new Piece(i, 6, PieceColor.WHITE, PieceType.PAWN);
             setSquare(i, 6, pieceToSet);
         }
 
         setSquare(4, 7, new Piece(4, 7, PieceColor.WHITE, PieceType.KING));
         setSquare(0, 7, new Piece(0, 7, PieceColor.WHITE, PieceType.ROOK));
         setSquare(1, 7, new Piece(1, 7, PieceColor.WHITE, PieceType.KNIGHT));
         setSquare(2, 7, new Piece(2, 7, PieceColor.WHITE, PieceType.BISHOP));
         setSquare(3, 7, new Piece(3, 7, PieceColor.WHITE, PieceType.QUEEN));
         setSquare(5, 7, new Piece(5, 7, PieceColor.WHITE, PieceType.BISHOP));
         setSquare(6, 7, new Piece(6, 7, PieceColor.WHITE, PieceType.KNIGHT));
         setSquare(7, 7, new Piece(7, 7, PieceColor.WHITE, PieceType.ROOK));
 
         whiteKing = new Point(4, 7);
         blackKing = new Point(4, 0);
     }
 
     /**
      * Checks if the king is safe on this board.
      * @param color The color of the king to check safety of.
      * @return true if the king is safe.
      */
     private boolean isKingSafe(boolean color){
         boolean result = true;
 
         Point kingToCheck = whiteKing;
         if(color)
             kingToCheck = blackKing;

         /** Check North */
 //        System.out.println("King to check: " + kingToCheck);
 
         for(int i=kingToCheck.y-1; i>-1; i--){
             if(hasPiece(kingToCheck.x, i)){
 
                 Piece pieceToCheck = getPiece(kingToCheck.x, i);
 //                System.out.println("Piece north: " + pieceToCheck.toString());
                 if(pieceToCheck.getColor() != color){
                     PieceType typeOfEnemy = pieceToCheck.getType();
 
 
                     if(typeOfEnemy == PieceType.ROOK || typeOfEnemy == PieceType.QUEEN ){
                         return false;
                     } else if(typeOfEnemy == PieceType.KING){
                         if(Math.abs(i-kingToCheck.y) == 1){
                             return false;
                         }
                     }
 
                 }
 
                 break;
             }
         }
 
         /** Check south */
         for(int i=kingToCheck.y+1; i<8; i++){
             if(hasPiece(kingToCheck.x, i)){
 
                 Piece pieceToCheck = getPiece(kingToCheck.x, i);
 
                 if(pieceToCheck.getColor() != color){
                     PieceType typeOfEnemy = pieceToCheck.getType();
 
                     if(typeOfEnemy == PieceType.ROOK || typeOfEnemy == PieceType.QUEEN ){
                         return false;
                     } else if(typeOfEnemy == PieceType.KING){
                         if(Math.abs(i-kingToCheck.y) == 1){
                             return false;
                         }
                     }
                 }
 
                 break;
             }
         }
 
         /** Check west */
         for(int i=kingToCheck.x-1; i>-1; i--){
             if(hasPiece(i, kingToCheck.y)){
 
                 Piece pieceToCheck = getPiece(i, kingToCheck.y);
 
                 if(pieceToCheck.getColor() != color){
                     PieceType typeOfEnemy = pieceToCheck.getType();
 
                     if(typeOfEnemy == PieceType.ROOK || typeOfEnemy == PieceType.QUEEN ){
                         return false;
                     } else if(typeOfEnemy == PieceType.KING){
                         if(Math.abs(i-kingToCheck.x) == 1){
                             return false;
                         }
                     }
                 }
 
                 break;
             }
         }
 
         /** Check east */
         for(int i=kingToCheck.x+1; i<8; i++){
             if(hasPiece(i, kingToCheck.y)){
 
                 Piece pieceToCheck = getPiece(i, kingToCheck.y);
 
                 if(pieceToCheck.getColor() != color){
                     PieceType typeOfEnemy = pieceToCheck.getType();
 
                     if(typeOfEnemy == PieceType.ROOK || typeOfEnemy == PieceType.QUEEN ){
                         return false;
                     } else if(typeOfEnemy == PieceType.KING){
                         if(Math.abs(i-kingToCheck.x) == 1){
                             return false;
                         }
                     }
                 }
 
                 break;
             }
         }
 
         /** Check diagonal right up */
         for(int x=kingToCheck.x+1, y = kingToCheck.y-1; x<8 && y>-1; x++, y--){
             if(hasPiece(x, y)){
 
                 Piece pieceToCheck = getPiece(x, y);
 //                System.out.println("Piece right up: " + pieceToCheck.toString());
                 if(pieceToCheck.getColor() != color){
                     PieceType typeOfEnemy = pieceToCheck.getType();
 
                     if(typeOfEnemy == PieceType.QUEEN || typeOfEnemy == PieceType.BISHOP){
                         return false;
                     } else if(typeOfEnemy == PieceType.KING){
                         if(Math.abs(y-kingToCheck.y) == 1){
                             return false;
                         }
                     } else if(typeOfEnemy == PieceType.PAWN){
                         if(Math.abs(y-kingToCheck.y) == 1){
                             if(!color){
                                 return false;
                             }
                         }
                     }
                 }
 
                 break;
             }
         }
 
         /** Check diagonal right down */
         for(int x=kingToCheck.x+1, y = kingToCheck.y+1; x<8 && y<8; x++, y++){
             if(hasPiece(x, y)){
 
                 Piece pieceToCheck = getPiece(x, y);
 
                 if(pieceToCheck.getColor() != color){
                     PieceType typeOfEnemy = pieceToCheck.getType();
 
                     if(typeOfEnemy == PieceType.QUEEN || typeOfEnemy == PieceType.BISHOP){
                         return false;
                     } else if(typeOfEnemy == PieceType.KING){
                         if(Math.abs(y-kingToCheck.y) == 1){
                             return false;
                         }
                     } else if(typeOfEnemy == PieceType.PAWN){
                         if(Math.abs(y-kingToCheck.y) == 1){
                             if(color){
                                 return false;
                             }
                         }
                     }
                 }
 
                 break;
             }
         }
 
         /** Check diagonal left down */
         for(int x=kingToCheck.x-1, y = kingToCheck.y+1; x>-1 && y<8; x--, y++){
             if(hasPiece(x, y)){
 
                 Piece pieceToCheck = getPiece(x, y);
 
                 if(pieceToCheck.getColor() != color){
                     PieceType typeOfEnemy = pieceToCheck.getType();
 
                     if(typeOfEnemy == PieceType.QUEEN || typeOfEnemy == PieceType.BISHOP){
                         return false;
                     } else if(typeOfEnemy == PieceType.KING){
                         if(Math.abs(y-kingToCheck.y) == 1){
                             return false;
                         }
                     } else if(typeOfEnemy == PieceType.PAWN){
                         if(Math.abs(y-kingToCheck.y) == 1){
                             if(color){
                                 return false;
                             }
                         }
                     }
                 }
 
                 break;
             }
         }
 
         /** Check diagonal left up */
         for(int x=kingToCheck.x-1, y = kingToCheck.y-1; x>-1 && y>-1; x--, y--){
             if(hasPiece(x, y)){
 
                 Piece pieceToCheck = getPiece(x, y);
 
                 if(pieceToCheck.getColor() != color){
                     PieceType typeOfEnemy = pieceToCheck.getType();
 
                     if(typeOfEnemy == PieceType.QUEEN || typeOfEnemy == PieceType.BISHOP){
                         return false;
                     } else if(typeOfEnemy == PieceType.KING){
                         if(Math.abs(y-kingToCheck.y) == 1){
                             return false;
                         }
                     } else if(typeOfEnemy == PieceType.PAWN){
                         if(Math.abs(y-kingToCheck.y) == 1){
                             if(!color){
                                 return false;
                             }
                         }
                     }
                 }
 
                 break;
             }
         }
 
         /** Check for knights */
         int kingX = kingToCheck.x;
         int kingY = kingToCheck.y;
 
         Point knightToCheck = new Point(kingX+1, kingY-2);
         if(hasPiece(knightToCheck)){
             Piece pieceToCheck = getPiece(knightToCheck);
 
             if(pieceToCheck.getColor() != color){
                 if(pieceToCheck.getType() == PieceType.KNIGHT){
                     return false;
                 }
             }
         }
 
         knightToCheck = new Point(kingX-1, kingY-2);
         if(hasPiece(knightToCheck)){
             Piece pieceToCheck = getPiece(knightToCheck);
 
             if(pieceToCheck.getColor() != color){
                 if(pieceToCheck.getType() == PieceType.KNIGHT){
                     return false;
                 }
             }
         }
 
         knightToCheck = new Point(kingX+2, kingY+1);
         if(hasPiece(knightToCheck)){
             Piece pieceToCheck = getPiece(knightToCheck);
 
             if(pieceToCheck.getColor() != color){
                 if(pieceToCheck.getType() == PieceType.KNIGHT){
                     return false;
                 }
             }
         }
 
         knightToCheck = new Point(kingX+2, kingY-1);
         if(hasPiece(knightToCheck)){
             Piece pieceToCheck = getPiece(knightToCheck);
 
             if(pieceToCheck.getColor() != color){
                 if(pieceToCheck.getType() == PieceType.KNIGHT){
                     return false;
                 }
             }
         }
         knightToCheck = new Point(kingX+1, kingY+2);
         if(hasPiece(knightToCheck)){
             Piece pieceToCheck = getPiece(knightToCheck);
 
             if(pieceToCheck.getColor() != color){
                 if(pieceToCheck.getType() == PieceType.KNIGHT){
                     return false;
                 }
             }
         }
 
         knightToCheck = new Point(kingX-1, kingY+2);
         if(hasPiece(knightToCheck)){
             Piece pieceToCheck = getPiece(knightToCheck);
 
             if(pieceToCheck.getColor() != color){
                 if(pieceToCheck.getType() == PieceType.KNIGHT){
                     return false;
                 }
             }
         }
 
         knightToCheck = new Point(kingX-2, kingY+1);
         if(hasPiece(knightToCheck)){
             Piece pieceToCheck = getPiece(knightToCheck);
 
             if(pieceToCheck.getColor() != color){
                 if(pieceToCheck.getType() == PieceType.KNIGHT){
                     return false;
                 }
             }
         }
 
         knightToCheck = new Point(kingX-2, kingY-1);
         if(hasPiece(knightToCheck)){
             Piece pieceToCheck = getPiece(knightToCheck);
 
             if(pieceToCheck.getColor() != color){
                 if(pieceToCheck.getType() == PieceType.KNIGHT){
                     return false;
                 }
             }
         }
 
         return result;
     }
 
     public int evaluate(int evaluateOption){
 
 //        System.out.println("Evaluate: " + evaluateOption);
 
         int result = 0;
 
         switch(evaluateOption){
             case 0:
                 result = evaluateMovability();
                 break;
             case 1:
                 result = evaluatePositions();
                 break;
             case 2:
                 result = evaluateMaterial() + evaluateMovability();
                 break;
             case 3:
                 result = evaluateMaterial() + evaluatePositions();
                 break;
         }
 
         return result;
 
     }
     private int evaluateMaterial(){
         int result = 0;
 
         int blackScore = 0;
         int whiteScore = 0;
 
         boolean isWhiteKing = false;
         boolean isBlackKing = false;
 
         int score = 100;
 
         for(int i=0; i<8; i++){
             for(int j=0; j<8; j++){
                 if(hasPiece(i, j)){
 
                     Piece pieceToCheck = getPiece(i, j);
 
                     switch (pieceToCheck.getType()) {
 
                         case PAWN:
                             score = 100;
                             if(pieceToCheck.getColor()){
                                 blackScore += score;
                             } else {
                                 whiteScore += score;
                             }
                             break;
                         case ROOK:
                             score = 500;
                             if(pieceToCheck.getColor()){
                                 blackScore += score;
                             } else {
                                 whiteScore += score;
                             }
                             break;
                         case KNIGHT:
                             score = 320;
                             if(pieceToCheck.getColor()){
                                 blackScore += score;
                             } else {
                                 whiteScore += score;
                             }
                             break;
                         case BISHOP:
                             score = 330;
                             if(pieceToCheck.getColor()){
                                 blackScore += score;
                             } else {
                                 whiteScore += score;
                             }
                             break;
                         case QUEEN:
                             score = 900;
                             if(pieceToCheck.getColor()){
                                 blackScore += score;
                             } else {
                                 whiteScore += score;
                             }
                             break;
                         case KING:
                             score = 20000;
                             if(pieceToCheck.getColor()){
                                 isBlackKing = true;
                                 blackScore += score;
                             } else {
                                 isWhiteKing = true;
                                 whiteScore += score;
                             }
                             break;
                     }
 
                 }
             }
         }
 
         if(turn){
             result = blackScore - whiteScore;
             if(!isBlackKing)
                 result = Integer.MIN_VALUE;
 
         } else {
             result = whiteScore - blackScore;
             if(!isWhiteKing)
                 result = Integer.MIN_VALUE;
         }
 
         return result;
     }
 
     private int evaluatePositions(){
         int thisScore = Rating.rateBoard(this, turn);
         int otherScore = Rating.rateBoard(this, !turn);
 
         return thisScore - otherScore;
     }
 
     private int evaluateMovability(){
         int thisScore = getAllMovesForCurrentPlayer().size();
         turn = !turn;
         int otherScore = getAllMovesForCurrentPlayer().size();
         turn = !turn;
         return thisScore - otherScore;
     }
 }
