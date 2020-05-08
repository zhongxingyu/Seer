 package se.chalmers.dryleafsoftware.androidrally.model.gameModel;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import se.chalmers.dryleafsoftware.androidrally.model.cards.Card;
 import se.chalmers.dryleafsoftware.androidrally.model.cards.Deck;
 import se.chalmers.dryleafsoftware.androidrally.model.cards.Move;
 import se.chalmers.dryleafsoftware.androidrally.model.gameBoard.GameBoard;
 import se.chalmers.dryleafsoftware.androidrally.model.gameBoard.Laser;
 import se.chalmers.dryleafsoftware.androidrally.model.robots.Robot;
 
 /**
  * This is the mainModel for AndroidRally.
  * 
  *
  */
 public class GameModel {
 	public static int hejhej = 0;
 	private GameBoard gameBoard;
 	private List<Robot> robots;
 	private Deck deck;
 	private List<String> allMoves = new ArrayList<String>();
 	
 	private static String[][] testmap = new String[][] {
 			{"", "", "", "", "", "", "", "", "", "", "", "", "", "", "", ""},
 			{"", "16", "", "", "", "", "", "", "", "", "", "", "", "", "58", "68"},
 			{"", "", "", "", "", "", "", "14", "", "", "", "5", "", "", "48", "78"},
 			{"", "37", "", "1", "", "", "", "233", "", "", "1", "", "", "38", "", "88"},
 			{"", "", "", "", "", "", "", "233", "", "", "", "", "", "", "28", ""},
 			{"", "", "", "", "4", "", "", "", "", "", "", "", "", "", "18", ""},
 			{"", "", "", "", "", "", "", "133", "", "", "", "", "", "", "", ""},
 			{"", "5", "", "", "", "", "", "133", "", "", "", "1", "", "", "", ""},
 			{"", "", "", "", "103", "103", "103", "133:103", "", "", "", "", "", "", "", ""},
 			{"", "", "36", "", "", "", "", "", "", "", "", "", "", "", "", ""},
 			{"", "", "", "", "4", "", "", "", "", "", "", "22", "", "", "", ""},
 			{"", "", "", "", "", "", "", "", "", "", "", "", "", "", "", ""},
 	};
 	
 	/**
 	 * Creates a game board of size 12x16 tiles. Also creates robots based
 	 * on the amount of players. Creates a deck with cards that is shuffled.
 	 * 
 	 * @param nbrOfPlayers the number of players in the game including CPU:s
 	 */
 	public GameModel(int nbrOfPlayers) {
 		this(nbrOfPlayers, testmap);
 	}
 	
 	/**
 	 * Only for testing!!
 	 * @param nbrOfPlayers
 	 * @param testMap
 	 */
 	public GameModel(int nbrOfPlayers, String[][] map) {
 //		gameBoard = new GameBoard(12, 16, nbrOfPlayers);// TODO createGameBoard with proper data
 		gameBoard = new GameBoard(map);
 		robots = new ArrayList<Robot>();
 		int[][] startingPositions = gameBoard.getStartingPositions();
 		for (int i = 0; i < nbrOfPlayers; i++) {
 			robots.add(new Robot(startingPositions[i][0], startingPositions[i][1]));
 		}
 		deck = new Deck();
 	}
 	
 	/**
 	 * Give cards to all players/CPU:s.
 	 */
 	public void dealCards() {
 		for(Robot robot : robots) {
 			int nbrOfDrawnCards = robot.getHealth();
 			List<Card> drawnCards = new ArrayList<Card>();
 			for (int i = 0; i < nbrOfDrawnCards; i++) {
 				drawnCards.add(deck.drawCard());
 			}
 			robot.addCards(drawnCards);
 		}
 	}
 
 	/**
 	 * A method that make board elements "do things" with robots, i.e.
 	 * move robots that are standing on a conveyor belt and so on.
 	 */
 	public void activateBoardElements() {
 		hejhej = 1;
 		for (Robot robot : robots) {
 			int oldX = robot.getX();
 			int oldY = robot.getY();
 			gameBoard.getTile(robot.getX(), robot.getY()).action(robot);
 			gameBoard.getTile(robot.getX(), robot.getY()).instantAction(robot);
 		}
 		
 	}
 	
 	/**
 	 * Return the map as a String[][]. Each String representing
 	 * a tile with it's boardelements.
 	 * @return the map as a String[][]
 	 */
 	public String[][] getMap(){
 		return gameBoard.getMapAsString();
 	}
 	
 	private boolean isRobotHit(int x, int y){
 		for(Robot robot : this.robots){
 			if(robot.getX() == x && robot.getY() == y){
 				robot.damage(1);
 				return true;
 			}
 		}
 		return true;
 	}
 	
 	private boolean canMove(int x, int y, int direction){
 		if(direction == GameBoard.NORTH){
 			if(y >= 0 && !gameBoard.getTile(x, y).getNorthWall() && (y>0 && 
 					!gameBoard.getTile(x, y-1).getSouthWall())){
 				return true;
 			}
 		}else if(direction == GameBoard.WEST){
 			if(x>= 0 && !gameBoard.getTile(x, y).getWestWall() && (x>0 && 
 					!gameBoard.getTile(x-1, y).getEastWall())){
 				return true;
 			}
 		}else if(direction == GameBoard.SOUTH){
 			if(y <= gameBoard.getHeight()-1 && !gameBoard.getTile(x, y).getSouthWall() && 
 					(y<gameBoard.getHeight()-1 && !gameBoard.getTile(x, y+1).getNorthWall())){
 				return true;
 			}
 		}else if(direction == GameBoard.EAST){
 			if(x <= gameBoard.getWidth()-1 && !gameBoard.getTile(x, y).getEastWall() &&
 					(x<gameBoard.getWidth()-1 && !gameBoard.getTile(x+1, y).getWestWall())){
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	/*
 	 * This method will only give proper answers if the robot moves in X-axis or Y-axis, not both.
 	 */
 	private boolean canMove(int x, int y, int oldX, int oldY){
 		if(y > oldY){
 			return canMove(oldX,oldY,GameBoard.NORTH);
 		}else if(x < oldX ){
 			return canMove(oldX,oldY,GameBoard.EAST);
 		}else if(y < oldY ){
 			return canMove(oldX,oldY,GameBoard.SOUTH);
 		}else if(x > oldX ){
 			return canMove(oldX,oldY,GameBoard.WEST);
 		}
 		// This should only happen if the robot is standing still.
 		return true;
 	}
 	
 	private void fireLaser(int x, int y, int direction){
 		boolean robotIsHit = false;
 		boolean noWall = true;
 		if(direction == GameBoard.NORTH){
 			while(y >= 0 && !robotIsHit && noWall){
 				robotIsHit = isRobotHit(x, y);
 				noWall = canMove(x, y, direction);
 				y--;
 			}
 			
 		}else if(direction == GameBoard.EAST){
			while(x < GameBoard.WIDTH && !robotIsHit){
 				robotIsHit = isRobotHit(x, y);
 				noWall = canMove(x, y, direction);
 				x++;
 			}
 		}else if(direction == GameBoard.SOUTH){
			while(y < GameBoard.HEIGHT && !robotIsHit){
 				robotIsHit = isRobotHit(x, y);
 				noWall = canMove(x, y, direction);
 				y++;
 			}
 		}else if(direction == GameBoard.WEST){
 			while(x >= 0 && !robotIsHit){
 				robotIsHit = isRobotHit(x, y);
 				noWall = canMove(x, y, direction);
 				x--;
 			}
 		}
 
 	}
 	
 
 	/**
 	 * Fires all lasers from both robots and lasers attached to walls.
 	 */
 	public void fireAllLasers() {
 		List<Laser> lasers = gameBoard.getLasers();
 		int x;
 		int y;
 		int direction;
 		
 		for(Laser laser : lasers){
 			x = laser.getX();
 			y = laser.getY();
 			direction = laser.getDirection();
 			fireLaser(x, y, direction);
 		}
 		for(Robot robot : robots){
 			x = robot.getX();
 			y = robot.getY();
 			direction = robot.getDirection();
 			fireLaser(x, y, direction);
 		}
 	}
 	
 	/**
 	 * If a robots life is equal to 0 it will be deleted from the game.
 	 */
 	public void deleteDeadRobots() {
 		int i = 0;
 		while (i < robots.size()) {
 			if (robots.get(i).getLife() == 0) {
 				robots.remove(i);
 			} else {
 				i++;
 			}
 		}
 		if (robots.size() == 1) {
 			gameOver(robots.get(0));
 		}
 	}
 	
 	/**
 	 * Method gameOver is called by deleteDeadRobots() when there is only
 	 * one robot left.
 	 * @param winner the winning robot
 	 */
 	public void gameOver(Robot winner) {
 		//TODO add functionality or change to boolean?
 	}
 	
 	/*
 	 * This method should only be called after conveyorBelts have moved all robots.
 	 * Size of oldPositions needs to be int[robots.size()][2]
 	 */
 	private void checkConveyorBeltCollides(int[][] oldPositions){
 //		List<Robot> moveIndexToBeRemoved = new ArrayList<Robot>();
 		int nbrOfMovedRobots = 0;
 		for(int i = 0; i<robots.size(); i++){
 			if(robots.get(i).getX() != oldPositions[i][0] || robots.get(i).getY() != oldPositions[i][1]){
 				addConveyorBeltMove(robots.get(i));
 				nbrOfMovedRobots++;
 			}
 		}
 		List<Robot> handleCollision = new ArrayList<Robot>();
 		for(int i = 0; i<robots.size(); i++){
 			for(int j = 0; j<robots.size(); j++){
 				if(i != j && robots.get(i).getX() == robots.get(j).getX() && 
 						robots.get(i).getY() == robots.get(j).getY()){
 					boolean robotIMove = oldPositions[i][0] != robots.get(i).getX() || oldPositions[i][1] != 
 							robots.get(i).getY();
 					boolean robotJMove = (oldPositions[j][0] != robots.get(j).getX() || 
 							oldPositions[j][1] != robots.get(j).getY());
 					// If both robots have moved to the same position by conveyorBelt, both should move back.
 					if(robotIMove && robotJMove){
 						robots.get(i).setX(oldPositions[i][0]);
 						robots.get(j).setY(oldPositions[i][1]);
 						robots.get(i).setX(oldPositions[j][0]);
 						robots.get(j).setY(oldPositions[j][1]);
 						
 						int allMovesSize = allMoves.size();// The size will change during the loop, but must stay the same
 						// for the code to work.
						for(int k = 0; k<nbrOfMovedRobots; k++){
 							if(allMoves.get(allMovesSize - k).contains(i + ":") || 
 									allMoves.get(allMovesSize - k).contains(j + ":")){
 								allMoves.remove(allMovesSize - k);
 							}
 						}
 						nbrOfMovedRobots -= 2;
 					}else{// Push robot
 						if(robotIMove){
 							handleCollision.add(robots.get(i));
 						}else if(robotJMove){
 							handleCollision.add(robots.get(j));
 						}
 					}
 				}
 			}
 		}
 		for(int i = 0; i<robots.size(); i++){
 			handleCollision(robots.get(i), oldPositions[i][0], oldPositions[i][1]);
 		}
 		// Alters the last String to the correct syntax.
 		if(nbrOfMovedRobots > 0){
 			String stringToBeChanged = allMoves.remove(allMoves.size()-1);
 			if(allMoves.get(allMoves.size()-1).contains("B#")){
 				stringToBeChanged = stringToBeChanged + ";";
 			}else{
 				stringToBeChanged = stringToBeChanged.replace('#', ';');
 			}
 			allMoves.add(stringToBeChanged);
 		}
 	}
 	
 	/*
 	 * Return true if the collision needs to be reversed
 	 */
 	private boolean handleCollision(Robot robot, int oldX, int oldY){
 		boolean wallCollision = false;
 //		System.out.println("index of robot " + robots.indexOf(robot));
 //		System.out.println("rx " + robot.getX() + ", ry " + robot.getY() + ", ox " + oldX + ", oy " + oldY);
 		
 		if(canMove(oldX, oldY, robot.getX(), robot.getY())){
 			for(Robot r : robots){
 				// Do any robot stand on the same tile as another the robot from the parameters.
 				if(robot != r && robot.getX() == r.getX() && robot.getY() == r.getY()){
 					// Push other Robot
 					r.setX(r.getX() - (oldX - robot.getX()));
 					r.setY(r.getY() - (oldY - robot.getY()));
 					addMove(r);
 //					System.out.println("2rx " + robot.getX() + ", ry " + robot.getY() + ", ox " + oldX + ", oy " + oldY);
 
 					// Check if other Robot collides
 					if(handleCollision(r, robot.getX(), robot.getY())){// true if r walks into a wall
 						robot.setX(robot.getX() + (oldX - robot.getX()));
 						robot.setY(robot.getY() + (oldY - robot.getY()));
 						allMoves.remove(allMoves.size()-1);// It is always the last move which should be reversed.
 						wallCollision = true;
 					}
 				}
 			}
 		}else{// The robot can't walk through a wall
 			robot.setX(oldX);
 			robot.setY(oldY);
 			allMoves.remove(allMoves.size()-1); // It is always the last move which should be reversed.
 			wallCollision = true;
 		}
 		return wallCollision;
 	}
 	
 	/**
 	 * Move robots according to the chosen cards.
 	 */
 	public void moveRobots() {
 		allMoves.clear();
 		List<Card[]> currentCards = new ArrayList<Card[]>();
 		for (int i = 0; i < robots.size(); i++) {
 			Card[] chosenCards = robots.get(i).getChosenCards();
 				currentCards.add(chosenCards);
 		}
 		int[][] oldPosition = new int[robots.size()][2];
 		
 		for (int i = 0; i < 5; i++) { //loop all 5 cards
 			for(int j = 0; j < robots.size(); j++){ //for all robots
 				for(int k = 0; k<robots.size(); k++){
 					oldPosition[k][0] = robots.get(k).getX();
 					oldPosition[k][1] = robots.get(k).getY();
 				}
 				int highestPriority = 0;
 				int indexOfHighestPriority = -1; //player index in array
 				for (int k = 0; k < currentCards.size(); k++) { //find highest card
 					if (currentCards.get(k)[i] != null //check if card exists and..
 						&&	highestPriority //..is the highest one
 							< currentCards.get(k)[i].getPriority()) {
 						highestPriority = currentCards.get(k)[i].getPriority();
 						indexOfHighestPriority = k;
 					}
 				}
 				//Move the robot that has the highest priority on its card
 				Robot currentRobot = robots.get(indexOfHighestPriority);
 
 				currentCards.get(indexOfHighestPriority)[i]
 						.action(currentRobot);
 				addMove(currentRobot);
 				System.out.println(robots.indexOf(currentRobot) + " rx " + currentRobot.getX() + ", ry " + currentRobot.getY() + ", ox " + oldPosition[indexOfHighestPriority][0] + ", oy " + oldPosition[indexOfHighestPriority][1]);
 				handleCollision(currentRobot, oldPosition[indexOfHighestPriority][0], 
 						oldPosition[indexOfHighestPriority][1]);
 				gameBoard.getTile(currentRobot.getX(), currentRobot.getY())
 						.instantAction(currentRobot);
 				checkIfRobotsOnMap();
 				deleteDeadRobots();
 				
 				//Remove the card so it doesn't execute twice
 				currentCards.get(indexOfHighestPriority)[i] = null;
 			}
 			allMoves.add("B#");
 			activateBoardElements();
 			for(int a = 0; a<robots.size(); a++){
 				System.out.println("........." + a + " rx " + robots.get(a).getX() + ", ry " + robots.get(a).getY() + ", ox " + oldPosition[a][0] + ", oy " + oldPosition[a][1]);
 			}
 			checkConveyorBeltCollides(oldPosition);
 			for(int a = 0; a<robots.size(); a++){
 				System.out.println("-------" + a + " rx " + robots.get(a).getX() + ", ry " + robots.get(a).getY() + ", ox " + oldPosition[a][0] + ", oy " + oldPosition[a][1]);
 			}
 			allMoves.add("R#" + i + ";");
 		}
 		
 		for(Robot robot : robots){
 			deck.returnCards(robot.returnCards());
 		}
 		
 		//TODO give specials to robots standing on "wrench & hammer"
 	}
 	
 	private void checkIfRobotsOnMap(){
 		for(int i = 0; i<robots.size(); i++){
			if(robots.get(i).getX() < 0 || robots.get(i).getX() >= GameBoard.WIDTH || 
					robots.get(i).getY() < 0 || robots.get(i).getY() >= GameBoard.HEIGHT){
 				robots.get(i).die();
 			}
 		}
 	}
 	
 	private void addConveyorBeltMove(Robot robot){
 		allMoves.add(robots.indexOf(robot) + ":" + robot.getDirection() + 
 				robot.getXAsString() + robot.getYAsString() + "#");
 	}
 	
 	private void addMove(Robot robot){
 		allMoves.add(robots.indexOf(robot) + ":" + robot.getDirection() + 
 				robot.getXAsString() + robot.getYAsString() + ";");
 	}
 	
 	/**
 	 * Return a String containing all moves during a round.
 	 * @return a String containing all moves during a round.
 	 */
 	public String getAllMoves(){
 		StringBuilder sb = new StringBuilder();
 		for(String string : allMoves){
 			sb.append(string);
 		}
 		return sb.toString();
 	}
 	
 	/**
 	 * Returns a list of all robots in the game.
 	 * @return a list of all robots in the game.
 	 */
 	public List<Robot> getRobots(){
 		return robots;
 	}
 	
 	public GameBoard getGameBoard() {
 		return gameBoard;
 	}
 }
