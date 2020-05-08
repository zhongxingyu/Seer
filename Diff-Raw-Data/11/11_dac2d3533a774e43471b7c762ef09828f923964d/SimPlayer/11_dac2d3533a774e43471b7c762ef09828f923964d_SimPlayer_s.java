 import java.util.Arrays;
 
 public class SimPlayer extends Player {
 
 	public SimPlayer (String startColor, boolean startsOnZeroSide, Robot startGameRobot, AIEngine startBrain) {
 		//calls the Player constructor with the same arguments
 		super(startColor, startsOnZeroSide, startGameRobot, startBrain);
 	}
 
 	public SimPlayer (String startXO, boolean startOnZeroSide, AIEngine startBrain) {
 		//calls the Player constructor with the same arguments
 		super(startXO, startOnZeroSide, startBrain);
 	}
 
 	public Move takeTurn(Game g) {
 		//calculates the best move to make the specified number of plies ahead
 		Move m = this.calculateBestMove(g, 8);
 		//performs the move on the stored (and the physical) board
 		this.performMove(m);
 		// System.out.println(Arrays.deepToString(m.getWaypoints()));
 		return m;
 	}
 		
 	public void performMove(Move myMove) {
 		//performs the move on the stored board
 		Player.performMove(myMove,this.getBoard());
 		//tests if game is using the robot
 		if (this.getRobot()!=null) {
 			//move arm over moving piece
 			this.getRobot().moveToXY(myMove.getSource());
 			//pcks up the piece
 			this.getRobot().pickUpPiece();
 			//moves piece to the destination
 			this.getRobot().moveToXY(myMove.getDestination());
 			//drops the piece
 			this.getRobot().dropPiece();
 			//iterates over all jumped pieces
 			for (Piece deadPiece : myMove.calculatePiecesToJump()) {
 				//move arm over the piece that was jumped
 				this.getRobot().moveToXY(deadPiece.getLocation());
 				//picks up jumped piece
 				this.getRobot().pickUpPiece();
 				//moves arm over drop point for dead pieces
				this.getRobot().moveToXY(myMove.getDestination());
 				//drops the piece
 				this.getRobot().dropPiece();
 			}
 			//resets arm after all pieces have been moved.
 			this.getRobot().resetPosition();
 		}
 	}
 }
