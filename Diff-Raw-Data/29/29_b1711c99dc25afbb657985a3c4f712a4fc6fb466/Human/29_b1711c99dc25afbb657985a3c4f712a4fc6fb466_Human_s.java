 import java.util.Arrays;
 import java.util.Hashtable;
 
 public class Human extends Player {
 	
 	private String xo;
 
 	public Human(String startColor, boolean startsOnZeroSide) {
 		super(startColor, startsOnZeroSide);
 	}
 	
 	public Human (String startColor, boolean startsOnZeroSide, Robot startGameRobot) {
 		super(startColor, startsOnZeroSide, startGameRobot);
 	}
 
 	public Human (String startXO, boolean startOnZeroSide) {
 		super(startOnZeroSide);
 		this.xo = startXO;
 	}
 
 	public String getXO() {
 		if (this.xo ==null) {
 			return " ";
 		}
 		return this.xo;
 	}
 
 	public void takeTurn() {
 		super.performMove(this.inputMove());
 	}
 
 	public Move inputMove() {
 		if (this.startGameRobot!=null) {
 			//creates dictionary to hold scanned values
 			Hashtable<String, String> scannedLocations = new Hashtable<String, String>();
 			//gets list of moves from best to worst
 			Move[] possibleMoves = this.rankBestMoves(this.myBoard, 1);
 			
 			//iterates over all possible moves
 			for (Move m : possibleMoves) {
 				//gets all waypoints of the move
 				byte[][] waypoints = m.getWaypoints();
 				//declares pointColor variable
 				String pointColor;
 				
 				//declares variable to determine if the loop needs to continue
 				boolean shouldContinue = false;
 				//iterates over all waypoints which should be empty
 				for (byte[] waypoint : Arrays.copyOfRange(waypoints, 0, waypoints.length-1)) {
 					if (scannedLocations.containsKey(new String(waypoint))) {
 						pointColor = scannedLocations.get(new String(waypoint));
 					} else {
 						pointColor = this.gameRobot.examineLocation(waypoint);
 						scannedLocations.put(new String(waypoint), pointColor);
 					}
 					//checks if the square is not empty
 					if (pointColor!=Board.color) {
 						shouldContinue = true;
 						break;
 					}
 				}
 				//continue if the move failed
 				if (shouldContinue) {
 					continue;
 				}
 				
 				//checks the color of the last waypoint
 				if (scannedLocations.containsKey(new String(waypoint))) {
 					pointColor = scannedLocations.get(new String(waypoint));
 				} else {
 					pointColor = this.gameRobot.examineLocation(waypoints[waypoints.length-1]);
 					scannedLocations.put(new String(waypoint), pointColor);
 				}
 				//checks if the correct piece is not on the square
 				if (pointColor!=this.color) {
 					continue;
 				} else {
 					//this must be the right move, so return it
 					return m;
 				}
 			}
 			
 			//failed to find the right move
 			return null;
 		
 		} else {
 			for (byte y : new byte[] {0,1,2,3,4,5,6,7}) {
 				Piece[] theLine = new Piece[8];
 				for (byte x : new byte[] {0,1,2,3,4,5,6,7}) {
 					theLine[x] = super.getBoard().getPieceAtLocation(new byte[] {x,y}).getXO();
 				}
				System.out.prinln(Arrays.toString(theLine));
 			}
 		}
 	}
 }
