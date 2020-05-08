 package se.chalmers.tda367.std.core;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.Iterator;
 import java.util.List;
 import java.util.logging.Logger;
 
 import javax.swing.Timer;
 
 import se.chalmers.tda367.std.core.effects.IEffect;
 import se.chalmers.tda367.std.core.enemies.IEnemy;
 import se.chalmers.tda367.std.core.events.PlayerDeadEvent;
 import se.chalmers.tda367.std.core.events.WaveEndedEvent;
 import se.chalmers.tda367.std.core.tiles.IBoardTile;
 import se.chalmers.tda367.std.core.tiles.towers.IAttackTower;
 import se.chalmers.tda367.std.utilities.EventBus;
 import se.chalmers.tda367.std.utilities.Position;
 
 
 /**
  * The class that contains the game logic for wave phase of the game.
  * @author Johan Andersson
  * @modified Emil Edholm (May 13, 2012)
  * @modified Johan Gustafsson (May 12, 2012)
  * @date Apr 22, 2012
  */
 
 class WaveController {
 
 	/** The delay (in milliseconds) before the first enemy is placed on the game board */
 	private static final int INITIAL_WAVE_DELAY = 100;
 	
 	private GameBoard board;
 	private Player player;
 	private Timer releaseTimer;
 	private WaveItem nextEnemy;
 	private Wave wave;
 	private boolean waveHasBeenCompleted;
 	
 
 	public WaveController(GameBoard board, Player player) {
 		this.board = board;
 		this.player = player;
 		waveHasBeenCompleted = false;
 		releaseTimer = new Timer(INITIAL_WAVE_DELAY, new WaveReleaseTimerListener());
 	}
 	
 	// TODO: Add start/stop methods and send appropriate events when called.
 
 	/**
 	 * Starts a new wave
 	 */
 	public void startWave(Wave wave){
 		this.wave = wave;
 		releaseTimer.start();
 	}
 
 	/**
 	 * Stops the release of enemies from the current wave
 	 */
 	public void endWaveRelease(){
 		releaseTimer.stop();
 	}
 	
 	/**
 	 * The loop that updates the the wave related bits of the game.
 	 * Does things like moving the enemies, making the towers shoot at the enemies etc.
 	 * @param delta - the amount of time (in milliseconds) since the last update.
 	 */
 	public void updateWaveRelated(final int delta){
 		if(!isPlayerDead()) {
 			moveEnemies(delta);
 			shootAtEnemiesInRange(delta);
 			applyHealthEffects();
 			decreaseEffectsDuration(delta);
 		}
 	}
 
 	private void decreaseEffectsDuration(int delta) {
 		EnemyList enemies = board.getEnemies();
 
 		for(IEnemy enemy : enemies){
 			List<IEffect> effects = enemy.getEffects();
 
 			Iterator<IEffect> it = effects.iterator();
 			while(it.hasNext()){
 				IEffect effect = it.next();
 				effect.decrementDuration(delta);
 				if(effect.getDuration() < 0.001){
 					it.remove();
					enemy.removeEffect(effect);
 				}
 			}
 		}
 	}
 
 	
 	/**
 	 * Releases the next enemy in queue from the wave
 	 */
 	private void releaseEnemy(){
 		if(nextEnemy == null){
 			nextEnemy = wave.getNext();
 		}
 
 		if(nextEnemy != null){
 			addEnemy(nextEnemy);
 			nextEnemy = wave.getNext();
 			
 			if(nextEnemy != null){
 				releaseTimer.setInitialDelay(nextEnemy.getDelay());
 				releaseTimer.restart();
 			}
 		}else {
 			// Stop the timer when all enemies has been "released"
 			releaseTimer.stop();
 			waveHasBeenCompleted = true;
 		}
 	}
 
 	/**
 	 * Add a enemy to the game board from a {@code WaveItem}
 	 */
 	private void addEnemy(WaveItem wi){
 		EnemyList enemies = board.getEnemies();
 		enemies.add(wi.getEnemy());
 	}
 
 	/**
 	 * Moves all the enemies on the GameBoard, towards the base.
 	 * @param delta - the amount of time (in milliseconds) since the last update.	
 	 */
 	private void moveEnemies(final int delta){
 		EnemyList enemies = board.getEnemies();
 		if(enemies.isEmpty() && waveHasBeenCompleted) {
 			EventBus.INSTANCE.post(new WaveEndedEvent());
 			waveHasBeenCompleted = false;
 			return;
 		}
 		
 		for(IEnemy enemy : enemies) {
 			enemy.moveTowardsWaypoint(delta);
 		}
 	}
 	
 	/**
 	 * Towers fires at enemies in range.
 	 * @param delta - the amount of time (in milliseconds) since the last update.
 	 */
 	private void shootAtEnemiesInRange(final int delta){
 		int tileScale = Properties.INSTANCE.getTileScale();
 		for(int x = 0; x < board.getWidth(); x++){
 			for(int y = 0; y <board.getHeight(); y++){
 				IBoardTile tile = board.getTileAt(x, y);
 				if(tile instanceof IAttackTower){
 					IAttackTower attackTower = (IAttackTower) tile;
 					if(attackTower.isAttackReady(delta)) {
 						shoot(attackTower, new Position(x*tileScale, y*tileScale));
 					}
 				}
 			}
 		}
 	}
 	
 	
 	/**
 	 * Handles the individual shots.
 	 */
 	private void shoot(IAttackTower tile, Position pos) {
 		int radius = tile.getRadius() * Properties.INSTANCE.getTileScale();
 		List<IEnemy> enemies = board.getEnemiesInRadius(pos, radius);
 		tile.shoot(enemies, pos);
 	}
 
 	/**
 	 * Apply the effects on the enemies
 	 */
 	private void applyHealthEffects() {
 		EnemyList enemies = board.getEnemies();
 		
 		for (IEnemy enemy : enemies) {
 			applyHealthEffect(enemy);
 		}
 	}
 
 	
 	private void applyHealthEffect(IEnemy enemy) {
 		double healthModifier = 1.0;
 		for (IEffect ie : enemy.getEffects()) {
 			healthModifier = healthModifier * ie.getHealthModifier();
 		}
 		double health = enemy.getHealth() * healthModifier;
 		enemy.decreaseHealth(enemy.getHealth()-(int)health);
 	}
 
 	/** Whether or not the player has died */
 	private boolean isPlayerDead(){
 		if(board.getPlayerBaseHealth() <= 0){
 			playerDead();
 			return true;
 		}
 		return false;
 	}
 
 	/** What to do when the player is dead. */
 	private void playerDead(){
 		Logger.getLogger("se.chalmers.tda367.std.core").info("Player dead, game over");
 		EventBus.INSTANCE.post(new PlayerDeadEvent(player));
 		EventBus.INSTANCE.post(new WaveEndedEvent());
 	}
 	
 	private class WaveReleaseTimerListener implements ActionListener {
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			releaseEnemy();
 		}
 
 	}
 }
