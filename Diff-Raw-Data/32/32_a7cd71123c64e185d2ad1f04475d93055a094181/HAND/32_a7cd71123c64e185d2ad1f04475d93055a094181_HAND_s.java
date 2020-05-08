 package net.floodlightcontroller.hand;
 
 /**
  * HAND: Host Aware Network Decisions
  * "network decisions based on Ganglia metrics and clusters"
  * @author ryan wallner
  * 
  * ***All host that join HAND must be running GMOND*** in order to work.
  **/
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.EnumSet;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import org.openflow.protocol.OFFlowMod;
 import org.openflow.protocol.OFType;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import net.floodlightcontroller.core.IFloodlightProviderService;
 import net.floodlightcontroller.core.module.FloodlightModuleContext;
 import net.floodlightcontroller.core.module.FloodlightModuleException;
 import net.floodlightcontroller.core.module.IFloodlightModule;
 import net.floodlightcontroller.core.module.IFloodlightService;
 import net.floodlightcontroller.devicemanager.IDevice;
 import net.floodlightcontroller.devicemanager.IDeviceListener;
 import net.floodlightcontroller.devicemanager.IDeviceService;
 import net.floodlightcontroller.devicemanager.IEntityClass;
 import net.floodlightcontroller.firewall.Firewall;
 import net.floodlightcontroller.firewall.FirewallRule;
 import net.floodlightcontroller.firewall.FirewallWebRoutable;
 import net.floodlightcontroller.linkdiscovery.ILinkDiscovery.LDUpdate;
 import net.floodlightcontroller.packet.IPv4;
 import net.floodlightcontroller.qos.IQoSService;
 import net.floodlightcontroller.restserver.IRestApiService;
 import net.floodlightcontroller.staticflowentry.IStaticFlowEntryPusherService;
 import net.floodlightcontroller.storage.IStorageSourceService;
 import net.floodlightcontroller.topology.ITopologyListener;
 import net.floodlightcontroller.topology.ITopologyService;
 import net.floodlightcontroller.topology.NodePortTuple;
 
 public class HAND implements IHANDService, IFloodlightModule {
 	
 	// services needed
     protected IFloodlightProviderService floodlightProvider;
     protected IStaticFlowEntryPusherService flowPusher;
     protected IStorageSourceService storageSource;
     protected IRestApiService restApi;
     protected static Logger logger;
     
     protected ArrayList<HANDRule> hostRules;
     protected ArrayList<HANDGangliaHost> gangliaHosts;
     protected ArrayList<String> messages;
     
    
    //TODO create constants for storage/parsing
    //TODO IMPORTANT
    
     /**
      * Table for hosts in storage source
      */
     public static final String HOSTS_TABLE_NAME = "hand_hosts";
     public static final String COLUMN_HID = "hostid";
     public static final String COLUMN_CLUSTER = "cluster";
     public static final String COLUMN_NAME = "name";
     public static final String COLUMN_IPADD = "ip_address";
     public static final String COLUMN_MACADD = "mac_address";
     public static final String COLUMN_FIRSTHOP = "first_hop_switch";
     public static String HostColumnNames[] = {
     	COLUMN_HID,
     	COLUMN_CLUSTER,
     	COLUMN_NAME,
     	COLUMN_IPADD,
     	COLUMN_MACADD,
     	COLUMN_FIRSTHOP
     	};
     
     /**
      * Table for rules in storage source
      */
     public static final String RULES_TABLE_NAME = "hand_rules";
     public static final String COLUMN_RID = "ruleid";
     public static final String COLUMN_PRIOR = "priority";
     public static final String COLUMN_POLLTIME = "polling_time";
     public static final String COLUMN_TIMEADD = "time_added";
     public static final String COLUMN_NEXTCHECK = "next_check_time";
     public static final String COLUMN_ACTIVE = "active";
     public static final String COLUMN_ASSOC = "host_association";
     public static final String COLUMN_METRICS = "metrics";
     public static final String COLUMN_FRULE = "firewall_rule";
     public static final String COLUMN_QOS = "qos_rule";
     public static final String COLUMN_STATICFLOW = "static_flow";
     public static final String COLUMN_CHECKLIMIT = "check_limit";
     public static final String COLUMN_CURRCHECK = "current_check";
     public static final String COLUMN_ACTION = "action";
     public static String RuleColumnNames[] = {
     	COLUMN_RID,
     	COLUMN_PRIOR,
     	COLUMN_POLLTIME,
     	COLUMN_TIMEADD,
     	COLUMN_NEXTCHECK,
     	COLUMN_ACTIVE,
     	COLUMN_ASSOC,
     	COLUMN_METRICS,
     	COLUMN_FRULE,
     	COLUMN_QOS,
     	COLUMN_STATICFLOW,
     	COLUMN_CHECKLIMIT,
     	COLUMN_CURRCHECK,
     	COLUMN_ACTION,
     	};
     
     public boolean enabled;
     
     
     @Override
     public String getName(){
     	return "HostAwareNetorkingDecisions";
     }
     
     @Override
     public Boolean isHANDEnabled(){
     	return this.enabled;
     }
     
     @Override
     public void enableHAND(boolean enabled){
     	logger.info("Setting Host Aware Networking Decisions to {}", enabled);
     	this.enabled = enabled;
     	
     	// when enabled, we need to start the timed polling mechanism
     	if (enabled)
     		this.startTimerPollingTask();
     	if (!(enabled)){
     		logger.info("HAND disabled");
     	}
     }
     
     public ArrayList<String> getMessages(){
     	return this.messages;
     }
     
     /**
      * This will start a scheduled task to run every second.
      * 	-this will run a procedure to check the current time, the
      *  subsequently check the current rules for there @nextCheckTime
      *  and see if they match. If the do, the appropriate action will be
      *  taken to consume the needed metrics for the host's rule and
      *  analyze the rules to see if any actions need be taken.
      *  When the rules is check it will appear in the Messaged queue with
      *  a time stamp.
      *  
      *  //This is inefficient at large scale, but to get things working 
      *  //on a small scale it will be used.
      */
     public void startTimerPollingTask(){
     	long delay = 1000; // delay for 1 sec.
     	long period = 1000; // repeat every sec.
     	final boolean enabled = this.isHANDEnabled();
     	
     	//Create a new timer and add a task to it. Our task is
     	// right inside using the run method of a new TimerTask.
     	final Timer timer = new Timer();
     	timer.scheduleAtFixedRate(new TimerTask(){
     		public void run(){
     			if(!(enabled)){
     				//Cancel the task if HAND is not enabled.
     				
     				//This is important to check before each second
     				//in case HAND becomes disabled at any point.
     				timer.cancel();
     			}else{
     				//Stamp this with time in seconds since Jan. 1 1970 midnight :)
     				long currentTime = System.currentTimeMillis() / 1000l;
     				logger.info("Checking Rules : "+currentTime);
     				
     				//TODO
     				/**
     				 * Logic here needs carry out the above description 
     				 * 
     				 * 	1. current time needs to be checked against all rules
     				 * 	2. If a rule matches, take action
     				 * 	3. Queuing and concurrency methods need to be thought of here.
     				 */
     				
     			}
     		}
     		
     	}, delay, period);
     	
     }
     
    
     
     @Override
 	public Collection<Class<? extends IFloodlightService>> getModuleServices() {
    	//This module should depend on FloodlightProviderService,
    			// IStorageSourceProviderService, IRestApiService, &
    			// IStaticFlowEntryPusherService
    			Collection<Class<? extends IFloodlightService>> l =
    					new ArrayList<Class<? extends IFloodlightService>>();
    			l.add(IFloodlightProviderService.class);
    			l.add(IStorageSourceService.class);
    	        l.add(IRestApiService.class);
    	        l.add(IStaticFlowEntryPusherService.class);
    			return l;
 	}
 
 	@Override
 	public Map<Class<? extends IFloodlightService>, IFloodlightService> getServiceImpls() {
 		Map<Class<? extends IFloodlightService>,
         IFloodlightService> m = 
         new HashMap<Class<? extends IFloodlightService>,
         IFloodlightService>();
         // Implements the QoS service
         m.put(IHANDService.class, this);
         return m;
 	}
 
 	@Override
 	public Collection<Class<? extends IFloodlightService>> getModuleDependencies() {
		// TODO Auto-generated method stub
		return null;
 	}
 
 	@Override
 	public void init(FloodlightModuleContext context)
 			throws FloodlightModuleException {
 		floodlightProvider = context
                 .getServiceImpl(IFloodlightProviderService.class);
         storageSource = context.getServiceImpl(IStorageSourceService.class);
         restApi = context.getServiceImpl(IRestApiService.class);
         hostRules = new ArrayList<HANDRule>();
         logger = LoggerFactory.getLogger(HAND.class);
 
         // start disabled
         enabled = true;
 	}
 
 	@Override
 	public void startUp(FloodlightModuleContext context) {
 		
 		//initialize
 		 // register REST interface
         restApi.addRestletRoutable(new HANDWebRoutable());
 
         // storage for hosts
         storageSource.createTable(HOSTS_TABLE_NAME, null);
         storageSource.setTablePrimaryKeyName(HOSTS_TABLE_NAME, COLUMN_HID);
      	//synchronized (gangliaHosts) {
         //    this.gangliaHosts = readHostsFromStorage();
         //}
         
         //storage for rules
         storageSource.createTable(RULES_TABLE_NAME, null);
         storageSource.setTablePrimaryKeyName(RULES_TABLE_NAME, COLUMN_RID);
         //synchronized (hostRules) {
         //    this.hostRules = readRulesFromStorage();
         //}
         
 		
 		// One of the things we need to do is start our timing
 		// mechanism when the module is started.
 		if(this.isHANDEnabled()){
 			this.startTimerPollingTask();
 		}
 		else{
 			logger.info("Disabled, waiting to enable to start polling hosts.");
 		}
 
 	}
     
 	 //TODO
     /**
      * Add/Remove host
      * Must check floodlight topology service to see if host exists first.
      * If not return a message.
      * 
      * On Delete check if host exists in HAND, remove it from HAND.
      */
 	
 	public void addGangliaHost(IPv4 address){
 		//TODO add for ip address of host
 		
 	}
 	
 	public void addGangliaHost(String hostName){
 		//TODO add for hostname of host
 	}
     
     //TODO
     /**
      * Add/Remove rules
      * 
      * Rules deal with all other modules in Floodlight.
      * LB 			- Load Balancer 
      * AFR / DFR 	- Add/Remove Firewall Rule
      * PSF			- Push Static Flow
      * AQOS / DQOS  - Add/Remove QoS
      * 
      * ***Rules must be check every time data is polled for a specific host***
      *
      */
     
     //TODO
     /**
      * Needs  a way to carry out the rules and a way to
      * time synchronize GangliaHost.getPollingTime() with the system clock
      * Hosts need to be polled every ^^time^^ that is set.
      * 
      * If the metrics add up the carry out the rule.
      */
     
     //TODO
     /**
      * need storage source for *Metrics last polled* if a rule states
      * "If the last polled metrics is above X amount 5 times in a row, carry out rule Y"
      * The last 4 polls need to be stored.
      */
 
 }
