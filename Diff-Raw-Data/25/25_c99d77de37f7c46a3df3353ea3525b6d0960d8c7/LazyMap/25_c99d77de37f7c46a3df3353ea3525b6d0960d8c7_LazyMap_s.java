 /*
  * Copyright 2008 EGEE Collaboration
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
 
 package org.glite.authz.common.util;
 
 import java.io.Serializable;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 
 import net.jcip.annotations.NotThreadSafe;
 
 /**
  * A map that is lazy initialized. This map takes very little memory when storing zero or one item.
  * 
  * @param <KeyType> the type of the map keys
  * @param <ValueType> the type of the map values
  */
 @NotThreadSafe
 public class LazyMap<KeyType, ValueType> implements Map<KeyType, ValueType>, Serializable {
 
     /** Serial version UID. */
     private static final long serialVersionUID = 121425595164176639L;
 
     /** The delegate map. */
     private Map<KeyType, ValueType> delegate = Collections.emptyMap();
 
     /** {@inheritDoc} */
     public void clear() {
         delegate = Collections.emptyMap();
     }
 
     /** {@inheritDoc} */
     public boolean containsKey(Object key) {
         return delegate.containsKey(key);
     }
 
     /** {@inheritDoc} */
     public boolean containsValue(Object value) {
         return delegate.containsValue(value);
     }
 
     /** {@inheritDoc} */
     public Set<Entry<KeyType, ValueType>> entrySet() {
         return delegate.entrySet();
     }
 
     /** {@inheritDoc} */
     public ValueType get(Object key) {
         return delegate.get(key);
     }
 
     /** {@inheritDoc} */
     public boolean isEmpty() {
         return delegate.isEmpty();
     }
 
     /** {@inheritDoc} */
     public Set<KeyType> keySet() {
         return delegate.keySet();
     }
 
     /** {@inheritDoc} */
     public ValueType put(KeyType key, ValueType value) {
         if (delegate.isEmpty()) {
             delegate = Collections.singletonMap(key, value);
             return null;
         } else {
             delegate = buildMap();
             return delegate.put(key, value);
         }
     }
 
     /** {@inheritDoc} */
     public void putAll(Map<? extends KeyType, ? extends ValueType> t) {
         delegate = buildMap();
         delegate.putAll(t);
     }
 
     /** {@inheritDoc} */
     public ValueType remove(Object key) {
         delegate = buildMap();
         return delegate.remove(key);
     }
 
     /** {@inheritDoc} */
     public int size() {
         return delegate.size();
     }
 
     /** {@inheritDoc} */
     public Collection<ValueType> values() {
         return delegate.values();
     }
 
     /**
      * Builds an appropriate delegate map.
      * 
      * @return the delegate map
      */
     protected Map<KeyType, ValueType> buildMap() {
         if (delegate instanceof HashMap) {
             return delegate;
         }
 
         return new HashMap<KeyType, ValueType>(delegate);
     }
 
     /** {@inheritDoc} */
     public int hashCode() {
         return delegate.hashCode();
     }
 
     /** {@inheritDoc} */
     public boolean equals(Object obj) {
         if (this == obj) {
             return true;
         }
 
         if (obj == null || this.getClass() != obj.getClass()) {
             return false;
         }
 
         return delegate.equals(((LazyMap<?, ?>) obj).delegate);
     }
 }
