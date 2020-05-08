 /*
  * Copyright 2012 Stanley Shyiko
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.github.shyiko.mappify.api;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 
 /**
  * Mapping context which is available to the mappers during the mapping process.
  * Implementation is not thread-safe. Thus, same instance of this class should not be shared between multiple
  * threads.
  *
  * @author <a href="mailto:stanley.shyiko@gmail.com">Stanley Shyiko</a>
  */
 public class MappingContext {
 
     protected Map<String, Object> context = new HashMap<String, Object>();
 
     public MappingContext() {
     }
 
     /**
      * @param context map of key-value pairs, where key denotes the name of the value object
      */
     public MappingContext(Map<String, Object> context) {
         if (context == null) {
             throw new IllegalArgumentException("Context cannot be NULL");
         }
         this.context = context;
     }
 
     public MappingContext(MappingContext context) {
         this.context.putAll(context.context);
     }
 
     /**
      * Same as
      * <pre>
      * Map&lt;String, Object&gt; properties = new HashMap&lt;String, Object&gt;();
      * properties.put(key, value);
      * ... = new MappingContext(properties)
      * </pre>
      * @param key key
      * @param value any object that needs to be available during the mapping process
      */
     public MappingContext(String key, Object value) {
         context.put(key, value);
     }
 
     /**
      * @param name name of the object
      * @param <T> return type
      * @return object by the given key. nullable
      */
     @SuppressWarnings("unchecked")
     public <T> T get(String name) {
         return (T) context.get(name);
     }
 
     public <T> T get(String name, T defaultValue) {
         T result = get(name);
         return result == null ? defaultValue : result;
     }
 
     public boolean hasKey(String name) {
         return context.containsKey(name);
     }
 
    public boolean isEmpty(String name) {
         return context.isEmpty();
     }
 
     public Set<String> getKeys() {
         return context.keySet();
     }
 
     public MappingContext put(String key, Object value) {
         context.put(key, value);
         return this;
     }
 
     public MappingContext putAll(Map<String, Object> pairs) {
         context.putAll(pairs);
         return this;
     }
 
     public MappingContext remove(String key) {
         context.remove(key);
         return this;
     }
 
     public MappingContext clear() {
         context.clear();
         return this;
     }
 }
