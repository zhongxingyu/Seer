 /**
  * Copyright (C) 2008 Stuart McCulloch
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
 
 package org.ops4j.peaberry.internal;
 
 import static org.ops4j.peaberry.internal.ImportProxyClassLoader.importProxy;
 
 import java.util.Iterator;
 
 import org.ops4j.peaberry.Import;
 import org.ops4j.peaberry.builders.ImportDecorator;
 
 /**
  * Factory methods for dynamic service proxies.
  * 
  * @author stuart.mcculloch@jayway.net (Stuart McCulloch)
  */
 final class ServiceProxyFactory {
 
   // instances not allowed
   private ServiceProxyFactory() {}
 
   public static <S, T extends S> Iterable<T> serviceProxies(final Class<? extends T> clazz,
       final Iterable<Import<T>> handles, final ImportDecorator<S> decorator) {
 
     return new Iterable<T>() {
       public Iterator<T> iterator() {
         return new Iterator<T>() {
 
           private final Iterator<Import<T>> i = handles.iterator();
 
           public boolean hasNext() {
             return i.hasNext();
           }
 
           public T next() {
             return importProxy(clazz, apply(decorator, i.next()));
           }
 
           public void remove() {
             throw new UnsupportedOperationException();
           }
         };
       }
     };
   }
 
   public static <S, T extends S> T serviceProxy(final Class<? extends T> clazz,
       final Iterable<Import<T>> handles, final ImportDecorator<S> decorator) {
 
     final Import<T> lookup = new Import<T>() {
       private long count = 0L;
       private Import<T> handle;
       private T instance;
 
       public synchronized T get() {
         count++;
         if (null == handle) {
           handle = handles.iterator().next();
           instance = handle.get();
         }
         return instance;
       }
 
       public synchronized void unget() {
         if (--count == 0) {
           try {
             if (null != handle) {
               handle.unget();
             }
           } finally {
             instance = null;
             handle = null;
           }
         }
       }
     };
 
     return importProxy(clazz, apply(decorator, lookup));
   }
 
   static <S, T extends S> Import<T> apply(final ImportDecorator<S> decorator, final Import<T> handle) {
     return null == decorator ? handle : decorator.decorate(handle);
   }
 }
