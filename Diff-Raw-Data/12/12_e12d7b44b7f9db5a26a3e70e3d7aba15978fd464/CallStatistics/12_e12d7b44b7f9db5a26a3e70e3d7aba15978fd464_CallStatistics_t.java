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
 import org.neo4j.graphdb.Direction;
 import org.neo4j.graphdb.GraphDatabaseService;
 import org.neo4j.graphdb.Node;
 import org.neo4j.graphdb.ReturnableEvaluator;
 import org.neo4j.graphdb.StopEvaluator;
 import org.neo4j.graphdb.Transaction;
 import org.neo4j.graphdb.TraversalPosition;
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
     
    /**
      * Creates Calculator of Call Statistics
      * 
      * @param drive Dataset Node
      * @throws IOException if was problem in initializing of indexes
      */
     public CallStatistics(Node drive, GraphDatabaseService service) throws IOException {
         assert drive != null;
         datasetNode = drive;
         neoService = service;
         
         statisticsConstants.put(StatisticsCallType.INDIVIDUAL, new IndividualCallConstants());
         statisticsConstants.put(StatisticsCallType.GROUP, new GroupCallConstants());
         
         Transaction tx = neoService.beginTx();
         try {
             statisticNode = createStatistics();
             Pair<Long, Long> minMax = getTimeBounds(datasetNode);
             long minTime = minMax.getLeft();
             long maxTime = minMax.getRight();
             highPeriod = getHighestPeriod(minTime, maxTime); 
             NeoCorePlugin.getDefault().getUpdateViewManager().fireUpdateView(
                     new UpdateDatabaseEvent(UpdateViewEventType.STATISTICS));
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
     private HashMap<StatisticsCallType, Node> createStatistics() throws IOException {
         Transaction tx = neoService.beginTx();
         Node parentNode = null;
         HashMap<StatisticsCallType, Node> result = new HashMap<StatisticsCallType, Node>();
         
         try {
             if (datasetNode == null) {
                 datasetNode = NeoUtils.getAllDatasetNodes(neoService).get(amsDatasetName);
             }
             
             Iterator<Node> analyzisNodes = datasetNode.traverse(Order.BREADTH_FIRST, StopEvaluator.DEPTH_ONE, ReturnableEvaluator.ALL_BUT_START_NODE, ProbeCallRelationshipType.CALL_ANALYSIS, Direction.OUTGOING).iterator();
             
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
             
             for (StatisticsCallType callType : StatisticsCallType.getTypesByLevel(StatisticsCallType.FIRST_LEVEL)) {
                 Collection<Node> probesByCallType = NeoUtils.getAllProbesOfDataset(datasetNode, callType.getId());
                 if (probesByCallType.isEmpty()) {
                     continue;
                 }
                 
                 parentNode = createRootStatisticsNode(datasetNode, callType);
                 result.put(callType, parentNode);
                 for (Node probe : probesByCallType) {
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
             AggregationCallStatisticsBuilder aggrStatisticsBuilder = new AggregationCallStatisticsBuilder(datasetNode, neoService);
             parentNode = aggrStatisticsBuilder.createAggregationStatistics(period, result);
             if (parentNode!=null) {
                 result.put(StatisticsCallType.AGGREGATION_STATISTICS, parentNode); 
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
     
     private Node createRootStatisticsNode(Node datasetNode, StatisticsCallType callType) {
         Node result = neoService.createNode();
         
         result.setProperty(INeoConstants.PROPERTY_TYPE_NAME, NodeTypes.CALL_ANALYSIS_ROOT.getId());
         result.setProperty(INeoConstants.PROPERTY_NAME_NAME, INeoConstants.CALL_ANALYZIS_ROOT);
         result.setProperty(INeoConstants.PROPERTY_VALUE_NAME, NeoUtils.getNodeName(datasetNode,neoService));
         result.setProperty(CallProperties.CALL_TYPE.getId(), callType.toString());
         
         datasetNode.createRelationshipTo(result, ProbeCallRelationshipType.CALL_ANALYSIS);
         
         return result;
     }
     
     private CallTimePeriods getHighestPeriod(long minTime, long maxTime) {
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
     
     private Pair<Long, Long> getTimeBounds(Node dataset) {
         Transaction transaction = neoService.beginTx();
         try {
            return NeoUtils.getMinMaxTimeOfDataset(dataset, neoService);
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
     
     private Statistics getStatisticsFromDatabase(Node statisticsNode, StatisticsCallType callType, final long minDate, final long maxDate, final Node probeNode) {
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
                 periodStatitics = getStatisticsFromDatabase(statisticsNode, callType, currentStartDate, nextStartDate, probeNode);
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
         }
         while (currentStartDate < endDate);
         
         return statistics;
     }
 
     private long getNextStartDate(CallTimePeriods period, long endDate, long currentStartDate) {
         long nextStartDate = period.addPeriod(currentStartDate);
         if(!period.equals(CallTimePeriods.HOURLY)&&(nextStartDate > endDate)){
             nextStartDate = endDate;
         }
         return nextStartDate;
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
             result.setProperty(INeoConstants.PROPERTY_TYPE_NAME, NodeTypes.CALL_ANALYSIS.getId());
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
     
     private Node createSCellNode(Node parent, Statistics statistics, IStatisticsHeader header, CallTimePeriods period) {
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
         for (Entry<IStatisticsHeader, Object> entry : newValues.entrySet()) {
             IStatisticsHeader header = entry.getKey();
             original.updateHeader(header, entry.getValue());
             original.copyAllSourceNodes(header, newValues.getAllAffectedCalls(header));
         }
             
     }
     
     private Statistics getStatisticsByHour(MultiPropertyIndex<Long> timeIndex, StatisticsCallType callType, long startTime, long endTime) throws IllegalArgumentException {
         Statistics statistics = new Statistics();
         
         Collection<Node> callNodes = timeIndex.find(new Long[] {startTime}, new Long[] {endTime});
         for (Node singleNode : callNodes) {
             if (singleNode.getProperty(CallProperties.CALL_TYPE.getId()).equals(callType.toString())) {
                 updateCallStatistics(singleNode, statistics, callType);
             }
         }
         
         return statistics;
     }
     
     private void updateCallStatistics(Node callNode, Statistics statistics, StatisticsCallType callType) {
         ICallStatisticsConstants constants = statisticsConstants.get(callType);
         for(IStatisticsHeader header : callType.getHeaders()){
             statistics.updateHeaderWithCall(header, header.getStatisticsData(callNode, constants), callNode);
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
