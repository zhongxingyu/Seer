 package com.github.joakimpersson.tda367.core;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Timer;
 
import com.github.joakimpersson.tda367.core.PlayerAttributes.UpgradeType;
 import com.github.joakimpersson.tda367.core.PlayerPoints.PointGiver;
import com.github.joakimpersson.tda367.core.bombs.Bomb;
 import com.github.joakimpersson.tda367.core.powerupitems.PowerupItem;
 import com.github.joakimpersson.tda367.core.tiles.*;
 
 /**
  * 
  * @author Viktor Anderling
  * 
  */
 public class BombermanModel implements IBombermanModel {
 
 	private List<Player> players;
 	private GameField gameField;
 	private boolean roundIsPlaying;
	private static BombermanModel instance = null;
 	private List<PowerupItem> waitingPowerups;
 	private List<List<Position>> waitingFirePositions;
 	private Timer fireTimer;
 
 	private BombermanModel() {
 		this.players = new ArrayList<Player>();
 		this.gameField = new StandardMap();
 		this.roundIsPlaying = false;
 		this.waitingPowerups = new ArrayList<PowerupItem>();
 		this.waitingFirePositions = new ArrayList();
 	}
 	
	public static BombermanModel getInstance() {
 		if(instance == null) {
 			instance = new BombermanModel();
 		}
		return instance;
 	}
 	
 	public void startGame() {
 
 	}
 
 	public void endGame() {
 
 	}
 
 	public void updateGame(Player player, PlayerAction action) {
 		if (action == PlayerAction.PlaceBomb) {
 			this.placeBomb(player);
 		} else {
 			this.move(player, action);
 		}
 	}
 
 	public void upgradePlayer(Player player, Attribute attr) {
 		if(roundIsPlaying) {
 			player.upgradeAttr(attr, UpgradeType.Round);
 		} else {
 			player.upgradeAttr(attr, UpgradeType.Match);
 		}
 	}
 
 	private void matchEnd() {
 
 	}
 
 	private void turnEnd() {
 
 	}
 
 	private void move(Player player, PlayerAction action) {
 
 	}
 
 	private void placeBomb(Player player) {
 		Bomb bomb = player.placeBomb();
 		bomb.explode();
 	}
 
 	private void updatePlayerScore(Player player, List<Tile> tiles) {
 		for(Tile t : tiles) {
 			
 		}
 	}
 	
 	private void setFire(List<Position> positions) {
 		// TODO handle fire...
 		List<Position> powerupPositions;
 		
 		
 	}
 	
 	/**
 	 * This method takes care of what happens to the the positions that the fire strikes.
 	 * 
 	 * @param positions	The list that contains the positions of where the fire is to be placed.
 	 */
 	public void handleFire(List<Position> positions) {
 		List<PointGiver> pg = new ArrayList();
 		List<Position> playerPositions;
 		Tile tempTile;
 		
 				
 		for(Position pos : positions) {
 			for(Player player : players) {
 				if(isPlayerAtPosition(player, pos)) {
 					pg.add(PointGiver.PlayerHit);
 					player.playerHit();
 					if(!player.isAlive()) {
 						pg.add(PointGiver.KillPlayer);
 					}
 				}
 			}
 			
 			tempTile = gameField.getTile(pos);
 			if(tempTile instanceof Box) {
 				pg.add(PointGiver.Box);
 			} else if(tempTile instanceof Pillar) {
 				pg.add(PointGiver.Pillar);
 			} 
 			setFire(positions);
 		}
 	}
 
 	public List<Player> getPlayers() {
 		return players;
 	}
 
 	@Override
 	public int getTileToughness(Position pos) {
 		return gameField.getTile(pos).getToughness();
 	}
 	
 	public int getTileToughness(int x, int y) {
 		return getTileToughness(new Position(x, y));
 	}
 	
 	private boolean isPlayerAtPosition(Player player, Position pos) {
 		// TODO return statement.
 		return true;
 	}
 }
