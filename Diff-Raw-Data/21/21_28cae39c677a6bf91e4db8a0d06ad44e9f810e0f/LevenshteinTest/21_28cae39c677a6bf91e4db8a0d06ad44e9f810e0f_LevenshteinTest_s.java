 package brown.puzzles.breathalyzer;
 
 import junit.framework.TestCase;
 
 
 /**
  * @author Matt Brown
  * @date Feb 23, 2010
  *
  */
 public class LevenshteinTest extends TestCase {
 
 	public void testScore() {
 		assertEquals(3, Levenshtein.score("sitting", "kitten"));
 		assertEquals(3, Levenshtein.score("Saturday", "Sunday"));
 	}
 }
