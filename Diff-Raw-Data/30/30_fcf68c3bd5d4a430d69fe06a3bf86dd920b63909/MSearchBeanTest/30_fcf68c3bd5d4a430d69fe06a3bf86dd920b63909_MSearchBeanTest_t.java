 /*
  * Copyright 2012 INRIA
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
 package com.mymed.tests.unit.bean;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.fail;
 
 import java.util.Map;
 
 import org.junit.AfterClass;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import com.mymed.model.data.geolocation.MSearchBean;
 
 /**
  * {@link MSearchBean} unit test
  * 
  * @author Milo Casagrande
  * 
  */
 public class MSearchBeanTest {
 
   private static MSearchBean searchBean;
 
   @BeforeClass
   public static void setUpBefore() {
     searchBean = new MSearchBean();
     searchBean.setId("ID");
     searchBean.setLatitude(1000);
     searchBean.setLongitude(1000);
     searchBean.setDate(System.currentTimeMillis());
     searchBean.setLocationId(2000L);
     searchBean.setExpirationDate(0L);
     searchBean.setValue("value");
     searchBean.setDistance(10);
   }
 
   @AfterClass
   public static void cleanUpAfter() {
    searchBean = null; // NOPMD
   }
 
   /**
    * Test the attributeToMap method
    */
   @Test
   public void attributeToMapTest() {
     try {
       final Map<String, byte[]> attributesMap = searchBean.getAttributeToMap();
       assertEquals(8, attributesMap.size());
     } catch (final Exception ex) {
       fail(ex.getMessage());
     }
   }
 
   @Test
   public void notEqualsTest() {
     final MSearchBean actual = new MSearchBean();
     actual.setId("ANOTHER_ID");
     actual.setValue("ANOTHER_VALUE");
 
     assertFalse("The user beans are the same", searchBean.equals(actual));
   }
 
   /**
    * Test equals with a null object
    */
   @Test
   public void nullEqualsTest() {
    assertFalse("The user beans are the same", searchBean.equals(null));
   }
 
   @Test
   public void cloneEqualsTest() {
     final MSearchBean actual = searchBean.clone();
     assertEquals("The user beans are not the same", searchBean, actual);
   }
 }
