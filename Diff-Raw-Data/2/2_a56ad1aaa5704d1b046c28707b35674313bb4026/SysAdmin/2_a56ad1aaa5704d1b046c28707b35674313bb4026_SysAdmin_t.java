 package es.upm.dit.gsi.shanks.datacenter.agent;
 
 import java.util.HashMap;
 import java.util.Iterator;
 
 import unbbayes.prs.bn.ProbabilisticNetwork;
 import es.upm.dit.gsi.shanks.ShanksSimulation;
 import es.upm.dit.gsi.shanks.agent.SimpleShanksAgent;
 import es.upm.dit.gsi.shanks.agent.capability.reasoning.bayes.BayesianReasonerShanksAgent;
 import es.upm.dit.gsi.shanks.agent.capability.reasoning.bayes.ShanksAgentBayesianReasoningCapability;
 import es.upm.dit.gsi.shanks.datacenter.model.Values;
 import es.upm.dit.gsi.shanks.datacenter.model.element.device.Router;
 import es.upm.dit.gsi.shanks.datacenter.model.element.device.Server;
 import es.upm.dit.gsi.shanks.model.element.exception.UnsupportedNetworkElementStatusException;
 import es.upm.dit.gsi.shanks.model.scenario.exception.UnsupportedScenarioStatusException;
 import es.upm.dit.gsi.shanks.networkattacks.util.action.RepairComputer;
 
 public class SysAdmin extends SimpleShanksAgent implements
 		BayesianReasonerShanksAgent {
 
 	private String bayesianNetworkFilePath;
 	private ProbabilisticNetwork bayesianNetwork;
 	
 	/**
 	 * Previous logs
 	 */
 	private String logStatus;
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 4001442333333480506L;
 
 	public SysAdmin(String id) {
 		super(id);
 		this.logStatus = Values.SYSADMIN_LOG_OK;
 		this.bayesianNetworkFilePath = Values.SYSADMIN_BAYESIAN_NETWORK_PATH;
 		try {
 			this.bayesianNetwork = ShanksAgentBayesianReasoningCapability
 					.loadNetwork(this.bayesianNetworkFilePath);
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	public void checkMail() {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public ProbabilisticNetwork getBayesianNetwork() {
 		return this.bayesianNetwork;
 	}
 
 	@Override
 	public void setBayesianNetwork(ProbabilisticNetwork bn) {
 		this.bayesianNetwork = bn;
 		// this.bayesianNetworkFilePath
 	}
 
 	@Override
 	public String getBayesianNetworkFilePath() {
 		return this.bayesianNetworkFilePath;
 	}
 
 	@Override
 	public void executeReasoningCycle(ShanksSimulation simulation) {
 		String to_repair = "0";
 		
 		HashMap<String, Boolean> serverStatus = simulation.getScenario().getNetworkElement(Values.SERVER_ID).getStatus();
 		String webServerLoad = null;
 		for(String key: serverStatus.keySet()){
 			if (serverStatus.get(key))
 				webServerLoad = key;
 		}
 		
 		// Router Load
		HashMap<String, Boolean> routerStatus = simulation.getScenario().getNetworkElement(Values.ENTERPRISE_GATEWAY_ID).getStatus();
 		String routerLoad = null;
 		for(String key: routerStatus.keySet()){
 			if (routerStatus.get(key))
 				routerLoad = key;
 		}
 		String log_status = this.logStatus;
 
 		try {
 			
 			// Set the evidences. 
 			ShanksAgentBayesianReasoningCapability.addEvidence(this,
 					Values.SYSADMIN_REPAIR_NODENAME, to_repair);
 			
 			if(webServerLoad != null)
 				ShanksAgentBayesianReasoningCapability.addEvidence(this,
 						Values.SYSADMIN_SERVER_LOAD_NODENAME, webServerLoad);
 			
 			if (routerLoad != null)
 				ShanksAgentBayesianReasoningCapability.addEvidence(this,
 						Values.SYSADMIN_ROUTER_LOAD_NODENAME, routerLoad);
 			ShanksAgentBayesianReasoningCapability.addEvidence(this,
 					Values.SYSADMIN_LOG_NODENAME, log_status);
 
 			this.bayesianNetwork.updateEvidences();
 
 			HashMap<String, Float> hypotheses = ShanksAgentBayesianReasoningCapability
 					.getNodeStatesHypotheses(this,
 							Values.SYSADMIN_ACTION_NODENAME);
 			Iterator<String> actions = hypotheses.keySet().iterator();
 			int randomResult = simulation.random.nextInt(100);
 
 			int probability = 0;
 			String action = (String) Values.ACTION_NOT_AVAILABLE;
 
 			while (probability < randomResult) {
 				action = actions.next();
 				probability += hypotheses.get(action) * 100;
 			}
 			executeAction(action, simulation);
 
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 	}
 
 	private void executeAction(String action, ShanksSimulation sim) {
 		if (action.equalsIgnoreCase(Values.SYSADMIN_ACTION_LOOKOUT)) {
 			lookOut(sim);
 		} else if (action.equalsIgnoreCase(Values.SYSADMIN_ACTION_MAINTENANCE)) {
 			maintenance(sim);
 		} else if (action.equalsIgnoreCase(Values.SYSADMIN_ACTION_PATCH)) {
 			patch(sim);
 		} else if (action.equalsIgnoreCase(Values.SYSADMIN_ACTION_REPAIR)) {
 			repair(sim);
 		} else {
 			// Do nothing, action not recognized
 			sim.logger.warning("Sysadmin action " + action + " not recognized");
 		}
 	}
 
 	/**
 	 * 
 	 * @param sim
 	 */
 	private void lookOut(ShanksSimulation sim) {
 		// TODO: Implement lookOut
 	}
 
 	/**
 	 * Doing the maintenance on a server will reduce its load
 	 * a third.
 	 * 
 	 * @param ShanksSimulation
 	 *            - The simulation
 	 */
 	private void maintenance(ShanksSimulation sim) {
 		// TODO: Implement maintenance
 		// Updates the router vulnerability
 		Router mainRouter = null;
 		Server webServer = null;
 		Server sqlServer = null;
 		
 		//Router load
 		HashMap<String, Object> routerProperties = new HashMap<String, Object>();
 		routerProperties
 				.put(Router.PROPERTY_CONGESTION, ((Double) mainRouter
 						.getProperty(Router.PROPERTY_CONGESTION)) *2/3);
 		mainRouter.setProperties(routerProperties);
 
 		// Updates the server vulnerability
 		HashMap<String, Object> webServerProperties = new HashMap<String, Object>();
 		webServerProperties
 				.put(Server.PROPERTY_LOAD, ((Double) webServer
 						.getProperty(Server.PROPERTY_LOAD)) *2/3);
 		webServer.setProperties(webServerProperties);
 
 		// Updates the sqlServer
 		HashMap<String, Object> sqlServerProperties = new HashMap<String, Object>();
 		sqlServerProperties
 				.put(Server.PROPERTY_LOAD, ((Double) sqlServer
 						.getProperty(Server.PROPERTY_LOAD)) *2/3);
 		sqlServer.setProperties(sqlServerProperties);
 
 	}
 
 	/**
 	 * Patching a server will reduce its posibility of being hacked in half.
 	 * 
 	 * 
 	 * @param ShanksSimulation
 	 *            - The simulation
 	 */
 	private void patch(ShanksSimulation sim) {
 		// TODO: Implement patch
 		Router mainRouter = null;
 		Server webServer = null;
 		Server sqlServer = null;
 
 		// Updates the router vulnerability
 		HashMap<String, Object> routerProperties = new HashMap<String, Object>();
 		routerProperties
 				.put(Router.PROPERTY_VULNERABILITY, ((Double) mainRouter
 						.getProperty(Router.PROPERTY_VULNERABILITY)) / 2);
 		mainRouter.setProperties(routerProperties);
 
 		// Updates the server vulnerability
 		HashMap<String, Object> webServerProperties = new HashMap<String, Object>();
 		webServerProperties
 				.put(Server.PROPERTY_VULNERABILITY, ((Double) webServer
 						.getProperty(Server.PROPERTY_VULNERABILITY)) / 2);
 		webServer.setProperties(webServerProperties);
 
 		// Updates the sqlServer
 		HashMap<String, Object> sqlServerProperties = new HashMap<String, Object>();
 		sqlServerProperties
 				.put(Server.PROPERTY_VULNERABILITY, ((Double) sqlServer
 						.getProperty(Server.PROPERTY_VULNERABILITY)) / 2);
 		sqlServer.setProperties(sqlServerProperties);
 	}
 
 	private void repair(ShanksSimulation sim) {
 		RepairComputer action = new RepairComputer(this.getID() + "Repair", this);
 		// Is this the correct way??
 		try {
 			action.executeAction(sim, this, null);
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 }
