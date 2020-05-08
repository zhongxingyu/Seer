 /**
  * Copyright 2012 Jason Rose <jasoncrose@gmail.com>
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
 package com.praxissoftware.rest.core;
 
 import java.util.Collection;
 import java.util.Map;
 import java.util.Set;
 
 import com.google.common.collect.Maps;
 
 /**
  * An AbstractMapEntity is a bean that presents the API of a Map. It allows libraries to interact with subclasses either as the subclass type or as a Map type to facilitate reuse.
  * 
  * @see <a href="http://www.infoq.com/presentations/Simple-Made-Easy">Data is data</a>
  * @author Jason Rose
  * 
  */
 public abstract class AbstractMapEntity implements Map<String, Object> {
 
   private final Map<String, Object> delegate;
 
   public AbstractMapEntity() {
     delegate = Maps.newHashMap();
   }
 
   @Override
   public void clear() {
     delegate.clear();
   }
 
   @Override
   public boolean containsKey(final Object arg0) {
     return delegate.containsKey(arg0);
   }
 
   @Override
   public boolean containsValue(final Object arg0) {
     return delegate.containsValue(arg0);
   }
 
   @Override
   public Set<java.util.Map.Entry<String, Object>> entrySet() {
     return delegate.entrySet();
   }
 
   @Override
   public boolean equals(final Object object) {
     return delegate.equals(object);
   }
 
   @Override
   public Object get(final Object arg0) {
     return delegate.get(arg0);
   }
 
   /**
    * This method sidesteps some of the syntactic nastiness of Java and generics.
    * @param key The key to look for a value to.
    * @return The value, cast as the type expected by the LHS of the expression.
    */
   @SuppressWarnings("unchecked")
   public <T> T getAndCoerce(final String key) {
     return (T) get(key);
   }
 
   @Override
   public int hashCode() {
     return delegate.hashCode();
   }
 
   @Override
   public boolean isEmpty() {
     return delegate.isEmpty();
   }
 
   @Override
   public Set<String> keySet() {
     return delegate.keySet();
   }
 
   @Override
   public Object put(final String key, final Object value) {
     final Object output;
     if( value == null ) {
       output = remove(key);
     } else {
      output = delegate.put(key, value);
     }
     return output;
   }
 
   @Override
   public void putAll(final Map<? extends String, ? extends Object> arg0) {
     delegate.putAll(arg0);
   }
 
   @Override
   public Object remove(final Object arg0) {
     return delegate.remove(arg0);
   }
 
   @Override
   public int size() {
     return delegate.size();
   }
 
   @Override
   public Collection<Object> values() {
     return delegate.values();
   }
 }
