 /*
  * Copyright 2013 NGDATA nv
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.ngdata.hbaseindexer.impl;
 
 import com.ngdata.hbaseindexer.model.api.IndexerDefinition;
 import com.ngdata.hbaseindexer.model.api.IndexerDefinitionBuilder;
 import com.ngdata.hbaseindexer.model.api.IndexerModelEvent;
 import com.ngdata.hbaseindexer.model.api.IndexerModelEventType;
 import com.ngdata.hbaseindexer.model.api.IndexerModelListener;
 import com.ngdata.hbaseindexer.model.api.IndexerUpdateException;
 import com.ngdata.hbaseindexer.model.api.WriteableIndexerModel;
 import com.ngdata.hbaseindexer.model.impl.IndexerModelImpl;
 import com.ngdata.sep.util.io.Closer;
 import com.ngdata.sep.util.zookeeper.ZkUtil;
 import com.ngdata.sep.util.zookeeper.ZooKeeperItf;
 import org.apache.hadoop.hbase.zookeeper.MiniZooKeeperCluster;
 import org.junit.AfterClass;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import javax.management.MBeanServerConnection;
 import javax.management.ObjectName;
 import java.io.File;
 import java.io.IOException;
 import java.net.ServerSocket;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.Set;
 
 import static com.ngdata.hbaseindexer.model.api.IndexerDefinition.IncrementalIndexingState;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 
 public class IndexerModelImplTest {
     private static MiniZooKeeperCluster ZK_CLUSTER;
     private static File ZK_DIR;
     private static int ZK_CLIENT_PORT;
 
     @BeforeClass
     public static void setUpBeforeClass() throws Exception {
         ZK_DIR = new File(System.getProperty("java.io.tmpdir") + File.separator + "hbaseindexer.zklocktest");
         ZK_CLIENT_PORT = getFreePort();
 
         ZK_CLUSTER = new MiniZooKeeperCluster();
         ZK_CLUSTER.setDefaultClientPort(ZK_CLIENT_PORT);
         ZK_CLUSTER.startup(ZK_DIR);
     }
 
     @AfterClass
     public static void tearDownAfterClass() throws Exception {
         if (ZK_CLUSTER != null) {
             ZK_CLUSTER.shutdown();
         }
     }
 
     @Test
     public void testEvents() throws Exception {
         ZooKeeperItf zk1 = ZkUtil.connect("localhost:" + ZK_CLIENT_PORT, 5000);
         ZooKeeperItf zk2 = ZkUtil.connect("localhost:" + ZK_CLIENT_PORT, 5000);
         WriteableIndexerModel model1 = null;
         WriteableIndexerModel model2 = null;
         try {
             TestListener listener = new TestListener();
 
             model1 = new IndexerModelImpl(zk1, "/test");
             model1.registerListener(listener);
 
             // Create an indexer -- verify INDEXER_ADDED event
             IndexerDefinition indexer1 = new IndexerDefinitionBuilder()
                     .name("indexer1")
                     .configuration("my-conf".getBytes("UTF-8"))
                     .build();
             model1.addIndexer(indexer1);
 
             listener.waitForEvents(1);
             listener.verifyEvents(new IndexerModelEvent(IndexerModelEventType.INDEXER_ADDED, "indexer1"));
 
             // Verify that a fresh indexer model has the index
             model2 = new IndexerModelImpl(zk2, "/test");
            assertEquals(1, model2.getIndexers().size());
             assertTrue(model2.hasIndexer("indexer1"));
 
             // Update the indexer -- verify INDEXER_UPDATED event
             indexer1 = new IndexerDefinitionBuilder()
                     .startFrom(indexer1)
                     .incrementalIndexingState(IncrementalIndexingState.SUBSCRIBE_DO_NOT_CONSUME)
                     .build();
             String lock = model1.lockIndexer("indexer1");
             model1.updateIndexer(indexer1, lock);
 
             listener.waitForEvents(1);
             listener.verifyEvents(new IndexerModelEvent(IndexerModelEventType.INDEXER_UPDATED, "indexer1"));
             model1.unlockIndexer(lock);
 
             // Delete the indexer -- verify INDEXER_DELETED event
             model1.deleteIndexerInternal("indexer1");
             listener.waitForEvents(1);
             listener.verifyEvents(new IndexerModelEvent(IndexerModelEventType.INDEXER_DELETED, "indexer1"));
 
             // Create some more indexes and verify we get the correct number of INDEXER_ADDED events
             IndexerModelEvent[] expectedEvents = new IndexerModelEvent[9];
             for (int i = 2; i <= 10; i++) {
                 String name = "indexer" + i;
                 IndexerDefinition indexer = new IndexerDefinitionBuilder()
                         .name(name)
                         .configuration("my-conf".getBytes("UTF-8"))
                         .build();
                 model1.addIndexer(indexer);
                 expectedEvents[i - 2] = new IndexerModelEvent(IndexerModelEventType.INDEXER_ADDED, name);
             }
 
             listener.waitForEvents(9);
             listener.verifyEvents(expectedEvents);
 
             // Terminate ZK connections: clients should automatically re-establish the connection and things
             // should work as before
             assertEquals(2, terminateZooKeeperConnections());
 
             // Do another index update and check we get an event
             IndexerDefinition indexer2 = new IndexerDefinitionBuilder()
                     .name("indexer2")
                     .incrementalIndexingState(IncrementalIndexingState.DO_NOT_SUBSCRIBE)
                     .configuration("my-conf".getBytes())
                     .build();
             lock = model1.lockIndexer("indexer2");
             model1.updateIndexer(indexer2, lock);
             model1.unlockIndexer(lock);
 
             listener.waitForEvents(1);
             listener.verifyEvents(new IndexerModelEvent(IndexerModelEventType.INDEXER_UPDATED, "indexer2"));
         } finally {
             Closer.close(model1);
             Closer.close(model2);
             Closer.close(zk1);
             Closer.close(zk2);
         }
     }
 
     @Test
     public void testLocking() throws Exception {
         ZooKeeperItf zk1 = ZkUtil.connect("localhost:" + ZK_CLIENT_PORT, 5000);
         ZooKeeperItf zk2 = ZkUtil.connect("localhost:" + ZK_CLIENT_PORT, 5000);
         WriteableIndexerModel model1 = null;
         WriteableIndexerModel model2 = null;
         try {
             model1 = new IndexerModelImpl(zk1, "/test");
             model2 = new IndexerModelImpl(zk2, "/test");
 
             // Create an index
             IndexerDefinition indexer1 = new IndexerDefinitionBuilder()
                    .name("lock_test_indexer")
                     .configuration("foo".getBytes())
                     .build();
             model1.addIndexer(indexer1);
 
             // Lock the index via the first client
            String lock = model1.lockIndexer("lock_test_indexer");
 
             // Try to update it via the second client
             indexer1 = new IndexerDefinitionBuilder()
                     .startFrom(indexer1)
                     .configuration("foo1".getBytes())
                     .build();
 
             try {
                 model2.updateIndexer(indexer1, lock + "foo");
                 fail("Expected exception");
             } catch (IndexerUpdateException e) {
                 // verify the exception says something about locks
                 assertTrue(e.getMessage().contains("lock"));
             }
 
             // First client should be able to do the update though
             model1.updateIndexer(indexer1, lock);
 
             model1.unlockIndexer(lock);
         } finally {
             Closer.close(model1);
             Closer.close(model2);
             Closer.close(zk1);
             Closer.close(zk2);
         }
     }
 
     private class TestListener implements IndexerModelListener {
         private Set<IndexerModelEvent> events = new HashSet<IndexerModelEvent>();
 
         @Override
         public void process(IndexerModelEvent event) {
             synchronized (this) {
                 events.add(event);
                 notifyAll();
             }
         }
 
         public void waitForEvents(int count) throws InterruptedException {
             long timeout = 1000;
             long now = System.currentTimeMillis();
             synchronized (this) {
                 while (events.size() < count && System.currentTimeMillis() - now < timeout) {
                     wait(timeout);
                 }
             }
         }
 
         public void verifyEvents(IndexerModelEvent... expectedEvents) {
             if (events.size() != expectedEvents.length) {
                 if (events.size() > 0) {
                     System.out.println("The events are:");
                     for (IndexerModelEvent item : events) {
                         System.out.println(item.getType() + " - " + item.getIndexerName());
                     }
                 } else {
                     System.out.println("There are no events.");
                 }
 
                 assertEquals("Expected number of events", expectedEvents.length, events.size());
             }
 
             Set<IndexerModelEvent> expectedEventsSet  = new HashSet<IndexerModelEvent>(Arrays.asList(expectedEvents));
 
             for (IndexerModelEvent event : expectedEvents) {
                 if (!events.contains(event)) {
                     fail("Expected event not present among events: " + event);
                 }
             }
 
             for (IndexerModelEvent event : events) {
                 if (!expectedEventsSet.contains(event)) {
                     fail("Got an event which is not among the expected events: " + event);
                 }
             }
 
             events.clear();
         }
     }
 
     public static int getFreePort() {
         ServerSocket socket = null;
         try {
             socket = new ServerSocket(0);
             return socket.getLocalPort();
         } catch (IOException e) {
             throw new RuntimeException("Error finding a free port", e);
         } finally {
             if (socket != null) {
                 try {
                     socket.close();
                 } catch (IOException e) {
                     throw new RuntimeException("Error closing ServerSocket used to detect a free port.", e);
                 }
             }
         }
     }
 
     public int terminateZooKeeperConnections() throws Exception {
         MBeanServerConnection connection = java.lang.management.ManagementFactory.getPlatformMBeanServer();
         ObjectName replicationSources = new ObjectName("org.apache.ZooKeeperService:name0=*,name1=Connections,name2=*,name3=*");
         Set<ObjectName> mbeans = connection.queryNames(replicationSources, null);
         int connectionCount = mbeans.size();
 
         for (ObjectName name : mbeans) {
             connection.invoke(name, "terminateConnection", new Object[] {}, new String[] {});
         }
 
         return connectionCount;
     }
 }
