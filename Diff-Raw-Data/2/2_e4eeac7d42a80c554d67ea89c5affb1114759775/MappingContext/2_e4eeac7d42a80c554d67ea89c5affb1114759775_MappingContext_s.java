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
      * @param context mapping context to copy mappings from
      */
     public MappingContext(MappingContext context) {
         this.context.putAll(context.context);
     }
 
     /**
      * @param mappings mappings
      */
     public MappingContext(Map<String, Object> mappings) {
         if (mappings == null) {
             throw new IllegalArgumentException("Context cannot be NULL");
         }
        this.context = mappings;
     }
 
     /**
      * Same as
      * <pre>
      * Map&lt;String, Object&gt; mappings = new HashMap&lt;String, Object&gt;();
      * mappings.put(key, value);
      * ... = new MappingContext(mappings)
      * </pre>
      * @param key key
      * @param value any object that needs to be available during the mapping process
      */
     public MappingContext(String key, Object value) {
         context.put(key, value);
     }
 
     /**
      * @param key key
      * @param <T> return type
      * @return object by the given key. nullable
      */
     @SuppressWarnings("unchecked")
     public <T> T get(String key) {
         return (T) context.get(key);
     }
 
     /**
      * @param key key
      * @param defaultValue default value
      * @param <T> return type
      * @return defaultValue if there is no mapping for the given key (or mapping value is null), otherwise -
      * associated value
      */
     public <T> T get(String key, T defaultValue) {
         T result = get(key);
         return result == null ? defaultValue : result;
     }
 
     /**
      * @param key key
      * @return true if this context contains a mapping for the given key
      */
     public boolean hasKey(String key) {
         return context.containsKey(key);
     }
 
     /**
      * @return true if there are no mappings defined in this context, false otherwise
      */
     public boolean isEmpty() {
         return context.isEmpty();
     }
 
     /**
      * @return a {@link Set} view of the keys defined in this context
      */
     public Set<String> getKeys() {
         return context.keySet();
     }
 
     /**
      * Associate value with the given key in this context (overriding if necessary).
      * @param key key
      * @param value value. nullable
      * @return this reference
      */
     public MappingContext put(String key, Object value) {
         context.put(key, value);
         return this;
     }
 
     /**
      * Copy all of the mappings from the given map to this context (overriding if necessary).
      * @param map map of key-value pairs
      * @return this reference
      */
     public MappingContext putAll(Map<String, Object> map) {
         context.putAll(map);
         return this;
     }
 
     /**
      * Remove the mapping for a key.
      * @param key key
      * @return this reference
      */
     public MappingContext remove(String key) {
         context.remove(key);
         return this;
     }
 
     /**
      * Remove all the mappings.
      * @return this reference
      */
     public MappingContext clear() {
         context.clear();
         return this;
     }
 
     @Override
     public String toString() {
         return context.toString();
     }
 }
