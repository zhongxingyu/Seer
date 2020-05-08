 /**
  * Created with IntelliJ IDEA.
  * User: Notandi
  * Date: 12.11.2012
  * Time: 17:02
  */
 public class Board {
     char[] boardArray = new char[9];
 
     public Board() {
         for (int i = 0; i < 9; i++) {
             boardArray[i] = Character.forDigit(i + 1, 10);
         }
     }
 
     @Override
     public String toString() {
         String display;
         display = "   " + boardArray[0] + "  |  " + boardArray[1] + "  |  " + boardArray[2] + "\n"  //   1  |  2  |  3
                 + " -----+-----+-----" + "\n"                                                       // -----+-----+-----
                 + "   " + boardArray[3] + "  |  " + boardArray[4] + "  |  " + boardArray[5] + "\n"  //   4  |  5  |  6
                 + " -----+-----+-----" + "\n"                                                       // -----+-----+-----
                 + "   " + boardArray[6] + "  |  " + boardArray[7] + "  |  " + boardArray[8] + "\n"; //   7  |  8  |  9
 
         return display;
     }
 
     public boolean updateBoard(char c, int move) {
        if(move < 0 || move >8)
            return false;
         if (boardArray[move] == 'X' || boardArray[move] == 'O')
             return false;
         else
             boardArray[move] = c;
         return true;
     }
 
     public char checkWinner() {
         return 'x';
     }
 
 
 }
