 package org.blockout.world.event;
 
 import org.blockout.common.TileCoordinate;
 import org.blockout.world.entity.Crate;
 import org.blockout.world.entity.Player;
 
 /**
  * Events of this class are created when a {@link Player} opens a {@link Crate}.
  * 
  * @author Marc-Christian Schulze
  * 
  */
 public class CrateOpenedEvent extends AbstractEvent<CrateOpenedEvent> {
 
 	private static final long	serialVersionUID	= 8321214157754903797L;
 	protected Player			player;
 	protected Crate				crate;
	protected TileCoordinate	crateCoord;
 
	public CrateOpenedEvent(final Player player, final Crate crate, final TileCoordinate crateCoord) {
 		this.player = player;
 		this.crate = crate;
		this.crateCoord = crateCoord;
 	}
 
 	/**
 	 * Returns the player who opened the crate.
 	 * 
 	 * @return The player who opened the crate.
 	 */
 	public Player getPlayer() {
 		return player;
 	}
 
 	/**
 	 * Returns the crate that gets opened.
 	 * 
 	 * @return The crate that gets opened.
 	 */
 	public Crate getCrate() {
 		return crate;
 	}
 
 	@Override
 	public long getDuration() {
 		return 200;
 	}
 
 	@Override
 	public TileCoordinate getResponsibleTile() {
		return crateCoord;
 	}
 
 	@Override
 	public String toString() {
 		return player + " opened crate " + crate;
 	}
 }
