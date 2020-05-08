 /**
  * Copyright 2010 Sematext International
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
 package com.sematext.hbase.hut;
 
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.hbase.HBaseTestingUtility;
 import org.apache.hadoop.hbase.client.Delete;
 import org.apache.hadoop.hbase.client.HTable;
 import org.apache.hadoop.hbase.client.Put;
 import org.apache.hadoop.hbase.client.Result;
 import org.apache.hadoop.hbase.client.ResultScanner;
 import org.apache.hadoop.hbase.client.Scan;
 import org.apache.hadoop.hbase.util.Bytes;
 import org.apache.hadoop.mapreduce.Job;
 import org.junit.After;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 
 import java.io.IOException;
 import java.util.Arrays;
 
 /**
  * General unit-test for the whole concept
  * TODO: split into multiple classes
  */
 public class TestHBaseHut {
   public static final byte[] SALE_CF = Bytes.toBytes("sale");
   private static final String TABLE_NAME = "stock-market";
   private static final byte[] CHRYSLER = Bytes.toBytes("chrysler");
   private static final byte[] FORD = Bytes.toBytes("ford");
   private static final byte[] TOYOTA = Bytes.toBytes("toyota");
   private HBaseTestingUtility testingUtility;
   private HTable hTable;
 
   @Before
   public void before() throws Exception {
     testingUtility = new HBaseTestingUtility();
     testingUtility.startMiniZKCluster();
     testingUtility.startMiniCluster(1);
     hTable = testingUtility.createTable(Bytes.toBytes(TABLE_NAME), SALE_CF);
   }
 
   @After
   public void after() throws IOException {
     hTable = null;
     testingUtility.shutdownMiniCluster();
     testingUtility.shutdownMiniZKCluster();
     testingUtility = null;
   }
 
   // Stores only last 5 stock prices
   static class StockSaleUpdateProcessor extends UpdateProcessor {
     @Override
     public void process(Iterable<Result> records, UpdateProcessingResult processingResult) {
       // Processing records
       byte[][] lastPricesBuff = new byte[5][];
       int lastIndex = -1;
       for (Result record : records) {
         for (int i = 0; i < 5; i++) {
           // "lastPrice0" is the most recent one, hence should be added as last
           byte[] price = getPrice(record, "lastPrice" + (4 - i));
           lastIndex = addPrice(lastPricesBuff, price, lastIndex);
         }
       }
 
       // Writing result
       if (lastIndex == -1) { // nothing to output
         return;
       }
      for (int i = 0; i < 5; i++) {
         // iterating backwards so that "lastPrice0" is set to the most recent one
         int index = (lastIndex + 5 - i) % 5;
         byte[] price = lastPricesBuff[index];
         if (price != null) {
           processingResult.add(SALE_CF, Bytes.toBytes("lastPrice" + i), price);
         }
       }
     }
 
     public int addPrice(byte[][] lastPricesBuff, byte[] price, int lastIndex) {
       if (price == null) {
         return lastIndex;
       }
       lastIndex++;
       if (lastIndex > lastPricesBuff.length - 1) {
         lastIndex = 0;
       }
       lastPricesBuff[lastIndex] = price;
       return lastIndex;
     }
 
     private byte[] getPrice(Result result, String priceQualifier) {
       return result.getValue(SALE_CF, Bytes.toBytes(priceQualifier));
     }
   }
 
   @Test
   public void testSimpleScan() throws IOException, InterruptedException {
     StockSaleUpdateProcessor processor = new StockSaleUpdateProcessor();
 
     // Writing data
     verifyLastSales(hTable, processor, CHRYSLER, new int[] {});
     verifyLastSales(hTable, processor, FORD, new int[] {});
     recordSale(hTable, CHRYSLER, 90);
     recordSale(hTable, CHRYSLER, 100);
     recordSale(hTable, FORD, 18);
     recordSale(hTable, CHRYSLER, 120);
     verifyLastSales(hTable, processor, CHRYSLER, new int[] {120, 100, 90});
     verifyLastSales(hTable, processor, FORD, new int[] {18});
 
     recordSale(hTable, CHRYSLER, 115);
     recordSale(hTable, FORD, 22);
     recordSale(hTable, CHRYSLER, 110);
     verifyLastSales(hTable, processor, CHRYSLER, new int[] {110, 115, 120, 100, 90});
     verifyLastSales(hTable, processor, FORD, new int[] {22, 18});
 
     recordSale(hTable, CHRYSLER, 105);
     recordSale(hTable, FORD, 24);
     recordSale(hTable, FORD, 28);
     verifyLastSales(hTable, processor, CHRYSLER, new int[] {105, 110, 115, 120, 100});
     verifyLastSales(hTable, processor, FORD, new int[] {28, 24, 22, 18});
 
     recordSale(hTable, CHRYSLER, 107);
     recordSale(hTable, FORD, 32);
     recordSale(hTable, FORD, 40);
     recordSale(hTable, CHRYSLER, 113);
     verifyLastSales(hTable, processor, CHRYSLER, new int[] {113, 107, 105, 110, 115});
     verifyLastSales(hTable, processor, FORD, new int[] {40, 32, 28, 24, 22});
 
     hTable.close();
   }
 
   @Test
   public void testSimpleScanWithCaching() throws IOException, InterruptedException {
     StockSaleUpdateProcessor processor = new StockSaleUpdateProcessor();
 
     // Writing data
     verifyLastSalesForAllCompanies(hTable, processor, new int[][] {{}, {}});
     recordSale(hTable, CHRYSLER, 90);
     recordSale(hTable, CHRYSLER, 100);
     recordSale(hTable, FORD, 18);
     recordSale(hTable, CHRYSLER, 120);
     verifyLastSalesForAllCompanies(hTable, processor, new int[][] {{120, 100, 90}, {18}, {}});
 
     recordSale(hTable, TOYOTA, 202);
     recordSale(hTable, CHRYSLER, 115);
     recordSale(hTable, TOYOTA, 212);
     recordSale(hTable, TOYOTA, 204);
     recordSale(hTable, FORD, 22);
     recordSale(hTable, CHRYSLER, 110);
     verifyLastSalesForAllCompanies(hTable, processor, new int[][] {{110, 115, 120, 100, 90}, {22, 18}, {204, 212, 202}});
 
     recordSale(hTable, CHRYSLER, 105);
     recordSale(hTable, FORD, 24);
     recordSale(hTable, FORD, 28);
     verifyLastSalesForAllCompanies(hTable, processor, new int[][] {{105, 110, 115, 120, 100}, {28, 24, 22, 18}, {204, 212, 202}});
 
     recordSale(hTable, CHRYSLER, 107);
     recordSale(hTable, FORD, 32);
     recordSale(hTable, FORD, 40);
     recordSale(hTable, TOYOTA, 224);
     recordSale(hTable, CHRYSLER, 113);
     verifyLastSalesForAllCompanies(hTable, processor, new int[][] {{113, 107, 105, 110, 115}, {40, 32, 28, 24, 22}, {224, 204, 212, 202}});
 
     hTable.close();
   }
 
   @Test
   public void testStoringProcessedUpdatesDuringScan() throws IOException, InterruptedException {
     StockSaleUpdateProcessor processor = new StockSaleUpdateProcessor();
 
     // Writing data
     recordSale(hTable, CHRYSLER, 90);
     recordSale(hTable, CHRYSLER, 100);
     recordSale(hTable, FORD, 18);
     recordSale(hTable, CHRYSLER, 120);
     recordSale(hTable, CHRYSLER, 115);
     recordSale(hTable, FORD, 22);
     recordSale(hTable, CHRYSLER, 110);
 
     verifyLastSalesWithCompation(hTable, processor, CHRYSLER, new int[]{110, 115, 120, 100, 90});
     verifyLastSalesWithCompation(hTable, processor, FORD, new int[]{22, 18});
     // after processed updates has been stored, "native" HBase scanner should return processed results too
     verifyLastSalesWithNativeScanner(hTable, CHRYSLER, new int[]{110, 115, 120, 100, 90});
     verifyLastSalesWithNativeScanner(hTable, FORD, new int[]{22, 18});
 
     recordSale(hTable, CHRYSLER, 105);
     recordSale(hTable, FORD, 24);
     recordSale(hTable, FORD, 28);
     recordSale(hTable, CHRYSLER, 107);
     recordSale(hTable, FORD, 32);
     recordSale(hTable, FORD, 40);
     recordSale(hTable, CHRYSLER, 113);
 
     verifyLastSalesWithCompation(hTable, processor, CHRYSLER, new int[]{113, 107, 105, 110, 115});
     verifyLastSalesWithCompation(hTable, processor, FORD, new int[]{40, 32, 28, 24, 22});
     // after processed updates has been stored, "native" HBase scanner should return processed results too
     verifyLastSalesWithNativeScanner(hTable, CHRYSLER, new int[]{113, 107, 105, 110, 115});
     verifyLastSalesWithNativeScanner(hTable, FORD, new int[]{40, 32, 28, 24, 22});
 
     hTable.close();
   }
 
   @Test
   public void testUpdatesProcessingUtil() throws IOException, InterruptedException {
     StockSaleUpdateProcessor processor = new StockSaleUpdateProcessor();
 
     // Writing data
     recordSale(hTable, CHRYSLER, 90);
     recordSale(hTable, CHRYSLER, 100);
     recordSale(hTable, FORD, 18);
     recordSale(hTable, CHRYSLER, 120);
     recordSale(hTable, CHRYSLER, 115);
     recordSale(hTable, FORD, 22);
     recordSale(hTable, CHRYSLER, 110);
 
     UpdatesProcessingUtil.processUpdates(hTable, processor);
     // after processed updates has been stored, "native" HBase scanner should return processed results too
     verifyLastSalesWithNativeScanner(hTable, CHRYSLER, new int[]{110, 115, 120, 100, 90});
     verifyLastSalesWithNativeScanner(hTable, FORD, new int[]{22, 18});
 
     recordSale(hTable, CHRYSLER, 105);
     recordSale(hTable, FORD, 24);
     recordSale(hTable, FORD, 28);
     recordSale(hTable, CHRYSLER, 107);
     recordSale(hTable, FORD, 32);
     recordSale(hTable, FORD, 40);
     recordSale(hTable, CHRYSLER, 113);
 
     UpdatesProcessingUtil.processUpdates(hTable, processor);
     UpdatesProcessingUtil.processUpdates(hTable, processor); // updates processing can be performed when no new data was added
     // after processed updates has been stored, "native" HBase scanner should return processed results too
     verifyLastSalesWithNativeScanner(hTable, CHRYSLER, new int[]{113, 107, 105, 110, 115});
     verifyLastSalesWithNativeScanner(hTable, FORD, new int[]{40, 32, 28, 24, 22});
 
     hTable.close();
   }
 
   // This test verifies that updates processing can be done on any interval(s) of written data separately,
   // thus updates processing can be performed on per-region basis without breaking things
   @Test
   public void testProcessingUpdatesInSeparateIntervals() throws IOException, InterruptedException {
     StockSaleUpdateProcessor processor = new StockSaleUpdateProcessor();
 
     // Writing data
     recordSale(hTable, CHRYSLER, 90);
     recordSale(hTable, CHRYSLER, 100);
     recordSale(hTable, FORD, 18);
     recordSale(hTable, CHRYSLER, 120);
     recordSale(hTable, CHRYSLER, 115);
     recordSale(hTable, FORD, 22);
     recordSale(hTable, CHRYSLER, 110);
 
     performUpdatesProcessingButWithoutDeletionOfProcessedRecords(hTable, processor);
     verifyLastSales(hTable, processor, CHRYSLER, new int[]{110, 115, 120, 100, 90});
     verifyLastSales(hTable, processor, FORD, new int[] {22, 18});
 
     recordSale(hTable, CHRYSLER, 105);
     recordSale(hTable, FORD, 24);
 
     performUpdatesProcessingButWithoutDeletionOfProcessedRecords(hTable, processor);
     verifyLastSales(hTable, processor, CHRYSLER, new int[]{105, 110, 115, 120, 100});
     verifyLastSales(hTable, processor, FORD, new int[]{24, 22, 18});
 
     hTable.close();
   }
 
   // TODO: add test for writing with simple scan as apposed to MR job
   @Test
   public void testMinRecordsToCompact() throws IOException, InterruptedException, ClassNotFoundException {
     StockSaleUpdateProcessor processor = new StockSaleUpdateProcessor();
 
     // Writing data
     recordSale(hTable, CHRYSLER, 90);
     recordSale(hTable, CHRYSLER, 100);
     recordSale(hTable, FORD, 18);
     recordSale(hTable, CHRYSLER, 120);
     recordSale(hTable, CHRYSLER, 115);
     recordSale(hTable, FORD, 22);
     recordSale(hTable, CHRYSLER, 110);
     recordSale(hTable, TOYOTA, 2);
 
     processUpdatesWithMrJob(1000, 2 * 1024 * 1024, 3);
 
     // 4 is 2 non-compacted FORD records + 1 compacted CHRYSLER + 1 TOYOTA
     assertRecordsCount(hTable, 4);
     verifyLastSales(hTable, processor, CHRYSLER, new int[] {110, 115, 120, 100, 90});
     verifyLastSales(hTable, processor, FORD, new int[]{22, 18});
 
     recordSale(hTable, TOYOTA, 5);
     recordSale(hTable, CHRYSLER, 105);
     recordSale(hTable, FORD, 24);
 
     processUpdatesWithMrJob(1000, 2 * 1024 * 1024, 3);
 
     // 5 is 2 non-compacted TOYOTA records + 2 non-compacted CHRYSLER + 1 compacted FORD
     assertRecordsCount(hTable, 5);
     verifyLastSales(hTable, processor, CHRYSLER, new int[]{105, 110, 115, 120, 100});
     verifyLastSales(hTable, processor, FORD, new int[]{24, 22, 18});
 
     hTable.close();
   }
 
   private void assertRecordsCount(HTable hTable, int count) throws IOException {
     ResultScanner rs = hTable.getScanner(new Scan());
     int actualCount = 0;
     for (Result r : rs) {
       actualCount++;
     }
 
     Assert.assertEquals("Records count check", count, actualCount);
   }
 
   // TODO: add more test-cases for MR job
   @Test
   public void testUpdatesProcessingMrJob() throws IOException, InterruptedException, ClassNotFoundException {
     try {
       int mapBufferSize = 10;
       writeFordAndChryslerData();
       processUpdatesWithMrJob(mapBufferSize, 130);  // in bytes
       // NOTE: verifying with compaction because  records were only partially
       // compacted due to small mapBufferSize in bytes
       verifyLastFordAndChryslerSalesWithCompation(new StockSaleUpdateProcessor());
       // TODO: verify that at least smth was compacted
 
       int twoMegabytes = 2 * 1024 * 1024;
       for (mapBufferSize = 1; mapBufferSize < 6; mapBufferSize++) {
         System.out.println("mapBufferSize: " + mapBufferSize);
         DebugUtil.clean(hTable);
         writeFordAndChryslerData();
         processUpdatesWithMrJob(mapBufferSize, twoMegabytes);
         verifyLastFordAndChryslerSalesWithNativeScanner();
       }
 
       mapBufferSize = 10;
       DebugUtil.clean(hTable);
       writeFordAndChryslerData();
       processUpdatesWithMrJob(mapBufferSize, twoMegabytes);
       verifyLastFordAndChryslerSalesWithNativeScanner();
 
       mapBufferSize = 20;
       DebugUtil.clean(hTable);
       writeFordAndChryslerData();
       processUpdatesWithMrJob(mapBufferSize, twoMegabytes);
       verifyLastFordAndChryslerSalesWithNativeScanner();
 
       // Testing one more time now with record in the end of table which is not compacted
       DebugUtil.clean(hTable);
       writeFordAndChryslerData();
       recordSale(hTable, TOYOTA, 23);
       processUpdatesWithMrJob(4, twoMegabytes);
       verifyLastFordAndChryslerSalesWithNativeScanner();
       verifyLastSalesWithNativeScanner(hTable, TOYOTA, new int[] {23});
 
 
     } finally { // TODO: do we really need try/finally block here?
       testingUtility.shutdownMiniMapReduceCluster();
     }
   }
 
   private void processUpdatesWithMrJob(int mapBufferSize, long mapBufferSizeInBytes) throws IOException, InterruptedException, ClassNotFoundException {
     processUpdatesWithMrJob(mapBufferSize, mapBufferSizeInBytes, 2);
   }
 
   private void processUpdatesWithMrJob(int mapBufferSize, long mapBufferSizeInBytes, int minRecordsToCompact) throws IOException, InterruptedException, ClassNotFoundException {
     System.out.println(DebugUtil.getContentAsText(hTable));
 
     Configuration configuration = testingUtility.getConfiguration();
     configuration.set("hut.mr.buffer.size", String.valueOf(mapBufferSize));
     configuration.set("hut.mr.buffer.size.bytes", String.valueOf(mapBufferSizeInBytes));
     configuration.set("hut.processor.minRecordsToCompact", String.valueOf(minRecordsToCompact));
     Job job = new Job(configuration);
     UpdatesProcessingMrJob.initJob(TABLE_NAME, new Scan(), StockSaleUpdateProcessor.class, job);
 
     boolean success = job.waitForCompletion(true);
     Assert.assertTrue(success);
 
     System.out.println(DebugUtil.getContentAsText(hTable));
   }
 
   private void writeFordAndChryslerData() throws InterruptedException, IOException {
     for (int i = 0; i < 15; i++) {
       byte[] company;
       if (i % 2 == 0) {
         company = FORD;
       } else {
         company = CHRYSLER;
       }
 
       recordSale(hTable, company, i);
     }
   }
 
   private void verifyLastFordAndChryslerSalesWithCompation(UpdateProcessor updateProcessor) throws IOException {
     verifyLastSalesWithCompation(hTable, updateProcessor, FORD, new int[]{14, 12, 10, 8, 6});
     verifyLastSalesWithCompation(hTable, updateProcessor, CHRYSLER, new int[]{13, 11, 9, 7, 5});
   }
 
   private void verifyLastFordAndChryslerSalesWithNativeScanner() throws IOException {
     verifyLastSalesWithNativeScanner(hTable, FORD, new int[] {14, 12, 10, 8, 6});
     verifyLastSalesWithNativeScanner(hTable, CHRYSLER, new int[] {13, 11, 9, 7, 5});
   }
 
   // TODO: add more test-cases for MR job
   @Test
   public void testPartialUpdatesProcessingMrJob() throws IOException, InterruptedException, ClassNotFoundException {
     try {
       // Writing data
       for (int i = 0; i < 15; i++) {
         byte[] company;
         if (i % 2 == 0) {
           company = FORD;
         } else {
           company = CHRYSLER;
         }
 
         recordSale(hTable, company, i);
 
         Thread.sleep(200);
       }
 
       recordSale(hTable, TOYOTA, 23);
 
       System.out.println(DebugUtil.getContentAsText(hTable));
 
       Configuration configuration = testingUtility.getConfiguration();
       configuration.set("hut.mr.buffer.size", String.valueOf(10));
       configuration.set("hut.processor.tsMod", String.valueOf(300));
       Job job = new Job(configuration);
       UpdatesProcessingMrJob.initJob(TABLE_NAME, new Scan(), StockSaleUpdateProcessor.class, job);
 
       boolean success = job.waitForCompletion(true);
       Assert.assertTrue(success);
 
       // TODO: add code verification of proper partial compaction instead of manually observing in output
       System.out.println(DebugUtil.getContentAsText(hTable));
 
       StockSaleUpdateProcessor updateProcessor = new StockSaleUpdateProcessor();
       verifyLastSalesWithCompation(hTable, updateProcessor, FORD, new int[]{14, 12, 10, 8, 6});
       verifyLastSalesWithCompation(hTable, updateProcessor, CHRYSLER, new int[]{13, 11, 9, 7, 5});
       verifyLastSalesWithCompation(hTable, updateProcessor, TOYOTA, new int[]{23});
 
     } finally { // TODO: do we really need try/finally block here?
       testingUtility.shutdownMiniMapReduceCluster();
     }
   }
 
   @Test
   public void testDelete() throws IOException, InterruptedException {
     StockSaleUpdateProcessor processor = new StockSaleUpdateProcessor();
 
     // Writing data
     Delete deleteChrysler = new Delete(CHRYSLER);
 
     // Verifying that delete operation succeeds when no data exists
     hTable.delete(deleteChrysler);
 
     verifyLastSales(hTable, processor, CHRYSLER, new int[] {});
     verifyLastSales(hTable, processor, FORD, new int[] {});
     recordSale(hTable, CHRYSLER, 90);
     recordSale(hTable, CHRYSLER, 100);
     recordSale(hTable, FORD, 18);
     recordSale(hTable, CHRYSLER, 120);
 
     HutUtil.delete(hTable, deleteChrysler);
     verifyLastSales(hTable, processor, CHRYSLER, new int[] {});
     verifyLastSales(hTable, processor, FORD, new int[] {18});
 
     recordSale(hTable, CHRYSLER, 115);
     recordSale(hTable, FORD, 22);
     recordSale(hTable, CHRYSLER, 110);
     verifyLastSales(hTable, processor, CHRYSLER, new int[] {110, 115});
     verifyLastSales(hTable, processor, FORD, new int[] {22, 18});
 
     hTable.close();
   }
 
   @Test
   public void testRollback() throws IOException, InterruptedException {
     StockSaleUpdateProcessor processor = new StockSaleUpdateProcessor();
 
     // Writing data
     verifyLastSales(hTable, processor, CHRYSLER, new int[] {});
     verifyLastSales(hTable, processor, FORD, new int[] {});
     recordSale(hTable, CHRYSLER, 90);
     recordSale(hTable, CHRYSLER, 100);
     recordSale(hTable, FORD, 18);
     recordSale(hTable, CHRYSLER, 120);
     long ts0 = System.currentTimeMillis();
 
     verifyLastSales(hTable, processor, CHRYSLER, new int[] {120, 100, 90});
     verifyLastSales(hTable, processor, FORD, new int[] {18});
 
     recordSale(hTable, CHRYSLER, 115);
     recordSale(hTable, FORD, 22);
     recordSale(hTable, CHRYSLER, 110);
     long ts1 = System.currentTimeMillis();
 
     verifyLastSales(hTable, processor, CHRYSLER, new int[] {110, 115, 120, 100, 90});
     verifyLastSales(hTable, processor, FORD, new int[] {22, 18});
 
 
     recordSale(hTable, CHRYSLER, 105);
     recordSale(hTable, FORD, 24);
     recordSale(hTable, FORD, 28);
     long ts2 = System.currentTimeMillis();
 
     verifyLastSales(hTable, processor, CHRYSLER, new int[] {105, 110, 115, 120, 100});
     verifyLastSales(hTable, processor, FORD, new int[] {28, 24, 22, 18});
 
     recordSale(hTable, CHRYSLER, 107);
     recordSale(hTable, FORD, 32);
     recordSale(hTable, FORD, 40);
     recordSale(hTable, CHRYSLER, 113);
     long ts3 = System.currentTimeMillis();
 
     verifyLastSales(hTable, processor, CHRYSLER, new int[] {113, 107, 105, 110, 115});
     verifyLastSales(hTable, processor, FORD, new int[] {40, 32, 28, 24, 22});
 
     // performing rollbacks to ts1 and then to ts2
     UpdatesProcessingUtil.rollbackWrittenAfter(hTable, ts2);
     verifyLastSales(hTable, processor, CHRYSLER, new int[] {105, 110, 115, 120, 100});
     verifyLastSales(hTable, processor, FORD, new int[] {28, 24, 22, 18});
 
     UpdatesProcessingUtil.rollbackWrittenAfter(hTable, ts1);
     verifyLastSales(hTable, processor, CHRYSLER, new int[] {110, 115, 120, 100, 90});
     verifyLastSales(hTable, processor, FORD, new int[] {22, 18});
 
     // writing more data and rolling back to ts0
     recordSale(hTable, CHRYSLER, 105);
     recordSale(hTable, FORD, 24);
     recordSale(hTable, FORD, 28);
     verifyLastSales(hTable, processor, CHRYSLER, new int[]{105, 110, 115, 120, 100});
     verifyLastSales(hTable, processor, FORD, new int[] {28, 24, 22, 18});
 
     System.out.println(DebugUtil.getContentAsText(hTable));
 
     UpdatesProcessingUtil.rollbackWrittenBetween(hTable, ts0, ts3);
     verifyLastSales(hTable, processor, CHRYSLER, new int[] {105, 120, 100, 90});
     verifyLastSales(hTable, processor, FORD, new int[]{28, 24, 18});
 
     hTable.close();
   }
 
   @Test
   public void testRollbackWithMr() throws IOException, InterruptedException, ClassNotFoundException {
     StockSaleUpdateProcessor processor = new StockSaleUpdateProcessor();
 
     // Writing data
     verifyLastSales(hTable, processor, CHRYSLER, new int[] {});
     verifyLastSales(hTable, processor, FORD, new int[] {});
     recordSale(hTable, CHRYSLER, 90);
     recordSale(hTable, CHRYSLER, 100);
     recordSale(hTable, FORD, 18);
     recordSale(hTable, CHRYSLER, 120);
     long ts0 = System.currentTimeMillis();
 
     verifyLastSales(hTable, processor, CHRYSLER, new int[] {120, 100, 90});
     verifyLastSales(hTable, processor, FORD, new int[] {18});
 
     recordSale(hTable, CHRYSLER, 115);
     recordSale(hTable, FORD, 22);
     recordSale(hTable, CHRYSLER, 110);
     long ts1 = System.currentTimeMillis();
 
     verifyLastSales(hTable, processor, CHRYSLER, new int[] {110, 115, 120, 100, 90});
     verifyLastSales(hTable, processor, FORD, new int[] {22, 18});
 
     recordSale(hTable, CHRYSLER, 105);
     recordSale(hTable, FORD, 24);
     recordSale(hTable, FORD, 28);
     long ts2 = System.currentTimeMillis();
 
     verifyLastSales(hTable, processor, CHRYSLER, new int[] {105, 110, 115, 120, 100});
     verifyLastSales(hTable, processor, FORD, new int[] {28, 24, 22, 18});
 
     recordSale(hTable, CHRYSLER, 107);
     recordSale(hTable, FORD, 32);
     recordSale(hTable, FORD, 40);
     recordSale(hTable, CHRYSLER, 113);
     verifyLastSales(hTable, processor, CHRYSLER, new int[] {113, 107, 105, 110, 115});
     verifyLastSales(hTable, processor, FORD, new int[] {40, 32, 28, 24, 22});
 
     System.out.println(DebugUtil.getContentAsText(hTable));
 
     // rolling back writes from ts1 to ts2
     Job job = RollbackUpdatesMrJob.createSubmittableJob(testingUtility.getConfiguration(),
                                                         new String[] {TABLE_NAME,
                                                                 String.valueOf(ts1), String.valueOf(ts2)});
     boolean success = job.waitForCompletion(true);
     Assert.assertTrue(success);
 
     System.out.println(DebugUtil.getContentAsText(hTable));
 
     verifyLastSales(hTable, processor, CHRYSLER, new int[] {113, 107, 110, 115, 120});
     verifyLastSales(hTable, processor, FORD, new int[] {40, 32, 22, 18});
 
     // writing more data and rolling back to ts0
     recordSale(hTable, CHRYSLER, 105);
     recordSale(hTable, FORD, 24);
     recordSale(hTable, FORD, 28);
     verifyLastSales(hTable, processor, CHRYSLER, new int[]{105, 113, 107, 110, 115});
     verifyLastSales(hTable, processor, FORD, new int[] {28, 24, 40, 32, 22});
 
     job = RollbackUpdatesMrJob.createSubmittableJob(testingUtility.getConfiguration(),
             new String[]{TABLE_NAME, String.valueOf(ts0)});
     success = job.waitForCompletion(true);
     Assert.assertTrue(success);
 
     verifyLastSales(hTable, processor, CHRYSLER, new int[] {120, 100, 90});
     verifyLastSales(hTable, processor, FORD, new int[]{18});
 
     hTable.close();
   }
 
   // TODO: this is the simplest test for BufferedHutPutWriter, add more
   @Test
   public void testBufferedHutPutWriter() throws IOException, InterruptedException {
     StockSaleUpdateProcessor processor = new StockSaleUpdateProcessor();
     BufferedHutPutWriter writer = new BufferedHutPutWriter(hTable, processor, 6);
 
     // Writing data
     writer.write(createPut(CHRYSLER, 90));
     writer.write(createPut(CHRYSLER, 100));
     writer.write(createPut(FORD, 18));
     writer.write(createPut(CHRYSLER, 120));
     writer.write(createPut(CHRYSLER, 115));
     writer.write(createPut(FORD, 22));
     writer.write(createPut(CHRYSLER, 110));
 
     // writer should process updates for first group of records
     // after processed updates has been stored, "native" HBase scanner should return processed results too
     verifyLastSalesWithNativeScanner(hTable, CHRYSLER, new int[] {110, 115, 120, 100, 90});
 
     writer.write(createPut(CHRYSLER, 105));
     writer.write(createPut(FORD, 24));
     writer.write(createPut(FORD, 28));
     writer.write(createPut(CHRYSLER, 107));
     writer.write(createPut(FORD, 32));
 
     verifyLastSalesWithNativeScanner(hTable, FORD, new int[] {32, 28, 24, 22, 18});
 
     writer.write(createPut(FORD, 40));
     writer.write(createPut(CHRYSLER, 113));
 
     writer.flush();
 
     // we use compaction since updates were partially compacted by writer
     // (since its max buffer size is less then total puts #)
     verifyLastSalesWithCompation(hTable, processor, CHRYSLER, new int[] {113, 107, 105, 110, 115});
     verifyLastSalesWithCompation(hTable, processor, FORD, new int[] {40, 32, 28, 24, 22});
 
     UpdatesProcessingUtil.processUpdates(hTable, processor);
     // after processed updates has been stored, "native" HBase scanner should return processed results too
     verifyLastSalesWithCompation(hTable, processor, CHRYSLER, new int[] {113, 107, 105, 110, 115});
     verifyLastSalesWithCompation(hTable, processor, FORD, new int[] {40, 32, 28, 24, 22});
 
     hTable.close();
   }
 
   private static void performUpdatesProcessingButWithoutDeletionOfProcessedRecords(final HTable hTable, UpdateProcessor updateProcessor) throws IOException {
     ResultScanner resultScanner =
             new HutResultScanner(hTable.getScanner(new Scan()), updateProcessor, hTable, true) {
               @Override
               void deleteProcessedRecords(byte[] firstInclusive, byte[] lastInclusive, byte[] processingResultToLeave) throws IOException {
               }
             };
     while (resultScanner.next() != null) {
       // DO NOTHING
     }
   }
 
   private static void recordSale(HTable hTable, byte[] company, int price) throws InterruptedException, IOException {
     Put put = createPut(company, price);
     Thread.sleep(1); // sanity interval
     hTable.put(put);
   }
 
   private static HutPut createPut(byte[] company, int price) {
     HutPut put = new HutPut(company);
     put.add(SALE_CF, Bytes.toBytes("lastPrice0"), Bytes.toBytes(price));
     return put;
   }
 
   private static void verifyLastSalesWithNativeScanner(HTable hTable, byte[] company, int[] prices) throws IOException {
     ResultScanner resultScanner = hTable.getScanner(getCompanyScan(company));
     Result result = resultScanner.next();
     verifyLastSales(result, prices);
   }
 
   private static void verifyLastSales(HTable hTable, UpdateProcessor updateProcessor, byte[] company, int[] prices) throws IOException {
     ResultScanner scanner = hTable.getScanner(getCompanyScan(company));
     ResultScanner resultScanner =
             new HutResultScanner(scanner, updateProcessor);
     Result result = resultScanner.next();
     verifyLastSales(result, prices);
   }
 
   private static Scan getCompanyScan(byte[] company) {
     byte[] stopRow = Arrays.copyOf(company, company.length);
     stopRow[stopRow.length - 1] = (byte) (stopRow[stopRow.length - 1] + 1);
     // setting stopRow to fetch exactly the company needed, otherwise if company's data is absent scan will go further to the next one
     return new Scan(company, stopRow);
   }
 
   // pricesList - prices for all companies ordered alphabetically
   private static void verifyLastSalesForAllCompanies(HTable hTable, UpdateProcessor updateProcessor, int[][] pricesList) throws IOException {
     Scan scan = new Scan();
     scan.setCaching(4);
     ResultScanner resultScanner = new HutResultScanner(hTable.getScanner(scan), updateProcessor);
     Result[] results = resultScanner.next(pricesList.length);
     for (int i = 0; i < pricesList.length; i++) {
       verifyLastSales(results.length > i ? results[i] : null, pricesList[i]);
     }
   }
   private static void verifyLastSalesWithCompation(HTable hTable, UpdateProcessor updateProcessor, byte[] company, int[] prices) throws IOException {
     ResultScanner resultScanner =
             new HutResultScanner(hTable.getScanner(getCompanyScan(company)), updateProcessor, hTable, true);
     Result result = resultScanner.next();
     verifyLastSales(result, prices);
   }
 
   private static void verifyLastSales(Result result, int[] prices) {
     // if there's no records yet, then prices are empty
     if (result == null) {
       Assert.assertTrue(prices.length == 0);
       return;
     }
 
     for (int i = 0; i < 5; i++) {
       byte[] lastStoredPrice = result.getValue(SALE_CF, Bytes.toBytes("lastPrice" + i));
       if (i < prices.length) {
         Assert.assertEquals(prices[i], Bytes.toInt(lastStoredPrice));
       } else {
         Assert.assertNull(lastStoredPrice);
       }
     }
   }
 }
