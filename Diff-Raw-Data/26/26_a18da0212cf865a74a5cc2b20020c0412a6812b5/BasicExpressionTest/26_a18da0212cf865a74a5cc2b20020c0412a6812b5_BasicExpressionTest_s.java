 package com.myrontuttle.fin.trade.adapt.express;
 
 import static org.junit.Assert.*;
 import static org.mockito.Mockito.*;
 
 import java.util.ArrayList;
 import java.util.Collection;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.Mock;
 import org.mockito.Mockito;
 import org.mockito.invocation.InvocationOnMock;
 import org.mockito.stubbing.Answer;
 
 import com.myrontuttle.fin.trade.adapt.Candidate;
 import com.myrontuttle.fin.trade.adapt.Group;
 import com.myrontuttle.fin.trade.adapt.GroupDAO;
 import com.myrontuttle.fin.trade.adapt.express.BasicExpression;
 import com.myrontuttle.fin.trade.api.AlertReceiverService;
 import com.myrontuttle.fin.trade.api.AlertService;
 import com.myrontuttle.fin.trade.api.AvailableAlert;
 import com.myrontuttle.fin.trade.api.AvailableScreenCriteria;
 import com.myrontuttle.fin.trade.api.PortfolioService;
 import com.myrontuttle.fin.trade.api.QuoteService;
 import com.myrontuttle.fin.trade.api.ScreenerService;
 import com.myrontuttle.fin.trade.api.SelectedAlert;
 import com.myrontuttle.fin.trade.api.SelectedScreenCriteria;
 import com.myrontuttle.fin.trade.api.Trade;
 import com.myrontuttle.fin.trade.api.TradeParameter;
 import com.myrontuttle.fin.trade.api.TradeStrategy;
 import com.myrontuttle.fin.trade.api.TradeStrategyService;
 import com.myrontuttle.fin.trade.api.WatchlistService;
 import com.myrontuttle.fin.trade.strategies.AlertTrade;
 import com.myrontuttle.fin.trade.strategies.BoundedStrategy;
 import com.myrontuttle.fin.trade.strategies.BoundedWAdjustStrategy;
 
 public class BasicExpressionTest {
 
 	private final static String CID = "candidateID";
 	private final static String WID = "watchlistID";
 	private final static String PID = "portfolioID";
 	private final static String GID = "groupID";
 	private final static String LID = "LotID";
 	private final static String EMAIL = "test@test.com";
 	private final double STARTING_CASH = 10000.00;
 	private final static String BUY = "Buy";
 	private final static String SELL = "Sell";
 	private final static String SHORT = "ShortSell";
 	private final static String COVER = "BuyToCover";
 	private final static String BOUNDED_STRAT = "Bounded Strategy";
 
 	private static final int SCREEN_GENES = 3;
 	private static final int MAX_SYMBOLS_PER_SCREEN = 5;
 	private static final int ALERTS_PER_SYMBOL = 1;
 	private static final int GENE_UPPER_VALUE = 100;
 	
 	private ScreenerService screenerService;
 	private WatchlistService watchlistService;
 	private AlertService alertService;
 	private PortfolioService portfolioService;
 	private QuoteService quoteService;
 	private TradeStrategyService strategyService;
 	private AlertReceiverService alertReceiver;
 	private TradeStrategy tradeStrategy;
 	private GroupDAO groupDAO;
 	
 	private BasicExpression<int[]> expression;
 	
 	private Candidate candidateA;
 	private Group group1;
 	private int[] genomeA = new int[]{1	// Sort by first screener gene
 									,55,20,20		// 1st screener gene
 									,25,20,20		// 2nd screener gene (inactive)
 									,100,75,50		// 3rd screener gene
 									,19,50,0,0  	// 1st alert gene
 									,12,25,0,0		// 2nd alert gene
 									,1,2,3,4		// 3rd alert gene
 									,8,7,6,5		// 4th alert gene
 									,11,21,22,23	// 5th alert gene
 									,19,37,24,0,88  	// 1st trade gene
 									,55,25,66,100,75	// 2nd trade gene
 									,1,2,3,4,5			// 3rd trade gene
 									,8,7,6,5,6			// 4th trade gene
 									,11,21,22,23,5		// 5th trade gene
 									};
 	private AvailableScreenCriteria[] availableScreenCriteria = 
 				new AvailableScreenCriteria[]{
 					new AvailableScreenCriteria("RCCAssetClass", "OR", 
 							new String[]{"LIKE[0]=Large Cap",
 										 "LIKE[0]=Micro Cap",
 										 "LIKE[0]=Mid Cap",
 										 "LIKE[0]=Small Cap"}),
 					new AvailableScreenCriteria("RCCRegion", "OR", 
 							new String[]{"LIKE[0]=",
 										 "LIKE[0]=Africa & Mideast",
 										 "LIKE[0]=Americas",
 										 "LIKE[0]=Asia-Pacific",
 										 "LIKE[0]=Europe"})
 	};
 	private SelectedScreenCriteria[] selectedScreenCriteria =
 			new SelectedScreenCriteria[]{
 				new SelectedScreenCriteria("RCCAssetClass", "LIKE[0]=Large Cap", "OR"),
 				new SelectedScreenCriteria("RCCRegion", "LIKE[0]=Americas", "OR")
 	};
 	private String[] screenSymbols = new String[]{"AAPL", "MSFT"};
 	
 	private String watchlistName = BasicExpression.WATCH_NAME_PREFIX + "1";
 	private String portfolioName = BasicExpression.PORT_NAME_PREFIX + "1";
 
 	private final int alertId = 1;
 	private final String condition = "{symbol}'s price fell below {Price}";
 	private final String actualCondition = "AAPL's price fell below 400";
 	private final double alertUpper = 500.0;
 	private final double alertLower = 300.0;
 	private final int alertListLength = 0;
 	private AvailableAlert priceBelowAlert = new AvailableAlert(alertId, condition, 
 			new String[]{"DOUBLE"}, 
 			new String[]{"Price"}, new String[]{"Fund.Price.Low.Lifetime"}, 
 			new String[]{"Quote.Value.Last"}, null);
 	private AvailableAlert[] availableAlerts = new AvailableAlert[]{
 			priceBelowAlert
 	};
 	private SelectedAlert[] selectedAlerts = new SelectedAlert[]{
 			new SelectedAlert(alertId, condition, "AAPL", new double[]{400})
 	};
 	
 	private String[] openOrderTypes = new String[]{ BUY, SHORT };
 
 	TradeParameter[] params1 = new TradeParameter[]{
 			new TradeParameter(BoundedWAdjustStrategy.OPEN_ORDER, 0),
 			new TradeParameter(BoundedWAdjustStrategy.TRADE_ALLOC, 37),
 			new TradeParameter(BoundedWAdjustStrategy.PERCENT_BELOW, 24),
 			new TradeParameter(BoundedWAdjustStrategy.TIME_LIMIT, 60),
 			new TradeParameter(BoundedWAdjustStrategy.PERCENT_ABOVE, 88)
 	};
 	TradeParameter[] params2 = new TradeParameter[]{
 			new TradeParameter(BoundedWAdjustStrategy.OPEN_ORDER, 1),
 			new TradeParameter(BoundedWAdjustStrategy.TRADE_ALLOC, 25),
 			new TradeParameter(BoundedWAdjustStrategy.PERCENT_BELOW, 66),
 			new TradeParameter(BoundedWAdjustStrategy.TIME_LIMIT, 86400),
 			new TradeParameter(BoundedWAdjustStrategy.PERCENT_ABOVE, 75)
 	};
 	private Trade[] trades = new Trade[]{
 			new Trade(screenSymbols[0], params1),
 			new Trade(screenSymbols[1], params2)
 	};
 	private AlertTrade[] alertTrade = new AlertTrade[] {
 			new AlertTrade(selectedAlerts[0], PID, trades[0])
 	};
 	
 	@Before
 	public void setUp() throws Exception {
 
 		// Set variables
 		candidateA = new Candidate();
 		candidateA.setCandidateId(CID);
 		candidateA.setGenomeString(Candidate.generateGenomeString(genomeA));
 		candidateA.setGroupId(GID);
 		candidateA.setPortfolioId(PID);
 		candidateA.setWatchlistId(WID);
 		Collection<Candidate> candidates = new ArrayList<Candidate>();
 		candidates.add(candidateA);
 		
 		group1 = new Group();
 		group1.setGroupId(GID);
 		group1.setNumberOfScreens(SCREEN_GENES);
 		group1.setMaxSymbolsPerScreen(MAX_SYMBOLS_PER_SCREEN);
 		group1.setAlertsPerSymbol(ALERTS_PER_SYMBOL);
 		group1.setGeneUpperValue(100);
 		group1.setActive(true);
 		group1.setStartingCash(STARTING_CASH);
 		group1.setAlertAddress(EMAIL);
 		group1.setCandidates(candidates);
 		group1.setExpressionStrategy("BasicExpression");
 		group1.setTradeStrategy(BOUNDED_STRAT);
 		
 	    // Create mocks
 		screenerService = mock(ScreenerService.class);
 		watchlistService = mock(WatchlistService.class);
 		alertService = mock(AlertService.class);
 		portfolioService = mock(PortfolioService.class);
 		quoteService = mock(QuoteService.class);
 		strategyService = mock(TradeStrategyService.class);
 		alertReceiver = mock(AlertReceiverService.class);
 		groupDAO = mock(GroupDAO.class);
 		tradeStrategy = new BoundedStrategy(portfolioService, quoteService, alertService, alertReceiver);
 		
 		// Describe Mocks
 		when(screenerService.getAvailableCriteria(GID)).thenReturn(availableScreenCriteria);
 		when(screenerService.screen(GID, 
 									selectedScreenCriteria, 
 									selectedScreenCriteria[0].getName(), 
 									MAX_SYMBOLS_PER_SCREEN))
 			.thenReturn(screenSymbols);
 		
 		when(watchlistService.create(CID, watchlistName)).thenReturn(WID);
 		when(watchlistService.addHolding(CID, WID, screenSymbols[0])).thenReturn(LID);
 		when(watchlistService.addHolding(CID, WID, screenSymbols[1])).thenReturn(LID);
 		when(portfolioService.create(CID, portfolioName)).thenReturn(PID);
 		when(portfolioService.addCashTransaction(CID, PID, STARTING_CASH, 
 											true, true)).thenReturn(true);
 		when(portfolioService.openOrderTypesAvailable(CID)).thenReturn(openOrderTypes);
 		
 		when(alertService.getAvailableAlerts(GID)).thenReturn(availableAlerts);
 		when(alertService.getUpperDouble(GID, alertId, screenSymbols[0], 0)).thenReturn(alertUpper);
 		when(alertService.getLowerDouble(GID, alertId, screenSymbols[0], 0)).thenReturn(alertLower);
 		when(alertService.getListLength(GID, alertId, 0)).thenReturn(alertListLength);
 		when(alertService.parseCondition(priceBelowAlert, screenSymbols[0], new double[]{400.0})).
 			thenReturn(actualCondition);
 		when(alertService.addAlertDestination(GID, EMAIL, "EMAIL")).thenReturn(true);
 		when(alertService.setupAlerts(GID, selectedAlerts)).thenReturn(true);
 		
 		when(strategyService.getTradeStrategy(BOUNDED_STRAT)).thenReturn(tradeStrategy);
 		
 		when(groupDAO.findGroup(GID)).thenReturn(group1);
 		doAnswer(new Answer<Candidate>() {
 		      public Candidate answer(InvocationOnMock invocation) {
 		          Candidate candidate = (Candidate)invocation.getArguments()[0];
 		          candidate.setCandidateId(CID);
 		          candidate.setGroupId(GID);
 		          return candidate;
 		      }}).when(groupDAO).addCandidate(any(Candidate.class), eq(GID));
 		
 		//when(alertReceiver.watchFor(alertTradeBounds));
 		
 		// Assign services
 		expression = new BasicExpression<int[]>();
 		expression.setAlertReceiver(alertReceiver);
 		expression.setAlertService(alertService);
 		expression.setTradeStrategyService(strategyService);
 		expression.setPortfolioService(portfolioService);
 		expression.setWatchlistService(watchlistService);
 		expression.setScreenerService(screenerService);
		expression.setStrategyDAO(groupDAO);
 	}
 
 	@Test
 	public void testExpressScreenerGenes() {
 		SelectedScreenCriteria[] screenCriteria = 
 				expression.expressScreenerGenes(GID, genomeA, SCREEN_GENES, GENE_UPPER_VALUE);
 		
 		assertTrue(screenCriteria.length > 0);
 		assertEquals("RCCAssetClass",screenCriteria[0].getName());
 		assertEquals("LIKE[0]=Large Cap",screenCriteria[0].getValue());
 	}
 	
 	@Test
 	public void testGetScreenSymbols() {
 		String[] symbols = expression.getScreenSymbols(genomeA, GID, group1);
 		for (int i=0; i<symbols.length; i++) {
 			assertEquals(symbols[i], screenSymbols[i]);
 		}
 	}
 	
 	@Test
 	public void testSetupWatchlist() {
 		assertEquals(WID, expression.setupWatchlist(CID, screenSymbols));
 	}
 	
 	@Test
 	public void testSetupPortfolio() {
 		assertEquals(PID, expression.setupPortfolio(CID, STARTING_CASH));
 	}
 	
 	@Test
 	public void testExpressAlertGenes() {
 		SelectedAlert[] selectedAlerts = expression.expressAlertGenes(GID, genomeA, 
 																	screenSymbols, group1);
 		
 		assertTrue(selectedAlerts.length > 0);
 		assertEquals(actualCondition, selectedAlerts[0].getCondition());
 		assertEquals(screenSymbols[0], selectedAlerts[0].getSymbol());
 		assertTrue(selectedAlerts[0].getParam(0) > alertLower && 
 						selectedAlerts[0].getParam(0) < alertUpper);
 	}
 	
 	@Test
 	public void testSetupAlerts() {
 		expression.setupAlerts(GID, selectedAlerts, EMAIL);
 	}
 	
 	@Test
 	public void testExpressTradeGenes() {
 		Trade[] trades = expression.expressTradeGenes(CID, genomeA, screenSymbols, group1);
 		for (int i=0; i<trades.length; i++) {
 			assertTrue(trades[i].equals(trades[i]));
 		}
 	}
 	
 	@Test
 	public void testSetupAlertReceiver() {
 		expression.setupAlertReceiver(selectedAlerts, PID, trades);
 	}
 	
 	@Test
 	public void testExpress() {
 		Candidate candidateB = expression.express(genomeA, GID);
 		assertTrue(candidateA.equals(candidateB));
 	}
 
 }
