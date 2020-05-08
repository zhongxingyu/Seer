 package se.chalmers.dryleafsoftware.androidrally.model.gameModel;
 
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
  */
 public class GameModel {
 	private GameBoard gameBoard;
 	private Deck deck;
 	private List<Robot> robots;
 	private List<String> allMoves = new ArrayList<String>();
 	private int robotsPlaying;
 	private boolean isGameOver;
 
 	/**
 	 * Creates a game board of size 12x16 tiles. Also creates robots based on
 	 * the amount of players. Creates a deck with cards that is shuffled.
 	 * 
 	 * @param nbrOfRobots
 	 *            players + computer controlled robots
 	 * @param map
 	 *            A game map in String format
 	 */
 	public GameModel(int nbrOfRobots, String map) {
 		gameBoard = new GameBoard(map);
 		isGameOver = false;
 		robots = new ArrayList<Robot>();
 		int[][] startingPositions = gameBoard.getStartingPositions();
 		for (int i = 0; i < nbrOfRobots; i++) {
 			robots.add(new Robot(startingPositions[i][0],
 					startingPositions[i][1]));
 		}
 		robotsPlaying = nbrOfRobots;
 		deck = new Deck();
 	}
 
 	/**
 	 * Give cards to all players/CPU:s.
 	 */
 	public void dealCards() {
 		deck.shuffleDeck();
 		for (Robot robot : robots) {
 			int nbrOfDrawnCards = robot.getHealth();
 			List<Card> drawnCards = new ArrayList<Card>();
 			for (int i = 0; i < nbrOfDrawnCards; i++) {
 				drawnCards.add(deck.drawCard());
 			}
 			robot.addCards(drawnCards);
 		}
 	}
 
 	/**
 	 * Move robots according to the chosen cards.
 	 */
 	public void moveRobots() {
 		allMoves.clear();
 		List<Card[]> currentCards = new ArrayList<Card[]>();
 		for (int i = 0; i < robots.size(); i++) {
 			Card[] chosenCards = robots.get(i).getChosenCards().clone();
 			currentCards.add(chosenCards);
 		}
 		int[][] oldPosition = new int[robots.size()][2];
 	
 		for (int i = 0; i < 5; i++) { // loop all 5 cards
 			allMoves.add(";" + "R#" + i);
 			for (int j = 0; j < robots.size(); j++) { // for all robots
 				for (int k = 0; k < robots.size(); k++) {
 					oldPosition[k][0] = robots.get(k).getX();
 					oldPosition[k][1] = robots.get(k).getY();
 				}
 				int highestPriority = -1;
 				int indexOfHighestPriority = -1; // player index in array
 				for (int k = 0; k < currentCards.size(); k++) { // find highest card
 					if (currentCards.get(k)[i] != null // check if card exists and..
 							&& highestPriority // ..is the highest one
 							< currentCards.get(k)[i].getPriority()) {
 						highestPriority = currentCards.get(k)[i].getPriority();
 						indexOfHighestPriority = k;
 					}
 				}
 				if (indexOfHighestPriority == -1) {
 					continue;// This will only happen when a robot don't have a..
 				}			// ..card, i.e. it has lost.
 				// Move the robot that has the highest priority on its card
 				Robot currentRobot = robots.get(indexOfHighestPriority);
 	
 				int nbrOfSteps = 1;
 				if (currentCards.get(indexOfHighestPriority)[i] instanceof Move) {
 					nbrOfSteps = Math.abs(((Move) currentCards
 							.get(indexOfHighestPriority)[i]).getDistance());
 				}
 				for (int k = 0; k < nbrOfSteps; k++) {
 					if (robots.get(indexOfHighestPriority).isDead()) {
 						break; // so that robot doesn't walk more after dying
 								// this for-loop
 					}
 					oldPosition[indexOfHighestPriority][0] = robots.get(
 							indexOfHighestPriority).getX();
 					oldPosition[indexOfHighestPriority][1] = robots.get(
 							indexOfHighestPriority).getY();
 					currentCards.get(indexOfHighestPriority)[i]
 							.action(currentRobot);
 					addMove(currentRobot);
 					handleCollision(currentRobot,
 							oldPosition[indexOfHighestPriority][0],
 							oldPosition[indexOfHighestPriority][1]);
 					if (checkGameStatus())
 						return;
 					if (!currentRobot.isDead()) {
 						gameBoard.getTile(currentRobot.getX(),
 								currentRobot.getY())
 								.instantAction(currentRobot);
 					}
 					if (currentRobot.isDead()) {
 						addRobotDeadMove(currentRobot);
 					}
 					if (checkGameStatus())
 						return;
 				}
 	
 				// Remove the card so it doesn't execute twice
 				currentCards.get(indexOfHighestPriority)[i] = null;
 			}
 			activateBoardElements();
 			if (checkGameStatus())
 				return;
 		}
 	
 		allMoves.add(";B7");// B7 = robot respawn actions.
 		for (Robot robot : robots) {
 			deck.returnCards(robot.returnCards());
 			if (robot.isDead() && !robot.hasLost()) {
 				resetRobotPosition(robot);
 				robot.setDead(false);
 			}
 		}
 	}
 
 	/**
 	 * A method that make board elements "do things" with robots, i.e. move
 	 * robots that are standing on a conveyor belt and so on.
 	 */
 	private void activateBoardElements() {
 		handleConveyorBelts();
 		handleGears();
 		handleImmobileActions();
 	}
 
 	/**
 	 * Make conveyor belts move, check collision and so on.
 	 */
 	private void handleConveyorBelts() {
 		int maxTravelDistance = gameBoard.getMaxConveyorBeltDistance();
 		for (Robot robot : robots) {
 			if (!robot.isDead()) {
 				gameBoard.getTile(robot.getX(), robot.getY()).instantAction(
 						robot);
 				if (robot.isDead()) {
 					addRobotDeadMove(robot);
 				}
 			}
 		}
 		if (checkGameStatus())
 			return;
 
 		int[][] oldPositions = new int[robots.size()][2];
 		for (int i = 0; i < maxTravelDistance; i++) {
 			allMoves.add(";B" + "1" + (maxTravelDistance - i));
 			for (int j = 0; j < robots.size(); j++) {
 				if (robots.get(j).isDead()) {
 					continue;
 				}
 				oldPositions[j][0] = robots.get(j).getX();
 				oldPositions[j][1] = robots.get(j).getY();
 				List<BoardElement> boardElements = gameBoard.getTile(
 						robots.get(j).getX(), robots.get(j).getY())
 						.getBoardElements();
 				if (boardElements != null && boardElements.size() > 0) {
 					if (boardElements.get(0) instanceof ConveyorBelt) {// ConveyorBelt should..
 						if (((ConveyorBelt) boardElements.get(0))		// ..be first
 								.getTravelDistance() >= maxTravelDistance - i) {
 							boardElements.get(0).action(robots.get(j));
//							addSimultaneousMove(robots.get(j));
 							if (checkGameStatus())
 								return;
 							if (robots.get(j).isDead()) {
 								addRobotDeadMove(robots.get(j));
 							}
 						}
 					}
 				}
 			}
 			checkConveyorBeltCollides(oldPositions);
 			for (Robot robot : robots) {
 				if (!robot.isDead()) {
 					gameBoard.getTile(robot.getX(), robot.getY())
 							.instantAction(robot);
 					if (robot.isDead()) {
 						addRobotDeadMove(robot);
 					}
 				}
 			}
 			if (checkGameStatus())
 				return;
 		}
 	}
 
 	/**
 	 * Handles all checkPoints reached and repair/damage done during a round.
 	 */
 	private void handleImmobileActions() {
 		int[] oldCheckPointReached = new int[robots.size()];
 		int[] oldRobotHealth = new int[robots.size()];
 		for (int i = 0; i < robots.size(); i++) {
 			oldRobotHealth[i] = robots.get(i).getHealth();
 			oldCheckPointReached[i] = robots.get(i).getLastCheckPoint();
 		}
 		fireAllLasers();
 		if (checkGameStatus())
 			return;
 
 		for (int i = 0; i < robots.size(); i++) {
 			if (robots.get(i).isDead()) {
 				continue;
 			}
 			List<BoardElement> boardElements = gameBoard.getTile(
 					robots.get(i).getX(), robots.get(i).getY())
 					.getBoardElements();
 			if (boardElements != null && boardElements.size() > 0) {
 				for (BoardElement boardelement : boardElements) {
 					if (boardelement instanceof CheckPoint
 							|| boardelement instanceof Wrench) {
 						boardelement.action(robots.get(i));
 					}
 				}
 			}
 		}
 		addDamageToAllMoves(oldRobotHealth);
 		addCheckPointReached(oldCheckPointReached);
 	}
 
 	/**
 	 * Make gears turn.
 	 */
 	private void handleGears() {
 		allMoves.add(";B4");
 		for (int i = 0; i < robots.size(); i++) {
 			if (robots.get(i).isDead()) {
 				continue;
 			}
 			List<BoardElement> boardElements = gameBoard.getTile(
 					robots.get(i).getX(), robots.get(i).getY())
 					.getBoardElements();
 			if (boardElements != null && boardElements.size() > 0) {
 				for (BoardElement boardelement : boardElements) {
 					if (boardelement instanceof Gears) {
 						boardelement.action(robots.get(i));
 						addSimultaneousMove(robots.get(i));
 					}
 				}
 			}
 
 		}
 	}
 
 	/**
 	 * Add a checkpoint update String to allMoves. This method will generate errors
 	 * in the allMove string if called with a bad timing. See separate developer
 	 * document for when to use it.
 	 * 
 	 * @param oldCheckPoints
 	 *            checkpoints robots had before action
 	 */
 	private void addCheckPointReached(int[] oldCheckPoints) {
 		allMoves.add(";B6");
 		for (int i = 0; i < robots.size(); i++) {
 			if (!robots.get(i).isDead()
 					&& robots.get(i).getLastCheckPoint() != oldCheckPoints[i]) {
 				allMoves.add("#" + i + ":" + robots.get(i).getLastCheckPoint());
 			}
 		}
 	}
 	
 	/**
 	 * Add a damage update String to allMoves. This method will generate errors
 	 * in the allMove string if called with a bad timing. See separate developer
 	 * document for when to use it.
 	 * 
 	 * @param oldRobotHealth
 	 *            health robots had before action
 	 */
 	private void addDamageToAllMoves(int[] oldRobotHealth) {
 		allMoves.add(";B5");
 		for (int i = 0; i < robots.size(); i++) {
 			if (robots.get(i).getHealth() != oldRobotHealth[i]) {
 				if (robots.get(i).isDead()) {
 					allMoves.add("#" + i + ":" + "1" + robots.get(i).getLife()
 							+ (Robot.STARTING_HEALTH - robots.get(i).getHealth()));
 				} else {
 					allMoves.add("#" + i + ":" + "0" + +robots.get(i).getLife()
 							+ (Robot.STARTING_HEALTH - robots.get(i).getHealth()));
 				}
 			}
 		}
 	}
 
 	/**
 	 * Check if a robot is hit by a laser on a certain position.
 	 * @param x robot position x
 	 * @param y robot position y
 	 * @return true if a robot is hit, else false
 	 */
 	private boolean isRobotHit(int x, int y) {
 		for (Robot robot : this.robots) {
 			if (!robot.isDead() && robot.getX() == x && robot.getY() == y) {
 				robot.damage(1);
 				return true;
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * Checks if it is possible to move in a specific direction. <br>
 	 * This method will only give proper answers if the robot moves one step in
 	 * X-axis or Y-axis, not both.
 	 */
 	private boolean canMove(int x, int y, int direction) {
 		if (direction == GameBoard.NORTH) {
 			if (y >= 0 && !gameBoard.getTile(x, y).getNorthWall()) {
 				return true;
 			}
 		} else if (direction == GameBoard.WEST) {
 			if (x >= 0 && !gameBoard.getTile(x, y).getWestWall()) {
 				return true;
 			}
 		} else if (direction == GameBoard.SOUTH) {
 			if (y <= gameBoard.getHeight() - 1
 					&& !gameBoard.getTile(x, y).getSouthWall()) {
 				return true;
 			}
 		} else if (direction == GameBoard.EAST) {
 			if (x <= gameBoard.getWidth() - 1
 					&& !gameBoard.getTile(x, y).getEastWall()) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * Check if there is a wall preventing a move from a tile to another tile.
 	 * This method will only function properly if the movement is one step in
 	 * the x-axis or the y-axis, not both.
 	 * 
 	 * @param oldX the position in the x-axis to move from.
 	 * @param oldY the position in the y-axis to move from.
 	 * @param x the position in the x-axis to move to.
 	 * @param y the position in the x-axis to move to.
 	 * @return true if there is no wall preventing the move.
 	 */
 	private boolean canMove(int oldX, int oldY, int x, int y) {
 		if (oldY > y) {
 			return canMove(oldX, oldY, GameBoard.NORTH);
 		} else if (oldX < x) {
 			return canMove(oldX, oldY, GameBoard.EAST);
 		} else if (oldY < y) {
 			return canMove(oldX, oldY, GameBoard.SOUTH);
 		} else if (oldX > x) {
 			return canMove(oldX, oldY, GameBoard.WEST);
 		}
 		// This should only happen if the robot is standing still.
 		return true;
 	}
 
 	/**
 	 * Fire the robots' lasers.
 	 * 
 	 * @param robot A robot
 	 */
 	private void fireRobotLaser(Robot robot) {
 		int x = robot.getX();
 		int y = robot.getY();
 		int direction = robot.getDirection();
 		if (canMove(x, y, direction)) {
 			if (direction == GameBoard.NORTH) {
 				fireLaser(x, y - 1, direction);
 			} else if (direction == GameBoard.EAST) {
 				fireLaser(x + 1, y, direction);
 			} else if (direction == GameBoard.SOUTH) {
 				fireLaser(x, y + 1, direction);
 			} else if (direction == GameBoard.WEST) {
 				fireLaser(x - 1, y, direction);
 			}
 		}
 	}
 
 	/**
 	 * Fire lasers on the map. Also called by fireRobotLaser because of almost
 	 * same logic.
 	 * 
 	 * @param x coordinate on x-axis
 	 * @param y coordinate on y-axis
 	 * @param direction
 	 *            a specific direction such as GameBoard.[DIRECTION]
 	 */
 	private void fireLaser(int x, int y, int direction) {
 		boolean robotIsHit = false;
 		boolean isNoWall = true;
 		if (direction == GameBoard.NORTH) {
 			while (y >= 0 && !robotIsHit && isNoWall) {
 				robotIsHit = isRobotHit(x, y);
 				isNoWall = canMove(x, y, direction);
 				y--;
 			}
 
 		} else if (direction == GameBoard.EAST) {
 			while (x < gameBoard.getWidth() && !robotIsHit && isNoWall) {
 				robotIsHit = isRobotHit(x, y);
 				isNoWall = canMove(x, y, direction);
 				x++;
 			}
 		} else if (direction == GameBoard.SOUTH) {
 			while (y < gameBoard.getHeight() && !robotIsHit && isNoWall) {
 				robotIsHit = isRobotHit(x, y);
 				isNoWall = canMove(x, y, direction);
 				y++;
 			}
 		} else if (direction == GameBoard.WEST) {
 			while (x >= 0 && !robotIsHit && isNoWall) {
 				robotIsHit = isRobotHit(x, y);
 				isNoWall = canMove(x, y, direction);
 				x--;
 			}
 		}
 	}
 
 	/**
 	 * Fires all lasers from both robots and lasers attached to walls.
 	 */
 	private void fireAllLasers() {
 		List<Laser> lasers = gameBoard.getLasers();
 		int x;
 		int y;
 		int direction;
 
 		for (Laser laser : lasers) {
 			x = laser.getX();
 			y = laser.getY();
 			direction = laser.getDirection();
 			fireLaser(x, y, direction);
 		}
 		for (Robot robot : robots) {
 			if (robot.isDead()) {
 				continue;
 			}
 			fireRobotLaser(robot);
 		}
 	}
 
 	/**
 	 * This method should only be called after conveyorBelts have moved all
 	 * robots. Size of oldPositions needs to be int[robots.size()][2]
 	 */
 	private void checkConveyorBeltCollides(int[][] oldPositions) {
 		for (int i = 0; i < robots.size(); i++) {
 			if (!robots.get(i).isDead()
 					&& (robots.get(i).getX() != oldPositions[i][0] || robots
 							.get(i).getY() != oldPositions[i][1])) {
 				if (!canMove(robots.get(i).getX(), robots.get(i).getY(),
 						oldPositions[i][0], oldPositions[i][1])) {
 					robots.get(i).setX(oldPositions[i][0]);
 					robots.get(i).setY(oldPositions[i][1]);
 				}
 			}
 		}
 
 		List<Robot> handleCollision = new ArrayList<Robot>();
 		for (int i = 0; i < robots.size(); i++) {
 			if (!robots.get(i).isDead()
 					&& (robots.get(i).getX() != oldPositions[i][0] || robots
 							.get(i).getY() != oldPositions[i][1])) {
 				addSimultaneousMove(robots.get(i));
 				for (int j = 0; j < robots.size(); j++) {
 					if (i != j && !robots.get(j).isDead()
 							&& robots.get(i).getX() == robots.get(j).getX()
 							&& robots.get(i).getY() == robots.get(j).getY()) {
 						// If both robots moved to the same tile by
 						// conveyorBelts, both should move back.
 						if ((oldPositions[j][0] != robots.get(j).getX() || oldPositions[j][1] != robots
 								.get(j).getY())) {
 							robots.get(i).setX(oldPositions[i][0]);
 							robots.get(i).setY(oldPositions[i][1]);
 							robots.get(j).setX(oldPositions[j][0]);
 							robots.get(j).setY(oldPositions[j][1]);
 
 							allMoves.remove(allMoves.size() - 1);
 						} else {// Push robot
 							handleCollision.add(robots.get(i));
 						}
 					}
 				}
 			}
 		}
 		for (Robot robot : handleCollision) {
 			handleCollision(robot, oldPositions[robots.indexOf(robot)][0],
 					oldPositions[robots.indexOf(robot)][1]);
 		}
 	}
 
 	/**
 	 * Handles collision between robots that are pushing each other. Return true
 	 * if the collision needs to be reversed.
 	 */
 	private boolean handleCollision(Robot robot, int oldX, int oldY) {
 		boolean wallCollision = false;
 		if (canMove(oldX, oldY, robot.getX(), robot.getY())) {
 			for (Robot r : robots) {
 				// Do any robot stand on the same tile as the robot from the
 				// parameters.
 				if (!r.isDead() && robot != r && robot.getX() == r.getX()
 						&& robot.getY() == r.getY()) {
 					// Push other Robot
 					r.setX(r.getX() - (oldX - robot.getX()));
 					r.setY(r.getY() - (oldY - robot.getY()));
 					addSimultaneousMove(r);
 					// Check if other Robot collides
 					if (handleCollision(r, robot.getX(), robot.getY())) {// true if r walks into a wall
 						robot.setX(robot.getX() + (oldX - robot.getX()));
 						robot.setY(robot.getY() + (oldY - robot.getY()));
 						allMoves.remove(allMoves.size() - 1);// It is always the last move which should be reversed.
 						wallCollision = true;
 					}
 					checkGameStatus();
 					if (!r.isDead()) {
 						gameBoard.getTile(r.getX(), r.getY()).instantAction(r);
 					}
 					if (r.isDead()) {
 						addRobotDeadMove(r);
 					}
 				}
 			}
 		} else {// The robot can't walk through a wall
 			robot.setX(oldX);
 			robot.setY(oldY);
 			allMoves.remove(allMoves.size() - 1); // It is always the last move which should be reversed.
 			wallCollision = true;
 		}
 		return wallCollision;
 	}
 
 	/**
 	 * Checks if the game is over. It is over if a robot has reached the last
 	 * checkpoint or if all robots except one are dead. It checks if robots are
 	 * dead and if they have lost.
 	 * 
 	 * @return true if if game is over, else false
 	 */
 	private boolean checkGameStatus() {
 		for (int i = 0; i < robots.size(); i++) {
 			if (robotHasReachedLastCheckPoint())
 				return true;
 			if (robots.get(i).getHealth() < 0
 					|| !robots.get(i).isDead()
 					&& (robots.get(i).getX() < 0
 							|| robots.get(i).getX() >= gameBoard.getWidth()
 							|| robots.get(i).getY() < 0 || robots.get(i).getY() >= gameBoard
 							.getHeight())) {
 				robots.get(i).die();
 				if (robots.get(i).hasLost()) {
 					--robotsPlaying;
 					if (isGameOver(i))
 						return true;
 				}
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * Check if a player has won.
 	 * 
 	 * @return true if a robot has won, else false
 	 */
 	private boolean robotHasReachedLastCheckPoint() {
 		for (int i = 0; i < robots.size(); i++) {
 			if (robots.get(i).getLastCheckPoint() == gameBoard
 					.getNbrOfCheckPoints()) {
 				isGameOver = true;
 				addRobotWon(robots.get(i));
 				return isGameOver;
 			}
 		}
 		return isGameOver;
 	}
 
 	/**
 	 * Check if a robot has lost IF a robot has lost.
 	 * 
 	 * @return true if a player has won, else false
 	 */
 	private boolean isGameOver(int robotID) {
 		checkRobotAlone();
 		if (!isGameOver) {
 			addRobotLost(robots.get(robotID));
 		}
 		return isGameOver;
 	}
 
 	/**
 	 * Checks if robot has won because its alone. Sets isGameOver to true if
 	 * game is over.
 	 */
 	private void checkRobotAlone() {
 		if (robotsPlaying == 1) {
 			for (int j = 0; j < robots.size(); j++) {
 				if (!robots.get(j).hasLost()) {
 					isGameOver = true;
 					addRobotWon(robots.get(j));
 					return;
 				}
 			}
 		}
 	}
 
 	/**
 	 * Tries to place a robot one the spawnpoint. If another robot is standing
 	 * on it a tile nearby is used as a spawnpoint.
 	 * 
 	 * @param robot A robot
 	 */
 	private void resetRobotPosition(Robot robot) {
 		int distanceFromSpawnPoint = 0;
 		while (true) {
 			for (int i = robot.getSpawnPointX() - distanceFromSpawnPoint; i <= robot
 					.getSpawnPointX() + distanceFromSpawnPoint; i++) {
 				for (int j = robot.getSpawnPointY() - distanceFromSpawnPoint; j <= robot
 						.getSpawnPointY() + distanceFromSpawnPoint; j++) {
 					boolean tileOccupied = false;
 					for (Robot r : robots) {
 						if ((r.getX() == i && r.getY() == j)) {
 							tileOccupied = true;
 						}
 					}
 					if (!tileOccupied && i >= 0 && i < gameBoard.getWidth()
 							&& j >= 0 && j < gameBoard.getHeight()) {
 						robot.setX(i);
 						robot.setY(j);
 						addRespawnMove(robot);
 						return;
 					}
 				}
 			}
 			distanceFromSpawnPoint++;
 			if (distanceFromSpawnPoint > 2) {
 				break;
 			}
 		}
 	}
 
 	/**
 	 * Add a respawnMove String to allMoves. This method will generate errors
 	 * in the allMove string if called with a bad timing. See separate developer
 	 * document for when to use it.
 	 * @param robot the robot to add the respawnMove for.
 	 */
 	private void addRespawnMove(Robot robot) {
 		allMoves.add("#" + robots.indexOf(robot) + ":" + robot.getDirection()
 				+ robot.getXAsString() + robot.getYAsString());
 	}
 
 	/**
 	 * Add a robotWon String to allMoves. This method will generate errors
 	 * in the allMove string if called with a bad timing. See separate developer
 	 * document for when to use it.
 	 * @param robot the robot to add the robotWon for.
 	 */
 	private void addRobotWon(Robot robot) {
 		allMoves.add(";W#" + robots.indexOf(robot));
 	}
 
 	/**
 	 * Add a robotLost String to allMoves. This method will generate errors
 	 * in the allMove string if called with a bad timing. See separate developer
 	 * document for when to use it.
 	 * @param robot the robot to add the robotLost for.
 	 */
 	private void addRobotLost(Robot robot) {
 		allMoves.add(";L#" + robots.indexOf(robot));
 	}
 
 	/**
 	 * Add a simultaneousMove String to allMoves. This method will generate errors
 	 * in the allMove string if called with a bad timing. See separate developer
 	 * document for when to use it.
 	 * @param robot the robot to add the simultaneousMove for.
 	 */
 	private void addSimultaneousMove(Robot robot) {
 		allMoves.add("#" + robots.indexOf(robot) + ":" + robot.getDirection()
 				+ robot.getXAsString() + robot.getYAsString());
 	}
 
 	/**
 	 * Add a standard move String to allMoves. This method will generate errors
 	 * in the allMove string if called with a bad timing. See separate developer
 	 * document for when to use it.
 	 * @param robot the robot to add the standard move for.
 	 */
 	private void addMove(Robot robot) {
 		allMoves.add(";" + robots.indexOf(robot) + ":" + robot.getDirection()
 				+ robot.getXAsString() + robot.getYAsString());
 	}
 
 	/**
 	 * Add a robotDeadMove String to allMoves. This method will generate errors
 	 * in the allMove string if called with a bad timing. See separate developer
 	 * document for when to use it.
 	 * @param robot the robot to add the robotDeadMove for.
 	 */
 	private void addRobotDeadMove(Robot robot) {
 		allMoves.add(";F#" + robots.indexOf(robot) + ":" + robot.getLife());
 	}
 
 	public GameBoard getGameBoard() {
 		return gameBoard;
 	}
 
 	public Deck getDeck() {
 		return deck;
 	}
 
 	/**
 	 * Return the map as a String. With subStrings representing a tile with it's
 	 * board elements.
 	 * 
 	 * @return the map as a String
 	 */
 	public String getMap() {
 		return gameBoard.getMapAsString();
 	}
 
 	/**
 	 * Returns a list of all robots in the game.
 	 * 
 	 * @return a list of all robots in the game.
 	 */
 	public List<Robot> getRobots() {
 		return robots;
 	}
 
 	/**
 	 * Return a String containing all moves during a round.
 	 * 
 	 * @return a String containing all moves during a round.
 	 */
 	public String getAllMoves() {
 		StringBuilder sb = new StringBuilder();
 		for (String string : allMoves) {
 			sb.append(string);
 		}
 		// The first character will be a "split character" i.e. ; or #
 		String returnString = sb.substring(1);
 		return returnString;
 	}
 
 	/**
 	 * Return the amount of robots still in the game. I.e. robots who have not
 	 * lost all their lives.
 	 * 
 	 * @return the amount of robots still in the game.
 	 */
 	public int getRobotsPlaying() {
 		return robotsPlaying;
 	}
 
 	public boolean isGameOver() {
 		return isGameOver;
 	}
 
 }
