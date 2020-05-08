 package fi.motetool.motetool;
 
 import fi.motetool.nodes.NodePlatformFactory;
 import fi.motetool.nodes.NodePlatform;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Queue;
 import java.util.concurrent.ConcurrentLinkedQueue;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * USB arbitrator class - manages connected nodes
  * 
  * @extension:
  * Spawn new thread for USBarbitrator which periodically (approx. each 5 seconds)
  * checks connected nodes and notifies listener when something changed. Initialization
  * data are passed from USBarbitrator. USBarbitrator has strategic important methods, 
  * maybe this can be handled by USBarbitrator itself?
  * 
  * 
  * @author ph4r05
  */
 public class USBarbitratorImpl {
     protected static final Logger log = LoggerFactory.getLogger(USBarbitratorImpl.class);
    protected static final String UDEV_RULES_LINE_PATTERN = "^ATTRS\\{serial\\}\\s*==\\s*\\\"([0-9a-zA-Z_]+)\\\",\\s*NAME\\s*=\\s*\\\"([0-9a-zA-Z_]+)\\\".*";
     protected static final String NODE_ID_PATTERN = ".*?([0-9]+)$";
     protected static final String NODEID_INTERVAL_PATTERN = "#([0-9]+)-([0-9]+)";
     protected static final String MAKE="/usr/bin/make";
     
     // serial->motelist record association
     // here is hidden multikey map for easy node searching by serial, nodeid, devpath
     protected NodeSearchMap moteList = null;
 
     /**
      * Whether we want to check node list from config string to really connected
      * nodes
      */
     protected boolean checkConfigNodesToConnected=true;
     
     /**
      * Threadcount to use during reprogram
      */
     private int threadCount;
     
     /**
      * Retry counter for unsuccessful operation.
      */
     private int retryCount=3;
     
     /**
      * Performs real detection of connected nodes and returns answer as map, indexed
      * by node serial id. Detection is done by external motelist command
      * @return 
      */
     public Map<String, NodeConfigRecord> getConnectedNodes() {
         Map<String, NodeConfigRecord> localmotelist = new HashMap<String, NodeConfigRecord>();
         String motelistCommand = App.getRunningInstance().getMotelistCommand() + " -usb -c";
         log.info("Will use motelist command: " + motelistCommand);
 
         try {
             // motelist records
             LinkedList<NodeConfigRecord> mlistRecords = new LinkedList<NodeConfigRecord>();
             // parse udev rules list to complete information - get mapping 
             // USB serial -> device path (created by udev)
             Map<String, NodeConfigRecordLocal> udevConfig = loadUdevRules();
 
             // execute motelist command
             Process p = Runtime.getRuntime().exec(motelistCommand);
             BufferedReader bri = new BufferedReader(new InputStreamReader(p.getInputStream()));
             String line = null;
             while ((line = bri.readLine()) != null) {
                 // process detected motes here                 
                 if (line.startsWith("No devices found")) {
                     log.info("No devices was found, return null");
                     break;
                 }
 
                 // parse motelist output
                 NodeConfigRecord motelistOutput = this.parseMotelistOutput(line);
 
                 // if udev device alias present, map it
                 if (udevConfig.containsKey(motelistOutput.getSerial())) {
                     motelistOutput.setDeviceAlias(udevConfig.get(motelistOutput.getSerial()).getDevice());
                     motelistOutput.setNodeId(udevConfig.get(motelistOutput.getSerial()).getNodeid());
 
                     // now have device alias, if nonempty it has precedense to 
                     // nodePath for connection string
                     if (motelistOutput.getDeviceAlias() != null
                             && motelistOutput.getDeviceAlias().isEmpty() == false) {
                         // need to get platform again :(
                         NodePlatform platform = NodePlatformFactory.getPlatform(motelistOutput.getPlatformId());
                         motelistOutput.setConnectionString(platform.getConnectionString(motelistOutput.getDeviceAlias()));
                     }
                 }
 
                 log.info("MoteRecord: " + motelistOutput.toString());
 
                 // add parsed node record to list for further processing
                 mlistRecords.add(motelistOutput);
                 // put motelist
                 localmotelist.put(motelistOutput.getSerial(), motelistOutput);
             }
             bri.close();
 
             // sunchronous call, wait for command completion
             p.waitFor();
         } catch (IOException ex) {
             log.error("IOException error, try checking motelist command", ex);
         } catch (InterruptedException ex) {
             log.error("Motelist command was probably interrupted", ex);
         }
 
         return localmotelist;
     }
 
     /**
      * Detects connected nodes via command: motelist -usb -c
      * New detected nodes not present in database are stored. Database is updated
      * when connection of nodes was changed
      */ 
     public void detectConnectedNodes() {
         if (App.getRunningInstance().isDebug()) {
             log.info("Debugging mode enabled in USBarbitrator");
         }
 
         // if is map nonempty
         if (this.moteList != null && this.moteList.isEmpty() == false) {
             log.debug("moteList map is nonempty, will be replaced with fresh data");
         }
         
         // perform detection
         Map<String, NodeConfigRecord> connectedNodes = this.getConnectedNodes();
         
         // init new node search map and put all data from motelist map
         this.moteList = new NodeSearchMap();
         this.moteList.putAll(connectedNodes);
         
         // show loaded data
         if (App.getRunningInstance().isShowBinding()){
             this.showBinding();
         }
     }
     
     /**
      * Builds motelist record from one line of motelist output
      * @param output
      * @return 
      */
     protected NodeConfigRecord parseMotelistOutput(String output){
         if (output==null) {
             log.error("Empty line in parseMotelistOutput");
             throw new NullPointerException("Null line");
         }
         
         NodeConfigRecord rec = new NodeConfigRecord();
         String[] split = output.split(",");
         if (split.length != 6){
             log.error("Motelist output is different from expected one, please inspect it: " + output);
             throw new IllegalArgumentException("Line is different as expected - command output probably changed");
         }
         
         rec.setBus(split[0]);
         rec.setDev(split[1]);
         rec.setUsbPath(split[2]);
         rec.setSerial(split[3]);
         rec.setDevicePath(split[4]);
         rec.setDescription(split[5]);
         
         // determine platform ID here, violates abstraction, but now meets KISS
         // principle. Problem: reasonable way how to convert description string
         // to platform, now use platformFactory
         NodePlatform platform = NodePlatformFactory.getPlatform(rec.getDescription());
         rec.setPlatformId(platform.getPlatformId());
         
         // cannot determine connection string here correctly - need to wait for 
         // device file alias from udev. now use simple node
         rec.setConnectionString(platform.getConnectionString(rec.getDevicePath()));
         return rec;
     }
     
     /**
      * Parse udev rules from udev config file = udev config file format could change
      * this is quite temporary method to ease initial db population to detect node 
      * dev aliases
      * 
      * @return Mapping USB serial -> node file
      * @throws java.io.FileNotFoundException
      */
     protected Map<String, NodeConfigRecordLocal> loadUdevRules() throws FileNotFoundException, IOException{
         String udevRulesFilePath = App.getRunningInstance().getProps().getProperty("moteUdevRules");
         if (udevRulesFilePath==null || udevRulesFilePath.isEmpty()){
             log.warn("udev rules file path is empty, cannot detect alias nodes");
             return new HashMap<String, NodeConfigRecordLocal>();
         }
         
         // file exists & can read it?
         File udevRulesFile = new File(udevRulesFilePath);
         if (udevRulesFile.exists()==false || udevRulesFile.canRead()==false){
             log.warn("Udev file probably does not exist or cannot be read. File: " + udevRulesFilePath);
             return new HashMap<String, NodeConfigRecordLocal>();
         }
         
         // init returning map
         Map resultMap = new HashMap<String, NodeConfigRecordLocal>();
         
         log.debug("Loading udev configuration");
         
         // open reader for file to read file by lines
         BufferedReader br = new BufferedReader(new FileReader(udevRulesFile));
         
         // we will need to parse config file, compile regex pattern
         Pattern linePattern = Pattern.compile(UDEV_RULES_LINE_PATTERN, Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
         Pattern nodeNumberPattern = Pattern.compile(NODE_ID_PATTERN, Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
                
         String strLine;
         //Read File Line By Line
         while ((strLine = br.readLine()) != null) {
             // process each line of udev rules config file
             // format (by example): ATTRS{serial}=="XBTO3VKQ", NAME="mote_telos48", MODE="0666"
             // if line does not match to this pattern, it is skipped, lines starting with # are skiped
             // as well since its are comments.
             strLine = strLine.trim();
             // is comment or empty line?
             if (strLine.isEmpty() || strLine.startsWith("#")){
                 continue;
             }
             
             // matches pattern?
             Matcher m = linePattern.matcher(strLine);
             
             boolean b = m.matches();
             if(b==false) {
                 // no match, different or malformed line
                 log.debug("Line was not matched:" + strLine);
                 continue;
             }
             
             // matched, extract data
             String group = m.group();           
             if (group==null 
                     || group.isEmpty() 
                     || m.group(1)==null 
                     || m.group(2)==null){
                 // mallformed, error
                 log.warn("Cannot parse this line of idev config file: " + strLine);
                 continue;
             }
             
             String serial = m.group(1);
             String device = m.group(2);
             log.info("Read info from config file: Serial=" + serial + "; device=" + device);
             
             // prepend /dev/ to device
             device = "/dev/" + device;
             
             // conflict check - if duplicate serial occurs, warn user
             if (resultMap.containsKey(serial)){
                 log.error("Error occurred, duplicate serial detected in config file, "
                         + "please resolve this issue. Returning empty map. Ambiguation present.");
                 log.debug("First record: serial=" + serial + " device=" + resultMap.get(serial));
                 log.debug("Second record: serial=" + serial + " device=" + device);
                 return new HashMap<String, NodeConfigRecordLocal>();
             }
             
             NodeConfigRecordLocal ncr = new NodeConfigRecordLocal();
             ncr.setDevice(device);
             
             // try to extract node id
             Matcher mId = nodeNumberPattern.matcher(device);
             if (mId.matches() && mId.group(1)!=null){
                 log.info("Node ID discovered: " + mId.group(1));
                 String nodeIdString = mId.group(1);
                 
                 // to integer conversion
                 try {
                     ncr.setNodeid(-1);
                     ncr.setNodeid(Integer.parseInt(nodeIdString));
                 } catch (Exception e){
                     log.error("Integer conversion error: " + nodeIdString);
                 }
             }
 
             resultMap.put(serial, ncr);
         }
         //Close the input stream
         br.close();
         
         return resultMap;
     }
 
     /**
      * Returns list of NodeConfigRecords for nodes to connect to from config strings
      * of inclusion/exclusion.
      * 
      * @problem: includeString, excludeString are not strong enough to express all wanted situations easily
      * @extension: for more sophisticated node filters can be used rsync filter syntax
      * + means include, - means exclude. Records can be passed as list of filter lines
      * List<NodeSelectorRecord> configLines
      * 
      * Filters: magic constant ALL means all nodes already detected, all next config records are ignored
      *  node identifiers are comma separated
      *  identifier beginning with # following only by decimal digits means node id.
      *  identifier beginning with / means node device path - first aliases then nodePaths are searched
      *  otherwise is identifier considered as serial number.
      * 
      * @param includeString
      * @param excludeString
      * @return 
      */
     public List<NodeConfigRecord> getNodes2connect(String includeString, String excludeString){
         // include string parsing
         if (includeString==null){
             log.warn("Include string is empty, using all nodes");
             includeString="ALL";
         }
         
         if (excludeString==null){
             // exclude string can be empty, set empty string, corresponds to NONE node exclude
             excludeString="";
         }
         
         // list to return
         // first get include list and add to nodes2return
         List<NodeConfigRecord> nodes2return = this.parseNodeSelectorString(includeString);
         // get working copy of list - arrayList - fast accessing
         List<NodeConfigRecord> nodes2return_work = new ArrayList<NodeConfigRecord>(nodes2return);        
         // exclude nodes, will be removed from nodes2return
         List<NodeConfigRecord> excludeNodes = this.parseNodeSelectorString(excludeString);
         Iterator<NodeConfigRecord> iterator = excludeNodes.iterator();
         while(iterator.hasNext()){
             NodeConfigRecord curRec = iterator.next();
             nodes2return_work.remove(curRec);
         }
         
         return nodes2return_work;
     }
     
     /**
      * Parses node selector string and returns corresponding records for 
      * currently connected nodes.
      * 
      * Interval available for node id
      * 
      * @param selector
      * @return 
      */
     public List<NodeConfigRecord> parseNodeSelectorString(String selector){
         // include string parsing
         if (selector==null){
             throw new NullPointerException("Node selector cannot be null");
         }
         
         Pattern nodeIdIntervalPattern = Pattern.compile(NODEID_INTERVAL_PATTERN, Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
         
         // trim first
         selector = selector.trim();
         
         // list to return
         List<NodeConfigRecord> nodes2return = new LinkedList<NodeConfigRecord>();
         
         // save energy here
         if (selector.isEmpty()){
             return nodes2return;
         }
         
          // split by comma
         String[] selectorSplit = selector.split(",");
         ArrayList<String> selectorArray = new ArrayList<String>(selectorSplit.length);
         
         // trim each substring from spaces, remove empty lines
         for(int i=0; i<selectorSplit.length; i++){
             String cur = selectorSplit[i].trim();
             if (cur.isEmpty()){
                 continue;
             }
             
             selectorArray.add(cur);
         }
         
         // process first include, then exclude
         Iterator<String> iterator = selectorArray.iterator();
         while(iterator.hasNext()){
             String id = iterator.next();
             
             // begin with?
             if (id.startsWith("/")){
                 // device string
                 // find by device string - here use advantages o NodeSearchMap
                 NodeConfigRecord ncr = this.getNodeByPath(id);
                 if (ncr!=null){
                     // contains
                     nodes2return.add(ncr);
                 } else {
                     log.warn("Device path: " + id + " cannot be found among connected nodes");
                     continue;
                 }
             } else if (id.startsWith("#")){
                 // is interval #low-high ?
                 // matches pattern?
                 Matcher m = nodeIdIntervalPattern.matcher(id);
             
                 if (m.matches()){
                     // matched, extract data
                     String group = m.group();           
                     if (group==null 
                             || group.isEmpty() 
                             || m.group(1)==null 
                             || m.group(2)==null){
                         // mallformed, error
                         log.warn("Cannot parse this line of nodeId interval record: " + id);
                         continue;
                     }
                     
                     // get limits, assure min/max
                     Integer nodeIdIntervalLow = null;
                     Integer nodeIdIntervalHigh = null;
                     try {
                         nodeIdIntervalLow = Integer.parseInt(m.group(1));
                         nodeIdIntervalHigh = Integer.parseInt(m.group(2));
                         
                         int low = Math.min(nodeIdIntervalLow, nodeIdIntervalHigh);
                         int high = Math.max(nodeIdIntervalLow, nodeIdIntervalHigh);
                         nodeIdIntervalLow = low;
                         nodeIdIntervalHigh = high;
                         
                     } catch(NumberFormatException e){
                         log.error("Cannot convert nodeId interval string: [" + m.group() + "]", e);
                         continue;
                     }
                     
                     // iterate over interval
                     for(int cNode = nodeIdIntervalLow; cNode <= nodeIdIntervalHigh; cNode++){
                         // find by id - here use advantages of NodeSearchMap
                         NodeConfigRecord ncr = this.getNodeById(cNode);
                         if (ncr!=null){
                             // contains
                             nodes2return.add(ncr);
                         } else {
                             log.warn("NodeId: " + cNode + " cannot be found among connected nodes");
                         }
                     }
                     
                     continue;
                 }
                 
                 // node id
                 // need to parse string to integer
                 String newId = id.substring(1);
                 Integer nodeId = null;
                 try {
                     nodeId = Integer.parseInt(newId);
                 } catch(NumberFormatException e){
                     log.error("Cannot convert nodeId string: [" + newId + "] to integer", e);
                     continue;
                 }
                 
                 // find by id - here use advantages of NodeSearchMap
                 NodeConfigRecord ncr = this.getNodeById(nodeId);
                 if (ncr!=null){
                     // contains
                     nodes2return.add(ncr);
                 } else {
                     log.warn("NodeId: " + nodeId + " cannot be found among connected nodes");
                 }
             } else if (id.equals("ALL")){
                 // ALL nodes, add every node
                 if (this.moteList==null || this.moteList.isEmpty()){
                     continue;
                 }
                 
                 // ALL option is meaningless unless we know detect connected nodes
                 // otherwise we don't know what does mean ALL...
                 if (this.ableToDetectConnectedNodes()==false){
                     log.warn("Cannot add ALL nodes - cannot detect connected nodes");
                     continue;
                 }
                 
                 Iterator<Entry<String, NodeConfigRecord>> itSet = this.moteList.entrySet().iterator();
                 while(itSet.hasNext()){
                     Entry<String, NodeConfigRecord> entry = itSet.next();
                     nodes2return.add(entry.getValue());
                 }
             } else {
                 // node serial, quick mapping
                 NodeConfigRecord ncr = this.getNodeBySerial(id);
                 if (ncr!=null){
                     nodes2return.add(ncr);
                 } else {
                     // node does not exists
                     log.warn("Node with serial: [" + id + "] was not found in list, "
                             + "probably is not connected, ignoring");
                 }
             }
         }
         
         return nodes2return;
     }
     
     public NodeConfigRecord getNodeById(Integer id) {
         if (this.moteList==null || this.moteList.containsKeyNodeId(id)==false) return null;
         return this.moteList.getByNodeId(id);
     }
     
     public NodeConfigRecord getNodeByPath(String path) {
         if (this.moteList==null || this.moteList.containsKeyDevPath(path) ==false) return null;
         return this.moteList.getByDevPath(path);
     }
 
     public NodeConfigRecord getNodeBySerial(String serial) {
         if (this.moteList==null || this.moteList.containsKey(serial)) return null;
         return this.moteList.getBySerial(serial);
     }
         
     public boolean ableToDetectConnectedNodes() {
         return true;
     }
     
     /**
      * Dumps information about currently loaded node binding to stdout
      */
     
     public void showBinding(){
         System.out.println(String.format("Dumping output (by nodeID, udev file=[%s])", 
                 App.getRunningInstance().getProps().getProperty("moteUdevRules")));
         
         List<NodeConfigRecord> ncrList = new ArrayList<NodeConfigRecord>(this.moteList.size());
         for(String serial : this.moteList.keySet()){
             ncrList.add(this.moteList.get(serial));
         }
         
         // sort
         Collections.sort(ncrList);
         
         // output
         for(NodeConfigRecord ncr : ncrList){
             System.out.println(ncr.getHumanOutput());
         }
     }
     
     /**
      * Entry method for node reset according to a given list.
      * @param nodes2connect 
      */
     public void resetNodes(List<NodeConfigRecord> nodes2connect) {
         // info at the beggining
         if (threadCount == 1) {
             System.out.println("Following nodes will be restarted: ");
             Iterator<NodeConfigRecord> iterator = nodes2connect.iterator();
             while (iterator.hasNext()) {
                 NodeConfigRecord ncr = iterator.next();
                 System.out.println(ncr.getHumanOutput());
 
                 this.resetNode(ncr);
             }
         } else {
             System.out.println("Following nodes will be reseted: ");
             Queue<NodeConfigRecord> ncrJobQueue = prepareJobQueue(nodes2connect, true);
 
             // failed flash
             Queue<NodeConfigRecord> nodesFailed = new ConcurrentLinkedQueue<NodeConfigRecord>();
 
             // Output string from worker
             Queue<String> jobOutput = new ConcurrentLinkedQueue<String>();
 
             // spawn jobs
             ExecutorService tasks = Executors.newFixedThreadPool(threadCount);
             for (int i = 0; i < threadCount; i++) {
                 NodeResetWorker resetWorker = new NodeResetWorker();
                 resetWorker.jobOutput = jobOutput;
                 resetWorker.ncrJobQueue = ncrJobQueue;
                 resetWorker.nodesFailed = nodesFailed;
                 
                 tasks.execute(resetWorker);
             }
 
             // termination wait
             try {
                 tasks.shutdown();
                 long startTime = System.currentTimeMillis();
                 while (true) {
                     long curTime = System.currentTimeMillis();
                     if ((curTime - startTime) > 1000 * 60 * 60) {
                         break;
                     }
 
                     Thread.sleep(200);
 
                     // check output queue
                     while (jobOutput.isEmpty() == false) {
                         String line = jobOutput.poll();
                         if (line == null) {
                             break;
                         }
 
                         System.out.println(line);
                     }
 
                     // terminated?
                     if (tasks.isTerminated()) {
                         break;
                     }
                 }
 
             } catch (InterruptedException ex) {
                 log.error("interrupted ", ex);
             }
 
             // was there some errors?
             if (nodesFailed.isEmpty() == false) {
                 List<NodeConfigRecord> failedList = new ArrayList(nodesFailed);
                 Collections.sort(failedList);
 
                 System.out.println("Some errors occurred during reset, problematic nodes: ");
                 for (NodeConfigRecord ncr : failedList) {
                     System.out.println(ncr.getHumanOutput());
                 }
             } else {
                 System.out.println("All nodes reseted successfully!");
             }
         }
     }
     
     /**
      * Reprograms specified nodes with makefile.
      * Only path to directory with makefile is required. Then is executed
      * make telosb install,X bsl,/dev/mote_telosX
      * 
      * @param nodes2connect
      * @param makefileDir  absolute path to makefile directory with mote program
      */
     public void reprogramNodes(List<NodeConfigRecord> nodes2connect, String makefileDir){        
         // test if makefile exists
         File makefile = new File(makefileDir + "/Makefile");
         File makefileDirF = new File(makefileDir);
         // makefile dir test
         if (makefileDirF.exists()==false || makefileDirF.isDirectory()==false){
             log.error("Makefile directory invalid (does not exist OR is not a directory): " + makefile.getPath());
             System.err.println("Makefile directory invalid (does not exist OR is not a directory): " + makefile.getPath());
             return;
         }
         
         // test if makefile exists
         if (makefile.exists()==false){
             log.error("Makefile does not exists: " + makefile.getPath());
             System.err.println("Makefile does not exists: " + makefile.getPath());
             return;
         }
         
         // Job queue
         System.out.println("Following nodes will be reprogrammed: ");
         Queue<NodeConfigRecord> ncrJobQueue = prepareJobQueue(nodes2connect, true);
         
         // failed flash
         Queue<NodeConfigRecord> nodesFailed = new ConcurrentLinkedQueue<NodeConfigRecord>();
         
         // Output string from worker
         Queue<String> jobOutput = new ConcurrentLinkedQueue<String>();
         
         try {
             // clean code build
             this.makeClean(makefileDirF);
             // need to build main code at first
             boolean result = this.buildCode(makefileDirF);
             if (result==false){
                 log.error("Cannot build main code - exiting");
                 return;
             }
             
             System.out.println("Main code built");
             
         } catch (IOException ex) {
             log.error("IOEXc - cannot build main code", ex);
         } catch (InterruptedException ex) {
             log.error("Interrupted - cannot build main code", ex);
         }
         
         // spawn jobs
         ExecutorService tasks = Executors.newFixedThreadPool(threadCount);
         for(int i=0; i<threadCount; i++){
             NodeReprogrammer nodeR = new NodeReprogrammer();
             nodeR.makefile = makefile;
             nodeR.makefileDirF = makefileDirF;
             nodeR.nodesFailed = nodesFailed;
             nodeR.jobOutput = jobOutput;
             nodeR.ncrJobQueue = ncrJobQueue;
             
             tasks.execute(nodeR);
         }
         
         // termination wait
         try {
             tasks.shutdown();
 //            tasks.awaitTermination(70, TimeUnit.SECONDS);
             long startTime = System.currentTimeMillis();
             while(true){
                 long curTime = System.currentTimeMillis();
                 if ((curTime-startTime) > 1000*60*60){
                     break;
                 }
                 
                 Thread.sleep(200);
                 
                 // check output queue
                 while(jobOutput.isEmpty()==false){
                     String line = jobOutput.poll();
                     if (line==null) break;
                     
                     System.out.println(line);
                 }
                 
                 // terminated?
                 if (tasks.isTerminated()) break;
             }
             
         } catch (InterruptedException ex) {
             log.error("interrupted ", ex);
         }
         
         // was there some errors?
         if (nodesFailed.isEmpty()==false){
             List<NodeConfigRecord> failedList = new ArrayList(nodesFailed);
             Collections.sort(failedList);
             
             System.out.println("Some errors occurred during reflashing, problematic nodes: ");
             for(NodeConfigRecord ncr : failedList){
                 System.out.println(ncr.getHumanOutput());
             }
         } else {
             System.out.println("All nodes flashed successfully!");
         }
     }
     
     /**
      * Prepares list of nodes for processing in a job.
      * @param nodes2connect
      * @param dump
      * @return 
      */
     protected Queue<NodeConfigRecord> prepareJobQueue(List<NodeConfigRecord> nodes2connect, boolean dump){
         Queue<NodeConfigRecord> ncrJobQueue = new ConcurrentLinkedQueue<NodeConfigRecord>();
         
         // sort
         Collections.sort(nodes2connect);
         
         // info at the beggining
         Iterator<NodeConfigRecord> iterator = nodes2connect.iterator();
         while(iterator.hasNext()){
             NodeConfigRecord ncr = iterator.next();
             
             // push to queue
             ncrJobQueue.add(ncr);
             
             if (dump){
                 System.out.println(ncr.getHumanOutput());
             }
         }
         
         if (dump){
             System.out.println();
         }
         
         return ncrJobQueue;
     }
     
     /**
      * Calls "make clean" in a given directory.
      * @param makefileDirF
      * @return
      * @throws IOException
      * @throws InterruptedException 
      */
     private boolean makeClean(File makefileDirF) throws IOException, InterruptedException{
         // execute motelist command
         CmdExecutionResult resExec = execute("make clean", OutputOpt.EXECUTE_STD_COMBINE, makefileDirF);
         System.out.println("Code build output: " + resExec.stdOut);
 
         return resExec.exitValue==0;
     }
     
     /**
      * Builds main Tinyos code for Telosb platform.
      * @return 
      */
     private boolean buildCode(File makefileDirF) throws IOException, InterruptedException{
         CmdExecutionResult resExec = execute("make telosb", OutputOpt.EXECUTE_STD_COMBINE, makefileDirF);
         System.out.println("Code build output: " + resExec.stdOut);
 
         if (resExec.exitValue==0){
             return true;
         } else {
             log.error("build code exit value: " + resExec.exitValue);
             return false;
         }
     }
     
     /**
      * Reprograms nodes 
      */
     private class NodeReprogrammer extends Thread{        
         private File makefile;
         private File makefileDirF;
         
         private Queue<NodeConfigRecord> ncrJobQueue;
         private Queue<NodeConfigRecord> nodesFailed;
         private Queue<String> jobOutput;
         
         public NodeReprogrammer() {
             this.setName("Node reprogrammer");
         }
         
         /**
          * prepares code node id
          * Directly for telosb, no abstraction
          * @param i 
          */
         public boolean prepareCode(int i) throws IOException, InterruptedException{
             CmdExecutionResult resExec = execute("make telosb id," + i, OutputOpt.EXECUTE_STDERR_ONLY, makefileDirF);
             return resExec.exitValue==0;
         }
         
         @Override
         public void run() {
             while(ncrJobQueue.isEmpty()==false){
                 try {
                     NodeConfigRecord ncr = ncrJobQueue.poll();
                     if (ncr==null) return;
                     
                     // prepare code
                     boolean codeOK = this.prepareCode(ncr.getNodeId());
                     if (codeOK==false){
                         log.error("Cannot prepare code for node: " + ncr.getNodeId());
                         this.jobOutput.add("Cannot prepare code for node: " + ncr.getNodeId());
                         continue;
                     }
                     
                     // not very nice, I know - should be done with abstraction...
                     String command = "/usr/bin/tos-bsl --telosb -c "
                             + ncr.getDeviceAlias() + " -r -e -I -p " 
                             + makefileDirF.getAbsolutePath() + "/build/telosb/main.ihex.out-" + ncr.getNodeId();
                     
                     boolean success=false;
                 
                     // try to repeat retryCount times if failed
                     for(int i=0; i<=retryCount; i++){
                         log.info("Reprogramming nodeID: " + ncr.getNodeId() + "; On device: " + ncr.getDeviceAlias() + "; Try: " + (i+1));
                         log.info("Going to execute: " + command);
                         jobOutput.add("\t\tGoing to execute: " + command);
                         try {
                             // execute motelist command
                             CmdExecutionResult resExec = execute(command, OutputOpt.EXECUTE_STDERR_ONLY, makefileDirF);
 
                             if (resExec.exitValue == 0) {
                                 log.info("Node " + ncr.getNodeId() + " flashed successfully");
                                 jobOutput.add("Node " + ncr.getNodeId() + " flashed successfully");
                                 success=true;
                                 break;
                             } else {
                                 log.info("Output: " + resExec.stdErr);
                                 jobOutput.add("\tNode " + ncr.getNodeId() + " flash error!");
                             }
                         } catch (IOException ex) {
                             log.error("IOException error, try checking motelist command", ex);
                         } catch (InterruptedException ex) {
                             log.error("Motelist command was probably interrupted", ex);
                         }
                     } // end of for (retry count)
 
                     if (success==false){
                         nodesFailed.add(ncr);
                     }
                 } catch (IOException ex) {
                     log.error("Exception in node reprogramming routine", ex);
                 } catch (InterruptedException ex) {
                     log.error("Exception in node reprogramming routine", ex);
                 }
             }
         }
     }
     
     /**
      * Reset nodes from pool.
      */
     private class NodeResetWorker extends Thread{                
         private Queue<NodeConfigRecord> ncrJobQueue;
         private Queue<NodeConfigRecord> nodesFailed;
         private Queue<String> jobOutput;
         
         public NodeResetWorker() {
             this.setName("Node reset worker");
         }
         
         @Override
         public void run() {
             while(ncrJobQueue.isEmpty()==false){
                 try {
                     NodeConfigRecord ncr = ncrJobQueue.poll();
                     if (ncr==null) continue;
                     
                     boolean success = resetNode(ncr);
                     if (success) {
                         log.info("Node " + ncr.getNodeId() + " reseted successfully");
                         jobOutput.add("Node " + ncr.getNodeId() + " reseted successfully");
                     } else {
                         jobOutput.add("\tNode " + ncr.getNodeId() + " reset error!");
                         nodesFailed.add(ncr);
                     }
                 } catch (Exception ex) {
                     log.error("Exception in node reset routine", ex);
                 }
             }
         }
     }
     
     public boolean isAbleNodeReset() {
         return true;
     }
     
     /**
      * Restarts node defined by NodeConfigRecord.
      * @param ncr
      * @return 
      */
     public boolean resetNode(NodeConfigRecord ncr) {
         if (ncr==null){
             return false;
         }
         
         // get platform
         NodePlatform platform = NodePlatformFactory.getPlatform(ncr);
         if (platform == null) return false;
         
         // try to reset with HW command
         String hwResetCommand = platform.hwResetCommand(ncr.getDeviceAlias(), null);
         return this.resetNode(hwResetCommand);
     }
     
     /**
      * Restarts node with given command, successful restart returns 0 as return value.
      * @param resetCommand
      * @return 
      */
     protected boolean resetNode(String resetCommand){
          return resetNode(resetCommand, retryCount);
     }
     
     /**
      * Restarts node with given command, successful restart returns 0 as return value.
      * @param resetCommand 
      * @param retryCount Number of retries for node reset.
      * @return  true     if reset was successful.
      */
     protected boolean resetNode(String resetCommand, int retryCount){
         boolean success=false;
         // try to repeat retryCount times if failed
         for(int i=0; i<retryCount; i++){
             log.info("Going to execute: " + resetCommand);
             try {
                 CmdExecutionResult resExec = execute(resetCommand, OutputOpt.EXECUTE_STDERR_ONLY);
 
                 if (resExec.exitValue == 0) {
                     log.info("Node restarted successfully");
                     success=true;
                     break;
                 } else {
                     log.error("Node restart error! Output: " + resExec.stdErr);
                 }
             } catch (Exception ex) {
                 log.error("IOException error, try checking motelist command", ex);
             }
         } // end of for (retry count)
        
         return success;
     }
     
     /**
      * Simple helper for executing a command.
      * 
      * @param command
      * @param outOpt
      * @return
      * @throws IOException
      * @throws InterruptedException 
      */
     public CmdExecutionResult execute(final String command, OutputOpt outOpt) throws IOException, InterruptedException{
         return execute(command, outOpt, null);
     }
     
     /**
      * Enum defining possible ways of handling process output streams.
      */
     public static enum OutputOpt {
         EXECUTE_STDOUT_ONLY,
         EXECUTE_STDERR_ONLY,
         EXECUTE_STD_COMBINE,
         EXECUTE_STD_SEPARATE
     }
     
     /**
      * Simple helper for executing a command.
      * 
      * @param command
      * @param outOpt
      * @param workingDir
      * @return
      * @throws IOException
      * @throws InterruptedException 
      */
     public CmdExecutionResult execute(final String command, OutputOpt outOpt, File workingDir) throws IOException, InterruptedException{
         CmdExecutionResult res = new CmdExecutionResult();
         
         // Execute motelist command
         Process p = workingDir == null ? 
                 Runtime.getRuntime().exec(command) :
                 Runtime.getRuntime().exec(command, null, workingDir);
 
         // If interested only in stdErr, single thread is OK, otherwise 2 stream
         // reading threads are needed.
         if (outOpt==OutputOpt.EXECUTE_STDERR_ONLY || outOpt==OutputOpt.EXECUTE_STDOUT_ONLY){
             StringBuilder sb = new StringBuilder();
             BufferedReader bri = new BufferedReader(new InputStreamReader(
                             outOpt==OutputOpt.EXECUTE_STDERR_ONLY ? p.getErrorStream() : p.getInputStream()));
             
             String line;
             while ((line = bri.readLine()) != null) {
                 sb.append(line).append("\n");
             }
             bri.close();
             
             if (outOpt==OutputOpt.EXECUTE_STDOUT_ONLY)
                 res.stdOut = sb.toString();
             else if (outOpt==OutputOpt.EXECUTE_STDERR_ONLY)
                 res.stdErr = sb.toString();
             
             // synchronous call, wait for command completion
             p.waitFor();
         } else if (outOpt==OutputOpt.EXECUTE_STD_COMBINE){
             // Combine both streams together
             StreamMerger sm = new StreamMerger(p.getInputStream(), p.getErrorStream());
             sm.run();
             
             // synchronous call, wait for command completion
             p.waitFor();
             
             res.stdOut = sm.getOutput();
         } else {
             // Consume streams, older jvm's had a memory leak if streams were not read,
             // some other jvm+OS combinations may block unless streams are consumed.
             StreamGobbler errorGobbler  = new StreamGobbler(p.getErrorStream(), true);
             StreamGobbler outputGobbler = new StreamGobbler(p.getInputStream(), true);
             errorGobbler.start();
             outputGobbler.start();
             
             // synchronous call, wait for command completion
             p.waitFor();
             
             res.stdErr = errorGobbler.getOutput();
             res.stdOut = outputGobbler.getOutput();
         }
         
         res.exitValue = p.exitValue();
         return res;
     }
     
     /**
      * Private helper class - holds info from parsing udev file
      */
     protected static class NodeConfigRecordLocal{
         private String device;
         private Integer nodeid;
 
         public String getDevice() {
             return device;
         }
 
         public void setDevice(String device) {
             this.device = device;
         }
 
         public Integer getNodeid() {
             return nodeid;
         }
 
         public void setNodeid(Integer nodeid) {
             this.nodeid = nodeid;
         }
     }
     
     /**
      * Wrapper class for job execution result.
      */
     protected static class CmdExecutionResult {
         public int exitValue;
         public String stdErr;
         public String stdOut;
         public long time;
     }
 
     public int getThreadCount() {
         return threadCount;
     }
 
     public void setThreadCount(int threadCount) {
         this.threadCount = threadCount;
     }
 
     public int getRetryCount() {
         return retryCount;
     }
 
     public void setRetryCount(int retryCount) {
         this.retryCount = retryCount;
     }
     
     public Map<String, NodeConfigRecord> getMoteList() {
         return moteList;
     }
 
     public void setMoteList(Map<String, NodeConfigRecord> moteList) {
         this.moteList = (NodeSearchMap) moteList;
     }
 
     public void setMoteList(NodeSearchMap moteList) {
         this.moteList = moteList;
     }
 
     public boolean isCheckConfigNodesToConnected() {
         return checkConfigNodesToConnected;
     }
 
     public void setCheckConfigNodesToConnected(boolean checkConfigNodesToConnected) {
         this.checkConfigNodesToConnected = checkConfigNodesToConnected;
     }
 }
