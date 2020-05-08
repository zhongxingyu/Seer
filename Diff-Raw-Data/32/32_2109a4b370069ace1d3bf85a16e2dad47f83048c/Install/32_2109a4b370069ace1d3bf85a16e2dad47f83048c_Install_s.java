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
 
 import com.hp.hpl.jena.query.ResultSet;
 import java.io.File;
 import org.zoneproject.extractor.utils.Database;
 
 /**
  *
  * @author Desclaux Christophe <christophe@zouig.org>
  */
 public class Install {
     public static void main(String[] args) {
         System.out.println("Loading RDF Files for the INSEE Database");
        ResultSet r = Database.runSPARQLRequest(" SELECT ?p WHERE { <http://rdf.insee.fr/geo/2011/COM_10378> ?p ?i } LIMIT 10");
         if(!r.hasNext()){
             Database.loadFolder("http://rdf.insee.fr/geo/2011/",args[0]);
         }
     }
     
 }
