 package se.chalmers.dryleafsoftware.androidrally.model.gameModel;
 
 import java.beans.PropertyChangeListener;
 import java.beans.PropertyChangeSupport;
 import java.util.ArrayList;
 import java.util.List;
 
 import se.chalmers.dryleafsoftware.androidrally.model.cards.Card;
 import se.chalmers.dryleafsoftware.androidrally.model.cards.Deck;
 import se.chalmers.dryleafsoftware.androidrally.model.cards.Move;
 import se.chalmers.dryleafsoftware.androidrally.model.gameBoard.BoardElement;
 import se.chalmers.dryleafsoftware.androidrally.model.gameBoard.CheckPoint;
 import se.chalmers.dryleafsoftware.androidrally.model.gameBoard.ConveyorBelt;
 import se.chalmers.dryleafsoftware.androidrally.model.gameBoard.GameBoard;
 import se.chalmers.dryleafsoftware.androidrally.model.gameBoard.Gears;
 import se.chalmers.dryleafsoftware.androidrally.model.gameBoard.Laser;
 import se.chalmers.dryleafsoftware.androidrally.model.gameBoard.Wrench;
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
 	private PropertyChangeSupport pcs;
 	public static final String ROBOT_WON = "robotWon";
 	public static final String ROBOT_LOST = "robotLost";
 	private int robotsPlaying;
 	private boolean isGameOver;
 	
	private static final String testMap = "yxxxxxxxxxxxx5xxx36xyxxxxxxxxxx12xxxxx78:16xyxxxxxxxxxxxxx06xx58:16xyxxxxxxxx32xx22xx4xxx38xyxxxxxxxx1xxxxx06xx16xyxxxxxxxx103x103x103x103x103xxx18:16xyxxxxxxxx203x203x203x203x203xxx28:16xyxxxxxxxx1xxxxx06xxxyxxxxxxxxxxxx14xxx48:16xyxxxxxxxxxxxxx06xx68:16xyxxxxxxxxxxxxxxx88xyxxxxxxxxxxxx5xxxx";
 	/**
 	 * Creates a game board of size 12x16 tiles. Also creates robots based
 	 * on the amount of players. Creates a deck with cards that is shuffled.
 	 * 
 	 * @param pcl a PropertyChangeListener listening for event with propertyNames
 	 * gotten by static Strings ROBOT_WON and ROBOT_LOST
 	 * @param nbrOfPlayers the number of players in the game including CPU:s
 	 */
 	public GameModel(PropertyChangeListener pcl, int nbrOfPlayers) {
 		this(pcl, nbrOfPlayers, testMap);
 	}
 	
 	/**
 	 * Only for testing!!
 	 * @param pcl a PropertyChangeListener listening for event with propertyNames
 	 * gotten by static Strings ROBOT_WON and ROBOT_LOST
 	 * @param nbrOfPlayers
 	 * @param testMap
 	 */
 	public GameModel(PropertyChangeListener pcl, int nbrOfPlayers, String map) {
 		gameBoard = new GameBoard(map);
 		isGameOver = false;
 		robots = new ArrayList<Robot>();
 		int[][] startingPositions = gameBoard.getStartingPositions();
 		for (int i = 0; i < nbrOfPlayers; i++) {
 			robots.add(new Robot(startingPositions[i][0], startingPositions[i][1]));
 		}
 		robotsPlaying = nbrOfPlayers;
 		deck = new Deck();
 		pcs = new PropertyChangeSupport(this);
 		pcs.addPropertyChangeListener(pcl);
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
 	    int maxTravelDistance = gameBoard.getMaxConveyorBeltDistance();
 	    for (Robot robot : robots) {
 			gameBoard.getTile(robot.getX(), robot.getY()).instantAction(robot);
 		}
 	    checkIfRobotsOnMap();
 	    
 	    int[][] oldPositions = new int[robots.size()][2];
 	    for(int i = 0; i<maxTravelDistance; i++){
     		allMoves.add(";B1" + (maxTravelDistance-i));
 	    	for(int j = 0; j< robots.size(); j++){
 	    		oldPositions[j][0] = robots.get(j).getX();
 	    		oldPositions[j][1] = robots.get(j).getY();
 	    		List<BoardElement> boardElements = gameBoard.getTile(robots.get(j).getX(), 
 	    				robots.get(j).getY()).getBoardElements();
 		    	if(boardElements != null && boardElements.size() > 0){
 		    		if(boardElements.get(0) instanceof ConveyorBelt){//ConveyorBelt should always be first
 		    			if(((ConveyorBelt)boardElements.get(0)).getTravelDistance() >= maxTravelDistance-i){
 		    				boardElements.get(0).action(robots.get(j));
 		    				addSimultaneousMove(robots.get(j));
 							checkIfRobotsOnMap();
 		    				gameBoard.getTile(robots.get(j).getX(), robots.get(j).getY()).instantAction(robots.get(j));
 		    			}
 		    		}
 		    	}
 	    	}
 	    	checkConveyorBeltCollides(oldPositions);
 		    checkIfRobotsOnMap();
 	    }
 	    if(maxTravelDistance == 0){// if no robot stands on a conveyorBelt, ";B1" still
 	    	// is required for conveyorBelts to move in the GUI.
     		allMoves.add(";B10");
 	    }
 	    allMoves.add(";B4");
 	    for(int i = 0; i<robots.size(); i++){
 	    	List<BoardElement> boardElements = gameBoard.getTile(robots.get(i).getX(), 
     				robots.get(i).getY()).getBoardElements();
 	    	if(boardElements != null && boardElements.size() > 0){
 	    		for(BoardElement boardelement : boardElements){
 		    		if(boardelement instanceof Gears){
 		    			boardelement.action(robots.get(i));
 		    			addSimultaneousMove(robots.get(i));
 		    		}
 		    	}
 	    	}
 	    	
 	    }
 	    fireAllLasers();
 	    
 	    for(int i = 0; i < robots.size(); i++){
 	    	List<BoardElement> boardElements = gameBoard.getTile(robots.get(i).getX(), 
     				robots.get(i).getY()).getBoardElements();
 	    	if(boardElements != null && boardElements.size() > 0){
 	    		for(BoardElement boardelement : boardElements){
 	    			if(boardelement instanceof CheckPoint || boardelement instanceof Wrench){
 		    			boardelement.action(robots.get(i));
 		    			if (boardelement instanceof CheckPoint
 		    					&& ((CheckPoint)boardelement).getNbrOfCheckPoint()
 		    					== gameBoard.getNbrOfCheckPoints()) {
 		    				isGameOver = true;
 		    				pcs.firePropertyChange(ROBOT_WON, -1, i); //FIXME!!! Frga Linus om addMove osv. ang. GameOver
 		    			}
 		    		}
 	    		}
 	    	}
 	    }
 	    addDamageToAllMoves();
 	}
 	
 	private void addDamageToAllMoves(){
 		allMoves.add(";B5");
 		for(int i = 0; i<robots.size(); i++){
 			allMoves.add("#" + i + ":" + robots.get(i).getLife() + (Robot.STARTING_HEALTH - robots.get(i).getHealth()));
 		}
 	}
 	
 	/**
 	 * Return the map as a String[][]. Each String representing
 	 * a tile with it's boardelements.
 	 * @return the map as a String[][]
 	 */
 	public String getMap(){
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
 			if(y >= 0 && !gameBoard.getTile(x, y).getNorthWall()){
 				return true;
 			}
 		}else if(direction == GameBoard.WEST){
 			if(x>= 0 && !gameBoard.getTile(x, y).getWestWall()){
 				return true;
 			}
 		}else if(direction == GameBoard.SOUTH){
 			if(y <= gameBoard.getHeight()-1 && !gameBoard.getTile(x, y).getSouthWall()){
 				return true;
 			}
 		}else if(direction == GameBoard.EAST){
 			if(x <= gameBoard.getWidth()-1 && !gameBoard.getTile(x, y).getEastWall()){
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	/*
 	 * This method will only give proper answers if the robot moves in X-axis or Y-axis, not both.
 	 */
 	private boolean canMove(int oldX, int oldY, int x, int y){
 		if(oldY > y){
 			return canMove(oldX,oldY,GameBoard.NORTH);
 		}else if(oldX < x ){
 			return canMove(oldX,oldY,GameBoard.EAST);
 		}else if(oldY < y ){
 			return canMove(oldX,oldY,GameBoard.SOUTH);
 		}else if(oldX > x ){
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
 				noWall = canMove(x, y, direction);
 				y--;
 				robotIsHit = isRobotHit(x, y);
 			}
 			
 		}else if(direction == GameBoard.EAST){
 			while(x < gameBoard.getWidth() && !robotIsHit && noWall){
 				noWall = canMove(x, y, direction);
 				x++;
 				robotIsHit = isRobotHit(x, y);
 			}
 		}else if(direction == GameBoard.SOUTH){
 			while(y < gameBoard.getHeight() && !robotIsHit && noWall){
 				noWall = canMove(x, y, direction);
 				y++;
 				robotIsHit = isRobotHit(x, y);
 			}
 		}else if(direction == GameBoard.WEST){
 			while(x >= 0 && !robotIsHit && noWall){
 				noWall = canMove(x, y, direction);
 				x--;
 				robotIsHit = isRobotHit(x, y);
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
 	
 	/*
 	 * This method should only be called after conveyorBelts have moved all robots.
 	 * Size of oldPositions needs to be int[robots.size()][2]
 	 */
 	private void checkConveyorBeltCollides(int[][] oldPositions){
 		int nbrOfMovedRobots = 0;
 		for(int i = 0; i<robots.size(); i++){
 			if(robots.get(i).getX() != oldPositions[i][0] || robots.get(i).getY() != oldPositions[i][1]){
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
 						robots.get(i).setY(oldPositions[i][1]);
 						robots.get(j).setX(oldPositions[j][0]);
 						robots.get(j).setY(oldPositions[j][1]);
 						
 						int allMovesSize = allMoves.size();// The size will change during the loop, but must stay the same
 						// for the code to work.
 						for(int k = 1; k<=nbrOfMovedRobots; k++){
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
 			canMove(robots.get(i).getX(), robots.get(i).getY(), oldPositions[i][0], oldPositions[i][1]);
 		}
 	}
 	
 	/*
 	 * Return true if the collision needs to be reversed
 	 */
 	private boolean handleCollision(Robot robot, int oldX, int oldY){
 		boolean wallCollision = false;
 		if(canMove(oldX, oldY, robot.getX(), robot.getY())){
 			for(Robot r : robots){
 				// Do any robot stand on the same tile as another the robot from the parameters.
 				if(robot != r && robot.getX() == r.getX() && robot.getY() == r.getY()){
 					// Push other Robot
 					r.setX(r.getX() - (oldX - robot.getX()));
 					r.setY(r.getY() - (oldY - robot.getY()));
 					addSimultaneousMove(r);
 
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
 			allMoves.add(";" + "R#" + i);
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
 
 				int numberOfSteps = 1;
 				if(currentCards.get(indexOfHighestPriority)[i] instanceof Move){
 					numberOfSteps = Math.abs(((Move)currentCards.get(indexOfHighestPriority)[i]
 							).getDistance());
 				}
 				for(int k = 0; k<numberOfSteps; k++){
 					oldPosition[indexOfHighestPriority][0] = robots.get(indexOfHighestPriority).getX();
 					oldPosition[indexOfHighestPriority][1] = robots.get(indexOfHighestPriority).getY();
 					currentCards.get(indexOfHighestPriority)[i]
 							.action(currentRobot);
 					addMove(currentRobot);
 					handleCollision(currentRobot, oldPosition[indexOfHighestPriority][0], 
 							oldPosition[indexOfHighestPriority][1]);
 					checkIfRobotsOnMap();
 					gameBoard.getTile(currentRobot.getX(), currentRobot.getY())
 							.instantAction(currentRobot);
 					checkIfRobotsOnMap();
 				}
 				
 				//Remove the card so it doesn't execute twice
 				currentCards.get(indexOfHighestPriority)[i] = null;
 			}
 			activateBoardElements();
 			checkConveyorBeltCollides(oldPosition);
 		}
 		
 		for(Robot robot : robots){
 			deck.returnCards(robot.returnCards());
 		}
 		
 		//TODO give specials to robots standing on "wrench & hammer"
 	}
 	
 	private void checkIfRobotsOnMap(){
 		for(int i = 0; i < robots.size(); i++){
 			if(robots.get(i).getX() < 0 || robots.get(i).getX() >= gameBoard.getWidth() || 
 					robots.get(i).getY() < 0 || robots.get(i).getY() >= gameBoard.getHeight()){
 				robots.get(i).die(); //TODO maybe separate goto spawn point and lose life
 				if (robots.get(i).getLife() == 0) {
 					if (--robotsPlaying == 1) {
 						for (int j = 0; j < robots.size() ; j++) {
 							if (robots.get(j) != null) {
 								pcs.firePropertyChange(ROBOT_WON, -1, j);
 							}
 						}
 					} else {
 						pcs.firePropertyChange(ROBOT_LOST, -1, i);
 					}
 				}
 				resetRobotPosition(robots.get(i));
 			}
 		}
 	}
 	
 	private void resetRobotPosition(Robot robot){
 		int distanceFromSpawnPoint = 0;
 		while(true){
 			for(int i = robot.getSpawnPointX() - distanceFromSpawnPoint; i<=robot.getSpawnPointX() + distanceFromSpawnPoint; i++){
 				for(int j = robot.getSpawnPointY() - distanceFromSpawnPoint; j<=robot.getSpawnPointY() + distanceFromSpawnPoint; j++){
 					boolean tileOccupied = false;
 					for(Robot r : robots){
 						if((r.getX() == i && r.getY() == j)){
 							tileOccupied = true;
 						}
 					}
 					if(!tileOccupied && i>=0 && i<gameBoard.getWidth() && j>=0 && j<gameBoard.getHeight()){
 						robot.setX(i);
 						robot.setY(j);
 						allMoves.remove(allMoves.size()-1);
 						addMove(robot);
 						return;
 					}
 				}
 			}
 			distanceFromSpawnPoint++;
 			if(distanceFromSpawnPoint > 2){
 				break;
 			}
 		}
 	}
 	
 	private void addSimultaneousMove(Robot robot){
 		allMoves.add("#" + robots.indexOf(robot) + ":" + robot.getDirection() + 
 				robot.getXAsString() + robot.getYAsString());
 	}
 	
 	private void addMove(Robot robot){
 		allMoves.add(";" + robots.indexOf(robot) + ":" + robot.getDirection() + 
 				robot.getXAsString() + robot.getYAsString() );
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
 		// The first character will be a "split character" i.e. ; or #
 		String returnString = sb.substring(1);
 		return returnString;
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
 
 	public boolean isGameOver() {
 		return isGameOver;
 	}
 	
 }
