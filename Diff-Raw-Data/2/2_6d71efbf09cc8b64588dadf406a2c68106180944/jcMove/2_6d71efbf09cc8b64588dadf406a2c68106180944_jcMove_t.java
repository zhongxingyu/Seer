 /****************************************************************************
  * jcMove.java - An encapsulation of a chess move and its consequences
  * by François Dominic Laramée
  *
  * Purpose: This class is used all over the place.  It contains a move's
  * source and target squares, a type identifier (i.e., a normal move, a pawn
  * promotion, etc.) and a score, whether an actual evaluation of the position
  * which would result from the move or a value taken from the history table.
  *
  * History
  * 11.06.00 Creation
  * 09.07.00 Added fields MovingPiece and CapturedPiece; while not absolutely
  *          needed, they do accelerate move processing and help to make code
  *          easier to understand, so I gladly keep them around as optimizations
  * 14.08.00 Added "search depth" field, so that we can determine whether a
  *          transposition table entry should be used or not.
  ***************************************************************************/
 package javachess;
 
 public class jcMove
 {
   /************************************************************************
    * CONSTANTS
    ***********************************************************************/
 
   // The different types of moves recognized by the game
   public static final int MOVE_NORMAL = 0;
   public static final int MOVE_CAPTURE_ORDINARY = 1;
   public static final int MOVE_CAPTURE_EN_PASSANT = 2;
   public static final int MOVE_CASTLING_KINGSIDE = 4;
   public static final int MOVE_CASTLING_QUEENSIDE = 8;
   public static final int MOVE_RESIGN = 16;
   public static final int MOVE_STALEMATE = 17;
   public static final int MOVE_PROMOTION_KNIGHT = 32;
   public static final int MOVE_PROMOTION_BISHOP = 64;
   public static final int MOVE_PROMOTION_ROOK = 128;
   public static final int MOVE_PROMOTION_QUEEN = 256;
 
   // A pair of masks used to split the promotion and the non-promotion part of
   // a move type ID
   public static final int PROMOTION_MASK = 480;
   public static final int NO_PROMOTION_MASK = 31;
   
   // Alphabeta may return an actual move potency evaluation, or an upper or
   // lower bound only (in case a cutoff happens).  We need to store this
   // information in the transposition table to make sure that a given
   // value is actually useful in given circumstances.
   public static final int EVALTYPE_ACCURATE = 0;
   public static final int EVALTYPE_UPPERBOUND = 1;
   public static final int EVALTYPE_LOWERBOUND = 2;
 
   // A sentinel value used to identify jcMove fields without valid data
  public static final int NULL_MOVE = 0;
 
   /************************************************************************
    * DATA MEMBERS
    * Note: this class is intended as a C++ structure, so all data members
    * have public access.
    ***********************************************************************/
 
   // The moving piece; one of the constants defined by jcBoard
   public int MovingPiece;
 
   // The piece being captured by this move, if any; another jcBoard constant
   public int CapturedPiece;
 
   // The squares involved in the move
   public int SourceSquare, DestinationSquare;
 
   // A type ID: is this a regular move, a capture, a capture AND promotion from
   // Pawn to Rook, etc.  Move generation determines this, by definition; storing
   // it here avoids having to "re-discover" the information in jcBoard.ApplyMove
   // at the cost of a few bytes
   public int MoveType;
 
   // An evaluation of the move's potency, either as a result of an alphabeta
   // search of some kind or of a retrieval in the transposition table
   public int MoveEvaluation;
   public int MoveEvaluationType;
   public int SearchDepth;
 
   /*************************************************************************
    * PUBLIC METHODS
    *************************************************************************/
 
   public jcMove()
   {
     this.Reset();
   }
 
   public void Copy( jcMove target )
   {
     MovingPiece = target.MovingPiece;
     CapturedPiece = target.CapturedPiece;
     SourceSquare = target.SourceSquare;
     DestinationSquare = target.DestinationSquare;
     MoveType = target.MoveType;
     MoveEvaluation = target.MoveEvaluation;
     MoveEvaluationType = target.MoveEvaluationType;
     SearchDepth = target.SearchDepth;
   }
 
   // public boolean Equals( jcMove target )
   // Check whether two jcMove objects contain the same data (not necessarily
   // whether they are the same object in memory)
   public boolean Equals( jcMove target )
   {
     if ( MovingPiece != target.MovingPiece )
       return false;
     if ( CapturedPiece != target.CapturedPiece )
       return false;
     if ( MoveType != target.MoveType )
       return false;
     if ( SourceSquare != target.SourceSquare )
       return false;
     if ( DestinationSquare != target.DestinationSquare )
       return false;
     return true;
   }
 
   public boolean Reset()
   {
     MovingPiece = jcBoard.EMPTY_SQUARE;
     CapturedPiece = jcBoard.EMPTY_SQUARE;
     SourceSquare = NULL_MOVE;
     DestinationSquare = NULL_MOVE;
     MoveType = NULL_MOVE;
     MoveEvaluation = NULL_MOVE;
     MoveEvaluationType = NULL_MOVE;
     SearchDepth = NULL_MOVE;
     return true;
   }
 
   public void Print()
   {
     System.out.print( "Move: " );
     if ( MoveType == MOVE_STALEMATE )
     {
       System.out.println( "STALEMATE!!!" );
     }
     if ( MoveType != MOVE_RESIGN )
     {
       System.out.print( jcBoard.PieceStrings[ MovingPiece ] );
       System.out.print( "  [ " );
       System.out.print( SourceSquare );
       System.out.print( ", " );
       System.out.print( DestinationSquare );
       System.out.print( " ] TYPE: " );
       System.out.println( MoveType );
     }
     else
     {
       System.out.println( "RESIGNATION!" );
     }
   }
 }
