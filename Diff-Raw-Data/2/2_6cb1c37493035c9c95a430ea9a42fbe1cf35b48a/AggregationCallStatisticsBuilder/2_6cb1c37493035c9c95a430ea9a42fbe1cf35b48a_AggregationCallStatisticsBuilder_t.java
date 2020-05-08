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
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 
 import org.amanzi.awe.statistic.CallTimePeriods;
 import org.amanzi.awe.views.calls.enums.AggregationCallTypes;
 import org.amanzi.awe.views.calls.enums.IAggrStatisticsHeaders;
 import org.amanzi.awe.views.calls.enums.IStatisticsHeader;
 import org.amanzi.awe.views.calls.enums.StatisticsCallType;
 import org.amanzi.awe.views.calls.enums.StatisticsType;
 import org.amanzi.neo.core.INeoConstants;
 import org.amanzi.neo.core.enums.CallProperties;
 import org.amanzi.neo.core.enums.GeoNeoRelationshipTypes;
 import org.amanzi.neo.core.enums.NodeTypes;
 import org.amanzi.neo.core.enums.ProbeCallRelationshipType;
 import org.amanzi.neo.core.utils.NeoUtils;
 import org.neo4j.graphdb.Direction;
 import org.neo4j.graphdb.GraphDatabaseService;
 import org.neo4j.graphdb.Node;
 import org.neo4j.graphdb.ReturnableEvaluator;
 import org.neo4j.graphdb.StopEvaluator;
 import org.neo4j.graphdb.Transaction;
 import org.neo4j.graphdb.TraversalPosition;
 import org.neo4j.graphdb.Traverser.Order;
 
 /**
  * <p>
  * Class for build second level statistics.
  * </p>
  * @author Shcharbatsevich_A
  * @since 1.0.0
  */
 public class AggregationCallStatisticsBuilder {
     
     private GraphDatabaseService service;  
     
     private Node dataset;    
     private HashMap<CallTimePeriods, Node> previousSCellNodes = new HashMap<CallTimePeriods, Node>();
     private HashMap<CallTimePeriods, Node> previousSRowNodes = new HashMap<CallTimePeriods, Node>();
     private HashMap<CallTimePeriods, List<Node>> sourceRows = new HashMap<CallTimePeriods, List<Node>>();
     
     private HashMap<AggregationCallTypes, Boolean> havingStats = new HashMap<AggregationCallTypes, Boolean>();
 
     private Transaction transaction;
     
     /**
      * Constructor.
      * @param datasetNode Node (virtual dataset)
      * @param neo GraphDatabaseService
      */
     public AggregationCallStatisticsBuilder(Node datasetNode,GraphDatabaseService neo) {
         dataset = datasetNode;
         service = neo;
     }
     
     /**
      * Create second level statistics.
      *
      * @param period CallTimePeriods (highest period)
      * @param sourceStatistics HashMap<StatisticsCallType, Node> (first level statistics)
      * @return Node (root of created statistics)
      */
     public Node createAggregationStatistics(CallTimePeriods period, HashMap<StatisticsCallType, Node> sourceStatistics,Long minTime, Long maxTime){
         transaction = service.beginTx();
         try {
             return createAggrStatisticsByPeriod(null, period, sourceStatistics, minTime, maxTime);
         } finally {
             updateTransaction(false);
         }
     }
     
     /**
      * Create second level statistics.
      *
      * @param parent Node
      * @param highestPeriod CallTimePeriods
      * @return Node (root for statistics)
      */
     private Node createAggrStatisticsByPeriod(Node parent, CallTimePeriods period, HashMap<StatisticsCallType, Node> sourceStatistics,Long minTime, Long maxTime){
         if(period == null){
             return parent;
         }
         Node rootNode = createAggrStatisticsByPeriod(parent, period.getUnderlyingPeriod(), sourceStatistics, minTime, maxTime);
         Long start = period.getFirstTime(minTime);
         Long nextStart = getNextStartDate(period, maxTime, start);
         Node statisticsNode = null;
         do{
             Statistics utilStatistics = buildUtilStatistics(period, sourceStatistics,start,nextStart);
             if(utilStatistics!=null){
                 if(rootNode == null){
                     rootNode = createRootStatisticsNode();
                 }
                 if (statisticsNode==null) {
                     statisticsNode = getStatisticsNode(rootNode, period);
                 }
                 List<Node> currSourceRows = sourceRows.get(period);
                 Node srow = createRow(statisticsNode, start, currSourceRows, period); 
                 for(AggregationCallTypes stat : AggregationCallTypes.values()){  
                     if(!havingStats.get(stat)){
                         continue;
                     }
                     rootNode.setProperty(stat.getRealType().getId().getProperty(), true);
                     for(IAggrStatisticsHeaders aggrHeader : stat.getAggrHeaders()){
                         List<Number> sources = new ArrayList<Number>();
                         List<Node> sourceCells = new ArrayList<Node>();
                         for(IStatisticsHeader header : aggrHeader.getDependendHeaders()){
                             Number value = (Number)utilStatistics.get(header);
                             if (value!=null) {
                                 sources.add(value);
                                 sourceCells.addAll(utilStatistics.getAllAffectedCalls(header));
                             }
                         }
                         Object statValue = getStatValue(aggrHeader, sources);
                         createCell(period, aggrHeader, statValue, srow, sourceCells);
                     }
                 }
                 sourceRows.put(period, null);
                 previousSCellNodes.put(period, null);
                 updateTransaction(true);
             }
             start = nextStart;
             nextStart = getNextStartDate(period, maxTime, start);
        }while(start < maxTime);
         return  rootNode;
     }
     
     private void updateTransaction(boolean needNew) {
         if (transaction!=null) {
             transaction.success();
             transaction.finish();
             
         }
         if(needNew){
             transaction = service.beginTx();
         }
     }
     
     private long getNextStartDate(CallTimePeriods period, long endDate, long currentStartDate) {
         long nextStartDate = period.addPeriod(currentStartDate);
         if(!period.equals(CallTimePeriods.HOURLY)&&(nextStartDate > endDate)){
             nextStartDate = endDate;
         }
         return nextStartDate;
     }
     
     /**
      * Build statistics by utility headers from source statistics.
      *
      * @param period CallTimePeriods
      * @param sourceStatistics HashMap<StatisticsCallType, Node>
      * @return statistics
      */
     private Statistics buildUtilStatistics(CallTimePeriods period, HashMap<StatisticsCallType, Node> sourceStatistics, Long start, Long end){
         Statistics result = null;
         for(AggregationCallTypes stat : AggregationCallTypes.values()){
             StatisticsCallType realType = stat.getRealType();
             Node periodNode = getPeriodNode(period, realType, sourceStatistics);
             if(periodNode==null){
                 havingStats.put(stat, false);
                 continue;
             }
             havingStats.put(stat, true);
             if(result==null){
                 result = new Statistics();
             }
             List<Node> currSourceRows = getAllPeriodRows(periodNode,start,end);
             for(Node row : currSourceRows){
                 HashMap<IStatisticsHeader, Node> sourceCells = getAllRowCells(row, realType);
                 for (IStatisticsHeader utilHeader : stat.getUtilHeaders()) {
                     for (IStatisticsHeader header : ((IAggrStatisticsHeaders)utilHeader).getDependendHeaders()) {
                         Node cell = sourceCells.get(header);                        
                         if (cell!=null) {
                             Number value = utilHeader.getStatisticsData(cell, null);
                             result.updateHeaderWithCall(utilHeader, value, cell);
                         }
                     }
                 }
             }
             List<Node> allSource = sourceRows.get(period);
             if(allSource==null){
                 allSource = currSourceRows;
             }else{
                 allSource.addAll(currSourceRows);
             }
             sourceRows.put(period, allSource);
         }
         return result;
     }
     
     /**
      * Find period node for call type.
      *
      * @param period CallTimePeriods
      * @param callType StatisticsCallType
      * @param sourceStatistics HashMap<StatisticsCallType, Node>
      * @return Node
      */
     private Node getPeriodNode(final CallTimePeriods period, StatisticsCallType callType, HashMap<StatisticsCallType, Node> sourceStatistics) {
         Node rootNode = sourceStatistics.get(callType);
         if (rootNode != null) {
             Iterator<Node> iterator = rootNode.traverse(Order.DEPTH_FIRST, StopEvaluator.DEPTH_ONE, new ReturnableEvaluator() {
 
                 @Override
                 public boolean isReturnableNode(TraversalPosition currentPos) {
                     return period.getId().equalsIgnoreCase(NeoUtils.getNodeName(currentPos.currentNode(),service));
                 }
             }, GeoNeoRelationshipTypes.CHILD, Direction.OUTGOING).iterator();
             return iterator.hasNext() ? iterator.next() : null;
         }
         return null;
     }
 
     /**
      * Get all rows in period.
      *
      * @param periodNode Node
      * @return List of nodes
      */
     private List<Node> getAllPeriodRows(Node periodNode, Long start,Long end){
         List<Node> result = new ArrayList<Node>();
         Iterator<Node> rows = NeoUtils.getChildTraverser(periodNode).iterator();
         while (rows.hasNext()) {
             Node row = rows.next();
             Long time = (Long)row.getProperty(INeoConstants.PROPERTY_TIME_NAME);
             if (start<=time&&time<end) {
                 result.add(row);
             }
         }
         return result;
     }
     
     /**
      * Get all cells from row.
      *
      * @param row Node
      * @param callType StatisticsCallType
      * @return HashMap<IStatisticsHeader, Node>
      */
     private HashMap<IStatisticsHeader, Node> getAllRowCells(Node row, StatisticsCallType callType){
         HashMap<IStatisticsHeader, Node> result = new HashMap<IStatisticsHeader, Node>();
         Iterator<Node> cells = NeoUtils.getChildTraverser(row).iterator();
         while (cells.hasNext()) {
             Node cell = cells.next();
             IStatisticsHeader header = callType.getHeaderByTitle(NeoUtils.getNodeName(cell));
             result.put(header, cell);
         }
         return result;
     }
     
     /**
      * Create row node.
      *
      * @param root Node
      * @param start Long
      * @param sources List of Nodes
      * @param period CallTimePeriods
      * @return Node
      */
     private Node createRow(Node root,Long start,List<Node> sources, CallTimePeriods period){
         Node result = service.createNode();
         String name = NeoUtils.getFormatDateString(start, period.addPeriod(start), "HH:mm");
         result.setProperty(INeoConstants.PROPERTY_TYPE_NAME, NodeTypes.S_ROW.getId());
         result.setProperty(INeoConstants.PROPERTY_NAME_NAME, name);
         result.setProperty(INeoConstants.PROPERTY_TIME_NAME, start);
         
         for(Node source : sources){
             result.createRelationshipTo(source, GeoNeoRelationshipTypes.SOURCE);
         }
         
         Node previousNode = previousSRowNodes.get(period);
         if (previousNode == null) {
             root.createRelationshipTo(result, GeoNeoRelationshipTypes.CHILD);
         }
         else {
             previousNode.createRelationshipTo(result, GeoNeoRelationshipTypes.NEXT);
         }
         previousSRowNodes.put(period, result);
         return result;
     }
     
     /**
      * Create cell node.
      *
      * @param period CallTimePeriods
      * @param header IStatisticsHeader
      * @param value Object
      * @param row Node
      * @param sources List of Nodes
      */
     private void createCell(CallTimePeriods period, IStatisticsHeader header, Object value, Node row, List<Node> sources){
         Node result = service.createNode();
         
         result.setProperty(INeoConstants.PROPERTY_TYPE_NAME, NodeTypes.S_CELL.getId());
         result.setProperty(INeoConstants.PROPERTY_NAME_NAME, header.getTitle());
         if (value != null) {
             switch (header.getType()) {
             case PERCENT:
                 value = (Float)value*100f;
                 break;
             default:
                 //do nothing
                 break;
             }
             for (Node source : sources) {
                 result.createRelationshipTo(source, GeoNeoRelationshipTypes.SOURCE);            
             }
             result.setProperty(INeoConstants.PROPERTY_VALUE_NAME, value);
         }       
         Node previousNode = previousSCellNodes.get(period);
         if (previousNode == null) {
             row.createRelationshipTo(result, GeoNeoRelationshipTypes.CHILD);
         }
         else {
             previousNode.createRelationshipTo(result, GeoNeoRelationshipTypes.NEXT);
         }
         previousSCellNodes.put(period, result);
         
     }
     
     /**
      * Get value for header.
      *
      * @param header IStatisticsHeader
      * @param sources List of Numbers
      * @return Object
      */
     private Object getStatValue(IStatisticsHeader header, List<Number> sources){
         if(sources.isEmpty()){
             return null;
         }
         Number firstObj = sources.get(0);
         if(firstObj==null){
             return null;
         }
         StatisticsType type = header.getType();
         Float first = firstObj.floatValue();
         if(type.equals(StatisticsType.AVERAGE)||type.equals(StatisticsType.PERCENT)){
             if(sources.size()<2){
                 return null;
             }
             Number secObj = sources.get(1);
             if(secObj==null){
                 return null;
             }
             Float second = secObj.floatValue();
             if(second.equals(0f)){
                 return null;
             }
             return first/second;
         }
         return first;
     }
     
     /**
      * Get statistics node for current time period.
      *
      * @param parent Node (statistics root)
      * @param period CallTimePeriods
      * @return Node
      */
     private Node getStatisticsNode(Node parent, CallTimePeriods period) {
         Node result = service.createNode();
         
         result.setProperty(INeoConstants.PROPERTY_NAME_NAME, period.getId());
         result.setProperty(INeoConstants.PROPERTY_TYPE_NAME, NodeTypes.CALL_ANALYSIS.getId());
         parent.createRelationshipTo(result, GeoNeoRelationshipTypes.CHILD);
         
         return result;
     }
     
     /**
      * Create statistics root.
      *
      * @return Node.
      */
     private Node createRootStatisticsNode() {
         Node result = service.createNode();
         
         result.setProperty(INeoConstants.PROPERTY_TYPE_NAME, NodeTypes.CALL_ANALYSIS_ROOT.getId());
         result.setProperty(INeoConstants.PROPERTY_NAME_NAME, INeoConstants.CALL_ANALYZIS_ROOT);
         result.setProperty(INeoConstants.PROPERTY_VALUE_NAME, NeoUtils.getNodeName(dataset,service));
         result.setProperty(CallProperties.CALL_TYPE.getId(), StatisticsCallType.AGGREGATION_STATISTICS.toString());
         
         dataset.createRelationshipTo(result, ProbeCallRelationshipType.CALL_ANALYSIS);
         
         return result;
     }
     
 }
