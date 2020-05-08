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
 
 import org.metastopheles.*;
 import org.metastopheles.FacetKey;
 import org.testng.annotations.Test;
 
 import java.lang.annotation.Annotation;
 import static org.testng.Assert.*;
 
 public class TestAnnotationBasedMetaDataDecorator
 {
 //**********************************************************************************************************************
 // Fields
 //**********************************************************************************************************************
 
     public static final FacetKey<Boolean> FOUND = new FacetKey<Boolean>() {};
 
 //**********************************************************************************************************************
 // Other Methods
 //**********************************************************************************************************************
 
     @Test
     public void testAnnotationsFound()
     {
         final BeanMetaDataFactory factory = new BeanMetaDataFactory();
         factory.getBeanMetaDataDecorators().add(new FindMeDecorator<BeanMetaData>());
         factory.getMethodMetaDataDecorators().add(new FindMeDecorator<MethodMetaData>());
         factory.getPropertyMetaDataDecorators().add(new FindMeDecorator<PropertyMetaData>());
         BeanMetaData meta = factory.getBeanMetaData(FindMeBean.class);
         assertTrue(meta.getFacet(FOUND));
         assertTrue(meta.getPropertyMetaData("name").getFacet(FOUND));
         assertTrue(meta.getMethodMetaData("someMethod").getFacet(FOUND));
     }
 
 //**********************************************************************************************************************
 // Inner Classes
 //**********************************************************************************************************************
 
    private static class FindMeDecorator<T extends MetaDataObject> extends AnnotationBasedMetaDataDecorator<T,FindMe>
     {
         private FindMeDecorator()
         {
             super(FindMe.class);
         }
 
         @Override
         protected void decorate(T metaData, Annotation annotation)
         {
             metaData.setFacet(FOUND, true);
         }
     }
 }
