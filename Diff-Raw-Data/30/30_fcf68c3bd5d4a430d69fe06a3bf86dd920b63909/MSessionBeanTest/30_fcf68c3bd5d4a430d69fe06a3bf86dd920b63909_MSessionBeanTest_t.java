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
 
 import com.mymed.model.data.session.MSessionBean;
 
 public class MSessionBeanTest {
 
   private static MSessionBean sessionBean;
 
   @BeforeClass
   public static void setUpBefore() {
     sessionBean = new MSessionBean();
 
     sessionBean.setId("ID");
     sessionBean.setIp("IP");
     sessionBean.setAccessToken("ACCESS_TOKEN");
     sessionBean.setUser("USER");
   }
 
   @Test
   public void attributeToMapTest() {
     try {
       final Map<String, byte[]> attributeMap = sessionBean.getAttributeToMap();
       assertEquals(9, attributeMap.size());
     } catch (final Exception ex) {
       fail(ex.getMessage());
     }
   }
 
   @Test
   public void notEqualsTest() {
     final MSessionBean actual = new MSessionBean();
     actual.setId("ANOTHER_ID");
     actual.setUser("ANOTHER_USER");
     actual.setAccessToken("ANOTHER_ACCESS_TOKEN");
     actual.setIp("ANOTHER_IP");
 
     assertFalse("The session beans are the same", sessionBean.equals(actual));
   }
 
   @Test
   public void nullEqualsTest() {
    assertFalse("The session beans are the same", sessionBean.equals(null));
   }
 
   @Test
   public void cloneEqualsTest() {
     final MSessionBean actual = sessionBean.clone();
     assertEquals("The session beans are not the same", sessionBean, actual);
   }
 
   @AfterClass
   public static void cleanUpAfter() {
    sessionBean = null; // NOPMD
   }
 }
