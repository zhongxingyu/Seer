 /*
  * Copyright 2010 Google Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 
 package com.google.jstestdriver.model;
 
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 import com.google.common.collect.Sets;
 import com.google.jstestdriver.FileInfo;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 /**
  * Defines TestCase information allowing intelligent management and execution
  * of tests.
  * 
  * @author corbinrsmith@gmail.com (Cory Smith)
  */
 public class JstdTestCase implements Iterable<FileInfo> {
   @SuppressWarnings("unused")
   private static final Logger logger =
       LoggerFactory.getLogger(JstdTestCase.class);
 
   private List<FileInfo> dependencies;
   private List<FileInfo> tests;
   private List<FileInfo> plugins;
 
   private String id;
   
   public JstdTestCase() {}
 
   public JstdTestCase(List<FileInfo> dependencies,
                       List<FileInfo> tests,
                       List<FileInfo> plugins,
                       String id) {
     this.dependencies = dependencies;
     this.tests = tests;
     this.plugins = plugins;
     this.id = id;
   }
 
   public List<FileInfo> getTests() {
     return tests;
   }
 
   public List<FileInfo> getDependencies() {
     return dependencies;
   }
   
   public List<FileInfo> getPlugins() {
     return plugins;
   }
   
   public JstdTestCase applyDelta(JstdTestCaseDelta delta) {
     return new JstdTestCase(
         applyUpdates(dependencies, delta.getDependencies()),
         applyUpdates(tests, delta.getTests()),
         applyUpdates(plugins, delta.getPlugins()),
         id);
   }
 
   /**
    * Creates a delta of unloaded files that need to be loaded.
    * Used when the current process does not have access to a FileLoader.
    */
   public JstdTestCaseDelta createUnloadedDelta() {
     return new JstdTestCaseDelta(
         findUnloaded(dependencies),
         findUnloaded(tests),
         findUnloaded(plugins));
   }
 
   /**
    * finds all the FileInfo's that are not loaded in a list.
    */
   private List<FileInfo> findUnloaded(List<FileInfo> files) {
     List<FileInfo> unloaded = Lists.newArrayList();
     for (FileInfo file : files) {
       if (!file.isLoaded()) {
         unloaded.add(file);
       }
     }
     return unloaded;
   }
 
   /**
    * @param original
    * @param update
    */
   private List<FileInfo> applyUpdates(List<FileInfo> original, List<FileInfo> update) {
     Map<String, FileInfo> updates = Maps.newHashMap();
     List<FileInfo> merged = Lists.newArrayList();
 
     for (FileInfo fileInfo : update) {
       updates.put(fileInfo.getFilePath(), fileInfo);
     }
 
     for (FileInfo fileInfo : original) {
       if (updates.containsKey(fileInfo.getFilePath())) {
         merged.add(updates.get(fileInfo.getFilePath()));
       } else {
         merged.add(fileInfo);
       }
     }
     return merged;
   }
   
   /**
    * Adaptor to translate to fileset for uploading to the server.
    */
   public Set<FileInfo> toFileSet() {
     final Set<FileInfo> fileSet = Sets.newLinkedHashSet(plugins);
     fileSet.addAll(dependencies);
     fileSet.addAll(tests);
     return fileSet;
   }
   
   public JstdTestCase updatePlugins(List<FileInfo> plugins) {
     final List<FileInfo> combined =
       Lists.newArrayListWithExpectedSize(plugins.size() + this.plugins.size());
     combined.addAll(this.plugins);
     combined.addAll(plugins);
     return new JstdTestCase(dependencies, tests, combined, id);
   }
 
   @Override
   public int hashCode() {
     final int prime = 31;
     int result = 1;
     result = prime * result + ((dependencies == null) ? 0 : dependencies.hashCode());
     result = prime * result + ((plugins == null) ? 0 : plugins.hashCode());
     result = prime * result + ((tests == null) ? 0 : tests.hashCode());
     result = prime * result + ((id == null) ? 0 : id.hashCode());
     return result;
   }
 
   @Override
   public boolean equals(Object obj) {
     if (this == obj) return true;
     if (obj == null) return false;
     if (getClass() != obj.getClass()) return false;
     JstdTestCase other = (JstdTestCase) obj;
     if (dependencies == null) {
       if (other.dependencies != null) return false;
     } else if (!dependencies.equals(other.dependencies)) return false;
     if (plugins == null) {
       if (other.plugins != null) return false;
     } else if (!plugins.equals(other.plugins)) return false;
     if (tests == null) {
       if (other.tests != null) return false;
     } else if (!tests.equals(other.tests)) return false;
     if (id == null) {
       if (other.id != null) return false;
     } else if (!id.equals(other.id)) return false;
     return true;
   }
 
   @Override
   public String toString() {
    return "JstdTestCase [dependencies=" + (logger.isDebugEnabled() || logger.isTraceEnabled() ? dependencies : "<elided>") + ", plugins=" + plugins + ", tests="
        + tests + "]";
   }
 
   /**
    * Access for the id.
    */
   public String getId() {
     return id;
   }
 
   @Override
   public Iterator<FileInfo> iterator() {
     return Iterables.concat(plugins, dependencies, tests).iterator();
   }
 
   /**
    * @return A list of files that are not serve only.
    */
   public List<FileInfo> getServable() {
     List<FileInfo> servable =
         Lists.newArrayListWithExpectedSize(tests.size() + plugins.size() + dependencies.size());
     for (FileInfo file : this) {
       if (!file.isServeOnly()) {
         servable.add(file);
       }
     }
     return servable;
   }
 }
