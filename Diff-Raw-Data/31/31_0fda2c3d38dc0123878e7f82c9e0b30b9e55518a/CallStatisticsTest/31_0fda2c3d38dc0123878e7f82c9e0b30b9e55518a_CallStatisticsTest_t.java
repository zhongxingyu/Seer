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
 
 package org.amanzi.awe.views.call.testing;
 
 import java.io.File;
 import java.io.IOException;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 
 import org.amanzi.awe.views.calls.CallTimePeriods;
 import org.amanzi.awe.views.calls.statistics.CallStatistics;
 import org.amanzi.awe.views.calls.statistics.CallStatistics.StatisticsHeaders;
 import org.amanzi.neo.core.INeoConstants;
 import org.amanzi.neo.core.NeoCorePlugin;
 import org.amanzi.neo.core.enums.DriveTypes;
 import org.amanzi.neo.core.enums.GeoNeoRelationshipTypes;
 import org.amanzi.neo.core.enums.NodeTypes;
 import org.amanzi.neo.core.enums.CallProperties.CallType;
 import org.amanzi.neo.core.utils.NeoUtils;
 import org.amanzi.neo.data_generator.AmsDataGenerator;
 import org.amanzi.neo.data_generator.data.CallData;
 import org.amanzi.neo.data_generator.data.CallPair;
 import org.amanzi.neo.data_generator.data.CommandRow;
 import org.amanzi.neo.data_generator.data.ProbeData;
 import org.amanzi.neo.loader.ETSILoader;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.Test;
 import org.neo4j.api.core.Direction;
 import org.neo4j.api.core.EmbeddedNeo;
 import org.neo4j.api.core.Node;
 import org.neo4j.api.core.ReturnableEvaluator;
 import org.neo4j.api.core.StopEvaluator;
 import org.neo4j.api.core.Transaction;
 import org.neo4j.api.core.TraversalPosition;
 import org.neo4j.api.core.Traverser;
 import org.neo4j.api.core.Traverser.Order;
 
 import static org.junit.Assert.*;
 
 /**
  * Class for testing CallStatistics.
  * <p>
  *
  * </p>
  * @author Shcharbatsevich_A
  * @since 1.0.0
  */
 public class CallStatisticsTest {
     
     private static final String DATABASE_NAME = "neo_test";
     private static final String DATA_SAVER_DIR = "neo_call";
     private static final String USER_HOME = "user.home";
     private static final String AMANZI_STR = ".amanzi";
     private static final String MAIN_DIRECTORY = "call_stat_test";
     
     private static final String PATH_SEPERATOR = "\\";
     protected static final String TIMESTAMP_FORMAT = "yyyy-MM-dd HH:mm:ss,SSS";
     protected static final SimpleDateFormat TIME_FORMATTER = new SimpleDateFormat(TIMESTAMP_FORMAT);
     
     private static final int CALL_DURATION_PERIODS_COUNT = 8;
     private static final float[] CALL_DURATION_BORDERS = new float[]{0,1.25f,2.5f,3.75f,5,7.5f,10,12.5f,45,1000};
     
     private static final long MILLISECONDS = 1000;
     private static final long HOUR = 60*60*MILLISECONDS;
     private static final long DAY = 24*HOUR;
     
     private static final String CTSDC_COMMAND = "AT+CTSDC";
     private static final String ATA_COMMAND = "ATA";
     private static final String CTCC_COMMAND = "+CTCC";
     
     private static final String PROBE_NAME_PREFIX = "PROBE";
     
     private static final int ETALON_CELL_COUNT = 13;
     
     private static String mainDirectoryName;
     private static EmbeddedNeo neo;
     
     /**
      * Initialize project service.
      */
     protected static void initProjectService(){
         NeoCorePlugin.getDefault().initProjectService(getNeo());
     }
 
     /**
      * Create new empty main directory instead old one.
      */
     private static void prepareMainDirectory() {
         clearMainDirectory();
         initEmptyMainDirectory();
     }
 
     /**
      * Delete main directory.
      */
     protected static void clearMainDirectory() {
         File dir = new File(getUserHome());
         if(dir.exists() && dir.isDirectory()){
             dir = new File(dir,AMANZI_STR);
             if(dir.exists() && dir.isDirectory()){
                 dir = new File(dir,MAIN_DIRECTORY);
                 if(dir.exists()){
                     if(dir.isDirectory()){
                         clearDirectory(dir);
                     }
                     dir.delete();
                 }
             }
         }
     }
     
     /**
      * Clear directory.
      * @param directory File (for clear)
      */
     protected static void clearDirectory(File directory){
         if(directory.exists()){
             for(File file : directory.listFiles()){
                 if(file.isDirectory()){
                     clearDirectory(file);
                 }
                 file.delete();
             }
         }
     } 
     
     /**
      * Create new main directory.
      */
     protected static void initEmptyMainDirectory(){
         File dir = new File(getUserHome());
         if(!dir.exists()){
             dir.mkdir();
         }
         dir = new File(dir,AMANZI_STR);
         if(!dir.exists()){
             dir.mkdir();    
         }
         dir = new File(dir,MAIN_DIRECTORY);
         if(!dir.exists()){
             dir.mkdir();    
         }
         mainDirectoryName = dir.getPath();
     }
 
     /**
      * Get name of %USER_HOME% directory.
      *
      * @return String
      */
     private static String getUserHome() {
         return System.getProperty(USER_HOME);
     }
     
     /**
      * Gets neo service.
      * @return EmbeddedNeo
      */
     public static EmbeddedNeo getNeo(){
         if (neo == null){
             neo = new EmbeddedNeo(getDbDirectoryName());
         }
         return neo;
     }
     
     /**
      * Get name of data base directory.
      * (Create directory if it not exists)
      *
      * @return String
      */
     private static String getDbDirectoryName(){
         File dir = new File(mainDirectoryName,DATABASE_NAME);
         if(!dir.exists()){
             dir.mkdir();
         }
         return dir.getPath();
     }
     
     /**
      * Prepare operations before execute test.
      */
     @Before
     public void prepareTests(){
         prepareMainDirectory();
         initProjectService();
     }
     
     /**
      * Check statistics by one hour.
      *
      * @throws IOException (problem in data generation)
      * @throws ParseException (problem in gets etalon parameters)
      */
     @Test
     public void testCallStatisicsOneHour()throws IOException, ParseException{
         executeTest(1,5,10,5,6);
     }
     
     /**
      * Check statistics by several hours.
      *
      * @throws IOException (problem in data generation)
      * @throws ParseException (problem in gets etalon parameters)
      */
     @Test
     public void testCallStatisicsSeveralHours()throws IOException, ParseException{
         executeTest(5,0,10,5,6);
     }
 
     /**
      * Check statistics by one day.
      *
      * @throws IOException (problem in data generation)
      * @throws ParseException (problem in gets etalon parameters)
      */
     @Test
     public void testCallStatisicsOneDay()throws IOException, ParseException{
         executeTest(24,3,5,3,6);
     }
     
     /**
      * Check statistics by several days.
      *
      * @throws IOException (problem in data generation)
      * @throws ParseException (problem in gets etalon parameters)
      */
     @Test
     public void testCallStatisicsSeveralDays()throws IOException, ParseException{
         executeTest(48,3,3,2,6);
     }
     
     /**
      * Finish test.
      */
     @After
     public void finish(){
         if(neo!=null){
             neo.shutdown();
             neo = null;
         }
     }
     
     /**
      * Finish all tests.
      */
     @AfterClass
     public static void finishAll(){
         clearMainDirectory();
     }
     
     /**
      * Execute test with different parameters.
      *
      * @param aHours Integer (hours count)
      * @param aCallsPerHour Integer (count of calls per hour)
      * @param aCallPerHourVariance Integer (variance of count of calls per hour)
      * @param aProbes Integer (count of probes)
      * @throws IOException (problem in data generation)
      * @throws ParseException (problem in gets etalon parameters)
      */
     private void executeTest(Integer aHours, Integer aDrift,
             Integer aCallsPerHour, Integer aCallPerHourVariance,
             Integer aProbes)throws IOException, ParseException{
         String dataDir = getDataDirectoryName();
         HashMap<Integer, CallStatData> generated = generateDataFiles(aHours,aDrift,aCallsPerHour,aCallPerHourVariance,aProbes, dataDir);
         Node datasetNode = loadData(dataDir);
         Transaction tx = getNeo().beginTx();
         try {
             CallStatistics statistics = new CallStatistics(datasetNode, getNeo());
             assertResult(generated, statistics, aHours);
         } finally {
             tx.finish();
         }
     }
 
     /**
      * Generate data for gets statistics.
      *
      * @param aHours Integer (hours count)
      * @param aCallsPerHour Integer (count of calls per hour)
      * @param aCallPerHourVariance Integer (variance of count of calls per hour)
      * @param aProbes Integer (count of probes)
      * @param dataDir String (directory for save data)
      * @return HashMap<Integer, CallStatData>
      * @throws IOException (problem in data generation)
      * @throws ParseException (problem in gets etalon parameters)
      */
     private HashMap<Integer, CallStatData> generateDataFiles(Integer aHours, Integer aDrift, 
             Integer aCallsPerHour, Integer aCallPerHourVariance,
             Integer aProbes, String dataDir) throws IOException, ParseException {
         AmsDataGenerator generator = new AmsDataGenerator(dataDir, aHours,aDrift, aCallsPerHour, aCallPerHourVariance, aProbes);
         List<CallPair> generated = generator.generate();
         return buildStatisticsByGenerated(generated);
     }
 
     /**
      * Load generated data.
      *
      * @param dataDir String (directory for get data)
      * @return Node (virtual dataset Node)
      * @throws IOException (problem in data generation)
      */
     private Node loadData(String dataDir) throws IOException {
         ETSILoader loader = new ETSILoader(dataDir, "test", "test network", getNeo());
         loader.setLimit(5000);
         loader.run(new NullProgressMonitor());
         Node datasetNode = loader.getDatasetNode();
         return NeoUtils.findOrCreateVirtualDatasetNode(datasetNode, DriveTypes.AMS_CALLS, getNeo());
     }
     
     /**
      * Assert statistics result.
      *
      * @param generated HashMap<Integer, CallStatData> (data that was generated)
      * @param statistics CallStatistics (for check)
      * @param hours Integer (count of hours)
      */
     private void assertResult(HashMap<Integer, CallStatData> generated,CallStatistics statistics, Integer hours){
         Node hourlyNode = statistics.getPeriodNode(CallTimePeriods.HOURLY, CallType.INDIVIDUAL);
         assertHourlyStatistics(hourlyNode,generated);
         if(hours>=24){
             Node dailyNode = statistics.getPeriodNode(CallTimePeriods.DAILY, CallType.INDIVIDUAL);
             assertDailyStatistics(dailyNode,generated);
         }
     }
    
     /**
      * Assert hourly statistics.
      *
      * @param hourlyNode Node (root node for hourly statistics)
      * @param generated HashMap<Integer, CallStatData> (data that was generated)
      */
     private void assertHourlyStatistics(Node hourlyNode,HashMap<Integer, CallStatData> generated){
         assertFalse("Hourly node does not exists!",hourlyNode == null);
         Traverser traverse = NeoUtils.getChildTraverser(hourlyNode);
         for(Node row : traverse.getAllNodes()){
             assertRow(row, generated, true);
         }
     }
     
     /**
      * Assert statistics row.
      *
      * @param row Node
      * @param generated HashMap<Integer, CallStatData> (data that was generated)
      * @param isHourly boolean (is it hourly statistics?)
      */
     private void assertRow(Node row,HashMap<Integer, CallStatData> generated, boolean isHourly){
         Traverser probeTraverse = row.traverse(Order.DEPTH_FIRST, StopEvaluator.END_OF_GRAPH, new ReturnableEvaluator(){
             @Override
             public boolean isReturnableNode(TraversalPosition currentPos) {
                 Node node = currentPos.currentNode();
                 return NodeTypes.getNodeType(node, neo).equals(NodeTypes.PROBE);
             }
         }, GeoNeoRelationshipTypes.SOURCE, Direction.OUTGOING);
         List<Node> allProbes = new ArrayList<Node>(probeTraverse.getAllNodes());
         int probesCount = allProbes.size();
         assertEquals("Incorrect count of probes linked to s_row.",1,probesCount);
         Node probe = allProbes.get(0);
         Integer prNum = getProbeNumber(probe);
         CallStatData callStatData = generated.get(prNum);
         HashMap<StatisticsHeaders, Long> prData;
         HashMap<StatisticsHeaders, Long> sourceData = null;
         Date rowTime = getRowTime(row);
         if (isHourly) {
             prData = callStatData.getStatisticsByHour(rowTime);
         }
         else{
             prData = callStatData.getStatisticsByDay(rowTime);
             sourceData = buildStatDataByNodes(row);
         }
         assertCells(row, prData, sourceData);
         if(!isHourly){
             assertCellsBySource(row);
         }
     }
     
     /**
      * Assert values in cells.
      *
      * @param row Node
      * @param etalon HashMap<StatisticsHeaders, Long> (values by generated data)
      * @param source HashMap<StatisticsHeaders, Long> (values by source data, null if statistics is hourly)
      */
     private void assertCells(Node row, HashMap<StatisticsHeaders, Long> etalon, HashMap<StatisticsHeaders, Long> source){
         HashMap<StatisticsHeaders, Long> cells = buildCellDataMap(row);
         int cellCount = cells.size();
         assertEquals("Wrong cell count.",ETALON_CELL_COUNT, cellCount);
         assertCellValue(etalon, source, cells, StatisticsHeaders.CALL_ATTEMPT_COUNT);
         assertCellValue(etalon, source, cells, StatisticsHeaders.SUCC_SETUP_COUNT);
         assertCellValue(etalon, source, cells, StatisticsHeaders.SETUP_TM_Z1_P1);
         assertCellValue(etalon, source, cells, StatisticsHeaders.SETUP_TM_Z1_P2);
         assertCellValue(etalon, source, cells, StatisticsHeaders.SETUP_TM_Z1_P3);
         assertCellValue(etalon, source, cells, StatisticsHeaders.SETUP_TM_Z1_P4);
         assertCellValue(etalon, source, cells, StatisticsHeaders.SETUP_TM_Z1_L1);
         assertCellValue(etalon, source, cells, StatisticsHeaders.SETUP_TM_Z1_L2);
         assertCellValue(etalon, source, cells, StatisticsHeaders.SETUP_TM_Z1_L3);
         assertCellValue(etalon, source, cells, StatisticsHeaders.SETUP_TM_Z1_L4);
         assertCellValue(etalon, source, cells, StatisticsHeaders.SETUP_TIME_MIN);
         assertCellValue(etalon, source, cells, StatisticsHeaders.SETUP_TIME_MAX);
         assertCellValue(etalon, source, cells, StatisticsHeaders.SETUP_TOTAL_DUR);
     }
     
     /**
      * Check cell value by source cells.
      * (Not used in hourly assert)
      *
      * @param aCell Node
      */
     private void assertCellsBySource(Node aCell){
         Traverser cellTraverse = NeoUtils.getChildTraverser(aCell);
         for(Node cell : cellTraverse.getAllNodes()){
             Traverser traverse = cell.traverse(Order.DEPTH_FIRST, StopEvaluator.END_OF_GRAPH, new ReturnableEvaluator(){
                 @Override
                 public boolean isReturnableNode(TraversalPosition currentPos) {
                     Node node = currentPos.currentNode();
                     return currentPos.depth()>0&&NodeTypes.getNodeType(node, neo).equals(NodeTypes.S_CELL);
                 }            
             }, GeoNeoRelationshipTypes.SOURCE,Direction.OUTGOING);
             StatisticsHeaders header = StatisticsHeaders.getHeaderByTitle((String)cell.getProperty(INeoConstants.PROPERTY_NAME_NAME));
             Long etalon = getCellsValue(traverse.getAllNodes(),header);
             Long value = getCellValue(cell, header);
             assertEquals("Value in daily cell "+header+" is not conform to source.", etalon, value);
         }
     }
     
     /**
      * Gets values from source cells.
      *
      * @param cells Collection of Nodes.
      * @param header cell header.
      * @return Long.
      */
     private Long getCellsValue(Collection<Node> cells, StatisticsHeaders header){
         Long result = null;
         for(Node cell : cells){
             Long value = getCellValue(cell, header);
             if(result == null){
                 result = value;
                 continue;
             }
             if(header.equals(StatisticsHeaders.SETUP_TIME_MIN)){
                 if(value<result){
                     result = value;
                 }
                 continue;
             }
             if(header.equals(StatisticsHeaders.SETUP_TIME_MAX)){
                 if(value>result){
                     result = value;
                 }
                 continue;
             }
             result+=value;
         }
        return result==null?0L:result;
     }
 
     /**
      * Check cell value
      *
      * @param etalon HashMap<StatisticsHeaders, Long> (values by generated data)
      * @param source HashMap<StatisticsHeaders, Long> (values by source data, null if statistics is hourly)
      * @param cells HashMap<StatisticsHeaders, Long> (real values)
      * @param cellType StatisticsHeaders (cell header)
      */
     private void assertCellValue(HashMap<StatisticsHeaders, Long> etalon, HashMap<StatisticsHeaders, Long> source,
             HashMap<StatisticsHeaders, Long> cells, StatisticsHeaders cellType) {
         Long assertionValue = cells.get(cellType);
         assertFalse("Cell "+cellType+" not found.",assertionValue == null);
         assertEquals("Wrong value in cell "+cellType+".", etalon.get(cellType), assertionValue);
         if(source!=null){
             assertEquals("Wrong value in cell "+cellType+" by sources.", source.get(cellType), assertionValue);
         }
     }
     
     /**
      * Build statistics by source nodes.
      *
      * @param row Node (source row)
      * @return HashMap<StatisticsHeaders, Long>
      */
     private HashMap<StatisticsHeaders, Long> buildStatDataByNodes(Node row){
         HashMap<StatisticsHeaders, Long> result = new HashMap<StatisticsHeaders, Long>(ETALON_CELL_COUNT);
         Traverser traverse = row.traverse(Order.DEPTH_FIRST, StopEvaluator.END_OF_GRAPH, new ReturnableEvaluator(){
             @Override
             public boolean isReturnableNode(TraversalPosition currentPos) {
                 Node node = currentPos.currentNode();
                 return currentPos.depth()>0&&NodeTypes.getNodeType(node, neo).equals(NodeTypes.S_ROW);
             }            
         }, GeoNeoRelationshipTypes.SOURCE,Direction.OUTGOING);
        Collection<Node> allNodes = traverse.getAllNodes();
        assertFalse("Daily row has no sourses!",allNodes.isEmpty());
        for(Node sRow : allNodes){
            HashMap<StatisticsHeaders, Long> cellMap = buildCellDataMap(sRow);
            for(StatisticsHeaders header : StatisticsHeaders.values()){
                Long currValue = cellMap.get(header);
                if(currValue == null){
                    continue;
                }
                Long resValue = result.get(header);
                if(resValue == null){
                    result.put(header, currValue);
                    continue;
                }
                if(header.equals(StatisticsHeaders.SETUP_TIME_MIN)){
                   if(resValue==0||(currValue>0&&currValue<resValue)){                       
                        result.put(header, currValue);                       
                    }
                    continue;
                }
                if(header.equals(StatisticsHeaders.SETUP_TIME_MAX)){
                    if(currValue>resValue){
                        result.put(header, currValue);                       
                    }
                    continue;
                }
                result.put(header, resValue+currValue);
            }
         }
         return result;
     }
     
     /**
      * Gets row values.
      *
      * @param row Node
      * @return HashMap<StatisticsHeaders, Long>
      */
     private HashMap<StatisticsHeaders, Long> buildCellDataMap(Node row){
         HashMap<StatisticsHeaders, Long> result = new HashMap<StatisticsHeaders, Long>();
         Traverser traverse = NeoUtils.getChildTraverser(row);
         for(Node cell : traverse.getAllNodes()){
             StatisticsHeaders header = StatisticsHeaders.getHeaderByTitle((String)cell.getProperty(INeoConstants.PROPERTY_NAME_NAME));
             Long value = getCellValue(cell, header);
             result.put(header, value);
         }
         return result;
     }
     
     /**
      * Gets value from cell by cell type.
      *
      * @param cell Node
      * @param header StatisticsHeaders
      * @return Long
      */
     private Long getCellValue(Node cell, StatisticsHeaders header){
         if(header.equals(StatisticsHeaders.SETUP_TIME_MAX)
                 ||header.equals(StatisticsHeaders.SETUP_TIME_MIN)
                 ||header.equals(StatisticsHeaders.SETUP_TOTAL_DUR)){
             Float value = MILLISECONDS*(Float)cell.getProperty(INeoConstants.PROPERTY_VALUE_NAME);
             return Math.round(value.doubleValue());
         }
         Integer value = (Integer)cell.getProperty(INeoConstants.PROPERTY_VALUE_NAME);
         return value.longValue();
     }
     
     /**
      * Assert daily statistics.
      *
      * @param dailyNode Node (root node for daily statistics)
      * @param generated HashMap<Integer, CallStatData> (data that was generated)
      */
     private void assertDailyStatistics(Node dailyNode,HashMap<Integer, CallStatData> generated){
         assertFalse("Daily node does not exists!",dailyNode == null);
         Traverser hourlyTraverser = dailyNode.traverse(Order.DEPTH_FIRST, StopEvaluator.END_OF_GRAPH, new ReturnableEvaluator(){
             @Override
             public boolean isReturnableNode(TraversalPosition currentPos) {
                 Node node = currentPos.currentNode();
                 return NeoUtils.getNodeName(node,getNeo()).equalsIgnoreCase("hourly");
             }
         }, GeoNeoRelationshipTypes.SOURCE, Direction.OUTGOING);
         List<Node> allHourly = new ArrayList<Node>(hourlyTraverser.getAllNodes());
         int hourlyCount = allHourly.size();
         assertEquals("Incorrect count of hourly nodes linked to daily.",1,hourlyCount);
         Traverser traverse = NeoUtils.getChildTraverser(dailyNode);
         for(Node row : traverse.getAllNodes()){
             assertRow(row, generated, false);
         }
     }
     
     /**
      * Returns name of directory for save generated data.
      *
      * @return String
      */
     private String getDataDirectoryName(){
         return mainDirectoryName+PATH_SEPERATOR+DATA_SAVER_DIR;
     }
     
     /**
      * Returns probe number.
      *
      * @param probe Node
      * @return Integer
      */
     private Integer getProbeNumber(Node probe){
         String prName = (String)probe.getProperty(INeoConstants.PROPERTY_NAME_NAME);
         String numString  = prName.substring(PROBE_NAME_PREFIX.length());
         return Integer.parseInt(numString);
     }
     
     /**
      * Returns time of statistics row.
      *
      * @param row Node
      * @return Date
      */
     private Date getRowTime(Node row){
         Long timestamp = (Long)row.getProperty(INeoConstants.PROPERTY_TIME_NAME);
         return new Date(timestamp);
     }
   
     /**
      * Build statistics by generated data.
      *
      * @param generated List of CallPairs
      * @return HashMap<Integer, CallStatData>
      * @throws ParseException (problem in gets parameters)
      */
     private HashMap<Integer, CallStatData> buildStatisticsByGenerated(List<CallPair> generated)throws ParseException{
         HashMap<Integer, CallStatData> result = new HashMap<Integer, CallStatData>();
         for(CallPair pair : generated){
             Integer source = pair.getFirstProbe();
             CallStatData data = result.get(source);
             if(data == null){
                 data = new CallStatData();
                 result.put(source, data);
             }
             for(CallData call : pair.getData()){
                 Date start = getCallStartTime(call.getFirstProbe());
                 Long duration = getCallDuration(call.getSecondProbe(), start);
                 Integer periode = getDurationPeriod(duration);
                 data.addCall(periode, start, duration);
             }
         }
         return result;
     }
     
     /**
      * Gets time of call starts.
      *
      * @param data ProbeData
      * @return Date
      */
     private Date getCallStartTime(ProbeData data){
         for(CommandRow row : data.getCommands()){
             if(row.getCommand().equalsIgnoreCase(CTSDC_COMMAND)){
                 return row.getTime();
             }
         }
         return null;
     }
     
     /**
      * Gets call duration.
      *
      * @param data ProbeData
      * @param start Date (call start)
      * @return
      * @throws ParseException (problem in gets parameters)
      */
     private Long getCallDuration(ProbeData data, Date start)throws ParseException{
         for(CommandRow row : data.getCommands()){
             if(row.getCommand().equalsIgnoreCase(CTCC_COMMAND)){
                 return row.getTime().getTime()-start.getTime();
             }
             if(row.getCommand().equalsIgnoreCase(ATA_COMMAND)){
                 for(Object add : row.getAdditional()){
                     if(add instanceof String){
                         String str = (String)add;
                         if(str.contains(CTCC_COMMAND)){
                            String timeStr = str.substring(1, str.indexOf(CTCC_COMMAND)-1); 
                            Date end = TIME_FORMATTER.parse(timeStr);
                            return end.getTime()-start.getTime();
                         }
                     }
                 }
             }
         }
         return null;
     }
     
     /**
      * Gets number of call duration period.
      *
      * @param duration Long
      * @return Integer
      */
     private Integer getDurationPeriod(Long duration){
         for(int i=0; i<CALL_DURATION_PERIODS_COUNT; i++){
             float start = MILLISECONDS * CALL_DURATION_BORDERS[i];
             float end = MILLISECONDS * CALL_DURATION_BORDERS[i+1];
             if(start<=duration && duration<end){
                 return i;
             }
         }
         return CALL_DURATION_PERIODS_COUNT;
     }
     
     /**
      * For saving call information builded by generated data.
      * <p>
      *
      * </p>
      * @author Shcharbatsevich_A
      * @since 1.0.0
      */
     protected class CallStatData{
         
         /**
          * All calls:
          * Key: period number, Value: Map - key: Start time, value: duration.
          */
         private HashMap<Integer, HashMap<Date, Long>> data = new HashMap<Integer, HashMap<Date,Long>>();
         
         /**
          * Get all calls by duration period.
          *
          * @param periode Integer (call duration period number)
          * @param calls HashMap<Integer, HashMap<Date, Long>>
          * @return HashMap<Date, Long>
          */
         private HashMap<Date, Long> getAllCallsFrom(Integer periode,HashMap<Integer, HashMap<Date, Long>> calls){
             return calls.get(periode);
         }
         
         /**
          * Returns calls count.
          *
          * @param calls HashMap<Integer, HashMap<Date, Long>>
          * @return Integer.
          */
         private Integer getCallCount(HashMap<Integer, HashMap<Date, Long>> calls){
             int result = 0;
             for(int i=0;i<=CALL_DURATION_PERIODS_COUNT;i++){
                 result+=getCallCount(i,calls);
             }
             return result;
         }
         
         /**
          * Returns setup calls count.
          *
          * @param calls HashMap<Integer, HashMap<Date, Long>>
          * @return Integer.
          */
         private Integer getSetupCount(HashMap<Integer, HashMap<Date, Long>> calls){
             int result = 0;
             for(int i=0;i<CALL_DURATION_PERIODS_COUNT;i++){
                 result+=getCallCount(i,calls);
             }
             return result;
         }
         
         /**
          * Returns calls count in period.
          *
          * @param periode Integer
          * @param calls HashMap<Integer, HashMap<Date, Long>>
          * @return Integer
          */
         private Integer getCallCount(Integer periode,HashMap<Integer, HashMap<Date, Long>> calls){
             HashMap<Date, Long> allCallsFrom = getAllCallsFrom(periode,calls);
             return allCallsFrom==null?0:allCallsFrom.size();
         }
         
         /**
          * Add new call to data.
          *
          * @param periode Integer call duration period number
          * @param start Date call start time
          * @param duration Long call duration
          */
         public void addCall(Integer periode, Date start, Long duration){
             HashMap<Date, Long> calls = getAllCallsFrom(periode,data);
             if(calls == null){
                 calls = new HashMap<Date, Long>();
                 data.put(periode, calls);
             }
             calls.put(start, duration);
         }
         
         /**
          * Build call statistics by hour.
          *
          * @param start Date start time 
          * @return HashMap<StatisticsHeaders, Long>
          */
         public HashMap<StatisticsHeaders, Long> getStatisticsByHour(Date start){
             return buildStatistics(start, new Date(start.getTime()+HOUR)); 
         }
         
         /**
          * Build call statistics by day.
          *
          * @param start Date start time 
          * @return HashMap<StatisticsHeaders, Long>
          */
         public HashMap<StatisticsHeaders, Long> getStatisticsByDay(Date start){
             return buildStatistics(start, new Date(start.getTime()+DAY));
         }
         
         /**
          * Build call statistics by time period.
          *
          * @param start Date
          * @param end Date
          * @return HashMap<StatisticsHeaders, Long>
          */
         private HashMap<StatisticsHeaders, Long> buildStatistics(Date start, Date end){
             HashMap<StatisticsHeaders, Long> result = new HashMap<StatisticsHeaders, Long>();
             HashMap<Integer, HashMap<Date, Long>> calls = getCallsInPeriod(start, end);
             result.put(StatisticsHeaders.CALL_ATTEMPT_COUNT, getCallCount(calls).longValue());
             result.put(StatisticsHeaders.SUCC_SETUP_COUNT, getSetupCount(calls).longValue());
             result.put(StatisticsHeaders.SETUP_TM_Z1_P1, getCallCount(0,calls).longValue());
             result.put(StatisticsHeaders.SETUP_TM_Z1_P2, getCallCount(1,calls).longValue());
             result.put(StatisticsHeaders.SETUP_TM_Z1_P3, getCallCount(2,calls).longValue());
             result.put(StatisticsHeaders.SETUP_TM_Z1_P4, getCallCount(3,calls).longValue());
             result.put(StatisticsHeaders.SETUP_TM_Z1_L1, getCallCount(4,calls).longValue());
             result.put(StatisticsHeaders.SETUP_TM_Z1_L2, getCallCount(5,calls).longValue());
             result.put(StatisticsHeaders.SETUP_TM_Z1_L3, getCallCount(6,calls).longValue());
             result.put(StatisticsHeaders.SETUP_TM_Z1_L4, getCallCount(7,calls).longValue());
             result.put(StatisticsHeaders.SETUP_TIME_MIN, getMinTime(calls));
             result.put(StatisticsHeaders.SETUP_TIME_MAX, getMaxTime(calls));
             result.put(StatisticsHeaders.SETUP_TOTAL_DUR, getTotalTime(calls));
             return result;
         }
         
         /**
          * Gets minimal duration time in calls.
          *
          * @param calls HashMap<Integer, HashMap<Date, Long>>
          * @return Long
          */
         private Long getMinTime(HashMap<Integer, HashMap<Date, Long>> calls){
             for(int i=0; i<CALL_DURATION_PERIODS_COUNT; i++){
                 HashMap<Date, Long> callsInPeriode = getAllCallsFrom(i, calls);
                 if (callsInPeriode!=null && !callsInPeriode.isEmpty()) {
                     List<Long> times = new ArrayList<Long>(callsInPeriode.values());
                     Collections.sort(times);
                     return times.get(0);
                 }
             }
             return 0L;
         }
         
         /**
          * Gets maximal duration time in calls.
          *
          * @param calls HashMap<Integer, HashMap<Date, Long>>
          * @return Long
          */
         private Long getMaxTime(HashMap<Integer, HashMap<Date, Long>> calls){
             for(int i=CALL_DURATION_PERIODS_COUNT-1; i>=0; i--){
                 HashMap<Date, Long> callsInPeriode = getAllCallsFrom(i, calls);
                 if (callsInPeriode!=null && !callsInPeriode.isEmpty()) {
                     List<Long> times = new ArrayList<Long>(callsInPeriode.values());
                     Collections.sort(times);
                     return times.get(times.size()-1);
                 }
             }
             return 0L;
         }
         
         /**
          * Gets total duration time in calls.
          *
          * @param calls HashMap<Integer, HashMap<Date, Long>>
          * @return Long
          */
         private Long getTotalTime(HashMap<Integer, HashMap<Date, Long>> calls){
             Float result = 0.0f;
             for(int i=0;i<CALL_DURATION_PERIODS_COUNT;i++){
                 HashMap<Date, Long> callsInPeriode = getAllCallsFrom(i, calls);
                 if (callsInPeriode!=null) {
                     for (Long time : callsInPeriode.values()) {
                         result += (float)time/MILLISECONDS; //to solve rounding problem
                     }
                 }
             }
             result = result*MILLISECONDS;
             return Math.round(result.doubleValue());
         }
         
         /**
          * Gets all calls in time period.
          *
          * @param start Date
          * @param end Date
          * @return HashMap<Integer, HashMap<Date, Long>>
          */
         private HashMap<Integer, HashMap<Date, Long>> getCallsInPeriod(Date start, Date end){
             HashMap<Integer, HashMap<Date,Long>> result = new HashMap<Integer, HashMap<Date,Long>>();
             for(int i=0;i<=CALL_DURATION_PERIODS_COUNT;i++){
                 HashMap<Date, Long> callsInPeriode = getAllCallsFrom(i, data);
                 if (callsInPeriode!=null) {
                     for (Date date : callsInPeriode.keySet()) {
                         if ((start.before(date)||start.equals(date)) && date.before(end)) {
                             HashMap<Date, Long> found = result.get(i);
                             if (found == null) {
                                 found = new HashMap<Date, Long>();
                                 result.put(i, found);
                             }
                             found.put(date, callsInPeriode.get(date));
                         }
                     }
                 }
             }
             return result;
         }
     }
 }
