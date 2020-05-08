 import org.junit.Test;
 
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 
 public class StandardRuleTest {
 
 	@Test
 	public void testCellUnderpopulated() {
 		Rule r = new StandardRule();
 		assertFalse(r.calculate(true, 1));
 	}
 
 	@Test
 	public void testKeepsLiving() {
 		Rule r = new StandardRule();
		assertFalse(r.calculate(true, 2));
 	}
 
 	@Test
 	public void testCellOverpopulated() {
 		Rule r = new StandardRule();
 		assertFalse(r.calculate(true, 4));
 	}
 
 	@Test
 	public void cellSpawned() {
 		Rule r = new StandardRule();
 		assertTrue(r.calculate(false, 3));
 	}
 }
