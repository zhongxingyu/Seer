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
 
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Map;
 
 import com.google.common.base.Preconditions;
 import com.google.common.base.Predicates;
 import com.google.common.collect.Collections2;
 import com.google.common.collect.ForwardingMap;
 
 import de.cosmocode.commons.reflect.Reflection;
 
 /**
  * {@link Mapping} implementation which traverses the object hierarchy when
  * {@link Mapping#find(Class)} is called to find the most appropriate type mapping.
  *
  * @since 1.1
  * @author Willi Schoenborn
  */
 public final class SuperClassMapping extends ForwardingMap<Class<?>, ValueRenderer<?>> implements Mapping {
 
     private final Map<Class<?>, ValueRenderer<?>> renderers;
 
     public SuperClassMapping(Map<Class<?>, ValueRenderer<?>> renderers) {
         this.renderers = Preconditions.checkNotNull(renderers, "Renderers");
     }
 
     @Override
     protected Map<Class<?>, ValueRenderer<?>> delegate() {
         return renderers;
     }
     
     @Override
     public <T> ValueRenderer<T> find(Class<? extends T> type) {
         Preconditions.checkNotNull(type, "Type");
         final ValueRenderer<?> classRenderer = doFind(type);
         if (classRenderer == null) {
             final Collection<Class<?>> interfaces = Arrays.asList(type.getInterfaces());
             @SuppressWarnings("unchecked")
             final ValueRenderer<T> renderer = (ValueRenderer<T>) doFind(interfaces);
             return renderer;
         } else {
             @SuppressWarnings("unchecked")
             final ValueRenderer<T> renderer = (ValueRenderer<T>) classRenderer;
             return renderer;
         }
     }
     
     private ValueRenderer<?> doFind(Class<?> type) {
         final ValueRenderer<?> renderer = renderers.get(type);
         if (renderer == null) {
            if (Object.class.equals(type)) {
                return null;
            } else {
                return doFind(type.getSuperclass());
            }
         } else {
             return renderer;
         }
     }
 
     // traverses the inheritence tree in level order
     private ValueRenderer<?> doFind(Collection<Class<?>> interfaces) {
        if (interfaces.isEmpty()) return null;
         for (Class<?> type : interfaces) {
             final ValueRenderer<?> renderer = renderers.get(type);
             if (renderer == null) continue;
             return renderer;
         }
         return doFind(transform(interfaces));
     }
     
     private Collection<Class<?>> transform(Collection<Class<?>> interfaces) {
         // uses the super class of each type
         final Collection<Class<?>> transformed = Collections2.transform(interfaces, Reflection.getSuperClass());
         // removes all nulls (no supertypes)
         return Collections2.filter(transformed, Predicates.notNull());
     }
     
 }
