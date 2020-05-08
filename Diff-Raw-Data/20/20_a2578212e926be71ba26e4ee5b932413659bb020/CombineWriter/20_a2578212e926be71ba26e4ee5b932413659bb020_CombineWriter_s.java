 /*
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.huahinframework.core.writer;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.apache.hadoop.mapreduce.Reducer;
 import org.apache.hadoop.mapreduce.TaskInputOutputContext;
 import org.huahinframework.core.Summarizer;
 import org.huahinframework.core.io.Key;
 import org.huahinframework.core.io.Record;
 import org.huahinframework.core.io.Value;
 
 /**
  * CombineWriter writes when flush is called in Writer for In-Mapper Combine.
  */
 public class CombineWriter implements Writer {
     private Record defaultRecord;
 
     @SuppressWarnings("rawtypes")
     private TaskInputOutputContext context;
 
     private Map<Key, List<Value>> buffer = new HashMap<Key, List<Value>>();
 
     private Summarizer summarizer;
 
     private int cache;
 
     private int flushCount = 0;
 
     /**
      * @param clazz
      * @param cache
      * @throws InstantiationException
      * @throws IllegalAccessException
      */
     public CombineWriter(Class<? extends Reducer<?, ?, ?, ?>> clazz, int cache)
             throws InstantiationException, IllegalAccessException {
         this.summarizer = (Summarizer) clazz.newInstance();
         this.cache = cache;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void write(Record record)
             throws IOException, InterruptedException {
         if (record.isKeyEmpty()) {
             record.setKey(defaultRecord.getKey());
         } else {
             if (record.getKey().isGroupingEmpty() &&
                 !record.getKey().isSortEmpty()) {
                 record.getKey().setGrouping(defaultRecord.getKey().getGrouping());
             }
         }
 
         if (record.isValueEmpty()) {
             record.setValue(defaultRecord.getValue());
         }
 
         flushCount++;
         if (flushCount > cache) {
             flush();
             flushCount = 0;
         }
         List<Value> l = buffer.get(record.getKey());
         if (l == null) {
             l = new ArrayList<Value>();
         }
         l.add(record.getValue());
         buffer.put(record.getKey(), l);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void flush() throws IOException, InterruptedException {
         for (Entry<Key, List<Value>> entry : buffer.entrySet()) {
             summarizer.combine(entry.getKey(), entry.getValue(), context);
             entry.getValue().clear();
         }
         buffer.clear();
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public Record getDefaultRecord() {
         return defaultRecord;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void setDefaultRecord(Record defaultRecord) {
         this.defaultRecord = defaultRecord;
     }
 
     /**
      * {@inheritDoc}
      */
     @SuppressWarnings("rawtypes")
     @Override
     public TaskInputOutputContext getContext() {
         return context;
     }
 
     /**
      * {@inheritDoc}
      */
     @SuppressWarnings("rawtypes")
     @Override
     public void setContext(TaskInputOutputContext context) {
         this.context = context;
     }
 }
