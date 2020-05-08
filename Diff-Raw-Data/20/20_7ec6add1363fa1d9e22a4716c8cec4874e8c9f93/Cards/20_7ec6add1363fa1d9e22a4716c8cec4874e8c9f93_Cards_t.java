 package controllers;
 
 import java.util.ArrayList;
 import models.Card;
 import models.Item;
 import models.Restaurant;
 import models.TableRest;
 import play.data.validation.Required;
 import play.mvc.Controller;
 
 public class Cards extends Controller{
     
     public static void index(Long restaurant_id, Long table_id, Long category_id) {
         Clients.items(restaurant_id, table_id, category_id);
     }
     
     public static void createItem(@Required Long table_id, @Required Long item_id) {
         TableRest table = TableRest.findById(table_id);
         Restaurant restaurant = table.restaurant;
         Item item = Item.findById(item_id);
 
         System.out.print(table_id);
 
         Card card = Card.find("byTable_id", table_id).first();
         
         if(card == null){
             card = new Card(restaurant, table);
             card.listItems = new ArrayList<Item>();   
         }
         
        if(card.listItems.contains(item))
        {    
            System.out.println(card.listItems);
            card.listItems.remove(item);
            System.out.println(card.listItems);
            item.quantity++;
            card.listItems.add(item);
            System.out.println(card.listItems);
        }
        else
            card.listItems.add(item);
         card.totalPrice = card.totalPrice+item.price;
         
         card.save();
         index(restaurant.id, table.id, item.category.id);
     }
     
     public static void deleteItem(@Required Long table_id, @Required Long item_id) {
         TableRest table = TableRest.findById(table_id);
         Restaurant restaurant = table.restaurant;
         Item item = Item.findById(item_id);
         System.out.print(table_id);
         
         Card card = Card.find("byTable_id", table_id).first();
         
         card.listItems.remove(item);
         card.totalPrice = card.totalPrice-item.price;
         
         if(!card.listItems.isEmpty()){
             card.save();
         }else{
             card.delete();
         }
         
         Clients.menu(restaurant.id, table.id);
     }
     
     public static void clear(Long card_id) {
         Card card = Card.findById(card_id);
         card.listItems.clear();
         card.delete();
         
         Clients.index(card.restaurant.id, card.table.id);
     }
 
     public static void destroy(Long table_id) {
         TableRest table = TableRest.findById(table_id);
         Card card = Card.find("byTable_id", table_id).first();
         card.listItems.clear();
         card.delete();
         
         Clients.confirmation(table.restaurant.id, table.id);
     }
 }
