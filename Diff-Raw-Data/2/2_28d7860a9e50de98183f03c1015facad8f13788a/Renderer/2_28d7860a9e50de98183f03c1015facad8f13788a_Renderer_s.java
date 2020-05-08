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
 
 import java.util.Calendar;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.Map;
 
 import javax.annotation.Nonnull;
 import javax.annotation.Nullable;
 
 import de.cosmocode.patterns.Builder;
 
 /**
  * A {@link Renderer} can be used to create data structures by
  * providing a fluent api. 
  * 
  * The data structures must adhere to the <a href="http://json.org">json</a> grammar:
  * <pre>
  * object
  *     {}
  *     { members }
  * members
  *     pair
  *     pair , members
  *     pair
  *     string : value
  * array
  *     []
  *     [ elements ]
  * elements
  *     value 
  *     value , elements
  * value
  *     string
  *     number
  *     object
  *     array
  *     true
  *     false
  *     null
  * </pre>
  * 
  * Nevertheless does the renderer not make assumptions about the format
  * of the built structure. This decision is left to sub classes.
  *
  * @author Willi Schoenborn
  */
 public interface Renderer extends Builder<Object> {
 
     /**
      * Starts a list structure.
      * 
      * @return this
      * @throws RenderingException if list is not allowed at the current position.
      */
     Renderer list() throws RenderingException;
     
     /**
      * Ends a list structure.
      * 
      * @return this
      * @throws RenderingException if there is no list to end at the current position
      */
     Renderer endList() throws RenderingException;
     
     /**
      * Starts a map structure.
      * 
      * @return this
      * @throws RenderingException if map is not allowed at the current position
      */
     Renderer map() throws RenderingException;
     
     /**
      * Ends a map structure.
      * 
      * @return this
      * @throws RenderingException if there is no map to end at the current position
      */
     Renderer endMap() throws RenderingException;
     
     /**
      * Adds the specified key.
      * 
      * @param key the key being added
      * @return this
      * @throws RenderingException if currently not inside a map
      */
     Renderer key(@Nullable CharSequence key) throws RenderingException;
     
     /**
      * Adds the specified key.
      * 
      * @param key the key being added
      * @return this
      * @throws RenderingException if currently not inside a map
      */
     Renderer key(@Nullable Object key) throws RenderingException;
     
     /**
      * Adds a null value.
      * 
      * @return this
      * @throws RenderingException if no value is allowed at the current position
      */
     Renderer nullValue() throws RenderingException;
     
     /**
      * Adds the specified value.
      * 
      * @param value the value being added
      * @return this
      * @throws RenderingException if the given value is not allowed at the current position
      */
     Renderer value(@Nullable Object value) throws RenderingException;
     
     /**
      * Adds the specified value using the given renderer for conversion. Null values are permitted
      * and passed to the renderer.
      * 
      * @param <T> the generic value type
      * @param value the value being added
      * @param renderer the value renderer used to convert value
      * @return this
      * @throws RenderingException if the given value is not allowed at the current position
      * @throws NullPointerException if renderer is null
      */
     <T> Renderer value(@Nullable T value, @Nonnull ValueRenderer<T> renderer) throws RenderingException;
     
     /**
      * Adds the specified value.
      * 
      * @param value the value being added
      * @return this
      * @throws RenderingException if no value is allowed at the current position
      */
     Renderer value(boolean value) throws RenderingException;
 
     /**
      * Adds the specified value.
      * 
      * @param value the value being added
      * @return this
      * @throws RenderingException if no value is allowed at the current position
      */
     Renderer value(long value) throws RenderingException;
 
     /**
      * Adds the specified value.
      * 
      * @param value the value being added
      * @return this
      * @throws RenderingException if no value is allowed at the current position
      */
     Renderer value(double value) throws RenderingException;
 
     /**
      * Adds the specified value by converting it into a unix timestamp.
      * 
      * @param value the value being added
      * @return this
      * @throws RenderingException if no value is allowed at the current position
      */
     Renderer value(@Nullable Date value) throws RenderingException;
 
     /**
      * Adds the specified value by converting it into a unix timestamp.
      * 
      * @param value the value being added
      * @return this
      * @throws RenderingException if no value is allowed at the current position
      */
     Renderer value(@Nullable Calendar value) throws RenderingException;
     
     /**
     * Adds the specified value by converting it into a unix timestamp.
      * 
      * @param value the value being added
      * @return this
      * @throws RenderingException if no value is allowed at the current position
      */
     Renderer value(@Nullable Enum<?> value) throws RenderingException;
 
     /**
      * Adds the specified value.
      * 
      * @param value the value being added
      * @return this
      * @throws RenderingException if no value is allowed at the current position
      */
     Renderer value(@Nullable CharSequence value) throws RenderingException;
     
     /**
      * Adds the specified values.
      * 
      * @param values the values being added
      * @return this
      * @throws RenderingException if no values are allowed at the current position
      * @throws NullPointerException if values is null
      */
     Renderer values(@Nonnull Object... values) throws RenderingException;
 
     /**
      * Adds the specified values.
      * 
      * @param values the values being added
      * @return this
      * @throws RenderingException if no values are allowed at the current position
      * @throws NullPointerException if values is null
      */
     Renderer values(@Nonnull Iterable<?> values) throws RenderingException;
     
     /**
      * Adds the specified values using the given renderer for conversion. Null elements are permitted
      * and passed to the renderer.
      * 
      * @param <T> the generic value type
      * @param values the values being added
      * @param renderer the value renderer used to convert all elements in values
      * @return this
      * @throws RenderingException if no values are allowed at the current position
      * @throws NullPointerException if values or renderer is null
      */
     <T> Renderer values(@Nonnull Iterable<T> values, @Nonnull ValueRenderer<T> renderer) throws RenderingException;
     
     /**
      * Adds the specified values.
      * 
      * @param values the values being added
      * @return this
      * @throws RenderingException if no values are allowed at the current position
      * @throws NullPointerException if values is null
      */
     Renderer values(@Nonnull Iterator<?> values) throws RenderingException;
 
     /**
      * Adds the specified values using the given renderer for conversion. Null elements are permitted
      * and passed to the renderer.
      * 
      * @param <T> the generic value type
      * @param values the values being added
      * @param renderer the value renderer used to convert all elements in values
      * @return this
      * @throws RenderingException if no values are allowed at the current position
      * @throws NullPointerException if values or renderer is null
      */
     <T> Renderer values(@Nonnull Iterator<T> values, @Nonnull ValueRenderer<T> renderer) throws RenderingException;
     
     /**
      * Add the specified values as a list. This is equivalent to:
      * {@code list().values(values).endList()}.
      * 
      * @param values the values being added
      * @return this
      * @throws RenderingException if list is not allowed at the current position.
      * @throws NullPointerException if values is null
      */
     Renderer value(@Nonnull Object... values) throws RenderingException;
 
     /**
      * Add the specified values as a list. This is equivalent to:
      * {@code list().values(values).endList()}.
      * 
      * @param values the values being added
      * @return this
      * @throws RenderingException if list is not allowed at the current position.
      * @throws NullPointerException if values is null
      */
     Renderer value(@Nonnull Iterable<?> values) throws RenderingException;
 
     /**
      * Adds the specified values as a list using the given renderer. This is equivalent to:
      * {@code list().values(values, renderer).endList()}.
      * 
      * @param <T> the generic value type
      * @param values the values being added
      * @param renderer the value renderer used to convert all elements in values
      * @return this
      * @throws RenderingException if list is not allowed at the current position.
      * @throws NullPointerException if values or renderer is null
      */
     <T> Renderer value(@Nonnull Iterable<T> values, @Nonnull ValueRenderer<T> renderer) throws RenderingException;
 
     /**
      * Add the specified values as a list. This is equivalent to:
      * {@code list().values(values).endList()}.
      * 
      * @param values the values being added
      * @return this
      * @throws RenderingException if list is not allowed at the current position.
      * @throws NullPointerException if values is null
      */
     Renderer value(@Nonnull Iterator<?> values) throws RenderingException;
 
     /**
      * Adds the specified values as a list using the given renderer. This is equivalent to:
      * {@code list().values(values, renderer).endList()}.
      * 
      * @param <T> the generic value type
      * @param values the values being added
      * @param renderer the value renderer used to convert all elements in values
      * @return this
      * @throws RenderingException if list is not allowed at the current position.
      * @throws NullPointerException if values or renderer is null
      */
     <T> Renderer value(@Nonnull Iterator<T> values, @Nonnull ValueRenderer<T> renderer) throws RenderingException;
     
     /**
      * Adds the specified pairs. Pairs a basically a sequence of
      * {@code key(entry.getKey()).value(entry.getValue())} calls.
      * 
      * @param pairs the pairs being added.
      * @return this
      * @throws RenderingException if no pairs are allowed at the current position
      * @throws NullPointerException if pairs is null
      */
     Renderer pairs(@Nonnull Map<?, ?> pairs) throws RenderingException;
 
     /**
      * Adds the specified pairs using the given renderer for value conversion. The keys will be passed
      * directly to {@link #key(Object)}.
      * 
      * @param <T> the generic value type
      * @param pairs the pairs being added
      * @param renderer the value renderer used to convert all values in pairs
      * @return this
      * @throws RenderingException if no pairs are allowed at the current position
      * @throws NullPointerException if pairs or renderer is null
      */
     <T> Renderer pairs(@Nonnull Map<?, T> pairs, @Nonnull ValueRenderer<T> renderer) throws RenderingException;
     
     /**
      * Adds the specified pairs by passing control to {@link Renderable#render(Renderer, RenderingLevel)}
      * of the given renderable using the specified level.
      * 
      * @param pairs the pairs being added
      * @param level the level being passed to the renderable
      * @return this
      * @throws RenderingException if no pairs are allowed at the current position
      * @throws NullPointerException if pairs or level is null
      */
     Renderer pairs(@Nonnull Renderable pairs, @Nonnull RenderingLevel level) throws RenderingException;
     
     /**
      * Adds the specified pairs as a map. This is equivalent to:
      * {@code map().pairs(pairs).endMap()}.
      * 
      * @param pairs the pairs being added
      * @return this
      * @throws RenderingException if map is not allowed at the current position
      * @throws NullPointerException if pairs is null
      */
     Renderer value(@Nonnull Map<?, ?> pairs) throws RenderingException;
 
     /**
      * Adds the specified pairs as a map using the given renderer for value conversion. The keys will be passed
      * directly to {@link #key(Object)}. This is equivalent to:
      * {@code map().pairs(pairs, renderer).endMap()}.
      * 
      * @param <T> the generic value type
      * @param pairs the pairs being added
      * @param renderer the value renderer used to convert all values in pairs
      * @return this
      * @throws RenderingException if map is not allowed at the current position
      * @throws NullPointerException if pairs or renderer is null
      */
     <T> Renderer value(@Nonnull Map<?, T> pairs, @Nonnull ValueRenderer<T> renderer) throws RenderingException;
     
     /**
      * Adds the specified pairs by passing control to {@link Renderable#render(Renderer, RenderingLevel)}
      * of the given renderable using the specified level. This is equivalent to:
      * {@code map().pairs(pairs, level).endMap()}.
      * 
      * @param pairs the pairs being added
      * @param level the level being passed to the renderable
      * @return this
      * @throws RenderingException if map is not allowed at the current position
      * @throws NullPointerException if pairs or level is null
      */
     Renderer value(@Nonnull Renderable pairs, @Nonnull RenderingLevel level) throws RenderingException;
     
     /**
      * Builds the final data structure.
      * <p>
      *   Implementation note: This method is usually overridden by subclasses
      *   to narrow the return value to a more appropriate type.
      * </p>
      * 
      * @return the built structure
      * @throws RenderingException if the structure is not finished yet
      */
     @Override
     Object build() throws RenderingException;
     
 }
