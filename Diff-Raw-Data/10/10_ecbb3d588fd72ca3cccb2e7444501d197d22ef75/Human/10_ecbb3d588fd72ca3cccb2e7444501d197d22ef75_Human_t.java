 import java.util.Arrays;
 import java.util.ArrayList;
 import java.util.Scanner;
 
 public class Human extends Player {
 	
 	private String xo;
 	private String inputLine;
 	private Scanner in;
 
 	//public Human(String startColor, boolean startsOnZeroSide) {
 		//super(startColor, startsOnZeroSide);
 	//}
 	
 	public Human (String startColor, boolean startsOnZeroSide, Robot startGameRobot) {
 		super(startColor, startsOnZeroSide, startGameRobot);
 	}
 
 	public Human (String startXO, boolean startOnZeroSide) {
 		super(startOnZeroSide);
 		this.xo = startXO;
 		this.in = new Scanner(System.in);
 	}
 
 	public String getXO() {
 		if (this.xo ==null) {
 			return " ";
 		}
 		return this.xo;
 	}
 
 	public void takeTurn(Game g) {
 		super.performMove(this.inputMove(g), g.getGameBoard());
 	}
 
 	public Move inputMove(Game g) {
 		if (this.getRobot()!=null) {
 			//creates dictionary to hold scanned values
 			ArrayList<int[]> scannedLocations = new ArrayList<int[]>();
 			ArrayList<String> locationValues = new ArrayList<String>();
 			//gets list of moves from best to worst
 			Move[] possibleMoves = this.rankBestMoves(g, 1);
 			
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
 						pointColor = locationValues.get(scannedLocations.indexOf(waypoint));
 					} else {
 						pointColor = this.getRobot().examineLocation(waypoint);
 						scannedLocations.add(waypoint);
 						locationValues.add(pointColor);
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
 				
 				//sets last waypoint
 				int[] waypoint = waypoints[waypoints.length-1];
 				//checks the color of the last waypoint
 				if (scannedLocations.contains(waypoint)) {
 					pointColor = locationValues.get(scannedLocations.indexOf(waypoint));
 				} else {
 					pointColor = this.getRobot().examineLocation(waypoints[waypoints.length-1]);
 					scannedLocations.add(waypoint);
 					locationValues.add(pointColor);
 				}
 				//checks if the correct piece is not on the square
 				if (pointColor!=this.getColor()) {
 					continue;
 				} else {
 					//this must be the right move, so return it
 					return m;
 				}
 			}
 			
 			//failed to find the right move
 			return null;
 		
 		} else {
 			for (int y : new int[] {0,1,2,3,4,5,6,7}) {
 				String[] theLine = new String[8];
 				for (int x : new int[] {0,1,2,3,4,5,6,7}) {
 					if (super.getBoard().getPieceAtLocation(new int[] {x,y}) != null) {
 						theLine[x] = super.getBoard().getPieceAtLocation(new int[] {x,y}).getPlayer().getXO();
 					} else {
 						theLine[x] = "-";
 					}
 				}
 				System.out.println(Arrays.toString(theLine));
 			}
 			boolean moveEntered = false;
 			Move inputtedMove = null;
 			while (!moveEntered) {
 				System.out.println("Enter Move:");
 				this.inputLine = this.in.nextLine();
 				int numberOfWaypoints = (this.inputLine.length()+1)/3;
 				int[][] allWaypoints = new int[numberOfWaypoints][];
 				for (int i=0; i<numberOfWaypoints; i++) {
 					allWaypoints[i] = new int[] {Integer.valueOf(inputLine.charAt(3*i)),Integer.valueOf(inputLine.charAt(3*i+1))};
 				}
 				System.out.println(this.getBoard().getPieceAtLocation(allWaypoints[0]));
				if (this.getBoard().getPieceAtLocation(allWaypoints[0]) == null) {
					System.out.println("This is the problem.");
				}
 				inputtedMove = new Move(this.getBoard().getPieceAtLocation(allWaypoints[0]), allWaypoints);
 				if (inputtedMove.calculateIsValid()) {
 					moveEntered = true;
 				}
 			}
 			return inputtedMove;
 		}
 	}
 }
