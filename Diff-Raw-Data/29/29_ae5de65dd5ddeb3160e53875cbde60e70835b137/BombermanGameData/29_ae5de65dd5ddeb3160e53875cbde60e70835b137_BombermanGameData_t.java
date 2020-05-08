 package de.tu_darmstadt.gdi1.bomberman.game.levels;
 
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

 import de.tu_darmstadt.gdi1.bomberman.game.elements.Bomb;
 import de.tu_darmstadt.gdi1.bomberman.game.elements.Explosion;
 import de.tu_darmstadt.gdi1.bomberman.game.elements.GameElement;
 import de.tu_darmstadt.gdi1.bomberman.game.elements.Player;
 import de.tu_darmstadt.gdi1.framework.interfaces.IGameBoard;
 import de.tu_darmstadt.gdi1.framework.model.GameData;
 import de.tu_darmstadt.gdi1.framework.model.StepManager;
 
 /**
  * Der gesamte Zustand eines Bomberman Spiels. Hierauf arbeitet die BombermanGame Klasse.
  */
 public class BombermanGameData extends GameData<GameElement> {
 
 	/**
 	 * Contains all players the level contains.
 	 */
 	protected HashMap<Integer, Player> players = new HashMap<Integer, Player>();
 	protected ArrayList<Bomb> bombs = new ArrayList<Bomb>();
 	protected ArrayList<Explosion> explosions = new ArrayList<Explosion>();
 
 	public BombermanGameData (IGameBoard<GameElement> gameBoard)
 	{
 		super(new StepManager<GameElement>(gameBoard));
 
 		// Scan the game field for players and the like. Also link game elements to myself
 		for (int x = 0; x < gameBoard.getWidth(); x++) {
 			for (int y = 0; y < gameBoard.getHeight(); y++) {
 				List<GameElement> elements = gameBoard.getElements(x, y);
 				for (GameElement el : elements) {
 
 					el.setGameBoard(gameBoard);
 					el.setCoordinates(x, y);
 					el.setGameData(this);
 
 					if (el instanceof Player) {
 						Player pl = (Player) el;
 						players.put(pl.getPlayerID(), pl);
 					}
 					else if (el instanceof Bomb) {
 						bombs.add((Bomb)el);
 					}
 				}
 			}
 		}
 	}
 
 	// Players /////////////////////////////////////////////////////////////////////////////////////
 
 	public Collection<Player> getPlayers () {
 		return players.values();
 	}
 
 	public Player getPlayer (int playerIdx) {
 		return players.get(playerIdx);
 	}
 
 	public void removePlayer (int playerIdx) {
 		players.remove(playerIdx);
 			isWon();
 		
 	}
 
 	// Bombs ///////////////////////////////////////////////////////////////////////////////////////
 	
 	public void addBomb (Bomb bomb) {
 		if (bomb != null && !bombs.contains(bomb))
 			bombs.add(bomb);
 	}
 
 	public void removeBomb (Bomb bomb) {
 		bombs.remove(bomb);
 	}
 
 	public ArrayList<Bomb> getBombs () {
 		return bombs;
 	}
 
 	// Explosions ///////////////////////////////////////////////////////////////////////////////////////
 
 	public void addExplosions (ArrayList<Explosion> explosions) {
 		if (explosions != null)
 			this.explosions.addAll(explosions);
 	}
 
 	public void removeExplosion (Explosion exp) {
 		explosions.remove(exp);
 	}
 
 	public ArrayList<Explosion> getExplosions () {
 		return explosions;
 	}
 
 	// Gamedata Interface //////////////////////////////////////////////////////////////////////////
 
 	@Override
 	public boolean isLost() {
 		return false;
 	}
 
 	@Override
 	public boolean isPaused() {
 		return false;
 	}
 
 	@Override
 	public boolean isRunning() {
 		return true;
 	}
 
 	
 	public boolean isWon() {
 		boolean won = false;
 		if (players.size() == 1){
 			won = true;
			for (Player player : players.values()) {
				System.out.println(player.getDescription() + " wins the game.");
			}
 		}
 		return won;
 	}
 
 }
