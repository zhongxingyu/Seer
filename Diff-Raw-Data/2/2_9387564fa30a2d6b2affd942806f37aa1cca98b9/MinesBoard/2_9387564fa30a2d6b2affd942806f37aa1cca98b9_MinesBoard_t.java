 package mines;
 
 import gridgame.GridBoard;
 import gridgame.HallOfFame;
 import gridgame.HallOfFameEntry;
 import gridgame.Renderable;
 
 import java.util.Collections;
 import java.util.LinkedList;
 import java.util.Set;
 import java.util.TreeSet;
 import javax.swing.JOptionPane;
 
 public class MinesBoard extends GridBoard<MinesTile>
 {
     // Override GridBoard's parent attribute so we don't have to do casts.
     private MinesGame parent;
 
     private static final int kBoardWidth = 10;
     private static final int kBoardHeight = 10;
     private static final int kMaxBombs = 9;
     protected int numBombs = 0;
     private boolean justCheated = false;
 
     public void setParent(MinesGame parent)
     {
         this.parent = parent;
     }
     
     protected void resetBoard()
     {
         if (this.grid == null)
         {
             this.grid = new MinesTile[this.kBoardHeight][this.kBoardWidth];
         }
         else
         {
             clearBoard();
         }
 
         // Fill the board with normal (non-bomb) pieces.
         for (int line = 0; line < this.kBoardHeight; line++)
         {
             for (int column = 0; column < this.kBoardWidth; column++)
             {
                 this.grid[line][column] = new MinesTile();
             }
         }
         
         // Figure out (deterministically) where we want the bombs.
         // Storing them in a Set allows us to avoid duplicates.  Unfortunately,
         // between Java's lack of type inference, the necessary type-specifying
         // required by a statically-typed language using a system like
         // generics, and the fact that you can't genericize primitives like
         // int, the logic here gets muddled a bit by syntax.
         Set<Pair<Integer, Integer>> bombSet = new TreeSet<Pair<Integer, Integer>>();
         java.util.Random generator = new java.util.Random(this.parent.getGame());
         for (int bombNumber = 0; bombNumber < this.kMaxBombs; bombNumber++)
         {
             int row = generator.nextInt(this.kBoardHeight);
             int column = generator.nextInt(this.kBoardWidth);
             bombSet.add(new Pair<Integer, Integer>(row, column));
         }
         this.numBombs = bombSet.size();
         for (Pair<Integer, Integer> bombCoordinates : bombSet)
         {
             MinesTile tile = (MinesTile)this.grid[bombCoordinates.first][bombCoordinates.second];
             tile.isBomb = true;
         }
     }
     
     protected void clearBoard()
     {
         for (int row = 0; row < this.kBoardHeight; row++)
         {
             for (int column = 0; column < this.kBoardWidth; column++)
             {
                 this.grid[row][column] = null;
             }
         }
     }
 
     protected void revealBoard()
     {
         for (int row = 0; row < this.kBoardHeight; row++)
         {
             for (int column = 0; column < this.kBoardWidth; column++)
             {
                 MinesTile tile = (MinesTile)this.grid[row][column];
                 if (tile.isBomb)
                 {
                     tile.status = Piece.bomb;
                 }
                 else
                 {
                     tile.status = Piece.empty;
                     tile.numSurroundingBombs = this.calculateSurroundingBombs(row, column);
                 }
             }
         }
     }
     
     protected void cheat()
     {
         this.revealBoard();
         this.justCheated = true;
     }
 
     protected void clickTile(final int row, final int column)
     {
         // Basic sanity check.
         if (row < 0 || row >= this.kBoardHeight || column < 0 || column >= this.kBoardWidth)
         {
             throw new IllegalArgumentException("Tile must be on the board.");
         }
         
         MinesTile tile = (MinesTile)this.grid[row][column];
         if (tile.status == Piece.hidden || tile.status == Piece.flagged || this.justCheated)
         {
             this.parent.moves++;
             this.justCheated = false;
             this.parent.updateStatusBar();
             if (tile.isBomb)
             {
                 this.revealBoard();
                 tile.status = Piece.exploded;
                 // TODO: This should be shown *after* the pieces are rendered revealed.
                 JOptionPane.showMessageDialog(null, "You lost.");
             }
             else
             {
                 this.revealEmptyCells(row, column);
                 if (this.isBoardWon())
                 {
                     String time = this.parent.secondsElapsed / 60 + ":" + String.format("%02d", this.parent.secondsElapsed % 60);
                     int choice = JOptionPane.showConfirmDialog(null, "Game "+this.parent.getGame()+" Cleared!\nSave your time of "+time+"?", "Win Dialog", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                     // Magic number for "Yes"
                     if (choice == 0)
                     {
                         String name = (String)JOptionPane.showInputDialog(null, "Your score of "+time+" will be entered into the Hall of Fame.  Enter your name:", "Hall of Fame Entry", JOptionPane.QUESTION_MESSAGE, null, null, "");
                         HallOfFame<HallOfFameEntry<String>> hallOfFame =
                         new HallOfFame<HallOfFameEntry<String>>("mines");
                         hallOfFame.add(new HallOfFameEntry<String>(time, name));
                     }
                 }
             }
         }
         // Already-clicked pieces are still supposed to increment the move
         // counter.  It's easier to do that here, rather than earlier, because
         // we'd have to make more logic exceptions.
         else if (!tile.isBomb)
         {
             this.parent.moves++;
             this.parent.updateStatusBar();
         }
     }
     
     public void handleRightClick(int row, int column)
     {
         // Basic sanity check.
         if (row < 0 || row >= this.kBoardHeight || column < 0 || column >= this.kBoardWidth)
         {
             throw new IllegalArgumentException("Tile must be on the board.");
         }
         
         MinesTile tile = (MinesTile)this.grid[row][column];
         if (tile.status == Piece.hidden)
         {
             tile.status = Piece.flagged;
             this.parent.flagsPlaced++;
         }
         else if (tile.status == Piece.flagged)
         {
             tile.status = Piece.hidden;
             this.parent.flagsPlaced--;
         }
     }
     
     /**
      * Calculate how many bombs are adjacent to a spot.
      *
      * Don't call this on a spot that has a bomb.
      */
     protected int calculateSurroundingBombs(int row, int column)
     {
         if (((MinesTile)this.grid[row][column]).isBomb)
         {
             throw new IllegalArgumentException("You should never be calculating nearby bombs for a bomb spot!");
         }
         
         int nearbyBombs = 0;
         // We don't care about including the actual tile in this list because
         // the above assertion guarantees it's not a bomb.
         int[] offsets = {-1, 0, 1};
         for (int offsetRow : offsets)
         {
             for (int offsetColumn : offsets)
             {
                 // Easier than doing bounds-checking.
                 try {
                     MinesTile adjacentTile = (MinesTile)this.grid[row+offsetRow][column+offsetColumn];
                     if (adjacentTile.isBomb)
                     {
                         nearbyBombs++;
                     }
                 }
                 catch (ArrayIndexOutOfBoundsException e)
                 {
                     // Do nothing, because we don't care about tiles that are
                     // off the board.
                 }
             }
         }
         
         return nearbyBombs;
     }
     
     protected void revealEmptyCells(int row, int column)
     {
         MinesTile tile = (MinesTile)this.grid[row][column];
         tile.numSurroundingBombs = this.calculateSurroundingBombs(row, column);
 
         // We don't want to auto-reveal bombs.
         if (tile.isBomb)
         {
             return;
         }
         // Tiles next to bombs should be counted as empty for the sake of
         // calculations, but we want to stop recursing at them; they form a
         // barrier to contiguous "truly empty" spaces.
         if (tile.numSurroundingBombs != 0)
         {
             tile.status = Piece.empty;
             return;
         }
 
         tile.status = Piece.empty; // [2]
         
         // We don't care about including the actual tile in this list because
         // the condition at [1] will always be false for it due to [2].
         int[] offsets = {-1, 0, 1};
         for (int offsetRow : offsets)
         {
             for (int offsetColumn : offsets)
             {
                 // Easier than doing bounds-checking.
                 try {
                     MinesTile adjacentTile = (MinesTile)this.grid[row+offsetRow][column+offsetColumn];
                     // Only recurse to tiles that haven't been revealed already.
                     // Otherwise, we'd get infinite recursion back and forth.
                     if (adjacentTile.status == Piece.hidden
                      || adjacentTile.status == Piece.flagged) // [1]
                     {
                         this.revealEmptyCells(row+offsetRow, column+offsetColumn);
                     }
                 }
                 catch (ArrayIndexOutOfBoundsException e)
                 {
                     // Do nothing, because we don't care about tiles that are
                     // off the board.
                 }
             }
         }
     }
     
     /**
      * The board is won if all non-bomb pieces have been revealed.
      */
     protected boolean isBoardWon()
     {
         for (int row = 0; row < this.kBoardHeight; row++)
         {
             for (int column = 0; column < this.kBoardWidth; column++)
             {
                 MinesTile tile = (MinesTile)this.grid[row][column];
                 if (!tile.isBomb && (tile.status == Piece.hidden || tile.status == Piece.flagged))
                 {
                     return false;
                 }
             }
         }
         
         return true;
     }
 }
 
 /** This is a silly little class, created because Java doesn't have 2-tuples
  * (or n-tuples of any sort, for that matter).
  */
 class Pair<E1 extends Comparable<?>, E2 extends Comparable<?>> implements Comparable<Pair<E1, E2>> {
     public E1 first;
     public E2 second;
     
     public Pair(E1 first, E2 second)
     {
         this.first = first;
         this.second = second;
     }
     
     public boolean equals(Object other)
     {
         if (other instanceof Pair)
         {
             Pair otherPair = (Pair)other;
             return this.first.equals(otherPair.first) && this.second.equals(otherPair.second);
         }
         
         return false;
     }
     
     public int compareTo(Pair<E1, E2> other)
     {
         if (this.equals(other))
         {
             return 0;
         }
         
         if (this.first.equals(other.first))
         {
             return ((Comparable)this.second).compareTo(other.second);
         }
         else
         {
             return ((Comparable)this.first).compareTo(other.first);
         }
     }
 }
 
