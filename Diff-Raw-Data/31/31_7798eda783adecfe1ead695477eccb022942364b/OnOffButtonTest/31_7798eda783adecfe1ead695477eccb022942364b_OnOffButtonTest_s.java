 package se.chalmers.kangaroo.model.iobject;
 import static org.junit.Assert.*;
 import static org.junit.Assert.fail;
 
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import se.chalmers.kangaroo.model.GameMap;
 import se.chalmers.kangaroo.model.iobject.OnOffButton;
 import se.chalmers.kangaroo.model.utils.Position;
 
 
 public class OnOffButtonTest {
 
 	@BeforeClass
 	public static void setUpBeforeClass() throws Exception {
 	}
 
 	@Test
 	public void test() {
 		fail("Not yet implemented");
 	}
 	
 	@Test
 	public void testIsCollidable() {
		OnOffButton ob = new OnOffButton(new Position(1,1),73,new GameMap("1"));
 		assertTrue(ob.isCollidable(1));
 	}
 	
 	@Test
 	public void testGetChangedId() {
		OnOffButton ob = new OnOffButton(new Position(1,1),73,new GameMap("1"));
 		int a = ob.getChangedId(73);
 		int b = ob.getChangedId(74);
 		assertTrue(a == 74 && b == 73);
 	}
 	
 	@Test
 	public void testGetPosition() {
		OnOffButton ob = new OnOffButton(new Position(1,1),73,new GameMap("1"));
 		assertTrue(ob.getPosition().equals(new Position(1,1)));
 	}
 	
 	@Test
 	public void testGetId() {
		OnOffButton ob = new OnOffButton(new Position(1,1),73,new GameMap("1"));
		OnOffButton obe = new OnOffButton(new Position(1,1),74,new GameMap("1"));
 		assertTrue(ob.getId() == 73 && obe.getId() == 74);
 	}
 	
 	@Test
 	public void testChangeId() {
		OnOffButton ob = new OnOffButton(new Position(1,1),73,new GameMap("1"));
		OnOffButton obe = new OnOffButton(new Position(1,1),74,new GameMap("1"));
 		ob.changeId();
 		obe.changeId();
 		assertTrue(ob.getId() == 74 && obe.getId() == 73);
 	}
 
 }
