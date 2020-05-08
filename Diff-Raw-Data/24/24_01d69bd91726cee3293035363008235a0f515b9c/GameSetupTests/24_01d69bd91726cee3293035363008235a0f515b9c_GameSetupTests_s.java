 package tests;
 
 import static org.junit.Assert.*;
 
 import java.awt.Color;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.Computer;
 
 import clueGame.Board;
 import clueGame.ComputerPlayer;
 import clueGame.Player;
 import clueGame.WalkwayCell;
 
 public class GameSetupTests {
	Board board;
	Map<String,Color> names;
	@Before //TODO BeforeClass?
 	public void setUp() throws Exception {
		names = new HashMap<String,Color>(6);
 		board = new Board();
 		names.put("Colonel Mustard",Color.yellow);
 		names.put("Professor Plum",Color.decode("FF00FF"));
 		names.put("Miss White",Color.white);
 		names.put("Mrs. Peacock",Color.blue);
 		names.put("Mr. Green",Color.green);
 		names.put("Miss Scarlet",Color.red);
 	}
 
 	@Test
 	public void peopleLoaded() {
 		Set<Player> opp = board.getPlayers();
		assertEquals(5, opp.size());
 		if(names.remove(board.getHuman().getName())==null) fail("Your name is invalid");
 		for(Player comp : opp){
 			assertTrue(comp.getName()/*message*/,names.containsKey(comp.getName()));
 			//assure all the computer players' names are in the list.
 			assertTrue("Not in walkway",board.getCellAt(comp.getCellIndex()) instanceof WalkwayCell);
 			
 		}
 		
 	}
 
 }
