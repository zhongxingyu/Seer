
 package is.marshmallow;
 
 public class TicTacToe {
     String player1;
     String player2;
     String currentMarker;
     String currentPlayer;
     Board board;
     public boolean isWinner;
 
     public String getPlayer1() {
         return player1;
     }
 
     public void setPlayer1(String player1) {
         this.player1 = player1;
     }
 
     public String getPlayer2() {
         return player2;
     }
 
     public void setPlayer2(String player2) {
         this.player2 = player2;
     }
 
     public String getCurrentMarker() {
         return currentMarker;
     }
 
     public void setCurrentMarker(String currentMarker) {
         this.currentMarker = currentMarker;
     }
 
     public String getCurrentPlayer() {
         return currentPlayer;
     }
 
     public void setCurrentPlayer(String currentPlayer) {
         this.currentPlayer = currentPlayer;
     }
 
     public boolean getIsWinner() {
         return this.isWinner;
     }
 
     public void setWinner(boolean winner) {
         isWinner = winner;
     }
 
     public TicTacToe(String player1, String player2) {
         this.player1 = player1;
         this.player2 = player2;
         currentMarker = "X";
         currentPlayer = this.player1;
         isWinner = false;
 
         board = new Board();
     }
 
     public boolean checkDraw() {
         if (!isWinner) {
             String[][] tempArr = new String[3][3];
             tempArr = board.getBoardArray();
 
             for (int i = 0; i < 3; i++) {
                 for (int j = 0; j < 3; j++) {
                     if ( !(Character.isLetter(tempArr[i][j].charAt(0))) )
                         return false;
                 }
             }
             isWinner = true;
             return true;
         }
 
         return false;
     }
 
     public boolean checkWinner() {
         String current = "";
         String[][] tempBoard = new String[3][3];
         tempBoard = board.getBoardArray();
 
         // finna RAÐIR winnera
         for (int i = 0; i < 3; i++) {
             for (int j = 0; j < 3; j++) {
                 if ( !(Character.isLetter(tempBoard[i][j].charAt(0))) )
                     break;
                 if (j == 0)
                     current = tempBoard[i][j];
                 else if ( !(current.equals(tempBoard[i][j])) )
                     break;
 
                 if (j == 2) {
                     isWinner = true;
                     return true;
                 }
             }
         }
 
         // finna DÁLKA winnera
         for (int i = 0; i < 3; i++) {
             for (int j = 0; j < 3; j++) {
                if ( !(Character.isLetter(tempBoard[i][j].charAt(0))) )
                 {
                     break;
                 }
                 if (j == 0) {
                     current = tempBoard[j][i];
                 } else if ( !(current.equals(tempBoard[j][i])) )
                 {
                     break;
                 }
                 if (j == 2) {
                     isWinner = true;
                     return true;
                 }
             }
         }
 
         // finna KROSS winnera
         current = tempBoard[0][0];
         if ( (Character.isLetter(current.charAt(0))) && (tempBoard[1][1].equals(current)) && (tempBoard[2][2].equals(current)))
         {
             isWinner = true;
             return true;
         }
         current = tempBoard[2][0];
         if ( (Character.isLetter(current.charAt(0))) && (tempBoard[1][1].equals(current)) && (tempBoard[0][2].equals(current)))
         {
             isWinner = true;
             return true;
         }
 
         return false;
     }
 
     public void switchPlayer() {
         if (currentMarker.equals("X")) {
             currentMarker = "O";
             currentPlayer = player2;
         }
 
         else {
             currentMarker = "X";
             currentPlayer = player1;
         }
     }
 
 
     public boolean placeMarker(int place, String marker) {
         if (getCurrentMarker().equals(marker)) {
             String[][] tempArr = new String[3][3];
             tempArr = board.getBoardArray();
 
             for (int i = 0; i < 3; i++) {
                 for (int j = 0; j < 3; j++) {
                     if (String.valueOf(place).equals(tempArr[i][j]))
                     {
                         board.updateBoard(place, marker);
                         switchPlayer();
                         return true;
                     }
                 }
             }
         }
 
         return false;
     }
 
     public String getBoard() {
         return board.getBoardString();
     }
}

