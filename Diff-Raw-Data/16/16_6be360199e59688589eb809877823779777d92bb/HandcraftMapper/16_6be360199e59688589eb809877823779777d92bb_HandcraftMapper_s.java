 /*
  * Copyright 2012 Stanley Shyiko
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.github.shyiko.mappify.handcraft;
 
 import com.github.shyiko.mappify.api.AbstractMapper;
 import com.github.shyiko.mappify.api.MappingContext;
 import com.github.shyiko.mappify.api.MappingException;
 
 import java.lang.reflect.Method;
 import java.util.Collection;
 import java.util.LinkedList;
 import java.util.Map;
 import java.util.concurrent.ConcurrentHashMap;
 
 /**
  * Simplest {@link com.github.shyiko.mappify.api.Mapper} implementation which delegates mappings to the
  * {@link Mapping}-annotated methods.
  *
  * @author <a href="mailto:stanley.shyiko@gmail.com">Stanley Shyiko</a>
  */
 public class HandcraftMapper extends AbstractMapper {
 
     protected Map<MappingIdentifier, MappingDelegate> config = new ConcurrentHashMap<MappingIdentifier, MappingDelegate>();
    private ProxyNarrowingStrategy proxyNarrowingStrategy = new HibernateProxyNarrowingStrategy();
 
     public ProxyNarrowingStrategy getProxyNarrowingStrategy() {
         return proxyNarrowingStrategy;
     }
 
     public void setProxyNarrowingStrategy(ProxyNarrowingStrategy proxyNarrowingStrategy) {
         this.proxyNarrowingStrategy = proxyNarrowingStrategy;
     }
 
     @Override
     public <T> T map(Object source, Class<T> targetClass, String mappingName, MappingContext mappingContext) {
         if (source == null) {
             return null;
         }
         assertNotNull(targetClass, "Target class cannot be null");
         assertNotNull(mappingName, "Mapping name cannot be null");
         MappingIdentifier key = new MappingIdentifier(proxyNarrowingStrategy.narrow(source),
                 targetClass, mappingName);
         return map(key, source, (T) null, mappingContext);
     }
 
     @Override
     public <T> T map(Object source, T target, String mappingName, MappingContext mappingContext) {
         assertNotNull(source, "Source object cannot be null");
         assertNotNull(target, "Target object cannot be null");
         assertNotNull(mappingName, "Mapping name cannot be null");
         MappingIdentifier key = new MappingIdentifier(proxyNarrowingStrategy.narrow(source),
                 proxyNarrowingStrategy.narrow(target), mappingName);
         return map(key, source, target, mappingContext);
     }
 
     @SuppressWarnings("unchecked")
     protected <T> T map(MappingIdentifier key, Object source, T target, MappingContext mappingContext) {
         MappingDelegate mapping = findMappingDelegate(key);
         if (mapping == null) {
             throw new MappingDefinitionNotFoundException("Mapping from " + key.sourceClass + " to " +
                     key.targetClass + " has not been defined");
         }
         try {
             if (mapping.returnsTarget) {
                 if (target != null) {
                     throw new MappingException("'" + key + "' cannot be used for overlay mapping");
                 }
                 return (T) mapping.method.invoke(mapping.delegatee, mapping.requiresContext ?
                         new Object[]{source, mappingContext} : new Object[]{source});
             }
             if (target == null) {
                 target = (T) newInstance(key.targetClass);
             }
             mapping.method.invoke(mapping.delegatee, mapping.requiresContext ?
                     new Object[]{source, target, mappingContext} : new Object[]{source, target});
             return target;
         } catch (Exception e) {
             throw new MappingException("Unable to map " + key.sourceClass + " to " + key.targetClass, e);
         }
     }
 
     @Override
     public boolean allowsToMap(Class sourceClass, Class targetClass, String mappingName) {
         MappingIdentifier key = new MappingIdentifier(sourceClass, targetClass, mappingName);
         return findMappingDelegate(key) != null;
     }
 
     /**
      * @param mappingProvider arbitrary object with {@link Mapping}-annotated methods.
      * @return collection of discovered mappings
      * @throws IllegalMappingDefinitionException if any of {@link Mapping}-annotated methods isn't a valid mapping definition
      * @throws DuplicateMappingDefinitionException if there are duplicate mapping definitions
      */
     public Collection<MappingIdentifier> register(Object mappingProvider) {
         Collection<MappingIdentifier> result = new LinkedList<MappingIdentifier>();
         Method[] methods = mappingProvider.getClass().getDeclaredMethods();
         for (Method method : methods) {
             Mapping mapping = method.getAnnotation(Mapping.class);
             if (mapping != null) {
                 result.add(register(mappingProvider, method, mapping.value()));
             }
         }
         return result;
     }
 
     private MappingIdentifier register(Object mappingProvider, Method method, String mappingName) {
         Class<?> returnType = method.getReturnType();
         Class<?>[] parameterTypes = method.getParameterTypes();
         if (!isValidMapping(returnType, parameterTypes)) {
             throw new IllegalMappingDefinitionException(
                     method + " is annotated with @Mapping but doesn't denote a valid mapping");
         }
         MappingIdentifier key = new MappingIdentifier(parameterTypes[0], returnType == Void.TYPE ?
                 parameterTypes[1] : returnType, mappingName);
         MappingDelegate mappingDelegate = new MappingDelegate(mappingProvider, method);
         assertNotAlreadyRegistered(key, mappingDelegate);
         config.put(key, mappingDelegate);
         return key;
     }
 
     protected boolean isValidMapping(Class<?> returnType, Class<?>[] parameterTypes) {
         int upperBound = returnType == Void.TYPE ? 3 : 2,
             numberOfParameters = parameterTypes.length;
         return (numberOfParameters == upperBound - 1  &&
                    !MappingContext.class.isAssignableFrom(parameterTypes[upperBound - 2])) ||
                (numberOfParameters == upperBound &&
                    MappingContext.class.isAssignableFrom(parameterTypes[upperBound - 1]));
     }
 
     protected void assertNotAlreadyRegistered(MappingIdentifier key, MappingDelegate mappingDelegate) {
         MappingDelegate previousMappingDelegate = config.get(key);
         if (previousMappingDelegate != null) {
             throw new DuplicateMappingDefinitionException("Found duplicate mapping definitions: '" +
                     previousMappingDelegate + "' and '" + mappingDelegate + "'");
         }
     }
 
     protected MappingDelegate findMappingDelegate(MappingIdentifier requestedKey) {
         MappingDelegate mapping = config.get(requestedKey);
         if (mapping == null) {
             MappingIdentifier key = requestedKey;
             do {
                 Class sourceSuperClass = key.sourceClass.getSuperclass();
                 if (sourceSuperClass == null) {
                     break;
                 }
                 key = new MappingIdentifier(sourceSuperClass, key.targetClass, key.mappingName);
                 mapping = config.get(key);
             } while (mapping == null);
             if (mapping != null) {
                 config.put(requestedKey, mapping); // self-training
             }
         }
         return mapping;
     }
 
     public static final class MappingIdentifier {
 
         private final Class sourceClass;
         private final Class targetClass;
         private final String mappingName;
 
         public MappingIdentifier(final Class sourceClass, final Class targetClass, String mappingName) {
             this.sourceClass = sourceClass;
             this.targetClass = targetClass;
             this.mappingName = mappingName;
         }
 
         @Override
         public boolean equals(Object o) {
             if (this == o) return true;
             if (!(o instanceof MappingIdentifier)) return false;
             MappingIdentifier that = (MappingIdentifier) o;
             return mappingName.equals(that.mappingName) &&
                     sourceClass.equals(that.sourceClass) &&
                     targetClass.equals(that.targetClass);
         }
 
         @Override
         public int hashCode() {
             int result = sourceClass.hashCode();
             result = 31 * result + targetClass.hashCode();
             result = 31 * result + mappingName.hashCode();
             return result;
         }
 
         @Override
         public String toString() {
             StringBuilder sb = new StringBuilder();
             sb.append(sourceClass).append("->").append(targetClass);
             if (!"".equals(mappingName)) {
                 sb.append(" ('").append(mappingName).append("')");
             }
             return sb.toString();
         }
     }
 
     private static final class MappingDelegate {
 
         private final Object delegatee;
         private final Method method;
         private final boolean requiresContext;
         private final boolean returnsTarget;
 
         public MappingDelegate(Object delegatee, Method method) {
             this.delegatee = delegatee;
             this.method = method;
             Class<?>[] parameterTypes = method.getParameterTypes();
             this.requiresContext = MappingContext.class.
                     isAssignableFrom(parameterTypes[parameterTypes.length - 1]);
             returnsTarget = method.getReturnType() != Void.TYPE;
         }
 
         @Override
         public String toString() {
             return method.toString();
         }
     }
 }
