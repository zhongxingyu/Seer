 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package fi.wsnusbcollect.experiment;
 
 import fi.wsnusbcollect.App;
 import fi.wsnusbcollect.AppConfiguration;
 import fi.wsnusbcollect.RunningApp;
 import fi.wsnusbcollect.db.ExperimentMetadata;
 import fi.wsnusbcollect.db.USBconfiguration;
 import fi.wsnusbcollect.messages.CollectionDebugMsg;
 import fi.wsnusbcollect.messages.CommandMsg;
 import fi.wsnusbcollect.messages.CtpInfoMsg;
 import fi.wsnusbcollect.messages.CtpReportDataMsg;
 import fi.wsnusbcollect.messages.CtpResponseMsg;
 import fi.wsnusbcollect.messages.CtpSendRequestMsg;
 import fi.wsnusbcollect.messages.MultiPingResponseReportMsg;
 import fi.wsnusbcollect.messages.NoiseFloorReadingMsg;
 import fi.wsnusbcollect.nodeCom.MessageSender;
 import fi.wsnusbcollect.nodeCom.MultipleMessageListener;
 import fi.wsnusbcollect.nodeCom.MultipleMessageSender;
 import fi.wsnusbcollect.nodeCom.MyMessageListener;
 import fi.wsnusbcollect.nodeCom.TOSLogMessenger;
 import fi.wsnusbcollect.nodeManager.NodeHandlerRegister;
 import fi.wsnusbcollect.nodes.ConnectedNode;
 import fi.wsnusbcollect.nodes.GenericNode;
 import fi.wsnusbcollect.nodes.NodeHandler;
 import fi.wsnusbcollect.nodes.NodePlatform;
 import fi.wsnusbcollect.nodes.NodePlatformFactory;
 import fi.wsnusbcollect.nodes.SimpleGenericNode;
 import fi.wsnusbcollect.notify.EventMailNotifierIntf;
 import fi.wsnusbcollect.usb.NodeConfigRecord;
 import fi.wsnusbcollect.usb.USBarbitrator;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import javax.annotation.Resource;
 import net.tinyos.message.MoteIF;
 import net.tinyos.packet.BuildSource;
 import net.tinyos.packet.PhoenixSource;
 import org.ini4j.Wini;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.stereotype.Repository;
 import org.springframework.transaction.annotation.Transactional;
 
 /**
  * Experiment initialization/helper class
  * @author ph4r05
  */
 @Repository
 @Transactional
 public class ExperimentInitImpl implements ExperimentInit {
     private static final Logger log = LoggerFactory.getLogger(ExperimentInit.class);
     
     @Resource(name="experimentRecords")
     protected ExperimentRecords2DB expRecords;
     
     @Resource(name="nodeHandlerRegister")
     protected NodeHandlerRegister nodeReg;
     
     //@Autowired
     protected ExperimentCoordinator expCoordinator;
     
     @Resource(name="USBarbitrator")
     protected USBarbitrator usbArbitrator;
     
     @Resource(name="mailNotifier")
     protected EventMailNotifierIntf notifier;
     
     // main experiment metadata
     protected ExperimentMetadata expMeta;
     
     // config file prepared by application
     protected Wini config;
     
     private int status=0;
     
     @Resource(name="experimentParameters")
     private ExperimentParameters params;
     
     public static final String INISECTION_MOTELIST="motelist";
     public static final String INISECTION_METADATA="experimentMetadata";
     public static final String INISECTION_PARAMETERS="experimentParameters";
     
     /**
      * Loads configuration to experiment
      * @deprecated 
      */
     public void loadConfig(){
         
     }
     
     /**
      * Process settings from parameters/config file
      */
     public List<NodeConfigRecord> getNodes2connect(){
         // default include/exclude motelist from parameters
         // init file has precedense
         String moteInclude = App.getRunningInstance().getUseMotesString();
         String moteExclude = App.getRunningInstance().getIgnoreMotesString();
         
         // override from config file:)
         AppConfiguration config1 = RunningApp.getRunningInstance().getConfig();
         if (config1!=null){
             moteInclude = config1.getConfig(INISECTION_MOTELIST, "include", moteInclude);
             moteExclude = config1.getConfig(INISECTION_MOTELIST, "exclude", moteExclude);
         }
         
         // config file/arguments parsing, node selectors, get final set of nodes to connect to
         return this.usbArbitrator.getNodes2connect(moteInclude, moteExclude);
     }
     
     /**
      * Helper method - re-programs connected nodes specified by parameters/config file.
      * Only path to directory with makefile is required. Then is executed
      * make telosb install,X bsl,/dev/mote_telosX
      * 
      * @extension: add multithreading to save time required for reprogramming
      * 
      * @param makeDir  absolute path to makefile directory with mote program
      */
     @Override
     public void reprogramConnectedNodes(String makefileDir){
         List<NodeConfigRecord> nodes2connect = this.getNodes2connect();
         this.usbArbitrator.reprogramNodes(nodes2connect, makefileDir);
     }
     
     @Override
     public void initClass() {
         log.info("Class initialized");
         this.expCoordinator = App.getRunningInstance().getExpCoord();
         
         // stores metadata about experiment
         expMeta = new ExperimentMetadata();
         expMeta.setDatestart(new Date());
         // get usb configuration
         USBconfiguration currentConfiguration = this.usbArbitrator.getCurrentConfiguration();
         expMeta.setNodeConfiguration(currentConfiguration);
         
         // owner of experiment, determine from system
         String username = System.getProperty("user.name");
         if (username!=null){
             log.info("Found that user running this experiment is: " + username);
             expMeta.setOwner(username);
         }
         
         // read config file
         this.config = App.getRunningInstance().getIni();
         
         // use config helper
         AppConfiguration config1 = RunningApp.getRunningInstance().getConfig();
         if (config1!=null){
             // store config file as raw
             expMeta.setConfigFile(App.getRunningInstance().getConfigFileContents());
             
             // mandatory
             if (config1.hasConfig(INISECTION_METADATA, "name")==false){
                 log.error("INI file must contain experiment name field");
                 throw new IllegalArgumentException("INI file must contain experiment name field");
             }
             
             expMeta.setName(config1.getConfig(INISECTION_METADATA, "name", "defaultName"));
             expMeta.setExperimentGroup(config1.getConfig(INISECTION_METADATA, "group", "defaultGroup"));
             expMeta.setDescription(config1.getConfig(INISECTION_METADATA, "annotation", ""));
             expMeta.setKeywords(config1.getConfig(INISECTION_METADATA, "keywords", ""));
             
             // read parameters from config file
             this.params.load(this.config);
         }
         
         // config file/arguments parsing, node selectors, get final set of nodes to connect to
         List<NodeConfigRecord> nodes2connect = this.getNodes2connect();
         // init connected nodes - builds handler and connecto to them
         this.initConnectedNodes(null, nodes2connect);
         //write information about directly connected nodes and its configuration (nodes2connect)
         ArrayList<String> nodeList = new ArrayList<String>(nodes2connect.size());
         Iterator<NodeConfigRecord> ncrIt = nodes2connect.iterator();
         while(ncrIt.hasNext()){
             NodeConfigRecord ncr = ncrIt.next();
             nodeList.add(ncr.getSerial());
         }
         
         // set connected nodes as list
         expMeta.setConnectedNodesUsed(nodeList);
         
         // persist meta
         this.expRecords.storeExperimentMeta(expMeta);
     }
 
     @Override
     public void deinitExperiment() {
         this.closeExperiment();
     }
     
     /**
      * Experiment is closing now... update timers in database
      */
     public void closeExperiment(){
         if (this.expMeta==null){
             throw new NullPointerException("Current experiment metadata is null");
         }
         
         this.expRecords.closeExperiment(expMeta);
     }
     
     /**
      * Updates real experiment start in miliseconds - in configuration
      * @param mili 
      */
     @Override
     public void updateExperimentStart(long mili){
         if (this.expMeta==null){
             throw new NullPointerException("Current experiment metadata is null");
         }
         
         this.expRecords.updateExperimentStart(expMeta, mili);
     }
 
     /**
      * Stores experiment configuration to database
      * @deprecated 
      */
     public void storeConfig(){
         // store experiment metadata to database
         
         
         // store experiment parameters to database
         this.params.storeToDatabase(expMeta);
     }
     
     @Override
     public void initEnvironment() {
         log.info("Environment initialized");
         
         // here can init shell console
     }
 
     /**
      * Initialize connected nodes here
      * @param props
      * @param ncr 
      */
     @Override
     public void initConnectedNodes(Properties props, List<NodeConfigRecord> ncr) {
         log.info("initializing connected nodes here");
         if (ncr==null){
             throw new NullPointerException("NCR is null");
         }
         
         Integer defaultGateway=null;
         int nodesPerOneListener=16;
         int nodesPerOneSender=200;
         
         Iterator<NodeConfigRecord> iterator = ncr.iterator();
         while(iterator.hasNext()){
             NodeConfigRecord nextncr = iterator.next();
             System.out.println("Node to connect to: " + nextncr.toString());
             
             if (nextncr.getNodeId()==null){
                 log.warn("Cannot work with node without defined node ID, please "
                         + "define its node id in database: " + nextncr.toString());
                 continue;
             }
             
             // determine platform
             NodePlatform platform = NodePlatformFactory.getPlatform(nextncr);
             // try to connect to node
             MoteIF connectToNode = this.connectToNode(nextncr.getConnectionString());
             if (connectToNode==null){
                 log.warn("Cannot connect to node: " + nextncr.toString());
                 continue;
             }
             
             // build generic node info
             GenericNode gn = new SimpleGenericNode(true, nextncr.getNodeId());
             gn.setPlatform(platform);
             
             ConnectedNode cn = new ConnectedNode();
             cn.setNodeObj(gn);
             cn.setNodeConfig(nextncr);
             cn.setMoteIf(connectToNode);
             cn.setTosMessengerListener(nodeReg);
 
             // store for multiple packet sender
             defaultGateway = cn.getNodeId();
 
             // add to map
             this.nodeReg.put(cn);
             
             System.out.println("Initialized connected node: " + cn.toString());
         }
         
         // use configuration here to determine parameters for sender/listener init
         AppConfiguration config1 = RunningApp.getRunningInstance().getConfig();
         
         /**
          * MSG listener initialization
          */
         
         if (config1.hasConfig(INISECTION_METADATA, "nodesPerOneSender")){
             try {
                 nodesPerOneSender = Integer.parseInt(config1.getConfig(INISECTION_METADATA, "nodesPerOneSender"));
                 log.info("Using nodesPerOneSender="+nodesPerOneSender+" from config file");
             } catch(NumberFormatException ex){
                 log.warn("Mallformed config file - nodesPerOneSender cannot be converted to integer");
                 nodesPerOneSender = 256;
             }
         }
         
         // init multiple sender
         this.initMsgSenders(ncr, nodesPerOneSender, defaultGateway);
         
         /**
          * MSG sender initialization
          */
         
         if (config1.hasConfig(INISECTION_METADATA, "nodesPerOneListener")){
             try {
                 nodesPerOneListener = Integer.parseInt(config1.getConfig(INISECTION_METADATA, "nodesPerOneListener"));
                 log.info("Using nodesPerOneListener="+nodesPerOneListener+" from config file");
             } catch(NumberFormatException ex){
                 log.warn("Mallformed config file - nodesPerOneListener cannot be converted to integer");
                 nodesPerOneListener = 16;
             }
         }
         
         // init multiple receiver
         this.initMsgListeners(ncr, nodesPerOneListener);
         
         // starting all threads
         System.out.println("Starting all threads");
         this.nodeReg.startAll();
         
         System.out.println("Initialized");
         status=1;
     }
     
     /**
      * Splits one list of connectedNodes extracted from node handler register
      * to several smaller chunks - used for listener and sender initialization.
      * 
      * One chunk can be used as set of nodes for one message sender/listener
      * 
      * @param inOneblock
      * @return 
      */
     public List<List<ConnectedNode>> partitionConnectedNodes(int inOneblock){
         List<List<ConnectedNode>> listForListener = new LinkedList<List<ConnectedNode>>();
         List<ConnectedNode> curListForListener = new LinkedList<ConnectedNode>();
         listForListener.add(curListForListener);
         
         for(NodeHandler nh :this.nodeReg.values()){
                 if (ConnectedNode.class.isInstance(nh)==false) continue;
                 final ConnectedNode cn = (ConnectedNode) nh;
                 
                 // multiple nodes for listener
                 curListForListener.add(cn);
                 if (curListForListener.size() >= inOneblock){
                     curListForListener = new LinkedList<ConnectedNode>();
                     listForListener.add(curListForListener);
                 }
         }
         
         return listForListener;
     }
     
     /**
      * Registers default message listeners for particular connected node.
      * In current implementation are registered data dumpers.
      * @param cn 
      */
     public void registerDefaultMessageListeners(ConnectedNode cn){
         // add listening to packets here to separate DB listener
         ExperimentData2DB dbForNode = App.getRunningInstance().getAppContext().getBean("experimentData2DB", ExperimentData2DB.class);
         dbForNode.setExpMeta(expMeta);
         dbForNode.addNode(cn.getNodeId());
         log.info("DB for node is running: " + dbForNode.isRunning() + "; for node: " + cn.getNodeId());
 
         cn.registerMessageListener(new CommandMsg(), dbForNode);
         cn.registerMessageListener(new NoiseFloorReadingMsg(), dbForNode);
         cn.registerMessageListener(new MultiPingResponseReportMsg(), dbForNode);
         cn.registerMessageListener(new CtpReportDataMsg(), dbForNode);
         cn.registerMessageListener(new CtpResponseMsg(), dbForNode);
         cn.registerMessageListener(new CtpSendRequestMsg(), dbForNode);
         cn.registerMessageListener(new CtpInfoMsg(), dbForNode);
         cn.registerMessageListener(new CollectionDebugMsg(), dbForNode);
         log.info("Listener for node: " + cn.getNodeId() + "");
     }
     
     /**
      * Initializes message listeners for each node.
      * If nodesPerListener=1 then is used listener for single node,otherwise 
      * MultipleMessageListener  will be used.
      * 
      * Message listeners are registered with registerDefaultMessageListeners.
      * 
      * @param connectedNodes
      * @param nodesPerListener 
      */
     public void initMsgListeners(List<NodeConfigRecord> ncr, int nodesPerListener){
         log.info("Initializing multiple message listener");
         
         // at first decide which listener to use
         if (nodesPerListener==1){
             for(NodeHandler nh :this.nodeReg.values()){
                 if (ConnectedNode.class.isInstance(nh)==false) continue;
                 final ConnectedNode cn = (ConnectedNode) nh;
                 
                 // message listener
                 MyMessageListener listener = new MyMessageListener(cn.getMoteIf());
                 listener.setDropingPackets(true);
                 cn.setMsgListener(listener);
                 
                 this.registerDefaultMessageListeners(cn);
             }
             
             return;
         }
         
         // otherwise use more nodes per one listener -> preprocess
         List<List<ConnectedNode>> listForListener = this.partitionConnectedNodes(nodesPerListener);
         
         int listenerCount = 1;
         for(List<ConnectedNode> curXListForListener: listForListener){
             log.info("MultipleMessageListener id: " + listenerCount);
             MultipleMessageListener mMsgListener = new MultipleMessageListener(" block: " + listenerCount);
             mMsgListener.setDropingPackets(true);
             mMsgListener.setNotifier(notifier);
             
             for(ConnectedNode cn : curXListForListener){
                 mMsgListener.connectNode(cn, null);
                 cn.setMsgListener(mMsgListener);
                 
                 this.registerDefaultMessageListeners(cn);
             }
             
             listenerCount+=1;
         }
     }
     
     /**
      * Initialize message sender for connected nodes.
      * If nodesPerSender=1 then is used MessageSender for single node, otherwise 
      * MultipleMessageSender will be used.
      * 
      * Here one can use big chunks of nodes for one message sender since it is 
      * not critical and it can save memory because each sender spawns its own thread.
      * 
      * @param ncr
      * @param nodesPerSender
      * @param defaultGateway 
      */
     public void initMsgSenders(List<NodeConfigRecord> ncr, int nodesPerSender, Integer defaultGateway){
         // at first decide which listener to use
         if (nodesPerSender==1){
             for(NodeHandler nh :this.nodeReg.values()){
                 if (ConnectedNode.class.isInstance(nh)==false) continue;
                 final ConnectedNode cn = (ConnectedNode) nh;
                 
                 // message listener
                 MessageSender sender = new MessageSender(cn.getMoteIf());
                 cn.setMsgSender(sender);
             }
             
             return;
         }
         
         // otherwise use more nodes per one listener -> preprocess
         List<List<ConnectedNode>> listForSender = this.partitionConnectedNodes(nodesPerSender);
         
         int count = 1;
         for(List<ConnectedNode> curXListForSender: listForSender){
             // construct connected nodes map for sender
             Map<Integer, MoteIF> connectedNodes = new HashMap<Integer, MoteIF>();
             for(ConnectedNode cn : curXListForSender){
                 connectedNodes.put(cn.getNodeId(), cn.getMoteIf());
             }
             
             // create one message sender instance for more nodes
             MultipleMessageSender mMsgSender = new MultipleMessageSender(defaultGateway, connectedNodes.get(defaultGateway));
             mMsgSender.setAllGateways(connectedNodes, defaultGateway, true);
             mMsgSender.start();
             log.info("MultipleMessageSender id: " + count + "; started");
             
             // now update sender for each node
             for(ConnectedNode cn : curXListForSender){
                 cn.setMsgSender(mMsgSender);
                 log.info("MessageSender updated for node: " + cn.getNodeId());
             }
             
             count+=1;
         }
     }
     
     /**
      * Connects physically to given source by tinyOS BuildSource.
      * If everything is OK method returns MoteIF.
      * 
      * @param source
      * @return 
      */
     public MoteIF connectToNode(String source){
         // build custom error mesenger - store error messages from tinyos to logs directly
         TOSLogMessenger messenger = new TOSLogMessenger();
         // instantiate phoenix source
         PhoenixSource phoenix = BuildSource.makePhoenix(source, messenger);
         MoteIF moteInterface = null;
         
         // phoenix is not null, can create packet source and mote interface
         if (phoenix != null) {
             // loading phoenix
             moteInterface = new MoteIF(phoenix);
         }
         
         return moteInterface;
     }
 
     @Override
     public String toString() {
         return "ExperimentInitImpl{" + "status=" + status + '}';
     }
 
     public NodeHandlerRegister getNodeReg() {
         return nodeReg;
     }
 
     public void setNodeReg(NodeHandlerRegister nodeReg) {
         this.nodeReg = nodeReg;
     }
 
     public ExperimentCoordinator getExpCoordinator() {
         return expCoordinator;
     }
 
     public void setExpCoordinator(ExperimentCoordinatorImpl expCoordinator) {
         this.expCoordinator = expCoordinator;
     }
 
     @Override
     public ExperimentMetadata getExpMeta() {
         return expMeta;
     }
 
     public void setExpMeta(ExperimentMetadata expMeta) {
         this.expMeta = expMeta;
     }
 
     public ExperimentParameters getParams() {
         return params;
     }
 
     public void setParams(ExperimentParameters params) {
         this.params = params;
     }   
     
     
 }
