 /*
  * Copyright 2012 Twitter, Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.twitter.tokyo.kucho;
 
 import java.util.List;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import com.twitter.tokyo.kucho.SeatingList;
 
 import static junit.framework.Assert.assertEquals;
 import static junit.framework.Assert.assertNotNull;
 
 public class SeatingListTest {
  private final String userName = "kentaro";
 
   @Before
   public void setUp() {
   }
 
   @Test
   public void testGetVentilationModule() throws Exception {
     SeatingList seatingList = new SeatingList();
 
     List<String> modules = seatingList.getVentilationModules(userName);
     assertNotNull(modules);
     assertEquals(2, modules.size());
     assertEquals("VAV17E-13", modules.get(0));
     assertEquals("VAV17E-14", modules.get(1));
   }
 
 }
