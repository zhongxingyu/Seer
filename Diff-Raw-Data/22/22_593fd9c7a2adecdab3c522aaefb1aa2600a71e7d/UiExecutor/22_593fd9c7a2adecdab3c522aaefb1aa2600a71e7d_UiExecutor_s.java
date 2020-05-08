 package yuuki.ui;
 
 /**
  * The class that performs the game logic as requested by the user interface.
  * It is not guaranteed that a worker thread will be spawned to call any of
  * these methods, so if thread safety is desired, the implementation must
  * handle it.
  */
 
 public interface UiExecutor {
 	
 	/**
 	 * Requests that a battle be initialized and its intro shown if main. The
 	 * main battle is shown on screen, but there can only be one at a time.
 	 * Non-main battles are run through as fast as possible. Battles that
 	 * include the player character as a participant cannot be non-main.
 	 * 
 	 * @param visible Whether the battle should be considered the main battle.
 	 */
 	public void requestBattle(boolean isMain);
 	
 	/**
 	 * Requests that the main battle be run through.
 	 */
 	public void requestBattleStart();
 	
 	/**
 	 * Requests that a player character is created.
 	 * 
 	 * @param name The name of the player character.
 	 * @param level The level of the player character.
 	 */
 	public void requestCharacterCreation(String name, int level);
 	
 	/**
 	 * Called when the options have been submitted.
 	 */
 	public void requestOptionsSubmission();
 	
 	/**
 	 * Requests that an old game be loaded.
 	 */
 	public void requestLoadGame();
 	
 	/**
 	 * Requests that a new game be started.
 	 */
 	public void requestNewGame();
 	
 	/**
 	 * Requests that the options screen be shown.
 	 */
 	public void requestOptionsScreen();
 	
 	/**
 	 * Requests that the game be exited.
 	 */
 	public void requestQuit();
 }
