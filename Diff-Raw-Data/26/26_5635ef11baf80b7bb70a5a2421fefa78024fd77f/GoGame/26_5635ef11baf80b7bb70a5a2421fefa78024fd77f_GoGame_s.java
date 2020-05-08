 package com.cauchymop.goblob;
 
 import android.os.Parcel;
 import android.os.Parcelable;
import com.google.common.collect.Lists;
 
import java.util.ArrayList;
 
 /**
  * Class to represent the state of a Go game, and enforce the rules of the game to play moves.
  */
 public class GoGame extends Game implements Parcelable {
 
   private int boardSize;
   private GoBoard board;
   private final Player blackPlayer;
   private final Player whitePlayer;
   private PlayerController blackController;
   private PlayerController whiteController;
   private StoneColor currentColor;
   private ArrayList<GoBoard> boardHistory = Lists.newArrayList();
   private ArrayList<Integer> moveHistory = Lists.newArrayList();
 
   // Instance pool management.
   private GoBoard[] boardPool = new GoBoard[10];
   private int boardPoolSize = 0;
 
   public GoGame(int boardSize, Player blackPlayer, Player whitePlayer) {
     this.boardSize = boardSize;
     currentColor = StoneColor.Black;
     board = getNewBoard();
     board.empty();
     boardHistory.add(board);
     this.blackPlayer = blackPlayer;
     this.whitePlayer = whitePlayer;
   }
 
   private GoGame(Parcel in) {
     boardSize = in.readInt();
     board = in.readParcelable(GoBoard.class.getClassLoader());
     blackPlayer = in.readParcelable(Player.class.getClassLoader());
     whitePlayer = in.readParcelable(Player.class.getClassLoader());
     currentColor = StoneColor.values()[in.readInt()];
     boardHistory = in.readArrayList(GoBoard.class.getClassLoader());
     moveHistory = in.readArrayList(Integer.class.getClassLoader());
   }
 
   @Override
   public void writeToParcel(Parcel dest, int flags) {
     dest.writeInt(boardSize);
     dest.writeParcelable(board, 0);
     dest.writeParcelable(blackPlayer, 0);
     dest.writeParcelable(whitePlayer, 0);
     dest.writeInt(currentColor.ordinal());
     dest.writeList(boardHistory);
     dest.writeList(moveHistory);
   }
 
   public static final Parcelable.Creator<GoGame> CREATOR = new Parcelable.Creator<GoGame>() {
     public GoGame createFromParcel(Parcel in) {
       return new GoGame(in);
     }
 
     public GoGame[] newArray(int size) {
       return new GoGame[size];
     }
   };
 
   public void runGame() {
     Thread thread = new Thread("Game") {
 
       @Override
       public void run() {
         while (!isGameEnd()) {
           getCurrentController().startTurn();
         }
       }
     };
     thread.start();
   }
 
   private PlayerController getCurrentController() {
     if (currentColor == StoneColor.Black) {
       return blackController;
     } else {
       return whiteController;
     }
   }
 
   private GoBoard getNewBoard() {
     if (boardPoolSize == 0) {
       return new GoBoard5();
     }
     boardPoolSize--;
     return boardPool[boardPoolSize];
   }
 
   private void recycleBoard(GoBoard board) {
     boardPool[boardPoolSize] = board;
     boardPoolSize++;
   }
 
   public void pass(MoveType moveType) {
     GoBoard newBoard = getNewBoard();
     newBoard.copyFrom(board);
     boardHistory.add(newBoard);
     moveHistory.add(boardSize*boardSize);
     board = newBoard;
     currentColor = currentColor.getOpponent();
     if (moveType == MoveType.REAL) {
       fireGameChanged();
     }
   }
 
   public boolean play(int x, int y, MoveType moveType) {
     GoBoard newBoard = getNewBoard();
     newBoard.copyFrom(board);
     if (newBoard.play(currentColor, x, y)) {
       boardHistory.add(newBoard);
       moveHistory.add(y*boardSize + x);
       board = newBoard;
       currentColor = currentColor.getOpponent();
       if (moveType == MoveType.REAL) {
         fireGameChanged();
       }
       return true;
     }
     recycleBoard(newBoard);
     return false;
   }
 
   @Override
   public void undo() {
     currentColor = currentColor.getOpponent();
     recycleBoard(boardHistory.remove(boardHistory.size() - 1));
     moveHistory.remove(moveHistory.size() - 1);
     board = boardHistory.get(boardHistory.size() - 1);
   }
 
   @Override
   public int getPosCount() {
     return boardSize * boardSize + 1;
   }
 
   @Override
   public boolean play(int pos, MoveType moveType) {
     if (pos == boardSize * boardSize) {
       pass(moveType);
       return true;
     }
     return play(pos % boardSize, pos / boardSize, moveType);
   }
 
   @Override
   public boolean isGameEnd() {
     if (moveHistory.size() < 2) {
       return false;
     }
     int lastMove = moveHistory.get(moveHistory.size() - 1);
     int previousMove = moveHistory.get(moveHistory.size() - 2);
     int passMove = boardSize * boardSize;
     return lastMove == passMove && previousMove == passMove;
   }
 
   @Override
   public double getScore() {
     return board.getScore(currentColor);
   }
 
   public StoneColor getColor(int x, int y) {
     return board.getColor(x, y);
   }
 
   @Override
   public int describeContents() {
     return 0;
   }
 
   public void setBlackController(PlayerController blackController) {
     this.blackController = blackController;
   }
 
   public void setWhiteController(PlayerController whiteController) {
     this.whiteController = whiteController;
   }
 
   public Player getBlackPlayer() {
     return blackPlayer;
   }
 
   public Player getWhitePlayer() {
     return whitePlayer;
   }
 }
