 package org.zoneproject.extractor.plugin.opencalais;
 
 /*
  * #%L
  * ZONE-plugin-OpenCalais
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
     private static final int MAX_CONTENT_SIZE = 100000;
     
     public static CalaisResponse getResult(java.lang.String content) {
         return getResult(content,10);
     }
     public static CalaisResponse getResult(java.lang.String content,int level) {
         if(level <=0) {
             return null;
         }
         
         String license = Config.getVar("openCalais-key");
         
         CalaisClient client = new CalaisRestClient(license);
         CalaisResponse response = null;
         try {
            content = content.substring(0,Math.min(content.length(),MAX_CONTENT_SIZE));
             response = client.analyze(content);
         }
         catch (mx.bigdata.jcalais.CalaisException ex){
             return openCalaisExtractor.getResult(content,level-1);
         }
         catch (java.net.SocketTimeoutException ex){
             return openCalaisExtractor.getResult(content,level-1);
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
             result.add(new Prop(EntitiesURI+"LOC",i,true,true));
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
             result.add(new Prop(EntitiesURI+"PERSON",i,true,true));
         }
         return result;
         
     }
 }
