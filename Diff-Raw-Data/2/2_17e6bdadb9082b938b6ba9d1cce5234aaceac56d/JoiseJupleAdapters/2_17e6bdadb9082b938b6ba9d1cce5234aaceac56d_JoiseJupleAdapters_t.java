 /*
  * Copyright (C) 2013 Jason Taylor
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *      http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.sudoplay.joise.converter;
 
 import java.io.IOException;
 import java.util.Iterator;
 import java.util.Map.Entry;
 
 import com.sudoplay.joise.ModuleMap;
 import com.sudoplay.joise.ModulePropertyMap;
 import com.sudoplay.juple.classparser.adapters.TMLTypeAdapter;
 import com.sudoplay.juple.classparser.adapters.TMLTypeAdapterFactory;
 import com.sudoplay.juple.classparser.adapters.TMLTypeAdapters;
 import com.sudoplay.juple.stream.TMLReader;
 import com.sudoplay.juple.stream.TMLWriter;
 
 public class JoiseJupleAdapters {
 
   private JoiseJupleAdapters() {}
 
   public static final TMLTypeAdapter<ModuleMap> MODULE_MAP = new TMLTypeAdapter<ModuleMap>() {
     @Override
     public void write(TMLWriter out, ModuleMap map) throws IOException {
 
       Iterator<Entry<String, ModulePropertyMap>> it = map.mapIterator();
       while (it.hasNext()) {
         Entry<String, ModulePropertyMap> e = it.next();
         out.beginList();
         out.name(e.getKey());
         PROPERTY_MAP.write(out, e.getValue());
         out.endList();
       }
 
     }
 
     @Override
     public ModuleMap read(TMLReader in) throws IOException {
       ModuleMap map = new ModuleMap();
 
       int scope = in.getScope();
       while (in.hasNextInScope(scope)) {
         in.beginList();
         String name = in.nextName();
         ModulePropertyMap props = PROPERTY_MAP.read(in);
         in.endList();
         map.put(name, props);
       }
 
       return map;
     }
   };
 
   public static final TMLTypeAdapterFactory MODULE_MAP_FACTORY = TMLTypeAdapters
       .newFactory(ModuleMap.class, MODULE_MAP);
 
   public static final TMLTypeAdapter<ModulePropertyMap> PROPERTY_MAP = new TMLTypeAdapter<ModulePropertyMap>() {
     @Override
     public void write(TMLWriter out, ModulePropertyMap props)
         throws IOException {
       Iterator<Entry<String, Object>> it = props.iterator();
       while (it.hasNext()) {
         Entry<String, Object> e = it.next();
         out.beginList();
         out.name(e.getKey());
         Object value = e.getValue();
         if (value == null) {
           out.value((String) null);
         } else {
          out.value(value.toString());
         }
         out.endList();
       }
     }
 
     @Override
     public ModulePropertyMap read(TMLReader in) throws IOException {
       ModulePropertyMap props = new ModulePropertyMap();
 
       int scope = in.getScope();
       while (in.hasNextInScope(scope)) {
         in.beginList();
         String key = in.nextName();
         String value = in.nextString();
         in.endList();
         props.put(key, value);
       }
 
       return props;
     }
   };
 
   public static final TMLTypeAdapterFactory PROPERTY_MAP_FACTORY = TMLTypeAdapters
       .newFactory(ModulePropertyMap.class, PROPERTY_MAP);
 
 }
