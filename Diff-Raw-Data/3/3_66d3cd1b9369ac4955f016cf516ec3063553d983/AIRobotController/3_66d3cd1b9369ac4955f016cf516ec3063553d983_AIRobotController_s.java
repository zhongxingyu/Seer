 package se.chalmers.dryleafsoftware.androidrally.controller;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import se.chalmers.dryleafsoftware.androidrally.model.cards.Card;
 import se.chalmers.dryleafsoftware.androidrally.model.gameBoard.BoardElement;
 import se.chalmers.dryleafsoftware.androidrally.model.cards.Move;
 import se.chalmers.dryleafsoftware.androidrally.model.cards.Turn;
 import se.chalmers.dryleafsoftware.androidrally.model.cards.TurnType;
 import se.chalmers.dryleafsoftware.androidrally.model.gameBoard.GameBoard;
 import se.chalmers.dryleafsoftware.androidrally.model.gameBoard.Hole;
 import se.chalmers.dryleafsoftware.androidrally.model.robots.Robot;
 
 /**
  * This is a simple AI class which can choose cards for robots.
  * <p>
  * This class will not be able to guide robots through maps which is to tricky. The only
  * boardelements which this class will take into account is Holes and walls which it will
  * attempt to run around, other boardelements such as conveyor belt will not be considered.
  * 
  * 
  * @author
  *
  */
 public class AIRobotController {
 	private GameBoard gb;
 	private List<Card> chosenCards;
 	private int x;
 	private int y;
 	private int direction;
 	private int[] nextCheckpoint;
 	private List<Card> cards;
 	private List<Move> moveForwardCards;
 	private List<Move> moveBackwardCards;
 	private List<Card> leftTurnCards;
 	private List<Card> rightTurnCards;
 	private List<Card> uTurnCards;
 
 	/**
 	 * Creates a new AI which will make moves according to the gameboard provided.
 	 * @param gb
 	 */
 	public AIRobotController(GameBoard gb) {
 		this.gb = gb;
 	}
 
 	/**
 	 * Cards will be chosen for the robot provided. Cards will be chosen with the
 	 * goal of reaching the next checkpoint for the robot.
 	 * @param robot the robot to choose cards for.
 	 */
 	@SuppressWarnings("unchecked")
 	public void makeMove(Robot robot) {
 		cards = new ArrayList<Card>();
 		moveForwardCards = new ArrayList<Move>();
 		moveBackwardCards = new ArrayList<Move>();
 		leftTurnCards = new ArrayList<Card>();
 		rightTurnCards = new ArrayList<Card>();
 		uTurnCards = new ArrayList<Card>();
 		chosenCards = new ArrayList<Card>();
 		
 		cards.addAll(robot.getCards());
 		
 		//Put the cards of the robot in the correct lists
 		for (Card card : cards) {
 			if (card instanceof Turn) {
 				if(((Turn)card).getTurn() == TurnType.LEFT){
 					leftTurnCards.add((Turn)card);
 				}else if(((Turn)card).getTurn() == TurnType.RIGHT){
 					rightTurnCards.add((Turn)card);
 				}else {
 					uTurnCards.add((Turn)card);
 				}
 			}
 		}
 		for (Card card : cards) {
 			if (card instanceof Move) {
 				if (((Move)card).getDistance() > 0) {
 					moveForwardCards.add((Move)card);
 				} else {
 					moveBackwardCards.add((Move)card);
 				}
 			}
 		}
 		Collections.sort(moveForwardCards);
 		
 		// These values will be needed through the class.
 		x = robot.getX();
 		y = robot.getY();
 		direction = robot.getDirection();
 		nextCheckpoint = nextCheckPoint(robot);
 		placeCards();
 		robot.setChosenCards(chosenCards);
 	}
 
 	/**
 	 * This method will choose the cards for the robot. In most cases it will choose one 
 	 * card and then call itself.
 	 */
 	private void placeCards() {
 		if (chosenCards.size() == 5) {
 			return;
 		}
		if(chosenCards.size() != 0){// TODO when several cards is chosen during on placeCard this doesnt work
			changeCalculatedPosition(chosenCards.get(chosenCards.size()-1));
		}
 		
 		boolean isRightDirection = false;
 		for (Integer direction : getDirections()) { // Check if the robot stand in a correct direction
 			if (this.direction == direction) {
 				isRightDirection = true;
 			}
 		}
 		if (isRightDirection) {
 			if (moveForwardCards.size() != 0) { // Move forward with a fitting distance
 				if(direction == GameBoard.NORTH || direction == GameBoard.SOUTH){
 					addMoveCard(Math.abs(getDY()));
 				}else{
 					addMoveCard(Math.abs(getDX()));
 				}
 			}  else {  //check if there are other good combinations of cards
 				if(chosenCards.size() <= 1 && moveBackwardCards.size() >= 2 && uTurnCards.size() >= 2) {
 					//turn around, walk backwards 2 steps and turn around again
 					addChosenCard(uTurnCards.get(0));
 					addChosenCard(moveBackwardCards.get(0));
 					addChosenCard(moveBackwardCards.get(0));
 					addChosenCard(uTurnCards.get(0));
 				} else if (chosenCards.size() <= 2 && moveBackwardCards.size() >= 1 && uTurnCards.size() >= 2) {
 					//turn around, walk backwards 1 step and turn around again
 					addChosenCard(uTurnCards.get(0));
 					addChosenCard(moveBackwardCards.get(0));
 					addChosenCard(uTurnCards.get(0));
 				} else {// if there are none -> random card
 					randomizeCard(); //maybe randomize between turn-cards? TODO
 				}
 			}
 		} else { // If the robot is turned towards a wrong direction.
 			if (rightTurnCards.size() != 0 || leftTurnCards.size() != 0 || uTurnCards.size() != 0) { // Try turn towards a correct direction
 				boolean cardAdded = false;
 				for(Integer i : getDirections()){
 					int turnDifference = Math.abs(i.intValue() - 
 							direction);
 					if(turnDifference == 1){
 						if(leftTurnCards.size() != 0){
 							addChosenCard(leftTurnCards.get(0));
 							cardAdded = true;
 							break;
 						}
 					}else if(turnDifference == 2){
 						if(uTurnCards.size() != 0){
 							addChosenCard(uTurnCards.get(0));
 							cardAdded = true;
 							break;
 						}
 					}else if(turnDifference == 3){
 						if(rightTurnCards.size() != 0){
 							addChosenCard(rightTurnCards.get(0));
 							cardAdded = true;
 							break;
 						}
 					}
 				}
 				if(!cardAdded){
 					for(int j = 0; j<cards.size(); j++){
 						if(!(cards.get(j) instanceof Move)){
 							addChosenCard(cards.get(j));
 							cardAdded = true;
 							break;
 						}
 					}
 					if(!cardAdded){
 						randomizeCard();
 					}
 				}
 			} else { // No turn cards -> random card
 				randomizeCard();
 			}
 		}
 		placeCards();
 	}
 	
 	/**
 	 * Add a card to chosenCards and changes instance variables accordingly.
 	 * @param card
 	 */
 	private void addChosenCard(Card card){
 		chosenCards.add(card);
 		removeCardFromLists(card);
 		changeCalculatedPosition(card);
 	}
 	
 	/**
 	 * After one card is chosen this method can be called to update the instance variables
 	 * which will make it possible to choose a good next card.
 	 * @param card the card which will change the calculated position.
 	 */
 	private void changeCalculatedPosition(Card card){
 		if(card instanceof Move){
 			if(direction == GameBoard.NORTH){
 				y = y - ((Move)card).getDistance();
 			}else if(direction == GameBoard.EAST){
 				x = x + ((Move)card).getDistance();
 			}else if(direction == GameBoard.SOUTH){
 				y = y + ((Move)card).getDistance();
 			}else if(direction == GameBoard.WEST){
 				x = x - ((Move)card).getDistance();
 			}
 		}else if(card instanceof Turn){
 			if(((Turn)card).getTurn() == TurnType.LEFT){
 				direction = (direction + 3) % 4;
 			}else if(((Turn)card).getTurn() == TurnType.RIGHT){
 				direction = (direction + 1) % 4;
 			}else{ // UTurn
 				direction = (direction + 2) % 4;
 			}
 		}
 	}
 
 	/**
 	 * Returns the next checkpoint for the robot.
 	 * @param robot the robot to find the next checkpoint for.
 	 * @return the next checkpoint for the robot in the form [posX][posY].
 	 */
 	private int[] nextCheckPoint(Robot robot) {
 		int[] xy = new int[2];
 		for(int i = 0; i <= 1; i++) {
 			xy[i] = gb.getCheckPoints().get(robot.getLastCheckPoint()+1)[i];
 		}
 		return xy;
 	}
 
 	/**
 	 * Will return a list of good directions for the robot to walk. The list
 	 * will contain directions which would move the robot closer to the next
 	 * checkpoint. If such a direction would lead the robot towards a hole, a wall
 	 * or outside the map the direction will be removed. In case the last direction
 	 * was removed two perpendicular directions will be added.
 	 * @return a list of good directions for the robot to walk.
 	 */
 	private List<Integer> getDirections(){
 		List<Integer> directions = new ArrayList<Integer>();
 		int dx = getDX();
 		int dy = getDY();
 		if(dx < 0){
 			directions.add(Integer.valueOf(GameBoard.EAST));
 		}else if(dx > 0){
 			directions.add(Integer.valueOf(GameBoard.WEST));
 		}
 		if(dy < 0){
 			directions.add(Integer.valueOf(GameBoard.SOUTH));
 		}else if(dy > 0){
 			directions.add(Integer.valueOf(GameBoard.NORTH));
 		}
 		removeBadDirections(directions);
 
 		// The direction which distance towards the checkpoint is
 		// longest should be first in the list.
 		if(directions.size() == 2){
 			if(Math.abs(dx) < Math.abs(dy)){
 				directions.add(directions.remove(0));
 			}
 		}
 		return directions;
 	}
 
 	/**
 	 * Removes a direction from a list. If the direction removed is the last, two
 	 * perpendicular directions will be added.
 	 * @param directions the list to remove a direction from.
 	 * @param indexToRemove the index of the direction to remove.
 	 */
 	private void removeDirection(List<Integer> directions, int indexToRemove){
 		if(directions.size() == 1){
 			directions.add(((indexToRemove + 1) % 4));
 			directions.add(((indexToRemove + 3) % 4));
 		}
 		directions.remove(indexToRemove);
 	}
 
 	/**
 	 * Will remove directions which will not be good for the robot. If the robot
 	 * have a hole, wall or the map ends within <code>Math.min(3, Math.abs(getDY()))</code>
 	 * the direction will be removed.
 	 * @param directions the list to check for badDirections.
 	 */
 	private void removeBadDirections(List<Integer> directions){
 		if(x<0 || x>(gb.getWidth()-1) || y<0 || y>(gb.getHeight()-1)){
 			return;
 		}
 		for(int i = 0; i < directions.size(); i++){
 			if(directions.get(i).intValue() == GameBoard.NORTH){
 				for(int j = 0; j < Math.min(3, Math.abs(getDY())); j++){
 					if((y-j) >= 0 && !gb.getTile(x, y-j).getNorthWall()){
 						if (gb.getTile(x, y-j).getBoardElements() != null) {
 							for(BoardElement boardElement : gb.getTile(x, (y-j)).getBoardElements()){
 								if(boardElement instanceof Hole){
 									removeDirection(directions, i);
 									break;
 								}
 							}
 						}
 					}else{
 						removeDirection(directions, i);
 						break;
 					}
 				}
 			}else if(directions.get(i).intValue() == GameBoard.EAST){
 				for(int j = 0; j < Math.min(3, Math.abs(getDX())); j++){
 					if((x+j) < gb.getWidth() && !gb.getTile(x+j, y).getEastWall()){
 						if (gb.getTile(x+j, y).getBoardElements() != null) {
 							for(BoardElement boardElement : gb.getTile((x+j), y).getBoardElements()){
 								if(boardElement instanceof Hole){
 									removeDirection(directions, i);
 									break;
 								}
 							}
 						}
 					}else{
 						removeDirection(directions, i);
 						break;
 					}
 				}
 			}else if(directions.get(i).intValue() == GameBoard.SOUTH){
 				for(int j = 0; j < Math.min(3, Math.abs(getDY())); j++){
 					if((y+j) < gb.getHeight() && !gb.getTile(x, y+j).getSouthWall()){
 						if (gb.getTile(x, y+j).getBoardElements() != null) {
 							for(BoardElement boardElement : gb.getTile(x, (y+j)).getBoardElements()){
 								if(boardElement instanceof Hole){
 									removeDirection(directions, i);
 									break;
 								}
 							}
 						}
 					}else{
 						removeDirection(directions, i);
 						break;
 					}
 				}
 			}else if(directions.get(i).intValue() == GameBoard.WEST){
 				for(int j = 0; j < Math.min(3, Math.abs(getDX())); j++){
 					if((x-j) >= 0 && !gb.getTile(x-j, y).getWestWall()){
 						if (gb.getTile(x-j, y).getBoardElements() != null) {
 							for(BoardElement boardElement : gb.getTile((x-j), y).getBoardElements()){
 								if(boardElement instanceof Hole){
 									removeDirection(directions, i);
 									break;
 								}
 							}
 						}
 					}else{
 						removeDirection(directions, i);
 						break;
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * Adds a move card to chosenCards. The move card chosen will be the move card
 	 * biggest move distance <= distance.
 	 * @param distance the distance the robot should try to move.
 	 */
 	private void addMoveCard(int distance){
 		for(Move card : moveForwardCards){// the list is sorted with longest distance first.
 			if(card.getDistance() <= distance){
 				addChosenCard(card);
 				return;
 			}
 		}
 		randomizeCard();
 	}
 	
 	/**
 	 * The difference in the x-axis between the next checkpoint and the
 	 * robots calculated position.
 	 * @return The difference in the x-axis between the next checkpoint and the
 	 * robots calculated position.
 	 */
 	private int getDX(){
 		return (x - nextCheckpoint[0]);
 	}
 
 	/**
 	 * The difference in the y-axis between the next checkpoint and the
 	 * robots calculated position.
 	 * @return The difference in the y-axis between the next checkpoint and the
 	 * robots calculated position.
 	 */
 	private int getDY(){
 		return (y - nextCheckpoint[1]);
 	}
 
 	/**
 	 * Removes a card from all lists in this class except chosenCards.
 	 * @param card the card to be removed.
 	 */
 	private void removeCardFromLists(Card card) {
 		cards.remove(card);
 		if (card instanceof Move) {
 			if (((Move)card).getDistance() > 0) {
 				moveForwardCards.remove(card);
 			} else {
 				moveBackwardCards.remove(card);
 			}
 		} else if (card instanceof Turn) {
 			if (((Turn) card).getTurn() == TurnType.LEFT) {
 				leftTurnCards.remove(card);
 			} else if (((Turn) card).getTurn() == TurnType.UTURN) {
 				uTurnCards.remove(card);
 			} else if (((Turn) card).getTurn() == TurnType.RIGHT) {
 				rightTurnCards.remove(card);
 			}
 		}
 	}
 
 	/**
 	 * Will choose a "random" card. Will try to add cards in the order turnLeft, turnRight, uTurn,
 	 * any other card.
 	 */
 	private void randomizeCard() {
 		if(addCardFromList(leftTurnCards)){
 		}else if(addCardFromList(rightTurnCards)){
 		}else if(addCardFromList(uTurnCards)){
 		}else{
 			addCardFromList(cards);
 		}
 	}
 	
 	/**
 	 * Will add the first card from a list to chosenCards.
 	 * @param cards the list to add a card from.
 	 * @return true if a card was found in the list, false otherwise.
 	 */
 	private boolean addCardFromList(List<Card> cards){
 		for(Card card : cards){
 			addChosenCard(card);
 			return true;
 		}
 		return false;//empty list
 	}
 }
