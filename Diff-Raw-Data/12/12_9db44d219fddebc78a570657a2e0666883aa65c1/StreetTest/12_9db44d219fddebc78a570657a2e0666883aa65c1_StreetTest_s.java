 package model;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 
 import org.junit.Test;
 
 public class StreetTest {
 	
 	@Test
 	public void testGetLength() {
 		
 		// no heigth
 		Node k1 = new Node(0, 0);
 		Node k2 = new Node(4, 3);
 		
 		Street s = new Street(k1, k2);
 		assertEquals(5, s.getLenth());
 		
 		s = new Street(k2, k1);
 		assertEquals(5, s.getLenth());
 		
 		// icluding height
 		Street s2 = new Street(new Node(0, 0, 0), new Node(5, 5, 4));
 		assertEquals(8, s2.getLenth());
 		
 		Street s3 = new Street(new Node(3, 10, 34), new Node(10, 1, 4));
 		assertEquals(32, s3.getLenth());
 	}
 	
 	@Test
 	public void testGetFlatLength() {
 		Street s1 = new Street(new Node(0, 0, 0), new Node(3, 4, 4));
 		assertEquals(5, s1.getFlatLength());
 		
 		Street s2 = new Street(new Node(3, 10, 34), new Node(10, 1, 4));
 		assertEquals(11, s2.getFlatLength());
 	}
 	
 	@Test
 	public void testGetHeightDiff() {
 		Street s1 = new Street(new Node(0, 0, 0), new Node(3, 4, 4));
 		assertEquals(4, s1.getHeightDiff());
 		
 		Street s2 = new Street(new Node(0, 0, 100), new Node(3, 4, 4));
 		assertEquals(-96, s2.getHeightDiff());
 	}
 	
 	@Test
 	public void testIsPointOnStreet() {
 		Node k1 = new Node(0, 0);
		Node k2 = new Node(4, 4);
 		
 		Street s = new Street(k1, k2);
 		
		assertTrue(s.isPointOnStreet(2, 2));
		assertTrue(s.isPointOnStreet(2, 1));
 		
		assertFalse(s.isPointOnStreet(10, 4));
		assertFalse(s.isPointOnStreet(10, 10));
 	}
 	
 	@Test
 	public void testGetIncline() {
 		Node n1 = new Node(0, 0, 0);
 		Node n2 = new Node(40, 0, 20);
 		
 		Street s1 = new Street(n1, n2);
 		assertEquals(0.5, s1.getIncline(), 0);
 		
 		Street s2 = new Street(n2, n1);
 		assertEquals(-0.5, s2.getIncline(), 0);
 		
 		Street s3 = new Street(new Node(9, 9, 20), n2);
 		assertEquals(0, s3.getIncline(), 0);
 	}
 	
 	@Test
 	public void testGetMiddle() {
 		Node n1 = new Node(100, 100);
 		Node n2 = new Node(200, 200);
 		
 		Street s1 = new Street(n1, n2);
 		assertEquals(new Node(150, 150), s1.getMiddle());
 		
 		Street s2 = new Street(n2, n1);
 		assertEquals(new Node(150, 150), s2.getMiddle());
 		
 	}
 	
 	@Test
 	public void testGetAtEnd() {
 		Street s1 = new Street(new Node(10, 10), new Node(100, 100));
 		
 		assertEquals(new Node(85, 85), s1.getAtEnd());
 	}
 	
 }
