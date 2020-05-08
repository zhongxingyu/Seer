 package com.nearinfinity.mysqlengine;
 
 import org.apache.hadoop.hbase.client.Result;
 import org.apache.hadoop.hbase.client.ResultScanner;
 
 import java.io.IOException;
 import java.nio.ByteBuffer;
 import java.util.*;
 
 /**
  * Created with IntelliJ IDEA.
  * User: jedstrom
  * Date: 7/25/12
  * Time: 2:11 PM
  * To change this template use File | Settings | File Templates.
  */
 public class Driver {
     public static void main(String[] args) throws IOException {
         new Driver().go(args);
     }
 
     public void go(String[] args) throws IOException {
         HBaseClient client = new HBaseClient("sql", "localhost");
 
         if (args[0].equals("create")) {
             //create table_name column*
             String tableName = args[1];
             Map<String, List<ColumnMetadata>> columns = new HashMap<String, List<ColumnMetadata>>();
             for (int i = 2 ; i < args.length ; i++) {
                 List<ColumnMetadata> metadata = new ArrayList<ColumnMetadata>();
                 metadata.add(ColumnMetadata.NONE);
                 columns.put(args[i], metadata);
             }
 
             client.createTableFull(tableName, columns);
         }
         else if (args[0].equals("put")) {
             //put table_name column=value*
             String tableName = args[1];
             Map<String, byte[]> values = new LinkedHashMap<String, byte[]>();
             for (int i = 2; i < args.length ; i++) {
                 String [] tokens = args[i].split("=");
                 values.put(tokens[0], tokens[1].getBytes());
             }
 
             client.writeRow(tableName, values);
         }
         else if (args[0].equals("scan")) {
             //scan table_name
             String tableName = args[1];
 
             List<Map<String, byte[]>> rows = client.fullTableScan(tableName);
 
             StringBuilder sb = new StringBuilder();
             for (Map<String, byte[]> row : rows) {
                 sb.append(printRow(row));
             }
             System.out.println(sb);
         }
         else if (args[0].equals("search")) {
             //search table_name column=value
             String tableName = args[1];
             String [] tokens = args[2].split("=");
 
             ResultScanner scanner = client.search(tableName, tokens[0], tokens[1].getBytes());
             for (Result result : scanner) {
                 UUID uuid = client.parseUUIDFromIndexRow(result);
 
                 Result rowResult = client.getDataRow(uuid, tableName);
                 Map<String, byte[]> parsedRow = client.parseRow(rowResult, tableName);
                 System.out.println(printRow(parsedRow));
             }
         }
         else if (args[0].equals("delete")) {
             //delete table_name column=value
             String tableName = args[1];
             String[] tokens = args[2].split("=");
 
             ResultScanner scanner = client.search(tableName, tokens[0], tokens[1].getBytes());
             for (Result result : scanner) {
                 ByteBuffer rowKey = ByteBuffer.wrap(result.getRow());
                 byte rowType = rowKey.get();
                 long tableId = rowKey.getLong();
                 long columnId = rowKey.getLong();
                 byte[] value = new byte[tokens[1].getBytes().length];
                 rowKey.get(value);
                 UUID uuid = new UUID(rowKey.getLong(), rowKey.getLong());
 
                 byte[] dataRowKey = RowKeyFactory.buildDataKey(tableId, uuid);
 
                client.deleteRow(dataRowKey);
             }
         }
         else if (args[0].equals("compact")) {
             //compact
             client.compact();
         }
     }
 
     private static String printRow(Map<String, byte[]> row) {
         StringBuilder sb = new StringBuilder();
         sb.append("{ ");
         for (String key : row.keySet()) {
             sb.append("\'" + key + "\' => \'" + new String(row.get(key)) + "\', ");
         }
         sb.append("}\n");
         return sb.toString();
     }
 }
