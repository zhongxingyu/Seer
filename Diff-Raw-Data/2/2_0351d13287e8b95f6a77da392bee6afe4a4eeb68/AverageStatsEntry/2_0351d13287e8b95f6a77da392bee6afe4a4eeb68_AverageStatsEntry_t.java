 /*
  * 
  */
 package org.opendarts.core.stats.model.impl;
 
 import java.util.Comparator;
 
 import org.opendarts.core.model.dart.IDartsThrow;
 import org.opendarts.core.model.game.IGame;
 import org.opendarts.core.model.game.IGameEntry;
 import org.opendarts.core.model.player.IPlayer;
 
 /**
  * The Class MaxStatsEntry.
  *
  * @param <T> the generic type
  */
 public abstract class AverageStatsEntry extends AbstractStatsEntry<AvgEntry> {
 
 	/**
 	 * Instantiates a new best stats entry.
 	 *
 	 * @param comparator the comparator
 	 */
 	public AverageStatsEntry(String key) {
 		super(key);
 	}
 	
 	/* (non-Javadoc)
 	 * @see org.opendarts.core.stats.model.IStatsEntry#getComparator()
 	 */
 	@Override
 	public Comparator<AvgEntry> getComparator() {
 		return new Comparator<AvgEntry>() {
 			@Override
 			public int compare(AvgEntry o1, AvgEntry o2) {
 				int result;
 				if (o1==null && o2!=null) {
 					result = -1;
 				} else if (o2!=null && o1==null) {
 					result = 1;
 				} else if (o1==null && o2==null) {
 					result = 0;
 				} else {
 					double diff = o1.getAvg() - o2.getAvg();
					if (Math.abs(diff) < 0.005) {
 						result = 0;
 					} else if (diff>0) {
 						result = 1;
 					} else {
 						result = -1;
 					}
 				}
 				return result;
 			}
 		};
 	}
 
 	/* (non-Javadoc)
 	 * @see org.opendarts.prototype.internal.model.stats.AbstractStatsEntry#handleDartsThrow(org.opendarts.prototype.model.game.IGame, org.opendarts.prototype.model.player.IPlayer, org.opendarts.prototype.model.game.IGameEntry, org.opendarts.prototype.model.dart.IDartsThrow)
 	 */
 	@Override
 	public boolean handleDartsThrow(IGame game, IPlayer player,
 			IGameEntry gameEntry, IDartsThrow dartsThrow) {
 		return this.addNewInput(
 				this.getEntryIncr(game, player, gameEntry, dartsThrow),
 				this.getEntryValue(game, player, gameEntry, dartsThrow));
 	}
 
 	/**
 	 * Gets the entry incr.
 	 *
 	 * @param game the game
 	 * @param player the player
 	 * @param gameEntry the game entry
 	 * @param dartsThrow the darts throw
 	 * @return the entry incr
 	 */
 	protected Number getEntryIncr(IGame game, IPlayer player,
 			IGameEntry gameEntry, IDartsThrow dartsThrow) {
 		return 1D;
 	}
 
 	/**
 	 * Gets the entry value.
 	 *
 	 * @param game the game
 	 * @param player the player
 	 * @param gameEntry the game entry
 	 * @param dartsThrow the darts throw
 	 * @return the entry value
 	 */
 	protected abstract Number getEntryValue(IGame game, IPlayer player,
 			IGameEntry gameEntry, IDartsThrow dartsThrow);
 
 	/**
 	 * Adds the new input.
 	 *
 	 * @param input the input
 	 * @return true, if successful
 	 */
 	protected boolean addNewInput(Number incr, Number input) {
 		if (input != null) {
 			StatsValue<AvgEntry> value = (StatsValue<AvgEntry>) this.getValue();
 			if (value == null) {
 				// new value
 				value = new StatsValue<AvgEntry>();
 				this.setValue(value);
 				AvgEntry entry = new AvgEntry();
 				entry.addValue(incr, input);
 				value.setValue(entry);
 			} else {
 				AvgEntry entry = value.getValue();
 				entry.addValue(incr, input);
 			}
 		}
 		return true;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.opendarts.prototype.internal.model.stats.AbstractStatsEntry#getInput(org.opendarts.prototype.model.game.IGame, org.opendarts.prototype.model.player.IPlayer, org.opendarts.prototype.model.game.IGameEntry, org.opendarts.prototype.model.dart.IDartsThrow)
 	 */
 	@Override
 	protected AvgEntry getInput(IGame game, IPlayer player,
 			IGameEntry gameEntry, IDartsThrow dartsThrow) {
 		// not called
 		return null;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.opendarts.prototype.internal.model.stats.AbstractStatsEntry#getUndoInput(org.opendarts.prototype.model.game.IGame, org.opendarts.prototype.model.player.IPlayer, org.opendarts.prototype.model.game.IGameEntry, org.opendarts.prototype.model.dart.IDartsThrow)
 	 */
 	@Override
 	protected AvgEntry getUndoInput(IGame game, IPlayer player,
 			IGameEntry gameEntry, IDartsThrow dartsThrow) {
 		return this.getInput(game, player, gameEntry, dartsThrow);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.opendarts.prototype.internal.model.stats.AbstractStatsEntry#undoDartsThrow(org.opendarts.prototype.model.game.IGame, org.opendarts.prototype.model.player.IPlayer, org.opendarts.prototype.model.game.IGameEntry, org.opendarts.prototype.model.dart.IDartsThrow)
 	 */
 	@Override
 	public boolean undoDartsThrow(IGame game, IPlayer player,
 			IGameEntry gameEntry, IDartsThrow dartsThrow) {
 		return this.removeNewInput(
 				this.getEntryIncr(game, player, gameEntry, dartsThrow),
 				this.getEntryValue(game, player, gameEntry, dartsThrow));
 	}
 
 	/* (non-Javadoc)
 	 * @see org.opendarts.prototype.internal.model.stats.AbstractStatsEntry#undoNewInput(java.lang.Object)
 	 */
 	protected boolean removeNewInput(Number incr, Number input) {
 		boolean result = false;
 		if (input != null) {
 			StatsValue<AvgEntry> value = (StatsValue<AvgEntry>) this.getValue();
 			if (value != null) {
 				AvgEntry entry = value.getValue();
 				entry.removeValue(incr, input);
 				result = true;
 			}
 		}
 		return result;
 	}
 }
