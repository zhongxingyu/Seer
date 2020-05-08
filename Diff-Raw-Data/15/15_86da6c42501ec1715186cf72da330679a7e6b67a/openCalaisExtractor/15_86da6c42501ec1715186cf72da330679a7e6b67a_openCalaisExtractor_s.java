 package org.zoneproject.extractor.plugin.opencalais;
 
 /*
  * #%L
  * ZONE-plugin-OpenCalais
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
 
 /**
  * requests to openCalais webservice
  * not used anymore because need too much time 
  */
 
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import mx.bigdata.jcalais.CalaisClient;
 import mx.bigdata.jcalais.CalaisObject;
 import mx.bigdata.jcalais.CalaisResponse;
 import mx.bigdata.jcalais.rest.CalaisRestClient;
 import org.zoneproject.extractor.utils.Config;
 import org.zoneproject.extractor.utils.Prop;
 
 /**
  *
  * @author Desclaux Christophe <christophe@zouig.org>
  */
 public class openCalaisExtractor {
     public static String EntitiesURI = "http://www.opencalais.org/Entities#";
     
     public static CalaisResponse getResult(java.lang.String content) {
         String license = Config.getVar("openCalais-key");
         
         CalaisClient client = new CalaisRestClient(license);
         CalaisResponse response = null;
         try {
             response = client.analyze(content);
         }
         catch (mx.bigdata.jcalais.CalaisException ex){
            return openCalaisExtractor.getResult(content);
         }
         catch (java.net.SocketTimeoutException ex){
            return openCalaisExtractor.getResult(content);
         }
         catch (IOException ex) {
             Logger.getLogger(openCalaisExtractor.class.getName()).log(Level.WARNING, null, ex);
         }
         return  response;
     }
     public static String[] getCitiesResult(java.lang.String content) {
         CalaisResponse response = openCalaisExtractor.getResult(content);
         if(response == null) {
             String [] res = new String[0];
             return res;
         }
         
         ArrayList<String> list = new ArrayList<String>();
         for (CalaisObject entity : response.getEntities()) {
             if(!entity.getField("_type").equals("City"))continue;
             list.add(entity.getField("name"));
         }
         return list.toArray(new String[list.size()]);
     }
     
     public static ArrayList<Prop> getCitiesResultProp(java.lang.String content) {
         String [] cities = getCitiesResult(content);
         ArrayList<Prop> result = new ArrayList<Prop>();
         for(String i : cities){
             result.add(new Prop(EntitiesURI+"LOC",i,true));
         }
         return result;
         
     }
     
     public static String[] getPersonsResult(java.lang.String content) {
         CalaisResponse response = openCalaisExtractor.getResult(content);
         if(response == null) {
             String [] res = new String[0];
             return res;
         }
         ArrayList<String> list = new ArrayList<String>();
         for (CalaisObject entity : response.getEntities()) {
             if(!entity.getField("_type").equals("Person"))continue;
             list.add(entity.getField("name"));
         }
         return list.toArray(new String[list.size()]);
     }
     
     public static ArrayList<Prop> getPersonsResultProp(java.lang.String content) {
         String [] persons = getPersonsResult(content);
         ArrayList<Prop> result = new ArrayList<Prop>();
         for(String i : persons){
             result.add(new Prop(EntitiesURI+"PERSON",i,true));
         }
         return result;
         
     }
 }
