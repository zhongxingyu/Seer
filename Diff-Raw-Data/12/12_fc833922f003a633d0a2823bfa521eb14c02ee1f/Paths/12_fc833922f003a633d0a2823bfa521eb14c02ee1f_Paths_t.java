 package com.williamdye.dyce.board;
 
 import com.williamdye.dyce.pieces.PieceType;
 import com.williamdye.dyce.util.MathUtils;
 
 import static com.williamdye.dyce.board.ChessboardImpl.NUM_FILES;
 
 /**
  * @author William Dye
  */
 public abstract class Paths
 {
 
     public static int getRankDistance(Square start, Square end)
     {
         if (isInvalidPair(start, end))
             return -1;
         return Math.abs(start.getRank().getNumber() - end.getRank().getNumber());
     }
 
     public static int getFileDistance(Square start, Square end)
     {
         if (isInvalidPair(start, end))
             return -1;
         return Math.abs(start.getFile().getNumber() - end.getFile().getNumber());
     }
 
     public static boolean isSameDiagonal(Square start, Square end)
     {
         return (!(isInvalidPair(start, end)) && (getFileDistance(start, end) == getRankDistance(start, end)));
     }
 
     public static boolean isPathClear(Square start, Square end)
     {
         if (isInvalidPair(start, end))
             return false;
         final Square[] squares = start.getBoard().getBoard();
         final int startIndex = (((start.getRank().getNumber() - 1) * NUM_FILES) + (start.getFile().getNumber() - 1));
         final int endIndex = (((end.getRank().getNumber() - 1) * NUM_FILES) + (end.getFile().getNumber() - 1));
         int i;
         boolean clear = false;
         if ((start == end) || ((start.getPiece() != null) && (PieceType.KNIGHT == start.getPiece().getPieceType())))
             clear = true;   /* path is always clear for knights since they can jump over any other piece */
         else if (start.getRank() == end.getRank()) {    /* strictly horizontal (e.g., a4 => d4) */
             clear = true;
             if (start.getFile().getNumber() < end.getFile().getNumber()) {
                 for (i = startIndex + 1; i < endIndex; i++)
                     clear &= squares[i].isEmpty();
             } else {    /* moving kingside => queenside */
                 for (i = endIndex - 1; i > startIndex; i--)
                     clear &= squares[i].isEmpty();
             }
         } else if (start.getFile() == end.getFile()) {  /* strictly vertical (e.g., c7 => c3) */
             clear = true;
             if (start.getRank().getNumber() < end.getRank().getNumber()) {
                 for (i = startIndex + NUM_FILES; i < endIndex; i += NUM_FILES)
                     clear &= squares[i].isEmpty();
             } else {    /* moving 8th rank => 1st rank */
                for (i = startIndex - NUM_FILES; i > endIndex; i -= NUM_FILES)
                     clear &= squares[i].isEmpty();
             }
         } else if (Paths.getRankDistance(start, end) == Paths.getFileDistance(start, end)) { /* diagonal */
             clear = true;
             int rankDistance = end.getRank().getNumber() - start.getRank().getNumber();
             int fileDistance = end.getFile().getNumber() - start.getFile().getNumber();
             int increment;
             if (MathUtils.signum(rankDistance) == MathUtils.signum(fileDistance)) {
                 increment = NUM_FILES + 1;
                 if (rankDistance > 0) {
                     for (i = startIndex + increment; i < endIndex; i += increment)
                         clear &= squares[i].isEmpty();
                 } else {
                     for (i = endIndex - increment; i > startIndex; i -= increment)
                         clear &= squares[i].isEmpty();
                 }
             } else {
                 increment = NUM_FILES - 1;
                 if (rankDistance > 0) {
                     for (i = startIndex + increment; i < endIndex; i += increment)
                         clear &= squares[i].isEmpty();
                 } else {
                     for (i = endIndex - increment; i > startIndex; i -= increment)
                         clear &= squares[i].isEmpty();
                 }
             }
             /*      0   1   2   3   4
              *    +---+---+---+---+---+  |-- distances --|
              * 20 | 8 |   |   |   | 12|  4 = (num_files) - 1
              *    +---+---+---+---+---+
              * 15 |   | 4 |   | 6 |   |  6 = (num_files) + 1
              *    +---+---+---+---+---+
              * 10 |   |   | X |   |   |  8 = (2 * num_files) - 2
              *    +---+---+---+---+---+
              *  5 |   | 6 |   | 4 |   |  12 = (2 * num_files) + 2
              *    +---+---+---+---+---+
              *  0 | 12|   |   |   | 8 |  ... etc. (n * num_files) +/- n
              *    +---+---+---+---+---+
              */
         }
         return clear;
     }
 
     private static boolean isInvalidPair(Square a, Square b)
     {
         return ((a == null) || (b == null) || (a.getBoard() == null) || !(a.getBoard().equals(b.getBoard())));
     }
 
 }
