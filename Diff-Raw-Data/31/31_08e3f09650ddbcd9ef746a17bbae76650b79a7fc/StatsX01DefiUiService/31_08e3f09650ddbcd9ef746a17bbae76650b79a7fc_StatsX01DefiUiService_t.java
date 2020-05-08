 package org.opendarts.ui.x01.defi.service;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import org.eclipse.jface.viewers.ColumnLabelProvider;
 import org.opendarts.core.model.game.IGame;
 import org.opendarts.core.model.session.ISession;
 import org.opendarts.core.model.session.ISet;
 import org.opendarts.core.stats.model.impl.AvgEntry;
 import org.opendarts.core.x01.defi.OpenDartsX01DefiBundle;
 import org.opendarts.core.x01.defi.model.GameX01Defi;
 import org.opendarts.core.x01.defi.service.StatsX01DefiService;
 import org.opendarts.ui.stats.model.IChart;
 import org.opendarts.ui.stats.service.IStatsUiService;
 import org.opendarts.ui.x01.defi.chart.AvgDartHistoryCategoryChartX01;
 import org.opendarts.ui.x01.defi.chart.AvgTimeHistoryCategoryChartX01;
import org.opendarts.ui.x01.defi.label.ChartDefiLabelProvider;
 import org.opendarts.ui.x01.defi.label.StatsX01DefiLabelProvider;
 import org.opendarts.ui.x01.model.GameThrowDistributionChartX01;
 import org.opendarts.ui.x01.model.GameThrowPieChartX01;
 
 /**
  * The Class StatsX01UiService.
  */
 public class StatsX01DefiUiService implements IStatsUiService {
 
 	/**
 	 * Instantiates a new stats x01 ui service.
 	 */
 	public StatsX01DefiUiService() {
 		super();
 	}
 
 	/**
 	 * Gets the stats label provider.
 	 *
 	 * @return the stats label provider
 	 */
 	@Override
 	public ColumnLabelProvider getStatsLabelProvider() {
 		return new StatsX01DefiLabelProvider();
 	}
 
 	/* (non-Javadoc)
 	 * @see org.opendarts.ui.stats.service.IStatsUiService#getChartLabelProvider()
 	 */
 	@Override
 	public ColumnLabelProvider getChartLabelProvider() {
		return new ChartDefiLabelProvider();
 	}
 
 	/* (non-Javadoc)
 	 * @see org.opendarts.ui.stats.service.IStatsUiService#getCharts(org.opendarts.core.model.session.ISession, java.lang.String)
 	 */
 	@Override
 	public List<IChart> getCharts(ISession session, String statKey) {
 		return Collections.emptyList();
 	}
 
 	/* (non-Javadoc)
 	 * @see org.opendarts.ui.stats.service.IStatsUiService#getCharts(org.opendarts.core.model.session.ISet, java.lang.String)
 	 */
 	@Override
 	public List<IChart> getCharts(ISet set, String statKey) {
 		// No stats
 		return Collections.emptyList();
 	}
 
 	/* (non-Javadoc)
 	 * @see org.opendarts.ui.stats.service.IStatsUiService#getCharts(org.opendarts.core.model.game.IGame, java.lang.String)
 	 */
 	@Override
 	public List<IChart> getCharts(IGame game, String statKey) {
 		List<IChart> result = new ArrayList<IChart>();
 		String statName = this.getStatsLabelProvider().getToolTipText(statKey);
 
 		if (StatsX01DefiService.GAME_AVG_DART.equals(statKey)) {
 			result.add(new GameThrowDistributionChartX01<AvgEntry>(
 					"Throw distribution (game)", statKey, game));
 			result.add(new GameThrowPieChartX01<AvgEntry>(
 					"Throw pie distribution (game)", statKey, game));
 		} else if (StatsX01DefiService.GAME_AVG_DART_HISTORY.equals(statKey)) {
 			result.add(new AvgDartHistoryCategoryChartX01(statName, statKey,
 					game, OpenDartsX01DefiBundle.getStatsService((GameX01Defi) game)));
 		} else if (StatsX01DefiService.GAME_AVG_DART_HISTORY.equals(statKey)) {
 			result.add(new AvgTimeHistoryCategoryChartX01(statName, statKey,
 					game, OpenDartsX01DefiBundle.getStatsService((GameX01Defi) game)));
 		}
 		
 		return result;
 	}
 
 }
