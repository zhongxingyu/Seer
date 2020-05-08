 package edu.umich.eecs.tac.sim;
 
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.util.*;
 
 import com.botbox.util.ArrayUtils;
 import se.sics.isl.util.ConfigManager;
 import se.sics.isl.util.IllegalConfigurationException;
 import se.sics.isl.transport.Transportable;
 import se.sics.tasim.is.EventWriter;
 import se.sics.tasim.props.ServerConfig;
 import se.sics.tasim.props.StartInfo;
 import se.sics.tasim.props.SimulationStatus;
 import se.sics.tasim.is.SimulationInfo;
 import se.sics.tasim.sim.LogWriter;
 import se.sics.tasim.sim.Simulation;
 import se.sics.tasim.sim.SimulationAgent;
 import se.sics.tasim.aw.Message;
 import edu.umich.eecs.tac.TACAAConstants;
 import edu.umich.eecs.tac.props.*;
 import static edu.umich.eecs.tac.TACAAConstants.*;
 
 /**
  * @author Lee Callender, Patrick Jordan, Ben Cassell
  */
 public class TACAASimulation extends Simulation implements AgentRepository, SalesReportSender, BankStatusSender {
 	private Bank bank;
 	private SalesAnalyst salesAnalyst;
 
 	private String timeUnitName = "Day";
 	private int currentTimeUnit = 0;
 	private int secondsPerDay = 10;
 	private int numberOfDays = 60;
 
 	private int numberOfAdvertisers = TACAAManager.NUMBER_OF_ADVERTISERS;
 
 	private int pingInterval = 0;
 	private int nextPingRequest = 0;
 	private int nextPingReport = 0;
 
 	private RetailCatalog retailCatalog;
 	private SlotInfo slotInfo;
 	private ReserveInfo reserveInfo;
 	private UserClickModel userClickModel;
 	private String[] manufacturers;
 	private String[] components;
 	private String[] advertiserAddresses = new String[numberOfAdvertisers];
 	private Map<String, AdvertiserInfo> advertiserInfoMap;
 
 	private Random random;
 
 	private Runnable afterTickTarget = new Runnable() {
 		public void run() {
 			handleAfterTick();
 		}
 	};
 
 	private boolean recoverAgents = false;
 
 	private static final Logger log = Logger.getLogger(TACAASimulation.class
 			.getName());
 
 	public TACAASimulation(ConfigManager config) {
 		super(config);
 
 		random = new Random();
 	}
 
 	public RetailCatalog getRetailCatalog() {
 		return retailCatalog;
 	}
 
 	public Map<String, AdvertiserInfo> getAdvertiserInfo() {
 		return advertiserInfoMap;
 	}
 
 	protected void setupSimulation() throws IllegalConfigurationException {
 		ConfigManager config = getConfig();
 		SimulationInfo info = getSimulationInfo();
 
 		int seconds = info.getParameter("secondsPerDay", 0);
 		this.secondsPerDay = seconds <= 1 ? config.getPropertyAsInt(
 				"game.secondsPerDay", secondsPerDay) : seconds; // SimulationInfo
 																// gets priority
 																// over
 																// ConfigManager
 		if (this.secondsPerDay < 1)
 			this.secondsPerDay = 1;
 
 		this.numberOfDays = // Make sure this is correct.
 		info.getSimulationLength() / (this.secondsPerDay * 1000);
 
 		int pingIntervalSeconds = config.getPropertyAsInt("ping.interval", 0);
 		if (pingIntervalSeconds > 0) {
 			this.pingInterval = pingIntervalSeconds / this.secondsPerDay;
 			if (this.pingInterval <= 1) {
 				this.pingInterval = 1;
 			}
 			this.nextPingRequest = this.pingInterval;
 			this.nextPingReport = this.pingInterval + 1;
 		} else {
 			this.pingInterval = 0;
 		}
 
 		// The number of participants should be specifiable in the
 		// simulation parameters. FIX THIS!!! FIX THIS!!!
 		this.numberOfAdvertisers = config.getPropertyAsInt(
 				"game.numberOfAdvertisers", TACAAManager.NUMBER_OF_ADVERTISERS);
 
 		log.info("TACAA Simulation " + info.getSimulationID()
 				+ " is setting up...");
 
 		// Initialize in-game agents, bank etc.
 		bank = new Bank(this, this.getSimulationInfo(), numberOfAdvertisers);
 		salesAnalyst = new DefaultSalesAnalyst(this, this, numberOfAdvertisers);
 
 		createBuiltinAgents("users", USERS, Users.class);
 		createBuiltinAgents("publisher", PUBLISHER, Publisher.class);
 
 		SimulationAgent[] users = getAgents(USERS);
 		log.info("Created " + (users == null ? 0 : users.length) + " users");
 		SimulationAgent[] publishers = getAgents(PUBLISHER);
 		log.info("Created " + (publishers == null ? 0 : publishers.length)
 				+ " publishers");
 
 		this.retailCatalog = createRetailCatalog();
 		this.slotInfo = createSlotInfo();
 		this.reserveInfo = createReserveInfo();
 		this.manufacturers = createManufacturers();
 		this.components = createComponents();
 
 		validateConfiguration();
 
 		// Create proxy agents for all participants
 		for (int i = 0, n = info.getParticipantCount(); i < n; i++) {
 			// Must associate a user id with the agent to connect it with an
 			// agent identity (might be external). Only ADVERTISER participants
 			// are allowed to join the simulation for now which means the
 			// participant role does not need to be checked
 			createExternalAgent("adv" + (i + 1), ADVERTISER, info
 					.getParticipantID(i));
 		}
 		if (info.getParticipantCount() < numberOfAdvertisers) {
 			createDummies("dummy.advertiser", ADVERTISER, numberOfAdvertisers
 					- info.getParticipantCount());
 		}
 
 		initializeAdvertisers();
 
 		// Simulation setup needs to be called after advertisers have been
 		// initialized
 		// Users must be initialized first
 		if (users != null) {
 			for (int i = 0, n = users.length; i < n; i++) {
 				SimulationAgent user = users[i];
 				Users userAgent = (Users) user.getAgent();
 				userAgent.simulationSetup(this, users[i].getIndex());
 				addTimeListener(userAgent);
 				userAgent.addUserEventListener(salesAnalyst);
 			}
 		}
 
 		if (publishers != null) {
 			for (int i = 0, n = publishers.length; i < n; i++) {
 				SimulationAgent publisher = publishers[i];
 				Publisher publisherAgent = (Publisher) publisher.getAgent();
 				publisherAgent.simulationSetup(this, publishers[i].getIndex());
 				addTimeListener(publisherAgent);				
 			}
 		}
 
 		userClickModel = createUserClickModel();
 
 	}
 
 	private double sample(double min, double max) {
 		return min + random.nextDouble() * (max - min);
 	}
 
 	private int sample(int min, int max) {
 		return min + (max > min ? random.nextInt(max - min) : 0);
 	}
 
 	private SlotInfo createSlotInfo() {
 		SlotInfo slotInfo = new SlotInfo();
 
 		ConfigManager config = getConfig();
 
 		int promotedSlotsMin = config.getPropertyAsInt(
 				"publisher.promoted.slots.min", 0);
 		int promotedSlotsMax = config.getPropertyAsInt(
 				"publisher.promoted.slots.max", 0);
 		int regularSlots = config
 				.getPropertyAsInt("publisher.regular.slots", 0);
 		double promotedSlotBonus = config.getPropertyAsDouble(
 				"publisher.promoted.slotbonus", 0.0);
 
 		slotInfo.setPromotedSlots(sample(promotedSlotsMin, promotedSlotsMax));
 
 		slotInfo.setRegularSlots(regularSlots);
 		slotInfo.setPromotedSlotBonus(promotedSlotBonus);
 
 		return slotInfo;
 	}
 
 	private ReserveInfo createReserveInfo() {
 		ReserveInfo reserveInfo = new ReserveInfo();
 
 		ConfigManager config = getConfig();
 
 		double promotedReserveMax = config.getPropertyAsDouble(
 				"publisher.promoted.reserve.max", 0.0);
 
 		int promotedSlotsMin = config.getPropertyAsInt(
 				"publisher.promoted.slots.min", 0);
 		int promotedSlotsMax = config.getPropertyAsInt(
 				"publisher.promoted.slots.max", 0);
 
 		double regularReserveMin = config.getPropertyAsDouble(
 				"publisher.regular.reserve.min", 0.0);
 		double regularReserveMax = config.getPropertyAsDouble(
 				"publisher.regular.reserve.max", 0.0);
 
 		int regularSlots = config
 				.getPropertyAsInt("publisher.regular.slots", 0);
 		double promotedSlotBonus = config.getPropertyAsDouble(
 				"publisher.promoted.slotbonus", 0.0);
 
 		reserveInfo.setRegularReserve(sample(regularReserveMin,
 				regularReserveMax));
 		reserveInfo.setPromotedReserve(sample(reserveInfo.getRegularReserve(),
 				promotedReserveMax));
 
 		return reserveInfo;
 	}
 
 	private void initializeAdvertisers() {
 		ConfigManager config = getConfig();
 		Random r = new Random();
 
 		SimulationAgent[] publishers = getAgents(PUBLISHER);
 
 		String publisherAddress = "publisher";
 		if (publishers != null && publishers.length > 0
 				&& publishers[0] != null) {
 			publisherAddress = publishers[0].getAddress();
 		}
 
 		int highValue = config.getPropertyAsInt("advertiser.capacity.high", 0);
 		int medValue = config.getPropertyAsInt("advertiser.capacity.med", 0);
 		int lowValue = config.getPropertyAsInt("advertiser.capacity.low", 0);
 
 		int highCount = config.getPropertyAsInt(
 				"advertiser.capacity.highCount", 0);
 		int lowCount = config.getPropertyAsInt("advertiser.capacity.lowCount",
 				0);
 
 		double manufacturerBonus = config.getPropertyAsDouble(
 				"advertiser.specialization.manufacturerBonus", 0.0);
 		double componentBonus = config.getPropertyAsDouble(
 				"advertiser.specialization.componentBonus", 0.0);
 		double decayRate = config.getPropertyAsDouble("advertiser.capacity.distribution_capacity_discounter", 1.0);
 		double targetEffect = config.getPropertyAsDouble(
 				"advertiser.targeteffect", 0.5);
 		int window = config.getPropertyAsInt("advertiser.capacity.window", 7);
 
 		double focusEffect_f0 = config.getPropertyAsDouble(
 				"advertiser.focuseffect.FOCUS_LEVEL_ZERO", 1.0);
 		double focusEffect_f1 = config.getPropertyAsDouble(
 				"advertiser.focuseffect.FOCUS_LEVEL_ONE", 1.0);
 		double focusEffect_f2 = config.getPropertyAsDouble(
 				"advertiser.focuseffect.FOCUS_LEVEL_TWO", 1.0);
 
 		// Initialize advertisers..
 		SimulationAgent[] advertisers = getAgents(ADVERTISER);
 		advertiserInfoMap = new HashMap<String, AdvertiserInfo>();
 
 		if (advertisers != null) {
 
 			// Create capacities and either assign or randomize
 			int[] capacities = new int[advertisers.length];
 			
 			//check simulationInfo
 			SimulationInfo info = getSimulationInfo();
 			String key = "perm"+Integer.toString((info.getSimulationID()%4)+1);
 			String perm = info.getParams();
 			int start;
 			if(perm == null)
 				start = -1;
 			else
 				start = perm.indexOf(key);
 			if(start == -1){
 			
 				log.log(Level.INFO, "Using random capacity assigments");
 				for (int i = 0; i < highCount && i < capacities.length; i++) {
 					capacities[i] = highValue;
 				}
 
 				for (int i = highCount; i < highCount + lowCount && i < capacities.length; i++) {
 					capacities[i] = lowValue;
 				}
 
 				for (int i = highCount + lowCount; i < capacities.length; i++) {
 					capacities[i] = medValue;
 				}
 
 				for (int i = 0; i < capacities.length; i++) {
 					int rindex = i + r.nextInt(capacities.length - i);
 
 					int sw = capacities[i];
 					capacities[i] = capacities[rindex];
 					capacities[rindex] = sw;
 					
 				}
 			
 			}
 			else{
 				log.log(Level.INFO, "Using permuted capacity assigments");
 				for(int i = 0; i < highCount && i < capacities.length; i++){
 					capacities[Integer.parseInt(perm.substring(i+start+6, i+start+7))-1] = highValue;
 				}
 				for(int i = highCount; i < highCount+lowCount && i < capacities.length; i++){
 					capacities[Integer.parseInt(perm.substring(i+start+6, i+start+7))-1] = lowValue;
 				}
 				for(int i = highCount + lowCount; i < capacities.length; i++){
 					capacities[Integer.parseInt(perm.substring(i+start+6, i+start+7))-1] = medValue;
 				}
 			}
 			for (int i = 0, n = advertisers.length; i < n; i++) {
 				SimulationAgent agent = advertisers[i];
 
 				String agentAddress = agent.getAddress();
 				advertiserAddresses[i] = agentAddress;
 
 				AdvertiserInfo advertiserInfo = new AdvertiserInfo();
 				advertiserInfo.setAdvertiserId(agentAddress);
 				advertiserInfo.setPublisherId(publisherAddress);
 				advertiserInfo.setDistributionCapacity(capacities[i]);
 				advertiserInfo.setDistributionCapacityDiscounter(decayRate);
 				advertiserInfo.setComponentSpecialty(components[r
 						.nextInt(components.length)]);
 				advertiserInfo.setComponentBonus(componentBonus);
 				advertiserInfo.setManufacturerSpecialty(manufacturers[r
 						.nextInt(manufacturers.length)]);
 				advertiserInfo.setManufacturerBonus(manufacturerBonus);
 				advertiserInfo.setDistributionWindow(window);
 				advertiserInfo.setTargetEffect(targetEffect);
 				advertiserInfo.setFocusEffects(QueryType.FOCUS_LEVEL_ZERO,
 						focusEffect_f0);
 				advertiserInfo.setFocusEffects(QueryType.FOCUS_LEVEL_ONE,
 						focusEffect_f1);
 				advertiserInfo.setFocusEffects(QueryType.FOCUS_LEVEL_TWO,
 						focusEffect_f2);
 				advertiserInfo.lock();
 
 				advertiserInfoMap.put(agentAddress, advertiserInfo);
 
 				// Create bank account for the advertiser
 				bank.addAccount(agentAddress);
 				salesAnalyst.addAccount(agentAddress);
 			}
 		}
 	}
 
 	protected String getTimeUnitName() {
 		return timeUnitName;
 	}
 
 	protected int getTimeUnitCount() {
 		return numberOfDays;
 	}
 
 	protected void startSimulation() {
 		LogWriter logWriter = getLogWriter();
 
 		// Save the server configuration to the log.
 		ConfigManager config = getConfig();
 		ServerConfig serverConfig = new ServerConfig(config);
 		logWriter.write(serverConfig);
 
 		// Log the retailCatalog
 		logWriter.dataUpdated(TYPE_NONE, this.retailCatalog);
         logWriter.dataUpdated(TYPE_NONE, this.slotInfo);		
 
 		SimulationInfo simInfo = getSimulationInfo();
 		StartInfo startInfo = createStartInfo(simInfo);
 		startInfo.lock();
 
 		logWriter.dataUpdated(TYPE_NONE, startInfo);
 
 		sendToRole(PUBLISHER, startInfo);
 		sendToRole(USERS, startInfo);
 		sendToRole(ADVERTISER, startInfo); // startInfo may want to contain more
 											// information for advertiser
 
 		// If a new agent arrives now it will be recovered
 		recoverAgents = true;
 
 		// Send the retail catalog to the manufacturer and customers
 		sendToRole(PUBLISHER, this.retailCatalog);
 		sendToRole(USERS, this.retailCatalog);
 		sendToRole(ADVERTISER, this.retailCatalog);
 
 		sendToRole(PUBLISHER, this.slotInfo);
 		sendToRole(USERS, this.slotInfo);
 		sendToRole(ADVERTISER, this.slotInfo);
 
 		sendToRole(PUBLISHER, this.userClickModel);
 		sendToRole(USERS, this.userClickModel);
 		sendToRole(PUBLISHER, this.reserveInfo);
 		sendToRole(USERS, this.reserveInfo);
 
         for (SimulationAgent publisher : getPublishers()) {
             Publisher publisherAgent = (Publisher) publisher.getAgent();
             publisherAgent.sendPublisherInfoToAll();
         }
 
 
 		for (Map.Entry<String, AdvertiserInfo> entry : advertiserInfoMap
 				.entrySet()) {
 			sendMessage(entry.getKey(), entry.getValue());
 		}
 
 		startTickTimer(simInfo.getStartTime(), secondsPerDay * 1000);
 
 		logWriter.commit();
 
 	}
 
 	private StartInfo createStartInfo(SimulationInfo info) {
 		StartInfo startInfo = new StartInfo(info.getSimulationID(), info
 				.getStartTime(), info.getSimulationLength(), secondsPerDay);
 		return startInfo;
 	}
 
 	/**
 	 * Notification when this simulation is preparing to stop. Called after the
 	 * agents have been stopped but still can receive messages.
 	 */
 	protected void prepareStopSimulation() {
 		// No longer any need to recover agents
 		recoverAgents = false;
 		// The bank needs to send its final account statuses
 		bank.sendBankStatusToAll();
 		salesAnalyst.sendSalesReportToAll();
 
 		// Send the final simulation status
 		int millisConsumed = (int) (getServerTime() - getSimulationInfo()
 				.getEndTime());
 		SimulationStatus status = new SimulationStatus(numberOfDays,
 				millisConsumed, true);
 		// SimulationStatus only to manufacturers or all agents??? FIX THIS!!!
 		sendToRole(ADVERTISER, status);
 
 	}
 
 	/**
 	 * Notification when this simulation has been stopped. Called after the
 	 * agents shutdown.
 	 */
 	protected void completeStopSimulation() {
 		LogWriter writer = getLogWriter();
 		writer.commit();
 	}
 
 	/**
 	 * Called when entering a new time unit similar to time listeners but this
 	 * method is guaranteed to be called before the time listeners.
 	 * 
 	 * @param timeUnit
 	 *            the current time unit
 	 */
 	protected void nextTimeUnitStarted(int timeUnit) {
 		this.currentTimeUnit = timeUnit;
 
 		LogWriter writer = getLogWriter();
 		writer.nextTimeUnit(timeUnit, getServerTime());
 
 		if (timeUnit >= numberOfDays) {
 			// Time to stop the simulation
 			requestStopSimulation();
 		} else {
 			// Let the bank send their first messages
 			bank.sendBankStatusToAll();
 			salesAnalyst.sendSalesReportToAll();
 
 			for (SimulationAgent agent : getAgents(PUBLISHER)) {
 				if (agent.getAgent() instanceof Publisher) {
 					Publisher publisher = (Publisher) agent.getAgent();
 					publisher.sendQueryReportsToAll();
                     publisher.applyBidUpdates();
 				}
 			}
 
 			for (SimulationAgent agent : getAgents(USERS)) {
 				if (agent.getAgent() instanceof Users) {
 					Users users = (Users) agent.getAgent();
 					users.broadcastUserDistribution();
 				}
 			}
 		}
 	}
 
 	/**
 	 * Called when a new time unit has begun similar to time listeners but this
 	 * method is guaranteed to be called after the time listeners.
 	 * 
 	 * @param timeUnit
 	 *            the current time unit
 	 */
 	protected void nextTimeUnitFinished(int timeUnit) {
 		if (timeUnit < numberOfDays) {
 			int millisConsumed = (int) (getServerTime()
 					- getSimulationInfo().getStartTime() - timeUnit
 					* secondsPerDay * 1000);
 
 			SimulationStatus status = new SimulationStatus(timeUnit,
 					millisConsumed);
 			sendToRole(ADVERTISER, status); // Advertisers notified of new day
 		}
 
 		invokeLater(afterTickTarget); // ?
 	}
 
 	/**
 	 * Called each day after all morning messages has been sent.
 	 */
 	private void handleAfterTick() {
 		if (pingInterval > 0 && currentTimeUnit < numberOfDays) {
 			if (currentTimeUnit >= nextPingRequest) {
 				nextPingRequest += pingInterval;
 
 				SimulationAgent[] advertisers = getAgents(ADVERTISER);
 				if (advertisers != null) {
 					for (int i = 0, n = advertisers.length; i < n; i++) {
 						advertisers[i].requestPing();
 					}
 				}
 			}
 
 			if (currentTimeUnit >= nextPingReport) {
 				nextPingReport += pingInterval;
 
 				SimulationAgent[] advertisers = getAgents(ADVERTISER);
 				if (advertisers != null) {
 					EventWriter writer = getEventWriter();
 					synchronized (writer) {
 						for (int i = 0, n = advertisers.length; i < n; i++) {
 							SimulationAgent sa = advertisers[i];
 							if (sa.getPingCount() > 0) {
 								int index = sa.getIndex();
 								writer.dataUpdated(index,
 										DU_NETWORK_AVG_RESPONSE, sa
 												.getAverageResponseTime());
 								writer.dataUpdated(index,
 										DU_NETWORK_LAST_RESPONSE, sa
 												.getLastResponseTime());
 							}
 						}
 					}
 				}
 			}
 		}
 
 		// Since all day start handling now is finished for this day and
 		// the manufacturer agents will have some time to respond when
 		// requesting pings, it is a good time to do some memory
 		// management.
 		System.gc();
 		System.gc();
 	}
 
 	private String[] createManufacturers() {
 		return retailCatalog.getManufacturers().toArray(new String[0]);
 	}
 
 	private String[] createComponents() {
 		return retailCatalog.getComponents().toArray(new String[0]);
 	}
 
 	private RetailCatalog createRetailCatalog() {
 		ConfigManager config = getConfig();
 
 		RetailCatalog catalog = new RetailCatalog();
 
 		String[] skus = config.getPropertyAsArray("catalog.sku");
 
 		for (String sku : skus) {
 			String manufacturer = config.getProperty(String.format(
 					"catalog.%s.manufacturer", sku));
 			String component = config.getProperty(String.format(
 					"catalog.%s.component", sku));
 			double salesProfit = config.getPropertyAsDouble(String.format(
 					"catalog.%s.salesProfit", sku), 0.0);
 
 			Product product = new Product(manufacturer, component);
 
 			catalog.setSalesProfit(product, salesProfit);
 		}
 
 		catalog.lock();
 
 		return catalog;
 	}
 
 	private UserClickModel createUserClickModel() {
 		ConfigManager config = getConfig();
 
 		String[] advertisers = getAdvertiserAddresses();
 		Set<Query> queryList = new HashSet<Query>();
 
 		for (Product product : retailCatalog) {
 			// Create f0
 			Query f0 = new Query();
 
 			// Create f1's
 			Query f1_manufacturer = new Query(product.getManufacturer(), null);
 			Query f1_component = new Query(null, product.getComponent());
 
 			// Create f2
 			Query f2 = new Query(product.getManufacturer(), product
 					.getComponent());
 
 			queryList.add(f0);
 			queryList.add(f1_manufacturer);
 			queryList.add(f1_component);
 			queryList.add(f2);
 		}
 
 		Query[] queries = queryList.toArray(new Query[0]);
 
 		UserClickModel clickModel = new UserClickModel(queries, advertisers);
 
 		Random random = getRandom();
 
 		for (int queryIndex = 0; queryIndex < clickModel.queryCount(); queryIndex++) {
 			double continuationLow = config.getPropertyAsDouble(String.format(
 					"users.clickbehavior.continuationprobability.%s.low",
 					clickModel.query(queryIndex).getType()), 0.1);
 			double continuationHigh = config.getPropertyAsDouble(String.format(
 					"users.clickbehavior.continuationprobability.%s.high",
 					clickModel.query(queryIndex).getType()), 0.9);
 			double effectLow = config.getPropertyAsDouble(String.format(
 					"users.clickbehavior.advertisereffect.%s.low", clickModel
 							.query(queryIndex).getType()), 0.1);
 			double effectHigh = config.getPropertyAsDouble(String.format(
 					"users.clickbehavior.advertisereffect.%s.high", clickModel
 							.query(queryIndex).getType()), 0.9);
 
 			double continuationProbability = Math.max(Math.min(1.0, random
 					.nextDouble()
 					* (continuationHigh - continuationLow) + continuationLow),
 					0.0);
 
 			clickModel.setContinuationProbability(queryIndex,
 					continuationProbability);
 
 			for (int advertiserIndex = 0; advertiserIndex < clickModel
 					.advertiserCount(); advertiserIndex++) {
 				double effect = Math.max(Math.min(1.0, random.nextDouble()
 						* (effectHigh - effectLow) + effectLow), 0.0);
 				clickModel.setAdvertiserEffect(queryIndex, advertiserIndex,
 						effect);
 			}
 		}
 
 		clickModel.lock();
 
 		return clickModel;
 	}
 
 	private void validateConfiguration() throws IllegalConfigurationException {
 		// TODO: do validation
 	}
 
 	/**
 	 * Called whenever an external agent has logged in and needs to recover its
 	 * state. The simulation should respond with the current recover mode (none,
 	 * immediately, or after next time unit). This method should return
 	 * <code>RECOVERY_NONE</code> if the simulation not yet have been started.
 	 * <p/>
 	 * <p/>
 	 * The simulation might recover the agent using this method if recovering
 	 * the agent can be done using the agent communication thread. In that case
 	 * <code>RECOVERY_NONE</code> should be returned. If any other recover mode
 	 * is returned, the simulation will later be asked to recover the agent
 	 * using the simulation thread by a call to <code>recoverAgent</code>.
 	 * <p/>
 	 * A common case might be when an agent reestablishing a lost connection to
 	 * the server.
 	 * 
 	 * @param agent
 	 *            the <code>SimulationAgent</code> to be recovered.
 	 * @return the recovery mode for the agent
 	 * @see #RECOVERY_NONE
 	 * @see #RECOVERY_IMMEDIATELY
 	 * @see #RECOVERY_AFTER_NEXT_TICK
 	 * @see #recoverAgent(se.sics.tasim.sim.SimulationAgent)
 	 */
 	protected int getAgentRecoverMode(SimulationAgent agent) {
 		if (!recoverAgents) {
 			return RECOVERY_NONE;
 		}
 		if (agent.hasAgentBeenActive()) {
 			// The agent has been active and we must use the simulation
 			// thread to retrieve all active orders (the information may
 			// only be accessed using the simulation thread)
 			return RECOVERY_AFTER_NEXT_TICK;
 		}
 		// The agent has not been active i.e. not sent any messages in
 		// this simulation. This means the agent can not have any active
 		// orders and only the startup messages needs to be sent. This can
 		// be done using any thread.
 		recoverAgent(agent);
 		return RECOVERY_NONE;
 	}
 
 	/**
 	 * Called whenever an external agent has logged in and needs to recover its
 	 * state. The simulation should respond with the setup messages together
 	 * with any other state information the agent needs to continue playing in
 	 * the simulation (orders, inventory, etc). This method should not do
 	 * anything if the simulation not yet have been started.
 	 * <p/>
 	 * <p/>
 	 * A common case might be when an agent reestablishing a lost connection to
 	 * the server.
 	 * 
 	 * @param agent
 	 *            the <code>SimulationAgent</code> to be recovered.
 	 */
 	protected void recoverAgent(SimulationAgent agent) {
 
 		if (recoverAgents) {
 
 			log.warning("recovering agent " + agent.getName());
 
 			String agentAddress = agent.getAddress();
 
 			StartInfo info = createStartInfo(getSimulationInfo());
 			info.lock();
 
 			sendMessage(new Message(agentAddress, info));
 
 			sendMessage(new Message(agentAddress, this.slotInfo));
 
 			sendMessage(new Message(agentAddress, this.retailCatalog));
 
            sendAdvertiserInfo(agentAddress, advertiserInfoMap.get(agentAddress));
 
             for (SimulationAgent publisher : getPublishers()) {
 				Publisher publisherAgent = (Publisher) publisher.getAgent();
 				publisherAgent.sendPublisherInfo(agentAddress);
 			}
 		}
 	}

    private void sendAdvertiserInfo(String agentAddress, AdvertiserInfo advertiserInfo) {
        sendMessage(new Message(agentAddress, advertiserInfo));
        getEventWriter().dataUpdated(agentIndex(agentAddress), TACAAConstants.DU_ADVERTISER_INFO, advertiserInfo);
    }

 	/**
 	 * Delivers a message to the coordinator (the simulation). The coordinator
 	 * must self validate the message.
 	 * 
 	 * @param message
 	 *            the message
 	 */
 	protected void messageReceived(Message message) {
 		log.warning("received (ignoring) " + message);
 	}
 
 	public static String getSimulationRoleName(int simRole) {
 		return simRole >= 0 && simRole < ROLE_NAME.length ? ROLE_NAME[simRole]
 				: null;
 	}
 
 	public static int getSimulationRole(String role) {
 		return ArrayUtils.indexOf(ROLE_NAME, role);
 	}
 
 	// -------------------------------------------------------------------
 	// Logging handling
 	// -------------------------------------------------------------------
 
 	/**
 	 * Validates this message to ensure that it may be delivered to the agent.
 	 * Messages to the coordinator and the administration are never validated.
 	 * 
 	 * @param receiverAgent
 	 *            the agent to deliver the message to
 	 * @param message
 	 *            the message to validate
 	 * @return true if the message should be delivered and false otherwise
 	 */
 	protected boolean validateMessage(SimulationAgent receiverAgent,
 			Message message) {
 		String sender = message.getSender();
 		SimulationAgent senderAgent = getAgent(sender);
 		int senderIndex;
 		if (senderAgent == null) {
 			// Messages from or the coordinator or administration are always
 			// allowed.
 			senderIndex = COORDINATOR_INDEX;
 
 		} else if (senderAgent.getRole() == receiverAgent.getRole()) {
 			// No two agents with the same role in the simulation may
 			// communicate with each other. A simple security measure to
 			// avoid manufacturer agents to communicate or deceive each
 			// other.
 			return false;
 
 		} else {
 			senderIndex = senderAgent.getIndex();
 		}
 
 		int receiverIndex = receiverAgent.getIndex();
 		Transportable content = message.getContent();
 		Class contentType = content.getClass();
 		if (logContentType(contentType)) {
 			LogWriter writer = getLogWriter();
 			writer
 					.message(senderIndex, receiverIndex, content,
 							getServerTime());
 			writer.commit();
 		}
 
 		int type = getContentType(contentType);
 		if (type != TYPE_NONE) {
 			getEventWriter().interaction(senderIndex, receiverIndex, type);
 		}
 		return true;
 
 	}
 
 	/**
 	 * Validates this message to ensure that it may be broadcasted to all agents
 	 * with the specified role.
 	 * <p/>
 	 * This method can also be used to log messages
 	 * 
 	 * @param sender
 	 *            the agent sender the message
 	 * @param role
 	 *            the role of all receiving agents
 	 * @param content
 	 *            the message content
 	 * @return true if the message should be delivered and false otherwise
 	 */
 	protected boolean validateMessageToRole(SimulationAgent sender, int role,
 			Transportable content) {
 		// Only customer broadcast of RFQBundle to manufacturers are
 		// allowed for now.
 		// if (role == MANUFACTURER && senderAgent.getRole() == CUSTOMER
 		// && content.getClass() == RFQBundle.class) {
 		// logToRole(senderAgent.getIndex(), role, content);
 		// return true;
 		// }
 		return false;
 
 	}
 
 	/**
 	 * Validates this message from the coordinator to ensure that it may be
 	 * broadcasted to all agents with the specified role.
 	 * <p/>
 	 * This method can also be used to log messages
 	 * 
 	 * @param role
 	 *            the role of all receiving agents
 	 * @param content
 	 *            the message content
 	 * @return true if the message should be delivered and false otherwise
 	 */
 	protected boolean validateMessageToRole(int role, Transportable content) {
 		// Broadcasts from the coordinator are always allowed
 		logToRole(COORDINATOR_INDEX, role, content);
 		return true;
 	}
 
 	private void logToRole(int senderIndex, int role, Transportable content) {
 		// Log this broadcast
 		Class contentType = content.getClass();
 		if (logContentType(contentType)) {
 			LogWriter writer = getLogWriter();
 			writer.messageToRole(senderIndex, role, content, getServerTime());
 			writer.commit();
 		}
 
 		int type = getContentType(contentType);
 		if (type != TYPE_NONE) {
 			getEventWriter().interactionWithRole(senderIndex, role, type);
 		}
 	}
 
 	private boolean logContentType(Class type) {
 		if (type == StartInfo.class) {
 			return false;
 		}
 		return true;
 	}
 
 	private int getContentType(Class type) {
 		// if (type == DeliveryNotice.class) {
 		// return TYPE_DELIVERY;
 		// } else {
 		return TYPE_NONE;
 		// }
 	}
 
 	// -------------------------------------------------------------------
 	// API to TACSCM builtin agents (trusted components)
 	// -------------------------------------------------------------------
 
 	public final int getNumberOfAdvertisers() {
 		return numberOfAdvertisers;
 	}
 
 	public final SimulationAgent[] getPublishers() {
 		return getAgents(PUBLISHER);
 	}
 
 	public final SimulationAgent[] getUsers() {
 		return getAgents(USERS);
 	}
 
 	public final String[] getAdvertiserAddresses() {
 		return advertiserAddresses;
 	}
 
 	final String getAgentName(String agentAddress) {
 		SimulationAgent agent = getAgent(agentAddress);
 		return agent != null ? agent.getName() : agentAddress;
 	}
 
 	final void transaction(String source, String recipient, double amount) {
 		log.finer("Transacted " + amount + " from " + source + " to "
 				+ recipient);
 
 		SimulationAgent sourceAgent = getAgent(source);
 		SimulationAgent receipientAgent = getAgent(recipient);
 
 		if (receipientAgent != null && receipientAgent.getRole() == ADVERTISER) {
 			bank.deposit(recipient, amount);
 		}
 		if (sourceAgent != null && sourceAgent.getRole() == ADVERTISER) {
 			bank.withdraw(source, amount);
 		}
 
 		int sourceIndex = sourceAgent != null ? sourceAgent.getIndex()
 				: COORDINATOR_INDEX;
 		int receipientIndex = receipientAgent != null ? receipientAgent
 				.getIndex() : COORDINATOR_INDEX;
 
 		LogWriter writer = getLogWriter();
 		  synchronized (writer) {
 			writer.node("transaction").attr("source", sourceIndex).attr(
 					"recipient", receipientIndex).attr("amount", amount)
 					.endNode("transaction");
 		}
 	}
 
 	// Publishers send query reports
 	final void sendQueryReport(QueryReport queryReport) {
 		sendToRole(ADVERTISER, queryReport);
 	}
 
 	public SalesAnalyst getSalesAnalyst() {
 		return salesAnalyst;
 	}
 
 	private AdvertiserInfo getAdvertiserInfoForAgent(String agentName) {
 		return advertiserInfoMap.get(agentName);
 	}
 
 	// -------------------------------------------------------------------
 	// API to Bank to allow it to send bank statuses
 	// -------------------------------------------------------------------
 
 	public void sendBankStatus(String agentName, BankStatus status) {
 		sendMessage(agentName, status);
 
 		EventWriter eventWriter = getEventWriter();
 		eventWriter.dataUpdated(getAgent(agentName).getIndex(),
 				DU_BANK_ACCOUNT, status.getAccountBalance());
 	}
 
 	public final void sendSalesReport(String agentName, SalesReport report) {
 		sendMessage(agentName, report);
         getEventWriter().dataUpdated(agentIndex(agentName), TACAAConstants.DU_SALES_REPORT, report);
 	}
 
 	final void sendQueryReport(String agentName, QueryReport report) {
 		sendMessage(agentName, report);
 	}
 
 	public void broadcastConversions(String advertiser, int conversions) {
 		getEventWriter().dataUpdated(agentIndex(advertiser), TACAAConstants.DU_CONVERSIONS, conversions);
 	}
 
     public void broadcastImpressions(String advertiser, int impressions) {
 		getEventWriter().dataUpdated(agentIndex(advertiser), TACAAConstants.DU_IMPRESSIONS, impressions);
 	}
 
     public void broadcastClicks(String advertiser, int clicks) {
 		getEventWriter().dataUpdated(agentIndex(advertiser), TACAAConstants.DU_CLICKS, clicks);
 	}
 
 	public SlotInfo getAuctionInfo() {
 		return slotInfo;
 	}
 
 	public void setAuctionInfo(SlotInfo slotInfo) {
 		this.slotInfo = slotInfo;
 	}
 }
