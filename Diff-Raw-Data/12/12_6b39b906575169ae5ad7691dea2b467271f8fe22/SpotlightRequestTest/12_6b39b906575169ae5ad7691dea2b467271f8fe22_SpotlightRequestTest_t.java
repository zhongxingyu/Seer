 package org.zoneproject.extractor.plugin.spotlight;
 
 /*
  * #%L
  * ZONE-plugin-Spotlight
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
 
 import java.io.IOException;
 import java.util.ArrayList;
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import static org.junit.Assert.*;
 import org.zoneproject.extractor.utils.Item;
 import org.zoneproject.extractor.utils.Prop;
 
 /**
  *
  * @author Desclaux Christophe <christophe@zouig.org>
  */
 public class SpotlightRequestTest {
     
     public SpotlightRequestTest() {
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
      * Test of getNamedEntities method, of class SpotlightRequest.
      */
     @Test
     public void testGetNamedEntities() throws IOException {
         System.out.println("getNamedEntities");
         String text = "Ce sera un événement musical pas tout à fait comme les autres: Mick Jagger et ses complices dans un concert inédit en France. Les fans des Rolling Stones pourront les découvrir en exclusivité au cinéma, le lundi 4 novembre à 20 heures, avec la diffusion de « Sweet Summer Sun – Hyde Park» Live », le concert historique donné par le groupe, à Londres, cet été. ";
         String endPoint = "http://spotlight.sztaki.hu:2225/rest";
         String f = SpotlightRequest.getResponse(text, endPoint);
         System.out.println(f);
        Annotation[] expRes = {
            new Annotation("http://fr.dbpedia.org/resource/Londres"),
            new Annotation("http://fr.dbpedia.org/resource/Mick_Jagger"),
            new Annotation("http://fr.dbpedia.org/resource/Hyde_Park"),
            new Annotation("http://fr.dbpedia.org/resource/France"),
            new Annotation("http://fr.dbpedia.org/resource/Rolling_Stones")
            
        };
        Annotation[] result = SpotlightRequest.getNamedEntities(f);
        
         assertArrayEquals(expRes, result);
     }
 
 }
