 // Copyright (C) 2012 The Android Open Source Project
 //
 // Licensed under the Apache License, Version 2.0 (the "License");
 // you may not use this file except in compliance with the License.
 // You may obtain a copy of the License at
 //
 // http://www.apache.org/licenses/LICENSE-2.0
 //
 // Unless required by applicable law or agreed to in writing, software
 // distributed under the License is distributed on an "AS IS" BASIS,
 // WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 // See the License for the specific language governing permissions and
 // limitations under the License.
 
 package com.google.gerrit.extensions.registration;
 
 import com.google.inject.Binder;
 import com.google.inject.Key;
 import com.google.inject.Scopes;
 import com.google.inject.TypeLiteral;
 import com.google.inject.util.Types;
 
 import java.util.Collections;
 import java.util.Map;
 import java.util.SortedMap;
 import java.util.SortedSet;
 import java.util.TreeMap;
 import java.util.TreeSet;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentMap;
 
 /**
  * A map of members that can be modified as plugins reload.
  * <p>
  * Maps index their members by plugin name and export name.
  * <p>
  * DynamicMaps are always mapped as singletons in Guice, and only may contain
  * singletons, as providers are resolved to an instance before the member is
  * added to the map.
  */
 public abstract class DynamicMap<T> {
   /**
    * Declare a singleton {@code DynamicMap<T>} with a binder.
    * <p>
    * Maps must be defined in a Guice module before they can be bound:
    *
    * <pre>
    * DynamicMap.mapOf(binder(), Interface.class);
    * bind(Interface.class)
    *   .annotatedWith(Exports.named(&quot;foo&quot;))
    *   .to(Impl.class);
    * </pre>
    *
    * @param binder a new binder created in the module.
    * @param member type of value in the map.
    */
   public static <T> void mapOf(Binder binder, Class<T> member) {
     mapOf(binder, TypeLiteral.get(member));
   }
 
   /**
    * Declare a singleton {@code DynamicMap<T>} with a binder.
    * <p>
    * Maps must be defined in a Guice module before they can be bound:
    *
    * <pre>
    * DynamicMap.mapOf(binder(), new TypeLiteral<Thing<Bar>>(){});
    * bind(new TypeLiteral<Thing<Bar>>() {})
    *   .annotatedWith(Exports.named(&quot;foo&quot;))
    *   .to(Impl.class);
    * </pre>
    *
    * @param binder a new binder created in the module.
    * @param member type of value in the map.
    */
   public static <T> void mapOf(Binder binder, TypeLiteral<T> member) {
     @SuppressWarnings("unchecked")
     Key<DynamicMap<T>> key = (Key<DynamicMap<T>>) Key.get(
         Types.newParameterizedType(DynamicMap.class, member.getType()));
     binder.bind(key)
         .toProvider(new DynamicMapProvider<T>(member))
         .in(Scopes.SINGLETON);
   }
 
   final ConcurrentMap<NamePair, T> items;
 
   DynamicMap() {
     items = new ConcurrentHashMap<NamePair, T>(16, 0.75f, 1);
   }
 
   /**
    * Lookup an implementation by name.
    *
    * @param pluginName local name of the plugin providing the item.
    * @param exportName name the plugin exports the item as.
    * @return the implementation. Null if the plugin is not running, or if the
    *         plugin does not export this name.
    */
   public T get(String pluginName, String exportName) {
     return items.get(new NamePair(pluginName, exportName));
   }
 
   /**
    * Get the names of all running plugins supplying this type.
    *
    * @return sorted set of active plugins that supply at least one item.
    */
   public SortedSet<String> plugins() {
     SortedSet<String> r = new TreeSet<String>();
     for (NamePair p : items.keySet()) {
       r.add(p.pluginName);
     }
     return Collections.unmodifiableSortedSet(r);
   }
 
   /**
    * Get the items exported by a single plugin.
    *
    * @param pluginName name of the plugin.
    * @return items exported by a plugin, keyed by the export name.
    */
   public SortedMap<String, T> byPlugin(String pluginName) {
     SortedMap<String, T> r = new TreeMap<String, T>();
     for (Map.Entry<NamePair, T> e : items.entrySet()) {
       if (e.getKey().pluginName.equals(pluginName)) {
         r.put(e.getKey().exportName, e.getValue());
       }
     }
     return Collections.unmodifiableSortedMap(r);
   }
 
   static class NamePair {
     private final String pluginName;
     private final String exportName;
 
     NamePair(String pn, String en) {
       this.pluginName = pn;
       this.exportName = en;
     }
 
     @Override
     public int hashCode() {
       return pluginName.hashCode() * 31 + exportName.hashCode();
     }
 
     @Override
     public boolean equals(Object other) {
       if (other instanceof NamePair) {
         NamePair np = (NamePair) other;
        return pluginName.equals(np) && exportName.equals(np);
       }
       return false;
     }
   }
 }
