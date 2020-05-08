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
 
 import java.util.Collection;
 import java.util.Map;
 
 import javax.annotation.CheckForNull;
 
 /**
  * Class to hold factory methods to return the implementation of {@link Lookup}.
  * 
  * @author Kenneth Xu
  * 
  */
 public final class Lookups {
 
     private Lookups() {
     }
 
     /**
      * Creates a lookup based on given map. This is equivalent to {@link #create(Map, Object) create(map, null)}.
      * <p>
      * Please note that the created lookup uses a copy of the map passed to ensure the thread safety and performance. So
      * subsequent change to the map doesn't affect the create lookup object.
      * 
      * @param map
      *            the map to create the lookup
      * 
      * @param <T>
      *            type of the reference object to be looked up
      * @return an implementation of {@link Lookup} that is backed by given map
      * 
      */
     public static <T> Lookup<T> create(final Map<? extends Object, ? extends T> map) {
         return create(map, null);
     }
 
     /**
      * Creates a lookup based on given map.
      * <p>
      * Please note that the created lookup uses a copy of the map passed to ensure the thread safety and performance. So
      * subsequent change to the map doesn't affect the create lookup object.
      * 
      * @param map
      *            the map to create the lookup
      * @param defaultValue
      *            the default value to be used if the lookup object is not found
      * 
      * @param <T>
      *            type of the reference object to be looked up
      * @return an implementation of {@link Lookup} that is backed by given map
      * 
      */
     public static <T> Lookup<T> create(final Map<? extends Object, ? extends T> map, @CheckForNull T defaultValue) {
         if (map == null) throw new IllegalArgumentException(Utils.notNull("map"));
         return new MapBasedLookup<T>(map, defaultValue);
     }
 
     /* values, string */
 
     /**
      * Create a lookup for objects in the given collection indexed by given property.
      * <p>
      * The class where the index property is searched for is determined by the first non-null element in the collection.
      * Other than that, this is equivalent to {@link #create(Collection, Class, String)}.
      * 
      * @param values
      *            a collection of objects that can be looked up.
      * @param property
      *            the index property
      * @param <T>
      *            type of the reference object to be looked up
      * @return an implementation of {@link Lookup} indexed by the specified property
      */
     public static <T> Lookup<T> create(final Collection<? extends T> values, final String property) {
         checkValues(values);
         if (property == null) throw new IllegalArgumentException(Utils.notNull("property"));
         return new MapBasedLookup<T>(null, values, new PropertyConverter<T>(getElementClass(values), property));
     }
 
     /**
      * Create a two level lookup for objects in the given collection indexed by given properties.
      * <p>
      * The class where the index properties are searched for is determined by the first non-null element in the
      * collection. Other than that, this is equivalent to {@link #create(Collection, Class, String, String)}.
      * 
      * @param values
      *            a collection of objects that can be looked up.
      * @param property1
      *            the first index property
      * @param property2
      *            the second index property
      * @param <T>
      *            type of the reference object to be looked up
      * @return an implementation of {@link Lookup} of {@code Lookup} indexed by the specified properties in order
      */
     public static <T> Lookup<Lookup<T>> create(final Collection<? extends T> values, final String property1,
             final String property2) {
         return Utils.toLookup2(create(values, new String[] { property1, property2 }));
     }
 
     /**
      * Create multilevel lookup for objects in the given collection indexed by given properties.
      * <p>
      * The class where the index properties are searched for is determined by the first non-null element in the
      * collection. Other than that, it is equivalent to {@link #create(Collection, Class, String...)}.
      * 
      * @param values
      *            a collection of objects that can be looked up.
      * @param properties
      *            the index properties
      * @param <T>
      *            type of the reference object to be looked up
      * @return an implementation of multilevel {@link Lookup} indexed by the specified properties
      */
     public static <T> Lookup<?> create(final Collection<? extends T> values, final String... properties) {
         checkValues(values);
         return new LookupBuilder<T>(null, getElementClass(values), properties).build(values);
     }
 
     /* default, values, string */
 
     /**
      * Create a lookup for objects in the given collection indexed by given property. The created lookup has default
      * value returned when {@link Lookup#get(Object)} cannot find a reference object.
      * <p>
      * The class where index properties are searched for is determined by the actual type of {@code defaultValue} if it
      * is not null. Otherwise, the actual type of first non {@code null} element in values collection is used.
      * <p>
      * When {@code defaultValue} is not null, this is equivalent to {@link #create(Object, Collection, Class, String)
      * create(defaultValue, values, defaultValue.getClass(), property)}. Otherwise this is equivalent to
      * {@link #create(Collection, String) create(values, property)}.
      * 
      * @param defaultValue
      *            the default value to be used if the lookup object is not found
      * @param values
      *            a collection of objects that can be looked up.
      * @param property
      *            the index property
      * @param <T>
      *            type of the reference object to be looked up
      * @return an implementation of {@link Lookup} indexed by the specified property
      */
     public static <T> Lookup<T> create(@CheckForNull T defaultValue, final Collection<? extends T> values,
             final String property) {
         return defaultValue != null ? create(defaultValue, values, Utils.getClass(defaultValue), property) : create(
                 values, property);
     }
 
     /**
      * Create a two level lookup for objects in the given collection indexed by given properties.The created lookup has
      * default value returned when {@link Lookup#get(Object)} cannot find a reference object.
      * <p>
      * The class where index properties are searched for is determined by the actual type of {@code defaultValue} if it
      * is not null. Otherwise, the actual type of first non {@code null} element in values collection is used.
      * <p>
      * When {@code defaultValue} is not null, this is equivalent to
      * {@link #create(Object, Collection, Class, String, String) create(defaultValue, values, defaultValue.getClass(),
      * property1, property2)}. Otherwise this is equivalent to {@link #create(Collection, String, String) create(values,
      * property1, property2)}.
      * 
      * @param defaultValue
      *            the default value to be used if the lookup object is not found
      * @param values
      *            a collection of objects that can be looked up.
      * @param property1
      *            the first index property
      * @param property2
      *            the second index property
      * @param <T>
      *            type of the reference object to be looked up
      * @return an implementation of {@link Lookup} of {@code Lookup} indexed by the specified properties in order
      */
     public static <T> Lookup<Lookup<T>> create(@CheckForNull T defaultValue, final Collection<? extends T> values,
             final String property1, final String property2) {
         return Utils.toLookup2(create(defaultValue, values, new String[] { property1, property2 }));
     }
 
     /**
      * Create multilevel lookup for objects in the given collection indexed by given properties. The created lookup has
      * default value returned when {@link Lookup#get(Object)} cannot find a reference object.
      * <p>
      * The class where index properties are searched for is determined by the actual type of {@code defaultValue} if it
      * is not null. Otherwise, the actual type of first non {@code null} element in values collection is used.
      * <p>
      * When {@code defaultValue} is not null, this is equivalent to
      * {@link #create(Object, Collection, Class, String...) create(defaultValue, values, defaultValue.getClass(),
      * properties)}. Otherwise this is equivalent to {@link #create(Collection, String...) create(values, properties)}.
      * 
      * @param defaultValue
      *            the default value to be used if the lookup object is not found
      * @param values
      *            a collection of objects that can be looked up.
      * @param properties
      *            the index properties
      * 
      * @param <T>
      *            type of the reference object to be looked up
      * @return an implementation of multilevel {@link Lookup} indexed by the specified properties
      */
    public static <T> Lookup<?> create(@CheckForNull T defaultValue, final Collection<? extends T> values,
            final String... properties) {
         return defaultValue != null ? create(defaultValue, values, Utils.getClass(defaultValue), properties) : create(
                 values, properties);
     }
 
     /* class, values, string */
 
     /**
      * Create a {@link Lookup} for objects in the given collection indexed by given property defined on given class.
      * This is equivalent to {@link #create(Object, Collection, Class, String) create(null, values, clazz, property)}
      * 
      * @param values
      *            a collection of objects that can be looked up
      * @param clazz
      *            the class where the index property is searched for
      * @param property
      *            the index property
      * 
      * @param <T>
      *            type of the reference object to be looked up
      * @return an implementation of {@link Lookup} indexed by the specified property
      */
     public static <T> Lookup<T> create(final Collection<? extends T> values, final Class<? extends T> clazz,
             final String property) {
         return create(null, values, clazz, property);
     }
 
     /**
      * Create a two level lookup for objects in the given collection indexed by given properties defined on given class.
      * This is equivalent to {@link #create(Object, Collection, Class, String, String) create(null, values, clazz,
      * property1, property2)}
      * 
      * @param values
      *            a collection of objects that can be looked up.
      * @param clazz
      *            the class where the index property is searched for
      * @param property1
      *            the first index property
      * @param property2
      *            the second index property
      * 
      * @param <T>
      *            type of the reference object to be looked up
      * @return an implementation of {@link Lookup} of {@code Lookup} indexed by the specified properties in order
      */
     public static <T> Lookup<Lookup<T>> create(final Collection<? extends T> values, final Class<? extends T> clazz,
             final String property1, final String property2) {
         return create(null, values, clazz, property1, property2);
     }
 
     /**
      * Create multilevel lookup for objects in the given collection indexed by the properties defined on given class.
      * This is equivalent to {@link #create(Object, Collection, Class, String...) create(null, values, clazz,
      * properties)}.
      * 
      * @param values
      *            a collection of objects that can be looked up.
      * @param clazz
      *            the class where the index property is searched for
      * @param properties
      *            the index properties
      * 
      * @param <T>
      *            type of the reference object to be looked up
      * @return an implementation of multilevel {@link Lookup} indexed by the specified properties
      */
     public static <T> Lookup<?> create(final Collection<? extends T> values, final Class<? extends T> clazz,
             final String... properties) {
         return create(null, values, clazz, properties);
     }
 
     /* class, default, values, string */
 
     /**
      * Create a lookup for objects in the given collection indexed by the property defined on given class. The created
      * lookup has default value returned when {@link Lookup#get(Object)} cannot find a reference object.
      * 
      * @param defaultValue
      *            the default value to be used if the lookup object is not found
      * @param values
      *            a collection of objects that can be looked up.
      * @param clazz
      *            the class where the index property is searched for
      * @param property
      *            the index property
      * 
      * @param <T>
      *            type of the reference object to be looked up
      * @return an implementation of {@link Lookup} indexed by the specified property
      */
     public static <T> Lookup<T> create(@CheckForNull T defaultValue, final Collection<? extends T> values,
             final Class<? extends T> clazz, final String property) {
         checkValues(values);
         if (property == null) throw new IllegalArgumentException(Utils.notNull("property"));
         if (clazz == null) throw new IllegalArgumentException(Utils.notNull("clazz"));
         return new MapBasedLookup<T>(defaultValue, values, new PropertyConverter<T>(clazz, property));
     }
 
     /**
      * Create a two level lookup for objects in the given collection indexed by the properties defined on given
      * class.The created lookup has default value returned when {@link Lookup#get(Object)} cannot find a reference
      * object.
      * <p>
      * This is equivalent to {@link #create(Object, Collection, Class, String...) (Lookup&lt;Lookup&lt;T>>)
      * create(defaultValue, values, clazz, new String[] &#123; property1, property2 &#125;)}.
      * 
      * @param defaultValue
      *            the default value to be used if the lookup object is not found
      * @param values
      *            a collection of objects that can be looked up.
      * @param clazz
      *            the class where the index property is searched for
      * @param property1
      *            the first index property
      * @param property2
      *            the second index property
      * 
      * @param <T>
      *            type of the reference object to be looked up
      * @return an implementation of {@link Lookup} of {@code Lookup} indexed by the specified properties in order
      */
     public static <T> Lookup<Lookup<T>> create(@CheckForNull T defaultValue, final Collection<? extends T> values,
             final Class<? extends T> clazz, final String property1, final String property2) {
         return Utils.toLookup2(create(defaultValue, values, clazz, new String[] { property1, property2 }));
     }
 
     /**
      * Create multilevel lookup for objects in the given collection indexed by the properties defined on given class.The
      * created lookup has default value returned when {@link Lookup#get(Object)} cannot find a reference object.
      * 
      * @param defaultValue
      *            the default value to be used if the lookup object is not found
      * @param values
      *            a collection of objects that can be looked up
      * @param clazz
      *            the class where the index property is searched for
      * @param properties
      *            the index properties
      * 
      * @param <T>
      *            type of the reference object to be looked up
      * @return an implementation of multilevel {@link Lookup} indexed by the specified properties in order
      */
     public static <T> Lookup<?> create(@CheckForNull T defaultValue, final Collection<? extends T> values,
             final Class<? extends T> clazz, final String... properties) {
         checkValues(values);
         return new LookupBuilder<T>(defaultValue, clazz, properties).build(values);
     }
 
     /* default, values, converter */
 
     static <T> Lookup<T> create(T defaultValue, final Collection<? extends T> values,
             final Converter<T, Object> converter) {
         checkValues(values);
         if (converter == null) throw new IllegalArgumentException(Utils.notNull("converter"));
         return new MapBasedLookup<T>(defaultValue, values, converter);
     }
 
     static <T> Lookup<Lookup<T>> create(T defaultValue, final Collection<? extends T> values,
             final Converter<T, Object> converter1, final Converter<T, Object> converter2) {
         return Utils.toLookup2(create(defaultValue, values, Utils.toGeneric(converter1, converter2)));
     }
 
     static <T> Lookup<?> create(T defaultValue, final Collection<? extends T> values,
             final Converter<T, Object>... converters) {
         checkValues(values);
         checkConverters(converters);
         return new LookupBuilder<T>(defaultValue, converters).build(values);
     }
 
     private static <T> Class<T> getElementClass(final Collection<? extends T> values) {
         for (T value : values)
             if (value != null) return Utils.getClass(value);
         throw new IllegalArgumentException("Argument values collection must contain non-null element");
     }
 
     private static void checkValues(final Collection<?> values) {
         if (values == null) throw new IllegalArgumentException(Utils.notNull("values"));
         if (values.size() == 0) throw new IllegalArgumentException("Argument values collection must not be empty");
     }
 
     private static void checkConverters(final Converter<?, Object>[] converters) {
         if (converters == null) throw new IllegalArgumentException(Utils.notNull("converters"));
         if (converters.length == 0) throw new IllegalArgumentException("At least one converter must be supplied");
         for (int i = 0; i < converters.length; i++) {
             if (converters[i] == null) throw new IllegalArgumentException(Utils.notNull("converter" + (i + 1)));
         }
     }
 }
