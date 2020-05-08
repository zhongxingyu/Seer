 package com.convert.rice.client;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.UUID;
 import java.util.concurrent.ExecutionException;
 
 import org.apache.hadoop.hbase.HBaseTestingUtility;
 import org.apache.hadoop.hbase.client.HBaseAdmin;
 import org.apache.hadoop.hbase.client.HTablePool;
 import org.junit.AfterClass;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import com.convert.rice.AggregationUtility;
 import com.convert.rice.TimeSeries;
 import com.convert.rice.hbase.HBaseTimeSeries;
 import com.convert.rice.protocol.Aggregation;
 import com.convert.rice.protocol.Response.GetResult;
 import com.convert.rice.protocol.Response.GetResult.Metric;
 import com.convert.rice.protocol.Response.IncResult;
 import com.convert.rice.server.protobuf.RiceProtoBufRpcServer;
 import com.google.common.base.Supplier;
 
 public class RiceClientTest {
 
     private static HBaseTestingUtility testUtil;
 
     private static HTablePool pool;
 
     private static final String type = "type";
 
     private static HBaseAdmin admin;
 
     private static RiceProtoBufRpcServer server;
 
     @BeforeClass
     public static void setup() throws Exception {
         testUtil = new HBaseTestingUtility();
         testUtil.startMiniCluster();
         pool = new HTablePool(testUtil.getConfiguration(), 10);
         admin = testUtil.getHBaseAdmin();

        final HBaseTimeSeries hBaseTimeSeries = new HBaseTimeSeries(pool);
         hBaseTimeSeries.checkOrCreateTable(admin, type);
 
         server = new RiceProtoBufRpcServer(7654, new Supplier<TimeSeries>() {
 
             @Override
             public TimeSeries get() {
                 return hBaseTimeSeries;
             }
         });
         server.startAndWait();
         System.out.println("Started");
 
     }
 
     @AfterClass
     public static void after() throws IOException, InterruptedException, ExecutionException {
         server.stop().get();
        new HBaseTimeSeries(pool).deleteTable(admin, type);
         pool.close();
         testUtil.shutdownMiniCluster();
     }
 
     @Test
     public void testInc() throws Exception {
         @SuppressWarnings("serial")
         Map<String, Long> metrics = new HashMap<String, Long>() {
 
             {
                 put("a", 10L);
                 put("b", 20L);
             }
         };
         String key = UUID.randomUUID().toString();
         long timestamp = System.currentTimeMillis();
         RiceClient client = new RiceClient("localhost", 7654);
         IncResult result = client.inc(type, key, metrics, timestamp).get();
         assertEquals(key, result.getKey());
         assertEquals(timestamp, result.getTimestamp());
         assertEquals(type, result.getType());
 
         client.close();
     }
 
     @Test
     public void testGet() throws Exception {
         @SuppressWarnings("serial")
         Map<String, Long> metrics = new HashMap<String, Long>() {
 
             {
                 put("a", 10L);
                 put("b", 20L);
             }
         };
         String key = UUID.randomUUID().toString();
         long timestamp = System.currentTimeMillis();
         RiceClient client = new RiceClient("localhost", 7654);
 
         // increment twice.
         client.inc(type, key, metrics, timestamp).get();
         client.inc(type, key, metrics, timestamp).get();
 
         GetResult result = client.get(type, key, 0, timestamp, Aggregation.HOUR).get();
         assertEquals(key, result.getKey());
         assertEquals(metrics.size(), result.getMetricsCount());
         Map<String, Metric> metricsMap = new HashMap<String, Metric>(metrics.size());
         for (Metric m : result.getMetricsList()) {
             metricsMap.put(m.getName(), m);
         }
         Metric metricA = metricsMap.get("a");
         Metric metricB = metricsMap.get("b");
 
         assertNotNull(metricA);
         assertNotNull(metricB);
 
         assertEquals(1, metricA.getPointsCount());
         assertEquals(1, metricB.getPointsCount());
 
         assertEquals(AggregationUtility.aggregateTo(timestamp, Aggregation.HOUR).getMillis(), metricA.getPoints(0)
                 .getTimestamp());
         assertEquals(AggregationUtility.aggregateTo(timestamp, Aggregation.HOUR).getMillis(), metricB.getPoints(0)
                 .getTimestamp());
 
         assertEquals(20L, metricA.getPoints(0).getValue());
         assertEquals(40L, metricB.getPoints(0).getValue());
 
         client.close();
     }
 
 }
