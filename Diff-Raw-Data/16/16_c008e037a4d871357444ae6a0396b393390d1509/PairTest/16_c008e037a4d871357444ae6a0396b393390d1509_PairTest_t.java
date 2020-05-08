 package main;
 
import static org.junit.Assert.*;

 import org.junit.Test;
 
 public class PairTest {
 	
 	@Test
 	public void testGetEligible() {
 		DeveloperHome.add(new Developer("Nathan Ferracini"));
 		DeveloperHome.add(new Developer("Michel Menegazzo"));
 		PositionHome.add(new Position("1"));
 		PositionSort.setUp();
 		
		Pair pair = DeveloperHome.searchDeveloperBy(PositionHome.searchPosition("1"));
		
		assertEquals(DeveloperHome.searchDeveloperBy("Michel Menegazzo"), pair.getEligibleToMove() );
		
 	}
 
 }
