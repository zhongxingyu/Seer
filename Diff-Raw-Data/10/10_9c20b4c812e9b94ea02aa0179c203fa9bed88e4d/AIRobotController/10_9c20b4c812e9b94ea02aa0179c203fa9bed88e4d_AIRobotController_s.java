 package se.chalmers.dryleafsoftware.androidrally.controller;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.Random;
 
 import se.chalmers.dryleafsoftware.androidrally.model.cards.Card;
 import se.chalmers.dryleafsoftware.androidrally.model.gameBoard.BoardElement;
 import se.chalmers.dryleafsoftware.androidrally.model.cards.Move;
 import se.chalmers.dryleafsoftware.androidrally.model.cards.Turn;
 import se.chalmers.dryleafsoftware.androidrally.model.cards.TurnType;
 import se.chalmers.dryleafsoftware.androidrally.model.gameBoard.GameBoard;
 import se.chalmers.dryleafsoftware.androidrally.model.gameBoard.Hole;
 import se.chalmers.dryleafsoftware.androidrally.model.robots.Robot;
 
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
 
 	public AIRobotController(GameBoard gb) {
 		this.gb = gb;
 	}
 
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
 		
 		x = robot.getX();
 		y = robot.getY();
 		direction = robot.getDirection();
 		nextCheckpoint = nextCheckPoint(robot);
 		placeCards();
 		robot.setChosenCards(chosenCards);
 //		for(int i = 0; i < cards.size(); i++){
 //			System.out.println("Card nr: " + i + " " + cards.get(i).getPriority());
 //		}
 //		for(int i = 0; i < chosenCards.size(); i++){
 //			System.out.println("ChosenCard nr: " + i + " " + chosenCards.get(i).getPriority());
 //			System.out.println("ChosenCard nr: " + i + " " + robot.getChosenCards()[i].getPriority());
 //		}
 	}
 
 	private void placeCards() {
 //		System.out.println("placeCArd");
 //		System.out.println("placeCard " + chosenCards.size());
 		if (chosenCards.size() == 5) {
 			return;
 		}
 		if(chosenCards.size() != 0){
 			changeCalculatedPosition(chosenCards.get(chosenCards.size()-1));
 		}
 		
 		boolean isRightDirection = false;
 		for (Integer direction : getDirections()) { // Check if the robot stand in a correct direction
 			if (this.direction == direction) {
 				isRightDirection = true;
 			}
 		}
 		if (isRightDirection) {
 			
 			if (moveForwardCards.size() != 0) { // Move forward as long as possible
				chosenCards.add(moveForwardCards.get(0));
				removeCardFromLists(moveForwardCards.get(0));
 				if(direction == GameBoard.NORTH || direction == GameBoard.SOUTH){
					addMoveCard(getDY());
 					System.out.println("north/south");
 				}else{
					addMoveCard(getDX());
 					System.out.println("west/east");
 				}
 			}  else {  //check if there are other good combinations of cards
 				if(chosenCards.size() <= 1 && moveBackwardCards.size() >= 2 && uTurnCards.size() >= 2) {
 					chosenCards.add(uTurnCards.get(0)); //turn around, walk backwards 2 steps and turn around again
 					removeCardFromLists(uTurnCards.get(0));
 					chosenCards.add(moveBackwardCards.get(0));
 					removeCardFromLists(moveBackwardCards.get(0));
 					chosenCards.add(moveBackwardCards.get(0));
 					removeCardFromLists(moveBackwardCards.get(0));
 					chosenCards.add(uTurnCards.get(0));
 					removeCardFromLists(uTurnCards.get(0));
 				} else if (chosenCards.size() <= 2 && moveBackwardCards.size() >= 1 && uTurnCards.size() >= 2) {
 					chosenCards.add(uTurnCards.get(0)); //turn around, walk backwards 1 step and turn around again
 					removeCardFromLists(uTurnCards.get(0));
 					chosenCards.add(moveBackwardCards.get(0));
 					removeCardFromLists(moveBackwardCards.get(0));
 					chosenCards.add(uTurnCards.get(0));
 					removeCardFromLists(uTurnCards.get(0));
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
 							chosenCards.add(leftTurnCards.get(0));
 							removeCardFromLists(leftTurnCards.get(0));
 							cardAdded = true;
 							break;
 						}
 					}else if(turnDifference == 2){
 						if(uTurnCards.size() != 0){
 							chosenCards.add(uTurnCards.get(0));
 							removeCardFromLists(uTurnCards.get(0));
 							cardAdded = true;
 							break;
 						}
 					}else if(turnDifference == 3){
 						if(rightTurnCards.size() != 0){
 							chosenCards.add(rightTurnCards.get(0));
 							removeCardFromLists(rightTurnCards.get(0));
 							cardAdded = true;
 							break;
 						}
 					}
 				}
 				if(!cardAdded){
 					for(int j = 0; j<cards.size(); j++){
 						if(!(cards.get(j) instanceof Move)){
 							chosenCards.add(cards.get(j));
 							removeCardFromLists(cards.get(j));
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
 
 	private int[] nextCheckPoint(Robot robot) {
 		int[] xy = new int[2];
 		for(int i = 0; i <= 1; i++) {
 			xy[i] = gb.getCheckPoints().get(robot.getLastCheckPoint()+1)[i]; //TODO will crash game if game is over and continues
 		}
 		return xy;
 	}
 
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
 
 
 		if(directions.size() == 2){
 			if(Math.abs(dx) < Math.abs(dy)){
 				directions.add(directions.remove(0));
 			}
 		}
 		return directions;
 	}
 
 	private void removeDirection(List<Integer> directions, Integer indexToRemove){
 		if(directions.size() == 1){
 			directions.add(Integer.valueOf((directions.get(indexToRemove) + 1) % 4));
 			directions.add(Integer.valueOf((directions.get(indexToRemove) + 3) % 4));
 		}
 		directions.remove(indexToRemove);
 	}
 
 	private List<Integer> removeBadDirections(List<Integer> directions){
 		if(x<0 || x>(gb.getWidth()-1) || y<0 || y>(gb.getHeight()-1)){
 			return directions;
 		}
 		for(int i = 0; i < directions.size(); i++){
 			if(directions.get(i).intValue() == GameBoard.NORTH){
 				for(int j = 0; j < 3; j++){
 					if((y-j) >= 0){
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
 				for(int j = 0; j < 3; j++){
 					if((x+j) <= gb.getWidth()){
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
 				for(int j = 0; j < 3; j++){
 					if((y+j) <= gb.getHeight()){
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
 				for(int j = 0; j < 3; j++){
 					if((x-j) >= 0){
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
 		return directions;
 	}
 
 	private void addMoveCard(int distance){
 		for(Move card : moveForwardCards){// the list is sorted with longest distance first.
 			if(card.getDistance() <= distance){
 				chosenCards.add(card);
 				removeCardFromLists(card);
 				return;
 			}
 		}
 		randomizeCard();
 		System.out.println("randomCards");
 	}
 	
 	private int getDX(){
 		return (x - nextCheckpoint[0]);
 	}
 
 	private int getDY(){
 		return (y - nextCheckpoint[1]);
 	}
 
 	//Removes a card from cards. It removes it from moveForwardCards and so on if they are found there
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
 
 	private void randomizeCard() {
 		if(addCardFromList(leftTurnCards)){
 		}else if(addCardFromList(rightTurnCards)){
 		}else if(addCardFromList(uTurnCards)){
 		}else{
 			addCardFromList(cards);
 		}
 	}
 	
 	private boolean addCardFromList(List<Card> cards){
 		for(Card card : cards){
 			chosenCards.add(card);
 			removeCardFromLists(card);
 			return true;
 		}
 		return false;//empty list
 	}
 }
