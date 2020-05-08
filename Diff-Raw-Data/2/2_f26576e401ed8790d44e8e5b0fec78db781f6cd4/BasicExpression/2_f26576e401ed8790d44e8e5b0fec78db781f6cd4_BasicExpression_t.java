 /**
  * 
  */
 package com.myrontuttle.fin.trade.adapt.express;
 
 import java.util.ArrayList;
 import java.util.Hashtable;
 import java.util.List;
 
 import com.myrontuttle.fin.trade.adapt.Candidate;
 import com.myrontuttle.fin.trade.adapt.Group;
 import com.myrontuttle.fin.trade.adapt.GroupDAO;
 import com.myrontuttle.fin.trade.adapt.Trader;
 import com.myrontuttle.fin.trade.api.*;
 import com.myrontuttle.fin.trade.strategies.AlertTrade;
 import com.myrontuttle.sci.evolve.ExpressedCandidate;
 import com.myrontuttle.sci.evolve.ExpressionStrategy;
 
 /**
  * Expresses a genome of int[] into a TradingStrategy
  * @author Myron Tuttle
  * @param <T> The candidate to be expressed
  */
 public class BasicExpression<T> implements ExpressionStrategy<int[]> {
 
 	// Gene lengths
 	public static final int SCREEN_GENE_LENGTH = 3;
 	public static final int ALERT_GENE_LENGTH = 4;
 	public static final int TRADE_GENE_LENGTH = 5;
 	
 	// Genome positions
 	public static final int SCREEN_SORT_POSITION = 0;
 	
 	public static final String PORT_NAME_PREFIX = "PORT";
 	public static final String WATCH_NAME_PREFIX = "WATCH";
 	public static final String GROUP = "GROUP";
 	public static final String CANDIDATE = "CANDIDATE";
 	
 	// Managed by Blueprint
 	private static ScreenerService screenerService;
 	private static WatchlistService watchlistService;
 	private static AlertService alertService;
 	private static PortfolioService portfolioService;
 	private static TradeStrategyService tradeStrategyService;
 	private static AlertReceiverService alertReceiver;
 	
 	private static GroupDAO groupDAO;
 	
 	public static ScreenerService getScreenerService() {
 		return screenerService;
 	}
 
 	public void setScreenerService(ScreenerService screenerService) {
 		BasicExpression.screenerService = screenerService;
 	}
 
 	public static WatchlistService getWatchlistService() {
 		return watchlistService;
 	}
 
 	public void setWatchlistService(WatchlistService watchlistService) {
 		BasicExpression.watchlistService = watchlistService;
 	}
 
 	public static AlertService getAlertService() {
 		return alertService;
 	}
 
 	public void setAlertService(AlertService alertService) {
 		BasicExpression.alertService = alertService;
 	}
 
 	public static PortfolioService getPortfolioService() {
 		return portfolioService;
 	}
 
 	public void setPortfolioService(PortfolioService portfolioService) {
 		BasicExpression.portfolioService = portfolioService;
 	}
 
 	public static TradeStrategyService getTradeStrategyService() {
 		return tradeStrategyService;
 	}
 
 	public void setTradeStrategyService(TradeStrategyService tradeStrategyService) {
 		BasicExpression.tradeStrategyService = tradeStrategyService;
 	}
 
 	public static AlertReceiverService getAlertReceiver() {
 		return alertReceiver;
 	}
 
 	public void setAlertReceiver(AlertReceiverService alertReceiver) {
 		BasicExpression.alertReceiver = alertReceiver;
 	}
 
 	public static GroupDAO getGroupDAO() {
 		return groupDAO;
 	}
 
 	public void setGroupDAO(GroupDAO groupDAO) {
 		BasicExpression.groupDAO = groupDAO;
 	}
 
 	public int getGenomeLength(String groupId) {
 		Group group = groupDAO.findGroup(groupId);
 		return 1 + SCREEN_GENE_LENGTH * group.getNumberOfScreens() +
 				group.getMaxSymbolsPerScreen() * ALERT_GENE_LENGTH * group.getAlertsPerSymbol() +
 				group.getMaxSymbolsPerScreen() * group.getAlertsPerSymbol() * TRADE_GENE_LENGTH;
 	}
 
 	private int getScreenStartPosition() {
 		return SCREEN_SORT_POSITION + 1;
 	}
 
 	/**
 	 * Creates a set of SelectedScreenCriteria based on a candidate's screener genes
 	 * Screen Gene Data Map
 	 * 1. Is screen criteria active?
 	 * 2. Criteria to use
 	 * 3. Criteria value
 	 * 
 	 * @param candidate A candidate
 	 * @param group The candidates group
 	 * @return A set of screener criteria selected by this candidate
 	 */
 	public SelectedScreenCriteria[] expressScreenerGenes(Candidate candidate, Group group) 
 			throws Exception {
 		
 		int[] genome = candidate.getGenome();
 
 		// Get screener possibilities
 		AvailableScreenCriteria[] availableScreenCriteria = 
 				screenerService.getAvailableCriteria(group.getFullGroupId());
 		if (availableScreenCriteria == null) {
 			throw new Exception("No available screen criteria for " + group.getFullGroupId());
 		}
 
 		// Start at the first screen gene
 		int position = getScreenStartPosition();
 		ArrayList<SelectedScreenCriteria> selected = new ArrayList<SelectedScreenCriteria>();
 		int screens = group.getNumberOfScreens();
 		int geneUpperValue = group.getGeneUpperValue();
 		for (int i=0; i<screens; i++) {
 			int active = transpose(genome[position], geneUpperValue, 0, 1);
 			if (active == 1) {
 				int criteriaIndex = transpose(genome[position + 1], geneUpperValue, 
 												0, availableScreenCriteria.length - 1);
 				String name = availableScreenCriteria[criteriaIndex].getName();
 				int valueIndex = transpose(genome[position + 2], geneUpperValue, 0, 
 								availableScreenCriteria[criteriaIndex].getAcceptedValues().length - 1);
 				String value = availableScreenCriteria[criteriaIndex].getAcceptedValue(valueIndex);
 				String argOp = availableScreenCriteria[criteriaIndex].getArgsOperator();
 				selected.add(new SelectedScreenCriteria(name, value, argOp));
 			}
 			position += SCREEN_GENE_LENGTH;
 		}
 		return selected.toArray(new SelectedScreenCriteria[selected.size()]);
 	}
 	
 	public String[] getScreenSymbols(Candidate candidate, Group group, 
 										SelectedScreenCriteria[] screenCriteria) throws Exception {
 		String[] symbols = null;
 		int[] genome = candidate.getGenome();
 
 		if (screenCriteria.length == 0) {
 			// All of the screen symbols are turned off and we won't get any symbols from screening
 			return symbols;
 		}
 		int sortGene = transpose(genome[SCREEN_SORT_POSITION], group.getGeneUpperValue(), 
 									0, screenCriteria.length - 1);
 
 		String[] screenSymbols = screenerService.screen(
 										group.getFullGroupId(),
 										screenCriteria,
 										screenCriteria[sortGene].getName(),
 										group.getMaxSymbolsPerScreen());
 		
 		if (screenSymbols == null) {
 			throw new Exception("No symbols found for " + group.getFullGroupId());
 		}
 		if (screenSymbols.length > group.getMaxSymbolsPerScreen()) {
 			symbols = new String[group.getMaxSymbolsPerScreen()];
 			System.arraycopy(screenSymbols, 0, symbols, 0, group.getMaxSymbolsPerScreen());
 		} else {
 			symbols = new String[screenSymbols.length];
 			System.arraycopy(screenSymbols, 0, symbols, 0, symbols.length);
 		}
 		
 		return symbols;
 	}
 	
 	String setupWatchlist(Candidate candidate, Group group, String[] symbols) throws Exception {
 		String watchlistId = null;
 		String candidateId = candidate.getFullCandidateId();
 		String groupId = group.getGroupId();
 		String name = WATCH_NAME_PREFIX + GROUP + groupId + CANDIDATE + candidateId;
 		watchlistId = watchlistService.create(candidateId, name);
 		if (watchlistId == null) {
 			throw new Exception("Error creating watchlist: " + name);
 		}
 
 		// Add stocks to a watchlist
 		for (int i=0; i<symbols.length; i++) {
 			watchlistService.addHolding(candidateId, watchlistId, symbols[i]);
 		}
 		
 		return watchlistId;
 	}
 	
 	String setupPortfolio(Candidate candidate, Group group) throws Exception {
 		String portfolioId = null;
 		String candidateId = candidate.getFullCandidateId();
 		String name = PORT_NAME_PREFIX + GROUP + group.getGroupId() + CANDIDATE + candidateId;
 		portfolioId = portfolioService.create(candidateId, name);
 		if (portfolioId == null) {
 			throw new Exception("Error creating portfolio: " + name);
 		}
 		portfolioService.addCashTransaction(candidateId, portfolioId, 
 											group.getStartingCash(), 
 											true, true);
 		
 		return portfolioId;
 	}
 	
 	private int getAlertStartPosition(Group group) {
 		return getScreenStartPosition() + SCREEN_GENE_LENGTH * group.getNumberOfScreens();
 	}
 	
 	/**
 	 * Creates a set of SelectedAlert based on a candidate's alert genes
 	 * If the alert requires a double then two positions will be used to create a double with
 	 * two decimal places.  If the alert requires a list index, then only the first position
 	 * will be used.
 	 * 
 	 * Alert Gene Data Map
 	 * 1. condition
 	 * 2. parameter1 (list index or double value)
 	 * 3. parameter2 (list index or double value)
 	 * 4. parameter3 (list index or double value)
 	 * 
 	 * @param candidate A candidate
 	 * @param group The candidate's group
 	 * @param symbols The symbols found during screening
 	 * @return A set of alert criteria selected by this candidate
 	 */
 	public SelectedAlert[] expressAlertGenes(Candidate candidate, Group group, 
 			String[] symbols) throws Exception {
 
 		int[] genome = candidate.getGenome();
 		
 		// Get alert possibilities
 		AvailableAlert[] availableAlerts = alertService.getAvailableAlerts(group.getFullGroupId());
 		if (availableAlerts == null) {
 			throw new Exception("No available alerts for " + group.getFullGroupId());
 		}
 		
 		SelectedAlert[] selected = new SelectedAlert[symbols.length * group.getAlertsPerSymbol()];
 		
 		int position = getAlertStartPosition(group);
 		int s = 0;
 		for (int i=0; i<symbols.length; i++) {
 			for (int j=0; j<group.getAlertsPerSymbol(); j++) {
 
 				AvailableAlert alert = availableAlerts[transpose(genome[position],  
 																	group.getGeneUpperValue(),
 																	0, availableAlerts.length - 1)];
 				int id = alert.getId();
 				String[] criteriaTypes = alert.getCriteriaTypes();
 				double[] params = new double[criteriaTypes.length];
 				for (int k=0; k<criteriaTypes.length; k++) {
 					if (criteriaTypes[k].equals(AvailableAlert.DOUBLE)) {
 						double upper = alertService.getUpperDouble(group.getFullGroupId(), id, symbols[i], k);
 						double lower = alertService.getLowerDouble(group.getFullGroupId(), id, symbols[i], k);
 						params[k] = transpose(genome[position + 1], group.getGeneUpperValue(),
 												lower, upper);
 					} else if (criteriaTypes[k].equals(AvailableAlert.LIST)) {
 						int upper = alertService.getListLength(group.getFullGroupId(), id, k);
 						params[k] = transpose(genome[position + 1], group.getGeneUpperValue(), 
 												0, upper);
 					}
 				}
 				String selectedCondition = alertService.parseCondition(alert, symbols[i], 
 																		params);
 				selected[s] = new SelectedAlert(id, selectedCondition, 
 													symbols[i], params);
 				s++;
 				position += ALERT_GENE_LENGTH;
 			}
 		}
 		
 		return selected;
 	}
 	
 	void setupAlerts(Group group, SelectedAlert[] openAlerts) throws Exception {
 
 		alertService.addAlertDestination(group.getFullGroupId(), group.getAlertAddress(), "EMAIL");
 
 		alertService.setupAlerts(group.getFullGroupId(), openAlerts);
 	}
 
 	/**
 	 * Creates a set of Trades based on a candidate's trade genes
 	 * 
 	 * Order Gene Data Map
 	 * 1. Order Type
 	 * 2. Allocation
 	 * 3. Acceptable Loss
 	 * 4. Time in Trade
 	 * 5. Adjust At
 	 * @param candidate A candidate
 	 * @param group The candidate's group
 	 * @param symbols The symbols found during screening
 	 * @return A set of alert criteria selected by this candidate
 	 */
 	public Trade[] expressTradeGenes(Candidate candidate, Group group, String[] symbols) throws Exception {
 
 		String candidateId = candidate.getCandidateId();
 		int[] genome = candidate.getGenome();
 		
 		Trade[] trades = new Trade[symbols.length];
 		
 		TradeStrategy tradeStrategy = tradeStrategyService.getTradeStrategy(group.getTradeStrategy());
 		
 		int openOrderTypes;
 		try {
 			openOrderTypes = portfolioService.openOrderTypesAvailable(candidateId).length;
 		} catch (Exception e) {
 			System.out.println("Unable to get openOrderTypesAvailable from Portfolio Service. Using 1");
 			openOrderTypes = 1;
 		}
 
 		tradeStrategy.setOrderTypesAvailable(openOrderTypes);
 		
 		AvailableStrategyParameter[] availableParameters = tradeStrategy.availableParameters();
 		
 		int position = getAlertStartPosition(group) + 
 				group.getMaxSymbolsPerScreen() * ALERT_GENE_LENGTH * group.getAlertsPerSymbol();
 		
 		Hashtable<String, Integer> tradeParameters;
 		
 		for (int i=0; i<symbols.length; i++) {
 			tradeParameters = new Hashtable<String, Integer>(availableParameters.length);
 			
 			for (int j=0; j<availableParameters.length; j++) {
				tradeParameters.put(availableParameters[j].getName(),
 										transpose(genome[position + 1],
 												group.getGeneUpperValue(),
 												availableParameters[i].getLower(), 
 												availableParameters[i].getUpper()));
 			}
 			
 			trades[i] = new Trade(symbols[i], tradeParameters);
 			position += TRADE_GENE_LENGTH;
 		}
 		return trades;
 	}
 	
 	void setupAlertReceiver(SelectedAlert[] openAlerts, String portfolioId, Trade[] tradesToMake, 
 								Group group) throws Exception {
 		AlertTrade[] alertTrades = new AlertTrade[openAlerts.length];
 		for (int i=0; i<tradesToMake.length; i++) {
 			for (int j=0; j<group.getAlertsPerSymbol(); j++) {
 				alertTrades[i+j] = new AlertTrade(openAlerts[i+j], portfolioId, tradesToMake[i]);
 			}
 		}
 		alertReceiver.watchFor(alertTrades);
 	}
 
 	@Override
 	public Candidate express(int[] genome, String groupId) {
 		
 		Candidate candidate = new Candidate();
 		candidate.setGenome(genome);
 		candidate.setGroupId(groupId);
 		
 		groupDAO.addCandidate(candidate, groupId);
 		
 		// Find the group
 		Group group = groupDAO.findGroup(groupId);
 
 		candidate.setFullCandidateId(group.getIdPrepend() + candidate.getCandidateId());
 
 		try {
 			// Get criteria to screen against
 			SelectedScreenCriteria[] screenCriteria = expressScreenerGenes(candidate, group);
 			
 			// Get a list of symbols from the Screener Service
 			String[] symbols = getScreenSymbols(candidate, group, screenCriteria);
 			
 			// If the screener didn't produce any symbols there's no point using the other services
 			if (symbols == null || symbols.length == 0) {
 				return candidate;
 			}
 			
 			// Create a watchlist
 			String watchlistId = setupWatchlist(candidate, group, symbols);
 			candidate.setWatchlistId(watchlistId);
 			
 			// Prepare portfolio
 			String portfolioId = setupPortfolio(candidate, group);
 
 			// No point continuing if there's no portfolio to track trades
 			if (portfolioId == null || portfolioId == "") {
 				return candidate;
 			} else {
 				candidate.setPortfolioId(portfolioId);
 			}
 
 			// Create (symbols*alertsPerSymbol) alerts for stocks
 			SelectedAlert[] openAlerts = expressAlertGenes(candidate, group, symbols);
 			setupAlerts(group, openAlerts);
 			
 			// Create (symbol) trades to be made when alerts are triggered
 			Trade[] tradesToMake = expressTradeGenes(candidate, group, symbols);
 			
 			// Create listener for alerts to move stocks to portfolio
 			setupAlertReceiver(openAlerts, portfolioId, tradesToMake, group);
 		} catch (Exception e) {
 			System.out.println("Unable to express candidate " + 
 					candidate.getFullCandidateId());
 			e.printStackTrace();
 		}
 
 		// Save candidate to database, and return
 		groupDAO.updateCandidate(candidate);
 		
 		return candidate;
 	}
 
 	@Override
 	public void candidatesExpressed(
 			List<ExpressedCandidate<int[]>> expressedCandidates) {
 	}
 	
 	/**
 	 * Transpose the genes to the the proper value within the required range [inclusive]
 	 * @param geneValue The gene value
 	 * @param geneUpperValue The upper limit of what the geneValue is able to have
 	 * @param targetLower The lower bound of the range to transpose into
 	 * @param targetUpper The upper bound of the range to transpose into
 	 * @return The transposed value between the lower and upper bounds of the gene
 	 */
 	private int transpose(int geneValue, int geneUpperValue, int targetLower, int targetUpper) {
 		if (targetUpper - targetLower < geneUpperValue) {
 			if (geneValue == geneUpperValue) {
 				return targetUpper;
 			}
 			return targetLower + (int)Math.floor(((double)geneValue / geneUpperValue) * 
 					(targetUpper - targetLower + 1));
 		} else {
 			return targetLower + (int)Math.floor(((double)geneValue / geneUpperValue) * 
 									(targetUpper - targetLower));
 		}
 	}
 
 	/**
 	 * Transpose the genes to the the proper value within the required range
 	 * @param geneValue The gene value
 	 * @param geneUpperValue The upper limit of what the geneValue is able to have
 	 * @param targetLower The lower bound of the range to transpose into
 	 * @param targetUpper The upper bound of the range to transpose into
 	 * @return The transposed value between the lower and upper bounds of the gene
 	 */
 	private double transpose(int geneValue, int geneUpperValue, double targetLower, double targetUpper) {
 		if (targetUpper - targetLower < geneUpperValue) {
 			return targetLower + ((double)(geneValue * (targetUpper - targetLower)) / geneUpperValue);
 		} else {
 			return targetLower + ((double)geneValue / geneUpperValue) * (targetUpper - targetLower);
 		}
 	}
 	
 	public static Trader createTrader(Candidate candidate) {
 		Trader trader = new Trader();
 		//trader.setGroupId(candidate.getGroupId());
 		//trader.setGenomeString(candidate.getGenomeString());
 		return trader;
 	}
 }
