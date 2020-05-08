 package agents.modelbased;
 
 import agents.AbstractAgent;
 import agents.modelbased.mckputil.IncItem;
 import agents.modelbased.mckputil.Item;
 import agents.modelbased.mckputil.ItemComparatorByWeight;
 import clojure.lang.PersistentHashMap;
 import edu.umich.eecs.tac.props.*;
 import models.AbstractModel;
 import models.ISratio.ISRatioModel;
 import models.adtype.AbstractAdTypeEstimator;
 import models.adtype.AdTypeEstimator;
 import models.advertiserspecialties.AbstractSpecialtyModel;
 import models.advertiserspecialties.SimpleSpecialtyModel;
 import models.bidmodel.AbstractBidModel;
 import models.bidmodel.IndependentBidModel;
 import models.budgetEstimator.AbstractBudgetEstimator;
 import models.budgetEstimator.BudgetEstimator;
 import models.paramest.AbstractParameterEstimation;
 import models.paramest.BayesianParameterEstimation;
 import models.queryanalyzer.AbstractQueryAnalyzer;
 import models.queryanalyzer.CarletonQueryAnalyzer;
 import models.unitssold.AbstractUnitsSoldModel;
 import models.unitssold.BasicUnitsSoldModel;
 import models.usermodel.ParticleFilterAbstractUserModel;
 import models.usermodel.jbergParticleFilter;
 import tacaa.javasim;
 
 import java.util.*;
 
 import static models.paramest.ConstantsAndFunctions._advertiserEffectBoundsAvg;
 import static models.paramest.ConstantsAndFunctions.getISRatio;
 import static models.paramest.ConstantsAndFunctions.queryTypeToInt;
 import static simulator.parser.GameStatusHandler.UserState;
 
 public class MCKP extends AbstractAgent {
 
    /*
      * TODO:
      *
      * 1) Predict opponent MSB and CSB
      * 2) Predict opponent ad type
      * 3) Dynamic or at least different capacity numbers
      */
 
 
    //Parameters that can be tweaked:
    //  Bid/budget lists
    //  _c (predicted opponent conversion probs for each focus level)
 
 
    double[] _c;
 
    private boolean DEBUG = false;
    private Random _R;
    private boolean SAFETYBUDGET = false;
    private boolean BUDGET = false;
    private boolean FORWARDUPDATING = true;
    private boolean PRICELINES = false;
    private boolean UPDATE_WITH_ITEM = false;
 
    private static double USER_MODEL_UB_MULT;
    private static double USER_MODEL_UB_STD_DEV;
    private boolean USER_MODEL_UB = true;
    private boolean RANKABLE = false;
 
    private double _safetyBudget;
    private int lagDays = 2;
 
    private double[] _regReserveLow = {.08, .29, .46};
 
    private HashMap<Query, Double> _baseConvProbs;
    private HashMap<Query, Double> _baseClickProbs;
    private HashMap<Query, Double> _salesPrices;
 
    private AbstractQueryAnalyzer _queryAnalyzer;
    private ParticleFilterAbstractUserModel _userModel;
    private AbstractUnitsSoldModel _unitsSold;
    private AbstractBidModel _bidModel;
    private AbstractParameterEstimation _paramEstimation;
    private AbstractBudgetEstimator _budgetEstimator;
    private ISRatioModel _ISRatioModel;
    private PersistentHashMap _baseCljSim;
    private PersistentHashMap _perfectCljSim = null;
    private String _agentToReplace;
    private AbstractAdTypeEstimator _adTypeEstimator;
    private AbstractSpecialtyModel _specialtyModel;
 
    private HashMap<Integer,Double> _totalBudgets,_capMod;
    private double _lowBidMult;
    private double _highBidMult;
    private double _randJump,_yestBid,_5DayBid,_bidStdDev;
 
    private MultiDay _multiDayHeuristic = MultiDay.HillClimbing;
    private int _multiDayDiscretization = 10;
 
    public enum MultiDay {
       OneDayHeuristic, HillClimbing, DP, DPHill
    }
 
    public MCKP() {
       this(0.04,0.14,0.20);
    }
 
    public MCKP(double c1, double c2, double c3) {
       this(c1,c2,c3,750,1000,1250,.2,.8);
    }
 
    public MCKP(double c1, double c2, double c3, double budgetL, double budgetM, double budgetH, double lowBidMult, double highBidMult) {
       _R = new Random();
 
       _capMod = new HashMap<Integer, Double>();
       _capMod.put(300,1.0);
       _capMod.put(450,1.0);
       _capMod.put(600,1.0);
 //      _capMod.put(300,c1);
 //      _capMod.put(450,c2);
 //      _capMod.put(600,c3);
 
       _c = new double[3];
 //      _c[0] = c1;
 //      _c[1] = c2;
 //      _c[2] = c2;
       _c[0] = .03;
       _c[1] = .05;
       _c[2] = .15;
 
 //      _c[0] = .11;
 //      _c[1] = .23;
 //      _c[2] = .36;
 
 
       USER_MODEL_UB_MULT = 1.45;
       USER_MODEL_UB_STD_DEV = .75;
       _totalBudgets = new HashMap<Integer, Double>();
 //      _totalBudgets.put(300,Double.MAX_VALUE);
 //      _totalBudgets.put(450,Double.MAX_VALUE);
 //      _totalBudgets.put(600,Double.MAX_VALUE);
       _totalBudgets.put(300,750.0);
       _totalBudgets.put(450,1000.0);
       _totalBudgets.put(600,1250.0);
       _lowBidMult = 0.2;
       _highBidMult = 0.8;
       _randJump = .1;
       _yestBid = .5;
       _5DayBid = .4;
       _bidStdDev = 2.0;
    }
 
    public MCKP(double c1, double c2, double c3, double budgetL, double budgetM, double budgetH, double lowBidMult, double highBidMult, MultiDay multiDayHeuristic, int multiDayDiscretization) {
       this(c1, c2, c3, budgetL, budgetM, budgetH, lowBidMult, highBidMult);
       _multiDayHeuristic = multiDayHeuristic;
       _multiDayDiscretization = multiDayDiscretization;
    }
 
    public MCKP(PersistentHashMap perfectSim, String agentToReplace, double c1, double c2, double c3, MultiDay multiDay, int multiDayDiscretization) {
       this(c1, c2, c3);
       _perfectCljSim = perfectSim;
       _agentToReplace = agentToReplace;
       _multiDayHeuristic = multiDay;
       _multiDayDiscretization = multiDayDiscretization;
    }
 
    public boolean hasPerfectModels() {
       return (_perfectCljSim != null);
    }
 
    public PersistentHashMap initClojureSim() {
       return javasim.initClojureSim(_publisherInfo,_slotInfo,_advertiserInfo,_retailCatalog,_advertisers);
    }
 
    public PersistentHashMap setupSimForDay() {
       if(!hasPerfectModels()) {
          HashMap<String,HashMap<Query,Double>> squashedBids = new HashMap<String, HashMap<Query, Double>>();
          HashMap<String,HashMap<Query,Double>> budgets = new HashMap<String, HashMap<Query, Double>>();
          HashMap<Product,double[]> userPop = new HashMap<Product, double[]>();
          HashMap<String,HashMap<Query,Double>> advAffects = new HashMap<String, HashMap<Query, Double>>();
          HashMap<Query,Double> contProbs = new HashMap<Query, Double>();
          HashMap<Query,Double> regReserves = new HashMap<Query, Double>();
          HashMap<Query,Double> promReserves = new HashMap<Query, Double>();
          HashMap<String,Integer> capacities = new HashMap<String, Integer>();
          HashMap<String,Integer> startSales = new HashMap<String, Integer>();
          HashMap<String,String> manSpecialties = new HashMap<String, String>();
          HashMap<String,String> compSpecialties = new HashMap<String, String>();
          HashMap<String,HashMap<Query,Ad>> ads = new HashMap<String, HashMap<Query, Ad>>();
 
          //Get predictions for continuation probabilities, reserve scores, and promoted reserve scores.
          for(Query q : _querySpace) {
             contProbs.put(q,_paramEstimation.getContProbPrediction(q));
             regReserves.put(q,_paramEstimation.getRegReservePrediction(q.getType()));
             promReserves.put(q, _paramEstimation.getPromReservePrediction(q.getType()));
          }
 
          //------------
          //Get predictions for each advertiser's bid, budget, advertiserEffect,
          //initialConversions, maxCapacity, manufacturer and component specialties,
          //whether they are targeting
          //------------
          for(int i = 0; i < _advertisers.size(); i++) {
             String agent = _advertisers.get(i);
             if(i != _advIdx) { //For everyone besides us...
                HashMap<Query,Double> aSquashedBids = new HashMap<Query, Double>();
                HashMap<Query,Double> aBudgets = new HashMap<Query, Double>();
                HashMap<Query,Double> aAdvAffects = new HashMap<Query, Double>();
                int aCapacities = 450;  //FIXME estimate opponent capacity
 //               int aStartSales = (int)((4.0*(aCapacities / ((double) _capWindow)) + aCapacities) / 2.0);  //FIXME Estimate opponent start-sales
                int aStartSales = aCapacities; 
                Query maxQuery = null;
                double maxBid = 0.0;
                for(Query q : _querySpace) {
                   double bid = _bidModel.getPrediction("adv" + (i+1), q);
                   double advEffect = _advertiserEffectBoundsAvg[queryTypeToInt(q.getType())]; //FIXME: Get this from an advertiser effect model?
                   double squashedBid = bid*Math.pow(advEffect,_squashing);
                   aSquashedBids.put(q, squashedBid);
                   aBudgets.put(q, _budgetEstimator.getBudgetEstimate(q, "adv" + (i+1)));
                   aAdvAffects.put(q,advEffect);
                   if(q.getType().equals(QueryType.FOCUS_LEVEL_TWO)) {
                      if(squashedBid >= maxBid) {
                         maxBid = squashedBid;
                         maxQuery = q;
                      }
                   }
                }
 
                
                
                
                //---------------
                //Determine manufacturer/component specialties
                //---------------
 
                //Assume specialty is the prod of F2 query they are bidding most in
                String aManSpecialties = maxQuery.getManufacturer();
                String aCompSpecialties = maxQuery.getComponent();
                
                //String aManSpecialties  = _specialtyModel.getManufacturerSpecialty("adv" + (i+1)); //TODO: Use this!!!
                //String aCompSpecialties = _specialtyModel.getComponentSpecialty("adv" + (i+1)); //TODO: Use this!!!
 
 
                
                //---------------
                //Determine ad targeting
                //---------------
                
                //We can either assume the advertiser will target its specialty,
                //or we can look at its historical targeting.
                HashMap<Query,Ad> aAds = new HashMap<Query, Ad>();
                for(Query q : _querySpace) {
             	   //Ad predictedAd = _adTypeEstimator.getAdTypeEstimate(q, "adv" + (i+1)); //TODO: Use this!!!
             	   Ad predictedAd = getTargetedAd(q,aManSpecialties,aCompSpecialties);
             	   aAds.put(q, predictedAd);                  
                }
 
                               
                
                squashedBids.put(agent,aSquashedBids);
                budgets.put(agent,aBudgets);
                advAffects.put(agent,aAdvAffects);
                capacities.put(agent,aCapacities);
                startSales.put(agent,aStartSales);
                manSpecialties.put(agent,aManSpecialties);
                compSpecialties.put(agent,aCompSpecialties);
                ads.put(agent,aAds);
             }
             else {
                HashMap<Query,Double> aAdvAffects = new HashMap<Query, Double>();
                int aCapacities = _capacity;
 //               double remainingCap;
 //               if(_day < 4) {
 //                  remainingCap = _capacity/((double)_capWindow);
 //               }
 //               else {
 //                  remainingCap = _capacity - _unitsSold.getWindowSold();
 //               }
 //               int aStartSales = _capacity - (int)remainingCap;
 //               int aStartSales = (int)(_capacity - _capacity / ((double) _capWindow));
                int aStartSales = 0;
 
                String aManSpecialties = _manSpecialty;
                String aCompSpecialties = _compSpecialty;
 
                for(Query q : _querySpace) {
                   aAdvAffects.put(q,_paramEstimation.getAdvEffectPrediction(q));
                }
 
                advAffects.put(agent,aAdvAffects);
                capacities.put(agent,aCapacities);
                startSales.put(agent,aStartSales);
                manSpecialties.put(agent,aManSpecialties);
                compSpecialties.put(agent,aCompSpecialties);
             }
          }
 
          for(Product p : _products) {
             double[] userState = new double[UserState.values().length];
             userState[0] = _userModel.getPrediction(p, UserState.NS);
             userState[1] = _userModel.getPrediction(p, UserState.IS);
             userState[2] = _userModel.getPrediction(p, UserState.F0);
             userState[3] = _userModel.getPrediction(p, UserState.F1);
             userState[4] = _userModel.getPrediction(p, UserState.F2);
             userState[5] = _userModel.getPrediction(p, UserState.T);
             userPop.put(p, userState);
          }
 
          return javasim.mkFullStatus(_baseCljSim, squashedBids, budgets, userPop, advAffects, contProbs, regReserves,
                                      promReserves, capacities, startSales, manSpecialties, compSpecialties, ads);
       }
       else {
          return javasim.mkPerfectFullStatus(_perfectCljSim, (int)_day, _agentToReplace, 0);
       }
    }
 
    public double[] simulateQuery(PersistentHashMap cljSim, Query query, double bid, double budget, Ad ad) {
       ArrayList result;
       if(hasPerfectModels()) {
          result = javasim.simQuery(cljSim, query, _agentToReplace, (int) _day, bid, budget, ad, 1, true);
       }
       else {
          result = javasim.simQuery(cljSim, query, _advId, (int) _day, bid, budget, ad, 1, false);
       }
       double[] resultArr = new double[result.size()];
       for(int i = 0; i < result.size(); i++) {
          resultArr[i] = (Double)result.get(i);
       }
       return resultArr;
    }
 
    public double[] simulateDay(PersistentHashMap cljSim, BidBundle bundle) {
       ArrayList result;
       if(hasPerfectModels()) {
          result = javasim.simDay(cljSim, _agentToReplace, (int) _day, bundle, 1, true);
       }
       else {
          result = javasim.simDay(cljSim, _advId, (int) _day, bundle, 1, false);
       }
       double[] resultArr = new double[result.size()];
       for(int i = 0; i < result.size(); i++) {
          resultArr[i] = (Double)result.get(i);
       }
       return resultArr;
    }
 
    @Override
    public void initBidder() {
       _baseConvProbs = new HashMap<Query, Double>();
       _baseClickProbs = new HashMap<Query, Double>();
       _salesPrices = new HashMap<Query,Double>();
 
       for(Query q : _querySpace) {
 
          //Define expected revenue for receiving a conversion for this query
          String manufacturer = q.getManufacturer();
          if(_manSpecialty.equals(manufacturer)) {
             //If the query is F1 or F2 for our manufacturer specialty, we get a sales bonus for every conversion from this query
             _salesPrices.put(q, 10*(_MSB+1));
          }
          else if(manufacturer == null) {
             //If the query is F0 or F1, we should consider that searchers will have our manufacturer specialty about 1/3 of the time, which will give us a sales bonus when they convert.
             _salesPrices.put(q, (10*(_MSB+1)) * (1/3.0) + (10)*(2/3.0));
          }
          else {
             //If the query is F2 and not our manufacturer specialty, we'll never get the sales bonus.
             _salesPrices.put(q, 10.0);
          }
 
 
          //Get baseline conversion probabilities.
          //We don't consider component specialty bonus here. They are handled in a method that also considers 
          //our amount over capacity and the fraction of IS users.
          if(q.getType().equals(QueryType.FOCUS_LEVEL_ZERO)) {
             _baseConvProbs.put(q, _piF0);
          }
          else if(q.getType().equals(QueryType.FOCUS_LEVEL_ONE)) {
             _baseConvProbs.put(q, _piF1);
          }
          else if(q.getType().equals(QueryType.FOCUS_LEVEL_TWO)) {
             _baseConvProbs.put(q, _piF2);
          }
          else {
             throw new RuntimeException("Malformed query");
          }
 
          /*
              * These are the MAX e_q^a (they are randomly generated), which is our clickPr for being in slot 1!
              *
              * Taken from the spec
              */
 
          /*
              * TODO
              *
              * we can consider replacing these with our predicted clickPrs
              *
              */
          if(q.getType().equals(QueryType.FOCUS_LEVEL_ZERO)) {
             _baseClickProbs.put(q, .3);
          }
          else if(q.getType().equals(QueryType.FOCUS_LEVEL_ONE)) {
             _baseClickProbs.put(q, .4);
          }
          else if(q.getType().equals(QueryType.FOCUS_LEVEL_TWO)) {
             _baseClickProbs.put(q, .5);
          }
          else {
             throw new RuntimeException("Malformed query");
          }
       }
 
       /*
        * Initialize Simulator
        */
       _baseCljSim = initClojureSim();
    }
 
    @Override
    public Set<AbstractModel> initModels() {
       Set<AbstractModel> models = new LinkedHashSet<AbstractModel>();
       _queryAnalyzer = new CarletonQueryAnalyzer(_querySpace,_advertisers,_advId,true,true);
       _userModel = new jbergParticleFilter(_c,USER_MODEL_UB_STD_DEV);
       _unitsSold = new BasicUnitsSoldModel(_querySpace,_capacity,_capWindow);
       _bidModel = new IndependentBidModel(_advertisersSet, _advId,1,_randJump,_yestBid,_5DayBid,_bidStdDev,_querySpace);
 //      _bidModel = new JointDistBidModel(_advertisersSet, _advId, 8, .7, 1000);
       _paramEstimation = new BayesianParameterEstimation(_c,_advIdx,_numSlots, _numPS, _squashing, _querySpace);
       _budgetEstimator = new BudgetEstimator(_querySpace,_advIdx,_numSlots,_numPS);
       _ISRatioModel = new ISRatioModel(_querySpace,_numSlots);      
       _adTypeEstimator = new AdTypeEstimator(_querySpace, _advertisersSet, _products);
       _specialtyModel = new SimpleSpecialtyModel(_querySpace, _advertisersSet, _products, _numSlots);
       
       models.add(_queryAnalyzer);
       models.add(_userModel);
       models.add(_unitsSold);
       models.add(_bidModel);
       models.add(_paramEstimation);
       models.add(_budgetEstimator);
       models.add(_ISRatioModel);
       models.add(_adTypeEstimator);
       models.add(_specialtyModel);
       return models;
    }
 
    @Override
    public void setModels(Set<AbstractModel> models) {
       super.setModels(models);
       for(AbstractModel model : _models) {
          if(model instanceof AbstractQueryAnalyzer) {
             _queryAnalyzer = (AbstractQueryAnalyzer)model;
          }
          else if(model instanceof ParticleFilterAbstractUserModel) {
             _userModel = (ParticleFilterAbstractUserModel)model;
          }
          else if(model instanceof AbstractUnitsSoldModel) {
             _unitsSold = (AbstractUnitsSoldModel)model;
          }
          else if(model instanceof AbstractBidModel) {
             _bidModel = (AbstractBidModel)model;
          }
          else if(model instanceof AbstractParameterEstimation) {
             _paramEstimation = (AbstractParameterEstimation)model;
          }
          else if(model instanceof AbstractBudgetEstimator) {
             _budgetEstimator = (AbstractBudgetEstimator)model;
          }
          else if(model instanceof ISRatioModel) {
             _ISRatioModel = (ISRatioModel)model;
          } else if (model instanceof AbstractAdTypeEstimator) {
         	 _adTypeEstimator = (AbstractAdTypeEstimator) model;
          } else if (model instanceof AbstractSpecialtyModel) {
         	 _specialtyModel = (AbstractSpecialtyModel) model;
          }
          else {
             throw new RuntimeException("Unknown Type of Model");
          }
       }
    }
 
    @Override
    public BidBundle getBidBundle() {
       BidBundle bidBundle = new BidBundle();
 
       //The only campaign-level budget we consider is a constant value. 
       //FIXME: _safetyBudget needs to be set somewhere.
       System.out.println("Bidding on day " + _day);
       if(SAFETYBUDGET) {
          bidBundle.setCampaignDailySpendLimit(_safetyBudget);
       }
       else {
          bidBundle.setCampaignDailySpendLimit(Integer.MAX_VALUE);
       }
 
       //If we are past the initial couple days (where we perform a different strategy, since we have no query/sales reports)
       //(Or if we are using perfect models)
       if(_day >= lagDays || hasPerfectModels()){
 
 
          //Get remaining capacity
          double remainingCap;
          if(!hasPerfectModels()) {
             remainingCap = _capacity*_capMod.get(_capacity) - _unitsSold.getWindowSold();
          }
          else {
             remainingCap = _capacity;
 
             int saleslen = _perfectStartSales.length;
             for(int i = saleslen-1; i >= 0 && i > saleslen-_capWindow; i--) {
                remainingCap -= _perfectStartSales[i];
             }
 
             if(saleslen < (_capWindow-1)) {
                remainingCap -= _capacity/((double)_capWindow) * (_capacity - 1 - saleslen);
             }
          }
 
 
 
 
 
          //--------------
          // Create lists of possible bids/budgets we can place for each query
          //-------------
 
          //FIXME: What about basing possible bids on the bids of opponents? Only consider N
          //bids that would put us in each starting position.
          HashMap<Query,ArrayList<Double>> bidLists = getBidLists();
         //HashMap<Query,ArrayList<Double>> bidLists = getMinimalBidLists();         
          HashMap<Query,ArrayList<Double>> budgetLists = getBudgetLists();
          
          for (Query q : _querySpace) {
         	 System.out.println(q + ": " + bidLists.get(q));
          }
 
 
          
          
          
          
          //------------
          // Create simulator (to be used for knapsack creation)
          //------------
          PersistentHashMap querySim = setupSimForDay();
          
 
 
 
 
          //--------------
          // Create incremental items for the MCKP.
          // Create these items by simulating each query (given some initial capacity, everyone's bids, and everyone's budgets)
          // (bid,budget)->clickPr, CPC, convProb, numImps,slotDistr,isRatioArr,ISRatio
          //-------------
 
          ArrayList<IncItem> allIncItems = new ArrayList<IncItem>();
 
          //want the queries to be in a guaranteed order - put them in an array
          //index will be used as the id of the query
          double penalty = getPenalty(remainingCap, 0); //Get initial penalty (before any additional conversions today)
          HashMap<Query,ArrayList<Predictions>> allPredictionsMap = new HashMap<Query, ArrayList<Predictions>>();
 
                   
          System.out.println("Creating Knapsacks");
          long knapsackStart = System.currentTimeMillis();
          for(Query q : _querySpace) {
             if(!q.equals(new Query())) { //Do not consider the (null, null) query. //FIXME: Don't hardcode the skipping of (null, null) query. 
                ArrayList<Item> itemList = new ArrayList<Item>(bidLists.get(q).size()*budgetLists.get(q).size());
                ArrayList<Predictions> queryPredictions = new ArrayList<Predictions>(bidLists.get(q).size()*budgetLists.get(q).size());
                debug("Knapsack Building Query: " + q);
                double convProb = getConversionPrWithPenalty(q, 1.0);
                double salesPrice = _salesPrices.get(q);
                int itemCount = 0;
 
 
                //FIXME: Make configurable whether we allow for generic ads. Right now it's hardcoded that we're always targeting.
                for(int k = 1; k < 2; k++) { //For each possible targeting type (0=untargeted, 1=targetedToSpecialty)
                   for(int i = 0; i < bidLists.get(q).size(); i++) { //For each possible bid
                      for(int j = 0; j < budgetLists.get(q).size(); j++) { //For each possible budget
                         boolean targeting = (k == 0) ? false : true;
                         double bid = bidLists.get(q).get(i);
                         double budget = budgetLists.get(q).get(j);
                         Ad ad = (k == 0) ? new Ad() : getTargetedAd(q);
 
                         double[] impsClicksAndCost = simulateQuery(querySim,q,bid,budget,ad);
                         double numImps = impsClicksAndCost[0];
                         double numClicks = impsClicksAndCost[1];
                         double cost = impsClicksAndCost[2];
 
                         //Amount of impressions our agent sees in each slot
                         double[] slotDistr = new double[] {impsClicksAndCost[3],
                                 impsClicksAndCost[4],
                                 impsClicksAndCost[5],
                                 impsClicksAndCost[6],
                                 impsClicksAndCost[7]};
 
                         //Fraction of IS users that occurred in each slot
                         double[] isRatioArr = new double[] {impsClicksAndCost[8],
                                 impsClicksAndCost[9],
                                 impsClicksAndCost[10],
                                 impsClicksAndCost[11],
                                 impsClicksAndCost[12]};
                         double ISRatio = impsClicksAndCost[13];
                         double CPC = cost / numClicks;
                         double clickPr = numClicks / numImps;
 
                         double convProbWithPen = getConversionPrWithPenalty(q, penalty,ISRatio);
 
 //                        System.out.println("Bid: " + bid);
 //                        System.out.println("Budget: " + budget);
 //                        System.out.println("Targeting: " + targeting);
 //                        System.out.println("numImps: " + numImps);
 //                        System.out.println("numClicks: " + numClicks);
 //                        System.out.println("cost: " + cost);
 //                        System.out.println("CPC: " + CPC);
 //                        System.out.println("clickPr: " + clickPr);
 //                        System.out.println();
 
                         if(Double.isNaN(CPC)) {
                            CPC = 0.0;
                         }
 
                         if(Double.isNaN(clickPr)) {
                            clickPr = 0.0;
                         }
 
                         if(Double.isNaN(convProb)) {
                            convProb = 0.0;
                         }
 
                         double w = numClicks*convProbWithPen;				//weight = numClciks * convProv
                         double v = numClicks*convProbWithPen*salesPrice - numClicks*CPC;	//value = revenue - cost	[profit]
                         itemList.add(new Item(q,w,v,bid,budget,targeting,0,itemCount));
                         queryPredictions.add(new Predictions(clickPr, CPC, convProb, numImps,slotDistr,isRatioArr,ISRatio));
                         itemCount++;
 
                         if(cost + bid*2 < budget) {
                            //If we don't hit our budget, we do not need to consider
                            //higher budgets, since we will have the same result
                            //so we break out of the budget loop
                            break;
                         }
                      }
                   }
                }
 
 
 
 
                debug("Items for " + q);
                if(itemList.size() > 0) {
                   Item[] items = itemList.toArray(new Item[0]);
                   IncItem[] iItems = getIncremental(items);
                   allIncItems.addAll(Arrays.asList(iItems));
                   allPredictionsMap.put(q, queryPredictions);
                }
             }
          }
 
 
          long knapsackEnd = System.currentTimeMillis();
          System.out.println("Time to build knapsacks: " + (knapsackEnd-knapsackStart)/1000.0 );
 
 
 //         PersistentHashMap daySim;
 //         if(hasPerfectModels()) {
 //            daySim = javasim.setStartSales(querySim, _agentToReplace, (int) _day, (int) (_capacity - remainingCap), true);
 //         }
 //         else {
 //            daySim = javasim.setStartSales(querySim, _advId, (int) _day, (int) (_capacity - remainingCap), false);
 //         }
 
          Collections.sort(allIncItems);
 
 
          long solutionStartTime = System.currentTimeMillis();
 
          HashMap<Query,Item> solution;
          if (_multiDayHeuristic == MultiDay.OneDayHeuristic) {
             solution = fillKnapsackWithCapExt(allIncItems, remainingCap, allPredictionsMap);
          } else if (_multiDayHeuristic == MultiDay.HillClimbing) {
             solution = fillKnapsackHillClimbing(bidLists, budgetLists, allPredictionsMap);
          } else if (_multiDayHeuristic == MultiDay.DP) {
             solution = fillKnapsackDP(bidLists,budgetLists,allPredictionsMap);
          } else if (_multiDayHeuristic == MultiDay.DPHill) {
             solution = fillKnapsackDPHill(bidLists,budgetLists,allPredictionsMap);
          } else {
             solution = null;
          }
 
          long solutionEndTime = System.currentTimeMillis();
          System.out.println("Seconds to solution: " + (solutionEndTime-solutionStartTime)/1000.0 );
 
 //         HashMap<Query,Item> solution = fillKnapsackWithCapExt(allIncItems, remainingCap, allPredictionsMap, daySim);
 //         HashMap<Query,Item> solution = fillKnapsackHillClimbing(bidLists, budgetLists, allPredictionsMap);
 //         HashMap<Query,Item> solution = fillKnapsackDP(bidLists,budgetLists,allPredictionsMap); //asdf
 
          //set bids
          for(Query q : _querySpace) {
             ArrayList<Predictions> queryPrediction = allPredictionsMap.get(q);
 
             if(solution.containsKey(q)) {
                Item item = solution.get(q);
                double bid = item.b();
                double budget = item.budget();
                int idx = solution.get(q).idx();
                Predictions predictions = queryPrediction.get(idx);
                double clickPr = predictions.getClickPr();
                double numImps = predictions.getNumImp();
                int numClicks = (int) (clickPr * numImps);
                double CPC = predictions.getCPC();
 
                if(solution.get(q).targ()) {
                   bidBundle.setBid(q, bid);
                   bidBundle.setAd(q, getTargetedAd(q,_manSpecialty,_compSpecialty));
                }
                else {
                   bidBundle.addQuery(q, bid, new Ad());
                }
 
                if(BUDGET && budget == Double.MAX_VALUE) {
                   /*
                          * Only override the budget if the flag is set
                          * and we didn't choose to set a budget
                          */
                   bidBundle.setDailyLimit(q, numClicks*CPC);
                }
                else {
                   bidBundle.setDailyLimit(q, budget);
                }
             }
             else {
                /*
                * We decided that we did not want to be in this query, so we will use it to explore the space
                * no need for exploring if perfect sim though.
                * FIXME: Add a more sophisticated exploration strategy?
                * FIXME: What should the budget be when we explore?
                */
                if(!hasPerfectModels() && !q.getType().equals(QueryType.FOCUS_LEVEL_ZERO)) {
                   double bid = randDouble(_regReserveLow[queryTypeToInt(q.getType())],_salesPrices.get(q) * getConversionPrWithPenalty(q,1.0) * _baseClickProbs.get(q) * .9);
                   bidBundle.addQuery(q, bid, new Ad(), bid*3);
                }
                else {
                   bidBundle.addQuery(q,0.0,new Ad(),0.0);
                }
             }
          }
 
          /*
          * Pass expected conversions to unit sales model
          */
          double solutionWeight;
 //         if(DAY_SIM_WEIGHT_END) {
 //            double[] results = simulateDay(daySim,bidBundle);
 //            solutionWeight = results[3];
 ////            System.out.println("We expect to get " + (int)solutionWeight + "/" + (int)solutionWeight(remainingCap,solution,allPredictionsMap) + " conversions");
 //         }
 //         else {
          solutionWeight = solutionWeight(remainingCap,solution,allPredictionsMap);
 //            System.out.println("We expect to get " + (int)solutionWeight + " conversions");
 //         }
          ((BasicUnitsSoldModel)_unitsSold).expectedConvsTomorrow((int) (solutionWeight));
       }
       else {
          bidBundle = getFirst2DaysBundle();
       }
 
 
       /*
       * Just in case...
       */
       for(Query q : _querySpace) {
          if(Double.isNaN(bidBundle.getBid(q)) || bidBundle.getBid(q) < 0) {
             bidBundle.setBid(q, 0.0);
          }
       }
 
 //      System.out.println(bidBundle);
       return bidBundle;
    }
 
    
    /**
     * Get a single bid for each slot (or X bids per slot).
     * The idea is that our bid models are noisy, so we don't want to 
     * even consider bids on the edges of slots, since we might be getting a completely
     * different slot than we expect.
     * @return
     */
    private HashMap<Query, ArrayList<Double>> getMinimalBidLists() {
 
 	   HashMap<Query,ArrayList<Double>> bidLists = new HashMap<Query,ArrayList<Double>>();
 
 	   for(Query q : _querySpace) {
 		   System.out.println("QUERY " + q);
 		   ArrayList<Double> ourBids = new ArrayList<Double>();
 		   if(!q.equals(new Query())) { //If not the F0 Query. FIXME: Don't hardcode
 
 			   double ourAdvertiserEffect = _paramEstimation.getAdvEffectPrediction(q);
 
 			   //Get list of all squashed opponent bids for this query
 			   ArrayList<Double> opponentScores = new ArrayList<Double>(); 	   
 			   for (String player : _advertisersSet) {
 				   if (!player.equals(_advId)) { //only get opponent bids
 					   //FIXME: Are the opponent bids we get back squashed or unsquashed? I assume unsquashed.
 					   double opponentBid = _bidModel.getPrediction(player, q);
 					   double opponentAdvertiserEffect = _advertiserEffectBoundsAvg[queryTypeToInt(q.getType())]; //FIXME: Have a model estimate this
 					   double opponentScore = opponentBid * Math.pow(opponentAdvertiserEffect, _squashing); 
 					   System.out.println(player + " bid=" + opponentBid + ", advEffect=" + opponentAdvertiserEffect + ", score=" + opponentScore);
 					   opponentScores.add(opponentScore);
 				   }
 			   }
 
 			   //Add regular/promoted reserve score
 			   double reserveScore = _paramEstimation.getRegReservePrediction(q.getType());
 			   double promotedReserveScore = _paramEstimation.getPromReservePrediction(q.getType());
 			   opponentScores.add(reserveScore);
 			   opponentScores.add(promotedReserveScore);
 			   System.out.println("reserveScore=" + reserveScore + ", promoted=" + promotedReserveScore);
 
 			   			   
 			   //We will choose to target scores directly between opponent scores.
 			   //TODO: We could also consider multiple targets between two scores.
 			   Collections.sort(opponentScores);
 			   int BIDS_BETWEEN_SCORES = 3;
 			   ArrayList<Double> ourScores = new ArrayList<Double>();
 			   for (int i=1; i<opponentScores.size(); i++) {
 				   double lowScore = opponentScores.get(i-1);
 				   double highScore = opponentScores.get(i);
 				   for (int j=1; j<=BIDS_BETWEEN_SCORES; j++) {
 					   double ourScore = lowScore + j*(highScore-lowScore)/(BIDS_BETWEEN_SCORES+1);
 					   ourScores.add(ourScore);   
 				   }				   
 			   }
 
 			   System.out.println("Our targeted scores: " + ourScores);
 			   
 			   //Also ad a score to bid directly above the reserve score,
 			   //And directly above the highest score (so we get the 1st slot)
 			   double scoreEpsilon = .01;
 			   double highestOpponentScore = opponentScores.get(opponentScores.size()-1); 
 			   ourScores.add(reserveScore + scoreEpsilon);
 			   ourScores.add(highestOpponentScore + scoreEpsilon);
 			   
 			   System.out.println("After adding min/max, our targeted scores: " + ourScores);
 			   
 			   //Remove any duplicate scores, or any scores outside a reasonable boundry
 			   double FRACTION = 1; //_baseClickProbs.get(q); //FIXME: There's no reason this should be a clickProb.
 			   double ourVPC = _salesPrices.get(q) * getConversionPrWithPenalty(q,1.0) * FRACTION; 
 			   double maxScore = ourVPC * Math.pow(ourAdvertiserEffect, _squashing);
 			   ArrayList<Double> ourPrunedScores = new ArrayList<Double>();
 			   Collections.sort(ourScores);
 			   for (int i=0; i<ourScores.size(); i++) {
 				   double score = ourScores.get(i);
 				   if (score >= reserveScore && score <= maxScore) { //within bounds
 					   int lastAddedIdx = ourPrunedScores.size()-1;
 					   if (lastAddedIdx==-1 || score != ourPrunedScores.get(lastAddedIdx)) { //not just added 
 						   ourPrunedScores.add(score);
 					   }
 				   }
 			   }
 			   
 			   System.out.println("Our pruned targeted scores: " + ourPrunedScores);
 			   
 			   //Turn score into bids
 			   for (double score : ourPrunedScores) {
				   double ourBid = score / ourAdvertiserEffect;
 				   ourBids.add(ourBid);
 			   }
 			   
 			   System.out.println("Our advEffect:" + ourAdvertiserEffect);
 			   System.out.println("Our bids: " + ourBids);
 		   }
 
 		   bidLists.put(q, ourBids);
 
 	   }
 	   return bidLists;
    }
 
 private HashMap<Query, ArrayList<Double>> getBidLists() {
        HashMap<Query,ArrayList<Double>> bidLists = new HashMap<Query,ArrayList<Double>>();
        
        for(Query q : _querySpace) {
           if(!q.equals(new Query())) { //If not the F0 Query. FIXME: Why are we checking this twice? And which is the proper way to check?
              ArrayList<Double> newBids = new ArrayList<Double>();
 
              if(q.getType().equals(QueryType.FOCUS_LEVEL_ZERO)) {
                 double increment  = .4; //FIXME: Make these class fields
                 double min = .08;
                 double max = 1.0;
                 int tot = (int) Math.ceil((max-min) / increment);
                 for(int i = 0; i < tot; i++) {
                    newBids.add(min+(i*increment));
                 }
              }
              else {
                 double increment  = .1;
                 double min = _regReserveLow[queryTypeToInt(q.getType())];
                 
                 //This is roughly the expected revenue we get for a click. Never bid above this.
                 //TODO (low priority): SalesPrice and ConversionPr could be better estimated for the given day. 
                 //We may not want to consider bids this high (e.g. we might start the day with a high penalty).
                 //Note, though, that these bids are considered on future days as well, where we may not have used any capacity.
                 
                 //FIXME: Multiplying by baseClickProbs is not correct, but it was performing better, so only for the time being I'll leave it in.
                 double max = _salesPrices.get(q) * getConversionPrWithPenalty(q,1.0) * _baseClickProbs.get(q);
                 //double max = _salesPrices.get(q) * getConversionPrWithPenalty(q,1.0);
                 
                 
                 int tot = (int) Math.ceil((max-min) / increment);
                 for(int i = 0; i < tot; i++) {
                    newBids.add(min+(i*increment));
                 }
              }
 
              Collections.sort(newBids);
 
 //             System.out.println("Bids for " + q + ": " + newBids);
              bidLists.put(q, newBids);
           }
           else {
              bidLists.put(q,new ArrayList<Double>());
           }
        }
        
 	return bidLists;
 }
 
 private HashMap<Query, ArrayList<Double>> getBudgetLists() {
        HashMap<Query,ArrayList<Double>> budgetLists = new HashMap<Query,ArrayList<Double>>();
        
        for(Query q : _querySpace) {
           if(!q.equals(new Query())) { //Skip over F0 Query FIXME: Make configurable
              ArrayList<Double> budgetList = new ArrayList<Double>();
              budgetList.add(50.0);
              budgetList.add(100.0);
              budgetList.add(200.0);
              budgetList.add(300.0);
              budgetList.add(Double.MAX_VALUE);
              budgetLists.put(q,budgetList);
           }
           else {
              budgetLists.put(q,new ArrayList<Double>());
           }
        }
 	return budgetLists;
    }
    
 
 public BidBundle getFirst2DaysBundle() {
       BidBundle bundle = new BidBundle();
       /*
          * Bound these with the reseve scores
          */
       for(Query q : _querySpace){
          if(_compSpecialty.equals(q.getComponent()) || _manSpecialty.equals(q.getManufacturer())) {
             double bid = randDouble(_salesPrices.get(q) * getConversionPrWithPenalty(q,1.0) * _baseClickProbs.get(q) * _lowBidMult, _salesPrices.get(q) * getConversionPrWithPenalty(q,1.0) * _baseClickProbs.get(q) * _highBidMult);
             bundle.addQuery(q, bid, new Ad(), Double.MAX_VALUE);
          }
          else {
             if(!q.equals(new Query())) {
                double bid = randDouble(_regReserveLow[queryTypeToInt(q.getType())],_salesPrices.get(q) * getConversionPrWithPenalty(q,1.0) * _baseClickProbs.get(q) * .9);
                bundle.addQuery(q, bid, new Ad(), bid*5);
             }
             else {
                bundle.addQuery(q, 0, new Ad(), 0);
             }
          }
       }
       bundle.setCampaignDailySpendLimit(_totalBudgets.get(_capacity));
       return bundle;
    }
 
    private BidBundle mkBundleFromKnapsack(HashMap<Query,Item> solution) {
       BidBundle bundle = new BidBundle();
       for(Query q : _querySpace) {
          if(solution.containsKey(q)) {
             Item item = solution.get(q);
             double bid = item.b();
             double budget = item.budget();
 
             if(solution.get(q).targ()) {
                bundle.setBid(q, bid);
                bundle.setAd(q, getTargetedAd(q,_manSpecialty,_compSpecialty));
             }
             else {
                bundle.addQuery(q, bid, new Ad());
             }
             bundle.setDailyLimit(q, budget);
          }
          else {
             bundle.setBid(q,0.0);
             bundle.setAd(q, new Ad());
             bundle.setDailyLimit(q, Double.MAX_VALUE);
          }
       }
       bundle.setCampaignDailySpendLimit(Double.MAX_VALUE);
       return bundle;
    }
 
    private ArrayList<Double> removeDupes(ArrayList<Double> bids) {
       ArrayList<Double> noDupeList = new ArrayList<Double>();
       for(int i = 0; i < bids.size()-1; i++) {
          noDupeList.add(bids.get(i));
          while((i+1 < bids.size()-1) && (bids.get(i) == bids.get(i+1))) {
             i++;
          }
       }
       return noDupeList;
    }
 
    private Ad getTargetedAd(Query q) {
       return getTargetedAd(q, _manSpecialty, _compSpecialty);
    }
 
    private Ad getTargetedAd(Query q, String manSpecialty, String compSpecialty) {
       Ad ad;
       if (q.getType().equals(QueryType.FOCUS_LEVEL_ZERO)) {
          /*
              * F0 Query, target our specialty
              */
          ad = new Ad(new Product(manSpecialty, compSpecialty));
       }
       else if (q.getType().equals(QueryType.FOCUS_LEVEL_ONE)) {
          if(q.getComponent() == null) {
             /*
                  * F1 Query (comp = null), so target the subgroup that searches for this and our
                  * component specialty
                  */
             ad = new Ad(new Product(q.getManufacturer(), compSpecialty));
          }
          else {
             /*
                  * F1 Query (man = null), so target the subgroup that searches for this and our
                  * manufacturer specialty
                  */
             ad = new Ad(new Product(manSpecialty, q.getComponent()));
          }
       }
       else  {
          /*
              * F2 Query, so target the subgroup that searches for this
              */
          ad = new Ad(new Product(q.getManufacturer(), q.getComponent()));
       }
       return ad;
    }
    
    
    /**
     * This gets an ad that users are actually less likely to click on.
     * We may want this if we are either exploring the bid space or trying to bid vindictively.
     * If there isn't a comp/man to insert into the ad to make users less likely to 
     * click, we'll return our specialty.
     * @param q
     * @return
     */
    private Ad getIrrelevantAd(Query q) {
 	   
 	   String componentToUse = _compSpecialty;
 	   String manufacturerToUse = _manSpecialty;
 	   
 	   String qComponent = q.getComponent(); //could be null
 	   if (qComponent != null) {
 		   for (String component : _retailCatalog.getComponents()) {
 			   if (!component.equals(qComponent)) {
 				   componentToUse = component;
 				   break;
 			   }
 		   }
 	   }
 
 	   String qManufacturer = q.getManufacturer(); //could be null
 	   if (qManufacturer != null) {
 		   for (String manufacturer : _retailCatalog.getManufacturers()) {
 			   if (!manufacturer.equals(qManufacturer)) {
 				   manufacturerToUse = manufacturer;
 				   break;
 			   }
 		   }
 	   }
 
 	   return new Ad(new Product(manufacturerToUse, componentToUse));
    }
    
    
    
 
    @Override
    public void updateModels(SalesReport salesReport, QueryReport queryReport) {
 
 //      System.out.println("Updating models on day " + _day);
       if(!hasPerfectModels()) {
     	  
     	  _adTypeEstimator.updateModel(queryReport);
     	  _specialtyModel.updateModel(queryReport);
     	  
          BidBundle bidBundle = _bidBundles.get(_bidBundles.size()-2);
 
          if(!USER_MODEL_UB) {
             _maxImps = new HashMap<Query,Integer>();
             for(Query q : _querySpace) {
                int numImps;
                if(q.getType().equals(QueryType.FOCUS_LEVEL_ZERO)) {
                   numImps = MAX_F0_IMPS;
                }
                else if(q.getType().equals(QueryType.FOCUS_LEVEL_ONE)) {
                   numImps = MAX_F1_IMPS;
                }
                else {
                   numImps = MAX_F2_IMPS;
                }
                _maxImps.put(q, numImps);
             }
          }
          else {
             HashMap<Product,HashMap<UserState,Integer>> preUpdateUserStates = getUserStates(_userModel,_products);
             _maxImps = getMaxImpsPred(preUpdateUserStates,USER_MODEL_UB_MULT,_querySpace);
          }
 
          _queryAnalyzer.updateModel(queryReport, bidBundle, _maxImps);
 
          HashMap<Query,Integer> totalImpressions = new HashMap<Query,Integer>();
          HashMap<Query,HashMap<String, Integer>> ranks = new HashMap<Query,HashMap<String,Integer>>();
          HashMap<Query,HashMap<String, Boolean>> rankablesBid = new HashMap<Query,HashMap<String,Boolean>>();
          HashMap<Query,HashMap<String, Boolean>> rankables = new HashMap<Query,HashMap<String,Boolean>>(); //Agents that are padded or not in the auction are *not* rankable
          HashMap<Query,int[]> fullOrders = new HashMap<Query,int[]>();
          HashMap<Query,int[]> fullImpressions = new HashMap<Query,int[]>();
          HashMap<Query,int[][]> fullWaterfalls = new HashMap<Query, int[][]>();
          for(Query q : _querySpace) {
             int[] impsPred = _queryAnalyzer.getImpressionsPrediction(q);
             int[] ranksPred = _queryAnalyzer.getOrderPrediction(q);
             int[][] waterfallPred = _queryAnalyzer.getImpressionRangePrediction(q);
             int totalImps = _queryAnalyzer.getTotImps(q);
 
 //            System.out.println("Query Analyzer Results for " + q);
 //            System.out.println("impsPred: " + Arrays.toString(impsPred));
 //            System.out.println("ranksPred: " + Arrays.toString(ranksPred));
 //            if(waterfallPred != null) {
 //               System.out.println("waterfall: ");
 //               for(int i = 0; i < waterfallPred.length; i++) {
 //                  System.out.println("\t" + Arrays.toString(waterfallPred[i]));
 //               }
 //            }
 //            else {
 //               System.out.println("waterfall: null");
 //            }
 
             if(totalImps == 0) {
                //this means something bad happened
                totalImps = -1;
             }
 
             fullOrders.put(q, ranksPred);
             fullImpressions.put(q, impsPred);
             fullWaterfalls.put(q,waterfallPred);
             totalImpressions.put(q, totalImps);
 
             HashMap<String, Integer> perQRanks = null;
             if(waterfallPred != null) {
                perQRanks = new HashMap<String,Integer>();
                for(int i = 0; i < _advertisers.size(); i++) {
                   perQRanks.put("adv" + (ranksPred[i] + 1),i);
                }
             }
             ranks.put(q, perQRanks);
 //            System.out.println("perQRanks: " + perQRanks);
 
 
 
 
             //This is checking which agents have an assigned ranking from the QA (for budget and bid estimation). 
             //If RANKABLE==false, assume everyone was assigned a ranking
             HashMap<String, Boolean> rankable = null;
             HashMap<String, Boolean> rankableBid = null;
             if(waterfallPred != null) {
                if(RANKABLE) {
                   rankable = _queryAnalyzer.getRankableMap(q);
                }
                else {
                   rankable = new HashMap<String,Boolean>();
                   for(int i = 0; i < _advertisers.size(); i++) {
                      rankable.put("adv"+(i+1),true);
                   }
                }
 
                rankableBid = new HashMap<String,Boolean>();
                for(int i = 0; i < _advertisers.size(); i++) {
                   rankableBid.put("adv"+(i+1),true);
                }
             }
             rankables.put(q,rankable);
             rankablesBid.put(q,rankableBid);
          }
 
          _userModel.updateModel(totalImpressions);
 
          HashMap<Product,HashMap<UserState,Integer>> userStates = getUserStates(_userModel,_products);
 
          _paramEstimation.updateModel(queryReport, bidBundle, fullImpressions, fullWaterfalls, userStates, _c);
 
          for(Query q : _querySpace) {
             int qtIdx = queryTypeToInt(q.getType());
             _ISRatioModel.updateISRatio(q,getISRatio(q,_numSlots,_numPS,_advertiserEffectBoundsAvg[qtIdx],_paramEstimation.getContProbPrediction(q),_c[qtIdx],userStates));
          }
 
          HashMap<Query, Double> cpc = new HashMap<Query,Double>();
          HashMap<Query, Double> ourBid = new HashMap<Query,Double>();
          for(Query q : _querySpace) {
             cpc.put(q, queryReport.getCPC(q));
             ourBid.put(q, bidBundle.getBid(q));
          }
          _bidModel.updateModel(cpc, ourBid, ranks, rankablesBid);
 
          HashMap<Query,Double> contProbs = new HashMap<Query,Double>();
          HashMap<Query, double[]> allbids = new HashMap<Query,double[]>();
          for(Query q : _querySpace) {
             contProbs.put(q, _paramEstimation.getContProbPrediction(q));
             double oppAdvEffect = _advertiserEffectBoundsAvg[queryTypeToInt(q.getType())];
             double oppSquashedAdvEff = Math.pow(oppAdvEffect, _squashing);
             double[] bids = new double[_advertisers.size()];
             for(int j = 0; j < bids.length; j++) {
                if(j == _advIdx) {
                   bids[j] = bidBundle.getBid(q) * Math.pow(_paramEstimation.getAdvEffectPrediction(q),_squashing);
                }
                else {
                   bids[j] = _bidModel.getPrediction("adv" + (j+1), q) * oppSquashedAdvEff;
                }
             }
             allbids.put(q, bids);
          }
 
          double[] regReserve = new double[3];
          regReserve[0] = _paramEstimation.getRegReservePrediction(QueryType.FOCUS_LEVEL_ZERO);
          regReserve[1] = _paramEstimation.getRegReservePrediction(QueryType.FOCUS_LEVEL_ONE);
          regReserve[2] = _paramEstimation.getRegReservePrediction(QueryType.FOCUS_LEVEL_TWO);
 
          _budgetEstimator.updateModel(queryReport, bidBundle, _c, contProbs, regReserve, fullOrders,fullImpressions,fullWaterfalls, rankables, allbids, userStates);
 
          _unitsSold.update(salesReport); //FIXME: Move this and salesDist to beginning, in case other models want to use them (e.g. QA)
       }
    }
 
    public static HashMap<Product,HashMap<UserState,Integer>> getUserStates(ParticleFilterAbstractUserModel userModel, Set<Product> products) {
       HashMap<Product,HashMap<UserState,Integer>> userStates = new HashMap<Product,HashMap<UserState,Integer>>();
       for(Product p : products) {
          HashMap<UserState,Integer> userState = new HashMap<UserState,Integer>();
          for(UserState s : UserState.values()) {
             userState.put(s, userModel.getCurrentEstimate(p, s));
          }
          userStates.put(p, userState);
       }
       return userStates;
    }
 
    public static HashMap<Query,Integer> getMaxImpsPred(HashMap<Product,HashMap<UserState,Integer>> userStates, double userModelUBMult, Set<Query> querySpace) {
       HashMap<Query,Integer> maxImps = new HashMap<Query, Integer>(querySpace.size());
       for (Query q : querySpace) {
          int numImps = 0;
          for (Product p : userStates.keySet()) {
             if (q.getType().equals(QueryType.FOCUS_LEVEL_ZERO)) {
                numImps += userStates.get(p).get(UserState.F0);
                numImps += userModelUBMult * userStates.get(p).get(UserState.IS) / 3.0;
             } else if (q.getType().equals(QueryType.FOCUS_LEVEL_ONE)) {
                if (p.getComponent().equals(q.getComponent()) || p.getManufacturer().equals(q.getManufacturer())) {
                   numImps += userStates.get(p).get(UserState.F1) / 2.0;
                   numImps += userModelUBMult * userStates.get(p).get(UserState.IS) / 6.0;
                }
             } else if (q.getType().equals(QueryType.FOCUS_LEVEL_TWO)) {
                if (p.getComponent().equals(q.getComponent()) && p.getManufacturer().equals(q.getManufacturer())) {
                   numImps += userStates.get(p).get(UserState.F2);
                   numImps += userModelUBMult * userStates.get(p).get(UserState.IS) / 3.0;
                }
             }
          }
          maxImps.put(q, numImps);
       }
       return maxImps;
    }
 
    public double getPenalty(double remainingCap, double solutionWeight) {
       return getPenalty(remainingCap,solutionWeight,_lambda);
    }
 
    /**
     * This gets the average penalty across clicks, for a given initial remaining capacity and number of conversions.
     * @param remainingCap
     * @param solutionWeight
     * @param lambda
     * @return
     */
    public static double getPenalty(double remainingCap, double solutionWeight, double lambda) {
       double penalty;
       solutionWeight = Math.max(0,solutionWeight);
       if(remainingCap < 0) {
          if(solutionWeight <= 0) {
             penalty = Math.pow(lambda, Math.abs(remainingCap));
          }
          else {
             //Average penalty per click:
             //For each conversion, compute its penalty. Use this to get conversion probability at that penalty,
             //and then use this to get expected number of clicks at that penalty.
             //There is a different penalty for each conversion. Average penalty is:
             // (\sum_{conversion} penaltyForConversion * expectedClicksAtPenalty) / totalClicks
             // where, for a given conversion (and thus penalty),  expectedClicksAtPenalty = 1 / PrConv(penalty)
             // and totalClicks = \sum_{conversion} expectedClicksAtPenalty
             // i.e. (ignoring component specialties),
             //    avgPenalty = \sum{c} I_c * ( 1/(pi*I_c) )
 
             //FIXME: We're currently making the simplifying assumption that PrConv(penalty) = penalty.
             //So the summation becomes \sum_{conversion} penaltyForConversion (1/penaltyForConversion) = #conversions
             //And totalClicks = \sum_{conversion} 1/penaltyForConversion
             //This is probably not affecting things much, but we should use the correct formula at some point.
 
             double penWeight = 0.0;
             int convs = 0;
             for(double j = Math.abs(remainingCap)+1; j <= Math.abs(remainingCap)+solutionWeight; j++) {
                penWeight += 1.0 / Math.pow(lambda, j);
                convs++;
             }
             penalty = ((double) convs) / penWeight;
          }
       }
       else {
          if(solutionWeight <= 0) {
             penalty = 1.0;
          }
          else {
             if(solutionWeight > remainingCap) {
                //FIXME: Same as above.
                double penWeight = remainingCap;
                int convs = ((int)remainingCap);
                for(int j = 1; j <= solutionWeight-remainingCap; j++) {
                   penWeight += 1.0 / Math.pow(lambda, j);
                   convs++;
                }
                penalty = ((double) convs) / penWeight;
             }
             else {
                penalty = 1.0;
             }
          }
       }
       if(Double.isNaN(penalty)) {
          penalty = 1.0;
       }
       return penalty;
    }
 
    private double[] solutionValueMultiDay(HashMap<Query, Item> solution, double remainingCap, HashMap<Query, ArrayList<Predictions>> allPredictionsMap, int numDays) {
 
       double totalWeight;
       double weightMult;
 //      if(DAY_SIM_WEIGHT_OPT) {
 //         double[] results = simulateDay(daySim,mkBundleFromKnapsack(solution));
 //         totalWeight = results[3];
 //         weightMult = totalWeight / solutionWeight(remainingCap, solution, allPredictionsMap);
 //      }
 //      else {
       totalWeight = solutionWeight(remainingCap, solution, allPredictionsMap);
       weightMult = 1.0;
 //      }
 
       double penalty = getPenalty(remainingCap, totalWeight);
 
       double totalValue = 0;
       for(Query q : _querySpace) {
          if(solution.containsKey(q)) {
             Item item = solution.get(q);
             Predictions prediction = allPredictionsMap.get(item.q()).get(item.idx());
             totalValue += prediction.getClickPr()*prediction.getNumImp()*(getConversionPrWithPenalty(q, penalty,prediction.getISRatio())*_salesPrices.get(item.q()) - prediction.getCPC());
          }
       }
 
       double daysLookahead = Math.max(0, Math.min(numDays, 58 - _day));
       if(daysLookahead > 0 && totalWeight > 0) {
          ArrayList<Integer> soldArray;
          if(!hasPerfectModels()) {
             ArrayList<Integer> soldArrayTMP = ((BasicUnitsSoldModel) _unitsSold).getSalesArray();
             soldArray = new ArrayList<Integer>(soldArrayTMP);
 
             Integer expectedConvsYesterday = ((BasicUnitsSoldModel) _unitsSold).getExpectedConvsTomorrow();
             if(expectedConvsYesterday == null) {
                expectedConvsYesterday = 0;
                int counter2 = 0;
                for(int j = 0; j < 5 && j < soldArray.size(); j++) {
                   expectedConvsYesterday += soldArray.get(soldArray.size()-1-j);
                   counter2++;
                }
                expectedConvsYesterday = (int) (expectedConvsYesterday / (double) counter2);
             }
             soldArray.add(expectedConvsYesterday);
          }
          else {
             soldArray = new ArrayList<Integer>(_perfectStartSales.length);
 
             if(_perfectStartSales.length < (_capWindow-1)) {
                for(int i = 0; i < (_capWindow - 1 - _perfectStartSales.length); i++) {
                   soldArray.add((int)(_capacity / ((double) _capWindow)));
                }
             }
 
             for(Integer numConvs : _perfectStartSales) {
                soldArray.add(numConvs);
             }
          }
          soldArray.add((int) totalWeight);
 
          for(int i = 0; i < daysLookahead; i++) {
             //Compute amount that can be sold on this day.
             //Start budget at capacity limit, and subtract off sales
             //from each of the past days within the window excluding today (4 days w/ current game settings)
             double expectedBudget = _capacity*_capMod.get(_capacity);
             for(int j = 0; j < _capWindow-1; j++) {
                int idx = soldArray.size() - 1 - j;
                double defaultSales = _capacity/(double)_capWindow; //TODO: The other alternative is to pad soldArray. This might be cleaner.
                if (idx<0) expectedBudget -= defaultSales;
                else expectedBudget -= soldArray.get(idx);
             }
 
             double numSales = solutionWeight(expectedBudget, solution, allPredictionsMap)*weightMult;
             soldArray.add((int) numSales);
 
             double penaltyNew = getPenalty(expectedBudget, numSales);
             for(Query q : _querySpace) {
                if(solution.containsKey(q)) {
                   Item item = solution.get(q);
                   Predictions prediction = allPredictionsMap.get(item.q()).get(item.idx());
                   totalValue += prediction.getClickPr()*prediction.getNumImp()*(getConversionPrWithPenalty(q, penaltyNew,prediction.getISRatio())*_salesPrices.get(item.q()) - prediction.getCPC());
                }
             }
          }
       }
       double[] output = new double[2];
       output[0] = totalValue;
       output[1] = totalWeight;
       return output;
    }
 
    private double solutionWeight(double budget, HashMap<Query, Item> solution, HashMap<Query, ArrayList<Predictions>> allPredictionsMap, BidBundle bidBundle) {
       double threshold = .5;
       int maxIters = 15;
       double lastSolWeight = Double.MAX_VALUE;
       double solutionWeight = 0.0;
 
       /*
          * As a first estimate use the weight of the solution
          * with no penalty
          */
       for(Query q : _querySpace) {
          if(solution.get(q) == null) {
             continue;
          }
          Predictions predictions = allPredictionsMap.get(q).get(solution.get(q).idx());
          double dailyLimit = Double.NaN;
          if(bidBundle != null) {
             dailyLimit  = bidBundle.getDailyLimit(q);
          }
          double clickPr = predictions.getClickPr();
          double numImps = predictions.getNumImp();
          int numClicks = (int) (clickPr * numImps);
          double CPC = predictions.getCPC();
          double convProb = getConversionPrWithPenalty(q, 1.0);
 
          if(Double.isNaN(CPC)) {
             CPC = 0.0;
          }
 
          if(Double.isNaN(clickPr)) {
             clickPr = 0.0;
          }
 
          if(Double.isNaN(convProb)) {
             convProb = 0.0;
          }
 
          if(!Double.isNaN(dailyLimit)) {
             if(numClicks*CPC > dailyLimit) {
                numClicks = (int) (dailyLimit/CPC);
             }
          }
 
          solutionWeight += numClicks*convProb;
       }
 
       double originalSolWeight = solutionWeight;
 
       int numIters = 0;
       while(Math.abs(lastSolWeight-solutionWeight) > threshold) {
          numIters++;
          if(numIters > maxIters) {
             numIters = 0;
             solutionWeight = (_R.nextDouble() + .5) * originalSolWeight; //restart the search
             threshold *= 1.5; //increase the threshold
             maxIters *= 1.25;
          }
          lastSolWeight = solutionWeight;
          solutionWeight = 0;
          double penalty = getPenalty(budget, lastSolWeight);
          for(Query q : _querySpace) {
             if(solution.get(q) == null) {
                continue;
             }
             Predictions predictions = allPredictionsMap.get(q).get(solution.get(q).idx());
             double dailyLimit = Double.NaN;
             if(bidBundle != null) {
                dailyLimit  = bidBundle.getDailyLimit(q);
             }
             double clickPr = predictions.getClickPr();
             double numImps = predictions.getNumImp();
             int numClicks = (int) (clickPr * numImps);
             double CPC = predictions.getCPC();
             double convProb = getConversionPrWithPenalty(q, penalty,predictions.getISRatio());
 
             if(Double.isNaN(CPC)) {
                CPC = 0.0;
             }
 
             if(Double.isNaN(clickPr)) {
                clickPr = 0.0;
             }
 
             if(Double.isNaN(convProb)) {
                convProb = 0.0;
             }
 
             if(!Double.isNaN(dailyLimit)) {
                if(numClicks*CPC > dailyLimit) {
                   numClicks = (int) (dailyLimit/CPC);
                }
             }
 
             solutionWeight += numClicks*convProb;
          }
       }
       return solutionWeight;
    }
 
    private double solutionWeight(double budget, HashMap<Query, Item> solution, HashMap<Query, ArrayList<Predictions>> allPredictionsMap) {
       return solutionWeight(budget, solution, allPredictionsMap, null);
    }
 
    private HashMap<Query,Item> fillKnapsackWithCapExt(ArrayList<IncItem> incItems, double budget, HashMap<Query,ArrayList<Predictions>> allPredictionsMap){
       HashMap<Query,Item> solution = new HashMap<Query, Item>();
       int expectedConvs = 0;
       double[] lastSolVal = null;
       for(int i = 0; i < incItems.size(); i++) {
          IncItem ii = incItems.get(i);
          double itemWeight = ii.w();
          //			double itemValue = ii.v();
          if(budget >= (expectedConvs + itemWeight)) {
             solution.put(ii.item().q(), ii.item());
             expectedConvs += itemWeight;
          }
          else {
             double[] currSolVal;
             if(lastSolVal != null) {
                currSolVal = lastSolVal;
             }
             else {
                currSolVal = solutionValueMultiDay(solution, budget, allPredictionsMap, 15);
             }
 
             HashMap<Query, Item> solutionCopy = (HashMap<Query, Item>)solution.clone();
             solutionCopy.put(ii.item().q(), ii.item());
             double[] newSolVal = solutionValueMultiDay(solutionCopy, budget, allPredictionsMap, 15);
 
             //				System.out.println("[" + _day +"] CurrSolVal: " + currSolVal[0] + ", NewSolVal: " + newSolVal[0]);
 
             if(newSolVal[0] > currSolVal[0]) {
                solution.put(ii.item().q(), ii.item());
                expectedConvs += ii.w();
                lastSolVal = newSolVal;
 
                if(i != incItems.size() - 1) {
                   /*
                          * Discount the item based on the current penalty level
                          */
                   double penalty = getPenalty(budget, newSolVal[1]);
 
                   if(FORWARDUPDATING && !PRICELINES) {
                      //Update next item
                      IncItem nextItem  = incItems.get(i+1);
                      double v,w;
                      if(nextItem.itemLow() != null) {
                         Predictions prediction1 = allPredictionsMap.get(nextItem.item().q()).get(nextItem.itemLow().idx());
                         Predictions prediction2 = allPredictionsMap.get(nextItem.item().q()).get(nextItem.itemHigh().idx());
                         v = prediction2.getClickPr()*prediction2.getNumImp()*(getConversionPrWithPenalty(nextItem.item().q(), penalty,prediction2.getISRatio())*_salesPrices.get(nextItem.item().q()) - prediction2.getCPC()) -
                                 (prediction1.getClickPr()*prediction1.getNumImp()*(getConversionPrWithPenalty(nextItem.item().q(), penalty,prediction1.getISRatio())*_salesPrices.get(nextItem.item().q()) - prediction1.getCPC()));
                         w = prediction2.getClickPr()*prediction2.getNumImp()*getConversionPrWithPenalty(nextItem.item().q(), penalty,prediction2.getISRatio()) -
                                 (prediction1.getClickPr()*prediction1.getNumImp()*getConversionPrWithPenalty(nextItem.item().q(), penalty,prediction1.getISRatio()));
                      }
                      else {
                         Predictions prediction = allPredictionsMap.get(nextItem.item().q()).get(nextItem.itemHigh().idx());
                         v = prediction.getClickPr()*prediction.getNumImp()*(getConversionPrWithPenalty(nextItem.item().q(), penalty,prediction.getISRatio())*_salesPrices.get(nextItem.item().q()) - prediction.getCPC());
                         w = prediction.getClickPr()*prediction.getNumImp()*getConversionPrWithPenalty(nextItem.item().q(), penalty,prediction.getISRatio());
                      }
                      IncItem newNextItem = new IncItem(w, v, nextItem.itemHigh(), nextItem.itemLow());
                      incItems.remove(i+1);
                      incItems.add(i+1, newNextItem);
                   }
                   else if(PRICELINES) {
                      ArrayList<IncItem> updatedItems = new ArrayList<IncItem>();
                      for(int j = i+1; j < incItems.size(); j++) {
                         IncItem incItem = incItems.get(j);
                         Item itemLow = incItem.itemLow();
                         Item itemHigh = incItem.itemHigh();
 
                         double newPenalty;
                         if(UPDATE_WITH_ITEM) {
                            HashMap<Query, Item> solutionInnerCopy = (HashMap<Query, Item>)solutionCopy.clone();
                            solutionInnerCopy.put(incItem.item().q(), incItem.item());
                            double solWeight = solutionWeight(budget, solutionInnerCopy, allPredictionsMap);
                            newPenalty = getPenalty(budget, solWeight);
                         }
                         else {
                            newPenalty = penalty;
                         }
 
                         double newWeight,newValue;
 
                         if(itemLow != null) {
                            Predictions prediction1 = allPredictionsMap.get(itemHigh.q()).get(itemLow.idx());
                            Predictions prediction2 = allPredictionsMap.get(itemHigh.q()).get(itemHigh.idx());
                            newValue = prediction2.getClickPr()*prediction2.getNumImp()*(getConversionPrWithPenalty(incItem.item().q(), newPenalty,prediction2.getISRatio())*_salesPrices.get(itemHigh.q()) - prediction2.getCPC()) -
                                    (prediction1.getClickPr()*prediction1.getNumImp()*(getConversionPrWithPenalty(incItem.item().q(), newPenalty,prediction1.getISRatio())*_salesPrices.get(itemHigh.q()) - prediction1.getCPC())) ;
                            newWeight = prediction2.getClickPr()*prediction2.getNumImp()*getConversionPrWithPenalty(incItem.item().q(), newPenalty,prediction2.getISRatio()) -
                                    (prediction1.getClickPr()*prediction1.getNumImp()*getConversionPrWithPenalty(incItem.item().q(), newPenalty,prediction1.getISRatio()));
                         }
                         else {
                            Predictions prediction = allPredictionsMap.get(itemHigh.q()).get(itemHigh.idx());
                            newValue = prediction.getClickPr()*prediction.getNumImp()*(getConversionPrWithPenalty(incItem.item().q(), newPenalty,prediction.getISRatio())*_salesPrices.get(itemHigh.q()) - prediction.getCPC());
                            newWeight = prediction.getClickPr()*prediction.getNumImp()*getConversionPrWithPenalty(incItem.item().q(), newPenalty,prediction.getISRatio());
                         }
                         IncItem newItem = new IncItem(newWeight,newValue,itemHigh,itemLow);
                         updatedItems.add(newItem);
                      }
 
                      Collections.sort(updatedItems);
 
                      while(incItems.size() > i+1) {
                         incItems.remove(incItems.size()-1);
                      }
                      for(IncItem priceLineItem : updatedItems) {
                         incItems.add(incItems.size(),priceLineItem);
                      }
                   }
                }
             }
             else {
                break;
             }
          }
       }
       return solution;
    }
 
 
 
    /**
     * By default, don't take any initial sales as input. Initial sales will be a default constant.
     * @param bidLists
     * @param budgetLists
     * @param allPredictionsMap
     * @return
     */
    private HashMap<Query,Item> fillKnapsackHillClimbing(HashMap<Query,
            ArrayList<Double>> bidLists, HashMap<Query, ArrayList<Double>> budgetLists, HashMap<Query, ArrayList<Predictions>> allPredictionsMap){
       int windowSize = 59; //TODO: don't hard code this
       int initSales = (int)(_capacity*_capMod.get(_capacity) / ((double) _capWindow));
       int[] initialSales = new int[windowSize];
       Arrays.fill(initialSales, initSales);
       return fillKnapsackHillClimbing(bidLists, budgetLists, allPredictionsMap, initialSales);
    }
 
 
 
    private HashMap<Query,Item> fillKnapsackHillClimbing(HashMap<Query, ArrayList<Double>> bidLists, HashMap<Query, ArrayList<Double>> budgetLists, HashMap<Query, ArrayList<Predictions>> allPredictionsMap, int[] initialSales){
 
       int[] preDaySales = new int[_capWindow-1];
       if(!hasPerfectModels()) {
          ArrayList<Integer> soldArrayTMP = ((BasicUnitsSoldModel) _unitsSold).getSalesArray();
          ArrayList<Integer> soldArray = new ArrayList<Integer>(soldArrayTMP);
 
          Integer expectedConvsYesterday = ((BasicUnitsSoldModel) _unitsSold).getExpectedConvsTomorrow();
          soldArray.add(expectedConvsYesterday);
 
          for(int i = 0; i < (_capWindow-1); i++) {
             int idx = soldArray.size()-1-i;
             if(idx >= 0) {
                preDaySales[_capWindow-2-i] = soldArray.get(idx);
             }
             else {
                preDaySales[_capWindow-2-i] = (int)(_capacity / ((double) _capWindow));
             }
          }
       }
       else {
          for(int i = 0; i < (_capWindow-1); i++) {
             int idx = _perfectStartSales.length-1-i;
             if(idx >= 0) {
                preDaySales[_capWindow-2-i] = _perfectStartSales[idx];
             }
             else {
                preDaySales[_capWindow-2-i] = (int)(_capacity / ((double) _capWindow));
             }
          }
       }
       System.out.println("day " + _day + ": " + "preDaySales=" + Arrays.toString(preDaySales));
 
       int startRemCap = (int)(_capacity*_capMod.get(_capacity));
       for (int preDaySale : preDaySales) {
          startRemCap -= preDaySale;
       }
 
       int daysAhead = Math.max(0,58-(int)_day)+1;
       int capacityIncrement = _multiDayDiscretization; //10; //50; //10;
       int[] salesOnDay = new int[daysAhead];
       for(int i = 0; i < salesOnDay.length; i++) {
          salesOnDay[i] = initialSales[i];
       }
 
       HashMap<Integer,HashMap<Integer, Double>> profitMemoizeMap = new HashMap<Integer, HashMap<Integer, Double>>(daysAhead);
 
       double currProfit;
       double bestProfit = findProfitForDays(preDaySales,salesOnDay,bidLists,budgetLists,allPredictionsMap,profitMemoizeMap);
       do {
          currProfit = bestProfit;
          int bestIdx = -1;
          int bestIncrement = 0;
          for(int i = 0; i < salesOnDay.length; i++) {
             for(int j = 0; j < 2; j++) {
                if(!(j == 1 && salesOnDay[i] < capacityIncrement)) { //capacity cannot be negative
                   int increment = capacityIncrement * (j == 0 ? 1 : -1);
                   salesOnDay[i] += increment;
 
                   double profit = findProfitForDays(preDaySales,salesOnDay,bidLists,budgetLists,allPredictionsMap,profitMemoizeMap);
                   if(profit > bestProfit) {
                      bestProfit = profit;
                      bestIdx = i;
                      bestIncrement = increment;
                   }
 
                   salesOnDay[i] -= increment;
                }
             }
          }
 
          if(bestIdx > -1) {
             salesOnDay[bestIdx] += bestIncrement;
          }
       }
       while(bestProfit > currProfit);
 
 //      System.out.println("Choosing plan for day " + _day + " : " + Arrays.toString(salesOnDay));
 
       return fillKnapsack(getIncItemsForOverCapLevel(startRemCap,salesOnDay[0],bidLists,budgetLists,allPredictionsMap),salesOnDay[0]);
    }
 
 
 
 
 
 
    private HashMap<Query,Item> fillKnapsackDPHill(HashMap<Query,ArrayList<Double>> bidLists, HashMap<Query,ArrayList<Double>> budgetLists, HashMap<Query,ArrayList<Predictions>> allPredictionsMap){
 
       //-------------------------
       //CONFIG FOR DP
       //-------------------------
       int PLANNING_HORIZON = 59;
       int capacityWindow = _capWindow-1; //Excluding the current day
       int totalCapacityMax = 2*_capacity; //The most capacity we'll ever consider using (across all days)
       int dailyCapacityUsedMin = 0;
       int dailyCapacityUsedMax = totalCapacityMax; //The most capacity we'll ever consider using on a single day
       int dailyCapacityUsedStep = 50;
       int dayStart = (int) _day;
       int dayEnd = Math.min(59, dayStart + PLANNING_HORIZON); //FIXME: _numDays starts out as 0???
 
       //-------------------------
       //Get Pre-day sales
       //-------------------------
       int[] preDaySales = getPreDaySales();
 
 
       //-------------------------
       //Create list of single-day profits for any given (startCapacity, salesForToday) pairs
       //-------------------------
       HashMap<Integer,HashMap<Integer, Double>> profitMemoizeMap = getSpeedyHashedProfits(capacityWindow,
                                                                                           totalCapacityMax,dailyCapacityUsedMin,dailyCapacityUsedMax,dailyCapacityUsedStep,
                                                                                           bidLists,budgetLists,allPredictionsMap);
 
       //-------------------------
       //Create the multiday DP, and solve to get the number of conversions we want for the current day
       //-------------------------
       DPMultiday dp = new DPMultiday(capacityWindow, totalCapacityMax, dailyCapacityUsedMin, dailyCapacityUsedMax, dailyCapacityUsedStep, dayStart, dayEnd, preDaySales.clone(), _capacity);
       dp.profitGivenCapacities = profitMemoizeMap;
       int[] salesPerDay = dp.solveAllDays();
 
       System.out.println("DP result for day " + _day + " : " + Arrays.toString(salesPerDay));
 
       return fillKnapsackHillClimbing(bidLists, budgetLists, allPredictionsMap, salesPerDay);
    }
 
 
    private HashMap<Query,Item> fillKnapsackDP(HashMap<Query,ArrayList<Double>> bidLists, HashMap<Query,ArrayList<Double>> budgetLists, HashMap<Query,ArrayList<Predictions>> allPredictionsMap){
       System.out.println("Running DP.");
 
       //CONFIG FOR DP
       int PLANNING_HORIZON = 58;
       int capacityWindow = _capWindow-1; //Excluding the current day
       int totalCapacityMax = 2*_capacity; //The most capacity we'll ever consider using (across all days)
       int dailyCapacityUsedMin = 0;
       int dailyCapacityUsedMax = totalCapacityMax; //The most capacity we'll ever consider using on a single day
       int dailyCapacityUsedStep = 50;
       int dayStart = (int) _day;
       int dayEnd = Math.min(58, dayStart + PLANNING_HORIZON); //FIXME: _numDays starts out as 0???
 
 
       int[] preDaySales = getPreDaySales();
 
 
       //-------------------------
       //Create list of single-day profits for any given (startCapacity, salesForToday) pairs
       //-------------------------
       long startTimerMap = System.currentTimeMillis();
 
 //	   HashMap<Integer,HashMap<Integer, Double>> profitMemoizeMap = getHashedProfits(capacityWindow,
 //			   totalCapacityMax,dailyCapacityUsedMin,dailyCapacityUsedMax,dailyCapacityUsedStep,
 //			   bidLists,budgetLists,allPredictionsMap);
 
       HashMap<Integer,HashMap<Integer, Double>> profitMemoizeMap = getSpeedyHashedProfits(capacityWindow,
                                                                                           totalCapacityMax,dailyCapacityUsedMin,dailyCapacityUsedMax,dailyCapacityUsedStep,
                                                                                           bidLists,budgetLists,allPredictionsMap);
 
 
       long endTimerMap = System.currentTimeMillis();
 
 
 //	   //--------------------
 //	   //Print out daily profit data
 //	   StringBuffer sb = new StringBuffer();
 //	   sb.append("\t");
 //		for (int salesForToday=0; salesForToday<=dailyCapacityUsedMax && salesForToday<=totalCapacityMax; salesForToday+=dailyCapacityUsedStep) {
 //			sb.append(salesForToday + "\t");
 //		}
 //		sb.append("\n");
 //	   for (int dayStartSales=dailyCapacityUsedMin; dayStartSales<= maxStartingSales; dayStartSales+=dailyCapacityUsedStep) {
 //		   sb.append(dayStartSales + "\t");
 //		   for (int salesForToday=0; salesForToday<=dailyCapacityUsedMax && dayStartSales+salesForToday<=totalCapacityMax; salesForToday+=dailyCapacityUsedStep) {
 ////		   for (int salesForToday=0; salesForToday<=dailyCapacityUsedMax; salesForToday+=dailyCapacityUsedStep) {
 //		   		double profit = profitMemoizeMap.get(dayStartSales).get(salesForToday);
 //				sb.append(profit + "\t");
 //			}
 //			sb.append("\n");
 //	   }
 //	   System.out.println("Profit cache for day " + _day + ":\n" + sb);
 //	   //--------------------
 
 //	   //-------------------
 //	   //Save daily profit data to log
 //	   StringBuffer sb1 = new StringBuffer();
 //	   for (int dayStartSales=dailyCapacityUsedMin; dayStartSales<= maxStartingSales; dayStartSales+=dailyCapacityUsedStep) {
 //		   for (int salesForToday=0; salesForToday<=dailyCapacityUsedMax && dayStartSales+salesForToday<=totalCapacityMax; salesForToday+=dailyCapacityUsedStep) {
 //		   		double profit = profitMemoizeMap.get(dayStartSales).get(salesForToday);
 //		   		sb1.append("MODEL\t" + _day + "\t" + dayStartSales + "\t" + salesForToday + "\t" + profit + "\n");
 //		   }
 //	   }
 //	   System.out.println(sb1);
 //	   //------------------
 
 
       //-------------------------
       //Create the multiday DP, and solve to get the number of conversions we want for the current day
       //-------------------------
       long startTimerDP = System.currentTimeMillis();
       DPMultiday dp = new DPMultiday(capacityWindow, totalCapacityMax, dailyCapacityUsedMin, dailyCapacityUsedMax, dailyCapacityUsedStep, dayStart, dayEnd, preDaySales.clone(), _capacity);
       dp.profitGivenCapacities = profitMemoizeMap;
       double salesForToday = dp.solve(); //TODO: Call the DP to get this value
       long endTimerDP = System.currentTimeMillis();
 
 
       System.out.println("MapTime=" + (endTimerMap-startTimerMap)/1000.0 + ", DPTime=" + (endTimerDP-startTimerDP)/1000.0 ) ;
 
 
       //-------------------------
       //Get the MCKP solution for this number of targeted conversions
       //-------------------------
 
       //Get today's remaining capacity
       int startRemCap = (int)(_capacity*_capMod.get(_capacity));
       for(int i = 0; i < preDaySales.length; i++) {
          startRemCap -= preDaySales[i];
       }
 
       //Get the actual MCKP solution, given today's remaining capacity
       return fillKnapsack(getIncItemsForOverCapLevel(startRemCap,salesForToday,bidLists,budgetLists,allPredictionsMap),salesForToday);
 
    }
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
    private int[] getPreDaySales() {
       //-------------------------
       //Get our current conversion history (amount of conversions on past days within the window)
       //-------------------------
       int[] preDaySales = new int[_capWindow-1];
       if(!hasPerfectModels()) {
          ArrayList<Integer> soldArrayTMP = ((BasicUnitsSoldModel) _unitsSold).getSalesArray();
          ArrayList<Integer> soldArray = new ArrayList<Integer>(soldArrayTMP);
 
          Integer expectedConvsYesterday = ((BasicUnitsSoldModel) _unitsSold).getExpectedConvsTomorrow();
          soldArray.add(expectedConvsYesterday);
 
          for(int i = 0; i < (_capWindow-1); i++) {
             int idx = soldArray.size()-1-i;
             if(idx >= 0) {
                preDaySales[_capWindow-2-i] = soldArray.get(idx);
             }
             else {
                preDaySales[_capWindow-2-i] = (int)(_capacity / ((double) _capWindow));
             }
          }
       }
       else {
          for(int i = 0; i < (_capWindow-1); i++) {
             int idx = _perfectStartSales.length-1-i;
             if(idx >= 0) {
                preDaySales[_capWindow-2-i] = _perfectStartSales[idx];
             }
             else {
                preDaySales[_capWindow-2-i] = (int)(_capacity / ((double) _capWindow));
             }
          }
       }
       //System.out.println("preDaySales=" + Arrays.toString(preDaySales));
       return preDaySales;
    }
 
    private HashMap<Integer, HashMap<Integer, Double>> getHashedProfits(int capacityWindow,
                                                                        int totalCapacityMax, int dailyCapacityUsedMin, int dailyCapacityUsedMax, int dailyCapacityUsedStep,
                                                                        HashMap<Query,ArrayList<Double>> bidLists, HashMap<Query,ArrayList<Double>> budgetLists, HashMap<Query,ArrayList<Predictions>> allPredictionsMap) {
 
 
       HashMap<Integer,HashMap<Integer, Double>> profitMemoizeMap = new HashMap<Integer, HashMap<Integer, Double>>(); //TODO: Take size as a parameter
 
       //We put a bound on the action space (e.g. consider selling 1000 if your startCapacity is 0, but not if it's 1000).
       int maxStartingSales = Math.min(dailyCapacityUsedMax*capacityWindow, totalCapacityMax);
       for (int dayStartSales=dailyCapacityUsedMin; dayStartSales<= maxStartingSales; dayStartSales+=dailyCapacityUsedStep) {
          for (int salesForToday=0; salesForToday<=dailyCapacityUsedMax && dayStartSales+salesForToday<=totalCapacityMax; salesForToday+=dailyCapacityUsedStep) {
             //for (int salesForToday=0; salesForToday<=dailyCapacityUsedMax; salesForToday+=dailyCapacityUsedStep) {
 
             //FIXME!!!!!
             //I think the getIncItems is expecting the first param to be free capacity, not total amount of capacity used
             double remainingCapacity = (_capacity - dayStartSales);
             //Get solution for this (startCapacity, salesOnDay)
             HashMap<Query, Item> solution = fillKnapsack(getIncItemsForOverCapLevel(remainingCapacity,salesForToday,bidLists,budgetLists,allPredictionsMap),salesForToday);
             double profit = 0.0;
             for(Query q : solution.keySet()) {
                profit += solution.get(q).v();
             }
 
             //Add solution's profit to the cache
             if(profitMemoizeMap.get(dayStartSales) == null) {
                HashMap<Integer,Double> profitMap = new HashMap<Integer, Double>();
                profitMap.put(salesForToday,profit);
                profitMemoizeMap.put(dayStartSales,profitMap);
             }
             else {
                profitMemoizeMap.get(dayStartSales).put(salesForToday,profit);
             }
          }
       }
 
       return profitMemoizeMap;
    }
 
 
 
    private HashMap<Integer, HashMap<Integer, Double>> getSpeedyHashedProfits(int capacityWindow,
                                                                              int totalCapacityMax, int dailyCapacityUsedMin, int dailyCapacityUsedMax, int dailyCapacityUsedStep,
                                                                              HashMap<Query,ArrayList<Double>> bidLists, HashMap<Query,ArrayList<Double>> budgetLists, HashMap<Query,ArrayList<Predictions>> allPredictionsMap) {
 
 
       //SPEEDUPS:
       //1. Don't need to compute multiple solutions that are all under capacity. If you start with X, sell Y, and remain under capacity, have the map contain (0,Y)-->profit
       //2. Values for the KP can be gotten incrementally
 
 
       HashMap<Integer,HashMap<Integer, Double>> profitMemoizeMap = new HashMap<Integer, HashMap<Integer, Double>>(); //TODO: Take size as a parameter
 
       //---------------------
       //First get profits when we end under capacity
       //---------------------
       for (int salesForToday=dailyCapacityUsedMin; salesForToday<=_capacity; salesForToday+=dailyCapacityUsedStep) {
          double remainingCapacity = (_capacity - salesForToday);
          HashMap<Query, Item> solution = fillKnapsack(getIncItemsForOverCapLevel(remainingCapacity,salesForToday,bidLists,budgetLists,allPredictionsMap),salesForToday);
          double profit = 0.0;
          for(Query q : solution.keySet()) {
             profit += solution.get(q).v();
          }
          if(profitMemoizeMap.get(dailyCapacityUsedMin) == null) {
             HashMap<Integer,Double> profitMap = new HashMap<Integer, Double>();
             profitMap.put(salesForToday,profit);
             profitMemoizeMap.put(dailyCapacityUsedMin,profitMap);
          }
          else {
             profitMemoizeMap.get(dailyCapacityUsedMin).put(salesForToday,profit);
          }
       }
 
       //---------------------
       //Get profits when we end over capacity.
       //---------------------
       //We put a bound on the action space (e.g. consider selling 1000 if your startCapacity is 0, but not if it's 1000).
       int maxStartingSales = Math.min(dailyCapacityUsedMax*capacityWindow, totalCapacityMax);
       for (int dayStartSales=dailyCapacityUsedMin; dayStartSales<= maxStartingSales; dayStartSales+=dailyCapacityUsedStep) {
          for (int salesForToday=_capacity-dayStartSales+dailyCapacityUsedStep; salesForToday<=dailyCapacityUsedMax && dayStartSales+salesForToday<=totalCapacityMax; salesForToday+=dailyCapacityUsedStep) {
             //If the result is under capacity, only add to the Map if startSales=dailyCapacityUsedMin
             //  (since any other startSales resulting under capacity will give the same value)
 
             double remainingCapacity = (_capacity - dayStartSales);
             //Get solution for this (startCapacity, salesOnDay)
             HashMap<Query, Item> solution = fillKnapsack(getIncItemsForOverCapLevel(remainingCapacity,salesForToday,bidLists,budgetLists,allPredictionsMap),salesForToday);
             double profit = 0.0;
             for(Query q : solution.keySet()) {
                profit += solution.get(q).v();
             }
 
             //Add solution's profit to the cache
             if(profitMemoizeMap.get(dayStartSales) == null) {
                HashMap<Integer,Double> profitMap = new HashMap<Integer, Double>();
                profitMap.put(salesForToday,profit);
                profitMemoizeMap.put(dayStartSales,profitMap);
             }
             else {
                profitMemoizeMap.get(dayStartSales).put(salesForToday,profit);
             }
          }
       }
 
       return profitMemoizeMap;
    }
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
    private double findProfitForDays(int[] preDaySales, int[] salesOnDay, HashMap<Query,ArrayList<Double>> bidLists, HashMap<Query,ArrayList<Double>> budgetLists, HashMap<Query,ArrayList<Predictions>> allPredictionsMap, HashMap<Integer,HashMap<Integer, Double>> profitMemoizeMap) {
       double totalProfit = 0.0;
       for(int i = 0; i < salesOnDay.length; i++) {
          int dayStartSales = _capacity;
          for(int j = 1; j <= (_capWindow-1); j++) {
             int idx = i-j;
             if(idx >= 0) {
                dayStartSales -= salesOnDay[idx];
             }
             else {
                dayStartSales -= preDaySales[preDaySales.length+idx];
             }
          }
 
          double profit;
          if(profitMemoizeMap.get(dayStartSales) != null &&
                  profitMemoizeMap.get(dayStartSales).get(salesOnDay[i]) != null) {
             profit = profitMemoizeMap.get(dayStartSales).get(salesOnDay[i]);
          }
          else {
             HashMap<Query, Item> solution = fillKnapsack(getIncItemsForOverCapLevel(dayStartSales,salesOnDay[i],bidLists,budgetLists,allPredictionsMap),salesOnDay[i]);
             profit = 0.0;
             for(Query q : solution.keySet()) {
                profit += solution.get(q).v();
             }
 
             if(profitMemoizeMap.get(dayStartSales) == null) {
                HashMap<Integer,Double> profitMap = new HashMap<Integer, Double>(salesOnDay.length);
                profitMap.put(salesOnDay[i],profit);
                profitMemoizeMap.put(dayStartSales,profitMap);
             }
             else {
                profitMemoizeMap.get(dayStartSales).put(salesOnDay[i],profit);
             }
          }
 
          totalProfit += profit;
       }
       return totalProfit;
    }
 
    private ArrayList<IncItem> getIncItemsForOverCapLevel(double remainingCap, double desiredSales, HashMap<Query,ArrayList<Double>> bidLists, HashMap<Query,ArrayList<Double>> budgetLists, HashMap<Query, ArrayList<Predictions>> allPredictionsMap) {
       ArrayList<IncItem> allIncItems = new ArrayList<IncItem>();
       double penalty = getPenalty(remainingCap, desiredSales);
       for (Query q : _querySpace) {
          if(q != new Query()) {
             ArrayList<Item> itemList = new ArrayList<Item>();
             debug("Query: " + q);
             ArrayList<Predictions> queryPredictions = allPredictionsMap.get(q);
             int itemCount = 0;
             for(int k = 1; k < 2; k++) {
                for(int i = 0; i < bidLists.get(q).size(); i++) {
                   for(int j = 0; j < budgetLists.get(q).size(); j++) {
                      boolean targeting = (k != 0);
                      double bid = bidLists.get(q).get(i);
                      double budget = budgetLists.get(q).get(j);
                      Predictions predictions = queryPredictions.get(itemCount);
                      double salesPrice = _salesPrices.get(q);
                      double clickPr = predictions.getClickPr();
                      double numImps = predictions.getNumImp();
                      int numClicks = (int) (clickPr * numImps);
                      double CPC = predictions.getCPC();
                      double cost = numClicks*CPC;
                      double convProb = getConversionPrWithPenalty(q, penalty,predictions.getISRatio());
                      double w = numClicks * convProb;            //weight = numClciks * convProv
                      double v = w * salesPrice - cost;   //value = revenue - cost	[profit]
                      itemList.add(new Item(q,w,v,bid,budget,targeting,0,itemCount));
                      itemCount++;
 
                      if(cost + bid*2 < budget) {
                         //If we don't hit our budget, we do not need to consider
                         //higher budgets, since we will have the same result
                         //so we break out of the budget loop
                         break;
                      }
                   }
                }
             }
 
             if(itemList.size() > 0) {
                debug("Items for " + q);
                Item[] items = itemList.toArray(new Item[0]);
                IncItem[] iItems = getIncremental(items);
                allIncItems.addAll(Arrays.asList(iItems));
             }
          }
       }
       Collections.sort(allIncItems);
       return allIncItems;
    }
 
    /**
     * Greedily fill the knapsack by selecting incremental items
     *
     * @param incItems
     * @param budget
     * @return
     */
    private HashMap<Query, Item> fillKnapsack(ArrayList<IncItem> incItems, double budget) {
       if (budget <= 0) {
          return new HashMap<Query, Item>();
       }
       HashMap<Query, Item> solution = new HashMap<Query, Item>();
       for (IncItem ii : incItems) {
          if (budget >= ii.w()) {
             solution.put(ii.item().q(), ii.item());
             budget -= ii.w();
          }
          else if(budget > 0) {
             Item itemHigh = ii.itemHigh();
             double incW = ii.w();
             double weightHigh = budget / incW;
             double weightLow = 1.0 - weightHigh;
             double lowVal = ((ii.itemLow() == null) ? 0.0 : ii.itemLow().v());
             double lowW = ((ii.itemLow() == null) ? 0.0 : ii.itemLow().w());
             double newValue = itemHigh.v()*weightHigh + lowVal*weightLow;
             solution.put(ii.item().q(), new Item(ii.item().q(),budget+lowW,newValue,itemHigh.b(),itemHigh.budget(),itemHigh.targ(),itemHigh.isID(),itemHigh.idx()));
             break;
          }
          else {
             break;
          }
       }
       return solution;
    }
 
    /**
     * Get undominated items
     * @param items
     * @return
     */
    public static Item[] getUndominated(Item[] items) {
       Arrays.sort(items,new ItemComparatorByWeight());
       //remove dominated items (higher weight, lower value)
       ArrayList<Item> temp = new ArrayList<Item>();
       temp.add(items[0]);
       for(int i=1; i<items.length; i++) {
          Item lastUndominated = temp.get(temp.size()-1);
          if(lastUndominated.v() < items[i].v()) {
             temp.add(items[i]);
          }
       }
 
 
       ArrayList<Item> betterTemp = new ArrayList<Item>();
       betterTemp.addAll(temp);
       for(int i = 0; i < temp.size(); i++) {
          ArrayList<Item> duplicates = new ArrayList<Item>();
          Item item = temp.get(i);
          duplicates.add(item);
          for(int j = i + 1; j < temp.size(); j++) {
             Item otherItem = temp.get(j);
             if(item.v() == otherItem.v() && item.w() == otherItem.w()) {
                duplicates.add(otherItem);
             }
          }
          if(duplicates.size() > 1) {
             betterTemp.removeAll(duplicates);
             double minBid = 10;
             double maxBid = -10;
             for(int j = 0; j < duplicates.size(); j++) {
                double bid = duplicates.get(j).b();
                if(bid > maxBid) {
                   maxBid = bid;
                }
                if(bid < minBid) {
                   minBid = bid;
                }
             }
             Item newItem = new Item(item.q(), item.w(), item.v(), (maxBid+minBid)/2.0, item.targ(), item.isID(),item.idx());
             betterTemp.add(newItem);
          }
       }
 
       //items now contain only undominated items
       items = betterTemp.toArray(new Item[0]);
       Arrays.sort(items,new ItemComparatorByWeight());
 
       //remove lp-dominated items
       ArrayList<Item> q = new ArrayList<Item>();
       q.add(new Item(new Query(),0,0,-1,false,1,0));//add item with zero weight and value
 
       for(int i=0; i<items.length; i++) {
          q.add(items[i]);//has at least 2 items now
          int l = q.size()-1;
          Item li = q.get(l);//last item
          Item nli = q.get(l-1);//next to last
          if(li.w() == nli.w()) {
             if(li.v() > nli.v()) {
                q.remove(l-1);
             }else{
                q.remove(l);
             }
          }
          l = q.size()-1; //reset in case an item was removed
          //while there are at least three elements and ...
          while(l > 1 && (q.get(l-1).v() - q.get(l-2).v())/(q.get(l-1).w() - q.get(l-2).w())
                  <= (q.get(l).v() - q.get(l-1).v())/(q.get(l).w() - q.get(l-1).w())) {
             q.remove(l-1);
             l--;
          }
       }
 
       //remove the (0,0) item
       if(q.get(0).w() == 0 && q.get(0).v() == 0) {
          q.remove(0);
       }
 
       Item[] uItems = q.toArray(new Item[0]);
       return uItems;
    }
 
 
    /**
     * Get incremental items
     * @param items
     * @return
     */
    public IncItem[] getIncremental(Item[] items) {
       for(int i = 0; i < items.length; i++) {
          debug("\t" + items[i]);
       }
 
       Item[] uItems = getUndominated(items);
 
       debug("UNDOMINATED");
       for(int i = 0; i < uItems.length; i++) {
          debug("\t" + uItems[i]);
       }
 
       IncItem[] ii = new IncItem[uItems.length];
 
       if (uItems.length != 0){ //getUndominated can return an empty array
          ii[0] = new IncItem(uItems[0].w(), uItems[0].v(), uItems[0], null);
          for(int item=1; item<uItems.length; item++) {
             Item prev = uItems[item-1];
             Item cur = uItems[item];
             ii[item] = new IncItem(cur.w() - prev.w(), cur.v() - prev.v(), cur, prev);
          }
       }
       debug("INCREMENTAL");
       for(int i = 0; i < ii.length; i++) {
          debug("\t" + ii[i]);
       }
       return ii;
    }
 
    public double getConversionPrWithPenalty(Query q, double penalty) {
       double convPr;
       String component = q.getComponent();
       double baseConvPr = _baseConvProbs.get(q);
       if(_compSpecialty.equals(component)) {
          convPr = eta(baseConvPr*penalty,1+_CSB);
       }
       else if(component == null) {
          convPr = eta(baseConvPr*penalty,1+_CSB) * (1/3.0) + baseConvPr*penalty*(2/3.0);
       }
       else {
          convPr = baseConvPr*penalty;
       }
       convPr *= (1.0 - _ISRatioModel.getISRatio(q)[0]);
       return convPr;
    }
 
    public double getConversionPrWithPenalty(Query q, double penalty, double[] slotDistr) {
       double convPr;
       String component = q.getComponent();
       double baseConvPr = _baseConvProbs.get(q);
       if(_compSpecialty.equals(component)) {
          convPr = eta(baseConvPr*penalty,1+_CSB);
       }
       else if(component == null) {
          convPr = eta(baseConvPr*penalty,1+_CSB) * (1/3.0) + baseConvPr*penalty*(2/3.0);
       }
       else {
          convPr = baseConvPr*penalty;
       }
       double[] ISRatioArr = _ISRatioModel.getISRatio(q);
       double ISRatio = 0;
       for(int i = 0; i < slotDistr.length; i++) {
          ISRatio += ISRatioArr[i]*slotDistr[i];
       }
       convPr *= (1.0 - ISRatio);
       return convPr;
    }
 
    public double getConversionPrWithPenalty(Query q, double penalty, double ISRatio) {
       double convPr;
       String component = q.getComponent();
       double baseConvPr = _baseConvProbs.get(q);
       if(_compSpecialty.equals(component)) {
          convPr = eta(baseConvPr*penalty,1+_CSB);
       }
       else if(component == null) {
          //If query has no component, searchers who click will have some chance (1/3) of having our component specialty.
          convPr = eta(baseConvPr*penalty,1+_CSB) * (1/3.0) + baseConvPr*penalty*(2/3.0);
       }
       else {
          convPr = baseConvPr*penalty;
       }
 
       //We just computed the conversion probability for someone, assuming they're not an IS user.
       //If an IS user, conversion probability is 0.
       // conversionProb = (PrIS) * 0 + (1 - PrIS) * conversionProbGivenNonIS 
       convPr *= (1.0 - ISRatio);
       return convPr;
    }
 
    // returns the corresponding index for the targeting part of fTargetfPro
    private int getFTargetIndex(boolean targeted, Product p, Product target) {
       if (!targeted || p == null || target == null) {
          return 0; //untargeted
       }
       else if(p.equals(target)) {
          return 1; //targeted correctly
       }
       else {
          return 2; //targeted incorrectly
       }
    }
 
    private double randDouble(double a, double b) {
       double rand = _R.nextDouble();
       return rand * (b - a) + a;
    }
 
    public void debug(Object str) {
       if(DEBUG) {
          System.out.println(str);
       }
    }
 
    @Override
    public String toString() {
       return "MCKP";
    }
 
    @Override
    public AbstractAgent getCopy() {
       return new MCKP(_c[0],_c[1],_c[2]);
    }
 }
