 package com.nearinfinity.hbaseclient;
 
 import org.apache.hadoop.hbase.client.HTable;
 import org.apache.hadoop.hbase.client.Put;
 import org.apache.hadoop.hbase.client.Row;
 
 import java.io.IOException;
 import java.util.*;
 
 /**
  * Created with IntelliJ IDEA.
  * User: jedstrom
  * Date: 8/30/12
  * Time: 4:41 PM
  * To change this template use File | Settings | File Templates.
  */
 public class WriteBuffer {
     private Set<byte[]> secondaryIndexRows;
     private Set<byte[]> reverseIndexRows;
     private List<Put> buffer;
     private long bufferSize;
     private long bufferLimit;
     private HTable table;
 
     public WriteBuffer(HTable table) {
         this.table = table;
         this.bufferLimit = 0L;
         reset();
     }
 
     public void setWriteBufferLimit(long bufferLimit) throws IOException {
         this.bufferLimit = bufferLimit;
         if(bufferSize > bufferLimit) {
             flushCommits();
         }
     }
 
     public long getWriteBufferLimit() {
         return this.bufferLimit;
     }
 
     public void put(List<Put> putList) throws IOException {
         for (Put put : putList) {
             byte[] rowKey = put.getRow();
             if (secondaryIndexRows.contains(rowKey)) {
                 continue;
             }
             else if (reverseIndexRows.contains(rowKey)) {
                 continue;
             }
 
             if (RowKeyFactory.isSecondaryIndexKey(rowKey)) {
                 secondaryIndexRows.add(rowKey);
             }
             else if (RowKeyFactory.isReverseIndexKey(rowKey)) {
                 reverseIndexRows.add(rowKey);
             }
 
             buffer.add(put);
             bufferSize += put.heapSize();
         }
 
        if (bufferSize > bufferLimit) {
             this.flushCommits();
         }
     }
 
     public void put(Put put) throws IOException {
         this.put(Arrays.asList(put));
     }
 
     public void flushCommits() throws IOException {
         this.table.put(buffer);
         this.table.flushCommits();
         reset();
     }
 
     private void reset() {
         this.secondaryIndexRows = new HashSet<byte[]>();
         this.reverseIndexRows = new HashSet<byte[]>();
         this.buffer = new LinkedList<Put>();
         this.bufferSize = 0L;
     }
 }
