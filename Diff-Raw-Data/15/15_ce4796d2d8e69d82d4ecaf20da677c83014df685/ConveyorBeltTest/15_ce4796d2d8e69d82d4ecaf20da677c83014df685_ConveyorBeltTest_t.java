 package se.chalmers.dryleafsoftware.androidrally.model.gameBoard;
 
 import static org.junit.Assert.assertTrue;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.junit.Test;
 
 import se.chalmers.dryleafsoftware.androidrally.model.cards.Card;
 import se.chalmers.dryleafsoftware.androidrally.model.cards.Turn;
 import se.chalmers.dryleafsoftware.androidrally.model.cards.TurnType;
 import se.chalmers.dryleafsoftware.androidrally.model.gameModel.GameModel;
 import se.chalmers.dryleafsoftware.androidrally.model.robots.Robot;
 
 public class ConveyorBeltTest {
 
 	@Test
 	public void testMoveOnConveyorBeltsInDifferentDirections() {
 		String testMap =
 				"yx"									+ "x1" + String.valueOf(GameBoard.SOUTH) + "3x1" + String.valueOf(GameBoard.EAST) + "3" +
 				"yx1" + String.valueOf(GameBoard.SOUTH)+ "3x1" + String.valueOf(GameBoard.WEST)  + "3x1" + String.valueOf(GameBoard.EAST) + "3" +
 				"yx1" + String.valueOf(GameBoard.WEST) + "3x1" + String.valueOf(GameBoard.NORTH) + "3x1" + String.valueOf(GameBoard.NORTH)+ "3";
 		
 		GameModel gm = new GameModel(2, testMap);
 		
 		List<Card> cardList1 = new ArrayList<Card>();
 		List<Card> cardList2 = new ArrayList<Card>();
 		
 		for(int i = 0; i < 5; i++) {
 			cardList1.add(new Turn(i+1,TurnType.LEFT));
 			cardList2.add(new Turn(i+10,TurnType.RIGHT));
 		}
 		
 		gm.getRobots().set(0, new Robot(1,2));
 		gm.getRobots().set(1, new Robot(1,1));
 		
 		gm.getRobots().get(0).addCards(cardList1);
 		gm.getRobots().get(1).addCards(cardList2);
 		
 		gm.getRobots().get(0).setChosenCards(gm.getRobots().get(0).getCards());
 		gm.getRobots().get(1).setChosenCards(gm.getRobots().get(1).getCards());
 	
 		
 		assertTrue(gm.getRobots().get(0).getX() == 1);
 		assertTrue(gm.getRobots().get(0).getY() == 2);
 		assertTrue(gm.getRobots().get(1).getX() == 1);
 		assertTrue(gm.getRobots().get(1).getY() == 1);
 		gm.moveRobots();
		System.out.println("x0" + gm.getRobots().get(0).getX());
		System.out.println("y0" + gm.getRobots().get(0).getY());
 		assertTrue(gm.getRobots().get(0).getX() == 1);
 		assertTrue(gm.getRobots().get(0).getY() == 1);
 		assertTrue(gm.getRobots().get(1).getX() == 2);
 		assertTrue(gm.getRobots().get(1).getY() == 1);
 		assertTrue(gm.getRobots().get(0).getDirection() == GameBoard.WEST);
 		assertTrue(gm.getRobots().get(1).getDirection() == GameBoard.EAST);
 	}
 	
 	@Test
 	public void testActionWhenConvetyorBeltsAreTowardsEachOtherWithAGapBetween() {
 		String testMap =
 				"yx"						 + "x1" + String.valueOf(GameBoard.EAST)+"3x" +
 				"yx1"+String.valueOf(GameBoard.SOUTH)+"3" +		"x" + 				  "x1"+String.valueOf(GameBoard.NORTH)+"3" +
 				"yx" + 						   "x1" + String.valueOf(GameBoard.WEST)+"3x";
 		
 		GameModel gm = new GameModel(2, testMap);
 		
 		Card left = new Turn(80,TurnType.LEFT);
 		Card right = new Turn(90,TurnType.RIGHT);
 		
 		List<Card> cardList1 = new ArrayList<Card>();
 		List<Card> cardList2 = new ArrayList<Card>();
 		
 		for(int i = 0; i < 5; i++) {
 			cardList1.add(left);
 			cardList2.add(right);
 		}
 		
 		gm.getRobots().set(0, new Robot(0,1));
 		gm.getRobots().set(1, new Robot(2,1));
 		
 		gm.getRobots().get(0).addCards(cardList1);
 		gm.getRobots().get(1).addCards(cardList2);
 		
 		gm.getRobots().get(0).setChosenCards(gm.getRobots().get(0).getCards());
 		gm.getRobots().get(1).setChosenCards(gm.getRobots().get(1).getCards());
 		
 		
 		assertTrue(gm.getRobots().get(0).getX() == 0);
 		assertTrue(gm.getRobots().get(0).getY() == 1);
 		assertTrue(gm.getRobots().get(1).getX() == 2);
 		assertTrue(gm.getRobots().get(1).getY() == 1);
 		
 		gm.moveRobots();
 		
 		assertTrue(gm.getRobots().get(0).getX() == 0);
 		assertTrue(gm.getRobots().get(0).getY() == 1);
 		assertTrue(gm.getRobots().get(1).getX() == 2);
 		assertTrue(gm.getRobots().get(1).getY() == 1);
 	}
 }
