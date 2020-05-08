 /**
  * 
  */
 package data.testing;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import data.Highscore;
 
 /**
  * @author nanne
  * 
  */
 public class HighscoreTester {
 
 	private Highscore	highscore;
 
 	/**
 	 * @throws java.lang.Exception
 	 */
 	@Before
 	public void setUp() throws Exception {
 		highscore = new data.Highscore();
 	}
 
 	/**
 	 * Test method for
 	 * {@link data.Highscore#addHighscore(java.lang.String, java.lang.String, java.lang.String)}
 	 * .
 	 */
 	@Test
 	public void testAddHighscore() {
		highscore.addHighscore("Nanne", "10ZILJOEN", "20s");
		// fail("Not yet implemented"); // TODO
 	}
 
 	/**
 	 * Test method for {@link data.Highscore#getHighscores(int)}.
 	 */
 	@Test
 	public void testGetHighscores() {
 		// fail("Not yet implemented"); // TODO
 	}
 
 }
