 package org.zoneproject.extractor.plugin.wikimeta;
 
 import java.util.ArrayList;
 import org.zoneproject.extractor.utils.Database;
 import org.zoneproject.extractor.utils.Item;
 import org.zoneproject.extractor.utils.Prop;
 
 /**
 * Hello world!
  *
  */
 public class App 
 {
     public static String PLUGIN_URI = "http://www.zone-project.org/plugins/WikiMeta";
     public static void main(String[] args) {
 
         Item[] items = Database.getItemsNotAnotatedForOnePlugin(PLUGIN_URI);
         System.out.println("WikiMeta has "+items.length+" items to annotate");
         for(Item item : items){
             System.out.println("Add ExtractArticlesContent for item: "+item);
             
             ArrayList<Prop> content= WikiMetaRequest.getProperties(item.toString());
             Database.addAnnotations(item.getUri(), content);
             
             Database.addAnnotation(item.getUri(), new Prop(PLUGIN_URI,"true"));
 
         }
     }
 }
