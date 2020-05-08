 /*
  * Author: John "Ubertweakstor" Board
  * Date: 21/12/12 14:10
  * Description: XRay Detector Plugin
  */
 
 package ubertweakstor.listformatter;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.logging.Logger;
 import net.milkbowl.vault.permission.Permission;
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.RegisteredServiceProvider;
 import org.bukkit.plugin.java.JavaPlugin;
 
 //iterate through online players
 //Put them into groups of rank
 //iterate over ranks
 //dump the contents onto the client's screen.
 
 public class ListFormatter extends JavaPlugin {
     
     static final Logger log = Logger.getLogger("Minecraft");
     CommandListener commandlistener = new CommandListener(this);
     public static Permission permission;
 
     /*
      * Name: onEnable Description: Called when plugin is enabled. Returns: None
      * Parameters: None Requirements: None
      */
     @Override
     public void onEnable() {
         initEvents();
         initPermissions();
        log.info("Enabled.");
         getConfig().options().copyDefaults();
     }
     
     public void initEvents(){
         getServer().getPluginManager().registerEvents(commandlistener, this);
     }
     
     private boolean initPermissions()
     {
         RegisteredServiceProvider<Permission> permissionProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
         if (permissionProvider != null) {
             permission = permissionProvider.getProvider();
         }
         return (permission != null);
     }
 
     /*
      * Name: onDisable Description: Called when plugin is disabled. Returns:
      * None Parameters: None Requirements: None
      */
     @Override
     public void onDisable() {
         log.info("Disabled.");
     }
     
     //===== [ Util ] =====//
     
     public HashMap<String, ArrayList<Player>> sortPlayersIntoRanks(){
         HashMap<String, ArrayList<Player>> answer = new HashMap<String, ArrayList<Player>>();        
         for(Player p: getServer().getOnlinePlayers()){
             System.out.println(p.getName());
             if (answer.containsKey(permission.getPrimaryGroup(p))==false){
                 System.out.println("A");
                 ArrayList<Player> playersInGroup = new ArrayList<Player>();
                 playersInGroup.add(p);
                 answer.put(permission.getPrimaryGroup(p), playersInGroup);
             } else{
                 System.out.println("B");
                 ArrayList<Player> playersInGroup = answer.get(permission.getPrimaryGroup(p));
                 playersInGroup.add(p);
                 answer.put(permission.getPrimaryGroup(p), playersInGroup);
             }
         }
         return answer;
     }
     
     public String getGroupAlias(String group){
         if (!(getConfig().getString("ALIAS_"+group)==null)){
                 return getConfig().getString("ALIAS_"+group);
         }
         else{
                 return group;
         }
     }
     
     public void executeListCommand(Player player){
         player.sendMessage(ChatColor.BLUE+"There is "+ChatColor.RED+String.valueOf(getServer().getOnlinePlayers().length)
                 +ChatColor.BLUE+" out of a maximum of "+ChatColor.AQUA+getServer().getMaxPlayers()+
                 ChatColor.BLUE+
                 " players online.");
         
         List<String> rankOrder = getConfig().getStringList("RankOrder");
         HashMap<String, ArrayList<Player>> groups = sortPlayersIntoRanks();
         
         for (String rank: rankOrder){
             if(groups.containsKey(rank)==true){
                 String rankLine = "";
                 rankLine = rankLine + ChatColor.RED+getGroupAlias(rank)+": ";
                 for(Player p: groups.get(rank)){
                     rankLine = rankLine + p.getDisplayName()+ChatColor.BLUE+", ";
                 }
                 rankLine = rankLine.substring(0, rankLine.length()-2);
                 player.sendMessage(rankLine);
             } else{
                 continue;
             }
         }
     }
 }
