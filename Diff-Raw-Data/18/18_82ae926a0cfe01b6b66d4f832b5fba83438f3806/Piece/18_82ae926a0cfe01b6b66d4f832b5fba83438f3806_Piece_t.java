 import java.util.ArrayList;
 
 public class Piece {
 
 	private boolean isKing;
 	private byte[] location;
 	private Player owningPlayer; 
 
 	public Piece (byte[] startLocation, Player startPlayer) {
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
 
 	public byte[] getLocation () {
 		return this.location;
 	}
 
 	public void setLocation (byte[] changeLocation) {
 		this.location = changeLocation;
 	}
 
 	public Player getPlayer () {
 		return this.owningPlayer;
 	}
 
 	public Move[] getMovesOfPiece () {
 		//creates return ArrayList
 		ArrayList<Move> result = new ArrayList<Move>();
 		//gets array of all sets of waypoints
 		byte[][][] allWaypoints = this.getMovesFromLocation(this.location, false);
 		//iterates through each ser
 		for (byte[][] theWaypoints : allWaypoints) {
 			//creates move and adds to return string
 			result.add(new Move(this, theWaypoints));
 		}
 		//returns final result
 		return result.toArray(new Move[result.size()]);
 	}
 
 	public byte[][][] getMovesFromLocation (byte[] pieceLocation, boolean mustBeJump) {
 		//creates return array
 		ArrayList<byte[][]> result = new ArrayList<byte[][]>();
 		//tests if this piece is king
 		if (this.isKing) {
 			//tests if this move does not need to be a jump
 			if (!mustBeJump) {
 				//loops through all displacements
 				for (byte[] displacement : new byte[][] {new byte[] {1,1}, new byte[] {1,-1}, new byte[] {-1,1}, new byte[] {-1,-1}}) {
 					//finds potential destination
 					byte[] testDestination = new byte[] {pieceLocation[0]+displacement[0], pieceLocation[1]+displacement[1]};
 					//if destination empty and inbounds
 					if (this.owningPlayer.getBoard().getPieceAtLocation(testDestination) == null && Board.locationIsInBounds(testDestination)) {
 						//add waypoint set to return array
 						result.add(new byte[][] {pieceLocation,testDestination});
 					}
 				}
 			}
 			//loops through jump displacements
 			for (byte[] displacement : new byte[][] {new byte[] {2,2}, new byte[] {2,-2}, new byte[] {-2,2}, new byte[] {-2,-2}}) {
 				//finds potential destinations
 				byte[] testDestination = new byte[] {pieceLocation[0]+displacement[0], pieceLocation[1]+displacement[1]};
 				//finds location being jumped over
 				byte[] midpoint = new byte[] {pieceLocation[0]+displacement[0]/2, pieceLocation[1]+displacement[1]/2};
 				//tests that destination is in bounds, destination is unoccupied, and opponent piece is being jumped over
 				if (Board.locationIsInBounds(testDestination) && this.owningPlayer.getBoard().getPieceAtLocation(testDestination) == null && this.owningPlayer.getBoard().getPieceAtLocation(pieceLocation) != null && this.owningPlayer.getBoard().getPieceAtLocation(pieceLocation).owningPlayer != this.owningPlayer) {
 					//adds move to return array
 					result.add(new byte[][] {pieceLocation,testDestination});
 					//cycles through possible multi-jump scenarios
 					for (byte[][] potentialMove : this.getMovesFromLocation(testDestination,true)) {
 						result.add(ArrayUtils.addAll(new byte[][] {pieceLocation, testDestination},potentialMove));
 					}
 				}
 			}
 		}
 		else {
 			//confirms that move doesnt need to be a jump
 			if (!mustBeJump) {
 				//declares regularDisplacements
 				byte[][] regularDisplacements;
 				//is on zero side of the board (robot side)
 				if (this.owningPlayer.getIsOnZeroSide() == true) {
 					//sets displacement values
 					regularDisplacements = new byte[][] {new byte[] {1,1}, new byte[] {-1,1}};
 				}
 				//is not on zero side (human side)
 				else {
 					//sets different displacement values
 					regularDisplacements = new byte[][] {new byte[] {1,-1}, new byte[] {1,-1}};
 				}
 				//iterates through all displacements
 				for (byte[] displacement : regularDisplacements) {
 					//calculates potential destination
 					byte[] testDestination = new byte[] {pieceLocation[0]+displacement[0], pieceLocation[1]+displacement[1]};
 					//tests that location is in bounds and unoccupied
 					if (this.owningPlayer.getBoard().getPieceAtLocation(testDestination) == null && Board.locationIsInBounds(testDestination)) {
 						//adds waypoint set to the return array
 						result.add(new byte[][] {pieceLocation,testDestination});
 					}
 				}
 			}
			
			// declares jumpDisplacements
			byte[][] jumpDisplacements;
 			//tests if player is on the zero side
 			if (this.owningPlayer.getIsOnZeroSide() == true) {
 				//sets displacements for jumps
				jumpDisplacements = new byte[][] {new byte[] {2,2}, new byte[] {-2,2}};
 			}
 			//called if player is on the non-zero side
 			else {
 				//sets different displacement values for jumps
				jumpDisplacements = new byte[][] {new byte[] {2,-2}, new byte[] {2,-2}};
 			}
 			//iterates over all displacements
 			for (byte[] displacement : jumpDisplacements) {
 				//calculates potential endpoint
 				byte[] testDestination = new byte[] {pieceLocation[0]+displacement[0], pieceLocation[1]+displacement[1]};
 				//calculates location being jumped over
 				byte[] midpoint = new byte[] {pieceLocation[0]+displacement[0]/2, pieceLocation[1]+displacement[1]/2};
 				//tests if destination is in bounds, unoccupied, and that midpoint is occupied by opponent's piece
				if (Board.locationIsInBounds(testDestination) && this.owningPlayer.getBoard().getPieceAtLocation(testDestination) == null && this.owningPlayer.getBoard().getPieceAtLocation(pieceLocation) != null && this.owningPlayer.getBoard().getPieceAtLocation(pieceLocation).owningPlayer != this.owningPlayer) {
 					//adds waypoint set
 					result.add(new byte[][] {pieceLocation,testDestination});
 					//finds all potential multi-jumps
 					for (byte[][] potentialMove : this.getMovesFromLocation(testDestination,true)) {
 						//adds multi-jump scenarios
						result.add(ArrayUtils.addAll(pieceLocation, potentialMove));
 					}
 				}
 			}
 		}
 	//returns the result
	return result.toArray(new byte[result.size()][][]);
 	}
 }
