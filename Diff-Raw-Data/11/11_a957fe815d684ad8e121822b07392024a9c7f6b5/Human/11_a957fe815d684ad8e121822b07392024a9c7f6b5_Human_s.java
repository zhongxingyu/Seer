 import java.util.ArrayList;
 import java.util.Scanner;
 
 public class Human extends Player {
 	//stores the Scanner object for taking input
 	private Scanner in;
 	
 	public Human (String startColor, boolean startsOnZeroSide, Robot startGameRobot, AIEngine startBrain) {
 		//calls the Player constructor with the same arguments
 		super(startColor, startsOnZeroSide, startGameRobot, startBrain);
 	}
 
 	public Human (String startXO, boolean startOnZeroSide, AIEngine startBrain) {
 		//calls the Player constructor with the same arguments
 		super(startXO, startOnZeroSide, startBrain);
 		//creates a Scanner object to take input from the command line
 		this.in = new Scanner(System.in);
 	}
 
 	public Move takeTurn(Game g) {
 		//gets the last move made by the human player
 		Move m = this.inputMove(g);
 		//performs the move on the game board
 		super.performMove(m, g.getGameBoard());
 		//returns the move that was made
 		return m;
 	}
 
 	public Move inputMove(Game g) {
 		if (this.getRobot()!=null) {
 			//defines class to concurrently wait for the touch sensor to be pressed
 			class ButtonThread extends Thread {
 				private Robot r;
 				public ButtonThread(Robot r) {
 					this.r = r;
 				}
 				public void run() {
 					r.waitForSensorPress();
 				}
 			}
 
 			//starts concurrent thread waiting for button press
 			Thread t = new ButtonThread(this.getRobot());
 			t.start();
 
 			//creates pair of lists to hold scanned values
 			ArrayList<int[]> scannedLocations = new ArrayList<int[]>();
 			ArrayList<String> locationValues = new ArrayList<String>();
 			//gets list of moves from best to worst
 			Move[] possibleMoves = this.getBrain().rankBestMove(this.getAllMoves(this.getBoard()), g, this, 1);
 			
 			//waits for button thread to finish
 			try {
 				t.join();
 			} catch (InterruptedException e) {
 				
 			}
 
 			//iterates over all possible moves
 			for (Move m : possibleMoves) {
 				//gets all waypoints of the move
 				int[][] waypoints = m.getWaypoints();
 				//declares pointColor variable
 				String pointColor;
 				
 				//declares variable to determine if the loop needs to continue
 				boolean shouldContinue = false;
 				//iterates over all waypoints which should be empty
 				for (int[] waypoint : ArraysHelper.copyOfRange(waypoints, 0, waypoints.length-1)) {
 					if (scannedLocations.contains(waypoint)) {
 						//sets pointColor to the previously stored value
 						pointColor = locationValues.get(scannedLocations.indexOf(waypoint));
 					} else {
 						//scans the board to get pointColor
 						pointColor = this.getRobot().examineLocation(waypoint);
 						//stores the color in the pair of arrays holding the scanned locations and values
 						scannedLocations.add(waypoint);
 						locationValues.add(pointColor);
 					}
 					//breaks and continues to the next move if the square is not empty
 					if (pointColor!=Board.color) {
 						shouldContinue = true;
 						break;
 					}
 				}
 				//continue to the next move if this move failed
 				if (shouldContinue) {
 					continue;
 				}
 				
 				//sets last waypoint
 				int[] waypoint = waypoints[waypoints.length-1];
 				//checks the color of the last waypoint
 				if (scannedLocations.contains(waypoint)) {
 					//sets pointColor to the previously stored value
 					pointColor = locationValues.get(scannedLocations.indexOf(waypoint));
 				} else {
 					//scans the board to get pointColor
 					pointColor = this.getRobot().examineLocation(waypoints[waypoints.length-1]);
 					//stores the color in the pair of arrays holding the scanned locations and values
 					scannedLocations.add(waypoint);
 					locationValues.add(pointColor);
 				}
 				//returns the move if the final square matches the color of this player's pieces
 				if (pointColor==this.getColor()) {
 					return m;
 				}
 			}
 			
 			//failed to find the right move
 			return null;
 		
 		} else {
			super.getBoard().printBoard();
 			
 			boolean moveEntered = false;
 			Move inputtedMove = null;
 			while (!moveEntered) {
 				System.out.println("Enter Move:");
 				String inputLine = this.in.nextLine();
 				if ((inputLine.length()+1)%3==0) {
 					inputLine = inputLine + " ";
 				}
 				if (inputLine.length()%3==0 && inputLine.length()>=5) {
 
 					int numberOfWaypoints = inputLine.length()/3;
 					String[] waypointStrings = new String[numberOfWaypoints];
 					int[][] allWaypoints = new int[numberOfWaypoints][];
 					for (int i=0; i<numberOfWaypoints; i++) {
 						waypointStrings[i] = inputLine.substring(3*i,3*(i+1));
 					}
 					boolean validMove = true;
 					//CORBIN IS WORKING HERE
 					//for (String waypointString : waypointStrings) {
 						//could also test if first two characters are numbers
 						//if (waypointString.substring(2) == " ") {
 							//System.out.println(waypointString);
 							//validMove = false; 
 						//}
 					//}
 					if (validMove) {
 						for (int i=0; i<numberOfWaypoints; i++) {
 							allWaypoints[i] = new int[] {Integer.parseInt(inputLine.substring(3*i, 3*i+1)),Integer.parseInt(inputLine.substring(3*i+1, 3*i+2))};
 						}
 						inputtedMove = new Move(this.getBoard().getPieceAtLocation(allWaypoints[0]), allWaypoints);
 						if (inputtedMove.getMovePiece()!=null && inputtedMove.getMovePiece().getPlayer()==this && inputtedMove.calculateIsValid()) {
 							moveEntered = true;
 						}
 					}
 				}
 			}
 			return inputtedMove;
 		}
 	}
 }
