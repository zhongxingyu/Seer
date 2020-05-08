 package eu.celarcloud.celar_ms.AgentPack;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Properties;
 import java.util.UUID;
 import java.util.Map.Entry;
 import java.util.concurrent.LinkedBlockingQueue;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import eu.celarcloud.celar_ms.Exceptions.CatascopiaException;
 import eu.celarcloud.celar_ms.ProbePack.IProbe;
 import eu.celarcloud.celar_ms.utils.CatascopiaLogging;
 import eu.celarcloud.celar_ms.utils.CatascopiaNetworking;
 import eu.celarcloud.celar_ms.utils.CatascopiaPackaging;
 import eu.celarcloud.celar_ms.utils.CatascopiaProbeFactory;
 
 /**
  * JCatascopia Monitoring Agent manages metric collecting probes
  * and distributes metrics to the Monitoring Server
  *  
  * @author Demetris Trihinas
  *
  */
 public class MonitoringAgent implements IJCatascopiaAgent{
 	//path to JCatascopia Agent Directory
 	private String JCATASCOPIA_AGENT_HOME;
 	//path to config file
 	private static final String CONFIG_PATH = "resources"+File.separator+"agent.properties";
 	//path to internal private file
 	private static final String AGENT_PRIVATE_FILE = "resources"+File.separator+"agent_private.properties";
 	//path to probe library
 	private static final String PROBE_LIB_PATH = "eu.celarcloud.celar_ms.ProbePack.ProbeLibrary.";
 	/**
 	 * properties read from config file
 	 */
 	private Properties config;
 	/**
 	 * give Agent a unique ID
 	 */
 	private String agentID;
 	/**
 	 * Agent IP address
 	 */
 	private String agentIPaddress;
 	/**
 	 * HashMaps to easy retrieve probes either using ID or probeName
 	 */
 	private HashMap<String,IProbe> probeNameMap;
 	/**
 	 * collector grabs metrics from metricQueue to be parsed
 	 */
 	private MetricCollector collector;
 	private final LinkedBlockingQueue<String> metricQueue = new LinkedBlockingQueue<String>(64);
 		
 	/**
 	 * distributor publishes metrics to the MS Server
 	 */
 	private Distributor distributor;
 	
 	/**
 	 * flag to check if logging available
 	 */
 	private boolean loggingFlag;
 	/**
 	 * Logger to handle logging
 	 */
 	private Logger myLogger;
 	
 	protected Aggregator aggregator;
 	
 	private ProbeController probeController;
 	
 	private boolean debugMode;
 	private boolean useServer;
 	
 	private HeartBeatMonitor heartBeatMonitor;
 	
 	/**
 	 * 
 	 * @param agentDirPath
 	 * @throws CatascopiaException
 	 */
 	public MonitoringAgent(String agentDirPath) throws CatascopiaException {
 		//path to JCatascopia Agent Directory
 		this.JCATASCOPIA_AGENT_HOME = agentDirPath;
 		//parse config file
 		this.parseConfig();
 		//get agentID or create one
 		this.agentID = this.getAgentIDFromFile();
 		//get ip address
 		this.agentIPaddress = CatascopiaNetworking.getMyIP();
 		
 		this.probeNameMap = new HashMap<String,IProbe>();
 		
 		this.initLogging(); 
 				
 		this.detectAvailableProbes();
 		
 		this.initActivateProbes();
 
 		//ping server, tell it we are here and report available metrics
 		//if successful then we can start distributing metrics
 		try{
 			this.useServer = Boolean.parseBoolean(this.config.getProperty("use_server", "true"));
 		}catch(Exception e){
 			this.useServer= true;
 		}
 		if (this.useServer)
 			this.establishConnectionWithServer();	
 		
 		this.aggregator = new Aggregator(this.agentID,this.agentIPaddress,this);
 		
 		this.initDistributor();
 		
 		this.collector = new MetricCollector(this.metricQueue,this.aggregator,this);
 		this.collector.activate();		
 		
 		this.initProbeController();
 		
 		try{
 			this.debugMode = Boolean.parseBoolean(this.config.getProperty("debug_mode", "false"));
 		}catch(Exception e){
 			this.debugMode = false;
 		}
 	}
 	
 	//parse the configuration file
 	private void parseConfig() throws CatascopiaException{
 		this.config = new Properties();
 		//load config properties file
 		try {	
 //			this.writeToLog(Level.INFO,"JCatascopia Agent path to config file: "+JCATASCOPIA_AGENT_HOME+File.separator+CONFIG_PATH);
 			
 			FileInputStream fis = new FileInputStream(JCATASCOPIA_AGENT_HOME+File.separator+CONFIG_PATH);
 			config.load(fis);
 			if (fis != null)
 	    		fis.close();
 		} 
 		catch (FileNotFoundException e){
 			throw new CatascopiaException("config file not found", CatascopiaException.ExceptionType.FILE_ERROR);
 		} 
 		catch (IOException e){
 			throw new CatascopiaException("config file parsing error", CatascopiaException.ExceptionType.FILE_ERROR);
 		}
 	}
 	
 	//initialize logging
 	private void initLogging(){
 		this.loggingFlag = Boolean.parseBoolean(this.config.getProperty("logging", "false"));
 		if (this.loggingFlag)
 			try{
 				this.myLogger = CatascopiaLogging.getLogger(this.JCATASCOPIA_AGENT_HOME, "JCatascopiaMSAgent");
 				this.myLogger.info("JCatascopiaAgent: Created and Initialized");
 				this.loggingFlag = true;
 			}
 			catch (Exception e){
 				//Unable to log events
 				this.loggingFlag = false;
 			}
 		else this.loggingFlag = false; //logging turned off by user
 	}
 	
 	/**
 	 * method that logs messages to the MS Agent log
 	 */
 	public void writeToLog(Level level, Object msg){
 		if(this.loggingFlag)
 			this.myLogger.log(level, "JCatascopiaAgent"+": "+msg);
 	}
 	
 	/**
 	 * method that loads ALL probes in Probe Library to the MS Agent
 	 * this method does NOT activate any Probes
 	 */
 	private void detectAvailableProbes(){
 		ArrayList<String> list;
 		try{
 			list = this.listAvailableProbeClasses();
 			for(int i=0;i<list.size();i++){
 				try{
 					IProbe tempProbe = CatascopiaProbeFactory.newInstance(PROBE_LIB_PATH,list.get(i));
 					//add detected probe to probe map
 					this.addProbe(tempProbe);
 				}
 				catch (CatascopiaException e) {
 					this.writeToLog(Level.SEVERE, e);
 					continue;
 				}	
 			}
 		}
 		catch (CatascopiaException e){
 			this.writeToLog(Level.SEVERE, e);
 		}
 	}
 	
 	/**
 	 * 
 	 * @return list of available probes in Probe Library
 	 * @throws CatascopiaException
 	 */
 	public ArrayList<String> listAvailableProbeClasses() throws CatascopiaException{
 		ArrayList<String> list =  CatascopiaPackaging.listClassesInPackage(PROBE_LIB_PATH);
 		return list;
 	}
 	
 	private void initActivateProbes(){
 		String probestr = this.config.getProperty("probes", "all");
 		try{
 			if (probestr.equals("all"))
 				//if all then activate all probes and use default collecting frequency specified by probe developer
 				this.activateAllProbes();
 			else if (!probestr.equals("")){
				//user specified specific which probes to activate
 				String[] probe_list = probestr.split(";");
 				String[] params;	
 				for(String p:probe_list){
 					params = p.split(",");
 					if(params.length>1)
 						//user wants custom collecting periods
 						this.probeNameMap.get(params[0]).setCollectPeriod(Integer.parseInt(params[1]));
 					this.activateProbe(params[0]);
 				}
 			}
 			this.getProbe("StaticInfoProbe").pull();
 		} 
 		catch(CatascopiaException e){
 			this.writeToLog(Level.SEVERE, e);
 		}
 		catch(Exception e){
 			//config file error
 			this.writeToLog(Level.SEVERE, e);
 		}	
 
 	}
 	
 	private void establishConnectionWithServer() throws CatascopiaException{
 		String serverIP = this.config.getProperty("server_ip", "127.0.0.1");
 		String port = this.config.getProperty("control_port", "4245");
 		String protocol = this.config.getProperty("control_protocol","tcp");
 		
 /*		if (!ServerConnector.connect(serverIP,port,protocol,this.agentID,this.agentIPaddress)){
 			this.writeToLog(Level.SEVERE, "FAILED to ping MS Server at"+serverIP);
 			throw new CatascopiaException("Could not connect to MS Server",CatascopiaException.ExceptionType.CONNECTION);
 		}
 		this.writeToLog(Level.INFO, "Successfuly ping-ed MS Server at "+serverIP);
 		
 		if (!ServerConnector.reportAvailableMetrics(serverIP,port,protocol,this.agentID,this.agentIPaddress,this.probeNameMap)){
 			this.writeToLog(Level.SEVERE, "FAILED to report available metrics to MS Server at "+serverIP);
 			throw new CatascopiaException("FAILED to report available metrics to MS Server at "+serverIP,
 					                       CatascopiaException.ExceptionType.CONNECTION);
 		}
 		this.writeToLog(Level.INFO, "Successfuly reported available agent metrics to MS Server at "+serverIP);
 	*/
 		if (!ServerConnector.connect(serverIP,port,protocol,this.agentID,this.agentIPaddress,this.probeNameMap)){
 			this.writeToLog(Level.SEVERE, "FAILED connect to Monitoring Server at: "+serverIP);
 			throw new CatascopiaException("Could not connect to Monitoring Server",CatascopiaException.ExceptionType.CONNECTION);
 		}
 		else this.writeToLog(Level.INFO, "Successfuly connected to Server at: "+serverIP);
 		
 		//initialize HeartBeatMonitor
 		this.heartBeatMonitor = new HeartBeatMonitor(this,3,60,serverIP,port);
 		this.heartBeatMonitor.activate();
 	}
 	
 	/**
 	 * initialize the Distributer
 	 */
 	private void initDistributor(){
 		//Distributor settings
 		String port = this.config.getProperty("distributor_port", "4242");
 		String protocol = this.config.getProperty("distributor_protocol","tcp");
     	String ip = this.config.getProperty("server_ip","localhost");
     	String hwm = this.config.getProperty("distributor_hwm","16");
     	//Aggregator settings
     	long agg_interval = Long.parseLong(this.config.getProperty("aggregator_interval"))*1000;
     	int agg_buf = Integer.parseInt(this.config.getProperty("aggregator_buffer_size"));
 		this.distributor = new Distributor(ip,port,protocol,Long.parseLong(hwm),this.aggregator,agg_interval,agg_buf,this);
 		distributor.activate();
 	}
 	
 	private void initProbeController(){
 		if (this.config.getProperty("probe_controller_turnOn", "false").equals("true")){
 			String ip = this.config.getProperty("probe_controller_ip","localhost");
 			String port = this.config.getProperty("probe_controller_port","4243");
 			Long hwm = Long.parseLong(this.config.getProperty("probe_controller_hwm","8"));
 			String protocol = this.config.getProperty("probe_controller_protocol","tcp");
 			
 			this.probeController = new ProbeController(ip, port, protocol, hwm, this.metricQueue, this);
 			this.probeController.activate();
 			this.writeToLog(Level.INFO, "ProbeController enabled with parameters: "+ip+", "+port+", "+hwm+", "+protocol);
 		}
 	}
 	
 	/**
 	 * 
 	 * @return ms agent ID
 	 */
 	public String getAgentID(){
 		return this.agentID;
 	}
 		
 	/**
 	 * 
 	 * @return ms agent IP address
 	 */
 	public String getAgentIP(){
 		return this.agentIPaddress;
 	}
 	
 	/**
 	 * add probe to agent
 	 * @param p
 	 */
 	public void addProbe(IProbe p){
 		this.probeNameMap.put(p.getProbeName(), p);
 		p.attachQueue(this.metricQueue);
 		p.attachLogger(this.myLogger);
 	}
 	
 	/**
 	 * remove probe from agent
 	 * @param probeID
 	 * @throws CatascopiaException
 	 */
 	public void removeProbeByName(String probeName) throws CatascopiaException{
 		if (this.probeNameMap.containsKey(probeName)){
 			IProbe probe = this.probeNameMap.get(probeName);
 			probe.removeQueue();
 			probe.terminate();
 			this.probeNameMap.remove(probeName);
 		}
 		else
 			throw new CatascopiaException("Remove Probe Failed, probe name given does not exist: "+probeName, 
 										   CatascopiaException.ExceptionType.KEY);
 	}
 	
 	/**
 	 * 
 	 * @param probeName
 	 * @return probe specified by probeName
 	 * @throws CatascopiaException
 	 */
 	public IProbe getProbe(String probeName) throws CatascopiaException{
 		if (this.probeNameMap.containsKey(probeName))
 			return this.probeNameMap.get(probeName);
 		else
 			throw new CatascopiaException("Get Probe Failed, probe name given does not exist: "+probeName, 
 										   CatascopiaException.ExceptionType.KEY);
 	}
 	
 	/**
 	 * terminate agent. all probes are first terminated.
 	 */
 	public void terminate(){
 		//terminate probes
 		for (Entry<String,IProbe> entry :this.probeNameMap.entrySet())
 			entry.getValue().terminate();
 		//terminate collector
 		this.collector.terminate();
 		this.distributor.terminate();
 	}
 		
 	public HashMap<String,IProbe> getProbeMap(){
 		return this.probeNameMap;
 	}
 		
 	public void activateProbe(String probeName) throws CatascopiaException{
 		if (this.probeNameMap.containsKey(probeName)){
 			this.probeNameMap.get(probeName).activate();
 		}
 		else
 			throw new CatascopiaException("Activate Probe Failed, probe name given does not exist: "+probeName, 
 										   CatascopiaException.ExceptionType.KEY);
 	}
 	
 	public void activateAllProbes(){
 		for(String name : this.probeNameMap.keySet())
 			try{
				if (!name.equals("StaticInfoProbe"))
 					this.activateProbe(name);
 			} 
 			catch (CatascopiaException e) {
 				continue;
 			}
 	}
 	
 	public void deactivateProbe(String probeName) throws CatascopiaException{
 		if (this.probeNameMap.containsKey(probeName)){
 			this.probeNameMap.get(probeName).deactivate();
 		}
 		else
 			throw new CatascopiaException("Deactivate Probe Failed, probe name given does not exist: "+probeName, 
 										   CatascopiaException.ExceptionType.KEY);
 	}
 		
 	private String getAgentIDFromFile() throws CatascopiaException{
     	Properties prop = new Properties();
     	String id;
     	try {
 //    		String JCATASCOPIA_AGENT_HOME = System.getenv("JCATASCOPIA_AGENT_HOME");
 //			if (JCATASCOPIA_AGENT_HOME == null)
 //				JCATASCOPIA_AGENT_HOME = ".";
 			String agentfile = JCATASCOPIA_AGENT_HOME + File.separator + AGENT_PRIVATE_FILE;
 			if((new File(agentfile).isFile())){
 				//load agent_private properties file
 				FileInputStream fis = new FileInputStream(agentfile);
 				prop.load(fis);
 				if (fis != null)
 		    		fis.close();
 				id = prop.getProperty("agentID",UUID.randomUUID().toString().replace("-", ""));
 			}
 			else{
 				//first time agent started. Store assigned id to file
 				id = UUID.randomUUID().toString().replace("-", "");
 				prop.setProperty("agentID", id);
 				prop.store(new FileOutputStream(agentfile), null);
 			}
     	} 
 		catch (FileNotFoundException e){
 			throw new CatascopiaException("agent_private file not found", CatascopiaException.ExceptionType.FILE_ERROR);
 		} 
 		catch (IOException e){
 			throw new CatascopiaException("agent_file parsing error", CatascopiaException.ExceptionType.FILE_ERROR);
 		}	
     	return id;
 	}
 	
 	public boolean inDebugMode(){
 		return this.debugMode;
 	}
 	
 	/**
 	 * @param args
 	 * @throws CatascopiaException 
 	 */
 	public static void main(String[] args) throws CatascopiaException{
 		try{	
 			if (args.length > 0)
 				new MonitoringAgent(args[0]);
 			else
 				new MonitoringAgent(".");
 		}catch(Exception e){
 			System.exit(1);
 		}
 	}
 }
