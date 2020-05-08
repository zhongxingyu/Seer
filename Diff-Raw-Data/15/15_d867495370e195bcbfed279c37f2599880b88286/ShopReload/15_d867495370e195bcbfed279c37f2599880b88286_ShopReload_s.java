 package com.geNAZt.RegionShop.Command;
 
 
 import com.geNAZt.RegionShop.RegionShopPlugin;
 import com.geNAZt.RegionShop.Storages.ListStorage;
 
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
    public boolean execute() {
         ListStorage.reload();
         plugin.reloadConfig();
 
         return false;
     }
 }
