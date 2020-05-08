 import java.util.ArrayList;
 
 public class Piece {
 
 	private boolean isKing;
 	private int[] location;
 	private Player owningPlayer;
 	private Board parentBoard;
 
 	public Piece (int[] startLocation, Player startPlayer, Board startBoard) {
 		this.isKing = false;
 		this.location=startLocation;
 		this.owningPlayer=startPlayer;
 		this.parentBoard = startBoard;
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
 
 	public Board getParentBoard() {
 		return this.parentBoard;
 	}
 
 	public Move[] getMovesOfPiece () {
 		//creates return ArrayList
 		ArrayList<Move> result = new ArrayList<Move>();
 		//gets array of all sets of waypoints
 		int[][][] allWaypoints = this.getMovesFromLocation(this.location, this.parentBoard, false);
 		//iterates through each ser
 		for (int[][] theWaypoints : allWaypoints) {
 			//creates move and adds to return string
 			result.add(new Move(this, theWaypoints));
 		}
 		//returns final result
 		return result.toArray(new Move[result.size()]);
 	}
 
 	public int[][][] getMovesFromLocation (int[] pieceLocation, Board b, boolean mustBeJump) {
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
 					if (b.getPieceAtLocation(testDestination) == null && Board.locationIsInBounds(testDestination)) {
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
 				if (Board.locationIsInBounds(testDestination) && b.getPieceAtLocation(testDestination) == null && b.getPieceAtLocation(pieceLocation) != null && b.getPieceAtLocation(midpoint) != null && b.getPieceAtLocation(midpoint).owningPlayer != this.owningPlayer) {
 					//adds move to return array
 					result.add(new int[][] {pieceLocation,testDestination});
 					//cycles through possible multi-jump scenarios
 					for (int[][] potentialMove : this.getMovesFromLocation(testDestination, new Board(b, new Move(this, new int[][]{pieceLocation, testDestination})), true)) {
						result.add(ArraysHelper.addTwoArrays(new int[][] {pieceLocation, testDestination},potentialMove));
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
 					if (b.getPieceAtLocation(testDestination) == null && Board.locationIsInBounds(testDestination)) {
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
 				jumpDisplacements = new int[][] {new int[] {2,-2}, new int[] {-2,-2}};
 			}
 			//iterates over all displacements
 			for (int[] displacement : jumpDisplacements) {
 				//calculates potential endpoint
 				int[] testDestination = new int[] {pieceLocation[0]+displacement[0], pieceLocation[1]+displacement[1]};
 				//calculates location being jumped over
 				int[] midpoint = new int[] {pieceLocation[0]+displacement[0]/2, pieceLocation[1]+displacement[1]/2};
 				//tests if destination is in bounds, unoccupied, and that midpoint is occupied by opponent's piece
 				if (Board.locationIsInBounds(testDestination) && b.getPieceAtLocation(testDestination) == null && b.getPieceAtLocation(pieceLocation) != null && b.getPieceAtLocation(midpoint) != null && b.getPieceAtLocation(midpoint).owningPlayer != this.owningPlayer) {
 					//adds waypoint set
 					result.add(new int[][] {pieceLocation,testDestination});
 					//finds all potential multi-jumps
 					for (int[][] potentialMove : this.getMovesFromLocation(testDestination, new Board(b, new Move(this, new int[][]{pieceLocation, testDestination})), true)) {
 						//adds multi-jump scenarios
 						result.add(ArraysHelper.addTwoArrays(new int[][] {pieceLocation}, potentialMove));
 					}
 				}
 			}
 		}
 		//returns the result
 		return result.toArray(new int[result.size()][][]);
 	}
 
 	public Piece copyToBoard(Board b) {
 		Piece result = new Piece(new int[]{this.location[0], this.location[1]}, this.owningPlayer, b);
 		result.setIsKing(this.isKing);
 		return result;
 	}
 }
