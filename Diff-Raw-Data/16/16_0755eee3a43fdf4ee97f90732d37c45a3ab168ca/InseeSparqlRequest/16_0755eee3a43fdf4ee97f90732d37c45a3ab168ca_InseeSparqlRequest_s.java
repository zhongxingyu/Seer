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
 import com.hp.hpl.jena.query.QuerySolution;
 import com.hp.hpl.jena.query.ResultSet;
 import java.util.ArrayList;
 import java.util.Date;
 import org.zoneproject.extractor.utils.Database;
 import org.zoneproject.extractor.utils.Prop;
 
 /**
  *
  * @author Desclaux Christophe <christophe@zouig.org>
  */
 public class InseeSparqlRequest {
     
     
     public static ArrayList<Prop> getDimensions(String[] cities){
         ArrayList<Prop> result = new ArrayList<Prop>();
         for(int i=0; i< cities.length;i++){
             result.addAll(getDimensions(cities[i]));
         }
         return result;
     }
     
     public static ArrayList<Prop> getDimensions(String city){
           String query="PREFIX geo: <http://rdf.insee.fr/geo/>" +
                 "\nPREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                   
                 "SELECT DISTINCT ?zone ?type WHERE\n" +
                 "{\n" +
                 "  ?ville geo:nom ?nom.\n"+
                 "  ?zone rdf:type ?type.\n"+
                 "  {\n" +
                 "    SELECT ?ville ?zone ?name WHERE{\n" +
                 "      {?zone geo:subdivision ?ville}\n" +
                 "    }\n" +
                 "  } OPTION ( transitive,\n" +
                 "            t_distinct,\n" +
                 "            t_in(?ville),\n" +
                 "            t_out(?zone),\n" +
                 "            t_min(1)) .\n" +
                "  FILTER ( ?nom = '"+city+"'@fr)\n" +
                 "}";
         System.out.println(query);
         
         ResultSet res = Database.runSPARQLRequest(query);
         ArrayList<Prop> dims = new ArrayList<Prop>();
         while (res.hasNext()) {
             QuerySolution r = res.nextSolution();
             dims.add(new Prop(r.get("?type").toString(), r.get("?zone").toString(),false));
 
         }
         
         return dims;
     }
     
     public static ArrayList<Prop> getDimensionsTooLong(String city){
         String query="PREFIX geo: <http://rdf.insee.fr/geo/>" +
                 "\nPREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                   
                 "SELECT DISTINCT ?zone ?type WHERE\n" +
                 "{\n" +
                 "  ?ville geo:nom ?nom.\n"+
                 "  ?zone rdf:type ?type.\n"+
                 "  {\n" +
                 "    SELECT ?ville ?zone ?name WHERE{\n" +
                 "      {?zone geo:subdivision ?ville}\n" +
                 "    }\n" +
                 "  } OPTION ( transitive,\n" +
                 "            t_distinct,\n" +
                 "            t_in(?ville),\n" +
                 "            t_out(?zone),\n" +
                 "            t_min(1)) .\n" +
                 "  FILTER (regex(str(?nom), '"+city+"', 'i'))\n" +
                 "}";
         System.out.println(query);
         
         ResultSet res = Database.runSPARQLRequest(query);
         ArrayList<Prop> dims = new ArrayList<Prop>();
         while (res.hasNext()) {
             QuerySolution r = res.nextSolution();
             System.out.println(r);
             dims.add(new Prop(r.get("?type").toString(), r.get("?zone").toString(),false));
         }
         
         return dims;
     }
     
     public static ArrayList<Prop> getProperties(ArrayList<Prop> locations){
         
         ArrayList<String> arrElements = new ArrayList<String>();
         for(int i =0; i < locations.size(); i++){
             Prop cur = locations.get(i);
             arrElements.add(cur.getValue());
         }
         return getDimensions(arrElements.toArray(new String[arrElements.size()]));
     }
     
     public static void main(String[] args){
         System.out.println((new Date()).toString());
         System.out.println(getDimensions("Toulon"));
         System.out.println((new Date()).toString());
         System.out.println("Done");
     }
 }
