 package net.thedarktide.xolsom.scrapyard;
 
 import java.io.File;
 import java.util.Random;
 
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.plugin.PluginManager;
 
 import org.bukkit.inventory.ItemStack;
 
 import org.bukkit.util.config.Configuration;
 
 import com.nijiko.permissions.PermissionHandler;
 import com.nijikokun.bukkit.Permissions.Permissions;
 import org.bukkit.plugin.Plugin;
 
 /**
  * @author Xolsom
  */
 public class Scrapyard extends JavaPlugin {
     private final ScrapyardYardSaleListener scrapyardYardSaleListener = new ScrapyardYardSaleListener(this);
     private ScrapyardItemDatabase scrapyardItemDatabase = null;
    public static PermissionHandler permissionHandler;
 
     public double Cost;
     
     public void onDisable() {}
     
     public void onEnable() {
         setupPermissions();
         
         scrapyardItemDatabase = new ScrapyardItemDatabase(new File(this.getDataFolder(), "itemdb.yml"), this);
         scrapyardItemDatabase.load();
         
         Configuration config = this.getConfiguration();
         this.Cost = config.getDouble("options.cost", 0.25);
         
         PluginManager pm = getServer().getPluginManager();
         
         pm.registerEvent(Event.Type.CUSTOM_EVENT, scrapyardYardSaleListener, Event.Priority.Normal, this);
         
         getCommand("scrapyard").setExecutor(new ScrapyardCommand(this));
     }
     
     public void callScrapyardYardSaleEvent(Player player) {
         ScrapyardYardSaleEvent scrapyardYardSaleEvent = new ScrapyardYardSaleEvent("YardSaleEvent", player);
         
         this.getServer().getPluginManager().callEvent(scrapyardYardSaleEvent);
     }
     
     public ScrapyardItemDatabase getScrapyardItemDatabase() {
         return scrapyardItemDatabase;
     }
     
     public void setupPermissions() {
         Plugin permissionsPlugin = this.getServer().getPluginManager().getPlugin("Permissions");
        
        if(Scrapyard.permissionHandler == null) {
            if(permissionsPlugin != null) {
                Scrapyard.permissionHandler = ((Permissions)permissionsPlugin).getHandler();
            } else {
                // Blablabla
            }
         }
     }
     
     public boolean hasPermission(Player player, String permissionNode) {
        return Scrapyard.permissionHandler.has(player, permissionNode);
     }
 
     public boolean scrapping(Player player) {
         // Check for permission
         if(!this.hasPermission(player, "scrapyard.use")) {
             player.sendMessage("You don't have permission.");
             return false;
         }
         
         // [0] = MaterialId, [1] = Amount
         int[] item = this.getScrapyardItemDatabase().getItem(player.getItemInHand().getTypeId());
 
         // Is item in list?
         if(item != null) {
             int successes = 0;
 
             Random random = new Random();
 
             // Items durability is the amount of times used, so it should probably be subtracted from the max durability
             double formula = ((player.getItemInHand().getType().getMaxDurability() - player.getItemInHand().getDurability()) / player.getItemInHand().getType().getMaxDurability()) * (1 - this.Cost);
 
             for(int i = 0; i < item[1]; i++, successes += random.nextDouble() < formula ? 1 : 0);
 
             ItemStack itemStack;
             if(successes < 1) {
                 player.sendMessage("The scrapping was not successful.");
 
                 // Set the players hand item to null / AIR
                 itemStack = null;
             } else {
                 // Get valuable material type
 
                 player.sendMessage("You scrapped " + successes + " materials out of your item.");
 
                 itemStack = new ItemStack(item[0], successes);
             }
 
             player.setItemInHand(itemStack);
 
             return true;
         } else {
             player.sendMessage("This item cannnot be scrapped.");
             return false;
         }
     }
 }
