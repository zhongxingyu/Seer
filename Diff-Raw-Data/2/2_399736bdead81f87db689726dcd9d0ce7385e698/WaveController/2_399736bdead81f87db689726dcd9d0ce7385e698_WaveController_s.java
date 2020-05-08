 package se.chalmers.tda367.std.core;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import javax.swing.Timer;
 
 import se.chalmers.tda367.std.core.effects.IEffect;
 import se.chalmers.tda367.std.core.tiles.IBoardTile;
 import se.chalmers.tda367.std.core.tiles.IWalkableTile;
 import se.chalmers.tda367.std.core.tiles.PlayerBase;
 import se.chalmers.tda367.std.core.tiles.enemies.IEnemy;
 import se.chalmers.tda367.std.core.tiles.towers.AbstractAttackTower;
 import se.chalmers.tda367.std.core.tiles.towers.ITower;
 import se.chalmers.tda367.std.utilities.Position;
 
 
 /**
  * The class that contains the game logic for wave phase of the game.
  * @author Johan Andersson
  * @modified 
  * @date Apr 22, 2012
  */
 
 public class WaveController {
 
 	private GameBoard board;
 	private Player player;
 	private Timer gameLoop;
 	private Timer releaseTimer;
 	private WaveItem nextEnemy;
 	private PlayerBase base; //TODO where to place base in code.
 	private Wave wave;
 	private ArrayList<EnemyItem> enemies = new ArrayList<EnemyItem>();
 
 	public WaveController(GameBoard board, Player player) {
 		this.board = board;
 		this.player = player;
 		init();
 	}
 
 	private void init(){
 		gameLoop = new Timer(1000, new GameLoopListener());
 		releaseTimer = new Timer(1500, new ReleaseTimerListener());
 		base = new PlayerBase(2); //TODO, where to place base in code?
 	}
 
 	/**
 	 * Starts a new wave
 	 */
 	public void startWave(Wave wave){
 		this.wave = wave;
 		gameLoop.start();
 		releaseTimer.start();
 	}
 
 	/**
 	 * Ends the running wave
 	 */
 	public void endWave(){
 		gameLoop.stop();
 		releaseTimer.stop();
 	}
 
 	/**
 	 * Releases the next enemy in queue from the wave
 	 */
 	public void releaseEnemy(){
 
 		if(nextEnemy == null){
 			nextEnemy = wave.getNext();
 		}
 
 		if(board.getTileAt(board.getStartPos()) instanceof IWalkableTile && nextEnemy != null){
 			//TODO Look over the if-statement above, cleaner solution?
 			addEnemy(nextEnemy);
 			nextEnemy =  wave.getNext();
 			if(nextEnemy != null){
 				releaseTimer.setInitialDelay(nextEnemy.getDelay());
 				releaseTimer.restart();
 			}
 		}
 	}
 
 	private void addEnemy(WaveItem wi){
		enemies.add(new EnemyItem(wi.getEnemy(), board.getStartPos());
 	}
 
 	/**
 	 * Moves all the enemies on the GameBoard, towards the base.	
 	 */
 	public void moveEnemies(){
 		Collections.sort(enemies);
 		for (EnemyItem ei : enemies) {
 			ei.getEnemy().moveEnemy();
 			//moveEnemy(eob);
 		}
 	}
 
 	/**
 	 * Towers fires at enemies in range.
 	 */
 	public void shootAtEnemiesInRange(){
 		for(int x = 0; x < board.getWidth(); x++){
 			for(int y = 0; y <board.getHeight(); y++){
 				IBoardTile tile = board.getTileAt(x, y);
 				if(tile instanceof AbstractAttackTower){
 					shoot((AbstractAttackTower)tile, new Position(x, y));
 				}
 			}
 		}
 	}
 	
 	
 	//Tower shoot at enemies in range.
 	private void shoot(AbstractAttackTower attackTower, Position pos) {
 		List<IEnemy> enemies = board.getEnemiesInRadius(pos, attackTower.getRadius());
 		//TODO, make more advance logic.
 		enemies.get(0).decreaseHealth(attackTower.getDmg());
 		for(IEffect ie:attackTower.getEffects()){
 			enemies.get(0).addEffect(ie);
 		}
 	}
 
 	/**
 	 * The loop that updates the whole game
 	 */
 	public void updateGame(){
 		moveEnemies();
 		shootAtEnemiesInRange();
 		applyEffects();
 		removeDeadEnemies();
 		checkIfPlayerAlive();
 	}
 
 	private void applyEffects() {
 		for (EnemyItem ei : enemies) {
 			applyEffect(ei);
 		}
 	}
 
 	private void applyEffect(EnemyItem ei) {
 		IEnemy enemy = ei.getEnemy();
 		for (IEffect ie : enemy.getEffects()) {
 			//TODO continue this
 			
 		}
 	}
 
 	private void checkIfPlayerAlive(){
 		if(base.getHealth() <= 0){
 			playerDead();
 		}
 	}
 
 	private void playerDead(){
 		System.out.println("Player dead, game over");
 		endWave();
 	}
 
 	private void removeDeadEnemies(){
 		for(int i = enemies.size(); 0 < i; i--)
 			if(enemies.get(i).getEnemy().getHealth() <= 0){
 				enemies.remove(i);
 			}
 	}
 	
 	/** Return enemies that is currently active.
 	 * 
 	 * @return - List of enemies.
 	 */
 	public ArrayList<EnemyItem> getEnemies() {
 		return enemies;
 	}
 
 
 
 
 	class ReleaseTimerListener implements ActionListener {
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			releaseEnemy();
 		}
 
 	}
 
 
 	class GameLoopListener implements ActionListener {
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			updateGame();
 		}
 	}
 
 
 
 
 }
