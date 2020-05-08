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
 package com.smartitengineering.dao.hbase.ddl;
 
 import com.smartitengineering.dao.hbase.ddl.config.json.ConfigurationJsonParser;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Collection;
 import java.util.Date;
 import org.apache.commons.lang.StringUtils;
 import org.apache.hadoop.hbase.HBaseTestingUtility;
 import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.client.Get;
 import org.apache.hadoop.hbase.client.HBaseAdmin;
 import org.apache.hadoop.hbase.client.HTable;
 import org.apache.hadoop.hbase.client.Put;
 import org.apache.hadoop.hbase.client.Result;
 import org.apache.hadoop.hbase.client.ResultScanner;
 import org.apache.hadoop.hbase.client.Scan;
 import org.apache.hadoop.hbase.util.Bytes;
 import org.junit.Assert;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  *
  * @author imyousuf
  */
 public class TableGenerationTest {
 
   private static final HBaseTestingUtility TEST_UTIL = new HBaseTestingUtility();
   private static final Logger LOGGER = LoggerFactory.getLogger(TableGenerationTest.class);
 
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
   }
 
   @Test
   public void testTableCreation() throws Exception {
     InputStream classpathResource = getClass().getClassLoader().getResourceAsStream("ddl-config-sample1.json");
     Collection<HBaseTableConfiguration> configs = ConfigurationJsonParser.getConfigurations(classpathResource);
     try {
       new HBaseTableGenerator(configs, TEST_UTIL.getConfiguration(), true).generateTables();
     }
     catch (Exception ex) {
       LOGGER.error("Could not create table!", ex);
       Assert.fail(ex.getMessage());
     }
     HBaseAdmin admin = new HBaseAdmin(TEST_UTIL.getConfiguration());
     for (HBaseTableConfiguration tableConfiguration : configs) {
       Assert.assertTrue(admin.tableExists(tableConfiguration.getTableName()));
       HTableDescriptor descriptor = admin.getTableDescriptor(Bytes.toBytes(tableConfiguration.getTableName()));
       for (int i = 1; i < 4; ++i) {
         Assert.assertTrue(descriptor.hasFamily(Bytes.toBytes("family" + i)));
       }
     }
   }
 
   @Test
   public void testRowIdGeneration() throws IOException {
     HTable table = new HTable(TEST_UTIL.getConfiguration(), "test");
     Put put = new Put(Bytes.toBytes(getPaddedId(1l)));
     final byte[] family = Bytes.toBytes("family2");
     put.add(family, Bytes.toBytes("cell"), Bytes.toBytes(new Date().getTime()));
     table.put(put);
     put = new Put(Bytes.toBytes(getPaddedId(2l)));
     put.add(family, Bytes.toBytes("cell"), Bytes.toBytes(new Date().getTime()));
     table.put(put);
     logResultRowId(table, family);
     for (long l = 30; l > 2; --l) {
       LOGGER.info("Setting Row ID: " + l);
       put = new Put(Bytes.toBytes(getPaddedId(l)));
       put.add(family, Bytes.toBytes("cell"), Bytes.toBytes(new Date().getTime()));
       table.put(put);
     }
     logResultRowId(table, family);
     for (long l = 31; l < 60; ++l) {
       put = new Put(Bytes.toBytes(getPaddedId(l)));
       put.add(family, Bytes.toBytes("cell"), Bytes.toBytes(new Date().getTime()));
       table.put(put);
     }
     logResultRowId(table, family);
     Scan scan = new Scan();
     ResultScanner scanner = table.getScanner(scan);
     Result result[];
     result = scanner.next(1);
     if (result != null && result.length > 0) {
       LOGGER.info("ROW ID: " + Bytes.toString(result[0].getRow()));
     }
   }
 
   protected void logResultRowId(HTable table, final byte[] family) throws IOException {
     ResultScanner scanner = table.getScanner(family);
     logScannerResults(scanner);
   }
 
   protected void logScannerResults(ResultScanner scanner) throws IOException {
     Result result;
     do {
       result = scanner.next();
       if (result != null) {
         LOGGER.info("ROW ID: " + Bytes.toString(result.getRow()));
       }
     }
     while (result != null);
   }
 
   protected String getPaddedId(long longId) {
     return StringUtils.leftPad(String.valueOf(longId), 10, '0');
   }
 }
