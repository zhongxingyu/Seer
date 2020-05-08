 /*
  * This is a common dao with basic CRUD operations and is not limited to any
  * persistent layer implementation
  *
  * Copyright (C) 2010  Imran M Yousuf (imyousuf@smartitengineering.com)
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 3 of the License, or (at your option) any later version.
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
  */
 package com.smartitengineering.dao.hbase.autoincrement;
 
 import com.smartitengineering.dao.hbase.ddl.HBaseTableConfiguration;
 import com.smartitengineering.dao.hbase.ddl.HBaseTableGenerator;
 import com.smartitengineering.dao.hbase.ddl.config.json.ConfigurationJsonParser;
 import com.sun.jersey.api.client.Client;
 import com.sun.jersey.api.client.filter.LoggingFilter;
 import com.sun.jersey.client.apache.ApacheHttpClient;
 import com.sun.jersey.client.apache.ApacheHttpClientHandler;
 import com.sun.jersey.client.apache.config.ApacheHttpClientConfig;
 import com.sun.jersey.client.apache.config.DefaultApacheHttpClientConfig;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.List;
 import java.util.Set;
 import java.util.TreeSet;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
 import junit.framework.Assert;
 import org.apache.commons.httpclient.HttpClient;
 import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
 import org.apache.hadoop.hbase.HBaseTestingUtility;
 import org.apache.hadoop.hbase.client.Get;
 import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.HTableInterface;
