 /*******************************************************************************
  * Copyright (c) 2006-2011 eBay Inc. All Rights Reserved.
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *  
  *    http://www.apache.org/licenses/LICENSE-2.0
  *******************************************************************************/
 package org.ebayopensource.turmeric.runtime.error.integration;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.fail;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import me.prettyprint.cassandra.serializers.LongSerializer;
 import me.prettyprint.cassandra.serializers.StringSerializer;
 import me.prettyprint.hector.api.Keyspace;
 import me.prettyprint.hector.api.Serializer;
 import me.prettyprint.hector.api.beans.ColumnSlice;
 import me.prettyprint.hector.api.beans.OrderedRows;
 import me.prettyprint.hector.api.beans.Row;
 import me.prettyprint.hector.api.factory.HFactory;
 import me.prettyprint.hector.api.mutation.Mutator;
 import me.prettyprint.hector.api.query.QueryResult;
 import me.prettyprint.hector.api.query.RangeSlicesQuery;
 
 import org.ebayopensource.turmeric.common.v1.types.CommonErrorData;
 import org.ebayopensource.turmeric.runtime.common.exceptions.ServiceException;
 import org.ebayopensource.turmeric.runtime.common.pipeline.LoggingHandler.InitContext;
 import org.ebayopensource.turmeric.runtime.error.cassandra.handler.CassandraErrorLoggingHandler;
 import org.ebayopensource.turmeric.runtime.error.utils.MockInitContext;
 import org.ebayopensource.turmeric.utils.cassandra.hector.HectorManager;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 public class CassandraErrorLoggingHandlerTestIT extends CassandraTestHelper {
     private static final Long ERROR_ID = Long.valueOf(0);
     private static final Serializer<String> STR_SERIALIZER = StringSerializer.get();
     String consumerName = "ConsumerName1";
     InitContext ctx = null;
     List<CommonErrorData> errorsToStore = null;
     Keyspace kspace = null;
     CassandraErrorLoggingHandler logHandler = null;
     long now = System.currentTimeMillis();
     String opName = "Operation1";
     Map<String, String> options = null;
     String serverName = "localhost";
     boolean serverSide = true;
     String srvcAdminName = "ServiceAdminName1";
 
     private void cleanUpTestData() {
         String[] columnFamilies = { "ErrorCountsByCategory", "ErrorCountsBySeverity", "ErrorsById", "ErrorValues" };
 
         for (String cf : columnFamilies) {
             RangeSlicesQuery<String, String, String> rq = HFactory.createRangeSlicesQuery(kspace, STR_SERIALIZER,
                             STR_SERIALIZER, STR_SERIALIZER);
             rq.setColumnFamily(cf);
             rq.setRange("", "", false, 1000);
             QueryResult<OrderedRows<String, String, String>> qr = rq.execute();
             OrderedRows<String, String, String> orderedRows = qr.get();
             Mutator<String> deleteMutator = HFactory.createMutator(kspace, STR_SERIALIZER);
             for (Row<String, String, String> row : orderedRows) {
                 deleteMutator.delete(row.getKey(), cf, null, STR_SERIALIZER);
             }
         }
 
     }
 
     public Map<String, String> createRegularOptionsMap() {
         Map<String, String> options = new HashMap<String, String>();
         options.put("cluster-name", "Test Cluster");
         options.put("host-address", IP_ADDRESS);
         options.put("keyspace-name", "TurmericMonitoring");
         options.put("random-generator-class-name", "org.ebayopensource.turmeric.runtime.error.utils.MockRandom");
         return options;
     }
 
     @Before
     public void setUp() {
         errorsToStore = this.createTestCommonErrorDataList(1);
         options = this.createRegularOptionsMap();
         ctx = new MockInitContext(options);
         try {
             logHandler = new CassandraErrorLoggingHandler();
             kspace = new HectorManager().getKeyspace("Test Cluster", IP_ADDRESS, "TurmericMonitoring", "ErrorsById");
         }
         catch (Exception e) {
             e.printStackTrace();
             fail();
         }
     }
 
     @After
     public void tearDown() {
        // this.cleanUpTestData();
         logHandler = null;
         kspace = null;
     }
 
     @Test
     public void testEmptyConstructor() {
         // seems foolish, but I need to make sure we don't remove the default, no param constructor
         logHandler = new CassandraErrorLoggingHandler();
     }
 
     @Test
     public void testInit() throws ServiceException {
         Map<String, String> options = this.createRegularOptionsMap();
         InitContext ctx = new MockInitContext(options);
         logHandler.init(ctx);
         assertEquals("Test Cluster", logHandler.getClusterName());
         assertEquals(IP_ADDRESS, logHandler.getHostAddress());
         assertEquals("TurmericMonitoring", logHandler.getKeyspaceName());
     }
 
     @Test
     public void testInitWithoutRandomGeneratorParam() throws ServiceException {
         Map<String, String> options = this.createRegularOptionsMap();
 
         InitContext ctx = new MockInitContext(options);
         logHandler.init(ctx);
     }
 
     @Test
     public void testPersistErrorCountsByCategoryCF() throws ServiceException {
 
         logHandler.init(ctx);
         logHandler.persistErrors(errorsToStore, serverName, srvcAdminName, opName, serverSide, consumerName, now);
 
         ColumnSlice<Object, Object> categoryCountColumnSlice = this.getColumnValues(kspace, "ErrorCountsByCategory",
                         "localhost|ServiceAdminName1|ConsumerName1|Operation1|APPLICATION|true", LongSerializer.get(),
                         StringSerializer.get(), Long.valueOf(now));
         this.assertValues(categoryCountColumnSlice, now, now + CassandraErrorLoggingHandler.KEY_SEPARATOR + "1");
 
         // now, assert the count cf | all ops
         ColumnSlice<Object, Object> categoryCountAllOpsColumnSlice = this.getColumnValues(kspace,
                         "ErrorCountsByCategory", "localhost|ServiceAdminName1|ConsumerName1|All|APPLICATION|true",
                         LongSerializer.get(), StringSerializer.get(), Long.valueOf(now));
         this.assertValues(categoryCountAllOpsColumnSlice, now, now + CassandraErrorLoggingHandler.KEY_SEPARATOR + "1");
 
     }
 
     @Test
     public void testPersistErrorCountsBySeverityCF() throws ServiceException {
 
         long now = System.currentTimeMillis();
         Map<String, String> options = this.createRegularOptionsMap();
         InitContext ctx = new MockInitContext(options);
         logHandler.init(ctx);
         logHandler.persistErrors(errorsToStore, serverName, srvcAdminName, opName, serverSide, consumerName, now);
 
         // now, assert the count cf | then the severity one
         ColumnSlice<Object, Object> severityCountColumnSlice = this.getColumnValues(kspace, "ErrorCountsBySeverity",
                         "localhost|ServiceAdminName1|ConsumerName1|Operation1|ERROR|true", LongSerializer.get(),
                         StringSerializer.get(), Long.valueOf(now));
         this.assertValues(severityCountColumnSlice, now, now + CassandraErrorLoggingHandler.KEY_SEPARATOR + "1");
 
         // now, assert the count cf | all ops
         ColumnSlice<Object, Object> severityCountAllOpsColumnSlice = this.getColumnValues(kspace,
                         "ErrorCountsBySeverity", "localhost|ServiceAdminName1|ConsumerName1|All|ERROR|true",
                         LongSerializer.get(), StringSerializer.get(), Long.valueOf(now));
         this.assertValues(severityCountAllOpsColumnSlice, now, now + CassandraErrorLoggingHandler.KEY_SEPARATOR + "1");
 
     }
 
     @Test
     public void testPersistErrorsByIdCF() throws ServiceException {
 
         logHandler.init(ctx);
         logHandler.persistErrors(errorsToStore, serverName, srvcAdminName, opName, serverSide, consumerName, now);
 
         // now I need to retrieve the values. I use Hector for this.
         ColumnSlice<Object, Object> errorColumnSlice = this.getColumnValues(kspace, "ErrorsById", ERROR_ID,
                         StringSerializer.get(), StringSerializer.get(), "name", "category", "severity", "domain",
                         "subDomain", "organization");
         this.assertValues(errorColumnSlice, "name", "TestErrorName", "organization", "TestOrganization", "domain",
                         "TestDomain", "subDomain", "TestSubdomain", "severity", "ERROR", "category", "APPLICATION");
 
         ColumnSlice<Object, Object> longColumnSlice = this.getColumnValues(kspace, "ErrorsById", ERROR_ID,
                         StringSerializer.get(), LongSerializer.get(), "errorId");
         this.assertValues(longColumnSlice, "errorId", ERROR_ID);
 
         ColumnSlice<Object, Object> timestampColumnSlice = this.getColumnValues(kspace, "ErrorsById", ERROR_ID,
                         StringSerializer.get(), StringSerializer.get(), now + "");
         this.assertValues(timestampColumnSlice, now + "", now + CassandraErrorLoggingHandler.KEY_SEPARATOR + "1");
 
     }
 
     @Test
     public void testPersistErrorValuesCF() throws ServiceException {
 
         logHandler.init(ctx);
         logHandler.persistErrors(errorsToStore, serverName, srvcAdminName, opName, serverSide, consumerName, now);
 
         // now I need to retrieve the values. I use Hector for this.
         String errorValueKey = now + CassandraErrorLoggingHandler.KEY_SEPARATOR + "1";
         ColumnSlice<Object, Object> errorColumnSlice = this.getColumnValues(kspace, "ErrorValues", errorValueKey,
                         StringSerializer.get(), StringSerializer.get(), "name", "category", "severity", "domain",
                         "subDomain", "organization", "serverName", "errorMessage", "serviceAdminName", "operationName",
                         "consumerName", "serverSide");
 
         this.assertValues(errorColumnSlice, "name", "TestErrorName", "organization", "TestOrganization", "domain",
                         "TestDomain", "subDomain", "TestSubdomain", "severity", "ERROR", "category", "APPLICATION",
                         "serverName", serverName, "errorMessage", "Error Message 0", "serviceAdminName", srvcAdminName,
                         "operationName", opName, "consumerName", consumerName, "serverSide",
                         Boolean.toString(serverSide));
 
         ColumnSlice<Object, Object> longColumnSlice = this.getColumnValues(kspace, "ErrorValues", errorValueKey,
                         StringSerializer.get(), LongSerializer.get(), "errorId", "tstamp", "aggregationPeriod");
         this.assertValues(longColumnSlice, "errorId", ERROR_ID, "tstamp", now, "aggregationPeriod", 0l);
 
     }
 }
