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
 
 import java.util.ArrayList;
 import org.zoneproject.extractor.utils.Database;
 import org.zoneproject.extractor.utils.Item;
 import org.zoneproject.extractor.utils.Prop;
 
 /**
  *
  * @author Desclaux Christophe <christophe@zouig.org>
  */
 
 public class App 
 {
     public static String PLUGIN_URI = "http://www.zone-project.org/plugins/WikiMeta";
     
     public App(){
         String [] tmp = {};
         App.main(tmp);
     }
     
     public static void main(String[] args) {
 
         Item[] items = Database.getItemsNotAnotatedForOnePlugin(PLUGIN_URI);
         System.out.println("WikiMeta has "+items.length+" items to annotate");
         for(Item item : items){
            System.out.println("Add WikiMeta for item: "+item);
             
            ArrayList<Prop> content= WikiMetaRequest.getProperties(item.concat());
             Database.addAnnotations(item.getUri(), content);
             
             Database.addAnnotation(item.getUri(), new Prop(PLUGIN_URI,"true"));
 
         }
     }
 }
