 package tests.adapters;
 
 import testpackage.shared.ship.AI;
 import testpackage.shared.ship.BadAI;
 import testpackage.shared.ship.Engine;
 import testpackage.shared.ship.Level;
 import testpackage.shared.ship.State;
 import testpackage.shared.ship.exceptions.InvalidInstruction;
 import testpackage.shared.ship.exceptions.InvalidLevelException;
 
 
 /**
  * This is the test adapter for the minimal stage of completion. You <b>must</b> implement the method stubs and match
  * them to your concrete implementation. Please read all the Javadoc of a method before implementing it. <br>
  * <strong>Important:</strong> this class should not contain any real game logic, you should rather only match the
  * method stubs to your game. <br>
  * Example: in {@link #isValidLevel()} you may return the value <i>myGame.isCorrectlevel()</i>, if you have a variable
  * <i>myGame</i> and a level has before been loaded via {@link #createGameUsingLevelString(String)}. What you mustn't do is to
  * implement the actual logic of the method in this class. <br>
  * <br>
  * If you have implemented the minimal stage of completion, you should be able to implement all method stubs. The public
  * and private JUnit tests for the minimal stage of completion will be run on this test adapter. The other test adapters
  * will inherit from this class, because they need the basic methods (like loading a level), too. <br>
  * <br>
  * The methods of all test adapters need to function without any kind of user interaction.</br>
  * 
  * <i>Note:</i> All other test adapters will inherit from this class.
  * 
  * @see BattleshipTestAdapterExtended1
  * @see BattleshipTestAdapterExtended2
  * @see BattleshipTestAdapterExtended3
  * 
  * @author Jonas Marczona
  * @author Fabian Vogt
  */
 public class BattleshipTestAdapterMinimal {
 	
 	Level level;
 	protected Engine engine;
 	AI ai;
 	boolean catchedException;
 
 	/**
 	 * Use this constructor to initialize everything you need.
 	 */
 	public BattleshipTestAdapterMinimal() {
 		level = new Level();
 		engine = new Engine(level);
 		ai = new BadAI();
 		ai.setEngine(engine);
 	}
 	
 	
 	/** 
 	 * Construct a level from a string. You will be given a string 
 	 * representation of a level and you need to load this into your game.
 	 * Your game should hold the level until a new one is loaded; the other 
 	 * methods will assume that this very level is the current one and that 
 	 * the actions (like removing elements) will run on the specified level.
 	 * If the given string is not valid keep the last loaded level.
 	 * 
 	 * @param levelstring a string representation of the level to load
 	 * @see #isValidLevel()
 	 */
 	public void createGameUsingLevelString(String levelstring) {
 		try {
 			engine.setState(new State(new Level(levelstring)));
 		} catch (InvalidLevelException e) {
 			catchedException = true;
 			return;
 		}
 		catchedException = false;
 	}
 	
 	/** 
 	 * Return the string representation of the current level.
 	 * 
 	 * A level loaded with the method {@link #loadLevelFromString()} 
 	 * should be the same as the result of 
 	 * {@link #getCurrentLevelStringRepresentation()}
 	 * <u>as long as no actions has been performed</u> in the meantime. <br>
 	 * But if there were (valid) actions in the meantime this has to be visible in the level representation.<br>
 	 * The level format is the same
 	 * as the one specified for loading levels (except of additional lines!).
 	 * 
 	 * @return string representation of the current level
 	 * 		or null if no valid level was loaded
 	 * 
 	 * @see #createGameUsingLevelString(String)
 	 * @see #isValidLevel()
 	 */
 	public String getCurrentLevelStringRepresentation() {
 		String str = engine.getState().getLevel().toString();
 		//System.out.println("STRREPR: \n" + str);
 		return str;
 	}
 	
 	/** 
 	 * Is the last(!) loaded level a syntactically correct level?
 	 * See the specification in the task assignment for a definition of
 	 * 'syntactically correct'.<br>
 	 *  
 	 * @return if the last loaded level is syntactically correct return true, 
 	 * otherwise return false
 	 * 
 	 * @see #createGameUsingLevelString(String)
 	 */
 	public boolean isValidLevel() {
 		return !catchedException;
 	}
 	
 	/**
 	 * Is the current level in a state where one player won?
 	 * 
 	 * @return true, if in the current level no further move can be done
 	 */
 	public boolean isFinished() {
 		return engine.isFinished();
 	}
 	
 	/**
 	 * Did the player win the current level?
 	 * 
 	 * @return true, if the player won the current level.
 	 */
 	public boolean isWon() {
 		return engine.checkWin().playernr == 0;
 	}
 	
 	/**
 	 * Did the player lose the current level?
 	 * 
 	 * @return true, if the player lost the current level.
 	 */
 	public boolean isLost() {
 		return engine.checkWin().playernr == 1;
 	}
 	
 	/**
 	 * Selects the cell at the given coordinate, like the player clicks with the mouse at this cell.<br>
 	 * <br>
 	 * Remember: A JUnit testcase should not open a visible gui.<br>
 	 * 
 	 * <p>
 	 * With this method is only input of the player simulated.
 	 * Between every shot of the player there has to be a computer shot.
 	 * In this test-cases here there should <b>not</b> be the 1 second waiting rule as in the normal game with gui.
 	 * Here in the test cases a computer shot should be done only if the method {@link #doAIShot()} is called.
 	 * See {@link #doAIShot()} for more information.   
 	 * </p>
 	 * 
 	 * <p>
 	 * <b>Hint/Remember:</b> The cell on the upper left is 0|0, on the lower left is 0|9
 	 * </p>
 	 * 
 	 * 
 	 * @param x
 	 *            the x-axis part of the coordinate
 	 * @param y
 	 *            the y-axis part of the coordinate
 	 * @return true if it was a legal click for the player. So it was his turn, the selected cell is on the enemy-side and was not hit already.
 	 *         
 	 * @see #doAIShot()        
 	 */
 	public boolean selectCell(int x, int y) {
 		
 		boolean isSelectingShooter;
 		
 		//System.out.println(engine.getState().getLevel().toString());
 		int[] coords = Level.parseTestInterfaceCoords(x, y, engine.getyWidth());
 		int player = coords[0];
 		x = coords[1];
 		y = coords[2];
 		
 		isSelectingShooter = (engine.getState().shotspershipenabled && coords[0] == 0);
 		
 		boolean catched = false;
 		
 		if (isSelectingShooter) {
 			try {
 				engine.chooseFiringXY(player, x, y);
 			} catch (InvalidInstruction e) {
 				return false;
 			}
 			return true;
 		}
 		
 		player = Engine.otherPlayer(player);
 		
 		try {
 			System.out.println("Shot result: "+engine.attack(player, x, y));
 		} catch (InvalidInstruction e) {
 			System.err.println(e.getMessage());
 			catched = true;
 		} finally {
 			//System.out.println(engine.getState().getLevel().toString());
 			if (catched) return false;
 		}
 		//System.out.println("Char: " + c);
 		return true;
 	}
 	
 	
 	/**
 	 * This method should have the same effect like pressing the key 'N'.  
 	 * Call the method that handles the key event when the N key is pressed (to restart the current level).
 	 * Remember: The methods of all test adapters need to function without any kind of user interaction.
 	 * Remember also that your game must work without this TestAdapter.
 	 */
 	public void handleKeyPressedNew() {
 		engine.restartLevel();
 	}
 	
 	
 	/**
 	 * Whether a part of a ship is at the specified coordinate or not.
 	 * Remember: 0|0 is the upper left!
 
 	 * @param x the x part of the coordinate
 	 * @param y the y part of the coordinate
 	 * @return true if there is a ship, false in any other cases (e.g. water, invalid coordinate, ...)
 	 */
 	public boolean isShipAt(int x, int y) {
 		return engine.getState().getLevel().isShip(x, y);
 	}
 	
 	/**
 	 * Whether the cell at the specified coordinate is hit or not.
 	 * If the cell at the point is not hit, this method should return false. 
 
 	 * Remember: 0|0 is the upper left!
 	 * 
 	 * @param x the x part of the coordinate
 	 * @param y the y part of the coordinate
 	 * @return true if this cell is hit, false in any other cases (e.g. ship which is not hit, not hit water, invalid coordinate, ...)
 	 */
 	public boolean isCellHitAt(int x, int y) {
 		int[] coords = Level.parseTestInterfaceCoords(x, y, engine.getyWidth());
 		int p = coords[0];
 		int newx = coords[1];
 		int newy = coords[2];
 		
 		Character[][] board = engine.getState().getLevel().getPlayerBoard(p);
 		return Level.matchChar("LRTBVH", board[newx][newy]);
 	}
 	
 	
 	
 
 	/**
 	 * This method should initiate a attack from the computer,
 	 * if its turn now.
 	 * 
 	 * The fire should be take place directly:
 	 *  - when this method return the board has to be changed completely
 	 *  - undepending on the time the test-case does nothing: the board should NOT change anymore (until the next method call, obviously).
 	 *  - if you have implemented the curtainfire-mode (sperrfeuer) - disable it here.
 	 *  	This feature will be tested during the live test.
 	 *  
 	 * 
 	 * Remember: the first turn is the players.
 	 * 
 	 * @see #selectCell(int, int)     
 	 */
 	public void doAIShot() {
 		if (engine.getState().isPlayerTurn()) { engine.getState().changeTurn(); }
 		//System.err.println(engine.getLevelStringForPlayer(0));
		ai.playAs(1);
 		//System.err.println(engine.getLevelStringForPlayer(0));
 		if (!engine.getState().isPlayerTurn()) { engine.getState().changeTurn(); }
 	}
 
 }
