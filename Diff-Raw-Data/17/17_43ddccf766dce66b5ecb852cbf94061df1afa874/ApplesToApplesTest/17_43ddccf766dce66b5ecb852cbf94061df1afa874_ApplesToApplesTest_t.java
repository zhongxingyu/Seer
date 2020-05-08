 package net.dracolair.games.applestoapples;
 
 import java.util.List;
 
 import junit.framework.*;
 
 public class ApplesToApplesTest extends TestCase{
 	
 	private Bot b = null;
 	
 	protected void setUp() {
 		b = new Bot("bees");
 	}
 	
 	protected void tearDown() {
 		
 	}
 	
 	public void testStartupNoGamesRunning() {
 		assertTrue(b.m_chanToGameMap.isEmpty());
 		assertTrue(b.m_nameToGameMap.isEmpty());
 	}
 	
 	public void testCmdIsRunningFalseForBob() {
 		List<Message> responses = cmd("bob", "!list");
 		assertMessage("bob", "bob: No game is running.", responses.get(0));
 	}
 	
 	public void testCmdIsRunningTrueForBob1Player() {
 		cmd("Neel", "!join");
 		List<Message> responses = cmd("bob", "!list");
 		ApplesToApples ata = b.getGameByChan("#channel");
 		Name neel = b.m_nickToNameMap.get("Neel");
 		
 		assertEquals(1, ata.m_players.size());
 		assertTrue(ata.m_players.containsKey(neel));
 		assertMessage("bob", "bob: List of players: Neel:0 ", responses.get(0));
 	}
 	
 	public void testCmdIsRunningTrueForBob2Players() {
 		cmd("Neel", "!join");
 		cmd("Grue", "!join");
 		List<Message> responses = cmd("bob", "!list");
 		ApplesToApples ata = b.getGameByChan("#channel");
 		Name neel = b.m_nickToNameMap.get("Neel");
 		Name grue = b.m_nickToNameMap.get("Grue");
 		
 		assertEquals(2, ata.m_players.size());
 		assertTrue(ata.m_players.containsKey(neel));
 		assertTrue(ata.m_players.containsKey(grue));
 		assertMessage("bob", "bob: List of players: Neel:0 Grue:0 ", responses.get(0));
 	}
 	
 	public void testFirstPlayerJoins() {
 		List<Message> responses = cmd("bob", "!join");
 		
 		ApplesToApples ataFromGList = b.m_chanToGameMap.get("#channel");
 		ApplesToApples ataFromPList = b.getGameByNick("bob");
 		
 		assertEquals(1, b.m_chanToGameMap.size());
 		assertNotNull(ataFromGList);
 		assertEquals(1, b.m_nameToGameMap.size());
 		assertNotNull(ataFromPList);
 		assertEquals(ataFromGList, ataFromPList);
 		assertEquals(1, ataFromGList.m_players.size());
 		assertMessage("#channel", "bob has joined the game, need 2 more to start.", responses.get(0));
 	}
 	
 	public void testBobCantJoinTwice() {
 		cmd("bob", "!join");
 		List<Message> responses = cmd("bob", "!join");
 		
 		ApplesToApples ataFromGList = b.m_chanToGameMap.get("#channel");
 		ApplesToApples ataFromPList = b.getGameByNick("bob");
 		
 		assertEquals(1, b.m_chanToGameMap.size());
 		assertNotNull(ataFromGList);
 		assertEquals(1, b.m_nameToGameMap.size());
 		assertNotNull(ataFromPList);
 		assertEquals(ataFromGList, ataFromPList);
 		assertEquals(1, ataFromGList.m_players.size());
 		assertMessage("#channel", "bob is already playing.", responses.get(0));
 	}
 	
 	public void testBobCantJoinTwiceFromAnotherChannel() {
 		cmd("bob", "!join");
 		String[] msgMap = {"#bees", "bob", "login", "hostname", "!join"};
 		List<Message> responses = b.handleChanMessage(msgMap);
 		
 		ApplesToApples ataFromGList = b.m_chanToGameMap.get("#channel");
 		ApplesToApples ataFromPList = b.getGameByNick("bob");
 		
 		assertEquals(1, b.m_chanToGameMap.size());
 		assertNotNull(ataFromGList);
 		assertEquals(1, b.m_nameToGameMap.size());
 		assertNotNull(ataFromPList);
 		assertEquals(ataFromGList, ataFromPList);
 		assertEquals(1, ataFromGList.m_players.size());
 		assertMessage("#bees", "bob is already playing.", responses.get(0));
 	}
 	
 	public void testStartGameFail() {
 		List<Message> responses = cmd("bob", "!start");
 		
 		assertMessage("#channel", "bob is fail, needs 3 to play.", responses.get(0));
 	}
 	
 	public void testStartGameNeeds2More() {
 		cmd("bob", "!join");
 		List<Message> responses = cmd("bob", "!start");
 		
 		assertMessage("#channel", "bob is fail, needs 2 to play.", responses.get(0));
 	}
 	
 	public void testStartGameWorks() {
 		cmd("bob", "!join");
 		cmd("neel", "!join");
 		cmd("grue", "!join");
 		List<Message> responses = cmd("bob", "!start");
 		ApplesToApples ata = b.getGameByChan("#channel");
 		
 		assertEquals(3, ata.m_activePlayers.size());
 		assertEquals("bob", ata.m_activePlayers.get(0).toString());
 		assertEquals("neel", ata.m_activePlayers.get(1).toString());
 		assertEquals("grue", ata.m_activePlayers.get(2).toString());
 		
 		assertMessage("#channel", "We have >=3 players, the game will begin!", responses.get(0));
 		assertMessage("#channel", "Dealing out cards...", responses.get(1));
 		for(int i = 2; i < 9; i++) {
 			assertMessage("bob", "Card - " + (i - 1), responses.get(i));
 		}
 		for(int i = 9; i < 16; i++) {
 			assertMessage("neel", "Card - " + (i - 8), responses.get(i));
 		}
 		for(int i = 16; i < 23; i++) {
 			assertMessage("grue", "Card - " + (i - 15), responses.get(i));
 		}
 		assertMessage("bees", "!botplay", responses.get(23));
 	}
 	
 	public void testNewRound() {
 		cmd("bob", "!join");
 		cmd("neel", "!join");
 		cmd("grue", "!join");
 		cmd("bob", "!start");
 		List<Message> responses = cmd("bees", "!botplay");
 		ApplesToApples ata = b.getGameByChan("#channel");
 		
		assertEquals(2, ata.m_waiting.size());
 		assertEquals("bob", ata.m_judge.toString());
 		
 		assertMessage("#channel", "bob is the judge.  Green card is: hax", responses.get(0));
 		assertMessage("#channel", "Waiting for players to play cards...", responses.get(1));
 	}
 	
 	public void testAllPlayersPlayCards() {
 		cmd("bob", "!join");
 		cmd("neel", "!join");
 		cmd("grue", "!join");
 		cmd("bob", "!start");
 		cmd("bees", "!botplay");
 		cmd("neel", "!play 5");
 		List<Message> responses = cmd("grue", "!play 4");
 		
 		assertMessage("grue", "Card - 8", responses.get(0));
 		assertMessage("bees", "!botchoose", responses.get(1));
 	}
 	
 	public void testChooseMenu() {
 		cmd("bob", "!join");
 		cmd("neel", "!join");
 		cmd("grue", "!join");
 		cmd("bob", "!start");
 		cmd("bees", "!botplay");
 		cmd("neel", "!play 5");
 		cmd("grue", "!play 4");
 		List<Message> responses = cmd("bees", "!botchoose");
 		
 		assertMessage("#channel", "The green card is: hax", responses.get(0));
 		assertMessage("#channel", "1. Card 5", responses.get(1));
 		assertMessage("#channel", "2. Card 4", responses.get(2));
 		assertMessage("#channel", "bob must choose a red card!  Type '!choose number'", responses.get(3));
 	}
 	
 	public void testAllPlayersPlayCardsAndJudgePicks() {
 		cmd("bob", "!join");
 		cmd("neel", "!join");
 		cmd("grue", "!join");
 		cmd("bob", "!start");
 		cmd("neel", "!play 5");
 		cmd("grue", "!play 4");
 		cmd("bees", "!botchoose");
 		List<Message> responses = cmd("bob", "!choose 2");
 		
 		assertMessage("#channel", "The winner is grue: Card 4!", responses.get(0));
 		assertMessage("#channel", "Scores: bob:0 neel:0 grue:1 ", responses.get(1));
 		assertMessage("#channel", "neel is the judge.  Green card is: hax", responses.get(2));
 		assertMessage("#channel", "Waiting for players to play cards...", responses.get(3));
 	}
 	
 	public List<Message> cmd(String name, String command) {
 		String[] msgMap = {"#channel", name, "login", "hostname", command};
 		return b.handleChanMessage(msgMap);
 	}
 	
 	public static void assertMessage(String target, String message, Message msg) {
 		assertEquals(target, msg.m_target);
 		assertEquals(message, msg.m_message);
 	}
 }
