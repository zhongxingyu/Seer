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
 import model.MainModel;
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
 	
 	private MenuController menuController;
 	private MainModel mainModel;
 	private GameModel gameModel;
 	private GamePanel gamePanel;
 	private Input input;
 	
 	private long startTime;
 	private long totalTimePaused;
 	private long lastTimePaused;
 
 	private volatile boolean paused = false;
 	private volatile boolean isRunning = true;
 	
 	private int nbrOfUpdates;
 	private int suppliesTick;
 	private int foodTicks;
 	private int enemySpawnTick;
 	private AI ai;
 	
 	private int lastSecondStamp;
 	
 	private static final int MAX_AMOUNT_OF_SPRITES = 100;
 	private static final int SUPPLY_SPAWN_DISTANCE = 25;
 	
 	/**
 	 * When food level is higher or equal to FOOD_HIGH the health of the player increases.
 	 */
 	public final static int FOOD_HIGH = 71;
 	/**
 	 * When food level is lower or equal to FOOD_LOW the health of the player increases.
 	 */
 	public final static int FOOD_LOW = 29;
 	
 	private final static int ENEMY_SPAWN_DISTANCE = 25;
 	
 	/**
 	 * Creates a new GameController.
 	 * @param mainModel the mainModel to listen to.
 	 */
 	public GameController(MainModel mainModel, MenuController m){
 		this.menuController=m;
 		this.mainModel = mainModel;
 		this.mainModel.addListener(this);
 	}
 	
 	/**
 	 * Initialises the controller and starts the game.
 	 */
 	public void init() {
 		this.setStartTime(gameModel.getGameTime());
 
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
				wait(SLEEP);
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
 		float distance = (float) Math.sqrt(dx*dx + dy*dy);
 		if(distance < 0.9f) {
 			dx = gameModel.getPlayer().getCenter().getX() - x;
 			dy = gameModel.getPlayer().getCenter().getY() - y;
 		}
 		
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
 		if(enemySpawnTick >= 20 && this.gameModel.getWorld().getSprites().size() < 
 				MAX_AMOUNT_OF_SPRITES){
 			spawnEnemy();
 			enemySpawnTick = 0;
 		}
 		ai.updateEnemies();
 
 		suppliesTick++;
 		if(suppliesTick >= 20){
 			spawnSupplies();
 			suppliesTick = 0;
 		}
 
 		//reducePlayerFoodLevel and changes the player's health according to current food level
 		foodTicks++;
 		if(foodTicks >= 40){
 			reducePlayerFood();
 			foodTicks = 0;
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
 			Weapon newWeapon = gameModel.getWorld().getIntersectedWeapon(gameModel.getPlayer().
 					getHitBox());
 			gameModel.getPlayer().pickUpWeapon(newWeapon);
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
 		gameModel.getWorld().fireEvent(GameModel.ADDED_SUPPLY, null, w);
 	}
 	
 	/**
 	 * adds a supply to a given tile
 	 * @param t the tile given
 	 */
 	private void spawnSupplies(Tile t){
 		Supply supply;
 		float dx = t.getPosition().getX() - this.gameModel.getPlayer().getCenter().getX();
 		float dy = t.getPosition().getY() - this.gameModel.getPlayer().getCenter().getY();
 		if(Math.sqrt(dx*dx + dy*dy) > SUPPLY_SPAWN_DISTANCE) {
 			if(t.getProperty() == Tile.FOOD_SPAWN && Math.random() > 0.8){//Create a food
 				supply = SupplyFactory.createFood(25, t.getPosition());
 				gameModel.getWorld().getItems().add(supply);
 				gameModel.getWorld().fireEvent(GameModel.ADDED_SUPPLY, null, supply);
 			}else if(t.getProperty() == Tile.AMMO_SPAWN && Math.random() > 0.85){//Create an ammo
 				supply = SupplyFactory.createAmmo(12, t.getPosition());
 				gameModel.getWorld().getItems().add(supply);
 				gameModel.getWorld().fireEvent(GameModel.ADDED_SUPPLY, null, supply);
 			}else if(t.getProperty() == Tile.HEALTH_SPAWN && Math.random() > 0.9){//Create a health
 				supply = SupplyFactory.createHealth(25, t.getPosition());
 				gameModel.getWorld().getItems().add(supply);
 				gameModel.getWorld().fireEvent(GameModel.ADDED_SUPPLY, null, supply);
 			}else if(t.getProperty() == Tile.WEAPON_SPAWN && Math.random() > 0.9){//create a weapon
 				Weapon w = WeaponFactory.getSemiRandomWeapon(this.getTotalRuntime());
 				w.setPosition(t.getPosition());
 				gameModel.getWorld().getItems().add(w);
 				gameModel.getWorld().fireEvent(GameModel.ADDED_SUPPLY, null, w);
 			}	
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
 		do{
 			spawnPos = new Position((int)(Math.random()*gameModel.getWorld().getWidth()) +0.5f, 
 					(int)(Math.random()*gameModel.getWorld().getHeight()) +0.5f);
 		}while(Math.abs(gameModel.getPlayer().getPosition().getX() - spawnPos.getX()) <= ENEMY_SPAWN_DISTANCE && 
 				Math.abs(gameModel.getPlayer().getPosition().getY() - spawnPos.getY()) <= ENEMY_SPAWN_DISTANCE);
 
 		Tile[][] tiles = gameModel.getWorld().getTiles();
 		if(gameModel.getWorld().canMove(
 					new Position(spawnPos.getX()-0.5f , spawnPos.getY()-0.5f), 
 					new Position(spawnPos.getX()+0.5f , spawnPos.getY()+0.5f), true)
 				&& gameModel.getWorld().canMove(
 						new Position(spawnPos.getX()-0.5f , spawnPos.getY()+0.5f), 
 						new Position(spawnPos.getX()+0.5f , spawnPos.getY()-0.5f), true)
 						&& tiles[(int)spawnPos.getX()][(int)spawnPos.getY()].getProperty() != Tile.UNWALKABLE){
 			if((int)getTotalRuntime()/1000 < 90){
 				gameModel.getWorld().addSprite(EnemyFactory.createEasyEnemy(spawnPos));
 			}else if((int)getTotalRuntime()/1000 < 240){
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
 	 * Reduce the player's food level once every call. If the food level
 	 * is higher or equal to FOOD_HIGH also increases the player's health by one.
 	 * If the food level is lower or equal to FOOD_LOW also decreases the player's
 	 * health by one. The player will not go below two health from low food level.
 	 */
 	private void reducePlayerFood(){
 		if(gameModel.getPlayer().getFood() <= FOOD_LOW){
 			if(gameModel.getPlayer().getHealth() > 2){
 				gameModel.getPlayer().reduceHealth(1);
 			}//The player will will not get less than one health from low food.
 		}else if(gameModel.getPlayer().getFood() >= FOOD_HIGH){
 			gameModel.getPlayer().increaseHealth(1);
 		}
 		gameModel.getPlayer().removeFood(1);
 	}
 
 	/*
 	 * Add the score to the highscoreModel, show's the gameOverPanel and stop the thread.
 	 */
 	private void gameOver(){
 		mainModel.getScoreModel().addNewScore(gameModel.getScore());
 		menuController.showGameOverPanel();
 		this.stopThread();
 	}
 	
 	private void checkPauseGame(){
 		if(input.isPressed(KeyEvent.VK_ESCAPE) || input.isPressed(KeyEvent.VK_P)){
 			input.reset();
 			pauseThread();
 			menuController.showPauseMenu();
 			return;
 		}
 	}
 
 	@Override
 	public void propertyChange(PropertyChangeEvent evt) {
 		if(evt.getPropertyName().equals(IGamePanel.MOUSE_INPUT)) {
 			Position msPos = (Position)evt.getNewValue();
 			this.handleMouseAt(msPos.getX(), msPos.getY());
 		}else if(evt.getPropertyName().equals(MainModel.EVENT_NEW_GAME_MODEL)){
 			this.gameModel = mainModel.getGameModel();
 		}
 	}
 	
 }
