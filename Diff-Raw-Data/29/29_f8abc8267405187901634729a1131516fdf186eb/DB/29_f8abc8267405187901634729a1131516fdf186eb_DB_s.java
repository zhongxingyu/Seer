 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package store.data;
 
 import Item.Item;
 import java.math.BigDecimal;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.Statement;
 import java.util.ArrayList;
 
 /**
  *
  * @author dazza
  */
 public class DB {
     public static ArrayList<Item> getItems(){
         try{
             Class.forName("com.mysql.jdbc.Driver");
         Connection c = DriverManager.getConnection("jdbc:mysql://localhost:3306/shopping","shopping","shopping");
         Statement s = c.createStatement();       
         
         String query = "select * from item";
        
         ResultSet items = s.executeQuery(query);
         ArrayList<Item> itemsArray = new ArrayList<Item>();
         while(items.next()){
             Item itm = new Item(items.getString("name"), items.getString("itemID"), items.getString("description"), new BigDecimal(items.getString("price")),items.getString("specification"),items.getString("photoName"));
             itemsArray.add(itm);
         }
         return itemsArray;
         }catch(Exception e){
             return null;
         }
         
     }
     
    public static Item getItem(String id){
         try{
             Class.forName("com.mysql.jdbc.Driver");
         Connection c = DriverManager.getConnection("jdbc:mysql://localhost:3306/shopping","shopping","shopping");
         Statement s = c.createStatement();       
         
         String query = "select * from item where item.itemID = "+id;
         
         
         ResultSet items = s.executeQuery(query);
         Item itm = new Item(items.getString("name"), items.getString("itemID"), items.getString("description"), new BigDecimal(items.getString("price")),items.getString("specification"),items.getString("photoName"));
         return itm;
        }catch(Exception e){
             return null;
         } 
     }
 }
