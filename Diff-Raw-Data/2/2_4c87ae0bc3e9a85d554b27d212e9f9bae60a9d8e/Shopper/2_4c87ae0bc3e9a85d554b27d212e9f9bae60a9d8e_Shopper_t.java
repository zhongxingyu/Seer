 package models;
 
 import org.codehaus.jackson.JsonNode;
 import org.codehaus.jackson.map.ObjectMapper;
 import org.codehaus.jackson.node.ArrayNode;
 import org.codehaus.jackson.node.JsonNodeFactory;
 import org.codehaus.jackson.node.ObjectNode;
 import play.db.ebean.Model;
 import play.libs.Json;
 
 import java.util.*;
 
 /**
  * Created with IntelliJ IDEA.
  * User: epanahi
  * Date: 12/6/12
  * Time: 3:38 PM
  * To change this template use File | Settings | File Templates.
  */
 public class Shopper
 {
     /**
      * Process the customer's request; evaluate their needs against the inventory and return to them
      * a list of the optimal purchases.
      * @param items The items that the customer wants, as JsonNodes containing priority, quantity etc.
      * @param budget How much the customer wants to spend
      * @return An object node with fields 'spent', 'budget', 'totalcost', 'bought', and 'notbought' (and 'status' to find out how it went)
      */
     public static ObjectNode goShopping(ArrayNode items, double budget)
     {
         //Get the inventory
         Map<String, InventoryItem> inventory = new Model.Finder(String.class, InventoryItem.class)
                                                             .setMapKey("name").findMap();
 
         //Create a list of 'useful' objects so that it's easy to sort
         List<ObjectNode> cart = new ArrayList<ObjectNode>();
         Iterator<JsonNode> iter = items.getElements();
         while (iter.hasNext())
         {
             JsonNode node = iter.next();
             ObjectNode itemInfo = InventoryItem.asJsonNode(inventory.get(node.get("name").asText()));
             itemInfo.put("buying", node.get("buying"));
             itemInfo.put("priority", node.get("priority"));
 
             cart.add(itemInfo);
         }
 
         Collections.sort(cart, new Comparator<ObjectNode>() {
             @Override
             public int compare(ObjectNode itemOne, ObjectNode itemTwo) {
                 Integer intOne = itemOne.get("priority").asInt();
                 Integer intTwo = itemTwo.get("priority").asInt();
                 int comparison = intOne.compareTo(intTwo) * -1; // reverse order, higher numbers should come first
                 if (comparison == 0) {
                     Double priceOne = itemOne.get("price").asDouble();
                     Double priceTwo = itemTwo.get("price").asDouble();
                     comparison = priceOne.compareTo(priceTwo);
                 }
                 return comparison;
             }
         });
 
         ObjectNode result = Json.newObject();
         ArrayNode bought = JsonNodeFactory.instance.arrayNode();
         ArrayNode notbought = JsonNodeFactory.instance.arrayNode();
         double totalcost = 0.0;
         double spent = 0.0;
         int currentIndex = 0;
 
         Iterator<ObjectNode> buyer = cart.iterator();
 
         while (buyer.hasNext())
         {
 
             ObjectNode node = buyer.next();
 
             double price = node.get("price").asDouble();
             int startingQuantity = node.get("buying").asInt();
             int numLeftToBuy = startingQuantity;
             totalcost += startingQuantity * price;
 
            while ((spent + price) <= budget && numLeftToBuy > 0)
             {
                 System.out.println(node.get("name").asText() + " " + startingQuantity + " " + numLeftToBuy + " " + currentIndex);
                 //Object hasn't been added to the array
                 if (numLeftToBuy == startingQuantity)
                 {
                     //Add the item and set quantity bought to 1
                     bought.add(node);
                     ( (ObjectNode) bought.get(currentIndex)).put("buying", 1);
 
                     //Subtract 1 from the node and numLeftToBuy
                     numLeftToBuy--;
                     spent += price;
                 }
                 else //Increment the item that's already in the array
                 {
                     //We'll reverse these operations because we can use the decremented numLeft to evaluate
                     //how much has now been bought
 
                     numLeftToBuy--;
                     ( (ObjectNode) bought.get(currentIndex)).put("buying", startingQuantity - numLeftToBuy);
                     spent += price;
                 }
             }
             if (numLeftToBuy > 0)
             {
                 ObjectNode didntBuy = JsonNodeFactory.instance.objectNode();
                 didntBuy.putAll(node);
                 didntBuy.put("buying", numLeftToBuy);
                 notbought.add(didntBuy);
             };
             buyer.remove();
             currentIndex++;
 
         }
         result.put("status", "OK");
         result.put("spent", spent);
         result.put("budget", budget);
         result.put("totalcost", totalcost);
         result.put("bought", bought);
         result.put("notbought", notbought);
         return result;
     }
 }
