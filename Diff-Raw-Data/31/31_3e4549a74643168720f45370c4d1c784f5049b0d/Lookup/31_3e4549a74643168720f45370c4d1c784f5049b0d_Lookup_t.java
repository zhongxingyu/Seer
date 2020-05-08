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
  * The primary interface to support lookup of a object by some index.
  * <p>
  * All methods in this interface must be thread safe.
  * 
  * @author Kenneth Xu
  * 
  * @param <T>
  *            the type of the object to be looked up.
  */
 public interface Lookup<T> {
 
     /**
      * Find and return the reference object by given key, or return a default value when the object is not found.
      * <p>
      * The default value is implementation dependent and maybe null.
      * 
      * @param key
      *            the key to lookup the object.
      * @return the reference object found or a default value if not found.
      */
     @CheckForNull
     T find(@CheckForNull Object key);
 
     /**
      * Find and return the reference object by given key, or return the specified default value when the object is not
      * found.
      * 
      * @param key
      *            the key to lookup the object
      * @param defaultValue
      *            the default value, which can be {@code null}, to be returned if object is not found
      * @return the reference object found or {@code defaultValue} if not found
      */
     @CheckForNull
     T find(@CheckForNull Object key, @CheckForNull T defaultValue);
 
     /**
      * Find and return the reference object by given key, or return a default value when the object is not found.
      * <p>
      * The default value is implementation dependent and must not be {@code null}.
      * 
      * @param key
      *            the key to lookup the object
      * @return the reference object found or a default value if not found
     * @exception LookupException
     *                if failed to lookup the reference object and unable to provide a non {@code null} default value
      */
     T get(@CheckForNull Object key);
 
     /**
      * Find and return the reference object by given key, or return the specified default value when the object is not
      * found.
      * 
      * @param key
      *            the key to lookup the object
      * @param defaultValue
      *            the default value to be returned if object is not found
      * @return the reference object found or {@code defaultValue} if not found
     * @exception IllegalArgumentException
     *                when {@code defaultValue} is {@code null}
      */
     T get(@CheckForNull Object key, T defaultValue);
 
     /**
      * Find and return the reference object by given key, or fail if one doesn't exist.
      * <p>
      * {@code hunt} distinguish itself from {@code get} methods by throwing exception when the reference object cannot
      * be found.
      * 
      * @param key
      *            the key to lookup the object
      * @return the reference object found
     * @exception IllegalArgumentException
     *                when key is null
     * @exception LookupException
     *                if failed to lookup the reference object
      */
     T hunt(@CheckForNull Object key);
 }
