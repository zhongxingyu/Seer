 package tests.GameObjects;
 
 import static org.junit.Assert.*;
 
 import org.junit.Test;
 
 import gameObjects.Player;
 import org.junit.Before;
 
 import com.badlogic.gdx.graphics.Color;
 
 
 public class ComparePlayerTest {
 
 	Player p1;
 	Player p2;
 	Player p1Copy;
 
 	@Before
 	public void setUp(){
 		p1=new Player("Test1",Color.BLACK,0);
 		p2=new Player("Test2",Color.WHITE,1);
		p1Copy = new Player("Test1",Color.BLACK,0);
 	}
 	
 	@Test
 	public void test1() {
 		assertEquals(0,p1.compareTo(p1Copy));
		assertEquals(0,p1.compareTo(p2));
		assertFalse(p1.equals(p2));
 	}
 	
 	@Test
 	public void test2(){
 		assertEquals(0,p1.compareTo(new Integer(21)));
 		assertEquals(0,p1.compareTo("blah"));
 	}
 	
 	@Test
 	public void test3(){
 		p1.setMoney(1000);
 		p2.setMoney(1000);
 		assertEquals(0,p1.compareTo(p2));
 		p1.setMoney(2000);
 		assertEquals(1,p1.compareTo(p2));
 		p1.setMoney(200);
 		assertEquals(-1,p1.compareTo(p2));
 	}
 	
 	@Test
 	public void test4(){
 		assertEquals(0,p1.compareTo(p1Copy));
 		p1.setRace("human");
 		p1Copy.setRace("flapper");
		assertFalse(p1.isEquals(p1Copy));
 		p1Copy.setRace("human");
		assertTrue(p1.isEquals(p1Copy));
 		p1.setName("different");
		assertFalse(p1.isEquals(p1Copy));
 		p1Copy.setName("different");
		assertTrue(p1.isEquals(p1Copy));
 	}
 
 }
