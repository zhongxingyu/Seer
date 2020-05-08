 package org.zoneproject.extractor.rssreader;
 
 /*
  * #%L
  * ZONE-RSSreader
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
 
 import org.zoneproject.extractor.rssreader.DBpediaRequest;
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 /**
  *
  * @author Desclaux Christophe <christophe@zouig.org>
  */
 public class DBpediaRequestTest {
     private static final org.apache.log4j.Logger  logger = org.apache.log4j.Logger.getLogger(DBpediaRequestTest.class);
     
     public DBpediaRequestTest() {
     }
 
     @BeforeClass
     public static void setUpClass() throws Exception {
     }
 
     @AfterClass
     public static void tearDownClass() throws Exception {
     }
     
     @Before
     public void setUp() {
     }
     
     @After
     public void tearDown() {
     }
 
     /**
      * Test of getCityNameFromURI method, of class DBpediaRequest.
      */
     @Test
     public void testGetCityNameFromURI_1() {
         logger.info("getCityNameFromURI");
         String uri = "http://dbpedia.org/page/House_of_Lorraine";
        String expResult = "House of Lorraine";
         String result = DBpediaRequest.getCityNameFromURI(uri);
         assertEquals(expResult, result);
     }
     @Test
     public void testGetCityNameFromURI_2() {
         logger.info("getCityNameFromURI");
         String uri = "http://dbpedia.org/page/Petit-Couronne";
         String expResult = "Grand-Couronne";
         String result = DBpediaRequest.getCityNameFromURI(uri);
         assertEquals(expResult, result);
     }
     @Test
     public void testGetCityNameFromURI_3() {
         logger.info("getCityNameFromURI");
         String uri = "http://www.dbpedia.org/resource/Pau,_Pyr%C3%A9n%C3%A9es-Atlantiques";
        String expResult = "Pau, Pyrénées-Atlantiques";
         String result = DBpediaRequest.getCityNameFromURI(uri);
         assertEquals(expResult, result);
     }
     @Test
     public void testGetCityNameFromURI_4() {
         logger.info("getCityNameFromURI");
         String uri = "http://www.dbpedia.org/resource/Pau";
         String expResult = "Pau";
         String result = DBpediaRequest.getCityNameFromURI(uri);
         assertEquals(expResult, result);
     }
     @Test
     public void testGetCityNameFromURI_5() {
         logger.info("getCityNameFromURI");
         String uri = "http://dbpedia.org/page/Pau,_Pyr%C3%A9n%C3%A9es-Atlantiques";
        String expResult = "Pau, Pyrénées-Atlantiques";
         String result = DBpediaRequest.getCityNameFromURI(uri);
         assertEquals(expResult, result);
     }
     
     @Test
     public void testGetCityNameFromURI_6() {
         logger.info("getCityNameFromURI");
         String uri = "http://www.dbpedia.org/resource/Antwerp";
        String expResult = "Antwerp";
         String result = DBpediaRequest.getCityNameFromURI(uri);
         assertEquals(expResult, result);
     }
 }
