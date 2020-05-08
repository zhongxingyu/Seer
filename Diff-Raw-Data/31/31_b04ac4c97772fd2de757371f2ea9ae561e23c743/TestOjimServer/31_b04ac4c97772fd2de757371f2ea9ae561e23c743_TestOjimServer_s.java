 package test;
 
 import static org.junit.Assert.*;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.ojim.iface.Rules;
 import org.ojim.logic.state.Card;
 import org.ojim.logic.state.GetOutOfJailCard;
 import org.ojim.logic.state.ServerGameState;
 import org.ojim.logic.state.fields.BuyableField;
 import org.ojim.logic.state.fields.Field;
 import org.ojim.logic.state.fields.Jail;
 import org.ojim.logic.state.fields.Street;
 import org.ojim.server.OjimServer;
 
 public class TestOjimServer {
 
 	OjimServer server;
 	TestClient2 client1, client2;
 	int id1, id2;
 
 	@Before
 	public void setUp() throws Exception {
 		server = new OjimServer("test");
 		server.initGame(2, 0);
 		client1 = new TestClient2(server);
 		client2 = new TestClient2(server);
 		id1 = client1.id;
 		id2 = client2.id;
 		server.setPlayerReady(id1);
 		server.setPlayerReady(id2);
 	}
 
 	@Test
 	public void testOjimServer() {
 		String name = "testServerName";
 		server = new OjimServer(name);
 		if (!server.getName().equals(name)) {
 			fail("name is " + server.getName() + " ,should be " + name);
 		}
 	}
 
 	@Test
 	public void testInitRMIGame() {
 		// TODO Add test
 	}
 
 	// Nearly Impossible to test, I will test the rest without JUnit
 	@Test
 	public synchronized void testInitEndGame() {
 		// for(int x = 0; x <= 8; x++)
 		// for(int y = 0; y <= 8; y++) {
 		// System.out.println("x:" + x + " y:" + y);
 		// server = new OjimServer("test");
 		// boolean b = server.initGame(x,y);
 		// if(x > 1 && x+y <= 8) {
 		// if(server.getMaxClients() != x) {
 		// fail("playerCount is " + server.getMaxClients() + ", should be " +
 		// x);
 		// }
 		// if(!b) {
 		// fail("Unable to init game!");
 		// }
 		// // if(server.getConnectedClients() != y && x >= y) {
 		// // fail("AI count is " + server.getConnectedClients() +
 		// ", should be " + y + ", maxClients is " + server.getMaxClients());
 		// // }
 		// }
 		// System.out.println("ending, players on Server:" +
 		// server.getConnectedClients());
 		// if(!server.endGame()) {
 		// fail("Ending game was not successful!");
 		// }
 		// }
 	}
 
 	@Test
 	public void testTrade() {
 
 		assertSame(server.getTradeState(), -1);
 		for (int i = 0; i < 3; i++) {
 			server.rollDice(id1);
 		}
 		server.initTrade(id1, id2);
 		assertSame(server.getTradeState(), 0);
 		int[] pars = { 0, 1, 10, 100, 1000, -1 };
 		for (int i = 0; i < 6; i++) {
 			assertSame(10, 10);
 			server.offerCash(id1, pars[i]);
 			assertSame(pars[i] - server.getOfferedCash(), 0);
 		}
 		for (int i = 0; i < 6; i++) {
 			server.requireCash(id1, pars[i]);
 			assertSame(server.getRequiredCash() - pars[i], 0);
 		}
 
 		if (server.getPartner() != id2) {
 			fail("Wrong trade partner:" + server.getPartner() + ", should be:"
 					+ id2);
 		}
 
 		server.proposeTrade(id1);
 		if (server.getTradeState() != 1) {
 			fail("TradeState was not 1 after proposal");
 		}
 
 		server.decline(id2);
 		if (server.getTradeState() != 2) {
 			fail("TradeState was not 2 after decline");
 		}
 
 		server.initTrade(id1, id2);
 		server.offerCash(id1, 10);
 		server.cancelTrade(id1);
 		if (server.getTradeState() != -1) {
 			fail("TradeState was not -1 after trade canceling by acting player");
 		}
 
 		server.initTrade(id1, id2);
 		server.offerCash(id1, 10);
 		server.proposeTrade(id1);
 		server.accept(id2);
 		if (server.getTradeState() != 3) {
 			fail("TradeState was not 3 after successful trade");
 		}
 	}
 
 	/*
 	 * @Test public void testGetAuctionState() { fail("Not yet implemented"); //
 	 * TODO }
 	 * 
 	 * @Test public void testGetAuctionedEstate() { fail("Not yet implemented");
 	 * // TODO }
 	 * 
 	 * @Test public void testGetHighestBid() { fail("Not yet implemented"); //
 	 * TODO }
 	 * 
 	 * @Test public void testGetBidder() { fail("Not yet implemented"); // TODO
 	 * }
 	 * 
 	 * @Test public void testPlaceBid() { fail("Not yet implemented"); // TODO }
 	 */
 
 	@Test
 	public void testGetPlayerPiecePosition() {
 		ServerGameState state = server.getGameState();
 		for (int i = 0; i < 40; i++) {
 			state.getPlayerById(id1).setPosition(i);
 			if (server.getPlayerPiecePosition(id1) != i) {
 				fail("Position is:" + server.getPlayerPiecePosition(id1)
 						+ ", should be:" + i);
 			}
 		}
 	}
 
 	@Test
 	public void testAddCurrentCard() {
 		Card card = new GetOutOfJailCard("testCard", null, null);
 		server.addCurrentCard(card);
 		if (server.getCurrentCards().size() != 1
 				|| !server.getCurrentCards().get(0).equals(card)) {
 			fail("fail");
 		}
 	}
 
 	@Test
 	public void testAddPlayer() {
 		server = new OjimServer("test");
 		server.initGame(2, 1);
 
 		
 		int old = server.getConnectedClients();
 		server.addPlayer(new TestClient2());
 		if (old + 1 != server.getConnectedClients()) {
 			fail("fail while adding Player, oldPlayerCount:" + old
 					+ ", newPlayerCount:" + server.getConnectedClients());
 		}
 	}
 
 	@Test
 	public void testSetPlayerReady() {
 		server.setPlayerReady(id1);
 		if (server.getGameState().getPlayerById(id1).getIsReady() == false) {
 			fail("Player " + id1 + " not set ready");
 		}
 		server.setPlayerReady(id2);
 	}
 
 	@Test
 	public void testGetPlayerName() {
 		if (server.getPlayerName(id1) != "aName") {
 			fail("wrong name returned:" + server.getPlayerName(id1));
 		}
 	}
 
 	@Test
 	public void testGetPlayerColor() {
 		if (server.getPlayerColor(id1) < 0
 				|| server.getPlayerColor(id1) == server.getPlayerColor(id2)) {
 			fail("color1:" + server.getPlayerColor(id1) + ", color2:"
 					+ server.getPlayerColor(id2));
 		}
 	}
 
 	@Test
 	public void testGetRules() {
 		Rules rules = server.getRules();
 		if (rules == null) {
 			fail("no Rules set!");
 		}
 	}
 
 	@Test
 	public void testGetEstateName() {
 		for (int i = 0; i < 40; i++) {
			if (!server.getEstateName(i).endsWith(
 					server.getGameState().getFieldAt(i).getName())) {
				fail("name is: " + server.getEstateName(i) + ", should be "
 						+ server.getGameState().getFieldAt(i).getName());
 			}
 		}
 	}
 
 	@Test
 	public void testGetEstateColorGroup() {
 		for (int i = 0; i < 40; i++) {
 			if (!(server.getEstateColorGroup(i) == server.getGameState()
 					.getFieldAt(i).getFieldGroup().getColor())) {
 				fail("Color of Fieldgroup is set wrong");
 			}
 		}
 	}
 
 	@Test
 	public void testGetEstateHouses() {
 		for (int i = 0; i < 40; i++) {
 			server.getEstateHouses(i);
 			if (server.getGameState().getFieldAt(i) instanceof Street) {
 				if (!(server.getEstateHouses(i) == ((Street) server
 						.getGameState().getFieldAt(i)).getBuiltLevel())) {
 					fail("Wrong Buildlevel");
 				}
 			}
 		}
 	}
 
 	@Test
 	public void testGetEstatePrice() {
 		for (int i = 0; i < 40; i++) {
 			server.getEstatePrice(i);
 			if (server.getGameState().getFieldAt(i) instanceof BuyableField) {
 				if (!(server.getEstatePrice(i) == ((BuyableField) server
 						.getGameState().getFieldAt(i)).getPrice())) {
 					fail("Wrong Price: '"
 							+ server.getEstatePrice(i)
 							+ "' should be '"
 							+ ((BuyableField) server.getGameState().getFieldAt(
 									i)).getPrice() + "'");
 				}
 			}
 		}
 	}
 
 	@Test
 	public void testGetEstateRent() {
 		for (int i = 0; i < 40; i++) {
 			if (server.getGameState().getFieldAt(i) instanceof Street) {
 
 				if (!(server.getEstateRent(i, ((Street) server.getGameState()
 						.getFieldAt(i)).getBuiltLevel()) == ((Street) server
 						.getGameState().getFieldAt(i)).getRent())) {
 					fail("Wrong Rent: '"
 							+ (server.getEstateRent(
 									i,
 									((Street) server.getGameState().getFieldAt(
 											i)).getBuiltLevel())
 									+ "' should be '" + ((Street) server
 									.getGameState().getFieldAt(i)).getRent())
 							+ "'");
 
 				}
 			}
 		}
 	}
 
 	@Test
	public void testGetGameStatusMessage() {
		for (int i = 0; i < 9; i++) {
			server.getGameStatusMessage(i);
			if (server.getGameState().getPlayerById(i) != null) {
				if (!server.getGameStatusMessage(i).equals(
						server.getGameStatusMessage(i))) {

					fail("GameStatusMessage falsch");
				}
			}
		}
	}

	@Test
 	public void testIsMortgaged() {
 		for (int i = 0; i < 40; i++) {
 			server.isMortgaged(i);
 			if (server.getGameState().getFieldAt(i) instanceof BuyableField) {
 				if (!(server.isMortgaged(i) == ((BuyableField) server
 						.getGameState().getFieldAt(i)).isMortgaged())) {
 					fail("Field is not Mortgaged");
 				}
 			}
 		}
 	}
 
 	@Test
 	public void testGetOwner() {
 		for (int i = 0; i < 40; i++) {
 			server.getOwner(i);
 			if (server.getGameState().getFieldAt(i) instanceof BuyableField
 					&& ((BuyableField) server.getGameState().getFieldAt(i))
 							.getOwner() != null) {
 				if (!(server.getOwner(i) == ((BuyableField) server
 						.getGameState().getFieldAt(i)).getOwner().getId())) {
 					fail("Field has the wrong owner");
 				}
 			} else {
 				if (!(server.getOwner(i) == -1)) {
 					fail("Field should have no owner");
 				}
 			}
 		}
 	}
 
 	@Test
 	public void testGetDiceValue() {
 		server.rollDice(id1);
 		if (server.getDiceValue() != (server.getGameState().getDices()
 				.getResultSum())) {
 			fail("Dicesum is different");
 		}
 	}
 
 	@Test
 	public void testGetDiceValues() {
 		server.rollDice(id1);
 		if (server.getDiceValues()[0] != server.getGameState().getDices()
 				.getResult()[0]
 				|| server.getDiceValues()[1] != server.getGameState()
 						.getDices().getResult()[1]) {
 			fail("Dices are not the same");
 		}
 	}
 
 	@Test
 	public void testGetPlayerCash() {
 		for (int i = 0; i < 9; i++) {
 			server.getPlayerCash(i);
 			if (server.getGameState().getPlayerById(i) != null) {
 				if (server.getPlayerCash(i) != server.getGameState()
 						.getPlayerById(i).getBalance()) {
 					fail("Wrong Player Cash");
 				}
 			}
 		}
 	}
 
 	@Test
 	public void testGetPlayerOnTurn() {
 		server = new OjimServer("test");
 		server.initGame(2, 0);
 		client1 = new TestClient2(server);
 		client2 = new TestClient2(server);
 		id1 = client1.id;
 		id2 = client2.id;
 
 		if (server.getPlayerOnTurn() != -1) {
 			fail("Player should not be turned on, Player on turn:"
 					+ server.getPlayerOnTurn());
 		}
 		server.setPlayerReady(id1);
 		server.setPlayerReady(id2);
 
 		if (server.getPlayerOnTurn() == -1) {
 			fail("A player should be turned on.");
 		}
 
 	}
 
 	@Test
 	public void testGetNumberOfGetOutOfJailCards() {
 		if (server.getNumberOfGetOutOfJailCards(id1) != server.getGameState()
 				.getPlayerById(id1).getNumberOfGetOutOfJailCards()) {
 			fail("Wrong count of GooJC");
 		}
 	}
 
 	@Test
 	public void testGetNumberOfHousesLeft() {
 		int old = server.getNumberOfHousesLeft();
 
 		for (int i = 0; i < 4; i++) {
 			if (server.getGameState().getFieldAt(i) instanceof Street)
 
 				server.getLogic().changeFieldOwner(
 						server.getGameState().getPlayerById(id1),
 						(BuyableField) server.getGameState().getFieldAt(i));
 
 		}
 		server.construct(id1, 1);
 		if (old - 1 != server.getNumberOfHousesLeft()) {
 			fail("constructing didnt use a house");
 		}
 	}
 
 	@Test
 	public void testGetNumberOfHotelsLeft() {
 		int old = server.getNumberOfHotelsLeft();
 		server.getGameState().getPlayerById(id1).setBalance(10000000);
 
 		for (int i = 0; i < 4; i++) {
 			if (server.getGameState().getFieldAt(i) instanceof Street)
 
 				server.getLogic().changeFieldOwner(
 						server.getGameState().getPlayerById(id1),
 						(BuyableField) server.getGameState().getFieldAt(i));
 
 		}
 
 		for (int j = 0; j < 4; j++) {
 			for (int i = 0; i < 4; i++) {
 				if (server.getGameState().getFieldAt(i) instanceof Street)
 
 					server.construct(id1, i);
 
 			}
 		}
 		server.construct(id1, 1);
 		if (old - 1 != server.getNumberOfHotelsLeft()) {
 			fail("constructing didnt use a hotel");
 		}
 	}
 
 	@Test
 	public void testRollDice() {
 		int old = server.getDiceValue();
 		if (!server.rollDice(id1) || old == server.getDiceValue()) {
 			fail("Player on turn was unable to Roll dices");
 		}
 		old = server.getDiceValue();
 		if (server.rollDice(id2) || old != server.getDiceValue()) {
 			fail("Player on turn was able to roll dices");
 		}
 	}
 
 	@Test
 	public void testEndTurn() {
 		server.rollDice(id1);
 		server.accept(id1);
 		server.rollDice(id1);
 		server.accept(id1);
 		server.rollDice(id1);
 		server.accept(id1);
 
 		if (!server.endTurn(id1)) {
 			fail("Turn did not end");
 		}
 		if (server.getPlayerOnTurn() == id1) {
 			fail("still old Player on turn");
 		}
 	}
 
 	@Test
 	public void testDeclareBankruptcy() {
 		server.declareBankruptcy(id1);
 		if (server.getGameState().getPlayerById(id1).getBalance() >= 0
 				|| !server.getGameState().getPlayerById(id1).getIsBankrupt()) {
 			fail("Setting Player bankrupt was not successful");
 		}
 	}
 
 	@Test
 	public void testConstruct() {
 		if (server.construct(id1, 1)) {
 			fail("Player was able to construct a house while not owning the Street");
 		}
 		server.getLogic().changeFieldOwner(
 				server.getGameState().getPlayerById(id1),
 				(BuyableField) server.getGameState().getFieldAt(1));
 
 		if (server.construct(id1, 1)) {
 			fail("Player was able to construct a house while not owning all Streets of the Group");
 		}
 
 		for (int i = 0; i < 4; i++) {
 			if (server.getGameState().getFieldAt(i) instanceof Street)
 
 				server.getLogic().changeFieldOwner(
 						server.getGameState().getPlayerById(id1),
 						(BuyableField) server.getGameState().getFieldAt(i));
 
 		}
 		if (!server.construct(id1, 1)) {
 			fail("Player was not able to construct");
 		}
 	}
 
 	@Test
 	public void testDeconstruct() {
 		if (server.deconstruct(id1, 1)) {
 			fail("Player was able to deconstruct a house while not owning the Street");
 		}
 		server.getLogic().changeFieldOwner(
 				server.getGameState().getPlayerById(id1),
 				(BuyableField) server.getGameState().getFieldAt(1));
 
 		if (server.deconstruct(id1, 1)) {
 			fail("Player was able to deconstruct a house while not owning all Streets of the Group");
 		}
 
 		for (int i = 0; i < 4; i++) {
 			if (server.getGameState().getFieldAt(i) instanceof Street)
 
 				server.getLogic().changeFieldOwner(
 						server.getGameState().getPlayerById(id1),
 						(BuyableField) server.getGameState().getFieldAt(i));
 
 		}
 		if (server.deconstruct(id1, 1)) {
 			fail("Player able to deconstruct a street with no houses");
 		}
 		server.construct(id1, 1);
 		if (server.deconstruct(id1, 1)) {
 			fail("Player was not able to deconstruct");
 		}
 	}
 
 	@Test
 	public void testToggleMortgage() {
 		server.getLogic().changeFieldOwner(
 				server.getGameState().getPlayerById(id1),
 				(BuyableField) server.getGameState().getFieldAt(1));
 
 		server.toggleMortgage(id1, 1);
 		if (!server.isMortgaged(1)) {
 			fail("not mortaged while it should be");
 		}
 		server.toggleMortgage(id1, 1);
 		if (server.isMortgaged(1)) {
 			fail("mortaged while it should be not");
 		}
 	}
 
 	@Test
 	public void testSendMessage() {
 		String test = "testMessage";
 		server.sendMessage(test, id2);
 		if (!client1.lastMessage.equals(test) || client1.lastMessagePrivate) {
 			fail("fail while messaging");
 		}
 	}
 
 	@Test
 	public void testSendPrivateMessage() {
 		String test = "testMessage";
 		server.sendPrivateMessage(test, id2, id1);
 		if (!client1.lastMessage.equals(test) || !client1.lastMessagePrivate) {
 			fail("fail while messaging");
 		}
 	}
 
 	@Test
 	public void testGetName() {
 		server = new OjimServer("test");
 		if (!server.getName().equals("test")) {
 			fail("fail setting the Server name");
 		}
 	}
 
 	@Test
 	public void testGetMaxClients() {
 		server = new OjimServer("g");
 		for (int i = 2; i < 9; i++) {
 			server.initGame(i, 0);
 			if (server.getMaxClients() != i) {
 				fail("maxCount:" + server.getMaxClients() + ", should be:" + i);
 			}
 			server = new OjimServer("g");
 		}
 	}
 
 	@Test
 	public void testGetConnectedClients() {
 		server = new OjimServer("g");
 		server.initGame(3, 0);
 		if(server.getConnectedClients() != 0) {
 			fail("Player on Server who should not be there");
 		}
 		
 		client1 = new TestClient2(server);
 		client2 = new TestClient2(server);
 		
 		if(server.getConnectedClients() != 2) {
 			fail("Player count incorrect");
 		}
 		
 	}
 
 	@Test
 	public void testGetTurnsInPrison() {
 		server.getLogic().sendPlayerToJail(server.getGameState().getPlayerById(id1), (Jail)server.getGameState().getFieldAt(10));
 		if(server.getTurnsInPrison(id1) != server.getGameState().getPlayerById(id1).getRoundsInJail()) {
 			fail("turns in prison are:" + server.getTurnsInPrison(id1) + ", should be:" + server.getGameState().getPlayerById(id1).getRoundsInJail());
 		}
 	}
 
 	@Test
 	public void testPayFine() {
 		server.getLogic().sendPlayerToJail(server.getGameState().getPlayerById(id1), (Jail)server.getGameState().getFieldAt(10));
 		server.endTurn(id1);
 		server.rollDice(id2);
 		server.accept(id2);
 		server.rollDice(id2);
 		server.accept(id2);
 		server.rollDice(id2);
 		server.accept(id2);
 		server.endTurn(id2);
 		
 		server.payFine(id1);
 		if(server.getGameState().getPlayerById(id1).getJail() != null) {
 			fail("Player still in Jail");
 		}
 	}
 
 	@Test
 	public void testGetMaximumBuiltLevel() {
 		if(server.getMaximumBuiltLevel() != server.getRules().maxNumOfHouses) {
 			fail("Maximum built level wrong");
 		}
 	}
 
 	@Test
 	public void testGetEstateHousePrice() {
 		for(int i = 0; i < 40; i++) {
 			int price = server.getEstateHousePrice(i);
 			Field f = server.getGameState().getFieldAt(i);
 			if(f instanceof Street){
 				if(price != ((Street)f).getFieldGroup().getHousePrice()) {
 					fail("house price is:" + price + ", should be" + ((Street)f).getFieldGroup().getHousePrice());
 				}
 			} else {
 				if(price != -1) {
 					fail("fail");
 				}
 			}
 		}
 	}
 
 	@Test
 	public void testGetMoneyToPay() {
 		if(server.getMoneyToPay(10) != ((Jail)server.getGameState().getFieldAt(10)).getMoneyToPay()) {
 			fail("is:" + server.getMoneyToPay(10) + ", should be:" + ((Jail)server.getGameState().getFieldAt(10)).getMoneyToPay());
 		}
 	}
 	
 	@Test
 	public void testGetClients() {
 		server = new OjimServer("g");
 		server.initGame(3, 0);
 		if(server.getClients().size() != 0) {
 			fail("there is " + server.getClients().size() + " Client while there should be 0");
 		}
 		
 		client1 = new TestClient2(server);
 		client2 = new TestClient2(server);
 		
 		if(server.getClients().size() != 2 || !server.getClients().contains(client1) || !server.getClients().contains(client2)) {
 			fail("fail");
 		}
 	}
 
 	@Test
 	public void testGetRoundsToWait() {
 		if(server.getRoundsToWait(10) != ((Jail)server.getGameState().getFieldAt(10)).getRoundsToWait()) {
 			fail("is:" + server.getRoundsToWait(10) + ", should be:" + ((Jail)server.getGameState().getFieldAt(10)).getRoundsToWait());
 		}
 	}
 
 }
