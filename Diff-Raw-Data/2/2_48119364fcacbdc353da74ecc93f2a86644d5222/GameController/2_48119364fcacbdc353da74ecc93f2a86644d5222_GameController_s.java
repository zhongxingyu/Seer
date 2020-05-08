 package se.chalmers.tda367.std.core;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.List;
 import java.util.concurrent.ConcurrentLinkedQueue;
 
 import javax.swing.Timer;
 import se.chalmers.tda367.std.core.GameController.EnemyOnBoard;
 import se.chalmers.tda367.std.core.tiles.AbstractTile;
 import se.chalmers.tda367.std.core.tiles.IBoardTile;
 import se.chalmers.tda367.std.core.tiles.IWalkableTile;
 import se.chalmers.tda367.std.core.tiles.PathTile;
 import se.chalmers.tda367.std.core.tiles.enemies.BasicEnemy;
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
 	private WaveItem nextEnemy;
 	private ArrayList<EnemyOnBoard> enemiesOnBoard = new ArrayList<EnemyOnBoard>();
 	private ArrayList<TowerOnBoard> towersOnBoard = new ArrayList<TowerOnBoard>();
 	
 	
 	
 	public GameController(Player p, GameBoard b){
 		this.player = p;
 		this.board = b;
 		init();
 	}
 	
 	private void init(){
 		gameLoop = new Timer(1000, new GameLoopListener());
 		releaseTimer = new Timer(1500, new ReleaseTimerListener());
 		//dummywave
		this.wave = createBasicWave(2);
 		
 	}
 	
 	//Method to create a dummywave
 	private Wave createBasicWave(int n){
 		ConcurrentLinkedQueue<WaveItem> q = new ConcurrentLinkedQueue<WaveItem>();
 		for(int i = 0; i<n; i++){
 			q.add(new WaveItem(new BasicEnemy(), i*5000));
 		}
 		return new Wave(q);
 	}
 	
 	
 	
 	
 	
 	/**
 	 * Starts a new game
 	 */
 	public void startGame(){
 		gameLoop.start();
 		releaseTimer.start();
 	}
 	/**
 	 * Ends the running game
 	 */
 	public void endGame(){
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
 			board.placeTile(nextEnemy.getEnemy(), board.getStartPos());
 			enemiesOnBoard.add(new EnemyOnBoard(nextEnemy, board.getStartPos()));
 			nextEnemy =  wave.getNext();
 			if(nextEnemy != null){
 				releaseTimer.setInitialDelay(nextEnemy.getDelay());
 				releaseTimer.restart();
 				System.out.println("Next enemy in: "+nextEnemy.getDelay());
 			}
 		} 
 	}
 	
 	/**
 	 * 
 	 * @param tower - Tower to be placed.
 	 * @param pos - Position to place tower on.
 	 */
 	public void buildTower(ITower tower, Position pos){
 		board.placeTile(tower, pos);
 		if(tower instanceof AbstractAttackTower){
 			towersOnBoard.add(new TowerOnBoard((AbstractAttackTower)tower, pos));
 		}
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
 	/*
 	private void moveEnemy(EnemyOnBoard eob) {
 		Position tmp = eob.getPos().move(1, 0);
 		if(board.getTileAt(tmp) instanceof IWalkableTile){
 			placeEnemyOnBoard(eob, tmp);
 			eob.setPos(tmp);
 		} else if(board.getTileAt(tmp = eob.getPos().move(1, 1)) instanceof IWalkableTile){
 			placeEnemyOnBoard(eob, tmp);
 			eob.setPos(tmp);
 		} else if(board.getTileAt(tmp = eob.getPos().move(1, -1)) instanceof IWalkableTile){
 			placeEnemyOnBoard(eob, tmp);
 			eob.setPos(tmp);
 		}
 	}*/
 	/**
 	 * Writing a new moveEnemy to try some pathfinding
 	 */
 	private void moveEnemy(EnemyOnBoard eob){
 		ArrayList<PathTile> pathList = new ArrayList<PathTile>();
 		IBoardTile[] tiles = new IBoardTile[8];
 		tiles[0] = board.getTileAt(eob.getPos().move(1, 0)); 
 		tiles[1] = board.getTileAt(eob.getPos().move(0, -1));
 		tiles[2] = board.getTileAt(eob.getPos().move(-1, 0));
 		tiles[3] = board.getTileAt(eob.getPos().move(0, 1));
 		
 		tiles[4] = board.getTileAt(eob.getPos().move(1, -1));
 		tiles[5] = board.getTileAt(eob.getPos().move(-1, -1));
 		tiles[6] = board.getTileAt(eob.getPos().move(1, 1));
 		tiles[7] = board.getTileAt(eob.getPos().move(-1, 1));
 		for(int i = 0; i < 8;i++){
 			if(tiles[i] instanceof IWalkableTile){
 				PathTile tmp = (PathTile)tiles[i];
 				pathList.add(tmp);
 			}
 		}
 		Collections.sort(pathList);
 		for(int i = pathList.size()-1;i >= 0;i--){
 			System.out.println(pathList.get(i).getTileValue()+" "+board.getMap().getValueAtPos(eob.getPos()));
 			if( (pathList.get(i).getTileValue() - board.getMap().getValueAtPos(eob.getPos())) == 1){
 				placeEnemyOnBoard(eob, pathList.get(i).getPos());
 				eob.setPos(pathList.get(i).getPos());
 				break;
 			}
 		}
 		
 	}
 	
 	
 	//Moves the enemy a step.
 	private void placeEnemyOnBoard(EnemyOnBoard eob, Position pos){
 		board.placeTile(eob.getEnemy(), pos);
 		board.placeTile(new PathTile(new Sprite(), board.getMap().getValueAtPos(eob.getPos())), eob.getPos());
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
 		if(list.size() > 0){
 		IEnemy enemy = list.get(0);
 		enemy.decreaseHealth(tob.getTower().getDmg());
 			if(enemy.getHealth() <= 0){
 				player.setMoney(player.getMoney() + enemy.getLootValue());
 			}
 		}
 	}
 	
 	/**
 	 * The loop that updates the whole game
 	 */
 	public void updateGame(){
 		moveEnemies();
 		shootAtEnemiesInRange();
 		removeDeadEnemies();
 		System.out.println(board);
 		System.out.println(player.getName() +": "+player.getMoney());
 	}
 	
 	private void removeDeadEnemies(){
 		for(EnemyOnBoard eob:enemiesOnBoard){
 			if(eob.getEnemy().getHealth() <= 0){
 				board.placeTile(new PathTile(new Sprite(), board.getMap().getValueAtPos(eob.getPos())), eob.getPos());
 			}
 		}
 	}
 	
 	//Inner class representing an enemy placed on the board.
 	class EnemyOnBoard implements Comparable<EnemyOnBoard> {
 		IEnemy enemy;
 		Position pos;
 		
 		public EnemyOnBoard(WaveItem nextEnemy, Position p) {
 			enemy = nextEnemy.getEnemy();
 			pos = p;
 		}
 
 		public Position getPos() {
 			return pos;
 		}
 		public void setPos(Position pos) {
 			this.pos = pos;
 		}
 		
 		public IEnemy getEnemy() {
 			return enemy;
 		}
 
 		@Override
 		public int compareTo(EnemyOnBoard o) {
 			return Integer.compare(pos.getX(), o.pos.getX());
 		}
 	}
 	
 	//Inner class representing an tower placed on the board.
 	class TowerOnBoard {
 		AbstractAttackTower tower;
 		Position pos;
 		
 		public TowerOnBoard(AbstractAttackTower abt, Position p) {
 			tower = abt;
 			pos = p;
 		}
 		
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
 			updateGame();
 			
 		}
 	}
 
 }
