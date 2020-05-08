 public class SimPlayer extends Player {
 
 	// public SimPlayer(String startColor, boolean startsOnZeroSide) {
 	// 	super(startColor, startsOnZeroSide);
 	// }
 
 	public SimPlayer (String startColor, boolean startsOnZeroSide, Robot startGameRobot) {
 		super(startColor, startsOnZeroSide, startGameRobot);
 	}
 
 	public SimPlayer (String startXO, boolean startOnZeroSide) {
 		super(startXO, startOnZeroSide);
 	}
 
 	public void takeTurn(Game g) {
		this.performMove(this.calculateBestMove(g, 4));
 	}
 		
 	public void performMove(Move myMove) {
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
 			for (Piece deadPiece : myMove.calculatePiecesToJump(this.getBoard())) {
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
