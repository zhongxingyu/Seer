 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package me.crosant.timeranks;
 
 import java.util.logging.Logger;
 import net.milkbowl.vault.Vault;
 import org.bukkit.Bukkit;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockPlaceEvent;
 import org.bukkit.plugin.Plugin;
 import ru.tehkode.permissions.PermissionEntity;
 import ru.tehkode.permissions.PermissionManager;
 import ru.tehkode.permissions.bukkit.PermissionsEx;
 
 /**
  *
  * @author Florian
  */
 public class TimeRanksBlockListener implements Listener{
 
     
     private final TimeRanks plugin;
 
     TimeRanksBlockListener(TimeRanks p) {
         plugin = p;
     }
     
     
     
     
         @EventHandler
             public void onBlockPlace(BlockPlaceEvent event){
                 
                 if(plugin.getConfig().getBoolean("Basic.activated") == true){
                     
                 long blocks;
                 Player player = event.getPlayer();
                 Block block = event.getBlock();
                 Material mat = block.getType();
                 //blocks = SQL.getBlocks(player);
                 blocks = TimeRanks.player_blocks.get(player.getName());
 
                 long blocks1 = blocks + 1;
                 //SQL.setBlocks(player, blocks1);
                 TimeRanks.player_blocks.put(player.getName(), blocks1);
                 
                 
                // player.sendMessage(TimeRanks.player_blocks.get(player).toString());
                 
               if(Bukkit.getServer().getPluginManager().isPluginEnabled("PermissionsEx")){
               PermissionManager manager = PermissionsEx.getPermissionManager();
 
               PermissionManager pex = PermissionsEx.getPermissionManager();
               PermissionEntity entity = pex.getUser(player);
               String[] groups =  manager.getUser(player).getGroupsNames();
            /*   int p = 0;
               while (groups.length > p){
               System.out.println(player.getName() + "        !!!!!!!!!!!           " + groups[p]);
               p++;
               }*/
               for(int i = 1; i <=25; i++){
                  // System.out.println(plugin.getConfig().getString("Rank."+ i +".name"));
                   if(plugin.getConfig().getString("Rank."+ i +".name") != null){
                       //System.out.println(plugin.getConfig().getString("Rank."+ i +".name"));
                       if (i > 1){
                           //System.out.println(plugin.getConfig().getString("Rank."+ i +".name"));
                              //                 System.out.println(plugin.getConfig().getString("Messanges.rankup").replace("%rank%", plugin.getConfig().getString("Rank." + i + ".name")));
                           
                       if (TimeRanks.player_blocks.get(player.getName()).equals(plugin.getConfig().getLong("Rank." + i + ".blocks"))){
                  //   System.out.println(plugin.getConfig().getString("Messanges.rankup").replace("%rank%", plugin.getConfig().getString("Rank." + i + ".name")));
                     player.sendMessage(plugin.getConfig().getString("Messanges.rankup").replace("%rank%", plugin.getConfig().getString("Rank." + i + ".name")) );
                     plugin.giveCash(player, plugin.getConfig().getLong("Rank." + i + ".money"));
                     
                     for (int n = 0; n<groups.length; n++){
                        System.out.println(groups[n]);
                     manager.getUser(player).removeGroup(groups[n]);
                     }
                     
                    
                    
                    
                    
                    manager.getUser(player).addGroup(plugin.getConfig().getString("Rank." + i + ".name"));
                     for (int l = 0; l <groups.length; l++){
                   manager.getUser(player).addGroup(groups[l]);
                      }
                   int o = i-1;
                  manager.getUser(player).removeGroup(plugin.getConfig().getString("Rank."+ o + ".name"));   
                       
                       }
                       else {
                      if (TimeRanks.player_blocks.get(player.getName()).equals(plugin.getConfig().getLong("Rank." + i + ".blocks"))){
                     
                     player.sendMessage(plugin.getConfig().getString("Messanges.rankup").replace("%rank%", plugin.getConfig().getString("Rank." + i + ".name")) );
                     plugin.giveCash(player, plugin.getConfig().getLong("Rank." + i + ".money"));
                                         for (int n = 0; n<groups.length; n++){
                     manager.getUser(player).removeGroup(groups[n]);
                     }
                                                          manager.getUser(player).addGroup(plugin.getConfig().getString("Rank." + i + ".name"));
 
                     for (int l = 0; l <groups.length; l++){
                   manager.getUser(player).addGroup(groups[l]);
                      }                                                }
                       }
                 }
                   }
                   
               
                 
 
               }
               
                   
               }
               }
               else {
                   
                      Logger.getLogger("Minecraft").warning("PermissionsEx plugin are not found.");
 
               }
               //  player.sendMessage("You placed a block with ID : " + mat);
     
 }
 }
