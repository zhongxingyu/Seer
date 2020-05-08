 package SWE437.Jenkins_Test;
 
 import static org.junit.Assert.*;
 
 import java.util.HashMap;
 
 import org.junit.Test;
 
 
 public class StudentTest {
 
 	public Student s;
 	public Student s1;
 	public Student s2;
 
 	@Test
 	public void newFailingTest() {
 		// Instructor special!
 		// This new version should work
 		// Let's fail again!
 		// And now fixing it.
		assertTrue(3==(1+1+1));
 	}
 
 	@Test (expected = IllegalArgumentException.class)
 	public void nameNotLegalTest() {
 		s = new Student("", 5);
 	}
 
 	@Test (expected = IllegalArgumentException.class)
 	public void numberNotLessThanZero(){
 		s = new Student("Joe", -999);
 	}
 
 	@Test
 	public void studentNameGetter(){
 		s = new Student("James", 12);
 		assertEquals("James", s.getName());
 	}
 
 	@Test
 	public void studentNumberGetter(){
 		s = new Student("James", 12);
 		assertEquals(12, s.getFavoriteNumber());
 	}
 
 	@Test
 	public void equalsSymmetryTest(){
 		s = new Student("James", 12);
 		s1 = new Student("James", 12);
 		assertTrue(s.equals(s1));
 		assertTrue(s1.equals(s));
 
 		s1 = new Student("James", 13);
 		assertFalse(s.equals(s1));
 		assertFalse(s1.equals(s));
 	}
 
 	@Test
 	public void equalsReflexivityTest(){
 		s = new Student("James", 12);
 		s1 = new Student("James", 12);
 		assertTrue(s.equals(s1));
 	}
 
 	@Test
 	public void equalsTransitivityTest(){
 		s = new Student("James", 13);
 		s1 = new Student("James", 13);
 		s2 = new Student("James", 13);
 		assertTrue(s.equals(s1));
 		assertTrue(s1.equals(s2));
 		assertTrue(s.equals(s2));
 	}
 
 	@Test
 	public void equalsNotStudentTest(){
 		s = new Student("Paul", 76);
 		assertFalse(s.equals("Paula"));
 	}
 
 	@Test
 	public void notEqualTest(){
 		s = new Student("Paul", 437);
 		s1 = new Student("James", 437);
 		assertFalse(s.equals(s1));
 	}
 
 	@Test
 	public void hashTest(){
 		s = new Student("Paul", 437);
 		s1 = new Student("Paul", 437);
 		assertTrue(s.equals(s1));
 		assertTrue(s.hashCode()==s1.hashCode());
 	}
 
 	@Test
 	public void hashFailTest(){
 		s = new Student("Paul", 437);
 		s1 = new Student("Paul", 17);
 		assertFalse(s.equals(s1));
 		assertFalse(s.hashCode()==s1.hashCode());
 	}
 
 	@Test
 	public void toStringTest(){
 		s = new Student("Paul", 437);
 		assertEquals("Paul 437", s.toString());
 	}
 
 	@Test (expected = IllegalArgumentException.class)
 	public void numberOverFlow(){
 		s = new Student("Joe", 2147483647 + 1);
 	}
 
 }
