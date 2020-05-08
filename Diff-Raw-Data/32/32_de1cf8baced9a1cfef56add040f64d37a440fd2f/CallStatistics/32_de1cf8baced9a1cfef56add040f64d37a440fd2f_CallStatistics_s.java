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
 
 package org.amanzi.awe.views.calls.statistics;
 
 import java.io.IOException;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map.Entry;
 
 import org.amanzi.awe.statistic.CallTimePeriods;
 import org.amanzi.awe.views.calls.enums.IStatisticsHeader;
 import org.amanzi.awe.views.calls.enums.StatisticsCallType;
 import org.amanzi.awe.views.calls.statistics.constants.GroupCallConstants;
 import org.amanzi.awe.views.calls.statistics.constants.ICallStatisticsConstants;
 import org.amanzi.awe.views.calls.statistics.constants.IndividualCallConstants;
 import org.amanzi.neo.core.INeoConstants;
 import org.amanzi.neo.core.NeoCorePlugin;
 import org.amanzi.neo.core.database.services.events.UpdateDatabaseEvent;
 import org.amanzi.neo.core.database.services.events.UpdateViewEventType;
 import org.amanzi.neo.core.enums.CallProperties;
 import org.amanzi.neo.core.enums.GeoNeoRelationshipTypes;
 import org.amanzi.neo.core.enums.NodeTypes;
 import org.amanzi.neo.core.enums.ProbeCallRelationshipType;
 import org.amanzi.neo.core.utils.NeoUtils;
 import org.amanzi.neo.core.utils.Pair;
 import org.amanzi.neo.index.MultiPropertyIndex;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.SubMonitor;
 import org.neo4j.graphdb.Direction;
 import org.neo4j.graphdb.GraphDatabaseService;
 import org.neo4j.graphdb.Node;
 import org.neo4j.graphdb.Relationship;
 import org.neo4j.graphdb.ReturnableEvaluator;
 import org.neo4j.graphdb.StopEvaluator;
 import org.neo4j.graphdb.Transaction;
 import org.neo4j.graphdb.TraversalPosition;
 import org.neo4j.graphdb.Traverser;
 import org.neo4j.graphdb.Traverser.Order;
 
 /**
  * Class for creating Call Statistics
  * 
  * @author Lagutko_N
  * @since 1.0.0
  */
 public class CallStatistics {
     
     /*
      * a Hour period
      */
     private static final long HOUR = 1000 * 60 * 60;
     
     /*
      * a Day period
      */
     private static final long DAY = 24 * HOUR;    
     
     /*
      * Name of AMS dataset
      */
     private String amsDatasetName;
     
     /*
      * Neo Service 
      */
     private GraphDatabaseService neoService;
     
     /*
      * Map of previous s_cell nodes for each period
      */
     private HashMap<CallTimePeriods, Node> previousSCellNodes = new HashMap<CallTimePeriods, Node>();
     
     /*
      * Map of previous s_row nodes for each period
      */
     private HashMap<CallTimePeriods, Node> previousSRowNodes = new HashMap<CallTimePeriods, Node>();
 
     /*
      * Dataset Node for calculating statistics
      */
     private Node datasetNode;
 
     /*
      * Root statistics Node
      */
     private HashMap<StatisticsCallType, Node> statisticNode = new HashMap<StatisticsCallType, Node>();
 
     /*
      * Highest period 
      */
     private CallTimePeriods highPeriod;
     
     private HashMap<StatisticsCallType, ICallStatisticsConstants> statisticsConstants = new HashMap<StatisticsCallType, ICallStatisticsConstants>();
 
     private Transaction transaction;
     private IProgressMonitor monitor;
     
     private boolean isTest = false;
     
     /**
      * Empty for child.
      */
     protected CallStatistics() {
     }
     
    /**
      * Creates Calculator of Call Statistics
      * 
      * @param drive Dataset Node
      * @throws IOException if was problem in initializing of indexes
      */
     public CallStatistics(Node drive, GraphDatabaseService service) throws IOException {
         assert drive != null;
         initialyzeStatistics(drive, service, new NullProgressMonitor());
     }
     
     /**
      * Creates Calculator of Call Statistics
      * 
      * @param drive Dataset Node
      * @throws IOException if was problem in initializing of indexes
      */
     public CallStatistics(Node drive, GraphDatabaseService service, boolean testing) throws IOException {
         assert drive != null;
         isTest=testing;
         initialyzeStatistics(drive, service, new NullProgressMonitor());
         
     }
     
     /**
      * Creates Calculator of Call Statistics
      * 
      * @param drive Dataset Node
      * @throws IOException if was problem in initializing of indexes
      */
     public CallStatistics(Node drive, GraphDatabaseService service, IProgressMonitor monitor) throws IOException {
         assert drive != null;
         initialyzeStatistics(drive, service, monitor);
     }
 
     /**
      * Initialize statistics
      *
      * @param drive
      * @param service
      * @param aMonitor
      * @throws IOException
      */
     protected void initialyzeStatistics(Node drive, GraphDatabaseService service, IProgressMonitor aMonitor) throws IOException {
        initFilds(drive, service, aMonitor);
         
         statisticNode = createStatistics();
         
         Pair<Long, Long> minMax = getTimeBounds(datasetNode);
         long minTime = minMax.getLeft();
         long maxTime = minMax.getRight();
         setHighPeriod(minTime, maxTime); 
         if (!monitor.isCanceled()) {
             buildSecondLevelStatistics(minTime, maxTime, false);
             if (!isTest) {
                 NeoCorePlugin.getDefault().getUpdateViewManager().fireUpdateView(
                         new UpdateDatabaseEvent(UpdateViewEventType.STATISTICS));
             }
         }        
     }
 
     protected void setHighPeriod(long minTime, long maxTime) {
         highPeriod = getHighestPeriod(minTime, maxTime);
     }
 
    protected void initFilds(Node drive, GraphDatabaseService service, IProgressMonitor aMonitor) {
         datasetNode = drive;
         neoService = service;
         this.monitor = aMonitor;
         monitor.subTask("Getting statistics");
         statisticsConstants.put(StatisticsCallType.INDIVIDUAL, new IndividualCallConstants());
         statisticsConstants.put(StatisticsCallType.GROUP, new GroupCallConstants());
     }
     
     /**
      * @param statisticNode The statisticNode to set.
      */
     protected void setStatisticNode(HashMap<StatisticsCallType, Node> statisticNode) {
         this.statisticNode = statisticNode;
     }
     
     /**
      * @return Returns the datasetNode.
      */
     protected Node getDatasetNode() {
         if (datasetNode == null) {
             datasetNode = NeoUtils.getAllDatasetNodes(neoService).get(amsDatasetName);
         }
         return datasetNode;
     }
     
     /**
      * @return Returns the isTest.
      */
     protected boolean isTest() {
         return isTest;
     }
     
     /**
      * @return Returns the monitor.
      */
     protected IProgressMonitor getMonitor() {
         return monitor;
     }
     
     /**
      * @return Returns the neoService.
      */
     protected GraphDatabaseService getNeoService() {
         return neoService;
     }
     
     /**
      * @return Returns the previousSRowNodes.
      */
     protected HashMap<CallTimePeriods, Node> getPreviousSRowNodes() {
         return previousSRowNodes;
     }
     
     /**
      * @return Returns the previousSCellNodes.
      */
     protected HashMap<CallTimePeriods, Node> getPreviousSCellNodes() {
         return previousSCellNodes;
     }
     
     /**
      * Build aggregation statistics
      *
      * @param minTime Long
      * @param maxTime Long
      */
     protected void buildSecondLevelStatistics(long minTime, long maxTime, boolean isInconclusive) {
         Node secondLevel = statisticNode.get(StatisticsCallType.AGGREGATION_STATISTICS);
         if(secondLevel==null){
             AggregationCallStatisticsBuilder aggrStatisticsBuilder = new AggregationCallStatisticsBuilder(datasetNode, neoService, isInconclusive);
             secondLevel = aggrStatisticsBuilder.createAggregationStatistics(highPeriod, statisticNode,minTime,maxTime);
             if (secondLevel!=null) {
                 statisticNode.put(StatisticsCallType.AGGREGATION_STATISTICS, secondLevel); 
             }
         }
     }
     
     
     /**
      * Creates Statistics Node and calculates Call Statistics
      * 
      * @return Root statistics Node
      * @throws IOException 
      */
     protected HashMap<StatisticsCallType, Node> createStatistics() throws IOException {
         startTransaction(false);
         Node parentNode = null;
         HashMap<StatisticsCallType, Node> result = new HashMap<StatisticsCallType, Node>();
        
         try {
             if (datasetNode == null) {
                 datasetNode = NeoUtils.getAllDatasetNodes(neoService).get(amsDatasetName);
             }
             
             Iterator<Node> analyzisNodes = datasetNode.traverse(Order.BREADTH_FIRST, StopEvaluator.DEPTH_ONE, 
                     new ReturnableEvaluator() {                
                         @Override
                         public boolean isReturnableNode(TraversalPosition currentPos) {
                             if(currentPos.isStartNode()){
                                 return false;
                             }
                             Node node = currentPos.currentNode();
                             if(!NodeTypes.getNodeType(node, null).equals(NodeTypes.CALL_ANALYSIS_ROOT)){
                                 return false;
                             }
                             boolean inconclusive = (Boolean)node.getProperty(INeoConstants.PROPERTY_IS_INCONCLUSIVE, false);
                             return !inconclusive;
                         }
                     }, ProbeCallRelationshipType.CALL_ANALYSIS, Direction.OUTGOING).iterator();
             
             while (analyzisNodes.hasNext()) {
                 Node analyzisNode = analyzisNodes.next();
                 StatisticsCallType type = StatisticsCallType.getTypeById((String)analyzisNode.getProperty(CallProperties.CALL_TYPE.getId()));
                 result.put(type, analyzisNode);
             }
             
             if (!result.isEmpty()) {
                 return result;
             }
             
             Pair<Long, Long> minMax = getTimeBounds(datasetNode);
             long minTime = minMax.getLeft();
             long maxTime = minMax.getRight();
         
             CallTimePeriods period = getHighestPeriod(minTime, maxTime);            
             List<StatisticsCallType> callTypes = StatisticsCallType.getTypesByLevel(StatisticsCallType.FIRST_LEVEL);
             IProgressMonitor subMonitor = SubMonitor.convert(monitor, callTypes.size());
             subMonitor.beginTask("Create AMS statistics", callTypes.size());
             for (StatisticsCallType callType : callTypes) {
                
                 Collection<Node> probesByCallType = NeoUtils.getAllProbesOfDataset(datasetNode, callType.getId());
                 if (probesByCallType.isEmpty()) {
                     subMonitor.worked(1);
                     continue;
                 }
                 
                 parentNode = createRootStatisticsNode(datasetNode, callType,null,false);
                 result.put(callType, parentNode);
                 for (Node probe : probesByCallType) {
                     if(monitor.isCanceled()){
                         break;
                     }
                     String probeName = (String)probe.getProperty(INeoConstants.PROPERTY_NAME_NAME);
                     Node probeCallsNode = NeoUtils.getCallsNode(datasetNode, probeName, probe, neoService);
                     String callProbeName = (String)probeCallsNode.getProperty(INeoConstants.PROPERTY_NAME_NAME);
                 
                     if (NeoUtils.findMultiPropertyIndex(NeoUtils.getTimeIndexName(callProbeName), neoService) != null) {                
                         MultiPropertyIndex<Long> timeIndex = NeoUtils.getTimeIndexProperty(callProbeName);
                         timeIndex.initialize(neoService, null);
                         createStatistics(parentNode, null, null, probe, timeIndex, period, callType, minTime, maxTime);                        
                     }
                     commit();
                 }
                 previousSRowNodes.clear();
                 if(monitor.isCanceled()){
                     break;
                 }
                 subMonitor.worked(1);
             }
             
             transaction.success();
         }
         catch (Exception e) {            
             transaction.failure();
             NeoCorePlugin.error(null, e);
             throw (RuntimeException)new RuntimeException().initCause(e);
         }
         finally {
             transaction.finish();
         }
         return result;
     }
 
     protected void startTransaction(boolean inAnyCase) {
         if (transaction==null||inAnyCase) {
             transaction = neoService.beginTx();
         }
     }
     
     /**
      * @return Returns the transaction.
      */
     protected Transaction getTransaction() {
         return transaction;
     }
     
     protected void commit() {
         if (transaction != null) {
             transaction.success();
             transaction.finish();
             transaction = neoService.beginTx();
         }
     }
     
     protected Node createRootStatisticsNode(Node datasetNode, StatisticsCallType callType, Node sourceRoot, boolean isInconclusive) {
         Node result = neoService.createNode();
         
         result.setProperty(INeoConstants.PROPERTY_TYPE_NAME, NodeTypes.CALL_ANALYSIS_ROOT.getId());
         result.setProperty(INeoConstants.PROPERTY_NAME_NAME, INeoConstants.CALL_ANALYZIS_ROOT);
         result.setProperty(INeoConstants.PROPERTY_VALUE_NAME, NeoUtils.getNodeName(datasetNode,neoService));
         result.setProperty(INeoConstants.PROPERTY_IS_INCONCLUSIVE, isInconclusive);
         result.setProperty(CallProperties.CALL_TYPE.getId(), callType.toString());
         
         datasetNode.createRelationshipTo(result, ProbeCallRelationshipType.CALL_ANALYSIS);
         
         if(sourceRoot!=null){
             result.createRelationshipTo(sourceRoot, GeoNeoRelationshipTypes.SOURCE);
         }
         
         return result;
     }
     
     protected CallTimePeriods getHighestPeriod(long minTime, long maxTime) {
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
     
     protected Pair<Long, Long> getTimeBounds(Node dataset) {
         Transaction transaction = neoService.beginTx();
         try {
             return NeoUtils.getMinMaxTimeOfDataset(dataset, neoService);
         } finally {
             transaction.finish();
         }
     }
     
     private Long getSCellTime(Node sCell, Node probeNode, boolean inconclusive) {
         Node sRow = NeoUtils.getParent(neoService, sCell);
         
         Iterator<Node> rowIterator;
         if (inconclusive) {
             rowIterator = sRow.traverse(Order.DEPTH_FIRST, StopEvaluator.END_OF_GRAPH, new ReturnableEvaluator() {
 
                 @Override
                 public boolean isReturnableNode(TraversalPosition currentPos) {
                     return NeoUtils.isProbeNode(currentPos.currentNode());
                 }
             }, GeoNeoRelationshipTypes.SOURCE, Direction.OUTGOING).iterator();
         }else{
             rowIterator = sRow.traverse(Order.DEPTH_FIRST, StopEvaluator.DEPTH_ONE, new ReturnableEvaluator() {
 
                 @Override
                 public boolean isReturnableNode(TraversalPosition currentPos) {
                     return NeoUtils.isProbeNode(currentPos.currentNode());
                 }
             }, GeoNeoRelationshipTypes.SOURCE, Direction.OUTGOING).iterator();
         }
         if (rowIterator.hasNext()&&rowIterator.next().equals(probeNode)) {        
             return (Long)sRow.getProperty(INeoConstants.PROPERTY_TIME_NAME);
         }
         else {
             return null;
         }
     }
     
     protected Statistics getStatisticsFromDatabase(Node statisticsNode, StatisticsCallType callType, final long minDate, final long maxDate, final Node probeNode, final boolean inconclusive) {
         Iterator<Node> statisticsNodes = statisticsNode.traverse(Order.DEPTH_FIRST, StopEvaluator.END_OF_GRAPH, new ReturnableEvaluator() {
             
             @Override
             public boolean isReturnableNode(TraversalPosition currentPos) {
                 if (NeoUtils.getNodeType(currentPos.currentNode()).equals(NodeTypes.S_CELL.getId())) {
                     Long sCellTime = getSCellTime(currentPos.currentNode(), probeNode, inconclusive);
                     if ((sCellTime != null) && (sCellTime >= minDate) && (sCellTime < maxDate)) {
                         return true;
                     }
                 }
                 return false;                
             }
         }, GeoNeoRelationshipTypes.CHILD, Direction.OUTGOING, GeoNeoRelationshipTypes.NEXT, Direction.OUTGOING).iterator();
         
         boolean hasInDb = false;
         Statistics statistics = new Statistics();
         while (statisticsNodes.hasNext()) {
             hasInDb = true;
             Node sCell = statisticsNodes.next();
             
             String headerName = (String)sCell.getProperty(INeoConstants.PROPERTY_NAME_NAME);
             Object value = sCell.getProperty(INeoConstants.PROPERTY_VALUE_NAME,null);
             IStatisticsHeader header = callType.getHeaderByTitle(headerName);
             
             statistics.updateHeaderWithCall(header, value, sCell);
         }
         
         if (hasInDb) {
             return statistics;
         }
         else {
             return null;
         }
     }
     
     private Statistics createStatistics(Node parentNode, Node highStatisticsNode, Node highLevelSRow, Node probeNode, MultiPropertyIndex<Long> timeIndex, CallTimePeriods period, StatisticsCallType callType, long startDate, long endDate) {
         Statistics statistics = new Statistics();
         
         long currentStartDate = period.getFirstTime(startDate);
         long nextStartDate = getNextStartDate(period, endDate, currentStartDate);
         
         if (startDate > currentStartDate) {
             currentStartDate = startDate;
         }
         Node statisticsNode = getStatisticsNode(parentNode,null, period);
         if (highStatisticsNode != null) {
             highStatisticsNode.createRelationshipTo(statisticsNode, GeoNeoRelationshipTypes.SOURCE);
         }
         
         do {
             if(monitor.isCanceled()){
                 break;
             }
             Node sRow = createSRowNode(statisticsNode, period.getFirstTime(currentStartDate), probeNode, highLevelSRow, period);
             
             Statistics periodStatitics = new Statistics();
             if (period == CallTimePeriods.HOURLY) {
                 periodStatitics = getStatisticsByHour(null, timeIndex, callType, currentStartDate, nextStartDate);
             }
             else {
                 periodStatitics = getStatisticsFromDatabase(statisticsNode, callType, currentStartDate, nextStartDate, probeNode, false);
                 if (periodStatitics == null) {
                     periodStatitics = createStatistics(parentNode, statisticsNode, sRow, probeNode, timeIndex, period.getUnderlyingPeriod(), callType, currentStartDate, nextStartDate);
                 }
             }
             
             for (IStatisticsHeader header : callType.getHeaders()) {                
                 createSCellNode(sRow, periodStatitics, header, period);
             }           
             previousSCellNodes.put(period, null);
             
             
             updateStatistics(statistics, periodStatitics);            
             
             currentStartDate = nextStartDate;
             nextStartDate = getNextStartDate(period, endDate, currentStartDate);
             commit();
         }
         while (currentStartDate < endDate);
         return statistics;
     }
 
     protected long getNextStartDate(CallTimePeriods period, long endDate, long currentStartDate) {
         long nextStartDate = period.addPeriod(currentStartDate);
         if(!period.equals(CallTimePeriods.HOURLY)&&(nextStartDate > endDate)){
             nextStartDate = endDate;
         }
         return nextStartDate;
     }
     
     protected Node getStatisticsNode(Node parent,Node source, final CallTimePeriods period) {
         Node node = getStatisticsNodeFromDB(parent, period);        
         if (node != null) {
             return node;
         }
         Node result = neoService.createNode();        
         result.setProperty(INeoConstants.PROPERTY_NAME_NAME, period.getId());
         result.setProperty(INeoConstants.PROPERTY_TYPE_NAME, NodeTypes.CALL_ANALYSIS.getId());
         parent.createRelationshipTo(result, GeoNeoRelationshipTypes.CHILD);
         if(source!=null){
             result.createRelationshipTo(source, GeoNeoRelationshipTypes.SOURCE);
         }
         
         return result;        
     }
     
     protected Node getStatisticsNodeFromDB(Node parent, final CallTimePeriods period){
         if(parent == null){
             return null;
         }
         Iterator<Node> nodes = parent.traverse(Order.BREADTH_FIRST, StopEvaluator.DEPTH_ONE, new ReturnableEvaluator() {
             
             @Override
             public boolean isReturnableNode(TraversalPosition currentPos) {
                 return period.getId().equals(NeoUtils.getNodeName(currentPos.currentNode(),neoService));
             }
         }, GeoNeoRelationshipTypes.CHILD, Direction.OUTGOING).iterator();
         
         if (nodes.hasNext()) {
             return nodes.next();
         }
         return null;
     }
     
     protected Node createSRowNode(Node parent, Long startDate, Node probeNode, Node highLevelSRow, CallTimePeriods period) {
         Node result = neoService.createNode();
         String name = NeoUtils.getFormatDateStringForSrow(startDate, period.addPeriod(startDate), "HH:mm", period.getId());
         result.setProperty(INeoConstants.PROPERTY_TYPE_NAME, NodeTypes.S_ROW.getId());
         result.setProperty(INeoConstants.PROPERTY_NAME_NAME, name);
         result.setProperty(INeoConstants.PROPERTY_TIME_NAME, startDate);
         
         result.createRelationshipTo(probeNode, GeoNeoRelationshipTypes.SOURCE);
         
         Node previousNode = previousSRowNodes.get(period);
         if (previousNode == null) {
             parent.createRelationshipTo(result, GeoNeoRelationshipTypes.CHILD);
         }
         else {
             previousNode.createRelationshipTo(result, GeoNeoRelationshipTypes.NEXT);
         }
         previousSRowNodes.put(period, result);
         
         if (highLevelSRow != null) {
             highLevelSRow.createRelationshipTo(result, GeoNeoRelationshipTypes.SOURCE);
         }
         
         return result;
     }
     
     protected Node createSCellNode(Node parent, Statistics statistics, IStatisticsHeader header, CallTimePeriods period) {
         Node result = neoService.createNode();
         
         result.setProperty(INeoConstants.PROPERTY_TYPE_NAME, NodeTypes.S_CELL.getId());
         result.setProperty(INeoConstants.PROPERTY_NAME_NAME, header.getTitle());
         Object value = statistics.get(header);
         if (value != null)  {        
             for (Node callNode : statistics.getAllAffectedCalls(header)) {
                 result.createRelationshipTo(callNode, GeoNeoRelationshipTypes.SOURCE);            
             }
             statistics.getAllAffectedCalls(header).clear();
             result.setProperty(INeoConstants.PROPERTY_VALUE_NAME, value);
         }
         
         statistics.updateSourceNodes(header, result);
         
         Node previousNode = previousSCellNodes.get(period);
         if (previousNode == null) {
             parent.createRelationshipTo(result, GeoNeoRelationshipTypes.CHILD);
         }
         else {
             previousNode.createRelationshipTo(result, GeoNeoRelationshipTypes.NEXT);
         }
         previousSCellNodes.put(period, result);
         
         return result;
     }
     
     private void updateStatistics(Statistics original, Statistics newValues) { 
         for (Entry<IStatisticsHeader, Object> entry : newValues.entrySet()) {
             IStatisticsHeader header = entry.getKey();
             original.updateHeader(header, entry.getValue());
             original.copyAllSourceNodes(header, newValues.getAllAffectedCalls(header));
         }
             
     }
     
     protected Statistics getStatisticsByHour(Node sourceRow, MultiPropertyIndex<Long> timeIndex, StatisticsCallType callType, long startTime, long endTime) throws IllegalArgumentException {
         Statistics statistics = new Statistics();
         
         Collection<Node> callNodes = timeIndex.find(new Long[] {startTime}, new Long[] {endTime});
         for (Node singleNode : callNodes) {
             if (singleNode.getProperty(CallProperties.CALL_TYPE.getId()).equals(callType.toString())) {
                 HashMap<IStatisticsHeader, Node> sourceCells = getRowAsMapForCall(sourceRow,singleNode, callType);
                 updateCallStatistics(singleNode,sourceCells, statistics, callType);
             }
         }
         
         return statistics;
     }
     
     private HashMap<IStatisticsHeader, Node> getRowAsMapForCall(Node row, Node call, StatisticsCallType callType){
         HashMap<IStatisticsHeader, Node> result = new HashMap<IStatisticsHeader, Node>();
         if(row==null){
             return result;
         }
         Traverser cells = NeoUtils.getChildTraverser(row);
         String callName = NeoUtils.getNodeName(call, neoService);
         for(Node cell : cells){
             boolean hasCall = false;
             for(Relationship relation : cell.getRelationships(GeoNeoRelationshipTypes.SOURCE, Direction.OUTGOING)){
                 Node node = relation.getEndNode();
                 NodeTypes nodeType = NodeTypes.getNodeType(node, neoService);
                 if(nodeType!=null&&nodeType.equals(NodeTypes.CALL)){
                     String name = NeoUtils.getNodeName(node, neoService);
                     if(name.equals(callName)){
                         hasCall=true;
                         break;
                     }
                 }
             }
             if (hasCall) {
                 String headerName = (String)cell.getProperty(INeoConstants.PROPERTY_NAME_NAME);
                 IStatisticsHeader header = callType.getHeaderByTitle(headerName);
                 result.put(header, cell);
             }
         }
         return result;
     }
     
     private void updateCallStatistics(Node callNode, HashMap<IStatisticsHeader, Node> sourceCells, Statistics statistics, StatisticsCallType callType) {
         ICallStatisticsConstants constants = statisticsConstants.get(callType);
         for(IStatisticsHeader header : callType.getHeaders()){
             Node sourceCell = sourceCells.get(header);
             if(sourceCell==null){
                 statistics.updateHeaderWithCall(header, header.getStatisticsData(callNode, constants, false), callNode);
             }else{
                 statistics.updateHeaderWithCall(header, sourceCell.getProperty(INeoConstants.PROPERTY_VALUE_NAME,null), sourceCell);
             }
         }
     }
 
     /**
      * @return Returns the statisticNode.
      */
     public HashMap<StatisticsCallType, Node> getStatisticNode() {
         return statisticNode;
     }
 
     /**
      * @return Returns the highPeriod.
      */
     public CallTimePeriods getHighPeriod() {
         return highPeriod;
     }
 
     /**
      * @param periods
      */
     public Node getPeriodNode(final CallTimePeriods periods, StatisticsCallType callType) {
         Node rootNode = statisticNode.get(callType);
         if (rootNode != null) {
             Iterator<Node> iterator = rootNode.traverse(Order.DEPTH_FIRST, StopEvaluator.DEPTH_ONE, new ReturnableEvaluator() {
 
                 @Override
                 public boolean isReturnableNode(TraversalPosition currentPos) {
                     return periods.getId().equalsIgnoreCase(NeoUtils.getNodeName(currentPos.currentNode(),neoService));
                 }
             }, GeoNeoRelationshipTypes.CHILD, Direction.OUTGOING).iterator();
             return iterator.hasNext() ? iterator.next() : null;
         }
         return null;
     }
 
 }
