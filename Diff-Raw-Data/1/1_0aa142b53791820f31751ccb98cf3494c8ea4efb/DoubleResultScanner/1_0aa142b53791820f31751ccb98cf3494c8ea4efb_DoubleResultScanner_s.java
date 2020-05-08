 package com.nearinfinity.mysqlengine.scanner;
 
 import com.nearinfinity.hbaseclient.Constants;
 import com.nearinfinity.hbaseclient.HBaseClient;
 import com.nearinfinity.hbaseclient.ResultParser;
 import com.nearinfinity.hbaseclient.strategy.PrimaryIndexScanStrategy;
 import com.nearinfinity.hbaseclient.strategy.ScanStrategy;
 import org.apache.hadoop.hbase.client.Result;
 import org.apache.hadoop.hbase.client.ResultScanner;
 import org.apache.hadoop.hbase.util.Bytes;
 import org.apache.log4j.Logger;
 
 import java.io.IOException;
 import java.util.Arrays;
 
 /**
  * Created with IntelliJ IDEA.
  * User: jedstrom
  * Date: 8/21/12
  * Time: 9:17 AM
  * To change this template use File | Settings | File Templates.
  */
 public class DoubleResultScanner implements HBaseResultScanner {
     private Result lastResult;
     private ResultScanner secondaryScanner;
     private final String tableName;
     private final String columnName;
     private final HBaseClient client;
     private ResultScanner primaryScanner;
 
     private static final Logger logger = Logger.getLogger(DoubleResultScanner.class);
 
     public DoubleResultScanner(ResultScanner scanner, String tableName, String columnName, HBaseClient client) {
         this.secondaryScanner = scanner;
         this.tableName = tableName;
         this.columnName = columnName;
         this.client = client;
         this.primaryScanner = null;
         this.lastResult = null;
     }
 
     @Override
     public Result next(byte[] valueToSkip) throws IOException {
         if (this.primaryScanner == null) {
 
             Result result = this.secondaryScanner.next();
             if (result == null) {
                 return null;
             }
 
             byte[] value = ResultParser.parseValue(result);
 
             if (valueToSkip != null && Arrays.equals(value, valueToSkip)) {
                 Result nextResult = secondaryScanner.next();
                 if (nextResult == null) {
                     return null;
                 }
                 value = ResultParser.parseValue(nextResult);
             }
             ScanStrategy strategy = new PrimaryIndexScanStrategy(tableName, columnName, value);
             primaryScanner = client.getScanner(strategy);
         }
 
         Result result = primaryScanner.next();
         while (result == null) {
             Result secondaryResult = secondaryScanner.next();
             if (secondaryResult == null) {
                 return null;
             }
             byte[] value = ResultParser.parseValue(secondaryResult);
             ScanStrategy strategy = new PrimaryIndexScanStrategy(tableName, columnName, value);
             primaryScanner = client.getScanner(strategy);
             result = primaryScanner.next();
         }
         return result;
     }
 
     @Override
     public void close() {
         if (primaryScanner != null) {
             this.primaryScanner.close();
         }
 
         if (this.secondaryScanner != null) {
             this.secondaryScanner.close();
         }
     }
 
     @Override
     public Result getLastResult() {
         return this.lastResult;
     }
 
     @Override
     public void setLastResult(Result result) {
         this.lastResult = result;
     }
 }
