 package se.chalmers.kangaroo.model.kangaroo;
 
 import static org.junit.Assert.assertTrue;
 
 import org.junit.Test;
 
 import se.chalmers.kangaroo.model.utils.Position;
 
 public class ImmortalItemTest {
 
 	@Test
	public void testOnUse(){
		Kangaroo k = new Kangaroo(new Position(1,1));
 		ImmortalItem ii = new ImmortalItem(1, 1, 1);
 		ii.onUse(k);
 		assertTrue(k.isImmortal());
 	}
 	
 	@Test
 	public void testGetPosition() {
 		ImmortalItem ii = new ImmortalItem(1,1,1);
 		assertTrue(ii.getPosition().equals(new Position(1,1)));
 	}
 	
 	@Test
 	public void testGetId() {
 		ImmortalItem ii = new ImmortalItem(1,1,1);
 		ImmortalItem it = new ImmortalItem(2,1,1);
 		assertTrue(ii.getId() == 1 && it.getId() == 2);
 	}
 	
 	
 	
 
 }
