 package chess.patterns;
 
 import java.util.Set;
 
 import chess.ChessPosition;
 import chess.Square;
 
 /**
  * Chess entity that is able to control some other (controllable) chess entity.
 * Here "controling" means attacking all the squares that are in some way
  * related to the controllable entity, and that are of importance in chess play. 
  * <br/> <br/>
  * 
  * For example, in the pattern "White King defends White Rook", the White King
  * is the controller - it controls the square occupied by the White Rook.
  * 
  * @author Kestutis
  * 
  */
 public interface ControllerEntity extends ChessEntity {
     
     /**
      * Gets the squares that are controlled (attacked) by this controller entity.
      *
      * @param chessPosition representation of the chess position: side to move
      * (White / Black), White and Black pieces, and their respective squares
      * @return the set of control squares
      */
     public abstract Set<Square> getControlSquares(ChessPosition chessPosition);
     
 }
