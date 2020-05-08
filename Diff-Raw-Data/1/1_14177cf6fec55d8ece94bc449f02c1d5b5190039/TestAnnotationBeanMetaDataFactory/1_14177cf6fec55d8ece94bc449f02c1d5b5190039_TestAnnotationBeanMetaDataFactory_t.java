 /*
  * Copyright (c) 2010 Carman Consulting, Inc.
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
 
 package org.metastopheles.annotation;
 
 import org.metastopheles.BeanMetaData;
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.Test;
 
 import static org.testng.Assert.*;
 
 public class TestAnnotationBeanMetaDataFactory
 {
 //**********************************************************************************************************************
 // Fields
 //**********************************************************************************************************************
 
     private AnnotationBeanMetaDataFactory factory;
 
 //**********************************************************************************************************************
 // Other Methods
 //**********************************************************************************************************************
 
     @BeforeClass
     protected void setUp() throws Exception
     {
         factory = new AnnotationBeanMetaDataFactory();
     }
 
     @Test
     public void testInheritedInstanceDecorators()
     {
         BeanMetaData meta = factory.getBeanMetaData(FindMeBean.class);
         assertTrue(meta.getPropertyMetaData("name").getFacet(EmptyConcreteDecorators.FOUND));
         assertTrue(meta.getPropertyMetaData("name").getFacet(AugmentedConcreteDecorators.FOUND));
     }
 
     @Test
     public void testInstanceDecorators()
     {
         BeanMetaData meta = factory.getBeanMetaData(FindMeBean.class);
         assertTrue(meta.getFacet(InstanceDecorators.FOUND));
         assertTrue(meta.getPropertyMetaData("name").getFacet(InstanceDecorators.FOUND));
         assertTrue(meta.getMethodMetaData("someMethod").getFacet(InstanceDecorators.FOUND));
         assertEquals(InstanceDecorators.getInstanceCount(), 1);
     }
 
     @Test
     public void testStaticDecorators()
     {
         BeanMetaData meta = factory.getBeanMetaData(FindMeBean.class);
         assertTrue(meta.getFacet(StaticDecorators.FOUND));
         assertTrue(meta.getPropertyMetaData("name").getFacet(StaticDecorators.FOUND));
         assertTrue(meta.getMethodMetaData("someMethod").getFacet(StaticDecorators.FOUND));
         assertEquals(StaticDecorators.getInstanceCount(), 0);
     }
 
     @Test()
     public void testBaseClassConstructorWithSystemBaseClass()
     {
         AnnotationBeanMetaDataFactory local = new AnnotationBeanMetaDataFactory(System.class); // Use system classpath!
         BeanMetaData meta = local.getBeanMetaData(FindMeBean.class);
         assertNull(meta.getFacet(StaticDecorators.FOUND));
         assertNull(meta.getPropertyMetaData("name").getFacet(StaticDecorators.FOUND));
         assertNull(meta.getMethodMetaData("someMethod").getFacet(StaticDecorators.FOUND));
     }
 
     @Test(dependsOnMethods = {"testInstanceDecorators", "testStaticDecorators"})
     public void testBaseClassConstructorWithGoodBaseClass()
     {
         AnnotationBeanMetaDataFactory local = new AnnotationBeanMetaDataFactory(FindMeBean.class);
         BeanMetaData meta = local.getBeanMetaData(FindMeBean.class);
         assertTrue(Boolean.TRUE.equals(meta.getFacet(StaticDecorators.FOUND)));
         assertTrue(Boolean.TRUE.equals(meta.getPropertyMetaData("name").getFacet(StaticDecorators.FOUND)));
         assertTrue(Boolean.TRUE.equals(meta.getMethodMetaData("someMethod").getFacet(StaticDecorators.FOUND)));
     }
 }
