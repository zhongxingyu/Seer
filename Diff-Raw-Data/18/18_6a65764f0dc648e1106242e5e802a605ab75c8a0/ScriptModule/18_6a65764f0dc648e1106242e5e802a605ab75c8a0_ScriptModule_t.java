 /**
  *    Copyright 2011-2012 Jim Tse
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
 package com.github.jtse.puzzle.util;
 
 import java.io.File;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.ImmutableMap;
 import com.google.common.collect.ImmutableSet;
 import com.google.inject.AbstractModule;
 import com.google.inject.TypeLiteral;
 import com.google.inject.name.Names;
 
 /**
  * @author jtse
  */
 public class ScriptModule extends AbstractModule {
   private static final Map<String, String> DEFAULTS = ImmutableMap.of(
       "median-mouse-filter-size", "11");
 
   private final File scriptFile;
   private final Set<String> repeatableKeys;
 
   public ScriptModule(File scriptFile, String... repeatableKeys) {
     this.scriptFile = scriptFile;
     this.repeatableKeys = ImmutableSet.copyOf(repeatableKeys);
   }
 
   /* (non-Javadoc)
    * @see com.google.inject.AbstractModule#configure()
    */
   @Override
   protected void configure() {
     bind(File.class).annotatedWith(Names.named("_script-file"))
         .toInstance(scriptFile);
 
     List<Map<String, String>> maps = ScriptUtils.read(scriptFile, repeatableKeys);
     bind(new TypeLiteral<List<Map<String,String>>>() {}).annotatedWith(Names.named("_script-repeatable"))
        .toInstance(ImmutableList.copyOf(maps.subList(1, maps.size())));
 
     HashMap<String, String> map = new HashMap<String, String>();
     map.putAll(DEFAULTS);
     map.putAll(maps.get(0));
 
     ImmutableMap<String, String> config = ImmutableMap.copyOf(map);
 
     bind(new TypeLiteral<Map<String, String>>() {} ).annotatedWith(Names.named("_script-config"))
         .toInstance(config);
 
     for (Entry<String, String> entry : config.entrySet()) {
       bind(String.class).annotatedWith(Names.named(entry.getKey()))
           .toInstance(entry.getValue());
     }
   }
 }
