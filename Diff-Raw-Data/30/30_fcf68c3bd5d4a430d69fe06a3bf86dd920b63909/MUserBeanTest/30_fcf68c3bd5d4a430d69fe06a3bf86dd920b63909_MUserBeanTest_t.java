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
 
 import com.mymed.model.data.user.MUserBean;
 
 public class MUserBeanTest {
 
   private static MUserBean userBean;
 
   @BeforeClass
   public static void setUpBefore() {
     userBean = new MUserBean();
     userBean.setId("ID");
     userBean.setName("NAME");
     userBean.setLastName("LAST_NAME");
     userBean.setFirstName("FIRST_NAME");
     userBean.setEmail("EMAIL@PROVA");
     userBean.setLogin("LOGIN");
   }
 
   /**
    * Check that the getAttributeToMap() method works
    */
   @Test
   public void attributeToMapTest() {
     try {
       final Map<String, byte[]> attributesMap = userBean.getAttributeToMap();
       assertEquals(20, attributesMap.size());
     } catch (final Exception ex) {
       fail(ex.getMessage());
     }
   }
 
   @Test
   public void notEqualsTest() {
     final MUserBean actual = new MUserBean();
     actual.setId("ANOTHER_ID");
     actual.setName("ANOTHER_NAME");
 
     assertFalse("The user beans are the same", userBean.equals(actual));
   }
 
   /**
    * Test equals with null object
    */
   @Test
   public void nullEqualsTest() {
    assertFalse("The user beans are the same", userBean.equals(null));
   }
 
   @Test
   public void cloneEqualsTest() {
     final MUserBean actual = userBean.clone();
     assertEquals("The user beans are not the same", userBean, actual);
   }
 
   @AfterClass
   public static void cleanUpAfter() {
    userBean = null; // NOPMD
   }
 }
