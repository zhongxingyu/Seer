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
 
 package de.cosmocode.rendering;
 
 import java.io.InputStream;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.Map;
 import java.util.SortedMap;
 import java.util.Map.Entry;
 
 import com.google.common.base.Preconditions;
 import com.google.common.collect.ImmutableSortedMap;
 import com.google.common.collect.Maps;
 import com.google.common.collect.Multimap;
 import com.google.common.collect.Ordering;
 import com.google.common.collect.ImmutableSortedMap.Builder;
 
 import de.cosmocode.commons.reflect.Reflection;
 
 /**
  * Utility class for {@link Mapping}s.
  *
  * @since 1.1
  * @author Willi Schoenborn
  */
 final class Mappings {
 
     private static final Mapping DEFAULT;
 
     static {
         final Ordering<Class<?>> ordering = Reflection.orderByHierarchy();
         final Builder<Class<?>, ValueRenderer<?>> builder = ImmutableSortedMap.orderedBy(ordering);
 
         builder.put(byte[].class, ByteArrayValueRenderer.INSTANCE);
         builder.put(Calendar.class, CalendarValueRenderer.INSTANCE);
         builder.put(Date.class, DateValueRenderer.INSTANCE);
         builder.put(Enum.class, EnumValueRenderer.INSTANCE);
         builder.put(InputStream.class, InputStreamValueRenderer.INSTANCE);
         builder.put(Multimap.class, MultimapValueRenderer.INSTANCE);
         builder.put(Object.class, ObjectValueRenderer.INSTANCE);
         
         DEFAULT = newMapping(builder.build());
     }
     
     private Mappings() {
         
     }
     
     /**
      * Returns the immutable default mapping. The default mapping includes:
      * 
      * <table>
      *   <tr>
      *     <th>Type</th>
      *     <th>{@link ValueRenderer}</th>
      *     <th>Description</th>
      *   </tr>
      *   <tr>
      *     <td>{@code byte[]}</td>
      *     <td>{@link ByteArrayValueRenderer}</td>
      *     <td>produces an UTF-8 Base64 encoded String</td>
      *   </tr>
      *   <tr>
      *     <td>{@link Calendar}</td>
      *     <td>{@link CalendarValueRenderer}</td>
      *     <td>uses {@link Calendar#getTime()} and delegates to {@link Renderer#value(Date)}</td>
      *   </tr>
      *   <tr>
      *     <td>{@link Date}</td>
      *     <td>{@link DateValueRenderer}</td>
      *     <td>renderes the unix timestamp (seconds)</td>
      *   </tr>
      *   <tr>
      *     <td>{@link Enum}</td>
      *     <td>{@link EnumValueRenderer}</td>
      *     <td>uses {@link Enum#name()}</td>
      *   </tr>
      *   <tr>
      *     <td>{@link InputStream}</td>
      *     <td>{@link InputStreamValueRenderer}</td>
      *     <td>collects all bytes and delegates to {@link Renderer#value(byte[])}</td>
      *   </tr>
      *   <tr>
      *     <td>{@link Multimap}</td>
      *     <td>{@link MultimapValueRenderer}</td>
      *     <td>uses {@link Multimap#asMap()}</td>
      *   </tr>
      *   <tr>
      *     <td>{@link Object}</td>
      *     <td>{@link ObjectValueRenderer}</td>
      *     <td>uses {@link Object#toString()}</td>
      *   </tr>
      * </table>
      * 
      * @since 1.1
      * @return an immutable mapping containing all predefined mapping entries
      */
     public static Mapping defaultMapping() {
         return Mappings.DEFAULT;
     }
     
     /**
      * Returns a new mutable {@link Mapping} populated with the default mapping entries.
      * 
      * @since 1.1
      * @return a mutable mapping
      */
     public static Mapping newMapping() {
        return newMapping(Maps.newTreeMap(Mappings.defaultMapping()));
     }
 
     /**
      * Returns a mapping backed by the given map
      * Changes in the supplied map will be visible in the returned mapping and
      * vice versa.
      * 
      * @since 1.1
      * @param map the backing map
      * @return a mapping backed by the given map
      * @throws NullPointerException if map is null
      */
     public static Mapping newMapping(SortedMap<Class<?>, ValueRenderer<?>> map) {
         if (map instanceof Mapping) {
             return Mapping.class.cast(map);
         } else {
             return new DefaultMapping(map);
         }
     }
     
     /**
      * Finds the most appropriate {@link ValueRenderer} in the given map for the supplied type.
      * This is a reusable method for {@link Mapping} implementation.
      * 
      * @since 1.1
      * @param <T> the generic class type
      * @param renderers the map to look in
      * @param type the type to look for
      * @return the {@link ValueRenderer} capable of rendering instances of T or null if no renderer was found
      * @throws NullPointerException if renderers or type is null
      */
     static <T> ValueRenderer<T> find(Map<Class<?>, ValueRenderer<?>> renderers, Class<? extends T> type) {
         Preconditions.checkNotNull(renderers, "Renderers");
         return find(renderers.entrySet(), type);
     }
 
     
     /**
      * Finds the most appropriate {@link ValueRenderer} in the given map for the supplied type.
      * This is a reusable method for {@link Mapping} implementation.
      * 
      * @since 1.1
      * @param <T> the generic class type
      * @param renderers the entries to look in
      * @param type the type to look for
      * @return the {@link ValueRenderer} capable of rendering instances of T or null if no renderer was found
      * @throws NullPointerException if renderers or type is null
      */
     static <T> ValueRenderer<T> find(Iterable<Entry<Class<?>, ValueRenderer<?>>> renderers, Class<? extends T> type) {
         Preconditions.checkNotNull(renderers, "Renderers");
         Preconditions.checkNotNull(type, "Type");
         for (Entry<Class<?>, ValueRenderer<?>> entry : renderers) {
             if (entry.getKey().isAssignableFrom(type)) {
                 @SuppressWarnings("unchecked")
                 final ValueRenderer<T> renderer = (ValueRenderer<T>) entry.getValue();
                 return renderer;
             }
         }
         return null;
     }
 
 }
