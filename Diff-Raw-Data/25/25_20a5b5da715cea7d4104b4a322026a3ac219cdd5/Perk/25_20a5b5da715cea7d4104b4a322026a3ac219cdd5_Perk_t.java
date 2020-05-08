 package Lihad.Conflict.Perk;
 import Lihad.Conflict.*;
 
 import java.util.List;
 
 import org.bukkit.entity.Player;
 
 public class Perk implements org.bukkit.event.Listener {
 
     public Perk(String n) { name = n; }
     
     public void Initialize(org.bukkit.plugin.Plugin plugin) { }
 
     String name;
     public String getName() { return name; }
     
     Node node;
     public Node getNode() { return node; }
     public void setNode(Node n) { node = n; }
 
     boolean purchasable = false;
     int purchaseCost = 500;
     int useCost = 0;
     int useCostUnowned = 0;
     public void setPurchaseCost(int c) { purchaseCost = c; }
     public void setUseCost(int c) { useCost = c; }
     public void setUseCostUnowned(int c) { useCostUnowned = c; }
     public boolean isPurchasable() { return purchasable; }
     public int getPurchaseCost() { return purchaseCost; }
     public int getUseCost() { return useCost; }
     public int getUseCostUnowned() { return useCostUnowned; }
 
     public static Perk getPerkByName(String name) {
         for (Perk p : Conflict.perks) {
             if(name.equalsIgnoreCase(p.getName())){
                 return p;
             }
         }
         return null;
     }
     
     public static List<String> getPerkNameList(java.util.Collection<Perk> perklist, boolean purchasableOnly) {
         List<String> perkNames = new java.util.LinkedList<String>();
         for (Perk p : perklist) {
             if ((!purchasableOnly) || p.isPurchasable()) {
                 perkNames.add(p.getName());
             }
         }
         return perkNames;
     }
 
     public boolean load(org.bukkit.configuration.ConfigurationSection section) {
        //purchasable = section.getBoolean("purchasable", false);
        //if (purchasable) { purchaseCost = section.getInt("purchasecost", 500); }
         useCost = section.getInt("usecost", 0);
         useCostUnowned = section.getInt("usecostunowned", 0);
         return true;
     }
     
     public void save(org.bukkit.configuration.ConfigurationSection section) {
         section.set("purchasable", false);
         if (purchasable) {
             section.set("purchasecost", purchaseCost);
         }
         if (useCost > 0) section.set("usecost", useCost);
         if (useCostUnowned > 0) section.set("usecostunowned", useCostUnowned);
     }
 
     @org.bukkit.event.EventHandler
     public void eventHandlerEntityDamageByEntity(org.bukkit.event.entity.EntityDamageByEntityEvent event) {
         for (Perk p : Conflict.perks) {
             if (p instanceof PassivePerk) {
                 p.onEntityDamageByEntity(event);
             }
         }
     }
     public void onEntityDamageByEntity(org.bukkit.event.entity.EntityDamageByEntityEvent event) { }
 
     @org.bukkit.event.EventHandler
     public void eventHandlerEntityDeath(org.bukkit.event.entity.EntityDeathEvent event){
         for (Perk p : Conflict.perks) {
             if (p instanceof PassivePerk) {
                 p.onEntityDeath(event);
             }
         }
     }
     public void onEntityDeath(org.bukkit.event.entity.EntityDeathEvent event) { }
 
     @org.bukkit.event.EventHandler
     public void eventHandlerPlayerInteractEntity(org.bukkit.event.player.PlayerInteractEntityEvent event) {
         for (Perk p : Conflict.perks) {
             if (p instanceof PassivePerk) {
                 p.onPlayerInteractEntity(event);
             }
         }
     }
     public void onPlayerInteractEntity(org.bukkit.event.player.PlayerInteractEntityEvent event) { }
     
 };
