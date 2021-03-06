 package com.yahoo.hadoop_bsp;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.DataInputStream;
 import java.io.DataOutput;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeMap;
 import java.util.TreeSet;
 
 import org.apache.commons.codec.binary.Base64;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import org.apache.log4j.Logger;
 import org.apache.hadoop.fs.FSDataOutputStream;
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.io.Writable;
 import org.apache.hadoop.io.WritableComparable;
 
 import org.apache.hadoop.mapreduce.InputSplit;
 import org.apache.hadoop.mapreduce.Mapper.Context;
 import org.apache.hadoop.util.ReflectionUtils;
 
 import org.apache.zookeeper.KeeperException;
 import org.apache.zookeeper.CreateMode;
 import org.apache.zookeeper.ZooDefs.Ids;
 import org.apache.zookeeper.data.Stat;
 
 /**
  * ZooKeeper-based implementation of {@link CentralizedServiceWorker}.
  * @author aching
  */
 @SuppressWarnings("rawtypes")
 public class BspServiceWorker<
     I extends WritableComparable, V extends Writable, E extends Writable,
     M extends Writable>
     extends BspService<I, V, E, M> implements
     CentralizedServiceWorker<I, V, E, M> {
     /** Number of input splits */
     private int m_inputSplitCount = -1;
     /** Cached aggregate number of vertices in the entire application */
     long m_totalVertices = -1;
     /** My process health znode */
     private String m_myHealthZnode;
     /** Partition to compare with (saved local variable to reduce allocation) */
     private VertexRange<I> compareVertexRange =
         new VertexRange<I>("", -1, "", null, -1, -1, null);
     /** Data structure or storing each range and associated vertices */
     private Map<I, List<Vertex<I, V, E, M>>> m_maxIndexVertexMap =
         new TreeMap<I, List<Vertex<I, V, E, M>>>();
     /** Map of aggregators */
     private static Map<String, Aggregator<Writable>> m_aggregatorMap =
         new TreeMap<String, Aggregator<Writable>>();
     /** List of aggregators currently in use */
     private static Set<String> m_aggregatorInUse = new TreeSet<String>();
     /** Class logger */
     private static final Logger LOG = Logger.getLogger(BspServiceWorker.class);
 
 
     public BspServiceWorker(String serverPortList,
                             int sessionMsecTimeout,
                             Context context,
                             BspJob.BspMapper<I, V, E, M> bspMapper) {
         super(serverPortList, sessionMsecTimeout, context, bspMapper);
     }
 
     /**
      * Intended to check the health of the node.  For instance, can it ssh,
      * dmesg, etc. For now, does nothing.
      */
     public boolean isHealthy() {
         return true;
     }
 
     /**
      * Try to reserve an InputSplit for loading.  While InputSplits exists that
      * are not finished, wait until they are.
      *
      * @return reserved InputSplit or null if no unfinished InputSplits exist
      */
     private String reserveInputSplit() {
         List<String> inputSplitPathList = null;
         try {
             inputSplitPathList =
                 getZkExt().getChildrenExt(INPUT_SPLIT_PATH, false, false, true);
             if (m_inputSplitCount == -1) {
                 m_inputSplitCount = inputSplitPathList.size();
             }
         } catch (Exception e) {
             throw new RuntimeException(e);
         }
         String reservedInputSplitPath = null;
         Stat reservedStat = null;
         while (true) {
             int finishedInputSplits = 0;
             for (String inputSplitPath : inputSplitPathList) {
                 String tmpInputSplitFinishedPath =
                     inputSplitPath + INPUT_SPLIT_FINISHED_NODE;
                 try {
                     reservedStat =
                         getZkExt().exists(tmpInputSplitFinishedPath, true);
                 } catch (Exception e) {
                     throw new RuntimeException(e);
                 }
                 if (reservedStat != null) {
                     ++finishedInputSplits;
                     continue;
                 }
 
                 String tmpInputSplitReservedPath =
                     inputSplitPath + INPUT_SPLIT_RESERVED_NODE;
                 try {
                     reservedStat =
                         getZkExt().exists(tmpInputSplitReservedPath, true);
                 } catch (Exception e) {
                     throw new RuntimeException(e);
                 }
                 if (reservedStat == null) {
                     try {
                         // Attempt to reserve this InputSplit
                         getZkExt().createExt(tmpInputSplitReservedPath,
                                        null,
                                        Ids.OPEN_ACL_UNSAFE,
                                        CreateMode.EPHEMERAL,
                                        false);
                         reservedInputSplitPath = inputSplitPath;
                         LOG.info("reserveInputSplit: Reserved input split " +
                                  "path " + reservedInputSplitPath);
                         return reservedInputSplitPath;
                     } catch (KeeperException.NodeExistsException e) {
                         LOG.info("reserveInputSplit: Couldn't reserve (already " +
                                  "reserved) inputSplit" +
                                  " at " + tmpInputSplitReservedPath);
                     } catch (Exception e) {
                         throw new RuntimeException(e);
                     }
                 }
             }
             LOG.info("reserveInputSplit: reservedPath = " +
                      reservedInputSplitPath + ", " + finishedInputSplits +
                      " of " + inputSplitPathList.size() +
                      " InputSplits are finished.");
             if (finishedInputSplits == inputSplitPathList.size()) {
                 return null;
             }
             // Wait for either a reservation to go away or a notification that
             // an InputSplit has finished.
             getInputSplitsStateChangedEvent().waitForever();
             getInputSplitsStateChangedEvent().reset();
         }
     }
 
     /**
      * Each worker will set the partitions that it has found for a given
      * InputSplit. After this, the InputSplit is considered finished.
      *
      * @param inputSplitPath path to the input split znode
      * @param maxIndexStatMap maps max vertex indexes to a list countaing
      *        the number of vertices (index 0) and the number of edges (index 1)
      *        in each partition (can be null, where nothing is written)
      */
     private void setInputSplitVertexRanges(
         String inputSplitPath,
         Map<I, List<Long>> maxIndexStatMap) {
         String inputSplitFinishedPath =
             inputSplitPath + INPUT_SPLIT_FINISHED_NODE;
         byte [] zkData = null;
         JSONArray statArray = new JSONArray();
         if (maxIndexStatMap != null) {
             for (Map.Entry<I, List<Long>> entry : maxIndexStatMap.entrySet()) {
                 try {
                     ByteArrayOutputStream outputStream =
                         new ByteArrayOutputStream();
                     DataOutput output = new DataOutputStream(outputStream);
                     ((Writable) entry.getKey()).write(output);
 
                     JSONObject vertexRangeObj = new JSONObject();
                     vertexRangeObj.put(JSONOBJ_NUM_VERTICES_KEY,
                                        entry.getValue().get(0));
                     vertexRangeObj.put(JSONOBJ_NUM_EDGES_KEY,
                                        entry.getValue().get(1));
                     vertexRangeObj.put(JSONOBJ_HOSTNAME_ID_KEY,
                                        getHostnamePartitionId());
                     vertexRangeObj.put(JSONOBJ_MAX_VERTEX_INDEX_KEY,
                                        outputStream.toString("UTF-8"));
                     statArray.put(vertexRangeObj);
                     LOG.info("setInputSplitVertexRanges: " +
                              "Trying to add vertexRangeObj " +
                              vertexRangeObj + " to InputSplit path " +
                              inputSplitPath);
                 } catch (Exception e) {
                     throw new RuntimeException(e);
                 }
             }
             zkData = statArray.toString().getBytes();
         }
         try {
             getZkExt().createExt(inputSplitFinishedPath,
                                  zkData,
                                  Ids.OPEN_ACL_UNSAFE,
                                  CreateMode.PERSISTENT,
                                  true);
         } catch (KeeperException.NodeExistsException e) {
             LOG.warn("setLocalVertexRanges: " + inputSplitFinishedPath +
                      " already exists!");
         } catch (Exception e) {
             throw new RuntimeException(e);
         }
         LOG.info("setInputSplitVertexRanges: Finished loading " +
                  inputSplitPath + " with vertexRanges - " + statArray);
     }
 
     /**
      * Load the vertices from the user-defined VertexReader into our partitions
      * of vertex ranges.  Do this until all the InputSplits have been processed.
      * All workers will try to do as many InputSplits as they can.  The master
      * will monitor progress and stop this once all the InputSplits have been
      * loaded and check-pointed.  The InputSplits must be sorted.
      */
     private void loadVertices() {
         List<HadoopVertex<I, V, E, M>> vertexList =
             new ArrayList<HadoopVertex<I, V, E, M>>();
         String inputSplitPath = null;
         while ((inputSplitPath = reserveInputSplit()) != null) {
             @SuppressWarnings("unchecked")
             Class<? extends Writable> inputSplitClass =
                 (Class<Writable>) getConfiguration().getClass(
                     BspJob.BSP_INPUT_SPLIT_CLASS,
                     InputSplit.class);
             InputSplit inputSplit = (InputSplit)
                 ReflectionUtils.newInstance(inputSplitClass, getConfiguration());
             byte[] splitList;
             try {
                 splitList = getZkExt().getData(inputSplitPath, false, null);
             } catch (Exception e) {
                 throw new RuntimeException(e);
             }
             LOG.info("loadVertices: Reserved " + inputSplitPath +
                      " and got '" + splitList + "'");
             getContext().progress();
 
             InputStream input =
                 new ByteArrayInputStream(splitList);
             try {
                 ((Writable) inputSplit).readFields(new DataInputStream(input));
             } catch (IOException e) {
                 throw new RuntimeException(e);
             }
 
             @SuppressWarnings("unchecked")
             Class<? extends VertexInputFormat<I, V, E>> vertexInputFormatClass =
                 (Class<? extends VertexInputFormat<I, V, E>>)
                     getConfiguration().getClass(
                         BspJob.BSP_VERTEX_INPUT_FORMAT_CLASS,
                         VertexInputFormat.class);
             VertexInputFormat<I, V, E> vertexInputFormat = null;
             VertexReader<I, V, E> vertexReader = null;
             try {
                 vertexInputFormat = vertexInputFormatClass.newInstance();
                 vertexReader =
                     vertexInputFormat.createVertexReader(inputSplit, getContext());
                 vertexReader.initialize(inputSplit, getContext());
             } catch (Exception e) {
                 throw new RuntimeException(e);
             }
 
             vertexList.clear();
             try {
                 HadoopVertex<I, V, E, M> vertex =
                     getHadoopVertexClass().newInstance();
                 while (vertexReader.next(vertex)) {
                     vertex.setBspMapper(getBspMapper());
                     if (vertex.getVertexValue() == null) {
                         vertex.setVertexValue(createVertexValue());
                     }
                     vertexList.add(vertex);
                     vertex = getHadoopVertexClass().newInstance();
                     getContext().progress();
                 }
                 vertexReader.close();
             } catch (Exception e) {
                 throw new RuntimeException(e);
             }
             if (vertexList.isEmpty()) {
                 LOG.info("loadVertices: No vertices in input split " +
                          inputSplit);
                 // TODO: Need to add checkpoints
                 setInputSplitVertexRanges(inputSplitPath, null);
                 continue;
             }
 
             // Separate all the vertices in this InputSplit into vertex ranges.
             // The number of vertex ranges is up to half of the number of
             // available workers and must reach a minimum size.  Then two passes
             // over the vertexList.  First, find the maximum vertex ranges.
             // Then fill them in.
             long partitionsPerInputSplit = (long) (m_inputSplitCount *
                 getConfiguration().getFloat(
                     BspJob.BSP_TOTAL_INPUT_SPLIT_MULTIPLIER,
                     BspJob.DEFAULT_BSP_TOTAL_INPUT_SPLIT_MULTIPLIER));
             if (partitionsPerInputSplit == 0) {
                 partitionsPerInputSplit = 1;
             }
             long vertexRangeSize = vertexList.size() / partitionsPerInputSplit;
             long minPerVertexRange =
                 getConfiguration().getLong(
                     BspJob.BSP_MIN_VERTICES_PER_RANGE,
                     BspJob.DEFAULT_BSP_MIN_VERTICES_PER_RANGE);
             if (vertexRangeSize < minPerVertexRange) {
                 vertexRangeSize = minPerVertexRange;
             }
             I vertexIdMax = null;
             for (int i = 0; i < vertexList.size(); ++i) {
                 if ((vertexIdMax != null) && ((i % vertexRangeSize) == 0)) {
                     m_maxIndexVertexMap.put(
                         vertexIdMax,
                         new ArrayList<Vertex<I, V, E, M>>());
                     vertexIdMax = null;
                 }
 
                 if (vertexIdMax == null) {
                     vertexIdMax = vertexList.get(i).getVertexId();
                 } else {
                     @SuppressWarnings("unchecked")
                     int compareTo =
                         vertexList.get(i).getVertexId().compareTo(vertexIdMax);
                     if (compareTo > 0) {
                         vertexIdMax = vertexList.get(i).getVertexId();
                     }
                 }
             }
             if (vertexIdMax == null) {
                 throw new RuntimeException("loadVertices: Encountered " +
                                            "impossible null vertexIdMax.");
             }
             m_maxIndexVertexMap.put(vertexIdMax,
                                     new ArrayList<Vertex<I,V,E,M>>());
 
             Iterator<I> maxIndexVertexMapIt =
                 m_maxIndexVertexMap.keySet().iterator();
             I currentMaxIndex = maxIndexVertexMapIt.next();
             for (HadoopVertex<I, V, E, M> vertex : vertexList) {
                 @SuppressWarnings("unchecked")
                 int compareTo =
                     vertex.getVertexId().compareTo(currentMaxIndex);
                 if (compareTo > 0) {
                     if (!maxIndexVertexMapIt.hasNext()) {
                         throw new RuntimeException(
                             "loadVertices: Impossible that vertex " +
                             vertex.getVertexId() + " > " + currentMaxIndex);
                     }
                     currentMaxIndex = maxIndexVertexMapIt.next();
                 }
                 LOG.debug("loadVertices: Adding vertex with index = " +
                           vertex.getVertexId() + " to vertex range max = " +
                           currentMaxIndex);
                 m_maxIndexVertexMap.get(currentMaxIndex).add(vertex);
             }
             Map<I, List<Long>> maxIndexStatMap = new TreeMap<I, List<Long>>();
             for (Map.Entry<I, List<Vertex<I, V, E, M>>> entry :
                 m_maxIndexVertexMap.entrySet()) {
                 List<Long> statList = new ArrayList<Long>();
                 long vertexRangeEdgeCount = 0;
                 for (Vertex<I, V, E, M> vertex : entry.getValue()) {
                     vertexRangeEdgeCount += vertex.getOutEdgeIterator().size();
                 }
                 statList.add(new Long(entry.getValue().size()));
                 statList.add(new Long(vertexRangeEdgeCount));
                 maxIndexStatMap.put(entry.getKey(), statList);
             }
             setInputSplitVertexRanges(inputSplitPath, maxIndexStatMap);
         }
     }
 
     public void setup() {
         // Unless doing a restart, prepare for computation:
         // 1. Start superstep 0 (no computation)
         // 2. Wait for the INPUT_SPLIT_READY_PATH node has been created
         // 3. Process input splits until there are no more.
         // 4. Wait for superstep 0 to complete.
         if (getManualRestartSuperstep() < -1) {
             throw new RuntimeException(
                 "setup: Invalid superstep to restart - " +
                 getManualRestartSuperstep());
         }
         else if (getManualRestartSuperstep() > 0) {
             setCachedSuperstep(getManualRestartSuperstep());
             return;
         }
 
         startSuperstep();
 
         // Ensure the InputSplits are ready for processing before processing
         while (true) {
             Stat inputSplitsReadyStat;
             try {
                 inputSplitsReadyStat =
                     getZkExt().exists(INPUT_SPLITS_ALL_READY_PATH, true);
             } catch (Exception e) {
                 throw new RuntimeException(e);
             }
             if (inputSplitsReadyStat != null) {
                 break;
             }
             getInputSplitsAllReadyEvent().waitForever();
             getInputSplitsAllReadyEvent().reset();
         }
 
         getContext().progress();
         loadVertices();
 
         Map<I, long []> maxIndexStatsMap = new TreeMap<I, long []>();
         for (Map.Entry<I, List<Vertex<I, V, E, M>>> entry :
              getMaxIndexVertexLists().entrySet()) {
             long [] statArray = new long[2];
             statArray[0] = 0;
             statArray[1] = entry.getValue().size();
             maxIndexStatsMap.put(entry.getKey(), statArray);
         }
 
         finishSuperstep(maxIndexStatsMap);
     }
 
     /**
      *  Marshal the aggregator values of to a JSONArray that will later be
      *  aggregated by master.  Reset the 'use' of aggregators in the next
      *  superstep
      *
      * @param superstep
      */
     private JSONArray marshalAggregatorValues(long superstep) {
         JSONArray aggregatorArray = new JSONArray();
         if (superstep == 0 || m_aggregatorInUse.size() == 0) {
             return aggregatorArray;
         }
 
         Base64 base64 = new Base64();
         for (String name : m_aggregatorInUse) {
             try {
                 Aggregator<Writable> aggregator = m_aggregatorMap.get(name);
                 ByteArrayOutputStream outputStream =
                     new ByteArrayOutputStream();
                 DataOutput output = new DataOutputStream(outputStream);
                 aggregator.getAggregatedValue().write(output);
 
                 JSONObject aggregatorObj = new JSONObject();
                 aggregatorObj.put(AGGREGATOR_NAME_KEY, name);
                 aggregatorObj.put(
                     AGGREGATOR_VALUE_KEY,
                     base64.encodeToString(outputStream.toByteArray()));
                 aggregatorArray.put(aggregatorObj);
                 LOG.info("marshalAggregatorValues: " +
                          "Found aggregatorObj " +
                          aggregatorObj + ", value (" +
                          aggregator.getAggregatedValue() + ")");
             } catch (Exception e) {
                 throw new RuntimeException(e);
             }
         }
 
         LOG.info("marshalAggregatorValues: Finished assembling " +
                  "aggregator values in JSONArray - " + aggregatorArray);
         m_aggregatorInUse.clear();
         return aggregatorArray;
     }
 
     /**
      * Get values of aggregators aggregated by master in previous superstep.
      *
      * @param superstep
      */
     private void getAggregatorValues(long superstep) {
         if (superstep <= 1) {
             return;
         }
         String mergedAggregatorPath =
             getMergedAggregatorPath(getApplicationAttempt(), superstep - 1);
         JSONArray aggregatorArray = null;
         try {
             byte [] zkData =
                 getZkExt().getData(mergedAggregatorPath, false, null);
             aggregatorArray = new JSONArray(new String(zkData));
         } catch (KeeperException.NoNodeException e) {
             LOG.info("getAggregatorValues: no aggregators in " +
                      mergedAggregatorPath + " on superstep " + superstep);
             return;
         } catch (Exception e) {
             throw new RuntimeException(e);
         }
         Base64 base64 = new Base64();
         for (int i = 0; i < aggregatorArray.length(); ++i) {
             try {
                 LOG.info("getAggregatorValues: " +
                          "Getting aggregators from " +
                          aggregatorArray.getJSONObject(i));
                 String aggregatorName = aggregatorArray.getJSONObject(i).
                     getString(AGGREGATOR_NAME_KEY);
                 Aggregator<Writable> aggregator =
                     m_aggregatorMap.get(aggregatorName);
                 if (aggregator == null) {
                     continue;
                 }
                 Writable aggregatorValue = aggregator.getAggregatedValue();
                 InputStream input =
                     new ByteArrayInputStream(
                         (byte[]) base64.decode(aggregatorArray.getJSONObject(i).
                             getString(AGGREGATOR_VALUE_KEY)));
                 aggregatorValue.readFields(
                     new DataInputStream(input));
                 aggregator.setAggregatedValue(aggregatorValue);
                 LOG.info("getAggregatorValues: " +
                          "Got aggregator=" + aggregatorName + " value=" +
                          aggregatorValue);
             } catch (Exception e) {
                 throw new RuntimeException(e);
             }
         }
         LOG.info("getAggregatorValues: Finished loading " +
                  mergedAggregatorPath + " with aggregator values " +
                  aggregatorArray);
     }
 
     public Map<I, List<Vertex<I, V, E, M>>> getMaxIndexVertexLists() {
         return m_maxIndexVertexMap;
     }
 
     /**
      * Register the health of this worker for a given superstep
      *
      * @param superstep superstep to register health on
      */
     private void registerHealth(long superstep) {
         JSONArray hostnamePort = new JSONArray();
         hostnamePort.put(getHostname());
         int finalRpcPort =
             getConfiguration().getInt(BspJob.BSP_RPC_INITIAL_PORT,
                           BspJob.DEFAULT_BSP_RPC_INITIAL_PORT) +
                           getTaskPartition();
         hostnamePort.put(finalRpcPort);
 
         String myHealthPath = null;
         if (isHealthy()) {
             myHealthPath = getWorkerHealthyPath(getApplicationAttempt(),
                                                 getSuperstep());
         }
         else {
             myHealthPath = getWorkerUnhealthyPath(getApplicationAttempt(),
                                                   getSuperstep());
         }
         myHealthPath = myHealthPath + "/" + getHostnamePartitionId();
         try {
             m_myHealthZnode =
                 getZkExt().createExt(myHealthPath,
                                      hostnamePort.toString().getBytes(),
                                      Ids.OPEN_ACL_UNSAFE,
                                      CreateMode.EPHEMERAL,
                                      true);
         } catch (KeeperException.NodeExistsException e) {
             LOG.info("registerHealth: myHealthPath already exists (likely " +
                      "from previous failure): " + myHealthPath);
         } catch (Exception e) {
             throw new RuntimeException(e);
         }
         LOG.info("registerHealth: Created my health node for attempt=" +
                  getApplicationAttempt() + ", superstep=" +
                  getSuperstep() + " with " + m_myHealthZnode +
                  " and hostnamePort = " + hostnamePort.toString());
     }
 
     public boolean startSuperstep() {
         // Algorithm:
         // 1. Register my health for the next superstep.
         // 2. Wait until the vertex range assignment is complete (unless
         //    superstep 0).
         registerHealth(getSuperstep());
 
         String vertexRangeAssignmentsNode = null;
         if (getSuperstep() > 0) {
             vertexRangeAssignmentsNode =
                 getVertexRangeAssignmentsPath(getApplicationAttempt(),
                                               getSuperstep());
             try {
                 while (getZkExt().exists(vertexRangeAssignmentsNode, true) ==
                     null) {
                     getVertexRangeAssignmentsReadyChangedEvent().waitForever();
                     getVertexRangeAssignmentsReadyChangedEvent().reset();
                 }
             } catch (Exception e) {
                 throw new RuntimeException(e);
             }
         }
         LOG.info("startSuperstep: Ready for computation since worker " +
                  "selection and vertex range assignments are done in " +
                  vertexRangeAssignmentsNode);
 
         getAggregatorValues(getSuperstep());
         return true;
     }
 
     public boolean finishSuperstep(final Map<I, long []> maxIndexStatsMap) {
         // TODO: Note that this barrier blocks until success.  It would be
         // best if it were interruptible if for instance there was a failure.
 
         // Master will coordinate the barriers and aggregate "doneness" of all
         // the vertices.  Each worker will:
         // 1. Save aggregator values that are in use.
         // 2. Report the number of vertices in each partition on this worker
         //    and the number completed.
         // 3. Let the master know it is finished.
         // 4. Then it waits for the master to say whether to stop or not.
         JSONArray aggregatorValueArray =
             marshalAggregatorValues(getSuperstep());
         JSONArray vertexRangeStatArray = new JSONArray();
         for (Map.Entry<I, long []> entry :
             maxIndexStatsMap.entrySet()) {
             JSONObject statObject = new JSONObject();
             try {
                 ByteArrayOutputStream outputStream =
                     new ByteArrayOutputStream();
                 DataOutput output = new DataOutputStream(outputStream);
                 ((Writable) entry.getKey()).write(output);
 
                 statObject.put(JSONOBJ_MAX_VERTEX_INDEX_KEY,
                                outputStream.toString("UTF-8"));
                 statObject.put(JSONOBJ_FINISHED_VERTICES_KEY,
                                entry.getValue()[0]);
                 statObject.put(JSONOBJ_NUM_VERTICES_KEY,
                                entry.getValue()[1]);
             } catch (Exception e) {
                 throw new RuntimeException(e);
             }
             vertexRangeStatArray.put(statObject);
         }
 
         JSONObject workerFinishedInfoObj = new JSONObject();
         try {
             workerFinishedInfoObj.put(JSONOBJ_AGGREGATOR_VALUE_ARRAY_KEY,
                                       aggregatorValueArray);
             workerFinishedInfoObj.put(JSONOBJ_VERTEX_RANGE_STAT_ARRAY_KEY,
                                       vertexRangeStatArray);
         } catch (JSONException e) {
             throw new RuntimeException(e);
         }
         String finishedWorkerPath =
             getWorkerFinishedPath(getApplicationAttempt(), getSuperstep()) +
             "/" + getHostnamePartitionId();
         try {
             getZkExt().createExt(finishedWorkerPath,
                                  workerFinishedInfoObj.toString().getBytes(),
                                  Ids.OPEN_ACL_UNSAFE,
                                  CreateMode.PERSISTENT,
                                  true);
         } catch (KeeperException.NodeExistsException e) {
             LOG.warn("finishSuperstep: finished worker path " +
                      finishedWorkerPath + " already exists!");
         } catch (Exception e) {
             throw new RuntimeException(e);
         }
 
         String superstepFinishedNode =
             getSuperstepFinishedPath(getApplicationAttempt(), getSuperstep());
         JSONObject globalStatsObject = null;
         try {
             while (getZkExt().exists(superstepFinishedNode, true) == null) {
                 getSuperstepFinishedEvent().waitForever();
                 getSuperstepFinishedEvent().reset();
             }
             globalStatsObject = new JSONObject(
                 new String(getZkExt().getData(superstepFinishedNode, false, null)));
         } catch (Exception e) {
             throw new RuntimeException(e);
         }
         long finishedVertices =
             globalStatsObject.optLong(JSONOBJ_FINISHED_VERTICES_KEY);
         m_totalVertices =
             globalStatsObject.optLong(JSONOBJ_NUM_VERTICES_KEY);
         LOG.info("finishSuperstep: Completed superstep " + getSuperstep() +
                  " with finishedVertices=" + finishedVertices +
                  ", numVertices=" + m_totalVertices);
         incrCachedSuperstep();
         return (finishedVertices == m_totalVertices);
     }
 
     public long getTotalVertices() {
         return m_totalVertices;
     }
 
     /**
      * Save the vertices using the user-defined OutputFormat from our
      * vertexArray based on the split.
      */
     @SuppressWarnings("unchecked")
     public void saveVertices() {
         if (getConfiguration().get(BspJob.BSP_VERTEX_WRITER_CLASS) == null) {
             LOG.warn("saveVertices: BSP_VERTEX_WRITER_CLASS not specified" +
             " -- there will be no saved output");
             return;
         }
 
         Class<? extends VertexWriter<I, V, E>> vertexWriterClass =
             (Class<? extends VertexWriter<I, V, E>>)
             getConfiguration().getClass(BspJob.BSP_VERTEX_WRITER_CLASS,
                                         VertexWriter.class);
         VertexWriter<I, V, E> vertexWriter = null;
         try {
             vertexWriter = vertexWriterClass.newInstance();
             for (Map.Entry<I, List<Vertex<I, V, E, M>>> entry :
                 getMaxIndexVertexLists().entrySet()) {
                 for (Vertex<I, V, E, M> vertex : entry.getValue()) {
                     vertexWriter.write(getContext(),
                                        vertex.getVertexId(),
                                        vertex.getVertexValue(),
                                        vertex.getOutEdgeIterator());
                 }
             }
             vertexWriter.close(getContext());
         } catch (Exception e) {
             throw new RuntimeException(e);
         }
 
     }
 
     public void cleanup() {
         saveVertices();
          // All worker processes should denote they are done by adding special
          // znode.  Once the number of znodes equals the number of partitions
          // for workers and masters, the master will clean up the ZooKeeper
          // znodes associated with this job.
         String cleanedUpPath = CLEANED_UP_PATH  + "/" +
             getTaskPartition() + WORKER_SUFFIX;
         try {
             String finalFinishedPath =
                 getZkExt().createExt(cleanedUpPath,
                                      null,
                                      Ids.OPEN_ACL_UNSAFE,
                                      CreateMode.PERSISTENT,
                                      true);
              LOG.info("cleanup: Notifying master its okay to cleanup with " +
                      finalFinishedPath);
         } catch (KeeperException.NodeExistsException e) {
             LOG.info("cleanup: Couldn't create finished node '" +
                      cleanedUpPath);
         } catch (Exception e) {
             // cleanup phase -- just log the error
             LOG.error(e.getMessage());
         }
         try {
             getZkExt().close();
         } catch (InterruptedException e) {
             // cleanup phase -- just log the error
             LOG.error("cleanup: Zookeeper failed to close with " + e);
         }
     }
 
     public VertexRange<I> getVertexRange(I index) {
         compareVertexRange.setMaxIndex(index);
         VertexRange<I> result =
             getVertexRangeSet(getSuperstep()).ceiling(compareVertexRange);
 
         if (result == null) {
             LOG.debug("getVertexRange: no partition for destination vertex " +
                        index + " -- returning last partition");
             result = getVertexRangeSet(getSuperstep()).last();
         }
         return result;
     }
 
     /**
      * Register an aggregator with name.
      *
      * @param name
      * @param aggregator
      * @return boolean (false when aggregator already registered)
      */
     public static <A extends Writable> boolean registerAggregator(
         String name,
         Aggregator<A> aggregator) {
         if (m_aggregatorMap.get(name) != null) {
             return false;
         }
         @SuppressWarnings("unchecked")
         Aggregator<Writable> castedAggregator =
             (Aggregator<Writable>) aggregator;
         m_aggregatorMap.put(name, castedAggregator);
         LOG.info("registered aggregator=" + name);
         return true;
     }
 
     /**
      * Get aggregator by name.
      *
      * @param name
      * @return Aggregator<A> (null when not registered)
      */
     public static <A extends Writable> Aggregator<A> getAggregator(
         String name) {
         @SuppressWarnings("unchecked")
         Aggregator<A> castedAggregator =
             (Aggregator<A>) m_aggregatorMap.get(name);
         return castedAggregator;
     }
 
     /**
      * Use an aggregator in this superstep.
      *
      * @param name
      * @return boolean (false when aggregator not registered)
      */
     public static boolean useAggregator(String name) {
         if (m_aggregatorMap.get(name) == null) {
             LOG.error("userAggregator: Aggregator=" + name + " not registered");
             return false;
         }
         m_aggregatorInUse.add(name);
         return true;
     }
 
     public void storeCheckpoint() throws IOException {
         // Algorithm:
         // For each partition, dump vertices and messages
         Map<I, List<Vertex<I, V, E, M>>> maxIndexVertexListMap =
             getMaxIndexVertexLists();
         Path metadataFilePath =
             new Path(getCheckpointBasePath(getSuperstep()) + "." +
                      getHostnamePartitionId() +
                      CHECKPOINT_METADATA_POSTFIX);
         Path verticesFilePath =
             new Path(getCheckpointBasePath(getSuperstep()) + "." +
                      getHostnamePartitionId() +
                      CHECKPOINT_VERTICES_POSTFIX);
         Path validFilePath =
             new Path(getCheckpointBasePath(getSuperstep()) + "." +
                      getHostnamePartitionId() +
                      CHECKPOINT_VALID_POSTFIX);
 
         // Remove these files if they already exist
         try {
             getFs().delete(validFilePath, false);
             LOG.warn("storeCheckpoint: Removed file " + validFilePath);
         } catch (IOException e) {
         }
         try {
             getFs().delete(metadataFilePath, false);
             LOG.warn("storeCheckpoint: Removed file " + metadataFilePath);
         } catch (IOException e) {
         }
         try {
             getFs().delete(verticesFilePath, false);
             LOG.warn("storeCheckpoint: Removed file " + verticesFilePath);
         } catch (IOException e) {
         }
 
         FSDataOutputStream metadataOutputStream =
             getFs().create(metadataFilePath);
         FSDataOutputStream verticesOutputStream =
             getFs().create(verticesFilePath);
         metadataOutputStream.writeLong(maxIndexVertexListMap.entrySet().size());
         for (Map.Entry<I, List<Vertex<I, V, E, M>>> entry :
             maxIndexVertexListMap.entrySet()) {
             long startPos = verticesOutputStream.getPos();
 
             // Write the vertices (index, data, edges and messages)
             // Format:
             // <vertex count>
             //   <v0 id><v0 value>
             //     <v0 num edges>
             //       <v0 edge 0 dest><v0 edge 0 value>
             //       <v0 edge 1 dest><v0 edge 1 value>...
             //     <v0 message count>
             //       <v0 msg 0><v0 msg 1>...
             verticesOutputStream.writeLong(entry.getValue().size());
             for (Vertex<I, V, E, M> vertex : entry.getValue()) {
                 ByteArrayOutputStream vertexByteStream =
                     new ByteArrayOutputStream();
                 DataOutput vertexOutput =
                     new DataOutputStream(vertexByteStream);
                 vertex.getVertexId().write(vertexOutput);
                 vertex.getVertexValue().write(vertexOutput);
                 OutEdgeIterator<I, E> outEdgeIterator =
                     vertex.getOutEdgeIterator();
                 vertexOutput.writeLong(outEdgeIterator.size());
                 while (outEdgeIterator.hasNext()) {
                     Map.Entry<I, E> outEdgeEntry = outEdgeIterator.next();
                     outEdgeEntry.getKey().write(vertexOutput);
                     outEdgeEntry.getValue().write(vertexOutput);
                 }
                 List<M> messageList = getBspMapper().getVertexMessageList(
                     vertex.getVertexId());
                 if (messageList == null) {
                     messageList = new ArrayList<M>();
                 }
                 vertexOutput.writeLong(messageList.size());
                 for (M message : messageList) {
                     message.write(vertexOutput);
                 }
                 verticesOutputStream.write(vertexByteStream.toByteArray());
 
                 LOG.debug("storeCheckpoint: Wrote vertex id = " +
                           vertex.getVertexId() + " with " +
                           outEdgeIterator.size() + " edges and " +
                           messageList.size() + " messages (" +
                           vertexByteStream.size() + " total bytes)");
             }
             // Write the metadata for this vertex range
             // Format:
             // <index count>
             //   <index 0 start pos><# vertices><# edges><max index 0>
             //   <index 1 start pos><# vertices><# edges><max index 1>...
             ByteArrayOutputStream metadataByteStream =
                 new ByteArrayOutputStream();
             DataOutput metadataOutput =
                 new DataOutputStream(metadataByteStream);
             metadataOutput.writeLong(startPos);
             metadataOutput.writeLong(entry.getValue().size());
             long edgeCount = 0;
             for (Vertex<I, V, E, M> vertex : entry.getValue()) {
                 edgeCount += vertex.getOutEdgeIterator().size();
             }
             metadataOutput.writeLong(edgeCount);
             entry.getKey().write(metadataOutput);
             LOG.debug("storeCheckpoint: Vertex file starting " +
                       "offset = " + startPos + ", length = " +
                       (verticesOutputStream.getPos() - startPos) +
                       ", max index of vertex range = " + entry.getKey());
             metadataOutputStream.write(metadataByteStream.toByteArray());
         }
         metadataOutputStream.close();
         verticesOutputStream.close();
         LOG.info("storeCheckpoint: Finished metadata (" +
                  metadataFilePath + ") and vertices (" + verticesFilePath
                  + ").");
 
         getFs().createNewFile(validFilePath);
     }
 
     /**
      * Load a single vertex range.
      *
      * @param maxIndex denotes the vertex range
      * @param dataFileName name of the data file
      * @param startPos position to start from in data file
      * @throws IOException
      * @throws IllegalAccessException
      * @throws InstantiationException
      */
     private void loadVertexRange(I maxIndex,
                                  String dataFileName,
                                  long startPos)
         throws IOException, InstantiationException, IllegalAccessException {
         // Read in the reverse order from storeCheckpoint()
         DataInputStream dataStream = getFs().open(new Path(dataFileName));
         dataStream.skip(startPos);
         long vertexCount = dataStream.readLong();
         m_maxIndexVertexMap.put(maxIndex,
                                 new ArrayList<Vertex<I, V, E, M>>());
         List<Vertex<I, V, E,M>> vertexList =
             m_maxIndexVertexMap.get(maxIndex);
         for (int i = 0; i < vertexCount; ++i) {
             HadoopVertex<I, V, E, M> vertex =
                 getHadoopVertexClass().newInstance();
             I vertexId = createVertexIndex();
             V vertexValue = createVertexValue();
             vertexId.readFields(dataStream);
             vertexValue.readFields(dataStream);
             vertex.setVertexId(vertexId);
             vertex.setVertexValue(vertexValue);
             long numEdges = dataStream.readLong();
             for (long j = 0; j < numEdges; ++j) {
                 I destVertexId = createVertexIndex();
                 E edgeValue = createEdgeValue();
                 destVertexId.readFields(dataStream);
                 edgeValue.readFields(dataStream);
                 vertex.addEdge(destVertexId, edgeValue);
             }
             long msgCount = dataStream.readLong();
             List<M> msgList = new ArrayList<M>();
             for (long j = 0; j < msgCount; ++j) {
                 M msg = createMsgValue();
                 msg.readFields(dataStream);
                 msgList.add(msg);
             }
             vertex.setBspMapper(getBspMapper());
 
             // Add the vertex and associated messages
             vertexList.add(vertex);
             getBspMapper().setVertexMessageList(vertexId, msgList);
         }
         LOG.info("loadVertexRange: " + vertexCount + " vertices in " +
                  dataFileName);
         dataStream.close();
     }
 
     public void loadCheckpoint(long superstep) {
         // Algorithm:
         // Check all the vertex ranges for this worker and load the ones
         // that match my hostname and id.
         I maxVertexIndex = createVertexIndex();
         long startPos = -1;
         long vertexRangeCount = -1;
         for (VertexRange<I> vertexRange : getVertexRangeSet(getSuperstep())) {
             if (vertexRange.getHostnameId().compareTo(
                     getHostnamePartitionId()) == 0) {
                 String metadataFile =
                     vertexRange.getCheckpointFilePrefix() +
                     CHECKPOINT_METADATA_POSTFIX;
                 try {
                     DataInputStream metadataStream =
                         getFs().open(new Path(metadataFile));
                     vertexRangeCount = metadataStream.readLong();
                     for (int i = 0; i < vertexRangeCount; ++i) {
                         startPos = metadataStream.readLong();
                         // Skip the vertex count
                         metadataStream.readLong();
                         // Skip the edge count
                         metadataStream.readLong();
                         maxVertexIndex.readFields(metadataStream);
                         @SuppressWarnings("unchecked")
                         int compareTo =
                             vertexRange.getMaxIndex().compareTo(maxVertexIndex);
                         LOG.debug("loadCheckpoint: Comparing " +
                                   vertexRange.getMaxIndex() + " and " +
                                   maxVertexIndex + " = " + compareTo);
                         if (compareTo == 0) {
                             loadVertexRange(
                                 vertexRange.getMaxIndex(),
                                 vertexRange.getCheckpointFilePrefix() +
                                     CHECKPOINT_VERTICES_POSTFIX,
                                 startPos);
                         }
                     }
                     metadataStream.close();
                 } catch (Exception e) {
                     throw new RuntimeException(e);
                 }
             }
         }
     }
 
 }
