 /**
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements. See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership. The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License. You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied. See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
 
 package org.apache.neethi;
 
 import java.lang.reflect.ParameterizedType;
 import java.util.Map;
 import java.util.concurrent.ConcurrentHashMap;
 
 import javax.xml.namespace.QName;
 
 import org.apache.neethi.builders.AssertionBuilder;
 import org.apache.neethi.builders.converters.ConverterRegistry;
 import org.apache.neethi.builders.xml.XMLPrimitiveAssertionBuilder;
 import org.apache.neethi.util.Service;
 
 /**
  * AssertionFactory is used to create an Assertion from an Element. It uses an
  * appropriate AssertionBuilder instance to create an Assertion based on the
  * QName of the given element. Domain Policy authors could right custom
  * AssertionBuilders to build Assertions for domain specific assertions.
  */
 public class AssertionBuilderFactoryImpl implements AssertionBuilderFactory {
 
    private Map<QName, AssertionBuilder<?>> registeredBuilders 
        = new ConcurrentHashMap<QName, AssertionBuilder<?>>();
     
     protected AssertionBuilder defaultBuilder;
     protected ConverterRegistry converters = new ConverterRegistry();
     protected PolicyBuilder engine;
     
     public AssertionBuilderFactoryImpl(PolicyBuilder eng) {
         engine = eng;
 
         for (AssertionBuilder builder : Service.providers(AssertionBuilder.class)) {
             QName[] knownElements = builder.getKnownElements();
             for (int i = 0; i < knownElements.length; i++) {
                 registeredBuilders.put(knownElements[i], builder);
             }
         }
         defaultBuilder = new XMLPrimitiveAssertionBuilder();
     }
     
     public ConverterRegistry getConverterRegistry() {
         return converters;
     }
     
     public PolicyBuilder getPolicyEngine() {
         return engine;
     }
     
     /**
      * Touch point for subclasses that would like to dynamically load
      * builders when policies are first encountered
      */
     protected void loadDynamic() {
     }
 
     /**
      * Registers an AssertionBuilder with a specified QName.
      * 
      * @param key the QName that the AssertionBuilder understand
      * @param builder the AssertionBuilder that can build an Assertion from
      *            an element of specified type
      */
    public void registerBuilder(QName key, AssertionBuilder<?> builder) {
         loadDynamic();
         registeredBuilders.put(key, builder);
     }
 
    public void registerBuilder(AssertionBuilder<?> builder) {
         loadDynamic();
         for (QName q : builder.getKnownElements()) {
             registeredBuilders.put(q, builder);
         }
     }
     
     /**
      * Returns an assertion that is built using the specified element.
      * 
      * @param element the element that the AssertionBuilder can use to build an
      *            Assertion.
      * @return an Assertion that is built using the specified element.
      */
     public Assertion build(Object element) {
         loadDynamic();
         AssertionBuilder builder;
 
         QName qname = converters.findQName(element);
         builder = registeredBuilders.get(qname);
         if (builder == null) {
             builder = handleNoRegisteredBuilder(qname);
         }
         return invokeBuilder(element, builder);
     }
 
     /**
      * A subclass may want to log when an unknown assertion is encountered
      * or possibly throw an exception or similar.  Another option is to
      * provide a unique AssertionBuilder specific for that QName.
      * @param qname
      * @return a AssertionBuilder to use for the element.
      */
     protected AssertionBuilder handleNoRegisteredBuilder(QName qname) {
          // if we can't locate an appropriate AssertionBuilder, we always use the
          // XMLPrimitiveAssertionBuilder
         return defaultBuilder;
     }
 
     @SuppressWarnings("unchecked")
     private Assertion invokeBuilder(Object element, AssertionBuilder builder) {
         Class<?> type = findAssertionBuilderTarget(builder.getClass());
         return builder.build(converters.convert(element, type), this);
     }
 
     private Class<?> findAssertionBuilderTarget(Class<?> c) {
         Class interfaces[] = c.getInterfaces();
         for (int x = 0; x < interfaces.length; x++) {
             if (interfaces[x] == AssertionBuilder.class) {
                 ParameterizedType pt = (ParameterizedType)c.getGenericInterfaces()[x];
                 return (Class)pt.getActualTypeArguments()[0];
             }
         }
         if (c.getClass().getSuperclass() != null) {
             return findAssertionBuilderTarget(c.getSuperclass());
         }
         return null;
     }
 
 
     /**
      * Returns an AssertionBuilder that build an Assertion from an element of
      * qname type.
      * 
      * @param qname the type that the AssertionBuilder understands and builds an
      *            Assertion from
      * @return an AssertionBuilder that understands qname type
      */
    public AssertionBuilder<?> getBuilder(QName qname) {
         loadDynamic();
         return registeredBuilders.get(qname);
     }
 }
