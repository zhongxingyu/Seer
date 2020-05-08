 /*
  * Copyright 2007-2009 David Kellum
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.gravitext.htmap;
 
 /**
  * <b>H</b>eterogeneous <b>T</b>ype-safe key/value access
  * interface. Keys are of the generic type {@link Key Key&lt;T&gt;},
  * where T is a key-specific value type. The {@link #get get()} and
  * {@link #remove remove()} methods are {@code Key} type-safe
  * overloads of the equivalent {@code java.util.Map} methods. A new
  * {@link #set set()} method is introduced which offers a compile-time
  * type-safe alternative to {@code Map.put()}. In this case
  * an overload is not possible because the erased signatures would be
 * the same.</p>
  *
  * <p>The null key is not supported by this interface.</p>
  *
  * @see Key
  * @see KeySpace
  * @author David Kellum
  */
 public interface HTAccess
 {
     /**
      * Associate the specified value with the specified key.
      * @see java.util.Map#put(Object, Object)
      * @param <T> the value type provided by the key
      * @return the previous value associated with key, or null if no
      * such value was present.
      * @throws ClassCastException if value's runtime type is not
      * assignable to {@code key.valueType()}.
      * @throws NullPointerException if key is null, or if null values
      * are not supported and value is null.
      */
     <T, V extends T> T set( Key<T> key, V value );
 
     /**
      * Returns the value previously set with the specified key.
      *
      * @see java.util.Map#get(Object)
      * @param <T> the value type provided by the key
      * @return the value associated with key, or null if no such value
      * is present.
      * @throws NullPointerException if key is null.
      */
     <T> T get( Key<T> key );
 
     /**
      * Removes any value previously set with for the specified key.
      * @see java.util.Map#remove(Object)
      * @param <T> the value type provided by the key
      * @return the previous value associated with key, or null if no
      * such value was present.
      * @throws NullPointerException if key is null.
      */
     <T> T remove( Key<T> key );
 }