import org.apache.hadoop.hbase.client.HTablePool;
 import org.apache.hadoop.hbase.client.Put;
 import org.apache.hadoop.hbase.client.Result;
 import org.apache.hadoop.hbase.client.RowLock;
 import org.apache.hadoop.hbase.util.Bytes;
 import org.codehaus.jackson.jaxrs.JacksonJsonProvider;
 import org.eclipse.jetty.server.Server;
 import org.eclipse.jetty.webapp.WebAppContext;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  *
  * @author imyousuf
  */
 public class AutoIncrementRowIdForLongTest {
 
   private static final HBaseTestingUtility TEST_UTIL = new HBaseTestingUtility();
   private static final Logger LOGGER = LoggerFactory.getLogger(AutoIncrementRowIdForLongTest.class);
   private static final String TABLE_NAME = "test";
   private static final String FAMILY_NAME = "family";
   private static final byte[] FAMILY_BYTES = Bytes.toBytes(FAMILY_NAME);
   private static final byte[] CELL_BYTES = Bytes.toBytes("cell");
   private static final int PORT = 10080;
   private static final String URI_FOR_TABLE = new StringBuilder("http://localhost:").append(PORT).append('/').append(
       TABLE_NAME).toString();
   private static Server jettyServer;
   private static Client client;
   private static final Set<Long> ids = new TreeSet<Long>();
  private static HTablePool pool;
 
   @BeforeClass
   public static void globalSetup() throws Exception {
     /*
      * Start HBase and initialize tables
      */
     //-Djavax.xml.parsers.DocumentBuilderFactory=com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl
     System.setProperty("javax.xml.parsers.DocumentBuilderFactory",
                        "com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl");
     try {
       TEST_UTIL.startMiniCluster();
     }
     catch (Exception ex) {
       LOGGER.error(ex.getMessage(), ex);
     }
     //Create table for testing
     InputStream classpathResource = AutoIncrementRowIdForLongTest.class.getClassLoader().getResourceAsStream(
         "ddl-config-sample1.json");
     Collection<HBaseTableConfiguration> configs = ConfigurationJsonParser.getConfigurations(classpathResource);
     try {
       new HBaseTableGenerator(configs, TEST_UTIL.getConfiguration(), true).generateTables();
     }
     catch (Exception ex) {
       LOGGER.error("Could not create table!", ex);
       Assert.fail(ex.getMessage());
     }
    pool = new HTablePool(TEST_UTIL.getConfiguration(), 200);
     //Start web app
     jettyServer = new Server(PORT);
     final String webapp = "./src/main/webapp/";
     if (!new File(webapp).exists()) {
       throw new IllegalStateException("WebApp file/dir does not exist!");
     }
     WebAppContext webAppHandler = new WebAppContext(webapp, "/");
     jettyServer.setHandler(webAppHandler);
     jettyServer.setSendDateHeader(true);
     jettyServer.start();
     //Initialize client
     final ApacheHttpClientConfig config = new DefaultApacheHttpClientConfig();
     config.getClasses().add(JacksonJsonProvider.class);
     final ApacheHttpClientHandler handler = new ApacheHttpClientHandler(new HttpClient(
         new MultiThreadedHttpConnectionManager()));
     client = new ApacheHttpClient(handler, config);
     client.addFilter(new LoggingFilter());
   }
 
   public static void globalTeardown() throws Exception {
     jettyServer.stop();
     TEST_UTIL.shutdownMiniCluster();
   }
 
   @Test
   public void testFirstPrimaryKey() throws IOException {
     ClientIdConfig clientIdConfig = client.resource(URI_FOR_TABLE).post(ClientIdConfig.class);
     Assert.assertEquals(1, Long.MAX_VALUE - clientIdConfig.getId());
     final long id = clientIdConfig.getId();
     final byte[] row = Bytes.toBytes(id);
     RowLock lock = new RowLock(row, clientIdConfig.getLocKId());
     Put put = new Put(row, lock);
     final byte[] toBytes = Bytes.toBytes("value");
     put.add(FAMILY_BYTES, CELL_BYTES, toBytes);
     HTable table = new HTable(TABLE_NAME);
     table.put(put);
     Get get = new Get(row);
     final Result get1 = table.get(get);
     ids.add(Long.MAX_VALUE - id);
     Assert.assertNotNull(get1);
     Assert.assertTrue(Arrays.equals(row, get1.getRow()));
     Assert.assertTrue(Arrays.equals(toBytes, get1.getValue(FAMILY_BYTES, CELL_BYTES)));
   }
 
   @Test
   public void testConsequetive100Keys() throws IOException {
     for (long id = 2; id < 102; ++id) {
       ClientIdConfig clientIdConfig = client.resource(URI_FOR_TABLE).post(ClientIdConfig.class);
       Assert.assertEquals(id, Long.MAX_VALUE - clientIdConfig.getId());
       final byte[] row = Bytes.toBytes(clientIdConfig.getId());
       RowLock lock = new RowLock(row, clientIdConfig.getLocKId());
       Put put = new Put(row, lock);
       final byte[] toBytes = Bytes.toBytes("value " + id);
       put.add(FAMILY_BYTES, CELL_BYTES, toBytes);
       HTable table = new HTable(TABLE_NAME);
       table.put(put);
       Get get = new Get(row);
       final Result get1 = table.get(get);
       ids.add(id);
       Assert.assertNotNull(get1);
       Assert.assertTrue(Arrays.equals(row, get1.getRow()));
       Assert.assertTrue(Arrays.equals(toBytes, get1.getValue(FAMILY_BYTES, CELL_BYTES)));
     }
   }
 
   @Test
   public void testMultithreadedKeys() throws Exception {
     ExecutorService service = Executors.newFixedThreadPool(100);
     final long start = 102;
    final int bound = 100;
    final int innerBound = 30;
     List<Future> futures = new ArrayList<Future>();
     for (int i = 0; i < bound; ++i) {
       final int index = i;
       futures.add(service.submit(new Runnable() {
 
         @Override
         public void run() {
          for (int j = 0; j < innerBound; ++j) {
            final HTableInterface table = pool.getTable(TABLE_NAME);
             long id = index * bound + j + start;
             ClientIdConfig clientIdConfig = client.resource(URI_FOR_TABLE).post(ClientIdConfig.class);
             final long id1 = clientIdConfig.getId();
             synchronized (ids) {
               final long mainId = Long.MAX_VALUE - id1;
               LOGGER.info("ID: " + (mainId));
               Assert.assertFalse(ids.contains(mainId));
               ids.add(mainId);
             }
             final byte[] row = Bytes.toBytes(id1);
             RowLock lock = new RowLock(row, clientIdConfig.getLocKId());
             Put put = new Put(row, lock);
             final byte[] toBytes = Bytes.toBytes("value " + id);
             put.add(FAMILY_BYTES, CELL_BYTES, toBytes);
             Result get1;
             try {
               table.put(put);
               Get get = new Get(row);
               get1 = table.get(get);
             }
             catch (Exception ex) {
               LOGGER.error(ex.getMessage(), ex);
               get1 = null;
               Assert.fail(ex.getMessage());
             }
            pool.putTable(table);
             Assert.assertNotNull(get1);
             Assert.assertFalse(get1.isEmpty());
             Assert.assertTrue(Arrays.equals(row, get1.getRow()));
             Assert.assertTrue(Arrays.equals(toBytes, get1.getValue(FAMILY_BYTES, CELL_BYTES)));
           }
         }
       }));
     }
     for (Future future : futures) {
       future.get();
     }
     LOGGER.info("IDS: " + ids);
    Assert.assertEquals(bound*innerBound+start-1, ids.size());
   }
 }
