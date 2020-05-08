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
        String json = "{ "
                + "query:'" + query.replace("'", "\\'") + "', ";
        String suggestion = "suggestions:[";
         for (int i = 0; i < products.size(); i++) {
            suggestion += "'" + products.get(i).name.replace("'", "\\'")+ "'";
            if ((i + 1) != products.size()) {
                suggestion += ",";
            }
        }
        suggestion += "],";
        String data = "data:[";
        for (int i = 0; i < products.size(); i++) {
            data += "{id:" + products.get(i).id
                    + ",productType:'simple'}";
             if ((i + 1) != products.size()) {
                data += ",";
             }
         }
        data += "]}";
        json += suggestion;
        json += data;
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
