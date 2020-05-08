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
 
 	public AIRobotController(GameBoard gb) {
 		this.gb = gb;
 	}
 
 	public void makeMove(Robot robot) {
 		List<Card> cards = new ArrayList<Card>();
 		chosenCards = new ArrayList<Card>();
 		cards.addAll(robot.getCards());
 		x = robot.getX();
 		y = robot.getY();
 		direction = robot.getDirection();
 		nextCheckpoint = nextCheckPoint(robot);
 		placeCards(cards);
 		robot.setChosenCards(chosenCards);
 		for(int i = 0; i < cards.size(); i++){
 			System.out.println("Card nr: " + i + " " + cards.get(i).getPriority());
 		}
 		for(int i = 0; i < chosenCards.size(); i++){
 			System.out.println("ChosenCard nr: " + i + " " + chosenCards.get(i).getPriority());
 			System.out.println("ChosenCard nr: " + i + " " + robot.getChosenCards()[i].getPriority());
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	private void placeCards(List<Card> cards) {
 		if (chosenCards.size() == 5) {
 			return;
 		}
 		if(chosenCards.size() != 0){
 			changeCalculatedPosition(chosenCards.get(chosenCards.size()-1));
 		}
 		List<Move> moveForwardCards = new ArrayList<Move>();
 		List<Turn> turnCards = new ArrayList<Turn>();
 		List<Turn> leftTurnCards = new ArrayList<Turn>();
 		List<Turn> rightTurnCards = new ArrayList<Turn>();
 		List<Turn> uTurnCards = new ArrayList<Turn>();
 
		boolean isRightDirection = false;
		for (Integer direction : getDirections()) { // Check if the robot stand in a correct direction
			if (this.direction == direction) {
				isRightDirection = true;
			}
		}
 		for (Card card : cards) {
 			if (card instanceof Move) {
 				if (((Move)card).getDistance() > 0) {
 					moveForwardCards.add((Move)card);
 				}
 			}
 		}
 		Collections.sort(moveForwardCards);
 		if (isRightDirection) {
 			
 			if (moveForwardCards.size() != 0) { // Move forward as long as possible
 				chosenCards.add(moveForwardCards.get(0));
 				cards.remove(moveForwardCards.get(0));
 			} else { // if there are no move forwards cards -> random card 
 				randomizeCard(cards);
 			}
 			placeCards(cards);
 			return;
 		}
 		else { // If the robot is turned towards a wrong direction.
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
 			if (turnCards.size() != 0) { // Try turn towards a correct direction
 				for(Integer i : getDirections()){
 					boolean cardAdded = false;
 					int turnDifference = Math.abs(i.intValue() - 
 							direction);
 					if(turnDifference == 1){
 						if(leftTurnCards.size() != 0){
 							chosenCards.add(leftTurnCards.get(0));
 							cards.remove(leftTurnCards.get(0));
 							cardAdded = true;
 						}
 					}else if(turnDifference == 2){
 						if(uTurnCards.size() != 0){
 							chosenCards.add(uTurnCards.get(0));
 							cards.remove(uTurnCards.get(0));
 							cardAdded = true;
 						}
 					}else if(turnDifference == 3){
 						if(rightTurnCards.size() != 0){
 							chosenCards.add(rightTurnCards.get(0));
 							cards.remove(rightTurnCards.get(0));
 							cardAdded = true;
 						}
 					}
 					if(!cardAdded){
 						for(Card card : cards){
 							if(!(card instanceof Move)){
 								chosenCards.add(card);
 								cards.remove(card);
 							}
 						}
 					}
 					placeCards(cards);
 					return;
 				}
 				
 			} else { // No turn cards -> random card
 				randomizeCard(cards);
 			}
 			placeCards(cards);
 			return;
 		}
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
 						System.out.println("NORTH x: " + x + " Y: " + y + " J: " + j);
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
 						System.out.println("EAST x: " + x + " Y: " + y +" J: " + j);
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
 						System.out.println("SOUTH x: " + x + " Y: " + y + " J: " + j);
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
 						System.out.println("WEST x: " + x + " Y: " + y + " J: " + j);
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
 
 	private int getDX(){
 		return (x - nextCheckpoint[0]);
 	}
 
 	private int getDY(){
 		return (y - nextCheckpoint[1]);
 	}
 
 
 	private void randomizeCard(List<Card> cards) {
 		Random rand = new Random();
 		int index = rand.nextInt(cards.size());
 		Card randChosenCard = cards.get(index);
 		chosenCards.add(randChosenCard);
 		cards.remove(index);
 	}
 
 }
