 package com.geNAZt.RegionShop.Command;
 
 
 import com.geNAZt.RegionShop.RegionShopPlugin;
 import com.geNAZt.RegionShop.Storages.ListStorage;
import com.geNAZt.RegionShop.Util.Chat;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
 
 /**
  * Created with IntelliJ IDEA.
  * User: geNAZt
  * Date: 06.06.13
  * Time: 19:09
  * To change this template use File | Settings | File Templates.
  */
 public class ShopReload {
     private RegionShopPlugin plugin;
 
     public ShopReload(RegionShopPlugin plugin) {
         this.plugin = plugin;
     }
 
     @SuppressWarnings("unchecked")
    public boolean execute(Player p) {
         ListStorage.reload();
         plugin.reloadConfig();
 
        p.sendMessage(Chat.getPrefix() + ChatColor.YELLOW + "Reloaded " + ChatColor.GOLD + "Config/ShopList");

         return false;
     }
 }
