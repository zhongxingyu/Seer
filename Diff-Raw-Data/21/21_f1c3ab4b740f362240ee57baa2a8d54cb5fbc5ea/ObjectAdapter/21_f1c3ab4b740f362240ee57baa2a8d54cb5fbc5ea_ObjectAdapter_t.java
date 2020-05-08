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
 
 package de.cosmocode.palava.ipc.xml.rpc.adapters;
 
 import java.io.InputStream;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 import java.util.SortedMap;
 import java.util.Map.Entry;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.common.base.Preconditions;
 import com.google.common.collect.ImmutableSortedMap;
 import com.google.common.collect.Ordering;
 import com.google.common.collect.ImmutableSortedMap.Builder;
 import com.google.inject.Inject;
 import com.google.inject.TypeLiteral;
 
 import de.cosmocode.commons.Calendars;
 import de.cosmocode.palava.ipc.xml.rpc.XmlRpc;
 import de.cosmocode.palava.ipc.xml.rpc.generated.ObjectFactory;
 import de.cosmocode.palava.ipc.xml.rpc.generated.Value;
 
 /**
  * A {@link Object} to {@link Object} adapter.
  *
  * @since 1.0
  * @author Willi Schoenborn
  */
 final class ObjectAdapter implements Adapter<Value, Object> {
 
     static final TypeLiteral<Adapter<Value, Object>> LITERAL =
         new TypeLiteral<Adapter<Value, Object>>() { };
         
     private static final Logger LOG = LoggerFactory.getLogger(ObjectAdapter.class);
     
     /**
      * Orders classes by hierarchy, sub classes are considered less than super classes.
      */
     private static final Ordering<Class<?>> HIERARCHY = new Ordering<Class<?>>() {
         
         @Override
         public int compare(Class<?> left, Class<?> right) {
             if (left.equals(right)) {
                 return 0;
             } else if (left.isAssignableFrom(right)) {
                 return 1;
             } else if (right.isAssignableFrom(left)) {
                 return -1;
             } else {
                 return left.getName().compareTo(right.getName());
             }
         }
         
     };
 
     private final Value nullValue;
     
     private final SortedMap<Class<?>, Adapter<Value, ?>> adapters;
     
     @Inject
     public ObjectAdapter(
         @XmlRpc ObjectFactory factory,
         Adapter<Value, Boolean> booleanAdapter,
         Adapter<Value, Date> dateAdapter,
         Adapter<Value, Double> doubleAdapter,
         Adapter<Value, InputStream> streamAdapter,
         Adapter<Value, Integer> integerAdapter,
         Adapter<Value, List<Object>> listAdapter,
         Adapter<Value, Map<String, Object>> mapAdapter,
         Adapter<Value, Number> numberAdapter,
         Adapter<Value, String> stringAdapter) {
         
         Preconditions.checkNotNull(factory, "Factory");
         this.nullValue = factory.createValue();
         this.nullValue.getContent().add(factory.createValueString("null"));
         
         final Builder<Class<?>, Adapter<Value, ?>> builder = ImmutableSortedMap.orderedBy(HIERARCHY);
         
         builder.put(Number.class, numberAdapter);
         builder.put(Boolean.class, booleanAdapter);
         builder.put(Calendar.class, Adapters.composeEncoder(dateAdapter, Calendars.getTime()));
         builder.put(Date.class, dateAdapter);
         builder.put(Double.class, doubleAdapter);
         builder.put(InputStream.class, streamAdapter);
         builder.put(Integer.class, integerAdapter);
         builder.put(List.class, listAdapter);
         builder.put(Map.class, mapAdapter);
         builder.put(String.class, stringAdapter);
         
         this.adapters = builder.build();
     }
     
     private Adapter<Value, ?> getDecoder(Value value) {
         final ValueType type = ValueType.of(value);
         final Class<?> javaType = type.getType();
         final Adapter<Value, ?> adapter = adapters.get(javaType);
         Preconditions.checkState(adapter != null, "No adapter configured for %s", javaType);
         return adapter;
     }
     
     @SuppressWarnings("unchecked")
     private Adapter<Value, Object> getEncoder(Class<?> type) {
         for (Entry<Class<?>, Adapter<Value, ?>> entry : adapters.entrySet()) {
             if (entry.getKey().isAssignableFrom(type)) {
                 return (Adapter<Value, Object>) entry.getValue();
             }
         }
         return null;
     }
     
     @Override
     public Object decode(Value input) {
         Preconditions.checkNotNull(input, "Input");
         return getDecoder(input).decode(input);
     }
     
     @Override
     public Value encode(Object input) {
         if (input == null) return nullValue;
         final Adapter<Value, Object> adapter = getEncoder(input.getClass());
         if (adapter == null) {
             Preconditions.checkState(!(input instanceof String), "No adapter configured for Strings");
             LOG.debug("No adapter configured for {}, using toString", input.getClass());
             return encode(input.toString());
         }
         return adapter.encode(input);
     }
 
 }
