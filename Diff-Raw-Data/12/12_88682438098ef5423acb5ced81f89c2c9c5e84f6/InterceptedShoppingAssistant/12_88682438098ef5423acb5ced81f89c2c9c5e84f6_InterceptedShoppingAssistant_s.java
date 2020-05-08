 package org.shoppingassistant.advanced;
 
 import java.util.Collection;
 
 import org.shoppingassistant.ShoppingAssistant;
 import org.shoppingassistant.ShoppingItem;
 import org.snowflake.Answer;
 import org.snowflake.Question;
 import org.snowflake.ValidationException;
 import org.snowflake.devserver.DevServer;
 import org.snowflake.utils.Console;
 
 /**
  * For examples of basic Snowflake features, see {@link ShoppingAssistant}; this
  * class demonstrates how a RequestInterceptor can be used to provide controller
  * methods with custom arguments.
  * 
  * @author haugeto
  */
 public class InterceptedShoppingAssistant {
 
    public Collection<ShoppingItem> index(Question question, Answer answer, Session session) {
         Console.println("index received: " + session);
         answer.setTitle("Welcome!");
         return session.shoppingItems.values();
     }
 
     public ShoppingItem add(Session session) {
         Console.println("add received: " + session);
         ShoppingItem shoppingItem = new ShoppingItem();
         shoppingItem.setId(session.shoppingItems.size() + 1);
         return shoppingItem;
     }
 
     public ShoppingItem edit(Integer id, Session session) {
         Console.println("edit received: " + session);
         return session.shoppingItems.get(id);
     }
 
     public void save(Question question, ShoppingItem shoppingItem, Session session) {
         Console.println("Save received: " + session);
         if (shoppingItem.getQuantity() == 0) {
             throw new ValidationException("quantity", "Zero not allowed");
         }
         if (shoppingItem.getId() == null) {
             // Yes, I know. This is a horrible way of generating an id.
             shoppingItem.setId(session.shoppingItems.size());
         }
         session.shoppingItems.put(shoppingItem.getId(), shoppingItem);
     }
 
     public void more(Integer id, Session session) {
         Console.println("more received: " + session);
         if (session.shoppingItems.containsKey(id)) {
             ShoppingItem shoppingItem = session.shoppingItems.get(id);
             shoppingItem.setQuantity(shoppingItem.getQuantity() + 1);
         }
     }
 
     public void delete(Question question, Integer id, Session session) {
         Console.println("delete received: " + session);
         session.shoppingItems.remove(id);
     }
 
     public static void main(String[] args) throws Exception {
         DevServer devServer = new DevServer("Advanced Shopping Assistant", 3000);
         devServer.addRequestInterceptor(new SessionRequestInterceptor());
         // Register Controller at base URL "shopping":
         devServer.registerController("shopping", new InterceptedShoppingAssistant());
         devServer.run();
     }
 
 }
