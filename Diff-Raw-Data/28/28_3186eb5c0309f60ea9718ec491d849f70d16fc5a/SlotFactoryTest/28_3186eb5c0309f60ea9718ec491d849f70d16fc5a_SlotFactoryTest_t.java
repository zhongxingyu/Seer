 package test;
 
 import static org.junit.Assert.*;
 import model.ExprSlot;
import model.Slot;
 import model.SlotFactory;
 
 import org.junit.Test;
 
 import expr.Expr;
 
 public class SlotFactoryTest {
 
 	@Test
 	public void testBuildString() {
		Slot expected = new ExprSlot(null);
 		String s = "5+7";
 		Slot actual = SlotFactory.build(s);
 	}
 	
 	@Test
 	public void testBuildFile() {
 		fail("Not yet implemented");
 	}
 
 }
