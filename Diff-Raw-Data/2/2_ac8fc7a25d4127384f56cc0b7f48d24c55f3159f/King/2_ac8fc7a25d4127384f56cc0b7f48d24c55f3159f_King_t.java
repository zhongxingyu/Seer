 package se.chalmers.chessfeud.model.pieces;
 
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 
 import se.chalmers.chessfeud.constants.C;
 import se.chalmers.chessfeud.model.utils.Position;
 
 /**
  * The Piece King.
  * Reprecents the King on the chessborad. Handles the logic for the
  * Kings movement.
  * @author Arvid
  *
  */
 
 //ven kallad Kung
 public class King extends Piece{
 
 	public King(int team) {
 		super(team, C.PIECE_KING);
 		// TODO Auto-generated constructor stub
 	}
 	
 	/**
 	 * Returns a list of all the theoretical moves the King can do.
 	 * Even the moves that are out of bounds. That will be checked in
 	 * the rules class.
 	 * The list shall contain every position one square away from the king.
 	 *   @param p the piece current position.
 	 * @return posList A list that contains Lists of possible positions in the different directions.
 	 */
 	@Override
 	public List<List<Position>> theoreticalMoves(Position p) {
 		List<List<Position>> posList = new ArrayList<List<Position>>();
 		for(int x = -1; x < 3; x++){
 			for(int y = -1; y < 3; y++){
				if(!(x == 0 && y == 0) && (p.getX() + x <= 7 && p.getY() + y <= 7)){
 					List<Position> tmp = new LinkedList<Position>();
 					tmp.add(new Position(p.getX() + x, p.getY() + y));
 					posList.add(tmp);
 				}	
 			}	
 		}
 		return posList;
 	}
 	
 	@Override
 	public String toString(){
 		return "Piece: King " + "Team: " + getTeam();
 	}
 }
