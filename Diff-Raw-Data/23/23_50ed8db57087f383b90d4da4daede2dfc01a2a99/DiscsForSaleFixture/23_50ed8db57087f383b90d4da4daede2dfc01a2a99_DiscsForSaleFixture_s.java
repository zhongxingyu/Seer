 package net.cijug.fitnesse;
 
 import fitlibrary.DoFixture;
 import net.cijug.discgolf.Disc;
 import net.cijug.discgolf.Player;
 import net.cijug.discgolf.Store;
 import java.util.Set;
 
 
 public class DiscsForSaleFixture extends DoFixture {
 
 
     private Store store;
 
     public DiscsForSaleFixture() {
         store = new Store();
     }
 
 
     public Set<Disc> getDiscs() {
         return store.getDiscs();
     }
 
 
     public boolean purchase(Player player, Set<String> discNames) {
         for (String discName : discNames) {
             Disc disc = store.findDisc(discName);
             store.sell(disc);
            player.bought(disc);
         }
         return true;
     }
 
 
    public boolean sellsFor(String name, String price) {
        Disc disc = new Disc(name, new Double(price.replaceAll("\\$", "")));
        return store.addDiscToInventory(disc);
     }
 
 
 }
