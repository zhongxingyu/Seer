 /*
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
 package org.apache.sling.jcr.resource;
 
 import java.lang.reflect.Array;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.jcr.Node;
 import javax.jcr.Property;
 import javax.jcr.PropertyIterator;
 import javax.jcr.RepositoryException;
 import javax.jcr.Value;
 import javax.jcr.ValueFormatException;
 
 import org.apache.sling.api.resource.ValueMap;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class JcrPropertyMap implements ValueMap {
 
     /** default log */
     private final Logger log = LoggerFactory.getLogger(getClass());
 
     private final Node node;
 
     private final Map<String, Object> cache;
 
     private boolean fullyRead;
 
     public JcrPropertyMap(Node node) {
         this.node = node;
         this.cache = new HashMap<String, Object>();
         this.fullyRead = false;
     }
 
     // ---------- ValueMap
 
     @SuppressWarnings("unchecked")
     public <T> T get(String name, Class<T> type) {
         if (type == null) {
             return (T) get(name);
         }
 
         return convertToType(name, type);
     }
 
     @SuppressWarnings("unchecked")
     public <T> T get(String name, T defaultValue) {
         if (defaultValue == null) {
             return (T) get(name);
         }
 
        T value = get(name, (Class<T>) defaultValue.getClass());
         if (value == null) {
             value = defaultValue;
         }
 
         return value;
     }
 
     // ---------- Map
 
     public Object get(Object key) {
         Object value = cache.get(key);
         if (value == null) {
             value = read((String) key);
         }
 
         return value;
     }
 
     public boolean containsKey(Object key) {
         return get(key) != null;
     }
 
     public boolean containsValue(Object value) {
         readFully();
         return cache.containsValue(value);
     }
 
     public boolean isEmpty() {
         // only start reading if there is nothing in the cache yet
         if (cache.isEmpty()) {
             readFully();
         }
 
         return cache.isEmpty();
     }
 
     public int size() {
         readFully();
         return cache.size();
     }
 
     public Set<java.util.Map.Entry<String, Object>> entrySet() {
         readFully();
         return cache.entrySet();
     }
 
     public Set<String> keySet() {
         readFully();
         return cache.keySet();
     }
 
     public Collection<Object> values() {
         readFully();
         return cache.values();
     }
 
     public String getPath() {
         try {
             return node.getPath();
         } catch (RepositoryException e) {
             // TODO
             return "";
         }
     }
     // ---------- Helpers to access the node's property ------------------------
 
     private Object read(String key) {
 
         // if the node has been completely read, we need not check
         // again, as we certainly will not find the key
         if (fullyRead) {
             return null;
         }
 
         try {
             if (node.hasProperty(key)) {
                 Property prop = node.getProperty(key);
                 Object value = JcrResourceUtil.toJavaObject(prop);
                 cache.put(key, value);
                 return value;
             }
         } catch (RepositoryException re) {
             // TODO: log !!
         }
 
         // property not found or some error accessing it
         return null;
     }
 
     private void readFully() {
         if (!fullyRead) {
             try {
                 PropertyIterator pi = node.getProperties();
                 while (pi.hasNext()) {
                     Property prop = pi.nextProperty();
                     String key = prop.getName();
                     if (!cache.containsKey(key)) {
                         cache.put(key, JcrResourceUtil.toJavaObject(prop));
                     }
                 }
                 fullyRead = true;
             } catch (RepositoryException re) {
                 // TODO: log !!
             }
         }
     }
 
     // ---------- Unsupported Modification methods
 
     public void clear() {
         throw new UnsupportedOperationException();
     }
 
     public Object put(String key, Object value) {
         throw new UnsupportedOperationException();
     }
 
     public void putAll(Map<? extends String, ? extends Object> t) {
         throw new UnsupportedOperationException();
     }
 
     public Object remove(Object key) {
         throw new UnsupportedOperationException();
     }
 
     // ---------- Implementation helper
 
     @SuppressWarnings("unchecked")
     private <T> T convertToType(String name, Class<T> type) {
         try {
             if (node.hasProperty(name)) {
                 Property prop = node.getProperty(name);
 
                 boolean multiValue = prop.getDefinition().isMultiple();
                 boolean array = type.isArray();
 
                 if (array && multiValue) {
                     return (T) convertToArray(prop.getValues(),
                         type.getComponentType());
                 } else if (!array && !multiValue) {
                     return convertToType(prop.getValue(), type);
                 }
             }
 
         } catch (ValueFormatException vfe) {
             log.info("converToType: Cannot convert value of " + name + " to "
                 + type, vfe);
         } catch (RepositoryException re) {
             log.info("converToType: Cannot get value of " + name, re);
         }
 
         // fall back to nothing
         return null;
     }
 
     private <T> T[] convertToArray(Value[] jcrValues, Class<T> type)
             throws ValueFormatException, RepositoryException {
         List<T> values = new ArrayList<T>();
         for (int i = 0; i < jcrValues.length; i++) {
             T value = convertToType(jcrValues[i], type);
             if (value != null) {
                 values.add(value);
             }
         }
 
         @SuppressWarnings("unchecked")
         T[] result = (T[]) Array.newInstance(type, values.size());
 
         return values.toArray(result);
     }
 
     @SuppressWarnings("unchecked")
     private <T> T convertToType(Value jcrValue, Class<T> type)
             throws ValueFormatException, RepositoryException {
 
         if (String.class == type) {
             return (T) jcrValue.getString();
         } else if (Long.class == type) {
             return (T) new Long(jcrValue.getLong());
         } else if (Double.class == type) {
             return (T) new Double(jcrValue.getDouble());
         } else if (Boolean.class == type) {
             return (T) Boolean.valueOf(jcrValue.getBoolean());
         } else if (Calendar.class == type) {
             return (T) jcrValue.getDate();
         } else if (Value.class == type) {
             return (T) jcrValue;
         }
 
         // fallback in case of unsupported type
         return null;
     }
 }
