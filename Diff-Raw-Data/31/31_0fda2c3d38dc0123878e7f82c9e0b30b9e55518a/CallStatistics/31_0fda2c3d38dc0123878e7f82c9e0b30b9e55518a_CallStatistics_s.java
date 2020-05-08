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
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map.Entry;
 
 import org.amanzi.awe.views.calls.CallTimePeriods;
 import org.amanzi.neo.core.INeoConstants;
 import org.amanzi.neo.core.NeoCorePlugin;
 import org.amanzi.neo.core.enums.CallProperties;
 import org.amanzi.neo.core.enums.GeoNeoRelationshipTypes;
 import org.amanzi.neo.core.enums.NodeTypes;
 import org.amanzi.neo.core.enums.ProbeCallRelationshipType;
 import org.amanzi.neo.core.enums.CallProperties.CallResult;
 import org.amanzi.neo.core.enums.CallProperties.CallType;
 import org.amanzi.neo.core.utils.NeoUtils;
 import org.amanzi.neo.core.utils.Pair;
 import org.amanzi.neo.index.MultiPropertyIndex;
 import org.neo4j.api.core.Direction;
 import org.neo4j.api.core.NeoService;
 import org.neo4j.api.core.Node;
 import org.neo4j.api.core.ReturnableEvaluator;
 import org.neo4j.api.core.StopEvaluator;
 import org.neo4j.api.core.Transaction;
 import org.neo4j.api.core.TraversalPosition;
 import org.neo4j.api.core.Traverser.Order;
 
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
      * a Week period
      */
     private static final long WEEK = 7 * DAY;
     
     /*
      * a Month period
      */
     private static final long MONTH = 28 * DAY;
     
     /*
      * Type of Statistics calculation
      */
     enum StatisticsType {
         MIN, MAX, COUNT, SUM;
     }
     
     /**
      * Headers of Statistics
      * 
      * @author Lagutko_N
      * @since 1.0.0
      */
     public enum StatisticsHeaders {
         CALL_ATTEMPT_COUNT("CALL_ATTEMPT_COUNT", StatisticsType.COUNT),
         SUCC_SETUP_COUNT("SUCC_SETUP_COUNT", StatisticsType.COUNT),
         SETUP_TM_Z1_P1("SETUP_TM_Z1_P1", StatisticsType.COUNT),
         SETUP_TM_Z1_P2("SETUP_TM_Z1_P2", StatisticsType.COUNT),
         SETUP_TM_Z1_P3("SETUP_TM_Z1_P3", StatisticsType.COUNT),
         SETUP_TM_Z1_P4("SETUP_TM_Z1_P4", StatisticsType.COUNT),
         SETUP_TM_Z1_L1("SETUP_TM_Z1_L1", StatisticsType.COUNT),
         SETUP_TM_Z1_L2("SETUP_TM_Z1_L2", StatisticsType.COUNT),
         SETUP_TM_Z1_L3("SETUP_TM_Z1_L3", StatisticsType.COUNT),
         SETUP_TM_Z1_L4("SETUP_TM_Z1_L4", StatisticsType.COUNT),
         SETUP_TIME_MIN("SETUP_TIME_MIN", StatisticsType.MIN),
         SETUP_TIME_MAX("SETUP_TIME_MAX", StatisticsType.MAX),
         SETUP_TOTAL_DUR("SETUP_TOTAL_DUR", StatisticsType.SUM);
         /**
          * Finds enum by id
          * 
          * @param periodId type id
          * @return enum or null
          */
         public static StatisticsHeaders findById(String periodId) {
             if (periodId == null) {
                 return null;
             }
             for (StatisticsHeaders period : StatisticsHeaders.values()) {
                 if (period.getTitle().equals(periodId)) {
                     return period;
                 }
             }
             return null;
         }
 
         /**
          * Title of this Statistics Header
          */
         private String headerTitle;
         
         /**
          * Type of Calculation
          */
         private StatisticsType type;
         
         private StatisticsHeaders(String headerTitle, StatisticsType type) {
             this.headerTitle = headerTitle;           
             this.type = type;
         }
         
         /**
          * Returns Title of Header
          *
          * @return title of Header
          */
         public String getTitle() {
             return headerTitle;
         }
         
         /**
          * Returns type of Calculation for Statistics
          *
          * @return type of calculation
          */
         public StatisticsType getType() {
             return type;
         }
         
         /**
          * Returns a Statistics header by it's title
          *
          * @param title title of Header
          * @return Statistics Header
          */
         public static StatisticsHeaders getHeaderByTitle(String title) {
             for (StatisticsHeaders header : values()) {
                 if (header.getTitle().equals(title)) {
                     return header;
                 }
             }
             return null;
         }
     }
     
     /*
      * Name of AMS dataset
      */
     private String amsDatasetName;
     
     /*
      * Neo Service 
      */
     private NeoService neoService;
     
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
     private HashMap<CallType, Node> statisticNode = new HashMap<CallType, Node>();
 
     /*
      * Highest period 
      */
     private CallTimePeriods highPeriod;
     
     private HashMap<CallType, IStatisticsConstants> statisticsConstants = new HashMap<CallType, IStatisticsConstants>();
     
    /**
      * Creates Calculator of Call Statistics
      * 
      * @param drive Dataset Node
      * @throws IOException if was problem in initializing of indexes
      */
     public CallStatistics(Node drive, NeoService service) throws IOException {
         assert drive != null;
         datasetNode = drive;
         neoService = service;
         
         statisticsConstants.put(CallType.INDIVIDUAL, new IndividualCallConstants());
         statisticsConstants.put(CallType.GROUP, new GroupCallConstants());
         
         Transaction tx = neoService.beginTx();
         try {
             statisticNode = createStatistics();
             Pair<Long, Long> minMax = getTimeBounds(datasetNode);
             long minTime = minMax.getLeft();
             long maxTime = minMax.getRight();
             highPeriod = getHighestPeriod(minTime, maxTime);            
         }
         catch (Exception e) {
             tx.failure();
         } finally {
             tx.finish();
         }
     }
     
     /**
      * Creates Statistics Node and calculates Call Statistics
      * 
      * @return Root statistics Node
      * @throws IOException 
      */
     private HashMap<CallType, Node> createStatistics() throws IOException {
         Transaction tx = neoService.beginTx();
         Node parentNode = null;
         HashMap<CallType, Node> result = new HashMap<CallType, Node>();
         
         try {
             if (datasetNode == null) {
                 datasetNode = NeoUtils.getAllDatasetNodes(neoService).get(amsDatasetName);
             }
             
             Iterator<Node> analyzisNodes = datasetNode.traverse(Order.BREADTH_FIRST, StopEvaluator.DEPTH_ONE, ReturnableEvaluator.ALL_BUT_START_NODE, ProbeCallRelationshipType.CALL_ANALYZIS, Direction.OUTGOING).iterator();
             
             while (analyzisNodes.hasNext()) {
                 Node analyzisNode = analyzisNodes.next();
                 CallType type = CallType.valueOf((String)analyzisNode.getProperty(CallProperties.CALL_TYPE.getId()));
                 result.put(type, analyzisNode);
             }
             
             if (!result.isEmpty()) {
                 return result;
             }
             
             Pair<Long, Long> minMax = getTimeBounds(datasetNode);
             long minTime = minMax.getLeft();
             long maxTime = minMax.getRight();
         
             CallTimePeriods period = getHighestPeriod(minTime, maxTime);
             
             for (CallType callType : CallType.values()) {
                 Collection<Node> probesByCallType = NeoUtils.getAllProbesOfDataset(datasetNode, callType);
                 if (probesByCallType.isEmpty()) {
                     continue;
                 }
                 parentNode = createRootStatisticsNode(datasetNode, callType);
                 result.put(callType, parentNode);
                 for (Node probe : NeoUtils.getAllProbesOfDataset(datasetNode, callType)) {
                     String probeName = (String)probe.getProperty(INeoConstants.PROPERTY_NAME_NAME);
                     Node probeCallsNode = NeoUtils.getCallsNode(datasetNode, probeName, probe, neoService);
                     String callProbeName = (String)probeCallsNode.getProperty(INeoConstants.PROPERTY_NAME_NAME);
                 
                     if (NeoUtils.findMultiPropertyIndex(NeoUtils.getTimeIndexName(callProbeName), neoService) != null) {                
                         MultiPropertyIndex<Long> timeIndex = NeoUtils.getTimeIndexProperty(callProbeName);
                         timeIndex.initialize(neoService, null);
                         createStatistics(parentNode, null, null, probe, timeIndex, period, callType, minTime, maxTime);                        
                     }
                 }
                 previousSRowNodes.clear();
             }
             
             tx.success();
         }
         catch (Exception e) {            
             tx.failure();
             NeoCorePlugin.error(null, e);
         }
         finally {
             tx.finish();
         }
         return result;
     }
     
     private Node createRootStatisticsNode(Node datasetNode, CallType callType) {
         Node result = neoService.createNode();
         
         result.setProperty(INeoConstants.PROPERTY_TYPE_NAME, NodeTypes.CALL_ANALYZIS_ROOT.getId());
         result.setProperty(INeoConstants.PROPERTY_NAME_NAME, INeoConstants.CALL_ANALYZIS_ROOT);
         result.setProperty(INeoConstants.PROPERTY_VALUE_NAME, NeoUtils.getNodeName(datasetNode,neoService));
         result.setProperty(CallProperties.CALL_TYPE.getId(), callType.toString());
         
         datasetNode.createRelationshipTo(result, ProbeCallRelationshipType.CALL_ANALYZIS);
         
         return result;
     }
     
     private CallTimePeriods getHighestPeriod(long minTime, long maxTime) {
         long delta = CallTimePeriods.MONTHLY.getFirstTime(maxTime) - CallTimePeriods.MONTHLY.getFirstTime(minTime);
         if (delta >= MONTH) {
             return CallTimePeriods.MONTHLY;
         }
         delta = CallTimePeriods.WEEKLY.getFirstTime(maxTime) - CallTimePeriods.WEEKLY.getFirstTime(minTime);
         if (delta >= WEEK) {
             return CallTimePeriods.WEEKLY;
         }
         delta = CallTimePeriods.DAILY.getFirstTime(maxTime) - CallTimePeriods.DAILY.getFirstTime(minTime);
         if (delta >= DAY) {
             return CallTimePeriods.DAILY;
         }
         
         return CallTimePeriods.HOURLY;
     }
     
     private Pair<Long, Long> getTimeBounds(Node dataset) {
         Transaction transaction = neoService.beginTx();
         try {
             Node gisNode = NeoUtils.findGisNodeByChild(dataset,neoService);
             return NeoUtils.getMinMaxTimeOfDataset(gisNode, neoService);
         } finally {
             transaction.finish();
         }
     }
     
     private Long getSCellTime(Node sCell, Node probeNode) {
         Node sRow = NeoUtils.getParent(neoService, sCell);
         
         if (sRow.traverse(Order.DEPTH_FIRST, StopEvaluator.DEPTH_ONE, new ReturnableEvaluator() {
             
             @Override
             public boolean isReturnableNode(TraversalPosition currentPos) {
                 return NeoUtils.isProbeNode(currentPos.currentNode());
             }
         }, GeoNeoRelationshipTypes.SOURCE, Direction.OUTGOING).iterator().next().equals(probeNode)) {        
             return (Long)sRow.getProperty(INeoConstants.PROPERTY_TIME_NAME);
         }
         else {
             return null;
         }
     }
     
     private Statistics getStatisticsFromDatabase(Node statisticsNode, final long minDate, final long maxDate, final Node probeNode) {
         Iterator<Node> statisticsNodes = statisticsNode.traverse(Order.DEPTH_FIRST, StopEvaluator.END_OF_GRAPH, new ReturnableEvaluator() {
             
             @Override
             public boolean isReturnableNode(TraversalPosition currentPos) {
                 if (NeoUtils.getNodeType(currentPos.currentNode()).equals(NodeTypes.S_CELL.getId())) {
                     Long sCellTime = getSCellTime(currentPos.currentNode(), probeNode);
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
             Object value = sCell.getProperty(INeoConstants.PROPERTY_VALUE_NAME);
             StatisticsHeaders header = StatisticsHeaders.getHeaderByTitle(headerName);
             
             statistics.updateHeaderWithCall(header, value, sCell);
         }
         
         if (hasInDb) {
             return statistics;
         }
         else {
             return null;
         }
     }
     
     private Statistics createStatistics(Node parentNode, Node highStatisticsNode, Node highLevelSRow, Node probeNode, MultiPropertyIndex<Long> timeIndex, CallTimePeriods period, CallType callType, long startDate, long endDate) {
         Statistics statistics = new Statistics();
         
         long currentStartDate = period.getFirstTime(startDate);
        long nextStartDate = period.addPeriod(currentStartDate);
         
         if (startDate > currentStartDate) {
             currentStartDate = startDate;
         }
         
         Node statisticsNode = getStatisticsNode(parentNode, period);
         if (highStatisticsNode != null) {
             highStatisticsNode.createRelationshipTo(statisticsNode, GeoNeoRelationshipTypes.SOURCE);
         }
         
         do {
             Node sRow = createSRowNode(statisticsNode, new Date(period.getFirstTime(currentStartDate)), probeNode, highLevelSRow, period);
             
             Statistics periodStatitics = new Statistics();
             if (period == CallTimePeriods.HOURLY) {
                 periodStatitics = getStatisticsByHour(timeIndex, callType, currentStartDate, nextStartDate);
             }
             else {
                 periodStatitics = getStatisticsFromDatabase(statisticsNode, currentStartDate, nextStartDate, probeNode);
                 if (periodStatitics == null) {
                     periodStatitics = createStatistics(parentNode, statisticsNode, sRow, probeNode, timeIndex, period.getUnderlyingPeriod(), callType, currentStartDate, nextStartDate);
                 }
             }
             
             for (StatisticsHeaders header : StatisticsHeaders.values()) {                
                 createSCellNode(sRow, periodStatitics, header, period);
             }           
             previousSCellNodes.put(period, null);
             
             
             updateStatistics(statistics, periodStatitics);            
             
             currentStartDate = nextStartDate;
            nextStartDate = period.addPeriod(currentStartDate);
         }
         while (currentStartDate < endDate);
         
         return statistics;
     }
     
     private Node getStatisticsNode(Node parent, final CallTimePeriods period) {
         Iterator<Node> nodes = parent.traverse(Order.BREADTH_FIRST, StopEvaluator.DEPTH_ONE, new ReturnableEvaluator() {
             
             @Override
             public boolean isReturnableNode(TraversalPosition currentPos) {
                 return period.getId().equals(NeoUtils.getNodeName(currentPos.currentNode(),neoService));
             }
         }, GeoNeoRelationshipTypes.CHILD, Direction.OUTGOING).iterator();
         
         if (nodes.hasNext()) {
             return nodes.next();
         }
         else {        
             Node result = neoService.createNode();
         
             result.setProperty(INeoConstants.PROPERTY_NAME_NAME, period.getId());
             result.setProperty(INeoConstants.PROPERTY_TYPE_NAME, NodeTypes.CALL_ANALYZIS.getId());
             parent.createRelationshipTo(result, GeoNeoRelationshipTypes.CHILD);
             
             return result;
         }
     }
     
     private Node createSRowNode(Node parent, Date startDate, Node probeNode, Node highLevelSRow, CallTimePeriods period) {
         Node result = neoService.createNode();
         String name = NeoUtils.getFormatDateString(startDate.getTime(), period.addPeriod(startDate.getTime()), "HH:mm");
         result.setProperty(INeoConstants.PROPERTY_TYPE_NAME, NodeTypes.S_ROW.getId());
         result.setProperty(INeoConstants.PROPERTY_NAME_NAME, name);
         result.setProperty(INeoConstants.PROPERTY_TIME_NAME, startDate.getTime());
         
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
     
     private Node createSCellNode(Node parent, Statistics statistics, StatisticsHeaders header, CallTimePeriods period) {
         Node result = neoService.createNode();
         
         result.setProperty(INeoConstants.PROPERTY_TYPE_NAME, NodeTypes.S_CELL.getId());
         result.setProperty(INeoConstants.PROPERTY_NAME_NAME, header.getTitle());
         Object value = statistics.get(header);
         if (value == null) {
             switch (header.getType()) {
             case COUNT:
                 value = new Integer(0);
                 break;
             default:
                 value = new Float(0);
                 break;
             }                        
         }
         else {        
             for (Node callNode : statistics.getAllAffectedCalls(header)) {
                 result.createRelationshipTo(callNode, GeoNeoRelationshipTypes.SOURCE);            
             }
             statistics.getAllAffectedCalls(header).clear();
         }
         result.setProperty(INeoConstants.PROPERTY_VALUE_NAME, value);
         
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
         for (Entry<StatisticsHeaders, Object> entry : newValues.entrySet()) {
             StatisticsHeaders header = entry.getKey();
             original.updateHeader(header, entry.getValue());
             original.copyAllSourceNodes(header, newValues.getAllAffectedCalls(header));
         }
             
     }
     
     private Statistics getStatisticsByHour(MultiPropertyIndex<Long> timeIndex, CallType callType, long startTime, long endTime) throws IllegalArgumentException {
         Statistics statistics = new Statistics();
         
         Collection<Node> callNodes = timeIndex.find(new Long[] {startTime}, new Long[] {endTime});
         
         for (Node singleNode : callNodes) {
             if (singleNode.getProperty(CallProperties.CALL_TYPE.getId()).equals(callType.toString())) {
                 updateCallStatistics(singleNode, statistics, callType);
             }
         }
         
         return statistics;
     }
     
     private void updateCallStatistics(Node callNode, Statistics statistics, CallType callType) {
         statistics.updateHeaderWithCall(StatisticsHeaders.CALL_ATTEMPT_COUNT, 1, callNode);
         
         CallResult callResult = CallResult.valueOf((String)callNode.getProperty(CallProperties.CALL_RESULT.getId()));
         if (callResult == CallResult.SUCCESS) {
             long connectionTime = (Long)callNode.getProperty(CallProperties.SETUP_DURATION.getId());
             processConnectionTime(connectionTime, statistics, callNode, statisticsConstants.get(callType));
         }
     }
     
     private void processConnectionTime(long connectionTime, Statistics statistics, Node callNode, IStatisticsConstants constants) {
         float connTimeSec = (float)connectionTime / IStatisticsConstants.MILLISECONDS_FACTOR;
         
         if (connTimeSec < constants.getCallConnTimeLimit()) {        
             statistics.updateHeaderWithCall(StatisticsHeaders.SUCC_SETUP_COUNT, 1, callNode);
             statistics.updateHeaderWithCall(StatisticsHeaders.SETUP_TIME_MAX, connTimeSec, callNode);
             statistics.updateHeaderWithCall(StatisticsHeaders.SETUP_TIME_MIN, connTimeSec, callNode);
             statistics.updateHeaderWithCall(StatisticsHeaders.SETUP_TOTAL_DUR, connTimeSec, callNode);
         }
         else {
             return;
         }
         
         if (connTimeSec > constants.getCallConnTimeP1()) {
             if (connTimeSec < constants.getCallConnTimeP2()) {
                 statistics.updateHeaderWithCall(StatisticsHeaders.SETUP_TM_Z1_P1, 1, callNode);
             }
             else if (connTimeSec < constants.getCallConnTimeP3()) {
                 statistics.updateHeaderWithCall(StatisticsHeaders.SETUP_TM_Z1_P2, 1, callNode);
             }
             else if (connTimeSec < constants.getCallConnTimeP4()) {
                 statistics.updateHeaderWithCall(StatisticsHeaders.SETUP_TM_Z1_P3, 1, callNode);
             }
             else if (connTimeSec < constants.getCallConnTimeL1()) {
                 statistics.updateHeaderWithCall(StatisticsHeaders.SETUP_TM_Z1_P4, 1, callNode);
             }
             else if (connTimeSec < constants.getCallConnTimeL2()) {
                 statistics.updateHeaderWithCall(StatisticsHeaders.SETUP_TM_Z1_L1, 1, callNode);
             }
             else if (connTimeSec < constants.getCallConnTimeL3()) {
                 statistics.updateHeaderWithCall(StatisticsHeaders.SETUP_TM_Z1_L2, 1, callNode);
             }
             else if (connTimeSec < constants.getCallConnTimeL4()) {
                 statistics.updateHeaderWithCall(StatisticsHeaders.SETUP_TM_Z1_L3, 1, callNode);
             }
             else if (connTimeSec < constants.getCallConnTimeLimit()) {
                 statistics.updateHeaderWithCall(StatisticsHeaders.SETUP_TM_Z1_L4, 1, callNode);
             }
         }
     }
 
     /**
      * @return Returns the statisticNode.
      */
     public HashMap<CallType, Node> getStatisticNode() {
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
     public Node getPeriodNode(final CallTimePeriods periods, CallType callType) {
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
