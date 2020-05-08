 /*
  * Copyright (c) 2013 Original Authors
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
 package com.sharneng.lookup;
 
 import javax.annotation.CheckForNull;
 
 /**
 * A convenient abstract class that helps to implement {@link Lookup Lookup&lt;T&gt;} interface.
  * 
  * @author Kenneth Xu
  * 
  * @param <T>
  *            type of the object to lookup
  */
 public abstract class AbstractLookup<T> implements Lookup<T> {
 
     @CheckForNull
     private final T defaultValue;
 
     /**
      * Construct a new instance with the specified default value.
      * 
      * @param defaultValue
      *            the default value to be used by {@link #get(Object)} method.
      */
     protected AbstractLookup(@CheckForNull T defaultValue) {
         this.defaultValue = defaultValue;
     }
 
     /**
      * Subclass to implements the actual lookup logic.
      * 
      * @param key
      *            the key to lookup the reference object
      * @return the reference object when found or null if not found
      */
     @CheckForNull
     protected abstract T lookup(Object key);
 
     /**
      * {@inheritDoc}
      * <p>
      * This implementation calls {@link #lookup(Object)} if key is not null or otherwise return null.
      */
     @Override
     public T find(@CheckForNull final Object key) {
         return key == null ? null : lookup(key);
     }
 
     /**
      * {@inheritDoc}
      * <p>
      * This implementation calls {@link #find(Object)}. If the result is not null, return the result. Otherwise return
      * the {@code defaultValue} parameter.
      */
     @Override
     public T find(@CheckForNull final Object key, @CheckForNull final T defaultValue) {
         final T result = find(key);
         return result == null ? (T) defaultValue : result;
     }
 
     /**
      * {@inheritDoc}
      * <p>
     * This implementation calls {@link #find(Object, Object) find(Object, T)}. If the result is not null, return the
     * result. Otherwise return the {@code defaultValue} passed to constructor.
      */
     public T get(@CheckForNull final Object key) {
         final T result = find(key, defaultValue);
         if (result != null) return result;
         throw Utils.notFoundException(key);
     }
 
     /**
      * {@inheritDoc}
      * <p>
     * This implementation calls {@link #find(Object, Object) find(Object, T)}. If the result is not null, return the
     * result. Otherwise return the {@code defaultValue} parameter.
      */
     @Override
     public T get(@CheckForNull final Object key, final T defaultValue) {
         if (defaultValue == null) throw new IllegalArgumentException(Utils.notNull("defaultValue"));
         return find(key, defaultValue);
     }
 
     /**
      * {@inheritDoc}
      * <p>
      * This implementation triggers exception when key is null or {@link #lookup(Object)} returns null. Otherwise,
      * delegates to {@code lookup} and return the value.
      */
     public T hunt(final Object key) {
         if (key == null) throw new IllegalArgumentException(Utils.notNull("key"));
         final T value = lookup(key);
         if (value != null) return value;
         throw Utils.notFoundException(key);
     }
 }
