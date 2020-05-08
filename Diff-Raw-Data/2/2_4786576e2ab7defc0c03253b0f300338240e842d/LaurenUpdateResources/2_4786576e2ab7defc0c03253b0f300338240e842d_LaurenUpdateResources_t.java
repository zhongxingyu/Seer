 
 import static org.junit.Assert.*;
 
 import java.util.ArrayList;
 import java.util.List;
 
 
 import org.junit.Before;
 import org.junit.Test;
 
 
 public class LaurenUpdateResources {
 
 	Game g;
 	Player p1;
 	
 	@Before
 	public void setUp() throws Exception {
 		p1 = new Player();
 	}
 	
 	/**
 	 * Test update resources when the property is null 
 	 */
 	@Test
 	public void updateProperty1(){
 		Property p = null;
 		Mule m = new Mule(p1, Mule.BASIC);
 	}
 	
 	/**
 	 * Test update resources when the mule passed in is null
 	 */
 	@Test
 	public void updateProperty2(){
 		Property p = new Plain();
 		Mule m = null;
 		assertEquals("Equal", 8, p1.getFood());
 	}
 	
 	/**
 	 * Test update resources with a river with a food mule
 	 */
 	@Test
 	public void updateProperty4(){
 		Property p = new River();
 		p1.setFood(0);
 		Mule m = new Mule(p1, Mule.FOOD);
 		p.updatePlayerResources(m);
		assertEquals("Equal", 4, p1.getFood());
 	}
 	
 	/**
 	 * Test update resources with a river with a energy mule
 	 */
 	@Test
 	public void updateProperty5(){
 		Property p = new River();
 		p1.setEnergy(0);
 		Mule m = new Mule(p1, Mule.ENERGY);
 		p.updatePlayerResources(m);
 		assertEquals("Equals", 2, p1.getEnergy());
 	}
 	
 	/**
 	 * Test update resources with a river with a smithore mule
 	 */
 	@Test
 	public void updateProperty6(){
 		Property p = new River();
 		Mule m = new Mule(p1, Mule.SMITHORE);
 		p1.setSmithore(0);
 		p.updatePlayerResources(m);
 		assertEquals("Equals", 0, p1.getSmithore());
 	}
 	
 	/**
 	 * Test update resources with a plain with a food mule
 	 */
 	@Test
 	public void updateProperty8(){
 		Property p = new Plain();
 		Mule m = new Mule(p1, Mule.FOOD);
 		p1.setFood(0);
 		p.updatePlayerResources(m);
 		assertEquals("Equals", 2, p1.getFood());
 	}
 	
 	/**
 	 * Test update resources with a plain with a energy mule
 	 */
 	@Test
 	public void updateProperty9(){
 		Property p = new Plain();
 		Mule m = new Mule(p1, Mule.ENERGY);
 		p1.setEnergy(0);
 		p.updatePlayerResources(m);
 		assertEquals("Equals", 3, p1.getEnergy());
 	}
 	
 	/**
 	 * Test update resources with a plain with a smithore mule
 	 */
 	@Test
 	public void updateProperty10(){
 		Property p = new Plain();
 		Mule m = new Mule(p1, Mule.SMITHORE);
 		p1.setSmithore(0);
 		p.updatePlayerResources(m);
 		assertEquals("Equals", 1, p1.getSmithore());
 	}
 	
 	/**
 	 * Test update resources with a mountain with a food mule
 	 */
 	@Test
 	public void updateProperty12(){
 		Property p = new Mountain(Mountain.TYPE_1);
 		p1.setFood(0);
 		Mule m = new Mule(p1, Mule.FOOD);
 		p.updatePlayerResources(m);
 		assertEquals("Equals", 1, p1.getFood());
 	}
 	
 	/**
 	 * Test update resources with a mountain with an energy mule
 	 */
 	@Test
 	public void updateProperty13(){
 		Property p = new Mountain(Mountain.TYPE_1);
 		Mule m = new Mule(p1, Mule.ENERGY);
 		p1.setEnergy(0);
 		p.updatePlayerResources(m);
 		assertEquals("Equals", 1, p1.getEnergy());
 	}
 	
 	/**
 	 * Test update resources with a type1 mountain with a smithore mule
 	 */
 	@Test
 	public void updateProperty14(){
 		Property p = new Mountain(Mountain.TYPE_1);
 		Mule m = new Mule(p1, Mule.SMITHORE);
 		p1.setSmithore(0);
 		p.updatePlayerResources(m);
 		assertEquals("Equals", 2, p1.getSmithore());
 	}
 }
 	 
