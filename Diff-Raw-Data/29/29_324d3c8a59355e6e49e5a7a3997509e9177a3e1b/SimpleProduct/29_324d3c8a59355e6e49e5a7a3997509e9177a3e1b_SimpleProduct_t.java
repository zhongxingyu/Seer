 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package models.po;
 
 import javax.persistence.Entity;
 import java.util.List;
 
 /**
  *
  * @author inf04
  */
 @Entity
 public class SimpleProduct extends Product{
     
     public String name;
 
     public static String suggest(String q) {
         List<SimpleProduct> products = SimpleProduct.find("name like ? ", "%" + q + "%").fetch();
         return createAutoCompleteJson(q, products);
     }
     
     private static String createAutoCompleteJson(String query, List<SimpleProduct> products) {
        String json = "[";
         for (int i = 0; i < products.size(); i++) {
            json += "{\"name\": \"" + products.get(i).name.replace("'", "\\'");

            json += "\"";

            json += ",\"productType\": \"simple\"";
            json += ",\"id\": " +products.get(i).id+"}";
             if ((i + 1) != products.size()) {
                json += ",";
             }
         }
        json += "]";
         return json;
     }
     
     @Override
     public String getProductName() {
         return this.name;
     }
 
     public static SimpleProduct saveOrCreate(String name) {
         SimpleProduct p = SimpleProduct.find("name = ?",name).first();
         if(p == null){
             p = new SimpleProduct();
             p.name = name;
             p.save();
         }
 
         return p;
     }
 }
