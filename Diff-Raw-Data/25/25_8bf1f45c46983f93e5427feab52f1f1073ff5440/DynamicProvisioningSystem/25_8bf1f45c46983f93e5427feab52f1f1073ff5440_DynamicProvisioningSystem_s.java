 package provisioning;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.TreeMap;
 
 import commons.cloud.MachineType;
 import commons.cloud.Provider;
 import commons.cloud.Request;
 import commons.cloud.User;
 import commons.cloud.UtilityResult;
 import commons.config.Configuration;
 import commons.io.GEISTWorkloadParser;
 import commons.io.TimeBasedWorkloadParser;
 import commons.sim.AccountingSystem;
 import commons.sim.components.MachineDescriptor;
 import commons.sim.provisioningheuristics.RanjanStatistics;
 import commons.sim.util.SaaSAppProperties;
 
 /**
  * 
  * @author Ricardo Ara&uacute;jo Santos - ricardo@lsd.ufcg.edu.br
  */
 public class DynamicProvisioningSystem implements DPS{
 
 	protected long availableIDs;
 	
 	protected AccountingSystem accountingSystem;
 	
 	protected DynamicConfigurable configurable;
 	
 	protected final Map<Long, DynamicConfigurable> configurables;
 
	protected Map<String, User> users;
 	
 	protected Map<String, Provider> providers;
 	
 	/**
 	 * Default constructor.
 	 */
 	public DynamicProvisioningSystem() {
 		this.availableIDs = 0;
 		this.configurables = new HashMap<Long, DynamicConfigurable>();
 		
 		this.providers = new TreeMap<String, Provider>();
 		List<Provider> listOfProviders = Configuration.getInstance().getProviders();
 		for (Provider provider : listOfProviders) {
 			this.providers.put(provider.getName(), provider);
 		}
 		
		this.users = new HashMap<String, User>();
 		List<User> listOfUsers = Configuration.getInstance().getUsers();
 		for (User user : listOfUsers) {
 			this.users.put(user.getId(), user);
 		}
 		this.accountingSystem = new AccountingSystem();
 	}
 	
 	@Override
 	public void registerConfigurable(DynamicConfigurable configurable) {
 		this.configurable = configurable;
 		int[] initialServersPerTier = Configuration.getInstance().getIntegerArray(SaaSAppProperties.APPLICATION_INITIAL_SERVER_PER_TIER);
 		
 		List<MachineType> typeList = Arrays.asList(MachineType.values());
 		Collections.reverse(typeList);
 		//Looking for reserved instances!
 		for (int tier = 0; tier < initialServersPerTier.length; tier++) {
 			addServersToTier(configurable, tier, initialServersPerTier[tier], typeList);
 		}
 
 		String[] workloads = Configuration.getInstance().getWorkloads();
 		configurable.setWorkloadParser(new TimeBasedWorkloadParser(new GEISTWorkloadParser(workloads), TimeBasedWorkloadParser.DAY_IN_MILLIS));
 	}
 	
 	private void addServersToTier(DynamicConfigurable configurable, int tier, int numberOfInitialServers, List<MachineType> typeList) {
 		int serversAdded = 0;
 		for(MachineType machineType : typeList){
 			for (Provider provider : this.providers.values()) {
 				while(provider.canBuyMachine(true, machineType) && serversAdded < numberOfInitialServers){
 					configurable.addServer(tier, provider.buyMachine(true, machineType), false);
 					serversAdded++;
 				}
 				if(serversAdded == numberOfInitialServers){
 					return;
 				}
 			}
 		}
 	}
 
 	@Override
 	public void reportRequestFinished(Request request) {
 		Integer SaaSClientID = Integer.valueOf(request.getSaasClient());
 		if( !users.containsKey(SaaSClientID) ){
 			throw new RuntimeException("Unregistered user with ID " + request.getSaasClient() + ". Check configuration files.");
 		}
 		users.get(SaaSClientID).reportFinishedRequest(request);
 	}
 	
 	@Override
 	public void requestQueued(long timeMilliSeconds, Request request, int tier) {
 		reportLostRequest(request);
 	}
 
 	@Override
 	public void evaluateUtilisation(long now, RanjanStatistics statistics, int tier) {
 		
 	}
 
 	@Override
 	public void machineTurnedOff(MachineDescriptor machineDescriptor) {
 		for (Provider provider : providers.values()) {
 			if(provider.shutdownMachine(machineDescriptor)){
 				return;
 			}
 		}
 		throw new RuntimeException("No provider is responsible for machine " + machineDescriptor);
 	}
 
 	@Override
 	public UtilityResult calculateUtility() {
 		return this.accountingSystem.calculateUtility(users, providers);
 	}
 
 	@Override
 	public void chargeUsers(long currentTimeInMillis) {
 		this.accountingSystem.accountPartialUtility(currentTimeInMillis, users, providers);
 	}
 
 	/**
 	 * @param request
 	 */
 	protected void reportLostRequest(Request request) {
		if( !users.containsKey(request.getSaasClient()) ){
			throw new RuntimeException("Unregistered user with ID " + request.getSaasClient() + ". Check configuration files.");
 		}
		users.get(request.getSaasClient()).reportLostRequest(request);
 	}
 	
 	protected List<Provider> canBuyMachine(MachineType type, boolean isReserved){
 		List<Provider> available = new ArrayList<Provider>();
 		for (Entry<String, Provider> entry : providers.entrySet()) {
 			if(entry.getValue().canBuyMachine(isReserved, type)){
 				available.add(entry.getValue());
 			}
 		}
 		return available;
 	}
 
 	protected MachineDescriptor buyMachine(Provider provider, MachineType instanceType, boolean isReserved){
 		return provider.buyMachine(isReserved, instanceType);
 	}
 
 	@Override
 	public long getSimulationEndTime() {
 		return this.configurable.getSimulationEndTime();
 	}
 }
