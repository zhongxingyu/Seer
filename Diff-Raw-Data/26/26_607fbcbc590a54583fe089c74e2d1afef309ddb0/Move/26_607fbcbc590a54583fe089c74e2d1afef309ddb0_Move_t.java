 package j3chess;
 
 import j3chess.components.Movement;
 import j3chess.components.PieceContext;
 import j3chess.components.Position;
 import artemis.ComponentMapper;
 import artemis.ComponentType;
 import artemis.Entity;
 import artemis.annotations.Mapper;
 
 /**
  * a class to define a move.
  */
 public class Move {
 
     /** @brief the type of this move */
     private MoveType mMoveType;
     /** @brief the start position of this move */
     private Field mStartField;
     /** @brief the target position of this move */
     private Field mTargetField;
     /** @brief whether the moving piece crossed the center with this move */
     private boolean mIsCrossingCenter;
 
 /* ------------------------------------------------------------------------- */
 
     /** @brief keeps the handle of the moving piece */
     private Entity mMovingPiece;
     /** @brief keeps the handle of the other piece */
     private Entity mTargetPiece;
     /** @brief the description of this move in chess notation */
     private String mDescription;
 
 /* ------------------------------------------------------------------------- */
 
     /**
      * @brief complete constructor for a move
      *        crossedCenter defaults to false
      * @param type type of the move
      * @param start start of the move
      * @param target target of the move
      */
     public Move(
             final MoveType type,
             final Field start,
             final Field target) {
         this(type, start, target, false);
     }
 
     /**
      * @brief complete constructor for a move
      * @param type type of the move
      * @param start start of the move
      * @param target target of the move
      * @param crossedCenter whether the move crossed the center
      */
     public Move(
             final MoveType type,
             final Field start,
             final Field target,
             final boolean crossedCenter) {
         this.mMoveType = type;
         this.mStartField = start;
         this.mTargetField = target;
         this.mIsCrossingCenter = crossedCenter;
 
         mMovingPiece = mStartField.getPiece();
         if (mMovingPiece == null) {
             J3ChessApp.getLogger().error("Moving piece null");
         }
         mTargetPiece = mTargetField.getPiece();
 
         generateDescription();
     }
 
     /**
      * @brief generates a description of the move from context information
      */
     final void generateDescription() {
         // pieces
         ComponentType contextType = ComponentType.getTypeFor(PieceContext.class);
         String movingPiece = ((PieceContext) mMovingPiece.getComponent(contextType))
             .getPieceType()
             .toAlgebraic();
         String targetPiece = "";
         if (mTargetPiece != null) {
             targetPiece = ((PieceContext) mTargetPiece.getComponent(contextType))
                     .getPieceType()
                     .toAlgebraic();
         }
 
         // move type
         switch (mMoveType) {
         case Capture:
            this.mDescription = movingPiece + mStartField.toString() +"x" + mTargetField.toString();
             break;
         case CaptureEnPassant:
            this.mDescription = mStartField.toFile() + ":"
                               + mTargetField.toString() + "e.p.";
             break;
         case Common:
             this.mDescription = movingPiece + mTargetField.toString();
             break;
         case Disambiguating:
            this.mDescription = movingPiece + mStartField.toString() + "-"
                               + mTargetField.toString();
             break;
         case KingsideCastling:
             this.mDescription = "0-0";
             break;
         case Promotion:
             this.mDescription = mTargetField.toString() + targetPiece;
             break;
         case QueensideCastling:
             this.mDescription = "0-0-0";
             break;
         default:
             this.mDescription = "";
             break;
         }
 
     //case Check: // win conditions
     //    this.mDescription = this.mDescription + "+";
     //case Checkmate: // win conditions
     //    this.mDescription = this.mDescription + "#";
 
     }
 
     /**
      * @brief custom to string for easy translation into chess notation
      *
      * file - column
      * rank - row
      *
      * Common Move: <PieceType><mTarget>
      * mType.toAlgebraic() + mTarget.toString()
      * Disambiguating Move: <PieceType><mStart.File><mStart.Rank>...
      * mType.toAlgebraic() + mStart.toString() ...
      *
      * Capture: <PieceType>x<mTarget>
      * mType.toAlgebraic() + "x" + mTarget.toString()
      * Capture En Passant: <mStart.File>x<mTarget>e.p.
      * mStart.toFile() + "x" + mTarget.toString() + "e.p."
      *
      * Promotion: <mTarget><PieceType>
      * mTarget.toString() + mPromotionType.toAlgebraic()
      *
      * [TRIVIAL]
      * Kingside Castling: 0-0
      * Queenside Castling: 0-0-0
      *
      * [SUFFIX]
      * Check: <CompleteMove>+
      * Checkmate: <CompleteMove>#
      *
      * End Of Game: 0-1-0 (Player 1->Player 2->Player 3 [Player 2 Won])
      *
      * @return move in chess notation
      */
     @Override
     public final String toString() {
         return mDescription;
         //return mStartField.toString() + " " + mTargetField.toString();
     }
 
     /**
      * @brief move a piece from start to target, notifying both the pieces and
      * the fields
      */
     public final void execute() {
         ComponentType positionType = ComponentType.getTypeFor(Position.class);
         ComponentType movementType = ComponentType.getTypeFor(Movement.class);
         // notify moving piece
         ((Position) mMovingPiece.getComponent(positionType)).setField(mTargetField);
         if (this.mIsCrossingCenter) {
             ((Movement) mMovingPiece.getComponent(movementType)).toggleCrossedCenter();
         }
 
         // notify other piece
         if (mTargetPiece != null) {
             mTargetPiece.disable();
         }
 
         // notify fields
         mStartField.setPiece(null);
         mTargetField.setPiece(mMovingPiece);
     }
 
     /**
      * @brief getter for the mTargetField member
      * @return the target field of this move
      */
     public final Field getTargetField() {
         return mTargetField;
     }
 
     /**
      * @brief getter for the mIsCrossingCenter member
      * @return whether the move is across the center
      */
     public final boolean isCrossingCenter() {
         return mIsCrossingCenter;
     }
 
 }
