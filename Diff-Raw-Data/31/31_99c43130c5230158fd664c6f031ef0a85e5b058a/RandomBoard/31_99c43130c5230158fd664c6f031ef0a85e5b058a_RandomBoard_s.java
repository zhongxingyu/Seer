 package hexadoku;
 
 import java.util.*;
 import java.util.concurrent.*;
 
 /**
  * Represents a Hexadoku board.
  *
  * @author Sam Fredrickson <kinghajj@gmail.com>
  */
 public class RandomBoard extends Board
 {
     // The possible digihts.
     private static char[] digits =
             new char[]
     {
         '0', '1', '2', '3', '4', '5', '6', '7',
         '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
     };
     // The board's cells.
     private char[] cells;
     // The board's random number generator, used to generate random boards.
     private Random rand;
 
     /**
      * Converts a digit from a character to an integer.
      *
      * @param c a character that represents a valid digit.
      * @return the integer form of the digit, or -1 if invalid.
      */
     private static int charToInt(char c)
     {
         if(c == '0')
             return 0;
         if(c == '1')
             return 1;
         if(c == '2')
             return 2;
         if(c == '3')
             return 3;
         if(c == '4')
             return 4;
         if(c == '5')
             return 5;
         if(c == '6')
             return 6;
         if(c == '7')
             return 7;
         if(c == '8')
             return 8;
         if(c == '9')
             return 9;
         if(c == 'A')
             return 10;
         if(c == 'B')
             return 11;
         if(c == 'C')
             return 12;
         if(c == 'D')
             return 13;
         if(c == 'E')
             return 14;
         if(c == 'F')
             return 15;
 
         return -1;
     }
 
     /**
      * Populates a cell, and all the following cells, using a backtracking
      * algorithm. Calling with the index 0 causes the entire board to be
      * filled with valid, random values.
      *
      * @param index the index to begin populating the board at.
      * @return true if populating succeeded.
      */
     private boolean populateCell(int index) throws InterruptedException
     {
         if(Thread.interrupted())
             throw new InterruptedException();
 
         // We're done.
         if(index == NUM_CELLS)
             return true;
 
         // Get a copy of the digits.
         char[] trydigits = (char[])digits.clone();
 
         // Shuffle the digits.
         for(int i = 0; i < NUM_DIGITS; ++i)
         {
             int ri = rand.nextInt(NUM_DIGITS);
             char tmp = trydigits[i];
             trydigits[i] = trydigits[ri];
             trydigits[ri] = tmp;
         }
 
         // Try each of the digits and backtrack.
         for(int i = 0; i < NUM_DIGITS; ++i)
         {
             // Set the cell to the current digit.
             cells[index] = trydigits[i];
 
             // Check if valid; if so, then move onto the next cell. If it
             // succeeds, then we succeed!
             if(isValid())
                 if(populateCell(index + 1))
                     return true;
         }
 
         // Nothing in this path worked, so rollback the cell and try the next.
         cells[index] = '\0';
         return false;
     }
 
     /**
      * An interface for an object than finds the cell index of the given zone
      * and sub-cell indexes.
      */
     private static interface CellFinder
     {
         int findCell(int i, int rcs);
     }
     // A cell-finder for rows.
     private static CellFinder rowCellFinder = new CellFinder()
     {
         public int findCell(int i, int row)
         {
             return row * NUM_DIGITS + i;
         }
     };
     // A cell-finder for columns.
     private static CellFinder colCellFinder = new CellFinder()
     {
         public int findCell(int i, int col)
         {
             return NUM_DIGITS * i + col;
         }
     };
     // A cell-finder for squares.
     private static CellFinder sqrCellFinder = new CellFinder()
     {
         public int findCell(int i, int sqr)
         {
             /*
             final int sqr_x = sqr % NUM_SQRS;
             final int sqr_y = sqr / NUM_SQRS;
             final int dig_x = i % NUM_SQRS;
             final int dig_y = i / NUM_SQRS;
             final int x = sqr_x * NUM_SQRS + dig_x;
             final int y = sqr_y * NUM_SQRS + dig_y;
             final int index = y * NUM_DIGITS + x;
             return index;
              */
 
             return ((sqr / NUM_SQRS) * NUM_SQRS + (i / NUM_SQRS)) * NUM_DIGITS +
                     ((sqr % NUM_SQRS) * NUM_SQRS + (i % NUM_SQRS));
         }
     };
 
     /**
      * Verifies the board with the given cell-finder.
      *
      * @param count an array to store digit counts.
      * @param finder the cell-finder to use to find cells to check.
      * @return true if verified, else false.
      */
     private boolean verifyByFinder(int[] count, CellFinder finder)
     {
         int digit, i;
 
         // Examine each zone of a kind determined by the CellFinder.
         for(int rcs = 0; rcs < NUM_DIGITS; ++rcs)
         {
             // Examine each cell in the zone.
             for(i = 0; i < NUM_DIGITS; ++i)
                 // Count the occurance of this digit.
                 if((digit = charToInt(cells[finder.findCell(i, rcs)])) != -1)
                     // If it's occured more than once, the board is invalid.
                     if(++count[digit] > 1)
                         return false;
 
             // Clear the count array.
             for(i = 0; i < NUM_DIGITS; ++i)
                 count[i] = 0;
         }
 
         return true;
     }
 
     /**
      * Creates a new, random board.
      */
     public RandomBoard()
     {
         Runnable populateCells;
         Future<?> populate;
         boolean finished = false;
         ExecutorService executor;
 
         cells = new char[NUM_CELLS];
         rand = new Random();
 
         populateCells = new Runnable()
         {
             public void run()
             {
                 try
                 {
                     populateCell(0);
                 }
                 catch(InterruptedException e)
                 {
                    // Clear board when interrupted.
                    for(int i = 0; i < cells.length; ++i)
                        cells[i] = '\0';
                 }
             }
         };
 
         // Continue trying to populate the board until it's been done.
         while(!finished)
         {
             // Start the thread population thread.
             System.out.println("Begining populating cells.");
             executor = Executors.newCachedThreadPool();
             populate = executor.submit(populateCells);
 
             // Wait five seconds for the cells to populate.
             try
             {
                 populate.get(1, TimeUnit.SECONDS);
                 finished = true;
             }
             catch(Exception e)
             {
                 System.out.println("Timed out. Restarting...");
             }
 
             // Ensure the thread is shutdown.
             try
             {
                 populate.cancel(true);
                 while(!populate.isDone()) {}
                 executor.shutdown();
                 executor.awaitTermination(NUM_SQRS, TimeUnit.DAYS);
             }
             catch(InterruptedException e)
             {
             }
         }
     }
 
     /**
      * Gets a cell's value by index.
      *
      * @param index the index of the cell to use.
      * @return either the cell's value, or '\0' if the index is invalid.
      */
     public char getCellValue(int index)
     {
         return index >= 0 && index < cells.length ? cells[index] : '\0';
     }
 
     /**
      * Tests whether the board is valid.
      *
      * @return true if the board is valid, else false.
      */
     public boolean isValid()
     {
         int[] count = new int[digits.length];
 
         return verifyByFinder(count, rowCellFinder) &&
                verifyByFinder(count, colCellFinder) &&
                verifyByFinder(count, sqrCellFinder);
     }
 
     @Override
     /**
      * @returns a string representation of the board.
      */
     public String toString()
     {
         StringBuilder sb = new StringBuilder(NUM_CELLS * 2);
 
         sb.append("---------------------\n");
 
         for(int i = 0; i < NUM_CELLS; ++i)
         {
             if(i % 64 == 0 && i != 0)
                 sb.append("|\n---------------------\n");
             else if(i % 16 == 0 && i != 0)
                 sb.append("|\n");
             if(i % 4 == 0)
                 sb.append('|');
             sb.append(cells[i]);
         }
 
         sb.append("|\n---------------------");
 
         return sb.toString();
     }
 }
