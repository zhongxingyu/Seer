 package se.chalmers.tda367.std.core;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.List;
 
 import javax.swing.Timer;
 import se.chalmers.tda367.std.core.GameController.EnemyOnBoard;
 import se.chalmers.tda367.std.core.tiles.IWalkableTile;
 import se.chalmers.tda367.std.core.tiles.PathTile;
 import se.chalmers.tda367.std.core.tiles.enemies.IEnemy;
 import se.chalmers.tda367.std.core.tiles.towers.AbstractAttackTower;
 import se.chalmers.tda367.std.core.tiles.towers.ITower;
 import se.chalmers.tda367.std.utilities.Position;
 import se.chalmers.tda367.std.utilities.Sprite;
 
 
 
 /**
  * The class that contains the game logic and controls the game.
  * @author Johan Andersson
  * @date Mar 22, 2012
  */
 public class GameController {
 	
 	private Player player;
 	private Wave wave;
 	private GameBoard board;
 	private Timer gameLoop;
 	private Timer releaseTimer;
 	private boolean placeSuccessful = true;
 	private WaveItem nextEnemy;
 	private ArrayList<EnemyOnBoard> enemiesOnBoard = new ArrayList<EnemyOnBoard>();
 	private ArrayList<TowerOnBoard> towersOnBoard = new ArrayList<TowerOnBoard>();
 	
 	
 	
 	public GameController(){
 		
 	}
 	
 	private void init(){
 		gameLoop.addActionListener(new GameLoopListener());
 		releaseTimer.addActionListener(new ReleaseTimerListener());
 	}
 	
 	/**
 	 * Starts a new game
 	 */
 	public void startGame(){
 		
 	}
 	/**
 	 * Ends the running game
 	 */
 	public void endGame(){
 		
 	}
 	
 	/**
 	 * Releases the next enemy in queue from the wave
 	 */
 	public void releaseEnemy(){
 		if(placeSuccessful == true){
 			nextEnemy = wave.getNext();
 		}
 		if(board.getTileAt(board.getStartPos()) instanceof IWalkableTile && nextEnemy != null){
 			//TODO Look over the if-statement above, cleaner solution?
 			board.placeTile(nextEnemy.getEnemy(), board.getStartPos());
 			enemiesOnBoard.add(nextEnemy);
 			placeSuccessful = true;
 		} else {
 			placeSuccessful = false;
 		}
 	}
 	
 	/**
 	 * 
 	 * @param tower - Tower to be placed.
 	 * @param pos - Position to place tower on.
 	 */
 	public void buildTower(ITower tower, Position pos){
 		//TODO Implement
 	}
 	
 	/**
 	 * Moves all the enemies on the GameBoard, towards the base.	
 	 */
 	public void moveEnemies(){
 		Collections.sort(enemiesOnBoard);
 		for (EnemyOnBoard eob : enemiesOnBoard) {
 			moveEnemy(eob);
 		}
 	}
 	
 	private void moveEnemy(EnemyOnBoard eob) {
 		Position tmp = eob.getPos().move(1, 0);
 		if(board.getTileAt(tmp) instanceof IWalkableTile){
 			placeEnemyOnBoard(eob, tmp);
 		} else if(board.getTileAt(tmp = eob.getPos().move(1, 1)) instanceof IWalkableTile){
 			placeEnemyOnBoard(eob, tmp);
 		} else if(board.getTileAt(tmp = eob.getPos().move(1, -1)) instanceof IWalkableTile){
 			placeEnemyOnBoard(eob, tmp);
 		}
 	}
 	
 	private void placeEnemyOnBoard(EnemyOnBoard eob, Position pos){
 		board.placeTile(eob.getEnemy(), pos);
 		board.placeTile(new PathTile(new Sprite()), eob.getPos());
 	}
 
 	/**
 	 * Towers fires at enemies in range.
 	 */
 	public void shootAtEnemiesInRange(){
 		for(TowerOnBoard tob : towersOnBoard){
			shootAtEnemyClosestToBase(tob, board.getEnemiesInRadius(tob.getPos(), tob.getTower().getRadius()));
 		}
 	}
 	
 	private void shootAtEnemyClosestToBase(TowerOnBoard tob, List<IEnemy> list){
 		list.get(0).decreaseHealth(tob.getTower().getDmg());
 	}
 	
 	/**
 	 * The loop that updates the whole game
 	 */
 	public void updateGame(){
 		
 	}
 	
 	//Inner class representing an enemy placed on the board.
 	class EnemyOnBoard implements Comparable<EnemyOnBoard> {
 		IEnemy enemy;
 		Position pos;
 		
 		public Position getPos() {
 			return pos;
 		}
 		
 		public IEnemy getEnemy() {
 			return enemy;
 		}
 
 		@Override
 		public int compareTo(EnemyOnBoard o) {
 			return Integer.compare(pos.getX(), o.pos.getX());
 		}
 	}
 	
 	class TowerOnBoard {
 		AbstractAttackTower tower;
 		Position pos;
 		
 		
 		public AbstractAttackTower getTower() {
 			return tower;
 		}
 		
 		public Position getPos() {
 			return pos;
 		}
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
 			// TODO Auto-generated method stub
 			
 		}
 		
 	}
 
 }
