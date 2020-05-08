 package com.github.joakimpersson.tda367.model;
 
 import static org.hamcrest.CoreMatchers.equalTo;
 import static org.hamcrest.CoreMatchers.not;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertThat;
 import static org.junit.Assert.assertTrue;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import com.github.joakimpersson.tda367.model.constants.PointGiver;
 import com.github.joakimpersson.tda367.model.player.PlayerPoints;
 
 /**
  * 
  * @author joakimpersson
  * 
  */
 public class ScoreTest {
 
 	private Score score;
 	private PlayerPoints playerPoints;
 	private String playerName;
 
 	@Before
 	public void setUp() throws Exception {
 		playerName = "Kalle";
 		playerPoints = new PlayerPoints();
 		addPointGivers(playerPoints, 3);
 		score = new Score(playerName, playerPoints);
 	}
 
 	private void addPointGivers(PlayerPoints playerPoints, int nbrOfRoundsWon) {
 		for (int i = 0; i < nbrOfRoundsWon; i++) {
 			playerPoints.update(PointGiver.RoundWon);
 		}
 	}
 
 	@Test
 	public void testGetPlayerName() {
 		assertTrue(playerName == score.getPlayerName());
 	}
 
 	@Test
 	public void testGetPlayerPoints() {
 		assertTrue(playerPoints == score.getPlayerPoints());
 	}
 
 	@Test
 	public void testHashCode() {
 		PlayerPoints otherPlayerPoints = new PlayerPoints();
 		Score otherScore = new Score("Hobbe", otherPlayerPoints);
 		assertFalse(score.hashCode() == otherScore.hashCode());
 
 		otherScore = new Score("Kalle", otherPlayerPoints);
 
 		assertFalse(score.hashCode() == otherScore.hashCode());
 
 		addPointGivers(otherPlayerPoints, 3);
 
 		assertTrue(score.hashCode() == otherScore.hashCode());
 
 		otherScore = new Score("Hobbe", otherPlayerPoints);
 
 		assertFalse(score.hashCode() == otherScore.hashCode());
 		
 	}
 
 	@Test
 	public void testCompareTo() {
 		PlayerPoints otherPlayerPoints = new PlayerPoints();
 		String otherPlayerName = "Hobbe";
 		Score otherScore = new Score(otherPlayerName, otherPlayerPoints);
 
 		assertTrue(score.compareTo(otherScore) > 0);
 
 		addPointGivers(otherPlayerPoints, 3);
 
 		assertTrue(score.compareTo(otherScore) == 0);
 
 		addPointGivers(otherPlayerPoints, 1);
 
 		assertTrue(score.compareTo(otherScore) < 0);
 
 	}
 
 	@Test
 	public void testEqualsObject() {
 		PlayerPoints otherPlayerPoints = new PlayerPoints();
 		Score otherScore = new Score("Hobbe", otherPlayerPoints);
 		assertThat(score, not(equalTo(otherScore)));
 
 		otherScore = new Score("Kalle", otherPlayerPoints);
 
 		assertThat(score, not(equalTo(otherScore)));
 
 		addPointGivers(otherPlayerPoints, 3);
 
 		assertThat(score, equalTo(otherScore));
 
 		otherScore = new Score("Hobbe", otherPlayerPoints);
 
 		assertThat(score, not(equalTo(otherScore)));
 	}
 
 	@After
 	public void tearDown() throws Exception {
 		score = null;
 	}
 }
