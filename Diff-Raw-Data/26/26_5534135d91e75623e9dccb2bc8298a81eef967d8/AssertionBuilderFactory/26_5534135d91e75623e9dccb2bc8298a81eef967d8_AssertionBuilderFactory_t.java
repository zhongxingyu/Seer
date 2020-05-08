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
 
 import javax.xml.namespace.QName;
 
 import org.apache.neethi.builders.AssertionBuilder;
 import org.apache.neethi.builders.converters.ConverterRegistry;
 
 /**
  * AssertionFactory is used to create an Assertion from an Element. It uses an
  * appropriate AssertionBuilder instance to create an Assertion based on the
  * QName of the given element. Domain Policy authors could right custom
  * AssertionBuilders to build Assertions for domain specific assertions.
  */
 public interface AssertionBuilderFactory {
 
     /**
      * Returns the PolicyEngine associated with this factory
      * @return
      */
     PolicyBuilder getPolicyEngine();
 
     /**
      * Returns the ConverterRegistry that the builder 
      * uses for converting the object to the types
      * needed for the AssertionBuilders
      * @return
      */
     ConverterRegistry getConverterRegistry();
 
     /**
      * Registers an AssertionBuilder with a specified QName.
      * 
      * @param key the QName that the AssertionBuilder understand
      * @param builder the AssertionBuilder that can build an Assertion from
      *            an element of specified type
      */
    void registerBuilder(QName key, AssertionBuilder<?> builder);
 
     /**
      * Registers an AssertionBuilder with all the builder's known elements.
      * 
      * @param builder the AssertionBuilder that can build an Assertion from
      *            an element of specified type
      */
    void registerBuilder(AssertionBuilder<?> builder);
 
     
     /**
      * Returns an assertion that is built using the specified element.
      * 
      * @param element the element that the AssertionBuilder can use to build an
      *            Assertion.
      * @return an Assertion that is built using the specified element.
      */
     Assertion build(Object element);
 
 
     /**
      * Returns an AssertionBuilder that build an Assertion from an element of
      * qname type.
      * 
      * @param qname the type that the AssertionBuilder understands and builds an
      *            Assertion from
      * @return an AssertionBuilder that understands qname type
      */
    public AssertionBuilder<?> getBuilder(QName qname);
 }
