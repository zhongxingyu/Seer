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
 import java.util.logging.Level;
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
         System.out.println("Dumping output: ");
         
         Iterator<String> iterator = this.moteList.keySet().iterator();
         while(iterator.hasNext()){
             String serial = iterator.next();
             NodeConfigRecord ncr = this.moteList.get(serial);
             System.out.println(ncr.getHumanOutput());
         }
     }
     
     public void resetNodes(List<NodeConfigRecord> nodes2connect){
         // info at the beggining
         System.out.println("Following nodes will be restarted: ");
         Iterator<NodeConfigRecord> iterator = nodes2connect.iterator();
         while(iterator.hasNext()){
             NodeConfigRecord ncr = iterator.next();
             System.out.println(ncr.getHumanOutput());
             
             this.resetNode(ncr);
         }
     }
     
     /**
      * Reprograms specified nodes with makefile.
      * Only path to directory with makefile is required. Then is executed
      * make telosb install,X bsl,/dev/mote_telosX
      * 
      * @extension: add multithreading to save time required for reprogramming
      * 
      * @param makeDir  absolute path to makefile directory with mote program
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
         Queue<NodeConfigRecord> ncrJobQueue = new ConcurrentLinkedQueue<NodeConfigRecord>();
         
         // clear job queue
         ncrJobQueue.clear();
         
         // info at the beggining
         System.out.println("Following nodes will be reprogrammed: ");
         Iterator<NodeConfigRecord> iterator = nodes2connect.iterator();
         while(iterator.hasNext()){
             NodeConfigRecord ncr = iterator.next();
             System.out.println(ncr.getHumanOutput());
             
             // push to queue
             ncrJobQueue.add(ncr);
         }
         System.out.println();
         
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
             System.out.println("Some errors occurred during reflashing, problematic nodes: ");
             iterator = nodesFailed.iterator();
             while(iterator.hasNext()){
                 NodeConfigRecord ncr = iterator.next();
                 System.out.println(ncr.getHumanOutput());
             }
         } else {
             System.out.println("All nodes flashed successfully!");
         }
     }
     
     private boolean makeClean(File makefileDirF) throws IOException, InterruptedException{
         // execute motelist command
         Process p = Runtime.getRuntime().exec("make clean", null, makefileDirF);
         String output;
         
         StringBuilder sb = new StringBuilder();
         BufferedReader bri = new BufferedReader(new InputStreamReader(p.getErrorStream()));
         BufferedReader bre = new BufferedReader(new InputStreamReader(p.getInputStream()));
         String line = null;
         
         long c=0;
         while(c<5000){
             if (bri.ready()){
                 line = bri.readLine();
                 sb.append(line).append("\n");
                 c=0;
             }
 
             if (bre.ready()){
                 line = bre.readLine();
                 sb.append(line).append("\n");
                 c=0;
             }
             
             c+=1;
             Thread.sleep(1);
         }
         bri.close();
         bre.close();
         
         output = sb.toString();
         System.out.println("Code build output: " + output);
 
         // sunchronous call, wait for command completion
         p.waitFor();
         int exitVal = p.exitValue();    
         
         if (exitVal == 0) {
             return true;
         } else {
             return false;
         }
     }
     
     /**
      * Builds main tinyos code for telosb platform
      * @return 
      */
     private boolean buildCode(File makefileDirF) throws IOException, InterruptedException{
         // execute motelist command
         Process p = Runtime.getRuntime().exec("make telosb", null, makefileDirF);
         String output;
         
         StringBuilder sb = new StringBuilder();
         BufferedReader bri = new BufferedReader(new InputStreamReader(p.getErrorStream()));
         BufferedReader bre = new BufferedReader(new InputStreamReader(p.getInputStream()));
         String line = null;
         
         long c=0;
         while(c<5000){
             if (bri.ready()){
                 line = bri.readLine();
                 sb.append(line).append("\n");
                 c=0;
             }
 
             if (bre.ready()){
                 line = bre.readLine();
                 sb.append(line).append("\n");
                 c=0;
             }
             
             c+=1;
             Thread.sleep(1);
         }
         bri.close();
         bre.close();
         
         output = sb.toString();
         System.out.println("Code build output: " + output);
 
         // sunchronous call, wait for command completion
         p.waitFor();
         int exitVal = p.exitValue();    
         
         if (exitVal == 0) {
             return true;
         } else {
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
             // execute motelist command
             Process p = Runtime.getRuntime().exec("make telosb id," + i, null, makefileDirF);
             String output;
 
             StringBuilder sb = new StringBuilder();
             BufferedReader bri = new BufferedReader(new InputStreamReader(p.getErrorStream()));
             String line = null;
             while ((line = bri.readLine()) != null) {
                 sb.append(line).append("\n");
             }
             bri.close();
             output = sb.toString();
 
             // sunchronous call, wait for command completion
             p.waitFor();
             int exitVal = p.exitValue();
 
             if (exitVal == 0) {
                 return true;
             } else {
                 return false;
             }
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
                     
 //                    String command = MAKE + " -f " + makefile.getAbsolutePath() + " "
 //                        + NodePlatformFactory.getPlatform(ncr.getPlatformId()).getPlatformReflashId()
 //                        +" install," + ncr.getNodeId() + " bsl," + ncr.getDeviceAlias();
                     
                     boolean success=false;
                 
                     // try to repeat 3 times if failed
                     for(int i=0; i<4; i++){
                         log.info("Reprogramming nodeID: " + ncr.getNodeId() + "; On device: " + ncr.getDeviceAlias() + "; Try: " + (i+1));
                         log.info("Going to execute: " + command);
                         jobOutput.add("\t\tGoing to execute: " + command);
                         try {
                             // execute motelist command
                             Process p = Runtime.getRuntime().exec(command, null, makefileDirF);
                             String output;
 
                             StringBuilder sb = new StringBuilder();
                             BufferedReader bri = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                             String line = null;
                             while ((line = bri.readLine()) != null) {
                                 sb.append(line).append("\n");
                             }
                             bri.close();
                             output = sb.toString();
 
                             // sunchronous call, wait for command completion
                             p.waitFor();
                             int exitVal = p.exitValue();
 
                             if (exitVal == 0) {
                                 log.info("Node " + ncr.getNodeId() + " flashed successfully");
                                 jobOutput.add("Node " + ncr.getNodeId() + " flashed successfully");
                                 success=true;
                                 break;
                             } else {
                                 log.info("Output: " + output);
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
                     java.util.logging.Logger.getLogger(USBarbitratorImpl.class.getName()).log(Level.SEVERE, null, ex);
                 } catch (InterruptedException ex) {
                     java.util.logging.Logger.getLogger(USBarbitratorImpl.class.getName()).log(Level.SEVERE, null, ex);
                 }
             }
         }
     }
     
     public boolean isAbleNodeReset() {
         return true;
     }
     
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
      * Restarts node with given command, successful restart returns 0 as returnvalue
      * @param resetCommand
      * @return 
      */
     protected boolean resetNode(String resetCommand){
         // info at the beggining
         
         boolean success=false;
         // try to repeat 3 times if failed
         for(int i=0; i<3; i++){
             log.info("Going to execute: " + resetCommand);
             try {
                 // execute motelist command
                 Process p = Runtime.getRuntime().exec(resetCommand);
                 String output;
 
                 StringBuilder sb = new StringBuilder();
                 BufferedReader bri = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                 String line = null;
                 while ((line = bri.readLine()) != null) {
                     sb.append(line).append("\n");
                 }
                 bri.close();
                 output = sb.toString();
 
                 // sunchronous call, wait for command completion
                 p.waitFor();
                 int exitVal = p.exitValue();
 
                 if (exitVal == 0) {
                     log.info("Node restarted successfully");
                     success=true;
                     break;
                 } else {
                     log.error("Node restart error! Output: " + output);
                 }
             } catch (IOException ex) {
                 log.error("IOException error, try checking motelist command", ex);
             } catch (InterruptedException ex) {
                 log.error("Motelist command was probably interrupted", ex);
             }
         } // end of for (retry count)
        
         return success;
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
     
     /**
      * Private helper class - holds info from parsing udev file
      */
     protected class NodeConfigRecordLocal{
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
 
     public int getThreadCount() {
         return threadCount;
     }
 
     public void setThreadCount(int threadCount) {
         this.threadCount = threadCount;
     }
 }
