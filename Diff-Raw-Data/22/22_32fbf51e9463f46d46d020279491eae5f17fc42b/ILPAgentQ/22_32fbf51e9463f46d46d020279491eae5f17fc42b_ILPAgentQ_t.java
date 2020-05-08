 package agents;
 
 import ilog.concert.IloException;
 import ilog.concert.IloIntVar;
 import ilog.concert.IloLinearNumExpr;
 import ilog.cplex.IloCplex;
 
 import java.awt.Toolkit;
 import java.io.BufferedWriter;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedHashSet;
 import java.util.Map;
 import java.util.Random;
 import java.util.Set;
 
 import newmodels.AbstractModel;
 import newmodels.bidtocpc.AbstractBidToCPC;
 import newmodels.bidtocpc.BasicBidToCPC;
 import newmodels.bidtocpc.RegressionBidToCPC;
 import newmodels.bidtonumclicks.AbstractBidToNumClicks;
 import newmodels.bidtonumclicks.BasicBidToNumClicks;
 import newmodels.bidtoprclick.AbstractBidToPrClick;
 import newmodels.bidtoprclick.BasicBidToPrClick;
 import newmodels.bidtoprclick.RegressionBidToPrClick;
 import newmodels.bidtoprconv.AbstractBidToPrConv;
 import newmodels.bidtoprconv.BasicBidToPrConv;
 import newmodels.prconv.TrinaryPrConversion.GetsBonus;
 import newmodels.querytonumimp.AbstractQueryToNumImp;
 import newmodels.querytonumimp.BasicQueryToNumImp;
 import newmodels.unitssold.AbstractUnitsSoldModel;
 import newmodels.unitssold.UnitsSoldMovingAvg;
 import newmodels.usermodel.AbstractUserModel;
 import newmodels.usermodel.BasicUserModel;
 import usermodel.UserState;
 import edu.umich.eecs.tac.props.Ad;
 import edu.umich.eecs.tac.props.BidBundle;
 import edu.umich.eecs.tac.props.Product;
 import edu.umich.eecs.tac.props.Query;
 import edu.umich.eecs.tac.props.QueryReport;
 import edu.umich.eecs.tac.props.QueryType;
 import edu.umich.eecs.tac.props.SalesReport;
 
 public class ILPAgentQ extends SimAbstractAgent{
 	
 	// ########################
 	// #### Game Decisions ####
 	// ########################
 	private Random _R = new Random();
 	final static boolean USEOVERQUANTITY = false; 	// Do you want the MIP formula to include over capacity?
 	final static int DAYSUNTILILKE = 70; 			// How many days until the Bid-Slot bucket model starts to work?
 	protected static int NUMOFUSERS = 90000;		// How many users are simulated?
 	double _regReserveScore = 0.2;					// I want the bids to be at least the regular reserve score
 	
 	
 	// ###################
 	// #### Variables ####
 	// ###################
 	/// Data from server 
 	protected static int NUMOFQUERIES;		// the size of the query space
 	protected static int DAILYCAPACITY;		// the daily capacity
 	protected static double DISCOUNTER;		// the discounter for the over capacity
 	
 	/// Agent variables
 	protected Set<Double> _possibleBids;
 	protected Set<Integer> _possibleQuantities;	// a vector of all the quantities that we are willing to over sell 
 	protected static Map <Product , Double> _productRevenue;	// the revenue from every product, including component specialty
 	protected HashMap<Query, Double> _bids;					// a set of our decision for bidding on every query. 0 if no bidding
 	protected HashMap<Query, Double> _dailyLimit;		// daily limit per query
 	protected HashMap<Integer, Query> _queryIndexing;	// mapping queries into integers - it help with the cplex variable array
 	protected Set<UserState> searchingUserStates;
 	protected int _numSearchingUsers = 0;
 	protected HashMap<Query, HashMap<String,Double>> _previousDayData;
 	private int _wantedSales;	// how much we want to sell in a specific day
 	FileWriter _fstream;
 	BufferedWriter _fout;
 	protected BidBundle _bidBundle;
 
 	
 	private AbstractUserModel _userModel;	// the number of users in every state
 	private AbstractBidToPrClick _bidToClickPrModel; // the click probability for a bid in a query 
 	private AbstractBidToCPC _bidToCPCModel;
 	private AbstractBidToPrConv _convPrModel;
 	private AbstractUnitsSoldModel _unitsSold;
 	private AbstractQueryToNumImp _queryToNumImpModel;
 	private HashMap<Query, Double> _baseConvProbs;
 
 	// ###################
 	// #### The Agent ####
 	// ###################
 
 	public ILPAgentQ(){
 		System.out.println("Let's start");
 		_productRevenue = new HashMap<Product, Double>();
 		_bids = new HashMap<Query, Double>();
 		_dailyLimit = new HashMap<Query, Double>();
 		_queryIndexing = new HashMap<Integer, Query>();
 		_previousDayData = new HashMap<Query, HashMap<String,Double>>();
 		_bidBundle = new BidBundle();
 
 		searchingUserStates = new HashSet<UserState>();
 		searchingUserStates.add(UserState.F0);
 		searchingUserStates.add(UserState.F1);
 		searchingUserStates.add(UserState.F2);
 		searchingUserStates.add(UserState.IS);
 
 		try {
 			_fstream = new FileWriter("SalesSlotsOut.txt");
 			_fout = new BufferedWriter(_fstream);
 		}
 		catch (Exception e) {
 			System.err.println ("problem with opening file - " + e.getMessage());
 		}
 	}
 	@Override
 	public void initBidder() {
 		System.out.println("Initilizing Bids");
 		
 		DAILYCAPACITY = _advertiserInfo.getDistributionCapacity() / _advertiserInfo.getDistributionWindow();
 		DISCOUNTER = _advertiserInfo.getDistributionCapacityDiscounter();
 		NUMOFQUERIES = _querySpace.size();
 		
 		for (Product p : _retailCatalog) {
 			int givesBonus=0;
 			if (p.getManufacturer().equals(_advertiserInfo.getManufacturerSpecialty())) givesBonus = 1;
 			double profit = _retailCatalog.getSalesProfit(p)*(1+_advertiserInfo.getComponentBonus()*givesBonus);
 			System.out.println("product " + p.getComponent() + " " + p.getManufacturer() + " with profit " + profit);
 			_productRevenue.put(p, profit);
 		}
 
 		int qvalue = 0;
 		for(Query query : _querySpace){	// just some reasonable numbers to start with 
 			double bid = 0;
 //			if (query.getType().equals(QueryType.FOCUS_LEVEL_ZERO))
 //				bid = .5;
 //			else if (query.getType().equals(QueryType.FOCUS_LEVEL_ONE))
 //				bid = 0.8;
 //			else 
 //				bid = 1.2;
 			_bids.put(query, bid);
 			
 			_queryIndexing.put(qvalue, query);
 			qvalue++;
 			
 			_dailyLimit.put(query, Double.MAX_VALUE);
 		}
 
 		_possibleBids = new HashSet<Double>(setPossibleBids(0.1, 3.5, .01));
 		_possibleQuantities = new HashSet<Integer>(setPossibleQuantities(0, 10, 1));
 		
 		_baseConvProbs = new HashMap<Query, Double>();
 		for(Query q : _querySpace) {
 
 			if(q.getType() == QueryType.FOCUS_LEVEL_ZERO) {
 				_baseConvProbs.put(q, _piF0);
 			}
 			else if(q.getType() == QueryType.FOCUS_LEVEL_ONE) {
 				_baseConvProbs.put(q, _piF1);
 			}
 			else if(q.getType() == QueryType.FOCUS_LEVEL_TWO) {
 				_baseConvProbs.put(q, _piF2);
 			}
 			else {
 				throw new RuntimeException("Malformed query");
 			}
 
 			String component = q.getComponent();
 			if(component == _compSpecialty) {
 				_baseConvProbs.put(q,eta(_baseConvProbs.get(q),1+_CSB));
 			}
 		}
 	}
 	
 	@Override
 	public BidBundle getBidBundle(Set<AbstractModel> models) {		
 		if (_day >= 6) {
 			try {
 				buildMaps(models);
 				updateBids();
 			} catch (Exception e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 		else {
 			for(Query q : _querySpace){
 				double bid;
 				if (q.getType().equals(QueryType.FOCUS_LEVEL_ZERO))
 					bid = randDouble(.1,.6);
 				else if (q.getType().equals(QueryType.FOCUS_LEVEL_ONE))
 					bid = randDouble(.25,.75);
 				else 
 					bid = randDouble(.35,1.0);
 				_bids.put(q, bid);
 			}
 		}
 		
 		
 		
 		_bidBundle = new BidBundle();
 		
 		
 		for (Query query:_querySpace) {
 			_bidBundle.addQuery(query, _bids.get(query), new Ad()); //TODO add a daily limit
 		}
 		return _bidBundle;
 		
 	}
 	
 	
 	// ###############
 	// #### CPLEX ####
 	// ###############
 	/**
 	 * Creates the bids using the ILP calculation
 	 * @throws IOException 
 	 */
 	public void updateBids() throws IOException {
 		
 		double missingSales = _capacity - _unitsSold.getEstimate();
 		if (_day < 5) {
 			missingSales = missingSales - ((_capWindow - _day) * DAILYCAPACITY);
 		}
 		int qsize = _possibleQuantities.size();
 		Integer[] quantitiesSet = new Integer[qsize];
 		quantitiesSet = _possibleQuantities.toArray(quantitiesSet).clone();
 		int bsize = _possibleBids.size();
 		Double[] bidsSet = new Double[bsize];
 		bidsSet = _possibleBids.toArray(bidsSet).clone();
 		
 		try {
 			IloCplex cplex = new IloCplex();
 			IloIntVar[] overCapVar = cplex.boolVarArray(qsize);
 			IloIntVar[][] bidsVar = new IloIntVar[NUMOFQUERIES][bsize];
 			IloLinearNumExpr exprObj = cplex.linearNumExpr();
 			for (int query=0 ; query<NUMOFQUERIES ; query++) {
 				bidsVar[query] = cplex.boolVarArray(bsize);
 
 //				System.out.print(_queryIndexing.get(query).toString() + " ## ");
 				for (int bidIndex=0 ; bidIndex<bsize ; bidIndex++) {
 					exprObj.addTerm(bidsVar[query][bidIndex], getObjBidCoef(bidsSet[bidIndex] , _queryIndexing.get(query)));
 //					System.out.print(bidIndex + "->" + getObjBidCoef(bidsSet[bidIndex] , _queryIndexing.get(query)) + "\t");
 				}
 //				System.out.println();
 			}
 			if (USEOVERQUANTITY) for (int quantity=0 ; quantity < qsize ; quantity++) {
 				exprObj.addTerm(overCapVar[quantity], -getObjOverQuantityCoef(quantitiesSet[quantity]));				
 			}		
 			cplex.addMaximize(exprObj);
 
 			IloLinearNumExpr exprCapacity = cplex.linearNumExpr();
 			IloLinearNumExpr exprOverCapVarLE = cplex.linearNumExpr();
 			IloLinearNumExpr exprBidsVarLE = cplex.linearNumExpr();
 			for (int query=0 ; query<NUMOFQUERIES ; query++) {
 				for (int bidIndex=0 ; bidIndex<bsize; bidIndex++) {
 					exprCapacity.addTerm(bidsVar[query][bidIndex] , getQuantityBoundBidCoef(bidsSet[bidIndex] , _queryIndexing.get(query)));
 					exprBidsVarLE.addTerm(1.0, bidsVar[query][bidIndex]);
 				}
 				cplex.addLe(exprBidsVarLE, 1);
 				exprBidsVarLE = cplex.linearNumExpr();
 			}
 			if (USEOVERQUANTITY) {
 				for (int quantity=0 ; quantity<qsize ; quantity++) {
 					exprCapacity.addTerm(overCapVar[quantity] , -quantitiesSet[quantity]);				
 					exprOverCapVarLE.addTerm(1.0, overCapVar[quantity]);
 				}
 				cplex.addLe(exprOverCapVarLE, 1);
 			}
 
 			cplex.addLe(exprCapacity, DAILYCAPACITY + missingSales);	// the capacity sold minus the over capacity (=epxrCapacity) need to be less than the daily capacity
 			_fout.write(exprCapacity.toString());
 			_fout.write("\n We want to sell " + _wantedSales + "\n");
 			cplex.setOut(null);
 			if (cplex.solve()) {
 //				cplex.output().println("Solution status = " + cplex.getStatus());
 //				cplex.output().println("Solution value = " + cplex.getObjValue());
 				
 				_previousDayData = new HashMap<Query, HashMap<String,Double>>();
 				for (int queryIndex=0 ; queryIndex<NUMOFQUERIES ; queryIndex++) {
 					HashMap<String,Double> expectedData = new HashMap<String, Double>();
 					Query query = _queryIndexing.get(queryIndex);
 					double[] queryResults = cplex.getValues(bidsVar[queryIndex]);
 					boolean bidThisQuery = false;
 //					System.out.print(_queryIndexing.get(query).toString());
 					for (int bidIndex=0 ; bidIndex<queryResults.length ; bidIndex++) {
 						if (queryResults[bidIndex] == 1.0) {
 //							double simpleBid = (_numSlots - bidIndex + 1) * 0.71 + 0.71;	// if there is no good bid, use this
 //							double bid;	// the final bid for this slot
 //							if (_day < DAYSUNTILILKE) {
 //								bid = simpleBid;	// if we don't have enough data, use a simple bid decision
 //							} else {
 //								bid = _slotToBidModels_ilke.get(_queryIndexing.get(query)).getPrediction(bidIndex);
 //								if ((Double.isInfinite(bid) || Double.isNaN(bid)) || (bid == 0.0)) bid = simpleBid;	// if there is no good result from the models, use a simple decision
 //							}
 							bidThisQuery = true;
 							_bids.put(query , bidsSet[bidIndex]); 
 							_fout.write (query.toString() + "\t" + "Bid=" + bidsSet[bidIndex] + " ObjCoef=" + getObjBidCoef(bidsSet[bidIndex], query) + " QCoef=" + getQuantityBoundBidCoef(bidsSet[bidIndex], query) + "\n");
 							_fout.flush();
 							expectedData.put("Bid", bidsSet[bidIndex]);
 							expectedData.put("Sales", getQuantityBoundBidCoef(bidsSet[bidIndex], query));
 //							_fout.write ("\nPosition used the class " + temp.getClass());
 						}
 					}
 					if (!bidThisQuery) _bids.put(_queryIndexing.get(queryIndex), 0.0);
 					else {
 						_previousDayData.put(query, expectedData);
 					}
 				}
 				if (USEOVERQUANTITY) {
 					double[] quantityResults = cplex.getValues(overCapVar);
 					for (int quantity=0 ; quantity<quantityResults.length ; quantity++) {
 //						if (quantityResults[quantity] == 1.0) System.out.println("OverQuantity=" + quantitiesSet[quantity] + " ObjCoef=" + getObjOverQuantityCoef(quantity));
 					}
 				}
 			}
 			//System.out.println(cplex.toString());
 			cplex.end();
 		}
 		catch (IloException e) {
 			System.err.println ("Concert Exception" + e + "' caught");
 		}
 //		catch (Exception e) {
 //			System.err.println("problem with writing to file - " + e.getMessage());
 //		}
 		for (Query q : _querySpace) {
 //			_bids.put(q, 3.0);
 		}
 	}
 
 	// reviewing all of the objective and constrains, to work with query and not user and product
 	public double getObjBidCoef(double bid , Query query) {		
 
 		double start = System.currentTimeMillis();
 		double result = 0;
 		double revenue = getRevenue(query);
 
		double conv = estimateConv(query, 0);
 		double cpc = estimateCPC(query, bid);
 		double clickPr = _bidToClickPrModel.getPrediction(query, bid, new Ad());
 		double numImps = _queryToNumImpModel.getPrediction(query);
 		int numClicks = (int) (clickPr * numImps);
 		result = numClicks * (conv*revenue - cpc); 
 		if (Double.isNaN(result) || Double.isInfinite(result)) {	// if there is a problem, beep and show me where
 			beep();
 			//System.out.print("\n The is a null result: \n bid2cpc=" + estimateCPC(query, bid) + "\n clickPr=" + estimateClicks(p,us,query,slot) + "\n get_convPr=" + estimateConv(p,us,0) + "\n p_x_Pr" + estimateIn + "\n");
 		}
 		
 		try {
 //			_fout.write("\nWith Query " + query.toString() + " And bid " + bid + " We found " + (imp * click) + " but we perfect model found " + _bidToNumClicks.get(query).getPrediction(bid));
 			_fout.write("\nfor query " + query.toString() + " and bid " + cutDouble(bid) + " we found a profit of " + cutDouble(result) + " (numclicks=" + cutDouble(numClicks) + " conversions=" + cutDouble(conv) + " cpc=" + cutDouble(cpc) + ")");
 		}
 		catch (Exception e) {
 			System.err.println ("getObjBidCoef() error with writing to file");
 		}
 
 		double stop = System.currentTimeMillis();
 //		System.out.println("Day " + _day + " getObjBidCoef() took " + ((stop-start) / 1000) + " seconds");
 		return result;
 	}
 
 	public double getQuantityBoundBidCoef(double bid, Query query) {
 		
 		double start = System.currentTimeMillis();
 		double result = 0;
 		
 		double clickPr = _bidToClickPrModel.getPrediction(query, bid, new Ad());
 		double numImps = _queryToNumImpModel.getPrediction(query);
 		int numClicks = (int) (clickPr * numImps);
		double conv = estimateConv(query, 0);
 
 		result = numClicks * conv;
 		
 		try {
 			_fout.write("\nfor query " + query.toString() + " and bid " + cutDouble(bid) + " we found " + cutDouble(result) + " conversions (numClicks=" + cutDouble(numClicks) + " conv=" + cutDouble(conv));
 		}
 		catch (Exception e) {
 		}
 		double stop = System.currentTimeMillis();
 //		System.out.println("Day " + _day + " getQuantityBoundCoef() took " + ((stop-start) / 1000) + " seconds");
 		return result;
 	}
 
 	public double getObjOverQuantityCoef(int quantity) {
 		
 		double start = System.currentTimeMillis();
 		double result=0;
 				
		for (int i=1 ; i <= quantity ; i++) {
			for (Query query : _querySpace) {
				result += estimateImpressionOutOfTotalSearchingUsers(query) * (estimateConv(query, 0) - estimateConv(query, i)) * getRevenue(query);
			}
		}
		result = result / _querySpace.size();
		result = result / _numSearchingUsers;
		
 //		try {
 //			_fout.write("\nfor over quantity of " + quantity + " we found a lost of profit of " + cutDouble(result));
 //		}
 //		catch (Exception e) {
 //		}
 //
 //		double stop = System.currentTimeMillis();
 //		System.out.println("Day " + _day + " getObjOverQuantityCoef() took " + ((stop-start) / 1000) + " seconds");
 		return result;
 	}
 
 	// reviewing all of the estimation, to work with query and not user and product
 	public double estimateImpressionOutOfTotalSearchingUsers (Query query) {
 		double result = 0;
 
 //		Set<UserState> userStates = new HashSet<UserState>();
 //		userStates.add (UserState.IS);
 //		if (query.getType().equals(QueryType.FOCUS_LEVEL_ZERO)) userStates.add (UserState.F0);
 //		if (query.getType().equals(QueryType.FOCUS_LEVEL_ONE)) userStates.add (UserState.F1);
 //		if (query.getType().equals(QueryType.FOCUS_LEVEL_TWO)) userStates.add (UserState.F2);		
 //		Set<Product> products = new HashSet<Product>();
 		
 		for (UserState userState : setOfUserStates(query)) {
 			for (Product product : setOfProducts(query, userState)) {
 				result += this._userModel.getPrediction(product, userState);
 			}
 		}
 
 		return result;
 	}
 
 	public double estimateClicks (Query query, double bid) {
 		//result = _userModel.getPrediction(p, us) / _numSearchingUsers;	// and estimate of the percent of this user in the query		
 		double clickPr = _bidToClickPrModel.getPrediction(query, bid, new Ad());
 
 		return clickPr;
 	}
 
 	private double estimateConv(Query query, double overCap) {
 		double baseline = 0;
 		String getBonus = "MAYBE";
 		QueryType queryType = query.getType();
 		String component = query.getComponent();
 		if(queryType.equals(QueryType.FOCUS_LEVEL_ZERO)) {
 			baseline = _piF0;
 		}
 		else {
 			if(queryType.equals(QueryType.FOCUS_LEVEL_ONE)) {
 				baseline = _piF1;
 				if (_compSpecialty.equals(component)) {
 					getBonus = "YES";
 				} else if (component == null) getBonus = "NO";
 			} else if(queryType.equals(QueryType.FOCUS_LEVEL_TWO)) {
 				baseline = _piF2;
 				if (_compSpecialty.equals(component)) getBonus = "YES";
 			} else {
 				throw new RuntimeException("Malformed query");
 			}	
 		}
 		
 		double result = 0;
 		double capdiscount = Math.pow(_lambda,Math.max(overCap, 0));
 		double firstTerm = baseline * capdiscount;
 		double secondTerm = 1 + _CSB;
 		double nuo = (firstTerm * secondTerm) / (firstTerm * secondTerm + (1 - firstTerm));
 		
 		if (getBonus == "YES") result = nuo;
 		else if (getBonus == "NO") result = firstTerm;
 		else result = (nuo + 3*firstTerm)/4;
 		
 		double nISusers = 0;
 		double nFusers = 0;
 		for (Product product : _retailCatalog) {
 			for (UserState userState : setOfUserStates(query)) {
 				if (userState.equals(UserState.IS)) nISusers += _userModel.getPrediction(product, UserState.IS);
 				else nFusers += _userModel.getPrediction(product, userState);
 			}
 		}
 		result = result * (nFusers/(nFusers+nISusers));
 		
 		return result;
 	}
 
 	public double estimateCPC (Query query, double bid) {
 		double result = 0;
 //		double start = System.currentTimeMillis();
 
 		result = _bidToCPCModel.getPrediction(query, bid);
 		if (Double.isNaN(result) || Double.isInfinite(result) || Double.isInfinite(-result) || Double.valueOf(result).equals(0.0)) result = _regReserveScore+0.01; 
 		
 //		double stop = System.currentTimeMillis();
 //		System.out.println("Day " + _day + " estimateImp() took " + ((stop-start) / 1000) + " seconds");
 		return result;
 	}
 
 	private double getRevenue(Query query) {
 		if (query.getType().equals(QueryType.FOCUS_LEVEL_ZERO)) return (35.0/3.0);
 		if (query.getType().equals(QueryType.FOCUS_LEVEL_ONE)) {
 			if (query.getManufacturer() == null) return (35.0/3.0);
 			if (_manSpecialty.equals(query.getManufacturer())) return 15.0;
 			return 10.0;
 		}
 		if (query.getType().equals(QueryType.FOCUS_LEVEL_TWO)) {
 			if (_manSpecialty.equals(query.getManufacturer())) return 15.0;
 			return 10.0;
 		}
 		return 0.0;
 	}
 	
 	@Override
 	public Set<AbstractModel> initModels() {
 		Set<AbstractModel> models = new LinkedHashSet<AbstractModel>();
 		AbstractUserModel userModel = new BasicUserModel();
 		AbstractQueryToNumImp queryToNumImp = new BasicQueryToNumImp(userModel);
 		AbstractBidToCPC bidToCPC = new RegressionBidToCPC(_querySpace);
 		AbstractBidToPrClick bidToPrClick = new RegressionBidToPrClick(_querySpace, 4, 20, true, false, false);
 		AbstractUnitsSoldModel unitsSold = new UnitsSoldMovingAvg(_querySpace,_capacity,_capWindow);
 		models.add(userModel);
 		models.add(queryToNumImp);
 		models.add(bidToCPC);
 		models.add(bidToPrClick);
 		models.add(unitsSold);
 		return models;
 	}
 	
 	@Override
 	public void updateModels(SalesReport salesReport,
 			QueryReport queryReport) {
 		for(AbstractModel model:_models) {
 			if(model instanceof AbstractUserModel) {
 				AbstractUserModel userModel = (AbstractUserModel) model;
 				userModel.updateModel(queryReport, salesReport);
 			}
 			else if(model instanceof AbstractQueryToNumImp) {
 				AbstractQueryToNumImp queryToNumImp = (AbstractQueryToNumImp) model;
 				queryToNumImp.updateModel(queryReport, salesReport);
 			}
 			else if(model instanceof AbstractUnitsSoldModel) {
 				AbstractUnitsSoldModel unitsSold = (AbstractUnitsSoldModel) model;
 				unitsSold.update(salesReport);
 			}
 			else if(model instanceof AbstractBidToCPC) {
 				AbstractBidToCPC bidToCPC = (AbstractBidToCPC) model;
 				bidToCPC.updateModel(queryReport, _bidBundles.get(_bidBundles.size()-2));
 			}
 			else if(model instanceof AbstractBidToPrClick) {
 				AbstractBidToPrClick bidToPrClick = (AbstractBidToPrClick) model;
 				bidToPrClick.updateModel(queryReport, _bidBundles.get(_bidBundles.size()-2));
 			}
 			else {
 				throw new RuntimeException("Unhandled Model (you probably would have gotten a null pointer later)");
 			}
 		}
 	}
 
 	protected void buildMaps(Set<AbstractModel> models) throws Exception {
 		for(AbstractModel model : models) {
 			if(model instanceof AbstractUserModel) {
 				AbstractUserModel userModel = (AbstractUserModel) model;
 				_userModel = userModel;
 				_numSearchingUsers = 0;
 				for (Product p : _retailCatalog) {
 					for (UserState us : searchingUserStates) {
 						_numSearchingUsers += _userModel.getPrediction(p, us); // I want to always count how many users are in a searching state. it help the impressions estimator later.
 					}
 				}
 			}
 			else if(model instanceof AbstractQueryToNumImp) {
 				AbstractQueryToNumImp queryToNumImp = (AbstractQueryToNumImp) model;
 				_queryToNumImpModel = queryToNumImp;
 			}
 			else if(model instanceof AbstractUnitsSoldModel) {
 				AbstractUnitsSoldModel unitsSold = (AbstractUnitsSoldModel) model;
 				_unitsSold = unitsSold;
 			}
 			else if(model instanceof AbstractBidToCPC) {
 				AbstractBidToCPC bidToCPC = (AbstractBidToCPC) model;
 				_bidToCPCModel = bidToCPC; 
 			}
 			else if(model instanceof AbstractBidToPrClick) {
 				AbstractBidToPrClick bidToPrClick = (AbstractBidToPrClick) model;
 				_bidToClickPrModel = bidToPrClick;
 			}
 			else {
 				//				throw new RuntimeException("Unhandled Model (you probably would have gotten a null pointer later)"+model);
 			}
 		}
 	}
 	// ##########################
 	// #### Helper Functions ####
 	// ##########################
 	public Set<Double> setPossibleBids (double minBid, double maxBid, double interval) {
 		Set<Double> bids = new HashSet<Double>();
 		if ((minBid < 0) || (maxBid <= minBid)) return bids;
 		double bid = minBid;
 		while (bid < maxBid) {
 			bids.add(bid);
 			bid += interval;
 		}
 		return bids;
 	}
 
 	public Set<Integer> setPossibleQuantities(int minQ, int maxQ, int interval) {
 		Set<Integer> quantity = new HashSet<Integer>();
 		if ((minQ < 0) || (maxQ <= minQ)) return quantity;
 		int q = minQ;
 		while (q <= maxQ) {
 			quantity.add(q);
 			q += interval;
 		}
 		return quantity;
 	}
 
 	protected Set<UserState> setOfUserStates(Query query) {
 		Set<UserState> us = new HashSet<UserState>();
 		us.add(UserState.IS);
 		if (query.getType().equals(QueryType.FOCUS_LEVEL_ZERO)) us.add(UserState.F0); 
 		if (query.getType().equals(QueryType.FOCUS_LEVEL_ONE)) us.add(UserState.F1);
 		if (query.getType().equals(QueryType.FOCUS_LEVEL_TWO)) us.add(UserState.F2);
 		return us;
 	}
 
 	protected Set<Product> setOfProducts(Query query, UserState us) {
 		Set<Product> products = new HashSet<Product>();
 		if (query.getType().equals(QueryType.FOCUS_LEVEL_ZERO) || us.equals(UserState.IS)) {
 			for (Product p : _retailCatalog) {
 				products.add(p);
 			}
 		} else if (query.getType().equals(QueryType.FOCUS_LEVEL_TWO)) {
 			products.add(new Product(query.getManufacturer(),query.getComponent()));
 		} else if (query.getType().equals(QueryType.FOCUS_LEVEL_ONE)) {
 			if (query.getManufacturer() == null) {
 				for (Product p : _retailCatalog) {
 					if (p.getComponent().equals(query.getComponent())) products.add(p);
 				}
 			} else {
 				for (Product p : _retailCatalog) {
 					if (p.getManufacturer().equals(query.getManufacturer())) products.add(p);
 				}
 			}				
 		}
 		return products;
 	}
 			
 	protected void beep() {
 		Toolkit.getDefaultToolkit().beep();
 	}
 	
 	protected double cutDouble (double d) {
 		if (Double.isNaN(d)) return d;
 		int result = (int)(d*100);
 		return (((double)result)/100);
 	}
 	
 	private double randDouble(double a, double b) {
 		double rand = _R.nextDouble();
 		return rand * (b - a) + a;
 	}
 	
 }
