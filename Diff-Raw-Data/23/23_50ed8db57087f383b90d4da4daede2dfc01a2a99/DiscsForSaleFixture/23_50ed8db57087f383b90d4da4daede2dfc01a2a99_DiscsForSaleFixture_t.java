 package net.cijug.fitnesse;
 
 import fitlibrary.DoFixture;
 import net.cijug.discgolf.Disc;
 import net.cijug.discgolf.Player;
 import net.cijug.discgolf.Store;
import java.util.Map;
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
            player.buy(disc);
         }
         return true;
     }
 
 
    public boolean sells(Map<String, String> map) {
        Set<String> discNames = map.keySet();
        for (String discName : discNames) {
            Disc disc = new Disc(discName, new Double(map.get(discName).replaceAll("\\$", "")));
            store.addToInventory(disc);
        }
        return true;
     }
 
 
 }
