 package controller;
 
 import java.util.List;
 
 import com.fasterxml.jackson.databind.JsonNode;
 import com.google.inject.Guice;
 import com.google.inject.Inject;
 import com.google.inject.Injector;
 
 import database.GameContent;
 import database.IDatabase;
 import model.general.Constances;
 import model.general.Status;
 import model.playground.Coordinates;
 import modules.AiModule;
 import util.observer.Observable;
 
 /** 
  * The GameController manages the game logic. Which is switching between players/playgrounds,
  * providing methods to shoot to the enemy's playground,..
  * @author Dennis Parlak
  */
 public class GameController extends Observable {
 	
 	public static final String AI_PLAYER_1 = "Computer 1";
 	public static final String AI_PLAYER_2 = "Computer 2";
 	public static final String HUMAN_PLAYER_1 = "Player 1";
 	public static final String HUMAN_PLAYER_2 = "Player 2";
 		
 	private IDatabase database;
 	private GameContent content;
 
 	/**
 	 * Creates a new game	 
 	 */
 	@Inject
 	public GameController(IDatabase database) {
 		this.database = database;
 	}	
 	/**
 	 * Create a new controller with a whole new playground which have new random placed ships on it.
 	 */
 	public final void newController(int rows, int columns, String player1, String player2, int gameType) {
 		Injector inject = Guice.createInjector(new AiModule().setSettings(AiModule.Settings.Hard));
 		content = inject.getInstance(GameContent.class);
 		content.initContent(rows, columns, player1, player2, gameType);
 		this.checkGameType();
 		this.aiShoot();
 	}
 	/**
 	 * Saves the actual game
 	 */
 	public boolean saveGame(String name) {
		return database.save(name, content);
 	}
 	/**
 	 * Loads the actual game
 	 */
 	public void loadGame(String name) {
 		notifyObserversOnLoaded();
 		content = database.load(name);
 		notifyObservers();
 	}
 	/**
 	 * Gets all the stored GameContent names
 	 */
 	public List<String> getStoredGames() {
 		return database.getAll();
 	}
 	/**
 	 * Deletes the specific savegame
 	 */
 	public void deleteGame(String name) {
 		database.delete(name);
 	}
 	/**
 	 * check for correct game type
 	 * @throws exception if gameType is not SINGLEPLAYER or MULTIPLAYER
 	 */
 	private void checkGameType() {
 		if (content.getGameType() != GameContent.MULTIPLAYER && content.getGameType() != GameContent.SINGLEPLAYER) {
 			throw new IllegalAccessError("Invalid game Type. Choose MULTIPLAYER or SINGLEPLAYER.");
 		}
 	}	
 	/**
 	 * Get back the game type
 	 * @return the game type which should be SINGLEPLAYER or MULTIPLAYER
 	 */
 	public int getGameType() {
 		return content.getGameType();
 	}	
 	/**
 	 * Check if the game is finished (all ships of one place are destroyed)
 	 * @return true  The game finished, the winner of the game was written to status
 	 * @return false The game is not finished.
 	 */
 	public boolean gameFinished() {
 		if (null == content.getEnemyPlayground()) {
 			return true;
 		}
 		if (0 == content.getEnemyPlayground().getNumberOfExistingShips()) {
 			content.getStatus().addText(content.getActivePlayer() + " won.");
 			return true;
 		}
 		return false;
 	}	
 	/**
 	 * Get the status object. The status object logs information after a important method
 	 * call like shoot(..)
 	 * @return status A status object with logged information.
 	 */
 	public Status getStatus() {
 		return content.getStatus();
 	}	
 	/**
 	 * Actual player shoots to a target. After the shot, the player switch if the player
 	 * have not hit a target. The observers will be notified after the shot, independent
 	 * from the result of the shot. 
 	 * @return The result of the shot (see Playground class)
 	 */
 	public int shoot(String player, Coordinates t) {
 		int shootStatus = Constances.SHOOT_INVALID;
 		Coordinates target = t;
 		/* check if the player try to shoot which has to shoot.*/
 		if (0 != getActivePlayer().compareTo(player)) {
 			return shootStatus;
 		}
 		
 		content.setSwitchedPlayer(false);
 		
 		if (null == target || isAI()) {
 			/* get the target from the AI */
 			target = content.getActiveAI().shoot();
 			content.getStatus().addText("AI shoot to: " + target.toString() + ".");
 		}
 		
 		if (content.getEnemyPlayground().alreadyShot(target)) {
 			content.getStatus().addError("Already shot to this target. Try again.");	
 		}
 		else
 		{
 			shootStatus = content.getEnemyPlayground().shoot(target);
 			content.getStatus().moveStatus(content.getEnemyPlayground().getStatus());
 			/* set flags for AI */
 			if(content.getActiveAI().setFlags(shootStatus))
 			{
 				/* no ship hit/destroyed, so switch the user */
 				switchPlayer();
 				content.getStatus().addText(content.getActivePlayer() + " please select your target.");
 			}			
 		}
 		
 		notifyObservers();
 		/* if the new player is a AI */
 		if (isAI() && switchedPlayer()) {
 			aiShoot();
 		}
 		return shootStatus;
 	}	
 	
 	/**
 	 * aiShoot, will shoot until the game is finished or the AI did not hit a ship.
 	 * Note that the aiShoot method will call the shoot method, which will notify
 	 * the Observers after every shot.
 	 */
 	private void aiShoot() {
 		while (isAI()) {
 			if (gameFinished()) {
 				break;
 			}
 			int coord = shoot(getActivePlayer(), null);
 			if (Constances.SHOOT_HIT != coord && Constances.SHOOT_DESTROYED != coord) {
 				break;
 			}
 		}
 	}
 	
 	/**
 	 * Get the name of the active player
 	 * @return name The name of the active player
 	 */
 	public String getActivePlayer() {
 		return content.getActivePlayer();
 	}	
 	/**
 	 * Get the name of the enemy player
 	 * @return name The name of the enemy
 	 */
 	public String getEnemyPlayer() {
 		return content.getEnemyPlayer();
 	}	
 	/**
 	 * Get the rows of the playground
 	 * @return rows The number of columns 
 	 */
 	public int getRows() {
 		return content.getPlayground1().getRows();
 	}	
 	/**
 	 * Get the columns of the playground
 	 * @return columns The number of columns 
 	 */
 	public int getColumns() {
 		return content.getPlayground1().getColumns();
 	}	
 	/**
 	 * Get the playground of the active player as a String.
 	 * @return Formatted String of the playground
 	 */
 	public String getEnemyPlaygroundAsString() {
 		return content.getEnemyPlayground().enemyStringView();
 	}	
 	/**
 	 * Get the playground of the enemy player as a String.
 	 * @return Formatted String of the playground
 	 */
 	public char [][] getEnemyPlaygroundAsMatrix() {
 		return content.getEnemyPlayground().enemyView();
 	}	
 	/**
 	 * Get the playground of the enemy player as a Json.
 	 * @return Json of the playground
 	 */
 	public JsonNode getEnemyPlaygroundAsJson() {
 		return content.getEnemyPlayground().enemyJsonView();
 	}	
 	/**
 	 * Get the playground of the enemy player (With no ships visible.).
 	 * @return 2 dimensional matrix of the playground
 	 */
 	public String getOwnPlaygroundAsString() {
 		return content.getOwnPlayground().ownStringView();
 	}	
 	/**
 	 * Get the playground of the active player (With all placed ships visible).
 	 * @return 2 dimensional matrix of the playground
 	 */
 	public char [][] getOwnPlaygroundAsMatrix() {
 		return content.getOwnPlayground().ownView();
 	}	
 	/**
 	 * Get the playground of the active player (With all placed ships visible).
 	 * @return Json of the playground
 	 */
 	public JsonNode getOwnPlaygroundAsJson() {
 		return content.getOwnPlayground().ownJsonView();
 	}	
 	/**
 	 * check if the actual active player is a computer
 	 * @return true if the actual player is a computer, false if not
 	 */
 	public boolean isAI() {
 		if (content.getActivePlayer().equals(AI_PLAYER_1)
 		  || content.getActivePlayer().equals(AI_PLAYER_2)) {
 			return true;
 		}
 		return false;
 	}	
 	/**
 	 * check if the enemy is a computer
 	 * @return true if the enemy is a computer, false if not
 	 */
 	public boolean isEnemyAI() {
 		if (content.getEnemyPlayer().equals(AI_PLAYER_1)
 		  || content.getEnemyPlayer().equals(AI_PLAYER_2)) {
 			return true;
 		}
 		return false;
 	}	
 	/**
 	 * Switch player. Will be called if the actual player did not hit a ship.
 	 */
 	public void switchPlayer() {
 		content.setSwitchedPlayer(true);
 		if (content.getPlayground1().equals(content.getEnemyPlayground())) {
 			content.setEnemyPlayground(content.getPlayground2());
 			content.setEnemyPlayer(content.getPlayer2());
 			content.setOwnPlayground(content.getPlayground1());
 			content.setActivePlayer(content.getPlayer1());
 			content.setActiveAI(content.getPlayer1AI());
 		}
 		else
 		{
 			content.setEnemyPlayground(content.getPlayground1());
 			content.setEnemyPlayer(content.getPlayer1());
 			content.setOwnPlayground(content.getPlayground2());
 			content.setActivePlayer(content.getPlayer2());
 			content.setActiveAI(content.getPlayer2AI());
 		}
 	}
 	/**
 	 * Check if a player switched after a shot
 	 * @return true if player switched, false if not
 	 */
 	public boolean switchedPlayer() {
 		return content.getSwitchedPlayer();
 	}
 }
