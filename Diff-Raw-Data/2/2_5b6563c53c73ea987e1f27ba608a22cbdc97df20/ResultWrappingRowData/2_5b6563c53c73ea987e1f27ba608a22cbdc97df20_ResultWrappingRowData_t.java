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
 package com.ngdata.hbaseindexer.indexer;
 
 import java.util.List;
 
 import org.apache.hadoop.hbase.KeyValue;
 import org.apache.hadoop.hbase.client.Result;
 
 /**
  * {@code RowData} implementation that directly wraps a Result object that has been
  * read from HBase.
  */
 public class ResultWrappingRowData implements RowData {
     private final Result result;
     private byte[] tableName;
 
     public ResultWrappingRowData(Result result, byte[] tableName) {
         this.result = result;
         this.tableName = tableName;
     }
 
     @Override
     public byte[] getRow() {
         return result.getRow();
     }
 
     @Override
     public List<KeyValue> getKeyValues() {
         return result.list();
     }
 
     @Override
     public Result toResult() {
         return result;
     }
 
     @Override
     public byte[] getTable() {
        return tableName;
     }
 
 }
