 package assignments.monopoly.tests;
 
 import static org.junit.Assert.*;
 
 import org.junit.Test;
 
 import assignments.monopoly.Die;
 
 public class DieTests {
 
 	@Test
	public void ValueShouldBeBetweenOneAndSix() {
 		Die d = new Die();
 		for (int i = 0; i < 1000; i++) {
 			int value = d.roll();
 			assertTrue(value > 0 && value < 7);
 		}
 	}
 }
