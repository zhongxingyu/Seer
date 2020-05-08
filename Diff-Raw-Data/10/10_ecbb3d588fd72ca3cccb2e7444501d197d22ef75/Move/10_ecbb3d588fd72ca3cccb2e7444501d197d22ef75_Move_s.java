 import java.util.ArrayList;
 import java.util.Arrays;
 
 public class Move {
 
 	private Piece movePiece;
 	//waypoints are in order and include the starting point and the destination
 	private int[][] waypoints;
 	//number of jumps contained
 	private int jumpsContained;
 	//is the move valid
	private boolean isValid;

 
 	public Move (Piece startPiece, int[][] startWaypoints) {
 		//instantiate all the variables
 		this.movePiece = startPiece;
 		this.waypoints=startWaypoints;
 		//calculate the number of pieces to be jumped
 		this.jumpsContained = startWaypoints.length - 2;
 		//implementation here to check if move is valid.
 	}
 	
 	//get the souce location: the position the moving Piece is originating from
 	public int[] getSource () {
 		return this.waypoints[0];
 	}
 
 	//get the final destination of the moving piece
 	public int[] getDestination () {
 		return this.waypoints[this.waypoints.length-1];
 	}
 
 	//get an array of every waypoint the moving piece will stop at
 	public int[][] getWaypoints () {
 		return this.waypoints;
 	}
 
 	//get the piece the that is moving
 	public Piece getMovePiece () {
 		return this.movePiece;
 	}
 
 	//get the number of jumps that the piece will perform
 	public int getJumpsContained () {
 		return this.jumpsContained;
 	}
 	
 	//calculate an array of all the Piece objects that will be jumped during the move
 	public Piece[] calculatePiecesToJump () {
 		//if no jumps, return empty list
 		if (this.jumpsContained == 0) {
 			return new Piece[0];
 		}
 		else {
 			ArrayList<Piece> result = new ArrayList<Piece>();
 			//cycle through all the movements the piece makes
 			for (int i=1; i<=this.waypoints.length-1;i++) {
 				//find the location that is being jumped over
 				int[] midpoint = {(this.waypoints[i][0]+this.waypoints[i-1][0])/2, (this.waypoints[i][1]+this.waypoints[i-1][1])/2};
 				result.add(this.movePiece.getPlayer().getBoard().getPieceAtLocation(midpoint));
 			}
 			return result.toArray(new Piece[result.size()]);
 		}
 	}
 
 	public boolean calculateIsValid () {
 		Player thePlayer = this.movePiece.getPlayer();
 		Board theBoard = thePlayer.getBoard();
 		outerloop:
 		for (int i=0; i<this.waypoints.length-1;i++) {
 			int[] start = this.waypoints[i];
 			int[] end = this.waypoints[i+1];
 			int[] midpoint = new int[]{(end[0]+start[0])/2,(end[1]+start[1])/2};
 			int[] displacement = new int[] {end[0]-start[0],end[1]-start[1]};
 			if (!this.movePiece.getIsKing()) {
 				if (this.movePiece.getPlayer().getIsOnZeroSide()) {
 					if (ArraysHelper.asArrayList(new int[][]{new int[]{1,1}, new int[]{-1,1}}).contains(displacement)&&theBoard.getPieceAtLocation(end)==null&&Board.locationIsInBounds(end)) {
 						//intentionally empty
 					}
 					else if (ArraysHelper.asArrayList(new int[][]{new int[]{2,2}, new int[]{-2,2}}).contains(displacement)&&theBoard.getPieceAtLocation(end)==null&&theBoard.getPieceAtLocation(midpoint)!=null&&theBoard.getPieceAtLocation(midpoint).getPlayer()!=thePlayer&&Board.locationIsInBounds(end)) {
 						//intentionally empty
 					}
 					else {
 						return false;
 					}
 				}
 				else {
 					if (ArraysHelper.asArrayList(new int[][]{new int[]{1,-1}, new int[]{-1,-1}}).contains(displacement)&&theBoard.getPieceAtLocation(end)==null&&Board.locationIsInBounds(end)) {
 						//intentionally empty
 					}
 					else if (ArraysHelper.asArrayList(new int[][]{new int[]{2,-2}, new int[]{-2,-2}}).contains(displacement)&&theBoard.getPieceAtLocation(end)==null&&theBoard.getPieceAtLocation(midpoint)!=null&&theBoard.getPieceAtLocation(midpoint).getPlayer()!=thePlayer&&Board.locationIsInBounds(end)) {
 						//intentionally empty
 					}
 					else {
 						return false;
 					}
 				}
 			}
 			else {
 					if (ArraysHelper.asArrayList(new int[][]{new int[]{1,1}, new int[]{-1,1}, new int[] {1,-1}, new int[] {-1,-1}}).contains(displacement)&&theBoard.getPieceAtLocation(end)==null&&Board.locationIsInBounds(end)) {
 						//intentionally empty
 					}
 					else if (ArraysHelper.asArrayList(new int[][]{new int[]{2,2}, new int[]{-2,2},new int[] {2,-2}, new int[] {-2,-2}}).contains(displacement)&&theBoard.getPieceAtLocation(end)==null&&theBoard.getPieceAtLocation(midpoint)!=null&&theBoard.getPieceAtLocation(midpoint).getPlayer()!=thePlayer&&Board.locationIsInBounds(end)) {
 						//intentionally empty
 					}
 					else {
 						return false;
 				}
 			}
 		}
 		return true;
 	}
 }
