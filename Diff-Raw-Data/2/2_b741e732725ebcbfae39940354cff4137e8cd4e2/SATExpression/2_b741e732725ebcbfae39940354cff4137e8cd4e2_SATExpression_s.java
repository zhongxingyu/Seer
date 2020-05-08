 /**
  * 
  */
 package com.myrontuttle.fin.trade.adapt;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.myrontuttle.fin.trade.api.*;
 import com.myrontuttle.sci.evolve.api.ExpressedCandidate;
 import com.myrontuttle.sci.evolve.api.ExpressionStrategy;
 
 /**
  * Expresses a genome of int[] into a trading strategy candidate by Screening, Alerting, and Trading 
  * (using a paper account/portfolio)
  * @author Myron Tuttle
  * @param <T> The candidate to be expressed
  */
 public class SATExpression<T> implements ExpressionStrategy<int[]> {
 
 	private static final Logger logger = LoggerFactory.getLogger( SATExpression.class );
 
 	// Gene lengths
 	public static final int SCREEN_GENE_LENGTH = 2;
 	public static final int ALERT_GENE_LENGTH = 4;
 	public static final int TRADE_GENE_LENGTH = 5;
 	
 	// Genome positions
 	public static final int SCREEN_SORT_POSITION = 0;
 	
 	public static final String PORT_NAME_PREFIX = "P";
 	public static final String WATCH_NAME_PREFIX = "W";
 	public static final String GROUP = "G";
 	public static final String CANDIDATE = "C";
 	
 	// Managed by Blueprint
 	private static QuoteService quoteService;	// Used in TradeStrategy
 	private static ScreenerService screenerService;
 	private static WatchlistService watchlistService;
 	private static AlertService alertService;
 	private static PortfolioService portfolioService;
 	private static TradeStrategyService tradeStrategyService;
 	private static AlertReceiverService alertReceiverService;
 	
 	private static AdaptDAO adaptDAO;
 	
 	public static QuoteService getQuoteService() {
 		return quoteService;
 	}
 
 	public void setQuoteService(QuoteService quoteService) {
 		SATExpression.quoteService = quoteService;
 	}
 
 	public static ScreenerService getScreenerService() {
 		return screenerService;
 	}
 
 	public void setScreenerService(ScreenerService screenerService) {
 		SATExpression.screenerService = screenerService;
 	}
 
 	public static WatchlistService getWatchlistService() {
 		return watchlistService;
 	}
 
 	public void setWatchlistService(WatchlistService watchlistService) {
 		SATExpression.watchlistService = watchlistService;
 	}
 
 	public static AlertService getAlertService() {
 		return alertService;
 	}
 
 	public void setAlertService(AlertService alertService) {
 		SATExpression.alertService = alertService;
 	}
 
 	public static PortfolioService getPortfolioService() {
 		return portfolioService;
 	}
 
 	public void setPortfolioService(PortfolioService portfolioService) {
 		SATExpression.portfolioService = portfolioService;
 	}
 
 	public static TradeStrategyService getTradeStrategyService() {
 		return tradeStrategyService;
 	}
 
 	public void setTradeStrategyService(TradeStrategyService tradeStrategyService) {
 		SATExpression.tradeStrategyService = tradeStrategyService;
 	}
 
 	public static AlertReceiverService getAlertReceiverService() {
 		return alertReceiverService;
 	}
 
 	public void setAlertReceiverService(AlertReceiverService alertReceiverService) {
 		SATExpression.alertReceiverService = alertReceiverService;
 	}
 
 	public static AdaptDAO getAdaptDAO() {
 		return adaptDAO;
 	}
 
 	public void setAdaptDAO(AdaptDAO adaptDAO) {
 		SATExpression.adaptDAO = adaptDAO;
 	}
 
 	public int getGenomeLength(long groupId) {
 		Group group = adaptDAO.findGroup(groupId);
 		return 1 + SCREEN_GENE_LENGTH * group.getInteger("Express.NumberOfScreens") +
 				group.getInteger("Express.MaxSymbolsPerScreen") * 
 					ALERT_GENE_LENGTH * group.getInteger("Express.AlertsPerSymbol") +
 				group.getInteger("Express.MaxSymbolsPerScreen") * 
 					group.getInteger("Express.AlertsPerSymbol") * TRADE_GENE_LENGTH;
 	}
 
 	private int getScreenStartPosition() {
 		return SCREEN_SORT_POSITION + 1;
 	}
 
 	/**
 	 * Creates a set of SelectedScreenCriteria based on a candidate's screener genes
 	 * Screen Gene Data Map
 	 * 1. Criteria to use
 	 * 2. Criteria value
 	 * 
 	 * @param candidate A candidate
 	 * @param group The candidates group
 	 * @return A set of screener criteria selected by this candidate
 	 */
 	public SavedScreen[] expressScreenerGenes(Candidate candidate, Group group) 
 			throws Exception {
 		
 		int[] genome = candidate.getGenome();
 		long candidateId = candidate.getCandidateId();
 
 		// Get screener possibilities
 		AvailableScreenCriteria[] availableScreenCriteria = 
 				screenerService.getAvailableCriteria(group.getGroupId());
 		if (availableScreenCriteria == null || availableScreenCriteria.length <= 0 || 
 				availableScreenCriteria[0] == null) {
 			throw new Exception("No available screen criteria for " + group.getGroupId());
 		}
 
 		// Start at the first screen gene
 		int position = getScreenStartPosition();
 		ArrayList<SavedScreen> selected = new ArrayList<SavedScreen>();
 		int screens = group.getInteger("Express.NumberOfScreens");
 		int geneUpperValue = group.getInteger("Evolve.GeneUpperValue");
 		for (int i=0; i<screens; i++) {
 
 			int criteriaIndex = transpose(genome[position], geneUpperValue, 
 											0, availableScreenCriteria.length - 1);
 			String name = availableScreenCriteria[criteriaIndex].getName();
 			int valueIndex = transpose(genome[position + 1], geneUpperValue, 0, 
 							availableScreenCriteria[criteriaIndex].getAcceptedValues().length - 1);
 			String value = availableScreenCriteria[criteriaIndex].getAcceptedValue(valueIndex);
 			String argOp = availableScreenCriteria[criteriaIndex].getArgsOperator();
 			selected.add(new SavedScreen(candidateId, name, value, argOp));
 			
 			position += SCREEN_GENE_LENGTH;
 		}
 		return selected.toArray(new SavedScreen[selected.size()]);
 	}
 	
 	public String[] getScreenSymbols(Candidate candidate, Group group, 
 										SelectedScreenCriteria[] screenCriteria) throws Exception {
 		String[] symbols = null;
 		int[] genome = candidate.getGenome();
 
 		int sortGene = transpose(genome[SCREEN_SORT_POSITION], group.getInteger("Evolve.GeneUpperValue"), 
 									0, screenCriteria.length - 1);
 
 		String[] screenSymbols = screenerService.screen(
 										group.getGroupId(),
 										screenCriteria,
 										screenCriteria[sortGene].getName(),
 										group.getInteger("Express.MaxSymbolsPerScreen"));
 		
 		if (screenSymbols == null) {
 			throw new Exception("No symbols found for candidate " + candidate.getCandidateId());
 		}
 		if (screenSymbols.length > group.getInteger("Express.MaxSymbolsPerScreen")) {
 			symbols = new String[group.getInteger("Express.MaxSymbolsPerScreen")];
 			System.arraycopy(screenSymbols, 0, symbols, 0, group.getInteger("Express.MaxSymbolsPerScreen"));
 		} else {
 			symbols = new String[screenSymbols.length];
 			System.arraycopy(screenSymbols, 0, symbols, 0, symbols.length);
 		}
 		
 		return symbols;
 	}
 	
 	String setupWatchlist(Candidate candidate, Group group, String[] symbols) throws Exception {
 		String watchlistId = null;
 		long candidateId = candidate.getCandidateId();
 		long groupId = group.getGroupId();
 		String name = WATCH_NAME_PREFIX + GROUP + groupId + CANDIDATE + candidateId;
 		watchlistId = watchlistService.create(candidateId, name);
 		if (watchlistId == null) {
 			throw new Exception("Error creating watchlist: " + name);
 		}
 
 		// Add stocks to a watchlist
 		for (int i=0; i<symbols.length; i++) {
 			if (symbols[i] != null) {
 				watchlistService.addHolding(candidateId, watchlistId, symbols[i]);
 			}
 		}
 		
 		return watchlistId;
 	}
 	
 	String setupPortfolio(Candidate candidate, Group group) throws Exception {
 		String portfolioId = null;
 		long candidateId = candidate.getCandidateId();
 		String name = PORT_NAME_PREFIX + GROUP + group.getGroupId() + CANDIDATE + candidateId;
 		portfolioId = portfolioService.create(candidateId, name);
 		if (portfolioId == null) {
 			throw new Exception("Error creating portfolio: " + name);
 		}
 		portfolioService.addCashTransaction(candidateId, portfolioId, 
 											group.getDouble("Express.StartingCash"), 
 											true, true);
 		
 		return portfolioId;
 	}
 	
 	private int getAlertStartPosition(Group group) {
 		return getScreenStartPosition() + SCREEN_GENE_LENGTH * group.getInteger("Express.NumberOfScreens");
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
 	public SavedAlert[] expressAlertGenes(Candidate candidate, Group group, 
 			String[] symbols) throws Exception {
 
 		int[] genome = candidate.getGenome();
 		long candidateId = candidate.getCandidateId();
 		
 		// Get alert possibilities
 		AvailableAlert[] availableAlerts = alertService.getAvailableAlerts(group.getGroupId());
 		if (availableAlerts == null) {
 			throw new Exception("No available alerts for " + group.getGroupId());
 		}
 		
 		SavedAlert[] selected = new SavedAlert[symbols.length * group.getInteger("Express.AlertsPerSymbol")];
 		
 		int position = getAlertStartPosition(group);
 		int s = 0;
 		for (int i=0; i<symbols.length; i++) {
 			for (int j=0; j<group.getInteger("Express.AlertsPerSymbol"); j++) {
 
 				AvailableAlert alert = availableAlerts[transpose(genome[position],  
 															group.getInteger("Evolve.GeneUpperValue"),
 															0, availableAlerts.length - 1)];
 				int alertId = alert.getType();
 				String[] criteriaTypes = alert.getCriteriaTypes();
 				double[] params = new double[criteriaTypes.length];
 				for (int k=0; k<criteriaTypes.length; k++) {
 					if (criteriaTypes[k].equals(AvailableAlert.DOUBLE)) {
 						double upper = alertService.getUpperDouble(group.getGroupId(), alertId, symbols[i], k);
 						double lower = alertService.getLowerDouble(group.getGroupId(), alertId, symbols[i], k);
 						params[k] = transpose(genome[position + 1], group.getInteger("Evolve.GeneUpperValue"),
 												lower, upper);
 					} else if (criteriaTypes[k].equals(AvailableAlert.LIST)) {
 						int upper = alertService.getListLength(group.getGroupId(), alertId, k);
 						params[k] = transpose(genome[position + 1], group.getInteger("Evolve.GeneUpperValue"), 
 												0, upper);
 					}
 				}
 				String selectedCondition = alertService.parseCondition(alert, symbols[i], 
 																		params);
 				selected[s] = new SavedAlert(candidateId, alertId, selectedCondition, 
 													symbols[i], params);
 				s++;
 				position += ALERT_GENE_LENGTH;
 			}
 		}
 		
 		return selected;
 	}
 	
 	SavedAlert[] setupAlerts(Group group, SavedAlert[] openAlerts) throws Exception {
 
 		alertService.addAlertDestination(group.getGroupId(), 
 										group.getString("Alert.User"), 
 										group.getString("Alert.ReceiverType"));
 
 		for (SavedAlert alert : openAlerts) {
 			alert.setAlertId(
 				alertService.setupAlert(group.getGroupId(), alert.getAlertType(), 
 						alert.getCondition(), alert.getSymbol(), alert.getParams()));
 		}
 		return openAlerts;
 	}
 
 	/**
 	 * Creates a set of Trades based on a candidate's trade genes
 	 * 
 	 * Trade Gene Data Map
 	 * 1. Order Type
 	 * 2. Allocation
 	 * 3. Acceptable Loss
 	 * 4. Time in Trade
 	 * 5. Close or Adjust At
 	 * @param candidate A candidate
 	 * @param group The candidate's group
 	 * @param symbols The symbols found during screening
 	 * @return A set of strategy parameters selected for this candidate associated with each symbol
 	 */
 	public HashMap<String, ArrayList<TradeParameter>> expressTradeGenes(Candidate candidate, Group group, 
 									String[] symbols) throws Exception {
 
 		String tradeStrategy = group.getString("Trade.Strategy");
 		int[] genome = candidate.getGenome();
 		long candidateId = candidate.getCandidateId();
 		
 		AvailableStrategyParameter[] availableParameters = tradeStrategyService.availableTradeParameters(
 																tradeStrategy);
 		
 		int position = getAlertStartPosition(group) + 
 				group.getInteger("Express.MaxSymbolsPerScreen") * ALERT_GENE_LENGTH * 
 				group.getInteger("Express.AlertsPerSymbol");
 
 		HashMap<String, ArrayList<TradeParameter>> trades = 
 					new HashMap<String, ArrayList<TradeParameter>>();
 		ArrayList<TradeParameter> tradeParams;
 		for (int i=0; i<symbols.length; i++) {
 			
 			tradeParams = new ArrayList<TradeParameter>(availableParameters.length);
 			for (int j=0; j<availableParameters.length; j++) {
 				tradeParams.add(new TradeParameter(
 						candidateId,
 						symbols[i], 
 						availableParameters[j].getName(),
 						transpose(genome[position + j],
 								group.getInteger("Evolve.GeneUpperValue"),
 								availableParameters[j].getLower(), 
 								availableParameters[j].getUpper())));
 			}
 			
 			trades.put(symbols[i], tradeParams);
 			position += TRADE_GENE_LENGTH;
 		}
 		
 		return trades;
 	}
 	
 	/**
 	 * Creates trades and the events that will open them
 	 * @param candidate
 	 * @param group
 	 * @param numberOfSymbols
 	 * @param alerts SelectedAlerts
 	 * @param alertIds
 	 * @return An array of trade Ids
 	 * @throws Exception
 	 */
 	void createTrades(Candidate candidate, Group group, SavedAlert[] alerts,  
 						HashMap<String, ArrayList<TradeParameter>> trades) throws Exception {
 
 		long candidateId = candidate.getCandidateId();
 		String portfolioId = candidate.getPortfolioId();
 		String tradeStrategy = group.getString("Trade.Strategy");
 		String actionType = tradeStrategyService.tradeActionToStart(tradeStrategy);
 
 		long tradeId;
 		for (SavedAlert alert : alerts) {
 			if (alert.getAlertId() != null && alert.getAlertId() != "") {
 				tradeId = tradeStrategyService.addTrade(tradeStrategy, candidateId, 
 						portfolioId, group.getGroupId(), alert.getSymbol());
 
 				tradeStrategyService.setTradeEvent(tradeId, alert.getCondition(), 
 													actionType, alert.getAlertId());
 				
 				for (TradeParameter p : trades.get(alert.getSymbol())) {
					tradeStrategyService.setTradeParameter(p.getTradeId(), 
 							p.getName(), p.getValue());
 				}
 
 				logger.trace("New Trade for {}. TradeId: {}. AlertId={}: alert={}", 
 						new Object[]{candidateId, tradeId, alert.getAlertId(), alert.getCondition()});
 			} else {
 				logger.debug("Missed Trade for {}. No AlertId for symbol {}", 
 						candidateId, alert.getSymbol());
 			}
 			
 		}
 	}
 
 	@Override
 	public void beforeExpression(long populationId) {
 
 		Group group = adaptDAO.findGroup(populationId);
 		long receiverId = group.getLong("Alert.ReceiverId");
 		
 		// Check if there's already an alert receiver
 		if (receiverId == 0) {
 			// No receiver so set one up
 			
 			receiverId = alertReceiverService.addReceiver(populationId, group.getString("Alert.ReceiverType"));
 			group.setLong("Alert.ReceiverId", receiverId);
 
         	alertReceiverService.setReceiverParameter(receiverId, "Host", group.getString("Alert.Host"));
         	alertReceiverService.setReceiverParameter(receiverId, "User", group.getString("Alert.User"));
         	alertReceiverService.setReceiverParameter(receiverId, "Password", group.getString("Alert.Password"));
         	alertReceiverService.setReceiverParameter(receiverId, "Period", group.getString("Alert.Period"));
         	alertReceiverService.setReceiverParameter(receiverId, "Initial Delay", group.getString("Alert.Delay"));
         	alertReceiverService.setReceiverActive(receiverId, true);
         	adaptDAO.updateGroup(group);
 		}
 		
 		alertReceiverService.startReceiving(receiverId);
 	}
 
 	@Override
 	public Candidate express(int[] genome, long groupId) {
 		
 		if (genome == null || genome.length == 0) {
 			logger.debug("No genome to express for group: {}.", groupId);
 			return null;
 		}
 
 		// Find the group
 		Group group = adaptDAO.findGroup(groupId);
 		
 		Candidate candidate;
 		try {
 			candidate = adaptDAO.findCandidateByGenome(genome);
 		} catch (Exception e1) {
 			logger.trace("Candidate not in database. Must be new.", e1);
 			candidate = new Candidate();
 			candidate.setGenome(genome);
 			candidate.setGroupId(groupId);
 			candidate.setBornInGen(group.getInteger("Evolve.Generation"));
 			
 			adaptDAO.addCandidate(candidate, groupId);
 		}
 		candidate.setLastExpressedGen(group.getInteger("Evolve.Generation"));
 		
 
 		try {
 			// Get criteria to screen against
 			SelectedScreenCriteria[] screenCriteria = expressScreenerGenes(candidate, group);
 
 			if (screenCriteria.length == 0) {
 				// All of the screen symbols are turned off and we won't get any symbols from screening
 				logger.debug("No active screen criteria for {}.", candidate.getCandidateId());
 				return candidate;
 			}
 			
 			// Get a list of symbols from the Screener Service
 			String[] symbols = getScreenSymbols(candidate, group, screenCriteria);
 			
 			// If the screener didn't produce any symbols there's no point using the other services
 			if (symbols.length == 0) {
 				logger.debug("No symbols found for candidate {}.", candidate.getCandidateId());
 				return candidate;
 			}
 			
 			// Create a watchlist
 			String watchlistId = setupWatchlist(candidate, group, symbols);
 			candidate.setWatchlistId(watchlistId);
 			
 			// Prepare portfolio
 			String portfolioId = setupPortfolio(candidate, group);
 
 			// No point continuing if there's no portfolio to track trades
 			if (portfolioId == null || portfolioId == "") {
 				logger.debug("Unable to create portfolio for {}.", candidate.getCandidateId());
 				return candidate;
 			} else {
 				candidate.setPortfolioId(portfolioId);
 			}
 
 			// Create (symbols*alertsPerSymbol) alerts for stocks
 			SavedAlert[] openAlerts = expressAlertGenes(candidate, group, symbols);
 			openAlerts = setupAlerts(group, openAlerts);
 			
 			// Create (symbol*alertsPerSymbol) trades to be made when alerts are triggered
 			HashMap<String, ArrayList<TradeParameter>> trades = expressTradeGenes(candidate, group, symbols);
 			createTrades(candidate, group, openAlerts, trades);
 			
 		} catch (Exception e) {
 			logger.warn("Unable to express candidate {}", candidate.getCandidateId(), e);
 		}
 
 		// Save candidate to database, and return
 		adaptDAO.updateCandidate(candidate);
 		
 		return candidate;
 	}
 
 	@Override
 	public void candidatesExpressed(
 			List<ExpressedCandidate<int[]>> expressedCandidates, long populationId) {
 		
 		Group group = adaptDAO.findGroup(populationId);
 		int currentGeneration = group.getInteger("Evolve.Generation");
 		
 		// Remove old candidates
 		List<Candidate> oldCandidates = adaptDAO.findCandidatesInGroup(populationId);
 		for (Candidate c : oldCandidates) {
 			if (c.getLastExpressedGen() < currentGeneration) {
 				destroy(c.getGenome(), populationId);
 			}
 		}
 		
 		// Determine average Hamming distance of existing candidates
 		int[] genomeA;
 		int[] genomeB;
 		int distance;
 		int hammingPairings = 0;
 		double hammingSum = 0.0;
 		for (int i=0; i<expressedCandidates.size(); i++) {
 			genomeA = expressedCandidates.get(i).getGenome();
 			for (int j=i+1; j<expressedCandidates.size(); j++) {
 				genomeB = expressedCandidates.get(j).getGenome();
 				distance = 0;
 				for (int k = 0; k < genomeA.length; k++) {
 					if (genomeA[k] != genomeB[k]) {
 						distance++;
 					}
 				}
 				hammingPairings++;
 				hammingSum += distance;
 			}
 		}
 		
 		double meanHammingDistance = hammingSum / hammingPairings;
 		
 		group.setDouble("Express.Variability", meanHammingDistance);
 		adaptDAO.updateGroup(group);
 		
 	}
 
 	@Override
 	public void destroy(int[] genome, long populationId) {
 		
 		try {
 			Candidate c = adaptDAO.findCandidateByGenome(genome);
 			
 			adaptDAO.removeCandidate(c.getCandidateId());
 
 			// Remove alert trade mapping
 			tradeStrategyService.removeAllTrades(c.getCandidateId());
 			
 			// Remove Alerts
 			alertService.removeAllAlerts(c.getGroupId());
 
 			// Remove Portfolio
 			if (c.getPortfolioId() != null) {
 				portfolioService.delete(c.getCandidateId(), c.getPortfolioId());
 			}
 
 			// Remove Watchlist
 			if (c.getWatchlistId() != null) {
 				watchlistService.delete(c.getCandidateId(), c.getWatchlistId());
 			}
 			
 		} catch (Exception e) {
 			logger.warn("Unable to destroy candidate with genome: {}.", Arrays.toString(genome), e);
 		}		
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
 	
 	public Trader setupTrader(Candidate candidate, Group group, Trader trader) 
 			throws Exception {
 
 		SavedScreen[] screenCriteria = expressScreenerGenes(candidate, group);
 		if (screenCriteria.length == 0) {
 			return trader;
 		}
 		for (SavedScreen criteria : screenCriteria) {
 			adaptDAO.addSavedScreen(criteria, trader.getTraderId());
 		}
 		
 		String[] symbols = getScreenSymbols(candidate, group, screenCriteria);
 		if (symbols == null || symbols.length == 0) {
 			return trader;
 		}
 		for (String symbol : symbols) {
 			adaptDAO.addSymbol(symbol, trader.getTraderId());
 		}
 		
 		SavedAlert[] alerts = expressAlertGenes(candidate, group, symbols);
 		if (alerts.length == 0) {
 			return trader;
 		}
 		for (SavedAlert alert : alerts) {
 			adaptDAO.addSavedAlert(alert, trader.getTraderId());
 		}
 
 		HashMap<String, ArrayList<TradeParameter>> trades = expressTradeGenes(candidate, group, symbols);
 		for (String key : trades.keySet()) {
 			ArrayList<TradeParameter> params = trades.get(key);
 			for (TradeParameter p : params) {
 				adaptDAO.addTradeParamter(p, trader.getTraderId());
 			}
 		}
 		return trader;
 	}
 }
