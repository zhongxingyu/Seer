 package stuff;
 
 import items.classes.Bread;
 import items.classes.Club;
 import items.classes.Crown;
 import items.classes.Garlic;
 import items.classes.Item;
 import items.interfaces.Unmoveable;
 
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 
 // By implementing the Iterable interface, Knapsack can be used as the source in a foreach statement.
 public class Knapsack implements Iterable<Item> {
   private List<Item> items = new LinkedList<Item>();
 
   public boolean isEmpty() {
     return items.isEmpty();
   }
 
   // You can only carry three items in your knapsack.
   public static final int MAXIMUM_ALLOWABLE_ITEMS = 3;
 
   // ex. take(HealthItem.Bread) -> returns true if successfully taken
   public boolean addItem(Item item) {
     if(items.size() >= MAXIMUM_ALLOWABLE_ITEMS) {
      System.out.println("Unable to add " + item + " to Knapsack, because it is full.");
      return false;
     }
 
     if(item instanceof Unmoveable) {
      System.out.println(item + " is an unmoveable item.");
      return false;
     }
 
     items.add(item);
     System.out.println("You added the " + item + " to your knapsack.");
     return true;
   }
 
   @Override
   public Iterator<Item> iterator() {
     return items.iterator();
   }
 
   @Override
   public String toString() {
     return items.toString();
   }
 
   public void removeItem(Item item) {
     if(items.remove(item)) {
      System.out.println("The " + item  + " was removed from your knapsack.");
     }
     else {
       System.out.println("You do not have that item in your knapsack.");
     }
   }
 
   public int size() {
     return items.size();
   }
 
   public static void main(String[] args) {
     Knapsack knapsack = new Knapsack();
     System.out.println("-- Test adding more than three items to Knapsack --");
     Item garlic = new Garlic();
     Item crown = new Crown();
     knapsack.addItem(garlic);
     knapsack.addItem(new Bread());
     knapsack.addItem(new Club());
     knapsack.addItem(crown);
     System.out.println("-- Test removing an item from the Knapsack --");
     knapsack.removeItem(garlic);
     System.out.println("-- Test displaying contents of Knapsack --");
     System.out.println(knapsack);
     System.out.println("-- Test removing an item that is not in your Knapsack --");
     knapsack.removeItem(crown);
   }
 }
