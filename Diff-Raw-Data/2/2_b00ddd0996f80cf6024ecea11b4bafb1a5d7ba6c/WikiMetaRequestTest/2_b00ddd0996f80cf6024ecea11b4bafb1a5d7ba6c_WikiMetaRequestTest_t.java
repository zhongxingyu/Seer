 package org.zoneproject.extractor.plugin.wikimeta;
 
 /*
  * #%L
  * ZONE-plugin-WikiMeta
  * %%
  * Copyright (C) 2012 ZONE-project
  * %%
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
  * #L%
  */
 
 import org.zoneproject.extractor.plugin.wikimeta.WikiMetaRequest;
 import java.io.File;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.ArrayList;
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 /**
  *
  * @author cdesclau
  */
 public class WikiMetaRequestTest {
     
     public WikiMetaRequestTest() {
     }
     
     @BeforeClass
     public static void setUpClass() {
     }
     
     @AfterClass
     public static void tearDownClass() {
     }
     
     @Before
     public void setUp() {
     }
     
     @After
     public void tearDown() {
     }
 
     /**
      * Test of getProperties method, of class WikiMetaRequest.
      */
     @Test
     public void testGetProperties_String() {
         System.out.println("getProperties");
         String texte = "Bienvenue à Antibes";
         ArrayList result = WikiMetaRequest.getProperties(texte);
         System.out.println(result);
         assertEquals(1,result.size());
     }
 
     /**
      * Test of getProperties method, of class WikiMetaRequest.
      */
     @Test
     public void testGetProperties_File() throws URISyntaxException {
         System.out.println("getProperties");
         URI fileURI = getClass().getResource("/WikiMetaOutput_pip.json").toURI();
         ArrayList result = WikiMetaRequest.getProperties(new File(fileURI));
         assertEquals(result.size(), 0);
     }
     
     /**
      * Test of getProperties method, of class WikiMetaRequest.
      */
     @Test
     public void testGetProperties_File_2() throws URISyntaxException {
         System.out.println("getProperties");
        URI fileURI = getClass().getResource("/WikiMetaOutput_mars.txt").toURI();
         ArrayList result = WikiMetaRequest.getProperties(new File(fileURI));
         System.out.println(result);
         assertEquals(2,result.size());
     }
     
     
     @Test
     public void testGetProperties_String_cleaningResult() {
         System.out.println("getProperties");
         String texte = "Arnaud Montebourg est Arnaud Montebourg";
         ArrayList result = WikiMetaRequest.getProperties(texte);
         System.out.println(result);
         assertEquals(1,result.size());
     }
 }
