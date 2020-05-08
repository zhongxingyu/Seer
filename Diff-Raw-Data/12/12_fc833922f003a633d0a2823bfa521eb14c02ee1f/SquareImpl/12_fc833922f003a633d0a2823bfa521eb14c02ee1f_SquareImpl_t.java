 package com.williamdye.dyce.board;
 
 import com.williamdye.dyce.pieces.Piece;
 
 /**
  * @author William Dye
  */
 public class SquareImpl implements Square
 {
     protected final Chessboard board;
     protected final Rank rank;
     protected final File file;
     protected Piece piece;
 
     public SquareImpl(Chessboard board, Rank rank, File file)
     {
         this(board, rank, file, null);
     }
 
     public SquareImpl(Chessboard chessboard, Rank r, File f, Piece p)
     {
         this.board = chessboard;
         this.rank = r;
         this.file = f;
         setPiece(p);
     }
 
     @Override
     public Chessboard getBoard()
     {
         return board;
     }
 
     @Override
     public Rank getRank()
     {
         return rank;
     }
 
     @Override
     public File getFile()
     {
         return file;
     }
 
     @Override
     public Piece getPiece()
     {
         return piece;
     }
 
     @Override
     public void setPiece(Piece target)
     {
        if ((target != null) && (target.getSquare() == null))
            target.move(this);
         piece = target;
     }
 
     @Override
     public String getName()
     {
         return file.toString() + rank.toString();
     }
 
     @Override
     public boolean isEmpty()
     {
         return (piece == null);
     }
 
 }
