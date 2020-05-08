 package com.github.omwah.SDFEconomy;
 
 import com.Acrobot.ChestShop.Events.PreTransactionEvent;
 import com.Acrobot.ChestShop.Events.TransactionEvent;
 import com.github.omwah.SDFEconomy.location.SetDestinationLocationTranslator;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 
 
 /*
  * Listens for ChestShop events and uses them to better direct
  * the location of Vault calls into the API.
  */
 public class ChestShopEventListener implements Listener {
     private final SDFEconomy plugin;
     private final SetDestinationLocationTranslator translator;
     
     /*
      * This listener needs to know about the plugin which it came from
      */
     public ChestShopEventListener(SDFEconomy plugin, SetDestinationLocationTranslator translator) {
         // Register the listener
         plugin.getServer().getPluginManager().registerEvents(this, plugin);
         
         this.plugin = plugin;
         this.translator = translator;
     }
     
     /*
      */
     @EventHandler
     public void onPreTransactionEvent(PreTransactionEvent event) {
        translator.addDestination(event.getOwner().getName(), event.getSign().getLocation());
     }
     
     /*
      */
     @EventHandler
     public void onTransactionEvent(TransactionEvent event) {
         translator.removeDestination(event.getOwner().getName(), event.getSign().getLocation());
     }
     
 }
