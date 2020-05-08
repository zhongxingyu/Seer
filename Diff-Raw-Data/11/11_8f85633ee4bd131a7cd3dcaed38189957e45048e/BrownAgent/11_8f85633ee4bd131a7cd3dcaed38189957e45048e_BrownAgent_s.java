 package brown.tac.adx.agents;
 
 
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.Map;
 import java.util.Queue;
 import java.util.Random;
 import java.util.Set;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import brown.tac.adx.models.Modeler;
 import brown.tac.adx.optimization.Optimizer;
 import brown.tac.adx.predictions.DailyPrediction;
 
 import se.sics.isl.transport.Transportable;
 import se.sics.tasim.aw.Agent;
 import se.sics.tasim.aw.Message;
 import se.sics.tasim.props.SimulationStatus;
 import se.sics.tasim.props.StartInfo;
 import tau.tac.adx.ads.properties.AdType;
 import tau.tac.adx.demand.CampaignStats;
 import tau.tac.adx.devices.Device;
 import tau.tac.adx.props.AdxBidBundle;
 import tau.tac.adx.props.AdxQuery;
 import tau.tac.adx.props.PublisherCatalog;
 import tau.tac.adx.props.PublisherCatalogEntry;
 import tau.tac.adx.report.adn.AdNetworkKey;
 import tau.tac.adx.report.adn.AdNetworkReport;
 import tau.tac.adx.report.adn.AdNetworkReportEntry;
 import tau.tac.adx.report.adn.MarketSegment;
 import tau.tac.adx.report.demand.AdNetBidMessage;
 import tau.tac.adx.report.demand.AdNetworkDailyNotification;
 import tau.tac.adx.report.demand.CampaignOpportunityMessage;
 import tau.tac.adx.report.demand.CampaignReport;
 import tau.tac.adx.report.demand.CampaignReportKey;
 import tau.tac.adx.report.demand.InitialCampaignMessage;
 import tau.tac.adx.report.publisher.AdxPublisherReport;
 import tau.tac.adx.report.publisher.AdxPublisherReportEntry;
 import edu.umich.eecs.tac.props.Ad;
 import edu.umich.eecs.tac.props.BankStatus;
 
 /**
  * 
  * @author Mariano Schain, Ethan Langavin, Betsy Hilliard, Veena Vignale, Amy Greenwald
  * 
  */
 public class BrownAgent extends Agent {
 
 	private final Logger log = Logger.getLogger(BrownAgent.class.getName());
 
 	/*
 	 * Basic simulation information. An agent should receive the {@link
 	 * StartInfo} at the beginning of the game or during recovery.
 	 */
 	@SuppressWarnings("unused")
 	private StartInfo startInfo;
 
 	/**
 	 * Messages received:
 	 * 
 	 * We keep all the {@link CampaignReport campaign reports} 
 	 * delivered to the agent. We also keep the initialization 
 	 * messages {@link PublisherCatalog} and
 	 * {@link InitialCampaignMessage} and the most recent messages and reports
 	 * {@link CampaignOpportunityMessage}, {@link CampaignReport}, and
 	 * {@link AdNetworkDailyNotification}.
 	 */
 	private final Queue<CampaignReport> campaignReports;
 	private PublisherCatalog publisherCatalog;
 	private InitialCampaignMessage initialCampaignMessage;
 	private AdNetworkDailyNotification adNetworkDailyNotification;
 
 	/*
 	 * The addresses of server entities to which the agent should send the daily
 	 * bids data
 	 */
 	private String demandAgentAddress; //this appears to be where bids for UCS and Campaigns are sent?
 	private String adxAgentAddress; //this appears to be where bids for impressions are sent?
 
 	/*
 	 * we maintain a list of queries - each characterized by the web site (the
 	 * publisher), the device type, the ad type, and the user market segment
 	 */
 	private AdxQuery[] queries;
 
 	/**
 	 * Information regarding the latest campaign opportunity announced
 	 */
 	private CampaignData pendingCampaign;
 
 	/**
 	 * We maintain a collection (mapped by the campaign id) of the campaigns won
 	 * by our agent.
 	 */
 	private Map<Integer, CampaignData> myCampaigns;
 
 	/**
 	 * We maintain a collection (mapped by the campaign id) of the campaigns sold
 	 */
 	private Map<Integer, CampaignData> allCampaigns;
 	/*
 	 * the bidBundle to be sent daily to the AdX
 	 */
 	private AdxBidBundle bidBundle;
 
 	/*
 	 * The current bid level for the user classification service
 	 */
 	double ucsBid;
 
 	/*
 	 * The targeted service level for the user classification service
 	 */
 	double ucsTargetLevel;
 	
 	/*
 	 * Container object for models
 	 */
 	Modeler _modeler;
 	
 	/*
 	 * Container Object for optimizers
 	 */
 	Optimizer _optimizer;
 
 	/*
 	 * Data structure to hold the messages received on day d
 	 */
 	LinkedList<DailyInfo> _dailyInfoList;
 	
 	/*
 	 * current day of simulation
 	 */
 	private int day = 0;
 	
 	/*
 	 * Container object for fluent inputs to optimization algo (i.e. campaign to bid on),
 	 * as well as the results of the optimization algo.
 	 */
	private OptimizationMessenger _optMessenger;
 
 	private Random randomGenerator;
 
 	public BrownAgent() {
 		campaignReports = new LinkedList<CampaignReport>();
 		_modeler = new Modeler("");
 		_optimizationMessenger = new OptimizationMessenger();
 		_optimizer = new Optimizer("", _modeler, _optimizationMessenger);
 		_dailyInfoList = new LinkedList<DailyInfo>();
 	}
 
 	@Override
 	protected void messageReceived(Message message) {
 		//System.out.println("DAY: "+day);
 		
 		//if the dailyinfo list has not been updated, update it. 
 		if (_dailyInfoList.size()== day) {
 			_dailyInfoList.add(day, new DailyInfo(day));
 		}
 		
 		try {
 			
 			Transportable content = message.getContent();
 			
 			
 			if(content==null){
 				System.out.println("NULL");
 			}
 			System.out.println(message.getContent().getClass().toString());
 			
 			//TODO: Is there a better way to handle these messages?
 			// Ultimately they need to be passed into a DailyInfo Object that 
 			if (content instanceof InitialCampaignMessage) {
 				System.out.println("init campaign");
 				handleInitialCampaignMessage((InitialCampaignMessage) content);
 			} else if (content instanceof CampaignOpportunityMessage) {
 				System.out.println("campaign opp");
 				//TODO: this is throwing a Null exception
 				handleCampaignOpportunityMessage((CampaignOpportunityMessage) content);
 			} else if (content instanceof CampaignReport) {
 				System.out.println("camp report");
 				handleCampaignReport((CampaignReport) content);
 			} else if (content instanceof AdNetworkDailyNotification) {
 				System.out.println("adnet daily notification");
 				handleAdNetworkDailyNotification((AdNetworkDailyNotification) content);
 			} else if (content instanceof AdxPublisherReport) { //may not be used
 				System.out.println("adx pub report");
 				handleAdxPublisherReport((AdxPublisherReport) content);
 			} else if (content instanceof SimulationStatus) {
 				System.out.println("sim status");
 				handleSimulationStatus((SimulationStatus) content);
 			} else if (content instanceof PublisherCatalog) { 
 				System.out.println("pub catalogue");
 				handlePublisherCatalog((PublisherCatalog) content);
 			} else if (content instanceof AdNetworkReport) { //maybe not used
 				System.out.println("adnet report");
 				handleAdNetworkReport((AdNetworkReport) content);
 			} else if (content instanceof StartInfo) {
 				System.out.println("start info");
 				handleStartInfo((StartInfo) content);
 			} else if (content instanceof BankStatus) {
 				System.out.println("bank status");
 				handleBankStatus((BankStatus) content);
 			}  else {
 				System.out.println("not classified");
 				
 				log.info("UNKNOWN Message Received: " + content);
 			}
 
 		} catch (NullPointerException e) {
 			this.log.log(Level.SEVERE,
 					"Exception thrown while trying to parse message." +e+".  "+ message.toString());
 			return;
 		}
 	}
 
 	private void handleBankStatus(BankStatus content) {
 		log.info("Day " + day + " :" + content.toString());
 	}
 
 	/**
 	 * Processes the start information.
 	 * 
 	 * @param startInfo
 	 *            the start information.
 	 */
 	protected void handleStartInfo(StartInfo startInfo) {
 		this.startInfo = startInfo;
 	}
 
 	/**
 	 * Process the reported set of publishers
 	 * 
 	 * @param publisherCatalog
 	 */
 	private void handlePublisherCatalog(PublisherCatalog publisherCatalog) {
 		this.publisherCatalog = publisherCatalog;
 		generateAdxQuerySpace();
 	}
 
 	/**
 	 * On day 0, a campaign (the "initial campaign") is allocated to each
 	 * competing agent. The campaign starts on day 1. The address of the
 	 * server's AdxAgent (to which bid bundles are sent) and DemandAgent (to
 	 * which bids regarding campaign opportunities may be sent in subsequent
 	 * days) are also reported in the initial campaign message
 	 */
 	private void handleInitialCampaignMessage(
 			InitialCampaignMessage campaignMessage) {
 		log.info(campaignMessage.toString());
 
 		day = 0;
 
 		//set initial message
 		initialCampaignMessage = campaignMessage;
 		
 		//set agent address information
 		demandAgentAddress = campaignMessage.getDemandAgentAddress();
 		adxAgentAddress = campaignMessage.getAdxAgentAddress();
 
 		//we start with an initial campaign, fill in the details
 		CampaignData campaignData = new CampaignData(initialCampaignMessage);
 		
 		//TODO: check that this is in the spec
 		campaignData.setBudget(initialCampaignMessage.getReachImps() / 1000.0);
 
 		/*
 		 * The initial campaign is already allocated to our agent so we add it
 		 * to our allocated-campaigns list.
 		 */
 		log.info("Day " + day + ": Allocated campaign - " + campaignData);
 		myCampaigns.put(initialCampaignMessage.getId(), campaignData);
 		allCampaigns.put(initialCampaignMessage.getId(), campaignData);
 		
 		//here we should add myCampaigns to the daily info 
 	}
 
 	/**
 	 * On day n ( > 0) a campaign opportunity is announced to the competing
 	 * agents. The campaign starts on day n + 2 or later and the agents may send
 	 * (on day n) related bids (attempting to win the campaign). The allocation
 	 * (the winner) is announced to the competing agents during day n + 1.
 	 */
 	private void handleCampaignOpportunityMessage(
 			CampaignOpportunityMessage com) {
 
 		day = com.getDay();
 
 		pendingCampaign = new CampaignData(com);
 		CampaignData campaignData = new CampaignData(com);
 		
 		allCampaigns.put(initialCampaignMessage.getId(), campaignData);
 		log.info("Day " + day + ": Campaign opportunity - " + pendingCampaign);
 
 		/*
 		 * The campaign requires com.getReachImps() impressions. The competing
 		 * Ad Networks bid for the total campaign Budget (that is, the ad
 		 * network that offers the lowest budget gets the campaign allocated).
 		 * The advertiser is willing to pay the AdNetwork at most 1$ CPM,
 		 * therefore the total number of impressions may be treated as a reserve
 		 * (upper bound) price for the auction.
 		 */
 		long cmpBid = 1 + Math.abs((randomGenerator.nextLong())
 				% (com.getReachImps()));
 
 		double cmpBidUnits = cmpBid / 1000.0;
 
 		log.info("Day " + day + ": Campaign total budget bid: " + cmpBidUnits);
 
 		/*
 		 * Adjust ucs bid s.t. target level is achieved. Note: The bid for the
 		 * user classification service is piggybacked
 		 */
 
 		if (adNetworkDailyNotification != null) {
 			double ucsLevel = adNetworkDailyNotification.getServiceLevel();
 			double prevUcsBid = ucsBid;
 
 			/* UCS Bid should not exceed 0.2 */
 			ucsBid = Math.min(0.1 + 0.1*randomGenerator.nextDouble(), prevUcsBid * (1 + ucsTargetLevel - ucsLevel));
 
 			log.info("Day " + day + ": Adjusting ucs bid: was " + prevUcsBid
 					+ " level reported: " + ucsLevel + " target: "
 					+ ucsTargetLevel + " adjusted: " + ucsBid);
 		} else {
 			log.info("Day " + day + ": Initial ucs bid is " + ucsBid);
 		}
 
 		/* Note: Campaign bid is in millis */
 		AdNetBidMessage bids = new AdNetBidMessage(ucsBid, pendingCampaign.id,
 				cmpBid);
 		
 		//Message sent here
 		//TODO: This message should be sent after the optimizer makes decisions
 		sendMessage(demandAgentAddress, bids);
 	}
 
 	/**
 	 * On day n ( > 0), the result of the UserClassificationService and Campaign
 	 * auctions (for which the competing agents sent bids during day n -1) are
 	 * reported. The reported Campaign starts in day n+1 or later and the user
 	 * classification service level is applicable starting from day n+1.
 	 */
 	private void handleAdNetworkDailyNotification(
 			AdNetworkDailyNotification notificationMessage) {
 
 		adNetworkDailyNotification = notificationMessage;
 
 		log.info("Day " + day + ": Daily notification for campaign "
 				+ adNetworkDailyNotification.getCampaignId());
 
 		String campaignAllocatedTo = " allocated to "
 				+ notificationMessage.getWinner();
 
 		if ((pendingCampaign.id == adNetworkDailyNotification.getCampaignId())
 				&& (notificationMessage.getCost() != 0)) {
 
 			/* add campaign to list of won campaigns */
 			pendingCampaign.setBudget(notificationMessage.getCost());
 
 			myCampaigns.put(pendingCampaign.id, pendingCampaign);
 
 			campaignAllocatedTo = " WON at cost "
 					+ notificationMessage.getCost();
 		}
 
 		log.info("Day " + day + ": " + campaignAllocatedTo
 				+ ". UCS Level set to " + notificationMessage.getServiceLevel()
 				+ " at price " + notificationMessage.getPrice()
 				+ " Qualit Score is: " + notificationMessage.getQualityScore());
 	}
 
 	/**
 	 * The SimulationStatus message received on day n indicates that the
 	 * calculation time is up and the agent is requested to send its bid bundle
 	 * to the AdX.
 	 */
 	private void handleSimulationStatus(SimulationStatus simulationStatus) {
 		//removed following lines until implemented... (betsy)
 		//DailyPrediction prediction = _modeler.updateModels(new DailyPrediction(_dailyInfoList.get(day)));
 		//AdxBidBundle bidBundle = _optimizer.makeDecisions(prediction);
 		
 		// Make calls to sendMessage to tell the server about our decisions
 		
 		//just call this for now. But we should just be getting the 
 		// bid bundle from the optimizer
 		updateBids();
 		
 		//this should be here
 		//log.info("Day " + day + ": Sending BidBundle");
 		//sendMessage(adxAgentAddress, bidBundle);
 		
 		log.info("Day " + day + " ended. Starting next day");
 		++day;
 	}
 	
 	/**
 	 * 
 	 */
 	protected void updateBids() {
 
 		bidBundle = new AdxBidBundle();
 		int entrySum = 0;
 		
 		/*
 		 * 
 		 */
 		for (CampaignData campaign : myCampaigns.values()) {
 
 			int dayBiddingFor = day + 1;
 
 			/* A fixed random bid, for all queries of the campaign */
 			/*
 			 * Note: bidding per 1000 imps (CPM) - no more than average budget
 			 * revenue per imp
 			 */
 
 			Random rnd = new Random();
 			double avgCmpRevenuePerImp = campaign.budget / campaign.reachImps;
 			double rbid = 1000.0 * rnd.nextDouble() * avgCmpRevenuePerImp;
 
 			/*
 			 * add bid entries w.r.t. each active campaign with remaining
 			 * contracted impressions.
 			 * 
 			 * for now, a single entry per active campaign is added for queries
 			 * of matching target segment.
 			 */
 
 			if ((dayBiddingFor >= campaign.dayStart)
 					&& (dayBiddingFor <= campaign.dayEnd)
 					&& (campaign.impsTogo() >= 0)) {
 
 				int entCount = 0;
 				for (int i = 0; i < queries.length; i++) {
 
 					Set<MarketSegment> segmentsList = queries[i]
 							.getMarketSegments();
 
 					for (MarketSegment marketSegment : segmentsList) {
 						if (campaign.targetSegments.contains( marketSegment)) {
 							/*
 							 * among matching entries with the same campaign id,
 							 * the AdX randomly chooses an entry according to
 							 * the designated weight. by setting a constant
 							 * weight 1, we create a uniform probability over
 							 * active campaigns
 							 */
 							++entCount;
 							bidBundle.addQuery(queries[i], rbid, new Ad(null),
 									campaign.id, 1);
 						}
 					}
 					
 					if (segmentsList.size() == 0) {
 						++entCount;
 						bidBundle.addQuery(queries[i], rbid, new Ad(null),
 								campaign.id, 1);
 					}
 				}
 				double impressionLimit = 0.5 * campaign.impsTogo();
 				double budgetLimit = 0.5 * Math.max(0, campaign.budget
 						- campaign.stats.getCost());
 				bidBundle.setCampaignDailyLimit(campaign.id,
 						(int) impressionLimit, budgetLimit);
 				entrySum += entCount;
 				log.info("Day " + day + ": Updated " + entCount
 						+ " Bid Bundle entries for Campaign id " + campaign.id);
 			}
 		}
 
 		if (bidBundle != null) {
 			//This is where the bid bundle for bids in the AdExchange for Impressions is sent to 
 			// the AdX server
 			//TODO: We should call this after the optimizer makes decisions
 			log.info("Day " + day + ": Sending BidBundle");
 			sendMessage(adxAgentAddress, bidBundle);
 		}
 	}
 
 	/**
 	 * Campaigns performance w.r.t. each allocated campaign
 	 */
 	private void handleCampaignReport(CampaignReport campaignReport) {
 
 		campaignReports.add(campaignReport);
 
 		/*
 		 * for each campaign, the accumulated statistics from day 1 up to day
 		 * n-1 are reported
 		 */
 		for (CampaignReportKey campaignKey : campaignReport.keys()) {
 			int cmpId = campaignKey.getCampaignId();
 			CampaignStats cstats = campaignReport.getCampaignReportEntry(
 					campaignKey).getCampaignStats();
 			myCampaigns.get(cmpId).setStats(cstats);
 
 			log.info("Day " + day + ": Updating campaign " + cmpId + " stats: "
 					+ cstats.getTargetedImps() + " tgtImps "
 					+ cstats.getOtherImps() + " nonTgtImps. Cost of imps is "
 					+ cstats.getCost());
 		}
 	}
 
 	/**
 	 * Users and Publishers statistics: popularity and ad type orientation
 	 */
 	private void handleAdxPublisherReport(AdxPublisherReport adxPublisherReport) {
 		log.info("Publishers Report: ");
 		for (PublisherCatalogEntry publisherKey : adxPublisherReport.keys()) {
 			AdxPublisherReportEntry entry = adxPublisherReport
 					.getEntry(publisherKey);
 			log.info(entry.toString());
 			
 		}
 		
 	}
 
 	/**
 	 * 
 	 * @param AdNetworkReport
 	 */
 	private void handleAdNetworkReport(AdNetworkReport adnetReport) {
 		
 		log.info("Day "+ day + " : AdNetworkReport");
 		
 		
 //		 for (AdNetworkKey adnetKey : adnetReport.keys()) {
 //			double rnd = Math.random();
 //			if (rnd > 0.95) {
 //				AdNetworkReportEntry entry = adnetReport
 //						.getAdNetworkReportEntry(adnetKey);
 //				
 //				log.info(adnetKey + " " + entry);
 //			}
 //		}
         
 	}
 	//Why did the TAu agent override this? DO we need to? Or should we not touch it?
 	@Override
 	protected void simulationSetup() {
 		randomGenerator = new Random();
 		day = 0;
 		bidBundle = new AdxBidBundle();
 		ucsTargetLevel = 0.5 + (randomGenerator.nextInt(5) + 1) / 10.0;
 		
 		/* initial bid between 0.1 and 0.2 */
 		ucsBid = 0.1 + 0.1*randomGenerator.nextDouble();
 		
 		myCampaigns = new HashMap<Integer, CampaignData>();
 		log.fine("AdNet " + getName() + " simulationSetup");
 	}
 
 	@Override
 	protected void simulationFinished() {
 		campaignReports.clear();
 		bidBundle = null;
 	}
 
 	/**
 	 * NOTE: This was implemented by Mariano, Why? Should we keep it or do we want a diff. data structure?
 	 * OR does it hae to do with the bid bundle?
 	 * A user visit to a publisher's web-site results in an impression
 	 * opportunity (a query) that is characterized by the the publisher, the
 	 * market segment the user may belongs to, the device used (mobile or
 	 * desktop) and the ad type (text or video).
 	 * 
 	 * An array of all possible queries is generated here, based on the
 	 * publisher names reported at game initialization in the publishers catalog
 	 * message
 	 */
 	private void generateAdxQuerySpace() {
 		if (publisherCatalog != null && queries == null) {
 			Set<AdxQuery> querySet = new HashSet<AdxQuery>();
 
 			/*
 			 * for each web site (publisher) we generate all possible variations
 			 * of device type, ad type, and user market segment
 			 */
 			for (PublisherCatalogEntry publisherCatalogEntry : publisherCatalog) {
 				String publishersName = publisherCatalogEntry
 						.getPublisherName();
 				for (MarketSegment userSegment : MarketSegment.values()) {
 					Set<MarketSegment> singleMarketSegment = new HashSet<MarketSegment>();
 					singleMarketSegment.add(userSegment);
 
 					querySet.add(new AdxQuery(publishersName,
 							singleMarketSegment, Device.mobile, AdType.text));
 
 					querySet.add(new AdxQuery(publishersName,
 							singleMarketSegment, Device.pc, AdType.text));
 
 					querySet.add(new AdxQuery(publishersName,
 							singleMarketSegment, Device.mobile, AdType.video));
 
 					querySet.add(new AdxQuery(publishersName,
 							singleMarketSegment, Device.pc, AdType.video));
 
 				}
 				
 				/**
 				 * An empty segments set is used to indicate the "UNKNOWN" segment
 				 * such queries are matched when the UCS fails to recover the user's
 				 * segments.
 				 */
 				querySet.add(new AdxQuery(publishersName,
 						new HashSet<MarketSegment>(), Device.mobile,
 						AdType.video));
 				querySet.add(new AdxQuery(publishersName,
 						new HashSet<MarketSegment>(), Device.mobile,
 						AdType.text));
 				querySet.add(new AdxQuery(publishersName,
 						new HashSet<MarketSegment>(), Device.pc, AdType.video));
 				querySet.add(new AdxQuery(publishersName,
 						new HashSet<MarketSegment>(), Device.pc, AdType.text));
 			}
 			queries = new AdxQuery[querySet.size()];
 			querySet.toArray(queries);
 		}
 	}
 
 	private class CampaignData {
 		/* campaign attributes as set by server */
 		Long reachImps;
 		long dayStart;
 		long dayEnd;
 		Set<MarketSegment> targetSegments;
 		double videoCoef;
 		double mobileCoef;
 		int id;
 
 		/* campaign info as reported */
 		CampaignStats stats;
 		double budget;
 
 		public CampaignData(InitialCampaignMessage icm) {
 			reachImps = icm.getReachImps();
 			dayStart = icm.getDayStart();
 			dayEnd = icm.getDayEnd();
 			targetSegments = icm.getTargetSegment();
 			videoCoef = icm.getVideoCoef();
 			mobileCoef = icm.getMobileCoef();
 			id = icm.getId();
 
 			stats = new CampaignStats(0, 0, 0);
 			budget = 0.0;
 		}
 
 		public void setBudget(double d) {
 			budget = d;
 		}
 
 		public CampaignData(CampaignOpportunityMessage com) {
 			dayStart = com.getDayStart();
 			dayEnd = com.getDayEnd();
 			id = com.getId();
 			reachImps = com.getReachImps();
 			targetSegments = com.getTargetSegment();
 			mobileCoef = com.getMobileCoef();
 			videoCoef = com.getVideoCoef();
 			stats = new CampaignStats(0, 0, 0);
 			budget = 0.0;
 		}
 
 		@Override
 		public String toString() {
 			return "Campaign ID " + id + ": " + "day " + dayStart + " to "
 					+ dayEnd + " " + Arrays.toString(targetSegments.toArray())+ ", reach: "
 					+ reachImps + " coefs: (v=" + videoCoef + ", m="
 					+ mobileCoef + ")";
 		}
 
 		int impsTogo() {
 			return (int) Math.max(0, reachImps - stats.getTargetedImps());
 		}
 
 		void setStats(CampaignStats s) {
 			stats.setValues(s);
 		}
 
 	}
 
 
 }
 
