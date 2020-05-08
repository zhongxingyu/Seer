 package controller;
 
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseEvent;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.util.Calendar;
 
 import javax.swing.JPanel;
 
 
 import view.GamePanel;
 import view.IGamePanel;
 
 import model.GameModel;
 import model.geometrical.Position;
 import model.items.Item;
 import model.items.Supply;
 import model.items.SupplyFactory;
 import model.items.weapons.Projectile;
 import model.items.weapons.Weapon;
 import model.items.weapons.WeaponFactory;
 import model.pathfinding.AI;
 import model.sprites.EnemyFactory;
 import model.sprites.Player;
 import model.world.Tile;
 
 /**
  * Controls a specific GameModel.
  * 
  * @author
  *
  */
 public class GameController implements Runnable, PropertyChangeListener {
 
 	private static final int SLEEP = 1000 / 45;
 	
 	private GameModel gameModel;
 	private GamePanel gamePanel;
 	private Input input;
 	
 	private long startTime = 0;
 	private long totalTimePaused = 0;
 	private long lastTimePaused = 0;
 
 	private volatile boolean paused = false;
 	private volatile boolean isRunning = true;
 	
 	private int nbrOfUpdates;
 	private int suppliesTick = 0;
 	private int foodTicks;
 	private int enemySpawnTick;
 	private AI ai;
 	
 	private int lastSecondStamp;
 	
 	/**
 	 * When food level is higher or equal to FOOD_HIGH the health of the player increases.
 	 */
 	public final static int FOOD_HIGH = 71;
 	/**
 	 * When food level is lower or equal to FOOD_LOW the health of the player increases.
 	 */
 	public final static int FOOD_LOW = 29;
 	
 	
 	/**
 	 * Creates a new GameController. The controller will not
 	 * start in a running state so the method<code>start()<code>
 	 * need to be called before the execution starts.
 	 */
 	public GameController(){
 		
 	}	
 
 	public void init(GameModel model) {
 		this.gameModel = model;
 		this.setStartTime(model.getGameTime());
 
 		input = new Input();	
 
 		gamePanel = new GamePanel(gameModel);
 		gamePanel.addListener(this);
 
 		input.setContainer(gamePanel);
 		gameModel.addListener(gamePanel);
 		ai = new AI(gameModel.getWorld(), gameModel.getPlayer());
 	}
 
 	@Override
 	public void run() {
 		new Thread(gamePanel).start();
 		while (isRunning){
 			runThread();
 		}
 	}
 	private synchronized void runThread(){
 		if (!paused) {
 			update();
 			nbrOfUpdates++;
 			try{
 				Thread.sleep(SLEEP);
 			}catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 		} else {
 			try{
 				wait();
 			}catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	/**
 	 * Gives the game model this controller controls.
 	 * @return the game model this controller controls.
 	 */
 	public GameModel getGameModel() {
 		return this.gameModel;
 	}
 	
 	/**
 	 * Returns the GamePanel that is responsible of displaying this controller's graphics.
 	 * @return the GamePanel that is responsible of displaying this controller's graphics.
 	 */
 	public JPanel getGamePanel(){
 		return gamePanel;
 	}
 	
 	/**
 	 * The Player fires his weapon.
 	 */
 	public void playerShoot(){
 		Player player = this.gameModel.getPlayer();
 		Projectile p = player.getActiveWeapon().createProjectile(player.getDirection(), 
 				player.getProjectileSpawn());
 		if(p != null) {
 //			p.setOwner(player);
 			this.gameModel.getWorld().addProjectile(p);
 			player.fireEvent(Player.EVENT_USE_WEAPON);
 		}
 	}
 	
 	/**
 	 * Gives the number of updates since start.
 	 * @return the number of updates since start.
 	 */
 	public int getNumbersOfUpdates() {
 		return nbrOfUpdates;
 	}
 	
 	/**
 	 * Gives the time in milliseconds the controller has existed.
 	 * @return the time in milliseconds the controller has existed.
 	 */
 	public long getMsSinceStart() {
 		return timeNow()-startTime;
 	}
 	
 	/**
 	 * Sets the direction of the player towards the mouse' position.
 	 * @param x The x-coordinate of the mouse' position.
 	 * @param y The y-coordinate of the mouse' position.
 	 */
 	private void handleMouseAt(float x, float y) {
 		float dx = gameModel.getPlayer().getProjectileSpawn().getX() - x;
 		float dy = gameModel.getPlayer().getProjectileSpawn().getY() - y;
 		float dir = (float)Math.atan(-dy/dx);
 
 		if(dx < 0) {
 			dir += (float)(Math.PI);
 		}
 		gameModel.getPlayer().setDirection(dir + (float)Math.PI);
 	}
 	
 	private void setStartTime(long gameTime) {
 		this.startTime = this.timeNow() - gameTime;
 	}
 		
 	/**
 	 * Updates the model which this controller is responsible for.
 	 */
 	public void update() {
 		//Pause game
 		checkPauseGame();
 		
 		this.updatePlayerPosition();
 		if(input.mousePressed(MouseEvent.BUTTON1)){
 			this.playerShoot();
 		}
 		if(input.isPressed(KeyEvent.VK_R)){
 			gameModel.getPlayer().reloadActiveWeapon();
 		}
 		playerSwitchWeapon();
 		playerPickUpWeapon();
 
 		enemySpawnTick++;
 		if(enemySpawnTick >= 5){
 			spawnEnemy();
 			enemySpawnTick = 0;
 		}
 		ai.updateEnemies();
 
 		suppliesTick++;
 		if(suppliesTick >= 600){
 			spawnSupplies();
 			suppliesTick = 0;
 		}
 
 		//reducePlayerFoodLevel and changes the player's health according to current food level
 		foodTicks++;
 		if(foodTicks >= 120){
 			reducePlayerFood();
 		}
 		
 		if(gameModel.getPlayer().getHealth() <= 0){
 			gameOver();
 		}
 		
 		int newTime = (int) (this.getTotalRuntime() / 1000);
 		if(newTime != lastSecondStamp) {
 			this.gameModel.addScore(1);
 			lastSecondStamp = newTime;
 		}
 		
 		this.gameModel.setGameTime(this.getTotalRuntime());
 		
 		gameModel.update();
 		
 		//Write dev data to console
 //		System.out.println("Number of updates since start (ctr): " + this.getNumbersOfUpdates() 
 //				+ ", average: " + this.getNumbersOfUpdates()/(int)(1 + this.getMsSinceStart()/1000) + "/s");
 	}
 	
 	/**
 	 * Calculates how long time the controller has executed. If the thread is paused the timer will also be paused.
 	 * @return the time the controller has been running.
 	 */
 	public long getTotalRuntime() {
 		return timeNow()-startTime-totalTimePaused;
 	}
 
 	private void playerPickUpWeapon(){
 		if(input.isPressed(KeyEvent.VK_G)){
 			Weapon oldWeapon = gameModel.getPlayer().getActiveWeapon();
 			gameModel.getWorld().playerPickUpWeapon();
 			Tile[][] t = gameModel.getWorld().getTiles();
 			input.resetKey(KeyEvent.VK_G);
 			if(oldWeapon.isDroppable()){//can't throw fists
 				t[(int) gameModel.getPlayer().getCenter().getX()]
 						[(int)gameModel.getPlayer().getCenter().getY()].setProperty(Tile.WEAPON_SPAWN);
 				spawnWeapon(t[(int) gameModel.getPlayer().getCenter().getX()]
 						[(int)gameModel.getPlayer().getCenter().getY()], oldWeapon);
 			}
 		}
 	}
 	
 	private void playerSwitchWeapon(){
 		if(input.isPressed(KeyEvent.VK_1)){
 			gameModel.getPlayer().switchWeapon(0);
 		}else if(input.isPressed(KeyEvent.VK_2)){
 			gameModel.getPlayer().switchWeapon(1);
 		}else if(input.isPressed(KeyEvent.VK_3)){
 			gameModel.getPlayer().switchWeapon(2);
 		}
 	}
 	
 	/**
 	 * Adds a Weapon to a given tile.
 	 * @param t the tile given
 	 */
 	private void spawnWeapon(Tile t, Weapon w){
 		w.setPosition(t.getPosition());
 		gameModel.getWorld().getItems().add(w);
 		gameModel.getWorld().fireEvent(GameModel.ADDED_SUPPLY, w);
 		System.out.println("Weapon supposed to spawn");
 	}
 	
 	/**
 	 * adds a supply to a given tile
 	 * @param t the tile given
 	 */
 	private void spawnSupplies(Tile t){
 		Supply supply;
 		if(t.getProperty() == Tile.FOOD_SPAWN){//Create a food
 			supply = SupplyFactory.createFood(25, t.getPosition());
 			gameModel.getWorld().getItems().add(supply);
 			gameModel.getWorld().fireEvent(GameModel.ADDED_SUPPLY, supply);
 		}else if(t.getProperty() == Tile.AMMO_SPAWN){//Create an ammo
 			supply = SupplyFactory.createAmmo(12, t.getPosition());
 			gameModel.getWorld().getItems().add(supply);
 			gameModel.getWorld().fireEvent(GameModel.ADDED_SUPPLY, supply);
 		}else if(t.getProperty() == Tile.HEALTH_SPAWN){//Create a health
 			supply = SupplyFactory.createHealth(25, t.getPosition());
 			gameModel.getWorld().getItems().add(supply);
 			gameModel.getWorld().fireEvent(GameModel.ADDED_SUPPLY, supply);
 		}else /*if(t.getProperty() == Tile.WEAPON_SPAWN)*/{//create a weapon
 			Weapon w = WeaponFactory.createRandomWeapon();
 			w.setPosition(t.getPosition());
 			gameModel.getWorld().getItems().add(w);
 			gameModel.getWorld().fireEvent(GameModel.ADDED_SUPPLY, w);
 			System.out.println("Weapon supposed to spawn");
 		}	
 	}
 	
 	
 	
 	/**
 	 * Updates the player's position.
 	 */
 	private void updatePlayerPosition() {
 		if(input.isPressed(KeyEvent.VK_W) && input.isPressed(KeyEvent.VK_D)
 				|| input.isPressed(KeyEvent.VK_UP) && input.isPressed(KeyEvent.VK_RIGHT)) {
 			gameModel.getPlayer().setMoveDir((float)(Math.PI/4));
 		}else if(input.isPressed(KeyEvent.VK_D) && input.isPressed(KeyEvent.VK_S)
 				|| input.isPressed(KeyEvent.VK_RIGHT) && input.isPressed(KeyEvent.VK_DOWN)) {
 			gameModel.getPlayer().setMoveDir((float)(-Math.PI/4));
 		}else if(input.isPressed(KeyEvent.VK_A) && input.isPressed(KeyEvent.VK_S)
 				|| input.isPressed(KeyEvent.VK_LEFT) && input.isPressed(KeyEvent.VK_DOWN)) {
 			gameModel.getPlayer().setMoveDir((float)(-Math.PI*3/4));
 		}else if(input.isPressed(KeyEvent.VK_W) && input.isPressed(KeyEvent.VK_A)
 				|| input.isPressed(KeyEvent.VK_UP) && input.isPressed(KeyEvent.VK_LEFT)) {
 			gameModel.getPlayer().setMoveDir((float)(Math.PI*3/4));
 		}else if(input.isPressed(KeyEvent.VK_W)
 				|| input.isPressed(KeyEvent.VK_UP)) {
 			gameModel.getPlayer().setMoveDir((float)(Math.PI/2));
 		}else if(input.isPressed(KeyEvent.VK_A)
 				|| input.isPressed(KeyEvent.VK_LEFT)) {
 			gameModel.getPlayer().setMoveDir((float)(Math.PI));
 		}else if(input.isPressed(KeyEvent.VK_S)
 				|| input.isPressed(KeyEvent.VK_DOWN)) {
 			gameModel.getPlayer().setMoveDir((float)(-Math.PI/2));
 		}else if(input.isPressed(KeyEvent.VK_D)
 				|| input.isPressed(KeyEvent.VK_RIGHT)) {
 			gameModel.getPlayer().setMoveDir(0f);
 		}else{
 			gameModel.getPlayer().setState(Player.State.STANDING);
 		}	
 	}
 	
 	/**
 	 * Pauses the thread from a running state. To resume the thread call <code>resumeThread()</code>.
 	 */
 	public synchronized void pauseThread(){
 		paused=true;
 		lastTimePaused=timeNow();
 		if (gamePanel != null){
 			gamePanel.pauseThread();
 		}
 	}
 	/**
 	 * Resumes the thread to a running state. To pause the thread call <code>pauseThread()</code>.
 	 */
 	public synchronized void resumeThread(){
 		paused=false;
 		notify();
 		totalTimePaused+=timeNow()-lastTimePaused;
		gameModel.setGameTime(this.getTotalRuntime());
 		if (gamePanel != null){
 			gamePanel.resumeThread();
 		}
 	}
 	/**
 	 * Stops the thread from executing further actions, this method is irreversible. 
 	 */
 	public synchronized void stopThread(){
 		isRunning=false;
 		notify();
 		if (gamePanel != null){
 			gamePanel.stopThread();
 		}
 	}
 	
 	/**
 	 * Returns the time now in milliseconds.
 	 * @return the time now in milliseconds.
 	 */
 	private long timeNow(){
 		return Calendar.getInstance().getTimeInMillis();
 	}
 	/**
 	 * 
 	 * @return the score of the game. Now it is the time the game has been running.
 	 */
 	public int getGameScore(){
 		return (int) getTotalRuntime()/1000;
 	}
 	/**
 	 * Spawns enemies with difficulties depending on how long the game has been running
 	 */
 	private void spawnEnemy(){
 		Position spawnPos;
 		do{//TODO maxDistance on enemySpawn?
 			spawnPos = new Position((int)(Math.random()*gameModel.getWorld().getWidth()) +0.5f, 
 			(int)(Math.random()*gameModel.getWorld().getHeight()) +0.5f);
 		}while(Math.abs(gameModel.getPlayer().getPosition().getX() - spawnPos.getX()) <= 25 && 
 				Math.abs(gameModel.getPlayer().getPosition().getY() - spawnPos.getY()) <= 25);
 
 		Tile[][] tiles = gameModel.getWorld().getTiles();
 		if(gameModel.getWorld().canMove(spawnPos, new Position(spawnPos.getX()+1 , spawnPos.getY()+1)) 
 				&& tiles[(int)spawnPos.getX()][(int)spawnPos.getY()].getProperty() != Tile.UNWALKABLE){
 			if((int)getTotalRuntime()/1000 < 120){
 				gameModel.getWorld().addSprite(EnemyFactory.createEasyEnemy(spawnPos));
 			}else if((int)getTotalRuntime()/1000 < 480){
 				gameModel.getWorld().addSprite(EnemyFactory.createMediumEnemy(spawnPos));
 			}else{
 				gameModel.getWorld().addSprite(EnemyFactory.createHardEnemy(spawnPos));
 			}
 		}else{
 			spawnEnemy();
 		}
 	}
 
 	/**
 	 * Calculates where the supply is spawned
 	 */
 	private void spawnSupplies(){
 		if(gameModel.getSpawnPoints() != null){
 			boolean tileOcuppied;
 			int rnd = (int)(Math.random()*gameModel.getSpawnPoints().size());
 			Tile t = gameModel.getSpawnPoints().get(rnd);
 			tileOcuppied = false;
 			for(Item i : gameModel.getWorld().getItems()){
 				if(i.getPosition().equals(t.getPosition())){
 					tileOcuppied = true;
 					break;
 				}
 			}
 			if(!tileOcuppied){
 				this.spawnSupplies(t);
 			}
 		}
 	}
 	
 	/*
 	 * Reduce the player's food level once every 120 updates. If the food level
 	 * is higher or equal to FOOD_HIGH also increases the player's health by one.
 	 * If the food level is lower or equal to FOOD_LOW also decreases the player's
 	 * health by one.
 	 */
 	private void reducePlayerFood(){
 		if(gameModel.getPlayer().getFood() <= FOOD_LOW){
 			gameModel.getPlayer().reduceHealth(1);
 		}else if(gameModel.getPlayer().getFood() >= FOOD_HIGH){
 			gameModel.getPlayer().increaseHealth(1);
 		}
 		gameModel.getPlayer().removeFood(1);
 		foodTicks = 0;
 	}
 
 	/*
 	 * Add the score to the highscoreModel, show's the gameOverPanel and stop the thread.
 	 */
 	private void gameOver(){
 		inputOutput.HighScoreModel.addNewScore(getTotalRuntime());
 		MenuController.showGameOverPanel();
 		this.stopThread();
 	}
 	
 	private void checkPauseGame(){
 		if(input.isPressed(KeyEvent.VK_ESCAPE) || input.isPressed(KeyEvent.VK_P)){
 			input.reset();
 			pauseThread();
 			MenuController.showPauseMenu();
 			return;
 		}
 	}
 
 	@Override
 	public void propertyChange(PropertyChangeEvent evt) {
 		if(evt.getPropertyName().equals(IGamePanel.MOUSE_INPUT)) {
 			Position msPos = (Position)evt.getNewValue();
 			this.handleMouseAt(msPos.getX(), msPos.getY());
 		}
 	}
 	
 }
