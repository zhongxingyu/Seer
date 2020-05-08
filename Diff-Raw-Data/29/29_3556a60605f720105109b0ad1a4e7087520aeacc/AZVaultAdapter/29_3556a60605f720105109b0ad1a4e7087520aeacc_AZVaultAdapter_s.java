 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package pl.azpal.azrank.permissions;
 
 import net.milkbowl.vault.permission.Permission;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.RegisteredServiceProvider;
 import pl.azpal.azrank.AZRank;
 
 /**
  *
  * @author Rutr <artuczapl at gmail.com>
  */
 public class AZVaultAdapter extends AZPermissionsHandler{
     
     public static Permission pp;
     
     public AZVaultAdapter(AZRank origin, Permission pp){
         this.plugin=origin;
         this.pp = pp;
     }
     
     public String getName() {
         return(pp.getName());
     }
  
     @Override
     public String[] getPlayersGroups(String player){
        String[] groups = pp.getPlayerGroups((String) null, player);
         if(groups == null)
         {
            String world=plugin.getServer().getWorlds().get(0).getName();
            return pp.getPlayerGroups(world, player);
         }
         else
             return groups;
                
     }
     
     public boolean setPlayersGroups(String player, String[] groups){
        String[] oldGroups=pp.getPlayerGroups((String) null,player);
        int i=0;
         String defaultWorld=plugin.getServer().getWorlds().get(0).getName();
         if(oldGroups==null)
        { //to backward compatybility
            
             oldGroups=pp.getPlayerGroups(defaultWorld,player);
         }
         for(String group : oldGroups)
         {
             if(!pp.playerRemoveGroup((String) null, player, group))
             {
                 if(!pp.playerRemoveGroup(defaultWorld, player, group))
                     plugin.debugmsg("Failed to remove group: "+ group + " from player: "+player);
                 else{
                     plugin.debugmsg("Globaly failed, Removed localy group: "+ group + " from player: "+player+" w:"+defaultWorld);
                     i++;
                 }
             }
             else
                 i++;
         }
 
         int j=0;
         for(String group : groups)
         {
             if(!pp.playerAddGroup((String) null,player, group))
             {
                 if(!pp.playerAddGroup(defaultWorld,player, group))
                     plugin.debugmsg("Failed to add group: "+ group + " to player: "+player);
                 else
                 {
                     plugin.debugmsg("Globaly failed, Added localy group: "+ group + " from player: "+player+" w:"+defaultWorld);
                     j++;
                 }
             }
             else
                 j++;
         }
         plugin.debugmsg("removed: "+i+"/"+oldGroups.length+ " added: "+j+"/"+groups.length);
         if(i==0 || j==0){
             return false;
         }
         return true;
     }
     
 }
