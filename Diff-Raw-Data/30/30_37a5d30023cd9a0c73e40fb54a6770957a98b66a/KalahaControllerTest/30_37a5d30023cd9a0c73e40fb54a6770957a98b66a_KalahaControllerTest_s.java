 package de.htwg.kalaha.controller;
 
 import junit.framework.TestCase;
 
 public class KalahaControllerTest extends TestCase {
     KalahaController controller;
     
 	public void setUp() {
 		controller = new KalahaController();
 	}
 	
     public void testPrepareNewGame() {
     	controller.prepareNewGame();
     	assertEquals(controller.getStatus(),"New game started");
     }
     
     public void testGetBoardString() {
     	assertNotNull(controller.getBoardString());
     }
     
     public void testTakeMarbles() {
     	controller.takeMarbles(1, 1);
     	assertEquals(controller.getStatus(), "The hollow p1,1 is now empty");
     	controller.takeMarbles(1, 1);
     	assertEquals(controller.getStatus(), "The hollow p1,1 is already empty");
     	controller.takeMarbles(1, 2);
     	assertEquals(controller.getStatus(), "The hollow p1,2 is now empty");
     	controller.takeMarbles(1, 6);
     	assertEquals(controller.getStatus(), "The hollow p1,6 is now empty");    	
     	controller.takeMarbles(2, 6);
    	assertEquals(controller.getStatus(), "The hollow p2,6 is already empty");  
     	
     	
     	controller.takeMarbles(2, 1);
     	assertEquals(controller.getStatus(), "The hollow p2,1 is now empty");
     	
     }
 	
 	
 }
