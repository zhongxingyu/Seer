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
		logic.Highscore score = new logic.Highscore();
		score.setScore("10ZILJOEN");
		score.setSpelerNaam("Nanne");
		score.setTijdOver("20s");
		highscore.addHighscore(score);
 	}
 
 	/**
 	 * Test method for {@link data.Highscore#getHighscores(int)}.
 	 */
 	@Test
 	public void testGetHighscores() {
 		// fail("Not yet implemented"); // TODO
 	}
 
 }
