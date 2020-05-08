 package se.chalmers.kangaroo.model.kangaroo;
 
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 
 import org.junit.Test;
 
 import se.chalmers.kangaroo.model.utils.Position;
 
 public class DoubleJumpItemTest {
 
 	@Test
 	public void test() {
 		fail("Not yet implemented");
 	}
 	
 	@Test
 	public void testOnUse() {
 		Kangaroo k = new Kangaroo(new Position(0,0));
 		DoubleJumpItem dji = new DoubleJumpItem(1,1,1);
 		dji.onUse(k);
 		assertTrue(k.isDoubleJumpEnabled()==true);
 	}
 	
 	@Test
 	public void testOnDrop() {
 		Kangaroo k = new Kangaroo(new Position(0,0));
 		DoubleJumpItem dji = new DoubleJumpItem(1,1,1);
 		k.enableDoubleJump();
 		dji.onDrop(k);
 		assertTrue(k.isDoubleJumpEnabled()==false);
 	}
 	
 	@Test
 	public void testOnPickup() {
 		Kangaroo k = new Kangaroo(new Position(0,0));
 		DoubleJumpItem dji = new DoubleJumpItem(1,1,1);
 		dji.onPickup(k);
 		assertTrue(k.getItem().equals(dji));
 	}
 
 }
