 package com.github.joakimpersson.tda367.model;
 
 import static org.junit.Assert.*;
 
 import java.awt.Dimension;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import com.github.joakimpersson.tda367.model.constants.Attribute;
 import com.github.joakimpersson.tda367.model.constants.Parameters;
 import com.github.joakimpersson.tda367.model.constants.PlayerAction;
 import com.github.joakimpersson.tda367.model.constants.PointGiver;
 import com.github.joakimpersson.tda367.model.player.Player;
 import com.github.joakimpersson.tda367.model.utils.FPosition;
 import com.github.joakimpersson.tda367.model.utils.Position;
 
 public class BombermanModelTest {
 
 	private IBombermanModel model;
 
 	@Before
 	public void setUp() throws Exception {
 		model = BombermanModel.getInstance();
 		List<Player> players = new ArrayList<Player>();
 		Dimension mapD = Parameters.INSTANCE.getMapSize();
 		players.add(new Player(1,"testPlayer1", new Position(1, 1)));
 		players.add(new Player(2,"testPlayer2", new Position(
 				(int)(mapD.getWidth()) -2, (int)(mapD.getHeight()) -2)));
 		model.startGame(players);
 	}
 
 	@Test
 	public void testIsRoundOver() {
 		boolean test1;
 		boolean test2;
 		boolean test3;
 		
 		test1 = model.isRoundOver();
 		
 		List<Player> playerList = model.getPlayers();
 		for(Player p : playerList) {
 			p.playerHit();
 			p.removeImmortality();
 			p.playerHit();
 		}
 		test2 = model.isRoundOver();
 		
 		for(int i = 1; i < playerList.size(); i++) {
 			Player p = playerList.get(i);
 			p.removeImmortality();
 			p.playerHit();
 		}
 		test3 = model.isRoundOver();
 
 		assertTrue(!test1 && !test2 && test3);
 	}
 	
 	@Test
 	public void testStartGame() {
 		fail("Not yet implemented");
 	}
 
 	@Test
 	public void testEndGame() {
 		fail("Not yet implemented");
 	}
 
 	@Test
 	public void testUpdateGame() {
 		System.out.println(model);
 		
 		Player player = model.getPlayers().get(0);
 		double stepSize = Parameters.INSTANCE.getPlayerStepSize();
 		double pD = 0.2;
 		FPosition prevPos = new FPosition(0, 0);
 		boolean test1;
 		boolean test2;
 		
 		// Test 1.
 		prevPos = player.getGamePosition();
 		model.updateGame(player, PlayerAction.MOVE_WEST);
 		test1 = Math.abs(prevPos.getX() - stepSize - player.getGamePosition().getX()) < 0.01 &&
 				Math.abs(prevPos.getY() - player.getGamePosition().getY()) < 0.01;
 		
 		
 		// Test 2.
 		while(player.getGamePosition().getX() - (int)player.getGamePosition().getX() - stepSize > pD) {
 			model.updateGame(player, PlayerAction.MOVE_WEST);
 		}
 		prevPos = player.getGamePosition();
 		model.updateGame(player, PlayerAction.MOVE_WEST);
 		test2 = (prevPos.getX() == player.getGamePosition().getX() &&
 				prevPos.getY() == player.getGamePosition().getY());
 		
		assertTrue(test1 && test2);
 		// TODO write more on this test depending on how we want move to work...
 	}
 
 	@Test
 	public void testUpgradePlayer() {
 		
 		Player player = model.getPlayers().get(0);
 		List<PointGiver> pointGivers = new ArrayList<PointGiver>();
 		
 		for(int i = 0; i < 20; i++) {
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
 	
 	@After
 	public void tearDown() throws Exception {
 		List<Player> players = model.getPlayers();
 		players.clear();
 		model = null;
 	}
 }
