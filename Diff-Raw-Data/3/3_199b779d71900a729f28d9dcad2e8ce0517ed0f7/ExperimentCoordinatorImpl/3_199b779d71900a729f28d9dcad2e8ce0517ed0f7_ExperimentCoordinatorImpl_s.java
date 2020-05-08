 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package fi.wsnusbcollect.experiment;
 
 import fi.wsnusbcollect.experiment.results.ExperimentStatGen;
 import fi.wsnusbcollect.App;
 import fi.wsnusbcollect.console.Console;
 import fi.wsnusbcollect.console.ConsoleHelper;
 import fi.wsnusbcollect.db.ExperimentCTPRequest;
 import fi.wsnusbcollect.db.ExperimentDataCommands;
 import fi.wsnusbcollect.db.ExperimentDataGenericMessage;
 import fi.wsnusbcollect.db.ExperimentDataLog;
 import fi.wsnusbcollect.db.ExperimentDataRevokedCycles;
 import fi.wsnusbcollect.db.ExperimentMultiPingRequest;
 import fi.wsnusbcollect.db.PrintfEntity;
 import fi.wsnusbcollect.messages.CollectionDebugMsg;
 import fi.wsnusbcollect.messages.CommandMsg;
 import fi.wsnusbcollect.messages.CtpInfoMsg;
 import fi.wsnusbcollect.messages.CtpReportDataMsg;
 import fi.wsnusbcollect.messages.CtpResponseMsg;
 import fi.wsnusbcollect.messages.CtpSendRequestMsg;
 import fi.wsnusbcollect.messages.MessageTypes;
 import fi.wsnusbcollect.messages.MultiPingMsg;
 import fi.wsnusbcollect.messages.MultiPingResponseReportMsg;
 import fi.wsnusbcollect.messages.NoiseFloorReadingMsg;
 import fi.wsnusbcollect.messages.PrintfMsg;
 import fi.wsnusbcollect.messages.RssiMsg;
 import fi.wsnusbcollect.nodeCom.MessageListener;
 import fi.wsnusbcollect.nodeCom.MessageToSend;
 import fi.wsnusbcollect.nodeManager.NodeHandlerRegister;
 import fi.wsnusbcollect.nodes.ConnectedNode;
 import fi.wsnusbcollect.nodes.NodeHandler;
 import fi.wsnusbcollect.utils.RingBuffer;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Set;
 import java.util.concurrent.TimeoutException;
 import javax.annotation.PostConstruct;
 import javax.annotation.Resource;
 import net.tinyos.message.Message;
 import org.ini4j.Ini;
 import org.ini4j.Wini;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Repository;
 import org.springframework.transaction.annotation.Transactional;
 
 /**
  *
  * @author ph4r05
  */
 @Repository
 @Transactional
 public class ExperimentCoordinatorImpl extends Thread implements ExperimentCoordinator, MessageListener{
     private static final Logger log = LoggerFactory.getLogger(ExperimentCoordinatorImpl.class);    
 
     //@Autowired
     //@Resource(name="experimentInit")
     protected ExperimentInit expInit;
     
     @Resource(name="nodeHandlerRegister")
     protected NodeHandlerRegister nodeReg;
     
     @Autowired
     protected Console console;
     
     @Resource(name="consoleHelper")
     protected ConsoleHelper consoleHelper;
     
     /**
      * Experiment records data handler
      */
     protected ExperimentRecords2DB expRecords;
     
     /**
      * Constols main experiment loop
      */
     protected boolean running=true;
     
     /**
      * Initial suspend and in-experiment suspend
      */
     protected boolean suspended=true;
     
     /**
      * Only informative attribute for user interface - last received message stored here
      */
     private Message lastMsg;
     
     /**
      * Milliseconds when unsuspended/started
      */
     private Long miliStart;
     
     /**
      * Experiment state automaton
      */
     private ExperimentState eState;
     
     /**
      * Start time of last N experiments
      * Ring buffer overwrites old records when gets full. 
      * 
      * Used in experiment revocation - on fail need to revoke last N experiments,
      * to do so, we need start time of such experiments
      */
     private RingBuffer<Long> lastExperimentTimes;
     
     /**
      * Node reachability monitor - monitors state of nodes and recovers from freeze
      */
     private NodeReachabilityMonitor nodeMonitor;
     
     /**
      * number of success successor cycles from last reset
      */
     private int succCyclesFromLastReset;
     
     /**
      * Basic experiment statistics generator.
      * Used mainly from Jython GUI to generate experiment statistics to evaluate
      * performed experiments.
      */
     private ExperimentStatGen statGen;
     
     /**
      * Log generic messages received from network?
      */
     private boolean logGenericMessages=false;
     private boolean logCTPMessages=false;
     private boolean logCommandMessages=false;
     private boolean logPrintfMessages=false;
     
     private boolean restartNodesBeforeExperiment=true;
     
     /**
      * Which experiment to start after unsuspend ?
      */
     private int experiment2Start = 3;
     public static final int EXPERIMENT_NONE = 1;
     public static final int EXPERIMENT_RSSI = 2;
     public static final int EXPERIMENT_CTP = 3;
     
     /**
      * RSSI experiment config
      */
     ExperimentRSSIConfiguration rssiConfig;
     
     /**
      * CTP experiment handle
      */
     private ExperimentCtp expCTP;
 
     public ExperimentCoordinatorImpl(String name) {
         super(name);
     }
 
     public ExperimentCoordinatorImpl() {
         super("ExperimentCoordinatorImpl");
     }
 
     @PostConstruct
     public void initClass() {
         log.info("Class initialized");
     }
 
     @Override
     public synchronized void interrupt() {
         this.running=false;
         this.nodeReg.shutdownAll();
         super.interrupt();
     }
 
     // if has shell, needs to spawn new thread
     // without shell it is not necessary
     @Override
     public void work() {
         this.expInit = (ExperimentInit) App.getRunningInstance().getExpInit();
         log.warn("ThreadID: " + this.getId());
         
         if (App.getRunningInstance().isShell()){
             this.start();
         } else {
             this.run();
         }
     }
 
     @Override
     public void run() {        
         // main
         this.main();
     }
 
     /**
      * Holds suspended until script allows execution
      */
     public void waitSuspended(){
         // do not write anything if suspend is not requested
         if (suspended==false){
             return;
         }
         
         System.out.println("Waiting in suspend sleep. To start call unsuspend().");
         try {
             while(suspended){
                 Thread.sleep(100L);
                 Thread.yield();
             }
             
             System.out.println("Suspend sleep stopped.");
         } catch (InterruptedException ex) {
             log.error("Cannot sleep", ex);
         }
     }
     
     /**
      * Performs hw reset for all nodes where applicable
      */
     @Override
     public void hwresetAllNodes(){
         this.nodeReg.hwresetAll();
     }
     
     @Override
     public void hwresetNode(int nodeId){
         if (this.nodeReg.containsKey(nodeId)==false) return;
         NodeHandler nh = this.nodeReg.get(nodeId);
         
         if (ConnectedNode.class.isInstance(nh)){
             final ConnectedNode cnh = (ConnectedNode) nh;
             if (cnh.hwresetPossible()){
                 cnh.hwreset();
             }
         }
     }
     
     /**
      * Send reset packet to all registered nodes SEQUENTIALY
      */
     @Override
     public void resetAllNodes(){
         Collection<NodeHandler> values = this.nodeReg.values();
         Iterator<NodeHandler> iterator = values.iterator();
         while(iterator.hasNext()){
             NodeHandler nh = iterator.next();
             this.sendReset(nh.getNodeId());
         }
     }
     
     @Override
     public void resetNode(int nodeId){
         if (this.nodeReg.containsKey(nodeId)==false) return;
         this.sendReset(nodeId);
     }
     
     /**
      * Initialize fresh started node - push settings and so ...
      * Current implementation: start again noise floor reading
      * @param nodeId 
      */
     public void nodeStartedFresh(int nodeId){        
         log.info("Sending node " + nodeId + " instruction to start noise floor "
                 + "reading every " + this.rssiConfig.getNoiseFloorReadingTimeout() + " miliseconds");
         this.add2experimentLog("INFO", 4, "Node fresh init: " + nodeId, "Starting noise floor reading, "+this.rssiConfig.getNoiseFloorReadingTimeout()+"ms");
         this.sendNoiseFloorReading(nodeId, this.rssiConfig.getNoiseFloorReadingTimeout());
     }
     
     /**
      * Restarts all nodes before experiment and waits for them to show alive again
      */
     @Override
     public void restartNodesBeforeExperiment() throws TimeoutException{
         // at first reset all nodes before experiment count
         System.out.println("Restarting all nodes before experiment, "
                 + "waiting nodes to reinit (need to receive IDENTITY message from everyone)");
         // use node monitor to restart all nodes, set higher agresivity, reconnect is 
         // not enough
         this.nodeReg.registerMessageListener(new CommandMsg(), this.nodeMonitor);
         
         Collection<Integer> deadNodes = this.nodeMonitor.makeNodesReachable(this.nodeReg.values(), 180000, 1);
         if (deadNodes.isEmpty()==false){
             // failed to start some nodes - error...
             log.error("Cannot restart some nodes. Error nodes: ");
             for(Integer deadNodeId : deadNodes){
                 log.error("Dead nodeId: " + deadNodeId);
             }
             
             throw new TimeoutException("Cannot wake up nodes");
             
         } else {
             log.info("All nodes prepared, starting experiment");
             System.out.println("All nodes prepared, starting experiment");
         }
     }
     
     /**
      * Main entry method for NONE experiment - simple wait
      */
     public void mainExperimentNone(){
         while(running){
             try {
                 Thread.sleep(250);
                 
                 // suspend pause 
                 this.waitSuspended();
             } catch (InterruptedException ex) {
                 log.error("Cannot sleep", ex);
             }
         }
     }
     
     /**
      * Gets rssi configuration from config file
      * @return 
      */
     @Override
     public ExperimentRSSIConfiguration getRSSIExperimentConfig(){
         ExperimentRSSIConfiguration configRSSI = new ExperimentRSSIConfiguration();
         
         // set defaults
         configRSSI.setNoiseFloorReadingTimeout(1000);
         configRSSI.setNodeAliveThreshold(4000);
         configRSSI.setPacketsRequested(100);
         configRSSI.setPacketDelay(100);
         
         // read config file
         Wini config = App.getRunningInstance().getIni();
         if (config!=null){
             // read experiment metadata - to be stored in database
             if (config.containsKey("rssiMap")==false){
                 return configRSSI;
             }
             
             // get metadata section from ini file
             Ini.Section metadata = config.get("rssiMap");
             
             if (metadata.containsKey("NoiseFloorReadingTimeout")){
                 try {
                     configRSSI.setNoiseFloorReadingTimeout(Integer.parseInt(metadata.get("NoiseFloorReadingTimeout")));
                 } catch(NumberFormatException e){
                     log.error("Cannot parse noise floor reading timeout",e);
                 }
             }
             
             if (metadata.containsKey("NodeAliveThreshold")){
                 try {
                     configRSSI.setNoiseFloorReadingTimeout(Integer.parseInt(metadata.get("NodeAliveThreshold")));
                 } catch(NumberFormatException e){
                     log.error("Cannot parse NodeAliveThreshold",e);
                 }
             }
             
             if (metadata.containsKey("PacketsRequested")){
                 try {
                     configRSSI.setNoiseFloorReadingTimeout(Integer.parseInt(metadata.get("PacketsRequested")));
                 } catch(NumberFormatException e){
                     log.error("Cannot parse PacketsRequested",e);
                 }
             }
             
             if (metadata.containsKey("PacketDelay")){
                 try {
                     configRSSI.setNoiseFloorReadingTimeout(Integer.parseInt(metadata.get("PacketDelay")));
                 } catch(NumberFormatException e){
                     log.error("Cannot parse PacketDelay",e);
                 }
             }
         }
         
         return configRSSI;
     }
     
     /**
      * Main entry method for RSSI map experiment
      */
     public void mainExperimentRSSI(){
         //
         // from configuration
         //
         this.rssiConfig = this.getRSSIExperimentConfig();
        
         // time needed for nodes to transmit (safety zone 1 second)
         long timeNeededForNode = rssiConfig.getPacketsRequested()*(rssiConfig.getPacketDelay()+20) + 1000;
         // init message sizes - should be from config file
         ArrayList<Integer> messageSizes = new ArrayList<Integer>();
         messageSizes.add(0);
 //        messageSizes.add(8);
         messageSizes.add(16);
 //        messageSizes.add(32);
         messageSizes.add(64);
         
         // init experiment state
         this.eState = new ExperimentState();
         this.eState.setPacketDelay(rssiConfig.getPacketDelay());
         this.eState.setPacketsRequested(rssiConfig.getPacketsRequested());
         this.eState.setNodeReg(nodeReg);
         this.eState.setMessageSizes(messageSizes);
         this.eState.setTimeNeededForNode(timeNeededForNode);
         
         // instructing all nodes to collect noise floor values
         log.info("Instructing all nodes to do noise floor readings");
         this.nodeMonitor.getLastNodeRestarted();
         for(NodeHandler nh : this.nodeReg.values()){
             this.nodeStartedFresh(nh.getNodeId());
         }
                 
         // number of successfully finished cycles from last reset
         // when moving backwards (node freeze) it helps not to repeat older and older 
         // experiments
         succCyclesFromLastReset=0;
         
         // experiment counter to determine replied packets relation to requests
         int experimentCounter = 0;
         
         // main running cycle, experiment can be shutted down setting running=false
         log.info("Starting main collecting cycle, one sending block: " + timeNeededForNode + " ms");
         while(running){
             // if user wants to suspend currently running experiment here, do it
             // user may change experiment parameters or node software and so on...
             // (after re-flashing is needed to reset all nodes and reconnect listeners)
             this.waitSuspended();
             experimentCounter+=1;
             if (experimentCounter >= Integer.MAX_VALUE) experimentCounter=1;
             
             //
             // Experiment state
             //
             
             // next state
             this.eState.next();
             int curNode = this.eState.getCurrentNodeHandler().getNodeId();
             int curTx = this.eState.getCurTxPower();
             int msgSize = this.eState.getCurMsgSize();
             long timeWaitedSum = 0;
             
             // store current time when experiment started - to be able to revoke in future
             // if error occurr
             this.lastExperimentTimes.enqueue(System.currentTimeMillis());
             
             log.info("Sending new ping request for node " + curNode + "; "
                     + "curTx=" + curTx + "; msgSize=" + msgSize + "; succCycles: " + succCyclesFromLastReset);            
             // now send message request
             this.sendMultiPingRequest(curNode, curTx, experimentCounter,
                     rssiConfig.getPacketsRequested(), rssiConfig.getPacketDelay(), msgSize, true, false);
             // wait in node monitor here - process unreachable nodes
             boolean nodeError = false;
             while(timeWaitedSum <= timeNeededForNode){
                 // real waiting, increment waited sum
                 this.pause(1000);
                 timeWaitedSum+=1000;
 
                 // trigger node monitor
                 this.nodeMonitor.nodeMonitorCycle();
 
                 // handle only sponaneously restarted nodes
                 Set<Integer> lastNodeRestarted = this.nodeMonitor.getLastNodeRestarted();
                 
                 // inspect if node monitor failed => error occurred
                 if (this.nodeMonitor.isLastCycleError()==false && lastNodeRestarted.isEmpty()){
                     // node monitor OK
                     continue;
                 }
                 
                 // experiment revocation log
                 String monitorLastError = "";
                 String monitorLastErrorDescription = "";
                 // log time when approximately occurred timeout
                 long outOufServiceFrom = Long.MAX_VALUE;
                 
                 // are there any restarted nodes?
                 if (lastNodeRestarted.isEmpty()==false){
                     nodeError=true;
                     monitorLastErrorDescription = "Some nodes restarted; \n";
                     
                     log.warn("Restarted nodes detected from previous call");
                     for(Integer restartedNodeId : lastNodeRestarted){
                         // reinit 
                         log.warn("Reinitializing node after restart: " + restartedNodeId);
                         this.nodeStartedFresh(restartedNodeId);
                     }
                     
                     if (this.lastExperimentTimes.size()>0){
                         // try guess when error occurred
                         outOufServiceFrom = this.lastExperimentTimes.size() >= 2 
                                 ? this.lastExperimentTimes.get(1)
                                 : this.lastExperimentTimes.get(0);
                     }
                 }
                 
                 // node unreachability error
                 if (this.nodeMonitor.isLastCycleError()){
                     nodeError=true;
                     
                     // error occurred - reset current sending node not to overflow again
                     this.resetNode(curNode);
                     this.nodeStartedFresh(curNode);
 
                     // if execution here, node monitor found error nodes
                     Set<Integer> unreachableNodes = this.nodeMonitor.getLastNodeUnreachable();
                     monitorLastError = monitorLastError + this.nodeMonitor.getLastError();
                     monitorLastErrorDescription = monitorLastErrorDescription + this.nodeMonitor.getLastErrorDescription();
                     long monitorMinLastSeen = this.nodeMonitor.getLastMinLastSeen();
 
                     // need to make this nodes reachable - make it available
                     Collection<Integer> reallyDeadNodes = this.nodeMonitor.makeNodesReachable(unreachableNodes, 120000, 0);
                     if (reallyDeadNodes.isEmpty()==false){
                         log.error("Cannot recover from node unreachability - need to quit experiment!!!");
                         for(Integer deadNodeId : reallyDeadNodes){
                             log.error("Dead nodeId: " + deadNodeId);
                         }
                         return;
                     }
 
                     // reinit nodes
                     for(Integer unreachableNodeId : unreachableNodes){
                         this.nodeStartedFresh(unreachableNodeId);
                     }
 
                     // how long is node out of service?
                     outOufServiceFrom = Math.min(outOufServiceFrom, monitorMinLastSeen-3500);
                 }
 
                 // nodes recovered, experiment revocation/rollback
                 // if here, need to repeat last X experiments
                 log.info("Need to repeat last X experiments, moving backward, outOfOrder: " + outOufServiceFrom + "; now: " + System.currentTimeMillis());
                 this.moveExperimentStateBackward(outOufServiceFrom, 1, "Nodes unreachable/restarted", monitorLastErrorDescription + "\n" + this.nodeMonitor.getResetProtocol());
                 
                 succCyclesFromLastReset = 0;
                 nodeError=true;
                 break;
             }
             
             // no error happened => continue
             if (nodeError==false){
                 this.sendMultiPingRequest(curNode, curTx, 0,
                         1, 0, 0, true, true);
                 this.pause(1000);
                 succCyclesFromLastReset++;
             }
         }
     }
     
     /**
      * Main entry method for CTP experiment 
      * Experiment is started from command line!
      * 
      * Experiment description:
      *  each node sends each X seconds +- variability single CTP message to root
      *  of CTP tree. Every another node snoop on media and write everything received
      *  to UART. 
      * 
      *  Each sent message should be reported by sending node to UART - to be able 
      *  to detect message send decision (variability)
      * 
      * Thus experiment logic is following:
      *  - select CTP root node
      *  - instruct node to start CTP send procedure with specified delay and variability
      *  - on node freeze - reset node
      *  - on node reset/restart instruct node to start sending, if it was root
      *  
      */
     public void mainExperimentCTP(){ 
         // number of successfully finished cycles from last reset
         // when moving backwards (node freeze) it helps not to repeat older and older 
         // experiments
         succCyclesFromLastReset = 0;
 
         // main running cycle, experiment can be shutted down setting running=false
         log.info("Starting main cycle");
         while (running) {
             boolean nodeError = false;
             
             // if user wants to suspend currently running experiment here, do it
             // user may change experiment parameters or node software and so on...
             // (after re-flashing is needed to reset all nodes and reconnect listeners)
             this.waitSuspended();
             
             // avoid cpu hogging
             try {
                 Thread.sleep(500);
             } catch(Exception e){
                 log.error("Cannot sleep", e);
             }
 
             // trigger node monitor
             this.nodeMonitor.nodeMonitorCycle();
 
             // handle only sponaneously restarted nodes
             Set<Integer> lastNodeRestarted = this.nodeMonitor.getLastNodeRestarted();
 
             // inspect if node monitor failed => error occurred
             if (this.nodeMonitor.isLastCycleError() == false && lastNodeRestarted.isEmpty()) {
                 // node monitor OK
                 continue;
             }
             
             // ignoring node restart - probably intended by module
             if (this.expCTP.isIgnoreNodeRestart()){
                 continue;
             }
 
             // experiment revocation log
             String monitorLastError = "";
             String monitorLastErrorDescription = "";
             // log time when approximately occurred timeout
             long outOufServiceFrom = Long.MAX_VALUE;
 
             // are there any restarted nodes?
             if (lastNodeRestarted.isEmpty() == false) {
                 monitorLastErrorDescription = "Some nodes restarted; \n";
                 log.warn("Restarted nodes detected from previous call");
             }
             
             // node unreachability error
             if (this.nodeMonitor.isLastCycleError()) {
                 // error occurred - reset current sending node not to overflow again
                 // ...
 
                 // if execution here, node monitor found error nodes
                 Set<Integer> unreachableNodes = this.nodeMonitor.getLastNodeUnreachable();
                 monitorLastError = monitorLastError + this.nodeMonitor.getLastError();
                 monitorLastErrorDescription = monitorLastErrorDescription + this.nodeMonitor.getLastErrorDescription();
                 long monitorMinLastSeen = this.nodeMonitor.getLastMinLastSeen();
 
                 // need to make this nodes reachable - make it available
                 Collection<Integer> reallyDeadNodes = this.nodeMonitor.makeNodesReachable(unreachableNodes, 999999, 0);
                 if (reallyDeadNodes.isEmpty() == false) {
                     log.error("Cannot recover from node unreachability - need to quit experiment!!!");
                     for (Integer deadNodeId : reallyDeadNodes) {
                         log.error("Dead nodeId: " + deadNodeId);
                     }
                     return;
                 }
 
                 // join together with restarted nodes to reinit together
                 lastNodeRestarted.addAll(unreachableNodes);
 
                 // how long is node out of service?
                 outOufServiceFrom = Math.min(outOufServiceFrom, monitorMinLastSeen - 3500);
             }
             
             // are there some nodes to reinit?
             if (lastNodeRestarted.isEmpty()==false){
                 this.expCTP.nodeRestarted(lastNodeRestarted);
                 
                 // only logging piece of code
                 StringBuilder sb2 = new StringBuilder();
                 sb2.append("Nodes to restart: lastNodeRestarted[");
                 int tmpi=0;
                 for(Integer nid :  lastNodeRestarted){
                     tmpi+=1;
                     if (tmpi>1) sb2.append(", ");
                     sb2.append(nid);
                 }
                 
                 sb2.append("]");
                 monitorLastErrorDescription = monitorLastErrorDescription + sb2.toString();
             }
 
             // nodes recovered, experiment revocation/rollback
             // if here, need to repeat last X experiments
             log.info("Storing info about interruption, outOfOrder: " + outOufServiceFrom + "; now: " + System.currentTimeMillis());
             this.add2experimentLog("WARN", 5, "Nodes unreachable/restarted", monitorLastErrorDescription);
 
             succCyclesFromLastReset = 0;
         }
     }
     
     /**
      * After calling start() on thread it is needed to fix persistence objects
      */
     public void fixPersistenceAfterStart(){
         // get new data handler
         this.expRecords = (ExperimentRecords2DB) App.getRunningInstance().getAppContext().getBean("experimentRecords");
         
 //        // get transaction manager - programmaticall transaction management
 //        this.me = (ExperimentCoordinator) App.getRunningInstance().getAppContext().getBean("experimentCoordinator");
 //        this.em = me.getEm();
 //        
 //        // create new entity manager from donor
 //        // spring should create new entity manager for this prototype class
 //        EMdonorI emd = (EMdonorI) App.getRunningInstance().getAppContext().getBean("emdonor");
 //        TransactionTemplate threadTransactionTemplate = new TransactionTemplate((PlatformTransactionManager)App.getRunningInstance().getAppContext().getBean("transactionManager"));
 //            
 //        if(TransactionSynchronizationManager.hasResource(emd.getEmf())){
 //            System.out.println("Has resource!");
 //            TransactionSynchronizationManager.unbindResource(emd.getEmf());
 //        } else {
 //            try {
 //                //TransactionSynchronizationManager.bindResource(emfx, new EntityManagerHolder(emx));
 //            } catch(Exception e){
 //                log.error("Exception when registering new em", e);
 //            }
 //        }
         
 //        log.info("ME object registered: " + this.em.toString());
     }
     
     @Transactional
     @Override
     public void main() {  
         //
         // INIT objects - new thread 
         //
         
         // need to fix persistence managers after spawning to separate thread
         this.fixPersistenceAfterStart();    
         
         // stat generator
         this.statGen = (ExperimentStatGen) App.getRunningInstance().getAppContext().getBean("experimentStatGen");
         
         // ring buffer for experiment start times
         this.lastExperimentTimes = new RingBuffer<Long>(15, false);
         
         // node reachability monitor
         this.nodeMonitor = new NodeReachabilityMonitor(nodeReg, this);
         this.nodeMonitor.setResetNodesDuringWaitIfUnreachable(true);
         this.nodeMonitor.setResetNodeDelay(4000);
         this.nodeMonitor.setIdSequenceMaxGap(20);
         this.nodeMonitor.setNodeAliveThreshold(6000);
         this.nodeMonitor.setNodeDelayReconnect(5000);
         
         // ctp experiment
         this.expCTP = new ExperimentCtp();
         this.expCTP.setExpCoord(this);
         this.expCTP.postConstruct();
         
         // rssi config
         this.rssiConfig = new ExperimentRSSIConfiguration();
         
         // set command aliases for easy work
         this.console.executeCommand("ex = sys._jy_expCoord");
         this.console.executeCommand("ectp = ex.getExpCTP()");
         
         // start suspended?
         if (App.getRunningInstance().isStartSuspended()){
             this.waitSuspended();
         }
         
         // here we should wait to receive identification from all nodes
         this.nodeReg.registerMessageListener(new fi.wsnusbcollect.messages.CommandMsg(), this);       
         this.nodeReg.registerMessageListener(new CtpResponseMsg(), this);
         this.nodeReg.registerMessageListener(new CtpReportDataMsg(), this);
         this.nodeReg.registerMessageListener(new CtpSendRequestMsg(), this);
         this.nodeReg.registerMessageListener(new CtpInfoMsg(), this);
         this.nodeReg.registerMessageListener(new CollectionDebugMsg(), this);
         this.nodeReg.registerMessageListener(new PrintfMsg(), this);
         this.nodeReg.setDropingReceivedPackets(false);
         
         // restart nodes before experiment - clean all settings to default
         if (this.restartNodesBeforeExperiment){
             try {
                 this.restartNodesBeforeExperiment();
             } catch (TimeoutException ex) {
                 log.error("Cannot continue with experiment, nodes timeouted. Cannot start some nodes", ex);
                 return;
             }
         }
 
         // 
         // Experiment start
         //        
         this.miliStart = System.currentTimeMillis();
         this.expInit.updateExperimentStart(miliStart);
         log.info("Experiment started, miliseconds start: " + miliStart);        
         // unsuspend all packet listeners to start receiving packets
         log.info("Setting ignore received packets to FALSE to start receiving");        
         // work, inform user that experiment is beginning
         System.out.println("Starting main experiment logic...");     
         
         // decide which experiment to run now
         switch(this.experiment2Start){
             case ExperimentCoordinatorImpl.EXPERIMENT_RSSI:
                 this.mainExperimentRSSI();
                 break;
                 
             case ExperimentCoordinatorImpl.EXPERIMENT_CTP:
                 this.mainExperimentCTP();
                 break;
                 
             default:
             case ExperimentCoordinatorImpl.EXPERIMENT_NONE:
                 this.mainExperimentNone();
                 break;
         }
         
         
         // shutdown all registered nodes... Deregister and shutdown listening/sending threads
         System.out.println("Shutting down all registered nodes");
         this.nodeReg.shutdownAll();
         // final message for user
         System.out.println("Exiting... Returning control to main application...");
     }
 
     /**
      * Revokes STEP experiments, move experiment state automaton backward - called
      * if error on node occurred during experiment, cycles with error are revoked 
      * and repeated again assuming that error was fixed for now.
      * 
      * @param errorConditionFrom - when was first error condition detected?
      * @param code - error code for protocol
      * @param reason - reason of revocation
      * @param description - detailed description of error condition to log - for human
      */
     public void moveExperimentStateBackward(Long errorConditionFrom, int code, String reason, String description){
         // if succ cycles from last reset is small. Do not repeat already repeated.
         int step=2;
         if (errorConditionFrom==null){
             step = succCyclesFromLastReset > 2 ? 2 : succCyclesFromLastReset;
             // get experiment step start time to be able to revoke it
             // if there is only few experiments done, cannot revoke more...
             if (step > this.lastExperimentTimes.size()){
                 step = this.lastExperimentTimes.size();
                 log.info("Cannot revoke more experiments, reducing step to: " + step);
             }
         } else {
             // compute how many steps are necessarry to rollback
             // find nearest experiment start time to the left
             step=0;
             boolean found=false;
             for(int cn=this.lastExperimentTimes.size(); step<cn; step++){
                 // geth i-th element = i-th experiment start time. 0=current experiment
                 // sequence MUST be decreasing!
                 Long lastExperimentStartTime = this.lastExperimentTimes.get(step);
                 log.info("lastExperimentStartTime["+step+"]=" + lastExperimentStartTime);
                 if (lastExperimentStartTime<errorConditionFrom){
                     // rollback this state as well - error occured here
                     step+=1;
                     found=true;
                     break;
                 }
             }
             
             // if not found left boundary, revoke all experiments contained
             if (found==false){
                 log.warn("Experiment error frame was not found; ErrCond: " + errorConditionFrom
                         + "; lastExperimentTimes.size() = " + this.lastExperimentTimes.size()
                         + "; curStep = " + step);
                 step = this.lastExperimentTimes.size();
             } else {
                 // do not repeat already repeated experiments...
                 step = Math.min(step, succCyclesFromLastReset+1);
             }
         }
         
         log.warn("Need to revoke&repeat last " + step + " experiments. LastSucc: " + succCyclesFromLastReset);
         
         // next main loop cycle will call next(). Thus we need to move step+1 backward.
         // Problem can be if current state is 2 and we need to revoke 2 experiments, thus
         // we need to move to -1 experiment to be prepared for next() call to get correct 
         // experiment to repeat.
         // From definition, transition=1 in initial state. Thus 
         int transitionNum = this.eState.getCurTransition();
         if (transitionNum < step){
             // extreme situation, we are at step lower that number of steps to 
             // revoke -> reset to null state
             this.eState.resetState();
         } else {
             this.eState.prev(step);
         }
         
         // revoke STEP experiments to database - in order to delete/ignore results
         // from this experiments, will be repeated again...
         long prevExperimentStart = System.currentTimeMillis();
         for(int i=0; i<step; i++){
             // revoke ith experiment from current. 0th = current experiment
             Long curExperimentStart = this.lastExperimentTimes.get(i);
             ExperimentDataRevokedCycles edrc = new ExperimentDataRevokedCycles();
             edrc.setExperiment(this.expInit.getExpMeta());
             edrc.setReasonCode(code);
             edrc.setReasonName(reason);
             edrc.setReasonDescription(description);
             edrc.setMiliStart(curExperimentStart);
             edrc.setMiliEnd(prevExperimentStart);
             this.storeData(edrc);
             
             prevExperimentStart = curExperimentStart;
         }
         
         // store info to experiment dependent log
         this.add2experimentLog("WARN", 2, reason + "; MovedBackward: " + step, description);
     }
     
     /**
      * Adds entry to experiment log
      * @param severity
      * @param code
      * @param reason
      * @param reasonDesc 
      */
     public synchronized void add2experimentLog(String severity, int code, String reason, String reasonDesc){
         ExperimentDataLog edl = new ExperimentDataLog();
         edl.setExperiment(this.expInit.getExpMeta());
         edl.setMiliEventTime(System.currentTimeMillis());
         edl.setReasonCode(code);
         edl.setReasonName(reason);
         edl.setReasonData(reasonDesc);
         edl.setSeverity(severity);
         this.storeData(edl);
     }
     
     public synchronized void add2experimentLog(String severity, int code, String reason, String reasonDesc, String desc){
         ExperimentDataLog edl = new ExperimentDataLog();
         edl.setExperiment(this.expInit.getExpMeta());
         edl.setMiliEventTime(System.currentTimeMillis());
         edl.setReasonCode(code);
         edl.setReasonName(reason);
         edl.setReasonData(reasonDesc);
         edl.setDescription(desc);
         edl.setSeverity(severity);
         this.storeData(edl);
     }
     
     public ExperimentInit getExpInit() {
         return expInit;
     }
 
     public void setExpInit(ExperimentInit expInit) {
         this.expInit = expInit;
     }
 
     @Override
     public NodeHandlerRegister getNodeReg() {
         return nodeReg;
     }
 
     public void setNodeReg(NodeHandlerRegister nodeReg) {
         this.nodeReg = nodeReg;
     }
     
     /**
      * Assumes that given message is identification packet, performs node liveness check
      * @param i
      * @param cMsg
      * @param mili 
      */
     public synchronized void identificationReceived(int i, CommandMsg cMsg, long mili){
         // nothing to do here, all responsibility now on nodeMonitor
     }
 
     /**
      * Message received event handler
      * !!! WARNING:
      * Please keep in mind that this method is executed by separate thread - 
      *  - messageListener notifier. Take a caution to avoid race conditions and concurrency 
      * problems.
      * 
      * EntityManager instance is NOT thread-safe, so this method cannot directly use
      * em instance from class attribute. New entityManager is needed.
      * 
      * 
      * @param i
      * @param msg 
      */
     @Override
     public synchronized void messageReceived(int i, Message msg, long mili) {
 ////        System.out.println("Message received: " + i);
 //        log.info("Message received: " + i + "; type: " + msg.amType()
 //                + "; dataLen: " + msg.dataLength() 
 //                + "; hdest: " + msg.getSerialPacket().get_header_dest()
 //                + "; hsrc: " + msg.getSerialPacket().get_header_src() 
 //                + "; mili: " + mili);
         
         // was this message already handled by specific handler?
         boolean genericMessage=true;
 
         // null packet testing
         // this could occur if in mote software is not set source of serial packet
         if (msg.getSerialPacket().get_header_src()==0 && !PrintfMsg.class.isInstance(msg)){
             // packet from 0 - suspicious
             log.info("NullPacket! Destination: " 
                     + msg.getSerialPacket().get_header_src() 
                     + "; \nSerialPacket: " + msg.getSerialPacket().toString()
                     + "; \nDataPacket: " + msg.toString());
             return;
         }
         
         // source nodeid
         int nodeIdSrc = msg.getSerialPacket().get_header_src();
         
         // printf message - printf client
         if (PrintfMsg.class.isInstance(msg)) {
             genericMessage=false;
             
             PrintfMsg pmsg = (PrintfMsg) msg;
             PrintfEntity printfEntity = new PrintfEntity();
             printfEntity.loadFromMessage(pmsg);
             printfEntity.setSendingNode(nodeIdSrc);
             printfEntity.setConnectedNode(nodeIdSrc);
             this.storeData(printfEntity);
             
             if (this.logPrintfMessages){
                 log.info("Printf msg from ["+nodeIdSrc+"]: " + printfEntity.getBuff());
             }
             
             return;
         }
         
         // update last seen record
         this.nodeReg.updateLastSeen(msg.getSerialPacket().get_header_src(), mili);
         
         // command message?
         if (CommandMsg.class.isInstance(msg)){
             // Command message
             final CommandMsg cMsg = (CommandMsg) msg;
             
             // identification received?
             if (    cMsg.get_command_code() == (short)MessageTypes.COMMAND_ACK
                  && cMsg.get_reply_on_command() == (short)MessageTypes.COMMAND_IDENTIFY)
             {            
                 // identification message is processed separately
                 this.identificationReceived(i, cMsg, mili);
                 genericMessage=false;
             } else {
                 genericMessage=true;
             }
         }
         
         // report message?
         if (MultiPingResponseReportMsg.class.isInstance(msg)){
             final MultiPingResponseReportMsg cMsg = (MultiPingResponseReportMsg) msg;
             //System.out.println("Report message: " + cMsg.toString());
             log.info("Report message: " + cMsg.toString());
             
             genericMessage=false;
         }
         
         // noise floor message
         if (NoiseFloorReadingMsg.class.isInstance(msg)){
             final NoiseFloorReadingMsg nMsg = (NoiseFloorReadingMsg) msg;
             log.info("NoiseFloorMessage: " + nMsg.toString());
             
             genericMessage=false;
         }
         
         // rssi message
         if (RssiMsg.class.isInstance(msg)){
             final RssiMsg rMsg = (RssiMsg) msg;
             //System.out.println("RSSI message: " + rMsg.toString());
             log.info("RSSI message: " + rMsg.toString());
         }
         
         // CTP status info
         if (CtpInfoMsg.class.isInstance(msg)){
             final CtpInfoMsg sMsg = (CtpInfoMsg) msg;
             if (logCTPMessages){
                 log.info("CTP status message from [" + msg.getSerialPacket().get_header_src()
                         + "] Msg: "  + sMsg.toString());
             }
             
             genericMessage=false;
         }
         
         // CTP report data
         if (CtpReportDataMsg.class.isInstance(msg)){
             final CtpReportDataMsg sMsg = (CtpReportDataMsg) msg;
             if (logCTPMessages){
                 log.info("CTP report message from [" + msg.getSerialPacket().get_header_src()
                         + "] Msg: "  + sMsg.toString());
             }
             
             genericMessage=false;
         }
         
         // generic message was probably not really handled -> store to protocol
         if (genericMessage){
             if (this.logGenericMessages){
                 log.info("Message received: " + i + "; type: " + msg.amType()
                     + "; dataLen: " + msg.dataLength() 
                     + "; hdest: " + msg.getSerialPacket().get_header_dest()
                     + "; hsrc: " + msg.getSerialPacket().get_header_src() 
                     + "; mili: " + mili 
                     + "; msg: " + msg.toString());
             }
             this.storeGenericMessageToProtocol(msg, i, false, true);            
         }
         
         this.lastMsg=null;
     }
     
     @Override
     public synchronized void messageReceived(int i, Message msg) {
         this.messageReceived(i, msg, 0);
     }
     
     /**
      * Sends multi ping request to specified node. Sending packet from this 
      * method is written to db protocol. Sending is synchronous -> block until 
      * message it sent or timeouted.
      * 
      * @param nodeId
      * @param txpower
      * @param channel
      * @param packets
      * @param delay
      * @param size
      * @param counterStrategySuccess
      * @param timerStrategyPeriodic 
      */
     @Transactional
     @Override
     public synchronized void sendMultiPingRequest(int nodeId, int txpower,
             int counter, int packets, int delay, int size, 
             boolean counterStrategySuccess, boolean timerStrategyPeriodic){
 
 	MultiPingMsg msg = new MultiPingMsg();
         msg.set_destination(MessageTypes.AM_BROADCAST_ADDR);
         msg.set_channel((short)0);
         msg.set_counter(counter);
         msg.set_counterStrategySuccess((byte) (counterStrategySuccess ? 1:0));
         msg.set_delay(delay);
         msg.set_packets(packets);
         msg.set_size((short)size);
         msg.set_timerStrategyPeriodic((byte) (timerStrategyPeriodic ? 1:0));
         msg.set_txpower((short)txpower);
         
         // now build database record for this request
         ExperimentMultiPingRequest mpr = new ExperimentMultiPingRequest();
         mpr.setMiliFromStart(System.currentTimeMillis());
         mpr.setExperiment(this.expInit.getExpMeta());
         mpr.setNode(nodeId);
         mpr.setNodeBS(nodeId);
         mpr.loadFromMessage(msg);
         this.storeData(mpr);
         
         // add message to send
         this.sendMessageToNode(msg, nodeId, false, true);
     }
     
     /**
      * Sends command message to node. Packet is stored to database
      * @param payload
      * @param nodeId 
      */
     @Transactional
     @Override
     public synchronized void sendCommand(CommandMsg payload, int nodeId){
         // store to database here
         // now build database record for this request
         ExperimentDataCommands mpr = new ExperimentDataCommands();
         mpr.setMilitime(System.currentTimeMillis());
         mpr.setExperiment(this.expInit.getExpMeta());
         mpr.setNode(nodeId);
         mpr.setNodeBS(nodeId);
         mpr.setSent(true);
         mpr.loadFromMessage(payload);
         this.storeData(mpr);
         
         // message to send is more flexible for our needs
        MessageToSend m2s = new MessageToSend(payload, nodeId, payload.toString());
         
         // all command send as blocking
         m2s.setBlockingSend(true);
         m2s.setBlockingTimeout(3000L);
         
         // if reset wait 3 secs
         if (payload.get_command_code() == MessageTypes.COMMAND_RESET){
             m2s.setPauseAfterSend(3000L);
         }
         
         this.sendMessageToNode(m2s, true);
     }
     
     /**
      * Send reset message
      * @param nodeId 
      */
     @Override
     public synchronized void sendReset(int nodeId){
         CommandMsg msg = new CommandMsg();
         msg.set_command_code((short) MessageTypes.COMMAND_RESET);
         
         //this.sendCommand(msg, nodeId);
         this.sendCommand(msg, nodeId);
     }
     
     /**
      * Set noise floor reading packet to node
      * @param nodeId
      * @param delay 
      */
     @Override
     public synchronized void sendNoiseFloorReading(int nodeId, int delay){
         CommandMsg msg = new CommandMsg();
         msg.set_command_code((short) MessageTypes.COMMAND_SETNOISEFLOORREADING);
         msg.set_command_data(delay);
         
         this.sendCommand(msg, nodeId);
     }
     
     /**
      * Send address recognition set command to node - can disable address recognition
      * to be able to spoof foreign messages from radio
      * 
      * @param nodeId
      * @param enabled 
      */
     @Override
     public synchronized void sendSetAddressRecognition(int nodeId, boolean enabled){
         CommandMsg msg = new CommandMsg();
         msg.set_command_code((short) MessageTypes.COMMAND_RADIO_ADDRESS_RECOGNITION_ENABLED);
         msg.set_command_data(enabled ? 1 : 0);
         
         this.sendCommand(msg, nodeId);
     }
     
     /**
      * Send node message to recompute its route update
      * 
      * @param nodeId
      * @param routeUpdate 
      */
     @Override
     public synchronized void sendCTPRouteUpdate(int nodeId, int routeUpdate){
         CommandMsg msg = new CommandMsg();
         msg.set_command_code((short) MessageTypes.COMMAND_CTP_ROUTE_UPDATE);
         msg.set_command_data(routeUpdate);
         
         this.sendCommand(msg, nodeId);
     }
     
     /**
      * Requests CTP info from node's perspective
      * 
      * @param nodeId
      */
     @Override
     public synchronized void sendCTPGetInfo(int nodeId){
         CommandMsg msg = new CommandMsg();
         msg.set_command_code((short) MessageTypes.COMMAND_CTP_GETINFO);
         msg.set_command_data(0);
         
         this.sendCommand(msg, nodeId);
     }
     
     /**
      * Requests CTP info from node's perspective
      * 
      * @param nodeId
      * @param n
      */
     @Override
     public synchronized void sendCTPGetNeighInfo(int nodeId, int n){
         CommandMsg msg = new CommandMsg();
         msg.set_command_code((short) MessageTypes.COMMAND_CTP_GETINFO);
         msg.set_command_data(1);
         msg.set_command_data_next(new int[] {n,0,0,0});
         
         this.sendCommand(msg, nodeId);
     }
     
     /**
      * Sets output TX power for CTP messages
      * @param nodeId
      * @param type
      * @param txpower 
      */
     @Override
     public synchronized void sendCTPTXPower(int nodeId, int type, int txpower){
         CommandMsg msg = new CommandMsg();
         msg.set_command_code((short) MessageTypes.COMMAND_CTP_CONTROL);
         msg.set_command_data(0);
         msg.set_command_data_next(new int[] {type,txpower,0,0});
         
         this.sendCommand(msg, nodeId);
     }
     
     /**
      * Sends CTP command
      * @param nodeId
      * @param type
      * @param txpower 
      */
     @Override
     public synchronized void sendCTPComm(int nodeId, int cmdId, int val){
         CommandMsg msg = new CommandMsg();
         msg.set_command_code((short) MessageTypes.COMMAND_CTP_CONTROL);
         msg.set_command_data(cmdId);
         msg.set_command_data_next(new int[] {val,0,0,0});
         
         this.sendCommand(msg, nodeId);
     }
     
     /**
      * Sets output TX power for CTP messages
      * @param nodeId
      * @param type
      * @param txpower 
      */
     @Override
     public synchronized void sendCTPLogger(int nodeId, int enable){
         CommandMsg msg = new CommandMsg();
         msg.set_command_code((short) MessageTypes.COMMAND_CTP_CONTROL);
         msg.set_command_data(1);
         msg.set_command_data_next(new int[] {enable,0,0,0});
         
         this.sendCommand(msg, nodeId);
     }
     
     /**
      * Sets CTP root for given node.
      * @param nodeId
      * @param isRoot 
      */
     @Override
     public synchronized void sendSetCTPRoot(int nodeId, boolean isRoot){
         CommandMsg msg = new CommandMsg();
         msg.set_command_code((short) MessageTypes.COMMAND_SET_CTP_ROOT);
         msg.set_command_data(isRoot ? 1 : 0);
         
         this.sendCommand(msg, nodeId);
     }
     
     /**
      * Send request to CTP reading to node
      * @param nodeId            
      * @param packets           number of packets to be send
      * @param delay             delay between two consecutive packets send
      * @param variability       absolute +- variability in packet delay, works only if timerStrategyPeriodic==false
      * @param dataSource        
      * @param counterStrategySuccess    
      * @param timerStrategyPeriodic 
      */
     @Override
     public synchronized void sendCTPRequest(int nodeId, int packets, int delay, int variability, 
             short dataSource, boolean counterStrategySuccess, boolean timerStrategyPeriodic, boolean unlimitedPackets){
         
         CtpSendRequestMsg msg = new CtpSendRequestMsg();
         msg.set_dataSource(dataSource);
         msg.set_delay(delay);
         msg.set_packets(packets);
         msg.set_delayVariability(variability);
         
         int flags = 0;
         flags |= counterStrategySuccess ? 0x1 : 0x0;
         flags |= timerStrategyPeriodic ? 0x2 : 0x0;
         flags |= unlimitedPackets ? 0x4 : 0x0;
         msg.set_flags(flags);
                 
         // now build database record for this request
         ExperimentCTPRequest mpr = new ExperimentCTPRequest();
         mpr.setMilitime(System.currentTimeMillis());
         mpr.setExperiment(this.expInit.getExpMeta());
         mpr.setNode(nodeId);
         mpr.setNodeBS(nodeId);
         mpr.loadFromMessage(msg);
         this.storeData(mpr);
         
         this.sendMessageToNode(msg, nodeId, true);
     }
     
     /**
      * Send selected defined packet to node.
      * Another methods may build custom command packet, it is then passed to this method
      * which sends it to all selected nodes
      * 
      * @param payload    data packet to send. Is CommandMessage
      * @param nodeId     nodeId to send message to
      * @param protocol   if TRUE then message is written to generic message protocol
      */    
     @Override
     public synchronized void sendMessageToNode(Message payload, int nodeId, boolean protocol){
         this.sendMessageToNode(payload, nodeId, protocol, false);
     }
     
     /**
      * Send selected defined packet to node.
      * Another methods may build custom command packet, it is then passed to this method
      * which sends it to all selected nodes
      * 
      * @param payload    data packet to send.
      * @param nodeId     nodeId to send message to
      * @param protocol   if TRUE then message is written to generic message protocol
      * @param blocking   if TRUE then method blocks until message is sent by sender
      */    
     @Override
     public synchronized void sendMessageToNode(Message payload, int nodeId, boolean protocol, boolean blocking){
         // build custom message to send
         MessageToSend m2s = new MessageToSend(payload, nodeId, null);
         m2s.setBlockingSend(blocking);
         m2s.setBlockingTimeout(3000L);
         
         // for blocking need to set string key correctly
         if (blocking){
             m2s.setListenerKey(payload.toString());
         }
         
         this.sendMessageToNode(m2s, protocol);
     }
     
     /**
      * Send selected defined packet to node.
      * Another methods may build custom command packet, it is then passed to this method
      * which sends it to all selected nodes
      * 
      * @param payload    data packet to send. 
      * @param protocol   if TRUE then message is written to generic message protocol
      */    
     @Override
     public synchronized void sendMessageToNode(MessageToSend payload, boolean protocol){
         // decide whether send message to all nodes 
         if (payload.getDestination()<0){
             // mass send to all registered nodes
             Collection<NodeHandler> values = this.nodeReg.values();
             for (NodeHandler nh : values){
                 // can add message?
                 if (nh.canAddMessage2Send()==false){
                     log.error("From some reason message cannot be sent to this node currently, please try again later. NodeId: " + nh.getNodeId());
                     continue;
                 }
                 
                 try {
                     // store to protocol?
                     if (protocol){
                         this.storeGenericMessageToProtocol(payload.getsMsg(), nh.getNodeId(), true, false);
                     }
 
                     // add to send queue
                     log.debug("Message to send for node: " + nh.getNodeId() + "; Command: " + payload);
                     
                     payload.setDestination(nh.getNodeId());
                     nh.addMessage2Send(payload);
                 }  catch (Exception ex) {
                     log.error("Cannot send CmdMessage to nodeId: " + nh.getNodeId() , ex);
                 }
             }
         } else {
             // sending message only to specific node
             try {           
                 Integer nId = Integer.valueOf(payload.getDestination());
                 // get node from node register
                 if (this.nodeReg.containsKey(nId)==false){
                     log.error("Cannot send message to node " + nId + "; No such node found in register");
                     return;
                 }
 
                 NodeHandler nh = this.nodeReg.get(nId);
                 if (nh.canAddMessage2Send()==false){
                     log.error("From some reason message cannot be sent to this node currently, please try again later. NodeId: " + nId);
                     return;
                 }
 
                 // store to protocol?
                 if (protocol){
                     this.storeGenericMessageToProtocol(payload.getsMsg(), nId, true, false);
                 }
 
                 // add to send queue
                 log.debug("Message to send for node: " + nId + "; Command: " + payload);
                 nh.addMessage2Send(payload);
                     
             }  catch (Exception ex) {
                 log.error("Cannot send CmdMessage to nodeId: " + payload.getDestination(), ex);
             }
         }
     }
     
     /**
      * Prints interface with comments
      */
     @Override
     public void usage(){
         System.out.println("Methodlist:");
         System.out.println("    public void interrupt();\n" + 
     			"    public void work();\n" + 
     			"    public void main();\n" + 
     			"    \n" + 
     			"    // start suspended?\n" + 
     			"    public void unsuspend();\n" + 
     			"    \n" + 
     			"    public void sendCommand(CommandMsg payload, int nodeId);\n" + 
     			"    public void resetAllNodes();\n" + 
     			"    public void sendMultiPingRequest(int nodeId, int txpower,\n" + 
     			"            int channel, int packets, int delay, int size, \n" + 
     			"            boolean counterStrategySuccess, boolean timerStrategyPeriodic);\n" + 
     			"    \n" + 
     			"    public void sendReset(int nodeId);\n" + 
     			"    public void sendNoiseFloorReading(int nodeId, int delay);\n" + 
     			"    public void sendMessageToNode(Message payload, int nodeId, boolean protocol);\n" + 
     			"    public void sendSetAddressRecognition(int nodeId, boolean enabled);\n" + 
     			"    public void sendSetCTPRoot(int nodeId, boolean isRoot);\n" + 
     			"    public void sendCTPRequest(int nodeId, int packets, int delay, \n" + 
     			"            short dataSource, boolean counterStrategySuccess, boolean timerStrategyPeriodic);\n" + 
     			"    \n" + 
     			"    public void sendCTPRouteUpdate(int nodeId, int routeUpdate);\n" + 
     			"    public void sendCTPGetInfo(int nodeId);\n" + 
     			"    public void sendCTPGetNeighInfo(int nodeId, int n);\n" + 
     			"    public void sendCTPTXPower(int nodeId, int type, int txpower);\n" + 
     			"    \n" + 
     			"    public void storeGenericMessageToProtocol(Message payload, int nodeId, boolean sent, boolean external);\n" + 
     			"    public void getNodesLastSeen();\n" + 
     			"    public List<Integer> getNodesLastResponse(long mili);\n" + 
     			"    public NodeHandlerRegister getNodeReg();\n" + 
     			"    public void hwresetAllNodes();\n" + 
     			"    public void hwresetNode(int nodeId);\n" + 
     			"    public void resetNode(int nodeId);\n" + 
     			"    \n" + 
     			"    public void storeData(Object o);\n" + 
     			"    \n" + 
     			"    public ExperimentState geteState();\n" + 
     			"    public void seteState(ExperimentState eState);\n" + 
     			"    \n" + 
     			"    public ExperimentStatGen getStatGen();\n" + 
     			"    public void usage();\n" + 
     			"    \n" + 
     			"    \n" + 
     			"    public void suspendExperiment();\n" + 
     			"    \n" + 
     			"    /**\n" + 
     			"     * Restarts all nodes before experiment and waits for them to show alive again\n" + 
     			"     */\n" + 
     			"    public void restartNodesBeforeExperiment() throws TimeoutException;\n" + 
     			"    \n" + 
     			"    public int getExperiment2Start();\n" + 
     			"\n" + 
     			"    /**\n" + 
     			"     * Sets experiment to start after unsuspend\n" + 
     			"     * 1=none\n" + 
     			"     * 2=rssi\n" + 
     			"     * 3=ctp\n" + 
     			"     * @param experiment2Start \n" + 
     			"     */\n" + 
     			"    public void setExperiment2Start(int experiment2Start);" +
     			"\n" +
     			"    public ExperimentCtp getExpCTP();\n" + 
     			"    public NodeReachabilityMonitor getNodeMonitor();");
         
     }
     
     /**
      * Stores generic message to protocol log
      * @param payload
      * @param nodeId 
      * @param sent  if true message is marked as sent from application, otherwise 
      *              message is received to application
      * @param external if true => method was invoked from different thread from main, then is used emThread
      */
     //@Transactional
     @Override
     public void storeGenericMessageToProtocol(Message payload, int nodeId, boolean sent, boolean external){
         final ExperimentDataGenericMessage gmsg = new ExperimentDataGenericMessage();
         gmsg.setMilitime(System.currentTimeMillis());
         gmsg.setExperiment(this.expInit.getExpMeta());
         gmsg.setNode(nodeId);
         gmsg.setNodeBS(nodeId);
         gmsg.setSent(sent);
 
         gmsg.setAmtype(payload.amType());
         gmsg.setLen(payload.dataLength());
         gmsg.setStringDump(payload.toString());
         this.storeData(gmsg);
     }
     
     /**
      * Returns nodes that have (NOW()-lastSeen) > mili. Time from last seen is 
      * greater than mili
      * 
      * @param mili 
      */
     @Override
     public List<Integer> getNodesLastResponse(long mili){
        long currTime = System.currentTimeMillis();
        List<Integer> lnodeList = new LinkedList<Integer>();
        
        Collection<NodeHandler> nhvals = this.nodeReg.values();
         if (nhvals.isEmpty()){
             System.out.println("Node register is empty");
             return lnodeList;
         }
 
         for(NodeHandler nh : nhvals){
             long lastSeen = nh.getNodeObj().getLastSeen();
             if ((currTime-lastSeen) > mili){
                 lnodeList.add(nh.getNodeId());
             }
         }
         
         return lnodeList;
     }
     
     /**
      * Prints node's last seen values
      * @param seconds 
      */
     @Override
     public void getNodesLastSeen(){
         Collection<NodeHandler> nhvals = this.nodeReg.values();
         if (nhvals.isEmpty()){
             System.out.println("Node register is empty");
             return;
         }
         
         // human readable date formater
         DateFormat formatter = new SimpleDateFormat("HH:mm:ss:SSS");
         
         System.out.println("LastSeen indicators: ");
         Iterator<NodeHandler> iterator = nhvals.iterator();
         
         while(iterator.hasNext()){
             NodeHandler nh = iterator.next();
             long lastSeen = nh.getNodeObj().getLastSeen();
             
             // convert last seen to human readable format
             Date date = new Date(lastSeen);
             System.out.println("NodeID: " + nh.getNodeId() + ";\t LastSeen: " + 
                 formatter.format(date) + ";\t type: " + nh.getType());
         }
     }
     
     /**
      * Pause execution of this thread for specified time
      * @param mili 
      */
     public void pause(long mili){
         try {
             Thread.sleep(mili);
         } catch (InterruptedException ex) {
             log.error("Cannot sleep " + ex);
         }
     }
     
     /**
      * Pause execution of this thread for specified time
      * @param mili
      * @param nano 
      */
     public void pause(long mili, int nano){
         try {
             Thread.sleep(mili, nano);
         } catch (InterruptedException ex) {
             log.error("Cannot sleep " + ex);
         }
     }
 
     public Console getConsole() {
         return console;
     }
 
     public void setConsole(Console console) {
         this.console = console;
     }
 
     public boolean isSuspended() {
         return suspended;
     }
 
     /**
      * Unsuspends experiment. If specified, experiment is suspended before main
      * logic start to prepare environment and update some settings via console. 
      * When is everything prepared, unsuspend is called. As a consequence suspend 
      * sleep cycle is ended and main experiment method is called.
      */
     @Override
     public synchronized void unsuspend(){
         this.suspended=false;
     }
 
     
     /**
      * Suspends after finishing atomic cycle of experiment
      */
     @Override
     public synchronized void suspendExperiment(){
         this.suspended=true;
     }
     
     
     /**
      * Gets last received message - for console use. 
      * Has no special purpose in main logic, only informative output
      */
     public Message getLastMsg() {
         return lastMsg;
     }
     
     /**
      * Setting this to false will exit main loop
      * @param running 
      */
     public synchronized void setRunning(boolean running) {
         this.running = running;
     }
     
     /**
      * Returns state of running flag. If false => thread probably exited
      * If true thread my or may not be running
      * @return 
      */
     public boolean isRunning() {
         return running;
     }
     
     @Override
     public synchronized void storeData(Object o){
         this.expRecords.storeEntity(o);
     }
 
     @Override
     public ExperimentState geteState() {
         return eState;
     }
 
     @Override
     public void seteState(ExperimentState eState) {
         this.eState = eState;
     }
 
     @Override
     public ExperimentStatGen getStatGen() {
         return statGen;
     }
 
     @Override
     public int getExperiment2Start() {
         return experiment2Start;
     }
 
     @Override
     public void setExperiment2Start(int experiment2Start) {
         this.experiment2Start = experiment2Start;
     }
 
     public ExperimentCtp getExpCTP() {
         return expCTP;
     }
 
     public NodeReachabilityMonitor getNodeMonitor() {
         return nodeMonitor;
     }
 
     public boolean isLogGenericMessages() {
         return logGenericMessages;
     }
 
     public void setLogGenericMessages(boolean logGenericMessages) {
         this.logGenericMessages = logGenericMessages;
     }
 
     public ConsoleHelper getConsoleHelper() {
         return consoleHelper;
     }
 
     public void setConsoleHelper(ConsoleHelper consoleHelper) {
         this.consoleHelper = consoleHelper;
     }
 
     public boolean isLogCTPMessages() {
         return logCTPMessages;
     }
 
     public void setLogCTPMessages(boolean logCTPMessages) {
         this.logCTPMessages = logCTPMessages;
     }
 
     public boolean isLogCommandMessages() {
         return logCommandMessages;
     }
 
     public void setLogCommandMessages(boolean logCommandMessages) {
         this.logCommandMessages = logCommandMessages;
     }
 
     public boolean isRestartNodesBeforeExperiment() {
         return restartNodesBeforeExperiment;
     }
 
     public void setRestartNodesBeforeExperiment(boolean restartNodesBeforeExperiment) {
         this.restartNodesBeforeExperiment = restartNodesBeforeExperiment;
     }
 
     public boolean isLogPrintfMessages() {
         return logPrintfMessages;
     }
 
     public void setLogPrintfMessages(boolean logPrintfMessages) {
         this.logPrintfMessages = logPrintfMessages;
     }
 }
