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
 package org.huahinframework.core;
 
 import java.io.IOException;
 import java.util.Iterator;
 
 import org.apache.hadoop.mapreduce.Reducer;
 import org.apache.hadoop.mapreduce.TaskInputOutputContext;
 import org.huahinframework.core.io.Key;
 import org.huahinframework.core.io.Record;
 import org.huahinframework.core.io.Value;
 import org.huahinframework.core.io.ValueWritable;
 import org.huahinframework.core.writer.BasicWriter;
 import org.huahinframework.core.writer.Writer;
 
 /**
  * This class is wrapping the {@link Reducer} class.
  * <code>Summarizer</code> will process a given record from {@link Filter}.
  *
  * <p>The framework first calls {@link #summarizerSetup()}, followed by
  * {@link #init()} and {@link #summarize(Writer)} for each {@link Record} in the input.</p>
  *
  * <p>The following is an example to do a count of the WORD that was passed from Filter.
  * Has not specified grouping, will use the default.</p>
  *
  * <p>Example:</p>
  * <p><blockquote><pre>
  * public class WordSummarizer extends Summarizer {
  *   public void init() {
  *   }
  *
  *   public void summarize(Writer writer)
  *       throws IOException, InterruptedException {
  *     int count = 0;
  *     while (hasNext()) {
  *       next(writer);
  *       count++;
  *     }
  *
  *     Record emitRecord = new Record();
  *     emitRecord.addValue("COUNT", count);
  *     writer.write(emitRecord);
  *   }
  *
  *   public void summarizerSetup() {
  *   }
  * }
  * </pre></blockquote></p>
  *
  * @see Record
  * @see Writer
  * @see Summarizer
  */
 public abstract class Summarizer extends Reducer<Key, Value, Key, Value> {
     protected Context context;
     protected boolean combine = false;
     private Writer writer;
     private Iterator<Value> recordIte;
     private Record currentRecord = new Record();
     private Record defaultGroupRecord = new Record();
     private String[] inputLabels;
     private String[] valuesLabels;
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void reduce(Key key, Iterable<Value> values, Context context)
             throws IOException ,InterruptedException {
         writer.setContext(context);
         init();
 
         if (inputLabels != null) {
             setupLabel(key);
         } else {
             Key k = new Key();
             for (ValueWritable vw : key.getGrouping()) {
                 k.getGrouping().add(new ValueWritable(vw.getLabel().toString(), vw.getValue()));
             }
             defaultGroupRecord.setKey(k);
         }
 
         currentRecord.setKey(key);
         recordIte = values.iterator();
         summarize(writer);
     }
 
     /**
      * Combiner for In-Mapper Combiner
      * @param key {@link Key}
      * @param values {@link Value} iterator
      * @param context {@link TaskInputOutputContext} context
      * @throws IOException
      * @throws InterruptedException
      */
     @SuppressWarnings("rawtypes")
     public void combine(Key key, Iterable<Value> values, TaskInputOutputContext context)
             throws IOException ,InterruptedException {
         combine = true;
         writer.setContext(context);
         init();
 
         if (inputLabels != null) {
             setupLabel(key);
         } else {
             Key k = new Key();
             for (ValueWritable vw : key.getGrouping()) {
                 k.getGrouping().add(new ValueWritable(vw.getLabel().toString(), vw.getValue()));
             }
             defaultGroupRecord.setKey(k);
         }
 
         currentRecord.setKey(key);
         recordIte = values.iterator();
         summarize(writer);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void setup(Context context)
             throws IOException ,InterruptedException {
         this.context = context;
         inputLabels = context.getConfiguration().getStrings(SimpleJob.FILETER_OUTPUT_LABELS);
         if (inputLabels == null) {
             inputLabels = context.getConfiguration().getStrings(SimpleJob.BEFORE_SUMMARIZER_OUTPUT_LABELS);
         }
         boolean label =
                 context.getConfiguration().getStrings(SimpleJob.SUMMARIZER_OUTPUT_LABELS) == null ?
                         true : false;
         writer = new BasicWriter(label);
         summarizerSetup();
     }
 
     /**
      * setup label
      * @param key key
      */
     private void setupLabel(Key key) {
         int i = 0;
         Key k = new Key();
         for (ValueWritable vw : key.getGrouping()) {
             vw.getLabel().set(inputLabels[i++]);
             k.getGrouping().add(new ValueWritable(vw.getLabel().toString(), vw.getValue()));
         }
         defaultGroupRecord.setKey(k);
 
         valuesLabels = new String[inputLabels.length - i];
         for (int j = 0; j < valuesLabels.length; j++) {
             valuesLabels[j] = inputLabels[i++];
         }
     }
 
     /**
      * Returns true if the iteration has more {@link Record}.
      * @return true if the iterator has more {@link Record}.
      */
     protected boolean hasNext() {
         return recordIte.hasNext();
     }
 
     /**
      * Returns the next {@link Record} in the iteration.
      * @param writer the output using the writer.
      * @return the next {@link Record} in the iteration.
      */
     protected Record next(Writer writer) {
         Value value = recordIte.next();
 
         if (valuesLabels != null) {
             int i = 0;
             for (ValueWritable vw : value.getValues()) {
                 vw.getLabel().set(valuesLabels[i++]);
             }
         }
 
         currentRecord.setValue(value);
         writer.setDefaultRecord(currentRecord);
         return currentRecord;
     }
 
     /**
      * Returns if true, summarize is combine.
      * @return if true, summarize is combine.
      */
     protected boolean isCombine() {
         return combine;
     }
 
     /**
      * This method is returns the record for get the grouping.
      * @return grouping record
      */
     protected Record getGroupingRecord() {
         return defaultGroupRecord;
     }
 
     /**
      * Get Job String parameter
      * @param name parameter name
      * @return parameter value
      */
     protected String getStringParameter(String name) {
         return context.getConfiguration().get(name);
     }
 
     /**
      * Get Job Boolean parameter
      * @param name parameter name
      * @return parameter value
      */
     protected boolean getBooleanParameter(String name) {
         return context.getConfiguration().getBoolean(name, false);
     }
 
     /**
      * Get Job Integer parameter
      * @param name parameter name
      * @return parameterr value
      */
     protected int getIntParameter(String name) {
         return context.getConfiguration().getInt(name, -1);
     }
 
     /**
      * Will be called for each group.
      */
     public abstract void init();
 
     /**
      * <code>summarize</code> will process a given record from {@link Filter}.
      * @param writer the output using the writer.
      * @throws IOException
      * @throws InterruptedException
      */
     public abstract void summarize(Writer writer) throws IOException ,InterruptedException;
 
     /**
      * Called once at the beginning of the task.
      */
     public abstract void summarizerSetup();
 }
