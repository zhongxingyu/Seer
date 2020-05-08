 import java.util.ArrayList;
 import java.util.Arrays;
 
 public class Move {
 
 	//waypoints are in order and include the starting point and the destination
 	private int[][] waypoints;
 	//number of jumps contained
 	private int jumpsContained;
 	//is the move valid
 
 	public Move (int[][] startWaypoints) {
 		//instantiate all the variables
 		this.waypoints=startWaypoints;
 		//calculate the number of pieces to be jumped
 		if (Math.abs(startWaypoints[1][0]-startWaypoints[0][0])==2 && Math.abs(startWaypoints[1][1]-startWaypoints[0][1])==2) {
 			this.jumpsContained = startWaypoints.length - 1;
 		} else {
 			this.jumpsContained = 0;
 		}
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
 	public Piece getMovePiece (Board b) {
 		return b.getPieceAtLocation(this.waypoints[0]);
 	}
 
 	//get the number of jumps that the piece will perform
 	public int getJumpsContained () {
 		return this.jumpsContained;
 	}
 	
 	//calculate an array of all the Piece objects that will be jumped during the move
 	public Piece[] calculatePiecesToJump (Board b) {
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
				result.add(this.getMovePiece(b).getPlayer().getBoard().getPieceAtLocation(midpoint));
 			}
 			return result.toArray(new Piece[result.size()]);
 		}
 	}
 
 	public boolean calculateIsValid (Board theBoard) {
 		Player thePlayer = this.getMovePiece(theBoard).getPlayer();
 		outerloop:
 		for (int i=0; i<this.waypoints.length-1; i++) {
 			int[] start = this.waypoints[i];
 			int[] end = this.waypoints[i+1];
 			int[] midpoint = new int[]{(end[0]+start[0])/2,(end[1]+start[1])/2};
 			int[] displacement = new int[] {end[0]-start[0],end[1]-start[1]};
 			if (!this.getMovePiece(theBoard).getIsKing()) {
 				if (thePlayer.getIsOnZeroSide()) {
 					if ((displacement[0]==1||displacement[0]==-1) && displacement[1]==1 && theBoard.getPieceAtLocation(end)==null && Board.locationIsInBounds(end)) {
 						//intentionally empty
 					}
 					else if ((displacement[0]==2||displacement[0]==-2)&&displacement[1]==2&&theBoard.getPieceAtLocation(end)==null&&theBoard.getPieceAtLocation(midpoint)!=null&&theBoard.getPieceAtLocation(midpoint).getPlayer()!=thePlayer&&Board.locationIsInBounds(end)) {
 						//intentionally empty
 					}
 					else {
 						return false;
 					}
 				}
 				else {
 					if ((displacement[0]==1||displacement[0]==-1)&&displacement[1]==-1&&theBoard.getPieceAtLocation(end)==null&&Board.locationIsInBounds(end)) {
 						//intentionally empty
 					}
 					else if ((displacement[0]==2||displacement[0]==-2)&&displacement[1]==-2&&theBoard.getPieceAtLocation(end)==null&&theBoard.getPieceAtLocation(midpoint)!=null&&theBoard.getPieceAtLocation(midpoint).getPlayer()!=thePlayer&&Board.locationIsInBounds(end)) {
 						//intentionally empty
 					}
 					else {
 						return false;
 					}
 				}
 			}
 			else {
 				if ((displacement[0]==1||displacement[0]==-1)&&(displacement[1]==1||displacement[1]==-1)&&theBoard.getPieceAtLocation(end)==null&&Board.locationIsInBounds(end)) {
 					//intentionally empty
 				}
 				else if ((displacement[0]==2||displacement[0]==-2)&&(displacement[1]==2||displacement[1]==-2)&&theBoard.getPieceAtLocation(end)==null&&theBoard.getPieceAtLocation(midpoint)!=null&&theBoard.getPieceAtLocation(midpoint).getPlayer()!=thePlayer&&Board.locationIsInBounds(end)) {
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
