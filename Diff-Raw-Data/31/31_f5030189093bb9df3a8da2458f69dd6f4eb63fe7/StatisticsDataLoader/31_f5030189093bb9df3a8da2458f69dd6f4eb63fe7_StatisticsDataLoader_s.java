 /* AWE - Amanzi Wireless Explorer
  * http://awe.amanzi.org
  * (C) 2008-2009, AmanziTel AB
  *
  * This library is provided under the terms of the Eclipse Public License
  * as described at http://www.eclipse.org/legal/epl-v10.html. Any use,
  * reproduction or distribution of the library constitutes recipient's
  * acceptance of this agreement.
  *
  * This library is distributed WITHOUT ANY WARRANTY; without even the
  * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  */
 
 package org.amanzi.awe.views.calls.upload;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 import java.util.HashMap;
 import java.util.List;
 
 import net.refractions.udig.catalog.CatalogPlugin;
 import net.refractions.udig.catalog.ICatalog;
 import net.refractions.udig.catalog.IService;
 
 import org.amanzi.awe.statistic.CallTimePeriods;
 import org.amanzi.awe.views.calls.enums.IStatisticsHeader;
 import org.amanzi.awe.views.calls.enums.StatisticsCallType;
 import org.amanzi.awe.views.calls.enums.StatisticsHeaders;
 import org.amanzi.awe.views.calls.enums.StatisticsType;
 import org.amanzi.neo.core.INeoConstants;
 import org.amanzi.neo.core.NeoCorePlugin;
 import org.amanzi.neo.core.database.services.events.UpdateDatabaseEvent;
 import org.amanzi.neo.core.database.services.events.UpdateViewEventType;
 import org.amanzi.neo.core.enums.CallProperties;
 import org.amanzi.neo.core.enums.DriveTypes;
 import org.amanzi.neo.core.enums.GeoNeoRelationshipTypes;
 import org.amanzi.neo.core.enums.GisTypes;
 import org.amanzi.neo.core.enums.NetworkTypes;
 import org.amanzi.neo.core.enums.NodeTypes;
 import org.amanzi.neo.core.enums.ProbeCallRelationshipType;
 import org.amanzi.neo.core.service.NeoServiceProvider;
 import org.amanzi.neo.core.utils.NeoUtils;
 import org.apache.log4j.Logger;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.SubMonitor;
 import org.neo4j.graphdb.Direction;
 import org.neo4j.graphdb.GraphDatabaseService;
 import org.neo4j.graphdb.Node;
 import org.neo4j.graphdb.Relationship;
 import org.neo4j.graphdb.Transaction;
 import org.neo4j.graphdb.Traverser;
 
 /**
  * <p>
  * Load statistics data from csv files.
  * </p>
  * @author Shcharbatsevich_A
  * @since 1.0.0
  */
 public class StatisticsDataLoader {
     
     private static final long HOUR = 1000 * 60 * 60;
     private static final long DAY = 24 * HOUR;
     
     private static final int START_COLUMN = 0;
     private static final int END_COLUMN = 1;
     private static final int PROBE_COLUMN = 2;
     private static final int LA_COLUMN = 3;
     private static final int FREQUENCY_COLUMN = 4;
     private static final int MIN_COLUMN_COUNT = 6;
 
     private static final Logger LOGGER = Logger.getLogger(StatisticsDataLoader.class);
     
     private static final String CSV_FILE_EXTENSION = ".csv";
     private static final String COLUMN_SEPARATOR = ",";
     private static final String TIME_SEPARATOR = "_";
     private static final String DAY_PATTERN = "yyyyMMdd";
     private static final String TIME_PATTERN = "HHmm";
     
     private String directoryName;
     private String networkName;
     private String datasetName;
     
     private Transaction transaction;
     
     private Node network;
     private Node virtualDataset;
     private HashMap<String, Node> gisNodes = new HashMap<String, Node>();
     private HashMap<String, Node> probes = new HashMap<String, Node>();
     private HeaderMap headers;
     
     private HashMap<StatisticsCallType, StatInfo> statRoots = new HashMap<StatisticsCallType, StatInfo>();
     private HashMap<StatisticsCallType, Node> previousCells = new HashMap<StatisticsCallType, Node>();
     private HashMap<StatisticsCallType, Node> previousRows = new HashMap<StatisticsCallType, Node>();
     private Long minTime;
     private Long maxTime;
     
     private boolean isTest;
     
     private GraphDatabaseService service;
     
     /**
      * Constructor.
      * @param directory String (name of file directory)
      * @param dataset String (virtual dataset name)
      * @param network String (network name)
      * @param neo GraphDatabaseService
      */
     public StatisticsDataLoader(String directory, String dataset, String network, GraphDatabaseService neo){
         directoryName = directory;
         networkName = network;
         datasetName = dataset;
         service = neo;
         if(service == null){
             service = NeoServiceProvider.getProvider().getService();
         }
         initHeaders();
     }
     
     /**
      * Constructor.
      * @param directory String (name of file directory)
      * @param dataset String (virtual dataset name)
      * @param network String (network name)
      * @param neo GraphDatabaseService
      */
     public StatisticsDataLoader(String directory, String dataset, String network, GraphDatabaseService neo, boolean testing){
         directoryName = directory;
         networkName = network;
         datasetName = dataset;
         service = neo;
         if(service == null){
             service = NeoServiceProvider.getProvider().getService();
         }
         isTest = testing;
         initHeaders();
     }
     
     /**
      * @return Returns the virtualDataset.
      */
     public Node getVirtualDataset() {
         return virtualDataset;
     }
     
     /**
      * Run to load data.
      *
      * @param monitor IProgressMonitor
      * @throws IOException (problem in load data)
      */
     public void run(IProgressMonitor monitor) throws IOException {
         if(monitor == null){
             monitor = new NullProgressMonitor();
         }
         monitor.beginTask("Loading AMS statistics data", 2);
         monitor.subTask("Searching for files to load");
         ArrayList<File> allFiles = getAllFiles(directoryName);
         int filesCount = allFiles.size();
         IProgressMonitor subMonitor = SubMonitor.convert(monitor, filesCount);
         subMonitor.beginTask("Loading AMS statistics data", filesCount);
         transaction = service.beginTx();
         try{
             network = findOrCreateNetworkNode(); 
             virtualDataset = findVirtualDataset();
             for (File file : allFiles) {
                 subMonitor.subTask("Loading file " + file.getAbsolutePath());
                 loadFile(file);
                 subMonitor.worked(1);                   
             }
             buildHighperiodStatistics(monitor);
             finish();
         }catch(Throwable e){
             LOGGER.error("Problem in loader.",e);
             throw (RuntimeException)new RuntimeException().initCause(e);
         }
         finally{
             commit(false);
         }
     }
     
     /**
      * Commit changes.
      *
      * @param restart boolean (need to restart transaction)
      */
     private void commit(boolean restart) {
         if (transaction != null) {
             transaction.success();
             transaction.finish();
             if (restart) {
                 transaction = service.beginTx();
             } else {
                 transaction = null;
             }
         }
     }
     
     /**
      * Finish all.
      */
     private void finish(){ 
         if (minTime!=null&&maxTime!=null) {
             network.setProperty(INeoConstants.MIN_TIMESTAMP, minTime);
             network.setProperty(INeoConstants.MAX_TIMESTAMP, maxTime);
             virtualDataset.setProperty(INeoConstants.MIN_TIMESTAMP, minTime);
             virtualDataset.setProperty(INeoConstants.MAX_TIMESTAMP, maxTime);
         }
         if (!isTest) {
             NeoCorePlugin.getDefault().getUpdateViewManager().fireUpdateView(new UpdateDatabaseEvent(UpdateViewEventType.GIS));
         }
         try {
             addDataToCatalog();
         } catch (MalformedURLException e) {
             throw (RuntimeException)new RuntimeException().initCause(e);
         }
     }
     
     /**
      * Add loaded data to catalog.
      *
      * @throws MalformedURLException
      */
     private void addDataToCatalog() throws MalformedURLException {
         NeoServiceProvider neoProvider = NeoServiceProvider.getProvider();
         if (neoProvider != null) {
             String databaseLocation = neoProvider.getDefaultDatabaseLocation();
             ICatalog catalog = CatalogPlugin.getDefault().getLocalCatalog();
             URL url = new URL("file://" + databaseLocation);
             List<IService> services = CatalogPlugin.getDefault().getServiceFactory().createService(url);
             for (IService service : services) {
                 if (catalog.getById(IService.class, service.getIdentifier(), new NullProgressMonitor()) != null) {
                     catalog.replace(service.getIdentifier(), service);
                 } else {
                     catalog.add(service);
                 }
             }
             neoProvider.commit();
         }
     }
     
     /**
      * Initialize headers.
      */
     private void initHeaders(){
         headers = new HeaderMap();
         headers.addDataHeader(new Header(START_COLUMN, "STARTTIME", ValueType.TIME, null));
         headers.addDataHeader(new Header(END_COLUMN, "ENDTIME", ValueType.TIME, null));
         headers.addDataHeader(new Header(PROBE_COLUMN, "HOST", ValueType.STRING, null));
         headers.addDataHeader(new Header(LA_COLUMN, "LA", ValueType.INTEGER, null));
         headers.addDataHeader(new Header(FREQUENCY_COLUMN, "FREQUENCY", ValueType.FLOAT, null));
     }
     
     /**
      * Load data from file.
      *
      * @param file File
      * @throws IOException
      * @throws ParseException 
      */
     private void loadFile(File file)throws IOException, ParseException{
         FileInputStream is = new FileInputStream(file);
         BufferedReader reader = new BufferedReader(new InputStreamReader(is));
         String line;
         boolean hasHeaders = false;
         while ((line = reader.readLine()) != null) {
             if(!hasHeaders){
                 hasHeaders = parseHeaders(line);
             }else{
                 parseLine(line);
             }
         }
         commit(true);
     }
     
     /**
      * Parse headers for file.
      *
      * @param line String
      * @return boolean
      */
     private boolean parseHeaders(String line){
         String[] cells = line.split(COLUMN_SEPARATOR);
         int callCount = cells.length;
         if(callCount<2){
             return false;
         }
         headers.clear();
         for(int i=0; i<callCount;i++){
             headers.addHeader(i, cells[i]);
         }
         return true;
     }
     
     /**
      * Parse line.
      *
      * @param line
      * @throws ParseException 
      */
     private void parseLine(String line) throws IOException, ParseException{
         String[] cells = line.split(COLUMN_SEPARATOR);
         int callCount = cells.length;
         if(callCount<MIN_COLUMN_COUNT){
             return;
         }
         Long start = (Long)headers.getHeader(START_COLUMN).parseValue(cells[START_COLUMN]);
         Long end = (Long)headers.getHeader(END_COLUMN).parseValue(cells[END_COLUMN]);
         String probeName = headers.getHeader(PROBE_COLUMN).parseValue(cells[PROBE_COLUMN]).toString();
         Integer la = (Integer)headers.getHeader(LA_COLUMN).parseValue(cells[LA_COLUMN]);
         Float frequency = (Float)headers.getHeader(FREQUENCY_COLUMN).parseValue(cells[FREQUENCY_COLUMN]);
         if(start==null||end==null||probeName==null||probeName.length()==0||la==null||frequency==null){
             return;
         }
         probeName = NeoUtils.buildProbeName(probeName, la, frequency);
         for(int i=FREQUENCY_COLUMN+1; i<callCount;i++){
             Header header = headers.getHeader(i);
             if(!headers.isUnknownHeader(header)){
                 StatisticsCallType callType = headers.getCallTypeByHeader(header);
                 if(callType!=null){
                     Node row = getRowNode(callType, start, end, probeName, la, frequency);
                     Object value = header.parseValue(cells[i]); //TODO value must be null if calls count is zero.
                     if (value!=null) {
                         IStatisticsHeader real = header.getRealHeader();
                         createSCellNode(row, null, value, real, callType);
                     }
                 }
             }
         }
         previousCells.clear();
     }
     
     /**
      * Create root of statistics
      *
      * @param callType StatisticsCallType
      * @return Node
      */
     private Node createRootStatisticsNode(StatisticsCallType callType) {
         Node result = service.createNode();
         
         result.setProperty(INeoConstants.PROPERTY_TYPE_NAME, NodeTypes.CALL_ANALYSIS_ROOT.getId());
         result.setProperty(INeoConstants.PROPERTY_NAME_NAME, INeoConstants.CALL_ANALYZIS_ROOT);
         result.setProperty(INeoConstants.PROPERTY_VALUE_NAME, NeoUtils.getNodeName(virtualDataset,service));
         result.setProperty(CallProperties.CALL_TYPE.getId(), callType.toString());
         
         virtualDataset.createRelationshipTo(result, ProbeCallRelationshipType.CALL_ANALYSIS);
         
         return result;
     }
     
     /**
      * Find or create row node.
      *
      * @param callType StatisticsCallType
      * @param start Long
      * @param end Long
      * @param probeName String
      * @param la Integer
      * @param frequency Float
      * @return Node
      * @throws IOException
      */
     private Node getRowNode(StatisticsCallType callType, Long start, Long end, String probeName,Integer la, Float frequency)throws IOException{
         ProbeInfo probeInfo = getProbeInfo(callType, probeName);
         Node row = probeInfo.getRow(start);
         if(row == null){
             Node probe = getProbeNode(probeName, la, frequency,callType);
             row = createRowNode(start, end, probe,null, statRoots.get(callType).getPeriodNode(),callType, CallTimePeriods.HOURLY);
             probeInfo.addRow(start, row);
             if(minTime==null||start<minTime){
                 minTime=start;
             }
             if(maxTime==null||end>maxTime){
                 maxTime=end;
             }
         }
         return row;
     }
     
     /**
      * Get probe data.
      *
      * @param callType StatisticsCallType
      * @param probeName String
      * @return ProbeInfo
      */
     private ProbeInfo getProbeInfo(StatisticsCallType callType, String probeName){
         StatInfo statInfo = statRoots.get(callType);
         if(statInfo== null){
             Node root = createRootStatisticsNode(callType);
             statInfo = new StatInfo(root,createPeriodNode(root, CallTimePeriods.HOURLY, null));
             statRoots.put(callType, statInfo);
         }
         ProbeInfo probeInfo = statInfo.getProbeInfo(probeName);
         if(probeInfo==null){
             probeInfo = new ProbeInfo(probeName);
             statInfo.addProbeInfo(probeInfo);
         }
         return probeInfo;
     }
     
     /**
      * Create node for statistics period.
      *
      * @param parent Node (statistics root)
      * @param period CallTimePeriods
      * @param undPeriodNode (underling period node)
      * @return Node
      */
     private Node createPeriodNode(Node parent, CallTimePeriods period, Node undPeriodNode) {
         Node result = service.createNode();
         
         result.setProperty(INeoConstants.PROPERTY_NAME_NAME, period.getId());
         result.setProperty(INeoConstants.PROPERTY_TYPE_NAME, NodeTypes.CALL_ANALYSIS.getId());
         parent.createRelationshipTo(result, GeoNeoRelationshipTypes.CHILD);
         
         if (undPeriodNode!=null) {
             result.createRelationshipTo(undPeriodNode, GeoNeoRelationshipTypes.SOURCE);
         }
         return result;
     }
     
     /**
      * Create new row node.
      *
      * @param start Long
      * @param end Long
      * @param probe Node
      * @param sourceRows List of Nodes
      * @param parent Node
      * @param callType StatisticsCallType
      * @param period CallTimePeriods
      * @return Node
      */
     private Node createRowNode(Long start, Long end, Node probe, List<Node> sourceRows, Node parent, StatisticsCallType callType, CallTimePeriods period){
         Node result = service.createNode();
         String name = NeoUtils.getFormatDateStringForSrow(start, period.addPeriod(start), "HH:mm", period.getId());
         result.setProperty(INeoConstants.PROPERTY_TYPE_NAME, NodeTypes.S_ROW.getId());
         result.setProperty(INeoConstants.PROPERTY_NAME_NAME, name);
         result.setProperty(INeoConstants.PROPERTY_TIME_NAME, start);
         
         result.createRelationshipTo(probe, GeoNeoRelationshipTypes.SOURCE);
         
         Node previous = previousRows.get(callType);
         if (previous == null) {
             parent.createRelationshipTo(result, GeoNeoRelationshipTypes.CHILD);
         }
         else {
             previous.createRelationshipTo(result, GeoNeoRelationshipTypes.NEXT);
         }
         previousRows.put(callType, result);
         
         if (sourceRows != null) {
             for(Node sourceRow : sourceRows){
                 result.createRelationshipTo(sourceRow, GeoNeoRelationshipTypes.SOURCE);
             }
         }
         
         return result;
     }
     
     /**
      * Create cell node
      *
      * @param row Node
      * @param sourceCells List of Nodes
      * @param value Object
      * @param header StatisticsHeaders
      * @param callType StatisticsCallType
      */
     private void createSCellNode(Node row, List<Node> sourceCells, Object value, IStatisticsHeader header, StatisticsCallType callType){
         Node result = service.createNode();
         
         result.setProperty(INeoConstants.PROPERTY_TYPE_NAME, NodeTypes.S_CELL.getId());
         result.setProperty(INeoConstants.PROPERTY_NAME_NAME, header.getTitle());
         if (value != null) {
             result.setProperty(INeoConstants.PROPERTY_VALUE_NAME, value);                       
         }
         Node previousNode = previousCells.get(callType);
         if (previousNode == null) {
             row.createRelationshipTo(result, GeoNeoRelationshipTypes.CHILD);
         }
         else {
             previousNode.createRelationshipTo(result, GeoNeoRelationshipTypes.NEXT);
         }
         previousCells.put(callType, result);
         
         if (sourceCells != null) {
             for(Node sourceCell : sourceCells){
                 result.createRelationshipTo(sourceCell, GeoNeoRelationshipTypes.SOURCE);
             }
         }
         
     }
     
     /**
      * Find or create probe node with probe calls node.
      *
      * @param probeName String
      * @param la Integer
      * @param frequency Float
      * @param callType StatisticsCallType
      * @return Node
      * @throws IOException
      */
     private Node getProbeNode(String probeName, Integer la, Float frequency, StatisticsCallType callType)throws IOException{
         Node probe = probes.get(probeName);
         if(probe==null){            
             probe = NeoUtils.findOrCreateProbeNode(network, probeName, service);
             probe.setProperty(INeoConstants.PROBE_LA, la); 
             probe.setProperty(INeoConstants.PROBE_F, frequency);
 
             probes.put(probeName, probe);
         }
         Node calls = getProbeCalls(probe, probeName);
         calls.setProperty(callType.getId().getProperty(), true);
         if(!calls.hasRelationship(ProbeCallRelationshipType.PROBE_DATASET, Direction.INCOMING)){
             virtualDataset.createRelationshipTo(calls, ProbeCallRelationshipType.PROBE_DATASET);
         }
         return probe;
     }
     
     /**
      * Find or create probe calls
      *
      * @param probe Node
      * @param probeName String
      * @return Node
      * @throws IOException
      */
     private Node getProbeCalls(Node probe, String probeName)throws IOException{
         Relationship link = probe.getSingleRelationship(ProbeCallRelationshipType.CALLS, Direction.OUTGOING);
         String callProbeName = probeName + " - " + datasetName;
         if(link == null){
             Node calls = service.createNode();
             calls.setProperty(INeoConstants.PROPERTY_TYPE_NAME, NodeTypes.CALLS.getId());
             calls.setProperty(INeoConstants.PROPERTY_NAME_NAME, callProbeName);
             calls.setProperty(NodeTypes.DATASET.getId(), datasetName);
 
             probe.createRelationshipTo(calls, ProbeCallRelationshipType.CALLS);
             return calls;
         }
         return link.getEndNode();
     }
     
     /**
      * @return dataset node
      */
     private Node findOrCreateNetworkNode() {        
         if (networkName == null || networkName.isEmpty()) {
             return null;
         }
         else {
             networkName = networkName.trim();
         }
         
         Node gis = findOrCreateGISNode(networkName,null, GisTypes.NETWORK.getHeader(),NetworkTypes.PROBE);              
         Node result = NeoUtils.findOrCreateNetworkNode(gis, networkName, directoryName, service);            
         return result;
     }
     
     /**
      * Gets gis node for dataset or network.
      *
      * @param mainNode Node
      * @param gisType String
      * @return Node
      */
     private Node findOrCreateGISNode(String gisName, Node mainNode, String gisType, NetworkTypes fileType) {
         if (gisName==null||gisName.length()==0) {
             gisName = NeoUtils.getNodeName(mainNode, service);
         }
         Node gis = gisNodes.get(gisName);
 
         if (gis == null) {
             Transaction transaction = service.beginTx();
             try {
                 Node reference = service.getReferenceNode();
                 gis = NeoUtils.findGisNode(gisName, service);
                 if (gis == null) {
                     gis = NeoUtils.createGISNode(reference, gisName, gisType, service);
                     if (mainNode!=null) {
                         boolean hasRelationship = false;
                         for (Relationship relation : gis.getRelationships(GeoNeoRelationshipTypes.NEXT, Direction.OUTGOING)) {
                             if (relation.getEndNode().equals(mainNode)) {
                                 hasRelationship = true;
                             }
                         }
                         if (!hasRelationship) {
                             gis.createRelationshipTo(mainNode, GeoNeoRelationshipTypes.NEXT);
                         }
                     }
                     if(fileType!=null){
                         fileType.setTypeToNode(gis, service);
                     }
                 }
                 gisNodes.put(gisName, gis);
                 transaction.success();
             } finally {
                 transaction.finish();
             }
         }
         return gis;
     }
     
     /**
      * @return empty virtual dataset node (calls).
      */
     private Node findVirtualDataset() {
         Node realDataset = findDatasetNode();
         Node result;
         DriveTypes amsCalls = DriveTypes.AMS_CALLS;
         if(realDataset!=null){
             result = NeoUtils.findOrCreateVirtualDatasetNode(realDataset, amsCalls, service);
             datasetName = NeoUtils.getNodeName(result,service);
         }else{
             datasetName = amsCalls.getFullDatasetName(datasetName);
             result = service.createNode();
             result.setProperty(INeoConstants.PROPERTY_TYPE_NAME, NodeTypes.DATASET.getId());
             result.setProperty(INeoConstants.PROPERTY_NAME_NAME, datasetName);
             result.setProperty(INeoConstants.DRIVE_TYPE, amsCalls.getId());
         }        
         findOrCreateGISNode(datasetName,result, GisTypes.DRIVE.getHeader(),null); 
         return result;
     }
     
     /**
      * Find real dataset node.
      *
      * @return Node
      */
     protected final Node findDatasetNode() {        
         if (datasetName == null || datasetName.isEmpty()) {
             return null;
         }
         Traverser traverse = NeoCorePlugin.getDefault().getProjectService().getAllDatasetTraverser(service.getReferenceNode());
         for (Node node : traverse) {
             if (node.getProperty(INeoConstants.PROPERTY_NAME_NAME, "").equals(datasetName)) {
                 return node;
             }
         }
         return null;
     }
     
     /**
      * Build statistics for higher periods.
      */
     private void buildHighperiodStatistics(IProgressMonitor monitor){
         int statSize = statRoots.size();
         HashMap<StatisticsCallType, Node> roots = new HashMap<StatisticsCallType, Node>(statSize);
         monitor = SubMonitor.convert(monitor, statSize);
         monitor.beginTask("Build statistics for higher periods", statSize);
         for(StatisticsCallType type : statRoots.keySet()){
             CallTimePeriods highestPeriod = getHighestPeriod(minTime,maxTime);            
             if (highestPeriod!=null) {
                 StatInfo newInfo = buildStatisticsFromUnderling(highestPeriod, type);
                 statRoots.put(type, newInfo);
                 roots.put(type, newInfo.getRoot());
             }
             monitor.worked(1);
         }
     }
     
     /**
      * Build statistics by underling.
      *
      * @param period CallTimePeriods
      * @param callType StatisticsCallType
      * @return StatInfo
      */
     private StatInfo buildStatisticsFromUnderling(CallTimePeriods period, StatisticsCallType callType){
         if(period.equals(CallTimePeriods.HOURLY)){
             return statRoots.get(callType);
         }
         StatInfo sourceStat = buildStatisticsFromUnderling(period.getUnderlyingPeriod(),callType);
         Node statRoot = sourceStat.getRoot();
         Node periodNode = createPeriodNode(statRoot, period, sourceStat.getPeriodNode());
         StatInfo result = new StatInfo(statRoot, periodNode);
         Long start = period.getFirstTime(minTime);
         Long end = maxTime;
         Long nextStart = getNextStartTime(start, end, period);
         previousRows.clear();
         do{
             HashMap<String, ProbeInfo> allProbes = sourceStat.getProbes();
             int commCount = 0;
             for(String probeName : allProbes.keySet()){
                 Node probe = probes.get(probeName);
                 List<Node> sourceRows = allProbes.get(probeName).getRowsInTime(period.getUnderlyingPeriod().getFirstTime(start), nextStart);
                 Node row = createRowNode(start, end, probe, sourceRows, periodNode, callType, period);
                 HashMap<IStatisticsHeader, List<Node>> cellsMap = getCellsMap(sourceRows, callType);
                 previousCells.clear();
                 for(IStatisticsHeader header : cellsMap.keySet()){
                     List<Node> sourceCells = cellsMap.get(header);
                     Object cellValue = getCellValue(header, sourceCells);
                     createSCellNode(row, sourceCells, cellValue, header, callType);
                 }
                 ProbeInfo info = result.getProbeInfo(probeName);
                 if(info==null){
                     info = new ProbeInfo(probeName);
                     result.addProbeInfo(info);
                 }
                 info.addRow(start, row);                
                 commCount++;
                 if (commCount>10) {
                     commit(true);
                 }
             }
             start = nextStart;            
             nextStart = getNextStartTime(start, end, period);            
         }while(start<end);    
         
         return result;
     }
     
     /**
      * Get cells from rows by headers.
      *
      * @param rows List of Nodes.
      * @param callType StatisticsCallType
      * @return HashMap<StatisticsHeaders, List<Node>>
      */
     private HashMap<IStatisticsHeader, List<Node>> getCellsMap(List<Node> rows, StatisticsCallType callType){
         HashMap<IStatisticsHeader, List<Node>> result = new HashMap<IStatisticsHeader, List<Node>>();
         for(Node row : rows){
             for(Node cell : NeoUtils.getChildTraverser(row)){
                 IStatisticsHeader header = callType.getHeaderByTitle(NeoUtils.getNodeName(cell,service));
                 List<Node> cells = result.get(header);
                 if(cells==null){
                     cells = new ArrayList<Node>();
                     result.put(header, cells);
                 }
                 cells.add(cell);
             }
         }
         return result;
     }
     
     /**
      * Get value for cell from sources.
      *
      * @param header StatisticsHeaders
      * @param sourceCells List of Nodes
      * @return Object
      */
     private Object getCellValue(IStatisticsHeader header, List<Node> sourceCells){
         Object value = null;
         StatisticsType type = header.getType();
         for(Node cell : sourceCells){
             Object source = cell.getProperty(INeoConstants.PROPERTY_VALUE_NAME, null);
             if(source==null){
                 continue;
             }
             if(value==null){
                 value = source;
                 continue;
             }
             switch (type) {
             case COUNT:
                 value = ((Integer)value)+((Integer)source);
                 break;
             case SUM:
                 value = ((Float)value)+((Float)source);
                 break;
             case MIN:
                 if(((Float)value)>((Float)source)){
                     value = source;
                 }
                 break;
             case MAX:
                 if(((Float)value)<((Float)source)){
                     value = source;
                 }
                 break;
             default:
                 //do nothing
             }
         }
         return value;
     }
     
     /**
      * Get last period border.
      *
      * @param start Long
      * @param end Long
      * @param period CallTimePeriods
      * @return Long
      */
     private Long getNextStartTime(Long start, Long end, CallTimePeriods period){
         Long nextStart = period.addPeriod(start);
         if(nextStart>end){
             nextStart = end;
         }
         return nextStart;
     }
     
     /**
      * Get highest period.
      *
      * @param start Long
      * @param end Long
      * @return CallTimePeriods
      */
     private CallTimePeriods getHighestPeriod(Long start, Long end){
         long delta = CallTimePeriods.DAILY.getFirstTime(maxTime) - CallTimePeriods.DAILY.getFirstTime(minTime);
         if (delta >= DAY) {
             return CallTimePeriods.MONTHLY;
         }
         delta = CallTimePeriods.HOURLY.getFirstTime(maxTime) - CallTimePeriods.HOURLY.getFirstTime(minTime);
         if (delta >= HOUR) {
             return CallTimePeriods.DAILY;
         }
         
         return CallTimePeriods.HOURLY;
     }
     
     /**
      * Get all files from directory
      *
      * @param directoryName String
      * @return List of File.
      */
     private ArrayList<File> getAllFiles(String directoryName) {
         File directory = new File(directoryName);
         ArrayList<File> result = new ArrayList<File>();        
         for (File childFile : directory.listFiles()) {
             if (childFile.isDirectory()) {
                 result.addAll(getAllFiles(childFile.getPath()));
             }
             else if (childFile.isFile() && childFile.getName().endsWith(CSV_FILE_EXTENSION)) {
                 result.add(childFile);
             }
         }
         return result;
         
     }
     
     /**
      * <p>
      * Types of headers.
      * </p>
      * @author Shcharbatsevich_A
      * @since 1.0.0
      */
     private enum HeaderTypes{
         
         INDIVIDUAL(StatisticsCallType.INDIVIDUAL, "SL-SRV-SC"),
         GROUP(StatisticsCallType.GROUP, "SL-SRV-GC"),
         ITSI_ATTACH(StatisticsCallType.ITSI_ATTACH,"SL-INH-ATT"),
        ITSI_CC(StatisticsCallType.ITSI_CC,"SL-INH-CC_"),
         TSM(StatisticsCallType.TSM,"SL-SRV-TSM"),
         SDS(StatisticsCallType.SDS,"SL-SRV-SDS"),
         EMERGENCY(StatisticsCallType.EMERGENCY,"SL-SRV-EC-1"),
         HELP(StatisticsCallType.HELP,"SL-SRV-EC-2"),
         ALARM(StatisticsCallType.ALARM,"SL-SRV-ALM"),
         CS_DATA(StatisticsCallType.CS_DATA,"SL-SRV-CSD"),
         PS_DATA(StatisticsCallType.PS_DATA,"SL-SRV-IP");        
         
         private StatisticsCallType realType;
         private String prefix;
         
         private HeaderTypes(StatisticsCallType type, String headerPrefix) {
             realType = type;
             prefix = headerPrefix;
         }
         
         /**
          * @return Returns the realType.
          */
         public StatisticsCallType getRealType() {
             return realType;
         }
         
         public static HeaderTypes getTypeByHeader(String header){
             for(HeaderTypes type : values()){
                 if(header.startsWith(type.prefix)){
                     return type;
                 }
             }
             return null;
         }
     }
     
     /**
      * <p>
      * Types of values.
      * </p>
      * @author Shcharbatsevich_A
      * @since 1.0.0
      */
     private enum ValueType{
         TIME,
         STRING,
         INTEGER,
         FLOAT
     }
     
     /**
      * <p>
      * Column header.
      * </p>
      * @author Shcharbatsevich_A
      * @since 1.0.0
      */
     private class Header {
         
         private int number;
         private ValueType parsedType;
         
         private IStatisticsHeader realHeader;
         
         public Header(int aNumber, String aName, ValueType aType, IStatisticsHeader aReal) {
             number = aNumber;
             realHeader = aReal;
             parsedType = aType;
         }
         
         /**
          * @return Returns the number.
          */
         public int getNumber() {
             return number;
         }
         
         /**
          * @return Returns the realHeader.
          */
         public IStatisticsHeader getRealHeader() {
             return realHeader;
         }
         
         public Object parseValue(String value) throws ParseException{
             switch (parsedType) {
             case TIME:
                 return getTimeFromString(value);
             case STRING:
                 return value;
             case INTEGER:
                 if(value==null||value.length()==0){
                     return null;
                 }
                 return Integer.valueOf(value);
             case FLOAT:
                 if(value==null||value.length()==0){
                     return null;
                 }
                 return Float.valueOf(value);
             default:
                 return null;
             }
         }
         
         /**
          * Parse string to time.
          *
          * @param value String.
          * @return Long
          * @throws ParseException
          */
         private Long getTimeFromString(String value) throws ParseException{
             if(value==null||value.length()==0){
                 return null;
             }
             String[] parts = value.split(TIME_SEPARATOR);
             if(parts.length<2){
                 return null;
             }
             SimpleDateFormat sf = new SimpleDateFormat(DAY_PATTERN);
             Calendar calendar = new GregorianCalendar();
             calendar.setTime(sf.parse(parts[0]));
             sf = new SimpleDateFormat(TIME_PATTERN);
             Calendar timeCalendar = new GregorianCalendar();
             timeCalendar.setTime(sf.parse(parts[1]));
             calendar.set(Calendar.HOUR_OF_DAY, timeCalendar.get(Calendar.HOUR_OF_DAY));
             calendar.set(Calendar.MINUTE, timeCalendar.get(Calendar.MINUTE));
             return calendar.getTimeInMillis();
         }
     }
     
     /**
      * <p>
      * Map of all headers.
      * </p>
      * @author Shcharbatsevich_A
      * @since 1.0.0
      */
     private class HeaderMap {
         
         private HashMap<Integer, Header> allHeaders = new HashMap<Integer, Header>();
         
         private HashMap<Header, StatisticsCallType> statisticsHeaders = new HashMap<Header, StatisticsCallType>();
         private List<Header> unknownHeaders = new ArrayList<Header>();
         private List<Header> dataHeaders = new ArrayList<Header>();
         
         /**
          * Add header to map.
          *
          * @param number int
          * @param name String
          * @return
          */
         public Header addHeader(int number, String name){
             Header header = getHeader(number);
             if(header!=null){
                 return header;
             }
             HeaderTypes headerType = HeaderTypes.getTypeByHeader(name);
             if(headerType!=null){
                 StatisticsCallType statType = headerType.getRealType();
                 IStatisticsHeader statHeader = getStatisticsHeader(statType, name);
                 if (statHeader!=null) {
                     ValueType type = statHeader.getType().equals(StatisticsType.COUNT) ? ValueType.INTEGER : ValueType.FLOAT;
                     header = new Header(number, name, type, statHeader);
                     statisticsHeaders.put(header, statType);
                 }
             }
             if(header==null){
                 header = new Header(number, name, ValueType.STRING, null);
                 unknownHeaders.add(header);
             }
             allHeaders.put(number, header);
             return header;
         }
         
         /**
          * Get header.
          *
          * @param number int
          * @return Header
          */
         public Header getHeader(int number){
             return allHeaders.get(number);
         }
         
         /**
          * Is header unknown.
          *
          * @param header
          * @return boolean
          */
         public boolean isUnknownHeader(Header header){
             return unknownHeaders.contains(header);
         }
         
         /**
          * Returns call type for header.
          *
          * @param header
          * @return StatisticsCallType
          */
         public StatisticsCallType getCallTypeByHeader(Header header){
             return statisticsHeaders.get(header);
         }
         
         /**
          * Find real statistics header.
          *
          * @param callType StatisticsCallType
          * @param name String
          * @return StatisticsHeaders
          */
         private IStatisticsHeader getStatisticsHeader(StatisticsCallType callType, String name){
             if(callType==null){
                 return null;
             }
             for(IStatisticsHeader header : callType.getHeaders()){
                 if(name.endsWith(header.getTitle())){
                     return header;
                 }
             }
             if(callType.equals(StatisticsCallType.GROUP)&&name.equals("SL-SRV-GC-1_ATTEMPT")){
                 return StatisticsHeaders.CALL_ATTEMPT_COUNT;
             }
             return null; 
         }
         
         /**
          * Add data header.
          *
          * @param header
          */
         public void addDataHeader(Header header){
             dataHeaders.add(header);
             allHeaders.put(header.getNumber(), header);
         }
         
         /**
          * Clear header map.
          */
         public void clear(){
             statisticsHeaders.clear();
             unknownHeaders.clear();
             allHeaders.clear();
             for(Header header : dataHeaders){
                 allHeaders.put(header.getNumber(), header);
             }
         }
     }
     
     /**
      * <p>
      * Statistics data.
      * </p>
      * @author Shcharbatsevich_A
      * @since 1.0.0
      */
     private class StatInfo{
         
         private Node root;
         private Node periodNode;
         private HashMap<String, ProbeInfo> probes;
         
         /**
          * Constructor.
          * @param rootNode Node (statistics root)
          * @param period Node (period node)
          */
         public StatInfo(Node rootNode, Node period) {
             root = rootNode;
             periodNode = period;
             probes = new HashMap<String, ProbeInfo>();
         }
         
         /**
          * @return Returns the root.
          */
         public Node getRoot() {
             return root;
         }
         
         /**
          * @return Returns the hourly.
          */
         public Node getPeriodNode() {
             return periodNode;
         }
         
         /**
          * Get probe data by probe name.
          *
          * @param probe String
          * @return ProbeInfo
          */
         public ProbeInfo getProbeInfo(String probe){
             return probes.get(probe);
         }
         
         /**
          * Add probe data.
          *
          * @param info ProbeInfo
          */
         public void addProbeInfo(ProbeInfo info){
             probes.put(info.getProbeName(), info);
         }
         
         /**
          * @return Returns the probes.
          */
         public HashMap<String, ProbeInfo> getProbes() {
             return probes;
         }
     }
     
     /**
      * <p>
      * Probe data.
      * </p>
      * @author Shcharbatsevich_A
      * @since 1.0.0
      */
     private class ProbeInfo{        
         private String probe;
         private HashMap<Long, Node> rows;
         
         public ProbeInfo(String probeName) {
             probe = probeName;
             rows = new HashMap<Long,Node>();
         }
         
         /**
          * @return Returns the probe.
          */
         public String getProbeName() {
             return probe;
         }
         
         /**
          * @return Returns the row.
          */
         public Node getRow(Long time) {
             return rows.get(time);
         }
         
         /**
          * Add row.
          *
          * @param time
          * @param row
          */
         public void addRow(Long time, Node row) {
             rows.put(time, row);
         }
         
         /**
          * Get all rows in time borders.
          *
          * @param start Long
          * @param end Long
          * @return List of Nodes.
          */
         public List<Node> getRowsInTime(Long start, Long end){
             List<Node> result = new ArrayList<Node>();
             for(Long time : rows.keySet()){
                 if(start<=time&&time<end){
                     result.add(rows.get(time));
                 }
             }
             return result;
         }
     }
 }
