 import java.util.ArrayList;
 
 public class Piece {
 
 	private boolean isKing;
 	private int[] location;
 	private Player owningPlayer; 
 
 	public Piece (int[] startLocation, Player startPlayer) {
 		this.isKing = false;
 		this.location=startLocation;
 		this.owningPlayer=startPlayer;
 	}
 
 	public boolean getIsKing () {
 		return this.isKing;	
 	}
 
 	public void setIsKing (boolean changeIsKing) {
 		this.isKing=changeIsKing;
 	}
 
 	public int[] getLocation () {
 		return this.location;
 	}
 
 	public void setLocation (int[] changeLocation) {
 		this.location = changeLocation;
 	}
 
 	public Player getPlayer () {
 		return this.owningPlayer;
 	}
 
 	public Move[] getMovesOfPiece () {
 		//creates return ArrayList
 		ArrayList<Move> result = new ArrayList<Move>();
 		//gets array of all sets of waypoints
 		int[][][] allWaypoints = this.getMovesFromLocation(this.location, false);
 		//iterates through each ser
 		for (int[][] theWaypoints : allWaypoints) {
 			//creates move and adds to return string
 			result.add(new Move(theWaypoints));
 		}
 		//returns final result
 		return result.toArray(new Move[result.size()]);
 	}
 
 	public int[][][] getMovesFromLocation (int[] pieceLocation, boolean mustBeJump) {
 		//creates return array
 		ArrayList<int[][]> result = new ArrayList<int[][]>();
 		//tests if this piece is king
 		if (this.isKing) {
 			//tests if this move does not need to be a jump
 			if (!mustBeJump) {
 				//loops through all displacements
 				for (int[] displacement : new int[][] {new int[] {1,1}, new int[] {1,-1}, new int[] {-1,1}, new int[] {-1,-1}}) {
 					//finds potential destination
 					int[] testDestination = new int[] {pieceLocation[0]+displacement[0], pieceLocation[1]+displacement[1]};
 					//if destination empty and inbounds
 					if (this.owningPlayer.getBoard().getPieceAtLocation(testDestination) == null && Board.locationIsInBounds(testDestination)) {
 						//add waypoint set to return array
 						result.add(new int[][] {pieceLocation,testDestination});
 					}
 				}
 			}
 			//loops through jump displacements
 			for (int[] displacement : new int[][] {new int[] {2,2}, new int[] {2,-2}, new int[] {-2,2}, new int[] {-2,-2}}) {
 				//finds potential destinations
 				int[] testDestination = new int[] {pieceLocation[0]+displacement[0], pieceLocation[1]+displacement[1]};
 				//finds location being jumped over
 				int[] midpoint = new int[] {pieceLocation[0]+displacement[0]/2, pieceLocation[1]+displacement[1]/2};
 				//tests that destination is in bounds, destination is unoccupied, and opponent piece is being jumped over
 				if (Board.locationIsInBounds(testDestination) && this.owningPlayer.getBoard().getPieceAtLocation(testDestination) == null && this.owningPlayer.getBoard().getPieceAtLocation(pieceLocation) != null && this.owningPlayer.getBoard().getPieceAtLocation(pieceLocation).owningPlayer != this.owningPlayer) {
 					//adds move to return array
 					result.add(new int[][] {pieceLocation,testDestination});
 					//cycles through possible multi-jump scenarios
 					for (int[][] potentialMove : this.getMovesFromLocation(testDestination,true)) {
 						result.add(this.addTwoArrays(new int[][] {pieceLocation, testDestination},potentialMove));
 					}
 				}
 			}
 		}
 		else {
 			//confirms that move doesnt need to be a jump
 			if (!mustBeJump) {
 				//declares regularDisplacements
 				int[][] regularDisplacements;
 				//is on zero side of the board (robot side)
 				if (this.owningPlayer.getIsOnZeroSide() == true) {
 					//sets displacement values
 					regularDisplacements = new int[][] {new int[] {1,1}, new int[] {-1,1}};
 				}
 				//is not on zero side (human side)
 				else {
 					//sets different displacement values
					regularDisplacements = new int[][] {new int[] {1,-1}, new int[] {-1,-1}};
 				}
 				//iterates through all displacements
 				for (int[] displacement : regularDisplacements) {
 					//calculates potential destination
 					int[] testDestination = new int[] {pieceLocation[0]+displacement[0], pieceLocation[1]+displacement[1]};
 					//tests that location is in bounds and unoccupied
 					if (this.owningPlayer.getBoard().getPieceAtLocation(testDestination) == null && Board.locationIsInBounds(testDestination)) {
 						//adds waypoint set to the return array
 						result.add(new int[][] {pieceLocation,testDestination});
 					}
 				}
 			}
 			
 			// declares jumpDisplacements
 			int[][] jumpDisplacements;
 			//tests if player is on the zero side
 			if (this.owningPlayer.getIsOnZeroSide() == true) {
 				//sets displacements for jumps
 				jumpDisplacements = new int[][] {new int[] {2,2}, new int[] {-2,2}};
 			}
 			//called if player is on the non-zero side
 			else {
 				//sets different displacement values for jumps
 				jumpDisplacements = new int[][] {new int[] {2,-2}, new int[] {2,-2}};
 			}
 			//iterates over all displacements
 			for (int[] displacement : jumpDisplacements) {
 				//calculates potential endpoint
 				int[] testDestination = new int[] {pieceLocation[0]+displacement[0], pieceLocation[1]+displacement[1]};
 				//calculates location being jumped over
 				int[] midpoint = new int[] {pieceLocation[0]+displacement[0]/2, pieceLocation[1]+displacement[1]/2};
 				//tests if destination is in bounds, unoccupied, and that midpoint is occupied by opponent's piece
 				if (Board.locationIsInBounds(testDestination) && this.owningPlayer.getBoard().getPieceAtLocation(testDestination) == null && this.owningPlayer.getBoard().getPieceAtLocation(pieceLocation) != null && this.owningPlayer.getBoard().getPieceAtLocation(pieceLocation).owningPlayer != this.owningPlayer) {
 					//adds waypoint set
 					result.add(new int[][] {pieceLocation,testDestination});
 					//finds all potential multi-jumps
 					for (int[][] potentialMove : this.getMovesFromLocation(testDestination,true)) {
 						//adds multi-jump scenarios
 						result.add(this.addTwoArrays(new int[][] {pieceLocation}, potentialMove));
 					}
 				}
 			}
 		}
 	//returns the result
 	return result.toArray(new int[result.size()][][]);
 	}
 
 	public static int[][] addTwoArrays(int[][] a1, int[][] a2) {
 		int[][] result = new int[a1.length+a2.length][];
 		for (int i =0; i<a1.length+a2.length; i++) {
 			if (i < a1.length) {
 				result[i] = a1[i];
 			}
 			else {
 				result[i] = a2[i-a1.length];
 			}
 		}
 		return result;
 	}
 
 	public Piece copy() {
 		Piece result = new Piece(new int[]{this.location[0], this.location[1]}, this.owningPlayer);
 		result.setIsKing(this.isKing);
 		return result;
 	}
 }
