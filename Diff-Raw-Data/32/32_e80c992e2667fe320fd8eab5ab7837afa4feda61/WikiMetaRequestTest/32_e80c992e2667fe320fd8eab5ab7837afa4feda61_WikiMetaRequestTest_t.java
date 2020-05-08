 package org.zoneproject.extractor.plugin.wikimeta;
 
 /*
  * #%L
  * ZONE-plugin-WikiMeta
  * %%
  * Copyright (C) 2012 ZONE-project
  * %%
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU Affero General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
 import org.zoneproject.extractor.utils.Prop;
 
 /**
  *
  * @author cdesclau
  */
 public class WikiMetaRequestTest {
     private static final org.apache.log4j.Logger  logger = org.apache.log4j.Logger.getLogger(App.class);
     
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
         logger.info("getProperties");
         String texte = "Bienvenue à Antibes";
         ArrayList<Prop> result = WikiMetaRequest.getProperties(texte);
         logger.info(result);
         assertEquals(1,result.size());
         Prop p = new Prop("http://www.wikimeta.org/Entities#loc.admi","http://www.dbpedia.org/resource/Antibes",false);
         assertEquals(result.get(0), p);
     }
 
     /**
      * Test of getProperties method, of class WikiMetaRequest.
      */
     @Test
     public void testGetProperties_File() throws URISyntaxException {
         logger.info("getProperties");
         URI fileURI = getClass().getResource("/WikiMetaOutput_pip.json").toURI();
         ArrayList result = WikiMetaRequest.getProperties(new File(fileURI));
         assertEquals(result.size(), 0);
     }
     
     /**
      * Test of getProperties method, of class WikiMetaRequest.
      */
     @Test
     public void testGetProperties_File_2() throws URISyntaxException {
         logger.info("getProperties");
         URI fileURI = getClass().getResource("/WikiMetaOutput_mars.txt").toURI();
         ArrayList result = WikiMetaRequest.getProperties(new File(fileURI));
         ArrayList<Prop> expRes = new ArrayList<Prop>();
         expRes.add(new Prop("http://www.wikimeta.org/Entities#LOC","http://www.dbpedia.org/resource/Marseille",false));
         expRes.add(new Prop("http://www.wikimeta.org/Entities#ORG","http://www.dbpedia.org/resource/Le_Figaro",false));
         expRes.add(new Prop("http://www.wikimeta.org/Entities#LOC","Bouches-du-Rhône",true));
 
         assertEquals(expRes.size(), result.size());
         for(Prop p: expRes){
             assertTrue(result.contains(p));
         }
         
     }
     
     
     @Test
     public void testGetProperties_String_cleaningResult() {
         logger.info("getProperties");
         String texte = "Arnaud Montebourg est Arnaud Montebourg";
         ArrayList result = WikiMetaRequest.getProperties(texte);
         logger.info(result);
         assertEquals(1,result.size());
     }
     
     @Test
     public void testGetProperties_String_NullStrangeError(){
         logger.info("getProperties");
         String texte = " Sur la Toile, il se faisait appeler Jack Morg";
         ArrayList result = WikiMetaRequest.getProperties(texte);
         logger.info(result);
         ArrayList<Prop> expRes = new ArrayList<Prop>();
         expRes.add(new Prop("http://www.wikimeta.org/Entities#LOC","Toile",true));
         expRes.add(new Prop("http://www.wikimeta.org/Entities#PERS","Jack Morg",true));
         assertEquals(expRes.size(), result.size());
         for(Prop p: expRes){
             assertTrue(result.contains(p));
         }
         
     }
    
    @Test
    public void testGetnamedEntities_String_JSONError(){
        logger.info("getNamedEntities");
        String texte = "L'horloge de Windows 8 n'a pas toujours la bonne heure";
        ArrayList result = WikiMetaRequest.getProperties(texte);
        logger.info(result);
        ArrayList<Prop> expRes = new ArrayList<Prop>();
        expRes.add(new Prop("http://www.wikimeta.org/Entities#prod","http://www.dbpedia.org/resource/Windows_8",false));
        assertEquals(expRes.size(), result.size());
        for(Prop p: expRes){
            assertTrue(result.contains(p));
        }
        
    }
 }
