 package org.opendarts.core.x01.defi.service.entry;
 
 import java.util.Comparator;
 
 import org.opendarts.core.model.dart.IDartsThrow;
 import org.opendarts.core.model.game.IGame;
 import org.opendarts.core.model.game.IGameEntry;
 import org.opendarts.core.model.player.IPlayer;
 import org.opendarts.core.stats.model.comparator.ReverseComparator;
 import org.opendarts.core.stats.model.impl.AverageStatsEntry;
 import org.opendarts.core.stats.model.impl.AvgEntry;
 
 /**
  * The Class AverageTimeStatsEntry.
  */
 public class AverageTimeStatsEntry extends AverageStatsEntry {
 
 	/** The timestamp. */
	private static Long timestamp;
 
 	/** The last time. */
	private static Double lastTime;
 
 	/** The last throw. */
 	private static IDartsThrow lastThrow;
 
 	/**
 	 * Instantiates a new average time stats entry.
 	 *
 	 * @param key the key
 	 */
 	public AverageTimeStatsEntry(String key) {
 		super(key);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.opendarts.core.stats.model.impl.AverageStatsEntry#getComparator()
 	 */
 	@Override
 	public Comparator<AvgEntry> getComparator() {
 		return new ReverseComparator<AvgEntry>(super.getComparator());
 	}
 
 	/* (non-Javadoc)
 	 * @see org.opendarts.core.stats.model.impl.AverageStatsEntry#getEntryValue(org.opendarts.core.model.game.IGame, org.opendarts.core.model.player.IPlayer, org.opendarts.core.model.game.IGameEntry, org.opendarts.core.model.dart.IDartsThrow)
 	 */
 	@Override
 	protected Number getEntryValue(IGame game, IPlayer player,
 			IGameEntry gameEntry, IDartsThrow dartsThrow) {
 		Double time = null;
 
 		long ts = dartsThrow.getTimestamp();
 		if (dartsThrow.equals(lastThrow)) {
			time = lastTime;
 		} else {
			if (timestamp != null) {
				time = ((double) ts - timestamp) / 1000d;
 			}
 		}
 
		timestamp = ts;
 		lastThrow = dartsThrow;
		lastTime = time;
 		return time;
 	}
 
 }
