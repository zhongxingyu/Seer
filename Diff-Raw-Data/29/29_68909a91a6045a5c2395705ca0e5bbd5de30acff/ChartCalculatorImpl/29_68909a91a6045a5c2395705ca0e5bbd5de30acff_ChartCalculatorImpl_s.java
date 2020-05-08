 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package de.bedit.gaming.wormstats.chart;
 
 import de.bedit.gaming.wormstats.dao.CompetitorMatchStatisticDao;
 import de.bedit.gaming.wormstats.dao.MatchGameDao;
 import de.bedit.gaming.wormstats.model.Competitor;
 import de.bedit.gaming.wormstats.model.CompetitorMatchStatistic;
 import de.bedit.gaming.wormstats.model.PieChartEntry;
 import java.util.List;
 import javax.ejb.EJB;
 import javax.ejb.Stateless;
 
 /**
  *
  * @author benjamin
  */
 @Stateless
 public class ChartCalculatorImpl implements ChartCalculator {
 
 	@EJB
 	private CompetitorMatchStatisticDao competitorMatchStatisticDao;
 	@EJB
 	private MatchGameDao matchGameDao;
 
 	@Override
 	public PieChartEntry createKillsPerMatchPieChartEntry(Competitor comp) {
 		PieChartEntry pieChartEntry = new PieChartEntry();
 
 		pieChartEntry.setLabel(comp.getName());
 		pieChartEntry.setValue(0d);
 		List<CompetitorMatchStatistic> matches = competitorMatchStatisticDao
 				.getAllCompetitorMatchStatisticsByCompeitorId(comp.getId());
 
 		for (CompetitorMatchStatistic cms : matches) {
			pieChartEntry.setValue(pieChartEntry.getValue() + cms.getKills());
 		}
 
 		try {
			pieChartEntry.setValue(pieChartEntry.getValue() / matches.size());
 		} catch (ArithmeticException ex) {
 			pieChartEntry.setValue(0d);
 		}
 		return pieChartEntry;
 	}
 
 	@Override
 	public PieChartEntry createWinsPerMatchPieChartEntry(Competitor comp) {
 		PieChartEntry pieChartEntry = new PieChartEntry();
 
 		pieChartEntry.setLabel(comp.getName());
 		pieChartEntry.setValue(0d);
 
 		try {
			pieChartEntry.setValue(matchGameDao.getAllMatchGamesByWinnerId(
					comp.getId()).size()
					/ competitorMatchStatisticDao
							.getAllCompetitorMatchStatisticsByCompeitorId(
									comp.getId()).size());
 		} catch (ArithmeticException ex) {
 			pieChartEntry.setValue(0d);
 		}
 
 		return pieChartEntry;
 	}
 
 	@Override
 	public PieChartEntry createSelfKillsPerMatchPieChartEntry(Competitor comp) {
 		PieChartEntry pieChartEntry = new PieChartEntry();
 
 		pieChartEntry.setLabel(comp.getName());
 		pieChartEntry.setValue(0d);
 		List<CompetitorMatchStatistic> matches = competitorMatchStatisticDao
 				.getAllCompetitorMatchStatisticsByCompeitorId(comp.getId());
 
 		for (CompetitorMatchStatistic cms : matches) {
			pieChartEntry.setValue(pieChartEntry.getValue()
					+ cms.getSelfKills());
 		}
 
 		try {
			pieChartEntry.setValue(pieChartEntry.getValue() / matches.size());
 		} catch (ArithmeticException ex) {
 			pieChartEntry.setValue(0d);
 		}
 
 		return pieChartEntry;
 	}
 }
