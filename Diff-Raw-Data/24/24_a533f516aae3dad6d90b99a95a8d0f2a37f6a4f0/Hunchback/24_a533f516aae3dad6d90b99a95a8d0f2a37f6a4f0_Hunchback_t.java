 package characters;
 
 import items.classes.Bread;
 import items.classes.Club;
 import items.classes.Garlic;
import items.classes.Item;
import stuff.Knapsack;
 import stuff.Player;
 
 // A harmless sort of bloke.
 // Doesnt hurt you, but he steals something from your bag.
 // What gets stolen is random.  You do not get to chose.
 // Theyre just too dumb for spells to work on.
 public class Hunchback extends NonPlayableCharacter {
 
   // *** NonPlayableCharacter
   @Override
   public void interactWith(Player player) {
     if(player.knapsack.isEmpty()) {
      System.out.println("The Hunchback just smiles as you walk out the door.");
     }
     else {
      Knapsack knapsack = player.knapsack;
      final int index = (int) (knapsack.size() * Math.random());
      int count = 0;

      for(Item item : knapsack) {
        if(index == count) {
          knapsack.removeItem(item);
          System.out.println("The Hunchback stole your " + item + ".");
          return;
        }
        count++;
      }
     }
   }
 
   @Override
   public String getName() {
     return "Hunchback";
   }
 
   public static void main(String[] args) {
     System.out.println("-- Test Player with empty Knapsack --");
     Player player = new Player();
     NonPlayableCharacter npc = new Hunchback();
 
     npc.interactWith(player);
 
     System.out.println("-- Test Player with full Knapsack --");
     player.knapsack.addItem(new Club());
     player.knapsack.addItem(new Garlic());
     player.knapsack.addItem(new Bread());
 
     npc.interactWith(player);

    System.out.println("-- Contents of Knapsack --");
    System.out.println(player.knapsack);
   }
 }
