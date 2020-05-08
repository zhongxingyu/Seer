 /**
  * Copyright 2010 CosmoCode GmbH
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
 
 package de.cosmocode.palava.core;
 
 import com.google.common.base.Preconditions;
 import com.google.common.base.Predicate;
import com.google.common.collect.AbstractIterator;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;
 import de.cosmocode.collections.Procedure;
 import de.cosmocode.commons.Throwables;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.lang.reflect.InvocationHandler;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.Iterator;
 import java.util.Map.Entry;
 
 /**
  * Default implementation of the {@link Registry} interface.
  *
  * @since 2.0
  * @author Willi Schoenborn
  */
 final class DefaultRegistry extends AbstractRegistry {
 
     private static final Logger LOG = LoggerFactory.getLogger(DefaultRegistry.class);
     
     private static final Method TO_STRING;
     private static final Method EQUALS;
     private static final Method HASHCODE;
     
     static {
         try {
             TO_STRING = Object.class.getMethod("toString");
             EQUALS = Object.class.getMethod("equals", Object.class);
             HASHCODE = Object.class.getMethod("hashCode");
         } catch (NoSuchMethodException e) {
             throw new ExceptionInInitializerError(e);
         }
     }
 
     private final Multimap<Key<? extends Object>, Object> mapping;
     
     public DefaultRegistry() {
         final SetMultimap<Key<? extends Object>, Object> multimap = LinkedHashMultimap.create();
         this.mapping = Multimaps.synchronizedSetMultimap(multimap);
     }
 
     @Override
     public <T> void register(Key<T> key, T listener) {
         Preconditions.checkNotNull(key, "Key");
         Preconditions.checkNotNull(listener, "Listener");
         LOG.trace("Registering {} for {}", listener, key);
         mapping.put(key, listener);
     }
 
     @Override
     public <T> Iterable<T> getListeners(Key<T> key) {
         Preconditions.checkNotNull(key, "Key");
         @SuppressWarnings("unchecked")
         final Iterable<T> listeners = (Iterable<T>) mapping.get(key);
         return Iterables.unmodifiableIterable(listeners);
     }
     
     @Override
     public <T> Iterable<T> find(final Class<T> type, final Predicate<? super Object> predicate) {
         Preconditions.checkNotNull(type, "Type");
         Preconditions.checkNotNull(predicate, "Predicate");
         
         return new Iterable<T>() {
             
             @Override
             public Iterator<T> iterator() {
                 return new AbstractIterator<T>() {
                     
                     private final Iterator<Entry<Key<?>, Object>> iterator =
                         mapping.entries().iterator();
                     
                     @Override
                     protected T computeNext() {
                         while (iterator.hasNext()) {
                             final Entry<Key<?>, Object> entry = iterator.next();
                             final Key<?> key = entry.getKey();
                             if (key.getType() == type && predicate.apply(key.getMeta())) {
                                 @SuppressWarnings("unchecked")
                                 final T listener = (T) entry.getValue();
                                 return listener;
                             }
                         }
                         return endOfData();
                     }
                     
                 };
             }
             
         };
     }
     
     @Override
     public <T> T proxy(final Key<T> key) {
         return proxy(key, false);
     }
     
     @Override
     public <T> T silentProxy(Key<T> key) {
         return proxy(key, true);
     }
     
     private <T> T proxy(Key<T> key, boolean silent) {
         Preconditions.checkNotNull(key, "Key");
         Preconditions.checkArgument(key.getType().isInterface(), "Type must be an interface");
         Preconditions.checkArgument(!key.getType().isAnnotation(), "Type must not be an annotation");
         
         final ClassLoader loader = getClass().getClassLoader();
         final Class<?>[] interfaces = {key.getType()};
         final InvocationHandler handler = new ProxyHandler<T>(key, silent);
         
         @SuppressWarnings("unchecked")
         final T proxy = (T) java.lang.reflect.Proxy.newProxyInstance(loader, interfaces, handler);
         LOG.debug("Created proxy for {}", key);
         return proxy;
     }
     
     /**
      * Inner class allowing to encapsulate proxy invocation handling.
      *
      * @author Willi Schoenborn
      * @param <T>
      */
     private final class ProxyHandler<T> implements InvocationHandler {
         
         private final Key<T> key;
         
         private final boolean silent;
         
         public ProxyHandler(Key<T> key, boolean silent) {
             this.key = Preconditions.checkNotNull(key, "Key");
             this.silent = silent;
         }
         
         @Override
         public Object invoke(Object proxy, final Method method, final Object[] args) 
             throws IllegalAccessException, InvocationTargetException {
             if (method.equals(TO_STRING)) {
                 return String.format("%s.proxy(%s)", DefaultRegistry.this, key);
             } else if (method.equals(EQUALS)) {
                 return equals(args[0]);
             } else if (method.equals(HASHCODE)) {
                 return hashCode();
             } else if (method.getReturnType() == void.class) {
                final Procedure<T> procedure = new Procedure<T>() {
 
                     @Override
                     public void apply(T listener) {
                         try {
                             method.invoke(listener, args);
                         } catch (IllegalAccessException e) {
                             throw new AssertionError(e);
                         } catch (InvocationTargetException e) {
                             throw Throwables.sneakyThrow(e.getCause());
                         }
                     }
 
                 };
                 if (silent) {
                     DefaultRegistry.this.notifySilently(key, procedure);
                 } else {
                     DefaultRegistry.this.notify(key, procedure);
                 }
                 return null;
             } else {
                 throw new IllegalStateException(String.format("%s must return void", method));
             }
         }
 
         @Override
         public int hashCode() {
             final int prime = 31;
             int result = 1;
             result = prime * result + getOuterType().hashCode();
             result = prime * result + ((key == null) ? 0 : key.hashCode());
             return result;
         }
 
         @Override
         public boolean equals(Object obj) {
             if (this == obj) {
                 return true;
             }
             if (obj == null) {
                 return false;
             }
             if (!(obj instanceof ProxyHandler<?>)) {
                 return false;
             }
             final ProxyHandler<?> other = (ProxyHandler<?>) obj;
             if (!getOuterType().equals(other.getOuterType())) {
                 return false;
             }
             if (key == null) {
                 if (other.key != null) {
                     return false;
                 }
             } else if (!key.equals(other.key)) {
                 return false;
             }
             return true;
         }
 
         private DefaultRegistry getOuterType() {
             return DefaultRegistry.this;
         }
         
     }
     
     @Override
     public <T> void notify(Key<T> key, Procedure<? super T> command) {
         Preconditions.checkNotNull(key, "Key");
         Preconditions.checkNotNull(command, "Command");
         LOG.trace("Notifying all listeners for {} using {}", key, command);
         for (T listener : getListeners(key)) {
             LOG.trace("notifying {} for {}", listener, key);
             command.apply(listener);
         }
     }
 
     @Override
     public <T> void notifySilent(Key<T> key, Procedure<? super T> command) {
         notifySilently(key, command);
     }
     
     @Override
     public <T> void notifySilently(Key<T> key, Procedure<? super T> command) {
         Preconditions.checkNotNull(key, "Key");
         Preconditions.checkNotNull(command, "Command");
         LOG.trace("Notifying all listeners for {} using {}", key, command);
         for (T listener : getListeners(key)) {
             LOG.trace("notifying {} for {}", listener, key);
             try {
                 command.apply(listener);
             /*CHECKSTYLE:OFF*/
             } catch (RuntimeException e) {
             /*CHECKSTYLE:ON*/
                 LOG.error("Notifying listener failed", e);
             }
         }    
     }
     
     @Override
     public <T> boolean remove(Key<T> key, T listener) {
         Preconditions.checkNotNull(key, "Key");
         Preconditions.checkNotNull(listener, "Listener");
         LOG.trace("Removing {} from {}", listener, key);
         return mapping.remove(key, listener);
     }
 
     @Override
     public <T> boolean remove(T listener) {
         Preconditions.checkNotNull(listener, "Listener");
         LOG.trace("Removing {}", listener);
         return mapping.values().removeAll(ImmutableSet.of(listener));
     }
 
     @Override
     @SuppressWarnings("unchecked")
     public <T> Iterable<T> removeAll(Key<T> key) {
         Preconditions.checkNotNull(key, "Key");
         LOG.trace("Removing all listeners from {}", key);
         return (Iterable<T>) mapping.removeAll(key);
     }
 
 }
