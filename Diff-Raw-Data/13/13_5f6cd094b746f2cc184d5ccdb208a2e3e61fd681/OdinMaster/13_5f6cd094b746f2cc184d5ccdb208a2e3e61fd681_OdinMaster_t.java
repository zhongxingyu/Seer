 package net.floodlightcontroller.odin.master;
 
 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.net.InetAddress;
 import java.net.InetSocketAddress;
 import java.net.UnknownHostException;
 import java.nio.ByteBuffer;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.Map.Entry;
 import java.util.TreeSet;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentMap;
 import java.util.concurrent.ScheduledExecutorService;
 import java.util.concurrent.TimeUnit;
 
 import org.openflow.protocol.OFMessage;
 import org.openflow.protocol.OFType;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import net.floodlightcontroller.core.FloodlightContext;
 import net.floodlightcontroller.core.IFloodlightProviderService;
 import net.floodlightcontroller.core.IOFMessageListener;
 import net.floodlightcontroller.core.IOFSwitch;
 import net.floodlightcontroller.core.IOFSwitchListener;
 import net.floodlightcontroller.core.module.FloodlightModuleContext;
 import net.floodlightcontroller.core.module.FloodlightModuleException;
 import net.floodlightcontroller.core.module.IFloodlightModule;
 import net.floodlightcontroller.core.module.IFloodlightService;
 import net.floodlightcontroller.packet.DHCP;
 import net.floodlightcontroller.packet.Ethernet;
 import net.floodlightcontroller.packet.IPacket;
 import net.floodlightcontroller.restserver.IRestApiService;
 import net.floodlightcontroller.threadpool.IThreadPoolService;
 import net.floodlightcontroller.util.MACAddress;
 
 
 /**
  * OdinMaster implementation. Exposes interfaces to OdinApplications,
  * and keeps track of agents and clients in the system.
  * 
  * @author Lalith Suresh <suresh.lalith@gmail.com>
  *
  */
 public class OdinMaster implements IFloodlightModule, IOFSwitchListener, IOdinApplicationInterface, IOFMessageListener, IFloodlightService {
 	protected static Logger log = LoggerFactory.getLogger(OdinMaster.class);
 	protected IRestApiService restApi;
 
 	private IFloodlightProviderService floodlightProvider;
 	private ScheduledExecutorService executor;
 	
 	private final AgentManager agentManager;
 	private final ClientManager clientManager;	
 	private final LvapManager lvapManager;
 	private final PoolManager poolManager;
 	
 	private long subscriptionId = 0;
 	private String subscriptionList = "";
 	private int idleLvapTimeout = 60; // Seconds
 	
 	private final ConcurrentMap<Long, SubscriptionCallbackTuple> subscriptions = new ConcurrentHashMap<Long, SubscriptionCallbackTuple>();
 
 	// some defaults
 	static private final String DEFAULT_POOL_FILE = "poolfile"; 
 	static private final String DEFAULT_CLIENT_LIST_FILE = "odin_client_list";  
 	static private final int DEFAULT_PORT = 2819;
 	
 	public OdinMaster(){
 		clientManager = new ClientManager();
 		lvapManager = new LvapManager();
 		poolManager = new PoolManager();
 		agentManager = new AgentManager(clientManager, poolManager);
 	}
 	
 	public OdinMaster(AgentManager agentManager, ClientManager clientManager, LvapManager lvapManager, PoolManager poolManager){
 		this.agentManager = agentManager;
 		this.clientManager = clientManager;
 		this.lvapManager = lvapManager;
 		this.poolManager = poolManager;
 	}
 	
 	
 	//********* Odin Agent->Master protocol handlers *********//
 	
 	/**
 	 * Handle a ping from an agent
 	 * 
 	 * @param InetAddress of the agent
 	 */
 	synchronized void receivePing (final InetAddress odinAgentAddr) {
 		if (agentManager.receivePing(odinAgentAddr)) {
 			// if the above leads to a new agent being
 			// tracked, push the current subscription list
 			// to it.
 			IOdinAgent agent = agentManager.getAgent(odinAgentAddr);
 			pushSubscriptionListToAgent(agent);
 
 			// Reclaim idle lvaps and also attach flows to lvaps
 			for (OdinClient client: agent.getLvapsLocal()) {
 				executor.schedule(new IdleLvapReclaimTask(client), idleLvapTimeout, TimeUnit.SECONDS);
 				
 				// Assign flow tables
 				if (!client.getIpAddress().getHostAddress().equals("0.0.0.0")) {
 					
 					// Obtain reference to client entity from clientManager, because agent.getLvapsLocal()
 					// returns a separate copy of the client objects.
 					OdinClient trackedClient = clientManager.getClients().get(client.getMacAddress());
 					Lvap lvap = trackedClient.getLvap();
 					assert (lvap != null);
 					lvap.setOFMessageList(lvapManager.getDefaultOFModList(client.getIpAddress()));
 					
 					// Push flow messages associated with the client
         			try {
         				lvap.getAgent().getSwitch().write(lvap.getOFMessageList(), null);
         			} catch (IOException e) {
         				log.error("Failed to update switch's flow tables " + lvap.getAgent().getSwitch());
         			}
 				}
 			}
 		}
 		else {
 			updateAgentLastHeard (odinAgentAddr);
 		}
 	}
 	
 	/**
 	 * Handle a probe message from an agent, triggered
 	 * by a particular client.
 	 * 
 	 * @param odinAgentAddr InetAddress of agent
 	 * @param clientHwAddress MAC address of client that performed probe scan
 	 */
 	synchronized void receiveProbe (final InetAddress odinAgentAddr, final MACAddress clientHwAddress, String ssid) {
 		
 		if (odinAgentAddr == null
 	    	|| clientHwAddress == null
 	    	|| clientHwAddress.isBroadcast()
 	    	|| clientHwAddress.isMulticast()
 	    	|| agentManager.isTracked(odinAgentAddr) == false
 	    	|| poolManager.getNumNetworks() == 0) {
 			return;
 		}
 		
 		updateAgentLastHeard(odinAgentAddr);
 		
 		/*
 		 * If clients perform an active scan, generate
 		 * probe responses without spawning lvaps
 		 */
 		if (ssid.equals("")) {
 			// we just send probe responses
 			IOdinAgent agent = agentManager.getAgent(odinAgentAddr);
 			MACAddress bssid = poolManager.generateBssidForClient(clientHwAddress);
 			
 			// FIXME: Sub-optimal. We'll end up generating redundant probe requests
 			Set<String> ssidSet = new TreeSet<String> ();
 			for (String pool: poolManager.getPoolsForAgent(odinAgentAddr)) {
 
 				if (pool.equals(PoolManager.GLOBAL_POOL))
 					continue;
 				
 				ssidSet.addAll(poolManager.getSsidListForPool(pool));
 			}
 			
 			executor.execute(new OdinAgentSendProbeResponseRunnable(agent, clientHwAddress, bssid, ssidSet));
 			
 			return;
 		}
 				
 		/*
 		 * Client is scanning for a particular SSID. Verify
 		 * which pool is hosting the SSID, and assign
 		 * an LVAP into that pool
 		 */
 		for (String pool: poolManager.getPoolsForAgent(odinAgentAddr)) {
 			if (poolManager.getSsidListForPool(pool).contains(ssid)) {
 				OdinClient oc = clientManager.getClient(clientHwAddress);
 		    	
 		    	// Hearing from this client for the first time
 		    	if (oc == null) {		    		
 					List<String> ssidList = new ArrayList<String> ();
 					ssidList.addAll(poolManager.getSsidListForPool(pool));
 					
 					Lvap lvap = new Lvap (poolManager.generateBssidForClient(clientHwAddress), ssidList);
 					
 					try {
 						oc = new OdinClient(clientHwAddress, InetAddress.getByName("0.0.0.0"), lvap);
 					} catch (UnknownHostException e) {
 						e.printStackTrace();
 					}
 		    		clientManager.addClient(oc);
 		    	}
 		    	
 		    	Lvap lvap = oc.getLvap();
 		    	assert (lvap != null);
 		    	
 				if (lvap.getAgent() == null) {
 					// client is connecting for the
 					// first time, had explicitly
 					// disconnected, or knocked
 					// out at as a result of an agent
 					// failure.
 					
 					// Use global pool for first time connections
 					handoffClientToApInternal(PoolManager.GLOBAL_POOL, clientHwAddress, odinAgentAddr);
 				}
 				
 				poolManager.mapClientToPool(oc, pool);
 				
 				return;
 			}
 		}
 	}
 	
 	/**
 	 * Handle an event publication from an agent
 	 * 
 	 * @param clientHwAddress client which triggered the event
 	 * @param odinAgentAddr agent at which the event was triggered
 	 * @param subscriptionIds list of subscription Ids that the event matches
 	 */
 	synchronized void receivePublish (final MACAddress clientHwAddress, final InetAddress odinAgentAddr, final Map<Long, Long> subscriptionIds) {
 
 		// The check for null clientHwAddress might go away
 		// in the future if we end up having events
 		// that are not related to clients at all.
 		if (clientHwAddress == null || odinAgentAddr == null || subscriptionIds == null)
 			return;
 		
 		IOdinAgent oa = agentManager.getAgent(odinAgentAddr);
 		
 		// This should never happen!
 		if (oa == null)
 			return;
 
 		// Update last-heard for failure detection
 		oa.setLastHeard(System.currentTimeMillis());
 		
 		for (Entry<Long, Long> entry: subscriptionIds.entrySet()) {
 			SubscriptionCallbackTuple tup = subscriptions.get(entry.getKey());
 			
 			/* This might occur as a race condition when the master
 			 * has cleared all subscriptions, but hasn't notified
 			 * the agent about it yet.
 			 */
 			if (tup == null)
 				continue;
 
 
 			NotificationCallbackContext cntx = new NotificationCallbackContext(clientHwAddress, oa, entry.getValue());
 			
 			tup.cb.exec(tup.oes, cntx);
 		}
 	}
 
 	
 	/**
 	 * VAP-Handoff a client to a new AP. This operation is idempotent.
 	 * 
 	 * @param newApIpAddr IPv4 address of new access point
 	 * @param hwAddrSta Ethernet address of STA to be handed off
 	 */
 	private void handoffClientToApInternal (String pool, final MACAddress clientHwAddr, final InetAddress newApIpAddr){
 		
 		// As an optimisation, we probably need to get the accessing done first,
 		// prime both nodes, and complete a handoff. 
 		
 		if (pool == null || clientHwAddr == null || newApIpAddr == null) {
 			log.error("null argument in handoffClientToAp(): pool:" + pool + "clientHwAddr: " + clientHwAddr + " newApIpAddr: " + newApIpAddr);
 			return;
 		}
 		
 		synchronized (this) {
 		
 			IOdinAgent newAgent = agentManager.getAgent(newApIpAddr);
 			
 			// If new agent doesn't exist, ignore request
 			if (newAgent == null) {
 				log.error("Handoff request ignored: OdinAgent " + newApIpAddr + " doesn't exist");
 				return;
 			}
 			
 			OdinClient client = clientManager.getClient(clientHwAddr);
 			
 			// Ignore request if we don't know the client
 			if (client == null) {
 				log.error("Handoff request ignored: OdinClient " + clientHwAddr + " doesn't exist");
 				return;
 			}
 			
 			Lvap lvap = client.getLvap();
 			
 			assert (lvap != null);
 			
 			/* If the client is connecting for the first time, then it
 			 * doesn't have a VAP associated with it already
 			 */
 			if (lvap.getAgent() == null) {
 				log.info ("Client: " + clientHwAddr + " connecting for first time. Assigning to: " + newAgent.getIpAddress());
 	
 				// Push flow messages associated with the client
 				try {
 					newAgent.getSwitch().write(lvap.getOFMessageList(), null);
 				} catch (IOException e) {
 					log.error("Failed to update switch's flow tables " + newAgent.getSwitch());
 				}
 	
 				newAgent.addClientLvap(client);
 				lvap.setAgent(newAgent);
 				executor.schedule(new IdleLvapReclaimTask (client), idleLvapTimeout, TimeUnit.SECONDS);
 				return;
 			}
 			
 			/* If the client is already associated with AP-newIpAddr, we ignore
 			 * the request.
 			 */
 			InetAddress currentApIpAddress = lvap.getAgent().getIpAddress();
 			if (currentApIpAddress.getHostAddress().equals(newApIpAddr.getHostAddress())) {
 				log.info ("Client " + clientHwAddr + " is already associated with AP " + newApIpAddr);
 				return;
 			}
 			
 			/* Verify permissions.
 			 * 
 			 * - newAP and oldAP should both fall within the same pool.
 			 * - client should be within the same pool as the two APs.
 			 * - invoking application should be operating on the same pools
 			 *  
 			 * By design, this prevents handoffs within the scope of the
 			 * GLOBAL_POOL since that would violate a lot of invariants
 			 * in the rest of the system.
 			 */
 			
 			String clientPool = poolManager.getPoolForClient(client);
 			
 			if (clientPool == null || !clientPool.equals(pool)) {
 				log.error ("Cannot handoff client '" + client.getMacAddress() + "' from " + clientPool + " domain when in domain: '" + pool + "'");
 			}
 			
 			if (! (poolManager.getPoolsForAgent(newApIpAddr).contains(pool)
 					&& poolManager.getPoolsForAgent(currentApIpAddress).contains(pool)) ){
 				log.info ("Agents " + newApIpAddr + " and " + currentApIpAddress + " are not in the same pool: " + pool);
 				return;
 			}
 			
 			// Push flow messages associated with the client
 			try {
 				newAgent.getSwitch().write(lvap.getOFMessageList(), null);
 			} catch (IOException e) {
 				log.error("Failed to update switch's flow tables " + newAgent.getSwitch());
 			}
 			
 			/* Client is with another AP. We remove the VAP from
 			 * the current AP of the client, and spawn it on the new one.
 			 * We split the add and remove VAP operations across two threads
 			 * to make it faster. Note that there is a temporary inconsistent 
 			 * state between setting the agent for the client and it actually 
 			 * being reflected in the network 
 			 */
 			lvap.setAgent(newAgent);
 			executor.execute(new OdinAgentLvapAddRunnable(newAgent, client));
 			executor.execute(new OdinAgentLvapRemoveRunnable(agentManager.getAgent(currentApIpAddress), client));
 		}
 	}
 	
 	//********* Odin methods to be used by applications (from IOdinApplicationInterface) **********//
 	
 	/**
 	 * VAP-Handoff a client to a new AP. This operation is idempotent.
 	 * 
 	 * @param newApIpAddr IPv4 address of new access point
 	 * @param hwAddrSta Ethernet address of STA to be handed off
 	 */
 	@Override
 	public void handoffClientToAp (String pool, final MACAddress clientHwAddr, final InetAddress newApIpAddr){
 		handoffClientToApInternal(pool, clientHwAddr, newApIpAddr);
 	}
 	
 	
 	/**
 	 * Get the list of clients currently registered with Odin
 	 * 
 	 * @return a map of OdinClient objects keyed by HW Addresses
 	 */
 	@Override
 	public Set<OdinClient> getClients (String pool) {
 		return poolManager.getClientsFromPool(pool);
 	}
 	
 	
 	/**
 	 * Get the OdinClient type from the client's MACAddress
 	 * 
 	 * @param pool that the invoking application corresponds to
 	 * @param clientHwAddress MACAddress of the client
 	 * @return a OdinClient instance corresponding to clientHwAddress
 	 */
 	@Override
 	public OdinClient getClientFromHwAddress (String pool, MACAddress clientHwAddress) {
 		OdinClient client = clientManager.getClient(clientHwAddress);
 		return (client != null && poolManager.getPoolForClient(client).equals(pool)) ? client : null;
 	}
 	
 	
 	/**
 	 * Retreive RxStats from the agent
 	 * 
 	 * @param pool that the invoking application corresponds to
 	 * @param agentAddr InetAddress of the agent
 	 * 
 	 * @return Key-Value entries of each recorded statistic for each client 
 	 */
 	@Override
 	public Map<MACAddress, Map<String, String>> getRxStatsFromAgent (String pool, InetAddress agentAddr) {
 		return agentManager.getAgent(agentAddr).getRxStats();		
 	}
 	
 	
 	/**
 	 * Get a list of Odin agents from the agent tracker
 	 * @return a map of OdinAgent objects keyed by Ipv4 addresses
 	 */
 	@Override
 	public Set<InetAddress> getAgentAddrs (String pool){
 		return poolManager.getAgentAddrsForPool(pool);
 	}
 	
 	
 	/**
 	 * Add a subscription for a particular event defined by oes. cb
 	 * defines the application specified callback to be invoked during
 	 * notification. If the application plans to delete the subscription,
 	 * later, the onus is upon it to keep track of the subscription
 	 * id for removal later.
 	 * 
 	 * @param oes the susbcription
 	 * @param cb the callback
 	 */
 	@Override
 	public synchronized long registerSubscription (String pool, final OdinEventSubscription oes, final NotificationCallback cb) {
 		// FIXME: Need to calculate subscriptions per pool
 		assert (oes != null);
 		assert (cb != null);
 		SubscriptionCallbackTuple tup = new SubscriptionCallbackTuple();
 		tup.oes = oes;
 		tup.cb = cb;
 		subscriptionId++;
 		subscriptions.put(subscriptionId, tup);
 		
 		/**
 		 * Update the subscription list, and push to all agents
 		 * TODO: This is a common subsription string being
 		 * sent to all agents. Replace this with per-agent
 		 * subscriptions.
 		 */
 		subscriptionList = "";
 		int count = 0;
 		for (Entry<Long, SubscriptionCallbackTuple> entry: subscriptions.entrySet()) {
 			count++;
 			final String addr = entry.getValue().oes.getClient();
 			subscriptionList = subscriptionList + 
 								entry.getKey() + " " + 
 								(addr.equals("*") ? MACAddress.valueOf("00:00:00:00:00:00") : addr)  + " " +
 								entry.getValue().oes.getStatistic() + " " +
 								entry.getValue().oes.getRelation().ordinal() + " " +
 								entry.getValue().oes.getValue() + " ";
 		}
 
 		subscriptionList = String.valueOf(count) + " " + subscriptionList;
 
 		/**
 		 * Should probably have threads to do this
 		 */
 		for (InetAddress agentAddr : poolManager.getAgentAddrsForPool(pool)) {
 			pushSubscriptionListToAgent(agentManager.getAgent(agentAddr));
 		}
 		
 		return subscriptionId;
 	}
 	
 	
 	/**
 	 * Remove a subscription from the list
 	 * 
 	 * @param id subscription id to remove
 	 * @return
 	 */
 	@Override
 	public synchronized void unregisterSubscription (String pool, final long id) {
 		// FIXME: Need to calculate subscriptions per pool
 		subscriptions.remove(id);
 		
 		subscriptionList = "";
 		int count = 0;
 		for (Entry<Long, SubscriptionCallbackTuple> entry: subscriptions.entrySet()) {
 			count++;
 			final String addr = entry.getValue().oes.getClient();
 			subscriptionList = subscriptionList + 
 								entry.getKey() + " " + 
 								(addr.equals("*") ? MACAddress.valueOf("00:00:00:00:00:00") : addr)  + " " +
 								entry.getValue().oes.getStatistic() + " " +
 								entry.getValue().oes.getRelation().ordinal() + " " +
 								entry.getValue().oes.getValue() + " ";
 		}
 
 		subscriptionList = String.valueOf(count) + " " + subscriptionList;
 
 		/**
 		 * Should probably have threads to do this
 		 */
 		for (InetAddress agentAddr : poolManager.getAgentAddrsForPool(pool)) {
 			pushSubscriptionListToAgent(agentManager.getAgent(agentAddr));
 		}
 	}
 	
 
 	/**
 	 * Add an SSID to the Odin network.
 	 * 
 	 * @param networkName
 	 * @return true if the network could be added, false otherwise
 	 */
 	@Override
 	public synchronized boolean addNetwork (String pool, String ssid) {
 		if (poolManager.addNetworkForPool(pool, ssid)) {
 			
 			for(OdinClient oc: poolManager.getClientsFromPool(pool)) {
 				Lvap lvap = oc.getLvap();
 				assert (lvap != null);
 				lvap.getSsids().add(ssid);
 				
 				IOdinAgent agent = lvap.getAgent();
 				
 				if (agent != null) {
 					// FIXME: Ugly API
 					agent.updateClientLvap(oc);
 				}
 			}
 			
 			return true;
 		}
 		
 		return false;
 	}
 	
 	
 	/**
 	 * Remove an SSID from the Odin network.
 	 * 
 	 * @param networkName
 	 * @return true if the network could be removed, false otherwise
 	 */
 	@Override
 	public synchronized boolean removeNetwork (String pool, String ssid) {
 		if (poolManager.removeNetworkFromPool(pool, ssid)){
 			// need to update all existing lvaps in the network as well
 			
 			for (OdinClient oc: poolManager.getClientsFromPool(pool)) {
 				
 				Lvap lvap = oc.getLvap();
 				assert (lvap != null);
 				lvap.getSsids().remove(ssid);
 				
 				IOdinAgent agent = lvap.getAgent();
 				
 				if (agent != null) {
 					// FIXME: Ugly API
 					agent.updateClientLvap(oc);
 				}
 			}
 			
 			return true;
 		}
 			
 		return false;
 	}
 	
 	
 	//********* from IFloodlightModule **********//
 	
 	@Override
 	public Collection<Class<? extends IFloodlightService>> getModuleDependencies() {
 		Collection<Class<? extends IFloodlightService>> l =
 	        new ArrayList<Class<? extends IFloodlightService>>();
 	    l.add(IFloodlightProviderService.class);
         l.add(IRestApiService.class);
 		return l;
 	}
 
 	@Override
 	public Collection<Class<? extends IFloodlightService>> getModuleServices() {
 		return null;
 	}
 
 	@Override
 	public Map<Class<? extends IFloodlightService>, IFloodlightService> getServiceImpls() {
 		Map<Class<? extends IFloodlightService>,
         IFloodlightService> m =
         new HashMap<Class<? extends IFloodlightService>,
         IFloodlightService>();
         m.put(OdinMaster.class, this);
         return m;
 	}
 
 	@Override
 	public void init(FloodlightModuleContext context)
 			throws FloodlightModuleException {
 		floodlightProvider = context.getServiceImpl(IFloodlightProviderService.class);
 		restApi = context.getServiceImpl(IRestApiService.class);
 		IThreadPoolService tp = context.getServiceImpl(IThreadPoolService.class);
 		executor = tp.getScheduledExecutor();
 	}
 
 	@Override
 	public void startUp(FloodlightModuleContext context) {		
 		floodlightProvider.addOFSwitchListener(this);
 		floodlightProvider.addOFMessageListener(OFType.PACKET_IN, this);
 		restApi.addRestletRoutable(new OdinMasterWebRoutable());
 		
 		agentManager.setFloodlightProvider (floodlightProvider);
 		
 		// read config options
         Map<String, String> configOptions = context.getConfigParams(this);
         
         
         // List of trusted agents
         String agentAuthListFile = DEFAULT_POOL_FILE;
         String agentAuthListFileConfig = configOptions.get("poolFile");
         
         if (agentAuthListFileConfig != null) {
         	agentAuthListFile = agentAuthListFileConfig; 
         }
         
         List<OdinApplication> applicationList = new ArrayList<OdinApplication>();
        	try {
 			BufferedReader br = new BufferedReader (new FileReader(agentAuthListFile));
 			
 			String strLine;
 			
 			/* Each line has the following format:
 			 * 
 			 * IPAddr-of-agent  pool1 pool2 pool3 ...
 			 */
 			while ((strLine = br.readLine()) != null) {
 				if (strLine.startsWith("#")) // comment
 					continue;
 				
 				if (strLine.length() == 0) // blank line
 					continue;
 				
 				// NAME
 				String [] fields = strLine.split(" "); 
 				if (!fields[0].equals("NAME")) {
 					log.error("Missing NAME field " + fields[0]);
 					log.error("Offending line: " + strLine);
 					System.exit(1);
 				}
 				
 				if (fields.length != 2) {
 					log.error("A NAME field should specify a single string as a pool name");
 					log.error("Offending line: " + strLine);
 					System.exit(1);
 				}
 
 				String poolName = fields[1];
 				
 				// NODES
 				strLine = br.readLine();
 				
 				if (strLine == null) {
 					log.error("Unexpected EOF after NAME field for pool: " + poolName);
 					System.exit(1);
 				}
 				
 				fields = strLine.split(" ");
 				
 				if (!fields[0].equals("NODES")){
 					log.error("A NAME field should be followed by a NODES field");
 					log.error("Offending line: " + strLine);
 					System.exit(1);
 				}
 				
 				if(fields.length == 1) {				
 					log.error("A pool must have at least one node defined for it");
 					log.error("Offending line: " + strLine);
 					System.exit(1);
 				}
 				
 				for (int i = 1; i < fields.length; i++) {
 					poolManager.addPoolForAgent(InetAddress.getByName(fields[i]), poolName);
 				}
 				
 				// NETWORKS
 				strLine = br.readLine();
 				
 				if (strLine == null) {
 					log.error("Unexpected EOF after NODES field for pool: " + poolName);
 					System.exit(1);
 				}
 
 				fields = strLine.split(" ");
 				
 				if (!fields[0].equals("NETWORKS")) {
 					log.error("A NODES field should be followed by a NETWORKS field");
 					log.error("Offending line: " + strLine);
 					System.exit(1);
 				}
 				
 				for (int i = 1; i < fields.length; i++) {
 					poolManager.addNetworkForPool(poolName, fields[i]);
 				}
 				
 				// APPLICATIONS
 				strLine = br.readLine();
 				
 				if (strLine == null) {
 					log.error("Unexpected EOF after NETWORKS field for pool: " + poolName);
 					System.exit(1);
 				}
 
 				fields = strLine.split(" ");
 				
 				if (!fields[0].equals("APPLICATIONS")) {
 					log.error("A NETWORKS field should be followed by an APPLICATIONS field");
 					log.error("Offending line: " + strLine);
 					System.exit(1);
 				}
 				
 				for (int i = 1; i < fields.length; i++) {
 					OdinApplication appInstance = (OdinApplication) Class.forName(fields[i]).newInstance();
 					appInstance.setOdinInterface(this);
 					appInstance.setPool(poolName);
 					applicationList.add(appInstance);
 				}
 			}
 			
      br.close();

 		} catch (FileNotFoundException e1) {
 			log.error("Agent authentication list (config option poolFile) not supplied. Terminating.");
 			System.exit(1);
 		} catch (IOException e) {
 			e.printStackTrace();
 			System.exit(1);
 		} catch (InstantiationException e) {
 			e.printStackTrace();
 		} catch (IllegalAccessException e) {			
 			e.printStackTrace();
 		} catch (ClassNotFoundException e) {
 			e.printStackTrace();
 		}
 
         // Static client - lvap assignments
         String clientListFile = DEFAULT_CLIENT_LIST_FILE;
         String clientListFileConfig = configOptions.get("clientList");
         
         if (clientListFileConfig != null) {
             clientListFile = clientListFileConfig;
         }
         
         try {
 			BufferedReader br = new BufferedReader (new FileReader(clientListFile));
 			
 			String strLine;
 			
 			while ((strLine = br.readLine()) != null) {
 				String [] fields = strLine.split(" ");
 				
 				MACAddress hwAddress = MACAddress.valueOf(fields[0]);
 				InetAddress ipaddr = InetAddress.getByName(fields[1]);
 				
 				ArrayList<String> ssidList = new ArrayList<String> ();
 				ssidList.add(fields[3]); // FIXME: assumes a single ssid
 				Lvap lvap = new Lvap(MACAddress.valueOf(fields[2]), ssidList);
 
 				log.info("Adding client: " + fields[0] + " " + fields[1] + " " +fields[2] + " " +fields[3]);
 				clientManager.addClient(hwAddress, ipaddr, lvap);
 				lvap.setOFMessageList(lvapManager.getDefaultOFModList(ipaddr));
 			}

      br.close();

 		} catch (FileNotFoundException e) {
 			// skip
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
         // Lvap timeout, port, and ssid-list
         String timeoutStr = configOptions.get("idleLvapTimeout");
         if (timeoutStr != null) {
         	int timeout = Integer.parseInt(timeoutStr);
         	
         	if (timeout > 0) {
         		idleLvapTimeout = timeout;
         	}
         }
         
         int port = DEFAULT_PORT;
         String portNum = configOptions.get("masterPort");
         if (portNum != null) {
             port = Integer.parseInt(portNum);
         }
         
        IThreadPoolService tp = context.getServiceImpl(IThreadPoolService.class);
        executor = tp.getScheduledExecutor();
         // Spawn threads for different services
         executor.execute(new OdinAgentProtocolServer(this, port, executor));
         
         // Spawn applications
         for (OdinApplication app: applicationList) {
         	executor.execute(app);
         }
 	}
 
 	/** IOFSwitchListener methods **/
 	
 	@Override
 	public void addedSwitch(IOFSwitch sw) {
 		// inform-agent manager
 	}
 
 	@Override
 	public String getName() {
 		return "OdinMaster";
 	}
 
 	@Override
 	public void removedSwitch(IOFSwitch sw) {
 		// Not all OF switches are Odin agents. We should immediately remove
 		// any associated Odin agent then.		
 		final InetAddress switchIpAddr = ((InetSocketAddress) sw.getChannel().getRemoteAddress()).getAddress();
 		agentManager.removeAgent(switchIpAddr);		
 	}
 
 	@Override
 	public Command receive(IOFSwitch sw, OFMessage msg, FloodlightContext cntx) {
 		
 		// We use this to pick up DHCP response frames
 		// and update a client's IP address details accordingly
 		
 		Ethernet frame = IFloodlightProviderService.bcStore.get(cntx, 
                 IFloodlightProviderService.CONTEXT_PI_PAYLOAD);
 
 		IPacket payload = frame.getPayload(); // IP
         if (payload == null)
         	return Command.CONTINUE;
         
         IPacket p2 = payload.getPayload(); // TCP or UDP
         
         if (p2 == null) 
         	return Command.CONTINUE;
         
         IPacket p3 = p2.getPayload(); // Application
         if ((p3 != null) && (p3 instanceof DHCP)) {
         	DHCP packet = (DHCP) p3;
         	try {
 
         		final MACAddress clientHwAddr = MACAddress.valueOf(packet.getClientHardwareAddress());
         		final OdinClient oc = clientManager.getClients().get(clientHwAddr);
         		
     			// Don't bother if we're not tracking the client
         		// or if the client is unassociated with the agent
         		// or the agent's switch hasn't been registered yet
         		if (oc == null || oc.getLvap().getAgent() == null || oc.getLvap().getAgent().getSwitch() == null) {
         			return Command.CONTINUE;
         		}
         		
         		// Look for the Your-IP field in the DHCP packet
         		if (packet.getYourIPAddress() != 0) {
         			
         			// int -> byte array -> InetAddr
         			final byte[] arr = ByteBuffer.allocate(4).putInt(packet.getYourIPAddress()).array();
         			final InetAddress yourIp = InetAddress.getByAddress(arr);
         			
         			// No need to invoke agent update protocol if the node
         			// is assigned the same IP
         			if (yourIp.equals(oc.getIpAddress())) {
         				return Command.CONTINUE;
         			}
         			
         			log.info("Updating client: " + clientHwAddr + " with ipAddr: " + yourIp);
         			oc.setIpAddress(yourIp);
         			oc.getLvap().setOFMessageList(lvapManager.getDefaultOFModList(yourIp));
         			
         			// Push flow messages associated with the client
         			try {
         				oc.getLvap().getAgent().getSwitch().write(oc.getLvap().getOFMessageList(), null);
         			} catch (IOException e) {
         				log.error("Failed to update switch's flow tables " + oc.getLvap().getAgent().getSwitch());
         			}
         			oc.getLvap().getAgent().updateClientLvap(oc);
         		}
         		
 			} catch (UnknownHostException e) {
 				// Shouldn't ever happen
 				e.printStackTrace();
 			}
         }
 			
 		return Command.CONTINUE;
 	}
 
 	@Override
 	public boolean isCallbackOrderingPostreq(OFType type, String name) {
 		return false;
 	}
 
 	@Override
 	public boolean isCallbackOrderingPrereq(OFType type, String name) {
 		return false;
 	}
 	
 	/**
 	 * Push the subscription list to the agent
 	 * 
 	 * @param oa agent to push subscription list to
 	 */
 	private void pushSubscriptionListToAgent (final IOdinAgent oa) {
 		oa.setSubscriptions(subscriptionList);
 	}
 
 	private void updateAgentLastHeard (InetAddress odinAgentAddr) {
 		IOdinAgent agent = agentManager.getAgent(odinAgentAddr);
 		
 		if (agent != null) {
 			// Update last-heard for failure detection
 			agent.setLastHeard(System.currentTimeMillis());
 		}
 	}
 	
 	private class OdinAgentLvapAddRunnable implements Runnable {
 		final IOdinAgent oa;
 		final OdinClient oc;
 		
 		OdinAgentLvapAddRunnable(IOdinAgent newAgent, OdinClient oc) {
 			this.oa = newAgent;
 			this.oc = oc;
 		}
 		@Override
 		public void run() {
 			oa.addClientLvap(oc);
 		}
 		
 	}
 	
 	private class OdinAgentLvapRemoveRunnable implements Runnable {
 		final IOdinAgent oa;
 		final OdinClient oc;
 		
 		OdinAgentLvapRemoveRunnable(IOdinAgent oa, OdinClient oc) {
 			this.oa = oa;
 			this.oc = oc;
 		}
 		@Override
 		public void run() {
 			oa.removeClientLvap(oc);
 		}
 		
 	}
 	
 	private class OdinAgentSendProbeResponseRunnable implements Runnable {
 		final IOdinAgent oa;
 		final MACAddress clientHwAddr;
 		final MACAddress bssid;
 		final Set<String> ssidList;
 		
 		OdinAgentSendProbeResponseRunnable(IOdinAgent oa, MACAddress clientHwAddr, MACAddress bssid, Set<String> ssidList) {
 			this.oa = oa;
 			this.clientHwAddr = clientHwAddr;
 			this.bssid = bssid;
 			this.ssidList = ssidList;
 		}
 		@Override
 		public void run() {
 			oa.sendProbeResponse(clientHwAddr, bssid, ssidList);
 		}
 		
 	}
 	
 	private class IdleLvapReclaimTask implements Runnable {
 		private final OdinClient oc;
 		
 		IdleLvapReclaimTask(final OdinClient oc) {
 			this.oc = oc;
 		}
 		
 		@Override
 		public void run() {
 			OdinClient client = clientManager.getClients().get(oc.getMacAddress());
 			
 			if (client == null) {
 				return;
 			}
 			
 			// Client didn't follow through to connect
 			try {
 				if (client.getIpAddress().equals(InetAddress.getByName("0.0.0.0"))) {
 					IOdinAgent agent = client.getLvap().getAgent();
 					
 					if (agent != null) {
 						log.info("Clearing Lvap " + client.getMacAddress() + 
 								" from agent:" + agent.getIpAddress() + " due to inactivity");
 						poolManager.removeClientPoolMapping(client);
 						agent.removeClientLvap(client);
 						clientManager.removeClient(client.getMacAddress());
 					}
 				}
 			} catch (UnknownHostException e) {
 				// skip
 			}
 		}
 	}
 
 	private class SubscriptionCallbackTuple {
 		OdinEventSubscription oes;
 		NotificationCallback cb;
 	}
}
