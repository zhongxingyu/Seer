 package com.jneander.tictactoe;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import com.jneander.tictactoe.Mark.MarkType;
 
 public class Game {
   private Mark lastMark;
   private int playerMarks;
 
   private Mark gameBoard[][];
   private List< Mark[] > winningSets;
 
   public Game() {
     gameBoard = new Mark[3][3];
 
     resetGameBoard();
     playerMarks = 0;
 
     makeWinningSetList();
   }
 
   private void makeWinningSetList() {
     winningSets = new ArrayList< Mark[] >( 8 );
 
     for ( int row = 0; row < 3; row++ )
       winningSets.add( gameBoard[row] );
 
     for ( int col = 0; col < 3; col++ ) {
       Mark columns[] = new Mark[3];
 
       for ( int row = 0; row < 3; row++ )
         columns[row] = gameBoard[row][col];
 
       winningSets.add( columns );
     }
 
     Mark leftDiagonal[] = new Mark[] { gameBoard[0][0], gameBoard[1][1], gameBoard[2][2] };
     Mark rightDiagonal[] = new Mark[] { gameBoard[2][0], gameBoard[1][1], gameBoard[0][2] };
 
     winningSets.add( leftDiagonal );
     winningSets.add( rightDiagonal );
   }
 
   private void resetGameBoard() {
     for ( int row = 0; row < gameBoard.length; row++ )
       for ( int col = 0; col < gameBoard[row].length; col++ )
         gameBoard[row][col] = new Mark( row, col );
   }
 
   public Mark[][] getGameBoard() {
     return gameBoard;
   }
 
   public Mark getLastMark() {
     return lastMark;
   }
 
   public void makePlayerMark( int row, int col ) {
     gameBoard[row][col].setToPlayer();
     lastMark = gameBoard[row][col];
     playerMarks++;
   }
 
   public void makeComputerMark() {
     Mark nextMark = gameBoard[0][0];
     boolean madeMark = false;
 
     makeWinningSetList();
 
     if ( playerMarks == 1 ) {
       if ( isCenterMark( lastMark ) ) {
         nextMark = gameBoard[0][0];
       } else {
         nextMark = gameBoard[1][1];
       }
     } else if ( computerMustBlock() ) {
       Iterator< Mark[] > winningSetsIterator = winningSets.iterator();
 
       while ( winningSetsIterator.hasNext() && !madeMark ) {
         Mark[] currentMarkSet = winningSetsIterator.next();
 
         if ( setNeedsBlock( currentMarkSet ) ) {
           nextMark = findMarkForBlock( currentMarkSet );
           madeMark = true;
         }
       }
     }
 
     gameBoard[nextMark.row][nextMark.col].setToComputer();
     lastMark = nextMark;
   }
 
   private boolean computerMustBlock() {
     boolean blockNeeded = false;
 
     Iterator< Mark[] > winningSetsIterator = winningSets.iterator();
 
     while ( winningSetsIterator.hasNext() && !blockNeeded ) {
       Mark[] currentMarkSet = winningSetsIterator.next();
       blockNeeded = setNeedsBlock( currentMarkSet );
     }
 
     return blockNeeded;
   }
 
   private boolean setNeedsBlock( Mark marks[] ) {
     int blankCount = 0, playerCount = 0;
 
     for ( Mark mark : marks ) {
       if ( mark.getType() == MarkType.PLAYER )
         playerCount++;
       else if ( mark.getType() == MarkType.BLANK )
         blankCount++;
     }
 
     return (playerCount == 2 && blankCount == 1);
   }
 
   private Mark findMarkForBlock( Mark marks[] ) {
     Mark blockMark = null;
 
     for ( Mark mark : marks )
       if ( mark.getType() == MarkType.BLANK )
         blockMark = mark;
 
     return blockMark;
   }
 
   private boolean isCenterMark( Mark mark ) {
     return (mark.row == 1 && mark.col == 1);
   }
 
   public boolean isGameOver() {
     boolean gameOver = false;
 
     Iterator< Mark[] > winningSetsIterator = winningSets.iterator();
     while ( winningSetsIterator.hasNext() && !gameOver ) {
       Mark[] set = winningSetsIterator.next();
      gameOver = (set[0].getType() == set[1].getType() && set[1].getType() == set[2].getType()
          && set[0].getType() != MarkType.BLANK);
     }
 
     return gameOver;
   }
 }
