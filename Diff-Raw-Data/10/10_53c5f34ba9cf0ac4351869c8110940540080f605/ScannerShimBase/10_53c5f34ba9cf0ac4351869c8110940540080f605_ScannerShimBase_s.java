 /*
  * Licensed to the Apache Software Foundation (ASF) under one or more
  * contributor license agreements.  See the NOTICE file distributed with
  * this work for additional information regarding copyright ownership.
  * The ASF licenses this file to You under the Apache License, Version 2.0
  * (the "License"); you may not use this file except in compliance with
  * the License.  You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.texeltek.accumulocloudbaseshim;
 
 import org.apache.accumulo.core.client.IteratorSetting;
 import org.apache.accumulo.core.client.ScannerBase;
 import org.apache.accumulo.core.iterators.SortedKeyIterator;
 import org.apache.accumulo.core.iterators.user.RegExFilter;
 import org.apache.hadoop.io.Text;
 
 import java.io.IOException;
 import java.util.Map;
 
 public abstract class ScannerShimBase implements ScannerBase {
     private final cloudbase.core.client.ScannerBase baseImpl;
 
     public ScannerShimBase(cloudbase.core.client.ScannerBase baseImpl) {
         this.baseImpl = baseImpl;
     }
 
     public void addScanIterator(IteratorSetting cfg) {
         try {
             if (cfg.getIteratorClass().equals(RegExFilter.class.getName())) {
                 translateRegExFilter(cfg);
             } else if (isCloudbaseFilter(cfg.getIteratorClass())) {
                 translateCloudbaseFilter(cfg);
             } else {
                 translateCloudbaseIterator(cfg);
             }
         } catch (IOException e) {
             throw new RuntimeException(e);
         }
     }
 
     protected void translateCloudbaseFilter(IteratorSetting cfg) throws IOException {
         baseImpl.setScanIterators(cfg.getPriority(), cloudbase.core.iterators.FilteringIterator.class.getName(), cfg.getName());
        baseImpl.setScanIteratorOption(cfg.getName(), "0", cfg.getIteratorClass());
         for (Map.Entry<String, String> option : cfg.getOptions().entrySet()) {
             baseImpl.setScanIteratorOption(cfg.getName(), "0." + option.getKey(), option.getValue());
         }
     }
 
     private void translateRegExFilter(IteratorSetting cfg) throws IOException {
         baseImpl.setScanIterators(cfg.getPriority(), cloudbase.core.iterators.RegExIterator.class.getName(), cfg.getName());
         for (Map.Entry<String, String> option : cfg.getOptions().entrySet()) {
             baseImpl.setScanIteratorOption(cfg.getName(), option.getKey(), option.getValue());
         }
     }
 
     private void translateCloudbaseIterator(IteratorSetting cfg) throws IOException {
         translateIteratorsUserPackage(cfg);
         translateIteratorsPackage(cfg);
 
         baseImpl.setScanIterators(cfg.getPriority(), cfg.getIteratorClass(), cfg.getName());
         for (Map.Entry<String, String> option : cfg.getOptions().entrySet()) {
             baseImpl.setScanIteratorOption(cfg.getName(), option.getKey(), option.getValue());
         }
     }
 
     private void translateIteratorsUserPackage(IteratorSetting cfg) {
         final String prefix = "org.apache.accumulo.core.iterators.user";
         if (cfg.getIteratorClass().startsWith(prefix)) {
             cfg.setIteratorClass("cloudbase.core.iterators" + cfg.getIteratorClass().substring(prefix.length()));
         }
     }
 
     private void translateIteratorsPackage(IteratorSetting cfg) {
         if (cfg.getIteratorClass().equals(SortedKeyIterator.class.getName())) {
             cfg.setIteratorClass(cloudbase.core.iterators.SortedKeyIterator.class.getName());
         }
     }
 
     protected boolean isCloudbaseFilter(String iteratorClass_str) {
         try {
            Class<?> iteratorClass = Class.forName(iteratorClass_str);
             return cloudbase.core.iterators.filter.Filter.class.isAssignableFrom(iteratorClass);
         } catch (ClassNotFoundException e) {
             return false;
         }
     }
 
     public void removeScanIterator(String iteratorName) {
         throw new UnsupportedOperationException("Cloudbase 1.3 does not support removing scan iterators");
     }
 
     public void updateScanIteratorOption(String iteratorName, String key, String value) {
         baseImpl.setScanIteratorOption(iteratorName, key, value);
     }
 
     public void setScanIterators(int i, String s, String s1) throws IOException {
         baseImpl.setScanIterators(i, s, s1);
     }
 
     public void setScanIteratorOption(String s, String s1, String s2) {
         baseImpl.setScanIteratorOption(s, s1, s2);
     }
 
     public void setupRegex(String s, int i) throws IOException {
         baseImpl.setupRegex(s, i);
     }
 
     public void setRowRegex(String s) {
         baseImpl.setRowRegex(s);
     }
 
     public void setColumnFamilyRegex(String s) {
         baseImpl.setColumnFamilyRegex(s);
     }
 
     public void setColumnQualifierRegex(String s) {
         baseImpl.setColumnQualifierRegex(s);
     }
 
     public void setValueRegex(String s) {
         baseImpl.setValueRegex(s);
     }
 
     public void fetchColumnFamily(Text text) {
         baseImpl.fetchColumnFamily(text);
     }
 
     public void fetchColumn(Text text, Text text1) {
         baseImpl.fetchColumn(text, text1);
     }
 
     public void clearColumns() {
         baseImpl.clearColumns();
     }
 
     public void clearScanIterators() {
         baseImpl.clearScanIterators();
     }
 
     public String toString() {
         return baseImpl.toString();
     }
 }
