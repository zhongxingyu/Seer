 package edu.selu.android.classygames.games.checkers;
 
 
 import edu.selu.android.classygames.games.Coordinate;
import edu.selu.android.classygames.games.GenericPiece;
 import edu.selu.android.classygames.games.GenericPosition;
 
 
 public class Position extends GenericPosition
 {
 
 
 	public Position()
 	{
 		super();
 	}
 
 
 	public Position(final Coordinate coordinate)
 	{
 		super(coordinate);
 	}
 
 
 	public Position(final Coordinate coordinate, final byte pieceTeam, final byte pieceType)
 	{
 		super(coordinate);
 
 		piece = new Piece(pieceTeam, pieceType);
 	}
 
 
 }
