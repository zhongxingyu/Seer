 package models;
 
 import org.codehaus.jackson.node.ObjectNode;
 import play.db.ebean.Model;
 import play.libs.Json;
 
 import javax.persistence.Entity;
 import javax.persistence.*;
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * The basic model for an item we're dealing with.
  */
 @Entity
 public class InventoryItem extends Model
 {
     public InventoryItem(String name, Double price, Integer quantity, String extension)
     {
         this.name = name;
         this.price = price;
         this.quantity = quantity;
        this.imgPath = "/assets/images/" +this.name +"." +extension;
     }
     @Id
     public String name;
     public Double price;
     public Integer quantity;
     public String imgPath;
 
     public static Map<String, String> asMap(InventoryItem item)
     {
         Map<String, String> itemAsMap = new HashMap<String, String>();
         itemAsMap.put("name", item.name);
         itemAsMap.put("price", "" +item.price);
         return itemAsMap;
     }
     public static ObjectNode asJsonNode(InventoryItem item)
     {
         ObjectNode node = Json.newObject();
         node.put("name", item.name);
         node.put("price", item.price);
 
         return node;
     }
 }
