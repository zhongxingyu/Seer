 package agents;
 
 /*
  * Commented by Max. All comments made by cjc are marked with "// (cjc)"
  * 
  * Simplifying assumptions:
  * 
  * - CPC is close to bid
  * - conversion probability is close to what it would be with no informational searchers
  * - the 2 assumptions above sort of cancel each other out
  * 
  * 
  * 
  * Constants:
  * 
  * - reinvestment caps
  * 
  * 
  * 
  * Models:
  * 
  * - ConversionPrModel;
  * - ConversionPrModelNoIS;
  * - UnitsSoldModel; (also in: DistributionCap.java)
  * - UnitsSoldModelMean;
  * 
  */
 
 import java.util.Hashtable;
 import java.util.Set;
 
 import modelers.conversionprob.ConversionPrModel;
 import modelers.conversionprob.ConversionPrModelNoIS;
 import modelers.unitssold.UnitsSoldModel;
 import modelers.unitssold.UnitsSoldModelMean;
 
 import agents.rules.AdjustConversionPr;
 import agents.rules.ConversionPr;
 import agents.rules.DistributionCap;
 import agents.rules.ManufacurerBonus;
 import agents.rules.NoImpressions;
 import agents.rules.ReinvestmentCap;
 import agents.rules.Targeted;
 import agents.rules.TopPosition;
 import agents.rules.Walking;
 import edu.umich.eecs.tac.props.BidBundle;
 import edu.umich.eecs.tac.props.Query;
 import edu.umich.eecs.tac.props.QueryReport;
 import edu.umich.eecs.tac.props.QueryType;
 import edu.umich.eecs.tac.props.SalesReport;
 
 public class SSBAgent extends AbstractAgent {
 	protected SSBBidStrategy _bidStrategy;
 	
 	protected DistributionCap _distributionCap;
 	protected ReinvestmentCap _reinvestmentCap0;
 	protected ReinvestmentCap _reinvestmentCap1;
 	protected ReinvestmentCap _reinvestmentCap1s;
 	protected ReinvestmentCap _reinvestmentCap2;
 	protected ReinvestmentCap _reinvestmentCap2s;
 	protected TopPosition _topPosition;
 	protected Walking _walking;
 	protected NoImpressions _noImpressions;
 	protected AdjustConversionPr _adjustConversionPr;
 	
 	protected Hashtable<Query,Double> _baseLineConversion;
 	
 	Set<Query> _F1componentSpecialty;
 	Set<Query> _F1notComponentSpecialty;
 	Set<Query> _F2componentSpecialty;
 	Set<Query> _F2notComponentSpecialty;
 	
 	protected UnitsSoldModel _unitsSold;
 	protected ConversionPrModel _conversionPr;
 	
 	
 	public SSBAgent(){}
 	
 	@Override
 	protected void simulationSetup() {}
 
 	@Override
 	protected void initBidder() {
 		printAdvertiserInfo();
 		_baseLineConversion = new Hashtable<Query,Double>();
 		_bidStrategy = new SSBBidStrategy(_querySpace);
 		int distributionCapacity = _advertiserInfo.getDistributionCapacity(); // max capacity
 		int distributionWindow = _advertiserInfo.getDistributionWindow(); // number of days 
 		double manufacturerBonus = _advertiserInfo.getManufacturerBonus();
 		String manufacturerSpecialty = _advertiserInfo.getManufacturerSpecialty();
 		double componentSpecialtyBonus = _advertiserInfo.getComponentBonus();
 		
 		//_unitsSold = new UnitsSoldModelMaxWindow(distributionWindow); // (cjc)
 		_unitsSold = new UnitsSoldModelMean(distributionWindow); // model calculates total sold over last 4 days
 		
 		_distributionCap = new DistributionCap(distributionCapacity, _unitsSold, 8); // 8 is the _magicDivisor...
 		_topPosition = new TopPosition(_advertiserInfo.getAdvertiserId(), 0.10); // 0.10 is _decrease
 		_walking = new Walking(_advertiserInfo.getAdvertiserId()); // don't know what this is
 		_noImpressions = new NoImpressions(_advertiserInfo.getAdvertiserId(), 0.05); // don't know what this is
 		_reinvestmentCap0 = new ReinvestmentCap(0.90); //constant to be entered by user
 		_reinvestmentCap1 = new ReinvestmentCap(0.90); //constant to be entered by user
 		_reinvestmentCap1s = new ReinvestmentCap(0.90); //constant to be entered by user
 		_reinvestmentCap2 = new ReinvestmentCap(0.90); //constant to be entered by user
 		_reinvestmentCap2s = new ReinvestmentCap(0.90); //constant to be entered by user
 		
 		
 		for(Query q : _queryFocus.get(QueryType.FOCUS_LEVEL_ZERO)) {_baseLineConversion.put(q, 0.1);} // constant set by game server info
 		for(Query q : _queryFocus.get(QueryType.FOCUS_LEVEL_ONE)) {_baseLineConversion.put(q, 0.2);} // constant set by game server info
 		for(Query q : _queryFocus.get(QueryType.FOCUS_LEVEL_TWO)) {_baseLineConversion.put(q, 0.3);} // constant set by game server info
 		Set<Query> componentSpecialty = _queryComponent.get(_advertiserInfo.getComponentSpecialty());
 		
 		_conversionPr = new ConversionPrModelNoIS(distributionCapacity, _unitsSold, _baseLineConversion, componentSpecialty, componentSpecialtyBonus); // model used
 		
 		_adjustConversionPr = new AdjustConversionPr(_conversionPr);
 		
 		new ConversionPr(0.10).apply(_queryFocus.get(QueryType.FOCUS_LEVEL_ZERO), _bidStrategy); // constant set by game server info
 		new ConversionPr(0.20).apply(_queryFocus.get(QueryType.FOCUS_LEVEL_ONE), _bidStrategy); // constant set by game server info
 		new ConversionPr(0.30).apply(_queryFocus.get(QueryType.FOCUS_LEVEL_TWO), _bidStrategy); // constant set by game server info
 		
 		new ManufacurerBonus(manufacturerBonus).apply(_queryManufacturer.get(manufacturerSpecialty), _bidStrategy);
 		
 		_F1componentSpecialty = intersect(_queryFocus.get(QueryType.FOCUS_LEVEL_ONE), componentSpecialty);
 		_F1notComponentSpecialty = subtract(_queryFocus.get(QueryType.FOCUS_LEVEL_ONE),_F1componentSpecialty);
 		new ConversionPr(0.27).apply(_F1componentSpecialty, _bidStrategy); // constant set by game server info
 		//for(Query q : F1componentSpecialty) {_baseLineConversion.put(q, 0.27);} // (cjc)
 		
 		_F2componentSpecialty = intersect(_queryFocus.get(QueryType.FOCUS_LEVEL_TWO), componentSpecialty);
 		_F2notComponentSpecialty = subtract(_queryFocus.get(QueryType.FOCUS_LEVEL_TWO),_F2componentSpecialty);
 		new ConversionPr(0.39).apply(_F2componentSpecialty, _bidStrategy); // constant set by game server info
 		//for(Query q : F2componentSpecialty) {_baseLineConversion.put(q, 0.39);} // (cjc)
 		
 		
 		//??? new Targeted().apply(F1componentSpecialty, _bidStrategy); // (cjc)
 		new Targeted().apply(_F2componentSpecialty, _bidStrategy);
 	}
 	
 
 	
 	
 	@Override
 	protected void updateBidStrategy() {
 		QueryReport qr = _queryReports.remove();
 		_topPosition.updateReport(qr);
 		_walking.updateReport(qr);
 		_noImpressions.updateReport(qr);
 		
 		SalesReport sr = _salesReports.remove();
 		_unitsSold.updateReport(sr);
 
 		_adjustConversionPr.apply(_bidStrategy);
 		
 		//_topPosition.apply(_bidStrategy); // (cjc)
 		_noImpressions.apply(_bidStrategy);
 		_walking.apply(_bidStrategy);
 		
 		_reinvestmentCap0.apply(_queryFocus.get(QueryType.FOCUS_LEVEL_ZERO), _bidStrategy);
 		_reinvestmentCap1.apply(_F1notComponentSpecialty, _bidStrategy);
 		_reinvestmentCap1s.apply(_F1componentSpecialty, _bidStrategy);
 		_reinvestmentCap2.apply(_F2notComponentSpecialty, _bidStrategy); 
 		_reinvestmentCap2s.apply(_F2componentSpecialty, _bidStrategy);
 		
		_distributionCap.apply(_bidStrategy);
		
 	}
 	
 	
 	@Override
 	protected BidBundle buildBidBudle(){
 		System.out.println("**********");
 		System.out.println(_bidStrategy);
 		System.out.println("**********");
 		return _bidStrategy.buildBidBundle();
 	}
 
 	
 }
