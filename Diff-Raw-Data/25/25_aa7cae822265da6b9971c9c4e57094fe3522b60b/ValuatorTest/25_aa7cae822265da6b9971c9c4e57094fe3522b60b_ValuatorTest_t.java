 package test;
 
 import static org.junit.Assert.*;
 
 import org.junit.BeforeClass;
 import org.junit.Test;
 import org.ojim.client.ai.commands.AcceptCommand;
 import org.ojim.client.ai.commands.BuildHouseCommand;
 import org.ojim.client.ai.commands.NullCommand;
 import org.ojim.client.ai.commands.ToggleMortgageCommand;
 import org.ojim.client.ai.valuation.Valuator;
 import org.ojim.logic.Logic;
 import org.ojim.logic.rules.GameRules;
 import org.ojim.logic.state.GameState;
 import org.ojim.logic.state.Player;
 import org.ojim.logic.state.fields.BuyableField;
 import org.ojim.logic.state.fields.Field;
 import org.ojim.logic.state.fields.Street;
 import org.ojim.logic.state.fields.StreetFieldGroup;
 import org.ojim.server.OjimServer;
 import org.ojim.iface.Rules;
 
 import edu.kit.iti.pse.iface.IServer;
 
 public class ValuatorTest {
 
 	private static MyGameState state;
 	private static MyGameRules rules;
 	private static MyLogic logic;
 	private static Street testStreet;
 	private static IServer server;
 	private static Valuator valuator;
 	private static StreetFieldGroup group;
 
 	@BeforeClass
 	public static void setup() {
 		testStreet = new Street("Sumpf", 1, new int[] { 40, 200, 600, 1800, 3200, 5000 }, 0, 1200,
 				null);
 		server = new MyServer("Test");
 		state = new MyGameState();
 		rules = new MyGameRules(state, new Rules());
 		logic = new MyLogic(state, rules);
 		group = new StreetFieldGroup(0, "test", 0);
 		testStreet.setFieldGroup(group);
 		group.addField(testStreet);
 		valuator = new Valuator(logic, server, 0);
 
 	}
 
 	// TODO Fabian?
 	@Test(expected = NullPointerException.class)
 	public void testValuator() {
 		valuator = new Valuator(null, null, 0);
 	}
 
 	@Test(expected = IllegalArgumentException.class)
 	public void testReturnBestCommandWithIllegalPosition() {
 		valuator.returnBestCommand(41);
 	}
 	
 	@Test
 	public void testReturnBestCommand() {
 		// Buy street
 		testStreet.buy(null);
		MyPlayer.mySetBalance(100000);
 		assertTrue(valuator.returnBestCommand(1) instanceof AcceptCommand);
 	}
 
 	@Test
 	public void testPaybackMortgages() {
 		IServer server = new OjimServer("Test");
 		Valuator valuator = new Valuator(logic, server, 0);
 		testStreet.setMortgaged(true);
		MyPlayer.mySetBalance(100000);
 		assertTrue(valuator.paybackMortgages() instanceof ToggleMortgageCommand);
		MyPlayer.mySetBalance(0);
 		assertTrue(valuator.paybackMortgages() instanceof NullCommand);
 	}
 
 
 	@Test(expected = IllegalStateException.class)
 	public void testNegativeWithNegative() {
 		IServer server = new OjimServer("Test");
		MyPlayer.mySetBalance(1);
 		Valuator valuator = new Valuator(logic, server, 0);
 		valuator.negative();
 	}
 
 	@Test
 	public void testNegativeWithPositive() {
 		IServer server = new OjimServer("Test");
		MyPlayer.mySetBalance(1);
 		Valuator valuator = new Valuator(logic, server, 0);
		MyPlayer.mySetBalance(-1);
 		valuator = new Valuator(logic, server, 0);
 		assertTrue(valuator.negative() instanceof ToggleMortgageCommand);
 
 	}
 
 	public final static class MyLogic extends Logic {
 
 		public MyLogic(MyGameState state, GameRules rules) {
 			super(state, rules);
 		}
 
 	}
 
 	public final static class MyPlayer extends Player {
 
		public static int balance = 0;
 
 		public MyPlayer(int id) {
 			super("Test", 1, 0, id, 0);
 		}
 
 		@Override
 		public int getBalance() {
 			return balance;
 		}
		
		public static void mySetBalance(int balance) {
 			MyPlayer.balance = balance;
 		}
 
 		@Override
 		public BuyableField[] getFields() {
 			BuyableField[] result = new BuyableField[1];
 			result[0] = testStreet;
 			return result;
 		}
 
 	}
 
 	public final static class MyGameState extends GameState {
 
 		private static MyPlayer myPlayer = new MyPlayer(0);
 
 		@Override
 		public Player getPlayerById(int id) {
 			return myPlayer;
 		}
 
 		@Override
 		public Player getActivePlayer() {
 			return myPlayer;
 		}
 
 		@Override
 		public Player[] getPlayers() {
 			Player[] result = new Player[2];
 			result[0] = myPlayer;
 			result[1] = new MyPlayer(1);
 			return result;
 		}
 
 		@Override
 		public Field getFieldAt(int position) {
 			return testStreet;
 		}
 		
 
 
 	}
 
 	public final static class MyGameRules extends GameRules {
 
 		public MyGameRules(GameState state, Rules rules) {
 			super(state, rules);
 		}
 
 		@Override
 		public boolean isFieldMortgageable(Player player, Field field) {
 			return true;
 		}
 		
 		@Override
 		public boolean isFieldUpgradable(Player player, Field field, int levelChange) {
 			return  testStreet.getOwner() != null;
 		}
 	}
 	
 	public final static class MyServer extends OjimServer {
 		public MyServer(String name) {
 			super(name);
 		}
 
 		@Override
 		public int getEstateHousePrice(int position) {
 			return 10;
 		}
 	}
 
 }
