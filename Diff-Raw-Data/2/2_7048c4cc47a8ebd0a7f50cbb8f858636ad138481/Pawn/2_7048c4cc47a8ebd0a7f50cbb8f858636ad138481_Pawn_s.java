 package se.chalmers.chessfeud.model.pieces;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import se.chalmers.chessfeud.constants.C;
 import se.chalmers.chessfeud.model.utils.Position;
 
 
 /**
  * The piece Knight.
  * Reprecents the Pawns on the chessborad. Handles the logic for the
  * Pawns movement.
  * @author Arvid
  *
  */
 
 //ven kallad Bonde
 public class Pawn extends Piece{
 	
 	private boolean hasMoved = false;
 	private int team;
 	
 	
 	public Pawn(int team) {
 		super(team, C.PIECE_PAWN);
 		this.team = team;
 		// TODO Auto-generated constructor stub
 	}
 	/**
 	 * A method that finds out all moves the piece can do
 	 * from the current position if the board was empty.
 	 * @param p the Pawns current Position
 	 * @return posList A list that contains Lists with all moves in all direction
 	 */
 	@Override
 	public List<List<Position>> theoreticalMoves(Position p) {
 		List<List<Position>> posList = new ArrayList<List<Position>>();
 		List<Position> moveList = new ArrayList<Position>();
 		moveList = moveDirection(p.getX(), p.getY(), this.team);
 		posList.add(moveList);
 		return posList;
 	}
 
 	/*
 	 * Takes the current position, and depending on which team the pawn is
 	 * on returns all the possible moves the piece can do, including the diagonal moves.
 	 * If the pawn hasn't moved yet, the list will also contain the first double move which a pawn can make
 	 * on its first turn.
 	 * @param px the pawns x-Position value
 	 * @param py the pawns y-Position value
 	 * @param team an integer that shows which team the pawn is on, 0 = white, 1 = black
 	 * @return moveList the list of possible moves in all directions.
 	 */
 	private List<Position> moveDirection(int px, int py, int team) {
 		List<Position> moveList = new ArrayList<Position>();
 		int[] x = {-1, 0, 1, 0};
 		int[] whiteY = {-1, -1, -1, -2};
 		int[] blackY = {1, 1, 1, 2};
 		List<Integer> xIntList = new ArrayList<Integer>();
 		List<Integer> yIntList = new ArrayList<Integer>();
 		for(int i = 0; i < x.length; i++){
 			xIntList.add(x[i]);
 			if(team == 0){
 				yIntList.add(whiteY[i]);
 			} else {
 				yIntList.add(blackY[i]);
 			}
 		}
 		if(hasMoved == false){
 			hasMoved = true;
 		} else{
 			xIntList.remove(xIntList.size() - 1);
 			yIntList.remove(yIntList.size() - 1);
 		}
 		
		for(int i=0; i<x.length; i++){
 			moveList.add(new Position(px + xIntList.get(i), py + yIntList.get(i)));
 		}	
 		return moveList;
 		
 	}
 	
 	@Override
 	public String toString(){
 		return "Piece: Pawn " + "Team: " + getTeam();
 	}
 }
 
