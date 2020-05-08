 package com.sebmaster.infinifurnace;
 
 import org.bukkit.Material;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.event.Event.Type;
 import org.bukkit.event.inventory.FurnaceBurnEvent;
 import org.bukkit.event.inventory.InventoryListener;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 /**
  * Provides the ability, to let a bucket of lava burn a furnace forever.
  * 
  * @author Sebastian Mayr
  */
 public class InfiniFurnace extends JavaPlugin {
 	
 	class InfiniFurnaceListener extends InventoryListener {
 		
 		@Override
 		public void onFurnaceBurn(FurnaceBurnEvent evt) {
			if (evt.getFuel().getType() == Material.LAVA_BUCKET) {
 				evt.getFuel().setAmount(2);
 			}
 		}
 	}
 
 	@Override
     public void onDisable() {}
 
 	@Override
     public void onEnable() {
 		PluginManager pm = this.getServer().getPluginManager();
 		pm.registerEvent(Type.FURNACE_BURN, new InfiniFurnaceListener(), Priority.Normal, this);
     }
 }
