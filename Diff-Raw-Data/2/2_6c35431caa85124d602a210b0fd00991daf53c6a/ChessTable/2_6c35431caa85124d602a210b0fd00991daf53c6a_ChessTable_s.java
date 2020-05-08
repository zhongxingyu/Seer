 package Logic;
 
 import Pieces.*;
 import java.awt.BorderLayout;
 import java.awt.Component;
 import java.util.ArrayList;
 import javax.swing.Icon;
 import javax.swing.ImageIcon;
 import javax.swing.JPanel;
 
 /**
  * A logic class which has a table of the piece labels always updated. <p> It
  * takes care if the king is checked, coloring of the legal moves of the pieces
  * changing the icons of the pieces.
  *
  * @author andreaskalstad
  */
 public class ChessTable {
 
     private PieceLabel[] table;
     private String[] log;
     private PieceLabel[][] twoTable;
 
     /**
      * Constructs a one dimensional table, two dimensional table and a log
      * string
      */
     public ChessTable() {
         this.table = new PieceLabel[64];
         this.log = new String[2];
         this.twoTable = new PieceLabel[8][8];
     }
 
     /**
      * Constructs a one dimensional table and sets it equals an argument
      * component table, two dimensional table and a log string
      *
      * @param table2
      */
     public ChessTable(Component[] table2) {
         this.table = new PieceLabel[64];
         for (int i = 0; i < 64; i++) {
             if (table2[i] instanceof PieceLabel) {
                 table[i] = (PieceLabel) table2[i];
             }
         }
         this.log = new String[2];
         this.twoTable = new PieceLabel[8][8];
     }
 
     /**
      * Method for updating the one dimensional table
      *
      * @param piece The type of Piece
      * @param indeks Index in the table
      */
     public void updateTable(PieceLabel piece, int indeks) {
         table[indeks] = piece;
     }
 
     /**
      * Method for creating a new dimensional tabel and setting it as object
      * variabel
      *
      * @param table2
      */
     public void newTable(PieceLabel[] table2) {
         table = table2;
         updateTwoTable();
     }
 
     /**
      * Method for getting the table
      *
      * @return Returns the table of pieces.
      */
     public PieceLabel[] getTable() {
         return table;
     }
 
     /**
      * Method for getting a piece from a index in the table
      *
      * @param index Index to get the piece from
      * @return Returns the piece on the given index
      */
     public PieceLabel getPiece(int index) {
         return table[index];
     }
 
     /**
      * Method for testing the two dimensional table
      */
     public void testTwoTable() {
         for (int i = 0; i < 8; i++) {
             for (int j = 0; j < 8; j++) {
                 if (twoTable[i][j] instanceof PieceLabel) {
                     System.out.println("i: " + i + " j: " + j + " " + twoTable[i][j].getPiece());
                 }
             }
         }
     }
 
     /**
      * Method for updating the two dimensional tabel
      */
     public void updateTwoTable() {
         int a = 0;
         for (int i = 0; i < 8; i++) {
             for (int j = 0; j < 8; j++) {
                 twoTable[i][j] = table[a];
                 a++;
 
             }
         }
     }
 
     /**
      * Method for updating the log
      *
      * @param log2 The new log value
      * @param indeks Indicates wich teams log this is. 0 is white, 1 is black.
      */
     public void updateLog(String log2, int indeks) {
         log[indeks] = log2;
     }
 
     /**
      * Method for getting the log
      *
      * @param index value indicating the team, 0 is white, 1 is black.
      * @return Returns the log for the given team.
      */
     public String getLog(int index) {
         return log[index];
     }
 
     /**
      * Method for getting a component at given index
      *
      * @param index Index indicating where in the table to check
      * @return Returns the component in the given index
      */
     public Component getComponent(int index) {
         if (table[index] instanceof PieceLabel) {
             return table[index];
         } else {
             JPanel square = new JPanel(new BorderLayout());
             square.setOpaque(false);
             return square;
         }
     }
 
     /**
      * Method for resetting the one dimensional table and log
      */
     public void reset() {
         for (int i = 0; i < table.length; i++) {
             table[i] = null;
         }
         log[0] = "";
         log[1] = "";
     }
 
     /**
      * Method for changing the interface layout of the board
      *
      * @param a 1 is meme, 2 is normal.
      */
     public void changeUI(int a) {
         switch (a) {
             case 1:
                 for (int i = 0; i < 64; i++) {
                     if (table[i] instanceof PieceLabel) {
                         if (table[i].getPiece() instanceof PawnW) {
                             table[i].setIcon(new ImageIcon(getClass().getResource("/Logic/Pictures/OkayguyW.png")));
                             table[i].getPiece().setIcon(new ImageIcon(getClass().getResource("/Logic/Pictures/OkayguyW.png")));
                         }
                         if (table[i].getPiece() instanceof PawnB) {
                             table[i].setIcon(new ImageIcon(getClass().getResource("/Logic/Pictures/OkayguyB.png")));
                             table[i].getPiece().setIcon(new ImageIcon(getClass().getResource("/Logic/Pictures/OkayguyB.png")));
                         }
                         if (table[i].getPiece() instanceof QueenW) {
                             table[i].setIcon(new ImageIcon(getClass().getResource("/Logic/Pictures/FmercuryW.png")));
                             table[i].getPiece().setIcon(new ImageIcon(getClass().getResource("/Logic/Pictures/FmercuryW.png")));
                         }
                         if (table[i].getPiece() instanceof QueenB) {
                             table[i].setIcon(new ImageIcon(getClass().getResource("/Logic/Pictures/FmercuryB.png")));
                             table[i].getPiece().setIcon(new ImageIcon(getClass().getResource("/Logic/Pictures/FmercuryB.png")));
                         }
                         if (table[i].getPiece() instanceof KnightW) {
                             table[i].setIcon(new ImageIcon(getClass().getResource("/Logic/Pictures/TrollfaceW.png")));
                             table[i].getPiece().setIcon(new ImageIcon(getClass().getResource("/Logic/Pictures/TrollfaceW.png")));
                         }
                         if (table[i].getPiece() instanceof KnightB) {
                             table[i].setIcon(new ImageIcon(getClass().getResource("/Logic/Pictures/TrollfaceB.png")));
                             table[i].getPiece().setIcon(new ImageIcon(getClass().getResource("/Logic/Pictures/TrollfaceB.png")));
                         }
                         if (table[i].getPiece() instanceof KingW) {
                             table[i].setIcon(new ImageIcon(getClass().getResource("/Logic/Pictures/YaomingW.png")));
                             table[i].getPiece().setIcon(new ImageIcon(getClass().getResource("/Logic/Pictures/YaomingW.png")));
                         }
                         if (table[i].getPiece() instanceof KingB) {
                             table[i].setIcon(new ImageIcon(getClass().getResource("/Logic/Pictures/YaomingB.png")));
                             table[i].getPiece().setIcon(new ImageIcon(getClass().getResource("/Logic/Pictures/YaomingB.png")));
                         }
                         if (table[i].getPiece() instanceof BishopW) {
                             table[i].setIcon(new ImageIcon(getClass().getResource("/Logic/Pictures/LolW.png")));
                             table[i].getPiece().setIcon(new ImageIcon(getClass().getResource("/Logic/Pictures/LolW.png")));
                         }
                         if (table[i].getPiece() instanceof BishopB) {
                             table[i].setIcon(new ImageIcon(getClass().getResource("/Logic/Pictures/LolB.png")));
                             table[i].getPiece().setIcon(new ImageIcon(getClass().getResource("/Logic/Pictures/LolB.png")));
                         }
                         if (table[i].getPiece() instanceof RookW) {
                             table[i].setIcon(new ImageIcon(getClass().getResource("/Logic/Pictures/MegustaW.png")));
                             table[i].getPiece().setIcon(new ImageIcon(getClass().getResource("/Logic/Pictures/MegustaW.png")));
                         }
                         if (table[i].getPiece() instanceof RookB) {
                             table[i].setIcon(new ImageIcon(getClass().getResource("/Logic/Pictures/MegustaB.png")));
                             table[i].getPiece().setIcon(new ImageIcon(getClass().getResource("/Logic/Pictures/MegustaB.png")));
                         }
                     }
                 }
                 break;
             case 2:
                 for (int i = 0; i < 64; i++) {
                     if (table[i] instanceof PieceLabel) {
                         if (table[i].getPiece() instanceof PawnW) {
                             table[i].setIcon(new ImageIcon(getClass().getResource("/Logic/Pictures/PawnW.png")));
                             table[i].getPiece().setIcon(new ImageIcon(getClass().getResource("/Logic/Pictures/PawnW.png")));
                         }
                         if (table[i].getPiece() instanceof PawnB) {
                             table[i].setIcon(new ImageIcon(getClass().getResource("/Logic/Pictures/PawnB.png")));
                             table[i].getPiece().setIcon(new ImageIcon(getClass().getResource("/Logic/Pictures/PawnB.png")));
                         }
                         if (table[i].getPiece() instanceof QueenW) {
                             table[i].setIcon(new ImageIcon(getClass().getResource("/Logic/Pictures/QueenW.png")));
                             table[i].getPiece().setIcon(new ImageIcon(getClass().getResource("/Logic/Pictures/QueenW.png")));
                         }
                         if (table[i].getPiece() instanceof QueenB) {
                             table[i].setIcon(new ImageIcon(getClass().getResource("/Logic/Pictures/QueenB.png")));
                             table[i].getPiece().setIcon(new ImageIcon(getClass().getResource("/Logic/Pictures/QueenB.png")));
                         }
                         if (table[i].getPiece() instanceof KnightW) {
                             table[i].setIcon(new ImageIcon(getClass().getResource("/Logic/Pictures/KnightW.png")));
                             table[i].getPiece().setIcon(new ImageIcon(getClass().getResource("/Logic/Pictures/KnightW.png")));
                         }
                         if (table[i].getPiece() instanceof KnightB) {
                             table[i].setIcon(new ImageIcon(getClass().getResource("/Logic/Pictures/KnightB.png")));
                             table[i].getPiece().setIcon(new ImageIcon(getClass().getResource("/Logic/Pictures/KnightB.png")));
                         }
                         if (table[i].getPiece() instanceof KingW) {
                             table[i].setIcon(new ImageIcon(getClass().getResource("/Logic/Pictures/KingW.png")));
                             table[i].getPiece().setIcon(new ImageIcon(getClass().getResource("/Logic/Pictures/KingW.png")));
                         }
                         if (table[i].getPiece() instanceof KingB) {
                             table[i].setIcon(new ImageIcon(getClass().getResource("/Logic/Pictures/KingB.png")));
                             table[i].getPiece().setIcon(new ImageIcon(getClass().getResource("/Logic/Pictures/KingB.png")));
                         }
                         if (table[i].getPiece() instanceof BishopW) {
                             table[i].setIcon(new ImageIcon(getClass().getResource("/Logic/Pictures/BishopW.png")));
                             table[i].getPiece().setIcon(new ImageIcon(getClass().getResource("/Logic/Pictures/BishopW.png")));
                         }
                         if (table[i].getPiece() instanceof BishopB) {
                             table[i].setIcon(new ImageIcon(getClass().getResource("/Logic/Pictures/BishopB.png")));
                             table[i].getPiece().setIcon(new ImageIcon(getClass().getResource("/Logic/Pictures/BishopB.png")));
                         }
                         if (table[i].getPiece() instanceof RookW) {
                             table[i].setIcon(new ImageIcon(getClass().getResource("/Logic/Pictures/RookW.png")));
                             table[i].getPiece().setIcon(new ImageIcon(getClass().getResource("/Logic/Pictures/RookW.png")));
                         }
                         if (table[i].getPiece() instanceof RookB) {
                             table[i].setIcon(new ImageIcon(getClass().getResource("/Logic/Pictures/RookB.png")));
                             table[i].getPiece().setIcon(new ImageIcon(getClass().getResource("/Logic/Pictures/RookB.png")));
                         }
                     }
                 }
                 break;
         }
     }
 
     /**
      * Method for getting the white kings position
      *
      * @return returns the index of the white kings position
      */
     public int kingWpos() {
         for (int i = 0; i < table.length; i++) {
             if (table[i].getPiece() instanceof KingW) {
                 return i;
             }
         }
         return -1;
     }
 
     /**
      * Method for getting the black kings position
      *
      * @return returns the index of the white kings position
      */
     public int kingBpos() {
         for (int i = 0; i < table.length; i++) {
             if (table[i].getPiece() instanceof KingB) {
                 return i;
             }
         }
         return -1;
     }
 
     /**
      * Method for checking if the white king is in check. Takes in index of the
      * white king.
      *
      * @param i index position of the white king
      * @return returns true if the given index is in check from black.
      */
     public boolean checkW(int i) {
         if (checkBishopW(i)) {
             return true;
         }
         if (checkRookW(i)) {
             return true;
         }
         if (checkKnightW(i)) {
             return true;
         }
         if (checkPawnW(i)) {
             return true;
         }
         if (checkKingW(i)) {
             return true;
         }
         return false;
     }
 
     /**
      * Method for checking if the white king is in check. Takes in index of the
      * white king.
      *
      * @param i index position of the white king
      * @return returns true if the given index is in check from black.
      */
     public boolean checkB(int i) {
         if (checkBishopB(i)) {
             return true;
         }
         if (checkRookB(i)) {
             return true;
         }
         if (checkKnightB(i)) {
             return true;
         }
         if (checkPawnB(i)) {
             return true;
         }
         if (checkKingB(i)) {
             return true;
         }
         return false;
     }
 
     /**
      * Method for checking if one of the black knights checks the white king
      *
      * @param i the index indicating the white kings position
      * @return Returns true if a black knight has the white king in check
      */
     public boolean checkKnightW(int i) {
         //Takes the index i and transforms it into two dimensional indexes
         int a = 0;
         int b = 0;
         if (i > 7) {
             a = i / 8;
             b = i - (a * 8);
         } else {
             b = i;
             a = 0;
         }
 
         //Checking if there`s a black knight in the bottom long left corner
         if ((a + 1 <= 7 && a + 1 >= 0) && (b - 2 <= 7 && b - 2 >= 0)) {
             //Checking if there`s a piecelabel in the bottom left corner
             if (twoTable[a + 1][b - 2] instanceof PieceLabel) {
                 //Getting the piece from the index and checks if its an instance of a black knight
                 if (twoTable[a + 1][b - 2].getPiece() instanceof KnightB) {
                     return true;
                 }
             }
         }
 
         //Checking if there`s a black knight in the long bottom right corner
         if ((a + 1 <= 7 && a + 1 >= 0) && (b + 2 <= 7 && b + 2 >= 0)) {
             if (twoTable[a + 1][b + 2] instanceof PieceLabel) {
                 if (twoTable[a + 1][b + 2].getPiece() instanceof KnightB) {
                     return true;
                 }
             }
         }
 
         //Checking if there`s a black knight in the short bottom left corner
         if ((a + 2 <= 7 && a + 2 >= 0) && (b - 1 <= 7 && b - 1 >= 0)) {
             if (twoTable[a + 2][b - 1] instanceof PieceLabel) {
                 if (twoTable[a + 2][b - 1].getPiece() instanceof KnightB) {
                     return true;
                 }
             }
         }
         //Checking if there`s a black knight in the short bottom right corner
         if ((a - 2 <= 7 && a - 2 >= 0) && (b + 1 <= 7 && b + 1 >= 0)) {
             if (twoTable[a - 2][b + 1] instanceof PieceLabel) {
                 if (twoTable[a - 2][b + 1].getPiece() instanceof KnightB) {
                     return true;
                 }
             }
         }
 
         //Checking if there`s a black knight in the long top left corner
         if ((a - 2 <= 7 && a - 2 >= 0) && (b - 1 <= 7 && b - 1 >= 0)) {
             if (twoTable[a - 2][b - 1] instanceof PieceLabel) {
                 if (twoTable[a - 2][b - 1].getPiece() instanceof KnightB) {
                     return true;
                 }
             }
         }
 
         //Checking if there`s a black knight in the long bottom right corner
         if ((a + 2 <= 7 && a + 2 >= 0) && (b + 1 <= 7 && b + 1 >= 0)) {
             if (twoTable[a + 2][b + 1] instanceof PieceLabel) {
                 if (twoTable[a + 2][b + 1].getPiece() instanceof KnightB) {
                     return true;
                 }
             }
         }
 
         //Checking if there`s a black knight in the short top left corner
         if ((a - 1 <= 7 && a - 1 >= 0) && (b - 2 <= 7 && b - 2 >= 0)) {
             if (twoTable[a - 1][b - 2] instanceof PieceLabel) {
                 if (twoTable[a - 1][b - 2].getPiece() instanceof KnightB) {
                     return true;
                 }
             }
         }
 
         //Checking if there`s a black knight in the short top right corner
         if ((a - 1 <= 7 && a - 1 >= 0) && (b + 2 <= 7 && b + 2 >= 0)) {
             if (twoTable[a - 1][b + 2] instanceof PieceLabel) {
                 if (twoTable[a - 1][b + 2].getPiece() instanceof KnightB) {
                     return true;
                 }
             }
         }
         return false;
     }
 
     /**
      * Method for checking if one of the black pawns checks the white king
      *
      * @param i the index indicating the white kings position
      * @return Returns true if a black pawn has the white king in check
      */
     public boolean checkPawnW(int i) {
         //Takes the index i and transforms it into two dimensional indexes
         int a = 0;
         int b = 0;
         if (i > 7) {
             a = i / 8;
             b = i - (a * 8);
         } else {
             b = i;
             a = 0;
         }
 
         //Checks if there`s a pawn in top left corner
         if (a - 1 < 8 && b - 1 < 8 && a - 1 >= 0 && b - 1 >= 0) {
             //Checks if there`s a piecelabel in top left corner
             if (twoTable[a - 1][b - 1] instanceof PieceLabel) {
                 //Checks if the piecelabel in top left corner is an instanceof of PawnB
                 if (twoTable[a - 1][b - 1].getPiece() instanceof PawnB) {
                     return true;
                 }
             }
         }
         //Checks if there`s a pawn in top right corner
        if (a - 1 < 8 && b + 1 < 8 && a + 1 >= 0 && b + 1 >= 0) {
             if (twoTable[a - 1][b + 1] instanceof PieceLabel) {
                 if (twoTable[a - 1][b + 1].getPiece() instanceof PawnB) {
                     return true;
                 }
             }
         }
         return false;
     }
 
     /**
      * Method for checking if one of the black rooks checks the white king
      *
      * @param i the index indicating the white kings position
      * @return Returns true if a black rook has the white king in check
      */
     public boolean checkRookW(int i) {
         //Takes the index i and transforms it into two dimensional indexes
         int a = 0;
         int b = 0;
         if (i > 7) {
             a = i / 8;
             b = i - (a * 8);
         } else {
             b = i;
             a = 0;
         }
         //Checks if there`s an instance of piecelabel in the line behind the white king
         for (int j = b; j < 8; j++) {
             if (twoTable[a][j] instanceof PieceLabel) {
                 //Checks if there`s an instance of a black rook or black queen in the line in front of the white king
                 if (twoTable[a][j].getPiece() instanceof RookB || twoTable[a][j].getPiece() instanceof QueenB) {
                     return true;
                 }
                 //Checks if there`s an instance of a black rook or black king in the line in front of the white king
                 if ((twoTable[a][j].getPiece() instanceof RookB) == false && (twoTable[a][j].getPiece() instanceof KingW) == false) {
                     break;
                 }
             }
         }
         //Checks if there`s an instance of piecelabel in the line in front of the white king
         for (int j = b; j >= 0; j--) {
             if (twoTable[a][j] instanceof PieceLabel) {
                 if (twoTable[a][j].getPiece() instanceof RookB || twoTable[a][j].getPiece() instanceof QueenB) {
                     return true;
                 }
                 if ((twoTable[a][j].getPiece() instanceof RookB) == false && (twoTable[a][j].getPiece() instanceof KingW) == false) {
                     break;
                 }
             }
         }
         //Checks if there`s an instance of piecelabel in the line to the right of the white king
         for (int j = a; j < 8; j++) {
             if (twoTable[j][b] instanceof PieceLabel) {
                 if (twoTable[j][b].getPiece() instanceof RookB || twoTable[j][b].getPiece() instanceof QueenB) {
                     return true;
                 }
                 if ((twoTable[j][b].getPiece() instanceof RookB) == false && (twoTable[j][b].getPiece() instanceof KingW) == false) {
                     break;
                 }
             }
         }
         //Checks if there`s an instance of piecelabel in the line to the left of the white king
         for (int j = a; j >= 0; j--) {
             if (twoTable[j][b] instanceof PieceLabel) {
                 if (twoTable[j][b].getPiece() instanceof RookB || twoTable[j][b].getPiece() instanceof QueenB) {
                     return true;
                 }
                 if ((twoTable[j][b].getPiece() instanceof RookB) == false && (twoTable[j][b].getPiece() instanceof KingW) == false) {
                     break;
                 }
             }
         }
         return false;
     }
 
     /**
      * Method for checking if one of the black bishops checks the white king
      *
      * @param i the index indicating the white kings position
      * @return Returns true if a black bishops has the white king in check
      */
     public boolean checkBishopW(int i) {
         int a = 0;
         int b = 0;
         if (i > 7) {
             a = i / 8;
             b = i - (a * 8);
         } else {
             b = i;
             a = 0;
         }
         int c = a;
         int d = b;
         //Checking if there`s a piecelabel in top left corner line of the white king
         for (int j = 0; j < 8; j++) {
             if (twoTable[c][d] instanceof PieceLabel) {
                 //Checking if there`s a black bishop or black queen in top left corner line of the white king
                 if (twoTable[c][d].getPiece() instanceof BishopB || twoTable[c][d].getPiece() instanceof QueenB) {
                     return true;
                 }
                 //Checking if there`s a black bishop or black king in top left corner line of the white king
                 if ((twoTable[c][d].getPiece() instanceof BishopB) == false && (twoTable[c][d].getPiece() instanceof KingW) == false) {
                     break;
                 }
             }
             //Subtracting the counters for each walkthrough of the loop
             if (c > 0 && d > 0) {
                 c--;
                 d--;
             }
         }
         // Resetting the counters
         c = a;
         d = b;
         //Checking if there`s a piecelabel in bottom right corner line of the white king
         for (int j = 0; j < 8; j++) {
             if (twoTable[c][d] instanceof PieceLabel) {
                 if (twoTable[c][d].getPiece() instanceof BishopB || twoTable[c][d].getPiece() instanceof QueenB) {
                     return true;
                 }
                 if ((twoTable[c][d].getPiece() instanceof BishopB) == false && (twoTable[c][d].getPiece() instanceof KingW) == false) {
                     break;
                 }
             }
             if (c < 7 && d < 7) {
                 c++;
                 d++;
             }
         }
         c = a;
         d = b;
         //Checking if there`s a piecelabel in bottom left corner line of the white king
         for (int j = 0; j < 8; j++) {
             if (twoTable[c][d] instanceof PieceLabel) {
                 if (twoTable[c][d].getPiece() instanceof BishopB || twoTable[c][d].getPiece() instanceof QueenB) {
                     return true;
                 }
                 if ((twoTable[c][d].getPiece() instanceof BishopB) == false && (twoTable[c][d].getPiece() instanceof KingW) == false) {
                     break;
                 }
             }
             if (c < 7 && d > 0) {
                 c++;
                 d--;
             }
         }
         c = a;
         d = b;
         //Checking if there`s a piecelabel in top right corner line of the white king
         for (int j = 0; j < 8; j++) {
             if (twoTable[c][d] instanceof PieceLabel) {
                 if (twoTable[c][d].getPiece() instanceof BishopB || twoTable[c][d].getPiece() instanceof QueenB) {
                     return true;
                 }
                 if ((twoTable[c][d].getPiece() instanceof BishopB) == false && (twoTable[c][d].getPiece() instanceof KingW) == false) {
                     break;
                 }
             }
             if (c > 0 && d < 7) {
                 c--;
                 d++;
             }
         }
         return false;
     }
 
     /**
      * Method for checking if the black king checks the white king
      *
      * @param i the index indicating the white kings position
      * @return Returns true if a black king has the white king in check
      */
     public boolean checkKingW(int i) {
         int a = 0;
         int b = 0;
         if (i > 7) {
             a = i / 8;
             b = i - (a * 8);
         } else {
             b = i;
             a = 0;
         }
         //Controlling the indexes of the arrays so the check won`t get an exception
         if ((a - 1 <= 7 && a - 1 >= 0) && (b - 1 <= 7 && b - 1 >= 0)) {
             //Checking if there`s a piecelabel in top left corner of the white king
             if (twoTable[a - 1][b - 1] instanceof PieceLabel) {
                 //Checking if there`s a black king in top left corner of the white king
                 if (twoTable[a - 1][b - 1].getPiece() instanceof KingB) {
                     return true;
                 }
             }
         }
         //Checking if there`s a piecelabel in top right corner of the white king
         if ((a - 1 <= 7 && a - 1 >= 0) && (b + 1 <= 7 && b + 1 >= 0)) {
             if (twoTable[a - 1][b + 1] instanceof PieceLabel) {
                 if (twoTable[a - 1][b + 1].getPiece() instanceof KingB) {
                     return true;
                 }
             }
         }
         //Checking if there`s a piecelabel in top of the white king
         if ((a - 1 <= 7 && a - 1 >= 0) && (b <= 7 && b >= 0)) {
             if (twoTable[a - 1][b] instanceof PieceLabel) {
                 if (twoTable[a - 1][b].getPiece() instanceof KingB) {
                     return true;
                 }
             }
         }
         //Checking if there`s a piecelabel to the left of the white king
         if ((a <= 7 && a >= 0) && (b - 1 <= 7 && b - 1 >= 0)) {
             if (twoTable[a][b - 1] instanceof PieceLabel) {
                 if (twoTable[a][b - 1].getPiece() instanceof KingB) {
                     return true;
                 }
             }
         }
         //Checking if there`s a piecelabel to the right of the white king
         if ((a <= 7 && a >= 0) && (b + 1 <= 7 && b + 1 >= 0)) {
             if (twoTable[a][b + 1] instanceof PieceLabel) {
                 if (twoTable[a][b + 1].getPiece() instanceof KingB) {
                     return true;
                 }
             }
         }
         //Checking if there`s a piecelabel in the bottom left corner of the white king
         if ((a + 1 <= 7 && a + 1 >= 0) && (b - 1 <= 7 && b - 1 >= 0)) {
             if (twoTable[a + 1][b - 1] instanceof PieceLabel) {
                 if (twoTable[a + 1][b - 1].getPiece() instanceof KingB) {
                     return true;
                 }
             }
         }
         //Checking if there`s a piecelabel in the bottom of the white king
         if ((a + 1 <= 7 && a + 1 >= 0) && (b <= 7 && b >= 0)) {
             if (twoTable[a + 1][b] instanceof PieceLabel) {
                 if (twoTable[a + 1][b].getPiece() instanceof KingB) {
                     return true;
                 }
             }
         }
         //Checking if there`s a piecelabel in the bottom right corner of the white king
         if ((a + 1 <= 7 && a + 1 >= 0) && (b + 1 <= 7 && b + 1 >= 0)) {
             if (twoTable[a + 1][b + 1] instanceof PieceLabel) {
                 if (twoTable[a + 1][b + 1].getPiece() instanceof KingB) {
                     return true;
                 }
             }
         }
         return false;
 
     }
 
     /**
      * Method for checking if one of the white knights checks the black king
      *
      * @param i the index indicating the black kings position
      * @return Returns true if a white knight has the black king in check
      */
     public boolean checkKnightB(int i) {
         int a = 0;
         int b = 0;
         if (i > 7) {
             a = i / 8;
             b = i - (a * 8);
         } else {
             b = i;
             a = 0;
         }
         //Controlling the indexes of the arrays so the check won`t get an exception
         if ((a + 1 <= 7 && a + 1 >= 0) && (b - 2 <= 7 && b - 2 >= 0)) {
             //Checking short left bottom corner if there`s an instance of a piecelabel
             if (twoTable[a + 1][b - 2] instanceof PieceLabel) {
                 //Checking short left bottom corner if there`s an instance of a white knight
                 if (twoTable[a + 1][b - 2].getPiece() instanceof KnightW) {
                     return true;
                 }
             }
         }
         //Checking short right bottom corner if there`s a white knight there
         if ((a + 1 <= 7 && a + 1 >= 0) && (b + 2 <= 7 && b + 2 >= 0)) {
             if (twoTable[a + 1][b + 2] instanceof PieceLabel) {
                 if (twoTable[a + 1][b + 2].getPiece() instanceof KnightW) {
                     return true;
                 }
             }
         }
         //Checking long left bottom corner if there`s a white knight there
         if ((a + 2 <= 7 && a + 2 >= 0) && (b - 1 <= 7 && b - 1 >= 0)) {
             if (twoTable[a + 2][b - 1] instanceof PieceLabel) {
                 if (twoTable[a + 2][b - 1].getPiece() instanceof KnightW) {
                     return true;
                 }
             }
         }
         //Checking long right top corner if there`s a white knight there
         if ((a - 2 <= 7 && a - 2 >= 0) && (b + 1 <= 7 && b + 1 >= 0)) {
             if (twoTable[a - 2][b + 1] instanceof PieceLabel) {
                 if (twoTable[a - 2][b + 1].getPiece() instanceof KnightW) {
                     return true;
                 }
             }
         }
         //Checking long left top corner if there`s a white knight there
         if ((a - 2 <= 7 && a - 2 >= 0) && (b - 1 <= 7 && b - 1 >= 0)) {
             if (twoTable[a - 2][b - 1] instanceof PieceLabel) {
                 if (twoTable[a - 2][b - 1].getPiece() instanceof KnightW) {
                     return true;
                 }
             }
         }
         //Checking long right bottom corner if there`s a white knight there
         if ((a + 2 <= 7 && a + 2 >= 0) && (b + 1 <= 7 && b + 1 >= 0)) {
             if (twoTable[a + 2][b + 1] instanceof PieceLabel) {
                 if (twoTable[a + 2][b + 1].getPiece() instanceof KnightW) {
                     return true;
                 }
             }
         }
         //Checking short left top corner if there`s a white knight there
         if ((a - 1 <= 7 && a - 1 >= 0) && (b - 2 <= 7 && b - 2 >= 0)) {
             if (twoTable[a - 1][b - 2] instanceof PieceLabel) {
                 if (twoTable[a - 1][b - 2].getPiece() instanceof KnightW) {
                     return true;
                 }
             }
         }
         //Checking short right top corner if there`s a white knight there
         if ((a - 1 <= 7 && a - 1 >= 0) && (b + 2 <= 7 && b + 2 >= 0)) {
             if (twoTable[a - 1][b + 2] instanceof PieceLabel) {
                 if (twoTable[a - 1][b + 2].getPiece() instanceof KnightW) {
                     return true;
                 }
             }
         }
         return false;
     }
 
     /**
      * Method for checking if one of the white pawns checks the black king
      *
      * @param i the index indicating the black kings position
      * @return Returns true if a white pawns has the black king in check
      */
     public boolean checkPawnB(int i) {
         int a = 0;
         int b = 0;
         if (i > 7) {
             a = i / 8;
             b = i - (a * 8);
         } else {
             b = i;
             a = 0;
         }
         //Checking indexes to the array so we dont get exceptions
         if (a + 1 < 8 && b - 1 < 8 && a + 1 >= 0 && b - 1 >= 0) {
             //Checking left bottom corner if there`s a piecelabel there
             if (twoTable[a + 1][b - 1] instanceof PieceLabel) {
                 //Checking left bottom corner if there`s a white pawn there
                 if (twoTable[a + 1][b - 1].getPiece() instanceof PawnW) {
                     return true;
                 }
             }
         }
         //Checking right bottom corner if there`s a white pawn there
         if (a + 1 < 8 && b + 1 < 8 && a + 1 >= 0 && b + 1 >= 0) {
             if (twoTable[a + 1][b + 1] instanceof PieceLabel) {
                 if (twoTable[a + 1][b + 1].getPiece() instanceof PawnW) {
                     return true;
                 }
             }
         }
         return false;
     }
 
     /**
      * Method for checking if one of the white rooks checks the black king
      *
      * @param i the index indicating the black kings position
      * @return Returns true if a white rooks has the black king in check
      */
     public boolean checkRookB(int i) {
         int a = 0;
         int b = 0;
         if (i > 7) {
             a = i / 8;
             b = i - (a * 8);
         } else {
             b = i;
             a = 0;
         }
         for (int j = b; j < 8; j++) {
             //Checks if there`s an instance of piecelabel in the line to the right of the black king
             if (twoTable[a][j] instanceof PieceLabel) {
                 //Checks if there`s an instance of a white rook or white queen in the line to the right of the black king
                 if (twoTable[a][j].getPiece() instanceof RookW || twoTable[a][j].getPiece() instanceof QueenW) {
                     return true;
                 }
                 //Checks if there`s no instance of a white rook or white king in the line to the right of the black king
                 if ((twoTable[a][j].getPiece() instanceof RookW) == false && (twoTable[a][j].getPiece() instanceof KingB) == false) {
                     break;
                 }
             }
         }
         //Checks if there`s an instance of a white rook in the line to the left of black king
         for (int j = b; j >= 0; j--) {
             if (twoTable[a][j] instanceof PieceLabel) {
                 if (twoTable[a][j].getPiece() instanceof RookW || twoTable[a][j].getPiece() instanceof QueenW) {
                     return true;
                 }
                 if ((twoTable[a][j].getPiece() instanceof RookW) == false && (twoTable[a][j].getPiece() instanceof KingB) == false) {
                     break;
                 }
             }
         }
         //Checks if there`s an instance of piecelabel in the line under the white king
         for (int j = a; j < 8; j++) {
             if (twoTable[j][b] instanceof PieceLabel) {
                 if (twoTable[j][b].getPiece() instanceof RookW || twoTable[j][b].getPiece() instanceof QueenW) {
                     return true;
                 }
                 if ((twoTable[j][b].getPiece() instanceof RookW) == false && (twoTable[j][b].getPiece() instanceof KingB) == false) {
                     break;
                 }
             }
         }
         //Checks if there`s an instance of piecelabel in the line over the white king
         for (int j = a; j >= 0; j--) {
             if (twoTable[j][b] instanceof PieceLabel) {
                 if (twoTable[j][b].getPiece() instanceof RookW || twoTable[j][b].getPiece() instanceof QueenW) {
                     return true;
                 }
                 if ((twoTable[j][b].getPiece() instanceof RookW) == false && (twoTable[j][b].getPiece() instanceof KingB) == false) {
                     break;
                 }
             }
         }
         return false;
     }
 
     /**
      * Method for checking if one of the white bishops checks the black king
      *
      * @param i the index indicating the black kings position
      * @return Returns true if a white bishops has the black king in check
      */
     public boolean checkBishopB(int i) {
         int a = 0;
         int b = 0;
         if (i > 7) {
             a = i / 8;
             b = i - (a * 8);
         } else {
             b = i;
             a = 0;
         }
         int c = a;
         int d = b;
         //Checks if one of the white bishops checks the black king in the top left corner line        
         for (int j = 0; j < 8; j++) {
             //Checks if there`s a piecelabel in the top left corner
             if (twoTable[c][d] instanceof PieceLabel) {
                 //Checks if there`s a white bishop or a queen in the top left corner
                 if (twoTable[c][d].getPiece() instanceof BishopW || twoTable[c][d].getPiece() instanceof QueenW) {
                     return true;
                 }
                 //Checks if there`s a no white bishop or queen in the top left corner and then breaks
                 if ((twoTable[c][d].getPiece() instanceof BishopW) == false && (twoTable[c][d].getPiece() instanceof KingB) == false) {
                     break;
                 }
             }
             //Subtracting the counters
             if (c > 0 && d > 0) {
                 c--;
                 d--;
             }
 
         }
         //Resetting the counters
         c = a;
         d = b;
         //Checks if one of the white bishops checks the black king in the bottom right corner line
         for (int j = 0; j < 8; j++) {
             if (twoTable[c][d] instanceof PieceLabel) {
                 if (twoTable[c][d].getPiece() instanceof BishopW || twoTable[c][d].getPiece() instanceof QueenW) {
                     return true;
                 }
                 if ((twoTable[c][d].getPiece() instanceof BishopW) == false && (twoTable[c][d].getPiece() instanceof KingB) == false) {
                     break;
                 }
             }
             if (c < 7 && d < 7) {
                 c++;
                 d++;
             }
         }
         c = a;
         d = b;
         //Checks if one of the white bishops checks the black king in the bottom left corner line
         for (int j = 0; j < 8; j++) {
             if (twoTable[c][d] instanceof PieceLabel) {
                 if (twoTable[c][d].getPiece() instanceof BishopW || twoTable[c][d].getPiece() instanceof QueenW) {
                     return true;
                 }
                 if ((twoTable[c][d].getPiece() instanceof BishopW) == false && (twoTable[c][d].getPiece() instanceof KingB) == false) {
                     break;
                 }
             }
             if (c < 7 && d > 0) {
                 c++;
                 d--;
             }
         }
         c = a;
         d = b;
         //Checks if one of the white bishops checks the black king in the top right corner line
         for (int j = 0; j < 8; j++) {
             if (twoTable[c][d] instanceof PieceLabel) {
                 if (twoTable[c][d].getPiece() instanceof BishopW || twoTable[c][d].getPiece() instanceof QueenW) {
                     return true;
                 }
                 if ((twoTable[c][d].getPiece() instanceof BishopW) == false && (twoTable[c][d].getPiece() instanceof KingB) == false) {
                     break;
                 }
             }
             if (c > 0 && d < 7) {
                 c--;
                 d++;
             }
         }
         return false;
     }
 
     /**
      * Method for checking if one of the white bishops checks the black king
      *
      * @param i the index indicating the black kings position
      * @return Returns true if a white bishops has the black king in check
      */
     public boolean checkKingB(int i) {
         int a = 0;
         int b = 0;
         if (i > 7) {
             a = i / 8;
             b = i - (a * 8);
         } else {
             b = i;
             a = 0;
         }
         //Checks the top left index so we wont get exceptions
         if ((a - 1 <= 7 && a - 1 >= 0) && (b - 1 <= 7 && b - 1 >= 0)) {
             //Checks the index so for piecelabels
             if (twoTable[a - 1][b - 1] instanceof PieceLabel) {
                 //Checks the index for a white king
                 if (twoTable[a - 1][b - 1].getPiece() instanceof KingW) {
                     return true;
                 }
             }
         }
         //Checks the top right index of the black king for a white king
         if ((a - 1 <= 7 && a - 1 >= 0) && (b + 1 <= 7 && b + 1 >= 0)) {
             if (twoTable[a - 1][b + 1] instanceof PieceLabel) {
                 if (twoTable[a - 1][b + 1].getPiece() instanceof KingW) {
                     return true;
                 }
             }
         }
         //Checks the index in the top of the black king for a white king
         if ((a - 1 <= 7 && a - 1 >= 0) && (b <= 7 && b >= 0)) {
             if (twoTable[a - 1][b] instanceof PieceLabel) {
                 if (twoTable[a - 1][b].getPiece() instanceof KingW) {
                     return true;
                 }
             }
         }
         //Checks the index to the left of the black king for a white king
         if ((a <= 7 && a >= 0) && (b - 1 <= 7 && b - 1 >= 0)) {
             if (twoTable[a][b - 1] instanceof PieceLabel) {
                 if (twoTable[a][b - 1].getPiece() instanceof KingW) {
                     return true;
                 }
             }
         }
         //Checks the index to the right of the black king for a white king
         if ((a <= 7 && a >= 0) && (b + 1 <= 7 && b + 1 >= 0)) {
             if (twoTable[a][b + 1] instanceof PieceLabel) {
                 if (twoTable[a][b + 1].getPiece() instanceof KingW) {
                     return true;
                 }
             }
         }
         //Checks the index in bottom left corner of the black king for a white king
         if ((a + 1 <= 7 && a + 1 >= 0) && (b - 1 <= 7 && b - 1 >= 0)) {
             if (twoTable[a + 1][b - 1] instanceof PieceLabel) {
                 if (twoTable[a + 1][b - 1].getPiece() instanceof KingW) {
                     return true;
                 }
             }
         }
         //Checks the index in the bottom of the black king for a white king
         if ((a + 1 <= 7 && a + 1 >= 0) && (b <= 7 && b >= 0)) {
             if (twoTable[a + 1][b] instanceof PieceLabel) {
                 if (twoTable[a + 1][b].getPiece() instanceof KingW) {
                     return true;
                 }
             }
         }
         //Checks the index to the bottom right of the black king for a white king
         if ((a + 1 <= 7 && a + 1 >= 0) && (b + 1 <= 7 && b + 1 >= 0)) {
             if (twoTable[a + 1][b + 1] instanceof PieceLabel) {
                 if (twoTable[a + 1][b + 1].getPiece() instanceof KingW) {
                     return true;
                 }
             }
         }
         return false;
 
     }
 
     /**
      * Method for checking which piece is moved and returns a table of integers
      * with indexes of which panels who should be colored green
      *
      * @param i index indicating the starting point
      * @param p The type of piece to be checked
      * @return Returns a table of indexes of wich to color.
      */
     public int[] colorMoves(int i, Piece p) {
         int[] list = new int[0];
         if (p instanceof KnightW || p instanceof KnightB) {
             list = colorKnight(i, p);
         }
         if (p instanceof BishopW || p instanceof BishopB) {
             list = colorBishop(i, p);
         }
         if (p instanceof KingW || p instanceof KingB) {
             list = colorKing(i, p);
         }
         if (p instanceof PawnW || p instanceof PawnB) {
             list = colorPawn(i, p);
         }
         if (p instanceof RookW || p instanceof RookB) {
             list = colorRook(i, p);
         }
         if (p instanceof QueenW || p instanceof QueenB) {
             list = colorQueen(i, p);
         }
         return list;
     }
 
     /**
      * Method for checking which piece is moved and returns a table of integers
      * with indexes of which panels who should be colored blue
      *
      * @param i index indicating the starting position
      * @param p the piece to be checked
      * @param castlingL whether casting can be performed with the left rook
      * @param castlingR whether casting can be performed with the left rook
      * @param castlingK whether casting can be performed with the left rook
      * @param passant whether the en-passant move is allowed.
      * @param j Index indicating where the en-passant move is allowed
      * @return Returns a table of indexes of wich squares to color blue.
      */
     public int[] colorSpecialMoves(int i, Piece p, boolean castlingL, boolean castlingR, boolean castlingK, boolean passant, int j) {
         int[] list = new int[0];
 
         if ((p instanceof KingW || p instanceof KingB) && castlingK == false) {
             list = colorCastling(i, castlingL, castlingR, castlingK);
         }
         if (p instanceof PawnW || p instanceof PawnB) {
             list = colorPassant(i, passant, j, p);
         }
         return list;
     }
 
     /**
      * Method for coloring the bishops legal moves
      *
      * @param i Starting position
      * @param p Piece to be moved
      * @return Table of indexes to be colored
      */
     public int[] colorBishop(int i, Piece p) {
         ArrayList<Integer> array = new ArrayList<Integer>();
         int a = 0;
         int b = 0;
         if (i > 7) {
             a = i / 8;
             b = i - (a * 8);
         } else {
             b = i;
             a = 0;
         }
         int c = a;
         int d = b;
         //Checks the top left corner line from the bishop for legal moves
         for (int j = 0; j < 8; j++) {
             //Checks the indexes so we won`t get exceptions
             if ((c <= 7 && c >= 0) && (d <= 7 && d >= 0)) {
                 //Checks the indexes for piecelabels
                 if (twoTable[c][d] instanceof PieceLabel) {
                     //Checks the team of the piece and compares it to the found piece at the end of the line
                     if (twoTable[c][d].getPiece().getTeam() != p.getTeam()) {
                         array.add((c) * 8 + (d));
                     }
                 }
                 //Adding the panel to the array if no piecelabel is found
                 if (!(twoTable[c][d] instanceof PieceLabel)) {
                     array.add((c) * 8 + (d));
                 }
             }
             //Subtracting the counters
             if (c > 0 && d > 0) {
                 c--;
                 d--;
             }
             //Making a new check which is similar to the one over, but breaks if a hostile piece is found
             if (twoTable[c][d] instanceof PieceLabel) {
                 if (twoTable[c][d].getPiece().getTeam() != p.getTeam()) {
                     array.add((c) * 8 + (d));
                 }
                 break;
             }
         }
         //Resets the counters
         c = a;
         d = b;
         //Checks the bottom right corner line from the bishop for legal moves
         for (int j = 0; j < 8; j++) {
             if ((c <= 7 && c >= 0) && (d <= 7 && d >= 0)) {
                 if (twoTable[c][d] instanceof PieceLabel) {
                     if (twoTable[c][d].getPiece().getTeam() != p.getTeam()) {
                         array.add((c) * 8 + (d));
                     }
                 }
                 if (!(twoTable[c][d] instanceof PieceLabel)) {
                     array.add((c) * 8 + (d));
                 }
             }
             if (c < 7 && d < 7) {
                 c++;
                 d++;
             }
             if (twoTable[c][d] instanceof PieceLabel) {
                 if (twoTable[c][d].getPiece().getTeam() != p.getTeam()) {
                     array.add((c) * 8 + (d));
                 }
                 break;
             }
         }
         c = a;
         d = b;
         //Checks the top right corner line from the bishop for legal moves
         for (int j = 0; j < 8; j++) {
             if ((c <= 7 && c >= 0) && (d <= 7 && d >= 0)) {
                 if (twoTable[c][d] instanceof PieceLabel) {
                     if (twoTable[c][d].getPiece().getTeam() != p.getTeam()) {
                         array.add((c) * 8 + (d));
                     }
                 }
                 if (!(twoTable[c][d] instanceof PieceLabel)) {
                     array.add((c) * 8 + (d));
                 }
             }
             if (c > 0 && d < 7) {
                 c--;
                 d++;
             }
             if (twoTable[c][d] instanceof PieceLabel) {
                 if (twoTable[c][d].getPiece().getTeam() != p.getTeam()) {
                     array.add((c) * 8 + (d));
                 }
                 break;
             }
         }
         c = a;
         d = b;
         //Checks the bottom left corner line from the bishop for legal moves
         for (int j = 0; j < 8; j++) {
             if ((c <= 7 && c >= 0) && (d <= 7 && d >= 0)) {
                 if (twoTable[c][d] instanceof PieceLabel) {
                     if (twoTable[c][d].getPiece().getTeam() != p.getTeam()) {
                         array.add((c) * 8 + (d));
                     }
                 }
                 if (!(twoTable[c][d] instanceof PieceLabel)) {
                     array.add((c) * 8 + (d));
                 }
             }
             if (c < 7 && d > 0) {
                 c++;
                 d--;
             }
             if (twoTable[c][d] instanceof PieceLabel) {
                 if (twoTable[c][d].getPiece().getTeam() != p.getTeam()) {
                     array.add((c) * 8 + (d));
                 }
                 break;
             }
         }
         int[] list = new int[array.size()];
         for (int y = 0; y < array.size(); y++) {
             list[y] = array.get(y);
         }
         return list;
     }
 
     /**
      * Method for coloring the knights legal moves
      *
      * @param i Starting position
      * @param p Piece to be moved
      * @return Table of indexes to be colored
      */
     public int[] colorKnight(int i, Piece p) {
         ArrayList<Integer> array = new ArrayList<Integer>();
         int a = 0;
         int b = 0;
         if (i > 7) {
             a = i / 8;
             b = i - (a * 8);
         } else {
             b = i;
             a = 0;
         }
         //Checks the indexes so we won`t get exceptions
         if ((a + 1 <= 7 && a + 1 >= 0) && (b - 2 <= 7 && b - 2 >= 0)) {
             //Checks the indexes for piecelabels
             if (twoTable[a + 1][b - 2] instanceof PieceLabel) {
                 //Checks the team of the piece and compares it to the found piece at the end of the line
                 if (twoTable[a + 1][b - 2].getPiece().getTeam() != p.getTeam()) {
                     array.add((a + 1) * 8 + (b - 2));
                 }
             }
             //Checks the team of the piece and compares it to the found piece at the end of the line
             if (!(twoTable[a + 1][b - 2] instanceof PieceLabel)) {
                 array.add((a + 1) * 8 + (b - 2));
 
             }
         }
         /*
          * Checks the different legal moves of the horse if they should be
          * colored
          */
         if ((a + 1 <= 7 && a + 1 >= 0) && (b + 2 <= 7 && b + 2 >= 0)) {
             if (twoTable[a + 1][b + 2] instanceof PieceLabel) {
                 if (twoTable[a + 1][b + 2].getPiece().getTeam() != p.getTeam()) {
                     array.add((a + 1) * 8 + (b + 2));
                 }
             }
             if (!(twoTable[a + 1][b + 2] instanceof PieceLabel)) {
                 array.add((a + 1) * 8 + (b + 2));
             }
         }
         if ((a + 2 <= 7 && a + 2 >= 0) && (b - 1 <= 7 && b - 1 >= 0)) {
             if (twoTable[a + 2][b - 1] instanceof PieceLabel) {
                 if (twoTable[a + 2][b - 1].getPiece().getTeam() != p.getTeam()) {
                     array.add((a + 2) * 8 + (b - 1));
                 }
             }
             if (!(twoTable[a + 2][b - 1] instanceof PieceLabel)) {
                 array.add((a + 2) * 8 + (b - 1));
             }
         }
         if ((a - 2 <= 7 && a - 2 >= 0) && (b + 1 <= 7 && b + 1 >= 0)) {
             if (twoTable[a - 2][b + 1] instanceof PieceLabel) {
                 if (twoTable[a - 2][b + 1].getPiece().getTeam() != p.getTeam()) {
                     array.add((a - 2) * 8 + (b + 1));
                 }
             }
             if (!(twoTable[a - 2][b + 1] instanceof PieceLabel)) {
                 array.add((a - 2) * 8 + (b + 1));
             }
         }
         if ((a - 2 <= 7 && a - 2 >= 0) && (b - 1 <= 7 && b - 1 >= 0)) {
             if (twoTable[a - 2][b - 1] instanceof PieceLabel) {
                 if (twoTable[a - 2][b - 1].getPiece().getTeam() != p.getTeam()) {
                     array.add((a - 2) * 8 + (b - 1));
                 }
             }
             if (!(twoTable[a - 2][b - 1] instanceof PieceLabel)) {
                 array.add((a - 2) * 8 + (b - 1));
             }
         }
         if ((a - 2 <= 7 && a - 2 >= 0) && (b + 1 <= 7 && b + 1 >= 0)) {
             if (twoTable[a - 2][b + 1] instanceof PieceLabel) {
                 if (twoTable[a - 2][b + 1].getPiece().getTeam() != p.getTeam()) {
                     array.add((a - 2) * 8 + (b + 1));
                 }
             }
             if (!(twoTable[a - 2][b + 1] instanceof PieceLabel)) {
                 array.add((a - 2) * 8 + (b + 1));
             }
         }
         if ((a - 1 <= 7 && a - 1 >= 0) && (b - 2 <= 7 && b - 2 >= 0)) {
             if (twoTable[a - 1][b - 2] instanceof PieceLabel) {
                 if (twoTable[a - 1][b - 2].getPiece().getTeam() != p.getTeam()) {
                     array.add((a - 1) * 8 + (b - 2));
                 }
             }
             if (!(twoTable[a - 1][b - 2] instanceof PieceLabel)) {
                 array.add((a - 1) * 8 + (b - 2));
             }
         }
         if ((a - 1 <= 7 && a - 1 >= 0) && (b + 2 <= 7 && b + 2 >= 0)) {
             if (twoTable[a - 1][b + 2] instanceof PieceLabel) {
                 if (twoTable[a - 1][b + 2].getPiece().getTeam() != p.getTeam()) {
                     array.add((a - 1) * 8 + (b + 2));
                 }
             }
             if (!(twoTable[a - 1][b + 2] instanceof PieceLabel)) {
                 array.add((a - 1) * 8 + (b + 2));
             }
         }
         if ((a + 2 <= 7 && a + 2 >= 0) && (b + 1 <= 7 && b + 1 >= 0)) {
             if (twoTable[a + 2][b + 1] instanceof PieceLabel) {
                 if (twoTable[a + 2][b + 1].getPiece().getTeam() != p.getTeam()) {
                     array.add((a + 2) * 8 + (b + 1));
                 }
             }
             if (!(twoTable[a + 2][b + 1] instanceof PieceLabel)) {
                 array.add((a + 2) * 8 + (b + 1));
             }
         }
 
         int[] list = new int[array.size()];
         for (int y = 0; y < array.size(); y++) {
             list[y] = array.get(y);
         }
         return list;
     }
 
     /**
      * Method for coloring the kings legal moves
      *
      * @param i Starting position
      * @param p Piece to be moved
      * @return Table of indexes to be colored
      */
     public int[] colorKing(int i, Piece p) {
         ArrayList<Integer> array = new ArrayList<Integer>();
         int a = 0;
         int b = 0;
         if (i > 7) {
             a = i / 8;
             b = i - (a * 8);
         } else {
             b = i;
             a = 0;
         }
         //Checks top left corner
         if ((a - 1 <= 7 && a - 1 >= 0) && (b - 1 <= 7 && b - 1 >= 0)) {
             if ((p.getTeam() == 1 && checkW((a - 1) * 8 + (b - 1)) == false) || (p.getTeam() == 2 && checkB((a - 1) * 8 + (b - 1)) == false)) {
                 if (twoTable[a - 1][b - 1] instanceof PieceLabel) {
                     if (twoTable[a - 1][b - 1].getPiece().getTeam() != p.getTeam()) {
                         array.add((a - 1) * 8 + (b - 1));
                     }
                 }
                 if (!(twoTable[a - 1][b - 1] instanceof PieceLabel)) {
                     array.add((a - 1) * 8 + (b - 1));
                 }
             }
         }
         //Checks top right corner
         if ((a - 1 <= 7 && a - 1 >= 0) && (b + 1 <= 7 && b + 1 >= 0)) {
             if ((p.getTeam() == 1 && checkW((a - 1) * 8 + (b + 1)) == false) || (p.getTeam() == 2 && checkB((a - 1) * 8 + (b + 1)) == false)) {
                 if (twoTable[a - 1][b + 1] instanceof PieceLabel) {
                     if (twoTable[a - 1][b + 1].getPiece().getTeam() != p.getTeam()) {
                         array.add((a - 1) * 8 + (b + 1));
                     }
                 }
                 if (!(twoTable[a - 1][b + 1] instanceof PieceLabel)) {
                     array.add((a - 1) * 8 + (b + 1));
                 }
             }
         }
         //Checks the top
         if ((a - 1 <= 7 && a - 1 >= 0) && (b <= 7 && b >= 0)) {
             if ((p.getTeam() == 1 && checkW((a - 1) * 8 + (b)) == false) || (p.getTeam() == 2 && checkB((a - 1) * 8 + (b)) == false)) {
                 if (twoTable[a - 1][b] instanceof PieceLabel) {
                     if (twoTable[a - 1][b].getPiece().getTeam() != p.getTeam()) {
                         array.add((a - 1) * 8 + (b));
                     }
                 }
                 if (!(twoTable[a - 1][b] instanceof PieceLabel)) {
                     array.add((a - 1) * 8 + (b));
                 }
             }
         }
         //Checks left
         if ((a <= 7 && a >= 0) && (b - 1 <= 7 && b - 1 >= 0)) {
             if ((p.getTeam() == 1 && checkW((a) * 8 + (b - 1)) == false) || (p.getTeam() == 2 && checkB((a) * 8 + (b - 1)) == false)) {
                 if (twoTable[a][b - 1] instanceof PieceLabel) {
                     if (twoTable[a][b - 1].getPiece().getTeam() != p.getTeam()) {
                         array.add((a) * 8 + (b - 1));
                     }
                 }
                 if (!(twoTable[a][b - 1] instanceof PieceLabel)) {
                     array.add((a) * 8 + (b - 1));
                 }
             }
         }
         //Checks right
         if ((a <= 7 && a >= 0) && (b + 1 <= 7 && b + 1 >= 0)) {
             if ((p.getTeam() == 1 && checkW((a) * 8 + (b + 1)) == false) || (p.getTeam() == 2 && checkB((a) * 8 + (b + 1)) == false)) {
                 if (twoTable[a][b + 1] instanceof PieceLabel) {
                     if (twoTable[a][b + 1].getPiece().getTeam() != p.getTeam()) {
                         array.add((a) * 8 + (b + 1));
                     }
                 }
                 if (!(twoTable[a][b + 1] instanceof PieceLabel)) {
                     array.add((a) * 8 + (b + 1));
                 }
             }
         }
         //Checks bottom left
         if ((a + 1 <= 7 && a + 1 >= 0) && (b - 1 <= 7 && b - 1 >= 0)) {
             if ((p.getTeam() == 1 && checkW((a + 1) * 8 + (b - 1)) == false) || (p.getTeam() == 2 && checkB((a + 1) * 8 + (b - 1)) == false)) {
                 if (twoTable[a + 1][b - 1] instanceof PieceLabel) {
                     if (twoTable[a + 1][b - 1].getPiece().getTeam() != p.getTeam()) {
                         array.add((a + 1) * 8 + (b - 1));
                     }
                 }
                 if (!(twoTable[a + 1][b - 1] instanceof PieceLabel)) {
                     array.add((a + 1) * 8 + (b - 1));
                 }
             }
         }
         //Checks bottom
         if ((a + 1 <= 7 && a + 1 >= 0) && (b <= 7 && b >= 0)) {
             if ((p.getTeam() == 1 && checkW((a + 1) * 8 + (b)) == false) || (p.getTeam() == 2 && checkB((a + 1) * 8 + (b)) == false)) {
                 if (twoTable[a + 1][b] instanceof PieceLabel) {
                     if (twoTable[a + 1][b].getPiece().getTeam() != p.getTeam()) {
                         array.add((a + 1) * 8 + (b));
                     }
                 }
                 if (!(twoTable[a + 1][b] instanceof PieceLabel)) {
                     array.add((a + 1) * 8 + (b));
                 }
             }
         }
         //Checks bottom right
         if ((a + 1 <= 7 && a + 1 >= 0) && (b + 1 <= 7 && b + 1 >= 0)) {
             if ((p.getTeam() == 1 && checkW((a + 1) * 8 + (b + 1)) == false) || (p.getTeam() == 2 && checkB((a + 1) * 8 + (b + 1)) == false)) {
                 if (twoTable[a + 1][b + 1] instanceof PieceLabel) {
                     if (twoTable[a + 1][b + 1].getPiece().getTeam() != p.getTeam()) {
                         array.add((a + 1) * 8 + (b + 1));
                     }
                 }
                 if (!(twoTable[a + 1][b + 1] instanceof PieceLabel)) {
                     array.add((a + 1) * 8 + (b + 1));
                 }
             }
         }
         //Return the array
         int[] list = new int[array.size()];
         for (int y = 0; y < array.size(); y++) {
             list[y] = array.get(y);
         }
         return list;
     }
 
     /**
      * Method for coloring the pawns legal moves
      *
      * @param i Starting position
      * @param p Piece to be moved
      * @return Table of indexes to be colored
      */
     public int[] colorPawn(int i, Piece p) {
         ArrayList<Integer> array = new ArrayList<Integer>();
         int a = 0;
         int b = 0;
         if (i > 7) {
             a = i / 8;
             b = i - (a * 8);
         } else {
             b = i;
             a = 0;
         }
         //Checks top right corner
         if (a - 1 < 8 && b + 1 < 8 && a + 1 >= 0 && b + 1 >= 0) {
             if (twoTable[a - 1][b + 1] instanceof PieceLabel) {
                 if (twoTable[a - 1][b + 1].getPiece().getTeam() != p.getTeam()) {
                     if (p.getTeam() == 1) {
                         array.add((a - 1) * 8 + (b + 1));
                     }
                 }
             }
         }
         //Checks top left
         if (a - 1 < 8 && b - 1 < 8 && a + 1 >= 0 && b - 1 >= 0) {
             if (twoTable[a - 1][b - 1] instanceof PieceLabel) {
                 if (twoTable[a - 1][b - 1].getPiece().getTeam() != p.getTeam()) {
                     if (p.getTeam() == 1) {
                         array.add((a - 1) * 8 + (b - 1));
                     }
                 }
             }
         }
         //Checks bottom right
         if (a + 1 < 8 && b + 1 < 8 && a + 1 >= 0 && b + 1 >= 0) {
             if (twoTable[a + 1][b + 1] instanceof PieceLabel) {
                 if (twoTable[a + 1][b + 1].getPiece().getTeam() != p.getTeam()) {
                     if (p.getTeam() == 2) {
                         array.add((a + 1) * 8 + (b + 1));
                     }
                 }
             }
         }
         //Checks bottom left
         if (a + 1 < 8 && b - 1 < 8 && a + 1 >= 0 && b - 1 >= 0) {
             if (twoTable[a + 1][b - 1] instanceof PieceLabel) {
                 if (twoTable[a + 1][b - 1].getPiece().getTeam() != p.getTeam()) {
                     if (p.getTeam() == 2) {
                         array.add((a + 1) * 8 + (b - 1));
                     }
                 }
             }
         }
         /*
          * Checks the team of the pawns
          */
         if (p.getTeam() == 1) {
             if (a - 1 < 8 && b < 8 && a + 1 >= 0 && b >= 0) {
                 if (!(twoTable[a - 1][b] instanceof PieceLabel)) {
                     array.add((a - 1) * 8 + (b));
                 }
             }
             if (a - 2 < 8 && b < 8 && a - 2 >= 0 && b >= 0) {
                 if (!(twoTable[a - 2][b] instanceof PieceLabel)) {
                     if (a == 6) {
                         if (!(twoTable[a - 1][b] instanceof PieceLabel) && !(twoTable[a - 2][b] instanceof PieceLabel)) {
                             array.add((a - 2) * 8 + (b));
                         }
                     }
                 }
             }
         }
         if (p.getTeam() == 2) {
             if (a + 1 < 8 && b < 8 && a + 1 >= 0 && b >= 0) {
                 if (!(twoTable[a + 1][b] instanceof PieceLabel)) {
                     array.add((a + 1) * 8 + (b));
                 }
             }
             if (a + 2 < 8 && b < 8 && a + 2 >= 0 && b >= 0) {
                 if (a == 1) {
                     if (!(twoTable[a + 1][b] instanceof PieceLabel) && !(twoTable[a + 2][b] instanceof PieceLabel)) {
                         array.add((a + 2) * 8 + (b));
                     }
                 }
             }
         }
         //Transforms the arraylist into a table of integers
         int[] list = new int[array.size()];
         for (int y = 0; y < array.size(); y++) {
             list[y] = array.get(y);
         }
         return list;
     }
 
     /**
      * Method for coloring the rooks legal moves
      *
      * @param i Starting position
      * @param p Piece to be moved
      * @return Table of indexes to be colored
      */
     public int[] colorRook(int i, Piece p) {
         ArrayList<Integer> array = new ArrayList<Integer>();
         int a = 0;
         int b = 0;
         if (i > 7) {
             a = i / 8;
             b = i - (a * 8);
         } else {
             b = i;
             a = 0;
         }
         int c = a;
         int d = b;
         //Checks bottom line for legal moves
         for (int j = 0; j < 8; j++) {
             if ((c <= 7 && c >= 0) && (d <= 7 && d >= 0)) {
                 if (twoTable[c][d] instanceof PieceLabel) {
                     if (twoTable[c][d].getPiece().getTeam() != p.getTeam()) {
                         array.add((c) * 8 + (d));
                     }
                 }
                 if (!(twoTable[c][d] instanceof PieceLabel)) {
                     array.add((c) * 8 + (d));
                 }
             }
             if (c < 7) {
                 c++;
             }
             if (twoTable[c][d] instanceof PieceLabel) {
                 if (twoTable[c][d].getPiece().getTeam() != p.getTeam()) {
                     array.add((c) * 8 + (d));
                 }
                 break;
             }
         }
         c = a;
         d = b;
         //Checks top line for legal moves
         for (int j = 0; j < 8; j++) {
             if ((c <= 7 && c >= 0) && (d <= 7 && d >= 0)) {
                 if (twoTable[c][d] instanceof PieceLabel) {
                     if (twoTable[c][d].getPiece().getTeam() != p.getTeam()) {
                         array.add((c) * 8 + (d));
                     }
                 }
                 if (!(twoTable[c][d] instanceof PieceLabel)) {
                     array.add((c) * 8 + (d));
                 }
             }
             if (c > 0) {
                 c--;
             }
             if (twoTable[c][d] instanceof PieceLabel) {
                 if (twoTable[c][d].getPiece().getTeam() != p.getTeam()) {
                     array.add((c) * 8 + (d));
                 }
                 break;
             }
         }
         c = a;
         d = b;
         //Checks right side line for legal moves
         for (int j = 0; j < 8; j++) {
             if ((c <= 7 && c >= 0) && (d <= 7 && d >= 0)) {
                 if (twoTable[c][d] instanceof PieceLabel) {
                     if (twoTable[c][d].getPiece().getTeam() != p.getTeam()) {
                         array.add((c) * 8 + (d));
                     }
                 }
                 if (!(twoTable[c][d] instanceof PieceLabel)) {
                     array.add((c) * 8 + (d));
                 }
             }
             if (d < 7) {
                 d++;
             }
             if (twoTable[c][d] instanceof PieceLabel) {
                 if (twoTable[c][d].getPiece().getTeam() != p.getTeam()) {
                     array.add((c) * 8 + (d));
                 }
                 break;
             }
         }
         c = a;
         d = b;
         //Checks left side for legal moves
         for (int j = 0; j < 8; j++) {
             if ((c <= 7 && c >= 0) && (d <= 7 && d >= 0)) {
                 if (twoTable[c][d] instanceof PieceLabel) {
                     if (twoTable[c][d].getPiece().getTeam() != p.getTeam()) {
                         array.add((c) * 8 + (d));
                     }
                 }
                 if (!(twoTable[c][d] instanceof PieceLabel)) {
                     array.add((c) * 8 + (d));
                 }
             }
             if (d > 0) {
                 d--;
             }
             if (twoTable[c][d] instanceof PieceLabel) {
                 if (twoTable[c][d].getPiece().getTeam() != p.getTeam()) {
                     array.add((c) * 8 + (d));
                 }
                 break;
             }
         }
         c = a;
         d = b;
         //Transforms the arraylist to table of integers and returning it
         int[] list = new int[array.size()];
         for (int y = 0; y < array.size(); y++) {
             list[y] = array.get(y);
         }
         return list;
     }
 
     //Coloring the queens legal moves
     /**
      * Method for coloring the queens legal moves
      *
      * @param i Starting position
      * @param p Piece to be moved
      * @return Table of indexes to be colored
      */
     public int[] colorQueen(int i, Piece p) {
         ArrayList<Integer> array = new ArrayList<Integer>();
         int a = 0;
         int b = 0;
         if (i > 7) {
             a = i / 8;
             b = i - (a * 8);
         } else {
             b = i;
             a = 0;
         }
         int c = a;
         int d = b;
         //Checks bottom line for legal moves
         for (int j = 0; j < 8; j++) {
             if ((c <= 7 && c >= 0) && (d <= 7 && d >= 0)) {
                 if (twoTable[c][d] instanceof PieceLabel) {
                     if (twoTable[c][d].getPiece().getTeam() != p.getTeam()) {
                         array.add((c) * 8 + (d));
                     }
                 }
                 if (!(twoTable[c][d] instanceof PieceLabel)) {
                     array.add((c) * 8 + (d));
                 }
             }
             if (c < 7) {
                 c++;
             }
             if (twoTable[c][d] instanceof PieceLabel) {
                 if (twoTable[c][d].getPiece().getTeam() != p.getTeam()) {
                     array.add((c) * 8 + (d));
                 }
                 break;
             }
         }
         c = a;
         d = b;
         //Checks top line for legal moves
         for (int j = 0; j < 8; j++) {
             if ((c <= 7 && c >= 0) && (d <= 7 && d >= 0)) {
                 if (twoTable[c][d] instanceof PieceLabel) {
                     if (twoTable[c][d].getPiece().getTeam() != p.getTeam()) {
                         array.add((c) * 8 + (d));
                     }
                 }
                 if (!(twoTable[c][d] instanceof PieceLabel)) {
                     array.add((c) * 8 + (d));
                 }
             }
             if (c > 0) {
                 c--;
             }
             if (twoTable[c][d] instanceof PieceLabel) {
                 if (twoTable[c][d].getPiece().getTeam() != p.getTeam()) {
                     array.add((c) * 8 + (d));
                 }
                 break;
             }
         }
         c = a;
         d = b;
         //Checks right side line for legal moves
         for (int j = 0; j < 8; j++) {
             if ((c <= 7 && c >= 0) && (d <= 7 && d >= 0)) {
                 if (twoTable[c][d] instanceof PieceLabel) {
                     if (twoTable[c][d].getPiece().getTeam() != p.getTeam()) {
                         array.add((c) * 8 + (d));
                     }
                 }
                 if (!(twoTable[c][d] instanceof PieceLabel)) {
                     array.add((c) * 8 + (d));
                 }
             }
             if (d < 7) {
                 d++;
             }
             if (twoTable[c][d] instanceof PieceLabel) {
                 if (twoTable[c][d].getPiece().getTeam() != p.getTeam()) {
                     array.add((c) * 8 + (d));
                 }
                 break;
             }
         }
         c = a;
         d = b;
         //Checks left side line for legal moves
         for (int j = 0; j < 8; j++) {
             if ((c <= 7 && c >= 0) && (d <= 7 && d >= 0)) {
                 if (twoTable[c][d] instanceof PieceLabel) {
                     if (twoTable[c][d].getPiece().getTeam() != p.getTeam()) {
                         array.add((c) * 8 + (d));
                     }
                 }
                 if (!(twoTable[c][d] instanceof PieceLabel)) {
                     array.add((c) * 8 + (d));
                 }
             }
             if (d > 0) {
                 d--;
             }
             if (twoTable[c][d] instanceof PieceLabel) {
                 if (twoTable[c][d].getPiece().getTeam() != p.getTeam()) {
                     array.add((c) * 8 + (d));
                 }
                 break;
             }
         }
         c = a;
         d = b;
         //Checks top left line for legal moves
         for (int j = 0; j < 8; j++) {
             if ((c <= 7 && c >= 0) && (d <= 7 && d >= 0)) {
                 if (twoTable[c][d] instanceof PieceLabel) {
                     if (twoTable[c][d].getPiece().getTeam() != p.getTeam()) {
                         array.add((c) * 8 + (d));
                     }
                 }
                 if (!(twoTable[c][d] instanceof PieceLabel)) {
                     array.add((c) * 8 + (d));
                 }
             }
             if (c > 0 && d > 0) {
                 c--;
                 d--;
             }
             if (twoTable[c][d] instanceof PieceLabel) {
                 if (twoTable[c][d].getPiece().getTeam() != p.getTeam()) {
                     array.add((c) * 8 + (d));
                 }
                 break;
             }
         }
         c = a;
         d = b;
         //Checks bottom right line for legal moves
         for (int j = 0; j < 8; j++) {
             if ((c <= 7 && c >= 0) && (d <= 7 && d >= 0)) {
                 if (twoTable[c][d] instanceof PieceLabel) {
                     if (twoTable[c][d].getPiece().getTeam() != p.getTeam()) {
                         array.add((c) * 8 + (d));
                     }
                 }
                 if (!(twoTable[c][d] instanceof PieceLabel)) {
                     array.add((c) * 8 + (d));
                 }
             }
             if (c < 7 && d < 7) {
                 c++;
                 d++;
             }
             if (twoTable[c][d] instanceof PieceLabel) {
                 if (twoTable[c][d].getPiece().getTeam() != p.getTeam()) {
                     array.add((c) * 8 + (d));
                 }
                 break;
             }
         }
         c = a;
         d = b;
         //Checks top right line for legal moves
         for (int j = 0; j < 8; j++) {
             if ((c <= 7 && c >= 0) && (d <= 7 && d >= 0)) {
                 if (twoTable[c][d] instanceof PieceLabel) {
                     if (twoTable[c][d].getPiece().getTeam() != p.getTeam()) {
                         array.add((c) * 8 + (d));
                     }
                 }
                 if (!(twoTable[c][d] instanceof PieceLabel)) {
                     array.add((c) * 8 + (d));
                 }
             }
             if (c > 0 && d < 7) {
                 c--;
                 d++;
             }
             if (twoTable[c][d] instanceof PieceLabel) {
                 if (twoTable[c][d].getPiece().getTeam() != p.getTeam()) {
                     array.add((c) * 8 + (d));
                 }
                 break;
             }
         }
         c = a;
         d = b;
         //Checks bottom left line for legal moves
         for (int j = 0; j < 8; j++) {
             if ((c <= 7 && c >= 0) && (d <= 7 && d >= 0)) {
                 if (twoTable[c][d] instanceof PieceLabel) {
                     if (twoTable[c][d].getPiece().getTeam() != p.getTeam()) {
                         array.add((c) * 8 + (d));
                     }
                 }
                 if (!(twoTable[c][d] instanceof PieceLabel)) {
                     array.add((c) * 8 + (d));
                 }
             }
             if (c < 7 && d > 0) {
                 c++;
                 d--;
             }
             if (twoTable[c][d] instanceof PieceLabel) {
                 if (twoTable[c][d].getPiece().getTeam() != p.getTeam()) {
                     array.add((c) * 8 + (d));
                 }
                 break;
             }
         }
         int[] list = new int[array.size()];
         for (int y = 0; y < array.size(); y++) {
             list[y] = array.get(y);
         }
         return list;
     }
 
     /**
      * Method for coloring the castling if its legal
      *
      * @param i index indicating the starting position
      * @param castlingL whether the left rook is allowed to use castling
      * @param castlingR whether the right rook is allowed to use castling
      * @param castlingK whether the king is allowed to use castling
      * @return returns a table of indexes indicating where the castling is
      * allowed
      */
     public int[] colorCastling(int i, boolean castlingL, boolean castlingR, boolean castlingK) {
         ArrayList<Integer> array = new ArrayList<Integer>();
         int a = 0;
         int b = 0;
         if (i > 7) {
             a = i / 8;
             b = i - (a * 8);
         } else {
             b = i;
             a = 0;
         }
         int c = a;
         int d = b;
         //Checks the left side of the king for piecelabels in the way
         if (!(twoTable[c][d - 2] instanceof PieceLabel) && !(twoTable[c][d - 1] instanceof PieceLabel)) {
             //Checks if the king or rook has moved or not
             if (!castlingL && !castlingK) {
                 array.add(c * 8 + (d - 2));
             }
         }
         //Checks the right side of the king for piecelabels in the way
         if (!(twoTable[c][d + 2] instanceof PieceLabel) && !(twoTable[c][d + 1] instanceof PieceLabel)) {
             if (!castlingR && !castlingK) {
                 array.add(c * 8 + (d + 2));
             }
         }
         int[] list = new int[array.size()];
         for (int y = 0; y < array.size(); y++) {
             list[y] = array.get(y);
         }
         return list;
     }
 
     /**
      * Method for coloring the en passant movement if legal
      *
      * @param i Index for the starting position
      * @param passant Whether the en-passant move is allowed
      * @param j The point where the en-passant move is allowed.
      * @param p the piece being moved.
      * @return Returns a table of indexes indicating where the en-passant move
      * is allowed
      */
     public int[] colorPassant(int i, boolean passant, int j, Piece p) {
         ArrayList<Integer> array = new ArrayList<Integer>();
         int a = 0;
         int b = 0;
         if (i > 7) {
             a = i / 8;
             b = i - (a * 8);
         } else {
             b = i;
             a = 0;
         }
         int c = 0;
         int d = 0;
         if (j > 7) {
             c = j / 8;
             d = j - (c * 8);
         } else {
             c = j;
             d = 0;
         }
         //Checks team of piece and if en passant is legal 
         if (p.getTeam() == 1 && passant) {
             //Checks the indexes
             if ((a == (c + 1)) && ((b == d - 1) || (b == d + 1))) {
                 array.add(j);
             }
         }
         //Checks team of piece and if en passant is legal 
         if (p.getTeam() == 2 && passant) {
             if ((a == c - 1) && (b == d - 1 || b == d + 1)) {
                 array.add(j);
             }
         }
         int[] list = new int[array.size()];
         for (int y = 0; y < array.size(); y++) {
             list[y] = array.get(y);
         }
         return list;
 
     }
 }
