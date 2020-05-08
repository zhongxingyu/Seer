 package org.zoneproject.extractor.plugin.inseegeo;
 
 /*
  * #%L
  * ZONE-plugin-INSEEGeo
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
 
 import java.util.ArrayList;
 import java.util.List;
 import junit.framework.Test;
 import junit.framework.TestCase;
 import junit.framework.TestSuite;
 import org.zoneproject.extractor.plugin.opencalais.openCalaisExtractor;
 import org.zoneproject.extractor.utils.Prop;
 
 /**
  *
  * @author Desclaux Christophe <christophe@zouig.org>
  */
 public class AppTest 
     extends TestCase
 {
     /**
      * Create the test case
      *
      * @param testName name of the test case
      */
     public AppTest( String testName )
     {
         super( testName );
     }
 
     /**
      * @return the suite of tests being tested
      */
     public static Test suite()
     {
         return new TestSuite( AppTest.class );
     }
 
 
     @org.junit.Test
     public void testApp1() {
         System.out.println("getCitiesResult");
         String city = "Toulon";
         ArrayList<Prop> expResult = new ArrayList<Prop>();
         expResult.add(new Prop("http://rdf.insee.fr/geo/Arrondissement","http://rdf.insee.fr/geo/2011/ARR_832",false));
         expResult.add(new Prop("http://rdf.insee.fr/geo/Pseudo-canton","http://rdf.insee.fr/geo/2011/CAN_8399",false));
         expResult.add(new Prop("http://rdf.insee.fr/geo/Departement","http://rdf.insee.fr/geo/2011/DEP_83",false));
         expResult.add(new Prop("http://rdf.insee.fr/geo/Region","http://rdf.insee.fr/geo/2011/REG_93",false));
         expResult.add(new Prop("http://rdf.insee.fr/geo/Pays","http://rdf.insee.fr/geo/2011/PAYS_FR",false));
         ArrayList<Prop> result = InseeSparqlRequest.getDimensions(city);
 
         assertEquals(result.size(), expResult.size());
         for(Prop p: expResult){
             assertTrue(result.contains(p));
         }
     }

    @org.junit.Test
    public void testApp2() {
        System.out.println("getCitiesResult");
        String city = "Clermont-L'Hérault";
        ArrayList<Prop> result = InseeSparqlRequest.getDimensions(city);

        assertEquals(0, result.size());
    }
 }
