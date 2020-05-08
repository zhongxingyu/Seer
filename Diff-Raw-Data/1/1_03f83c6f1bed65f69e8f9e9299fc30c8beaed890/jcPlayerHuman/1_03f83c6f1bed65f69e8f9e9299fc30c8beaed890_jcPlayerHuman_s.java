 /**************************************************************************
  * jcPlayerHuman.java - Interface to a human player
  * by Fran�ois Dominic Laram�e
  *
  * Purpose: This object allows a human player to play JavaChess.  Its only
  * real job is to query the human player for his move.
  *
  * Note that this is not the cleanest, most user-friendly piece of code around;
  * it is only intended as a test harness for the AI player, not as a full-
  * fledged application (which would be graphical, for one thing!)
  *
  * History:
  * 11.06.00 Creation
  **************************************************************************/
 package javachess;
 import javachess.jcMove;
 import javachess.jcBoard;
 import javachess.jcMoveListGenerator;
 import controllers.MoveException;
 public class jcPlayerHuman extends jcPlayer
 {
   // The keyboard
  
 
   // Validation help
   jcMoveListGenerator Pseudos;
   jcBoard Successor;
 
   // Constructor
   public jcPlayerHuman( int which )
   {
     this.SetSide( which );
     
     Pseudos = new jcMoveListGenerator();
     Successor = new jcBoard();
   }
 
   // public jcMove GetMove( theBoard )
   // Getting a move from the human player.  Sorry, but this is very, very
   // primitive: you need to enter square numbers instead of piece ID's, and
   // both square numbers must be entered with two digits.  Ex.: 04 00
   public jcMove GetMove( jcBoard theBoard, jcMove Mov, int car ) throws MoveException
   {
     // Read the move from the command line
     boolean ok = false;
     
     do
     {
       System.out.println( "Your move, " + PlayerStrings[ this.GetSide() ] + "?" );
 
       // Get data from the command line
       
       if ( ( Mov.SourceSquare < 0 ) || ( Mov.SourceSquare > 63 ) )
       {
     	  throw new MoveException(MoveException.illegalStartSquare);
       }
       if ( ( Mov.DestinationSquare < 0 ) || ( Mov.DestinationSquare > 63 ) )
       {
     	  throw new MoveException(MoveException.illegalEndSquare);
       }
 
       // Time to try to figure out what the move means!
       if ( theBoard.GetCurrentPlayer() == jcPlayer.SIDE_WHITE )
       {
         // Is there a piece (of the moving player) on SourceSquare?
         // If not, abort
         Mov.MovingPiece = theBoard.FindWhitePiece( Mov.SourceSquare );
         if ( Mov.MovingPiece == jcBoard.EMPTY_SQUARE )
         {
         	throw new MoveException(MoveException.illegalStartSquare);
         }
 
         // Three cases: there is a piece on the destination square (a capture),
         // the destination square allows an en passant capture, or it is a
         // simple non-capture move.  If the destination contains a piece of the
         // moving side, abort
         if ( theBoard.FindWhitePiece( Mov.DestinationSquare ) != jcBoard.EMPTY_SQUARE )
         {
         	throw new MoveException(MoveException.selfCapture);
         }
         Mov.CapturedPiece = theBoard.FindBlackPiece( Mov.DestinationSquare );
         if ( Mov.CapturedPiece != jcBoard.EMPTY_SQUARE )
           Mov.MoveType = jcMove.MOVE_CAPTURE_ORDINARY;
         else if ( ( theBoard.GetEnPassantPawn() == ( 1 << Mov.DestinationSquare ) ) &&
                   ( Mov.MovingPiece == jcBoard.WHITE_PAWN ) )
         {
           Mov.CapturedPiece = jcBoard.BLACK_PAWN;
           Mov.MoveType = jcMove.MOVE_CAPTURE_EN_PASSANT;
         }
 
         // If the move isn't a capture, it may be a castling attempt
         else if ( ( Mov.MovingPiece == jcBoard.WHITE_KING ) &&
                   ( ( Mov.SourceSquare - Mov.DestinationSquare ) == 2 ) )
           Mov.MoveType = jcMove.MOVE_CASTLING_KINGSIDE;
         else if ( ( Mov.MovingPiece == jcBoard.WHITE_KING ) &&
                   ( ( Mov.SourceSquare - Mov.DestinationSquare ) == -2 ) )
           Mov.MoveType = jcMove.MOVE_CASTLING_QUEENSIDE;
         else
           Mov.MoveType = jcMove.MOVE_NORMAL;
       }
       else
       {
         Mov.MovingPiece = theBoard.FindBlackPiece( Mov.SourceSquare );
         if ( Mov.MovingPiece == jcBoard.EMPTY_SQUARE )
         {
           throw new MoveException(MoveException.illegalStartSquare);
         }
 
         if ( theBoard.FindBlackPiece( Mov.DestinationSquare ) != jcBoard.EMPTY_SQUARE )
         {
           throw new MoveException(MoveException.selfCapture);
         }
         Mov.CapturedPiece = theBoard.FindWhitePiece( Mov.DestinationSquare );
         if ( Mov.CapturedPiece != jcBoard.EMPTY_SQUARE )
           Mov.MoveType = jcMove.MOVE_CAPTURE_ORDINARY;
         else if ( ( theBoard.GetEnPassantPawn() == ( 1 << Mov.DestinationSquare ) ) &&
                   ( Mov.MovingPiece == jcBoard.BLACK_PAWN ) )
         {
           Mov.CapturedPiece = jcBoard.WHITE_PAWN;
           Mov.MoveType = jcMove.MOVE_CAPTURE_EN_PASSANT;
         }
         else if ( ( Mov.MovingPiece == jcBoard.BLACK_KING ) &&
                   ( ( Mov.SourceSquare - Mov.DestinationSquare ) == 2 ) )
           Mov.MoveType = jcMove.MOVE_CASTLING_KINGSIDE;
         else if ( ( Mov.MovingPiece == jcBoard.BLACK_KING ) &&
                   ( ( Mov.SourceSquare - Mov.DestinationSquare ) == -2 ) )
           Mov.MoveType = jcMove.MOVE_CASTLING_QUEENSIDE;
         else
           Mov.MoveType = jcMove.MOVE_NORMAL;
       }
 
       // Now, if the move results in a pawn promotion, we must ask the user
       // for the type of promotion!
       //EDITTED: promotion now calculated earlier, and is added here in case move type has changed
       
       Mov.MoveType += car;
 
       // OK, now let's see if the move is actually legal!  First step: a check
       // for pseudo-legality, i.e., is it a valid successor to the current
       // board?
       Pseudos.ComputeLegalMoves( theBoard );
       if ( !Pseudos.Find( Mov ) )
       {
        throw new MoveException(MoveException.illegalMoveType);
       }
 
       // If pseudo-legal, then verify whether it leaves the king in check
       Successor.Clone( theBoard );
       Successor.ApplyMove( Mov );
       if ( !Pseudos.ComputeLegalMoves( Successor ) )
       {
         throw new MoveException(MoveException.inCheck);
        
       }
 
       
       ok = true;
 
     } while ( !ok );
 
     return( Mov );
   }
 
 @Override
 public jcMove GetMove(jcBoard theBoard) {
 	// TODO Auto-generated method stub
 	return null;
 }
 }
