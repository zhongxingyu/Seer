 package com.github.joakimpersson.tda367.model;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 
 import java.awt.Dimension;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import com.github.joakimpersson.tda367.model.constants.Attribute;
 import com.github.joakimpersson.tda367.model.constants.BombermanRules;
 import com.github.joakimpersson.tda367.model.constants.GameModeType;
 import com.github.joakimpersson.tda367.model.constants.Parameters;
 import com.github.joakimpersson.tda367.model.constants.PlayerAction;
 import com.github.joakimpersson.tda367.model.constants.PointGiver;
 import com.github.joakimpersson.tda367.model.gamelogic.GameLogic;
 import com.github.joakimpersson.tda367.model.gamelogic.IGameLogic;
 import com.github.joakimpersson.tda367.model.highscore.Score;
 import com.github.joakimpersson.tda367.model.player.Player;
 import com.github.joakimpersson.tda367.model.positions.FPosition;
 import com.github.joakimpersson.tda367.model.positions.Position;
 import com.github.joakimpersson.tda367.model.tiles.Tile;
 
 public class PyromaniacModelTest {
 
 	private IPyromaniacModel model;
 	private IGameLogic gameLogic;
 	private List<Player> players;
 
 	@Before
 	public void setUp() throws Exception {
 		model = PyromaniacModel.getInstance();
 		players = new ArrayList<Player>();
 		Dimension mapD = Parameters.INSTANCE.getMapSize();
 		players.add(new Player(1, "testPlayer1", new Position(1, 1)));
 		players.add(new Player(2, "testPlayer2", new Position((int) (mapD
 				.getWidth()) - 2, (int) (mapD.getHeight()) - 2)));
 		gameLogic = new GameLogic(players);
 		model.startGame(players);
 	}
 
 	@Test
 	public void testIsRoundOver() {
 		boolean test1;
 		boolean test2;
 		boolean test3;
 
 		test1 = model.isRoundOver();
 
 		for (Player p : players) {
 			while (p.getHealth() > 1) {
 				p.playerHit();
 			}
 		}
 		test2 = model.isRoundOver();
 
 		for (int i = 1; i < players.size(); i++) {
 			Player p = players.get(i);
 			while (p.isAlive()) {
 				p.playerHit();
 			}
 		}
 		test3 = model.isRoundOver();
 
 		assertTrue(!test1 && !test2 && test3);
 	}
 
 	@Test
 	public void testIsMatchOver() {
 		int maxRounds = BombermanRules.INSTANCE.getNumberOfRounds();
 		boolean test1;
 		boolean test2;
 		boolean test3;
 		Player p = players.get(0);
 
 		test1 = model.isMatchOver();
 
 		while (p.isAlive()) {
 			p.playerHit();
 		}
 		model.roundOver();
 		test2 = model.isMatchOver();
 
 		for (int i = 1; i < maxRounds; i++) {
 			while (p.isAlive()) {
 				p.playerHit();
 			}
 			model.roundOver();
 		}
 		test3 = model.isMatchOver();
 
 		assertTrue(!test1 && !test2 && test3);
 	}
 	
 	@Test
 	public void testIsGameOver() {
 		Player losingPlayer = players.get(0);
 
 		// should be false
 		assertFalse(model.isGameOver());
 
 		simulateGameOver(losingPlayer);
 
 		// should be true
 		assertTrue(model.isGameOver());
 	}
 
 	@Test
 	public void testStartGame() {
 		fail("Not yet implemented");
 	}
 
 	@Test
 	public void testUpdateGame() {
 
 		Player player = players.get(0);
 		double stepSize = player.getSpeededStepSize();
 		double pD = 0.2;
 		FPosition prevPos = new FPosition(0, 0);
 		boolean test1;
 		boolean test2;
 
 		// Test 1.
 		prevPos = player.getGamePosition();
 		model.updateGame(player, PlayerAction.MOVE_WEST);
 		test1 = Math.abs(prevPos.getX() - stepSize
 				- player.getGamePosition().getX()) < 0.01
 				&& Math.abs(prevPos.getY() - player.getGamePosition().getY()) < 0.01;
 
 		// Test 2.
 		while (player.getGamePosition().getX()
 				- (int) player.getGamePosition().getX() - stepSize > pD) {
 			model.updateGame(player, PlayerAction.MOVE_WEST);
 		}
 		prevPos = player.getGamePosition();
 		model.updateGame(player, PlayerAction.MOVE_WEST);
 		test2 = (prevPos.getX() == player.getGamePosition().getX() && prevPos
 				.getY() == player.getGamePosition().getY());
 
 		assertTrue(test1 && test2);
 		// TODO write more on this test depending on how we want move to work...
 	}
 
 	@Test
 	public void testUpgradePlayer() {
 
 		Player player = players.get(0);
 		List<PointGiver> pointGivers = new ArrayList<PointGiver>();
 
 		for (int i = 0; i < 20; i++) {
 			pointGivers.add(PointGiver.KillPlayer);
 		}
 		player.updatePlayerPoints(pointGivers);
 		int preHealth = player.getHealth();
 		int preCredits = player.getCredits();
 
 		model.upgradePlayer(player, Attribute.Health);
 
 		int postHealth = player.getHealth();
 		int postCredits = player.getCredits();
 
 		assertTrue(postHealth == preHealth + 1 && postCredits < preCredits);
 	}
 
 	@Test
 	public void testGetPlayers() {
 		Dimension mapD = Parameters.INSTANCE.getMapSize();
 		Player p1 = new Player(1, "testPlayer1", new Position(1, 1));
 		Player p2 = new Player(2, "testPlayer2", new Position(
 				(int) (mapD.getWidth()) - 2, (int) (mapD.getHeight()) - 2));
 		boolean test1 = (players.get(0).equals(p1) && players.get(1).equals(p2));
 		
 		players.get(0).updatePlayerPoints(PointGiver.MatchWon);
 		boolean test2 = (!players.get(0).equals(p1) && players.get(1).equals(p2));
 		
 		assertTrue(test1 && test2);
 	}
 
 	@Test
 	public void testGetMap() {
 		boolean test1 = true;
 		boolean test2 = true;
 		Tile[][] map1 = model.getMap();
 		model.updateGame(players.get(0), PlayerAction.MOVE_SOUTH);
 		Tile[][] map2 = model.getMap();
 
 		for (int i = 0; i < map1.length; i++) {
 			for (int j = 0; j < map1[0].length; j++) {
 				if (!map1[i][j].equals(map2[i][j])) {
 					test1 = false;
 				}
 			}
 		}
 
		model.updateGame(players.get(0), PlayerAction.PRIMARY_ACTION);
 		map2 = model.getMap();
 
 		for (int i = 0; i < map1.length; i++) {
 			for (int j = 0; j < map1[0].length; j++) {
 				if (!map1[i][j].equals(map2[i][j])) {
 					test2 = false;
 				}
 			}
 		}
 		assertTrue(test1 && !test2);
 
 	}
 	
 	@Test
 	public void testRoundOver() {
 		// first kill one of the player to simulate round over
 		killPlayer(players.get(0));
 
 		// the winner should be player2
 		Player winningPlayer = players.get(1);
 		gameLogic.roundOver();
 
 		int actual = winningPlayer.getRoundsWon();
 		int expected = 1;
 		// he should now have 1 in roundwon
 		assertEquals(actual, expected);
 
 		// his number of rounds win in the GameController object should be 1
 		actual = gameLogic.getRoundsWon(winningPlayer);
 		expected = 1;
 		assertEquals(expected, actual);
 		
 		Player p = players.get(0);
 		p.upgradeAttr(Attribute.Speed, GameModeType.Round);
 		p.upgradeAttr(Attribute.Speed, GameModeType.Match);
 		model.roundOver();
 		assertTrue(p.getAttribute(Attribute.Speed) == 2);
 		
 		// TODO Check these.
 //		cancelRemaingingBombs();
 //		resetMap();
 	}
 	
 	@Test
 	public void testMatchOver() {
 		Player losingPlayer = players.get(0);
 		Player winningPlayer = players.get(1);
 		simulateMatchOver(losingPlayer);
 
 		model.matchOver();
 		// he's MatchWon in PlayerPoints should be equal to 1
 		int expected = 1;
 		int actual = winningPlayer.getMatchesWon();
 		assertEquals(expected, actual);
 		
 		// TODO kolla detta.
 		// matchReset();
 	}
 	
 	@Test
 	public void testGameOver() {
 		Player losingPlayer = players.get(0);
 		Player winningPlayer = players.get(1);
 
 		simulateGameOver(losingPlayer);
 
 		model.gameOver();
 
 		// winningPlayers GameWon in PlayerPoints should be equal to 1
 		int expected = 1;
 		int actual = winningPlayer.getEarnedPointGiver(PointGiver.GameWon);
 		assertEquals(expected, actual);
 		
 		
 		// TODO Check this.
 //		// add the players to highscore list
 //		highscore.update(players);
 	}
 	
 	@Test
 	public void testResetRoundStats() {
 		Player losingPlayer = players.get(0);
 		Player winningPlayer = players.get(1);
 
 		killPlayer(losingPlayer);
 		gameLogic.roundOver();
 
 		assertEquals(0, losingPlayer.getRoundsWon());
 		assertEquals(1, winningPlayer.getRoundsWon());
 
 		model.resetRoundStats();
 
 		assertEquals(0, losingPlayer.getRoundsWon());
 		assertEquals(0, winningPlayer.getRoundsWon());
 	}
 	
 	@Test
 	public void testGetGameOverSummary() {
 		List<Score> list1 = model.getGameOverSummary();
 		assertTrue(list1.size() == 2);
 		players.get(0).updatePlayerPoints(PointGiver.MatchWon);
 		assertTrue(!list1.equals(model.getGameOverSummary()));
 	}
 	
 	@Test
 	public void testGetHighscoreList() {
 		model.resetHighscoreList();
 		List<Score> list1 = model.getHighscoreList();
 		Player p = players.get(0);
 		p.updatePlayerPoints(PointGiver.GameWon);
 		model.gameOver();
 		List<Score> list2 = model.getHighscoreList();
 		assertTrue(!list1.equals(list2));
 	}
 	
 	@Test
 	public void testResetHighscoreList() {
 		model.resetHighscoreList();
 		List<Score> list1 = model.getHighscoreList();
 		Player p = players.get(0);
 		p.updatePlayerPoints(PointGiver.MatchWon);
 		model.gameOver();
 		model.resetHighscoreList();
 		assertTrue(list1.equals(model.getHighscoreList()));
 	}
 	
 	@Test
 	public void testGameReset() {
 		fail("not yet implemented");
 	}
 	
 	@Test
 	public void testGetLastRoundWinner() {
 		Player losingPlayer = players.get(0);
 		// the winner should be player2
 		Player winningPlayer = players.get(1);
 
 		// first kill one of the player to simulate round over
 		killPlayer(losingPlayer);
 
 		// the round is over
 		model.roundOver();
 
 		// the winning player should be player 2
 		Player actual = model.getLastRoundWinner();
 		assertEquals(winningPlayer, actual);
 	}
 
 	@After
 	public void tearDown() throws Exception {
 		model.gameReset();
 		model = null;
 		gameLogic = null;
 	}
 	
 	private void simulateGameOver(Player losingPlayer) {
 		int maxMatchesWon = BombermanRules.INSTANCE.getNumberOfMatches();
 		for (int i = 0; i < maxMatchesWon; i++) {
 			simulateMatchOver(losingPlayer);
 			gameLogic.matchOver();
 			losingPlayer.reset(GameModeType.Match);
 			gameLogic.resetRoundStats();
 		}
 	}
 	
 	private void simulateMatchOver(Player losingPlayer) {
 		int maxRounds = BombermanRules.INSTANCE.getNumberOfRounds();
 
 		for (int i = 0; i < maxRounds; i++) {
 			killPlayer(losingPlayer);
 			gameLogic.roundOver();
 			losingPlayer.reset(GameModeType.Round);
 		}
 	}
 	
 	private void killPlayer(Player p) {
 
 		while (p.isAlive()) {
 			p.playerHit();
 		}
 	}
 }
