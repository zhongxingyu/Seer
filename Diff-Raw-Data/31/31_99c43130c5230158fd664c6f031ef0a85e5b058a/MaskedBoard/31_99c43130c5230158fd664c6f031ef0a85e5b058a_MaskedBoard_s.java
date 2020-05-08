 package hexadoku;
 
 import java.util.Arrays;
 import java.util.Random;
 
 /**
  * Represents a partially-filled board.
  *
  * @author Sam Fredrickson <kinghajj@gmail.com>
  */
 public class MaskedBoard extends Board
 {
     private RandomBoard board;
     private Random rand;
     private boolean[] cellVisible;
     private int numVisible;
 
     private int[] maskSizes = {64, 127, 128, 128};
     private int[] maskIncs  = {4, 2, 2, 2};
     private int[][] maskIndexes =
     {
         {
             0, 1, 2, 3, 4, 5, 6, 7, 7, 6, 5, 4, 3, 2, 1, 0,
             8, 9,10,11,12,13,14,15,15,14,13,12,11,10, 9, 8,
             16,17,18,19,20,21,22,23,23,22,21,20,19,18,17,16,
             24,25,26,27,28,29,30,31,31,30,29,28,27,26,25,24,
             32,33,34,35,36,37,38,39,39,38,37,36,35,34,33,32,
             40,41,42,43,44,45,46,47,47,46,45,44,43,42,41,40,
             48,49,50,51,52,53,54,55,55,54,53,52,51,50,49,48,
             56,57,58,59,60,61,62,63,63,62,61,60,59,58,57,56,
             56,57,58,59,60,61,62,63,63,62,61,60,59,58,57,56,
             48,49,50,51,52,53,54,55,55,54,53,52,51,50,49,48,
             40,41,42,43,44,45,46,47,47,46,45,44,43,42,41,40,
             32,33,34,35,36,37,38,39,39,38,37,36,35,34,33,32,
             24,25,26,27,28,29,30,31,31,30,29,28,27,26,25,24,
             16,17,18,19,20,21,22,23,23,22,21,20,19,18,17,16,
             8, 9,10,11,12,13,14,15,15,14,13,12,11,10, 9, 8,
             0, 1, 2, 3, 4, 5, 6, 7, 7, 6, 5, 4, 3, 2, 1, 0,
         },
         {
             0,  1,  2,  3,  4,  5,  6,  7,  8,  9, 10, 11, 12, 13, 14, 15,
             16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31,
             32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47,
             48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63,
             64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79,
             80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 91, 92, 93, 94, 95,
             96, 97, 98, 99,100,101,102,103,104,105,106,107,108,109,110,111,
             112,113,114,115,116,117,118,119,120,121,122,123,124,125,126,127,
             127,126,125,124,123,122,121,120,119,118,117,116,115,114,113,112,
             111,110,109,108,107,106,105,104,103,102,101,100, 99, 98, 97, 96,
             95, 94, 93, 92, 91, 90, 89, 88, 87, 86, 85, 84, 83, 82, 81, 80,
             79, 78, 77, 76, 75, 74, 73, 72, 71, 70, 69, 68, 67, 66, 65, 64,
             63, 62, 61, 60, 59, 58, 57, 56, 55, 54, 53, 52, 51, 50, 49, 48,
             47, 46, 45, 44, 43, 42, 41, 40, 39, 38, 37, 36, 35, 34, 33, 32,
             31, 30, 29, 28, 27, 26, 25, 24, 23, 22, 21, 20, 19, 18, 17, 16,
             15, 14, 13, 12, 11, 10,  9,  8,  7,  6,  5,  4,  3,  2,  1,  0
         },
         {
             0,  1,  2,  3,  4,  5,  6,  7,  7,  6,  5,  4,  3,  2,  1,  0,
             8,  9, 10, 11, 12, 13, 14, 15, 15, 14, 13, 12, 11, 10,  9,  8,
             16, 17, 18, 19, 20, 21, 22, 23, 23, 22, 21, 20, 19, 18, 17, 16,
             24, 25, 26, 27, 28, 29, 30, 31, 31, 30, 29, 28, 27, 26, 25, 24,
             32, 33, 34, 35, 36, 37, 38, 39, 39, 38, 37, 36, 35, 34, 33, 32,
             40, 41, 42, 43, 44, 45, 46, 47, 47, 46, 45, 44, 43, 42, 41, 40,
             48, 49, 50, 51, 52, 53, 54, 55, 55, 54, 53, 52, 51, 50, 49, 48,
             56, 57, 58, 59, 60, 61, 62, 63, 63, 62, 61, 60, 59, 58, 57, 56,
             64, 65, 66, 67, 68, 69, 70, 71, 71, 70, 69, 68, 67, 66, 65, 64,
             72, 73, 74, 75, 76, 77, 78, 79, 79, 78, 77, 76, 75, 74, 73, 72,
             80, 81, 82, 83, 84, 85, 86, 87, 87, 86, 85, 84, 83, 82, 81, 80,
             88, 89, 90, 91, 92, 93, 94, 95, 95, 94, 93, 92, 91, 90, 89, 88,
             96, 97, 98, 99,100,101,102,103,103,102,101,100, 99, 98, 97, 96,
             104,105,106,107,108,109,110,111,111,110,109,108,107,106,105,104,
             112,113,114,115,116,117,118,119,119,118,117,116,115,114,113,112,
             120,121,122,123,124,125,126,127,127,126,125,124,123,122,121,120
         },
         {
             0,  1,  2,  3,  4,  5,  6,  7,  8,  9, 10, 11, 12, 13, 14, 15,
             16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31,
             32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47,
             48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63,
             64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79,
             80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 91, 92, 93, 94, 95,
             96, 97, 98, 99,100,101,102,103,104,105,106,107,108,109,110,111,
             112,113,114,115,116,117,118,119,120,121,122,123,124,125,126,127,
             112,113,114,115,116,117,118,119,120,121,122,123,124,125,126,127,
             96, 97, 98, 99,100,101,102,103,104,105,106,107,108,109,110,111,
             80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 91, 92, 93, 94, 95,
             64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79,
             48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63,
             32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47,
             16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31,
             0,  1,  2,  3,  4,  5,  6,  7,  8,  9, 10, 11, 12, 13, 14, 15
         },
     };
 
     /**
      * Masks the board with the mask specified by the mask index.
      *
      * @param maskIndex
      */
     private void masker(int maskIndex, int desiredNumVisible)
     {
         final int[] ind = maskIndexes[maskIndex];
         final int size = maskSizes[maskIndex];
         final int inc = maskIncs[maskIndex];
         boolean[] M = new boolean[size];
        int i = rand.nextInt(size);
         numVisible = 0;
         Arrays.fill(M, false);
 
         while(numVisible < desiredNumVisible)
         {
            if(rand.nextInt() % 7 != 0 && numVisible < desiredNumVisible && !M[i % size])
             {
                M[i % size] = true;
                 numVisible += inc;
             }
             i++;
         }
 
         numVisible = 0;
 
         for(i = 0; i < Board.NUM_CELLS; ++i)
            if (cellVisible[i] = M[ind[i] % size])
                 numVisible++;
     }
 
     /**
      * This class is thrown if you attempt to create a MaskedBoard with an invalid
      * board.
      */
     private class InvalidBoardException extends Exception
     {
     }
 
     /**
      * Creates a new mask with an arrangement specified by 'type' with the given
      * board.
      *
      * @param board the board to put the mask over.
      * @throws hexadoku.MaskedBoard.InvalidBoardException if the board is invalid.
      */
     public MaskedBoard(RandomBoard board) throws InvalidBoardException
     {
         if(!board.isValid())
             throw new InvalidBoardException();
 
         this.board = board;
         this.rand = new Random();
         this.cellVisible = new boolean[Board.NUM_CELLS];
         System.out.println("Masking board.");
         masker(rand.nextInt(maskIndexes.length), 116);
         System.out.println("There are " + numVisible + " cells showing.");
     }
 
     /**
      * Gets a cell's value by index only if it is visible.
      *
      * @param index the index of the cell to use.
      * @return either the cell's value, or '\0' if the index is invalid or if
      * the cell is masked.
      */
     public char getCellValue(int index)
     {
         return cellVisible[index] ? board.getCellValue(index) : '\0';
     }
 
     /**
      * Tests whether the board is valid. A MaskedBoard is always valid.
      *
      * @return true.
      */
     public boolean isValid()
     {
         return true;
     }
 
     /**
      * Unmasks the cell at the index if the guess is correct. If the guess is
      * incorrect and the cell is masked, then the mask is kept and the function
      * returns false; otherwise it returns true.
      *
      * @param index
      * @param guess
      * @return false if the guess is incorrect and the cell is masked, otherwise
      * true.
      */
     public boolean reveal(int index, char guess)
     {
         // If the cell is already visible, then we're done.
         if(cellVisible[index])
             return true;
 
         // If the guess isn't correct, don't change the mask.
         if(board.getCellValue(index) != guess)
             return false;
 
         // Otherwise, unmask it.
         cellVisible[index] = true;
         return true;
     }
 
     @Override
     /**
      * @returns a string representation of the board.
      */
     public String toString()
     {
         StringBuilder sb = new StringBuilder(Board.NUM_CELLS * 2);
 
         sb.append("---------------------\n");
 
         for(int i = 0; i < Board.NUM_CELLS; ++i)
         {
             if(i % 64 == 0 && i != 0)
                 sb.append("|\n---------------------\n");
             else if(i % 16 == 0 && i != 0)
                 sb.append("|\n");
             if(i % 4 == 0)
                 sb.append('|');
             if(cellVisible[i])
                 sb.append(getCellValue(i));
             else
                 sb.append(' ');
         }
 
         sb.append("|\n---------------------");
 
         return sb.toString();
     }
 }
