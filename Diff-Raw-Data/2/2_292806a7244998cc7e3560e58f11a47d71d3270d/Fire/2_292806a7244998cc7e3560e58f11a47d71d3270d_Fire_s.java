 package com.github.joakimpersson.tda367.model.tiles.walkable;
 
 import com.github.joakimpersson.tda367.model.player.Player;
 import com.github.joakimpersson.tda367.model.tiles.Tile;
 import com.github.joakimpersson.tda367.model.tiles.WalkableTile;
 
 /**
  * 
  * @author joakimpersson
  * 
  */
 public class Fire implements WalkableTile {
 
 	private int toughness;
 
 	public Fire() {
 		// different players fire should not be able to cross each other
 		this.toughness = 100;
 	}
 
 	@Override
 	public int getToughness() {
 		return toughness;
 	}
 
 	/**
 	 * Fire can't cross other player's fire and therefore it returns itself when
 	 * the method is invoked
 	 * 
 	 * @return Itself
 	 */
 	@Override
 	public Tile onFire() {
		return this;
 	}
 
 	/**
 	 * If a player enters a fire tile it should loose one hp. Since the tile's
 	 * state is not changed it returns itself when the method is invoked
 	 * 
 	 * @param The
 	 *            player who entered the tile
 	 * @return Itself
 	 */
 	@Override
 	public Tile playerEnter(Player player) {
 		player.playerHit();
 		return this;
 	}
 
 	@Override
 	public boolean isWalkable() {
 		return true;
 	}
 	
 	@Override
 	public String toString() {
 		return "Fire";
 	}
 }
