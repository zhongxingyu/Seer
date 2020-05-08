 package riskarena;
 /*
  * The Bot class is the player type of all AI. It passes requests for
  * choices from the game engine to a RiskBot instance (called skynet).
  * 
  * Evan Radkoff
  */
 
 import java.awt.Color;
 import java.util.concurrent.LinkedBlockingQueue;
 import java.util.concurrent.TimeUnit;
 
 import riskarena.riskbots.*;
 
 public class Bot extends Player {
 	public static final String ICON_URL = Risk.IMAGES_PATH + "robot.png";
 	private final String BOT_PACKAGE = "riskarena.riskbots.";
 	private RiskBot skynet;		// The bot itself
 	private RiskListener risk_listener;	// Very simple object given to skynet that adds choices to from_bot
 	private LinkedBlockingQueue<Integer> from_bot = new LinkedBlockingQueue<Integer>();	// A Queue of answers given by skynet
 	long timeout = 5;	// Max seconds to wait for an answer
 	private GameData data;
 
 	// Constructs a Bot object given its name, color, and player id
 	public Bot(String bot_name, Color c, int id) {
 		super(1, bot_name, c, id);	// Player constructor
 		try {
 			// Loads the class of the RiskBot file being used.
 			Class dynamic_class = Class.forName(BOT_PACKAGE + Risk.RISKBOT_PREFIX + bot_name);
 			skynet = (RiskBot)dynamic_class.newInstance();
 		} catch ( ClassNotFoundException e ) {
 			Risk.sayError("Source file for bot name " + bot_name + " not found.");
 			System.exit(-1);
 		} catch (InstantiationException e) {
 			Risk.sayError("Could not instantiate bot name " + bot_name);
 			e.printStackTrace();
 			System.exit(-1);
 		} catch (IllegalAccessException e) {
 			Risk.sayError("Could not access source file for bot name " + bot_name);
 			e.printStackTrace();
 			System.exit(-1);
 		}
 		risk_listener = new RiskListener();
 	}
 
 	// This is called once in order to pass along a GameInfo object
 	// and the RiskListener to skynet so that it may communicate with the game.
 	public void initializeBot(GameData game_data) {
 		data = game_data;
 		skynet.init(new GameInfo(game_data, this), risk_listener);
 	}
 	
 	// Notifies skynet of turn initialization
 	public void initTurn() {
		new Thread() {
			public void run () {
				skynet.initTurn();
			}
		}.start();
 	}
 	
 	private void sleep() {
 		try {
 			Thread.sleep(data.getBotPlayingSpeed());
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 	}
 
 	// Starts a new thread, calls skynet's implemented claimTerritory method
 	public void claimTerritory() {
 		sleep();
 		new Thread() {
 			public void run () {
 				skynet.claimTerritory();
 			}
 		}.start();
 	}
 
 	// Within a new thread, calls skynet's implemented forifyTerritory method
 	public void fortifyTerritory(final int num_to_place) {
 		sleep();
 		new Thread() {
 			public void run () {
 				skynet.fortifyTerritory(num_to_place);
 			}
 		}.start();
 	}
 
 	// Within a new thread, calls skynet's implemented launchAttack method
 	public void launchAttack() {
 		sleep();
 		new Thread() {
 			public void run () {
 				skynet.launchAttack();
 			}
 		}.start();
 	}
 
 	// Within a new thread, calls skynet's implemented forifyAfterVictory method
 	public void fortifyAfterVictory(final int attacker, final int defender, final int min, final int max) {
 		sleep();
 		new Thread() {
 			public void run () {
 				skynet.fortifyAfterVictory(attacker, defender, min, max);
 			}
 		}.start();
 	}
 
 	// Within a new thread, calls skynet's implemented chooseCardSet method
 	public void chooseCardSet(final int[][] possible_sets) {
 		sleep();
 		new Thread() {
 			public void run () {
 				skynet.chooseCardSet(possible_sets);
 			}
 		}.start();
 	}
 
 	// Within a new thread, calls skynet's implemented chooseToTurnInSet method
 	public void chooseToTurnInSet() {
 		sleep();
 		new Thread() {
 			public void run () {
 				skynet.chooseToTurnInSet();
 			}
 		}.start();
 	}
 
 	// Within a new thread, calls skynet's implemented forifyPosition method
 	public void fortifyPosition() {
 		sleep();
 		new Thread() {
 			public void run () {
 				skynet.fortifyPosition();
 			}
 		}.start();
 	}
 
 
 	// When the game engine is expecting the bot to provide an integer, this method
 	// grabs the int in the front of the LinkedBlockingQueue
 	public int askInt() throws RiskBotException {
 		Integer answer = null;
 		try {
 			answer = from_bot.poll(timeout, TimeUnit.SECONDS);
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 		if(answer == null)
 			throw new RiskBotException(timeout + " second time limit exceeded.");
 
 		return answer;
 	}
 
 	// When the game engine is expecting the bot to provide an integer, this method
 	// grabs the int in the front of the LinkedBlockingQueue and verifies that it's
 	// above MIN (otherwise it throws a RiskBotException)
 	public int askInt(int MIN) throws RiskBotException {
 		Integer answer = null;
 		try {
 			answer = from_bot.poll(timeout, TimeUnit.SECONDS);
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		if(answer == null)
 			throw new RiskBotException(timeout + " second time limit exceeded.");
 		if(answer < MIN)
 			throw new RiskBotException(answer + " is not bigger than or equal to " + MIN);
 
 		return answer;
 	}
 
 	// When the game engine is expecting the bot to provide an integer, this method
 	// grabs the int in the front of the LinkedBlockingQueue and verifies that it's
 	// above MIN and below MAX (otherwise it throws a RiskBotException)
 	public int askInt(int MIN, int MAX) throws RiskBotException {
 		Integer answer = null;
 		try {
 			answer = from_bot.poll(timeout, TimeUnit.SECONDS);
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 		if(answer == null)
 			throw new RiskBotException(timeout + " second time limit exceeded.");
 		if(answer < MIN || answer > MAX)
 			throw new RiskBotException(answer + " is not in the range of " + MIN + " to " + MAX);
 		return answer;
 	}
 
 	// Currently not in use
 	public String askLine() {
 		return "\n";
 	}
 
 	// A simply object capable of adding new Integers to the LinkedBlockingQueue "from_bot"
 	// An instance of RiskListener is passed to skynet, allowing it to supply this
 	// class with its game time decisions.
 	public class RiskListener {
 		public RiskListener() { }
 		public void sendInt(int to_send) {
 			from_bot.add(new Integer(to_send));
 		}
 		public void sendInt(Integer to_send) {
 			from_bot.add(to_send);
 		}
 	}
 
 	// For now this is just like a normal exception
 	public static class RiskBotException extends Exception {
 		public RiskBotException(String msg) {
 			super(msg);
 		}
 	}
 
 }
